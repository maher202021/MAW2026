package com.serviseyem.ui.screens.admin

import androidx.compose.foundation.BorderStroke
import com.serviseyem.ui.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.serviseyem.ui.MainViewModel
import com.serviseyem.ui.screens.admin.components.AdminEntityCard
import com.serviseyem.ui.screens.admin.components.AdminFilterChips
import com.serviseyem.utils.VisualThemePalette
import com.serviseyem.ui.viewmodels.AdminViewModel
import java.util.UUID
import kotlinx.coroutines.launch

/**
 * 👑 Admin Panel: User & Accounts Manager (إدارة المستخدمين والحسابات)
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminUserManager(
    onBack: () -> Unit = {},
    mainViewModel: MainViewModel,
    adminViewModel: AdminViewModel = mainViewModel.adminViewModel,
    themeColors: VisualThemePalette,
    modifier: Modifier = Modifier,
    isPanelMode: Boolean = false
) {
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    val rawUsersList by mainViewModel.registeredUsersList.collectAsState()

    var searchQuery by remember { mutableStateOf("") }
    var selectedFilter by remember { mutableStateOf("الكل") }
    val filters = listOf("الكل", "عميل", "فني", "متجر", "محظور")

    var resetPasswordTargetUser by remember { mutableStateOf<Map<String, Any>?>(null) }
    var newTempPasswordInput by remember { mutableStateOf("") }
    var generatedPasswordSuccess by remember { mutableStateOf<Pair<String, String>?>(null) }

    val filteredUsers = remember(rawUsersList, searchQuery, selectedFilter) {
        rawUsersList.filter { userMap ->
            val name = userMap["name"] as? String ?: ""
            val phone = userMap["phone"] as? String ?: ""
            val role = userMap["role"] as? String ?: "CLIENT"
            val isBlocked = userMap["isBlocked"] as? Boolean ?: false

            val matchesSearch = name.contains(searchQuery, ignoreCase = true) ||
                    phone.contains(searchQuery, ignoreCase = true)

            val matchesFilter = when (selectedFilter) {
                "عميل" -> role == "CLIENT" && !isBlocked
                "فني" -> role == "PROVIDER" && !isBlocked
                "متجر" -> role == "STORE" && !isBlocked
                "محظور" -> isBlocked
                else -> true
            }

            matchesSearch && matchesFilter
        }
    }

    val content = @Composable { padding: PaddingValues ->
        Column(
            modifier = if (isPanelMode) {
                Modifier
                    .fillMaxWidth()
                    .padding(padding)
                    .padding(vertical = 4.dp)
            } else {
                modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(12.dp)
            },
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text("بحث باسم المستخدم أو رقم الهاتف...", color = Color.Gray, fontSize = 11.5.sp) },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = themeColors.accent) },
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = themeColors.accent,
                    unfocusedBorderColor = Color.White.copy(alpha = 0.12f),
                    focusedContainerColor = Color(0xFF1E293B),
                    unfocusedContainerColor = Color(0xFF1E293B),
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White
                ),
                modifier = Modifier.fillMaxWidth()
            )

            AdminFilterChips(
                categories = filters,
                selectedCategory = selectedFilter,
                onSelectCategory = { selectedFilter = it },
                themeColors = themeColors
            )

            if (filteredUsers.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text("لا يوجد مستخدمون يطابقون خيارات البحث", color = Color.Gray, fontSize = 13.sp)
                }
            } else {
                // Safely render list based on mode to prevent nested scrollable crashes
                if (isPanelMode) {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        filteredUsers.forEachIndexed { index, userMap ->
                            UserItem(userMap, index, adminViewModel, scope, snackbarHostState, themeColors, { resetPasswordTargetUser = it }, { newTempPasswordInput = "" })
                        }
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        itemsIndexed(filteredUsers, key = { index, userMap -> (userMap["id"] as? String)?.ifBlank { null } ?: (userMap["phone"] as? String)?.ifBlank { null } ?: "user_$index" }) { index, userMap ->
                            UserItem(userMap, index, adminViewModel, scope, snackbarHostState, themeColors, { resetPasswordTargetUser = it }, { newTempPasswordInput = "" })
                        }
                    }
                }
            }
        }
    }

    if (isPanelMode) {
        content(PaddingValues(0.dp))
    } else {
        Scaffold(
            snackbarHost = { SnackbarHost(snackbarHostState) },
            topBar = {
                TopAppBar(
                    title = { Text("👥 إدارة المستخدمين والحسابات", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color.White) },
                    navigationIcon = {
                        IconButton(onClick = onBack) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "رجوع", tint = Color.White)
                        }
                    },
                    actions = {
                        IconButton(onClick = {
                            val csv = buildString {
                                appendLine("Name,Phone,Role,City")
                                rawUsersList.forEach { u ->
                                    appendLine("${u["name"]},${u["phone"]},${u["role"]},${u["city"]}")
                                }
                            }
                            adminViewModel.recordAuditLog("EXPORT_USERS", "تصدير المستخدمين")
                            scope.launch { snackbarHostState.showSnackbar("📥 تم تصدير بيانات المستخدمين بنجاح") }
                        }) {
                            Icon(Icons.Default.Share, contentDescription = "تصدير", tint = themeColors.accent)
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFF0F172A))
                )
            },
            containerColor = Color(0xFF0F172A)
        ) { paddingValues ->
            content(paddingValues)
        }
    }

    val targetUserForReset = resetPasswordTargetUser
    if (targetUserForReset != null) {
        PasswordResetDialog(
            userMap = targetUserForReset,
            newPasswordInput = newTempPasswordInput,
            onPasswordChange = { newTempPasswordInput = it },
            onDismiss = { resetPasswordTargetUser = null },
            onSave = { userId, phone, name, pass ->
                val db = com.google.firebase.firestore.FirebaseFirestore.getInstance()
                val cleanPhone = phone.replace("+", "").replace(" ", "").trim()
                val hashedPass = com.serviseyem.utils.SecureHasher.hashPassword(pass)
                val passData = mapOf("password" to hashedPass, "updatedAt" to System.currentTimeMillis())
                db.collection("registered_users").document(userId).update(passData)
                if (cleanPhone.isNotEmpty()) {
                    db.collection("registered_users").document(cleanPhone).update(passData)
                    db.collection("providers").document(cleanPhone).update(passData)
                }
                mainViewModel.addNotification(
                    title = "🔑 تم تحديث كلمة المرور للحساب",
                    message = "عزيزي $name، تم تعيين كلمة مرور جديدة لحسابك من قبل الإدارة، يرجى التواصل مع الإدارة للحصول عليها.",
                    targetType = "USER",
                    targetValue = phone
                )
                resetPasswordTargetUser = null
                generatedPasswordSuccess = Pair(name, pass)
                scope.launch { snackbarHostState.showSnackbar("✅ تم تحديث كلمة المرور وحفظ البصمة المشفرة بنجاح") }
            },
            snackbarHostState = snackbarHostState,
            scope = scope
        )
    }

    val genPassSuccess = generatedPasswordSuccess
    if (genPassSuccess != null) {
        val (targetName, plainPass) = genPassSuccess
        val clipboardManager = androidx.compose.ui.platform.LocalClipboardManager.current
        AlertDialog(
            onDismissRequest = { generatedPasswordSuccess = null },
            containerColor = Color(0xFF1E293B),
            title = {
                Text(
                    "🔑 كلمة المرور الجديدة للحساب",
                    color = Color.White,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        "المستخدم: $targetName",
                        color = Color.White,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        "تم حفظ كلمة المرور كبصمة مشفرة (Hash) في قاعدة البيانات بأمان. تظهر كلمة المرور الأصلية هذه المرة فقط ولن يمكن استرجاعها لاحقاً:",
                        color = Color.LightGray,
                        fontSize = 12.sp
                    )
                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A)),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = plainPass,
                                color = themeColors.accent,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Button(
                                onClick = {
                                    clipboardManager.setText(androidx.compose.ui.text.AnnotatedString(plainPass))
                                    scope.launch { snackbarHostState.showSnackbar("📋 تم نسخ كلمة المرور إلى الحافظة") }
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = themeColors.accent),
                                shape = RoundedCornerShape(6.dp),
                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Text("نسخ للحافظة 📋", fontSize = 11.sp, color = Color.White)
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = { generatedPasswordSuccess = null },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF3B82F6))
                ) {
                    Text("تم وحفظ", color = Color.White, fontWeight = FontWeight.Bold)
                }
            }
        )
    }
}

@Composable
fun UserItem(
    userMap: Map<String, Any>,
    index: Int,
    adminViewModel: AdminViewModel,
    scope: kotlinx.coroutines.CoroutineScope,
    snackbarHostState: SnackbarHostState,
    themeColors: VisualThemePalette,
    onResetPassword: (Map<String, Any>) -> Unit,
    onResetInput: () -> Unit
) {
    val userId = userMap["id"] as? String ?: ""
    val name = userMap["name"] as? String ?: "مستخدم"
    val phone = userMap["phone"] as? String ?: ""
    val city = userMap["city"] as? String ?: "صنعاء"
    val role = userMap["role"] as? String ?: "CLIENT"
    val isBlocked = userMap["isBlocked"] as? Boolean ?: false

    AdminEntityCard(
        title = name,
        subtitle = "📱 $phone • 📍 $city • 👤 $role",
        statusText = if (isBlocked) "محظور" else "نشط",
        statusColor = if (isBlocked) Color(0xFFEF5350) else Color(0xFF10B981),
        isBlocked = isBlocked,
        themeColors = themeColors,
        actions = {
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = {
                            if (isBlocked) {
                                adminViewModel.unblockUser(userId) { success ->
                                    scope.launch { snackbarHostState.showSnackbar("تم إلغاء حظر المستخدم") }
                                }
                            } else {
                                adminViewModel.blockUser(userId) { success ->
                                    scope.launch { snackbarHostState.showSnackbar("تم حظر المستخدم") }
                                }
                            }
                        },
                        shape = RoundedCornerShape(8.dp),
                        border = BorderStroke(1.dp, if (isBlocked) Color(0xFF10B981) else Color(0xFFEF5350)),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(if (isBlocked) "إلغاء الحظر" else "حظر 🚫", fontSize = 10.5.sp, color = if (isBlocked) Color(0xFF10B981) else Color(0xFFEF5350))
                    }

                    OutlinedButton(
                        onClick = {
                            onResetPassword(userMap)
                            onResetInput()
                        },
                        shape = RoundedCornerShape(8.dp),
                        border = BorderStroke(1.dp, Color(0xFF3B82F6)),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("تغيير السر 🔑", fontSize = 10.5.sp, color = Color(0xFF3B82F6), fontWeight = FontWeight.Bold)
                    }

                    IconButton(
                        onClick = {
                            adminViewModel.deleteUser(userId) { success ->
                                scope.launch { snackbarHostState.showSnackbar("🗑️ تم حذف حساب المستخدم") }
                            }
                        },
                        modifier = Modifier.background(Color(0xFFEF5350).copy(alpha = 0.15f), RoundedCornerShape(8.dp))
                    ) {
                        Icon(Icons.Default.Delete, contentDescription = "حذف", tint = Color(0xFFEF5350), modifier = Modifier.size(18.dp))
                    }
                }

                val context = androidx.compose.ui.platform.LocalContext.current
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = {
                            val cleanPhone = phone.replace("+", "").replace(" ", "").trim()
                            val msg = android.net.Uri.encode("مرحباً $name، تواصل من إدارة تطبيق دليل خدمات اليمن:")
                            val intent = android.content.Intent(android.content.Intent.ACTION_VIEW, android.net.Uri.parse("https://wa.me/$cleanPhone?text=$msg"))
                            try { context.startActivity(intent) } catch (e: Exception) { e.printStackTrace() }
                        },
                        shape = RoundedCornerShape(8.dp),
                        border = BorderStroke(1.dp, Color(0xFF25D366)),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("واتساب 💬", color = Color(0xFF25D366), fontSize = 10.5.sp, fontWeight = FontWeight.Bold)
                    }

                    OutlinedButton(
                        onClick = {
                            val cleanPhone = phone.replace("+", "").replace(" ", "").trim()
                            val msg = android.net.Uri.encode("مرحباً $name، تواصل من إدارة تطبيق دليل خدمات اليمن:")
                            val intent = android.content.Intent(android.content.Intent.ACTION_VIEW, android.net.Uri.parse("https://t.me/share/url?url=$cleanPhone&text=$msg"))
                            try { context.startActivity(intent) } catch (e: Exception) { e.printStackTrace() }
                        },
                        shape = RoundedCornerShape(8.dp),
                        border = BorderStroke(1.dp, Color(0xFF0088CC)),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("تيليجرام ✈️", color = Color(0xFF0088CC), fontSize = 10.5.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    )
}

@Composable
fun PasswordResetDialog(
    userMap: Map<String, Any>,
    newPasswordInput: String,
    onPasswordChange: (String) -> Unit,
    onDismiss: () -> Unit,
    onSave: (String, String, String, String) -> Unit,
    snackbarHostState: SnackbarHostState,
    scope: kotlinx.coroutines.CoroutineScope
) {
    val name = userMap["name"] as? String ?: "المستخدم"
    val phone = userMap["phone"] as? String ?: ""
    val userId = userMap["id"] as? String ?: phone

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Color(0xFF1E293B),
        title = { Text("🔑 تعيين كلمة مرور جديدة", color = Color.White, fontSize = 15.sp, fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text("إعادة تعيين كلمة المرور للحساب: $name ($phone)", color = Color.Gray, fontSize = 12.sp)
                OutlinedTextField(
                    value = newPasswordInput,
                    onValueChange = onPasswordChange,
                    placeholder = { Text("اكتب كلمة المرور الجديدة...", color = Color.DarkGray) },
                    colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (newPasswordInput.trim().length >= 4) {
                        onSave(userId, phone, name, newPasswordInput.trim())
                    } else {
                        scope.launch { snackbarHostState.showSnackbar("⚠️ كلمة المرور يجب أن لا تقل عن 4 رموز") }
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF3B82F6))
            ) {
                Text("حفظ وإرسال 🔑", color = Color.White, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("إلغاء", color = Color.Gray) }
        }
    )
}
