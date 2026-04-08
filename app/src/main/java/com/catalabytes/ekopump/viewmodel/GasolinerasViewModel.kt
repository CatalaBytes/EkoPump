package com.catalabytes.ekopump.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.catalabytes.ekopump.data.location.LocationProvider
import com.catalabytes.ekopump.data.prefs.CalculadorPrefs
import com.catalabytes.ekopump.data.prefs.PriceHistoryPrefs
import com.catalabytes.ekopump.data.repository.Combustible
import com.catalabytes.ekopump.data.repository.GasolineraConDistancia
import com.catalabytes.ekopump.data.repository.GasolinerasRepository
import com.catalabytes.ekopump.domain.model.EnergyType
import com.catalabytes.ekopump.domain.model.TendenciaPrecio
import com.catalabytes.ekopump.domain.model.VehicleType
import com.catalabytes.ekopump.ui.common.UiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class GasolinerasViewModel @Inject constructor(
    private val repository: GasolinerasRepository,
    private val locationProvider: LocationProvider,
    private val calculadorPrefs: CalculadorPrefs,
    private val priceHistoryPrefs: PriceHistoryPrefs
) : ViewModel() {

    private val _uiState = MutableStateFlow<UiState<List<GasolineraConDistancia>>>(UiState.Loading)
    val uiState: StateFlow<UiState<List<GasolineraConDistancia>>> = _uiState

    private val _combustible = MutableStateFlow(Combustible.GASOLINA_95)
    val combustible: StateFlow<Combustible> = _combustible

    private val _userLat = MutableStateFlow(40.4168)
    val userLat: StateFlow<Double> = _userLat

    private val _userLon = MutableStateFlow(-3.7038)
    val userLon: StateFlow<Double> = _userLon

    private val _tendencias = MutableStateFlow<Map<String, TendenciaPrecio>>(emptyMap())
    val tendencias: StateFlow<Map<String, TendenciaPrecio>> = _tendencias

    val consumo: StateFlow<Float> = calculadorPrefs.consumo
        .stateIn(viewModelScope, SharingStarted.Eagerly, 7f)

    val litros: StateFlow<Float> = calculadorPrefs.litros
        .stateIn(viewModelScope, SharingStarted.Eagerly, 40f)

    val vehicleType: StateFlow<VehicleType> = calculadorPrefs.vehicleType
        .map { name ->
            try { VehicleType.valueOf(name) } catch (e: Exception) { VehicleType.TURISMO }
        }
        .stateIn(viewModelScope, SharingStarted.Eagerly, VehicleType.TURISMO)

    val energyType: StateFlow<EnergyType?> = calculadorPrefs.energyType
        .map { name ->
            if (name == null) null
            else try { EnergyType.valueOf(name) } catch (e: Exception) { null }
        }
        .stateIn(viewModelScope, SharingStarted.Eagerly, null)

    private val _radioKm = MutableStateFlow(10.0)
    val radioKm: StateFlow<Double> = _radioKm.asStateFlow()

    fun setRadioKm(km: Double) {
        _radioKm.value = km
        cargar()
    }

    init { cargar() }

    fun cargar() {
        viewModelScope.launch {
            _uiState.value = UiState.Loading
            try {
                val location = locationProvider.getLocation()
                location?.let {
                    _userLat.value = it.latitude
                    _userLon.value = it.longitude
                }
                val data = repository.getGasolinerasCercanas(_combustible.value, _radioKm.value)
                _uiState.value = UiState.Success(data)
                calcularTendencias(data)
            } catch (e: Exception) {
                _uiState.value = UiState.Error(e.message ?: "Error desconocido")
            }
        }
    }

    private fun calcularTendencias(lista: List<GasolineraConDistancia>) {
        val historial = priceHistoryPrefs.cargar()
        val UMBRAL = 0.002
        val nuevasTendencias = mutableMapOf<String, TendenciaPrecio>()
        val preciosActuales  = mutableMapOf<String, Double>()
        lista.forEach { item ->
            val id = item.gasolinera.id
            val precioActual = _combustible.value.precio(item.gasolinera) ?: return@forEach
            preciosActuales[id] = precioActual
            val precioAnterior = historial[id]
            nuevasTendencias[id] = when {
                precioAnterior == null                 -> TendenciaPrecio.ESTABLE
                precioActual > precioAnterior + UMBRAL -> TendenciaPrecio.SUBE
                precioActual < precioAnterior - UMBRAL -> TendenciaPrecio.BAJA
                else                                   -> TendenciaPrecio.ESTABLE
            }
        }
        _tendencias.value = nuevasTendencias
        priceHistoryPrefs.guardar(preciosActuales)
    }

    fun setCombustible(c: Combustible) { _combustible.value = c; cargar() }
    fun setConsumo(v: Float)     { viewModelScope.launch { calculadorPrefs.setConsumo(v) } }
    fun setLitros(v: Float)      { viewModelScope.launch { calculadorPrefs.setLitros(v) } }
    fun setVehicleType(tipo: VehicleType) {
        viewModelScope.launch { calculadorPrefs.setVehicleType(tipo.name) }
    }

    fun setEnergyType(tipo: EnergyType?) {
        viewModelScope.launch { calculadorPrefs.setEnergyType(tipo?.name) }
    }
}
