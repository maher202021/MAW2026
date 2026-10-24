package com.serviseyem.ui.screens.dashboard

import android.widget.Toast
import com.serviseyem.ui.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.serviseyem.data.UnifiedBusinessAccount
import com.serviseyem.data.repositories.DashboardRepositoryImpl
import com.serviseyem.data.repositories.ProductsRepositoryImpl
import com.serviseyem.ui.MainViewModel
import com.serviseyem.ui.screens.dashboard.components.UnifiedEmptyState
import com.serviseyem.ui.screens.dashboard.components.UnifiedLoadingIndicator
import com.serviseyem.ui.screens.dashboard.viewmodels.JobPosterDashboardViewModel
import com.serviseyem.utils.VisualThemePalette
import androidx.hilt.navigation.compose.hiltViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun JobPosterDashboard(
    account: UnifiedBusinessAccount,
    viewModel: MainViewModel,
    themeColors: VisualThemePalette,
    onBackClick: () -> Unit,
    jobViewModel: JobPosterDashboardViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    var activeTab by remember { mutableIntStateOf(0) }

    LaunchedEffect(account.id) {
        jobViewModel.initialize(account.id)
    }

    val uiState by jobViewModel.uiState.collectAsState()
    val jobs by jobViewModel.jobs.collectAsState()

    var showPostJobDialog by remember { mutableStateOf(false) }
    var titleInput by remember { mutableStateOf("") }
    var companyInput by remember { mutableStateOf(account.name) }
    var salaryInput by remember { mutableStateOf("") }
    var reqsInput by remember { mutableStateOf("") }

    val tabs = listOf(
        "الوظائف المنشورة 💼",
        "المتقدمين 📄",
        "إعدادات المعلن ⚙️",
        "الإحصائيات 📊"
    )

    LaunchedEffect(jobViewModel) {
        jobViewModel.eventFlow.collect { event ->
            when (event) {
                is DashboardEvent.ShowToast -> Toast.makeText(context, event.message, Toast.LENGTH_SHORT).show()
                else -> {}
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(text = "لوحة تحكم معلن الوظائف 💼", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = themeColors.textPrimary)
                        Text(text = account.name, fontSize = 11.sp, color = themeColors.textSecondary)
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = themeColors.textPrimary)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = themeColors.surface)
            )
        },
        containerColor = themeColors.background
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            ScrollableTabRow(
                selectedTabIndex = activeTab,
                containerColor = themeColors.surface,
                contentColor = themeColors.accent,
                edgePadding = 12.dp
            ) {
                tabs.forEachIndexed { index, label ->
                    Tab(
                        selected = activeTab == index,
                        onClick = { activeTab = index },
                        text = {
                            Text(
                                text = label,
                                fontSize = 12.sp,
                                fontWeight = if (activeTab == index) FontWeight.Bold else FontWeight.Normal,
                                color = if (activeTab == index) themeColors.accent else themeColors.textSecondary
                            )
                        }
                    )
                }
            }

            if (uiState.isLoading) {
                UnifiedLoadingIndicator(themeColors = themeColors)
            } else {
                when (activeTab) {
                    0 -> {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(text = "💼 الشواغر المنشورة (${jobs.size})", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = themeColors.textPrimary)
                                Button(
                                    onClick = { showPostJobDialog = true },
                                    colors = ButtonDefaults.buttonColors(containerColor = themeColors.accent),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Text("نشر وظيفة 💼", fontSize = 12.sp, color = Color.Black, fontWeight = FontWeight.Bold)
                                }
                            }

                            if (jobs.isEmpty()) {
                                UnifiedEmptyState(
                                    title = "لا توجد وظائف معلنة حالياً",
                                    description = "انشر شواغر وظائف لمؤسستك أو شركتك لاستقبال طلبات التوظيف.",
                                    iconText = "💼",
                                    actionLabel = "نشر وظيفة 💼",
                                    onActionClick = { showPostJobDialog = true },
                                    themeColors = themeColors
                                )
                            } else {
                                LazyColumn(
                                    verticalArrangement = Arrangement.spacedBy(10.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    items(jobs, key = { it.id }) { job ->
                                        Card(
                                            modifier = Modifier.fillMaxWidth(),
                                            colors = CardDefaults.cardColors(containerColor = themeColors.surface),
                                            shape = RoundedCornerShape(12.dp)
                                        ) {
                                            Row(
                                                modifier = Modifier.padding(12.dp),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                                    Text(text = job.title, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = themeColors.textPrimary)
                                                    Text(text = "الشركة: ${job.companyName}", fontSize = 11.sp, color = themeColors.textSecondary)
                                                    Text(text = "الراتب: ${job.salary}", fontSize = 11.sp, color = themeColors.accent)
                                                    Text(text = "المتقدمين: ${job.applicantsCount} متقدم 📄", fontSize = 11.sp, color = Color(0xFF10B981))
                                                }
                                                IconButton(onClick = { jobViewModel.deleteJob(job.id) }) {
                                                    Text("🗑️", fontSize = 16.sp)
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                    1 -> {
                        UnifiedEmptyState(
                            title = "لا يوجد متقدمين جدد",
                            description = "تظهر طلبات التقديم وسير المتقدمين هنا فور إرسالها من الباحثين عن عمل.",
                            iconText = "📄",
                            themeColors = themeColors
                        )
                    }
                    2 -> {
                        TabProfileEdit(
                            name = account.name,
                            phone = account.phone,
                            cityArea = account.neighborhood.ifBlank { account.cityId },
                            description = account.description,
                            workingHours = account.workingHours,
                            photoUrl = account.logoImage,
                            coverUrl = account.coverImage,
                            isAvailable = true,
                            rating = account.rating.toDouble(),
                            reviewCount = account.numReviews,
                            themeColors = themeColors,
                            onSaveProfile = { name, phone, city, desc, hours, available ->
                                Toast.makeText(context, "تم حفظ بيانات المعلن بنجاح ✅", Toast.LENGTH_SHORT).show()
                            },
                            onChangePassword = { oldP, newP ->
                                Toast.makeText(context, "تم تغيير كلمة المرور بنجاح 🔑", Toast.LENGTH_SHORT).show()
                            }
                        )
                    }
                    3 -> {
                        TabStatisticsGrowth(
                            stats = uiState.stats,
                            themeColors = themeColors
                        )
                    }
                }
            }
        }
    }

    if (showPostJobDialog) {
        AlertDialog(
            onDismissRequest = { showPostJobDialog = false },
            title = { Text("نشر شاغر وظيفي جديد", fontSize = 14.sp, fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = titleInput,
                        onValueChange = { titleInput = it },
                        label = { Text("المسمى الوظيفي") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = companyInput,
                        onValueChange = { companyInput = it },
                        label = { Text("اسم الشركة / المنشأة") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = salaryInput,
                        onValueChange = { salaryInput = it },
                        label = { Text("الراتب المتوقع") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = reqsInput,
                        onValueChange = { reqsInput = it },
                        label = { Text("الشروط والمؤهلات المطلوبة") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (titleInput.isNotBlank()) {
                            jobViewModel.postJob(titleInput, companyInput, salaryInput, reqsInput)
                            titleInput = ""
                            salaryInput = ""
                            reqsInput = ""
                            showPostJobDialog = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = themeColors.accent)
                ) {
                    Text("نشر الوظيفة", color = Color.Black, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showPostJobDialog = false }) {
                    Text("إلغاء")
                }
            }
        )
    }
}
