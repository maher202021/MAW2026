package com.serviseyem.data.repositories

import androidx.annotation.Keep
import com.serviseyem.domain.entities.DashboardStatsEntity
import com.serviseyem.domain.entities.FavoriteItemEntity
import com.serviseyem.domain.entities.GalleryAlbumEntity
import com.serviseyem.domain.entities.ProductItemEntity
import com.serviseyem.domain.entities.RatingReviewEntity
import kotlinx.coroutines.flow.Flow

/**
 * 🏛️ Repository Contracts for Dashboards, Products, Favorites, Ratings, and Gallery.
 */

interface IDashboardRepository {
    fun getDashboardStats(ownerId: String, role: String): Flow<DashboardStatsEntity>
    suspend fun refreshDashboardStats(ownerId: String, role: String): Result<Unit>
}

interface IFavoritesRepository {
    fun getUserFavorites(userId: String): Flow<List<FavoriteItemEntity>>
    suspend fun addFavorite(favorite: FavoriteItemEntity): Result<Unit>
    suspend fun removeFavorite(userId: String, targetId: String): Result<Unit>
    suspend fun isFavorite(userId: String, targetId: String): Boolean
}

interface IProductsRepository {
    fun getOwnerProducts(ownerId: String): Flow<List<ProductItemEntity>>
    fun getAllAvailableProducts(): Flow<List<ProductItemEntity>>
    suspend fun addProduct(product: ProductItemEntity): Result<String>
    suspend fun updateProduct(product: ProductItemEntity): Result<Unit>
    suspend fun deleteProduct(productId: String): Result<Unit>
}

interface IRatingsRepository {
    fun getTargetRatings(targetId: String): Flow<List<RatingReviewEntity>>
    suspend fun addRating(rating: RatingReviewEntity): Result<String>
}

interface IGalleryRepository {
    fun getOwnerGallery(ownerId: String): Flow<List<GalleryAlbumEntity>>
    suspend fun saveGalleryAlbum(album: GalleryAlbumEntity): Result<String>
    suspend fun deleteGalleryAlbum(albumId: String): Result<Unit>
}

@Keep
data class InventoryItem(
    val id: String = "",
    val ownerId: String = "",
    val sku: String = "",
    val name: String = "",
    val quantity: Int = 0,
    val minThreshold: Int = 5,
    val price: Double = 0.0,
    val inStock: Boolean = true
)

@Keep
data class LoyaltyProgram(
    val id: String = "",
    val ownerId: String = "",
    val name: String = "",
    val pointsRequired: Int = 100,
    val rewardDescription: String = "",
    val discountValue: Double = 0.0,
    val isEnabled: Boolean = true
)

@Keep
data class StaffMember(
    val id: String = "",
    val ownerId: String = "",
    val name: String = "",
    val role: String = "",
    val phone: String = "",
    val canEditPrices: Boolean = false,
    val canChat: Boolean = true
)

interface IDashboardExtensionsRepository {
    fun getSpecialOffers(ownerId: String): Flow<List<com.serviseyem.data.SpecialOfferEntity>>
    fun updateSpecialOfferStatus(offerId: String, isEnabled: Boolean)
    fun deleteSpecialOffer(offerId: String)
    fun addSpecialOffer(offer: com.serviseyem.data.SpecialOfferEntity)
    fun getCoupons(ownerId: String): Flow<List<com.serviseyem.data.SpecialOfferEntity>>
    fun updateCouponStatus(couponId: String, isEnabled: Boolean)
    fun deleteCoupon(couponId: String)
    fun addCoupon(coupon: com.serviseyem.data.SpecialOfferEntity)
    fun getInventory(ownerId: String): Flow<List<InventoryItem>>
    fun updateInventoryQuantity(itemId: String, newQty: Int, inStock: Boolean)
    fun deleteInventoryItem(itemId: String)
    fun addInventoryItem(item: InventoryItem)
    fun getLoyaltyPrograms(ownerId: String): Flow<List<LoyaltyProgram>>
    fun updateLoyaltyProgramStatus(programId: String, isEnabled: Boolean)
    fun deleteLoyaltyProgram(programId: String)
    fun addLoyaltyProgram(program: LoyaltyProgram)
    fun getStaff(ownerId: String): Flow<List<StaffMember>>
    fun deleteStaff(staffId: String)
    fun addStaff(staffMember: StaffMember)
}
