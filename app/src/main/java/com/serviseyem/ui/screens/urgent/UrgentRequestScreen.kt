package com.serviseyem.ui.screens.urgent

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.result.PickVisualMediaRequest
import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
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
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.serviseyem.ui.MainViewModel
import com.serviseyem.ui.viewmodels.InstantRequestViewModel
import com.serviseyem.ui.viewmodels.InstantUiState
import kotlinx.coroutines.launch

import com.serviseyem.ui.screens.urgent.components.UrgentFormFields

import android.net.Uri
import androidx.compose.ui.draw.clip
import coil.compose.AsyncImage
import com.serviseyem.utils.FirebaseStorageUploader
import java.util.UUID

/**
 * 🚨 UrgentRequestScreen
 * شاشة طلب خدمة عاجلة خلال 30 دقيقة مع مؤقت فوري وتنبيهات أولوية قصوى.
 * تستخدم النمط المعماري MVVM مع InstantRequestViewModel وعرض الملاحظات عبر Snackbar.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UrgentRequestScreen(
    viewModel: MainViewModel,
    instantViewModel: InstantRequestViewModel = viewModel(),
    onNavigateBack: () -> Unit = {},
    onNavigateToUrgentList: () -> Unit = {}
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    val currentUserId by viewModel.currentUserId.collectAsState()
    val currentUserName by viewModel.currentUserName.collectAsState()
    val currentUserPhone by viewModel.currentUserPhone.collectAsState()
    val uiState by instantViewModel.uiState.collectAsState()

    var customerPhone by remember(currentUserPhone) { mutableStateOf(currentUserPhone) }
    var customerName by remember(currentUserName, currentUserPhone) { 
        mutableStateOf(currentUserName.ifEmpty { if (currentUserPhone.isNotEmpty()) "عميل ($currentUserPhone)" else "" }) 
    }
    var selectedDepartment by rememberSaveable { mutableStateOf("خدمات وفنيين") }
    var selectedCategory by rememberSaveable { mutableStateOf(UrgentConstants.getSubCategories("خدمات وفنيين").first()) }
    var serviceTitle by rememberSaveable { mutableStateOf("") }
    var serviceDetails by rememberSaveable { mutableStateOf("") }
    var selectedCity by rememberSaveable { mutableStateOf("صنعاء") }
    var selectedArea by rememberSaveable { mutableStateOf("") }
    var pinCode by rememberSaveable { mutableStateOf("") }
    var isPinVisible by remember { mutableStateOf(false) }
    var attachedImageUri by remember { mutableStateOf<Uri?>(null) }
    var isUploadingImage by remember { mutableStateOf(false) }
    var isSubmitting by remember { mutableStateOf(false) }

    // ⏳ حماية زمنية (30 ثانية) لحالات الإرسال والرفع لتجنب تعليق واجهة المستخدم (القاعدة 7)
    LaunchedEffect(isSubmitting, isUploadingImage) {
        if (isSubmitting || isUploadingImage) {
            kotlinx.coroutines.delay(30_000L)
            isSubmitting = false
            isUploadingImage = false
        }
    }

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        if (uri != null) {
            attachedImageUri = uri
        }
    }

    var createdRequestCode by remember { mutableStateOf<String?>(null) }
    var createdPinCode by remember { mutableStateOf<String?>(null) }
    var showSuccessDialog by remember { mutableStateOf(false) }
    var expandedCategoryDropdown by remember { mutableStateOf(false) }
    var expandedDeptDropdown by remember { mutableStateOf(false) }

    val subCategories = remember(selectedDepartment) {
        UrgentConstants.getSubCategories(selectedDepartment)
    }

    LaunchedEffect(selectedDepartment) {
        selectedCategory = subCategories.firstOrNull() ?: ""
    }

    LaunchedEffect(uiState) {
        when (val state = uiState) {
            is InstantUiState.Error -> {
                scope.launch {
                    snackbarHostState.showSnackbar(state.message)
                }
            }
            else -> {}
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Icon(Icons.Default.Warning, contentDescription = null, tint = Color(0xFFD32F2F))
                        Text("طلب صيانة طارئة (30 دقيقة)", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "رجوع")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // كارد تنبيه الطوارئ
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFFFFEBEE)),
                shape = RoundedCornerShape(12.dp)
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Icon(Icons.Default.Warning, contentDescription = null, tint = Color(0xFFD32F2F))
                    Text(
                        "🚨 سيتم إشعار جميع مقدمي الخدمة والمحلات والمراكز المختصة فوراً في نطاق منطقتك لتقديم عروضهم خلال 30 دقيقة.",
                        fontSize = 13.sp,
                        color = Color(0xFFB71C1C),
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            UrgentFormFields(
                customerName = customerName,
                onCustomerNameChange = { customerName = it },
                customerPhone = customerPhone,
                onCustomerPhoneChange = { customerPhone = it },
                selectedCity = selectedCity,
                onCitySelected = { selectedCity = it },
                selectedArea = selectedArea,
                onAreaChange = { selectedArea = it }
            )

            // اختيار القسم الرئيسي والفرعي (فنيين، محلات وتجارة، مطاعم، مراكز طبية، عقارات)
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Box(modifier = Modifier.weight(1f)) {
                    OutlinedTextField(
                        value = selectedDepartment,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("القسم الرئيسي *") },
                        trailingIcon = {
                            IconButton(onClick = { expandedDeptDropdown = true }) {
                                Icon(Icons.Default.ArrowDropDown, contentDescription = null)
                            }
                        },
                        modifier = Modifier.fillMaxWidth().testTag("urgent_department_selector")
                    )
                    DropdownMenu(
                        expanded = expandedDeptDropdown,
                        onDismissRequest = { expandedDeptDropdown = false }
                    ) {
                        UrgentConstants.departments.forEach { dept ->
                            DropdownMenuItem(
                                text = { Text(dept, fontWeight = FontWeight.Bold) },
                                onClick = {
                                    selectedDepartment = dept
                                    expandedDeptDropdown = false
                                }
                            )
                        }
                    }
                }

                Box(modifier = Modifier.weight(1.2f)) {
                    OutlinedTextField(
                        value = selectedCategory,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("النوع الفرعي *") },
                        trailingIcon = {
                            IconButton(onClick = { expandedCategoryDropdown = true }) {
                                Icon(Icons.Default.ArrowDropDown, contentDescription = null)
                            }
                        },
                        modifier = Modifier.fillMaxWidth().testTag("urgent_subcategory_selector")
                    )
                    DropdownMenu(
                        expanded = expandedCategoryDropdown,
                        onDismissRequest = { expandedCategoryDropdown = false }
                    ) {
                        subCategories.forEach { sub ->
                            DropdownMenuItem(
                                text = { Text(sub) },
                                onClick = {
                                    selectedCategory = sub
                                    expandedCategoryDropdown = false
                                }
                            )
                        }
                    }
                }
            }

            OutlinedTextField(
                value = serviceTitle,
                onValueChange = { serviceTitle = it },
                label = { Text("عنوان الخدمة الطارئة *") },
                placeholder = { Text("مثال: تسرب مياه طارئ في المطبخ") },
                modifier = Modifier.fillMaxWidth().testTag("urgent_service_title"),
                singleLine = true
            )

            OutlinedTextField(
                value = serviceDetails,
                onValueChange = { serviceDetails = it },
                label = { Text("تفاصيل المشكلة والخدمة المطلوب تنفيذها *") },
                placeholder = { Text("اكتب وصفاً توضيحياً للمشكلة لتمكين الفنيين من تقييم العمل وتقديم العرض المناسب...") },
                modifier = Modifier.fillMaxWidth().height(100.dp).testTag("urgent_service_details"),
                maxLines = 4
            )

            // رمز PIN للأمان والإلغاء
            OutlinedTextField(
                value = pinCode,
                onValueChange = { if (it.length <= 6) pinCode = it },
                label = { Text("رمز PIN سري خاص بك للتحكم بالطلب *") },
                placeholder = { Text("مثال: 1234 (لحفظ أمان طلبك وإلغائه)") },
                modifier = Modifier.fillMaxWidth().testTag("urgent_pin_input"),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                visualTransformation = if (isPinVisible) VisualTransformation.None else PasswordVisualTransformation(),
                trailingIcon = {
                    IconButton(onClick = { isPinVisible = !isPinVisible }) {
                        Icon(
                            imageVector = if (isPinVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                            contentDescription = "تبديل الرؤية"
                        )
                    }
                },
                shape = RoundedCornerShape(12.dp),
                singleLine = true
            )

            // إرفاق صورة المشكلة (معاينة وضغط)
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("📷 إرفاق صورة المشكلة (اختياري):", fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                        OutlinedButton(
                            onClick = {
                                photoPickerLauncher.launch(
                                    androidx.activity.result.PickVisualMediaRequest(
                                        ActivityResultContracts.PickVisualMedia.ImageOnly
                                    )
                                )
                            },
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Text(if (attachedImageUri != null) "تغيير الصورة" else "اختيار صورة", fontSize = 12.sp)
                        }
                    }

                    if (attachedImageUri != null) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(140.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .border(1.dp, Color.Gray.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                        ) {
                            AsyncImage(
                                model = attachedImageUri,
                                contentDescription = "معاينة صورة الطلب",
                                modifier = Modifier.fillMaxSize()
                            )
                            IconButton(
                                onClick = { attachedImageUri = null },
                                modifier = Modifier.align(Alignment.TopEnd).background(Color.Black.copy(alpha = 0.6f), RoundedCornerShape(4.dp))
                            ) {
                                Icon(Icons.Default.Delete, contentDescription = "حذف الصورة", tint = Color.Red)
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // زر إرسال الطلب العاجل
            val isLoading = isSubmitting || isUploadingImage || (uiState is InstantUiState.Loading)
            Button(
                onClick = {
                    if (isSubmitting || isLoading) return@Button
                    if (currentUserId.isBlank()) {
                        scope.launch { snackbarHostState.showSnackbar("يرجى تسجيل الدخول أو تفعيل الهوية أولاً") }
                        return@Button
                    }
                    if (customerPhone.isBlank() || serviceTitle.isBlank() || serviceDetails.isBlank() || selectedArea.isBlank()) {
                        scope.launch { snackbarHostState.showSnackbar("يرجى تعبئة كافة الحقول الإجبارية (*)") }
                        return@Button
                    }
                    val cleanPhone = com.serviseyem.ui.helpers.AppPreferenceHelper.normalizePhoneNumber(customerPhone).trim()
                    val isValidPhone = cleanPhone.length == 9 && cleanPhone.all { it.isDigit() } && listOf("77", "73", "71", "70", "78").any { cleanPhone.startsWith(it) }
                    if (!isValidPhone) {
                        scope.launch { snackbarHostState.showSnackbar("رقم الهاتف غير صحيح! يجب أن يكون 9 أرقام ويبدأ بـ 77/73/71/70/78") }
                        return@Button
                    }
                    if (pinCode.length < 4) {
                        scope.launch { snackbarHostState.showSnackbar("يرجى كتابة رمز PIN مكون من 4 أرقام") }
                        return@Button
                    }

                    isSubmitting = true

                    val executeCreateRequest: (String) -> Unit = { uploadedImageUrl ->
                        try {
                            instantViewModel.createInstantRequest(
                                userId = currentUserId,
                                userName = customerName,
                                userPhone = cleanPhone,
                                userCity = selectedCity,
                                userNeighborhood = selectedArea,
                                categoryId = selectedDepartment,
                                categoryName = selectedCategory,
                                serviceTitle = serviceTitle,
                                description = if (uploadedImageUrl.isNotBlank()) "$serviceDetails\n[مرفق صورة: $uploadedImageUrl]" else serviceDetails,
                                customPin = pinCode,
                                onResult = { success, returnedCodeOrMsg, returnedPin ->
                                    isSubmitting = false
                                    if (success) {
                                        createdRequestCode = returnedCodeOrMsg.ifBlank { "URG-XXXX" }
                                        createdPinCode = returnedPin.ifBlank { pinCode }
                                        showSuccessDialog = true
                                    } else {
                                        scope.launch { snackbarHostState.showSnackbar(returnedCodeOrMsg) }
                                    }
                                }
                            )
                        } catch (e: Exception) {
                            isSubmitting = false
                            scope.launch { snackbarHostState.showSnackbar("حدث خطأ أثناء إرسال الطلب") }
                        }
                    }

                    val uriToUpload = attachedImageUri
                    if (uriToUpload != null) {
                        isUploadingImage = true
                        scope.launch {
                            var uploadedUrl = ""
                            try {
                                val path = "urgent_requests/req_${System.currentTimeMillis()}_${java.util.UUID.randomUUID().toString().take(6)}.webp"
                                val result = FirebaseStorageUploader.uploadImageUri(
                                    context = context,
                                    uri = uriToUpload,
                                    storagePath = path,
                                    maxDimension = 800,
                                    maxSizeBytes = 300 * 1024L
                                )
                                uploadedUrl = result.getOrDefault("")
                            } catch (_: Exception) {
                                uploadedUrl = ""
                            } finally {
                                isUploadingImage = false
                            }
                            executeCreateRequest(uploadedUrl)
                        }
                    } else {
                        executeCreateRequest("")
                    }
                },
                modifier = Modifier.fillMaxWidth().height(52.dp).testTag("submit_urgent_request_btn"),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD32F2F)),
                enabled = !isLoading
            ) {
                if (isLoading) {
                    CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                } else {
                    Icon(Icons.Default.Warning, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("إرسال الطلب العاجل (مؤقت 30 دقيقة)", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                }
            }

            OutlinedButton(
                onClick = onNavigateBack,
                modifier = Modifier.fillMaxWidth().height(48.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("إلغاء والعودة")
            }
        }
    }

    if (showSuccessDialog) {
        AlertDialog(
            onDismissRequest = {},
            title = {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Color(0xFFD32F2F))
                    Text("تم تعميم الطلب العاجل!", fontWeight = FontWeight.Bold)
                }
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("تم تعميم طلبك على الفنيين المتاحين فوراً.")
                    Text("رمز الطلب: ${createdRequestCode ?: "URG-XXXX"}", fontWeight = FontWeight.Bold, color = Color(0xFFD32F2F))
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "رمز PIN السري: ${if (isPinVisible) (createdPinCode ?: pinCode) else "••••"}",
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF1E293B)
                        )
                        IconButton(onClick = { isPinVisible = !isPinVisible }, modifier = Modifier.size(32.dp)) {
                            Icon(
                                imageVector = if (isPinVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                contentDescription = if (isPinVisible) "إخفاء الرمز" else "إظهار الرمز",
                                tint = Color(0xFF64748B)
                            )
                        }
                    }
                    Text("⏳ ستبدأ العروض بالظهور خلال 30 دقيقة عبر قائمة الطلبات العاجلة.", fontSize = 13.sp)
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        showSuccessDialog = false
                        onNavigateToUrgentList()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD32F2F))
                ) {
                    Text("متابعة العروض الآن")
                }
            }
        )
    }
}
