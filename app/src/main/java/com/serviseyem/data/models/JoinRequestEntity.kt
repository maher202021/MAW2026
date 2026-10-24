package com.serviseyem.data.models

import androidx.annotation.Keep

@Keep
data class JoinRequestEntity(
    val id: String = "",
    val type: String = "PROVIDER", // "PROVIDER" | "STORE" | "RESTAURANT" | "MEDICAL" | "PROPERTY" | "JOB" | "CLIENT"
    val status: String = "PENDING", // "PENDING" | "APPROVED" | "REJECTED" | "ACTIVE"
    val fullName: String = "",
    val phone: String = "",
    val passwordHash: String = "",
    val city: String = "",
    val area: String = "",
    val neighborhood: String = "",
    val categoryId: String = "",
    val categoryName: String = "",
    val businessName: String = "",
    val ownerName: String = "",
    val jobTitle: String = "",
    val companyName: String = "",
    val propertyTitle: String = "",
    val price: Double = 0.0,
    val propertyType: String = "",
    val profileImage: String = "",
    val idCardImage: String = "",
    val workImages: List<String> = emptyList(),
    val coverImage: String = "",
    val logoImage: String = "",
    val approvalStatus: String = "PENDING", // "PENDING" | "APPROVED" | "REJECTED"
    val rejectionReason: String = "",
    val submittedAt: Long = System.currentTimeMillis(),
    val approvedAt: Long? = null,
    val rejectedAt: Long? = null,
    val approvedBy: String = "",
    val rejectedBy: String = "",
    val isActive: Boolean = false,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val jobRequirements: String = "",
    val salaryRange: String = ""
)
