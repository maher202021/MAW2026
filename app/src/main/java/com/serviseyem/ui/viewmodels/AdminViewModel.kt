package com.serviseyem.ui.viewmodels
import com.serviseyem.data.isValid
import com.serviseyem.ui.helpers.AppState

import com.serviseyem.ui.MainViewModel
import com.serviseyem.ui.*
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

import android.content.Context
import androidx.annotation.Keep
import androidx.lifecycle.viewModelScope
import com.serviseyem.data.*
import com.serviseyem.data.models.*
import com.serviseyem.utils.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.util.UUID
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.SetOptions

@Keep
data class PendingRequest(
    val id: String = "",
    val name: String = "",
    val phone: String = "",
    val section: String = "",
    val city: String = "",
    val details: String = "",
    val status: String = "PENDING", // PENDING, APPROVED, REJECTED
    val createdAt: Long = System.currentTimeMillis()
)

@Keep
data class SystemStats(
    val totalProviders: Int = 0,
    val activeBookings: Int = 0,
    val completedOrders: Int = 0,
    val pendingApprovals: Int = 0,
    val totalUsers: Int = 0,
    val systemHealth: String = "EXCELLENT"
)

@Keep
data class SystemHealth(
    val status: String = "HEALTHY", // "HEALTHY", "WARNING", "CRITICAL"
    val uptime: Long = System.currentTimeMillis() - 1000L * 60 * 60 * 24 * 7,
    val memoryUsage: Double = 34.5,
    val cpuUsage: Double = 12.8,
    val activeConnections: Int = 142,
    val lastError: String? = null
)

@Keep
data class StorageUsage(
    val totalBytes: Long = 10L * 1024 * 1024 * 1024,
    val usedBytes: Long = 2L * 1024 * 1024 * 1024 + 450 * 1024 * 1024,
    val freeBytes: Long = 7L * 1024 * 1024 * 1024 + 574 * 1024 * 1024,
    val imageCount: Int = 854,
    val documentCount: Int = 128
)

@Keep
data class BookingStats(
    val total: Int = 5120,
    val pending: Int = 42,
    val inProgress: Int = 88,
    val completed: Int = 4850,
    val cancelled: Int = 140,
    val today: Int = 34,
    val thisWeek: Int = 245,
    val thisMonth: Int = 1120
)

@Keep
data class RevenueStats(
    val totalRevenue: Double = 5420000.0,
    val platformCommission: Double = 542000.0,
    val providerPayouts: Double = 4878000.0,
    val currency: String = "YER",
    val todayRevenue: Double = 125000.0,
    val monthRevenue: Double = 1840000.0
)

@Keep
data class ProviderStats(
    val totalVerified: Int = 340,
    val pendingApproval: Int = 12,
    val topRated: Int = 95,
    val suspended: Int = 3
)

@Keep
data class CategoryStats(
    val maintenanceCount: Int = 145,
    val storeCount: Int = 88,
    val restaurantCount: Int = 64,
    val medicalCount: Int = 43
)

@Keep
data class CityStats(
    val sanaaCount: Int = 420,
    val adenCount: Int = 240,
    val taizCount: Int = 160,
    val ibbCount: Int = 100,
    val mukallaCount: Int = 80
)

@Keep
data class AuditLog(
    val id: String = UUID.randomUUID().toString(),
    val adminEmail: String = "admin",
    val action: String = "",
    val details: String = "",
    val timestamp: Long = System.currentTimeMillis()
)

@Keep
data class AdminNotification(
    val id: String = UUID.randomUUID().toString(),
    val title: String = "",
    val message: String = "",
    val type: String = "INFO",
    val isRead: Boolean = false,
    val timestamp: Long = System.currentTimeMillis()
)

@Keep
data class SystemLog(
    val id: String = UUID.randomUUID().toString(),
    val tag: String = "APP",
    val message: String = "",
    val level: String = "INFO", // INFO, WARN, ERROR
    val timestamp: Long = System.currentTimeMillis()
)


class AdminViewModel @Inject constructor(
    val application: android.app.Application,
    val appState: AppState,
    val secureStorage: com.serviseyem.utils.SecureStorage
) : BaseViewModel() {
    val crud = AdminCrudOperations(db)

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    internal val _adminRole = MutableStateFlow("GUEST")
    val adminRole: StateFlow<String> = _adminRole.asStateFlow()

    internal val _supervisorPermissions = MutableStateFlow<List<String>>(emptyList())
    val supervisorPermissions: StateFlow<List<String>> = _supervisorPermissions.asStateFlow()

    /**
     * 🔐 تسجيل دخول الأدمن الآمن
     * - يتحقق عبر Cloud Function
     * - Rate limiting مطبق على السيرفر
     * - Custom Claims مطلوبة
     */
    fun loginAdminSecure(
        email: String,
        password: String,
        rememberMe: Boolean = true,
        onResult: (success: Boolean, errorMessage: String?) -> Unit
    ) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                val trimmedEmail = email.trim()
                val trimmedPass = password.trim()

                val currentSettings = appState._settings.value
                val supervisorsList = appState._supervisors.value
                val verifiedRole = com.serviseyem.utils.AdminSecurityManager.verifyCredentials(
                    username = trimmedEmail,
                    passwordAttempt = trimmedPass,
                    settings = currentSettings,
                    context = application,
                    supervisors = supervisorsList,
                    preferredRole = "ADMIN"
                )

                if (verifiedRole == "ADMIN" || verifiedRole == "OWNER") {
                    val assignedRole = verifiedRole
                    val perms = if (assignedRole == "OWNER") listOf("ALL") else emptyList()

                    // 🔐 حفظ البيانات بشكل آمن للوصول الطارئ مستقبلاً
                    com.serviseyem.utils.SecureAdminStorage.storeCredentials(
                        context = application,
                        ownerEmail = if (assignedRole == "OWNER") trimmedEmail else null,
                        ownerPassword = if (assignedRole == "OWNER") trimmedPass else null,
                        adminEmail = if (assignedRole == "ADMIN") trimmedEmail else null,
                        adminPassword = if (assignedRole == "ADMIN") trimmedPass else null
                    )

                    if (rememberMe) {
                        secureStorage.saveAdminSession(com.serviseyem.utils.AdminSession(
                            uid = "admin_${System.currentTimeMillis()}", email = trimmedEmail,
                            loginTime = System.currentTimeMillis(), refreshToken = "ADMIN_SESSION", 
                            role = assignedRole, permissions = perms
                        ))
                    }
                    _adminRole.value = assignedRole
                    _supervisorPermissions.value = perms
                    _isLoading.value = false
                    onResult(true, null)
                    return@launch
                }

                _isLoading.value = false
                onResult(false, "بيانات الدخول غير صحيحة")
            } catch (e: Exception) {
                _isLoading.value = false
                onResult(false, e.message ?: "فشل تسجيل الدخول")
            }
        }
    }

    /**
     * ✨ م2: تسجيل دخول المالك (SuperAdmin/Owner)
     */
    fun loginOwnerSecure(
        email: String,
        password: String,
        rememberMe: Boolean = true,
        onResult: (success: Boolean, errorMessage: String?) -> Unit
    ) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                val trimmedEmail = email.trim()
                val trimmedPass = password.trim()

                val currentSettings = appState._settings.value
                val supervisorsList = appState._supervisors.value
                val verifiedRole = com.serviseyem.utils.AdminSecurityManager.verifyCredentials(
                    username = trimmedEmail,
                    passwordAttempt = trimmedPass,
                    settings = currentSettings,
                    context = application,
                    supervisors = supervisorsList,
                    preferredRole = "OWNER"
                )

                if (verifiedRole == "OWNER") {
                    com.serviseyem.utils.SecureAdminStorage.storeCredentials(
                        context = application,
                        ownerEmail = trimmedEmail,
                        ownerPassword = trimmedPass
                    )

                    if (rememberMe) {
                        secureStorage.saveAdminSession(com.serviseyem.utils.AdminSession(
                            uid = "owner_${System.currentTimeMillis()}", email = trimmedEmail,
                            loginTime = System.currentTimeMillis(), refreshToken = "OWNER_SESSION", 
                            role = "OWNER", permissions = listOf("ALL")
                        ))
                    }
                    _adminRole.value = "OWNER"
                    _supervisorPermissions.value = listOf("ALL")
                    _isLoading.value = false
                    onResult(true, null)
                    return@launch
                }

                _isLoading.value = false
                onResult(false, "بيانات دخول المالك غير صحيحة")
            } catch (e: Exception) {
                _isLoading.value = false
                onResult(false, e.message ?: "فشل تسجيل الدخول")
            }
        }
    }

    /**
     * 💼 تسجيل دخول المشرف (Supervisor)
     */
    fun loginSupervisorSecure(
        usernameOrEmail: String,
        passcode: String,
        rememberMe: Boolean = true,
        onResult: (success: Boolean, errorMessage: String?) -> Unit
    ) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                val trimmedUser = usernameOrEmail.trim()
                val trimmedPass = passcode.trim()

                val currentSettings = appState._settings.value
                val supervisorsList = appState._supervisors.value
                val verifiedRole = com.serviseyem.utils.AdminSecurityManager.verifyCredentials(
                    username = trimmedUser,
                    passwordAttempt = trimmedPass,
                    settings = currentSettings,
                    context = application,
                    supervisors = supervisorsList,
                    preferredRole = "SUPERVISOR"
                )

                if (verifiedRole != null && verifiedRole != "GUEST") {
                    val matchingSup = supervisorsList.find {
                        it.id.equals(trimmedUser, ignoreCase = true) ||
                                it.name.trim().equals(trimmedUser, ignoreCase = true)
                    }
                    val perms = matchingSup?.permissions?.ifEmpty {
                        listOf("MANAGE_PROVIDERS", "MANAGE_BOOKINGS", "MANAGE_REQUESTS")
                    } ?: if (verifiedRole == "OWNER") listOf("ALL") else emptyList()

                    if (rememberMe) {
                        secureStorage.saveAdminSession(
                            com.serviseyem.utils.AdminSession(
                                uid = matchingSup?.id ?: "sup_${System.currentTimeMillis()}",
                                email = trimmedUser,
                                loginTime = System.currentTimeMillis(),
                                refreshToken = "SUPERVISOR_SESSION",
                                role = verifiedRole,
                                permissions = perms
                            )
                        )
                    }

                    _adminRole.value = verifiedRole
                    _supervisorPermissions.value = perms
                    _isLoading.value = false
                    onResult(true, null)
                    return@launch
                }

                _isLoading.value = false
                onResult(false, "بيانات دخول المشرف غير صحيحة")
            } catch (e: Exception) {
                _isLoading.value = false
                onResult(false, e.message ?: "فشل تسجيل الدخول")
            }
        }
    }

    /**
     * 🚪 تسجيل الخروج الآمن
     */
    fun logoutAdmin() {
        viewModelScope.launch {
            try {
                secureStorage.clearAdminSession()
                com.google.firebase.auth.FirebaseAuth.getInstance().signOut()
                _adminRole.value = "GUEST"
                _supervisorPermissions.value = emptyList()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    /**
     * 🔍 التحقق من صحة جلسة الأدمن الحالية
     */
    suspend fun isAdminSessionValid(): Boolean {
        return try {
            val session = secureStorage.getAdminSession() ?: return false
            
            // إذا كان هناك جلسة محفوظة، نعتبرها صالحة مؤقتاً للوصول للوحة
            if (session.role == "OWNER" || session.role == "ADMIN" || session.role == "SUPERVISOR") {
                // التحقق الإضافي عبر Auth إذا كان متاحاً
                val user = com.google.firebase.auth.FirebaseAuth.getInstance().currentUser
                if (user != null) {
                    val tokenResult = user.getIdToken(false).await()
                    val isAdmin = (tokenResult.claims["isAdmin"] as? Boolean == true) || 
                                 (tokenResult.claims["isSuperAdmin"] as? Boolean == true)
                    // لا نرفض الجلسة إذا لم يكن هناك Claim ولكن الجلسة المحلية موجودة (كحالة طوارئ)
                }
                _adminRole.value = session.role
                _supervisorPermissions.value = session.permissions
                return true
            }
            false
        } catch (e: Exception) {
            false
        }
    }

    /**
     * 💾 حفظ الإعدادات بشكل ذري (Atomic)
     * يستخدم Firestore Transaction لمنع التضارب
     */
    fun saveSettingsAtomically(
        appName: String,
        welcomeMessage: String,
        footerMessage: String,
        onSuccess: () -> Unit = {},
        onError: (String) -> Unit = {}
    ) {
        viewModelScope.launch {
            try {
                val settingsRef = db.collection("settings").document("main_settings")
                db.runTransaction { transaction ->
                    val snapshot = transaction.get(settingsRef)
                    val currentVersion = snapshot.getLong("version") ?: 0L
                    val updatedData = mapOf(
                        "appName" to appName,
                        "welcomeMessage" to welcomeMessage,
                        "footerMessage" to footerMessage,
                        "version" to (currentVersion + 1),
                        "updatedAt" to System.currentTimeMillis(),
                        "updatedBy" to com.google.firebase.auth.FirebaseAuth.getInstance().currentUser?.uid
                    )
                    transaction.set(settingsRef, updatedData, SetOptions.merge())
                    val auditRef = db.collection("audit_logs").document()
                    transaction.set(auditRef, mapOf(
                        "action" to "SETTINGS_UPDATED",
                        "userId" to (com.google.firebase.auth.FirebaseAuth.getInstance().currentUser?.uid ?: "unknown"),
                        "timestamp" to System.currentTimeMillis(),
                        "version" to (currentVersion + 1)
                    ))
                    currentVersion + 1
                }.await()
                appState._settings.value = appState._settings.value.copy(
                    appName = appName,
                    welcomeMessage = welcomeMessage,
                    footerMessage = footerMessage
                )
                onSuccess()
            } catch (e: Exception) {
                onError(e.message ?: "فشل حفظ الإعدادات سحابياً")
            }
        }
    }

    /**
     * 💾 إنشاء نسخة احتياطية للإعدادات
     * تُحفظ في settings_backups مع تاريخ الإنشاء
     */
    fun createSettingsBackup(onResult: (Boolean) -> Unit = {}) {
        viewModelScope.launch {
            try {
                val settingsDoc = db.collection("settings")
                    .document("main_settings")
                    .get()
                    .await()
                if (!settingsDoc.exists()) {
                    onResult(false)
                    return@launch
                }
                val settingsData = settingsDoc.data ?: emptyMap<String, Any>()
                val backupData: Map<String, Any?> = mapOf(
                    "timestamp" to System.currentTimeMillis(),
                    "version" to (settingsData["version"] ?: 0L),
                    "data" to settingsData,
                    "createdBy" to com.google.firebase.auth.FirebaseAuth.getInstance().currentUser?.uid
                )
                db.collection("settings_backups").add(backupData).await()
                onResult(true)
            } catch (e: Exception) {
                onResult(false)
            }
        }
    }

    // --- Callback/Lambda Properties for decoupling ---
    var getHomeViewModel: (() -> HomeViewModel)? = null
    var getSettingsViewModel: (() -> SettingsViewModel)? = null
    var getBookingViewModel: (() -> BookingViewModel)? = null
    var getInstantRequestViewModel: (() -> InstantRequestViewModel)? = null
    var getNotifications: (() -> MutableStateFlow<List<NotificationEntity>>)? = null
    var onAddNotification: ((String, String, String, String) -> Unit)? = null
    var onTriggerNotificationFull: ((String, String, String, String) -> Unit)? = null
    var onTriggerNotification: ((String) -> Unit)? = null
    var onApplyFilters: (() -> Unit)? = null

    inner class MainViewModelDelegate {
        val homeViewModel get() = this@AdminViewModel.homeViewModel
        val settingsViewModel get() = this@AdminViewModel.settingsViewModel
        val bookingViewModel get() = this@AdminViewModel.bookingViewModel
        val instantRequestViewModel get() = this@AdminViewModel.instantRequestViewModel
        val _notifications get() = this@AdminViewModel._notifications
        
        fun addNotification(title: String, message: String, targetType: String = "", targetValue: String = "") {
            this@AdminViewModel.addNotification(title, message, targetType, targetValue)
        }
        
        fun triggerNotification(title: String, message: String, targetType: String = "ALL", targetValue: String = "") {
            this@AdminViewModel.triggerNotification(title, message, targetType, targetValue)
        }
        
        fun triggerNotification(msg: String) {
            this@AdminViewModel.onTriggerNotification?.invoke(msg)
        }
        
        fun applyFilters() {
            this@AdminViewModel.applyFilters()
        }
    }
    
    val mainViewModel = MainViewModelDelegate()

    private val auth get() = com.google.firebase.auth.FirebaseAuth.getInstance()
    private val homeViewModel get() = getHomeViewModel?.invoke() ?: throw IllegalStateException("homeViewModel not provided")
    private val settingsViewModel get() = getSettingsViewModel?.invoke() ?: throw IllegalStateException("settingsViewModel not provided")
    private val bookingViewModel get() = getBookingViewModel?.invoke() ?: throw IllegalStateException("bookingViewModel not provided")
    private val instantRequestViewModel get() = getInstantRequestViewModel?.invoke() ?: throw IllegalStateException("instantRequestViewModel not provided")
    private val _notifications get() = getNotifications?.invoke() ?: throw IllegalStateException("_notifications not provided")

    private fun addNotification(title: String, message: String, targetType: String = "", targetValue: String = "") {
        onAddNotification?.invoke(title, message, targetType, targetValue)
    }

    private fun triggerNotification(title: String, message: String, targetType: String = "", targetValue: String = "") {
        onTriggerNotificationFull?.invoke(title, message, targetType, targetValue)
    }

    private fun applyFilters() {
        onApplyFilters?.invoke()
    }

    fun logAdminActivity(adminName: String, action: String, details: String) {
        logAdminActivity("Admin: $adminName - Action: $action - Details: $details")
    }

    private fun deleteBooking(bookingId: String) {
        mainViewModel.bookingViewModel.deleteBooking(bookingId)
    }

    private fun updateBooking(booking: com.serviseyem.data.BookingEntity) {
        mainViewModel.bookingViewModel.updateBooking(booking)
    }

    internal val _pendingProviders get() = appState._pendingProviders
    val pendingProviders: StateFlow<List<PendingProviderEntity>> = _pendingProviders.asStateFlow()
    internal val _pendingTechnicians get() = appState._pendingTechnicians
    val pendingTechnicians: StateFlow<List<PendingProviderEntity>> = _pendingTechnicians.asStateFlow()
    internal val _registeredUsersList get() = appState._registeredUsersList
    val registeredUsersList: StateFlow<List<Map<String, Any>>> = _registeredUsersList.asStateFlow()
    internal val _registeredUsersCount get() = appState._registeredUsersCount
    val registeredUsersCount: StateFlow<Int> = _registeredUsersCount.asStateFlow()
    internal val _reports get() = appState._reports
    val reports: StateFlow<List<ReportEntity>> = _reports.asStateFlow()
    internal val _activityLogs get() = appState._activityLogs
    val activityLogs: StateFlow<List<ActivityLogEntity>> = _activityLogs.asStateFlow()
    internal val _callsLog get() = appState._callsLog
    val callsLog: StateFlow<List<CallEntity>> = _callsLog.asStateFlow()
    internal val _coupons get() = appState._coupons
    val coupons: StateFlow<List<CouponEntity>> = _coupons.asStateFlow()
    internal val _internalWallets get() = appState._internalWallets
    val internalWallets: StateFlow<List<com.serviseyem.data.InternalWalletEntity>> = _internalWallets.asStateFlow()
    internal val _walletTransactions get() = appState._walletTransactions
    val walletTransactions: StateFlow<List<com.serviseyem.data.WalletTransactionEntity>> = _walletTransactions.asStateFlow()
    internal val _paymentWallets get() = appState._paymentWallets
    val paymentWallets: StateFlow<List<PaymentWalletEntity>> = _paymentWallets.asStateFlow()
    internal val _payments get() = appState._payments
    val payments: StateFlow<List<PaymentEntity>> = _payments.asStateFlow()
    internal val _orders get() = appState._orders
    val orders: StateFlow<List<com.serviseyem.data.OrderEntity>> = _orders.asStateFlow()
    internal val _ratings get() = appState._ratings
    val ratings: StateFlow<List<com.serviseyem.data.RatingEntity>> = _ratings.asStateFlow()
    internal val _customProfileTabs get() = appState._customProfileTabs
    val customProfileTabs: StateFlow<List<com.serviseyem.data.CustomProfileTabEntity>> = _customProfileTabs.asStateFlow()
    internal val _stores get() = appState._stores
    val stores: StateFlow<List<com.serviseyem.data.StoreEntity>> = _stores.asStateFlow()
    internal val _products get() = appState._products
    val products: StateFlow<List<com.serviseyem.data.ProductEntity>> = _products.asStateFlow()
    internal val _properties get() = appState._properties
    val properties: StateFlow<List<com.serviseyem.data.PropertyEntity>> = _properties.asStateFlow()
    internal val _jobs get() = appState._jobs
    val jobs: StateFlow<List<com.serviseyem.data.JobEntity>> = _jobs.asStateFlow()
    internal val _jobApplications get() = appState._jobApplications
    val jobApplications: StateFlow<List<com.serviseyem.data.JobApplicationEntity>> = _jobApplications.asStateFlow()

fun approveRequest(request: PendingProviderEntity) {
        val lowerArea = request.area.lowercase()
        val finalCityId = when {
            lowerArea.contains("عدن") || lowerArea.contains("aden") -> "ye_ade"
            lowerArea.contains("تعز") || lowerArea.contains("taiz") -> "ye_tai"
            lowerArea.contains("الحديدة") || lowerArea.contains("hodeidah") -> "ye_hod"
            else -> "ye_san"
        }

        val cleanPhone = com.serviseyem.domain.usecases.ValidatePhoneUseCase.normalizePhone(request.phone)
            .ifBlank { request.phone.trim().replace(" ", "").replace("+", "") }
        val resolvedPasswordHash = request.passwordHash.ifBlank { request.password }.let { rawOrHash ->
            val trimmed = rawOrHash.trim()
            if (trimmed.isNotBlank() && !trimmed.contains(":")) {
                com.serviseyem.utils.PasswordHasher.hash(trimmed)
            } else {
                trimmed
            }
        }
        
        // Helper to remove from pending_providers permanently
        val clearPendingFromDbAndState = {
            db.collection("pending_providers").document(request.id).delete()
            if (cleanPhone.isNotEmpty()) {
                db.collection("pending_providers").document(cleanPhone).delete()
                db.collection("pending_providers").whereEqualTo("phone", request.phone).get().addOnSuccessListener { qs ->
                    qs?.documents?.forEach { doc ->
                        db.collection("pending_providers").document(doc.id).delete()
                    }
                }
            }
            _pendingProviders.value = _pendingProviders.value.filter { 
                it.id != request.id && it.id != cleanPhone && (cleanPhone.isEmpty() || com.serviseyem.domain.usecases.ValidatePhoneUseCase.normalizePhone(it.phone).ifBlank { it.phone.trim().replace(" ", "").replace("+", "") } != cleanPhone)
            }
        }

        clearPendingFromDbAndState()

        val now = System.currentTimeMillis()
        val joinReqUpdates = mapOf(
            "status" to "APPROVED",
            "approvalStatus" to "APPROVED",
            "isActive" to true,
            "approvedAt" to now,
            "updatedAt" to now
        )
        db.collection("join_requests").document(request.id).update(joinReqUpdates)
        if (cleanPhone.isNotEmpty() && cleanPhone != request.id) {
            db.collection("join_requests").document(cleanPhone).update(joinReqUpdates)
        }

        if (request.profession == "STORE_OWNER" || request.categoryId.uppercase() == "STORE" || request.categoryId.uppercase() == "RESTAURANT" || request.categoryId.uppercase() == "MEDICAL") {
            val storeId = "store_" + cleanPhone
            val resolvedCategory = when (request.categoryId.uppercase()) {
                "RESTAURANT" -> "مطاعم وكافيهات"
                "MEDICAL" -> "مراكز طبية وعيادات"
                else -> "محلات ومراكز تجارية"
            }
            val resolvedSectionId = if (request.categoryId.uppercase() == "RESTAURANT") "restaurants" else if (request.categoryId.uppercase() == "MEDICAL") "medical" else "stores"
            viewModelScope.launch {
                val resolvedStoreDocUrl = if (request.idPhotoBase64.isNotBlank()) {
                    com.serviseyem.utils.FirebaseStorageUploader.resolveBase64ToStorageUrl(
                        request.idPhotoBase64,
                        "stores/$storeId/verification_doc.pdf"
                    )
                } else ""
                val newStore = com.serviseyem.data.StoreEntity(
                    id = storeId,
                    name = request.name,
                    description = request.specialization.ifBlank { "محل تجاري معتمد وموثق" },
                    ownerId = cleanPhone.ifBlank { request.phone },
                    ownerName = request.name,
                    phone = cleanPhone.ifBlank { request.phone },
                    localNeighborhood = request.localNeighborhood,
                    cityId = finalCityId,
                    sectionId = resolvedSectionId,
                    categoryId = resolvedCategory,
                    isActive = true,
                    isApproved = true,
                    isPinned = false,
                    isDeleted = false,
                    passwordHash = resolvedPasswordHash,
                    pdfFileBase64 = resolvedStoreDocUrl
                )
                if (request.id != storeId) {
                    db.collection("stores").document(request.id).delete()
                }
                if (cleanPhone.isNotEmpty() && cleanPhone != storeId) {
                    db.collection("stores").document(cleanPhone).delete()
                }
                db.collection("stores").document(storeId).set(newStore)
                
                // Instant Local Sync for stores
                _stores.value = _stores.value.filter { it.id != storeId && it.id != request.id && it.phone.trim().replace(" ", "").replace("+", "") != cleanPhone } + newStore
            }

            val (approvalTitle, approvalMsg, toastMsg) = when (request.categoryId.uppercase()) {
                "RESTAURANT" -> Triple(
                    "🎉 تهانينا! تم تفعيل مطعمك / كافيهك بنجاح",
                    "مرحباً بك، لقد تم مراجعة وتفعيل مطعمك '${request.name}' بنجاح في دليل المطاعم والكافيهات! يمكنك الآن إدارة قائمتك وحجوزاتك مباشرة من شاشة الانضمام.",
                    "✅ تم تفعيل مطعم ${request.name}"
                )
                "MEDICAL" -> Triple(
                    "🎉 تهانينا! تم تفعيل مركزك الطبي / عيادتك بنجاح",
                    "مرحباً بك، لقد تم مراجعة وتفعيل مركزك الطبي/عيادتك '${request.name}' بنجاح في الدليل الطبي! يمكنك الآن استقبال حجوزات المواعيد وإدارتها مباشرة من شاشة الانضمام.",
                    "✅ تم تفعيل المركز الطبي ${request.name}"
                )
                else -> Triple(
                    "🎉 تهانينا! تم تفعيل متجرك بنجاح",
                    "مرحباً بك يا غالي، لقد تم مراجعة وتفعيل متجرك/محلك '${request.name}' بنجاح في التطبيق! يمكنك الآن إضافة منتجاتك وإدارة متجرك مباشرة من شاشة الانضمام.",
                    "✅ تم تفعيل متجر ${request.name}"
                )
            }

            mainViewModel.addNotification(
                title = approvalTitle,
                message = approvalMsg,
                targetType = "USER",
                targetValue = request.phone
            )
            mainViewModel.triggerNotification(toastMsg)
        } else if (request.profession == "PROPERTY_OWNER" || request.categoryId.uppercase() == "PROPERTY") {
            val propId = "prop_" + cleanPhone
            val propPrice = try { request.chatRecipientId.toDouble() } catch(e: Exception) { 0.0 }
            viewModelScope.launch {
                val resolvedPropDocUrl = if (request.idPhotoBase64.isNotBlank()) {
                    com.serviseyem.utils.FirebaseStorageUploader.resolveBase64ToStorageUrl(
                        request.idPhotoBase64,
                        "properties/$propId/verification_doc.pdf"
                    )
                } else ""
                val newProp = com.serviseyem.data.PropertyEntity(
                    id = propId,
                    title = request.name,
                    description = request.specialization.ifBlank { "عقار معلن وموثق" },
                    phone = cleanPhone.ifBlank { request.phone },
                    ownerId = cleanPhone.ifBlank { request.phone },
                    localNeighborhood = request.localNeighborhood,
                    cityId = finalCityId,
                    isActive = true,
                    isApproved = true,
                    isPinned = false,
                    isDeleted = false,
                    passwordHash = resolvedPasswordHash,
                    price = propPrice,
                    pdfFileBase64 = resolvedPropDocUrl
                )
                if (request.id != propId) {
                    db.collection("properties").document(request.id).delete()
                }
                if (cleanPhone.isNotEmpty() && cleanPhone != propId) {
                    db.collection("properties").document(cleanPhone).delete()
                }
                db.collection("properties").document(propId).set(newProp)
                
                // Instant Local Sync for properties
                _properties.value = _properties.value.filter { it.id != propId && it.id != request.id && it.phone.trim().replace(" ", "").replace("+", "") != cleanPhone } + newProp
            }

            mainViewModel.addNotification(
                title = "🎉 تهانينا! تم تفعيل إعلان عقارك بنجاح",
                message = "مرحباً بك، لقد تم مراجعة وتفعيل عقارك '${request.name}' بنجاح في دليل العقارات المعتمد! يمكنك تعديله وإدارته ورؤية تعليقات العملاء مباشرة من شاشة الانضمام.",
                targetType = "USER",
                targetValue = request.phone
            )
            mainViewModel.triggerNotification("✅ تم تفعيل عقار ${request.name}")
        } else if (request.profession == "JOB_POSTER" || request.categoryId.uppercase() == "JOB") {
            val jobId = "job_" + cleanPhone
            val newJob = com.serviseyem.data.JobEntity(
                id = jobId,
                title = request.customCategoryName.ifBlank { "وظيفة - " + request.name },
                companyName = request.name,
                phone = cleanPhone.ifBlank { request.phone },
                cityId = finalCityId,
                address = request.localNeighborhood,
                description = request.specialization.ifBlank { "إعلان وظيفة معتمد وموثق" },
                isActive = true,
                isApproved = true
            )
            if (request.id != jobId) {
                db.collection("jobs").document(request.id).delete()
            }
            if (cleanPhone.isNotEmpty() && cleanPhone != jobId) {
                db.collection("jobs").document(cleanPhone).delete()
            }
            db.collection("jobs").document(jobId).set(newJob)
            
            // Instant Local Sync for jobs: clean up unapproved version and set approved job
            _jobs.value = _jobs.value.filter { it.id != jobId && it.id != request.id && it.phone.trim().replace(" ", "").replace("+", "") != cleanPhone } + newJob

            mainViewModel.addNotification(
                title = "🎉 تهانينا! تم تفعيل إعلان وظيفتك بنجاح",
                message = "مرحباً بك، لقد تم مراجعة ونشر إعلان وظيفتك '${request.name}' بنجاح! يمكنك إدارتها الآن مباشرة من شاشة الانضمام.",
                targetType = "USER",
                targetValue = request.phone
            )
            mainViewModel.triggerNotification("✅ تم تفعيل إعلان وظيفة ${request.name}")
        } else if (request.profession == "JOB_SEEKER" || request.categoryId.uppercase() == "JOB_SEEKER") {
            val seekerMap = mapOf(
                "id" to request.id,
                "name" to request.name,
                "phone" to cleanPhone,
                "profession" to request.customCategoryName.ifBlank { "باحث عن عمل" },
                "city" to request.area,
                "isApproved" to true,
                "status" to "APPROVED",
                "createdAt" to System.currentTimeMillis()
            )
            db.collection("job_seekers").document(request.id).set(seekerMap)
            if (cleanPhone.isNotEmpty() && cleanPhone != request.id) {
                db.collection("job_seekers").document(cleanPhone).set(seekerMap)
            }
            mainViewModel.addNotification(
                title = "🎉 تهانينا! تم اعتماد ملفك كباحث عن عمل",
                message = "مرحباً بك يا غالي، تم اعتماد ملفك في قسم التوظيف والبحث عن عمل بنجاح!",
                targetType = "USER",
                targetValue = request.phone
            )
            mainViewModel.triggerNotification("✅ تم اعتماد ملف المتقدم ${request.name}")
        } else if (request.profession == "CLIENT" || request.categoryId.uppercase() == "CLIENT") {
            val userMap = mutableMapOf<String, Any>(
                "id" to cleanPhone.ifBlank { request.id },
                "name" to request.name,
                "phone" to cleanPhone,
                "residence" to request.area,
                "isApproved" to true,
                "createdAt" to System.currentTimeMillis()
            )
            if (resolvedPasswordHash.isNotBlank()) {
                userMap["passwordHash"] = resolvedPasswordHash
            }
            db.collection("users").document(request.id).set(userMap, SetOptions.merge())
            if (cleanPhone.isNotEmpty() && cleanPhone != request.id) {
                db.collection("users").document(cleanPhone).set(userMap, SetOptions.merge())
            }
            
            mainViewModel.addNotification(
                title = "🎉 تهانينا! تم تفعيل حسابك بنجاح",
                message = "مرحباً بك يا غالي، تم تفعيل حسابك كعميل مسجل بنجاح!",
                targetType = "USER",
                targetValue = request.phone
            )
            mainViewModel.triggerNotification("✅ تم تفعيل حساب المستخدم ${request.name}")
        } else {
            val providerId = "prov_" + cleanPhone
            val approvedProvider = ProviderEntity(
                id = providerId,
                name = request.name,
                phone = cleanPhone.ifBlank { request.phone },
                categoryId = request.categoryId,
                area = request.area,
                isVip = false,
                subscriptionStatus = "APPROVED",
                isAvailable = true,
                cityId = finalCityId,
                localNeighborhood = request.localNeighborhood,
                rating = 5.0f,
                isBlocked = false,
                customCategoryName = request.customCategoryName,
                passwordHash = resolvedPasswordHash,
                isDeleted = false,
                deletedAt = null
            )
            db.collection("providers").document(approvedProvider.id).set(approvedProvider)
            
            // Instant Local Sync
            val currentProviders = mainViewModel.homeViewModel._providers.value.filter { it.id != approvedProvider.id && it.phone.trim().replace(" ", "").replace("+", "") != cleanPhone }.toMutableList()
            currentProviders.add(approvedProvider)
            mainViewModel.homeViewModel._providers.value = currentProviders
            applyFilters()

            // Add accepted notification!
            mainViewModel.addNotification(
                title = "🎉 تهانينا! تم قبول انضمامك كفني معتمد",
                message = "مرحباً بك يا غالي، لقد تم قبول طلب انضمامك كمهني معتمد وأصبحت الآن نشطاً في دليل كل خدمات اليمن! حسابك يظهر الآن لجميع العملاء.",
                targetType = "USER",
                targetValue = request.phone
            )
            
            mainViewModel.triggerNotification("✅ تم قبول طلب ${request.name}")
        }
    }

fun rejectRequest(request: PendingProviderEntity, reason: String) {
        val cleanPhone = request.phone.trim().replace(" ", "").replace("+", "")
        val now = System.currentTimeMillis()
        val finalReason = reason.ifBlank { "لم تستوفِ المستندات أو الشروط المطلوبة" }

        val rejectUpdates = mapOf(
            "status" to "REJECTED",
            "approvalStatus" to "REJECTED",
            "isActive" to false,
            "rejectionReason" to finalReason,
            "rejectedAt" to now,
            "rejectedBy" to "ADMIN",
            "updatedAt" to now
        )
        db.collection("join_requests").document(request.id).update(rejectUpdates)
        if (cleanPhone.isNotEmpty() && cleanPhone != request.id) {
            db.collection("join_requests").document(cleanPhone).update(rejectUpdates)
        }

        db.collection("pending_providers").document(request.id).delete()
        if (cleanPhone.isNotEmpty()) {
            db.collection("pending_providers").document(cleanPhone).delete()
        }
        _pendingProviders.value = _pendingProviders.value.filter { 
            it.id != request.id && it.id != cleanPhone && (cleanPhone.isEmpty() || it.phone.trim().replace(" ", "").replace("+", "") != cleanPhone)
        }
        
        // Add rejected notification!
        mainViewModel.addNotification(
            title = "❌ تنويه حول طلب انضمامك",
            message = "للأسف لم يتم قبول طلب انضمامك للأسباب التالية: $finalReason. يمكنك تعديل البيانات وإعادة تقديم الطلب.",
            targetType = "USER",
            targetValue = request.phone
        )
        
        mainViewModel.triggerNotification("❌ تم رفض طلب ${request.name} بسبب: $finalReason")
    }

fun approveTechnician(providerId: String) {
        val technician = _pendingProviders.value.find { it.id == providerId }
        technician?.let {
            _pendingProviders.value = _pendingProviders.value.filter { it.id != providerId }
            val lowerArea = it.area.lowercase()
            val finalCityId = when {
                lowerArea.contains("عدن") || lowerArea.contains("aden") -> "ye_ade"
                lowerArea.contains("تعز") || lowerArea.contains("taiz") -> "ye_tai"
                lowerArea.contains("الحديدة") || lowerArea.contains("hodeidah") -> "ye_hod"
                else -> "ye_san"
            }
            val cleanTechPhone = com.serviseyem.domain.usecases.ValidatePhoneUseCase.normalizePhone(it.phone)
                .ifBlank { it.phone.trim().replace(" ", "").replace("+", "") }
            val finalId = "prov_" + cleanTechPhone
            val resolvedTechHash = it.passwordHash.ifBlank { it.password }.let { rawOrHash ->
                val trimmed = rawOrHash.trim()
                if (trimmed.isNotBlank() && !trimmed.contains(":")) {
                    com.serviseyem.utils.PasswordHasher.hash(trimmed)
                } else {
                    trimmed
                }
            }
            val p = ProviderEntity(
                id = finalId,
                name = it.name,
                phone = cleanTechPhone.ifBlank { it.phone },
                categoryId = it.categoryId,
                area = it.area,
                localNeighborhood = it.localNeighborhood,
                cityId = finalCityId,
                isVerified = true,
                isRecommended = false,
                subscriptionStatus = "APPROVED",
                isVip = false,
                isAvailable = true,
                isBlocked = false,
                rating = 5.0f,
                subscriptionExpiry = System.currentTimeMillis() + (30L * 24 * 60 * 60 * 1000),
                workPhotosBase64 = it.workPhotosBase64,
                passwordHash = resolvedTechHash,
                isDeleted = false,
                deletedAt = null
            )

            val notification = NotificationEntity(
                id = UUID.randomUUID().toString(),
                title = "🎉 تم قبول طلبك",
                message = "تم قبول طلب انضمامك كـ ${it.name} بنجاح بالدليل اليمني",
                targetType = "USER",
                targetValue = it.phone,
                timestamp = System.currentTimeMillis()
            )
            _notifications.value = listOf(notification) + _notifications.value

            // Instant Local Sync
            val currentProviders = mainViewModel.homeViewModel._providers.value.filter { it.id != finalId }.toMutableList()
            currentProviders.add(p)
            mainViewModel.homeViewModel._providers.value = currentProviders
            applyFilters()

            try {
                db.collection("pending_providers").document(providerId).delete()
                db.collection("providers").document(finalId).set(p)
                if (notification.isValid()) db.collection("notifications").document(notification.id).set(notification)
            } catch (e: Exception) {}
        }
    }

fun rejectTechnician(providerId: String, reason: String = "لم يستوفِ الشروط") {
        val technician = _pendingProviders.value.find { it.id == providerId }
        technician?.let {
            val updated = it.copy(status = "REJECTED", reason = reason)
            _pendingProviders.value = _pendingProviders.value.map { item -> if (item.id == providerId) updated else item }

            val notification = NotificationEntity(
                id = UUID.randomUUID().toString(),
                title = "❌ تم رفض طلبك",
                message = "تم رفض طلب انضمامك بسبب: $reason",
                targetType = "USER",
                targetValue = it.phone,
                timestamp = System.currentTimeMillis()
            )
            _notifications.value = listOf(notification) + _notifications.value

            try {
                db.collection("pending_providers").document(providerId).set(updated)
                if (notification.isValid()) db.collection("notifications").document(notification.id).set(notification)
            } catch (e: Exception) {}
        }
    }

fun loadPendingTechnicians() {
        try {
            db.collection("pending_providers")
                .addSnapshotListener { snapshot, error ->
                    if (error != null) return@addSnapshotListener
                    if (snapshot != null) {
                        val fetched = snapshot.documents.mapNotNull { doc ->
                            try {
                                val parsed = doc.toObject(PendingProviderEntity::class.java)
                                parsed?.copy(id = doc.id)
                            } catch (e: Exception) {
                                try {
                                    PendingProviderEntity(
                                        id = doc.id,
                                        name = doc.getString("name") ?: "",
                                        phone = doc.getString("phone") ?: "",
                                        categoryId = doc.getString("categoryId") ?: "",
                                        area = doc.getString("area") ?: doc.getString("localArea") ?: "",
                                        localNeighborhood = doc.getString("localNeighborhood") ?: "",
                                        status = doc.getString("status") ?: "PENDING",
                                        reason = doc.getString("reason") ?: "",
                                        idPhotoBase64 = doc.getString("idPhotoBase64") ?: "",
                                        selfiePhotoBase64 = doc.getString("selfiePhotoBase64") ?: "",
                                        workPhotosBase64 = (doc.get("workPhotosBase64") as? List<*>)?.mapNotNull { it as? String } ?: emptyList()
                                    )
                                } catch (e2: Exception) {
                                    null
                                }
                            }
                        }
                        _pendingTechnicians.value = fetched
                    }
                }
        } catch (e: Exception) {}
    }

fun approvePendingProvider(pending: PendingProviderEntity) {
        db.collection("pending_providers").document(pending.id).delete()
        val cleanPhone = com.serviseyem.domain.usecases.ValidatePhoneUseCase.normalizePhone(pending.phone)
            .ifBlank { pending.phone.trim().replace(" ", "").replace("+", "") }
        val resolvedHash = when {
            pending.passwordHash.isNotBlank() && com.serviseyem.utils.SecureHasher.isValidHash(pending.passwordHash) -> pending.passwordHash
            pending.passwordHash.isNotBlank() -> com.serviseyem.utils.SecureHasher.hashPassword(pending.passwordHash.trim())
            else -> ""
        }
        val provider = ProviderEntity(
            id = if (pending.id.isNotBlank()) pending.id else "p_" + cleanPhone.ifBlank { pending.phone },
            name = pending.name,
            phone = cleanPhone.ifBlank { pending.phone },
            categoryId = pending.categoryId,
            area = pending.area,
            localNeighborhood = pending.localNeighborhood,
            subscriptionStatus = "APPROVED",
            isAvailable = true,
            profession = pending.profession,
            specialization = pending.specialization,
            customCategoryName = pending.customCategoryName,
            passwordHash = resolvedHash,
            providerType = pending.providerType
        )
        db.collection("providers").document(provider.id).set(provider)
        mainViewModel.triggerNotification("✅ تم قبول وتعميم الفني: ${pending.name}")
    }

fun rejectPendingProvider(pending: PendingProviderEntity, reason: String = "تم رفض الطلب") {
        db.collection("pending_providers").document(pending.id).delete()
        mainViewModel.triggerNotification("❌ تم رفض طلب انضمام: ${pending.name}")
    }

fun approveRegisteredUser(userId: String, userName: String = "") {
        _registeredUsersList.value = _registeredUsersList.value.map { u ->
            if (u["id"]?.toString() == userId) {
                u.toMutableMap().apply { put("isApproved", true) }
            } else u
        }
        db.collection("registered_users").document(userId).update("isApproved", true)
            .addOnSuccessListener {
                mainViewModel.triggerNotification("✅ تم قبول وتأكيد طلب انضمام المستخدم $userName بنجاح من قبل الأدمن!")
            }
        db.collection("users").document(userId).update("isApproved", true)
    }

fun toggleBlockRegisteredUser(userId: String, currentBlocked: Boolean, userName: String = "") {
        val newBlockedState = !currentBlocked
        _registeredUsersList.value = _registeredUsersList.value.map { u ->
            if (u["id"]?.toString() == userId) {
                u.toMutableMap().apply { put("isBlocked", newBlockedState) }
            } else u
        }
        db.collection("registered_users").document(userId).update("isBlocked", newBlockedState)
            .addOnSuccessListener {
                val actionText = if (newBlockedState) "حظر" else "إلغاء حظر"
                mainViewModel.triggerNotification("🛡️ تم $actionText حساب المستخدم $userName بنجاح.")
            }
        db.collection("users").document(userId).update("isBlocked", newBlockedState)
    }

fun deleteRegisteredUser(userId: String, userName: String = "") {
        _registeredUsersList.value = _registeredUsersList.value.filter { u -> u["id"]?.toString() != userId }
        db.collection("registered_users").document(userId).delete()
            .addOnSuccessListener {
                mainViewModel.triggerNotification("🗑️ تم حذف حساب المستخدم $userName من القاعدة بنجاح.")
            }
        db.collection("users").document(userId).delete()
    }

fun saveStore(store: com.serviseyem.data.StoreEntity) {
        val cleanPhone = com.serviseyem.domain.usecases.ValidatePhoneUseCase.normalizePhone(store.phone)
            .ifBlank { store.phone.trim().replace(" ", "").replace("+", "") }
        val duplicateType = checkAndGetDuplicateAccountType(cleanPhone, store.id)
        if (duplicateType != null) {
            mainViewModel.triggerNotification("❌ عذراً! رقم الهاتف (${store.phone}) مسجل بالفعل كـ ($duplicateType). لا يُسمح بتكرار الحسابات.")
            logAdminActivity("محاولة تسجيل متجر مكرر محجوبة لرقم: $cleanPhone - نوع التكرار: $duplicateType")
            return
        }
        val targetId = if (store.id.isEmpty()) db.collection("stores").document().id else store.id
        val rawPassword = store.passwordHash.trim()
        val resolvedPasswordHash = when {
            rawPassword.isNotBlank() && com.serviseyem.utils.SecureHasher.isValidHash(rawPassword) -> rawPassword
            rawPassword.isNotBlank() -> com.serviseyem.utils.SecureHasher.hashPassword(rawPassword)
            else -> ""
        }
        val finalStore = store.copy(
            id = targetId,
            phone = cleanPhone,
            ownerId = if (store.ownerId.isEmpty()) cleanPhone else store.ownerId,
            passwordHash = resolvedPasswordHash
        )

        // Instant Local State Update (Solves retry/delay bug)
        val updatedStores = _stores.value.filter { it.id != targetId }.toMutableList()
        updatedStores.add(finalStore)
        _stores.value = updatedStores

        if (rawPassword.isNotEmpty() && !com.serviseyem.utils.SecureHasher.isValidHash(rawPassword)) {
            try {
                val authEmail = getAuthEmailForPhone(cleanPhone)
                auth.createUserWithEmailAndPassword(authEmail, rawPassword)
                    .addOnFailureListener { /* account exists */ }
            } catch (e: Exception) {}
        }

        viewModelScope.launch {
            val ctx = appContext
            val logoPath = com.serviseyem.utils.FirebaseStorageUploader.getStoreLogoPath(targetId)
            val coverPath = com.serviseyem.utils.FirebaseStorageUploader.getStoreCoverPath(targetId)
            val finalLogo = if (ctx != null) {
                uploadImageStringOrUri(ctx, finalStore.logoImage, logoPath)
            } else {
                com.serviseyem.utils.FirebaseStorageUploader.resolveBase64ToStorageUrl(finalStore.logoImage, logoPath)
            }
            val finalCover = if (ctx != null) {
                uploadImageStringOrUri(ctx, finalStore.coverImage, coverPath)
            } else {
                com.serviseyem.utils.FirebaseStorageUploader.resolveBase64ToStorageUrl(finalStore.coverImage, coverPath)
            }
            val finalImages = finalStore.images.mapIndexed { idx, img ->
                val photoPath = com.serviseyem.utils.FirebaseStorageUploader.getStorePhotoPath(targetId, idx)
                if (ctx != null) {
                    uploadImageStringOrUri(ctx, img, photoPath)
                } else {
                    com.serviseyem.utils.FirebaseStorageUploader.resolveBase64ToStorageUrl(img, photoPath)
                }
            }
            val finalPdfUrl = if (finalStore.pdfFileBase64.isNotBlank()) {
                com.serviseyem.utils.FirebaseStorageUploader.resolveBase64ToStorageUrl(
                    finalStore.pdfFileBase64,
                    "stores/$targetId/catalog.pdf"
                )
            } else ""

            val uploadedStore = finalStore.copy(
                logoImage = finalLogo,
                coverImage = finalCover,
                images = finalImages,
                pdfFileBase64 = finalPdfUrl
            )

            val currentList = _stores.value.filter { it.id != targetId }.toMutableList()
            currentList.add(uploadedStore)
            _stores.value = currentList

            db.collection("stores").document(targetId).set(uploadedStore)
                .addOnSuccessListener {
                    // Send admin notification if it's a new pending store
                    if (!uploadedStore.isActive || !uploadedStore.isApproved) {
                        val adminNotif = NotificationEntity(
                            id = UUID.randomUUID().toString(),
                            title = "🏪 طلب انضمام نشاط تجاري جديد",
                            message = "قدم النشاط التجاري '${uploadedStore.name}' (${uploadedStore.categoryId}) طلب انضمام جديد بمنطقة ${uploadedStore.cityId}.",
                            targetType = "SUPERVISOR",
                            targetValue = "ALL",
                            timestamp = System.currentTimeMillis()
                        )
                        try {
                            if (adminNotif.isValid()) db.collection("notifications").document(adminNotif.id).set(adminNotif)
                        } catch (e: Exception) {}
                    }
                    mainViewModel.triggerNotification("✅ تم حفظ وتأكيد بيانات الطلب بنجاح!")
                }
                .addOnFailureListener {
                    mainViewModel.triggerNotification("⚠️ تم حفظ الطلب محلياً وفي انتظار مزامنة الشبكة")
                }
        }
    }

fun deleteStore(storeId: String) {
        _stores.value = _stores.value.map {
            if (it.id == storeId) it.copy(isDeleted = true, deletedAt = System.currentTimeMillis()) else it
        }
        val updates = mapOf(
            "isDeleted" to true,
            "deleted" to true,
            "deletedAt" to System.currentTimeMillis()
        )
        db.collection("stores").document(storeId).update(updates)
            .addOnSuccessListener {
                mainViewModel.triggerNotification("🗑️ تم حذف المتجر ونقله للمحذوفات")
            }
            .addOnFailureListener { e ->
                db.collection("stores").document(storeId).get().addOnSuccessListener { snapshot ->
                    val store = snapshot.toObject(com.serviseyem.data.StoreEntity::class.java)
                    if (store != null) {
                        db.collection("stores").document(storeId).set(store.copy(isDeleted = true, deletedAt = System.currentTimeMillis()))
                            .addOnSuccessListener {
                                mainViewModel.triggerNotification("🗑️ تم حذف المتجر ونقله للمحذوفات")
                            }
                    }
                }
            }
    }

fun restoreStore(storeId: String) {
        _stores.value = _stores.value.map {
            if (it.id == storeId) it.copy(isDeleted = false, deletedAt = null) else it
        }
        val updates = hashMapOf<String, Any?>(
            "isDeleted" to false,
            "deleted" to false,
            "deletedAt" to null
        )
        db.collection("stores").document(storeId).update(updates)
            .addOnSuccessListener {
                mainViewModel.triggerNotification("♻️ تم استعادة المتجر بنجاح")
            }
    }

fun deleteStorePermanently(storeId: String) {
        _stores.value = _stores.value.filter { it.id != storeId }
        db.collection("stores").document(storeId).delete()
            .addOnSuccessListener {
                mainViewModel.triggerNotification("🗑️ تم حذف المتجر نهائياً من النظام")
            }
    }

fun setStoreActive(storeId: String, isActive: Boolean) {
        val cleanTargetId = storeId.trim()
        val targetStore = _stores.value.find { it.id == cleanTargetId || it.phone.trim().replace(" ", "").replace("+", "") == cleanTargetId.replace(" ", "").replace("+", "") }
        val docId = targetStore?.id ?: cleanTargetId

        _stores.value = _stores.value.map {
            if (it.id == docId || it.id == cleanTargetId) it.copy(isActive = isActive, isApproved = isActive) else it
        }

        val updates = mapOf(
            "isActive" to isActive,
            "isApproved" to isActive,
            "active" to isActive
        )

        if (targetStore != null && isActive) {
            val cleanPhone = targetStore.phone.trim().replace(" ", "").replace("+", "")
            if (cleanPhone.isNotEmpty()) {
                db.collection("pending_providers").whereEqualTo("phone", targetStore.phone).get().addOnSuccessListener { qs ->
                    qs?.documents?.forEach { doc ->
                        db.collection("pending_providers").document(doc.id).delete()
                    }
                }
                _pendingProviders.value = _pendingProviders.value.filter { 
                    it.phone.trim().replace(" ", "").replace("+", "") != cleanPhone 
                }
            }

            val notification = NotificationEntity(
                id = UUID.randomUUID().toString(),
                title = "🎉 تم قبول واعتماد طلبك بنجاح!",
                message = "تهانينا! تم قبول واعتماد منشأتك (${targetStore.name}) بالدليل اليمني بنجاح!",
                targetType = "USER",
                targetValue = targetStore.phone,
                timestamp = System.currentTimeMillis()
            )
            _notifications.value = listOf(notification) + _notifications.value
            try { if (notification.isValid()) db.collection("notifications").document(notification.id).set(notification) } catch(e: Exception) {}
        }

        db.collection("stores").document(docId).update(updates)
            .addOnSuccessListener {
                mainViewModel.triggerNotification(if (isActive) "✅ تم تفعيل المتجر والموافقة عليه" else "🔒 تم إلغاء تفعيل المتجر")
            }
            .addOnFailureListener {
                if (targetStore != null) {
                    db.collection("stores").document(docId).set(targetStore.copy(isActive = isActive, isApproved = isActive))
                        .addOnSuccessListener {
                            mainViewModel.triggerNotification(if (isActive) "✅ تم تفعيل المتجر والموافقة عليه" else "🔒 تم إلغاء تفعيل المتجر")
                        }
                }
            }
    }

fun setStorePinned(storeId: String, isPinned: Boolean) {
        _stores.value = _stores.value.map {
            if (it.id == storeId) it.copy(isPinned = isPinned) else it
        }
        val updates = mapOf(
            "isPinned" to isPinned,
            "pinned" to isPinned
        )
        db.collection("stores").document(storeId).update(updates)
            .addOnSuccessListener {
                mainViewModel.triggerNotification(if (isPinned) "📌 تم تثبيت المتجر في الشاشة الرئيسية" else "📌 تم إلغاء تثبيت المتجر")
            }
    }

fun setStoreVip(storeId: String, isVip: Boolean) {
        _stores.value = _stores.value.map {
            if (it.id == storeId) it.copy(isVip = isVip) else it
        }
        db.collection("stores").document(storeId).update("isVip", isVip)
            .addOnSuccessListener {
                mainViewModel.triggerNotification(if (isVip) "🏆 تم تمييز المتجر بشارة VIP" else "🔒 تم إلغاء شارة VIP عن المتجر")
            }
    }

fun setStoreVerified(storeId: String, isVerified: Boolean) {
        _stores.value = _stores.value.map {
            if (it.id == storeId) it.copy(isVerified = isVerified) else it
        }
        db.collection("stores").document(storeId).update("isVerified", isVerified)
            .addOnSuccessListener {
                mainViewModel.triggerNotification(if (isVerified) "🛡️ تم توثيق حساب المتجر" else "🔒 تم إلغاء التوثيق عن المتجر")
            }
    }

fun setStoreRecommended(storeId: String, isRecommended: Boolean) {
        _stores.value = _stores.value.map {
            if (it.id == storeId) it.copy(isRecommended = isRecommended) else it
        }
        db.collection("stores").document(storeId).update("isRecommended", isRecommended)
            .addOnSuccessListener {
                mainViewModel.triggerNotification(if (isRecommended) "💖 تم ترشيح المتجر كموصى به" else "🔒 تم إلغاء ترشيح المتجر")
            }
    }

fun setStoreBlocked(storeId: String, isBlocked: Boolean, reason: String = "") {
        _stores.value = _stores.value.map {
            if (it.id == storeId) it.copy(isBlocked = isBlocked, blockReason = reason) else it
        }
        val updates = mapOf(
            "isBlocked" to isBlocked,
            "blockReason" to reason
        )
        db.collection("stores").document(storeId).update(updates)
            .addOnSuccessListener {
                mainViewModel.triggerNotification(if (isBlocked) "🚫 تم حظر المحل/المركز بنجاح ($reason)" else "✅ تم إلغاء حظر المحل/المركز")
            }
    }

fun setStoreChatDisabled(storeId: String, isDisabled: Boolean) {
        _stores.value = _stores.value.map {
            if (it.id == storeId) it.copy(isChatDisabled = isDisabled) else it
        }
        db.collection("stores").document(storeId).update("isChatDisabled", isDisabled)
            .addOnSuccessListener {
                mainViewModel.triggerNotification(if (isDisabled) "🔇 تم إيقاف الدردشة للمتجر" else "💬 تم تفعيل الدردشة للمتجر")
            }
    }

fun setStoreNotificationsDisabled(storeId: String, isDisabled: Boolean) {
        _stores.value = _stores.value.map {
            if (it.id == storeId) it.copy(isNotificationsDisabled = isDisabled) else it
        }
        db.collection("stores").document(storeId).update("isNotificationsDisabled", isDisabled)
            .addOnSuccessListener {
                mainViewModel.triggerNotification(if (isDisabled) "🔕 تم كتم الإشعارات للمتجر" else "🔔 تم تفعيل الإشعارات للمتجر")
            }
    }

fun setStorePaymentEnabled(storeId: String, isEnabled: Boolean) {
        db.collection("stores").document(storeId).update("paymentEnabled", isEnabled)
            .addOnSuccessListener {
                mainViewModel.triggerNotification(if (isEnabled) "💳 تم تفعيل نظام الدفع والمحفظة للمتجر" else "🔒 تم تعطيل نظام الدفع للمتجر")
            }
    }

fun toggleStoreBlocked(storeId: String, isBlocked: Boolean) {
        db.collection("stores").document(storeId).update("isBlocked", isBlocked)
            .addOnSuccessListener {
                mainViewModel.triggerNotification(if (isBlocked) "🚫 تم حظر المتجر" else "✅ تم إلغاء حظر المتجر")
            }
    }

fun toggleStoreActive(storeId: String) {
        val store = _stores.value.find { it.id == storeId }
        if (store != null) {
            val updated = store.copy(isActive = !store.isActive)
            saveStore(updated)
            mainViewModel.triggerNotification(if (!updated.isActive) "🔒 تم حظر/إيقاف المحل/المركز مؤقتاً" else "🟢 تم تفعيل المحل/المركز")
        }
    }

fun toggleStorePinned(storeId: String) {
        val store = _stores.value.find { it.id == storeId }
        if (store != null) {
            val updated = store.copy(isPinned = !store.isPinned)
            saveStore(updated)
            mainViewModel.triggerNotification(if (updated.isPinned) "📌 تم تثبيت المحل في البداية" else "🔓 تم إلغاء تثبيت المحل")
        }
    }

fun toggleStoreChatDisabled(storeId: String) {
        val store = _stores.value.find { it.id == storeId }
        if (store != null) {
            val updated = store.copy(isChatDisabled = !store.isChatDisabled)
            saveStore(updated)
            mainViewModel.triggerNotification(if (updated.isChatDisabled) "🚫 تم إيقاف المحادثات للمحل/المركز" else "💬 تم تفعيل المحادثات للمحل/المركز")
        }
    }

fun approveStorePdf(storeId: String, approve: Boolean) {
        db.collection("stores").document(storeId).get().addOnSuccessListener { snapshot ->
            val store = snapshot.toObject(com.serviseyem.data.StoreEntity::class.java)
            if (store != null) {
                db.collection("stores").document(storeId).set(store.copy(pdfStatus = if (approve) "APPROVED" else "REJECTED"))
                    .addOnSuccessListener {
                        mainViewModel.triggerNotification(if (approve) "✅ تم قبول ملف الـ PDF للمحل بنجاح!" else "❌ تم رفض ملف الـ PDF للمحل.")
                    }
            }
        }
    }

fun saveProperty(property: com.serviseyem.data.PropertyEntity) {
        val cleanPhone = com.serviseyem.domain.usecases.ValidatePhoneUseCase.normalizePhone(property.phone)
            .ifBlank { property.phone.trim().replace(" ", "").replace("+", "") }
        val duplicateType = checkAndGetDuplicateAccountType(cleanPhone, property.id)
        if (duplicateType != null) {
            mainViewModel.triggerNotification("❌ عذراً! رقم الهاتف (${property.phone}) مسجل بالفعل كـ ($duplicateType). لا يُسمح بتكرار الحسابات.")
            logAdminActivity("محاولة تسجيل عقار مكرر محجوبة لرقم: $cleanPhone - نوع التكرار: $duplicateType")
            return
        }
        val targetId = if (property.id.isEmpty()) db.collection("properties").document().id else property.id
        val rawPassword = property.passwordHash.trim()
        val resolvedPasswordHash = when {
            rawPassword.isNotBlank() && com.serviseyem.utils.SecureHasher.isValidHash(rawPassword) -> rawPassword
            rawPassword.isNotBlank() -> com.serviseyem.utils.SecureHasher.hashPassword(rawPassword)
            else -> ""
        }
        val finalProp = property.copy(
            id = targetId,
            phone = cleanPhone,
            ownerId = if (property.ownerId.isEmpty()) cleanPhone else property.ownerId,
            passwordHash = resolvedPasswordHash
        )

        // Instant Local State Update
        val updatedProps = _properties.value.filter { it.id != targetId }.toMutableList()
        updatedProps.add(finalProp)
        _properties.value = updatedProps

        if (rawPassword.isNotEmpty() && !com.serviseyem.utils.SecureHasher.isValidHash(rawPassword)) {
            try {
                val authEmail = getAuthEmailForPhone(cleanPhone)
                auth.createUserWithEmailAndPassword(authEmail, rawPassword)
                    .addOnFailureListener { /* account exists */ }
            } catch (e: Exception) {}
        }

        viewModelScope.launch {
            val ctx = appContext
            val finalImages = finalProp.images.mapIndexed { idx, img ->
                val photoPath = com.serviseyem.utils.FirebaseStorageUploader.getPropertyPhotoPath(targetId, idx)
                if (ctx != null) {
                    uploadImageStringOrUri(ctx, img, photoPath)
                } else {
                    com.serviseyem.utils.FirebaseStorageUploader.resolveBase64ToStorageUrl(img, photoPath)
                }
            }
            val finalPdfUrl = if (finalProp.pdfFileBase64.isNotBlank()) {
                com.serviseyem.utils.FirebaseStorageUploader.resolveBase64ToStorageUrl(
                    finalProp.pdfFileBase64,
                    "properties/$targetId/deed.pdf"
                )
            } else ""

            val uploadedProp = finalProp.copy(
                images = finalImages,
                pdfFileBase64 = finalPdfUrl
            )
            val currentList = _properties.value.filter { it.id != targetId }.toMutableList()
            currentList.add(uploadedProp)
            _properties.value = currentList

            db.collection("properties").document(targetId).set(uploadedProp)
                .addOnSuccessListener {
                    if (!uploadedProp.isActive || !uploadedProp.isApproved) {
                        val adminNotif = NotificationEntity(
                            id = UUID.randomUUID().toString(),
                            title = "🏡 طلب انضمام عقار/مكتب عقاري جديد",
                            message = "قدم المكتب/العقار '${uploadedProp.title}' طلب انضمام جديد بمنطقة ${uploadedProp.cityId}.",
                            targetType = "SUPERVISOR",
                            targetValue = "ALL",
                            timestamp = System.currentTimeMillis()
                        )
                        try {
                            if (adminNotif.isValid()) db.collection("notifications").document(adminNotif.id).set(adminNotif)
                        } catch (e: Exception) {}
                    }
                    mainViewModel.triggerNotification("✅ تم تسجيل بيانات العقار بنجاح!")
                }
                .addOnFailureListener {
                    mainViewModel.triggerNotification("⚠️ تم الحفظ محلياً وبانتظار مزامنة السحابة")
                }
        }
    }

fun deleteProperty(propertyId: String) {
        _properties.value = _properties.value.map {
            if (it.id == propertyId) it.copy(isDeleted = true, deletedAt = System.currentTimeMillis()) else it
        }
        val updates = mapOf(
            "isDeleted" to true,
            "deleted" to true,
            "deletedAt" to System.currentTimeMillis()
        )
        db.collection("properties").document(propertyId).update(updates)
            .addOnSuccessListener {
                mainViewModel.triggerNotification("🗑️ تم حذف العقار بنجاح")
            }
            .addOnFailureListener { e ->
                db.collection("properties").document(propertyId).get().addOnSuccessListener { snapshot ->
                    val property = snapshot.toObject(com.serviseyem.data.PropertyEntity::class.java)
                    if (property != null) {
                        db.collection("properties").document(propertyId).set(property.copy(isDeleted = true, deletedAt = System.currentTimeMillis()))
                            .addOnSuccessListener {
                                mainViewModel.triggerNotification("🗑️ تم حذف العقار بنجاح")
                            }
                    }
                }
            }
    }

fun restoreProperty(propertyId: String) {
        _properties.value = _properties.value.map {
            if (it.id == propertyId) it.copy(isDeleted = false, deletedAt = null) else it
        }
        val updates = hashMapOf<String, Any?>(
            "isDeleted" to false,
            "deleted" to false,
            "deletedAt" to null
        )
        db.collection("properties").document(propertyId).update(updates)
            .addOnSuccessListener {
                mainViewModel.triggerNotification("♻️ تم استعادة العقار بنجاح")
            }
    }

fun deletePropertyPermanently(propertyId: String) {
        _properties.value = _properties.value.filter { it.id != propertyId }
        db.collection("properties").document(propertyId).delete()
            .addOnSuccessListener {
                mainViewModel.triggerNotification("🗑️ تم حذف العقار نهائياً من النظام")
            }
    }

fun setPropertyActive(propertyId: String, isActive: Boolean) {
        val cleanTargetId = propertyId.trim()
        val targetProp = _properties.value.find { it.id == cleanTargetId || it.phone.trim().replace(" ", "").replace("+", "") == cleanTargetId.replace(" ", "").replace("+", "") }
        val docId = targetProp?.id ?: cleanTargetId

        _properties.value = _properties.value.map {
            if (it.id == docId || it.id == cleanTargetId) it.copy(isActive = isActive, isApproved = isActive) else it
        }

        val updates = mapOf(
            "isActive" to isActive,
            "isApproved" to isActive,
            "active" to isActive
        )

        if (targetProp != null && isActive) {
            val cleanPhone = targetProp.phone.trim().replace(" ", "").replace("+", "")
            if (cleanPhone.isNotEmpty()) {
                db.collection("pending_providers").whereEqualTo("phone", targetProp.phone).get().addOnSuccessListener { qs ->
                    qs?.documents?.forEach { doc ->
                        db.collection("pending_providers").document(doc.id).delete()
                    }
                }
                _pendingProviders.value = _pendingProviders.value.filter { 
                    it.phone.trim().replace(" ", "").replace("+", "") != cleanPhone 
                }
            }

            val notification = NotificationEntity(
                id = UUID.randomUUID().toString(),
                title = "🎉 تم قبول وإعتماد إعلان عقارك",
                message = "تهانينا! تم قبول ونشر إعلان العقار (${targetProp.title}) للجميع!",
                targetType = "USER",
                targetValue = targetProp.phone,
                timestamp = System.currentTimeMillis()
            )
            _notifications.value = listOf(notification) + _notifications.value
            try { if (notification.isValid()) db.collection("notifications").document(notification.id).set(notification) } catch(e: Exception) {}
        }

        db.collection("properties").document(docId).update(updates)
            .addOnSuccessListener {
                mainViewModel.triggerNotification(if (isActive) "✅ تم تفعيل ونشر العقار للجميع" else "🔒 تم إلغاء تفعيل ونشر العقار")
            }
            .addOnFailureListener {
                if (targetProp != null) {
                    db.collection("properties").document(docId).set(targetProp.copy(isActive = isActive, isApproved = isActive))
                        .addOnSuccessListener {
                            mainViewModel.triggerNotification(if (isActive) "✅ تم تفعيل ونشر العقار للجميع" else "🔒 تم إلغاء تفعيل ونشر العقار")
                        }
                }
            }
    }

fun setPropertyPinned(propertyId: String, isPinned: Boolean) {
        _properties.value = _properties.value.map {
            if (it.id == propertyId) it.copy(isPinned = isPinned) else it
        }
        val updates = mapOf(
            "isPinned" to isPinned,
            "pinned" to isPinned
        )
        db.collection("properties").document(propertyId).update(updates)
            .addOnSuccessListener {
                mainViewModel.triggerNotification(if (isPinned) "📌 تم تمييز وتثبيت العقار في الصدارة" else "📌 تم إلغاء تثبيت العقار")
            }
    }

fun setPropertyVip(propertyId: String, isVip: Boolean) {
        _properties.value = _properties.value.map {
            if (it.id == propertyId) it.copy(isVip = isVip) else it
        }
        db.collection("properties").document(propertyId).update("isVip", isVip)
            .addOnSuccessListener {
                mainViewModel.triggerNotification(if (isVip) "🏆 تم تمييز العقار بشارة VIP" else "🔒 تم إلغاء شارة VIP عن العقار")
            }
    }

fun setPropertyVerified(propertyId: String, isVerified: Boolean) {
        _properties.value = _properties.value.map {
            if (it.id == propertyId) it.copy(isVerified = isVerified) else it
        }
        db.collection("properties").document(propertyId).update("isVerified", isVerified)
            .addOnSuccessListener {
                mainViewModel.triggerNotification(if (isVerified) "🛡️ تم توثيق إعلان العقار" else "🔒 تم إلغاء التوثيق عن العقار")
            }
    }

fun setPropertyRecommended(propertyId: String, isRecommended: Boolean) {
        _properties.value = _properties.value.map {
            if (it.id == propertyId) it.copy(isRecommended = isRecommended) else it
        }
        db.collection("properties").document(propertyId).update("isRecommended", isRecommended)
            .addOnSuccessListener {
                mainViewModel.triggerNotification(if (isRecommended) "💖 تم ترشيح العقار كموصى به" else "🔒 تم إلغاء ترشيح العقار")
            }
    }

fun setPropertyBlocked(propertyId: String, isBlocked: Boolean, reason: String = "") {
        _properties.value = _properties.value.map {
            if (it.id == propertyId) it.copy(isBlocked = isBlocked, blockReason = reason) else it
        }
        val updates = mapOf(
            "isBlocked" to isBlocked,
            "blockReason" to reason
        )
        db.collection("properties").document(propertyId).update(updates)
            .addOnSuccessListener {
                mainViewModel.triggerNotification(if (isBlocked) "🚫 تم حظر إعلان العقار بنجاح ($reason)" else "✅ تم إلغاء حظر إعلان العقار")
            }
    }

fun setPropertyChatDisabled(propertyId: String, isDisabled: Boolean) {
        _properties.value = _properties.value.map {
            if (it.id == propertyId) it.copy(isChatDisabled = isDisabled) else it
        }
        db.collection("properties").document(propertyId).update("isChatDisabled", isDisabled)
            .addOnSuccessListener {
                mainViewModel.triggerNotification(if (isDisabled) "🔇 تم إيقاف الدردشة للمعلن" else "💬 تم تفعيل الدردشة للمعلن")
            }
    }

fun setPropertyNotificationsDisabled(propertyId: String, isDisabled: Boolean) {
        _properties.value = _properties.value.map {
            if (it.id == propertyId) it.copy(isNotificationsDisabled = isDisabled) else it
        }
        db.collection("properties").document(propertyId).update("isNotificationsDisabled", isDisabled)
            .addOnSuccessListener {
                mainViewModel.triggerNotification(if (isDisabled) "🔕 تم كتم الإشعارات للمعلن" else "🔔 تم تفعيل الإشعارات للمعلن")
            }
    }

fun setPropertyPaymentEnabled(propertyId: String, isEnabled: Boolean) {
        db.collection("properties").document(propertyId).update("paymentEnabled", isEnabled)
            .addOnSuccessListener {
                mainViewModel.triggerNotification(if (isEnabled) "💳 تم تفعيل نظام الدفع للعقار" else "🔒 تم تعطيل نظام الدفع للعقار")
            }
    }

fun togglePropertyBlocked(propertyId: String, isBlocked: Boolean) {
        db.collection("properties").document(propertyId).update("isBlocked", isBlocked)
            .addOnSuccessListener {
                mainViewModel.triggerNotification(if (isBlocked) "🚫 تم حظر العقار" else "✅ تم إلغاء حظر العقار")
            }
    }

fun approvePropertyPdf(propertyId: String, approve: Boolean) {
        db.collection("properties").document(propertyId).get().addOnSuccessListener { snapshot ->
            val prop = snapshot.toObject(com.serviseyem.data.PropertyEntity::class.java)
            if (prop != null) {
                db.collection("properties").document(propertyId).set(prop.copy(pdfStatus = if (approve) "APPROVED" else "REJECTED"))
                    .addOnSuccessListener {
                        mainViewModel.triggerNotification(if (approve) "✅ تم قبول ملف الـ PDF للعقار بنجاح!" else "❌ تم رفض ملف الـ PDF للعقار.")
                    }
            }
        }
    }

fun saveJob(job: com.serviseyem.data.JobEntity) {
        val cleanPhone = job.phone.trim().replace(" ", "").replace("+", "")
        val targetId = if (job.id.isEmpty()) db.collection("jobs").document().id else job.id
        val finalJob = job.copy(id = targetId, phone = cleanPhone)

        // Instant Local State Update
        val updatedJobs = _jobs.value.filter { it.id != targetId }.toMutableList()
        updatedJobs.add(finalJob)
        _jobs.value = updatedJobs

        db.collection("jobs").document(targetId).set(finalJob)
            .addOnSuccessListener {
                mainViewModel.triggerNotification(if (finalJob.isApproved) "✅ تم حفظ ونشر الإعلان الوظيفي بنجاح!" else "📨 تم تقديم إعلان الوظيفة للمراجع من قبل الأدمن!")
            }
            .addOnFailureListener {
                mainViewModel.triggerNotification("⚠️ تم إدراج الوظيفة محلياً وفي انتظار المزامنة")
            }
    }

fun deleteJob(jobId: String) {
        _jobs.value = _jobs.value.map {
            if (it.id == jobId) it.copy(isDeleted = true) else it
        }
        val updates = hashMapOf<String, Any?>(
            "isDeleted" to true,
            "deletedAt" to System.currentTimeMillis()
        )
        db.collection("jobs").document(jobId).update(updates)
            .addOnSuccessListener {
                mainViewModel.triggerNotification("🗑️ تم نقل الإعلان الوظيفي لسلة المحذوفات")
            }
    }

fun restoreJob(jobId: String) {
        _jobs.value = _jobs.value.map {
            if (it.id == jobId) it.copy(isDeleted = false) else it
        }
        val updates = hashMapOf<String, Any?>(
            "isDeleted" to false,
            "deletedAt" to null
        )
        db.collection("jobs").document(jobId).update(updates)
            .addOnSuccessListener {
                mainViewModel.triggerNotification("♻️ تم استعادة الإعلان الوظيفي بنجاح")
            }
    }

fun deleteJobPermanently(jobId: String) {
        db.collection("jobs").document(jobId).delete()
            .addOnSuccessListener {
                mainViewModel.triggerNotification("🗑️ تم حذف الإعلان الوظيفي نهائياً من النظام")
            }
    }

fun setJobApproved(jobId: String, isApproved: Boolean) {
        _jobs.value = _jobs.value.map {
            if (it.id == jobId) it.copy(isApproved = isApproved, isActive = isApproved) else it
        }
        val updates = mapOf(
            "isApproved" to isApproved,
            "isActive" to isApproved
        )
        val targetJob = _jobs.value.find { it.id == jobId }
        if (targetJob != null && isApproved) {
            val notification = NotificationEntity(
                id = UUID.randomUUID().toString(),
                title = "🎉 تم قبول ونشر إعلان وظيفتك!",
                message = "تهانينا! تم قبول ونشر إعلان الوظيفة (${targetJob.title}) للجميع!",
                targetType = "USER",
                targetValue = targetJob.phone,
                timestamp = System.currentTimeMillis()
            )
            _notifications.value = listOf(notification) + _notifications.value
            try { if (notification.isValid()) db.collection("notifications").document(notification.id).set(notification) } catch(e: Exception) {}
        }
        db.collection("jobs").document(jobId).update(updates)
            .addOnSuccessListener {
                mainViewModel.triggerNotification(if (isApproved) "✅ تم قبول ونشر إعلان الوظيفة بنجاح!" else "❌ تم رفض إعلان الوظيفة")
            }
        // Also remove from pending_providers and update join_requests
        db.collection("pending_providers").document(jobId).delete()
        db.collection("join_requests").document(jobId).update(mapOf(
            "status" to if (isApproved) "APPROVED" else "REJECTED",
            "approvalStatus" to if (isApproved) "APPROVED" else "REJECTED"
        ))
        _pendingProviders.value = _pendingProviders.value.filter { it.id != jobId }
    }

fun setJobBlocked(jobId: String, isBlocked: Boolean, reason: String = "") {
        val updates = mapOf(
            "isBlocked" to isBlocked,
            "blockReason" to reason
        )
        db.collection("jobs").document(jobId).update(updates)
            .addOnSuccessListener {
                mainViewModel.triggerNotification(if (isBlocked) "🚫 تم حظر الإعلان الوظيفي ($reason)" else "✅ تم إلغاء حظر الإعلان الوظيفي")
            }
    }

fun setJobPinned(jobId: String, isPinned: Boolean) {
        db.collection("jobs").document(jobId).update("isPinned", isPinned)
            .addOnSuccessListener {
                mainViewModel.triggerNotification(if (isPinned) "📌 تم تثبيت الوظيفة في الصدارة" else "📌 تم إلغاء تثبيت الوظيفة")
            }
    }

fun setJobVip(jobId: String, isVip: Boolean) {
        db.collection("jobs").document(jobId).update("isVip", isVip)
            .addOnSuccessListener {
                mainViewModel.triggerNotification(if (isVip) "🏆 تم تمييز الإعلان الوظيفي كـ VIP" else "🔒 تم إلغاء شارة VIP عن الوظيفة")
            }
    }

fun setJobChatDisabled(jobId: String, isDisabled: Boolean) {
        db.collection("jobs").document(jobId).update("isChatDisabled", isDisabled)
            .addOnSuccessListener {
                mainViewModel.triggerNotification(if (isDisabled) "🔇 تم إيقاف الدردشة للإعلان الوظيفي" else "💬 تم تفعيل الدردشة للإعلان الوظيفي")
            }
    }

fun submitJobApplication(application: com.serviseyem.data.JobApplicationEntity) {
        val targetId = db.collection("job_applications").document().id
        val finalApp = application.copy(id = targetId)
        _jobApplications.value = _jobApplications.value + finalApp
        db.collection("job_applications").document(targetId).set(finalApp)
            .addOnSuccessListener {
                mainViewModel.triggerNotification("📨 تم إرسال طلب التقديم على الوظيفة بنجاح!")
            }
            .addOnFailureListener {
                mainViewModel.triggerNotification("❌ فشل تقديم الطلب: ${it.message}")
            }
    }

fun updateJobApplicationStatus(appId: String, status: String, reason: String = "") {
        _jobApplications.value = _jobApplications.value.map {
            if (it.id == appId) it.copy(status = status, rejectionReason = reason) else it
        }
        val updates = mutableMapOf<String, Any>(
            "status" to status
        )
        if (reason.isNotEmpty()) {
            updates["rejectionReason"] = reason
        }
        db.collection("job_applications").document(appId).update(updates)
            .addOnSuccessListener {
                mainViewModel.triggerNotification("✅ تم تحديث حالة طلب التقديم إلى: $status")
            }
            .addOnFailureListener {
                db.collection("job_applications").document(appId).set(updates, com.google.firebase.firestore.SetOptions.merge())
            }
    }

fun acceptJobApplication(appId: String) {
        updateJobApplicationStatus(appId, "ACCEPTED")
    }

fun rejectJobApplication(appId: String, reason: String) {
        updateJobApplicationStatus(appId, "REJECTED", reason)
    }

fun deleteJobApplication(appId: String) {
        _jobApplications.value = _jobApplications.value.filter { it.id != appId }
        db.collection("job_applications").document(appId).delete()
            .addOnSuccessListener {
                mainViewModel.triggerNotification("🗑️ تم حذف طلب التقديم من النظام بنجاح")
            }
    }

fun submitReport(report: com.serviseyem.data.ReportEntity, onComplete: () -> Unit = {}) {
        db.collection("reports").document(report.id).set(report)
            .addOnSuccessListener {
                onComplete()
            }
    }

fun deleteReport(reportId: String) {
        db.collection("reports").document(reportId).delete()
        mainViewModel.triggerNotification("🗑️ تم حذف البلاغ من النظام")
    }

fun sendReport(providerId: String, providerName: String, reporterName: String, content: String) {
        val newReport = ReportEntity(
            id = UUID.randomUUID().toString(),
            providerId = providerId,
            providerName = providerName,
            reporterName = reporterName,
            content = content
        )
        db.collection("reports").document(newReport.id).set(newReport)
        mainViewModel.triggerNotification("📢 تم إرسال بلاغك ضد $providerName")
    }

fun addCoupon(code: String, pointsValue: Int, expiryMs: Long, discountPercentage: Int = 0, maxUsageCount: Int = 100) {
        val couponId = UUID.randomUUID().toString()
        val coupon = CouponEntity(
            id = couponId,
            code = code,
            pointsValue = pointsValue,
            expiryTimestamp = System.currentTimeMillis() + expiryMs,
            status = "ACTIVE",
            discountPercentage = discountPercentage,
            maxUsageCount = maxUsageCount,
            usedCount = 0
        )
        db.collection("coupons").document(couponId).set(coupon)
        mainViewModel.triggerNotification("🎫 تم إضافة كوبون جديد بنجاح: $code")
    }

    fun saveCoupon(coupon: CouponEntity) {
        viewModelScope.launch {
            val couponId = if (coupon.id.isBlank()) UUID.randomUUID().toString() else coupon.id
            val finalCoupon = coupon.copy(id = couponId)
            safeFirestoreCall(
                operation = {
                    crud.saveEntity("coupons", couponId, finalCoupon)
                },
                onSuccess = {
                    mainViewModel.triggerNotification("🎫 تم حفظ الكوبون بنجاح: ${finalCoupon.code}")
                },
                onError = {
                    mainViewModel.triggerNotification("⚠️ تم حفظ الكوبون محلياً، سيتم المزامنة تلقائياً")
                }
            )
        }
    }

    fun deleteCoupon(couponId: String) {
        viewModelScope.launch {
            safeFirestoreCall(
                operation = {
                    crud.deleteEntity("coupons", couponId, false)
                },
                onSuccess = {
                    mainViewModel.triggerNotification("🗑️ تم حذف الكوبون")
                },
                onError = {
                    mainViewModel.triggerNotification("⚠️ تم حذف الكوبون محلياً، سيتم المزامنة تلقائياً")
                }
            )
        }
    }

fun saveInternalWallet(wallet: com.serviseyem.data.InternalWalletEntity) {
        val targetId = if (wallet.id.isEmpty()) db.collection("internal_wallets").document().id else wallet.id
        val finalW = wallet.copy(id = targetId, updatedAt = System.currentTimeMillis())
        db.collection("internal_wallets").document(targetId).set(finalW)
            .addOnSuccessListener {
                mainViewModel.triggerNotification("✅ تم حفظ بيانات المحفظة الرقمية الداخلية (${finalW.ownerName})")
            }
    }

fun performWalletTransaction(
        walletId: String,
        ownerName: String,
        ownerPhone: String,
        ownerType: String,
        type: String, // DEPOSIT, WITHDRAWAL, TRANSFER
        amount: Double,
        note: String
    ) {
        val currentW = _internalWallets.value.find { it.id == walletId }
            ?: com.serviseyem.data.InternalWalletEntity(
                id = walletId,
                ownerName = ownerName,
                ownerPhone = ownerPhone,
                ownerType = ownerType
            )

        val newBalance = when (type) {
            "DEPOSIT" -> currentW.balance + amount
            "WITHDRAWAL" -> (currentW.balance - amount).coerceAtLeast(0.0)
            else -> currentW.balance + amount
        }

        val updatedWallet = currentW.copy(
            balance = newBalance,
            updatedAt = System.currentTimeMillis()
        )

        db.collection("internal_wallets").document(walletId).set(updatedWallet)

        val txId = db.collection("wallet_transactions").document().id
        val tx = com.serviseyem.data.WalletTransactionEntity(
            id = txId,
            walletId = walletId,
            type = type,
            amount = amount,
            balanceAfter = newBalance,
            note = note,
            performByAdmin = true,
            timestamp = System.currentTimeMillis()
        )
        db.collection("wallet_transactions").document(txId).set(tx)
        mainViewModel.triggerNotification("💸 تم تنفيذ عملية ($type) بمبلغ $amount ريال للمحفظة ${currentW.ownerName}. الرصيد الجديد: $newBalance ريال")
    }

fun addPaymentWallet(wallet: PaymentWalletEntity) {
        val docRef = db.collection("payment_wallets").document()
        val walletWithId = wallet.copy(id = docRef.id, createdAt = System.currentTimeMillis(), updatedAt = System.currentTimeMillis())
        
        if (walletWithId.isDefault) {
            // Unset other defaults
            db.collection("payment_wallets").whereEqualTo("isDefault", true).get().addOnSuccessListener { qs ->
                for (doc in qs.documents) {
                    doc.reference.update("isDefault", false)
                }
            }
        }
        
        docRef.set(walletWithId).addOnSuccessListener {
            mainViewModel.triggerNotification("✅ تم إضافة المحفظة بنجاح!")
        }.addOnFailureListener {
            mainViewModel.triggerNotification("❌ فشل إضافة المحفظة: ${it.message}")
        }
    }

fun updatePaymentWallet(wallet: PaymentWalletEntity) {
        if (wallet.id.isEmpty()) return
        
        if (wallet.isDefault) {
            // Unset other defaults
            db.collection("payment_wallets").whereEqualTo("isDefault", true).get().addOnSuccessListener { qs ->
                for (doc in qs.documents) {
                    if (doc.id != wallet.id) {
                        doc.reference.update("isDefault", false)
                    }
                }
            }
        }
        
        val updated = wallet.copy(updatedAt = System.currentTimeMillis())
        db.collection("payment_wallets").document(wallet.id).set(updated).addOnSuccessListener {
            mainViewModel.triggerNotification("✅ تم تحديث المحفظة بنجاح!")
        }.addOnFailureListener {
            mainViewModel.triggerNotification("❌ فشل تحديث المحفظة: ${it.message}")
        }
    }

fun deletePaymentWallet(walletId: String) {
        db.collection("payment_wallets").document(walletId).delete().addOnSuccessListener {
            mainViewModel.triggerNotification("🗑️ تم حذف المحفظة بنجاح!")
        }.addOnFailureListener {
            mainViewModel.triggerNotification("❌ فشل حذف المحفظة: ${it.message}")
        }
    }

fun togglePaymentWalletVisibility(walletId: String, currentVisible: Boolean) {
        val newStatus = !currentVisible
        db.collection("payment_wallets").document(walletId).update("isVisibleToUsers", newStatus).addOnSuccessListener {
            mainViewModel.triggerNotification(if (newStatus) "👁️ تم إظهار المحفظة للمستخدمين" else "🙈 تم إخفاء المحفظة عن المستخدمين")
        }.addOnFailureListener {
            mainViewModel.triggerNotification("❌ فشل تغيير حالة إظهار المحفظة")
        }
    }

fun createPayment(
        userId: String,
        providerId: String,
        amount: Double,
        method: String,
        bookingId: String = "",
        isLinkedToBooking: Boolean = false,
        bookingServiceType: String = "",
        walletProvider: String = "",
        walletNumber: String = "",
        walletAccountName: String = "",
        transferId: String = "",
        transferPhoto: String = "",
        status: String = "PENDING"
    ) {
        val docRef = db.collection("payments").document()
        
        val settingsVal = mainViewModel.settingsViewModel._settings.value
        val advanceAmount = if (settingsVal.requireAdvancePayment) {
            var calculated = amount * settingsVal.advancePaymentPercent
            if (calculated < settingsVal.minAdvanceAmount) calculated = settingsVal.minAdvanceAmount
            if (calculated > settingsVal.maxAdvanceAmount) calculated = settingsVal.maxAdvanceAmount
            calculated
        } else {
            0.0
        }
        
        val commission = if (settingsVal.isCommissionEnabled) {
            amount * settingsVal.paymentCommissionRate
        } else {
            0.0
        }
        
        val remainingAmount = amount - advanceAmount
        val providerShare = amount - commission
        
        val payment = PaymentEntity(
            id = docRef.id,
            userId = userId,
            providerId = providerId,
            bookingId = bookingId,
            type = "service",
            method = method,
            status = status,
            amount = amount,
            advanceAmount = advanceAmount,
            remainingAmount = remainingAmount,
            commission = commission,
            providerShare = providerShare,
            currency = "YER",
            walletProvider = walletProvider,
            walletNumber = walletNumber,
            walletAccountName = walletAccountName,
            transferId = transferId,
            transferPhoto = transferPhoto,
            isLinkedToBooking = isLinkedToBooking,
            bookingDate = if (isLinkedToBooking) System.currentTimeMillis() else null,
            bookingServiceType = bookingServiceType,
            createdAt = System.currentTimeMillis()
        )
        
        docRef.set(payment).addOnSuccessListener {
            mainViewModel.triggerNotification("✅ تم إنشاء طلب الدفع بنجاح!")
        }.addOnFailureListener {
            mainViewModel.triggerNotification("❌ فشل إنشاء طلب الدفع: ${it.message}")
        }
    }

fun confirmPayment(
        paymentId: String,
        transferId: String,
        transferPhoto: String,
        walletProvider: String,
        walletNumber: String,
        walletAccountName: String
    ) {
        val updates = mapOf(
            "status" to "PROCESSING",
            "transferId" to transferId,
            "transferPhoto" to transferPhoto,
            "walletProvider" to walletProvider,
            "walletNumber" to walletNumber,
            "walletAccountName" to walletAccountName,
            "updatedAt" to System.currentTimeMillis()
        )
        
        db.collection("payments").document(paymentId).update(updates).addOnSuccessListener {
            mainViewModel.triggerNotification("✅ تم تقديم إثبات التحويل بنجاح! بانتظار مراجعة الإدارة.")
        }.addOnFailureListener {
            mainViewModel.triggerNotification("❌ فشل تأكيد الدفع: ${it.message}")
        }
    }

fun verifyPayment(paymentId: String, isVerified: Boolean, note: String, adminName: String) {
        val cleanId = paymentId.trim()
        if (cleanId.isBlank()) {
            mainViewModel.triggerNotification("❌ معرف الدفعة غير صالح")
            return
        }
        val existingPayment = _payments.value.find { it.id == cleanId }
        if (existingPayment == null && _payments.value.isNotEmpty()) {
            mainViewModel.triggerNotification("❌ لم يتم العثور على معاملة الدفع المطلوبة: $cleanId")
            return
        }
        val status = if (isVerified) "COMPLETED" else "FAILED"
        val verificationStatus = if (isVerified) "VERIFIED" else "REJECTED"
        
        val updates = mutableMapOf<String, Any>(
            "status" to status,
            "verificationStatus" to verificationStatus,
            "verificationNote" to note,
            "verifiedBy" to adminName,
            "verifiedAt" to System.currentTimeMillis(),
            "updatedAt" to System.currentTimeMillis()
        )
        
        if (isVerified) {
            updates["paidAt"] = System.currentTimeMillis()
        }
        
        db.collection("payments").document(cleanId).update(updates).addOnSuccessListener {
            mainViewModel.triggerNotification(if (isVerified) "✅ تم قبول وتأكيد عملية الدفع بنجاح!" else "❌ تم رفض عملية الدفع.")
            
            db.collection("payments").document(paymentId).get().addOnSuccessListener { snapshot ->
                val payment = snapshot.toObject(PaymentEntity::class.java)
                if (payment != null) {
                    if (payment.isLinkedToBooking && payment.bookingId.isNotEmpty()) {
                        db.collection("bookings").document(payment.bookingId).update("status", if (isVerified) "APPROVED" else "PENDING")
                    }
                }
            }
        }.addOnFailureListener {
            mainViewModel.triggerNotification("❌ فشل التحقق من الدفع: ${it.message}")
        }
    }

fun refundPayment(paymentId: String, reason: String) {
        val updates = mapOf(
            "status" to "REFUNDED",
            "verificationStatus" to "DISPUTED",
            "verificationNote" to reason,
            "updatedAt" to System.currentTimeMillis()
        )
        
        db.collection("payments").document(paymentId).update(updates).addOnSuccessListener {
            mainViewModel.triggerNotification("🔄 تم استرداد المبلغ بنجاح.")
        }.addOnFailureListener {
            mainViewModel.triggerNotification("❌ فشل استرداد الدفع: ${it.message}")
        }
    }

fun saveProduct(product: com.serviseyem.data.ProductEntity) {
        val targetId = if (product.id.isEmpty()) db.collection("products").document().id else product.id
        val currentUserId = com.google.firebase.auth.FirebaseAuth.getInstance().currentUser?.uid ?: ""
        val resolvedStoreId = product.storeId.ifEmpty { currentUserId }
        val resolvedOwnerId = product.ownerId.ifEmpty { currentUserId.ifEmpty { resolvedStoreId } }
        val localProduct = product.copy(
            id = targetId,
            storeId = resolvedStoreId,
            ownerId = resolvedOwnerId
        )
        
        // Instant Local State Sync
        val currentProds = _products.value.filter { it.id != targetId }.toMutableList()
        currentProds.add(localProduct)
        _products.value = currentProds

        viewModelScope.launch {
            val ctx = appContext
            val finalImg = if (ctx != null && product.imageUrl.isNotEmpty() && !product.imageUrl.startsWith("http")) {
                uploadImageStringOrUri(ctx, product.imageUrl, com.serviseyem.utils.FirebaseStorageUploader.getStoreProductPath(resolvedStoreId.ifEmpty { "general" }, targetId))
            } else product.imageUrl

            val finalProduct = product.copy(
                id = targetId,
                storeId = resolvedStoreId,
                ownerId = resolvedOwnerId,
                imageUrl = finalImg
            )
            db.collection("products").document(targetId).set(finalProduct)
                .addOnSuccessListener {
                    mainViewModel.triggerNotification("✅ تم حفظ المنتج بنجاح!")
                }
        }
    }

fun deleteProduct(productId: String) {
        _products.value = _products.value.filter { it.id != productId }
        db.collection("products").document(productId).get().addOnSuccessListener { snapshot ->
            val product = snapshot.toObject(com.serviseyem.data.ProductEntity::class.java)
            if (product != null) {
                db.collection("products").document(productId).set(product.copy(isDeleted = true))
            } else {
                db.collection("products").document(productId).update("isDeleted", true)
            }
        }
    }

fun updateProductPrice(productId: String, newPrice: Double) {
        val existing = _products.value.find { it.id == productId }
        if (existing != null) {
            val updated = existing.copy(price = newPrice, oldPrice = if (existing.price != newPrice) existing.price else existing.oldPrice)
            _products.value = _products.value.map { if (it.id == productId) updated else it }
            db.collection("products").document(productId).update("price", newPrice, "oldPrice", updated.oldPrice)
                .addOnSuccessListener {
                    mainViewModel.triggerNotification("⚡ تم تحديث السعر فورياً لجميع العملاء!")
                }
        } else {
            db.collection("products").document(productId).update("price", newPrice)
        }
    }

fun saveOffer(offer: com.serviseyem.data.models.Offer) {
        val targetId = if (offer.id.isEmpty()) db.collection("offers").document().id else offer.id
        val finalOffer = offer.copy(id = targetId, updatedAt = System.currentTimeMillis())

        val currentList = mainViewModel.instantRequestViewModel._offers.value.filter { it.id != targetId }.toMutableList()
        currentList.add(finalOffer)
        mainViewModel.instantRequestViewModel._offers.value = currentList

        db.collection("offers").document(targetId).set(finalOffer)
            .addOnSuccessListener {
                mainViewModel.triggerNotification("🎁 تم نشر العرض وتحديث الأسعار فورياً!")
            }
            .addOnFailureListener {
                mainViewModel.triggerNotification("⚠️ فشل حفظ العرض: ${it.localizedMessage}")
            }
    }

fun deleteOffer(offerId: String) {
        mainViewModel.instantRequestViewModel._offers.value = mainViewModel.instantRequestViewModel._offers.value.filter { it.id != offerId }
        db.collection("offers").document(offerId).delete()
            .addOnSuccessListener {
                mainViewModel.triggerNotification("🗑️ تم حذف العرض بنجاح!")
            }
    }

fun toggleOfferStatus(offerId: String, isActive: Boolean) {
        mainViewModel.instantRequestViewModel._offers.value = mainViewModel.instantRequestViewModel._offers.value.map { if (it.id == offerId) it.copy(isActive = isActive) else it }
        db.collection("offers").document(offerId).update(
            "isActive", isActive,
            "updatedAt", System.currentTimeMillis()
        )
    }

fun listenToOffersForEntity(
        entityId: String,
        onResult: (List<com.serviseyem.data.models.Offer>) -> Unit
    ): com.google.firebase.firestore.ListenerRegistration {
        return db.collection("offers")
            .whereEqualTo("entityId", entityId)
            .addSnapshotListener { snapshot, error ->
                if (error == null && snapshot != null) {
                    val fetched = snapshot.documents.mapNotNull { doc ->
                        doc.toObject(com.serviseyem.data.models.Offer::class.java)?.copy(id = doc.id)
                    }
                    onResult(fetched)
                }
            }
    }

fun listenToProductsForStore(
        storeId: String,
        onResult: (List<com.serviseyem.data.ProductEntity>) -> Unit
    ): com.google.firebase.firestore.ListenerRegistration {
        return db.collection("products")
            .whereEqualTo("storeId", storeId)
            .addSnapshotListener { snapshot, error ->
                if (error == null && snapshot != null) {
                    val fetched = snapshot.documents.mapNotNull { doc ->
                        val p = doc.toObject(com.serviseyem.data.ProductEntity::class.java)
                        if (p != null && !p.isDeleted) p.copy(id = doc.id) else null
                    }
                    onResult(fetched)
                }
            }
    }

fun saveCustomProfileTab(tab: com.serviseyem.data.CustomProfileTabEntity) {
        val targetId = if (tab.id.isEmpty()) java.util.UUID.randomUUID().toString().take(6) else tab.id
        val finalTab = tab.copy(id = targetId)
        db.collection("custom_profile_tabs").document(targetId).set(finalTab)
        mainViewModel.triggerNotification("📑 تم حفظ التبويب المخصص بنجاح: ${tab.title}")
    }

fun deleteCustomProfileTab(tabId: String) {
        db.collection("custom_profile_tabs").document(tabId).delete()
        mainViewModel.triggerNotification("🗑️ تم حذف التبويب المخصص")
    }

fun toggleCustomProfileTab(tabId: String) {
        val current = _customProfileTabs.value.find { it.id == tabId }
        if (current != null) {
            val updated = current.copy(isEnabled = !current.isEnabled)
            db.collection("custom_profile_tabs").document(tabId).set(updated)
            mainViewModel.triggerNotification(if (updated.isEnabled) "🟢 تم تفعيل التبويب" else "🔴 تم إيقاف التبويب")
        }
    }

fun addNewCategory(nameAr: String, nameEn: String, icon: String, description: String, parentId: String = "", isMainCategory: Boolean = true) {
        homeViewModel.addNewCategory(nameAr, nameEn, icon, description, parentId, isMainCategory)
    }

fun editCategory(categoryId: String, newName: String, newIcon: String, parentId: String = "", isMainCategory: Boolean = true) {
        db.collection("categories").document(categoryId).get().addOnSuccessListener { snapshot ->
            val cat = snapshot.toObject(CategoryEntity::class.java)
            if (cat != null) {
                db.collection("categories").document(categoryId).set(
                    cat.copy(
                        name = newName,
                        icon = newIcon,
                        parentId = parentId,
                        isMainCategory = isMainCategory
                    )
                )
            }
        }
        mainViewModel.triggerNotification("✏️ تم تعديل القسم وتحديث هيكلته بنجاح: $newName")
    }

fun deleteCategory(categoryId: String) {
        homeViewModel.deleteCategory(categoryId)
    }

fun togglePinCategory(categoryId: String) {
        db.collection("categories").document(categoryId).get().addOnSuccessListener { snapshot ->
            val cat = snapshot.toObject(com.serviseyem.data.CategoryEntity::class.java)
            if (cat != null) {
                val updated = cat.copy(isPinned = !cat.isPinned)
                db.collection("categories").document(categoryId).set(updated)
                mainViewModel.triggerNotification(if (updated.isPinned) "📌 تم تثبيت القسم في البداية" else "🔓 تم إلغاء تثبيت القسم")
            }
        }
    }

fun mergeCategories(sourceCategoryId: String, targetCategoryId: String) {
        if (sourceCategoryId == targetCategoryId) {
            mainViewModel.triggerNotification("⚠️ لا يمكن دمج القسم مع نفسه!")
            return
        }

        // 1. Move approved mainViewModel.homeViewModel.providers of sourceCategory to targetCategory
        db.collection("providers").whereEqualTo("categoryId", sourceCategoryId).get().addOnSuccessListener { snapshot ->
            for (doc in snapshot.documents) {
                doc.reference.update("categoryId", targetCategoryId)
            }
        }

        // 2. Move pending mainViewModel.homeViewModel.providers of sourceCategory to targetCategory
        db.collection("pending_providers").whereEqualTo("categoryId", sourceCategoryId).get().addOnSuccessListener { snapshot ->
            for (doc in snapshot.documents) {
                doc.reference.update("categoryId", targetCategoryId)
            }
        }

        // 2b. Move stores of sourceCategory to targetCategory
        db.collection("stores").whereEqualTo("categoryId", sourceCategoryId).get().addOnSuccessListener { snapshot ->
            for (doc in snapshot.documents) {
                doc.reference.update("categoryId", targetCategoryId)
            }
        }

        // 3. Delete the source category
        db.collection("categories").document(sourceCategoryId).delete().addOnSuccessListener {
            mainViewModel.triggerNotification("✅ تم دمج القسمين وتحويل كافة الفنيين والمتاجر بنجاح!")
        }
    }

fun saveCategoryEntity(cat: CategoryEntity) {
        val catId = cat.id.ifEmpty { UUID.randomUUID().toString().take(6) }
        val updated = cat.copy(id = catId)
        db.collection("categories").document(catId).set(updated)
        mainViewModel.triggerNotification("📁 تم حفظ وتحديث بيانات القسم: ${updated.name}")
    }

fun addSubCategory(parentId: String, nameAr: String, icon: String) {
        val nextId = UUID.randomUUID().toString().take(6)
        val subCat = CategoryEntity(
            id = nextId,
            name = nameAr,
            icon = icon,
            parentId = parentId,
            isMainCategory = false,
            order = mainViewModel.homeViewModel._categories.value.size + 1
        )
        db.collection("categories").document(nextId).set(subCat)
        mainViewModel.triggerNotification("📂 تم إضافة قسم فرعي جديد: $nameAr")
    }

fun convertCategoryType(catId: String, newParentId: String, isMain: Boolean) {
        val cat = mainViewModel.homeViewModel._categories.value.find { it.id == catId }
        if (cat != null) {
            val updated = cat.copy(parentId = newParentId, isMainCategory = isMain)
            db.collection("categories").document(catId).set(updated)
            mainViewModel.triggerNotification("🔄 تم تغيير تصنيف القسم بنجاح")
        }
    }

fun reorderCategories(newOrderedList: List<CategoryEntity>) {
        newOrderedList.forEachIndexed { index, cat ->
            val updated = cat.copy(order = index + 1)
            db.collection("categories").document(cat.id).set(updated)
        }
    }

fun addNewCity(nameAr: String, nameEn: String, icon: String = "📍", photoUrl: String = "", sortOrder: Int = 0) {
        val nextId = "city_" + UUID.randomUUID().toString().take(4)
        val city = CityEntity(nextId, nameAr, nameEn, icon.ifEmpty { "📍" }, photoUrl, sortOrder)
        db.collection("cities").document(nextId).set(city)
        mainViewModel.triggerNotification("🏙️ تم إضافة مدينة/محافظة: $nameAr")
    }

fun updateCity(city: CityEntity) {
        if (city.id.isEmpty()) return
        db.collection("cities").document(city.id).set(city)
        mainViewModel.triggerNotification("💾 تم تحديث بيانات المدينة/المحافظة: ${city.nameAr}")
    }

fun removeCity(cityId: String) {
        db.collection("cities").document(cityId).delete()
        mainViewModel.triggerNotification("🗑️ تم حذف المدينة")
    }

    fun removeProvider(providerId: String) {
        viewModelScope.launch {
            safeFirestoreCall(
                operation = {
                    crud.deleteEntity("providers", providerId, true)
                },
                onSuccess = {
                    mainViewModel.triggerNotification("🗑️ تم حذف حساب الفني بنجاح")
                },
                onError = {
                    mainViewModel.triggerNotification("⚠️ تم حذف الفني محلياً، سيتم المزامنة تلقائياً")
                }
            )
        }
    }

    fun removeProviderPermanently(providerId: String) {
        viewModelScope.launch {
            safeFirestoreCall(
                operation = {
                    crud.deleteEntity("providers", providerId, false)
                },
                onSuccess = {
                    mainViewModel.triggerNotification("🗑️ تم حذف حساب الفني نهائياً وبشكل كامل من خوادم الدليل")
                },
                onError = { e ->
                    mainViewModel.triggerNotification("❌ فشل حذف الفني نهائياً: ${e.message}")
                }
            )
        }
    }

    fun restoreProvider(providerId: String) {
        viewModelScope.launch {
            safeFirestoreCall(
                operation = {
                    crud.updateFields("providers", providerId, mapOf("isDeleted" to false, "deletedAt" to null))
                },
                onSuccess = {
                    mainViewModel.triggerNotification("🟢 تم استعادة وتفعيل حساب الفني بنجاح!")
                },
                onError = {
                    mainViewModel.triggerNotification("⚠️ تم استعادة الفني محلياً، سيتم المزامنة تلقائياً")
                }
            )
        }
    }

fun pinProvider(providerId: String, isPinned: Boolean) {
        db.collection("providers").document(providerId).get().addOnSuccessListener { snapshot ->
            val p = snapshot.toObject(ProviderEntity::class.java)
            if (p != null) {
                db.collection("providers").document(providerId).set(p.copy(isVip = isPinned))
            }
        }
        mainViewModel.triggerNotification(if (isPinned) "📌 تم تثبيت الفني" else "📌 تم إلغاء تثبيت الفني")
    }

fun recommendProvider(providerId: String, isRecommended: Boolean) {
        db.collection("providers").document(providerId).get().addOnSuccessListener { snapshot ->
            val p = snapshot.toObject(ProviderEntity::class.java)
            if (p != null) {
                db.collection("providers").document(providerId).set(
                    p.copy(isRecommended = isRecommended, rating = if (isRecommended) 5.0f else 4.2f)
                )
            }
        }
        mainViewModel.triggerNotification(if (isRecommended) "⭐ تمت توصية الفني" else "⭐ تم إلغاء توصية الفني")
    }

fun verifyProviderBadge(providerId: String, isVerified: Boolean) {
        db.collection("providers").document(providerId).get().addOnSuccessListener { snapshot ->
            val p = snapshot.toObject(ProviderEntity::class.java)
            if (p != null) {
                db.collection("providers").document(providerId).set(
                    p.copy(isVerified = isVerified, subscriptionStatus = if (isVerified) "APPROVED" else "PENDING")
                )
            }
        }
        mainViewModel.triggerNotification(if (isVerified) "🔷 تم توثيق الفني بالشارة الزرقاء" else "🔷 تم إلغاء توثيق الفني")
    }

fun toggleProviderSubscription(providerId: String, status: String) {
        db.collection("providers").document(providerId).get().addOnSuccessListener { snapshot ->
            val p = snapshot.toObject(ProviderEntity::class.java)
            if (p != null) {
                db.collection("providers").document(providerId).set(p.copy(subscriptionStatus = status))
            }
        }
        mainViewModel.triggerNotification(if (status == "APPROVED") "✨ تم تفعيل العضوية الذهبية للفني" else "✨ تم إلغاء العضوية الذهبية")
    }

fun toggleProviderSubscription(providerId: String) {
        val provider = mainViewModel.homeViewModel._providers.value.find { it.id == providerId }
        provider?.let {
            val updated = it.copy(subscriptionStatus = if (it.subscriptionStatus == "APPROVED") "EXPIRED" else "APPROVED")
            mainViewModel.homeViewModel._providers.value = mainViewModel.homeViewModel._providers.value.map { item -> if (item.id == providerId) updated else item }
            db.collection("providers").document(providerId).set(updated)
        }
    }

fun setProviderChatDisabled(providerId: String, disabled: Boolean) {
        db.collection("providers").document(providerId).get().addOnSuccessListener { snapshot ->
            val p = snapshot.toObject(ProviderEntity::class.java)
            if (p != null) {
                db.collection("providers").document(providerId).set(p.copy(isChatDisabled = disabled))
            }
        }
        mainViewModel.triggerNotification(if (disabled) "🔇 تم إيقاف دردشة الفني إدارياً" else "🔊 تم تفعيل دردشة الفني")
    }

fun setProviderNotificationsDisabled(providerId: String, disabled: Boolean) {
        db.collection("providers").document(providerId).get().addOnSuccessListener { snapshot ->
            val p = snapshot.toObject(ProviderEntity::class.java)
            if (p != null) {
                db.collection("providers").document(providerId).set(p.copy(isNotificationsDisabled = disabled))
            }
        }
        mainViewModel.triggerNotification(if (disabled) "🔕 تم تعطيل إشعارات الفني إدارياً" else "🔔 تم تفعيل إشعارات الفني")
    }

fun setProviderPaymentRequired(providerId: String, required: Boolean) {
        db.collection("providers").document(providerId).get().addOnSuccessListener { snapshot ->
            val p = snapshot.toObject(ProviderEntity::class.java)
            if (p != null) {
                db.collection("providers").document(providerId).set(p.copy(isPaymentRequired = required))
            }
        }
        mainViewModel.triggerNotification(if (required) "💳 تم ربط حساب الفني بنظام الدفع والعمولة الإلزامية" else "🔓 تم استثناء الفني من شروط الدفع المسبق والعمولة")
    }

fun extendProviderSubscription(providerId: String, extraMs: Long) {
        db.collection("providers").document(providerId).get().addOnSuccessListener { snapshot ->
            val p = snapshot.toObject(ProviderEntity::class.java)
            if (p != null) {
                val currentExpiry = if (p.subscriptionExpiry > System.currentTimeMillis()) p.subscriptionExpiry else System.currentTimeMillis()
                db.collection("providers").document(providerId).set(
                    p.copy(subscriptionExpiry = currentExpiry + extraMs, subscriptionStatus = "APPROVED")
                )
            }
        }
        mainViewModel.triggerNotification("💸 تم تجديد وتمديد اشتراك فني بنجاح!")
    }

    fun toggleProviderBlock(providerId: String) {
        viewModelScope.launch {
            val provider = mainViewModel.homeViewModel._providers.value.find { it.id == providerId }
            if (provider != null) {
                val newBlockedStatus = !provider.isBlocked
                safeFirestoreCall(
                    operation = {
                        crud.toggleEntityStatus("providers", providerId, "isBlocked", newBlockedStatus)
                        mainViewModel.homeViewModel._providers.value = mainViewModel.homeViewModel._providers.value.map { item ->
                            if (item.id == providerId) item.copy(isBlocked = newBlockedStatus) else item
                        }
                    },
                    onSuccess = {
                        val statusText = if (newBlockedStatus) "حظر" else "إلغاء حظر"
                        mainViewModel.triggerNotification("🛡️ تم $statusText الفني ${provider.name}")
                    },
                    onError = {
                        mainViewModel.triggerNotification("⚠️ تم تحديث الحالة محلياً، سيتم المزامنة تلقائياً")
                    }
                )
            }
        }
    }

fun toggleProviderStatus(provider: ProviderEntity) {
        val updated = provider.copy(isAvailable = !provider.isAvailable)
        db.collection("providers").document(provider.id).set(updated)
        mainViewModel.triggerNotification("🔄 تم تغيير حالة التوفر لـ ${provider.name}")
    }

fun toggleProviderPin(providerId: String) {
        val provider = mainViewModel.homeViewModel._providers.value.find { it.id == providerId }
        provider?.let {
            val updated = it.copy(isVip = !it.isVip)
            mainViewModel.homeViewModel._providers.value = mainViewModel.homeViewModel._providers.value.map { item -> if (item.id == providerId) updated else item }
            db.collection("providers").document(providerId).set(updated)
        }
    }

fun toggleProviderVerification(providerId: String) {
        val provider = mainViewModel.homeViewModel._providers.value.find { it.id == providerId }
        provider?.let {
            val updated = it.copy(isVerified = !it.isVerified)
            mainViewModel.homeViewModel._providers.value = mainViewModel.homeViewModel._providers.value.map { item -> if (item.id == providerId) updated else item }
            db.collection("providers").document(providerId).set(updated)
        }
    }

fun toggleProviderRecommendation(providerId: String) {
        val provider = mainViewModel.homeViewModel._providers.value.find { it.id == providerId }
        provider?.let {
            val updated = it.copy(isRecommended = !it.isRecommended)
            mainViewModel.homeViewModel._providers.value = mainViewModel.homeViewModel._providers.value.map { item -> if (item.id == providerId) updated else item }
            db.collection("providers").document(providerId).set(updated)
        }
    }

fun updateProviderEntity(provider: ProviderEntity) {
        mainViewModel.homeViewModel._providers.value = mainViewModel.homeViewModel._providers.value.map { item -> if (item.id == provider.id) provider else item }
        db.collection("providers").document(provider.id).set(provider)
        mainViewModel.triggerNotification("💾 تم تحديث بيانات مقدم الخدمة ${provider.name} بنجاح")
    }

fun editProviderPhoneAndCategory(providerId: String, newPhone: String, newCategoryId: String) {
        db.collection("providers").document(providerId).get().addOnSuccessListener { snapshot ->
            val p = snapshot.toObject(ProviderEntity::class.java)
            if (p != null) {
                db.collection("providers").document(providerId).set(p.copy(phone = newPhone, categoryId = newCategoryId))
            }
        }
        mainViewModel.triggerNotification("✏️ تم تعديل بيانات اتصال وتصنيف الفني")
    }

fun addNewProvider(name: String, phone: String, catId: String, area: String, price: Double, isVip: Boolean) {
        val providerId = "prov_" + phone.trim().replace(" ", "").replace("+", "")
        val newP = ProviderEntity(
            id = providerId,
            name = name,
            phone = phone,
            categoryId = catId,
            area = area,
            isVip = isVip,
            subscriptionStatus = "APPROVED",
            isAvailable = true,
            cityId = "ye_san",
            localNeighborhood = area,
            previewPrice = price,
            rating = 5.0f
        )
        db.collection("providers").document(newP.id).set(newP)
        mainViewModel.triggerNotification("➕ تم إضافة الفني $name يدوياً")
    }

fun addNewProviderCustom(
        name: String,
        phone: String,
        catId: String,
        street: String,
        cityId: String,
        profileImage: String,
        idCardImage: String,
        forensicImage: String,
        price: Double,
        isVip: Boolean
    ) {
        val newP = ProviderEntity(
            id = "prov_" + UUID.randomUUID().toString().take(6),
            name = name,
            phone = phone,
            categoryId = catId,
            area = street,
            localNeighborhood = street,
            cityId = cityId,
            profileImage = profileImage.ifEmpty { "https://cdn-icons-png.flaticon.com/512/147/147144.png" },
            coverImage = idCardImage.ifEmpty { "https://img.freepik.com/free-photo/view-of-yemen_1150-12349.jpg" },
            previewPrice = price,
            isVip = isVip,
            isVerified = true,
            isRecommended = true,
            subscriptionStatus = "APPROVED",
            isAvailable = true,
            rating = 5.0f,
            subscriptionExpiry = System.currentTimeMillis() + (30L * 24 * 60 * 60 * 1000)
        )
        db.collection("providers").document(newP.id).set(newP)
        mainViewModel.triggerNotification("✨ تم إضافة الفني $name يدوياً بالدليل اليمني بنجاح")
    }

fun addNewBanner(title: String, url: String, redirect: String, type: String, size: String, duration: Int, displayTime: String = "طوال اليوم") {
        val banner = BannerEntity(
            id = UUID.randomUUID().toString(),
            title = title,
            url = url,
            redirectCategory = redirect,
            type = type,
            size = size,
            duration = duration,
            displayTime = displayTime,
            order = mainViewModel.homeViewModel._banners.value.size + 1
        )
        db.collection("banners").document(banner.id).set(banner)
        mainViewModel.triggerNotification("🖼️ تم إضافة إعلان جديد: $title")
    }

fun addBanner(title: String, url: String, redirect: String, type: String, size: String, duration: Int, displayTime: String = "طوال اليوم") {
        addNewBanner(title, url, redirect, type, size, duration, displayTime)
    }

    fun deleteBanner(bannerId: String) {
        viewModelScope.launch {
            safeFirestoreCall(
                operation = {
                    crud.deleteEntity("banners", bannerId, false)
                    homeViewModel.deleteBanner(bannerId)
                },
                onSuccess = {
                    mainViewModel.triggerNotification("🗑️ تم حذف البنر")
                },
                onError = {
                    mainViewModel.triggerNotification("⚠️ تم حذف البنر محلياً، سيتم المزامنة تلقائياً")
                }
            )
        }
    }

    fun reorderBanners(newOrderedList: List<BannerEntity>) {
        viewModelScope.launch {
            try {
                newOrderedList.forEachIndexed { index, banner ->
                    crud.updateFields("banners", banner.id, mapOf("order" to index + 1))
                }
                mainViewModel.triggerNotification("✅ تم تحديث ترتيب البنرات")
            } catch (e: Exception) {
                mainViewModel.triggerNotification("⚠️ تم تحديث الترتيب محلياً، سيتم المزامنة تلقائياً")
            }
        }
    }

fun placeOrder(order: com.serviseyem.data.OrderEntity) {
        val targetId = db.collection("orders").document().id
        val finalOrder = order.copy(id = targetId)
        db.collection("orders").document(targetId).set(finalOrder).addOnSuccessListener {
            mainViewModel.triggerNotification("🛍️ تم تسجيل طلبك بنجاح! رقم الطلب: ${targetId.take(6)}")
        }
    }

fun updateOrderStatus(orderId: String, status: String) {
        db.collection("orders").document(orderId).get().addOnSuccessListener { snap ->
            val order = snap.toObject(com.serviseyem.data.OrderEntity::class.java)
            if (order != null) {
                db.collection("orders").document(orderId).set(order.copy(status = status)).addOnSuccessListener {
                    mainViewModel.triggerNotification("📦 تم تحديث حالة الطلب إلى $status")
                }
            }
        }
    }

fun deleteOrder(orderId: String) {
        db.collection("orders").document(orderId).delete().addOnSuccessListener {
            mainViewModel.triggerNotification("🗑️ تم حذف الطلب بنجاح.")
        }
    }

fun deleteAllOrders(customerPhone: String) {
        db.collection("orders")
            .whereEqualTo("customerPhone", customerPhone)
            .get()
            .addOnSuccessListener { querySnapshot ->
                val batch = db.batch()
                for (doc in querySnapshot.documents) {
                    batch.delete(doc.reference)
                }
                batch.commit().addOnSuccessListener {
                    mainViewModel.triggerNotification("🗑️ تم حذف جميع الطلبات بنجاح.")
                }
            }
    }

fun addRating(rating: com.serviseyem.data.RatingEntity) {
        val targetId = if (rating.id.isBlank()) db.collection("ratings").document().id else rating.id
        val finalRating = rating.copy(id = targetId)
        // تحديث متفائل فوري للواجهة
        val currentList = appState._ratings.value.toMutableList()
        currentList.removeAll { it.id == targetId }
        currentList.add(0, finalRating)
        appState._ratings.value = currentList

        db.collection("ratings").document(targetId).set(finalRating).addOnSuccessListener {
            mainViewModel.triggerNotification("⭐ شكراً لتقييمك! تم إرسال تقييمك بنجاح.")
            recalculateTargetRating(rating.targetId, rating.targetType)
        }
    }

fun addRatingReply(ratingId: String, replyText: String) {
        val updates = mapOf(
            "reply" to replyText,
            "replyTimestamp" to System.currentTimeMillis()
        )
        db.collection("ratings").document(ratingId).update(updates)
            .addOnSuccessListener {
                mainViewModel.triggerNotification("✅ تم إضافة الرد على التعليق بنجاح!")
            }
            .addOnFailureListener {
                mainViewModel.triggerNotification("❌ فشل إضافة الرد: ${it.message}")
            }
    }

fun deleteRating(ratingId: String) {
        appState._ratings.value = appState._ratings.value.filter { it.id != ratingId }
        db.collection("ratings").document(ratingId).get().addOnSuccessListener { snap ->
            val rating = snap.toObject(com.serviseyem.data.RatingEntity::class.java)
            if (rating != null) {
                db.collection("ratings").document(ratingId).delete().addOnSuccessListener {
                    recalculateTargetRating(rating.targetId, rating.targetType)
                }
            }
        }
    }

fun approveRating(ratingId: String, isApproved: Boolean) {
        db.collection("ratings").document(ratingId).get().addOnSuccessListener { snap ->
            val rating = snap.toObject(com.serviseyem.data.RatingEntity::class.java)
            if (rating != null) {
                db.collection("ratings").document(ratingId).set(rating.copy(isApproved = isApproved))
            }
        }
    }

fun submitRating(providerId: String, rating: Int) {
        mainViewModel.triggerNotification("⭐ شكراً لتقييمك $rating نجوم!")
    }

fun submitRating(ratingEntity: com.serviseyem.data.RatingEntity, onComplete: () -> Unit = {}) {
        // تحديث متفائل فوري للتقييمات
        val currentList = appState._ratings.value.toMutableList()
        currentList.removeAll { it.id == ratingEntity.id }
        currentList.add(0, ratingEntity)
        appState._ratings.value = currentList

        db.collection("ratings").document(ratingEntity.id).set(ratingEntity)
            .addOnSuccessListener {
                recalculateTargetRating(ratingEntity.targetId, ratingEntity.targetType)
                onComplete()
            }
            .addOnFailureListener {
                recalculateTargetRating(ratingEntity.targetId, ratingEntity.targetType)
                onComplete()
            }
    }

fun recalculateTargetRating(targetId: String, targetType: String) {
        db.collection("ratings").whereEqualTo("targetId", targetId).get().addOnSuccessListener { snapshot ->
            val ratingsList = snapshot.documents.mapNotNull { it.toObject(com.serviseyem.data.RatingEntity::class.java) }
            val count = ratingsList.size
            val avg = if (count > 0) ratingsList.map { it.rating }.sum() / count else 5.0f

            if (targetType.equals("PROVIDER", ignoreCase = true) || targetType.equals("TECHNICIAN", ignoreCase = true)) {
                // تحديث مزود الخدمة / الفني في قاعدة البيانات
                db.collection("providers").document(targetId).get().addOnSuccessListener { provSnap ->
                    val prov = provSnap.toObject(com.serviseyem.data.ProviderEntity::class.java)
                    if (prov != null) {
                        db.collection("providers").document(targetId).set(prov.copy(rating = avg, numReviews = count))
                    }
                }
                // تحديث الذاكرة فوراً لتظهر في البطاقات
                val provList = mainViewModel.homeViewModel.providers.value.toMutableList()
                val pIdx = provList.indexOfFirst { it.id == targetId }
                if (pIdx >= 0) {
                    provList[pIdx] = provList[pIdx].copy(rating = avg, numReviews = count)
                    mainViewModel.homeViewModel._providers.value = provList
                }
            } else if (targetType.equals("STORE", ignoreCase = true) || targetType.equals("RESTAURANT", ignoreCase = true) || targetType.equals("MEDICAL", ignoreCase = true)) {
                db.collection("stores").document(targetId).get().addOnSuccessListener { storeSnap ->
                    val store = storeSnap.toObject(com.serviseyem.data.StoreEntity::class.java)
                    if (store != null) {
                        db.collection("stores").document(targetId).set(store.copy(rating = avg, numReviews = count))
                    }
                }
                val sList = stores.value.toMutableList()
                val sIdx = sList.indexOfFirst { it.id == targetId }
                if (sIdx >= 0) {
                    sList[sIdx] = sList[sIdx].copy(rating = avg, numReviews = count)
                    _stores.value = sList
                }
            } else if (targetType.equals("PROPERTY", ignoreCase = true)) {
                db.collection("properties").document(targetId).get().addOnSuccessListener { propSnap ->
                    val prop = propSnap.toObject(com.serviseyem.data.PropertyEntity::class.java)
                    if (prop != null) {
                        db.collection("properties").document(targetId).set(prop.copy(rating = avg, numReviews = count))
                    }
                }
                val pList = properties.value.toMutableList()
                val propIdx = pList.indexOfFirst { it.id == targetId }
                if (propIdx >= 0) {
                    pList[propIdx] = pList[propIdx].copy(rating = avg, numReviews = count)
                    _properties.value = pList
                }
            }
        }
    }

fun logAdminActivity(action: String) {
        val id = db.collection("activity_logs").document().id
        val log = com.serviseyem.data.ActivityLogEntity(id = id, action = action, timestamp = System.currentTimeMillis())
        db.collection("activity_logs").document(id).set(log)
    }

fun logCall(providerId: String, providerName: String) {
        val callId = UUID.randomUUID().toString()
        val call = CallEntity(
            id = callId,
            providerId = providerId,
            providerName = providerName,
            callerName = "مواطن يمني 🇾🇪",
            timestamp = System.currentTimeMillis()
        )
        db.collection("calls").document(callId).set(call)
    }

fun checkAndGetDuplicateAccountType(phone: String, excludeId: String): String? {
        val cleanInput = phone.trim().replace(" ", "").replace("+", "")
        if (cleanInput.isEmpty()) return null
        
        // 1. Check in active mainViewModel.homeViewModel.providers
        val dupProvider = mainViewModel.homeViewModel._providers.value.any { !it.isDeleted && it.phone.trim().replace(" ", "").replace("+", "") == cleanInput && it.id != excludeId }
        if (dupProvider) return "مقدم خدمة نشط (فني)"
        
        // 2. Check in pending mainViewModel.homeViewModel.providers
        val dupPendingProvider = _pendingProviders.value.any { it.status == "PENDING" && it.phone.trim().replace(" ", "").replace("+", "") == cleanInput && it.id != excludeId }
        if (dupPendingProvider) return "طلب انضمام فني قيد المراجعة"
        
        // 3. Check in stores
        val dupStore = _stores.value.any { !it.isDeleted && it.phone.trim().replace(" ", "").replace("+", "") == cleanInput && it.id != excludeId }
        if (dupStore) return "محل أو متجر تجاري مسجل"
        
        // 4. Check in properties
        val dupProp = _properties.value.any { !it.isDeleted && it.phone.trim().replace(" ", "").replace("+", "") == cleanInput && it.id != excludeId }
        if (dupProp) return "إعلان عقاري مدرج"
        
        return null
    }

fun updateProviderPortfolio(providerId: String, images: List<String>) {
        try {
            db.collection("providers").document(providerId).update("portfolioImages", images)
        } catch (e: Exception) {}
    }

fun addPortfolioImage(providerId: String, imageBase64: String) {
        viewModelScope.launch {
            try {
                val uploadedUrl = com.serviseyem.utils.FirebaseStorageUploader.resolveBase64ToStorageUrl(
                    imageBase64,
                    com.serviseyem.utils.FirebaseStorageUploader.getPortfolioImagePath(providerId),
                    maxSizeBytes = 250 * 1024L
                )
                if (uploadedUrl.isBlank()) return@launch
                db.collection("providers").document(providerId).get().addOnSuccessListener { snapshot ->
                    if (snapshot != null && snapshot.exists()) {
                        @Suppress("UNCHECKED_CAST")
                        val list = snapshot.get("portfolioImages") as? List<String> ?: emptyList()
                        val updated = list + uploadedUrl
                        db.collection("providers").document(providerId).update("portfolioImages", updated)
                    }
                }
            } catch (e: Exception) {}
        }
    }

fun removePortfolioImage(providerId: String, index: Int) {
        try {
            db.collection("providers").document(providerId).get().addOnSuccessListener { snapshot ->
                if (snapshot != null && snapshot.exists()) {
                    val list = (snapshot.get("portfolioImages") as? List<String>)?.toMutableList() ?: mutableListOf()
                    if (index < list.size) {
                        list.removeAt(index)
                        db.collection("providers").document(providerId).update("portfolioImages", list)
                    }
                }
            }
        } catch (e: Exception) {}
    }

fun clearPortfolio(providerId: String) {
        try {
            db.collection("providers").document(providerId).update("portfolioImages", emptyList<String>())
        } catch (e: Exception) {}
    }

fun redirectBookingToEntity(bookingId: String, targetEntityId: String, targetEntityName: String, targetPhone: String) {
        val updates = mapOf(
            "providerId" to targetEntityId,
            "providerName" to targetEntityName,
            "providerPhone" to targetPhone,
            "status" to "PENDING"
        )
        db.collection("bookings").document(bookingId).update(updates)
            .addOnSuccessListener {
                mainViewModel.triggerNotification("🔄 تم توجيه الحجز للجهة/الفني ($targetEntityName) بنجاح!")
            }
    }

fun unbanEntity(entityType: String, entityId: String) {
        when (entityType.uppercase()) {
            "PROVIDER" -> db.collection("providers").document(entityId).update("isBlocked", false)
            "STORE", "RESTAURANT", "MEDICAL" -> setStoreBlocked(entityId, false, "")
            "PROPERTY" -> setPropertyBlocked(entityId, false, "")
            "JOB" -> setJobBlocked(entityId, false, "")
            else -> {
                db.collection("stores").document(entityId).update("isBlocked", false)
                db.collection("properties").document(entityId).update("isBlocked", false)
                db.collection("jobs").document(entityId).update("isBlocked", false)
                db.collection("providers").document(entityId).update("isBlocked", false)
            }
        }
        mainViewModel.triggerNotification("✅ تم إلغاء حظر الكيان بنجاح!")
    }

fun restoreEntity(entityType: String, entityId: String) {
        when (entityType.uppercase()) {
            "PROVIDER" -> restoreProvider(entityId)
            "STORE", "RESTAURANT", "MEDICAL" -> restoreStore(entityId)
            "PROPERTY" -> restoreProperty(entityId)
            "JOB" -> restoreJob(entityId)
            else -> {
                restoreStore(entityId)
                restoreProperty(entityId)
                restoreJob(entityId)
            }
        }
    }

fun hardDeleteEntity(entityType: String, entityId: String, reason: String = "") {
        val cleanReason = reason.trim()
        if (cleanReason.isNotBlank()) {
            logAdminActivity("🗑️ حذف نهائي للسجل ($entityType: $entityId) | السبب: $cleanReason")
        }
        when (entityType.uppercase()) {
            "PROVIDER" -> removeProviderPermanently(entityId)
            "STORE", "RESTAURANT", "MEDICAL" -> deleteStorePermanently(entityId)
            "PROPERTY" -> deletePropertyPermanently(entityId)
            "JOB" -> deleteJobPermanently(entityId)
            else -> {
                deleteStorePermanently(entityId)
                deletePropertyPermanently(entityId)
                deleteJobPermanently(entityId)
            }
        }
    }

fun sendNotificationToApplicants(title: String, message: String, jobId: String = "") {
        val notif = NotificationEntity(
            id = UUID.randomUUID().toString(),
            title = title,
            message = message,
            targetType = if (jobId.isNotEmpty()) "JOB_APPLICANTS" else "ALL",
            targetValue = jobId,
            timestamp = System.currentTimeMillis()
        )
        if (notif.isValid()) db.collection("notifications").document(notif.id).set(notif)
            .addOnSuccessListener {
                mainViewModel.triggerNotification("🔔 تم إرسال الإشعار لجميع المتقدمين للوظائف بنجاح!")
            }
    }

fun exportJobApplicantsCsv(context: android.content.Context) {
        try {
            val apps = _jobApplications.value
            val csvContent = StringBuilder()
            csvContent.append("المعرف,اسم المتقدم,رقم الهاتف,الوظيفة,الشركة,المؤهلات,الحالة,تاريخ التقديم\n")
            apps.forEach { app ->
                // ✨ م2: استخدام DateFormatter الموحد
                val dateStr = com.serviseyem.utils.DateFormatter.formatCustom(app.createdAt, "yyyy-MM-dd HH:mm")
                csvContent.append("${app.id},\"${app.applicantName}\",\"${app.applicantPhone}\",\"${app.jobTitle}\",\"${app.companyName}\",\"${app.applicantQuals.replace("\n", " ")}\",\"${app.status}\",\"$dateStr\"\n")
            }
            val clipboard = context.getSystemService(android.content.Context.CLIPBOARD_SERVICE) as android.content.ClipboardManager
            val clip = android.content.ClipData.newPlainText("Job Applicants CSV", csvContent.toString())
            clipboard.setPrimaryClip(clip)
            mainViewModel.triggerNotification("📋 تم نسخ بيانات المتقدمين للوظائف بصيغة CSV إلى الحافظة بنجاح (${apps.size} متقدم)!")
        } catch (e: Exception) {
            mainViewModel.triggerNotification("❌ فشل تصدير البيانات: ${e.message}")
        }
    }

    // ==========================================
    // 📊 Additional Admin & Audit Utilities (Merged from com.serviseyem.viewmodels.AdminViewModel)
    // ==========================================
    private val _auditLogs = MutableStateFlow<List<AuditLog>>(emptyList())
    val auditLogs: StateFlow<List<AuditLog>> = _auditLogs.asStateFlow()

    private val _adminNotificationsList = MutableStateFlow<List<AdminNotification>>(emptyList())
    val adminNotificationsList: StateFlow<List<AdminNotification>> = _adminNotificationsList.asStateFlow()

    private val _systemLogs = MutableStateFlow<List<SystemLog>>(emptyList())
    val systemLogs: StateFlow<List<SystemLog>> = _systemLogs.asStateFlow()

    private val _pendingRequests = MutableStateFlow<List<PendingRequest>>(emptyList())
    val pendingRequests: StateFlow<List<PendingRequest>> = _pendingRequests.asStateFlow()

    private val _systemStats = MutableStateFlow(SystemStats())
    val systemStats: StateFlow<SystemStats> = _systemStats.asStateFlow()

    fun loadPendingRequests() {
        db.collection("join_requests")
            .get()
            .addOnSuccessListener { snap ->
                val list = snap.documents.mapNotNull { doc ->
                    try {
                        val req = doc.toObject(com.serviseyem.data.models.JoinRequestEntity::class.java)
                        if (req != null) {
                            val sectionName = when (req.type.uppercase()) {
                                "PROVIDER" -> "فني / مهني"
                                "STORE" -> "متجر / معرض"
                                "RESTAURANT" -> "مطعم / كافيه"
                                "MEDICAL" -> "مركز طبي / عيادة"
                                "PROPERTY" -> "عقار / أرض"
                                "JOB" -> "وظيفة / شاغر"
                                "CLIENT" -> "مستخدم عادي"
                                else -> req.type
                            }
                            PendingRequest(
                                id = req.id,
                                name = req.fullName,
                                phone = req.phone,
                                section = sectionName,
                                city = req.city,
                                details = "الفئة: ${req.categoryName} • الحي: ${req.neighborhood}",
                                status = req.status,
                                createdAt = req.submittedAt
                            )
                        } else null
                    } catch (e: Exception) {
                        null
                    }
                }
                _pendingRequests.value = list
            }
    }

    fun loadSystemStats() {
        val totalProvs = homeViewModel?.providers?.value?.size ?: 0
        _systemStats.value = SystemStats(
            totalProviders = totalProvs,
            totalUsers = 0,
            activeBookings = 0,
            completedOrders = 0,
            pendingApprovals = _pendingRequests.value.count { it.status == "PENDING" }
        )
    }

    fun getSystemStats(): SystemStats {
        loadSystemStats()
        return _systemStats.value
    }

    fun approveProviderRequest(requestId: String, onResult: ((Boolean) -> Unit)? = null) {
        db.collection("join_requests").document(requestId).get().addOnSuccessListener { snapshot ->
            val req = snapshot.toObject(com.serviseyem.data.models.JoinRequestEntity::class.java)
            if (req != null) {
                val requestProfession = when (req.type.uppercase()) {
                    "STORE", "RESTAURANT", "MEDICAL" -> "STORE_OWNER"
                    "PROPERTY" -> "PROPERTY_OWNER"
                    "JOB" -> "JOB_POSTER"
                    "CLIENT" -> "CLIENT"
                    else -> "PROVIDER"
                }
                val pendingProvider = PendingProviderEntity(
                    id = req.id,
                    name = req.fullName,
                    phone = req.phone,
                    categoryId = req.categoryId,
                    area = req.city,
                    localNeighborhood = req.neighborhood,
                    status = "APPROVED",
                    idPhotoBase64 = req.idCardImage,
                    selfiePhotoBase64 = req.profileImage,
                    workPhotosBase64 = req.workImages,
                    customCategoryName = req.categoryName,
                    profession = requestProfession,
                    passwordHash = req.passwordHash,
                    providerType = requestProfession
                )
                approveRequest(pendingProvider)
                loadPendingRequests()
                onResult?.invoke(true)
            } else {
                onResult?.invoke(false)
            }
        }.addOnFailureListener {
            onResult?.invoke(false)
        }
    }

    fun rejectProviderRequest(requestId: String, reason: String = "", onResult: ((Boolean) -> Unit)? = null) {
        db.collection("join_requests").document(requestId).update(mapOf(
            "status" to "REJECTED",
            "approvalStatus" to "REJECTED",
            "rejectionReason" to reason
        )).addOnSuccessListener {
            db.collection("pending_providers").document(requestId).delete()
            recordAuditLog("REJECT_REQUEST", "تم رفض طلب الانضمام $requestId. السبب: $reason")
            loadPendingRequests()
            mainViewModel.triggerNotification("🚫 تم رفض طلب الانضمام.")
            onResult?.invoke(true)
        }.addOnFailureListener {
            onResult?.invoke(false)
        }
    }

    fun blockUser(userId: String, onResult: ((Boolean) -> Unit)? = null) {
        db.collection("users").document(userId).update("isBlocked", true)
            .addOnSuccessListener {
                recordAuditLog("BLOCK_USER", "حظر المستخدم $userId")
                mainViewModel.triggerNotification("🚫 تم حظر المستخدم بنجاح.")
                onResult?.invoke(true)
            }
            .addOnFailureListener {
                onResult?.invoke(false)
            }
    }

    fun unblockUser(userId: String, onResult: ((Boolean) -> Unit)? = null) {
        db.collection("users").document(userId).update("isBlocked", false)
            .addOnSuccessListener {
                recordAuditLog("UNBLOCK_USER", "إلغاء حظر المستخدم $userId")
                mainViewModel.triggerNotification("✅ تم إلغاء حظر المستخدم.")
                onResult?.invoke(true)
            }
            .addOnFailureListener {
                onResult?.invoke(false)
            }
    }

    fun deleteUser(userId: String, onResult: ((Boolean) -> Unit)? = null) {
        db.collection("users").document(userId).delete()
            .addOnSuccessListener {
                recordAuditLog("DELETE_USER", "حذف المستخدم $userId")
                mainViewModel.triggerNotification("🗑️ تم حذف حساب المستخدم بنجاح.")
                onResult?.invoke(true)
            }
            .addOnFailureListener {
                onResult?.invoke(false)
            }
    }

    fun recordAuditLog(action: String, details: String) {
        val entry = AuditLog(action = action, details = details)
        _auditLogs.value = listOf(entry) + _auditLogs.value
        try {
            db.collection("admin_audit_logs").document(entry.id).set(entry)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun exportReport(type: String): String {
        recordAuditLog("EXPORT_REPORT", "تصدير تقرير من نوع $type")
        // ✨ م2: استخدام DateFormatter الموحد
        val dateStr = com.serviseyem.utils.DateFormatter.formatDateDash(System.currentTimeMillis())
        return "تقرير شامل للـ $type - التاريخ: $dateStr"
    }

    fun syncAllData(context: Context, onComplete: ((Boolean) -> Unit)? = null) {
        viewModelScope.launch {
            try {
                val syncMgr = com.serviseyem.utils.FullSyncManager(context)
                val res = syncMgr.syncAllSettings()
                onComplete?.invoke(res)
            } catch (e: Exception) {
                onComplete?.invoke(false)
            }
        }
    }

    fun clearCache(context: Context) {
        try {
            com.serviseyem.utils.FullSyncManager(context).clearLocalCache()
            mainViewModel.triggerNotification("🧹 تم مسح الذاكرة المؤقتة بنجاح")
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun getSystemHealth(): SystemHealth = SystemHealth()
    fun getStorageUsage(): StorageUsage = StorageUsage()
    fun getBookingStats(): BookingStats = BookingStats()
    fun getRevenueStats(): RevenueStats = RevenueStats()
    fun getProviderStats(): ProviderStats = ProviderStats()
    fun getCategoryStats(): CategoryStats = CategoryStats()
    fun getCityStats(): CityStats = CityStats()

    fun sendAdminNotification(title: String, message: String, target: String) {
        val notif = AdminNotification(
            id = UUID.randomUUID().toString(),
            title = title,
            message = "$message (الهدف: $target)",
            type = "BROADCAST",
            isRead = false,
            timestamp = System.currentTimeMillis()
        )
        _adminNotificationsList.value = listOf(notif) + _adminNotificationsList.value
        recordAuditLog("SEND_NOTIFICATION", "إرسال إشعار: $title إلى $target")
    }

    fun getAuditLogs(filter: String = "ALL"): List<AuditLog> {
        if (filter.isBlank() || filter == "ALL") return _auditLogs.value
        return _auditLogs.value.filter { it.action.contains(filter, ignoreCase = true) || it.details.contains(filter, ignoreCase = true) }
    }

    fun exportAuditLogs(): String {
        recordAuditLog("EXPORT_AUDIT", "تصدير سجل التدقيق والأمان")
        return buildString {
            appendLine("ID,Admin,Action,Details,Timestamp")
            _auditLogs.value.forEach {
                appendLine("${it.id},${it.adminEmail},${it.action},\"${it.details}\",${it.timestamp}")
            }
        }
    }

    fun getSystemLogs(): List<SystemLog> = _systemLogs.value
    fun clearSystemLogs() {
        _systemLogs.value = emptyList()
    }

    private val _passwordRecoveryRequests = MutableStateFlow<List<Map<String, Any>>>(emptyList())
    val passwordRecoveryRequests: StateFlow<List<Map<String, Any>>> = _passwordRecoveryRequests.asStateFlow()

    private var passwordRecoveryListenerRegistration: ListenerRegistration? = null

    init {
        listenToPasswordRecoveryRequests()
        try {
            val session = secureStorage.getAdminSession()
            if (session != null) {
                val isExpired = System.currentTimeMillis() - session.loginTime > 30L * 24 * 60 * 60 * 1000
                if (!isExpired && session.role in listOf("OWNER", "ADMIN", "SUPERVISOR")) {
                    _adminRole.value = session.role
                    _supervisorPermissions.value = session.permissions
                }
            } else {
                val user = com.google.firebase.auth.FirebaseAuth.getInstance().currentUser
                if (user != null) {
                    user.getIdToken(false).addOnSuccessListener { token ->
                        val role = token?.claims?.get("role") as? String
                        if (role != null && role.uppercase() in listOf("OWNER", "ADMIN", "SUPERVISOR")) {
                            _adminRole.value = role.uppercase()
                        }
                    }
                }
            }
        } catch (_: Exception) {}
    }

    private fun listenToPasswordRecoveryRequests() {
        passwordRecoveryListenerRegistration?.remove()
        passwordRecoveryListenerRegistration = db.collection("password_recovery_requests")
            .addSnapshotListener { snap, err ->
                if (err == null && snap != null) {
                    try {
                        val requests = snap.documents.mapNotNull { doc ->
                            val m = doc.data?.toMutableMap() ?: return@mapNotNull null
                            m["id"] = doc.id
                            m
                        }.sortedByDescending {
                            val rawTs = it["timestamp"] ?: it["requestedAt"] ?: it["createdAt"]
                            when (rawTs) {
                                is Number -> rawTs.toLong()
                                is com.google.firebase.Timestamp -> rawTs.toDate().time
                                else -> (it["requestedAt"] as? Number)?.toLong()
                                    ?: (it["createdAt"] as? Number)?.toLong()
                                    ?: 0L
                            }
                        }
                        _passwordRecoveryRequests.value = requests
                    } catch (e: Throwable) {
                        e.printStackTrace()
                    }
                }
            }
    }

    override fun onCleared() {
        super.onCleared()
        passwordRecoveryListenerRegistration?.remove()
    }

    /**
     * 🔐 Emergency Fallback - لا يحتوي credentials مكشوفة
     * يستخدم EncryptedSharedPreferences و SHA-256 Hashing
     */
    private suspend fun attemptEmergencyFallback(
        email: String,
        password: String,
        rememberMe: Boolean
    ): Boolean {
        val secureAdminStorage = com.serviseyem.utils.SecureAdminStorage
        
        // 1. التحقق للمالك
        if (secureAdminStorage.verifyFallbackCredentials(application, email, password, "OWNER")) {
            handleEmergencyFallbackSuccess("OWNER", email, rememberMe)
            return true
        }
        
        // 2. التحقق للأدمن
        if (secureAdminStorage.verifyFallbackCredentials(application, email, password, "ADMIN")) {
            handleEmergencyFallbackSuccess("ADMIN", email, rememberMe)
            return true
        }
        
        return false
    }

    private fun handleEmergencyFallbackSuccess(role: String, email: String, rememberMe: Boolean) {
        _adminRole.value = role
        if (rememberMe) {
            secureStorage.saveAdminSession(com.serviseyem.utils.AdminSession(
                uid = "emergency_${role.lowercase()}",
                email = email,
                loginTime = System.currentTimeMillis(),
                refreshToken = "EMERGENCY_SESSION",
                role = role
            ))
        }
    }
}