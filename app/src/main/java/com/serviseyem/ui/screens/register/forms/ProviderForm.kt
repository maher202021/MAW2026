package com.serviseyem.ui.screens.register.forms

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.serviseyem.data.models.RegistrationFormsConfig
import com.serviseyem.utils.VisualThemePalette

/**
 * ProviderForm - استمارة تسجيل الفني ومقدم الخدمة
 */
@Composable
fun ProviderForm(
    themeColors: VisualThemePalette,
    onSubmit: (Map<String, Any>) -> Unit,
    modifier: Modifier = Modifier,
    formsConfig: RegistrationFormsConfig? = null,
    isSubmittingExternal: Boolean? = null,
    externalError: String? = null,
    onClearExternalError: () -> Unit = {}
) {
    UnifiedRegistrationForm(
        role = "PROVIDER",
        themeColors = themeColors,
        modifier = modifier,
        formsConfig = formsConfig,
        isSubmittingExternal = isSubmittingExternal,
        externalError = externalError,
        onClearExternalError = onClearExternalError,
        onRegistrationSuccess = onSubmit
    )
}
