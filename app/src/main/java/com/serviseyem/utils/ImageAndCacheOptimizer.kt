package com.serviseyem.utils

import android.content.Context
import java.io.File

/**
 * ⚡ ImageAndCacheOptimizer - تحسين استهلاك الذاكرة والكاش لتوفير المساحة وسرعة الأداء
 */
object ImageAndCacheOptimizer {
    private const val MAX_CACHE_SIZE_BYTES = 50 * 1024 * 1024L // 50 ميجابايت كحد أقصى

    fun getCacheSizeMB(context: Context): Double {
        return try {
            val size = getDirectorySize(context.cacheDir)
            size.toDouble() / (1024.0 * 1024.0)
        } catch (e: Exception) {
            0.0
        }
    }

    fun clearAllAppCache(context: Context) {
        try {
            deleteRecursively(context.cacheDir)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun clearExcessCache(context: Context) {
        try {
            val cacheDir = context.cacheDir
            val size = getDirectorySize(cacheDir)
            if (size > MAX_CACHE_SIZE_BYTES) {
                deleteRecursively(cacheDir)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun getDirectorySize(file: File): Long {
        var length = 0L
        file.listFiles()?.let { list ->
            for (child in list) {
                length += if (child.isDirectory) getDirectorySize(child) else child.length()
            }
        }
        return length
    }

    private fun deleteRecursively(file: File) {
        file.listFiles()?.let { list ->
            for (child in list) {
                if (child.isDirectory) {
                    deleteRecursively(child)
                }
                child.delete()
            }
        }
    }
}
