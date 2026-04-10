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
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Settings
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
import com.catalabytes.ekopump.data.local.entity.RefuelEntity
import com.catalabytes.ekopump.data.prefs.LocaleHelper
import com.catalabytes.ekopump.data.repository.Combustible
import com.catalabytes.ekopump.data.repository.GasolineraConDistancia
import com.catalabytes.ekopump.ui.common.UiState
import com.catalabytes.ekopump.ui.history.AddRefuelSheet
import com.catalabytes.ekopump.ui.map.GasolineraDetailSheet
import com.catalabytes.ekopump.ui.history.HistoryScreen
import com.catalabytes.ekopump.ui.favorites.FavoritasScreen
import com.catalabytes.ekopump.ui.history.HistoryViewModel
import com.catalabytes.ekopump.ui.map.MapScreen
import com.catalabytes.ekopump.ui.settings.LanguageSelectorDialog
import com.catalabytes.ekopump.ui.settings.CalculadorDialog
import com.catalabytes.ekopump.domain.calcularAhorro
import com.catalabytes.ekopump.ui.brent.BrentWidget
import com.catalabytes.ekopump.ui.brent.BrentHistorialScreen
import com.catalabytes.ekopump.viewmodel.BrentViewModel
import com.catalabytes.ekopump.ui.theme.EkoAmber40
import com.catalabytes.ekopump.ui.theme.EkoGreen40
import com.catalabytes.ekopump.ui.theme.EkoPumpTheme
import com.catalabytes.ekopump.viewmodel.GasolinerasViewModel
import dagger.hilt.android.AndroidEntryPoint
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Public
import android.content.Intent
import android.net.Uri
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.border
import com.catalabytes.ekopump.ui.onboarding.OnboardingScreen
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState

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
        setContent { EkoPumpTheme { EkoPumpApp() } }
    }
}


@Composable
fun EkoPumpApp() {
    val context = androidx.compose.ui.platform.LocalContext.current
    val prefs = context.getSharedPreferences("ekopump_onboarding", android.content.Context.MODE_PRIVATE)
    var onboardingCompletado by remember { mutableStateOf(prefs.getBoolean("completado", false)) }
    if (!onboardingCompletado) {
        OnboardingScreen(onFinish = {
            prefs.edit().putBoolean("completado", true).apply()
            onboardingCompletado = true
        })
    } else {
        GasolinerasScreen()
    }
}

@Composable
fun GasolinerasScreen(
    viewModel: GasolinerasViewModel = hiltViewModel(),
    brentViewModel: BrentViewModel = hiltViewModel(),
    historyViewModel: HistoryViewModel = hiltViewModel()
) {
    val uiState     by viewModel.uiState.collectAsState()
    val combustible by viewModel.combustible.collectAsState()
    val radioKm     by viewModel.radioKm.collectAsState()
    val alertIds          by viewModel.alertIds.collectAsState()
    val lastRefreshMs     by viewModel.lastRefreshMs.collectAsState()
    val gpsDisponible     by viewModel.gpsDisponible.collectAsState()
    val modoTransportista by viewModel.modoTransportista.collectAsState()
    val tendencias        by viewModel.tendencias.collectAsState()
    var tabActual   by remember { mutableStateOf(0) }
    val ctx = androidx.compose.ui.platform.LocalContext.current
    var mostrarIdiomas  by remember { mutableStateOf(false) }
    var gasolineraMapaSeleccionada by remember { mutableStateOf<com.catalabytes.ekopump.data.repository.GasolineraConDistancia?>(null) }
    var mostrarRepostarDesdeDetalle  by remember { mutableStateOf(false) }
    var mostrarBrent              by remember { mutableStateOf(false) }
    var mostrarRegistrarRepostaje by remember { mutableStateOf(false) }
    var isRefreshing by remember { mutableStateOf(false) }
    var bannerTransportistaDismissed by remember { mutableStateOf(false) }
    LaunchedEffect(modoTransportista) { if (!modoTransportista) bannerTransportistaDismissed = false }

    LaunchedEffect(uiState) { isRefreshing = false }
    val userLat by viewModel.userLat.collectAsState()
    val userLon by viewModel.userLon.collectAsState()

    val permisosLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { viewModel.cargar() }

    LaunchedEffect(Unit) {
        permisosLauncher.launch(arrayOf(
            android.Manifest.permission.POST_NOTIFICATIONS,
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
        ))
    }

    // BottomSheet desde mapa — paso 1: info + horario + favorita
    gasolineraMapaSeleccionada?.let { item ->
        if (mostrarRepostarDesdeDetalle) {
            // Paso 2: repostaje
            val precio = combustible.precio(item.gasolinera)
            AddRefuelSheet(
                stationName      = item.gasolinera.nombre,
                stationAddress   = item.gasolinera.direccion,
                fuelType         = combustible.label,
                pricePerLiter    = precio ?: 0.0,
                avgNationalPrice = precio ?: 0.0,
                latitude         = item.gasolinera.latitud,
                longitude        = item.gasolinera.longitud,
                onSave           = { historyViewModel.addRefuel(it) },
                onDismiss        = {
                    mostrarRepostarDesdeDetalle = false
                    gasolineraMapaSeleccionada  = null
                }
            )
        } else {
            // Paso 1: detalle
            val esMasBarataItem = when (val s = uiState) {
                is UiState.Success -> s.data.firstOrNull()?.gasolinera?.id == item.gasolinera.id
                else -> false
            }
            GasolineraDetailSheet(
                item        = item,
                combustible = combustible,
                onRepostar  = { mostrarRepostarDesdeDetalle = true },
                onDismiss   = { gasolineraMapaSeleccionada = null },
                hasAlert    = alertIds.contains(item.gasolinera.id),
                onSetAlert  = { umbral ->
                    viewModel.setAlert(item.gasolinera.id, item.gasolinera.nombre, combustible.name, umbral)
                },
                onRemoveAlert = { viewModel.removeAlert(item.gasolinera.id) },
                tendencia   = tendencias[item.gasolinera.id]
                    ?: com.catalabytes.ekopump.domain.model.TendenciaPrecio.ESTABLE,
                esMasBarata = esMasBarataItem
            )
        }
    }

    if (mostrarIdiomas) {
        LanguageSelectorDialog(onDismiss = { mostrarIdiomas = false })
    }
    if (mostrarBrent) {
        BrentHistorialScreen(viewModel = brentViewModel, onBack = { mostrarBrent = false })
        return
    }
    if (mostrarRegistrarRepostaje) {
        com.catalabytes.ekopump.ui.repostaje.RegistrarRepostajeScreen(
            onBack = { mostrarRegistrarRepostaje = false }
        )
        return
    }

    val tabs = listOf(
        Triple("Lista",     Icons.Default.List,     0),
        Triple("Mapa",      Icons.Default.Map,      1),
        Triple("Historial", Icons.Default.Settings, 2),
        Triple("Perfil",    Icons.Default.Settings, 3)
    )

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
                    Text("⛽ EKOPUMP", color = Color.White,
                        fontWeight = FontWeight.ExtraBold, fontSize = 22.sp)
                    Row {
                        IconButton(onClick = { mostrarIdiomas = true }) {
                            Icon(Icons.Default.Language, contentDescription = "Idioma", tint = Color.White)
                        }
                        IconButton(onClick = {
                            val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://ekopump.es"))
                            ctx.startActivity(intent)
                        }) {
                            Icon(Icons.Default.Public, contentDescription = "Web", tint = Color.White)
                        }
                        IconButton(onClick = { viewModel.cargar() }) {
                            Icon(Icons.Default.Refresh, contentDescription = "Actualizar", tint = Color.White)
                        }
                    }
                }
                BrentWidget(viewModel = brentViewModel, onClick = { mostrarBrent = true })
                if (tabActual == 0) {
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 4.dp)
                    ) {
                        items(Combustible.entries) { c ->
                            FilterChip(
                                selected = combustible == c,
                                onClick  = { viewModel.setCombustible(c) },
                                label    = { Text(c.label, fontSize = 13.sp) },
                                colors   = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = Color.White,
                                    selectedLabelColor     = EkoGreen40
                                )
                            )
                        }
                    }
                }
                // ── Slider radio de búsqueda ─────────────────────────────
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        "📍 Radio: ${radioKm.toInt()} km",
                        color = Color.White,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium
                    )
                    Slider(
                        value = radioKm.toFloat(),
                        onValueChange = { viewModel.setRadioKm(it.toDouble()) },
                        valueRange = 2f..50f,
                        steps = 11,
                        modifier = Modifier.weight(1f).padding(horizontal = 12.dp),
                        colors = SliderDefaults.colors(
                            thumbColor = Color.White,
                            activeTrackColor = Color(0xFF00C853),
                            inactiveTrackColor = Color(0xFF2E7D32)
                        )
                    )
                }
                // ── Timestamp última actualización ───────────────────────
                if (lastRefreshMs > 0L) {
                    val ahoraMs = System.currentTimeMillis()
                    val diffMin = ((ahoraMs - lastRefreshMs) / 60_000L).toInt()
                    val textoActualizado = when {
                        diffMin < 1  -> "Actualizado: ahora mismo"
                        diffMin == 1 -> "Actualizado: hace 1 min"
                        else         -> "Actualizado: hace $diffMin min"
                    }
                    Text(
                        text = textoActualizado,
                        color = Color(0xFF4CAF50),
                        fontSize = 11.sp,
                        modifier = androidx.compose.ui.Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 2.dp)
                    )
                }
                // ── Banner modo transportista ─────────────────────────────
                if (modoTransportista && !bannerTransportistaDismissed) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color(0xFFFF8F00).copy(alpha = 0.2f))
                            .padding(horizontal = 16.dp, vertical = 5.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            "🚛 Modo Transportista · Gasóleo A · Radio 25 km",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color(0xFFFFD54F),
                            modifier = Modifier.weight(1f)
                        )
                        IconButton(
                            onClick = { bannerTransportistaDismissed = true },
                            modifier = Modifier.size(28.dp)
                        ) {
                            Icon(
                                Icons.Default.Close,
                                contentDescription = "Cerrar",
                                tint = Color(0xFFFFD54F),
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }
            }
        },
        bottomBar = {
            NavigationBar(
                containerColor = Color(0xFF0D1F0D),
                tonalElevation = 0.dp
            ) {
                val navItems = listOf(
                    Triple(Icons.Default.List,     "Lista",     0),
                    Triple(Icons.Default.Map,      "Mapa",      1),
                    Triple(Icons.Default.History,  "Historial", 2),
                    Triple(Icons.Default.Favorite, "Favoritas", 3),
                    Triple(Icons.Default.Settings, "Perfil",    4)
                )
                navItems.forEach { (icon, label, idx) ->
                    NavigationBarItem(
                        selected = tabActual == idx,
                        onClick  = { tabActual = idx },
                        icon  = { Icon(icon, contentDescription = label) },
                        label = { Text(label, fontSize = 11.sp) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor   = Color(0xFF69F0AE),
                            selectedTextColor   = Color(0xFF69F0AE),
                            unselectedIconColor = Color(0xFF6B8F72),
                            unselectedTextColor = Color(0xFF6B8F72),
                            indicatorColor      = Color(0xFF1B5E20)
                        )
                    )
                }
            }
        }
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize().padding(padding)) {
            when (tabActual) {
                1 -> when (val state = uiState) {
                    is UiState.Success -> MapScreen(
                        gasolineras        = state.data,
                        combustible        = combustible,
                        userLat            = userLat,
                        userLon            = userLon,
                        locationDisponible = gpsDisponible,
                        onGasolineraClick  = { gasolineraMapaSeleccionada = it }
                    )
                    is UiState.Loading -> CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center), color = EkoGreen40)
                    is UiState.Error   -> Text("⚠ ${state.message}",
                        modifier = Modifier.align(Alignment.Center),
                        color = MaterialTheme.colorScheme.error)
                }
                2 -> HistoryScreen(
                    viewModel   = historyViewModel,
                    onRegistrar = { mostrarRegistrarRepostaje = true }
                )
                3 -> when (val state = uiState) {
                    is UiState.Success -> FavoritasScreen(
                        gasolineras       = state.data,
                        combustible       = combustible,
                        alertIds          = alertIds,
                        onGasolineraClick = { gasolineraMapaSeleccionada = it }
                    )
                    else -> HistoryScreen(viewModel = historyViewModel, onRegistrar = { mostrarRegistrarRepostaje = true })
                }
                4 -> PerfilScreen(viewModel = viewModel)
                else -> when (val state = uiState) {
                    is UiState.Loading -> Column(
                        modifier = Modifier.align(Alignment.Center),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        CircularProgressIndicator(color = EkoGreen40)
                        Spacer(Modifier.height(12.dp))
                        Text(androidx.compose.ui.res.stringResource(
                            com.catalabytes.ekopump.R.string.buscando),
                            color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    is UiState.Error -> Column(
                        modifier = Modifier.align(Alignment.Center).padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("⚠ ${state.message}", color = MaterialTheme.colorScheme.error)
                        Spacer(Modifier.height(12.dp))
                        Button(onClick = { viewModel.cargar() },
                            colors = ButtonDefaults.buttonColors(containerColor = EkoGreen40)
                        ) { Text("Reintentar") }
                    }
                    is UiState.Success -> ListaGasolineras(
                        lista        = state.data,
                        combustible  = combustible,
                        consumo      = viewModel.consumo.collectAsState().value,
                        litros       = viewModel.litros.collectAsState().value,
                        tendencias   = viewModel.tendencias.collectAsState().value,
                        onRepostar   = { historyViewModel.addRefuel(it) },
                        isRefreshing = isRefreshing,
                        onRefresh    = { isRefreshing = true; viewModel.cargar() }
                    )
                }
            }
        }
    }
}

@Composable
fun PerfilScreen(viewModel: GasolinerasViewModel) {
    val consumo           by viewModel.consumo.collectAsState()
    val litros            by viewModel.litros.collectAsState()
    val vehicleType       by viewModel.vehicleType.collectAsState()
    val energyType        by viewModel.energyType.collectAsState()
    val modoTransportista by viewModel.modoTransportista.collectAsState()
    val verde    = Color(0xFF69F0AE)
    val darkBg   = Color(0xFF0D1F0D)
    val darkCard = Color(0xFF162916)
    val grayText = Color(0xFFB0BEC5)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(darkBg)
            .padding(20.dp)
            .verticalScroll(androidx.compose.foundation.rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        Spacer(Modifier.height(8.dp))

        // ── Modo Transportista ───────────────────────────────────────────
        Card(
            colors = CardDefaults.cardColors(containerColor = darkCard),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        "🚛 Modo Transportista",
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = if (modoTransportista) Color(0xFFFFD54F) else Color.White
                    )
                    Text(
                        "Gasóleo A · Radio 25 km · Consumo 30 L/100km",
                        fontSize = 12.sp,
                        color = grayText
                    )
                }
                Switch(
                    checked = modoTransportista,
                    onCheckedChange = { viewModel.setModoTransportista(it) },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = Color(0xFFFF8F00),
                        checkedTrackColor = Color(0xFFFF8F00).copy(alpha = 0.4f),
                        uncheckedThumbColor = grayText,
                        uncheckedTrackColor = grayText.copy(alpha = 0.2f)
                    )
                )
            }
        }

        Text("🚗 Mi vehículo", fontWeight = FontWeight.ExtraBold,
            fontSize = 24.sp, color = Color.White)
        Text("¿Qué conduces?", fontSize = 13.sp, color = grayText)

        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            com.catalabytes.ekopump.domain.model.VehicleType.entries.forEach { tipo ->
                val sel = vehicleType == tipo
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(12.dp))
                        .background(if (sel) verde.copy(alpha = 0.15f) else darkCard)
                        .border(1.5.dp,
                            if (sel) verde else Color.White.copy(alpha = 0.1f),
                            RoundedCornerShape(12.dp))
                        .clickable {
                            viewModel.setVehicleType(tipo)
                            viewModel.setConsumo(tipo.consumoDefault)
                            viewModel.setLitros(tipo.litrosDefault)
                        }
                        .padding(vertical = 10.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.width(56.dp)
                    ) {
                        Text(tipo.emoji, fontSize = 22.sp)
                        Text(
                            tipo.labelEs, fontSize = 10.sp, fontWeight = FontWeight.SemiBold,
                            color = if (sel) verde else grayText,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                            maxLines = 1,
                            overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                        )
                    }
                }
            }
        }

        // Selector de energía alternativa
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("⚡ Energía alternativa", fontSize = 13.sp, color = grayText)
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                com.catalabytes.ekopump.domain.model.EnergyType.entries.forEach { tipo ->
                    val sel = energyType == tipo
                    val accentColor = when (tipo) {
                        com.catalabytes.ekopump.domain.model.EnergyType.GNC     -> Color(0xFF29B6F6)
                        com.catalabytes.ekopump.domain.model.EnergyType.GNL     -> Color(0xFFAB47BC)
                        com.catalabytes.ekopump.domain.model.EnergyType.ADBLUE  -> Color(0xFF1E88E5)
                        com.catalabytes.ekopump.domain.model.EnergyType.EV      -> Color(0xFFFFD600)
                    }
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(12.dp))
                            .background(if (sel) accentColor.copy(alpha = 0.18f) else darkCard)
                            .border(1.5.dp,
                                if (sel) accentColor else Color.White.copy(alpha = 0.08f),
                                RoundedCornerShape(12.dp))
                            .clickable {
                                viewModel.setEnergyType(if (sel) null else tipo)
                            }
                            .padding(vertical = 10.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(tipo.emoji, fontSize = 18.sp)
                            Text(tipo.labelEs, fontSize = 10.sp, fontWeight = FontWeight.Bold,
                                color = if (sel) accentColor else grayText)
                            Text(tipo.descripcion, fontSize = 8.sp, color = grayText.copy(alpha = 0.7f),
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                                lineHeight = 10.sp)
                        }
                    }
                }
            }
            if (energyType != null) {
                Text(
                    "Toca de nuevo para deseleccionar",
                    fontSize = 10.sp,
                    color = grayText.copy(alpha = 0.5f)
                )
            }
        }

        // Consumo
        Column(
            Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp))
                .background(darkCard).padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween, Alignment.CenterVertically) {
                Text("Consumo", fontSize = 13.sp, color = grayText)
                val esElectrico = vehicleType == com.catalabytes.ekopump.domain.model.VehicleType.ELECTRICO
                Text(
                    "${"%.1f".format(consumo)} ${if (esElectrico) "kWh/100km" else "L/100km"}",
                    fontSize = 15.sp, fontWeight = FontWeight.ExtraBold, color = verde
                )
            }
            Slider(value = consumo, onValueChange = { viewModel.setConsumo(it) },
                valueRange = vehicleType.consumoMin..vehicleType.consumoMax,
                colors = SliderDefaults.colors(thumbColor = verde, activeTrackColor = verde,
                    inactiveTrackColor = Color.White.copy(alpha = 0.15f)))
            val unidad = if (vehicleType == com.catalabytes.ekopump.domain.model.VehicleType.ELECTRICO) "kWh" else "L"
            Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween) {
                Text("${vehicleType.consumoMin.toInt()}$unidad", fontSize = 10.sp, color = grayText)
                Text("${vehicleType.consumoMax.toInt()}$unidad", fontSize = 10.sp, color = grayText)
            }
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                vehicleType.quickConsumos.forEach { v ->
                    val s = consumo == v
                    Box(Modifier.clip(RoundedCornerShape(8.dp))
                        .background(if (s) verde else Color.White.copy(0.07f))
                        .border(1.dp, if (s) verde else Color.White.copy(0.15f), RoundedCornerShape(8.dp))
                        .clickable { viewModel.setConsumo(v) }
                        .padding(horizontal = 10.dp, vertical = 5.dp)
                    ) { Text("${v.toInt()}L", fontSize = 11.sp, fontWeight = FontWeight.Bold,
                        color = if (s) Color(0xFF0D1F0D) else grayText) }
                }
            }
        }

        // Litros
        Column(
            Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp))
                .background(darkCard).padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween, Alignment.CenterVertically) {
                Text("Litros a repostar", fontSize = 13.sp, color = grayText)
                Text("${"%.0f".format(litros)} L", fontSize = 15.sp,
                    fontWeight = FontWeight.ExtraBold, color = verde)
            }
            Slider(value = litros, onValueChange = { viewModel.setLitros(it) },
                valueRange = vehicleType.litrosMin..vehicleType.litrosMax,
                colors = SliderDefaults.colors(thumbColor = verde, activeTrackColor = verde,
                    inactiveTrackColor = Color.White.copy(alpha = 0.15f)))
            Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween) {
                Text("${vehicleType.litrosMin.toInt()}L", fontSize = 10.sp, color = grayText)
                Text("${vehicleType.litrosMax.toInt()}L", fontSize = 10.sp, color = grayText)
            }
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                vehicleType.quickLitros.forEach { v ->
                    val s = litros == v
                    Box(Modifier.clip(RoundedCornerShape(8.dp))
                        .background(if (s) verde else Color.White.copy(0.07f))
                        .border(1.dp, if (s) verde else Color.White.copy(0.15f), RoundedCornerShape(8.dp))
                        .clickable { viewModel.setLitros(v) }
                        .padding(horizontal = 10.dp, vertical = 5.dp)
                    ) { Text("${v.toInt()}L", fontSize = 11.sp, fontWeight = FontWeight.Bold,
                        color = if (s) Color(0xFF0D1F0D) else grayText) }
                }
            }
        }
    }
}

@Composable
@OptIn(ExperimentalMaterial3Api::class)
fun ListaGasolineras(
    lista: List<GasolineraConDistancia>,
    combustible: Combustible,
    consumo: Float,
    litros: Float,
    tendencias: Map<String, com.catalabytes.ekopump.domain.model.TendenciaPrecio> = emptyMap(),
    onRepostar: (RefuelEntity) -> Unit = {},
    isRefreshing: Boolean = false,
    onRefresh: () -> Unit = {}
) {
    PullToRefreshBox(
        isRefreshing = isRefreshing,
        onRefresh    = onRefresh,
        modifier     = Modifier.fillMaxSize()
    ) {
    LazyColumn(contentPadding = PaddingValues(vertical = 8.dp)) {
        item {
            Text(
                androidx.compose.ui.res.stringResource(com.catalabytes.ekopump.R.string.gasolineras_cercanas, lista.size),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
            )
        }
        items(lista.size) { i ->
            val masCercana = lista.minByOrNull { it.distanciaKm ?: Double.MAX_VALUE }
            val precioRef  = masCercana?.let { combustible.precio(it.gasolinera) }
            val kmRef      = masCercana?.distanciaKm ?: 0.0
            GasolineraItem(
                item        = lista[i],
                combustible = combustible,
                posicion    = i + 1,
                precioRef   = precioRef,
                kmRef       = kmRef,
                consumo     = consumo.toDouble(),
                litros      = litros.toDouble(),
                tendencia   = tendencias[lista[i].gasolinera.id] ?: com.catalabytes.ekopump.domain.model.TendenciaPrecio.ESTABLE,
                onRepostar  = onRepostar
            )
        }
    }
    } // cierre PullToRefreshBox
}

@Composable
fun GasolineraItem(
    item: GasolineraConDistancia,
    combustible: Combustible,
    posicion: Int,
    precioRef: Double?,
    kmRef: Double,
    consumo: Double,
    litros: Double,
    tendencia: com.catalabytes.ekopump.domain.model.TendenciaPrecio = com.catalabytes.ekopump.domain.model.TendenciaPrecio.ESTABLE,
    onRepostar: (RefuelEntity) -> Unit = {}
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val g = item.gasolinera
    val esMasBarata = posicion == 1
    val precio = combustible.precio(g)
    var mostrarSheet by remember { mutableStateOf(false) }

    val ahorro = if (posicion > 1 && precio != null && precioRef != null && precioRef > 0) {
        val kmExtra = ((item.distanciaKm ?: 0.0) - kmRef).coerceAtLeast(0.0)
        com.catalabytes.ekopump.domain.calcularAhorro(precioRef, precio, kmExtra, consumo, litros)
    } else null

    // BottomSheet repostaje
    if (mostrarSheet && precio != null) {
        AddRefuelSheet(
            stationName      = g.nombre,
            stationAddress   = g.direccion,
            fuelType         = combustible.label,
            pricePerLiter    = precio,
            avgNationalPrice = precioRef ?: precio,
            latitude         = g.latitud,
            longitude        = g.longitud,
            onSave           = onRepostar,
            onDismiss        = { mostrarSheet = false }
        )
    }

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
                ahorro?.let { a ->
                    val (label, color) = if (a.valeLaPena)
                        "✅ Ahorras ${"%.2f".format(a.beneficioNeto)}€" to androidx.compose.ui.graphics.Color(0xFF2E7D32)
                    else
                        "❌ Gastas ${"%.2f".format(-a.beneficioNeto)}€ más" to androidx.compose.ui.graphics.Color(0xFFB71C1C)
                    Text(
                        label,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = color,
                        modifier = Modifier.padding(top = 3.dp)
                    )
                }
            }
            Spacer(Modifier.width(8.dp))
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                // Botón ⛽ Repostar aquí
                if (precio != null) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(EkoGreen40.copy(alpha = 0.15f))
                            .clickable { mostrarSheet = true }
                            .padding(horizontal = 8.dp, vertical = 6.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("⛽", fontSize = 18.sp)
                    }
                }
                precio?.let {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        val (badgeText, badgeColor) = when (tendencia) {
                            com.catalabytes.ekopump.domain.model.TendenciaPrecio.SUBE ->
                                "↑" to androidx.compose.ui.graphics.Color(0xFFB71C1C)
                            com.catalabytes.ekopump.domain.model.TendenciaPrecio.BAJA ->
                                "↓" to androidx.compose.ui.graphics.Color(0xFF2E7D32)
                            com.catalabytes.ekopump.domain.model.TendenciaPrecio.ESTABLE ->
                                "→" to androidx.compose.ui.graphics.Color(0xFF9E9E9E)
                        }
                        Text(
                            badgeText,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = badgeColor,
                            modifier = Modifier.padding(bottom = 2.dp)
                        )
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
    }
}
