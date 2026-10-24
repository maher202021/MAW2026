package com.serviseyem.ui.screens.urgent

import androidx.compose.foundation.layout.*
import com.serviseyem.ui.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.serviseyem.data.models.InstantRequestEntity
import com.serviseyem.ui.screens.urgent.components.UrgentCard
import com.serviseyem.utils.VisualThemePalette

/**
 * ⚡ UrgentListContent
 * Renders the LazyColumn list of urgent request cards with keys, status badges, and empty list indicators.
 */
@Composable
fun UrgentListContent(
    requests: List<InstantRequestEntity>,
    isProvider: Boolean,
    themeColors: VisualThemePalette,
    onNavigateToDetails: (requestId: String) -> Unit,
    onNavigateToSubmitUrgentOffer: (requestId: String) -> Unit,
    modifier: Modifier = Modifier,
    now: Long = System.currentTimeMillis()
) {
    if (requests.isEmpty()) {
        Box(
            modifier = modifier
                .fillMaxSize()
                .padding(32.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "⚡ لا توجد طلبات عاجلة مطابقة حالياً",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.DarkGray
                )
                Text(
                    text = "سيتم تحديث القائمة تلقائياً عند وصول طلبات عاجلة في منطقتك خلال 30 دقيقة.",
                    fontSize = 12.sp,
                    color = Color.Gray
                )
            }
        }
    } else {
        LazyColumn(
            modifier = modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(requests, key = { it.id }) { req ->
                UrgentCard(
                    request = req,
                    isProvider = isProvider,
                    themeColors = themeColors,
                    onNavigateToDetails = { onNavigateToDetails(req.id) },
                    onNavigateToSubmitOffer = { onNavigateToSubmitUrgentOffer(req.id) },
                    now = now
                )
            }
        }
    }
}
