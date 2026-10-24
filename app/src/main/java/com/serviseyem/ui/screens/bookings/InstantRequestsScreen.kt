package com.serviseyem.ui.screens.bookings

import android.widget.Toast
import com.serviseyem.ui.*
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.serviseyem.ui.MainViewModel
import com.serviseyem.utils.VisualThemePalette

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InstantRequestsScreen(
    viewModel: MainViewModel,
    themeColors: VisualThemePalette,
    onBackClick: () -> Unit = {}
) {
    val instantReqViewModel: InstantRequestScreenViewModel = hiltViewModel()
    val uiState by instantReqViewModel.uiState.collectAsState()

    val instantRequests by viewModel.instantRequests.collectAsState()
    val requestOffers by viewModel.requestOffers.collectAsState()
    
    val currentUserPhone by viewModel.currentUserPhone.collectAsState()
    val currentUserId by viewModel.currentUserId.collectAsState()
    val adminRole by viewModel.adminRole.collectAsState()

    val filteredList = remember(instantRequests, uiState.selectedTab, uiState.searchQuery, currentUserPhone, currentUserId) {
        instantRequests.filter { req ->
            val matchesTab = if (uiState.selectedTab == 0) {
                currentUserPhone.isBlank() || req.userPhone == currentUserPhone || req.userId == currentUserId || adminRole.isNotBlank()
            } else {
                req.status == "WAITING_FOR_OFFERS" || req.status == "REVIEWING_OFFERS"
            }
            val matchesQuery = uiState.searchQuery.isBlank() ||
                    req.requestCode.contains(uiState.searchQuery, ignoreCase = true) ||
                    req.serviceTitle.contains(uiState.searchQuery, ignoreCase = true) ||
                    req.userCity.contains(uiState.searchQuery, ignoreCase = true)
            matchesTab && matchesQuery
        }.sortedByDescending { it.createdAt }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("🚨 اطلب خدمتك الآن (مزايدة فورية)", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = themeColors.textPrimary)
                        Text("عروض أسعار تنافسية ومباشرة خلال ساعتين ⏱️", fontSize = 11.sp, color = themeColors.accent)
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "رجوع", tint = themeColors.textPrimary)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = themeColors.surface)
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { instantReqViewModel.setShowCreateDialog(true) },
                containerColor = themeColors.accent,
                contentColor = Color.Black,
                icon = { Icon(Icons.Default.Add, contentDescription = null, tint = Color.Black) },
                text = { Text("اطلب خدمتك الآن ⚡", fontWeight = FontWeight.Bold, color = Color.Black) }
            )
        },
        containerColor = themeColors.background
    ) { paddingValues ->
        Column(modifier = Modifier.fillMaxSize().padding(paddingValues)) {
            TabRow(
                selectedTabIndex = uiState.selectedTab,
                containerColor = themeColors.surface,
                contentColor = themeColors.accent
            ) {
                Tab(
                    selected = uiState.selectedTab == 0,
                    onClick = { instantReqViewModel.setTab(0) },
                    text = { Text("طلباتي السابقة 📑", fontSize = 12.sp, color = if (uiState.selectedTab == 0) themeColors.accent else themeColors.textSecondary) }
                )
                Tab(
                    selected = uiState.selectedTab == 1,
                    onClick = { instantReqViewModel.setTab(1) },
                    text = { Text("سوق الطلبات المفتوحة (للفنيين) 🛠️", fontSize = 12.sp, color = if (uiState.selectedTab == 1) themeColors.accent else themeColors.textSecondary) }
                )
            }
            
            OutlinedTextField(
                value = uiState.searchQuery,
                onValueChange = { instantReqViewModel.setSearchQuery(it) },
                placeholder = { Text("بحث بالرقم أو الخدمة أو المدينة...", fontSize = 12.sp, color = themeColors.textSecondary) },
                modifier = Modifier.fillMaxWidth().padding(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = themeColors.accent,
                    unfocusedBorderColor = themeColors.border,
                    focusedTextColor = themeColors.textPrimary,
                    unfocusedTextColor = themeColors.textPrimary
                )
            )

            InstantRequestList(
                requests = filteredList,
                offers = requestOffers,
                currentUserId = currentUserId,
                themeColors = themeColors,
                onCreateRequestClick = { instantReqViewModel.setShowCreateDialog(true) },
                onRetryClick = { viewModel.refreshData() },
                onViewOffers = { instantReqViewModel.setSelectedRequestForOffers(it) },
                onSubmitOffer = { instantReqViewModel.setSelectedRequestForSubmitOffer(it) }
            )
        }
    }

    if (uiState.showCreateDialog) {
        CreateInstantRequestDialog(
            viewModel = viewModel,
            themeColors = themeColors,
            onDismiss = { instantReqViewModel.setShowCreateDialog(false) }
        )
    }

    uiState.selectedRequestForOffers?.let { req ->
        val reqOffers = requestOffers.filter { it.requestId == req.id }
        ReviewOffersDialog(
            request = req,
            offers = reqOffers,
            viewModel = viewModel,
            themeColors = themeColors,
            onDismiss = { instantReqViewModel.setSelectedRequestForOffers(null) },
            onAcceptOffer = { selectedOffer ->
                viewModel.acceptRequestOffer(req, selectedOffer)
                instantReqViewModel.setSelectedRequestForOffers(null)
            }
        )
    }

    uiState.selectedRequestForSubmitOffer?.let { req ->
        SubmitOfferDialog(
            request = req,
            viewModel = viewModel,
            themeColors = themeColors,
            onDismiss = { instantReqViewModel.setSelectedRequestForSubmitOffer(null) }
        )
    }
}
