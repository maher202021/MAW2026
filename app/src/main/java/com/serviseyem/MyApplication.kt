package com.serviseyem

import android.app.Application
import android.os.Bundle
import com.google.firebase.analytics.FirebaseAnalytics
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
        AppCrashDiagnosticsInitializer.installUncaughtExceptionHandler(filesDir)
        super.onCreate()
        instance = this

        firebaseAnalytics = AppFirebaseAndSyncInitializer.initializeFirebaseServices(this)
        AppFirebaseAndSyncInitializer.initializeNotificationChannelsAndSync(this)

        // [نظام خرائط وملاحة عالمي - ملكنا]
        Thread {
            try {
                com.serviseyem.utils.OfflineTileInterceptor.migrateCacheIfNeeded(this)
                com.serviseyem.utils.BundledPacks.installIfNeeded(this)
            } catch (_: Throwable) {
            }
        }.start()
    }

    @Suppress("unused")
    private fun initializeCrashlyticsSafely() {
        AppCrashDiagnosticsInitializer.initializeCrashlyticsSafely(
            context = this,
            isReady = { isCrashlyticsReady },
            markReady = { isCrashlyticsReady = true }
        )
    }

    /**
     * دالة آمنة لتسجيل الاستثناءات في Crashlytics
     */
    fun recordExceptionSafely(throwable: Throwable) {
        AppCrashDiagnosticsInitializer.recordExceptionSafely(
            throwable = throwable,
            isReady = { isCrashlyticsReady },
            markReady = { isCrashlyticsReady = true }
        )
    }
}
