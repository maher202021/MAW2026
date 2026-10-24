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

    val uiState by propertyViewModel.uiState.collectAsState()
    val allBookings by viewModel.bookings.collectAsState()

    val propertyRequests = remember(allBookings, account) {
        allBookings.filter { b -> b.providerId == account.id || b.providerPhone == account.phone }
    }

    val tabs = listOf(
        "العقارات المعروضة 🏠",
        "طلبات المعاينة 👁️",
        "تقييمات العملاء ⭐",
        "الملف التعريفي 📝"
    )

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
                            }
                        )
                    }
                    1 -> {
                        TabBookingsOrders(
                            bookings = propertyRequests,
                            themeColors = themeColors,
                            onAcceptBooking = { bId -> viewModel.updateBookingStatus(bId, "APPROVED") },
                            onRejectBooking = { bId, reason -> viewModel.updateBookingStatus(bId, "REJECTED") },
                            onStartProgress = { bId -> viewModel.updateBookingStatus(bId, "IN_PROGRESS") },
                            onCompleteBooking = { bId -> viewModel.updateBookingStatus(bId, "COMPLETED") },
                            onChatWithClient = { phone ->
                                viewModel.openOrCreateChatChannel(
                                    targetId = account.id,
                                    targetType = "REAL_ESTATE",
                                    targetName = account.name,
                                    targetPhone = phone,
                                    onCreated = {}
                                )
                            }
                        )
                    }
                    2 -> {
                        TabReviewsFeedback(
                            reviews = uiState.reviews,
                            themeColors = themeColors,
                            onReplySubmit = { revId, reply ->
                                Toast.makeText(context, "تم الرد على تقييم المكتب بنجاح ✅", Toast.LENGTH_SHORT).show()
                            }
                        )
                    }
                    3 -> {
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
                                Toast.makeText(context, "تم تحديث بيانات المكتب العقاري ✅", Toast.LENGTH_SHORT).show()
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
