package com.serviseyem.ui.theme

import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

// 🌊 Dark Luxury Midnight Palette (Background #070A13, Surfaces #121A2C, Crisp White / Light Gray Text)
val AppBackground = Color(0xFF070A13)          // Dark Night Background (#070A13)
val CardSurface = Color(0xFF121A2C)            // Dark Slate Card Surface (#121A2C)
val CardBorder = Color(0xFF1E293B)             // Dark Slate Border (#1E293B)
val GlassBorder = Color(0xFF1E293B)            // Glass Slate Border (#1E293B)

// 🌟 Core Brand Colors (Trust & Reliability)
val PrimaryBlue = Color(0xFFE8B23A)            // Soft Luxury Gold (#E8B23A)
val DarkCyanPrimary = Color(0xFF0D9488)        // Deep Emerald / Medical Teal (#0D9488)
val SecondaryTeal = Color(0xFF0D9488)          // Medical Teal (#0D9488)
val GoldShine = Color(0xFFE8B23A)              // Soft Gold Accent (#E8B23A)
val MutedGold = Color(0xFFD9901A)              // Muted Gold
val RichBlueTeal = Color(0xFF0284C7)           // Deep Sky Blue
val SoftIndigo = Color(0xFF6C5CE7)             // Royal Indigo
val MutedPurple = Color(0xFF7C3AED)            // Violet

// 🖋️ High-Contrast Dark Theme Typography (Crisp White / Light Gray)
val TextLightMint = Color(0xFFFFFFFF)          // Primary Text: Pure Crisp White (#FFFFFF)
val TextDarkTitle = Color(0xFFFFFFFF)          // Headings: Pure Crisp White (#FFFFFF)
val TextMutedMint = Color(0xFFCBD5E1)          // Secondary Text: Soft Light Gray Slate (#CBD5E1)
val TextMutedLight = Color(0xFF94A3B8)         // Tertiary Text: (#94A3B8)
val White = Color(0xFFFFFFFF)                  // Crisp Pure White (#FFFFFF)
val Black = Color(0xFF070A13)                  // Midnight Dark (#070A13)
val ButtonLightText = Color(0xFFFFFFFF)        // Pure White for buttons over dark/accent backgrounds

// 🟢 Status Colors
val SuccessGreen = Color(0xFF16A34A)           // Commercial Green (#16A34A)
val WarningAmber = Color(0xFFE8B23A)           // Warm Amber (#E8B23A)
val ErrorCoral = Color(0xFFE11D48)             // Mature Red (#E11D48)

// 🎨 Distinct Category Department Themes (Dark Luxury Surface Accents)

// 🍔 1. المطاعم والكافيهات
val FoodScreenBg = Color(0xFF070A13)
val FoodCardSurface = Color(0xFF121A2C)
val FoodCardBorder = Color(0xFF1E293B)
val FoodAccent = Color(0xFFE11D48)             // Mature Red (#E11D48)

// 🏪 2. المحلات والمتاجر
val StoreScreenBg = Color(0xFF070A13)
val StoreCardSurface = Color(0xFF121A2C)
val StoreCardBorder = Color(0xFF1E293B)
val StoreAccent = Color(0xFF16A34A)            // Technicians / Stores Green

// 🏥 3. المراكز الطبية والعيادات
val MedicalScreenBg = Color(0xFF070A13)
val MedicalCardSurface = Color(0xFF121A2C)
val MedicalCardBorder = Color(0xFF1E293B)
val MedicalAccent = Color(0xFF0D9488)          // Medical Teal (#0D9488)

// 🏢 4. العقارات والمباني
val EstateScreenBg = Color(0xFF070A13)
val EstateCardSurface = Color(0xFF121A2C)
val EstateCardBorder = Color(0xFF1E293B)
val EstateAccent = Color(0xFFE8B23A)           // Soft Gold (#E8B23A)

// 💼 5. إعلانات الوظائف والفنيين
val JobsScreenBg = Color(0xFF0F172A)
val JobsCardSurface = Color(0xFF1E293B)
val JobsCardBorder = Color(0xFF334155)
val JobsAccent = Color(0xFF4F46E5)

// 🔗 Legacy & Compatibility Aliases
val SoftLightSageBg = AppBackground
val LightCardSurface = CardSurface
val LightCardBorder = CardBorder
val DeepEmerald = DarkCyanPrimary
val EmeraldLight = Color(0xFF10B981)
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

val TravelFamilyPrimary = PrimaryBlue
val TravelFamilySecondary = SecondaryTeal
val TravelFamilySunset = GoldShine

val HealthFamilyPrimary = MedicalAccent
val HealthFamilySecondary = SecondaryTeal
val HealthFamilyGold = GoldShine

val ServicesFamilyPrimary = PrimaryBlue
val ServicesFamilySecondary = GoldShine
val ServicesFamilyTeal = SecondaryTeal

val DarkLuxuryBackground = AppBackground
val SecondaryBg = CardSurface
val TextPrimaryWhite = Color(0xFFFFFFFF)
val TextSecondarySlate = Color(0xFFCBD5E1)
val TextMutedGray = Color(0xFF94A3B8)
val LightGray = CardSurface
val DarkGray = AppBackground
val MediumGray = TextMutedMint

// 🌈 Pre-configured Brushes & Gradients
val GoldGradientStart = GoldShine
val GoldGradientEnd = Color(0xFFF59E0B)
val GlowingCoralStart = FoodAccent
val SkyBlueStart = PrimaryBlue

val GoldShineGradient = Brush.horizontalGradient(listOf(GoldGradientStart, GoldGradientEnd))
val DarkCyanGradient = Brush.horizontalGradient(listOf(DarkCyanPrimary, SecondaryTeal))
val DarkCyanBackgroundGradient = Brush.verticalGradient(listOf(AppBackground, Color(0xFF7DD3FC)))
val LuxuryGoldGradient = GoldShineGradient
val LuxuryEmeraldGradient = DarkCyanGradient
val LuxurySkyBlueGradient = Brush.horizontalGradient(listOf(SkyBlueStart, SecondaryTeal))
val LuxuryCoralGradient = Brush.horizontalGradient(listOf(GlowingCoralStart, GlowingCoralStart))
val LuxuryDarkNavyGradient = DarkCyanBackgroundGradient
