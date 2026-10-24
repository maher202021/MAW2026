@file:OptIn(androidx.compose.foundation.layout.ExperimentalLayoutApi::class, androidx.compose.material3.ExperimentalMaterial3Api::class)

package com.serviseyem.ui.screens.register
import com.serviseyem.ui.*

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.serviseyem.ui.MainViewModel
import com.serviseyem.utils.VisualThemePalette

/**
 * 📝 ProviderRegisterFormLayout - شاشة نماذج التسجيل والانضمام الموحدة
 * تتبع معمارية Factory Pattern و MVVM النظيفة (<200 سطر)
 *
 * @param viewModel نموذج العرض الرئيسي للتطبيق
 * @param themeColors لوحة ألوان الثيم المختارة
 * @param regType نوع التسجيل المطلوب (client, provider, store, ...)
 * @param sectionId القسم الفرعي المختار اختيارياً
 * @param onRegTypeChange استدعاء عند تغيير التبويب
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProviderRegisterFormLayout(
    viewModel: MainViewModel,
    themeColors: VisualThemePalette,
    regType: String,
    sectionId: String = "",
    onRegTypeChange: (String) -> Unit = {}
) {
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }

    val currentUserName by viewModel.currentUserName.collectAsState()
    val currentUserPhone by viewModel.currentUserPhone.collectAsState()
    val currentUserResidence by viewModel.currentUserResidence.collectAsState()
    val categories by viewModel.categories.collectAsState()

    var activeType by remember(regType) { mutableStateOf((RegistrationType.fromId(regType) ?: RegistrationType.PROVIDER)) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "نموذج تسجيل: ${activeType.title}",
                            fontSize = 14.5.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Text(
                            text = "دليل خدمات اليمن - استكمال البيانات المعيارية",
                            fontSize = 10.5.sp,
                            color = themeColors.accent
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = { viewModel.cancelOrResetJoinRequest(context) }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "رجوع",
                            tint = Color.White
                        )
                    }
                },
                actions = {
                    TextButton(onClick = { viewModel.triggerRestoreAccountDialog.value = true }) {
                        Text("استرجاع حسابي", color = themeColors.accent, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFF0F172A))
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = Color(0xFF0F172A)
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 0.dp, vertical = 0.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Category selector tabs
            ScrollableTabRow(
                selectedTabIndex = RegistrationType.values().indexOf(activeType),
                containerColor = Color(0xFF1E293B),
                contentColor = themeColors.accent,
                edgePadding = 8.dp
            ) {
                RegistrationType.values().forEach { item ->
                    Tab(
                        selected = activeType == item,
                        onClick = {
                            activeType = item
                            onRegTypeChange(item.id)
                        },
                        text = {
                            Text(
                                text = "${item.icon} ${item.title}",
                                fontSize = 11.5.sp,
                                fontWeight = if (activeType == item) FontWeight.Bold else FontWeight.Normal,
                                color = if (activeType == item) themeColors.accent else Color.Gray
                            )
                        }
                    )
                }
            }

            // Unified Simplified Registration Form
            com.serviseyem.ui.screens.register.forms.UnifiedRegistrationForm(
                role = activeType.name,
                themeColors = themeColors,
                onRegistrationSuccess = { data ->
                    val name = data["entityName"] ?: ""
                    val phone = data["phone"] ?: ""
                    val pass = data["password"] ?: ""
                    
                    viewModel.setUserSessionDetails(context, name, phone, "صنعاء")
                    
                    viewModel.submitJoinForm(
                        context = context,
                        name = name,
                        phone = phone,
                        catId = activeType.id,
                        area = data["city"] ?: "صنعاء",
                        neighborhood = "",
                        photoPath = "",
                        idCardPath = "",
                        gpsCoords = "",
                        workPhotos = emptyList(),
                        customCategoryName = data["specialization"] ?: "عام",
                        password = pass,
                        productAttachmentsJson = ""
                    )
                }
            )
        }
    }
}
