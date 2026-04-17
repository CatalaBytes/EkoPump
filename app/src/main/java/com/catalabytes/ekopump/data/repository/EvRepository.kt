package com.catalabytes.ekopump.data.repository

import com.catalabytes.ekopump.data.api.ocm.OcmApiService
import com.catalabytes.ekopump.data.api.ocm.toDomain
import com.catalabytes.ekopump.data.api.ocm.toEntity
import com.catalabytes.ekopump.data.local.dao.EvChargerDao
import com.catalabytes.ekopump.domain.model.EvCharger
import javax.inject.Inject
import javax.inject.Singleton

private const val TTL_EV_MS = 2 * 60 * 60 * 1000L // 2 horas

@Singleton
class EvRepository @Inject constructor(
    private val api: OcmApiService,
    private val dao: EvChargerDao
) {
    suspend fun getChargers(lat: Double, lon: Double): List<EvCharger> {
        val ahora = System.currentTimeMillis()
        val cachedAt = dao.getCachedAt() ?: 0L
        if (ahora - cachedAt < TTL_EV_MS) {
            val cache = dao.getAll()
            if (cache.isNotEmpty()) return cache.map { it.toDomain() }
        }
        val frescos = api.getPoi(latitude = lat, longitude = lon).map { it.toDomain() }
        dao.deleteAll()
        dao.upsertAll(frescos.map { it.toEntity(ahora) })
        return frescos
    }
}
