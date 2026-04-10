package com.catalabytes.ekopump.ui.history

import android.content.Intent
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.LocalGasStation
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.catalabytes.ekopump.data.local.entity.RefuelEntity
import com.patrykandpatrick.vico.compose.cartesian.CartesianChartHost
import com.patrykandpatrick.vico.compose.cartesian.layer.rememberColumnCartesianLayer
import com.patrykandpatrick.vico.compose.cartesian.rememberCartesianChart
import com.patrykandpatrick.vico.compose.common.component.rememberLineComponent
import com.patrykandpatrick.vico.core.cartesian.data.CartesianChartModelProducer
import com.patrykandpatrick.vico.core.cartesian.data.columnSeries
import com.patrykandpatrick.vico.core.cartesian.layer.ColumnCartesianLayer
import com.patrykandpatrick.vico.core.common.Fill
import java.text.SimpleDateFormat
import java.util.*

private val EkoGreen = Color(0xFF00C853)
private val EkoDark  = Color(0xFF0D1B2A)
private val EkoCard  = Color(0xFF1A2D40)

@Composable
fun HistoryScreen(
    onRegistrar: () -> Unit = {},
    viewModel: HistoryViewModel = hiltViewModel()
) {
    val refuels          by viewModel.refuels.collectAsState()
    val totalSpent       by viewModel.totalSpent.collectAsState()
    val totalSaved       by viewModel.totalSaved.collectAsState()
    val totalLiters      by viewModel.totalLiters.collectAsState()
    val refuelCount      by viewModel.refuelCount.collectAsState()
    val refuelsMes       by viewModel.refuelsMes.collectAsState()
    val totalGastadoMes  by viewModel.totalGastadoMes.collectAsState()
    val totalLitrosMes   by viewModel.totalLitrosMes.collectAsState()
    val avgConsumoMes    by viewModel.avgConsumoRealMes.collectAsState()
    val totalAhorroMes   by viewModel.totalAhorroMes.collectAsState()

    val context = LocalContext.current

    // Vico model producer para gráfico de barras
    val modelProducer = remember { CartesianChartModelProducer() }
    val ultimos10 = remember(refuelsMes) { refuelsMes.take(10).reversed() }
    LaunchedEffect(ultimos10) {
        if (ultimos10.isNotEmpty()) {
            modelProducer.runTransaction {
                columnSeries { series(ultimos10.map { it.totalCost }) }
            }
        }
    }

    val mesActual = remember {
        SimpleDateFormat("MMMM yyyy", Locale("es")).format(Date()).replaceFirstChar { it.uppercase() }
    }

    Scaffold(
        containerColor = EkoDark,
        floatingActionButton = {
            FloatingActionButton(
                onClick = onRegistrar,
                containerColor = EkoGreen
            ) {
                Icon(
                    imageVector = Icons.Default.LocalGasStation,
                    contentDescription = "Registrar repostaje",
                    tint = Color.Black
                )
            }
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(EkoDark)
                .padding(innerPadding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            item {
                Text(
                    text = "⛽ Historial",
                    style = MaterialTheme.typography.headlineSmall,
                    color = EkoGreen,
                    fontWeight = FontWeight.Bold
                )
            }

            item { Spacer(Modifier.height(4.dp)) }

            // Resumen del mes
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = EkoCard),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Text(
                            "📅 Resumen de $mesActual",
                            color = EkoGreen,
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.titleSmall
                        )
                        Spacer(Modifier.height(10.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            MesStatItem(Modifier.weight(1f), "💶 Gastado", "${"%.2f".format(totalGastadoMes ?: 0.0)} €")
                            MesStatItem(Modifier.weight(1f), "🛢 Litros", "${"%.1f".format(totalLitrosMes ?: 0.0)} L")
                        }
                        Spacer(Modifier.height(6.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            MesStatItem(
                                Modifier.weight(1f),
                                "⚡ Consumo real",
                                if (avgConsumoMes != null) "${"%.1f".format(avgConsumoMes)} L/100" else "—"
                            )
                            MesStatItem(
                                Modifier.weight(1f),
                                "💚 Ahorro",
                                if (totalAhorroMes != null) "${"%.2f".format(totalAhorroMes)} €" else "—",
                                highlight = true
                            )
                        }

                        // Gráfico de barras si hay datos
                        if (ultimos10.isNotEmpty()) {
                            Spacer(Modifier.height(12.dp))
                            Text(
                                "Gasto por repostaje (últimos ${ultimos10.size})",
                                color = Color.Gray,
                                style = MaterialTheme.typography.labelSmall
                            )
                            Spacer(Modifier.height(6.dp))
                            val columnLayer = rememberColumnCartesianLayer(
                                columnProvider = ColumnCartesianLayer.ColumnProvider.series(
                                    rememberLineComponent(
                                        fill = Fill(0xFF2E7D32.toInt()),
                                        thickness = 16.dp
                                    )
                                )
                            )
                            CartesianChartHost(
                                chart = rememberCartesianChart(columnLayer),
                                modelProducer = modelProducer,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(120.dp)
                            )
                        }

                        Spacer(Modifier.height(10.dp))
                        OutlinedButton(
                            onClick = {
                                val texto = buildString {
                                    append("⛽ Este mes he gastado ${"%.2f".format(totalGastadoMes ?: 0.0)} € en combustible")
                                    append(" (${"%.1f".format(totalLitrosMes ?: 0.0)} L repostados).")
                                    if (totalAhorroMes != null && (totalAhorroMes ?: 0f) > 0f) {
                                        append(" He ahorrado ${"%.2f".format(totalAhorroMes)} € usando EkoPump 🟢")
                                    }
                                    if (avgConsumoMes != null) {
                                        append(" Consumo medio real: ${"%.1f".format(avgConsumoMes)} L/100km.")
                                    }
                                    append(" #EkoPump ekopump.es")
                                }
                                val intent = Intent(Intent.ACTION_SEND).apply {
                                    type = "text/plain"
                                    putExtra(Intent.EXTRA_TEXT, texto)
                                }
                                context.startActivity(Intent.createChooser(intent, "Compartir resumen"))
                            },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = EkoGreen),
                            border = androidx.compose.foundation.BorderStroke(1.dp, EkoGreen)
                        ) {
                            Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(6.dp))
                            Text("Compartir resumen del mes")
                        }
                    }
                }
            }

            item { Spacer(Modifier.height(4.dp)) }

            // Stats globales
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    StatCard(Modifier.weight(1f), "Repostajes", "$refuelCount")
                    StatCard(Modifier.weight(1f), "Litros", "${"%.1f".format(totalLiters ?: 0.0)} L")
                    StatCard(Modifier.weight(1f), "Ahorrado", "${"%.2f".format(totalSaved ?: 0.0)} €", highlight = true)
                }
            }

            item {
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
            }

            item { Spacer(Modifier.height(4.dp)) }

            if (refuels.isEmpty()) {
                item { EmptyHistory() }
            } else {
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
private fun MesStatItem(
    modifier: Modifier = Modifier,
    label: String,
    value: String,
    highlight: Boolean = false
) {
    Column(modifier = modifier) {
        Text(label, color = Color.Gray, style = MaterialTheme.typography.labelSmall)
        Text(
            value,
            color = if (highlight) EkoGreen else Color.White,
            fontWeight = FontWeight.SemiBold,
            style = MaterialTheme.typography.bodyMedium
        )
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
                if (refuel.consumoRealL100 != null) {
                    Text(
                        "⚡ Consumo real: ${"%.1f".format(refuel.consumoRealL100)} L/100km",
                        color = Color.Gray,
                        style = MaterialTheme.typography.labelSmall
                    )
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
                "Pulsa el botón + para añadir el primero",
                color = Color.Gray,
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}
