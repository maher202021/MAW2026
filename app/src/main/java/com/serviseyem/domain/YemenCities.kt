package com.serviseyem.domain

object YemenCities {
    val defaultCity: String = "صنعاء"

    val list: List<String> = listOf(
        "صنعاء",
        "أمانة العاصمة",
        "عدن",
        "تعز",
        "الحديدة",
        "إب",
        "حضرموت",
        "مأرب",
        "ذمار",
        "صعدة",
        "حجة",
        "عمران",
        "البيضاء",
        "لحج",
        "أبين",
        "الضالع",
        "شبوة",
        "المهرة",
        "سقطرى",
        "ريمة",
        "الجوف",
        "المحويت"
    )

    val MAIN_CITIES: List<String> = list

    val listWithAll: List<String> = listOf("الكل") + list
}
