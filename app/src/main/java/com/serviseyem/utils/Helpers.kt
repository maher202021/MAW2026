package com.serviseyem.utils

import com.serviseyem.utils.*

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Base64
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import com.serviseyem.data.AdminSettingsEntity
import com.serviseyem.data.ProviderEntity
import com.serviseyem.data.StoreEntity
import com.serviseyem.data.PropertyEntity
import java.io.ByteArrayOutputStream

data class VisualThemePalette(
    val activeId: String,
    val primary: Color,
    val secondary: Color,
    val background: Color,
    val surface: Color,
    val textPrimary: Color,
    val textSecondary: Color,
    val accent: Color,
    val gradientBrush: Brush,
    val scheme: ColorScheme,
    val border: Color = Color(0xFF374151)
)

data class PresetPalette(
    val name: String,
    val primaryHex: String,
    val secondaryHex: String,
    val bgHex: String,
    val surfaceHex: String
)

val colorsPresetsList = listOf(
    PresetPalette("🌌 الكوني الفضي", "#9CA3AF", "#374151", "#111827", "#1F2937"),
    PresetPalette("✨ ذهبي فاخر", "#D4AF37", "#FFD700", "#1A1A1A", "#2D2D2D"),
    PresetPalette("🟢 زمردي راقي", "#004B49", "#50C878", "#0C1814", "#152A20"),
    PresetPalette("⚫ الأسود الدخاني", "#121212", "#333333", "#080808", "#101010")
)

fun getBookingTimestamp(): Long = System.currentTimeMillis()

fun resolveThemePalette(settings: AdminSettingsEntity): VisualThemePalette {
    val parseColorSafely = { hex: String, defaultColor: Color ->
        try {
            Color(android.graphics.Color.parseColor(hex))
        } catch (e: Exception) {
            defaultColor
        }
    }

    return when (settings.activeThemeId) {
        "COSMIC_SILVER" -> {
            val primary = Color(0xFF9CA3AF)
            val secondary = Color(0xFF374151)
            val background = Color(0xFF111827)
            val surface = Color(0xFF1F2937)
            val textPrimary = Color(0xFFF9FAFB)
            val textSecondary = Color(0xFFD1D5DB)
            val accent = Color(0xFFE5E7EB)
            VisualThemePalette(
                activeId = "COSMIC_SILVER",
                primary = primary,
                secondary = secondary,
                background = background,
                surface = surface,
                textPrimary = textPrimary,
                textSecondary = textSecondary,
                accent = accent,
                gradientBrush = Brush.verticalGradient(listOf(Color(0xFF1F2937), Color(0xFF111827))),
                scheme = darkColorScheme(primary = primary, secondary = secondary, background = background, surface = surface)
            )
        }
        "LUXURY_GOLD" -> {
            val primary = Color(0xFFD97706)
            val secondary = Color(0xFF451A03)
            val background = Color(0xFF0F0F10)
            val surface = Color(0xFF1C1917)
            val textPrimary = Color(0xFFFFFAFA)
            val textSecondary = Color(0xFFE7E5E4)
            val accent = Color(0xFFFBBF24)
            VisualThemePalette(
                activeId = "LUXURY_GOLD",
                primary = primary,
                secondary = secondary,
                background = background,
                surface = surface,
                textPrimary = textPrimary,
                textSecondary = textSecondary,
                accent = accent,
                gradientBrush = Brush.verticalGradient(listOf(Color(0xFF292524), Color(0xFF0F0F10))),
                scheme = darkColorScheme(primary = primary, secondary = secondary, background = background, surface = surface)
            )
        }
        "ELITE_EMERALD" -> {
            val primary = Color(0xFF059669)
            val secondary = Color(0xFF047857)
            val background = Color(0xFF022C22)
            val surface = Color(0xFF064E3B)
            val textPrimary = Color(0xFFF0FDF4)
            val textSecondary = Color(0xFFA7F3D0)
            val accent = Color(0xFF34D399)
            VisualThemePalette(
                activeId = "ELITE_EMERALD",
                primary = primary,
                secondary = secondary,
                background = background,
                surface = surface,
                textPrimary = textPrimary,
                textSecondary = textSecondary,
                accent = accent,
                gradientBrush = Brush.verticalGradient(listOf(Color(0xFF064E3B), Color(0xFF022C22))),
                scheme = darkColorScheme(primary = primary, secondary = secondary, background = background, surface = surface)
            )
        }
        "EMERALD_YEMEN", "YEMEN_EMERALD" -> {
            val primary = Color(0xFF0C1628)
            val secondary = Color(0xFF1E2D4A)
            val background = Color(0xFF090E17) // Sophisticated modern dark navy background
            val surface = Color(0xFF111C30) // Sleek dark card surface
            val textPrimary = Color(0xFFFFFFFF) // Crisp high-contrast pure white
            val textSecondary = Color(0xFF94A3B8) // Slate-400
            val accent = Color(0xFFD4AF37) // Royal Gold
            val border = Color(0xFF1E2E4A)
            VisualThemePalette(
                activeId = "EMERALD_YEMEN",
                primary = primary,
                secondary = secondary,
                background = background,
                surface = surface,
                textPrimary = textPrimary,
                textSecondary = textSecondary,
                accent = accent,
                border = border,
                gradientBrush = Brush.verticalGradient(listOf(Color(0xFF111C30), Color(0xFF090E17))),
                scheme = darkColorScheme(
                    primary = primary,
                    secondary = secondary,
                    background = background,
                    surface = surface,
                    onPrimary = Color.White,
                    onSecondary = Color.White,
                    onBackground = Color.White,
                    onSurface = Color.White
                )
            )
        }
        "CUSTOM" -> {
            val primary = parseColorSafely(settings.customPrimaryHex, Color(0xFF0C1628))
            val secondary = parseColorSafely(settings.customSecondaryHex, Color(0xFF1E2D4A))
            val background = parseColorSafely(settings.customBackgroundHex, Color(0xFF090E17))
            val surface = parseColorSafely(settings.customSurfaceHex, Color(0xFF111C30))
            val textPrimary = Color(0xFFFFFFFF)
            val textSecondary = Color(0xFF94A3B8)
            val accent = Color(0xFFD4AF37)
            VisualThemePalette(
                activeId = "CUSTOM",
                primary = primary,
                secondary = secondary,
                background = background,
                surface = surface,
                textPrimary = textPrimary,
                textSecondary = textSecondary,
                accent = accent,
                border = Color(0xFF1E2E4A),
                gradientBrush = Brush.verticalGradient(listOf(surface, background)),
                scheme = darkColorScheme(primary = primary, secondary = secondary, background = background, surface = surface)
            )
        }
        else -> {
            // Default: Sophisticated modern dark luxury theme (Screenshot 1)
            val primary = Color(0xFF0C1628)
            val secondary = Color(0xFF1E2D4A)
            val background = Color(0xFF090E17)
            val surface = Color(0xFF111C30)
            val textPrimary = Color(0xFFFFFFFF)
            val textSecondary = Color(0xFF94A3B8)
            val accent = Color(0xFFD4AF37)
            VisualThemePalette(
                activeId = "YEMEN_DARK_LUXURY",
                primary = primary,
                secondary = secondary,
                background = background,
                surface = surface,
                textPrimary = textPrimary,
                textSecondary = textSecondary,
                accent = accent,
                border = Color(0xFF1E2E4A),
                gradientBrush = Brush.verticalGradient(listOf(Color(0xFF111C30), Color(0xFF090E17))),
                scheme = darkColorScheme(primary = primary, secondary = secondary, background = background, surface = surface)
            )
        }
    }
}

fun getCityCenterCoords(cityId: String): Pair<Double, Double> {
    return when (cityId) {
        "ye_san" -> Pair(15.3694, 44.1910)
        "ye_ade" -> Pair(12.7855, 45.0186)
        "ye_tai" -> Pair(13.5794, 44.0205)
        "ye_hod" -> Pair(14.7979, 42.9530)
        else -> Pair(15.3694, 44.1910)
    }
}

fun calculateDistanceInMeters(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Float {
    val results = FloatArray(1)
    return try {
        android.location.Location.distanceBetween(lat1, lon1, lat2, lon2, results)
        results[0]
    } catch (e: Exception) {
        val dLat = Math.toRadians(lat2 - lat1)
        val dLon = Math.toRadians(lon2 - lon1)
        val a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *
                Math.sin(dLon / 2) * Math.sin(dLon / 2)
        val c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a))
        (6371000 * c).toFloat()
    }
}

fun formatDistance(meters: Float): String {
    return if (meters < 1000f) {
        "${meters.toInt()} م"
    } else {
        val km = meters / 1000f
        String.format(java.util.Locale.US, "%.1f كم", km)
    }
}

fun getAreaCoords(areaName: String): Pair<Double, Double> {
    val norm = areaName.lowercase().trim()
    return when {
        norm.contains("تعز") || norm.contains("taiz") || norm.contains("ye_tai") -> Pair(13.5794, 44.0135)
        norm.contains("عدن") || norm.contains("aden") || norm.contains("ye_ade") -> Pair(12.7855, 45.0186)
        norm.contains("الحديدة") || norm.contains("hodeidah") || norm.contains("ye_hud") || norm.contains("hod") -> Pair(14.7979, 42.9530)
        norm.contains("حضرموت") || norm.contains("المكلا") || norm.contains("mukalla") || norm.contains("ye_had") -> Pair(14.5424, 49.1242)
        norm.contains("إب") || norm.contains("ibb") || norm.contains("ye_ibb") -> Pair(13.9667, 44.1833)
        norm.contains("ذمار") || norm.contains("dhamar") || norm.contains("ye_dha") -> Pair(14.5428, 44.4051)
        norm.contains("مأرب") || norm.contains("marib") || norm.contains("ye_mar") -> Pair(15.4542, 45.3262)
        norm.contains("شبوة") || norm.contains("عتق") || norm.contains("shabwah") || norm.contains("ye_sha") -> Pair(14.5377, 46.8319)
        norm.contains("صعدة") || norm.contains("saada") || norm.contains("ye_saa") -> Pair(16.9402, 43.7639)
        norm.contains("لحج") || norm.contains("lahej") || norm.contains("ye_lah") -> Pair(13.0582, 44.8838)
        norm.contains("أبين") || norm.contains("زنجبار") || norm.contains("abyan") || norm.contains("ye_aby") -> Pair(13.1287, 45.3807)
        norm.contains("المهرة") || norm.contains("الغيضة") || norm.contains("mahra") || norm.contains("ye_mah") -> Pair(16.2079, 52.1760)
        norm.contains("حجة") || norm.contains("hajja") || norm.contains("ye_haj") -> Pair(15.6917, 43.6028)
        norm.contains("البيضاء") || norm.contains("bayda") || norm.contains("ye_bay") -> Pair(13.9852, 45.5727)
        norm.contains("عمران") || norm.contains("amran") || norm.contains("ye_amr") -> Pair(15.6594, 43.9439)
        norm.contains("سيئون") || norm.contains("sayun") -> Pair(15.9432, 48.7871)
        else -> Pair(15.3694, 44.1910) // Sana'a default center
    }
}

fun getProviderCoords(provider: ProviderEntity): Pair<Double, Double> {
    if (provider.latitude != 0.0 && provider.longitude != 0.0) {
        return Pair(provider.latitude, provider.longitude)
    }
    val baseStr = when {
        provider.localNeighborhood.isNotEmpty() -> provider.localNeighborhood
        provider.area.isNotEmpty() -> provider.area
        provider.cityId.isNotEmpty() -> provider.cityId
        else -> "صنعاء"
    }
    val base = getAreaCoords(baseStr)
    // تطبيق الإزاحة الحتمية (Deterministic Hash Offset) بين 40 و 160 متر
    val idHash = (provider.id.ifEmpty { provider.name }).hashCode().toLong()
    val hashAngle = (kotlin.math.abs(idHash) % 360).toDouble() * (Math.PI / 180.0)
    val hashOffsetMeters = 40.0 + (kotlin.math.abs(idHash ushr 16) % 120)
    val metersPerDegreeLat = 110540.0
    val metersPerDegreeLng = 111320.0 * kotlin.math.cos(Math.toRadians(base.first))
    val offsetLat = (hashOffsetMeters * kotlin.math.sin(hashAngle)) / metersPerDegreeLat
    val offsetLng = (hashOffsetMeters * kotlin.math.cos(hashAngle)) / metersPerDegreeLng
    return Pair(base.first + offsetLat, base.second + offsetLng)
}

fun getStoreCoords(store: StoreEntity): Pair<Double, Double> {
    if (store.latitude != 0.0 && store.longitude != 0.0) {
        return Pair(store.latitude, store.longitude)
    }
    val baseStr = when {
        store.localNeighborhood.isNotEmpty() -> store.localNeighborhood
        store.cityId.isNotEmpty() -> store.cityId
        else -> "صنعاء"
    }
    val base = getAreaCoords(baseStr)
    // تطبيق الإزاحة الحتمية (Deterministic Hash Offset) بين 40 و 160 متر
    val idHash = (store.id.ifEmpty { store.name }).hashCode().toLong()
    val hashAngle = (kotlin.math.abs(idHash) % 360).toDouble() * (Math.PI / 180.0)
    val hashOffsetMeters = 40.0 + (kotlin.math.abs(idHash ushr 16) % 120)
    val metersPerDegreeLat = 110540.0
    val metersPerDegreeLng = 111320.0 * kotlin.math.cos(Math.toRadians(base.first))
    val offsetLat = (hashOffsetMeters * kotlin.math.sin(hashAngle)) / metersPerDegreeLat
    val offsetLng = (hashOffsetMeters * kotlin.math.cos(hashAngle)) / metersPerDegreeLng
    return Pair(base.first + offsetLat, base.second + offsetLng)
}

fun getPropertyCoords(property: PropertyEntity): Pair<Double, Double> {
    if (property.latitude != 0.0 && property.longitude != 0.0) {
        return Pair(property.latitude, property.longitude)
    }
    val baseStr = when {
        property.localNeighborhood.isNotEmpty() -> property.localNeighborhood
        property.cityId.isNotEmpty() -> property.cityId
        else -> "صنعاء"
    }
    val base = getAreaCoords(baseStr)
    // تطبيق الإزاحة الحتمية (Deterministic Hash Offset) بين 40 و 160 متر
    val idHash = (property.id.ifEmpty { property.title }).hashCode().toLong()
    val hashAngle = (kotlin.math.abs(idHash) % 360).toDouble() * (Math.PI / 180.0)
    val hashOffsetMeters = 40.0 + (kotlin.math.abs(idHash ushr 16) % 120)
    val metersPerDegreeLat = 110540.0
    val metersPerDegreeLng = 111320.0 * kotlin.math.cos(Math.toRadians(base.first))
    val offsetLat = (hashOffsetMeters * kotlin.math.sin(hashAngle)) / metersPerDegreeLat
    val offsetLng = (hashOffsetMeters * kotlin.math.cos(hashAngle)) / metersPerDegreeLng
    return Pair(base.first + offsetLat, base.second + offsetLng)
}

fun getProviderCoords(providerId: String): Pair<Double, Double> {
    return when (providerId) {
        "p_amin" -> Pair(15.3694, 44.1910)
        else -> {
            val idHash = providerId.hashCode().toLong()
            val hashAngle = (kotlin.math.abs(idHash) % 360).toDouble() * (Math.PI / 180.0)
            val hashOffsetMeters = 40.0 + (kotlin.math.abs(idHash ushr 16) % 120)
            val metersPerDegreeLat = 110540.0
            val metersPerDegreeLng = 111320.0 * kotlin.math.cos(Math.toRadians(15.3694))
            val offsetLat = (hashOffsetMeters * kotlin.math.sin(hashAngle)) / metersPerDegreeLat
            val offsetLng = (hashOffsetMeters * kotlin.math.cos(hashAngle)) / metersPerDegreeLng
            Pair(15.3694 + offsetLat, 44.1910 + offsetLng)
        }
    }
}

fun getDistance(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
    return calculateDistanceInMeters(lat1, lon1, lat2, lon2) / 1000.0
}

@Deprecated("Use FirebaseStorageUploader.uploadImageUri instead")
fun convertUriToBase64(@Suppress("UNUSED_PARAMETER") context: Context, uri: Uri): String {
    return uri.toString()
}

fun Color.luminance(): Float {
    return (0.2126f * this.red + 0.7152f * this.green + 0.0722f * this.blue)
}

fun isMoreThan8HoursBefore(timestamp: Long): Boolean {
    val diff = timestamp - System.currentTimeMillis()
    return diff > (8 * 60 * 60 * 1000)
}

/**
 * ⏰ تنسيق ساعات العمل للغة العربية باتجاه RTL سليم وطبيعي (مثال: 9:00 ص - 10:00 م)
 */
fun formatArabicWorkingHours(raw: String, defaultVal: String = "9:00 ص - 10:00 م"): String {
    if (raw.isBlank() || raw.equals("default", ignoreCase = true) || raw.contains("null")) {
        return defaultVal
    }
    return raw.trim()
        .replace("AM", "ص", ignoreCase = true)
        .replace("PM", "م", ignoreCase = true)
        .replace("to", "-", ignoreCase = true)
        .replace("إلى", "-")
        .trim()
}

/**
 * 💰 محاذاة وتنسيق رموز العملات والأسعار للغة العربية
 */
fun formatPriceWithCurrency(price: Double, currency: String = "YER"): String {
    val cleanPrice = if (price % 1.0 == 0.0) price.toLong().toString() else String.format(java.util.Locale.US, "%.1f", price)
    val curLabel = if (currency.equals("YER", ignoreCase = true) || currency.contains("ريال")) "ر.ي" else currency
    return "\u200F$cleanPrice $curLabel"
}

