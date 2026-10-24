package com.serviseyem.ui.screens.entities

import android.content.Intent
import com.serviseyem.ui.*
import android.net.Uri
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.serviseyem.data.StoreEntity
import com.serviseyem.ui.MainViewModel
import com.serviseyem.ui.components.GenericEntityReviewsDialog
import com.serviseyem.ui.components.SmartAsyncImage
import com.serviseyem.utils.VisualThemePalette

@Composable
fun RestaurantsScreen(
    viewModel: MainViewModel,
    themeColors: VisualThemePalette,
    onRestaurantClick: (StoreEntity) -> Unit,
    onChatClick: (StoreEntity) -> Unit,
    onOrderMealClick: (StoreEntity) -> Unit
) {
    val stores by viewModel.stores.collectAsState()
    val cities by viewModel.cities.collectAsState()

    val currentUserId by viewModel.currentUserId.collectAsState()
    val adminRole by viewModel.adminRole.collectAsState()
    val isAdminUser = adminRole == "ADMIN" || adminRole == "SUPER_ADMIN" || adminRole == "MAIN_ADMIN" || adminRole == "OWNER"
    val isLoggedIn = currentUserId.isNotBlank() && currentUserId != "guest"

    var searchQuery by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("الكل") }
    var selectedCityId by remember { mutableStateOf("الكل") }
    var selectedMinRating by remember { mutableStateOf(0.0f) }
    var showCreateRestaurantDialog by remember { mutableStateOf(false) }

    val categories = listOf("الكل", "شعبية ويمنية", "بحرية", "وجبات سريعة", "مشاوي ولحوم", "مخابز وحلويات", "كافيهات وعصائر")

    val isLoading = remember(stores) { stores.isEmpty() }

    val restaurantStores = remember(stores, searchQuery, selectedCategory, selectedCityId, selectedMinRating, currentUserId, adminRole) {
        stores.filter { store ->
            val isApprovedOrOwner = store.isApproved || store.ownerId == currentUserId || isAdminUser
            val isRestaurant = (store.sectionId == "restaurants" ||
                    store.categoryId.contains("rest", ignoreCase = true) ||
                    store.categoryId.contains("food", ignoreCase = true) ||
                    store.categoryId.contains("مطعم", ignoreCase = true) ||
                    store.categoryId.contains("وجب", ignoreCase = true) ||
                    store.name.contains("مطعم", ignoreCase = true) ||
                    store.name.contains("كافيه", ignoreCase = true) ||
                    store.name.contains("وجبات", ignoreCase = true)) && !store.isDeleted

            val matchesSearch = searchQuery.isBlank() ||
                    store.name.contains(searchQuery, ignoreCase = true) ||
                    store.description.contains(searchQuery, ignoreCase = true) ||
                    store.localNeighborhood.contains(searchQuery, ignoreCase = true)

            val matchesCat = selectedCategory == "الكل" ||
                    store.categoryId.contains(selectedCategory, ignoreCase = true) ||
                    store.name.contains(selectedCategory, ignoreCase = true)

            val matchesCity = selectedCityId == "الكل" || store.cityId == selectedCityId

            val matchesRating = store.rating >= selectedMinRating

            isApprovedOrOwner && isRestaurant && matchesSearch && matchesCat && matchesCity && matchesRating
        }
    }

    var showGuestDialog by remember { mutableStateOf(false) }

    if (showCreateRestaurantDialog) {
        com.serviseyem.StoreCreateEditDialog(
            store = null,
            viewModel = viewModel,
            themeColors = themeColors,
            sectionId = "restaurants",
            onDismiss = { showCreateRestaurantDialog = false }
        )
    }

    if (showGuestDialog) {
        com.serviseyem.ui.screens.register.GuestRegistrationDialog(
            viewModel = viewModel,
            themeColors = themeColors,
            onDismiss = { showGuestDialog = false },
            onRegisterCompleted = { _, _, _, _ -> showGuestDialog = false }
        )
    }

    GenericSectionView(
        themeColors = themeColors,
        items = restaurantStores,
        isLoading = isLoading,
        title = "المطاعم والكافيهات اليمنيّة",
        titleIcon = "🍔",
        searchPlaceholder = "بحث عن مطعم، كافيه، أو وجبة... 🍔",
        categories = categories,
        cities = cities,
        onSearchQueryChanged = { searchQuery = it },
        onCategorySelected = { selectedCategory = it },
        onCitySelected = { selectedCityId = it },
        onMinRatingSelected = { selectedMinRating = it },
        emptyMessage = "لا توجد مطاعم مطابقة للبحث",
        extraHeaderContent = {
            Button(
                onClick = { 
                    viewModel.setTargetRegistrationType("restaurant")
                    viewModel.navigateToScreen(AppScreens.REGISTER_FORM) 
                },
                colors = ButtonDefaults.buttonColors(containerColor = themeColors.accent),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.fillMaxWidth().height(38.dp)
            ) {
                Text("➕ تسجيل وإضافة مطعم / كافيه جديد", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 11.5.sp)
            }
        },
        itemContent = { rest ->
            RestaurantCard(
                restaurant = rest,
                themeColors = themeColors,
                onClick = { onRestaurantClick(rest) },
                onChatClick = {
                    if (!isLoggedIn) {
                        showGuestDialog = true
                    } else {
                        onChatClick(rest)
                    }
                },
                onOrderMealClick = { onOrderMealClick(rest) }
            )
        }
    )
}

@Composable
fun RestaurantCard(
    restaurant: StoreEntity,
    themeColors: VisualThemePalette,
    isLoggedIn: Boolean = true,
    onClick: () -> Unit,
    onChatClick: () -> Unit,
    onOrderMealClick: () -> Unit
) {
    val context = LocalContext.current
    var showReviewsDialog by remember { mutableStateOf(false) }

    val isVerified = restaurant.isVerified || restaurant.isActive
    val isVip = restaurant.isVip
    val coverImg = restaurant.coverImage.ifBlank { "" }
    val logoImg = restaurant.logoImage.ifBlank { "" }

    // 3D Card Container with layered depth and luxury warm/fiery metallic borders
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = themeColors.surface,
        shadowElevation = if (isVip) 10.dp else 6.dp,
        tonalElevation = 4.dp,
        border = BorderStroke(
            width = if (isVip) 1.5.dp else 1.dp,
            brush = Brush.linearGradient(
                colors = when {
                    isVip -> listOf(Color(0xFFFFDF00), Color(0xFFEA580C), Color(0xFFFFDF00).copy(alpha = 0.4f))
                    isVerified -> listOf(Color(0xFFF59E0B), Color(0xFFEF4444), Color(0xFFF59E0B).copy(alpha = 0.3f))
                    else -> listOf(Color.White.copy(alpha = 0.22f), Color.White.copy(alpha = 0.05f))
                }
            )
        ),
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color.White.copy(alpha = 0.05f),
                            Color.Transparent,
                            Color.Black.copy(alpha = 0.45f)
                        )
                    )
                )
        ) {
            // 1. Compact 3D Cover Header
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(82.dp)
                    .background(Color(0xFF0F172A))
            ) {
                if (coverImg.isNotBlank()) {
                    SmartAsyncImage(
                        model = coverImg,
                        contentDescription = restaurant.name,
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                Brush.linearGradient(
                                    listOf(Color(0xFF451A03), Color(0xFF1E293B), Color(0xFF7C2D12))
                                )
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("🍔", fontSize = 28.sp)
                    }
                }

                // 3D Specular top edge highlight
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(2.dp)
                        .background(
                            Brush.horizontalGradient(
                                listOf(Color.Transparent, Color.White.copy(alpha = 0.6f), Color.Transparent)
                            )
                        )
                )

                // Dark multi-stop gradient for text readability
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(
                                    Color.Black.copy(alpha = 0.35f),
                                    Color.Transparent,
                                    Color.Black.copy(alpha = 0.85f)
                                )
                            )
                        )
                )

                // Top badges row (VIP/Verified + Rating)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 6.dp, vertical = 5.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (isVip || isVerified) {
                        Surface(
                            color = if (isVip) Color(0xFFB45309).copy(alpha = 0.9f) else Color(0xFFC2410C).copy(alpha = 0.9f),
                            shape = RoundedCornerShape(6.dp),
                            shadowElevation = 2.dp,
                            border = BorderStroke(0.5.dp, if (isVip) Color(0xFFFFD700) else Color(0xFFFB923C))
                        ) {
                            Text(
                                text = if (isVip) "👑 VIP" else "موثق ✓",
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
                            )
                        }
                    } else {
                        Spacer(modifier = Modifier.width(1.dp))
                    }

                    // Rating Badge
                    Surface(
                        color = Color.Black.copy(alpha = 0.75f),
                        shape = RoundedCornerShape(6.dp),
                        shadowElevation = 2.dp,
                        border = BorderStroke(0.5.dp, Color(0xFFFFD700).copy(alpha = 0.4f)),
                        modifier = Modifier.clickable { showReviewsDialog = true }
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.Star, contentDescription = "Rating", tint = Color(0xFFFFD700), modifier = Modifier.size(11.dp))
                            Spacer(modifier = Modifier.width(2.5.dp))
                            Text(
                                text = String.format(java.util.Locale.US, "%.1f", restaurant.rating),
                                fontSize = 9.5.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                    }
                }
            }

            // 2. Overlapping 3D Avatar & Info Section
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.Bottom
                ) {
                    // Floating 3D Logo
                    Surface(
                        shape = CircleShape,
                        color = Color(0xFF0F172A),
                        shadowElevation = 6.dp,
                        border = BorderStroke(2.dp, if (isVip) Color(0xFFFFD700) else Color(0xFFF59E0B)),
                        modifier = Modifier
                            .offset(y = (-18).dp)
                            .size(46.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            if (logoImg.isNotBlank()) {
                                SmartAsyncImage(
                                    model = logoImg,
                                    contentDescription = restaurant.name,
                                    modifier = Modifier.fillMaxSize().clip(CircleShape)
                                )
                            } else {
                                Text("🍔", fontSize = 20.sp)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .padding(bottom = 2.dp)
                    ) {
                        Text(
                            text = restaurant.name,
                            fontSize = 12.5.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        val descText = restaurant.description.ifBlank { "مطعم ومأكولات مميزة" }
                        Text(
                            text = descText,
                            fontSize = 9.5.sp,
                            color = Color(0xFFF59E0B),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                // Neighborhood & Hours
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .offset(y = (-10).dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Default.LocationOn, contentDescription = null, tint = Color.LightGray, modifier = Modifier.size(11.dp))
                        Spacer(modifier = Modifier.width(2.dp))
                        val locText = restaurant.localNeighborhood.ifBlank { "اليمن" }
                        Text(
                            text = locText,
                            fontSize = 9.sp,
                            color = Color.LightGray,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    val hours = com.serviseyem.utils.formatArabicWorkingHours(restaurant.workingHours, "10:00 ص - 12:00 م")
                    Text(
                        text = "⏰ $hours",
                        fontSize = 8.5.sp,
                        color = themeColors.textSecondary,
                        maxLines = 1
                    )
                }

                // 3. Compact 3D Action Buttons: [تفاصيل] [اطلب وجبتك]
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .offset(y = (-4).dp)
                        .padding(bottom = 6.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Button 1: Details
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = themeColors.accent,
                        shadowElevation = 3.dp,
                        modifier = Modifier
                            .weight(1f)
                            .height(29.dp)
                            .clickable { onClick() }
                    ) {
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .fillMaxSize()
                                .background(
                                    Brush.verticalGradient(
                                        listOf(Color.White.copy(alpha = 0.25f), Color.Transparent)
                                    )
                                )
                        ) {
                            Text("التفاصيل 📋", fontSize = 9.5.sp, color = Color.Black, fontWeight = FontWeight.Bold)
                        }
                    }

                    // Button 1: Details
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = themeColors.accent,
                        shadowElevation = 2.dp,
                        modifier = Modifier
                            .weight(1f)
                            .height(29.dp)
                            .clickable { onClick() }
                    ) {
                        Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                            Text("التفاصيل 📋", fontSize = 9.sp, color = Color.Black, fontWeight = FontWeight.Bold)
                        }
                    }

                    // Button 2: Instant Chat
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = themeColors.primary,
                        shadowElevation = 2.dp,
                        modifier = Modifier
                            .weight(1.1f)
                            .height(29.dp)
                            .clickable { onChatClick() }
                    ) {
                        Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                            Text("محادثة 💬", color = Color.White, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    // Button 3: Order Meal
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = Color(0xFF10B981),
                        shadowElevation = 2.dp,
                        modifier = Modifier
                            .weight(1f)
                            .height(29.dp)
                            .clickable { onOrderMealClick() }
                    ) {
                        Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                            Text("اطلب 🍕", color = Color.White, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }

    if (showReviewsDialog) {
        GenericEntityReviewsDialog(
            title = restaurant.name,
            rating = restaurant.rating,
            numReviews = restaurant.numReviews,
            themeColors = themeColors,
            onDismiss = { showReviewsDialog = false }
        )
    }
}
