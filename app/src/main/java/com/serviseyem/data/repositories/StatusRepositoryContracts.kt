package com.serviseyem.data.repositories

import androidx.annotation.Keep
import com.serviseyem.data.models.InstantRequestEntity
import kotlinx.coroutines.flow.Flow

/**
 * 🏛️ Domain Entity for System Overview Metrics
 * 📌 Architectural Note: Contract Interfaces retained for modular Clean Architecture abstractions
 * and future repository decoupling without impacting runtime stability.
 */
@Keep
data class SystemStatusMetrics(
    val providersCount: Int = 0,
    val storesCount: Int = 0,
    val propertiesCount: Int = 0,
    val instantRequestsCount: Int = 0,
    val bookingsCount: Int = 0,
    val pendingJoinRequestsCount: Int = 0,
    val unreadNotificationsCount: Int = 0,
    val lastUpdatedTimestamp: Long = System.currentTimeMillis()
)

/**
 * 🏛️ Contract Interface: IUrgentRepository
 */
interface IUrgentRepository {
    fun getUrgentRequestsFlow(userId: String, isProvider: Boolean): Flow<List<InstantRequestEntity>>
    fun getUrgentRequestDetailsFlow(requestId: String): Flow<InstantRequestEntity?>
    fun getOffersForUrgentRequestFlow(requestId: String): Flow<List<com.serviseyem.data.models.RequestOfferEntity>>
    suspend fun createUrgentRequest(request: InstantRequestEntity): Result<String>
    suspend fun submitUrgentOffer(offer: com.serviseyem.data.models.RequestOfferEntity): Result<String>
    suspend fun acceptUrgentOffer(requestId: String, offerId: String, providerPhone: String): Result<Unit>
    suspend fun cancelUrgentRequest(requestId: String, userPin: String): Result<Unit>
}
