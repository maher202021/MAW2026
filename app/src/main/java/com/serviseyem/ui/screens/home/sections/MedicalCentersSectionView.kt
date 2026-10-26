package com.serviseyem.ui.screens.home.sections

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.serviseyem.data.StoreEntity
import com.serviseyem.domain.isAdmin
import com.serviseyem.ui.MainViewModel
import com.serviseyem.ui.components.BespokePremiumSystem
import com.serviseyem.ui.setActiveBrowserTab
import com.serviseyem.ui.screens.home.extensions.isMedicalCenter
import com.serviseyem.utils.VisualThemePalette

/**
 * 🏥 MedicalCentersSectionView - عرض المراكز والعيادات والمستشفيات الطبية بتصميم عصري فخم وخفيف
 */
@Composable
fun MedicalCentersSectionView(
    viewModel: MainViewModel,
    themeColors: VisualThemePalette,
    onStoreClick: (StoreEntity) -> Unit,
    onCreateMedicalClick: () -> Unit
) {
    val context = LocalContext.current
    val allStores by viewModel.stores.collectAsState()
    val currentUserId by viewModel.currentUserId.collectAsState()
    val adminRole by viewModel.adminRole.collectAsState()
    val isAdminUser = adminRole.isAdmin()

    val subCategories = listOf("الكل", "مستشفيات", "عيادات تخصصية", "أسنان", "صيدليات", "بصريات", "مختبرات")
    var selectedSubCategory by remember { mutableStateOf(subCategories[0]) }

    val medicalList = remember(allStores, currentUserId, adminRole, selectedSubCategory) {
        allStores.filter { 
            it.isMedicalCenter() && !it.isDeleted && (it.isApproved || it.ownerId == currentUserId || isAdminUser)
        }.filter { 
            if (selectedSubCategory == "الكل") true else it.categoryId.contains(selectedSubCategory) || it.name.contains(selectedSubCategory)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(BespokePremiumSystem.DeepDark)
            .padding(bottom = 20.dp)
    ) {
        // 0. Top Back Header
        BespokePremiumSystem.SectionBackHeader(
            title = "المراكز الطبية",
            onBackClick = { viewModel.setActiveBrowserTab("الرئيسية") }
        )

        // 1. Medical Section Header
        BespokePremiumSystem.SectionHeader(
            title = "الرعاية والمراكز الطبية",
            subtitle = "أفضل الخدمات الطبية المعتمدة في متناول يدك",
            icon = Icons.Default.HealthAndSafety,
            accentColor = BespokePremiumSystem.CyanMedical,
            actionLabel = "إضافة منشأة",
            onActionClick = onCreateMedicalClick
        )

        // 2. Emergency 24/7 Pill (Glassmorphic)
        Surface(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 8.dp),
            shape = RoundedCornerShape(16.dp),
            color = Color(0xFF991B1B).copy(alpha = 0.2f),
            border = BorderStroke(1.dp, Color(0xFFEF4444).copy(alpha = 0.4f))
        ) {
            Row(
                modifier = Modifier.padding(14.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Surface(shape = CircleShape, color = Color(0xFFEF4444), modifier = Modifier.size(10.dp)) {}
                Text("صيدليات ومراكز الطوارئ المناوبة (24/7)", color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
                Icon(Icons.Default.ChevronLeft, contentDescription = null, tint = Color.White)
            }
        }

        // 3. Category Selector (Minimalist Pills)
        Row(
            modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()).padding(horizontal = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            subCategories.forEach { cat ->
                val isSelected = selectedSubCategory == cat
                Surface(
                    onClick = { selectedSubCategory = cat },
                    shape = RoundedCornerShape(12.dp),
                    color = if (isSelected) BespokePremiumSystem.CyanMedical.copy(alpha = 0.2f) else Color.White.copy(alpha = 0.05f),
                    border = BorderStroke(1.dp, if (isSelected) BespokePremiumSystem.CyanMedical else BespokePremiumSystem.GlassBorder)
                ) {
                    Text(
                        cat,
                        color = if (isSelected) BespokePremiumSystem.CyanMedical else Color.LightGray,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // 4. Medical Cards
        medicalList.forEach { med ->
            BespokePremiumSystem.MedicalClinicalCard(
                name = med.name,
                specialty = if (med.categoryId.isNotBlank()) med.categoryId else "مركز طبي متكامل",
                isOpen = med.isActive,
                onBook = { onStoreClick(med) },
                onCall = {
                    try {
                        val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:${med.phone}"))
                        context.startActivity(intent)
                    } catch (e: Exception) {}
                },
                onNav = { /* Directions */ }
            )
        }

        if (medicalList.isEmpty()) {
            Box(modifier = Modifier.fillMaxWidth().padding(40.dp), contentAlignment = Alignment.Center) {
                Text("لا توجد منشآت طبية حالياً", color = Color.Gray, fontSize = 14.sp)
            }
        }
    }
}

