package com.catalabytes.ekopump.data.local.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import com.catalabytes.ekopump.data.local.entity.EvChargerEntity

@Dao
interface EvChargerDao {

    @Upsert
    suspend fun upsertAll(chargers: List<EvChargerEntity>)

    @Query("SELECT * FROM ev_charger_cache")
    suspend fun getAll(): List<EvChargerEntity>

    @Query("DELETE FROM ev_charger_cache")
    suspend fun deleteAll()

    @Query("SELECT MAX(cachedAt) FROM ev_charger_cache")
    suspend fun getCachedAt(): Long?
}
