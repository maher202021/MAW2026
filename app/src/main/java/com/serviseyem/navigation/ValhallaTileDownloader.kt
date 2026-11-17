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
    fun isDownloaded(context: Context): Boolean =
        com.serviseyem.utils.ValhallaDownloadManager.isDownloaded(context)

    suspend fun downloadAndExtractTiles(
        context: Context,
        onProgress: (Int, String) -> Unit
    ): Boolean = com.serviseyem.utils.ValhallaDownloadManager.downloadAndExtractOfflineTiles(context) { p, msg ->
        onProgress((p * 100).toInt().coerceIn(0, 100), msg)
    }
}
