package com.catalabytes.ekopump.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.catalabytes.ekopump.data.local.entity.RefuelEntity
import com.catalabytes.ekopump.data.prefs.CalculadorPrefs
import com.catalabytes.ekopump.data.prefs.OdometroPrefs
import com.catalabytes.ekopump.data.repository.RefuelRepository
import com.catalabytes.ekopump.domain.ConsumoCalculator
import com.catalabytes.ekopump.notifications.AutonomiaChecker
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class GuardarResult {
    object Idle : GuardarResult()
    object Guardado : GuardarResult()
    data class Advertencia(val mensaje: String) : GuardarResult()
    data class Error(val mensaje: String) : GuardarResult()
}

@HiltViewModel
class RegistrarRepostajeViewModel @Inject constructor(
    private val repository: RefuelRepository,
    private val odometroPrefs: OdometroPrefs,
    private val calculadorPrefs: CalculadorPrefs,
    private val consumoCalculator: ConsumoCalculator,
    private val autonomiaChecker: AutonomiaChecker
) : ViewModel() {

    val ultimoOdometroKm: StateFlow<Int> = odometroPrefs.ultimoOdometroKm
        .stateIn(viewModelScope, SharingStarted.Eagerly, 0)

    val litrosPrefill: StateFlow<Float> = calculadorPrefs.litros
        .stateIn(viewModelScope, SharingStarted.Eagerly, 40f)

    val consumoActual: StateFlow<Float> = calculadorPrefs.consumo
        .stateIn(viewModelScope, SharingStarted.Eagerly, 7f)

    private val _resultado = MutableStateFlow<GuardarResult>(GuardarResult.Idle)
    val resultado: StateFlow<GuardarResult> = _resultado.asStateFlow()

    fun guardarRepostaje(
        odometroActual: Int,
        litros: Float,
        precioLitro: Double,
        nombreGasolinera: String,
        gasolineraId: String? = null,
        ahorroEstimado: Float? = null
    ) {
        viewModelScope.launch {
            val ultimoOdo = odometroPrefs.ultimoOdometroKm.first()
            val kmRecorridos = if (ultimoOdo > 0) (odometroActual - ultimoOdo) else 0
            val consumoAnterior = calculadorPrefs.consumo.first()

            // Calcular consumo real solo si hay km previos válidos
            val consumoReal: Float? = if (kmRecorridos > 0 && consumoCalculator.validarDatos(litros, kmRecorridos)) {
                consumoCalculator.calcularConsumoReal(litros, kmRecorridos)
            } else null

            // Validar si el consumo real parece sospechoso
            val advertencia = when {
                consumoReal != null && consumoReal > 30f ->
                    "Consumo calculado (${"%0.1f".format(consumoReal)} L/100km) parece alto. Revisa el odómetro."
                consumoReal != null && consumoReal < 2f  ->
                    "Consumo calculado (${"%0.1f".format(consumoReal)} L/100km) parece muy bajo. Revisa el odómetro."
                else -> null
            }

            val totalCost = litros * precioLitro

            val entity = RefuelEntity(
                stationName     = nombreGasolinera.ifBlank { "Gasolinera" },
                stationAddress  = "",
                fuelType        = "Gasolina",
                pricePerLiter   = precioLitro,
                liters          = litros.toDouble(),
                totalCost       = totalCost,
                savedAmount     = ahorroEstimado?.toDouble() ?: 0.0,
                latitude        = 0.0,
                longitude       = 0.0,
                odometroKm      = odometroActual,
                consumoRealL100 = consumoReal,
                ahorroEstimadoEur = ahorroEstimado,
                gasolineraId    = gasolineraId,
                gasolineraName  = nombreGasolinera.ifBlank { null }
            )

            repository.addRefuel(entity)
            odometroPrefs.guardar(odometroActual)

            // Media móvil: 70% anterior + 30% nuevo
            if (consumoReal != null) {
                val nuevoConsumo = 0.7f * consumoAnterior + 0.3f * consumoReal
                calculadorPrefs.setConsumo(nuevoConsumo)
                // Alerta de autonomía con litros recién repostados
                autonomiaChecker.check(litros, nuevoConsumo)
            }

            _resultado.value = if (advertencia != null) {
                GuardarResult.Advertencia(advertencia)
            } else {
                GuardarResult.Guardado
            }
        }
    }

    fun resetResultado() { _resultado.value = GuardarResult.Idle }
}
