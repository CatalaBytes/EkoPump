package com.catalabytes.ekopump.domain

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
