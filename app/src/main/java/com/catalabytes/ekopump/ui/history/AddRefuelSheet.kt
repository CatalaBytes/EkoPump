package com.catalabytes.ekopump.ui.history

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.catalabytes.ekopump.data.local.entity.RefuelEntity

private val EkoGreen = Color(0xFF00C853)

/**
 * BottomSheet para registrar un repostaje.
 * Llamar desde StationDetailScreen o desde el ítem de lista al pulsar "Repostar aquí".
 *
 * Ejemplo de uso:
 *   var showSheet by remember { mutableStateOf(false) }
 *   if (showSheet) {
 *       AddRefuelSheet(
 *           stationName    = station.rotulo,
 *           stationAddress = station.direccion,
 *           fuelType       = selectedFuelType,
 *           pricePerLiter  = station.precioProducto,
 *           avgNationalPrice = viewModel.avgPrice,
 *           latitude       = station.latitud,
 *           longitude      = station.longitud,
 *           onSave         = { historyViewModel.addRefuel(it) },
 *           onDismiss      = { showSheet = false }
 *       )
 *   }
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddRefuelSheet(
    stationName: String,
    stationAddress: String,
    fuelType: String,
    pricePerLiter: Double,
    avgNationalPrice: Double,       // para calcular ahorro
    latitude: Double,
    longitude: Double,
    onSave: (RefuelEntity) -> Unit,
    onDismiss: () -> Unit
) {
    var litersText by remember { mutableStateOf("") }
    val liters = litersText.toDoubleOrNull() ?: 0.0
    val totalCost = liters * pricePerLiter
    val savedAmount = liters * (avgNationalPrice - pricePerLiter).coerceAtLeast(0.0)

    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .padding(bottom = 32.dp)
        ) {
            Text(
                "⛽ Registrar repostaje",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = EkoGreen
            )
            Spacer(Modifier.height(4.dp))
            Text(stationName, style = MaterialTheme.typography.bodyMedium)
            Text(stationAddress, style = MaterialTheme.typography.bodySmall, color = Color.Gray)

            Spacer(Modifier.height(20.dp))

            // Info del combustible
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                InfoChip("⚡ $fuelType")
                InfoChip("${"%.3f".format(pricePerLiter)} €/L")
            }

            Spacer(Modifier.height(16.dp))

            // Campo litros
            OutlinedTextField(
                value = litersText,
                onValueChange = { litersText = it.replace(',', '.') },
                label = { Text("Litros repostados") },
                suffix = { Text("L") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            Spacer(Modifier.height(12.dp))

            // Preview coste y ahorro
            if (liters > 0) {
                Card(
                    colors = CardDefaults.cardColors(containerColor = EkoGreen.copy(alpha = 0.1f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(Modifier.padding(12.dp)) {
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Total:", color = Color.White)
                            Text("${"%.2f".format(totalCost)} €", color = EkoGreen, fontWeight = FontWeight.Bold)
                        }
                        if (savedAmount > 0) {
                            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("Ahorro vs media:", color = Color.Gray)
                                Text("${"%.2f".format(savedAmount)} €", color = EkoGreen)
                            }
                        }
                    }
                }
            }

            Spacer(Modifier.height(20.dp))

            Button(
                onClick = {
                    if (liters > 0) {
                        onSave(
                            RefuelEntity(
                                stationName    = stationName,
                                stationAddress = stationAddress,
                                fuelType       = fuelType,
                                pricePerLiter  = pricePerLiter,
                                liters         = liters,
                                totalCost      = totalCost,
                                savedAmount    = savedAmount,
                                latitude       = latitude,
                                longitude      = longitude
                            )
                        )
                        onDismiss()
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = liters > 0,
                colors = ButtonDefaults.buttonColors(containerColor = EkoGreen)
            ) {
                Text("Guardar repostaje", color = Color.Black, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
private fun InfoChip(text: String) {
    Surface(
        color = Color(0xFF1A2D40),
        shape = MaterialTheme.shapes.small
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
            style = MaterialTheme.typography.labelMedium,
            color = Color.White
        )
    }
}
