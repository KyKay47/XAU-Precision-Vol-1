package com.ferhatozcelik.jetpackcomposetemplate

enum class SignalType { BUY, SELL }

data class TradeSignal(
    val type: SignalType,
    val entryPrice: Double,
    val stopLoss: Double,
    val targetPrice: Double,
    val timestamp: Long,
    val reason: String
)

class PriceActionTriggerEngine(
    private val riskRewardRatio: Double = 2.0 // Minimum 1:2 R:R
) {

    /**
     * Evaluates current market price action against active unmitigated zones.
     * Generates a trade setup if confirmation criteria are met.
     */
    fun evaluateEntry(
        currentCandle: Candle,
        previousCandle: Candle,
        activeZones: List<SmartMoneyZone>
    ): TradeSignal? {
        val unmitigatedZones = activeZones.filter { !it.isMitigated }

        for (zone in unmitigatedZones) {
            when (zone.type) {
                ZoneType.BULLISH_OB, ZoneType.BULLISH_FVG -> {
                    // Check if current candle low taps into the bullish POI zone
                    if (currentCandle.low <= zone.topPrice && currentCandle.low >= zone.bottomPrice) {
                        // Confirmation 1: Bullish Engulfing
                        val isBullishEngulfing = currentCandle.close > previousCandle.high && currentCandle.close > currentCandle.open

                        // Confirmation 2: Strong Rejection Wick (Lower wick is at least 50% of candle body)
                        val totalRange = currentCandle.high - currentCandle.low
                        val lowerWick = Math.min(currentCandle.open, currentCandle.close) - currentCandle.low
                        val isWickRejection = totalRange > 0 && (lowerWick / totalRange) >= 0.50

                        if (isBullishEngulfing || isWickRejection) {
                            val entry = currentCandle.close
                            val sl = currentCandle.low - 0.50 // Small buffer below low
                            val risk = entry - sl
                            val tp = entry + (risk * riskRewardRatio)

                            return TradeSignal(
                                type = SignalType.BUY,
                                entryPrice = entry,
                                stopLoss = sl,
                                targetPrice = tp,
                                timestamp = currentCandle.openTime,
                                reason = "Mitigated ${zone.type.name} with ${if (isBullishEngulfing) "Bullish Engulfing" else "Wick Rejection"}"
                            )
                        }
                    }
                }

                ZoneType.BEARISH_OB, ZoneType.BEARISH_FVG -> {
                    // Check if current candle high taps into the bearish POI zone
                    if (currentCandle.high >= zone.bottomPrice && currentCandle.high <= zone.topPrice) {
                        // Confirmation 1: Bearish Engulfing
                        val isBearishEngulfing = currentCandle.close < previousCandle.low && currentCandle.close < currentCandle.open

                        // Confirmation 2: Strong Rejection Wick (Upper wick is at least 50% of candle body)
                        val totalRange = currentCandle.high - currentCandle.low
                        val upperWick = currentCandle.high - Math.max(currentCandle.open, currentCandle.close)
                        val isWickRejection = totalRange > 0 && (upperWick / totalRange) >= 0.50

                        if (isBearishEngulfing || isWickRejection) {
                            val entry = currentCandle.close
                            val sl = currentCandle.high + 0.50 // Small buffer above high
                            val risk = sl - entry
                            val tp = entry - (risk * riskRewardRatio)

                            return TradeSignal(
                                type = SignalType.BUY,
                                entryPrice = entry,
                                stopLoss = sl,
                                targetPrice = tp,
                                timestamp = currentCandle.openTime,
                                reason = "Mitigated ${zone.type.name} with ${if (isBearishEngulfing) "Bearish Engulfing" else "Wick Rejection"}"
                            )
                        }
                    }
                }
            }
        }
        return null
    }
}

