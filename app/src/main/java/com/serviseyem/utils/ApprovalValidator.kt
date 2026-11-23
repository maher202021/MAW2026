package com.serviseyem.utils

/**
 * ApprovalValidator
 * يتحقق من اكتمال البيانات الأساسية والموقع الجغرافي قبل اعتماد طلبات الانضمام
 * حتى لا تُنشأ كيانات بإحداثيات (0.0, 0.0) أو بدون مدينة صالحة.
 */
object ApprovalValidator {
    data class Problem(
        val field: String,
        val message: String
    )

    fun validate(
        lat: Double?,
        lng: Double?,
        cityId: String?,
        name: String?,
        phone: String?,
        requireLocation: Boolean = true
    ): List<Problem> = buildList {
        if (requireLocation && (lat == null || lng == null || !LandmarkAddress.isValid(lat, lng))) {
            add(Problem("location", "الموقع الجغرافي غير محدد؛ حدّده من الخريطة قبل الاعتماد"))
        }
        if (cityId.isNullOrBlank()) {
            add(Problem("city", "المدينة / المحافظة غير محددة أو غير معروفة"))
        }
        if (name.isNullOrBlank()) {
            add(Problem("name", "الاسم مطلوب"))
        }
        if (phone.isNullOrBlank()) {
            add(Problem("phone", "رقم الهاتف مطلوب"))
        }
    }
}
