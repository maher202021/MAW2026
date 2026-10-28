package com.serviseyem.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
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
import coil.compose.AsyncImage

/**
 * 💎 BespokePremiumSystem
 * Hyper-luxurious design system components for category-specific screens.
 */
object BespokePremiumSystem {

    val DeepDark = Color(0xFF0B1120)
    val SurfaceDark = Color(0xFF131C2E)
    val GlassBorder = Color.White.copy(alpha = 0.08f)
    val EmeraldAccent = Color(0xFF10B981)
    val WarmOrange = Color(0xFFF97316)
    val CyanMedical = Color(0xFF06B6D4)
    val RoyalPurple = Color(0xFF6C5CE7)
    val ElectricBlue = Color(0xFF2563EB)

    val BrandGradient = Brush.horizontalGradient(listOf(Color(0xFF6C5CE7), Color(0xFF4A3FCC)))
    val StoresGradient = Brush.horizontalGradient(listOf(Color(0xFF2563EB), Color(0xFF7C3AED)))
    val FoodGradient = Brush.horizontalGradient(listOf(Color(0xFFE11D48), Color(0xFFF97316)))
    val MedicalGradient = Brush.horizontalGradient(listOf(Color(0xFF0D9488), Color(0xFF06B6D4)))
    val EstateGradient = Brush.horizontalGradient(listOf(Color(0xFF1E293B), Color(0xFFB45309)))
    val JobsGradient = Brush.horizontalGradient(listOf(Color(0xFF7C3AED), Color(0xFFA855F7)))
    val ServicesGradient = Brush.horizontalGradient(listOf(Color(0xFF16A34A), Color(0xFFF59E0B)))
    val GoldBannerGradient = Brush.horizontalGradient(listOf(Color(0xFFD97706), Color(0xFFB45309), Color(0xFF78350F)))

    @Composable
    fun SectionBackHeader(
        title: String,
        onBackClick: () -> Unit
    ) {
        androidx.activity.compose.BackHandler { onBackClick() }
        Surface(
            onClick = onBackClick,
            shape = RoundedCornerShape(14.dp),
            color = SurfaceDark,
            border = BorderStroke(1.dp, GlassBorder),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 6.dp, vertical = 6.dp)
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.ArrowForward,
                        contentDescription = "الرجوع للرئيسية",
                        tint = EmeraldAccent,
                        modifier = Modifier.size(20.dp)
                    )
                    Text(
                        text = "العودة للرئيسية",
                        color = Color.White,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = Color.White.copy(alpha = 0.08f),
                    border = BorderStroke(0.5.dp, GlassBorder)
                ) {
                    Text(
                        text = title,
                        color = EmeraldAccent,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                    )
                }
            }
        }
    }

    @Composable
    fun TrustStatisticsBar() {
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = SurfaceDark,
            border = BorderStroke(1.dp, GlassBorder),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 6.dp, vertical = 4.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("1,500+", color = EmeraldAccent, fontSize = 13.sp, fontWeight = FontWeight.Black)
                    Text("مزود خدمة موثوق", color = Color.LightGray, fontSize = 9.sp)
                }
                Box(modifier = Modifier.height(24.dp).width(1.dp).background(Color.White.copy(alpha = 0.1f)))
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("12", color = Color(0xFF38BDF8), fontSize = 13.sp, fontWeight = FontWeight.Black)
                    Text("مدينة يمنية", color = Color.LightGray, fontSize = 9.sp)
                }
                Box(modifier = Modifier.height(24.dp).width(1.dp).background(Color.White.copy(alpha = 0.1f)))
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("45,000+", color = Color(0xFFFBBF24), fontSize = 13.sp, fontWeight = FontWeight.Black)
                    Text("حجز ناجح", color = Color.LightGray, fontSize = 9.sp)
                }
            }
        }
    }

    @Composable
    fun JoinAsProviderBanner(onJoinClick: () -> Unit) {
        val vibrantOrangeGradient = Brush.horizontalGradient(
            listOf(
                Color(0xFFEA580C),
                Color(0xFFF97316),
                Color(0xFFFB923C)
            )
        )
        Surface(
            onClick = onJoinClick,
            shape = RoundedCornerShape(18.dp),
            color = Color.Transparent,
            border = BorderStroke(1.dp, Color(0xFFFED7AA).copy(alpha = 0.5f)),
            shadowElevation = 6.dp,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 6.dp, vertical = 6.dp)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(vibrantOrangeGradient)
                    .padding(horizontal = 16.dp, vertical = 12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text("👑", fontSize = 15.sp)
                            Text("انضم كمزوّد خدمة معتمد", color = Color.White, fontSize = 14.5.sp, fontWeight = FontWeight.Black)
                        }
                        Spacer(modifier = Modifier.height(2.dp))
                        Text("سجّل نشاطك التجاري أو مهنتك واستقبل آلاف الطلبات الآن", color = Color.White.copy(alpha = 0.95f), fontSize = 10.5.sp, fontWeight = FontWeight.Medium)
                    }
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = Color.White,
                        shadowElevation = 4.dp,
                        modifier = Modifier.padding(start = 10.dp)
                    ) {
                        Text(
                            text = "سجّل الآن ✨",
                            color = Color(0xFFC2410C),
                            fontSize = 11.5.sp,
                            fontWeight = FontWeight.ExtraBold,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp)
                        )
                    }
                }
            }
        }
    }

    @Composable
    fun GlassmorphicSurface(
        modifier: Modifier = Modifier,
        shape: RoundedCornerShape = RoundedCornerShape(20.dp),
        content: @Composable ColumnScope.() -> Unit
    ) {
        Surface(
            modifier = modifier,
            shape = shape,
            color = SurfaceDark.copy(alpha = 0.75f),
            border = BorderStroke(1.dp, GlassBorder),
            tonalElevation = 2.dp
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                content()
            }
        }
    }

    @Composable
    fun SectionHeader(
        title: String,
        subtitle: String,
        icon: ImageVector,
        accentColor: Color,
        onActionClick: (() -> Unit)? = null,
        actionLabel: String = ""
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Surface(
                    shape = CircleShape,
                    color = accentColor.copy(alpha = 0.15f),
                    modifier = Modifier.size(40.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(icon, contentDescription = null, tint = accentColor, modifier = Modifier.size(20.dp))
                    }
                }
                Column {
                    Text(title, color = Color.White, fontSize = 17.sp, fontWeight = FontWeight.ExtraBold)
                    Text(subtitle, color = Color.Gray, fontSize = 11.sp)
                }
            }
            if (onActionClick != null) {
                TextButton(onClick = onActionClick) {
                    Text(actionLabel, color = accentColor, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }

    // 1. TechHub Store Card
    @Composable
    fun TechHubStoreCard(
        title: String,
        imageUrl: String,
        rating: Float,
        priceRange: String,
        onCartClick: () -> Unit,
        onClick: () -> Unit
    ) {
        Surface(
            onClick = onClick,
            shape = RoundedCornerShape(24.dp),
            color = Color(0xFF131E33),
            border = BorderStroke(1.dp, GlassBorder),
            modifier = Modifier.fillMaxWidth().padding(6.dp)
        ) {
            Box(modifier = Modifier.fillMaxWidth()) {
                Column {
                    AsyncImage(
                        model = imageUrl,
                        contentDescription = title,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(140.dp)
                            .clip(RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)),
                        contentScale = ContentScale.Crop
                    )
                    Column(modifier = Modifier.padding(14.dp)) {
                        Text(title, color = Color.White, fontSize = 15.sp, fontWeight = FontWeight.Bold, maxLines = 1)
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            Icon(Icons.Default.Star, contentDescription = null, tint = Color(0xFFF59E0B), modifier = Modifier.size(14.dp))
                            Text(rating.toString(), color = Color.White, fontSize = 12.sp)
                            Text("•", color = Color.Gray)
                            Text(priceRange, color = EmeraldAccent, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                        }
                    }
                }
                Surface(
                    onClick = onCartClick,
                    shape = CircleShape,
                    color = EmeraldAccent,
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(12.dp)
                        .size(36.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(Icons.Default.AddShoppingCart, contentDescription = null, tint = DeepDark, modifier = Modifier.size(18.dp))
                    }
                }
            }
        }
    }

    // 2. Foodie Restaurant Card
    @Composable
    fun FoodieRestaurantCard(
        title: String,
        imageUrl: String,
        deliveryTime: String,
        price: String,
        onClick: () -> Unit,
        onAddClick: () -> Unit
    ) {
        Card(
            onClick = onClick,
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
            modifier = Modifier.fillMaxWidth().padding(8.dp)
        ) {
            Row(modifier = Modifier.fillMaxWidth().padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                AsyncImage(
                    model = imageUrl,
                    contentDescription = title,
                    modifier = Modifier.size(90.dp).clip(RoundedCornerShape(16.dp)),
                    contentScale = ContentScale.Crop
                )
                Spacer(modifier = Modifier.width(16.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(title, color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.ExtraBold)
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        Icon(Icons.Default.Timer, contentDescription = null, tint = WarmOrange, modifier = Modifier.size(14.dp))
                        Text(deliveryTime, color = Color.LightGray, fontSize = 12.sp)
                    }
                    Text(price, color = EmeraldAccent, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                }
                Surface(
                    onClick = onAddClick,
                    shape = RoundedCornerShape(12.dp),
                    color = EmeraldAccent.copy(alpha = 0.2f),
                    border = BorderStroke(1.dp, EmeraldAccent),
                    modifier = Modifier.size(40.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(Icons.Default.Add, contentDescription = null, tint = EmeraldAccent)
                    }
                }
            }
        }
    }

    // 3. Medical Clinical Card
    @Composable
    fun MedicalClinicalCard(
        name: String,
        specialty: String,
        isOpen: Boolean,
        onBook: () -> Unit,
        onCall: () -> Unit,
        onNav: () -> Unit
    ) {
        GlassmorphicSurface(modifier = Modifier.fillMaxWidth().padding(8.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = CyanMedical.copy(alpha = 0.1f),
                    modifier = Modifier.size(50.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(Icons.Default.LocalHospital, contentDescription = null, tint = CyanMedical)
                    }
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(name, color = Color.White, fontSize = 15.sp, fontWeight = FontWeight.Bold)
                    Text(specialty, color = Color.LightGray, fontSize = 12.sp)
                }
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = if (isOpen) Color(0xFF065F46) else Color(0xFF991B1B),
                    modifier = Modifier.padding(4.dp)
                ) {
                    Text(
                        if (isOpen) "مفتوح الآن" else "مغلق",
                        color = Color.White,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(
                    onClick = onBook,
                    modifier = Modifier.weight(1.5f).height(42.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = CyanMedical),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("حجز موعد", color = DeepDark, fontWeight = FontWeight.ExtraBold, fontSize = 12.sp)
                }
                IconButton(
                    onClick = onCall,
                    modifier = Modifier.size(42.dp).background(Color(0xFF334155), RoundedCornerShape(12.dp))
                ) {
                    Icon(Icons.Default.Phone, contentDescription = null, tint = Color.White)
                }
                IconButton(
                    onClick = onNav,
                    modifier = Modifier.size(42.dp).background(Color(0xFF334155), RoundedCornerShape(12.dp))
                ) {
                    Icon(Icons.Default.Directions, contentDescription = null, tint = Color.White)
                }
            }
        }
    }

    // 4. Estate Luxury Card
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
            shape = RoundedCornerShape(24.dp),
            color = Color(0xFF131E33),
            border = BorderStroke(1.dp, GlassBorder),
            modifier = Modifier.fillMaxWidth().padding(8.dp)
        ) {
            Column {
                Box(modifier = Modifier.fillMaxWidth().height(180.dp)) {
                    AsyncImage(
                        model = imageUrl,
                        contentDescription = title,
                        modifier = Modifier.fillMaxSize().clip(RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)),
                        contentScale = ContentScale.Crop
                    )
                    Row(
                        modifier = Modifier.align(Alignment.TopEnd).padding(12.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Surface(
                            onClick = onBookmark,
                            shape = CircleShape,
                            color = DeepDark.copy(alpha = 0.4f),
                            modifier = Modifier.size(36.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(Icons.Default.BookmarkBorder, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
                            }
                        }
                        Surface(
                            onClick = onShare,
                            shape = CircleShape,
                            color = DeepDark.copy(alpha = 0.4f),
                            modifier = Modifier.size(36.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(Icons.Default.Share, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
                            }
                        }
                    }
                }
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(price, color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.ExtraBold)
                    Text(title, color = Color.LightGray, fontSize = 14.sp, fontWeight = FontWeight.Medium, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            Icon(Icons.Default.Bed, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(16.dp))
                            Text("$rooms غرف", color = Color.Gray, fontSize = 11.sp)
                        }
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            Icon(Icons.Default.SquareFoot, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(16.dp))
                            Text(area, color = Color.Gray, fontSize = 11.sp)
                        }
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            Icon(Icons.Default.LocationOn, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(16.dp))
                            Text(location, color = Color.Gray, fontSize = 11.sp, maxLines = 1)
                        }
                    }
                }
            }
        }
    }

    // 5. LinkedIn Job Card
    @Composable
    fun LinkedInJobCard(
        title: String,
        company: String,
        type: String,
        salary: String,
        logoUrl: String,
        onApply: () -> Unit
    ) {
        GlassmorphicSurface(modifier = Modifier.fillMaxWidth().padding(8.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.Top) {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = Color.White.copy(alpha = 0.05f),
                    modifier = Modifier.size(54.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        if (logoUrl.isNotEmpty()) {
                            AsyncImage(model = logoUrl, contentDescription = company, modifier = Modifier.fillMaxSize().padding(8.dp))
                        } else {
                            Icon(Icons.Default.Work, contentDescription = null, tint = Color.Gray)
                        }
                    }
                }
                Spacer(modifier = Modifier.width(14.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(title, color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    Text(company, color = EmeraldAccent, fontSize = 13.sp, fontWeight = FontWeight.Medium)
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Surface(shape = RoundedCornerShape(6.dp), color = Color.White.copy(alpha = 0.1f)) {
                            Text(type, color = Color.LightGray, fontSize = 10.sp, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
                        }
                        Surface(shape = RoundedCornerShape(6.dp), color = EmeraldAccent.copy(alpha = 0.1f)) {
                            Text(salary, color = EmeraldAccent, fontSize = 10.sp, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
            Button(
                onClick = onApply,
                modifier = Modifier.fillMaxWidth().height(44.dp),
                colors = ButtonDefaults.buttonColors(containerColor = EmeraldAccent),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("التقديم الآن", color = DeepDark, fontWeight = FontWeight.ExtraBold)
            }
        }
    }
}
