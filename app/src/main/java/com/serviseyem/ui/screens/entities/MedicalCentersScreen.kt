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
import com.serviseyem.data.ProviderEntity
import com.serviseyem.ui.MainViewModel
import com.serviseyem.ui.components.GenericEntityReviewsDialog
import com.serviseyem.ui.components.SmartAsyncImage
import com.serviseyem.utils.VisualThemePalette

@Composable
fun MedicalCentersScreen(
    viewModel: MainViewModel,
    themeColors: VisualThemePalette,
    onMedicalCenterClick: (ProviderEntity) -> Unit,
    onChatClick: (ProviderEntity) -> Unit,
    onBookAppointmentClick: (ProviderEntity) -> Unit
) {
    val providers by viewModel.providers.collectAsState()
    val isProvidersLoading by viewModel.isProvidersLoading.collectAsState()
    val cities by viewModel.cities.collectAsState()

    var searchQuery by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("الكل") }
    var selectedCityId by remember { mutableStateOf("الكل") }
    var selectedMinRating by remember { mutableStateOf(0.0f) }

    val categories = listOf("الكل", "مستشفيات", "عيادات تخصصية", "صيدليات", "مختبرات تحاليل", "مراكز أشعة")

    val currentUserId by viewModel.currentUserId.collectAsState()
    val adminRole by viewModel.adminRole.collectAsState()
    val isAdminUser = adminRole == "ADMIN" || adminRole == "SUPER_ADMIN" || adminRole == "MAIN_ADMIN" || adminRole == "OWNER"
    val isLoggedIn = currentUserId.isNotBlank() && currentUserId != "guest"

    var showCreateMedicalDialog by remember { mutableStateOf(false) }

    val medicalProviders = remember(providers, searchQuery, selectedCategory, selectedCityId, selectedMinRating, currentUserId, adminRole) {
        providers.filter { provider ->
            val isApprovedOrOwner = provider.subscriptionStatus == "APPROVED" || provider.isVerified || provider.phone == currentUserId || isAdminUser
            val isMedical = provider.categoryId == "medical" ||
                    provider.categoryId == "health" ||
                    provider.categoryId.contains("med", ignoreCase = true) ||
                    provider.categoryId.contains("health", ignoreCase = true) ||
                    provider.categoryId.contains("طبي", ignoreCase = true) ||
                    provider.categoryId.contains("مستشفى", ignoreCase = true) ||
                    provider.categoryId.contains("صيدل", ignoreCase = true) ||
                    provider.profession.contains("طب", ignoreCase = true) ||
                    provider.profession.contains("صيدل", ignoreCase = true) ||
                    provider.profession.contains("مستشفى", ignoreCase = true) ||
                    provider.profession.contains("عيادة", ignoreCase = true) ||
                    provider.profession.contains("مختبر", ignoreCase = true) ||
                    provider.profession.contains("أشعة", ignoreCase = true) ||
                    provider.specialization.contains("طب", ignoreCase = true) ||
                    provider.specialization.contains("صيدل", ignoreCase = true) ||
                    provider.specialization.contains("عيادة", ignoreCase = true) ||
                    provider.customCategoryName.contains("طبي", ignoreCase = true) ||
                    provider.customCategoryName.contains("مستشفى", ignoreCase = true) ||
                    provider.customCategoryName.contains("صيدلية", ignoreCase = true)

            val matchesSearch = searchQuery.isBlank() ||
                    provider.name.contains(searchQuery, ignoreCase = true) ||
                    provider.profession.contains(searchQuery, ignoreCase = true) ||
                    provider.specialization.contains(searchQuery, ignoreCase = true)

            val matchesCat = selectedCategory == "الكل" ||
                    provider.specialization.contains(selectedCategory, ignoreCase = true) ||
                    provider.profession.contains(selectedCategory, ignoreCase = true) ||
                    provider.customCategoryName.contains(selectedCategory, ignoreCase = true)

            val matchesCity = selectedCityId == "الكل" || provider.cityId == selectedCityId

            val matchesRating = provider.rating >= selectedMinRating

            isApprovedOrOwner && isMedical && matchesSearch && matchesCat && matchesCity && matchesRating
        }
    }

    var showGuestDialog by remember { mutableStateOf(false) }

    if (showCreateMedicalDialog) {
        com.serviseyem.StoreCreateEditDialog(
            store = null,
            viewModel = viewModel,
            themeColors = themeColors,
            sectionId = "medical",
            onDismiss = { showCreateMedicalDialog = false }
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
        items = medicalProviders,
        isLoading = isProvidersLoading,
        title = "المراكز الطبية والعيادات التخصصية",
        titleIcon = "🏥",
        searchPlaceholder = "بحث عن مركز طبي، عيادة، أو طبيب... 🏥",
        categories = categories,
        cities = cities,
        onSearchQueryChanged = { searchQuery = it },
        onCategorySelected = { selectedCategory = it },
        onCitySelected = { selectedCityId = it },
        onMinRatingSelected = { selectedMinRating = it },
        emptyMessage = "لا توجد مراكز طبية مطابقة للتصفية الحالية",
        extraHeaderContent = {
            Button(
                onClick = { 
                    viewModel.setTargetRegistrationType("medical")
                    viewModel.navigateToScreen(AppScreens.REGISTER_FORM) 
                },
                colors = ButtonDefaults.buttonColors(containerColor = themeColors.accent),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.fillMaxWidth().height(38.dp)
            ) {
                Text("➕ تسجيل وإضافة مركز طبي / صيدلية", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 11.5.sp)
            }
        },
        itemContent = { provider ->
            MedicalCenterCard(
                provider = provider,
                themeColors = themeColors,
                onClick = { onMedicalCenterClick(provider) },
                onChatClick = {
                    if (!isLoggedIn) {
                        showGuestDialog = true
                    } else {
                        onChatClick(provider)
                    }
                },
                onBookAppointmentClick = { onBookAppointmentClick(provider) }
            )
        }
    )
}

@Composable
fun MedicalCenterCard(
    provider: ProviderEntity,
    themeColors: VisualThemePalette,
    isLoggedIn: Boolean = true,
    onClick: () -> Unit,
    onChatClick: () -> Unit,
    onBookAppointmentClick: () -> Unit
) {
    val context = LocalContext.current
    var showReviewsDialog by remember { mutableStateOf(false) }

    val isVerified = provider.isVerified || provider.subscriptionStatus == "APPROVED" || provider.isVip
    val isVip = provider.isVip
    val coverImg = provider.coverImage.ifBlank { "" }
    val logoImg = provider.profileImage.ifBlank { "" }

    // 3D Card Container with layered depth and medical cyan/emerald/gold metallic borders
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = themeColors.surface,
        shadowElevation = if (isVip) 10.dp else 6.dp,
        tonalElevation = 4.dp,
        border = BorderStroke(
            width = if (isVip) 1.5.dp else 1.dp,
            brush = Brush.linearGradient(
                colors = when {
                    isVip -> listOf(Color(0xFFFFDF00), Color(0xFF0284C7), Color(0xFFFFDF00).copy(alpha = 0.4f))
                    isVerified -> listOf(Color(0xFF06B6D4), Color(0xFF10B981), Color(0xFF06B6D4).copy(alpha = 0.3f))
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
                        contentDescription = provider.name,
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                Brush.linearGradient(
                                    listOf(Color(0xFF082F49), Color(0xFF0F172A), Color(0xFF064E3B))
                                )
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("🏥", fontSize = 28.sp)
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
                            color = if (isVip) Color(0xFFB45309).copy(alpha = 0.9f) else Color(0xFF0E7490).copy(alpha = 0.9f),
                            shape = RoundedCornerShape(6.dp),
                            shadowElevation = 2.dp,
                            border = BorderStroke(0.5.dp, if (isVip) Color(0xFFFFD700) else Color(0xFF22D3EE))
                        ) {
                            Text(
                                text = if (isVip) "👑 VIP" else "مركز موثق ✓",
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
                                text = String.format(java.util.Locale.US, "%.1f", provider.rating),
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
                        border = BorderStroke(2.dp, if (isVip) Color(0xFFFFD700) else Color(0xFF06B6D4)),
                        modifier = Modifier
                            .offset(y = (-18).dp)
                            .size(46.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            if (logoImg.isNotBlank()) {
                                SmartAsyncImage(
                                    model = logoImg,
                                    contentDescription = provider.name,
                                    modifier = Modifier.fillMaxSize().clip(CircleShape)
                                )
                            } else {
                                Text("🏥", fontSize = 20.sp)
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
                            text = provider.name,
                            fontSize = 12.5.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        val specText = provider.specialization.ifBlank { provider.profession.ifBlank { "مركز طبي متكامل" } }
                        Text(
                            text = specText,
                            fontSize = 9.5.sp,
                            color = Color(0xFF06B6D4),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                // Neighborhood & Availability
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
                        val locText = provider.localNeighborhood.ifBlank { provider.area.ifBlank { "اليمن" } }
                        Text(
                            text = locText,
                            fontSize = 9.sp,
                            color = Color.LightGray,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    Text(
                        text = if (provider.isAvailable) "🟢 متاح" else "⚪ مغلق",
                        fontSize = 8.5.sp,
                        color = if (provider.isAvailable) Color(0xFF10B981) else Color.LightGray,
                        maxLines = 1
                    )
                }

                // 3. Compact 3D Action Buttons: [تفاصيل] [حجز موعد]
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

                    // Button 3: Book Appointment
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = Color(0xFF10B981),
                        shadowElevation = 2.dp,
                        modifier = Modifier
                            .weight(1f)
                            .height(29.dp)
                            .clickable { onBookAppointmentClick() }
                    ) {
                        Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                            Text("حجز موعد 🩺", color = Color.White, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }

    if (showReviewsDialog) {
        GenericEntityReviewsDialog(
            title = provider.name,
            rating = provider.rating,
            numReviews = provider.numReviews,
            themeColors = themeColors,
            onDismiss = { showReviewsDialog = false }
        )
    }
}
