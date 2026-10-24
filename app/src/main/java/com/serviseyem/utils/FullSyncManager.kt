package com.serviseyem.utils

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import com.serviseyem.data.AdminSettingsEntity
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.Locale

import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 🔄 FullSyncManager
 * المركز الرئيسي لإدارة المزامنة الشاملة وحفظ واسترجاع إعدادات الأدمن، الثيمات،
 * واستمارات التسجيل وحقولها من وإلى Firebase Firestore مع التخزين المحلي الدائم.
 */
@Singleton
class FullSyncManager @Inject constructor(
    @ApplicationContext private val context: Context
) {

    private val firestore: FirebaseFirestore by lazy { FirebaseFirestore.getInstance() }
    private val prefs: SharedPreferences = context.getSharedPreferences("app_sync_manager_prefs", Context.MODE_PRIVATE)
    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    private val _lastSyncTimestamp = MutableStateFlow(prefs.getLong(KEY_LAST_SYNC_TS, 0L))
    val lastSyncTimestamp: StateFlow<Long> = _lastSyncTimestamp.asStateFlow()

    private val _isSyncing = MutableStateFlow(false)
    val isSyncing: StateFlow<Boolean> = _isSyncing.asStateFlow()

    private var autoSyncJob: Job? = null

    companion object {
        private const val TAG = "FullSyncManager"
        private const val KEY_LAST_SYNC_TS = "key_last_sync_timestamp"
        private const val KEY_LOCAL_SETTINGS_CACHE = "key_local_admin_settings_cache"
        private const val COLLECTION_ADMIN_SETTINGS = "settings"
        private const val DOC_MAIN_CONFIG = "main_settings"
        private const val DOC_FORMS_CONFIG = "forms_config"
        private const val DOC_THEME_CONFIG = "theme_config"
        private const val SYNC_INTERVAL_MS = 5 * 60 * 1000L // 5 minutes
    }

    /**
     * بدء المزامنة التلقائية كل 5 دقائق (استرجاع آمن من السحابة للكاش المحلي دون الكتابة العشوائية)
     */
    fun startAutoSync() {
        autoSyncJob?.cancel()
        autoSyncJob = scope.launch {
            while (isActive) {
                if (isSyncRequired()) {
                    pullRemoteSettingsToLocalCache()
                }
                delay(SYNC_INTERVAL_MS)
            }
        }
    }

    /**
     * سحب الإعدادات من السحابة وتحديث الكاش المحلي بأمان (مخصص للمزامنة الدورية في الخلفية)
     * لا يقوم بالكتابة على Firestore، مما يحمي إعدادات الأدمن ويتجنب أخطاء PERMISSION_DENIED لدى العملاء.
     */
    suspend fun pullRemoteSettingsToLocalCache(): Boolean = withContext(Dispatchers.IO) {
        _isSyncing.value = true
        try {
            val remoteData = restoreAllSettings()
            val now = System.currentTimeMillis()
            if (remoteData != null) {
                val safeMap = remoteData.mapValues { (_, v) ->
                    when (v) {
                        null, is Boolean, is Number, is String -> v
                        else -> v.toString()
                    }
                }
                val jsonStr = org.json.JSONObject(safeMap).toString()
                prefs.edit()
                    .putString(KEY_LOCAL_SETTINGS_CACHE, jsonStr)
                    .putLong(KEY_LAST_SYNC_TS, now)
                    .apply()
            } else {
                prefs.edit().putLong(KEY_LAST_SYNC_TS, now).apply()
            }
            _lastSyncTimestamp.value = now
            true
        } catch (e: Exception) {
            Log.w(TAG, "Background pullRemoteSettingsToLocalCache note: ${e.message}")
            false
        } finally {
            _isSyncing.value = false
        }
    }

    fun stopAutoSync() {
        autoSyncJob?.cancel()
        autoSyncJob = null
    }

    /**
     * 1. مزامنة جميع الإعدادات (الأدمن، الثيم، الاستمارات، الحقول)
     */
    suspend fun syncAllSettings(): Boolean = withContext(Dispatchers.IO) {
        _isSyncing.value = true
        try {
            val adminSync = syncAdminSettingsInternal()
            val themeSync = syncThemeSettingsInternal()
            val formSync = syncFormSettingsInternal()

            val success = adminSync && themeSync && formSync
            if (success) {
                val now = System.currentTimeMillis()
                prefs.edit().putLong(KEY_LAST_SYNC_TS, now).apply()
                _lastSyncTimestamp.value = now
                Log.d(TAG, "All settings synchronized successfully to Firestore.")
            }
            success
        } catch (e: Exception) {
            Log.e(TAG, "Error in syncAllSettings: ${e.message}", e)
            false
        } finally {
            _isSyncing.value = false
        }
    }

    /**
     * 2. مزامنة إعدادات الأدمن
     */
    suspend fun syncAdminSettings(settings: AdminSettingsEntity? = null): Boolean = withContext(Dispatchers.IO) {
        try {
            syncAdminSettingsInternal(settings)
        } catch (e: Exception) {
            Log.e(TAG, "Error syncing admin settings: ${e.message}", e)
            false
        }
    }

    private suspend fun syncAdminSettingsInternal(settings: AdminSettingsEntity? = null): Boolean = suspendCancellableCoroutine { continuation ->
        val dataToSync = HashMap<String, Any>()
        if (settings != null) {
            dataToSync["appName"] = settings.appName
            dataToSync["welcomeMessage"] = settings.welcomeMessage
            dataToSync["footerMessage"] = settings.footerMessage
            dataToSync["footerBgColorHex"] = settings.footerBgColorHex
            dataToSync["footerItemsOrder"] = settings.footerItemsOrder
            dataToSync["showInfoIcon"] = settings.showInfoIcon
            dataToSync["showBookingsIcon"] = settings.showBookingsIcon
            dataToSync["showLangIcon"] = settings.showLangIcon
            dataToSync["showAdminIcon"] = settings.showAdminIcon
            dataToSync["showFooterText"] = settings.showFooterText
            dataToSync["activeThemeId"] = settings.activeThemeId
            dataToSync["customPrimaryHex"] = settings.customPrimaryHex
            dataToSync["customSecondaryHex"] = settings.customSecondaryHex
            dataToSync["customBackgroundHex"] = settings.customBackgroundHex
            dataToSync["customSurfaceHex"] = settings.customSurfaceHex
            dataToSync["isMaintenanceActive"] = settings.isMaintenanceActive
            dataToSync["supportPhone"] = settings.supportPhone
            dataToSync["supportWhatsapp"] = settings.supportWhatsapp
            dataToSync["supportEmail"] = settings.supportEmail
            dataToSync["adminUsername"] = settings.adminUsername
            dataToSync["allowVoiceInput"] = settings.allowVoiceInput
            dataToSync["allowTextToSpeech"] = settings.allowTextToSpeech
            dataToSync["isAssistantEnabled"] = settings.isAssistantEnabled
            dataToSync["isMapFeatureEnabled"] = settings.isMapFeatureEnabled
            dataToSync["registrationRequirements"] = settings.registrationRequirements
            dataToSync["updatedAt"] = System.currentTimeMillis()
        } else {
            dataToSync["updatedAt"] = System.currentTimeMillis()
        }

        firestore.collection(COLLECTION_ADMIN_SETTINGS)
            .document(DOC_MAIN_CONFIG)
            .set(dataToSync, SetOptions.merge())
            .addOnSuccessListener {
                if (continuation.isActive) continuation.resume(true) {}
            }
            .addOnFailureListener { err ->
                Log.w(TAG, "Failed admin settings sync: ${err.message}")
                if (continuation.isActive) continuation.resume(false) {}
            }
    }

    /**
     * 3. مزامنة الألوان والثيمات
     */
    suspend fun syncThemeSettings(themeId: String? = null, primaryHex: String? = null, secondaryHex: String? = null): Boolean = withContext(Dispatchers.IO) {
        try {
            syncThemeSettingsInternal(themeId, primaryHex, secondaryHex)
        } catch (e: Exception) {
            Log.e(TAG, "Error in syncThemeSettings: ${e.message}", e)
            false
        }
    }

    private suspend fun syncThemeSettingsInternal(themeId: String? = null, primaryHex: String? = null, secondaryHex: String? = null): Boolean = suspendCancellableCoroutine { continuation ->
        val themeMap = hashMapOf<String, Any>(
            "updatedAt" to System.currentTimeMillis()
        )
        if (!themeId.isNullOrBlank()) themeMap["activeThemeId"] = themeId.trim()
        if (!primaryHex.isNullOrBlank()) themeMap["customPrimaryHex"] = primaryHex.trim()
        if (!secondaryHex.isNullOrBlank()) themeMap["customSecondaryHex"] = secondaryHex.trim()

        firestore.collection(COLLECTION_ADMIN_SETTINGS)
            .document(DOC_THEME_CONFIG)
            .set(themeMap, SetOptions.merge())
            .addOnSuccessListener {
                if (continuation.isActive) continuation.resume(true) {}
            }
            .addOnFailureListener {
                if (continuation.isActive) continuation.resume(false) {}
            }
    }

    /**
     * 4. مزامنة استمارات التسجيل لجميع الأقسام
     */
    suspend fun syncFormSettings(formsData: Map<String, Any>? = null): Boolean = withContext(Dispatchers.IO) {
        try {
            syncFormSettingsInternal(formsData)
        } catch (e: Exception) {
            Log.e(TAG, "Error in syncFormSettings: ${e.message}", e)
            false
        }
    }

    private suspend fun syncFormSettingsInternal(formsData: Map<String, Any>? = null): Boolean = suspendCancellableCoroutine { continuation ->
        val data = if (!formsData.isNullOrEmpty()) {
            HashMap(formsData).apply { put("updatedAt", System.currentTimeMillis()) }
        } else {
            mapOf("updatedAt" to System.currentTimeMillis())
        }

        firestore.collection(COLLECTION_ADMIN_SETTINGS)
            .document(DOC_FORMS_CONFIG)
            .set(data, SetOptions.merge())
            .addOnSuccessListener {
                if (continuation.isActive) continuation.resume(true) {}
            }
            .addOnFailureListener {
                if (continuation.isActive) continuation.resume(false) {}
            }
    }

    /**
     * 5 & 6. مزامنة حقول استمارة معينة وترتيبها وإلزاميتها
     */
    suspend fun syncFormFields(section: String, fieldsDefinition: String): Boolean = withContext(Dispatchers.IO) {
        suspendCancellableCoroutine { continuation ->
            val payload = mapOf(
                "${section.lowercase(Locale.ROOT)}_fields" to fieldsDefinition,
                "lastUpdatedSection" to section,
                "updatedAt" to System.currentTimeMillis()
            )

            firestore.collection(COLLECTION_ADMIN_SETTINGS)
                .document(DOC_FORMS_CONFIG)
                .set(payload, SetOptions.merge())
                .addOnSuccessListener {
                    if (continuation.isActive) continuation.resume(true) {}
                }
                .addOnFailureListener {
                    if (continuation.isActive) continuation.resume(false) {}
                }
        }
    }

    /**
     * 8. استرجاع جميع الإعدادات من Firebase Firestore
     */
    suspend fun restoreAllSettings(): Map<String, Any?>? = withContext(Dispatchers.IO) {
        suspendCancellableCoroutine { continuation ->
            firestore.collection(COLLECTION_ADMIN_SETTINGS)
                .document(DOC_MAIN_CONFIG)
                .get()
                .addOnSuccessListener { snapshot ->
                    if (snapshot.exists()) {
                        if (continuation.isActive) continuation.resume(snapshot.data) {}
                    } else {
                        if (continuation.isActive) continuation.resume(null) {}
                    }
                }
                .addOnFailureListener { err ->
                    Log.e(TAG, "Failed to restore settings: ${err.message}")
                    if (continuation.isActive) continuation.resume(null) {}
                }
        }
    }

    /**
     * الحصول على وقت آخر مزامنة بصيغة نصية منسقة
     */
    fun getLastSyncTime(): String {
        val ts = prefs.getLong(KEY_LAST_SYNC_TS, 0L)
        if (ts == 0L) return "لم تتم المزامنة بعد"
        // ✨ م2: استخدام DateFormatter الموحد
        return DateFormatter.formatCustom(ts, "yyyy/MM/dd - hh:mm a")
    }

    /**
     * التحقق من الحاجة للمزامنة
     */
    fun isSyncRequired(): Boolean {
        val lastTs = prefs.getLong(KEY_LAST_SYNC_TS, 0L)
        if (lastTs <= 0L) return true
        val elapsed = System.currentTimeMillis() - lastTs
        return elapsed < 0L || elapsed >= SYNC_INTERVAL_MS
    }

    /**
     * مزامنة فورية
     */
    fun forceSync(onComplete: (Boolean) -> Unit = {}) {
        scope.launch {
            val res = syncAllSettings()
            withContext(Dispatchers.Main) {
                onComplete(res)
            }
        }
    }

    /**
     * مسح التخزين المؤقت المحلي
     */
    fun clearLocalCache() {
        prefs.edit().remove(KEY_LOCAL_SETTINGS_CACHE).remove(KEY_LAST_SYNC_TS).apply()
        _lastSyncTimestamp.value = 0L
    }
}
