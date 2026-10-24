package com.serviseyem.ui.theme

import androidx.compose.ui.graphics.Color

/**
 * 🎨 BookingStatusColors
 * ملف مركزي موحّد يحدد ألوان ونصوص حالات الحجز المختلفة لتسهيل صيانتها وتطبيقها في واجهات التطبيق.
 */
object BookingStatusColors {

    /**
     * إرجاع اللون المناسب لحالة الحجز.
     */
    fun getBookingStatusColor(status: String): Color {
        return when (status.uppercase().trim()) {
            "APPROVED", "ACCEPTED" -> Color(0xFF10B981)   // مقبول ومؤكد (أخضر)
            "PENDING" -> Color(0xFFF59E0B)              // قيد الانتظار (أصفر)
            "IN_PROGRESS" -> Color(0xFF3B82F6)          // جاري التنفيذ (أزرق)
            "REJECTED" -> Color(0xFFEF4444)             // مرفوض (أحمر)
            "COMPLETED" -> Color(0xFF059669)            // مكتمل بنجاح (أخضر داكن)
            "CANCELLED" -> Color(0xFF6B7280)            // ملغي (رمادي)
            else -> Color(0xFF94A3B8)                   // لون افتراضي
        }
    }

    /**
     * إرجاع النص العربي المناسب لحالة الحجز.
     */
    fun getBookingStatusLabel(status: String): String {
        return when (status.uppercase().trim()) {
            "APPROVED", "ACCEPTED" -> "مقبول ومؤكد ✅"
            "PENDING" -> "قيد الانتظار ⏳"
            "IN_PROGRESS" -> "جاري التنفيذ ⚙️"
            "REJECTED" -> "مرفوض 🚫"
            "COMPLETED" -> "مكتمل بنجاح 🎉"
            "CANCELLED" -> "ملغي ❌"
            else -> status
        }
    }
}
