package com.serviseyem.data.models

import androidx.annotation.Keep

/**
 * 👥 UserRole
 * enum class موحّدة تمثل جميع الأدوار الممكنة للمستخدمين داخل التطبيق.
 */
@Keep
enum class UserRole(
    val code: String,
    val titleArabic: String,
    val titleEnglish: String
) {
    GUEST("GUEST", "زائر", "Guest"),
    CLIENT("CLIENT", "عميل", "Client"),
    TECHNICIAN("TECHNICIAN", "فني", "Technician"),
    STORE_OWNER("STORE_OWNER", "صاحب متجر", "Store Owner"),
    RESTAURANT_OWNER("RESTAURANT_OWNER", "صاحب مطعم", "Restaurant Owner"),
    MEDICAL_CENTER("MEDICAL_CENTER", "مركز طبي", "Medical Center"),
    REAL_ESTATE("REAL_ESTATE", "مكتب عقاري", "Real Estate"),
    JOB_POSTER("JOB_POSTER", "معلن وظائف", "Job Poster"),
    SUPERVISOR("SUPERVISOR", "مشرف", "Supervisor"),
    ADMIN("ADMIN", "مدير", "Admin"),
    OWNER("OWNER", "مالك", "Owner");

    companion object {
        /**
         * دالة مساعدة للحصول على الدور المناسب من خلال الرمز الداخلي.
         * تعيد الدور زائر [GUEST] كقيمة افتراضية عند عدم المطابقة.
         */
        fun fromCode(code: String): UserRole {
            val upperCode = code.trim().uppercase()
            entries.firstOrNull { it.code == upperCode }?.let { return it }
            return when (upperCode) {
                "PROVIDER" -> TECHNICIAN
                "STORE" -> STORE_OWNER
                "RESTAURANT" -> RESTAURANT_OWNER
                "MEDICAL" -> MEDICAL_CENTER
                "PROPERTY", "PROPERTY_OWNER" -> REAL_ESTATE
                "JOB", "EMPLOYER" -> JOB_POSTER
                "USER", "CUSTOMER" -> CLIENT
                else -> GUEST
            }
        }
    }
}
