package com.serviseyem

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.serviseyem.data.*
import com.serviseyem.ui.*
import com.serviseyem.ui.MainViewModel
import com.serviseyem.utils.VisualThemePalette

// --------------------------------------------------------
// 10. LEGACY ADMIN JOBS, APPLICANTS, BLOCKED & DELETED PANELS
// --------------------------------------------------------
@Composable
fun AdminJobsPanel(
    viewModel: MainViewModel,
    themeColors: VisualThemePalette
) {
    val activatedProviders by viewModel.providers.collectAsState()
    val categories by viewModel.categories.collectAsState()
    var activeJobsSearchQuery by remember { mutableStateOf("") }

    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
        Text("💼 إدارة شواغر التوظيف والوظائف الشاملة", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = themeColors.accent)
        Text("البحث والتحكم في شارات وتفاصيل إعلانات الوظائف وإمكانيات التواصل والاتصال والحذف:", fontSize = 11.sp, color = themeColors.textSecondary)
        Spacer(modifier = Modifier.height(4.dp))

        OutlinedTextField(
            value = activeJobsSearchQuery,
            onValueChange = { activeJobsSearchQuery = it },
            label = { Text("البحث في شواغر الوظائف والمهن...") },
            modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
            colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White),
            trailingIcon = { Icon(imageVector = Icons.Default.Search, contentDescription = null, tint = themeColors.accent) }
        )

        val filteredJobs = activatedProviders.filter {
            val isJob = it.categoryId.contains("job", ignoreCase = true) || 
                        it.profession.contains("job", ignoreCase = true) || 
                        it.profession.contains("وظيفة", ignoreCase = true) || 
                        it.profession.contains("توظيف", ignoreCase = true) || 
                        it.profession.contains("شاغر", ignoreCase = true) || 
                        it.profession.contains("وظائف", ignoreCase = true)
            isJob && (
                it.name.contains(activeJobsSearchQuery, ignoreCase = true) ||
                it.phone.contains(activeJobsSearchQuery, ignoreCase = true) ||
                it.area.contains(activeJobsSearchQuery, ignoreCase = true)
            )
        }

        if (filteredJobs.isEmpty()) {
            Card(
                colors = CardDefaults.cardColors(containerColor = themeColors.surface),
                modifier = Modifier.fillMaxWidth().padding(top = 8.dp)
            ) {
                Box(modifier = Modifier.fillMaxWidth().padding(16.dp), contentAlignment = Alignment.Center) {
                    Text("لا توجد إعلانات وظائف مطابقة للبحث حالياً.", fontSize = 11.sp, color = Color.LightGray)
                }
            }
        } else {
            filteredJobs.forEach { p ->
                Card(
                    colors = CardDefaults.cardColors(containerColor = themeColors.surface),
                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(p.name, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                val catName = if (p.categoryId == "other" && p.customCategoryName.isNotEmpty()) p.customCategoryName else (categories.find { it.id == p.categoryId }?.name ?: "إعلان توظيف")
                                Text("المسمى الوظيفي: ${p.profession.ifEmpty { catName }} | المنطقة: ${p.area}", fontSize = 11.sp, color = themeColors.textSecondary)
                            }
                            IconButton(
                                onClick = { viewModel.removeProvider(p.id) }
                            ) {
                                Icon(imageVector = Icons.Default.Delete, contentDescription = "حذف الإعلان نهائياً", tint = Color.Red, modifier = Modifier.size(20.dp))
                            }
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.weight(1f)
                            ) {
                                Checkbox(
                                    checked = p.isVip,
                                    onCheckedChange = { viewModel.pinProvider(p.id, it) },
                                    colors = CheckboxDefaults.colors(checkedColor = Color(0xFFD97706)),
                                    modifier = Modifier.size(32.dp)
                                )
                                Spacer(modifier = Modifier.width(2.dp))
                                Text("VIP ذهبي", fontSize = 10.sp, color = Color.White, maxLines = 1)
                            }
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.weight(1f)
                            ) {
                                Checkbox(
                                    checked = p.isVerified,
                                    onCheckedChange = { viewModel.verifyProviderBadge(p.id, it) },
                                    colors = CheckboxDefaults.colors(checkedColor = Color(0xFF3B82F6)),
                                    modifier = Modifier.size(32.dp)
                                )
                                Spacer(modifier = Modifier.width(2.dp))
                                Text("موثق حساب", fontSize = 10.sp, color = Color.White, maxLines = 1)
                            }
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.weight(1f)
                            ) {
                                Checkbox(
                                    checked = p.isRecommended,
                                    onCheckedChange = { viewModel.recommendProvider(p.id, it) },
                                    colors = CheckboxDefaults.colors(checkedColor = Color(0xFFEC4899)),
                                    modifier = Modifier.size(32.dp)
                                )
                                Spacer(modifier = Modifier.width(2.dp))
                                Text("موصى به", fontSize = 10.sp, color = Color.White, maxLines = 1)
                            }
                        }
                        
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.weight(1f)
                            ) {
                                Checkbox(
                                    checked = p.isChatDisabled,
                                    onCheckedChange = { viewModel.setProviderChatDisabled(p.id, it) },
                                    colors = CheckboxDefaults.colors(checkedColor = Color(0xFFEF4444)),
                                    modifier = Modifier.size(32.dp)
                                )
                                Spacer(modifier = Modifier.width(2.dp))
                                Text("إيقاف الدردشة 🔇", fontSize = 9.sp, color = Color.LightGray, maxLines = 1)
                            }
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.weight(1f)
                            ) {
                                Checkbox(
                                    checked = p.isNotificationsDisabled,
                                    onCheckedChange = { viewModel.setProviderNotificationsDisabled(p.id, it) },
                                    colors = CheckboxDefaults.colors(checkedColor = Color(0xFFEF4444)),
                                    modifier = Modifier.size(32.dp)
                                )
                                Spacer(modifier = Modifier.width(2.dp))
                                Text("إيقاف الإشعارات 🔕", fontSize = 9.sp, color = Color.LightGray, maxLines = 1)
                            }
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.weight(1f)
                            ) {
                                Checkbox(
                                    checked = p.isPaymentRequired,
                                    onCheckedChange = { viewModel.setProviderPaymentRequired(p.id, it) },
                                    colors = CheckboxDefaults.colors(checkedColor = Color(0xFF10B981)),
                                    modifier = Modifier.size(32.dp)
                                )
                                Spacer(modifier = Modifier.width(2.dp))
                                Text("ربط بالدفع 💳", fontSize = 9.sp, color = Color.LightGray, maxLines = 1)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun AdminJobApplicantsPanel(
    viewModel: MainViewModel,
    themeColors: VisualThemePalette
) {
    val context = LocalContext.current
    val applications by viewModel.jobApplications.collectAsState()
    var searchQuery by remember { mutableStateOf("") }
    var selectedStatusFilter by remember { mutableStateOf("ALL") }
    var showNotifDialog by remember { mutableStateOf(false) }
    var notifTitle by remember { mutableStateOf("") }
    var notifMessage by remember { mutableStateOf("") }
    var rejectReasonAppId by remember { mutableStateOf<String?>(null) }
    var rejectReasonText by remember { mutableStateOf("") }

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text("📄 إدارة المتقدمين للوظائف والسير الذاتية", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = themeColors.accent)
                Text("مراجعة وتصدير وقبول/رفض طلبات المتقدمين مع إمكانية التواصل المباشر:", fontSize = 11.sp, color = themeColors.textSecondary)
            }
        }

        // Quick Export and Notify Action Bar
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Button(
                onClick = { viewModel.exportJobApplicantsCsv(context) },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981)),
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text("📋 تصدير CSV للحافظة", fontSize = 10.sp, color = Color.Black, fontWeight = FontWeight.Bold)
            }
            Button(
                onClick = { showNotifDialog = true },
                colors = ButtonDefaults.buttonColors(containerColor = themeColors.accent),
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text("🔔 إشعار جماعي للمتقدمين", fontSize = 10.sp, color = Color.Black, fontWeight = FontWeight.Bold)
            }
        }

        // Search Field
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            label = { Text("البحث بالاسم، الرقم، الوظيفة، أو المؤهل...") },
            modifier = Modifier.fillMaxWidth(),
            colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White),
            trailingIcon = { Icon(imageVector = Icons.Default.Search, contentDescription = null, tint = themeColors.accent) }
        )

        // Status Filter Chips
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            listOf(
                "ALL" to "الكل (${applications.size})",
                "PENDING" to "قيد الانتظار (${applications.count { it.status == "PENDING" }})",
                "ACCEPTED" to "مقبول (${applications.count { it.status == "ACCEPTED" }})",
                "REJECTED" to "مرفوض (${applications.count { it.status == "REJECTED" }})"
            ).forEach { (statusKey, label) ->
                val isSel = selectedStatusFilter == statusKey
                Button(
                    onClick = { selectedStatusFilter = statusKey },
                    colors = ButtonDefaults.buttonColors(containerColor = if (isSel) themeColors.accent else Color.DarkGray),
                    modifier = Modifier.weight(1f),
                    contentPadding = PaddingValues(vertical = 4.dp)
                ) {
                    Text(label, fontSize = 9.sp, color = if (isSel) Color.Black else Color.White, maxLines = 1)
                }
            }
        }

        val filteredApps = applications.filter { app ->
            val matchesStatus = selectedStatusFilter == "ALL" || app.status == selectedStatusFilter
            val matchesSearch = searchQuery.isEmpty() ||
                    app.applicantName.contains(searchQuery, ignoreCase = true) ||
                    app.applicantPhone.contains(searchQuery, ignoreCase = true) ||
                    app.jobTitle.contains(searchQuery, ignoreCase = true) ||
                    app.applicantQuals.contains(searchQuery, ignoreCase = true)
            matchesStatus && matchesSearch
        }

        if (filteredApps.isEmpty()) {
            Card(
                colors = CardDefaults.cardColors(containerColor = themeColors.surface),
                modifier = Modifier.fillMaxWidth().padding(top = 8.dp)
            ) {
                Box(modifier = Modifier.fillMaxWidth().padding(20.dp), contentAlignment = Alignment.Center) {
                    Text("لا يوجد متقدمون مطابقون للبحث الحالي.", fontSize = 11.sp, color = Color.LightGray)
                }
            }
        } else {
            filteredApps.forEach { app ->
                Card(
                    colors = CardDefaults.cardColors(containerColor = themeColors.surface),
                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                    border = BorderStroke(1.dp, when (app.status) {
                        "ACCEPTED" -> Color.Green.copy(alpha = 0.3f)
                        "REJECTED" -> Color.Red.copy(alpha = 0.3f)
                        else -> themeColors.accent.copy(alpha = 0.2f)
                    })
                ) {
                    Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(app.applicantName, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color.White)
                            Surface(
                                color = when (app.status) {
                                    "ACCEPTED" -> Color.Green.copy(alpha = 0.2f)
                                    "REJECTED" -> Color.Red.copy(alpha = 0.2f)
                                    else -> Color.Yellow.copy(alpha = 0.2f)
                                },
                                shape = RoundedCornerShape(4.dp)
                            ) {
                                Text(
                                    text = when (app.status) {
                                        "ACCEPTED" -> "✅ مقبول"
                                        "REJECTED" -> "❌ مرفوض"
                                        else -> "⏳ قيد الدراسية"
                                    },
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = when (app.status) {
                                        "ACCEPTED" -> Color.Green
                                        "REJECTED" -> Color.Red
                                        else -> Color.Yellow
                                    },
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }

                        Text("الوظيفة: ${app.jobTitle} (${app.companyName})", fontSize = 10.sp, color = themeColors.accent, fontWeight = FontWeight.Bold)
                        Text("📱 رقم الهاتف: ${app.applicantPhone}", fontSize = 10.sp, color = Color.White)
                        
                        if (app.applicantQuals.isNotEmpty()) {
                            Text("📝 المؤهلات والخبرات:\n${app.applicantQuals}", fontSize = 9.sp, color = Color.LightGray)
                        }

                        // Action Buttons
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Button(
                                onClick = { viewModel.acceptJobApplication(app.id) },
                                colors = ButtonDefaults.buttonColors(containerColor = Color.Green),
                                modifier = Modifier.weight(1f),
                                contentPadding = PaddingValues(0.dp)
                            ) {
                                Text("قبول الطلب ✅", fontSize = 9.sp, color = Color.Black, fontWeight = FontWeight.Bold)
                            }

                            Button(
                                onClick = { rejectReasonAppId = app.id },
                                colors = ButtonDefaults.buttonColors(containerColor = Color.Red),
                                modifier = Modifier.weight(1f),
                                contentPadding = PaddingValues(0.dp)
                            ) {
                                Text("رفض الطلب ❌", fontSize = 9.sp, color = Color.White)
                            }

                            IconButton(
                                onClick = { viewModel.deleteJobApplication(app.id) },
                                modifier = Modifier.size(32.dp)
                            ) {
                                Icon(imageVector = Icons.Default.Delete, contentDescription = "حذف الطلب", tint = Color.Red)
                            }
                        }
                    }
                }
            }
        }
    }

    // Broadcast Dialog
    if (showNotifDialog) {
        AlertDialog(
            onDismissRequest = { showNotifDialog = false },
            title = { Text("🔔 إرسال إشعار للمتقدمين للوظائف", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = themeColors.accent) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = notifTitle,
                        onValueChange = { notifTitle = it },
                        label = { Text("عنوان الإشعار") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = notifMessage,
                        onValueChange = { notifMessage = it },
                        label = { Text("نص الرسالة الإشعارية") },
                        modifier = Modifier.fillMaxWidth().height(90.dp)
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (notifTitle.isNotEmpty() && notifMessage.isNotEmpty()) {
                            viewModel.sendNotificationToApplicants(notifTitle, notifMessage)
                            showNotifDialog = false
                            notifTitle = ""
                            notifMessage = ""
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = themeColors.accent)
                ) {
                    Text("إرسال الآن 🚀", color = Color.Black, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showNotifDialog = false }) {
                    Text("إلغاء", color = Color.White)
                }
            }
        )
    }

    // Reject Reason Dialog
    rejectReasonAppId?.let { appId ->
        AlertDialog(
            onDismissRequest = { rejectReasonAppId = null },
            title = { Text("سبب رفض طلب التقديم", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color.Red) },
            text = {
                OutlinedTextField(
                    value = rejectReasonText,
                    onValueChange = { rejectReasonText = it },
                    label = { Text("اكتب سبب الرفض الموجه للمتقدم...") },
                    modifier = Modifier.fillMaxWidth().height(80.dp)
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.rejectJobApplication(appId, rejectReasonText.ifEmpty { "عدم مطابقة الشروط المطلوب إيفاؤها" })
                        rejectReasonAppId = null
                        rejectReasonText = ""
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Red)
                ) {
                    Text("تأكيد الرفض ❌", color = Color.White, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { rejectReasonAppId = null }) {
                    Text("إلغاء", color = Color.White)
                }
            }
        )
    }
}

@Composable
fun AdminCentralizedBlockedPanel(
    viewModel: MainViewModel,
    themeColors: VisualThemePalette
) {
    val providers by viewModel.providers.collectAsState()
    val stores by viewModel.stores.collectAsState()
    val properties by viewModel.properties.collectAsState()
    val jobs by viewModel.jobs.collectAsState()
    var searchQuery by remember { mutableStateOf("") }
    var selectedCategoryFilter by remember { mutableStateOf("ALL") }

    data class BlockedItem(
        val id: String,
        val type: String, // PROVIDER, STORE, RESTAURANT, MEDICAL, PROPERTY, JOB
        val name: String,
        val phone: String,
        val blockReason: String
    )

    val allBlocked = mutableListOf<BlockedItem>()

    providers.filter { it.isBlocked || it.subscriptionStatus == "SUSPENDED" }.forEach { p ->
        allBlocked.add(BlockedItem(p.id, "PROVIDER", p.name, p.phone, "حظر إداري عام"))
    }

    stores.filter { it.isBlocked }.forEach { s ->
        val typeStr = when {
            s.categoryId.contains("طبي") || s.categoryId.contains("عياد") -> "MEDICAL"
            s.categoryId.contains("مطعم") || s.categoryId.contains("كافيه") -> "RESTAURANT"
            else -> "STORE"
        }
        allBlocked.add(BlockedItem(s.id, typeStr, s.name, s.phone, s.blockReason.ifEmpty { "حظر إداري" }))
    }

    properties.filter { it.isBlocked }.forEach { pr ->
        allBlocked.add(BlockedItem(pr.id, "PROPERTY", pr.title, pr.phone, pr.blockReason.ifEmpty { "حظر إداري" }))
    }

    jobs.filter { it.isBlocked }.forEach { j ->
        allBlocked.add(BlockedItem(j.id, "JOB", j.title, j.phone, j.blockReason.ifEmpty { "حظر إداري" }))
    }

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text("🚫 القائمة المركزية لإدارة الكيانات المحظورة", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = themeColors.accent)
        Text("عرض وإلغاء حظر جميع الفنيين والمحلات والمطاعم والمراكز الطبية والعقارات والوظائف من مكان واحد:", fontSize = 11.sp, color = themeColors.textSecondary)

        // Search Bar
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            label = { Text("البحث في الكيانات المحظورة...") },
            modifier = Modifier.fillMaxWidth(),
            colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White),
            trailingIcon = { Icon(imageVector = Icons.Default.Search, contentDescription = null, tint = themeColors.accent) }
        )

        // Category filter chips
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            listOf(
                "ALL" to "الكل (${allBlocked.size})",
                "PROVIDER" to "فنيين",
                "STORE" to "محلات",
                "RESTAURANT" to "مطاعم",
                "MEDICAL" to "طبية",
                "PROPERTY" to "عقارات",
                "JOB" to "وظائف"
            ).forEach { (catKey, label) ->
                val isSel = selectedCategoryFilter == catKey
                Button(
                    onClick = { selectedCategoryFilter = catKey },
                    colors = ButtonDefaults.buttonColors(containerColor = if (isSel) themeColors.accent else Color.DarkGray),
                    modifier = Modifier.weight(1f),
                    contentPadding = PaddingValues(0.dp)
                ) {
                    Text(label, fontSize = 8.sp, color = if (isSel) Color.Black else Color.White, maxLines = 1)
                }
            }
        }

        val filteredBlocked = allBlocked.filter { item ->
            val matchesType = selectedCategoryFilter == "ALL" || item.type == selectedCategoryFilter
            val matchesSearch = searchQuery.isEmpty() ||
                    item.name.contains(searchQuery, ignoreCase = true) ||
                    item.phone.contains(searchQuery, ignoreCase = true) ||
                    item.blockReason.contains(searchQuery, ignoreCase = true)
            matchesType && matchesSearch
        }

        if (filteredBlocked.isEmpty()) {
            Card(
                colors = CardDefaults.cardColors(containerColor = themeColors.surface),
                modifier = Modifier.fillMaxWidth().padding(top = 8.dp)
            ) {
                Box(modifier = Modifier.fillMaxWidth().padding(20.dp), contentAlignment = Alignment.Center) {
                    Text("🎉 لا توجد كيانات محظورة حالياً ضمن النطاق المSelected.", fontSize = 11.sp, color = Color.Green)
                }
            }
        } else {
            filteredBlocked.forEach { item ->
                Card(
                    colors = CardDefaults.cardColors(containerColor = themeColors.surface),
                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                    border = BorderStroke(1.dp, Color.Red.copy(alpha = 0.4f))
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp).fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Surface(
                                    color = Color.Red.copy(alpha = 0.2f),
                                    shape = RoundedCornerShape(4.dp)
                                ) {
                                    Text(
                                        text = when (item.type) {
                                            "PROVIDER" -> "🔧 فني"
                                            "STORE" -> "🏪 محل"
                                            "RESTAURANT" -> "🍔 مطعم"
                                            "MEDICAL" -> "🏥 مركز طبي"
                                            "PROPERTY" -> "🏠 عقار"
                                            "JOB" -> "💼 وظيفة"
                                            else -> "كيان"
                                        },
                                        fontSize = 9.sp,
                                        color = Color.Red,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(item.name, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.White)
                            }
                            Text("الهاتف: ${item.phone}", fontSize = 10.sp, color = themeColors.textSecondary)
                            Text("سبب الحظر: ${item.blockReason}", fontSize = 9.sp, color = Color(0xFFF87171))
                        }

                        Button(
                            onClick = { viewModel.unbanEntity(item.type, item.id) },
                            colors = ButtonDefaults.buttonColors(containerColor = Color.Green),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text("إلغاء الحظر ✅", fontSize = 10.sp, color = Color.Black, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun AdminCentralizedDeletedPanel(
    viewModel: MainViewModel,
    themeColors: VisualThemePalette
) {
    val providers by viewModel.providers.collectAsState()
    val stores by viewModel.stores.collectAsState()
    val properties by viewModel.properties.collectAsState()
    val jobs by viewModel.jobs.collectAsState()
    var searchQuery by remember { mutableStateOf("") }
    var selectedCategoryFilter by remember { mutableStateOf("ALL") }

    data class DeletedItem(
        val id: String,
        val type: String, // PROVIDER, STORE, PROPERTY, JOB
        val name: String,
        val phone: String,
        val deletedAt: Long
    )

    val allDeleted = mutableListOf<DeletedItem>()

    providers.filter { it.isDeleted }.forEach { p ->
        allDeleted.add(DeletedItem(p.id, "PROVIDER", p.name, p.phone, p.deletedAt ?: 0L))
    }

    stores.filter { it.isDeleted }.forEach { s ->
        allDeleted.add(DeletedItem(s.id, "STORE", s.name, s.phone, s.deletedAt ?: 0L))
    }

    properties.filter { it.isDeleted }.forEach { pr ->
        allDeleted.add(DeletedItem(pr.id, "PROPERTY", pr.title, pr.phone, pr.deletedAt ?: 0L))
    }

    jobs.filter { it.isDeleted }.forEach { j ->
        allDeleted.add(DeletedItem(j.id, "JOB", j.title, j.phone, 0L))
    }

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text("🗑️ سلة المحذوفات المركزية لكافة الكيانات", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = themeColors.accent)
        Text("استعادة الكيانات المحذوفة ناعماً (Soft Delete) أو حذفها نهائياً من قاعدة البيانات:", fontSize = 11.sp, color = themeColors.textSecondary)

        // Search Bar
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            label = { Text("البحث في العناصر المحذوفة...") },
            modifier = Modifier.fillMaxWidth(),
            colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White),
            trailingIcon = { Icon(imageVector = Icons.Default.Search, contentDescription = null, tint = themeColors.accent) }
        )

        // Category Filter
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            listOf(
                "ALL" to "الكل (${allDeleted.size})",
                "PROVIDER" to "فنيين",
                "STORE" to "محلات/مراكز",
                "PROPERTY" to "عقارات",
                "JOB" to "وظائف"
            ).forEach { (catKey, label) ->
                val isSel = selectedCategoryFilter == catKey
                Button(
                    onClick = { selectedCategoryFilter = catKey },
                    colors = ButtonDefaults.buttonColors(containerColor = if (isSel) themeColors.accent else Color.DarkGray),
                    modifier = Modifier.weight(1f),
                    contentPadding = PaddingValues(0.dp)
                ) {
                    Text(label, fontSize = 9.sp, color = if (isSel) Color.Black else Color.White, maxLines = 1)
                }
            }
        }

        val filteredDeleted = allDeleted.filter { item ->
            val matchesType = selectedCategoryFilter == "ALL" || item.type == selectedCategoryFilter
            val matchesSearch = searchQuery.isEmpty() ||
                    item.name.contains(searchQuery, ignoreCase = true) ||
                    item.phone.contains(searchQuery, ignoreCase = true)
            matchesType && matchesSearch
        }

        if (filteredDeleted.isEmpty()) {
            Card(
                colors = CardDefaults.cardColors(containerColor = themeColors.surface),
                modifier = Modifier.fillMaxWidth().padding(top = 8.dp)
            ) {
                Box(modifier = Modifier.fillMaxWidth().padding(20.dp), contentAlignment = Alignment.Center) {
                    Text("سلة المحذوفات فارغة حالياً.", fontSize = 11.sp, color = Color.LightGray)
                }
            }
        } else {
            filteredDeleted.forEach { item ->
                Card(
                    colors = CardDefaults.cardColors(containerColor = themeColors.surface),
                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                    border = BorderStroke(1.dp, Color.Gray.copy(alpha = 0.3f))
                ) {
                    Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Surface(
                                    color = Color.Gray.copy(alpha = 0.3f),
                                    shape = RoundedCornerShape(4.dp)
                                ) {
                                    Text(
                                        text = when (item.type) {
                                            "PROVIDER" -> "🔧 فني"
                                            "STORE" -> "🏪 محل/مركز"
                                            "PROPERTY" -> "🏠 عقار"
                                            "JOB" -> "💼 وظيفة"
                                            else -> "عنصر"
                                        },
                                        fontSize = 9.sp,
                                        color = Color.White,
                                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(item.name, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.White)
                            }
                            Text("الهاتف: ${item.phone}", fontSize = 10.sp, color = themeColors.textSecondary)
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Button(
                                onClick = { viewModel.restoreEntity(item.type, item.id) },
                                colors = ButtonDefaults.buttonColors(containerColor = themeColors.accent),
                                modifier = Modifier.weight(1f),
                                contentPadding = PaddingValues(0.dp)
                            ) {
                                Text("استعادة الكيان ♻️", fontSize = 10.sp, color = Color.Black, fontWeight = FontWeight.Bold)
                            }

                            Button(
                                onClick = { viewModel.hardDeleteEntity(item.type, item.id) },
                                colors = ButtonDefaults.buttonColors(containerColor = Color.Red),
                                modifier = Modifier.weight(1f),
                                contentPadding = PaddingValues(0.dp)
                            ) {
                                Text("حذف نهائي 🗑️", fontSize = 10.sp, color = Color.White)
                            }
                        }
                    }
                }
            }
        }
    }
}
