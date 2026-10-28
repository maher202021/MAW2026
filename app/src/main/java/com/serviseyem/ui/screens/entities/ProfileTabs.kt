package com.serviseyem.ui.screens.entities

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.serviseyem.ui.components.BespokePremiumSystem
import com.serviseyem.utils.VisualThemePalette

@Composable
fun ProfileTabs(
    selectedTab: Int,
    onTabSelected: (Int) -> Unit,
    entityType: ProfileEntityType,
    themeColors: VisualThemePalette
) {
    val tabTitles = when (entityType) {
        ProfileEntityType.STORE -> listOf("الرئيسية", "المنتجات", "الكتالوج", "العروض والخصومات", "التقييمات", "سياسة المتجر", "الفروع")
        ProfileEntityType.RESTAURANT -> listOf("القائمة", "الأطباق الأكثر طلبًا", "العروض", "التقييمات", "معلومات المطعم", "الحجز")
        ProfileEntityType.MEDICAL -> listOf("الرئيسية", "الأطباء", "الأقسام والتخصصات", "الحجز", "المختبر والأشعة", "التقييمات", "عن المركز", "الفروع")
        ProfileEntityType.REAL_ESTATE -> listOf("نظرة عامة", "الموقع", "المواصفات", "التقسيط والدفع", "التواصل")
        ProfileEntityType.JOB -> listOf("تفاصيل الوظيفة", "المتطلبات", "الرواتب والمزايا", "عن الشركة", "التقديم")
        ProfileEntityType.TECHNICIAN -> listOf("سابقة الأعمال 📸", "الخدمات والتسعير 🛠️", "التقييمات والآراء ⭐", "شروط الضمان 🛡️")
        ProfileEntityType.GENERAL -> listOf("الخدمات", "التقييمات", "معلومات التواصل")
    }

    val activeGradient = when (entityType) {
        ProfileEntityType.STORE -> Brush.horizontalGradient(listOf(Color(0xFF8B5CF6), Color(0xFF6366F1)))
        ProfileEntityType.RESTAURANT -> Brush.horizontalGradient(listOf(Color(0xFFFF5A5F), Color(0xFFFF9A5A)))
        ProfileEntityType.MEDICAL -> Brush.horizontalGradient(listOf(Color(0xFF4ECDC4), Color(0xFF2DD4BF)))
        ProfileEntityType.REAL_ESTATE -> Brush.horizontalGradient(listOf(Color(0xFF10B981), Color(0xFF34D399)))
        ProfileEntityType.JOB -> Brush.horizontalGradient(listOf(Color(0xFF4A90E2), Color(0xFF6366F1)))
        ProfileEntityType.TECHNICIAN -> Brush.horizontalGradient(listOf(Color(0xFF10B981), Color(0xFF059669)))
        ProfileEntityType.GENERAL -> Brush.horizontalGradient(listOf(Color(0xFF2563EB), Color(0xFF06B6D4)))
    }

    ScrollableTabRow(
        selectedTabIndex = selectedTab.coerceIn(0, tabTitles.size - 1),
        containerColor = Color.Transparent,
        contentColor = Color.White,
        edgePadding = 4.dp,
        divider = {},
        indicator = {},
        modifier = Modifier.fillMaxWidth()
    ) {
        tabTitles.forEachIndexed { index, title ->
            val isSelected = selectedTab == index
            Surface(
                onClick = { onTabSelected(index) },
                shape = RoundedCornerShape(999.dp),
                color = if (isSelected) Color.Transparent else BespokePremiumSystem.SurfaceDark,
                border = BorderStroke(1.dp, if (isSelected) Color.Transparent else BespokePremiumSystem.GlassBorder),
                modifier = Modifier.padding(horizontal = 4.dp, vertical = 4.dp)
            ) {
                Box(
                    modifier = Modifier
                        .then(if (isSelected) Modifier.background(activeGradient) else Modifier)
                        .padding(horizontal = 14.dp, vertical = 8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = title,
                        fontSize = 11.5.sp,
                        fontWeight = if (isSelected) FontWeight.Black else FontWeight.SemiBold,
                        color = if (isSelected) Color.White else Color(0xFF94A3B8)
                    )
                }
            }
        }
    }
}
