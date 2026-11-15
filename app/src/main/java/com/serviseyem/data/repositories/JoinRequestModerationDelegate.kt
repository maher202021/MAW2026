package com.serviseyem.data.repositories

import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.serviseyem.data.NotificationEntity
import com.serviseyem.data.PendingProviderEntity
import com.serviseyem.domain.usecases.ValidatePhoneUseCase
import com.serviseyem.utils.AppConstants
import com.serviseyem.utils.SecureHasher
import kotlinx.coroutines.tasks.await
import java.util.Locale
import java.util.UUID

/**
 * معالجة اعتماد أو رفض طلبات الانضمام ونشر الكيانات المعتمدة عبر Batch موحد.
 */
internal class JoinRequestModerationDelegate(
    private val firestore: FirebaseFirestore
) {
    suspend fun approveJoinRequest(request: PendingProviderEntity): Result<Unit> {
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
            val resolvedRole = type.uppercase(Locale.ROOT)
            val ownerId = requestDoc.getString("userId")?.takeIf { it.isNotBlank() }
                ?: requestDoc.getString("uid")?.takeIf { it.isNotBlank() }
                ?: reqId
            val rawPhone = request.phone.ifBlank { requestDoc.getString("phone") ?: "" }
            val cleanPhone = ValidatePhoneUseCase.normalizePhone(rawPhone)
            val rawOrHashedPassword = (requestDoc.getString("passwordHash") ?: requestDoc.getString("password") ?: "").trim()
            val passwordHash = when {
                rawOrHashedPassword.isBlank() -> ""
                SecureHasher.isValidHash(rawOrHashedPassword) -> rawOrHashedPassword
                else -> SecureHasher.hashPassword(rawOrHashedPassword)
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
                SetOptions.merge()
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
                    batch.set(provRef, provData, SetOptions.merge())
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
                    batch.set(storeRef, storeData, SetOptions.merge())
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
                    batch.set(propRef, propData, SetOptions.merge())
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
                    batch.set(jobRef, jobData, SetOptions.merge())
                }
            }

            // Update/Create User profile and Registered Users document
            val userRef = firestore.collection("users").document(reqId)
            batch.set(
                userRef,
                mapOf(
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
                ),
                SetOptions.merge()
            )

            if (cleanPhone.isNotBlank()) {
                val regUserRef = firestore.collection("registered_users").document(cleanPhone)
                batch.set(
                    regUserRef,
                    mapOf(
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
                    ),
                    SetOptions.merge()
                )
            }

            // Create notification for user
            val notifId = UUID.randomUUID().toString()
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

    suspend fun rejectJoinRequest(request: PendingProviderEntity, reason: String): Result<Unit> {
        return try {
            val reqId = request.id.trim()
            if (reqId.isBlank()) {
                return Result.failure(IllegalArgumentException("معرف طلب الانضمام غير صالح"))
            }
            val cleanPhone = ValidatePhoneUseCase.normalizePhone(request.phone)
            val now = System.currentTimeMillis()
            val finalReason = reason.ifBlank { "لم تستوفِ المستندات أو الشروط المطلوبة" }

            val batch = firestore.batch()
            val requestRef = firestore.collection(AppConstants.COL_JOIN_REQUESTS).document(reqId)

            batch.set(
                requestRef,
                mapOf(
                    "status" to "REJECTED",
                    "approvalStatus" to "REJECTED",
                    "isActive" to false,
                    "rejectionReason" to finalReason,
                    "rejectedAt" to now,
                    "rejectedBy" to "ADMIN",
                    "updatedAt" to now
                ),
                SetOptions.merge()
            )

            // Update status in pending_providers as well safely with merge
            val pendingRef = firestore.collection("pending_providers").document(reqId)
            batch.set(
                pendingRef,
                mapOf(
                    "status" to "REJECTED",
                    "reason" to finalReason
                ),
                SetOptions.merge()
            )

            // Create notification for user
            val notifId = UUID.randomUUID().toString()
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
}
