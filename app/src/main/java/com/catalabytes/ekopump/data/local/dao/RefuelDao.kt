package com.catalabytes.ekopump.data.local.dao

import androidx.room.*
import com.catalabytes.ekopump.data.local.entity.RefuelEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface RefuelDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(refuel: RefuelEntity): Long

    @Delete
    suspend fun delete(refuel: RefuelEntity)

    @Query("SELECT * FROM refuel_history ORDER BY timestamp DESC")
    fun getAllRefuels(): Flow<List<RefuelEntity>>

    @Query("SELECT * FROM refuel_history ORDER BY timestamp DESC LIMIT 1")
    suspend fun getLastRefuel(): RefuelEntity?

    @Query("SELECT SUM(totalCost) FROM refuel_history")
    fun getTotalSpent(): Flow<Double?>

    @Query("SELECT SUM(savedAmount) FROM refuel_history")
    fun getTotalSaved(): Flow<Double?>

    @Query("SELECT SUM(liters) FROM refuel_history")
    fun getTotalLiters(): Flow<Double?>

    @Query("SELECT COUNT(*) FROM refuel_history")
    fun getRefuelCount(): Flow<Int>

    // Queries filtradas por mes (startMs = inicio del mes actual en milisegundos)
    @Query("SELECT * FROM refuel_history WHERE timestamp >= :startMs ORDER BY timestamp DESC")
    fun getRefuelsSince(startMs: Long): Flow<List<RefuelEntity>>

    @Query("SELECT SUM(totalCost) FROM refuel_history WHERE timestamp >= :startMs")
    fun getTotalSpentSince(startMs: Long): Flow<Double?>

    @Query("SELECT SUM(liters) FROM refuel_history WHERE timestamp >= :startMs")
    fun getTotalLitersSince(startMs: Long): Flow<Double?>

    @Query("SELECT AVG(consumoRealL100) FROM refuel_history WHERE consumoRealL100 IS NOT NULL AND timestamp >= :startMs")
    fun getAvgConsumoRealSince(startMs: Long): Flow<Float?>

    @Query("SELECT SUM(ahorroEstimadoEur) FROM refuel_history WHERE ahorroEstimadoEur IS NOT NULL AND timestamp >= :startMs")
    fun getTotalAhorroSince(startMs: Long): Flow<Float?>
}
