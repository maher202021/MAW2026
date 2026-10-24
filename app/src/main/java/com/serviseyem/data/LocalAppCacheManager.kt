package com.serviseyem.data

import com.serviseyem.utils.*

import android.content.Context
import android.content.SharedPreferences
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton
import org.json.JSONArray
import org.json.JSONObject

/**
 * 📦 Local App Cache Manager & Offline Sync Storage Engine
 * Solves Problem 6: Offline caching, fast local rendering, queued sync operations,
 * and automatic cache pruning for stale records older than 30 days.
 */
@Singleton
class LocalAppCacheManager @Inject constructor(
    @ApplicationContext context: Context
) {

    private val prefs: SharedPreferences = context.getSharedPreferences("YS_Local_App_Cache_v2026", Context.MODE_PRIVATE)

    // 1. Save & Load Cached Providers
    fun saveProvidersCache(rawJsonString: String) {
        prefs.edit().putString("KEY_PROVIDERS_CACHE", rawJsonString).putLong("KEY_PROVIDERS_TIME", System.currentTimeMillis()).apply()
    }

    fun getProvidersCacheRaw(): String {
        return prefs.getString("KEY_PROVIDERS_CACHE", "[]") ?: "[]"
    }

    // 2. Save & Load Cached Stores
    fun saveStoresCache(rawJsonString: String) {
        prefs.edit().putString("KEY_STORES_CACHE", rawJsonString).putLong("KEY_STORES_TIME", System.currentTimeMillis()).apply()
    }

    fun getStoresCacheRaw(): String {
        return prefs.getString("KEY_STORES_CACHE", "[]") ?: "[]"
    }

    // 3. Bookings Cache -> Delegated exclusively to Room Database (AppDatabase.bookingDao())
    init {
        if (prefs.contains("KEY_BOOKINGS_CACHE") || prefs.contains("KEY_BOOKINGS_TIME")) {
            prefs.edit().remove("KEY_BOOKINGS_CACHE").remove("KEY_BOOKINGS_TIME").apply()
        }
    }

    fun saveOffersCache(rawJsonString: String) {
        prefs.edit().putString("KEY_OFFERS_CACHE", rawJsonString).putLong("KEY_OFFERS_TIME", System.currentTimeMillis()).apply()
    }

    fun getOffersCacheRaw(): String {
        return prefs.getString("KEY_OFFERS_CACHE", "[]") ?: "[]"
    }

    fun getOffersCacheTime(): Long {
        return prefs.getLong("KEY_OFFERS_TIME", 0L)
    }

    // 4. Save & Load Cached Categories
    fun saveCategoriesCache(rawJsonString: String) {
        prefs.edit()
            .putString("KEY_CATEGORIES_CACHE", rawJsonString)
            .putLong("KEY_CATEGORIES_TIME", System.currentTimeMillis())
            .apply()
    }

    fun getCategoriesCacheRaw(): String {
        return prefs.getString("KEY_CATEGORIES_CACHE", "[]") ?: "[]"
    }

    // 4.1 Save & Load Cached Jobs
    fun saveJobsCache(ownerId: String, rawJsonString: String) {
        prefs.edit().putString("KEY_JOBS_CACHE_$ownerId", rawJsonString).apply()
    }

    fun getJobsCacheRaw(ownerId: String): String {
        return prefs.getString("KEY_JOBS_CACHE_$ownerId", "[]") ?: "[]"
    }

    // 4.2 Save & Load Cached Doctors
    fun saveDoctorsCache(ownerId: String, rawJsonString: String) {
        prefs.edit().putString("KEY_DOCTORS_CACHE_$ownerId", rawJsonString).apply()
    }

    fun getDoctorsCacheRaw(ownerId: String): String {
        return prefs.getString("KEY_DOCTORS_CACHE_$ownerId", "[]") ?: "[]"
    }

    // 5. Offline Queue Operations (When user creates booking or sends message offline)
    data class OfflineSyncAction(
        val id: String = java.util.UUID.randomUUID().toString(),
        val type: String, // "CREATE_BOOKING", "UPDATE_PROFILE", "SEND_MESSAGE"
        val payloadJson: String,
        val createdAt: Long = System.currentTimeMillis()
    )

    fun queueOfflineAction(action: OfflineSyncAction) {
        val currentQueue = getOfflineQueueRaw()
        try {
            val jsonArray = try {
                JSONArray(currentQueue)
            } catch (_: Exception) {
                JSONArray()
            }
            val obj = JSONObject().apply {
                put("id", action.id)
                put("type", action.type)
                put("payloadJson", action.payloadJson)
                put("createdAt", action.createdAt)
            }
            jsonArray.put(obj)
            prefs.edit().putString("KEY_OFFLINE_QUEUE", jsonArray.toString()).apply()
        } catch (e: Exception) {
            // Safe fallback
        }
    }

    fun getOfflineQueueRaw(): String {
        return prefs.getString("KEY_OFFLINE_QUEUE", "[]") ?: "[]"
    }

    fun clearOfflineQueue() {
        prefs.edit().remove("KEY_OFFLINE_QUEUE").apply()
    }

    // 6. Automatic Cache Pruning (Removes cached data older than 30 days)
    fun pruneStaleCache() {
        val now = System.currentTimeMillis()
        val thirtyDaysMs = 30L * 24 * 60 * 60 * 1000
        val editor = prefs.edit()
        var modified = false

        val cachePairs = listOf(
            "KEY_PROVIDERS_CACHE" to "KEY_PROVIDERS_TIME",
            "KEY_STORES_CACHE" to "KEY_STORES_TIME",
            "KEY_OFFERS_CACHE" to "KEY_OFFERS_TIME",
            "KEY_CATEGORIES_CACHE" to "KEY_CATEGORIES_TIME"
        )

        for ((cacheKey, timeKey) in cachePairs) {
            val savedTime = prefs.getLong(timeKey, 0L)
            if (savedTime > 0L && now - savedTime > thirtyDaysMs) {
                editor.remove(cacheKey).remove(timeKey)
                modified = true
            }
        }

        if (modified) {
            editor.apply()
        }
    }
}
