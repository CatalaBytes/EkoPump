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

    suspend fun addRefuel(refuel: RefuelEntity) = dao.insert(refuel)
    suspend fun deleteRefuel(refuel: RefuelEntity) = dao.delete(refuel)
}
