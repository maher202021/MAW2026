package com.serviseyem

import android.content.Context
import android.os.Handler
import android.os.Looper
import android.util.Log
import com.google.firebase.crashlytics.FirebaseCrashlytics
import java.io.File
import java.util.Date

/**
 * تشخيص الأعطال غير المتوقعة، التهيئة الآمنة لـ Crashlytics، وتسجيل الاستثناءات بأمان.
 */
internal object AppCrashDiagnosticsInitializer {

    fun installUncaughtExceptionHandler(filesDir: File) {
        val defaultHandler = Thread.getDefaultUncaughtExceptionHandler()
        Thread.setDefaultUncaughtExceptionHandler { thread, throwable ->
            try {
                val logFile = File(filesDir, "crash_log.txt")
                logFile.appendText("CRASH at ${Date()}: ${throwable.stackTraceToString()}\n")
            } catch (_: Exception) {}
            Log.e("CRASH_DIAGNOSTIC", "UNCAUGHT EXCEPTION in thread ${thread.name}", throwable)
            defaultHandler?.uncaughtException(thread, throwable)
        }
    }

    /**
     * تهيئة Crashlytics بأمان مع:
     * 1. تأخير 3 ثوانٍ لضمان اكتمال تهيئة Firebase
     * 2. try-catch شامل لمنع إغلاق التطبيق
     * 3. حفظ حالة التهيئة لمنع المحاولات المتكررة الفاشلة
     */
    @Suppress("unused")
    fun initializeCrashlyticsSafely(
        context: Context,
        isReady: () -> Boolean,
        markReady: () -> Unit
    ) {
        if (isReady()) {
            Log.d("MyApplication", "✅ Crashlytics already initialized")
            return
        }

        try {
            Handler(Looper.getMainLooper()).postDelayed({
                try {
                    val crashlytics = FirebaseCrashlytics.getInstance()
                    // 🚨 M-10: التحقق من موافقة المستخدم الإحصائية والتشخيصية للامتثال لسياسات الخصوصية
                    val prefs = context.getSharedPreferences("yemen_service_prefs", Context.MODE_PRIVATE)
                    val isConsentGranted = prefs.getBoolean("user_diagnostics_consent", true)
                    crashlytics.setCrashlyticsCollectionEnabled(isConsentGranted)
                    markReady()
                    Log.d("MyApplication", "✅ Firebase Crashlytics initialized successfully (Consent=$isConsentGranted)")

                    try {
                        crashlytics.log("Crashlytics initialized successfully")
                    } catch (_: Throwable) { /* تجاهل */ }
                } catch (e: Throwable) {
                    Log.e("MyApplication", "❌ Firebase Crashlytics initialization failed: ${e.message}")
                    tryAlternativeCrashlyticsInit(markReady)
                }
            }, 3000)
        } catch (e: Throwable) {
            Log.e("MyApplication", "❌ Failed to schedule Crashlytics init: ${e.message}")
        }
    }

    /**
     * محاولة بديلة لتهيئة Crashlytics (كحل أخير)
     */
    private fun tryAlternativeCrashlyticsInit(markReady: () -> Unit) {
        try {
            val crashlytics = FirebaseCrashlytics.getInstance()
            crashlytics.setCrashlyticsCollectionEnabled(true)
            markReady()
            Log.d("MyApplication", "✅ Firebase Crashlytics initialized (alternative method)")
        } catch (e: Throwable) {
            Log.e("MyApplication", "❌ Firebase Crashlytics alternative initialization failed: ${e.message}")
            // التطبيق يستمر في العمل بدون Crashlytics
        }
    }

    /**
     * دالة آمنة لتسجيل الاستثناءات في Crashlytics
     */
    fun recordExceptionSafely(
        throwable: Throwable,
        isReady: () -> Boolean,
        markReady: () -> Unit
    ) {
        if (isReady()) {
            try {
                FirebaseCrashlytics.getInstance().recordException(throwable)
            } catch (e: Throwable) {
                Log.e("MyApplication", "Failed to record exception: ${e.message}")
            }
        } else {
            try {
                FirebaseCrashlytics.getInstance().recordException(throwable)
                markReady()
            } catch (e: Throwable) {
                Log.e("MyApplication", "Crashlytics not ready: ${throwable.message}")
            }
        }
    }
}
