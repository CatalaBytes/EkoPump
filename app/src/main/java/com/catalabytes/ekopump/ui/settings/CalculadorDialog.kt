package com.catalabytes.ekopump.ui.settings

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.catalabytes.ekopump.domain.model.VehicleType
import com.catalabytes.ekopump.ui.theme.EkoGreen40
import com.catalabytes.ekopump.viewmodel.GasolinerasViewModel

private val EkoGreenDark = Color(0xFF1B5E20)
private val EkoGreenNeon = Color(0xFF69F0AE)
private val EkoDarkBg    = Color(0xFF0D1F0D)
private val EkoDarkCard  = Color(0xFF162916)
private val EkoGrayText  = Color(0xFFB0BEC5)

@Composable
fun CalculadorDialog(
    onDismiss: () -> Unit,
    viewModel: GasolinerasViewModel
) {
    val consumo     by viewModel.consumo.collectAsState()
    val litros      by viewModel.litros.collectAsState()
    val vehicleType by viewModel.vehicleType.collectAsState()

    Dialog(onDismissRequest = onDismiss) {
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(24.dp))
                .background(Brush.verticalGradient(listOf(EkoDarkBg, EkoGreenDark)))
                .border(1.dp, EkoGreenNeon.copy(alpha = 0.3f), RoundedCornerShape(24.dp))
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("⚙", fontSize = 22.sp)
                    Spacer(Modifier.width(8.dp))
                    Text("Mi vehículo", fontWeight = FontWeight.ExtraBold,
                        fontSize = 20.sp, color = Color.White)
                }

                Text("¿Qué conduces?", fontSize = 13.sp,
                    color = EkoGrayText, fontWeight = FontWeight.Medium)

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    VehicleType.entries.forEach { tipo ->
                        VehicleCard(
                            tipo     = tipo,
                            selected = vehicleType == tipo,
                            modifier = Modifier.weight(1f),
                            onClick  = {
                                viewModel.setVehicleType(tipo)
                                viewModel.setConsumo(tipo.consumoDefault)
                                viewModel.setLitros(tipo.litrosDefault)
                            }
                        )
                    }
                }

                FuturisticSliderSection(
                    title         = "Consumo",
                    value         = consumo,
                    min           = vehicleType.consumoMin,
                    max           = vehicleType.consumoMax,
                    formatted     = "${"%.1f".format(consumo)} L/100km",
                    quickValues   = vehicleType.quickConsumos,
                    onValueChange = { viewModel.setConsumo(it) },
                    quickLabel    = { "${it.toInt()}L" }
                )

                FuturisticSliderSection(
                    title         = "Litros a repostar",
                    value         = litros,
                    min           = vehicleType.litrosMin,
                    max           = vehicleType.litrosMax,
                    formatted     = "${"%.0f".format(litros)} L",
                    quickValues   = vehicleType.quickLitros,
                    onValueChange = { viewModel.setLitros(it) },
                    quickLabel    = { "${it.toInt()}L" }
                )

                Button(
                    onClick  = onDismiss,
                    modifier = Modifier.fillMaxWidth().height(48.dp),
                    shape    = RoundedCornerShape(12.dp),
                    colors   = ButtonDefaults.buttonColors(
                        containerColor = EkoGreenNeon,
                        contentColor   = EkoDarkBg
                    )
                ) {
                    Text("Listo ✓", fontWeight = FontWeight.ExtraBold, fontSize = 16.sp)
                }
            }
        }
    }
}

@Composable
private fun VehicleCard(
    tipo: VehicleType,
    selected: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    val scale by animateFloatAsState(
        targetValue = if (selected) 1.06f else 1f,
        animationSpec = tween(200), label = "scale"
    )
    val borderColor by animateColorAsState(
        targetValue = if (selected) EkoGreenNeon else Color.White.copy(alpha = 0.1f),
        animationSpec = tween(200), label = "border"
    )
    val bgColor by animateColorAsState(
        targetValue = if (selected) EkoGreenNeon.copy(alpha = 0.15f) else EkoDarkCard,
        animationSpec = tween(200), label = "bg"
    )

    Box(
        modifier = modifier
            .scale(scale)
            .clip(RoundedCornerShape(12.dp))
            .background(bgColor)
            .border(1.5.dp, borderColor, RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .padding(vertical = 10.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(tipo.emoji, fontSize = 22.sp)
            Spacer(Modifier.height(2.dp))
            Text(
                tipo.labelEs,
                fontSize = 10.sp,
                fontWeight = FontWeight.SemiBold,
                color = if (selected) EkoGreenNeon else EkoGrayText,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun FuturisticSliderSection(
    title: String,
    value: Float,
    min: Float,
    max: Float,
    formatted: String,
    quickValues: List<Float>,
    onValueChange: (Float) -> Unit,
    quickLabel: (Float) -> String
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(EkoDarkCard)
            .padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(title, fontSize = 13.sp, color = EkoGrayText, fontWeight = FontWeight.Medium)
            Text(formatted, fontSize = 15.sp, fontWeight = FontWeight.ExtraBold, color = EkoGreenNeon)
        }

        Slider(
            value = value,
            onValueChange = onValueChange,
            valueRange = min..max,
            colors = SliderDefaults.colors(
                thumbColor         = EkoGreenNeon,
                activeTrackColor   = EkoGreenNeon,
                inactiveTrackColor = Color.White.copy(alpha = 0.15f)
            )
        )

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text("${min.toInt()}L", fontSize = 10.sp, color = EkoGrayText)
            Text("${max.toInt()}L", fontSize = 10.sp, color = EkoGrayText)
        }

        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            quickValues.forEach { v ->
                val sel = value == v
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (sel) EkoGreenNeon else Color.White.copy(alpha = 0.07f))
                        .border(1.dp,
                            if (sel) EkoGreenNeon else Color.White.copy(alpha = 0.15f),
                            RoundedCornerShape(8.dp))
                        .clickable { onValueChange(v) }
                        .padding(horizontal = 10.dp, vertical = 5.dp)
                ) {
                    Text(quickLabel(v), fontSize = 11.sp, fontWeight = FontWeight.Bold,
                        color = if (sel) EkoDarkBg else EkoGrayText)
                }
            }
        }
    }
}
