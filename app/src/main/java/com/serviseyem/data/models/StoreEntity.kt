package com.serviseyem.data

import androidx.annotation.Keep

@Keep
data class StoreEntity(
    val id: String = "",
    val sectionId: String = "stores",
    val name: String = "",
    val description: String = "",
    val ownerId: String = "",
    val ownerName: String = "",
    val phone: String = "",
    val categoryId: String = "",
    val cityId: String = "",
    val localNeighborhood: String = "",
    val coverImage: String = "",
    val logoImage: String = "",
    val rating: Float = 5.0f,
    val numReviews: Int = 0,
    val isActive: Boolean = true,
    val isPinned: Boolean = false,
    val displayOrder: Int = 0,
    val maxImages: Int = 5,
    val workingHours: String = "9:00 AM - 10:00 PM",
    val latitude: Double = 15.3694,
    val longitude: Double = 44.1910,
    val isDeleted: Boolean = false,
    val deletedAt: Long? = null,
    val paymentEnabled: Boolean = true,
    val createdAt: Long = System.currentTimeMillis(),
    val passwordHash: String = "", // Secure salted PBKDF2 hash of the password
    val pdfFileUri: String = "",
    // TODO: Migrate to FirebaseStorageUploader
    val pdfFileBase64: String = "",
    val pdfStatus: String = "",
    val images: List<String> = emptyList(),
    val isApproved: Boolean = false,
    val isVip: Boolean = false,
    val isVerified: Boolean = false,
    val isRecommended: Boolean = false,
    val isChatDisabled: Boolean = false,
    val isNotificationsDisabled: Boolean = false,
    val productAttachmentsJson: String = "",
    val specialOffersJson: String = "",
    override val isBlocked: Boolean = false,
    override val blockReason: String = "",
    val backgroundColorHex: String = "",
    val commercialRegisterNo: String = "",
    val medicalLicenseNo: String = "",
    val providerType: String = "",
    val keywords: List<String> = emptyList()
) : com.serviseyem.data.models.SupervisedEntity {
    @get:com.google.firebase.firestore.Exclude
    val password: String get() = passwordHash
}
