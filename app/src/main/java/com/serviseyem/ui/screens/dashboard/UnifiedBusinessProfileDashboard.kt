package com.serviseyem.ui.screens.dashboard

import androidx.compose.foundation.background
import com.serviseyem.ui.*
import androidx.compose.foundation.layout.*
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.serviseyem.data.UnifiedBusinessAccount
import com.serviseyem.ui.MainViewModel
import com.serviseyem.utils.VisualThemePalette

/**
 * 🏢 Dedicated Profile/Dashboard Router delegating to the 6 Specialized Dashboards
 */
@Composable
fun UnifiedBusinessProfileDashboard(
    accountType: String, // "TECHNICIAN", "STORE", "RESTAURANT", "MEDICAL", "REAL_ESTATE", "JOB_POSTER"
    providerId: String,
    viewModel: MainViewModel,
    themeColors: VisualThemePalette
) {
    val context = LocalContext.current
    val providers by viewModel.providers.collectAsState()
    val stores by viewModel.stores.collectAsState()
    val properties by viewModel.properties.collectAsState()

    val cleanId = providerId.trim().replace(" ", "").replace("+", "")
    val activeProvider = remember(providers, providerId, cleanId) {
        providers.find { it.id == providerId || it.phone.trim().replace(" ", "").replace("+", "") == cleanId }
    }
    val activeStore = remember(stores, providerId, cleanId) {
        stores.find { s ->
            s.id == providerId || s.ownerId == providerId || 
            s.phone.trim().replace(" ", "").replace("+", "") == cleanId || 
            s.ownerId.trim().replace(" ", "").replace("+", "") == cleanId 
        }
    }
    val activeProperty = remember(properties, providerId, cleanId) {
        properties.find { p ->
            p.id == providerId || 
            p.phone.trim().replace(" ", "").replace("+", "") == cleanId || 
            p.ownerId.trim().replace(" ", "").replace("+", "") == cleanId 
        }
    }

    when (accountType) {
        "RESTAURANT" -> {
            if (activeStore != null) {
                val acc = UnifiedBusinessAccount.fromStore(activeStore, "restaurants")
                RestaurantDashboard(account = acc, viewModel = viewModel, themeColors = themeColors, onBackClick = { viewModel.cancelOrResetJoinRequest(context) })
            } else {
                LoadingOrEmptyDashboard("المطعم")
            }
        }
        "MEDICAL" -> {
            if (activeStore != null) {
                val acc = UnifiedBusinessAccount.fromStore(activeStore, "medical")
                MedicalDashboard(account = acc, viewModel = viewModel, themeColors = themeColors, onBackClick = { viewModel.cancelOrResetJoinRequest(context) })
            } else {
                LoadingOrEmptyDashboard("المركز الطبي")
            }
        }
        "STORE" -> {
            if (activeStore != null) {
                val acc = UnifiedBusinessAccount.fromStore(activeStore, "stores")
                StoreDashboard(account = acc, viewModel = viewModel, themeColors = themeColors, onBackClick = { viewModel.cancelOrResetJoinRequest(context) })
            } else {
                LoadingOrEmptyDashboard("المتجر")
            }
        }
        "REAL_ESTATE" -> {
            if (activeProperty != null) {
                val acc = UnifiedBusinessAccount.fromProperty(activeProperty)
                PropertyDashboard(account = acc, viewModel = viewModel, themeColors = themeColors, onBackClick = { viewModel.cancelOrResetJoinRequest(context) })
            } else {
                LoadingOrEmptyDashboard("العقارات")
            }
        }
        "JOB_POSTER" -> {
            val acc = UnifiedBusinessAccount(
                id = providerId,
                name = activeStore?.name ?: activeProvider?.name ?: "مسؤول التوظيف",
                phone = activeStore?.phone ?: activeProvider?.phone ?: providerId,
                cityId = activeStore?.cityId ?: activeProvider?.cityId ?: "",
                businessType = com.serviseyem.data.BusinessType.JOB_POSTER
            )
            JobPosterDashboard(account = acc, viewModel = viewModel, themeColors = themeColors, onBackClick = { viewModel.cancelOrResetJoinRequest(context) })
        }
        else -> { // TECHNICIAN or default
            if (activeProvider != null) {
                val acc = UnifiedBusinessAccount.fromProvider(activeProvider)
                TechnicianDashboard(account = acc, viewModel = viewModel, themeColors = themeColors, onBackClick = { viewModel.cancelOrResetJoinRequest(context) })
            } else if (activeStore != null) {
                val acc = UnifiedBusinessAccount.fromStore(activeStore, "stores")
                StoreDashboard(account = acc, viewModel = viewModel, themeColors = themeColors, onBackClick = { viewModel.cancelOrResetJoinRequest(context) })
            } else {
                LoadingOrEmptyDashboard("مقدم الخدمة")
            }
        }
    }
}

@Composable
private fun LoadingOrEmptyDashboard(title: String) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0F172A)),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            CircularProgressIndicator(color = Color(0xFF1E88E5))
            Text(
                text = "جاري تحميل بيانات $title...",
                color = Color.White,
                fontSize = 14.sp
            )
        }
    }
}
