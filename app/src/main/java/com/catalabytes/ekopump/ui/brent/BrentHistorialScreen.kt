package com.catalabytes.ekopump.ui.brent

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.catalabytes.ekopump.R
import com.catalabytes.ekopump.ui.theme.EkoAmber40
import com.catalabytes.ekopump.ui.theme.EkoGreen40
import com.catalabytes.ekopump.viewmodel.BrentViewModel
import com.patrykandpatrick.vico.compose.cartesian.CartesianChartHost
import com.patrykandpatrick.vico.compose.cartesian.axis.rememberBottom
import com.patrykandpatrick.vico.compose.cartesian.axis.rememberStart
import com.patrykandpatrick.vico.compose.cartesian.layer.rememberLine
import com.patrykandpatrick.vico.compose.cartesian.layer.rememberLineCartesianLayer
import com.patrykandpatrick.vico.compose.cartesian.rememberCartesianChart
import com.patrykandpatrick.vico.core.cartesian.axis.HorizontalAxis
import com.patrykandpatrick.vico.core.cartesian.axis.VerticalAxis
import com.patrykandpatrick.vico.core.cartesian.data.CartesianChartModelProducer
import com.patrykandpatrick.vico.core.cartesian.data.lineSeries
import com.patrykandpatrick.vico.core.cartesian.layer.LineCartesianLayer
import com.patrykandpatrick.vico.core.common.Fill

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BrentHistorialScreen(
    viewModel: BrentViewModel,
    onBack: () -> Unit
) {
    val brent     by viewModel.brent.collectAsState()
    val historial by viewModel.historial.collectAsState()

    val modelProducer = remember { CartesianChartModelProducer() }

    LaunchedEffect(historial) {
        if (historial.isNotEmpty()) {
            modelProducer.runTransaction {
                lineSeries { series(historial.map { it.precio }) }
            }
        }
    }

    Scaffold(
        topBar = {
            Column(
                modifier = Modifier
                    .background(Brush.horizontalGradient(listOf(EkoGreen40, EkoAmber40)))
                    .statusBarsPadding()
                    .padding(bottom = 8.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = stringResource(R.string.volver), tint = Color.White)
                    }
                    Text("🛢 Brent Crude Oil", color = Color.White, fontWeight = FontWeight.ExtraBold, fontSize = 20.sp)
                }
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(padding).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            brent?.let { b ->
                val subiendo = b.variacion >= 0
                val color    = if (subiendo) Color(0xFFFF6B35) else Color(0xFF4CAF50)
                val flecha   = if (subiendo) "▲" else "▼"
                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(stringResource(R.string.brent_precio_actual), fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text("${"%.2f".format(b.precio)} ${stringResource(R.string.brent_usd_barril)}", fontSize = 22.sp, fontWeight = FontWeight.Bold)
                        }
                        Text("$flecha ${"%.2f".format(b.variacionPct)}%", color = color, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }

            Text(stringResource(R.string.brent_ultimos_30_dias), fontWeight = FontWeight.SemiBold, fontSize = 15.sp)

            if (historial.isNotEmpty()) {
                Card(shape = RoundedCornerShape(12.dp), modifier = Modifier.fillMaxWidth()) {
                    val amberLine = LineCartesianLayer.rememberLine(
                        fill = LineCartesianLayer.LineFill.single(Fill(0xFFD97706.toInt()))
                    )
                    val lineLayer = rememberLineCartesianLayer(
                        lineProvider = LineCartesianLayer.LineProvider.series(amberLine)
                    )
                    CartesianChartHost(
                        chart = rememberCartesianChart(
                            lineLayer,
                            startAxis = VerticalAxis.rememberStart(),
                            bottomAxis = HorizontalAxis.rememberBottom()
                        ),
                        modelProducer = modelProducer,
                        modifier = Modifier.fillMaxWidth().height(220.dp).padding(8.dp)
                    )
                }

                val min = historial.minByOrNull { it.precio }
                val max = historial.maxByOrNull { it.precio }
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(stringResource(R.string.brent_minimo), fontSize = 11.sp, color = Color(0xFF4CAF50))
                        Text("${"%.2f".format(min?.precio)}$", fontWeight = FontWeight.Bold, color = Color(0xFF4CAF50))
                        Text(min?.fecha ?: "", fontSize = 10.sp)
                    }
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(stringResource(R.string.brent_maximo), fontSize = 11.sp, color = Color(0xFFFF6B35))
                        Text("${"%.2f".format(max?.precio)}$", fontWeight = FontWeight.Bold, color = Color(0xFFFF6B35))
                        Text(max?.fecha ?: "", fontSize = 10.sp)
                    }
                }
            } else {
                Box(modifier = Modifier.fillMaxWidth().height(220.dp), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = EkoGreen40)
                }
            }
        }
    }
}
