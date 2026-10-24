package com.serviseyem.ui

import androidx.compose.runtime.Composable
import com.serviseyem.ui.*
import com.serviseyem.data.ProviderEntity
import com.serviseyem.ui.components.ProviderCard as ComponentProviderCard
import com.serviseyem.utils.VisualThemePalette

@Composable
fun ProviderCard(
    provider: ProviderEntity,
    themeColors: VisualThemePalette,
    viewModel: MainViewModel,
    onChatOpen: (String) -> Unit
) {
    ComponentProviderCard(
        provider = provider,
        themeColors = themeColors,
        viewModel = viewModel,
        onChatOpen = onChatOpen
    )
}
