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
import org.maplibre.android.location.LocationComponentActivationOptions
import org.maplibre.android.location.modes.CameraMode
import org.maplibre.android.location.modes.RenderMode
import org.maplibre.android.camera.CameraPosition
import org.maplibre.android.camera.CameraUpdateFactory
import org.maplibre.android.geometry.LatLng
import org.maplibre.android.maps.MapView
import org.maplibre.android.style.expressions.Expression.*
import org.maplibre.android.style.layers.CircleLayer
import org.maplibre.android.style.layers.PropertyFactory.*
import org.maplibre.android.style.layers.SymbolLayer
import org.maplibre.android.style.sources.GeoJsonOptions
import org.maplibre.android.style.sources.GeoJsonSource

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
                    map.setStyle("https://tiles.openfreemap.org/styles/liberty") { style ->

                        map.cameraPosition = CameraPosition.Builder()
                            .target(LatLng(userLat, userLon))
                            .zoom(12.0)
                            .build()

                        // ── 1. Bitmaps para puntos individuales ──────────────────
                        gasolineras.forEach { item ->
                            val g = item.gasolinera
                            val precio = combustible.precio(g)
                            val precioStr = precio?.let { "${"%.3f".format(it)}€" } ?: "—"
                            style.addImage(g.id, crearMarcadorConPrecio(precioStr, item.esMasCercana))
                        }

                        // ── 2. GeoJSON Source con clustering activado ─────────────
                        val featureCollection = MapClusterHelper.buildFeatureCollection(gasolineras, combustible)
                        val source = GeoJsonSource(
                            "gasolineras-source",
                            featureCollection,
                            GeoJsonOptions()
                                .withCluster(true)
                                .withClusterMaxZoom(13)
                                .withClusterRadius(60)
                        )
                        style.addSource(source)

                        // ── 3. Capa círculos de cluster ───────────────────────────
                        val clusterCircles = CircleLayer("layer-clusters", "gasolineras-source").apply {
                            setFilter(has("point_count"))
                            setProperties(
                                circleColor(
                                    step(
                                        get("point_count"),
                                        color(Color.parseColor("#388E3C")),   // verde  < 10
                                        stop(10,  color(Color.parseColor("#F57C00"))), // naranja 10-49
                                        stop(50,  color(Color.parseColor("#C62828")))  // rojo   50+
                                    )
                                ),
                                circleRadius(
                                    step(
                                        get("point_count"),
                                        literal(22f),
                                        stop(10, literal(30f)),
                                        stop(50, literal(38f))
                                    )
                                ),
                                circleOpacity(0.88f),
                                circleStrokeWidth(2.5f),
                                circleStrokeColor(Color.WHITE)
                            )
                        }
                        style.addLayer(clusterCircles)

                        // ── 4. Capa texto contador cluster ────────────────────────
                        val clusterCount = SymbolLayer("layer-cluster-count", "gasolineras-source").apply {
                            setFilter(has("point_count"))
                            setProperties(
                                textField(toString(get("point_count"))),
                                textSize(13f),
                                textColor(Color.WHITE),
                                textIgnorePlacement(true),
                                textAllowOverlap(true)
                            )
                        }
                        style.addLayer(clusterCount)

                        // ── 5. Capa puntos individuales (sin cluster) ─────────────
                        val individualPoints = SymbolLayer("gasolineras-layer", "gasolineras-source").apply {
                            setFilter(not(has("point_count")))
                            setProperties(
                                iconImage(
                                    // Mapea el índice del feature a su icono bitmap
                                    // Usamos el id para reconstruir el índice
                                    toString(get("id"))
                                ),
                                iconAllowOverlap(true),
                                iconSize(1.0f)
                            )
                        }
                        style.addLayer(individualPoints)

                        // ── 6. Punto azul de ubicación del usuario ───────────────
                        try {
                            val locationComponent = map.locationComponent
                            val activationOptions = LocationComponentActivationOptions
                                .builder(ctx, style)
                                .useDefaultLocationEngine(true)
                                .build()
                            locationComponent.activateLocationComponent(activationOptions)
                            locationComponent.isLocationComponentEnabled = true
                            locationComponent.cameraMode = CameraMode.NONE
                            locationComponent.renderMode = RenderMode.COMPASS
                        } catch (e: Exception) {
                            // Permiso no concedido aún, se ignora
                        }

                        // ── 7. Click: cluster → zoom in │ punto → info ────────────
                        map.addOnMapClickListener { latLng ->
                            val pixel = map.projection.toScreenLocation(latLng)

                            val clusterFeatures = map.queryRenderedFeatures(pixel, "layer-clusters")
                            if (clusterFeatures.isNotEmpty()) {
                                val currentZoom = map.cameraPosition.zoom
                                map.animateCamera(
                                    CameraUpdateFactory.newLatLngZoom(latLng, currentZoom + 2.0),
                                    400
                                )
                                true
                            } else {
                                false
                            }
                        }
                    }
                }
                onStart()
            }
        }
    )
}

private fun crearMarcadorConPrecio(precio: String, esBarata: Boolean): Bitmap {
    val fondoColor = if (esBarata) Color.parseColor("#1B5E20") else Color.parseColor("#2E7D32")
    val bordeColor = if (esBarata) Color.parseColor("#FFD700") else Color.parseColor("#1B5E20")

    val paintFondo  = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = fondoColor; style = Paint.Style.FILL }
    val paintBorde  = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = bordeColor; style = Paint.Style.STROKE; strokeWidth = 4f }
    val paintPunta  = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = fondoColor; style = Paint.Style.FILL }
    val paintPrecio = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = Color.WHITE; textSize = 22f; typeface = Typeface.DEFAULT_BOLD; textAlign = Paint.Align.CENTER }
    val paintIcono  = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = Color.WHITE; textSize = 18f; textAlign = Paint.Align.CENTER }

    val w = 120; val h = 80; val radio = 12f
    val bitmap = Bitmap.createBitmap(w, h + 20, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(bitmap)

    val rect = RectF(2f, 2f, w - 2f, h - 2f)
    canvas.drawRoundRect(rect, radio, radio, paintFondo)
    canvas.drawRoundRect(rect, radio, radio, paintBorde)

    val path = Path().apply {
        moveTo(w / 2f - 12f, h - 2f)
        lineTo(w / 2f, h + 18f)
        lineTo(w / 2f + 12f, h - 2f)
        close()
    }
    canvas.drawPath(path, paintPunta)
    canvas.drawText("⛽", w / 2f, 26f, paintIcono)
    canvas.drawText(precio, w / 2f, 58f, paintPrecio)

    return bitmap
}
