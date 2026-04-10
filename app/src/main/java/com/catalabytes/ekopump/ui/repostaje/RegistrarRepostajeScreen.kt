package com.catalabytes.ekopump.ui.repostaje

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.catalabytes.ekopump.ui.theme.EkoGreen40
import com.catalabytes.ekopump.viewmodel.GuardarResult
import com.catalabytes.ekopump.viewmodel.RegistrarRepostajeViewModel

private val EkoDark = Color(0xFF0D1B2A)
private val EkoCard = Color(0xFF1A2D40)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegistrarRepostajeScreen(
    onBack: () -> Unit,
    gasolineraId: String? = null,
    gasolineraName: String? = null,
    precioSugerido: Double? = null,
    viewModel: RegistrarRepostajeViewModel = hiltViewModel()
) {
    val ultimoOdo     by viewModel.ultimoOdometroKm.collectAsState()
    val litrosPrefill by viewModel.litrosPrefill.collectAsState()
    val resultado     by viewModel.resultado.collectAsState()

    var odometroStr  by remember { mutableStateOf("") }
    var litrosStr    by remember { mutableStateOf("") }
    var precioStr    by remember { mutableStateOf(precioSugerido?.let { "%.3f".format(it) } ?: "") }
    var nombreStr    by remember { mutableStateOf(gasolineraName ?: "") }

    LaunchedEffect(litrosPrefill) {
        if (litrosStr.isEmpty()) litrosStr = "%.0f".format(litrosPrefill)
    }

    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(resultado) {
        when (val r = resultado) {
            is GuardarResult.Guardado -> { onBack(); viewModel.resetResultado() }
            is GuardarResult.Advertencia -> {
                snackbarHostState.showSnackbar(r.mensaje, duration = SnackbarDuration.Long)
                // Guardado igualmente — solo advertencia
                onBack()
                viewModel.resetResultado()
            }
            is GuardarResult.Error -> {
                snackbarHostState.showSnackbar(r.mensaje)
                viewModel.resetResultado()
            }
            else -> Unit
        }
    }

    Scaffold(
        containerColor = EkoDark,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(EkoGreen40)
                    .statusBarsPadding()
                    .padding(vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBack) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "Volver", tint = Color.White)
                }
                Text(
                    "Registrar repostaje",
                    color = Color.White,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 20.sp
                )
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(20.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            if (ultimoOdo > 0) {
                Text(
                    "Ultimo odómetro registrado: $ultimoOdo km",
                    fontSize = 13.sp,
                    color = Color(0xFF80CBC4)
                )
            }

            EkoTextField(
                value = odometroStr,
                onValueChange = { v ->
                    // Solo dígitos, máx 7 cifras
                    val filtered = v.filter { it.isDigit() }.take(7)
                    odometroStr = filtered
                },
                label = "Odómetro actual (km)",
                placeholder = "Ej: 45230",
                keyboardType = KeyboardType.Number
            )

            EkoTextField(
                value = litrosStr,
                onValueChange = { v ->
                    // Hasta 2 decimales, solo dígitos y punto/coma
                    val sanitized = v.replace(",", ".").filter { it.isDigit() || it == '.' }
                    val parts = sanitized.split(".")
                    val filtered = if (parts.size > 1)
                        "${parts[0]}.${parts[1].take(2)}"
                    else sanitized
                    litrosStr = filtered.take(6) // máx "200.00"
                },
                label = "Litros repostados",
                placeholder = "Ej: 40",
                keyboardType = KeyboardType.Decimal
            )

            EkoTextField(
                value = precioStr,
                onValueChange = { v ->
                    // Hasta 3 decimales, solo dígitos y punto/coma
                    val sanitized = v.replace(",", ".").filter { it.isDigit() || it == '.' }
                    val parts = sanitized.split(".")
                    val filtered = if (parts.size > 1)
                        "${parts[0]}.${parts[1].take(3)}"
                    else sanitized
                    precioStr = filtered.take(5) // máx "5.000"
                },
                label = "Precio por litro (€)",
                placeholder = "Ej: 1.569",
                keyboardType = KeyboardType.Decimal
            )

            EkoTextField(
                value = nombreStr,
                onValueChange = { v ->
                    // Máx 100 chars, sin caracteres peligrosos (<, >, &, ", ')
                    val filtered = v.filter { it != '<' && it != '>' && it != '&' && it != '"' && it != '\'' }
                    nombreStr = filtered.take(100)
                },
                label = "Nombre gasolinera (opcional)",
                placeholder = "Ej: Repsol Madrid",
                keyboardType = KeyboardType.Text
            )

            Spacer(Modifier.height(8.dp))

            Button(
                onClick = {
                    val odo = odometroStr.trim().toIntOrNull()
                    val lit = litrosStr.trim().replace(",", ".").toFloatOrNull()
                    val pre = precioStr.trim().replace(",", ".").toDoubleOrNull()
                    when {
                        odo == null || odo <= 0 || odo > 9_999_999 ->
                            Unit
                        lit == null || lit < 1f || lit > 200f ->
                            Unit
                        pre == null || pre < 0.5 || pre > 5.0 ->
                            Unit
                        else -> viewModel.guardarRepostaje(
                            odometroActual   = odo,
                            litros           = lit,
                            precioLitro      = pre,
                            nombreGasolinera = nombreStr,
                            gasolineraId     = gasolineraId
                        )
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                colors = ButtonDefaults.buttonColors(containerColor = EkoGreen40)
            ) {
                Text(
                    "Guardar repostaje",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }

            // Resumen calculado en tiempo real
            val odoVal = odometroStr.toIntOrNull()
            val litVal = litrosStr.replace(",", ".").toFloatOrNull()
            val preVal = precioStr.replace(",", ".").toDoubleOrNull()
            if (odoVal != null && litVal != null && preVal != null && odoVal > 0 && litVal > 0 && preVal > 0) {
                val total = litVal * preVal
                Card(
                    colors = CardDefaults.cardColors(containerColor = EkoCard),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text("Resumen", fontWeight = FontWeight.Bold, color = Color.White, fontSize = 14.sp)
                        Text("Total: ${"%.2f".format(total)} €", color = Color(0xFF69F0AE))
                        if (ultimoOdo > 0 && odoVal > ultimoOdo) {
                            val km = odoVal - ultimoOdo
                            val consumoCalc = (litVal / km.toFloat()) * 100f
                            Text("Km desde ultimo repostaje: $km km", color = Color.LightGray, fontSize = 13.sp)
                            Text("Consumo calculado: ${"%.1f".format(consumoCalc)} L/100km", color = Color.LightGray, fontSize = 13.sp)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun EkoTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    placeholder: String,
    keyboardType: KeyboardType = KeyboardType.Text
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label, color = Color(0xFF80CBC4)) },
        placeholder = { Text(placeholder, color = Color.Gray) },
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
        singleLine = true,
        modifier = Modifier.fillMaxWidth(),
        colors = OutlinedTextFieldDefaults.colors(
            focusedTextColor       = Color.White,
            unfocusedTextColor     = Color.White,
            focusedBorderColor     = EkoGreen40,
            unfocusedBorderColor   = Color(0xFF2E7D32),
            focusedLabelColor      = EkoGreen40,
            unfocusedLabelColor    = Color(0xFF80CBC4)
        )
    )
}
