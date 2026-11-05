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
            .background(com.serviseyem.ui.theme.AppBackground)
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
            accentColor = Color(0xFF6C5CE7),
            onActionClick = {
                haptic.performHapticFeedback(androidx.compose.ui.hapticfeedback.HapticFeedbackType.LongPress)
                onCreateJobClick()
            },
            actionLabel = "نشر وظيفة +"
        )

        // Top Hero Promo Banner (Career Style with Admin Dynamic Sync)
        val allBanners by viewModel.banners.collectAsState()
        val customJobBanner = remember(allBanners) {
            allBanners.firstOrNull { 
                it.targetSection.contains("job", ignoreCase = true) || 
                it.redirectCategory.contains("job", ignoreCase = true)
            }
        }
        val displayBannerTitle = customJobBanner?.title?.ifBlank { "فرص العمل والتطوير المهني 💼" } ?: "فرص العمل والتطوير المهني 💼"
        val displayBannerImage = customJobBanner?.url?.ifBlank { "https://images.unsplash.com/photo-1486406146926-c627a92ad1ab?auto=format&fit=crop&w=800&q=80" } ?: "https://images.unsplash.com/photo-1486406146926-c627a92ad1ab?auto=format&fit=crop&w=800&q=80"
        val displayBannerBadge = customJobBanner?.displayTime?.ifBlank { "سوق العمل اليمني VIP 💼" } ?: "سوق العمل اليمني VIP 💼"

        Surface(
            modifier = Modifier.fillMaxWidth().height(160.dp),
            shape = RoundedCornerShape(20.dp),
            color = Color(0xFF121A2C),
            border = BorderStroke(1.dp, Color(0xFF6C5CE7).copy(alpha = 0.5f)),
            shadowElevation = 4.dp
        ) {
            Box(modifier = Modifier.fillMaxSize()) {
                com.serviseyem.ui.components.SmartAsyncImage(
                    model = displayBannerImage,
                    contentDescription = "فرص العمل",
                    contentScale = androidx.compose.ui.layout.ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            androidx.compose.ui.graphics.Brush.horizontalGradient(
                                listOf(Color(0xFA070A13), Color(0xE6312E81), Color(0x66070A13))
                            )
                        )
                )
                Column(modifier = Modifier.align(Alignment.CenterStart).padding(16.dp)) {
                    Surface(shape = RoundedCornerShape(8.dp), color = Color(0xFFE8B23A)) {
                        Text(displayBannerBadge, color = Color(0xFF070A13), fontSize = 10.sp, fontWeight = FontWeight.Black, modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp))
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(displayBannerTitle, color = Color.White, fontSize = 16.5.sp, fontWeight = FontWeight.Black)
                    Text("استكشف أحدث الوظائف المتاحة لدى أفضل الشركات والمؤسسات اليمنية", color = Color(0xFFCBD5E1), fontSize = 11.sp)
                    Spacer(modifier = Modifier.height(10.dp))
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = Color.White,
                        shadowElevation = 2.dp
                    ) {
                        Text("قدم سيرتك الذاتية ➔", color = Color(0xFF6C5CE7), fontSize = 11.sp, fontWeight = FontWeight.Black, modifier = Modifier.padding(horizontal = 12.dp, vertical = 5.dp))
                    }
                }
            }
        }

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
                    color = if (isSelected) Color(0xFF4F46E5) else Color(0xFF1E293B),
                    border = BorderStroke(1.dp, if (isSelected) Color.Transparent else Color(0xFF334155)),
                    shadowElevation = if (isSelected) 3.dp else 1.dp
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
                            fontWeight = if (isSelected) FontWeight.ExtraBold else FontWeight.Bold,
                            color = if (isSelected) Color.White else Color(0xFFCBD5E1)
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

