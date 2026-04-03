package com.catalabytes.ekopump.domain.model

data class Gasolinera(
    val id: String,
    val nombre: String,
    val direccion: String,
    val localidad: String,
    val provincia: String,
    val latitud: Double,
    val longitud: Double,
    val horario: String,
    val gasolina95: Double?,
    val gasolina98: Double?,
    val gasoleoA: Double?,
    val gasoleoB: Double?,
    val gasoleoPremium: Double?,
    val glp: Double?
)
