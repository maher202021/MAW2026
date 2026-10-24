package com.serviseyem.utils

import android.util.Patterns

/**
 * 🛠️ ValidationUtils - أدوات التحقق من مدخلات المستخدم والبيانات
 */
object ValidationUtils {
    /**
     * التحقق الصارم من صحة البريد الإلكتروني
     */
    fun isValidEmail(email: String): Boolean {
        if (email.isBlank()) return false
        return Patterns.EMAIL_ADDRESS.matcher(email.trim()).matches()
    }

    /**
     * التحقق من صحة رقم الهاتف اليمني أو الدولي
     */
    fun isValidPhone(phone: String): Boolean {
        val clean = phone.trim().replace(" ", "").replace("-", "")
        if (clean.length < 7) return false
        return Patterns.PHONE.matcher(clean).matches()
    }
}
