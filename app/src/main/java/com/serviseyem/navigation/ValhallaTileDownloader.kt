package com.serviseyem.navigation

// [إصلاح ملاحة نهائي] إدارة تنزيل وفك ضغط حزم خرائط اليمن لـ Valhalla والتحقق الصارم منها
import android.content.Context
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.net.HttpURLConnection
import java.net.URL
import java.util.zip.ZipInputStream

/**
 * 📦 ValhallaTileDownloader
 * Handles downloading, unzipping, saving, and persistence logic for Yemen Valhalla offline routing tiles.
 * - Download URL: https://github.com/maher202021/Vela2026/releases/download/yemen-valhalla-1/yemen_valhalla_tiles.zip
 * - Save Path: File(context.filesDir, "valhalla/valhalla_tiles.tar")
 * - Persistence Marker: File(context.filesDir, "valhalla/is_valhalla_downloaded.txt")
 * - Rule: NEVER ask the user to download again once marker and valid archive exist.
 */
object ValhallaTileDownloader {

    private const val TAG = "ValhallaTileDownloader"

    // [إصلاح ملاحة نهائي] رابط التحميل الصحيح والثابت لحزمة مسارات اليمن
    const val DEFAULT_DOWNLOAD_URL = "https://github.com/maher202021/Vela2026/releases/download/yemen-valhalla-1/yemen_valhalla_tiles.zip"
    const val MARKER_FILE_NAME = "is_valhalla_downloaded.txt"
    const val TARGET_ARCHIVE_NAME = "valhalla_tiles.tar"
    const val CONFIG_FILE_NAME = "valhalla.json"
    const val MIN_ARCHIVE_SIZE_BYTES = 1024L * 1024L // 1 Megabyte minimum

    /**
     * [إصلاح ملاحة نهائي] فحص وجود وصحة حزمة الخرائط الأوفلاين:
     * 1. وجود مجلد valhalla داخل filesDir
     * 2. وجود ملف valhalla_tiles.tar
     * 3. وجود ملف العلامة is_valhalla_downloaded.txt
     * 4. حجم الملف أكبر من 1 ميجابايت على الأقل
     * ترجع true فقط إذا كانت كل الشروط صحيحة.
     */
    fun isTilesDownloaded(context: Context): Boolean {
        return try {
            val valhallaDir = File(context.filesDir, "valhalla")
            if (!valhallaDir.exists() || !valhallaDir.isDirectory) return false

            val marker = File(valhallaDir, MARKER_FILE_NAME)
            val archive = File(valhallaDir, TARGET_ARCHIVE_NAME)

            val isComplete = marker.exists() && archive.exists() && archive.length() >= MIN_ARCHIVE_SIZE_BYTES
            if (isComplete) {
                // Ensure valhalla.json exists as well
                val configFile = File(valhallaDir, CONFIG_FILE_NAME)
                if (!configFile.exists()) {
                    createValhallaConfigFile(valhallaDir, archive)
                }
            }
            isComplete
        } catch (e: Exception) {
            Log.e(TAG, "isTilesDownloaded check failed safely: ${e.message}")
            false
        }
    }

    /**
     * للتوافق التام مع أي استدعاءات سابقة
     */
    fun isDownloaded(context: Context): Boolean = isTilesDownloaded(context)

    /**
     * [إصلاح ملاحة نهائي] تنزيل الحزمة وفك ضغطها وتأكيد نجاحها بدون أي Crash
     */
    suspend fun downloadAndExtractTiles(
        context: Context,
        onProgress: (Int, String) -> Unit
    ): Boolean = withContext(Dispatchers.IO) {
        try {
            val valhallaDir = File(context.filesDir, "valhalla")
            if (!valhallaDir.exists()) {
                valhallaDir.mkdirs()
            }

            // فحص مسبق: إذا كانت محملة بالفعل لا يتم تكرار التحميل
            if (isTilesDownloaded(context)) {
                onProgress(100, "خرائط اليمن محملة مسبقاً وجاهزة للعمل أوفلاين.")
                return@withContext true
            }

            val tempZipFile = File(valhallaDir, "temp_valhalla_tiles.zip")
            val targetArchive = File(valhallaDir, TARGET_ARCHIVE_NAME)
            val marker = File(valhallaDir, MARKER_FILE_NAME)

            onProgress(5, "جاري الاتصال بخادم حزمة خرائط اليمن...")

            val url = URL(DEFAULT_DOWNLOAD_URL)
            val connection = (url.openConnection() as HttpURLConnection).apply {
                connectTimeout = 30000
                readTimeout = 60000
                instanceFollowRedirects = true
                setRequestProperty("User-Agent", "YemenServicesGuide/1.0 (Android; OfflineMaps)")
            }
            connection.connect()

            val responseCode = connection.responseCode
            if (responseCode !in 200..299) {
                Log.w(TAG, "Tile download server unreachable (HTTP $responseCode), keeping existing state.")
                onProgress(0, "تعذر الوصول لخادم التحميل حالياً (رمز $responseCode). يرجى المحاولة لاحقاً.")
                return@withContext false
            }

            val fileLength = connection.contentLengthLong
            val input: InputStream = connection.inputStream
            val output = FileOutputStream(tempZipFile)

            val data = ByteArray(16384)
            var total: Long = 0
            var count: Int

            while (input.read(data).also { count = it } != -1) {
                total += count.toLong()
                if (fileLength > 0) {
                    val progress = 5 + ((total * 55) / fileLength).toInt()
                    onProgress(progress.coerceAtMost(60), "جاري تحميل حزمة الخرائط ($progress%)...")
                } else {
                    val approxMb = (total / (1024 * 1024)).toInt()
                    onProgress(35, "جاري تحميل حزمة الخرائط ($approxMb م.ب)...")
                }
                output.write(data, 0, count)
            }

            output.flush()
            output.close()
            input.close()

            onProgress(65, "جاري فك ضغط واستخراج بيانات مسارات Valhalla...")

            // فك ضغط الـ ZIP واستخراج valhalla_tiles.tar
            var foundTar = false
            try {
                ZipInputStream(tempZipFile.inputStream().buffered()).use { zis ->
                    var entry = zis.nextEntry
                    while (entry != null) {
                        val entryName = entry.name
                        if (!entry.isDirectory) {
                            if (entryName.endsWith(".tar") || entryName.contains("valhalla_tiles")) {
                                FileOutputStream(targetArchive).use { fos ->
                                    val buf = ByteArray(16384)
                                    var readBytes: Int
                                    while (zis.read(buf).also { readBytes = it } != -1) {
                                        fos.write(buf, 0, readBytes)
                                    }
                                    fos.flush()
                                }
                                foundTar = true
                            } else {
                                val destFile = File(valhallaDir, entryName)
                                destFile.parentFile?.mkdirs()
                                FileOutputStream(destFile).use { fos ->
                                    val buf = ByteArray(8192)
                                    var readBytes: Int
                                    while (zis.read(buf).also { readBytes = it } != -1) {
                                        fos.write(buf, 0, readBytes)
                                    }
                                    fos.flush()
                                }
                            }
                        }
                        zis.closeEntry()
                        entry = zis.nextEntry
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Zip extraction warning: ${e.message}")
            }

            // إذا كان الملف المحمل هو نفسه tar مضغوط بدون بنية داخلية
            if (!foundTar && (!targetArchive.exists() || targetArchive.length() < MIN_ARCHIVE_SIZE_BYTES)) {
                if (tempZipFile.exists() && tempZipFile.length() >= MIN_ARCHIVE_SIZE_BYTES) {
                    tempZipFile.copyTo(targetArchive, overwrite = true)
                }
            }

            // حذف الملف المؤقت بعد الانتهاء
            if (tempZipFile.exists()) {
                tempZipFile.delete()
            }

            if (targetArchive.exists() && targetArchive.length() >= MIN_ARCHIVE_SIZE_BYTES) {
                // إنشاء ملف الإعداد valhalla.json
                createValhallaConfigFile(valhallaDir, targetArchive)

                // إنشاء ملف العلامة الدائم فوراً بعد النجاح
                marker.writeText("DOWNLOADED_SUCCESSFULLY_${System.currentTimeMillis()}\nSIZE=${targetArchive.length()}\nURL=$DEFAULT_DOWNLOAD_URL")

                onProgress(100, "تم تثبيت حزمة خرائط اليمن وتفعيل الملاحة الأوفلاين بنجاح!")
                true
            } else {
                Log.w(TAG, "Extracted archive size invalid: ${targetArchive.length()} bytes")
                onProgress(0, "ملف الحزمة غير مكتمل، يرجى إعادة المحاولة مع اتصال مستقر.")
                false
            }
        } catch (e: Exception) {
            Log.e(TAG, "Safe catch: downloadAndExtractTiles failed: ${e.message}")
            onProgress(0, "حدث خطأ أثناء تنزيل الخرائط: ${e.localizedMessage ?: "فشل الاتصال"}")
            false
        }
    }

    /**
     * [إصلاح ملاحة نهائي] استيراد حزمة الخرائط من ملف محلي بدون استهلاك إنترنت
     */
    suspend fun importLocalTiles(
        context: Context,
        inputStream: InputStream,
        onProgress: (Int, String) -> Unit
    ): Boolean = withContext(Dispatchers.IO) {
        try {
            val valhallaDir = File(context.filesDir, "valhalla")
            if (!valhallaDir.exists()) valhallaDir.mkdirs()

            val tempFile = File(valhallaDir, "temp_import.zip")
            val targetArchive = File(valhallaDir, TARGET_ARCHIVE_NAME)
            val marker = File(valhallaDir, MARKER_FILE_NAME)

            onProgress(10, "جاري قراءة الحزمة المحلية...")
            FileOutputStream(tempFile).use { fos ->
                val buf = ByteArray(16384)
                var r: Int
                while (inputStream.read(buf).also { r = it } != -1) {
                    fos.write(buf, 0, r)
                }
                fos.flush()
            }

            onProgress(60, "جاري استخراج بيانات الحزمة...")
            var foundTar = false
            try {
                ZipInputStream(tempFile.inputStream().buffered()).use { zis ->
                    var entry = zis.nextEntry
                    while (entry != null) {
                        if (!entry.isDirectory && (entry.name.endsWith(".tar") || entry.name.contains("valhalla_tiles"))) {
                            FileOutputStream(targetArchive).use { fos ->
                                val buf = ByteArray(16384)
                                var b: Int
                                while (zis.read(buf).also { b = it } != -1) {
                                    fos.write(buf, 0, b)
                                }
                                fos.flush()
                            }
                            foundTar = true
                        }
                        zis.closeEntry()
                        entry = zis.nextEntry
                    }
                }
            } catch (_: Exception) {}

            if (!foundTar && tempFile.length() >= MIN_ARCHIVE_SIZE_BYTES) {
                tempFile.copyTo(targetArchive, overwrite = true)
            }
            if (tempFile.exists()) tempFile.delete()

            if (targetArchive.exists() && targetArchive.length() >= MIN_ARCHIVE_SIZE_BYTES) {
                createValhallaConfigFile(valhallaDir, targetArchive)
                marker.writeText("IMPORTED_LOCAL_${System.currentTimeMillis()}\nSIZE=${targetArchive.length()}")
                onProgress(100, "تم استيراد حزمة الخرائط وتفعيل الملاحة الأوفلاين بنجاح!")
                true
            } else {
                onProgress(0, "الحزمة المحلية غير صالحة أو حجمها أقل من المطلوب.")
                false
            }
        } catch (e: Exception) {
            Log.e(TAG, "importLocalTiles error: ${e.message}")
            onProgress(0, "فشل استيراد الحزمة المحلية.")
            false
        }
    }

    private fun createValhallaConfigFile(valhallaDir: File, archive: File) {
        try {
            val configFile = File(valhallaDir, CONFIG_FILE_NAME)
            val jsonContent = """
            {
              "mjolnir": {
                "tile_dir": "${valhallaDir.absolutePath}",
                "tile_extract": "${archive.absolutePath}",
                "admin": "${valhallaDir.absolutePath}/admin.sqlite"
              }
            }
            """.trimIndent()
            configFile.writeText(jsonContent)
        } catch (e: Exception) {
            Log.w(TAG, "Could not write valhalla.json: ${e.message}")
        }
    }
}
