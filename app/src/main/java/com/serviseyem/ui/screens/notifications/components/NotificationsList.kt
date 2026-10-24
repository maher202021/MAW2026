package com.serviseyem.ui.screens.notifications.components

import androidx.compose.foundation.layout.*
import com.serviseyem.ui.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.serviseyem.data.NotificationEntity
import com.serviseyem.utils.VisualThemePalette

/**
 * 📜 NotificationsList
 * High-performance LazyColumn for displaying notifications with keys and callbacks.
 */
@Composable
fun NotificationsList(
    notifications: List<NotificationEntity>,
    readIds: Set<String>,
    onNotificationClick: (NotificationEntity) -> Unit,
    onDeleteNotification: (NotificationEntity) -> Unit,
    themeColors: VisualThemePalette,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(
            items = notifications,
            key = { it.id.ifBlank { "${it.timestamp}_${it.title}" } }
        ) { notification ->
            val isUnread = !readIds.contains(notification.id)
            NotificationItemCard(
                notification = notification,
                isUnread = isUnread,
                onCardClick = { onNotificationClick(notification) },
                onDeleteClick = { onDeleteNotification(notification) },
                themeColors = themeColors
            )
        }
    }
}
