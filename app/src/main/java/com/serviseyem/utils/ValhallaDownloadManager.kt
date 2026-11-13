package com.serviseyem.utils

import android.content.Context
import android.net.Uri
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL
import java.util.zip.ZipInputStream

object ValhallaDownloadManager {

    private const val PREFS_NAME = "valhalla_prefs"
    private const val KEY_IS_DOWNLOADED = "is_valhalla_downloaded"
    private const val KEY_DOWNLOAD_TIME = "valhalla_download_time"
    private const val MARKER_FILE_NAME = "is_valhalla_downloaded.txt"
    private const val VALHALLA_DIR_NAME = "valhalla"
    private const val TILES_ARCHIVE_NAME = "valhalla_tiles.tar"
    private const val MIN_VALID_SIZE_BYTES = 10_000_000L // 10 ميجابايت كحد أدنى لمنع الحزم الفارغة أو التالفة

    fun getValhallaDir(context: Context): File {
        val dir = File(context.filesDir, VALHALLA_DIR_NAME)
        if (!dir.exists()) dir.mkdirs()
        return dir
    }

    fun getTilesArchive(context: Context): File = File(getValhallaDir(context), TILES_ARCHIVE_NAME)

    fun getMarkerFile(context: Context): File = File(getValhallaDir(context), MARKER_FILE_NAME)

    fun getInstalledSizeMb(context: Context): Long {
        val f = getTilesArchive(context)
        return if (f.exists()) f.length() / (1024L * 1024L) else 0L
    }

    /**
     * التحقق الحقيقي من وجود ملف valhalla_tiles.tar بحجم يتجاوز 10 ميجابايت ووجود علامة النجاح
     */
    fun isDownloaded(context: Context): Boolean {
        val archive = getTilesArchive(context)
        val marker = getMarkerFile(context)
        return archive.exists() && archive.length() >= MIN_VALID_SIZE_BYTES && marker.exists() && marker.length() > 0
    }

    fun isCityDownloaded(context: Context, cityName: String): Boolean {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getBoolean("city_offline_$cityName", false) || isDownloaded(context)
    }

    fun markCityAsDownloaded(context: Context, cityName: String) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putBoolean("city_offline_$cityName", true).apply()
    }

    fun clearOfflineData(context: Context) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putBoolean(KEY_IS_DOWNLOADED, false).apply()
        try {
            val marker = getMarkerFile(context)
            if (marker.exists()) marker.delete()
            val archive = getTilesArchive(context)
            if (archive.exists()) archive.delete()
        } catch (_: Exception) {
        }
    }

    suspend fun downloadAndExtractOfflineTiles(
        context: Context,
        customUrl: String = "",
        onProgress: (Float, String) -> Unit
    ): Boolean = withContext(Dispatchers.IO) {
        val valhallaDir = getValhallaDir(context)
        val targetArchive = getTilesArchive(context)
        val marker = getMarkerFile(context)
        val tempZipFile = File(valhallaDir, "temp_valhalla_download.zip")
        val stagingArchive = File(valhallaDir, "staging_valhalla_tiles.tar")

        try {
            var currentUrl = if (customUrl.isNotBlank()) customUrl.trim() else NavConfig.VALHALLA_DEFAULT_URL

            onProgress(0.05f, "جاري الاتصال بسيرفر خرائط ومسارات اليمن...")

            var connection: HttpURLConnection? = null
            var redirects = 0
            while (redirects < 5) {
                val url = URL(currentUrl)
                val conn = (url.openConnection() as HttpURLConnection).apply {
                    instanceFollowRedirects = true
                    connectTimeout = 20000
                    readTimeout = 60000
                    setRequestProperty("User-Agent", "Serviseyem-App/1.2.0")
                }
                val status = conn.responseCode
                if (status in listOf(301, 302, 303, 307, 308)) {
                    val newUrl = conn.getHeaderField("Location")
                    conn.disconnect()
                    if (!newUrl.isNullOrBlank()) {
                        currentUrl = newUrl
                        redirects++
                        continue
                    }
                }
                connection = conn
                break
            }

            val conn = connection ?: throw IllegalStateException("تعذر إنشاء اتصال بسيرفر الخرائط")
            if (conn.responseCode !in 200..299) {
                throw IllegalStateException("فشل الاتصال: رمز الاستجابة ${conn.responseCode}")
            }

            val length = conn.contentLengthLong
            val data = ByteArray(16384)
            var total = 0L

            conn.inputStream.use { input ->
                FileOutputStream(tempZipFile).use { output ->
                    var count: Int
                    while (input.read(data).also { count = it } != -1) {
                        total += count.toLong()
                        output.write(data, 0, count)
                        if (length > 0) {
                            val progress = 0.05f + (total.toFloat() / length.toFloat()) * 0.65f
                            onProgress(progress.coerceIn(0.05f, 0.70f), "جاري تحميل بيانات الملاحة (${(progress * 100).toInt()}%)...")
                        } else {
                            onProgress(0.35f, "جاري استقبال حزم الخرائط (${total / (1024 * 1024)} MB)...")
                        }
                    }
                    output.fd.sync()
                }
            }
            conn.disconnect()

            onProgress(0.72f, "جاري فك ضغط خرائط الملاحة الدقيقة والتحقق من سلامتها...")
            extractArchiveFile(tempZipFile, valhallaDir, stagingArchive)
            if (tempZipFile.exists()) tempZipFile.delete()

            if (!stagingArchive.exists() || stagingArchive.length() < MIN_VALID_SIZE_BYTES) {
                stagingArchive.delete()
                throw IllegalStateException("حجم ملف خرائط Valhalla غير مكتمل (${stagingArchive.length()} بايت) - يجب أن يكون أكبر من 10 ميجابايت.")
            }

            promoteInstalledArchive(context, stagingArchive, targetArchive, marker)
            onProgress(1.0f, "اكتمل تنزيل وتفعيل الملاحة الدقيقة لليمن بنجاح ✓ (${targetArchive.length() / (1024 * 1024)} MB)")
            true
        } catch (e: CancellationException) {
            tempZipFile.delete()
            stagingArchive.delete()
            throw e
        } catch (e: Exception) {
            e.printStackTrace()
            tempZipFile.delete()
            stagingArchive.delete()
            if (!targetArchive.exists() || targetArchive.length() < MIN_VALID_SIZE_BYTES) {
                targetArchive.delete()
                marker.delete()
                context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
                    .edit().putBoolean(KEY_IS_DOWNLOADED, false).apply()
            }
            onProgress(0.0f, "فشل تحميل خرائط الملاحة: ${e.localizedMessage ?: "خطأ في الشبكة"}")
            false
        }
    }

    /**
     * استيراد ملف خرائط أوفلاين من ذاكرة الهاتف (يدعم .zip المحتوي على valhalla_tiles.tar أو .tar مباشرة أو places.db/.zip)
     * دون إتلاف الملفات الحالية إذا فشل الاستيراد أو كان الملف غير صالح.
     */
    suspend fun importFromUri(
        context: Context,
        uri: Uri,
        onProgress: (Float, String) -> Unit
    ): Result<String> = withContext(Dispatchers.IO) {
        val valhallaDir = getValhallaDir(context)
        val targetArchive = getTilesArchive(context)
        val marker = getMarkerFile(context)
        val workDir = File(valhallaDir, "import_work").apply { deleteRecursively(); mkdirs() }
        val copiedSource = File(workDir, "picked_package")
        val stagingArchive = File(workDir, "staging_valhalla_tiles.tar")

        try {
            onProgress(0.10f, "جاري فتح وقراءة الملف من ذاكرة الهاتف...")
            val resolver = context.contentResolver
            val input = resolver.openInputStream(uri)
                ?: return@withContext Result.failure(IOException("تعذر الوصول إلى الملف المختار"))

            val buf = ByteArray(32 * 1024)
            var totalCopied = 0L
            input.use { stream ->
                FileOutputStream(copiedSource).use { out ->
                    while (true) {
                        val n = stream.read(buf)
                        if (n < 0) break
                        out.write(buf, 0, n)
                        totalCopied += n
                        if (totalCopied % (512 * 1024) < 32 * 1024) {
                            val mb = totalCopied / (1024 * 1024)
                            onProgress(0.30f, "جاري نسخ الملف إلى ذاكرة التطبيق الداخلية ($mb MB)...")
                        }
                    }
                    out.fd.sync()
                }
            }

            if (!copiedSource.exists() || copiedSource.length() < 10_000L) {
                throw IOException("الملف المختار فارغ أو صغير جداً")
            }

            val header = ByteArray(512.coerceAtMost(copiedSource.length().toInt()))
            FileInputStream(copiedSource).use { it.read(header) }
            val isZip = header.size >= 2 && header[0] == 'P'.code.toByte() && header[1] == 'K'.code.toByte()
            val isSqlite = header.size >= 15 && String(header.copyOfRange(0, 15), Charsets.US_ASCII) == "SQLite format 3"
            val hasUstar = header.size >= 262 && String(header.copyOfRange(257, 262), Charsets.US_ASCII) == "ustar"

            if (isSqlite) {
                onProgress(0.70f, "تم اكتشاف قاعدة أماكن محلية (places.db) — جاري تثبيتها...")
                val rows = PlacesDbDownloader.importFromLocalFile(context, copiedSource, workDir)
                workDir.deleteRecursively()
                val msg = "تم استيراد قاعدة الأماكن بنجاح ✓ ($rows مكان ومَعلم)"
                onProgress(1.0f, msg)
                return@withContext Result.success(msg)
            }

            if (isZip) {
                onProgress(0.55f, "جاري فحص وفك ضغط الحزمة داخل التطبيق...")
                val contents = inspectAndExtractZip(copiedSource, valhallaDir, stagingArchive, workDir, onProgress)
                when (contents) {
                    ZipKind.VALHALLA_TAR -> {
                        if (!stagingArchive.exists() || stagingArchive.length() < MIN_VALID_SIZE_BYTES) {
                            throw IOException("ملف التوجيه .tar داخل الحزمة غير مكتمل (أقل من 10 ميجابايت)")
                        }
                        promoteInstalledArchive(context, stagingArchive, targetArchive, marker)
                        workDir.deleteRecursively()
                        val mb = targetArchive.length() / (1024L * 1024L)
                        val msg = "تم استيراد وحفظ خرائط الملاحة الدقيقة بنجاح ✓ ($mb MB)"
                        onProgress(1.0f, msg)
                        return@withContext Result.success(msg)
                    }
                    ZipKind.PLACES_DB -> {
                        val dbFile = File(workDir, "places.db")
                        val rows = PlacesDbDownloader.importFromLocalFile(context, dbFile, workDir)
                        workDir.deleteRecursively()
                        val msg = "تم استيراد قاعدة الأماكن من ملف ZIP بنجاح ✓ ($rows مكان)"
                        onProgress(1.0f, msg)
                        return@withContext Result.success(msg)
                    }
                    ZipKind.UNSUPPORTED -> {
                        throw IOException("ملف ZIP المختار لا يحتوي على valhalla_tiles.tar أو places.db")
                    }
                }
            }

            val displayName = uri.lastPathSegment.orEmpty().lowercase()
            if (hasUstar || displayName.endsWith(".tar")) {
                onProgress(0.75f, "جاري التحقق من ملف valhalla_tiles.tar وحفظه...")
                if (copiedSource.length() < MIN_VALID_SIZE_BYTES) {
                    throw IOException("حجم ملف الـ TAR غير مكتمل (${copiedSource.length() / (1024 * 1024)} MB) — يجب ألا يقل عن 10 ميجابايت")
                }
                if (!copiedSource.renameTo(stagingArchive)) {
                    copiedSource.copyTo(stagingArchive, overwrite = true)
                }
                promoteInstalledArchive(context, stagingArchive, targetArchive, marker)
                workDir.deleteRecursively()
                val mb = targetArchive.length() / (1024L * 1024L)
                val msg = "تم استيراد وتفعيل خرائط الملاحة (.tar) بنجاح ✓ ($mb MB)"
                onProgress(1.0f, msg)
                return@withContext Result.success(msg)
            }

            throw IOException("نوع الملف غير مدعوم. اختر ملف yemen_valhalla_tiles.zip أو valhalla_tiles.tar أو places.db")
        } catch (e: CancellationException) {
            workDir.deleteRecursively()
            throw e
        } catch (e: Exception) {
            workDir.deleteRecursively()
            val errMsg = e.localizedMessage ?: "تعذر استيراد الملف"
            onProgress(0.0f, "فشل الاستيراد: $errMsg")
            Result.failure(e)
        }
    }

    private enum class ZipKind { VALHALLA_TAR, PLACES_DB, UNSUPPORTED }

    private fun extractArchiveFile(zipFile: File, valhallaDir: File, stagingArchive: File) {
        val buf = ByteArray(16384)
        ZipInputStream(zipFile.inputStream().buffered()).use { zis ->
            var entry = zis.nextEntry
            while (entry != null) {
                val entryName = entry.name
                val baseName = entryName.substringAfterLast('/')
                val isTar = entryName.endsWith(".tar", ignoreCase = true) || baseName == TILES_ARCHIVE_NAME
                val destinationFile = if (isTar) stagingArchive else File(valhallaDir, entryName)

                val canonicalDest = destinationFile.canonicalPath
                val canonicalDir = valhallaDir.canonicalPath + File.separator
                if (!isTar && !canonicalDest.startsWith(canonicalDir)) {
                    zis.closeEntry()
                    entry = zis.nextEntry
                    continue
                }

                if (!entry.isDirectory) {
                    destinationFile.parentFile?.mkdirs()
                    FileOutputStream(destinationFile).use { fos ->
                        var len: Int
                        while (zis.read(buf).also { len = it } > 0) {
                            fos.write(buf, 0, len)
                        }
                        fos.fd.sync()
                    }
                }
                zis.closeEntry()
                entry = zis.nextEntry
            }
        }
    }

    private fun inspectAndExtractZip(
        zipFile: File,
        valhallaDir: File,
        stagingArchive: File,
        workDir: File,
        onProgress: (Float, String) -> Unit
    ): ZipKind {
        var foundTar = false
        var foundPlacesDb = false
        val buf = ByteArray(32 * 1024)
        ZipInputStream(zipFile.inputStream().buffered()).use { zis ->
            var entry = zis.nextEntry
            while (entry != null) {
                val entryName = entry.name
                val baseName = entryName.substringAfterLast('/')
                val isTar = !entry.isDirectory && (entryName.endsWith(".tar", ignoreCase = true) || baseName == TILES_ARCHIVE_NAME)
                val isPlacesDb = !entry.isDirectory && baseName == "places.db"

                val destinationFile = when {
                    isTar -> stagingArchive
                    isPlacesDb -> File(workDir, "places.db")
                    else -> File(valhallaDir, entryName)
                }

                val canonicalDest = destinationFile.canonicalPath
                val allowedBase = if (isTar || isPlacesDb) workDir.canonicalPath else valhallaDir.canonicalPath
                if (!canonicalDest.startsWith(allowedBase + File.separator)) {
                    zis.closeEntry()
                    entry = zis.nextEntry
                    continue
                }

                if (!entry.isDirectory) {
                    if (isTar) onProgress(0.75f, "جاري استخراج حزمة الطرق (valhalla_tiles.tar)...")
                    destinationFile.parentFile?.mkdirs()
                    FileOutputStream(destinationFile).use { fos ->
                        var len: Int
                        while (zis.read(buf).also { len = it } > 0) {
                            fos.write(buf, 0, len)
                        }
                        fos.fd.sync()
                    }
                    if (isTar) foundTar = true
                    if (isPlacesDb) foundPlacesDb = true
                }
                zis.closeEntry()
                entry = zis.nextEntry
            }
        }
        return when {
            foundTar -> ZipKind.VALHALLA_TAR
            foundPlacesDb -> ZipKind.PLACES_DB
            else -> ZipKind.UNSUPPORTED
        }
    }

    private fun promoteInstalledArchive(
        context: Context,
        stagingArchive: File,
        targetArchive: File,
        marker: File
    ) {
        val backup = File(targetArchive.parentFile, "${TILES_ARCHIVE_NAME}.bak")
        if (backup.exists()) backup.delete()
        if (targetArchive.exists()) targetArchive.renameTo(backup)

        if (!stagingArchive.renameTo(targetArchive)) {
            stagingArchive.copyTo(targetArchive, overwrite = true)
            stagingArchive.delete()
        }
        if (backup.exists()) backup.delete()

        marker.writeText("SUCCESS_${System.currentTimeMillis()}\nSIZE=${targetArchive.length()}")
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit()
            .putBoolean(KEY_IS_DOWNLOADED, true)
            .putLong(KEY_DOWNLOAD_TIME, System.currentTimeMillis())
            .apply()
    }
}
