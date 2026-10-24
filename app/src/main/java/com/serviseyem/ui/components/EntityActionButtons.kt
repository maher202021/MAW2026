package com.serviseyem.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.serviseyem.utils.VisualThemePalette

/**
 * ✨ مرحلة 2: زر إجراءات موحد للكيانات
 * يستبدل الكود المكرر في AdminRequestsPanel, AdminStoresPropertiesPanel, ...
 */
@Composable
fun EntityActionButtons(
    entityId: String,
    isVip: Boolean = false,
    isVerified: Boolean = false,
    isBlocked: Boolean = false,
    onToggleVip: (() -> Unit)? = null,
    onToggleVerified: (() -> Unit)? = null,
    onToggleBlocked: (() -> Unit)? = null,
    onDelete: (() -> Unit)? = null,
    onCall: (() -> Unit)? = null,
    phone: String = "",
    themeColors: VisualThemePalette,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        // صف الشارات
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (onToggleVip != null) {
                Checkbox(
                    checked = isVip,
                    onCheckedChange = { onToggleVip() },
                    colors = CheckboxDefaults.colors(checkedColor = Color(0xFFF59E0B)),
                    modifier = Modifier.size(32.dp)
                )
                Text(
                    "VIP ⭐",
                    fontSize = 10.sp,
                    color = Color.White,
                    modifier = Modifier.clickable { onToggleVip() }
                )
                Spacer(Modifier.width(4.dp))
            }
            
            if (onToggleVerified != null) {
                Checkbox(
                    checked = isVerified,
                    onCheckedChange = { onToggleVerified() },
                    colors = CheckboxDefaults.colors(checkedColor = Color(0xFF3B82F6)),
                    modifier = Modifier.size(32.dp)
                )
                Text(
                    "موثق ✅",
                    fontSize = 10.sp,
                    color = Color.White,
                    modifier = Modifier.clickable { onToggleVerified() }
                )
            }
        }
        
        // صف الأزرار
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (onToggleBlocked != null) {
                OutlinedButton(
                    onClick = onToggleBlocked,
                    shape = androidx.compose.foundation.shape.RoundedCornerShape(8.dp),
                    border = androidx.compose.foundation.BorderStroke(
                        1.dp,
                        if (isBlocked) Color(0xFF10B981) else Color(0xFFEF4444)
                    ),
                    modifier = Modifier.weight(1f),
                    contentPadding = PaddingValues(vertical = 4.dp)
                ) {
                    Text(
                        if (isBlocked) "فك الحظر" else "حظر",
                        fontSize = 10.sp,
                        color = if (isBlocked) Color(0xFF10B981) else Color(0xFFEF4444)
                    )
                }
            }
            
            if (onDelete != null) {
                IconButton(
                    onClick = onDelete,
                    modifier = Modifier
                        .size(32.dp)
                        .background(Color(0xFFEF5350).copy(alpha = 0.15f))
                ) {
                    Icon(Icons.Default.Delete, "حذف", tint = Color(0xFFEF5350), modifier = Modifier.size(16.dp))
                }
            }
        }
    }
}
