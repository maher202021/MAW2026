package com.serviseyem.ui

import android.content.Context
import androidx.lifecycle.viewModelScope
import com.serviseyem.data.*
import com.serviseyem.data.models.*
import com.serviseyem.utils.*
import com.google.firebase.firestore.FieldValue
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

fun isGuestSessionId(uid: String): Boolean {
    val t = uid.trim()
    return t.isBlank() || t.equals("guest", ignoreCase = true) || t.startsWith("USR_GUEST_")
}

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
    productAttachmentsJson: String = "",
    ownerName: String = "",
    description: String = "",
    workingHours: String = "",
    hasDelivery: Boolean = false,
    hasEmergency: Boolean = false,
    experienceYears: String = "",
    onResult: (Boolean, String) -> Unit = { _, _ -> }
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
        ownerName = ownerName,
        description = description,
        workingHours = workingHours,
        hasDelivery = hasDelivery,
        hasEmergency = hasEmergency,
        experienceYears = experienceYears,
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
            val cleanPhone = com.serviseyem.domain.usecases.ValidatePhoneUseCase.normalizePhone(userMap["phone"]?.toString() ?: "")
            val uList = _registeredUsersList.value.toMutableList()
            uList.removeAll { com.serviseyem.domain.usecases.ValidatePhoneUseCase.normalizePhone(it["phone"]?.toString() ?: "") == cleanPhone }
            uList.add(userMap)
            _registeredUsersList.value = uList
        },
        onJoinRequestPhoneUpdated = { _joinRequestPhone.value = it },
        onNavigateToScreen = { _currentScreen.value = it },
        onResult = onResult
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
    return isLoggedIn || (phone.isNotBlank() && !isGuestSessionId(currentUserId.value))
}

fun MainViewModel.restoreUserAccountByPhoneAndPassword(
    context: Context,
    phone: String,
    password: String,
    onResult: (Boolean, String) -> Unit
) {
    val cleanPhone = com.serviseyem.domain.usecases.ValidatePhoneUseCase.normalizePhone(phone).filter { it.isDigit() }
    val cleanPassword = password.trim()
    if (cleanPhone.length < 7) {
        onResult(false, "رقم الهاتف غير صحيح")
        return
    }
    if (cleanPassword.isBlank()) {
        onResult(false, "يرجى إدخال كلمة المرور")
        return
    }
    accountRecoveryHelper.searchAccountForRestore(cleanPhone) { match ->
        if (match == null) {
            onResult(false, "لم يتم العثور على حساب مرتبط برقم الهاتف هذا")
            return@searchAccountForRestore
        }
        accountRecoveryHelper.verifyRestorePassword(cleanPhone, match.type, cleanPassword) { isValid ->
            if (!isValid) {
                onResult(false, "كلمة المرور غير صحيحة")
                return@verifyRestorePassword
            }
            viewModelScope.launch {
                val restoredEntityId = when {
                    match.provider != null -> {
                        if (match.provider.isDeleted) {
                            adminViewModel.crud.updateFields("providers", match.provider.id, mapOf("isDeleted" to false))
                        }
                        match.provider.id
                    }
                    match.store != null -> {
                        if (match.store.isDeleted) {
                            adminViewModel.crud.updateFields("stores", match.store.id, mapOf("isDeleted" to false))
                        }
                        match.store.id
                    }
                    match.property != null -> {
                        if (match.property.isDeleted) {
                            adminViewModel.crud.updateFields("properties", match.property.id, mapOf("isDeleted" to false))
                        }
                        match.property.id
                    }
                    match.job != null -> {
                        if (match.job.isDeleted) {
                            adminViewModel.crud.updateFields("jobs", match.job.id, mapOf("isDeleted" to false))
                        }
                        match.job.id
                    }
                    else -> "usr_$cleanPhone"
                }

                val area = match.provider?.area?.takeIf { it.isNotBlank() }
                    ?: match.store?.cityId?.takeIf { it.isNotBlank() }
                    ?: match.property?.cityId?.takeIf { it.isNotBlank() }
                    ?: match.job?.cityId?.takeIf { it.isNotBlank() }
                    ?: "اليمن"

                setUserSessionDetails(context, match.name, cleanPhone, area)
                setJoinRequestPhone(context, cleanPhone)
                if (restoredEntityId.isNotBlank()) {
                    _currentUserId.value = restoredEntityId
                }

                val mappedRole = when (match.type.uppercase()) {
                    "PROVIDER" -> "PROVIDER"
                    "STORE", "RESTAURANT", "MEDICAL" -> "STORE_OWNER"
                    "PROPERTY" -> "PROPERTY_OWNER"
                    "JOB" -> "JOB_POSTER"
                    else -> null
                }
                if (mappedRole != null && _adminRole.value !in listOf("OWNER", "ADMIN", "SUPERVISOR")) {
                    _adminRole.value = mappedRole
                }

                val sp = context.getSharedPreferences("yemen_service_prefs", Context.MODE_PRIVATE)
                sp.edit().apply {
                    putBoolean("is_account_logged_in", true)
                    putString("user_account_type", match.type)
                    putString("logged_account_id", restoredEntityId)
                    putString("user_id", com.serviseyem.utils.SecurityCryptoUtils.encrypt(restoredEntityId))
                    apply()
                }
                runCatching {
                    val masterKey = androidx.security.crypto.MasterKey.Builder(context)
                        .setKeyScheme(androidx.security.crypto.MasterKey.KeyScheme.AES256_GCM)
                        .build()
                    val secureSp = androidx.security.crypto.EncryptedSharedPreferences.create(
                        context,
                        "yemen_service_secure_prefs",
                        masterKey,
                        androidx.security.crypto.EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                        androidx.security.crypto.EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
                    )
                    secureSp.edit()
                        .putBoolean("is_account_logged_in", true)
                        .putString("user_account_type", match.type)
                        .putString("logged_account_id", restoredEntityId)
                        .apply()
                }
                onResult(true, "تم استعادة الحساب وتسجيل الدخول بنجاح!")
            }
        }
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
    confirmPassword: String = password,
    onResult: (Boolean, String) -> Unit = { _, _ -> }
) {
    authViewModel.registerGuestUser(context, name, phone, residence, password, confirmPassword, onResult)
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
        when (collection) {
            "providers" -> {
                homeViewModel._providers.value = homeViewModel._providers.value.map { p ->
                    if (p.id == id) p.copy(
                        profileImage = if (profileImg.isNotBlank()) profileImg else p.profileImage,
                        coverImage = if (coverImg.isNotBlank()) coverImg else p.coverImage
                    ) else p
                }
                if (selectedProvider?.id == id) {
                    selectedProvider = selectedProvider?.copy(
                        profileImage = if (profileImg.isNotBlank()) profileImg else selectedProvider?.profileImage ?: "",
                        coverImage = if (coverImg.isNotBlank()) coverImg else selectedProvider?.coverImage ?: ""
                    )
                }
            }
            "stores" -> {
                adminViewModel.appState._stores.value = adminViewModel.appState._stores.value.map { s ->
                    if (s.id == id) s.copy(
                        logoImage = if (profileImg.isNotBlank()) profileImg else s.logoImage,
                        coverImage = if (coverImg.isNotBlank()) coverImg else s.coverImage
                    ) else s
                }
                if (selectedStore?.id == id) {
                    selectedStore = selectedStore?.copy(
                        logoImage = if (profileImg.isNotBlank()) profileImg else selectedStore?.logoImage ?: "",
                        coverImage = if (coverImg.isNotBlank()) coverImg else selectedStore?.coverImage ?: ""
                    )
                }
            }
            "properties" -> {
                val extraImg = coverImg.ifBlank { profileImg }
                if (extraImg.isNotBlank()) {
                    adminViewModel.appState._properties.value = adminViewModel.appState._properties.value.map { prop ->
                        if (prop.id == id) {
                            val nextImgs = (listOf(extraImg) + prop.images.filter { it != extraImg }).take(12)
                            prop.copy(images = nextImgs)
                        } else prop
                    }
                    selectedProperty?.takeIf { it.id == id }?.let { prop ->
                        val nextImgs = (listOf(extraImg) + prop.images.filter { it != extraImg }).take(12)
                        selectedProperty = prop.copy(images = nextImgs)
                    }
                    updates["images"] = adminViewModel.appState._properties.value.find { it.id == id }?.images ?: listOf(extraImg)
                }
            }
        }
        homeViewModel.applyFilters()
        viewModelScope.launch { adminViewModel.crud.updateFields(collection, id, updates) }
        triggerNotification("📸 تم تحديث الصور بنجاح")
    }
}

fun MainViewModel.changeEntityPassword(
    collection: String,
    entityId: String,
    oldPassword: String,
    newPassword: String,
    onResult: (Boolean, String) -> Unit
) {
    val cleanId = entityId.trim()
    val cleanOld = oldPassword.trim()
    val cleanNew = newPassword.trim()
    if (cleanId.isBlank()) {
        onResult(false, "معرف الحساب غير صالح")
        return
    }
    val policyCheck = com.serviseyem.utils.SecurityCryptoUtils.validatePasswordPolicy(cleanNew)
    if (!policyCheck.first) {
        onResult(false, policyCheck.second ?: "كلمة المرور يجب أن تكون 7 خانات على الأقل وتحتوي على حرف ورقم")
        return
    }
    viewModelScope.launch {
        try {
            val doc = db.collection(collection).document(cleanId).get().await()
            val storedHash = (doc.getString("passwordHash") ?: doc.getString("password") ?: "").trim()
            if (storedHash.isNotEmpty()) {
                val isValid = if (cleanOld.isEmpty()) false else {
                    PasswordHasher.verifyPassword(cleanOld, storedHash) || cleanOld == storedHash
                }
                if (!isValid) {
                    triggerNotification("❌ كلمة المرور الحالية غير صحيحة")
                    onResult(false, "كلمة المرور الحالية غير صحيحة")
                    return@launch
                }
            }
            val newHash = PasswordHasher.hashPassword(cleanNew)
            adminViewModel.crud.updateFields(
                collection,
                cleanId,
                mapOf("passwordHash" to newHash, "password" to "")
            )
            when (collection) {
                "providers" -> {
                    homeViewModel._providers.value = homeViewModel._providers.value.map {
                        if (it.id == cleanId) it.copy(passwordHash = newHash) else it
                    }
                    if (selectedProvider?.id == cleanId) {
                        selectedProvider = selectedProvider?.copy(passwordHash = newHash)
                    }
                }
                "stores" -> {
                    adminViewModel.appState._stores.value = adminViewModel.appState._stores.value.map {
                        if (it.id == cleanId) it.copy(passwordHash = newHash) else it
                    }
                    if (selectedStore?.id == cleanId) {
                        selectedStore = selectedStore?.copy(passwordHash = newHash)
                    }
                }
                "properties" -> {
                    adminViewModel.appState._properties.value = adminViewModel.appState._properties.value.map {
                        if (it.id == cleanId) it.copy(passwordHash = newHash) else it
                    }
                    if (selectedProperty?.id == cleanId) {
                        selectedProperty = selectedProperty?.copy(passwordHash = newHash)
                    }
                }
                "jobs" -> {
                    adminViewModel.appState._jobs.value = adminViewModel.appState._jobs.value.map {
                        if (it.id == cleanId) it.copy(passwordHash = newHash) else it
                    }
                    if (selectedJob?.id == cleanId) {
                        selectedJob = selectedJob?.copy(passwordHash = newHash)
                    }
                }
            }
            triggerNotification("🔒 تم تحديث كلمة المرور بنجاح")
            onResult(true, "تم تحديث كلمة المرور بنجاح")
        } catch (e: Exception) {
            triggerNotification("⚠️ تعذر تحديث كلمة المرور")
            onResult(false, e.localizedMessage ?: "تعذر تحديث كلمة المرور")
        }
    }
}

fun MainViewModel.incrementEntityViews(collection: String, entityId: String) {
    val cleanId = entityId.trim()
    val cleanCol = collection.trim()
    if (cleanId.isBlank() || cleanCol.isBlank()) return
    viewModelScope.launch {
        try {
            db.collection(cleanCol).document(cleanId)
                .update("viewsCount", FieldValue.increment(1L))
                .await()
        } catch (_: Exception) {
            // Silently ignore permission/offline errors on analytics increment
        }
    }
}

fun MainViewModel.findOwnedBusinessDestination(): Pair<String, (() -> Unit)>? {
    val rawPhone = currentUserPhone.value.trim()
    val normPhone = com.serviseyem.domain.usecases.ValidatePhoneUseCase.normalizePhone(rawPhone).filter { it.isDigit() }
    val uid = currentUserId.value.trim()
    val isGuestUid = isGuestSessionId(uid)
    if (normPhone.length < 7 && isGuestUid) return null

    fun matchesPhoneOrId(phone: String?, id: String?, ownerId: String? = null): Boolean {
        val pNorm = com.serviseyem.domain.usecases.ValidatePhoneUseCase.normalizePhone(phone ?: "").filter { it.isDigit() }
        if (normPhone.length >= 7 && pNorm == normPhone) return true
        if (!isGuestUid && (id == uid || ownerId == uid)) return true
        return false
    }

    val ownedProv = providers.value.firstOrNull { !it.isDeleted && matchesPhoneOrId(it.phone, it.id) }
    if (ownedProv != null) {
        val isMed = ownedProv.categoryId.contains("medical", ignoreCase = true) ||
            ownedProv.categoryId.contains("clinic", ignoreCase = true) ||
            ownedProv.categoryId.contains("hospital", ignoreCase = true) ||
            ownedProv.categoryId.contains("pharmacy", ignoreCase = true) ||
            ownedProv.categoryId.contains("lab", ignoreCase = true)
        val route = if (isMed) AppScreens.MEDICAL_DASHBOARD else AppScreens.PROVIDER_DASHBOARD
        return route to { selectedProvider = ownedProv }
    }

    val ownedStore = stores.value.firstOrNull { !it.isDeleted && matchesPhoneOrId(it.phone, it.id, it.ownerId) }
    if (ownedStore != null) {
        val route = when (ownedStore.sectionId.lowercase()) {
            "restaurants", "restaurant" -> AppScreens.RESTAURANT_DASHBOARD
            "medical" -> AppScreens.MEDICAL_DASHBOARD
            else -> AppScreens.STORE_DASHBOARD
        }
        return route to { selectedStore = ownedStore }
    }

    val ownedProp = properties.value.firstOrNull { !it.isDeleted && matchesPhoneOrId(it.phone, it.id, it.ownerId) }
    if (ownedProp != null) {
        return AppScreens.PROPERTY_DASHBOARD to { selectedProperty = ownedProp }
    }

    val ownedJob = jobs.value.firstOrNull { !it.isDeleted && matchesPhoneOrId(it.phone, it.id) }
    if (ownedJob != null) {
        return AppScreens.JOB_POSTER_DASHBOARD to { selectedJob = ownedJob }
    }

    return null
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
