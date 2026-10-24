package com.serviseyem.utils

import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Base64
import com.google.firebase.crashlytics.FirebaseCrashlytics
import java.security.KeyStore
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
 * 🔐 ChatCryptoManager
 * تشفير وفك تشفير الرسائل بتقنية AES-256-GCM الموثقة لتأمين الرسائل محلياً على الجهاز.
 */
object ChatCryptoManager {

    private const val ALGORITHM_GCM = "AES/GCM/NoPadding"
    private const val LEGACY_ALGORITHM_CBC = "AES/CBC/PKCS5Padding"
    private const val KEYSTORE_ALIAS_GCM = "WAM_Chat_LocalKey_GCM"
    private const val LEGACY_KEYSTORE_ALIAS = "WAM_Chat_LocalKey"
    private const val PBKDF2_ALGORITHM = "PBKDF2WithHmacSHA256"
    private const val PBKDF2_ITERATIONS = 10000
    private const val KEY_SIZE_BITS = 256
    private const val GCM_IV_LENGTH = 12
    private const val GCM_TAG_LENGTH_BITS = 128
    private const val MAX_CACHED_KEYS = 64

    private val secureRandom = SecureRandom()
    private val derivedKeyCache = java.util.concurrent.ConcurrentHashMap<String, SecretKeySpec>()
    private val legacyDerivedKeyCache = java.util.concurrent.ConcurrentHashMap<String, SecretKeySpec>()

    /**
     * الحصول على المفتاح الأساسي من Android KeyStore (بوضع GCM) أو توليده بأمان داخل العتاد
     */
    private fun getOrCreateKeystoreKey(): SecretKey {
        return try {
            val keyStore = KeyStore.getInstance("AndroidKeyStore")
            keyStore.load(null)
            if (!keyStore.containsAlias(KEYSTORE_ALIAS_GCM)) {
                val keyGenerator = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, "AndroidKeyStore")
                val keyGenSpec = KeyGenParameterSpec.Builder(
                    KEYSTORE_ALIAS_GCM,
                    KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
                )
                    .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                    .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                    .setKeySize(KEY_SIZE_BITS)
                    .setRandomizedEncryptionRequired(false)
                    .build()
                keyGenerator.init(keyGenSpec)
                keyGenerator.generateKey()
            } else {
                val entry = keyStore.getEntry(KEYSTORE_ALIAS_GCM, null) as? KeyStore.SecretKeyEntry
                entry?.secretKey ?: SecurityCryptoUtils.getSecretKey()
            }
        } catch (e: Exception) {
            SecurityCryptoUtils.getSecretKey()
        }
    }

    private fun getLegacyKeystoreKey(): java.security.Key {
        return try {
            val keyStore = KeyStore.getInstance("AndroidKeyStore")
            keyStore.load(null)
            val entry = keyStore.getEntry(LEGACY_KEYSTORE_ALIAS, null) as? KeyStore.SecretKeyEntry
            entry?.secretKey ?: SecurityCryptoUtils.getSecretKey()
        } catch (e: Exception) {
            SecurityCryptoUtils.getSecretKey()
        }
    }

    /**
     * توليد مفتاح AES 256 بت من معرف الغرفة باستخدام PBKDF2 مع تخزين مؤقت آمن في الذاكرة
     */
    private fun deriveKeyFromPassphrase(passphrase: String): SecretKeySpec {
        derivedKeyCache[passphrase]?.let { return it }
        val derived = try {
            val factory = SecretKeyFactory.getInstance(PBKDF2_ALGORITHM)
            val digest = java.security.MessageDigest.getInstance("SHA-256")
            val salt = digest.digest(("WAM_E2EE_Salt_v2_" + passphrase).toByteArray(Charsets.UTF_8)).copyOfRange(0, 16)
            val spec = PBEKeySpec(passphrase.toCharArray(), salt, PBKDF2_ITERATIONS, KEY_SIZE_BITS)
            try {
                val tmp = factory.generateSecret(spec)
                SecretKeySpec(tmp.encoded, "AES")
            } finally {
                spec.clearPassword()
            }
        } catch (e: Exception) {
            val digest = java.security.MessageDigest.getInstance("SHA-256")
            val keyBytes = digest.digest(passphrase.toByteArray(Charsets.UTF_8))
            SecretKeySpec(keyBytes, "AES")
        }
        if (derivedKeyCache.size >= MAX_CACHED_KEYS) {
            derivedKeyCache.clear()
        }
        derivedKeyCache[passphrase] = derived
        return derived
    }

    private fun deriveLegacyKeyFromPassphrase(passphrase: String): SecretKeySpec {
        legacyDerivedKeyCache[passphrase]?.let { return it }
        val derived = try {
            val factory = SecretKeyFactory.getInstance(PBKDF2_ALGORITHM)
            val salt = passphrase.toByteArray(Charsets.UTF_8)
            val spec = PBEKeySpec(passphrase.toCharArray(), salt, PBKDF2_ITERATIONS, KEY_SIZE_BITS)
            try {
                val tmp = factory.generateSecret(spec)
                SecretKeySpec(tmp.encoded, "AES")
            } finally {
                spec.clearPassword()
            }
        } catch (e: Exception) {
            val digest = java.security.MessageDigest.getInstance("SHA-256")
            val keyBytes = digest.digest(passphrase.toByteArray(Charsets.UTF_8))
            SecretKeySpec(keyBytes, "AES")
        }
        if (legacyDerivedKeyCache.size >= MAX_CACHED_KEYS) {
            legacyDerivedKeyCache.clear()
        }
        legacyDerivedKeyCache[passphrase] = derived
        return derived
    }

    /**
     * تحديد المفتاح المستخدم للتشفير بناءً على المدخلات
     */
    private fun resolveKey(roomKey: String?): java.security.Key {
        return if (!roomKey.isNullOrBlank()) {
            deriveKeyFromPassphrase(roomKey.trim())
        } else {
            getOrCreateKeystoreKey()
        }
    }

    private fun resolveLegacyKey(roomKey: String?): java.security.Key {
        return if (!roomKey.isNullOrBlank()) {
            deriveLegacyKeyFromPassphrase(roomKey.trim())
        } else {
            getLegacyKeystoreKey()
        }
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
            Base64.decode(clean, Base64.DEFAULT)
        } catch (e: Throwable) {
            java.util.Base64.getMimeDecoder().decode(clean)
        }
    }

    /**
     * تشفير النص العادي إلى Base64 باستخدام AES/GCM/NoPadding و IV عشوائي 12 بايت
     */
    fun encrypt(plainText: String, roomKey: String? = null): String {
        if (plainText.isBlank()) return plainText
        return try {
            val key = resolveKey(roomKey)
            val cipher = Cipher.getInstance(ALGORITHM_GCM)
            val iv = ByteArray(GCM_IV_LENGTH)
            secureRandom.nextBytes(iv)
            val gcmSpec = GCMParameterSpec(GCM_TAG_LENGTH_BITS, iv)

            cipher.init(Cipher.ENCRYPT_MODE, key, gcmSpec)
            val encryptedBytes = cipher.doFinal(plainText.toByteArray(Charsets.UTF_8))
            val combined = iv + encryptedBytes
            "enc::gcm:" + base64Encode(combined)
        } catch (e: Exception) {
            try {
                FirebaseCrashlytics.getInstance().recordException(e)
            } catch (ignored: Throwable) {}
            throw SecurityException("فشل تشفير البيانات الحساسة بأمان: ${e.message}", e)
        }
    }

    /**
     * فك تشفير النص المشفر Base64 مع دعم AES-GCM الحديث والتوافق العكسي لرسائل AES-CBC القديمة
     */
    fun decrypt(cipherText: String, roomKey: String? = null): String {
        if (!cipherText.startsWith("enc::")) return cipherText
        return try {
            val cleanCipher = cipherText.removePrefix("enc::")
            if (cleanCipher.startsWith("gcm:")) {
                val combined = base64Decode(cleanCipher.removePrefix("gcm:"))
                if (combined.size <= GCM_IV_LENGTH) {
                    throw SecurityException("حمولة GCM قصيرة جداً")
                }
                val iv = combined.copyOfRange(0, GCM_IV_LENGTH)
                val encrypted = combined.copyOfRange(GCM_IV_LENGTH, combined.size)
                return try {
                    val key = resolveKey(roomKey)
                    val cipher = Cipher.getInstance(ALGORITHM_GCM)
                    cipher.init(Cipher.DECRYPT_MODE, key, GCMParameterSpec(GCM_TAG_LENGTH_BITS, iv))
                    String(cipher.doFinal(encrypted), Charsets.UTF_8)
                } catch (_: Exception) {
                    val legacyKey = resolveLegacyKey(roomKey)
                    val cipher = Cipher.getInstance(ALGORITHM_GCM)
                    cipher.init(Cipher.DECRYPT_MODE, legacyKey, GCMParameterSpec(GCM_TAG_LENGTH_BITS, iv))
                    String(cipher.doFinal(encrypted), Charsets.UTF_8)
                }
            }

            // التوافق العكسي للرسائل القديمة المشفرة بـ AES/CBC/PKCS5Padding
            val combined = base64Decode(cleanCipher)
            val key = resolveLegacyKey(roomKey)
            val cipher = Cipher.getInstance(LEGACY_ALGORITHM_CBC)
            if (combined.size <= 16) {
                throw SecurityException("حمولة التشفير غير صالحة")
            }
            val iv = combined.copyOfRange(0, 16)
            val encrypted = combined.copyOfRange(16, combined.size)
            val ivSpec = IvParameterSpec(iv)
            cipher.init(Cipher.DECRYPT_MODE, key, ivSpec)
            val decryptedBytes = cipher.doFinal(encrypted)
            String(decryptedBytes, Charsets.UTF_8)
        } catch (e: Exception) {
            try {
                FirebaseCrashlytics.getInstance().recordException(e)
            } catch (ignored: Throwable) {}
            throw SecurityException("فشل فك تشفير الرسالة المشفرة (enc::): ${e.message}", e)
        }
    }
}
