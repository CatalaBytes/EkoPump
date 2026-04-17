package com.catalabytes.ekopump.domain.model

import com.catalabytes.ekopump.data.repository.GasolineraConDistancia

sealed class MapPoint {
    data class Gasolinera(val item: GasolineraConDistancia) : MapPoint()
    data class Ev(val charger: EvCharger) : MapPoint()
}
