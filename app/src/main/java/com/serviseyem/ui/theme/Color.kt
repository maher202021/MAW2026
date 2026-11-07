package com.serviseyem.ui.theme

import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

// 🌊 Dark Luxury Midnight Palette (Background #0F172A, Surfaces #1E293B, Crisp White / Light Gray Text)
val AppBackground = Color(0xFF0F172A)          // Dark Midnight Background (#0F172A)
val CardSurface = Color(0xFF1E293B)            // Dark Slate Card Surface (#1E293B)
val CardBorder = Color(0xFF334155)             // Glass Dark Slate Border (#334155)
val GlassBorder = Color(0xFF334155)            // Glass Slate Border (#334155)

// 🌟 Core Brand Colors (Trust & Reliability)
val PrimaryBlue = Color(0xFF0284C7)            // Deep Elegant Sky Blue (#0284C7)
val DarkCyanPrimary = Color(0xFF0D5C3A)        // Deep Emerald Green (#0D5C3A)
val SecondaryTeal = Color(0xFF0D9488)          // Medical Teal (#0D9488)
val GoldShine = Color(0xFFD4A017)              // Golden Amber Accent (#D4A017)
val MutedGold = Color(0xFFB8860B)              // Deep Amber
val RichBlueTeal = Color(0xFF0284C7)           // Deep Sky Blue
val SoftIndigo = Color(0xFF4F46E5)             // Royal Indigo
val MutedPurple = Color(0xFF7C3AED)            // Violet

// 🖋️ High-Contrast Dark Theme Typography (Crisp White / Light Gray)
val TextLightMint = Color(0xFFFFFFFF)          // Primary Text: Pure Crisp White (#FFFFFF)
val TextDarkTitle = Color(0xFFFFFFFF)          // Headings: Pure Crisp White (#FFFFFF)
val TextMutedMint = Color(0xFFCBD5E1)          // Secondary Text: Soft Light Gray Slate (#CBD5E1)
val TextMutedLight = Color(0xFF94A3B8)         // Tertiary Text: (#94A3B8)
val White = Color(0xFFFFFFFF)                  // Crisp Pure White (#FFFFFF)
val Black = Color(0xFF0F172A)                  // Midnight Navy (#0F172A)
val ButtonLightText = Color(0xFFFFFFFF)        // Pure White for buttons over dark/accent backgrounds

// 🟢 Status Colors
val SuccessGreen = Color(0xFF059669)           // Commercial Green (#059669)
val WarningAmber = Color(0xFFD97706)           // Warm Amber (#D97706)
val ErrorCoral = Color(0xFFDC2626)             // Red-600

// 🎨 Distinct Category Department Themes (Dark Luxury Surface Accents)

// 🍔 1. المطاعم والكافيهات
val FoodScreenBg = Color(0xFF0F172A)
val FoodCardSurface = Color(0xFF1E293B)
val FoodCardBorder = Color(0xFF334155)
val FoodAccent = Color(0xFFEA580C)

// 🏪 2. المحلات والمتاجر
val StoreScreenBg = Color(0xFF0F172A)
val StoreCardSurface = Color(0xFF1E293B)
val StoreCardBorder = Color(0xFF334155)
val StoreAccent = Color(0xFF059669)

// 🏥 3. المراكز الطبية والعيادات
val MedicalScreenBg = Color(0xFF0F172A)
val MedicalCardSurface = Color(0xFF1E293B)
val MedicalCardBorder = Color(0xFF334155)
val MedicalAccent = Color(0xFF0D9488)

// 🏢 4. العقارات والمباني
val EstateScreenBg = Color(0xFF0F172A)
val EstateCardSurface = Color(0xFF1E293B)
val EstateCardBorder = Color(0xFF334155)
val EstateAccent = Color(0xFFB45309)

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
