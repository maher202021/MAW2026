package com.serviseyem.data.repositories

import android.content.Context
import android.util.Log
import com.serviseyem.data.BookingEntity
import com.serviseyem.data.NotificationEntity
import com.serviseyem.data.PendingProviderEntity
import com.serviseyem.data.models.InstantRequestEntity
import com.serviseyem.utils.AppConstants
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

/**
 * 📦 StatusRepositoryImpl
 * Implements IStatusRepository for system stats, pending join requests, notifications, and system bookings.
 */
class StatusRepositoryImpl(
    @Suppress("UNUSED_PARAMETER") context: Context? = null
) : IStatusRepository {

    private val firestore = FirebaseFirestore.getInstance()

    override fun getSystemMetrics(): Flow<SystemStatusMetrics> = callbackFlow {
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
                            trySend(SystemStatusMetrics(pendingJoinRequestsCount = pendingCount, lastUpdatedTimestamp = System.currentTimeMillis()))
                        }
                    }.addOnFailureListener {
                        trySend(SystemStatusMetrics(pendingJoinRequestsCount = pendingCount, lastUpdatedTimestamp = System.currentTimeMillis()))
                    }
            }

        awaitClose { listener.remove() }
    }

    override fun getSystemMetricsFlow(): Flow<SystemStatusMetrics> = getSystemMetrics()

    override fun getPendingJoinRequests(): Flow<List<PendingProviderEntity>> = callbackFlow {
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

    override fun getPendingJoinRequestsFlow(): Flow<List<PendingProviderEntity>> = getPendingJoinRequests()

    override fun getSystemBookings(): Flow<List<BookingEntity>> = callbackFlow {
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

    override fun getSystemBookingsFlow(): Flow<List<BookingEntity>> = getSystemBookings()

    override fun getInstantRequests(): Flow<List<InstantRequestEntity>> = callbackFlow {
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

    override fun getInstantRequestsFlow(): Flow<List<InstantRequestEntity>> = getInstantRequests()

    override fun getNotifications(): Flow<List<NotificationEntity>> = callbackFlow {
        val listener = firestore.collection(AppConstants.COL_NOTIFICATIONS)
            .orderBy("timestamp", com.google.firebase.firestore.Query.Direction.DESCENDING)
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

    override fun getNotificationsFlow(): Flow<List<NotificationEntity>> = getNotifications()

    override suspend fun approveJoinRequest(request: PendingProviderEntity): Result<Unit> {
        return try {
            val reqId = request.id.trim()
            if (reqId.isBlank()) {
                return Result.failure(IllegalArgumentException("معرف طلب الانضمام غير صالح"))
            }
            val requestDoc = firestore.collection(AppConstants.COL_JOIN_REQUESTS).document(reqId).get().await()
            if (!requestDoc.exists() && request.name.isBlank() && request.phone.isBlank()) {
                return Result.failure(IllegalStateException("طلب الانضمام غير موجود"))
            }
            val type = (requestDoc.getString("type") ?: "PROVIDER").trim().ifBlank { "PROVIDER" }
            val resolvedRole = type.uppercase(java.util.Locale.ROOT)
            val ownerId = requestDoc.getString("userId")?.takeIf { it.isNotBlank() }
                ?: requestDoc.getString("uid")?.takeIf { it.isNotBlank() }
                ?: reqId
            val rawPhone = request.phone.ifBlank { requestDoc.getString("phone") ?: "" }
            val cleanPhone = com.serviseyem.domain.usecases.ValidatePhoneUseCase.normalizePhone(rawPhone)
            val rawOrHashedPassword = (requestDoc.getString("passwordHash") ?: requestDoc.getString("password") ?: "").trim()
            val passwordHash = when {
                rawOrHashedPassword.isBlank() -> ""
                com.serviseyem.utils.SecureHasher.isValidHash(rawOrHashedPassword) -> rawOrHashedPassword
                else -> com.serviseyem.utils.SecureHasher.hashPassword(rawOrHashedPassword)
            }
            val resolvedName = request.name.ifBlank {
                requestDoc.getString("fullName") ?: requestDoc.getString("name") ?: ""
            }.trim()
            val resolvedCategory = request.categoryId.ifBlank {
                requestDoc.getString("categoryId") ?: requestDoc.getString("professionCategory") ?: requestDoc.getString("category") ?: ""
            }.trim()
            val resolvedArea = request.area.ifBlank {
                requestDoc.getString("city") ?: requestDoc.getString("area") ?: ""
            }.trim()
            val resolvedNeighborhood = request.localNeighborhood.ifBlank {
                requestDoc.getString("localNeighborhood") ?: requestDoc.getString("neighborhood") ?: ""
            }.trim()
            val now = System.currentTimeMillis()

            val batch = firestore.batch()
            val requestRef = firestore.collection(AppConstants.COL_JOIN_REQUESTS).document(reqId)
            batch.set(
                requestRef,
                mapOf(
                    "status" to "APPROVED",
                    "approvalStatus" to "APPROVED",
                    "isActive" to true,
                    "approvedAt" to now,
                    "updatedAt" to now
                ),
                com.google.firebase.firestore.SetOptions.merge()
            )

            // Create Entity in appropriate collection
            when (resolvedRole) {
                "PROVIDER" -> {
                    val provRef = firestore.collection("providers").document(reqId)
                    val provData = mapOf(
                        "id" to reqId,
                        "ownerId" to ownerId,
                        "name" to resolvedName,
                        "phone" to cleanPhone,
                        "password" to "",
                        "passwordHash" to passwordHash,
                        "categoryId" to resolvedCategory,
                        "area" to resolvedArea,
                        "localNeighborhood" to resolvedNeighborhood,
                        "isAvailable" to true,
                        "subscriptionStatus" to "APPROVED",
                        "rating" to 5.0f,
                        "isBlocked" to false,
                        "createdAt" to now
                    )
                    batch.set(provRef, provData, com.google.firebase.firestore.SetOptions.merge())
                }
                "STORE", "RESTAURANT", "MEDICAL" -> {
                    val storeRef = firestore.collection("stores").document(reqId)
                    val sectionId = when (resolvedRole) {
                        "RESTAURANT" -> "restaurants"
                        "MEDICAL" -> "medical"
                        else -> "stores"
                    }
                    val storeData = mapOf(
                        "id" to reqId,
                        "ownerId" to ownerId,
                        "name" to (requestDoc.getString("businessName")?.takeIf { it.isNotBlank() } ?: resolvedName),
                        "ownerName" to resolvedName,
                        "phone" to cleanPhone,
                        "password" to "",
                        "passwordHash" to passwordHash,
                        "category" to resolvedCategory,
                        "categoryId" to resolvedCategory,
                        "sectionId" to sectionId,
                        "city" to resolvedArea,
                        "cityId" to resolvedArea,
                        "localNeighborhood" to resolvedNeighborhood,
                        "isActive" to true,
                        "isApproved" to true,
                        "type" to resolvedRole,
                        "createdAt" to now
                    )
                    batch.set(storeRef, storeData, com.google.firebase.firestore.SetOptions.merge())
                }
                "PROPERTY" -> {
                    val propRef = firestore.collection("properties").document(reqId)
                    val propData = mapOf(
                        "id" to reqId,
                        "ownerId" to ownerId,
                        "title" to (requestDoc.getString("propertyTitle")?.takeIf { it.isNotBlank() } ?: resolvedName),
                        "ownerName" to resolvedName,
                        "phone" to cleanPhone,
                        "password" to "",
                        "passwordHash" to passwordHash,
                        "category" to resolvedCategory,
                        "categoryId" to resolvedCategory,
                        "city" to resolvedArea,
                        "cityId" to resolvedArea,
                        "localNeighborhood" to resolvedNeighborhood,
                        "isActive" to true,
                        "isApproved" to true,
                        "createdAt" to now
                    )
                    batch.set(propRef, propData, com.google.firebase.firestore.SetOptions.merge())
                }
                "JOB" -> {
                    val jobRef = firestore.collection("jobs").document(reqId)
                    val jobTitle = requestDoc.getString("jobTitle")?.takeIf { it.isNotBlank() } ?: resolvedName
                    val salaryRange = requestDoc.getString("salaryRange") ?: ""
                    val requirements = requestDoc.getString("jobRequirements") ?: ""
                    val jobData = mapOf(
                        "id" to reqId,
                        "ownerId" to ownerId,
                        "title" to jobTitle,
                        "jobTitle" to jobTitle,
                        "companyName" to (requestDoc.getString("companyName")?.takeIf { it.isNotBlank() } ?: resolvedName),
                        "salary" to salaryRange,
                        "salaryRange" to salaryRange,
                        "requirements" to requirements,
                        "phone" to cleanPhone,
                        "city" to resolvedArea,
                        "cityId" to resolvedArea,
                        "address" to resolvedNeighborhood,
                        "isActive" to true,
                        "isApproved" to true,
                        "createdAt" to now
                    )
                    batch.set(jobRef, jobData, com.google.firebase.firestore.SetOptions.merge())
                }
            }

            // Update/Create User profile and Registered Users document
            val userRef = firestore.collection("users").document(reqId)
            batch.set(userRef, mapOf(
                "id" to reqId,
                "ownerId" to ownerId,
                "name" to resolvedName,
                "phone" to cleanPhone,
                "password" to "",
                "passwordHash" to passwordHash,
                "role" to resolvedRole,
                "accountType" to resolvedRole,
                "status" to "APPROVED",
                "isApproved" to true,
                "updatedAt" to now
            ), com.google.firebase.firestore.SetOptions.merge())

            if (cleanPhone.isNotBlank()) {
                val regUserRef = firestore.collection("registered_users").document(cleanPhone)
                batch.set(regUserRef, mapOf(
                    "id" to reqId,
                    "name" to resolvedName,
                    "phone" to cleanPhone,
                    "password" to "",
                    "passwordHash" to passwordHash,
                    "role" to resolvedRole,
                    "accountType" to resolvedRole,
                    "status" to "APPROVED",
                    "isApproved" to true,
                    "updatedAt" to now
                ), com.google.firebase.firestore.SetOptions.merge())
            }

            // Create notification for user
            val notifId = java.util.UUID.randomUUID().toString()
            val notifRef = firestore.collection(AppConstants.COL_NOTIFICATIONS).document(notifId)
            val notif = NotificationEntity(
                id = notifId,
                title = "🎉 تم قبول وتوثيق طلب الانضمام!",
                message = "تهانينا! تمت مراجعة والموافقة على طلب انضمامك ($type). حسابك أصبح نشطاً ومتاحاً الآن.",
                targetType = "USER",
                targetValue = cleanPhone,
                notificationType = "JOIN_APPROVED",
                relatedRequestId = reqId,
                isRead = false,
                fcmSent = false,
                timestamp = now,
                createdAt = now
            )
            batch.set(notifRef, notif)

            // Delete from pending_providers so that it is removed from waiting list
            val pendingRef = firestore.collection("pending_providers").document(reqId)
            batch.delete(pendingRef)

            batch.commit().await()
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e("StatusRepositoryImpl", "Error approving join request", e)
            Result.failure(e)
        }
    }

    override suspend fun rejectJoinRequest(request: PendingProviderEntity, reason: String): Result<Unit> {
        return try {
            val reqId = request.id.trim()
            if (reqId.isBlank()) {
                return Result.failure(IllegalArgumentException("معرف طلب الانضمام غير صالح"))
            }
            val cleanPhone = com.serviseyem.domain.usecases.ValidatePhoneUseCase.normalizePhone(request.phone)
            val now = System.currentTimeMillis()
            val finalReason = reason.ifBlank { "لم تستوفِ المستندات أو الشروط المطلوبة" }

            val batch = firestore.batch()
            val requestRef = firestore.collection(AppConstants.COL_JOIN_REQUESTS).document(reqId)

            batch.set(requestRef, mapOf(
                "status" to "REJECTED",
                "approvalStatus" to "REJECTED",
                "isActive" to false,
                "rejectionReason" to finalReason,
                "rejectedAt" to now,
                "rejectedBy" to "ADMIN",
                "updatedAt" to now
            ), com.google.firebase.firestore.SetOptions.merge())

            // Update status in pending_providers as well safely with merge
            val pendingRef = firestore.collection("pending_providers").document(reqId)
            batch.set(pendingRef, mapOf(
                "status" to "REJECTED",
                "reason" to finalReason
            ), com.google.firebase.firestore.SetOptions.merge())

            // Create notification for user
            val notifId = java.util.UUID.randomUUID().toString()
            val notifRef = firestore.collection(AppConstants.COL_NOTIFICATIONS).document(notifId)
            val notif = NotificationEntity(
                id = notifId,
                title = "❌ حالة طلب الانضمام",
                message = "نأسف لإبلاغك بأنه تم رفض طلب الانضمام للسبب التالي: $finalReason",
                targetType = "USER",
                targetValue = cleanPhone,
                notificationType = "JOIN_REJECTED",
                relatedRequestId = reqId,
                isRead = false,
                fcmSent = false,
                timestamp = now,
                createdAt = now
            )
            batch.set(notifRef, notif)

            batch.commit().await()
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e("StatusRepositoryImpl", "Error rejecting join request", e)
            Result.failure(e)
        }
    }

    override suspend fun clearNotifications(): Result<Unit> {
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

    override suspend fun refreshSystemStatus(): Result<Unit> {
        return try {
            firestore.collection("system_stats").document("system_counters")
                .get(com.google.firebase.firestore.Source.SERVER)
                .await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
