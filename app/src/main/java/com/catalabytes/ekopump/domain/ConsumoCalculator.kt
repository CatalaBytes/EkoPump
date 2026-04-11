package com.catalabytes.ekopump.domain

import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ConsumoCalculator @Inject constructor() {

    /** Consumo real en L/100km a partir de litros repostados y km recorridos desde el último repostaje. */
    fun calcularConsumoReal(litrosRepostados: Float, kmRecorridos: Int): Float =
        (litrosRepostados / kmRecorridos.toFloat()) * 100f

    /** Kilómetros de autonomía estimados con los litros actuales en depósito. */
    fun calcularAutonomiaRestante(litrosActuales: Float, consumoL100: Float): Int =
        ((litrosActuales / consumoL100) * 100f).toInt()

    /** Valida que los datos de entrada son razonables antes de persistir. */
    fun validarDatos(litros: Float, kmRecorridos: Int): Boolean =
        litros in 1f..200f && kmRecorridos in 1..2000
}
