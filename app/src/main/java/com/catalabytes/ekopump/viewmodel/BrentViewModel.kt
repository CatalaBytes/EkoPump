package com.catalabytes.ekopump.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.catalabytes.ekopump.data.brent.BrentHistorial
import com.catalabytes.ekopump.data.brent.BrentPrice
import com.catalabytes.ekopump.data.brent.BrentRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class BrentViewModel @Inject constructor(
    private val repository: BrentRepository
) : ViewModel() {

    private val _brent = MutableStateFlow<BrentPrice?>(null)
    val brent: StateFlow<BrentPrice?> = _brent

    private val _historial = MutableStateFlow<List<BrentHistorial>>(emptyList())
    val historial: StateFlow<List<BrentHistorial>> = _historial

    private val _lastRefreshMs = MutableStateFlow(0L)
    val lastRefreshMs: StateFlow<Long> = _lastRefreshMs.asStateFlow()

    init {
        cargar()
        viewModelScope.launch {
            while (true) {
                delay(30 * 60 * 1000L)
                cargar()
            }
        }
    }

    fun cargar() {
        viewModelScope.launch {
            val precio = repository.getBrentPrice()
            if (precio != null) {
                _brent.value = precio
                _lastRefreshMs.value = System.currentTimeMillis()
            }
        }
        viewModelScope.launch {
            val hist = repository.getHistorial(30)
            if (hist.isNotEmpty()) _historial.value = hist
        }
    }
}
