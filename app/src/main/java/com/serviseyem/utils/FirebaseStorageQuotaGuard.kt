package com.serviseyem.utils

import com.serviseyem.utils.*

/**
 * 📊 Firebase Storage Quota Guard & Admin Upload Management
 * Keeps storage usage strictly below Spark Free Tier (5GB limit).
 * Tracks 75% warning, 85% urgent alert, and 95% emergency upload shutdown.
 */
data class StorageUsageReport(
    val totalUsedBytes: Long = 0L,
    val totalFilesCount: Int = 0,
    val coverPhotosBytes: Long = 0L,
    val avatarPhotosBytes: Long = 0L,
    val servicePhotosBytes: Long = 0L,
    val chatMediaBytes: Long = 0L,
    val maxSparkLimitBytes: Long = 5_000_000_000L // 5GB Spark limit
) {
    val usagePercentage: Float
        get() = ((totalUsedBytes.toDouble() / maxSparkLimitBytes.toDouble()) * 100).toFloat().coerceIn(0f, 100f)

    val remainingBytes: Long
        get() = (maxSparkLimitBytes - totalUsedBytes).coerceAtLeast(0L)

    val isWarning75: Boolean get() = usagePercentage >= 75f
    val isUrgent85: Boolean get() = usagePercentage >= 85f
    val isCritical95: Boolean get() = usagePercentage >= 95f
}

data class AdminUploadPermissions(
    val isGlobalUploadEnabled: Boolean = true,
    val isAdminUploadEnabled: Boolean = true,
    val isUserUploadEnabled: Boolean = true,
    val isStoreUploadEnabled: Boolean = true,
    val isMedicalUploadEnabled: Boolean = true,
    val isRestaurantUploadEnabled: Boolean = true,
    val isTechnicianUploadEnabled: Boolean = true,
    val isRealEstateUploadEnabled: Boolean = true,
    val isJobsUploadEnabled: Boolean = true,
    val maxSingleFileSizeMB: Int = 5,
    val maxFilesPerUser: Int = 20
)

object FirebaseStorageQuotaGuard {

    private var currentPermissions = AdminUploadPermissions()
    private var currentUsageReport = StorageUsageReport()

    fun updateUsage(report: StorageUsageReport) {
        currentUsageReport = report
    }

    fun updatePermissions(permissions: AdminUploadPermissions) {
        currentPermissions = permissions
    }

    fun getCurrentUsage(): StorageUsageReport = currentUsageReport
    fun getCurrentPermissions(): AdminUploadPermissions = currentPermissions

    /**
     * Checks whether upload is allowed for a specific user category
     */
    fun canUpload(userType: String, fileSizeBytes: Long): Pair<Boolean, String> {
        if (!currentPermissions.isGlobalUploadEnabled) {
            return Pair(false, "تم إيقاف رفع الصور والملفات مؤقتاً بواسطة الإدارة.")
        }

        if (currentUsageReport.isCritical95) {
            return Pair(false, "تم الوصول للحد الأقصى للتخزين (95%). تم تعطيل رفع الصور تلقائياً لحماية الخدمة.")
        }

        val maxAllowedBytes = currentPermissions.maxSingleFileSizeMB * 1024 * 1024L
        if (fileSizeBytes > maxAllowedBytes) {
            return Pair(false, "حجم الملف يتجاوز الحد المسموح به (${currentPermissions.maxSingleFileSizeMB} ميجابايت).")
        }

        val allowed = when (userType.uppercase()) {
            "ADMIN" -> currentPermissions.isAdminUploadEnabled
            "STORE" -> currentPermissions.isStoreUploadEnabled
            "MEDICAL" -> currentPermissions.isMedicalUploadEnabled
            "RESTAURANT" -> currentPermissions.isRestaurantUploadEnabled
            "TECHNICIAN" -> currentPermissions.isTechnicianUploadEnabled
            "REAL_ESTATE" -> currentPermissions.isRealEstateUploadEnabled
            "JOB" -> currentPermissions.isJobsUploadEnabled
            else -> currentPermissions.isUserUploadEnabled
        }

        return if (allowed) {
            Pair(true, "مسموح")
        } else {
            Pair(false, "غير مسموح برفع الصور لهذه الفئة حالياً بواسطة الأدمن.")
        }
    }
}
