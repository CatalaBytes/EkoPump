package com.catalabytes.ekopump.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Navigation
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.catalabytes.ekopump.R
import com.catalabytes.ekopump.data.repository.GasolineraConDistancia
import com.catalabytes.ekopump.domain.AhorroDoble
import com.catalabytes.ekopump.ui.theme.EkoGreen40

@Composable
fun SmartDecisionCard(
    mejorGasolinera: GasolineraConDistancia?,
    precioActual: Double?,
    ahorroDoble: AhorroDoble?,
    habitualDisponible: Boolean,
    brentBajando: Boolean,
    combustibleLabel: String,
    onNavigate: () -> Unit,
    modifier: Modifier = Modifier
) {
    if (mejorGasolinera == null || precioActual == null) return

    val mejorAhorro = ahorroDoble?.dePaso?.beneficioNeto ?: 0.0
    val mensajeVoz = when {
        brentBajando && mejorAhorro > 3.0 -> stringResource(R.string.smart_voz_buen_momento)
        brentBajando                       -> stringResource(R.string.smart_voz_bajando)
        mejorAhorro > 5.0                  -> stringResource(R.string.smart_voz_gran_ahorro)
        else                               -> stringResource(R.string.smart_voz_mejor_opcion)
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 8.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1B3A1F)),
        elevation = CardDefaults.cardElevation(6.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {

            // Cabecera con badge
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text("⚡", fontSize = 14.sp)
                    Text(
                        stringResource(R.string.smart_mejor_ahora),
                        fontSize = 11.sp,
                        color = EkoGreen40,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.8.sp
                    )
                }
                ahorroDoble?.dePaso?.let { r ->
                    if (r.beneficioNeto > 0.5) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(20.dp))
                                .background(EkoGreen40.copy(alpha = 0.2f))
                                .padding(horizontal = 8.dp, vertical = 2.dp)
                        ) {
                            Text(
                                stringResource(R.string.smart_ahorras_fmt, "%.2f".format(r.beneficioNeto)),
                                fontSize = 11.sp,
                                color = EkoGreen40,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }
            }

            Spacer(Modifier.height(10.dp))

            // Gasolinera + precio
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        mejorGasolinera.gasolinera.nombre,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color.White,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    mejorGasolinera.distanciaKm?.let { dist ->
                        Text(
                            "\uD83D\uDCCD ${"%.1f".format(dist)} km \u00b7 ${mejorGasolinera.gasolinera.localidad}",
                            fontSize = 12.sp,
                            color = Color(0xFF6B8F72)
                        )
                    }
                }
                Text(
                    "${"%.3f".format(precioActual)}\u20ac",
                    fontSize = 26.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = EkoGreen40
                )
            }

            Spacer(Modifier.height(10.dp))

            // Mensaje de voz
            Text(
                mensajeVoz,
                fontSize = 12.sp,
                color = Color(0xFF8FAF93),
                fontStyle = FontStyle.Italic
            )

            // Líneas de ahorro dual (de paso / de casa)
            ahorroDoble?.let { doble ->
                Spacer(Modifier.height(8.dp))
                HorizontalDivider(color = Color.White.copy(alpha = 0.08f))
                Spacer(Modifier.height(8.dp))

                // De paso
                val dePasoLabel = if (doble.dePaso.valeLaPena)
                    "✅ ${"%.2f".format(doble.dePaso.beneficioNeto)}€"
                else
                    "❌ ${"%.2f".format(-doble.dePaso.beneficioNeto)}€"
                Text(
                    stringResource(R.string.ahorro_de_paso, dePasoLabel),
                    fontSize = 12.sp,
                    color = if (doble.dePaso.valeLaPena) EkoGreen40 else Color(0xFFEF9A9A)
                )

                // De casa (solo si el usuario tiene punto habitual guardado)
                if (habitualDisponible) {
                    Spacer(Modifier.height(4.dp))
                    val deCasaLabel = if (doble.deCasa.valeLaPena)
                        "✅ ${"%.2f".format(doble.deCasa.beneficioNeto)}€"
                    else
                        "❌ ${"%.2f".format(-doble.deCasa.beneficioNeto)}€"
                    Text(
                        stringResource(R.string.ahorro_de_casa, deCasaLabel),
                        fontSize = 12.sp,
                        color = if (doble.deCasa.valeLaPena) EkoGreen40 else Color(0xFFEF9A9A)
                    )
                }
            }

            Spacer(Modifier.height(12.dp))

            // Botón navegar
            Button(
                onClick = onNavigate,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = EkoGreen40),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.Default.Navigation, null, Modifier.size(16.dp))
                Spacer(Modifier.width(6.dp))
                Text(
                    stringResource(R.string.detail_navegar),
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}
