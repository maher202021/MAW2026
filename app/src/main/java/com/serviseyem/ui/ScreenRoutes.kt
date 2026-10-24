package com.serviseyem.ui

object ScreenRoutes {
    fun isFullScreen(currentScreen: String): Boolean {
        return currentScreen == AppScreens.MAP_VIEW || currentScreen.endsWith("_DETAILS")
    }

    fun showBottomBar(currentScreen: String, adminRole: String = "GUEST"): Boolean {
        return currentScreen != AppScreens.CHAT_DIRECT && currentScreen != AppScreens.MAP_VIEW && currentScreen != AppScreens.SMART_ASSISTANT
    }

    fun showTopBar(currentScreen: String, adminRole: String = "GUEST"): Boolean {
        return currentScreen != AppScreens.CHAT_DIRECT && 
               currentScreen != AppScreens.MAP_VIEW && 
               currentScreen != AppScreens.SMART_ASSISTANT &&
               !currentScreen.endsWith("_DETAILS") &&
               currentScreen != AppScreens.DYNAMIC_PROFILE
    }

    fun isRegistrationForm(screen: String): Boolean =
        screen in setOf("REGISTER_FORM", "JOIN_REQUEST_STATUS", "LOGIN", "REGISTER")

    fun isFormOpen(dialogAction: String?): Boolean = dialogAction != null

    fun isRegistrationOrFormOpen(
        currentScreen: String,
        showGuestRegisterDialogForAction: String? = null,
        showAssistantDialog: Boolean = false,
        showRequestServiceModal: Boolean = false
    ): Boolean {
        return currentScreen in setOf(
            "REGISTER_FORM", "JOIN_REQUEST_STATUS", "LOGIN", 
            "PROVIDER_REGISTRATION", "STORE_CREATION", "PROPERTY_CREATION", 
            "JOB_CREATION", "CREATE_BOOKING", "REGISTER",
            "MAP", "MAP_VIEW", "ADMIN_PANEL", "ADMIN_LOGIN", "OWNER_PANEL",
            "CHAT_LIST", "CHAT_DIRECT", "CHAT",
            AppScreens.SMART_ASSISTANT, AppScreens.QUICK_SERVICE_REQUEST
        ) || showGuestRegisterDialogForAction != null || 
          showAssistantDialog || showRequestServiceModal
    }
}
