package com.catalabytes.ekopump.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.catalabytes.ekopump.data.brent.BrentPrice
import com.catalabytes.ekopump.data.brent.BrentRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class BrentViewModel @Inject constructor(
    private val repository: BrentRepository
) : ViewModel() {

    private val _brent = MutableStateFlow<BrentPrice?>(null)
    val brent: StateFlow<BrentPrice?> = _brent

    init { cargar() }

    fun cargar() {
        viewModelScope.launch {
            _brent.value = repository.getBrentPrice()
        }
    }
}
