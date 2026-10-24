package com.serviseyem.di

import android.content.Context
import com.serviseyem.security.SecurityManager
import com.serviseyem.utils.SecureStorage
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreSettings
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object FirebaseModule {

    private fun ensureFirebaseInitialized(context: Context) {
        if (FirebaseApp.getApps(context).isEmpty()) {
            try {
                val options = com.google.firebase.FirebaseOptions.Builder()
                    .setApplicationId("1:954106721012:android:5fa5e385532b08d5c0e4a1")
                    .setApiKey("AIzaSyBq2SEhBADFGVF4sDyV3sC_t2HqQ1m8lC0")
                    .setProjectId("serviseyem")
                    .setGcmSenderId("954106721012")
                    .setStorageBucket("serviseyem.firebasestorage.app")
                    .build()
                FirebaseApp.initializeApp(context, options)
            } catch (_: Throwable) {
                try {
                    FirebaseApp.initializeApp(context)
                } catch (_: Throwable) {}
            }
        }
    }

    @Provides
    @Singleton
    fun provideFirebaseFirestore(@ApplicationContext context: Context): FirebaseFirestore {
        try {
            ensureFirebaseInitialized(context)
        } catch (_: Throwable) {}
        val firestore = FirebaseFirestore.getInstance()
        // Firestore persistence is disabled to rely purely on cloud sync.
        return firestore
    }

    @Provides
    @Singleton
    fun provideFirebaseAuth(@ApplicationContext context: Context): FirebaseAuth {
        try {
            ensureFirebaseInitialized(context)
        } catch (_: Throwable) {}
        return FirebaseAuth.getInstance()
    }

    @Provides
    @Singleton
    fun provideSecureStorage(@ApplicationContext context: Context): SecureStorage {
        return SecureStorage(context)
    }

    @Provides
    @Singleton
    fun provideSecurityManager(@ApplicationContext context: Context): SecurityManager {
        return SecurityManager(context)
    }
}
