package com.catalabytes.ekopump.ui.history

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.LocalGasStation
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.catalabytes.ekopump.data.local.entity.RefuelEntity
import java.text.SimpleDateFormat
import java.util.*

private val EkoGreen = Color(0xFF00C853)
private val EkoDark  = Color(0xFF0D1B2A)
private val EkoCard  = Color(0xFF1A2D40)

@Composable
fun HistoryScreen(viewModel: HistoryViewModel = hiltViewModel()) {
    val refuels     by viewModel.refuels.collectAsState()
    val totalSpent  by viewModel.totalSpent.collectAsState()
    val totalSaved  by viewModel.totalSaved.collectAsState()
    val totalLiters by viewModel.totalLiters.collectAsState()
    val refuelCount by viewModel.refuelCount.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(EkoDark)
            .padding(16.dp)
    ) {
        Text(
            text = "⛽ Historial",
            style = MaterialTheme.typography.headlineSmall,
            color = EkoGreen,
            fontWeight = FontWeight.Bold
        )

        Spacer(Modifier.height(12.dp))

        // Stat cards fila superior
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            StatCard(Modifier.weight(1f), "Repostajes", "$refuelCount")
            StatCard(Modifier.weight(1f), "Litros", "${"%.1f".format(totalLiters ?: 0.0)} L")
            StatCard(Modifier.weight(1f), "Ahorrado", "${"%.2f".format(totalSaved ?: 0.0)} €", highlight = true)
        }

        Spacer(Modifier.height(8.dp))

        // Total gastado
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = EkoCard),
            shape = RoundedCornerShape(12.dp)
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("💶 Total gastado:", color = Color.White, modifier = Modifier.weight(1f))
                Text(
                    "${"%.2f".format(totalSpent ?: 0.0)} €",
                    color = EkoGreen,
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.titleMedium
                )
            }
        }

        Spacer(Modifier.height(16.dp))

        if (refuels.isEmpty()) {
            EmptyHistory()
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(refuels, key = { it.id }) { refuel ->
                    AnimatedVisibility(
                        visible = true,
                        enter = fadeIn() + slideInVertically()
                    ) {
                        RefuelCard(refuel = refuel, onDelete = { viewModel.deleteRefuel(refuel) })
                    }
                }
            }
        }
    }
}

@Composable
private fun StatCard(
    modifier: Modifier = Modifier,
    label: String,
    value: String,
    highlight: Boolean = false
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = if (highlight) EkoGreen.copy(alpha = 0.15f) else EkoCard
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(value, color = if (highlight) EkoGreen else Color.White, fontWeight = FontWeight.Bold)
            Text(label, color = Color.Gray, style = MaterialTheme.typography.labelSmall)
        }
    }
}

@Composable
private fun RefuelCard(refuel: RefuelEntity, onDelete: () -> Unit) {
    val date = remember(refuel.timestamp) {
        SimpleDateFormat("dd MMM yyyy · HH:mm", Locale("es")).format(Date(refuel.timestamp))
    }
    var showConfirm by remember { mutableStateOf(false) }

    Card(
        colors = CardDefaults.cardColors(containerColor = EkoCard),
        shape = RoundedCornerShape(14.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(Icons.Default.LocalGasStation, contentDescription = null, tint = EkoGreen)
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(refuel.stationName, color = Color.White, fontWeight = FontWeight.SemiBold)
                Text(refuel.fuelType,   color = Color.Gray,  style = MaterialTheme.typography.bodySmall)
                Text(date,              color = Color.Gray,  style = MaterialTheme.typography.labelSmall)
                Spacer(Modifier.height(6.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("${"%.2f".format(refuel.liters)} L",             color = Color.White)
                    Text("${"%.3f".format(refuel.pricePerLiter)} €/L",    color = Color.LightGray)
                    Text("${"%.2f".format(refuel.totalCost)} €",          color = EkoGreen, fontWeight = FontWeight.Bold)
                }
                if (refuel.savedAmount > 0) {
                    Text(
                        "💚 Ahorraste ${"%.2f".format(refuel.savedAmount)} €",
                        color = EkoGreen,
                        style = MaterialTheme.typography.labelSmall
                    )
                }
            }
            IconButton(onClick = { showConfirm = true }) {
                Icon(Icons.Default.Delete, contentDescription = "Borrar", tint = Color.Gray)
            }
        }
    }

    if (showConfirm) {
        AlertDialog(
            onDismissRequest = { showConfirm = false },
            title   = { Text("¿Borrar repostaje?") },
            text    = { Text("Esta acción no se puede deshacer.") },
            confirmButton = {
                TextButton(onClick = { onDelete(); showConfirm = false }) {
                    Text("Borrar", color = Color.Red)
                }
            },
            dismissButton = {
                TextButton(onClick = { showConfirm = false }) { Text("Cancelar") }
            }
        )
    }
}

@Composable
private fun EmptyHistory() {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text("⛽", style = MaterialTheme.typography.displayLarge)
            Spacer(Modifier.height(8.dp))
            Text("Aún no has registrado repostajes", color = Color.Gray)
            Text(
                "Pulsa el botón + en una gasolinera",
                color = Color.Gray,
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}
