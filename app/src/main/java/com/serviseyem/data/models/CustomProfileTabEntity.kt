package com.serviseyem.data

import androidx.annotation.Keep

@Keep
data class CustomProfileTabEntity(
    val id: String = "",
    val title: String = "",
    val icon: String = "📑",
    val targetType: String = "ALL", // "ALL", "PROVIDERS", "STORES", "PROPERTIES"
    val contentHtmlOrText: String = "",
    val isEnabled: Boolean = true,
    val displayOrder: Int = 0
)

@Keep
data class ColorPaletteEntity(
    val id: String = "",
    val name: String = "",
    val primaryHex: String = "#059669",
    val secondaryHex: String = "#115E59",
    val backgroundHex: String = "#0A0F0D",
    val surfaceHex: String = "#121D18"
)
