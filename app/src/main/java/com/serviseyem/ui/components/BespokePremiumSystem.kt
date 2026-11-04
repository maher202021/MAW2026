package com.serviseyem.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import coil.compose.AsyncImage
import com.serviseyem.ui.theme.*

/**
 * 💎 BespokePremiumSystem
 * World-Class Clean Design System with High Contrast (#FFFFFF cards on #F8FAFC background).
 * Sector-specific accents highlight actions without causing contrast fatigue.
 */
object BespokePremiumSystem {

    val DeepDark = Color(0xFF0F172A)
    val SurfaceDark = Color(0xFF1E293B)
    val GlassBorder = Color(0xFF334155)
    val EmeraldAccent = Color(0xFF059669)
    val WarmOrange = Color(0xFFEA580C)
    val CyanMedical = Color(0xFF0D9488)
    val RoyalPurple = Color(0xFF4F46E5)
    val ElectricBlue = Color(0xFF2563EB)

    val BrandGradient = Brush.horizontalGradient(listOf(Color(0xFF2563EB), Color(0xFF1D4ED8)))
    val StoresGradient = Brush.horizontalGradient(listOf(Color(0xFF059669), Color(0xFF047857)))
    val FoodGradient = Brush.horizontalGradient(listOf(Color(0xFFEA580C), Color(0xFFC2410C)))
    val MedicalGradient = Brush.horizontalGradient(listOf(Color(0xFF0D9488), Color(0xFF0F766E)))
    val EstateGradient = Brush.horizontalGradient(listOf(Color(0xFF4F46E5), Color(0xFF3730A3)))
    val JobsGradient = Brush.horizontalGradient(listOf(Color(0xFF4F46E5), Color(0xFF6366F1)))
    val ServicesGradient = BrandGradient
    val GoldBannerGradient = Brush.horizontalGradient(listOf(Color(0xFFF59E0B), Color(0xFFD97706)))

    val CardPureWhite = Color(0xFF1E293B)
    val CardDarkTitle = Color(0xFFFFFFFF)  // Pure White Heading
    val CardMutedText = Color(0xFFCBD5E1)  // Soft Slate Text
    val CardBorderLight = Color(0xFF334155) // Dark glass border
    val ButtonLightText = Color(0xFFFFFFFF)

    @Composable
    fun SectionBackHeader(title: String, onBackClick: () -> Unit) {
        androidx.activity.compose.BackHandler { onBackClick() }
        Surface(
            onClick = onBackClick,
            shape = RoundedCornerShape(14.dp),
            color = Color(0xFF1E293B),
            border = BorderStroke(1.dp, CardBorderLight),
            shadowElevation = 2.dp,
            modifier = Modifier.fillMaxWidth().padding(horizontal = 6.dp, vertical = 6.dp)
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Icon(Icons.Filled.ArrowForward, contentDescription = "رجوع", tint = ElectricBlue, modifier = Modifier.size(20.dp))
                    Text(text = "العودة للرئيسية", color = CardDarkTitle, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                }
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = Color(0xFF0F172A),
                    border = BorderStroke(0.8.dp, Color(0xFF334155))
                ) {
                    Text(text = title, color = ElectricBlue, fontSize = 11.5.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp))
                }
            }
        }
    }

    @Composable
    fun TrustStatisticsBar() {
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = Color(0xFF1E293B),
            border = BorderStroke(1.dp, CardBorderLight),
            shadowElevation = 2.dp,
            modifier = Modifier.fillMaxWidth().padding(horizontal = 6.dp, vertical = 4.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("1,500+", color = EmeraldAccent, fontSize = 14.sp, fontWeight = FontWeight.Black)
                    Text("مزود خدمة", color = CardMutedText, fontSize = 9.5.sp)
                }
                Box(modifier = Modifier.height(24.dp).width(1.dp).background(Color(0xFF334155)))
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("12", color = ElectricBlue, fontSize = 14.sp, fontWeight = FontWeight.Black)
                    Text("مدينة", color = CardMutedText, fontSize = 9.5.sp)
                }
                Box(modifier = Modifier.height(24.dp).width(1.dp).background(Color(0xFF334155)))
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("45,000+", color = Color(0xFFF59E0B), fontSize = 14.sp, fontWeight = FontWeight.Black)
                    Text("حجز وطلب", color = CardMutedText, fontSize = 9.5.sp)
                }
            }
        }
    }

    @Composable
    fun JoinAsProviderBanner(onJoinClick: () -> Unit) {
        Surface(
            onClick = onJoinClick,
            shape = RoundedCornerShape(18.dp),
            color = Color.Transparent,
            border = BorderStroke(1.dp, Color(0xFFD4A017)),
            shadowElevation = 4.dp,
            modifier = Modifier.fillMaxWidth().padding(horizontal = 6.dp, vertical = 6.dp)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Brush.horizontalGradient(listOf(Color(0xFF1E293B), Color(0xFF0F172A))))
                    .padding(horizontal = 16.dp, vertical = 12.dp)
            ) {
                Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("👑 انضم كمزوّد خدمة معتمد", color = Color(0xFFFFC93C), fontSize = 14.sp, fontWeight = FontWeight.Black)
                        Text("سجّل نشاطك التجاري الآن واستقبل الطلبات مباشرة", color = Color(0xFFCBD5E1), fontSize = 10.5.sp)
                    }
                    Surface(shape = RoundedCornerShape(10.dp), color = Color(0xFFD4A017), modifier = Modifier.padding(start = 8.dp)) {
                        Text(text = "سجّل الآن ✨", color = Color(0xFF0F172A), fontSize = 11.5.sp, fontWeight = FontWeight.Black, modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp))
                    }
                }
            }
        }
    }

    @Composable
    fun GlassmorphicSurface(modifier: Modifier = Modifier, shape: RoundedCornerShape = RoundedCornerShape(20.dp), content: @Composable ColumnScope.() -> Unit) {
        Surface(modifier = modifier, shape = shape, color = Color(0xFF1E293B), border = BorderStroke(1.dp, CardBorderLight), shadowElevation = 2.dp) {
            Column(modifier = Modifier.padding(12.dp)) { content() }
        }
    }

    @Composable
    fun SectionHeader(title: String, subtitle: String, icon: ImageVector, accentColor: Color, onActionClick: (() -> Unit)? = null, actionLabel: String = "") {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Surface(shape = CircleShape, color = accentColor.copy(alpha = 0.12f), modifier = Modifier.size(38.dp)) {
                    Box(contentAlignment = Alignment.Center) { Icon(icon, contentDescription = null, tint = accentColor, modifier = Modifier.size(20.dp)) }
                }
                Column {
                    Text(title, color = CardDarkTitle, fontSize = 16.sp, fontWeight = FontWeight.Black)
                    Text(subtitle, color = CardMutedText, fontSize = 11.sp)
                }
            }
            if (onActionClick != null && actionLabel.isNotBlank()) {
                Surface(
                    onClick = onActionClick,
                    shape = RoundedCornerShape(8.dp),
                    color = accentColor.copy(alpha = 0.12f),
                    border = BorderStroke(0.8.dp, accentColor.copy(alpha = 0.3f))
                ) {
                    Text(actionLabel, color = accentColor, fontSize = 11.5.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp))
                }
            }
        }
    }

    // 🏪 بطاقة المتجر والمحلات (#1E293B + Emerald Green CTA)
    @Composable
    fun TechHubStoreCard(title: String, imageUrl: String, rating: Float, priceRange: String, onCartClick: () -> Unit, onClick: () -> Unit) {
        Surface(
            onClick = onClick,
            shape = RoundedCornerShape(20.dp),
            color = Color(0xFF1E293B),
            border = BorderStroke(1.dp, CardBorderLight),
            shadowElevation = 3.dp,
            modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 6.dp)
        ) {
            Column {
                SmartAsyncImage(
                    model = imageUrl,
                    contentDescription = title,
                    modifier = Modifier.fillMaxWidth().aspectRatio(16f / 9f).clip(RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp)),
                    contentScale = ContentScale.Crop
                )
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(title, color = CardDarkTitle, fontSize = 14.sp, fontWeight = FontWeight.ExtraBold, maxLines = 1, modifier = Modifier.weight(1f))
                        Surface(shape = CircleShape, color = Color(0xFF064E3B), modifier = Modifier.size(18.dp)) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(Icons.Filled.Check, contentDescription = "موثق", tint = EmeraldAccent, modifier = Modifier.size(12.dp))
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            Icon(Icons.Filled.Star, contentDescription = null, tint = Color(0xFFFBBF24), modifier = Modifier.size(13.dp))
                            Text(rating.toString(), color = CardDarkTitle, fontSize = 11.5.sp, fontWeight = FontWeight.Bold)
                        }
                        Surface(shape = RoundedCornerShape(6.dp), color = Color(0xFF064E3B)) {
                            Text(priceRange, color = EmeraldAccent, fontSize = 10.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
                        }
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(44.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(StoresGradient)
                            .clickable { onCartClick() },
                        contentAlignment = Alignment.Center
                    ) {
                        Text("عرض المتجر 🛍️", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.ExtraBold)
                    }
                }
            }
        }
    }

    // 🍔 بطاقة المطعم والكافيهات (#121A2C + Mature Red #E11D48 CTA)
    @Composable
    fun FoodieRestaurantCard(title: String, imageUrl: String, deliveryTime: String, price: String, onClick: () -> Unit, onAddClick: () -> Unit) {
        Surface(
            onClick = onClick,
            shape = RoundedCornerShape(20.dp),
            color = Color(0xFF121A2C),
            border = BorderStroke(1.dp, CardBorderLight),
            shadowElevation = 3.dp,
            modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 6.dp)
        ) {
            Row(modifier = Modifier.fillMaxWidth().padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                SmartAsyncImage(model = imageUrl, contentDescription = title, modifier = Modifier.size(86.dp).clip(RoundedCornerShape(16.dp)), contentScale = ContentScale.Crop)
                Spacer(modifier = Modifier.width(14.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(title, color = CardDarkTitle, fontSize = 15.sp, fontWeight = FontWeight.ExtraBold, maxLines = 1)
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        Icon(Icons.Filled.Timer, contentDescription = null, tint = Color(0xFFE8B23A), modifier = Modifier.size(13.dp))
                        Text(deliveryTime, color = CardMutedText, fontSize = 11.5.sp, fontWeight = FontWeight.Medium)
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(price, color = Color(0xFFE11D48), fontSize = 13.5.sp, fontWeight = FontWeight.ExtraBold)
                }
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Brush.horizontalGradient(listOf(Color(0xFFE11D48), Color(0xFFBE123C))))
                        .clickable { onAddClick() },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Filled.Add, contentDescription = "إضافة", tint = Color.White, modifier = Modifier.size(20.dp))
                }
            }
        }
    }

    // 🏥 بطاقة المركز الطبي والعيادات (#121A2C + Medical Teal CTA)
    @Composable
    fun MedicalClinicalCard(name: String, specialty: String, isOpen: Boolean, onBook: () -> Unit, onCall: () -> Unit, onNav: () -> Unit, onClick: () -> Unit = onBook) {
        Surface(
            onClick = onClick,
            shape = RoundedCornerShape(20.dp),
            color = Color(0xFF121A2C),
            border = BorderStroke(1.dp, CardBorderLight),
            shadowElevation = 3.dp,
            modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 6.dp)
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    Surface(shape = RoundedCornerShape(12.dp), color = Color(0xFF0D9488), modifier = Modifier.size(46.dp)) {
                        Box(contentAlignment = Alignment.Center) { Icon(Icons.Filled.LocalHospital, contentDescription = null, tint = Color.White) }
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(name, color = CardDarkTitle, fontSize = 15.sp, fontWeight = FontWeight.ExtraBold)
                        Text(specialty, color = CardMutedText, fontSize = 11.5.sp)
                    }
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = if (isOpen) Color(0xFF064E3B) else Color(0xFF881337),
                        border = BorderStroke(0.5.dp, if (isOpen) Color(0xFF10B981) else Color(0xFFF43F5E))
                    ) {
                        Text(
                            text = if (isOpen) "متاح الآن 🟢" else "مغلق 🔴",
                            color = if (isOpen) Color(0xFF34D399) else Color(0xFFFB7185),
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.height(14.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Box(
                        modifier = Modifier
                            .weight(1.5f)
                            .height(44.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(MedicalGradient)
                            .clickable { onBook() },
                        contentAlignment = Alignment.Center
                    ) {
                        Text("عرض الملف والتفاصيل 🏥", color = Color.White, fontWeight = FontWeight.ExtraBold, fontSize = 12.sp)
                    }
                    Surface(onClick = onCall, shape = RoundedCornerShape(12.dp), color = Color(0xFF070A13), modifier = Modifier.size(44.dp)) {
                        Box(contentAlignment = Alignment.Center) { Icon(Icons.Filled.Phone, contentDescription = "اتصال", tint = CyanMedical, modifier = Modifier.size(18.dp)) }
                    }
                    Surface(onClick = onNav, shape = RoundedCornerShape(12.dp), color = Color(0xFF070A13), modifier = Modifier.size(44.dp)) {
                        Box(contentAlignment = Alignment.Center) { Icon(Icons.Filled.Directions, contentDescription = "الموقع", tint = ElectricBlue, modifier = Modifier.size(18.dp)) }
                    }
                }
            }
        }
    }

    // 💼 بطاقة الوظائف والفرص (#121A2C + Royal Indigo CTA)
    @Composable
    fun LinkedInJobCard(title: String, company: String, type: String, salary: String, logoUrl: String, onApply: () -> Unit, onClick: () -> Unit = onApply) {
        Surface(
            onClick = onClick,
            shape = RoundedCornerShape(20.dp),
            color = Color(0xFF121A2C),
            border = BorderStroke(1.dp, CardBorderLight),
            shadowElevation = 3.dp,
            modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 6.dp)
        ) {
            Row(modifier = Modifier.fillMaxWidth().padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                Surface(shape = RoundedCornerShape(12.dp), color = Color(0xFF312E81), modifier = Modifier.size(46.dp)) {
                    Box(contentAlignment = Alignment.Center) { Icon(Icons.Filled.Work, contentDescription = null, tint = Color.White) }
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(title, color = CardDarkTitle, fontSize = 14.5.sp, fontWeight = FontWeight.ExtraBold)
                    Text(company, color = CardMutedText, fontSize = 11.5.sp)
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text(type, color = Color(0xFF818CF8), fontSize = 10.5.sp, fontWeight = FontWeight.SemiBold)
                        if (salary.isNotBlank()) {
                            Text("•", color = CardMutedText)
                            Text(salary, color = EmeraldAccent, fontSize = 10.5.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
                Box(
                    modifier = Modifier
                        .height(44.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(JobsGradient)
                        .clickable { onApply() }
                        .padding(horizontal = 14.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text("عرض التفاصيل", color = Color.White, fontSize = 11.5.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }

    // 🏢 بطاقة العقارات الفخمة (#121A2C + Crisp Typography)
    @Composable
    fun EstateLuxuryCard(
        title: String,
        price: String,
        location: String,
        rooms: Int,
        area: String,
        imageUrl: String,
        onBookmark: () -> Unit,
        onShare: () -> Unit,
        onClick: () -> Unit
    ) {
        Surface(
            onClick = onClick,
            shape = RoundedCornerShape(20.dp),
            color = Color(0xFF121A2C),
            border = BorderStroke(1.dp, CardBorderLight),
            shadowElevation = 3.dp,
            modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 6.dp)
        ) {
            Column {
                SmartAsyncImage(model = imageUrl, contentDescription = title, modifier = Modifier.fillMaxWidth().aspectRatio(16f / 9f).clip(RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp)), contentScale = ContentScale.Crop)
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Text(title, color = CardDarkTitle, fontSize = 15.sp, fontWeight = FontWeight.ExtraBold, modifier = Modifier.weight(1f))
                        Text(price, color = Color(0xFF38BDF8), fontSize = 14.sp, fontWeight = FontWeight.Black)
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(location, color = CardMutedText, fontSize = 11.5.sp)
                    Spacer(modifier = Modifier.height(10.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Surface(shape = RoundedCornerShape(6.dp), color = Color(0xFF0F172A)) {
                            Text("$rooms غرف • $area م²", color = CardDarkTitle, fontSize = 10.5.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp))
                        }
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            Surface(onClick = onBookmark, shape = CircleShape, color = Color(0xFF0F172A), modifier = Modifier.size(34.dp)) {
                                Box(contentAlignment = Alignment.Center) { Icon(Icons.Filled.BookmarkBorder, contentDescription = null, tint = CardDarkTitle, modifier = Modifier.size(16.dp)) }
                            }
                            Surface(onClick = onShare, shape = CircleShape, color = Color(0xFF0F172A), modifier = Modifier.size(34.dp)) {
                                Box(contentAlignment = Alignment.Center) { Icon(Icons.Filled.Share, contentDescription = null, tint = CardDarkTitle, modifier = Modifier.size(16.dp)) }
                            }
                        }
                    }
                }
            }
        }
    }
}
