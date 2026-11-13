package com.serviseyem.ui.screens.map

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test

class ParseCoordinatesTest {

    @Test
    fun parsesWesternCommaAndSpaceCoordinates() {
        val comma = parseCoordinates("15.3694, 44.1910")
        assertNotNull(comma)
        assertEquals(15.3694, comma!!.first, 0.0001)
        assertEquals(44.1910, comma.second, 0.0001)

        val space = parseCoordinates("  12.7794 45.0367  ")
        assertNotNull(space)
        assertEquals(12.7794, space!!.first, 0.0001)
        assertEquals(45.0367, space.second, 0.0001)
    }

    @Test
    fun parsesArabicIndicDigitsAndArabicComma() {
        val parsed = parseCoordinates("١٥.٥، ٤٤.٢٥")
        assertNotNull(parsed)
        assertEquals(15.5, parsed!!.first, 0.0001)
        assertEquals(44.25, parsed.second, 0.0001)
    }

    @Test
    fun rejectsZeroAndOutOfBoundsCoordinates() {
        assertNull(parseCoordinates("0, 0"))
        assertNull(parseCoordinates("95.0, 44.0"))
        assertNull(parseCoordinates("15.3, 185.0"))
        assertNull(parseCoordinates("غير صالح"))
        assertNull(parseCoordinates(""))
    }
}
