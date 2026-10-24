package com.serviseyem.ui.helpers

import android.content.Context
import com.serviseyem.ui.*
import com.serviseyem.data.NotificationEntity
import com.serviseyem.data.PendingProviderEntity
import com.serviseyem.data.StoreEntity
import com.serviseyem.data.PropertyEntity
import com.serviseyem.data.JobEntity
import com.serviseyem.data.models.JoinRequestEntity
import com.serviseyem.utils.FirebaseStorageUploader
import com.serviseyem.utils.SecurityCryptoUtils
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import java.util.UUID

/**
 * Helper class for handling provider/store/property/job join requests and registrations.
 */
class RegistrationHelper(
    private val db: FirebaseFirestore,
    private val auth: FirebaseAuth,
    private val preferenceHelper: AppPreferenceHelper
) {

    suspend fun uploadImageStringOrUri(
        context: Context,
        input: String,
        storagePath: String,
        maxSizeBytes: Long
    ): String {
        if (input.isBlank()) return ""
        if (input.startsWith("http://") || input.startsWith("https://")) return input
        return try {
            if (input.startsWith("content://") || input.startsWith("file://")) {
                val uri = android.net.Uri.parse(input)
                val res = FirebaseStorageUploader.uploadImageUri(
                    context, uri, storagePath, maxDimension = 800, maxSizeBytes = maxSizeBytes
                )
                res.getOrElse {
                    val compressed = FirebaseStorageUploader.compressImageToBytes(context, uri, maxDimension = 320, maxSizeBytes = 35 * 1024L)
                    if (compressed != null) {
                        "data:image/webp;base64," + android.util.Base64.encodeToString(compressed, android.util.Base64.NO_WRAP)
                    } else ""
                }
            } else {
                FirebaseStorageUploader.resolveBase64ToStorageUrl(input, storagePath, maxDimension = 800, maxSizeBytes = maxSizeBytes)
            }
        } catch (e: Exception) {
            e.printStackTrace()
            ""
        }
    }

    fun submitJoinForm(
        context: Context,
        scope: CoroutineScope,
        name: String,
        phone: String,
        catId: String,
        area: String,
        neighborhood: String,
        photoPath: String,
        idCardPath: String,
        gpsCoords: String,
        workPhotos: List<String> = emptyList(),
        customCategoryName: String = "",
        password: String = "",
        productAttachmentsJson: String = "",
        checkDuplicate: (String) -> String?,
        logAdminActivity: (String) -> Unit,
        triggerNotification: (String) -> Unit,
        addApplicantNotification: (String, String, String, String) -> Unit,
        onPendingAdded: (PendingProviderEntity) -> Unit,
        onStoreAdded: (StoreEntity) -> Unit,
        onPropertyAdded: (PropertyEntity) -> Unit,
        onJobAdded: (JobEntity) -> Unit,
        onClientAdded: (Map<String, Any>) -> Unit,
        onJoinRequestPhoneUpdated: (String) -> Unit,
        onNavigateToScreen: (String) -> Unit
    ) {
        val cleanPhone = com.serviseyem.domain.usecases.ValidatePhoneUseCase.normalizePhone(phone)
        val duplicateType = checkDuplicate(cleanPhone)
        if (duplicateType != null) {
            triggerNotification("❌ عذراً! رقم الهاتف ($phone) مسجل بالفعل كـ ($duplicateType). لا يُسمح بتكرار الحسابات.")
            logAdminActivity("محاولة تسجيل فني مكرر محجوبة لرقم: $cleanPhone - نوع التكرار: $duplicateType")
            return
        }

        scope.launch {
            try {
                // Synchronous check in join_requests
                val qs = com.google.android.gms.tasks.Tasks.await(
                    db.collection("join_requests").whereEqualTo("phone", cleanPhone).get()
                )
                if (!qs.isEmpty) {
                    triggerNotification("❌ يوجد طلب انضمام مسجل بالفعل قيد المراجعة لرقم الهاتف هذا")
                    return@launch
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }

            try {
                triggerNotification("⏳ جاري ضغط الصور وحفظ الملفات في سحابة التخزين...")
            val finalSelfie = uploadImageStringOrUri(
                context, photoPath,
                FirebaseStorageUploader.getProviderProfilePath(cleanPhone),
                maxSizeBytes = 150 * 1024L
            )
            val finalIdCard = uploadImageStringOrUri(
                context, idCardPath,
                FirebaseStorageUploader.getProviderIdCardPath(cleanPhone),
                maxSizeBytes = 150 * 1024L
            )
            val finalWorkPhotos = workPhotos.mapIndexed { idx, p ->
                uploadImageStringOrUri(
                    context, p,
                    FirebaseStorageUploader.getProviderWorkPhotoPath(cleanPhone, idx),
                    maxSizeBytes = 300 * 1024L
                )
            }
            val encSelfie = if (finalSelfie.isNotEmpty()) SecurityCryptoUtils.encrypt(finalSelfie) else ""
            val encIdCard = if (finalIdCard.isNotEmpty()) SecurityCryptoUtils.encrypt(finalIdCard) else ""
            if (password.isNotEmpty()) {
                val valResult = SecurityCryptoUtils.validatePasswordPolicy(password)
                if (valResult.first) {
                    val authEmail = "$cleanPhone@yemen-services.app"
                    auth.createUserWithEmailAndPassword(authEmail, password.trim())
                        .addOnFailureListener { /* Account might already exist */ }
                }
            }
            val requestType = when (catId.uppercase()) {
                "STORE" -> "STORE"
                "RESTAURANT" -> "RESTAURANT"
                "MEDICAL" -> "MEDICAL"
                "PROPERTY" -> "PROPERTY"
                "JOB" -> "JOB"
                "JOB_SEEKER" -> "JOB_SEEKER"
                "CLIENT" -> "CLIENT"
                else -> if (catId.contains("وظيف", ignoreCase = true) || customCategoryName.contains("باحث", ignoreCase = true) || customCategoryName.contains("متقدم", ignoreCase = true)) "JOB_SEEKER" else "PROVIDER"
            }
            val requestProfession = when (requestType) {
                "STORE", "RESTAURANT", "MEDICAL" -> "STORE_OWNER"
                "PROPERTY" -> "PROPERTY_OWNER"
                "JOB" -> "JOB_POSTER"
                "JOB_SEEKER" -> "JOB_SEEKER"
                "CLIENT" -> "CLIENT"
                else -> "PROVIDER"
            }
            val requestDocId = cleanPhone
            val currentAuthUid = com.google.firebase.auth.FirebaseAuth.getInstance().currentUser?.uid ?: ""
            val securedPasswordHash = if (password.isNotBlank()) com.serviseyem.utils.PasswordHasher.hash(password.trim()) else ""
            val newRequest = PendingProviderEntity(
                id = requestDocId,
                name = name,
                phone = cleanPhone,
                categoryId = catId,
                area = area,
                localNeighborhood = neighborhood,
                status = "PENDING",
                selfiePhotoBase64 = encSelfie,
                idPhotoBase64 = encIdCard,
                workPhotosBase64 = finalWorkPhotos,
                customCategoryName = customCategoryName,
                passwordHash = securedPasswordHash,
                productAttachmentsJson = productAttachmentsJson,
                profession = requestProfession,
                providerType = requestProfession
            )
            val coordParts = gpsCoords.split(",")
            val parsedLat = coordParts.getOrNull(0)?.trim()?.toDoubleOrNull() ?: 15.3694
            val parsedLng = coordParts.getOrNull(1)?.trim()?.toDoubleOrNull() ?: 44.1910

            // Push to Cloud
            val pendingDataMap = mapOf(
                "id" to requestDocId,
                "uid" to currentAuthUid,
                "name" to name,
                "phone" to cleanPhone,
                "categoryId" to catId,
                "area" to area,
                "localNeighborhood" to neighborhood,
                "gpsCoords" to gpsCoords,
                "latitude" to parsedLat,
                "longitude" to parsedLng,
                "status" to "PENDING",
                "selfiePhotoBase64" to encSelfie,
                "idPhotoBase64" to encIdCard,
                "workPhotosBase64" to finalWorkPhotos,
                "customCategoryName" to customCategoryName,
                "password" to securedPasswordHash,
                "passwordHash" to securedPasswordHash,
                "productAttachmentsJson" to productAttachmentsJson,
                "profession" to requestProfession,
                "providerType" to requestProfession
            )
            db.collection("pending_providers").document(requestDocId).set(pendingDataMap)
            val joinRequestMap = mapOf(
                "id" to requestDocId,
                "uid" to currentAuthUid,
                "userId" to currentAuthUid,
                "type" to requestType,
                "status" to "PENDING",
                "approvalStatus" to "PENDING",
                "fullName" to name,
                "phone" to cleanPhone,
                "password" to securedPasswordHash,
                "passwordHash" to securedPasswordHash,
                "city" to area,
                "area" to neighborhood,
                "neighborhood" to neighborhood,
                "gpsCoords" to gpsCoords,
                "latitude" to parsedLat,
                "longitude" to parsedLng,
                "categoryId" to catId,
                "categoryName" to customCategoryName.ifBlank { catId },
                "businessName" to (if (requestType == "STORE" || requestType == "RESTAURANT" || requestType == "MEDICAL") name else ""),
                "ownerName" to name,
                "propertyTitle" to (if (requestType == "PROPERTY") (if (customCategoryName.isNotBlank()) "$customCategoryName ($name)" else "مكتب/عقار ($name)") else ""),
                "jobTitle" to (if (requestType == "JOB") customCategoryName.ifBlank { name } else ""),
                "companyName" to (if (requestType == "JOB") name else ""),
                "profileImage" to finalSelfie,
                "idCardImage" to finalIdCard,
                "workImages" to finalWorkPhotos,
                "submittedAt" to System.currentTimeMillis(),
                "createdAt" to System.currentTimeMillis(),
                "updatedAt" to System.currentTimeMillis()
            )
            db.collection("join_requests").document(requestDocId).set(joinRequestMap)
                .addOnSuccessListener {
                    val adminNotifTitle = when (requestType) {
                        "STORE" -> "🏪 طلب انضمام متجر جديد"
                        "RESTAURANT" -> "🍔 طلب انضمام مطعم / كافيه جديد"
                        "MEDICAL" -> "🏥 طلب انضمام مركز طبي جديد"
                        "PROPERTY" -> "🏠 طلب إضافة عقار جديد"
                        "JOB" -> "💼 طلب إعلان وظيفة جديدة"
                        "JOB_SEEKER" -> "💼 طلب انضمام متقدم للوظائف"
                        "CLIENT" -> "👤 طلب تسجيل حساب عميل جديد"
                        else -> "🔧 طلب انضمام فني جديد"
                    }
                    val entityLabel = when (requestType) {
                        "STORE" -> "متجر / محل تجاري"
                        "RESTAURANT" -> "مطعم / كافيه"
                        "MEDICAL" -> "مركز طبي / عيادة"
                        "PROPERTY" -> "عقار"
                        "JOB" -> "إعلان وظيفي"
                        "JOB_SEEKER" -> "متقدم للوظيفة"
                        "CLIENT" -> "عميل / مستخدم عادي"
                        else -> "مهني / فني"
                    }
                    val adminNotif = NotificationEntity(
                        id = UUID.randomUUID().toString(),
                        title = adminNotifTitle,
                        message = "قدم $name طلب تسجيل جديد كـ ($entityLabel) في مدينة/منطقة $area.",
                        targetType = "SUPERVISOR",
                        targetValue = "ALL",
                        notificationType = "JOIN_REQUEST",
                        relatedRequestId = requestDocId,
                        timestamp = System.currentTimeMillis()
                    )
                    try {
                        if (adminNotif.isValid()) db.collection("notifications").document(adminNotif.id).set(adminNotif)
                    } catch (e: Exception) {}
                }
                .addOnFailureListener { e ->
                    val errorMsg = e.localizedMessage ?: "تأكد من صغر حجم الصور واتصالك بالإنترنت"
                    triggerNotification("❌ فشل تقديم الطلب: $errorMsg")
                }

            onPendingAdded(newRequest)
            preferenceHelper.setJoinRequestPhone(context, cleanPhone)
            onJoinRequestPhoneUpdated(cleanPhone)

            val userEntityLabel = when (requestType) {
                "STORE" -> "متجر / محل تجاري"
                "RESTAURANT" -> "مطعم / كافيه"
                "MEDICAL" -> "مركز طبي / عيادة"
                "PROPERTY" -> "عقار"
                "JOB" -> "إعلان وظيفة"
                "JOB_SEEKER" -> "متقدم للوظيفة"
                "CLIENT" -> "حساب عميل"
                else -> "فني / مهني"
            }
            addApplicantNotification(
                "📨 تم استلام طلب انضمامك بنجاح",
                "مرحباً $name، تم استلام طلب تسجيلك كـ ($userEntityLabel) وجاري مراجعته والتحقق من البيانات من قِبل إدارة التطبيق. نسعد بانضمامك وسنبلغك بإشعار فور التفعيل والاعتماد!",
                "USER",
                cleanPhone
            )

            triggerNotification("📨 تم إرسال طلب انضمامك بنجاح، وهو قيد المراجعة لدى الإدارة")
            onNavigateToScreen("JOIN_REQUEST_STATUS")
            } catch (e: Exception) {
                e.printStackTrace()
                com.serviseyem.utils.AppErrorLogManager.logFirestoreError("RegistrationHelper", "Error in submitJoinForm", e)
                triggerNotification("❌ حدث خطأ أثناء إرسال الطلب: ${e.localizedMessage ?: "يرجى المحاولة مجدداً"}")
            }
        }
    }

    fun registerClientUser(
        name: String,
        phone: String,
        residence: String,
        password: String = "",
        onClientAdded: (Map<String, Any>) -> Unit
    ) {
        val cleanPhone = com.serviseyem.domain.usecases.ValidatePhoneUseCase.normalizePhone(phone)
        val currentAuthUid = com.google.firebase.auth.FirebaseAuth.getInstance().currentUser?.uid ?: ""
        val hashedPassword = if (password.isNotBlank()) com.serviseyem.utils.PasswordHasher.hash(password.trim()) else ""
        val userMap = mapOf(
            "id" to cleanPhone,
            "uid" to currentAuthUid,
            "userId" to currentAuthUid,
            "name" to name,
            "phone" to cleanPhone,
            "residence" to residence,
            "password" to hashedPassword,
            "passwordHash" to hashedPassword,
            "isApproved" to false,
            "createdAt" to System.currentTimeMillis()
        )
        try {
            db.collection("registered_users").document(cleanPhone).set(userMap)
            db.collection("users").document(cleanPhone).set(userMap)
        } catch (e: Exception) {}
        onClientAdded(userMap)
    }

    fun cancelOrResetJoinRequest(
        context: Context,
        phone: String,
        pendingProviders: List<PendingProviderEntity>,
        onPendingRemoved: (String) -> Unit,
        onPhoneCleared: () -> Unit,
        onGoBack: () -> Boolean
    ) {
        val cleanPhone = com.serviseyem.domain.usecases.ValidatePhoneUseCase.normalizePhone(phone)
        if (cleanPhone.isNotEmpty()) {
            val matching = pendingProviders.find { 
                com.serviseyem.domain.usecases.ValidatePhoneUseCase.normalizePhone(it.phone) == cleanPhone || it.id == cleanPhone || it.id == phone 
            }
            matching?.let {
                onPendingRemoved(it.id)
                try {
                    db.collection("pending_providers").document(it.id).delete()
                } catch (e: Exception) {}
            }
            try {
                db.collection("pending_providers").document(cleanPhone).delete()
                db.collection("join_requests").document(cleanPhone).delete()
                if (phone != cleanPhone) {
                    db.collection("pending_providers").document(phone).delete()
                    db.collection("join_requests").document(phone).delete()
                }
            } catch (e: Exception) {}
        }
        preferenceHelper.clearJoinRequestPhone(context)
        onPhoneCleared()
        onGoBack()
    }

    fun setJoinRequestPhone(
        context: Context,
        phone: String,
        onPhoneUpdated: (String) -> Unit
    ) {
        val cleanPhone = com.serviseyem.domain.usecases.ValidatePhoneUseCase.normalizePhone(phone)
        preferenceHelper.setJoinRequestPhone(context, cleanPhone)
        onPhoneUpdated(cleanPhone)
    }
}
