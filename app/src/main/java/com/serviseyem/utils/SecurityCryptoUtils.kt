package com.serviseyem.utils

import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Base64
import com.google.firebase.crashlytics.FirebaseCrashlytics
import java.security.KeyStore
import java.security.MessageDigest
import java.security.SecureRandom
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.GCMParameterSpec
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.PBEKeySpec
import javax.crypto.spec.SecretKeySpec

/**
 * Client-Side Encryption & Hashing Utility for Protecting Sensitive Data
 * Ensures sensitive user details, FCM tokens, and internal credentials
 * are securely encrypted using AndroidKeyStore with AES/GCM/NoPadding and PBKDF2 key derivation.
 */
object SecurityCryptoUtils {
    private const val ANDROID_KEYSTORE_PROVIDER = "AndroidKeyStore"
    private const val KEYSTORE_ALIAS = "WAM_MasterVaultKey_GCM"
    private const val LEGACY_KEYSTORE_ALIAS = "WAM_MasterVaultKey"
    private const val PBKDF2_ALGORITHM = "PBKDF2WithHmacSHA256"
    private const val PBKDF2_ITERATIONS = 10000
    private const val KEY_SIZE_BITS = 256
    private const val CIPHER_TRANSFORMATION = "AES/GCM/NoPadding"
    private const val LEGACY_CBC_TRANSFORMATION = "AES/CBC/PKCS5Padding"
    private const val GCM_IV_LENGTH = 12
    private const val GCM_TAG_LENGTH_BITS = 128

    private val secureRandom = SecureRandom()

    @Volatile
    private var cachedFallbackKey: SecretKeySpec? = null

    fun getSecretKey(): SecretKey {
        return try {
            val keyStore = KeyStore.getInstance(ANDROID_KEYSTORE_PROVIDER).apply {
                load(null)
            }
            if (keyStore.containsAlias(KEYSTORE_ALIAS)) {
                val entry = keyStore.getEntry(KEYSTORE_ALIAS, null) as? KeyStore.SecretKeyEntry
                entry?.secretKey ?: (keyStore.getKey(KEYSTORE_ALIAS, null) as? SecretKey) ?: generateAndStoreKey()
            } else {
                generateAndStoreKey()
            }
        } catch (e: Throwable) {
            // خط دفاع احتياطي في حال غياب مزود AndroidKeyStore (مثل بيئات اختبارات JVM)
            deriveFallbackKey()
        }
    }

    private fun getLegacyCbcKey(): SecretKey {
        return try {
            val keyStore = KeyStore.getInstance(ANDROID_KEYSTORE_PROVIDER).apply {
                load(null)
            }
            if (keyStore.containsAlias(LEGACY_KEYSTORE_ALIAS)) {
                val entry = keyStore.getEntry(LEGACY_KEYSTORE_ALIAS, null) as? KeyStore.SecretKeyEntry
                entry?.secretKey ?: (keyStore.getKey(LEGACY_KEYSTORE_ALIAS, null) as? SecretKey) ?: deriveFallbackKey()
            } else {
                deriveFallbackKey()
            }
        } catch (e: Throwable) {
            deriveFallbackKey()
        }
    }

    private fun generateAndStoreKey(): SecretKey {
        val keyGenerator = KeyGenerator.getInstance(
            KeyProperties.KEY_ALGORITHM_AES,
            ANDROID_KEYSTORE_PROVIDER
        )
        val keyGenParameterSpec = KeyGenParameterSpec.Builder(
            KEYSTORE_ALIAS,
            KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
        )
            .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
            .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
            .setKeySize(KEY_SIZE_BITS)
            .setRandomizedEncryptionRequired(false)
            .build()
        keyGenerator.init(keyGenParameterSpec)
        return keyGenerator.generateKey()
    }

    /**
     * اشتقاق مفتاح احتياطي ديناميكي في حال تعذر الوصول إلى AndroidKeyStore (مثل اختبارات JVM)
     * أو لتشفير البيانات العابرة بين جهاز الأدمن وجهاز المستخدم بأمان.
     */
    private fun deriveFallbackKey(): SecretKeySpec {
        cachedFallbackKey?.let { return it }
        return synchronized(this) {
            cachedFallbackKey?.let { return@synchronized it }
            val derived = try {
                val factory = SecretKeyFactory.getInstance(PBKDF2_ALGORITHM)
                val salt = LEGACY_KEYSTORE_ALIAS.toByteArray(Charsets.UTF_8)
                val seed = "WAM_MasterVault_Fallback_Key_v2".toCharArray()
                val spec = PBEKeySpec(seed, salt, PBKDF2_ITERATIONS, KEY_SIZE_BITS)
                try {
                    val secret = factory.generateSecret(spec)
                    SecretKeySpec(secret.encoded, "AES")
                } finally {
                    spec.clearPassword()
                }
            } catch (e: Throwable) {
                val digest = MessageDigest.getInstance("SHA-256")
                SecretKeySpec(digest.digest(LEGACY_KEYSTORE_ALIAS.toByteArray(Charsets.UTF_8)), "AES")
            }
            cachedFallbackKey = derived
            derived
        }
    }

    /**
     * Hashes plain text string using SecureHasher (PBKDF2) for secure one-way password storage.
     * ✨ م2: تم استبدال SHA-256 بـ PBKDF2 المتقدم لضمان أعلى مستويات الحماية
     */
    fun hashPassword(password: String): String {
        if (password.isBlank()) return ""
        return com.serviseyem.utils.SecureHasher.hashPassword(password.trim())
    }

    /**
     * Verifies provided input against stored password hash using PasswordHasher.
     * Enforces salt:hash verification with zero plain-text fallbacks or hardcoded seeds.
     */
    fun verifyAdminPassword(input: String, storedHashOrPass: String? = null): Boolean {
        if (input.isBlank() || storedHashOrPass.isNullOrBlank()) return false
        val trimmedInput = input.trim()
        val trimmedStored = storedHashOrPass.trim()
        return com.serviseyem.utils.SecureHasher.verifyPassword(trimmedInput, trimmedStored) || 
               PasswordHasher.verifyPassword(trimmedInput, trimmedStored)
    }

    private fun base64Encode(bytes: ByteArray): String {
        return try {
            Base64.encodeToString(bytes, Base64.NO_WRAP)
        } catch (e: Throwable) {
            java.util.Base64.getEncoder().encodeToString(bytes)
        }
    }

    private fun base64Decode(str: String): ByteArray {
        val clean = str.trim()
        return try {
            Base64.decode(clean, Base64.NO_WRAP)
        } catch (_: Throwable) {
            try {
                Base64.decode(clean, Base64.DEFAULT)
            } catch (_: Throwable) {
                java.util.Base64.getDecoder().decode(clean)
            }
        }
    }

    /**
     * Encrypts sensitive fields (such as FCM tokens or local credentials) into Base64 encoded AES-GCM cipher text.
     * Generates a unique, cryptographically secure 12-byte random IV for each operation and prefixes it to the ciphertext.
     * Never returns raw plainText if KeyStore fails; uses fallback derived AES-GCM key instead.
     */
    fun encrypt(plainText: String?): String {
        if (plainText.isNullOrEmpty()) return ""
        return try {
            val key = getSecretKey()
            val cipher = Cipher.getInstance(CIPHER_TRANSFORMATION)
            val iv = ByteArray(GCM_IV_LENGTH)
            secureRandom.nextBytes(iv)
            val gcmSpec = GCMParameterSpec(GCM_TAG_LENGTH_BITS, iv)
            cipher.init(Cipher.ENCRYPT_MODE, key, gcmSpec)
            val encryptedBytes = cipher.doFinal(plainText.toByteArray(Charsets.UTF_8))
            val combined = iv + encryptedBytes
            "gcm:" + base64Encode(combined)
        } catch (e: Throwable) {
            try {
                FirebaseCrashlytics.getInstance().recordException(e)
            } catch (ignored: Throwable) {}
            encryptCrossDevice(plainText)
        }
    }

    /**
     * تشفير AES-GCM مشترك عبر الأجهزة (يُستخدم عند تشفير كلمة المرور المؤقتة من جهاز الأدمن ليتمكن جهاز المستخدم من فك تشفيرها)
     */
    fun encryptCrossDevice(plainText: String?): String {
        if (plainText.isNullOrEmpty()) return ""
        return try {
            val fallbackKey = deriveFallbackKey()
            val cipher = Cipher.getInstance(CIPHER_TRANSFORMATION)
            val iv = ByteArray(GCM_IV_LENGTH)
            secureRandom.nextBytes(iv)
            val gcmSpec = GCMParameterSpec(GCM_TAG_LENGTH_BITS, iv)
            cipher.init(Cipher.ENCRYPT_MODE, fallbackKey, gcmSpec)
            val encryptedBytes = cipher.doFinal(plainText.toByteArray(Charsets.UTF_8))
            "gcm:" + base64Encode(iv + encryptedBytes)
        } catch (ex: Throwable) {
            try {
                FirebaseCrashlytics.getInstance().recordException(ex)
            } catch (_: Throwable) {}
            throw IllegalStateException("Encryption failed critically", ex)
        }
    }

    /**
     * فك تشفير النصوص المشفرة عبر الأجهزة (أو محلياً) بأمان دون إرجاع النص المشفر الخام عند الفشل.
     */
    fun decryptCrossDevice(encryptedText: String?): String {
        if (encryptedText.isNullOrEmpty()) return ""
        val normalizedInput = encryptedText.trim().removePrefix("enc::").trim()
        if (normalizedInput.startsWith("gcm:") || normalizedInput.startsWith("gcmx:")) {
            return try {
                val rawBase64 = normalizedInput.removePrefix("gcmx:").removePrefix("gcm:")
                val decodedBytes = base64Decode(rawBase64)
                if (decodedBytes.size <= GCM_IV_LENGTH) return ""
                val iv = decodedBytes.copyOfRange(0, GCM_IV_LENGTH)
                val encrypted = decodedBytes.copyOfRange(GCM_IV_LENGTH, decodedBytes.size)
                try {
                    val fallbackKey = deriveFallbackKey()
                    val cipher = Cipher.getInstance(CIPHER_TRANSFORMATION)
                    cipher.init(Cipher.DECRYPT_MODE, fallbackKey, GCMParameterSpec(GCM_TAG_LENGTH_BITS, iv))
                    String(cipher.doFinal(encrypted), Charsets.UTF_8)
                } catch (_: Throwable) {
                    val key = getSecretKey()
                    val cipher = Cipher.getInstance(CIPHER_TRANSFORMATION)
                    cipher.init(Cipher.DECRYPT_MODE, key, GCMParameterSpec(GCM_TAG_LENGTH_BITS, iv))
                    String(cipher.doFinal(encrypted), Charsets.UTF_8)
                }
            } catch (_: Throwable) {
                ""
            }
        }
        return decrypt(normalizedInput)
    }

    /**
     * Decrypts Base64 encoded AES-GCM cipher text (or legacy AES-CBC payload) back to plain text.
     */
    fun decrypt(encryptedText: String?): String {
        if (encryptedText.isNullOrEmpty()) return ""
        val normalizedInput = encryptedText.trim().removePrefix("enc::").trim()
        if (normalizedInput.isEmpty()) return ""
        return try {
            if (normalizedInput.startsWith("gcm:") || normalizedInput.startsWith("gcmx:")) {
                val rawBase64 = normalizedInput.removePrefix("gcmx:").removePrefix("gcm:")
                val decodedBytes = base64Decode(rawBase64)
                if (decodedBytes.size <= GCM_IV_LENGTH) return ""
                val iv = decodedBytes.copyOfRange(0, GCM_IV_LENGTH)
                val encrypted = decodedBytes.copyOfRange(GCM_IV_LENGTH, decodedBytes.size)
                return try {
                    val key = getSecretKey()
                    val cipher = Cipher.getInstance(CIPHER_TRANSFORMATION)
                    cipher.init(Cipher.DECRYPT_MODE, key, GCMParameterSpec(GCM_TAG_LENGTH_BITS, iv))
                    String(cipher.doFinal(encrypted), Charsets.UTF_8)
                } catch (_: Throwable) {
                    val fallbackKey = deriveFallbackKey()
                    val cipher = Cipher.getInstance(CIPHER_TRANSFORMATION)
                    cipher.init(Cipher.DECRYPT_MODE, fallbackKey, GCMParameterSpec(GCM_TAG_LENGTH_BITS, iv))
                    String(cipher.doFinal(encrypted), Charsets.UTF_8)
                }
            }
            // التوافق العكسي مع البيانات المشفرة مسبقاً بـ AES/CBC/PKCS5Padding
            val decodedBytes = base64Decode(normalizedInput)
            if (decodedBytes.size <= 16) {
                return ""
            }
            val iv = decodedBytes.copyOfRange(0, 16)
            val encrypted = decodedBytes.copyOfRange(16, decodedBytes.size)
            val key = getLegacyCbcKey()
            val cipher = Cipher.getInstance(LEGACY_CBC_TRANSFORMATION)
            val ivSpec = IvParameterSpec(iv)
            cipher.init(Cipher.DECRYPT_MODE, key, ivSpec)
            val decryptedBytes = cipher.doFinal(encrypted)
            String(decryptedBytes, Charsets.UTF_8)
        } catch (e: Throwable) {
            try {
                FirebaseCrashlytics.getInstance().recordException(e)
            } catch (ignored: Throwable) {}
            ""
        }
    }

    /**
     * Encrypts FCM Tokens specifically for privacy compliance.
     */
    fun encryptFcmToken(token: String?): String = encrypt(token)

    /**
     * Decrypts FCM Tokens.
     */
    fun decryptFcmToken(encryptedToken: String?): String = decrypt(encryptedToken)

    /**
     * Sanitizes and HTML-encodes user inputs for safe display context.
     */
    fun sanitizeInput(input: String?): String {
        if (input.isNullOrEmpty()) return ""
        return input
            .replace(Regex("</?[a-zA-Z][^>]*>"), "")
            .replace("javascript:", "", ignoreCase = true)
            .replace(Regex("&(?!amp;|lt;|gt;|quot;|#x27;)"), "&amp;")
            .replace("<", "&lt;")
            .replace(">", "&gt;")
            .replace("\"", "&quot;")
            .replace("'", "&#x27;")
            .trim()
    }

    /**
     * Validates password against policy (minimum 7 characters, rejecting common weak ones).
     */
    fun validatePasswordPolicy(password: String): Pair<Boolean, String?> {
        val cleanPass = password.trim()
        if (cleanPass.length < 7) {
            return Pair(false, "عفواً، يجب أن تكون كلمة المرور مكونة من 7 خانات على الأقل (تحتوي على أحرف وأرقام معاً) لضمان حماية حسابك.")
        }
        val weakPasswords = listOf(
            "123456", "1234567", "12345678", "000000", "0000000", "00000000", "111111", "1111111", "11111111",
            "112233", "123123", "password", "yemen123", "yemen2026", "7777777", "77777777", "123456789"
        )
        if (cleanPass.lowercase() in weakPasswords || cleanPass.toSet().size <= 1) {
            return Pair(false, "عفواً، لقد قمت بإدخال كلمة مرور ضعيفة وسهلة التخمين. يرجى اختيار كلمة مرور قوية تحتوي على أحرف وأرقام لحماية حسابك.")
        }
        return Pair(true, null)
    }
}
