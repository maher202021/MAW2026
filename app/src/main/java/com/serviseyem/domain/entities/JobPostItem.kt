package com.serviseyem.domain.entities

import androidx.annotation.Keep

/**
 * 💼 JobPostItem
 * تم نقلها من JobPosterDashboardViewModel لتكون متاحة للجميع
 */
@Keep
data class JobPostItem(
    val id: String = "",
    val title: String = "",
    val companyName: String = "",
    val salary: String = "",
    val requirements: String = "",
    val applicantsCount: Int = 0
)
