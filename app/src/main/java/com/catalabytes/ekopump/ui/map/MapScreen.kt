package com.catalabytes.ekopump.ui.map

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RectF
import android.graphics.Typeface
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.catalabytes.ekopump.data.repository.Combustible
import com.catalabytes.ekopump.data.repository.GasolineraConDistancia
import com.catalabytes.ekopump.ui.theme.EkoGreen40
import kotlinx.coroutines.launch
import org.maplibre.android.MapLibre
import org.maplibre.android.camera.CameraPosition
import org.maplibre.android.camera.CameraUpdateFactory
import org.maplibre.android.geometry.LatLng
import org.maplibre.android.location.LocationComponentActivationOptions
import org.maplibre.android.location.modes.CameraMode
import org.maplibre.android.location.modes.RenderMode
import org.maplibre.android.maps.MapLibreMap
import org.maplibre.android.maps.MapView
import org.maplibre.android.style.expressions.Expression.*
import org.maplibre.android.style.layers.PropertyFactory.*
import org.maplibre.android.style.layers.SymbolLayer
import org.maplibre.android.style.sources.GeoJsonOptions
import org.maplibre.android.style.sources.GeoJsonSource

@Composable
fun MapScreen(
    gasolineras: List<GasolineraConDistancia>,
    combustible: Combustible,
    userLat: Double,
    userLon: Double,
    locationDisponible: Boolean = false,
    onGasolineraClick: (GasolineraConDistancia) -> Unit = {}
) {
    val context = LocalContext.current
    MapLibre.getInstance(context)

    var mapInstance by remember { mutableStateOf<MapLibreMap?>(null) }
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()

    Box(modifier = Modifier.fillMaxSize()) {
        AndroidView(
            modifier = Modifier.fillMaxSize(),
            factory = { ctx ->
                MapView(ctx).apply {
                    getMapAsync { map ->
                        mapInstance = map
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

                            // ── 2. Bitmaps para clusters ──────────────────────────────
                            style.addImage("cluster-s", crearBitmapCluster())
                            style.addImage("cluster-m", crearBitmapCluster())
                            style.addImage("cluster-l", crearBitmapCluster())

                            // ── 3. GeoJSON Source con clustering activado ─────────────
                            val featureCollection = MapClusterHelper.buildFeatureCollection(gasolineras, combustible)
                            val source = GeoJsonSource(
                                "gasolineras-source",
                                org.maplibre.geojson.FeatureCollection.fromFeatures(emptyList()),
                                GeoJsonOptions()
                                    .withCluster(true)
                                    .withClusterMaxZoom(14)
                                    .withClusterRadius(50)
                            )
                            style.addSource(source)
                            source.setGeoJson(featureCollection)

                            // ── 4. Capa clusters ──────────────────────────────────────
                            val clusterLayer = SymbolLayer("layer-clusters", "gasolineras-source").apply {
                                minZoom = 0f
                                maxZoom = 13f
                                setProperties(
                                    iconImage("cluster-s"),
                                    iconAllowOverlap(true),
                                    iconSize(1.0f)
                                )
                            }
                            style.addLayer(clusterLayer)

                            // ── 5. Capa puntos individuales ───────────────────────────
                            val individualPoints = SymbolLayer("gasolineras-layer", "gasolineras-source").apply {
                                minZoom = 13f
                                maxZoom = 22f
                                setProperties(
                                    iconImage(toString(get("id"))),
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
                                // Permiso no concedido aún
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
                                    return@addOnMapClickListener true
                                }

                                val markerFeatures = map.queryRenderedFeatures(pixel, "gasolineras-layer")
                                if (markerFeatures.isNotEmpty()) {
                                    val feature = markerFeatures.first()
                                    val id = feature.getStringProperty("id")
                                    val gasolinera = gasolineras.find { it.gasolinera.id == id }
                                    if (gasolinera != null) {
                                        onGasolineraClick(gasolinera)
                                        return@addOnMapClickListener true
                                    }
                                }
                                false
                            }
                        }
                    }
                    onStart()
                    onResume()
                }
            }
        )

        // ── FAB: centrar en ubicación GPS ─────────────────────────────────
        FloatingActionButton(
            onClick = {
                val m = mapInstance
                if (m == null || !locationDisponible) {
                    coroutineScope.launch {
                        snackbarHostState.showSnackbar("Ubicación no disponible")
                    }
                } else {
                    m.animateCamera(
                        CameraUpdateFactory.newLatLngZoom(LatLng(userLat, userLon), 14.0),
                        800
                    )
                }
            },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp)
                .navigationBarsPadding(),
            containerColor = EkoGreen40
        ) {
            Icon(
                Icons.Default.MyLocation,
                contentDescription = "Mi ubicación",
                tint = androidx.compose.ui.graphics.Color.White
            )
        }

        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier.align(Alignment.BottomCenter)
        )
    }
}

private fun crearBitmapCluster(): Bitmap {
    val size = 120
    val radio = size / 2f
    val bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(bitmap)

    val paintFondo = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#388E3C")
        style = Paint.Style.FILL
    }
    val paintBorde = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.WHITE
        style = Paint.Style.STROKE
        strokeWidth = 4f
    }
    val paintIcono = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.WHITE
        textSize = 36f
        textAlign = Paint.Align.CENTER
    }

    canvas.drawCircle(radio, radio, radio - 4f, paintFondo)
    canvas.drawCircle(radio, radio, radio - 4f, paintBorde)

    val textY = radio - (paintIcono.descent() + paintIcono.ascent()) / 2f
    canvas.drawText("⛽", radio, textY, paintIcono)

    return bitmap
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
