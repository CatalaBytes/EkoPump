package com.catalabytes.ekopump.data.api.ocm

import retrofit2.http.GET
import retrofit2.http.Query

interface OcmApiService {
    @GET("poi/")
    suspend fun getPoi(
        @Query("latitude") latitude: Double,
        @Query("longitude") longitude: Double,
        @Query("distance") distance: Double = 10.0,
        @Query("distanceunit") distanceUnit: String = "KM",
        @Query("maxresults") maxResults: Int = 50
    ): List<OcmPoi>
}
