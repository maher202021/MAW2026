package com.serviseyem.data

import androidx.annotation.Keep

@Keep
@com.google.firebase.firestore.IgnoreExtraProperties
data class AdminSettingsEntity(
    val id: String = "main_settings",
    val appName: String = "دليل خدمات اليمن",
    val welcomeMessage: String = "مرحباً بكم في منصة الخدمات اليمنية الشاملة",
    val footerMessage: String = "777644",
    val footerBgColorHex: String = "#115E59",
    val footerItemsOrder: String = "INFO,BOOKINGS,TEXT,LANG,ADMIN",
    val showInfoIcon: Boolean = true,
    val showBookingsIcon: Boolean = true,
    val showLangIcon: Boolean = true,
    val showAdminIcon: Boolean = true,
    val showFooterText: Boolean = true,
    val infoIconType: String = "INFO",
    val adminIconType: String = "LOCK",
    val langIconType: String = "GLOBE",
    val activeThemeId: String = "EMERALD_YEMEN",
    val customPrimaryHex: String = "#059669",
    val customSecondaryHex: String = "#115E59",
    val customBackgroundHex: String = "#0A0F0D",
    val customSurfaceHex: String = "#121D18",
    val isMaintenanceActive: Boolean = false,
    val hidePromoFooter: Boolean = false,
    val assistantHidden: Boolean = false,
    val showLoyaltyBanner: Boolean = false,
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
    val ownerEmail: String = "",
    @Deprecated("Use SecureAdminStorage / ownerPasswordHash instead")
    @get:com.google.firebase.firestore.Exclude
    val ownerPassword: String = "",
    
    // Notifications control
    val isNotificationsEnabled: Boolean = true,

    // Chat service states and controls
    val disableChatAll: Boolean = false,
    val disableChatProviders: Boolean = false,
    val disableChatUsers: Boolean = false,
    val disableChatSupervisors: Boolean = false,
    val chatDisabledAnnouncement: String = "خدمة الدردشة متوقفة حالياً للصيانة، نعتذر عن الإزعاج",
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
    val bookingRouting: String = "BOTH", // "BOTH", "ADMIN", "PROVIDER"

    // Section 10 layout and details customization
    val activeFontFamily: String = "CAIRO", // "CAIRO", "DEFAULT", "TAHOMA", "AMIRI"
    val registrationRequirements: String = "الاسم الثلاثي للفني|Mandatory,رقم الهاتف|Mandatory,قسم الصيانة|Mandatory,المدينة والمحافظة|Mandatory,الحي أو الشارع|Optional,صورة سيلفي شخصية|Optional,صورة الهوية المهنية|Optional,نماذج من أعمالك السابقة|Optional",
    val maxWorkPhotos: Int = 5,
    val showWorkPhotos: Boolean = true,
    val bypassVisitorRegistration: Boolean = true,
    val disableChatFirewall: Boolean = false,
    val disableBookingFirewall: Boolean = true,
    
    // X and Y scaling positions (as percentages of screen width/height, 0f..1f)
    val assistantPositionX: Float = 0.85f,
    val assistantPositionY: Float = 0.70f,
    val chatPositionX: Float = 0.85f,
    val chatPositionY: Float = 0.82f,
    val chatXOffset: Int = 20,
    val chatYOffset: Int = 80,
    val assistantXOffset: Int = 20,
    val assistantYOffset: Int = 140,

    // Map feature switches
    val isMapFeatureEnabled: Boolean = true,
    val mapProvider: String = "MAPLIBRE", // "MAPLIBRE", "GOOGLE", "MAPBOX"
    val mapDefaultZoom: Float = 14f,
    val mapMaxDistanceKm: Int = 20,
    val mapPrecisionDigits: Int = 1, // 1 or 2 digits after decimal

    // Assistant controls
    val isAssistantEnabled: Boolean = true,
    val isAssistantIconVisible: Boolean = true,

    // Card dimensions styles
    val coverHeight: Int = 0,
    val avatarSize: Int = 50,
    val avatarShape: String = "CIRCLE", // "CIRCLE" or "ROUNDED"
    val cardBackgroundHex: String = "#1E293B",
    val providerNameColorHex: String = "#FFFFFF",
    val ratingColorHex: String = "#F59E0B",
    val locationColorHex: String = "#94A3B8",
    val previewPriceColorHex: String = "#10B981",

    // Badges switches
    val showUserIdInsteadOfNameInChat: Boolean = false,
    val requirePaymentProofImage: Boolean = true,
    val enableBookingPaymentStep: Boolean = true,
    val showVipBadge: Boolean = true,
    val showVerifiedBadge: Boolean = true,
    val showRecommendedBadge: Boolean = true,
    val vipBadgeColorHex: String = "#D97706",
    val verifiedBadgeColorHex: String = "#3B82F6",
    val recommendedBadgeColorHex: String = "#10B981",

    // Buttons switches and colors
    val showCallButton: Boolean = true,
    val showVoiceCallButton: Boolean = false,
    val showInstantChatButton: Boolean = true,
    val showReviewButton: Boolean = true,
    val showReportButton: Boolean = true,
    val showWhatsappButton: Boolean = true,
    val showDetailsButton: Boolean = true,
    val showBookButton: Boolean = true,
    val callButtonColorHex: String = "#10B981",
    val whatsappButtonColorHex: String = "#25D366",
    val detailsButtonColorHex: String = "#3B82F6",
    val bookButtonColorHex: String = "#F59E0B",
    val buttonsOrder: String = "CALL,WHATSAPP,DETAILS,BOOK",

    // Information rows switches and order
    val showDistance: Boolean = true,
    val showPreviewPrice: Boolean = true,
    val showAvailability: Boolean = true,
    val showReviewsCount: Boolean = true,
    val infoRowsOrder: String = "NAME,RATING,DISTANCE,LOCATION,PRICE,AVAILABILITY",

    // Padding & Spacing
    val elementSpacing: Int = 8,
    val cardPadding: Int = 12,
    val cardMarginHorizontal: Int = 0,
    val cardMarginVertical: Int = 4,

    // Click press effect scale
    val enableScaleAnimation: Boolean = true,
    val clickScaleRatio: Float = 0.95f,

    // Admin customizable booking labels and terms
    val bookingTerms: String = "الشروط: لضمان جدية الحجز يرجى التواجد بالمنزل واستقبل الفني وتأكيد العنوان بدقة عبر الهاتف. الدليل لا يتحمل مسؤولية الاتفاقات الخاصة.",
    val bookingLabelName: String = "الاسم الثنائي/الثلاثي للعميل كامل",
    val bookingLabelPhone: String = "رقم الهاتف اليمني (أرقام فقط)",
    val bookingLabelArea: String = "منطقة السكن والحي بالتفصيل (مثل: صنعاء-شارع الستين)",
    val bookingLabelService: String = "نوع الخدمة المطلوبة بالتفصيل",

    // About App customization fields
    val aboutCoverType: String = "IMAGE", // IMAGE, VIDEO, TEXT
    val aboutCoverContent: String = "https://images.unsplash.com/photo-1542435503-956c469947f6?auto=format&fit=crop&w=600&q=80",
    // TODO: Migrate to FirebaseStorageUploader
    val aboutCoverBase64: String = "",
    val aboutCustomInfo: String = "تطبيق دليل خدمات اليمن الذكي هو منصة متكاملة مخصصة لربط المستخدمين والعملاء بأمهر الفنيين، المهندسين، ومقدمي الخدمات في شتى المجالات والصيانة في مختلف محافظات الجمهورية اليمنية بسهولة وسرعة فائقة.",

    // Main screen Banner configuration
    val bannerEnabled: Boolean = false,
    val bannerType: String = "TEXT", // "TEXT", "IMAGE", "VIDEO"
    val bannerContent: String = "",
    // TODO: Migrate to FirebaseStorageUploader
    val bannerBase64: String = "",
    val bannerLocation: String = "TOP", // "TOP", "BOTTOM"
    val bannerDurationSeconds: Int = 10, // 0 for persistent, otherwise duration in seconds
    val bannerDisplayStyle: String = "SLIDE", // "SLIDE", "FADE", "BLINK", "SCROLL"
    val appDownloadUrl: String = "https://example.com/download_app",
    val allowSendImages: Boolean = true,
    val allowSendVideos: Boolean = true,
    val maxImagesPerChat: Int = 5,
    val maxVideosPerChat: Int = 2,
    val showRefreshIcon: Boolean = false,
    val showSettingsIcon: Boolean = false,
    val headerIconsOrder: String = "MENU,NOTIF,CHAT",
    val headerIconsVisible: String = "MENU,NOTIF,CHAT",
    val categoriesLayoutType: String = "GRID_VERTICAL",
    val isFavoritesEnabled: Boolean = true,
    val encryptionType: String = "تشفير آمن سحابي",
    val splashWelcomeMessage: String = "التطبيق الأول في اليمن والوطن العربي الذي يربط مقدمي الخدمات وأصحاب المهن بالمستخدمين فورياً",
    val websiteUrl: String = "https://www.yamandelil.com",
    val telegramUrl: String = "https://t.me/yamandelil",
    val facebookUrl: String = "https://facebook.com/yamandelil",
    val twitterUrl: String = "https://twitter.com/yamandelil",
    val instagramUrl: String = "https://instagram.com/yamandelil",
    val youtubeUrl: String = "https://youtube.com/yamandelil",
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
    val minAdvanceAmount: Double = 500.0,
    val maxAdvanceAmount: Double = 50000.0,
    val isCommissionEnabled: Boolean = true,
    val paymentCommissionRate: Float = 0.10f,
    
    // Dynamic controls for Stores & Real Estate sections
    val isStoresEnabled: Boolean = true,
    val isPropertiesEnabled: Boolean = true,
    val dynamicTabsList: String = "الرئيسية,المحلات,العقارات,المفضلة",
    val maxStorePhotos: Int = 5,
    val maxPropertyPhotos: Int = 5,
    val storesRequiredFields: String = "الاسم,الوصف,الهاتف,الموقع",
    val propertiesRequiredFields: String = "العنوان,الوصف,السعر,الهاتف",
    val recommendationsLayout: String = "GRID_HORIZONTAL",
    val storesTabName: String = "المحلات",
    val propertiesTabName: String = "العقارات",
    val storesRegistrationTerms: String = "شروط تسجيل المحل: يرجى إدخال بيانات صحيحة ومطابقة للواقع التجاري وصور واضحة.",
    val propertiesRegistrationTerms: String = "شروط إضافة العقار: يرجى تأكيد ملكية العقار وصحة الصور والأسعار المعروضة.",
    val showStoresPhotosOption: Boolean = true,
    val showPropertiesPhotosOption: Boolean = true,
    val dynamicSectionsData: String = "",
    val isUserPasswordRequired: Boolean = false,
    val isBookingsIconVisible: Boolean = true,
    val isMapsIconVisible: Boolean = true,
    val bookingsIconType: String = "DateRange", // "DateRange", "Star", "Favorite", "Home"
    val bookingsAccessControl: String = "ALL", // "ALL", "REGISTERED_ONLY", "DISABLED"
    val blockedUsersForBookings: String = "",
    
    // 3D Golden Icons, Font Scaling, & Icon Customization Settings
    val topNavIconStyle: String = "GOLDEN_3D", // "GOLDEN_3D", "METALLIC", "MINIMAL"
    val navIconSizeDp: Int = 18,
    val globalFontScale: Float = 1.0f,
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
    val chatDisplayIdentityMode: String = "NAME_AND_PHONE", // "NAME_ONLY", "NAME_AND_PHONE", "NAME_AND_ID", "PHONE_ONLY"
    val isChatTextEnabled: Boolean = true,
    val isChatAudioEnabled: Boolean = true,
    val isChatImageEnabled: Boolean = true,
    val isChatVideoEnabled: Boolean = true,
    val isChatCallEnabled: Boolean = true,
    val chatRoutingMode: String = "DEFAULT", // "ADMIN_ONLY", "ADMIN_PROVIDERS", "ADMIN_CATEGORIES", "CATEGORY_ONLY", "ADMIN_SUPERVISORS", "SUPERVISORS_ONLY", "DEFAULT"
    val chatBlockedIds: String = "", // Comma-separated IDs/phones blocked from chat
    val chatDisabledCategories: String = "", // Comma-separated category IDs with disabled chat

    // Registration Forms Controls (Default: Providers ON, all others OFF)
    val enableProvidersRegistration: Boolean = true,
    val enableStoresRegistration: Boolean = false,
    val enableRestaurantsRegistration: Boolean = false,
    val enablePropertiesRegistration: Boolean = false,
    val enableMedicalRegistration: Boolean = false,
    val enableJobsRegistration: Boolean = false,

    // Admin Customizable Booking Settings
    val isBookingEnabled: Boolean = true,
    val routingMode: String = "auto_nearest",  // auto_nearest, admin_select, supervisor, specialty, admin_dispatch
    val maxCancellationHours: Int = 8,          // 8 ساعات قبل الموعد
    val maxCancellationAttempts: Int = 3,       // عدد محاولات الإلغاء المسموح بها
    val enableBookingPassword: Boolean = true,
    val bookingPasswordLength: Int = 4,
    val enableUniqueBookingNumber: Boolean = true,
    val bookingNumberPrefix: String = "BK",
    // ⚠️ مكرر للتوافقية: استخدم advancePaymentPercent للحسابات. advancePaymentPercentage للتوافقية مع الشاشات القديمة.
    val advancePaymentPercentage: Int = 30,
    val enableProgressTracking: Boolean = true,

    // Advanced Payment, Instant Booking & Deposit Rule Controls
    val linkedCategoriesForInstantBooking: String = "",
    val linkedProvidersForInstantBooking: String = "",
    val linkedProvidersForDeposit: String = "",
    val exemptUsersFromDeposit: String = "",
    val showWalletInProfile: Boolean = true,
    val allowBookingWithoutDeposit: Boolean = true,

    // Voice Call In-App System Controls
    // ملاحظة أمنية: عند التعارض، disableVoiceCalls يتفوق على voiceCallsEnabled
    val voiceCallsEnabled: Boolean = true,
    val voiceCallsAllowedCategories: String = "",
    val voiceCallsAllowedProviders: String = "",
    val voiceCallsAllowedUsers: String = "",
    val disableVoiceCalls: Boolean = true,
    val voiceCallsDisabledAnnouncement: String = "",
    val appLanguage: String = "ar",
    val hideTopHeaderBar: Boolean = false,
    val customAppName: String = "",
    val showClientIdentityToProviders: Boolean = true
)

@Keep
data class DynamicSection(
    val id: String = "",
    val name: String = "",
    val icon: String = "",
    val isEnabled: Boolean = true,
    val type: String = "store", // "store" or "property"
    val order: Int = 0,
    val terms: String = "",
    val maxPhotos: Int = 5,
    val showPhotos: Boolean = true,
    val allowPdf: Boolean = true,
    val requiredFields: String = "الاسم,الوصف,الهاتف,الموقع"
) {
    companion object {
        fun parseDynamicSections(serialized: String): List<DynamicSection> {
            val trimmed = serialized.trim()
            if (trimmed.isEmpty()) {
                return listOf(
                    DynamicSection("stores", "المحلات والمراكز", "🏪", true, "store", 1, "شروط تسجيل المحل: يرجى إدخال بيانات صحيحة ومطابقة للواقع التجاري وصور واضحة.", 10, true, true, "الاسم,الوصف,الهاتف,الموقع"),
                    DynamicSection("restaurants", "المطاعم والكافيهات", "🍔", true, "store", 2, "شروط تسجيل المطعم: توضيح نوع الوجبات، الأسعار، وساعات العمل وصور الوجبات.", 10, true, true, "الاسم,الوصف,الهاتف,الموقع,المنيو"),
                    DynamicSection("medical", "المراكز الطبية والعيادات", "🏥", true, "store", 3, "شروط تسجيل المركز الطبي: إضافة اسم الدكتور/المركز والتخصص وأوقات الدوام.", 10, true, true, "الاسم,التخصص,الهاتف,الموقع"),
                    DynamicSection("properties", "العقارات والأراضي", "🏠", true, "property", 4, "شروط إضافة العقار: يرجى تأكيد ملكية العقار وصحة الصور والأسعار المعروضة.", 10, true, true, "العنوان,الوصف,السعر,الهاتف"),
                    DynamicSection("jobs", "إعلانات الوظائف", "💼", true, "store", 5, "شروط إعلان الوظيفة: توضيح المسمى الوظيفي والراتب والشروط المطلوبة ورقم التواصل.", 5, true, true, "المسمى,الجهة,الهاتف,الراتب"),
                    DynamicSection("services", "المهن والخدمات", "🛠️", false, "store", 6, "شروط تسجيل الفني: تحديد المهنة/التخصص والأسعار التقديرية ونطاق العمل.", 10, true, true, "الاسم,المهنة,الهاتف,المنطقة")
                )
            }
            if (trimmed == "[]") return emptyList()
            // 1. Try standard JSON parsing
            if (trimmed.startsWith("[") && trimmed.endsWith("]")) {
                try {
                    val jsonArray = org.json.JSONArray(trimmed)
                    val list = mutableListOf<DynamicSection>()
                    for (i in 0 until jsonArray.length()) {
                        val obj = jsonArray.getJSONObject(i)
                        list.add(
                            DynamicSection(
                                id = obj.optString("id", ""),
                                name = obj.optString("name", ""),
                                icon = obj.optString("icon", ""),
                                isEnabled = obj.optBoolean("isEnabled", true),
                                type = obj.optString("type", "store"),
                                order = obj.optInt("order", i),
                                terms = obj.optString("terms", ""),
                                maxPhotos = obj.optInt("maxPhotos", 5),
                                showPhotos = obj.optBoolean("showPhotos", true),
                                allowPdf = obj.optBoolean("allowPdf", true),
                                requiredFields = obj.optString("requiredFields", "الاسم,الهاتف,الوصف")
                            )
                        )
                    }
                    return list.sortedBy { it.order }
                } catch (_: Exception) {}
            }
            // 2. Fallback to legacy delimiter parsing (;; and ||)
            return try {
                trimmed.split(";;").filter { it.isNotEmpty() }.map { sectionStr ->
                    val parts = sectionStr.split("||")
                    DynamicSection(
                        id = parts.getOrElse(0) { "" },
                        name = parts.getOrElse(1) { "" },
                        icon = parts.getOrElse(2) { "" },
                        isEnabled = parts.getOrElse(3) { "true" }.toBoolean(),
                        type = parts.getOrElse(4) { "store" },
                        order = parts.getOrElse(5) { "0" }.toIntOrNull() ?: 0,
                        terms = parts.getOrElse(6) { "" },
                        maxPhotos = parts.getOrElse(7) { "5" }.toIntOrNull() ?: 5,
                        showPhotos = parts.getOrElse(8) { "true" }.toBoolean(),
                        allowPdf = parts.getOrElse(9) { "true" }.toBoolean(),
                        requiredFields = parts.getOrElse(10) { "الاسم,الهاتف,الوصف" }
                    )
                }.sortedBy { it.order }
            } catch (e: Exception) {
                emptyList()
            }
        }

        fun serializeDynamicSections(list: List<DynamicSection>): String {
            return try {
                val jsonArray = org.json.JSONArray()
                for (sec in list) {
                    val obj = org.json.JSONObject()
                    obj.put("id", sec.id)
                    obj.put("name", sec.name)
                    obj.put("icon", sec.icon)
                    obj.put("isEnabled", sec.isEnabled)
                    obj.put("type", sec.type)
                    obj.put("order", sec.order)
                    obj.put("terms", sec.terms)
                    obj.put("maxPhotos", sec.maxPhotos)
                    obj.put("showPhotos", sec.showPhotos)
                    obj.put("allowPdf", sec.allowPdf)
                    obj.put("requiredFields", sec.requiredFields)
                    jsonArray.put(obj)
                }
                jsonArray.toString()
            } catch (_: Exception) {
                list.joinToString(";;") { sec ->
                    listOf(
                        sec.id,
                        sec.name,
                        sec.icon,
                        sec.isEnabled.toString(),
                        sec.type,
                        sec.order.toString(),
                        sec.terms,
                        sec.maxPhotos.toString(),
                        sec.showPhotos.toString(),
                        sec.allowPdf.toString(),
                        sec.requiredFields
                    ).joinToString("||")
                }
            }
        }
    }
}

@Keep
data class SpecialOfferEntity(
    val id: String = java.util.UUID.randomUUID().toString(),
    val providerId: String = "",
    val title: String = "",
    val description: String = "",
    val discountPercent: Int = 0,
    val originalPrice: Double = 0.0,
    val offerPrice: Double = 0.0,
    val expiryDate: String = "",
    val couponCode: String = "",
    val isEnabled: Boolean = true
) {
    companion object {
        fun parseList(jsonStr: String): List<SpecialOfferEntity> {
            val trimmed = jsonStr.trim()
            if (trimmed.isBlank() || trimmed == "[]") return emptyList()
            // 1. Try standard JSON parsing
            if (trimmed.startsWith("[") && trimmed.endsWith("]")) {
                try {
                    val jsonArray = org.json.JSONArray(trimmed)
                    val list = mutableListOf<SpecialOfferEntity>()
                    for (i in 0 until jsonArray.length()) {
                        val obj = jsonArray.getJSONObject(i)
                        list.add(
                            SpecialOfferEntity(
                                id = obj.optString("id", java.util.UUID.randomUUID().toString()),
                                providerId = obj.optString("providerId", ""),
                                title = obj.optString("title", ""),
                                description = obj.optString("description", ""),
                                discountPercent = obj.optInt("discountPercent", 0),
                                originalPrice = obj.optDouble("originalPrice", 0.0),
                                offerPrice = obj.optDouble("offerPrice", 0.0),
                                expiryDate = obj.optString("expiryDate", ""),
                                couponCode = obj.optString("couponCode", ""),
                                isEnabled = obj.optBoolean("isEnabled", true)
                            )
                        )
                    }
                    return list
                } catch (_: Exception) {}
            }
            // 2. Fallback to legacy delimiter parsing (;;; and |||)
            return try {
                trimmed.split(";;;").filter { it.isNotBlank() }.map { chunk ->
                    val p = chunk.split("|||")
                    SpecialOfferEntity(
                        id = p.getOrElse(0) { "" },
                        title = p.getOrElse(1) { "" },
                        description = p.getOrElse(2) { "" },
                        discountPercent = p.getOrElse(3) { "0" }.toIntOrNull() ?: 0,
                        originalPrice = p.getOrElse(4) { "0" }.toDoubleOrNull() ?: 0.0,
                        offerPrice = p.getOrElse(5) { "0" }.toDoubleOrNull() ?: 0.0,
                        expiryDate = p.getOrElse(6) { "" },
                        isEnabled = p.getOrElse(7) { "true" }.toBoolean(),
                        providerId = p.getOrElse(8) { "" },
                        couponCode = p.getOrElse(9) { "" }
                    )
                }
            } catch (e: Exception) {
                emptyList()
            }
        }

        fun serializeList(list: List<SpecialOfferEntity>): String {
            return try {
                val jsonArray = org.json.JSONArray()
                for (offer in list) {
                    val obj = org.json.JSONObject()
                    obj.put("id", offer.id)
                    obj.put("providerId", offer.providerId)
                    obj.put("title", offer.title)
                    obj.put("description", offer.description)
                    obj.put("discountPercent", offer.discountPercent)
                    obj.put("originalPrice", offer.originalPrice)
                    obj.put("offerPrice", offer.offerPrice)
                    obj.put("expiryDate", offer.expiryDate)
                    obj.put("couponCode", offer.couponCode)
                    obj.put("isEnabled", offer.isEnabled)
                    jsonArray.put(obj)
                }
                jsonArray.toString()
            } catch (_: Exception) {
                list.joinToString(";;;") { offer ->
                    listOf(
                        offer.id,
                        offer.title,
                        offer.description,
                        offer.discountPercent.toString(),
                        offer.originalPrice.toString(),
                        offer.offerPrice.toString(),
                        offer.expiryDate,
                        offer.isEnabled.toString(),
                        offer.providerId,
                        offer.couponCode
                    ).joinToString("|||")
                }
            }
        }
    }
}
