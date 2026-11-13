package com.serviseyem.utils

import org.junit.Assert.assertEquals
import org.junit.Test

class ArabicNormalizerTest {

    @Test
    fun normalizesDiacriticsHamzaAlifMaqsurahAndTaaMarbuta() {
        assertEquals("مدرسه الاميل", ArabicNormalizer.normalize("مَدرسةُ الأميل"))
        assertEquals("جامعه صنعاء الجديده", ArabicNormalizer.normalize("جَامِعَةُ صَنْعَاءَ الجَدِيدَة"))
        assertEquals("مسجد ابراهيم", ArabicNormalizer.normalize("  مَسْجِدُ   إِبْرَاهِيم  "))
    }

    @Test
    fun collapsesMultipleSpacesAndTatweel() {
        assertEquals("شارع تعز", ArabicNormalizer.normalize("شـــارع    تَعِز"))
    }
}
