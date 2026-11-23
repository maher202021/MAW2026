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
import androidx.compose.runtime.saveable.rememberSaveable
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
    val providersList by viewModel.providers.collectAsState()
    val storesList by viewModel.stores.collectAsState()
    val propertiesList by viewModel.properties.collectAsState()
    val jobsList by viewModel.jobs.collectAsState()
    val products by viewModel.products.collectAsState()
    val ratings by viewModel.ratings.collectAsState()
    val bookings by viewModel.bookings.collectAsState()

    val currentUserId by viewModel.currentUserId.collectAsState()
    val currentUserName by viewModel.currentUserName.collectAsState()
    val currentUserPhone by viewModel.currentUserPhone.collectAsState()
    val adminRole by viewModel.adminRole.collectAsState()

    // Resolve live entity from reactive StateFlows so edits reflect immediately
    val liveProvider = remember(provider, providersList) {
        provider?.let { p -> providersList.find { it.id == p.id } ?: p }
    }
    val liveStore = remember(store, storesList) {
        store?.let { s -> storesList.find { it.id == s.id } ?: s }
    }
    val liveProperty = remember(property, propertiesList) {
        property?.let { pr -> propertiesList.find { it.id == pr.id } ?: pr }
    }
    val liveJob = remember(job, jobsList) {
        job?.let { j -> jobsList.find { it.id == j.id } ?: j }
    }

    // Determine Entity Type dynamically
    val entityType = remember(liveProvider, liveStore, liveProperty, liveJob, categories) {
        when {
            liveProvider != null -> {
                val catName = categories.find { it.id == liveProvider.categoryId }?.name?.lowercase() ?: ""
                when {
                    liveProvider.categoryId == "medical" || catName.contains("طب") || catName.contains("عياد") -> ProfileEntityType.MEDICAL
                    liveProvider.categoryId == "restaurants" || catName.contains("مطعم") || catName.contains("كافيه") -> ProfileEntityType.RESTAURANT
                    liveProvider.categoryId == "stores" || catName.contains("متجر") || catName.contains("سوق") -> ProfileEntityType.STORE
                    else -> ProfileEntityType.TECHNICIAN
                }
            }
            liveStore != null -> {
                when (liveStore.sectionId) {
                    "restaurants" -> ProfileEntityType.RESTAURANT
                    "medical" -> ProfileEntityType.MEDICAL
                    else -> ProfileEntityType.STORE
                }
            }
            liveProperty != null -> ProfileEntityType.REAL_ESTATE
            liveJob != null -> ProfileEntityType.JOB
            else -> ProfileEntityType.GENERAL
        }
    }

    val entityId = liveProvider?.id ?: liveStore?.id ?: liveProperty?.id ?: liveJob?.id ?: ""
    val collectionName = remember(liveProvider, liveStore, liveProperty, liveJob) {
        when {
            liveProvider != null -> "providers"
            liveStore != null -> "stores"
            liveProperty != null -> "properties"
            liveJob != null -> "jobs"
            else -> ""
        }
    }

    // Unify primary fields
    val entityName = liveProvider?.name ?: liveStore?.name ?: liveProperty?.title ?: liveJob?.title ?: "ملف النشاط"
    val entityPhone = liveProvider?.phone ?: liveStore?.phone ?: liveProperty?.phone ?: liveJob?.phone ?: ""
    val entityAddress = liveProvider?.localNeighborhood?.ifEmpty { liveProvider.area }
        ?: liveStore?.localNeighborhood?.ifEmpty { liveStore.cityId }
        ?: liveProperty?.localNeighborhood?.ifEmpty { liveProperty.cityId }
        ?: liveJob?.address?.ifEmpty { liveJob.cityId } ?: "اليمن"
    val workingHours = liveStore?.workingHours ?: ""
    val profileCover = liveProvider?.coverImage?.ifEmpty { liveProvider.profileImage }
        ?: liveStore?.coverImage?.ifEmpty { liveStore.logoImage }
        ?: liveProperty?.images?.firstOrNull() ?: ""
    val entityDescription = liveProvider?.profession?.ifEmpty { liveProvider.specialization }
        ?: liveStore?.description
        ?: liveProperty?.description
        ?: liveJob?.description ?: "لا يوجد وصف متاح."

    var selectedTab by remember { mutableIntStateOf(0) }
    var showRatingDialog by remember { mutableStateOf(false) }
    var showGuestForRatingDialog by remember { mutableStateOf(false) }

    val favoriteIds by viewModel.favoriteIds.collectAsState()
    val isFav = remember(favoriteIds, entityId) { favoriteIds.contains(entityId) }
    val entityReviews = remember(ratings, entityId) {
        ratings.filter { it.targetId == entityId }
    }
    val ratingValue = remember(entityReviews, liveProvider, liveStore, liveProperty) {
        if (entityReviews.isNotEmpty()) {
            (entityReviews.sumOf { it.rating.toDouble() } / entityReviews.size).toFloat()
        } else {
            liveProvider?.rating ?: liveStore?.rating ?: liveProperty?.rating ?: 5.0f
        }
    }
    val reviewsCount = remember(entityReviews, liveProvider, liveStore, liveProperty) {
        maxOf(entityReviews.size, liveProvider?.numReviews ?: liveStore?.numReviews ?: liveProperty?.numReviews ?: 0)
    }

    // Ownership logic: check if logged-in user is the owner
    val joinPhoneState by viewModel.joinRequestPhone.collectAsState()
    val isOwner = remember(currentUserId, currentUserPhone, liveProvider, liveStore, liveProperty, adminRole, joinPhoneState) {
        val phoneClean = AppPreferenceHelper.normalizePhoneNumber(currentUserPhone)
        val uidClean = currentUserId.trim()
        val provPhone = AppPreferenceHelper.normalizePhoneNumber(liveProvider?.phone ?: "")
        val storePhone = AppPreferenceHelper.normalizePhoneNumber(liveStore?.phone ?: "")
        val storeOwner = liveStore?.ownerId?.trim() ?: ""
        val propPhone = AppPreferenceHelper.normalizePhoneNumber(liveProperty?.phone ?: "")
        val propOwner = liveProperty?.ownerId?.trim() ?: ""
        val joinPhone = AppPreferenceHelper.normalizePhoneNumber(joinPhoneState)

        val provId = liveProvider?.id?.trim() ?: ""
        val storeId = liveStore?.id?.trim() ?: ""
        val propId = liveProperty?.id?.trim() ?: ""

        (uidClean.isNotEmpty() && uidClean != "guest" && (uidClean == provId || uidClean == storeId || uidClean == propId || uidClean == storeOwner || uidClean == propOwner)) ||
            (phoneClean.isNotEmpty() && (phoneClean == provPhone || phoneClean == storePhone || phoneClean == propPhone)) ||
            (joinPhone.isNotEmpty() && (joinPhone == provPhone || joinPhone == storePhone || joinPhone == propPhone))
    }

    // Analytics: increment view count once per non-owner profile visit
    LaunchedEffect(entityId, collectionName, isOwner) {
        if (entityId.isNotBlank() && collectionName.isNotBlank() && !isOwner) {
            viewModel.incrementEntityViews(collectionName, entityId)
        }
    }

    var previewAsCustomer by rememberSaveable { mutableStateOf(false) }
    // Load live Special Offers, Doctors (for Medical), and Gallery Albums (for Technician/RealEstate)
    var remoteOffers by remember(entityId) { mutableStateOf<List<SpecialOfferEntity>>(emptyList()) }
    LaunchedEffect(entityId) {
        if (entityId.isNotBlank()) {
            val repo = com.serviseyem.data.repositories.OffersRepository(
                com.google.firebase.firestore.FirebaseFirestore.getInstance(),
                LocalAppCacheManager(context)
            )
            repo.getOffersForProvider(entityId).onSuccess { list ->
                remoteOffers = list.filter { it.isEnabled }
            }
        }
    }
    val mergedOffers = remember(remoteOffers, liveProvider, liveStore, entityId) {
        val inlineJson = liveProvider?.specialOffersJson ?: liveStore?.specialOffersJson ?: ""
        val inlineParsed = SpecialOfferEntity.parseList(inlineJson).filter {
            (it.providerId.isBlank() || it.providerId == entityId) && it.isEnabled
        }
        (remoteOffers + inlineParsed).distinctBy { it.id.ifBlank { it.title } }
    }

    val doctorsList by remember(entityId, entityType) {
        if (entityId.isNotBlank() && entityType == ProfileEntityType.MEDICAL) {
            com.serviseyem.data.repositories.MedicalRepository(
                com.google.firebase.firestore.FirebaseFirestore.getInstance(),
                LocalAppCacheManager(context)
            ).getDoctors(entityId)
        } else {
            kotlinx.coroutines.flow.flowOf(emptyList())
        }
    }.collectAsState(initial = emptyList())

    val galleryAlbums by remember(entityId, entityType) {
        if (entityId.isNotBlank() && (entityType == ProfileEntityType.TECHNICIAN || entityType == ProfileEntityType.REAL_ESTATE)) {
            com.serviseyem.data.repositories.GalleryRepositoryImpl(context).getOwnerGallery(entityId)
        } else {
            kotlinx.coroutines.flow.flowOf(emptyList())
        }
    }.collectAsState(initial = emptyList())

    val entityBookings = remember(bookings, entityId, entityPhone) {
        val cleanP = AppPreferenceHelper.normalizePhoneNumber(entityPhone)
        bookings.filter { b ->
            b.providerId == entityId ||
                (cleanP.isNotEmpty() && AppPreferenceHelper.normalizePhoneNumber(b.providerPhone) == cleanP)
        }
    }
    val calculatedBookingsCount = entityBookings.size
    val calculatedRevenue = entityBookings.filter { it.status == "COMPLETED" }.sumOf { it.totalAmount }

    val dashboardRoute = remember(entityType) {
        when (entityType) {
            ProfileEntityType.TECHNICIAN -> AppScreens.TECHNICIAN_DASHBOARD
            ProfileEntityType.STORE -> AppScreens.STORE_DASHBOARD
            ProfileEntityType.RESTAURANT -> AppScreens.RESTAURANT_DASHBOARD
            ProfileEntityType.MEDICAL -> AppScreens.MEDICAL_DASHBOARD
            ProfileEntityType.REAL_ESTATE -> AppScreens.PROPERTY_DASHBOARD
            ProfileEntityType.JOB -> AppScreens.JOB_POSTER_DASHBOARD
            else -> AppScreens.TECHNICIAN_DASHBOARD
        }
    }

    val navigateToDashboardTab: (Int) -> Unit = { tabIdx ->
        viewModel.selectedProvider = liveProvider
        viewModel.selectedStore = liveStore
        viewModel.selectedProperty = liveProperty
        viewModel.selectedJob = liveJob
        viewModel.selectedDashboardInitialTab = tabIdx
        viewModel.navigateTo(dashboardRoute)
    }

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
                    if (isOwner || adminRole != "GUEST") {
                        AssistChip(
                            onClick = { previewAsCustomer = !previewAsCustomer },
                            label = {
                                Text(
                                    if (previewAsCustomer) "إنهاء المعاينة ✕" else "معاينة كعميل 👁️",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            },
                            colors = AssistChipDefaults.assistChipColors(
                                containerColor = if (previewAsCustomer) Color(0xFFEF4444) else themeColors.accent.copy(alpha = 0.25f),
                                labelColor = Color.White
                            )
                        )
                    }
                    IconButton(onClick = {
                        val lat = liveStore?.latitude ?: liveProvider?.latitude ?: liveProperty?.latitude ?: 15.3547
                        val lng = liveStore?.longitude ?: liveProvider?.longitude ?: liveProperty?.longitude ?: 44.2066
                        viewModel.openNavigation(lat, lng, entityName)
                    }) {
                        Icon(imageVector = Icons.Default.Navigation, contentDescription = "الملاحة للوجهة", tint = Color(0xFF00E5FF))
                    }
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
                isOwner = isOwner && !previewAsCustomer,
                viewModel = viewModel,
                themeColors = themeColors,
                onOpenChat = onOpenChat,
                onRequestBooking = onRequestBooking,
                onOrderProduct = onOrderProduct,
                onEditProfile = {
                    val profileTab = when (entityType) {
                        ProfileEntityType.TECHNICIAN -> 5
                        ProfileEntityType.REAL_ESTATE -> 3
                        ProfileEntityType.JOB -> 0
                        else -> 4
                    }
                    navigateToDashboardTab(profileTab)
                },
                onEditProducts = {
                    val itemsTab = when (entityType) {
                        ProfileEntityType.TECHNICIAN, ProfileEntityType.MEDICAL -> 2
                        else -> 0
                    }
                    navigateToDashboardTab(itemsTab)
                },
                onEditGallery = {
                    val galleryTab = if (entityType == ProfileEntityType.TECHNICIAN) 3 else 0
                    navigateToDashboardTab(galleryTab)
                }
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
                val promoText = mergedOffers.firstOrNull()?.let { offer ->
                    val prefix = if (offer.discountPercent > 0) "خصم ${offer.discountPercent}%: " else "عرض خاص: "
                    prefix + offer.title.ifBlank { offer.description }
                }
                ProfileHeader(
                    entityName = entityName,
                    entityType = entityType,
                    entityAddress = entityAddress,
                    ratingValue = ratingValue,
                    reviewsCount = reviewsCount,
                    profileCover = profileCover,
                    entityDescription = entityDescription,
                    isVerified = liveProvider?.isVerified ?: liveStore?.isVerified ?: liveProperty?.isVerified ?: true,
                    isVip = liveProvider?.isVip ?: liveStore?.isVip ?: liveProperty?.isVip ?: false,
                    isOwner = isOwner,
                    bookingsCount = calculatedBookingsCount,
                    completedRevenue = calculatedRevenue,
                    themeColors = themeColors,
                    workingHours = workingHours,
                    promoBannerText = promoText,
                    onMapClick = { viewModel.navigateToScreen(AppScreens.MAP_VIEW) }
                )
            }

            // 2. PROMINENT DASHBOARD BUTTON FOR OWNER
            if (isOwner && !previewAsCustomer) {
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
                                onClick = { navigateToDashboardTab(0) },
                                colors = ButtonDefaults.buttonColors(containerColor = themeColors.accent),
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp)
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
            if ((isOwner || adminRole != "GUEST") && !previewAsCustomer) {
                item {
                    ProfileOwnerAdminControlBar(
                        entityId = entityId,
                        entityType = entityType,
                        provider = liveProvider,
                        store = liveStore,
                        property = liveProperty,
                        job = liveJob,
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
                    provider = liveProvider,
                    store = liveStore,
                    property = liveProperty,
                    job = liveJob,
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
                    provider = liveProvider,
                    store = liveStore,
                    property = liveProperty,
                    job = liveJob,
                    products = products,
                    entityReviews = entityReviews,
                    entityDescription = entityDescription,
                    themeColors = themeColors,
                    isOwner = isOwner,
                    onAddReviewClick = {
                        if (currentUserId.isBlank() || currentUserId == "guest") {
                            showGuestForRatingDialog = true
                        } else {
                            showRatingDialog = true
                        }
                    },
                    viewModel = viewModel,
                    offers = mergedOffers,
                    doctors = doctorsList,
                    galleryAlbums = galleryAlbums,
                    onSwitchTab = { selectedTab = it },
                    onRequestBooking = onRequestBooking
                )
            }
        }
    }

    if (showGuestForRatingDialog) {
        com.serviseyem.ui.screens.register.GuestRegistrationDialog(
            viewModel = viewModel,
            themeColors = themeColors,
            onDismiss = { showGuestForRatingDialog = false },
            onRegisterCompleted = { _, _, _, _ ->
                showGuestForRatingDialog = false
                showRatingDialog = true
            }
        )
    }

    if (showRatingDialog) {
        var starRating by remember { mutableIntStateOf(5) }
        var commentText by remember { mutableStateOf("") }
        AlertDialog(
            onDismissRequest = { showRatingDialog = false },
            containerColor = themeColors.surface,
            title = { Text("⭐ تقييم $entityName", color = themeColors.accent, fontWeight = FontWeight.Bold, fontSize = 15.sp) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center
                    ) {
                        (1..5).forEach { star ->
                            IconButton(onClick = { starRating = star }, modifier = Modifier.size(48.dp)) {
                                Icon(
                                    imageVector = Icons.Default.Star,
                                    contentDescription = "تقييم $star نجوم",
                                    tint = if (star <= starRating) Color(0xFFFFB400) else Color.Gray,
                                    modifier = Modifier.size(30.dp)
                                )
                            }
                        }
                    }
                    OutlinedTextField(
                        value = commentText,
                        onValueChange = { commentText = it },
                        label = { Text("اكتب تجربتك ورأيك بصراحة") },
                        minLines = 3,
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White)
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val cleanComment = commentText.trim()
                        if (cleanComment.isBlank()) {
                            Toast.makeText(context, "يرجى كتابة تعليق مع التقييم", Toast.LENGTH_SHORT).show()
                        } else {
                            val review = RatingEntity(
                                id = com.serviseyem.utils.EntityIdGenerator.generateReviewId(),
                                targetId = entityId,
                                targetType = when (entityType) {
                                    ProfileEntityType.TECHNICIAN -> "PROVIDER"
                                    ProfileEntityType.RESTAURANT -> "RESTAURANT"
                                    ProfileEntityType.MEDICAL -> "MEDICAL"
                                    ProfileEntityType.REAL_ESTATE -> "PROPERTY"
                                    else -> "STORE"
                                },
                                userId = currentUserId,
                                userName = currentUserName.ifBlank { "عميل معتمد" },
                                userPhone = currentUserPhone,
                                rating = starRating.toFloat(),
                                comment = cleanComment,
                                isApproved = true,
                                timestamp = System.currentTimeMillis()
                            )
                            viewModel.submitRating(review) {
                                Toast.makeText(context, "⭐ شكراً! تم نشر تقييمك بنجاح", Toast.LENGTH_SHORT).show()
                            }
                            showRatingDialog = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFB400))
                ) {
                    Text("إرسال التقييم", color = Color.Black, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showRatingDialog = false }) {
                    Text("إلغاء", color = Color.White)
                }
            }
        )
    }
}
