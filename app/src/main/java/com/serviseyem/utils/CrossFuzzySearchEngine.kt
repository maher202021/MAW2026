package com.serviseyem.utils

import android.content.Context
import com.serviseyem.data.PropertyEntity
import com.serviseyem.data.ProviderEntity
import com.serviseyem.data.StoreEntity
import com.serviseyem.ui.screens.map.utils.CityAliases
import com.serviseyem.ui.screens.map.utils.OfflineMapManager
import com.serviseyem.ui.screens.map.utils.StreetOrLandmark
import com.serviseyem.ui.screens.map.utils.YemeniNavigationHub
import com.serviseyem.ui.screens.map.visualaddress.LandmarkRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import java.util.*
import kotlin.math.*

/**
 * 🔍 CrossFuzzySearchEngine
 * محرك البحث الذكي الهجين والشامل (Universal Search Engine):
 * 1. المعالم اليمنية من yemen_landmarks.json + LandmarkRepository + إضافات المجتمع (مساجد، مدارس، صرافات، مستشفيات، جولات، جامعات)
 * 2. المتاجر والخدمات والمراكز من التطبيق
 * 3. الشوارع والأحياء مع خوارزمية Levenshtein المحسّنة والسريعة للتسامح مع الأخطاء الإملائية
 * 4. دعم لهجات اليمن (صنعاني، عدني، حضرمي، تعزي) والمترادفات المحلية
 * 5. حساب مسافة Haversine من موقع GPS الحالي للمستخدم والترتيب الذكي حسب القرب الجغرافي
 */
object CrossFuzzySearchEngine {

    data class SearchResult(
        val id: String,
        val title: String,
        val subtitle: String,
        val type: SearchType,
        val latitude: Double,
        val longitude: Double,
        val originalItem: Any? = null,
        val score: Int = 0,
        val distanceKm: Double? = null
    )

    enum class SearchType {
        CITY, AREA, STREET, PROVIDER, STORE, PROPERTY, PERSON, LANDMARK
    }

    @Volatile
    private var cachedLandmarks: List<StreetOrLandmark>? = null

    fun invalidateCache() {
        cachedLandmarks = null
    }

    /**
     * تحميل معالم اليمن وفهرستها من أصول التطبيق + المستودع الدائم + مساهمات المستخدمين
     */
    fun getOrLoadLandmarks(context: Context? = null): List<StreetOrLandmark> {
        cachedLandmarks?.let { return it }
        val list = mutableListOf<StreetOrLandmark>()

        if (context != null) {
            try {
                val jsonStr = context.assets.open("yemen_landmarks.json").bufferedReader().use { it.readText() }
                val jsonArray = JSONArray(jsonStr)
                for (i in 0 until jsonArray.length()) {
                    val obj = jsonArray.getJSONObject(i)
                    list.add(
                        StreetOrLandmark(
                            name = obj.getString("name"),
                            type = obj.optString("type", "معلم شهير"),
                            city = obj.optString("city", "اليمن"),
                            area = obj.optString("area", ""),
                            lat = obj.getDouble("lat"),
                            lng = obj.getDouble("lng")
                        )
                    )
                }
            } catch (_: Exception) {
                // Fallback to in-memory popular streets
            }

            try {
                OsmContributionManager.getPendingLandmarks(context).forEach { node ->
                    val typeAr = when (node.category) {
                        "mosque" -> "مسجد"
                        "water_well" -> "بئر مياه"
                        "market" -> "سوق"
                        "fuel" -> "محطة وقود"
                        "workshop" -> "ورشة"
                        else -> "معلم مجتمعي"
                    }
                    list.add(
                        StreetOrLandmark(
                            name = node.name,
                            type = typeAr,
                            city = node.city.ifBlank { "اليمن" },
                            area = node.notes.ifBlank { "مضاف من المجتمع" },
                            lat = node.latitude,
                            lng = node.longitude
                        )
                    )
                }
            } catch (_: Exception) {
            }
        }

        // دمج معالم LandmarkRepository (جولات، مساجد، أسواق، جامعات، مستشفيات)
        LandmarkRepository.getAllLandmarks().forEach { lm ->
            list.add(
                StreetOrLandmark(
                    name = lm.name,
                    type = lm.category.labelArabic,
                    city = lm.cityId,
                    area = lm.aliases.joinToString(" / "),
                    lat = lm.latitude,
                    lng = lm.longitude
                )
            )
        }

        list.addAll(YemeniNavigationHub.POPULAR_STREETS_LANDMARKS)
        val distinctList = list.distinctBy { normalizeText(it.name) + "_" + String.format(Locale.US, "%.3f_%.3f", it.lat, it.lng) }
        cachedLandmarks = distinctList
        return distinctList
    }

    /**
     * حساب مسافة الدائرة العظمى (Haversine Formula) بدقة الكيلومتر
     */
    fun computeHaversineKm(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
        if (lat1 == 0.0 || lon1 == 0.0 || lat2 == 0.0 || lon2 == 0.0) return Double.MAX_VALUE
        val r = 6371.0
        val dLat = Math.toRadians(lat2 - lat1)
        val dLon = Math.toRadians(lon2 - lon1)
        val a = sin(dLat / 2).pow(2) +
                cos(Math.toRadians(lat1)) * cos(Math.toRadians(lat2)) *
                sin(dLon / 2).pow(2)
        val c = 2 * atan2(sqrt(a), sqrt(1 - a))
        return r * c
    }

    private fun formatDistance(distKm: Double): String {
        return if (distKm < 1.0) {
            "${(distKm * 1000).toInt()} م"
        } else {
            String.format(Locale.US, "%.1f كم", distKm)
        }
    }

    suspend fun searchEverythingAsync(
        query: String,
        providers: List<ProviderEntity>,
        stores: List<StoreEntity>,
        properties: List<PropertyEntity>,
        userLat: Double = 0.0,
        userLng: Double = 0.0,
        context: Context? = null
    ): List<SearchResult> = withContext(Dispatchers.Default) {
        searchEverything(query, providers, stores, properties, userLat, userLng, context)
    }

    /**
     * تنفيذ البحث الذكي الشامل searchEverything
     */
    fun searchEverything(
        query: String,
        providers: List<ProviderEntity>,
        stores: List<StoreEntity>,
        properties: List<PropertyEntity>,
        userLat: Double = 0.0,
        userLng: Double = 0.0,
        context: Context? = null
    ): List<SearchResult> {
        val rawQuery = query.trim()
        if (rawQuery.isEmpty()) return emptyList()

        val cleanQuery = normalizeText(rawQuery)
        val dialectVariants = DialectSynonyms.expand(rawQuery).map { normalizeText(it) }.filter { it.isNotBlank() }
        val results = mutableListOf<SearchResult>()
        val hasGps = userLat != 0.0 && userLng != 0.0

        // 1. البحث في المدن والمحافظات الرئيسية (أولوية جغرافية)
        OfflineMapManager.MAJOR_YEMENI_CITIES.forEach { city ->
            val scoreAr = calculateMatchScore(cleanQuery, normalizeText(city.nameAr))
            if (scoreAr > 25) {
                val dist = if (hasGps) computeHaversineKm(userLat, userLng, city.latitude, city.longitude) else null
                val distText = dist?.let { " • يبعد ${formatDistance(it)}" } ?: ""
                results.add(
                    SearchResult(
                        id = "city_${city.nameAr}",
                        title = city.nameAr,
                        subtitle = "محافظة / عاصمة يمنية 🇾🇪$distText",
                        type = SearchType.CITY,
                        latitude = city.latitude,
                        longitude = city.longitude,
                        originalItem = city,
                        score = scoreAr + 30,
                        distanceKm = dist
                    )
                )
            }
        }

        // 2. البحث في المديريات والمناطق والأحياء
        CityAliases.ALIASES.forEach { (cityName, aliases) ->
            aliases.forEach { alias ->
                val score = calculateMatchScore(cleanQuery, normalizeText(alias))
                if (score > 35) {
                    val matchingCity = OfflineMapManager.MAJOR_YEMENI_CITIES.find { it.nameAr == cityName }
                    val lat = matchingCity?.latitude ?: 15.3694
                    val lng = matchingCity?.longitude ?: 44.1910
                    val dist = if (hasGps) computeHaversineKm(userLat, userLng, lat, lng) else null
                    val distText = dist?.let { " • يبعد ${formatDistance(it)}" } ?: ""
                    results.add(
                        SearchResult(
                            id = "area_$alias",
                            title = alias,
                            subtitle = "منطقة / حي في $cityName 📍$distText",
                            type = SearchType.AREA,
                            latitude = lat,
                            longitude = lng,
                            originalItem = alias,
                            score = score + 25,
                            distanceKm = dist
                        )
                    )
                }
            }
        }

        // 3. البحث في المعالم اليمنية (مساجد، مدارس، صرافات، مستشفيات، جولات، شوارع)
        val allLandmarks = getOrLoadLandmarks(context)
        val queryTokens = cleanQuery.split(' ', '-', '•', '/').map { it.trim() }.filter { it.length >= 2 }
        allLandmarks.forEach { landmark ->
            val combinedTarget = normalizeText("${landmark.name} ${landmark.area} ${landmark.city} ${landmark.type}")
            val isDialectMatch = dialectVariants.any { variant -> variant.length >= 2 && combinedTarget.contains(variant) }
            val scoreName = calculateMatchScore(cleanQuery, normalizeText(landmark.name))
            val scoreCombined = if (scoreName >= 70) scoreName else calculateMatchScore(cleanQuery, combinedTarget)

            val tokenMatches = if (queryTokens.isNotEmpty()) queryTokens.count { token -> combinedTarget.contains(token) } else 0
            val allTokensMatch = queryTokens.isNotEmpty() && tokenMatches == queryTokens.size

            val effectiveScore = maxOf(
                scoreName,
                scoreCombined,
                when {
                    allTokensMatch -> 85
                    tokenMatches > 0 -> 58 + tokenMatches * 10
                    else -> 0
                },
                if (isDialectMatch) 76 else 0
            )

            if (effectiveScore > 25) {
                val dist = if (hasGps) computeHaversineKm(userLat, userLng, landmark.lat, landmark.lng) else null
                val distText = dist?.let { " • يبعد ${formatDistance(it)}" } ?: ""
                val typeIcon = when {
                    landmark.type.contains("صراف") || landmark.type.contains("بنك") || landmark.name.contains("كريمي", true) || landmark.name.contains("صراف") -> "💱"
                    landmark.type.contains("مسجد") || landmark.type.contains("جامع") || landmark.name.contains("جامع") || landmark.name.contains("مسجد") -> "🕌"
                    landmark.type.contains("مستشفى") || landmark.name.contains("مستشفى") || landmark.name.contains("طبي") -> "🏥"
                    landmark.type.contains("مدرسة") || landmark.type.contains("جامعة") || landmark.name.contains("مدرسة") || landmark.name.contains("جامعة") -> "🎓"
                    landmark.type.contains("جولة") || landmark.type.contains("دوار") || landmark.name.contains("جولة") -> "🔄"
                    landmark.type.contains("شارع") || landmark.name.contains("شارع") || landmark.name.contains("خط") -> "🛣️"
                    landmark.type.contains("وقود") -> "⛽"
                    else -> "🏛️"
                }

                results.add(
                    SearchResult(
                        id = "landmark_${landmark.name}_${landmark.lat}",
                        title = landmark.name,
                        subtitle = "$typeIcon ${landmark.type} - ${landmark.area} (${landmark.city})$distText",
                        type = SearchType.LANDMARK,
                        latitude = landmark.lat,
                        longitude = landmark.lng,
                        originalItem = landmark,
                        score = effectiveScore + 20,
                        distanceKm = dist
                    )
                )
            }
        }

        // 4. البحث عن الفنيين ومزودي الخدمات (الأشخاص)
        providers.forEach { p ->
            val providerTarget = normalizeText("${p.name} ${p.profession} ${p.specialization} ${p.customCategoryName} ${p.area} ${p.localNeighborhood}")
            val isDialectMatch = dialectVariants.any { variant -> variant.length >= 2 && providerTarget.contains(variant) }
            val scoreName = calculateMatchScore(cleanQuery, normalizeText(p.name))
            val scoreProf = calculateMatchScore(cleanQuery, normalizeText("${p.profession} ${p.specialization} ${p.area}"))
            val maxScore = maxOf(scoreName, scoreProf, if (isDialectMatch) 72 else 0)

            if (maxScore > 30) {
                val dist = if (hasGps) computeHaversineKm(userLat, userLng, p.latitude, p.longitude) else null
                val distText = dist?.let { " • يبعد ${formatDistance(it)}" } ?: ""
                results.add(
                    SearchResult(
                        id = p.id,
                        title = p.name,
                        subtitle = "👤 ${p.profession} - ${p.area}$distText",
                        type = SearchType.PERSON,
                        latitude = p.latitude,
                        longitude = p.longitude,
                        originalItem = p,
                        score = maxScore + 20,
                        distanceKm = dist
                    )
                )
            }
        }

        // 5. البحث في المتاجر والمراكز الطبية والمطاعم
        stores.forEach { s ->
            val storeTarget = normalizeText("${s.name} ${s.categoryId} ${s.description} ${s.localNeighborhood} ${s.sectionId}")
            val isDialectMatch = dialectVariants.any { variant -> variant.length >= 2 && storeTarget.contains(variant) }
            val scoreName = calculateMatchScore(cleanQuery, normalizeText(s.name))
            val scoreCat = calculateMatchScore(cleanQuery, normalizeText("${s.categoryId} ${s.localNeighborhood} ${s.description}"))
            val maxScore = maxOf(scoreName, scoreCat, if (isDialectMatch) 72 else 0)

            if (maxScore > 30) {
                val dist = if (hasGps) computeHaversineKm(userLat, userLng, s.latitude, s.longitude) else null
                val distText = dist?.let { " • يبعد ${formatDistance(it)}" } ?: ""
                val category = when {
                    s.sectionId.contains("medical") -> "🏥 مركز طبي"
                    s.sectionId.contains("restaurant") -> "🍔 مطعم/بوفيه"
                    else -> "🏪 متجر"
                }
                results.add(
                    SearchResult(
                        id = s.id,
                        title = s.name,
                        subtitle = "$category - ${s.localNeighborhood}$distText",
                        type = SearchType.STORE,
                        latitude = s.latitude,
                        longitude = s.longitude,
                        originalItem = s,
                        score = maxScore,
                        distanceKm = dist
                    )
                )
            }
        }

        // 6. البحث في العقارات
        properties.forEach { pr ->
            val propTarget = normalizeText("${pr.title} ${pr.localNeighborhood} ${pr.propertyType}")
            val isDialectMatch = dialectVariants.any { variant -> variant.length >= 2 && propTarget.contains(variant) }
            val scoreTitle = calculateMatchScore(cleanQuery, normalizeText(pr.title))
            val scoreLoc = calculateMatchScore(cleanQuery, normalizeText(pr.localNeighborhood))
            val maxScore = maxOf(scoreTitle, scoreLoc, if (isDialectMatch) 70 else 0)

            if (maxScore > 30) {
                val dist = if (hasGps) computeHaversineKm(userLat, userLng, pr.latitude, pr.longitude) else null
                val distText = dist?.let { " • يبعد ${formatDistance(it)}" } ?: ""
                results.add(
                    SearchResult(
                        id = pr.id,
                        title = pr.title,
                        subtitle = "🏠 عقار في ${pr.localNeighborhood}$distText",
                        type = SearchType.PROPERTY,
                        latitude = pr.latitude,
                        longitude = pr.longitude,
                        originalItem = pr,
                        score = maxScore,
                        distanceKm = dist
                    )
                )
            }
        }

        val distinct = results.distinctBy { it.title + it.type.name + it.latitude.toString() }

        return if (hasGps) {
            distinct.sortedWith(
                compareByDescending<SearchResult> { it.score >= 80 }
                    .thenBy { it.distanceKm ?: Double.MAX_VALUE }
                    .thenByDescending { it.score }
            ).take(25)
        } else {
            distinct.sortedByDescending { it.score }.take(25)
        }
    }

    fun search(
        query: String,
        providers: List<ProviderEntity>,
        stores: List<StoreEntity>,
        properties: List<PropertyEntity>
    ): List<SearchResult> {
        return searchEverything(
            query = query,
            providers = providers,
            stores = stores,
            properties = properties
        )
    }

    private fun normalizeText(text: String): String {
        if (text.isBlank()) return ""
        val sb = StringBuilder(text.length)
        var prevSpace = false
        for (raw in text.trim().lowercase(Locale.getDefault())) {
            if (raw in '\u064B'..'\u065F' || raw == '\u0640') continue
            val ch = when (raw) {
                'أ', 'إ', 'آ' -> 'ا'
                'ة' -> 'ه'
                'ى' -> 'ي'
                'ؤ' -> 'و'
                'ئ' -> 'ي'
                else -> raw
            }
            if (ch.isWhitespace()) {
                if (!prevSpace) {
                    sb.append(' ')
                    prevSpace = true
                }
            } else {
                sb.append(ch)
                prevSpace = false
            }
        }
        return sb.toString().trim()
    }

    private fun calculateMatchScore(query: String, target: String): Int {
        if (query.isEmpty() || target.isEmpty()) return 0
        if (target == query) return 100
        if (target.startsWith(query)) return 90
        if (target.contains(query)) return 75

        val maxLength = maxOf(query.length, target.length)
        if (abs(query.length - target.length) > (maxLength * 0.55).toInt() + 1) return 0

        val distance = levenshtein(query, target)
        val score = ((maxLength - distance).toDouble() / maxLength.toDouble() * 100).toInt()
        return if (score > 45) score else 0
    }

    private fun levenshtein(s1: String, s2: String): Int {
        var prev = IntArray(s2.length + 1) { it }
        var curr = IntArray(s2.length + 1)
        for (i in 1..s1.length) {
            curr[0] = i
            val c1 = s1[i - 1]
            for (j in 1..s2.length) {
                val cost = if (c1 == s2[j - 1]) 0 else 1
                curr[j] = minOf(prev[j] + 1, curr[j - 1] + 1, prev[j - 1] + cost)
            }
            val tmp = prev
            prev = curr
            curr = tmp
        }
        return prev[s2.length]
    }
}
