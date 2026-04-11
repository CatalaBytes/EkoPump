package com.catalabytes.ekopump.domain.model

import androidx.annotation.StringRes
import com.catalabytes.ekopump.R

enum class EnergyType(
    val emoji: String,
    @get:StringRes val labelRes: Int,
    @get:StringRes val descripcionRes: Int
) {
    GNC("\ud83d\udfe6", R.string.energy_gnc, R.string.energy_gnc_desc),
    GNL("\ud83d\udfe5", R.string.energy_gnl, R.string.energy_gnl_desc),
    ADBLUE("\ud83d\udd35", R.string.energy_adblue, R.string.energy_adblue_desc),
    EV("\u26a1", R.string.energy_ev, R.string.energy_ev_desc)
}
