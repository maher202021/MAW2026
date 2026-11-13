package com.serviseyem.utils

/**
 * توحيد النصوص العربية للبحث المحلي المطابق لقاعدة places.db
 */
object ArabicNormalizer {
    private val DIACRITICS = Regex("[\\u064B-\\u0652\\u0640]")
    private val ALEF = Regex("[إأآٱ]")
    private val WHITESPACE = Regex("\\s+")

    fun normalize(input: String): String {
        if (input.isBlank()) return ""
        var s = input.trim().lowercase()
        s = DIACRITICS.replace(s, "")
        s = ALEF.replace(s, "ا")
        s = s.replace('ى', 'ي')
            .replace('ئ', 'ي')
            .replace('ؤ', 'و')
            .replace('ة', 'ه')
        return WHITESPACE.replace(s, " ").trim()
    }
}
