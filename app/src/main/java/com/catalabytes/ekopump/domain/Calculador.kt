package com.catalabytes.ekopump.domain

import kotlin.math.*

/**
 * Calcula si vale la pena ir a una gasolinera más lejana.
 *
 * @param precioRef     precio de la gasolinera de referencia (más cercana) €/L
 * @param precioDestino precio de la gasolinera destino €/L
 * @param kmExtra       kilómetros extra de desvío (ida)
 * @param consumoL100   consumo del coche en L/100km
 * @param litrosRepostar litros que se van a repostar
 * @return AhorroResult con el beneficio neto y si vale la pena
 */
data class AhorroResult(
    val ahorroBruto: Double,      // € ahorrados solo por precio
    val costeKmExtra: Double,     // € gastados en combustible extra
    val beneficioNeto: Double,    // ahorroBruto - costeKmExtra
    val valeLaPena: Boolean
)

data class AhorroDoble(
    val dePaso: AhorroResult,     // sin km extra (pasas por allí de todos modos)
    val deCasa: AhorroResult      // viaje específico desde punto habitual
)

fun calcularAhorro(
    precioRef: Double,
    precioDestino: Double,
    kmExtra: Double,
    consumoL100: Double,
    litrosRepostar: Double
): AhorroResult {
    val ahorroBruto   = (precioRef - precioDestino) * litrosRepostar
    val litrosKmExtra = (kmExtra * 2 * consumoL100) / 100.0   // ida y vuelta
    val costeKmExtra  = litrosKmExtra * precioDestino
    val beneficioNeto = ahorroBruto - costeKmExtra
    return AhorroResult(
        ahorroBruto   = ahorroBruto,
        costeKmExtra  = costeKmExtra,
        beneficioNeto = beneficioNeto,
        valeLaPena    = beneficioNeto > 0.05   // margen mínimo de 5 céntimos
    )
}

fun haversineKm(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
    val R = 6371.0
    val dLat = (lat2 - lat1) * PI / 180.0
    val dLon = (lon2 - lon1) * PI / 180.0
    val a = sin(dLat / 2).pow(2) +
            cos(lat1 * PI / 180.0) * cos(lat2 * PI / 180.0) * sin(dLon / 2).pow(2)
    return R * 2.0 * atan2(sqrt(a), sqrt(1.0 - a))
}

fun calcularAhorroDoble(
    precioRef: Double,
    precioDestino: Double,
    distanciaHabitualKm: Double,
    consumoL100: Double,
    litrosRepostar: Double
): AhorroDoble = AhorroDoble(
    dePaso = calcularAhorro(precioRef, precioDestino, 0.0, consumoL100, litrosRepostar),
    deCasa = calcularAhorro(precioRef, precioDestino, distanciaHabitualKm, consumoL100, litrosRepostar)
)
