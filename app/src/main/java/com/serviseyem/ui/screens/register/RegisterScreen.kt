package com.serviseyem.ui.screens.register

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.serviseyem.data.models.UserRole
import com.serviseyem.ui.*
import com.serviseyem.ui.MainViewModel
import com.serviseyem.ui.dialogs.RestoreAccountDialog
import com.serviseyem.ui.helpers.AppPreferenceHelper
import com.serviseyem.ui.screens.register.forms.*
import com.serviseyem.ui.screens.register.status.JoinStatus
import com.serviseyem.ui.screens.register.status.JoinStatusUseCase
import com.serviseyem.utils.VisualThemePalette

/**
 * RegisterScreen - الشاشة المتكاملة لخيارات التسجيل والانضمام بـ "دليل خدمات اليمن"
 * - رقم الهاتف فقط بدون بريد إلكتروني لجميع الفئات الـ 8
 * - جميع الطلبات تُرفع للإدارة للمراجعة والاعتماد أو الرفض
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegisterScreen(
    viewModel: MainViewModel,
    themeColors: VisualThemePalette,
    onBackClick: () -> Unit = {},
    onSelectRegistrationType: (RegistrationType) -> Unit = {},
    onOpenRestoreDialog: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var showRestoreDialog by remember { mutableStateOf(false) }
    val targetTypeStr by viewModel.targetRegistrationType.collectAsState()
    var selectedType by remember(targetTypeStr) {
        mutableStateOf(RegistrationType.fromId(targetTypeStr))
    }
    var forceShowForm by remember(targetTypeStr) {
        mutableStateOf(!targetTypeStr.isNullOrBlank())
    }
    var isSubmittingForm by remember { mutableStateOf(false) }
    var formSubmissionError by remember { mutableStateOf<String?>(null) }

    val joinPhone by viewModel.joinRequestPhone.collectAsState()
    val pendingProviders by viewModel.pendingProviders.collectAsState()
    val providers by viewModel.providers.collectAsState()
    val notifications by viewModel.notifications.collectAsState()
    val stores by viewModel.stores.collectAsState()
    val properties by viewModel.properties.collectAsState()
    val categories by viewModel.categories.collectAsState()
    val jobs by viewModel.jobs.collectAsState()
    val registeredUsersList by viewModel.registeredUsersList.collectAsState()
    val jobSeekers by viewModel.jobSeekers.collectAsState()
    val formsConfig by viewModel.registrationFormsConfig.collectAsState()

    val useCase = remember { JoinStatusUseCase() }
    val currentStatus = remember(
        joinPhone, pendingProviders, providers, stores, properties, categories, notifications, jobs, registeredUsersList, jobSeekers
    ) {
        useCase.determineStatus(
            joinPhone = joinPhone,
            pendingProviders = pendingProviders,
            providers = providers,
            stores = stores,
            properties = properties,
            categories = categories,
            notifications = notifications,
            jobs = jobs,
            registeredUsersList = registeredUsersList,
            jobSeekers = jobSeekers
        )
    }

    // معالجة زر الرجوع في النظام عند فتح استمارة فرعية
    BackHandler(enabled = selectedType != null) {
        selectedType = null
        forceShowForm = false
        isSubmittingForm = false
        formSubmissionError = null
        viewModel.setTargetRegistrationType(null)
    }

    // إذا كان للمستخدم طلب قيد الانتظار أو تم اعتماده، نعرض شاشة الانتظار أو لوحة التحكم مباشرة
    if (currentStatus !is JoinStatus.NoRequest && !forceShowForm) {
        JoinRequestStatusScreen(
            viewModel = viewModel,
            themeColors = themeColors,
            modifier = modifier
        )
        return
    }

    // إرسال النموذج وتوجيهه بحسب القسم مع انتظار نتيجة التحقق والرفع
    val handleFormSubmit: (Map<String, Any>, RegistrationType) -> Unit = { data, type ->
        val cleanPhone = AppPreferenceHelper.normalizePhoneNumber((data["phone"] as? String) ?: "")
        val password = (data["password"] as? String) ?: ""
        val area = ((data["city"] as? String) ?: "صنعاء").ifBlank { "صنعاء" }
        val address = ((data["address"] as? String) ?: "").trim()
        val photoPath = (data["imageUri"] as? String) ?: ""
        val idCardPath = (data["idCardUri"] as? String) ?: ""
        val fullName = ((data["fullName"] as? String) ?: (data["ownerName"] as? String) ?: "").trim()
        val entityName = ((data["entityName"] as? String) ?: "").trim()
        val spec = ((data["specialization"] as? String) ?: (data["craftType"] as? String) ?: "").trim()
        val description = ((data["description"] as? String) ?: "").trim()
        val workingHours = ((data["workingHours"] as? String) ?: "").trim()
        val experienceYears = ((data["experienceYears"] as? String) ?: "").trim()
        val hasDelivery = (data["hasDelivery"] as? String)?.toBoolean() == true
        val hasEmergency = (data["hasEmergency"] as? String)?.toBoolean() == true

        val catId: String
        val displayName: String
        val customCategory: String
        when (type) {
            RegistrationType.PROVIDER -> {
                catId = spec.ifBlank { "صيانة عامة" }
                displayName = fullName.ifBlank { "فني جديد" }
                customCategory = spec.ifBlank { "صيانة عامة" }
            }
            RegistrationType.STORE -> {
                catId = "STORE"
                displayName = entityName.ifBlank { fullName.ifBlank { "متجر جديد" } }
                customCategory = spec.ifBlank { "محلات ومراكز تجارية" }
            }
            RegistrationType.RESTAURANT -> {
                catId = "RESTAURANT"
                displayName = entityName.ifBlank { fullName.ifBlank { "مطعم جديد" } }
                customCategory = spec.ifBlank { "مطاعم وكافيهات" }
            }
            RegistrationType.MEDICAL -> {
                catId = "MEDICAL"
                displayName = entityName.ifBlank { fullName.ifBlank { "مركز طبي / عيادة" } }
                customCategory = spec.ifBlank { "مراكز طبية وعيادات" }
            }
            RegistrationType.PROPERTY -> {
                catId = "PROPERTY"
                displayName = entityName.ifBlank { fullName.ifBlank { "مكتب عقاري" } }
                customCategory = spec.ifBlank { "عقارات وأراضي" }
            }
            RegistrationType.JOB -> {
                catId = "JOB"
                displayName = entityName.ifBlank { fullName.ifBlank { "شركة معلنة" } }
                customCategory = spec.ifBlank { "إعلان وظيفة" }
            }
            RegistrationType.JOB_SEEKER -> {
                catId = "JOB_SEEKER"
                displayName = fullName.ifBlank { "متقدم للوظيفة" }
                customCategory = spec.ifBlank { "باحث عن عمل" }
            }
            RegistrationType.CLIENT -> {
                catId = "CLIENT"
                displayName = fullName.ifBlank { "عميل جديد" }
                customCategory = "حساب عميل"
            }
        }

        isSubmittingForm = true
        formSubmissionError = null

        viewModel.submitJoinForm(
            context = context,
            name = displayName,
            phone = cleanPhone,
            catId = catId,
            area = area,
            neighborhood = address,
            photoPath = photoPath,
            idCardPath = idCardPath,
            gpsCoords = "",
            customCategoryName = customCategory,
            password = password,
            ownerName = fullName.ifBlank { displayName },
            description = description,
            workingHours = workingHours,
            hasDelivery = hasDelivery,
            hasEmergency = hasEmergency,
            experienceYears = experienceYears,
            onResult = { ok, msg ->
                isSubmittingForm = false
                if (ok) {
                    val draftKey = when (type) {
                        RegistrationType.CLIENT -> UserRole.CLIENT.code
                        RegistrationType.PROVIDER -> "PROVIDER"
                        RegistrationType.STORE -> "STORE"
                        RegistrationType.RESTAURANT -> "RESTAURANT"
                        RegistrationType.MEDICAL -> "MEDICAL"
                        RegistrationType.PROPERTY -> "PROPERTY"
                        RegistrationType.JOB -> "JOB"
                        RegistrationType.JOB_SEEKER -> "JOB_SEEKER"
                    }
                    RegistrationDraftManager(context).clearDraft(draftKey)
                    viewModel.setTargetRegistrationType(null)
                    forceShowForm = false
                    selectedType = null
                } else {
                    formSubmissionError = msg
                }
            }
        )
    }

    Scaffold(
        topBar = {
            if (selectedType == null) {
                TopAppBar(
                    title = {
                        Column {
                            Text(
                                text = "📝 التسجيل والانضمام",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            Text(
                                text = "منصتك الشاملة للخدمات والأنشطة في اليمن",
                                fontSize = 10.5.sp,
                                color = themeColors.accent
                            )
                        }
                    },
                    navigationIcon = {
                        IconButton(
                            onClick = {
                                viewModel.setTargetRegistrationType(null)
                                onBackClick()
                            },
                            modifier = Modifier.size(48.dp)
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "رجوع",
                                tint = Color.White
                            )
                        }
                    },
                    actions = {
                        if (currentStatus !is JoinStatus.NoRequest) {
                            TextButton(
                                onClick = { forceShowForm = false },
                                modifier = Modifier.defaultMinSize(minHeight = 48.dp)
                            ) {
                                Text("حالة طلبي ⏳", color = themeColors.accent, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFF0F172A))
                )
            }
        },
        containerColor = Color(0xFF0F172A),
        modifier = modifier.fillMaxSize()
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            if (selectedType == null) {
                val allTypes = remember { RegistrationType.values().toList() }
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 14.dp, vertical = 10.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "📌 اختر نوع الحساب المناسب لك:",
                                fontSize = 13.5.sp,
                                fontWeight = FontWeight.Bold,
                                color = themeColors.accent
                            )
                            Text(
                                text = "${allTypes.size} خيارات معتمدة",
                                fontSize = 10.5.sp,
                                color = Color.Gray
                            )
                        }
                    }

                    item {
                        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            allTypes.forEach { regType ->
                                Card(
                                    onClick = {
                                        formSubmissionError = null
                                        isSubmittingForm = false
                                        selectedType = regType
                                        onSelectRegistrationType(regType)
                                    },
                                    colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
                                    border = BorderStroke(1.dp, Color.White.copy(alpha = 0.12f)),
                                    shape = RoundedCornerShape(14.dp),
                                    modifier = Modifier.fillMaxWidth().testTag("reg_type_${regType.id}")
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(14.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(46.dp)
                                                .clip(RoundedCornerShape(12.dp))
                                                .background(themeColors.accent.copy(alpha = 0.15f)),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(regType.icon, fontSize = 22.sp)
                                        }

                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(
                                                text = regType.title,
                                                fontSize = 13.5.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = Color.White
                                            )
                                            Spacer(modifier = Modifier.height(2.dp))
                                            Text(
                                                text = regType.description,
                                                fontSize = 10.5.sp,
                                                color = Color.LightGray,
                                                lineHeight = 15.sp
                                            )
                                        }

                                        Button(
                                            onClick = {
                                                formSubmissionError = null
                                                isSubmittingForm = false
                                                selectedType = regType
                                                onSelectRegistrationType(regType)
                                            },
                                            colors = ButtonDefaults.buttonColors(containerColor = themeColors.accent),
                                            shape = RoundedCornerShape(8.dp),
                                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                                            modifier = Modifier.defaultMinSize(minHeight = 48.dp)
                                        ) {
                                            Text(
                                                text = "تعبئة 👈",
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = Color.Black
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // استرجاع حساب
                    item {
                        Card(
                            onClick = {
                                showRestoreDialog = true
                                onOpenRestoreDialog()
                            },
                            colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A)),
                            border = BorderStroke(1.dp, themeColors.accent.copy(alpha = 0.4f)),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth().testTag("reg_open_restore_card")
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(14.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text("🔓", fontSize = 20.sp)
                                    Column {
                                        Text(
                                            text = "لديك حساب بالفعل في دليل الخدمات؟",
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color.White
                                        )
                                        Text(
                                            text = "اضغط هنا لاسترجاع حسابك وحجوزاتك السابقة بالكامل",
                                            fontSize = 10.5.sp,
                                            color = themeColors.accent
                                        )
                                    }
                                }
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                    contentDescription = "استرجاع",
                                    tint = themeColors.accent
                                )
                            }
                        }
                    }

                    item {
                        Text(
                            text = "بالتسجيل بالمنصة برقم الهاتف، فإنك توافق على مراجعة طلبك من الإدارة وشروط حماية البيانات.",
                            fontSize = 10.sp,
                            color = Color.Gray,
                            textAlign = TextAlign.Center,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 10.dp)
                        )
                    }
                }
            } else {
                // شاشة استمارة التسجيل المحددة
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 14.dp, vertical = 8.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            IconButton(
                                onClick = {
                                    selectedType = null
                                    forceShowForm = false
                                    isSubmittingForm = false
                                    formSubmissionError = null
                                    viewModel.setTargetRegistrationType(null)
                                },
                                modifier = Modifier.size(48.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                    contentDescription = "رجوع",
                                    tint = Color.White
                                )
                            }
                            Text(selectedType?.icon ?: "📋", fontSize = 18.sp)
                            Text(
                                text = selectedType?.title ?: "استمارة التسجيل",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                        TextButton(
                            onClick = {
                                selectedType = null
                                forceShowForm = false
                                isSubmittingForm = false
                                formSubmissionError = null
                                viewModel.setTargetRegistrationType(null)
                            },
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                            modifier = Modifier.defaultMinSize(minHeight = 48.dp)
                        ) {
                            Text("تغيير 🔁", color = themeColors.accent, fontSize = 11.sp)
                        }
                    }

                    Box(modifier = Modifier.weight(1f)) {
                        when (selectedType) {
                            RegistrationType.PROVIDER -> {
                                ProviderForm(
                                    themeColors = themeColors,
                                    formsConfig = formsConfig,
                                    isSubmittingExternal = isSubmittingForm,
                                    externalError = formSubmissionError,
                                    onClearExternalError = { formSubmissionError = null },
                                    onSubmit = { data -> handleFormSubmit(data, RegistrationType.PROVIDER) }
                                )
                            }
                            RegistrationType.STORE -> {
                                UnifiedRegistrationForm(
                                    role = "STORE",
                                    themeColors = themeColors,
                                    formsConfig = formsConfig,
                                    isSubmittingExternal = isSubmittingForm,
                                    externalError = formSubmissionError,
                                    onClearExternalError = { formSubmissionError = null },
                                    onRegistrationSuccess = { map -> handleFormSubmit(map, RegistrationType.STORE) }
                                )
                            }
                            RegistrationType.RESTAURANT -> {
                                UnifiedRegistrationForm(
                                    role = "RESTAURANT",
                                    themeColors = themeColors,
                                    formsConfig = formsConfig,
                                    isSubmittingExternal = isSubmittingForm,
                                    externalError = formSubmissionError,
                                    onClearExternalError = { formSubmissionError = null },
                                    onRegistrationSuccess = { map -> handleFormSubmit(map, RegistrationType.RESTAURANT) }
                                )
                            }
                            RegistrationType.MEDICAL -> {
                                UnifiedRegistrationForm(
                                    role = "MEDICAL",
                                    themeColors = themeColors,
                                    formsConfig = formsConfig,
                                    isSubmittingExternal = isSubmittingForm,
                                    externalError = formSubmissionError,
                                    onClearExternalError = { formSubmissionError = null },
                                    onRegistrationSuccess = { map -> handleFormSubmit(map, RegistrationType.MEDICAL) }
                                )
                            }
                            RegistrationType.PROPERTY -> {
                                UnifiedRegistrationForm(
                                    role = "PROPERTY",
                                    themeColors = themeColors,
                                    formsConfig = formsConfig,
                                    isSubmittingExternal = isSubmittingForm,
                                    externalError = formSubmissionError,
                                    onClearExternalError = { formSubmissionError = null },
                                    onRegistrationSuccess = { map -> handleFormSubmit(map, RegistrationType.PROPERTY) }
                                )
                            }
                            RegistrationType.JOB -> {
                                UnifiedRegistrationForm(
                                    role = "JOB",
                                    themeColors = themeColors,
                                    formsConfig = formsConfig,
                                    isSubmittingExternal = isSubmittingForm,
                                    externalError = formSubmissionError,
                                    onClearExternalError = { formSubmissionError = null },
                                    onRegistrationSuccess = { map -> handleFormSubmit(map, RegistrationType.JOB) }
                                )
                            }
                            RegistrationType.JOB_SEEKER -> {
                                JobSeekerForm(
                                    viewModel = viewModel,
                                    themeColors = themeColors,
                                    onSuccess = {
                                        viewModel.setTargetRegistrationType(null)
                                        forceShowForm = false
                                        selectedType = null
                                    }
                                )
                            }
                            RegistrationType.CLIENT, null -> {
                                UnifiedRegistrationForm(
                                    role = UserRole.CLIENT.code,
                                    themeColors = themeColors,
                                    formsConfig = formsConfig,
                                    isSubmittingExternal = isSubmittingForm,
                                    externalError = formSubmissionError,
                                    onClearExternalError = { formSubmissionError = null },
                                    onRegistrationSuccess = { map -> handleFormSubmit(map, RegistrationType.CLIENT) }
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    if (showRestoreDialog) {
        RestoreAccountDialog(
            viewModel = viewModel,
            themeColors = themeColors,
            onDismiss = { showRestoreDialog = false }
        )
    }
}

/**
 * Backwards compatibility wrapper expected by main router
 */
@Composable
fun RegisterScreenLayout() {
    // Legacy placeholder wrapper
}
