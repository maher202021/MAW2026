package com.serviseyem.ui.screens.urgent.components

import androidx.compose.foundation.BorderStroke
import com.serviseyem.ui.*
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.serviseyem.data.models.InstantRequestEntity
import com.serviseyem.ui.components.UrgentTimerComponent
import com.serviseyem.utils.VisualThemePalette

/**
 * 🎴 UrgentCard
 * بطاقة عرض الطلب العاجل القابلة لإعادة الاستخدام.
 */
@Composable
fun UrgentCard(
    request: InstantRequestEntity,
    isProvider: Boolean,
    themeColors: VisualThemePalette,
    onNavigateToDetails: (requestId: String) -> Unit,
    onNavigateToSubmitOffer: (requestId: String) -> Unit = {},
    modifier: Modifier = Modifier,
    now: Long = System.currentTimeMillis()
) {
    val remainingMinutes = if (request.expiresAt <= 0) 30
        else (((request.expiresAt - now) / 1000) / 60).coerceAtLeast(0)
    val isCritical = remainingMinutes < 5
    val isUrgent = remainingMinutes < 10

    val cardBgColor = when {
        request.status != "WAITING_FOR_OFFERS" -> themeColors.surface
        isCritical -> Color(0xFFFFEBEE)
        isUrgent -> Color(0xFFFFF8E1)
        else -> themeColors.surface
    }

    val cardBorderColor = when {
        request.status != "WAITING_FOR_OFFERS" -> themeColors.border
        isCritical -> Color(0xFFE53935)
        isUrgent -> Color(0xFFFFA000)
        else -> Color(0xFFEF9A9A)
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onNavigateToDetails(request.id) }
            .testTag("urgent_card_${request.id}"),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = cardBgColor),
        border = BorderStroke(1.5.dp, cardBorderColor)
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(request.requestCode, fontWeight = FontWeight.Black, fontSize = 15.sp, color = Color(0xFFD32F2F))
                    UrgentStatusBadge(status = request.status)
                }
                UrgentTimerComponent(
                    expiresAt = request.expiresAt,
                    totalDurationMillis = 30 * 60 * 1000L,
                    isCompact = true
                )
            }

            Text(request.serviceTitle, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = themeColors.textPrimary)
            Text(request.description, style = MaterialTheme.typography.bodySmall, maxLines = 2, color = themeColors.textSecondary)

            HorizontalDivider(color = themeColors.border.copy(alpha = 0.5f))

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    Icon(Icons.Default.LocationOn, contentDescription = null, modifier = Modifier.size(14.dp), tint = Color(0xFFD32F2F))
                    Text("${request.userCity} - ${request.userNeighborhood}", fontSize = 12.sp, color = themeColors.textSecondary)
                }
                if (!isProvider) {
                    Text("${request.offersCount} عروض مستلمة", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFF2E7D32))
                }
            }

            UrgentActionButtons(
                isProvider = isProvider,
                status = request.status,
                themeColors = themeColors,
                onViewDetails = { onNavigateToDetails(request.id) },
                onSubmitOffer = { onNavigateToSubmitOffer(request.id) }
            )
        }
    }
}
