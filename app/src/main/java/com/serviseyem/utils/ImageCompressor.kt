package com.serviseyem.utils

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream

/**
 * 🖼️ ImageCompressor
 * ضغط الصور وتوليد صور مصغرة (Thumbnails 200x200) لتقليل استهلاك الإنترنت
 * وتحسين سرعة تحميل الصور الفورية في المحادثات.
 */
object ImageCompressor {

    suspend fun generateThumbnail(
        context: Context,
        imageUri: Uri,
        maxDimension: Int = 200,
        quality: Int = 75
    ): File? = withContext(Dispatchers.IO) {
        try {
            val boundsOptions = BitmapFactory.Options().apply { inJustDecodeBounds = true }
            var inputStream = context.contentResolver.openInputStream(imageUri) ?: return@withContext null
            BitmapFactory.decodeStream(inputStream, null, boundsOptions)
            inputStream.close()

            val origWidth = boundsOptions.outWidth
            val origHeight = boundsOptions.outHeight
            if (origWidth <= 0 || origHeight <= 0) return@withContext null

            var sampleSize = 1
            while (origWidth / sampleSize > maxDimension * 2 || origHeight / sampleSize > maxDimension * 2) {
                sampleSize *= 2
            }

            val decodeOptions = BitmapFactory.Options().apply { inSampleSize = sampleSize }
            inputStream = context.contentResolver.openInputStream(imageUri) ?: return@withContext null
            val originalBitmap = BitmapFactory.decodeStream(inputStream, null, decodeOptions)
            inputStream.close()

            if (originalBitmap == null) return@withContext null

            val width = originalBitmap.width
            val height = originalBitmap.height
            val ratio = width.toFloat() / height.toFloat().coerceAtLeast(1f)

            val targetWidth: Int
            val targetHeight: Int
            if (width > height) {
                targetWidth = maxDimension
                targetHeight = (maxDimension / ratio).toInt().coerceAtLeast(1)
            } else {
                targetHeight = maxDimension
                targetWidth = (maxDimension * ratio).toInt().coerceAtLeast(1)
            }

            val scaledBitmap = Bitmap.createScaledBitmap(originalBitmap, targetWidth, targetHeight, true)
            val thumbFile = File(context.cacheDir, "thumb_${System.currentTimeMillis()}.jpg")
            FileOutputStream(thumbFile).use { outputStream ->
                scaledBitmap.compress(Bitmap.CompressFormat.JPEG, quality, outputStream)
                outputStream.flush()
            }
            if (scaledBitmap != originalBitmap) {
                originalBitmap.recycle()
            }
            if (!scaledBitmap.isRecycled) {
                scaledBitmap.recycle()
            }

            thumbFile
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    /**
     * ⚡ ضغط الصور الكاملة المرفوعة وتقليل حجمها لأقل من 250KB قبل الرفع لتوفير باقات الإنترنت.
     */
    suspend fun compressFullImage(
        context: Context,
        imageUri: Uri,
        maxDimension: Int = 1024,
        quality: Int = 80
    ): File? = withContext(Dispatchers.IO) {
        try {
            val inputStream = context.contentResolver.openInputStream(imageUri) ?: return@withContext null
            val boundsOptions = BitmapFactory.Options().apply { inJustDecodeBounds = true }
            BitmapFactory.decodeStream(inputStream, null, boundsOptions)
            inputStream.close()

            val origWidth = boundsOptions.outWidth
            val origHeight = boundsOptions.outHeight
            if (origWidth <= 0 || origHeight <= 0) return@withContext null

            var sampleSize = 1
            while (origWidth / sampleSize > maxDimension || origHeight / sampleSize > maxDimension) {
                sampleSize *= 2
            }

            val decodeOptions = BitmapFactory.Options().apply { inSampleSize = sampleSize }
            val nextStream = context.contentResolver.openInputStream(imageUri) ?: return@withContext null
            val decodedBitmap = BitmapFactory.decodeStream(nextStream, null, decodeOptions)
            nextStream.close()

            if (decodedBitmap == null) return@withContext null

            val compressedFile = File(context.cacheDir, "compressed_${System.currentTimeMillis()}.jpg")
            FileOutputStream(compressedFile).use { outputStream ->
                decodedBitmap.compress(Bitmap.CompressFormat.JPEG, quality, outputStream)
                outputStream.flush()
            }
            if (!decodedBitmap.isRecycled) {
                decodedBitmap.recycle()
            }

            compressedFile
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    /**
     * ⚡ ضغط ذكي فائق إلى WebP بحجم ~60KB لحفظ بيانات التاجر وسعة Firebase (Zero-Waste Firebase)
     */
    suspend fun compressToSmartWebP(
        context: Context,
        imageUri: Uri,
        maxDimension: Int = 800,
        targetQuality: Int = 75
    ): File? = withContext(Dispatchers.IO) {
        try {
            val inputStream = context.contentResolver.openInputStream(imageUri) ?: return@withContext null
            val boundsOptions = BitmapFactory.Options().apply { inJustDecodeBounds = true }
            BitmapFactory.decodeStream(inputStream, null, boundsOptions)
            inputStream.close()

            val origWidth = boundsOptions.outWidth
            val origHeight = boundsOptions.outHeight
            if (origWidth <= 0 || origHeight <= 0) return@withContext null

            var sampleSize = 1
            while (origWidth / sampleSize > maxDimension || origHeight / sampleSize > maxDimension) {
                sampleSize *= 2
            }

            val decodeOptions = BitmapFactory.Options().apply { inSampleSize = sampleSize }
            val nextStream = context.contentResolver.openInputStream(imageUri) ?: return@withContext null
            val decodedBitmap = BitmapFactory.decodeStream(nextStream, null, decodeOptions)
            nextStream.close()

            if (decodedBitmap == null) return@withContext null

            val compressedFile = File(context.cacheDir, "webp_${System.currentTimeMillis()}.webp")
            val format = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.R) {
                Bitmap.CompressFormat.WEBP_LOSSY
            } else {
                @Suppress("DEPRECATION")
                Bitmap.CompressFormat.WEBP
            }

            FileOutputStream(compressedFile).use { outputStream ->
                decodedBitmap.compress(format, targetQuality, outputStream)
                outputStream.flush()
            }
            if (!decodedBitmap.isRecycled) {
                decodedBitmap.recycle()
            }

            compressedFile
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}
