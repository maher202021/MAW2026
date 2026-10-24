package com.serviseyem.ui.helpers

import android.content.Context
import com.serviseyem.ui.*
import android.content.SharedPreferences

/**
 * Helper class to centralize and safely handle SharedPreferences operations across the application.
 */
class AppPreferenceHelper {

    companion object {
        private const val PREFS_YEMEN_SERVICE = "yemen_service_prefs"
        private const val PREFS_APP = "app_prefs"

        private const val KEY_JOIN_REQUEST_PHONE = "join_request_phone"
        private const val KEY_PASSWORD_RECOVERY_WAITING_PHONE = "password_recovery_waiting_phone"
        private const val KEY_IS_ACCOUNT_LOGGED_IN = "is_account_logged_in"
        private const val KEY_READ_NOTIFICATIONS = "read_notifications"

        /**
         * 📞 تطبيع أرقام الهواتف اليمنية لتكون موحدة بـ 9 أرقام
         *
         * سلوك الدالة:
         * 1. استخراج الأرقام فقط من النص المدخل.
         * 2. إذا كان النص فارغاً أو أقل من 7 أرقام، يتم إرجاع النص الأصلي كما هو.
         * 3. إذا كان يبدأ بـ 00967، يتم حذفها وإرجاع الباقي.
         * 4. إذا كان يبدأ بـ 967، يتم حذفها وإرجاع الباقي.
         * 5. إذا كان يبدأ بـ 0 وكان طوله 10 أرقام، يتم حذف الصفر الأول.
         * 6. إذا كان الطول النهائي 9 أرقام، يتم إرجاع الأرقام التسعة.
         * 7. إذا كان الطول أكثر من 9، يتم إرجاع آخر 9 أرقام.
         * 8. إذا كان الطول أقل من 9 بعد كل التعديلات، يتم إرجاع النص الأصلي.
         */
        fun normalizePhoneNumber(phone: String): String {
            if (phone.isBlank()) return phone
            val digits = phone.filter { it.isDigit() }
            if (digits.length < 7) return phone

            var processed = digits
            if (processed.startsWith("00967")) {
                processed = processed.substring(5)
            } else if (processed.startsWith("967")) {
                processed = processed.substring(3)
            }

            if (processed.startsWith("0") && processed.length == 10) {
                processed = processed.substring(1)
            }

            if (processed.length == 9) {
                return processed
            }

            if (processed.length > 9) {
                return processed.takeLast(9)
            }

            return phone
        }
    }

    private fun getYemenServicePrefs(context: Context): SharedPreferences {
        return context.getSharedPreferences(PREFS_YEMEN_SERVICE, Context.MODE_PRIVATE)
    }

    private fun getAppPrefs(context: Context): SharedPreferences {
        return context.getSharedPreferences(PREFS_APP, Context.MODE_PRIVATE)
    }

    // --- Join Request Phone ---
    fun getJoinRequestPhone(context: Context): String {
        val stored = getYemenServicePrefs(context).getString(KEY_JOIN_REQUEST_PHONE, "") ?: ""
        if (stored.startsWith("enc::") || stored.startsWith("gcm:") || stored.startsWith("gcmx:")) {
            val decrypted = com.serviseyem.utils.SecurityCryptoUtils.decrypt(stored.removePrefix("enc::"))
            return if (decrypted.isNotBlank() && !decrypted.startsWith("gcm:") && !decrypted.startsWith("gcmx:") && !decrypted.startsWith("enc::")) decrypted else ""
        }
        return stored
    }

    fun setJoinRequestPhone(context: Context, phone: String) {
        if (phone.isBlank()) {
            getYemenServicePrefs(context).edit().remove(KEY_JOIN_REQUEST_PHONE).apply()
            return
        }
        val encrypted = "enc::" + com.serviseyem.utils.SecurityCryptoUtils.encrypt(phone)
        getYemenServicePrefs(context).edit().putString(KEY_JOIN_REQUEST_PHONE, encrypted).apply()
    }

    fun clearJoinRequestPhone(context: Context) {
        getYemenServicePrefs(context).edit().remove(KEY_JOIN_REQUEST_PHONE).apply()
    }

    // --- Password Recovery Waiting Phone ---
    fun getPasswordRecoveryWaitingPhone(context: Context): String {
        val stored = getYemenServicePrefs(context).getString(KEY_PASSWORD_RECOVERY_WAITING_PHONE, "") ?: ""
        if (stored.startsWith("enc::") || stored.startsWith("gcm:") || stored.startsWith("gcmx:")) {
            val decrypted = com.serviseyem.utils.SecurityCryptoUtils.decrypt(stored.removePrefix("enc::"))
            return if (decrypted.isNotBlank() && !decrypted.startsWith("gcm:") && !decrypted.startsWith("gcmx:") && !decrypted.startsWith("enc::")) decrypted else ""
        }
        return stored
    }

    fun setPasswordRecoveryWaitingPhone(context: Context, phone: String) {
        if (phone.isBlank()) {
            getYemenServicePrefs(context).edit().remove(KEY_PASSWORD_RECOVERY_WAITING_PHONE).apply()
            return
        }
        val encrypted = "enc::" + com.serviseyem.utils.SecurityCryptoUtils.encrypt(phone)
        getYemenServicePrefs(context).edit().putString(KEY_PASSWORD_RECOVERY_WAITING_PHONE, encrypted).apply()
    }

    // --- Account Login Status ---
    fun isAccountLoggedIn(context: Context): Boolean {
        return getYemenServicePrefs(context).getBoolean(KEY_IS_ACCOUNT_LOGGED_IN, false)
    }

    fun setAccountLoggedIn(context: Context, isLoggedIn: Boolean) {
        getYemenServicePrefs(context).edit().putBoolean(KEY_IS_ACCOUNT_LOGGED_IN, isLoggedIn).apply()
    }

    // --- Notifications Read Status ---
    fun getReadNotificationIds(context: Context): Set<String> {
        return getAppPrefs(context).getStringSet(KEY_READ_NOTIFICATIONS, emptySet())?.toSet() ?: emptySet()
    }

    fun markNotificationAsRead(context: Context, notifId: String, currentRead: MutableSet<String>): Set<String> {
        val updated = currentRead.toMutableSet().apply { add(notifId) }
        getAppPrefs(context).edit().putStringSet(KEY_READ_NOTIFICATIONS, HashSet(updated)).apply()
        return updated
    }

    fun markAllNotificationsAsRead(context: Context, allIds: Set<String>): Set<String> {
        val copy = HashSet(allIds)
        getAppPrefs(context).edit().putStringSet(KEY_READ_NOTIFICATIONS, copy).apply()
        return copy
    }

    /**
     * 📞 تطبيع أرقام الهواتف اليمنية لتكون موحدة بـ 9 أرقام (تابع للنسخة الاستاتيكية)
     */
    fun normalizePhoneNumber(phone: String): String {
        return Companion.normalizePhoneNumber(phone)
    }
}
