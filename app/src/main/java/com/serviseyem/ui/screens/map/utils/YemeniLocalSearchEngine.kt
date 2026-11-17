package com.serviseyem.ui.screens.map.utils

import com.serviseyem.data.PropertyEntity
import com.serviseyem.data.ProviderEntity
import com.serviseyem.data.StoreEntity
import com.serviseyem.domain.YemenCities
import java.text.Normalizer
import kotlin.math.abs

/**
 * 🇾🇪 YemeniLocalSearchEngine
 * Comprehensive search engine tailored for Yemeni dialect, local terminology, fuzzy Levenshtein matching,
 * category translation, and landmark indexing — 100% Free & On-Device.
 */
object YemeniLocalSearchEngine {

    // 📚 Yemeni Local Synonym & Dialect Dictionary
    val LOCAL_SYNONYMS = mapOf(
        "ماطور" to listOf("مضخة", "مولد", "كهرباء", "سباك", "دينمو"),
        "ماطور ماء" to listOf("مضخة", "سباك", "شابورة"),
        "كنداكتر" to listOf("كهربائي", "طبلون", "لوحة توزيع"),
        "دينمو" to listOf("كهربائي", "مضخة", "ماطور"),
        "بوفيه" to listOf("مطعم", "وجبات سريعة", "ساندويتش", "سلته", "عصيرات"),
        "مطعم شعبات" to listOf("مطعم", "سلته", "شفوت", "فحسة"),
        "بشرية" to listOf("ميكانيكي", "فني سيارات", "ورشة"),
        "بنشر" to listOf("بنشري", "إطارات", "فني سيارات"),
        "بنشري" to listOf("بنشر", "إطارات", "صيانة سيارات"),
        "سمكري" to listOf("سمكرة", "فني سيارات", "ورشة"),
        "بقالة" to listOf("متجر", "سوبر ماركت", "دكان", "مواد غذائية"),
        "دكان" to listOf("بقالة", "متجر", "سوبر ماركت"),
        "تنزيلات" to listOf("متجر", "تخفيضات", "مول"),
        "صيدلية" to listOf("مركز طبي", "دواء", "مستشفى", "عيادة"),
        "مستوصف" to listOf("مركز طبي", "عيادة", "مستشفى"),
        "دكتور" to listOf("طبيب", "مركز طبي", "عيادة"),
        "عمارة" to listOf("عقار", "مبنى", "شقة", "دور"),
        "شقة" to listOf("عقار", "إيجار", "سكن"),
        "بيت" to listOf("عقار", "منزل", "فلل"),
        "أرضية" to listOf("عقار", "أرض", "مخطط"),
        "صرافة" to listOf("صراف", "حوالة", "الكريمي", "العمقي", "النجم", "بنك", "مصرف"),
        "صراف" to listOf("صرافة", "حوالة", "الكريمي", "العمقي", "بنك", "مصرف"),
        "جولة" to listOf("دوار", "تقاطع", "ميدان", "مفرق"),
        "مدرسة" to listOf("ثانوية", "أساسية", "معهد", "كلية", "جامعة"),
        "جامع" to listOf("مسجد", "مصلى"),
        "مسجد" to listOf("جامع", "مصلى")
    )

    // Pre-normalized map built lazily to avoid normalizing dictionary keys per query
    private val normalizedSynonyms: List<Pair<String, List<String>>> by lazy {
        LOCAL_SYNONYMS.map { (k, v) ->
            normalizeArabicText(k) to v.map { normalizeArabicText(it) }
        }
    }

    // 🎨 Authentic Yemeni Category Display Translations
    val LOCAL_CATEGORY_TRANSLATIONS = mapOf(
        "STORES" to "بقالة وتنزيلات 🛒",
        "RESTAURANTS" to "بوفيه ومأكولات 🍔",
        "TECHNICIANS" to "ميكانيكي وفني 🔧",
        "MEDICAL" to "صيدلية ومختبر 💊",
        "PROPERTIES" to "شقق وعقارات 🏢",
        "JOBS" to "وظائف وكوادر 💼",
        "PROVIDERS" to "مهندسين وفنيين 🛠️"
    )

    /**
     * Normalize Arabic text: Strip diacritics, unify Alef variants (أ، إ، آ -> ا), Taa Marbuta (ة -> ه), Yaa (ى -> ي)
     */
    fun normalizeArabicText(input: String): String {
        if (input.isBlank()) return ""
        val sb = StringBuilder(input.length)
        var prevSpace = false
        for (raw in input.trim().lowercase()) {
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

    /**
     * Compute Levenshtein distance for pre-normalized or arbitrary strings (1D DP row optimization).
     */
    fun computeLevenshteinDistance(s1: String, s2: String): Int {
        val str1 = normalizeArabicText(s1)
        val str2 = normalizeArabicText(s2)
        return levenshteinRaw(str1, str2)
    }

    private fun levenshteinRaw(str1: String, str2: String): Int {
        if (str1 == str2) return 0
        if (str1.isEmpty()) return str2.length
        if (str2.isEmpty()) return str1.length

        var prev = IntArray(str2.length + 1) { it }
        var curr = IntArray(str2.length + 1)
        for (i in 1..str1.length) {
            curr[0] = i
            val c1 = str1[i - 1]
            for (j in 1..str2.length) {
                val cost = if (c1 == str2[j - 1]) 0 else 1
                curr[j] = minOf(prev[j] + 1, curr[j - 1] + 1, prev[j - 1] + cost)
            }
            val tmp = prev
            prev = curr
            curr = tmp
        }
        return prev[str2.length]
    }

    /**
     * Checks if search query matches query target using dialect expansion and fuzzy matching.
     * Note: accepts already-normalized or raw strings cleanly.
     */
    fun matchesDialectQuery(query: String, targetText: String): Boolean {
        val normQuery = normalizeArabicText(query)
        val normTarget = normalizeArabicText(targetText)

        if (normQuery.isBlank()) return true
        if (normTarget.contains(normQuery)) return true

        // Check pre-normalized dialect synonym dictionary
        for ((normLocal, standardSynonyms) in normalizedSynonyms) {
            if (normQuery.contains(normLocal) || (normQuery.length >= 3 && normLocal.contains(normQuery))) {
                for (syn in standardSynonyms) {
                    if (syn.isNotEmpty() && normTarget.contains(syn)) {
                        return true
                    }
                }
            }
        }

        // Fast-guarded Fuzzy Levenshtein match for words of length >= 3
        val queryWords = normQuery.split(' ').filter { it.length >= 3 }
        if (queryWords.isEmpty()) return false
        val targetWords = normTarget.split(' ').filter { it.length >= 3 }

        for (qWord in queryWords) {
            val maxAllowedDist = if (qWord.length <= 4) 1 else 2
            for (tWord in targetWords) {
                if (abs(qWord.length - tWord.length) > maxAllowedDist) continue
                if (levenshteinRaw(qWord, tWord) <= maxAllowedDist) {
                    return true
                }
            }
        }

        return false
    }

    /**
     * Search public Yemeni landmarks matching search query
     */
    fun searchYemeniLandmarks(query: String): List<StreetOrLandmark> {
        val norm = normalizeArabicText(query)
        if (norm.length < 2) return emptyList()

        return YemeniNavigationHub.POPULAR_STREETS_LANDMARKS.filter { landmark ->
            matchesDialectQuery(norm, "${landmark.name} ${landmark.area} ${landmark.city} ${landmark.type}")
        }
    }

    /**
     * Get authentic Yemeni label for category
     */
    fun getLocalCategoryLabel(categoryKey: String): String {
        return LOCAL_CATEGORY_TRANSLATIONS[categoryKey.uppercase()] ?: categoryKey
    }
}
