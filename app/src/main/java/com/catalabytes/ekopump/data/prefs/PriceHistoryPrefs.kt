package com.catalabytes.ekopump.data.prefs

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import org.json.JSONObject
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PriceHistoryPrefs @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val prefs = context.getSharedPreferences("price_history", Context.MODE_PRIVATE)
    private val KEY = "precios_json"

    fun cargar(): Map<String, Double> {
        val json = prefs.getString(KEY, null) ?: return emptyMap()
        return try {
            val obj = JSONObject(json)
            obj.keys().asSequence().associateWith { obj.getDouble(it) }
        } catch (e: Exception) { emptyMap() }
    }

    fun guardar(precios: Map<String, Double>) {
        val obj = JSONObject()
        precios.forEach { (id, precio) -> obj.put(id, precio) }
        prefs.edit().putString(KEY, obj.toString()).apply()
    }
}
