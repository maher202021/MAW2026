package com.serviseyem.ui.screens.dashboard.components

import androidx.compose.foundation.background
import com.serviseyem.ui.*
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.serviseyem.ui.components.SmartAsyncImage
import com.serviseyem.utils.VisualThemePalette

@Composable
fun UnifiedProductsServicesSection(
    title: String,
    description: String = "",
    price: String = "",
    imageUrl: String = "",
    isAvailable: Boolean = true,
    themeColors: VisualThemePalette,
    onEditClick: (() -> Unit)? = null,
    onDeleteClick: (() -> Unit)? = null,
    onToggleAvailability: ((Boolean) -> Unit)? = null
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = themeColors.surface),
        shape = RoundedCornerShape(12.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.08f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(60.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color.Black.copy(alpha = 0.2f))
                    .border(1.dp, themeColors.accent.copy(alpha = 0.3f), RoundedCornerShape(8.dp)),
                contentAlignment = Alignment.Center
            ) {
                if (imageUrl.isNotBlank()) {
                    SmartAsyncImage(
                        model = imageUrl,
                        contentDescription = title,
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    Text(text = "📦", fontSize = 24.sp)
                }
            }

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                Text(
                    text = title,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = themeColors.textPrimary
                )
                if (description.isNotBlank()) {
                    Text(
                        text = description,
                        fontSize = 11.sp,
                        color = themeColors.textSecondary,
                        maxLines = 2
                    )
                }
                if (price.isNotBlank()) {
                    Text(
                        text = "$price ريال يمني",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = themeColors.accent
                    )
                }
            }

            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                if (onEditClick != null) {
                    IconButton(onClick = onEditClick) {
                        Text(text = "✏️", fontSize = 14.sp)
                    }
                }
                if (onDeleteClick != null) {
                    IconButton(onClick = onDeleteClick) {
                        Text(text = "🗑️", fontSize = 14.sp)
                    }
                }
            }
        }
    }
}
