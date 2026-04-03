package com.catalabytes.ekopump.data.api

import retrofit2.http.GET

interface MineturApiService {
    @GET("EstacionesTerrestres/")
    suspend fun getEstaciones(): MineturResponse
}
