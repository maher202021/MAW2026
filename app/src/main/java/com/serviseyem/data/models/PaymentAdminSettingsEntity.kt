package com.serviseyem.data

import androidx.annotation.Keep

@Keep
data class PaymentAdminSettingsEntity(
    val id: String = "main_payment_config",
    val isPaymentSystemEnabled: Boolean = true,
    val linkBookings: Boolean = true,
    val linkStores: Boolean = true,
    val linkRestaurants: Boolean = true,
    val linkMedical: Boolean = true,
    val linkProperties: Boolean = true,
    val linkJobs: Boolean = true
) {
    fun isSectionPaymentEnabled(section: String): Boolean {
        if (!isPaymentSystemEnabled) return false
        return when (section.trim().uppercase()) {
            "BOOKING", "BOOKINGS", "PROVIDER", "TECHNICIAN" -> linkBookings
            "STORE", "STORES", "STORE_OWNER" -> linkStores
            "RESTAURANT", "RESTAURANTS", "RESTAURANT_OWNER" -> linkRestaurants
            "MEDICAL", "MEDICAL_CENTER" -> linkMedical
            "PROPERTY", "PROPERTIES", "REAL_ESTATE" -> linkProperties
            "JOB", "JOBS", "JOB_POSTER" -> linkJobs
            else -> isPaymentSystemEnabled
        }
    }
}
