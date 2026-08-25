package com.ferhatozcelik.jetpackcomposetemplate

// ==========================================
// 1. DATA MODELS & ENUMS
// ==========================================

enum class Trend { BULLISH, BEARISH }
enum class BreakType { BOS, CHOCH }
enum class SwingType { HIGH, LOW }

data class Candle(
    val openTime: Long,
    val open: Double,
    val high: Double,
    val low: Double,
    val close: Double,
    val volume: Double
)

data class SwingPoint(
    val type: SwingType,
    val price: Double,
    val time: Long,
    val index: Int
)

data class StructuralBreak(
    val type: BreakType,
    val trendAfterBreak: Trend,
    val breakPrice: Double,
    val breakTime: Long,
    val brokenSwing: SwingPoint
)

// ==========================================
// 2. MARKET STRUCTURE ENGINE LOGIC
// ==========================================

class MarketStructureEngine(private val swingLookback: Int = 3) {

    private var currentTrend: Trend = Trend.BULLISH
    private var lastSwingHigh: SwingPoint? = null
    private var lastSwingLow: SwingPoint? = null

    /**
     * Analyzes a list of candles sorted chronologically (oldest to newest).
     * Returns all detected structural breaks (BOS/CHOCH) in sequential order.
     */
    fun analyze(candles: List<Candle>): List<StructuralBreak> {
        val structuralBreaks = mutableListOf<StructuralBreak>()

        if (candles.size < (swingLookback * 2 + 1)) return structuralBreaks

        for (i in swingLookback until (candles.size - swingLookback)) {
            val currentCandle = candles[i]

            // 1. Check if index 'i' is a local Swing High
            if (isSwingHigh(candles, i)) {
                lastSwingHigh = SwingPoint(SwingType.HIGH, currentCandle.high, currentCandle.openTime, i)
            }

            // 2. Check if index 'i' is a local Swing Low
            if (isSwingLow(candles, i)) {
                lastSwingLow = SwingPoint(SwingType.LOW, currentCandle.low, currentCandle.openTime, i)
            }

            // 3. Evaluate structural breaks against current closed candle
            val activeCandle = candles[i]
            val breakEvent = evaluateBreak(activeCandle)
            if (breakEvent != null) {
                structuralBreaks.add(breakEvent)
            }
        }

        return structuralBreaks
    }

    private fun isSwingHigh(candles: List<Candle>, index: Int): Boolean {
        val targetHigh = candles[index].high
        for (offset in 1..swingLookback) {
            if (candles[index - offset].high >= targetHigh || candles[index + offset].high > targetHigh) {
                return false
            }
        }
        return true
    }

    private fun isSwingLow(candles: List<Candle>, index: Int): Boolean {
        val targetLow = candles[index].low
        for (offset in 1..swingLookback) {
            if (candles[index - offset].low <= targetLow || candles[index + offset].low < targetLow) {
                return false
            }
        }
        return true
    }

    private fun evaluateBreak(candle: Candle): StructuralBreak? {
        val high = lastSwingHigh
        val low = lastSwingLow

        // Bullish Break (Close above last Swing High)
        if (high != null && candle.close > high.price) {
            val breakType = if (currentTrend == Trend.BULLISH) BreakType.BOS else BreakType.CHOCH
            currentTrend = Trend.BULLISH
            
            // Invalidate swing point to prevent duplicate triggers
            lastSwingHigh = null 

            return StructuralBreak(
                type = breakType,
                trendAfterBreak = Trend.BULLISH,
                breakPrice = candle.close,
                breakTime = candle.openTime,
                brokenSwing = high
            )
        }

        // Bearish Break (Close below last Swing Low)
        if (low != null && candle.close < low.price) {
            val breakType = if (currentTrend == Trend.BEARISH) BreakType.BOS else BreakType.CHOCH
            currentTrend = Trend.BEARISH
            
            // Invalidate swing point to prevent duplicate triggers
            lastSwingLow = null 

            return StructuralBreak(
                type = breakType,
                trendAfterBreak = Trend.BEARISH,
                breakPrice = candle.close,
                breakTime = candle.openTime,
                brokenSwing = low
            )
        }

        return null
    }
}

