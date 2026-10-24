package com.serviseyem.security

import android.content.Context
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.os.Build
import android.os.SystemClock
import android.util.Log
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import java.io.File
import java.security.MessageDigest

/**
 * 🛡️ SecurityManager - إدارة الأمان والحماية والحد من محاولات الدخول الخاطئة
 */
class SecurityManager(context: Context) {
    private val appContext: Context = context.applicationContext ?: context

    private val securePrefs: SharedPreferences
        get() = getOrCreateSecurePrefs(appContext)

    fun registerFailedAttempt(): Boolean {
        val existingLockTime = securePrefs.getLong(KEY_LOCKOUT_TIMESTAMP, 0L)
        if (existingLockTime != 0L && !isLockedOut()) {
            resetAttempts()
        }

        val attempts = securePrefs.getInt(KEY_FAILED_ATTEMPTS, 0) + 1
        val editor = securePrefs.edit().putInt(KEY_FAILED_ATTEMPTS, attempts)

        if (attempts >= MAX_ATTEMPTS) {
            val lockTime = System.currentTimeMillis() + LOCKOUT_DURATION_MS
            val currentElapsed = try {
                SystemClock.elapsedRealtime()
            } catch (e: Exception) {
                0L
            }
            editor.putLong(KEY_LOCKOUT_TIMESTAMP, lockTime)
            if (currentElapsed > 0L) {
                editor.putLong(KEY_LOCKOUT_ELAPSED, currentElapsed + LOCKOUT_DURATION_MS)
            }
            editor.apply()
            return true // تم القفل
        }
        editor.apply()
        return false
    }

    fun isLockedOut(): Boolean {
        val remainingMs = getRemainingLockoutMs()
        if (remainingMs > 0L) {
            return true
        }
        if (securePrefs.getLong(KEY_LOCKOUT_TIMESTAMP, 0L) != 0L) {
            resetAttempts()
        }
        return false
    }

    fun getRemainingLockoutSeconds(): Long {
        val ms = getRemainingLockoutMs()
        return if (ms > 0L) ((ms + 999L) / 1000L) else 0L
    }

    private fun getRemainingLockoutMs(): Long {
        val lockTime = securePrefs.getLong(KEY_LOCKOUT_TIMESTAMP, 0L)
        if (lockTime == 0L) return 0L

        val wallDiff = lockTime - System.currentTimeMillis()
        val lockElapsed = securePrefs.getLong(KEY_LOCKOUT_ELAPSED, 0L)
        val currentElapsed = try {
            SystemClock.elapsedRealtime()
        } catch (e: Exception) {
            0L
        }

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

    internal fun resetAttempts() {
        securePrefs.edit()
            .remove(KEY_FAILED_ATTEMPTS)
            .remove(KEY_LOCKOUT_TIMESTAMP)
            .remove(KEY_LOCKOUT_ELAPSED)
            .apply()
    }

    fun savePinCode(pin: String) {
        val cleanPin = pin.trim()
        if (cleanPin.isEmpty()) return
        val hashed = com.serviseyem.utils.SecureHasher.hashPin(cleanPin)
        securePrefs.edit().putString(KEY_SECURE_PIN, hashed).apply()
    }

    fun verifyPinCode(inputPin: String): Boolean {
        val cleanPin = inputPin.trim()
        if (cleanPin.isEmpty()) return false
        val savedPinHash = securePrefs.getString(KEY_SECURE_PIN, null) ?: return false
        return com.serviseyem.utils.SecureHasher.verifyPin(cleanPin, savedPinHash)
    }

    fun hasPinCode(): Boolean {
        return !securePrefs.getString(KEY_SECURE_PIN, null).isNullOrEmpty()
    }

    companion object {
        private const val TAG = "SecurityManager"
        private const val PREFS_FALLBACK_NAME = "app_security_prefs"
        private const val PREFS_SECURE_NAME = "app_security_secure_prefs"
        private const val KEY_FAILED_ATTEMPTS = "failed_attempts"
        private const val KEY_LOCKOUT_TIMESTAMP = "lockout_timestamp"
        private const val KEY_LOCKOUT_ELAPSED = "lockout_elapsed"
        private const val KEY_SECURE_PIN = "secure_local_pin"
        private const val MAX_ATTEMPTS = 5
        private const val LOCKOUT_DURATION_MS = 30 * 60 * 1000L // 30 minutes

        @Volatile
        private var cachedSecurePrefs: SharedPreferences? = null

        private fun getOrCreateSecurePrefs(appContext: Context): SharedPreferences {
            cachedSecurePrefs?.let { return it }
            synchronized(SecurityManager::class.java) {
                cachedSecurePrefs?.let { return it }
                val resolved = try {
                    val masterKey = MasterKey.Builder(appContext)
                        .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
                        .build()

                    EncryptedSharedPreferences.create(
                        appContext,
                        PREFS_SECURE_NAME,
                        masterKey,
                        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
                    )
                } catch (e: Throwable) {
                    try {
                        appContext.deleteSharedPreferences(PREFS_SECURE_NAME)
                        val masterKey = MasterKey.Builder(appContext)
                            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
                            .build()
                        EncryptedSharedPreferences.create(
                            appContext,
                            PREFS_SECURE_NAME,
                            masterKey,
                            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
                        )
                    } catch (ex: Throwable) {
                        Log.w(TAG, "EncryptedSharedPreferences unavailable; falling back to private preferences: ${ex.message}")
                        appContext.getSharedPreferences(PREFS_FALLBACK_NAME, Context.MODE_PRIVATE)
                    }
                }
                cachedSecurePrefs = resolved
                return resolved
            }
        }

        /**
         * Checks if the device is rooted or running in a compromised environment.
         */
        fun isDeviceRooted(): Boolean {
            return try {
                val rootFiles = listOf(
                    "/system/app/Superuser.apk",
                    "/sbin/su",
                    "/system/bin/su",
                    "/system/xbin/su",
                    "/system/xbin/daemonsu",
                    "/data/local/xbin/su",
                    "/data/local/bin/su",
                    "/system/sd/xbin/su",
                    "/system/bin/failsafe/su",
                    "/data/local/su",
                    "/sbin/.magisk",
                    "/cache/.disable_magisk",
                    "/dev/.magisk.unblock"
                )
                for (file in rootFiles) {
                    if (File(file).exists()) return true
                }

                if (Build.TAGS?.contains("test-keys") == true) return true

                false
            } catch (e: Throwable) {
                false
            }
        }

        /**
         * Checks for known hooking tools such as Frida or Xposed.
         */
        fun isHookingFrameworkDetected(): Boolean {
            return try {
                val checkPaths = listOf(
                    "/data/local/tmp/frida-server",
                    "/system/framework/XposedBridge.jar",
                    "/system/lib/libsubstrate.so",
                    "/system/lib64/libsubstrate.so"
                )
                for (p in checkPaths) {
                    if (File(p).exists()) return true
                }

                val mapsFile = File("/proc/self/maps")
                if (mapsFile.exists() && mapsFile.canRead()) {
                    mapsFile.useLines { lines ->
                        for (line in lines) {
                            if (line.contains("frida-agent") || line.contains("XposedBridge.jar")) {
                                return true
                            }
                        }
                    }
                }
                false
            } catch (e: Throwable) {
                false
            }
        }

        /**
         * Unified helper to check if the runtime environment shows signs of root or hooking.
         */
        fun isDeviceCompromised(): Boolean {
            return isDeviceRooted() || isHookingFrameworkDetected()
        }

        /**
         * Verifies application signature SHA-256 digest against expected hashes to detect illegal repackaging.
         */
        fun verifyAppSignature(context: Context): Boolean {
            return try {
                val expectedHash = com.serviseyem.BuildConfig.SIGNATURE_HASH.trim()
                if (expectedHash.isEmpty()) {
                    return true
                }

                val pm = context.packageManager
                val pkg = context.packageName
                val signatures = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    val signingInfo = pm.getPackageInfo(
                        pkg,
                        PackageManager.PackageInfoFlags.of(PackageManager.GET_SIGNING_CERTIFICATES.toLong())
                    ).signingInfo
                    if (signingInfo != null) {
                        if (signingInfo.hasMultipleSigners()) {
                            signingInfo.apkContentsSigners
                        } else {
                            signingInfo.signingCertificateHistory
                        }
                    } else null
                } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                    @Suppress("DEPRECATION")
                    val signingInfo = pm.getPackageInfo(pkg, PackageManager.GET_SIGNING_CERTIFICATES).signingInfo
                    if (signingInfo != null) {
                        if (signingInfo.hasMultipleSigners()) {
                            signingInfo.apkContentsSigners
                        } else {
                            signingInfo.signingCertificateHistory
                        }
                    } else null
                } else {
                    @Suppress("DEPRECATION")
                    pm.getPackageInfo(pkg, PackageManager.GET_SIGNATURES).signatures
                }

                if (signatures.isNullOrEmpty()) {
                    return true
                }

                val md = MessageDigest.getInstance("SHA-256")
                for (sig in signatures) {
                    val digest = md.digest(sig.toByteArray())
                    val hash = digest.joinToString("") { "%02x".format(it) }
                    if (hash.equals(expectedHash, ignoreCase = true)) {
                        return true
                    }
                }
                Log.e(TAG, "Signature mismatch!")
                false
            } catch (e: Throwable) {
                Log.e(TAG, "Exception during signature verification: ${e.message}")
                true
            }
        }
    }
}
