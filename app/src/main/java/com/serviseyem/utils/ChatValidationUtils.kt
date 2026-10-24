package com.serviseyem.utils

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import java.io.ByteArrayOutputStream

/**
 * 📦 ChatValidationUtils
 * أدوات التحقق من نوع الملف، الحجم الأقصى (2MB)، وتطبيق الضغط الشديد للأبعاد (800x800) وجودة 70%
 * لتوفير استهلاك باقة Firebase Free Tier وتقليل حجم البيانات المرفوعة.
 */
object ChatValidationUtils {

    const val MAX_IMAGE_SIZE = (500 * 1024).toLong() // 500KB - الحد الأقصى لحجم الصورة بعد الضغط
    const val MAX_VIDEO_SIZE = (15 * 1024 * 1024).toLong() // 15MB - الحد الأقصى للفيديو
    const val MAX_FILE_SIZE = (1.5 * 1024 * 1024).toLong() // 1.5MB - الحد الأقصى المسموح للصوت
    const val MAX_TEXT_LENGTH = 1000
    const val MAX_DAILY_UPLOADS = 25

    // القائمة المحدثة للصيغ المسموح بها لتشمل كافة أنواع التسجيل الصوتي والفيديو
    val allowedMimeTypes = listOf(
        "image/jpeg", "image/png", "image/gif", "image/webp", "image/bmp",
        "audio/mpeg", "audio/mp3", "audio/aac", "audio/amr", "audio/wav", "audio/ogg", "audio/3gpp",
        "audio/mp4", "audio/m4a", "audio/x-m4a",
        "video/mp4", "video/3gpp", "video/webm", "video/mkv"
    )

    fun validateFile(uri: Uri, context: Context, isImage: Boolean, isVideo: Boolean = false): ValidationResult {
        var mimeType = context.contentResolver.getType(uri)
        if (mimeType.isNullOrBlank()) {
            val extension = android.webkit.MimeTypeMap.getFileExtensionFromUrl(uri.toString())
            if (extension.isNotBlank()) {
                mimeType = android.webkit.MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension.lowercase())
            }
        }
        if (mimeType.isNullOrBlank()) {
            val uriStr = uri.toString().lowercase()
            mimeType = when {
                isImage -> "image/jpeg"
                isVideo -> "video/mp4"
                uriStr.endsWith(".m4a") || uriStr.endsWith(".mp3") || uriStr.endsWith(".aac") || uriStr.contains("audio") || uriStr.contains("record") -> "audio/mp4"
                else -> if (!isImage && !isVideo) "audio/mp4" else null
            }
        }

        // 1. فحص نوع الملف (MIME Type)
        val isSupported = mimeType != null && (
            mimeType in allowedMimeTypes ||
            mimeType.startsWith("image/") ||
            mimeType.startsWith("audio/") ||
            mimeType.startsWith("video/")
        )
        if (!isSupported) {
            return ValidationResult(
                isValid = false,
                message = "⚠️ نوع الملف ($mimeType) غير مدعوم حالياً."
            )
        }

        // 2. فحص الحجم الخام
        val fileSize = try {
            context.contentResolver.openAssetFileDescriptor(uri, "r")?.use { it.length } ?: 0L
        } catch (_: Exception) { 0L }

        if (isVideo && fileSize > MAX_VIDEO_SIZE) {
            val sizeMb = String.format(java.util.Locale.US, "%.1f", fileSize.toDouble() / (1024 * 1024))
            return ValidationResult(
                isValid = false,
                message = "⚠️ حجم الفيديو كبير جداً ($sizeMb MB). الحد الأقصى 15 ميجابايت."
            )
        }

        // للملفات الصوتية: نطبق فحص الحجم الأقصى فوراً قبل الرفع لتوفير الباندويث
        if (!isImage && !isVideo && fileSize > MAX_FILE_SIZE) {
            val sizeMb = String.format(java.util.Locale.US, "%.1f", fileSize.toDouble() / (1024 * 1024))
            return ValidationResult(
                isValid = false,
                message = "⚠️ التسجيل الصوتي كبير جداً ($sizeMb MB). الحد الأقصى 1.5 ميجابايت."
            )
        }

        // 3. فحص الحصص اليومية
        if (!canUploadToday(context)) {
            return ValidationResult(
                isValid = false,
                message = "⚠️ تجاوزت الحد اليومي المسموح به لرفع الوسائط."
            )
        }

        return ValidationResult(isValid = true, message = "")
    }

    /**
     * تطبيق ضغط الصور بدقة (حد أقصى 800x800) وجودة 70% مع ضمان عدم تجاوز 500KB
     */
    fun compressImage(context: Context, uri: Uri): ByteArray {
        return try {
            val boundsOptions = BitmapFactory.Options().apply { inJustDecodeBounds = true }
            var inputStream = context.contentResolver.openInputStream(uri)
            BitmapFactory.decodeStream(inputStream, null, boundsOptions)
            inputStream?.close()

            val maxWidth = 800
            val maxHeight = 800
            var sampleSize = 1
            while (boundsOptions.outWidth / sampleSize > maxWidth * 2 || boundsOptions.outHeight / sampleSize > maxHeight * 2) {
                sampleSize *= 2
            }

            val decodeOptions = BitmapFactory.Options().apply { inSampleSize = sampleSize }
            inputStream = context.contentResolver.openInputStream(uri)
            val bitmap = BitmapFactory.decodeStream(inputStream, null, decodeOptions)
            inputStream?.close()

            if (bitmap == null) return byteArrayOf()

            val scaledBitmap = if (bitmap.width > maxWidth || bitmap.height > maxHeight) {
                val scale = minOf(maxWidth.toFloat() / bitmap.width, maxHeight.toFloat() / bitmap.height)
                val newW = (bitmap.width * scale).toInt().coerceAtLeast(1)
                val newH = (bitmap.height * scale).toInt().coerceAtLeast(1)
                val scaled = Bitmap.createScaledBitmap(bitmap, newW, newH, true)
                if (scaled != bitmap) bitmap.recycle()
                scaled
            } else {
                bitmap
            }

            var quality = 70
            var outputStream = ByteArrayOutputStream()
            scaledBitmap.compress(Bitmap.CompressFormat.JPEG, quality, outputStream)
            var result = outputStream.toByteArray()

            // خفض الجودة تدريجياً لضمان عدم تجاوز 500KB
            while (result.size > MAX_IMAGE_SIZE && quality > 25) {
                quality -= 10
                outputStream = ByteArrayOutputStream()
                scaledBitmap.compress(Bitmap.CompressFormat.JPEG, quality, outputStream)
                result = outputStream.toByteArray()
            }

            if (!scaledBitmap.isRecycled) scaledBitmap.recycle()
            
            android.util.Log.d("ChatValidation", "Final compressed size: ${result.size / 1024} KB at quality=$quality")
            result
        } catch (e: Exception) {
            android.util.Log.e("ChatValidation", "Compression error", e)
            byteArrayOf()
        }
    }

    fun validateProcessedData(data: ByteArray, isImage: Boolean, isVideo: Boolean = false): ValidationResult {
        if (data.isEmpty()) {
            return ValidationResult(isValid = false, message = "تعذر معالجة بيانات الملف.")
        }
        val limit = when {
            isImage -> MAX_IMAGE_SIZE
            isVideo -> MAX_VIDEO_SIZE
            else -> MAX_FILE_SIZE
        }
        if (data.size > limit) {
            val sizeMb = String.format(java.util.Locale.US, "%.1f", data.size.toDouble() / (1024 * 1024))
            val limitMb = String.format(java.util.Locale.US, "%.1f", limit.toDouble() / (1024 * 1024))
            return ValidationResult(isValid = false, message = "⚠️ حجم الملف ($sizeMb MB) يتجاوز الحد المسموح ($limitMb MB).")
        }
        return ValidationResult(isValid = true, message = "")
    }

    fun generateThumbnail(context: Context, uri: Uri): ByteArray {
        return try {
            val inputStream = context.contentResolver.openInputStream(uri)
            val options = BitmapFactory.Options().apply { inSampleSize = 4 }
            val bitmap = BitmapFactory.decodeStream(inputStream, null, options)
            inputStream?.close()

            if (bitmap == null) return byteArrayOf()

            val thumbHeight = if (bitmap.width > 0) (150f * bitmap.height / bitmap.width).toInt().coerceAtLeast(1) else 150
            val thumbBitmap = Bitmap.createScaledBitmap(bitmap, 150, thumbHeight, true)
            val outputStream = ByteArrayOutputStream()
            thumbBitmap.compress(Bitmap.CompressFormat.JPEG, 40, outputStream)
            if (thumbBitmap != bitmap) bitmap.recycle()
            if (!thumbBitmap.isRecycled) thumbBitmap.recycle()
            outputStream.toByteArray()
        } catch (_: Exception) { byteArrayOf() }
    }

    fun canUploadToday(context: Context): Boolean {
        val prefs = context.getSharedPreferences("chat_upload_limits", Context.MODE_PRIVATE)
        val todayStr = java.text.SimpleDateFormat("yyyyMMdd", java.util.Locale.US).format(java.util.Date())
        val lastDate = prefs.getString("last_upload_date", "")
        val count = if (lastDate == todayStr) prefs.getInt("upload_count", 0) else 0
        return count < MAX_DAILY_UPLOADS
    }

    fun recordUploadToday(context: Context) {
        val prefs = context.getSharedPreferences("chat_upload_limits", Context.MODE_PRIVATE)
        val todayStr = java.text.SimpleDateFormat("yyyyMMdd", java.util.Locale.US).format(java.util.Date())
        val lastDate = prefs.getString("last_upload_date", "")
        val count = if (lastDate == todayStr) prefs.getInt("upload_count", 0) else 0
        prefs.edit()
            .putString("last_upload_date", todayStr)
            .putInt("upload_count", count + 1)
            .apply()
    }
}

