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
import com.serviseyem.ui.screens.dashboard.viewmodels.RestaurantDashboardViewModel
import com.serviseyem.utils.VisualThemePalette

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RestaurantDashboard(
    account: UnifiedBusinessAccount,
    viewModel: MainViewModel,
    themeColors: VisualThemePalette,
    onBackClick: () -> Unit
) {
    val context = LocalContext.current
    val appContext = context.applicationContext
    var activeTab by remember { mutableIntStateOf(0) }

    val restaurantViewModel = remember(account.id, appContext) {
        RestaurantDashboardViewModel(
            ownerId = account.id,
            dashboardRepository = DashboardRepositoryImpl(appContext),
            productsRepository = ProductsRepositoryImpl(appContext),
            ratingsRepository = RatingsRepositoryImpl(appContext)
        )
    }

    val uiState by restaurantViewModel.uiState.collectAsState()
    val allBookings by viewModel.bookings.collectAsState()

    val restaurantOrders = remember(allBookings, account) {
        allBookings.filter { b -> b.providerId == account.id || b.providerPhone == account.phone }
    }

    val tabs = listOf(
        "قائمة الطعام 🍽️",
        "طلبات العملاء 🛵",
        "العروض الخاصة 🎁",
        "تقييمات العملاء ⭐",
        "الملف التعريفي 📝"
    )

    LaunchedEffect(restaurantViewModel) {
        restaurantViewModel.eventFlow.collect { event ->
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
                        Text(text = "لوحة تحكم المطعم 🍽️", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = themeColors.textPrimary)
                        Text(text = account.name, fontSize = 11.sp, color = themeColors.textSecondary)
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = themeColors.textPrimary)
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
                            titleLabel = "منيو وقائمة الوجبات",
                            addButtonLabel = "إضافة وجبة جديدة 🍽️",
                            themeColors = themeColors,
                            onAddProduct = { title, price, desc, img ->
                                restaurantViewModel.addMeal(title, price, desc, img)
                            },
                            onDeleteProduct = { id ->
                                restaurantViewModel.deleteMeal(id)
                            }
                        )
                    }
                    1 -> {
                        TabBookingsOrders(
                            bookings = restaurantOrders,
                            themeColors = themeColors,
                            onAcceptBooking = { bId -> viewModel.updateBookingStatus(bId, "IN_PREPARATION") },
                            onRejectBooking = { bId, reason -> viewModel.updateBookingStatus(bId, "REJECTED") },
                            onStartProgress = { bId -> viewModel.updateBookingStatus(bId, "READY") },
                            onCompleteBooking = { bId -> viewModel.updateBookingStatus(bId, "DELIVERED") },
                            onChatWithClient = { phone ->
                                viewModel.openOrCreateChatChannel(
                                    targetId = account.id,
                                    targetType = "RESTAURANT",
                                    targetName = account.name,
                                    targetPhone = phone,
                                    onCreated = {}
                                )
                            },
                            onUpdateOrderStatus = { bId, status ->
                                viewModel.updateBookingStatus(bId, status)
                                Toast.makeText(context, "تم تحديث حالة وجبة العميل ✅", Toast.LENGTH_SHORT).show()
                            }
                        )
                    }
                    2 -> {
                        TabOffersCoupons(themeColors = themeColors)
                    }
                    3 -> {
                        TabReviewsFeedback(
                            reviews = uiState.reviews,
                            themeColors = themeColors,
                            onReplySubmit = { revId, reply ->
                                Toast.makeText(context, "تم الرد على تقييم المطعم بنجاح ✅", Toast.LENGTH_SHORT).show()
                            }
                        )
                    }
                    4 -> {
                        TabProfileEdit(
                            name = account.name,
                            phone = account.phone,
                            cityArea = account.neighborhood.ifBlank { account.cityId },
                            description = account.description,
                            workingHours = account.workingHours,
                            photoUrl = account.logoImage,
                            coverUrl = account.coverImage,
                            isAvailable = true,
                            rating = account.rating.toDouble(),
                            reviewCount = account.numReviews,
                            themeColors = themeColors,
                            onSaveProfile = { name, phone, city, desc, hours, available ->
                                Toast.makeText(context, "تم تحديث بيانات المطعم بنجاح ✅", Toast.LENGTH_SHORT).show()
                            },
                            onChangePassword = { oldP, newP ->
                                Toast.makeText(context, "تم تغيير كلمة المرور بنجاح 🔑", Toast.LENGTH_SHORT).show()
                            }
                        )
                    }
                }
            }
        }
    }
}
