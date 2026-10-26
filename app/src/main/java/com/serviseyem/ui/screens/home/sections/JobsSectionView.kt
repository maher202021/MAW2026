package com.serviseyem.ui.screens.home.sections

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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.serviseyem.data.JobEntity
import com.serviseyem.ui.MainViewModel
import com.serviseyem.ui.components.BespokePremiumSystem
import com.serviseyem.ui.setActiveBrowserTab
import com.serviseyem.utils.VisualThemePalette

/**
 * 💼 JobsSectionView - عرض إعلانات الوظائف والفرص المتاحة بتصميم عصري فخم وخفيف (LinkedIn Style)
 */
@Composable
fun JobsSectionView(
    viewModel: MainViewModel,
    themeColors: VisualThemePalette,
    onJobClick: (JobEntity) -> Unit,
    onCreateJobClick: () -> Unit
) {
    val jobsList by viewModel.jobs.collectAsState()
    val activeJobs = remember(jobsList) { jobsList.filter { !it.isDeleted && (it.isApproved || it.isActive) } }
    
    val haptic = androidx.compose.ui.platform.LocalHapticFeedback.current

    data class SubCategoryItem(val label: String, val key: String?, val icon: String)

    val subCategories = listOf(
        SubCategoryItem("الكل", null, "💼"),
        SubCategoryItem("تقنية", "تقنية", "💻"),
        SubCategoryItem("مبيعات", "مبيعات", "💰"),
        SubCategoryItem("طبية", "طبية", "🩺"),
        SubCategoryItem("هندسية", "هندسة", "🏗️")
    )
    
    var selectedSubCat by remember { mutableStateOf(subCategories[0]) }

    val filteredList = remember(activeJobs, selectedSubCat) {
        val key = selectedSubCat.key
        if (key == null) activeJobs
        else {
            activeJobs.filter { 
                it.title.contains(key, ignoreCase = true) || 
                it.companyName.contains(key, ignoreCase = true) || 
                it.jobType.contains(key, ignoreCase = true) ||
                it.description.contains(key, ignoreCase = true)
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(com.serviseyem.ui.components.BespokePremiumSystem.DeepDark)
            .padding(8.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // 0. Top Back Header
        com.serviseyem.ui.components.BespokePremiumSystem.SectionBackHeader(
            title = "إعلانات الوظائف",
            onBackClick = { viewModel.setActiveBrowserTab("الرئيسية") }
        )

        // Section Header
        com.serviseyem.ui.components.BespokePremiumSystem.SectionHeader(
            title = "فرص العمل المتاحة",
            subtitle = "اكتشف مسارك المهني القادم في أفضل الشركات اليمنية",
            icon = Icons.Default.BusinessCenter,
            accentColor = com.serviseyem.ui.components.BespokePremiumSystem.EmeraldAccent,
            onActionClick = {
                haptic.performHapticFeedback(androidx.compose.ui.hapticfeedback.HapticFeedbackType.LongPress)
                onCreateJobClick()
            },
            actionLabel = "نشر وظيفة +"
        )

        // Subcategory Micro-Pills
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            subCategories.forEach { subCat ->
                val isSelected = selectedSubCat == subCat
                Surface(
                    onClick = { 
                        haptic.performHapticFeedback(androidx.compose.ui.hapticfeedback.HapticFeedbackType.TextHandleMove)
                        selectedSubCat = subCat 
                    },
                    shape = RoundedCornerShape(12.dp),
                    color = if (isSelected) com.serviseyem.ui.components.BespokePremiumSystem.EmeraldAccent else Color.White.copy(alpha = 0.05f),
                    border = BorderStroke(1.dp, if (isSelected) Color.White.copy(alpha = 0.2f) else com.serviseyem.ui.components.BespokePremiumSystem.GlassBorder)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(subCat.icon, fontSize = 14.sp)
                        Text(
                            subCat.label,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isSelected) com.serviseyem.ui.components.BespokePremiumSystem.DeepDark else Color.White
                        )
                    }
                }
            }
        }

        if (filteredList.isEmpty()) {
            com.serviseyem.ui.components.BespokePremiumSystem.GlassmorphicSurface(
                modifier = Modifier.fillMaxWidth().padding(top = 20.dp)
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth().padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(Icons.Default.SearchOff, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(48.dp))
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        "لا توجد وظائف معروضة في هذا القسم حالياً.",
                        fontSize = 13.sp,
                        color = Color.LightGray,
                        textAlign = TextAlign.Center
                    )
                }
            }
        } else {
            filteredList.forEach { job ->
                com.serviseyem.ui.components.BespokePremiumSystem.LinkedInJobCard(
                    title = job.title,
                    company = job.companyName,
                    type = job.jobType,
                    salary = job.salary.ifBlank { "غير محدد" },
                    logoUrl = "", // In a real app, we'd have a company logo
                    onApply = { 
                        haptic.performHapticFeedback(androidx.compose.ui.hapticfeedback.HapticFeedbackType.LongPress)
                        onJobClick(job) 
                    }
                )
            }
        }
        
        Spacer(modifier = Modifier.height(80.dp)) // Bottom padding
    }
}

