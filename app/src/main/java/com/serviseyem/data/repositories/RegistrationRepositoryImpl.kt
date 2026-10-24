package com.serviseyem.data.repositories

import android.content.Context
import android.util.Log
import com.serviseyem.data.models.JoinRequestEntity
import com.serviseyem.data.NotificationEntity
import com.serviseyem.domain.entities.JoinStatusEntity
import com.serviseyem.domain.entities.RegistrationEntity
import com.serviseyem.domain.usecases.ValidatePhoneUseCase
import com.serviseyem.security.BookingSecurityHelper
import com.serviseyem.utils.AppConstants
import com.serviseyem.utils.NotificationDeduplicator
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import java.util.UUID

/**
 * 📦 RegistrationRepositoryImpl
 * Production implementation of IRegistrationRepository with strict Firestore schema,
 * phone deduplication, admin notifications, and real-time status tracking.
 */
class RegistrationRepositoryImpl(
    private val context: Context
) : IRegistrationRepository {

    private val firestore = FirebaseFirestore.getInstance()
    private val deduplicator = NotificationDeduplicator(context)

    private suspend fun checkExistingPendingRequest(phone: String): Boolean = coroutineScope {
        val cleanPhone = ValidatePhoneUseCase.normalizePhone(phone)
        if (cleanPhone.isBlank()) return@coroutineScope false

        // 1. فحص طلبات الانضمام المعلقة أولاً (استعلام واحد فقط محدود بواحد)
        val hasPendingJoin = runCatching {
            !firestore.collection(AppConstants.COL_JOIN_REQUESTS)
                .whereEqualTo("phone", cleanPhone)
                .whereEqualTo("status", "PENDING")
                .limit(1)
                .get()
                .await()
                .isEmpty
        }.getOrDefault(false)

        if (hasPendingJoin) return@coroutineScope true

        // 2. فحص سريع للمستخدمين المسجلين عبر المعرف المباشر (Direct Document Get - يستهلك قراءة واحدة فقط)
        val hasDirectUser = runCatching {
            firestore.collection("registered_users").document(cleanPhone).get().await().exists() ||
                firestore.collection("users").document(cleanPhone).get().await().exists() ||
                firestore.collection("providers").document("p_$cleanPhone").get().await().exists()
        }.getOrDefault(false)

        if (hasDirectUser) return@coroutineScope true

        // 3. فحص احتياطي موحد فقط عند عدم وجود وثيقة مباشرة
        val hasIndexedUser = runCatching {
            !firestore.collection("registered_users")
                .whereEqualTo("phone", cleanPhone)
                .limit(1)
                .get()
                .await()
                .isEmpty
        }.getOrDefault(false)

        hasIndexedUser
    }

    private suspend fun sendAdminJoinNotification(requestId: String, applicantName: String, phone: String, type: String) {
        try {
            if (deduplicator.isJoinNotificationDuplicate(requestId, "JOIN_REQUEST")) {
                return
            }
            val notifId = UUID.randomUUID().toString()
            val userNotifId = UUID.randomUUID().toString()
            val typeTitle = when (type) {
                "PROVIDER" -> "مهني / فني"
                "STORE" -> "متجر / محل تجاري"
                "RESTAURANT" -> "مطعم / كافيه"
                "MEDICAL" -> "مركز طبي / دكتور"
                "PROPERTY" -> "عقار / مكتب عقاري"
                "JOB" -> "إعلان توظيف / صاحب عمل"
                "JOB_SEEKER" -> "متقدم للوظائف"
                "CLIENT" -> "عميل جديد"
                else -> type
            }

            // 1. Admin Notification
            val notification = NotificationEntity(
                id = notifId,
                title = "📥 طلب انضمام جديد ($typeTitle)",
                message = "قدم $applicantName ($phone) طلب انضمام جديد كـ ($typeTitle). يرجى مراجعة بيانات الطلب والموافقة عليه.",
                targetType = "ADMIN",
                targetValue = "ALL",
                notificationType = "JOIN_REQUEST",
                relatedRequestId = requestId,
                isRead = false,
                fcmSent = false,
                timestamp = System.currentTimeMillis(),
                createdAt = System.currentTimeMillis()
            )

            // 2. Applicant Notification
            val userNotification = NotificationEntity(
                id = userNotifId,
                title = "📨 تم استلام طلب انضمامك بنجاح",
                message = "أهلاً $applicantName، تم استلام طلب تسجيلك كـ ($typeTitle) وجاري مراجعته والتحقق من البيانات من قِبل إدارة التطبيق. نسعد بانضمامك وسنبلغك بإشعار فور التفعيل والاعتماد!",
                targetType = "USER",
                targetValue = phone,
                notificationType = "JOIN_REQUEST",
                relatedRequestId = requestId,
                isRead = false,
                fcmSent = false,
                timestamp = System.currentTimeMillis(),
                createdAt = System.currentTimeMillis()
            )

            firestore.collection(AppConstants.COL_NOTIFICATIONS).document(notifId).set(notification).await()
            firestore.collection(AppConstants.COL_NOTIFICATIONS).document(userNotifId).set(userNotification).await()
            deduplicator.markJoinNotificationSent(requestId, "JOIN_REQUEST")
        } catch (e: Exception) {
            Log.e("RegistrationRepository", "Failed to send join notifications", e)
        }
    }

    override suspend fun registerClient(client: RegistrationEntity.Client): Result<String> {
        return try {
            val cleanPhone = ValidatePhoneUseCase.normalizePhone(client.phone)
            if (cleanPhone.isBlank() || cleanPhone.length < 7) {
                return Result.failure(IllegalArgumentException("رقم الهاتف غير صالح"))
            }
            val cleanPassword = client.rawPassword.trim()
            if (cleanPassword.isBlank()) {
                return Result.failure(IllegalArgumentException("لا يمكن التسجيل بدون كلمة مرور صريحة"))
            }
            val policyCheck = com.serviseyem.utils.SecurityCryptoUtils.validatePasswordPolicy(cleanPassword)
            if (!policyCheck.first) {
                return Result.failure(IllegalArgumentException(policyCheck.second ?: "كلمة المرور ضعيفة"))
            }
            if (checkExistingPendingRequest(cleanPhone)) {
                return Result.failure(Exception("يوجد طلب تسجيل أو حساب مسجل بالفعل لرقم الهاتف هذا"))
            }

            val id = "${cleanPhone}_CLIENT"
            val currentUid = com.google.firebase.auth.FirebaseAuth.getInstance().currentUser?.uid ?: ""
            val hashedPassword = com.serviseyem.utils.PasswordHasher.hash(client.rawPassword)
            val request = JoinRequestEntity(
                id = id,
                type = "CLIENT",
                status = "PENDING",
                fullName = client.fullName.trim(),
                phone = cleanPhone,
                passwordHash = hashedPassword,
                city = client.city.trim(),
                profileImage = client.profileImageUrl,
                approvalStatus = "PENDING",
                submittedAt = System.currentTimeMillis(),
                createdAt = System.currentTimeMillis(),
                updatedAt = System.currentTimeMillis()
            )

            // Store request in join_requests
            val requestMap = mapOf(
                "id" to id,
                "uid" to currentUid,
                "userId" to currentUid,
                "type" to "CLIENT",
                "status" to "PENDING",
                "approvalStatus" to "PENDING",
                "fullName" to client.fullName.trim(),
                "phone" to cleanPhone,
                "passwordHash" to hashedPassword,
                "city" to client.city.trim(),
                "profileImage" to client.profileImageUrl,
                "submittedAt" to System.currentTimeMillis(),
                "createdAt" to System.currentTimeMillis(),
                "updatedAt" to System.currentTimeMillis()
            )

            val pendingMap = mapOf(
                "id" to id,
                "name" to client.fullName.trim(),
                "phone" to cleanPhone,
                "categoryId" to "CLIENT",
                "area" to client.city.trim(),
                "status" to "PENDING",
                "profession" to "CLIENT",
                "providerType" to "CLIENT",
                "password" to "",
                "passwordHash" to hashedPassword,
                "createdAt" to System.currentTimeMillis()
            )

            firestore.runTransaction { transaction ->
                val userRef = firestore.collection("users").document(cleanPhone)
                val userDoc = transaction.get(userRef)
                if (userDoc.exists()) {
                    throw Exception("الحساب موجود بالفعل")
                }

                val reqRef = firestore.collection(AppConstants.COL_JOIN_REQUESTS).document(id)
                val pendRef = firestore.collection("pending_providers").document(id)

                transaction.set(reqRef, requestMap)
                transaction.set(pendRef, pendingMap)
            }.await()

            sendAdminJoinNotification(id, request.fullName, cleanPhone, "CLIENT")
            Result.success(id)
        } catch (e: Exception) {
            Log.e("RegistrationRepository", "Error registering client", e)
            Result.failure(e)
        }
    }

    override suspend fun registerProvider(provider: RegistrationEntity.Provider): Result<String> {
        return try {
            val cleanPhone = ValidatePhoneUseCase.normalizePhone(provider.phone)
            if (checkExistingPendingRequest(cleanPhone)) {
                return Result.failure(Exception("يوجد طلب انضمام مهني قيد المراجعة بالفعل لرقم الهاتف هذا"))
            }

            val id = "${cleanPhone}_PROVIDER"
            val hashedPassword = com.serviseyem.utils.PasswordHasher.hash(provider.rawPassword)
            val request = JoinRequestEntity(
                id = id,
                type = "PROVIDER",
                status = "PENDING",
                fullName = provider.fullName.trim(),
                phone = cleanPhone,
                passwordHash = hashedPassword,
                city = provider.city.trim(),
                categoryId = provider.professionCategory.trim(),
                categoryName = provider.professionCategory.trim(),
                idCardImage = provider.identityDocumentUrl,
                workImages = provider.workImages,
                businessName = provider.fullName.trim(),
                approvalStatus = "PENDING",
                submittedAt = System.currentTimeMillis(),
                createdAt = System.currentTimeMillis(),
                updatedAt = System.currentTimeMillis()
            )

            val pendingMap = mapOf(
                "id" to id,
                "name" to provider.fullName.trim(),
                "phone" to cleanPhone,
                "categoryId" to provider.professionCategory.trim(),
                "customCategoryName" to provider.professionCategory.trim(),
                "area" to provider.city.trim(),
                "status" to "PENDING",
                "profession" to "PROVIDER",
                "providerType" to "PROVIDER",
                "password" to "",
                "passwordHash" to hashedPassword,
                "createdAt" to System.currentTimeMillis()
            )

            val batch = firestore.batch()
            batch.set(firestore.collection(AppConstants.COL_JOIN_REQUESTS).document(id), request)
            batch.set(firestore.collection("pending_providers").document(id), pendingMap)
            batch.commit().await()

            sendAdminJoinNotification(id, request.fullName, cleanPhone, "PROVIDER")
            Result.success(id)
        } catch (e: Exception) {
            Log.e("RegistrationRepository", "Error registering provider", e)
            Result.failure(e)
        }
    }

    override suspend fun registerStore(store: RegistrationEntity.Store): Result<String> {
        return try {
            val cleanPhone = ValidatePhoneUseCase.normalizePhone(store.phone)
            if (checkExistingPendingRequest(cleanPhone)) {
                return Result.failure(Exception("يوجد طلب انضمام متجر قيد المراجعة بالفعل لرقم الهاتف هذا"))
            }

            val id = "${cleanPhone}_STORE"
            val hashedPassword = com.serviseyem.utils.PasswordHasher.hash(store.rawPassword)
            val request = JoinRequestEntity(
                id = id,
                type = "STORE",
                status = "PENDING",
                businessName = store.storeName.trim(),
                ownerName = store.ownerName.trim(),
                fullName = store.ownerName.trim(),
                phone = cleanPhone,
                passwordHash = hashedPassword,
                categoryId = store.storeCategory.trim(),
                categoryName = store.storeCategory.trim(),
                city = store.city.trim(),
                area = store.addressDetails.trim(),
                logoImage = store.logoUrl,
                workImages = store.storeImages,
                approvalStatus = "PENDING",
                submittedAt = System.currentTimeMillis(),
                createdAt = System.currentTimeMillis(),
                updatedAt = System.currentTimeMillis()
            )

            val pendingMap = mapOf(
                "id" to id,
                "name" to store.storeName.trim(),
                "ownerName" to store.ownerName.trim(),
                "phone" to cleanPhone,
                "categoryId" to "STORE",
                "customCategoryName" to store.storeCategory.trim(),
                "area" to store.city.trim(),
                "localNeighborhood" to store.addressDetails.trim(),
                "status" to "PENDING",
                "profession" to "STORE_OWNER",
                "providerType" to "STORE_OWNER",
                "password" to "",
                "passwordHash" to hashedPassword,
                "createdAt" to System.currentTimeMillis()
            )

            val batch = firestore.batch()
            batch.set(firestore.collection(AppConstants.COL_JOIN_REQUESTS).document(id), request)
            batch.set(firestore.collection("pending_providers").document(id), pendingMap)
            batch.commit().await()

            sendAdminJoinNotification(id, request.businessName, cleanPhone, "STORE")
            Result.success(id)
        } catch (e: Exception) {
            Log.e("RegistrationRepository", "Error registering store", e)
            Result.failure(e)
        }
    }

    override suspend fun registerRestaurant(restaurant: RegistrationEntity.Restaurant): Result<String> {
        return try {
            val cleanPhone = ValidatePhoneUseCase.normalizePhone(restaurant.phone)
            if (checkExistingPendingRequest(cleanPhone)) {
                return Result.failure(Exception("يوجد طلب انضمام مطعم قيد المراجعة بالفعل لرقم الهاتف هذا"))
            }

            val id = "${cleanPhone}_RESTAURANT"
            val hashedPassword = com.serviseyem.utils.PasswordHasher.hash(restaurant.rawPassword)
            val request = JoinRequestEntity(
                id = id,
                type = "RESTAURANT",
                status = "PENDING",
                businessName = restaurant.restaurantName.trim(),
                ownerName = restaurant.ownerName.trim(),
                fullName = restaurant.ownerName.trim(),
                phone = cleanPhone,
                passwordHash = hashedPassword,
                categoryId = restaurant.cuisineType.trim(),
                categoryName = restaurant.cuisineType.trim(),
                city = restaurant.city.trim(),
                area = restaurant.addressDetails.trim(),
                logoImage = restaurant.logoUrl,
                workImages = restaurant.menuImageUrls,
                approvalStatus = "PENDING",
                submittedAt = System.currentTimeMillis(),
                createdAt = System.currentTimeMillis(),
                updatedAt = System.currentTimeMillis()
            )

            val pendingMap = mapOf(
                "id" to id,
                "name" to restaurant.restaurantName.trim(),
                "ownerName" to restaurant.ownerName.trim(),
                "phone" to cleanPhone,
                "categoryId" to "RESTAURANT",
                "customCategoryName" to restaurant.cuisineType.trim(),
                "area" to restaurant.city.trim(),
                "localNeighborhood" to restaurant.addressDetails.trim(),
                "status" to "PENDING",
                "profession" to "STORE_OWNER",
                "providerType" to "STORE_OWNER",
                "password" to "",
                "passwordHash" to hashedPassword,
                "createdAt" to System.currentTimeMillis()
            )

            val batch = firestore.batch()
            batch.set(firestore.collection(AppConstants.COL_JOIN_REQUESTS).document(id), request)
            batch.set(firestore.collection("pending_providers").document(id), pendingMap)
            batch.commit().await()

            sendAdminJoinNotification(id, request.businessName, cleanPhone, "RESTAURANT")
            Result.success(id)
        } catch (e: Exception) {
            Log.e("RegistrationRepository", "Error registering restaurant", e)
            Result.failure(e)
        }
    }

    override suspend fun registerMedicalCenter(medical: RegistrationEntity.MedicalCenter): Result<String> {
        return try {
            val cleanPhone = ValidatePhoneUseCase.normalizePhone(medical.phone)
            if (checkExistingPendingRequest(cleanPhone)) {
                return Result.failure(Exception("يوجد طلب انضمام مركز طبي قيد المراجعة بالفعل لرقم الهاتف هذا"))
            }

            val id = "${cleanPhone}_MEDICAL"
            val hashedPassword = com.serviseyem.utils.PasswordHasher.hash(medical.rawPassword)
            val request = JoinRequestEntity(
                id = id,
                type = "MEDICAL",
                status = "PENDING",
                businessName = medical.centerName.trim(),
                ownerName = medical.doctorName.trim(),
                fullName = medical.doctorName.trim(),
                phone = cleanPhone,
                passwordHash = hashedPassword,
                categoryId = medical.specialtyCategory.trim(),
                categoryName = medical.specialtyCategory.trim(),
                city = medical.city.trim(),
                area = medical.addressDetails.trim(),
                logoImage = medical.logoUrl,
                approvalStatus = "PENDING",
                submittedAt = System.currentTimeMillis(),
                createdAt = System.currentTimeMillis(),
                updatedAt = System.currentTimeMillis()
            )

            val pendingMap = mapOf(
                "id" to id,
                "name" to medical.centerName.trim(),
                "ownerName" to medical.doctorName.trim(),
                "phone" to cleanPhone,
                "categoryId" to "MEDICAL",
                "customCategoryName" to medical.specialtyCategory.trim(),
                "area" to medical.city.trim(),
                "localNeighborhood" to medical.addressDetails.trim(),
                "status" to "PENDING",
                "profession" to "STORE_OWNER",
                "providerType" to "STORE_OWNER",
                "password" to "",
                "passwordHash" to hashedPassword,
                "createdAt" to System.currentTimeMillis()
            )

            val batch = firestore.batch()
            batch.set(firestore.collection(AppConstants.COL_JOIN_REQUESTS).document(id), request)
            batch.set(firestore.collection("pending_providers").document(id), pendingMap)
            batch.commit().await()

            sendAdminJoinNotification(id, request.businessName, cleanPhone, "MEDICAL")
            Result.success(id)
        } catch (e: Exception) {
            Log.e("RegistrationRepository", "Error registering medical center", e)
            Result.failure(e)
        }
    }

    override suspend fun registerProperty(property: RegistrationEntity.Property): Result<String> {
        return try {
            val cleanPhone = ValidatePhoneUseCase.normalizePhone(property.phone)
            if (checkExistingPendingRequest(cleanPhone)) {
                return Result.failure(Exception("يوجد طلب إضافة عقار قيد المراجعة بالفعل لرقم الهاتف هذا"))
            }

            val id = "${cleanPhone}_PROPERTY"
            val hashedPassword = com.serviseyem.utils.PasswordHasher.hash(property.rawPassword)
            val request = JoinRequestEntity(
                id = id,
                type = "PROPERTY",
                status = "PENDING",
                propertyTitle = property.title.trim(),
                propertyType = property.propertyType.trim(),
                categoryId = property.category.trim(),
                categoryName = property.category.trim(),
                ownerName = property.ownerName.trim(),
                fullName = property.ownerName.trim(),
                phone = cleanPhone,
                passwordHash = hashedPassword,
                city = property.city.trim(),
                area = property.areaDetails.trim(),
                price = property.priceYer,
                workImages = property.imageUrls,
                approvalStatus = "PENDING",
                submittedAt = System.currentTimeMillis(),
                createdAt = System.currentTimeMillis(),
                updatedAt = System.currentTimeMillis()
            )

            val pendingMap = mapOf(
                "id" to id,
                "name" to property.title.trim(),
                "ownerName" to property.ownerName.trim(),
                "phone" to cleanPhone,
                "categoryId" to "PROPERTY",
                "customCategoryName" to property.category.trim(),
                "area" to property.city.trim(),
                "localNeighborhood" to property.areaDetails.trim(),
                "status" to "PENDING",
                "profession" to "PROPERTY_OWNER",
                "providerType" to "PROPERTY_OWNER",
                "password" to "",
                "passwordHash" to hashedPassword,
                "createdAt" to System.currentTimeMillis()
            )

            val batch = firestore.batch()
            batch.set(firestore.collection(AppConstants.COL_JOIN_REQUESTS).document(id), request)
            batch.set(firestore.collection("pending_providers").document(id), pendingMap)
            batch.commit().await()

            sendAdminJoinNotification(id, request.propertyTitle, cleanPhone, "PROPERTY")
            Result.success(id)
        } catch (e: Exception) {
            Log.e("RegistrationRepository", "Error registering property", e)
            Result.failure(e)
        }
    }

    override suspend fun registerJob(job: RegistrationEntity.Job): Result<String> {
        return try {
            val cleanPhone = ValidatePhoneUseCase.normalizePhone(job.contactPhone)
            if (checkExistingPendingRequest(cleanPhone)) {
                return Result.failure(Exception("يوجد إعلان توظيف قيد المراجعة بالفعل لرقم الهاتف هذا"))
            }

            val id = "${cleanPhone}_JOB"
            val hashedPassword = com.serviseyem.utils.PasswordHasher.hash(job.rawPassword)
            val request = JoinRequestEntity(
                id = id,
                type = "JOB",
                status = "PENDING",
                jobTitle = job.jobTitle.trim(),
                companyName = job.companyName.trim(),
                businessName = job.companyName.trim(),
                jobRequirements = job.requirements.trim(),
                salaryRange = job.salaryRange.trim(),
                categoryId = job.category.trim(),
                categoryName = job.category.trim(),
                phone = cleanPhone,
                passwordHash = hashedPassword,
                city = job.city.trim(),
                area = job.city.trim(),
                approvalStatus = "PENDING",
                submittedAt = System.currentTimeMillis(),
                createdAt = System.currentTimeMillis(),
                updatedAt = System.currentTimeMillis()
            )

            val pendingMap = mapOf(
                "id" to id,
                "name" to job.jobTitle.trim(),
                "ownerName" to job.companyName.trim(),
                "phone" to cleanPhone,
                "categoryId" to "JOB",
                "customCategoryName" to job.category.trim(),
                "area" to job.city.trim(),
                "details" to job.requirements.trim(),
                "salaryRange" to job.salaryRange.trim(),
                "status" to "PENDING",
                "profession" to "JOB_POSTER",
                "providerType" to "JOB_POSTER",
                "password" to "",
                "passwordHash" to hashedPassword,
                "createdAt" to System.currentTimeMillis()
            )

            val batch = firestore.batch()
            batch.set(firestore.collection(AppConstants.COL_JOIN_REQUESTS).document(id), request)
            batch.set(firestore.collection("pending_providers").document(id), pendingMap)
            batch.commit().await()

            sendAdminJoinNotification(id, request.jobTitle, cleanPhone, "JOB")
            Result.success(id)
        } catch (e: Exception) {
            Log.e("RegistrationRepository", "Error registering job", e)
            Result.failure(e)
        }
    }

    override fun getJoinStatusFlow(phoneNumber: String): Flow<JoinStatusEntity?> = callbackFlow {
        val cleanPhone = ValidatePhoneUseCase.normalizePhone(phoneNumber)
        if (cleanPhone.isBlank()) {
            trySend(null)
            close()
            awaitClose { }
            return@callbackFlow
        }

        val listener: ListenerRegistration = firestore.collection(AppConstants.COL_JOIN_REQUESTS)
            .whereEqualTo("phone", cleanPhone)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    trySend(null)
                    return@addSnapshotListener
                }
                if (snapshot != null && !snapshot.isEmpty) {
                    // Get latest request by submittedAt/createdAt
                    val doc = snapshot.documents.maxByOrNull { it.getLong("submittedAt") ?: it.getLong("createdAt") ?: 0L }
                    if (doc != null) {
                        val name = doc.getString("fullName") 
                            ?: doc.getString("businessName") 
                            ?: doc.getString("propertyTitle") 
                            ?: doc.getString("jobTitle") 
                            ?: doc.getString("name") 
                            ?: ""
                        val entity = JoinStatusEntity(
                            requestId = doc.id,
                            applicantName = name,
                            registrationType = doc.getString("type") ?: doc.getString("role") ?: "PROVIDER",
                            status = doc.getString("status") ?: doc.getString("approvalStatus") ?: "PENDING",
                            rejectionReason = doc.getString("rejectionReason") ?: "",
                            createdAt = doc.getLong("submittedAt") ?: doc.getLong("createdAt") ?: 0L,
                            updatedAt = doc.getLong("updatedAt") ?: 0L
                        )
                        trySend(entity)
                    } else {
                        trySend(null)
                    }
                } else {
                    trySend(null)
                }
            }
        awaitClose { listener.remove() }
    }
}
