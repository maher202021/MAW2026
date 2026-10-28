package com.serviseyem.navigation

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.math.abs

/**
 * Unit tests for HaversinePrecisionNavigator verifying geodesic formulas,
 * IUGG Earth radius distance, bearings, and turn classifications.
 */
class HaversinePrecisionNavigatorTest {

    @Test
    fun testIdenticalPointsReturnZeroDistance() {
        val dist = HaversinePrecisionNavigator.distanceMeters(15.3694, 44.1910, 15.3694, 44.1910)
        assertEquals(0.0, dist, 0.001)
    }

    @Test
    fun testOneDegreeOfLatitudeOnMeridian() {
        // 1 degree of latitude on any meridian should be ~111,195 m with IUGG radius (6371008.8 * PI / 180 = 111,195.08 m)
        val dist = HaversinePrecisionNavigator.distanceMeters(0.0, 0.0, 1.0, 0.0)
        val expected = (HaversinePrecisionNavigator.EARTH_RADIUS_METERS * Math.PI) / 180.0
        assertEquals(expected, dist, 0.5)
        assertTrue(abs(dist - 111195.0) < 5.0)
    }

    @Test
    fun testCardinalBearings() {
        // North
        val north = HaversinePrecisionNavigator.initialBearing(0.0, 0.0, 10.0, 0.0)
        assertEquals(0.0, north, 0.1)

        // East
        val east = HaversinePrecisionNavigator.initialBearing(0.0, 0.0, 0.0, 10.0)
        assertEquals(90.0, east, 0.1)

        // South
        val south = HaversinePrecisionNavigator.initialBearing(10.0, 0.0, 0.0, 0.0)
        assertEquals(180.0, south, 0.1)

        // West
        val west = HaversinePrecisionNavigator.initialBearing(0.0, 10.0, 0.0, 0.0)
        assertEquals(270.0, west, 0.1)
    }

    @Test
    fun testAngleDeltaWraparound() {
        // From 359 degrees to 1 degree clockwise should be +2 degrees
        val deltaClockwise = HaversinePrecisionNavigator.angleDelta(359.0, 1.0)
        assertEquals(2.0, deltaClockwise, 0.001)

        // From 1 degree to 359 degrees counter-clockwise should be -2 degrees
        val deltaCounterClockwise = HaversinePrecisionNavigator.angleDelta(1.0, 359.0)
        assertEquals(-2.0, deltaCounterClockwise, 0.001)
    }

    @Test
    fun testDestinationPointAndCrossTrack() {
        val startLat = 15.3500
        val startLon = 44.2000
        val distance = 5000.0 // 5km
        val bearing = 90.0 // East

        val dest = HaversinePrecisionNavigator.destinationPoint(startLat, startLon, bearing, distance)
        val measuredDist = HaversinePrecisionNavigator.distanceMeters(startLat, startLon, dest.first, dest.second)
        assertEquals(distance, measuredDist, 1.0)

        // Point directly on track should have ~0 cross-track distance
        val midPoint = HaversinePrecisionNavigator.destinationPoint(startLat, startLon, bearing, 2500.0)
        val crossTrack = HaversinePrecisionNavigator.crossTrackDistance(
            midPoint.first, midPoint.second,
            startLat, startLon,
            dest.first, dest.second
        )
        assertEquals(0.0, crossTrack, 0.5)
    }
}
