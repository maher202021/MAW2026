package com.serviseyem.ui.theme

import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

// 💎 Dark Luxury Yemeni Directory Color Tokens (#070A13)
val DarkLuxuryBackground = Color(0xFF070A13)
val SecondaryBg = Color(0xFF0C1220)
val CardSurface = Color(0xFF121A2C)
val CardBorder = Color(0xFF182238)
val GlassBorder = Color.White.copy(alpha = 0.08f)

// 🌟 Accent Gradients & Neon Palette
val GoldGradientStart = Color(0xFFFFC93C)
val GoldGradientEnd = Color(0xFFFF9F1C)
val EmeraldNeonStart = Color(0xFF00E676)
val EmeraldNeonEnd = Color(0xFF00B894)
val SkyBlueStart = Color(0xFF38B6FF)
val SkyBlueEnd = Color(0xFF0E90FF)
val GlowingCoralStart = Color(0xFFFF7EB3)
val GlowingCoralEnd = Color(0xFFFF2D6F)

// 🎨 5 Section Family Theme Palettes (عوائل الأقسام الـ5)
// 1. Food & Drinks (عائلة الأطعمة والمشروبات - حمراء شهية)
val FoodFamilyPrimary = Color(0xFFFF4B4B)
val FoodFamilySecondary = Color(0xFFFF8000)
val FoodFamilyGold = Color(0xFFFFC93C)

// 2. Tech & Stores (عائلة التقنية والمتاجر - كحلية عميقة)
val TechFamilyPrimary = Color(0xFF102A43)
val TechFamilySecondary = Color(0xFF38B6FF)
val TechFamilyCyan = Color(0xFF00E5FF)

// 3. Travel & Tourism (عائلة السياحة والسفر - تركواز وسفر)
val TravelFamilyPrimary = Color(0xFF00C9A7)
val TravelFamilySecondary = Color(0xFF0E90FF)
val TravelFamilySunset = Color(0xFFFFC93C)

// 4. Health & Elegance (عائلة الصحة والأناقة - أرجوانية فاخرة)
val HealthFamilyPrimary = Color(0xFF8B5CF6)
val HealthFamilySecondary = Color(0xFFFF7EB3)
val HealthFamilyGold = Color(0xFFFFC93C)

// 5. Home & Professional Services (عائلة الخدمات المنزلية والمهنية - زمردية اعتمادية)
val ServicesFamilyPrimary = Color(0xFF00E676)
val ServicesFamilySecondary = Color(0xFF76FF03)
val ServicesFamilyTeal = Color(0xFF00B894)

// Standard Text & UI Colors
val TextPrimaryWhite = Color(0xFFF8FAFC)
val TextSecondarySlate = Color(0xFF94A3B8)
val TextMutedGray = Color(0xFF64748B)

val White = Color(0xFFFFFFFF)
val Black = Color(0xFF000000)
val LightGray = Color(0xFFF1F5F9)
val DarkGray = Color(0xFF121A2C)
val MediumGray = Color(0xFF64748B)

val SuccessGreen = Color(0xFF00E676)
val WarningAmber = Color(0xFFFFC93C)
val ErrorPink = Color(0xFFFF2D6F)

// 🌈 Pre-configured Brushes & Gradients
val LuxuryGoldGradient = Brush.horizontalGradient(listOf(GoldGradientStart, GoldGradientEnd))
val LuxuryEmeraldGradient = Brush.horizontalGradient(listOf(EmeraldNeonStart, EmeraldNeonEnd))
val LuxurySkyBlueGradient = Brush.horizontalGradient(listOf(SkyBlueStart, SkyBlueEnd))
val LuxuryCoralGradient = Brush.horizontalGradient(listOf(GlowingCoralStart, GlowingCoralEnd))
val LuxuryDarkNavyGradient = Brush.verticalGradient(listOf(SecondaryBg, DarkLuxuryBackground))




