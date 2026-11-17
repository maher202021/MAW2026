package com.serviseyem.utils

fun getStarsString(r: Float): String {
    val filled = r.toInt().coerceIn(0, 5)
    val empty = (5 - filled).coerceIn(0, 5)
    return "★".repeat(filled) + "☆".repeat(empty)
}
