package com.serviseyem.ui.viewmodels
import com.serviseyem.data.isValid
import kotlinx.coroutines.tasks.await

import android.content.Context
import com.serviseyem.ui.*
import androidx.lifecycle.viewModelScope
import com.serviseyem.data.*
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.UUID

import javax.inject.Inject

open class AuthViewModel @Inject constructor() : BaseViewModel() {

    fun updateUserFcmToken(userId: String, token: String, currentUserPhone: String) {
        if (userId.isEmpty() || userId == "guest" || token.isBlank()) return
        viewModelScope.launch {
            try {
                db.collection("registered_users").document(userId).get().addOnSuccessListener { doc ->
                    if (doc != null && doc.exists()) {
                        doc.reference.update("fcmToken", token)
                    }
                }
                val cleanPhone = com.serviseyem.domain.usecases.ValidatePhoneUseCase.normalizePhone(currentUserPhone).filter { it.isDigit() }
                if (cleanPhone.length >= 7) {
                    val phoneVariants = listOf(cleanPhone, "0$cleanPhone", "+967$cleanPhone", "967$cleanPhone", "00967$cleanPhone").distinct()
                    for (col in listOf("providers", "stores", "properties", "users", "registered_users")) {
                        for (ph in phoneVariants) {
                            db.collection(col).whereEqualTo("phone", ph).get().addOnSuccessListener { snaps ->
                                for (doc in snaps.documents) {
                                    doc.reference.update("fcmToken", token)
                                }
                            }
                        }
                    }
                }
            } catch (e: Exception) {
                android.util.Log.e("AuthViewModel", "Error updating fcmToken: ", e)
            }
        }
    }

    internal val _currentUserId = MutableStateFlow("guest")
    val currentUserId: StateFlow<String> = _currentUserId.asStateFlow()

    internal val _currentUserName = MutableStateFlow("")
    val currentUserName: StateFlow<String> = _currentUserName.asStateFlow()

    internal val _currentUserPhone = MutableStateFlow("")
    val currentUserPhone: StateFlow<String> = _currentUserPhone.asStateFlow()

    fun setCurrentUserPhone(phone: String) {
        _currentUserPhone.value = phone
    }

    internal val _currentUserResidence = MutableStateFlow("")
    val currentUserResidence: StateFlow<String> = _currentUserResidence.asStateFlow()

    internal val _joinRequestPhone = MutableStateFlow("")
    val joinRequestPhone: StateFlow<String> = _joinRequestPhone.asStateFlow()

    internal val _adminRole = MutableStateFlow("GUEST")
    val adminRole: StateFlow<String> = _adminRole.asStateFlow()

    internal val _passwordRecoveryWaitingPhone = MutableStateFlow("")
    val passwordRecoveryWaitingPhone: StateFlow<String> = _passwordRecoveryWaitingPhone.asStateFlow()

    internal val _showBackdoorDialog = MutableStateFlow(false)
    val showBackdoorDialog: StateFlow<Boolean> = _showBackdoorDialog.asStateFlow()

    internal val _supervisors = MutableStateFlow<List<SupervisorEntity>>(emptyList())
    val supervisors: StateFlow<List<SupervisorEntity>> = _supervisors.asStateFlow()

    internal val _currentSupervisorPermissions = MutableStateFlow<List<String>>(emptyList())
    val currentSupervisorPermissions: StateFlow<List<String>> = _currentSupervisorPermissions.asStateFlow()

    val auth: FirebaseAuth by lazy {
        FirebaseAuth.getInstance()
    }

    fun resetRegistrationState() {
        _joinRequestPhone.value = ""
        _passwordRecoveryWaitingPhone.value = ""
    }

    private var clickCount = 0
    private var lastBackdoorClickTime = 0L
    @Volatile
    private var cachedAppContext: Context? = null

    fun getOrGenerateUserId(): String {
        var current = _currentUserId.value
        if (current.isBlank() || current == "guest") {
            val sp = cachedAppContext?.getSharedPreferences("yemen_service_prefs", Context.MODE_PRIVATE)
            val savedGuest = sp?.getString("persistent_guest_id", "")?.takeIf { it.isNotBlank() }?.let {
                com.serviseyem.utils.SecurityCryptoUtils.decrypt(it).ifBlank {
                    if (!it.startsWith("gcm:") && !it.startsWith("gcmx:") && !it.startsWith("enc::")) it else ""
                }
            }.orEmpty()
            current = if (savedGuest.isNotBlank() && savedGuest != "guest") {
                savedGuest
            } else {
                "USR_GUEST_" + (100000..999999).random().toString()
            }
            _currentUserId.value = current
            if (sp != null) {
                val enc = com.serviseyem.utils.SecurityCryptoUtils.encrypt(current)
                sp.edit()
                    .putString("persistent_guest_id", enc)
                    .putString("user_id", enc)
                    .apply()
            }
        }
        return current
    }

    fun setPasswordRecoveryWaitingPhone(phone: String) {
        _passwordRecoveryWaitingPhone.value = phone
    }

    fun setJoinRequestPhone(context: Context, phone: String) {
        cachedAppContext = context.applicationContext
        val normalized = com.serviseyem.domain.usecases.ValidatePhoneUseCase.normalizePhone(phone).filter { it.isDigit() }
        val finalPhone = normalized.ifBlank { phone.trim() }
        _joinRequestPhone.value = finalPhone
        val sp = context.getSharedPreferences("yemen_service_prefs", Context.MODE_PRIVATE)
        sp.edit().putString("join_request_phone", com.serviseyem.utils.SecurityCryptoUtils.encrypt(finalPhone)).apply()
    }

    fun initializeUserIdentity(context: Context, onFavoritesLoaded: ((Set<String>) -> Unit)? = null) {
        cachedAppContext = context.applicationContext
        com.serviseyem.ui.LocaleManager.init(context)
        val sp = context.getSharedPreferences("yemen_service_prefs", Context.MODE_PRIVATE)
        
        val rawId = sp.getString("user_id", "guest") ?: "guest"
        var savedId = if (rawId != "guest" && rawId.isNotEmpty()) com.serviseyem.utils.SecurityCryptoUtils.decrypt(rawId).ifBlank {
            if (!rawId.startsWith("gcm:") && !rawId.startsWith("gcmx:") && !rawId.startsWith("enc::")) rawId else ""
        } else rawId
        val rawName = sp.getString("user_name", "") ?: ""
        val savedName = com.serviseyem.utils.SecurityCryptoUtils.decrypt(rawName).ifBlank {
            if (!rawName.startsWith("gcm:") && !rawName.startsWith("gcmx:") && !rawName.startsWith("enc::")) rawName else ""
        }
        val rawPhone = sp.getString("user_phone", "") ?: ""
        val savedPhone = com.serviseyem.utils.SecurityCryptoUtils.decrypt(rawPhone).ifBlank {
            if (!rawPhone.startsWith("gcm:") && !rawPhone.startsWith("gcmx:") && !rawPhone.startsWith("enc::")) rawPhone else ""
        }
        val rawResidence = sp.getString("user_residence", "") ?: ""
        val savedResidence = com.serviseyem.utils.SecurityCryptoUtils.decrypt(rawResidence).ifBlank {
            if (!rawResidence.startsWith("gcm:") && !rawResidence.startsWith("gcmx:") && !rawResidence.startsWith("enc::")) rawResidence else ""
        }

        if (savedId == "guest" || savedId.isEmpty()) {
            if (savedPhone.isNotEmpty()) {
                val cleanP = savedPhone.filter { it.isDigit() }
                savedId = "USR-" + (if (cleanP.length >= 6) cleanP.takeLast(6) else (100000..999999).random().toString())
            } else {
                val persistentGuest = sp.getString("persistent_guest_id", "") ?: ""
                if (persistentGuest.isNotEmpty()) {
                    savedId = com.serviseyem.utils.SecurityCryptoUtils.decrypt(persistentGuest)
                }
                if (savedId.isEmpty() || savedId == "guest") {
                    savedId = "USR_GUEST_" + (100000..999999).random().toString()
                    val enc = com.serviseyem.utils.SecurityCryptoUtils.encrypt(savedId)
                    sp.edit().putString("persistent_guest_id", enc).apply()
                }
            }
            sp.edit().putString("user_id", com.serviseyem.utils.SecurityCryptoUtils.encrypt(savedId)).apply()
        }

        _currentUserId.value = savedId
        _currentUserName.value = savedName
        _currentUserPhone.value = savedPhone
        _currentUserResidence.value = savedResidence
        
        val rawJoinPhone = sp.getString("join_request_phone", "") ?: ""
        val savedJoinPhone = if (rawJoinPhone.isNotBlank()) {
            com.serviseyem.utils.SecurityCryptoUtils.decrypt(rawJoinPhone).ifBlank {
                if (!rawJoinPhone.startsWith("gcm:") && !rawJoinPhone.startsWith("gcmx:") && !rawJoinPhone.startsWith("enc::")) rawJoinPhone else ""
            }
        } else ""
        _joinRequestPhone.value = savedJoinPhone

        val rawWaitingPhone = sp.getString("password_recovery_waiting_phone", "") ?: ""
        if (rawWaitingPhone.isNotBlank()) {
            val decryptedWaiting = com.serviseyem.utils.SecurityCryptoUtils.decrypt(rawWaitingPhone).ifBlank {
                if (!rawWaitingPhone.startsWith("gcm:") && !rawWaitingPhone.startsWith("gcmx:") && !rawWaitingPhone.startsWith("enc::")) rawWaitingPhone else ""
            }
            if (decryptedWaiting.isNotBlank()) {
                _passwordRecoveryWaitingPhone.value = decryptedWaiting
            }
        }
        
        val secureStorage = com.serviseyem.utils.SecureStorage(context)
        val session = secureStorage.getAdminSession()
        if (session != null) {
            val isExpired = System.currentTimeMillis() - session.loginTime > 30L * 24 * 60 * 60 * 1000
            if (!isExpired) {
                _adminRole.value = session.role
                if (session.role == "OWNER") {
                    _currentSupervisorPermissions.value = listOf("ALL")
                } else if (session.role == "SUPERVISOR") {
                    if (session.permissions.isNotEmpty()) {
                        _currentSupervisorPermissions.value = session.permissions
                    }
                    // إذا كان مشرفاً، نحاول جلب صلاحياته المخزنة أو الافتراضية
                    viewModelScope.launch {
                        try {
                            val db = com.google.firebase.firestore.FirebaseFirestore.getInstance()
                            val doc = db.collection("supervisors").document(session.uid).get().await()
                            if (doc.exists()) {
                                @Suppress("UNCHECKED_CAST")
                                val perms = doc.get("permissions") as? List<String> ?: emptyList()
                                _currentSupervisorPermissions.value = perms
                            }
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    }
                }
            } else {
                secureStorage.clearAdminSession()
                _adminRole.value = "GUEST"
            }
        } else {
            _adminRole.value = "GUEST"
        }

        try {
            val savedFavs = sp.getStringSet("favorite_ids_set", emptySet()) ?: emptySet()
            if (savedFavs.isNotEmpty()) {
                onFavoritesLoaded?.invoke(savedFavs)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        autoSyncProviderCredentials(context, savedJoinPhone, savedId, sp)
    }

    private fun autoSyncProviderCredentials(
        @Suppress("UNUSED_PARAMETER") context: Context,
        savedJoinPhone: String,
        @Suppress("UNUSED_PARAMETER") savedId: String,
        sp: android.content.SharedPreferences
    ) {
        val phoneToLookup = com.serviseyem.domain.usecases.ValidatePhoneUseCase.normalizePhone(
            savedJoinPhone.ifEmpty { _currentUserPhone.value }
        ).filter { it.isDigit() }
        if (phoneToLookup.length >= 7) {
            db.collection("providers").whereEqualTo("phone", phoneToLookup).get().addOnSuccessListener { snapshot ->
                if (snapshot != null && !snapshot.isEmpty) {
                    val candidates = snapshot.documents.mapNotNull { doc ->
                        doc.toObject(ProviderEntity::class.java)?.let { entity ->
                            if (entity.id.isBlank()) entity.copy(id = doc.id) else entity
                        }
                    }
                    val prov = candidates.firstOrNull { !it.isDeleted } ?: candidates.firstOrNull()
                    if (prov != null) {
                        _currentUserId.value = prov.id
                        _currentUserName.value = prov.name
                        _currentUserPhone.value = prov.phone
                        _currentUserResidence.value = prov.area
                        if (_adminRole.value !in listOf("OWNER", "ADMIN", "SUPERVISOR")) {
                            _adminRole.value = "PROVIDER"
                        }
                        
                        sp.edit().apply {
                            putString("user_id", com.serviseyem.utils.SecurityCryptoUtils.encrypt(prov.id))
                            putString("user_name", com.serviseyem.utils.SecurityCryptoUtils.encrypt(prov.name))
                            putString("user_phone", com.serviseyem.utils.SecurityCryptoUtils.encrypt(prov.phone))
                            putString("user_residence", com.serviseyem.utils.SecurityCryptoUtils.encrypt(prov.area))
                            apply()
                        }
                    }
                } else {
                    db.collection("stores").whereEqualTo("phone", phoneToLookup).get().addOnSuccessListener { sSnap ->
                        if (sSnap != null && !sSnap.isEmpty) {
                            val candidates = sSnap.documents.mapNotNull { doc ->
                                doc.toObject(com.serviseyem.data.StoreEntity::class.java)?.let { entity ->
                                    if (entity.id.isBlank()) entity.copy(id = doc.id) else entity
                                }
                            }
                            val st = candidates.firstOrNull { !it.isDeleted } ?: candidates.firstOrNull()
                            if (st != null) {
                                _currentUserId.value = st.id
                                _currentUserName.value = st.name
                                _currentUserPhone.value = st.phone
                                _currentUserResidence.value = st.cityId
                                if (_adminRole.value !in listOf("OWNER", "ADMIN", "SUPERVISOR")) {
                                    _adminRole.value = "STORE_OWNER"
                                }
                                sp.edit().apply {
                                    putString("user_id", com.serviseyem.utils.SecurityCryptoUtils.encrypt(st.id))
                                    putString("user_name", com.serviseyem.utils.SecurityCryptoUtils.encrypt(st.name))
                                    putString("user_phone", com.serviseyem.utils.SecurityCryptoUtils.encrypt(st.phone))
                                    putString("user_residence", com.serviseyem.utils.SecurityCryptoUtils.encrypt(st.cityId))
                                    apply()
                                }
                            }
                        } else {
                            db.collection("pending_providers").whereEqualTo("phone", phoneToLookup).get().addOnSuccessListener { pSnapshot ->
                                if (pSnapshot != null && !pSnapshot.isEmpty) {
                                    val pend = pSnapshot.documents.first().toObject(PendingProviderEntity::class.java)
                                    if (pend != null) {
                                        val pendId = "user_" + pend.phone
                                        _currentUserId.value = pendId
                                        _currentUserName.value = pend.name
                                        _currentUserPhone.value = pend.phone
                                        _currentUserResidence.value = pend.area
                                        
                                        sp.edit().apply {
                                            putString("user_id", com.serviseyem.utils.SecurityCryptoUtils.encrypt(pendId))
                                            putString("user_name", com.serviseyem.utils.SecurityCryptoUtils.encrypt(pend.name))
                                            putString("user_phone", com.serviseyem.utils.SecurityCryptoUtils.encrypt(pend.phone))
                                            putString("user_residence", com.serviseyem.utils.SecurityCryptoUtils.encrypt(pend.area))
                                            apply()
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

    fun registerGuestUser(
        context: Context,
        name: String,
        phone: String,
        residence: String,
        password: String = "",
        confirmPassword: String = password
    ) {
        cachedAppContext = context.applicationContext
        val cleanPhone = com.serviseyem.domain.usecases.ValidatePhoneUseCase.normalizePhone(phone).filter { it.isDigit() }
        val cleanPassword = password.trim()
        val cleanConfirm = confirmPassword.trim()
        if (cleanPhone.length < 7) {
            triggerToast("⚠️ يرجى إدخال رقم هاتف صحيح")
            return
        }
        if (cleanPassword.isBlank()) {
            triggerToast("⚠️ لا يمكن التسجيل بدون كلمة مرور، يرجى إدخال كلمة مرور صريحة لتأمين حسابك")
            return
        }
        if (cleanPassword != cleanConfirm) {
            triggerToast("⚠️ كلمة المرور وتأكيد كلمة المرور غير متطابقين")
            return
        }

        val valResult = com.serviseyem.utils.SecurityCryptoUtils.validatePasswordPolicy(cleanPassword)
        if (!valResult.first) {
            triggerToast("⚠️ ${valResult.second ?: "يرجى إدخال كلمة مرور قوية لحماية حسابك"}")
            return
        }

        viewModelScope.launch {
            try {
                val client = com.serviseyem.domain.entities.RegistrationEntity.Client(
                    fullName = name.trim(),
                    phone = cleanPhone,
                    city = residence.trim(),
                    rawPassword = cleanPassword
                )

                val repository = com.serviseyem.data.repositories.RegistrationRepositoryImpl(context)
                val result = repository.registerClient(client)

                if (result.isSuccess) {
                    val userId = result.getOrNull() ?: ("usr_" + cleanPhone)

                    _currentUserId.value = userId
                    _currentUserName.value = name.trim()
                    _currentUserPhone.value = cleanPhone
                    _currentUserResidence.value = residence.trim()
                    _joinRequestPhone.value = cleanPhone

                    val sp = context.getSharedPreferences("yemen_service_prefs", Context.MODE_PRIVATE)
                    sp.edit().apply {
                        putString("user_id", com.serviseyem.utils.SecurityCryptoUtils.encrypt(userId))
                        putString("user_name", com.serviseyem.utils.SecurityCryptoUtils.encrypt(name.trim()))
                        putString("user_phone", com.serviseyem.utils.SecurityCryptoUtils.encrypt(cleanPhone))
                        putString("user_residence", com.serviseyem.utils.SecurityCryptoUtils.encrypt(residence.trim()))
                        putString("join_request_phone", com.serviseyem.utils.SecurityCryptoUtils.encrypt(cleanPhone))
                        apply()
                    }

                    triggerToast("🎉 أهلاً بك في الدليل $name، تم تسجيل وحماية حسابك آمنياً بنجاح!")
                } else {
                    val errorMsg = result.exceptionOrNull()?.localizedMessage ?: "فشل تسجيل حساب العميل"
                    triggerToast("❌ $errorMsg")
                }
            } catch (e: Exception) {
                triggerToast("❌ حدث خطأ أثناء التسجيل: ${e.localizedMessage}")
            }
        }
    }

    fun setUserSessionDetails(context: Context, name: String, phone: String, residence: String = "اليمن") {
        val normalized = com.serviseyem.domain.usecases.ValidatePhoneUseCase.normalizePhone(phone).filter { it.isDigit() }
        val finalPhone = normalized.ifBlank { phone.trim() }
        _currentUserName.value = name.ifBlank { "عميل" }
        _currentUserPhone.value = finalPhone
        _currentUserResidence.value = residence.ifBlank { "اليمن" }
        if (_currentUserId.value.isEmpty() || _currentUserId.value == "guest") {
            _currentUserId.value = "user_" + (if (finalPhone.length >= 6) finalPhone.takeLast(6) else (100000..999999).random().toString())
        }
        _joinRequestPhone.value = finalPhone
        val sp = context.getSharedPreferences("yemen_service_prefs", Context.MODE_PRIVATE)
        sp.edit().apply {
            putString("user_name", com.serviseyem.utils.SecurityCryptoUtils.encrypt(_currentUserName.value))
            putString("user_phone", com.serviseyem.utils.SecurityCryptoUtils.encrypt(finalPhone))
            putString("user_residence", com.serviseyem.utils.SecurityCryptoUtils.encrypt(_currentUserResidence.value))
            putString("user_id", com.serviseyem.utils.SecurityCryptoUtils.encrypt(_currentUserId.value))
            putString("join_request_phone", com.serviseyem.utils.SecurityCryptoUtils.encrypt(finalPhone))
            apply()
        }
        try {
            com.google.firebase.crashlytics.FirebaseCrashlytics.getInstance().setUserId(_currentUserId.value)
            com.google.firebase.crashlytics.FirebaseCrashlytics.getInstance().log("Session updated for user ${_currentUserId.value}")
        } catch (e: Exception) {
            // تجاهل في بيئات الاختبار
        }
    }

    fun loginUserDirectly(context: Context, phone: String, password: String) {
        val finalPhone = com.serviseyem.domain.usecases.ValidatePhoneUseCase.normalizePhone(phone).filter { it.isDigit() }
        val cleanPassword = password.trim()
        if (finalPhone.length < 7 || cleanPassword.isBlank()) {
            triggerToast("❌ يرجى إدخال رقم الهاتف وكلمة المرور بشكل صحيح")
            return
        }
        val phoneVariants = listOf(finalPhone, "0$finalPhone", "+967$finalPhone", "967$finalPhone", "00967$finalPhone").distinct()

        viewModelScope.launch {
            try {
                var matchedDoc: com.google.firebase.firestore.DocumentSnapshot? = null
                for (col in listOf("registered_users", "users")) {
                    for (ph in phoneVariants) {
                        val snap = runCatching {
                            db.collection(col).whereEqualTo("phone", ph).limit(1).get().await()
                        }.getOrNull()
                        val docs = snap?.documents.orEmpty().toMutableList()
                        runCatching {
                            val direct = db.collection(col).document(ph).get().await()
                            if (direct.exists() && docs.none { it.id == direct.id }) {
                                docs.add(direct)
                            }
                        }
                        for (doc in docs) {
                            val hasCredential = !doc.getString("passwordHash").isNullOrBlank() || !doc.getString("password").isNullOrBlank()
                            if (hasCredential) {
                                matchedDoc = doc
                                break
                            }
                        }
                        if (matchedDoc != null) break
                    }
                    if (matchedDoc != null) break
                }
                val targetDoc = matchedDoc

                if (targetDoc != null) {
                    val storedHash = targetDoc.getString("passwordHash")?.trim().orEmpty()
                        .ifBlank { targetDoc.getString("password")?.trim().orEmpty() }
                    val fieldUsed = if (!targetDoc.getString("passwordHash").isNullOrBlank()) "passwordHash" else "password"
                    val isVerified = com.serviseyem.utils.SecureHasher.verifyPassword(cleanPassword, storedHash) ||
                        com.serviseyem.utils.PasswordHasher.verifyPassword(cleanPassword, storedHash) ||
                        com.serviseyem.utils.SecureAdminStorage.verifyAndMigrate(
                            docRef = targetDoc.reference,
                            inputPassword = cleanPassword,
                            storedPassOrHash = storedHash,
                            fieldName = fieldUsed
                        )
                    if (isVerified) {
                        val userName = targetDoc.getString("name")?.takeIf { it.isNotBlank() }
                            ?: targetDoc.getString("fullName")?.takeIf { it.isNotBlank() }
                            ?: "عميل"
                        val userCity = targetDoc.getString("city")?.takeIf { it.isNotBlank() }
                            ?: targetDoc.getString("area")?.takeIf { it.isNotBlank() }
                            ?: "اليمن"
                        setUserSessionDetails(context, userName, finalPhone, userCity)
                        val sp = context.getSharedPreferences("yemen_service_prefs", Context.MODE_PRIVATE)
                        sp.edit().apply {
                            putBoolean("is_account_logged_in", true)
                            putString("user_account_type", "CLIENT")
                            putString("logged_account_id", targetDoc.id)
                            apply()
                        }
                        runCatching {
                            val masterKey = androidx.security.crypto.MasterKey.Builder(context)
                                .setKeyScheme(androidx.security.crypto.MasterKey.KeyScheme.AES256_GCM)
                                .build()
                            val secureSp = androidx.security.crypto.EncryptedSharedPreferences.create(
                                context,
                                "yemen_service_secure_prefs",
                                masterKey,
                                androidx.security.crypto.EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                                androidx.security.crypto.EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
                            )
                            secureSp.edit()
                                .putBoolean("is_account_logged_in", true)
                                .putString("user_account_type", "CLIENT")
                                .putString("logged_account_id", targetDoc.id)
                                .apply()
                        }
                        if (_adminRole.value !in listOf("OWNER", "ADMIN", "SUPERVISOR")) {
                            _adminRole.value = "GUEST"
                        }
                        triggerToast("✅ تم تسجيل الدخول بنجاح")
                    } else {
                        triggerToast("❌ كلمة المرور غير صحيحة")
                    }
                } else {
                    triggerToast("❌ الحساب غير موجود")
                }
            } catch (e: Exception) {
                triggerToast("❌ حدث خطأ أثناء التحقق: ${e.message}")
            }
        }
    }

    internal var currentSupervisorId: String = ""

    fun setAdminRole(role: String) {
        _adminRole.value = role
        if (role == "OWNER") {
            _currentSupervisorPermissions.value = listOf("ALL")
        }
    }

    fun authenticateAdmin(role: String) {
        _adminRole.value = role
        if (role == "OWNER") {
            _currentSupervisorPermissions.value = listOf("ALL")
        }
        triggerToast("🔓 تم تسجيل الدخول بنجاح بصلاحية: $role")
    }

    fun authenticateAdmin(context: Context, role: String, remember: Boolean) {
        _adminRole.value = role
        if (role == "OWNER") {
            _currentSupervisorPermissions.value = listOf("ALL")
        }
        try {
            val secureStorage = com.serviseyem.utils.SecureStorage(context)
            val existingSession = secureStorage.getAdminSession()
            if (role == "SUPERVISOR" && _currentSupervisorPermissions.value.isEmpty() && existingSession?.role == "SUPERVISOR" && existingSession.permissions.isNotEmpty()) {
                _currentSupervisorPermissions.value = existingSession.permissions
            }
            if (remember) {
                val perms = if (role == "OWNER") listOf("ALL") else _currentSupervisorPermissions.value
                val resolvedUid = if (role == "SUPERVISOR") {
                    currentSupervisorId.ifBlank {
                        existingSession?.uid?.takeIf { it.isNotBlank() && !it.startsWith("supervisor_") }
                            ?: "${role.lowercase()}_${System.currentTimeMillis()}"
                    }
                } else {
                    "${role.lowercase()}_${System.currentTimeMillis()}"
                }
                secureStorage.saveAdminSession(
                    com.serviseyem.utils.AdminSession(
                        uid = resolvedUid,
                        email = existingSession?.email?.takeIf { it.isNotBlank() } ?: role,
                        loginTime = System.currentTimeMillis(),
                        refreshToken = "${role}_SESSION",
                        role = role,
                        permissions = perms
                    )
                )
            } else {
                secureStorage.clearAdminSession()
            }
        } catch (_: Exception) {}
        triggerToast("🔓 تم تسجيل الدخول بنجاح بصلاحية: $role")
    }

    fun logout(context: Context) {
        _adminRole.value = "GUEST"
        _currentSupervisorPermissions.value = emptyList()
        currentSupervisorId = ""
        try {
            com.serviseyem.utils.SecureStorage(context).clearAdminSession()
        } catch (_: Exception) {}
        triggerToast("🔒 تم تسجيل الخروج بنجاح")
    }

    fun verifyAdminOrOwnerPassword(password: String, adminPass: String = "", ownerPass: String = ""): Boolean {
        val trimmed = password.trim()
        if (trimmed.isEmpty() || com.serviseyem.utils.SecureHasher.isHashFormat(trimmed)) return false
        
        if (com.serviseyem.utils.SecurityCryptoUtils.verifyAdminPassword(trimmed, adminPass) ||
            com.serviseyem.utils.SecurityCryptoUtils.verifyAdminPassword(trimmed, ownerPass)) {
            return true
        }
        val matchSup = _supervisors.value.find {
            it.passcodeHash.isNotBlank() && com.serviseyem.utils.SecurityCryptoUtils.verifyAdminPassword(trimmed, it.passcodeHash)
        }
        return matchSup != null
    }

    fun registerBackdoorInteraction() {
        if (!com.serviseyem.BuildConfig.DEBUG) return
        val now = System.currentTimeMillis()
        if (now - lastBackdoorClickTime > 3000L) {
            clickCount = 0
        }
        lastBackdoorClickTime = now
        clickCount++
        if (clickCount >= 3) {
            clickCount = 0
            _showBackdoorDialog.value = true
        }
    }

    fun showBackdoorDialog() {
        if (!com.serviseyem.BuildConfig.DEBUG) return
        _showBackdoorDialog.value = true
    }

    fun dismissBackdoorDialog() {
        _showBackdoorDialog.value = false
    }

    fun setSupervisorSession(sup: SupervisorEntity) {
        currentSupervisorId = sup.id
        _adminRole.value = "SUPERVISOR"
        _currentSupervisorPermissions.value = sup.permissions
    }

    fun hasAdminPermission(permissionKey: String): Boolean {
        return com.serviseyem.utils.PermissionGuard.hasPermission(
            role = com.serviseyem.utils.RoleManager.fromRoleString(_adminRole.value),
            permission = permissionKey,
            supervisorGrantedPermissions = _currentSupervisorPermissions.value
        )
    }

    fun addSupervisor(name: String, role: String, passcode: String, permissions: List<String> = emptyList()) {
        val cleanName = name.trim()
        val cleanPasscode = passcode.trim()
        if (cleanName.isBlank() || cleanPasscode.isBlank()) {
            triggerToast("❌ يرجى إدخال اسم المشرف والرمز السري")
            return
        }
        val nextId = "sup_" + UUID.randomUUID().toString().take(6)
        // ✨ م2: تشفير كلمة المرور (Hashing) قبل التخزين لحماية المشرفين
        val hashedPass = if (com.serviseyem.utils.SecureHasher.isValidHash(cleanPasscode)) {
            cleanPasscode
        } else {
            com.serviseyem.utils.SecureHasher.hashPassword(cleanPasscode)
        }
        val newSup = SupervisorEntity(id = nextId, name = cleanName, role = role, passcodeHash = hashedPass, permissions = permissions)
        db.collection("supervisors").document(nextId).set(newSup)
        triggerToast("🔑 تم إضافة المشرف $cleanName وتعيين ${permissions.size} صلاحية بنجاح")
    }

    fun editSupervisor(id: String, name: String, role: String, passcode: String, permissions: List<String> = emptyList()) {
        val cleanName = name.trim()
        val cleanPasscode = passcode.trim()
        if (id.isBlank() || cleanName.isBlank() || cleanPasscode.isBlank()) {
            triggerToast("❌ بيانات المشرف غير مكتملة")
            return
        }
        // ✨ م2: تشفير كلمة المرور في حال التعديل لضمان الأمان
        val finalPass = if (com.serviseyem.utils.SecureHasher.isValidHash(cleanPasscode)) {
            cleanPasscode
        } else {
            com.serviseyem.utils.SecureHasher.hashPassword(cleanPasscode)
        }
        val updatedSup = SupervisorEntity(id = id, name = cleanName, role = role, passcodeHash = finalPass, permissions = permissions)
        db.collection("supervisors").document(id).set(updatedSup)
        triggerToast("✏️ تم تعديل بيانات وصلاحيات المشرف $cleanName (${permissions.size} صلاحية) بنجاح")
    }

    fun updateSupervisorPermissions(id: String, permissions: List<String>) {
        db.collection("supervisors").document(id).update("permissions", permissions)
        triggerToast("🛡️ تم تحديث الصلاحيات الممنوحة للمشرف (${permissions.size} صلاحية)")
    }

    fun removeSupervisor(id: String) {
        db.collection("supervisors").document(id).delete()
        triggerToast("🗑️ تم إلغاء صلاحية المشرف بنجاح")
    }

    fun submitPasswordRecoveryRequest(
        phone: String,
        channel: String,
        note: String,
        onComplete: (Boolean) -> Unit = {}
    ) {
        val cleanPhone = com.serviseyem.domain.usecases.ValidatePhoneUseCase.normalizePhone(phone).filter { it.isDigit() }
        if (cleanPhone.length < 7) {
            onComplete(false)
            return
        }
        val currentTime = System.currentTimeMillis()
        val currentUid = com.google.firebase.auth.FirebaseAuth.getInstance().currentUser?.uid ?: ""

        val resetRequest = mapOf(
            "id" to cleanPhone,
            "uid" to currentUid,
            "phone" to cleanPhone,
            "channel" to channel,
            "note" to note,
            "status" to "PENDING",
            "newPassword" to "",
            "passwordHash" to "",
            "createdAt" to currentTime,
            "requestedAt" to currentTime,
            "timestamp" to currentTime
        )
        db.collection("password_resets").document(cleanPhone).set(resetRequest)

        val adminRecoveryRequest = mapOf(
            "id" to cleanPhone,
            "uid" to currentUid,
            "phone" to cleanPhone,
            "name" to "طلب استعادة ($cleanPhone)",
            "accountType" to "مسترجع",
            "status" to "PENDING",
            "createdAt" to currentTime,
            "requestedAt" to currentTime,
            "timestamp" to currentTime,
            "newPassword" to "",
            "passwordHash" to "",
            "adminNotes" to "القناة: $channel | ملاحظة: $note"
        )
        db.collection("password_recovery_requests").document(cleanPhone)
            .set(adminRecoveryRequest)
            .addOnSuccessListener {
                val adminNotifId = "PWD_RESET_ADMIN_$cleanPhone"
                val adminNotif = mapOf(
                    "id" to adminNotifId,
                    "title" to "🔑 طلب استعادة حساب جديد",
                    "message" to "ورد طلب استعادة حساب للرقم: $cleanPhone عبر قناة $channel",
                    "targetType" to "ADMIN_ONLY",
                    "targetValue" to "ALL",
                    "timestamp" to currentTime,
                    "dedupKey" to "PWD_RESET_$cleanPhone"
                )
                if (adminNotif.isValid()) {
                    db.collection("notifications").document(adminNotifId).set(adminNotif)
                }
                onComplete(true)
            }
            .addOnFailureListener {
                onComplete(false)
            }
    }

    data class PasswordRecoveryStatus(val status: String = "", val tempPassword: String = "")

    private val _passwordRecoveryStatus = MutableStateFlow(PasswordRecoveryStatus())
    val passwordRecoveryStatus: StateFlow<PasswordRecoveryStatus> = _passwordRecoveryStatus.asStateFlow()

    private var passwordRecoveryStatusListener: com.google.firebase.firestore.ListenerRegistration? = null

    fun listenToPasswordRecoveryStatus(phone: String) {
        passwordRecoveryStatusListener?.remove()
        val cleanPhone = com.serviseyem.domain.usecases.ValidatePhoneUseCase.normalizePhone(phone).filter { it.isDigit() }
        if (cleanPhone.length < 7) return
        passwordRecoveryStatusListener = db.collection("password_recovery_requests").document(cleanPhone)
            .addSnapshotListener { snapshot, e ->
                if (e != null) return@addSnapshotListener
                if (snapshot != null && snapshot.exists()) {
                    val status = snapshot.getString("status") ?: "PENDING"
                    val rawPass = (snapshot.getString("newPassword") ?: snapshot.getString("tempPassword") ?: "").trim()
                    val temp = when {
                        rawPass.isEmpty() -> ""
                        rawPass.startsWith("gcmx:") -> com.serviseyem.utils.SecurityCryptoUtils.decryptCrossDevice(rawPass)
                        rawPass.startsWith("gcm:") || rawPass.startsWith("enc::") ->
                            com.serviseyem.utils.SecurityCryptoUtils.decrypt(rawPass).ifBlank {
                                com.serviseyem.utils.SecurityCryptoUtils.decryptCrossDevice(rawPass)
                            }
                        com.serviseyem.utils.SecureHasher.isValidHash(rawPass) -> ""
                        else -> rawPass
                    }
                    _passwordRecoveryStatus.value = PasswordRecoveryStatus(status, temp)
                }
            }
    }

    override fun onCleared() {
        super.onCleared()
        passwordRecoveryStatusListener?.remove()
    }

    fun observePasswordRecoveryStatus(
        phone: String,
        onUpdate: (status: String, newPassword: String, accountName: String, accountType: String) -> Unit
    ): com.google.firebase.firestore.ListenerRegistration? {
        if (phone.isBlank()) return null
        val cleanPhone = com.serviseyem.domain.usecases.ValidatePhoneUseCase.normalizePhone(phone).filter { it.isDigit() }
        if (cleanPhone.length < 7) return null
        return try {
            db.collection("password_recovery_requests")
                .document(cleanPhone)
                .addSnapshotListener { snapshot, e ->
                    if (e != null) return@addSnapshotListener
                    if (snapshot != null && snapshot.exists()) {
                        val status = snapshot.getString("status") ?: "PENDING"
                        val rawPass = (snapshot.getString("newPassword") ?: snapshot.getString("tempPassword") ?: "").trim()
                        val newPassword = when {
                            rawPass.isEmpty() -> ""
                            rawPass.startsWith("gcmx:") -> com.serviseyem.utils.SecurityCryptoUtils.decryptCrossDevice(rawPass)
                            rawPass.startsWith("gcm:") || rawPass.startsWith("enc::") ->
                                com.serviseyem.utils.SecurityCryptoUtils.decrypt(rawPass).ifBlank {
                                    com.serviseyem.utils.SecurityCryptoUtils.decryptCrossDevice(rawPass)
                                }
                            com.serviseyem.utils.SecureHasher.isValidHash(rawPass) -> ""
                            else -> rawPass
                        }
                        val accountName = snapshot.getString("name") ?: "صاحب الحساب"
                        val accountType = snapshot.getString("accountType") ?: "حساب معتمد"
                        onUpdate(status, newPassword, accountName, accountType)
                    }
                }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    fun requestPasswordRecovery(
        phone: String,
        name: String,
        accountType: String
    ) {
        viewModelScope.launch {
            try {
                val cleanPhone = com.serviseyem.domain.usecases.ValidatePhoneUseCase.normalizePhone(phone).filter { it.isDigit() }
                if (cleanPhone.length < 7) {
                    triggerToast("❌ رقم الهاتف غير صالح")
                    return@launch
                }
                val currentTime = System.currentTimeMillis()
                val currentUid = com.google.firebase.auth.FirebaseAuth.getInstance().currentUser?.uid ?: ""
                val requestData = mapOf(
                    "id" to cleanPhone,
                    "uid" to currentUid,
                    "phone" to cleanPhone,
                    "name" to name.ifBlank { "صاحب الحساب ($cleanPhone)" },
                    "accountType" to accountType,
                    "status" to "PENDING",
                    "createdAt" to currentTime,
                    "requestedAt" to currentTime,
                    "timestamp" to currentTime,
                    "newPassword" to "",
                    "passwordHash" to "",
                    "adminNotes" to ""
                )
                
                db.collection("password_recovery_requests")
                    .document(cleanPhone)
                    .set(requestData, com.google.firebase.firestore.SetOptions.merge())
                    .await()

                runCatching {
                    db.collection("password_resets").document(cleanPhone).set(
                        mapOf(
                            "id" to cleanPhone,
                            "uid" to currentUid,
                            "phone" to cleanPhone,
                            "status" to "PENDING",
                            "newPassword" to "",
                            "passwordHash" to "",
                            "createdAt" to currentTime,
                            "requestedAt" to currentTime,
                            "timestamp" to currentTime
                        ),
                        com.google.firebase.firestore.SetOptions.merge()
                    )
                }

                val notifDocId = "PWD_RESET_NOTIF_$cleanPhone"
                val adminNotif = mapOf(
                    "id" to notifDocId,
                    "title" to "🔑 طلب استعادة كلمة مرور ($name)",
                    "message" to "ورد طلب استعادة وتعيين كلمة مرور للحساب: $name ($accountType) - الهاتف: $cleanPhone",
                    "targetType" to "ADMIN_ONLY",
                    "targetValue" to "ALL",
                    "timestamp" to currentTime,
                    "dedupKey" to "PWD_RESET_$cleanPhone"
                )
                if (adminNotif.isValid()) db.collection("notifications").document(notifDocId).set(adminNotif)
                
                // Record in activity_logs
                val logId = db.collection("activity_logs").document().id
                val log = com.serviseyem.data.ActivityLogEntity(
                    id = logId,
                    action = "🔑 طلب استعادة كلمة المرور للحساب: $name ($cleanPhone - $accountType)",
                    timestamp = currentTime
                )
                db.collection("activity_logs").document(logId).set(log)

                triggerToast("✅ تم إرسال طلبك للإدارة. سيتم مراجعته والتواصل معك قريباً")
            } catch (e: Exception) {
                triggerToast("❌ فشل إرسال الطلب: ${e.message}")
            }
        }
    }
}
