package com.catalabytes.ekopump.data.repository

import android.location.Location
import androidx.annotation.StringRes
import com.catalabytes.ekopump.R
import com.catalabytes.ekopump.data.api.MineturApiService
import com.catalabytes.ekopump.data.local.dao.GasolineraDao
import com.catalabytes.ekopump.data.local.entity.GasolineraEntity
import com.catalabytes.ekopump.data.location.LocationProvider
import com.catalabytes.ekopump.data.mapper.toDomain
import com.catalabytes.ekopump.data.mapper.toEntity
import com.catalabytes.ekopump.domain.model.Gasolinera
import javax.inject.Inject
import javax.inject.Singleton

private const val TTL_MS = 30 * 60 * 1000L // 30 minutos

@Singleton
class GasolinerasRepository @Inject constructor(
    private val api: MineturApiService,
    private val locationProvider: LocationProvider,
    private val gasolineraDao: GasolineraDao
) {
    suspend fun getGasolineras(): List<Gasolinera> {
        val ahora = System.currentTimeMillis()
        val cachedAt = gasolineraDao.getCachedAt() ?: 0L
        if (ahora - cachedAt < TTL_MS) {
            val cache = gasolineraDao.getAll()
            if (cache.isNotEmpty()) return cache.map { it.toDomain() }
        }
        val frescas = api.getEstaciones().listaEESSPrecio.map { it.toDomain() }
        gasolineraDao.deleteAll()
        gasolineraDao.upsertAll(frescas.map { it.toEntity(ahora) })
        return frescas
    }

    suspend fun getGasolinerasCercanas(
        combustible: Combustible = Combustible.GASOLINA_95,
        maxKm: Double = 10.0
    ): List<GasolineraConDistancia> {
        val ubicacion = locationProvider.getLocation()
        val todas = getGasolineras()

        return if (ubicacion != null) {
            val conDistancia = todas
                .filter { it.latitud != 0.0 && it.longitud != 0.0 }
                .map { g ->
                    val distancia = calcularDistancia(
                        ubicacion.latitude, ubicacion.longitude,
                        g.latitud, g.longitud
                    )
                    GasolineraConDistancia(g, distancia)
                }
                .filter { (it.distanciaKm ?: Double.MAX_VALUE) <= maxKm }
                .filter { combustible.precio(it.gasolinera) != null }

            // Gasolinera de referencia = la MÁS CERCANA con precio disponible
            val masCercana = conDistancia.minByOrNull { it.distanciaKm ?: Double.MAX_VALUE }

            // Lista ordenada por precio, con la más cercana marcada
            conDistancia
                .sortedBy { combustible.precio(it.gasolinera) ?: Double.MAX_VALUE }
                .map { it.copy(esMasCercana = it.gasolinera.id == masCercana?.gasolinera?.id) }

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
    val distanciaKm: Double?,
    val esMasCercana: Boolean = false   // ← nuevo flag
)

enum class Combustible(
    val label: String,
    @get:StringRes val labelRes: Int,
    val precio: (Gasolinera) -> Double?
) {
    GASOLINA_95("Gasolina 95", R.string.combustible_95, { it.gasolina95 }),
    GASOLINA_98("Gasolina 98", R.string.combustible_98, { it.gasolina98 }),
    GASOLEO_A("Diésel", R.string.combustible_diesel, { it.gasoleoA }),
    GASOLEO_PREMIUM("Diésel Premium", R.string.combustible_diesel_premium, { it.gasoleoPremium }),
    GLP("GLP", R.string.combustible_glp, { it.glp }),
    GNC("GNC", R.string.combustible_gnc, { it.gnc }),
    GNL("GNL", R.string.combustible_gnl, { it.gnl })
}
