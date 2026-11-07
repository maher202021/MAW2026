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
        "أرضية" to listOf("عقار", "أرض", "مخطط")
    )

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
        var text = input.trim().lowercase()
        // Strip Arabic Tashkeel / Harakat
        text = text.replace(Regex("[\\u064B-\\u0652]"), "")
        // Unify Alef
        text = text.replace(Regex("[أإآ]"), "ا")
        // Unify Taa Marbuta / Haa
        text = text.replace("ة", "ه")
        // Unify Yaa / Alef Maksura
        text = text.replace("ى", "ي")
        return text
    }

    /**
     * Compute Levenshtein distance for fuzzy string matching
     */
    fun computeLevenshteinDistance(s1: String, s2: String): Int {
        val str1 = normalizeArabicText(s1)
        val str2 = normalizeArabicText(s2)
        if (str1 == str2) return 0
        if (str1.isEmpty()) return str2.length
        if (str2.isEmpty()) return str1.length

        val dp = Array(str1.length + 1) { IntArray(str2.length + 1) }
        for (i in 0..str1.length) dp[i][0] = i
        for (j in 0..str2.length) dp[0][j] = j

        for (i in 1..str1.length) {
            for (j in 1..str2.length) {
                val cost = if (str1[i - 1] == str2[j - 1]) 0 else 1
                dp[i][j] = minOf(
                    dp[i - 1][j] + 1,
                    dp[i][j - 1] + 1,
                    dp[i - 1][j - 1] + cost
                )
            }
        }
        return dp[str1.length][str2.length]
    }

    /**
     * Checks if search query matches query target using dialect expansion and fuzzy matching
     */
    fun matchesDialectQuery(query: String, targetText: String): Boolean {
        val normQuery = normalizeArabicText(query)
        val normTarget = normalizeArabicText(targetText)

        if (normQuery.isBlank()) return true
        if (normTarget.contains(normQuery)) return true

        // Check dialect synonym dictionary
        for ((localWord, standardSynonyms) in LOCAL_SYNONYMS) {
            val normLocal = normalizeArabicText(localWord)
            if (normQuery.contains(normLocal) || normLocal.contains(normQuery)) {
                for (syn in standardSynonyms) {
                    if (normTarget.contains(normalizeArabicText(syn))) {
                        return true
                    }
                }
            }
        }

        // Fuzzy Levenshtein match for words of length >= 4
        val queryWords = normQuery.split(" ").filter { it.length >= 3 }
        val targetWords = normTarget.split(" ").filter { it.length >= 3 }

        for (qWord in queryWords) {
            for (tWord in targetWords) {
                val dist = computeLevenshteinDistance(qWord, tWord)
                val maxAllowedDist = if (qWord.length <= 4) 1 else 2
                if (dist <= maxAllowedDist) {
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
