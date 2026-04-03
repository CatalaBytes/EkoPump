package com.catalabytes.ekopump.data.repository

import android.location.Location
import com.catalabytes.ekopump.data.api.MineturApiService
import com.catalabytes.ekopump.data.location.LocationProvider
import com.catalabytes.ekopump.data.mapper.toDomain
import com.catalabytes.ekopump.domain.model.Gasolinera
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GasolinerasRepository @Inject constructor(
    private val api: MineturApiService,
    private val locationProvider: LocationProvider
) {
    suspend fun getGasolineras(): List<Gasolinera> =
        api.getEstaciones().listaEESSPrecio.map { it.toDomain() }

    suspend fun getGasolinerasCercanas(
        combustible: Combustible = Combustible.GASOLINA_95,
        maxKm: Double = 10.0
    ): List<GasolineraConDistancia> {
        val ubicacion = locationProvider.getLocation()
        val todas = getGasolineras()

        return if (ubicacion != null) {
            todas
                .filter { it.latitud != 0.0 && it.longitud != 0.0 }
                .map { g ->
                    val distancia = calcularDistancia(
                        ubicacion.latitude, ubicacion.longitude,
                        g.latitud, g.longitud
                    )
                    GasolineraConDistancia(g, distancia)
                }
                .filter { (it.distanciaKm ?: Double.MAX_VALUE) <= maxKm }
                .sortedBy { combustible.precio(it.gasolinera) ?: Double.MAX_VALUE }
        } else {
            todas
                .sortedBy { combustible.precio(it) ?: Double.MAX_VALUE }
                .take(50)
                .map { GasolineraConDistancia(it, null) }
        }
    }

    private fun calcularDistancia(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
        val results = FloatArray(1)
        Location.distanceBetween(lat1, lon1, lat2, lon2, results)
        return results[0] / 1000.0
    }
}

data class GasolineraConDistancia(
    val gasolinera: Gasolinera,
    val distanciaKm: Double?
)

enum class Combustible(val label: String, val precio: (Gasolinera) -> Double?) {
    GASOLINA_95("Gasolina 95", { it.gasolina95 }),
    GASOLINA_98("Gasolina 98", { it.gasolina98 }),
    GASOLEO_A("Diésel", { it.gasoleoA }),
    GASOLEO_PREMIUM("Diésel Premium", { it.gasoleoPremium }),
    GLP("GLP", { it.glp })
}
