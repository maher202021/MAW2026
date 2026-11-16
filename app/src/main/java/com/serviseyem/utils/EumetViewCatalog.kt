package com.serviseyem.utils

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.net.HttpURLConnection
import java.net.URL

data class WmsLayerInfo(val name: String, val title: String) {
    /** اسم عربي مبسّط للعرض. */
    fun arabicLabel(): String {
        val t = title.lowercase()
        return when {
            "dust" in t -> "الغبار (Meteosat)"
            "natural" in t -> "ألوان طبيعية (Meteosat)"
            "cloud mask" in t || "clm" in t -> "قناع السحب (Meteosat)"
            "airmass" in t -> "كتل الهواء (Meteosat)"
            "10.8" in t || "infrared" in t || "ir " in t -> "أشعة تحت حمراء (Meteosat)"
            "convection" in t -> "عواصف وحمل حراري (Meteosat)"
            "fog" in t -> "الضباب وسحب منخفضة (Meteosat)"
            "fire" in t -> "حرائق نشطة (Meteosat)"
            else -> title.take(40)
        }
    }
}

object EumetViewCatalog {
    private const val CAPS = "https://view.eumetsat.int/geoserver/ows?service=WMS&version=1.3.0&request=GetCapabilities"
    private val NAME_TITLE = Regex("<Name>([^<]{3,80})</Name>\\s*<Title>([^<]{1,200})</Title>", RegexOption.DOT_MATCHES_ALL)
    private val SAFE_NAME = Regex("^[A-Za-z0-9_:.\\-]{3,80}$")
    @Volatile private var cache: Pair<Long, List<WmsLayerInfo>>? = null

    fun isSafe(name: String) = SAFE_NAME.matches(name)

    /** طبقات المحيط الهندي (IODC). كاش 24 ساعة. null عند الفشل. */
    suspend fun iodcLayers(): List<WmsLayerInfo>? = withContext(Dispatchers.IO) {
        cache?.let { (t, v) -> if (System.currentTimeMillis() - t < 24 * 3_600_000L) return@withContext v }
        var c: HttpURLConnection? = null
        try {
            c = (URL(CAPS).openConnection() as HttpURLConnection).apply {
                connectTimeout = 10_000
                readTimeout = 25_000
                setRequestProperty("User-Agent", "AlYemenServicesApp/1.0")
            }
            if (c.responseCode != 200) return@withContext null
            val xml = c.inputStream.bufferedReader().use { it.readText() }
            val found = NAME_TITLE.findAll(xml)
                .map { WmsLayerInfo(it.groupValues[1].trim(), it.groupValues[2].trim()) }
                .filter { it.name.contains(':') && isSafe(it.name) &&
                    (it.title.contains("IODC", true) || it.title.contains("Indian Ocean", true) || it.title.contains("41.5")) }
                .distinctBy { it.name }.take(25).toList()
            if (found.isNotEmpty()) cache = System.currentTimeMillis() to found
            found.ifEmpty { null }
        } catch (e: Exception) { null } finally { c?.disconnect() }
    }
}
