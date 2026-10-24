package com.serviseyem.ui.screens.home.sections

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
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
import com.serviseyem.utils.VisualThemePalette

/**
 * 💼 JobsSectionView - عرض إعلانات الوظائف والفرص المتاحة بتصميم عصري فخم وخفيف
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
    data class SubCategoryItem(val label: String, val key: String?)

    val subCategories = listOf(
        SubCategoryItem("الكل", null),
        SubCategoryItem("تقنية وهندسية", "تقنية"),
        SubCategoryItem("مبيعات ومحاسبة", "مبيعات"),
        SubCategoryItem("حرفية ومهنية", "حرفية"),
        SubCategoryItem("تعليمية وصحية", "تعليم")
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

    Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        // Section Header
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f).padding(end = 8.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Icon(Icons.Default.Work, contentDescription = null, tint = Color(0xFF0D5C46), modifier = Modifier.size(18.dp))
                    Text("إعلانات الوظائف والفرص", fontSize = 14.5.sp, fontWeight = FontWeight.Bold, color = Color(0xFF0F172A))
                }
                Text("تصفح أحدث الوظائف وفرص العمل الشاغرة باليمن", fontSize = 10.5.sp, color = Color(0xFF64748B), maxLines = 1)
            }
            Button(
                onClick = onCreateJobClick,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0D5C46)),
                shape = RoundedCornerShape(10.dp),
                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                modifier = Modifier.defaultMinSize(minHeight = 34.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = null, tint = Color.White, modifier = Modifier.size(15.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("نشر وظيفة", fontSize = 11.sp, color = Color.White, fontWeight = FontWeight.Bold, maxLines = 1)
            }
        }

        // Subcategory Chips
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .padding(vertical = 2.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            subCategories.forEach { subCat ->
                val isSelected = selectedSubCat == subCat
                Surface(
                    onClick = { selectedSubCat = subCat },
                    shape = RoundedCornerShape(16.dp),
                    color = if (isSelected) Color(0xFF0D5C46) else Color.White,
                    shadowElevation = if (isSelected) 3.dp else 1.dp,
                    border = BorderStroke(1.dp, if (isSelected) Color(0xFFD4AF37) else Color(0xFFE2E8F0))
                ) {
                    Text(
                        subCat.label,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isSelected) Color.White else Color(0xFF1E293B),
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                    )
                }
            }
        }

        if (filteredList.isEmpty()) {
            Card(
                colors = CardDefaults.cardColors(containerColor = Color.White),
                border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier.fillMaxWidth().padding(12.dp)
            ) {
                Text(
                    "لا توجد إعلانات وظائف معروضة في هذا القسم حالياً.",
                    fontSize = 11.5.sp,
                    color = Color(0xFF64748B),
                    modifier = Modifier.padding(16.dp),
                    textAlign = TextAlign.Center
                )
            }
        } else {
            filteredList.forEach { job ->
                Card(
                    onClick = { onJobClick(job) },
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    shape = RoundedCornerShape(16.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.5.dp),
                    border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 2.dp, vertical = 4.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = Color(0xFFF1F5F9),
                            border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
                            modifier = Modifier.size(52.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                                Icon(Icons.Default.BusinessCenter, contentDescription = null, tint = Color(0xFF0D5C46), modifier = Modifier.size(26.dp))
                            }
                        }
                        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                            Text(
                                text = job.title,
                                fontSize = 14.5.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF0F172A),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                Icon(Icons.Default.Apartment, contentDescription = null, tint = Color(0xFF64748B), modifier = Modifier.size(12.dp))
                                Text(
                                    "${job.companyName} - 📍 ${job.cityId}",
                                    fontSize = 11.sp,
                                    color = Color(0xFF475569),
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                            if (job.salary.isNotEmpty()) {
                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = Color(0xFFDCFCE7),
                                    border = BorderStroke(0.5.dp, Color(0xFF10B981))
                                ) {
                                    Text(
                                        text = "الراتب: ${job.salary}",
                                        fontSize = 10.5.sp,
                                        color = Color(0xFF065F46),
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                            }
                        }

                        Button(
                            onClick = { onJobClick(job) },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0D5C46)),
                            shape = RoundedCornerShape(10.dp),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                            modifier = Modifier.defaultMinSize(minHeight = 34.dp)
                        ) {
                            Text("تقديم", fontSize = 11.sp, color = Color.White, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}
