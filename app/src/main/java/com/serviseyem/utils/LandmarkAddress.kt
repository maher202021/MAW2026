package com.serviseyem.utils

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject

data class LandmarkAddress(
    val id: String = System.currentTimeMillis().toString(),
    val label: String,
    val landmarkName: String,
    val distanceMeters: Int = 0,
    val directionLabel: String = "",
    val notes: String = "",
    val pointLat: Double,
    val pointLng: Double
) {
    fun describe(): String {
        val parts = mutableListOf<String>()
        if (landmarkName.isNotBlank()) parts.add(landmarkName)
        if (distanceMeters > 0 && directionLabel.isNotBlank()) {
            parts.add("$distanceMeters م $directionLabel")
        }
        if (notes.isNotBlank()) parts.add(notes)
        return if (parts.isNotEmpty()) parts.joinToString(" • ") else "${"%.4f".format(java.util.Locale.US, pointLat)}, ${"%.4f".format(java.util.Locale.US, pointLng)}"
    }

    companion object {
        fun isValid(lat: Double, lng: Double): Boolean {
            return !lat.isNaN() && !lng.isNaN() &&
                lat in -90.0..90.0 && lng in -180.0..180.0 &&
                !(lat == 0.0 && lng == 0.0)
        }
    }
}

class LandmarkAddressStore(context: Context) {
    private val prefs = context.applicationContext.getSharedPreferences("landmark_addresses", Context.MODE_PRIVATE)

    fun all(): List<LandmarkAddress> = try {
        val arr = JSONArray(prefs.getString("items", "[]"))
        (0 until arr.length()).mapNotNull { i ->
            val o = arr.getJSONObject(i)
            val la = o.optDouble("lat", 0.0)
            val lo = o.optDouble("lng", 0.0)
            if (LandmarkAddress.isValid(la, lo)) {
                LandmarkAddress(
                    id = o.optString("id", i.toString()),
                    label = o.optString("label", "عنوان محفوظ"),
                    landmarkName = o.optString("landmark", ""),
                    distanceMeters = o.optInt("dist", 0),
                    directionLabel = o.optString("dir", ""),
                    notes = o.optString("notes", ""),
                    pointLat = la,
                    pointLng = lo
                )
            } else null
        }
    } catch (_: Exception) {
        emptyList()
    }

    fun add(addr: LandmarkAddress) {
        if (!LandmarkAddress.isValid(addr.pointLat, addr.pointLng)) return
        val updated = (listOf(addr) + all().filterNot { it.label == addr.label }).take(30)
        val arr = JSONArray()
        updated.forEach {
            arr.put(
                JSONObject()
                    .put("id", it.id)
                    .put("label", it.label)
                    .put("landmark", it.landmarkName)
                    .put("dist", it.distanceMeters)
                    .put("dir", it.directionLabel)
                    .put("notes", it.notes)
                    .put("lat", it.pointLat)
                    .put("lng", it.pointLng)
            )
        }
        prefs.edit().putString("items", arr.toString()).apply()
    }

    fun remove(id: String) {
        val remaining = all().filterNot { it.id == id }
        val arr = JSONArray()
        remaining.forEach {
            arr.put(
                JSONObject()
                    .put("id", it.id)
                    .put("label", it.label)
                    .put("landmark", it.landmarkName)
                    .put("dist", it.distanceMeters)
                    .put("dir", it.directionLabel)
                    .put("notes", it.notes)
                    .put("lat", it.pointLat)
                    .put("lng", it.pointLng)
            )
        }
        prefs.edit().putString("items", arr.toString()).apply()
    }
}
