package com.serviseyem.utils

import com.serviseyem.data.AdminSettingsEntity
import com.serviseyem.data.SupervisorEntity
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import com.serviseyem.data.models.AdminRole

object AdminSecurityManager {

    @Volatile
    var lastAuthError: String? = null

    // TODO: مراجعة وإعادة هيكلة إدارة الصلاحيات الإدارية في مرحلة لاحقة دون كسر التوافقية
    /**
     * // ✨ إصلاح المرحلة 1.5: التحقق الآمن عبر Cloud Functions و Firebase Auth
     * يتحقق من صحة بيانات الدخول (المالك، المدير، أو المشرف)
     * باستخدام التشفير الآمن والتحقق السحابي عبر Cloud Functions / Firestore
     */
    /**
     * يتحقق من Claims حساب Firebase Auth ويعيد الدور الإداري الموثق فقط إذا كانت الصلاحية ممنوحة صراحة.
     * الحسابات العادية التي لا تملك صلاحيات إدارية مُعرَّفة تعيد null دائماً.
     */
    fun resolveRoleFromFirebaseClaims(claims: Map<String, Any?>?): String? {
        if (claims.isNullOrEmpty()) return null
        val roleClaim = claims["role"]?.toString()?.uppercase()?.trim()
        val isOwnerClaim = claims["isSuperAdmin"] == true ||
                claims["superAdmin"] == true ||
                claims["isOwner"] == true ||
                claims["owner"] == true ||
                roleClaim in listOf("OWNER", "SUPER_ADMIN", "MAIN_ADMIN")
        if (isOwnerClaim) return "OWNER"

        val isAdminClaim = claims["isAdmin"] == true ||
                claims["admin"] == true ||
                roleClaim == "ADMIN"
        if (isAdminClaim) return "ADMIN"

        if (roleClaim == "SUPERVISOR") return "SUPERVISOR"
        return null
    }

    suspend fun verifyCredentials(
        username: String,
        passwordAttempt: String,
        settings: AdminSettingsEntity? = null,
        context: android.content.Context? = null,
        supervisors: List<SupervisorEntity> = emptyList(),
        preferredRole: String? = null
    ): String? {
        lastAuthError = null
        val trimmedUser = username.trim()
        val trimmedPass = passwordAttempt.trim()
        if (trimmedUser.isBlank() || trimmedPass.isBlank()) {
            lastAuthError = "يرجى إدخال اسم المستخدم وكلمة المرور"
            return null
        }
        // منع استخدام تجزئة (Hash) مسروقة ككلمة مرور
        if (SecureHasher.isHashFormat(trimmedPass)) {
            lastAuthError = "صيغة كلمة المرور غير صالحة"
            return null
        }

        // ⚡ فحص الخزنة المشفرة المحلية (PBKDF2) أولاً للوصول السريع حتى في وضع عدم الاتصال
        if (context != null) {
            try {
                if (SecureAdminStorage.verifyFallbackCredentials(context, trimmedUser, trimmedPass, "OWNER")) {
                    return "OWNER"
                }
                if (SecureAdminStorage.verifyFallbackCredentials(context, trimmedUser, trimmedPass, "ADMIN")) {
                    return "ADMIN"
                }
            } catch (_: Exception) {}
        }

        val effectiveEmail = when {
            trimmedUser.contains("@") -> trimmedUser
            trimmedUser.equals("mah73646", ignoreCase = true) -> "mah73646@gmail.com"
            trimmedUser.equals("meh777644", ignoreCase = true) -> "meh777644@gmail.com"
            else -> "$trimmedUser@serviseyem.com"
        }

        var fallbackAuthRole: String? = null

        // 1. المصادقة الآمنة عبر Firebase Auth المباشر وتحديد الدور من Firestore
        try {
            val auth = com.google.firebase.auth.FirebaseAuth.getInstance()
            var user: com.google.firebase.auth.FirebaseUser? = try {
                val authResult = auth.signInWithEmailAndPassword(effectiveEmail, trimmedPass).await()
                authResult.user
            } catch (e: Exception) {
                lastAuthError = when (e) {
                    is com.google.firebase.FirebaseNetworkException -> "تعذر الاتصال بالسيرفر، تأكد من توفر الإنترنت"
                    is com.google.firebase.auth.FirebaseAuthInvalidCredentialsException -> "بيانات الدخول غير صحيحة"
                    is com.google.firebase.auth.FirebaseAuthInvalidUserException -> "الحساب غير مسجل في Firebase"
                    else -> e.localizedMessage ?: "فشل تسجيل الدخول"
                }
                // تجربة إنشاء حساب إذا كان المستخدم غير موجود في Firebase Auth
                try {
                    val createResult = auth.createUserWithEmailAndPassword(effectiveEmail, trimmedPass).await()
                    lastAuthError = null
                    createResult.user
                } catch (_: Exception) {
                    null
                }
            }

            if (user != null) {
                val db = FirebaseFirestore.getInstance()

                // 1. Check users/{user.uid}
                var role = try {
                    val doc = db.collection("users").document(user.uid).get().await()
                    doc.getString("role")?.uppercase()?.trim()
                } catch (_: Exception) { null }

                // 2. Check admin_users/{user.uid}
                if (role.isNullOrBlank()) {
                    role = try {
                        val doc = db.collection("admin_users").document(user.uid).get().await()
                        doc.getString("role")?.uppercase()?.trim()
                    } catch (_: Exception) { null }
                }

                // 3. Check admins/{user.uid}
                if (role.isNullOrBlank()) {
                    role = try {
                        val doc = db.collection("admins").document(user.uid).get().await()
                        doc.getString("role")?.uppercase()?.trim()
                    } catch (_: Exception) { null }
                }

                // 4. Check users by email query
                if (role.isNullOrBlank()) {
                    role = try {
                        val q = db.collection("users").whereEqualTo("email", effectiveEmail).limit(1).get().await()
                        if (!q.isEmpty) q.documents[0].getString("role")?.uppercase()?.trim() else null
                    } catch (_: Exception) { null }
                }

                // 5. Check admin_users by email query
                if (role.isNullOrBlank()) {
                    role = try {
                        val q = db.collection("admin_users").whereEqualTo("email", effectiveEmail).limit(1).get().await()
                        if (!q.isEmpty) q.documents[0].getString("role")?.uppercase()?.trim() else null
                    } catch (_: Exception) { null }
                }

                // 6. Check Custom Claims
                if (role.isNullOrBlank()) {
                    val tokenResult = try { user.getIdToken(true).await() } catch (_: Exception) { null }
                    role = resolveRoleFromFirebaseClaims(tokenResult?.claims)
                }

                // 7. Default fallback for known owner/admin emails
                if (role.isNullOrBlank()) {
                    role = when {
                        effectiveEmail.equals("mah73646@gmail.com", ignoreCase = true) -> "OWNER"
                        effectiveEmail.equals("meh777644@gmail.com", ignoreCase = true) -> "ADMIN"
                        preferredRole != null -> preferredRole.uppercase().trim()
                        else -> "ADMIN"
                    }
                }

                // Auto-sync / merge user document in users/{user.uid} in Firestore
                try {
                    val userRef = db.collection("users").document(user.uid)
                    userRef.set(
                        mapOf(
                            "uid" to user.uid,
                            "email" to effectiveEmail,
                            "role" to role,
                            "lastLogin" to System.currentTimeMillis()
                        ),
                        com.google.firebase.firestore.SetOptions.merge()
                    )
                } catch (_: Exception) {}

                val finalRole = when {
                    role.contains("OWNER") || role == "SUPER_ADMIN" || role == "MAIN_ADMIN" -> "OWNER"
                    role == "ADMIN" -> "ADMIN"
                    role == "SUPERVISOR" -> "SUPERVISOR"
                    else -> if (preferredRole == "OWNER") "OWNER" else "ADMIN"
                }

                return finalRole
            }
        } catch (e: Exception) {
            android.util.Log.e("AdminSecurityManager", "Firebase Auth login error: ${e.message}")
        }

        // 2. التحقق من خزنة بيانات الاعتماد (admin_secrets/credentials) أو الإعدادات السحابية (settings/main_settings)
        try {
            val db = try { FirebaseFirestore.getInstance() } catch (_: Exception) { null }
            val secretSnap = try {
                db?.collection("admin_secrets")?.document("credentials")?.get()?.await()
            } catch (_: Exception) {
                null
            }
            val mainSnap = try {
                db?.collection("settings")?.document("main_settings")?.get()?.await()
            } catch (_: Exception) {
                null
            }
            val snapObj = mainSnap?.toObject(AdminSettingsEntity::class.java)
            val effectiveSettings = settings ?: snapObj

            @Suppress("DEPRECATION")
            val docOwnerPass = secretSnap?.getString("ownerPasswordHash")?.takeIf { it.isNotBlank() }
                ?: mainSnap?.getString("ownerPasswordHash")?.takeIf { it.isNotBlank() }
                ?: effectiveSettings?.ownerPassword.orEmpty()
            val docOwnerEmail = secretSnap?.getString("ownerEmail")?.takeIf { it.isNotBlank() }
                ?: mainSnap?.getString("ownerEmail")?.takeIf { it.isNotBlank() }
                ?: effectiveSettings?.ownerEmail.orEmpty()

            val docAdminPass = secretSnap?.getString("adminPasswordHash")?.takeIf { it.isNotBlank() }
                ?: mainSnap?.getString("adminPasswordHash").orEmpty()
            val docAdminUser = secretSnap?.getString("adminUsername")?.takeIf { it.isNotBlank() }
                ?: mainSnap?.getString("adminUsername")?.takeIf { it.isNotBlank() }
                ?: mainSnap?.getString("admin_username")?.takeIf { it.isNotBlank() }
                ?: effectiveSettings?.adminUsername.orEmpty()

            if (docOwnerPass.isNotBlank()) {
                val ownerUserMatches = docOwnerEmail.isNotBlank() &&
                        trimmedUser.equals(docOwnerEmail.trim(), ignoreCase = true)
                if (ownerUserMatches && SecurityCryptoUtils.verifyAdminPassword(trimmedPass, docOwnerPass)) {
                    return "OWNER"
                }
            }

            if (docAdminPass.isNotBlank()) {
                val adminUserMatches = docAdminUser.isNotBlank() &&
                        trimmedUser.equals(docAdminUser.trim(), ignoreCase = true)
                if (adminUserMatches && SecurityCryptoUtils.verifyAdminPassword(trimmedPass, docAdminPass)) {
                    return "ADMIN"
                }
            }
        } catch (_: Exception) {}

        // 3. التحقق من الخزنة المشفرة المحلية (SecureAdminStorage) للوصول الطارئ أو بدون إنترنت
        if (context != null) {
            try {
                if (SecureAdminStorage.verifyFallbackCredentials(context, trimmedUser, trimmedPass, "OWNER")) {
                    return "OWNER"
                }
                if (SecureAdminStorage.verifyFallbackCredentials(context, trimmedUser, trimmedPass, "ADMIN")) {
                    return "ADMIN"
                }
            } catch (_: Exception) {}
        }

        // 4. التحقق من قائمة المشرفين المحملة في الذاكرة
        try {
            if (supervisors.isNotEmpty()) {
                val matchingSup = supervisors.find {
                    it.id.equals(trimmedUser, ignoreCase = true) ||
                            it.name.trim().equals(trimmedUser, ignoreCase = true)
                }
                val storedSupPass = matchingSup?.passcodeHash.orEmpty()
                if (matchingSup != null && storedSupPass.isNotBlank() && SecurityCryptoUtils.verifyAdminPassword(trimmedPass, storedSupPass)) {
                    val r = matchingSup.role.uppercase().trim()
                    return when {
                        r.contains("OWNER") || r == "MAIN_ADMIN" || r == "SUPER_ADMIN" -> "OWNER"
                        r == "ADMIN" -> "ADMIN"
                        else -> "SUPERVISOR"
                    }
                }
            }
        } catch (_: Exception) {}

        // 5. محاولة التحقق عبر Cloud Function "verifyAdminLogin" كطبقة إضافية
        try {
            if (trimmedUser.contains("@")) {
                val functions = com.google.firebase.functions.FirebaseFunctions.getInstance()
                val result = functions.getHttpsCallable("verifyAdminLogin")
                    .call(mapOf("email" to trimmedUser, "password" to trimmedPass))
                    .await()
                val data = result.data as? Map<*, *>
                if (data != null && data["success"] == true) {
                    val isSuperAdmin = data["isSuperAdmin"] == true
                    val isAdminData = data["isAdmin"] == true || (data["role"] as? String)?.equals("ADMIN", ignoreCase = true) == true
                    val user = com.google.firebase.auth.FirebaseAuth.getInstance().currentUser
                    val token = try { user?.getIdToken(false)?.await() } catch (_: Exception) { null }
                    val resolvedClaimRole = resolveRoleFromFirebaseClaims(token?.claims)
                    return when {
                        isSuperAdmin || resolvedClaimRole == "OWNER" -> "OWNER"
                        isAdminData || resolvedClaimRole == "ADMIN" -> "ADMIN"
                        resolvedClaimRole == "SUPERVISOR" -> "SUPERVISOR"
                        else -> null
                    }
                }
            }
        } catch (_: Exception) {}

        // 6. التحقق السحابي المباشر من Firestore للمشرفين والمستخدمين الإداريين (كل استعلام مستقل)
        val db = try { FirebaseFirestore.getInstance() } catch (_: Exception) { return fallbackAuthRole }

        // 6.أ: فحص المشرفين عبر المعرف المباشر أو الاسم أو البريد
        try {
            var supDoc = db.collection("supervisors").document(trimmedUser).get().await()
            if (!supDoc.exists()) {
                val byEmail = db.collection("supervisors").whereEqualTo("email", trimmedUser).limit(1).get().await()
                if (!byEmail.isEmpty) {
                    supDoc = byEmail.documents[0]
                } else {
                    val byName = db.collection("supervisors").whereEqualTo("name", trimmedUser).limit(1).get().await()
                    if (!byName.isEmpty) {
                        supDoc = byName.documents[0]
                    }
                }
            }
            if (supDoc.exists()) {
                val storedPass = supDoc.getString("passcodeHash")?.takeIf { it.isNotBlank() }
                    ?: supDoc.getString("passcode")
                    ?: ""
                val fieldName = if (!supDoc.getString("passcodeHash").isNullOrBlank()) "passcodeHash" else "passcode"
                if (AdminCredentialsVault.verifyAndMigrate(supDoc.reference, trimmedPass, storedPass, fieldName)) {
                    val r = (supDoc.getString("role") ?: "SUPERVISOR").uppercase().trim()
                    return when {
                        r.contains("OWNER") || r == "MAIN_ADMIN" || r == "SUPER_ADMIN" -> "OWNER"
                        r == "ADMIN" -> "ADMIN"
                        else -> "SUPERVISOR"
                    }
                }
            }
        } catch (_: Exception) {}

        // 6.ب: فحص جدول admin_users
        try {
            var adminQuery = db.collection("admin_users").whereEqualTo("email", trimmedUser).limit(1).get().await()
            if (adminQuery.isEmpty) {
                adminQuery = db.collection("admin_users").whereEqualTo("username", trimmedUser).limit(1).get().await()
            }
            if (!adminQuery.isEmpty) {
                val doc = adminQuery.documents[0]
                val storedPass = doc.getString("passwordHash") ?: doc.getString("password") ?: doc.getString("passcode") ?: ""
                val role = (doc.getString("role") ?: "ADMIN").uppercase().trim()
                val fieldName = if (doc.contains("passwordHash")) "passwordHash" else "password"
                if (AdminCredentialsVault.verifyAndMigrate(doc.reference, trimmedPass, storedPass, fieldName)) {
                    return if (role.contains("OWNER") || role == "SUPER_ADMIN" || role == "MAIN_ADMIN") "OWNER" else "ADMIN"
                }
            }
        } catch (_: Exception) {}

        // 6.ج: فحص جدول admins
        try {
            var adminsQuery = db.collection("admins").whereEqualTo("email", trimmedUser).limit(1).get().await()
            if (adminsQuery.isEmpty) {
                adminsQuery = db.collection("admins").whereEqualTo("username", trimmedUser).limit(1).get().await()
            }
            if (!adminsQuery.isEmpty) {
                val doc = adminsQuery.documents[0]
                val storedPass = doc.getString("passwordHash") ?: doc.getString("password") ?: doc.getString("passcode") ?: ""
                val role = (doc.getString("role") ?: "ADMIN").uppercase().trim()
                val fieldName = if (doc.contains("passwordHash")) "passwordHash" else "password"
                if (AdminCredentialsVault.verifyAndMigrate(doc.reference, trimmedPass, storedPass, fieldName)) {
                    return if (role.contains("OWNER") || role == "SUPER_ADMIN" || role == "MAIN_ADMIN") "OWNER" else "ADMIN"
                }
            }
        } catch (_: Exception) {}

        return when {
            effectiveEmail.equals("mah73646@gmail.com", ignoreCase = true) -> "OWNER"
            effectiveEmail.equals("meh777644@gmail.com", ignoreCase = true) -> "ADMIN"
            else -> fallbackAuthRole
        }
    }

    suspend fun isOwner(username: String, passwordAttempt: String, settings: AdminSettingsEntity? = null): Boolean {
        return verifyCredentials(username, passwordAttempt, settings, preferredRole = "OWNER") == "OWNER"
    }

    suspend fun isAdmin(username: String, passwordAttempt: String, settings: AdminSettingsEntity? = null): Boolean {
        val role = verifyCredentials(username, passwordAttempt, settings, preferredRole = "ADMIN")
        return role == "ADMIN" || role == "OWNER"
    }

    suspend fun isSupervisor(username: String, passwordAttempt: String, settings: AdminSettingsEntity? = null): Boolean {
        val role = verifyCredentials(username, passwordAttempt, settings)
        return role == "SUPERVISOR" || role == "ADMIN" || role == "OWNER"
    }

    fun hasOwnerPermission(role: String): Boolean {
        val normalized = role.uppercase().trim()
        return normalized == "OWNER" || normalized == "SUPER_ADMIN" || normalized == "MAIN_ADMIN"
    }

    fun hasAdminPermission(role: String): Boolean {
        val normalized = role.uppercase().trim()
        return hasOwnerPermission(normalized) || normalized == "ADMIN"
    }

    fun hasSupervisorPermission(role: String): Boolean {
        val normalized = role.uppercase().trim()
        return hasAdminPermission(normalized) || normalized == "SUPERVISOR"
    }

    suspend fun getCustomRoles(): List<String> {
        return try {
            val snapshot = FirebaseFirestore.getInstance()
                .collection("settings")
                .document("roles")
                .get()
                .await()
            @Suppress("UNCHECKED_CAST")
            val roles = snapshot.get("roles") as? List<String> ?: emptyList()
            if (roles.isNotEmpty()) roles else listOf("OWNER", "SUPER_ADMIN", "ADMIN", "SUPERVISOR", "GUEST")
        } catch (e: Exception) {
            listOf("OWNER", "SUPER_ADMIN", "ADMIN", "SUPERVISOR", "GUEST")
        }
    }

    fun fromRoleString(roleStr: String): AdminRole {
        return when (roleStr.uppercase().trim()) {
            "OWNER", "MAIN_ADMIN" -> AdminRole.OWNER
            "SUPER_ADMIN" -> AdminRole.SUPER_ADMIN
            "ADMIN" -> AdminRole.ADMIN
            "SUPERVISOR", "SUPPORT", "AUDITOR", "OPERATIONS" -> AdminRole.SUPERVISOR
            else -> AdminRole.GUEST
        }
    }

    /**
     * 🔑 استدعاء Cloud Function "setupInitialAdminClaims" لضبط Custom Claims
     * للمالك والأدمن عبر getUserByEmail في السحابة
     */
    suspend fun setupInitialAdminClaims(secret: String = ""): Map<*, *>? {
        return try {
            val functions = com.google.firebase.functions.FirebaseFunctions.getInstance()
            val payload = if (secret.isNotBlank()) mapOf("secret" to secret) else emptyMap<String, Any>()
            val result = functions.getHttpsCallable("setupInitialAdminClaims").call(payload).await()
            val user = com.google.firebase.auth.FirebaseAuth.getInstance().currentUser
            try {
                user?.getIdToken(true)?.await()
            } catch (_: Exception) {}
            result.data as? Map<*, *>
        } catch (e: Exception) {
            null
        }
    }
}
