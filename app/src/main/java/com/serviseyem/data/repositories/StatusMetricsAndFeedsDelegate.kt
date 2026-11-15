package com.serviseyem.data.repositories

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.Source
import com.serviseyem.data.BookingEntity
import com.serviseyem.data.NotificationEntity
import com.serviseyem.data.PendingProviderEntity
import com.serviseyem.data.models.InstantRequestEntity
import com.serviseyem.utils.AppConstants
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

/**
 * تدفقات المراقبة الإدارية وإحصائيات النظام والحجوزات والطلبات الفورية والإشعارات.
 */
internal class StatusMetricsAndFeedsDelegate(
    private val firestore: FirebaseFirestore
) {
    fun getSystemMetrics(): Flow<SystemStatusMetrics> = callbackFlow {
        val listener: ListenerRegistration = firestore.collection(AppConstants.COL_JOIN_REQUESTS)
            .whereEqualTo("status", "PENDING")
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    trySend(SystemStatusMetrics())
                    return@addSnapshotListener
                }

                val pendingCount = snapshot?.size() ?: 0

                firestore.collection("system_stats").document("system_counters").get()
                    .addOnSuccessListener { doc ->
                        if (doc.exists()) {
                            trySend(
                                SystemStatusMetrics(
                                    providersCount = doc.getLong("providersCount")?.toInt() ?: 0,
                                    storesCount = doc.getLong("storesCount")?.toInt() ?: 0,
                                    propertiesCount = doc.getLong("propertiesCount")?.toInt() ?: 0,
                                    instantRequestsCount = doc.getLong("instantRequestsCount")?.toInt() ?: 0,
                                    bookingsCount = doc.getLong("bookingsCount")?.toInt() ?: 0,
                                    pendingJoinRequestsCount = pendingCount,
                                    lastUpdatedTimestamp = System.currentTimeMillis()
                                )
                            )
                        } else {
                            trySend(
                                SystemStatusMetrics(
                                    pendingJoinRequestsCount = pendingCount,
                                    lastUpdatedTimestamp = System.currentTimeMillis()
                                )
                            )
                        }
                    }.addOnFailureListener {
                        trySend(
                            SystemStatusMetrics(
                                pendingJoinRequestsCount = pendingCount,
                                lastUpdatedTimestamp = System.currentTimeMillis()
                            )
                        )
                    }
            }

        awaitClose { listener.remove() }
    }

    fun getPendingJoinRequests(): Flow<List<PendingProviderEntity>> = callbackFlow {
        val listener = firestore.collection(AppConstants.COL_JOIN_REQUESTS)
            .whereEqualTo("status", "PENDING")
            .limit(100)
            .addSnapshotListener { snapshot, error ->
                if (error != null || snapshot == null) {
                    trySend(emptyList())
                    return@addSnapshotListener
                }

                val list = snapshot.documents.mapNotNull { doc ->
                    PendingProviderEntity(
                        id = doc.id,
                        name = doc.getString("fullName") ?: doc.getString("name") ?: "",
                        phone = doc.getString("phone") ?: "",
                        categoryId = doc.getString("categoryId") ?: doc.getString("professionCategory") ?: doc.getString("category") ?: "",
                        area = doc.getString("city") ?: doc.getString("area") ?: "",
                        localNeighborhood = doc.getString("localNeighborhood") ?: doc.getString("neighborhood") ?: "",
                        status = doc.getString("status") ?: "PENDING",
                        reason = doc.getString("reason") ?: ""
                    )
                }
                trySend(list)
            }

        awaitClose { listener.remove() }
    }

    fun getSystemBookings(): Flow<List<BookingEntity>> = callbackFlow {
        val listener = firestore.collection(AppConstants.COL_BOOKINGS)
            .limit(100)
            .addSnapshotListener { snapshot, error ->
                if (error != null || snapshot == null) {
                    trySend(emptyList())
                    return@addSnapshotListener
                }

                val list = snapshot.documents.mapNotNull { doc ->
                    val resolvedName = doc.getString("customerName")
                        ?: doc.getString("clientName")
                        ?: doc.getString("fullName")
                        ?: ""
                    val resolvedPhone = doc.getString("customerPhone")
                        ?: doc.getString("clientPhone")
                        ?: doc.getString("userPhone")
                        ?: ""
                    val resolvedArea = doc.getString("customerArea")
                        ?: doc.getString("clientAddress")
                        ?: doc.getString("fullAddress")
                        ?: ""
                    val resolvedDate = doc.getString("date")
                        ?: doc.getString("scheduledDate")
                        ?: doc.getString("dateString")
                        ?: ""
                    val resolvedTime = doc.getString("time")
                        ?: doc.getString("timeString")
                        ?: ""
                    val resolvedAmount = doc.getDouble("totalAmount")
                        ?: doc.getDouble("priceYer")
                        ?: doc.getDouble("price")
                        ?: 0.0
                    BookingEntity(
                        id = doc.id,
                        bookingCode = doc.getString("bookingCode") ?: doc.id.take(8).uppercase(),
                        serviceType = doc.getString("serviceTitle") ?: doc.getString("serviceType") ?: "خدمة عامة",
                        providerId = doc.getString("providerId") ?: "",
                        providerName = doc.getString("providerName") ?: "",
                        customerName = resolvedName,
                        customerPhone = resolvedPhone,
                        customerArea = resolvedArea,
                        clientName = resolvedName,
                        status = doc.getString("status") ?: "PENDING",
                        date = resolvedDate,
                        time = resolvedTime,
                        dateString = resolvedDate,
                        timeString = resolvedTime,
                        totalAmount = resolvedAmount,
                        price = resolvedAmount
                    )
                }
                trySend(list)
            }

        awaitClose { listener.remove() }
    }

    fun getInstantRequests(): Flow<List<InstantRequestEntity>> = callbackFlow {
        val listener = firestore.collection(AppConstants.COL_INSTANT_REQUESTS)
            .limit(100)
            .addSnapshotListener { snapshot, error ->
                if (error != null || snapshot == null) {
                    trySend(emptyList())
                    return@addSnapshotListener
                }

                val list = snapshot.documents.mapNotNull { doc ->
                    InstantRequestEntity(
                        id = doc.id,
                        userId = doc.getString("userId") ?: "",
                        requestCode = doc.getString("requestCode") ?: doc.id.take(6).uppercase(),
                        serviceTitle = doc.getString("serviceTitle") ?: doc.getString("title") ?: "",
                        categoryName = doc.getString("category") ?: doc.getString("categoryName") ?: "",
                        userCity = doc.getString("userCity") ?: doc.getString("city") ?: "",
                        userNeighborhood = doc.getString("userNeighborhood") ?: doc.getString("address") ?: "",
                        description = doc.getString("detailsDescription") ?: doc.getString("description") ?: "",
                        acceptedPrice = doc.getDouble("maxBudgetYer") ?: doc.getDouble("acceptedPrice") ?: 0.0,
                        status = doc.getString("status") ?: "WAITING_FOR_OFFERS",
                        offersCount = (doc.getLong("offersCount") ?: 0L).toInt(),
                        createdAt = doc.getLong("createdAt") ?: System.currentTimeMillis(),
                        expiresAt = doc.getLong("expiresAt") ?: (System.currentTimeMillis() + 30 * 60 * 1000L)
                    )
                }
                trySend(list)
            }

        awaitClose { listener.remove() }
    }

    fun getNotifications(): Flow<List<NotificationEntity>> = callbackFlow {
        val listener = firestore.collection(AppConstants.COL_NOTIFICATIONS)
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .limit(50)
            .addSnapshotListener { snapshot, error ->
                if (error != null || snapshot == null) {
                    trySend(emptyList())
                    return@addSnapshotListener
                }

                val list = snapshot.documents.mapNotNull { doc ->
                    NotificationEntity(
                        id = doc.id,
                        title = doc.getString("title") ?: "",
                        message = doc.getString("message") ?: doc.getString("body") ?: "",
                        timestamp = doc.getLong("timestamp") ?: System.currentTimeMillis(),
                        notificationType = doc.getString("notificationType") ?: "SYSTEM"
                    )
                }
                trySend(list)
            }

        awaitClose { listener.remove() }
    }

    suspend fun clearNotifications(): Result<Unit> {
        return try {
            val unreadSnap = try {
                firestore.collection(AppConstants.COL_NOTIFICATIONS)
                    .whereEqualTo("isRead", false)
                    .limit(200)
                    .get()
                    .await()
            } catch (_: Exception) {
                firestore.collection(AppConstants.COL_NOTIFICATIONS)
                    .limit(200)
                    .get()
                    .await()
            }
            if (!unreadSnap.isEmpty) {
                val batch = firestore.batch()
                unreadSnap.documents.forEach { doc ->
                    batch.update(doc.reference, "isRead", true)
                }
                batch.commit().await()
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun refreshSystemStatus(): Result<Unit> {
        return try {
            firestore.collection("system_stats").document("system_counters")
                .get(Source.SERVER)
                .await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
