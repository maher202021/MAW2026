package com.serviseyem.ui.screens.admin

import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Login
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.serviseyem.ui.*
import com.serviseyem.ui.viewmodels.AdminViewModel
import com.serviseyem.utils.VisualThemePalette

@Composable
internal fun AdminSecureLoginScreen(
    viewModel: MainViewModel,
    themeColors: VisualThemePalette,
    context: Context,
    state: AdminPanelState
) {
    var accountType by rememberSaveable { mutableStateOf("ADMIN") } // "ADMIN", "OWNER" or "SUPERVISOR"
    var emailInput by rememberSaveable { mutableStateOf("") }
    var passwordInput by rememberSaveable { mutableStateOf("") }
    var passwordVisible by rememberSaveable { mutableStateOf(false) }
    var loginError by rememberSaveable { mutableStateOf<String?>(null) }
    var isLoginLoading by remember { mutableStateOf(false) }
    var rememberMe by rememberSaveable { mutableStateOf(true) }

    val adminViewModelForLogin: AdminViewModel = viewModel.adminViewModel

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .size(72.dp)
                .background(
                    themeColors.accent.copy(alpha = 0.15f),
                    RoundedCornerShape(20.dp)
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                Icons.Default.AdminPanelSettings,
                contentDescription = "قفل الأدمن",
                tint = themeColors.accent,
                modifier = Modifier.size(40.dp)
            )
        }

        Spacer(Modifier.height(16.dp))

        Text(
            "لوحة تحكم الإدارة العليا",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White
        )

        Text(
            "🔒 محمي بواسطة Firebase Auth + Custom Claims",
            fontSize = 11.sp,
            color = Color.Gray,
            modifier = Modifier.padding(top = 4.dp),
            textAlign = TextAlign.Center
        )

        Spacer(Modifier.height(16.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Button(
                onClick = { accountType = "ADMIN" },
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (accountType == "ADMIN") themeColors.accent else themeColors.surface
                ),
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(
                    "🛡️ أدمن",
                    color = if (accountType == "ADMIN") Color.Black else Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp
                )
            }

            Button(
                onClick = { accountType = "OWNER" },
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (accountType == "OWNER") themeColors.accent else themeColors.surface
                ),
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(
                    "👑 مالك",
                    color = if (accountType == "OWNER") Color.Black else Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp
                )
            }

            Button(
                onClick = { accountType = "SUPERVISOR" },
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (accountType == "SUPERVISOR") themeColors.accent else themeColors.surface
                ),
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(
                    "👮 مشرف",
                    color = if (accountType == "SUPERVISOR") Color.Black else Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp
                )
            }
        }

        Spacer(Modifier.height(16.dp))

        OutlinedTextField(
            value = emailInput,
            onValueChange = {
                emailInput = it.trim()
                loginError = null
            },
            label = { Text(if (accountType == "SUPERVISOR") "اسم المستخدم أو معرف المشرف" else "البريد الإلكتروني") },
            leadingIcon = {
                Icon(
                    if (accountType == "SUPERVISOR") Icons.Default.Person else Icons.Default.Email,
                    null,
                    tint = Color(0xFFF59E0B)
                )
            },
            keyboardOptions = KeyboardOptions(
                keyboardType = if (accountType == "SUPERVISOR") KeyboardType.Text else KeyboardType.Email,
                imeAction = ImeAction.Next
            ),
            singleLine = true,
            enabled = !isLoginLoading,
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.fillMaxWidth(),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Color(0xFFF59E0B),
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White,
                focusedLabelColor = Color(0xFFF59E0B)
            )
        )

        Spacer(Modifier.height(12.dp))

        OutlinedTextField(
            value = passwordInput,
            onValueChange = {
                passwordInput = it
                loginError = null
            },
            label = { Text(if (accountType == "SUPERVISOR") "رمز المرور (Passcode)" else "كلمة المرور") },
            leadingIcon = {
                Icon(Icons.Default.Lock, null, tint = Color(0xFFF59E0B))
            },
            visualTransformation = if (passwordVisible) {
                VisualTransformation.None
            } else {
                PasswordVisualTransformation()
            },
            keyboardOptions = KeyboardOptions(
                keyboardType = if (accountType == "SUPERVISOR") KeyboardType.NumberPassword else KeyboardType.Password,
                imeAction = ImeAction.Done
            ),
            trailingIcon = {
                IconButton(onClick = { passwordVisible = !passwordVisible }) {
                    Icon(
                        if (passwordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                        contentDescription = "تبديل الرؤية",
                        tint = Color.Gray
                    )
                }
            },
            singleLine = true,
            enabled = !isLoginLoading,
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.fillMaxWidth(),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Color(0xFFF59E0B),
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White,
                focusedLabelColor = Color(0xFFF59E0B)
            )
        )

        Spacer(Modifier.height(12.dp))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { rememberMe = !rememberMe }
                .padding(vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Start
        ) {
            Checkbox(
                checked = rememberMe,
                onCheckedChange = { rememberMe = it },
                colors = CheckboxDefaults.colors(
                    checkedColor = Color(0xFFF59E0B),
                    uncheckedColor = Color.LightGray,
                    checkmarkColor = Color.Black
                )
            )
            Spacer(Modifier.width(8.dp))
            Text(
                text = "تذكرني على هذا الجهاز",
                color = Color.White,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium
            )
        }

        loginError?.let { error ->
            Spacer(Modifier.height(8.dp))
            Surface(
                color = Color(0xFFEF5350).copy(alpha = 0.15f),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.Warning, null, tint = Color(0xFFEF5350))
                    Spacer(Modifier.width(8.dp))
                    Text(
                        error,
                        color = Color(0xFFEF5350),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        Spacer(Modifier.height(20.dp))

        Button(
            onClick = {
                if (emailInput.isBlank() || passwordInput.isBlank()) {
                    loginError = if (accountType == "SUPERVISOR") {
                        "يرجى إدخال اسم المستخدم ورمز المرور"
                    } else {
                        "يرجى إدخال البريد وكلمة المرور"
                    }
                    return@Button
                }

                isLoginLoading = true
                loginError = null

                when (accountType) {
                    "OWNER" -> {
                        adminViewModelForLogin.loginOwnerSecure(
                            email = emailInput,
                            password = passwordInput,
                            rememberMe = rememberMe
                        ) { success, errorMsg ->
                            isLoginLoading = false
                            if (success) {
                                state.isAuthorized = true
                                state.activeSubTab = "BACKDOOR"
                                viewModel.authenticateAdmin(context, "OWNER", rememberMe)
                                viewModel.triggerNotification("👑 مرحباً بك في لوحة التحكم (المالك)")
                            } else {
                                loginError = errorMsg ?: "فشل تسجيل الدخول"
                            }
                        }
                    }
                    "ADMIN" -> {
                        adminViewModelForLogin.loginAdminSecure(
                            email = emailInput,
                            password = passwordInput,
                            rememberMe = rememberMe
                        ) { success, errorMsg ->
                            isLoginLoading = false
                            if (success) {
                                state.isAuthorized = true
                                state.activeSubTab = "REG_REQ"
                                viewModel.authenticateAdmin(context, "ADMIN", rememberMe)
                                viewModel.triggerNotification("👑 مرحباً بك في لوحة التحكم")
                            } else {
                                loginError = errorMsg ?: "فشل تسجيل الدخول"
                            }
                        }
                    }
                    "SUPERVISOR" -> {
                        adminViewModelForLogin.loginSupervisorSecure(
                            usernameOrEmail = emailInput,
                            passcode = passwordInput,
                            rememberMe = rememberMe
                        ) { success, errorMsg ->
                            isLoginLoading = false
                            if (success) {
                                state.isAuthorized = true
                                state.activeSubTab = "REG_REQ"
                                viewModel.authenticateAdmin(context, "SUPERVISOR", rememberMe)
                                viewModel.triggerNotification("💼 مرحباً بك في لوحة التحكم (مشرف)")
                            } else {
                                loginError = errorMsg ?: "بيانات دخول المشرف غير صحيحة"
                            }
                        }
                    }
                }
            },
            enabled = emailInput.isNotBlank() && passwordInput.isNotBlank() && !isLoginLoading,
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFFF59E0B),
                contentColor = Color.Black
            ),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
        ) {
            if (isLoginLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(24.dp),
                    color = Color.Black,
                    strokeWidth = 2.dp
                )
            } else {
                Icon(Icons.Default.Login, null)
                Spacer(Modifier.width(8.dp))
                Text(
                    "تسجيل الدخول الآمن",
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
            }
        }

        Spacer(Modifier.height(12.dp))

        TextButton(
            onClick = { viewModel.navigateToScreen(AppScreens.USER_BROWSE) }
        ) {
            Text(
                "العودة إلى التطبيق 🏠",
                color = Color.LightGray,
                fontSize = 12.sp
            )
        }
    }
}

@Composable
internal fun AdminDashboardTopBar(
    viewModel: MainViewModel,
    themeColors: VisualThemePalette,
    context: Context,
    state: AdminPanelState
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            "🔐 لوحة التحكم الرئيسية",
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            color = themeColors.accent
        )
        Button(
            onClick = {
                state.isAuthorized = false
                viewModel.logout(context)
            },
            colors = ButtonDefaults.buttonColors(containerColor = Color.Red.copy(alpha = 0.8f)),
            shape = RoundedCornerShape(8.dp)
        ) {
            Text("تسجيل خروج", color = Color.White, fontSize = 11.sp)
        }
    }
    Spacer(modifier = Modifier.height(10.dp))
}

@Composable
internal fun AdminNavigationTabsBar(
    adminRole: String,
    supervisorPermissions: List<String>,
    themeColors: VisualThemePalette,
    state: AdminPanelState
) {
    val allTabs = remember(adminRole, supervisorPermissions) {
        val baseTabs = mutableListOf(
            Triple("REG_REQ", "2️⃣ ⌛ طلبات الانضمام والاعتماد", "OPERATIONS"),
            Triple("MANUAL_ADD", "3️⃣ ➕ الإضافة اليدوية", "ENTITIES"),
            Triple("STORES", "4️⃣ 🏪 المحلات التجارية", "ENTITIES"),
            Triple("RESTAURANTS", "5️⃣ 🍔 المطاعم والكافيهات", "ENTITIES"),
            Triple("MEDICAL", "6️⃣ 🏥 المراكز الطبية والعيادات", "ENTITIES"),
            Triple("PROPERTIES", "7️⃣ 🏠 العقارات والأراضي", "ENTITIES"),
            Triple("JOBS", "8️⃣ 💼 إعلانات الوظائف", "ENTITIES"),
            Triple("APPLICANTS", "9️⃣ 📄 المتقدمين للوظائف", "OPERATIONS"),
            Triple("STATS", "🔟 📊 الإحصائيات الشاملة", "SYSTEM"),
            Triple("BOOKINGS", "1️⃣1️⃣ 📅 نظام الحجوزات والطلبات", "OPERATIONS"),
            Triple("CHATS", "1️⃣2️⃣ 💬 رقابة وإدارة المحادثات", "OPERATIONS"),
            Triple("PROVIDERS", "1️⃣3️⃣ 👥 أعضاء الدليل والتميز", "ENTITIES"),
            Triple("PASSWORDS_RESET", "1️⃣4️⃣ 🔑 إعادة تعيين كلمات المرور", "SECURITY"),
            Triple("BANNERS", "1️⃣5️⃣ 📢 البنرات الإعلانية والتوجيه", "CUSTOMIZATION"),
            Triple("CATEGORIES", "1️⃣6️⃣ 🗂️ تحكم الأقسام والتصنيفات", "ENTITIES"),
            Triple("CITIES", "1️⃣7️⃣ 🗺️ تحكم المدن والمحافظات", "SYSTEM"),
            Triple("COMPLAINTS", "1️⃣8️⃣ ⚠️ الشكاوى والبلاغات", "OPERATIONS"),
            Triple("VIP", "1️⃣9️⃣ 🏆 ترقيات واشتراكات VIP", "OPERATIONS"),
            Triple("SUPERVISORS", "2️⃣0️⃣ 🛡️ المشرفين ومصفوفة الصلاحيات (538)", "SECURITY"),
            Triple("COLORS", "2️⃣1️⃣ 🎨 تخصيص الألوان والمظهر", "CUSTOMIZATION"),
            Triple("NOTIFICATIONS", "2️⃣2️⃣ 🔔 بث وإدارة الإشعارات", "OPERATIONS"),
            Triple("BACKUP", "2️⃣3️⃣ 💾 النسخ الاحتياطي والمزامنة", "SYSTEM"),
            Triple("CLEAN", "2️⃣4️⃣ 🧹 تنظيف وتهيئة البيانات", "SYSTEM"),
            Triple("REVIEWS", "2️⃣5️⃣ ⭐ إدارة التقييمات والتعليقات", "OPERATIONS"),
            Triple("CALLS", "2️⃣6️⃣ 📞 مراقبة سجلات المكالمات", "OPERATIONS"),
            Triple("COUPONS", "2️⃣7️⃣ 🎫 إدارة الكوبونات والخصومات", "OPERATIONS"),
            Triple("BLOCKED", "2️⃣8️⃣ 🚫 قائمة المحظورين المركزية", "SECURITY"),
            Triple("DELETED", "2️⃣9️⃣ 🗑️ سلة المحذوفات المركزية", "SYSTEM"),
            Triple("PAYMENTS", "3️⃣0️⃣ 💳 المدفوعات ونظام المحافظ", "OPERATIONS"),
            Triple("CUSTOM_TABS", "3️⃣1️⃣ 📑 التبويبات المخصصة للملفات", "CUSTOMIZATION"),
            Triple("GOLDEN_ICONS", "3️⃣2️⃣ 👑 الأيقونات الذهبية والخطوط", "CUSTOMIZATION"),
            Triple("ADVANCED_CHAT", "3️⃣3️⃣ ⚡ الدردشات المتقدمة", "OPERATIONS"),
            Triple("CARD_CUSTOMIZER", "3️⃣4️⃣ 🎛️ تخصيص أزرار وأشكال البطاقات", "CUSTOMIZATION"),
            Triple("NEW_SECTION_CREATOR", "3️⃣5️⃣ ➕ إنشاء الأقسام الجديدة", "ENTITIES"),
            Triple("REG_FORMS_MANAGER", "3️⃣6️⃣ 📋 تخصيص استمارات وشروط التسجيل", "CUSTOMIZATION"),
            Triple("ROLES_PERMISSIONS", "3️⃣7️⃣ 🛡️ الصلاحيات والأدوار (538)", "SECURITY"),
            Triple("MAP_CONTROLS", "3️⃣8️⃣ 🗺️ التحكم بشاشة الخرائط", "CUSTOMIZATION"),
            Triple("AI_ASSISTANT_PANEL", "3️⃣9️⃣ 🤖 المساعد الذكي وقاموس الصيانة", "CUSTOMIZATION"),
            Triple("ADMIN_PAYMENT_PANEL", "4️⃣0️⃣ 💳 المدفوعات والاشتراكات والربط الشامل", "SYSTEM"),
            Triple("VOICE_CALLS_PANEL", "4️⃣1️⃣ 🎙️ إدارة المكالمات الصوتية", "SYSTEM"),
            Triple("QUICK_SERVICE", "⚡ الخدمات الفورية والفنيين", "OPERATIONS"),
            Triple("FINANCIAL_REPORTS", "📈 تقارير الأرباح", "SYSTEM"),
            Triple("STORAGE_QUOTA", "🗄️ سعة التخزين والكاش", "SYSTEM"),
            Triple("SECURITY_AUDIT", "🛡️ سجل الأمان", "SECURITY"),
            Triple("SYSTEM_SETTINGS", "⚙️ إعدادات النظام", "SYSTEM"),
            Triple("AUTO_DISPATCH", "🧭 التوجيه التلقائي", "OPERATIONS"),
            Triple("API_MANAGEMENT", "🔌 مفاتيح API", "SYSTEM"),
            Triple("MAINTENANCE_MODE", "🚧 وضع الصيانة", "SYSTEM"),
            Triple("DATA_MANAGEMENT", "🏢 الإدارة الشاملة للمنشآت", "ENTITIES")
        )

        val filteredByRole = if (adminRole == "SUPERVISOR" && supervisorPermissions.isNotEmpty()) {
            baseTabs.filter { it.first in supervisorPermissions }
        } else {
            baseTabs
        }

        val finalTabs = filteredByRole.toMutableList()
        if (adminRole == "OWNER") {
            finalTabs.add(0, Triple("BACKDOOR", "1️⃣ ⚙️ البوابة الخلفية (BACKDOOR)", "SECURITY"))
        }
        finalTabs
    }

    val filteredTabs = remember(state.selectedGroupFilter, allTabs) {
        if (state.selectedGroupFilter == "ALL") allTabs
        else allTabs.filter { it.third == state.selectedGroupFilter }
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 6.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        val groups = listOf(
            "ALL" to "🌟 كل الأقسام (38+)",
            "ENTITIES" to "🏢 المنشآت والأقسام",
            "OPERATIONS" to "⚡ العمليات والحجوزات",
            "SECURITY" to "🛡️ الأمان والمشرفين",
            "CUSTOMIZATION" to "🎨 التخصيص والمظهر",
            "SYSTEM" to "⚙️ النظام والبيانات"
        )
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            contentPadding = PaddingValues(horizontal = 2.dp)
        ) {
            items(groups) { (gKey, gLabel) ->
                val isGrpSel = state.selectedGroupFilter == gKey
                FilterChip(
                    selected = isGrpSel,
                    onClick = { state.selectedGroupFilter = gKey },
                    label = {
                        Text(
                            gLabel,
                            fontSize = 10.sp,
                            fontWeight = if (isGrpSel) FontWeight.Bold else FontWeight.Normal
                        )
                    },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = themeColors.accent,
                        selectedLabelColor = Color.Black,
                        containerColor = themeColors.surface,
                        labelColor = Color.White
                    )
                )
            }
        }

        LazyRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = PaddingValues(bottom = 4.dp)
        ) {
            items(filteredTabs) { tab ->
                val isSel = state.activeSubTab == tab.first
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(30.dp))
                        .background(if (isSel) themeColors.accent else themeColors.surface)
                        .clickable { state.activeSubTab = tab.first }
                        .padding(horizontal = 14.dp, vertical = 8.dp)
                        .border(
                            if (isSel) 2.dp else 1.dp,
                            if (isSel) Color.White else themeColors.accent.copy(alpha = 0.25f),
                            RoundedCornerShape(30.dp)
                        )
                ) {
                    Text(
                        text = tab.second,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isSel) Color.Black else Color.White
                    )
                }
            }
        }
    }
}
