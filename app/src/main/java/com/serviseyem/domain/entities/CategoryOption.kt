package com.serviseyem.domain.entities

import androidx.annotation.Keep

/**
 * 🏷️ CategoryOption
 * خيار بسيط للاختيار من قوائم التصنيفات
 */
@Keep
data class CategoryOption(
    val id: String = "",
    val label: String = ""
)
