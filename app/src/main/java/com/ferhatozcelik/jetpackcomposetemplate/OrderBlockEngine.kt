package com.ferhatozcelik.jetpackcomposetemplate

enum class ZoneType { BULLISH_FVG, BEARISH_FVG, BULLISH_OB, BEARISH_OB }

data class SmartMoneyZone(
    val type: ZoneType,
    val topPrice: Double,
    val bottomPrice: Double,
    val creationTime: Long,
    var isMitigated: Boolean = false
)

class ZoneDetectionEngine(private val fvgMinThreshold: Double = 0.5) {

    fun detectFairValueGaps(candles: List<Candle>): List<SmartMoneyZone> {
        val fvgList = mutableListOf<SmartMoneyZone>()
        if (candles.size < 3) return fvgList

        for (i in 2 until candles.size) {
            val c1 = candles[i - 2]
            val c2 = candles[i - 1]
            val c3 = candles[i]

            if (c3.low > c1.high && (c3.low - c1.high) >= fvgMinThreshold) {
                fvgList.add(
                    SmartMoneyZone(
                        type = ZoneType.BULLISH_FVG,
                        topPrice = c3.low,
                        bottomPrice = c1.high,
                        creationTime = c2.openTime
                    )
                )
            }

            if (c3.high < c1.low && (c1.low - c3.high) >= fvgMinThreshold) {
                fvgList.add(
                    SmartMoneyZone(
                        type = ZoneType.BEARISH_FVG,
                        topPrice = c1.low,
                        bottomPrice = c3.high,
                        creationTime = c2.openTime
                    )
                )
            }
        }
        return fvgList
    }

    fun detectOrderBlocks(candles: List<Candle>, structuralBreaks: List<StructuralBreak>): List<SmartMoneyZone> {
        val orderBlocks = mutableListOf<SmartMoneyZone>()

        for (breakEvent in structuralBreaks) {
            val breakIndex = breakEvent.brokenSwing.index
            if (breakIndex <= 0) continue

            if (breakEvent.trendAfterBreak == Trend.BULLISH) {
                for (j in breakIndex downTo 0) {
                    val candle = candles[j]
                    if (candle.close < candle.open) {
                        orderBlocks.add(
                            SmartMoneyZone(
                                type = ZoneType.BULLISH_OB,
                                topPrice = candle.high,
                                bottomPrice = candle.low,
                                creationTime = candle.openTime
                            )
                        )
                        break
                    }
                }
            } else if (breakEvent.trendAfterBreak == Trend.BEARISH) {
                for (j in breakIndex downTo 0) {
                    val candle = candles[j]
                    if (candle.close > candle.open) {
                        orderBlocks.add(
                            SmartMoneyZone(
                                type = ZoneType.BEARISH_OB,
                                topPrice = candle.high,
                                bottomPrice = candle.low,
                                creationTime = candle.openTime
                            )
                        )
                        break
                    }
                }
            }
        }
        return orderBlocks
    }

    fun updateMitigationState(zones: List<SmartMoneyZone>, currentCandle: Candle) {
        for (zone in zones) {
            if (zone.isMitigated) continue

            when (zone.type) {
                ZoneType.BULLISH_FVG, ZoneType.BULLISH_OB -> {
                    if (currentCandle.low <= zone.topPrice) {
                        zone.isMitigated = true
                    }
                }
                ZoneType.BEARISH_FVG, ZoneType.BEARISH_OB -> {
                    if (currentCandle.high >= zone.bottomPrice) {
                        zone.isMitigated = true
                    }
                }
            }
        }
    }
}

