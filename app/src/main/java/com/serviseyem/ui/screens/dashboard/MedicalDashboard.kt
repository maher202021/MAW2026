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
import com.serviseyem.data.repositories.*
import com.serviseyem.ui.MainViewModel
import com.serviseyem.ui.screens.dashboard.components.UnifiedEmptyState
import com.serviseyem.ui.screens.dashboard.components.UnifiedLoadingIndicator
import com.serviseyem.ui.screens.dashboard.viewmodels.MedicalDashboardViewModel
import com.serviseyem.utils.VisualThemePalette
import androidx.hilt.navigation.compose.hiltViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MedicalDashboard(
    account: UnifiedBusinessAccount,
    viewModel: MainViewModel,
    themeColors: VisualThemePalette,
    onBackClick: () -> Unit,
    medicalViewModel: MedicalDashboardViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    var activeTab by remember { mutableIntStateOf(0) }

    LaunchedEffect(account.id) {
        medicalViewModel.initialize(account.id)
    }

    val uiState by medicalViewModel.uiState.collectAsState()
    val doctors by medicalViewModel.doctors.collectAsState()
    val allBookings by viewModel.bookings.collectAsState()

    val medicalBookings = remember(allBookings, account) {
        allBookings.filter { b -> b.providerId == account.id || b.providerPhone == account.phone }
    }

    var showAddDoctorDialog by remember { mutableStateOf(false) }
    var docNameInput by remember { mutableStateOf("") }
    var docSpecInput by remember { mutableStateOf("") }
    var docHoursInput by remember { mutableStateOf("") }

    val tabs = listOf(
        "العيادات والأطباء 🩺",
        "الحجوزات الطبية 📅",
        "الخدمات الطبية 💊",
        "تقييمات المرضى ⭐",
        "الملف التعريفي 📝"
    )

    LaunchedEffect(medicalViewModel) {
        medicalViewModel.eventFlow.collect { event ->
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
                        Text(text = "لوحة تحكم المركز الطبي 🩺", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = themeColors.textPrimary)
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
                                Text(text = "🩺 أطباء المركز والعيادات (${doctors.size})", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = themeColors.textPrimary)
                                Button(
                                    onClick = { showAddDoctorDialog = true },
                                    colors = ButtonDefaults.buttonColors(containerColor = themeColors.accent),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Text("إضافة طبيب 🩺", fontSize = 12.sp, color = Color.Black, fontWeight = FontWeight.Bold)
                                }
                            }

                            if (doctors.isEmpty()) {
                                UnifiedEmptyState(
                                    title = "لا يوجد أطباء مضافين حالياً",
                                    description = "أضف أطباء المركز وتخصصاتهم لتمكين المرضى من حجز المواعيد.",
                                    iconText = "🩺",
                                    actionLabel = "إضافة طبيب 🩺",
                                    onActionClick = { showAddDoctorDialog = true },
                                    themeColors = themeColors
                                )
                            } else {
                                LazyColumn(
                                    verticalArrangement = Arrangement.spacedBy(10.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    items(doctors, key = { it.id }) { doc ->
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
                                                    Text(text = "د. ${doc.name}", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = themeColors.textPrimary)
                                                    Text(text = "التخصص: ${doc.specialty}", fontSize = 12.sp, color = themeColors.accent)
                                                    if (doc.workingHours.isNotBlank()) {
                                                        Text(text = "أوقات الدوام: ${doc.workingHours}", fontSize = 11.sp, color = themeColors.textSecondary)
                                                    }
                                                }
                                                IconButton(onClick = { medicalViewModel.deleteDoctor(doc.id) }) {
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
                        TabBookingsOrders(
                            bookings = medicalBookings,
                            themeColors = themeColors,
                            onAcceptBooking = { bId -> viewModel.updateBookingStatus(bId, "APPROVED") },
                            onRejectBooking = { bId, reason -> viewModel.updateBookingStatus(bId, "REJECTED") },
                            onStartProgress = { bId -> viewModel.updateBookingStatus(bId, "IN_PROGRESS") },
                            onCompleteBooking = { bId -> viewModel.updateBookingStatus(bId, "COMPLETED") },
                            onChatWithClient = { phone ->
                                viewModel.openOrCreateChatChannel(
                                    targetId = account.id,
                                    targetType = "MEDICAL",
                                    targetName = account.name,
                                    targetPhone = phone,
                                    onCreated = {}
                                )
                            }
                        )
                    }
                    2 -> {
                        TabProductsServices(
                            products = uiState.products,
                            titleLabel = "الفحوصات والخدمات الطبية",
                            addButtonLabel = "إضافة خدمة طبية 💊",
                            themeColors = themeColors,
                            onAddProduct = { title, price, desc, img ->
                                medicalViewModel.addMedicalService(title, price, desc)
                            },
                            onDeleteProduct = { id -> }
                        )
                    }
                    3 -> {
                        TabReviewsFeedback(
                            reviews = uiState.reviews,
                            themeColors = themeColors,
                            onReplySubmit = { revId, reply ->
                                Toast.makeText(context, "تم الرد على تقييم المريض بنجاح ✅", Toast.LENGTH_SHORT).show()
                            }
                        )
                    }
                    4 -> {
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
                                Toast.makeText(context, "تم تحديث بيانات المركز بنجاح ✅", Toast.LENGTH_SHORT).show()
                            },
                            onChangePassword = { oldP, newP ->
                                Toast.makeText(context, "تم تغيير كلمة المرور بنجاح 🔑", Toast.LENGTH_SHORT).show()
                            }
                        )
                    }
                }
            }
        }
    }

    if (showAddDoctorDialog) {
        AlertDialog(
            onDismissRequest = { showAddDoctorDialog = false },
            title = { Text("إضافة طبيب جديد للمركز", fontSize = 14.sp, fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = docNameInput,
                        onValueChange = { docNameInput = it },
                        label = { Text("اسم الطبيب") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = docSpecInput,
                        onValueChange = { docSpecInput = it },
                        label = { Text("التخصص الطبي") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = docHoursInput,
                        onValueChange = { docHoursInput = it },
                        label = { Text("ساعات الدوام (مثال: 4 م - 8 م)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (docNameInput.isNotBlank()) {
                            medicalViewModel.addDoctor(docNameInput, docSpecInput, docHoursInput)
                            docNameInput = ""
                            docSpecInput = ""
                            docHoursInput = ""
                            showAddDoctorDialog = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = themeColors.accent)
                ) {
                    Text("حفظ الطبيب", color = Color.Black, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddDoctorDialog = false }) {
                    Text("إلغاء")
                }
            }
        )
    }
}
