package com.serviseyem.data

import com.serviseyem.utils.*
import androidx.annotation.Keep
import com.serviseyem.utils.EntityIdGenerator

// @Deprecated: Kept for backward compatibility across modules. Refactor usages to com.serviseyem.data.models.ChatChannel.
typealias ChatChannelEntity = com.serviseyem.data.models.ChatChannel

@Keep
enum class BusinessType(val titleArabic: String, val icon: String, val sectionId: String) {
    TECHNICIAN("فني / مقدم خدمة", "🛠️", "services"),
    STORE("متجر / مركز تجاري", "🏪", "stores"),
    RESTAURANT("مطعم / كافيه", "🍔", "restaurants"),
    MEDICAL("مركز طبي / عيادة / صيدلية", "🏥", "medical"),
    REAL_ESTATE("مكتب عقاري / مالي", "🏠", "properties"),
    JOB_POSTER("معلن وظائف / شركة", "💼", "jobs")
}

/**
 * 🏢 Unified Business Account Model
 * Unifies ProviderEntity, StoreEntity, PropertyEntity, and JobEntity into a single standardized model
 */
@Keep
data class UnifiedBusinessAccount(
    val id: String = EntityIdGenerator.generateStoreId(),
    val businessType: BusinessType = BusinessType.STORE,
    val name: String = "",
    val description: String = "",
    val phone: String = "",
    val ownerName: String = "",
    val categoryId: String = "",
    val cityId: String = "",
    val neighborhood: String = "",
    val coverImage: String = "",
    val logoImage: String = "",
    val rating: Float = 5.0f,
    val numReviews: Int = 0,
    val isVerified: Boolean = true,
    val isVip: Boolean = false,
    val isRecommended: Boolean = true,
    val workingHours: String = "9:00 AM - 10:00 PM",
    val latitude: Double = 15.3694,
    val longitude: Double = 44.1910,
    val password: String = "",
    val isDeleted: Boolean = false,
    val deletedAt: Long? = null,
    val deletedBy: String? = null,
    val deleteReason: String? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val isBookingEnabled: Boolean = true,
    val isInstantOrdersEnabled: Boolean = true,
    val isChatEnabled: Boolean = true,
    val rawProvider: ProviderEntity? = null,
    val rawStore: StoreEntity? = null,
    val rawProperty: PropertyEntity? = null,
    val rawJob: JobEntity? = null
) {
    val city: String get() = cityId
    val specialty: String get() = description
    val isActive: Boolean get() = isBookingEnabled && !isDeleted
    val logoUrl: String get() = logoImage
    val reviewsCount: Int get() = numReviews
    companion object {
        fun fromProvider(p: ProviderEntity): UnifiedBusinessAccount {
            return UnifiedBusinessAccount(
                id = p.id.ifBlank { EntityIdGenerator.generateProviderId() },
                businessType = BusinessType.TECHNICIAN,
                name = p.name,
                description = p.profession.ifBlank { p.specialization },
                phone = p.phone,
                ownerName = p.name,
                categoryId = p.categoryId,
                cityId = p.cityId,
                neighborhood = p.localNeighborhood.ifBlank { p.area },
                coverImage = p.coverImage,
                logoImage = p.profileImage,
                rating = p.rating,
                numReviews = p.numReviews,
                isVerified = p.isVerified || p.subscriptionStatus == "APPROVED" || p.isAvailable,
                isVip = p.isVip,
                isRecommended = p.isRecommended,
                password = p.password,
                isDeleted = p.isDeleted,
                deletedAt = p.deletedAt,
                latitude = p.latitude,
                longitude = p.longitude,
                rawProvider = p
            )
        }

        fun fromStore(s: StoreEntity, sectionId: String = "stores"): UnifiedBusinessAccount {
            val type = when (sectionId) {
                "restaurants" -> BusinessType.RESTAURANT
                "medical" -> BusinessType.MEDICAL
                else -> BusinessType.STORE
            }
            return UnifiedBusinessAccount(
                id = s.id.ifBlank { EntityIdGenerator.generateStoreId() },
                businessType = type,
                name = s.name,
                description = s.description,
                phone = s.phone,
                ownerName = s.ownerName,
                categoryId = s.categoryId,
                cityId = s.cityId,
                neighborhood = s.localNeighborhood,
                coverImage = s.coverImage,
                logoImage = s.logoImage,
                rating = s.rating,
                numReviews = s.numReviews,
                isVerified = s.isVerified || s.isActive,
                isVip = s.isVip,
                isRecommended = s.isRecommended,
                workingHours = s.workingHours,
                password = s.password,
                isDeleted = s.isDeleted,
                deletedAt = s.deletedAt,
                latitude = s.latitude,
                longitude = s.longitude,
                createdAt = s.createdAt,
                rawStore = s
            )
        }

        fun fromProperty(p: PropertyEntity): UnifiedBusinessAccount {
            return UnifiedBusinessAccount(
                id = p.id.ifBlank { EntityIdGenerator.generatePropertyId() },
                businessType = BusinessType.REAL_ESTATE,
                name = p.title,
                description = p.description,
                phone = p.phone,
                ownerName = p.ownerName,
                cityId = p.cityId,
                neighborhood = p.localNeighborhood,
                coverImage = p.images.firstOrNull() ?: "",
                logoImage = "",
                rating = p.rating,
                numReviews = p.numReviews,
                isVerified = p.isVerified || p.isApproved || p.isActive,
                isVip = p.isVip,
                isRecommended = p.isRecommended,
                password = p.password,
                isDeleted = p.isDeleted,
                deletedAt = p.deletedAt,
                latitude = p.latitude,
                longitude = p.longitude,
                createdAt = p.createdAt,
                rawProperty = p
            )
        }

        fun fromJob(j: JobEntity): UnifiedBusinessAccount {
            return UnifiedBusinessAccount(
                id = j.id.ifBlank { EntityIdGenerator.generateJobId() },
                businessType = BusinessType.JOB_POSTER,
                name = j.companyName.ifBlank { j.title },
                description = j.description,
                phone = j.phone,
                ownerName = j.managerName,
                cityId = j.cityId,
                neighborhood = j.address,
                isVerified = true,
                isVip = j.isVip,
                isDeleted = j.isDeleted,
                createdAt = j.createdAt,
                rawJob = j
            )
        }
    }
}

/**
 * 📊 Streamlined UI State object for the unified dashboard
 */
@Keep
data class UnifiedDashboardUiState(
    val account: UnifiedBusinessAccount? = null,
    val isLoading: Boolean = false,
    val isRefreshing: Boolean = false,
    val errorMessage: String? = null,
    val searchQuery: String = "",
    val selectedFilter: String = "ALL",
    val hasMore: Boolean = false,
    val isMoreLoading: Boolean = false,
    val ratingsList: List<RatingEntity> = emptyList(),
    val bookingsList: List<BookingEntity> = emptyList(),
    val productsList: List<ProductEntity> = emptyList(),
    val offersList: List<SpecialOfferEntity> = emptyList(),
    val attachmentsList: List<ProductAttachment> = emptyList()
)
