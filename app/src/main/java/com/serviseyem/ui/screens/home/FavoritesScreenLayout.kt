package com.serviseyem.ui.screens.home

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.serviseyem.data.*
import com.serviseyem.ui.*
import com.serviseyem.ui.components.SmartAsyncImage
import com.serviseyem.utils.VisualThemePalette

/**
 * ❤️ شاشة المفضلة والعروض الحصرية الشاملة
 * مطابقة بدقة لتصميم الصورة مع تصفح الفئات وحالات الفراغ التفاعلية
 */
@Composable
fun FavoritesScreenLayout(
    viewModel: MainViewModel,
    themeColors: VisualThemePalette,
    onBackClick: () -> Unit,
    onOpenProviderDetails: (ProviderEntity) -> Unit = {},
    onOpenStoreDetails: (StoreEntity) -> Unit = {},
    onOpenPropertyDetails: (PropertyEntity) -> Unit = {},
    onOpenChat: (channelId: String) -> Unit = {}
) {
    val context = LocalContext.current
    val providers by viewModel.providers.collectAsState()
    val stores by viewModel.stores.collectAsState()
    val properties by viewModel.properties.collectAsState()
    val favoriteIds by viewModel.favoriteIds.collectAsState()
    val isProvidersLoading by viewModel.isProvidersLoading.collectAsState()

    var selectedCategoryIndex by remember { mutableStateOf(0) }

    val favoriteProviders = remember(providers, favoriteIds) {
        providers.filter { favoriteIds.contains(it.id) }
    }
    val favoriteStores = remember(stores, favoriteIds) {
        stores.filter { favoriteIds.contains(it.id) }
    }
    val favoriteProperties = remember(properties, favoriteIds) {
        properties.filter { favoriteIds.contains(it.id) }
    }
    val favoriteOffers = remember(favoriteStores) {
        favoriteStores.filter { it.isVip || it.isRecommended }
    }

    val totalCount = favoriteProviders.size + favoriteStores.size + favoriteProperties.size

    val categories = listOf(
        "الكل (${totalCount})" to 0,
        "المتاجر والمطاعم (${favoriteStores.size}) 🛍️" to 1,
        "الفنيون والخدمات (${favoriteProviders.size}) 🔧" to 2,
        "العروض والتخفيضات (${favoriteOffers.size}) 🔥" to 3,
        "العقارات (${favoriteProperties.size}) 🏢" to 4
    )

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(themeColors.background),
        contentPadding = PaddingValues(12.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // 1. Header Banner Card (شريط المفضلة العلوي الفاخر)
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = themeColors.surface),
                border = BorderStroke(1.dp, Color(0xFFEF4444).copy(alpha = 0.4f)),
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(4.dp, RoundedCornerShape(16.dp))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        modifier = Modifier.weight(1f),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(46.dp)
                                .shadow(6.dp, CircleShape)
                                .clip(CircleShape)
                                .background(
                                    Brush.linearGradient(
                                        listOf(Color(0xFFEF4444), Color(0xFFB91C1C))
                                    )
                                )
                                .border(1.5.dp, Color.White.copy(alpha = 0.6f), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Favorite,
                                contentDescription = "المفضلة",
                                tint = Color.White,
                                modifier = Modifier.size(24.dp)
                            )
                        }

                        Column {
                            Text(
                                text = "قائمة المفضلة والعروض الحصرية ❤️",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "إدارة المفضلة وتلقي إشعارات التخفيضات داخل التطبيق فورياً",
                                fontSize = 11.sp,
                                color = Color.LightGray
                            )
                        }
                    }

                    IconButton(
                        onClick = onBackClick,
                        modifier = Modifier
                            .size(34.dp)
                            .background(Color.White.copy(alpha = 0.08f), CircleShape)
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "رجوع",
                            tint = Color.White,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        }

        // 2. Horizontal Scrollable Filter Chips (أزرار تصفية الأقسام)
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                categories.forEach { (title, idx) ->
                    val isSelected = selectedCategoryIndex == idx
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(20.dp))
                            .background(
                                if (isSelected) themeColors.accent
                                else themeColors.surface
                            )
                            .clickable { selectedCategoryIndex = idx }
                            .border(
                                1.dp,
                                if (isSelected) Color.White.copy(alpha = 0.6f) else Color.Gray.copy(alpha = 0.3f),
                                RoundedCornerShape(20.dp)
                            )
                            .padding(horizontal = 14.dp, vertical = 7.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = title,
                            fontSize = 11.5.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                            color = if (isSelected) Color.Black else Color.White
                        )
                    }
                }
            }
        }

        // 3. Content Area
        val showAll = selectedCategoryIndex == 0
        val showStores = selectedCategoryIndex == 1
        val showProviders = selectedCategoryIndex == 2
        val showOffers = selectedCategoryIndex == 3
        val showProperties = selectedCategoryIndex == 4

        val hasAnyItems = when (selectedCategoryIndex) {
            0 -> totalCount > 0
            1 -> favoriteStores.isNotEmpty()
            2 -> favoriteProviders.isNotEmpty()
            3 -> favoriteOffers.isNotEmpty()
            4 -> favoriteProperties.isNotEmpty()
            else -> false
        }

        if (isProvidersLoading && totalCount == 0) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(40.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        CircularProgressIndicator(color = themeColors.accent, modifier = Modifier.size(36.dp))
                        Text("جاري مزامنة قائمتك المفضلة...", color = Color.Gray, fontSize = 12.sp)
                    }
                }
            }
        } else if (!hasAnyItems) {
            // Empty State Card (مطابقة لتصميم الصورة تماماً)
            item {
                Card(
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = themeColors.surface),
                    border = BorderStroke(1.dp, Color.Gray.copy(alpha = 0.25f)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 12.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(72.dp)
                                .shadow(8.dp, CircleShape)
                                .clip(CircleShape)
                                .background(Color(0xFFEF4444).copy(alpha = 0.15f))
                                .border(1.5.dp, Color(0xFFEF4444).copy(alpha = 0.4f), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("❤️", fontSize = 34.sp)
                        }

                        Text(
                            text = "قائمة المفضلة فارغة حالياً 💔",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )

                        Text(
                            text = "تصفح المتاجر والمطاعم والخدمات واضغط على زر القلب ❤️ في أي صفحة للوصول إليها بسرعة من هنا، وسنرسل لك إشعارات داخل التطبيق فور إضافة عروض جديدة!",
                            fontSize = 12.sp,
                            color = Color.LightGray,
                            textAlign = TextAlign.Center,
                            lineHeight = 18.sp,
                            modifier = Modifier.padding(horizontal = 8.dp)
                        )

                        Button(
                            onClick = onBackClick,
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = themeColors.accent),
                            contentPadding = PaddingValues(horizontal = 24.dp, vertical = 11.dp),
                            modifier = Modifier.padding(top = 6.dp)
                        ) {
                            Text(
                                text = "تصفح المتاجر والخدمات الآن 🚀",
                                fontSize = 12.5.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.Black
                            )
                        }
                    }
                }
            }
        } else {
            // List of Favorited Items
            if (showAll || showProviders) {
                if (favoriteProviders.isNotEmpty()) {
                    item {
                        Text(
                            text = "🔧 الفنيون ومقدمو الخدمات (${favoriteProviders.size})",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = themeColors.accent
                        )
                    }
                    items(favoriteProviders, key = { "fav_prov_${it.id}" }) { provider ->
                        FavoriteProviderCard(
                            provider = provider,
                            themeColors = themeColors,
                            onClick = { onOpenProviderDetails(provider) },
                            onRemoveFavorite = { viewModel.toggleFavorite(provider.id) },
                            onCall = {
                                if (provider.phone.isNotBlank()) {
                                    try {
                                        val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:${provider.phone}"))
                                        context.startActivity(intent)
                                    } catch (e: Exception) {
                                        android.widget.Toast.makeText(context, "تعذر الاتصال", android.widget.Toast.LENGTH_SHORT).show()
                                    }
                                }
                            }
                        )
                    }
                }
            }

            if (showAll || showStores || showOffers) {
                val targetStores = if (showOffers) favoriteOffers else favoriteStores
                if (targetStores.isNotEmpty()) {
                    item {
                        Text(
                            text = "🛍️ المتاجر والمطاعم (${targetStores.size})",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF34D399)
                        )
                    }
                    items(targetStores, key = { "fav_store_${it.id}" }) { store ->
                        FavoriteStoreCard(
                            store = store,
                            themeColors = themeColors,
                            onClick = { onOpenStoreDetails(store) },
                            onRemoveFavorite = { viewModel.toggleFavorite(store.id) },
                            onCall = {
                                if (store.phone.isNotEmpty()) {
                                    try {
                                        val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:${store.phone}"))
                                        context.startActivity(intent)
                                    } catch (e: Exception) {
                                        android.widget.Toast.makeText(context, "تعذر الاتصال", android.widget.Toast.LENGTH_SHORT).show()
                                    }
                                }
                            }
                        )
                    }
                }
            }

            if (showAll || showProperties) {
                if (favoriteProperties.isNotEmpty()) {
                    item {
                        Text(
                            text = "🏢 العقارات والأراضي (${favoriteProperties.size})",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFA78BFA)
                        )
                    }
                    items(favoriteProperties, key = { "fav_prop_${it.id}" }) { property ->
                        FavoritePropertyCard(
                            property = property,
                            themeColors = themeColors,
                            onClick = { onOpenPropertyDetails(property) },
                            onRemoveFavorite = { viewModel.toggleFavorite(property.id) },
                            onCall = {
                                if (property.phone.isNotEmpty()) {
                                    try {
                                        val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:${property.phone}"))
                                        context.startActivity(intent)
                                    } catch (e: Exception) {
                                        android.widget.Toast.makeText(context, "تعذر الاتصال", android.widget.Toast.LENGTH_SHORT).show()
                                    }
                                }
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun FavoriteProviderCard(
    provider: ProviderEntity,
    themeColors: VisualThemePalette,
    onClick: () -> Unit,
    onRemoveFavorite: () -> Unit,
    onCall: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = themeColors.surface),
        border = BorderStroke(1.dp, themeColors.accent.copy(alpha = 0.25f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            SmartAsyncImage(
                model = provider.profileImage.ifEmpty { provider.coverImage },
                contentDescription = provider.name,
                modifier = Modifier
                    .size(52.dp)
                    .clip(CircleShape)
            )

            Column(modifier = Modifier.weight(1f)) {
                Text(provider.name, fontWeight = FontWeight.Bold, color = Color.White, fontSize = 13.5.sp)
                Text(provider.profession.ifEmpty { provider.area }, color = Color.LightGray, fontSize = 11.sp)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Star, contentDescription = null, tint = Color(0xFFFFB300), modifier = Modifier.size(13.dp))
                    Spacer(modifier = Modifier.width(3.dp))
                    Text(String.format(java.util.Locale.US, "%.1f", provider.rating), color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("📍 ${provider.localNeighborhood.ifEmpty { provider.area }}", color = Color.Gray, fontSize = 10.sp)
                }
            }

            IconButton(
                onClick = onCall,
                modifier = Modifier
                    .size(36.dp)
                    .background(Color(0xFF10B981).copy(alpha = 0.15f), CircleShape)
            ) {
                Icon(Icons.Default.Phone, contentDescription = "اتصال", tint = Color(0xFF10B981), modifier = Modifier.size(18.dp))
            }

            IconButton(
                onClick = onRemoveFavorite,
                modifier = Modifier
                    .size(36.dp)
                    .background(Color(0xFFEF4444).copy(alpha = 0.15f), CircleShape)
            ) {
                Icon(Icons.Default.Favorite, contentDescription = "إزالة من المفضلة", tint = Color(0xFFEF4444), modifier = Modifier.size(18.dp))
            }
        }
    }
}

@Composable
fun FavoriteStoreCard(
    store: StoreEntity,
    themeColors: VisualThemePalette,
    onClick: () -> Unit,
    onRemoveFavorite: () -> Unit,
    onCall: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = themeColors.surface),
        border = BorderStroke(1.dp, Color(0xFF10B981).copy(alpha = 0.25f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            SmartAsyncImage(
                model = store.logoImage.ifEmpty { store.coverImage },
                contentDescription = store.name,
                modifier = Modifier
                    .size(52.dp)
                    .clip(RoundedCornerShape(10.dp))
            )

            Column(modifier = Modifier.weight(1f)) {
                Text(store.name, fontWeight = FontWeight.Bold, color = Color.White, fontSize = 13.5.sp)
                Text(store.description, color = Color.LightGray, fontSize = 10.5.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Star, contentDescription = null, tint = Color(0xFFFFB300), modifier = Modifier.size(13.dp))
                    Spacer(modifier = Modifier.width(3.dp))
                    Text(String.format(java.util.Locale.US, "%.1f", store.rating), color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("📍 ${store.localNeighborhood.ifEmpty { store.cityId }}", color = Color.Gray, fontSize = 10.sp)
                }
            }

            if (store.phone.isNotEmpty()) {
                IconButton(
                    onClick = onCall,
                    modifier = Modifier
                        .size(36.dp)
                        .background(Color(0xFF10B981).copy(alpha = 0.15f), CircleShape)
                ) {
                    Icon(Icons.Default.Phone, contentDescription = "اتصال", tint = Color(0xFF10B981), modifier = Modifier.size(18.dp))
                }
            }

            IconButton(
                onClick = onRemoveFavorite,
                modifier = Modifier
                    .size(36.dp)
                    .background(Color(0xFFEF4444).copy(alpha = 0.15f), CircleShape)
            ) {
                Icon(Icons.Default.Favorite, contentDescription = "إزالة من المفضلة", tint = Color(0xFFEF4444), modifier = Modifier.size(18.dp))
            }
        }
    }
}

@Composable
fun FavoritePropertyCard(
    property: PropertyEntity,
    themeColors: VisualThemePalette,
    onClick: () -> Unit,
    onRemoveFavorite: () -> Unit,
    onCall: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = themeColors.surface),
        border = BorderStroke(1.dp, Color(0xFF8B5CF6).copy(alpha = 0.25f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            SmartAsyncImage(
                model = property.images.firstOrNull() ?: "",
                contentDescription = property.title,
                modifier = Modifier
                    .size(52.dp)
                    .clip(RoundedCornerShape(10.dp))
            )

            Column(modifier = Modifier.weight(1f)) {
                Text(property.title, fontWeight = FontWeight.Bold, color = Color.White, fontSize = 13.5.sp)
                Text("${property.price.toInt()} ${property.currency} • ${property.propertyType}", color = themeColors.accent, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Star, contentDescription = null, tint = Color(0xFFFFB300), modifier = Modifier.size(13.dp))
                    Spacer(modifier = Modifier.width(3.dp))
                    Text(String.format(java.util.Locale.US, "%.1f", property.rating), color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("📍 ${property.localNeighborhood.ifEmpty { property.cityId }}", color = Color.Gray, fontSize = 10.sp)
                }
            }

            if (property.phone.isNotEmpty()) {
                IconButton(
                    onClick = onCall,
                    modifier = Modifier
                        .size(36.dp)
                        .background(Color(0xFF10B981).copy(alpha = 0.15f), CircleShape)
                ) {
                    Icon(Icons.Default.Phone, contentDescription = "اتصال", tint = Color(0xFF10B981), modifier = Modifier.size(18.dp))
                }
            }

            IconButton(
                onClick = onRemoveFavorite,
                modifier = Modifier
                    .size(36.dp)
                    .background(Color(0xFFEF4444).copy(alpha = 0.15f), CircleShape)
            ) {
                Icon(Icons.Default.Favorite, contentDescription = "إزالة من المفضلة", tint = Color(0xFFEF4444), modifier = Modifier.size(18.dp))
            }
        }
    }
}
