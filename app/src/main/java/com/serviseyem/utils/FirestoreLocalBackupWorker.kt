package com.serviseyem.utils

import com.serviseyem.utils.*

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import java.io.File
import java.io.FileOutputStream
import java.util.Locale
import java.util.concurrent.TimeUnit

/**
 * WorkManager worker that performs daily local encrypted backups (AES-256)
 * of offline Firestore persistent documents.
 */
class FirestoreLocalBackupWorker(
    private val context: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result {
        return try {
            Log.d(TAG, "Starting daily local encrypted backup of Firestore cache...")
            val db = FirebaseFirestore.getInstance()

            val backupSb = StringBuilder()
            backupSb.append("{\n\"timestamp\": \"${System.currentTimeMillis()}\",\n\"collections\": {\n")

            val collectionsToBackup = listOf(
                "providers", "stores", "properties", "bookings",
                "registered_users", "settings", "chats"
            )

            var isFirstCol = true
            // TODO: تطبيق التصفح بالصفحات (Pagination) بدلاً من جلب المجموعة كاملة دفعة واحدة لتفادي استهلاك الذاكرة مع نمو البيانات
            for (colName in collectionsToBackup) {
                try {
                    val snapshot = db.collection(colName).limit(300).get().await()
                    if (!isFirstCol) backupSb.append(",\n")
                    isFirstCol = false

                    backupSb.append("  \"$colName\": [")
                    var isFirstDoc = true
                    for (doc in snapshot.documents) {
                        if (!isFirstDoc) backupSb.append(",")
                        isFirstDoc = false
                        val dataJson = doc.data?.entries?.joinToString(prefix = "{", postfix = "}") {
                            "\"${it.key}\": \"${it.value.toString().replace("\"", "\\\"")}\""
                        } ?: "{}"
                        backupSb.append(dataJson)
                    }
                    backupSb.append("]")
                } catch (e: Exception) {
                    Log.e(TAG, "Failed backing up collection $colName", e)
                }
            }

            backupSb.append("\n}\n}")

            val rawBackupString = backupSb.toString()
            val encryptedData = SecurityCryptoUtils.encrypt(rawBackupString)

            val backupDir = File(context.filesDir, "encrypted_backups")
            if (!backupDir.exists()) {
                backupDir.mkdirs()
            }

            // ✨ م2: استخدام DateFormatter
            val timestampStr = DateFormatter.formatCustom(System.currentTimeMillis(), "yyyyMMdd_HHmmss")
            val backupFile = File(backupDir, "wam_firestore_backup_$timestampStr.enc")

            FileOutputStream(backupFile).use { fos ->
                fos.write(encryptedData.toByteArray(Charsets.UTF_8))
            }

            // Prune old backups to keep only the 7 most recent
            val existingBackups = backupDir.listFiles { _, name -> name.startsWith("wam_firestore_backup_") && name.endsWith(".enc") }
            if (existingBackups != null && existingBackups.size > 7) {
                existingBackups.sortBy { it.lastModified() }
                val toDeleteCount = existingBackups.size - 7
                for (i in 0 until toDeleteCount) {
                    existingBackups[i].delete()
                }
            }

            Log.d(TAG, "Daily encrypted backup created successfully: ${backupFile.absolutePath}, size=${backupFile.length()} bytes")
            Result.success()
        } catch (e: Exception) {
            Log.e(TAG, "Error performing Firestore local backup", e)
            Result.retry()
        }
    }

    companion object {
        private const val TAG = "FirestoreBackupWorker"
        private const val WORK_NAME = "DailyFirestoreLocalEncryptedBackup"

        fun schedulePeriodicBackup(context: Context) {
            val backupRequest = PeriodicWorkRequestBuilder<FirestoreLocalBackupWorker>(24, TimeUnit.HOURS)
                .setBackoffCriteria(androidx.work.BackoffPolicy.EXPONENTIAL, 15, TimeUnit.MINUTES)
                .build()

            WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                WORK_NAME,
                androidx.work.ExistingPeriodicWorkPolicy.KEEP,
                backupRequest
            )
            Log.d(TAG, "Scheduled daily 24-hour encrypted local backup worker.")
        }
    }
}
