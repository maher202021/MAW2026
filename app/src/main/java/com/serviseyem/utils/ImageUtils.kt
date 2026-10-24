package com.serviseyem.utils

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Base64
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream

object ImageUtils {
    fun uriToBase64(
        context: Context,
        uri: Uri,
        maxWidth: Int = 600,
        quality: Int = 60
    ): String {
        return try {
            val inputStream = context.contentResolver.openInputStream(uri) ?: return ""
            val originalBitmap = BitmapFactory.decodeStream(inputStream)
            inputStream.close()
            if (originalBitmap == null) return ""

            val targetMax = maxWidth.coerceAtMost(600)
            val finalQuality = quality.coerceAtMost(60)

            val ratio = originalBitmap.width.toFloat() / originalBitmap.height.toFloat()
            val targetWidth = minOf(targetMax, originalBitmap.width)
            val targetHeight = (targetWidth / ratio).toInt().coerceAtMost(600)

            val scaledBitmap = Bitmap.createScaledBitmap(originalBitmap, targetWidth, targetHeight, true)

            val outputStream = ByteArrayOutputStream()
            scaledBitmap.compress(Bitmap.CompressFormat.JPEG, finalQuality, outputStream)

            if (scaledBitmap != originalBitmap) {
                if (!originalBitmap.isRecycled) originalBitmap.recycle()
            }
            if (!scaledBitmap.isRecycled) scaledBitmap.recycle()

            val bytes = outputStream.toByteArray()
            if (bytes.size > 1.5 * 1024 * 1024) {
                // Hard cap 1.5MB
                return ""
            }

            Base64.encodeToString(bytes, Base64.NO_WRAP)
        } catch (e: Exception) {
            e.printStackTrace()
            ""
        }
    }

    fun uriToCompressedBase64(context: Context, uri: Uri, maxWidth: Int = 600, maxHeight: Int = 600, quality: Int = 60): String {
        return uriToBase64(context, uri, maxWidth, quality)
    }

    suspend fun compressAndOptimizeImage(
        context: Context,
        uri: Uri,
        maxWidth: Int = 800,
        maxHeight: Int = 800,
        quality: Int = 80,
        maxSizeKB: Int = 200
    ): String {
        return withContext(Dispatchers.IO) {
            try {
                val inputStream = context.contentResolver.openInputStream(uri) ?: return@withContext ""
                val bitmap = BitmapFactory.decodeStream(inputStream)
                inputStream.close()
                if (bitmap == null) return@withContext ""
                
                val scaledBitmap = if (bitmap.width > maxWidth || bitmap.height > maxHeight) {
                    val ratio = minOf(maxWidth.toFloat() / bitmap.width, maxHeight.toFloat() / bitmap.height)
                    Bitmap.createScaledBitmap(
                        bitmap,
                        (bitmap.width * ratio).toInt(),
                        (bitmap.height * ratio).toInt(),
                        true
                    )
                } else bitmap
                
                var currentQuality = quality
                var compressedBytes: ByteArray
                var outputStream: ByteArrayOutputStream
                
                do {
                    outputStream = ByteArrayOutputStream()
                    scaledBitmap.compress(Bitmap.CompressFormat.JPEG, currentQuality, outputStream)
                    compressedBytes = outputStream.toByteArray()
                    currentQuality -= 10
                } while (compressedBytes.size > maxSizeKB * 1024 && currentQuality >= 20)
                
                if (scaledBitmap != bitmap) {
                    if (!bitmap.isRecycled) bitmap.recycle()
                }
                if (!scaledBitmap.isRecycled) scaledBitmap.recycle()

                Base64.encodeToString(compressedBytes, Base64.DEFAULT)
            } catch (e: Exception) {
                e.printStackTrace()
                ""
            }
        }
    }
}

object ImageOptimizationUtils {
    suspend fun compressAndOptimizeImage(
        context: Context,
        uri: Uri,
        maxWidth: Int = 800,
        maxHeight: Int = 800,
        quality: Int = 80,
        maxSizeKB: Int = 200
    ): String {
        return ImageUtils.compressAndOptimizeImage(context, uri, maxWidth, maxHeight, quality, maxSizeKB)
    }
}

fun convertGenericUriToBase64(context: Context, uri: Uri): String {
    return ImageUtils.uriToBase64(context, uri)
}

fun convertBitmapToBase64(bitmap: Bitmap): String {
    return try {
        val outputStream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 75, outputStream)
        Base64.encodeToString(outputStream.toByteArray(), Base64.NO_WRAP)
    } catch (e: Exception) {
        ""
    }
}

fun compressAndResizeImageUri(context: Context, uri: Uri, maxDimension: Int = 800, quality: Int = 70): String {
    return ImageUtils.uriToBase64(context, uri, maxDimension, quality)
}
