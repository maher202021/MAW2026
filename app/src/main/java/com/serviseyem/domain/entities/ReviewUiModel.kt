package com.serviseyem.domain.entities

import androidx.annotation.Keep
import java.util.UUID

/**
 * ⭐ ReviewUiModel
 * تم نقلها لتكون متاحة بشكل عام لنظام التقييمات
 */
@Keep
data class ReviewUiModel(
    val id: String = UUID.randomUUID().toString(),
    val authorName: String = "",
    val rating: Int = 5,
    val comment: String = "",
    val replyText: String = ""
)
