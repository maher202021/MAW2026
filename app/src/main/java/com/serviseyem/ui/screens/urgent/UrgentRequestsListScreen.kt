package com.serviseyem.ui.screens.urgent

import androidx.compose.foundation.layout.*
import com.serviseyem.ui.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.serviseyem.ui.MainViewModel
import com.serviseyem.utils.VisualThemePalette
import com.serviseyem.ui.viewmodels.InstantRequestViewModel

/**
 * ⚡ UrgentRequestsListScreen
 * Main host screen for 30-minute Urgent Requests List with live filter and modular content.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UrgentRequestsListScreen(
    viewModel: MainViewModel,
    instantViewModel: InstantRequestViewModel = viewModel(),
    themeColors: VisualThemePalette,
    onNavigateBack: () -> Unit = {},
    onNavigateToDetails: (requestId: String) -> Unit = {},
    onNavigateToNewUrgentRequest: () -> Unit = {},
    onNavigateToSubmitUrgentOffer: (requestId: String) -> Unit = {}
) {
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }

    val currentUserId by viewModel.currentUserId.collectAsState()
    val isProvider = viewModel.isProviderUser

    val requestsList by instantViewModel.instantRequests.collectAsState()

    var onlyUnder10MinFilter by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }

    LaunchedEffect(currentUserId, isProvider) {
        instantViewModel.observeInstantRequests(currentUserId, isProvider)
    }

    var now by remember { mutableLongStateOf(System.currentTimeMillis()) }
    LaunchedEffect(Unit) {
        while (true) {
            kotlinx.coroutines.delay(20_000L)
            now = System.currentTimeMillis()
        }
    }

    val mySubmittedOffers by instantViewModel.requestOffers.collectAsState()

    val filteredList = requestsList.filter { req ->
        val isExpired = req.expiresAt in 1..now && (req.status == "WAITING_FOR_OFFERS" || req.status == "OPEN")
        if (isExpired) return@filter false

        val remainingMinutes = if (req.expiresAt > now) ((req.expiresAt - now) / 1000L / 60L).toInt() else 30
        val matchesTimeFilter = if (onlyUnder10MinFilter) remainingMinutes in 1..10 else true
        val matchesSearch = searchQuery.isBlank() ||
                req.requestCode.contains(searchQuery, ignoreCase = true) ||
                req.serviceTitle.contains(searchQuery, ignoreCase = true) ||
                req.userNeighborhood.contains(searchQuery, ignoreCase = true)

        val alreadyOffered = isProvider && mySubmittedOffers.any { it.requestId == req.id && it.technicianId == currentUserId }

        matchesTimeFilter && matchesSearch && !alreadyOffered
    }

    Scaffold(
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Icon(Icons.Default.Warning, contentDescription = null, tint = Color(0xFFD32F2F))
                        Text("الطلبات المستعجلة (30 دقيقة)", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = Color(0xFFB71C1C))
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "رجوع")
                    }
                },
                actions = {
                    IconButton(onClick = {
                        if (filteredList.isEmpty()) {
                            android.widget.Toast.makeText(context, "لا توجد طلبات لتصديرها", android.widget.Toast.LENGTH_SHORT).show()
                            return@IconButton
                        }
                        val res = com.serviseyem.utils.ReportExporter.exportUrgentRequestsToCsv(context, filteredList)
                        res.onSuccess { file ->
                            try {
                                com.serviseyem.utils.ReportExporter.shareExportedFile(context, file, "مشاركة تقرير الطلبات العاجلة")
                            } catch (e: Exception) {
                                android.widget.Toast.makeText(context, "فشل المشاركة: ${e.message}", android.widget.Toast.LENGTH_SHORT).show()
                            }
                        }.onFailure { e ->
                            android.widget.Toast.makeText(context, "فشل التصدير: ${e.message}", android.widget.Toast.LENGTH_SHORT).show()
                        }
                    }) {
                        Icon(Icons.Default.Share, contentDescription = "تصدير التقرير", tint = Color(0xFFD32F2F))
                    }
                    if (!isProvider) {
                        IconButton(onClick = onNavigateToNewUrgentRequest) {
                            Icon(Icons.Default.AddCircle, contentDescription = "طلب عاجل جديد", tint = Color(0xFFD32F2F))
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFFFFEBEE))
            )
        },
        floatingActionButton = {
            if (!isProvider) {
                ExtendedFloatingActionButton(
                    onClick = onNavigateToNewUrgentRequest,
                    icon = { Icon(Icons.Default.Warning, contentDescription = null) },
                    text = { Text("طلب عاجل ⚡") },
                    containerColor = Color(0xFFD32F2F),
                    contentColor = Color.White
                )
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            UrgentListFilterBar(
                searchQuery = searchQuery,
                onSearchQueryChange = { searchQuery = it },
                onlyUnder10MinFilter = onlyUnder10MinFilter,
                onToggleUnder10MinFilter = { onlyUnder10MinFilter = it },
                themeColors = themeColors
            )

            UrgentListContent(
                requests = filteredList,
                isProvider = isProvider,
                themeColors = themeColors,
                onNavigateToDetails = onNavigateToDetails,
                onNavigateToSubmitUrgentOffer = onNavigateToSubmitUrgentOffer,
                now = now
            )
        }
    }
}
