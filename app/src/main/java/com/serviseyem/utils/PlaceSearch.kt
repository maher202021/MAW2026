package com.serviseyem.utils

import android.content.Context
import com.serviseyem.data.ProviderEntity
import com.serviseyem.data.StoreEntity
import com.serviseyem.navigation.RealValhallaNavigator
import java.util.Locale

sealed class PlaceSearchOutcome {
    data class Results(
        val items: List<RealValhallaNavigator.SearchAddressResult>,
        val source: String
    ) : PlaceSearchOutcome()

    data class Message(
        val text: String,
        val canDownloadPlaces: Boolean
    ) : PlaceSearchOutcome()
}

object PlaceSearch {
    private fun key(r: RealValhallaNavigator.SearchAddressResult) =
        String.format(Locale.US, "%.4f_%.4f", r.latitude, r.longitude)

    /** ترتيب المصادر: كيانات التطبيق ← قاعدة الأماكن المحلية ← Nominatim (عند وجود إنترنت). */
    suspend fun search(
        context: Context,
        query: String,
        providers: List<ProviderEntity>,
        stores: List<StoreEntity>,
        userLat: Double,
        userLng: Double
    ): PlaceSearchOutcome {
        val q = query.trim()
        if (q.length < 2) return PlaceSearchOutcome.Results(emptyList(), "none")
        val merged = LinkedHashMap<String, RealValhallaNavigator.SearchAddressResult>()
        fun add(list: List<RealValhallaNavigator.SearchAddressResult>) =
            list.forEach { merged.putIfAbsent(key(it), it) }

        // 1) فنيون ومتاجر (محلي)
        val exp = DialectSynonyms.expand(q)
        fun hit(text: String): Boolean {
            val t = ArabicNormalizer.normalize(text)
            return exp.any { it.isNotEmpty() && t.contains(it) }
        }
        add(
            providers.filter { it.latitude != 0.0 && it.longitude != 0.0 && (hit(it.name) || hit(it.area)) }
                .take(6)
                .map {
                    RealValhallaNavigator.SearchAddressResult(
                        displayName = "${it.name} - ${it.area}",
                        shortName = it.name,
                        latitude = it.latitude,
                        longitude = it.longitude,
                        category = "provider",
                        icon = "👷"
                    )
                }
        )
        add(
            stores.filter { it.latitude != 0.0 && it.longitude != 0.0 && hit(it.name) }
                .take(6)
                .map {
                    RealValhallaNavigator.SearchAddressResult(
                        displayName = it.name,
                        shortName = it.name,
                        latitude = it.latitude,
                        longitude = it.longitude,
                        category = "store",
                        icon = "🏪"
                    )
                }
        )

        // 2) قاعدة الأماكن المحلية (شوارع/أحياء/مدن/معالم)
        val dbInstalled = OfflinePlacesDb.isInstalled(context)
        if (dbInstalled) add(OfflinePlacesDb.search(context, q, userLat, userLng, 20))

        // 3) الإنترنت
        val online = NetworkUtils.hasInternetTransport(context)
        if (online) add(RealValhallaNavigator.searchAddressAndPlaces(q, emptyList(), emptyList(), userLat, userLng))

        return when {
            merged.isNotEmpty() -> PlaceSearchOutcome.Results(merged.values.toList(), if (online) "online" else "offline")
            !online && !dbInstalled -> PlaceSearchOutcome.Message(
                "لا يوجد بحث عن الشوارع والأحياء بلا إنترنت حتى تحمّل «قاعدة الأماكن» (نحو ${NavConfig.PLACES_DB_APPROX_MB} ميجا). " +
                    "يمكنك الآن اختيار الوجهة من الخريطة أو من المدن أو من عناوينك المحفوظة.",
                canDownloadPlaces = true
            )
            !online -> PlaceSearchOutcome.Message(
                "لا نتائج في قاعدة الأماكن المحلية لـ «$q». جرّب كتابة أخرى أو اختر من الخريطة.",
                canDownloadPlaces = false
            )
            else -> PlaceSearchOutcome.Message(
                RealValhallaNavigator.lastSearchError ?: "لا توجد نتائج لـ «$q»",
                canDownloadPlaces = false
            )
        }
    }
}
