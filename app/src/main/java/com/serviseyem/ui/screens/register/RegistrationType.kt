package com.serviseyem.ui.screens.register

/**
 * 🏷️ أنواع الحسابات المتاحة للتسجيل في منصة دليل خدمات اليمن
 */
enum class RegistrationType(
    val id: String,
    val title: String,
    val icon: String,
    val description: String
) {
    CLIENT(
        id = "client",
        title = "مستخدم عادي",
        icon = "👤",
        description = "إنشاء حساب عميل للاستفادة من الخدمات وطلب الصيانة"
    ),
    PROVIDER(
        id = "provider",
        title = "فني / مهني",
        icon = "🔧",
        description = "تقديم طلب انضمام كفني معتمد واستقبال طلبات الحجز"
    ),
    STORE(
        id = "store",
        title = "متجر / معرض",
        icon = "🏪",
        description = "تسجيل محل تجاري أو معرض لعرض منتجاتك وخدماتك"
    ),
    RESTAURANT(
        id = "restaurant",
        title = "مطعم / كافيه",
        icon = "🍔",
        description = "تسجيل مطعم أو كافيه لاستقبال الطلبات والخدمات"
    ),
    MEDICAL(
        id = "medical",
        title = "مركز طبي / عيادة",
        icon = "🏥",
        description = "تسجيل عيادة أو مركز طبي أو صيدلية لتسهيل وصول المرضى"
    ),
    PROPERTY(
        id = "property",
        title = "عقار / أرض",
        icon = "🏠",
        description = "إدراج عقار للبيع أو الإيجار والوصول للباحثين عن سكن"
    ),
    JOB(
        id = "job",
        title = "نشر وظيفة / صاحب عمل",
        icon = "🏢",
        description = "نشر إعلان وظيفة شاغرة جديدة واستقبال طلبات التوظيف"
    ),
    JOB_SEEKER(
        id = "job_seeker",
        title = "استمارة متقدم لوظيفة / باحث عن عمل",
        icon = "👨‍💼",
        description = "تقديم استمارة السيرة الذاتية والانضمام كباحث عن وظيفة بالمنصة"
    );

    companion object {
        fun fromId(id: String?): RegistrationType? {
            if (id.isNullOrBlank()) return null
            return values().firstOrNull { it.id.equals(id, ignoreCase = true) }
        }
    }
}
