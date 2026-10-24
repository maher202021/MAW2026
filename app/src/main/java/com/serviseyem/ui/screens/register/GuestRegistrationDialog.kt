@file:OptIn(androidx.compose.foundation.layout.ExperimentalLayoutApi::class, androidx.compose.material3.ExperimentalMaterial3Api::class)

package com.serviseyem.ui.screens.register
import com.serviseyem.ui.*

import android.content.Context
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Place
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.serviseyem.ui.MainViewModel
import com.serviseyem.ui.screens.register.components.RegistrationField
import com.serviseyem.ui.screens.register.components.RegistrationSubmitButton
import com.serviseyem.utils.Validators
import com.serviseyem.utils.VisualThemePalette
import kotlinx.coroutines.launch

/**
 * 🔒 حالات واجهة تسجيل الزائر واسترجاع الحساب
 */
sealed class GuestAuthUiState {
    object Idle : GuestAuthUiState()
    data class Loading(val message: String) : GuestAuthUiState()
    data class Success(val message: String) : GuestAuthUiState()
    data class Error(val errorMessage: String) : GuestAuthUiState()
}

/**
 * 🔐 GuestRegistrationDialog - نافذة تسجيل الزوار واسترجاع الحسابات
 */
@Composable
fun GuestRegistrationDialog(
    viewModel: MainViewModel,
    themeColors: VisualThemePalette,
    onDismiss: () -> Unit,
    onRegisterCompleted: (String, String, String, String) -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    val currentName = viewModel.currentUserName.collectAsState().value
    val currentPhone = viewModel.currentUserPhone.collectAsState().value
    val currentResidence = viewModel.currentUserResidence.collectAsState().value
    val currentUserId = viewModel.currentUserId.collectAsState().value
    val settingsState by viewModel.settings.collectAsState()

    var isRestoreMode by remember { mutableStateOf(false) }
    var name by remember { mutableStateOf(currentName) }
    var phoneBody by remember {
        mutableStateOf(if (currentPhone.startsWith("+967")) currentPhone.removePrefix("+967") else currentPhone)
    }
    var residence by remember { mutableStateOf(currentResidence) }
    var password by remember { mutableStateOf("") }

    var uiState by remember { mutableStateOf<GuestAuthUiState>(GuestAuthUiState.Idle) }
    var phoneError by remember { mutableStateOf<String?>(null) }
    var nameError by remember { mutableStateOf<String?>(null) }
    var passwordError by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(currentUserId) {
        if (currentUserId.isNotEmpty() && uiState is GuestAuthUiState.Loading) {
            uiState = GuestAuthUiState.Success("تم إنشاء الحساب بنجاح!")
            kotlinx.coroutines.delay(1000)
            onDismiss()
        }
    }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A)),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
                .border(2.dp, themeColors.accent, RoundedCornerShape(16.dp))
        ) {
            Column(
                modifier = Modifier
                    .padding(18.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Top Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (isRestoreMode) "🔓 استرجاع الحساب والبيانات" else "🔐 التحقق من الهوية وتسجيل زائر",
                        fontSize = 13.5.sp,
                        fontWeight = FontWeight.Bold,
                        color = themeColors.accent
                    )
                    IconButton(onClick = onDismiss, modifier = Modifier.size(24.dp)) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "إغلاق", tint = Color.Red)
                    }
                }

                SnackbarHost(hostState = snackbarHostState)

                // Status Banner
                when (val state = uiState) {
                    is GuestAuthUiState.Loading -> {
                        Card(
                            colors = CardDefaults.cardColors(containerColor = themeColors.accent.copy(alpha = 0.15f)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(10.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                CircularProgressIndicator(modifier = Modifier.size(16.dp), color = themeColors.accent, strokeWidth = 2.dp)
                                Text(state.message, fontSize = 11.sp, color = Color.White, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                    is GuestAuthUiState.Error -> {
                        Card(
                            colors = CardDefaults.cardColors(containerColor = Color(0xFFEF5350).copy(alpha = 0.15f)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("❌ ${state.errorMessage}", fontSize = 11.sp, color = Color(0xFFEF5350), modifier = Modifier.padding(10.dp))
                        }
                    }
                    is GuestAuthUiState.Success -> {
                        Card(
                            colors = CardDefaults.cardColors(containerColor = Color(0xFF10B981).copy(alpha = 0.15f)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("✅ ${state.message}", fontSize = 11.sp, color = Color(0xFF10B981), modifier = Modifier.padding(10.dp))
                        }
                    }
                    GuestAuthUiState.Idle -> {}
                }

                if (isRestoreMode) {
                    Text(
                        text = "يرجى إدخال رقم هاتفك وكلمة المرور لاسترجاع حسابك:",
                        fontSize = 11.sp,
                        color = Color.LightGray,
                        lineHeight = 16.sp
                    )

                    RegistrationField(
                        value = phoneBody,
                        onValueChange = { phoneBody = it; phoneError = null },
                        label = "رقم الهاتف المسجل (مثلاً 771234567)",
                        placeholder = "771234567",
                        leadingIcon = Icons.Default.Phone,
                        errorMessage = phoneError,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                        themeColors = themeColors
                    )

                    RegistrationField(
                        value = password,
                        onValueChange = { password = it; passwordError = null },
                        label = "كلمة المرور",
                        leadingIcon = Icons.Default.Lock,
                        isPassword = true,
                        errorMessage = passwordError,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                        themeColors = themeColors
                    )

                    RegistrationSubmitButton(
                        text = "استرجاع الحساب الآن 🔓",
                        onClick = {
                            val cleanPhone = phoneBody.trim()
                            val cleanPassword = password.trim()
                            val phoneVal = Validators.validateYemenPhone(cleanPhone)
                            if (!phoneVal.isValid) {
                                phoneError = phoneVal.errorMessage
                                return@RegistrationSubmitButton
                            }
                            if (cleanPassword.isEmpty()) {
                                passwordError = "يرجى إدخال كلمة المرور"
                                return@RegistrationSubmitButton
                            }

                            val fullPhone = if (cleanPhone.length == 9) cleanPhone else "77$cleanPhone"
                            uiState = GuestAuthUiState.Loading("🔍 جاري البحث عن حسابك برقم الهاتف $fullPhone...")

                            viewModel.restoreGuestUser(context, fullPhone, cleanPassword) { success, msg ->
                                if (success) {
                                    uiState = GuestAuthUiState.Success("تم العثور على حسابك واسترجاع البيانات!")
                                    scope.launch { snackbarHostState.showSnackbar("🔓 تم استرجاع الحساب بنجاح!") }
                                    onDismiss()
                                } else {
                                    uiState = GuestAuthUiState.Error("لم يتم العثور على حساب بهذا الرقم أو كلمة المرور غير صحيحة")
                                }
                            }
                        },
                        isLoading = uiState is GuestAuthUiState.Loading,
                        loadingText = "جاري البحث واسترجاع الحساب...",
                        themeColors = themeColors
                    )

                    TextButton(
                        onClick = { isRestoreMode = false; uiState = GuestAuthUiState.Idle },
                        modifier = Modifier.align(Alignment.CenterHorizontally)
                    ) {
                        Text("إنشاء حساب جديد؟ اضغط هنا للتسجيل", color = themeColors.accent, fontSize = 11.sp)
                    }

                } else {
                    Text(
                        text = "تسجيل عميل جديد",
                        fontSize = 11.sp,
                        color = Color.LightGray,
                        lineHeight = 16.sp,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )

                    com.serviseyem.ui.screens.register.forms.UnifiedRegistrationForm(
                        role = com.serviseyem.data.models.UserRole.CLIENT.code,
                        themeColors = themeColors,
                        onRegistrationSuccess = { data ->
                            val cleanName = data["entityName"] ?: ""
                            val cleanPhone = data["phone"] ?: ""
                            val cleanResidence = data["city"] ?: ""
                            val cleanPassword = data["password"] ?: ""
                            val cleanConfirmPassword = data["confirmPassword"] ?: cleanPassword

                            val fullPhone = if (cleanPhone.length == 9) cleanPhone else "77$cleanPhone"
                            uiState = GuestAuthUiState.Loading("جاري إنشاء الحساب...")
                            viewModel.registerGuestUser(
                                context = context,
                                name = cleanName,
                                phone = fullPhone,
                                residence = cleanResidence,
                                password = cleanPassword,
                                confirmPassword = cleanConfirmPassword
                            )
                            onRegisterCompleted(cleanName, fullPhone, cleanResidence, cleanPassword)
                        }
                    )

                    TextButton(
                        onClick = { isRestoreMode = true; uiState = GuestAuthUiState.Idle },
                        modifier = Modifier.align(Alignment.CenterHorizontally)
                    ) {
                        Text("لديك حساب بالفعل؟ استرجاع الحساب الآن 🔓", color = themeColors.accent, fontSize = 11.sp)
                    }
                }
            }
        }
    }
}
