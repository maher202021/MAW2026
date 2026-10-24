package com.serviseyem.data.repositories

import com.serviseyem.data.LocalAppCacheManager
import com.serviseyem.data.SpecialOfferEntity
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class OffersRepository @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val cacheManager: LocalAppCacheManager
) {
    suspend fun getOffers(): Result<List<SpecialOfferEntity>> {
        return try {
            val snapshot = firestore.collection("special_offers")
                .limit(50)
                .get()
                .await()

            var offers = snapshot.documents.mapNotNull { doc ->
                doc.toObject(SpecialOfferEntity::class.java)?.copy(id = doc.id)
            }

            if (offers.isEmpty()) {
                val legacySnapshot = firestore.collection("offers")
                    .limit(50)
                    .get()
                    .await()
                offers = legacySnapshot.documents.mapNotNull { doc ->
                    doc.toObject(SpecialOfferEntity::class.java)?.copy(id = doc.id)
                }
            }

            cacheManager.saveOffersCache(SpecialOfferEntity.serializeList(offers))
            Result.success(offers)
        } catch (e: Exception) {
            val fallbackStr = cacheManager.getOffersCacheRaw()
            val fallback = if (fallbackStr != "[]") SpecialOfferEntity.parseList(fallbackStr) else emptyList()
            if (fallback.isNotEmpty()) {
                Result.success(fallback)
            } else {
                Result.failure(e)
            }
        }
    }

    private fun readLocalCache(): MutableList<SpecialOfferEntity> {
        val raw = cacheManager.getOffersCacheRaw()
        return if (raw.isNotBlank() && raw != "[]") {
            SpecialOfferEntity.parseList(raw).toMutableList()
        } else {
            mutableListOf()
        }
    }

    suspend fun addOffer(offer: SpecialOfferEntity): Result<Unit> {
        return try {
            val docId = offer.id.ifBlank { java.util.UUID.randomUUID().toString() }
            val finalOffer = offer.copy(id = docId)
            firestore.collection("special_offers").document(docId).set(finalOffer).await()
            try {
                firestore.collection("offers").document(docId).set(finalOffer).await()
            } catch (_: Exception) {}
            val currentOffers = readLocalCache()
            if (currentOffers.none { it.id == docId }) {
                currentOffers.add(0, finalOffer)
            }
            cacheManager.saveOffersCache(SpecialOfferEntity.serializeList(currentOffers))
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updateOffer(offer: SpecialOfferEntity): Result<Unit> {
        return try {
            val docId = offer.id.ifBlank { java.util.UUID.randomUUID().toString() }
            val finalOffer = offer.copy(id = docId)
            firestore.collection("special_offers").document(docId).set(finalOffer).await()
            try {
                firestore.collection("offers").document(docId).set(finalOffer).await()
            } catch (_: Exception) {}
            val currentOffers = readLocalCache().map {
                if (it.id == docId) finalOffer else it
            }
            cacheManager.saveOffersCache(SpecialOfferEntity.serializeList(currentOffers))
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun deleteOffer(offerId: String): Result<Unit> {
        return try {
            firestore.collection("special_offers").document(offerId).delete().await()
            try {
                firestore.collection("offers").document(offerId).delete().await()
            } catch (_: Exception) {}
            val currentOffers = readLocalCache().filter { it.id != offerId }
            cacheManager.saveOffersCache(SpecialOfferEntity.serializeList(currentOffers))
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
