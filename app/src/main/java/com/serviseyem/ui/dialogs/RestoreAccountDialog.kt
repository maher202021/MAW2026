package com.serviseyem.ui.dialogs

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.serviseyem.ui.*
import com.serviseyem.utils.VisualThemePalette

@Composable
fun RestoreAccountDialog(
    viewModel: MainViewModel,
    themeColors: VisualThemePalette,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    var restorePhoneInput by rememberSaveable { mutableStateOf("") }
    var restorePasswordInput by rememberSaveable { mutableStateOf("") }
    var restoreStep by rememberSaveable { mutableStateOf(1) }
    var selectedChannel by rememberSaveable { mutableStateOf("IN_APP_CHAT") }
    var isSearchingAccount by remember { mutableStateOf(false) }
    var isVerifyingPassword by remember { mutableStateOf(false) }
    var isRequestingReset by remember { mutableStateOf(false) }
    var matchResult by remember { mutableStateOf<MainViewModel.RestoreAccountMatch?>(null) }
    var showSuccessState by rememberSaveable { mutableStateOf(false) }
    var successUserName by rememberSaveable { mutableStateOf("") }
    var targetSuccessScreen by rememberSaveable { mutableStateOf(AppScreens.USER_BROWSE) }

    val sp = remember(context) {
        context.getSharedPreferences("yemen_service_prefs", android.content.Context.MODE_PRIVATE)
    }
    val secureSp = remember(context) {
        runCatching {
            val masterKey = androidx.security.crypto.MasterKey.Builder(context)
                .setKeyScheme(androidx.security.crypto.MasterKey.KeyScheme.AES256_GCM)
                .build()
            androidx.security.crypto.EncryptedSharedPreferences.create(
                context,
                "yemen_service_secure_prefs",
                masterKey,
                androidx.security.crypto.EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                androidx.security.crypto.EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
            )
        }.getOrNull()
    }

    LaunchedEffect(restoreStep) {
        if (restoreStep == 2 && matchResult == null) {
            val cleanPhone = com.serviseyem.domain.usecases.ValidatePhoneUseCase.normalizePhone(restorePhoneInput).filter { it.isDigit() }
            if (cleanPhone.length >= 7) {
                isSearchingAccount = true
                viewModel.searchAccountForRestore(cleanPhone) { match ->
                    isSearchingAccount = false
                    if (match != null) {
                        matchResult = match
                    } else {
                        restoreStep = 1
                    }
                }
            } else {
                restoreStep = 1
            }
        }
    }

    LaunchedEffect(isSearchingAccount) {
        if (isSearchingAccount) {
            kotlinx.coroutines.delay(30_000L)
            isSearchingAccount = false
        }
    }
    LaunchedEffect(isVerifyingPassword) {
        if (isVerifyingPassword) {
            kotlinx.coroutines.delay(30_000L)
            isVerifyingPassword = false
        }
    }
    LaunchedEffect(isRequestingReset) {
        if (isRequestingReset) {
            kotlinx.coroutines.delay(30_000L)
            isRequestingReset = false
        }
    }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.padding(16.dp).fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(16.dp).verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text("🔑 استعادة حسابك برقم الهاتف", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)

                if (showSuccessState) {
                    Column(
                        modifier = Modifier.fillMaxWidth().padding(8.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("🎉 تم استعادة الحساب بنجاح!", color = Color(0xFF4ADE80), fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        Text("أهلاً وسهلاً بك مجدداً: $successUserName", color = Color.White, fontSize = 14.sp)
                        Text("تم توثيق جهازك وتسجيل الدخول بنجاح إلى حسابك.", color = Color.LightGray, fontSize = 12.sp)

                        Button(
                            onClick = {
                                viewModel.navigateToScreen(targetSuccessScreen)
                                onDismiss()
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = themeColors.accent),
                            modifier = Modifier.fillMaxWidth().height(48.dp),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("موافق (الانتقال للوحة الحساب) 🚀", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        }
                    }
                } else if (restoreStep == 1) {
                    Text("الرجاء إدخال رقم الهاتف المسجل به حسابك للبحث المباشر:", color = Color.LightGray, fontSize = 11.sp)
                    OutlinedTextField(
                        value = restorePhoneInput,
                        onValueChange = { restorePhoneInput = it },
                        label = { Text("رقم الهاتف المسجل") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth().testTag("restore_phone_input"),
                        colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White)
                    )

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Button(
                            onClick = {
                                val cleanPhone = com.serviseyem.domain.usecases.ValidatePhoneUseCase.normalizePhone(restorePhoneInput).filter { it.isDigit() }
                                if (cleanPhone.length >= 7) {
                                    isSearchingAccount = true
                                    viewModel.searchAccountForRestore(cleanPhone) { match ->
                                        isSearchingAccount = false
                                        if (match != null) {
                                            matchResult = match
                                            restoreStep = 2
                                        } else {
                                            Toast.makeText(context, "❌ لا يوجد حساب مسجل بهذا الرقم!", Toast.LENGTH_LONG).show()
                                        }
                                    }
                                } else {
                                    Toast.makeText(context, "❌ يرجى إدخال رقم هاتف صحيح!", Toast.LENGTH_LONG).show()
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = themeColors.accent),
                            modifier = Modifier.weight(1f).height(48.dp).testTag("restore_next_button"),
                            enabled = !isSearchingAccount
                        ) {
                            Text(if (isSearchingAccount) "جاري البحث..." else "التالي ➡️", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }
                        Button(
                            onClick = onDismiss,
                            colors = ButtonDefaults.buttonColors(containerColor = Color.Gray),
                            modifier = Modifier.weight(1f).height(48.dp)
                        ) {
                            Text("إلغاء", color = Color.White, fontSize = 12.sp)
                        }
                    }
                } else {
                    val match = matchResult
                    val provName = match?.name?.takeIf { it.isNotBlank() } ?: "مستخدم"
                    Text("👤 تم العثور على حساب (${match?.type ?: "CLIENT"}) لـ: $provName", color = themeColors.accent, fontWeight = FontWeight.Bold, fontSize = 11.5.sp)
                    Text("الرجاء إدخال كلمة المرور للتحقق واسترجاع البيانات:", color = Color.LightGray, fontSize = 10.5.sp)

                    OutlinedTextField(
                        value = restorePasswordInput,
                        onValueChange = { restorePasswordInput = it },
                        placeholder = { Text("أدخل كلمة المرور") },
                        visualTransformation = PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth().testTag("restore_password_input"),
                        colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White)
                    )

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Button(
                            onClick = {
                                val currentMatch = matchResult
                                val cleanPhone = com.serviseyem.domain.usecases.ValidatePhoneUseCase.normalizePhone(restorePhoneInput).filter { it.isDigit() }
                                val cleanPassword = restorePasswordInput.trim()
                                if (currentMatch == null || cleanPhone.length < 7) {
                                    Toast.makeText(context, "❌ بيانات الحساب غير صالحة، يرجى إعادة البحث برقم الهاتف.", Toast.LENGTH_LONG).show()
                                    restoreStep = 1
                                    return@Button
                                }
                                if (cleanPassword.isBlank()) {
                                    Toast.makeText(context, "❌ يرجى إدخال كلمة المرور!", Toast.LENGTH_LONG).show()
                                    return@Button
                                }
                                isVerifyingPassword = true
                                viewModel.verifyRestorePassword(
                                    cleanPhone = cleanPhone,
                                    accountType = currentMatch.type.ifBlank { "CLIENT" },
                                    passwordInput = cleanPassword
                                ) { isPasswordCorrect ->
                                    isVerifyingPassword = false
                                    if (!isPasswordCorrect) {
                                        Toast.makeText(context, "❌ كلمة المرور غير صحيحة! تأكد منها أو اطلب الاستعادة من الإدارة.", Toast.LENGTH_LONG).show()
                                    } else {
                                        val provArea = currentMatch.provider?.area?.takeIf { it.isNotBlank() }
                                            ?: currentMatch.store?.cityId?.takeIf { it.isNotBlank() }
                                            ?: currentMatch.property?.cityId?.takeIf { it.isNotBlank() }
                                            ?: currentMatch.job?.cityId?.takeIf { it.isNotBlank() }
                                            ?: "اليمن"
                                        viewModel.setUserSessionDetails(context, provName, cleanPhone, provArea)

                                        val resolvedAccountType = currentMatch.type.ifBlank { "CLIENT" }
                                        val resolvedAccountId = currentMatch.provider?.id
                                            ?: currentMatch.store?.id
                                            ?: currentMatch.property?.id
                                            ?: currentMatch.job?.id
                                            ?: "usr_$cleanPhone"

                                        if (resolvedAccountId.isNotBlank()) {
                                            viewModel._currentUserId.value = resolvedAccountId
                                        }

                                        sp.edit()
                                            .remove("is_account_logged_in")
                                            .remove("user_account_type")
                                            .remove("logged_account_id")
                                            .apply()

                                        secureSp?.edit()
                                            ?.putBoolean("is_account_logged_in", true)
                                            ?.putString("user_account_type", resolvedAccountType)
                                            ?.putString("logged_account_id", resolvedAccountId)
                                            ?.apply()

                                        viewModel.setJoinRequestPhone(context, cleanPhone)
                                        if (viewModel.adminRole.value !in listOf("OWNER", "ADMIN", "SUPERVISOR")) {
                                            viewModel.authViewModel.setAdminRole(
                                                when (resolvedAccountType.uppercase()) {
                                                    "PROVIDER" -> "PROVIDER"
                                                    "STORE", "RESTAURANT", "MEDICAL" -> "STORE_OWNER"
                                                    "PROPERTY" -> "PROPERTY_OWNER"
                                                    "JOB" -> "JOB_POSTER"
                                                    else -> "GUEST"
                                                }
                                            )
                                        }

                                        when {
                                            currentMatch.provider != null -> {
                                                if (currentMatch.provider.isDeleted) viewModel.restoreProvider(currentMatch.provider.id)
                                                viewModel.selectedProvider = currentMatch.provider
                                                viewModel.selectedStore = null
                                                viewModel.selectedProperty = null
                                                viewModel.selectedJob = null
                                                val pCat = currentMatch.provider.categoryId
                                                val isMed = pCat.contains("medical", ignoreCase = true) ||
                                                    pCat.contains("clinic", ignoreCase = true) ||
                                                    pCat.contains("pharmacy", ignoreCase = true) ||
                                                    currentMatch.provider.profession.contains("طب", ignoreCase = true)
                                                targetSuccessScreen = if (isMed) AppScreens.MEDICAL_DASHBOARD else AppScreens.PROVIDER_DASHBOARD
                                            }
                                            currentMatch.store != null -> {
                                                if (currentMatch.store.isDeleted) viewModel.restoreStore(currentMatch.store.id)
                                                viewModel.selectedStore = currentMatch.store
                                                viewModel.selectedProvider = null
                                                viewModel.selectedProperty = null
                                                viewModel.selectedJob = null
                                                targetSuccessScreen = when (currentMatch.store.sectionId.lowercase()) {
                                                    "restaurants", "restaurant" -> AppScreens.RESTAURANT_DASHBOARD
                                                    "medical" -> AppScreens.MEDICAL_DASHBOARD
                                                    else -> AppScreens.STORE_DASHBOARD
                                                }
                                            }
                                            currentMatch.property != null -> {
                                                if (currentMatch.property.isDeleted) viewModel.restoreProperty(currentMatch.property.id)
                                                viewModel.selectedProperty = currentMatch.property
                                                viewModel.selectedProvider = null
                                                viewModel.selectedStore = null
                                                viewModel.selectedJob = null
                                                targetSuccessScreen = AppScreens.PROPERTY_DASHBOARD
                                            }
                                            currentMatch.job != null -> {
                                                if (currentMatch.job.isDeleted) viewModel.restoreJob(currentMatch.job.id)
                                                viewModel.selectedJob = currentMatch.job
                                                viewModel.selectedProvider = null
                                                viewModel.selectedStore = null
                                                viewModel.selectedProperty = null
                                                targetSuccessScreen = AppScreens.JOB_POSTER_DASHBOARD
                                            }
                                            else -> {
                                                viewModel.selectedProvider = null
                                                viewModel.selectedStore = null
                                                viewModel.selectedProperty = null
                                                viewModel.selectedJob = null
                                                targetSuccessScreen = "CLIENT_DASHBOARD"
                                            }
                                        }

                                        restorePasswordInput = ""
                                        successUserName = provName
                                        showSuccessState = true
                                    }
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = themeColors.accent),
                            modifier = Modifier.weight(1f).height(48.dp).testTag("restore_confirm_button"),
                            enabled = !isVerifyingPassword && !isRequestingReset
                        ) {
                            Text(if (isVerifyingPassword) "جاري التحقق..." else "تأكيد ودخول 🔓", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 11.5.sp)
                        }
                        Button(
                            onClick = {
                                restorePasswordInput = ""
                                matchResult = null
                                restoreStep = 1
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color.Gray),
                            modifier = Modifier.weight(1f).height(48.dp),
                            enabled = !isVerifyingPassword && !isRequestingReset
                        ) {
                            Text("رجوع", color = Color.White, fontSize = 11.5.sp)
                        }
                    }

                    HorizontalDivider(color = Color.White.copy(alpha = 0.1f))
                    Text("نسيت كلمة المرور؟ اختر وسيلة استلام كلمة المرور المؤقتة من الإدارة:", fontSize = 10.5.sp, color = Color.LightGray)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        val channels = listOf(
                            "IN_APP_CHAT" to "التطبيق",
                            "WHATSAPP" to "واتساب",
                            "TELEGRAM" to "تيليجرام"
                        )
                        channels.forEach { (code, label) ->
                            val selected = selectedChannel == code
                            OutlinedButton(
                                onClick = { selectedChannel = code },
                                shape = RoundedCornerShape(8.dp),
                                border = BorderStroke(1.dp, if (selected) themeColors.accent else Color.Gray.copy(alpha = 0.4f)),
                                colors = ButtonDefaults.outlinedButtonColors(
                                    containerColor = if (selected) themeColors.accent.copy(alpha = 0.2f) else Color.Transparent,
                                    contentColor = if (selected) themeColors.accent else Color.White
                                ),
                                contentPadding = PaddingValues(horizontal = 6.dp, vertical = 4.dp),
                                modifier = Modifier.weight(1f).height(48.dp)
                            ) {
                                Text(label, fontSize = 10.5.sp, fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal)
                            }
                        }
                    }

                    Button(
                        onClick = {
                            val cleanPhone = com.serviseyem.domain.usecases.ValidatePhoneUseCase.normalizePhone(restorePhoneInput).filter { it.isDigit() }
                            if (cleanPhone.length < 7) {
                                Toast.makeText(context, "❌ رقم الهاتف غير صالح لإرسال طلب الاستعادة!", Toast.LENGTH_SHORT).show()
                                return@Button
                            }
                            isRequestingReset = true
                            viewModel.requestPasswordReset(
                                context = context,
                                phone = cleanPhone,
                                name = provName,
                                accountType = match?.type ?: "USER",
                                channel = selectedChannel
                            ) { success ->
                                isRequestingReset = false
                                if (success) {
                                    Toast.makeText(context, "⏳ تم إرسال طلب استعادة كلمة المرور للإدارة بنجاح!", Toast.LENGTH_LONG).show()
                                    viewModel.navigateToScreen(AppScreens.PASSWORD_RESET_WAITING)
                                    onDismiss()
                                } else {
                                    Toast.makeText(context, "❌ حدث خطأ، يرجى المحاولة لاحقاً", Toast.LENGTH_SHORT).show()
                                }
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = themeColors.secondary),
                        modifier = Modifier.fillMaxWidth().height(48.dp).testTag("restore_request_admin_button"),
                        enabled = !isRequestingReset && !isVerifyingPassword
                    ) {
                        Text(
                            if (isRequestingReset) "جاري إرسال الطلب..." else "💬 طلب الاستعادة من الأدمن عبر القناة المختارة",
                            color = Color.White,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}
