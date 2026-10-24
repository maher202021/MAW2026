package com.serviseyem.domain.entities

import androidx.annotation.Keep

/**
 * 🩺 DoctorItem
 * تم نقلها من MedicalDashboardViewModel لتكون متاحة للجميع
 */
@Keep
data class DoctorItem(
    val id: String = "",
    val name: String = "",
    val specialty: String = "",
    val workingHours: String = ""
)
