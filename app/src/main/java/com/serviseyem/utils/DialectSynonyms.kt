package com.serviseyem.utils

import com.serviseyem.ui.screens.map.utils.YemeniLocalSearchEngine

/**
 * توسيع استعلامات البحث باللهجة اليمنية والمرادفات الشائعة للشوارع والمعالم والخدمات
 */
object DialectSynonyms {
    private val PLACE_SYNONYMS = mapOf(
        "جوله" to listOf("دوار", "تقاطع", "ميدان"),
        "دوار" to listOf("جوله", "ميدان"),
        "شارع" to listOf("طريق", "خط"),
        "خط" to listOf("شارع", "طريق"),
        "جامع" to listOf("مسجد"),
        "مسجد" to listOf("جامع"),
        "مستشفى" to listOf("مستوصف", "مركز طبي", "عياده"),
        "مستوصف" to listOf("مستشفى", "عياده", "مركز طبي"),
        "سوبرماركت" to listOf("بقاله", "دكان", "متجر", "سوبر ماركت"),
        "بقاله" to listOf("دكان", "متجر", "سوبر ماركت"),
        "دكان" to listOf("بقاله", "متجر"),
        "بوفيه" to listOf("مطعم", "كافتيريا"),
        "بنشر" to listOf("بنشري", "اطارات", "ميكانيكي"),
        "بنشري" to listOf("بنشر", "اطارات", "ميكانيكي"),
        "ماطور" to listOf("مضخه", "مولد", "كهرباء", "سباك")
    )

    fun expand(query: String): List<String> {
        val norm = ArabicNormalizer.normalize(query)
        if (norm.isBlank()) return emptyList()

        val variants = LinkedHashSet<String>()
        variants.add(norm)

        fun applyMap(source: Map<String, List<String>>) {
            source.forEach { (rawKey, syns) ->
                val k = ArabicNormalizer.normalize(rawKey)
                if (k.isNotEmpty() && norm.contains(k)) {
                    syns.forEach { syn ->
                        val s = ArabicNormalizer.normalize(syn)
                        if (s.isNotEmpty()) {
                            variants.add(norm.replace(k, s))
                            variants.add(s)
                        }
                    }
                }
            }
        }

        applyMap(PLACE_SYNONYMS)
        applyMap(YemeniLocalSearchEngine.LOCAL_SYNONYMS)

        return variants.toList()
    }
}
