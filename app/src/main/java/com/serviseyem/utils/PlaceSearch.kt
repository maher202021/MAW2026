package com.serviseyem.utils

import android.content.Context
import com.serviseyem.data.ProviderEntity
import com.serviseyem.data.StoreEntity
import com.serviseyem.navigation.RealValhallaNavigator
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
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
        "${ArabicNormalizer.normalize(r.shortName)}_${String.format(Locale.US, "%.3f_%.3f", r.latitude, r.longitude)}"

    /** بحث هجين متوازٍ: كيانات التطبيق + معالم اليمن المدمجة + قاعدة الأماكن المحلية + خرائط OSM الحية */
    suspend fun search(
        context: Context,
        query: String,
        providers: List<ProviderEntity>,
        stores: List<StoreEntity>,
        userLat: Double,
        userLng: Double
    ): PlaceSearchOutcome = coroutineScope {
        val q = query.trim()
        if (q.length < 2) return@coroutineScope PlaceSearchOutcome.Results(emptyList(), "none")
        val merged = LinkedHashMap<String, RealValhallaNavigator.SearchAddressResult>()
        fun add(list: List<RealValhallaNavigator.SearchAddressResult>) =
            list.forEach { merged.putIfAbsent(key(it), it) }

        val dbInstalled = OfflinePlacesDb.isInstalled(context)
        val online = NetworkUtils.hasInternetTransport(context)

        val localTask = async(Dispatchers.Default) {
            val out = mutableListOf<RealValhallaNavigator.SearchAddressResult>()
            val exp = DialectSynonyms.expand(q).map { ArabicNormalizer.normalizeLoose(it) }.filter { it.isNotEmpty() }
            val queryTokens = ArabicNormalizer.normalizeLoose(q).split(' ').filter { it.length >= 2 }
            // [نظام خرائط وملاحة عالمي - ملكنا]
            fun hit(text: String): Boolean {
                val t = ArabicNormalizer.normalizeLoose(text)
                return exp.any { t.contains(it) } ||
                    (queryTokens.isNotEmpty() && queryTokens.all { t.contains(it) })
            }
            providers.filter {
                it.latitude != 0.0 && it.longitude != 0.0 &&
                    hit("${it.name} ${it.area} ${it.localNeighborhood} ${it.profession} ${it.specialization} ${it.customCategoryName}")
            }.take(8).forEach {
                out.add(
                    RealValhallaNavigator.SearchAddressResult(
                        displayName = "${it.name} - ${it.area.ifBlank { it.localNeighborhood }}",
                        shortName = it.name,
                        latitude = it.latitude,
                        longitude = it.longitude,
                        category = "provider",
                        icon = "👷"
                    )
                )
            }
            stores.filter {
                it.latitude != 0.0 && it.longitude != 0.0 &&
                    hit("${it.name} ${it.localNeighborhood} ${it.description} ${it.categoryId} ${it.sectionId}")
            }.take(8).forEach {
                out.add(
                    RealValhallaNavigator.SearchAddressResult(
                        displayName = if (it.localNeighborhood.isNotBlank()) "${it.name} - ${it.localNeighborhood}" else it.name,
                        shortName = it.name,
                        latitude = it.latitude,
                        longitude = it.longitude,
                        category = "store",
                        icon = "🏪"
                    )
                )
            }
            CrossFuzzySearchEngine.searchEverything(
                query = q,
                providers = emptyList(),
                stores = emptyList(),
                properties = emptyList(),
                userLat = userLat,
                userLng = userLng,
                context = context
            ).take(12).forEach { res ->
                val icon = when (res.type) {
                    CrossFuzzySearchEngine.SearchType.CITY -> "🏙️"
                    CrossFuzzySearchEngine.SearchType.AREA -> "📍"
                    CrossFuzzySearchEngine.SearchType.STREET -> "🛣️"
                    else -> res.subtitle.take(2).ifBlank { "📌" }
                }
                out.add(
                    RealValhallaNavigator.SearchAddressResult(
                        displayName = "${res.title} — ${res.subtitle}",
                        shortName = res.title,
                        latitude = res.latitude,
                        longitude = res.longitude,
                        category = res.type.name.lowercase(Locale.US),
                        icon = icon
                    )
                )
            }
            out
        }

        val dbTask = async(Dispatchers.IO) {
            if (dbInstalled) OfflinePlacesDb.search(context, q, userLat, userLng, 25) else emptyList()
        }

        val onlineTask = async(Dispatchers.IO) {
            if (online) RealValhallaNavigator.searchAddressAndPlaces(q, emptyList(), emptyList(), userLat, userLng) else emptyList()
        }

        add(localTask.await())
        add(dbTask.await())
        add(onlineTask.await())

        when {
            merged.isNotEmpty() -> PlaceSearchOutcome.Results(merged.values.toList(), if (online) "online" else "offline")
            !online && !dbInstalled -> PlaceSearchOutcome.Message(
                "لا نتائج محلية مطابقة لـ «$q». يمكنك استيراد قاعدة الأماكن الكاملة أو تفعيل الإنترنت للبحث في خرائط OSM الحية.",
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
