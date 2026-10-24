package com.serviseyem.utils

import android.content.Context
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

enum class SyncStatus {
    SYNCED,        // متزامن بالكامل
    SYNCING,       // جاري المزامنة
    CONFLICT,      // تعارض
    NOT_SYNCED,    // غير متزامن
    ERROR          // خطأ
}

data class SyncError(
    val id: String = java.util.UUID.randomUUID().toString(),
    val message: String,
    val timestamp: Long = System.currentTimeMillis(),
    val module: String = "GENERAL",
    val exception: Throwable? = null
)

/**
 * 📊 SyncStatusTracker
 * تتبع وإدارة حالة المزامنة وعرض الإحصائيات والأخطاء والتقدم في واجهة المستخدم.
 */
class SyncStatusTracker(private val context: Context) {

    private val _syncStatus = MutableStateFlow(SyncStatus.NOT_SYNCED)
    val syncStatus: StateFlow<SyncStatus> = _syncStatus.asStateFlow()

    private val _lastSyncTime = MutableStateFlow(0L)
    val lastSyncTime: StateFlow<Long> = _lastSyncTime.asStateFlow()

    private val _syncedItemsCount = MutableStateFlow(0)
    val syncedItemsCount: StateFlow<Int> = _syncedItemsCount.asStateFlow()

    private val _pendingItemsCount = MutableStateFlow(0)
    val pendingItemsCount: StateFlow<Int> = _pendingItemsCount.asStateFlow()

    private val _syncProgress = MutableStateFlow(0f)
    val syncProgress: StateFlow<Float> = _syncProgress.asStateFlow()

    private val _errors = MutableStateFlow<List<SyncError>>(emptyList())
    val errors: StateFlow<List<SyncError>> = _errors.asStateFlow()

    fun getSyncStatus(): SyncStatus = _syncStatus.value

    fun getLastSyncTime(): Long = _lastSyncTime.value

    fun getSyncedItemsCount(): Int = _syncedItemsCount.value

    fun getPendingItemsCount(): Int = _pendingItemsCount.value

    fun getSyncProgress(): Float = _syncProgress.value

    fun getErrors(): List<SyncError> = _errors.value

    fun updateStatus(status: SyncStatus) {
        _syncStatus.value = status
    }

    fun updateProgress(progress: Float, syncedCount: Int, pendingCount: Int) {
        _syncProgress.value = progress.coerceIn(0f, 1f)
        _syncedItemsCount.value = syncedCount
        _pendingItemsCount.value = pendingCount
    }

    fun recordSuccessfulSync(syncedCount: Int) {
        _syncStatus.value = SyncStatus.SYNCED
        _lastSyncTime.value = System.currentTimeMillis()
        _syncedItemsCount.value = syncedCount
        _pendingItemsCount.value = 0
        _syncProgress.value = 1f
    }

    fun recordError(message: String, module: String = "SYNC", throwable: Throwable? = null) {
        _syncStatus.value = SyncStatus.ERROR
        val error = SyncError(message = message, module = module, exception = throwable)
        _errors.value = _errors.value + error
    }

    fun clearErrors() {
        _errors.value = emptyList()
        if (_syncStatus.value == SyncStatus.ERROR) {
            _syncStatus.value = SyncStatus.NOT_SYNCED
        }
    }

    fun retryFailedSync(action: () -> Unit) {
        clearErrors()
        _syncStatus.value = SyncStatus.SYNCING
        action()
    }

    fun manualSync(action: () -> Unit) {
        _syncStatus.value = SyncStatus.SYNCING
        _syncProgress.value = 0.1f
        action()
    }
}
