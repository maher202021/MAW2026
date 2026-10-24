package com.serviseyem.ui.screens.register.forms

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.serviseyem.utils.VisualThemePalette

/**
 * 🛠️ ProviderForm - استمارة تسجيل الفني ومقدم الخدمة
 * تضمن الحقول الـ 11 المحددة بدقة (الاسم الثلاثي، الهاتف، كلمة المرور، التأكيد، المهنة، سنوات الخبرة، المدينة، الحي، نبذة، صورة شخصية، صورة البطاقة)
 */
@Composable
fun ProviderForm(
    themeColors: VisualThemePalette,
    onSubmit: (Map<String, Any>) -> Unit,
    modifier: Modifier = Modifier
) {
    UnifiedRegistrationForm(
        role = "PROVIDER",
        themeColors = themeColors,
        onRegistrationSuccess = onSubmit
    )
}
