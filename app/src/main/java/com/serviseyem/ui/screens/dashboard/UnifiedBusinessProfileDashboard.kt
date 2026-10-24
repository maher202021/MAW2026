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
import com.serviseyem.data.ProviderEntity
import com.serviseyem.data.StoreEntity
import com.serviseyem.data.PropertyEntity
import com.serviseyem.data.UnifiedBusinessAccount
import com.serviseyem.ui.MainViewModel
import com.serviseyem.utils.VisualThemePalette

/**
 * 🏢 Dedicated Profile/Dashboard Router delegating to the Specialized Dashboards
 */
@Composable
fun UnifiedBusinessProfileDashboard(
    accountType: String, // "TECHNICIAN", "STORE", "RESTAURANT", "MEDICAL", "REAL_ESTATE", "JOB_POSTER"
    providerId: String,
    viewModel: MainViewModel,
    themeColors: VisualThemePalette
) {
    val providers by viewModel.providers.collectAsState()
    val stores by viewModel.stores.collectAsState()
    val properties by viewModel.properties.collectAsState()

    val cleanId = providerId.trim().replace(" ", "").replace("+", "")
    
    val activeProvider = remember(providers, providerId, cleanId, viewModel.selectedProvider) {
        viewModel.selectedProvider ?: providers.find { it.id == providerId || it.phone.trim().replace(" ", "").replace("+", "") == cleanId }
    }
    val activeStore = remember(stores, providerId, cleanId, viewModel.selectedStore) {
        viewModel.selectedStore ?: stores.find { s ->
            s.id == providerId || s.ownerId == providerId || 
            s.phone.trim().replace(" ", "").replace("+", "") == cleanId || 
            s.ownerId.trim().replace(" ", "").replace("+", "") == cleanId 
        }
    }
    val activeProperty = remember(properties, providerId, cleanId, viewModel.selectedProperty) {
        viewModel.selectedProperty ?: properties.find { p ->
            p.id == providerId || 
            p.phone.trim().replace(" ", "").replace("+", "") == cleanId || 
            p.ownerId.trim().replace(" ", "").replace("+", "") == cleanId 
        }
    }

    when (accountType) {
        "RESTAURANT" -> {
            val store = activeStore ?: StoreEntity(
                id = providerId.ifBlank { "store_rest" },
                name = "مطعم ومقهى",
                phone = viewModel.currentUserPhone.value.ifBlank { "777000000" },
                sectionId = "restaurants",
                cityId = "صنعاء"
            )
            val acc = UnifiedBusinessAccount.fromStore(store, "restaurants")
            RestaurantDashboard(account = acc, viewModel = viewModel, themeColors = themeColors, onBackClick = { viewModel.goBack() })
        }
        "MEDICAL" -> {
            val store = activeStore ?: StoreEntity(
                id = providerId.ifBlank { "med_center" },
                name = "مركز طبي / عيادة",
                phone = viewModel.currentUserPhone.value.ifBlank { "777000000" },
                sectionId = "medical",
                cityId = "صنعاء"
            )
            val acc = UnifiedBusinessAccount.fromStore(store, "medical")
            MedicalDashboard(account = acc, viewModel = viewModel, themeColors = themeColors, onBackClick = { viewModel.goBack() })
        }
        "STORE" -> {
            val store = activeStore ?: StoreEntity(
                id = providerId.ifBlank { "store_general" },
                name = "متجر تجاري",
                phone = viewModel.currentUserPhone.value.ifBlank { "777000000" },
                sectionId = "stores",
                cityId = "صنعاء"
            )
            val acc = UnifiedBusinessAccount.fromStore(store, "stores")
            StoreDashboard(account = acc, viewModel = viewModel, themeColors = themeColors, onBackClick = { viewModel.goBack() })
        }
        "REAL_ESTATE" -> {
            val prop = activeProperty ?: PropertyEntity(
                id = providerId.ifBlank { "prop_general" },
                title = "مكتب عقاري واستثماري",
                phone = viewModel.currentUserPhone.value.ifBlank { "777000000" },
                cityId = "صنعاء"
            )
            val acc = UnifiedBusinessAccount.fromProperty(prop)
            PropertyDashboard(account = acc, viewModel = viewModel, themeColors = themeColors, onBackClick = { viewModel.goBack() })
        }
        "JOB_POSTER" -> {
            val acc = UnifiedBusinessAccount(
                id = providerId.ifBlank { "job_poster_1" },
                name = activeStore?.name ?: activeProvider?.name ?: "مسؤول التوظيف والفرص",
                phone = activeStore?.phone ?: activeProvider?.phone ?: viewModel.currentUserPhone.value.ifBlank { "777000000" },
                cityId = activeStore?.cityId ?: activeProvider?.cityId ?: "صنعاء",
                businessType = com.serviseyem.data.BusinessType.JOB_POSTER
            )
            JobPosterDashboard(account = acc, viewModel = viewModel, themeColors = themeColors, onBackClick = { viewModel.goBack() })
        }
        else -> { // TECHNICIAN or default
            val prov = activeProvider ?: ProviderEntity(
                id = providerId.ifBlank { "tech_provider" },
                name = "لوحة تحكم الفني",
                phone = viewModel.currentUserPhone.value.ifBlank { "777000000" },
                categoryId = "technician",
                area = "صنعاء"
            )
            val acc = UnifiedBusinessAccount.fromProvider(prov)
            TechnicianDashboard(account = acc, viewModel = viewModel, themeColors = themeColors, onBackClick = { viewModel.goBack() })
        }
    }
}
