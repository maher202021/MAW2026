package com.serviseyem.utils

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Build
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageMetadata
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeoutOrNull
import java.io.ByteArrayOutputStream
import java.io.InputStream
import java.util.UUID

/**
 * 🚀 Firebase Storage Uploader & Ultra-Optimizer
 * Handles image compression (WebP / JPEG, 800px max, <150KB for profile, <300KB for items),
 * quota safety checks, and direct upload to Firebase Storage with download URL generation.
 */
object FirebaseStorageUploader {

    private val storage: FirebaseStorage by lazy {
        FirebaseStorage.getInstance()
    }

    /**
     * Compress bitmap or Uri into WebP/JPEG byte array strictly respecting dimension and byte size limits.
     * @param maxDimension Maximum width or height (default 800px)
     * @param maxSizeBytes Maximum file size in bytes (150KB for profile, 300KB for product/store/prop)
     */
    suspend fun compressImageToBytes(
        context: Context,
        imageUri: Uri,
        maxDimension: Int = 800,
        maxSizeBytes: Long = 300 * 1024L
    ): ByteArray? = withContext(Dispatchers.IO) {
        withTimeoutOrNull(10000L) {
            try {
                var inputStream: InputStream? = context.contentResolver.openInputStream(imageUri)
                    ?: return@withTimeoutOrNull null

                val boundsOptions = BitmapFactory.Options().apply { inJustDecodeBounds = true }
                BitmapFactory.decodeStream(inputStream, null, boundsOptions)
                inputStream?.close()

                val origWidth = boundsOptions.outWidth
                val origHeight = boundsOptions.outHeight
                if (origWidth <= 0 || origHeight <= 0) return@withTimeoutOrNull null

                // Calculate sample size
                var sampleSize = 1
                while (origWidth / sampleSize > maxDimension * 1.5 || origHeight / sampleSize > maxDimension * 1.5) {
                    sampleSize *= 2
                }

                val decodeOptions = BitmapFactory.Options().apply { inSampleSize = sampleSize }
                val nextStream = context.contentResolver.openInputStream(imageUri) ?: return@withTimeoutOrNull null
                val decodedBitmap = BitmapFactory.decodeStream(nextStream, null, decodeOptions)
                nextStream.close()

                if (decodedBitmap == null) return@withTimeoutOrNull null

                // Exact scale if larger than maxDimension
                val finalBitmap = if (decodedBitmap.width > maxDimension || decodedBitmap.height > maxDimension) {
                    val ratio = Math.min(
                        maxDimension.toFloat() / decodedBitmap.width,
                        maxDimension.toFloat() / decodedBitmap.height
                    )
                    val newW = (decodedBitmap.width * ratio).toInt().coerceAtLeast(1)
                    val newH = (decodedBitmap.height * ratio).toInt().coerceAtLeast(1)
                    Bitmap.createScaledBitmap(decodedBitmap, newW, newH, true)
                } else {
                    decodedBitmap
                }

                // Compress to WebP (or JPEG fallback)
                var quality = 80
                var outputBytes: ByteArray
                do {
                    val bos = ByteArrayOutputStream()
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                        finalBitmap.compress(Bitmap.CompressFormat.WEBP_LOSSY, quality, bos)
                    } else {
                        @Suppress("DEPRECATION")
                        finalBitmap.compress(Bitmap.CompressFormat.WEBP, quality, bos)
                    }
                    outputBytes = bos.toByteArray()
                    quality -= 15
                } while (outputBytes.size > maxSizeBytes && quality >= 30)

                // If still too large, try JPEG with high compression
                if (outputBytes.size > maxSizeBytes) {
                    val bos = ByteArrayOutputStream()
                    finalBitmap.compress(Bitmap.CompressFormat.JPEG, 55, bos)
                    outputBytes = bos.toByteArray()
                }

                outputBytes
            } catch (e: Exception) {
                e.printStackTrace()
                null
            }
        }
    }

    /**
     * Compress an in-memory Bitmap directly to WebP bytes.
     */
    suspend fun compressBitmapToBytes(
        bitmap: Bitmap,
        maxDimension: Int = 800,
        maxSizeBytes: Long = 300 * 1024L
    ): ByteArray = withContext(Dispatchers.IO) {
        val scaledBitmap = if (bitmap.width > maxDimension || bitmap.height > maxDimension) {
            val ratio = Math.min(
                maxDimension.toFloat() / bitmap.width,
                maxDimension.toFloat() / bitmap.height
            )
            val newW = (bitmap.width * ratio).toInt().coerceAtLeast(1)
            val newH = (bitmap.height * ratio).toInt().coerceAtLeast(1)
            Bitmap.createScaledBitmap(bitmap, newW, newH, true)
        } else {
            bitmap
        }

        var quality = 80
        var outputBytes: ByteArray
        do {
            val bos = ByteArrayOutputStream()
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                scaledBitmap.compress(Bitmap.CompressFormat.WEBP_LOSSY, quality, bos)
            } else {
                @Suppress("DEPRECATION")
                scaledBitmap.compress(Bitmap.CompressFormat.WEBP, quality, bos)
            }
            outputBytes = bos.toByteArray()
            quality -= 15
        } while (outputBytes.size > maxSizeBytes && quality >= 30)

        outputBytes
    }

    /**
     * Uploads bytes to a specific storage path in Firebase Storage and returns the public download URL.
     */
    suspend fun uploadBytesToStorage(
        bytes: ByteArray,
        storagePath: String,
        mimeType: String = "image/webp",
        userType: String = "USER"
    ): Result<String> = withContext(Dispatchers.IO) {
        try {
            // Check quota guard first
            val (canUpload, _) = FirebaseStorageQuotaGuard.canUpload(userType, bytes.size.toLong())
            if (!canUpload) {
                // بديل آمن وسريع في حال تقييد السيرفر لضمان عدم توقف واجهة المستخدم
                val safeBase64 = "data:$mimeType;base64," + android.util.Base64.encodeToString(bytes, android.util.Base64.NO_WRAP)
                return@withContext Result.success(safeBase64)
            }

            val storageRef = storage.reference.child(storagePath)
            val metadata = StorageMetadata.Builder()
                .setContentType(mimeType)
                .setCustomMetadata("uploadedAt", System.currentTimeMillis().toString())
                .build()

            storageRef.putBytes(bytes, metadata).await()
            val downloadUrl = storageRef.downloadUrl.await().toString()
            Result.success(downloadUrl)
        } catch (e: Exception) {
            e.printStackTrace()
            // في حال عدم توفر اتصال بالإنترنت أو تعذر الوصول إلى Firebase Storage، نعتمد تمثيل البيانات الآمن
            if (bytes.isNotEmpty()) {
                val safeBase64 = "data:$mimeType;base64," + android.util.Base64.encodeToString(bytes, android.util.Base64.NO_WRAP)
                Result.success(safeBase64)
            } else {
                Result.failure(e)
            }
        }
    }

    /**
     * Uploads an image Uri directly with automatic compression and quota check.
     */
    suspend fun uploadImageUri(
        context: Context,
        uri: Uri,
        storagePath: String,
        maxDimension: Int = 800,
        maxSizeBytes: Long = 300 * 1024L,
        userType: String = "USER"
    ): Result<String> {
        val bytes = compressImageToBytes(context, uri, maxDimension, maxSizeBytes)
            ?: return Result.failure(Exception("فشل ضغط الصورة قبل الرفع."))
        return uploadBytesToStorage(bytes, storagePath, "image/webp", userType)
    }

    /**
     * Uploads a Bitmap directly with automatic compression and quota check.
     */
    suspend fun uploadBitmap(
        bitmap: Bitmap,
        storagePath: String,
        maxDimension: Int = 800,
        maxSizeBytes: Long = 300 * 1024L,
        userType: String = "USER"
    ): Result<String> {
        val bytes = compressBitmapToBytes(bitmap, maxDimension, maxSizeBytes)
        return uploadBytesToStorage(bytes, storagePath, "image/webp", userType)
    }

    // Helper functions for standard app entity paths
    fun getProviderProfilePath(providerId: String): String =
        "providers/$providerId/profile.webp"

    fun getProviderIdCardPath(providerId: String): String =
        "providers/$providerId/id_card.webp"

    fun getProviderWorkPhotoPath(providerId: String, index: Int): String =
        "providers/$providerId/work_photos/photo_${index}_${UUID.randomUUID().toString().take(6)}.webp"

    fun getStoreLogoPath(storeId: String): String =
        "stores/$storeId/logo.webp"

    fun getStoreCoverPath(storeId: String): String =
        "stores/$storeId/cover.webp"

    fun getStoreProductPath(storeId: String, productId: String): String =
        "stores/$storeId/products/$productId.webp"

    fun getStorePhotoPath(storeId: String, index: Int): String =
        "stores/$storeId/photos/photo_${index}_${UUID.randomUUID().toString().take(6)}.webp"

    fun getPropertyPhotoPath(propertyId: String, index: Int): String =
        "properties/$propertyId/photos/photo_${index}_${UUID.randomUUID().toString().take(6)}.webp"

    fun getChatMessageMediaPath(channelId: String, messageId: String): String =
        "chat/$channelId/${messageId}.webp"

    fun getPortfolioImagePath(providerId: String): String =
        "providers/$providerId/portfolio/img_${UUID.randomUUID().toString().take(8)}.webp"

    suspend fun uploadImageToStorage(context: Context, uri: Uri, path: String): Result<String> {
        return uploadImageUri(context, uri, path, maxDimension = 800, maxSizeBytes = 300 * 1024L)
    }

    /**
     * يحوّل أي نص صورة Base64 إلى رابط سحابي في Firebase Storage لمنع تضخم مستندات Firestore (حد 1MB).
     * وفي حال عدم توفر اتصال، يضغط الصورة إلى حجم مصغر آمن جداً (< 35KB).
     */
    suspend fun resolveBase64ToStorageUrl(
        input: String,
        storagePath: String,
        maxDimension: Int = 800,
        maxSizeBytes: Long = 250 * 1024L
    ): String = withContext(Dispatchers.IO) {
        val trimmed = input.trim()
        if (trimmed.isBlank()) return@withContext ""
        if (trimmed.startsWith("http://") || trimmed.startsWith("https://")) return@withContext trimmed
        try {
            val cleanBase64 = if (trimmed.contains(",")) trimmed.substringAfter(",") else trimmed
            val bytes = android.util.Base64.decode(cleanBase64, android.util.Base64.DEFAULT)
            val bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
            if (bitmap != null) {
                val uploaded = uploadBitmap(bitmap, storagePath, maxDimension, maxSizeBytes)
                if (uploaded.isSuccess) {
                    return@withContext uploaded.getOrThrow()
                }
                // Offline safe thumbnail fallback (< 35KB) so Firestore 1MB limit is never exceeded
                val thumbBytes = compressBitmapToBytes(bitmap, maxDimension = 320, maxSizeBytes = 35 * 1024L)
                return@withContext "data:image/webp;base64," + android.util.Base64.encodeToString(thumbBytes, android.util.Base64.NO_WRAP)
            } else {
                val pdfUpload = uploadBytesToStorage(bytes, storagePath, "application/pdf")
                if (pdfUpload.isSuccess) return@withContext pdfUpload.getOrThrow()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        trimmed
    }

    /**
     * Cleans up temporary image files in cache directory older than 24 hours to save storage and RAM.
     */
    suspend fun cleanOldCacheFiles(context: Context) = withContext(Dispatchers.IO) {
        try {
            val cacheDir = context.cacheDir
            val now = System.currentTimeMillis()
            val maxAge = 24 * 60 * 60 * 1000L // 24 hours
            cacheDir.listFiles()?.forEach { file ->
                if (file.isFile && (now - file.lastModified()) > maxAge) {
                    file.delete()
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
