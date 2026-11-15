package com.serviseyem.ui.screens.map.components

/**
 * 🗺️ نماذج وبيانات الشبكة الجغرافية الحقيقية للمحافظات والمدن اليمنية (الحدائق، الشوارع، الجولات، الأحياء)
 */
internal data class GeoParkData(
    val name: String,
    val lat: Double,
    val lng: Double,
    val wM: Float,
    val hM: Float
)

internal data class GeoRoadSegment(
    val name: String,
    val isHighway: Boolean,
    val points: List<Pair<Double, Double>>
)

internal data class GeoRoundaboutData(
    val name: String,
    val lat: Double,
    val lng: Double,
    val isHighlight: Boolean = false
)

internal data class GeoDistrictData(
    val name: String,
    val lat: Double,
    val lng: Double,
    val emoji: String
)

internal fun getCityParks(selectedCity: String): List<GeoParkData> = when {
    selectedCity.contains("عدن") -> listOf(
        GeoParkData("ساحل أبين وكورنيش خور مكسر 🏖️", 12.8050, 45.0450, 1200f, 400f),
        GeoParkData("حديقة النصر العامة 🌲", 12.8350, 45.0250, 600f, 450f),
        GeoParkData("متنزه صيرة وبحر صيرة 🌊", 12.7750, 45.0400, 500f, 400f)
    )
    selectedCity.contains("تعز") -> listOf(
        GeoParkData("حديقة الشعب العامة 🌲", 13.5820, 44.0150, 500f, 400f),
        GeoParkData("متنزه جبل صبر السياحي ⛰️", 13.5450, 44.0280, 800f, 500f)
    )
    selectedCity.contains("الحديدة") -> listOf(
        GeoParkData("حديقة الشعب بالحديدة 🌲", 14.8020, 42.9600, 600f, 450f),
        GeoParkData("كورنيش الكثيب والساحل 🌊", 14.8250, 42.9400, 1100f, 350f)
    )
    selectedCity.contains("إب") -> listOf(
        GeoParkData("منتجع مشورة الطبيعي 🌲", 13.9850, 44.1350, 750f, 500f),
        GeoParkData("حديقة خليج سري العامة 🌸", 13.9720, 44.1750, 500f, 400f)
    )
    selectedCity.contains("حضرموت") || selectedCity.contains("المكلا") -> listOf(
        GeoParkData("متنزه خور المكلا السياحي 🌊", 14.5360, 49.1280, 900f, 300f),
        GeoParkData("حديقة الطفل بالمكلا 🌲", 14.5420, 49.1350, 450f, 350f)
    )
    selectedCity.contains("مأرب") -> listOf(
        GeoParkData("متنزه سد مأرب التاريخي 🌊", 15.3950, 45.2400, 850f, 550f),
        GeoParkData("حديقة النصر العامة 🌲", 15.4680, 45.3280, 450f, 350f)
    )
    selectedCity.contains("ذمار") -> listOf(
        GeoParkData("حديقة السعادة بذمار 🌲", 14.5480, 44.4080, 500f, 400f),
        GeoParkData("متنزه قرن هران الطبيعي ⛰️", 14.5650, 44.3920, 700f, 450f)
    )
    else -> listOf(
        GeoParkData("حديقة السبعين الكبرى 🌲", 15.3320, 44.2020, 750f, 550f),
        GeoParkData("حديقة الثورة العامة 🌲", 15.3850, 44.2050, 650f, 500f),
        GeoParkData("ميدان التحرير 🏛️", 15.3565, 44.2055, 300f, 250f)
    )
}

internal fun getCityRoads(selectedCity: String): List<GeoRoadSegment> = when {
    selectedCity.contains("عدن") -> listOf(
        GeoRoadSegment("طريق خط الجسر البحري", true, listOf(Pair(12.8350, 45.0150), Pair(12.8150, 45.0250), Pair(12.7980, 45.0350))),
        GeoRoadSegment("شارع مدرم (المعلا الرئيسي)", true, listOf(Pair(12.7880, 44.9950), Pair(12.7870, 45.0150))),
        GeoRoadSegment("شارع أروى (كريتر)", false, listOf(Pair(12.7750, 45.0300), Pair(12.7780, 45.0420))),
        GeoRoadSegment("طريق العقبة التاريخي", false, listOf(Pair(12.7850, 45.0150), Pair(12.7760, 45.0280))),
        GeoRoadSegment("كورنيش ريمي والمنصورة", false, listOf(Pair(12.8450, 45.0120), Pair(12.8250, 45.0150))),
        GeoRoadSegment("شارع التسعين التجاري", true, listOf(Pair(12.8550, 44.9900), Pair(12.8550, 45.0250))),
        GeoRoadSegment("طريق مطار عدن الدولي", true, listOf(Pair(12.8250, 45.0300), Pair(12.8350, 45.0400)))
    )
    selectedCity.contains("تعز") -> listOf(
        GeoRoadSegment("شارع جمال عبدالناصر الرئيسي", true, listOf(Pair(13.5850, 44.0050), Pair(13.5780, 44.0180), Pair(13.5750, 44.0300))),
        GeoRoadSegment("طريق الحوبان العام", true, listOf(Pair(13.6050, 44.0550), Pair(13.5900, 44.0400))),
        GeoRoadSegment("شارع 26 سبتمبر", false, listOf(Pair(13.5820, 44.0150), Pair(13.5750, 44.0150))),
        GeoRoadSegment("طريق قلعة القاهرة السياحي", false, listOf(Pair(13.5750, 44.0180), Pair(13.5650, 44.0180))),
        GeoRoadSegment("شارع وادي القاضي", false, listOf(Pair(13.5900, 44.0100), Pair(13.5820, 44.0120))),
        GeoRoadSegment("طريق بير باشا وجامعة تعز", true, listOf(Pair(13.5780, 43.9850), Pair(13.5800, 44.0050)))
    )
    selectedCity.contains("الحديدة") -> listOf(
        GeoRoadSegment("شارع صنعاء الرئيسي", true, listOf(Pair(14.8100, 42.9850), Pair(14.7950, 42.9650), Pair(14.7900, 42.9550))),
        GeoRoadSegment("كورنيش الحديدة الساحلي", true, listOf(Pair(14.8350, 42.9350), Pair(14.8050, 42.9450), Pair(14.7800, 42.9500))),
        GeoRoadSegment("شارع الميناء التجاري", true, listOf(Pair(14.8050, 42.9450), Pair(14.7950, 42.9500))),
        GeoRoadSegment("شارع جمال بالحديدة", false, listOf(Pair(14.8000, 42.9550), Pair(14.7900, 42.9600))),
        GeoRoadSegment("شارع فلسطين والقدس", false, listOf(Pair(14.8150, 42.9650), Pair(14.8000, 42.9700)))
    )
    selectedCity.contains("إب") -> listOf(
        GeoRoadSegment("شارع تعز العام (إب)", true, listOf(Pair(13.9850, 44.1750), Pair(13.9650, 44.1700), Pair(13.9450, 44.1650))),
        GeoRoadSegment("شارع العدين الرئيسي", true, listOf(Pair(13.9700, 44.1750), Pair(13.9720, 44.1550), Pair(13.9750, 44.1400))),
        GeoRoadSegment("الدائري الغربي بإب", true, listOf(Pair(13.9900, 44.1550), Pair(13.9650, 44.1550))),
        GeoRoadSegment("طريق بعدان ومشورة السياحي", false, listOf(Pair(13.9700, 44.1800), Pair(13.9750, 44.1950))),
        GeoRoadSegment("شارع 22 مايو", false, listOf(Pair(13.9650, 44.1650), Pair(13.9600, 44.1750)))
    )
    selectedCity.contains("حضرموت") || selectedCity.contains("المكلا") -> listOf(
        GeoRoadSegment("شارع الستين البحري (المكلا)", true, listOf(Pair(14.5450, 49.1100), Pair(14.5380, 49.1250), Pair(14.5300, 49.1450))),
        GeoRoadSegment("طريق الخور وجسر بن حبريش", true, listOf(Pair(14.5380, 49.1250), Pair(14.5340, 49.1300))),
        GeoRoadSegment("شارع الشرج التجاري", false, listOf(Pair(14.5420, 49.1200), Pair(14.5360, 49.1220))),
        GeoRoadSegment("طريق فوة العام", true, listOf(Pair(14.5500, 49.0700), Pair(14.5450, 49.1050))),
        GeoRoadSegment("طريق الديس والغليلة", false, listOf(Pair(14.5450, 49.1350), Pair(14.5550, 49.1450)))
    )
    selectedCity.contains("مأرب") -> listOf(
        GeoRoadSegment("الشارع العام الرئيسي بمأرب", true, listOf(Pair(15.4850, 45.3100), Pair(15.4650, 45.3250), Pair(15.4450, 45.3400))),
        GeoRoadSegment("طريق سد مأرب التاريخي", true, listOf(Pair(15.4600, 45.3200), Pair(15.4200, 45.2700))),
        GeoRoadSegment("خط صافر الدولي", true, listOf(Pair(15.4650, 45.3350), Pair(15.4700, 45.3850))),
        GeoRoadSegment("شارع المحافظة والدائري", false, listOf(Pair(15.4700, 45.3150), Pair(15.4550, 45.3300)))
    )
    selectedCity.contains("ذمار") -> listOf(
        GeoRoadSegment("شارع صنعاء - تعز العام", true, listOf(Pair(14.5650, 44.4000), Pair(14.5450, 44.4050), Pair(14.5250, 44.4100))),
        GeoRoadSegment("الدائري الشمالي بذمار", true, listOf(Pair(14.5600, 44.3850), Pair(14.5600, 44.4200))),
        GeoRoadSegment("شارع رداع التجاري", false, listOf(Pair(14.5450, 44.4050), Pair(14.5400, 44.4300))),
        GeoRoadSegment("شارع السوق المركزي", false, listOf(Pair(14.5420, 44.3980), Pair(14.5460, 44.4120)))
    )
    else -> listOf(
        GeoRoadSegment("شارع الستين الغربي", true, listOf(Pair(15.3950, 44.1680), Pair(15.3780, 44.1680), Pair(15.3480, 44.1720), Pair(15.3200, 44.1750))),
        GeoRoadSegment("شارع الستين الشرقي", true, listOf(Pair(15.3950, 44.2250), Pair(15.3650, 44.2250), Pair(15.3350, 44.2200), Pair(15.3000, 44.2200))),
        GeoRoadSegment("شارع الخمسين الجنوبي", true, listOf(Pair(15.3050, 44.1700), Pair(15.3050, 44.1950), Pair(15.3050, 44.2250))),
        GeoRoadSegment("شارع السبعين الرئيسي", true, listOf(Pair(15.3450, 44.2050), Pair(15.3320, 44.2050), Pair(15.3250, 44.2050), Pair(15.3050, 44.2050))),
        GeoRoadSegment("شارع الزبيري الرئيسي", true, listOf(Pair(15.3480, 44.1720), Pair(15.3520, 44.1880), Pair(15.3530, 44.1980), Pair(15.3530, 44.2150))),
        GeoRoadSegment("شارع الدائري الغربي", false, listOf(Pair(15.3800, 44.1780), Pair(15.3680, 44.1830), Pair(15.3620, 44.1865), Pair(15.3585, 44.1880), Pair(15.3520, 44.1880), Pair(15.3420, 44.1890))),
        GeoRoadSegment("شارع حدة الرئيسي", false, listOf(Pair(15.3520, 44.1980), Pair(15.3400, 44.1940), Pair(15.3280, 44.1920), Pair(15.3100, 44.1850))),
        GeoRoadSegment("شارع هائل التجاري", false, listOf(Pair(15.3700, 44.1830), Pair(15.3620, 44.1830), Pair(15.3520, 44.1840))),
        GeoRoadSegment("شارع الرقاص", false, listOf(Pair(15.3700, 44.1800), Pair(15.3600, 44.1810), Pair(15.3520, 44.1820))),
        GeoRoadSegment("شارع بغداد", false, listOf(Pair(15.3520, 44.1920), Pair(15.3440, 44.1920), Pair(15.3380, 44.1920))),
        GeoRoadSegment("شارع الجزائر", false, listOf(Pair(15.3400, 44.1880), Pair(15.3400, 44.1950), Pair(15.3400, 44.2050))),
        GeoRoadSegment("شارع عمان", false, listOf(Pair(15.3480, 44.2000), Pair(15.3320, 44.2000))),
        GeoRoadSegment("شارع علي عبدالمغني", false, listOf(Pair(15.3600, 44.2050), Pair(15.3530, 44.2080), Pair(15.3520, 44.2120))),
        GeoRoadSegment("شارع المطار", true, listOf(Pair(15.3850, 44.2150), Pair(15.4200, 44.2200), Pair(15.4600, 44.2250))),
        GeoRoadSegment("شارع تعز العام", true, listOf(Pair(15.3520, 44.2160), Pair(15.3350, 44.2160), Pair(15.3200, 44.2160), Pair(15.2950, 44.2200))),
        GeoRoadSegment("شارع مأرب والنصر", true, listOf(Pair(15.3850, 44.2200), Pair(15.3850, 44.2600)))
    )
}

internal fun getCityRoundabouts(selectedCity: String): List<GeoRoundaboutData> = when {
    selectedCity.contains("عدن") -> listOf(
        GeoRoundaboutData("جولة كالتكس (المنصورة)", 12.8350, 45.0150, true),
        GeoRoundaboutData("جولة ريجل (خور مكسر)", 12.8120, 45.0350, true),
        GeoRoundaboutData("قلعة صيرة التاريخية 🏰", 12.7720, 45.0420, true),
        GeoRoundaboutData("جولة الفل (كريتر)", 12.7780, 45.0320),
        GeoRoundaboutData("دوار المعلا الرئيسي", 12.7880, 45.0080),
        GeoRoundaboutData("جولة سوزوكي والشيخ عثمان", 12.8620, 45.0280)
    )
    selectedCity.contains("تعز") -> listOf(
        GeoRoundaboutData("جولة العواضي (شارع جمال)", 13.5780, 44.0180, true),
        GeoRoundaboutData("قلعة القاهرة الأثرية 🏰", 13.5650, 44.0180, true),
        GeoRoundaboutData("جولة سنان 📍", 13.5820, 44.0120),
        GeoRoundaboutData("دوار الحوبان المركزي", 13.6020, 44.0480, true),
        GeoRoundaboutData("باب موسى التاريخي 🏛️", 13.5730, 44.0160),
        GeoRoundaboutData("جولة المسبح", 13.5850, 44.0150)
    )
    selectedCity.contains("الحديدة") -> listOf(
        GeoRoundaboutData("دوار الساعة الشهير ⏰", 14.7950, 42.9550, true),
        GeoRoundaboutData("ميناء الحديدة الدولي ⚓", 14.8050, 42.9450, true),
        GeoRoundaboutData("جولة الكورنيش الساحلي", 14.8150, 42.9400),
        GeoRoundaboutData("جولة يمن موبايل", 14.7980, 42.9650),
        GeoRoundaboutData("دوار الصدفة", 14.8250, 42.9380)
    )
    selectedCity.contains("إب") -> listOf(
        GeoRoundaboutData("جولة العدين المركزية 📍", 13.9720, 44.1550, true),
        GeoRoundaboutData("منتجع جبل ربي ⛰️", 13.9550, 44.1720, true),
        GeoRoundaboutData("جولة الصوفية", 13.9680, 44.1680),
        GeoRoundaboutData("دوار المعاين", 13.9850, 44.1600),
        GeoRoundaboutData("جولة سبلة", 13.9620, 44.1780)
    )
    selectedCity.contains("حضرموت") || selectedCity.contains("المكلا") -> listOf(
        GeoRoundaboutData("خور المكلا وجسر الشهيد 🌉", 14.5380, 49.1250, true),
        GeoRoundaboutData("حصن الغويزي التاريخي 🏰", 14.5580, 49.1480, true),
        GeoRoundaboutData("جولة الدلة المركزية ☕", 14.5420, 49.1200, true),
        GeoRoundaboutData("منارة المكلا التاريخية 🗼", 14.5320, 49.1380),
        GeoRoundaboutData("دوار فوة السكني", 14.5480, 49.0950)
    )
    selectedCity.contains("مأرب") -> listOf(
        GeoRoundaboutData("دوار المدينة المركزي 📍", 15.4650, 45.3250, true),
        GeoRoundaboutData("عرش بلقيس الأثري 🏛️", 15.4380, 45.3350, true),
        GeoRoundaboutData("معبد أوام التاريخي 🏛️", 15.4280, 45.3420, true),
        GeoRoundaboutData("سد مأرب العظيم 🌊", 15.3950, 45.2400, true),
        GeoRoundaboutData("جولة النصر", 15.4720, 45.3200)
    )
    selectedCity.contains("ذمار") -> listOf(
        GeoRoundaboutData("جولة الميدان العام 📍", 14.5450, 44.4050, true),
        GeoRoundaboutData("الجامع الكبير التاريخي 🕌", 14.5420, 44.4080, true),
        GeoRoundaboutData("دوار المحافظة", 14.5550, 44.4020),
        GeoRoundaboutData("جولة عمارة النصر", 14.5480, 44.4150)
    )
    else -> listOf(
        GeoRoundaboutData("جولة الضبيبي 📍", 15.3585, 44.1880, true),
        GeoRoundaboutData("جامعة صنعاء القديمة 🏛️", 15.3620, 44.1865, true),
        GeoRoundaboutData("جولة كنتاكي / الرويشان", 15.3510, 44.1980),
        GeoRoundaboutData("جولة المصباحي (حدة)", 15.3280, 44.1920),
        GeoRoundaboutData("جولة عصر", 15.3460, 44.1720),
        GeoRoundaboutData("جولة مذبح", 15.3780, 44.1680),
        GeoRoundaboutData("ميدان التحرير 🏛️", 15.3565, 44.2055),
        GeoRoundaboutData("باب اليمن التاريخي 🏛️", 15.3530, 44.2155),
        GeoRoundaboutData("ميدان السبعين وجامع الصالح 🕌", 15.3250, 44.2050),
        GeoRoundaboutData("جولة شميله", 15.3200, 44.2160),
        GeoRoundaboutData("جولة سبأ", 15.3720, 44.2120),
        GeoRoundaboutData("جولة القادسية", 15.3380, 44.1820)
    )
}

internal fun getCityDistricts(selectedCity: String): List<GeoDistrictData> = when {
    selectedCity.contains("عدن") -> listOf(
        GeoDistrictData("حي كريتر التاريخي", 12.7750, 45.0320, "🏛️"),
        GeoDistrictData("حي المعلا", 12.7880, 45.0050, "🚢"),
        GeoDistrictData("حي التواهي", 12.7950, 44.9850, "🌊"),
        GeoDistrictData("حي خور مكسر", 12.8150, 45.0350, "✈️"),
        GeoDistrictData("حي المنصورة", 12.8450, 45.0150, "🏙️"),
        GeoDistrictData("حي الشيخ عثمان", 12.8650, 45.0300, "🛍️"),
        GeoDistrictData("حي البريقة", 12.7500, 44.8850, "🏖️"),
        GeoDistrictData("حي دار سعد", 12.8900, 45.0250, "🏡")
    )
    selectedCity.contains("تعز") -> listOf(
        GeoDistrictData("حي القاهرة", 13.5780, 44.0150, "🏰"),
        GeoDistrictData("حي المظفر", 13.5820, 44.0080, "🏛️"),
        GeoDistrictData("حي الحوبان", 13.6050, 44.0500, "🏭"),
        GeoDistrictData("حي صالة", 13.5800, 44.0350, "🌳"),
        GeoDistrictData("حي بير باشا", 13.5780, 43.9850, "🎓"),
        GeoDistrictData("حي وادي القاضي", 13.5900, 44.0100, "🏡"),
        GeoDistrictData("حي الجمهوري", 13.5700, 44.0250, "🏥")
    )
    selectedCity.contains("الحديدة") -> listOf(
        GeoDistrictData("حي الميناء", 14.8050, 42.9450, "⚓"),
        GeoDistrictData("حي الحوك", 14.7850, 42.9550, "🌊"),
        GeoDistrictData("حي الحالي", 14.8050, 42.9750, "🌴"),
        GeoDistrictData("حي 7 يوليو", 14.8150, 42.9850, "🏙️"),
        GeoDistrictData("حي غليل", 14.7950, 42.9650, "🛍️")
    )
    selectedCity.contains("إب") -> listOf(
        GeoDistrictData("حي الظهار", 13.9750, 44.1650, "🏙️"),
        GeoDistrictData("حي المشنة", 13.9650, 44.1750, "🏞️"),
        GeoDistrictData("مدينة جبلة التاريخية", 13.9200, 44.1480, "👑"),
        GeoDistrictData("حي المعاين", 13.9880, 44.1580, "🏡"),
        GeoDistrictData("حي السبل", 13.9620, 44.1720, "✨")
    )
    selectedCity.contains("حضرموت") || selectedCity.contains("المكلا") -> listOf(
        GeoDistrictData("حي المكلا القديمة", 14.5320, 49.1350, "🏛️"),
        GeoDistrictData("حي الشرج", 14.5420, 49.1200, "🛍️"),
        GeoDistrictData("حي الديس", 14.5480, 49.1380, "🏙️"),
        GeoDistrictData("حي فوه", 14.5500, 49.0850, "🏖️"),
        GeoDistrictData("حي روكب", 14.5650, 49.1850, "🌊")
    )
    selectedCity.contains("مأرب") -> listOf(
        GeoDistrictData("حي المدينة المركزي", 15.4650, 45.3250, "🏛️"),
        GeoDistrictData("حي الروضة", 15.4850, 45.3150, "🏡"),
        GeoDistrictData("منطقة الوادي", 15.4500, 45.3650, "🌴"),
        GeoDistrictData("حي الجفينة", 15.4350, 45.3050, "⛺")
    )
    selectedCity.contains("ذمار") -> listOf(
        GeoDistrictData("حي الميدان المركزي", 14.5450, 44.4050, "🛍️"),
        GeoDistrictData("حي السوق والجمارك", 14.5400, 44.3980, "🏢"),
        GeoDistrictData("حي روضة ذمار", 14.5550, 44.4150, "🏡"),
        GeoDistrictData("حي هران", 14.5680, 44.3900, "⛰️")
    )
    else -> listOf(
        GeoDistrictData("حي جامعة صنعاء", 15.3650, 44.1850, "🎓"),
        GeoDistrictData("حي هائل التجاري", 15.3620, 44.1820, "🛍️"),
        GeoDistrictData("حي الدائري", 15.3560, 44.1890, "🏙️"),
        GeoDistrictData("حي حدة الراقي", 15.3300, 44.1880, "✨"),
        GeoDistrictData("صنعاء القديمة التاريخية", 15.3550, 44.2160, "🏛️"),
        GeoDistrictData("حي التحرير", 15.3570, 44.2050, "🏢"),
        GeoDistrictData("حي السبعين", 15.3350, 44.2050, "🌳"),
        GeoDistrictData("حي عصر", 15.3420, 44.1690, "⛰️"),
        GeoDistrictData("حي المذبح", 15.3850, 44.1680, "🏡"),
        GeoDistrictData("حي الحصبة", 15.3820, 44.2050, "🏙️"),
        GeoDistrictData("حي الروضة", 15.4200, 44.2150, "🌿"),
        GeoDistrictData("حي شميله", 15.3200, 44.2160, "🛒"),
        GeoDistrictData("حي الأصبحي", 15.3050, 44.2100, "🏘️"),
        GeoDistrictData("حي بيت بوس", 15.2950, 44.1950, "🏰"),
        GeoDistrictData("حي نقم وشيراتون", 15.3650, 44.2300, "🏨"),
        GeoDistrictData("حي الصافية", 15.3450, 44.2120, "🏡")
    )
}
