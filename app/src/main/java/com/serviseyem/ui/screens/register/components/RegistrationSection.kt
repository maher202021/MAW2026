package com.serviseyem.ui.screens.register.components

import androidx.compose.foundation.BorderStroke
import com.serviseyem.ui.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.serviseyem.utils.VisualThemePalette

/**
 * 📦 RegistrationSection - قسم موحد لعرض الحقول المنظمة في بطاقة
 */
@Composable
fun RegistrationSection(
    title: String,
    subtitle: String? = null,
    icon: String? = null,
    themeColors: VisualThemePalette,
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.12f)),
        shape = RoundedCornerShape(14.dp),
        modifier = modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(modifier = Modifier.fillMaxWidth()) {
                if (icon != null) {
                    Text(icon, fontSize = 16.sp, modifier = Modifier.padding(end = 6.dp))
                }
                Column {
                    Text(
                        text = title,
                        fontSize = 13.5.sp,
                        fontWeight = FontWeight.Bold,
                        color = themeColors.accent
                    )
                    if (!subtitle.isNullOrBlank()) {
                        Text(
                            text = subtitle,
                            fontSize = 11.sp,
                            color = Color.Gray
                        )
                    }
                }
            }
            HorizontalDivider(color = Color.White.copy(alpha = 0.08f), thickness = 1.dp)
            content()
        }
    }
}
