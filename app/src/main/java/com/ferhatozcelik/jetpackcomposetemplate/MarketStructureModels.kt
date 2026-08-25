package com.ferhatozcelik.jetpackcomposetemplate

enum class Trend { BULLISH, BEARISH }
enum class BreakType { BOS, CHOCH }

data class Candle(
    val openTime: Long,
    val open: Double,
    val high: Double,
    val low: Double,
    val close: Double,
    val volume: Double
)

enum class SwingType { HIGH, LOW }

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
