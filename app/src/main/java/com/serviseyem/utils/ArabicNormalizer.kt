package com.serviseyem.utils

/**
 * توحيد النصوص العربية للبحث المحلي المطابق لقاعدة places.db
 */
object ArabicNormalizer {
    private val DIACRITICS = Regex("[\\u064B-\\u0652\\u0640]")
    private val ALEF = Regex("[إأآٱ]")
    private val WHITESPACE = Regex("\\s+")

    // [نظام خرائط وملاحة عالمي - ملكنا]
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

    fun normalizeLoose(input: String): String {
        val base = normalize(input)
        if (base.isEmpty()) return ""
        return base.split(' ')
            .filter { it.isNotBlank() }
            .joinToString(" ") { token ->
                if (token.length > 3 && token.startsWith("ال")) token.removePrefix("ال") else token
            }
    }
}
