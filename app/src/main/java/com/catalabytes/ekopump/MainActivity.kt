package com.catalabytes.ekopump

import android.Manifest
import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.clickable
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.catalabytes.ekopump.data.prefs.LocaleHelper
import com.catalabytes.ekopump.data.repository.Combustible
import com.catalabytes.ekopump.data.repository.GasolineraConDistancia
import com.catalabytes.ekopump.ui.common.UiState
import com.catalabytes.ekopump.ui.map.MapScreen
import com.catalabytes.ekopump.ui.settings.LanguageSelectorDialog
import com.catalabytes.ekopump.ui.brent.BrentWidget
import com.catalabytes.ekopump.ui.brent.BrentHistorialScreen
import com.catalabytes.ekopump.viewmodel.BrentViewModel
import com.catalabytes.ekopump.ui.theme.EkoAmber40
import com.catalabytes.ekopump.ui.theme.EkoGreen40
import com.catalabytes.ekopump.ui.theme.EkoPumpTheme
import com.catalabytes.ekopump.viewmodel.GasolinerasViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    override fun attachBaseContext(newBase: Context) {
        val prefs = newBase.getSharedPreferences("ekopump_lang", Context.MODE_PRIVATE)
        val langCode = prefs.getString("language", "system") ?: "system"
        super.attachBaseContext(LocaleHelper.applyLanguage(newBase, langCode))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent { EkoPumpTheme { GasolinerasScreen() } }
    }
}

@Composable
fun GasolinerasScreen(viewModel: GasolinerasViewModel = hiltViewModel(), brentViewModel: BrentViewModel = hiltViewModel()) {
    val uiState by viewModel.uiState.collectAsState()
    val combustible by viewModel.combustible.collectAsState()
    var mostrarMapa by remember { mutableStateOf(false) }
    var mostrarIdiomas by remember { mutableStateOf(false) }
    var mostrarBrent by remember { mutableStateOf(false) }
    val userLat by viewModel.userLat.collectAsState()
    val userLon by viewModel.userLon.collectAsState()

    val permisosLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { viewModel.cargar() }

    LaunchedEffect(Unit) {
        permisosLauncher.launch(arrayOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
        ))
    }

    if (mostrarIdiomas) {
        LanguageSelectorDialog(onDismiss = { mostrarIdiomas = false })
    }

    if (mostrarBrent) {
        BrentHistorialScreen(viewModel = brentViewModel, onBack = { mostrarBrent = false })
        return
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
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("⛽ EKOPUMP", color = Color.White, fontWeight = FontWeight.ExtraBold, fontSize = 22.sp)
                    Row {
                        IconButton(onClick = { mostrarIdiomas = true }) {
                            Icon(Icons.Default.Language, contentDescription = "Idioma", tint = Color.White)
                        }
                        IconButton(onClick = { mostrarMapa = !mostrarMapa }) {
                            Icon(
                                if (mostrarMapa) Icons.Default.List else Icons.Default.Map,
                                contentDescription = if (mostrarMapa) "Lista" else "Mapa",
                                tint = Color.White
                            )
                        }
                        IconButton(onClick = { viewModel.cargar() }) {
                            Icon(Icons.Default.Refresh, contentDescription = "Actualizar", tint = Color.White)
                        }
                    }
                }
                BrentWidget(viewModel = brentViewModel, onClick = { mostrarBrent = true })
                if (!mostrarMapa) {
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
                                    selectedContainerColor = Color.White,
                                    selectedLabelColor = EkoGreen40
                                )
                            )
                        }
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
                    Text(androidx.compose.ui.res.stringResource(com.catalabytes.ekopump.R.string.buscando), color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                is UiState.Error -> Column(
                    modifier = Modifier.align(Alignment.Center).padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("⚠️ ${state.message}", color = MaterialTheme.colorScheme.error)
                    Spacer(Modifier.height(12.dp))
                    Button(onClick = { viewModel.cargar() },
                        colors = ButtonDefaults.buttonColors(containerColor = EkoGreen40)
                    ) { Text("Reintentar") }
                }
                is UiState.Success -> {
                    if (mostrarMapa) {
                        MapScreen(
                            gasolineras = state.data,
                            combustible = combustible,
                            userLat = userLat,
                            userLon = userLon
                        )
                    } else {
                        ListaGasolineras(state.data, combustible)
                    }
                }
            }
        }
    }
}

@Composable
fun ListaGasolineras(lista: List<GasolineraConDistancia>, combustible: Combustible) {
    LazyColumn(contentPadding = PaddingValues(vertical = 8.dp)) {
        item {
            Text(
                androidx.compose.ui.res.stringResource(com.catalabytes.ekopump.R.string.gasolineras_cercanas, lista.size),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
            )
        }
        items(lista.size) { i -> GasolineraItem(lista[i], combustible, posicion = i + 1) }
    }
}

@Composable
fun GasolineraItem(item: GasolineraConDistancia, combustible: Combustible, posicion: Int) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val g = item.gasolinera
    val precio = combustible.precio(g)
    val esMasBarata = posicion == 1

    Card(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 4.dp)
            .clickable { com.catalabytes.ekopump.ui.navigation.navegarAGasolinera(context, g.latitud, g.longitud, g.nombre) },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (esMasBarata) EkoGreen40.copy(alpha = 0.08f)
            else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
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
                    if (esMasBarata) Text("🏆 ", fontSize = 14.sp)
                    Text(g.nombre, fontWeight = FontWeight.Bold, fontSize = 15.sp,
                        color = if (esMasBarata) EkoGreen40 else MaterialTheme.colorScheme.onSurface)
                }
                Text(g.direccion, style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
                Row(verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    Icon(Icons.Default.LocationOn, null, Modifier.size(12.dp),
                        tint = MaterialTheme.colorScheme.primary)
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
                    Text("${"%.3f".format(it)}€", fontWeight = FontWeight.ExtraBold,
                        fontSize = 16.sp, color = Color.White)
                }
            }
        }
    }
}
