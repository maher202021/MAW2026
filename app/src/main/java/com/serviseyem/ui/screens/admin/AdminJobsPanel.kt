package com.serviseyem.ui.screens.admin

import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.serviseyem.data.JobApplicationEntity
import com.serviseyem.data.JobEntity
import com.serviseyem.ui.MainViewModel
import com.serviseyem.ui.screens.admin.components.AdminEntityCard
import com.serviseyem.utils.VisualThemePalette

/**
 * 💼 Admin Panel: Jobs & Applicants Management (إدارة الوظائف والمتقدمين)
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminJobsPanel(
    onBack: () -> Unit = {},
    viewModel: MainViewModel = viewModel(),
    themeColors: VisualThemePalette,
    modifier: Modifier = Modifier,
    initialTab: String = "JOBS"
) {
    val context = LocalContext.current
    val allJobs by viewModel.jobs.collectAsState()
    val allApplicants by viewModel.jobApplications.collectAsState()

    var activeTab by remember(initialTab) {
        mutableStateOf(if (initialTab == "APPLICANTS") "المتقدمين للوظائف" else "إعلانات الوظائف")
    }

    var searchQuery by remember { mutableStateOf("") }
    var selectedFilter by remember { mutableStateOf("الكل") }
    var showAddJobDialog by remember { mutableStateOf(false) }

    val filteredJobs = remember(allJobs, searchQuery, selectedFilter) {
        allJobs.filter { job ->
            val matchesSearch = searchQuery.isBlank() ||
                    job.title.contains(searchQuery, ignoreCase = true) ||
                    job.companyName.contains(searchQuery, ignoreCase = true) ||
                    job.cityId.contains(searchQuery, ignoreCase = true) ||
                    job.phone.contains(searchQuery)

            val matchesFilter = when (selectedFilter) {
                "نشط" -> job.isActive && !job.isBlocked
                "مميز VIP" -> job.isVip
                "محظور" -> job.isBlocked
                else -> true
            }

            matchesSearch && matchesFilter
        }
    }

    val filteredApplicants = remember(allApplicants, searchQuery) {
        allApplicants.filter { app ->
            searchQuery.isBlank() ||
                    app.applicantName.contains(searchQuery, ignoreCase = true) ||
                    app.jobTitle.contains(searchQuery, ignoreCase = true) ||
                    app.applicantPhone.contains(searchQuery)
        }
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Main Control Card
        Card(
            colors = CardDefaults.cardColors(containerColor = themeColors.surface),
            shape = RoundedCornerShape(14.dp),
            border = BorderStroke(1.dp, themeColors.accent.copy(alpha = 0.3f)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (activeTab == "إعلانات الوظائف") "💼 إعلانات الوظائف (${allJobs.size})" else "📋 طلبات المتقدمين (${allApplicants.size})",
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.5.sp,
                        color = Color.White
                    )

                    if (activeTab == "إعلانات الوظائف") {
                        Button(
                            onClick = { showAddJobDialog = true },
                            colors = ButtonDefaults.buttonColors(containerColor = themeColors.accent),
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null, tint = Color.Black, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("إضافة وظيفة ➕", fontSize = 11.sp, color = Color.Black, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                // Sub-tabs
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = { activeTab = "إعلانات الوظائف" },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (activeTab == "إعلانات الوظائف") themeColors.accent else Color.White.copy(alpha = 0.08f)
                        ),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(
                            "💼 الوظائف المعروضة (${allJobs.size})",
                            fontSize = 11.5.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (activeTab == "إعلانات الوظائف") Color.Black else Color.White
                        )
                    }

                    Button(
                        onClick = { activeTab = "المتقدمين للوظائف" },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (activeTab == "المتقدمين للوظائف") themeColors.accent else Color.White.copy(alpha = 0.08f)
                        ),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(
                            "📋 المتقدمين (${allApplicants.size})",
                            fontSize = 11.5.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (activeTab == "المتقدمين للوظائف") Color.Black else Color.White
                        )
                    }
                }

                // Search Bar
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = {
                        Text(
                            if (activeTab == "إعلانات الوظائف") "بحث بعنوان الوظيفة، الشركة، المدينة، الهاتف..." else "بحث باسم المتقدم، المسمى الوظيفي، الهاتف...",
                            fontSize = 11.5.sp,
                            color = Color.Gray
                        )
                    },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = themeColors.accent, modifier = Modifier.size(18.dp)) },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { searchQuery = "" }) {
                                Icon(Icons.Default.Clear, contentDescription = "مسح", tint = Color.Gray, modifier = Modifier.size(16.dp))
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedBorderColor = themeColors.accent,
                        unfocusedBorderColor = Color.White.copy(alpha = 0.15f)
                    )
                )

                // Filter Chips for Jobs
                if (activeTab == "إعلانات الوظائف") {
                    val filters = listOf("الكل", "نشط", "مميز VIP", "محظور")
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        items(filters) { f ->
                            val isSelected = selectedFilter == f
                            FilterChip(
                                selected = isSelected,
                                onClick = { selectedFilter = f },
                                label = { Text(f, fontSize = 11.sp, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = themeColors.accent,
                                    selectedLabelColor = Color.Black,
                                    containerColor = Color.White.copy(alpha = 0.05f),
                                    labelColor = Color.White
                                )
                            )
                        }
                    }
                }
            }
        }

        // Content Section
        if (activeTab == "إعلانات الوظائف") {
            if (filteredJobs.isEmpty()) {
                Card(
                    colors = CardDefaults.cardColors(containerColor = themeColors.surface),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Box(modifier = Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                        Text("لا توجد إعلانات وظائف مطابقة 💼", color = Color.Gray, fontSize = 13.sp)
                    }
                }
            } else {
                filteredJobs.forEach { job ->
                    AdminEntityCard(
                        title = job.title.ifBlank { "إعلان وظيفة" },
                        subtitle = "🏢 ${job.companyName.ifBlank { "جهة غير محددة" }} • 📍 ${job.cityId} • 📱 ${job.phone}",
                        details = "💰 الراتب: ${job.salary.ifBlank { "يحدد بعد المقابلة" }} • ⏱️ نوع الدوام: ${job.jobType.ifBlank { "دوام كامل" }}",
                        statusText = if (job.isBlocked) "🚫 محظور" else if (job.isVip) "⭐ VIP" else if (job.isActive) "نشط ✅" else "معلق ⏸️",
                        statusColor = if (job.isBlocked) Color(0xFFEF4444) else if (job.isVip) Color(0xFFF59E0B) else Color(0xFF10B981),
                        isVip = job.isVip,
                        isBlocked = job.isBlocked,
                        themeColors = themeColors,
                        actions = {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                OutlinedButton(
                                    onClick = {
                                        viewModel.adminViewModel.setJobVip(job.id, !job.isVip)
                                        Toast.makeText(context, if (job.isVip) "تم إلغاء تمييز الوظيفة" else "تم تمييز الوظيفة بنجاح ⭐", Toast.LENGTH_SHORT).show()
                                    },
                                    shape = RoundedCornerShape(8.dp),
                                    border = BorderStroke(1.dp, if (job.isVip) Color(0xFFF59E0B) else Color.White.copy(alpha = 0.3f)),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Text(
                                        if (job.isVip) "إلغاء VIP" else "تمييز VIP ⭐",
                                        fontSize = 10.5.sp,
                                        color = if (job.isVip) Color(0xFFF59E0B) else Color.White
                                    )
                                }

                                if (job.phone.isNotBlank()) {
                                    IconButton(
                                        onClick = {
                                            try {
                                                val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:${job.phone}"))
                                                context.startActivity(intent)
                                            } catch (e: Exception) {
                                                Toast.makeText(context, "تعذر فتح تطبيق الاتصال", Toast.LENGTH_SHORT).show()
                                            }
                                        },
                                        modifier = Modifier.background(Color(0xFF10B981).copy(alpha = 0.15f), RoundedCornerShape(8.dp))
                                    ) {
                                        Icon(Icons.Default.Call, contentDescription = "اتصال", tint = Color(0xFF10B981), modifier = Modifier.size(18.dp))
                                    }
                                }

                                IconButton(
                                    onClick = {
                                        viewModel.adminViewModel.deleteJobPermanently(job.id)
                                        Toast.makeText(context, "🗑️ تم حذف الإعلان الوظيفي", Toast.LENGTH_SHORT).show()
                                    },
                                    modifier = Modifier.background(Color(0xFFEF5350).copy(alpha = 0.15f), RoundedCornerShape(8.dp))
                                ) {
                                    Icon(Icons.Default.Delete, contentDescription = "حذف", tint = Color(0xFFEF5350), modifier = Modifier.size(18.dp))
                                }
                            }
                        }
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }
        } else {
            // Applicants Tab
            if (filteredApplicants.isEmpty()) {
                Card(
                    colors = CardDefaults.cardColors(containerColor = themeColors.surface),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Box(modifier = Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                        Text("لا يوجد متقدمين للوظائف حالياً 📋", color = Color.Gray, fontSize = 13.sp)
                    }
                }
            } else {
                filteredApplicants.forEach { app ->
                    AdminEntityCard(
                        title = "👤 ${app.applicantName}",
                        subtitle = "💼 الوظيفة: ${app.jobTitle} • 📱 ${app.applicantPhone}",
                        details = "🎓 المؤهل والخبرة: ${app.applicantQuals.ifBlank { "مقدم عبر التطبيق" }} • 📅 الحالة: ${app.status}",
                        statusText = when (app.status) {
                            "ACCEPTED" -> "مقبول ✅"
                            "REJECTED" -> "مرفوض ❌"
                            else -> "قيد المراجعة ⏳"
                        },
                        statusColor = when (app.status) {
                            "ACCEPTED" -> Color(0xFF10B981)
                            "REJECTED" -> Color(0xFFEF4444)
                            else -> Color(0xFFF59E0B)
                        },
                        themeColors = themeColors,
                        actions = {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Button(
                                    onClick = {
                                        viewModel.adminViewModel.updateJobApplicationStatus(app.id, "ACCEPTED")
                                        Toast.makeText(context, "تم قبول طلب المتقدم ✅", Toast.LENGTH_SHORT).show()
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981)),
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Text("قبول ✅", fontSize = 10.5.sp, color = Color.White, fontWeight = FontWeight.Bold)
                                }

                                OutlinedButton(
                                    onClick = {
                                        viewModel.adminViewModel.updateJobApplicationStatus(app.id, "REJECTED")
                                        Toast.makeText(context, "تم رفض طلب المتقدم ❌", Toast.LENGTH_SHORT).show()
                                    },
                                    shape = RoundedCornerShape(8.dp),
                                    border = BorderStroke(1.dp, Color(0xFFEF4444)),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Text("رفض ❌", fontSize = 10.5.sp, color = Color(0xFFEF4444))
                                }

                                if (app.applicantPhone.isNotBlank()) {
                                    IconButton(
                                        onClick = {
                                            try {
                                                val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:${app.applicantPhone}"))
                                                context.startActivity(intent)
                                            } catch (e: Exception) {
                                                Toast.makeText(context, "تعذر فتح تطبيق الاتصال", Toast.LENGTH_SHORT).show()
                                            }
                                        },
                                        modifier = Modifier.background(Color(0xFF10B981).copy(alpha = 0.15f), RoundedCornerShape(8.dp))
                                    ) {
                                        Icon(Icons.Default.Call, contentDescription = "اتصال", tint = Color(0xFF10B981), modifier = Modifier.size(18.dp))
                                    }
                                }

                                IconButton(
                                    onClick = {
                                        viewModel.adminViewModel.deleteJobApplication(app.id)
                                        Toast.makeText(context, "🗑️ تم حذف طلب المتقدم", Toast.LENGTH_SHORT).show()
                                    },
                                    modifier = Modifier.background(Color(0xFFEF5350).copy(alpha = 0.15f), RoundedCornerShape(8.dp))
                                ) {
                                    Icon(Icons.Default.Delete, contentDescription = "حذف", tint = Color(0xFFEF5350), modifier = Modifier.size(18.dp))
                                }
                            }
                        }
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }
        }
    }

    // Add Job Dialog
    if (showAddJobDialog) {
        var jobTitleInput by remember { mutableStateOf("") }
        var jobCompanyInput by remember { mutableStateOf("") }
        var jobPhoneInput by remember { mutableStateOf("") }
        var jobCityInput by remember { mutableStateOf("صنعاء") }
        var jobSalaryInput by remember { mutableStateOf("") }
        var jobDescInput by remember { mutableStateOf("") }

        AlertDialog(
            onDismissRequest = { showAddJobDialog = false },
            title = { Text("➕ إضافة إعلان وظيفة جديد", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Color.White) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(value = jobTitleInput, onValueChange = { jobTitleInput = it }, label = { Text("المسمى الوظيفي") }, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(value = jobCompanyInput, onValueChange = { jobCompanyInput = it }, label = { Text("اسم الشركة / المنشأة") }, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(value = jobPhoneInput, onValueChange = { jobPhoneInput = it }, label = { Text("رقم الهاتف للتقديم") }, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(value = jobCityInput, onValueChange = { jobCityInput = it }, label = { Text("المدينة") }, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(value = jobSalaryInput, onValueChange = { jobSalaryInput = it }, label = { Text("الراتب المتوقع (اختياري)") }, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(value = jobDescInput, onValueChange = { jobDescInput = it }, label = { Text("شروط ومتطلبات الوظيفة") }, modifier = Modifier.fillMaxWidth().height(70.dp))
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (jobTitleInput.isBlank()) {
                            Toast.makeText(context, "يرجى كتابة المسمى الوظيفي", Toast.LENGTH_SHORT).show()
                            return@Button
                        }
                        val newJob = JobEntity(
                            id = "job_${System.currentTimeMillis()}",
                            title = jobTitleInput.trim(),
                            companyName = jobCompanyInput.trim(),
                            phone = jobPhoneInput.trim(),
                            cityId = jobCityInput.trim(),
                            salary = jobSalaryInput.trim(),
                            description = jobDescInput.trim(),
                            isApproved = true,
                            isActive = true
                        )
                        viewModel.adminViewModel.saveJob(newJob)
                        showAddJobDialog = false
                        Toast.makeText(context, "تمت إضافة الوظيفة بنجاح 🎉", Toast.LENGTH_SHORT).show()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = themeColors.accent)
                ) {
                    Text("إضافة وحفظ ➕", color = Color.Black, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddJobDialog = false }) { Text("إلغاء", color = Color.Gray) }
            },
            containerColor = Color(0xFF1E293B)
        )
    }
}
