package com.serviseyem.ui.screens.dashboard.components

import androidx.compose.foundation.layout.*
import com.serviseyem.ui.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.serviseyem.utils.VisualThemePalette

@Composable
fun UnifiedEmptyState(
    title: String,
    description: String? = null,
    iconText: String = "📦",
    actionLabel: String? = null,
    onActionClick: (() -> Unit)? = null,
    themeColors: VisualThemePalette
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp),
        colors = CardDefaults.cardColors(containerColor = themeColors.surface),
        shape = RoundedCornerShape(16.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.08f))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(text = iconText, fontSize = 42.sp)
            Text(
                text = title,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = themeColors.textPrimary,
                textAlign = TextAlign.Center,
                lineHeight = 22.sp,
                modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp)
            )
            if (!description.isNullOrBlank()) {
                Text(
                    text = description,
                    fontSize = 12.5.sp,
                    color = themeColors.textSecondary,
                    textAlign = TextAlign.Center,
                    lineHeight = 18.sp,
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp)
                )
            }
            if (!actionLabel.isNullOrBlank() && onActionClick != null) {
                Spacer(modifier = Modifier.height(4.dp))
                Button(
                    onClick = onActionClick,
                    colors = ButtonDefaults.buttonColors(containerColor = themeColors.accent),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text(
                        text = actionLabel,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black
                    )
                }
            }
        }
    }
}
