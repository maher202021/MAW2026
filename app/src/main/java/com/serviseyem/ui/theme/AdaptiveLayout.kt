package com.serviseyem.ui.theme

import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

enum class AppWidthClass {
    Compact,
    Medium,
    Expanded
}

object AppLayoutMetrics {
    val MaxContainerWidth: Dp = 840.dp
    val BottomSafeScrollPadding: Dp = 110.dp
    val MinTouchTarget: Dp = 48.dp
    val MinButtonHeight: Dp = 44.dp
}

fun widthClassFor(maxWidth: Dp): AppWidthClass = when {
    maxWidth < 600.dp -> AppWidthClass.Compact
    maxWidth < 840.dp -> AppWidthClass.Medium
    else -> AppWidthClass.Expanded
}

fun computeAdaptiveColumns(maxWidth: Dp): Int = when {
    maxWidth < 600.dp -> 1
    maxWidth < 840.dp -> 2
    else -> 3
}

@Composable
fun <T> AdaptiveCardGrid(
    items: List<T>,
    modifier: Modifier = Modifier,
    horizontalSpacing: Dp = 10.dp,
    verticalSpacing: Dp = 10.dp,
    itemContent: @Composable (T) -> Unit
) {
    BoxWithConstraints(modifier = modifier.fillMaxWidth()) {
        val columns = computeAdaptiveColumns(maxWidth)
        val rows = remember(items, columns) { items.chunked(columns) }
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(verticalSpacing)
        ) {
            rows.forEach { rowItems ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(horizontalSpacing)
                ) {
                    rowItems.forEach { entry ->
                        Box(modifier = Modifier.weight(1f)) {
                            itemContent(entry)
                        }
                    }
                    if (rowItems.size < columns) {
                        repeat(columns - rowItems.size) {
                            Spacer(modifier = Modifier.weight(1f))
                        }
                    }
                }
            }
        }
    }
}
