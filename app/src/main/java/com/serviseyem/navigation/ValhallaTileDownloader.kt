package com.serviseyem.navigation

import android.content.Context
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
 * مدير تحميل وفك ضغط حزم خرائط ومسارات Valhalla الحقيقية لليمن:
 * - التحميل من: https://github.com/maher202021/Vela2026/releases/download/yemen-valhalla-1/yemen_valhalla_tiles.zip
 * - الحفظ في: File(context.filesDir, "valhalla/valhalla_tiles.tar")
 * - علامة النجاح الدائمة: File(context.filesDir, "valhalla/is_valhalla_downloaded.txt")
 * - حماية كاملة من الـ Zip Slip ومنع إنشاء ملفات فارغة أو تالفة.
 * - عند وجود العلامة والملف بحجم سليم (> 10MB) لا يُطلب التحميل إطلاقاً.
 */
object ValhallaTileDownloader {

    const val DOWNLOAD_URL = "https://github.com/maher202021/Vela2026/releases/download/yemen-valhalla-1/yemen_valhalla_tiles.zip"
    const val MARKER_FILE_NAME = "is_valhalla_downloaded.txt"
    const val TARGET_ARCHIVE_NAME = "valhalla_tiles.tar"
    const val MIN_VALID_SIZE_BYTES = 10_000_000L // 10 ميجابايت كحد أدنى إلزامي للملف الحقيقي

    /**
     * فحص حقيقي لملف الخرائط والماركر:
     * يفتح الملاحة فوراً ولا يطلب التحميل إلا إذا كان الملف محذوفاً أو حجمه غير صالح.
     */
    fun isDownloaded(context: Context): Boolean {
        val valhallaDir = File(context.filesDir, "valhalla")
        val marker = File(valhallaDir, MARKER_FILE_NAME)
        val tarFile = File(valhallaDir, TARGET_ARCHIVE_NAME)
        return marker.exists() && tarFile.exists() && tarFile.length() >= MIN_VALID_SIZE_BYTES
    }

    /**
     * تحميل وفك ضغط حقيقي لحزمة الشوارع، وكتابة علامة النجاح فقط بعد التحقق الصارم 100%.
     */
    suspend fun downloadAndExtractTiles(
        context: Context,
        onProgress: (Int, String) -> Unit
    ): Boolean = withContext(Dispatchers.IO) {
        val valhallaDir = File(context.filesDir, "valhalla")
        if (!valhallaDir.exists()) {
            valhallaDir.mkdirs()
        }

        val markerFile = File(valhallaDir, MARKER_FILE_NAME)
        val tarFile = File(valhallaDir, TARGET_ARCHIVE_NAME)

        // التحقق المسبق: إذا كانت محملة وسليمة لا يُستهلك أي نت
        if (tarFile.exists() && tarFile.length() >= MIN_VALID_SIZE_BYTES && markerFile.exists()) {
            onProgress(100, "خرائط اليمن محملة مسبقاً وجاهزة للعمل أوفلاين بنسبة 100%.")
            return@withContext true
        }

        val tempZipFile = File(valhallaDir, "temp_yemen_valhalla_tiles.zip")

        try {
            onProgress(5, "جاري الاتصال بخادم حزم شوارع اليمن...")

            val url = URL(DOWNLOAD_URL)
            val connection = (url.openConnection() as HttpURLConnection).apply {
                connectTimeout = 30000
                readTimeout = 90000
                instanceFollowRedirects = true
                setRequestProperty("User-Agent", "Serviseyem-App/1.2.0 (Android)")
            }
            connection.connect()

            val responseCode = connection.responseCode
            if (responseCode !in 200..299) {
                throw Exception("فشل الاتصال بخادم الخرائط: رمز الاستجابة $responseCode")
            }

            val fileLength = connection.contentLength.toLong()
            val input: InputStream = connection.inputStream
            val output = FileOutputStream(tempZipFile)

            val data = ByteArray(16384)
            var total: Long = 0
            var count: Int

            while (input.read(data).also { count = it } != -1) {
                total += count.toLong()
                output.write(data, 0, count)
                if (fileLength > 0) {
                    val progress = 5 + ((total * 55) / fileLength).toInt()
                    onProgress(progress.coerceAtMost(60), "جاري تحميل بيانات الشوارع (${(total / (1024 * 1024))} MB)...")
                } else {
                    onProgress(35, "جاري استقبال حزمة الشوارع (${(total / (1024 * 1024))} MB)...")
                }
            }

            output.flush()
            output.close()
            input.close()
            connection.disconnect()

            // التأكد من أن حجم الـ zip المحمل أكبر من 10 ميجابايت
            if (tempZipFile.length() < MIN_VALID_SIZE_BYTES) {
                tempZipFile.delete()
                throw Exception("الملف المحمل غير مكتمل أو تالف (${tempZipFile.length()} بايت)، تحقق من اتصال الإنترنت.")
            }

            onProgress(65, "جاري فك ضغط واستخراج حزمة مسارات Valhalla بأمان...")

            // استخراج محتوى الـ zip مع حماية Zip Slip الصارمة
            val canonicalBaseDir = valhallaDir.canonicalPath + File.separator
            ZipInputStream(tempZipFile.inputStream().buffered()).use { zis ->
                var entry = zis.nextEntry
                while (entry != null) {
                    val entryName = entry.name
                    val destinationFile = if (entryName.endsWith(".tar") || entryName.contains("valhalla_tiles")) {
                        tarFile
                    } else {
                        File(valhallaDir, entryName)
                    }

                    // 🛡️ حماية Zip Slip الصارمة لمنع الاختراق
                    val canonicalDest = destinationFile.canonicalPath
                    if (!canonicalDest.startsWith(canonicalBaseDir) && destinationFile != tarFile) {
                        zis.closeEntry()
                        entry = zis.nextEntry
                        continue
                    }

                    if (!entry.isDirectory) {
                        destinationFile.parentFile?.mkdirs()
                        FileOutputStream(destinationFile).use { fos ->
                            val buf = ByteArray(16384)
                            var len: Int
                            while (zis.read(buf).also { len = it } != -1) {
                                fos.write(buf, 0, len)
                            }
                            fos.flush()
                        }
                    }
                    zis.closeEntry()
                    entry = zis.nextEntry
                }
            }

            // تنظيف الملف المؤقت
            if (tempZipFile.exists()) {
                tempZipFile.delete()
            }

            // التأكد من أن الملف النهائي موجود وحجمه منطقي (أكبر من 10 ميجابايت)
            if (tarFile.exists() && tarFile.length() >= MIN_VALID_SIZE_BYTES) {
                // كتابة علامة النجاح الصريحة
                markerFile.writeText("SUCCESS:${System.currentTimeMillis()}\nSIZE=${tarFile.length()}")
                onProgress(100, "تم إعداد وتجهيز خرائط Valhalla لليمن بنجاح تام! (${tarFile.length() / (1024 * 1024)} MB)")
                true
            } else {
                if (tarFile.exists()) tarFile.delete()
                if (markerFile.exists()) markerFile.delete()
                throw Exception("Extraction failed or file corrupted")
            }
        } catch (e: Exception) {
            e.printStackTrace()
            if (tempZipFile.exists()) tempZipFile.delete()
            if (tarFile.exists() && tarFile.length() < MIN_VALID_SIZE_BYTES) tarFile.delete()
            if (markerFile.exists()) markerFile.delete()
            onProgress(0, "فشل التحميل: ${e.localizedMessage ?: "تحقق من اتصال الإنترنت"}")
            false
        }
    }
}
