package com.serviseyem.ui.screens.chat.managers

import com.serviseyem.data.models.ChatMessage
import com.serviseyem.data.models.MediaType

/**
 * 🧠 ChatSmartSuggestionsEngine
 * محرك ذكي محلي بالكامل يوفر:
 * 1. اقتراحات ردود سريعة ذكية (Smart Replies) تظهر فوق شريط الكتابة حسب سياق آخر رسالة ونوع الخدمة.
 * 2. تلخيص فوري للمحادثة (Local Conversation Summary) دون أي استهلاك لواجهات الذكاء السحابية.
 */
object ChatSmartSuggestionsEngine {

    data class SmartSummary(
        val totalCount: Int,
        val textCount: Int,
        val imageCount: Int,
        val audioCount: Int,
        val locationCount: Int,
        val summaryTitle: String,
        val summaryDetails: String
    )

    /**
     * توليد اقتراحات الردود الذكية بناءً على آخر رسالة ونوع الكيان
     */
    fun generateSuggestions(
        lastMessage: ChatMessage?,
        isLastMessageMine: Boolean,
        relatedEntityType: String?
    ): List<String> {
        val entityType = relatedEntityType?.uppercase() ?: "PROVIDER"
        if (lastMessage == null || isLastMessageMine) {
            // مقترحات افتراضية حسب نوع الخدمة لبدء المحادثة أو إرسال استفسار سريع
            return when (entityType) {
                "PROVIDER" -> listOf("أرسل لي صورة المشكلة 📷", "شارك موقعك بدقة 📍", "هل المشكلة طارئة؟ 🚨")
                "STORE", "RESTAURANT" -> listOf("هل المنتج متوفر؟ 📦", "كم سعر التوصيل؟ 🛵", "أريد تأكيد الطلب 🛍️")
                "MEDICAL" -> listOf("ما هي أوقات الدوام؟ 🕒", "أود تأكيد موعد الكشف 🩺", "استفسار عن التحاليل 📋")
                "PROPERTY" -> listOf("هل العقار متاح للمعاينة؟ 🏠", "كم مبلغ الإيجار الشهري؟ 💰", "أين يقع العقار تحديداً؟ 📍")
                else -> listOf("السلام عليكم 🤝", "أهلاً بك، كيف يمكنني مساعدتك؟", "في خدمتك دائماً ✨")
            }
        }

        val text = lastMessage.message.trim().lowercase()

        // 1. استفسارات الوصول والتوقيت
        if (text.contains("متى") || text.contains("وين") || text.contains("تأخرت") || text.contains("وصلت") || text.contains("طريق")) {
            return listOf(
                "بعد 10 دقائق ⏱️",
                "بعد 30 دقيقة 🚗",
                "أنا في الطريق الآن 📍",
                "وصلت عند الموقع 🎯"
            )
        }

        // 2. رد على الإشعار بأن الفني في الطريق
        if (text.contains("في الطريق") || text.contains("جاي") || text.contains("قريب")) {
            return listOf(
                "تمام، بانتظارك 👍",
                "الموقع عبر الخريطة دقيق 📍",
                "شكراً لإبلاغي ✨"
            )
        }

        // 3. استفسارات الأسعار والتكلفة
        if (text.contains("كم") || text.contains("سعر") || text.contains("تكلف") || text.contains("بكم") || text.contains("حساب")) {
            return listOf(
                "حسب الفحص والمعاينة 🔍",
                "سأرسل لك الفاتورة بالتفصيل 📄",
                "السعر المتفق عليه 💰",
                "شامل قطع الغيار والتركيب 🛠️"
            )
        }

        // 4. استفسارات الموقع والعنوان
        if (text.contains("موقع") || text.contains("عنوان") || text.contains("وين مكانك") || text.contains("اللوكيشن")) {
            return listOf(
                "سأشارك موقعي المباشر الآن 📍",
                "بجوار المعلم الرئيسي 🏢",
                "أرسلت لك الإحداثيات 🗺️"
            )
        }

        // 5. رسائل الإنجاز والانتهاء
        if (text.contains("خلصت") || text.contains("تم") || text.contains("جاهز") || text.contains("انتهيت")) {
            return listOf(
                "شكراً جزيلاً، تم الاستلام ✔️",
                "تسلم يدك، عمل متقن ⭐️",
                "سأقوم بالتقييم الآن ⭐️⭐️⭐️⭐️⭐️"
            )
        }

        // 6. التحيات والبدء
        if (text.contains("مرحبا") || text.contains("سلام") || text.contains("مساء") || text.contains("صباح") || text.contains("أهلا")) {
            return listOf(
                "أهلاً بك، كيف يمكنني مساعدتك؟",
                "حياك الله، أنا في خدمتك 🤝",
                "وعليكم السلام ورحمة الله ✨"
            )
        }

        // 7. وسائط مرفقة
        if (lastMessage.mediaType == MediaType.IMAGE) {
            return listOf("تمت معاينة الصورة بوضوح 🔍", "سأحدد القطعة المطلوبة 🔧", "جاري مراجعة التفاصيل 📋")
        }
        if (lastMessage.mediaType == MediaType.LOCATION) {
            return listOf("تم استلام الموقع وجاري التوجه 🚗", "أنا قريب من المكان 📍", "شكراً للمشاركة 👍")
        }
        if (lastMessage.mediaType == MediaType.AUDIO) {
            return listOf("استمعت للتسجيل، كل شيء واضح 🎧", "سأوافيك بالرد فوراً 🤝")
        }

        // مقترحات عامة سريعة
        return listOf("تمام، متفقين 👍", "سأتواصل معك بعد قليل 📞", "شكراً لك ✨")
    }

    /**
     * توليد ملخص محلي ذكي لتاريخ المحادثة (خاصة عند فتحها بعد غياب)
     */
    fun summarizeMessages(messages: List<ChatMessage>): SmartSummary? {
        if (messages.size < 5) return null

        var textCount = 0
        var imageCount = 0
        var audioCount = 0
        var locationCount = 0

        messages.forEach { msg ->
            when (msg.mediaType) {
                MediaType.TEXT -> textCount++
                MediaType.IMAGE -> imageCount++
                MediaType.AUDIO -> audioCount++
                MediaType.LOCATION -> locationCount++
                else -> textCount++
            }
        }

        val detailsBuilder = mutableListOf<String>()
        if (imageCount > 0) detailsBuilder.add("$imageCount صورة 📷")
        if (audioCount > 0) detailsBuilder.add("$audioCount تسجيل صوتي 🎤")
        if (locationCount > 0) detailsBuilder.add("موقع مباشر 📍")
        if (detailsBuilder.isEmpty()) detailsBuilder.add("$textCount رسالة نصية 💬")

        return SmartSummary(
            totalCount = messages.size,
            textCount = textCount,
            imageCount = imageCount,
            audioCount = audioCount,
            locationCount = locationCount,
            summaryTitle = "ملخص المحادثة (${messages.size} رسالة)",
            summaryDetails = detailsBuilder.joinToString(" · ")
        )
    }
}
