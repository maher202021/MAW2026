package com.serviseyem.data

import androidx.annotation.Keep

@Keep
data class CityEntity(
    val id: String = "",
    val nameAr: String = "",
    val nameEn: String = "",
    val icon: String = "📍",
    val photoUrl: String = "",
    val sortOrder: Int = 0
)
