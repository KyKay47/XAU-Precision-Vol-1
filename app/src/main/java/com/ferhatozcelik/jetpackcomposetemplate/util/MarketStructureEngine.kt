package com.ferhatozcelik.jetpackcomposetemplate.util

import com.ferhatozcelik.jetpackcomposetemplate.Candle

enum class Trend { BULLISH, BEARISH, NEUTRAL }
enum class TradeSignal { BUY, SELL, WAIT }

data class MultiTimeframeCandles(
    val h4: List<Candle>,
    val h1: List<Candle>,
    val m15: List<Candle>
)

data class MarketStructure(
    val h4Trend: Trend,
    val h1Trend: Trend,
    val swingHigh: Double,
    val swingLow: Double,
    val isBOS: Boolean
)

data class PriceAction(
    val isBullishEngulfing: Boolean,
    val isBearishEngulfing: Boolean,
    val hasRejectionWick: Boolean
)

data class VolatilityMovement(
    val isExplosive: Boolean,
    val averageRange: Double
)

data class TradeBounds(
    val entry: Double,
    val stopLoss: Double,
    val takeProfit: Double
)

data class CompleteMarketAnalysis(
    val signal: TradeSignal,
    val structure: MarketStructure,
    val action: PriceAction,
    val movement: VolatilityMovement,
    val bounds: TradeBounds,
    val rationale: String
)

class MarketStructureEngine(private val swingLookback: Int = 3) {

    private fun determineTrend(candles: List<Candle>): Trend {
        if (candles.size < swingLookback * 2) return Trend.NEUTRAL
        val recentHighs = candles.takeLast(10).map { it.high }
        val recentLows = candles.takeLast(10).map { it.low }

        val isHigherHighs = recentHighs.last() > recentHighs.first()
        val isHigherLows = recentLows.last() > recentLows.first()
        val isLowerHighs = recentHighs.last() < recentHighs.first()
        val isLowerLows = recentLows.last() < recentLows.first()

        return when {
            isHigherHighs && isHigherLows -> Trend.BULLISH
            isLowerHighs && isLowerLows -> Trend.BEARISH
            else -> Trend.NEUTRAL
        }
    }

    private fun analyzeMovement(candle: Candle): VolatilityMovement {
        val range = candle.high - candle.low
        val body = Math.abs(candle.close - candle.open)
        val isExplosive = range > 0 && (body / range) >= 0.70
        return VolatilityMovement(isExplosive = isExplosive, averageRange = range)
    }

    private fun analyzeAction(candles: List<Candle>): PriceAction {
        if (candles.size < 2) return PriceAction(false, false, false)
        val current = candles.last()
        val previous = candles[candles.size - 2]

        val isBullishEngulfing = current.close > previous.high && current.close > current.open
        val isBearishEngulfing = current.close < previous.low && current.close < current.open

        val totalRange = current.high - current.low
        val lowerWick = Math.min(current.open, current.close) - current.low
        val upperWick = current.high - Math.max(current.open, current.close)
        val hasRejectionWick = totalRange > 0 && ((lowerWick / totalRange >= 0.40) || (upperWick / totalRange >= 0.40))

        return PriceAction(
            isBullishEngulfing = isBullishEngulfing,
            isBearishEngulfing = isBearishEngulfing,
            hasRejectionWick = hasRejectionWick
        )
    }

    private fun calculateBounds(signal: TradeSignal, currentClose: Double): TradeBounds {
        val riskAmount = 1.50
        val rrRatio = 2.0

        return when (signal) {
            TradeSignal.BUY -> {
                val sl = currentClose - riskAmount
                val tp = currentClose + (riskAmount * rrRatio)
                TradeBounds(entry = currentClose, stopLoss = sl, takeProfit = tp)
            }
            TradeSignal.SELL -> {
                val sl = currentClose + riskAmount
                val tp = currentClose - (riskAmount * rrRatio)
                TradeBounds(entry = currentClose, stopLoss = sl, takeProfit = tp)
            }
            TradeSignal.WAIT -> TradeBounds(0.0, 0.0, 0.0)
        }
    }

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

        val signal = when {
            h4Trend == Trend.BULLISH && h1Trend == Trend.BULLISH && isBullishBOS && movement.isExplosive -> TradeSignal.BUY
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
