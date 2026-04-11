package com.catalabytes.ekopump.data.ev

import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONArray

data class ChargePoint(
    val id: String,
    val nombre: String,
    val lat: Double,
    val lon: Double,
    val conectores: Int
)

object OpenChargeMapRepository {

    private val client = OkHttpClient()

    // API pública, sin clave (datos abiertos)
    private const val BASE_URL = "https://api.openchargemap.io/v3/poi/"

    fun fetchCercanos(lat: Double, lon: Double, radioKm: Int = 10): List<ChargePoint> {
        val url = "$BASE_URL?output=json&latitude=$lat&longitude=$lon" +
                "&distance=$radioKm&distanceunit=KM&maxresults=50&compact=true&verbose=false"
        return try {
            val request = Request.Builder().url(url).build()
            val body = client.newCall(request).execute().use { it.body?.string() ?: return emptyList() }
            val array = JSONArray(body)
            (0 until array.length()).mapNotNull { i ->
                val obj = array.getJSONObject(i)
                val addr = obj.optJSONObject("AddressInfo") ?: return@mapNotNull null
                val latP = addr.optDouble("Latitude", Double.NaN)
                val lonP = addr.optDouble("Longitude", Double.NaN)
                if (latP.isNaN() || lonP.isNaN()) return@mapNotNull null
                ChargePoint(
                    id = obj.optInt("ID", 0).toString(),
                    nombre = addr.optString("Title", "Punto de carga"),
                    lat = latP,
                    lon = lonP,
                    conectores = obj.optJSONArray("Connections")?.length() ?: 0
                )
            }
        } catch (e: Exception) {
            emptyList()
        }
    }
}
