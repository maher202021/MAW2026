package com.serviseyem.ui

import android.content.Context
import androidx.lifecycle.viewModelScope
import com.serviseyem.data.*
import com.serviseyem.data.models.*
import com.serviseyem.utils.*
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

fun MainViewModel.applyFilters() {
    homeViewModel.applyFilters()
}

fun MainViewModel.setActiveBrowserTab(tabName: String) {
    homeViewModel.setActiveBrowserTab(tabName)
}

fun MainViewModel.openBrowserTab(tabName: String, categoryId: String? = null, query: String = "") {
    homeViewModel.openBrowserTab(tabName, categoryId, query)
}

fun MainViewModel.selectCategory(catId: String) {
    homeViewModel.selectCategory(catId)
}

fun MainViewModel.updateSearchQuery(query: String) {
    homeViewModel.updateSearchQuery(query)
}

fun MainViewModel.toggleVipFilter() {
    homeViewModel.toggleVipFilter()
}

fun MainViewModel.toggleAvailableFilter() {
    homeViewModel.toggleAvailableFilter()
}

fun MainViewModel.setCityFilter(city: String) {
    homeViewModel.setCityFilter(city)
}

fun MainViewModel.setNeighborhoodFilter(neighborhood: String) {
    homeViewModel.setNeighborhoodFilter(neighborhood)
}

fun MainViewModel.setPhoneOrNameFilter(query: String) {
    homeViewModel.setPhoneOrNameFilter(query)
}

fun MainViewModel.setRadiusKm(radius: Int) {
    homeViewModel.setRadiusKm(radius)
}

fun MainViewModel.registerBackdoorInteraction() {
    authViewModel.registerBackdoorInteraction()
}

fun MainViewModel.changeAdminCredentials(newPass: String, newOwnerPass: String = "") {
    val current = _settings.value
    val hashedOwnerPass = if (newOwnerPass.isNotBlank()) {
        if (newOwnerPass.contains(":")) newOwnerPass else SecureHasher.hashPassword(newOwnerPass.trim())
    } else ""
    val hashedAdminPass = if (newPass.isNotBlank()) {
        if (newPass.contains(":")) newPass else SecureHasher.hashPassword(newPass.trim())
    } else ""
    val ctx = appContext
    if (ctx != null) {
        SecureAdminStorage.storeCredentials(
            context = ctx,
            ownerEmail = current.ownerEmail.takeIf { it.isNotBlank() },
            ownerPassword = if (newOwnerPass.isNotBlank()) newOwnerPass.trim() else null,
            adminEmail = current.adminUsername.takeIf { it.isNotBlank() },
            adminPassword = if (newPass.isNotBlank()) newPass.trim() else null
        )
    }
    val hashUpdates = mutableMapOf<String, Any>()
    if (hashedOwnerPass.isNotBlank()) {
        hashUpdates["ownerPasswordHash"] = hashedOwnerPass
        if (current.ownerEmail.isNotBlank()) hashUpdates["ownerEmail"] = current.ownerEmail
    }
    if (hashedAdminPass.isNotBlank()) {
        hashUpdates["adminPasswordHash"] = hashedAdminPass
        if (current.adminUsername.isNotBlank()) hashUpdates["adminUsername"] = current.adminUsername
    }
    if (hashUpdates.isNotEmpty()) {
        db.collection("settings").document("main_settings")
            .set(hashUpdates, com.google.firebase.firestore.SetOptions.merge())
        db.collection("admin_secrets").document("credentials")
            .set(hashUpdates, com.google.firebase.firestore.SetOptions.merge())
    }
    val updated = current.copy(ownerPassword = "")
    _settings.value = updated
    settingsViewModel.updateAdminSettings(updated)
}

fun MainViewModel.authenticateAdmin(role: String) {
    if (role == "SUPERVISOR" && authViewModel._currentSupervisorPermissions.value.isEmpty() && adminViewModel._supervisorPermissions.value.isNotEmpty()) {
        authViewModel._currentSupervisorPermissions.value = adminViewModel._supervisorPermissions.value
    }
    authViewModel.authenticateAdmin(role)
    adminViewModel._adminRole.value = role
    if (role == "OWNER") {
        adminViewModel._supervisorPermissions.value = listOf("ALL")
    } else if (role == "SUPERVISOR" && adminViewModel._supervisorPermissions.value.isEmpty()) {
        adminViewModel._supervisorPermissions.value = authViewModel._currentSupervisorPermissions.value
    }
}

fun MainViewModel.authenticateAdmin(context: Context, role: String, remember: Boolean) {
    if (role == "SUPERVISOR" && authViewModel._currentSupervisorPermissions.value.isEmpty() && adminViewModel._supervisorPermissions.value.isNotEmpty()) {
        authViewModel._currentSupervisorPermissions.value = adminViewModel._supervisorPermissions.value
    }
    authViewModel.authenticateAdmin(context, role, remember)
    adminViewModel._adminRole.value = role
    if (role == "OWNER") {
        adminViewModel._supervisorPermissions.value = listOf("ALL")
    } else if (role == "SUPERVISOR" && adminViewModel._supervisorPermissions.value.isEmpty()) {
        adminViewModel._supervisorPermissions.value = authViewModel._currentSupervisorPermissions.value
    }
    // 🎯 إزالة saved_admin_role غير المشفر
}

fun MainViewModel.logout(context: Context) {
    authViewModel.logout(context)
    adminViewModel.logoutAdmin()
}

fun MainViewModel.navigateToScreen(screen: String) {
    if (screen == AppScreens.USER_BROWSE || screen == "HOME") {
        _screenBackStack.value = listOf(AppScreens.USER_BROWSE)
        _currentScreen.value = AppScreens.USER_BROWSE
        return
    }
    val updated = _screenBackStack.value.toMutableList()
    if (updated.lastOrNull() != screen) {
        updated.add(screen)
    }
    _screenBackStack.value = updated
    _currentScreen.value = screen
}

fun MainViewModel.navigateAndRemoveScreens(targetScreen: String, screensToRemove: List<String>) {
    val updated = _screenBackStack.value.toMutableList()
    updated.removeAll(screensToRemove)
    if (updated.lastOrNull() != targetScreen) {
        updated.add(targetScreen)
    }
    _screenBackStack.value = updated
    _currentScreen.value = targetScreen
}

fun MainViewModel.navigateTo(screen: String) {
    navigateToScreen(screen)
}

fun MainViewModel.goBack(): Boolean {
    // إذا كان المستخدم في الشاشة الرئيسية حالياً، امنع العودة للشاشات السابقة واطلب الخروج من التطبيق
    if (_currentScreen.value == AppScreens.USER_BROWSE || _currentScreen.value == "HOME") {
        _screenBackStack.value = listOf(AppScreens.USER_BROWSE)
        return false
    }
    val current = _screenBackStack.value
    if (current.size > 1) {
        val updated = current.toMutableList()
        updated.removeAt(updated.size - 1)
        while (updated.size > 1 && updated.last() == _currentScreen.value) {
            updated.removeAt(updated.size - 1)
        }
        val targetScreen = updated.lastOrNull() ?: AppScreens.USER_BROWSE
        _screenBackStack.value = updated
        _currentScreen.value = targetScreen
        return true
    } else {
        _currentScreen.value = AppScreens.USER_BROWSE
        _screenBackStack.value = listOf(AppScreens.USER_BROWSE)
        return false
    }
}

fun MainViewModel.switchLanguage() {
    val ctx = appContext
    if (ctx != null) {
        val newLang = LocaleManager.toggleLanguage(ctx)
        _currentLanguage.value = newLang
    } else {
        val newLang = if (_currentLanguage.value == "ar") "en" else "ar"
        _currentLanguage.value = newLang
    }
}

fun MainViewModel.toggleLanguage(context: Context) {
    appContext = context.applicationContext
    val newLang = LocaleManager.toggleLanguage(context)
    _currentLanguage.value = newLang
}

fun MainViewModel.setLanguage(lang: String) {
    _currentLanguage.value = lang
    val ctx = appContext
    if (ctx != null) {
        LocaleManager.setLanguage(ctx, lang)
    }
    val newSettings = _settings.value.copy(appLanguage = lang)
    _settings.value = newSettings
    try {
        settingsViewModel.updateAppLanguage(lang)
    } catch (e: Exception) {
        android.util.Log.e("MainViewModel", "Error: ", e)
    }
}

fun MainViewModel.setLanguage(context: Context, lang: String) {
    appContext = context.applicationContext
    setLanguage(lang)
}

fun MainViewModel.loadUserPoints() {
    _currentUserPoints.value = (100..500).random()
}

fun MainViewModel.redeemLoyaltyPoints() {
    triggerNotification("🎉 تم استبدال نقاطك بنجاح! تم الخصم بنجاح.")
}

fun MainViewModel.rewardSharePoints() {
    _currentUserPoints.value = _currentUserPoints.value + 20
    triggerNotification("🎁 حصلت على 20 نقطة مشاركة!")
}

fun MainViewModel.clearSmartAssistantChatHistory() {
    _currentUserPoints.value = 0
    triggerNotification("🧹 تم تصفية وحذف سجل المحادثة الذكية بنجاح!")
}

fun MainViewModel.submitJoinForm(
    context: Context,
    name: String, phone: String, catId: String, area: String,
    neighborhood: String, photoPath: String, idCardPath: String, gpsCoords: String,
    workPhotos: List<String> = emptyList(),
    customCategoryName: String = "",
    password: String = "",
    productAttachmentsJson: String = ""
) {
    registrationHelper.submitJoinForm(
        context = context,
        scope = viewModelScope,
        name = name,
        phone = phone,
        catId = catId,
        area = area,
        neighborhood = neighborhood,
        photoPath = photoPath,
        idCardPath = idCardPath,
        gpsCoords = gpsCoords,
        workPhotos = workPhotos,
        customCategoryName = customCategoryName,
        password = password,
        productAttachmentsJson = productAttachmentsJson,
        checkDuplicate = { checkAndGetDuplicateAccountType(it, "") },
        logAdminActivity = { logAdminActivity(it) },
        triggerNotification = { triggerNotification(it) },
        addApplicantNotification = { title, msg, type, targetVal ->
            addNotification(
                title = title,
                message = msg,
                targetType = type,
                targetValue = targetVal
            )
        },
        onPendingAdded = { newReq ->
            val currentPending = _pendingProviders.value.filter { it.id != newReq.id }.toMutableList()
            currentPending.add(newReq)
            _pendingProviders.value = currentPending
            val currentTechs = _pendingTechnicians.value.filter { it.id != newReq.id }.toMutableList()
            currentTechs.add(newReq)
            _pendingTechnicians.value = currentTechs
        },
        onStoreAdded = { newStore ->
            val sList = _stores.value.toMutableList()
            sList.removeAll { it.id == newStore.id }
            sList.add(newStore)
            _stores.value = sList
        },
        onPropertyAdded = { newProp ->
            val pList = _properties.value.toMutableList()
            pList.removeAll { it.id == newProp.id }
            pList.add(newProp)
            _properties.value = pList
        },
        onJobAdded = { newJob ->
            val jList = _jobs.value.toMutableList()
            jList.removeAll { it.id == newJob.id }
            jList.add(newJob)
            _jobs.value = jList
        },
        onClientAdded = { userMap ->
            val uList = _registeredUsersList.value.toMutableList()
            uList.removeAll { it["phone"] == userMap["phone"] }
            uList.add(userMap)
            _registeredUsersList.value = uList
        },
        onJoinRequestPhoneUpdated = { _joinRequestPhone.value = it },
        onNavigateToScreen = { _currentScreen.value = it }
    )
}

fun MainViewModel.registerClientUser(name: String, phone: String, residence: String, password: String = "") {
    registrationHelper.registerClientUser(name, phone, residence, password) { userMap ->
        val uList = _registeredUsersList.value.toMutableList()
        uList.removeAll { it["phone"] == userMap["phone"] }
        uList.add(userMap)
        _registeredUsersList.value = uList
    }
}

fun MainViewModel.cancelOrResetJoinRequest(context: Context) {
    registrationHelper.cancelOrResetJoinRequest(
        context = context,
        phone = _joinRequestPhone.value,
        pendingProviders = _pendingProviders.value,
        onPendingRemoved = { id ->
            _pendingProviders.value = _pendingProviders.value.filter { it.id != id }
        },
        onPhoneCleared = { _joinRequestPhone.value = "" },
        onGoBack = { goBack() }
    )
}

fun MainViewModel.setJoinRequestPhone(context: Context, phone: String) {
    registrationHelper.setJoinRequestPhone(context, phone) {
        _joinRequestPhone.value = it
    }
}

fun MainViewModel.setPasswordRecoveryWaitingPhone(phone: String) = authViewModel.setPasswordRecoveryWaitingPhone(phone)
fun MainViewModel.resetRegistrationState() = authViewModel.resetRegistrationState()
fun MainViewModel.searchAccountForRestore(cleanPhone: String, onResult: (MainViewModel.RestoreAccountMatch?) -> Unit) {
    accountRecoveryHelper.searchAccountForRestore(cleanPhone, onResult)
}

fun MainViewModel.verifyRestorePassword(cleanPhone: String, accountType: String, passwordInput: String, onResult: (Boolean) -> Unit) {
    accountRecoveryHelper.verifyRestorePassword(cleanPhone, accountType, passwordInput, onResult)
}

fun MainViewModel.requestPasswordReset(
    context: Context,
    phone: String,
    name: String,
    accountType: String,
    channel: String = "IN_APP_CHAT",
    onResult: (Boolean) -> Unit
) {
    accountRecoveryHelper.requestPasswordReset(
        context = context,
        phone = phone,
        name = name,
        accountType = accountType,
        onPasswordWaitingPhoneSet = { setPasswordRecoveryWaitingPhone(it) },
        triggerNotification = { triggerNotification(it) },
        onResult = onResult,
        channel = channel
    )
}

fun MainViewModel.adminResolvePasswordReset(
    context: Context,
    phone: String,
    newPassword: String,
    onResult: (Boolean) -> Unit
) {
    accountRecoveryHelper.adminResolvePasswordReset(
        context = context,
        phone = phone,
        newPassword = newPassword,
        onResult = onResult
    )
}

fun MainViewModel.isUserLoggedIn(context: Context): Boolean {
    val isLoggedIn = preferenceHelper.isAccountLoggedIn(context)
    val phone = currentUserPhone.value
    return isLoggedIn || (phone.isNotBlank() && currentUserId.value != "guest" && currentUserId.value.isNotBlank())
}

fun MainViewModel.restoreUserAccountByPhoneAndPassword(
    context: Context,
    phone: String,
    password: String,
    onResult: (Boolean, String) -> Unit
) {
    val email = getAuthEmailForPhone(phone)
    auth.signInWithEmailAndPassword(email, password)
        .addOnSuccessListener { authResult ->
            val userId = authResult.user?.uid ?: ""
            viewModelScope.launch {
                adminViewModel.crud.updateFields("users", userId, mapOf("isDeleted" to false))
                adminViewModel.crud.updateFields("providers", phone, mapOf("isDeleted" to false))
                adminViewModel.crud.updateFields("stores", phone, mapOf("isDeleted" to false))
                adminViewModel.crud.updateFields("properties", phone, mapOf("isDeleted" to false))
                onResult(true, "تم استعادة الحساب بنجاح!")
            }
        }
        .addOnFailureListener { e ->
            onResult(false, e.message ?: "فشل في تسجيل الدخول")
        }
}

fun MainViewModel.restoreGuestUser(context: Context, phone: String, password: String, onResult: (Boolean, String) -> Unit) {
    restoreUserAccountByPhoneAndPassword(context, phone, password, onResult)
}

fun MainViewModel.loginUserDirectly(context: Context, phone: String, password: String) = authViewModel.loginUserDirectly(context, phone, password)
fun MainViewModel.showBackdoorDialog() = authViewModel.showBackdoorDialog()
fun MainViewModel.dismissBackdoorDialog() = authViewModel.dismissBackdoorDialog()
fun MainViewModel.setSupervisorSession(sup: SupervisorEntity) {
    authViewModel.setSupervisorSession(sup)
    adminViewModel._adminRole.value = "SUPERVISOR"
    adminViewModel._supervisorPermissions.value = sup.permissions
}
fun MainViewModel.hasAdminPermission(permissionKey: String): Boolean = authViewModel.hasAdminPermission(permissionKey)
fun MainViewModel.updateSupervisorPermissions(id: String, permissions: List<String>) = authViewModel.updateSupervisorPermissions(id, permissions)
fun MainViewModel.removeSupervisor(id: String) = authViewModel.removeSupervisor(id)

val MainViewModel.isProviderUser: Boolean get() = adminRole.value == "PROVIDER" || adminRole.value == "TECHNICIAN"
val MainViewModel._currentSupervisorPermissions get() = authViewModel._currentSupervisorPermissions
val MainViewModel.currentSupervisorPermissions get() = authViewModel.currentSupervisorPermissions

fun MainViewModel.verifyAdminOrOwnerPassword(password: String, adminPass: String = "", ownerPass: String = ""): Boolean {
    val effectiveAdmin = adminPass
    val effectiveOwner = ownerPass.ifEmpty { settings.value.ownerPassword }
    if (authViewModel.verifyAdminOrOwnerPassword(password, effectiveAdmin, effectiveOwner)) {
        return true
    }
    val ctx = appContext
    return if (ctx != null) {
        SecureAdminStorage.verifyStoredPasswordOnly(ctx, password)
    } else {
        false
    }
}

fun MainViewModel.setUserSessionDetails(context: Context, name: String, phone: String, residence: String = "اليمن") {
    authViewModel.setUserSessionDetails(context, name, phone, residence)
}

fun MainViewModel.registerGuestUser(
    context: Context,
    name: String,
    phone: String,
    residence: String,
    password: String = "",
    confirmPassword: String = password
) {
    authViewModel.registerGuestUser(context, name, phone, residence, password, confirmPassword)
}

fun MainViewModel.toggleFavorite(id: String) {
    val current = _favoriteIds.value.toMutableSet()
    if (current.contains(id)) {
        current.remove(id)
        triggerNotification("💔 تم إزالة العنصر من المفضلة")
    } else {
        current.add(id)
        triggerNotification("💖 تم إضافة العنصر إلى المفضلة")
    }
    _favoriteIds.value = current
}

fun MainViewModel.addSupervisor(name: String, role: String, passcode: String, permissions: List<String> = emptyList()) =
    authViewModel.addSupervisor(name, role, passcode, permissions)

fun MainViewModel.editSupervisor(id: String, name: String, role: String, passcode: String, permissions: List<String> = emptyList()) =
    authViewModel.editSupervisor(id, name, role, passcode, permissions)

fun MainViewModel.updateBusinessAccountStatus(accountId: String, isActive: Boolean) {
    viewModelScope.launch {
        try {
            adminViewModel.crud.updateFields("stores", accountId, mapOf("isActive" to isActive))
        } catch (e: Exception) {
            android.util.Log.e("MainViewModel", "Error: ", e)
        }
        try {
            adminViewModel.crud.updateFields("providers", accountId, mapOf("isAvailable" to isActive))
        } catch (e: Exception) {
            android.util.Log.e("MainViewModel", "Error: ", e)
        }
    }
}

fun MainViewModel.updateEntityImages(collection: String, id: String, profileImg: String, coverImg: String) {
    val updates = mutableMapOf<String, Any>()
    if (profileImg.isNotBlank()) {
        updates["profileImage"] = profileImg
        updates["logoImage"] = profileImg
    }
    if (coverImg.isNotBlank()) {
        updates["coverImage"] = coverImg
    }
    if (updates.isNotEmpty()) {
        viewModelScope.launch { adminViewModel.crud.updateFields(collection, id, updates) }
        triggerNotification("📸 تم تحديث الصور بنجاح")
    }
}

fun MainViewModel.acceptRequestOffer(
    req: InstantRequestEntity,
    offer: RequestOfferEntity
) = instantRequestViewModel.acceptRequestOffer(req, offer)

fun MainViewModel.completeInstantRequest(requestId: String) = instantRequestViewModel.completeInstantRequest(requestId)

fun MainViewModel.createInstantRequest(
    userId: String,
    userName: String,
    userPhone: String,
    userCity: String,
    userNeighborhood: String,
    categoryId: String,
    categoryName: String,
    serviceTitle: String,
    description: String,
    images: List<String> = emptyList(),
    urgencyTime: String = "فوراً (خلال 30 دقيقة)",
    deliveryMethod: String = "",
    customPin: String = "",
    onResult: (Boolean, String, String) -> Unit = { _, _, _ -> }
) {
    instantRequestViewModel.createInstantRequest(
        userId, userName, userPhone, userCity, userNeighborhood, categoryId, categoryName, serviceTitle, description, images, urgencyTime, deliveryMethod, customPin, onResult
    )
}

fun MainViewModel.submitOfferForRequest(
    requestId: String,
    requestCode: String,
    technicianId: String,
    technicianName: String,
    technicianPhone: String,
    technicianAvatar: String,
    technicianRating: Float,
    price: Double,
    estimatedArrivalTime: String = "خلال 30 دقيقة",
    estimatedDuration: String = "ساعتان",
    notes: String = ""
) {
    instantRequestViewModel.submitOfferForRequest(
        requestId, requestCode, technicianId, technicianName, technicianPhone, technicianAvatar, technicianRating, price, estimatedArrivalTime, estimatedDuration, notes
    )
}

fun MainViewModel.cancelInstantRequest(requestId: String, userPin: String = "") {
    instantRequestViewModel.cancelInstantRequest(requestId = requestId, userPin = userPin)
}

fun MainViewModel.setCurrentUserPhone(phone: String) {
    authViewModel.setCurrentUserPhone(phone)
}
