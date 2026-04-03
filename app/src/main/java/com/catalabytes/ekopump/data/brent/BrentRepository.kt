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
    suspend fun getBrentPrice(): BrentPrice? = withContext(Dispatchers.IO) {
        try {
            val request = Request.Builder()
                .url("https://query1.finance.yahoo.com/v8/finance/quote?symbols=BZ%3DF&fields=regularMarketPrice,regularMarketChange,regularMarketChangePercent")
                .header("User-Agent", "Mozilla/5.0")
                .build()
            val response = client.newCall(request).execute()
            val body = response.body?.string() ?: return@withContext null
            val result = JSONObject(body)
                .getJSONObject("quoteResponse")
                .getJSONArray("result")
                .getJSONObject(0)
            BrentPrice(
                precio = result.getDouble("regularMarketPrice"),
                variacion = result.getDouble("regularMarketChange"),
                variacionPct = result.getDouble("regularMarketChangePercent")
            )
        } catch (e: Exception) { null }
    }
}
