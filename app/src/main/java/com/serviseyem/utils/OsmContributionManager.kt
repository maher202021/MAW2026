package com.serviseyem.utils

import android.content.Context
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL

/**
 * 🗺️ OsmContributionManager
 * 🏅 الفكرة الرابعة: "التوأمة مع OpenStreetMap"
 * يتيح لأي مواطن يمني إضافة معلم جديد (مسجد، سوق، بئر ماء، محطة وقود، ورشة) وحفظه محلياً ومشاركته مجاناً وبدون أي مفاتيح API.
 */
object OsmContributionManager {

    data class LandmarkNode(
        val name: String,
        val category: String, // "mosque", "water_well", "market", "fuel", "workshop"
        val latitude: Double,
        val longitude: Double,
        val city: String,
        val notes: String = "",
        val timestamp: Long = System.currentTimeMillis()
    )

    private const val PREFS_NAME = "osm_pending_edits"

    fun savePendingLandmark(context: Context, node: LandmarkNode) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val currentJson = prefs.getString("landmarks", "[]") ?: "[]"
        val array = org.json.JSONArray(currentJson)
        val obj = JSONObject().apply {
            put("name", node.name)
            put("category", node.category)
            put("latitude", node.latitude)
            put("longitude", node.longitude)
            put("city", node.city)
            put("notes", node.notes)
            put("timestamp", node.timestamp)
        }
        array.put(obj)
        prefs.edit().putString("landmarks", array.toString()).apply()
    }

    fun getPendingLandmarks(context: Context): List<LandmarkNode> {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val currentJson = prefs.getString("landmarks", "[]") ?: "[]"
        val list = mutableListOf<LandmarkNode>()
        try {
            val array = org.json.JSONArray(currentJson)
            for (i in 0 until array.length()) {
                val obj = array.getJSONObject(i)
                list.add(
                    LandmarkNode(
                        name = obj.getString("name"),
                        category = obj.getString("category"),
                        latitude = obj.getDouble("latitude"),
                        longitude = obj.getDouble("longitude"),
                        city = obj.getString("city"),
                        notes = obj.optString("notes", ""),
                        timestamp = obj.optLong("timestamp", System.currentTimeMillis())
                    )
                )
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return list
    }
}
