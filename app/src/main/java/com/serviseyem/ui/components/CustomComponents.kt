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
    val actualSize = iconSizeDp.coerceIn(22, 34).toFloat()
    val sizeDp = actualSize.dp
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = Modifier
            .heightIn(min = 48.dp)
            .clickable(onClick = onClick)
            .padding(horizontal = 2.dp, vertical = 2.dp)
            .wrapContentHeight()
    ) {
        Box(contentAlignment = Alignment.Center) {
            if (isSelected) {
                Box(
                    modifier = Modifier
                        .size(sizeDp + 6.dp)
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
                color = if (isSelected) Color(0xFF0284C7) else Color(0xFF1E293B),
                border = BorderStroke(
                    width = if (isSelected) 1.5.dp else 1.dp,
                    color = if (isSelected) Color(0xFFFFC93C) else Color(0xFF334155)
                ),
                shadowElevation = if (isSelected) 4.dp else 2.dp,
                modifier = Modifier.size(sizeDp + 4.dp)
            ) {
                Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                    if (vectorIcon != null && (iconStyle == "MINIMAL" || forceWhiteText)) {
                        Icon(
                            imageVector = vectorIcon,
                            contentDescription = label,
                            tint = if (isSelected) Color(0xFFFFC93C) else Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                    } else {
                        Text(
                            text = emojiIcon,
                            fontSize = 17.sp,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }

            if (badgeCount > 0) {
                Surface(
                    shape = CircleShape,
                    color = Color(0xFFEF4444),
                    border = BorderStroke(1.dp, Color(0xFFFFFFFF)),
                    shadowElevation = 3.dp,
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .offset(x = 4.dp, y = (-2).dp)
                        .size(16.dp)
                ) {
                    Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                        Text(
                            text = if (badgeCount > 99) "99+" else badgeCount.toString(),
                            color = Color.White,
                            fontSize = 8.5.sp,
                            fontWeight = FontWeight.Black,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        }
        Spacer(modifier = Modifier.height(3.dp))
        Text(
            text = label,
            fontSize = 11.sp,
            lineHeight = 17.sp,
            maxLines = 2,
            overflow = TextOverflow.Clip,
            softWrap = true,
            color = if (isSelected) Color(0xFFFFC93C) else Color(0xFFC9D2E0),
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
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

