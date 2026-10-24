package com.serviseyem.ui.screens.register.forms

import android.content.Context
import com.serviseyem.ui.*
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey

/**
 * Manager for auto-saving and restoring registration drafts securely using EncryptedSharedPreferences.
 */
class RegistrationDraftManager(context: Context) {
    private val prefs: SharedPreferences = run {
        val prefsName = "RegistrationDraftsSecure"
        val appCtx = context.applicationContext ?: context
        fun createEncrypted(): SharedPreferences {
            val masterKey = MasterKey.Builder(appCtx)
                .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
                .build()
            return EncryptedSharedPreferences.create(
                appCtx,
                prefsName,
                masterKey,
                EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
            )
        }
        try {
            createEncrypted()
        } catch (e: Exception) {
            try {
                appCtx.deleteSharedPreferences(prefsName)
                createEncrypted()
            } catch (e2: Exception) {
                appCtx.getSharedPreferences(prefsName, Context.MODE_PRIVATE)
            }
        }
    }

    fun saveDraft(role: String, data: Map<String, String>) {
        val editor = prefs.edit()
        data.forEach { (key, value) ->
            if (key == "password" || key == "confirmPassword") return@forEach
            editor.putString("${role}_$key", value)
        }
        editor.apply()
    }

    fun getDraft(role: String): Map<String, String> {
        val keys = listOf("entityName", "managerName", "phone", "city", "specialization")
        val map = mutableMapOf<String, String>()
        keys.forEach { key ->
            prefs.getString("${role}_$key", null)?.let { value ->
                map[key] = value
            }
        }
        return map
    }

    fun clearDraft(role: String) {
        val keys = listOf("entityName", "managerName", "phone", "city", "specialization")
        val editor = prefs.edit()
        keys.forEach { key ->
            editor.remove("${role}_$key")
        }
        editor.apply()
    }
}
