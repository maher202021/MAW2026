package com.serviseyem.data.models

import androidx.annotation.Keep

@Keep
enum class DiscountType {
    PERCENTAGE,
    FIXED
}

@Keep
enum class ApplicableType {
    ALL,
    CATEGORY,
    PRODUCT
}

typealias EntityType = com.serviseyem.data.EntityType

@Keep
data class Offer(
    val id: String = "",
    val entityId: String = "",                  // صاحب العرض (فني / متجر / مطعم / مركز طبي / عقار)
    val entityType: String = com.serviseyem.data.EntityType.STORE.code,
    val title: String = "",
    val description: String = "",
    val discountType: DiscountType = DiscountType.PERCENTAGE, // PERCENTAGE أو FIXED
    val discountValue: Double = 0.0,            // نسبة أو مبلغ ثابت
    val originalPrice: Double? = null,
    val finalPrice: Double? = null,
    val applicableTo: ApplicableType = ApplicableType.ALL, // ALL أو CATEGORY أو PRODUCT
    val categoryId: String? = null,
    val productIds: List<String> = emptyList(),
    val startDate: Long = 0L,
    val endDate: Long = 0L,
    val isActive: Boolean = true,
    val maxUsage: Int? = null,                 // عدد مرات الاستخدام المسموح
    val usedCount: Int = 0,
    val imageUrl: String? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = 0L
) {
    /**
     * Calculates the discounted price for a given original price if this offer applies and is active.
     */
    fun calculatePrice(price: Double): Double {
        if (!isActive) return price
        val now = System.currentTimeMillis()
        if (startDate > 0 && now < startDate) return price
        if (endDate > 0 && now > endDate) return price
        
        return when (discountType) {
            DiscountType.PERCENTAGE -> {
                val clampedPercent = discountValue.coerceIn(0.0, 100.0)
                price * (1.0 - (clampedPercent / 100.0))
            }
            DiscountType.FIXED -> {
                maxOf(0.0, price - discountValue)
            }
        }
    }

    /**
     * Helper to get human-readable remaining time string (e.g. "متبقي 3 أيام" or "عرض ساري")
     */
    fun getRemainingTimeString(): String {
        if (endDate <= 0) return "عرض دائم"
        val diff = endDate - System.currentTimeMillis()
        if (diff <= 0) return "منتهي الصلاحية"
        val hours = diff / (1000 * 60 * 60)
        val days = hours / 24
        return when {
            days > 0 -> "متبقي $days يوم"
            hours > 0 -> "متبقي $hours ساعة"
            else -> "ينتهي قريباً"
        }
    }
}
