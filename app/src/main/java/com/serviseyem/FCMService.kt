package com.serviseyem

import com.serviseyem.utils.*
import com.serviseyem.ui.helpers.AppPreferenceHelper
import kotlinx.coroutines.*

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Handler
import android.os.Looper
import androidx.core.app.NotificationCompat
import com.google.firebase.FirebaseApp
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

class FCMService : FirebaseMessagingService() {

    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    companion object {
        private val notificationSeq = java.util.concurrent.atomic.AtomicInteger(1000)
        private fun nextNotificationId(): Int {
            val base = (System.currentTimeMillis() % 100000).toInt()
            val offset = notificationSeq.incrementAndGet() % 10000
            return kotlin.math.abs(base * 10 + offset)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        serviceScope.cancel()
    }

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)

        val data = remoteMessage.data
        val type = data["type"] ?: data["notificationType"] ?: ""

        // 🔑 معالجة طلبات استعادة كلمة المرور الحرجة للأدمن
        if (type.equals("PASSWORD_RECOVERY", ignoreCase = true)) {
            val requestId = data["requestId"] ?: ""
            val phone = data["phone"] ?: ""
            val name = data["name"] ?: "غير محدد"
            val accountType = data["accountType"] ?: "حساب"
            val notificationId = if (requestId.isNotBlank()) {
                requestId.hashCode()
            } else {
                nextNotificationId()
            }

            val intent = Intent(this, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
                putExtra("navigate_to", "PASSWORD_RECOVERY_PANEL")
                putExtra("target_screen", "ADMIN_PANEL")
                putExtra("requestId", requestId)
            }

            val pendingIntent = PendingIntent.getActivity(
                this,
                notificationId,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            val notification = NotificationCompat.Builder(
                this,
                NotificationHelper.CHANNEL_ADMIN_CRITICAL
            )
                .setSmallIcon(android.R.drawable.ic_dialog_info)
                .setContentTitle("🔑 طلب استعادة كلمة مرور")
                .setContentText("$accountType - $name ($phone)")
                .setStyle(
                    NotificationCompat.BigTextStyle()
                        .bigText(
                            "طلب استعادة كلمة مرور جديد\n\n" +
                            "النوع: $accountType\n" +
                            "الاسم: $name\n" +
                            "الهاتف: $phone\n\n" +
                            "اضغط للمعالجة الفورية"
                        )
                )
                .setPriority(NotificationCompat.PRIORITY_MAX)
                .setCategory(NotificationCompat.CATEGORY_ALARM)
                .setColor(android.graphics.Color.RED)
                .setAutoCancel(true)
                .setContentIntent(pendingIntent)
                .setVibrate(longArrayOf(0, 500, 200, 500))
                .setDefaults(NotificationCompat.DEFAULT_ALL)
                .setVisibility(NotificationCompat.VISIBILITY_PRIVATE) // 🚨 H-06: إخفاء البيانات الحساسة على شاشة القفل لضمان الخصوصية والأمان
                .build()

            notificationManager.notify(notificationId, notification)
            return
        }

        if (type.equals("CHAT", ignoreCase = true) || data.containsKey("channelId")) {
            val channelId = data["channelId"] ?: ""
            val senderId = data["senderId"] ?: ""
            val senderName = data["senderName"] ?: "رسالة جديدة"
            val messageText = data["message"] ?: remoteMessage.notification?.body ?: "لديك رسالة جديدة"
            val mediaType = data["mediaType"] ?: "TEXT"
            val mediaUrl = data["mediaUrl"]

            ChatNotificationHelper.showChatMessageNotification(
                context = this,
                notificationId = nextNotificationId(),
                channelId = channelId,
                senderId = senderId,
                senderName = senderName,
                messageText = messageText,
                mediaType = mediaType,
                mediaUrl = mediaUrl
            )
            return
        }

        if (type.equals("URGENT", ignoreCase = true) || data.containsKey("requestCode")) {
            val requestCode = data["requestCode"] ?: ""
            val title = data["title"] ?: remoteMessage.notification?.title ?: "طلب طوارئ عاجل"
            val description = data["description"] ?: remoteMessage.notification?.body ?: ""
            val city = data["city"] ?: ""

            ChatNotificationHelper.showUrgentRequestNotification(
                context = this,
                notificationId = nextNotificationId(),
                requestCode = requestCode,
                title = title,
                description = description,
                city = city
            )
            return
        }

        val title = remoteMessage.notification?.title ?: remoteMessage.data["title"] ?: "تحديث جديد 🔔"
        val body = remoteMessage.notification?.body ?: remoteMessage.data["body"] ?: "لديك إشعار جديد في تطبيق خدمات اليمن."
        val targetScreen = remoteMessage.data["targetScreen"]
            ?: remoteMessage.data["target_screen"]
            ?: remoteMessage.data["navigate_to"]
            ?: "MAIN"

        sendLocalNotification(title, body, targetScreen)
    }

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        if (token.isBlank()) return
        
        // 🔒 الخطوة 1: حفظ التوكن في التخزين المشفر فوراً (H-03: Encrypted token storage) مع حفظ النسخة الاحتياطية المتوافقة
        try {
            val secureStorage = com.serviseyem.utils.SecureStorage(this)
            secureStorage.saveSecureString("fcm_token_backup", token)
        } catch (e: Exception) {
            android.util.Log.w("FCMService", "SecureStorage token save fallback: ${e.message}")
        }
        try {
            val sp = getSharedPreferences("yemen_service_prefs", Context.MODE_PRIVATE)
            sp.edit().putString("fcm_token_backup", token).apply()
        } catch (_: Exception) {}
        
        // 🔄 الخطوة 2: مزامنة مع Firestore باستخدام serviceScope المدار لمنع تسريب الخيوط
        serviceScope.launch {
            try {
                // التحقق من تهيئة Firebase
                if (FirebaseApp.getApps(this@FCMService).isEmpty()) {
                    try {
                        FirebaseApp.initializeApp(this@FCMService)
                    } catch (e: Exception) {
                        android.util.Log.e("FCMService", "FirebaseApp init failed: ${e.message}")
                        return@launch
                    }
                }
                
                val db = FirebaseFirestore.getInstance()
                
                // 🔑 جلب المعرف الموثوق من FirebaseAuth كمرجع أمان أساسي (H-05) مع التراجع الآمن
                val firebaseUid = try {
                    com.google.firebase.auth.FirebaseAuth.getInstance().currentUser?.uid
                } catch (_: Exception) {
                    null
                }
                
                val sp = getSharedPreferences("yemen_service_prefs", Context.MODE_PRIVATE)
                val rawUserId = sp.getString("user_id", "") ?: ""
                val localUserId = if (rawUserId.isNotEmpty() && rawUserId != "guest") {
                    if (rawUserId.startsWith("gcm:")) {
                        try {
                            val dec = SecurityCryptoUtils.decrypt(rawUserId)
                            if (dec.isEmpty() || dec.startsWith("gcm:")) {
                                android.util.Log.e("FCMService", "Failed to decrypt user_id from SharedPreferences")
                                ""
                            } else dec
                        } catch (e: Exception) {
                            android.util.Log.e("FCMService", "Exception decrypting user_id", e)
                            ""
                        }
                    } else {
                        try {
                            SecurityCryptoUtils.decrypt(rawUserId).ifEmpty { rawUserId }
                        } catch (e: Exception) {
                            android.util.Log.e("FCMService", "Failed to decrypt legacy user_id", e)
                            ""
                        }
                    }
                } else rawUserId

                val userId = if (!firebaseUid.isNullOrBlank()) firebaseUid else localUserId
                
                if (userId.isEmpty() || userId == "guest" || userId.startsWith("gcm:")) {
                    return@launch
                }

                val rawPhone = sp.getString("user_phone", "") ?: ""
                val phone = if (rawPhone.isNotEmpty()) {
                    if (rawPhone.startsWith("gcm:")) {
                        try {
                            val dec = SecurityCryptoUtils.decrypt(rawPhone)
                            if (dec.isEmpty() || dec.startsWith("gcm:")) {
                                android.util.Log.e("FCMService", "Failed to decrypt user_phone from SharedPreferences")
                                ""
                            } else dec
                        } catch (e: Exception) {
                            android.util.Log.e("FCMService", "Exception decrypting user_phone", e)
                            ""
                        }
                    } else {
                        try {
                            SecurityCryptoUtils.decrypt(rawPhone).ifEmpty { rawPhone }
                        } catch (e: Exception) {
                            android.util.Log.e("FCMService", "Failed to decrypt legacy user_phone", e)
                            ""
                        }
                    }
                } else ""
                val cleanPhone = AppPreferenceHelper.normalizePhoneNumber(phone.trim())

                val rawRole = sp.getString("user_role", "") ?: ""
                val resolvedRole = if (rawRole.isNotEmpty()) {
                    if (rawRole.startsWith("gcm:")) {
                        try {
                            val dec = SecurityCryptoUtils.decrypt(rawRole)
                            if (dec.isBlank() || dec.startsWith("gcm:")) {
                                android.util.Log.e("FCMService", "Failed to decrypt user_role from SharedPreferences")
                                "CLIENT"
                            } else dec
                        } catch (e: Exception) {
                            android.util.Log.e("FCMService", "Exception decrypting user_role", e)
                            "CLIENT"
                        }
                    } else {
                        rawRole.ifBlank { "CLIENT" }
                    }
                } else "CLIENT"

                val now = System.currentTimeMillis()

                // 1. تحديث fcm_tokens بشكل مستقل وآمن باستخدام merge لضمان عدم فشل العملية
                val tokenData = mapOf(
                    "token" to token,
                    "role" to resolvedRole,
                    "updatedAt" to now
                )
                
                // أ: التحديث في المستند الأساسي للتوافقية الكاملة
                db.collection("fcm_tokens").document(userId)
                    .set(tokenData, SetOptions.merge())
                    .addOnFailureListener { e ->
                        android.util.Log.w("FCMService", "Failed to sync main fcm_tokens document: ${e.message}")
                    }
                    .addOnSuccessListener {
                        android.util.Log.d("FCMService", "Main FCM token synced successfully.")
                    }

                // ب: التحديث في مجموعة الأجهزة الفرعية باستخدام SHA-256 لمنع حذف التوكنات للأجهزة المتعددة (M-12: Support Multi-Device)
                try {
                    val tokenHash = java.security.MessageDigest.getInstance("SHA-256")
                        .digest(token.toByteArray(Charsets.UTF_8))
                        .joinToString("") { "%02x".format(it) }
                        .take(32)
                    
                    db.collection("fcm_tokens").document(userId)
                        .collection("devices").document(tokenHash)
                        .set(tokenData, SetOptions.merge())
                        .addOnFailureListener { e ->
                            android.util.Log.w("FCMService", "Failed to sync fcm_tokens device document: ${e.message}")
                        }
                        .addOnSuccessListener {
                            android.util.Log.d("FCMService", "FCM token device document synced successfully ($tokenHash).")
                        }
                } catch (hashEx: Exception) {
                    android.util.Log.w("FCMService", "SHA-256 token hashing skipped: ${hashEx.message}")
                }

                // 2. تحديث registered_users باستخدام cleanPhone أو userId مع SetOptions.merge لتفادي فشل التحديث
                val targetUserDocId = if (cleanPhone.isNotBlank() && cleanPhone.length >= 7) cleanPhone else userId
                db.collection("registered_users").document(targetUserDocId)
                    .set(mapOf("fcmToken" to token), SetOptions.merge())
                    .addOnFailureListener { e ->
                        android.util.Log.w("FCMService", "Skipped registered_users token update: ${e.message}")
                    }
                    .addOnSuccessListener {
                        android.util.Log.d("FCMService", "registered_users token updated successfully ($targetUserDocId).")
                    }

                // 3. تحديث providers, stores, properties بشكل مستقل لكل مجموعة (مع دعم المعرفات القياسية المسبوقة بـ prov_ / store_ / prop_)
                if (cleanPhone.isNotEmpty() && !cleanPhone.startsWith("gcm:") && cleanPhone.length >= 7) {
                    listOf("prov_$cleanPhone", cleanPhone).distinct().forEach { docId ->
                        db.collection("providers").document(docId)
                            .update("fcmToken", token)
                            .addOnFailureListener { e ->
                                android.util.Log.w("FCMService", "Provider token update skipped ($docId): ${e.message}")
                            }
                    }
                    
                    listOf("store_$cleanPhone", cleanPhone).distinct().forEach { docId ->
                        db.collection("stores").document(docId)
                            .update("fcmToken", token)
                            .addOnFailureListener { e ->
                                android.util.Log.w("FCMService", "Store token update skipped ($docId): ${e.message}")
                            }
                    }

                    listOf("prop_$cleanPhone", cleanPhone).distinct().forEach { docId ->
                        db.collection("properties").document(docId)
                            .update("fcmToken", token)
                            .addOnFailureListener { e ->
                                android.util.Log.w("FCMService", "Property token update skipped ($docId): ${e.message}")
                            }
                    }
                }

            } catch (e: Exception) {
                android.util.Log.e("FCMService", "Error during FCM token sync", e)
            }
        }
    }

    private fun sendLocalNotification(title: String, body: String, targetScreen: String = "MAIN") {
        val channelId = "yemen_services_fcm_channel"
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "إشعارات الخدمة والحجوزات والمحادثات",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "قناة مخصصة للتنبيهات الفورية بتحديثات الحالة والرسائل والطلبات"
                enableLights(true)
                enableVibration(true)
            }
            notificationManager.createNotificationChannel(channel)
        }

        val notificationId = nextNotificationId()
        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
            putExtra("target_screen", targetScreen)
            putExtra("navigate_to", targetScreen)
        }
        val pendingIntent = PendingIntent.getActivity(
            this,
            notificationId,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val builder = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle(title)
            .setContentText(body)
            .setStyle(NotificationCompat.BigTextStyle().bigText(body))
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)

        notificationManager.notify(notificationId, builder.build())
    }
}
