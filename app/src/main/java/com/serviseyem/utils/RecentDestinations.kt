package com.serviseyem.utils

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject

data class RecentDest(val name: String, val lat: Double, val lng: Double)

class RecentDestinations(context: Context) {
    private val prefs = context.applicationContext.getSharedPreferences("recent_destinations", Context.MODE_PRIVATE)

    fun all(): List<RecentDest> = try {
        val a = JSONArray(prefs.getString("d", "[]"))
        (0 until a.length()).mapNotNull {
            val o = a.getJSONObject(it)
            val la = o.optDouble("lat")
            val lo = o.optDouble("lng")
            if (LandmarkAddress.isValid(la, lo)) RecentDest(o.optString("n"), la, lo) else null
        }
    } catch (_: Exception) {
        emptyList()
    }

    fun add(name: String, lat: Double, lng: Double) {
        if (name.isBlank() || !LandmarkAddress.isValid(lat, lng)) return
        val list = (listOf(RecentDest(name.take(60), lat, lng)) + all().filterNot { it.name == name }).take(10)
        val a = JSONArray()
        list.forEach {
            a.put(JSONObject().put("n", it.name).put("lat", it.lat).put("lng", it.lng))
        }
        prefs.edit().putString("d", a.toString()).apply()
    }

    fun clear() = prefs.edit().clear().apply()
}
