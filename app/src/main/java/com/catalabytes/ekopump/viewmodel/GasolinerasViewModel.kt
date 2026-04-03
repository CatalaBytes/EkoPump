package com.catalabytes.ekopump.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.catalabytes.ekopump.data.repository.Combustible
import com.catalabytes.ekopump.data.repository.GasolineraConDistancia
import com.catalabytes.ekopump.data.repository.GasolinerasRepository
import com.catalabytes.ekopump.ui.common.UiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class GasolinerasViewModel @Inject constructor(
    private val repository: GasolinerasRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<UiState<List<GasolineraConDistancia>>>(UiState.Loading)
    val uiState: StateFlow<UiState<List<GasolineraConDistancia>>> = _uiState

    private val _combustible = MutableStateFlow(Combustible.GASOLINA_95)
    val combustible: StateFlow<Combustible> = _combustible

    init { cargar() }

    fun cargar() {
        viewModelScope.launch {
            _uiState.value = UiState.Loading
            try {
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
}
