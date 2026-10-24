package com.serviseyem.data.repositories

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Build
import android.util.Base64
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream

/**
 * ⚡ Firebase Optimization Architecture
 * Optimization strategy to stay strictly within Firebase Spark Plan limits:
 * - 50,000 reads/day
 * - 20,000 writes/day
 * - 1,000 deletes/day
 * 
 * Features:
 * 1. Pagination: 15-20 msgs/batch, 10-15 bookings/batch, 10 urgent requests/batch.
 * 2. TTL Caching: Channels (5m), Messages (30s), Bookings (5m), Urgent Requests (30s).
 * 3. Delta Sync & Batch Writes.
 * 4. Image Compression: WebP format under 150KB.
 */
object FirebaseOptimizationManager {

    // Pagination constants
    const val CHAT_PAGE_SIZE = 15
    const val BOOKING_PAGE_SIZE = 10
    const val URGENT_REQUEST_PAGE_SIZE = 10

    // TTL Cache durations (milliseconds)
    const val TTL_CHANNELS_MS = 5 * 60 * 1000L      // 5 minutes
    const val TTL_MESSAGES_MS = 30 * 1000L          // 30 seconds
    const val TTL_BOOKINGS_MS = 5 * 60 * 1000L      // 5 minutes
    const val TTL_URGENT_MS = 30 * 1000L            // 30 seconds

    /**
     * Helper to verify if cache is still valid based on TTL
     */
    fun isCacheValid(lastSyncedAt: Long, ttlMillis: Long): Boolean {
        if (lastSyncedAt <= 0L || ttlMillis <= 0L) return false
        val elapsed = System.currentTimeMillis() - lastSyncedAt
        return elapsed in 0 until ttlMillis
    }

    /**
     * Compress bitmap to target WebP byte array (< 150KB) via FirebaseStorageUploader
     */
    suspend fun compressImageToWebPBytes(bitmap: Bitmap, maxSizeBytes: Long = 150 * 1024L): ByteArray = withContext(Dispatchers.IO) {
        com.serviseyem.utils.FirebaseStorageUploader.compressBitmapToBytes(
            bitmap = bitmap,
            maxDimension = 800,
            maxSizeBytes = maxSizeBytes
        )
    }

    /**
     * Upload bitmap directly to Firebase Storage via FirebaseStorageUploader
     */
    suspend fun uploadOptimizedBitmap(
        bitmap: Bitmap,
        storagePath: String,
        maxSizeBytes: Long = 150 * 1024L
    ): Result<String> = withContext(Dispatchers.IO) {
        com.serviseyem.utils.FirebaseStorageUploader.uploadBitmap(
            bitmap = bitmap,
            storagePath = storagePath,
            maxDimension = 800,
            maxSizeBytes = maxSizeBytes
        )
    }

    /**
     * Generic pagination helper for lists
     */
    fun <T> paginateList(sourceList: List<T>, pageIndex: Int, pageSize: Int = CHAT_PAGE_SIZE): List<T> {
        if (sourceList.isEmpty() || pageIndex < 0 || pageSize <= 0) return emptyList()
        val fromIndex = pageIndex.toLong() * pageSize.toLong()
        if (fromIndex >= sourceList.size) return emptyList()
        val start = fromIndex.toInt()
        val toIndex = kotlin.math.min(start + pageSize, sourceList.size)
        return sourceList.subList(start, toIndex)
    }
}
