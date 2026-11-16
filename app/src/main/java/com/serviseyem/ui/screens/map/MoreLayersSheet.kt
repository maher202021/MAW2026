package com.serviseyem.ui.screens.map

import androidx.compose.runtime.Composable
import com.serviseyem.utils.VisualThemePalette

@Composable
fun MoreLayersSheet(
    state: MapScreenState,
    onDismiss: () -> Unit,
    themeColors: VisualThemePalette
) {
    com.serviseyem.ui.screens.map.components.MoreLayersSheet(
        state = state,
        onDismiss = onDismiss,
        themeColors = themeColors
    )
}
