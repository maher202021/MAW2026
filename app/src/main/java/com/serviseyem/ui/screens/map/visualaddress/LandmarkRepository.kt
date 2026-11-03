package com.serviseyem.ui.screens.map.visualaddress

/**
 * 🏛️ LandmarkRepository
 * مستودع معالم اليمن البارزة (الجولات، المستشفيات، المساجد، الأسواق)
 * يوفر نقاط ارتكاز جغرافية دقيقة وثابتة لكل المدن اليمنية
 */
object LandmarkRepository {

    private val predefinedLandmarks = listOf(
        // 🏰 صنعاء
        YemeniLandmark("sn_ruwayshan", "جولة الرويشان", "صنعاء", LandmarkCategory.ROUNDABOUT, 15.3428, 44.2045, listOf("الرويشان", "حدة"), "🔄"),
        YemeniLandmark("sn_misbahi", "جولة المصباحي", "صنعاء", LandmarkCategory.ROUNDABOUT, 15.3340, 44.1950, listOf("المصباحي", "حدة"), "🔄"),
        YemeniLandmark("sn_tawahi", "جولة كحلان / عصر", "صنعاء", LandmarkCategory.ROUNDABOUT, 15.3520, 44.1750, listOf("عصر", "الستين الغربي"), "🔄"),
        YemeniLandmark("sn_saleh", "جامع الصالح", "صنعاء", LandmarkCategory.MOSQUE, 15.3267, 44.2089, listOf("الصالح", "ميدان السبعين"), "🕌"),
        YemeniLandmark("sn_thawra", "مستشفى الثورة العام", "صنعاء", LandmarkCategory.HOSPITAL, 15.3540, 44.2230, listOf("الثورة", "نقم"), "🏥"),
        YemeniLandmark("sn_sabeen", "مستشفى السبعين للأمومة", "صنعاء", LandmarkCategory.HOSPITAL, 15.3310, 44.2010, listOf("السبعين"), "🏥"),
        YemeniLandmark("sn_shumailah", "سوق شميلة", "صنعاء", LandmarkCategory.MARKET, 15.3180, 44.2250, listOf("شميلة", "بين النهدين"), "🛒"),
        YemeniLandmark("sn_hasaba", "جولة الساعة / الحصبة", "صنعاء", LandmarkCategory.ROUNDABOUT, 15.3780, 44.2020, listOf("الساعة", "الحصبة"), "🔄"),
        YemeniLandmark("sn_univ", "جامعة صنعاء القديمة", "صنعاء", LandmarkCategory.UNIVERSITY, 15.3710, 44.1830, listOf("جامعة صنعاء", "الدائري"), "🎓"),
        YemeniLandmark("sn_bab_yemen", "باب اليمن التاريخي", "صنعاء", LandmarkCategory.PUBLIC_BUILDING, 15.3524, 44.2140, listOf("باب اليمن", "صنعاء القديمة"), "🏛️"),

        // ⚓ عدن
        YemeniLandmark("ad_safina", "جولة السفينة (دار سعد)", "عدن", LandmarkCategory.ROUNDABOUT, 12.8730, 44.9850, listOf("السفينة", "دار سعد"), "🔄"),
        YemeniLandmark("ad_qahira", "جولة القاهرة", "عدن", LandmarkCategory.ROUNDABOUT, 12.8550, 45.0020, listOf("القاهرة", "المنصورة"), "🔄"),
        YemeniLandmark("ad_jumhuria", "مستشفى الجمهورية التعليمي", "عدن", LandmarkCategory.HOSPITAL, 12.8220, 45.0350, listOf("الجمهورية", "خور مكسر"), "🏥"),
        YemeniLandmark("ad_sira", "قلعة صيرة", "عدن", LandmarkCategory.PUBLIC_BUILDING, 12.7780, 45.0450, listOf("صيرة", "كريتر"), "🏛️"),
        YemeniLandmark("ad_mall", "عدن مول", "عدن", LandmarkCategory.MARKET, 12.7890, 45.0320, listOf("عدن مول", "صيرة"), "🛒"),
        YemeniLandmark("ad_univ", "جامعة عدن - كلية الطب", "عدن", LandmarkCategory.UNIVERSITY, 12.8280, 45.0290, listOf("جامعة عدن"), "🎓"),

        // ⛰️ تعز
        YemeniLandmark("tz_moror", "جولة المرور", "تعز", LandmarkCategory.ROUNDABOUT, 13.5780, 44.0150, listOf("المرور", "بير باشا"), "🔄"),
        YemeniLandmark("tz_masbah", "جولة المسبح", "تعز", LandmarkCategory.ROUNDABOUT, 13.5820, 44.0230, listOf("المسبح"), "🔄"),
        YemeniLandmark("tz_thawra", "مستشفى الثورة بتعز", "تعز", LandmarkCategory.HOSPITAL, 13.5890, 44.0320, listOf("الثورة"), "🏥"),
        YemeniLandmark("tz_qahira", "قلعة القاهرة", "تعز", LandmarkCategory.PUBLIC_BUILDING, 13.5680, 44.0180, listOf("القاهرة"), "🏛️"),

        // 🌴 المكلا (حضرموت)
        YemeniLandmark("mk_dalh", "جولة الدلة", "المكلا", LandmarkCategory.ROUNDABOUT, 14.5360, 49.1280, listOf("الدلة", "الخور"), "🔄"),
        YemeniLandmark("mk_khor", "خور المكلا", "المكلا", LandmarkCategory.PUBLIC_BUILDING, 14.5320, 49.1310, listOf("الخور"), "🏛️"),
        YemeniLandmark("mk_ibnsina", "مستشفى ابن سينا", "المكلا", LandmarkCategory.HOSPITAL, 14.5150, 49.0950, listOf("ابن سينا"), "🏥"),

        // 🌊 الحديدة
        YemeniLandmark("hd_saah", "دوار الساعة", "الحديدة", LandmarkCategory.ROUNDABOUT, 14.7980, 42.9550, listOf("الساعة"), "🔄"),
        YemeniLandmark("hd_thawra", "مستشفى الثورة بالحديدة", "الحديدة", LandmarkCategory.HOSPITAL, 14.8050, 42.9680, listOf("الثورة"), "🏥")
    )

    private val customLandmarks = mutableListOf<YemeniLandmark>()

    fun getLandmarksForCity(city: String): List<YemeniLandmark> {
        val normalizedCity = city.trim().replace("محافظة", "").replace("مدينة", "").trim()
        val all = predefinedLandmarks + customLandmarks
        val matched = all.filter { it.cityId.contains(normalizedCity, ignoreCase = true) || normalizedCity.contains(it.cityId, ignoreCase = true) }
        return if (matched.isNotEmpty()) matched else predefinedLandmarks.take(6)
    }

    fun getAllLandmarks(): List<YemeniLandmark> = predefinedLandmarks + customLandmarks

    fun findLandmarkById(id: String): YemeniLandmark? {
        return (predefinedLandmarks + customLandmarks).find { it.id == id }
    }

    fun addCustomLandmark(landmark: YemeniLandmark) {
        if (customLandmarks.none { it.id == landmark.id }) {
            customLandmarks.add(landmark)
        }
    }
}
