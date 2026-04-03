package com.catalabytes.ekopump.data.brent

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
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
    suspend fun getBrentPrice(): BrentPrice? = withContext(Dispatchers.IO) {
        // Intentamos dos fuentes por orden
        fetchStooq() ?: fetchYahooV7()
    }

    private fun fetchStooq(): BrentPrice? = try {
        val req = Request.Builder()
            .url("https://stooq.com/q/l/?s=bz.f&f=sd2t2ohlcv&e=csv")
            .header("User-Agent", "Mozilla/5.0 (Linux; Android 14)")
            .header("Accept", "text/csv,*/*")
            .build()
        val body = client.newCall(req).execute().body?.string() ?: return null
        val cols = body.trim().lines().getOrNull(1)?.split(",") ?: return null
        val close = cols.getOrNull(6)?.toDoubleOrNull() ?: return null
        val open  = cols.getOrNull(3)?.toDoubleOrNull() ?: close
        val var_  = close - open
        android.util.Log.d("BrentRepository", "stooq OK: $close")
        BrentPrice(close, var_, if (open != 0.0) (var_ / open) * 100 else 0.0)
    } catch (e: Exception) {
        android.util.Log.w("BrentRepository", "stooq fallo: ${e.message}")
        null
    }

    private fun fetchYahooV7(): BrentPrice? = try {
        val req = Request.Builder()
            .url("https://query2.finance.yahoo.com/v8/finance/quote?symbols=BZ%3DF")
            .header("User-Agent", "Mozilla/5.0 (Linux; Android 14)")
            .header("Accept", "application/json")
            .build()
        val body = client.newCall(req).execute().body?.string() ?: return null
        val r = org.json.JSONObject(body)
            .getJSONObject("quoteResponse")
            .getJSONArray("result")
            .getJSONObject(0)
        val precio = r.getDouble("regularMarketPrice")
        val variacion = r.getDouble("regularMarketChange")
        val variacionPct = r.getDouble("regularMarketChangePercent")
        android.util.Log.d("BrentRepository", "Yahoo OK: $precio")
        BrentPrice(precio, variacion, variacionPct)
    } catch (e: Exception) {
        android.util.Log.w("BrentRepository", "Yahoo fallo: ${e.message}")
        null
    }
}
