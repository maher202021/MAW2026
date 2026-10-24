package com.serviseyem.ui.helpers
import com.serviseyem.data.isValid

import android.content.Context
import com.serviseyem.ui.*
import com.serviseyem.data.NotificationEntity
import com.serviseyem.data.PropertyEntity
import com.serviseyem.data.ProviderEntity
import com.serviseyem.data.StoreEntity
import com.serviseyem.ui.MainViewModel.RestoreAccountMatch
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.cancelChildren
import kotlinx.coroutines.launch
import kotlinx.coroutines.supervisorScope

/**
 * Helper class for searching accounts and managing password recovery / reset operations.
 */
class AccountRecoveryHelper(
    private val db: FirebaseFirestore,
    private val preferenceHelper: AppPreferenceHelper
) {

    private val recoveryScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

    fun clear() {
        recoveryScope.coroutineContext.cancelChildren()
    }

    fun searchAccountForRestore(cleanPhone: String, onResult: (RestoreAccountMatch?) -> Unit) {
        val normalizedPhone = com.serviseyem.domain.usecases.ValidatePhoneUseCase.normalizePhone(cleanPhone).filter { it.isDigit() }
        if (normalizedPhone.isBlank() || normalizedPhone.length < 7) {
            onResult(null)
            return
        }
        val phoneVariants = listOf(
            normalizedPhone,
            cleanPhone.trim(),
            "0$normalizedPhone",
            "+967$normalizedPhone",
            "967$normalizedPhone",
            "00967$normalizedPhone"
        ).filter { it.isNotBlank() }.distinct()

        recoveryScope.launch {
            try {
                val match = supervisorScope {
                    val providerDef = async(Dispatchers.IO) {
                        runCatching {
                            for (ph in phoneVariants) {
                                val snap = com.google.android.gms.tasks.Tasks.await(
                                    db.collection("providers").whereEqualTo("phone", ph).limit(5).get()
                                )
                                val candidates = snap.documents.mapNotNull { doc ->
                                    doc.toObject(ProviderEntity::class.java)?.let { entity ->
                                        if (entity.id.isBlank()) entity.copy(id = doc.id) else entity
                                    }
                                }
                                val chosen = candidates.firstOrNull { !it.isDeleted } ?: candidates.firstOrNull()
                                if (chosen != null) {
                                    return@runCatching RestoreAccountMatch(
                                        type = "PROVIDER",
                                        name = chosen.name.ifBlank { "مزود خدمة" },
                                        provider = chosen
                                    )
                                }
                            }
                            null
                        }.getOrNull()
                    }
                    val storeDef = async(Dispatchers.IO) {
                        runCatching {
                            for (ph in phoneVariants) {
                                val snap = com.google.android.gms.tasks.Tasks.await(
                                    db.collection("stores").whereEqualTo("phone", ph).limit(5).get()
                                )
                                val candidates = snap.documents.mapNotNull { doc ->
                                    doc.toObject(StoreEntity::class.java)?.let { entity ->
                                        if (entity.id.isBlank()) entity.copy(id = doc.id) else entity
                                    }
                                }
                                val chosen = candidates.firstOrNull { !it.isDeleted } ?: candidates.firstOrNull()
                                if (chosen != null) {
                                    val resolvedType = when (chosen.sectionId.lowercase().trim()) {
                                        "restaurants", "restaurant" -> "RESTAURANT"
                                        "medical" -> "MEDICAL"
                                        else -> "STORE"
                                    }
                                    return@runCatching RestoreAccountMatch(
                                        type = resolvedType,
                                        name = chosen.name.ifBlank { "متجر معتمد" },
                                        store = chosen
                                    )
                                }
                            }
                            null
                        }.getOrNull()
                    }
                    val propDef = async(Dispatchers.IO) {
                        runCatching {
                            for (ph in phoneVariants) {
                                val snap = com.google.android.gms.tasks.Tasks.await(
                                    db.collection("properties").whereEqualTo("phone", ph).limit(5).get()
                                )
                                val candidates = snap.documents.mapNotNull { doc ->
                                    doc.toObject(PropertyEntity::class.java)?.let { entity ->
                                        if (entity.id.isBlank()) entity.copy(id = doc.id) else entity
                                    }
                                }
                                val chosen = candidates.firstOrNull { !it.isDeleted } ?: candidates.firstOrNull()
                                if (chosen != null) {
                                    return@runCatching RestoreAccountMatch(
                                        type = "PROPERTY",
                                        name = chosen.title.ifBlank { "مكتب عقارات" },
                                        property = chosen
                                    )
                                }
                            }
                            null
                        }.getOrNull()
                    }
                    val jobDef = async(Dispatchers.IO) {
                        runCatching {
                            for (ph in phoneVariants) {
                                val snap = com.google.android.gms.tasks.Tasks.await(
                                    db.collection("jobs").whereEqualTo("phone", ph).limit(1).get()
                                )
                                val doc = snap.documents.firstOrNull()
                                if (doc != null) {
                                    val jName = doc.getString("title")?.takeIf { it.isNotBlank() }
                                        ?: doc.getString("jobTitle")?.takeIf { it.isNotBlank() }
                                        ?: doc.getString("companyName")?.takeIf { it.isNotBlank() }
                                        ?: "معلن وظائف"
                                    return@runCatching RestoreAccountMatch("JOB", jName)
                                }
                            }
                            null
                        }.getOrNull()
                    }
                    val userDef = async(Dispatchers.IO) {
                        runCatching {
                            for (ph in phoneVariants) {
                                val snap = com.google.android.gms.tasks.Tasks.await(
                                    db.collection("users").whereEqualTo("phone", ph).limit(1).get()
                                )
                                val doc = snap.documents.firstOrNull() ?: runCatching {
                                    com.google.android.gms.tasks.Tasks.await(
                                        db.collection("registered_users").whereEqualTo("phone", ph).limit(1).get()
                                    ).documents.firstOrNull()
                                }.getOrNull() ?: runCatching {
                                    com.google.android.gms.tasks.Tasks.await(
                                        db.collection("registered_users").document(ph).get()
                                    ).takeIf { it.exists() }
                                }.getOrNull()
                                if (doc != null) {
                                    val uName = doc.getString("name")?.takeIf { it.isNotBlank() }
                                        ?: doc.getString("fullName")?.takeIf { it.isNotBlank() }
                                        ?: "مستخدم مسجل"
                                    return@runCatching RestoreAccountMatch("CLIENT", uName)
                                }
                            }
                            null
                        }.getOrNull()
                    }
                    val joinReqDef = async(Dispatchers.IO) {
                        runCatching {
                            for (ph in phoneVariants) {
                                val snap = com.google.android.gms.tasks.Tasks.await(
                                    db.collection("join_requests").whereEqualTo("phone", ph).limit(1).get()
                                )
                                val rDoc = snap.documents.firstOrNull()
                                if (rDoc != null) {
                                    val rName = rDoc.getString("fullName")?.takeIf { it.isNotBlank() }
                                        ?: rDoc.getString("businessName")?.takeIf { it.isNotBlank() }
                                        ?: rDoc.getString("propertyTitle")?.takeIf { it.isNotBlank() }
                                        ?: rDoc.getString("jobTitle")?.takeIf { it.isNotBlank() }
                                        ?: rDoc.getString("name")?.takeIf { it.isNotBlank() }
                                        ?: "حساب مسجل"
                                    val rType = rDoc.getString("type")?.takeIf { it.isNotBlank() } ?: "CLIENT"
                                    return@runCatching RestoreAccountMatch(rType, rName)
                                }
                            }
                            null
                        }.getOrNull()
                    }

                    val results = awaitAll(providerDef, storeDef, propDef, jobDef, userDef, joinReqDef)
                    results.firstOrNull { it != null }
                }
                onResult(match)
            } catch (e: Exception) {
                onResult(null)
            }
        }
    }

    fun verifyRestorePassword(cleanPhone: String, accountType: String, passwordInput: String, onResult: (Boolean) -> Unit) {
        val trimmedInput = passwordInput.trim()
        val normalizedPhone = com.serviseyem.domain.usecases.ValidatePhoneUseCase.normalizePhone(cleanPhone).filter { it.isDigit() }
        if (trimmedInput.isBlank() || normalizedPhone.isBlank() || normalizedPhone.length < 7) {
            onResult(false)
            return
        }
        val primaryCols = when (accountType.uppercase().trim()) {
            "PROVIDER" -> listOf("providers")
            "STORE", "RESTAURANT", "MEDICAL" -> listOf("stores")
            "PROPERTY" -> listOf("properties")
            "JOB" -> listOf("jobs")
            "CLIENT", "USER" -> listOf("users", "registered_users")
            else -> listOf("join_requests", "users", "registered_users")
        }
        recoveryScope.launch {
            try {
                val isVerified = kotlinx.coroutines.withContext(Dispatchers.IO) {
                    val candidateCols = (primaryCols + listOf(
                        "registered_users",
                        "users",
                        "providers",
                        "stores",
                        "properties",
                        "jobs",
                        "join_requests",
                        "pending_providers"
                    )).distinct()
                    val phoneVariants = listOf(
                        normalizedPhone,
                        cleanPhone.trim(),
                        "0$normalizedPhone",
                        "+967$normalizedPhone",
                        "967$normalizedPhone",
                        "00967$normalizedPhone"
                    ).filter { it.isNotBlank() }.distinct()

                    var foundAnyStoredCredentialInPrimary = false

                    for (col in candidateCols) {
                        // إذا وُجدت كلمة مرور في المجموعة الأساسية للكيان ولم تتطابق، نرفض الرجوع إلى مجموعات ثانوية قديمة
                        if (col !in primaryCols && foundAnyStoredCredentialInPrimary) {
                            continue
                        }
                        for (ph in phoneVariants) {
                            val snap = runCatching {
                                com.google.android.gms.tasks.Tasks.await(
                                    db.collection(col).whereEqualTo("phone", ph).get()
                                )
                            }.getOrNull()
                            val docs = snap?.documents.orEmpty().toMutableList()
                            if (col in listOf("users", "registered_users")) {
                                runCatching {
                                    val directDoc = com.google.android.gms.tasks.Tasks.await(
                                        db.collection(col).document(ph).get()
                                    )
                                    if (directDoc.exists() && docs.none { it.id == directDoc.id }) {
                                        docs.add(directDoc)
                                    }
                                }
                            }
                            for (doc in docs) {
                                val hashFieldVal = doc.getString("passwordHash")?.trim().orEmpty()
                                val passFieldVal = doc.getString("password")?.trim().orEmpty()
                                val storedHash = hashFieldVal.ifBlank { passFieldVal }
                                val fieldUsed = if (hashFieldVal.isNotBlank()) "passwordHash" else "password"

                                if (storedHash.isNotBlank()) {
                                    if (col in primaryCols) {
                                        foundAnyStoredCredentialInPrimary = true
                                    }
                                    val matched = com.serviseyem.utils.PasswordHasher.verifyPassword(trimmedInput, storedHash) ||
                                        com.serviseyem.utils.SecurityCryptoUtils.verifyAdminPassword(trimmedInput, storedHash) ||
                                        com.serviseyem.utils.SecureAdminStorage.verifyAndMigrate(
                                            docRef = doc.reference,
                                            inputPassword = trimmedInput,
                                            storedPassOrHash = storedHash,
                                            fieldName = fieldUsed
                                        )
                                    if (matched) return@withContext true
                                }
                            }
                        }
                    }

                    // التحقق الاحتياطي من طلبات استعادة كلمة المرور المعتمدة (RESOLVED / APPROVED / ACCEPTED) في حال تم التعيين من قبل الإدارة
                    for (resetCol in listOf("password_recovery_requests", "password_resets")) {
                        val resetDoc = runCatching {
                            com.google.android.gms.tasks.Tasks.await(
                                db.collection(resetCol).document(normalizedPhone).get()
                            )
                        }.getOrNull()
                        val status = resetDoc?.getString("status")?.uppercase()?.trim().orEmpty()
                        if (resetDoc != null && resetDoc.exists() && status in listOf("RESOLVED", "APPROVED", "ACCEPTED")) {
                            val resetHash = resetDoc.getString("passwordHash")?.trim().orEmpty()
                                .ifBlank { resetDoc.getString("tempPassword")?.trim().orEmpty() }
                            val rawNewPass = resetDoc.getString("newPassword")?.trim().orEmpty()
                            val decryptedNewPass = if (rawNewPass.startsWith("gcm:") || rawNewPass.startsWith("gcmx:") || rawNewPass.startsWith("enc::")) {
                                com.serviseyem.utils.SecurityCryptoUtils.decryptCrossDevice(rawNewPass)
                            } else if (!com.serviseyem.utils.SecureHasher.isValidHash(rawNewPass)) {
                                rawNewPass
                            } else ""

                            val resetMatched = (resetHash.isNotBlank() && (
                                    com.serviseyem.utils.PasswordHasher.verifyPassword(trimmedInput, resetHash) ||
                                    com.serviseyem.utils.SecurityCryptoUtils.verifyAdminPassword(trimmedInput, resetHash)
                                )) || (decryptedNewPass.isNotBlank() && trimmedInput == decryptedNewPass)

                            if (resetMatched) {
                                val finalHash = if (com.serviseyem.utils.SecureHasher.isValidHash(resetHash)) {
                                    resetHash
                                } else {
                                    com.serviseyem.utils.PasswordHasher.hash(trimmedInput)
                                }
                                // مزامنة التجزئة الجديدة مع المجموعات الأساسية ومسح كلمة المرور المؤقتة المعروضة بعد التأكد من نجاح التحديث
                                var updatedAny = false
                                for (col in primaryCols) {
                                    for (ph in phoneVariants) {
                                        runCatching {
                                            val s = com.google.android.gms.tasks.Tasks.await(
                                                db.collection(col).whereEqualTo("phone", ph).get()
                                            )
                                            for (d in s.documents) {
                                                com.google.android.gms.tasks.Tasks.await(
                                                    d.reference.update(mapOf("password" to "", "passwordHash" to finalHash))
                                                )
                                                updatedAny = true
                                            }
                                        }
                                    }
                                }
                                if (updatedAny) {
                                    runCatching {
                                        resetDoc.reference.update("newPassword", "")
                                    }
                                }
                                return@withContext true
                            }
                        }
                    }
                    false
                }
                onResult(isVerified)
            } catch (e: Exception) {
                onResult(false)
            }
        }
    }

    fun requestPasswordReset(
        context: Context,
        phone: String,
        name: String,
        accountType: String,
        onPasswordWaitingPhoneSet: (String) -> Unit,
        triggerNotification: (String) -> Unit,
        onResult: (Boolean) -> Unit,
        channel: String = "IN_APP_CHAT"
    ) {
        val cleanPhone = com.serviseyem.domain.usecases.ValidatePhoneUseCase.normalizePhone(phone).filter { it.isDigit() }
        if (cleanPhone.isBlank() || cleanPhone.length < 7) {
            onResult(false)
            return
        }
        val safeName = name.trim().ifBlank { "مستخدم ($cleanPhone)" }
        val safeType = accountType.trim().ifBlank { "USER" }
        val safeChannel = channel.trim().ifBlank { "IN_APP_CHAT" }
        val currentUid = com.google.firebase.auth.FirebaseAuth.getInstance().currentUser?.uid ?: ""
        val now = System.currentTimeMillis()
        val reqData = mapOf(
            "id" to cleanPhone,
            "uid" to currentUid,
            "phone" to cleanPhone,
            "name" to safeName,
            "accountType" to safeType,
            "channel" to safeChannel,
            "status" to "PENDING",
            "createdAt" to now,
            "requestedAt" to now,
            "timestamp" to now,
            "newPassword" to "",
            "passwordHash" to "",
            "adminNotes" to "القناة: $safeChannel"
        )
        db.collection("password_recovery_requests").document(cleanPhone).set(reqData).addOnSuccessListener {
            runCatching {
                db.collection("password_resets").document(cleanPhone).set(
                    mapOf(
                        "id" to cleanPhone,
                        "uid" to currentUid,
                        "phone" to cleanPhone,
                        "channel" to safeChannel,
                        "status" to "PENDING",
                        "newPassword" to "",
                        "passwordHash" to "",
                        "createdAt" to now,
                        "requestedAt" to now,
                        "timestamp" to now
                    ),
                    com.google.firebase.firestore.SetOptions.merge()
                )
            }
            onPasswordWaitingPhoneSet(cleanPhone)
            preferenceHelper.setPasswordRecoveryWaitingPhone(context, cleanPhone)
            val adminNotif = NotificationEntity(
                id = "PWD_NOTIF_$cleanPhone",
                title = "🔑 طلب استعادة كلمة مرور ($safeName)",
                message = "قدم $safeName ($safeType) ذو الرقم $cleanPhone طلباً لاستعادة وتعيين كلمة المرور عبر قناة ($safeChannel).",
                targetType = "SUPERVISOR",
                targetValue = "ALL",
                timestamp = now,
                dedupKey = "PWD_RESET_${cleanPhone}"
            )
            try { if (adminNotif.isValid()) db.collection("notifications").document(adminNotif.id).set(adminNotif) } catch (e: Exception) {}
            
            // Log in activity_logs (بدون أي كلمة مرور أو هاش صريح)
            val logId = db.collection("activity_logs").document().id
            val log = com.serviseyem.data.ActivityLogEntity(
                id = logId,
                action = "🔑 طلب استعادة كلمة مرور للحساب: $safeName ($cleanPhone - $safeType) عبر القناة: $safeChannel",
                timestamp = now
            )
            db.collection("activity_logs").document(logId).set(log)

            triggerNotification("🔑 طلب استعادة كلمة مرور جديد من: $safeName ($cleanPhone)")
            onResult(true)
        }.addOnFailureListener {
            onResult(false)
        }
    }

    fun adminResolvePasswordReset(
        @Suppress("UNUSED_PARAMETER") context: Context,
        phone: String,
        newPassword: String,
        onResult: (Boolean) -> Unit
    ) {
        val cleanPhone = com.serviseyem.domain.usecases.ValidatePhoneUseCase.normalizePhone(phone).filter { it.isDigit() }
        val trimmedNewPass = newPassword.trim()
        if (cleanPhone.isBlank() || cleanPhone.length < 7 || trimmedNewPass.isBlank()) {
            onResult(false)
            return
        }
        recoveryScope.launch {
            try {
                val success = kotlinx.coroutines.withContext(Dispatchers.IO) {
                    val now = System.currentTimeMillis()
                    val hashedPassword = com.serviseyem.utils.PasswordHasher.hash(trimmedNewPass)
                    // استخدام encryptCrossDevice ليتمكن جهاز المستخدم من فك تشفير كلمة المرور المؤقتة بأمان
                    val encryptedDisplayPassword = com.serviseyem.utils.SecurityCryptoUtils.encryptCrossDevice(trimmedNewPass)
                    val updates = mapOf(
                        "id" to cleanPhone,
                        "phone" to cleanPhone,
                        "status" to "RESOLVED",
                        "newPassword" to encryptedDisplayPassword,
                        "passwordHash" to hashedPassword,
                        "resolvedAt" to now,
                        "timestamp" to now
                    )
                    com.google.android.gms.tasks.Tasks.await(
                        db.collection("password_recovery_requests").document(cleanPhone)
                            .set(updates, com.google.firebase.firestore.SetOptions.merge())
                    )
                    runCatching {
                        com.google.android.gms.tasks.Tasks.await(
                            db.collection("password_resets").document(cleanPhone).set(
                                mapOf(
                                    "id" to cleanPhone,
                                    "phone" to cleanPhone,
                                    "status" to "RESOLVED",
                                    "newPassword" to encryptedDisplayPassword,
                                    "tempPassword" to hashedPassword,
                                    "passwordHash" to hashedPassword,
                                    "resolvedAt" to now,
                                    "timestamp" to now
                                ),
                                com.google.firebase.firestore.SetOptions.merge()
                            )
                        )
                    }

                    val passUpdate = mapOf(
                        "password" to "",
                        "passwordHash" to hashedPassword
                    )
                    val phoneQueries = listOf(
                        cleanPhone,
                        "0$cleanPhone",
                        "+967$cleanPhone",
                        "967$cleanPhone",
                        "00967$cleanPhone"
                    ).distinct()
                    val collections = listOf("providers", "stores", "properties", "jobs", "users", "registered_users", "join_requests", "pending_providers")

                    for (col in collections) {
                        for (ph in phoneQueries) {
                            val snaps = runCatching {
                                com.google.android.gms.tasks.Tasks.await(
                                    db.collection(col).whereEqualTo("phone", ph).get()
                                )
                            }.getOrNull() ?: continue
                            for (doc in snaps.documents) {
                                runCatching {
                                    com.google.android.gms.tasks.Tasks.await(doc.reference.update(passUpdate))
                                }
                            }
                        }
                        if (col in listOf("users", "registered_users")) {
                            for (docId in listOf(cleanPhone, "usr_$cleanPhone", "user_$cleanPhone")) {
                                runCatching {
                                    val docRef = db.collection(col).document(docId)
                                    val snap = com.google.android.gms.tasks.Tasks.await(docRef.get())
                                    if (snap.exists()) {
                                        com.google.android.gms.tasks.Tasks.await(docRef.update(passUpdate))
                                    }
                                }
                            }
                        }
                    }

                    // Log sensitive admin operation in activity_logs
                    runCatching {
                        val logId = db.collection("activity_logs").document().id
                        val log = com.serviseyem.data.ActivityLogEntity(
                            id = logId,
                            action = "🔑 إعادة تعيين كلمة المرور برقم الهاتف: $cleanPhone بواسطة الإدارة",
                            timestamp = now
                        )
                        db.collection("activity_logs").document(logId).set(log)
                    }

                    runCatching {
                        val notifId = "PWD_RESOLVE_$cleanPhone"
                        val notif = NotificationEntity(
                            id = notifId,
                            title = "🔑 تم إعادة تعيين كلمة المرور",
                            message = "تم إعادة تعيين كلمة مرور حسابك بنجاح من قبل الإدارة.",
                            customerPhone = cleanPhone,
                            targetType = "USER",
                            targetValue = cleanPhone,
                            notificationType = "SYSTEM",
                            timestamp = now,
                            dedupKey = "PWD_RESOLVE_$cleanPhone"
                        )
                        if (notif.isValid()) db.collection("notifications").document(notifId).set(notif)
                    }
                    true
                }
                onResult(success)
            } catch (e: Exception) {
                onResult(false)
            }
        }
    }
}
