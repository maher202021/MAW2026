package com.serviseyem.ui.theme

import com.serviseyem.utils.*
import com.serviseyem.ui.*

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp

// 💎 Luxury Yemeni Directory Dark Color Scheme
private val DarkYemenColorScheme = darkColorScheme(
    primary = AmberGoldHighlight,
    onPrimary = Black,
    primaryContainer = WarmGoldAccent,
    onPrimaryContainer = TextPrimaryWhite,
    secondary = EmeraldGreen,
    onSecondary = White,
    secondaryContainer = DeepEmerald,
    onSecondaryContainer = TextPrimaryWhite,
    tertiary = NeonCyan,
    onTertiary = Black,
    background = DeepRoyalNavy,
    onBackground = TextPrimaryWhite,
    surface = SlateCardSurface,
    onSurface = TextPrimaryWhite,
    surfaceVariant = SlateSurfaceVariant,
    onSurfaceVariant = TextSecondarySlate,
    outline = SlateBorder,
    error = ErrorPink
)

// 💎 Luxury Yemeni Directory Light Color Scheme
private val LightYemenColorScheme = lightColorScheme(
    primary = DeepEmerald,
    onPrimary = White,
    primaryContainer = EmeraldGreen,
    onPrimaryContainer = White,
    secondary = WarmGoldAccent,
    onSecondary = White,
    secondaryContainer = AmberGoldHighlight,
    onSecondaryContainer = Black,
    tertiary = NeonCyan,
    onTertiary = Black,
    background = White,
    onBackground = Black,
    surface = LightGray,
    onSurface = Black,
    surfaceVariant = White,
    onSurfaceVariant = MediumGray,
    outline = SlateBorder,
    error = ErrorPink
)

// 💎 Standardized 8.dp Grid Shapes
val YemenShapes = Shapes(
    extraSmall = RoundedCornerShape(6.dp),
    small = RoundedCornerShape(10.dp),
    medium = RoundedCornerShape(14.dp),
    large = RoundedCornerShape(18.dp),
    extraLarge = RoundedCornerShape(24.dp)
)

/**
 * 👑 YemenLuxuryTheme - المظهر العالي الفخامة لدليل خدمات اليمن الذكي
 * يدعم الخط العربي المخصص (Cairo) والرموز والاستجابة لجميع مقاسات الشاشات.
 */
@Composable
fun YemenLuxuryTheme(
    darkTheme: Boolean = true, // Default to Dark Theme for Luxury Aesthetic
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkYemenColorScheme
        else -> LightYemenColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = YemenTypography,
        shapes = YemenShapes,
        content = content
    )
}

// Legacy Theme Aliases for Full Compatibility across app
@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    YemenLuxuryTheme(
        darkTheme = darkTheme,
        dynamicColor = dynamicColor,
        content = content
    )
}

@Composable
fun YemenServicesTheme(
    darkTheme: Boolean = true,
    content: @Composable () -> Unit
) {
    YemenLuxuryTheme(
        darkTheme = darkTheme,
        dynamicColor = false,
        content = content
    )
}

