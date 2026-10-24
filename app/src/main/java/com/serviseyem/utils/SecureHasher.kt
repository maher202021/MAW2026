package com.serviseyem.utils

import android.util.Base64
import java.security.MessageDigest
import java.security.SecureRandom
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.PBEKeySpec

/**
 * 🔒 SecureHasher
 * المحرك الموحد الآمن لتشفير والتحقق من كلمة المرور والرموز السرية (PBKDF2WithHmacSHA256)
 * محمي ضد هجمات التوقيت (Timing Attacks) عبر المقارنة الثابتة زمنياً (MessageDigest.isEqual)
 */
object SecureHasher {

    private const val ALGORITHM = "PBKDF2WithHmacSHA256"
    private const val ITERATIONS_PASSWORD = 12000
    private const val ITERATIONS_PIN = 10000
    private const val KEY_LENGTH = 256
    private const val SALT_SIZE = 16

    private val secureRandom = SecureRandom()

    fun generateSalt(): ByteArray {
        val salt = ByteArray(SALT_SIZE)
        secureRandom.nextBytes(salt)
        return salt
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
        } catch (e: Throwable) {
            try {
                Base64.decode(clean, Base64.DEFAULT)
            } catch (_: Throwable) {
                java.util.Base64.getDecoder().decode(clean)
            }
        }
    }

    fun hashPassword(password: String, salt: ByteArray = generateSalt()): String {
        val cleanPassword = password.trim()
        if (cleanPassword.isEmpty()) return ""
        val effectiveSalt = if (salt.isNotEmpty()) salt else generateSalt()
        val spec = PBEKeySpec(cleanPassword.toCharArray(), effectiveSalt, ITERATIONS_PASSWORD, KEY_LENGTH)
        return try {
            val skf = SecretKeyFactory.getInstance(ALGORITHM)
            val hash = skf.generateSecret(spec).encoded
            val saltBase64 = base64Encode(effectiveSalt)
            val hashBase64 = base64Encode(hash)
            "$saltBase64:$hashBase64"
        } finally {
            spec.clearPassword()
        }
    }

    private fun verifyWithIterations(input: String, salt: ByteArray, expectedHashBytes: ByteArray, iterations: Int): Boolean {
        if (input.isBlank() || salt.isEmpty() || expectedHashBytes.isEmpty()) return false
        val spec = PBEKeySpec(input.toCharArray(), salt, iterations, KEY_LENGTH)
        return try {
            val skf = SecretKeyFactory.getInstance(ALGORITHM)
            val actualHashBytes = skf.generateSecret(spec).encoded
            MessageDigest.isEqual(actualHashBytes, expectedHashBytes)
        } finally {
            spec.clearPassword()
        }
    }

    fun verifyPassword(password: String, storedHash: String): Boolean {
        if (password.isBlank() || storedHash.isBlank()) return false
        val trimmedInput = password.trim()
        val trimmedStored = storedHash.trim()

        // 🎯 أمان: لا نقبل النص الصريح أبداً ولا نقبل إدخال الهاش المسروق ككلمة مرور.
        if (!trimmedStored.contains(":") || trimmedInput == trimmedStored || isHashFormat(trimmedInput)) {
            return false
        }

        return try {
            val parts = trimmedStored.split(":")
            if (parts.size != 2 || parts[0].isBlank() || parts[1].isBlank()) return false
            val salt = base64Decode(parts[0])
            val expectedHashBytes = base64Decode(parts[1])
            verifyWithIterations(trimmedInput, salt, expectedHashBytes, ITERATIONS_PASSWORD) ||
                verifyWithIterations(trimmedInput, salt, expectedHashBytes, ITERATIONS_PIN)
        } catch (e: Exception) {
            false
        }
    }

    fun hashPin(pin: String, salt: ByteArray = generateSalt()): String {
        val cleanPin = pin.trim()
        if (cleanPin.isEmpty()) return ""
        val effectiveSalt = if (salt.isNotEmpty()) salt else generateSalt()
        val saltBase64 = base64Encode(effectiveSalt)
        val spec = PBEKeySpec(cleanPin.toCharArray(), effectiveSalt, ITERATIONS_PIN, KEY_LENGTH)
        return try {
            val skf = SecretKeyFactory.getInstance(ALGORITHM)
            val hash = skf.generateSecret(spec).encoded
            val hashBase64 = base64Encode(hash)
            "$saltBase64:$hashBase64"
        } finally {
            spec.clearPassword()
        }
    }

    fun verifyPin(pin: String, storedHash: String): Boolean {
        if (pin.isBlank() || storedHash.isBlank()) return false
        val trimmedInput = pin.trim()
        val trimmedStored = storedHash.trim()

        // 🎯 أمان: لا نقبل النص الصريح أبداً للـ PIN ولا نقبل الهاش المسروق.
        if (!trimmedStored.contains(":") || trimmedInput == trimmedStored || isHashFormat(trimmedInput)) {
            return false
        }

        return try {
            val parts = trimmedStored.split(":")
            if (parts.size != 2) return false
            val salt = base64Decode(parts[0])
            val expectedHashBytes = base64Decode(parts[1])
            verifyWithIterations(trimmedInput, salt, expectedHashBytes, ITERATIONS_PIN) ||
                verifyWithIterations(trimmedInput, salt, expectedHashBytes, ITERATIONS_PASSWORD)
        } catch (e: Exception) {
            false
        }
    }

    /**
     * التحقق من أن النص المخزن يمثل تجزئة مشفرة صالحة بصيغة saltBase64:hashBase64
     */
    fun isValidHash(storedHash: String): Boolean {
        val trimmed = storedHash.trim()
        if (!trimmed.contains(":")) return false
        val parts = trimmed.split(":")
        if (parts.size != 2 || parts[0].isBlank() || parts[1].isBlank()) return false
        return try {
            val salt = base64Decode(parts[0])
            val hash = base64Decode(parts[1])
            salt.size >= 8 && hash.size >= 16
        } catch (e: Throwable) {
            false
        }
    }

    /**
     * يتحقق مما إذا كانت السلسلة المدخلة تبدو كـ Hash مشفر (PBKDF2 أو BCrypt أو SHA-256 Hex) لمنع هجمات Pass-the-Hash
     */
    fun isHashFormat(candidate: String): Boolean {
        val trimmed = candidate.trim()
        if (trimmed.isEmpty()) return false
        if (isValidHash(trimmed)) return true
        if (trimmed.startsWith("$2a$") || trimmed.startsWith("$2b$") || trimmed.startsWith("$2y$")) return true
        if (trimmed.startsWith("pbkdf2:")) return true
        if (trimmed.length == 64 && trimmed.all { it in '0'..'9' || it in 'a'..'f' || it in 'A'..'F' }) return true
        return false
    }
}
