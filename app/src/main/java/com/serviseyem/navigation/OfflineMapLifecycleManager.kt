package com.serviseyem.navigation

import android.content.Context
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.net.HttpURLConnection
import java.net.URL
import java.util.zip.ZipInputStream

/**
 * 🛰️ OfflineMapLifecycleManager
 * مكوّن إدارة دورة حياة تنزيل بيانات الخرائط غير المتصلة (أوفلاين)
 * - يتحقق أولاً من الملفات الموجودة في التخزين الداخلي قبل إجراء أي اتصال بالشبكة لمنع استهلاك البيانات.
 * - يدير حالات التنزيل عبر StateFlow (IDLE, CHECKING, DOWNLOADING, EXTRACTING, COMPLETED, ERROR).
 * - فك ضغط وحفظ الحزمة في التخزين الداخلي للتطبيق (Internal Storage).
 * - تسجيل ملف العلامة الدائم `is_valhalla_downloaded.txt`.
 */
class OfflineMapLifecycleManager private constructor(private val context: Context) {

    companion object {
        const val DOWNLOAD_URL = "https://github.com/maher202021/Vela2026/releases/download/yemen-valhalla-1/yemen_valhalla_tiles.zip"
        const val VALHALLA_DIR = "valhalla"
        const val MARKER_FILE = "is_valhalla_downloaded.txt"
        const val TILES_ARCHIVE = "valhalla_tiles.tar"

        @Volatile
        private var INSTANCE: OfflineMapLifecycleManager? = null

        fun getInstance(context: Context): OfflineMapLifecycleManager {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: OfflineMapLifecycleManager(context.applicationContext).also { INSTANCE = it }
            }
        }
    }

    sealed class DownloadState {
        object Idle : DownloadState()
        object CheckingLocalFiles : DownloadState()
        data class Downloading(val progressPercent: Int, val bytesDownloaded: Long, val totalBytes: Long) : DownloadState()
        data class Extracting(val progressPercent: Int, val currentFile: String) : DownloadState()
        data class Completed(val archivePath: String, val fileSize: Long) : DownloadState()
        data class Error(val errorMessage: String) : DownloadState()
    }

    private val _downloadState = MutableStateFlow<DownloadState>(DownloadState.Idle)
    val downloadState: StateFlow<DownloadState> = _downloadState.asStateFlow()

    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private var downloadJob: Job? = null

    /**
     * فحص التخزين الداخلي بدقة قبل استهلاك أي اتصال بالشبكة
     */
    fun isOfflineDataAvailable(): Boolean {
        val dir = File(context.filesDir, VALHALLA_DIR)
        val marker = File(dir, MARKER_FILE)
        val archive = File(dir, TILES_ARCHIVE)
        return marker.exists() && archive.exists() && archive.length() >= 10_000_000L
    }

    /**
     * بدء عملية التحميل مع التحقق المسبق من وجود الملفات لتوفير بيانات الإنترنت
     */
    fun startDownload(forceRedownload: Boolean = false) {
        if (downloadJob?.isActive == true) return

        downloadJob = scope.launch {
            _downloadState.value = DownloadState.CheckingLocalFiles

            val targetDir = File(context.filesDir, VALHALLA_DIR)
            if (!targetDir.exists()) {
                targetDir.mkdirs()
            }

            val marker = File(targetDir, MARKER_FILE)
            val targetArchive = File(targetDir, TILES_ARCHIVE)

            // التحقق المسبق لمنع أي تنزيل غير ضروري
            if (!forceRedownload && marker.exists() && targetArchive.exists() && targetArchive.length() >= 10_000_000L) {
                _downloadState.value = DownloadState.Completed(
                    archivePath = targetArchive.absolutePath,
                    fileSize = targetArchive.length()
                )
                return@launch
            }

            val tempZipFile = File(targetDir, "temp_tiles_download.zip")

            try {
                _downloadState.value = DownloadState.Downloading(progressPercent = 0, bytesDownloaded = 0, totalBytes = -1)

                val url = URL(DOWNLOAD_URL)
                val connection = (url.openConnection() as HttpURLConnection).apply {
                    connectTimeout = 20000
                    readTimeout = 60000
                    instanceFollowRedirects = true
                    setRequestProperty("User-Agent", "YemenServicesGuide/1.2.0 (Android)")
                }
                connection.connect()

                val responseCode = connection.responseCode
                if (responseCode !in 200..299) {
                    _downloadState.value = DownloadState.Error("تعذر الاتصال بالخادم لتحميل الخرائط (رمز الاستجابة $responseCode).")
                    return@launch
                }

                val totalLength = connection.contentLength.toLong()
                val input: InputStream = connection.inputStream
                val output = FileOutputStream(tempZipFile)

                val buffer = ByteArray(16384)
                var bytesRead: Int
                var totalRead: Long = 0

                while (input.read(buffer).also { bytesRead = it } != -1) {
                    if (!isActive) {
                        output.close()
                        input.close()
                        tempZipFile.delete()
                        _downloadState.value = DownloadState.Idle
                        return@launch
                    }
                    output.write(buffer, 0, bytesRead)
                    totalRead += bytesRead
                    val percent = if (totalLength > 0) ((totalRead * 60) / totalLength).toInt() else 30
                    _downloadState.value = DownloadState.Downloading(percent, totalRead, totalLength)
                }

                output.flush()
                output.close()
                input.close()

                // فك الضغط في التخزين الداخلي مع حماية Zip Slip
                _downloadState.value = DownloadState.Extracting(65, "جاري فك ضغط حزم الخرائط...")

                ZipInputStream(tempZipFile.inputStream().buffered()).use { zis ->
                    var entry = zis.nextEntry
                    while (entry != null) {
                        if (!isActive) break
                        val entryName = entry.name
                        _downloadState.value = DownloadState.Extracting(80, entryName)

                        val destFile = if (entryName.endsWith(".tar") || entryName.contains("valhalla_tiles")) {
                            targetArchive
                        } else {
                            File(targetDir, entryName)
                        }

                        // 🛡️ فحص حماية Zip Slip
                        val canonicalDest = destFile.canonicalPath
                        val canonicalDir = targetDir.canonicalPath + File.separator
                        if (!canonicalDest.startsWith(canonicalDir) && destFile != targetArchive) {
                            zis.closeEntry()
                            entry = zis.nextEntry
                            continue
                        }

                        if (!entry.isDirectory) {
                            destFile.parentFile?.mkdirs()
                            FileOutputStream(destFile).use { fos ->
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

                if (tempZipFile.exists()) tempZipFile.delete()

                // التحقق من أن حجم ملف الـ TAR أكبر من 10 ميجابايت فعلاً
                if (!targetArchive.exists() || targetArchive.length() < 10_000_000L) {
                    if (targetArchive.exists()) targetArchive.delete()
                    if (marker.exists()) marker.delete()
                    _downloadState.value = DownloadState.Error("حجم حزمة الملاحة غير مكتمل (${targetArchive.length()} بايت) - يجب ألا يقل عن 10MB.")
                    return@launch
                }

                // كتابة العلامة الحقيقية فقط بعد التحقق
                marker.writeText("OFFLINE_READY_${System.currentTimeMillis()}\nSIZE=${targetArchive.length()}")

                _downloadState.value = DownloadState.Completed(targetArchive.absolutePath, targetArchive.length())

            } catch (e: Exception) {
                e.printStackTrace()
                if (tempZipFile.exists()) tempZipFile.delete()
                if (targetArchive.exists() && targetArchive.length() < 10_000_000L) targetArchive.delete()
                if (marker.exists()) marker.delete()
                _downloadState.value = DownloadState.Error("فشل تحميل بيانات الملاحة: ${e.localizedMessage ?: "خطأ غير متوقع"}")
            }
        }
    }

    fun cancelDownload() {
        downloadJob?.cancel()
        _downloadState.value = DownloadState.Idle
    }
}
