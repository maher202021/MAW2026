package com.serviseyem.ui.screens.urgent

import androidx.compose.foundation.background
import com.serviseyem.ui.*
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.serviseyem.utils.VisualThemePalette

/**
 * 🔍 UrgentListFilterBar
 * Filter & search bar for urgent requests list (less than 10 mins remaining filter, search query).
 */
@Composable
fun UrgentListFilterBar(
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    onlyUnder10MinFilter: Boolean,
    onToggleUnder10MinFilter: (Boolean) -> Unit,
    themeColors: VisualThemePalette,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        OutlinedTextField(
            value = searchQuery,
            onValueChange = onSearchQueryChange,
            placeholder = { Text("بحث برقم الطلب، المدينة، أو نوع الخدمة...") },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = Color.Gray) },
            singleLine = true,
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.fillMaxWidth()
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(if (onlyUnder10MinFilter) Color(0xFFEF4444) else Color(0xFFF1F5F9))
                    .border(
                        1.dp,
                        if (onlyUnder10MinFilter) Color(0xFFDC2626) else Color(0xFFCBD5E1),
                        RoundedCornerShape(8.dp)
                    )
                    .clickable { onToggleUnder10MinFilter(!onlyUnder10MinFilter) }
                    .padding(horizontal = 10.dp, vertical = 6.dp)
            ) {
                Text(
                    text = "🚨 خروج عن الوقت الحرج (أقل من 10 دقائق)",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (onlyUnder10MinFilter) Color.White else Color(0xFF334155)
                )
            }
        }
    }
}
