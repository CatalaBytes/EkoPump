package com.catalabytes.ekopump.ui.settings

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.catalabytes.ekopump.ui.theme.EkoGreen40
import com.catalabytes.ekopump.viewmodel.GasolinerasViewModel

@Composable
fun CalculadorDialog(
    onDismiss: () -> Unit,
    viewModel: GasolinerasViewModel
) {
    val consumo by viewModel.consumo.collectAsState()
    val litros  by viewModel.litros.collectAsState()

    Dialog(onDismissRequest = onDismiss) {
        Card(shape = RoundedCornerShape(16.dp)) {
            Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                Text("⚙️ Mi coche", fontWeight = FontWeight.Bold, fontSize = 18.sp)

                // Consumo
                Column {
                    Text("Consumo: ${"%.1f".format(consumo)} L/100km", fontSize = 14.sp)
                    Slider(
                        value = consumo,
                        onValueChange = { viewModel.setConsumo(it) },
                        valueRange = 4f..18f,
                        steps = 27,
                        colors = SliderDefaults.colors(thumbColor = EkoGreen40, activeTrackColor = EkoGreen40)
                    )
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("4L", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text("18L", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    // Atajos rápidos
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        listOf(5f, 7f, 9f, 12f).forEach { v ->
                            FilterChip(
                                selected = consumo == v,
                                onClick  = { viewModel.setConsumo(v) },
                                label    = { Text("${v.toInt()}L") },
                                colors   = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = EkoGreen40,
                                    selectedLabelColor     = androidx.compose.ui.graphics.Color.White
                                )
                            )
                        }
                    }
                }

                HorizontalDivider()

                // Litros a repostar
                Column {
                    Text("Litros a repostar: ${"%.0f".format(litros)}L", fontSize = 14.sp)
                    Slider(
                        value = litros,
                        onValueChange = { viewModel.setLitros(it) },
                        valueRange = 10f..80f,
                        steps = 13,
                        colors = SliderDefaults.colors(thumbColor = EkoGreen40, activeTrackColor = EkoGreen40)
                    )
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("10L", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text("80L", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        listOf(20f, 30f, 40f, 50f).forEach { v ->
                            FilterChip(
                                selected = litros == v,
                                onClick  = { viewModel.setLitros(v) },
                                label    = { Text("${v.toInt()}L") },
                                colors   = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = EkoGreen40,
                                    selectedLabelColor     = androidx.compose.ui.graphics.Color.White
                                )
                            )
                        }
                    }
                }

                Button(
                    onClick  = onDismiss,
                    modifier = Modifier.align(Alignment.End),
                    colors   = ButtonDefaults.buttonColors(containerColor = EkoGreen40)
                ) { Text("Listo") }
            }
        }
    }
}
