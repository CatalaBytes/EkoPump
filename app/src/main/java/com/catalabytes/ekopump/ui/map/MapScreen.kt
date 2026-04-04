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

                        // Crear un icono por gasolinera con el precio incluido en el bitmap
                        val features = gasolineras.take(150)
                            .filter { it.gasolinera.latitud != 0.0 }
                            .mapIndexed { idx, item ->
                                val g = item.gasolinera
                                val precio = combustible.precio(g)
                                val precioStr = precio?.let { "${"%.3f".format(it)}€" } ?: "—"
                                val esBarata  = idx == 0

                                // Icono único con precio embebido
                                val iconKey = "pin_$idx"
                                val bitmap  = crearMarcadorConPrecio(precioStr, esBarata)
                                style.addImage(iconKey, bitmap)

                                Feature.fromGeometry(
                                    Point.fromLngLat(g.longitud, g.latitud)
                                ).also { f ->
                                    f.addStringProperty("icon", iconKey)
                                    f.addStringProperty("nombre", g.nombre)
                                }
                            }

                        val source = GeoJsonSource(
                            "gasolineras-source",
                            FeatureCollection.fromFeatures(features)
                        )
                        style.addSource(source)

                        val layer = SymbolLayer("gasolineras-layer", "gasolineras-source").apply {
                            setProperties(
                                iconImage("{icon}"),
                                iconAllowOverlap(true),
                                iconSize(1.0f)
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

private fun crearMarcadorConPrecio(precio: String, esBarata: Boolean): Bitmap {
    val fondoColor  = if (esBarata) Color.parseColor("#1B5E20") else Color.parseColor("#2E7D32")
    val bordeColor  = if (esBarata) Color.parseColor("#FFD700") else Color.parseColor("#1B5E20")

    val paintFondo = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = fondoColor; style = Paint.Style.FILL
    }
    val paintBorde = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = bordeColor; style = Paint.Style.STROKE; strokeWidth = 4f
    }
    val paintPunta = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = fondoColor; style = Paint.Style.FILL
    }
    val paintPrecio = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.WHITE
        textSize = 22f
        typeface = Typeface.DEFAULT_BOLD
        textAlign = Paint.Align.CENTER
    }
    val paintIcono = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.WHITE
        textSize = 18f
        textAlign = Paint.Align.CENTER
    }

    val w = 120; val h = 80; val radio = 12f
    val bitmap = Bitmap.createBitmap(w, h + 20, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(bitmap)

    // Cuerpo redondeado
    val rect = RectF(2f, 2f, w - 2f, h - 2f)
    canvas.drawRoundRect(rect, radio, radio, paintFondo)
    canvas.drawRoundRect(rect, radio, radio, paintBorde)

    // Punta inferior
    val path = Path().apply {
        moveTo(w / 2f - 12f, h - 2f)
        lineTo(w / 2f, h + 18f)
        lineTo(w / 2f + 12f, h - 2f)
        close()
    }
    canvas.drawPath(path, paintPunta)

    // Icono ⛽ pequeño arriba
    canvas.drawText("⛽", w / 2f, 26f, paintIcono)

    // Precio centrado
    canvas.drawText(precio, w / 2f, 58f, paintPrecio)

    return bitmap
}
