package com.serviseyem.ui.theme

import android.os.Build
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp

// 💎 World-Class Dark Luxury Color Scheme (#0F172A background, #1E293B surface)
private val LightLuxuryColorScheme = androidx.compose.material3.darkColorScheme(
    primary = PrimaryBlue,
    onPrimary = Color.White,
    primaryContainer = Color(0xFF1E293B),
    onPrimaryContainer = Color.White,
    secondary = GoldShine,
    onSecondary = Color.White,
    background = Color(0xFF0F172A),
    onBackground = Color.White,
    surface = Color(0xFF1E293B),
    onSurface = Color.White,
    surfaceVariant = Color(0xFF1E293B),
    onSurfaceVariant = Color(0xFFCBD5E1),
    outline = Color(0xFF334155),
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
