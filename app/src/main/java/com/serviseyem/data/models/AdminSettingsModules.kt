package com.serviseyem.data.models

import androidx.annotation.Keep

@Keep
data class BrandingSettings(
    val appName: String = "دليل خدمات اليمن",
    val welcomeMessage: String = "مرحباً بكم في منصة الخدمات اليمنية الشاملة",
    val activeThemeId: String = "EMERALD_YEMEN",
    val customPrimaryHex: String = "#059669",
    val customSecondaryHex: String = "#115E59",
    val customBackgroundHex: String = "#0A0F0D",
    val customSurfaceHex: String = "#121D18",
    val appVersion: String = "v2.2026"
)

@Keep
data class ChatSettings(
    val chatHidden: Boolean = false,
    val chatSize: Int = 56,
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
    val allowTextToSpeech: Boolean = true
)

@Keep
data class BookingSettings(
    val defaultBookingStatus: String = "PENDING",
    val requireAdvancePayment: Boolean = false,
    val autoApproveBookings: Boolean = false,
    val bookingCancelTimeoutHours: Int = 24,
    val maxActiveBookingsPerUser: Int = 5
)

@Keep
data class PaymentSettings(
    val defaultCurrency: String = "YER",
    val commissionPercentage: Double = 5.0,
    val minWithdrawalAmount: Double = 1000.0,
    val enableBankTransfers: Boolean = true,
    val enableWalletPayments: Boolean = true
)

@Keep
data class UiSettings(
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
    val hidePromoFooter: Boolean = false,
    val assistantHidden: Boolean = false,
    val showLoyaltyBanner: Boolean = false,
    val assistantSize: Int = 56
)

@Keep
data class SupportSettings(
    val supportPhone: String = "777644",
    val supportWhatsapp: String = "777644",
    val supportEmail: String = "",
    val adminUsername: String = "",
    val ownerEmail: String = "",
    @Deprecated("Use SecureAdminStorage / ownerPasswordHash instead")
    @get:com.google.firebase.firestore.Exclude
    val ownerPassword: String = ""
)

@Keep
data class RegistrationSettings(
    val isMaintenanceActive: Boolean = false,
    val allowVoiceInputJoinForm: Boolean = true,
    val isNotificationsEnabled: Boolean = true,
    val requirePhoneVerification: Boolean = true,
    val autoApproveProviders: Boolean = false
)

@Keep
data class MapSettings(
    val maxSearchRadiusKm: Int = 20,
    val isSpeechSearchEnabled: Boolean = true,
    val defaultCity: String = "صنعاء"
)

@Keep
data class AdminSettingsHolder(
    val branding: BrandingSettings = BrandingSettings(),
    val chat: ChatSettings = ChatSettings(),
    val booking: BookingSettings = BookingSettings(),
    val payment: PaymentSettings = PaymentSettings(),
    val ui: UiSettings = UiSettings(),
    val support: SupportSettings = SupportSettings(),
    val registration: RegistrationSettings = RegistrationSettings(),
    val map: MapSettings = MapSettings()
)
