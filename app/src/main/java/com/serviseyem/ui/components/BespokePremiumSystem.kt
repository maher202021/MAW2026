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
 * Hyper-luxurious theme design system components with distinctive category-tailored palettes.
 * Strictly NO Pure White #FFFFFF or Black #000000.
 */
object BespokePremiumSystem {

    val DeepDark = AppBackground
    val SurfaceDark = CardSurface
    val GlassBorder = CardBorder
    val EmeraldAccent = DarkCyanPrimary
    val WarmOrange = GoldShine
    val CyanMedical = SecondaryTeal
    val RoyalPurple = MutedPurple
    val ElectricBlue = RichBlueTeal

    val BrandGradient = GoldShineGradient
    val StoresGradient = DarkCyanGradient
    val FoodGradient = Brush.horizontalGradient(listOf(DarkCyanPrimary, ErrorCoral))
    val MedicalGradient = Brush.horizontalGradient(listOf(DarkCyanPrimary, SecondaryTeal))
    val EstateGradient = Brush.horizontalGradient(listOf(DarkCyanPrimary, RichBlueTeal))
    val JobsGradient = Brush.horizontalGradient(listOf(DarkCyanPrimary, MutedPurple))
    val ServicesGradient = Brush.horizontalGradient(listOf(DarkCyanPrimary, SoftIndigo))
    val GoldBannerGradient = GoldShineGradient

    @Composable
    fun SectionBackHeader(title: String, onBackClick: () -> Unit) {
        androidx.activity.compose.BackHandler { onBackClick() }
        Surface(
            onClick = onBackClick,
            shape = RoundedCornerShape(14.dp),
            color = SurfaceDark,
            border = BorderStroke(1.dp, GlassBorder),
            modifier = Modifier.fillMaxWidth().padding(horizontal = 6.dp, vertical = 6.dp)
        ) {
            Row(modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Icon(Icons.Filled.ArrowForward, contentDescription = "رجوع", tint = EmeraldAccent, modifier = Modifier.size(20.dp))
                    Text(text = "العودة", color = TextLightMint, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                }
                Surface(shape = RoundedCornerShape(8.dp), color = EmeraldAccent.copy(alpha = 0.12f), border = BorderStroke(0.5.dp, GlassBorder)) {
                    Text(text = title, color = EmeraldAccent, fontSize = 11.sp, fontWeight = FontWeight.SemiBold, modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp))
                }
            }
        }
    }

    @Composable
    fun TrustStatisticsBar() {
        Surface(shape = RoundedCornerShape(16.dp), color = SurfaceDark, border = BorderStroke(1.dp, GlassBorder), modifier = Modifier.fillMaxWidth().padding(horizontal = 6.dp, vertical = 4.dp)) {
            Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 10.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceEvenly) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("1,500+", color = EmeraldAccent, fontSize = 13.sp, fontWeight = FontWeight.Black)
                    Text("مزود خدمة", color = TextMutedMint, fontSize = 9.sp)
                }
                Box(modifier = Modifier.height(24.dp).width(1.dp).background(TextMutedMint.copy(alpha = 0.2f)))
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("12", color = CyanMedical, fontSize = 13.sp, fontWeight = FontWeight.Black)
                    Text("مدينة", color = TextMutedMint, fontSize = 9.sp)
                }
                Box(modifier = Modifier.height(24.dp).width(1.dp).background(TextMutedMint.copy(alpha = 0.2f)))
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("45,000+", color = WarmOrange, fontSize = 13.sp, fontWeight = FontWeight.Black)
                    Text("حجز", color = TextMutedMint, fontSize = 9.sp)
                }
            }
        }
    }

    @Composable
    fun JoinAsProviderBanner(onJoinClick: () -> Unit) {
        Surface(onClick = onJoinClick, shape = RoundedCornerShape(18.dp), color = Color.Transparent, border = BorderStroke(1.dp, WarmOrange.copy(alpha = 0.5f)), modifier = Modifier.fillMaxWidth().padding(horizontal = 6.dp, vertical = 6.dp)) {
            Box(modifier = Modifier.fillMaxWidth().background(GoldBannerGradient).padding(horizontal = 16.dp, vertical = 12.dp)) {
                Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("👑 انضم كمزوّد خدمة", color = TextLightMint, fontSize = 14.sp, fontWeight = FontWeight.Black)
                        Text("سجّل نشاطك الآن واستقبل الطلبات", color = TextLightMint.copy(alpha = 0.9f), fontSize = 10.sp)
                    }
                    Surface(shape = RoundedCornerShape(10.dp), color = TextLightMint, modifier = Modifier.padding(start = 8.dp)) {
                        Text(text = "سجّل الآن ✨", color = FoodCardSurface, fontSize = 11.sp, fontWeight = FontWeight.ExtraBold, modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp))
                    }
                }
            }
        }
    }

    @Composable
    fun GlassmorphicSurface(modifier: Modifier = Modifier, shape: RoundedCornerShape = RoundedCornerShape(20.dp), content: @Composable ColumnScope.() -> Unit) {
        Surface(modifier = modifier, shape = shape, color = SurfaceDark, border = BorderStroke(1.dp, GlassBorder), tonalElevation = 2.dp) {
            Column(modifier = Modifier.padding(12.dp)) { content() }
        }
    }

    @Composable
    fun SectionHeader(title: String, subtitle: String, icon: ImageVector, accentColor: Color, onActionClick: (() -> Unit)? = null, actionLabel: String = "") {
        Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 12.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Surface(shape = CircleShape, color = accentColor.copy(alpha = 0.15f), modifier = Modifier.size(40.dp)) {
                    Box(contentAlignment = Alignment.Center) { Icon(icon, contentDescription = null, tint = accentColor, modifier = Modifier.size(20.dp)) }
                }
                Column {
                    Text(title, color = CardDarkTitle, fontSize = 17.sp, fontWeight = FontWeight.ExtraBold)
                    Text(subtitle, color = CardMutedText, fontSize = 11.sp)
                }
            }
            if (onActionClick != null) {
                TextButton(onClick = onActionClick) { Text(actionLabel, color = accentColor, fontSize = 12.sp, fontWeight = FontWeight.Bold) }
            }
        }
    }

    val CardPureWhite = CardSurface // Deprecated alias, points to Tinted CardSurface
    val CardDarkTitle = Color(0xFF0F2B48) // Crisp Deep Navy
    val CardMutedText = Color(0xFF3B648B)
    val CardBorderLight = CardBorder
    val ActionEmeraldGradient = Brush.horizontalGradient(listOf(Color(0xFF0D5C3A), Color(0xFF147D72)))
    val ButtonLightText = Color(0xFFE0F2FE)

    // 🏪 بطاقة المتجر والمحلات
    @Composable
    fun TechHubStoreCard(title: String, imageUrl: String, rating: Float, priceRange: String, onCartClick: () -> Unit, onClick: () -> Unit) {
        Surface(
            onClick = onClick,
            shape = RoundedCornerShape(20.dp),
            color = StoreCardSurface,
            border = BorderStroke(1.dp, StoreCardBorder),
            shadowElevation = 6.dp,
            modifier = Modifier.fillMaxWidth().padding(6.dp)
        ) {
            Column {
                AsyncImage(
                    model = imageUrl,
                    contentDescription = title,
                    modifier = Modifier.fillMaxWidth().height(115.dp).clip(RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp)),
                    contentScale = ContentScale.Crop
                )
                Column(modifier = Modifier.padding(12.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(title, color = CardDarkTitle, fontSize = 14.sp, fontWeight = FontWeight.ExtraBold, maxLines = 1, modifier = Modifier.weight(1f))
                        Surface(shape = CircleShape, color = Color(0xFF00B894).copy(alpha = 0.15f), modifier = Modifier.size(18.dp)) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(Icons.Filled.Check, contentDescription = "موثق", tint = Color(0xFF00B894), modifier = Modifier.size(12.dp))
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            Icon(Icons.Filled.Star, contentDescription = null, tint = Color(0xFFF59E0B), modifier = Modifier.size(13.dp))
                            Text(rating.toString(), color = CardDarkTitle, fontSize = 11.5.sp, fontWeight = FontWeight.Bold)
                        }
                        Surface(shape = RoundedCornerShape(6.dp), color = StoreCardBorder.copy(alpha = 0.4f)) {
                            Text(priceRange, color = StoreAccent, fontSize = 10.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
                        }
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(38.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(ActionEmeraldGradient)
                            .clickable { onCartClick() },
                        contentAlignment = Alignment.Center
                    ) {
                        Text("عرض المتجر 🛍️", color = ButtonLightText, fontSize = 11.5.sp, fontWeight = FontWeight.ExtraBold)
                    }
                }
            }
        }
    }

    // 🍔 بطاقة المطعم والكافيهات (Warm Peach Card)
    @Composable
    fun FoodieRestaurantCard(title: String, imageUrl: String, deliveryTime: String, price: String, onClick: () -> Unit, onAddClick: () -> Unit) {
        Surface(
            onClick = onClick,
            shape = RoundedCornerShape(20.dp),
            color = FoodCardSurface,
            border = BorderStroke(1.dp, FoodCardBorder),
            shadowElevation = 6.dp,
            modifier = Modifier.fillMaxWidth().padding(horizontal = 6.dp, vertical = 6.dp)
        ) {
            Row(modifier = Modifier.fillMaxWidth().padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                AsyncImage(model = imageUrl, contentDescription = title, modifier = Modifier.size(86.dp).clip(RoundedCornerShape(14.dp)), contentScale = ContentScale.Crop)
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(title, color = CardDarkTitle, fontSize = 15.sp, fontWeight = FontWeight.ExtraBold, maxLines = 1)
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        Icon(Icons.Filled.Timer, contentDescription = null, tint = Color(0xFFF59E0B), modifier = Modifier.size(13.dp))
                        Text(deliveryTime, color = CardMutedText, fontSize = 11.sp, fontWeight = FontWeight.Medium)
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(price, color = FoodAccent, fontSize = 13.sp, fontWeight = FontWeight.ExtraBold)
                }
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(ActionEmeraldGradient)
                        .clickable { onAddClick() },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Filled.Add, contentDescription = "إضافة", tint = ButtonLightText, modifier = Modifier.size(18.dp))
                }
            }
        }
    }

    // 🏥 بطاقة المركز الطبي والعيادات (Calming Seafoam Card)
    @Composable
    fun MedicalClinicalCard(name: String, specialty: String, isOpen: Boolean, onBook: () -> Unit, onCall: () -> Unit, onNav: () -> Unit) {
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = MedicalCardSurface,
            border = BorderStroke(1.dp, MedicalCardBorder),
            shadowElevation = 6.dp,
            modifier = Modifier.fillMaxWidth().padding(horizontal = 6.dp, vertical = 6.dp)
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    Surface(shape = RoundedCornerShape(12.dp), color = MedicalAccent.copy(alpha = 0.15f), modifier = Modifier.size(46.dp)) {
                        Box(contentAlignment = Alignment.Center) { Icon(Icons.Filled.LocalHospital, contentDescription = null, tint = MedicalAccent) }
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(name, color = CardDarkTitle, fontSize = 15.sp, fontWeight = FontWeight.ExtraBold)
                        Text(specialty, color = CardMutedText, fontSize = 11.5.sp)
                    }
                    Surface(shape = RoundedCornerShape(8.dp), color = if (isOpen) Color(0xFF00B894).copy(alpha = 0.15f) else Color(0xFFEF4444).copy(alpha = 0.15f)) {
                        Text(if (isOpen) "متاح الآن" else "مغلق", color = if (isOpen) Color(0xFF00B894) else Color(0xFFEF4444), fontSize = 10.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp))
                    }
                }
                Spacer(modifier = Modifier.height(14.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Box(
                        modifier = Modifier
                            .weight(1.5f)
                            .height(40.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(ActionEmeraldGradient)
                            .clickable { onBook() },
                        contentAlignment = Alignment.Center
                    ) {
                        Text("حجز موعد الآن", color = ButtonLightText, fontWeight = FontWeight.ExtraBold, fontSize = 12.sp)
                    }
                    Surface(onClick = onCall, shape = RoundedCornerShape(10.dp), color = MedicalCardBorder.copy(alpha = 0.5f), modifier = Modifier.size(40.dp)) {
                        Box(contentAlignment = Alignment.Center) { Icon(Icons.Filled.Phone, contentDescription = "اتصال", tint = MedicalAccent, modifier = Modifier.size(18.dp)) }
                    }
                    Surface(onClick = onNav, shape = RoundedCornerShape(10.dp), color = MedicalCardBorder.copy(alpha = 0.5f), modifier = Modifier.size(40.dp)) {
                        Box(contentAlignment = Alignment.Center) { Icon(Icons.Filled.Directions, contentDescription = "الموقع", tint = Color(0xFF00B894), modifier = Modifier.size(18.dp)) }
                    }
                }
            }
        }
    }

    // 💼 بطاقة الوظائف والفرص (Periwinkle Card)
    @Composable
    fun LinkedInJobCard(title: String, company: String, type: String, salary: String, logoUrl: String, onApply: () -> Unit) {
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = JobsCardSurface,
            border = BorderStroke(1.dp, JobsCardBorder),
            shadowElevation = 6.dp,
            modifier = Modifier.fillMaxWidth().padding(horizontal = 6.dp, vertical = 6.dp)
        ) {
            Row(modifier = Modifier.fillMaxWidth().padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                Surface(shape = RoundedCornerShape(12.dp), color = JobsAccent.copy(alpha = 0.15f), modifier = Modifier.size(46.dp)) {
                    Box(contentAlignment = Alignment.Center) { Icon(Icons.Filled.Work, contentDescription = null, tint = JobsAccent) }
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(title, color = CardDarkTitle, fontSize = 14.5.sp, fontWeight = FontWeight.ExtraBold)
                    Text(company, color = CardMutedText, fontSize = 11.5.sp)
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text(type, color = JobsAccent, fontSize = 10.5.sp, fontWeight = FontWeight.SemiBold)
                        if (salary.isNotBlank()) {
                            Text("•", color = CardMutedText)
                            Text(salary, color = Color(0xFF00B894), fontSize = 10.5.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(10.dp))
                        .background(ActionEmeraldGradient)
                        .clickable { onApply() }
                        .padding(horizontal = 14.dp, vertical = 9.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text("تقديم", color = ButtonLightText, fontSize = 11.5.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }

    // 🏢 بطاقة العقارات الفخمة (Cashmere Sand Card)
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
            color = EstateCardSurface,
            border = BorderStroke(1.dp, EstateCardBorder),
            shadowElevation = 6.dp,
            modifier = Modifier.fillMaxWidth().padding(horizontal = 6.dp, vertical = 6.dp)
        ) {
            Column {
                AsyncImage(model = imageUrl, contentDescription = title, modifier = Modifier.fillMaxWidth().height(140.dp).clip(RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp)), contentScale = ContentScale.Crop)
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Text(title, color = CardDarkTitle, fontSize = 15.sp, fontWeight = FontWeight.ExtraBold, modifier = Modifier.weight(1f))
                        Text(price, color = EstateAccent, fontSize = 14.sp, fontWeight = FontWeight.Black)
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(location, color = CardMutedText, fontSize = 11.5.sp)
                    Spacer(modifier = Modifier.height(10.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Surface(shape = RoundedCornerShape(6.dp), color = EstateCardBorder.copy(alpha = 0.4f)) {
                            Text("$rooms غرف • $area م²", color = CardDarkTitle, fontSize = 10.5.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp))
                        }
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            Surface(onClick = onBookmark, shape = CircleShape, color = EstateCardBorder.copy(alpha = 0.4f), modifier = Modifier.size(34.dp)) {
                                Box(contentAlignment = Alignment.Center) { Icon(Icons.Filled.BookmarkBorder, contentDescription = null, tint = CardDarkTitle, modifier = Modifier.size(16.dp)) }
                            }
                            Surface(onClick = onShare, shape = CircleShape, color = EstateCardBorder.copy(alpha = 0.4f), modifier = Modifier.size(34.dp)) {
                                Box(contentAlignment = Alignment.Center) { Icon(Icons.Filled.Share, contentDescription = null, tint = CardDarkTitle, modifier = Modifier.size(16.dp)) }
                            }
                        }
                    }
                }
            }
        }
    }
}
