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
fun StoresScreen(
    viewModel: MainViewModel,
    themeColors: VisualThemePalette,
    onStoreClick: (StoreEntity) -> Unit,
    onChatClick: (StoreEntity) -> Unit,
    onRequestServiceClick: (StoreEntity) -> Unit
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
    var showCreateStoreDialog by remember { mutableStateOf(false) }

    val categories = listOf("الكل", "سوبرماركت", "ملابس وأزياء", "إلكترونيات", "أجهزة منزلية", "أدوات مطبخ", "أحذية وحقائب", "عطور وتجميل")

    val isLoading = remember(stores) { stores.isEmpty() }

    val filteredStores = remember(stores, searchQuery, selectedCategory, selectedCityId, selectedMinRating, currentUserId, adminRole) {
        stores.filter { store ->
            val isApprovedOrOwner = store.isApproved || store.ownerId == currentUserId || isAdminUser
            val isPureStore = store.sectionId != "restaurants" &&
                    !store.categoryId.contains("rest", ignoreCase = true) &&
                    !store.categoryId.contains("food", ignoreCase = true) &&
                    !store.name.contains("مطعم", ignoreCase = true) &&
                    !store.isDeleted

            val matchesSearch = searchQuery.isBlank() ||
                    store.name.contains(searchQuery, ignoreCase = true) ||
                    store.localNeighborhood.contains(searchQuery, ignoreCase = true) ||
                    store.description.contains(searchQuery, ignoreCase = true)

            val matchesCat = selectedCategory == "الكل" ||
                    store.categoryId.contains(selectedCategory, ignoreCase = true) ||
                    store.name.contains(selectedCategory, ignoreCase = true)

            val matchesCity = selectedCityId == "الكل" || store.cityId == selectedCityId

            val matchesRating = store.rating >= selectedMinRating

            isApprovedOrOwner && isPureStore && matchesSearch && matchesCat && matchesCity && matchesRating
        }
    }

    var showGuestDialog by remember { mutableStateOf(false) }

    if (showCreateStoreDialog) {
        com.serviseyem.StoreCreateEditDialog(
            store = null,
            viewModel = viewModel,
            themeColors = themeColors,
            sectionId = "stores",
            onDismiss = { showCreateStoreDialog = false }
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
        items = filteredStores,
        isLoading = isLoading,
        title = "المحلات التجارية والمتاجر",
        titleIcon = "🏪",
        searchPlaceholder = "بحث في المحلات والمتاجر... 🏪",
        categories = categories,
        cities = cities,
        onSearchQueryChanged = { searchQuery = it },
        onCategorySelected = { selectedCategory = it },
        onCitySelected = { selectedCityId = it },
        onMinRatingSelected = { selectedMinRating = it },
        emptyMessage = "لا توجد محلات تجارية مطابقة للبحث",
        extraHeaderContent = {
            Button(
                onClick = { 
                    viewModel.setTargetRegistrationType("store")
                    viewModel.navigateToScreen(AppScreens.REGISTER_FORM) 
                },
                colors = ButtonDefaults.buttonColors(containerColor = themeColors.accent),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.fillMaxWidth().height(38.dp)
            ) {
                Text("➕ تسجيل وإضافة متجر تجاري جديد", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 11.5.sp)
            }
        },
        itemContent = { store ->
            StoreItemCard(
                store = store,
                themeColors = themeColors,
                onClick = { onStoreClick(store) },
                onChatClick = {
                    if (!isLoggedIn) {
                        showGuestDialog = true
                    } else {
                        onChatClick(store)
                    }
                },
                onRequestServiceClick = { onRequestServiceClick(store) }
            )
        }
    )
}

@Composable
fun StoreItemCard(
    store: StoreEntity,
    themeColors: VisualThemePalette,
    isLoggedIn: Boolean = true,
    onClick: () -> Unit,
    onChatClick: () -> Unit,
    onRequestServiceClick: () -> Unit
) {
    val context = LocalContext.current
    var showReviewsDialog by remember { mutableStateOf(false) }

    val isVerified = store.isVerified || store.isActive
    val isVip = store.isVip
    val coverImg = store.coverImage.ifBlank { "" }
    val logoImg = store.logoImage.ifBlank { "" }

    // 3D Card Container with layered depth and luxury metallic borders
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = themeColors.surface,
        shadowElevation = if (isVip) 10.dp else 6.dp,
        tonalElevation = 4.dp,
        border = BorderStroke(
            width = if (isVip) 1.5.dp else 1.dp,
            brush = Brush.linearGradient(
                colors = when {
                    isVip -> listOf(Color(0xFFFFDF00), Color(0xFFD97706), Color(0xFFFFDF00).copy(alpha = 0.4f))
                    isVerified -> listOf(themeColors.accent, Color(0xFF10B981), themeColors.accent.copy(alpha = 0.3f))
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
                        contentDescription = store.name,
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                Brush.linearGradient(
                                    listOf(Color(0xFF1E293B), Color(0xFF0F172A), Color(0xFF334155))
                                )
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("🏪", fontSize = 28.sp)
                    }
                }

                // 3D Shimmer / Specular top edge highlight
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
                            color = if (isVip) Color(0xFFB45309).copy(alpha = 0.9f) else Color(0xFF065F46).copy(alpha = 0.9f),
                            shape = RoundedCornerShape(6.dp),
                            shadowElevation = 2.dp,
                            border = BorderStroke(0.5.dp, if (isVip) Color(0xFFFFD700) else Color(0xFF34D399))
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
                                text = String.format(java.util.Locale.US, "%.1f", store.rating),
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
                        border = BorderStroke(2.dp, if (isVip) Color(0xFFFFD700) else themeColors.accent),
                        modifier = Modifier
                            .offset(y = (-18).dp)
                            .size(46.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            if (logoImg.isNotBlank()) {
                                SmartAsyncImage(
                                    model = logoImg,
                                    contentDescription = store.name,
                                    modifier = Modifier.fillMaxSize().clip(CircleShape)
                                )
                            } else {
                                Text("🏪", fontSize = 20.sp)
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
                            text = store.name,
                            fontSize = 12.5.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        val descText = store.description.ifBlank { "متجر تجاري معتمد" }
                        Text(
                            text = descText,
                            fontSize = 9.5.sp,
                            color = themeColors.accent,
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
                        val locText = store.localNeighborhood.ifBlank { "اليمن" }
                        Text(
                            text = locText,
                            fontSize = 9.sp,
                            color = Color.LightGray,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    val hours = com.serviseyem.utils.formatArabicWorkingHours(store.workingHours)
                    Text(
                        text = "⏰ $hours",
                        fontSize = 8.5.sp,
                        color = themeColors.textSecondary,
                        maxLines = 1
                    )
                }

                // 3. Compact 3D Action Buttons: [تفاصيل] [اتصال / محادثة]
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

                    // Button 2: Instant Chat (دائماً يفتح المحادثة الفورية مع المتجر)
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

                    // Button 3: Direct Phone Call (إن وجد رقم هاتف)
                    val hasPhone = store.phone.isNotBlank()
                    if (hasPhone) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = Color(0xFF10B981),
                            shadowElevation = 2.dp,
                            modifier = Modifier
                                .weight(0.9f)
                                .height(29.dp)
                                .clickable {
                                    val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:${store.phone}"))
                                    context.startActivity(intent)
                                }
                        ) {
                            Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                                Text("اتصال 📞", color = Color.White, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }
    }

    if (showReviewsDialog) {
        GenericEntityReviewsDialog(
            title = store.name,
            rating = store.rating,
            numReviews = store.numReviews,
            themeColors = themeColors,
            onDismiss = { showReviewsDialog = false }
        )
    }
}
