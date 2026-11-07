package com.serviseyem.ui.screens.register

import android.content.ClipData
import com.serviseyem.ui.*
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.serviseyem.ui.MainViewModel
import com.serviseyem.ui.AppScreens
import com.serviseyem.utils.VisualThemePalette
import kotlinx.coroutines.tasks.await
import androidx.compose.ui.text.input.PasswordVisualTransformation

private fun getSecurePrefs(context: Context): android.content.SharedPreferences {
    val prefsName = "yemen_service_secure_prefs"
    fun createEncrypted(): android.content.SharedPreferences {
        val masterKey = androidx.security.crypto.MasterKey.Builder(context)
            .setKeyScheme(androidx.security.crypto.MasterKey.KeyScheme.AES256_GCM)
            .build()
        return androidx.security.crypto.EncryptedSharedPreferences.create(
            context,
            prefsName,
            masterKey,
            androidx.security.crypto.EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            androidx.security.crypto.EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )
    }

    return try {
        createEncrypted()
    } catch (e: Exception) {
        try {
            context.deleteSharedPreferences(prefsName)
            createEncrypted()
        } catch (e2: Exception) {
            context.getSharedPreferences(prefsName, Context.MODE_PRIVATE)
        }
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PasswordResetWaitingScreen(
    viewModel: MainViewModel,
    themeColors: VisualThemePalette,
    onBackClick: () -> Unit
) {
    val context = LocalContext.current
    val sp = remember(context) { context.getSharedPreferences("yemen_service_prefs", Context.MODE_PRIVATE) }
    
    val targetPhoneFromVm by viewModel.passwordRecoveryWaitingPhone.collectAsState()
    val cleanPhone = remember(targetPhoneFromVm, context) {
        val raw = if (targetPhoneFromVm.isNotBlank()) {
            targetPhoneFromVm
        } else {
            val fromHelper = viewModel.preferenceHelper.getPasswordRecoveryWaitingPhone(context)
            if (fromHelper.startsWith("gcm:") || fromHelper.startsWith("gcmx:") || fromHelper.startsWith("enc::")) {
                com.serviseyem.utils.SecurityCryptoUtils.decrypt(fromHelper)
            } else {
                fromHelper
            }
        }
        com.serviseyem.domain.usecases.ValidatePhoneUseCase.normalizePhone(raw).filter { it.isDigit() }
    }

    var status by rememberSaveable { mutableStateOf("PENDING") }
    var newPassword by rememberSaveable { mutableStateOf("") }
    var accountName by rememberSaveable { mutableStateOf("صاحب الحساب") }
    var accountType by rememberSaveable { mutableStateOf("حساب معتمد") }
    var isSubmittingLogin by remember { mutableStateOf(false) }
    var enteredPassword by rememberSaveable { mutableStateOf("") }
    var passwordError by remember { mutableStateOf<String?>(null) }
    var isPasswordVisible by remember { mutableStateOf(false) }

    val secureSp = remember(context) { 
        try { getSecurePrefs(context) } catch(e: Exception) { null }
    }
    val settingsState by viewModel.settings.collectAsState()
    val isResolved = status == "RESOLVED" || status == "APPROVED"

    // ⏳ حماية زمنية (30 ثانية) لمنع تعليق زر تسجيل الدخول (القاعدة 7)
    LaunchedEffect(isSubmittingLogin) {
        if (isSubmittingLogin) {
            kotlinx.coroutines.delay(30_000L)
            isSubmittingLogin = false
        }
    }

    // ⏱️ مؤقت إخفاء كلمة المرور (10 ثوانٍ)
    LaunchedEffect(isPasswordVisible) {
        if (isPasswordVisible) {
            kotlinx.coroutines.delay(10_000L)
            isPasswordVisible = false
        }
    }

    // Listen in real-time to Firestore for reset completion
    DisposableEffect(cleanPhone) {
        if (cleanPhone.isBlank()) {
            onDispose { }
        } else {
            val listener = viewModel.observePasswordRecoveryStatus(cleanPhone) { newStatus, newPass, newName, newType ->
                status = newStatus
                newPassword = newPass
                accountName = newName
                accountType = newType
            }
            onDispose { listener?.remove() }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "🔑 استعادة كلمة المرور",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = Color.White
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "رجوع",
                            tint = Color.White
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = themeColors.surface
                )
            )
        },
        containerColor = themeColors.background
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Main Status Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(22.dp),
                colors = CardDefaults.cardColors(containerColor = themeColors.surface),
                border = BorderStroke(
                    1.5.dp,
                    if (isResolved) Color(0xFF10B981) else themeColors.accent.copy(alpha = 0.5f)
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    // Glowing Status Icon
                    Box(
                        modifier = Modifier
                            .size(72.dp)
                            .clip(CircleShape)
                            .background(
                                if (isResolved) Color(0xFF10B981).copy(alpha = 0.2f)
                                else themeColors.accent.copy(alpha = 0.2f)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (isResolved) Icons.Default.CheckCircle else Icons.Default.Lock,
                            contentDescription = null,
                            tint = if (isResolved) Color(0xFF10B981) else themeColors.accent,
                            modifier = Modifier.size(40.dp)
                        )
                    }

                    if (isResolved) {
                        Text(
                            text = "🎉 تم إعادة تعيين كلمة المرور بنجاح!",
                            fontSize = 17.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color(0xFF10B981),
                            textAlign = TextAlign.Center
                        )
                        Text(
                            text = if (newPassword.isNotBlank()) {
                                "مرحباً $accountName، قام مدير المنصة بتعيين كلمة المرور الجديدة لحسابك:"
                            } else {
                                "مرحباً $accountName، تمت الموافقة على إعادة تعيين كلمة المرور لحسابك. يرجى إدخال كلمة المرور الجديدة للدخول:"
                            },
                            fontSize = 12.sp,
                            color = Color.LightGray,
                            textAlign = TextAlign.Center
                        )

                        if (newPassword.isNotBlank()) {
                            // New Password Display Box
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp),
                                colors = CardDefaults.cardColors(containerColor = Color.Black.copy(alpha = 0.4f)),
                                border = BorderStroke(1.dp, Color(0xFF10B981).copy(alpha = 0.5f))
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(14.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column {
                                        Text("كلمة المرور الجديدة:", fontSize = 10.sp, color = Color.Gray)
                                        Text(
                                            text = if (isPasswordVisible) newPassword else "••••••••",
                                            fontSize = 18.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color.White,
                                            letterSpacing = if (isPasswordVisible) 1.sp else 4.sp
                                        )
                                    }
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        IconButton(onClick = { isPasswordVisible = !isPasswordVisible }) {
                                            Icon(
                                                imageVector = if (isPasswordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                                contentDescription = "إظهار",
                                                tint = themeColors.accent
                                            )
                                        }
                                        IconButton(
                                            onClick = {
                                                val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                                val clip = ClipData.newPlainText("New Password", newPassword)
                                                clipboard.setPrimaryClip(clip)
                                                Toast.makeText(context, "📋 تم نسخ كلمة المرور!", Toast.LENGTH_SHORT).show()
                                            }
                                        ) {
                                            Icon(Icons.Default.ContentCopy, contentDescription = "نسخ", tint = themeColors.accent)
                                        }
                                    }
                                }
                            }
                        }

                        // Direct Login Button
                        OutlinedTextField(
                            value = enteredPassword,
                            onValueChange = { 
                                enteredPassword = it
                                passwordError = null
                            },
                            label = { Text("أدخل كلمة المرور الجديدة للتأكيد") },
                            visualTransformation = PasswordVisualTransformation(),
                            isError = passwordError != null,
                            supportingText = { passwordError?.let { Text(it) } },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White,
                                focusedBorderColor = themeColors.accent,
                                unfocusedBorderColor = Color.Gray
                            )
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(
                            onClick = {
                                val cleanEntered = enteredPassword.trim()
                                val cleanExpected = newPassword.trim()
                                if (cleanEntered.isBlank()) {
                                    passwordError = "❌ يرجى إدخال كلمة المرور الجديدة"
                                    return@Button
                                }
                                if (cleanPhone.length < 7) {
                                    passwordError = "❌ رقم الهاتف غير صالح، يرجى العودة وإعادة المحاولة"
                                    return@Button
                                }
                                if (cleanExpected.isNotBlank() && cleanEntered != cleanExpected && !com.serviseyem.utils.SecureHasher.verifyPassword(cleanEntered, cleanExpected)) {
                                    passwordError = "❌ كلمة المرور المدخلة غير مطابقة لكلمة المرور الجديدة"
                                    return@Button
                                }
                                isSubmittingLogin = true
                                viewModel.searchAccountForRestore(cleanPhone) { match ->
                                    val resolvedType = match?.type?.takeIf { it.isNotBlank() } ?: "CLIENT"
                                    viewModel.verifyRestorePassword(cleanPhone, resolvedType, cleanEntered) { isVerified ->
                                        isSubmittingLogin = false
                                        if (!isVerified) {
                                            passwordError = "❌ كلمة المرور غير صحيحة أو لم تكتمل مزامنة التعيين بعد"
                                            return@verifyRestorePassword
                                        }
                                        val resolvedName = match?.name?.takeIf { it.isNotBlank() } ?: accountName
                                        val provArea = match?.provider?.area?.takeIf { it.isNotBlank() }
                                            ?: match?.store?.cityId?.takeIf { it.isNotBlank() }
                                            ?: match?.property?.cityId?.takeIf { it.isNotBlank() }
                                            ?: "اليمن"
                                        viewModel.setUserSessionDetails(context, resolvedName, cleanPhone, provArea)
                                        viewModel.setJoinRequestPhone(context, cleanPhone)
                                        val resolvedAccountId = match?.provider?.id ?: match?.store?.id ?: match?.property?.id ?: ""

                                        sp.edit()
                                            .putBoolean("is_account_logged_in", true)
                                            .putString("user_account_type", resolvedType)
                                            .putString("logged_account_id", resolvedAccountId)
                                            .remove("password_recovery_waiting_phone")
                                            .apply()

                                        secureSp?.edit()
                                            ?.putBoolean("is_account_logged_in", true)
                                            ?.putString("user_account_type", resolvedType)
                                            ?.putString("logged_account_id", resolvedAccountId)
                                            ?.apply()

                                        viewModel.setPasswordRecoveryWaitingPhone("")

                                        if (viewModel.adminRole.value !in listOf("OWNER", "ADMIN", "SUPERVISOR")) {
                                            viewModel.authViewModel.setAdminRole(when (resolvedType) {
                                                "PROVIDER" -> "PROVIDER"
                                                "STORE", "RESTAURANT", "MEDICAL" -> "STORE_OWNER"
                                                else -> "GUEST"
                                            })
                                        }

                                        val targetDest = if (match?.provider != null) {
                                            if (match.provider.isDeleted) viewModel.restoreProvider(match.provider.id)
                                            viewModel.selectedProvider = match.provider
                                            viewModel.selectedStore = null
                                            viewModel.selectedProperty = null
                                            viewModel.selectedJob = null
                                            AppScreens.DYNAMIC_PROFILE
                                        } else if (match?.store != null) {
                                            if (match.store.isDeleted) viewModel.restoreStore(match.store.id)
                                            viewModel.selectedStore = match.store
                                            viewModel.selectedProvider = null
                                            viewModel.selectedProperty = null
                                            viewModel.selectedJob = null
                                            AppScreens.DYNAMIC_PROFILE
                                        } else if (match?.property != null) {
                                            if (match.property.isDeleted) viewModel.restoreProperty(match.property.id)
                                            viewModel.selectedProperty = match.property
                                            viewModel.selectedProvider = null
                                            viewModel.selectedStore = null
                                            viewModel.selectedJob = null
                                            AppScreens.DYNAMIC_PROFILE
                                        } else {
                                            viewModel.selectedProvider = null
                                            viewModel.selectedStore = null
                                            viewModel.selectedProperty = null
                                            viewModel.selectedJob = null
                                            AppScreens.USER_BROWSE
                                        }
                                        viewModel.navigateAndRemoveScreens(
                                            targetScreen = targetDest,
                                            screensToRemove = listOf(AppScreens.REGISTER_FORM, AppScreens.PASSWORD_RESET_WAITING, "REGISTER", "REGISTER_FORM", "PASSWORD_RESET_WAITING")
                                        )
                                        Toast.makeText(context, "🔓 أهلاً بك، تم تسجيل الدخول إلى حسابك!", Toast.LENGTH_LONG).show()
                                    }
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981)),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp),
                            enabled = !isSubmittingLogin
                        ) {
                            Text(
                                text = if (isSubmittingLogin) "جاري الدخول..." else "تسجيل الدخول ومتابعة ملفي الشخصي 🔓",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                    } else {
                        // Pending State
                        Text(
                            text = "⏳ طلبك قيد المتابعة لدى الإدارة",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = themeColors.accent,
                            textAlign = TextAlign.Center
                        )
                        Text(
                            text = "تم إشعار إدارة المنصة بطلب إعادة تعيين كلمة المرور لرقمك ($cleanPhone).\nسيقوم المشرف بمراجعة الحساب وتعيين كلمة مرور جديدة وإرسالها لك مباشرة عبر الوسيلة المختارة.",
                            fontSize = 12.sp,
                            color = Color.LightGray,
                            textAlign = TextAlign.Center,
                            lineHeight = 18.sp
                        )

                        // Info card
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = Color.Black.copy(alpha = 0.25f))
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                verticalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Text("📞 رقم الهاتف: $cleanPhone", fontSize = 11.sp, color = Color.White)
                                Text("👤 نوع الحساب: $accountType", fontSize = 11.sp, color = Color.White)
                                Text("🛡️ حالة الطلب: بانتظار المشرف ⌛", fontSize = 11.sp, color = themeColors.accent)
                            }
                        }

                        LinearProgressIndicator(
                            modifier = Modifier.fillMaxWidth(),
                            color = themeColors.accent,
                            trackColor = Color.DarkGray
                        )
                    }
                }
            }

            // Quick Contact with Admin Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = themeColors.surface),
                border = BorderStroke(1.dp, Color.White.copy(alpha = 0.1f))
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text = "💬 وسائل التواصل المباشر مع إدارة المنصة:",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // WhatsApp
                        Button(
                            onClick = {
                                val rawSupport = settingsState.supportWhatsapp.trim().ifBlank { "967777000000" }
                                val adminPhone = if (rawSupport.startsWith("967") || rawSupport.startsWith("+967")) {
                                    rawSupport.removePrefix("+")
                                } else if (rawSupport.length == 9) {
                                    "967$rawSupport"
                                } else {
                                    "967777000000"
                                }
                                val msg = "مرحباً إدارة دليل خدمات اليمن، أطلب تسريع إعادة تعيين كلمة المرور لحسابي المسجل: $cleanPhone"
                                runCatching {
                                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://wa.me/$adminPhone?text=${Uri.encode(msg)}"))
                                    context.startActivity(intent)
                                }.onFailure {
                                    Toast.makeText(context, "❌ تعذر فتح تطبيق واتساب على جهازك", Toast.LENGTH_SHORT).show()
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981)),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.weight(1f).height(42.dp)
                        ) {
                            Icon(Icons.Default.Phone, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("واتساب", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }

                        // Telegram
                        Button(
                            onClick = {
                                runCatching {
                                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://t.me/yemenservices_support"))
                                    context.startActivity(intent)
                                }.onFailure {
                                    Toast.makeText(context, "❌ تعذر فتح تطبيق تيليجرام على جهازك", Toast.LENGTH_SHORT).show()
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0EA5E9)),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.weight(1f).height(42.dp)
                        ) {
                            Icon(Icons.Default.Send, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("تيليجرام", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }

                        // In-app chat
                        Button(
                            onClick = {
                                viewModel.openSupportChat()
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = themeColors.primary),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.weight(1f).height(42.dp)
                        ) {
                            Icon(Icons.Default.Email, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("محادثة", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            // Return to Home Button
            OutlinedButton(
                onClick = onBackClick,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth().height(46.dp),
                border = BorderStroke(1.dp, Color.Gray)
            ) {
                Text("العودة إلى الصفحة الرئيسية 🏠", fontSize = 12.sp, color = Color.White)
            }
        }
    }
}
