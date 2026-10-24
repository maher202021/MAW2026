package com.serviseyem.sync

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.serviseyem.utils.FullSyncManager
import com.serviseyem.utils.OfflineQueueManager
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * 🔄 SyncWorker - عامل مزامنة البيانات المحلية وسجلات الحجوزات في الخلفية بذكاء لتوفير الإنترنت
 */
class SyncWorker(
    context: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(context, workerParams) {

    @EntryPoint
    @InstallIn(SingletonComponent::class)
    interface SyncWorkerEntryPoint {
        fun fullSyncManager(): FullSyncManager
        fun offlineQueueManager(): OfflineQueueManager
    }

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        if (!com.serviseyem.NetworkUtils.isNetworkAvailable(applicationContext)) {
            Log.d(TAG, "No validated network connection available, deferring background sync")
            return@withContext if (runAttemptCount < MAX_RETRY_ATTEMPTS) {
                Result.retry()
            } else {
                Result.failure()
            }
        }

        return@withContext try {
            val (syncManager, offlineQueue) = try {
                val entryPoint = EntryPointAccessors.fromApplication(
                    applicationContext,
                    SyncWorkerEntryPoint::class.java
                )
                entryPoint.fullSyncManager() to entryPoint.offlineQueueManager()
            } catch (e: Exception) {
                if (e is kotlinx.coroutines.CancellationException) throw e
                FullSyncManager(applicationContext) to OfflineQueueManager(applicationContext)
            }

            // 1. سحب أحدث الإعدادات من السحابة إلى الكاش المحلي بأمان (دون الكتابة الفوقية على Firestore)
            syncManager.pullRemoteSettingsToLocalCache()

            // 2. معالجة طابور وضع عدم الاتصال (Offline Queue) والانتظار الفعلي حتى اكتمال الإرسال
            val queueSuccess = offlineQueue.processQueueSuspend()

            // 3. تنظيف الكاش المحلي والرسائل القديمة (>30 يوماً) لتوفير الذاكرة وسعة الجهاز

            if (queueSuccess) {
                Result.success()
            } else {
                if (runAttemptCount < MAX_RETRY_ATTEMPTS) {
                    Result.retry()
                } else {
                    Result.failure()
                }
            }
        } catch (e: kotlinx.coroutines.CancellationException) {
            throw e
        } catch (e: Exception) {
            Log.e(TAG, "Background sync worker failed: ${e.message}", e)
            if (runAttemptCount < MAX_RETRY_ATTEMPTS) {
                Result.retry()
            } else {
                Result.failure()
            }
        }
    }

    companion object {
        private const val TAG = "SyncWorker"
        private const val MAX_RETRY_ATTEMPTS = 3
        const val WORK_NAME = "periodic_sync_work"
    }
}

