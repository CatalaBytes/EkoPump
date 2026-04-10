package com.catalabytes.ekopump.data.prefs

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import org.json.JSONObject
import javax.inject.Inject
import javax.inject.Singleton

data class PriceAlert(
    val gasolineraId: String,
    val nombre: String,
    val combustible: String,
    val precioUmbral: Double
)

@Singleton
class PriceAlertPrefs @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val prefs = context.getSharedPreferences("price_alerts", Context.MODE_PRIVATE)
    private val KEY = "alerts_json"

    fun savePriceAlert(gasolineraId: String, nombre: String, combustible: String, precioUmbral: Double) {
        val alerts = getPriceAlerts().toMutableMap()
        alerts[gasolineraId] = PriceAlert(gasolineraId, nombre, combustible, precioUmbral)
        persist(alerts)
    }

    fun getPriceAlerts(): Map<String, PriceAlert> {
        val json = prefs.getString(KEY, null) ?: return emptyMap()
        return try {
            val obj = JSONObject(json)
            buildMap {
                obj.keys().forEach { key ->
                    val a = obj.getJSONObject(key)
                    put(key, PriceAlert(
                        gasolineraId = a.getString("gasolineraId"),
                        nombre       = a.getString("nombre"),
                        combustible  = a.getString("combustible"),
                        precioUmbral = a.getDouble("precioUmbral")
                    ))
                }
            }
        } catch (e: Exception) { emptyMap() }
    }

    fun removePriceAlert(gasolineraId: String) {
        val alerts = getPriceAlerts().toMutableMap()
        alerts.remove(gasolineraId)
        persist(alerts)
    }

    fun getAlertIds(): Set<String> = getPriceAlerts().keys

    private fun persist(alerts: Map<String, PriceAlert>) {
        val obj = JSONObject()
        alerts.values.forEach { a ->
            obj.put(a.gasolineraId, JSONObject().apply {
                put("gasolineraId", a.gasolineraId)
                put("nombre",       a.nombre)
                put("combustible",  a.combustible)
                put("precioUmbral", a.precioUmbral)
            })
        }
        prefs.edit().putString(KEY, obj.toString()).apply()
    }
}
