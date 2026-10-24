package com.serviseyem.security

import android.content.Context
import android.content.SharedPreferences
import android.os.SystemClock
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import java.security.MessageDigest

/**
 * 🔒 BookingSecurityHelper
 * Manages PIN encryption (PBKDF2/SHA-256), 3-attempt failure tracking,
 * and 30-minute security lockouts for booking cancellations and modifications.
 */
object BookingSecurityHelper {

    private const val PREFS_NAME = "booking_security_vault"
    private const val SECURE_PREFS_NAME = "booking_security_vault_encrypted"
    private const val KEY_ATTEMPTS_PREFIX = "attempts_"
    private const val KEY_LOCKOUT_PREFIX = "lockout_"
    private const val KEY_LOCKOUT_ELAPSED_PREFIX = "lockout_elapsed_"
    private const val MAX_ATTEMPTS = 3
    private const val LOCKOUT_DURATION_MS = 30 * 60 * 1000L // 30 minutes lockout (ثلاثون دقيقة)

    @Volatile
    private var cachedPrefs: SharedPreferences? = null

    private fun getPrefs(context: Context): SharedPreferences {
        cachedPrefs?.let { return it }
        synchronized(this) {
            cachedPrefs?.let { return it }
            val appCtx = context.applicationContext ?: context
            val resolved = try {
                val masterKey = MasterKey.Builder(appCtx)
                    .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
                    .build()
                EncryptedSharedPreferences.create(
                    appCtx,
                    SECURE_PREFS_NAME,
                    masterKey,
                    EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                    EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
                )
            } catch (e: Throwable) {
                try {
                    appCtx.deleteSharedPreferences(SECURE_PREFS_NAME)
                    val masterKey = MasterKey.Builder(appCtx)
                        .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
                        .build()
                    EncryptedSharedPreferences.create(
                        appCtx,
                        SECURE_PREFS_NAME,
                        masterKey,
                        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
                    )
                } catch (ex: Throwable) {
                    appCtx.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
                }
            }
            cachedPrefs = resolved
            return resolved
        }
    }

    /**
     * Hashes a PIN or password using SecureHasher (PBKDF2/SHA-256) for secure comparison.
     */
    fun hashPin(pin: String): String {
        if (pin.isBlank()) return ""
        return com.serviseyem.utils.SecureHasher.hashPin(pin.trim())
    }

    /**
     * Checks whether a given string is already a hashed PIN/password (PBKDF2, salted SHA-256, or 64-char hex SHA-256).
     */
    fun isSha256Hash(value: String): Boolean {
        val clean = value.trim()
        if (clean.isEmpty()) return false
        if (com.serviseyem.utils.SecureHasher.isValidHash(clean)) return true
        return clean.length == 64 && clean.all { it.isDigit() || it in 'a'..'f' || it in 'A'..'F' }
    }

    /**
     * Checks if a booking is currently locked out from cancellation/modification attempts.
     * Uses both wall-clock time and monotonic elapsedRealtime to prevent manual clock tampering.
     */
    fun isBookingLocked(context: Context, bookingId: String): Boolean {
        val keyId = bookingId.trim()
        if (keyId.isEmpty()) return false
        val prefs = getPrefs(context)
        val lockTime = prefs.getLong(KEY_LOCKOUT_PREFIX + keyId, 0L)
        if (lockTime == 0L) return false

        val remainingMs = computeRemainingLockoutMs(prefs, keyId, lockTime)
        if (remainingMs > 0L) {
            return true
        }

        // Lockout expired, reset
        resetAttempts(context, keyId)
        return false
    }

    /**
     * Returns remaining lockout time in seconds.
     */
    fun getRemainingLockoutSeconds(context: Context, bookingId: String): Long {
        val keyId = bookingId.trim()
        if (keyId.isEmpty()) return 0L
        val prefs = getPrefs(context)
        val lockTime = prefs.getLong(KEY_LOCKOUT_PREFIX + keyId, 0L)
        if (lockTime == 0L) return 0L
        val diff = computeRemainingLockoutMs(prefs, keyId, lockTime)
        return if (diff > 0L) ((diff + 999L) / 1000L) else 0L
    }

    private fun computeRemainingLockoutMs(prefs: SharedPreferences, bookingId: String, lockTime: Long): Long {
        val wallDiff = lockTime - System.currentTimeMillis()
        val lockElapsed = prefs.getLong(KEY_LOCKOUT_ELAPSED_PREFIX + bookingId, 0L)
        val currentElapsed = try {
            SystemClock.elapsedRealtime()
        } catch (e: Exception) {
            0L
        }

        // If monotonic clock was recorded in the current boot session, prevent bypassing via system clock jump
        if (lockElapsed > 0L && currentElapsed > 0L) {
            val elapsedStart = lockElapsed - LOCKOUT_DURATION_MS
            if (currentElapsed >= elapsedStart) {
                val elapsedDiff = lockElapsed - currentElapsed
                return if (elapsedDiff > 0L) {
                    maxOf(wallDiff, elapsedDiff).coerceAtMost(LOCKOUT_DURATION_MS)
                } else {
                    0L
                }
            }
        }
        return if (wallDiff > 0L) wallDiff.coerceAtMost(LOCKOUT_DURATION_MS) else 0L
    }

    /**
     * Records a failed PIN attempt. If attempts reach 3, locks out for 30 minutes (ثلاثون دقيقة).
     * Returns the number of remaining attempts before lockout.
     */
    fun recordFailedAttempt(context: Context, bookingId: String): Int {
        val keyId = bookingId.trim()
        if (keyId.isEmpty()) return 0
        val prefs = getPrefs(context)
        val existingLockTime = prefs.getLong(KEY_LOCKOUT_PREFIX + keyId, 0L)
        if (existingLockTime != 0L && computeRemainingLockoutMs(prefs, keyId, existingLockTime) <= 0L) {
            resetAttempts(context, keyId)
        }

        val currentAttempts = prefs.getInt(KEY_ATTEMPTS_PREFIX + keyId, 0) + 1
        val editor = prefs.edit()
        editor.putInt(KEY_ATTEMPTS_PREFIX + keyId, currentAttempts)

        if (currentAttempts >= MAX_ATTEMPTS) {
            val lockoutTime = System.currentTimeMillis() + LOCKOUT_DURATION_MS
            val currentElapsed = try {
                SystemClock.elapsedRealtime()
            } catch (e: Exception) {
                0L
            }
            editor.putLong(KEY_LOCKOUT_PREFIX + keyId, lockoutTime)
            if (currentElapsed > 0L) {
                editor.putLong(KEY_LOCKOUT_ELAPSED_PREFIX + keyId, currentElapsed + LOCKOUT_DURATION_MS)
            }
            editor.apply()
            return 0
        }
        editor.apply()
        return (MAX_ATTEMPTS - currentAttempts).coerceAtLeast(0)
    }

    /**
     * Resets failed attempts and lockout upon successful verification.
     */
    fun resetAttempts(context: Context, bookingId: String) {
        val keyId = bookingId.trim()
        if (keyId.isEmpty()) return
        val prefs = getPrefs(context)
        prefs.edit()
            .remove(KEY_ATTEMPTS_PREFIX + keyId)
            .remove(KEY_LOCKOUT_PREFIX + keyId)
            .remove(KEY_LOCKOUT_ELAPSED_PREFIX + keyId)
            .apply()
    }

    /**
     * Verifies raw input password against target (handles PBKDF2, salted/unsalted SHA-256, and legacy plain text).
     */
    fun verifyPassword(rawInput: String, targetPasswordOrHash: String): Boolean {
        val cleanInput = rawInput.trim()
        val cleanTarget = targetPasswordOrHash.trim()
        if (cleanInput.isEmpty() || cleanTarget.isEmpty()) return false

        // 1. PBKDF2 / Salted SHA-256 match via SecureHasher
        if (com.serviseyem.utils.SecureHasher.verifyPin(cleanInput, cleanTarget) ||
            com.serviseyem.utils.SecureHasher.verifyPassword(cleanInput, cleanTarget)
        ) {
            return true
        }

        // 2. Legacy 64-char SHA-256 hex digest match
        val isHexSha256 = cleanTarget.length == 64 &&
            cleanTarget.all { it.isDigit() || it in 'a'..'f' || it in 'A'..'F' }
        if (isHexSha256) {
            return try {
                val digest = MessageDigest.getInstance("SHA-256")
                    .digest(cleanInput.toByteArray(Charsets.UTF_8))
                val computedHex = digest.joinToString("") { "%02x".format(it) }
                MessageDigest.isEqual(
                    computedHex.toByteArray(Charsets.UTF_8),
                    cleanTarget.lowercase().toByteArray(Charsets.UTF_8)
                )
            } catch (e: Exception) {
                false
            }
        }

        // 3. Constant-time direct match ONLY for legacy plain-text records (never allow raw hash string replay)
        if (cleanTarget.contains(":")) return false

        val inputBytes = cleanInput.toByteArray(Charsets.UTF_8)
        val targetBytes = cleanTarget.toByteArray(Charsets.UTF_8)
        return MessageDigest.isEqual(inputBytes, targetBytes)
    }

    /**
     * Masks sensitive phone number (e.g. 771234567 -> 77****567) until accepted.
     */
    fun maskPhoneNumber(phone: String): String {
        val clean = phone.trim()
        val len = clean.length
        if (len < 6) return "***"
        
        val prefixLen = if (len >= 10) 3 else 2
        val suffixLen = if (len >= 9) 3 else 2
        val maskLen = (len - prefixLen - suffixLen).coerceAtLeast(2)
        
        val prefix = clean.take(prefixLen)
        val suffix = clean.takeLast(suffixLen)
        val stars = "*".repeat(maskLen)
        return "$prefix$stars$suffix"
    }
}
