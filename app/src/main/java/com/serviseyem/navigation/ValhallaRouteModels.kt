package com.serviseyem.navigation

sealed class NavigationRouteResult {
    data class Success(
        val points: List<RoutePoint>,
        val instructions: List<NavigationInstruction>,
        val totalDistanceMeters: Double,
        val estimatedDurationSeconds: Int,
        val isOfflineEngine: Boolean = false,
        val modeLabel: String = ""
    ) : NavigationRouteResult()

    data class Error(val message: String) : NavigationRouteResult()
}

data class RoutePoint(val lat: Double, val lon: Double)

data class NavigationInstruction(
    val text: String,
    val maneuverType: ManeuverType,
    val distanceRemaining: Double,
    val segmentDistance: Double,
    val streetName: String = "",
    val maneuverLat: Double = 0.0,
    val maneuverLon: Double = 0.0,
    val shapeIndex: Int = 0
)

enum class ManeuverType {
    START,
    STRAIGHT,
    SLIGHT_RIGHT,
    TURN_RIGHT,
    SHARP_RIGHT,
    SLIGHT_LEFT,
    TURN_LEFT,
    SHARP_LEFT,
    U_TURN,
    ROUNDABOUT,
    DESTINATION
}
