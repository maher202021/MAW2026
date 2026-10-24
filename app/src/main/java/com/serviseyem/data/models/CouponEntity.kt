package com.serviseyem.data

import androidx.annotation.Keep

@Keep
data class CouponEntity(
    val id: String = "",
    val code: String = "",
    val pointsValue: Int = 0,
    val expiryTimestamp: Long = 0L,
    val status: String = "ACTIVE",
    val discountPercentage: Int = 0, // percentage discount (e.g. 15 for 15%)
    val maxUsageCount: Int = 100,
    val usedCount: Int = 0
) {
    fun isValidCoupon(nowMillis: Long = System.currentTimeMillis()): Boolean {
        val active = status.equals("ACTIVE", ignoreCase = true)
        val notExpired = expiryTimestamp <= 0L || nowMillis <= expiryTimestamp
        val withinQuota = maxUsageCount <= 0 || usedCount < maxUsageCount
        return active && code.isNotBlank() && notExpired && withinQuota
    }
}
