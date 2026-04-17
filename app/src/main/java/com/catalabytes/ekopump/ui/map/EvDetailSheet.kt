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
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.catalabytes.ekopump.R
import com.catalabytes.ekopump.domain.model.EvCharger
import com.catalabytes.ekopump.domain.model.EvConexion
import com.catalabytes.ekopump.ui.favorites.FavoritasPrefs

private val EvAccent   = Color(0xFFFFD600)
private val EkoGreen   = Color(0xFF2E7D32)
private val EkoGreenL  = Color(0xFF00C853)
private val Operativo  = Color(0xFF43A047)
private val NoOperativo = Color(0xFFE53935)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EvDetailSheet(
    charger: EvCharger,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    var esFavorita by remember {
        mutableStateOf(FavoritasPrefs.esFavorita(context, charger.id.toString()))
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
            // ── Cabecera: nombre + badge estado ─────────────────────────
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "⚡ ${charger.nombre}",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFF0FDF4)
                    )
                    charger.localidad?.let {
                        Text(
                            text = it,
                            fontSize = 13.sp,
                            color = Color(0xFF6B8F72),
                            modifier = Modifier.padding(top = 2.dp)
                        )
                    }
                }
                IconButton(onClick = {
                    esFavorita = FavoritasPrefs.toggleFavorita(context, charger.id.toString())
                }) {
                    Icon(
                        imageVector = if (esFavorita) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                        contentDescription = null,
                        tint = if (esFavorita) Color.Red else Color(0xFF6B8F72)
                    )
                }
                val (estadoLabel, estadoColor) = if (charger.esOperacional)
                    stringResource(R.string.ev_detail_operacional) to Operativo
                else
                    stringResource(R.string.ev_detail_no_operativo) to NoOperativo
                Box(
                    modifier = Modifier
                        .background(estadoColor.copy(alpha = 0.15f), RoundedCornerShape(8.dp))
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = "● $estadoLabel",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = estadoColor
                    )
                }
            }

            Spacer(Modifier.height(16.dp))

            // ── Bloque destacado ─────────────────────────────────────────
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFF1B2E1C), RoundedCornerShape(12.dp))
                    .padding(vertical = 16.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text("⚡", fontSize = 36.sp)
                    charger.operador?.let {
                        Text(
                            text = it,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color(0xFFB0C4B1)
                        )
                    }
                    charger.distanciaKm?.let {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                Icons.Default.LocationOn,
                                contentDescription = null,
                                tint = EkoGreen,
                                modifier = Modifier.size(14.dp)
                            )
                            Text(
                                text = "${"%.1f".format(it)} km",
                                fontSize = 13.sp,
                                color = Color(0xFF6B8F72)
                            )
                        }
                    }
                    charger.coste?.let {
                        Text(
                            text = "💳 $it",
                            fontSize = 13.sp,
                            color = Color(0xFFB0C4B1)
                        )
                    }
                    charger.totalPuntos?.let {
                        Text(
                            text = "$it puntos de carga",
                            fontSize = 12.sp,
                            color = Color(0xFF6B8F72)
                        )
                    }
                }
            }

            // ── Conexiones ───────────────────────────────────────────────
            if (charger.conexiones.isNotEmpty()) {
                Spacer(Modifier.height(16.dp))
                Text(
                    text = "Conexiones",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFF6B8F72)
                )
                Spacer(Modifier.height(8.dp))
                Column(verticalArrangement = Arrangement.spacedBy(0.dp)) {
                    charger.conexiones.forEachIndexed { index, conexion ->
                        ConexionRow(conexion)
                        if (index < charger.conexiones.lastIndex) {
                            HorizontalDivider(
                                color = Color(0xFF1B2E1C),
                                thickness = 1.dp
                            )
                        }
                    }
                }
            }

            Spacer(Modifier.height(20.dp))

            // ── Botón Navegar ────────────────────────────────────────────
            OutlinedButton(
                onClick = {
                    val uri = Uri.parse("google.navigation:q=${charger.latitud},${charger.longitud}")
                    val intent = Intent(Intent.ACTION_VIEW, uri).apply {
                        setPackage("com.google.android.apps.maps")
                    }
                    try {
                        context.startActivity(intent)
                    } catch (_: Exception) {
                        val fallback = Intent(
                            Intent.ACTION_VIEW,
                            Uri.parse("https://maps.google.com/?daddr=${charger.latitud},${charger.longitud}")
                        )
                        context.startActivity(fallback)
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = EkoGreenL),
                border = ButtonDefaults.outlinedButtonBorder.copy(
                    brush = SolidColor(EkoGreen)
                )
            ) {
                Text(
                    stringResource(R.string.detail_navegar),
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}

@Composable
private fun ConexionRow(conexion: EvConexion) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 10.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = "🔌 ${conexion.tipoConector}",
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color(0xFFF0FDF4)
            )
            val detalles = buildList {
                conexion.potenciaKw?.let { add("${"%.1f".format(it)} kW") }
                conexion.cantidad?.let { if (it > 1) add("×$it") }
            }.joinToString(" · ")
            if (detalles.isNotEmpty()) {
                Text(
                    text = detalles,
                    fontSize = 12.sp,
                    color = Color(0xFF6B8F72),
                    modifier = Modifier.padding(top = 2.dp)
                )
            }
        }
        if (conexion.esCargaRapida) {
            Box(
                modifier = Modifier
                    .background(EvAccent.copy(alpha = 0.15f), RoundedCornerShape(6.dp))
                    .padding(horizontal = 8.dp, vertical = 3.dp)
            ) {
                Text(
                    text = "⚡ ${stringResource(R.string.ev_detail_carga_rapida)}",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = EvAccent
                )
            }
        }
    }
}
