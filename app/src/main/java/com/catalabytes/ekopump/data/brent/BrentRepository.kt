package com.catalabytes.ekopump.data.brent

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import javax.inject.Inject
import javax.inject.Singleton

data class BrentPrice(
    val precio: Double,
    val variacion: Double,
    val variacionPct: Double
)

data class BrentHistorial(
    val fecha: String,
    val precio: Double
)

@Singleton
class BrentRepository @Inject constructor(
    private val client: OkHttpClient
) {
    private val API_KEY = "35acbd2745bb473eb497a6bf4ac16584"

    suspend fun getBrentPrice(): BrentPrice? = withContext(Dispatchers.IO) {
        try {
            val url = "https://www.alphavantage.co/query?function=BRENT&interval=daily&apikey=$API_KEY"
            val body = client.newCall(Request.Builder().url(url)
                .header("User-Agent", "EkoPump/1.0").build())
                .execute().body?.string() ?: return@withContext null
            android.util.Log.d("BrentRepository", "raw: ${body.take(200)}")
            val data = JSONObject(body).optJSONArray("data") ?: return@withContext null
            val hoy  = data.getJSONObject(0).optString("value").toDoubleOrNull() ?: return@withContext null
            val ayer = data.optJSONObject(1)?.optString("value")?.toDoubleOrNull() ?: hoy
            val variacion = hoy - ayer
            val variacionPct = if (ayer != 0.0) (variacion / ayer) * 100.0 else 0.0
            android.util.Log.d("BrentRepository", "OK: $hoy USD")
            BrentPrice(hoy, variacion, variacionPct)
        } catch (e: Exception) {
            android.util.Log.e("BrentRepository", "error: ${e.message}", e)
            null
        }
    }

    suspend fun getHistorial(dias: Int = 30): List<BrentHistorial> = withContext(Dispatchers.IO) {
        try {
            val url = "https://www.alphavantage.co/query?function=BRENT&interval=daily&apikey=$API_KEY"
            val body = client.newCall(Request.Builder().url(url)
                .header("User-Agent", "EkoPump/1.0").build())
                .execute().body?.string() ?: return@withContext emptyList()
            val data = JSONObject(body).optJSONArray("data") ?: return@withContext emptyList()
            (0 until minOf(dias, data.length()))
                .mapNotNull { i ->
                    val obj = data.getJSONObject(i)
                    val fecha = obj.optString("date")
                    val precio = obj.optString("value").toDoubleOrNull()
                    if (fecha.isNotEmpty() && precio != null) BrentHistorial(fecha, precio) else null
                }
                .reversed() // cronológico ascendente para el gráfico
        } catch (e: Exception) {
            android.util.Log.e("BrentRepository", "historial error: ${e.message}", e)
            emptyList()
        }
    }
}
