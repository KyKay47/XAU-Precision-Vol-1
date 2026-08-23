package com.ferhatozcelik.jetpackcomposetemplate.util

import com.ferhatozcelik.jetpackcomposetemplate.data.model.Candle
import com.ferhatozcelik.jetpackcomposetemplate.data.repository.MultiTimeframeCandles
import kotlin.math.abs

enum class Trend { BULLISH, BEARISH, RANGING }
enum class Pattern { BULLISH_ENGULFING, BEARISH_ENGULFING, PINBAR_REJECTION, NONE }
enum class TradeSignal { BUY, SELL, WAIT }

// 1. PRICE MOVEMENT: Measures velocity and body expansion
data class PriceMovement(
    val rangeInDollars: Double,
    val bodySize: Double,
    val isExplosive: Boolean
)

// 2. PRICE ACTION: Identifies candle patterns and rejection wicks
data class PriceAction(
    val pattern: Pattern,
    val upperWick: Double,
    val lowerWick: Double,
    val hasRejection: Boolean
)

// 3. MARKET STRUCTURE: Identifies trend and key swings
data class MarketStructure(
    val h4Trend: Trend,
    val h1Trend: Trend,
    val swingHigh: Double,
    val swingLow: Double,
    val isBOS: Boolean
)

// 4. TRADE EXECUTION BOUNDS
data class TradeExecutionBounds(
    val entryZoneMin: Double,
    val entryZoneMax: Double,
    val stopLoss: Double,
    val takeProfit1: Double,
    val takeProfit2: Double
)

// UNIFIED ENGINE RESULT
data class CompleteMarketAnalysis(
    val signal: TradeSignal,
    val structure: MarketStructure,
    val action: PriceAction,
    val movement: PriceMovement,
    val bounds: TradeExecutionBounds?,
    val rationale: String
)

object MarketStructureEngine {

    // Evaluate Price Movement
    private fun analyzeMovement(candle: Candle): PriceMovement {
        val range = candle.high - candle.low
        val body = abs(candle.close - candle.open)
        val isExplosive = body > (range * 0.6) // Body makes up > 60% of total bar range
        return PriceMovement(rangeInDollars = range, bodySize = body, isExplosive = isExplosive)
    }

    // Evaluate Price Action
    private fun analyzeAction(candles: List<Candle>): PriceAction {
        if (candles.size < 2) return PriceAction(Pattern.NONE, 0.0, 0.0, false)

        val current = candles.last()
        val prev = candles[candles.size - 2]
        val range = current.high - current.low

        val upperWick = current.high - maxOf(current.open, current.close)
        val lowerWick = minOf(current.open, current.close) - current.low

        // Rejection Wick Check (> 45% of total bar range)
        val hasUpperRejection = range > 0 && (upperWick / range) > 0.45
        val hasLowerRejection = range > 0 && (lowerWick / range) > 0.45

        // Engulfing Check
        val isBullishEngulfing = current.close > prev.high && current.open < prev.low
        val isBearishEngulfing = current.close < prev.low && current.open > prev.high

        val pattern = when {
            isBullishEngulfing -> Pattern.BULLISH_ENGULFING
            isBearishEngulfing -> Pattern.BEARISH_ENGULFING
            hasLowerRejection || hasUpperRejection -> Pattern.PINBAR_REJECTION
            else -> Pattern.NONE
        }

        return PriceAction(
            pattern = pattern,
            upperWick = upperWick,
            lowerWick = lowerWick,
            hasRejection = hasLowerRejection || hasUpperRejection
        )
    }

    // Evaluate Market Structure Trend
    private fun determineTrend(candles: List<Candle>): Trend {
        if (candles.size < 5) return Trend.RANGING
        val recentHighs = candles.takeLast(5).map { it.high }
        val recentLows = candles.takeLast(5).map { it.low }

        val higherHighs = recentHighs.last() > recentHighs.first()
        val higherLows = recentLows.last() > recentLows.first()
        val lowerHighs = recentHighs.last() < recentHighs.first()
        val lowerLows = recentLows.last() < recentLows.first()

        return when {
            higherHighs && higherLows -> Trend.BULLISH
            lowerHighs && lowerLows -> Trend.BEARISH
            else -> Trend.RANGING
        }
    }

    // Dynamic Trade Execution Bounds Calculation
    private fun calculateBounds(
        signal: TradeSignal,
        lastPrice: Double,
        slPips: Double = 7.41,
        tp1Pips: Double = 7.41,
        tp2Pips: Double = 14.82
    ): TradeExecutionBounds? {
        if (signal == TradeSignal.WAIT) return null

        val entryZoneMin = lastPrice - 0.50
        val entryZoneMax = lastPrice + 0.99
        val entryMid = (entryZoneMin + entryZoneMax) / 2.0

        return if (signal == TradeSignal.BUY) {
            // BUY Logic: SL below entry, TP above entry
            TradeExecutionBounds(
                entryZoneMin = entryZoneMin,
                entryZoneMax = entryZoneMax,
                stopLoss = entryMid - slPips,
                takeProfit1 = entryMid + tp1Pips,
                takeProfit2 = entryMid + tp2Pips
            )
        } else {
            // SELL Logic: SL above entry, TP below entry
            TradeExecutionBounds(
                entryZoneMin = entryZoneMin,
                entryZoneMax = entryZoneMax,
                stopLoss = entryMid + slPips,
                takeProfit1 = entryMid - tp1Pips,
                takeProfit2 = entryMid - tp2Pips
            )
        }
    }

    // Master Aggregator
    fun evaluate(data: MultiTimeframeCandles): CompleteMarketAnalysis {
        val h4Trend = determineTrend(data.h4)
        val h1Trend = determineTrend(data.h1)

        val m15Candles = data.m15
        val lastCandle = m15Candles.last()

        val movement = analyzeMovement(lastCandle)
        val action = analyzeAction(m15Candles)

        val swingHigh = m15Candles.takeLast(10).maxOf { it.high }
        val swingLow = m15Candles.takeLast(10).minOf { it.low }

        val isBullishBOS = lastCandle.close > swingHigh
        val isBearishBOS = lastCandle.close < swingLow

        val structure = MarketStructure(
            h4Trend = h4Trend,
            h1Trend = h1Trend,
            swingHigh = swingHigh,
            swingLow = swingLow,
            isBOS = isBullishBOS || isBearishBOS
        )

        // Confluence Signal Decision
        val signal = when {
            // BUY: Trend Bullish + Break of Structure + Strong Price Action/Movement
            h4Trend == Trend.BULLISH && h1Trend == Trend.BULLISH && isBullishBOS && movement.isExplosive -> TradeSignal.BUY
            // SELL: Trend Bearish + Break of Structure + Strong Price Action/Movement
            h4Trend == Trend.BEARISH && h1Trend == Trend.BEARISH && isBearishBOS && movement.isExplosive -> TradeSignal.SELL
            else -> TradeSignal.WAIT
        }

        val rationale = when (signal) {
            TradeSignal.BUY -> "High-confluence BUY: H4/H1 Bullish alignment, M15 BOS above SwingHigh with explosive momentum."
            TradeSignal.SELL -> "High-confluence SELL: H4/H1 Bearish alignment, M15 BOS below SwingLow with explosive momentum."
            TradeSignal.WAIT -> "WAIT: Market lacking full confluence across Movement, Price Action, or Structure."
        }

        val bounds = calculateBounds(signal, lastCandle.close)

        return CompleteMarketAnalysis(
            signal = signal,
            structure = structure,
            action = action,
            movement = movement,
            bounds = bounds,
            rationale = rationale
        )
    }
}

    // Master Aggregator
    fun evaluate(data: MultiTimeframeCandles): CompleteMarketAnalysis {
        val h4Trend = determineTrend(data.h4)
        val h1Trend = determineTrend(data.h1)
        
        val m15Candles = data.m15
        val lastCandle = m15Candles.last()
        
        val movement = analyzeMovement(lastCandle)
        val action = analyzeAction(m15Candles)

        val swingHigh = m15Candles.takeLast(10).maxOf { it.high }
        val swingLow = m15Candles.takeLast(10).minOf { it.low }
        
        val isBullishBOS = lastCandle.close > swingHigh
        val isBearishBOS = lastCandle.close < swingLow

        val structure = MarketStructure(
            h4Trend = h4Trend,
            h1Trend = h1Trend,
            swingHigh = swingHigh,
            swingLow = swingLow,
            isBOS = isBullishBOS || isBearishBOS
        )

        // Confluence Signal Decision
        val signal = when {
            // BUY: Trend Bullish + Break of Structure + Strong Price Action/Movement
            h4Trend == Trend.BULLISH && h1Trend == Trend.BULLISH && isBullishBOS && movement.isExplosive -> TradeSignal.BUY
            // SELL: Trend Bearish + Break of Structure + Strong Price Action/Movement
            h4Trend == Trend.BEARISH && h1Trend == Trend.BEARISH && isBearishBOS && movement.isExplosive -> TradeSignal.SELL
            else -> TradeSignal.WAIT
        }

        val rationale = when (signal) {
            TradeSignal.BUY -> "High-confluence BUY: H4/H1 Bullish alignment, M15 BOS above $swingHigh with explosive movement."
            TradeSignal.SELL -> "High-confluence SELL: H4/H1 Bearish alignment, M15 BOS below $swingLow with explosive movement."
            TradeSignal.WAIT -> "WAIT: Market lacking full confluence across Movement, Price Action, or Structure."
        }

        return CompleteMarketAnalysis(
            signal = signal,
            structure = structure,
            action = action,
            movement = movement,
            rationale = rationale
        )
    }
}

