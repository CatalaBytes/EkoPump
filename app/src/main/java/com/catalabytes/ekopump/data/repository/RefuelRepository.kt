package com.catalabytes.ekopump.data.repository

import com.catalabytes.ekopump.data.local.dao.RefuelDao
import com.catalabytes.ekopump.data.local.entity.RefuelEntity
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RefuelRepository @Inject constructor(
    private val dao: RefuelDao
) {
    val allRefuels: Flow<List<RefuelEntity>> = dao.getAllRefuels()
    val totalSpent: Flow<Double?> = dao.getTotalSpent()
    val totalSaved: Flow<Double?> = dao.getTotalSaved()
    val totalLiters: Flow<Double?> = dao.getTotalLiters()
    val refuelCount: Flow<Int> = dao.getRefuelCount()

    fun getRefuelsSince(startMs: Long): Flow<List<RefuelEntity>>   = dao.getRefuelsSince(startMs)
    fun getTotalSpentSince(startMs: Long): Flow<Double?>           = dao.getTotalSpentSince(startMs)
    fun getTotalLitersSince(startMs: Long): Flow<Double?>          = dao.getTotalLitersSince(startMs)
    fun getAvgConsumoRealSince(startMs: Long): Flow<Float?>        = dao.getAvgConsumoRealSince(startMs)
    fun getTotalAhorroSince(startMs: Long): Flow<Float?>           = dao.getTotalAhorroSince(startMs)
    suspend fun getLastRefuel(): RefuelEntity?                     = dao.getLastRefuel()

    suspend fun addRefuel(refuel: RefuelEntity) = dao.insert(refuel)
    suspend fun deleteRefuel(refuel: RefuelEntity) = dao.delete(refuel)
}
