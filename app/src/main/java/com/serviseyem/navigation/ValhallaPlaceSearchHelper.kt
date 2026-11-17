package com.serviseyem.navigation

import android.util.Log
import com.serviseyem.data.ProviderEntity
import com.serviseyem.data.StoreEntity
import com.serviseyem.utils.ArabicNormalizer
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder
import java.util.Locale

/**
 * مساعد البحث السريع والذكي عن العناوين والشوارع والمعالم والخدمات (محلياً + Nominatim + Photon + Overpass POIs).
 */
internal object ValhallaPlaceSearchHelper {

    private const val TAG = "PlaceSearchHelper"

    private fun isValidLatLng(lat: Double, lon: Double): Boolean =
        !lat.isNaN() && !lon.isNaN() && lat in 11.5..20.0 && lon in 41.0..55.0

    private fun iconForOsmType(type: String, category: String = "", name: String = ""): String {
        val t = "$type $category $name".lowercase(Locale.US)
        return when {
            t.contains("bureau_de_change") || t.contains("bank") || t.contains("صراف") || t.contains("كريمي") || t.contains("عمقي") -> "💱"
            t.contains("mosque") || t.contains("place_of_worship") || t.contains("مسجد") || t.contains("جامع") -> "🕌"
            t.contains("school") || t.contains("college") || t.contains("university") || t.contains("مدرس") || t.contains("جامع") -> "🎓"
            t.contains("hospital") || t.contains("clinic") || t.contains("pharmacy") || t.contains("مستشف") || t.contains("صيدلي") -> "🏥"
            t.contains("roundabout") || t.contains("جول") || t.contains("دوار") -> "🔄"
            t.contains("fuel") || t.contains("وقود") || t.contains("بترول") -> "⛽"
            t.contains("city") || t.contains("town") -> "🏙️"
            t.contains("village") || t.contains("hamlet") -> "🏡"
            t.contains("road") || t.contains("street") || t.contains("residential") || t.contains("primary") || t.contains("secondary") || t.contains("tertiary") || t.contains("شارع") -> "🛣️"
            else -> "📍"
        }
    }

    private fun fetchNominatim(q: String, anchorLat: Double, anchorLng: Double): List<RealValhallaNavigator.SearchAddressResult> {
        var conn: HttpURLConnection? = null
        return try {
            val encoded = URLEncoder.encode(q, "UTF-8")
            val viewboxParam = if (isValidLatLng(anchorLat, anchorLng)) {
                "&viewbox=${anchorLng - 0.6},${anchorLat + 0.6},${anchorLng + 0.6},${anchorLat - 0.6}"
            } else ""
            val url = URL("https://nominatim.openstreetmap.org/search?format=json&q=$encoded&countrycodes=ye&limit=15&addressdetails=1&accept-language=ar$viewboxParam")
            conn = (url.openConnection() as HttpURLConnection).apply {
                connectTimeout = 4500
                readTimeout = 4500
                setRequestProperty("User-Agent", "AlYemenServicesApp/1.2 (Android)")
            }
            if (conn.responseCode !in 200..299) return emptyList()
            val body = conn.inputStream.bufferedReader().use { it.readText() }
            val arr = JSONArray(body)
            val out = mutableListOf<RealValhallaNavigator.SearchAddressResult>()
            for (i in 0 until arr.length()) {
                val obj = arr.getJSONObject(i)
                val displayName = obj.optString("display_name", "")
                val lat = obj.optString("lat", "0").toDoubleOrNull() ?: obj.optDouble("lat", 0.0)
                val lon = obj.optString("lon", "0").toDoubleOrNull() ?: obj.optDouble("lon", 0.0)
                if (lat != 0.0 && lon != 0.0 && !lat.isNaN() && !lon.isNaN()) {
                    val short = displayName.split(",").firstOrNull()?.trim().orEmpty().ifBlank { displayName }
                    val type = obj.optString("type", "place")
                    out.add(
                        RealValhallaNavigator.SearchAddressResult(
                            displayName = displayName,
                            shortName = short,
                            latitude = lat,
                            longitude = lon,
                            category = type,
                            icon = iconForOsmType(type, name = short)
                        )
                    )
                }
            }
            out
        } catch (e: Exception) {
            Log.d(TAG, "Nominatim search error: ${e.message}")
            emptyList()
        } finally {
            conn?.disconnect()
        }
    }

    private fun fetchPhoton(q: String, anchorLat: Double, anchorLng: Double): List<RealValhallaNavigator.SearchAddressResult> {
        var conn: HttpURLConnection? = null
        return try {
            val encoded = URLEncoder.encode(q, "UTF-8")
            val latBias = if (isValidLatLng(anchorLat, anchorLng)) anchorLat else 15.3547
            val lonBias = if (isValidLatLng(anchorLat, anchorLng)) anchorLng else 44.2067
            val url = URL("https://photon.komoot.io/api/?q=$encoded&lat=$latBias&lon=$lonBias&limit=12&bbox=41.5,12.0,54.6,19.1")
            conn = (url.openConnection() as HttpURLConnection).apply {
                connectTimeout = 4000
                readTimeout = 4000
                setRequestProperty("User-Agent", "AlYemenServicesApp/1.2 (Android)")
            }
            if (conn.responseCode !in 200..299) return emptyList()
            val body = conn.inputStream.bufferedReader().use { it.readText() }
            val features = JSONObject(body).optJSONArray("features") ?: return emptyList()
            val out = mutableListOf<RealValhallaNavigator.SearchAddressResult>()
            for (i in 0 until features.length()) {
                val feat = features.optJSONObject(i) ?: continue
                val coords = feat.optJSONObject("geometry")?.optJSONArray("coordinates") ?: continue
                if (coords.length() < 2) continue
                val lon = coords.optDouble(0, 0.0)
                val lat = coords.optDouble(1, 0.0)
                if (!isValidLatLng(lat, lon)) continue
                val props = feat.optJSONObject("properties") ?: continue
                val name = props.optString("name", "").ifBlank { props.optString("street", "") }
                if (name.isBlank()) continue
                val city = props.optString("city", "").ifBlank { props.optString("state", "") }
                val district = props.optString("district", "")
                val osmValue = props.optString("osm_value", "place")
                val displayParts = listOfNotNull(
                    name.takeIf { it.isNotBlank() },
                    district.takeIf { it.isNotBlank() && it != name },
                    city.takeIf { it.isNotBlank() && it != name }
                )
                out.add(
                    RealValhallaNavigator.SearchAddressResult(
                        displayName = displayParts.joinToString(" — "),
                        shortName = name,
                        latitude = lat,
                        longitude = lon,
                        category = osmValue,
                        icon = iconForOsmType(osmValue, name = name)
                    )
                )
            }
            out
        } catch (e: Exception) {
            Log.d(TAG, "Photon search error: ${e.message}")
            emptyList()
        } finally {
            conn?.disconnect()
        }
    }

    private fun buildOverpassCategoryFilter(normQuery: String): String? {
        return when {
            normQuery.contains("صراف") || normQuery.contains("حوال") || normQuery.contains("بنك") || normQuery.contains("مصرف") || normQuery.contains("كريمي") || normQuery.contains("عمقي") ->
                """node["amenity"~"bureau_de_change|bank|atm"]"""
            normQuery.contains("جامع") || normQuery.contains("مسجد") || normQuery.contains("مصلى") ->
                """nwr["amenity"="place_of_worship"]"""
            normQuery.contains("مدرس") || normQuery.contains("ثانوي") || normQuery.contains("معهد") ->
                """nwr["amenity"~"school|college|university"]"""
            normQuery.contains("جول") || normQuery.contains("دوار") ->
                """nwr["junction"="roundabout"]"""
            normQuery.contains("مستشف") || normQuery.contains("مستوصف") || normQuery.contains("صيدلي") || normQuery.contains("عياد") ->
                """nwr["amenity"~"hospital|clinic|pharmacy"]"""
            normQuery.contains("محط") || normQuery.contains("بترول") || normQuery.contains("وقود") ->
                """nwr["amenity"="fuel"]"""
            else -> null
        }
    }

    private fun fetchOverpassCategoryPois(
        rawQuery: String,
        normQuery: String,
        anchorLat: Double,
        anchorLng: Double
    ): List<RealValhallaNavigator.SearchAddressResult> {
        val filter = buildOverpassCategoryFilter(normQuery) ?: return emptyList()
        val lat = if (isValidLatLng(anchorLat, anchorLng)) anchorLat else 15.3547
        val lng = if (isValidLatLng(anchorLat, anchorLng)) anchorLng else 44.2067
        var conn: HttpURLConnection? = null
        return try {
            val ql = "[out:json][timeout:4];(${filter}(around:12000,$lat,$lng););out center 15;"
            val url = URL("https://overpass-api.de/api/interpreter?data=" + URLEncoder.encode(ql, "UTF-8"))
            conn = (url.openConnection() as HttpURLConnection).apply {
                connectTimeout = 4500
                readTimeout = 4500
                setRequestProperty("User-Agent", "AlYemenServicesApp/1.2 (Android)")
            }
            if (conn.responseCode !in 200..299) return emptyList()
            val body = conn.inputStream.bufferedReader().use { it.readText() }
            val elements = JSONObject(body).optJSONArray("elements") ?: return emptyList()
            val out = mutableListOf<RealValhallaNavigator.SearchAddressResult>()
            val tokens = normQuery.split(' ').filter { it.length >= 3 }
            for (i in 0 until elements.length()) {
                val el = elements.optJSONObject(i) ?: continue
                val center = el.optJSONObject("center")
                val pLat = center?.optDouble("lat") ?: el.optDouble("lat", Double.NaN)
                val pLon = center?.optDouble("lon") ?: el.optDouble("lon", Double.NaN)
                if (pLat.isNaN() || pLon.isNaN() || !isValidLatLng(pLat, pLon)) continue
                val tags = el.optJSONObject("tags") ?: continue
                val nameAr = tags.optString("name:ar", "").ifBlank { tags.optString("name", "") }
                val fallbackLabel = when {
                    tags.optString("junction") == "roundabout" -> "جولة / تقاطع مروري"
                    tags.optString("amenity") == "bureau_de_change" -> "مركز صرافة وتحويلات"
                    tags.optString("amenity") == "bank" -> "مصرف / بنك"
                    tags.optString("amenity") == "place_of_worship" -> "مسجد / جامع"
                    tags.optString("amenity") == "school" -> "مدرسة"
                    tags.optString("amenity") == "fuel" -> "محطة وقود"
                    else -> ""
                }
                val title = nameAr.ifBlank { fallbackLabel }
                if (title.isBlank()) continue
                val street = tags.optString("addr:street", "")
                val display = if (street.isNotBlank() && street != title) "$title — $street" else title
                val normTitle = ArabicNormalizer.normalize(display)
                // Rank exact keyword match higher if user included a specific brand like "الكريمي"
                val hasSpecificToken = tokens.any { t ->
                    t !in setOf("صرافه", "صراف", "جامع", "مسجد", "مدرسه", "جوله", "دوار", "بنك", "محطه", "مستشفى") &&
                        normTitle.contains(t)
                }
                val item = RealValhallaNavigator.SearchAddressResult(
                    displayName = display,
                    shortName = title,
                    latitude = pLat,
                    longitude = pLon,
                    category = tags.optString("amenity", tags.optString("junction", "poi")),
                    icon = iconForOsmType(tags.optString("amenity", ""), tags.optString("junction", ""), title)
                )
                if (hasSpecificToken) out.add(0, item) else out.add(item)
            }
            out
        } catch (e: Exception) {
            Log.d(TAG, "Overpass POI error: ${e.message}")
            emptyList()
        } finally {
            conn?.disconnect()
        }
    }

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
        val normQ = ArabicNormalizer.normalize(q)

        val localResults = mutableListOf<RealValhallaNavigator.SearchAddressResult>()

        providers.filter {
            it.latitude != 0.0 && it.longitude != 0.0 &&
                ArabicNormalizer.normalize("${it.name} ${it.area} ${it.localNeighborhood} ${it.profession} ${it.specialization}").contains(normQ)
        }.take(6).forEach {
            localResults.add(
                RealValhallaNavigator.SearchAddressResult(
                    displayName = "${it.name} - ${it.area.ifBlank { it.localNeighborhood }}",
                    shortName = it.name,
                    latitude = it.latitude,
                    longitude = it.longitude,
                    category = "provider",
                    icon = "👷"
                )
            )
        }
        stores.filter {
            it.latitude != 0.0 && it.longitude != 0.0 &&
                ArabicNormalizer.normalize("${it.name} ${it.localNeighborhood} ${it.description} ${it.categoryId}").contains(normQ)
        }.take(6).forEach {
            localResults.add(
                RealValhallaNavigator.SearchAddressResult(
                    displayName = if (it.localNeighborhood.isNotBlank()) "${it.name} - ${it.localNeighborhood}" else it.name,
                    shortName = it.name,
                    latitude = it.latitude,
                    longitude = it.longitude,
                    category = "store",
                    icon = "🏪"
                )
            )
        }

        val anchorLat = if (isValidLatLng(userLat, userLng)) userLat else 15.3547
        val anchorLng = if (isValidLatLng(userLat, userLng)) userLng else 44.2067

        val (nominatimList, photonList, overpassList) = coroutineScope {
            val nomDeferred = async { fetchNominatim(q, anchorLat, anchorLng) }
            val phoDeferred = async { fetchPhoton(q, anchorLat, anchorLng) }
            val ovpDeferred = async { fetchOverpassCategoryPois(q, normQ, anchorLat, anchorLng) }
            Triple(nomDeferred.await(), phoDeferred.await(), ovpDeferred.await())
        }

        val merged = LinkedHashMap<String, RealValhallaNavigator.SearchAddressResult>()
        fun addUnique(item: RealValhallaNavigator.SearchAddressResult) {
            val k = "${ArabicNormalizer.normalize(item.shortName)}_${String.format(Locale.US, "%.3f_%.3f", item.latitude, item.longitude)}"
            merged.putIfAbsent(k, item)
        }

        localResults.forEach(::addUnique)
        nominatimList.forEach(::addUnique)
        overpassList.forEach(::addUnique)
        photonList.forEach(::addUnique)

        val finalList = merged.values.toMutableList()
        if (isValidLatLng(userLat, userLng)) {
            finalList.sortBy {
                HaversinePrecisionNavigator.calculateDistance(userLat, userLng, it.latitude, it.longitude)
            }
        }
        finalList
    }
}
