package com.catalabytes.ekopump

import android.Manifest
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.catalabytes.ekopump.data.repository.Combustible
import com.catalabytes.ekopump.data.repository.GasolineraConDistancia
import com.catalabytes.ekopump.ui.common.UiState
import com.catalabytes.ekopump.ui.theme.EkoPumpTheme
import com.catalabytes.ekopump.ui.theme.EkoAmber40
import com.catalabytes.ekopump.ui.theme.EkoGreen40
import com.catalabytes.ekopump.viewmodel.GasolinerasViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent { EkoPumpTheme { GasolinerasScreen() } }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GasolinerasScreen(viewModel: GasolinerasViewModel = hiltViewModel()) {
    val uiState by viewModel.uiState.collectAsState()
    val combustible by viewModel.combustible.collectAsState()

    val permisosLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { viewModel.cargar() }

    LaunchedEffect(Unit) {
        permisosLauncher.launch(arrayOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
        ))
    }

    Scaffold(
        topBar = {
            Column(
                modifier = Modifier
                    .background(
                        Brush.horizontalGradient(listOf(EkoGreen40, EkoAmber40))
                    )
                    .statusBarsPadding()
                    .padding(bottom = 8.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "⛽ EKOPUMP",
                        color = androidx.compose.ui.graphics.Color.White,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 22.sp
                    )
                    IconButton(onClick = { viewModel.cargar() }) {
                        Icon(
                            Icons.Default.Refresh,
                            contentDescription = "Actualizar",
                            tint = androidx.compose.ui.graphics.Color.White
                        )
                    }
                }
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 4.dp)
                ) {
                    items(Combustible.entries) { c ->
                        FilterChip(
                            selected = combustible == c,
                            onClick = { viewModel.setCombustible(c) },
                            label = { Text(c.label, fontSize = 13.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = androidx.compose.ui.graphics.Color.White,
                                selectedLabelColor = EkoGreen40
                            )
                        )
                    }
                }
            }
        }
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize().padding(padding)) {
            when (val state = uiState) {
                is UiState.Loading -> Column(
                    modifier = Modifier.align(Alignment.Center),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    CircularProgressIndicator(color = EkoGreen40)
                    Spacer(Modifier.height(12.dp))
                    Text("Buscando gasolineras...", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                is UiState.Error -> Column(
                    modifier = Modifier.align(Alignment.Center).padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("⚠️ ${state.message}", color = MaterialTheme.colorScheme.error)
                    Spacer(Modifier.height(12.dp))
                    Button(
                        onClick = { viewModel.cargar() },
                        colors = ButtonDefaults.buttonColors(containerColor = EkoGreen40)
                    ) { Text("Reintentar") }
                }
                is UiState.Success -> {
                    val lista = state.data
                    LazyColumn(contentPadding = PaddingValues(vertical = 8.dp)) {
                        item {
                            Text(
                                "${lista.size} gasolineras en 10 km · ordenadas por precio",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
                            )
                        }
                        items(lista.size) { i ->
                            GasolineraItem(lista[i], combustible, posicion = i + 1)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun GasolineraItem(item: GasolineraConDistancia, combustible: Combustible, posicion: Int) {
    val g = item.gasolinera
    val precio = combustible.precio(g)
    val esMasBarata = posicion == 1

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 4.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (esMasBarata)
                EkoGreen40.copy(alpha = 0.08f)
            else
                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        ),
        elevation = CardDefaults.cardElevation(if (esMasBarata) 4.dp else 1.dp)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (esMasBarata) {
                        Text("🏆 ", fontSize = 14.sp)
                    }
                    Text(
                        g.nombre,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = if (esMasBarata) EkoGreen40 else MaterialTheme.colorScheme.onSurface
                    )
                }
                Text(
                    g.direccion,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        Icons.Default.LocationOn,
                        contentDescription = null,
                        modifier = Modifier.size(12.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        item.distanciaKm?.let { "${"%.1f".format(it)} km · ${g.localidad}" } ?: g.localidad,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
            Spacer(Modifier.width(8.dp))
            precio?.let {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (esMasBarata) EkoGreen40 else MaterialTheme.colorScheme.primary)
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                ) {
                    Text(
                        "${"%.3f".format(it)}€",
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 16.sp,
                        color = androidx.compose.ui.graphics.Color.White
                    )
                }
            }
        }
    }
}
