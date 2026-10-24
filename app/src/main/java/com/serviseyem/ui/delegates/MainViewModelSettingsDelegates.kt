package com.serviseyem.ui

import android.content.Context
import com.serviseyem.data.*
import com.serviseyem.ui.viewmodels.SettingsViewModel.CardSettings
import com.serviseyem.ui.viewmodels.SettingsViewModel.ChatParticipantType

fun MainViewModel.loadCardSettings() = settingsViewModel.loadCardSettings()
fun MainViewModel.updateCardSettings(settings: CardSettings) = settingsViewModel.updateCardSettings(settings)
fun MainViewModel.updateTheme(themeId: String) = settingsViewModel.updateTheme(themeId)
fun MainViewModel.saveCustomSettingsState(newSettings: AdminSettingsEntity) = settingsViewModel.saveCustomSettingsState(newSettings)
fun MainViewModel.updateAdminSettings(newSettings: AdminSettingsEntity) = settingsViewModel.updateAdminSettings(newSettings)
fun MainViewModel.initColorSync(context: Context) = settingsViewModel.initColorSync(context)
fun MainViewModel.updateCloudColorScheme(context: Context, newScheme: ColorSchemeEntity) = settingsViewModel.updateCloudColorScheme(context, newScheme)
fun MainViewModel.updatePersonalColors(context: Context, personal: PersonalColors) = settingsViewModel.updatePersonalColors(context, personal)
fun MainViewModel.triggerManualSync(context: Context) = settingsViewModel.triggerManualSync(context)
fun MainViewModel.resolveConflict(context: Context, useCloud: Boolean) = settingsViewModel.resolveConflict(context, useCloud)
fun MainViewModel.getCurrentTimestampString(): String = settingsViewModel.getCurrentTimestampString()
fun MainViewModel.addNewSyncLog(
    context: Context,
    type: String,
    status: String,
    changes: List<String>,
    versionFrom: Int,
    versionTo: Int
) = settingsViewModel.addNewSyncLog(context, type, status, changes, versionFrom, versionTo)
fun MainViewModel.toggleChatParticipant(participantType: ChatParticipantType) = settingsViewModel.toggleChatParticipant(participantType)
fun MainViewModel.isChatBlockedFor(participantType: ChatParticipantType): Boolean = settingsViewModel.isChatBlockedFor(participantType)
fun MainViewModel.canParticipateInChat(participantType: ChatParticipantType): Boolean = settingsViewModel.canParticipateInChat(participantType)
fun MainViewModel.startVoiceCall(name: String, role: String) = settingsViewModel.startVoiceCall(name, role)
fun MainViewModel.endVoiceCall() = settingsViewModel.endVoiceCall()
fun MainViewModel.exportComplaintsToCSV() = settingsViewModel.exportComplaintsToCSV()
fun MainViewModel.exportComplaintsToPDF() = settingsViewModel.exportComplaintsToPDF()
fun MainViewModel.exportPerformanceReportToPDF() = settingsViewModel.exportPerformanceReportToPDF()
fun MainViewModel.createSystemBackup(onComplete: (Boolean, String) -> Unit) = settingsViewModel.createSystemBackup(onComplete)
fun MainViewModel.restoreSystemFromBackup(jsonStr: String, onComplete: (Boolean, String) -> Unit) = settingsViewModel.restoreSystemFromBackup(jsonStr, onComplete)
fun MainViewModel.exportSelectedCollectionsAsJson(selectedCollections: List<String>, onResult: (String) -> Unit) = settingsViewModel.exportSelectedCollectionsAsJson(selectedCollections, onResult)
fun MainViewModel.saveBackupToLocalStorage(context: Context, jsonStr: String, fileName: String): String = settingsViewModel.saveBackupToLocalStorage(context, jsonStr, fileName)
fun MainViewModel.setSecondaryFirebaseConfig(projectId: String, apiKey: String, appId: String, storageBucket: String, isEnabled: Boolean) = settingsViewModel.setSecondaryFirebaseConfig(projectId, apiKey, appId, storageBucket, isEnabled)
fun MainViewModel.saveCustomPermissionsMatrixToFirestore(permissions: List<String>) = settingsViewModel.saveCustomPermissionsMatrixToFirestore(permissions)
fun MainViewModel.deleteColorPalette(id: String) = settingsViewModel.deleteColorPalette(id)
fun MainViewModel.resetAccountPassword(entityType: String, phoneOrId: String, newPass: String) = settingsViewModel.resetAccountPassword(entityType, phoneOrId, newPass)
fun MainViewModel.requestAdminPasswordReset(phone: String) = settingsViewModel.requestAdminPasswordReset(phone)
fun MainViewModel.requestPasswordReset(phone: String, onResult: (Boolean, String) -> Unit) = settingsViewModel.requestPasswordReset(phone, onResult)
fun MainViewModel.approvePasswordReset(phone: String, onResult: (Boolean, String) -> Unit) = settingsViewModel.approvePasswordReset(phone, onResult)
fun MainViewModel.adminResetAccountPassword(phone: String, newPassword: String, notifyAction: String, customerName: String) = settingsViewModel.adminResetAccountPassword(phone, newPassword, notifyAction, customerName)
fun MainViewModel.requestPasswordRecoveryForStore(name: String, phone: String, password: String) = settingsViewModel.requestPasswordRecoveryForStore(name, phone, password)
fun MainViewModel.requestPasswordRecoveryForProperty(title: String, phone: String, password: String) = settingsViewModel.requestPasswordRecoveryForProperty(title, phone, password)
fun MainViewModel.requestPasswordRecoveryGeneral(accountName: String, phone: String, accountType: String, currentPassword: String) = settingsViewModel.requestPasswordRecoveryGeneral(accountName, phone, accountType, currentPassword)
fun MainViewModel.wipeAllDatabaseData(password: String): Boolean = settingsViewModel.wipeAllDatabaseData(password)
fun MainViewModel.wipeSelectedDatabaseData(password: String, selectedCollections: List<String>): Boolean = settingsViewModel.wipeSelectedDatabaseData(password, selectedCollections)
fun MainViewModel.wipeAllMockAndTemporaryData() = settingsViewModel.wipeAllMockAndTemporaryData()

fun MainViewModel.updateBackdoorSettings(
    appName: String, welcomeMsg: String, footerMsg: String, themeId: String,
    supportPhone: String, supportEmail: String, supportWhatsapp: String,
    isMaintenance: Boolean, hiddenFooter: Boolean, botHidden: Boolean, botSize: Int,
    chatHidden: Boolean, chatSize: Int, radiusKm: Int, isSpeech: Boolean,
    isDataSaver: Boolean, imgQuality: Int,
    bookingTerms: String = "يرجى الالتزام التام بالمواعيد المحددة والتسعيرة المتفق عليها مع الفني.",
    bookingLabelName: String = "الاسم الكامل للعميل",
    bookingLabelPhone: String = "رقم هاتف العميل للتواصل (مثال: 777000111)",
    bookingLabelArea: String = "المنطقة والحي السكني",
    bookingLabelService: String = "تفاصيل ونوع الخدمة المطلوبة",
    adminUsername: String = "",
    adminPassword: String = "",
    customPrimaryHex: String = "#059669",
    customSecondaryHex: String = "#115E59",
    customBackgroundHex: String = "#0A0F0D",
    customSurfaceHex: String = "#121D18"
) = settingsViewModel.updateBackdoorSettings(
    appName, welcomeMsg, footerMsg, themeId, supportPhone, supportEmail, supportWhatsapp,
    isMaintenance, hiddenFooter, botHidden, botSize, chatHidden, chatSize, radiusKm, isSpeech,
    isDataSaver, imgQuality, bookingTerms, bookingLabelName, bookingLabelPhone, bookingLabelArea,
    bookingLabelService, adminUsername, adminPassword, customPrimaryHex, customSecondaryHex,
    customBackgroundHex, customSurfaceHex
)

fun MainViewModel.addColorPalette(name: String, primaryHex: String, secondaryHex: String, backgroundHex: String = "#0A0F0D", surfaceHex: String = "#121D18") =
    settingsViewModel.addColorPalette(name, primaryHex, secondaryHex, backgroundHex, surfaceHex)
