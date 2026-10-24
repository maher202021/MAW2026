package com.serviseyem

import android.app.Application
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import com.google.firebase.FirebaseApp
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreSettings
import com.google.firebase.appcheck.FirebaseAppCheck
import com.google.firebase.appcheck.playintegrity.PlayIntegrityAppCheckProviderFactory
import com.google.firebase.appcheck.debug.DebugAppCheckProviderFactory
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class MyApplication : Application() {

    companion object {
        @Volatile
        var instance: MyApplication? = null
            private set
        @Volatile
        private var firebaseAnalytics: FirebaseAnalytics? = null
        @Volatile
        private var isCrashlyticsReady = false

        @JvmStatic
        fun logFirebaseEvent(name: String, params: Bundle) {
            try {
                firebaseAnalytics?.logEvent(name, params)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    override fun onCreate() {
        Thread.setDefaultUncaughtExceptionHandler { thread, throwable ->
            try {
                val logFile = java.io.File(filesDir, "crash_log.txt")
                logFile.appendText("CRASH at ${java.util.Date()}: ${throwable.stackTraceToString()}\n")
            } catch (_: Exception) {}
            android.util.Log.e("CRASH_DIAGNOSTIC", "UNCAUGHT EXCEPTION in thread ${thread.name}", throwable)
        }
        super.onCreate()
        instance = this

        // ===================== الخطوة 1: تهيئة Firebase بأمان =====================
        try {
            if (FirebaseApp.getApps(this).isEmpty()) {
                FirebaseApp.initializeApp(this)
            }
            Log.d("MyApplication", "✅ Firebase initialized successfully")
        } catch (e: Throwable) {
            Log.e("MyApplication", "❌ Firebase initialization failed: ${e.message}")
            e.printStackTrace()
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

        // ===================== الخطوة 2: تهيئة Analytics بأمان (تم التعطيل للاختبار) =====================
        /*
        try {
            firebaseAnalytics = FirebaseAnalytics.getInstance(this)
            Log.d("MyApplication", "✅ FirebaseAnalytics initialized successfully")
        } catch (e: Throwable) {
            Log.e("MyApplication", "❌ FirebaseAnalytics initialization failed: ${e.message}")
            e.printStackTrace()
        }
        */

        // ===================== الخطوة 3: تهيئة Crashlytics بأمان (تم التعطيل للاختبار) =====================
        // initializeCrashlyticsSafely()

        // ===================== الخطوة 4: تهيئة قنوات الإشعارات الموحدة =====================
        try {
            com.serviseyem.utils.NotificationChannels.createAll(this)
            Log.d("MyApplication", "✅ Unified notification channels created successfully")
        } catch (e: Throwable) {
            Log.e("MyApplication", "❌ Failed to create notification channels: ${e.message}")
        }

        try {
            com.serviseyem.utils.NotificationHelper.createNotificationChannels(this)
            Log.d("MyApplication", "✅ Admin notification channels created successfully")
        } catch (e: Throwable) {
            Log.e("MyApplication", "❌ Failed to create admin notification channels: ${e.message}")
        }

        try {
            com.serviseyem.utils.ChatNotificationHelper.createNotificationChannels(this)
            Log.d("MyApplication", "✅ Chat notification channels created successfully")
        } catch (e: Throwable) {
            Log.e("MyApplication", "❌ Failed to create chat notification channels: ${e.message}")
        }

        // ===================== الخطوة 5: جدولة المزامنة الدورية في الخلفية (تم التعطيل بناءً على طلب المستخدم) =====================
        /*
        try {
            com.serviseyem.sync.PeriodicSyncScheduler(this).schedulePeriodicSync()
            Log.d("MyApplication", "✅ Background periodic sync scheduled successfully")
        } catch (e: Throwable) {
            Log.e("MyApplication", "❌ Failed to schedule periodic sync: ${e.message}")
        }
        */

        // ===================== الخطوة 6: إدراج البيانات التجريبية الأولية في Room =====================
        // تم تعطيل الاستدعاء التلقائي في الخلفية منعاً لأي استثناء محتمل عند بدء التشغيل
    }


    /**
     * تهيئة Crashlytics بأمان مع:
     * 1. تأخير 3 ثوانٍ لضمان اكتمال تهيئة Firebase
     * 2. try-catch شامل لمنع إغلاق التطبيق
     * 3. حفظ حالة التهيئة لمنع المحاولات المتكررة الفاشلة
     */
    private fun initializeCrashlyticsSafely() {
        if (isCrashlyticsReady) {
            Log.d("MyApplication", "✅ Crashlytics already initialized")
            return
        }

        try {
            Handler(Looper.getMainLooper()).postDelayed({
                try {
                    val crashlytics = FirebaseCrashlytics.getInstance()
                    // 🚨 M-10: التحقق من موافقة المستخدم الإحصائية والتشخيصية للامتثال لسياسات الخصوصية
                    val prefs = getSharedPreferences("yemen_service_prefs", android.content.Context.MODE_PRIVATE)
                    val isConsentGranted = prefs.getBoolean("user_diagnostics_consent", true)
                    crashlytics.setCrashlyticsCollectionEnabled(isConsentGranted)
                    isCrashlyticsReady = true
                    Log.d("MyApplication", "✅ Firebase Crashlytics initialized successfully (Consent=$isConsentGranted)")

                    try {
                        crashlytics.log("Crashlytics initialized successfully")
                    } catch (_: Throwable) { /* تجاهل */ }
                } catch (e: Throwable) {
                    Log.e("MyApplication", "❌ Firebase Crashlytics initialization failed: ${e.message}")
                    tryAlternativeCrashlyticsInit()
                }
            }, 3000)
        } catch (e: Throwable) {
            Log.e("MyApplication", "❌ Failed to schedule Crashlytics init: ${e.message}")
        }
    }

    /**
     * محاولة بديلة لتهيئة Crashlytics (كحل أخير)
     */
    private fun tryAlternativeCrashlyticsInit() {
        try {
            val crashlytics = FirebaseCrashlytics.getInstance()
            crashlytics.setCrashlyticsCollectionEnabled(true)
            isCrashlyticsReady = true
            Log.d("MyApplication", "✅ Firebase Crashlytics initialized (alternative method)")
        } catch (e: Throwable) {
            Log.e("MyApplication", "❌ Firebase Crashlytics alternative initialization failed: ${e.message}")
            // التطبيق يستمر في العمل بدون Crashlytics
        }
    }

    /**
     * دالة آمنة لتسجيل الاستثناءات في Crashlytics
     */
    fun recordExceptionSafely(throwable: Throwable) {
        if (isCrashlyticsReady) {
            try {
                FirebaseCrashlytics.getInstance().recordException(throwable)
            } catch (e: Throwable) {
                Log.e("MyApplication", "Failed to record exception: ${e.message}")
            }
        } else {
            try {
                FirebaseCrashlytics.getInstance().recordException(throwable)
                isCrashlyticsReady = true
            } catch (e: Throwable) {
                Log.e("MyApplication", "Crashlytics not ready: ${throwable.message}")
            }
        }
    }
}
