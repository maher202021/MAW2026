package com.serviseyem.data

import androidx.annotation.Keep

@Keep
data class BannerEntity(
    val id: String = "",
    val title: String = "",
    val url: String = "",
    val redirectCategory: String = "",
    val type: String = "",
    val size: String = "",
    val duration: Int = 5,
    val displayTime: String = "طوال اليوم",
    val order: Int = 0,
    val targetSection: String = "ALL" // ALL, HOME, STORES, RESTAURANTS, MEDICAL, PROPERTIES, JOBS
)
