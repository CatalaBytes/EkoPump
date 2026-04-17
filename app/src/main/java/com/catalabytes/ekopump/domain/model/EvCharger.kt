package com.catalabytes.ekopump.domain.model

data class EvCharger(
    val id: Int,
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
    val conexiones: List<EvConexion>
)

data class EvConexion(
    val tipoConector: String,
    val potenciaKw: Double?,
    val cantidad: Int?,
    val esCargaRapida: Boolean
)
