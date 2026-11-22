package com.serviseyem.data.models

import androidx.annotation.Keep

@Keep
data class QuickServiceConfig(
    val isQuickServiceActive: Boolean = true,
    val quickServiceTitle: String = "اطلب خدمتك الفورية ⚡",
    val quickServiceDescription: String = "أرسل طلبك وسيصلك أقرب مقدم خدمة معتمد في دقائق",
    val allowAudioRecording: Boolean = true,
    val allowImageUpload: Boolean = true,
    val allowLiveLocation: Boolean = true,
    val maxImagesAllowed: String = "3",
    val maxAudioSeconds: String = "60",
    val broadcastRadiusKm: String = "10",
    val defaultResponseWindowMinutes: String = "5"
)

@Keep
data class PaymentSystemConfig(
    val systemEnabled: Boolean = true,
    val linkBookings: Boolean = true,
    val linkStores: Boolean = true,
    val linkRestaurants: Boolean = true,
    val linkMedical: Boolean = true,
    val linkProperties: Boolean = true,
    val linkJobs: Boolean = true
)

@Keep
data class AutoRoutingConfig(
    val isGeneralAutoRoutingEnabled: Boolean = true,
    val isStoresRoutingEnabled: Boolean = true,
    val storesCriteria: String = "الأقرب جغرافياً (GPS)",
    val storesRadiusKm: String = "15",
    val isMallsRoutingEnabled: Boolean = true,
    val mallsCriteria: String = "الأقرب داخل نفس المدينة",
    val isRestaurantsRoutingEnabled: Boolean = true,
    val restaurantsCriteria: String = "أقرب مطعم مع أسرع تحضير",
    val restaurantsRadiusKm: String = "10",
    val isMedicalRoutingEnabled: Boolean = true,
    val medicalCriteria: String = "أقرب طوارئ وتوفر التخصص",
    val isDeliveryRoutingEnabled: Boolean = true,
    val deliveryCriteria: String = "إسناد تلقائي لأقرب مندوب شاغر",
    val deliveryMaxRadiusKm: String = "25"
)

@Keep
data class RegistrationCustomField(
    val id: String = "",
    val sectionKey: String = "الفنيين والصيانة",
    val label: String = "",
    val fieldType: String = "نص", // نص، رقم، هاتف، قائمة منسدلة
    val isRequired: Boolean = true
)

@Keep
data class RegistrationFormsConfig(
    val termsBySection: Map<String, String> = DEFAULT_TERMS,
    val customFields: List<RegistrationCustomField> = emptyList()
) {
    companion object {
        val SECTIONS = listOf(
            "الفنيين والصيانة",
            "المحلات التجارية",
            "المطاعم",
            "المراكز الطبية",
            "العقارات",
            "المراكز التجارية والأسواق"
        )

        val DEFAULT_TERMS = mapOf(
            "الفنيين والصيانة" to "1. إرفاق صور واضحة للأعمال والهوية. 2. الالتزام بتسعيرة المنصة والمواعيد المحددة.",
            "المحلات التجارية" to "1. مطابقة الاسم التجاري والموقع الجغرافي للواقع. 2. تحديث حالة توفر المنتجات والأسعار.",
            "المطاعم" to "1. الالتزام بمعايير النظافة وسلامة الغذاء. 2. الالتزام بزمن التجهيز والتوصيل المحدد.",
            "المراكز الطبية" to "1. إدخال التخصص الطبي والترخيص الساري. 2. الالتزام بمواعيد العيادات والطوارئ.",
            "العقارات" to "1. إرفاق صور حقيقية وحديثة للعقار. 2. توضيح السعر والعملة بدقة.",
            "المراكز التجارية والأسواق" to "1. تحديد موقع السوق بدقة على الخريطة. 2. الالتزام بسياسة العرض والتوصيل."
        )
    }
}

@Keep
data class PlatformHomeSection(
    val id: String = "",
    val titleAr: String = "",
    val titleEn: String = "",
    val emoji: String = "✨",
    val subtitleAr: String = "",
    val routeKey: String = "",
    val isVisible: Boolean = true,
    val isBuiltIn: Boolean = true,
    val order: Int = 0,
    val linkToMap: Boolean = true,
    val linkToOrders: Boolean = true,
    val linkToPayment: Boolean = false,
    val enableBookings: Boolean = true,
    val sectionType: String = "خدمات وصيانة"
) {
    companion object {
        val DEFAULT_SECTIONS = listOf(
            PlatformHomeSection(
                id = "tab_services",
                titleAr = "الخدمات الرئيسية",
                titleEn = "Main Services",
                emoji = "🛠️",
                subtitleAr = "فنيو الصيانة والخدمات المنزلية والهندسية",
                routeKey = "SERVICES_HOME",
                isVisible = true,
                isBuiltIn = true,
                order = 0
            ),
            PlatformHomeSection(
                id = "tab_stores",
                titleAr = "المحلات",
                titleEn = "Stores",
                emoji = "🏪",
                subtitleAr = "المتاجر والأسواق المحلية",
                routeKey = "STORES",
                isVisible = true,
                isBuiltIn = true,
                order = 1
            ),
            PlatformHomeSection(
                id = "tab_malls",
                titleAr = "المراكز التجارية",
                titleEn = "Commercial Centers",
                emoji = "🏬",
                subtitleAr = "المولات والأسواق المركزية",
                routeKey = "COMMERCIAL_CENTERS",
                isVisible = true,
                isBuiltIn = true,
                order = 2
            ),
            PlatformHomeSection(
                id = "tab_restaurants",
                titleAr = "المطاعم",
                titleEn = "Restaurants",
                emoji = "🍽️",
                subtitleAr = "المطاعم والكافيهات وتوصيل الوجبات",
                routeKey = "RESTAURANTS",
                isVisible = true,
                isBuiltIn = true,
                order = 3
            ),
            PlatformHomeSection(
                id = "tab_medical",
                titleAr = "المراكز الطبية",
                titleEn = "Medical Centers",
                emoji = "🏥",
                subtitleAr = "المستشفيات والعيادات والمختبرات",
                routeKey = "MEDICAL",
                isVisible = true,
                isBuiltIn = true,
                order = 4
            ),
            PlatformHomeSection(
                id = "tab_properties",
                titleAr = "العقارات",
                titleEn = "Real Estate",
                emoji = "🏢",
                subtitleAr = "شقق، فلل، أراضٍ ومحلات للبيع والإيجار",
                routeKey = "PROPERTIES",
                isVisible = true,
                isBuiltIn = true,
                order = 5
            ),
            PlatformHomeSection(
                id = "tab_jobs",
                titleAr = "الوظائف",
                titleEn = "Jobs",
                emoji = "💼",
                subtitleAr = "فرص العمل والباحثون عن عمل",
                routeKey = "JOBS",
                isVisible = true,
                isBuiltIn = true,
                order = 6
            ),
            PlatformHomeSection(
                id = "tab_favorites",
                titleAr = "المفضلة",
                titleEn = "Favorites",
                emoji = "❤️",
                subtitleAr = "الجهات والفنيون المحفوظون",
                routeKey = "FAVORITES",
                isVisible = true,
                isBuiltIn = true,
                order = 7
            )
        )
    }
}

@Keep
data class MapLayerSpec(
    val id: String,
    val titleAr: String,
    val emoji: String,
    val group: String,
    val descriptionAr: String
)

object MapLayersCatalog {
    val ALL_LAYERS = listOf(
        MapLayerSpec("VELA_3D", "طبقة فيلا ثلاثية الأبعاد 3D", "🏔️", "MODES", "تضاريس جبلية ومبانٍ ثلاثية الأبعاد عالية الدقة"),
        MapLayerSpec("AERIAL_OAM", "صور جوية عالية الدقة (OAM)", "✈️", "BASE", "صور جوية وطائرات مسيرة للمدن اليمنية"),
        MapLayerSpec("AR_VPS", "الواقع المعزز والملاحة البصرية (AR)", "📷", "MODES", "توجيه الأسهم المباشر عبر الكاميرا"),
        MapLayerSpec("NAVIGATION", "الملاحة والتوجيه الذكي بالصوت", "🧭", "MODES", "حساب المسار الأسرع والأكثر أماناً مع بدائل الطرق"),
        MapLayerSpec("ALEPH_AI", "مستكشف الخرائط الذكي (AI)", "🧠", "MODES", "تحليل التغطية والخدمات والكثافة حول الموقع"),
        MapLayerSpec("NASA_GIBS", "أقمار ناسا الحية (VIIRS / MODIS)", "🛰️", "BASE", "صور فضائية حية ومؤشرات حرارية"),
        MapLayerSpec("EOX_CLOUDLESS", "قمر EOX الخالي من السحب", "🌍", "BASE", "خريطة فضائية صافية 100% خالية من الغيوم"),
        MapLayerSpec("ESRI_IMAGERY", "صور الأقمار الصناعية (Esri World)", "📸", "BASE", "صور أقمار صناعية تفصيلية للمباني والطرق"),
        MapLayerSpec("EUMETSAT", "خرائط الطقس والمناخ الحية", "🌦️", "WEATHER_HAZARD", "الأمطار والسحب والرياح اللحظية فوق اليمن"),
        MapLayerSpec("EARTHQUAKES", "راصد الزلازل والمخاطر (USGS)", "🌋", "WEATHER_HAZARD", "تنبيهات النشاط الزلزالي والمخاطر الطبيعية"),
        MapLayerSpec("GNSS", "مقياس دقة الأقمار (GPS/GLONASS)", "📡", "NETWORK", "مراقبة دقة الإشارة والارتفاعات والبوصلة"),
        MapLayerSpec("DEAD_ZONES", "مناطق انقطاع وضعف الشبكة", "📶", "NETWORK", "كشف المناطق منخفضة التغطية الميدانية"),
        MapLayerSpec("RELIABILITY", "مؤشر موثوقية الطرق والممرات", "🛡️", "WEATHER_HAZARD", "حالة الوعورة والانحدار والتجمعات"),
        MapLayerSpec("ROAD_SAFETY", "سلامة الطرق والتحذيرات", "⚠️", "WEATHER_HAZARD", "النقاط الحرجة والمنعطفات الجبلية"),
        MapLayerSpec("RADAR", "رادار التغطية والتوزيع الجغرافي", "⭕", "NETWORK", "نطاق المسح الجغرافي للخدمات القريبة"),
        MapLayerSpec("HEATMAP", "خريطة الكثافة والطلب الحرارية", "🔥", "OVERLAY", "تركيز الطلبات ومقدمي الخدمات حسب الأحياء"),
        MapLayerSpec("OFFLINE_MAPS", "مدير الخرائط والمسارات أوفلاين", "📥", "TOOLS", "تنزيل خرائط Valhalla وقاعدة الأماكن المحلية"),
        MapLayerSpec("MEASURE_RULER", "مسطرة قياس المسافات والمساحات", "📏", "TOOLS", "حساب المسافات المباشرة والمساحة بين النقاط"),
        MapLayerSpec("GPX_EXPORT", "تصدير واستيراد المسارات (GPX)", "📤", "TOOLS", "حفظ ومشاركة المسارات الجغرافية"),
        MapLayerSpec("PDF_EXPORT", "تصدير الخريطة الميدانية (PDF)", "🖨️", "TOOLS", "طباعة أو حفظ نسخة PDF للموقع والمسار"),
        MapLayerSpec("OSM_CONTRIBUTE", "المساهمة وتحرير المعالم (OSM)", "✏️", "TOOLS", "إضافة وتحديث المعالم والأماكن المحلية"),
        MapLayerSpec("RURAL_SOS", "نداء الطوارئ والإنقاذ الجغرافي", "🆘", "TOOLS", "إرسال الإحداثيات السريع في المناطق الريفية"),
        MapLayerSpec("NIGHT_HUD", "الوضع الليلي للملاحة (Night HUD)", "🌙", "MODES", "عرض ليلي مريح للعين أثناء القيادة"),
        MapLayerSpec("GROUP_TRACK", "تتبع الفريق والمجموعات الميدانية", "👥", "MODES", "مشاركة وتتبع المواقع الحية للميدان")
    )

    val DEFAULT_ORDER_CSV: String = ALL_LAYERS.joinToString(",") { it.id }

    fun parseOrderedVisibleLayers(
        orderCsv: String,
        disabledCsv: String
    ): List<MapLayerSpec> {
        val disabled = disabledCsv.split(",").map { it.trim() }.filter { it.isNotBlank() }.toSet()
        val orderedIds = orderCsv.split(",").map { it.trim() }.filter { it.isNotBlank() }
        val byId = ALL_LAYERS.associateBy { it.id }
        val result = mutableListOf<MapLayerSpec>()
        orderedIds.forEach { id ->
            byId[id]?.let { spec ->
                if (spec.id !in disabled && result.none { it.id == spec.id }) {
                    result.add(spec)
                }
            }
        }
        ALL_LAYERS.forEach { spec ->
            if (spec.id !in disabled && result.none { it.id == spec.id }) {
                result.add(spec)
            }
        }
        return result
    }

    fun parseFullOrderList(orderCsv: String): List<MapLayerSpec> {
        val orderedIds = orderCsv.split(",").map { it.trim() }.filter { it.isNotBlank() }
        val byId = ALL_LAYERS.associateBy { it.id }
        val result = mutableListOf<MapLayerSpec>()
        orderedIds.forEach { id ->
            byId[id]?.let { spec ->
                if (result.none { it.id == spec.id }) result.add(spec)
            }
        }
        ALL_LAYERS.forEach { spec ->
            if (result.none { it.id == spec.id }) result.add(spec)
        }
        return result
    }
}
