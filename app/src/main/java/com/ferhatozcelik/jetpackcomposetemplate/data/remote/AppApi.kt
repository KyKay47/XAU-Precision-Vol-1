package com.ferhatozcelik.jetpackcomposetemplate.data.remote

import retrofit2.http.GET
import retrofit2.http.Query

interface GoldApiService {
    @GET("api/v3/ticker/price")
    suspend fun getSymbolPrice(
        @Query("symbol") symbol: String = "PAXGUSDT"
    ): Any
}
