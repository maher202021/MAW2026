package com.serviseyem.utils

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.os.StatFs
import androidx.work.BackoffPolicy
import androidx.work.Constraints
import androidx.work.CoroutineWorker
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL
import java.security.MessageDigest
import java.util.concurrent.TimeUnit
import kotlin.math.max

data class RegionPack(
    val id: String,
    val name: String,
    val url: String,
    val sha256: String,
    val sizeMb: Long,
    val minZoom: Int,
    val maxZoom: Int,
    val minLat: Double,
    val maxLat: Double,
    val minLon: Double,
    val maxLon: Double,
    val format: String = "png",
    val version: Int = 1
)

object RegionPackManager {
    private val ID_REGEX = Regex("^[a-z0-9_-]+$")
    private const val PREFS_NAME = "valhalla_prefs"
    private const val PREF_MANIFEST_URL = "regions_manifest_url"

    fun dir(context: Context): File {
        return File(context.applicationContext.filesDir, "regions").apply { mkdirs() }
    }

    fun workName(id: String): String = "region_pack_$id"

    fun configuredManifestUrl(context: Context): String {
        val sp = context.applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val saved = sp.getString(PREF_MANIFEST_URL, "").orEmpty().trim()
        return saved.ifBlank { NavConfig.REGIONS_MANIFEST_URL.trim() }
    }

    fun isValidId(id: String): Boolean = ID_REGEX.matches(id)

    fun mbtilesFile(context: Context, id: String): File {
        require(isValidId(id)) { "Invalid region pack id: $id" }
        return File(dir(context), "$id.mbtiles")
    }

    fun markerFile(context: Context, id: String): File {
        require(isValidId(id)) { "Invalid region pack id: $id" }
        return File(dir(context), "$id.json")
    }

    fun partFile(context: Context, id: String): File {
        require(isValidId(id)) { "Invalid region pack id: $id" }
        return File(dir(context), "$id.part")
    }

    fun isInstalled(context: Context, id: String): Boolean {
        if (!isValidId(id)) return false
        val db = mbtilesFile(context, id)
        val marker = markerFile(context, id)
        return db.exists() && db.length() > 0L && marker.exists()
    }

    fun installedIds(context: Context): Set<String> {
        val baseDir = dir(context)
        val files = baseDir.listFiles() ?: return emptySet()
        val ids = HashSet<String>()
        for (f in files) {
            if (f.isFile && f.name.endsWith(".json")) {
                val id = f.name.removeSuffix(".json")
                if (isValidId(id) && File(baseDir, "$id.mbtiles").let { it.exists() && it.length() > 0L }) {
                    ids.add(id)
                }
            }
        }
        return ids
    }

    fun delete(context: Context, id: String) {
        if (!isValidId(id)) return
        OfflineTileInterceptor.invalidateRegions()
        runCatching { mbtilesFile(context, id).delete() }
        runCatching { markerFile(context, id).delete() }
        runCatching { partFile(context, id).delete() }
        OfflineTileInterceptor.invalidateRegions()
    }

    fun parseManifest(json: String): List<RegionPack> {
        val trimmed = json.trim()
        if (trimmed.isEmpty()) return emptyList()

        val array = if (trimmed.startsWith("[")) {
            JSONArray(trimmed)
        } else {
            val root = JSONObject(trimmed)
            root.optJSONArray("regions") ?: root.optJSONArray("packs") ?: JSONArray()
        }

        val out = mutableListOf<RegionPack>()
        for (i in 0 until array.length()) {
            val obj = array.optJSONObject(i) ?: continue
            val id = obj.optString("id", "").trim()
            val url = obj.optString("url", "").trim()
            if (!ID_REGEX.matches(id)) continue
            if (!url.startsWith("https://")) continue

            val name = obj.optString("name", id).trim().ifBlank { id }
            val sha = obj.optString("sha256", obj.optString("sha", "")).trim().lowercase()
            val sizeMb = obj.optLong("sizeMb", obj.optLong("size_mb", 0L)).coerceAtLeast(1L)
            val minZ = obj.optInt("minZoom", obj.optInt("min_zoom", obj.optInt("minZ", 0)))
            val maxZ = obj.optInt("maxZoom", obj.optInt("max_zoom", obj.optInt("maxZ", 17)))
            val minLat = obj.optDouble("minLat", obj.optDouble("min_lat", 12.0))
            val maxLat = obj.optDouble("maxLat", obj.optDouble("max_lat", 19.5))
            val minLon = obj.optDouble("minLon", obj.optDouble("min_lon", 41.5))
            val maxLon = obj.optDouble("maxLon", obj.optDouble("max_lon", 54.5))
            val format = obj.optString("format", "png").trim().ifBlank { "png" }
            val version = obj.optInt("version", 1)

            out.add(
                RegionPack(
                    id = id,
                    name = name,
                    url = url,
                    sha256 = sha,
                    sizeMb = sizeMb,
                    minZoom = minZ,
                    maxZoom = maxZ,
                    minLat = minLat,
                    maxLat = maxLat,
                    minLon = minLon,
                    maxLon = maxLon,
                    format = format,
                    version = version
                )
            )
        }
        return out
    }

    suspend fun fetchManifest(url: String = NavConfig.REGIONS_MANIFEST_URL): Result<List<RegionPack>> =
        withContext(Dispatchers.IO) {
            val cleanUrl = url.trim()
            if (cleanUrl.isBlank()) {
                return@withContext Result.failure(
                    IllegalStateException("لم يتم ضبط رابط قائمة الخرائط بعد (REGIONS_MANIFEST_URL).")
                )
            }
            if (!cleanUrl.startsWith("https://")) {
                return@withContext Result.failure(
                    IllegalArgumentException("يجب أن يبدأ رابط قائمة الخرائط بـ https://")
                )
            }

            var conn: HttpURLConnection? = null
            try {
                conn = (URL(cleanUrl).openConnection() as HttpURLConnection).apply {
                    instanceFollowRedirects = true
                    connectTimeout = 15_000
                    readTimeout = 20_000
                    setRequestProperty("User-Agent", "AlYemenServicesApp/1.0 (Android)")
                    setRequestProperty("Accept", "application/json")
                }
                val code = conn.responseCode
                if (code !in 200..299) {
                    return@withContext Result.failure(
                        IOException("تعذر جلب قائمة المناطق (HTTP $code)")
                    )
                }
                val body = conn.inputStream.bufferedReader().use { it.readText() }
                Result.success(parseManifest(body))
            } catch (e: Exception) {
                Result.failure(e)
            } finally {
                try {
                    conn?.disconnect()
                } catch (_: Exception) {
                }
            }
        }

    fun enqueue(context: Context, pack: RegionPack, wifiOnly: Boolean) {
        if (!isValidId(pack.id) || !pack.url.startsWith("https://")) return

        val constraints = Constraints.Builder()
            .setRequiredNetworkType(if (wifiOnly) NetworkType.UNMETERED else NetworkType.CONNECTED)
            .setRequiresStorageNotLow(true)
            .build()

        val data = workDataOf(
            "id" to pack.id,
            "name" to pack.name,
            "url" to pack.url,
            "sha" to pack.sha256,
            "size" to pack.sizeMb,
            "minZ" to pack.minZoom,
            "maxZ" to pack.maxZoom,
            "minLat" to pack.minLat,
            "maxLat" to pack.maxLat,
            "minLon" to pack.minLon,
            "maxLon" to pack.maxLon,
            "format" to pack.format,
            "version" to pack.version
        )

        val request = OneTimeWorkRequestBuilder<RegionDownloadWorker>()
            .setConstraints(constraints)
            .setBackoffCriteria(BackoffPolicy.LINEAR, 20L, TimeUnit.SECONDS)
            .setInputData(data)
            .addTag("region_pack")
            .addTag(workName(pack.id))
            .build()

        WorkManager.getInstance(context.applicationContext)
            .enqueueUniqueWork(workName(pack.id), ExistingWorkPolicy.KEEP, request)
    }

    fun cancel(context: Context, id: String) {
        if (!isValidId(id)) return
        WorkManager.getInstance(context.applicationContext).cancelUniqueWork(workName(id))
        runCatching { partFile(context, id).delete() }
    }

    fun sha256(file: File): String {
        val digest = MessageDigest.getInstance("SHA-256")
        val buf = ByteArray(64 * 1024)
        file.inputStream().use { input ->
            while (true) {
                val read = input.read(buf)
                if (read <= 0) break
                digest.update(buf, 0, read)
            }
        }
        return digest.digest().joinToString("") { "%02x".format(it) }
    }

    fun looksLikeMbtiles(file: File): Boolean {
        if (!file.exists() || file.length() < 100L) return false
        val header = ByteArray(16)
        try {
            file.inputStream().use { input ->
                val n = input.read(header)
                if (n < 15) return false
            }
            val magic = String(header, 0, 15, Charsets.US_ASCII)
            if (magic != "SQLite format 3") return false
        } catch (_: Exception) {
            return false
        }

        var db: SQLiteDatabase? = null
        return try {
            db = SQLiteDatabase.openDatabase(
                file.absolutePath,
                null,
                SQLiteDatabase.OPEN_READONLY or SQLiteDatabase.NO_LOCALIZED_COLLATORS
            )
            db.rawQuery(
                "SELECT name FROM sqlite_master WHERE type='table' AND name='tiles' LIMIT 1",
                null
            ).use { cursor ->
                cursor.moveToFirst()
            }
        } catch (_: Exception) {
            false
        } finally {
            try {
                db?.close()
            } catch (_: Exception) {
            }
        }
    }

    fun writeMarker(
        context: Context,
        id: String,
        name: String,
        minZ: Int,
        maxZ: Int,
        minLat: Double,
        maxLat: Double,
        minLon: Double,
        maxLon: Double,
        format: String,
        version: Int,
        sizeMb: Long
    ) {
        val obj = JSONObject().apply {
            put("id", id)
            put("name", name)
            put("minZ", minZ)
            put("maxZ", maxZ)
            put("minLat", minLat)
            put("maxLat", maxLat)
            put("minLon", minLon)
            put("maxLon", maxLon)
            put("format", format)
            put("version", version)
            put("sizeMb", sizeMb)
            put("installedAt", System.currentTimeMillis())
        }
        val marker = markerFile(context, id)
        val tmp = File(marker.parentFile, "$id.json.tmp")
        tmp.writeText(obj.toString(2))
        if (marker.exists()) marker.delete()
        if (!tmp.renameTo(marker)) {
            marker.writeText(obj.toString(2))
            tmp.delete()
        }
    }
}

class RegionDownloadWorker(
    appContext: Context,
    params: WorkerParameters
) : CoroutineWorker(appContext, params) {

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        val id = inputData.getString("id")?.trim().orEmpty()
        val name = inputData.getString("name")?.trim().orEmpty().ifBlank { id }
        val urlStr = inputData.getString("url")?.trim().orEmpty()
        val sha = inputData.getString("sha")?.trim()?.lowercase().orEmpty()
        val sizeMb = inputData.getLong("size", 0L).coerceAtLeast(1L)
        val minZ = inputData.getInt("minZ", 0)
        val maxZ = inputData.getInt("maxZ", 17)
        val minLat = inputData.getDouble("minLat", 12.0)
        val maxLat = inputData.getDouble("maxLat", 19.5)
        val minLon = inputData.getDouble("minLon", 41.5)
        val maxLon = inputData.getDouble("maxLon", 54.5)
        val format = inputData.getString("format")?.trim().orEmpty().ifBlank { "png" }
        val version = inputData.getInt("version", 1)

        if (!RegionPackManager.isValidId(id) || !urlStr.startsWith("https://")) {
            return@withContext Result.failure()
        }

        val dir = RegionPackManager.dir(applicationContext)
        val stat = StatFs(dir.absolutePath)
        val requiredBytes = max(sizeMb * 2L * 1024L * 1024L, 120L * 1024L * 1024L)
        if (stat.availableBytes < requiredBytes) {
            return@withContext Result.failure(
                workDataOf("error" to "المساحة التخزينية المتاحة غير كافية")
            )
        }

        val partFile = RegionPackManager.partFile(applicationContext, id)
        val targetFile = RegionPackManager.mbtilesFile(applicationContext, id)
        val startedAt = System.currentTimeMillis()
        val deadlineMs = 8L * 60L * 1000L

        var conn: HttpURLConnection? = null
        try {
            val existingBytes = if (partFile.exists()) partFile.length() else 0L
            conn = (URL(urlStr).openConnection() as HttpURLConnection).apply {
                instanceFollowRedirects = true
                connectTimeout = 20_000
                readTimeout = 30_000
                setRequestProperty("User-Agent", "AlYemenServicesApp/1.0 (Android)")
                if (existingBytes > 0L) {
                    setRequestProperty("Range", "bytes=$existingBytes-")
                }
            }

            val code = conn.responseCode
            if (code in 500..599) {
                return@withContext Result.retry()
            }

            val append = when (code) {
                HttpURLConnection.HTTP_PARTIAL -> true
                HttpURLConnection.HTTP_OK -> false
                416 -> {
                    // Range already complete, proceed directly to verification
                    if (existingBytes > 0L) {
                        return@withContext finalizePartFile(
                            id, name, sha, sizeMb, minZ, maxZ, minLat, maxLat, minLon, maxLon, format, version,
                            partFile, targetFile
                        )
                    } else {
                        partFile.delete()
                        return@withContext Result.retry()
                    }
                }
                else -> return@withContext Result.failure()
            }

            val contentLenHeader = conn.contentLengthLong.takeIf { it > 0L }
                ?: conn.getHeaderField("Content-Length")?.toLongOrNull()
                ?: -1L
            val totalExpected = when {
                append && contentLenHeader > 0L -> existingBytes + contentLenHeader
                !append && contentLenHeader > 0L -> contentLenHeader
                else -> sizeMb * 1024L * 1024L
            }

            var downloaded = if (append) existingBytes else 0L
            var lastProgressPct = -1
            var lastProgressReportAt = 0L

            conn.inputStream.use { input ->
                FileOutputStream(partFile, append).use { output ->
                    val buffer = ByteArray(64 * 1024)
                    while (true) {
                        if (isStopped) {
                            return@withContext Result.retry()
                        }
                        if (System.currentTimeMillis() - startedAt > deadlineMs) {
                            return@withContext Result.retry()
                        }
                        val read = input.read(buffer)
                        if (read <= 0) break
                        output.write(buffer, 0, read)
                        downloaded += read

                        val pct = if (totalExpected > 0L) {
                            ((downloaded * 100L) / totalExpected).toInt().coerceIn(0, 99)
                        } else 0
                        val now = System.currentTimeMillis()
                        if (pct != lastProgressPct && (now - lastProgressReportAt >= 250L || pct == 99)) {
                            lastProgressPct = pct
                            lastProgressReportAt = now
                            setProgress(workDataOf("p" to pct))
                        }
                    }
                    output.flush()
                }
            }

            return@withContext finalizePartFile(
                id, name, sha, sizeMb, minZ, maxZ, minLat, maxLat, minLon, maxLon, format, version,
                partFile, targetFile
            )
        } catch (_: IOException) {
            if (runAttemptCount < 5) Result.retry() else Result.failure()
        } catch (_: Exception) {
            Result.failure()
        } finally {
            try {
                conn?.disconnect()
            } catch (_: Exception) {
            }
        }
    }

    private suspend fun finalizePartFile(
        id: String,
        name: String,
        sha: String,
        sizeMb: Long,
        minZ: Int,
        maxZ: Int,
        minLat: Double,
        maxLat: Double,
        minLon: Double,
        maxLon: Double,
        format: String,
        version: Int,
        partFile: File,
        targetFile: File
    ): Result {
        setProgress(workDataOf("p" to 99))
        if (sha.isNotBlank()) {
            val actual = RegionPackManager.sha256(partFile).lowercase()
            if (actual != sha) {
                partFile.delete()
                return Result.failure(workDataOf("error" to "فشل التحقق من بصمة الملف (SHA-256)"))
            }
        }
        if (!RegionPackManager.looksLikeMbtiles(partFile)) {
            partFile.delete()
            return Result.failure(workDataOf("error" to "الملف الذي تم تنزيله ليس بصيغة MBTiles صالحة"))
        }

        OfflineTileInterceptor.invalidateRegions()
        if (targetFile.exists()) targetFile.delete()
        if (!partFile.renameTo(targetFile)) {
            partFile.copyTo(targetFile, overwrite = true)
            partFile.delete()
        }

        RegionPackManager.writeMarker(
            context = applicationContext,
            id = id,
            name = name,
            minZ = minZ,
            maxZ = maxZ,
            minLat = minLat,
            maxLat = maxLat,
            minLon = minLon,
            maxLon = maxLon,
            format = format,
            version = version,
            sizeMb = sizeMb
        )
        OfflineTileInterceptor.invalidateRegions()
        setProgress(workDataOf("p" to 100))
        return Result.success()
    }
}
