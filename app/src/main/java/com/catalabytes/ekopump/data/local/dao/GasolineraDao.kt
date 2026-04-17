package com.catalabytes.ekopump.data.local.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import com.catalabytes.ekopump.data.local.entity.GasolineraEntity

@Dao
interface GasolineraDao {

    @Upsert
    suspend fun upsertAll(gasolineras: List<GasolineraEntity>)

    @Query("SELECT * FROM gasolineras_cache")
    suspend fun getAll(): List<GasolineraEntity>

    @Query("DELETE FROM gasolineras_cache")
    suspend fun deleteAll()

    @Query("SELECT MAX(cachedAt) FROM gasolineras_cache")
    suspend fun getCachedAt(): Long?
}
