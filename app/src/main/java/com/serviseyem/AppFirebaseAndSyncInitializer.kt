package com.serviseyem

import android.content.Context
import android.util.Log
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseOptions
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreSettings
import com.serviseyem.sync.PeriodicSyncScheduler
import com.serviseyem.utils.ChatNotificationHelper
import com.serviseyem.utils.NotificationChannels
import com.serviseyem.utils.NotificationHelper

/**
 * تهيئة Firebase و Firestore و Analytics وقنوات الإشعارات وجدولة المزامنة الدورية عند إقلاع التطبيق.
 */
internal object AppFirebaseAndSyncInitializer {

    fun initializeFirebaseServices(context: Context): FirebaseAnalytics? {
        // ===================== الخطوة 1: تهيئة Firebase وحساب Google بأمان =====================
        try {
            if (FirebaseApp.getApps(context).isEmpty()) {
                try {
                    val options = FirebaseOptions.Builder()
                        .setApplicationId("1:954106721012:android:5fa5e385532b08d5c0e4a1")
                        .setApiKey("AIzaSyBq2SEhBADFGVF4sDyV3sC_t2HqQ1m8lC0")
                        .setProjectId("serviseyem")
                        .setGcmSenderId("954106721012")
                        .setStorageBucket("serviseyem.firebasestorage.app")
                        .build()
                    FirebaseApp.initializeApp(context, options)
                } catch (_: Throwable) {
                    FirebaseApp.initializeApp(context)
                }
            }
            Log.d("MyApplication", "✅ Firebase initialized successfully")
        } catch (e: Throwable) {
            Log.e("MyApplication", "❌ Firebase initialization failed: ${e.message}")
            try {
                FirebaseApp.initializeApp(context)
            } catch (_: Throwable) {}
        }

        // ===================== تهيئة Firestore الموحدة =====================
        try {
            val firestore = FirebaseFirestore.getInstance()
            val settings = FirebaseFirestoreSettings.Builder()
                .setPersistenceEnabled(true)
                .setCacheSizeBytes(100 * 1024 * 1024L) // 100MB limit to prevent storage bloat
                .build()
            firestore.firestoreSettings = settings
            Log.d("MyApplication", "✅ FirebaseFirestore settings initialized successfully")
        } catch (e: Throwable) {
            try {
                Log.w("MyApplication", "⚠️ Firestore settings notice: ${e.message}")
            } catch (_: Throwable) {}
        }

        // ===================== الخطوة 2: تهيئة Analytics بأمان =====================
        return try {
            FirebaseAnalytics.getInstance(context)
        } catch (_: Throwable) {
            null
        }
    }

    fun initializeNotificationChannelsAndSync(context: Context) {
        // ===================== الخطوة 3: تهيئة قنوات الإشعارات الموحدة =====================
        try {
            NotificationChannels.createAll(context)
            Log.d("MyApplication", "✅ Unified notification channels created successfully")
        } catch (e: Throwable) {
            Log.e("MyApplication", "❌ Failed to create notification channels: ${e.message}")
        }

        try {
            NotificationHelper.createNotificationChannels(context)
            Log.d("MyApplication", "✅ Admin notification channels created successfully")
        } catch (e: Throwable) {
            Log.e("MyApplication", "❌ Failed to create admin notification channels: ${e.message}")
        }

        try {
            ChatNotificationHelper.createNotificationChannels(context)
            Log.d("MyApplication", "✅ Chat notification channels created successfully")
        } catch (e: Throwable) {
            Log.e("MyApplication", "❌ Failed to create chat notification channels: ${e.message}")
        }

        // ===================== الخطوة 4: جدولة المزامنة الدورية وحساب المزامنة بأمان =====================
        try {
            PeriodicSyncScheduler(context).schedulePeriodicSync()
            Log.d("MyApplication", "✅ Background periodic sync scheduled successfully")
        } catch (e: Throwable) {
            Log.w("MyApplication", "⚠️ Notice on periodic sync: ${e.message}")
        }

        // ===================== الخطوة 6: إدراج البيانات التجريبية الأولية في Room =====================
        // تم تعطيل الاستدعاء التلقائي في الخلفية منعاً لأي استثناء محتمل عند بدء التشغيل
    }
}
