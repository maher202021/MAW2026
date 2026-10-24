package com.serviseyem.ui.screens.dashboard

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.serviseyem.ui.screens.dashboard.components.UnifiedProfileSection
import com.serviseyem.ui.screens.dashboard.components.UnifiedSettingsSection
import com.serviseyem.utils.VisualThemePalette

@Composable
fun TabProfileEdit(
    name: String,
    phone: String,
    cityArea: String,
    description: String,
    workingHours: String = "",
    photoUrl: String = "",
    coverUrl: String = "",
    isAvailable: Boolean = true,
    rating: Double = 5.0,
    reviewCount: Int = 0,
    themeColors: VisualThemePalette,
    onChangePhoto: ((String) -> Unit)? = null,
    onChangeCover: ((String) -> Unit)? = null,
    onSaveProfile: (name: String, phone: String, cityArea: String, description: String, workingHours: String, isAvailable: Boolean) -> Unit,
    onChangePassword: (oldPass: String, newPass: String) -> Unit
) {
    var currentPhoto by remember(photoUrl) { mutableStateOf(photoUrl) }
    var currentCover by remember(coverUrl) { mutableStateOf(coverUrl) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        UnifiedProfileSection(
            title = name,
            subtitle = description,
            phone = phone,
            cityArea = cityArea,
            photoUrl = currentPhoto,
            coverUrl = currentCover,
            rating = rating,
            reviewCount = reviewCount,
            isAvailable = isAvailable,
            themeColors = themeColors,
            onChangePhoto = { newUrl ->
                currentPhoto = newUrl
                onChangePhoto?.invoke(newUrl)
            },
            onChangeCover = { newUrl ->
                currentCover = newUrl
                onChangeCover?.invoke(newUrl)
            }
        )

        UnifiedSettingsSection(
            name = name,
            phone = phone,
            cityArea = cityArea,
            description = description,
            workingHours = workingHours,
            isAvailable = isAvailable,
            themeColors = themeColors,
            onSaveProfile = onSaveProfile,
            onChangePassword = onChangePassword
        )
    }
}
