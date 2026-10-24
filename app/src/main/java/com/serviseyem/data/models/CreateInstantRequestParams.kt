package com.serviseyem.data.models

import androidx.annotation.Keep

/**
 * ⚡ CreateInstantRequestParams
 * كائن بيانات موحد لإنشاء وتمرير معطيات الطلبات الفورية والعاجلة لتقليل عدد المعاملات.
 */
@Keep
data class CreateInstantRequestParams(
    val userId: String = "",
    val userName: String = "",
    val userPhone: String = "",
    val userCity: String = "",
    val userNeighborhood: String = "",
    val categoryId: String = "services",
    val categoryName: String = "",
    val serviceTitle: String = "",
    val description: String = "",
    val images: List<String> = emptyList(),
    val urgencyTime: String = "فوراً (خلال 30 دقيقة)",
    val deliveryMethod: String = "",
    val customPin: String = ""
)
