package com.ferhatozcelik.jetpackcomposetemplate.data.remote

import retrofit2.http.GET
import retrofit2.http.Query

interface GoldApiService {
    @GET("api/v3/klines")
    suspend fun getKlines(
        @Query("symbol") symbol: String = "PAXGUSDT", // Set to Gold spot symbol
        @Query("interval") interval: String,
        @Query("limit") limit: Int = 100
    ): List<List<Any>>
}
