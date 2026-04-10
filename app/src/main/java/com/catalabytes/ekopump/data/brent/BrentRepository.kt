package com.catalabytes.ekopump.data.brent

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone
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

private const val URL = "https://query1.finance.yahoo.com/v8/finance/chart/BZ=F?interval=1d&range=60d"
private const val UA  = "Mozilla/5.0 (Linux; Android 13) AppleWebKit/537.36 Chrome/120 Safari/537.36"

@Singleton
class BrentRepository @Inject constructor(
    private val client: OkHttpClient
) {
    // Cacheamos la respuesta en memoria para no hacer 2 llamadas HTTP por cargar()
    private var cachedBody: String? = null
    private var cachedBodyMs: Long = 0L
    private val CACHE_TTL_MS = 5 * 60 * 1000L // 5 min de caché interna

    private suspend fun fetchRaw(): String? = withContext(Dispatchers.IO) {
        val ahora = System.currentTimeMillis()
        if (cachedBody != null && (ahora - cachedBodyMs) < CACHE_TTL_MS) {
            return@withContext cachedBody
        }
        try {
            val body = client.newCall(
                Request.Builder().url(URL)
                    .header("User-Agent", UA)
                    .header("Accept", "application/json")
                    .build()
            ).execute().body?.string()
            if (body != null) {
                cachedBody   = body
                cachedBodyMs = ahora
            }
            body
        } catch (e: Exception) {
            android.util.Log.e("BrentRepository", "fetchRaw error: ${e.message}")
            null
        }
    }

    suspend fun getBrentPrice(): BrentPrice? = withContext(Dispatchers.IO) {
        try {
            val body   = fetchRaw() ?: return@withContext null
            val meta   = JSONObject(body)
                .getJSONObject("chart")
                .getJSONArray("result")
                .getJSONObject(0)
                .getJSONObject("meta")

            val precio         = meta.getDouble("regularMarketPrice")
            val cierrePrevio   = meta.optDouble("previousClose").takeIf { !it.isNaN() }
                ?: meta.optDouble("chartPreviousClose").takeIf { !it.isNaN() }
                ?: precio
            val variacion      = precio - cierrePrevio
            val variacionPct   = if (cierrePrevio != 0.0) (variacion / cierrePrevio) * 100.0 else 0.0

            android.util.Log.d("BrentRepository", "precio=$precio var=$variacion pct=$variacionPct")
            BrentPrice(precio, variacion, variacionPct)
        } catch (e: Exception) {
            android.util.Log.e("BrentRepository", "getBrentPrice error: ${e.message}", e)
            null
        }
    }

    suspend fun getHistorial(dias: Int = 30): List<BrentHistorial> = withContext(Dispatchers.IO) {
        try {
            val body    = fetchRaw() ?: return@withContext emptyList()
            val result  = JSONObject(body)
                .getJSONObject("chart")
                .getJSONArray("result")
                .getJSONObject(0)

            val timestamps  = result.getJSONArray("timestamp")
            val closes      = result
                .getJSONObject("indicators")
                .getJSONArray("quote")
                .getJSONObject(0)
                .getJSONArray("close")

            val sdf = SimpleDateFormat("dd MMM", Locale("es", "ES")).apply {
                timeZone = TimeZone.getTimeZone("UTC")
            }

            val puntos = mutableListOf<BrentHistorial>()
            for (i in 0 until timestamps.length()) {
                val ts     = timestamps.getLong(i)
                val precio = if (closes.isNull(i)) null else closes.optDouble(i).takeIf { !it.isNaN() }
                if (precio != null) {
                    puntos.add(BrentHistorial(sdf.format(Date(ts * 1000L)), precio))
                }
            }
            // Devolver los últimos `dias` puntos en orden cronológico
            puntos.takeLast(dias)
        } catch (e: Exception) {
            android.util.Log.e("BrentRepository", "getHistorial error: ${e.message}", e)
            emptyList()
        }
    }
}
