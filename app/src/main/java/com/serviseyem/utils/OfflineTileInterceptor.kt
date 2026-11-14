package com.serviseyem.utils

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.webkit.WebResourceRequest
import android.webkit.WebResourceResponse
import org.json.JSONObject
import java.io.ByteArrayInputStream
import java.io.File
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

object OfflineTileInterceptor {
    private val OSM_TILE_REGEX = Regex("^/(?:[^/]+/)*(\\d{1,2})/(\\d+)/(\\d+)(?:@\\dx)?\\.(png|jpg|jpeg|webp)$", RegexOption.IGNORE_CASE)

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
        // Prefer more specific city packs (higher minZ) first, then larger maxZ
        return list.sortedWith(
            compareByDescending<RegionInfo> { it.minZ }
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

    fun readFromRegions(context: Context, z: Int, x: Int, y: Int): ByteArray? {
        val index = getOrLoadIndex(context)
        if (index.isEmpty()) return null
        if (z !in 0..22) return null
        val maxCoord = 1 shl z
        if (x < 0 || x >= maxCoord || y < 0 || y >= maxCoord) return null

        val (lat, lon) = tileCenter(z, x, y)
        val tmsY = maxCoord - 1 - y

        for (region in index) {
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
                "Cache-Control" to "public, max-age=86400"
            ),
            ByteArrayInputStream(bytes)
        )
    }

    fun intercept(context: Context, request: WebResourceRequest?): WebResourceResponse? {
        val uri = request?.url ?: return null
        val host = uri.host?.lowercase() ?: return null
        if (!host.endsWith("openstreetmap.org")) return null

        val path = uri.path ?: return null
        val match = OSM_TILE_REGEX.matchEntire(path) ?: return null
        val z = match.groupValues[1].toIntOrNull() ?: return null
        val x = match.groupValues[2].toIntOrNull() ?: return null
        val y = match.groupValues[3].toIntOrNull() ?: return null

        val bytes = readFromRegions(context, z, x, y) ?: return null
        return image(mimeOf(bytes), bytes)
    }
}
