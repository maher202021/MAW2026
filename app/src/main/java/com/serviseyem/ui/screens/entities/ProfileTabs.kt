package com.serviseyem.ui.screens.entities

import androidx.compose.foundation.shape.RoundedCornerShape
import com.serviseyem.ui.*
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.serviseyem.utils.VisualThemePalette

@Composable
fun ProfileTabs(
    selectedTab: Int,
    onTabSelected: (Int) -> Unit,
    entityType: ProfileEntityType,
    themeColors: VisualThemePalette
) {
    val tabTitles = when (entityType) {
        ProfileEntityType.TECHNICIAN -> listOf("سابقة الأعمال 📸", "التقييمات والآراء ⭐", "شروط الضمان 🛡️")
        ProfileEntityType.STORE -> listOf("قائمة المنتجات", "التقييمات", "معلومات المتجر")
        ProfileEntityType.RESTAURANT -> listOf("قائمة المنيو", "التقييمات", "معلومات المطعم")
        ProfileEntityType.MEDICAL -> listOf("العيادات والخدمات", "التقييمات", "معلومات المركز")
        ProfileEntityType.REAL_ESTATE -> listOf("الوحدات والمخطط", "المواصفات", "معاينة وخريطة")
        ProfileEntityType.JOB -> listOf("شروط الوظيفة", "المزايا والحوافز", "عن جهة العمل")
        ProfileEntityType.GENERAL -> listOf("الخدمات", "التقييمات", "معلومات التواصل")
    }

    TabRow(
        selectedTabIndex = selectedTab,
        containerColor = themeColors.surface,
        contentColor = themeColors.accent,
        modifier = Modifier.clip(RoundedCornerShape(12.dp))
    ) {
        tabTitles.forEachIndexed { index, title ->
            Tab(
                selected = selectedTab == index,
                onClick = { onTabSelected(index) },
                text = {
                    Text(
                        title,
                        fontSize = 11.sp,
                        fontWeight = if (selectedTab == index) FontWeight.Bold else FontWeight.Normal,
                        color = if (selectedTab == index) themeColors.accent else Color.LightGray
                    )
                }
            )
        }
    }
}
