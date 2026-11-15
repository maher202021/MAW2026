package com.serviseyem.data

import androidx.annotation.Keep

@Keep
data class SpecialOfferEntity(
    val id: String = java.util.UUID.randomUUID().toString(),
    val providerId: String = "",
    val title: String = "",
    val description: String = "",
    val discountPercent: Int = 0,
    val originalPrice: Double = 0.0,
    val offerPrice: Double = 0.0,
    val expiryDate: String = "",
    val couponCode: String = "",
    val isEnabled: Boolean = true
) {
    companion object {
        fun parseList(jsonStr: String): List<SpecialOfferEntity> {
            val trimmed = jsonStr.trim()
            if (trimmed.isBlank() || trimmed == "[]") return emptyList()
            if (trimmed.startsWith("[") && trimmed.endsWith("]")) {
                try {
                    val jsonArray = org.json.JSONArray(trimmed)
                    val list = mutableListOf<SpecialOfferEntity>()
                    for (i in 0 until jsonArray.length()) {
                        val obj = jsonArray.getJSONObject(i)
                        list.add(
                            SpecialOfferEntity(
                                id = obj.optString("id", java.util.UUID.randomUUID().toString()),
                                providerId = obj.optString("providerId", ""),
                                title = obj.optString("title", ""),
                                description = obj.optString("description", ""),
                                discountPercent = obj.optInt("discountPercent", 0),
                                originalPrice = obj.optDouble("originalPrice", 0.0),
                                offerPrice = obj.optDouble("offerPrice", 0.0),
                                expiryDate = obj.optString("expiryDate", ""),
                                couponCode = obj.optString("couponCode", ""),
                                isEnabled = obj.optBoolean("isEnabled", true)
                            )
                        )
                    }
                    return list
                } catch (_: Exception) {}
            }
            return try {
                trimmed.split(";;;").filter { it.isNotBlank() }.map { chunk ->
                    val p = chunk.split("|||")
                    SpecialOfferEntity(
                        id = p.getOrElse(0) { "" },
                        title = p.getOrElse(1) { "" },
                        description = p.getOrElse(2) { "" },
                        discountPercent = p.getOrElse(3) { "0" }.toIntOrNull() ?: 0,
                        originalPrice = p.getOrElse(4) { "0" }.toDoubleOrNull() ?: 0.0,
                        offerPrice = p.getOrElse(5) { "0" }.toDoubleOrNull() ?: 0.0,
                        expiryDate = p.getOrElse(6) { "" },
                        isEnabled = p.getOrElse(7) { "true" }.toBoolean(),
                        providerId = p.getOrElse(8) { "" },
                        couponCode = p.getOrElse(9) { "" }
                    )
                }
            } catch (_: Exception) {
                emptyList()
            }
        }

        fun serializeList(list: List<SpecialOfferEntity>): String {
            return try {
                val jsonArray = org.json.JSONArray()
                for (offer in list) {
                    val obj = org.json.JSONObject()
                    obj.put("id", offer.id)
                    obj.put("providerId", offer.providerId)
                    obj.put("title", offer.title)
                    obj.put("description", offer.description)
                    obj.put("discountPercent", offer.discountPercent)
                    obj.put("originalPrice", offer.originalPrice)
                    obj.put("offerPrice", offer.offerPrice)
                    obj.put("expiryDate", offer.expiryDate)
                    obj.put("couponCode", offer.couponCode)
                    obj.put("isEnabled", offer.isEnabled)
                    jsonArray.put(obj)
                }
                jsonArray.toString()
            } catch (_: Exception) {
                list.joinToString(";;;") { offer ->
                    listOf(
                        offer.id,
                        offer.title,
                        offer.description,
                        offer.discountPercent.toString(),
                        offer.originalPrice.toString(),
                        offer.offerPrice.toString(),
                        offer.expiryDate,
                        offer.isEnabled.toString(),
                        offer.providerId,
                        offer.couponCode
                    ).joinToString("|||")
                }
            }
        }
    }
}
