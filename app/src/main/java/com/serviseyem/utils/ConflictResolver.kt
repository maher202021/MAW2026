package com.serviseyem.utils

import android.content.Context
import android.util.Log
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

data class Conflict(
    val entityId: String,
    val entityType: String, // "ADMIN_SETTINGS", "PROVIDER", "BOOKING", "FORM_CONFIG"
    val localVersion: Int,
    val cloudVersion: Int,
    val localData: Map<String, Any?>,
    val cloudData: Map<String, Any?>,
    val timestamp: Long = System.currentTimeMillis()
)

enum class Resolution {
    USE_CLOUD,
    USE_LOCAL,
    MERGE
}

data class ConflictAuditEntry(
    val id: String = java.util.UUID.randomUUID().toString(),
    val entityId: String,
    val entityType: String,
    val resolution: Resolution,
    val resolvedAt: Long = System.currentTimeMillis(),
    val notes: String = ""
)

/**
 * ⚖️ ConflictResolver
 * كشف وإدارة وحل التعارضات الناتجة عن التعديل المتزامن محلياً وسحابياً
 */
class ConflictResolver(@Suppress("UNUSED_PARAMETER") context: Context? = null) {

    private val _pendingConflicts = MutableStateFlow<List<Conflict>>(emptyList())
    val pendingConflicts: StateFlow<List<Conflict>> = _pendingConflicts.asStateFlow()

    private val _auditLogs = MutableStateFlow<List<ConflictAuditEntry>>(emptyList())
    val auditLogs: StateFlow<List<ConflictAuditEntry>> = _auditLogs.asStateFlow()

    companion object {
        private const val TAG = "ConflictResolver"
        private const val MAX_AUDIT_LOGS = 200
    }

    /**
     * كشف التعارضات بين النسخة المحلية والسحابية بناء على الإصدار أو التوقيت
     */
    fun detectConflicts(
        entityId: String,
        entityType: String,
        localData: Map<String, Any?>,
        cloudData: Map<String, Any?>,
        localVer: Int = 1,
        cloudVer: Int = 1
    ): Conflict? {
        val allKeys = localData.keys + cloudData.keys
        val hasDifference = allKeys.any { k ->
            localData[k] != cloudData[k]
        }

        if (hasDifference && localVer != cloudVer) {
            val conflict = Conflict(
                entityId = entityId,
                entityType = entityType,
                localVersion = localVer,
                cloudVersion = cloudVer,
                localData = localData,
                cloudData = cloudData
            )
            _pendingConflicts.update { current ->
                current.filterNot { it.entityId == entityId && it.entityType == entityType } + conflict
            }
            return conflict
        }
        return null
    }

    fun getPendingConflicts(): List<Conflict> = _pendingConflicts.value

    /**
     * حل تعارض معين وتطبيق القرار
     */
    fun resolveConflict(
        conflict: Conflict,
        resolution: Resolution,
        onResolved: ((Map<String, Any?>) -> Unit)? = null
    ): Map<String, Any?> {
        val resultData = when (resolution) {
            Resolution.USE_CLOUD -> conflict.cloudData
            Resolution.USE_LOCAL -> conflict.localData
            Resolution.MERGE -> mergeChanges(conflict)
        }

        // إزالة التعارض من القائمة المعلقة بشكل ذري
        _pendingConflicts.update { current ->
            current.filterNot {
                it.entityId == conflict.entityId && it.entityType == conflict.entityType
            }
        }

        // تسجيل في سجل التدقيق مع سقف أقصى للذاكرة
        val auditEntry = ConflictAuditEntry(
            entityId = conflict.entityId,
            entityType = conflict.entityType,
            resolution = resolution,
            notes = "Resolved with $resolution successfully"
        )
        _auditLogs.update { current ->
            (current + auditEntry).takeLast(MAX_AUDIT_LOGS)
        }

        Log.d(TAG, "Conflict resolved for ${conflict.entityId} using $resolution")
        onResolved?.invoke(resultData)
        return resultData
    }

    /**
     * دمج التغييرات المحلية والسحابية (مقارنة الطوابع الزمنية لكل حقل)
     */
    fun mergeChanges(conflict: Conflict): Map<String, Any?> {
        val merged = HashMap<String, Any?>()
        merged.putAll(conflict.cloudData)

        val localTime = (conflict.localData["updatedAt"] as? Number)?.toLong() 
            ?: (conflict.localData["timestamp"] as? Number)?.toLong() 
            ?: conflict.timestamp

        val cloudTime = (conflict.cloudData["updatedAt"] as? Number)?.toLong() 
            ?: (conflict.cloudData["timestamp"] as? Number)?.toLong() 
            ?: 0L

        val isLocalNewerVersion = conflict.localVersion > conflict.cloudVersion

        conflict.localData.forEach { (key, localVal) ->
            if (localVal != null) {
                val fieldLocalTs = (conflict.localData["${key}_updatedAt"] as? Number)?.toLong() ?: localTime
                val fieldCloudTs = (conflict.cloudData["${key}_updatedAt"] as? Number)?.toLong() ?: cloudTime

                if (fieldLocalTs >= fieldCloudTs) {
                    if (localVal is String) {
                        if (localVal.isNotBlank() || fieldLocalTs > fieldCloudTs || isLocalNewerVersion || !merged.containsKey(key)) {
                            merged[key] = localVal
                        }
                    } else {
                        merged[key] = localVal
                    }
                }
            }
        }
        merged["lastMergedAt"] = System.currentTimeMillis()
        return merged
    }

    /**
     * حل جميع التعارضات باستخدام السحابة
     */
    fun resolveAllWithCloud(onResolved: (List<Pair<Conflict, Map<String, Any?>>>) -> Unit) {
        val snapshot = _pendingConflicts.value.toList()
        val list = snapshot.map { c ->
            Pair(c, resolveConflict(c, Resolution.USE_CLOUD))
        }
        onResolved(list)
    }

    /**
     * حل جميع التعارضات باستخدام المحلي
     */
    fun resolveAllWithLocal(onResolved: (List<Pair<Conflict, Map<String, Any?>>>) -> Unit) {
        val snapshot = _pendingConflicts.value.toList()
        val list = snapshot.map { c ->
            Pair(c, resolveConflict(c, Resolution.USE_LOCAL))
        }
        onResolved(list)
    }
}
