package com.serviseyem.ui.components

import androidx.compose.runtime.*
import com.serviseyem.data.PropertyEntity
import com.serviseyem.ui.MainViewModel
import com.serviseyem.utils.VisualThemePalette

@Composable
fun PropertyListItemCard(
    property: PropertyEntity,
    themeColors: VisualThemePalette,
    onClick: () -> Unit,
    viewModel: MainViewModel,
    onChatClick: (() -> Unit)? = null
) {
    com.serviseyem.PropertyListItemCard(
        prop = property,
        themeColors = themeColors,
        onClick = onClick,
        viewModel = viewModel
    )
}
