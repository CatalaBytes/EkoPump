package com.catalabytes.ekopump.ui.map

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.catalabytes.ekopump.data.repository.Combustible
import com.catalabytes.ekopump.data.repository.GasolineraConDistancia
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
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val g = item.gasolinera
    val precio = combustible.precio(g)
    val precioStr = precio?.let { "${"%.3f".format(it)} €/L" } ?: "—"

    var esFavorita by remember {
        mutableStateOf(FavoritasPrefs.esFavorita(context, g.id))
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
                    Text(
                        text = g.nombre,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFF0FDF4)
                    )
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
                    Text(
                        text = precioStr,
                        fontSize = 36.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = EkoGreenL
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

            Spacer(Modifier.height(24.dp))

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
