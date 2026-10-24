package com.serviseyem.data

import androidx.annotation.Keep

@Keep
data class PropertyEntity(
    val id: String = "",
    val sectionId: String = "properties",
    val title: String = "",
    val description: String = "",
    val price: Double = 0.0,
    val currency: String = "YER",
    val type: String = "rent", // rent, sale
    val propertyType: String = "apartment", // apartment, house, land, shop
    val ownerId: String = "",
    val ownerName: String = "",
    val phone: String = "",
    val cityId: String = "",
    val localNeighborhood: String = "",
    val rating: Float = 5.0f,
    val numReviews: Int = 0,
    val isActive: Boolean = true,
    val isPinned: Boolean = false,
    val displayOrder: Int = 0,
    val latitude: Double = 15.3694,
    val longitude: Double = 44.1910,
    val images: List<String> = emptyList(),
    val isDeleted: Boolean = false,
    val deletedAt: Long? = null,
    val paymentEnabled: Boolean = true,
    val createdAt: Long = System.currentTimeMillis(),
    val passwordHash: String = "", // Secure salted PBKDF2 hash of the password
    val pdfFileUri: String = "",
    // TODO: Migrate to FirebaseStorageUploader
    val pdfFileBase64: String = "",
    val pdfStatus: String = "",
    val isApproved: Boolean = false,
    val maxImages: Int = 5,
    val isVip: Boolean = false,
    val isVerified: Boolean = false,
    val isRecommended: Boolean = false,
    val isChatDisabled: Boolean = false,
    val isNotificationsDisabled: Boolean = false,
    val productAttachmentsJson: String = "",
    override val isBlocked: Boolean = false,
    override val blockReason: String = ""
) : com.serviseyem.data.models.SupervisedEntity {
    @get:com.google.firebase.firestore.Exclude
    val password: String get() = passwordHash
}
