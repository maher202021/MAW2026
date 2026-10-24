package com.serviseyem.di

import android.content.Context
import com.serviseyem.data.repositories.ApiKeyRepositoryImpl
import com.serviseyem.data.repositories.BookingRepository
import com.serviseyem.data.repositories.CategoryRepository
import com.serviseyem.data.repositories.ChatRepository
import com.serviseyem.data.repositories.DashboardExtensionsRepositoryImpl
import com.serviseyem.data.repositories.DashboardRepositoryImpl
import com.serviseyem.data.repositories.DataManagementRepositoryImpl
import com.serviseyem.data.repositories.FavoritesRepositoryImpl
import com.serviseyem.data.repositories.GalleryRepositoryImpl
import com.serviseyem.data.repositories.IApiKeyRepository
import com.serviseyem.data.repositories.IBookingRepository
import com.serviseyem.data.repositories.IChatRepository
import com.serviseyem.data.repositories.IDashboardExtensionsRepository
import com.serviseyem.data.repositories.IDashboardRepository
import com.serviseyem.data.repositories.IDataManagementRepository
import com.serviseyem.data.repositories.IFavoritesRepository
import com.serviseyem.data.repositories.IGalleryRepository
import com.serviseyem.data.repositories.IProductsRepository
import com.serviseyem.data.repositories.IRatingsRepository
import com.serviseyem.data.repositories.IRegistrationRepository
import com.serviseyem.data.repositories.IStatusRepository
import com.serviseyem.data.repositories.InstantRequestRepository
import com.serviseyem.data.repositories.MapRepository
import com.serviseyem.data.repositories.NotificationRepository
import com.serviseyem.data.repositories.ProductsRepositoryImpl
import com.serviseyem.data.repositories.RatingsRepositoryImpl
import com.serviseyem.data.repositories.RealtimeSyncRepository
import com.serviseyem.data.repositories.RegistrationRepositoryImpl
import com.serviseyem.data.repositories.StatusRepositoryImpl
import com.google.firebase.firestore.FirebaseFirestore
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object RepositoryModule {

    @Provides
    @Singleton
    fun provideChatRepository(
        @ApplicationContext context: Context,
        firestore: FirebaseFirestore
    ): ChatRepository {
        return ChatRepository(context, firestore)
    }

    @Provides
    @Singleton
    fun provideIChatRepository(chatRepository: ChatRepository): IChatRepository {
        return chatRepository
    }

    @Provides
    @Singleton
    fun provideBookingRepository(
        @ApplicationContext context: Context,
        firestore: FirebaseFirestore
    ): BookingRepository {
        return BookingRepository(context, firestore)
    }

    @Provides
    @Singleton
    fun provideIBookingRepository(bookingRepository: BookingRepository): IBookingRepository {
        return bookingRepository
    }

    @Provides
    @Singleton
    fun provideInstantRequestRepository(
        @ApplicationContext context: Context
    ): InstantRequestRepository {
        return InstantRequestRepository(context)
    }

    @Provides
    @Singleton
    fun provideRegistrationRepositoryImpl(
        @ApplicationContext context: Context
    ): RegistrationRepositoryImpl {
        return RegistrationRepositoryImpl(context)
    }

    @Provides
    @Singleton
    fun provideRegistrationRepository(
        registrationRepositoryImpl: RegistrationRepositoryImpl
    ): IRegistrationRepository {
        return registrationRepositoryImpl
    }

    @Provides
    @Singleton
    fun provideStatusRepositoryImpl(
        @ApplicationContext context: Context
    ): StatusRepositoryImpl {
        return StatusRepositoryImpl(context)
    }

    @Provides
    @Singleton
    fun provideStatusRepository(
        statusRepositoryImpl: StatusRepositoryImpl
    ): IStatusRepository {
        return statusRepositoryImpl
    }

    @Provides
    @Singleton
    fun provideDashboardRepository(
        @ApplicationContext context: Context
    ): IDashboardRepository {
        return DashboardRepositoryImpl(context)
    }

    @Provides
    @Singleton
    fun provideDashboardExtensionsRepository(): IDashboardExtensionsRepository {
        return DashboardExtensionsRepositoryImpl()
    }

    @Provides
    @Singleton
    fun provideProductsRepository(
        @ApplicationContext context: Context
    ): IProductsRepository {
        return ProductsRepositoryImpl(context)
    }

    @Provides
    @Singleton
    fun provideRatingsRepository(
        @ApplicationContext context: Context
    ): IRatingsRepository {
        return RatingsRepositoryImpl(context)
    }

    @Provides
    @Singleton
    fun provideFavoritesRepository(
        @ApplicationContext context: Context
    ): IFavoritesRepository {
        return FavoritesRepositoryImpl(context)
    }

    @Provides
    @Singleton
    fun provideGalleryRepository(
        @ApplicationContext context: Context
    ): IGalleryRepository {
        return GalleryRepositoryImpl(context)
    }

    @Provides
    @Singleton
    fun provideDataManagementRepository(
        firestore: FirebaseFirestore
    ): IDataManagementRepository {
        return DataManagementRepositoryImpl(firestore)
    }

    @Provides
    @Singleton
    fun provideApiKeyRepository(
        firestore: FirebaseFirestore
    ): IApiKeyRepository {
        return ApiKeyRepositoryImpl(firestore)
    }

    @Provides
    @Singleton
    fun provideCategoryRepository(
        firestore: FirebaseFirestore
    ): CategoryRepository {
        return CategoryRepository(firestore)
    }

    @Provides
    @Singleton
    fun provideNotificationRepository(): NotificationRepository {
        return NotificationRepository()
    }

    @Provides
    @Singleton
    fun provideMapRepository(
        @ApplicationContext context: Context
    ): MapRepository {
        return MapRepository(context)
    }

    @Provides
    @Singleton
    fun provideRealtimeSyncRepository(
        @ApplicationContext context: Context
    ): RealtimeSyncRepository {
        return RealtimeSyncRepository(context)
    }

    @Provides
    @Singleton
    fun provideProviderRepository(
        firestore: FirebaseFirestore,
        appState: com.serviseyem.ui.helpers.AppState
    ): com.serviseyem.data.repositories.ProviderRepository {
        return com.serviseyem.data.repositories.ProviderRepository(firestore, appState)
    }

    @Provides
    @Singleton
    fun provideStoreRepository(
        firestore: FirebaseFirestore,
        appState: com.serviseyem.ui.helpers.AppState
    ): com.serviseyem.data.repositories.StoreRepository {
        return com.serviseyem.data.repositories.StoreRepository(firestore, appState)
    }

    @Provides
    @Singleton
    fun providePropertyRepository(
        firestore: FirebaseFirestore,
        appState: com.serviseyem.ui.helpers.AppState
    ): com.serviseyem.data.repositories.PropertyRepository {
        return com.serviseyem.data.repositories.PropertyRepository(firestore, appState)
    }

    @Provides
    @Singleton
    fun provideWalletManager(
        @ApplicationContext context: Context
    ): com.serviseyem.utils.WalletManager {
        return com.serviseyem.utils.WalletManager(context)
    }
}
