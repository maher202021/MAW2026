package com.serviseyem.ui.theme

import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

// 💎 World-Class Clean Design System (High Contrast & Professional Aesthetics)
// Clean neutral backgrounds and crisp white cards for maximum readability and visual polish
val AppBackground = Color(0xFFF8FAFC)        // Clean ultra-light neutral background (#F8FAFC / Slate-50)
val CardSurface = Color(0xFFFFFFFF)          // Pure crisp white cards
val CardBorder = Color(0xFFE2E8F0)           // Subtle border (#E2E8F0 / Slate-200)
val GlassBorder = Color(0xFFCBD5E1)          // Slate-300

// 🌟 Core Brand Colors (Trust & Reliability)
val PrimaryBlue = Color(0xFF2563EB)          // Professional Indigo/Royal Blue (#2563EB)
val DarkCyanPrimary = PrimaryBlue            // Backward-compatible alias
val SecondaryTeal = Color(0xFF0D9488)        // Medical/Teal accent
val GoldShine = Color(0xFFF59E0B)            // Warm Golden Amber / CTA accent
val MutedGold = Color(0xFFD97706)            // Deep Amber
val RichBlueTeal = Color(0xFF0284C7)         // Deep Sky Blue
val SoftIndigo = Color(0xFF4F46E5)           // Royal Indigo
val MutedPurple = Color(0xFF7C3AED)          // Violet

// 🖋️ High-Contrast Typography (Crisp Deep Navy / Charcoal - Zero eye-strain)
val TextLightMint = Color(0xFF1E293B)        // Primary Text: Royal Dark Navy (#1E293B / Slate-800)
val TextDarkTitle = Color(0xFF0F172A)        // Headings: Deepest Navy (#0F172A / Slate-900)
val TextMutedMint = Color(0xFF475569)        // Secondary Text: Muted Slate (#475569 / Slate-600)
val TextMutedLight = Color(0xFF64748B)       // Tertiary Text: (#64748B / Slate-500)
val White = Color(0xFFFFFFFF)                // Pure White
val Black = Color(0xFF0F172A)                // Deep Navy Slate
val ButtonLightText = Color(0xFFFFFFFF)      // Pure White for buttons over dark/accent backgrounds

// 🟢 Status Colors
val SuccessGreen = Color(0xFF059669)         // Emerald Green (#059669)
val WarningAmber = Color(0xFFF59E0B)         // Warm Amber (#F59E0B)
val ErrorCoral = Color(0xFFDC2626)           // Red-600

// 🎨 Sector-Specific Identities (Cards stay pure white #FFFFFF, sector accents highlight buttons & badges)
// 🏪 1. المحلات والمتاجر (Emerald Green - التجارة والنمو)
val StoreAccent = Color(0xFF059669)
val StoreBorder = Color(0xFFA7F3D0)
val StoreSurface = Color(0xFFFFFFFF)
val StoreScreenBg = Color(0xFFF8FAFC)

// 🍔 2. المطاعم والكافيهات (Warm Orange / Coral - الضيافة والأغذية)
val FoodAccent = Color(0xFFEA580C)
val FoodBorder = Color(0xFFFED7AA)
val FoodSurface = Color(0xFFFFFFFF)
val FoodScreenBg = Color(0xFFF8FAFC)

// 🏥 3. المراكز الطبية والعيادات (Medical Teal / Cyan - النظافة والأمان)
val MedicalAccent = Color(0xFF0D9488)
val MedicalBorder = Color(0xFF99F6E4)
val MedicalSurface = Color(0xFFFFFFFF)
val MedicalScreenBg = Color(0xFFF8FAFC)

// 🏢 4. العقارات والوظائف (Royal Indigo / Deep Blue - الاحترافية والعمل)
val EstateAccent = Color(0xFF4F46E5)
val EstateBorder = Color(0xFFC7D2FE)
val EstateSurface = Color(0xFFFFFFFF)
val EstateScreenBg = Color(0xFFF8FAFC)

val JobsAccent = Color(0xFF4F46E5)
val JobsBorder = Color(0xFFC7D2FE)
val JobsSurface = Color(0xFFFFFFFF)
val JobsScreenBg = Color(0xFFF8FAFC)

// 🔗 Legacy & Compatibility Aliases
val SoftLightSageBg = AppBackground
val LightCardSurface = CardSurface
val LightCardBorder = CardBorder
val DeepEmerald = StoreAccent
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
val FoodFamilySecondary = FoodBorder
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
val TextPrimaryWhite = TextLightMint
val TextSecondarySlate = TextMutedMint
val TextMutedGray = TextMutedMint
val LightGray = CardSurface
val DarkGray = AppBackground
val MediumGray = TextMutedMint

// 🌈 Pre-configured Brushes & Gradients
val GoldGradientStart = GoldShine
val GoldGradientEnd = Color(0xFFD97706)
val GlowingCoralStart = FoodAccent
val SkyBlueStart = PrimaryBlue

val GoldShineGradient = Brush.horizontalGradient(listOf(GoldGradientStart, GoldGradientEnd))
val DarkCyanGradient = Brush.horizontalGradient(listOf(PrimaryBlue, Color(0xFF1D4ED8)))
val DarkCyanBackgroundGradient = Brush.verticalGradient(listOf(Color(0xFFF8FAFC), Color(0xFFF1F5F9)))
val LuxuryGoldGradient = GoldShineGradient
val LuxuryEmeraldGradient = Brush.horizontalGradient(listOf(Color(0xFF059669), Color(0xFF047857)))
val LuxurySkyBlueGradient = Brush.horizontalGradient(listOf(PrimaryBlue, Color(0xFF0284C7)))
val LuxuryCoralGradient = Brush.horizontalGradient(listOf(FoodAccent, Color(0xFFC2410C)))
val LuxuryDarkNavyGradient = DarkCyanBackgroundGradient
