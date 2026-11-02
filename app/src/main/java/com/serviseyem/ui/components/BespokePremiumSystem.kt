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
 * Hyper-luxurious dark theme design system components with Elegant Dark Cyan and Gold Shine.
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
                Surface(shape = RoundedCornerShape(8.dp), color = EmeraldAccent.copy(alpha = 0.1f), border = BorderStroke(0.5.dp, GlassBorder)) {
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
                        Text(text = "سجّل الآن ✨", color = AppBackground, fontSize = 11.sp, fontWeight = FontWeight.ExtraBold, modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp))
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
                    Text(title, color = TextLightMint, fontSize = 17.sp, fontWeight = FontWeight.ExtraBold)
                    Text(subtitle, color = TextMutedMint, fontSize = 11.sp)
                }
            }
            if (onActionClick != null) {
                TextButton(onClick = onActionClick) { Text(actionLabel, color = accentColor, fontSize = 12.sp, fontWeight = FontWeight.Bold) }
            }
        }
    }

    @Composable
    fun TechHubStoreCard(title: String, imageUrl: String, rating: Float, priceRange: String, onCartClick: () -> Unit, onClick: () -> Unit) {
        Surface(onClick = onClick, shape = RoundedCornerShape(24.dp), color = SurfaceDark, border = BorderStroke(1.dp, GlassBorder), modifier = Modifier.fillMaxWidth().padding(6.dp)) {
            Box(modifier = Modifier.fillMaxWidth()) {
                Column {
                    AsyncImage(model = imageUrl, contentDescription = title, modifier = Modifier.fillMaxWidth().height(140.dp).clip(RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)), contentScale = ContentScale.Crop)
                    Column(modifier = Modifier.padding(14.dp)) {
                        Text(title, color = TextLightMint, fontSize = 15.sp, fontWeight = FontWeight.Bold, maxLines = 1)
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            Icon(Icons.Filled.Star, contentDescription = null, tint = WarmOrange, modifier = Modifier.size(14.dp))
                            Text(rating.toString(), color = TextLightMint, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            Text("•", color = TextMutedMint)
                            Text(priceRange, color = EmeraldAccent, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                        }
                    }
                }
                Surface(onClick = onCartClick, shape = CircleShape, color = EmeraldAccent, modifier = Modifier.align(Alignment.BottomEnd).padding(12.dp).size(36.dp)) {
                    Box(contentAlignment = Alignment.Center) { Icon(Icons.Filled.AddShoppingCart, contentDescription = null, tint = AppBackground, modifier = Modifier.size(18.dp)) }
                }
            }
        }
    }

    @Composable
    fun FoodieRestaurantCard(title: String, imageUrl: String, deliveryTime: String, price: String, onClick: () -> Unit, onAddClick: () -> Unit) {
        Card(onClick = onClick, shape = RoundedCornerShape(20.dp), colors = CardDefaults.cardColors(containerColor = SurfaceDark), border = BorderStroke(1.dp, GlassBorder), modifier = Modifier.fillMaxWidth().padding(8.dp)) {
            Row(modifier = Modifier.fillMaxWidth().padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                AsyncImage(model = imageUrl, contentDescription = title, modifier = Modifier.size(90.dp).clip(RoundedCornerShape(16.dp)), contentScale = ContentScale.Crop)
                Spacer(modifier = Modifier.width(16.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(title, color = TextLightMint, fontSize = 16.sp, fontWeight = FontWeight.ExtraBold)
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        Icon(Icons.Filled.Timer, contentDescription = null, tint = WarmOrange, modifier = Modifier.size(14.dp))
                        Text(deliveryTime, color = TextMutedMint, fontSize = 12.sp)
                    }
                    Text(price, color = EmeraldAccent, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                }
                Surface(onClick = onAddClick, shape = RoundedCornerShape(12.dp), color = ErrorCoral.copy(alpha = 0.15f), border = BorderStroke(1.dp, ErrorCoral), modifier = Modifier.size(40.dp)) {
                    Box(contentAlignment = Alignment.Center) { Icon(Icons.Filled.Add, contentDescription = null, tint = ErrorCoral) }
                }
            }
        }
    }

    @Composable
    fun MedicalClinicalCard(name: String, specialty: String, isOpen: Boolean, onBook: () -> Unit, onCall: () -> Unit, onNav: () -> Unit) {
        GlassmorphicSurface(modifier = Modifier.fillMaxWidth().padding(8.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Surface(shape = RoundedCornerShape(12.dp), color = CyanMedical.copy(alpha = 0.12f), modifier = Modifier.size(50.dp)) {
                    Box(contentAlignment = Alignment.Center) { Icon(Icons.Filled.LocalHospital, contentDescription = null, tint = CyanMedical) }
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(name, color = TextLightMint, fontSize = 15.sp, fontWeight = FontWeight.Bold)
                    Text(specialty, color = TextMutedMint, fontSize = 12.sp)
                }
                Surface(shape = RoundedCornerShape(8.dp), color = if (isOpen) Color(0xFF00BFA5) else ErrorCoral, modifier = Modifier.padding(4.dp)) {
                    Text(if (isOpen) "مفتوح" else "مغلق", color = AppBackground, fontSize = 10.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp))
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(onClick = onBook, modifier = Modifier.weight(1.5f).height(42.dp), colors = ButtonDefaults.buttonColors(containerColor = EmeraldAccent), shape = RoundedCornerShape(12.dp)) { Text("حجز", color = AppBackground, fontWeight = FontWeight.ExtraBold, fontSize = 12.sp) }
                IconButton(onClick = onCall, modifier = Modifier.size(42.dp).background(AppBackground, RoundedCornerShape(12.dp))) { Icon(Icons.Filled.Phone, contentDescription = null, tint = EmeraldAccent) }
                IconButton(onClick = onNav, modifier = Modifier.size(42.dp).background(AppBackground, RoundedCornerShape(12.dp))) { Icon(Icons.Filled.Directions, contentDescription = null, tint = EmeraldAccent) }
            }
        }
    }

    @Composable
    fun LinkedInJobCard(title: String, company: String, type: String, salary: String, logoUrl: String, onApply: () -> Unit) {
        GlassmorphicSurface(modifier = Modifier.fillMaxWidth().padding(8.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Surface(shape = RoundedCornerShape(12.dp), color = EmeraldAccent.copy(alpha = 0.1f), modifier = Modifier.size(48.dp)) {
                    Box(contentAlignment = Alignment.Center) { Icon(Icons.Filled.Work, contentDescription = null, tint = EmeraldAccent) }
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(title, color = TextLightMint, fontSize = 15.sp, fontWeight = FontWeight.Bold)
                    Text(company, color = TextMutedMint, fontSize = 12.sp)
                    Text(type, color = CyanMedical, fontSize = 11.sp)
                }
                Button(onClick = onApply, colors = ButtonDefaults.buttonColors(containerColor = WarmOrange), shape = RoundedCornerShape(8.dp)) {
                    Text("تقديم", color = AppBackground, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }

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
        GlassmorphicSurface(modifier = Modifier.fillMaxWidth().padding(8.dp).clickable { onClick() }) {
            Column {
                AsyncImage(model = imageUrl, contentDescription = title, modifier = Modifier.fillMaxWidth().height(150.dp).clip(RoundedCornerShape(12.dp)), contentScale = ContentScale.Crop)
                Spacer(modifier = Modifier.height(8.dp))
                Text(title, color = TextLightMint, fontSize = 16.sp, fontWeight = FontWeight.ExtraBold)
                Text(location, color = TextMutedMint, fontSize = 12.sp)
                Spacer(modifier = Modifier.height(8.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Text(price, color = WarmOrange, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        IconButton(onClick = onBookmark) { Icon(Icons.Filled.BookmarkBorder, contentDescription = null, tint = EmeraldAccent) }
                        IconButton(onClick = onShare) { Icon(Icons.Filled.Share, contentDescription = null, tint = EmeraldAccent) }
                    }
                }
            }
        }
    }
}
