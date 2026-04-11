package com.catalabytes.ekopump.data.prefs

import android.content.Context

object PuntoHabitual {
    private const val PREFS   = "ekopump_punto_habitual"
    private const val KEY_LAT = "lat"
    private const val KEY_LON = "lon"

    fun guardar(context: Context, lat: Double, lon: Double) {
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit()
            .putFloat(KEY_LAT, lat.toFloat())
            .putFloat(KEY_LON, lon.toFloat())
            .apply()
    }

    fun cargar(context: Context): Pair<Double, Double>? {
        val prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        if (!prefs.contains(KEY_LAT)) return null
        return Pair(
            prefs.getFloat(KEY_LAT, 0f).toDouble(),
            prefs.getFloat(KEY_LON, 0f).toDouble()
        )
    }

    fun limpiar(context: Context) {
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit().clear().apply()
    }

    fun existe(context: Context): Boolean =
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).contains(KEY_LAT)
}
