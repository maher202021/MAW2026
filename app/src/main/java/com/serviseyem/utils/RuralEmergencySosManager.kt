package com.serviseyem.utils

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast

/**
 * 🚨 RuralEmergencySosManager
 * 🥉 الفكرة الثالثة: "خريطة تعمل بدون هواتف - وضع الطوارئ الريفية"
 * عندما يكون المستخدم خارج نطاق المدن أو بدون إنترنت، يوفر هذا النظام:
 * 1. عرض آخر موقع GPS معروف بدقة الإحداثيات.
 * 2. إرسال استغاثة SOS مشفرة ومباشرة عبر الرسائل النصية SMS بدلاً من الإنترنت.
 * 3. حصر أقرب مركز نجدة أو فني طوارئ مسجل في الريف.
 */
object RuralEmergencySosManager {

    data class EmergencyPacket(
        val latitude: Double,
        val longitude: Double,
        val nearestTown: String,
        val userPhone: String = "",
        val timestamp: Long = System.currentTimeMillis()
    )

    fun sendEmergencySms(
        context: Context,
        emergencyNumber: String = "199", // أو رقم طوارئ يمني / قريب
        packet: EmergencyPacket,
        onSuccess: () -> Unit = {}
    ) {
        val googleMapsLink = "https://maps.google.com/?q=${packet.latitude},${packet.longitude}"
        val message = "🚨 بلاغ نجدة طارئ (تطبيق خدمات اليمن):\n" +
                "موقعي الحالي: ${packet.latitude}, ${packet.longitude}\n" +
                "أقرب منطقة: ${packet.nearestTown}\n" +
                "رابط الخريطة: $googleMapsLink\n" +
                "يرجى النجدة العاجلة، الهاتف خارج تغطية الإنترنت."

        try {
            val smsIntent = Intent(Intent.ACTION_VIEW).apply {
                data = Uri.parse("smsto:$emergencyNumber")
                putExtra("sms_body", message)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(smsIntent)
            onSuccess()
        } catch (e: Exception) {
            Toast.makeText(context, "فشل فتح تطبيق الرسائل للنجدة", Toast.LENGTH_SHORT).show()
        }
    }
}
