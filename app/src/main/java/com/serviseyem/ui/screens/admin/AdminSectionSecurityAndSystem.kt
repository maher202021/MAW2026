@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
package com.serviseyem.ui.screens.admin

import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.serviseyem.data.AdminSettingsEntity
import com.serviseyem.data.BookingEntity
import com.serviseyem.data.CategoryEntity
import com.serviseyem.data.ColorPaletteEntity
import com.serviseyem.data.JobEntity
import com.serviseyem.data.PropertyEntity
import com.serviseyem.data.ProviderEntity
import com.serviseyem.data.ReportEntity
import com.serviseyem.data.StoreEntity
import com.serviseyem.data.SupervisorEntity
import com.serviseyem.data.models.AdminPermissionsRegistry
import com.serviseyem.data.models.PermissionCategory
import com.serviseyem.ui.*
import com.serviseyem.ui.screens.admin.components.adminSwitchColors
import com.serviseyem.ui.screens.admin.components.shareGeneratedFile
import com.serviseyem.ui.screens.admin.components.writeTextFileToExportsAndShare
import com.serviseyem.utils.VisualThemePalette
import java.io.File
import java.io.FileOutputStream
import java.util.zip.ZipInputStream

internal fun LazyListScope.adminSecurityAndSystemSections(
    activeSubTab: String,
    adminRole: String,
    viewModel: MainViewModel,
    themeColors: VisualThemePalette,
    context: Context,
    state: AdminPanelState,
    settingsState: AdminSettingsEntity,
    activatedProviders: List<ProviderEntity>,
    deletedList: List<ProviderEntity>,
    stores: List<StoreEntity>,
    properties: List<PropertyEntity>,
    jobs: List<JobEntity>,
    categories: List<CategoryEntity>,
    bookings: List<BookingEntity>,
    reports: List<ReportEntity>,
    supervisorsList: List<SupervisorEntity>,
    colorPalettesList: List<ColorPaletteEntity>,
    passwordRecoveryRequests: List<Map<String, Any>>
) {
    when (activeSubTab) {
        "STORES" -> {
            item {
                AdminStoresPropertiesPanel(viewModel = viewModel, themeColors = themeColors, initialSector = "STORES")
            }
        }
        "RESTAURANTS" -> {
            item {
                AdminRestaurantsPanel(viewModel = viewModel, themeColors = themeColors)
            }
        }
        "QUICK_SERVICE" -> {
            item {
                AdminQuickServicePanel(viewModel = viewModel, themeColors = themeColors)
            }
        }
        "MEDICAL" -> {
            item {
                AdminMedicalPanel(viewModel = viewModel, themeColors = themeColors)
            }
        }
        "PROPERTIES" -> {
            item {
                AdminStoresPropertiesPanel(viewModel = viewModel, themeColors = themeColors, initialSector = "PROPERTIES")
            }
        }
        "JOBS", "APPLICANTS" -> {
            item {
                AdminJobsPanel(viewModel = viewModel, themeColors = themeColors, initialTab = activeSubTab)
            }
        }
        "STATS" -> {
            item {
                AdminDashboardPanel(viewModel = viewModel, themeColors = themeColors)
            }
        }
        "BACKDOOR" -> {
            if (adminRole == "OWNER") {
                item {
                    OwnerBackdoorPanelLayout(viewModel = viewModel, themeColors = themeColors)
                }
            }
        }
        "REG_REQ" -> {
            item {
                AdminRequestsPanel(viewModel = viewModel, themeColors = themeColors)
            }
        }
        "COMPLAINTS" -> {
            item {
                AdminReportsPanel(viewModel = viewModel, themeColors = themeColors)
            }
        }
        "PASSWORDS_RESET" -> {
            item {
                Text("🔑 لوحة تعيين وإعادة ضبط كلمات المرور (عرض الحسابات وكلمات المرور السابقة)", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = themeColors.accent)
                Text("يتم عرض كافة الحسابات والمسجلين في الأقسام مع كلمات المرور الخاصة بهم مباشرة دون الحاجة للبحث اليدوي:", fontSize = 11.sp, color = themeColors.textSecondary)
                Spacer(modifier = Modifier.height(8.dp))
            }

            item {
                val pendingCount = passwordRecoveryRequests.count { (it["status"] as? String) != "RESOLVED" }
                val passTabs = listOf(
                    Triple("REQUESTS", if (pendingCount > 0) "🚨 طلبات الاستعادة ($pendingCount)" else "⏳ طلبات الاستعادة", passwordRecoveryRequests.size),
                    Triple("SERVICES", "🔧 الخدمات والفنيين", activatedProviders.size),
                    Triple("STORES", "🏪 المتاجر", stores.filter { !it.categoryId.contains("طبي") && !it.categoryId.contains("عياد") && !it.categoryId.contains("مطعم") && !it.categoryId.contains("كافيه") }.size),
                    Triple("RESTAURANTS", "🍔 المطاعم", stores.filter { it.categoryId.contains("مطعم") || it.categoryId.contains("كافيه") }.size),
                    Triple("MEDICAL", "🏥 المراكز الطبية", stores.filter { it.categoryId.contains("طبي") || it.categoryId.contains("عياد") }.size),
                    Triple("PROPERTIES", "🏠 العقارات", properties.size),
                    Triple("JOBS", "💼 الوظائف", jobs.size)
                )
                LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp), modifier = Modifier.padding(bottom = 8.dp)) {
                    items(passTabs.size) { idx ->
                        val pt = passTabs[idx]
                        val isSel = state.adminPasswordSubTab == pt.first
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(14.dp))
                                .background(if (isSel) themeColors.accent else themeColors.surface)
                                .clickable { state.adminPasswordSubTab = pt.first }
                                .padding(horizontal = 10.dp, vertical = 6.dp)
                                .border(1.dp, if (isSel) Color.White else Color.Gray.copy(alpha = 0.3f), RoundedCornerShape(14.dp))
                        ) {
                            Text("${pt.second} (${pt.third})", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = if (isSel) Color.Black else Color.White)
                        }
                    }
                }
            }

            when (state.adminPasswordSubTab) {
                "REQUESTS" -> {
                    if (passwordRecoveryRequests.isEmpty()) {
                        item {
                            Card(
                                colors = CardDefaults.cardColors(containerColor = themeColors.surface),
                                modifier = Modifier.fillMaxWidth().padding(8.dp)
                            ) {
                                Box(modifier = Modifier.fillMaxWidth().padding(24.dp), contentAlignment = Alignment.Center) {
                                    Text("لا توجد طلبات استعادة كلمة مرور واردة حالياً ✅", color = Color.LightGray, fontSize = 12.sp)
                                }
                            }
                        }
                    } else {
                        items(passwordRecoveryRequests, key = { "rec_${it["id"]}" }) { req ->
                            val reqPhone = (req["phone"] as? String) ?: ""
                            val reqName = (req["name"] as? String) ?: "مستخدم"
                            val reqType = (req["accountType"] as? String) ?: "حساب"
                            val reqStatus = (req["status"] as? String) ?: "PENDING"
                            var newPassInput by remember { mutableStateOf((req["newPassword"] as? String) ?: "") }

                            Card(
                                colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
                                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                                border = BorderStroke(1.dp, if (reqStatus == "RESOLVED") Color(0xFF10B981) else Color(0xFFEF4444))
                            ) {
                                Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                                        Text("👤 $reqName", fontWeight = FontWeight.Bold, color = Color.White, fontSize = 13.sp)
                                        Surface(
                                            color = if (reqStatus == "RESOLVED") Color(0xFF10B981) else Color(0xFFEF4444),
                                            shape = RoundedCornerShape(8.dp)
                                        ) {
                                            Text(
                                                if (reqStatus == "RESOLVED") "تم التعيين والحل ✅" else "بانتظار التعيين ⌛",
                                                color = Color.White,
                                                fontSize = 10.sp,
                                                fontWeight = FontWeight.Bold,
                                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                            )
                                        }
                                    }

                                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                        Text("📱 الهاتف: $reqPhone", color = Color.LightGray, fontSize = 11.sp)
                                        Text("النوع: $reqType", color = themeColors.accent, fontSize = 11.sp)
                                    }

                                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
                                        OutlinedTextField(
                                            value = newPassInput,
                                            onValueChange = { newPassInput = it },
                                            label = { Text("كلمة المرور الجديدة", fontSize = 10.sp) },
                                            singleLine = true,
                                            modifier = Modifier.weight(1f),
                                            colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White)
                                        )
                                        Button(
                                            onClick = { newPassInput = (100000..999999).random().toString() },
                                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF8B5CF6)),
                                            contentPadding = PaddingValues(horizontal = 8.dp),
                                            modifier = Modifier.height(48.dp)
                                        ) {
                                            Text("توليد 🎲", fontSize = 10.sp, color = Color.White)
                                        }
                                        Button(
                                            onClick = {
                                                if (newPassInput.isNotBlank()) {
                                                    viewModel.adminResolvePasswordReset(context, reqPhone, newPassInput) {
                                                        Toast.makeText(context, "✅ تم اعتماد وتحديث كلمة المرور لـ $reqName", Toast.LENGTH_LONG).show()
                                                    }
                                                } else {
                                                    Toast.makeText(context, "الرجاء إدخال أو توليد كلمة المرور", Toast.LENGTH_SHORT).show()
                                                }
                                            },
                                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981)),
                                            contentPadding = PaddingValues(horizontal = 8.dp),
                                            modifier = Modifier.height(48.dp)
                                        ) {
                                            Text("اعتماد 💾", fontSize = 10.sp, color = Color.Black, fontWeight = FontWeight.Bold)
                                        }
                                    }

                                    Text("خيارات إرسال كلمة المرور للمستخدم فوراً:", fontSize = 10.sp, color = themeColors.accent, fontWeight = FontWeight.Bold)

                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        Button(
                                            onClick = {
                                                val text = "مرحباً $reqName، تم تعيين كلمة المرور الجديدة لحسابك في دليل اليمن: $newPassInput"
                                                val url = "https://wa.me/967${reqPhone.trim().removePrefix("0").removePrefix("+967")}?text=${Uri.encode(text)}"
                                                try {
                                                    context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
                                                } catch (e: Exception) {
                                                    Toast.makeText(context, "فشل فتح واتساب", Toast.LENGTH_SHORT).show()
                                                }
                                            },
                                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF25D366)),
                                            shape = RoundedCornerShape(8.dp),
                                            contentPadding = PaddingValues(horizontal = 4.dp),
                                            modifier = Modifier.weight(1f).height(48.dp)
                                        ) {
                                            Text("واتساب", fontSize = 9.sp, color = Color.White, fontWeight = FontWeight.Bold)
                                        }

                                        Button(
                                            onClick = {
                                                val text = "مرحباً $reqName، تم تعيين كلمة المرور الجديدة لحسابك في دليل اليمن: $newPassInput"
                                                val url = "https://t.me/share/url?url=https://yemen-services.app&text=${Uri.encode(text)}"
                                                try {
                                                    context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
                                                } catch (e: Exception) {
                                                    Toast.makeText(context, "فشل فتح تيليجرام", Toast.LENGTH_SHORT).show()
                                                }
                                            },
                                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0088CC)),
                                            shape = RoundedCornerShape(8.dp),
                                            contentPadding = PaddingValues(horizontal = 4.dp),
                                            modifier = Modifier.weight(1f).height(48.dp)
                                        ) {
                                            Text("تيليجرام", fontSize = 9.sp, color = Color.White, fontWeight = FontWeight.Bold)
                                        }

                                        Button(
                                            onClick = {
                                                val text = "كلمة المرور الجديدة لحسابك في دليل اليمن هي: $newPassInput"
                                                try {
                                                    val intent = Intent(Intent.ACTION_SENDTO, Uri.parse("smsto:$reqPhone")).apply {
                                                        putExtra("sms_body", text)
                                                    }
                                                    context.startActivity(intent)
                                                } catch (e: Exception) {
                                                    Toast.makeText(context, "فشل فتح الرسائل SMS", Toast.LENGTH_SHORT).show()
                                                }
                                            },
                                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF3B82F6)),
                                            shape = RoundedCornerShape(8.dp),
                                            contentPadding = PaddingValues(horizontal = 4.dp),
                                            modifier = Modifier.weight(1f).height(48.dp)
                                        ) {
                                            Text("SMS", fontSize = 9.sp, color = Color.White, fontWeight = FontWeight.Bold)
                                        }

                                        Button(
                                            onClick = {
                                                viewModel.triggerNotification("🔔 تم إرسال إشعار فوري لـ $reqName بكلمة المرور")
                                                Toast.makeText(context, "🔔 تم إرسال إشعار فوري داخل التطبيق!", Toast.LENGTH_SHORT).show()
                                            },
                                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF59E0B)),
                                            shape = RoundedCornerShape(8.dp),
                                            contentPadding = PaddingValues(horizontal = 4.dp),
                                            modifier = Modifier.weight(1.1f).height(48.dp)
                                        ) {
                                            Text("إشعار فوري 🔔", fontSize = 9.sp, color = Color.Black, fontWeight = FontWeight.Bold)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
                "SERVICES" -> {
                    if (activatedProviders.isEmpty()) {
                        item { Text("لا توجد حسابات فنيين مسجلة.", fontSize = 11.sp, color = Color.Gray, modifier = Modifier.padding(8.dp)) }
                    } else {
                        items(activatedProviders, key = { "pass_prov_${it.id}" }) { p ->
                            PasswordEntityCard(
                                name = p.name,
                                phone = p.phone,
                                category = "خدمات وفنيين",
                                password = p.password,
                                onResetPassword = { newPass ->
                                    viewModel.resetAccountPassword("PROVIDER", p.phone, newPass)
                                    Toast.makeText(context, "🔐 تم تحديث كلمة المرور لـ ${p.name}", Toast.LENGTH_SHORT).show()
                                },
                                context = context
                            )
                        }
                    }
                }
                "STORES", "RESTAURANTS", "MEDICAL" -> {
                    val filteredStores = stores.filter { s ->
                        val isMed = s.categoryId.contains("طبي") || s.categoryId.contains("عياد")
                        val isRest = s.categoryId.contains("مطعم") || s.categoryId.contains("كافيه")
                        when (state.adminPasswordSubTab) {
                            "MEDICAL" -> isMed
                            "RESTAURANTS" -> isRest
                            else -> !isMed && !isRest
                        }
                    }
                    if (filteredStores.isEmpty()) {
                        item { Text("لا توجد منشآت مسجلة في هذا القسم.", fontSize = 11.sp, color = Color.Gray, modifier = Modifier.padding(8.dp)) }
                    } else {
                        items(filteredStores, key = { "pass_store_${it.id}" }) { s ->
                            PasswordEntityCard(
                                name = s.name,
                                phone = s.phone,
                                category = s.categoryId,
                                password = s.password,
                                onResetPassword = { newPass ->
                                    viewModel.resetAccountPassword("STORE", s.phone, newPass)
                                    Toast.makeText(context, "🔐 تم تحديث كلمة المرور لـ ${s.name}", Toast.LENGTH_SHORT).show()
                                },
                                context = context
                            )
                        }
                    }
                }
                "PROPERTIES" -> {
                    if (properties.isEmpty()) {
                        item { Text("لا توجد عقارات مسجلة.", fontSize = 11.sp, color = Color.Gray, modifier = Modifier.padding(8.dp)) }
                    } else {
                        items(properties, key = { "pass_prop_${it.id}" }) { prop ->
                            PasswordEntityCard(
                                name = prop.title,
                                phone = prop.phone,
                                category = "عقار",
                                password = "",
                                onResetPassword = { newPass ->
                                    viewModel.resetAccountPassword("PROPERTY", prop.phone, newPass)
                                    Toast.makeText(context, "🔐 تم تحديث كلمة المرور لـ ${prop.title}", Toast.LENGTH_SHORT).show()
                                },
                                context = context
                            )
                        }
                    }
                }
                "JOBS" -> {
                    if (jobs.isEmpty()) {
                        item { Text("لا توجد وظائف مسجلة.", fontSize = 11.sp, color = Color.Gray, modifier = Modifier.padding(8.dp)) }
                    } else {
                        items(jobs, key = { "pass_job_${it.id}" }) { job ->
                            PasswordEntityCard(
                                name = job.title,
                                phone = job.phone,
                                category = "وظيفة",
                                password = "",
                                onResetPassword = { newPass ->
                                    viewModel.resetAccountPassword("JOB", job.phone, newPass)
                                    Toast.makeText(context, "🔐 تم تحديث كلمة المرور لـ ${job.title}", Toast.LENGTH_SHORT).show()
                                },
                                context = context
                            )
                        }
                    }
                }
            }
        }
        "SUPERVISORS" -> {
            item {
                Text("👥 إدارة حسابات المشرفين وصلاحيات التطبيق (مزامنة فورية)", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = themeColors.accent)
                Spacer(modifier = Modifier.height(4.dp))
            }

            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = themeColors.surface),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("أضف مشرفاً جديداً بكلمة مرور وصلاحية محددة:", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp)

                        OutlinedTextField(
                            value = state.supervisorInputName,
                            onValueChange = { state.supervisorInputName = it },
                            label = { Text("اسم المشرف الكامل") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
                            colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White)
                        )

                        OutlinedTextField(
                            value = state.supervisorInputPasscode,
                            onValueChange = { state.supervisorInputPasscode = it },
                            label = { Text("كلمة مرور الدخول") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
                            colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White)
                        )

                        Text("اختر الأدوار والصلاحيات الأمنية للمشرف المضاف (يمكنك اختيار صلاحية واحدة أو أكثر):", color = themeColors.textSecondary, fontSize = 10.sp)
                        val roles = listOf("SUPPORT" to "دعم فني", "AUDITOR" to "مدقق ومراقب", "ADMIN" to "مدير رئيسي", "OPERATIONS" to "عمليات")
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            roles.forEach { (roleKey, roleName) ->
                                val isSel = state.supervisorInputRole.split(",").contains(roleKey)
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(if (isSel) themeColors.accent else Color.Black.copy(alpha = 0.3f))
                                        .clickable {
                                            val currentSelected = state.supervisorInputRole.split(",").filter { it.isNotEmpty() }.toMutableList()
                                            if (currentSelected.contains(roleKey)) {
                                                currentSelected.remove(roleKey)
                                            } else {
                                                currentSelected.add(roleKey)
                                            }
                                            if (currentSelected.isEmpty()) {
                                                currentSelected.add("SUPPORT")
                                            }
                                            state.supervisorInputRole = currentSelected.joinToString(",")
                                        }
                                        .padding(vertical = 6.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(roleName, fontSize = 9.sp, color = if (isSel) Color.Black else Color.White, fontWeight = FontWeight.Bold)
                                }
                            }
                        }

                        AdminPermissionsSelectorView(
                            themeColors = themeColors,
                            selectedPermissions = state.supervisorInputPermissions,
                            onPermissionsChanged = { state.supervisorInputPermissions = it }
                        )

                        Button(
                            onClick = {
                                if (state.supervisorInputName.trim().isNotEmpty() && state.supervisorInputPasscode.trim().isNotEmpty()) {
                                    viewModel.addSupervisor(
                                        name = state.supervisorInputName.trim(),
                                        role = state.supervisorInputRole,
                                        passcode = state.supervisorInputPasscode.trim(),
                                        permissions = state.supervisorInputPermissions
                                    )
                                    state.supervisorInputName = ""
                                    state.supervisorInputPasscode = ""
                                    state.supervisorInputPermissions = emptyList()
                                } else {
                                    Toast.makeText(context, "الرجاء تعبئة اسم المشرف وكلمة المرور أولاً!", Toast.LENGTH_SHORT).show()
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = themeColors.accent),
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text("إضافة المشرف وتثبيت الصلاحيات 🛡️", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                        }
                    }
                }
            }

            if (supervisorsList.isNotEmpty()) {
                item {
                    Text("📋 قائمة المشرفين المسجلين في النظام الآن:", fontSize = 12.sp, color = Color.White, fontWeight = FontWeight.Bold)
                }

                items(supervisorsList, key = { it.id }) { sup ->
                    var showSupPasscode by remember { mutableStateOf(false) }
                    Card(
                        colors = CardDefaults.cardColors(containerColor = themeColors.surface),
                        modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp)
                    ) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(sup.name, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                    val displayRoles = sup.role.split(",").map { r ->
                                        when (r.trim()) {
                                            "ADMIN" -> "مدير رئيسي 👑"
                                            "AUDITOR" -> "مدقق ومراقب 🔍"
                                            "OPERATIONS" -> "عمليات ميدانية 🚗"
                                            "SUPPORT" -> "دعم فني 📞"
                                            else -> r.trim()
                                        }
                                    }.joinToString(" + ")
                                    Text("الصلاحيات الممنوحة: $displayRoles (${if (sup.permissions.isEmpty()) "كاملة/افتراضية" else "${sup.permissions.size}/538 مخصصة"})", fontSize = 10.sp, color = themeColors.accent)
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text("رمز الدخول (Passcode): ${if (showSupPasscode) sup.passcodeHash else "••••••••"}", fontSize = 11.sp, color = themeColors.textSecondary)
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            text = if (showSupPasscode) "🔒 إخفاء" else "👁️ إظهار",
                                            fontSize = 9.sp,
                                            color = Color.Yellow,
                                            modifier = Modifier.clickable { showSupPasscode = !showSupPasscode }
                                        )
                                    }
                                }

                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    IconButton(
                                        onClick = { state.editingSupervisorObj = sup }
                                    ) {
                                        Icon(imageVector = Icons.Default.Edit, contentDescription = "تعديل المشرف", tint = Color.Green, modifier = Modifier.size(20.dp))
                                    }
                                    IconButton(
                                        onClick = { viewModel.removeSupervisor(sup.id) }
                                    ) {
                                        Icon(imageVector = Icons.Default.Delete, contentDescription = "حذف المشرف", tint = Color.Red, modifier = Modifier.size(20.dp))
                                    }
                                }
                            }
                        }
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(10.dp))
                Text("🛡️ مصفوفة الصلاحيات الشاملة للنظام (538 صلاحية موزعة على 18 مجالاً)", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = themeColors.accent)
                Spacer(modifier = Modifier.height(4.dp))
            }

            item {
                var showAll538Matrix by rememberSaveable { mutableStateOf(false) }
                var matrixSearchQuery by rememberSaveable { mutableStateOf("") }

                Card(
                    colors = CardDefaults.cardColors(containerColor = themeColors.surface),
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, themeColors.accent.copy(alpha = 0.4f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Lock,
                                    contentDescription = null,
                                    tint = themeColors.accent,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    "538 صلاحية أمنية معتمدة ومتزامنة",
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp
                                )
                            }

                            Button(
                                onClick = { showAll538Matrix = !showAll538Matrix },
                                colors = ButtonDefaults.buttonColors(containerColor = if (showAll538Matrix) Color.DarkGray else themeColors.accent),
                                shape = RoundedCornerShape(8.dp),
                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    text = if (showAll538Matrix) "إخفاء المصفوفة ▲" else "عرض الكل (538) ▼",
                                    color = if (showAll538Matrix) Color.White else Color.Black,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        Text(
                            text = "تغطي المنظومة 38 مجالاً وقطاعاً إدارياً شاملاً يضم 538 صلاحية دقيقة ومفصلة: البوابة الخلفية، طلبات الانضمام، الإضافة اليدوية، المحلات، المطاعم، المراكز الطبية، العقارات، الوظائف، المتقدمين، الإحصائيات، الحجوزات، المحادثات، أعضاء الدليل، كلمات المرور، البنرات، الأقسام، المدن، الشكاوى، ترقيات VIP، المشرفين، الألوان، الإشعارات، النسخ الاحتياطي، تنظيف البيانات، التقييمات، المكالمات، الكوبونات، المحظورين، المحذوفات، المدفوعات، التبويبات المخصصة، الأيقونات الذهبية، الدردشات المتقدمة، تخصيص البطاقات، منشئ الأقسام، استمارات التسجيل، الصلاحيات والأدوار، وتحكم الخرائط.",
                            color = themeColors.textSecondary,
                            fontSize = 10.sp,
                            lineHeight = 14.sp
                        )

                        if (showAll538Matrix) {
                            OutlinedTextField(
                                value = matrixSearchQuery,
                                onValueChange = { matrixSearchQuery = it },
                                label = { Text("بحث في مصفوفة الصلاحيات (الاسم أو الرمز)...", fontSize = 10.sp) },
                                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(16.dp)) },
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth(),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedTextColor = Color.White,
                                    unfocusedTextColor = Color.White
                                )
                            )

                            PermissionCategory.values().forEach { cat ->
                                var catItems = AdminPermissionsRegistry.getByCategory(cat)
                                if (matrixSearchQuery.isNotBlank()) {
                                    val q = matrixSearchQuery.trim().lowercase()
                                    catItems = catItems.filter {
                                        it.name.lowercase().contains(q) ||
                                            it.key.lowercase().contains(q) ||
                                            it.description.lowercase().contains(q)
                                    }
                                }

                                if (catItems.isNotEmpty()) {
                                    Card(
                                        colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A)),
                                        modifier = Modifier.fillMaxWidth().padding(vertical = 3.dp)
                                    ) {
                                        Column(modifier = Modifier.padding(8.dp)) {
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween
                                            ) {
                                                Text(
                                                    text = "${cat.iconEmoji} ${cat.arabicTitle}",
                                                    color = themeColors.accent,
                                                    fontWeight = FontWeight.Bold,
                                                    fontSize = 11.sp
                                                )
                                                Text(
                                                    text = "${catItems.size} صلاحية",
                                                    color = Color.Gray,
                                                    fontSize = 10.sp
                                                )
                                            }

                                            catItems.forEach { perm ->
                                                Row(
                                                    modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp),
                                                    horizontalArrangement = Arrangement.SpaceBetween,
                                                    verticalAlignment = Alignment.CenterVertically
                                                ) {
                                                    Column(modifier = Modifier.weight(1f)) {
                                                        Text(
                                                            text = "${perm.id}. ${perm.name}",
                                                            color = Color.White,
                                                            fontSize = 10.sp,
                                                            fontWeight = FontWeight.SemiBold
                                                        )
                                                        Text(
                                                            text = perm.description,
                                                            color = Color.Gray,
                                                            fontSize = 8.sp,
                                                            maxLines = 1
                                                        )
                                                    }
                                                    Text(
                                                        text = perm.key,
                                                        color = themeColors.accent.copy(alpha = 0.8f),
                                                        fontSize = 8.sp,
                                                        fontWeight = FontWeight.Bold
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(10.dp))
                Text("🎨 نظام الألوان والسمات المتعددة المتزامن فورياً", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = themeColors.accent)
                Spacer(modifier = Modifier.height(4.dp))
            }

            item {
                var newPaletteName by rememberSaveable { mutableStateOf("") }
                var newPalettePrimary by rememberSaveable { mutableStateOf("#059669") }
                var newPaletteSecondary by rememberSaveable { mutableStateOf("#115E59") }

                Card(
                    colors = CardDefaults.cardColors(containerColor = themeColors.surface),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("أضف ستايل لوني مخصص ومثبت بالدليل:", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp)

                        OutlinedTextField(
                            value = newPaletteName,
                            onValueChange = { newPaletteName = it },
                            label = { Text("اسم لوحة الألوان (مثال: الشتاء المتجمد)") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
                            colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White)
                        )

                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedTextField(
                                value = newPalettePrimary,
                                onValueChange = { newPalettePrimary = it },
                                label = { Text("اللون الأساسي (Primary)") },
                                singleLine = true,
                                modifier = Modifier.weight(1f),
                                colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White)
                            )

                            OutlinedTextField(
                                value = newPaletteSecondary,
                                onValueChange = { newPaletteSecondary = it },
                                label = { Text("اللون الثانوي (Secondary)") },
                                singleLine = true,
                                modifier = Modifier.weight(1f),
                                colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White)
                            )
                        }

                        Button(
                            onClick = {
                                if (newPaletteName.trim().isNotEmpty() && newPalettePrimary.trim().isNotEmpty() && newPaletteSecondary.trim().isNotEmpty()) {
                                    viewModel.addColorPalette(newPaletteName.trim(), newPalettePrimary.trim(), newPaletteSecondary.trim())
                                    newPaletteName = ""
                                } else {
                                    Toast.makeText(context, "الرجاء تعبئة الاسم والألوان الستة عشرية بالكامل!", Toast.LENGTH_SHORT).show()
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = themeColors.accent),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("حقن وإضافة بالدليل المتكامل للألوان", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                        }
                    }
                }
            }

            if (colorPalettesList.isNotEmpty()) {
                item {
                    Text("🌈 لوحات الألوان والسمات المضافة حديثاً بالدليل:", fontSize = 12.sp, color = Color.White, fontWeight = FontWeight.Bold)
                }

                items(colorPalettesList, key = { it.id }) { pal ->
                    Card(
                        colors = CardDefaults.cardColors(containerColor = themeColors.surface),
                        modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(10.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(20.dp)
                                        .clip(CircleShape)
                                        .background(try { Color(android.graphics.Color.parseColor(pal.primaryHex)) } catch (e: Exception) { Color.Gray })
                                )
                                Box(
                                    modifier = Modifier
                                        .size(20.dp)
                                        .clip(CircleShape)
                                        .background(try { Color(android.graphics.Color.parseColor(pal.secondaryHex)) } catch (e: Exception) { Color.DarkGray })
                                )

                                Column {
                                    Text(pal.name, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                    Text("رئيسي: ${pal.primaryHex} | ثانوي: ${pal.secondaryHex}", fontSize = 9.sp, color = themeColors.textSecondary)
                                }
                            }

                            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                IconButton(
                                    onClick = {
                                        val updatedSettings = settingsState.copy(
                                            activeThemeId = "CUSTOM_THEME",
                                            customPrimaryHex = pal.primaryHex,
                                            customSecondaryHex = pal.secondaryHex,
                                            customBackgroundHex = pal.backgroundHex,
                                            customSurfaceHex = pal.surfaceHex
                                        )
                                        viewModel.saveCustomSettingsState(updatedSettings)
                                        Toast.makeText(context, "🌈 تم تطبيق هذا السطح اللوني الآن ومزامنته فوراً!", Toast.LENGTH_SHORT).show()
                                    }
                                ) {
                                    Icon(imageVector = Icons.Default.Check, contentDescription = "تطبيق فوري", tint = Color.Green, modifier = Modifier.size(20.dp))
                                }

                                IconButton(
                                    onClick = { viewModel.deleteColorPalette(pal.id) }
                                ) {
                                    Icon(imageVector = Icons.Default.Delete, contentDescription = "حذف اللون", tint = Color.Red, modifier = Modifier.size(20.dp))
                                }
                            }
                        }
                    }
                }
            }
        }
        "BACKUP" -> {
            item {
                Text("💾 لوحة النسخ الاحتياطي والمزامنة والجدولة والتقارير", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = themeColors.accent)
                Text("أدوات التصدير الشامل للبيانات والتحقق من صحة الاتصال المتزامن مع خوادم Cloud Firestore:", fontSize = 11.sp, color = themeColors.textSecondary)
                Spacer(modifier = Modifier.height(8.dp))
            }

            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = themeColors.surface),
                    modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                    border = BorderStroke(1.dp, Color.Green.copy(alpha = 0.3f))
                ) {
                    Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("🛡️ إحصائيات حالة المزامنة والاتصال الحي", fontSize = 12.sp, color = Color.Green, fontWeight = FontWeight.Bold)

                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("الحالة الفورية:", fontSize = 11.sp, color = Color.White)
                            Text("متصل وآمن 🟢", fontSize = 11.sp, color = Color.Green, fontWeight = FontWeight.Bold)
                        }
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("حجم البيانات النشطة:", fontSize = 11.sp, color = Color.White)
                            val sizeEst = (activatedProviders.size + categories.size + bookings.size + reports.size) * 1.5f
                            Text(String.format("%.2f KB", sizeEst), fontSize = 11.sp, color = Color.White)
                        }
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("تردد نبض الاتصال:", fontSize = 11.sp, color = Color.White)
                            Text("كل 10 ثوانٍ (ذكي تلقائي)", fontSize = 11.sp, color = themeColors.accent)
                        }

                        Spacer(modifier = Modifier.height(4.dp))

                        Button(
                            onClick = {
                                viewModel.triggerManualSync(context)
                                viewModel.refreshData(true)
                                Toast.makeText(context, "🔄 تم بدء فحص ومزامنة كامل جداول البيانات مع السحاب!", Toast.LENGTH_SHORT).show()
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = themeColors.accent),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("تحديث وإعادة جدولة الفحص الفوري 🔄", color = Color.Black, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = themeColors.surface),
                    modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                    border = BorderStroke(1.dp, themeColors.accent.copy(alpha = 0.4f))
                ) {
                    Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("💾 نظام النسخ الاحتياطي التلقائي واستيراد البيانات", fontSize = 12.sp, color = themeColors.accent, fontWeight = FontWeight.Bold)
                        Text("يقوم النظام تلقائياً بجدولة نسخ كامل الجداول وقواعد البيانات لضمان عدم ضياع البيانات الفنية والحجوزات والمتاجر والعقارات والكلمات المشفرة.", fontSize = 11.sp, color = Color.LightGray)

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Button(
                                onClick = {
                                    viewModel.createSystemBackup { success, jsonStr ->
                                        if (success) {
                                            state.backupJsonStringState = jsonStr
                                            val path = viewModel.saveBackupToLocalStorage(context, jsonStr, "yemen_services_backup_${System.currentTimeMillis()}")
                                            if (path.isNotEmpty()) {
                                                Toast.makeText(context, "✅ تم حفظ النسخة الاحتياطية بذاكرة الهاتف/SD Card:\n$path", Toast.LENGTH_LONG).show()
                                            } else {
                                                Toast.makeText(context, "✅ تم إنشاء النسخة الاحتياطية بنجاح!", Toast.LENGTH_SHORT).show()
                                            }
                                        } else {
                                            Toast.makeText(context, "❌ فشل إنشاء النسخة الاحتياطية", Toast.LENGTH_SHORT).show()
                                        }
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981)),
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("حفظ بالهاتف/SD Card 💾", color = Color.Black, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            }

                            Button(
                                onClick = {
                                    viewModel.createSystemBackup { success, jsonStr ->
                                        if (success) {
                                            val clipboardManager = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                            val clipData = android.content.ClipData.newPlainText("YemenServiceBackup", jsonStr)
                                            clipboardManager.setPrimaryClip(clipData)
                                            Toast.makeText(context, "📋 تم نسخ كود الاحتياط الكامل للذاكرة بنجاح!", Toast.LENGTH_LONG).show()
                                        }
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = themeColors.accent),
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("نسخ الكود الكامل 📋", color = Color.Black, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            }
                        }

                        if (state.backupJsonStringState.isNotEmpty()) {
                            Text("تم توليد الكود الاحتياطي بنجاح (${state.backupJsonStringState.length} حرفاً). احتفظ به في مكان آمن.", fontSize = 10.sp, color = Color.Green)
                        }

                        Divider(color = Color.Gray.copy(alpha = 0.5f), thickness = 1.dp)

                        Text("📥 استعادة النظام من نسخة احتياطية سابقة (ملف أو كود)", fontSize = 12.sp, color = Color.White, fontWeight = FontWeight.Bold)

                        val backupFilePickerLauncher = rememberLauncherForActivityResult(
                            contract = ActivityResultContracts.GetContent()
                        ) { uri: Uri? ->
                            uri?.let {
                                try {
                                    var fileText = ""
                                    val contentResolver = context.contentResolver
                                    val mimeType = contentResolver.getType(it) ?: ""
                                    if (mimeType.contains("zip") || it.toString().endsWith(".zip", ignoreCase = true)) {
                                        contentResolver.openInputStream(it)?.use { inputStream ->
                                            ZipInputStream(inputStream).use { zipInput ->
                                                var entry = zipInput.nextEntry
                                                while (entry != null) {
                                                    if (!entry.isDirectory && (entry.name.endsWith(".json", ignoreCase = true) || entry.name.endsWith(".txt", ignoreCase = true))) {
                                                        fileText = zipInput.bufferedReader().use { r -> r.readText() }
                                                        break
                                                    }
                                                    entry = zipInput.nextEntry
                                                }
                                            }
                                        }
                                    } else {
                                        contentResolver.openInputStream(it)?.use { inputStream ->
                                            fileText = inputStream.bufferedReader().use { r -> r.readText() }
                                        }
                                    }

                                    if (fileText.isNotBlank()) {
                                        state.restoreJsonInputState = fileText
                                        Toast.makeText(context, "✅ تم استخراج وقراءة ملف النسخة الاحتياطية بنجاح! اضغط تأكيد الاستعادة أدناه.", Toast.LENGTH_LONG).show()
                                    } else {
                                        Toast.makeText(context, "⚠️ الملف المحدد فارغ أو غير متوافق!", Toast.LENGTH_SHORT).show()
                                    }
                                } catch (e: Exception) {
                                    Toast.makeText(context, "❌ فشل قراءة الملف: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
                                }
                            }
                        }

                        Button(
                            onClick = { backupFilePickerLauncher.launch("*/*") },
                            colors = ButtonDefaults.buttonColors(containerColor = themeColors.accent),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("📂 اختيار ملف النسخة الاحتياطية من ذاكرة الهاتف / Drive (ZIP, JSON, TXT)", color = Color.Black, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }

                        OutlinedTextField(
                            value = state.restoreJsonInputState,
                            onValueChange = { state.restoreJsonInputState = it },
                            label = { Text("أو أدخل/الصق كود النسخة الاحتياطية JSON مباشرة هنا") },
                            modifier = Modifier.fillMaxWidth(),
                            maxLines = 5,
                            textStyle = TextStyle(fontSize = 11.sp, fontFamily = FontFamily.Monospace),
                            colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White)
                        )

                        Button(
                            onClick = {
                                if (state.restoreJsonInputState.trim().isEmpty()) {
                                    Toast.makeText(context, "⚠️ يرجى اختيار ملف أو لصق كود النسخة أولاً!", Toast.LENGTH_SHORT).show()
                                } else {
                                    viewModel.restoreSystemFromBackup(state.restoreJsonInputState) { success, msg ->
                                        if (success) {
                                            state.restoreJsonInputState = ""
                                            Toast.makeText(context, "💚 تم استعادة كامل البيانات والمزامنة السحابية بنجاح بنسبة 100%!", Toast.LENGTH_LONG).show()
                                        } else {
                                            Toast.makeText(context, "❌ فشل استعادة البيانات: $msg", Toast.LENGTH_LONG).show()
                                        }
                                    }
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color.Red),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("تأكيد استعادة قواعد البيانات ومزامنتها سحابياً ⚠️", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }

                        Divider(color = Color.Gray.copy(alpha = 0.5f), thickness = 1.dp)

                        Text("🔥 ربط وإدارة المزامنة المزدوجة مع حساب Firebase ثانوي", fontSize = 12.sp, color = Color(0xFFF59E0B), fontWeight = FontWeight.Bold)

                        var secProjId by remember { mutableStateOf("") }
                        var secApiKey by remember { mutableStateOf("") }
                        var secAppId by remember { mutableStateOf("") }
                        var secBucket by remember { mutableStateOf("") }
                        var secEnabled by remember { mutableStateOf(false) }

                        LaunchedEffect(Unit) {
                            viewModel.loadSecondaryFirebaseConfig { projId, apiKey, appId, bucket, enabled ->
                                secProjId = projId
                                secApiKey = apiKey
                                secAppId = appId
                                secBucket = bucket
                                secEnabled = enabled
                            }
                        }

                        OutlinedTextField(
                            value = secProjId,
                            onValueChange = { secProjId = it },
                            label = { Text("Project ID الحساب الثانوي", fontSize = 10.sp) },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Color(0xFFF59E0B), focusedTextColor = Color.White, unfocusedTextColor = Color.White)
                        )
                        OutlinedTextField(
                            value = secApiKey,
                            onValueChange = { secApiKey = it },
                            label = { Text("API Key الحساب الثانوي", fontSize = 10.sp) },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Color(0xFFF59E0B), focusedTextColor = Color.White, unfocusedTextColor = Color.White)
                        )

                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                            Switch(
                                checked = secEnabled,
                                onCheckedChange = { secEnabled = it },
                                colors = adminSwitchColors(themeColors)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("تفعيل المزامنة المزدوجة التلقائية مع Firebase الثانوي ⚡", fontSize = 11.sp, color = Color.White, fontWeight = FontWeight.Bold)
                        }

                        Button(
                            onClick = {
                                viewModel.setSecondaryFirebaseConfig(secProjId, secApiKey, secAppId, secBucket, secEnabled)
                                Toast.makeText(context, "⚡ تم حفظ إعدادات المزامنة الثانوية لـ Firebase بنجاح!", Toast.LENGTH_SHORT).show()
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF59E0B)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("حفظ وتحديث إعدادات المزامنة المزدوجة 💾", color = Color.Black, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = themeColors.surface),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("📁 تصدير التقارير الإدارية الشاملة للجمهورية", fontSize = 12.sp, color = Color.White, fontWeight = FontWeight.Bold)

                        Button(
                            onClick = {
                                val csv = buildString {
                                    appendLine("ID,Name,Phone,Profession,Area,CityId,Status,Verified,VIP,Rating")
                                    activatedProviders.forEach { p ->
                                        appendLine("\"${p.id}\",\"${p.name}\",\"${p.phone}\",\"${p.profession}\",\"${p.area}\",\"${p.cityId}\",\"${p.subscriptionStatus}\",${p.isVerified},${p.isVip},${p.rating}")
                                    }
                                }
                                val exported = writeTextFileToExportsAndShare(
                                    context = context,
                                    fileName = "yemen_providers_${System.currentTimeMillis()}.csv",
                                    content = csv,
                                    mimeType = "text/csv",
                                    chooserTitle = "تصدير الدليل الكامل للفنيين (CSV)"
                                )
                                Toast.makeText(
                                    context,
                                    if (exported != null) "✅ تم تصدير ومشاركة ${activatedProviders.size} سجل بنجاح!" else "❌ تعذر إنشاء ملف CSV",
                                    Toast.LENGTH_LONG
                                ).show()
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("تصدير الدليل الكامل للفنيين (CSV)", color = Color.Black, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }

                        Button(
                            onClick = {
                                val csv = buildString {
                                    appendLine("BookingCode,UserName,UserPhone,ProviderName,Area,Status,PaymentStatus,TotalAmount,Commission")
                                    bookings.forEach { b ->
                                        val amt = if (b.totalAmount > 0.0) b.totalAmount else b.price
                                        val comm = amt * settingsState.paymentCommissionRate.toDouble()
                                        appendLine("\"${b.bookingNumber}\",\"${b.effectiveCustomerName}\",\"${b.effectiveCustomerPhone}\",\"${b.providerName}\",\"${b.effectiveCustomerArea}\",\"${b.status}\",\"${b.paymentStatus}\",$amt,$comm")
                                    }
                                }
                                val exported = writeTextFileToExportsAndShare(
                                    context = context,
                                    fileName = "yemen_bookings_${System.currentTimeMillis()}.csv",
                                    content = csv,
                                    mimeType = "text/csv",
                                    chooserTitle = "تصدير سجل الحجوزات النشطة (CSV)"
                                )
                                Toast.makeText(
                                    context,
                                    if (exported != null) "✅ تم تصدير ومشاركة ${bookings.size} حجز بنجاح!" else "❌ تعذر إنشاء ملف الحجوزات",
                                    Toast.LENGTH_LONG
                                ).show()
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF3B82F6)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("تصدير سجل الحجوزات النشطة (CSV)", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
        "CLEAN" -> {
            item {
                Spacer(modifier = Modifier.height(10.dp))
                Text("⚙️ تخصيص تصفية وتهيئة البيانات الفورية", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = themeColors.accent)
                Text("حدد أنواع البيانات التي تريد مسحها أو نسخها وحفظها احتياطياً:", fontSize = 11.sp, color = themeColors.textSecondary)
                Spacer(modifier = Modifier.height(8.dp))
            }

            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = themeColors.surface),
                    border = BorderStroke(1.dp, themeColors.accent.copy(alpha = 0.3f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("📁 تحديد الكل لإجراء العملية الشاملة", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.White)
                            Switch(
                                checked = state.wipeProvidersChecked && state.wipeBookingsChecked && state.wipeChatsChecked && state.wipeNotifsChecked && state.wipeReportsChecked && state.wipePendingChecked && state.wipeBannersChecked && state.wipeSupervisorsChecked && state.wipeCitiesChecked && state.wipeThemesChecked,
                                onCheckedChange = { allChecked ->
                                    state.wipeProvidersChecked = allChecked
                                    state.wipeBookingsChecked = allChecked
                                    state.wipeChatsChecked = allChecked
                                    state.wipeNotifsChecked = allChecked
                                    state.wipeReportsChecked = allChecked
                                    state.wipeCategoriesChecked = allChecked
                                    state.wipePendingChecked = allChecked
                                    state.wipeBannersChecked = allChecked
                                    state.wipeSupervisorsChecked = allChecked
                                    state.wipeCitiesChecked = allChecked
                                    state.wipeThemesChecked = allChecked
                                },
                                colors = adminSwitchColors(themeColors)
                            )
                        }

                        Divider(color = themeColors.accent.copy(alpha = 0.2f))

                        val itemsList = listOf(
                            Triple("الفنيين ومقدمي الخدمات الحقيقيين 👤", state.wipeProvidersChecked) { v: Boolean -> state.wipeProvidersChecked = v },
                            Triple("الحجوزات والطلبات المجدولة 📅", state.wipeBookingsChecked) { v: Boolean -> state.wipeBookingsChecked = v },
                            Triple("المحادثات وقنوات الشات المتبادلة 💬", state.wipeChatsChecked) { v: Boolean -> state.wipeChatsChecked = v },
                            Triple("الإشعارات الموجهة والمرسلة 🔔", state.wipeNotifsChecked) { v: Boolean -> state.wipeNotifsChecked = v },
                            Triple("بلاغات الشكاوى ومشاكل المستخدمين ⚠️", state.wipeReportsChecked) { v: Boolean -> state.wipeReportsChecked = v },
                            Triple("الأقسام وتصنيفات المهن (حذر) 📂", state.wipeCategoriesChecked) { v: Boolean -> state.wipeCategoriesChecked = v },
                            Triple("طلبات الانضمام والتسجيل المعلقة 📝", state.wipePendingChecked) { v: Boolean -> state.wipePendingChecked = v },
                            Triple("الإعلانات وبنرات واجهة التطبيق 📢", state.wipeBannersChecked) { v: Boolean -> state.wipeBannersChecked = v },
                            Triple("المشرفين والإداريين المساعدين 🔑", state.wipeSupervisorsChecked) { v: Boolean -> state.wipeSupervisorsChecked = v },
                            Triple("المحافظات والمدن المعتمدة 🗺️", state.wipeCitiesChecked) { v: Boolean -> state.wipeCitiesChecked = v },
                            Triple("قوالب الألوان المخصصة 🎨", state.wipeThemesChecked) { v: Boolean -> state.wipeThemesChecked = v }
                        )

                        itemsList.forEach { (label, isChecked, setChecked) ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { setChecked(!isChecked) }
                                    .padding(vertical = 4.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(label, fontSize = 12.sp, color = Color.White)
                                Checkbox(
                                    checked = isChecked,
                                    onCheckedChange = { setChecked(it) },
                                    colors = CheckboxDefaults.colors(checkedColor = themeColors.accent)
                                )
                            }
                        }
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(10.dp))
                Text("📥 تصدير ونسخ البيانات المحددة (نسخة احتياطية):", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = themeColors.accent)
                Spacer(modifier = Modifier.height(4.dp))
            }

            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = themeColors.surface),
                    border = BorderStroke(1.dp, themeColors.accent.copy(alpha = 0.2f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            text = "يمكنك حفظ البيانات المحددة ونقلها فوراً إلى أي هاتف آخر أو نسخها لحساب جوجل درايف الخاص بك لحفظها من الفقدان:",
                            fontSize = 11.sp,
                            color = themeColors.textSecondary
                        )

                        Button(
                            onClick = {
                                val selectedCols = buildSelectedCollectionsList(state)
                                viewModel.exportSelectedCollectionsAsJson(selectedCols) { jsonString ->
                                    val clipboardManager = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                    val clipData = android.content.ClipData.newPlainText("YemenService_Backup_JSON", jsonString)
                                    clipboardManager.setPrimaryClip(clipData)
                                    Toast.makeText(context, "📋 تم نسخ البيانات المحددة بصيغة JSON بنجاح إلى ذاكرة الهاتف!", Toast.LENGTH_SHORT).show()
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = themeColors.primary),
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Icon(imageVector = Icons.Default.Share, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("📋 نسخ البيانات المحددة للحافظة (Clipboard)", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }

                        Button(
                            onClick = {
                                val selectedCols = buildSelectedCollectionsList(state)
                                viewModel.exportSelectedCollectionsAsJson(selectedCols) { jsonString ->
                                    val shareIntent = Intent(Intent.ACTION_SEND).apply {
                                        type = "text/plain"
                                        putExtra(Intent.EXTRA_SUBJECT, "نسخة احتياطية - دليل خدمات اليمن")
                                        putExtra(Intent.EXTRA_TEXT, jsonString)
                                    }
                                    context.startActivity(Intent.createChooser(shareIntent, "تصدير نسخة البيانات الاحتياطية إلى:"))
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981)),
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Icon(imageVector = Icons.Default.Share, contentDescription = null, tint = Color.Black, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("☁️ نسخ لجوجل درايف / ذاكرة الهاتف ومشاركتها", color = Color.Black, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(10.dp))
                Text("🚨 منطقة التطهير الكلي والمسح النهائي:", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.Red)
                Spacer(modifier = Modifier.height(4.dp))
            }

            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF451A03)),
                    border = BorderStroke(1.dp, Color.Red),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text("تنبيه أمني صارم للغاية!", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color.Red)
                        Text(
                            text = "الضغط على الزر أدناه سيقوم بحذف وإعادة تهيئة الأنواع المحددة أعلاه فقط فوراً! لن يتسنى لك مراجعة أو التراجع عن هذه البيانات بمجرد تأكيد المسح بالرمز السري.",
                            fontSize = 11.sp,
                            color = Color.White
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Button(
                            onClick = { state.showWipeConfirmDialog = true },
                            colors = ButtonDefaults.buttonColors(containerColor = Color.Red),
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text("مسح كامل البيانات وإعادة بناء الدليل العظيم", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }
                    }
                }
            }
        }
        "BLOCKED" -> {
            val blockedProviders = activatedProviders.filter { it.isBlocked }

            item {
                Spacer(modifier = Modifier.height(10.dp))
                Text("🚫 إدارة ومراقبة الفنيين المحظورين", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = themeColors.accent)
                Text("استعراض وإلغاء حظر مقدمي الخدمات الموقوفين لأسباب إدارية أو بلاغات:", fontSize = 11.sp, color = themeColors.textSecondary)
                Spacer(modifier = Modifier.height(8.dp))
            }

            if (blockedProviders.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("🎉 لا يوجد أي فني محظور حالياً بالمنصة.", fontSize = 12.sp, color = Color.Gray)
                    }
                }
            } else {
                item {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = themeColors.surface),
                        border = BorderStroke(1.dp, themeColors.accent.copy(alpha = 0.3f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            blockedProviders.forEach { provider ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 8.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column {
                                        Text("👤 الفني: ${provider.name}", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                        Text("📞 هاتف: ${provider.phone} | المهنة: ${provider.profession}", fontSize = 11.sp, color = Color.LightGray)
                                        Text("📍 المنطقة: ${provider.area}", fontSize = 10.sp, color = themeColors.textSecondary)
                                    }

                                    Button(
                                        onClick = { viewModel.toggleProviderBlock(provider.id) },
                                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981))
                                    ) {
                                        Text("إلغاء الحظر ✅", color = Color.Black, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                                Divider(color = Color.White.copy(alpha = 0.05f))
                            }
                        }
                    }
                }
            }
        }
        "DELETED" -> {
            item {
                Spacer(modifier = Modifier.height(10.dp))
                Text("🗑️ إدارة ومراقبة الفنيين المحذوفين", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = themeColors.accent)
                Text("استعراض واستعادة مقدمي الخدمات الذين تم حذفهم منطقياً من الدليل:", fontSize = 11.sp, color = themeColors.textSecondary)
                Spacer(modifier = Modifier.height(8.dp))
            }

            if (deletedList.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("🎉 لا يوجد أي فني محذوف منطقياً حالياً.", fontSize = 12.sp, color = Color.Gray)
                    }
                }
            } else {
                item {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = themeColors.surface),
                        border = BorderStroke(1.dp, themeColors.accent.copy(alpha = 0.3f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            deletedList.forEach { provider ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 8.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text("👤 الفني: ${provider.name}", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                        Text("📞 هاتف: ${provider.phone} | المهنة: ${provider.profession}", fontSize = 11.sp, color = Color.LightGray)
                                        Text("📍 المنطقة: ${provider.area}", fontSize = 10.sp, color = themeColors.textSecondary)
                                    }

                                    Button(
                                        onClick = { viewModel.restoreProvider(provider.id) },
                                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981)),
                                        modifier = Modifier.padding(start = 8.dp)
                                    ) {
                                        Text("استعادة حساب الفني 🟢", color = Color.Black, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                                Divider(color = Color.White.copy(alpha = 0.05f))
                            }
                        }
                    }
                }
            }
        }
        "FINANCIAL_REPORTS" -> {
            item {
                val completedBookings = bookings.filter { it.status.equals("COMPLETED", ignoreCase = true) || it.paymentStatus.equals("PAID", ignoreCase = true) }
                val totalGrossRevenue = bookings.sumOf { if (it.totalAmount > 0.0) it.totalAmount else it.price }
                val totalCommission = bookings.sumOf {
                    val amt = if (it.totalAmount > 0.0) it.totalAmount else it.price
                    amt * settingsState.paymentCommissionRate.toDouble()
                }
                Card(colors = CardDefaults.cardColors(containerColor = themeColors.surface), modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("📈 تقارير الأرباح المتقدمة", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = themeColors.accent)
                        Text("إجمالي الطلبات والحجوزات: ${bookings.size} (مكتملة/مدفوعة: ${completedBookings.size})", fontSize = 12.sp, color = Color.White)
                        Text("إجمالي حجم المعاملات المسجلة: ${String.format("%.2f", totalGrossRevenue)} ريال يمني", fontSize = 12.sp, color = Color.LightGray)
                        Text(
                            "إجمالي العمولات الفعلية (${(settingsState.paymentCommissionRate * 100).toInt()}%): ${String.format("%.2f", totalCommission)} ريال يمني",
                            fontSize = 12.sp,
                            color = Color(0xFF10B981),
                            fontWeight = FontWeight.Bold
                        )
                        Button(
                            onClick = {
                                try {
                                    val pdf = android.graphics.pdf.PdfDocument()
                                    val pageInfo = android.graphics.pdf.PdfDocument.PageInfo.Builder(595, 842, 1).create()
                                    val page = pdf.startPage(pageInfo)
                                    val canvas = page.canvas
                                    val titlePaint = android.graphics.Paint().apply {
                                        color = android.graphics.Color.BLACK
                                        textSize = 18f
                                        isFakeBoldText = true
                                    }
                                    val bodyPaint = android.graphics.Paint().apply {
                                        color = android.graphics.Color.DKGRAY
                                        textSize = 12f
                                    }
                                    var y = 60f
                                    canvas.drawText("Yemen Services Financial Report", 40f, y, titlePaint)
                                    y += 28f
                                    canvas.drawText("Generated: ${java.text.SimpleDateFormat("yyyy-MM-dd HH:mm", java.util.Locale.US).format(java.util.Date())}", 40f, y, bodyPaint)
                                    y += 22f
                                    canvas.drawText("Total Bookings: ${bookings.size} | Completed/Paid: ${completedBookings.size}", 40f, y, bodyPaint)
                                    y += 22f
                                    canvas.drawText("Gross Transaction Volume: ${String.format(java.util.Locale.US, "%.2f", totalGrossRevenue)} YER", 40f, y, bodyPaint)
                                    y += 22f
                                    canvas.drawText("Platform Commission (${(settingsState.paymentCommissionRate * 100).toInt()}%): ${String.format(java.util.Locale.US, "%.2f", totalCommission)} YER", 40f, y, bodyPaint)
                                    y += 30f
                                    bookings.take(25).forEachIndexed { idx, b ->
                                        val amt = if (b.totalAmount > 0.0) b.totalAmount else b.price
                                        canvas.drawText("${idx + 1}. #${b.bookingNumber} | ${b.status} | $amt YER", 40f, y, bodyPaint)
                                        y += 18f
                                    }
                                    pdf.finishPage(page)
                                    val exportDir = File(context.cacheDir, "exports").apply { mkdirs() }
                                    val pdfFile = File(exportDir, "financial_report_${System.currentTimeMillis()}.pdf")
                                    FileOutputStream(pdfFile).use { pdf.writeTo(it) }
                                    pdf.close()
                                    shareGeneratedFile(
                                        context = context,
                                        file = pdfFile,
                                        mimeType = "application/pdf",
                                        chooserTitle = "مشاركة تقرير الأرباح (PDF)"
                                    )
                                    Toast.makeText(context, "✅ تم إنشاء وتصدير تقرير الأرباح PDF بنجاح!", Toast.LENGTH_SHORT).show()
                                } catch (e: Exception) {
                                    Toast.makeText(context, "❌ تعذر إنشاء ملف PDF: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = themeColors.accent)
                        ) {
                            Text("تصدير ومشاركة تقرير الأرباح (PDF) 📄", color = Color.Black, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
        "STORAGE_QUOTA" -> {
            item {
                AdminStorageAndCacheScreenContent(viewModel = viewModel, themeColors = themeColors)
            }
        }
        "SECURITY_AUDIT" -> {
            item {
                val activityLogs by viewModel.activityLogs.collectAsState()
                val currentAuthUser = remember { com.google.firebase.auth.FirebaseAuth.getInstance().currentUser }
                val errorLogs by com.serviseyem.utils.AppErrorLogManager.logs.collectAsState()
                Card(colors = CardDefaults.cardColors(containerColor = themeColors.surface), modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("🛡️ سجل التدقيق الأمني المباشر", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = themeColors.accent)
                        Text("الصلاحية الحالية: $adminRole", fontSize = 12.sp, color = Color.White, fontWeight = FontWeight.Bold)
                        Text(
                            "حالة Firebase Auth: ${if (currentAuthUser != null) "متصل (${currentAuthUser.uid.take(8)}...)" else "جلسة غير متصلة"}",
                            fontSize = 12.sp,
                            color = if (currentAuthUser != null) Color(0xFF10B981) else Color(0xFFF59E0B)
                        )
                        Text("إجمالي أحداث السجل النشط: ${activityLogs.size} | سجل أخطاء النظام: ${errorLogs.size}", fontSize = 11.5.sp, color = Color.LightGray)
                        if (activityLogs.isNotEmpty()) {
                            Divider(color = Color.White.copy(alpha = 0.1f))
                            Text("أحدث عمليات الإدارة المسجلة:", fontSize = 11.5.sp, fontWeight = FontWeight.Bold, color = themeColors.accent)
                            activityLogs.take(5).forEach { log ->
                                Text("• ${log.action}", fontSize = 10.5.sp, color = Color.White)
                            }
                        }
                    }
                }
            }
        }
        "SYSTEM_SETTINGS" -> {
            item {
                AdminSystemSettingsScreenContent(viewModel = viewModel, themeColors = themeColors)
            }
        }
        "AUTO_DISPATCH" -> {
            item {
                AdminAutoRoutingScreenContent(viewModel = viewModel, themeColors = themeColors)
            }
        }
        "API_MANAGEMENT" -> {
            item {
                AdminApiKeysScreenContent(viewModel = viewModel, themeColors = themeColors)
            }
        }
        "ROLES_PERMISSIONS" -> {
            item {
                Text("🛡️ مصفوفة الصلاحيات والأدوار (538 صلاحية كاملة)", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = themeColors.accent)
                Text("إدارة وتعيين الصلاحيات الـ 538 الموزعة على 18 قطاعاً لجميع المشرفين والإداريين ومزامنتها مع Firestore سحابياً:", fontSize = 11.sp, color = themeColors.textSecondary)
                Spacer(modifier = Modifier.height(4.dp))
            }

            item {
                val syncedMatrix by viewModel.adminPermissionsMatrix.collectAsState()
                var adminCustomPermissions by remember {
                    mutableStateOf(
                        syncedMatrix.ifEmpty { AdminPermissionsRegistry.allPermissions.map { it.key } }
                    )
                }
                LaunchedEffect(syncedMatrix) {
                    if (syncedMatrix.isNotEmpty()) {
                        adminCustomPermissions = syncedMatrix
                    }
                }
                AdminPermissionsSelectorView(
                    themeColors = themeColors,
                    selectedPermissions = adminCustomPermissions,
                    onPermissionsChanged = { adminCustomPermissions = it },
                    onSaveRequested = {
                        viewModel.saveCustomPermissionsMatrixToFirestore(adminCustomPermissions, "ADMIN")
                    }
                )
            }

            item {
                AdminRolesPermissionsPanel(viewModel = viewModel, themeColors = themeColors)
            }
        }
        "MAP_CONTROLS" -> {
            item {
                Text("🗺️ التحكم بشاشة الخرائط والمواقع الجغرافية", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = themeColors.accent)
                Text("إدارة ظهور الخرائط والمنشآت والطبقات وتحديد نطاقات التغطية:", fontSize = 11.sp, color = themeColors.textSecondary)
                Spacer(modifier = Modifier.height(4.dp))
            }

            item {
                AdminMapControlsSection(
                    viewModel = viewModel,
                    settingsState = settingsState,
                    themeColors = themeColors
                )
            }

            item {
                Spacer(modifier = Modifier.height(8.dp))
                AdminMapPanel(viewModel = viewModel, themeColors = themeColors)
            }
        }
        "AI_ASSISTANT_PANEL" -> {
            item {
                AdminAssistantPanel(viewModel = viewModel, themeColors = themeColors)
            }
        }
        "ADMIN_PAYMENT_PANEL" -> {
            item {
                AdminPaymentPanel(
                    viewModel = viewModel,
                    themeColors = themeColors,
                    onToggleSystem = { enabled ->
                        viewModel.saveCustomSettingsState(settingsState.copy(isPaymentEnabled = enabled))
                    },
                    onUnlinkAll = {
                        viewModel.saveCustomSettingsState(
                            settingsState.copy(
                                linkedCategoriesForInstantBooking = "",
                                linkedProvidersForDeposit = ""
                            )
                        )
                    },
                    onAdminOverride = { targetId, amount ->
                        viewModel.adminOverrideWalletBalance(targetId, amount)
                    }
                )
            }
        }
        "VOICE_CALLS_PANEL" -> {
            item {
                AdminVoiceCallPanel(viewModel = viewModel, themeColors = themeColors)
            }
        }
        "MAINTENANCE_MODE" -> {
            item {
                AdminMaintenanceScreenContent(viewModel = viewModel, themeColors = themeColors)
            }
        }
        "DATA_MANAGEMENT" -> {
            item {
                AdminDataManagementScreenContent(viewModel = viewModel, themeColors = themeColors)
            }
        }
    }
}

internal fun buildSelectedCollectionsList(state: AdminPanelState): List<String> {
    val selectedCols = mutableListOf<String>()
    if (state.wipeProvidersChecked) selectedCols.add("providers")
    if (state.wipeBookingsChecked) selectedCols.add("bookings")
    if (state.wipeChatsChecked) selectedCols.add("chat_channels")
    if (state.wipeNotifsChecked) selectedCols.add("notifications")
    if (state.wipeReportsChecked) selectedCols.add("reports")
    if (state.wipeCategoriesChecked) selectedCols.add("categories")
    if (state.wipePendingChecked) selectedCols.add("pending_providers")
    if (state.wipeBannersChecked) selectedCols.add("banners")
    if (state.wipeSupervisorsChecked) selectedCols.add("supervisors")
    if (state.wipeCitiesChecked) selectedCols.add("cities")
    if (state.wipeThemesChecked) selectedCols.add("color_themes")
    return selectedCols
}

@Composable
fun PasswordEntityCard(
    name: String,
    phone: String,
    category: String,
    @Suppress("UNUSED_PARAMETER") password: String?,
    onResetPassword: (String) -> Unit,
    context: Context
) {
    var editPass by remember { mutableStateOf("") }

    Card(
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
        border = BorderStroke(1.dp, Color(0xFF3B82F6).copy(alpha = 0.4f))
    ) {
        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text(name, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color.White)
                Text("القسم: $category", fontSize = 10.sp, color = Color(0xFF3B82F6))
            }
            Text("رقم الهاتف: $phone", fontSize = 11.sp, color = Color.LightGray)

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text(
                    "🔒 كلمة المرور محمية ومشفرة (لا يمكن عرضها)",
                    fontSize = 11.sp,
                    color = Color.Gray,
                    fontWeight = FontWeight.Bold
                )
            }

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
                OutlinedTextField(
                    value = editPass,
                    onValueChange = { editPass = it },
                    label = { Text("كلمة مرور جديدة (اختيار التحديث)", fontSize = 9.sp) },
                    singleLine = true,
                    modifier = Modifier.weight(1f),
                    colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White)
                )
                Button(
                    onClick = {
                        if (editPass.isNotBlank()) {
                            onResetPassword(editPass)
                            editPass = ""
                        } else {
                            Toast.makeText(context, "الرجاء إدخال كلمة المرور الجديدة", Toast.LENGTH_SHORT).show()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981)),
                    modifier = Modifier.height(48.dp),
                    contentPadding = PaddingValues(horizontal = 8.dp)
                ) {
                    Text("تحديث 🔒", fontSize = 10.sp, color = Color.Black, fontWeight = FontWeight.Bold)
                }
            }

            Row(horizontalArrangement = Arrangement.spacedBy(4.dp), modifier = Modifier.fillMaxWidth()) {
                Button(
                    onClick = {
                        val whatsappText = "مرحباً يا غالي، قامت الإدارة بتعيين كلمة مرور جديدة لحسابك في دليل خدمات اليمن، يرجى التواصل مع الإدارة للحصول عليها."
                        val whatsappUrl = "https://wa.me/967${phone.trim().removePrefix("0").removePrefix("+967")}?text=${Uri.encode(whatsappText)}"
                        try {
                            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(whatsappUrl))
                            context.startActivity(intent)
                        } catch (e: Exception) {
                            Toast.makeText(context, "فشل فتح واتساب", Toast.LENGTH_SHORT).show()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF25D366)),
                    modifier = Modifier.height(48.dp).weight(1f),
                    contentPadding = PaddingValues(horizontal = 4.dp)
                ) {
                    Text("🟢 واتساب", color = Color.White, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                }

                Button(
                    onClick = {
                        val teleText = "مرحباً، تم تعيين كلمة مرور جديدة لحسابك في دليل خدمات اليمن، يرجى التواصل مع الإدارة لاستلامها."
                        val teleUrl = "https://t.me/share/url?url=https://yemen-services.app&text=${Uri.encode(teleText)}"
                        try {
                            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(teleUrl))
                            context.startActivity(intent)
                        } catch (e: Exception) {
                            Toast.makeText(context, "فشل فتح تيليجرام", Toast.LENGTH_SHORT).show()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0088CC)),
                    modifier = Modifier.height(48.dp).weight(1f),
                    contentPadding = PaddingValues(horizontal = 4.dp)
                ) {
                    Text("✈️ تيليجرام", color = Color.White, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                }

                Button(
                    onClick = {
                        val smsText = "تم تعيين كلمة مرور جديدة لحسابك في دليل خدمات اليمن، يرجى التواصل مع الإدارة لاستلامها."
                        try {
                            val intent = Intent(Intent.ACTION_SENDTO, Uri.parse("smsto:$phone")).apply {
                                putExtra("sms_body", smsText)
                            }
                            context.startActivity(intent)
                        } catch (e: Exception) {
                            Toast.makeText(context, "فشل فتح SMS", Toast.LENGTH_SHORT).show()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF3B82F6)),
                    modifier = Modifier.height(48.dp).weight(1f),
                    contentPadding = PaddingValues(horizontal = 4.dp)
                ) {
                    Text("💬 SMS", color = Color.White, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
