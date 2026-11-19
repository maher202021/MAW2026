package com.serviseyem.ui.screens.dashboard

import android.widget.Toast
import com.serviseyem.ui.*
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.serviseyem.data.UnifiedBusinessAccount
import com.serviseyem.data.repositories.*
import com.serviseyem.domain.entities.GalleryAlbumEntity
import com.serviseyem.ui.MainViewModel
import com.serviseyem.ui.screens.dashboard.components.UnifiedLoadingIndicator
import com.serviseyem.ui.screens.dashboard.viewmodels.TechnicianDashboardViewModel
import com.serviseyem.utils.VisualThemePalette

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TechnicianDashboard(
    account: UnifiedBusinessAccount,
    viewModel: MainViewModel,
    themeColors: VisualThemePalette,
    onBackClick: () -> Unit
) {
    val context = LocalContext.current
    val appContext = context.applicationContext
    var activeTab by remember { mutableIntStateOf(0) }

    val techViewModel = remember(account.id, appContext) {
        TechnicianDashboardViewModel(
            ownerId = account.id,
            dashboardRepository = DashboardRepositoryImpl(appContext),
            productsRepository = ProductsRepositoryImpl(appContext),
            ratingsRepository = RatingsRepositoryImpl(appContext),
            galleryRepository = GalleryRepositoryImpl(appContext)
        )
    }

    DisposableEffect(techViewModel) {
        onDispose { techViewModel.dispose() }
    }

    val uiState by techViewModel.uiState.collectAsState()
    val allBookings by viewModel.bookings.collectAsState()

    val providerBookings = remember(allBookings, account.id, account.phone) {
        allBookings.filter { b ->
            b.providerId == account.id || (account.phone.isNotBlank() && b.providerPhone == account.phone)
        }
    }

    val liveStats = remember(uiState.stats, providerBookings, uiState.products, uiState.reviews, account.rating, account.numReviews) {
        val activeCount = providerBookings.count {
            it.status in listOf(
                com.serviseyem.utils.BookingStatus.PENDING.code,
                com.serviseyem.utils.BookingStatus.ACCEPTED.code,
                "APPROVED",
                com.serviseyem.utils.BookingStatus.IN_PROGRESS.code
            )
        }
        val completedCount = providerBookings.count {
            it.status in listOf(
                com.serviseyem.utils.BookingStatus.COMPLETED.code,
                com.serviseyem.utils.BookingStatus.PAID.code,
                com.serviseyem.utils.BookingStatus.CLOSED.code
            )
        }
        val revenue = providerBookings.filter {
            it.status in listOf(
                com.serviseyem.utils.BookingStatus.COMPLETED.code,
                com.serviseyem.utils.BookingStatus.PAID.code,
                com.serviseyem.utils.BookingStatus.CLOSED.code
            )
        }.sumOf { it.totalAmount }
        val avgRating = if (uiState.reviews.isNotEmpty()) uiState.reviews.map { it.rating }.average() else account.rating.toDouble()
        uiState.stats.copy(
            activeBookingsCount = maxOf(uiState.stats.activeBookingsCount, activeCount),
            completedBookingsCount = maxOf(uiState.stats.completedBookingsCount, completedCount),
            totalRevenueYer = maxOf(uiState.stats.totalRevenueYer, revenue),
            averageRating = avgRating,
            totalReviewsCount = maxOf(uiState.reviews.size, account.numReviews),
            totalProductsCount = uiState.products.size
        )
    }

    val tabs = listOf(
        "طلبات الحجز 📅",
        "حجوزاتي الحالية 📋",
        "خدماتي وأسعاري 🛠️",
        "معرض الأعمال 📸",
        "التقييمات والآراء ⭐",
        "الملف الشخصي 👤",
        "الإحصائيات 📊"
    )

    LaunchedEffect(Unit) {
        val requestedTab = viewModel.selectedDashboardInitialTab
        if (requestedTab in tabs.indices) {
            activeTab = requestedTab
            viewModel.selectedDashboardInitialTab = 0
        }
    }

    LaunchedEffect(techViewModel) {
        techViewModel.eventFlow.collect { event ->
            when (event) {
                is DashboardEvent.ShowToast -> Toast.makeText(context, event.message, Toast.LENGTH_SHORT).show()
                else -> {}
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(text = "لوحة تحكم الفني 🛠️", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = themeColors.textPrimary)
                        Text(text = account.name, fontSize = 11.sp, color = themeColors.textSecondary)
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = themeColors.textPrimary)
                    }
                },
                actions = {
                    TextButton(
                        onClick = {
                            viewModel.selectedProvider = account.rawProvider ?: viewModel.providers.value.find { it.id == account.id }
                            viewModel.selectedStore = null
                            viewModel.selectedProperty = null
                            viewModel.selectedJob = null
                            viewModel.navigateToScreen(AppScreens.DYNAMIC_PROFILE)
                        },
                        modifier = Modifier.heightIn(min = 48.dp)
                    ) {
                        Text("معاينة 👁️", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = themeColors.accent)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = themeColors.surface)
            )
        },
        containerColor = themeColors.background
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            ScrollableTabRow(
                selectedTabIndex = activeTab,
                containerColor = themeColors.surface,
                contentColor = themeColors.accent,
                edgePadding = 12.dp
            ) {
                tabs.forEachIndexed { index, label ->
                    Tab(
                        selected = activeTab == index,
                        onClick = { activeTab = index },
                        text = {
                            Text(
                                text = label,
                                fontSize = 12.sp,
                                fontWeight = if (activeTab == index) FontWeight.Bold else FontWeight.Normal,
                                color = if (activeTab == index) themeColors.accent else themeColors.textSecondary
                            )
                        }
                    )
                }
            }

            if (uiState.isLoading) {
                UnifiedLoadingIndicator(themeColors = themeColors)
            } else {
                when (activeTab) {
                    0 -> {
                        TabBookingsOrders(
                            bookings = providerBookings.filter { it.status == com.serviseyem.utils.BookingStatus.PENDING.code },
                            themeColors = themeColors,
                            onAcceptBooking = { bId ->
                                viewModel.updateBookingStatus(bId, com.serviseyem.utils.BookingStatus.ACCEPTED.code)
                                Toast.makeText(context, "تم قبول طلب الحجز وإشعار العميل ✅", Toast.LENGTH_SHORT).show()
                            },
                            onRejectBooking = { bId, reason ->
                                viewModel.updateBookingStatus(bId, com.serviseyem.utils.BookingStatus.REJECTED.code, reason)
                                Toast.makeText(context, "تم رفض الطلب وإرسال السبب للعميل ❌", Toast.LENGTH_SHORT).show()
                            },
                            onStartProgress = { bId -> viewModel.updateBookingStatus(bId, com.serviseyem.utils.BookingStatus.IN_PROGRESS.code) },
                            onCompleteBooking = { bId -> viewModel.updateBookingStatus(bId, com.serviseyem.utils.BookingStatus.COMPLETED.code) },
                            onChatWithClient = { phone ->
                                viewModel.openOrCreateChatChannel(
                                    targetId = account.id,
                                    targetType = "PROVIDER",
                                    targetName = account.name,
                                    targetPhone = phone,
                                    onCreated = { channel ->
                                        channel?.id?.let { chId ->
                                            viewModel.targetChatChannelId = chId
                                        }
                                        viewModel.navigateToScreen(AppScreens.CHAT_DIRECT)
                                    }
                                )
                            }
                        )
                    }
                    1 -> {
                        TabBookingsOrders(
                            bookings = providerBookings.filter {
                                it.status in listOf(
                                    com.serviseyem.utils.BookingStatus.ACCEPTED.code,
                                    "APPROVED",
                                    com.serviseyem.utils.BookingStatus.IN_PROGRESS.code
                                )
                            },
                            themeColors = themeColors,
                            onAcceptBooking = { bId -> viewModel.updateBookingStatus(bId, com.serviseyem.utils.BookingStatus.ACCEPTED.code) },
                            onRejectBooking = { bId, reason -> viewModel.updateBookingStatus(bId, com.serviseyem.utils.BookingStatus.REJECTED.code, reason) },
                            onStartProgress = { bId ->
                                viewModel.updateBookingStatus(bId, com.serviseyem.utils.BookingStatus.IN_PROGRESS.code)
                                Toast.makeText(context, "تم بدء تنفيذ الخدمة ⚙️", Toast.LENGTH_SHORT).show()
                            },
                            onCompleteBooking = { bId ->
                                viewModel.updateBookingStatus(bId, com.serviseyem.utils.BookingStatus.COMPLETED.code)
                                Toast.makeText(context, "تم إكمال الخدمة بنجاح 🏁", Toast.LENGTH_SHORT).show()
                            },
                            onChatWithClient = { phone ->
                                viewModel.openOrCreateChatChannel(
                                    targetId = account.id,
                                    targetType = "PROVIDER",
                                    targetName = account.name,
                                    targetPhone = phone,
                                    onCreated = { channel ->
                                        channel?.id?.let { chId ->
                                            viewModel.targetChatChannelId = chId
                                        }
                                        viewModel.navigateToScreen(AppScreens.CHAT_DIRECT)
                                    }
                                )
                            }
                        )
                    }
                    2 -> {
                        TabProductsServices(
                            products = uiState.products,
                            titleLabel = "قائمة الخدمات والأسعار",
                            addButtonLabel = "إضافة خدمة جديدة 🛠️",
                            themeColors = themeColors,
                            onAddProduct = { title, price, desc, img ->
                                techViewModel.addNewProductService(title, price, desc, img)
                            },
                            onDeleteProduct = { id ->
                                techViewModel.deleteProduct(id)
                            },
                            onEditProduct = { updated ->
                                techViewModel.updateProduct(updated)
                            },
                            onToggleAvailability = { item ->
                                techViewModel.toggleProductAvailability(item)
                            }
                        )
                    }
                    3 -> {
                        TabGalleryAlbums(
                            albums = uiState.galleryAlbums,
                            themeColors = themeColors,
                            onAddPhoto = { img ->
                                techViewModel.saveGalleryAlbum(context, img)
                            },
                            onDeletePhoto = { id ->
                                techViewModel.deleteGalleryAlbum(id)
                            }
                        )
                    }
                    4 -> {
                        TabReviewsFeedback(
                            reviews = uiState.reviews,
                            themeColors = themeColors,
                            onReplySubmit = { revId, reply ->
                                techViewModel.replyToReview(revId, reply)
                            }
                        )
                    }
                    5 -> {
                        val baseProv = account.rawProvider ?: viewModel.providers.value.find { it.id == account.id }
                        TabProfileEdit(
                            name = account.name,
                            phone = account.phone,
                            cityArea = account.neighborhood.ifBlank { account.cityId },
                            description = account.description,
                            workingHours = account.workingHours,
                            photoUrl = account.logoImage,
                            coverUrl = account.coverImage,
                            isAvailable = baseProv?.isAvailable ?: account.isActive,
                            rating = liveStats.averageRating,
                            reviewCount = liveStats.totalReviewsCount,
                            themeColors = themeColors,
                            onChangePhoto = { newUrl ->
                                viewModel.updateEntityImages("providers", account.id, profileImg = newUrl, coverImg = "")
                            },
                            onChangeCover = { newUrl ->
                                viewModel.updateEntityImages("providers", account.id, profileImg = "", coverImg = newUrl)
                            },
                            onSaveProfile = { name, _, city, desc, _, available ->
                                val currentProv = baseProv ?: com.serviseyem.data.ProviderEntity(
                                    id = account.id,
                                    name = account.name,
                                    phone = account.phone,
                                    cityId = account.cityId
                                )
                                viewModel.updateProviderEntity(
                                    currentProv.copy(
                                        name = name,
                                        localNeighborhood = city,
                                        area = city,
                                        profession = desc,
                                        specialization = desc,
                                        isAvailable = available
                                    )
                                )
                            },
                            onChangePassword = { oldP, newP ->
                                viewModel.changeEntityPassword("providers", account.id, oldP, newP) { _, _ -> }
                            }
                        )
                    }
                    6 -> {
                        TabStatisticsGrowth(
                            stats = liveStats,
                            themeColors = themeColors
                        )
                    }
                }
            }
        }
    }
}
