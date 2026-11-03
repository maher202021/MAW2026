package com.serviseyem.ui.theme

import android.os.Build
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp

// 🌊 Elegant Light Icy-Blue + Deep Emerald & Gold Shine Color Scheme (Strictly NO Pure White or Pure Black)
private val LightLuxuryColorScheme = androidx.compose.material3.lightColorScheme(
    primary = DarkCyanPrimary,
    onPrimary = CardSurface,
    primaryContainer = SecondaryTeal,
    onPrimaryContainer = CardSurface,
    secondary = GoldShine,
    onSecondary = Black,
    background = AppBackground,
    onBackground = TextLightMint,
    surface = CardSurface,
    onSurface = TextLightMint,
    outline = CardBorder,
    error = ErrorCoral
)

// 💎 Standardized 8.dp Grid Shapes
val YemenShapes = Shapes(
    extraSmall = RoundedCornerShape(6.dp),
    small = RoundedCornerShape(10.dp),
    medium = RoundedCornerShape(14.dp),
    large = RoundedCornerShape(18.dp),
    extraLarge = RoundedCornerShape(24.dp)
)

@Composable
fun YemenLuxuryTheme(
    darkTheme: Boolean = false,
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = LightLuxuryColorScheme,
        typography = YemenTypography,
        shapes = YemenShapes,
        content = content
    )
}

@Composable
fun MyApplicationTheme(content: @Composable () -> Unit) {
    YemenLuxuryTheme(content = content)
}

@Composable
fun YemenServicesTheme(content: @Composable () -> Unit) {
    YemenLuxuryTheme(content = content)
}
