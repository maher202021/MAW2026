package com.serviseyem.utils

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.webkit.WebResourceRequest
import android.webkit.WebResourceResponse
import org.json.JSONObject
import java.io.ByteArrayInputStream
import java.io.File
import java.io.FileOutputStream
import java.net.HttpURLConnection
import java.net.URL
import java.security.MessageDigest
import kotlin.math.PI
import kotlin.math.atan
import kotlin.math.sinh

data class RegionInfo(
    val id: String,
    val name: String,
    val minZ: Int,
    val maxZ: Int,
    val minLat: Double,
    val maxLat: Double,
    val minLon: Double,
    val maxLon: Double,
    val filePath: String,
    val format: String = "png"
)

// [نظام خرائط وملاحة عالمي - ملكنا]
object BundledPacks {
    private const val BUNDLED_ID = "yemen_low"
    private const val ASSET_NAME = "base_lowzoom.mbtiles"
    private const val BUNDLED_VERSION = 1

    fun installIfNeeded(context: Context) {
        try {
            val appCtx = context.applicationContext
            val targetDb = RegionPackManager.mbtilesFile(appCtx, BUNDLED_ID)
            val marker = RegionPackManager.markerFile(appCtx, BUNDLED_ID)
            if (targetDb.exists() && targetDb.length() > 10_000L && marker.exists()) {
                val obj = runCatching { JSONObject(marker.readText()) }.getOrNull()
                if (obj != null && obj.optInt("version", 0) >= BUNDLED_VERSION) {
                    return
                }
            }

            val assetList = appCtx.assets.list("") ?: return
            if (ASSET_NAME !in assetList) return

            val tmpFile = File(RegionPackManager.dir(appCtx), "$BUNDLED_ID.tmp")
            appCtx.assets.open(ASSET_NAME).use { input ->
                FileOutputStream(tmpFile).use { output ->
                    val buf = ByteArray(32 * 1024)
                    while (true) {
                        val read = input.read(buf)
                        if (read <= 0) break
                        output.write(buf, 0, read)
                    }
                    output.fd.sync()
                }
            }

            if (RegionPackManager.looksLikeMbtiles(tmpFile)) {
                if (targetDb.exists()) targetDb.delete()
                if (!tmpFile.renameTo(targetDb)) {
                    tmpFile.copyTo(targetDb, overwrite = true)
                    tmpFile.delete()
                }
                val sizeMb = (targetDb.length() / (1024L * 1024L)).coerceAtLeast(1L)
                RegionPackManager.writeMarker(
                    context = appCtx,
                    id = BUNDLED_ID,
                    name = "اليمن الأساسية (مدمجة)",
                    minZ = 0,
                    maxZ = 9,
                    minLat = 11.5,
                    maxLat = 19.2,
                    minLon = 41.0,
                    maxLon = 54.7,
                    format = "png",
                    version = BUNDLED_VERSION,
                    sizeMb = sizeMb
                )
                OfflineTileInterceptor.invalidateRegions()
            } else {
                tmpFile.delete()
            }
        } catch (_: Exception) {
        }
    }
}

// [نظام خرائط وملاحة عالمي - ملكنا]
object OfflineTileInterceptor {
    private val OSM_TILE_REGEX = Regex("^/(?:[^/]+/)*(\\d{1,2})/(\\d+)/(\\d+)(?:@\\dx)?\\.(png|jpg|jpeg|webp)$", RegexOption.IGNORE_CASE)
    private val ESRI_ZYX_REGEX = Regex("^/.*/tile/(\\d{1,2})/(\\d+)/(\\d+)$", RegexOption.IGNORE_CASE)

    const val CACHE_VERSION = 3
    private const val PREFS_NAME = "tile_cache_prefs"
    private const val KEY_CACHE_VERSION = "cache_version"
    private const val TILE_TTL_MS = 7L * 24L * 60L * 60L * 1000L

    private val CACHEABLE_HOSTS = setOf(
        "tile.openstreetmap.org",
        "a.tile.openstreetmap.org",
        "b.tile.openstreetmap.org",
        "c.tile.openstreetmap.org",
        "server.arcgisonline.com",
        "services.arcgisonline.com",
        "wayback.maptiles.arcgis.com",
        "tiles.maps.eox.at",
        "tile.opentopomap.org",
        "a.tile.opentopomap.org",
        "b.tile.opentopomap.org",
        "c.tile.opentopomap.org",
        "gibs.earthdata.nasa.gov"
    )

    @Volatile
    private var cacheMigrated = false

    @Volatile
    private var regionIndex: List<RegionInfo>? = null
    private val indexLock = Any()
    private val dbLock = Any()

    private val regionDbs = object : LinkedHashMap<String, SQLiteDatabase>(4, 0.75f, true) {
        override fun removeEldestEntry(eldest: MutableMap.MutableEntry<String, SQLiteDatabase>?): Boolean {
            if (size > 3 && eldest != null) {
                try {
                    eldest.value.close()
                } catch (_: Exception) {
                }
                return true
            }
            return false
        }
    }

    /**
     * يهاجر الكاش عند رفع CACHE_VERSION لحذف أي بلاطات تالفة أو حاملة لعلامة API KEY REQUIRED القديمة.
     * يعيد true إذا تم التنظيف لأول مرة حتى يقوم WebView بتنظيف كاشه الداخلي أيضاً.
     */
    fun migrateCacheIfNeeded(context: Context): Boolean {
        if (cacheMigrated) return false
        synchronized(this) {
            if (cacheMigrated) return false
            val appCtx = context.applicationContext
            val sp = appCtx.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            val savedVer = sp.getInt(KEY_CACHE_VERSION, 0)
            var didMigrate = false
            if (savedVer < CACHE_VERSION) {
                try {
                    tileCacheDir(appCtx).deleteRecursively()
                    File(appCtx.cacheDir, "WebView").deleteRecursively()
                    File(appCtx.cacheDir, "tiles").deleteRecursively()
                } catch (_: Exception) {
                }
                sp.edit().putInt(KEY_CACHE_VERSION, CACHE_VERSION).apply()
                didMigrate = true
            }
            cacheMigrated = true
            return didMigrate
        }
    }

    private fun tileCacheDir(context: Context): File =
        File(context.applicationContext.cacheDir, "tile_cache_v$CACHE_VERSION").apply { mkdirs() }

    private fun isCacheable(host: String, contentType: String?, bytes: ByteArray): Boolean {
        val h = host.lowercase()
        if (h.contains("carto") || h.contains("cartocdn")) return false
        if (h !in CACHEABLE_HOSTS && !CACHEABLE_HOSTS.any { h.endsWith(".$it") }) return false
        if (bytes.size < 64) return false

        val ct = contentType?.lowercase().orEmpty()
        if (ct.contains("text/") || ct.contains("json") || ct.contains("xml") || ct.contains("html")) {
            return false
        }

        val isPng = bytes.size >= 4 &&
            bytes[0] == 0x89.toByte() && bytes[1] == 0x50.toByte() &&
            bytes[2] == 0x4E.toByte() && bytes[3] == 0x47.toByte()
        val isJpg = bytes.size >= 3 &&
            bytes[0] == 0xFF.toByte() && bytes[1] == 0xD8.toByte() && bytes[2] == 0xFF.toByte()
        val isWebp = bytes.size >= 12 &&
            bytes[0] == 'R'.code.toByte() && bytes[1] == 'I'.code.toByte() &&
            bytes[2] == 'F'.code.toByte() && bytes[3] == 'F'.code.toByte() &&
            bytes[8] == 'W'.code.toByte() && bytes[9] == 'E'.code.toByte() &&
            bytes[10] == 'B'.code.toByte() && bytes[11] == 'P'.code.toByte()

        if (!isPng && !isJpg && !isWebp) return false

        // Reject any payload somehow carrying ASCII API key watermark text
        if (bytes.size < 8192) {
            val headText = String(bytes, 0, bytes.size.coerceAtMost(2048), Charsets.US_ASCII)
            if (headText.contains("API KEY REQUIRED", ignoreCase = true)) return false
        }
        return true
    }

    private fun cacheFileForUrl(context: Context, fullUrl: String): File {
        val digest = MessageDigest.getInstance("SHA-256")
            .digest(fullUrl.toByteArray(Charsets.UTF_8))
            .joinToString("") { "%02x".format(it) }
        val shard = digest.substring(0, 2)
        val dir = File(tileCacheDir(context), shard).apply { mkdirs() }
        return File(dir, "$digest.tile")
    }

    fun invalidateRegions() {
        synchronized(indexLock) {
            regionIndex = null
        }
        synchronized(dbLock) {
            regionDbs.values.forEach { db ->
                try {
                    db.close()
                } catch (_: Exception) {
                }
            }
            regionDbs.clear()
        }
    }

    fun hasAnyInstalledRegions(context: Context): Boolean {
        return getOrLoadIndex(context).isNotEmpty()
    }

    private fun getOrLoadIndex(context: Context): List<RegionInfo> {
        regionIndex?.let { return it }
        synchronized(indexLock) {
            regionIndex?.let { return it }
            val loaded = loadIndex(context)
            regionIndex = loaded
            return loaded
        }
    }

    private fun loadIndex(context: Context): List<RegionInfo> {
        val dir = RegionPackManager.dir(context)
        val files = dir.listFiles() ?: return emptyList()
        val list = mutableListOf<RegionInfo>()
        for (file in files) {
            if (!file.isFile || !file.name.endsWith(".json")) continue
            val id = file.name.removeSuffix(".json")
            if (!RegionPackManager.isValidId(id)) continue
            val dbFile = File(dir, "$id.mbtiles")
            if (!dbFile.exists() || dbFile.length() == 0L) continue
            try {
                val obj = JSONObject(file.readText())
                list.add(
                    RegionInfo(
                        id = obj.optString("id", id),
                        name = obj.optString("name", id),
                        minZ = obj.optInt("minZ", 0),
                        maxZ = obj.optInt("maxZ", 17),
                        minLat = obj.optDouble("minLat", -90.0),
                        maxLat = obj.optDouble("maxLat", 90.0),
                        minLon = obj.optDouble("minLon", -180.0),
                        maxLon = obj.optDouble("maxLon", 180.0),
                        filePath = dbFile.absolutePath,
                        format = obj.optString("format", "png")
                    )
                )
            } catch (_: Exception) {
            }
        }
        // Prefer specific regional high-zoom packs before bundled yemen_low
        return list.sortedWith(
            compareBy<RegionInfo> { it.id == "yemen_low" }
                .thenByDescending { it.minZ }
                .thenByDescending { it.maxZ }
        )
    }

    fun tileCenter(z: Int, x: Int, y: Int): Pair<Double, Double> {
        val n = (1 shl z.coerceIn(0, 22)).toDouble()
        val lon = ((x + 0.5) / n) * 360.0 - 180.0
        val latRad = atan(sinh(PI * (1.0 - 2.0 * (y + 0.5) / n)))
        val lat = Math.toDegrees(latRad)
        return lat to lon
    }

    private fun openOrGetDb(path: String): SQLiteDatabase? {
        synchronized(dbLock) {
            val existing = regionDbs[path]
            if (existing != null && existing.isOpen) return existing
            return try {
                val db = SQLiteDatabase.openDatabase(
                    path,
                    null,
                    SQLiteDatabase.OPEN_READONLY or SQLiteDatabase.NO_LOCALIZED_COLLATORS
                )
                regionDbs[path] = db
                db
            } catch (_: Exception) {
                null
            }
        }
    }

    fun readFromRegions(context: Context, z: Int, x: Int, y: Int, includeBundledLowZoom: Boolean = true): ByteArray? {
        val index = getOrLoadIndex(context)
        if (index.isEmpty()) return null
        if (z !in 0..22) return null
        val maxCoord = 1 shl z
        if (x < 0 || x >= maxCoord || y < 0 || y >= maxCoord) return null

        val (lat, lon) = tileCenter(z, x, y)
        val tmsY = maxCoord - 1 - y

        for (region in index) {
            if (!includeBundledLowZoom && region.id == "yemen_low") continue
            if (z < region.minZ || z > region.maxZ) continue
            if (lat < region.minLat || lat > region.maxLat) continue
            if (lon < region.minLon || lon > region.maxLon) continue

            val db = openOrGetDb(region.filePath) ?: continue
            synchronized(dbLock) {
                try {
                    db.rawQuery(
                        "SELECT tile_data FROM tiles WHERE zoom_level=? AND tile_column=? AND tile_row=? LIMIT 1",
                        arrayOf(z.toString(), x.toString(), tmsY.toString())
                    ).use { cursor ->
                        if (cursor.moveToFirst()) {
                            val bytes = cursor.getBlob(0)
                            if (bytes != null && bytes.isNotEmpty()) {
                                return bytes
                            }
                        }
                    }
                } catch (_: Exception) {
                }
            }
        }
        return null
    }

    fun mimeOf(bytes: ByteArray): String {
        if (bytes.size >= 4 &&
            bytes[0] == 0x89.toByte() &&
            bytes[1] == 0x50.toByte() &&
            bytes[2] == 0x4E.toByte() &&
            bytes[3] == 0x47.toByte()
        ) {
            return "image/png"
        }
        if (bytes.size >= 3 &&
            bytes[0] == 0xFF.toByte() &&
            bytes[1] == 0xD8.toByte() &&
            bytes[2] == 0xFF.toByte()
        ) {
            return "image/jpeg"
        }
        if (bytes.size >= 12 &&
            bytes[0] == 'R'.code.toByte() &&
            bytes[1] == 'I'.code.toByte() &&
            bytes[2] == 'F'.code.toByte() &&
            bytes[3] == 'F'.code.toByte() &&
            bytes[8] == 'W'.code.toByte() &&
            bytes[9] == 'E'.code.toByte() &&
            bytes[10] == 'B'.code.toByte() &&
            bytes[11] == 'P'.code.toByte()
        ) {
            return "image/webp"
        }
        return "image/png"
    }

    private fun image(mime: String, bytes: ByteArray): WebResourceResponse {
        return WebResourceResponse(
            mime,
            null,
            200,
            "OK",
            mapOf(
                "Access-Control-Allow-Origin" to "*",
                "Cache-Control" to "public, max-age=604800"
            ),
            ByteArrayInputStream(bytes)
        )
    }

    private fun extractCoords(host: String, path: String): Triple<Int, Int, Int>? {
        val m1 = OSM_TILE_REGEX.matchEntire(path)
        if (m1 != null) {
            val z = m1.groupValues[1].toIntOrNull() ?: return null
            val x = m1.groupValues[2].toIntOrNull() ?: return null
            val y = m1.groupValues[3].toIntOrNull() ?: return null
            return Triple(z, x, y)
        }
        if (host.contains("arcgisonline.com") || host.contains("arcgis.com")) {
            val m2 = ESRI_ZYX_REGEX.matchEntire(path)
            if (m2 != null) {
                val z = m2.groupValues[1].toIntOrNull() ?: return null
                val y = m2.groupValues[2].toIntOrNull() ?: return null
                val x = m2.groupValues[3].toIntOrNull() ?: return null
                return Triple(z, x, y)
            }
        }
        return null
    }

    fun intercept(context: Context, request: WebResourceRequest?): WebResourceResponse? {
        val uri = request?.url ?: return null
        val host = uri.host?.lowercase() ?: return null
        migrateCacheIfNeeded(context)

        val path = uri.path ?: return null
        val coords = extractCoords(host, path)

        // 1. User-downloaded high-zoom MBTiles regions for standard map hosts
        val isOsmOrTopo = host.endsWith("openstreetmap.org") || host.endsWith("opentopomap.org")
        if (coords != null && isOsmOrTopo) {
            val (z, x, y) = coords
            readFromRegions(context, z, x, y, includeBundledLowZoom = false)?.let { bytes ->
                return image(mimeOf(bytes), bytes)
            }
        }

        val isCacheHost = host in CACHEABLE_HOSTS || CACHEABLE_HOSTS.any { host.endsWith(".$it") }
        if (!isCacheHost) return null

        val fullUrl = uri.toString()
        val cacheFile = runCatching { cacheFileForUrl(context, fullUrl) }.getOrNull()

        // 2. Serve valid cached tile from disk if fresh
        if (cacheFile != null && cacheFile.exists() && cacheFile.length() >= 64L) {
            val isFresh = (System.currentTimeMillis() - cacheFile.lastModified()) <= TILE_TTL_MS
            if (isFresh || !NetworkUtils.hasInternetTransport(context)) {
                val cachedBytes = runCatching { cacheFile.readBytes() }.getOrNull()
                if (cachedBytes != null && isCacheable(host, null, cachedBytes)) {
                    return image(mimeOf(cachedBytes), cachedBytes)
                } else {
                    runCatching { cacheFile.delete() }
                }
            }
        }

        // 3. Offline fallback to bundled Yemen low-zoom MBTiles when no internet
        val hasNet = NetworkUtils.hasInternetTransport(context)
        val isBaseVectorOrSatellite = isOsmOrTopo || path.contains("World_Imagery") || path.contains("s2cloudless")
        if (!hasNet && coords != null && isBaseVectorOrSatellite) {
            val (z, x, y) = coords
            readFromRegions(context, z, x, y, includeBundledLowZoom = true)?.let { bytes ->
                return image(mimeOf(bytes), bytes)
            }
            return null
        }

        if (!hasNet) return null

        // 4. Online fetch & validate & persist to tile_cache_v3
        var conn: HttpURLConnection? = null
        return try {
            conn = (URL(fullUrl).openConnection() as HttpURLConnection).apply {
                connectTimeout = 5_000
                readTimeout = 6_000
                instanceFollowRedirects = true
                setRequestProperty("User-Agent", "AlYemenServicesApp/1.2 (Android; OpenStreetMap)")
            }
            if (conn.responseCode !in 200..299) {
                if (coords != null && isBaseVectorOrSatellite) {
                    val (z, x, y) = coords
                    readFromRegions(context, z, x, y, includeBundledLowZoom = true)?.let {
                        return image(mimeOf(it), it)
                    }
                }
                return null
            }
            val ct = conn.contentType
            val bytes = conn.inputStream.use { it.readBytes() }
            if (!isCacheable(host, ct, bytes)) {
                return null
            }
            if (cacheFile != null) {
                runCatching {
                    val tmp = File(cacheFile.parentFile, "${cacheFile.name}.tmp")
                    tmp.writeBytes(bytes)
                    if (cacheFile.exists()) cacheFile.delete()
                    if (!tmp.renameTo(cacheFile)) {
                        tmp.copyTo(cacheFile, overwrite = true)
                        tmp.delete()
                    }
                }
            }
            image(mimeOf(bytes), bytes)
        } catch (_: Exception) {
            if (coords != null && isBaseVectorOrSatellite) {
                val (z, x, y) = coords
                readFromRegions(context, z, x, y, includeBundledLowZoom = true)?.let {
                    return image(mimeOf(it), it)
                }
            }
            null
        } finally {
            try {
                conn?.disconnect()
            } catch (_: Exception) {
            }
        }
    }
}
