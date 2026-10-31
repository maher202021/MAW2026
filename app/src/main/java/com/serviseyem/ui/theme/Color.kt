package com.serviseyem.ui.theme

import com.serviseyem.utils.*
import com.serviseyem.ui.*

import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

// 💎 Luxury Yemeni Directory Color Tokens (دليل خدمات يمني فاخر)

// Primary Palette - Deep Royal Slate & Emerald Gold
val DeepRoyalNavy = Color(0xFF0B1120)
val SlateDarkBackground = Color(0xFF0F172A)
val SlateCardSurface = Color(0xFF1E293B)
val SlateSurfaceVariant = Color(0xFF131C2E)
val SlateBorder = Color(0xFF334155)

// Accent Palette - Gold, Emerald, Cyan
val WarmGoldAccent = Color(0xFFD97706)
val AmberGoldHighlight = Color(0xFFF59E0B)
val PureGold = Color(0xFFFFD700)
val EmeraldGreen = Color(0xFF10B981)
val DeepEmerald = Color(0xFF0D9488)
val NeonCyan = Color(0xFF00E5FF)

// Text Palette
val TextPrimaryWhite = Color(0xFFF8FAFC)
val TextSecondarySlate = Color(0xFF94A3B8)
val TextMutedGray = Color(0xFF64748B)

// Utility & Feedback Palette
val White = Color(0xFFFFFFFF)
val Black = Color(0xFF000000)
val LightGray = Color(0xFFF1F5F9)
val DarkGray = Color(0xFF1E293B)
val MediumGray = Color(0xFF64748B)

val SuccessGreen = Color(0xFF10B981)
val WarningAmber = Color(0xFFF59E0B)
val ErrorPink = Color(0xFFEC4899)

// Luxury Glass & Border Gradients
val GlassBorder = Color.White.copy(alpha = 0.08f)
val LuxuryGoldGradient = Brush.horizontalGradient(
    listOf(Color(0xFFFFFAED), Color(0xFFF59E0B), Color(0xFFD97706), Color(0xFFFEF3C7))
)
val LuxuryEmeraldGradient = Brush.horizontalGradient(
    listOf(Color(0xFF0D9488), Color(0xFF10B981), Color(0xFF06B6D4))
)
val LuxuryNavyGradient = Brush.verticalGradient(
    listOf(Color(0xFF131C2E), Color(0xFF0B1120))
)



