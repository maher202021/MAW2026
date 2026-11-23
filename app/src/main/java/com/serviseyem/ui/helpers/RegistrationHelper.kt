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
        ownerName: String = "",
        description: String = "",
        workingHours: String = "",
        hasDelivery: Boolean = false,
        hasEmergency: Boolean = false,
        experienceYears: String = "",
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
        onNavigateToScreen: (String) -> Unit,
        onResult: (Boolean, String) -> Unit = { _, _ -> }
    ) {
        val cleanPhone = com.serviseyem.domain.usecases.ValidatePhoneUseCase.normalizePhone(phone).filter { it.isDigit() }
        if (cleanPhone.length < 7) {
            val msg = "رقم الهاتف غير صحيح. يجب أن يتكون من 9 أرقام ويبدأ بـ (77, 73, 71, 70, 78)"
            triggerNotification("❌ $msg")
            onResult(false, msg)
            return
        }
        val duplicateType = checkDuplicate(cleanPhone)
        if (duplicateType != null) {
            val msg = "عذراً! رقم الهاتف ($phone) مسجل بالفعل كـ ($duplicateType). لا يُسمح بتكرار الحسابات."
            triggerNotification("❌ $msg")
            logAdminActivity("محاولة تسجيل مكرر محجوبة لرقم: $cleanPhone - نوع التكرار: $duplicateType")
            onResult(false, msg)
            return
        }

        scope.launch {
            try {
                val phoneVariants = listOf(
                    cleanPhone,
                    phone.trim(),
                    "0$cleanPhone",
                    "+967$cleanPhone",
                    "967$cleanPhone"
                ).filter { it.isNotBlank() }.distinct()

                val existingConflict = kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
                    var foundMsg: String? = null
                    for (ph in phoneVariants) {
                        val jrSnap = runCatching {
                            com.google.android.gms.tasks.Tasks.await(
                                db.collection("join_requests").whereEqualTo("phone", ph).get()
                            )
                        }.getOrNull()
                        val hasActiveJoin = jrSnap?.documents?.any { doc ->
                            val st = (doc.getString("status") ?: doc.getString("approvalStatus") ?: "").uppercase()
                            st in listOf("PENDING", "APPROVED", "ACCEPTED")
                        } == true
                        if (hasActiveJoin) {
                            foundMsg = "يوجد طلب انضمام أو حساب نشط مسجل بالفعل لرقم الهاتف هذا"
                            break
                        }

                        val pendSnap = runCatching {
                            com.google.android.gms.tasks.Tasks.await(
                                db.collection("pending_providers").whereEqualTo("phone", ph).get()
                            )
                        }.getOrNull()
                        val hasActivePending = pendSnap?.documents?.any { doc ->
                            val st = (doc.getString("status") ?: "PENDING").uppercase()
                            st == "PENDING"
                        } == true
                        if (hasActivePending) {
                            foundMsg = "يوجد طلب انضمام قيد المراجعة بالفعل لرقم الهاتف هذا"
                            break
                        }

                        val userExists = runCatching {
                            !com.google.android.gms.tasks.Tasks.await(
                                db.collection("registered_users").whereEqualTo("phone", ph).limit(1).get()
                            ).isEmpty || com.google.android.gms.tasks.Tasks.await(
                                db.collection("registered_users").document(ph).get()
                            ).exists()
                        }.getOrDefault(false)
                        if (userExists) {
                            foundMsg = "يوجد حساب مستخدم مسجل بالفعل لرقم الهاتف هذا"
                            break
                        }
                    }
                    foundMsg
                }

                if (existingConflict != null) {
                    triggerNotification("❌ $existingConflict")
                    onResult(false, existingConflict)
                    return@launch
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }

            try {
                triggerNotification("⏳ جاري معالجة الطلب وحفظ البيانات...")
                val (finalSelfie, finalIdCard, finalWorkPhotos) = kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
                    val selfie = uploadImageStringOrUri(
                        context, photoPath,
                        FirebaseStorageUploader.getProviderProfilePath(cleanPhone),
                        maxSizeBytes = 150 * 1024L
                    )
                    val idCard = uploadImageStringOrUri(
                        context, idCardPath,
                        FirebaseStorageUploader.getProviderIdCardPath(cleanPhone),
                        maxSizeBytes = 150 * 1024L
                    )
                    val work = workPhotos.mapIndexed { idx, p ->
                        uploadImageStringOrUri(
                            context, p,
                            FirebaseStorageUploader.getProviderWorkPhotoPath(cleanPhone, idx),
                            maxSizeBytes = 300 * 1024L
                        )
                    }
                    Triple(selfie, idCard, work)
                }

                val encSelfie = if (finalSelfie.isNotEmpty()) SecurityCryptoUtils.encrypt(finalSelfie) else ""
                val encIdCard = if (finalIdCard.isNotEmpty()) SecurityCryptoUtils.encrypt(finalIdCard) else ""
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
                val resolvedOwnerName = ownerName.trim().ifBlank { name.trim() }
                val cleanName = name.trim()
                val requestDocId = cleanPhone
                val currentAuthUid = com.google.firebase.auth.FirebaseAuth.getInstance().currentUser?.uid ?: ""
                val resolvedOwnerId = currentAuthUid.ifBlank { cleanPhone }
                val securedPasswordHash = if (password.isNotBlank()) com.serviseyem.utils.PasswordHasher.hash(password.trim()) else ""
                val now = System.currentTimeMillis()
                val coordParts = gpsCoords.split(",")
                val parsedLat = coordParts.getOrNull(0)?.trim()?.toDoubleOrNull()?.takeIf { it in -90.0..90.0 } ?: 0.0
                val parsedLng = coordParts.getOrNull(1)?.trim()?.toDoubleOrNull()?.takeIf { it in -180.0..180.0 } ?: 0.0
                val normalizedCoords = if (com.serviseyem.utils.LandmarkAddress.isValid(parsedLat, parsedLng)) {
                    "$parsedLat,$parsedLng"
                } else ""
                val newRequest = PendingProviderEntity(
                    id = requestDocId,
                    name = cleanName,
                    phone = cleanPhone,
                    categoryId = catId,
                    area = area,
                    localNeighborhood = neighborhood,
                    status = "PENDING",
                    selfiePhotoBase64 = encSelfie,
                    idPhotoBase64 = encIdCard,
                    workPhotosBase64 = finalWorkPhotos,
                    customCategoryName = customCategoryName,
                    specialization = customCategoryName,
                    passwordHash = securedPasswordHash,
                    productAttachmentsJson = productAttachmentsJson,
                    profession = requestProfession,
                    providerType = requestProfession,
                    ownerName = resolvedOwnerName,
                    description = description.trim(),
                    workingHours = workingHours.trim(),
                    hasDelivery = hasDelivery,
                    hasEmergency = hasEmergency,
                    experienceYears = experienceYears.trim(),
                    submittedAt = now,
                    ownerId = resolvedOwnerId,
                    uid = currentAuthUid,
                    gpsCoords = normalizedCoords,
                    latitude = parsedLat,
                    longitude = parsedLng
                )

                // Push to Cloud safely on IO dispatcher
                val pendingDataMap = mapOf(
                    "id" to requestDocId,
                    "uid" to currentAuthUid,
                    "ownerId" to resolvedOwnerId,
                    "name" to cleanName,
                    "ownerName" to resolvedOwnerName,
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
                    "specialization" to customCategoryName,
                    "description" to description.trim(),
                    "workingHours" to workingHours.trim(),
                    "hasDelivery" to hasDelivery,
                    "hasEmergency" to hasEmergency,
                    "experienceYears" to experienceYears.trim(),
                    "submittedAt" to now,
                    "createdAt" to now,
                    "password" to "",
                    "passwordHash" to securedPasswordHash,
                    "productAttachmentsJson" to productAttachmentsJson,
                    "profession" to requestProfession,
                    "providerType" to requestProfession
                )
                val joinRequestMap = mapOf(
                    "id" to requestDocId,
                    "uid" to currentAuthUid,
                    "userId" to resolvedOwnerId,
                    "type" to requestType,
                    "status" to "PENDING",
                    "approvalStatus" to "PENDING",
                    "fullName" to resolvedOwnerName,
                    "phone" to cleanPhone,
                    "password" to "",
                    "passwordHash" to securedPasswordHash,
                    "city" to area,
                    "area" to neighborhood,
                    "neighborhood" to neighborhood,
                    "gpsCoords" to gpsCoords,
                    "latitude" to parsedLat,
                    "longitude" to parsedLng,
                    "categoryId" to catId,
                    "categoryName" to customCategoryName.ifBlank { catId },
                    "specialization" to customCategoryName,
                    "description" to description.trim(),
                    "workingHours" to workingHours.trim(),
                    "hasDelivery" to hasDelivery,
                    "hasEmergency" to hasEmergency,
                    "experienceYears" to experienceYears.trim(),
                    "businessName" to (if (requestType == "STORE" || requestType == "RESTAURANT" || requestType == "MEDICAL") cleanName else ""),
                    "ownerName" to resolvedOwnerName,
                    "propertyTitle" to (if (requestType == "PROPERTY") (if (customCategoryName.isNotBlank()) "$customCategoryName ($cleanName)" else cleanName) else ""),
                    "jobTitle" to (if (requestType == "JOB") customCategoryName.ifBlank { cleanName } else ""),
                    "companyName" to (if (requestType == "JOB") cleanName else ""),
                    "profileImage" to finalSelfie,
                    "logoImage" to finalSelfie,
                    "idCardImage" to finalIdCard,
                    "workImages" to finalWorkPhotos,
                    "submittedAt" to now,
                    "createdAt" to now,
                    "updatedAt" to now
                )

                kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
                    com.google.android.gms.tasks.Tasks.await(
                        db.collection("pending_providers").document(requestDocId).set(pendingDataMap)
                    )
                    com.google.android.gms.tasks.Tasks.await(
                        db.collection("join_requests").document(requestDocId).set(joinRequestMap)
                    )
                }

                val adminNotifTitle = when (requestType) {
                    "STORE" -> "🏪 طلب انضمام متجر جديد"
                    "RESTAURANT" -> "🍔 طلب انضمام مطعم / كافيه جديد"
                    "MEDICAL" -> "🏥 طلب انضمام مركز طبي جديد"
                    "PROPERTY" -> "🏠 طلب إضافة عقار / مكتب عقاري"
                    "JOB" -> "💼 طلب إعلان وظيفة جديدة"
                    "JOB_SEEKER" -> "💼 طلب انضمام باحث عن عمل"
                    "CLIENT" -> "👤 طلب تسجيل حساب عميل جديد"
                    else -> "🔧 طلب انضمام فني جديد"
                }
                val entityLabel = when (requestType) {
                    "STORE" -> "متجر / محل تجاري"
                    "RESTAURANT" -> "مطعم / كافيه"
                    "MEDICAL" -> "مركز طبي / عيادة"
                    "PROPERTY" -> "عقار / مكتب عقاري"
                    "JOB" -> "إعلان وظيفي"
                    "JOB_SEEKER" -> "باحث عن عمل"
                    "CLIENT" -> "عميل / مستخدم عادي"
                    else -> "مهني / فني"
                }
                val adminNotif = NotificationEntity(
                    id = UUID.randomUUID().toString(),
                    title = adminNotifTitle,
                    message = "قدم $cleanName طلب تسجيل جديد كـ ($entityLabel) في مدينة/منطقة $area.",
                    targetType = "SUPERVISOR",
                    targetValue = "ALL",
                    notificationType = "JOIN_REQUEST",
                    relatedRequestId = requestDocId,
                    timestamp = now
                )
                try {
                    if (adminNotif.isValid()) db.collection("notifications").document(adminNotif.id).set(adminNotif)
                } catch (_: Exception) {}

                onPendingAdded(newRequest)
                preferenceHelper.setJoinRequestPhone(context, cleanPhone)
                onJoinRequestPhoneUpdated(cleanPhone)

                val userEntityLabel = when (requestType) {
                    "STORE" -> "متجر / محل تجاري"
                    "RESTAURANT" -> "مطعم / كافيه"
                    "MEDICAL" -> "مركز طبي / عيادة"
                    "PROPERTY" -> "عقار / مكتب عقاري"
                    "JOB" -> "إعلان وظيفة"
                    "JOB_SEEKER" -> "باحث عن عمل"
                    "CLIENT" -> "حساب عميل"
                    else -> "فني / مهني"
                }
                addApplicantNotification(
                    "📨 تم استلام طلب انضمامك بنجاح",
                    "مرحباً $resolvedOwnerName، تم استلام طلب تسجيلك كـ ($userEntityLabel) وجاري مراجعته والتحقق من البيانات من قِبل إدارة التطبيق. نسعد بانضمامك وسنبلغك بإشعار فور التفعيل والاعتماد!",
                    "USER",
                    cleanPhone
                )

                val okMsg = "تم إرسال طلب انضمامك بنجاح، وهو قيد المراجعة لدى الإدارة"
                triggerNotification("📨 $okMsg")
                onResult(true, okMsg)
                onNavigateToScreen("JOIN_REQUEST_STATUS")
            } catch (e: Exception) {
                e.printStackTrace()
                com.serviseyem.utils.AppErrorLogManager.logFirestoreError("RegistrationHelper", "Error in submitJoinForm", e)
                val errMsg = "حدث خطأ أثناء إرسال الطلب: ${e.localizedMessage ?: "يرجى المحاولة مجدداً"}"
                triggerNotification("❌ $errMsg")
                onResult(false, errMsg)
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
