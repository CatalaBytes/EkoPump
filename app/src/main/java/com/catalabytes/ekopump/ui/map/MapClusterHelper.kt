package com.catalabytes.ekopump.ui.map

import com.catalabytes.ekopump.data.repository.Combustible
import com.catalabytes.ekopump.data.repository.GasolineraConDistancia
import org.maplibre.geojson.Feature
import org.maplibre.geojson.FeatureCollection
import org.maplibre.geojson.Point

object MapClusterHelper {

    fun buildFeatureCollection(
        gasolineras: List<GasolineraConDistancia>,
        combustible: Combustible
    ): FeatureCollection {
        val features = gasolineras.map { item ->
            val g = item.gasolinera
            val precioStr = combustible.precio(g)?.let { "${"%.3f".format(it)}€" } ?: "—"

            Feature.fromGeometry(Point.fromLngLat(g.longitud, g.latitud)).apply {
                addStringProperty("id", g.id)
                addStringProperty("nombre", g.nombre)
                addStringProperty("precio", precioStr)
                addBooleanProperty("esBarata", item.esMasCercana)
                addBooleanProperty("isIndividual", true)
            }
        }
        return FeatureCollection.fromFeatures(features)
    }
}
