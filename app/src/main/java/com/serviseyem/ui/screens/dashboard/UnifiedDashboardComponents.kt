package com.serviseyem.ui.screens.dashboard

import androidx.compose.runtime.Composable
import com.serviseyem.ui.*
import com.serviseyem.data.UnifiedBusinessAccount
import com.serviseyem.ui.MainViewModel
import com.serviseyem.ui.screens.dashboard.components.*
import com.serviseyem.utils.VisualThemePalette

@Composable
fun UnifiedProfileSectionWrapper(
    account: UnifiedBusinessAccount,
    themeColors: VisualThemePalette
) {
    com.serviseyem.ui.screens.dashboard.components.UnifiedProfileSection(
        title = account.name,
        subtitle = account.description,
        phone = account.phone,
        cityArea = account.neighborhood.ifBlank { account.cityId },
        photoUrl = account.logoImage,
        coverUrl = account.coverImage,
        rating = account.rating.toDouble(),
        reviewCount = account.numReviews,
        isAvailable = true,
        themeColors = themeColors
    )
}
