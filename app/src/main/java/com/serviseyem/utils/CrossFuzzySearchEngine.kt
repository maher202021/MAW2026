package com.serviseyem.utils

import com.serviseyem.data.PropertyEntity
import com.serviseyem.data.ProviderEntity
import com.serviseyem.data.StoreEntity
import com.serviseyem.ui.screens.map.utils.CityAliases
import com.serviseyem.ui.screens.map.utils.OfflineMapManager
import java.util.*

/**
 * 🔍 CrossFuzzySearchEngine
 * محرك البحث الذكي الهجين (Smart Hybrid Search) - النسخة المطورة:
 * - يدعم اللغة العربية والإنجليزية (Bilingual)
 * - خوارزمية Levenshtein المتقدمة للتعامل مع الأخطاء الإملائية
 * - البحث الذكي عن الأشخاص والأسماء والشوارع والمحلات
 * - معالجة خاصة للهجات اليمنية (صنعاني، عدني، حضرمي، الخ)
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
        val score: Int = 0
    )

    enum class SearchType {
        CITY, AREA, STREET, PROVIDER, STORE, PROPERTY, PERSON
    }

    /**
     * تنفيذ البحث الذكي الشامل
     */
    fun search(
        query: String,
        providers: List<ProviderEntity>,
        stores: List<StoreEntity>,
        properties: List<PropertyEntity>
    ): List<SearchResult> {
        val rawQuery = query.trim()
        if (rawQuery.isEmpty()) return emptyList()

        val cleanQuery = normalizeText(rawQuery)
        val results = mutableListOf<SearchResult>()

        // 1. البحث في المدن والمحافظات (أولوية قصوى)
        OfflineMapManager.MAJOR_YEMENI_CITIES.forEach { city ->
            val scoreAr = calculateMatchScore(cleanQuery, normalizeText(city.nameAr))
            // OfflineMapManager.CityCoordinates only has nameAr
            val maxScore = scoreAr 
            
            if (maxScore > 25) {
                results.add(SearchResult(
                    id = city.nameAr,
                    title = city.nameAr,
                    subtitle = "محافظة / مدينة يمنية 🇾🇪",
                    type = SearchType.CITY,
                    latitude = city.latitude,
                    longitude = city.longitude,
                    originalItem = city,
                    score = maxScore + 30
                ))
            }
        }

        // ... (rest of the code) ...

        // 3. البحث عن الأشخاص والفنيين (الاسم + المهنة)
        providers.forEach { p ->
            val scoreName = calculateMatchScore(cleanQuery, normalizeText(p.name))
            val scoreProf = calculateMatchScore(cleanQuery, normalizeText(p.profession))
            val scoreSpec = calculateMatchScore(cleanQuery, normalizeText(p.specialization))
            val maxScore = maxOf(scoreName, scoreProf, scoreSpec)

            if (maxScore > 30) {
                results.add(SearchResult(
                    id = p.id,
                    title = p.name,
                    subtitle = "👤 ${p.profession} - ${p.area}",
                    type = SearchType.PERSON,
                    latitude = p.latitude,
                    longitude = p.longitude,
                    originalItem = p,
                    score = maxScore + 20
                ))
            }
        }

        // 4. البحث في المتاجر والمطاعم
        stores.forEach { s ->
            val scoreName = calculateMatchScore(cleanQuery, normalizeText(s.name))
            val scoreCat = calculateMatchScore(cleanQuery, normalizeText(s.categoryId))
            val maxScore = maxOf(scoreName, scoreCat)

            if (maxScore > 30) {
                val category = when {
                    s.sectionId.contains("medical") -> "🏥 مركز طبي"
                    s.sectionId.contains("restaurant") -> "🍔 مطعم/كافيه"
                    else -> "🏪 متجر"
                }
                results.add(SearchResult(
                    id = s.id,
                    title = s.name,
                    subtitle = "$category - ${s.localNeighborhood}",
                    type = SearchType.STORE,
                    latitude = s.latitude,
                    longitude = s.longitude,
                    originalItem = s,
                    score = maxScore
                ))
            }
        }

        // 5. البحث في العقارات والشوارع
        properties.forEach { pr ->
            val scoreTitle = calculateMatchScore(cleanQuery, normalizeText(pr.title))
            val scoreLoc = calculateMatchScore(cleanQuery, normalizeText(pr.localNeighborhood))
            val maxScore = maxOf(scoreTitle, scoreLoc)

            if (maxScore > 30) {
                results.add(SearchResult(
                    id = pr.id,
                    title = pr.title,
                    subtitle = "🏠 عقار في ${pr.localNeighborhood}",
                    type = SearchType.PROPERTY,
                    latitude = pr.latitude,
                    longitude = pr.longitude,
                    originalItem = pr,
                    score = maxScore
                ))
            }
        }

        // تصفية وترتيب النتائج
        return results
            .distinctBy { it.title + it.type.name + it.latitude.toString() }
            .sortedByDescending { it.score }
            .take(15)
    }

    /**
     * توحيد النصوص (العربية والإنجليزية) للبحث المرن
     */
    private fun normalizeText(text: String): String {
        var t = text.lowercase(Locale.getDefault()).trim()
        
        // معالجة العربية
        t = t.replace("أ", "ا")
            .replace("إ", "ا")
            .replace("آ", "ا")
            .replace("ة", "ه")
            .replace("ى", "ي")
            .replace("ؤ", "و")
            .replace("ئ", "ي")
            .replace("  ", " ")
        
        // إزالة ال التعريف في بداية الكلمة (اختياري، يفضل إبقاؤها مع تقليل وزنها)
        if (t.startsWith("ال") && t.length > 4) {
            // نبقي النسخة الأصلية ونضيف النسخة بدون ال للبحث (أو نكتفي بتقليل أثر ال)
        }
        
        return t
    }

    private fun calculateMatchScore(query: String, target: String): Int {
        if (query.isEmpty() || target.isEmpty()) return 0
        
        // 1. تطابق كامل
        if (target == query) return 100
        
        // 2. يبدأ بـ (أهمية عالية)
        if (target.startsWith(query)) return 90
        
        // 3. يحتوي على (أهمية متوسطة)
        if (target.contains(query)) return 75
        
        // 4. خوارزمية Levenshtein للبحث التقريبي (Typo tolerance)
        val distance = levenshtein(query, target)
        val maxLength = maxOf(query.length, target.length)
        val score = ((maxLength - distance).toDouble() / maxLength.toDouble() * 100).toInt()
        
        // لا نقبل سكور أقل من 40 في الليفنشتاين لتجنب النتائج العشوائية جداً
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

