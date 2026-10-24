package com.serviseyem.data

import androidx.annotation.Keep

/**
 * 🏛️ Unified Sealed Class for all system Entity Types (replaces duplicate enum in Offer.kt).
 */
@Keep
sealed class EntityType(val code: String, val labelArabic: String) {
    object TECHNICIAN : EntityType("TECHNICIAN", "فني")
    object STORE : EntityType("STORE", "متجر")
    object PROPERTY : EntityType("PROPERTY", "عقار")
    object REAL_ESTATE : EntityType("REAL_ESTATE", "عقارات")
    object RESTAURANT : EntityType("RESTAURANT", "مطعم")
    object MEDICAL : EntityType("MEDICAL", "مركز طبي")
    object JOB : EntityType("JOB", "وظيفة")
    object JOB_POSTER : EntityType("JOB_POSTER", "معلن وظائف")

    companion object {
        fun fromString(value: String): EntityType {
            return when (value.trim().uppercase()) {
                "TECHNICIAN", "PROVIDER" -> TECHNICIAN
                "STORE" -> STORE
                "PROPERTY" -> PROPERTY
                "REAL_ESTATE" -> REAL_ESTATE
                "RESTAURANT" -> RESTAURANT
                "MEDICAL" -> MEDICAL
                "JOB" -> JOB
                "JOB_POSTER" -> JOB_POSTER
                else -> STORE
            }
        }
    }
}
