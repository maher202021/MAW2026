package com.serviseyem.data.models

import androidx.annotation.Keep

@Keep
enum class Currency(val code: String, val symbolArabic: String, val nameArabic: String) {
    YER("YER", "ر.ي", "ريال يمني"),
    SAR("SAR", "ر.س", "ريال سعودي"),
    USD("USD", "$", "دولار أمريكي")
}

/**
 * 📌 Architectural Note: Data Model AdminRole enum representing system role categories and Arabic display titles.
 * Used across admin domain models and UI permissions rendering.
 */
@Keep
enum class AdminRole(val code: String, val titleArabic: String) {
    OWNER("OWNER", "المالك"),
    SUPER_ADMIN("SUPER_ADMIN", "مدير النظام الشامل"),
    MAIN_ADMIN("MAIN_ADMIN", "المدير الرئيسي"),
    ADMIN("ADMIN", "مدير للنظام"),
    SUPERVISOR("SUPERVISOR", "مشرف"),
    AUDITOR("AUDITOR", "مراقب مالي"),
    SUPPORT("SUPPORT", "الدعم الفني"),
    OPERATIONS("OPERATIONS", "إدارة العمليات"),
    GUEST("GUEST", "زائر");

    val roleId: String get() = code

    companion object {
        fun fromString(value: String): AdminRole {
            val clean = value.trim()
            return entries.firstOrNull {
                it.code.equals(clean, ignoreCase = true) || it.titleArabic == clean
            } ?: GUEST
        }
    }
}

@Keep
enum class PaymentStatus(val code: String, val labelArabic: String) {
    PENDING("PENDING", "قيد الانتظار"),
    PROCESSING("PROCESSING", "قيد المعالجة"),
    COMPLETED("COMPLETED", "مكتملة"),
    FAILED("FAILED", "فاشلة"),
    REFUNDED("REFUNDED", "مسترجعة"),
    CANCELLED("CANCELLED", "ملغاة"),
    DISPUTED("DISPUTED", "قيد النزاع")
}

@Keep
enum class NotificationType(val code: String, val titleArabic: String) {
    NORMAL("NORMAL", "إشعار عام"),
    BOOKING("BOOKING", "إشعار حجز"),
    MESSAGE("MESSAGE", "رسالة جديدة"),
    SYSTEM("SYSTEM", "إشعار نظام"),
    ADMIN("ADMIN", "إشعار إدارة"),
    REGISTRATION_APPROVED("REGISTRATION_APPROVED", "قبول التسجيل"),
    SPECIAL_OFFER("SPECIAL_OFFER", "عرض خاص"),
    JOIN_REQUEST("JOIN_REQUEST", "طلب انضمام"),
    JOIN_APPROVED("JOIN_APPROVED", "قبول انضمام"),
    JOIN_REJECTED("JOIN_REJECTED", "رفض انضمام")
}

@Keep
enum class JoinRequestStatus(val code: String, val labelArabic: String) {
    PENDING("PENDING", "قيد المراجعة"),
    APPROVED("APPROVED", "مقبول"),
    REJECTED("REJECTED", "مرفوض"),
    ACTIVE("ACTIVE", "نشط")
}

@Keep
enum class InstantRequestStatus(val code: String, val labelArabic: String) {
    WAITING_FOR_OFFERS("WAITING_FOR_OFFERS", "بانتظار العروض"),
    REVIEWING_OFFERS("REVIEWING_OFFERS", "مراجعة العروض"),
    ACCEPTED("ACCEPTED", "تم قبول العرض"),
    IN_PROGRESS("IN_PROGRESS", "قيد التنفيذ"),
    COMPLETED("COMPLETED", "مكتمل"),
    EXPIRED("EXPIRED", "منتهي الصلاحية"),
    CANCELLED("CANCELLED", "ملغى")
}

@Keep
enum class UrgencyLevel(val code: String, val labelArabic: String) {
    NORMAL("NORMAL", "عادي"),
    URGENT("URGENT", "عاجل")
}

@Keep
enum class OrderStatus(val code: String, val labelArabic: String) {
    PENDING("PENDING", "قيد الانتظار"),
    PROCESSING("PROCESSING", "قيد التجهيز والتوصيل"),
    COMPLETED("COMPLETED", "مكتمل ومستلم"),
    CANCELLED("CANCELLED", "ملغي")
}

@Keep
enum class WalletProvider(val code: String, val displayNameAr: String) {
    JEEB("jeeb", "محفظة جيب"),
    ALKARIMI("alKarimi", "الكريمي إكسبرس / حاسب"),
    JAWALY("jawaly", "جوالي"),
    FLOOSI("floosi", "ام فلوس / فلوسك"),
    ONE_CASH("oneCash", "ون كاش"),
    CASH_EXCHANGE("cashExchange", "صرافة / تحويل"),
    FOREIGN_CURRENCY("foreignCurrency", "عملات أجنبية"),
    YEMEN_MOBILE("yemenMobile", "يمن موبايل"),
    OTHER("other", "أخرى");

    companion object {
        fun fromCode(code: String): WalletProvider {
            val clean = code.trim()
            return entries.firstOrNull { 
                it.code.equals(clean, ignoreCase = true) || it.displayNameAr.contains(clean, ignoreCase = true) 
            } ?: OTHER
        }
    }
}

