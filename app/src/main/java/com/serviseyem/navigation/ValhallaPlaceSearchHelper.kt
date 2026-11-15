package com.serviseyem.navigation

import com.serviseyem.data.ProviderEntity
import com.serviseyem.data.StoreEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder

/**
 * مساعد البحث عن العناوين والأماكن محلياً وعبر Nominatim OpenStreetMap.
 */
internal object ValhallaPlaceSearchHelper {

    suspend fun searchAddressAndPlaces(
        query: String,
        providers: List<ProviderEntity>,
        stores: List<StoreEntity>,
        userLat: Double,
        userLng: Double,
        onError: (String?) -> Unit
    ): List<RealValhallaNavigator.SearchAddressResult> = withContext(Dispatchers.IO) {
        onError(null)
        val q = query.trim()
        if (q.length < 2) return@withContext emptyList()

        val results = mutableListOf<RealValhallaNavigator.SearchAddressResult>()

        providers.filter {
            it.latitude != 0.0 && it.longitude != 0.0 &&
                (it.name.contains(q, ignoreCase = true) || it.area.contains(q, ignoreCase = true))
        }.take(5).forEach {
            results.add(
                RealValhallaNavigator.SearchAddressResult(
                    displayName = "${it.name} - ${it.area}",
                    shortName = it.name,
                    latitude = it.latitude,
                    longitude = it.longitude,
                    category = "provider",
                    icon = "👷"
                )
            )
        }
        stores.filter {
            it.latitude != 0.0 && it.longitude != 0.0 && it.name.contains(q, ignoreCase = true)
        }.take(5).forEach {
            results.add(
                RealValhallaNavigator.SearchAddressResult(
                    displayName = it.name,
                    shortName = it.name,
                    latitude = it.latitude,
                    longitude = it.longitude,
                    category = "store",
                    icon = "🏪"
                )
            )
        }

        var conn: HttpURLConnection? = null
        try {
            val encoded = URLEncoder.encode(q, "UTF-8")
            val viewboxParam = if (userLat != 0.0 && userLng != 0.0 && !userLat.isNaN() && !userLng.isNaN()) {
                "&viewbox=${userLng - 0.5},${userLat + 0.5},${userLng + 0.5},${userLat - 0.5}"
            } else ""
            val url = URL("https://nominatim.openstreetmap.org/search?format=json&q=$encoded&countrycodes=ye&limit=10&addressdetails=1&accept-language=ar$viewboxParam")
            conn = (url.openConnection() as HttpURLConnection).apply {
                connectTimeout = 7000
                readTimeout = 7000
                setRequestProperty("User-Agent", "AlYemenServicesApp/1.0 (Android)")
            }
            if (conn.responseCode in 200..299) {
                val body = conn.inputStream.bufferedReader().use { it.readText() }
                val arr = JSONArray(body)
                for (i in 0 until arr.length()) {
                    val obj = arr.getJSONObject(i)
                    val displayName = obj.optString("display_name", "")
                    val lat = obj.optString("lat", "0").toDoubleOrNull() ?: obj.optDouble("lat", 0.0)
                    val lon = obj.optString("lon", "0").toDoubleOrNull() ?: obj.optDouble("lon", 0.0)
                    if (lat != 0.0 && lon != 0.0 && !lat.isNaN() && !lon.isNaN()) {
                        val short = displayName.split(",").firstOrNull()?.trim().orEmpty().ifBlank { displayName }
                        val type = obj.optString("type", "place")
                        val icon = when (type) {
                            "city", "town" -> "🏙️"
                            "village", "hamlet" -> "🏡"
                            "road", "residential", "primary", "secondary" -> "🛣️"
                            "hospital", "clinic" -> "🏥"
                            "school", "university" -> "🎓"
                            "mosque", "place_of_worship" -> "🕌"
                            else -> "📍"
                        }
                        results.add(
                            RealValhallaNavigator.SearchAddressResult(
                                displayName = displayName,
                                shortName = short,
                                latitude = lat,
                                longitude = lon,
                                category = type,
                                icon = icon
                            )
                        )
                    }
                }
            } else {
                onError("تعذر البحث عبر الإنترنت (رمز ${conn.responseCode})")
            }
        } catch (e: Exception) {
            onError("تعذر الاتصال بخدمة البحث: ${e.localizedMessage ?: "لا يوجد اتصال"}")
        } finally {
            conn?.disconnect()
        }

        results
    }
}
