package com.serviseyem.utils

import android.content.Context
import androidx.annotation.Keep
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.util.UUID

@Keep
data class RoadHazard(
    val id: String = UUID.randomUUID().toString(),
    val type: String = "ROAD_BLOCK", // "ROAD_BLOCK", "FLOOD", "ACCIDENT", "TRAFFIC", "POTHOLE"
    val title: String = "",
    val description: String = "",
    val latitude: Double = 0.0,
    val longitude: Double = 0.0,
    val reportedBy: String = "مستخدم",
    val reporterPhone: String = "",
    val confirmations: Int = 1,
    val createdAt: Long = System.currentTimeMillis(),
    val expiresAt: Long = System.currentTimeMillis() + (6 * 60 * 60 * 1000L), // Default 6 hours validity
    val active: Boolean = true
) {
    val typeLabelArabic: String
        get() = when (type) {
            "ROAD_BLOCK" -> "طريق مقطوع 🚫"
            "FLOOD" -> "سيول ومياه جارية 🌊"
            "ACCIDENT" -> "حادث سير 💥"
            "TRAFFIC" -> "ازدحام خانق 🚗"
            "POTHOLE" -> "حفر وأضرار طريق ⚠️"
            else -> "تنبيه طريق ⚠️"
        }
}

/**
 * 🚧 RoadHazardManager
 * نظام التنبيهات المرورية والمخاطر المجتمعية لليمن:
 * يتيح للسائقين والمواطنين الإبلاغ اللحظي عن الطرق المقطوعة، السيول، الحوادث، والازدحام
 * مع عرضها على الخريطة ومزامنتها سحابياً ودعم العمل بلا إنترنت عبر الكاش المحلي.
 */
object RoadHazardManager {

    private const val PREFS_KEY_HAZARDS = "cached_road_hazards_json"
    private const val COLLECTION_NAME = "road_hazards"

    /**
     * إرسال بلاغ مروري جديد ومزامنته سحابياً مع الكاش المحلي
     */
    suspend fun reportHazard(
        context: Context,
        type: String,
        latitude: Double,
        longitude: Double,
        title: String,
        description: String = "",
        reporterName: String = "مستخدم",
        reporterPhone: String = ""
    ): Result<RoadHazard> = withContext(Dispatchers.IO) {
        val hazard = RoadHazard(
            type = type,
            title = title.ifBlank { "تنبيه مروري" },
            description = description,
            latitude = latitude,
            longitude = longitude,
            reportedBy = reporterName,
            reporterPhone = reporterPhone,
            confirmations = 1,
            createdAt = System.currentTimeMillis(),
            expiresAt = System.currentTimeMillis() + (6 * 60 * 60 * 1000L),
            active = true
        )

        // 1. Save to local cache immediately
        saveToLocalCache(context, hazard)

        // 2. Sync to Firestore if network is available
        if (NetworkUtils.hasInternetTransport(context)) {
            try {
                val db = FirebaseFirestore.getInstance()
                val map = mapOf(
                    "id" to hazard.id,
                    "type" to hazard.type,
                    "title" to hazard.title,
                    "description" to hazard.description,
                    "latitude" to hazard.latitude,
                    "longitude" to hazard.longitude,
                    "reportedBy" to hazard.reportedBy,
                    "reporterPhone" to hazard.reporterPhone,
                    "confirmations" to hazard.confirmations,
                    "createdAt" to hazard.createdAt,
                    "expiresAt" to hazard.expiresAt,
                    "active" to hazard.active
                )
                db.collection(COLLECTION_NAME).document(hazard.id)
                    .set(map, SetOptions.merge())
                    .await()
            } catch (e: Exception) {
                // Ignore network error; locally cached
            }
        }

        Result.success(hazard)
    }

    /**
     * جلب التنبيهات المرورية النشطة القريبة من مسار السائق
     */
    suspend fun getActiveHazards(
        context: Context,
        centerLat: Double = 0.0,
        centerLng: Double = 0.0,
        radiusKm: Double = 50.0
    ): List<RoadHazard> = withContext(Dispatchers.IO) {
        val now = System.currentTimeMillis()
        val cached = loadFromLocalCache(context).filter { it.expiresAt > now && it.active }

        if (!NetworkUtils.hasInternetTransport(context)) {
            return@withContext cached
        }

        try {
            val db = FirebaseFirestore.getInstance()
            val snap = db.collection(COLLECTION_NAME)
                .whereEqualTo("active", true)
                .whereGreaterThan("expiresAt", now)
                .limit(50)
                .get()
                .await()

            val remoteList = snap.documents.mapNotNull { doc ->
                try {
                    RoadHazard(
                        id = doc.getString("id") ?: doc.id,
                        type = doc.getString("type") ?: "ROAD_BLOCK",
                        title = doc.getString("title") ?: "",
                        description = doc.getString("description") ?: "",
                        latitude = doc.getDouble("latitude") ?: 0.0,
                        longitude = doc.getDouble("longitude") ?: 0.0,
                        reportedBy = doc.getString("reportedBy") ?: "",
                        reporterPhone = doc.getString("reporterPhone") ?: "",
                        confirmations = doc.getLong("confirmations")?.toInt() ?: 1,
                        createdAt = doc.getLong("createdAt") ?: now,
                        expiresAt = doc.getLong("expiresAt") ?: (now + 3600000L),
                        active = doc.getBoolean("active") ?: true
                    )
                } catch (_: Exception) {
                    null
                }
            }

            if (remoteList.isNotEmpty()) {
                val merged = (remoteList + cached).distinctBy { it.id }
                saveAllToLocalCache(context, merged)
                return@withContext merged
            }
        } catch (_: Exception) {
            // Fallback to cached
        }

        cached
    }

    private fun saveToLocalCache(context: Context, hazard: RoadHazard) {
        val current = loadFromLocalCache(context).filterNot { it.id == hazard.id }.toMutableList()
        current.add(0, hazard)
        saveAllToLocalCache(context, current.take(100))
    }

    private fun saveAllToLocalCache(context: Context, hazards: List<RoadHazard>) {
        try {
            val arr = JSONArray()
            hazards.forEach { h ->
                arr.put(JSONObject().apply {
                    put("id", h.id)
                    put("type", h.type)
                    put("title", h.title)
                    put("description", h.description)
                    put("latitude", h.latitude)
                    put("longitude", h.longitude)
                    put("reportedBy", h.reportedBy)
                    put("reporterPhone", h.reporterPhone)
                    put("confirmations", h.confirmations)
                    put("createdAt", h.createdAt)
                    put("expiresAt", h.expiresAt)
                    put("active", h.active)
                })
            }
            val sp = context.getSharedPreferences("YS_Road_Hazards_Cache", Context.MODE_PRIVATE)
            sp.edit().putString(PREFS_KEY_HAZARDS, arr.toString()).apply()
        } catch (_: Exception) {}
    }

    fun loadFromLocalCache(context: Context): List<RoadHazard> {
        return try {
            val sp = context.getSharedPreferences("YS_Road_Hazards_Cache", Context.MODE_PRIVATE)
            val json = sp.getString(PREFS_KEY_HAZARDS, "[]") ?: "[]"
            val arr = JSONArray(json)
            val list = mutableListOf<RoadHazard>()
            for (i in 0 until arr.length()) {
                val o = arr.getJSONObject(i)
                list.add(
                    RoadHazard(
                        id = o.optString("id"),
                        type = o.optString("type", "ROAD_BLOCK"),
                        title = o.optString("title", ""),
                        description = o.optString("description", ""),
                        latitude = o.optDouble("latitude", 0.0),
                        longitude = o.optDouble("longitude", 0.0),
                        reportedBy = o.optString("reportedBy", ""),
                        reporterPhone = o.optString("reporterPhone", ""),
                        confirmations = o.optInt("confirmations", 1),
                        createdAt = o.optLong("createdAt", System.currentTimeMillis()),
                        expiresAt = o.optLong("expiresAt", System.currentTimeMillis() + 3600000L),
                        active = o.optBoolean("active", true)
                    )
                )
            }
            list
        } catch (_: Exception) {
            emptyList()
        }
    }
}
