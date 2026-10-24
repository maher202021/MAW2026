package com.serviseyem.utils

import android.content.Context
import java.io.File

/**
 * 🗄️ CacheManager
 * نظام تخزين مؤقت محلي (Local Storage Caching) بسعة قصوى 20 ميجابايت
 * يطبق خوارزمية الحذف للأقدم استخداماً (LRU) لحفظ الوسائط وتوفير استهلاك باقة الإنترنت و Firebase
 */
class CacheManager(context: Context) {
    private val cacheDir = File(context.cacheDir, "chat_cache").apply { if (!exists()) mkdirs() }
    private val MAX_CACHE_SIZE = 20 * 1024 * 1024L // 20 ميجابايت

    fun getCachedFile(url: String): File? {
        val fileName = url.hashCode().toString()
        val file = File(cacheDir, fileName)
        return if (file.exists() && file.length() > 0) {
            file.setLastModified(System.currentTimeMillis())
            file
        } else null
    }

    fun cacheFile(url: String, data: ByteArray) {
        clearIfNeeded()
        val fileName = url.hashCode().toString()
        val file = File(cacheDir, fileName)
        try {
            file.writeBytes(data)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun clearIfNeeded() {
        val files = cacheDir.listFiles() ?: return
        val totalSize = files.sumOf { it.length() }
        if (totalSize > MAX_CACHE_SIZE) {
            files.sortedBy { it.lastModified() }
                .take((files.size / 2).coerceAtLeast(1))
                .forEach { it.delete() }
        }
    }

    fun clearAll() {
        cacheDir.listFiles()?.forEach { it.delete() }
    }
}
