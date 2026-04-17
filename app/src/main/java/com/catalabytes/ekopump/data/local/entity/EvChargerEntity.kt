package com.catalabytes.ekopump.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverter
import com.catalabytes.ekopump.domain.model.EvConexion
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory

@Entity(tableName = "ev_charger_cache")
data class EvChargerEntity(
    @PrimaryKey val id: Int,
    val nombre: String,
    val direccion: String?,
    val localidad: String?,
    val latitud: Double,
    val longitud: Double,
    val distanciaKm: Double?,
    val operador: String?,
    val totalPuntos: Int?,
    val esOperacional: Boolean,
    val esPublico: Boolean,
    val coste: String?,
    val conexiones: List<EvConexion>,
    val cachedAt: Long
)

class EvChargerConverters {
    private val moshi = Moshi.Builder().addLast(KotlinJsonAdapterFactory()).build()
    private val adapter = moshi.adapter<List<EvConexion>>(
        Types.newParameterizedType(List::class.java, EvConexion::class.java)
    )

    @TypeConverter
    fun fromConexiones(json: String): List<EvConexion> =
        adapter.fromJson(json) ?: emptyList()

    @TypeConverter
    fun toConexiones(list: List<EvConexion>): String = adapter.toJson(list)
}
