package com.catalabytes.ekopump.ui.history

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.catalabytes.ekopump.data.local.entity.RefuelEntity
import com.catalabytes.ekopump.data.repository.RefuelRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HistoryViewModel @Inject constructor(
    private val repository: RefuelRepository
) : ViewModel() {

    val refuels = repository.allRefuels
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val totalSpent = repository.totalSpent
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    val totalSaved = repository.totalSaved
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    val totalLiters = repository.totalLiters
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    val refuelCount = repository.refuelCount
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    fun addRefuel(refuel: RefuelEntity) {
        viewModelScope.launch { repository.addRefuel(refuel) }
    }

    fun deleteRefuel(refuel: RefuelEntity) {
        viewModelScope.launch { repository.deleteRefuel(refuel) }
    }
}
