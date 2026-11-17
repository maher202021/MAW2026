package com.serviseyem.utils

import com.serviseyem.ui.screens.map.utils.YemeniLocalSearchEngine

/**
 * توسيع استعلامات البحث باللهجة اليمنية والمرادفات الشائعة للشوارع والمعالم والخدمات
 */
object DialectSynonyms {
    private val PLACE_SYNONYMS = mapOf(
        "جوله" to listOf("دوار", "تقاطع", "ميدان", "مفرق", "roundabout"),
        "دوار" to listOf("جوله", "ميدان", "تقاطع", "مفرق"),
        "مفرق" to listOf("تقاطع", "جوله", "دوار"),
        "شارع" to listOf("طريق", "خط", "جاده"),
        "خط" to listOf("شارع", "طريق", "طريق عام"),
        "جامع" to listOf("مسجد", "مصلى", "mosque"),
        "مسجد" to listOf("جامع", "مصلى", "mosque"),
        "مصلى" to listOf("مسجد", "جامع"),
        "صرافه" to listOf("صراف", "حواله", "حوالات", "تحويل", "الكريمي", "العمقي", "النجم", "الامتياز", "بنك", "مصرف", "exchange"),
        "صراف" to listOf("صرافه", "حواله", "تحويل", "الكريمي", "العمقي", "بنك", "مصرف"),
        "حواله" to listOf("صرافه", "صراف", "الكريمي", "العمقي", "بنك"),
        "بنك" to listOf("مصرف", "صرافه", "صراف", "ايداع", "bank"),
        "مصرف" to listOf("بنك", "صرافه", "صراف", "الكريمي"),
        "مدرسه" to listOf("ثانويه", "اساسيه", "اعداديه", "معهد", "اكاديميه", "مجمع تربوي", "تربيه", "school"),
        "ثانويه" to listOf("مدرسه", "معهد", "تربيه"),
        "معهد" to listOf("مدرسه", "اكاديميه", "مركز تدريب", "كليه"),
        "جامعه" to listOf("كليه", "حرم جامعي", "university"),
        "كليه" to listOf("جامعه", "معهد"),
        "مستشفى" to listOf("مستوصف", "مركز طبي", "عياده", "مجمع طبي", "مختبر", "hospital"),
        "مستوصف" to listOf("مستشفى", "عياده", "مركز طبي"),
        "محطه" to listOf("محطه وقود", "بترول", "ديزل", "غاز", "طرمبه", "fuel"),
        "بترول" to listOf("محطه", "وقود", "بنزين", "طرمبه"),
        "سوبرماركت" to listOf("بقاله", "دكان", "متجر", "سوبر ماركت", "هايبر", "مول"),
        "بقاله" to listOf("دكان", "متجر", "سوبر ماركت", "مواد غذائيه"),
        "دكان" to listOf("بقاله", "متجر", "تموينات"),
        "بوفيه" to listOf("مطعم", "كافتيريا", "مقهايه"),
        "بنشر" to listOf("بنشري", "اطارات", "ميكانيكي", "ورشه"),
        "بنشري" to listOf("بنشر", "اطارات", "ميكانيكي", "ورشه"),
        "ورشه" to listOf("صيانه", "ميكانيكي", "سمكري", "كهربائي سيارات"),
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
