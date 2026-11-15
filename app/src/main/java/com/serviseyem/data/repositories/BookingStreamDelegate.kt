package com.serviseyem.data.repositories

import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.Query
import com.serviseyem.data.BookingEntity
import com.serviseyem.domain.usecases.ValidatePhoneUseCase
import com.serviseyem.utils.AppConstants
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

/**
 * تدفق الحجوزات الفوري مع القراءة من الذاكرة المحلية أولاً والمزامنة مع Firestore.
 */
internal class BookingStreamDelegate(
    private val firestore: FirebaseFirestore,
    private val getCachedBookings: () -> List<BookingEntity>,
    private val loadFromCache: () -> List<BookingEntity>,
    private val saveToCache: (List<BookingEntity>) -> Unit
) {
    fun getBookingsFlow(
        userId: String,
        isProvider: Boolean,
        pageLimit: Long
    ): Flow<List<BookingEntity>> = callbackFlow {
        val safeLimit = pageLimit.coerceIn(1L, 500L)
        val cleanUser = ValidatePhoneUseCase.normalizePhone(userId).ifBlank { userId.trim() }

        fun filterLocal(items: List<BookingEntity>): List<BookingEntity> {
            return if (userId.isNotBlank()) {
                items.filter {
                    if (isProvider) {
                        it.providerId == userId ||
                            ValidatePhoneUseCase.normalizePhone(it.providerPhone) == cleanUser
                    } else {
                        it.clientId == userId ||
                            it.customerPhone == userId ||
                            ValidatePhoneUseCase.normalizePhone(it.effectiveCustomerPhone) == cleanUser
                    }
                }
            } else items
        }

        // Emit memory cache immediately for instant rendering
        val local = loadFromCache()
        if (local.isNotEmpty()) {
            trySend(filterLocal(local))
        }

        val collection = firestore.collection(AppConstants.COL_BOOKINGS)
        val query = if (userId.isNotBlank()) {
            val baseQuery = if (isProvider) collection.whereEqualTo("providerId", userId)
            else collection.whereEqualTo("clientId", userId)
            baseQuery.limit(safeLimit)
        } else {
            collection.orderBy("createdAt", Query.Direction.DESCENDING).limit(safeLimit)
        }

        val listener: ListenerRegistration = query.addSnapshotListener { snapshot, error ->
            if (error != null) {
                Log.w("BookingRepository", "Firestore listener failed: ${error.message}")
                trySend(filterLocal(loadFromCache()))
                return@addSnapshotListener
            }

            if (snapshot != null) {
                val list = snapshot.documents.mapNotNull { doc ->
                    try {
                        doc.toObject(BookingEntity::class.java)?.copy(id = doc.id)
                    } catch (e: Exception) {
                        null
                    }
                }
                if (userId.isBlank()) {
                    saveToCache(list)
                } else {
                    val fetchedIds = list.map { it.id }.toSet()
                    val merged = list + getCachedBookings().filterNot { it.id in fetchedIds }
                    saveToCache(merged)
                }
                trySend(list)
            }
        }

        awaitClose { listener.remove() }
    }
}
