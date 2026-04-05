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
import com.catalabytes.ekopump.data.prefs.LocaleHelper
import com.catalabytes.ekopump.data.repository.Combustible
import com.catalabytes.ekopump.data.repository.GasolineraConDistancia
import com.catalabytes.ekopump.ui.common.UiState
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
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.border
import com.catalabytes.ekopump.ui.onboarding.OnboardingScreen

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
    brentViewModel: BrentViewModel = hiltViewModel()
) {
    val uiState     by viewModel.uiState.collectAsState()
    val combustible by viewModel.combustible.collectAsState()
    var tabActual   by remember { mutableStateOf(0) }
    var mostrarIdiomas  by remember { mutableStateOf(false) }
    var mostrarBrent    by remember { mutableStateOf(false) }
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

    if (mostrarIdiomas) {
        LanguageSelectorDialog(onDismiss = { mostrarIdiomas = false })
    }
    if (mostrarBrent) {
        BrentHistorialScreen(viewModel = brentViewModel, onBack = { mostrarBrent = false })
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
            }
        },
        bottomBar = {
            NavigationBar(
                containerColor = Color(0xFF0D1F0D),
                tonalElevation = 0.dp
            ) {
                val iconos = listOf(
                    Icons.Default.List to "Lista",
                    Icons.Default.Map to "Mapa",
                    Icons.Default.LocationOn to "Historial",
                    Icons.Default.Settings to "Perfil"
                )
                iconos.forEachIndexed { idx, (icon, label) ->
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
                        gasolineras = state.data,
                        combustible = combustible,
                        userLat = userLat,
                        userLon = userLon
                    )
                    is UiState.Loading -> CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center), color = EkoGreen40)
                    is UiState.Error   -> Text("⚠ ${state.message}",
                        modifier = Modifier.align(Alignment.Center),
                        color = MaterialTheme.colorScheme.error)
                }
                2 -> HistorialPlaceholder()
                3 -> PerfilScreen(viewModel = viewModel)
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
                        lista       = state.data,
                        combustible = combustible,
                        consumo     = viewModel.consumo.collectAsState().value,
                        litros      = viewModel.litros.collectAsState().value,
                        tendencias  = viewModel.tendencias.collectAsState().value
                    )
                }
            }
        }
    }
}

@Composable
fun HistorialPlaceholder() {
    Column(
        modifier = Modifier.fillMaxSize().background(Color(0xFF0D1F0D)),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("📊", fontSize = 56.sp)
        Spacer(Modifier.height(16.dp))
        Text("Historial de repostajes", fontWeight = FontWeight.ExtraBold,
            fontSize = 22.sp, color = Color.White)
        Spacer(Modifier.height(8.dp))
        Text("Próximamente", fontSize = 14.sp, color = Color(0xFF69F0AE))
        Spacer(Modifier.height(4.dp))
        Text(
            "Cada repostaje quedará registrado aquí con gasto, ahorro y gasolinera.",
            fontSize = 13.sp, color = Color(0xFF6B8F72),
            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
            modifier = Modifier.padding(horizontal = 32.dp)
        )
    }
}

@Composable
fun PerfilScreen(viewModel: GasolinerasViewModel) {
    val consumo     by viewModel.consumo.collectAsState()
    val litros      by viewModel.litros.collectAsState()
    val vehicleType by viewModel.vehicleType.collectAsState()
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
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(tipo.emoji, fontSize = 22.sp)
                        Text(tipo.labelEs, fontSize = 10.sp, fontWeight = FontWeight.SemiBold,
                            color = if (sel) verde else grayText,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center)
                    }
                }
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
                Text("${"%.1f".format(consumo)} L/100km", fontSize = 15.sp,
                    fontWeight = FontWeight.ExtraBold, color = verde)
            }
            Slider(value = consumo, onValueChange = { viewModel.setConsumo(it) },
                valueRange = vehicleType.consumoMin..vehicleType.consumoMax,
                colors = SliderDefaults.colors(thumbColor = verde, activeTrackColor = verde,
                    inactiveTrackColor = Color.White.copy(alpha = 0.15f)))
            Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween) {
                Text("${vehicleType.consumoMin.toInt()}L", fontSize = 10.sp, color = grayText)
                Text("${vehicleType.consumoMax.toInt()}L", fontSize = 10.sp, color = grayText)
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
fun ListaGasolineras(lista: List<GasolineraConDistancia>, combustible: Combustible, consumo: Float, litros: Float, tendencias: Map<String, com.catalabytes.ekopump.domain.model.TendenciaPrecio> = emptyMap()) {
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
                tendencia   = tendencias[lista[i].gasolinera.id] ?: com.catalabytes.ekopump.domain.model.TendenciaPrecio.ESTABLE
            )
        }
    }
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
    tendencia: com.catalabytes.ekopump.domain.model.TendenciaPrecio = com.catalabytes.ekopump.domain.model.TendenciaPrecio.ESTABLE
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val g = item.gasolinera
    val esMasBarata = posicion == 1
    val precio = combustible.precio(g)
    val ahorro = if (posicion > 1 && precio != null && precioRef != null && precioRef > 0) {
        val kmExtra = ((item.distanciaKm ?: 0.0) - kmRef).coerceAtLeast(0.0)
        com.catalabytes.ekopump.domain.calcularAhorro(precioRef, precio, kmExtra, consumo, litros)
    } else null

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
                // Chip ahorro/coste vs gasolinera más cercana
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
            precio?.let {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    // Badge tendencia ↑↓→
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
