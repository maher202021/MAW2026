package com.serviseyem.ui.screens.entities

import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
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
import com.serviseyem.data.*
import com.serviseyem.ui.*
import com.serviseyem.ui.components.SmartAsyncImage
import com.serviseyem.ui.helpers.AppPreferenceHelper
import com.serviseyem.utils.VisualThemePalette

enum class ProfileEntityType(val labelAr: String, val badgeColor: Color) {
    TECHNICIAN("فني / مهني معتمد", Color(0xFF3B82F6)),
    STORE("متجر / تسوق تجاري", Color(0xFF10B981)),
    RESTAURANT("مطعم / كافيه", Color(0xFFF59E0B)),
    MEDICAL("مركز طبي / عيادة", Color(0xFFEF4444)),
    REAL_ESTATE("مكتب عقاري / استثمار", Color(0xFF8B5CF6)),
    JOB("شواغر وفرص عمل", Color(0xFF06B6D4)),
    GENERAL("نشاط تجاري معتمد", Color(0xFF6B7280))
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DynamicPolymorphicProfileScreen(
    provider: ProviderEntity? = null,
    store: StoreEntity? = null,
    property: PropertyEntity? = null,
    job: JobEntity? = null,
    viewModel: MainViewModel,
    themeColors: VisualThemePalette,
    onBackClick: () -> Unit,
    onOpenChat: (channelId: String) -> Unit = {},
    onRequestBooking: () -> Unit = {},
    onOrderProduct: () -> Unit = {}
) {
    val context = LocalContext.current
    val categories by viewModel.categories.collectAsState()
    val products by viewModel.products.collectAsState()
    val ratings by viewModel.ratings.collectAsState()
    val bookings by viewModel.bookings.collectAsState()

    val currentUserId by viewModel.currentUserId.collectAsState()
    val currentUserPhone by viewModel.currentUserPhone.collectAsState()
    val adminRole by viewModel.adminRole.collectAsState()

    // Determine Entity Type dynamically
    val entityType = remember(provider, store, property, job, categories) {
        when {
            provider != null -> {
                val catName = categories.find { it.id == provider.categoryId }?.name?.lowercase() ?: ""
                when {
                    provider.categoryId == "medical" || catName.contains("طب") || catName.contains("عياد") -> ProfileEntityType.MEDICAL
                    provider.categoryId == "restaurants" || catName.contains("مطعم") || catName.contains("كافيه") -> ProfileEntityType.RESTAURANT
                    provider.categoryId == "stores" || catName.contains("متجر") || catName.contains("سوق") -> ProfileEntityType.STORE
                    else -> ProfileEntityType.TECHNICIAN
                }
            }
            store != null -> {
                when (store.sectionId) {
                    "restaurants" -> ProfileEntityType.RESTAURANT
                    "medical" -> ProfileEntityType.MEDICAL
                    else -> ProfileEntityType.STORE
                }
            }
            property != null -> ProfileEntityType.REAL_ESTATE
            job != null -> ProfileEntityType.JOB
            else -> ProfileEntityType.GENERAL
        }
    }

    // Unify primary fields
    val entityName = provider?.name ?: store?.name ?: property?.title ?: job?.title ?: "ملف النشاط"
    val entityPhone = provider?.phone ?: store?.phone ?: property?.phone ?: job?.phone ?: ""
    val entityAddress = provider?.localNeighborhood?.ifEmpty { provider.area }
        ?: store?.localNeighborhood?.ifEmpty { store.cityId }
        ?: property?.localNeighborhood?.ifEmpty { property.cityId }
        ?: job?.address?.ifEmpty { job.cityId } ?: "اليمن"
    val ratingValue = provider?.rating ?: store?.rating ?: property?.rating ?: 5.0f
    val reviewsCount = provider?.numReviews ?: store?.numReviews ?: property?.numReviews ?: 0
    val profileCover = provider?.coverImage?.ifEmpty { provider.profileImage }
        ?: store?.coverImage?.ifEmpty { store.logoImage }
        ?: property?.images?.firstOrNull() ?: ""
    val entityDescription = provider?.profession?.ifEmpty { provider.specialization }
        ?: store?.description
        ?: property?.description
        ?: job?.description ?: "لا يوجد وصف متاح."

    var selectedTab by remember { mutableIntStateOf(0) }
    var showRatingDialog by remember { mutableStateOf(false) }

    val entityId = provider?.id ?: store?.id ?: property?.id ?: job?.id ?: ""
    val favoriteIds by viewModel.favoriteIds.collectAsState()
    val isFav = remember(favoriteIds, entityId) { favoriteIds.contains(entityId) }
    val entityReviews = remember(ratings, entityId) {
        ratings.filter { it.targetId == entityId }
    }

    // Ownership logic: check if logged-in user is the owner
    val joinPhoneState by viewModel.joinRequestPhone.collectAsState()
    val isOwner = remember(currentUserId, currentUserPhone, provider, store, property, adminRole, joinPhoneState) {
        val phoneClean = AppPreferenceHelper.normalizePhoneNumber(currentUserPhone)
        val uidClean = currentUserId.trim()
        val provPhone = AppPreferenceHelper.normalizePhoneNumber(provider?.phone ?: "")
        val storePhone = AppPreferenceHelper.normalizePhoneNumber(store?.phone ?: "")
        val storeOwner = store?.ownerId?.trim() ?: ""
        val propPhone = AppPreferenceHelper.normalizePhoneNumber(property?.phone ?: "")
        val propOwner = property?.ownerId?.trim() ?: ""
        val joinPhone = AppPreferenceHelper.normalizePhoneNumber(joinPhoneState)
        val isAdmin = adminRole != "GUEST"

        val provId = provider?.id?.trim() ?: ""
        val storeId = store?.id?.trim() ?: ""
        val propId = property?.id?.trim() ?: ""

        (uidClean.isNotEmpty() && (uidClean == provId || uidClean == storeId || uidClean == propId || uidClean == storeOwner || uidClean == propOwner)) ||
        (phoneClean.isNotEmpty() && (phoneClean == provPhone || phoneClean == storePhone || phoneClean == propPhone)) ||
        (joinPhone.isNotEmpty() && (joinPhone == provPhone || joinPhone == storePhone || joinPhone == propPhone))
    }

    val entityBookings = remember(bookings, entityId, entityPhone) {
        val cleanP = com.serviseyem.ui.helpers.AppPreferenceHelper.normalizePhoneNumber(entityPhone)
        bookings.filter { b ->
            b.providerId == entityId ||
            (cleanP.isNotEmpty() && com.serviseyem.ui.helpers.AppPreferenceHelper.normalizePhoneNumber(b.providerPhone) == cleanP)
        }
    }
    val calculatedBookingsCount = entityBookings.size
    val calculatedRevenue = entityBookings.filter { it.status == "COMPLETED" }.sumOf { it.totalAmount }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = entityName,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = entityType.labelAr,
                            fontSize = 11.sp,
                            color = entityType.badgeColor,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "رجوع",
                            tint = Color.White
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.toggleFavorite(entityId) }) {
                        Icon(
                            imageVector = if (isFav) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                            contentDescription = "المفضلة",
                            tint = if (isFav) Color(0xFFEF4444) else Color.White
                        )
                    }
                    if (entityPhone.isNotBlank()) {
                        IconButton(onClick = {
                            val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:$entityPhone"))
                            context.startActivity(intent)
                        }) {
                            Icon(imageVector = Icons.Default.Phone, contentDescription = "اتصال", tint = Color(0xFF10B981))
                        }
                    }
                    IconButton(onClick = {
                        val shareText = "دليل خدمات اليمن | $entityName\n${entityType.labelAr}\nالعنوان: $entityAddress\nالهاتف: $entityPhone"
                        val intent = Intent(Intent.ACTION_SEND).apply {
                            type = "text/plain"
                            putExtra(Intent.EXTRA_TEXT, shareText)
                        }
                        context.startActivity(Intent.createChooser(intent, "مشاركة الملف"))
                    }) {
                        Icon(imageVector = Icons.Default.Share, contentDescription = "مشاركة", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = themeColors.surface)
            )
        },
        bottomBar = {
            ProfileActions(
                entityId = entityId,
                entityName = entityName,
                entityPhone = entityPhone,
                entityType = entityType,
                isOwner = isOwner,
                viewModel = viewModel,
                themeColors = themeColors,
                onOpenChat = onOpenChat,
                onRequestBooking = onRequestBooking,
                onOrderProduct = onOrderProduct,
                onEditProfile = { viewModel.navigateTo("OWNER_PROFILE_VIEW") },
                onEditProducts = { viewModel.navigateTo("PRODUCTS_MGMT_VIEW") },
                onEditGallery = { viewModel.navigateTo("GALLERY_MGMT_VIEW") }
            )
        },
        containerColor = themeColors.background
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(12.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // 1. HERO BANNER & PROFILE HEADER
            item {
                ProfileHeader(
                    entityName = entityName,
                    entityType = entityType,
                    entityAddress = entityAddress,
                    ratingValue = ratingValue,
                    reviewsCount = reviewsCount,
                    profileCover = profileCover,
                    entityDescription = entityDescription,
                    isVerified = provider?.isVerified ?: store?.isVerified ?: property?.isVerified ?: true,
                    isVip = provider?.isVip ?: store?.isVip ?: property?.isVip ?: false,
                    isOwner = isOwner,
                    bookingsCount = calculatedBookingsCount,
                    completedRevenue = calculatedRevenue,
                    themeColors = themeColors,
                    onMapClick = { viewModel.navigateToScreen(AppScreens.MAP_VIEW) }
                )
            }

            // 2. PROMINENT DASHBOARD BUTTON FOR OWNER
            if (isOwner) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A)),
                        shape = RoundedCornerShape(14.dp),
                        border = BorderStroke(1.dp, themeColors.accent)
                    ) {
                        Column(
                            modifier = Modifier.padding(14.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "👑 مرحباً بك يا صاحب المنشأة / الفني!",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = themeColors.accent
                            )
                            Button(
                                onClick = {
                                    val destination = when (entityType) {
                                        ProfileEntityType.TECHNICIAN -> "TECHNICIAN_DASHBOARD"
                                        ProfileEntityType.STORE -> "STORE_DASHBOARD"
                                        ProfileEntityType.RESTAURANT -> "RESTAURANT_DASHBOARD"
                                        ProfileEntityType.MEDICAL -> "MEDICAL_DASHBOARD"
                                        ProfileEntityType.REAL_ESTATE -> "PROPERTY_DASHBOARD"
                                        ProfileEntityType.JOB -> "JOB_POSTER_DASHBOARD"
                                        else -> "TECHNICIAN_DASHBOARD"
                                    }
                                    viewModel.navigateTo(destination)
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = themeColors.accent),
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    text = "الدخول لوحة التحكم الرئيسية ⚙️",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.Black
                                )
                            }
                        }
                    }
                }
            }

            // 3. OWNER CONTROL BAR
            if (isOwner || adminRole != "GUEST") {
                item {
                    ProfileOwnerAdminControlBar(
                        entityId = entityId,
                        entityType = entityType,
                        provider = provider,
                        store = store,
                        property = property,
                        job = job,
                        isOwner = isOwner,
                        isAdmin = adminRole != "GUEST",
                        viewModel = viewModel,
                        themeColors = themeColors
                    )
                }
            }

            // 4. SPECS & DETAILS
            item {
                ProfileSpecs(
                    entityType = entityType,
                    provider = provider,
                    store = store,
                    property = property,
                    job = job,
                    products = products,
                    themeColors = themeColors
                )
            }

            // 5. TABS & CONTENT
            item {
                ProfileTabs(
                    selectedTab = selectedTab,
                    onTabSelected = { selectedTab = it },
                    entityType = entityType,
                    themeColors = themeColors
                )
            }

            item {
                ProfileTabContent(
                    selectedTab = selectedTab,
                    entityType = entityType,
                    provider = provider,
                    store = store,
                    property = property,
                    job = job,
                    products = products,
                    entityReviews = entityReviews,
                    entityDescription = entityDescription,
                    themeColors = themeColors,
                    isOwner = isOwner,
                    onAddReviewClick = { showRatingDialog = true }
                )
            }
        }
    }
}
