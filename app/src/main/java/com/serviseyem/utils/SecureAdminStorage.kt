package com.serviseyem.utils

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey

/**
 * 🔐 SecureAdminStorage - تخزين آمن لبيانات المالك والأدمن
 * 
 * يستخدم:
 * - Android Keystore (Hardware-backed)
 * - AES256-GCM Encryption
 * - EncryptedSharedPreferences
 * 
 * ⚠️ البيانات لا تُخزَّن في الكود المصدري
 * ⚠️ يتم تحميلها من Cloud Functions عند أول تشغيل
 */
object SecureAdminStorage {
    
    private const val PREFS_NAME = "secure_admin_vault_v2"
    private const val KEY_OWNER_EMAIL = "owner_email_hash"
    private const val KEY_OWNER_HASH = "owner_pass_hash"
    private const val KEY_ADMIN_EMAIL = "admin_email_hash"
    private const val KEY_ADMIN_HASH = "admin_pass_hash"
    private const val KEY_VAULT_INITIALIZED = "vault_initialized"
    private const val KEY_VAULT_LAST_SYNC = "vault_last_sync"
    
    private const val SALT_PREFIX = "yemen_admin_v2_"

    @Volatile
    private var cachedEncryptedPrefs: SharedPreferences? = null

    private fun getSecurePrefs(context: Context): SharedPreferences? {
        cachedEncryptedPrefs?.let { return it }
        return synchronized(this) {
            cachedEncryptedPrefs?.let { return@synchronized it }
            val appContext = context.applicationContext ?: context
            try {
                val masterKey = MasterKey.Builder(appContext)
                    .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
                    .build()

                val encryptedPrefs = EncryptedSharedPreferences.create(
                    appContext,
                    PREFS_NAME,
                    masterKey,
                    EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                    EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
                )
                migrateLegacyPrefs(appContext, encryptedPrefs)
                cachedEncryptedPrefs = encryptedPrefs
                encryptedPrefs
            } catch (e: Throwable) {
                try {
                    appContext.deleteSharedPreferences(PREFS_NAME)
                    val masterKey = MasterKey.Builder(appContext)
                        .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
                        .build()
                    val recreated = EncryptedSharedPreferences.create(
                        appContext,
                        PREFS_NAME,
                        masterKey,
                        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
                    )
                    cachedEncryptedPrefs = recreated
                    recreated
                } catch (ex: Throwable) {
                    android.util.Log.e("SecureAdminStorage", "Failed to init secure storage, falling back to private prefs", ex)
                    val fallback = appContext.getSharedPreferences("${PREFS_NAME}_fallback", Context.MODE_PRIVATE)
                    cachedEncryptedPrefs = fallback
                    fallback
                }
            }
        }
    }

    /**
     * 🔄 ترحيل بيانات الاعتماد من SharedPreferences العادي إلى المشفر ثم حذف النسخة القديمة
     */
    private fun migrateLegacyPrefs(context: Context, encryptedPrefs: SharedPreferences) {
        val legacySources = listOf("admin_security_prefs", "secure_admin_vault", "admin_vault_legacy")
        for (src in legacySources) {
            try {
                val oldSp = context.getSharedPreferences(src, Context.MODE_PRIVATE)
                val entries = oldSp.all
                if (entries.isNotEmpty()) {
                    val editor = encryptedPrefs.edit()
                    for ((k, v) in entries) {
                        if (!encryptedPrefs.contains(k)) {
                            when (v) {
                                is String -> editor.putString(k, v)
                                is Boolean -> editor.putBoolean(k, v)
                                is Long -> editor.putLong(k, v)
                                is Int -> editor.putInt(k, v)
                                is Float -> editor.putFloat(k, v)
                            }
                        }
                    }
                    editor.apply()
                    oldSp.edit().clear().apply()
                }
            } catch (_: Exception) {}
        }
    }
    
    /**
     * حفظ credentials بشكل آمن (تشفير Hash بـ PBKDF2)
     * لا نحفظ password نفسه في أي مكان إطلاقاً - فقط Hash
     */
    fun storeCredentials(
        context: Context,
        ownerEmail: String? = null,
        ownerPassword: String? = null,
        adminEmail: String? = null,
        adminPassword: String? = null
    ): Boolean {
        val prefs = getSecurePrefs(context) ?: return false
        
        return try {
            val editor = prefs.edit()
            
            ownerEmail?.trim()?.takeIf { it.isNotEmpty() }?.let { clean ->
                editor.putString(KEY_OWNER_EMAIL, hashValue(clean.lowercase(java.util.Locale.ROOT), SALT_PREFIX + "email"))
            }
            ownerPassword?.trim()?.takeIf { it.isNotEmpty() }?.let { clean ->
                val hash = if (SecureHasher.isValidHash(clean)) clean else SecureHasher.hashPassword(clean)
                editor.putString(KEY_OWNER_HASH, hash)
            }
            adminEmail?.trim()?.takeIf { it.isNotEmpty() }?.let { clean ->
                editor.putString(KEY_ADMIN_EMAIL, hashValue(clean.lowercase(java.util.Locale.ROOT), SALT_PREFIX + "email"))
            }
            adminPassword?.trim()?.takeIf { it.isNotEmpty() }?.let { clean ->
                val hash = if (SecureHasher.isValidHash(clean)) clean else SecureHasher.hashPassword(clean)
                editor.putString(KEY_ADMIN_HASH, hash)
            }
            
            editor.putBoolean(KEY_VAULT_INITIALIZED, true)
            editor.putLong(KEY_VAULT_LAST_SYNC, System.currentTimeMillis())
            editor.apply()
            true
        } catch (e: Exception) {
            android.util.Log.e("SecureAdminStorage", "Failed to store credentials", e)
            false
        }
    }
    
    /**
     * التحقق من Credentials المخزنة محلياً باستخدام PBKDF2
     */
    fun verifyFallbackCredentials(
        context: Context,
        email: String,
        password: String,
        role: String
    ): Boolean {
        val cleanEmail = email.trim()
        val cleanPass = password.trim()
        if (cleanEmail.isEmpty() || cleanPass.isEmpty() || SecureHasher.isHashFormat(cleanPass)) return false

        val prefs = getSecurePrefs(context) ?: return false
        
        if (!prefs.getBoolean(KEY_VAULT_INITIALIZED, false)) {
            return false
        }
        
        return try {
            val emailLowerHash = hashValue(cleanEmail.lowercase(java.util.Locale.ROOT), SALT_PREFIX + "email")
            val emailExactHash = hashValue(cleanEmail, SALT_PREFIX + "email")
            val passHash = hashValue(cleanPass, SALT_PREFIX + "pass")
            
            when (role.trim().uppercase(java.util.Locale.ROOT)) {
                "OWNER" -> {
                    val storedEmail = prefs.getString(KEY_OWNER_EMAIL, null)?.takeIf { it.isNotBlank() } ?: return false
                    val storedPass = prefs.getString(KEY_OWNER_HASH, null)?.takeIf { it.isNotBlank() } ?: return false
                    val emailMatches = constantTimeEquals(storedEmail, emailLowerHash) ||
                            constantTimeEquals(storedEmail, emailExactHash) ||
                            storedEmail.equals(cleanEmail, ignoreCase = true)
                    val passMatches = (!constantTimeEquals(storedPass, cleanPass)) && (
                            constantTimeEquals(storedPass, passHash) ||
                            SecureHasher.verifyPassword(cleanPass, storedPass) ||
                            SecurityCryptoUtils.verifyAdminPassword(cleanPass, storedPass)
                    )
                    emailMatches && passMatches
                }
                "ADMIN" -> {
                    val storedEmail = prefs.getString(KEY_ADMIN_EMAIL, null)?.takeIf { it.isNotBlank() } ?: return false
                    val storedPass = prefs.getString(KEY_ADMIN_HASH, null)?.takeIf { it.isNotBlank() } ?: return false
                    val emailMatches = constantTimeEquals(storedEmail, emailLowerHash) ||
                            constantTimeEquals(storedEmail, emailExactHash) ||
                            storedEmail.equals(cleanEmail, ignoreCase = true)
                    val passMatches = (!constantTimeEquals(storedPass, cleanPass)) && (
                            constantTimeEquals(storedPass, passHash) ||
                            SecureHasher.verifyPassword(cleanPass, storedPass) ||
                            SecurityCryptoUtils.verifyAdminPassword(cleanPass, storedPass)
                    )
                    emailMatches && passMatches
                }
                else -> false
            }
        } catch (e: Exception) {
            android.util.Log.e("SecureAdminStorage", "Failed to verify credentials", e)
            false
        }
    }

    /**
     * التحقق من كلمة مرور الأدمن أو المالك المخزنة محلياً في الخزنة المشفرة (لعمليات التأكيد الحساسة بعد تسجيل الدخول)
     */
    fun verifyStoredPasswordOnly(context: Context, password: String): Boolean {
        val cleanPass = password.trim()
        if (cleanPass.isEmpty() || SecureHasher.isHashFormat(cleanPass)) return false
        val prefs = getSecurePrefs(context) ?: return false
        if (!prefs.getBoolean(KEY_VAULT_INITIALIZED, false)) return false
        return try {
            val passHash = hashValue(cleanPass, SALT_PREFIX + "pass")
            val storedOwnerPass = prefs.getString(KEY_OWNER_HASH, null)?.takeIf { it.isNotBlank() }
            if (storedOwnerPass != null && !constantTimeEquals(storedOwnerPass, cleanPass)) {
                val ownerMatches = constantTimeEquals(storedOwnerPass, passHash) ||
                        SecureHasher.verifyPassword(cleanPass, storedOwnerPass) ||
                        SecurityCryptoUtils.verifyAdminPassword(cleanPass, storedOwnerPass)
                if (ownerMatches) return true
            }
            val storedAdminPass = prefs.getString(KEY_ADMIN_HASH, null)?.takeIf { it.isNotBlank() }
            if (storedAdminPass != null && !constantTimeEquals(storedAdminPass, cleanPass)) {
                val adminMatches = constantTimeEquals(storedAdminPass, passHash) ||
                        SecureHasher.verifyPassword(cleanPass, storedAdminPass) ||
                        SecurityCryptoUtils.verifyAdminPassword(cleanPass, storedAdminPass)
                if (adminMatches) return true
            }
            false
        } catch (e: Exception) {
            false
        }
    }

    /**
     * 🛡️ التحقق من كلمة المرور عبر البصمة (PBKDF2) وترقية الحسابات القديمة تلقائياً في Firestore
     * عند أول تسجيل دخول ناجح لأي حساب قديم مخزن كنص، يتم تحويله إلى بصمة مشفرة وتحديث Firestore فوراً
     */
    suspend fun verifyAndMigrate(
        docRef: com.google.firebase.firestore.DocumentReference?,
        inputPassword: String,
        storedPassOrHash: String,
        fieldName: String = "passcode"
    ): Boolean {
        if (inputPassword.isBlank() || storedPassOrHash.isBlank()) return false
        val cleanInput = inputPassword.trim()
        val cleanStored = storedPassOrHash.trim()
        if (SecureHasher.isHashFormat(cleanInput)) return false

        val isAlreadyHashed = SecureHasher.isHashFormat(cleanStored) || fieldName.endsWith("Hash", ignoreCase = true)
        val isLegacyPlainMatch = !isAlreadyHashed && constantTimeEquals(cleanInput, cleanStored)
        val isValid = isLegacyPlainMatch ||
                SecureHasher.verifyPassword(cleanInput, cleanStored) ||
                SecurityCryptoUtils.verifyAdminPassword(cleanInput, cleanStored)

        if (isValid && docRef != null) {
            // إذا كانت كلمة المرور القديمة نصاً عادياً غير مشفر بـ PBKDF2/BCrypt
            if (!isAlreadyHashed) {
                val newHash = SecureHasher.hashPassword(cleanInput)
                val updates = mutableMapOf<String, Any>(fieldName to newHash)
                if (fieldName == "password" || fieldName == "passwordHash") {
                    updates["passwordHash"] = newHash
                    updates["password"] = ""
                } else if (fieldName == "passcode" || fieldName == "passcodeHash") {
                    updates["passcodeHash"] = newHash
                    updates["passcode"] = ""
                }
                var attempts = 0
                var success = false
                while (attempts < 3 && !success) {
                    try {
                        kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
                            com.google.android.gms.tasks.Tasks.await(docRef.update(updates))
                        }
                        success = true
                    } catch (e: Exception) {
                        attempts++
                        if (attempts >= 3) {
                            android.util.Log.e("SecureAdminStorage", "Automatic hash migration in Firestore failed after $attempts attempts", e)
                        } else {
                            kotlinx.coroutines.delay(300L * attempts)
                        }
                    }
                }
            }
        }
        return isValid
    }
    
    /**
     * Hash آمن باستخدام SHA-256 + Salt
     */
    private fun hashValue(value: String, salt: String): String {
        return try {
            val digest = java.security.MessageDigest.getInstance("SHA-256")
            val bytes = digest.digest((salt + value).toByteArray(Charsets.UTF_8))
            bytes.joinToString("") { "%02x".format(it) }
        } catch (e: Exception) {
            ""
        }
    }
    
    /**
     * Constant-Time Comparison لتجنب Timing Attacks
     */
    private fun constantTimeEquals(a: String, b: String): Boolean {
        val aBytes = a.toByteArray(Charsets.UTF_8)
        val bBytes = b.toByteArray(Charsets.UTF_8)
        var diff = aBytes.size xor bBytes.size
        val maxLen = maxOf(aBytes.size, bBytes.size)
        for (i in 0 until maxLen) {
            val byteA = if (i < aBytes.size) aBytes[i].toInt() else 0
            val byteB = if (i < bBytes.size) bBytes[i].toInt() else 0
            diff = diff or (byteA xor byteB)
        }
        return diff == 0
    }
    
    /**
     * حذف البيانات المحفوظة (للاستخدام عند التحديثات)
     */
    fun clearVault(context: Context): Boolean {
        val prefs = getSecurePrefs(context) ?: return false
        return try {
            prefs.edit().clear().apply()
            true
        } catch (e: Exception) {
            false
        }
    }
    
    /**
     * التحقق من تهيئة الـ Vault
     */
    fun isVaultInitialized(context: Context): Boolean {
        val prefs = getSecurePrefs(context) ?: return false
        return prefs.getBoolean(KEY_VAULT_INITIALIZED, false)
    }
}

/**
 * 🔐 واجهة ومستودع أوراق اعتماد الأدمن والمشرفين (AdminCredentialsVault)
 */
typealias AdminCredentialsVault = SecureAdminStorage
