package com.serviseyem.utils

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey

/**
 * 🔐 نموذج جلسة الأدمن
 */
data class AdminSession(
    val uid: String,
    val email: String,
    val loginTime: Long,
    val refreshToken: String,
    val role: String = "ADMIN",
    val permissions: List<String> = emptyList()
)

/**
 * 🔐 SecureStorage
 * تخزين مشفر وآمن لجلسات الأدمن والبيانات الحساسة
 * باستخدام EncryptedSharedPreferences و Android KeyStore
 */
class SecureStorage(
    private val context: Context
) {
    private var isUsingEncryptedPrefs = false

    private val prefs: SharedPreferences by lazy {
        try {
            val masterKey = MasterKey.Builder(context)
                .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
                .build()

            val encryptedPrefs = EncryptedSharedPreferences.create(
                context,
                "secure_admin_prefs",
                masterKey,
                EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
            )
            isUsingEncryptedPrefs = true
            migrateFromPlainSharedPreferences(encryptedPrefs)
            encryptedPrefs
        } catch (e: Throwable) {
            try {
                context.deleteSharedPreferences("secure_admin_prefs")
                val masterKey = MasterKey.Builder(context)
                    .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
                    .build()
                val recreatedPrefs = EncryptedSharedPreferences.create(
                    context,
                    "secure_admin_prefs",
                    masterKey,
                    EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                    EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
                )
                isUsingEncryptedPrefs = true
                recreatedPrefs
            } catch (ex: Throwable) {
                isUsingEncryptedPrefs = false
                context.getSharedPreferences("secure_admin_prefs_vault_enc", Context.MODE_PRIVATE)
            }
        }
    }

    private fun putSecureString(editor: SharedPreferences.Editor, key: String, value: String) {
        val storedValue = if (isUsingEncryptedPrefs) value else SecurityCryptoUtils.encrypt(value)
        editor.putString(key, storedValue)
    }

    private fun getSecureString(key: String): String? {
        val raw = prefs.getString(key, null) ?: return null
        return if (isUsingEncryptedPrefs) raw else SecurityCryptoUtils.decrypt(raw)
    }

    /**
     * 🔄 ترحيل بيانات الدخول والجلسات من SharedPreferences العادي إلى المشفر وحذف النسخة القديمة
     */
    private fun migrateFromPlainSharedPreferences(targetPrefs: SharedPreferences) {
        val legacySources = listOf("secure_admin_prefs_fallback", "admin_prefs", "admin_session_prefs")
        for (sourceName in legacySources) {
            try {
                val oldPrefs = context.getSharedPreferences(sourceName, Context.MODE_PRIVATE)
                val allEntries = oldPrefs.all
                if (allEntries.isNotEmpty()) {
                    val editor = targetPrefs.edit()
                    for ((key, value) in allEntries) {
                        if (!targetPrefs.contains(key)) {
                            when (value) {
                                is String -> editor.putString(key, value)
                                is Long -> editor.putLong(key, value)
                                is Int -> editor.putInt(key, value)
                                is Boolean -> editor.putBoolean(key, value)
                                is Float -> editor.putFloat(key, value)
                            }
                        }
                    }
                    editor.apply()
                    oldPrefs.edit().clear().apply()
                }
            } catch (e: Throwable) {
                // تجاوز أي استثناء في حال كان الملف غير موجود
            }
        }
    }

    /**
     * 🔐 حفظ جلسة الأدمن بشكل آمن
     */
    fun saveAdminSession(session: AdminSession) {
        val editor = prefs.edit()
        putSecureString(editor, "admin_uid", session.uid)
        putSecureString(editor, "admin_email", session.email)
        editor.putLong("admin_login_time", session.loginTime)
        putSecureString(editor, "admin_refresh_token", session.refreshToken)
        putSecureString(editor, "admin_role", session.role)
        putSecureString(editor, "admin_permissions", session.permissions.joinToString(","))
        editor.apply()
    }

    /**
     * 📖 استرجاع جلسة الأدمن
     */
    fun getAdminSession(): AdminSession? {
        val uid = getSecureString("admin_uid") ?: return null
        val email = getSecureString("admin_email") ?: return null
        val loginTime = prefs.getLong("admin_login_time", 0)
        val refreshToken = getSecureString("admin_refresh_token") ?: return null
        val role = getSecureString("admin_role") ?: "ADMIN"
        val permsStr = getSecureString("admin_permissions") ?: ""
        val permissions = if (permsStr.isEmpty()) emptyList() else permsStr.split(",")

        return AdminSession(uid, email, loginTime, refreshToken, role, permissions)
    }

    /**
     * 🗑️ مسح جلسة الأدمن
     */
    fun clearAdminSession() {
        prefs.edit()
            .remove("admin_uid")
            .remove("admin_email")
            .remove("admin_login_time")
            .remove("admin_refresh_token")
            .remove("admin_role")
            .remove("admin_permissions")
            .apply()
    }

    /**
     * 🔐 حفظ أي نص بشكل مشفر وآمن
     */
    fun saveSecureString(key: String, value: String) {
        val editor = prefs.edit()
        putSecureString(editor, key, value)
        editor.apply()
    }

    /**
     * 📖 استرجاع أي نص مشفر بشكل آمن
     */
    fun getSecureStringPublic(key: String, defaultValue: String? = null): String? {
        return getSecureString(key) ?: defaultValue
    }
}
