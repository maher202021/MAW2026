package com.serviseyem.data

import androidx.annotation.Keep

@Keep
data class ProviderEntity(
    val id: String = "",
    val name: String = "",
    val phone: String = "",
    val categoryId: String = "",
    val area: String = "",
    val isVip: Boolean = false,
    val subscriptionStatus: String = "PENDING", // e.g., "APPROVED"
    val isAvailable: Boolean = true,
    val cityId: String = "",
    val localNeighborhood: String = "",
    val rating: Float = 5.0f,
    val points: Int = 0,
    val isVerified: Boolean = true,
    val isRecommended: Boolean = true,
    val numReviews: Int = 0,
    val coverImage: String = "",
    val profileImage: String = "",
    val previewPrice: Double = 1500.0,
    val latitude: Double = 15.3694,
    val longitude: Double = 44.1910,
    val subscriptionExpiry: Long = System.currentTimeMillis() + (30L * 24 * 60 * 60 * 1000),
    // TODO: Migrate to FirebaseStorageUploader
    val workPhotosBase64: List<String> = emptyList(),
    val productAttachmentsJson: String = "",
    val specialOffersJson: String = "",
    val customCategoryName: String = "",
    val profession: String = "",
    val specialization: String = "",
    val chatRecipientId: String = "",
    override val isBlocked: Boolean = false,
    override val blockReason: String = "",
    val isChatDisabled: Boolean = false,
    val isNotificationsDisabled: Boolean = false,
    val isPaymentRequired: Boolean = false,
    val passwordHash: String = "",
    val isDeleted: Boolean = false,
    val deletedAt: Long? = null,
    val providerType: String = "",
    val createdAt: Long = System.currentTimeMillis()
) : com.serviseyem.data.models.SupervisedEntity {
    @get:com.google.firebase.firestore.Exclude
    val password: String get() = passwordHash
    @get:com.google.firebase.firestore.Exclude
    val portfolioImages: List<String> get() = workPhotosBase64
    @get:com.google.firebase.firestore.Exclude
    val hasValidCoordinates: Boolean get() = latitude != 0.0 && longitude != 0.0
}

@Keep
data class PendingProviderEntity(
    val id: String = "",
    val name: String = "",
    val phone: String = "",
    val categoryId: String = "",
    val area: String = "",
    val localNeighborhood: String = "",
    val status: String = "PENDING",
    val reason: String = "",
    // TODO: Migrate to FirebaseStorageUploader
    val idPhotoBase64: String = "",
    // TODO: Migrate to FirebaseStorageUploader
    val selfiePhotoBase64: String = "",
    // TODO: Migrate to FirebaseStorageUploader
    val workPhotosBase64: List<String> = emptyList(),
    val productAttachmentsJson: String = "",
    val customCategoryName: String = "",
    val profession: String = "",
    val specialization: String = "",
    val chatRecipientId: String = "",
    val passwordHash: String = "",
    val providerType: String = "",
    val keywords: List<String> = emptyList()
) {
    @get:com.google.firebase.firestore.Exclude
    val password: String get() = passwordHash
}
