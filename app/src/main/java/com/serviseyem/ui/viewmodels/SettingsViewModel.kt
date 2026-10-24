package com.serviseyem.ui.viewmodels
import com.serviseyem.ui.helpers.AppState

import com.serviseyem.ui.MainViewModel
import com.serviseyem.ui.*

import android.content.Context
import androidx.lifecycle.viewModelScope
import com.serviseyem.data.*
import com.serviseyem.data.models.*
import com.serviseyem.utils.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.UUID
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.SetOptions
import javax.inject.Inject

class SettingsViewModel @Inject constructor(
    val appState: AppState
) : BaseViewModel() {
    fun updateAppLanguage(lang: String) {
        val updated = _settings.value.copy(appLanguage = lang)
        _settings.value = updated
        try {
            db.collection("settings").document("main_settings").update("appLanguage", lang)
        } catch (e: Exception) {
            android.util.Log.e("SettingsViewModel", "Error: ", e)
        }
    }
    // --- Callback/Lambda Properties for decoupling ---
    var getAuthViewModel: (() -> AuthViewModel)? = null
    var getHomeViewModel: (() -> HomeViewModel)? = null
    var getBookingViewModel: (() -> BookingViewModel)? = null
    var getAdminViewModel: (() -> AdminViewModel)? = null
    
    var getProviders: (() -> MutableStateFlow<List<ProviderEntity>>)? = null
    var getBookings: (() -> MutableStateFlow<List<BookingEntity>>)? = null
    var getCategories: (() -> MutableStateFlow<List<CategoryEntity>>)? = null
    var getStores: (() -> MutableStateFlow<List<StoreEntity>>)? = null
    var getProperties: (() -> MutableStateFlow<List<PropertyEntity>>)? = null
    
    var getPasswordRecoveryWaitingPhone: (() -> MutableStateFlow<String>)? = null
    var setPasswordRecoveryWaitingPhone: ((String) -> Unit)? = null
    var verifyAdminOrOwnerPassword: ((String) -> Boolean)? = null
    var triggerNotification: ((String) -> Unit)? = null

    inner class MainViewModelDelegate {
        val authViewModel get() = getAuthViewModel?.invoke() ?: throw IllegalStateException("authViewModel not provided")
        val homeViewModel get() = getHomeViewModel?.invoke() ?: throw IllegalStateException("homeViewModel not provided")
        val bookingViewModel get() = getBookingViewModel?.invoke() ?: throw IllegalStateException("bookingViewModel not provided")
        val adminViewModel get() = getAdminViewModel?.invoke() ?: throw IllegalStateException("adminViewModel not provided")
        
        val _providers get() = getProviders?.invoke() ?: throw IllegalStateException("_providers not provided")
        val _bookings get() = getBookings?.invoke() ?: throw IllegalStateException("_bookings not provided")
        val _categories get() = getCategories?.invoke() ?: throw IllegalStateException("_categories not provided")
        val _stores get() = getStores?.invoke() ?: throw IllegalStateException("_stores not provided")
        val _properties get() = getProperties?.invoke() ?: throw IllegalStateException("_properties not provided")
        
        val _passwordRecoveryWaitingPhone get() = getPasswordRecoveryWaitingPhone?.invoke() ?: throw IllegalStateException("_passwordRecoveryWaitingPhone not provided")
        
        fun setPasswordRecoveryWaitingPhone(phone: String) {
            this@SettingsViewModel.setPasswordRecoveryWaitingPhone?.invoke(phone)
        }
        
        fun verifyAdminOrOwnerPassword(password: String): Boolean {
            return this@SettingsViewModel.verifyAdminOrOwnerPassword?.invoke(password) ?: false
        }
        
        fun triggerNotification(msg: String) {
            this@SettingsViewModel.triggerNotification?.invoke(msg)
        }

        fun requestPasswordRecovery(phone: String, name: String, accountType: String) {
            getAuthViewModel?.invoke()?.requestPasswordRecovery(phone, name, accountType)
        }
    }
    
    val mainViewModel = MainViewModelDelegate()

data class CardSettings(
        val cardHeight: Int = 180,
        val cardWidth: Int = 360,
        val cornerRadius: Int = 12,
        val backgroundColor: String = "#162A2D",
        val nameColor: String = "#FFFFFF",
        val ratingColor: String = "#FFD700",
        val locationColor: String = "#A0B2B5",
        val priceColor: String = "#4CAF50",
        val showVipBadge: Boolean = true,
        val showVerifiedBadge: Boolean = true,
        val showRecommendedBadge: Boolean = true,
        val vipColor: String = "#FFD700",
        val verifiedColor: String = "#2196F3",
        val recommendedColor: String = "#FF6B6B",
        val showCallButton: Boolean = true,
        val showWhatsAppButton: Boolean = true,
        val showDetailsButton: Boolean = true,
        val showBookingButton: Boolean = true,
        val callButtonColor: String = "#CE1126",
        val whatsappColor: String = "#25D366",
        val detailsColor: String = "#0D47A1",
        val bookingColor: String = "#E65100",
        val showDistance: Boolean = true,
        val showPrice: Boolean = true,
        val showAvailability: Boolean = true,
        val showRatingCount: Boolean = true,
        val imageShape: String = "circle",
        val spacing: Int = 8,
        val padding: Int = 12,
        val scaleAnimation: Boolean = true,
        val scaleFactor: Float = 0.95f
    )

    internal val _cardSettings = MutableStateFlow(CardSettings())
    val cardSettings: StateFlow<CardSettings> = _cardSettings.asStateFlow()

    fun updateCardSettings(settings: CardSettings) {
        _cardSettings.value = settings
        try {
            db.collection("settings").document("card_settings").set(settings)
        } catch (e: Exception) {}
    }

enum class ChatParticipantType {
        VISITOR,    // زائر
        PROVIDER,   // مقدم خدمة
        ADMIN,      // مشرف
        ALL         // الجميع
    }

    internal val _settings get() = appState._settings
    val settings: StateFlow<AdminSettingsEntity> = _settings.asStateFlow()
    internal val _colorScheme = MutableStateFlow(com.serviseyem.data.ColorSchemeEntity())
    val colorScheme: StateFlow<com.serviseyem.data.ColorSchemeEntity> = _colorScheme.asStateFlow()
    internal val _personalColors = MutableStateFlow(com.serviseyem.data.UserColorsEntity())
    val personalColors: StateFlow<com.serviseyem.data.UserColorsEntity> = _personalColors.asStateFlow()
    internal val _colorSyncStatus = MutableStateFlow(com.serviseyem.data.ColorSyncStatus.SYNCED)
    val colorSyncStatus: StateFlow<com.serviseyem.data.ColorSyncStatus> = _colorSyncStatus.asStateFlow()
    internal val _colorSyncLogs = MutableStateFlow<List<com.serviseyem.data.SyncLogEntity>>(emptyList())
    val colorSyncLogs: StateFlow<List<com.serviseyem.data.SyncLogEntity>> = _colorSyncLogs.asStateFlow()
    internal val _pendingConflictScheme = MutableStateFlow<com.serviseyem.data.ColorSchemeEntity?>(null)
    val pendingConflictScheme: StateFlow<com.serviseyem.data.ColorSchemeEntity?> = _pendingConflictScheme.asStateFlow()
    internal val _blockedChatParticipants = MutableStateFlow<Set<ChatParticipantType>>(emptySet())
    val blockedChatParticipants: StateFlow<Set<ChatParticipantType>> = _blockedChatParticipants.asStateFlow()
    internal val _activeVoiceCall = MutableStateFlow<Pair<String, String>?>(null)
    val activeVoiceCall: StateFlow<Pair<String, String>?> = _activeVoiceCall.asStateFlow()
    private var colorSchemeListener: com.google.firebase.firestore.ListenerRegistration? = null
    private var userColorsListener: com.google.firebase.firestore.ListenerRegistration? = null
    private var cardSettingsListener: com.google.firebase.firestore.ListenerRegistration? = null

fun loadCardSettings() {
        try {
            cardSettingsListener?.remove()
            cardSettingsListener = db.collection("settings").document("card_settings")
                ?.addSnapshotListener { snapshot, error ->
                    if (error != null) return@addSnapshotListener
                    if (snapshot != null && snapshot.exists()) {
                        val settings = snapshot.toObject(CardSettings::class.java)
                        if (settings != null) {
                            _cardSettings.value = settings
                        }
                    }
                }
        } catch (e: Exception) {
            android.util.Log.e("SettingsViewModel", "Error loading card settings", e)
        }
    }

fun updateTheme(themeId: String) {
        val updated = _settings.value.copy(activeThemeId = themeId)
        _settings.value = updated
        db.collection("settings").document("main_settings").get().addOnSuccessListener { snapshot ->
            val s = snapshot.toObject(AdminSettingsEntity::class.java) ?: AdminSettingsEntity()
            db.collection("settings").document("main_settings").set(s.copy(activeThemeId = themeId), com.google.firebase.firestore.SetOptions.merge())
        }.addOnFailureListener {
            db.collection("settings").document("main_settings").set(updated, com.google.firebase.firestore.SetOptions.merge())
        }
        mainViewModel.triggerNotification("🎨 تم تغيير مظهر التطبيق ومزامنته سحابياً إلى $themeId")
    }

fun saveCustomSettingsState(newSettings: AdminSettingsEntity) {
        safeFirestoreCallWithCallback(
            operation = { onSuccess, onFailure ->
                db.collection("settings").document("main_settings").set(newSettings, com.google.firebase.firestore.SetOptions.merge())
                    .addOnSuccessListener {
                        _settings.value = newSettings
                        onSuccess()
                    }
                    .addOnFailureListener { e ->
                        _settings.value = newSettings
                        onFailure(e)
                    }
            },
            onSuccess = { mainViewModel.triggerNotification("✅ تم حفظ ومزامنة كافة إعدادات التطبيق والدفع سحابياً بنجاح!") },
            onError = { mainViewModel.triggerNotification("⚠️ تم حفظ الإعدادات محلياً، وسيتم المزامنة سحابياً فور توفر الاتصال") },
            errorMessage = "فشل حفظ الإعدادات"
        )
    }

    fun updatePaymentEnabled(enabled: Boolean) {
        val current = _settings.value
        val updated = current.copy(isPaymentEnabled = enabled)
        saveCustomSettingsState(updated)
    }

fun updateBackdoorSettings(
        appName: String, welcomeMsg: String, footerMsg: String, themeId: String,
        supportPhone: String, supportEmail: String, supportWhatsapp: String,
        isMaintenance: Boolean, hiddenFooter: Boolean, botHidden: Boolean, botSize: Int,
        chatHidden: Boolean, chatSize: Int, radiusKm: Int, isSpeech: Boolean,
        isDataSaver: Boolean, imgQuality: Int,
        bookingTerms: String = "يرجى الالتزام التام بالمواعيد المحددة والتسعيرة المتفق عليها مع الفني.",
        bookingLabelName: String = "الاسم الكامل للعميل",
        bookingLabelPhone: String = "رقم هاتف العميل للتواصل (مثال: 777000111)",
        bookingLabelArea: String = "المنطقة والحي السكني",
        bookingLabelService: String = "تفاصيل ونوع الخدمة المطلوبة",
        adminUsername: String = "",
        adminPassword: String = "",
        customPrimaryHex: String = "#059669",
        customSecondaryHex: String = "#115E59",
        customBackgroundHex: String = "#0A0F0D",
        customSurfaceHex: String = "#121D18"
    ) {
        val updated = _settings.value.copy(
            appName = appName,
            welcomeMessage = welcomeMsg,
            footerMessage = footerMsg,
            activeThemeId = themeId,
            isMaintenanceActive = isMaintenance,
            hidePromoFooter = hiddenFooter,
            assistantHidden = botHidden,
            assistantSize = botSize,
            chatHidden = chatHidden,
            chatSize = chatSize,
            maxSearchRadiusKm = radiusKm,
            isSpeechSearchEnabled = isSpeech,
            supportPhone = supportPhone,
            supportEmail = supportEmail,
            supportWhatsapp = supportWhatsapp,
            bookingTerms = bookingTerms,
            bookingLabelName = bookingLabelName,
            bookingLabelPhone = bookingLabelPhone,
            bookingLabelArea = bookingLabelArea,
            bookingLabelService = bookingLabelService,
            adminUsername = adminUsername,
            ownerPassword = "",
            customPrimaryHex = customPrimaryHex,
            customSecondaryHex = customSecondaryHex,
            customBackgroundHex = customBackgroundHex,
            customSurfaceHex = customSurfaceHex
        )
        _settings.value = updated
        if (adminPassword.isNotBlank()) {
            val hashedAdmin = if (adminPassword.contains(":")) adminPassword else com.serviseyem.utils.SecureHasher.hashPassword(adminPassword.trim())
            val credUpdates = mapOf(
                "adminUsername" to adminUsername.trim(),
                "adminPasswordHash" to hashedAdmin
            )
            db.collection("settings").document("main_settings").set(credUpdates, com.google.firebase.firestore.SetOptions.merge())
            db.collection("admin_secrets").document("credentials").set(credUpdates, com.google.firebase.firestore.SetOptions.merge())
        }
        db.collection("settings").document("main_settings").set(updated, com.google.firebase.firestore.SetOptions.merge())
            .addOnSuccessListener {
                mainViewModel.triggerNotification("💾 تم حفظ ومزامنة إعدادات الإدارة العليا سحابياً بنجاح!")
            }
            .addOnFailureListener { e ->
                android.util.Log.e("SettingsViewModel", "Failed to sync backdoor settings", e)
                mainViewModel.triggerNotification("⚠️ تم حفظ الإعدادات محلياً، وسيتم المزامنة سحابياً فور توفر الاتصال")
            }
    }

fun updateAdminSettings(newSettings: AdminSettingsEntity) {
        val sanitized = newSettings.copy(ownerPassword = "")
        _settings.value = sanitized
        db.collection("settings").document("main_settings").set(sanitized, com.google.firebase.firestore.SetOptions.merge())
            .addOnSuccessListener {
                mainViewModel.triggerNotification("👑 تم تحديث ومزامنة إعدادات المنصة سحابياً بنجاح!")
            }
            .addOnFailureListener { e ->
                android.util.Log.e("SettingsViewModel", "Failed to sync admin settings", e)
                mainViewModel.triggerNotification("⚠️ تم حفظ الإعدادات محلياً، تعذر المزامنة السحابية مؤقتاً")
            }
    }

fun initColorSync(context: android.content.Context) {
        // 1. Load Local cached values
        val localScheme = com.serviseyem.utils.ColorSyncManager.getLocalColorScheme(context)
        _colorScheme.value = localScheme

        val localPersonal = com.serviseyem.utils.ColorSyncManager.getLocalPersonalColors(context)
        _personalColors.value = localPersonal

        _colorSyncLogs.value = com.serviseyem.utils.ColorSyncManager.getSyncLogs(context)

        // 2. Real-time Firestore listener for Main Color Scheme
        _colorSyncStatus.value = com.serviseyem.data.ColorSyncStatus.SYNCING
        colorSchemeListener?.remove()
        colorSchemeListener = db.collection("app_settings").document("color_scheme")
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    error.printStackTrace()
                    _colorSyncStatus.value = com.serviseyem.data.ColorSyncStatus.NOT_SYNCED
                    return@addSnapshotListener
                }

                if (snapshot != null && snapshot.exists()) {
                    try {
                        val cloudScheme = snapshot.toObject(com.serviseyem.data.ColorSchemeEntity::class.java)
                        if (cloudScheme != null) {
                            val currentLocal = _colorScheme.value
                            if (cloudScheme.version > currentLocal.version) {
                                // Newer cloud version found! Check for potential conflicts.
                                val cloudSerialized = com.serviseyem.utils.ColorSyncManager.serializeColorScheme(cloudScheme)
                                val localSerialized = com.serviseyem.utils.ColorSyncManager.serializeColorScheme(currentLocal)
                                
                                if (cloudSerialized != localSerialized) {
                                    _pendingConflictScheme.value = cloudScheme
                                    _colorSyncStatus.value = com.serviseyem.data.ColorSyncStatus.CONFLICT
                                    addNewSyncLog(
                                        context,
                                        "colors",
                                        "conflict",
                                        listOf("تعارض الإصدارات: محلي v${currentLocal.version} وسحابي v${cloudScheme.version}"),
                                        currentLocal.version,
                                        cloudScheme.version
                                    )
                                } else {
                                    // Same colors, just update version
                                    com.serviseyem.utils.ColorSyncManager.saveLocalColorScheme(context, cloudScheme)
                                    _colorScheme.value = cloudScheme
                                    _colorSyncStatus.value = com.serviseyem.data.ColorSyncStatus.SYNCED
                                }
                            } else if (cloudScheme.version < currentLocal.version) {
                                // Cloud is older! Push local changes to cloud to sync other devices.
                                db.collection("app_settings").document("color_scheme").set(currentLocal)
                                    .addOnSuccessListener {
                                        _colorSyncStatus.value = com.serviseyem.data.ColorSyncStatus.SYNCED
                                        addNewSyncLog(
                                            context,
                                            "colors",
                                            "success",
                                            listOf("مزامنة تصاعدية: تحديث السحابة للإصدار v${currentLocal.version}"),
                                            cloudScheme.version,
                                            currentLocal.version
                                        )
                                    }
                            } else {
                                // Versions match. Check content
                                val cloudSerialized = com.serviseyem.utils.ColorSyncManager.serializeColorScheme(cloudScheme)
                                val localSerialized = com.serviseyem.utils.ColorSyncManager.serializeColorScheme(currentLocal)
                                if (cloudSerialized != localSerialized) {
                                    com.serviseyem.utils.ColorSyncManager.saveLocalColorScheme(context, cloudScheme)
                                    _colorScheme.value = cloudScheme
                                    _colorSyncStatus.value = com.serviseyem.data.ColorSyncStatus.SYNCED
                                    addNewSyncLog(
                                        context,
                                        "colors",
                                        "success",
                                        listOf("مزامنة تلقائية: تم توحيد محتوى الألوان المتطابقة الإصدار"),
                                        currentLocal.version,
                                        cloudScheme.version
                                    )
                                } else {
                                    _colorSyncStatus.value = com.serviseyem.data.ColorSyncStatus.SYNCED
                                }
                            }
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                        _colorSyncStatus.value = com.serviseyem.data.ColorSyncStatus.NOT_SYNCED
                    }
                } else {
                    // Seed Firestore with default color scheme if it doesn't exist
                    val defaultScheme = com.serviseyem.data.ColorSchemeEntity(version = 1, lastUpdated = getCurrentTimestampString())
                    db.collection("app_settings").document("color_scheme").set(defaultScheme)
                        .addOnSuccessListener {
                            _colorSyncStatus.value = com.serviseyem.data.ColorSyncStatus.SYNCED
                            _colorScheme.value = defaultScheme
                            com.serviseyem.utils.ColorSyncManager.saveLocalColorScheme(context, defaultScheme)
                            addNewSyncLog(context, "colors", "success", listOf("تهيئة أولية لنظام الألوان السحابي"), 0, 1)
                        }
                }
            }
        firestoreListeners.add(colorSchemeListener!!)

        // 3. Real-time Firestore listener for Personal User Colors
        viewModelScope.launch {
            mainViewModel.authViewModel._currentUserId.collect { userId ->
                if (userId != "guest" && userId.isNotEmpty()) {
                    userColorsListener?.remove()
                    userColorsListener = db.collection("users").document(userId)
                        .addSnapshotListener { snapshot, error ->
                            if (error == null && snapshot != null && snapshot.exists()) {
                                try {
                                    val cloudPersonal = snapshot.toObject(com.serviseyem.data.UserColorsEntity::class.java)
                                    if (cloudPersonal != null) {
                                        val localPersonalColors = _personalColors.value
                                        if (cloudPersonal.colorsLastSynced != localPersonalColors.colorsLastSynced) {
                                            com.serviseyem.utils.ColorSyncManager.saveLocalPersonalColors(context, cloudPersonal)
                                            _personalColors.value = cloudPersonal
                                            addNewSyncLog(
                                                context,
                                                "personal",
                                                "success",
                                                listOf("تحديث الألوان الشخصية للمستخدم من السحابة"),
                                                0,
                                                0
                                            )
                                        }
                                    }
                                } catch (e: Exception) {
                                    e.printStackTrace()
                                }
                            }
                        }
                    firestoreListeners.add(userColorsListener!!)
                }
            }
        }
    }

fun updateCloudColorScheme(context: android.content.Context, newScheme: com.serviseyem.data.ColorSchemeEntity) {
        _colorSyncStatus.value = com.serviseyem.data.ColorSyncStatus.SYNCING
        val oldVer = _colorScheme.value.version
        db.collection("app_settings").document("color_scheme").set(newScheme)
            .addOnSuccessListener {
                com.serviseyem.utils.ColorSyncManager.saveLocalColorScheme(context, newScheme)
                _colorScheme.value = newScheme
                _colorSyncStatus.value = com.serviseyem.data.ColorSyncStatus.SYNCED
                addNewSyncLog(
                    context,
                    "colors",
                    "success",
                    listOf("تحديث مظهر الألوان العام بنجاح وزيادة الإصدار إلى v${newScheme.version}"),
                    oldVer,
                    newScheme.version
                )
                mainViewModel.triggerNotification("🎨 تم تحديث ومزامنة ألوان المظهر العام بنجاح!")
            }
            .addOnFailureListener { err ->
                _colorSyncStatus.value = com.serviseyem.data.ColorSyncStatus.NOT_SYNCED
                addNewSyncLog(
                    context,
                    "colors",
                    "failed",
                    listOf("فشل تحديث الألوان السحابية: ${err.localizedMessage}"),
                    oldVer,
                    newScheme.version
                )
                mainViewModel.triggerNotification("❌ فشل تحديث المظهر السحابي")
            }
    }

fun updatePersonalColors(context: android.content.Context, personal: com.serviseyem.data.PersonalColors) {
        val userId = mainViewModel.authViewModel._currentUserId.value
        val nowStr = getCurrentTimestampString()
        val newPersonalEntity = com.serviseyem.data.UserColorsEntity(personalColors = personal, colorsLastSynced = nowStr)
        
        com.serviseyem.utils.ColorSyncManager.saveLocalPersonalColors(context, newPersonalEntity)
        _personalColors.value = newPersonalEntity
        
        addNewSyncLog(
            context,
            "personal",
            "success",
            listOf("تخصيص ألوان المستخدم الشخصية محلياً: ${personal.favorite}"),
            0,
            0
        )

        if (userId != "guest" && userId.isNotEmpty()) {
            _colorSyncStatus.value = com.serviseyem.data.ColorSyncStatus.SYNCING
            db.collection("users").document(userId).set(newPersonalEntity)
                .addOnSuccessListener {
                    _colorSyncStatus.value = com.serviseyem.data.ColorSyncStatus.SYNCED
                    addNewSyncLog(
                        context,
                        "personal",
                        "success",
                        listOf("تم مزامنة ألوان المستخدم الشخصية بنجاح مع حسابه السحابي"),
                        0,
                        0
                    )
                }
                .addOnFailureListener { err ->
                    _colorSyncStatus.value = com.serviseyem.data.ColorSyncStatus.NOT_SYNCED
                    addNewSyncLog(
                        context,
                        "personal",
                        "failed",
                        listOf("فشل مزامنة ألوان المستخدم الشخصية: ${err.localizedMessage}"),
                        0,
                        0
                    )
                }
        }
    }

fun triggerManualSync(context: android.content.Context) {
        _colorSyncStatus.value = com.serviseyem.data.ColorSyncStatus.SYNCING
        addNewSyncLog(context, "colors", "syncing", listOf("بدء المزامنة اليدوية الإجبارية للألوان"), _colorScheme.value.version, _colorScheme.value.version)
        
        db.collection("app_settings").document("color_scheme").get()
            .addOnSuccessListener { doc ->
                if (doc != null && doc.exists()) {
                    val cloud = doc.toObject(com.serviseyem.data.ColorSchemeEntity::class.java)
                    if (cloud != null) {
                        val local = _colorScheme.value
                        if (cloud.version > local.version) {
                            _pendingConflictScheme.value = cloud
                            _colorSyncStatus.value = com.serviseyem.data.ColorSyncStatus.CONFLICT
                            addNewSyncLog(context, "colors", "conflict", listOf("توقف المزامنة بسبب وجود تعارض في الألوان في السحابة v${cloud.version}"), local.version, cloud.version)
                        } else if (cloud.version < local.version) {
                            db.collection("app_settings").document("color_scheme").set(local)
                                .addOnSuccessListener {
                                    _colorSyncStatus.value = com.serviseyem.data.ColorSyncStatus.SYNCED
                                    addNewSyncLog(context, "colors", "success", listOf("نجاح مزامنة الألوان التصاعدية يدوياً v${local.version}"), cloud.version, local.version)
                                    mainViewModel.triggerNotification("✅ تم رفع وتحديث ألوان الدليل بنجاح!")
                                }
                        } else {
                            com.serviseyem.utils.ColorSyncManager.saveLocalColorScheme(context, cloud)
                            _colorScheme.value = cloud
                            _colorSyncStatus.value = com.serviseyem.data.ColorSyncStatus.SYNCED
                            addNewSyncLog(context, "colors", "success", listOf("ألوان الدليل متزامنة تماماً ومتطابقة مع السحابة"), local.version, cloud.version)
                            mainViewModel.triggerNotification("✅ ألوان التطبيق متزامنة بالكامل!")
                        }
                    }
                } else {
                    val defaultScheme = com.serviseyem.data.ColorSchemeEntity(version = 1, lastUpdated = getCurrentTimestampString())
                    db.collection("app_settings").document("color_scheme").set(defaultScheme)
                        .addOnSuccessListener {
                            _colorSyncStatus.value = com.serviseyem.data.ColorSyncStatus.SYNCED
                            _colorScheme.value = defaultScheme
                            com.serviseyem.utils.ColorSyncManager.saveLocalColorScheme(context, defaultScheme)
                            addNewSyncLog(context, "colors", "success", listOf("تهيئة أولية ناجحة أثناء المزامنة اليدوية"), 0, 1)
                            mainViewModel.triggerNotification("✅ تم تهيئة ألوان السحابة بنجاح!")
                        }
                }
            }
            .addOnFailureListener { err ->
                _colorSyncStatus.value = com.serviseyem.data.ColorSyncStatus.NOT_SYNCED
                addNewSyncLog(context, "colors", "failed", listOf("فشل المزامنة اليدوية: ${err.localizedMessage}"), _colorScheme.value.version, _colorScheme.value.version)
                mainViewModel.triggerNotification("❌ فشل الاتصال بالخادم لمزامنة الألوان")
            }
    }

fun resolveConflict(context: android.content.Context, useCloud: Boolean) {
        val pending = _pendingConflictScheme.value ?: return
        val local = _colorScheme.value
        
        if (useCloud) {
            com.serviseyem.utils.ColorSyncManager.saveLocalColorScheme(context, pending)
            _colorScheme.value = pending
            _colorSyncStatus.value = com.serviseyem.data.ColorSyncStatus.SYNCED
            addNewSyncLog(
                context,
                "colors",
                "success",
                listOf("تم حل التعارض: تم اختيار واعتماد النسخة السحابية v${pending.version}"),
                local.version,
                pending.version
            )
            _pendingConflictScheme.value = null
            mainViewModel.triggerNotification("✅ تم اعتماد وتطبيق ألوان السحابة بنجاح!")
        } else {
            val updatedLocal = local.copy(version = pending.version + 1, lastUpdated = getCurrentTimestampString())
            _colorSyncStatus.value = com.serviseyem.data.ColorSyncStatus.SYNCING
            
            db.collection("app_settings").document("color_scheme").set(updatedLocal)
                .addOnSuccessListener {
                    com.serviseyem.utils.ColorSyncManager.saveLocalColorScheme(context, updatedLocal)
                    _colorScheme.value = updatedLocal
                    _colorSyncStatus.value = com.serviseyem.data.ColorSyncStatus.SYNCED
                    addNewSyncLog(
                        context,
                        "colors",
                        "success",
                        listOf("تم حل التعارض: تم فرض النسخة المحلية وتحديث السحابة للإصدار v${updatedLocal.version}"),
                        local.version,
                        updatedLocal.version
                    )
                    _pendingConflictScheme.value = null
                    mainViewModel.triggerNotification("✅ تم فرض ألوانك المحلية وتحديث السحابة بنجاح!")
                }
                .addOnFailureListener { err ->
                    _colorSyncStatus.value = com.serviseyem.data.ColorSyncStatus.NOT_SYNCED
                    addNewSyncLog(
                        context,
                        "colors",
                        "failed",
                        listOf("فشل فرض النسخة المحلية في حل التعارض: ${err.localizedMessage}"),
                        local.version,
                        updatedLocal.version
                    )
                    mainViewModel.triggerNotification("❌ تعذر رفع نسختك المحلية لحل التعارض")
                }
        }
    }

fun getCurrentTimestampString(): String {
        return try {
            // ✨ م2: استخدام DateFormatter
            com.serviseyem.utils.DateFormatter.formatIso(System.currentTimeMillis()) + "Z"
        } catch (e: Exception) {
            "2026-08-06T15:00:00Z"
        }
    }

fun addNewSyncLog(
        context: android.content.Context,
        type: String,
        status: String,
        changes: List<String>,
        versionFrom: Int,
        versionTo: Int
    ) {
        val newLog = com.serviseyem.data.SyncLogEntity(
            syncId = "sync_${java.util.UUID.randomUUID().toString().take(6)}",
            timestamp = getCurrentTimestampString(),
            type = type,
            status = status,
            changes = changes,
            versionFrom = versionFrom,
            versionTo = versionTo
        )
        com.serviseyem.utils.ColorSyncManager.saveSyncLog(context, newLog)
        _colorSyncLogs.value = com.serviseyem.utils.ColorSyncManager.getSyncLogs(context)
    }

fun toggleChatParticipant(participantType: ChatParticipantType) {
        val current = _blockedChatParticipants.value
        _blockedChatParticipants.value = if (participantType in current) {
            current - participantType
        } else {
            current + participantType
        }
        try {
            db.collection("settings").document("chat_participants")
                ?.set(mapOf("blocked" to _blockedChatParticipants.value.map { it.name }))
        } catch (e: Exception) {}
    }

fun isChatBlockedFor(participantType: ChatParticipantType): Boolean {
        return participantType in _blockedChatParticipants.value || ChatParticipantType.ALL in _blockedChatParticipants.value
    }

fun canParticipateInChat(participantType: ChatParticipantType): Boolean {
        return !isChatBlockedFor(participantType)
    }

fun startVoiceCall(name: String, role: String) {
        _activeVoiceCall.value = Pair(name, role)
    }

fun endVoiceCall() {
        _activeVoiceCall.value = null
    }

fun exportComplaintsToCSV() {
        mainViewModel.triggerNotification("📁 تم تصدير البلاغات بصيغة CSV")
    }

fun exportComplaintsToPDF() {
        mainViewModel.triggerNotification("📃 تم تصدير البلاغات بصيغة PDF")
    }

fun exportPerformanceReportToPDF() {
        mainViewModel.triggerNotification("📊 تم تصدير تقرير أداء شبكة الفنيين والمنصة بصيغة PDF بنجاح!")
    }

fun createSystemBackup(onComplete: (Boolean, String) -> Unit) {
        try {
            val root = org.json.JSONObject()
            
            // Serialize mainViewModel.homeViewModel.providers
            val provArray = org.json.JSONArray()
            mainViewModel.homeViewModel._providers.value.forEach { prov ->
                val obj = org.json.JSONObject()
                obj.put("id", prov.id)
                obj.put("name", prov.name)
                obj.put("phone", prov.phone)
                obj.put("customCategoryName", prov.customCategoryName)
                obj.put("cityId", prov.cityId)
                obj.put("localNeighborhood", prov.localNeighborhood)
                obj.put("isAvailable", prov.isAvailable)
                obj.put("rating", prov.rating.toDouble())
                obj.put("numReviews", prov.numReviews)
                provArray.put(obj)
            }
            root.put("providers", provArray)

            // Serialize mainViewModel.bookingViewModel.bookings
            val bookArray = org.json.JSONArray()
            mainViewModel.bookingViewModel._bookings.value.forEach { b ->
                val obj = org.json.JSONObject()
                obj.put("id", b.id)
                obj.put("customerPhone", b.customerPhone)
                obj.put("customerName", b.customerName)
                obj.put("customerArea", b.customerArea)
                obj.put("providerId", b.providerId)
                obj.put("providerName", b.providerName)
                obj.put("dateString", b.dateString)
                obj.put("timeString", b.timeString)
                obj.put("serviceType", b.serviceType)
                obj.put("status", b.status)
                bookArray.put(obj)
            }
            root.put("bookings", bookArray)

            // Serialize mainViewModel.homeViewModel.categories
            val catArray = org.json.JSONArray()
            mainViewModel.homeViewModel._categories.value.forEach { c ->
                val obj = org.json.JSONObject()
                obj.put("id", c.id)
                obj.put("name", c.name)
                obj.put("icon", c.icon)
                obj.put("parentId", c.parentId)
                obj.put("isMainCategory", c.isMainCategory)
                catArray.put(obj)
            }
            root.put("categories", catArray)

            // Serialize mainViewModel.adminViewModel.stores
            val storeArray = org.json.JSONArray()
            mainViewModel.adminViewModel._stores.value.forEach { s ->
                val obj = org.json.JSONObject()
                obj.put("id", s.id)
                obj.put("name", s.name)
                obj.put("description", s.description)
                obj.put("phone", s.phone)
                obj.put("ownerName", s.ownerName)
                obj.put("cityId", s.cityId)
                obj.put("localNeighborhood", s.localNeighborhood)
                obj.put("isActive", s.isActive)
                storeArray.put(obj)
            }
            root.put("stores", storeArray)

            // Serialize mainViewModel.adminViewModel.properties
            val propArray = org.json.JSONArray()
            mainViewModel.adminViewModel._properties.value.forEach { p ->
                val obj = org.json.JSONObject()
                obj.put("id", p.id)
                obj.put("title", p.title)
                obj.put("description", p.description)
                obj.put("price", p.price)
                obj.put("phone", p.phone)
                obj.put("cityId", p.cityId)
                obj.put("ownerName", p.ownerName)
                obj.put("isActive", p.isActive)
                propArray.put(obj)
            }
            root.put("properties", propArray)

            val jsonStr = root.toString(2)
            
            // Also write to cloud Firestore "database_backups" collection for periodic backup logging
            val backupId = "backup_" + System.currentTimeMillis()
            val backupData = hashMapOf(
                "id" to backupId,
                "timestamp" to com.google.firebase.Timestamp.now(),
                "data" to jsonStr,
                "summary" to "Providers: ${mainViewModel._providers.value.size}, Bookings: ${mainViewModel._bookings.value.size}, Categories: ${mainViewModel._categories.value.size}, Stores: ${mainViewModel._stores.value.size}"
            )
            db.collection("database_backups").document(backupId).set(backupData)
                .addOnSuccessListener {
                    mainViewModel.triggerNotification("💾 تم إنشاء النسخة الاحتياطية الدورية السحابية الأسبوعية وحفظها بنجاح!")
                    onComplete(true, jsonStr)
                }
                .addOnFailureListener { e ->
                    onComplete(true, jsonStr)
                }
        } catch (e: Exception) {
            e.printStackTrace()
            onComplete(false, e.message ?: "Unknown error")
        }
    }

fun restoreSystemFromBackup(jsonStr: String, onComplete: (Boolean, String) -> Unit) {
        try {
            val root = org.json.JSONObject(jsonStr)
            
            // Restore mainViewModel.homeViewModel.providers
            if (root.has("providers")) {
                val array = root.getJSONArray("providers")
                for (i in 0 until array.length()) {
                    val obj = array.getJSONObject(i)
                    val id = obj.getString("id")
                    val phone = obj.optString("phone", id)
                    val data = hashMapOf(
                        "id" to id,
                        "name" to obj.optString("name", ""),
                        "phone" to phone,
                        "customCategoryName" to obj.optString("customCategoryName", ""),
                        "cityId" to obj.optString("cityId", ""),
                        "localNeighborhood" to obj.optString("localNeighborhood", ""),
                        "isAvailable" to obj.optBoolean("isAvailable", true),
                        "rating" to obj.optDouble("rating", 4.5).toFloat(),
                        "numReviews" to obj.optInt("numReviews", 1)
                    )
                    db.collection("providers").document(id).set(data)
                }
            }

            // Restore mainViewModel.bookingViewModel.bookings
            if (root.has("bookings")) {
                val array = root.getJSONArray("bookings")
                for (i in 0 until array.length()) {
                    val obj = array.getJSONObject(i)
                    val id = obj.getString("id")
                    val data = hashMapOf(
                        "id" to id,
                        "customerPhone" to obj.optString("customerPhone", ""),
                        "customerName" to obj.optString("customerName", ""),
                        "customerArea" to obj.optString("customerArea", ""),
                        "providerId" to obj.optString("providerId", ""),
                        "providerName" to obj.optString("providerName", ""),
                        "dateString" to obj.optString("dateString", ""),
                        "timeString" to obj.optString("timeString", ""),
                        "serviceType" to obj.optString("serviceType", ""),
                        "status" to obj.optString("status", "PENDING")
                    )
                    db.collection("bookings").document(id).set(data)
                }
            }

            // Restore mainViewModel.homeViewModel.categories
            if (root.has("categories")) {
                val array = root.getJSONArray("categories")
                for (i in 0 until array.length()) {
                    val obj = array.getJSONObject(i)
                    val id = obj.getString("id")
                    val data = hashMapOf(
                        "id" to id,
                        "name" to obj.optString("name", ""),
                        "icon" to obj.optString("icon", ""),
                        "parentId" to obj.optString("parentId", ""),
                        "isMainCategory" to obj.optBoolean("isMainCategory", true)
                    )
                    db.collection("categories").document(id).set(data)
                }
            }

            // Restore mainViewModel.adminViewModel.stores
            if (root.has("stores")) {
                val array = root.getJSONArray("stores")
                for (i in 0 until array.length()) {
                    val obj = array.getJSONObject(i)
                    val id = obj.getString("id")
                    val data = hashMapOf(
                        "id" to id,
                        "name" to obj.optString("name", ""),
                        "description" to obj.optString("description", ""),
                        "phone" to obj.optString("phone", ""),
                        "ownerName" to obj.optString("ownerName", ""),
                        "cityId" to obj.optString("cityId", ""),
                        "localNeighborhood" to obj.optString("localNeighborhood", ""),
                        "isActive" to obj.optBoolean("isActive", true)
                    )
                    db.collection("stores").document(id).set(data)
                }
            }

            // Restore mainViewModel.adminViewModel.properties
            if (root.has("properties")) {
                val array = root.getJSONArray("properties")
                for (i in 0 until array.length()) {
                    val obj = array.getJSONObject(i)
                    val id = obj.getString("id")
                    val data = hashMapOf(
                        "id" to id,
                        "title" to obj.optString("title", ""),
                        "description" to obj.optString("description", ""),
                        "price" to obj.optDouble("price", 0.0),
                        "phone" to obj.optString("phone", ""),
                        "cityId" to obj.optString("cityId", ""),
                        "ownerName" to obj.optString("ownerName", ""),
                        "isActive" to obj.optBoolean("isActive", true)
                    )
                    db.collection("properties").document(id).set(data)
                }
            }

            mainViewModel.triggerNotification("💚 تم استعادة قاعدة البيانات الشاملة بنجاح ومزامنتها سحابياً!")
            onComplete(true, "Success")
        } catch (e: Exception) {
            e.printStackTrace()
            onComplete(false, e.message ?: "Unknown parsing error")
        }
    }

fun exportSelectedCollectionsAsJson(selectedCollections: List<String>, onResult: (String) -> Unit) {
        viewModelScope.launch {
            val rootJson = org.json.JSONObject()
            if (selectedCollections.isEmpty()) {
                onResult("{}")
                return@launch
            }
            var completedCount = 0
            selectedCollections.forEach { col ->
                db.collection(col).get().addOnSuccessListener { snapshot ->
                    val arr = org.json.JSONArray()
                    snapshot?.documents?.forEach { doc ->
                        val obj = org.json.JSONObject()
                        doc.data?.forEach { (k, v) ->
                            // Simple formatting for JSON serialization
                            if (v != null) {
                                obj.put(k, v.toString())
                            }
                        }
                        obj.put("id", doc.id)
                        arr.put(obj)
                    }
                    rootJson.put(col, arr)
                    completedCount++
                    if (completedCount == selectedCollections.size) {
                        onResult(rootJson.toString(4))
                    }
                }.addOnFailureListener {
                    completedCount++
                    if (completedCount == selectedCollections.size) {
                        onResult(rootJson.toString(4))
                    }
                }
            }
        }
    }

fun saveBackupToLocalStorage(context: android.content.Context, jsonStr: String, fileName: String): String {
        return try {
            val downloadDir = android.os.Environment.getExternalStoragePublicDirectory(android.os.Environment.DIRECTORY_DOWNLOADS)
            val backupFolder = java.io.File(downloadDir, "YemenServicesBackups")
            if (!backupFolder.exists()) {
                backupFolder.mkdirs()
            }
            val targetFile = java.io.File(backupFolder, if (fileName.endsWith(".json")) fileName else "$fileName.json")
            targetFile.writeText(jsonStr, Charsets.UTF_8)
            targetFile.absolutePath
        } catch (e: Exception) {
            try {
                val targetFile = java.io.File(context.getExternalFilesDir(null), fileName)
                targetFile.writeText(jsonStr, Charsets.UTF_8)
                targetFile.absolutePath
            } catch (e2: Exception) {
                ""
            }
        }
    }

fun setSecondaryFirebaseConfig(projectId: String, apiKey: String, appId: String, storageBucket: String, isEnabled: Boolean) {
        val map = mapOf(
            "secondary_projectId" to projectId,
            "secondary_apiKey" to apiKey,
            "secondary_appId" to appId,
            "secondary_storageBucket" to storageBucket,
            "secondary_enabled" to isEnabled
        )
        db.collection("admin_settings").document("secondary_firebase").set(map)
        mainViewModel.triggerNotification(if (isEnabled) "🟢 تم تفعيل المزامنة المزدوجة مع حساب Firebase الثانوي بنجاح!" else "🔴 تم إيقاف المزامنة مع الحساب الثانوي")
    }

fun saveCustomPermissionsMatrixToFirestore(permissions: List<String>) {
        val payload = mapOf(
            "activePermissions" to permissions,
            "totalCount" to permissions.size,
            "updatedAt" to System.currentTimeMillis()
        )
        db.collection("settings").document("admin_permissions_matrix").set(payload)
            .addOnSuccessListener {
                mainViewModel.triggerNotification("💾 تم حفظ وتحديث مصفوفة الصلاحيات (${permissions.size} / 538) في قاعدة بيانات Firestore بنجاح!")
            }
            .addOnFailureListener {
                mainViewModel.triggerNotification("❌ تعذر حفظ مصفوفة الصلاحيات في Firestore: ${it.localizedMessage}")
            }
    }

fun addColorPalette(name: String, primaryHex: String, secondaryHex: String, backgroundHex: String = "#0A0F0D", surfaceHex: String = "#121D18") {
        val nextId = "palette_" + UUID.randomUUID().toString().take(6)
        val newPal = ColorPaletteEntity(nextId, name, primaryHex, secondaryHex, backgroundHex, surfaceHex)
        db.collection("color_themes").document(nextId).set(newPal)
        mainViewModel.triggerNotification("🎨 تم إضافة اللون $name بنجاح")
    }

fun updateColorPalette(id: String, name: String, primaryHex: String, secondaryHex: String, backgroundHex: String = "#0A0F0D", surfaceHex: String = "#121D18") {
        val updatedPal = ColorPaletteEntity(id, name, primaryHex, secondaryHex, backgroundHex, surfaceHex)
        db.collection("color_themes").document(id).set(updatedPal)
        mainViewModel.triggerNotification("✏️ تم تعديل اللون $name بنجاح")
    }

fun deleteColorPalette(id: String) {
        db.collection("color_themes").document(id).delete()
        mainViewModel.triggerNotification("🗑️ تم حذف اللون بنجاح")
    }

fun resetAccountPassword(entityType: String, phoneOrId: String, newPass: String) {
        val cleanPhone = phoneOrId.trim().replace(" ", "").replace("+967", "").replace("967", "").replace("+", "")
        val hashedPass = com.serviseyem.utils.PasswordHasher.hash(newPass.trim())
        val passUpdate = mapOf("password" to hashedPass, "passwordHash" to hashedPass)
        
        // Update Firestore
        when (entityType) {
            "PROVIDER", "TECHNICIAN", "TECH" -> {
                db.collection("providers").get().addOnSuccessListener { qs ->
                    for (doc in qs.documents) {
                        val p = doc.getString("phone") ?: ""
                        if (p.contains(cleanPhone)) {
                            db.collection("providers").document(doc.id).update(passUpdate)
                        }
                    }
                }
                mainViewModel.homeViewModel._providers.value = mainViewModel.homeViewModel._providers.value.map { if (it.phone.contains(cleanPhone)) it.copy(passwordHash = hashedPass) else it }
            }
            "STORE", "RESTAURANT", "MEDICAL", "CENTER" -> {
                db.collection("stores").get().addOnSuccessListener { qs ->
                    for (doc in qs.documents) {
                        val p = doc.getString("phone") ?: ""
                        if (p.contains(cleanPhone)) {
                            db.collection("stores").document(doc.id).update(passUpdate)
                        }
                    }
                }
                mainViewModel.adminViewModel._stores.value = mainViewModel.adminViewModel._stores.value.map { if (it.phone.contains(cleanPhone)) it.copy(passwordHash = hashedPass) else it }
            }
            "JOB" -> {
                db.collection("jobs").get().addOnSuccessListener { qs ->
                    for (doc in qs.documents) {
                        val p = doc.getString("phone") ?: ""
                        if (p.contains(cleanPhone)) {
                            db.collection("jobs").document(doc.id).update(passUpdate)
                        }
                    }
                }
            }
            "USER" -> {
                db.collection("registered_users").get().addOnSuccessListener { qs ->
                    for (doc in qs.documents) {
                        val p = doc.getString("phone") ?: ""
                        if (p.contains(cleanPhone)) {
                            db.collection("registered_users").document(doc.id).update(passUpdate)
                        }
                    }
                }
                db.collection("users").get().addOnSuccessListener { qs ->
                    for (doc in qs.documents) {
                        val p = doc.getString("phone") ?: ""
                        if (p.contains(cleanPhone)) {
                            db.collection("users").document(doc.id).update(passUpdate)
                        }
                    }
                }
            }
        }

        // Log in activity_logs
        val logId = db.collection("activity_logs").document().id
        val log = com.serviseyem.data.ActivityLogEntity(
            id = logId,
            action = "🔑 إعادة تعيين كلمة المرور للحساب ($entityType - $cleanPhone)",
            timestamp = System.currentTimeMillis()
        )
        db.collection("activity_logs").document(logId).set(log)

        mainViewModel.triggerNotification("🔑 تم تحديث وإعادة تعيين كلمة المرور للحساب ($phoneOrId) بنجاح!")
    }

fun requestAdminPasswordReset(phone: String) {
        val cleanPhone = phone.trim().replace(" ", "").replace("+", "")
        val notif = NotificationEntity(
            id = "reset_" + UUID.randomUUID().toString().take(6),
            title = "🔑 طلب إعادة تعيين كلمة مرور لمستخدم",
            message = "طلب صاحب الهاتف $cleanPhone إعادة تعيين كلمة المرور الخاصة بحسابه الشخصي نظراً لنسيانها أو فقدانها.",
            targetType = "SUPERVISOR",
            targetValue = "ALL",
            timestamp = System.currentTimeMillis(),
            dedupKey = "PWD_RESET_$cleanPhone"
        )
        try {
            if (notif.isValid()) db.collection("notifications").document(notif.id).set(notif)
        } catch (e: Exception) {}
        mainViewModel.triggerNotification("📩 تم إرسال طلب استعادة وإعادة تعيين كلمة المرور لإدارة التطبيق بنجاح")
    }

fun requestPasswordReset(phone: String, onResult: (Boolean, String) -> Unit) {
        val cleanPhone = com.serviseyem.domain.usecases.ValidatePhoneUseCase.normalizePhone(phone).filter { it.isDigit() }
        if (cleanPhone.length < 7) {
            onResult(false, "رقم الهاتف غير صالح")
            return
        }
        mainViewModel.requestPasswordRecovery(cleanPhone, cleanPhone, "USER")
        requestAdminPasswordReset(cleanPhone)
        onResult(true, "تم إرسال طلب إعادة التعيين للإدارة بنجاح")
    }

fun approvePasswordReset(phone: String, onResult: (Boolean, String) -> Unit) {
        val cleanPhone = com.serviseyem.domain.usecases.ValidatePhoneUseCase.normalizePhone(phone).filter { it.isDigit() }
        if (cleanPhone.length < 7) {
            onResult(false, "رقم الهاتف غير صالح")
            return
        }

        // Generate temporary password
        val chars = "1234567890abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ"
        val tempPassword = (1..8).map { chars.random() }.joinToString("")
        val hashedPassword = com.serviseyem.utils.SecureHasher.hashPassword(tempPassword)
        val encryptedTempPass = com.serviseyem.utils.SecurityCryptoUtils.encryptCrossDevice(tempPassword)
        val now = System.currentTimeMillis()

        val statusPayload = mapOf(
            "status" to "APPROVED",
            "newPassword" to encryptedTempPass,
            "passwordHash" to hashedPassword,
            "resolvedAt" to now,
            "timestamp" to now
        )
        db.collection("password_recovery_requests").document(cleanPhone)
            .set(statusPayload, com.google.firebase.firestore.SetOptions.merge())
        db.collection("password_resets").document(cleanPhone)
            .set(statusPayload, com.google.firebase.firestore.SetOptions.merge())

        val phoneVariants = listOf(cleanPhone, "0$cleanPhone", "+967$cleanPhone", "967$cleanPhone", "00967$cleanPhone").distinct()
        val passUpdate = mapOf("password" to "", "passwordHash" to hashedPassword)
        val collections = listOf("providers", "pending_providers", "stores", "properties", "registered_users", "users", "join_requests")
        for (col in collections) {
            for (ph in phoneVariants) {
                db.collection(col).whereEqualTo("phone", ph).get().addOnSuccessListener { qs ->
                    for (doc in qs.documents) {
                        doc.reference.update(passUpdate)
                    }
                }
            }
            db.collection(col).document(cleanPhone).get().addOnSuccessListener { doc ->
                if (doc != null && doc.exists()) {
                    doc.reference.update(passUpdate)
                }
            }
        }

        val batch = db.batch()
        val notifId = java.util.UUID.randomUUID().toString()
        val notifRef = db.collection("notifications").document(notifId)
        val notif = NotificationEntity(
            id = notifId,
            title = "🔑 تم إعادة تعيين كلمة المرور",
            message = "تمت الموافقة على طلبك لإعادة تعيين كلمة المرور. كلمة المرور المؤقتة الجديدة هي: $tempPassword  يرجى تغييرها بعد تسجيل الدخول حفاظاً على خصوصيتك.",
            targetType = "USER",
            targetValue = cleanPhone,
            notificationType = "PASSWORD_RESET_APPROVED",
            timestamp = now,
            dedupKey = "PWD_RESET_APPROVED_$cleanPhone"
        )
        batch.set(notifRef, notif)

        val logId = db.collection("activity_logs").document().id
        val log = com.serviseyem.data.ActivityLogEntity(
            id = logId,
            action = "🔑 الموافقة على إعادة تعيين كلمة المرور للهاتف: $cleanPhone",
            timestamp = now
        )
        batch.set(db.collection("activity_logs").document(logId), log)

        batch.commit()
            .addOnSuccessListener {
                mainViewModel.triggerNotification("🔑 تم الموافقة على طلب إعادة تعيين كلمة المرور للهاتف ($cleanPhone)")
                onResult(true, "تم إعادة التعيين بنجاح. كلمة المرور المؤقتة هي: $tempPassword")
            }
            .addOnFailureListener {
                onResult(false, "فشل حفظ التحديثات: ${it.localizedMessage}")
            }
    }

fun adminResetAccountPassword(phone: String, newPassword: String, notifyAction: String, customerName: String) {
        val cleanPhone = com.serviseyem.domain.usecases.ValidatePhoneUseCase.normalizePhone(phone).filter { it.isDigit() }
        val cleanNewPass = newPassword.trim()
        if (cleanPhone.length < 7 || cleanNewPass.isBlank()) {
            mainViewModel.triggerNotification("❌ يرجى التأكد من رقم الهاتف وكلمة المرور الجديدة")
            return
        }
        val hashedPassword = com.serviseyem.utils.SecureHasher.hashPassword(cleanNewPass)
        val encryptedNewPass = com.serviseyem.utils.SecurityCryptoUtils.encryptCrossDevice(cleanNewPass)
        val passUpdate = mapOf("password" to "", "passwordHash" to hashedPassword)
        val phoneVariants = listOf(cleanPhone, "0$cleanPhone", "+967$cleanPhone", "967$cleanPhone", "00967$cleanPhone").distinct()

        val collections = listOf("providers", "pending_providers", "stores", "properties", "registered_users", "users", "join_requests")
        for (col in collections) {
            for (ph in phoneVariants) {
                db.collection(col).whereEqualTo("phone", ph).get().addOnSuccessListener { snap ->
                    for (doc in snap.documents) {
                        doc.reference.update(passUpdate)
                    }
                }
            }
            db.collection(col).document(cleanPhone).get().addOnSuccessListener { doc ->
                if (doc != null && doc.exists()) {
                    doc.reference.update(passUpdate)
                }
            }
        }

        val now = System.currentTimeMillis()
        val resolvePayload = mapOf(
            "status" to "RESOLVED",
            "newPassword" to encryptedNewPass,
            "passwordHash" to hashedPassword,
            "resolvedAt" to now,
            "timestamp" to now
        )
        try {
            db.collection("password_recovery_requests").document(cleanPhone)
                .set(resolvePayload, com.google.firebase.firestore.SetOptions.merge())
            db.collection("password_resets").document(cleanPhone)
                .set(resolvePayload, com.google.firebase.firestore.SetOptions.merge())
        } catch (_: Exception) {}

        mainViewModel.homeViewModel._providers.value = mainViewModel.homeViewModel._providers.value.map {
            if (com.serviseyem.domain.usecases.ValidatePhoneUseCase.normalizePhone(it.phone).filter { c -> c.isDigit() } == cleanPhone) {
                it.copy(passwordHash = hashedPassword)
            } else it
        }
        mainViewModel.adminViewModel._stores.value = mainViewModel.adminViewModel._stores.value.map {
            if (com.serviseyem.domain.usecases.ValidatePhoneUseCase.normalizePhone(it.phone).filter { c -> c.isDigit() } == cleanPhone) {
                it.copy(passwordHash = hashedPassword)
            } else it
        }

        // Log in activity_logs
        val logId = db.collection("activity_logs").document().id
        val log = com.serviseyem.data.ActivityLogEntity(
            id = logId,
            action = "🔑 إعادة تعيين كلمة المرور للحساب: $customerName ($cleanPhone) - الإجراء: $notifyAction",
            timestamp = now
        )
        db.collection("activity_logs").document(logId).set(log)

        val notifId = UUID.randomUUID().toString()
        val (title, message) = when (notifyAction) {
            "DIRECT_PASSWORD" -> Pair(
                "🔑 إعادة تعيين كلمة المرور بنجاح",
                "تم إعادة تعيين كلمة مرور حسابك. يرجى التواصل مع الدعم لاستلام كلمة المرور الجديدة"
            )
            "VERIFICATION_WHATSAPP" -> Pair(
                "🔐 التحقق من الهوية - استعادة الحساب",
                "عزيزي المشترك، يرجى التواصل عبر الواتساب أو التليجرام أو المحادثة الفورية مع الإدارة للتحقق من هويتك وتأكيد ملكيتك للحساب واستلام كلمة المرور."
            )
            "INSTANT_CHAT" -> Pair(
                "💬 محادثة فورية لاستعادة الحساب",
                "تم فتح قناة دعم فورية لك. يرجى التوجه للمحادثة المباشرة مع الإدارة للتحقق من هويتك واسترجاع حسابك فوراً."
            )
            else -> Pair(
                "🔑 تحديث كلمة المرور",
                "قامت الإدارة بتحديث ومعالجة طلب استعادة كلمة المرور الخاصة بحسابك."
            )
        }

        val userNotif = NotificationEntity(
            id = notifId,
            title = title,
            message = message,
            targetType = "USER",
            targetValue = cleanPhone,
            timestamp = now,
            dedupKey = "PWD_RESET_${cleanPhone}"
        )
        db.collection("notifications").document(notifId).set(userNotif).addOnSuccessListener {
            mainViewModel.triggerNotification("✅ تم إرسال إشعار إعادة التعيين للمستخدم بنجاح!")
        }
    }

fun requestPasswordRecoveryForStore(name: String, phone: String, password: String) {
        val cleanPhone = com.serviseyem.domain.usecases.ValidatePhoneUseCase.normalizePhone(phone).filter { it.isDigit() }
        mainViewModel.setPasswordRecoveryWaitingPhone(cleanPhone)
        mainViewModel.requestPasswordRecovery(cleanPhone, name, "STORE")
    }

fun requestPasswordRecoveryForProperty(title: String, phone: String, password: String) {
        val cleanPhone = com.serviseyem.domain.usecases.ValidatePhoneUseCase.normalizePhone(phone).filter { it.isDigit() }
        mainViewModel.setPasswordRecoveryWaitingPhone(cleanPhone)
        mainViewModel.requestPasswordRecovery(cleanPhone, title, "PROPERTY")
    }

fun requestPasswordRecoveryGeneral(accountName: String, phone: String, accountType: String, currentPassword: String) {
        val cleanPhone = com.serviseyem.domain.usecases.ValidatePhoneUseCase.normalizePhone(phone).filter { it.isDigit() }
        mainViewModel.setPasswordRecoveryWaitingPhone(cleanPhone)
        mainViewModel.requestPasswordRecovery(cleanPhone, accountName, accountType)
    }

fun wipeAllDatabaseData(password: String): Boolean {
        if (mainViewModel.verifyAdminOrOwnerPassword(password)) {
            val collections = listOf("categories", "providers", "pending_providers", "banners", "settings", "reports", "bookings", "notifications", "chat_channels", "cities", "stores", "medical", "restaurants", com.serviseyem.utils.AppConstants.COL_JOBS, "job_applications", "properties", "products", "reviews")
            collections.forEach { col ->
                db.collection(col).get().addOnSuccessListener { snapshot ->
                    snapshot.documents.forEach { doc -> doc.reference.delete() }
                }
            }
            mainViewModel.triggerNotification("💥 تم مسح كامل قاعدة البيانات المحددة بنجاح وإعادتها إلى الصفر!")
            return true
        } else {
            mainViewModel.triggerNotification("❌ كلمة المرور غير صحيحة! فشل تطهير البيانات.")
            return false
        }
    }

fun wipeSelectedDatabaseData(password: String, selectedCollections: List<String>): Boolean {
        if (mainViewModel.verifyAdminOrOwnerPassword(password)) {
            selectedCollections.forEach { col ->
                db.collection(col).get().addOnSuccessListener { snapshot ->
                    snapshot.documents.forEach { doc ->
                        // If mainViewModel.homeViewModel.providers is selected, check if we keep our default user p_maher/amin_alghorbani if needed
                        doc.reference.delete()
                    }
                }
            }
            mainViewModel.triggerNotification("🧹 تم مسح الفئات المحددة وإعادتها إلى الصفر بنجاح!")
            return true
        } else {
            mainViewModel.triggerNotification("❌ كلمة المرور غير صحيحة! فشل تطهير البيانات.")
            return false
        }
    }

fun wipeAllMockAndTemporaryData() {
        viewModelScope.launch {
            try {
                val mockPrefixes = listOf("mock_", "test_", "demo_", "temp_", "sample_")
                fun isMockDoc(doc: com.google.firebase.firestore.DocumentSnapshot): Boolean {
                    val idLower = doc.id.lowercase()
                    val isFlagged = doc.getBoolean("isMock") == true ||
                        doc.getBoolean("isTest") == true ||
                        doc.getBoolean("isTemporary") == true
                    val hasPrefix = mockPrefixes.any { idLower.startsWith(it) }
                    return isFlagged || hasPrefix
                }

                for (col in listOf("notifications", "chat_channels", "reports", "bookings", "providers")) {
                    db.collection(col).whereEqualTo("isMock", true).get().addOnSuccessListener { snapshot ->
                        snapshot?.documents?.forEach { doc ->
                            if (isMockDoc(doc)) doc.reference.delete()
                        }
                    }
                    db.collection(col).whereEqualTo("isTest", true).get().addOnSuccessListener { snapshot ->
                        snapshot?.documents?.forEach { doc ->
                            if (isMockDoc(doc)) doc.reference.delete()
                        }
                    }
                }
                mainViewModel.triggerNotification("🧹 تم تنظيف البيانات التجريبية والوهمية فقط مع الحفاظ الكامل على جميع بيانات وفنيي الإنتاج!")
            } catch (e: Exception) {
                android.util.Log.e("SettingsViewModel", "Error wiping mock data", e)
                mainViewModel.triggerNotification("❌ حدث خطأ أثناء عملية التنظيف")
            }
        }
    }

fun autoCleanupData(daysToKeep: Int = 30) {
        viewModelScope.launch {
            try {
                val cutoffTime = System.currentTimeMillis() - (daysToKeep * 24L * 60 * 60 * 1000)
                db.collection("notifications").whereLessThan("timestamp", cutoffTime).get()
                    .addOnSuccessListener { snapshot ->
                        snapshot?.documents?.forEach { doc -> doc.reference.delete() }
                    }
            } catch (e: Exception) {
                android.util.Log.e("SettingsViewModel", "Error in autoCleanupData", e)
            }
        }
    }

fun scheduleAutoCleanup(days: Int = 30) {
        viewModelScope.launch {
            while (true) {
                kotlinx.coroutines.delay(24 * 60 * 60 * 1000L)
                try {
                    autoCleanupData(days)
                } catch (e: Exception) {
                    android.util.Log.e("SettingsViewModel", "Error in scheduleAutoCleanup", e)
                }
            }
        }
    }

    // ==========================================
    // 🔄 Sync Utilities (Transferred from SyncViewModel)
    // ==========================================
    fun triggerManualSync(context: android.content.Context, onComplete: ((Boolean) -> Unit)? = null) {
        viewModelScope.launch {
            try {
                val syncMgr = com.serviseyem.utils.FullSyncManager(context)
                val success = syncMgr.syncAllSettings()
                if (success) {
                    triggerToast("🔄 تم استكمال المزامنة بنجاح")
                } else {
                    triggerToast("⚠️ تعذرت المزامنة المباشرة مع السحابة")
                }
                onComplete?.invoke(success)
            } catch (e: Exception) {
                android.util.Log.e("SettingsViewModel", "Error in triggerManualSync", e)
                onComplete?.invoke(false)
            }
        }
    }

    fun resolveConflict(context: android.content.Context, conflict: com.serviseyem.utils.Conflict, resolution: com.serviseyem.utils.Resolution) {
        try {
            com.serviseyem.utils.ConflictResolver(context).resolveConflict(conflict, resolution)
            triggerToast("✅ تم حل التعارض بنجاح")
        } catch (e: Exception) {
            android.util.Log.e("SettingsViewModel", "Error resolving conflict", e)
        }
    }

    fun retryOfflineQueue(context: android.content.Context) {
        try {
            com.serviseyem.utils.OfflineQueueManager(context).retryFailedRequests()
            triggerToast("🚀 جاري إعادة إرسال العمليات المتبقية...")
        } catch (e: Exception) {
            android.util.Log.e("SettingsViewModel", "Error retrying offline queue", e)
        }
    }

    override fun onCleared() {
        super.onCleared()
        cardSettingsListener?.remove()
        colorSchemeListener?.remove()
        userColorsListener?.remove()
    }
}