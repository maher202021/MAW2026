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
                FirebaseApp.initializeApp(context)
            } catch (_: Throwable) {}
        }
    }

    @Provides
    @Singleton
    fun provideFirebaseFirestore(@ApplicationContext context: Context): FirebaseFirestore {
        try {
            ensureFirebaseInitialized(context)
        } catch (_: Throwable) {}
        return FirebaseFirestore.getInstance()
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
