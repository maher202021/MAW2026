package com.serviseyem.utils

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

fun getStarsString(r: Float): String {
    val filled = r.toInt().coerceIn(0, 5)
    val empty = (5 - filled).coerceIn(0, 5)
    return "★".repeat(filled) + "☆".repeat(empty)
}

@Composable
fun OptionCheckboxCard(
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    themeColors: VisualThemePalette
) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = if (checked) themeColors.accent.copy(alpha = 0.15f) else Color(0xFF0F172A)
        ),
        border = BorderStroke(
            1.dp,
            if (checked) themeColors.accent else Color.Gray.copy(alpha = 0.3f)
        ),
        shape = RoundedCornerShape(8.dp),
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onCheckedChange(!checked) }
    ) {
        Row(
            modifier = Modifier
                .padding(10.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (checked) themeColors.accent else Color.White
                )
                Text(
                    text = subtitle,
                    fontSize = 9.sp,
                    color = themeColors.textSecondary
                )
            }
            Checkbox(
                checked = checked,
                onCheckedChange = onCheckedChange,
                colors = CheckboxDefaults.colors(
                    checkedColor = themeColors.accent,
                    uncheckedColor = Color.Gray
                )
            )
        }
    }
}
