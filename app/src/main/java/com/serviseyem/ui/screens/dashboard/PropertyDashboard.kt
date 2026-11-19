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
import com.serviseyem.ui.MainViewModel
import com.serviseyem.ui.screens.dashboard.components.UnifiedLoadingIndicator
import com.serviseyem.ui.screens.dashboard.viewmodels.PropertyDashboardViewModel
import com.serviseyem.utils.VisualThemePalette

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PropertyDashboard(
    account: UnifiedBusinessAccount,
    viewModel: MainViewModel,
    themeColors: VisualThemePalette,
    onBackClick: () -> Unit
) {
    val context = LocalContext.current
    val appContext = context.applicationContext
    var activeTab by remember { mutableIntStateOf(0) }

    val propertyViewModel = remember(account.id, appContext) {
        PropertyDashboardViewModel(
            ownerId = account.id,
            dashboardRepository = DashboardRepositoryImpl(appContext),
            productsRepository = ProductsRepositoryImpl(appContext),
            ratingsRepository = RatingsRepositoryImpl(appContext)
        )
    }

    DisposableEffect(propertyViewModel) {
        onDispose { propertyViewModel.dispose() }
    }

    val uiState by propertyViewModel.uiState.collectAsState()
    val allBookings by viewModel.bookings.collectAsState()

    val propertyRequests = remember(allBookings, account.id, account.phone) {
        allBookings.filter { b ->
            b.providerId == account.id || (account.phone.isNotBlank() && b.providerPhone == account.phone)
        }
    }

    val liveStats = remember(uiState.stats, propertyRequests, uiState.products, uiState.reviews, account.rating, account.numReviews) {
        val activeCount = propertyRequests.count {
            it.status in listOf("PENDING", "ACCEPTED", "APPROVED", "IN_PROGRESS")
        }
        val completedCount = propertyRequests.count {
            it.status in listOf("COMPLETED", "PAID", "CLOSED")
        }
        val revenue = propertyRequests.filter {
            it.status in listOf("COMPLETED", "PAID", "CLOSED")
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
        "العقارات المعروضة 🏠",
        "طلبات المعاينة 👁️",
        "تقييمات العملاء ⭐",
        "الملف التعريفي 📝",
        "الإحصائيات 📊"
    )

    LaunchedEffect(Unit) {
        val requestedTab = viewModel.selectedDashboardInitialTab
        if (requestedTab in tabs.indices) {
            activeTab = requestedTab
            viewModel.selectedDashboardInitialTab = 0
        }
    }

    LaunchedEffect(propertyViewModel) {
        propertyViewModel.eventFlow.collect { event ->
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
                        Text(text = "لوحة تحكم العقارات 🏠", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = themeColors.textPrimary)
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
                            viewModel.selectedProperty = account.rawProperty ?: viewModel.properties.value.find { it.id == account.id }
                            viewModel.selectedProvider = null
                            viewModel.selectedStore = null
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
                        TabProductsServices(
                            products = uiState.products,
                            titleLabel = "قائمة العقارات المعروضة",
                            addButtonLabel = "إضافة عقار جديد 🏠",
                            themeColors = themeColors,
                            onAddProduct = { title, price, desc, img ->
                                propertyViewModel.addProperty(title, price, desc, img)
                            },
                            onDeleteProduct = { id ->
                                propertyViewModel.deleteProperty(id)
                            },
                            onEditProduct = { updated ->
                                propertyViewModel.updatePropertyItem(updated)
                            },
                            onToggleAvailability = { item ->
                                propertyViewModel.togglePropertyAvailability(item)
                            }
                        )
                    }
                    1 -> {
                        TabBookingsOrders(
                            bookings = propertyRequests,
                            themeColors = themeColors,
                            onAcceptBooking = { bId -> viewModel.updateBookingStatus(bId, "APPROVED") },
                            onRejectBooking = { bId, reason -> viewModel.updateBookingStatus(bId, "REJECTED", reason) },
                            onStartProgress = { bId -> viewModel.updateBookingStatus(bId, "IN_PROGRESS") },
                            onCompleteBooking = { bId -> viewModel.updateBookingStatus(bId, "COMPLETED") },
                            onChatWithClient = { phone ->
                                viewModel.openOrCreateChatChannel(
                                    targetId = account.id,
                                    targetType = "REAL_ESTATE",
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
                        TabReviewsFeedback(
                            reviews = uiState.reviews,
                            themeColors = themeColors,
                            onReplySubmit = { revId, reply ->
                                propertyViewModel.replyToReview(revId, reply)
                            }
                        )
                    }
                    3 -> {
                        val baseProperty = account.rawProperty ?: viewModel.properties.value.find { it.id == account.id }
                        TabProfileEdit(
                            name = account.name,
                            phone = account.phone,
                            cityArea = account.neighborhood.ifBlank { account.cityId },
                            description = account.description,
                            workingHours = account.workingHours,
                            photoUrl = account.logoImage,
                            coverUrl = account.coverImage,
                            isAvailable = baseProperty?.isActive ?: account.isActive,
                            rating = liveStats.averageRating,
                            reviewCount = liveStats.totalReviewsCount,
                            themeColors = themeColors,
                            onChangePhoto = { newUrl ->
                                viewModel.updateEntityImages("properties", account.id, profileImg = newUrl, coverImg = "")
                            },
                            onChangeCover = { newUrl ->
                                viewModel.updateEntityImages("properties", account.id, profileImg = "", coverImg = newUrl)
                            },
                            onSaveProfile = { name, _, city, desc, _, available ->
                                val currentProp = baseProperty ?: com.serviseyem.data.PropertyEntity(
                                    id = account.id,
                                    title = account.name,
                                    phone = account.phone,
                                    cityId = account.cityId
                                )
                                viewModel.updatePropertyEntity(
                                    currentProp.copy(
                                        title = name,
                                        localNeighborhood = city,
                                        description = desc,
                                        isActive = available
                                    )
                                )
                            },
                            onChangePassword = { oldP, newP ->
                                viewModel.changeEntityPassword("properties", account.id, oldP, newP) { _, _ -> }
                            }
                        )
                    }
                    4 -> {
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
