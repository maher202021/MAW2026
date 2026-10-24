package com.serviseyem.data

import androidx.annotation.Keep

@Keep
data class JobEntity(
    val id: String = "",
    val sectionId: String = "jobs",
    val title: String = "", // المسمى الوظيفي
    val companyName: String = "", // اسم الشركة أو الجهة
    val managerName: String = "", // اسم المسؤول
    val phone: String = "", // رقم الهاتف والواتساب
    val cityId: String = "", // المحافظة / المدينة
    val address: String = "", // الحي والشارع
    val jobType: String = "دوام كامل", // دوام كامل، دوام جزئي، عن بعد، بالساعة
    val salary: String = "", // الراتب المتوقع
    val description: String = "", // التفاصيل
    val requirements: String = "", // الشروط والمؤهلات
    val isApproved: Boolean = false,
    val isActive: Boolean = true,
    val isDeleted: Boolean = false,
    val createdAt: Long = System.currentTimeMillis(),
    val isPinned: Boolean = false,
    val isVip: Boolean = false,
    val isChatDisabled: Boolean = false,
    val isBlocked: Boolean = false,
    val blockReason: String = ""
)

@Keep
data class JobApplicationEntity(
    val id: String = "",
    val jobId: String = "",
    val jobTitle: String = "",
    val companyName: String = "",
    val applicantName: String = "",
    val applicantPhone: String = "",
    val applicantQuals: String = "",
    // TODO: Migrate to FirebaseStorageUploader
    val cvBase64: String = "",
    val status: String = "PENDING",
    val rejectionReason: String = "",
    val createdAt: Long = System.currentTimeMillis()
)
