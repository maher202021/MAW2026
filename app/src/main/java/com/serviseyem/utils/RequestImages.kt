package com.serviseyem.utils

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Base64

/**
 * RequestImages
 * يفك تشفير صور الطلبات المشفّرة (gcm:/gcmx:/enc::) أو Base64 العادي بأمان،
 * ويحوّلها إلى Bitmap مصغّر للمعاينة في لوحة الإدارة أو بايتات صالحة للرفع.
 */
object RequestImages {

    fun isHttpUrl(value: String?): Boolean {
        val v = value?.trim().orEmpty()
        return v.startsWith("http://", ignoreCase = true) || v.startsWith("https://", ignoreCase = true)
    }

    fun decryptToString(encryptedOrPlain: String?): String {
        val raw = encryptedOrPlain?.trim().orEmpty()
        if (raw.isBlank()) return ""
        return try {
            if (raw.startsWith("gcm:") || raw.startsWith("gcmx:") || raw.startsWith("enc::")) {
                SecurityCryptoUtils.decrypt(raw).ifBlank {
                    SecurityCryptoUtils.decryptCrossDevice(raw)
                }
            } else {
                raw
            }
        } catch (_: Exception) {
            ""
        }
    }

    fun decryptToBytes(encryptedOrPlain: String?): ByteArray? {
        val plain = decryptToString(encryptedOrPlain)
        if (plain.isBlank() || isHttpUrl(plain)) return null
        return try {
            val b64 = plain.substringAfter("base64,", plain).trim()
            if (b64.isBlank()) return null
            Base64.decode(b64, Base64.DEFAULT)
        } catch (_: Exception) {
            null
        }
    }

    fun toBitmap(bytes: ByteArray?, maxSide: Int = 1280): Bitmap? {
        if (bytes == null || bytes.isEmpty()) return null
        return try {
            val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
            BitmapFactory.decodeByteArray(bytes, 0, bytes.size, bounds)
            if (bounds.outWidth <= 0 || bounds.outHeight <= 0) return null
            var sampleSize = 1
            while (maxOf(bounds.outWidth, bounds.outHeight) / sampleSize > maxSide * 2) {
                sampleSize *= 2
            }
            BitmapFactory.decodeByteArray(
                bytes,
                0,
                bytes.size,
                BitmapFactory.Options().apply { inSampleSize = sampleSize }
            )
        } catch (_: Exception) {
            null
        }
    }
}
