package com.serviseyem.data.models

import androidx.annotation.Keep

@Keep
data class FcmTokenEntity(
    val token: String = "",
    val phone: String = "",
    val role: String = "CLIENT",
    val updatedAt: Long = System.currentTimeMillis()
)
