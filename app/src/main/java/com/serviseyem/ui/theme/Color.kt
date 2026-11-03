package com.serviseyem.ui.theme

import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

// 🌊 Elegant Light Sky-Blue + Emerald & Soft Gold Theme (Strictly NO Pure White #FFFFFF or Black #000000)
// The main background is distinctly sky-blue (سماوي فاتح أنيق ومميز ومبهر)
val AppBackground = Color(0xFFAFE0F8)
val CardSurface = Color(0xFFD6EEFA)
val CardBorder = Color(0xFF8FD2F8)
val GlassBorder = Color(0xFF75C7F5)

// 🌟 Accent Palette
val GoldShine = Color(0xFFD4A017)
val DarkCyanPrimary = Color(0xFF0D5C3A)      // Deep Elegant Emerald Green
val SecondaryTeal = Color(0xFF147D72)        // Muted Teal
val MutedGold = Color(0xFFB8860B)
val RichBlueTeal = Color(0xFF0284C7)
val SoftIndigo = Color(0xFF4F46E5)
val MutedPurple = Color(0xFF9333EA)

// Text Colors (Deep Midnight Navy & Slate - NO Pure Black #000000)
val TextLightMint = Color(0xFF0F2B48)        // Primary Text: Crisp Deep Navy
val TextMutedMint = Color(0xFF3B648B)        // Secondary Text: Elegant Muted Slate
val White = Color(0xFFD6EEFA)                // Tinted Sky-Off-White (Never #FFFFFF)
val Black = Color(0xFF0F2B48)                // Deep Navy Slate (Never #000000)

val SuccessGreen = Color(0xFF0D5C3A)
val WarningAmber = Color(0xFFD97706)
val ErrorCoral = Color(0xFFE11D48)
val ButtonLightText = Color(0xFFE0F2FE)

// 🎨 Distinct Category Department Themes (Strictly Tinted, Never White / Black)
// 🍔 1. المطاعم والكافيهات (Warm Appetizing Peach/Apricot)
val FoodScreenBg = Color(0xFFFDE8D4)
val FoodCardSurface = Color(0xFFFFF2E6)
val FoodCardBorder = Color(0xFFFDBA74)
val FoodAccent = Color(0xFFEA580C)

// 🏪 2. المحلات والمتاجر (Modern Azure Sky Tint)
val StoreScreenBg = Color(0xFFD3E8FA)
val StoreCardSurface = Color(0xFFE5F2FC)
val StoreCardBorder = Color(0xFF93C5FD)
val StoreAccent = Color(0xFF0284C7)

// 🏥 3. المراكز الطبية والعيادات (Calming Seafoam Mint)
val MedicalScreenBg = Color(0xFFCEF2E8)
val MedicalCardSurface = Color(0xFFE4FAF4)
val MedicalCardBorder = Color(0xFF86EFAC)
val MedicalAccent = Color(0xFF0D9488)

// 🏢 4. العقارات والمباني (Prestigious Cashmere Desert Sand)
val EstateScreenBg = Color(0xFFEFE8DA)
val EstateCardSurface = Color(0xFFF9F4EB)
val EstateCardBorder = Color(0xFFD4C3AC)
val EstateAccent = Color(0xFFB45309)

// 💼 5. إعلانات الوظائف والفنيين (Periwinkle Violet Slate)
val JobsScreenBg = Color(0xFFDFE2F7)
val JobsCardSurface = Color(0xFFECEEFC)
val JobsCardBorder = Color(0xFFC7D2FE)
val JobsAccent = Color(0xFF6366F1)

// 🔗 Compatibility Aliases
val SoftLightSageBg = AppBackground
val LightCardSurface = CardSurface
val LightCardBorder = CardBorder
val DeepEmerald = DarkCyanPrimary
val EmeraldLight = Color(0xFF15803D)
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
val FoodFamilyPrimary = FoodAccent
val FoodFamilySecondary = FoodCardBorder
val FoodFamilyGold = GoldShine

val TechFamilyPrimary = StoreAccent
val TechFamilySecondary = RichBlueTeal
val TechFamilyCyan = SecondaryTeal

val TravelFamilyPrimary = DarkCyanPrimary
val TravelFamilySecondary = SecondaryTeal
val TravelFamilySunset = GoldShine

val HealthFamilyPrimary = MedicalAccent
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
val GoldGradientEnd = Color(0xFFF59E0B)
val GlowingCoralStart = ErrorCoral
val SkyBlueStart = RichBlueTeal

val GoldShineGradient = Brush.horizontalGradient(listOf(GoldGradientStart, GoldGradientEnd))
val DarkCyanGradient = Brush.horizontalGradient(listOf(DarkCyanPrimary, SecondaryTeal))
val DarkCyanBackgroundGradient = Brush.verticalGradient(listOf(AppBackground, Color(0xFFC5E8FA)))
val LuxuryGoldGradient = GoldShineGradient
val LuxuryEmeraldGradient = DarkCyanGradient
val LuxurySkyBlueGradient = Brush.horizontalGradient(listOf(SkyBlueStart, SecondaryTeal))
val LuxuryCoralGradient = Brush.horizontalGradient(listOf(GlowingCoralStart, GlowingCoralStart))
val LuxuryDarkNavyGradient = DarkCyanBackgroundGradient
