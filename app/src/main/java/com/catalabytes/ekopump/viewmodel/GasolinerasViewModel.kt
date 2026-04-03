package com.catalabytes.ekopump.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.catalabytes.ekopump.data.location.LocationProvider
import com.catalabytes.ekopump.data.prefs.CalculadorPrefs
import com.catalabytes.ekopump.data.repository.Combustible
import com.catalabytes.ekopump.data.repository.GasolineraConDistancia
import com.catalabytes.ekopump.data.repository.GasolinerasRepository
import com.catalabytes.ekopump.ui.common.UiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class GasolinerasViewModel @Inject constructor(
    private val repository: GasolinerasRepository,
    private val locationProvider: LocationProvider,
    private val calculadorPrefs: CalculadorPrefs
) : ViewModel() {

    private val _uiState = MutableStateFlow<UiState<List<GasolineraConDistancia>>>(UiState.Loading)
    val uiState: StateFlow<UiState<List<GasolineraConDistancia>>> = _uiState

    private val _combustible = MutableStateFlow(Combustible.GASOLINA_95)
    val combustible: StateFlow<Combustible> = _combustible

    private val _userLat = MutableStateFlow(40.4168)
    val userLat: StateFlow<Double> = _userLat

    private val _userLon = MutableStateFlow(-3.7038)
    val userLon: StateFlow<Double> = _userLon

    val consumo: StateFlow<Float> = calculadorPrefs.consumo
        .stateIn(viewModelScope, SharingStarted.Eagerly, 7f)

    val litros: StateFlow<Float> = calculadorPrefs.litros
        .stateIn(viewModelScope, SharingStarted.Eagerly, 40f)

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
                val data = repository.getGasolinerasCercanas(_combustible.value)
                _uiState.value = UiState.Success(data)
            } catch (e: Exception) {
                _uiState.value = UiState.Error(e.message ?: "Error desconocido")
            }
        }
    }

    fun setCombustible(c: Combustible) {
        _combustible.value = c
        cargar()
    }

    fun setConsumo(v: Float) {
        viewModelScope.launch { calculadorPrefs.setConsumo(v) }
    }

    fun setLitros(v: Float) {
        viewModelScope.launch { calculadorPrefs.setLitros(v) }
    }
}
