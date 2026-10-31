package com.serviseyem.ui.components

import com.serviseyem.utils.*
import com.serviseyem.ui.*

import android.content.Context
import android.graphics.BitmapFactory
import android.util.Base64
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.painter.BitmapPainter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.serviseyem.data.BannerEntity
import com.serviseyem.utils.VisualThemePalette
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode

@Composable
fun CustomFilterChip(
    label: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .background(if (selected) Color(0xFFFFD700) else Color(0xFF0F172A))
            .clickable { onClick() }
            .padding(horizontal = 10.dp, vertical = 6.dp)
            .border(1.dp, if (selected) Color.White else Color(0xFF334155), RoundedCornerShape(12.dp))
    ) {
        Text(
            text = label,
            color = if (selected) Color.Black else Color.White,
            fontSize = 9.sp,
            fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal
        )
    }
}

@Composable
fun Luxury3DNavIcon(
    emojiIcon: String,
    vectorIcon: ImageVector?,
    label: String,
    isSelected: Boolean,
    badgeCount: Int = 0,
    iconSizeDp: Int = 18,
    iconStyle: String = "GOLDEN_3D",
    forceWhiteText: Boolean = false,
    onClick: () -> Unit
) {
    // 0.7f: معامل تصغير نسبي (30% scale reduction) للحفاظ على مساحات أشرطة التنقل
    // 16f (حد أدنى): يمنع انكماش الأيقونة عن الحجم المرئي الأدنى
    // 24f (حد أقصى): يمنع تضخم الأيقونة وخروجها عن شريط التنقل السفلي والعلوي
    val actualSize = (iconSizeDp * 0.7f).coerceIn(16f, 24f)
    val sizeDp = actualSize.dp
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clickable(onClick = onClick)
            .padding(horizontal = 2.dp, vertical = 1.dp)
    ) {
        Box(contentAlignment = Alignment.Center) {
            if (isSelected) {
                Box(
                    modifier = Modifier
                        .size(sizeDp + 8.dp)
                        .background(
                            brush = Brush.radialGradient(
                                colors = listOf(
                                    Color(0xFFF59E0B).copy(alpha = 0.45f),
                                    Color.Transparent
                                )
                            ),
                            shape = CircleShape
                        )
                )
            }

            Surface(
                shape = CircleShape,
                color = if (isSelected) Color(0xFF1E293B) else Color(0xFF0F172A),
                border = BorderStroke(
                    width = if (isSelected) 1.4.dp else 0.8.dp,
                    brush = Brush.linearGradient(
                        colors = if (isSelected) listOf(
                            Color(0xFFFFFAED),
                            Color(0xFFF59E0B),
                            Color(0xFFD97706),
                            Color(0xFFFEF3C7)
                        ) else listOf(
                            Color(0xFFCBD5E1).copy(alpha = 0.6f),
                            Color(0xFF475569).copy(alpha = 0.3f)
                        )
                    )
                ),
                shadowElevation = if (isSelected) 5.dp else 1.dp,
                modifier = Modifier.size(sizeDp + 6.dp)
            ) {
                Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                    if (vectorIcon != null && (iconStyle == "MINIMAL" || forceWhiteText)) {
                        Icon(
                            imageVector = vectorIcon,
                            contentDescription = label,
                            tint = if (forceWhiteText) Color.White else if (isSelected) Color(0xFFF59E0B) else Color.White,
                            modifier = Modifier.size(sizeDp)
                        )
                    } else {
                        Text(
                            text = emojiIcon,
                            fontSize = (sizeDp.value * 0.68f).sp,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }

            if (badgeCount > 0) {
                Surface(
                    shape = CircleShape,
                    color = Color(0xFF10B981), // Emerald Green
                    border = BorderStroke(1.dp, Color(0xFFFFD700)), // Gold border
                    shadowElevation = 3.dp,
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .offset(x = 4.dp, y = (-2).dp)
                        .size(15.dp)
                ) {
                    Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                        Text(
                            text = if (badgeCount > 99) "99+" else badgeCount.toString(),
                            color = Color(0xFFFFD700), // Gold text
                            fontSize = 8.sp,
                            fontWeight = FontWeight.ExtraBold,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        }
        Spacer(modifier = Modifier.height(1.dp))
        Text(
            text = label,
            fontSize = 7.5.sp,
            maxLines = 1,
            softWrap = false,
            color = if (forceWhiteText) Color.White else if (isSelected) Color(0xFFFFD700) else Color.LightGray,
            fontWeight = if (isSelected || forceWhiteText) FontWeight.Bold else FontWeight.Normal
        )
    }
}

@Composable
fun BannerSliderView(banners: List<BannerEntity>, themeColors: VisualThemePalette, onBannerClick: (String) -> Unit) {
    var currentIndex by remember { mutableStateOf(0) }
    
    val activeBanner = if (banners.isNotEmpty()) banners.getOrNull(currentIndex) else null
    
    LaunchedEffect(currentIndex, banners) {
        if (banners.isNotEmpty()) {
            val active = banners.getOrNull(currentIndex)
            val durationSec = if (active != null && active.duration > 0) active.duration else 5
            kotlinx.coroutines.delay(durationSec * 1000L)
            currentIndex = (currentIndex + 1) % banners.size
        }
    }

    if (activeBanner != null) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .height(130.dp)
                .clickable { onBannerClick(activeBanner.redirectCategory) },
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = themeColors.secondary)
        ) {
            Box(modifier = Modifier.fillMaxSize()) {
                when (activeBanner.type.uppercase()) {
                    "IMAGE" -> {
                        if (activeBanner.url.isNotEmpty()) {
                            if (activeBanner.url.startsWith("data:image") || activeBanner.url.length > 200) {
                                val bitmap = remember(activeBanner.url) {
                                    try {
                                        val base64Data = if (activeBanner.url.contains(",")) activeBanner.url.substringAfter(",") else activeBanner.url
                                        val decodedBytes = Base64.decode(base64Data, Base64.DEFAULT)
                                        BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.size)
                                    } catch (e: Exception) {
                                        null
                                    }
                                }
                                if (bitmap != null) {
                                    Image(
                                        painter = BitmapPainter(bitmap.asImageBitmap()),
                                        contentDescription = activeBanner.title,
                                        modifier = Modifier.fillMaxSize(),
                                        contentScale = androidx.compose.ui.layout.ContentScale.Crop
                                    )
                                } else {
                                    AsyncImage(
                                        model = activeBanner.url,
                                        contentDescription = activeBanner.title,
                                        modifier = Modifier.fillMaxSize(),
                                        contentScale = androidx.compose.ui.layout.ContentScale.Crop
                                    )
                                }
                            } else {
                                AsyncImage(
                                    model = activeBanner.url,
                                    contentDescription = activeBanner.title,
                                    modifier = Modifier.fillMaxSize(),
                                    contentScale = androidx.compose.ui.layout.ContentScale.Crop
                                )
                            }
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(
                                        Brush.verticalGradient(
                                            colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.8f))
                                        )
                                    )
                            )
                            Column(
                                modifier = Modifier
                                    .align(Alignment.BottomStart)
                                    .padding(12.dp)
                            ) {
                                Text(
                                    text = activeBanner.title,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                                if (activeBanner.redirectCategory.isNotEmpty()) {
                                    Text(
                                        text = "اضغط للانتقال إلى قسم: ${activeBanner.redirectCategory}",
                                        fontSize = 9.sp,
                                        color = themeColors.accent
                                    )
                                }
                            }
                        } else {
                            BannerTextFallback(activeBanner = activeBanner, themeColors = themeColors)
                        }
                    }
                    "VIDEO" -> {
                        if (activeBanner.url.isNotEmpty()) {
                            val context = LocalContext.current
                            androidx.compose.ui.viewinterop.AndroidView(
                                factory = { ctx ->
                                    android.widget.VideoView(ctx).apply {
                                        setVideoURI(android.net.Uri.parse(activeBanner.url))
                                        setOnPreparedListener { mp ->
                                            mp.isLooping = true
                                            mp.setVolume(0f, 0f)
                                            start()
                                        }
                                        setOnErrorListener { _, _, _ -> true }
                                    }
                                },
                                modifier = Modifier.fillMaxSize()
                            )
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(
                                        Brush.verticalGradient(
                                            colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.7f))
                                        )
                                    )
                            )
                            Column(
                                modifier = Modifier
                                    .align(Alignment.BottomStart)
                                    .padding(12.dp)
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text("🎬 فيديو مميز", fontSize = 9.sp, color = themeColors.accent, fontWeight = FontWeight.Bold)
                                }
                                Text(
                                    text = activeBanner.title,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            }
                        } else {
                            BannerTextFallback(activeBanner = activeBanner, themeColors = themeColors)
                        }
                    }
                    else -> {
                        BannerTextFallback(activeBanner = activeBanner, themeColors = themeColors)
                    }
                }
            }
        }
    }
}

@Composable
fun BannerTextFallback(activeBanner: BannerEntity, themeColors: VisualThemePalette) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.horizontalGradient(
                    listOf(
                        themeColors.primary,
                        themeColors.secondary.copy(alpha = 0.8f)
                    )
                )
            )
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(imageVector = Icons.Default.Info, contentDescription = "إعلان ممتاز", tint = themeColors.accent, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "إعلان رسمي دليل خدمات اليمن",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = themeColors.accent
                )
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = activeBanner.title,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                textAlign = TextAlign.Center,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
            if (activeBanner.redirectCategory.isNotEmpty()) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "🔗 الانتقال لقسم: ${activeBanner.redirectCategory}",
                    fontSize = 9.sp,
                    color = themeColors.accent.copy(alpha = 0.9f),
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
fun CategorySectionIconView(iconStr: String, modifier: Modifier = Modifier, size: androidx.compose.ui.unit.Dp = 20.dp) {
    if (iconStr.length > 15) {
        if (iconStr.startsWith("http://") || iconStr.startsWith("https://")) {
            AsyncImage(
                model = iconStr,
                contentDescription = null,
                modifier = modifier.size(size)
            )
        } else {
            val bitmap = remember(iconStr) {
                try {
                    val base64Data = if (iconStr.contains(",")) iconStr.substringAfter(",") else iconStr
                    val decodedBytes = Base64.decode(base64Data, Base64.DEFAULT)
                    BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.size)
                } catch (e: Exception) {
                    null
                }
            }
            if (bitmap != null) {
                Image(
                    painter = BitmapPainter(bitmap.asImageBitmap()),
                    contentDescription = null,
                    modifier = modifier.size(size)
                )
            } else {
                Text(text = "📁", fontSize = 14.sp)
            }
        }
    } else {
        Text(text = iconStr.ifEmpty { "📁" }, fontSize = 14.sp, modifier = modifier)
    }
}

@Composable
fun CategoryChip(name: String, icon: String, isSelected: Boolean, themeColors: VisualThemePalette, onClick: () -> Unit) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clickable(
                interactionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() },
                indication = null
            ) { onClick() }
            .padding(horizontal = 4.dp, vertical = 4.dp)
            .width(76.dp)
    ) {
        val luxuryGoldBrush = Brush.linearGradient(
            colors = listOf(
                Color(0xFFFFE259),
                Color(0xFFFFA751),
                Color(0xFFFFE259)
            )
        )
        
        Box(
            modifier = Modifier
                .size(56.dp)
                .clip(CircleShape)
                .background(if (isSelected) themeColors.accent.copy(alpha = 0.25f) else themeColors.surface)
                .border(
                    width = if (isSelected) 2.5.dp else 1.dp,
                    brush = if (isSelected) luxuryGoldBrush else Brush.linearGradient(listOf(themeColors.accent.copy(alpha = 0.3f), themeColors.accent.copy(alpha = 0.1f))),
                    shape = CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            CategorySectionIconView(iconStr = icon, size = 26.dp)
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = name,
            fontSize = 11.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
            color = if (isSelected) themeColors.accent else Color.White,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
fun DetailedProviderPlaceholderCard(themeColors: VisualThemePalette) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp)
            .testTag("provider_detail_placeholder_card"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = themeColors.surface),
        border = BorderStroke(2.dp, themeColors.accent)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .background(themeColors.accent, RoundedCornerShape(8.dp))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = "👑 نموذجي معتمد",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.Black
                        )
                    }
                    Text(
                        text = "صيانة منزلية",
                        fontSize = 10.sp,
                        color = themeColors.accent,
                        fontWeight = FontWeight.Bold
                    )
                }
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Star,
                        contentDescription = "Rating",
                        tint = themeColors.accent,
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = "4.9 (نموذج)",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }
        }
    }
}

@Composable
fun OptionCheckboxCard(
    title: String,
    subtitle: String? = null,
    isChecked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    themeColors: VisualThemePalette
) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = if (isChecked) themeColors.accent.copy(alpha = 0.15f) else themeColors.surface
        ),
        border = BorderStroke(
            1.dp,
            if (isChecked) themeColors.accent else Color.Gray.copy(alpha = 0.25f)
        ),
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onCheckedChange(!isChecked) }
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(title, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.White)
                if (!subtitle.isNullOrEmpty()) {
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(subtitle, fontSize = 10.sp, color = Color.LightGray)
                }
            }
            Spacer(modifier = Modifier.width(8.dp))
            Box(
                modifier = Modifier
                    .size(28.dp)
                    .background(
                        if (isChecked) themeColors.accent else Color.Transparent,
                        RoundedCornerShape(6.dp)
                    )
                    .border(
                        1.5.dp,
                        if (isChecked) themeColors.accent else Color.Gray,
                        RoundedCornerShape(6.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                if (isChecked) {
                    Text("✓", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                }
            }
        }
    }
}

@Composable
fun SkeletonLoader(
    modifier: Modifier = Modifier,
    width: androidx.compose.ui.unit.Dp = 100.dp,
    height: androidx.compose.ui.unit.Dp = 100.dp,
    shape: androidx.compose.ui.graphics.Shape = RoundedCornerShape(8.dp)
) {
    val shimmerColors = listOf(
        Color(0xFF334155).copy(alpha = 0.6f),
        Color(0xFF475569).copy(alpha = 0.3f),
        Color(0xFF334155).copy(alpha = 0.6f)
    )

    val transition = rememberInfiniteTransition(label = "shimmer")
    val translateAnim = transition.animateFloat(
        initialValue = 0f,
        targetValue = 1000f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = 1200,
                easing = LinearEasing
            ),
            repeatMode = RepeatMode.Restart
        ),
        label = "shimmer_translate"
    )

    val brush = Brush.linearGradient(
        colors = shimmerColors,
        start = androidx.compose.ui.geometry.Offset.Zero,
        end = androidx.compose.ui.geometry.Offset(x = translateAnim.value, y = translateAnim.value)
    )

    Box(
        modifier = modifier
            .size(width = width, height = height)
            .background(brush = brush, shape = shape)
    )
}

@Composable
fun ProviderListSkeleton(modifier: Modifier = Modifier) {
    Column(modifier = modifier.padding(16.dp)) {
        repeat(3) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    SkeletonLoader(width = 60.dp, height = 60.dp, shape = CircleShape)
                    Spacer(modifier = Modifier.width(16.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        SkeletonLoader(width = 120.dp, height = 16.dp)
                        Spacer(modifier = Modifier.height(8.dp))
                        SkeletonLoader(width = 180.dp, height = 12.dp)
                    }
                }
            }
        }
    }
}

@Composable
fun GroupChatListSkeleton(modifier: Modifier = Modifier) {
    Column(modifier = modifier.padding(16.dp)) {
        repeat(4) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                SkeletonLoader(width = 50.dp, height = 50.dp, shape = CircleShape)
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    SkeletonLoader(width = 100.dp, height = 14.dp)
                    Spacer(modifier = Modifier.height(6.dp))
                    SkeletonLoader(width = 200.dp, height = 12.dp)
                }
            }
        }
    }
}


