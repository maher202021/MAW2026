package com.serviseyem.ui.theme

import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

// 🌊 Elegant Dark Cyan + Gold Shine Theme (Strictly NO White/Black)
val AppBackground = Color(0xFF0A191D)
val CardSurface = Color(0xFF112328)
val CardBorder = Color(0xFF1D3B42)
val GlassBorder = Color(0xFF26525C)

// 🌟 Accent Palette
val GoldShine = Color(0xFFFFD700)
val DarkCyanPrimary = Color(0xFF00838F)
val SecondaryTeal = Color(0xFF006064)
val MutedGold = Color(0xFFB8860B)
val RichBlueTeal = Color(0xFF0EA5E9)
val SoftIndigo = Color(0xFF6366F1)
val MutedPurple = Color(0xFFA855F7)

// Text Colors (Soft light Mint for contrast)
val TextLightMint = Color(0xFFE0F2F1)
val TextMutedMint = Color(0xFF92A8A4)
val White = Color(0xFFFFFFFF)
val Black = Color(0xFF000000)

val SuccessGreen = Color(0xFF00BFA5)
val WarningAmber = Color(0xFFFFC107)
val ErrorCoral = Color(0xFFFF5252)

// 🔗 Legacy Aliases mapped to new theme
val SoftLightSageBg = AppBackground
val LightCardSurface = CardSurface
val LightCardBorder = CardBorder
val DeepEmerald = DarkCyanPrimary
val EmeraldLight = DarkCyanPrimary
val SoftGold = GoldShine
val MutedTeal = SecondaryTeal
val VividCoralRed = ErrorCoral
val WarmCoral = WarningAmber

val TextDarkGreen = TextLightMint
val TextMutedSage = TextMutedMint
val SuccessGreenLegacy = SuccessGreen
val WarningAmberLegacy = WarningAmber
val ErrorPinkLegacy = ErrorCoral

// Section Families
val FoodFamilyPrimary = DarkCyanPrimary
val FoodFamilySecondary = ErrorCoral
val FoodFamilyGold = GoldShine

val TechFamilyPrimary = DarkCyanPrimary
val TechFamilySecondary = RichBlueTeal
val TechFamilyCyan = SecondaryTeal

val TravelFamilyPrimary = DarkCyanPrimary
val TravelFamilySecondary = SecondaryTeal
val TravelFamilySunset = GoldShine

val HealthFamilyPrimary = DarkCyanPrimary
val HealthFamilySecondary = SecondaryTeal
val HealthFamilyGold = GoldShine

val ServicesFamilyPrimary = DarkCyanPrimary
val ServicesFamilySecondary = GoldShine
val ServicesFamilyTeal = SecondaryTeal

// Legacy Aliases for Full Compatibility
val DarkLuxuryBackground = AppBackground
val SecondaryBg = CardSurface
val TextPrimaryWhite = TextLightMint
val TextSecondarySlate = TextMutedMint
val TextMutedGray = TextMutedMint
val LightGray = CardSurface
val DarkGray = AppBackground
val MediumGray = TextMutedMint

// 🌈 Pre-configured Brushes & Gradients
val GoldGradientStart = GoldShine
val GoldGradientEnd = MutedGold
val GlowingCoralStart = ErrorCoral
val SkyBlueStart = RichBlueTeal

val GoldShineGradient = Brush.horizontalGradient(listOf(GoldGradientStart, GoldGradientEnd))
val DarkCyanGradient = Brush.horizontalGradient(listOf(DarkCyanPrimary, SecondaryTeal))
val DarkCyanBackgroundGradient = Brush.verticalGradient(listOf(AppBackground, Color(0xFF0D252B)))
val LuxuryGoldGradient = GoldShineGradient
val LuxuryEmeraldGradient = DarkCyanGradient
val LuxurySkyBlueGradient = Brush.horizontalGradient(listOf(SkyBlueStart, SecondaryTeal))
val LuxuryCoralGradient = Brush.horizontalGradient(listOf(GlowingCoralStart, GlowingCoralStart))
val LuxuryDarkNavyGradient = DarkCyanBackgroundGradient
