package com.catalabytes.ekopump.ui.map

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.NotificationsOff
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.catalabytes.ekopump.data.repository.Combustible
import com.catalabytes.ekopump.data.repository.GasolineraConDistancia
import com.catalabytes.ekopump.domain.model.TendenciaPrecio
import com.catalabytes.ekopump.ui.favorites.FavoritasPrefs

private val EkoGreen  = Color(0xFF2E7D32)
private val EkoGreenL = Color(0xFF00C853)
private val EkoAmber  = Color(0xFFF57F17)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GasolineraDetailSheet(
    item: GasolineraConDistancia,
    combustible: Combustible,
    onRepostar: () -> Unit,
    onDismiss: () -> Unit,
    hasAlert: Boolean = false,
    onSetAlert: (Double) -> Unit = {},
    onRemoveAlert: () -> Unit = {},
    tendencia: TendenciaPrecio = TendenciaPrecio.ESTABLE,
    esMasBarata: Boolean = false
) {
    val context = LocalContext.current
    val g = item.gasolinera
    val precio = combustible.precio(g)
    val precioStr = precio?.let { "${"%.3f".format(it)} €/L" } ?: "—"

    var esFavorita by remember {
        mutableStateOf(FavoritasPrefs.esFavorita(context, g.id))
    }

    var mostrarDialogAlerta by remember { mutableStateOf(false) }
    val precioSugerido = precio?.let { it - 0.02 } ?: 1.5
    var umbralInput by remember { mutableStateOf("${"%.3f".format(precioSugerido)}") }

    if (mostrarDialogAlerta) {
        AlertDialog(
            onDismissRequest = { mostrarDialogAlerta = false },
            containerColor = Color(0xFF1B2E1C),
            title = {
                Text("🔔 Alerta de precio", color = Color(0xFFF0FDF4), fontWeight = FontWeight.Bold)
            },
            text = {
                Column {
                    Text(
                        "Te avisaremos cuando ${g.nombre} tenga ${combustible.label} por debajo del precio que elijas.",
                        fontSize = 14.sp,
                        color = Color(0xFFB0C4B1)
                    )
                    Spacer(Modifier.height(12.dp))
                    OutlinedTextField(
                        value = umbralInput,
                        onValueChange = { umbralInput = it },
                        label = { Text("Precio umbral (€/L)", color = Color(0xFF6B8F72)) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color(0xFFF0FDF4),
                            unfocusedTextColor = Color(0xFFF0FDF4),
                            focusedBorderColor = EkoGreenL,
                            unfocusedBorderColor = Color(0xFF2E7D32)
                        )
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    val umbral = umbralInput.replace(",", ".").toDoubleOrNull()
                    if (umbral != null) {
                        onSetAlert(umbral)
                        mostrarDialogAlerta = false
                    }
                }) {
                    Text("Activar alerta", color = EkoGreenL, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { mostrarDialogAlerta = false }) {
                    Text("Cancelar", color = Color(0xFF6B8F72))
                }
            }
        )
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = Color(0xFF0D1A0F),
        tonalElevation = 0.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .padding(bottom = 32.dp)
        ) {
            // ── Cabecera: nombre + favorita ──────────────────────────────
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        if (esMasBarata) {
                            Text("🏆 ", fontSize = 18.sp)
                        }
                        Text(
                            text = g.nombre,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFF0FDF4)
                        )
                    }
                    Text(
                        text = g.localidad + " · " + g.provincia,
                        fontSize = 13.sp,
                        color = Color(0xFF6B8F72),
                        modifier = Modifier.padding(top = 2.dp)
                    )
                }
                IconButton(onClick = {
                    esFavorita = FavoritasPrefs.toggleFavorita(context, g.id)
                }) {
                    Icon(
                        imageVector = if (esFavorita) Icons.Default.Favorite
                                      else Icons.Default.FavoriteBorder,
                        contentDescription = "Favorita",
                        tint = if (esFavorita) Color(0xFFE53935) else Color(0xFF6B8F72)
                    )
                }
            }

            Spacer(Modifier.height(16.dp))

            // ── Precio destacado ─────────────────────────────────────────
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFF1B2E1C), RoundedCornerShape(12.dp))
                    .padding(vertical = 16.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    val (tendenciaSimbolo, tendenciaColor) = when (tendencia) {
                        TendenciaPrecio.SUBE   -> "↑ Subiendo" to Color(0xFFE53935)
                        TendenciaPrecio.BAJA   -> "↓ Bajando"  to Color(0xFF43A047)
                        TendenciaPrecio.ESTABLE -> "→ Estable" to Color(0xFF9E9E9E)
                    }
                    Text(
                        text = precioStr,
                        fontSize = 36.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = EkoGreenL
                    )
                    Text(
                        text = tendenciaSimbolo,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = tendenciaColor,
                        modifier = Modifier.padding(top = 2.dp)
                    )
                    Text(
                        text = combustible.label,
                        fontSize = 13.sp,
                        color = Color(0xFF6B8F72)
                    )
                    item.distanciaKm?.let {
                        Text(
                            text = "${"%.1f".format(it)} km",
                            fontSize = 13.sp,
                            color = Color(0xFF6B8F72)
                        )
                    }
                }
            }

            Spacer(Modifier.height(16.dp))

            // ── Todos los precios ────────────────────────────────────────
            val precios = listOf(
                "95"      to g.gasolina95,
                "98"      to g.gasolina98,
                "Diésel"  to g.gasoleoA,
                "D.Prem"  to g.gasoleoPremium,
                "GLP"     to g.glp,
                "GNC"     to g.gnc,
                "GNL"     to g.gnl
            ).filter { it.second != null }

            if (precios.size > 1) {
                androidx.compose.foundation.lazy.LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(precios) { (label, precio) ->
                        Box(
                            modifier = Modifier
                                .background(
                                    Color(0xFF1B2E1C),
                                    androidx.compose.foundation.shape.RoundedCornerShape(8.dp)
                                )
                                .padding(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(label, fontSize = 11.sp, color = Color(0xFF6B8F72))
                                Text(
                                    "${"%.3f".format(precio!!)}€",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFFB0C4B1)
                                )
                            }
                        }
                    }
                }
                Spacer(Modifier.height(16.dp))
            }

            // ── Dirección ────────────────────────────────────────────────
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.LocationOn, contentDescription = null,
                    tint = EkoGreen, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(8.dp))
                Text(
                    text = g.direccion,
                    fontSize = 14.sp,
                    color = Color(0xFFB0C4B1)
                )
            }

            // ── Horario ──────────────────────────────────────────────────
            if (g.horario.isNotBlank()) {
                Spacer(Modifier.height(10.dp))
                Row(verticalAlignment = Alignment.Top) {
                    Icon(Icons.Default.Schedule, contentDescription = null,
                        tint = EkoAmber, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = g.horario,
                        fontSize = 14.sp,
                        color = Color(0xFFB0C4B1),
                        lineHeight = 20.sp
                    )
                }
            }

            Spacer(Modifier.height(16.dp))

            // ── Alerta de precio ─────────────────────────────────────────
            if (hasAlert) {
                OutlinedButton(
                    onClick = onRemoveAlert,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFFFB300)),
                    border = ButtonDefaults.outlinedButtonBorder.copy(
                        brush = androidx.compose.ui.graphics.SolidColor(Color(0xFFFFB300))
                    )
                ) {
                    Icon(Icons.Default.NotificationsOff, contentDescription = null,
                        modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("🔕 Quitar alerta de precio")
                }
            } else {
                OutlinedButton(
                    onClick = {
                        umbralInput = "${"%.3f".format(precioSugerido)}"
                        mostrarDialogAlerta = true
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFF80CBC4)),
                    border = ButtonDefaults.outlinedButtonBorder.copy(
                        brush = androidx.compose.ui.graphics.SolidColor(Color(0xFF26A69A))
                    )
                ) {
                    Icon(Icons.Default.Notifications, contentDescription = null,
                        modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("🔔 Alertarme si baja de ${combustible.label.lowercase()}")
                }
            }

            Spacer(Modifier.height(12.dp))

            // ── Botones ──────────────────────────────────────────────────
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Navegar con Google Maps
                OutlinedButton(
                    onClick = {
                        val uri = Uri.parse("google.navigation:q=${g.latitud},${g.longitud}")
                        val intent = Intent(Intent.ACTION_VIEW, uri).apply {
                            setPackage("com.google.android.apps.maps")
                        }
                        try { context.startActivity(intent) }
                        catch (e: Exception) {
                            val fallback = Intent(Intent.ACTION_VIEW,
                                Uri.parse("https://maps.google.com/?daddr=${g.latitud},${g.longitud}"))
                            context.startActivity(fallback)
                        }
                    },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = EkoGreenL),
                    border = ButtonDefaults.outlinedButtonBorder.copy(
                        brush = androidx.compose.ui.graphics.SolidColor(EkoGreen)
                    )
                ) {
                    Text("🗺️ Navegar")
                }

                // Repostar aquí
                Button(
                    onClick = onRepostar,
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(containerColor = EkoGreen)
                ) {
                    Text("⛽ Repostar", color = Color.White, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
