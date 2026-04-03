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

@Singleton
class BrentRepository @Inject constructor(
    private val client: OkHttpClient
) {
    private val API_KEY = "6VBNQA6LSC9R79DQ"

    suspend fun getBrentPrice(): BrentPrice? = withContext(Dispatchers.IO) {
        try {
            val url = "https://www.alphavantage.co/query?function=BRENT&interval=daily&apikey=$API_KEY"
            val req = Request.Builder()
                .url(url)
                .header("User-Agent", "EkoPump/1.0")
                .build()
            val body = client.newCall(req).execute().body?.string() ?: return@withContext null
            android.util.Log.d("BrentRepository", "raw: ${body.take(300)}")

            val json = JSONObject(body)
            val data = json.optJSONArray("data") ?: return@withContext null

            // Últimos dos días para calcular variación
            val hoy = data.getJSONObject(0).optString("value").toDoubleOrNull() ?: return@withContext null
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
}
