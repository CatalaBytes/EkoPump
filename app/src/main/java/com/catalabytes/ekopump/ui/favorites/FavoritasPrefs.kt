package com.catalabytes.ekopump.ui.favorites

import android.content.Context

/**
 * Favoritas almacenadas en SharedPreferences como Set<String> de IDs.
 * Sin migración Room — rápido y suficiente para esta feature.
 */
object FavoritasPrefs {

    private const val PREFS_NAME = "ekopump_favoritas"
    private const val KEY_IDS    = "favoritas_ids"

    private fun getPrefs(context: Context) =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    fun getIds(context: Context): Set<String> =
        getPrefs(context).getStringSet(KEY_IDS, emptySet()) ?: emptySet()

    fun esFavorita(context: Context, id: String): Boolean =
        getIds(context).contains(id)

    /** Alterna favorita. Devuelve el nuevo estado (true = es favorita). */
    fun toggleFavorita(context: Context, id: String): Boolean {
        val prefs = getPrefs(context)
        val actual = getIds(context).toMutableSet()
        val nuevo = if (actual.contains(id)) {
            actual.remove(id)
            false
        } else {
            actual.add(id)
            true
        }
        prefs.edit().putStringSet(KEY_IDS, actual).commit()
        return nuevo
    }
}
