package com.serviseyem.data

import androidx.annotation.Keep

@Keep
@com.google.firebase.firestore.IgnoreExtraProperties
data class AdminSettingsEntity(
    val id: String = "main_settings",
    val appName: String = "دليل خدمات اليمن",
    val welcomeMessage: String = AdminSettingsDefaults.WELCOME_MESSAGE,
    val footerMessage: String = "777644",
    val footerBgColorHex: String = "#115E59",
    val footerItemsOrder: String = "INFO,BOOKINGS,TEXT,LANG,ADMIN",
    val activeThemeId: String = "EMERALD_YEMEN",
    val customPrimaryHex: String = "#059669",
    val customSecondaryHex: String = "#115E59",
    val customBackgroundHex: String = "#0A0F0D",
    val customSurfaceHex: String = "#121D18",
    val isMaintenanceActive: Boolean = false,
    val hidePromoFooter: Boolean = false,
    val assistantHidden: Boolean = false,
    val assistantSize: Int = 56,
    val chatHidden: Boolean = false,
    val chatSize: Int = 56,
    val maxSearchRadiusKm: Int = 20,
    val appVersion: String = "v2.2026",
    val isSpeechSearchEnabled: Boolean = true,
    val supportPhone: String = "777644",
    val supportWhatsapp: String = "777644",
    val supportEmail: String = "",
    val adminUsername: String = "",
    @Deprecated("Use SecureAdminStorage / ownerPasswordHash instead")
    @get:com.google.firebase.firestore.Exclude
    val ownerPassword: String = "",
    // Chat service states and controls
    val disableChatAll: Boolean = false,
    val disableChatProviders: Boolean = false,
    val disableChatUsers: Boolean = false,
    val chatDisabledAnnouncement: String = AdminSettingsDefaults.CHAT_DISABLED_ANNOUNCEMENT,
    val allowChatUserToProvider: Boolean = true,
    val allowChatProviderToAdmin: Boolean = true,
    val allowChatUserToAdmin: Boolean = true,
    val chatFontSizeSp: Int = 14,
    val chatBackgroundHex: String = "#1E293B",
    val allowVoiceInput: Boolean = true,
    val allowTextToSpeech: Boolean = true,
    val allowVoiceInputJoinForm: Boolean = true,
    val allowTextToSpeechJoinForm: Boolean = true,
    val allowVoiceInputAssistant: Boolean = true,
    val allowTextToSpeechAssistant: Boolean = true,
    val allowVoiceInputBookingForm: Boolean = true,
    val allowTextToSpeechBookingForm: Boolean = true,
    val approveChatsBeforeProvider: Boolean = false,
    val bookingRouting: String = "BOTH",
    // Layout and registration customization
    val activeFontFamily: String = "CAIRO",
    // Map feature switches
    val isMapFeatureEnabled: Boolean = true,
    val mapProvider: String = "LEAFLET_OSM",
    val defaultMapBaseLayer: String = "esri_world_imagery",
    val showMapToVisitors: Boolean = true,
    val showProvidersOnMap: Boolean = true,
    val showStoresOnMap: Boolean = true,
    val showRestaurantsOnMap: Boolean = true,
    val showMedicalOnMap: Boolean = true,
    val showPropertiesOnMap: Boolean = true,
    val mapLayersOrder: String = com.serviseyem.data.models.MapLayersCatalog.DEFAULT_ORDER_CSV,
    val mapDefaultZoom: Float = 14f,
    // Assistant controls
    val isAssistantEnabled: Boolean = true,
    val isAssistantIconVisible: Boolean = true,
    val cardBackgroundHex: String = "#1E293B",
    val providerNameColorHex: String = "#FFFFFF",
    val ratingColorHex: String = "#F59E0B",
    val locationColorHex: String = "#94A3B8",
    val vipBadgeColorHex: String = "#D97706",
    val verifiedBadgeColorHex: String = "#3B82F6",
    val recommendedBadgeColorHex: String = "#10B981",
    // Buttons switches and colors
    val showCallButton: Boolean = true,
    val showVoiceCallButton: Boolean = false,
    val showWhatsappButton: Boolean = true,
    val showBookButton: Boolean = true,
    // Padding & Spacing
    val elementSpacing: Int = 8,
    val cardPadding: Int = 12,
    // Admin customizable booking labels and terms
    val bookingTerms: String = AdminSettingsDefaults.BOOKING_TERMS,
    val bookingLabelName: String = "الاسم الثنائي/الثلاثي للعميل كامل",
    val bookingLabelPhone: String = "رقم الهاتف اليمني (أرقام فقط)",
    val bookingLabelArea: String = "منطقة السكن والحي بالتفصيل (مثل: صنعاء-شارع الستين)",
    val bookingLabelService: String = "نوع الخدمة المطلوبة بالتفصيل",
    val aboutCustomInfo: String = AdminSettingsDefaults.ABOUT_CUSTOM_INFO,
    // Main screen Banner configuration
    val bannerEnabled: Boolean = false,
    val bannerType: String = "TEXT",
    val bannerContent: String = "",
    val bannerBase64: String = "",
    val bannerLocation: String = "TOP",
    val bannerDurationSeconds: Int = 10,
    val bannerDisplayStyle: String = "SLIDE",
    val appDownloadUrl: String = "https://example.com/download_app",
    val telegramUrl: String = "https://t.me/yamandelil",
    val aboutLayoutOrder: String = "COVER,LOGO,TITLE,ANNOUNCEMENT,ABOUT_CARD,DOWNLOAD_BTN,CONTACTS,SOCIALS",
    val hideTwitter: Boolean = false,
    val hideInstagram: Boolean = false,
    val hideYoutube: Boolean = false,
    val hideFacebook: Boolean = false,
    val hideTelegram: Boolean = false,
    val hideWebsite: Boolean = false,
    // Integrated payment configurations
    val isPaymentEnabled: Boolean = false,
    val isBookingPaymentRequired: Boolean = false,
    val requireAdvancePayment: Boolean = false,
    val advancePaymentPercent: Float = 0.30f,
    val paymentCommissionRate: Float = 0.10f,
    val isBookingsIconVisible: Boolean = true,
    val bookingsAccessControl: String = "ALL",
    val blockedUsersForBookings: String = "",
    // 3D Golden Icons, Font Scaling, & Icon Customization Settings
    val topNavIconStyle: String = "GOLDEN_3D",
    val navIconSizeDp: Int = 18,
    val topHomeIcon: String = "🏠",
    val topMapsIcon: String = "🗺️",
    val topJoinIcon: String = "👤",
    val topNotifIcon: String = "🔔",
    val topChatsIcon: String = "✉️",
    val bottomInfoIcon: String = "ℹ️",
    val bottomBookingsIcon: String = "📅",
    val bottomLangIcon: String = "🌐",
    val bottomAdminIcon: String = "🔒",
    // Advanced Instant Chat System Controls & Routing
    val chatDisplayIdentityMode: String = "NAME_AND_PHONE",
    val isChatTextEnabled: Boolean = true,
    val isChatAudioEnabled: Boolean = true,
    val isChatImageEnabled: Boolean = true,
    val isChatVideoEnabled: Boolean = true,
    val isChatCallEnabled: Boolean = true,
    val chatRoutingMode: String = "DEFAULT",
    val chatBlockedIds: String = "",
    // Registration Forms Controls
    val enableProvidersRegistration: Boolean = true,
    val enableStoresRegistration: Boolean = false,
    // Admin Customizable Booking Settings
    val isBookingEnabled: Boolean = true,
    val routingMode: String = "auto_nearest",
    // Advanced Payment, Instant Booking & Deposit Rule Controls
    val linkedCategoriesForInstantBooking: String = "",
    val linkedProvidersForDeposit: String = "",
    // Voice Call In-App System Controls
    val voiceCallsEnabled: Boolean = true,
    val disableVoiceCalls: Boolean = true,
    val voiceCallsDisabledAnnouncement: String = "",
    val appLanguage: String = "ar",
    val minRequiredVersion: String = "2.5.0",
    val forceUpdateEnabled: Boolean = false,
    val autoDetectLanguage: Boolean = true,
    val showInfoIcon: Boolean = true,
    val showBookingsIcon: Boolean = true,
    val showLangIcon: Boolean = true,
    val showAdminIcon: Boolean = true,
    val showFooterText: Boolean = true,
    val infoIconType: String = "INFO",
    val adminIconType: String = "LOCK",
    val langIconType: String = "GLOBE",
    val showLoyaltyBanner: Boolean = false,
    val registrationRequirements: String = AdminSettingsDefaults.REGISTRATION_REQUIREMENTS,
    val maxWorkPhotos: Int = 5,
    val bypassVisitorRegistration: Boolean = true,
    val disableChatFirewall: Boolean = false,
    val disableBookingFirewall: Boolean = true,
    val chatXOffset: Int = 20,
    val chatYOffset: Int = 80,
    val assistantXOffset: Int = 20,
    val assistantYOffset: Int = 140,
    val disabledMapLayers: String = "",
    val mapMaxZoom: Int = 22,
    val showSatMenuOnMap: Boolean = true,
    val showMeasureToolOnMap: Boolean = true,
    val mapMaxDistanceKm: Int = 20,
    // Card dimensions & styles
    val coverHeight: Int = 0,
    val avatarSize: Int = 50,
    val showVipBadge: Boolean = true,
    val showVerifiedBadge: Boolean = true,
    val showRecommendedBadge: Boolean = true,
    val showDetailsButton: Boolean = true,
    val callButtonColorHex: String = "#10B981",
    val whatsappButtonColorHex: String = "#25D366",
    val detailsButtonColorHex: String = "#3B82F6",
    val bookButtonColorHex: String = "#F59E0B",
    // About App customization fields
    val aboutCoverType: String = "IMAGE",
    val aboutCoverContent: String = AdminSettingsDefaults.ABOUT_COVER_URL,
    val aboutCoverBase64: String = "",
    val showRefreshIcon: Boolean = false,
    val showSettingsIcon: Boolean = false,
    val headerIconsOrder: String = "MENU,NOTIF,CHAT",
    val categoriesLayoutType: String = "GRID_VERTICAL",
    val encryptionType: String = "تشفير آمن سحابي",
    val splashWelcomeMessage: String = AdminSettingsDefaults.SPLASH_WELCOME_MESSAGE,
    val websiteUrl: String = "https://www.yamandelil.com",
    val facebookUrl: String = "https://facebook.com/yamandelil",
    val twitterUrl: String = "https://twitter.com/yamandelil",
    val instagramUrl: String = "https://instagram.com/yamandelil",
    val youtubeUrl: String = "https://youtube.com/yamandelil",
    val minAdvanceAmount: Double = 500.0,
    val maxAdvanceAmount: Double = 50000.0,
    val isCommissionEnabled: Boolean = true,
    // Dynamic controls for Stores & Real Estate sections
    val isStoresEnabled: Boolean = true,
    val isPropertiesEnabled: Boolean = true,
    val isUserPasswordRequired: Boolean = false,
    val bookingsIconType: String = "DateRange",
    val globalFontScale: Float = 1.0f,
    val chatDisabledCategories: String = "",
    val enableRestaurantsRegistration: Boolean = false,
    val enablePropertiesRegistration: Boolean = false,
    val enableMedicalRegistration: Boolean = false,
    val enableJobsRegistration: Boolean = false,
    val exemptUsersFromDeposit: String = "",
    val showWalletInProfile: Boolean = true,
    val voiceCallsAllowedCategories: String = "",
    val voiceCallsAllowedProviders: String = "",
    val voiceCallsAllowedUsers: String = "",
    val hideTopHeaderBar: Boolean = false,
    val customAppName: String = "",
    val forceRtlDirection: Boolean = true,
    val defaultCurrency: String = "ريال يمني (YER)",
    val defaultThemeMode: String = "الوضع الداكن (Dark)"
) {
    var ownerEmail: String = ""
    // Notifications control
    var isNotificationsEnabled: Boolean = true
    var disableChatSupervisors: Boolean = false
    var showWorkPhotos: Boolean = true
    // Floating position percentages and offsets
    var assistantPositionX: Float = 0.85f
    var assistantPositionY: Float = 0.70f
    var chatPositionX: Float = 0.85f
    var chatPositionY: Float = 0.82f
    var mapPrecisionDigits: Int = 1
    var avatarShape: String = "CIRCLE"
    var previewPriceColorHex: String = "#10B981"
    // Badges switches
    var showUserIdInsteadOfNameInChat: Boolean = false
    var requirePaymentProofImage: Boolean = true
    var enableBookingPaymentStep: Boolean = true
    var showInstantChatButton: Boolean = true
    var showReviewButton: Boolean = true
    var showReportButton: Boolean = true
    var buttonsOrder: String = "CALL,WHATSAPP,DETAILS,BOOK"
    // Information rows switches and order
    var showDistance: Boolean = true
    var showPreviewPrice: Boolean = true
    var showAvailability: Boolean = true
    var showReviewsCount: Boolean = true
    var infoRowsOrder: String = "NAME,RATING,DISTANCE,LOCATION,PRICE,AVAILABILITY"
    var cardMarginHorizontal: Int = 0
    var cardMarginVertical: Int = 4
    // Click press effect scale
    var enableScaleAnimation: Boolean = true
    var clickScaleRatio: Float = 0.95f
    var allowSendImages: Boolean = true
    var allowSendVideos: Boolean = true
    var maxImagesPerChat: Int = 5
    var maxVideosPerChat: Int = 2
    var headerIconsVisible: String = "MENU,NOTIF,CHAT"
    var isFavoritesEnabled: Boolean = true
    var dynamicTabsList: String = "الرئيسية,المحلات,العقارات,المفضلة"
    var maxStorePhotos: Int = 5
    var maxPropertyPhotos: Int = 5
    var storesRequiredFields: String = "الاسم,الوصف,الهاتف,الموقع"
    var propertiesRequiredFields: String = "العنوان,الوصف,السعر,الهاتف"
    var recommendationsLayout: String = "GRID_HORIZONTAL"
    var storesTabName: String = "المحلات"
    var propertiesTabName: String = "العقارات"
    var storesRegistrationTerms: String = AdminSettingsDefaults.STORES_TERMS
    var propertiesRegistrationTerms: String = AdminSettingsDefaults.PROPERTIES_TERMS
    var showStoresPhotosOption: Boolean = true
    var showPropertiesPhotosOption: Boolean = true
    var dynamicSectionsData: String = ""
    var isMapsIconVisible: Boolean = true
    var maxCancellationHours: Int = 8
    var maxCancellationAttempts: Int = 3
    var enableBookingPassword: Boolean = true
    var bookingPasswordLength: Int = 4
    var enableUniqueBookingNumber: Boolean = true
    var bookingNumberPrefix: String = "BK"
    var advancePaymentPercentage: Int = 30
    var enableProgressTracking: Boolean = true
    var linkedProvidersForInstantBooking: String = ""
    var allowBookingWithoutDeposit: Boolean = true
    var showClientIdentityToProviders: Boolean = true
}
