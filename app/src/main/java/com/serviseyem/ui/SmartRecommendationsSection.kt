package com.serviseyem

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.serviseyem.data.*
import com.serviseyem.ui.MainViewModel
import com.serviseyem.utils.VisualThemePalette

// --------------------------------------------------------
// 1. SMART RECOMMENDATIONS SECTION
// --------------------------------------------------------
@Composable
fun SmartRecommendationsSection(
    viewModel: MainViewModel,
    themeColors: VisualThemePalette,
    onStoreClick: (StoreEntity) -> Unit,
    onPropertyClick: (PropertyEntity) -> Unit
) {
    val stores by viewModel.stores.collectAsState()
    val properties by viewModel.properties.collectAsState()
    val settingsState by viewModel.settings.collectAsState()

    // Filter active items
    val activeStores = remember(stores) { stores.filter { it.isActive && !it.isDeleted } }
    val activeProps = remember(properties) { properties.filter { it.isActive && !it.isDeleted } }

    // Logic: Recommended, Verified, VIP or Pinned by Admin
    val recommendedStores = remember(activeStores, settingsState.isStoresEnabled) {
        if (!settingsState.isStoresEnabled) emptyList()
        else {
            val flagged = activeStores.filter { it.isRecommended || it.isPinned || it.isVerified || it.isVip }
            val list = if (flagged.isNotEmpty()) flagged else activeStores
            list.sortedWith(compareByDescending<StoreEntity> { it.rating }
                .thenByDescending { it.createdAt })
            .take(10)
        }
    }

    val recommendedProps = remember(activeProps, settingsState.isPropertiesEnabled) {
        if (!settingsState.isPropertiesEnabled) emptyList()
        else {
            val flagged = activeProps.filter { it.isRecommended || it.isPinned || it.isVerified || it.isVip }
            val list = if (flagged.isNotEmpty()) flagged else activeProps
            list.sortedWith(compareByDescending<PropertyEntity> { it.rating }
                .thenByDescending { it.createdAt })
            .take(10)
        }
    }

    if (recommendedStores.isNotEmpty() || recommendedProps.isNotEmpty()) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(themeColors.surface, RoundedCornerShape(16.dp))
                .padding(14.dp)
                .border(1.dp, themeColors.accent.copy(alpha = 0.15f), RoundedCornerShape(16.dp)),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("💡", fontSize = 20.sp)
                Spacer(modifier = Modifier.width(6.dp))
                Column {
                    Text(
                        text = "توصيات ذكية منتقاة لك",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = themeColors.accent
                    )
                    Text(
                        text = "محلات وعقارات متميزة بناءً على التقييمات وتفضيلات المستخدمين",
                        fontSize = 10.sp,
                        color = themeColors.textSecondary
                    )
                }
            }

            if (settingsState.recommendationsLayout == "GRID_HORIZONTAL") {
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(recommendedStores) { store ->
                        RecommendationItemCard(
                            title = store.name,
                            subtitle = store.description,
                            coverImage = store.coverImage.ifEmpty { store.logoImage },
                            avatarImage = store.logoImage,
                            rating = store.rating,
                            badge = "متجر مميز",
                            badgeColor = Color(0xFFF59E0B),
                            themeColors = themeColors,
                            onClick = { onStoreClick(store) }
                        )
                    }
                    items(recommendedProps) { prop ->
                        val label = if (prop.type == "rent") "للإيجار" else "للبيع"
                        RecommendationItemCard(
                            title = prop.title,
                            subtitle = "${prop.price} YER - $label",
                            coverImage = prop.images.firstOrNull() ?: "",
                            avatarImage = "",
                            rating = prop.rating,
                            badge = "عقار متميز",
                            badgeColor = Color(0xFF10B981),
                            themeColors = themeColors,
                            onClick = { onPropertyClick(prop) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun RecommendationItemCard(
    title: String,
    subtitle: String,
    coverImage: String,
    avatarImage: String,
    rating: Float,
    badge: String,
    badgeColor: Color = Color(0xFFF59E0B),
    themeColors: VisualThemePalette,
    onClick: () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = themeColors.surface),
        shape = RoundedCornerShape(14.dp),
        border = BorderStroke(0.8.dp, themeColors.accent.copy(alpha = 0.25f)),
        modifier = Modifier
            .width(170.dp)
            .height(200.dp)
            .clickable { onClick() }
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Dual-image layout: Cover Banner + Avatar Overlay
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(95.dp)
            ) {
                // 1. Cover image / Banner background
                if (coverImage.isNotBlank()) {
                    AsyncImage(
                        model = coverImage,
                        contentDescription = title,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(RoundedCornerShape(topStart = 14.dp, topEnd = 14.dp))
                    )
                } else {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                Brush.verticalGradient(
                                    listOf(themeColors.primary.copy(alpha = 0.6f), Color(0xFF1E293B))
                                )
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Star,
                            contentDescription = null,
                            tint = themeColors.accent.copy(alpha = 0.4f),
                            modifier = Modifier.size(32.dp)
                        )
                    }
                }

                // Dark gradient overlay on top for badge readability
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                listOf(Color.Black.copy(alpha = 0.4f), Color.Transparent, Color.Black.copy(alpha = 0.6f))
                            )
                        )
                )

                // Top Badge
                Box(
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(6.dp)
                        .clip(RoundedCornerShape(6.dp))
                        .background(badgeColor.copy(alpha = 0.9f))
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(badge, fontSize = 8.5.sp, fontWeight = FontWeight.Bold, color = Color.Black)
                }

                // Rating Badge Top-End
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(6.dp)
                        .clip(RoundedCornerShape(6.dp))
                        .background(Color.Black.copy(alpha = 0.7f))
                        .padding(horizontal = 5.dp, vertical = 2.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("⭐", fontSize = 8.sp)
                        Spacer(modifier = Modifier.width(2.dp))
                        Text(String.format("%.1f", rating), fontSize = 9.sp, color = themeColors.accent, fontWeight = FontWeight.Bold)
                    }
                }

                // Circular Logo/Avatar Overlay at Bottom-Start
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(start = 8.dp, bottom = 4.dp)
                        .size(34.dp)
                        .clip(CircleShape)
                        .background(themeColors.surface)
                        .border(1.5.dp, themeColors.accent, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    if (avatarImage.isNotBlank()) {
                        AsyncImage(
                            model = avatarImage,
                            contentDescription = title,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize().clip(CircleShape)
                        )
                    } else {
                        Text(
                            text = title.take(1).ifEmpty { "🏪" },
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = themeColors.accent
                        )
                    }
                }
            }

            // Card Body Content (Title, Subtitle & Action)
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 8.dp, vertical = 6.dp),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = title,
                        fontSize = 11.5.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = subtitle.ifEmpty { "خدمات متكاملة وعروض متميزة" },
                        fontSize = 9.5.sp,
                        color = themeColors.textSecondary,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        lineHeight = 13.sp
                    )
                }

                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = themeColors.accent.copy(alpha = 0.15f),
                    border = BorderStroke(0.7.dp, themeColors.accent.copy(alpha = 0.4f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "عرض التفاصيل 🔍",
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        color = themeColors.accent,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(vertical = 3.dp)
                    )
                }
            }
        }
    }
}
