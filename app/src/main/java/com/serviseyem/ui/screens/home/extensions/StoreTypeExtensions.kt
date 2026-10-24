package com.serviseyem.ui.screens.home.extensions

import com.serviseyem.data.StoreEntity
import com.serviseyem.ui.*

/**
 * 🏷️ Classifications helpers to ensure absolute separation between categories
 */
fun StoreEntity.isRestaurantOrCafe(): Boolean {
    val lowerName = name.lowercase()
    val lowerSec = sectionId.lowercase()
    val lowerCat = categoryId.lowercase()
    val lowerType = providerType.lowercase()

    if (lowerSec in listOf("restaurants", "restaurant", "food", "cafe", "cafes", "dining", "sweets", "bakery")) return true
    if (lowerType in listOf("restaurant", "cafe", "food", "dining")) return true
    if (lowerCat in listOf("restaurants", "restaurant", "cafe", "food", "dining", "sweets", "bakery")) return true

    val foodKeywords = listOf(
        "مطعم", "مطاعم", "كافيه", "كافية", "مقهى", "بوفيه", "وجبات", "مشويات", "شاورما",
        "بيتزا", "حلويات", "مخبز", "مخابز", "عصائر", "عصير", "دجاج", "حنيذ", "زربيان",
        "شبيات", "برجر", "قهوة", "مأكولات", "سندوتشات", "اكلات", "أكلات", "بحرية", "بحريات"
    )
    return foodKeywords.any { lowerName.contains(it) }
}

fun StoreEntity.isMedicalCenter(): Boolean {
    val lowerName = name.lowercase()
    val lowerSec = sectionId.lowercase()
    val lowerCat = categoryId.lowercase()
    val lowerType = providerType.lowercase()

    if (medicalLicenseNo.isNotBlank()) return true
    if (lowerSec in listOf("medical", "clinics", "clinic", "hospital", "hospitals", "pharmacy", "pharmacies", "lab", "labs")) return true
    if (lowerType in listOf("medical", "clinic", "hospital", "doctor", "pharmacy", "lab")) return true
    if (lowerCat in listOf("medical", "clinics", "hospitals", "pharmacies", "labs", "dental")) return true

    val medicalKeywords = listOf(
        "مستشفى", "مستشفيات", "عياده", "عيادة", "عيادات", "مركز طبي", "مجمع طبي",
        "طبي", "طبيب", "صيدلية", "صيدليه", "مختبر", "مختبرات", "بصريات", "أسنان",
        "اسنان", "علاج", "دكتور", "صيدليات", "تشخيص"
    )
    return medicalKeywords.any { lowerName.contains(it) }
}

fun StoreEntity.isCommercialStore(): Boolean {
    return !isMedicalCenter() && !isRestaurantOrCafe()
}
