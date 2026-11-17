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
    fun isOfflineDataAvailable(): Boolean = com.serviseyem.utils.ValhallaDownloadManager.isDownloaded(context)

    fun startDownload(forceRedownload: Boolean = false) {
        if (downloadJob?.isActive == true) return

        downloadJob = scope.launch {
            _downloadState.value = DownloadState.CheckingLocalFiles
            val archive = com.serviseyem.utils.ValhallaDownloadManager.getTilesArchive(context)
            if (!forceRedownload && com.serviseyem.utils.ValhallaDownloadManager.isDownloaded(context)) {
                _downloadState.value = DownloadState.Completed(archive.absolutePath, archive.length())
                return@launch
            }
            val ok = com.serviseyem.utils.ValhallaDownloadManager.downloadAndExtractOfflineTiles(context) { prog, msg ->
                val pct = (prog * 100).toInt().coerceIn(0, 100)
                if (prog >= 0.72f && prog < 1.0f) {
                    _downloadState.value = DownloadState.Extracting(pct, msg)
                } else if (prog > 0f && prog < 1.0f) {
                    _downloadState.value = DownloadState.Downloading(pct, 0L, -1L)
                }
            }
            _downloadState.value = if (ok) {
                val installed = com.serviseyem.utils.ValhallaDownloadManager.getTilesArchive(context)
                DownloadState.Completed(installed.absolutePath, installed.length())
            } else {
                DownloadState.Error("تعذر تحميل أو تثبيت حزمة الملاحة")
            }
        }
    }

    fun cancelDownload() {
        downloadJob?.cancel()
        _downloadState.value = DownloadState.Idle
    }
}
