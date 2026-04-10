package com.catalabytes.ekopump.ui.history

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.catalabytes.ekopump.data.local.entity.RefuelEntity
import com.catalabytes.ekopump.data.repository.RefuelRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.Calendar
import javax.inject.Inject

private fun startOfCurrentMonthMs(): Long {
    val cal = Calendar.getInstance()
    cal.set(Calendar.DAY_OF_MONTH, 1)
    cal.set(Calendar.HOUR_OF_DAY, 0)
    cal.set(Calendar.MINUTE, 0)
    cal.set(Calendar.SECOND, 0)
    cal.set(Calendar.MILLISECOND, 0)
    return cal.timeInMillis
}

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

    // Mes actual
    private val mesInicioMs = startOfCurrentMonthMs()

    val refuelsMes = repository.getRefuelsSince(mesInicioMs)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val totalGastadoMes = repository.getTotalSpentSince(mesInicioMs)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val totalLitrosMes = repository.getTotalLitersSince(mesInicioMs)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val avgConsumoRealMes = repository.getAvgConsumoRealSince(mesInicioMs)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val totalAhorroMes = repository.getTotalAhorroSince(mesInicioMs)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    fun addRefuel(refuel: RefuelEntity) {
        viewModelScope.launch { repository.addRefuel(refuel) }
    }

    fun deleteRefuel(refuel: RefuelEntity) {
        viewModelScope.launch { repository.deleteRefuel(refuel) }
    }
}
