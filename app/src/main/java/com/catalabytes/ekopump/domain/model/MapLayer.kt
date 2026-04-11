package com.catalabytes.ekopump.domain.model

import androidx.annotation.StringRes
import com.catalabytes.ekopump.R

enum class MapLayer(
    val emoji: String,
    @get:StringRes val labelRes: Int,
    @get:StringRes val descripcionRes: Int,
    val activo: Boolean,
    val url: String = "https://ekopump.es/roadmap"
) {
    GASOLINERAS("⛽", R.string.layer_gasolineras,  R.string.layer_gasolineras_desc,  true),
    ELECTRICO(  "⚡", R.string.layer_electrico,    R.string.layer_electrico_desc,    true),
    BICI(       "🚲", R.string.layer_bici,         R.string.layer_bici_desc,         false),
    BUS(        "🚌", R.string.layer_bus,          R.string.layer_bus_desc,          false),
    TREN(       "🚂", R.string.layer_tren,         R.string.layer_tren_desc,         false),
    PEATONAL(   "🚶", R.string.layer_peatonal,     R.string.layer_peatonal_desc,     false)
}
