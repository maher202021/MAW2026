package com.serviseyem.utils

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast

object PhoneDialerHelper {
    fun dial(context: Context, phone: String) {
        if (phone.isBlank()) {
            Toast.makeText(context, "لا يوجد رقم هاتف", Toast.LENGTH_SHORT).show()
            return
        }
        try {
            context.startActivity(Intent(Intent.ACTION_DIAL, Uri.parse("tel:$phone")))
        } catch (e: ActivityNotFoundException) {
            Toast.makeText(context, "لا يمكن فتح تطبيق الاتصال", Toast.LENGTH_SHORT).show()
        }
    }
}
