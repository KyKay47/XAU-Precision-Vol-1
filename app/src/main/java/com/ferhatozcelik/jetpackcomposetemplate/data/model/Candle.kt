package com.ferhatozcelik.jetpackcomposetemplate.data.model

data class Candle(
    val open: Double,
    val high: Double,
    val low: Double,
    val close: Double
)

fun parseBinanceCandles(rawKlines: List<List<Any>>): List<Candle> {
    return rawKlines.map { kline ->
        Candle(
            open = kline[1].toString().toDouble(),
            high = kline[2].toString().toDouble(),
            low = kline[3].toString().toDouble(),
            close = kline[4].toString().toDouble()
        )
    }
}
