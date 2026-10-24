package com.serviseyem.utils

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.net.URL
import java.util.zip.ZipInputStream

object ValhallaDownloadManager {

    suspend fun downloadAndExtractOfflineTiles(
        context: Context,
        customUrl: String = "",
        onProgress: (Float, String) -> Unit
    ): Boolean = withContext(Dispatchers.IO) {
        try {
            val defaultUrl = "https://github.com/maher202021/Vela2026/releases/download/yemen-valhalla-1/yemen_valhalla_tiles.zip"
            val downloadUrl = if (customUrl.isNotBlank()) customUrl else defaultUrl

            onProgress(0.1f, "جاري الاتصال بسيرفر الخرائط...")
            val url = URL(downloadUrl)
            val connection = url.openConnection()
            connection.connect()
            val length = connection.contentLength

            val input = connection.getInputStream()
            val zipFile = File(context.filesDir, "yemen_valhalla_tiles.zip")
            val output = FileOutputStream(zipFile)

            val data = ByteArray(4096)
            var total: Long = 0
            var count: Int

            while (input.read(data).also { count = it } != -1) {
                total += count.toLong()
                output.write(data, 0, count)
                if (length > 0) {
                    val progress = 0.1f + (total.toFloat() / length.toFloat()) * 0.6f
                    onProgress(progress, "جاري التحميل (${(progress * 100).toInt()}%)...")
                }
            }
            output.flush()
            output.close()
            input.close()

            onProgress(0.75f, "جاري فك ضغط خرائط الملاحة...")
            
            // Unzip file
            val zipInputStream = ZipInputStream(zipFile.inputStream())
            var zipEntry = zipInputStream.nextEntry
            val targetDir = context.filesDir

            while (zipEntry != null) {
                val newFile = File(targetDir, zipEntry.name)
                if (zipEntry.isDirectory) {
                    newFile.mkdirs()
                } else {
                    newFile.parentFile?.mkdirs()
                    val fos = FileOutputStream(newFile)
                    var len: Int
                    while (zipInputStream.read(data).also { len = it } > 0) {
                        fos.write(data, 0, len)
                    }
                    fos.close()
                }
                zipInputStream.closeEntry()
                zipEntry = zipInputStream.nextEntry
            }
            zipInputStream.close()

            // Delete zip after extraction to save space
            if (zipFile.exists()) {
                zipFile.delete()
            }

            // Create verification marker file
            val marker = File(context.filesDir, "is_valhalla_downloaded.txt")
            marker.writeText("SUCCESS: Valhalla offline navigation downloaded successfully at ${System.currentTimeMillis()}")

            onProgress(1.0f, "اكتمل تنزيل وتفعيل الملاحة الدقيقة بنجاح ✓")
            true
        } catch (e: Exception) {
            e.printStackTrace()
            onProgress(0.0f, "فشل التحميل: ${e.localizedMessage ?: "خطأ في الاتصال"}")
            false
        }
    }
}
