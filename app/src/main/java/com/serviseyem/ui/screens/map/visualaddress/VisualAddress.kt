package com.serviseyem.ui.screens.map.visualaddress

/**
 * 🏛️ YemeniLandmark - معلم يمني بارز وموثق
 */
data class YemeniLandmark(
    val id: String,
    val name: String,
    val cityId: String,
    val category: LandmarkCategory,
    val latitude: Double,
    val longitude: Double,
    val aliases: List<String> = emptyList(),
    val iconEmoji: String = "📍"
)

enum class LandmarkCategory(val labelArabic: String, val icon: String) {
    ROUNDABOUT("جولة / دوار", "🔄"),
    MOSQUE("جامع / مسجد", "🕌"),
    HOSPITAL("مستشفى / مركز صحي", "🏥"),
    MARKET("سوق / مركز تجاري", "🛒"),
    UNIVERSITY("جامعة / كلية", "🎓"),
    PUBLIC_BUILDING("مبنى حكومي / منشأة", "🏛️")
}

enum class OffsetDirection(val labelArabic: String, val bearingDeg: Double) {
    NORTH("شمالاً", 0.0),
    NORTH_EAST("شمال شرق", 45.0),
    EAST("شرقاً", 90.0),
    SOUTH_EAST("جنوب شرق", 135.0),
    SOUTH("جنوباً", 180.0),
    SOUTH_WEST("جنوب غرب", 225.0),
    WEST("غرباً", 270.0),
    NORTH_WEST("شمال غرب", 315.0)
}

/**
 * 📍 VisualAddress - العنوان البصري اليمني المقاوم لتشويش GPS
 */
data class VisualAddress(
    val landmarkId: String,
    val landmarkName: String,
    val cityId: String,
    val direction: OffsetDirection,
    val approximateDistanceMeters: Int,
    val visualNotes: String = "",
    val calculatedLatitude: Double = 0.0,
    val calculatedLongitude: Double = 0.0
) {
    fun toDisplayText(): String {
        val notesPart = if (visualNotes.isNotBlank()) " • $visualNotes" else ""
        return "$landmarkName • $approximateDistanceMeters م ${direction.labelArabic}$notesPart"
    }

    fun toDriverGuidanceString(): String {
        return "توجه أولاً نحو: [$landmarkName]، ثم اتجه ${direction.labelArabic} مسافة $approximateDistanceMeters متر تقريبا.${if (visualNotes.isNotBlank()) " المعلم البصري: $visualNotes" else ""}"
    }
}
