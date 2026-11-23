package com.serviseyem.ui.screens.register.forms

import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import com.serviseyem.ui.MainViewModel
import com.serviseyem.ui.helpers.AppPreferenceHelper
import com.serviseyem.ui.submitJoinForm
import com.serviseyem.utils.VisualThemePalette

/**
 * JobSeekerForm - استمارة المتقدم للوظائف (باحث عن عمل)
 */
@Composable
fun JobSeekerForm(
    viewModel: MainViewModel,
    themeColors: VisualThemePalette,
    onSuccess: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val formsConfig by viewModel.registrationFormsConfig.collectAsState()
    var isSubmitting by remember { mutableStateOf(false) }
    var submissionError by remember { mutableStateOf<String?>(null) }

    UnifiedRegistrationForm(
        role = "JOB_SEEKER",
        themeColors = themeColors,
        modifier = modifier,
        formsConfig = formsConfig,
        isSubmittingExternal = isSubmitting,
        externalError = submissionError,
        onClearExternalError = { submissionError = null },
        onRegistrationSuccess = { data ->
            val fullName = (data["fullName"] ?: "").trim()
            val cleanPhone = AppPreferenceHelper.normalizePhoneNumber(data["phone"] ?: "")
            val password = data["password"] ?: ""
            val city = (data["city"] ?: "صنعاء").ifBlank { "صنعاء" }
            val address = (data["address"] ?: "").trim()
            val craftType = (data["specialization"] ?: data["craftType"] ?: "").trim().ifBlank { "باحث عن عمل" }
            val description = (data["description"] ?: "").trim()
            val experienceYears = (data["experienceYears"] ?: "").trim()

            isSubmitting = true
            submissionError = null

            viewModel.submitJoinForm(
                context = context,
                name = fullName,
                phone = cleanPhone,
                catId = "JOB_SEEKER",
                area = city,
                neighborhood = address,
                photoPath = data["imageUri"] ?: "",
                idCardPath = data["idCardUri"] ?: "",
                gpsCoords = (data["gpsCoords"] ?: "").trim(),
                customCategoryName = craftType,
                password = password,
                ownerName = fullName,
                description = description,
                experienceYears = experienceYears,
                onResult = { ok, msg ->
                    isSubmitting = false
                    if (ok) {
                        RegistrationDraftManager(context).clearDraft("JOB_SEEKER")
                        onSuccess()
                    } else {
                        submissionError = msg
                    }
                }
            )
        }
    )
}
