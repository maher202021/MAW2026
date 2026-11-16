package com.serviseyem.ui.screens.admin

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.serviseyem.data.AdminSettingsEntity
import com.serviseyem.data.BookingEntity
import com.serviseyem.data.CategoryEntity
import com.serviseyem.data.ChatChannelEntity
import com.serviseyem.data.CityEntity
import com.serviseyem.data.InternalWalletEntity
import com.serviseyem.data.NotificationEntity
import com.serviseyem.data.PaymentEntity
import com.serviseyem.data.PaymentWalletEntity
import com.serviseyem.data.PendingProviderEntity
import com.serviseyem.data.ProviderEntity
import com.serviseyem.data.SupervisorEntity

@Stable
internal class AdminPanelState(
    initialAuthorized: Boolean,
    initialSubTab: String,
    settingsState: AdminSettingsEntity
) {
    // Auth & Navigation
    var isAuthorized by mutableStateOf(initialAuthorized)
    var activeSubTab by mutableStateOf(initialSubTab)
    var selectedGroupFilter by mutableStateOf("ALL")

    // Section Sub-Tabs
    var adminBookingSubTab by mutableStateOf("SERVICES")
    var adminChatSubTab by mutableStateOf("SERVICES")
    var adminAddSubTab by mutableStateOf("SERVICES")
    var adminPasswordSubTab by mutableStateOf("REQUESTS")
    var categoryManagementMode by mutableStateOf("MAINTENANCE")

    // Category & City Drafts & Dialogs
    var showDeleteCategoryConfirmId by mutableStateOf<String?>(null)
    var showEditCategoryObj by mutableStateOf<CategoryEntity?>(null)
    var showEditCityObj by mutableStateOf<CityEntity?>(null)
    var showMergeCategoryObj by mutableStateOf<CategoryEntity?>(null)
    var selectedTargetCategoryIdForMerge by mutableStateOf("")
    var editCatName by mutableStateOf("")
    var editCatIcon by mutableStateOf("")
    var newCatName by mutableStateOf("")
    var newCatIcon by mutableStateOf("")
    var newCityArName by mutableStateOf("")
    var newCityEnName by mutableStateOf("")
    var newCityIcon by mutableStateOf("📍")

    // Pending Request Rejection
    var rejectingProviderRequest by mutableStateOf<PendingProviderEntity?>(null)
    var providerRejectionReasonText by mutableStateOf("")

    // Bookings Dialogs
    var showDeleteBookingConfirmId by mutableStateOf<String?>(null)
    var showRejectionReasonDialogId by mutableStateOf<String?>(null)
    var bookingRejectionReasonInput by mutableStateOf("")
    var editingBookingObj by mutableStateOf<BookingEntity?>(null)
    var redirectingBookingObj by mutableStateOf<BookingEntity?>(null)

    // Supervisors Drafts & Dialogs
    var editingSupervisorObj by mutableStateOf<SupervisorEntity?>(null)
    var supervisorInputName by mutableStateOf("")
    var supervisorInputRole by mutableStateOf("SUPPORT")
    var supervisorInputPasscode by mutableStateOf("")
    var supervisorInputPermissions by mutableStateOf<List<String>>(emptyList())

    // Notifications Drafts & Dialogs
    var showDeleteNotifConfirmId by mutableStateOf<String?>(null)
    var selectedRecoveryNotif by mutableStateOf<NotificationEntity?>(null)
    var notifTitleInput by mutableStateOf("")
    var notifMsgInput by mutableStateOf("")
    var notifTargetType by mutableStateOf("ALL")
    var notifTargetValue by mutableStateOf("")
    var notifDelayHours by mutableStateOf("")
    var notifValidityHours by mutableStateOf("")

    // Chats Dialogs
    var showActiveChatChannelObj by mutableStateOf<ChatChannelEntity?>(null)
    var adminChatReplyInput by mutableStateOf("")
    var showDeleteChatConfirmId by mutableStateOf<String?>(null)

    // Backup & Restore
    var backupJsonStringState by mutableStateOf("")
    var restoreJsonInputState by mutableStateOf("")

    // Manual Add (Providers)
    var manualName by mutableStateOf("")
    var manualPhone by mutableStateOf("")
    var manualCategoryId by mutableStateOf("")
    var manualStreet by mutableStateOf("")
    var manualCityId by mutableStateOf("")
    var manualPhotoUrl by mutableStateOf("")
    var manualIdCardUrl by mutableStateOf("")
    var manualForensicUrl by mutableStateOf("")
    var manualPriceValue by mutableStateOf("1500")
    var manualIsVipGolden by mutableStateOf(false)

    // Providers Filter
    var activeProvidersSearchQuery by mutableStateOf("")

    // Coupons Draft
    var couponCodeInput by mutableStateOf("")
    var couponPointsInput by mutableStateOf("100")
    var couponExpiryDaysInput by mutableStateOf("30")
    var couponDiscountInput by mutableStateOf("10")
    var couponMaxUsageInput by mutableStateOf("100")

    // Section 10 Theme & Card Customization Drafts
    var editPrimaryHex by mutableStateOf(settingsState.customPrimaryHex)
    var editSecondaryHex by mutableStateOf(settingsState.customSecondaryHex)
    var editCardBgHex by mutableStateOf(settingsState.cardBackgroundHex)
    var editProviderNameHex by mutableStateOf(settingsState.providerNameColorHex)
    var editLocationHex by mutableStateOf(settingsState.locationColorHex)
    var editRatingHex by mutableStateOf(settingsState.ratingColorHex)
    var editVipBadgeHex by mutableStateOf(settingsState.vipBadgeColorHex)
    var editVerifiedHex by mutableStateOf(settingsState.verifiedBadgeColorHex)
    var editRecommendedHex by mutableStateOf(settingsState.recommendedBadgeColorHex)
    var editFontSelected by mutableStateOf(settingsState.activeFontFamily)

    var editChatIconSize by mutableStateOf(settingsState.chatSize.toFloat())
    var editChatIconX by mutableStateOf(settingsState.chatXOffset.toFloat())
    var editChatIconY by mutableStateOf(settingsState.chatYOffset.toFloat())
    var editAssistantIconSize by mutableStateOf(settingsState.assistantSize.toFloat())
    var editAssistantIconX by mutableStateOf(settingsState.assistantXOffset.toFloat())
    var editAssistantIconY by mutableStateOf(settingsState.assistantYOffset.toFloat())

    var requirementItemInput by mutableStateOf("")
    var isNewRequirementMandatory by mutableStateOf(true)
    var requirementsListState by mutableStateOf(
        settingsState.registrationRequirements.split(",").filter { it.isNotBlank() }
    )

    var editCoverHeight by mutableStateOf(settingsState.coverHeight.toFloat())
    var editAvatarSize by mutableStateOf(settingsState.avatarSize.toFloat())
    var editElementSpacing by mutableStateOf(settingsState.elementSpacing.toFloat())
    var editCardPadding by mutableStateOf(settingsState.cardPadding.toFloat())

    var editShowVipBadge by mutableStateOf(settingsState.showVipBadge)
    var editShowVerifiedBadge by mutableStateOf(settingsState.showVerifiedBadge)
    var editShowRecommendedBadge by mutableStateOf(settingsState.showRecommendedBadge)

    var editShowCallButton by mutableStateOf(settingsState.showCallButton)
    var editShowWhatsappButton by mutableStateOf(settingsState.showWhatsappButton)
    var editShowDetailsButton by mutableStateOf(settingsState.showDetailsButton)
    var editShowBookButton by mutableStateOf(settingsState.showBookButton)

    var editCallButtonColorHex by mutableStateOf(settingsState.callButtonColorHex)
    var editWhatsappButtonColorHex by mutableStateOf(settingsState.whatsappButtonColorHex)
    var editDetailsButtonColorHex by mutableStateOf(settingsState.detailsButtonColorHex)
    var editBookButtonColorHex by mutableStateOf(settingsState.bookButtonColorHex)

    var editShowLoyaltyBanner by mutableStateOf(settingsState.showLoyaltyBanner)
    var editMaxWorkPhotos by mutableStateOf(settingsState.maxWorkPhotos.toFloat())

    // Wipe States
    var showWipeConfirmDialog by mutableStateOf(false)
    var wipeInputPassword by mutableStateOf("")
    var wipeProvidersChecked by mutableStateOf(true)
    var wipeBookingsChecked by mutableStateOf(true)
    var wipeChatsChecked by mutableStateOf(true)
    var wipeNotifsChecked by mutableStateOf(true)
    var wipeReportsChecked by mutableStateOf(true)
    var wipeCategoriesChecked by mutableStateOf(false)
    var wipePendingChecked by mutableStateOf(true)
    var wipeBannersChecked by mutableStateOf(true)
    var wipeSupervisorsChecked by mutableStateOf(false)
    var wipeCitiesChecked by mutableStateOf(false)
    var wipeThemesChecked by mutableStateOf(false)

    // Payment & Voice Call Settings Drafts
    var isPaymentEnabledInput by mutableStateOf(settingsState.isPaymentEnabled)
    var isBookingPaymentRequiredInput by mutableStateOf(settingsState.isBookingPaymentRequired)
    var requireAdvancePaymentInput by mutableStateOf(settingsState.requireAdvancePayment)
    var advancePaymentPercentInput by mutableStateOf(settingsState.advancePaymentPercent.toString())
    var minAdvanceAmountInput by mutableStateOf(settingsState.minAdvanceAmount.toString())
    var maxAdvanceAmountInput by mutableStateOf(settingsState.maxAdvanceAmount.toString())
    var isCommissionEnabledInput by mutableStateOf(settingsState.isCommissionEnabled)
    var paymentCommissionRateInput by mutableStateOf(settingsState.paymentCommissionRate.toString())
    var linkedCategoriesForInstantBookingInput by mutableStateOf(settingsState.linkedCategoriesForInstantBooking)
    var linkedProvidersForDepositInput by mutableStateOf(settingsState.linkedProvidersForDeposit)
    var exemptUsersFromDepositInput by mutableStateOf(settingsState.exemptUsersFromDeposit)
    var showWalletInProfileInput by mutableStateOf(settingsState.showWalletInProfile)
    var voiceCallsEnabledInput by mutableStateOf(settingsState.voiceCallsEnabled)
    var voiceCallsAllowedCategoriesInput by mutableStateOf(settingsState.voiceCallsAllowedCategories)
    var voiceCallsAllowedProvidersInput by mutableStateOf(settingsState.voiceCallsAllowedProviders)
    var voiceCallsAllowedUsersInput by mutableStateOf(settingsState.voiceCallsAllowedUsers)
    var disableVoiceCallsInput by mutableStateOf(settingsState.disableVoiceCalls)
    var hideTopHeaderBarInput by mutableStateOf(settingsState.hideTopHeaderBar)
    var customAppNameInput by mutableStateOf(settingsState.customAppName)

    // Internal Wallet & Payment Wallet Dialogs
    var showWalletTxDialog by mutableStateOf(false)
    var selectedWalletForTx by mutableStateOf<InternalWalletEntity?>(null)
    var txTypeInput by mutableStateOf("DEPOSIT")
    var txAmountInput by mutableStateOf("1000")
    var txNoteInput by mutableStateOf("شحن رصيد بواسطة الإدارة")

    var editingWalletObj by mutableStateOf<PaymentWalletEntity?>(null)
    var walletProviderInput by mutableStateOf("jeeb")
    var walletNumberInput by mutableStateOf("")
    var walletAccountNameInput by mutableStateOf("")
    var walletAccountNameArInput by mutableStateOf("")
    var walletDescriptionInput by mutableStateOf("")
    var walletInstructionsInput by mutableStateOf("")
    var walletTypeInput by mutableStateOf("BOTH")
    var walletCurrencyInput by mutableStateOf("YER")
    var walletIsVisibleInput by mutableStateOf(true)
    var walletIsDefaultInput by mutableStateOf(false)
    var walletDisplayOrderInput by mutableStateOf("0")
    var walletMinTransferInput by mutableStateOf("100")
    var walletMaxTransferInput by mutableStateOf("1000000")
    var walletStatusInput by mutableStateOf("active")
    var showAddWalletDialog by mutableStateOf(false)

    // Payment Verification / Rejection / Refund Dialogs
    var verifyingPaymentObj by mutableStateOf<PaymentEntity?>(null)
    var adminVerifyPaymentNote by mutableStateOf("")
    var rejectingPaymentObj by mutableStateOf<PaymentEntity?>(null)
    var adminRejectPaymentNote by mutableStateOf("")
    var refundingPaymentObj by mutableStateOf<PaymentEntity?>(null)
    var refundReasonInput by mutableStateOf("")

    // Preserved optional search/edit refs
    var showEditProviderMetadataObj by mutableStateOf<ProviderEntity?>(null)
}

@Composable
internal fun rememberAdminPanelState(
    adminRole: String,
    settingsState: AdminSettingsEntity
): AdminPanelState {
    val state = remember {
        AdminPanelState(
            initialAuthorized = adminRole != "GUEST",
            initialSubTab = if (adminRole == "OWNER") "BACKDOOR" else "REG_REQ",
            settingsState = settingsState
        )
    }

    // Keep auth & settings-bound sliders/switches synced identically to the original remember(key) calls
    remember(adminRole) {
        state.isAuthorized = adminRole != "GUEST"
        state.activeSubTab = if (adminRole == "OWNER") "BACKDOOR" else "REG_REQ"
        true
    }
    remember(settingsState.chatSize) { state.editChatIconSize = settingsState.chatSize.toFloat(); true }
    remember(settingsState.chatXOffset) { state.editChatIconX = settingsState.chatXOffset.toFloat(); true }
    remember(settingsState.chatYOffset) { state.editChatIconY = settingsState.chatYOffset.toFloat(); true }
    remember(settingsState.assistantSize) { state.editAssistantIconSize = settingsState.assistantSize.toFloat(); true }
    remember(settingsState.assistantXOffset) { state.editAssistantIconX = settingsState.assistantXOffset.toFloat(); true }
    remember(settingsState.assistantYOffset) { state.editAssistantIconY = settingsState.assistantYOffset.toFloat(); true }
    remember(settingsState.coverHeight) { state.editCoverHeight = settingsState.coverHeight.toFloat(); true }
    remember(settingsState.avatarSize) { state.editAvatarSize = settingsState.avatarSize.toFloat(); true }
    remember(settingsState.elementSpacing) { state.editElementSpacing = settingsState.elementSpacing.toFloat(); true }
    remember(settingsState.cardPadding) { state.editCardPadding = settingsState.cardPadding.toFloat(); true }
    remember(settingsState.showVipBadge) { state.editShowVipBadge = settingsState.showVipBadge; true }
    remember(settingsState.showVerifiedBadge) { state.editShowVerifiedBadge = settingsState.showVerifiedBadge; true }
    remember(settingsState.showRecommendedBadge) { state.editShowRecommendedBadge = settingsState.showRecommendedBadge; true }
    remember(settingsState.showCallButton) { state.editShowCallButton = settingsState.showCallButton; true }
    remember(settingsState.showWhatsappButton) { state.editShowWhatsappButton = settingsState.showWhatsappButton; true }
    remember(settingsState.showDetailsButton) { state.editShowDetailsButton = settingsState.showDetailsButton; true }
    remember(settingsState.showBookButton) { state.editShowBookButton = settingsState.showBookButton; true }
    remember(settingsState.callButtonColorHex) { state.editCallButtonColorHex = settingsState.callButtonColorHex; true }
    remember(settingsState.whatsappButtonColorHex) { state.editWhatsappButtonColorHex = settingsState.whatsappButtonColorHex; true }
    remember(settingsState.detailsButtonColorHex) { state.editDetailsButtonColorHex = settingsState.detailsButtonColorHex; true }
    remember(settingsState.bookButtonColorHex) { state.editBookButtonColorHex = settingsState.bookButtonColorHex; true }
    remember(settingsState.showLoyaltyBanner) { state.editShowLoyaltyBanner = settingsState.showLoyaltyBanner; true }
    remember(settingsState.maxWorkPhotos) { state.editMaxWorkPhotos = settingsState.maxWorkPhotos.toFloat(); true }

    remember(settingsState.isPaymentEnabled) { state.isPaymentEnabledInput = settingsState.isPaymentEnabled; true }
    remember(settingsState.isBookingPaymentRequired) { state.isBookingPaymentRequiredInput = settingsState.isBookingPaymentRequired; true }
    remember(settingsState.requireAdvancePayment) { state.requireAdvancePaymentInput = settingsState.requireAdvancePayment; true }
    remember(settingsState.advancePaymentPercent) { state.advancePaymentPercentInput = settingsState.advancePaymentPercent.toString(); true }
    remember(settingsState.minAdvanceAmount) { state.minAdvanceAmountInput = settingsState.minAdvanceAmount.toString(); true }
    remember(settingsState.maxAdvanceAmount) { state.maxAdvanceAmountInput = settingsState.maxAdvanceAmount.toString(); true }
    remember(settingsState.isCommissionEnabled) { state.isCommissionEnabledInput = settingsState.isCommissionEnabled; true }
    remember(settingsState.paymentCommissionRate) { state.paymentCommissionRateInput = settingsState.paymentCommissionRate.toString(); true }
    remember(settingsState.linkedCategoriesForInstantBooking) { state.linkedCategoriesForInstantBookingInput = settingsState.linkedCategoriesForInstantBooking; true }
    remember(settingsState.linkedProvidersForDeposit) { state.linkedProvidersForDepositInput = settingsState.linkedProvidersForDeposit; true }
    remember(settingsState.exemptUsersFromDeposit) { state.exemptUsersFromDepositInput = settingsState.exemptUsersFromDeposit; true }
    remember(settingsState.showWalletInProfile) { state.showWalletInProfileInput = settingsState.showWalletInProfile; true }
    remember(settingsState.voiceCallsEnabled) { state.voiceCallsEnabledInput = settingsState.voiceCallsEnabled; true }
    remember(settingsState.voiceCallsAllowedCategories) { state.voiceCallsAllowedCategoriesInput = settingsState.voiceCallsAllowedCategories; true }
    remember(settingsState.voiceCallsAllowedProviders) { state.voiceCallsAllowedProvidersInput = settingsState.voiceCallsAllowedProviders; true }
    remember(settingsState.voiceCallsAllowedUsers) { state.voiceCallsAllowedUsersInput = settingsState.voiceCallsAllowedUsers; true }
    remember(settingsState.disableVoiceCalls) { state.disableVoiceCallsInput = settingsState.disableVoiceCalls; true }
    remember(settingsState.hideTopHeaderBar) { state.hideTopHeaderBarInput = settingsState.hideTopHeaderBar; true }
    remember(settingsState.customAppName) { state.customAppNameInput = settingsState.customAppName; true }

    return state
}
