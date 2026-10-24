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
import com.serviseyem.data.PropertyEntity
import com.serviseyem.ui.MainViewModel
import com.serviseyem.ui.components.GenericEntityReviewsDialog
import com.serviseyem.ui.components.SmartAsyncImage
import com.serviseyem.utils.VisualThemePalette

@Composable
fun PropertiesScreen(
    viewModel: MainViewModel,
    themeColors: VisualThemePalette,
    onPropertyClick: (PropertyEntity) -> Unit,
    onChatClick: (PropertyEntity) -> Unit,
    onRequestInspectionClick: (PropertyEntity) -> Unit
) {
    val properties by viewModel.properties.collectAsState()
    val cities by viewModel.cities.collectAsState()

    val currentUserId by viewModel.currentUserId.collectAsState()
    val adminRole by viewModel.adminRole.collectAsState()
    val isAdminUser = adminRole == "ADMIN" || adminRole == "SUPER_ADMIN" || adminRole == "MAIN_ADMIN" || adminRole == "OWNER"
    val isLoggedIn = currentUserId.isNotBlank() && currentUserId != "guest"

    var searchQuery by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("الكل") }
    var selectedCityId by remember { mutableStateOf("الكل") }
    var selectedTypeFilter by remember { mutableStateOf("الكل") } // rent, sale
    var selectedMinRating by remember { mutableStateOf(0.0f) }
    var showCreatePropertyDialog by remember { mutableStateOf(false) }

    val categories = listOf("الكل", "شقة", "بيت ومستقل", "محل تجاري", "أرض")

    // Determine loading state from properties list presence
    val isLoading = remember(properties) { properties.isEmpty() }

    val filteredProperties = remember(properties, searchQuery, selectedCategory, selectedCityId, selectedTypeFilter, selectedMinRating, currentUserId, adminRole) {
        properties.filter { prop ->
            val isApprovedOrOwner = prop.isApproved || prop.ownerId == currentUserId || isAdminUser
            val matchesSearch = searchQuery.isBlank() ||
                    prop.title.contains(searchQuery, ignoreCase = true) ||
                    prop.description.contains(searchQuery, ignoreCase = true) ||
                    prop.localNeighborhood.contains(searchQuery, ignoreCase = true)

            val matchesCat = selectedCategory == "الكل" ||
                    (selectedCategory == "شقة" && prop.propertyType.contains("apartment", ignoreCase = true)) ||
                    (selectedCategory == "بيت ومستقل" && prop.propertyType.contains("house", ignoreCase = true)) ||
                    (selectedCategory == "محل تجاري" && prop.propertyType.contains("shop", ignoreCase = true)) ||
                    (selectedCategory == "أرض" && prop.propertyType.contains("land", ignoreCase = true)) ||
                    prop.propertyType.contains(selectedCategory, ignoreCase = true)

            val matchesType = selectedTypeFilter == "الكل" || prop.type == selectedTypeFilter

            val matchesCity = selectedCityId == "الكل" || prop.cityId == selectedCityId

            val matchesRating = prop.rating >= selectedMinRating

            isApprovedOrOwner && !prop.isDeleted && matchesSearch && matchesCat && matchesType && matchesCity && matchesRating
        }
    }

    var showGuestDialog by remember { mutableStateOf(false) }

    if (showCreatePropertyDialog) {
        com.serviseyem.PropertyCreateEditDialog(
            property = null,
            viewModel = viewModel,
            themeColors = themeColors,
            onDismiss = { showCreatePropertyDialog = false }
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
        items = filteredProperties,
        isLoading = isLoading,
        title = "العقارات والأراضي اليمنيّة",
        titleIcon = "🏠",
        searchPlaceholder = "بحث عن شقة، أرض، بيت للإيجار أو البيع... 🏠",
        categories = categories,
        cities = cities,
        onSearchQueryChanged = { searchQuery = it },
        onCategorySelected = { selectedCategory = it },
        onCitySelected = { selectedCityId = it },
        onMinRatingSelected = { selectedMinRating = it },
        emptyMessage = "لا توجد عقارات مطابقة للتصفية الحالية",
        extraHeaderContent = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(
                    onClick = { 
                        viewModel.setTargetRegistrationType("property")
                        viewModel.navigateToScreen(AppScreens.REGISTER_FORM) 
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = themeColors.accent),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth().height(38.dp)
                ) {
                    Text("➕ تسجيل وإضافة إعلان عقاري جديد", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 11.5.sp)
                }

                // 🔑 Listing Type Filter (Rent vs Sale)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                Text("🏷️ طبيعة العقد:", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.LightGray)
                listOf(
                    "الكل" to "الكل",
                    "rent" to "للإيجار 🔑",
                    "sale" to "للبيع والتمليك 📜"
                ).forEach { (typeVal, typeLabel) ->
                    FilterChip(
                        selected = selectedTypeFilter == typeVal,
                        onClick = { selectedTypeFilter = typeVal },
                        label = { Text(typeLabel, fontSize = 10.sp) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = themeColors.accent,
                            selectedLabelColor = Color.Black
                        )
                    )
                }
            }
        }
    },
        itemContent = { prop ->
            PropertyCard(
                property = prop,
                themeColors = themeColors,
                isLoggedIn = isLoggedIn,
                onClick = { onPropertyClick(prop) },
                onChatClick = {
                    if (!isLoggedIn) {
                        showGuestDialog = true
                    } else {
                        onChatClick(prop)
                    }
                },
                onRequestInspectionClick = { onRequestInspectionClick(prop) }
            )
        }
    )
}

@Composable
fun PropertyCard(
    property: PropertyEntity,
    themeColors: VisualThemePalette,
    isLoggedIn: Boolean = true,
    onClick: () -> Unit,
    onChatClick: () -> Unit,
    onRequestInspectionClick: () -> Unit
) {
    val context = LocalContext.current
    var showReviewsDialog by remember { mutableStateOf(false) }

    val imageSource = property.images.firstOrNull() ?: ""
    val isRent = property.type == "rent"
    val isVerified = property.isVerified || property.isApproved || property.isVip
    val isVip = property.isVip

    // 3D Card Container with layered depth and luxury real-estate metallic borders
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = themeColors.surface,
        shadowElevation = if (isVip) 10.dp else 6.dp,
        tonalElevation = 4.dp,
        border = BorderStroke(
            width = if (isVip) 1.5.dp else 1.dp,
            brush = Brush.linearGradient(
                colors = when {
                    isVip -> listOf(Color(0xFFFFDF00), Color(0xFF7C3AED), Color(0xFFFFDF00).copy(alpha = 0.4f))
                    isRent -> listOf(Color(0xFF3B82F6), Color(0xFF60A5FA), Color(0xFF3B82F6).copy(alpha = 0.3f))
                    else -> listOf(Color(0xFF10B981), Color(0xFF34D399), Color(0xFF10B981).copy(alpha = 0.3f))
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
                    .height(86.dp)
                    .background(Color(0xFF0F172A))
            ) {
                if (imageSource.isNotBlank()) {
                    SmartAsyncImage(
                        model = imageSource,
                        contentDescription = property.title,
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                Brush.linearGradient(
                                    listOf(Color(0xFF1E1B4B), Color(0xFF0F172A), Color(0xFF312E81))
                                )
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("🏠", fontSize = 28.sp)
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

                // Badges row (Rent/Sale + Rating)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 6.dp, vertical = 5.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        color = if (isRent) Color(0xFF2563EB).copy(alpha = 0.9f) else Color(0xFF059669).copy(alpha = 0.9f),
                        shape = RoundedCornerShape(6.dp),
                        shadowElevation = 2.dp,
                        border = BorderStroke(0.5.dp, if (isRent) Color(0xFF93C5FD) else Color(0xFF6EE7B7))
                    ) {
                        Text(
                            text = if (isRent) "🔑 للإيجار" else "🏷️ للبيع",
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
                        )
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
                                text = String.format(java.util.Locale.US, "%.1f", property.rating),
                                fontSize = 9.5.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                    }
                }
            }

            // 2. Info Section (Compact & Crisp)
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 6.dp),
                verticalArrangement = Arrangement.spacedBy(3.dp)
            ) {
                // Title & Price
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = property.title,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "${property.price.toInt()} ${property.currency}",
                        fontSize = 11.5.sp,
                        fontWeight = FontWeight.Bold,
                        color = themeColors.accent
                    )
                }

                // Type & Neighborhood
                val typeName = when (property.propertyType) {
                    "apartment" -> "🏢 شقة"
                    "house" -> "🏡 منزل"
                    "villa" -> "🏰 فيلا"
                    "shop" -> "🏬 محل"
                    "land" -> "📐 أرض"
                    else -> "🏠 ${property.propertyType}"
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = typeName,
                        fontSize = 9.5.sp,
                        color = themeColors.accent,
                        maxLines = 1
                    )
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.LocationOn, contentDescription = null, tint = Color.LightGray, modifier = Modifier.size(10.dp))
                        Spacer(modifier = Modifier.width(2.dp))
                        val locText = property.localNeighborhood.ifBlank { "اليمن" }
                        Text(
                            text = locText,
                            fontSize = 9.sp,
                            color = Color.LightGray,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                // 3. Compact 3D Action Buttons: [تفاصيل] [معاينة]
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 3.dp),
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

                    // Button 3: Request Inspection
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = Color(0xFF10B981),
                        shadowElevation = 2.dp,
                        modifier = Modifier
                            .weight(1f)
                            .height(29.dp)
                            .clickable { onRequestInspectionClick() }
                    ) {
                        Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                            Text("معاينة 👁️", color = Color.White, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }

    if (showReviewsDialog) {
        GenericEntityReviewsDialog(
            title = property.title,
            rating = property.rating,
            numReviews = property.numReviews,
            themeColors = themeColors,
            onDismiss = { showReviewsDialog = false }
        )
    }
}
