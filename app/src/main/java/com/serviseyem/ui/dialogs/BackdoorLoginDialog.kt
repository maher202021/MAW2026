package com.serviseyem.ui.dialogs
import kotlinx.coroutines.launch
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.CoroutineScope

import androidx.compose.foundation.BorderStroke
import com.serviseyem.ui.*
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.serviseyem.ui.MainViewModel
import com.serviseyem.utils.VisualThemePalette

/**
 * 🔐 BackdoorLoginDialog - نافذة تسجيل الدخول للبوابة الخلفية للتحكم الشامل بالدليل
 * تفتح بالضغط على زر الهوم 3 مرات متتالية
 */
@Composable
fun BackdoorLoginDialog(
    viewModel: MainViewModel,
    themeColors: VisualThemePalette,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val settingsState by viewModel.settings.collectAsState()
    val supervisors by viewModel.supervisors.collectAsState()

    val coroutineScope = rememberCoroutineScope()
    val securityManager = remember(context) { com.serviseyem.security.SecurityManager(context) }

    var emailInput by rememberSaveable { mutableStateOf("") }
    var passwordInput by rememberSaveable { mutableStateOf("") }
    var isPasswordVisible by rememberSaveable { mutableStateOf(false) }
    var rememberMe by rememberSaveable { mutableStateOf(true) }
    var isAuthenticating by remember { mutableStateOf(false) }
    var failedAttempts by rememberSaveable { mutableIntStateOf(0) }
    var lockoutUntil by rememberSaveable { mutableLongStateOf(0L) }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A)),
            shape = RoundedCornerShape(20.dp),
            border = BorderStroke(1.5.dp, themeColors.accent.copy(alpha = 0.6f)),
            modifier = Modifier
                .padding(12.dp)
                .fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(14.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Header
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = themeColors.accent.copy(alpha = 0.15f),
                    modifier = Modifier.size(52.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text("🔐", fontSize = 26.sp)
                    }
                }

                Text(
                    text = "تسجيل دخول الإدارة العليا",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )

                Text(
                    text = "منطقة صلاحيات المالك والإدارة العليا لمنصة دليل خدمات اليمن",
                    fontSize = 11.sp,
                    color = Color.LightGray,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )

                HorizontalDivider(color = Color.White.copy(alpha = 0.1f))

                // Email field
                OutlinedTextField(
                    value = emailInput,
                    onValueChange = { emailInput = it },
                    label = { Text("البريد الإلكتروني للإدارة", fontSize = 12.sp) },
                    placeholder = { Text("admin@example.com", fontSize = 12.sp, color = Color.Gray) },
                    leadingIcon = {
                        Icon(Icons.Default.Email, contentDescription = null, tint = themeColors.accent)
                    },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedBorderColor = themeColors.accent,
                        unfocusedBorderColor = Color.Gray.copy(alpha = 0.4f),
                        focusedLabelColor = themeColors.accent,
                        unfocusedLabelColor = Color.LightGray
                    )
                )

                // Password field
                OutlinedTextField(
                    value = passwordInput,
                    onValueChange = { passwordInput = it },
                    label = { Text("كلمة المرور السرية", fontSize = 12.sp) },
                    leadingIcon = {
                        Icon(Icons.Default.Lock, contentDescription = null, tint = themeColors.accent)
                    },
                    trailingIcon = {
                        IconButton(onClick = { isPasswordVisible = !isPasswordVisible }) {
                            Text(
                                text = if (isPasswordVisible) "👁️" else "🔒",
                                fontSize = 16.sp
                            )
                        }
                    },
                    singleLine = true,
                    visualTransformation = if (isPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedBorderColor = themeColors.accent,
                        unfocusedBorderColor = Color.Gray.copy(alpha = 0.4f),
                        focusedLabelColor = themeColors.accent,
                        unfocusedLabelColor = Color.LightGray
                    )
                )

                // Remember me option
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { rememberMe = !rememberMe },
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Checkbox(
                        checked = rememberMe,
                        onCheckedChange = { rememberMe = it },
                        colors = CheckboxDefaults.colors(
                            checkedColor = themeColors.accent,
                            checkmarkColor = Color.Black
                        )
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "تذكرني وحفظ تسجيل الدخول 🔐",
                        color = Color.White,
                        fontSize = 11.5.sp
                    )
                }

                // Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        shape = RoundedCornerShape(10.dp),
                        border = BorderStroke(1.dp, Color.Gray.copy(alpha = 0.5f)),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("إلغاء", color = Color.White, fontSize = 12.sp)
                    }

                    Button(
                        onClick = {
                            val now = System.currentTimeMillis()
                            if (securityManager.isLockedOut() || now < lockoutUntil) {
                                val remainingSec = maxOf(
                                    securityManager.getRemainingLockoutSeconds(),
                                    ((lockoutUntil - now) / 1000).coerceAtLeast(1)
                                )
                                viewModel.triggerNotification("⏳ تم إيقاف المحاولات مؤقتاً. يرجى الانتظار $remainingSec ثانية.")
                                return@Button
                            }

                            if (!com.serviseyem.security.SecurityManager.verifyAppSignature(context)) {
                                viewModel.triggerNotification("❌ تعذر التحقق من سلامة توقيع التطبيق للدخول الإداري.")
                                return@Button
                            }

                            val trimmedUser = emailInput.trim()
                            val trimmedPass = passwordInput.trim()

                            if (trimmedUser.isBlank() || trimmedPass.isBlank()) {
                                viewModel.triggerNotification("❌ يرجى إدخال البريد الإلكتروني وكلمة المرور!")
                                return@Button
                            }

                            isAuthenticating = true
                            coroutineScope.launch {
                                try {
                                    val result = com.serviseyem.utils.AdminSecurityManager.verifyCredentials(
                                        username = trimmedUser,
                                        passwordAttempt = trimmedPass,
                                        settings = settingsState,
                                        context = context,
                                        supervisors = supervisors,
                                        preferredRole = "OWNER"
                                    )
                                    when (result) {
                                        "OWNER" -> {
                                            failedAttempts = 0
                                            securityManager.resetAttempts()
                                            onDismiss()
                                            viewModel.authenticateAdmin(context, "OWNER", rememberMe)
                                            viewModel.triggerNotification("🔓 مرحباً بك بصلاحية المالك - تم تسجيل الدخول بنجاح!")
                                        }
                                        "ADMIN" -> {
                                            failedAttempts = 0
                                            securityManager.resetAttempts()
                                            onDismiss()
                                            viewModel.authenticateAdmin(context, "ADMIN", rememberMe)
                                            viewModel.triggerNotification("🔓 مرحباً بك بصلاحية مدير النظام - تم تسجيل الدخول بنجاح!")
                                        }
                                        "SUPERVISOR" -> {
                                            val matchingSup = supervisors.find {
                                                it.id.equals(trimmedUser, ignoreCase = true) ||
                                                    it.name.trim().equals(trimmedUser, ignoreCase = true)
                                            } ?: com.serviseyem.data.SupervisorEntity(
                                                id = trimmedUser,
                                                name = trimmedUser,
                                                role = "SUPERVISOR",
                                                permissions = listOf("MANAGE_PROVIDERS", "MANAGE_BOOKINGS", "MANAGE_REQUESTS")
                                            )
                                            failedAttempts = 0
                                            securityManager.resetAttempts()
                                            viewModel.setSupervisorSession(matchingSup)
                                            onDismiss()
                                            viewModel.authenticateAdmin(context, "SUPERVISOR", rememberMe)
                                            viewModel.triggerNotification("🔓 مرحباً بك المشرف: ${matchingSup.name} - تم تسجيل الدخول بنجاح!")
                                        }
                                        else -> {
                                            failedAttempts++
                                            securityManager.registerFailedAttempt()
                                            if (failedAttempts >= 3) {
                                                lockoutUntil = System.currentTimeMillis() + 60_000L
                                            }
                                            viewModel.triggerNotification("❌ بيانات الدخول غير صحيحة!")
                                        }
                                    }
                                } catch (e: Throwable) {
                                    android.util.Log.e("BackdoorLoginDialog", "Login error", e)
                                    viewModel.triggerNotification("❌ حدث خطأ غير متوقع أثناء تسجيل الدخول. يرجى المحاولة لاحقاً.")
                                } finally {
                                    isAuthenticating = false
                                }
                            }
                        },
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = themeColors.accent,
                            disabledContainerColor = Color.Gray.copy(alpha = 0.3f),
                            disabledContentColor = Color.LightGray
                        ),
                        modifier = Modifier.weight(1.5f),
                        enabled = !isAuthenticating && emailInput.isNotBlank() && passwordInput.isNotBlank()
                    ) {
                        if (isAuthenticating) {
                            CircularProgressIndicator(color = Color.Black, modifier = Modifier.size(16.dp))
                        } else {
                            Text("دخول للنظام 🚀", color = if (emailInput.isNotBlank() && passwordInput.isNotBlank()) Color.Black else Color.LightGray, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }
                    }
                }
            }
        }
    }
}
