package com.serviseyem.utils

import android.content.Context
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import kotlin.math.*

data class NavigationRouteResult(
    val distanceKm: Double,
    val distanceText: String,
    val durationText: String,
    val instructions: List<String>,
    val isOfflineReal: Boolean
)

object MockValhallaNavigator {

    fun calculateRoute(context: Context, startLat: Double, startLng: Double, endLat: Double, endLng: Double, targetCity: String = ""): NavigationRouteResult {
        // Calculate direct Haversine distance as fallback
        val R = 6371.0 // Radius of the earth in km
        val dLat = Math.toRadians(endLat - startLat)
        val dLon = Math.toRadians(endLng - startLng)
        val a = sin(dLat / 2) * sin(dLat / 2) +
                cos(Math.toRadians(startLat)) * cos(Math.toRadians(endLat)) *
                sin(dLon / 2) * sin(dLon / 2)
        val c = 2 * atan2(sqrt(a), sqrt(1 - a))
        val directKm = R * c

        var routeDistance = if (directKm < 1.0) max(2.5, directKm * 1.3) else directKm * 1.25
        routeDistance = round(routeDistance * 10) / 10.0

        val hours = (routeDistance / 60.0)
        val durationStr = if (hours >= 1.0) {
            val h = hours.toInt()
            val m = ((hours - h) * 60).toInt()
            "$h ساعات و $m دقيقة"
        } else {
            "${(hours * 60).toInt()} دقيقة"
        }

        // Try reading asset JSON for custom instructions
        var jsonInstructions = listOf(
            "تحرك نحو الوجهة المطلوبة لمسافة ${routeDistance} كم",
            "اتبع الإرشادات الصوتية والبصرية على خريطة دليل اليمن المباشرة",
            "الوصول المتوقع إلى الموقع المحدد خلال $durationStr"
        )

        try {
            val inputStream = context.assets.open("mock_valhalla_network.json")
            val reader = BufferedReader(InputStreamReader(inputStream))
            val jsonString = reader.readText()
            reader.close()

            val jsonObject = JSONObject(jsonString)
            val routesArray = jsonObject.getJSONArray("routes")
            for (i in 0 until routesArray.length()) {
                val r = routesArray.getJSONObject(i)
                val toCity = r.getString("to")
                if (targetCity.contains(toCity) || toCity.contains(targetCity)) {
                    routeDistance = r.getDouble("distance_km")
                    val dur = r.getString("duration")
                    val instArray = r.getJSONArray("instructions")
                    val list = mutableListOf<String>()
                    for (j in 0 until instArray.length()) {
                        list.add(instArray.getString(j))
                    }
                    return NavigationRouteResult(
                        distanceKm = routeDistance,
                        distanceText = "$routeDistance كم",
                        durationText = dur,
                        instructions = list,
                        isOfflineReal = false
                    )
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        return NavigationRouteResult(
            distanceKm = routeDistance,
            distanceText = "$routeDistance كم",
            durationText = durationStr,
            instructions = jsonInstructions,
            isOfflineReal = false
        )
    }

    fun isRealValhallaAvailable(context: Context): Boolean {
        val valhallaFile = java.io.File(context.filesDir, "valhalla_tiles.tar")
        val markerFile = java.io.File(context.filesDir, "is_valhalla_downloaded.txt")
        return valhallaFile.exists() || markerFile.exists()
    }
}
