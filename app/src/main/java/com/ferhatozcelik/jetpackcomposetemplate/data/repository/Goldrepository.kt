package com.ferhatozcelik.jetpackcomposetemplate.data.repository

import com.ferhatozcelik.jetpackcomposetemplate.data.model.Candle
import com.ferhatozcelik.jetpackcomposetemplate.data.model.parseBinanceCandles
import com.ferhatozcelik.jetpackcomposetemplate.data.remote.GoldApiService
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope

data class MultiTimeframeCandles(
    val h4: List<Candle>,
    val h1: List<Candle>,
    val m15: List<Candle>
)

class GoldRepository(private val apiService: GoldApiService) {

    suspend fun fetchGoldCandles(): MultiTimeframeCandles = coroutineScope {
        val h4Deferred = async { apiService.getKlines(interval = "4h") }
        val h1Deferred = async { apiService.getKlines(interval = "1h") }
        val m15Deferred = async { apiService.getKlines(interval = "15m") }

        MultiTimeframeCandles(
            h4 = parseBinanceCandles(h4Deferred.await()),
            h1 = parseBinanceCandles(h1Deferred.await()),
            m15 = parseBinanceCandles(m15Deferred.await())
        )
    }
}
