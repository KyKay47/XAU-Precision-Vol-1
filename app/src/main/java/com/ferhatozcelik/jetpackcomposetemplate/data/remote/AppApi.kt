package com.ferhatozcelik.jetpackcomposetemplate.data.remote

import retrofit2.http.GET
import retrofit2.http.Query

interface AppApi {
    @GET("api/v3/klines")
    suspend fun getKlines(
        @Query("symbol") symbol: String = "PAXGUSDT",
        @Query("interval") interval: String,
        @Query("limit") limit: Int = 75
    ): List<List<Any>>
}
