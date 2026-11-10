package com.serviseyem.utils

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
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
        prefs.edit().clear().apply()
        try {
            val marker = getMarkerFile(context)
            if (marker.exists()) marker.delete()
            val archive = getTilesArchive(context)
            if (archive.exists()) archive.delete()
        } catch (_: Exception) {}
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

        try {
            val defaultUrl = "https://github.com/maher202021/Vela2026/releases/download/yemen-valhalla-1/yemen_valhalla_tiles.zip"
            var currentUrl = if (customUrl.isNotBlank()) customUrl else defaultUrl

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
            conn.connect()
            if (conn.responseCode !in 200..299) {
                throw IllegalStateException("فشل الاتصال: رمز الاستجابة ${conn.responseCode}")
            }

            val length = conn.contentLength.toLong()
            val input = conn.inputStream
            val output = FileOutputStream(tempZipFile)

            val data = ByteArray(16384)
            var total: Long = 0
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
            output.flush()
            output.close()
            input.close()
            conn.disconnect()

            onProgress(0.72f, "جاري فك ضغط خرائط الملاحة الدقيقة والتحقق من سلامتها...")

            // فك الضغط بأمان مع حماية Zip Slip
            var extractedTarFound = false
            ZipInputStream(tempZipFile.inputStream().buffered()).use { zis ->
                var entry = zis.nextEntry
                while (entry != null) {
                    val entryName = entry.name
                    val destinationFile = if (entryName.endsWith(".tar") || entryName.contains("valhalla_tiles")) {
                        targetArchive
                    } else {
                        File(valhallaDir, entryName)
                    }

                    // 🛡️ حماية Zip Slip الصارمة
                    val canonicalDest = destinationFile.canonicalPath
                    val canonicalDir = valhallaDir.canonicalPath + File.separator
                    if (!canonicalDest.startsWith(canonicalDir) && destinationFile != targetArchive) {
                        zis.closeEntry()
                        entry = zis.nextEntry
                        continue
                    }

                    if (!entry.isDirectory) {
                        destinationFile.parentFile?.mkdirs()
                        FileOutputStream(destinationFile).use { fos ->
                            var len: Int
                            while (zis.read(data).also { len = it } > 0) {
                                fos.write(data, 0, len)
                            }
                            fos.flush()
                        }
                        if (destinationFile == targetArchive) {
                            extractedTarFound = true
                        }
                    }
                    zis.closeEntry()
                    entry = zis.nextEntry
                }
            }

            if (tempZipFile.exists()) tempZipFile.delete()

            // التحقق الدقيق من حجم ملف الـ TAR الفعلي
            if (!targetArchive.exists() || targetArchive.length() < MIN_VALID_SIZE_BYTES) {
                targetArchive.delete()
                marker.delete()
                throw IllegalStateException("حجم ملف خرائط Valhalla غير مكتمل (${targetArchive.length()} بايت) - يجب أن يكون أكبر من 10 ميجابايت.")
            }

            // توثيق النجاح الفعلي الحقيقي فقط بعد التحقق
            marker.writeText("SUCCESS_${System.currentTimeMillis()}\nSIZE=${targetArchive.length()}")
            val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            prefs.edit().putBoolean(KEY_IS_DOWNLOADED, true).putLong(KEY_DOWNLOAD_TIME, System.currentTimeMillis()).apply()

            onProgress(1.0f, "اكتمل تنزيل وتفعيل الملاحة الدقيقة لليمن بنجاح ✓ (${targetArchive.length() / (1024 * 1024)} MB)")
            true
        } catch (e: Exception) {
            e.printStackTrace()
            // تنظيف أي ملفات جزئية تالفة لمنع التنزيل الكاذب
            if (tempZipFile.exists()) tempZipFile.delete()
            if (targetArchive.exists() && targetArchive.length() < MIN_VALID_SIZE_BYTES) {
                targetArchive.delete()
            }
            marker.delete()
            val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            prefs.edit().putBoolean(KEY_IS_DOWNLOADED, false).apply()

            onProgress(0.0f, "فشل تحميل خرائط الملاحة: ${e.localizedMessage ?: "خطأ في الشبكة"}")
            false
        }
    }
}
