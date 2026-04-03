package com.catalabytes.ekopump.ui.map

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RectF
import android.graphics.Typeface
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import com.catalabytes.ekopump.data.repository.Combustible
import com.catalabytes.ekopump.data.repository.GasolineraConDistancia
import org.maplibre.android.MapLibre
import org.maplibre.android.camera.CameraPosition
import org.maplibre.android.geometry.LatLng
import org.maplibre.android.maps.MapView
import org.maplibre.android.style.layers.PropertyFactory.*
import org.maplibre.android.style.layers.SymbolLayer
import org.maplibre.android.style.sources.GeoJsonSource
import org.maplibre.geojson.Feature
import org.maplibre.geojson.FeatureCollection
import org.maplibre.geojson.Point

@Composable
fun MapScreen(
    gasolineras: List<GasolineraConDistancia>,
    combustible: Combustible,
    userLat: Double,
    userLon: Double
) {
    val context = LocalContext.current
    MapLibre.getInstance(context)

    AndroidView(
        modifier = Modifier.fillMaxSize(),
        factory = { ctx ->
            MapView(ctx).apply {
                getMapAsync { map ->
                    map.setStyle("https://demotiles.maplibre.org/style.json") { style ->
                        map.cameraPosition = CameraPosition.Builder()
                            .target(LatLng(userLat, userLon))
                            .zoom(12.0)
                            .build()

                        // Añadir icono base
                        val iconBitmap = crearMarcador("⛽")
                        style.addImage("ekopump-icon", iconBitmap)

                        // Crear features GeoJSON
                        val features = gasolineras.take(150)
                            .filter { it.gasolinera.latitud != 0.0 }
                            .map { item ->
                                val g = item.gasolinera
                                val precio = combustible.precio(g)
                                Feature.fromGeometry(
                                    Point.fromLngLat(g.longitud, g.latitud)
                                ).also { f ->
                                    f.addStringProperty("nombre", g.nombre)
                                    f.addStringProperty("precio",
                                        precio?.let { "${"%.3f".format(it)}€" } ?: "")
                                }
                            }

                        val source = GeoJsonSource(
                            "gasolineras-source",
                            FeatureCollection.fromFeatures(features)
                        )
                        style.addSource(source)

                        // Capa de iconos
                        val layer = SymbolLayer("gasolineras-layer", "gasolineras-source").apply {
                            setProperties(
                                iconImage("ekopump-icon"),
                                iconAllowOverlap(true),
                                iconSize(1.0f),
                                textField("{precio}"),
                                textSize(11f),
                                textColor("#FFFFFF"),
                                textAnchor("top"),
                                textOffset(arrayOf(0f, -2.8f)),
                                textAllowOverlap(false)
                            )
                        }
                        style.addLayer(layer)
                    }
                }
                onStart()
            }
        }
    )
}

private fun crearMarcador(texto: String): Bitmap {
    val width = 80
    val height = 80
    val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(bitmap)

    val paintCirculo = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#2E7D32")
        style = Paint.Style.FILL
    }
    val paintTexto = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.WHITE
        textSize = 36f
        typeface = Typeface.DEFAULT_BOLD
        textAlign = Paint.Align.CENTER
    }
    val paintPunta = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#2E7D32")
        style = Paint.Style.FILL
    }

    canvas.drawCircle(width / 2f, height / 2f - 10f, 30f, paintCirculo)

    val path = Path().apply {
        moveTo(width / 2f - 8f, height / 2f + 18f)
        lineTo(width / 2f, height.toFloat())
        lineTo(width / 2f + 8f, height / 2f + 18f)
        close()
    }
    canvas.drawPath(path, paintPunta)
    canvas.drawText(texto, width / 2f, height / 2f - 2f, paintTexto)

    return bitmap
}
