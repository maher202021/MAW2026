package com.serviseyem.domain.entities

import androidx.annotation.Keep

/**
 * 🏛️ Domain Entities for Dashboards, Products, Favorites, Ratings & Gallery
 * Pure Kotlin entities independent of Android dependencies.
 */

@Keep
data class DashboardStatsEntity(
    val totalViews: Int = 0,
    val activeBookingsCount: Int = 0,
    val completedBookingsCount: Int = 0,
    val averageRating: Double = 5.0,
    val totalReviewsCount: Int = 0,
    val totalRevenueYer: Double = 0.0,
    val totalProductsCount: Int = 0,
    val pendingRequestsCount: Int = 0
)

@Keep
data class ProductItemEntity(
    val id: String = "",
    val ownerId: String = "",
    val title: String = "",
    val description: String = "",
    val category: String = "",
    val priceYer: Double = 0.0,
    val imageUrl: String = "",
    val isAvailable: Boolean = true,
    val createdAt: Long = System.currentTimeMillis()
)

@Keep
data class FavoriteItemEntity(
    val id: String = "",
    val userId: String = "",
    val targetId: String = "",
    val targetType: String = "", // PROVIDER, STORE, RESTAURANT, MEDICAL, PROPERTY, JOB
    val title: String = "",
    val category: String = "",
    val city: String = "",
    val imageUrl: String = "",
    val rating: Double = 5.0,
    val createdAt: Long = System.currentTimeMillis()
)

@Keep
@Deprecated("استخدم RatingEntity كنموذج موحد للتقييمات", ReplaceWith("RatingEntity"))
data class RatingReviewEntity(
    val id: String = "",
    val targetId: String = "",
    val authorName: String = "",
    val authorPhone: String = "",
    val rating: Double = 5.0,
    val comment: String = "",
    val dateTimestamp: Long = System.currentTimeMillis()
) {
    val userName: String get() = authorName
    val userPhone: String get() = authorPhone
    val timestamp: Long get() = dateTimestamp

    fun toRatingEntity(targetType: String = "STORE"): com.serviseyem.data.RatingEntity {
        return com.serviseyem.data.RatingEntity(
            id = id,
            targetId = targetId,
            targetType = targetType,
            userName = authorName,
            userPhone = authorPhone,
            rating = rating.toFloat(),
            comment = comment,
            timestamp = dateTimestamp
        )
    }
}

@Keep
data class GalleryAlbumEntity(
    val id: String = "",
    val ownerId: String = "",
    val title: String = "",
    val imageUrls: List<String> = emptyList(),
    val createdAt: Long = System.currentTimeMillis()
)
