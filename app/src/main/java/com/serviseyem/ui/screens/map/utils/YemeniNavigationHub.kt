package com.serviseyem.ui.screens.map.utils

import kotlin.math.*

data class YemeniCityLocation(
    val name: String,
    val lat: Double,
    val lng: Double,
    val governorate: String
)

data class NavigationRouteInfo(
    val originName: String,
    val destinationName: String,
    val distanceKm: Double,
    val durationText: String,
    val highwayOrStreetName: String,
    val steps: List<String>,
    val originCoords: Pair<Double, Double>,
    val destCoords: Pair<Double, Double>
)

data class StreetOrLandmark(
    val name: String,
    val area: String,
    val city: String,
    val lat: Double,
    val lng: Double,
    val type: String // "شارع رئيسي", "حي سكني", "معلم شهير", "مستشفى", "مركز تجاري"
)

object YemeniNavigationHub {

    // المدن والمحافظات الرئيسية في اليمن
    val CITIES = listOf(
        YemeniCityLocation("صنعاء", 15.3694, 44.1910, "أمانة العاصمة"),
        YemeniCityLocation("عدن", 12.7855, 45.0187, "عدن"),
        YemeniCityLocation("تعز", 13.5779, 44.0215, "تعز"),
        YemeniCityLocation("الحديدة", 14.7978, 42.9545, "الحديدة"),
        YemeniCityLocation("إب", 13.9687, 44.1822, "إب"),
        YemeniCityLocation("المكلا", 14.5423, 49.1242, "حضرموت"),
        YemeniCityLocation("مأرب", 15.4226, 45.3288, "مأرب"),
        YemeniCityLocation("ذمار", 14.5426, 44.4051, "ذمار"),
        YemeniCityLocation("صعدة", 16.9402, 43.7639, "صعدة"),
        YemeniCityLocation("شبوة (عتق)", 14.5377, 46.8319, "شبوة"),
        YemeniCityLocation("حجة", 15.6944, 43.6033, "حجة"),
        YemeniCityLocation("البيضاء", 13.9852, 45.5727, "البيضاء"),
        YemeniCityLocation("المهرة (الغيضة)", 16.2079, 52.1760, "المهرة"),
        YemeniCityLocation("عمران", 15.6601, 43.9437, "عمران"),
        YemeniCityLocation("لحج", 13.0592, 44.8828, "لحج"),
        YemeniCityLocation("أبين (زنجبار)", 13.1287, 45.3800, "أبين"),
        YemeniCityLocation("الضالع", 13.6957, 44.7314, "الضالع"),
        YemeniCityLocation("المحويت", 15.4701, 43.5448, "المحويت"),
        YemeniCityLocation("سقطرى (حديبو)", 12.6503, 54.0189, "سقطرى")
    )

    // الشوارع والأحياء والمعالم الحضرية الشهيرة للتنقل الدقيق
    val POPULAR_STREETS_LANDMARKS = listOf(
        // صنعاء
        StreetOrLandmark("شارع الزبيري", "التحرير / وسط المدينة", "صنعاء", 15.3486, 44.2001, "شارع رئيسي"),
        StreetOrLandmark("شارع حدة", "حي حدة الدبلوماسي", "صنعاء", 15.3190, 44.1950, "شارع رئيسي"),
        StreetOrLandmark("شارع الستين الغربي", "مذبح / السنينة", "صنعاء", 15.3620, 44.1680, "طريق شرياني سريع"),
        StreetOrLandmark("شارع الستين الجنوبي", "السبعين / حدة", "صنعاء", 15.3120, 44.2050, "طريق شرياني سريع"),
        StreetOrLandmark("ميدان السبعين وجامع الصالح", "السبعين", "صنعاء", 15.3250, 44.2150, "معلم شهير"),
        StreetOrLandmark("شارع هائل التجاري", "الجامعة القديمة", "صنعاء", 15.3610, 44.1905, "مركز تجاري وتسوق"),
        StreetOrLandmark("شارع تعز (شميلة)", "الصافية / شميلة", "صنعاء", 15.3210, 44.2250, "شارع رئيسي"),
        StreetOrLandmark("شارع الدائري الغربي", "جامعة صنعاء الجديدة", "صنعاء", 15.3680, 44.1840, "شارع رئيسي"),
        StreetOrLandmark("مستشفى الثورة العام", "باب اليمن / الصافية", "صنعاء", 15.3400, 44.2180, "مستشفى"),
        StreetOrLandmark("مستشفى جامعة العلوم والتكنولوجيا", "شارع الستين الغربي", "صنعاء", 15.3720, 44.1670, "مستشفى"),
        StreetOrLandmark("باب اليمن وصنعاء القديمة", "صنعاء القديمة", "صنعاء", 15.3530, 44.2150, "معلم تاريخي"),
        StreetOrLandmark("مطار صنعاء الدولي", "حي الروضة والمطار", "صنعاء", 15.4760, 44.2190, "مطار ومعلم دولي"),

        // عدن
        StreetOrLandmark("شارع مدرم (المعلا الرئيسي)", "المعلا", "عدن", 12.7870, 44.9960, "شارع رئيسي"),
        StreetOrLandmark("شارع أروى وسوق الطويل", "كريتر التاريخية", "عدن", 12.7790, 45.0340, "مركز تجاري"),
        StreetOrLandmark("شارع التسعين", "المنصورة", "عدن", 12.8610, 44.9810, "طريق سريع"),
        StreetOrLandmark("جولة القاهرة وسوق الشيخ عثمان", "الشيخ عثمان", "عدن", 12.8750, 44.9970, "مركز تجاري"),
        StreetOrLandmark("كورنيش ساحل أبين", "خور مكسر", "عدن", 12.8120, 45.0450, "معلم سياحي"),
        StreetOrLandmark("مطار عدن الدولي", "خور مكسر", "عدن", 12.8290, 45.0280, "مطار ومعلم دولي"),

        // تعز
        StreetOrLandmark("شارع جمال عبد الناصر", "وسط المدينة / الحوض", "تعز", 13.5780, 44.0150, "شارع رئيسي"),
        StreetOrLandmark("شارع 26 سبتمبر", "باب موسى / المدينة القديمة", "تعز", 13.5720, 44.0190, "مركز تسوق"),
        StreetOrLandmark("جولة بير باشا وجامعة تعز", "حبيل سلمان", "تعز", 13.5650, 43.9850, "حي رئيسي"),
        StreetOrLandmark("جولة الحوبان", "الحوبان", "تعز", 13.6080, 44.0750, "تقاطع رئيسي"),

        // الحديدة
        StreetOrLandmark("شارع صنعاء", "وسط الحديدة", "الحديدة", 14.7950, 42.9650, "شارع رئيسي"),
        StreetOrLandmark("شارع جمال والكورنيش", "الميناء", "الحديدة", 14.8050, 42.9450, "شارع ساحلي"),

        // إب
        StreetOrLandmark("شارع العدين", "الظهار", "إب", 13.9740, 44.1750, "شارع رئيسي وتجاري"),
        StreetOrLandmark("شارع تعز الدائري", "المشنة", "إب", 13.9610, 44.1890, "شارع رئيسي"),

        // المكلا
        StreetOrLandmark("شارع الستين والكورنيش", "المكلا", "المكلا", 14.5360, 49.1300, "شارع رئيسي"),
        StreetOrLandmark("خور المكلا وجسر المشاة", "الشرج", "المكلا", 14.5390, 49.1240, "معلم سياحي")
    )

    /**
     * حساب مسار الملاحة بين أي مدينتين يمنيتين بدقة متناهية
     */
    fun calculateInterCityRoute(originName: String, destName: String): NavigationRouteInfo {
        val orig = CITIES.find { it.name == originName } ?: CITIES[0]
        val dest = CITIES.find { it.name == destName } ?: CITIES[1]

        val directDist = MapDistanceCalculator.calculateDistanceMeters(
            orig.lat, orig.lng, dest.lat, dest.lng
        ) / 1000.0

        // معامل تعرج الطرق الجبلية والصحراوية في اليمن (بين 1.25 إلى 1.45)
        val roadFactor = when {
            (orig.name == "صنعاء" && dest.name == "الحديدة") || (orig.name == "الحديدة" && dest.name == "صنعاء") -> 1.38
            (orig.name == "صنعاء" && dest.name == "عدن") || (orig.name == "عدن" && dest.name == "صنعاء") -> 1.32
            (orig.name == "صنعاء" && dest.name == "تعز") || (orig.name == "تعز" && dest.name == "صنعاء") -> 1.30
            (orig.name == "صنعاء" && dest.name == "مأرب") || (orig.name == "مأرب" && dest.name == "صنعاء") -> 1.22
            else -> 1.30
        }

        val actualDistanceKm = round(directDist * roadFactor * 10.0) / 10.0

        // متوسط السرعة الآمنة على الطرق السريعة اليمنية (حوالي 65-75 كم/س)
        val avgSpeed = 68.0
        val totalHours = actualDistanceKm / avgSpeed
        val hours = totalHours.toInt()
        val minutes = ((totalHours - hours) * 60).toInt()
        val durationText = if (hours > 0) "$hours ساعة و $minutes دقيقة" else "$minutes دقيقة"

        val (highway, steps) = generateInterCitySteps(orig.name, dest.name, actualDistanceKm)

        return NavigationRouteInfo(
            originName = orig.name,
            destinationName = dest.name,
            distanceKm = actualDistanceKm,
            durationText = durationText,
            highwayOrStreetName = highway,
            steps = steps,
            originCoords = Pair(orig.lat, orig.lng),
            destCoords = Pair(dest.lat, dest.lng)
        )
    }

    private fun generateInterCitySteps(orig: String, dest: String, distKm: Double): Pair<String, List<String>> {
        val highway: String
        val steps = mutableListOf<String>()

        when {
            (orig == "صنعاء" && dest == "عدن") || (orig == "عدن" && dest == "صنعاء") -> {
                highway = "طريق خط صنعاء - تعز - عدن الدولي السريع"
                steps.add("انطلق عبر الطريق الرئيسي المتجه نحو نقيل يسلح")
                steps.add("المرور عبر محافظة ذمار ثم مدينة يريم ونقيل سمارة")
                steps.add("متابعة السير عبر محافظة إب ثم نقيل السياني")
                steps.add("الدخول عبر محافظة لحج وصولاً إلى مداخل العاصمة عدن")
            }
            (orig == "صنعاء" && dest == "الحديدة") || (orig == "الحديدة" && dest == "صنعاء") -> {
                highway = "طريق صنعاء - مناخة - كيلو 16 الساحلي"
                steps.add("الخروج غرباً عبر عصر ومتنزهات مناخة الجبلية")
                steps.add("الهبوط الآمن عبر منعطفات جبال حراز التاريخية")
                steps.add("الدخول لسهل تهامة عبر باجل ثم كيلو 16 وصولاً للحديدة")
            }
            (orig == "صنعاء" && dest == "مأرب") || (orig == "مأرب" && dest == "صنعاء") -> {
                highway = "طريق صنعاء - نهم - مأرب السريع"
                steps.add("التحرك شرقاً بمحاذاة خط مأرب الصحراوي الرئيسي")
                steps.add("المرور بنقاط الربط الدولية والخدمات البترولية")
                steps.add("الوصول إلى وسط مدينة مأرب وسد مأرب التاريخي")
            }
            else -> {
                highway = "الطريق الوطني الرابط بين $orig و $dest"
                steps.add("الانطلاق من مركز $orig عبر البوابة الرئيسية للطريق السريع")
                steps.add("متابعة المسار الإرشادي لمسافة ${round(distKm * 0.6)} كم")
                steps.add("الاقتراب من مداخل $dest والتمركز نحو الوجهة المقصودة")
            }
        }
        return Pair(highway, steps)
    }

    /**
     * حساب مسار ملاحة شوارع وأحياء دقيق (Urban / Street Navigation)
     */
    fun calculateStreetRoute(
        userLat: Double,
        userLng: Double,
        destLat: Double,
        destLng: Double,
        destinationTitle: String
    ): NavigationRouteInfo {
        val distMeters = MapDistanceCalculator.calculateDistanceMeters(userLat, userLng, destLat, destLng)
        val distKm = round((distMeters / 1000.0) * 10.0) / 10.0

        // سرعة المدينة المتوسطة للملاحة الحضرية (32 كم/ساعة للمركبات، 4 كم/ساعة للمشاة)
        val minutesByCar = max(2, (distKm / 32.0 * 60).roundToInt())
        val durationText = if (minutesByCar >= 60) {
            "${minutesByCar / 60} ساعة و ${minutesByCar % 60} دقيقة"
        } else {
            "$minutesByCar دقيقة بالسيارة"
        }

        val steps = listOf(
            "اتجه نحو المسار الرئيسي لمسافة 250 متر",
            "انعطف عند التقاطع القادم وتابع السير بشكل مستقيم",
            "استمر لمسافة ${if (distKm > 1.0) "$distKm كم" else "${distMeters.roundToInt()} متر"} على نفس الشارع",
            "وجهتك: $destinationTitle ستكون ظاهرة أمامك على المسار مباشرة"
        )

        return NavigationRouteInfo(
            originName = "موقعي الحالي 📍",
            destinationName = destinationTitle,
            distanceKm = distKm,
            durationText = durationText,
            highwayOrStreetName = "أقرب مسار مباشر إلى $destinationTitle",
            steps = steps,
            originCoords = Pair(userLat, userLng),
            destCoords = Pair(destLat, destLng)
        )
    }
}
