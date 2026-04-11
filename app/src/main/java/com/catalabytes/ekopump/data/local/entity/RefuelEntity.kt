package com.catalabytes.ekopump.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "refuel_history")
data class RefuelEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val stationName: String,
    val stationAddress: String,
    val fuelType: String,
    val pricePerLiter: Double,
    val liters: Double,
    val totalCost: Double,
    val savedAmount: Double,
    val latitude: Double,
    val longitude: Double,
    val timestamp: Long = System.currentTimeMillis(),
    // v2: odómetro, consumo real, ahorro estimado, referencia gasolinera
    val odometroKm: Int? = null,
    val consumoRealL100: Float? = null,
    val ahorroEstimadoEur: Float? = null,
    val gasolineraId: String? = null,
    val gasolineraName: String? = null
)
