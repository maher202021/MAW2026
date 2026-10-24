package com.serviseyem.domain.usecases

import android.content.Context
import com.serviseyem.ui.helpers.AccountRecoveryHelper
import javax.inject.Inject

class RequestPasswordResetUseCase @Inject constructor() {

    fun execute(
        recoveryHelper: AccountRecoveryHelper,
        context: Context,
        phone: String,
        name: String,
        accountType: String,
        onPasswordWaitingPhoneSet: (String) -> Unit,
        triggerNotification: (String) -> Unit,
        onResult: (Boolean) -> Unit
    ) {
        val cleanPhone = ValidatePhoneUseCase.normalizePhone(phone)
        if (cleanPhone.isBlank() || cleanPhone.contains("/")) {
            onResult(false)
            return
        }
        recoveryHelper.requestPasswordReset(
            context = context,
            phone = cleanPhone,
            name = name.trim(),
            accountType = accountType.trim().ifBlank { "CLIENT" },
            onPasswordWaitingPhoneSet = onPasswordWaitingPhoneSet,
            triggerNotification = triggerNotification,
            onResult = onResult
        )
    }
}
