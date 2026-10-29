package com.serviseyem.navigation

// [إصلاح ملاحة نهائي] إدارة دورة حياة الخرائط الأوفلاين والتكامل مع ValhallaTileDownloader
import android.content.Context
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.io.File

/**
 * 🛰️ OfflineMapLifecycleManager
 * مكوّن إدارة دورة حياة تنزيل بيانات الخرائط غير المتصلة (أوفلاين)
 * - يتحقق أولاً من الملفات الموجودة في التخزين الداخلي قبل إجراء أي اتصال بالشبكة لمنع استهلاك البيانات.
 * - يدير حالات التنزيل عبر StateFlow (IDLE, CHECKING, DOWNLOADING, EXTRACTING, COMPLETED, ERROR).
 * - تسجيل ملف العلامة الدائم `is_valhalla_downloaded.txt`.
 */
class OfflineMapLifecycleManager private constructor(private val context: Context) {

    companion object {
        const val DOWNLOAD_URL = ValhallaTileDownloader.DEFAULT_DOWNLOAD_URL
        const val VALHALLA_DIR = "valhalla"
        const val MARKER_FILE = ValhallaTileDownloader.MARKER_FILE_NAME
        const val TILES_ARCHIVE = ValhallaTileDownloader.TARGET_ARCHIVE_NAME

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
     * [إصلاح ملاحة نهائي] فحص التخزين الداخلي بدقة عبر ValhallaTileDownloader
     */
    fun isOfflineDataAvailable(): Boolean {
        return ValhallaTileDownloader.isTilesDownloaded(context)
    }

    /**
     * [إصلاح ملاحة نهائي] بدء التحميل بأمان عند طلب المستخدم
     */
    fun startDownload(forceRedownload: Boolean = false) {
        if (downloadJob?.isActive == true) return

        downloadJob = scope.launch {
            _downloadState.value = DownloadState.CheckingLocalFiles

            // التحقق المسبق الصارم: إذا كانت الحزمة محملة مسبقاً لا يتم التنزيل مرة أخرى أبداً
            if (!forceRedownload && ValhallaTileDownloader.isTilesDownloaded(context)) {
                val targetArchive = File(context.filesDir, "$VALHALLA_DIR/$TILES_ARCHIVE")
                _downloadState.value = DownloadState.Completed(
                    archivePath = targetArchive.absolutePath,
                    fileSize = targetArchive.length()
                )
                return@launch
            }

            _downloadState.value = DownloadState.Downloading(progressPercent = 5, bytesDownloaded = 0, totalBytes = -1)

            val success = ValhallaTileDownloader.downloadAndExtractTiles(context) { percent, msg ->
                if (percent in 5..60) {
                    _downloadState.value = DownloadState.Downloading(percent, percent.toLong() * 1024L * 1024L, 100L * 1024L * 1024L)
                } else if (percent > 60 && percent < 100) {
                    _downloadState.value = DownloadState.Extracting(percent, msg)
                }
            }

            if (success) {
                val targetArchive = File(context.filesDir, "$VALHALLA_DIR/$TILES_ARCHIVE")
                _downloadState.value = DownloadState.Completed(targetArchive.absolutePath, targetArchive.length())
            } else {
                _downloadState.value = DownloadState.Error("تعذر إكمال تحميل الحزمة. يمكنك الاستمرار في الملاحة أونلاين.")
            }
        }
    }

    fun cancelDownload() {
        downloadJob?.cancel()
        _downloadState.value = DownloadState.Idle
    }
}
