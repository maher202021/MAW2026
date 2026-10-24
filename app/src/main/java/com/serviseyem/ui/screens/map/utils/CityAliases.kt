package com.serviseyem.ui.screens.map.utils

/**
 * 🏙️ CityAliases
 * Map of Yemeni cities and governorates to their aliases, area names, and system codes.
 * Used by MapScreenFilters for resilient city matching across all Yemeni governorates.
 */
object CityAliases {
    val ALIASES: Map<String, List<String>> = mapOf(
        "صنعاء" to listOf("صنعاء", "sanaa", "ye_san", "1", "حدة", "السبعين", "التحرير", "الصافية", "شعوب", "الروضة"),
        "عدن" to listOf("عدن", "aden", "ye_ade", "2", "كريتر", "المعلا", "المنصورة", "الشيخ عثمان", "خور مكسر", "التواهي", "دار سعد", "البريقة"),
        "تعز" to listOf("تعز", "taiz", "ye_tai", "3", "الحوبان", "صالة", "المظفر", "القاهرة", "بير باشا"),
        "الحديدة" to listOf("الحديدة", "hodeidah", "ye_hud", "الحالي", "الميناء", "الحوك", "7 يوليو"),
        "إب" to listOf("إب", "ibb", "ye_ibb", "يريم", "العدين", "جبلة", "الظهار", "المشنة"),
        "حضرموت" to listOf("حضرموت", "المكلا", "mukalla", "سيئون", "ye_had", "الشحر", "تريم", "القطن"),
        "المكلا" to listOf("حضرموت", "المكلا", "mukalla", "سيئون", "ye_had", "الشحر"),
        "مأرب" to listOf("مأرب", "marib", "ye_mar", "المدينة", "الوادي", "صافر"),
        "ذمار" to listOf("ذمار", "dhamar", "ye_dha", "عنس", "هران"),
        "صعدة" to listOf("صعدة", "saada", "ye_saa"),
        "حجة" to listOf("حجة", "hajjah", "ye_haj"),
        "شبوة" to listOf("شبوة", "عتق", "shabwah", "ye_sha"),
        "المهرة" to listOf("المهرة", "الغيضة", "al-mahra", "ye_mah"),
        "أبين" to listOf("أبين", "زنجبار", "ابين", "ye_aby", "جعار"),
        "لحج" to listOf("لحج", "الحوطة", "lahj", "ye_lah"),
        "الضالع" to listOf("الضالع", "dhale", "ye_dha"),
        "البيضاء" to listOf("البيضاء", "al-bayda", "ye_bay", "رداع"),
        "عمران" to listOf("عمران", "amran", "ye_amr"),
        "المحويت" to listOf("المحويت", "al-mahweet", "ye_mah"),
        "ريمة" to listOf("ريمة", "raymah", "ye_ray"),
        "الجوف" to listOf("الجوف", "al-jawf", "ye_jaw"),
        "سقطرى" to listOf("سقطرى", "socotra", "ye_soc", "حديبو")
    )
}
