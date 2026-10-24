package com.serviseyem.utils

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.core.content.ContextCompat

/**
 * 🛡️ PermissionManager - مدير الأذونات الديناميكية لطلب والتحقق من أذونات النظام بشكل احترافي
 * يدعم توفير رسائل الشرح المخصصة باللغة العربية وإمكانية التوجيه لإعدادات التطبيق.
 */
object PermissionManager {

    // Common system permissions
    const val PERMISSION_CAMERA = Manifest.permission.CAMERA
    const val PERMISSION_RECORD_AUDIO = Manifest.permission.RECORD_AUDIO
    const val PERMISSION_FINE_LOCATION = Manifest.permission.ACCESS_FINE_LOCATION
    const val PERMISSION_COARSE_LOCATION = Manifest.permission.ACCESS_COARSE_LOCATION
    const val PERMISSION_NOTIFICATIONS = "android.permission.POST_NOTIFICATIONS"

    val PERMISSION_STORAGE_IMAGES: String
        get() = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            Manifest.permission.READ_MEDIA_IMAGES
        } else {
            Manifest.permission.READ_EXTERNAL_STORAGE
        }

    /**
     * التحقق مما إذا كان الإذن الممكن ممنوحاً
     */
    fun hasPermission(context: Context, permission: String): Boolean {
        return ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED
    }

    /**
     * التحقق من مجموعة أذونات معاً
     */
    fun hasAllPermissions(context: Context, permissions: Array<String>): Boolean {
        return permissions.all { hasPermission(context, it) }
    }

    /**
     * الحصول على رسالة الشرح (Rationale) المخصصة باللغة العربية قبل طلب الإذن
     */
    fun getRationaleForPermission(permission: String): String {
        return when (permission) {
            PERMISSION_CAMERA -> "يحتاج التطبيق إلى الوصول لكاميرا الجهاز لالتقاط صور المعرض، الهوية أو الترخيص مباشرة."
            PERMISSION_RECORD_AUDIO -> "يحتاج التطبيق لإذن الميكروفون لتسجيل وإرسال الرسائل الصوتية والفويس نوت في المحادثات المباشرة."
            PERMISSION_STORAGE_IMAGES, Manifest.permission.READ_EXTERNAL_STORAGE -> "يحتاج التطبيق لإذن الوصول للصغار والمعرض لاختيار الصور وإرفاق السير الذاتية."
            PERMISSION_FINE_LOCATION, PERMISSION_COARSE_LOCATION -> "يحتاج التطبيق لتحديد موقعك الجغرافي لعرض الفنيين والمتاجر القريبة منك باليمن."
            PERMISSION_NOTIFICATIONS -> "يحتاج التطبيق لإذن الإشعارات لتنبيهك بجديد العروض والرسائل وتحديثات طلبات الانضمام."
            else -> "يحتاج التطبيق إلى هذا الإذن لضمان تقديم كافة الخدمات بكفاءة عالية."
        }
    }

    /**
     * التوجيه لإعدادات التطبيق في حالة الرفض الدائم
     */
    fun openAppSettings(context: Context) {
        try {
            val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                data = Uri.fromParts("package", context.packageName, null)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
