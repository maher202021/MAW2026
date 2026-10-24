
package com.serviseyem.ui
import androidx.compose.runtime.getValue
import com.serviseyem.ui.*
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import com.serviseyem.utils.*
import com.serviseyem.ui.viewmodels.BaseViewModel
import com.serviseyem.ui.viewmodels.BookingDistributionMode
import com.serviseyem.ui.viewmodels.BookingFormFields
import com.serviseyem.utils.BookingStatus
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.serviseyem.data.*
import com.serviseyem.data.models.*
import com.serviseyem.ui.viewmodels.SettingsViewModel.CardSettings
import com.serviseyem.ui.viewmodels.SettingsViewModel.ChatParticipantType
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.util.UUID

@HiltViewModel
class MainViewModel @Inject constructor(
    val authViewModel: com.serviseyem.ui.viewmodels.AuthViewModel,
    val homeViewModel: com.serviseyem.ui.viewmodels.HomeViewModel,
    val bookingViewModel: com.serviseyem.ui.viewmodels.BookingViewModel,
    val adminViewModel: com.serviseyem.ui.viewmodels.AdminViewModel,
    val settingsViewModel: com.serviseyem.ui.viewmodels.SettingsViewModel,
    val instantRequestViewModel: com.serviseyem.ui.viewmodels.InstantRequestViewModel,
    val chatRepo: com.serviseyem.data.repositories.ChatRepository,
    val appState: com.serviseyem.ui.helpers.AppState,
    val walletManager: com.serviseyem.utils.WalletManager,
    val providerRepository: com.serviseyem.data.repositories.ProviderRepository,
    val storeRepository: com.serviseyem.data.repositories.StoreRepository,
    val propertyRepository: com.serviseyem.data.repositories.PropertyRepository,
    val storeManagementViewModel: com.serviseyem.ui.viewmodels.StoreManagementViewModel = com.serviseyem.ui.viewmodels.StoreManagementViewModel(appState),
    val propertyManagementViewModel: com.serviseyem.ui.viewmodels.PropertyManagementViewModel = com.serviseyem.ui.viewmodels.PropertyManagementViewModel(appState),
    val jobManagementViewModel: com.serviseyem.ui.viewmodels.JobManagementViewModel = com.serviseyem.ui.viewmodels.JobManagementViewModel(appState),
    val paymentManagementViewModel: com.serviseyem.ui.viewmodels.PaymentManagementViewModel = com.serviseyem.ui.viewmodels.PaymentManagementViewModel(appState)
) : BaseViewModel() {
    private var isSeeded = false

    val preferenceHelper = com.serviseyem.ui.helpers.AppPreferenceHelper()
    val notificationRepository = com.serviseyem.data.repositories.NotificationRepository()
    val firestoreSeedHelper by lazy { com.serviseyem.ui.helpers.FirestoreSeedHelper(db) }
    val realtimeSyncHelper by lazy { com.serviseyem.ui.helpers.RealtimeSyncHelper(db) }
    val registrationHelper by lazy { com.serviseyem.ui.helpers.RegistrationHelper(db, auth, preferenceHelper) }
    val accountRecoveryHelper by lazy { com.serviseyem.ui.helpers.AccountRecoveryHelper(db, preferenceHelper) }

    override fun onCleared() {
        super.onCleared()
        try {
            realtimeSyncHelper?.clearListeners()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
    val auth = com.google.firebase.auth.FirebaseAuth.getInstance()
    internal val _currentLanguage = MutableStateFlow("ar")
    val currentLanguage: StateFlow<String> = _currentLanguage.asStateFlow()
    internal val _favoriteIds = MutableStateFlow<Set<String>>(emptySet())
    val favoriteIds: StateFlow<Set<String>> = _favoriteIds.asStateFlow()
    internal val _notifications get() = appState._notifications
    val notifications: StateFlow<List<NotificationEntity>> = _notifications.asStateFlow()
    internal val _readNotificationIds = MutableStateFlow<Set<String>>(emptySet())
    val readNotificationIds: StateFlow<Set<String>> = _readNotificationIds.asStateFlow()
    internal val _supervisors get() = appState._supervisors
    val supervisors: StateFlow<List<SupervisorEntity>> = _supervisors.asStateFlow()
    internal val _colorPalettes get() = appState._colorPalettes
    val colorPalettes: StateFlow<List<ColorPaletteEntity>> = _colorPalettes.asStateFlow()
    internal val _isOnline = MutableStateFlow(true)
    val isOnline: StateFlow<Boolean> = _isOnline.asStateFlow()
    internal val _currentUserPoints = MutableStateFlow(0)
    val currentUserPoints: StateFlow<Int> = _currentUserPoints.asStateFlow()
    internal val _targetRegistrationType = MutableStateFlow<String?>(null)
    val targetRegistrationType: StateFlow<String?> = _targetRegistrationType.asStateFlow()

    fun setTargetRegistrationType(type: String?) {
        _targetRegistrationType.value = type
    }
    internal val _screenBackStack = MutableStateFlow<List<String>>(listOf("USER_BROWSE"))
    val screenBackStack: StateFlow<List<String>> = _screenBackStack.asStateFlow()
    // INTENTIONAL: Delegating properties for unified access across ViewModels.
    // Do not convert to StateFlow - would break existing collectAsState() calls.
    val _currentUserId get() = authViewModel._currentUserId
    val currentUserId get() = authViewModel.currentUserId
    val _currentUserName get() = authViewModel._currentUserName
    val currentUserName get() = authViewModel.currentUserName
    val _currentUserPhone get() = authViewModel._currentUserPhone
    val currentUserPhone get() = authViewModel.currentUserPhone
    val _currentUserResidence get() = authViewModel._currentUserResidence
    val currentUserResidence get() = authViewModel.currentUserResidence
    val _adminRole get() = adminViewModel._adminRole
    val adminRole get() = adminViewModel.adminRole
    val supervisorPermissions get() = adminViewModel.supervisorPermissions
    val _passwordRecoveryWaitingPhone get() = authViewModel._passwordRecoveryWaitingPhone
    val passwordRecoveryWaitingPhone get() = authViewModel.passwordRecoveryWaitingPhone
    val _joinRequestPhone get() = authViewModel._joinRequestPhone
    val joinRequestPhone get() = authViewModel.joinRequestPhone
    val showBackdoorDialog get() = authViewModel.showBackdoorDialog
    val _categories get() = homeViewModel._categories
    val categories get() = homeViewModel.categories
    val _providers get() = homeViewModel._providers
    val providers get() = homeViewModel.providers
    val _filteredProviders get() = homeViewModel._filteredProviders
    val filteredProviders get() = homeViewModel.filteredProviders
    val _filteredStores get() = homeViewModel._filteredStores
    val filteredStores: StateFlow<List<StoreEntity>> get() = homeViewModel.filteredStores
    val _filteredProperties get() = homeViewModel._filteredProperties
    val filteredProperties: StateFlow<List<PropertyEntity>> get() = homeViewModel.filteredProperties
    val _banners get() = homeViewModel._banners
    val banners get() = homeViewModel.banners
    val activeBrowserTab get() = homeViewModel.activeBrowserTab
    val _selectedCategoryId get() = homeViewModel._selectedCategoryId
    val selectedCategoryId get() = homeViewModel.selectedCategoryId
    val _searchQuery get() = homeViewModel._searchQuery
    val searchQuery get() = homeViewModel.searchQuery
    val _filterVipOnly get() = homeViewModel._filterVipOnly
    val filterVipOnly get() = homeViewModel.filterVipOnly
    val _filterAvailableOnly get() = homeViewModel._filterAvailableOnly
    val filterAvailableOnly get() = homeViewModel.filterAvailableOnly
    val _filterCityId get() = homeViewModel._filterCityId
    val filterCityId get() = homeViewModel.filterCityId
    val _filterNeighborhoodName get() = homeViewModel._filterNeighborhoodName
    val filterNeighborhoodName get() = homeViewModel.filterNeighborhoodName
    val _phoneOrNameFilter get() = homeViewModel._phoneOrNameFilter
    val phoneOrNameFilter get() = homeViewModel.phoneOrNameFilter
    val _bookings get() = bookingViewModel._bookings
    val bookings get() = bookingViewModel.bookings
    val _bookingFormFields = bookingViewModel._bookingFormFields
    val bookingFormFields = bookingViewModel.bookingFormFields
    val _distributionMode = bookingViewModel._distributionMode
    val distributionMode = bookingViewModel.distributionMode
    val chatCoordinator = com.serviseyem.ui.helpers.ChatCoordinator()

    val _chatMessages get() = chatCoordinator._chatMessages
    val chatMessages get() = chatCoordinator.chatMessages
    val _chatChannels get() = chatCoordinator._chatChannels
    val chatChannels get() = chatCoordinator.chatChannels
    val _activeChatChannel get() = chatCoordinator._activeChatChannel
    val activeChatChannel get() = chatCoordinator.activeChatChannel
    val _pendingProviders get() = adminViewModel._pendingProviders
    val pendingProviders get() = adminViewModel.pendingProviders
    val _pendingTechnicians get() = adminViewModel._pendingTechnicians
    val pendingTechnicians get() = adminViewModel.pendingTechnicians
    val _registeredUsersList get() = adminViewModel._registeredUsersList
    val registeredUsersList get() = adminViewModel.registeredUsersList
    val _registeredUsersCount get() = adminViewModel._registeredUsersCount
    val registeredUsersCount get() = adminViewModel.registeredUsersCount
    val _passwordRecoveryRequests get() = adminViewModel.passwordRecoveryRequests
    val passwordRecoveryRequests get() = adminViewModel.passwordRecoveryRequests
    val _reports get() = adminViewModel._reports
    val reports get() = adminViewModel.reports
    val _activityLogs get() = adminViewModel._activityLogs
    val activityLogs get() = adminViewModel.activityLogs
    val _callsLog get() = adminViewModel._callsLog
    val callsLog get() = adminViewModel.callsLog
    val _coupons get() = adminViewModel._coupons
    val coupons get() = adminViewModel.coupons
    val _internalWallets get() = adminViewModel._internalWallets
    val internalWallets get() = adminViewModel.internalWallets
    val _walletTransactions get() = adminViewModel._walletTransactions
    val walletTransactions get() = adminViewModel.walletTransactions
    val _paymentWallets get() = adminViewModel._paymentWallets
    val paymentWallets get() = adminViewModel.paymentWallets
    val _payments get() = adminViewModel._payments
    val payments get() = adminViewModel.payments
    val _orders get() = adminViewModel._orders
    val orders get() = adminViewModel.orders
    val _ratings get() = adminViewModel._ratings
    val ratings get() = adminViewModel.ratings
    val _customProfileTabs get() = adminViewModel._customProfileTabs
    val customProfileTabs get() = adminViewModel.customProfileTabs
    val _stores get() = adminViewModel._stores
    val stores get() = adminViewModel.stores
    val _products get() = adminViewModel._products
    val products get() = adminViewModel.products
    val _properties get() = adminViewModel._properties
    val properties get() = adminViewModel.properties
    val _jobs get() = adminViewModel._jobs
    val jobs get() = adminViewModel.jobs
    val _jobApplications get() = adminViewModel._jobApplications
    val jobApplications get() = adminViewModel.jobApplications
    val _settings get() = settingsViewModel._settings
    val settings get() = settingsViewModel.settings
    val _cardSettings get() = settingsViewModel._cardSettings
    val cardSettings get() = settingsViewModel.cardSettings
    val _colorScheme get() = settingsViewModel._colorScheme
    val colorScheme get() = settingsViewModel.colorScheme
    val _personalColors get() = settingsViewModel._personalColors
    val personalColors get() = settingsViewModel.personalColors
    val _colorSyncStatus get() = settingsViewModel._colorSyncStatus
    val colorSyncStatus get() = settingsViewModel.colorSyncStatus
    val _colorSyncLogs get() = settingsViewModel._colorSyncLogs
    val colorSyncLogs get() = settingsViewModel.colorSyncLogs
    val _pendingConflictScheme get() = settingsViewModel._pendingConflictScheme
    val pendingConflictScheme get() = settingsViewModel.pendingConflictScheme
    val _blockedChatParticipants get() = settingsViewModel._blockedChatParticipants
    val blockedChatParticipants get() = settingsViewModel.blockedChatParticipants
    val _activeVoiceCall get() = settingsViewModel._activeVoiceCall
    val activeVoiceCall get() = settingsViewModel.activeVoiceCall
    val _instantRequests get() = instantRequestViewModel._instantRequests
    val instantRequests get() = instantRequestViewModel.instantRequests
    val _requestOffers get() = instantRequestViewModel._requestOffers
    val requestOffers get() = instantRequestViewModel.requestOffers
    val _offers get() = instantRequestViewModel._offers
    val offers get() = instantRequestViewModel.offers
    internal val _currentScreen = MutableStateFlow("USER_BROWSE")
    val currentScreen: StateFlow<String> = _currentScreen.asStateFlow()
    internal val _navigationStack = mutableListOf<String>()
    
    // NOTE: These are intentionally plain variables, not StateFlow.
    // They are read synchronously in AppNavigator during navigation.
    // Do NOT convert to StateFlow without refactoring all call sites.
    var selectedProvider: com.serviseyem.data.ProviderEntity? = null
    var selectedStore: com.serviseyem.data.StoreEntity? = null
    var selectedProperty: com.serviseyem.data.PropertyEntity? = null
    var selectedJob: com.serviseyem.data.JobEntity? = null
    var selectedOfferId by androidx.compose.runtime.mutableStateOf("")
    var selectedRequestId by androidx.compose.runtime.mutableStateOf("")
    var showQuickServiceDialog by androidx.compose.runtime.mutableStateOf(false)
    internal val _userLatitude = MutableStateFlow(15.3694)
    val userLatitude: StateFlow<Double> = _userLatitude.asStateFlow()
    internal val _userLongitude = MutableStateFlow(44.1910)
    val userLongitude: StateFlow<Double> = _userLongitude.asStateFlow()
    internal val _isGpsTrackingActive = MutableStateFlow(false)
    val isGpsTrackingActive: StateFlow<Boolean> = _isGpsTrackingActive.asStateFlow()
    internal val _isProvidersLoading get() = appState._isProvidersLoading
    val isProvidersLoading: StateFlow<Boolean> = _isProvidersLoading.asStateFlow()
    internal val _isChatChannelsLoading get() = appState._isChatChannelsLoading
    val isChatChannelsLoading: StateFlow<Boolean> = _isChatChannelsLoading.asStateFlow()
    internal val _cities get() = appState._cities
    val cities: StateFlow<List<CityEntity>> = _cities.asStateFlow()
    internal val _deletedProviders get() = appState._deletedProviders
    val deletedProviders: StateFlow<List<ProviderEntity>> = _deletedProviders.asStateFlow()
    private val _showNotificationsDialog = MutableStateFlow(false)
    val showNotificationsDialog: StateFlow<Boolean> = _showNotificationsDialog.asStateFlow()

    private val _showRestoreAccountDialog = MutableStateFlow(false)
    val showRestoreAccountDialog: StateFlow<Boolean> = _showRestoreAccountDialog.asStateFlow()

    fun toggleNotificationsDialog(show: Boolean) {
        _showNotificationsDialog.value = show
    }

    fun toggleRestoreAccountDialog(show: Boolean) {
        _showRestoreAccountDialog.value = show
    }

    private var isSettingsLoaded = false
    private var isCategoriesLoaded = false
    private var isCitiesLoaded = false
    private var isProvidersLoaded = false

    private fun checkInitializationComplete() {
        _isInitialized.value = true
    }

    internal val _isInitialized get() = appState._isInitialized
    val isInitialized: StateFlow<Boolean> = _isInitialized.asStateFlow()
    internal val _maxKmRadius get() = appState._maxKmRadius
    val maxKmRadius: StateFlow<Int> = _maxKmRadius.asStateFlow()
    var lastNotifMsg: String = ""
    var lastNotifTime: Long = 0L
    var targetChatChannelId by mutableStateOf<String?>(null)
    var targetChatUserId by mutableStateOf<String?>(null)
    var targetChatUserName by mutableStateOf<String?>(null)
    var targetChatUserPhoto by mutableStateOf<String?>(null)
    var targetChatRelatedEntityId by mutableStateOf<String?>(null)
    var targetChatRelatedEntityType by mutableStateOf<String?>(null)
    val triggerRestoreAccountDialog = MutableStateFlow<Boolean>(false)

    fun openDirectChat(
        targetUserId: String,
        targetUserName: String,
        targetUserPhoto: String = "",
        relatedEntityId: String? = null,
        relatedEntityType: String? = null
    ) {
        targetChatChannelId = null
        targetChatUserId = targetUserId
        targetChatUserName = targetUserName
        targetChatUserPhoto = targetUserPhoto
        targetChatRelatedEntityId = relatedEntityId
        targetChatRelatedEntityType = relatedEntityType
        navigateToScreen(AppScreens.CHAT_DIRECT)
    }
    val notificationViewModel = com.serviseyem.ui.screens.notifications.NotificationViewModel(this)

    init {

        viewModelScope.launch {
            authViewModel.adminRole.collect { role ->
                if (adminViewModel._adminRole.value != role) {
                    adminViewModel._adminRole.value = role
                }
            }
        }
        viewModelScope.launch {
            adminViewModel.adminRole.collect { role ->
                if (authViewModel._adminRole.value != role) {
                    authViewModel._adminRole.value = role
                }
            }
        }
        viewModelScope.launch {
            authViewModel.currentSupervisorPermissions.collect { perms ->
                if (adminViewModel.supervisorPermissions.value != perms) {
                    adminViewModel._supervisorPermissions.value = perms
                }
            }
        }
        viewModelScope.launch {
            appState._supervisors.collect { sups ->
                if (authViewModel._supervisors.value != sups) {
                    authViewModel._supervisors.value = sups
                }
            }
        }
    }
    private fun checkAndTriggerFavoriteOffersNotifications() {
    }
    fun updateUserLocation(lat: Double, lng: Double) {
        _userLatitude.value = lat
        _userLongitude.value = lng
    }
    fun startLocationUpdates() {
        _isGpsTrackingActive.value = true
        appContext?.let { ctx ->
            try {
                if (androidx.core.content.ContextCompat.checkSelfPermission(ctx, android.Manifest.permission.ACCESS_FINE_LOCATION) == android.content.pm.PackageManager.PERMISSION_GRANTED ||
                    androidx.core.content.ContextCompat.checkSelfPermission(ctx, android.Manifest.permission.ACCESS_COARSE_LOCATION) == android.content.pm.PackageManager.PERMISSION_GRANTED) {
                    val fusedClient = com.google.android.gms.location.LocationServices.getFusedLocationProviderClient(ctx)
                    fusedClient.lastLocation.addOnSuccessListener { loc ->
                        loc?.let { updateUserLocation(it.latitude, it.longitude) }
                    }
                }
                val lm = ctx.getSystemService(android.content.Context.LOCATION_SERVICE) as? android.location.LocationManager
                val loc = lm?.getLastKnownLocation(android.location.LocationManager.GPS_PROVIDER)
                    ?: lm?.getLastKnownLocation(android.location.LocationManager.NETWORK_PROVIDER)
                loc?.let {
                    updateUserLocation(it.latitude, it.longitude)
                }
            } catch (e: Exception) {
                android.util.Log.e("MainViewModel", "Error: ", e)
            }
        }
    }
    fun refreshData(showNotification: Boolean = false) {
        viewModelScope.launch {
            _isRefreshing.value = true
            _uiErrorMessage.value = null
            try {
                setupRealtimeFirestoreListeners()
            } catch (e: Exception) {
                _uiErrorMessage.value = "تعذر تحديث البيانات: ${e.localizedMessage}"
            } finally {
                kotlinx.coroutines.delay(600)
                _isRefreshing.value = false
            }
        }
    }
    fun updateOnlineStatus(online: Boolean) {
        _isOnline.value = online
    }
    fun retryConnection(context: android.content.Context) {
        val cm = context.getSystemService(android.content.Context.CONNECTIVITY_SERVICE) as? android.net.ConnectivityManager
        if (cm != null) {
            val activeNetwork = cm.activeNetwork
            val capabilities = cm.getNetworkCapabilities(activeNetwork)
            val online = capabilities?.hasCapability(android.net.NetworkCapabilities.NET_CAPABILITY_INTERNET) == true
            _isOnline.value = online
            if (online) {
                setupRealtimeFirestoreListeners()
            }
        }
    }
    fun initializeUserIdentity(context: android.content.Context) {
        appContext = context.applicationContext
        authViewModel.appContext = appContext
        homeViewModel.appContext = appContext
        bookingViewModel.appContext = appContext
        adminViewModel.appContext = appContext
        settingsViewModel.appContext = appContext
        instantRequestViewModel.appContext = appContext
        bookingViewModel.getCoupons = { _coupons.value }
        bookingViewModel.getProviders = { _providers.value }
        bookingViewModel.getCurrentUserPhone = { _currentUserPhone.value }
        bookingViewModel.getCurrentUserName = { _currentUserName.value }
        bookingViewModel.getCurrentUserResidence = { _currentUserResidence.value }
        bookingViewModel.setCurrentUserPhone = { _currentUserPhone.value = it }
        bookingViewModel.setCurrentUserName = { _currentUserName.value = it }
        bookingViewModel.setCurrentUserResidence = { _currentUserResidence.value = it }
        bookingViewModel.onAddNotification = { title, message, targetType, targetValue ->
            addNotification(title = title, message = message, targetType = targetType, targetValue = targetValue)
        }
        bookingViewModel.triggerNotificationCallback = { msg ->
            triggerNotification(msg)
        }
        bookingViewModel.onOpenOrCreateChatChannel = { targetId, targetType, targetName, targetPhone, targetCategory, relatedEntityId, relatedEntityType, onComplete ->
            openOrCreateChatChannel(
                targetId = targetId,
                targetType = targetType,
                targetName = targetName,
                targetPhone = targetPhone,
                targetCategory = targetCategory,
                relatedEntityId = relatedEntityId,
                relatedEntityType = relatedEntityType,
                onCreated = onComplete
            )
        }
        adminViewModel.getHomeViewModel = { homeViewModel }
        adminViewModel.getSettingsViewModel = { settingsViewModel }
        adminViewModel.getBookingViewModel = { bookingViewModel }
        adminViewModel.getInstantRequestViewModel = { instantRequestViewModel }
        adminViewModel.getNotifications = { _notifications }
        adminViewModel.onAddNotification = { title, message, targetType, targetValue ->
            addNotification(title = title, message = message, targetType = targetType, targetValue = targetValue)
        }
        adminViewModel.onTriggerNotificationFull = { title, message, targetType, targetValue ->
            triggerNotification(title = title, message = message, targetType = targetType, targetValue = targetValue)
        }
        adminViewModel.onTriggerNotification = { msg ->
            triggerNotification(msg)
        }
        adminViewModel.onApplyFilters = {
            homeViewModel.applyFilters(_currentUserResidence.value)
        }
        storeManagementViewModel.onTriggerNotification = { msg -> triggerNotification(msg) }
        propertyManagementViewModel.onTriggerNotification = { msg -> triggerNotification(msg) }
        jobManagementViewModel.onTriggerNotification = { msg -> triggerNotification(msg) }
        paymentManagementViewModel.onTriggerNotification = { msg -> triggerNotification(msg) }
        settingsViewModel.getAuthViewModel = { authViewModel }
        settingsViewModel.getHomeViewModel = { homeViewModel }
        settingsViewModel.getBookingViewModel = { bookingViewModel }
        settingsViewModel.getAdminViewModel = { adminViewModel }
        settingsViewModel.getProviders = { _providers }
        settingsViewModel.getBookings = { _bookings }
        settingsViewModel.getCategories = { _categories }
        settingsViewModel.getStores = { _stores }
        settingsViewModel.getProperties = { _properties }
        settingsViewModel.getPasswordRecoveryWaitingPhone = { _passwordRecoveryWaitingPhone }
        settingsViewModel.setPasswordRecoveryWaitingPhone = { _passwordRecoveryWaitingPhone.value = it }
        settingsViewModel.verifyAdminOrOwnerPassword = { password ->
            verifyAdminOrOwnerPassword(password)
        }
        settingsViewModel.triggerNotification = { msg ->
            triggerNotification(msg)
        }
        instantRequestViewModel.triggerNotification = { msg ->
            triggerNotification(msg)
        }
        instantRequestViewModel.addNotification = { title, message, targetType, targetValue ->
            addNotification(title = title, message = message, targetType = targetType, targetValue = targetValue)
        }
        instantRequestViewModel.getOrCreateChatChannel = { providerId, providerName, customerPhone, customerName ->
            openOrCreateChatChannel(
                targetId = providerId,
                targetType = "URGENT_REQUEST",
                targetName = providerName,
                targetPhone = customerPhone,
                targetCategory = "",
                relatedEntityId = "",
                relatedEntityType = "URGENT_REQUEST",
                onCreated = { }
            )
        }
        try {
            authViewModel.initializeUserIdentity(context) { savedFavs ->
                _favoriteIds.value = savedFavs
                checkAndTriggerFavoriteOffersNotifications()
            }
        } catch (e: Exception) {
            android.util.Log.e("MainViewModel", "❌ Error in AuthViewModel initialization", e)
        }
        try {
            setupRealtimeFirestoreListeners()
        } catch (e: Exception) {
            android.util.Log.e("MainViewModel", "❌ Error in setting up realtime listeners", e)
        }
        try {
            settingsViewModel.loadCardSettings()
        } catch (e: Exception) {
            android.util.Log.e("MainViewModel", "❌ Error in SettingsViewModel loadCardSettings", e)
        }
        try {
            adminViewModel.loadPendingTechnicians()
        } catch (e: Exception) {
            android.util.Log.e("MainViewModel", "❌ Error in AdminViewModel loadPendingTechnicians", e)
        }
        try {
            seedFirestoreIfEmpty()
        } catch (e: Exception) {
            android.util.Log.e("MainViewModel", "❌ Error in seedFirestoreIfEmpty", e)
        }
        // راقب اكتمال تحميل البيانات الأساسية
        viewModelScope.launch {
            settings.collect {
                isSettingsLoaded = true
                checkInitializationComplete()
            }
        }
        viewModelScope.launch {
            categories.collect { cats ->
                if (cats.isNotEmpty()) {
                    isCategoriesLoaded = true
                    checkInitializationComplete()
                }
            }
        }
        viewModelScope.launch {
            cities.collect { cityList ->
                if (cityList.isNotEmpty()) {
                    isCitiesLoaded = true
                    checkInitializationComplete()
                }
            }
        }
        viewModelScope.launch {
            providers.collect { provList ->
                if (provList.isNotEmpty()) {
                    isProvidersLoaded = true
                    checkInitializationComplete()
                }
            }
        }
        // مهلة أمان قصوى (fallback) للشاشة الترحيبية في حالة ضعف الاتصال
        viewModelScope.launch {
            kotlinx.coroutines.delay(2800)
            if (!_isInitialized.value) {
                _isInitialized.value = true
            }
            
            // Self-repair: If after 5 seconds no data is synced, force a seed check
            kotlinx.coroutines.delay(4000)
            if (providers.value.isEmpty() && stores.value.isEmpty()) {
                android.util.Log.w("MainViewModel", "⚠️ No data detected after sync. Forcing seed repair...")
                seedFirestoreIfEmpty(force = true)
            }
        }
    }
    fun setupRealtimeFirestoreListeners() {
        realtimeSyncHelper?.clearListeners()
        realtimeSyncHelper?.setupRealtimeFirestoreListeners(appState)
        
        // Use new robust repository-based sync
        providerRepository.startSyncingProviders()
        storeRepository.startSyncingStores()
        propertyRepository.startSyncingProperties()

        viewModelScope.launch {
            realtimeSyncHelper?.loadOnDemandInitialData(appState)
        }
    }
    fun seedFirestoreIfEmpty(force: Boolean = false) {
        if (force) isSeeded = false
        firestoreSeedHelper?.seedFirestoreIfEmpty()
    }
    override fun getDefaultStoresList(): List<com.serviseyem.data.StoreEntity> {
        return com.serviseyem.data.helpers.DefaultDemoData.getDemoStores()
    }
    override fun getDefaultPropertiesList(): List<com.serviseyem.data.PropertyEntity> {
        return com.serviseyem.data.helpers.DefaultDemoData.getDemoProperties()
    }
    override fun getDefaultProvidersList(): List<com.serviseyem.data.ProviderEntity> {
        return com.serviseyem.data.helpers.DefaultDemoData.getDemoProviders()
    }
    override suspend fun uploadImageStringOrUri(
        context: android.content.Context,
        input: String,
        storagePath: String,
        maxSizeBytes: Long
    ): String {
        return registrationHelper?.uploadImageStringOrUri(context, input, storagePath, maxSizeBytes) ?: input
    }
    override fun getAuthEmailForPhone(phone: String): String = authViewModel.getAuthEmailForPhone(phone)

    data class RestoreAccountMatch(
        val type: String,
        val name: String,
        val provider: com.serviseyem.data.ProviderEntity? = null,
        val store: com.serviseyem.data.StoreEntity? = null,
        val property: com.serviseyem.data.PropertyEntity? = null
    )

    /**
     * 🌐 مراقبة حالة طلب استعادة كلمة المرور بشكل حي
     * هذه الدالة تمنع الواجهة من الوصول المباشر إلى Firestore.
     */
    fun observePasswordRecoveryStatus(
        phone: String,
        onUpdate: (status: String, newPassword: String, accountName: String, accountType: String) -> Unit
    ): com.google.firebase.firestore.ListenerRegistration? {
        return authViewModel.observePasswordRecoveryStatus(phone, onUpdate)
    }

    /**
     * 🔑 إرسال طلب استعادة كلمة مرور
     * تفويض إلى AuthViewModel لمنع كتابة Firestore من MainViewModel كمنطق UI
     */
    fun requestPasswordRecovery(
        phone: String,
        name: String,
        accountType: String
    ) {
        authViewModel.requestPasswordRecovery(phone, name, accountType)
    }

    /**
     * 🔍 البحث الشامل في كافة الفئات (Server-Side)
     * لحل مشكلة عنق الزجاجة عند كبر حجم البيانات
     */
    suspend fun searchAll(query: String): Map<String, List<Any>> {
        val providers = realtimeSyncHelper?.searchProviders(query) ?: emptyList()
        val stores = realtimeSyncHelper?.searchStores(query) ?: emptyList()
        val properties = realtimeSyncHelper?.searchProperties(query) ?: emptyList()
        val jobs = realtimeSyncHelper?.searchJobs(query) ?: emptyList()
        
        return mapOf(
            "providers" to providers,
            "stores" to stores,
            "properties" to properties,
            "jobs" to jobs
        )
    }

}
