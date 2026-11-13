package com.serviseyem.utils

import android.content.Context
import com.serviseyem.data.PropertyEntity
import com.serviseyem.data.ProviderEntity
import com.serviseyem.data.StoreEntity
import com.serviseyem.ui.screens.map.utils.CityAliases
import com.serviseyem.ui.screens.map.utils.OfflineMapManager
import com.serviseyem.ui.screens.map.utils.StreetOrLandmark
import com.serviseyem.ui.screens.map.utils.YemeniNavigationHub
import org.json.JSONArray
import java.util.*
import kotlin.math.*

/**
 * 🔍 CrossFuzzySearchEngine
 * محرك البحث الذكي الهجين والشامل (Universal Search Engine):
 * 1. المعالم اليمنية من yemen_landmarks.json (مساجد، مدارس، مستشفيات، جولات، جامعات)
 * 2. المتاجر والخدمات والمراكز من MainViewModel
 * 3. الشوارع والأحياء مع خوارزمية Levenshtein المتقدمة للتسامح مع الأخطاء الإملائية
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

    private var cachedLandmarks: List<StreetOrLandmark>? = null

    /**
     * تحميل معالم اليمن وفهرستها من أصول التطبيق أو الذاكرة
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
            } catch (e: Exception) {
                // Fallback to in-memory popular streets
            }
        }

        list.addAll(YemeniNavigationHub.POPULAR_STREETS_LANDMARKS)
        val distinctList = list.distinctBy { it.name }
        cachedLandmarks = distinctList
        return distinctList
    }

    /**
     * حساب مسافة الدائرة العظمى (Haversine Formula) بدقة الكيلومتر
     */
    fun computeHaversineKm(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
        if (lat1 == 0.0 || lon1 == 0.0 || lat2 == 0.0 || lon2 == 0.0) return Double.MAX_VALUE
        val r = 6371.0 // نصف قطر الأرض بالكيلومتر
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

        // 3. البحث في المعالم اليمنية (مساجد، مدارس، مستشفيات، جولات، شوارع) من JSON والذاكرة
        val allLandmarks = getOrLoadLandmarks(context)
        val queryTokens = cleanQuery.split(" ", "-", "•", "/").map { it.trim() }.filter { it.length >= 2 }
        allLandmarks.forEach { landmark ->
            val combinedTarget = normalizeText("${landmark.name} ${landmark.area} ${landmark.city} ${landmark.type}")
            val isDialectMatch = com.serviseyem.ui.screens.map.utils.YemeniLocalSearchEngine.matchesDialectQuery(rawQuery, combinedTarget)
            val scoreName = calculateMatchScore(cleanQuery, normalizeText(landmark.name))
            val scoreCombined = calculateMatchScore(cleanQuery, combinedTarget)

            // Tokenized matching: check if query tokens match target name or combined string
            val tokenMatches = queryTokens.count { token -> combinedTarget.contains(token) }
            val hasTokenMatch = tokenMatches > 0 && queryTokens.isNotEmpty()

            val effectiveScore = maxOf(
                scoreName,
                scoreCombined,
                if (hasTokenMatch) (60 + tokenMatches * 10) else 0,
                if (isDialectMatch) 75 else 0
            )

            if (effectiveScore > 25) {
                val dist = if (hasGps) computeHaversineKm(userLat, userLng, landmark.lat, landmark.lng) else null
                val distText = dist?.let { " • يبعد ${formatDistance(it)}" } ?: ""
                val typeIcon = when {
                    landmark.type.contains("مسجد") || landmark.type.contains("جامع") || landmark.name.contains("جامع") || landmark.name.contains("مسجد") -> "🕌"
                    landmark.type.contains("مستشفى") || landmark.name.contains("مستشفى") || landmark.name.contains("طبي") -> "🏥"
                    landmark.type.contains("مدرسة") || landmark.type.contains("جامعة") || landmark.name.contains("مدرسة") || landmark.name.contains("جامعة") -> "🎓"
                    landmark.type.contains("جولة") || landmark.type.contains("دوار") || landmark.name.contains("جولة") -> "🔄"
                    landmark.type.contains("شارع") || landmark.name.contains("شارع") || landmark.name.contains("خط") -> "🛣️"
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
            val isDialectMatch = com.serviseyem.ui.screens.map.utils.YemeniLocalSearchEngine.matchesDialectQuery(
                rawQuery, "${p.name} ${p.profession} ${p.specialization}"
            )
            val scoreName = calculateMatchScore(cleanQuery, normalizeText(p.name))
            val scoreProf = calculateMatchScore(cleanQuery, normalizeText(p.profession))
            val scoreSpec = calculateMatchScore(cleanQuery, normalizeText(p.specialization))
            val maxScore = maxOf(scoreName, scoreProf, scoreSpec, if (isDialectMatch) 70 else 0)

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
            val isDialectMatch = com.serviseyem.ui.screens.map.utils.YemeniLocalSearchEngine.matchesDialectQuery(
                rawQuery, "${s.name} ${s.categoryId} ${s.description}"
            )
            val scoreName = calculateMatchScore(cleanQuery, normalizeText(s.name))
            val scoreCat = calculateMatchScore(cleanQuery, normalizeText(s.categoryId))
            val maxScore = maxOf(scoreName, scoreCat, if (isDialectMatch) 70 else 0)

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
            val isDialectMatch = com.serviseyem.ui.screens.map.utils.YemeniLocalSearchEngine.matchesDialectQuery(
                rawQuery, "${pr.title} ${pr.localNeighborhood} ${pr.propertyType}"
            )
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

        // تصفية وترتيب النتائج:
        // إذا كان موقع GPS متوفراً: ترتيب النتائج القوية أولاً حسب المسافة الأقرب
        val distinct = results.distinctBy { it.title + it.type.name + it.latitude.toString() }

        return if (hasGps) {
            distinct.sortedWith(
                compareByDescending<SearchResult> { it.score >= 80 }
                    .thenBy { it.distanceKm ?: Double.MAX_VALUE }
                    .thenByDescending { it.score }
            ).take(20)
        } else {
            distinct.sortedByDescending { it.score }.take(20)
        }
    }

    /**
     * واجهة البحث الأصلية (للتوافق القديم)
     */
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

    /**
     * توحيد النصوص العربية والإنجليزية
     */
    private fun normalizeText(text: String): String {
        var t = text.lowercase(Locale.getDefault()).trim()
        t = t.replace("أ", "ا")
            .replace("إ", "ا")
            .replace("آ", "ا")
            .replace("ة", "ه")
            .replace("ى", "ي")
            .replace("ؤ", "و")
            .replace("ئ", "ي")
            .replace("  ", " ")
        return t
    }

    private fun calculateMatchScore(query: String, target: String): Int {
        if (query.isEmpty() || target.isEmpty()) return 0
        if (target == query) return 100
        if (target.startsWith(query)) return 90
        if (target.contains(query)) return 75

        val distance = levenshtein(query, target)
        val maxLength = maxOf(query.length, target.length)
        val score = ((maxLength - distance).toDouble() / maxLength.toDouble() * 100).toInt()
        return if (score > 40) score else 0
    }

    private fun levenshtein(s1: String, s2: String): Int {
        val dp = Array(s1.length + 1) { IntArray(s2.length + 1) }
        for (i in 0..s1.length) dp[i][0] = i
        for (j in 0..s2.length) dp[0][j] = j
        for (i in 1..s1.length) {
            for (j in 1..s2.length) {
                val cost = if (s1[i - 1] == s2[j - 1]) 0 else 1
                dp[i][j] = minOf(dp[i - 1][j] + 1, dp[i][j - 1] + 1, dp[i - 1][j - 1] + cost)
            }
        }
        return dp[s1.length][s2.length]
    }
}
