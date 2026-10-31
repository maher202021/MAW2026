// LEGACY – لم يعد مستخدماً بعد التحويل إلى Native Canvas
package com.serviseyem.ui.screens.map.utils

import com.serviseyem.data.PropertyEntity
import com.serviseyem.ui.*
import com.serviseyem.data.ProviderEntity
import com.serviseyem.data.StoreEntity
import com.serviseyem.utils.getPropertyCoords
import com.serviseyem.utils.getProviderCoords
import com.serviseyem.utils.getStoreCoords
import com.serviseyem.ui.screens.home.extensions.*
import org.json.JSONArray
import org.json.JSONObject

/**
 * 🗺️ MapJsonBuilder // [إصلاح نهائي]
 * Builds structured JSON strings for Leaflet WebView markers.
 */
object MapJsonBuilder {

    fun buildMarkersJson(
        nearbyProviders: List<ProviderEntity>,
        nearbyStores: List<StoreEntity>,
        nearbyProperties: List<PropertyEntity>,
        dynamicOffsets: Map<String, Pair<Double, Double>>,
        safeUserLat: Double,
        safeUserLng: Double
    ): String {
        return buildMarkersJsonArray(
            nearbyProviders,
            nearbyStores,
            nearbyProperties,
            dynamicOffsets,
            safeUserLat,
            safeUserLng
        )
    }

    fun buildMarkersJsonArray(
        nearbyProviders: List<ProviderEntity>,
        nearbyStores: List<StoreEntity>,
        nearbyProperties: List<PropertyEntity>,
        dynamicOffsets: Map<String, Pair<Double, Double>>,
        safeUserLat: Double,
        safeUserLng: Double
    ): String {
        val jsonArray = JSONArray()

        // 1. Providers
        nearbyProviders.forEach { p ->
            val baseCoords = getProviderCoords(p)
            val rawLat = baseCoords.first
            val rawLng = baseCoords.second
            val isApproximate = p.latitude == 0.0 || p.longitude == 0.0

            val categoryEmoji = when {
                p.categoryId.contains("spaka") || p.profession.contains("سباك") -> "🔧"
                p.categoryId.contains("kahraba") || p.profession.contains("كهربا") -> "⚡"
                p.categoryId.contains("solar") || p.profession.contains("طاقة") -> "☀️"
                p.categoryId.contains("dehan") || p.profession.contains("دهان") -> "🎨"
                p.categoryId.contains("hadada") || p.profession.contains("حداد") -> "🔨"
                p.categoryId.contains("ac") || p.categoryId.contains("tabreed") || p.profession.contains("تكييف") -> "❄️"
                p.categoryId.contains("car") || p.categoryId.contains("mechanic") || p.profession.contains("ميكانيك") -> "🚗"
                p.categoryId.contains("carpentry") || p.categoryId.contains("najjara") || p.profession.contains("نجار") -> "🪚"
                else -> "👷"
            }

            val pSpec = p.customCategoryName.ifEmpty { p.specialization.ifEmpty { p.profession } }
            val cleanSpec = if (pSpec.isBlank()) "فني صيانة معتمد" else pSpec

            val obj = JSONObject().apply {
                put("id", p.id)
                put("type", "PROVIDER")
                put("name", p.name.ifBlank { "فني دليل اليمن" })
                put("lat", rawLat)
                put("lng", rawLng)
                put("isApprox", isApproximate)
                put("emoji", categoryEmoji)
                put("spec", cleanSpec)
                put("phone", p.phone)
                put("rating", if (p.rating > 0) String.format(java.util.Locale.US, "%.1f", p.rating) else "5.0")
                put("status", if (p.isAvailable) "متاح لاستقبال الطلبات 🟢" else "مشغول حالياً 🟡")
                put("badgeColor", "#2563EB") // أزرق للفنيين
                put("serviceCategory", "فنيون صيانة")
            }
            jsonArray.put(obj)
        }

        // 2. Stores & Medical Centers & Restaurants
        nearbyStores.forEach { s ->
            val coords = getStoreCoords(s)
            val isMedical = s.isMedicalCenter()
            val isRestaurant = s.isRestaurantOrCafe()

            val emoji = when {
                isMedical -> "🏥"
                isRestaurant -> "🍔"
                s.sectionId.contains("supermarket") || s.name.contains("بقالة") || s.name.contains("هايبر") -> "🛒"
                else -> "🏪"
            }

            val rawLat = coords.first
            val rawLng = coords.second
            val isApproximate = s.latitude == 0.0 || s.longitude == 0.0

            val (badgeColor, categoryLabel) = when {
                isMedical -> Pair("#EF4444", "مراكز طبية وصيدليات") // أحمر للمراكز الطبية
                isRestaurant -> Pair("#F59E0B", "مطاعم وكافيهات") // برتقالي للمطاعم
                else -> Pair("#10B981", "متاجر وأسواق") // أخضر للمتاجر
            }

            val cleanDesc = s.description.ifBlank { if (isMedical) "مركز طبي / صيدلية معتمدة" else "محل / متجر معتمد" }.take(60)

            val obj = JSONObject().apply {
                put("id", s.id)
                put("type", "STORE")
                put("name", s.name.ifBlank { "متجر معتمد" })
                put("lat", rawLat)
                put("lng", rawLng)
                put("isApprox", isApproximate)
                put("emoji", emoji)
                put("spec", cleanDesc)
                put("phone", s.phone)
                put("rating", if (s.rating > 0) String.format(java.util.Locale.US, "%.1f", s.rating) else "5.0")
                put("status", if (s.isActive) "مفتوح ويستقبل الطلبات 🟢" else "مغلق حالياً 🔴")
                put("badgeColor", badgeColor)
                put("serviceCategory", categoryLabel)
            }
            jsonArray.put(obj)
        }

        // 3. Properties
        nearbyProperties.forEach { pr ->
            val coords = getPropertyCoords(pr)
            val rawLat = coords.first
            val rawLng = coords.second
            val isApproximate = pr.latitude == 0.0 || pr.longitude == 0.0

            val priceFormatted = if (pr.price > 0) "${pr.price} ${pr.currency.ifEmpty { "ريال" }}" else "حسب الاتفاق"
            val cleanDesc = pr.description.ifBlank { "عقار معروض" }.take(60)

            val obj = JSONObject().apply {
                put("id", pr.id)
                put("type", "PROPERTY")
                put("name", pr.title.ifBlank { "عقار معروض" })
                put("lat", rawLat)
                put("lng", rawLng)
                put("isApprox", isApproximate)
                put("emoji", "🏠")
                put("spec", "$cleanDesc ($priceFormatted)")
                put("phone", pr.phone)
                put("rating", "5.0")
                put("status", "معروض للإيجار / البيع 🟢")
                put("badgeColor", "#8B5CF6")
                put("serviceCategory", "عقارات وشقق")
            }
            jsonArray.put(obj)
        }

        android.util.Log.d("MapJsonBuilder", "Built ${jsonArray.length()} markers for Leaflet Map (Providers: ${nearbyProviders.size}, Stores: ${nearbyStores.size}, Properties: ${nearbyProperties.size})")
        return jsonArray.toString()
    }
}
