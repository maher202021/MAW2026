package com.serviseyem.ui.components

import androidx.compose.runtime.*
import com.serviseyem.data.StoreEntity
import com.serviseyem.ui.MainViewModel
import com.serviseyem.utils.VisualThemePalette

@Composable
fun StoreListItemCard(
    store: StoreEntity,
    themeColors: VisualThemePalette,
    onClick: () -> Unit,
    viewModel: MainViewModel,
    onChatClick: (() -> Unit)? = null
) {
    com.serviseyem.StoreListItemCard(
        store = store,
        themeColors = themeColors,
        onClick = onClick,
        viewModel = viewModel,
        onChatClick = onChatClick
    )
}
