package com.serviseyem.ui.helpers

import com.serviseyem.data.*
import com.serviseyem.data.models.*
import com.serviseyem.ui.viewmodels.BookingDistributionMode
import com.serviseyem.ui.viewmodels.BookingFormFields
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AppState @Inject constructor() {
    internal val _activityLogs = MutableStateFlow<List<ActivityLogEntity>>(emptyList())
    val activityLogs: StateFlow<List<ActivityLogEntity>> = _activityLogs.asStateFlow()

    internal val _banners = MutableStateFlow<List<BannerEntity>>(emptyList())
    val banners: StateFlow<List<BannerEntity>> = _banners.asStateFlow()

    internal val _bookingFormFields = MutableStateFlow<BookingFormFields>(BookingFormFields())
    val bookingFormFields: StateFlow<BookingFormFields> = _bookingFormFields.asStateFlow()

    internal val _bookings = MutableStateFlow<List<BookingEntity>>(emptyList())
    val bookings: StateFlow<List<BookingEntity>> = _bookings.asStateFlow()

    internal val _callsLog = MutableStateFlow<List<CallEntity>>(emptyList())
    val callsLog: StateFlow<List<CallEntity>> = _callsLog.asStateFlow()

    internal val _categories = MutableStateFlow<List<CategoryEntity>>(emptyList())
    val categories: StateFlow<List<CategoryEntity>> = _categories.asStateFlow()

    internal val _chatChannels = MutableStateFlow<List<ChatChannelEntity>>(emptyList())
    val chatChannels: StateFlow<List<ChatChannelEntity>> = _chatChannels.asStateFlow()

    internal val _cities = MutableStateFlow<List<CityEntity>>(emptyList())
    val cities: StateFlow<List<CityEntity>> = _cities.asStateFlow()

    internal val _colorPalettes = MutableStateFlow<List<ColorPaletteEntity>>(emptyList())
    val colorPalettes: StateFlow<List<ColorPaletteEntity>> = _colorPalettes.asStateFlow()

    internal val _coupons = MutableStateFlow<List<CouponEntity>>(emptyList())
    val coupons: StateFlow<List<CouponEntity>> = _coupons.asStateFlow()

    internal val _customProfileTabs = MutableStateFlow<List<CustomProfileTabEntity>>(emptyList())
    val customProfileTabs: StateFlow<List<CustomProfileTabEntity>> = _customProfileTabs.asStateFlow()

    internal val _deletedProviders = MutableStateFlow<List<ProviderEntity>>(emptyList())
    val deletedProviders: StateFlow<List<ProviderEntity>> = _deletedProviders.asStateFlow()

    internal val _distributionMode = MutableStateFlow(BookingDistributionMode.ADMIN_ONLY)
    val distributionMode: StateFlow<BookingDistributionMode> = _distributionMode.asStateFlow()

    internal val _instantRequests = MutableStateFlow<List<InstantRequestEntity>>(emptyList())
    val instantRequests: StateFlow<List<InstantRequestEntity>> = _instantRequests.asStateFlow()

    internal val _internalWallets = MutableStateFlow<List<InternalWalletEntity>>(emptyList())
    val internalWallets: StateFlow<List<InternalWalletEntity>> = _internalWallets.asStateFlow()

    internal val _isChatChannelsLoading = MutableStateFlow(false)
    val isChatChannelsLoading: StateFlow<Boolean> = _isChatChannelsLoading.asStateFlow()

    internal val _isInitialized = MutableStateFlow(true)
    val isInitialized: StateFlow<Boolean> = _isInitialized.asStateFlow()

    internal val _isProvidersLoading = MutableStateFlow(true)
    val isProvidersLoading: StateFlow<Boolean> = _isProvidersLoading.asStateFlow()

    internal val _jobApplications = MutableStateFlow<List<JobApplicationEntity>>(emptyList())
    val jobApplications: StateFlow<List<JobApplicationEntity>> = _jobApplications.asStateFlow()

    internal val _jobs = MutableStateFlow<List<JobEntity>>(emptyList())
    val jobs: StateFlow<List<JobEntity>> = _jobs.asStateFlow()

    internal val _jobListings = MutableStateFlow<List<JobEntity>>(emptyList())
    val jobListings: StateFlow<List<JobEntity>> = _jobListings.asStateFlow()

    internal val _maxKmRadius = MutableStateFlow(50)
    val maxKmRadius: StateFlow<Int> = _maxKmRadius.asStateFlow()

    internal val _notifications = MutableStateFlow<List<NotificationEntity>>(emptyList())
    val notifications: StateFlow<List<NotificationEntity>> = _notifications.asStateFlow()

    internal val _offers = MutableStateFlow<List<Offer>>(emptyList())
    val offers: StateFlow<List<Offer>> = _offers.asStateFlow()

    internal val _orders = MutableStateFlow<List<OrderEntity>>(emptyList())
    val orders: StateFlow<List<OrderEntity>> = _orders.asStateFlow()

    internal val _payments = MutableStateFlow<List<PaymentEntity>>(emptyList())
    val payments: StateFlow<List<PaymentEntity>> = _payments.asStateFlow()

    internal val _paymentWallets = MutableStateFlow<List<PaymentWalletEntity>>(emptyList())
    val paymentWallets: StateFlow<List<PaymentWalletEntity>> = _paymentWallets.asStateFlow()

    internal val _pendingProviders = MutableStateFlow<List<PendingProviderEntity>>(emptyList())
    val pendingProviders: StateFlow<List<PendingProviderEntity>> = _pendingProviders.asStateFlow()

    internal val _products = MutableStateFlow<List<ProductEntity>>(com.serviseyem.data.helpers.DefaultDemoData.getDemoProducts())
    val products: StateFlow<List<ProductEntity>> = _products.asStateFlow()

    internal val _properties = MutableStateFlow<List<PropertyEntity>>(com.serviseyem.data.helpers.DefaultDemoData.getDemoProperties())
    val properties: StateFlow<List<PropertyEntity>> = _properties.asStateFlow()

    internal val _providers = MutableStateFlow<List<ProviderEntity>>(com.serviseyem.data.helpers.DefaultDemoData.getDemoProviders())
    val providers: StateFlow<List<ProviderEntity>> = _providers.asStateFlow()

    internal val _ratings = MutableStateFlow<List<RatingEntity>>(emptyList())
    val ratings: StateFlow<List<RatingEntity>> = _ratings.asStateFlow()

    internal val _registeredUsersCount = MutableStateFlow(0)
    val registeredUsersCount: StateFlow<Int> = _registeredUsersCount.asStateFlow()

    internal val _registeredUsersList = MutableStateFlow<List<Map<String, Any>>>(emptyList())
    val registeredUsersList: StateFlow<List<Map<String, Any>>> = _registeredUsersList.asStateFlow()

    internal val _reports = MutableStateFlow<List<ReportEntity>>(emptyList())
    val reports: StateFlow<List<ReportEntity>> = _reports.asStateFlow()

    internal val _requestOffers = MutableStateFlow<List<RequestOfferEntity>>(emptyList())
    val requestOffers: StateFlow<List<RequestOfferEntity>> = _requestOffers.asStateFlow()

    internal val _settings = MutableStateFlow(AdminSettingsEntity())
    val settings: StateFlow<AdminSettingsEntity> = _settings.asStateFlow()

    internal val _stores = MutableStateFlow<List<StoreEntity>>(com.serviseyem.data.helpers.DefaultDemoData.getDemoStores())
    val stores: StateFlow<List<StoreEntity>> = _stores.asStateFlow()

    internal val _supervisors = MutableStateFlow<List<SupervisorEntity>>(emptyList())
    val supervisors: StateFlow<List<SupervisorEntity>> = _supervisors.asStateFlow()

    internal val _walletTransactions = MutableStateFlow<List<WalletTransactionEntity>>(emptyList())
    val walletTransactions: StateFlow<List<WalletTransactionEntity>> = _walletTransactions.asStateFlow()

    internal val _pendingTechnicians = MutableStateFlow<List<PendingProviderEntity>>(emptyList())
    val pendingTechnicians: StateFlow<List<PendingProviderEntity>> = _pendingTechnicians.asStateFlow()
}
