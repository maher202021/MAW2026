package com.serviseyem.data

import androidx.annotation.Keep

@Keep
data class ReportEntity(
    val id: String = "",
    val providerId: String = "",
    val providerName: String = "",
    val targetType: String = "PROVIDER", // PROVIDER, STORE, RESTAURANT, MEDICAL, PROPERTY, JOB, GENERAL
    val targetId: String = "",
    val targetName: String = "",
    val reporterName: String = "",
    val reporterPhone: String = "",
    val reason: String = "",
    val explanation: String = "",
    val content: String = "",
    // TODO: Migrate to FirebaseStorageUploader
    val proofPhotoBase64: String = "",
    val status: String = "PENDING", // PENDING, INVESTIGATING, RESOLVED, REJECTED
    val adminActionNotes: String = "",
    val timestamp: Long = System.currentTimeMillis()
)

@Keep
data class ActivityLogEntity(
    val id: String = "",
    val action: String = "",
    val timestamp: Long = 0L
)
