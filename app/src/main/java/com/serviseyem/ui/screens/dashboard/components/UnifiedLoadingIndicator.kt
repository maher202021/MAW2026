package com.serviseyem.ui.screens.dashboard.components

import androidx.compose.foundation.layout.*
import com.serviseyem.ui.*
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.serviseyem.utils.VisualThemePalette

@Composable
fun UnifiedLoadingIndicator(
    message: String = "جاري تحميل البيانات...",
    themeColors: VisualThemePalette,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(220.dp)
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            CircularProgressIndicator(
                color = themeColors.accent,
                strokeWidth = 3.dp
            )
            Text(
                text = message,
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium,
                color = themeColors.textSecondary
            )
        }
    }
}
