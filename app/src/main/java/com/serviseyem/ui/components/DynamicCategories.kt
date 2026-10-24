package com.serviseyem.ui.components

import androidx.compose.foundation.background
import com.serviseyem.ui.*
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.serviseyem.utils.VisualThemePalette

data class CategoryFilterItem(
    val id: String,
    val title: String,
    val icon: ImageVector,
    val count: Int,
    val color: Color
)

/**
 * 🏷️ DynamicCategories (شريط التصنيفات الديناميكية المتفاعلة)
 * يعرض التصنيفات الرئيسية مع الأيقونات وعدد العناصر المتاحة وتأثيرات التحديد المتوهجة.
 */
@Composable
fun DynamicCategories(
    selectedCategoryId: String,
    onCategorySelected: (String) -> Unit,
    themeColors: VisualThemePalette,
    modifier: Modifier = Modifier
) {
    val categories = listOf(
        CategoryFilterItem("ALL", "الكل", Icons.Default.Home, 120, Color(0xFF00E5FF)),
        CategoryFilterItem("PROVIDERS", "فنيون ومهنيون", Icons.Default.Engineering, 45, Color(0xFF38BDF8)),
        CategoryFilterItem("STORES", "متاجر وأسواق", Icons.Default.Storefront, 32, Color(0xFF10B981)),
        CategoryFilterItem("RESTAURANTS", "مطاعم وكافيهات", Icons.Default.Restaurant, 28, Color(0xFFF59E0B)),
        CategoryFilterItem("MEDICAL", "مراكز طبية", Icons.Default.MedicalServices, 19, Color(0xFF06B6D4)),
        CategoryFilterItem("PROPERTIES", "عقارات وشقق", Icons.Default.Apartment, 15, Color(0xFF8B5CF6)),
        CategoryFilterItem("JOBS", "وظائف شاغرة", Icons.Default.Work, 22, Color(0xFFEC4899))
    )

    LazyRow(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(categories) { cat ->
            val isSelected = selectedCategoryId == cat.id

            Surface(
                shape = RoundedCornerShape(20.dp),
                color = if (isSelected) cat.color else Color(0xFF1E293B),
                shadowElevation = if (isSelected) 4.dp else 1.dp,
                modifier = Modifier.clickable { onCategorySelected(cat.id) }
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        imageVector = cat.icon,
                        contentDescription = null,
                        tint = if (isSelected) Color(0xFF0F172A) else cat.color,
                        modifier = Modifier.size(16.dp)
                    )

                    Text(
                        text = cat.title,
                        fontSize = 12.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                        color = if (isSelected) Color(0xFF0F172A) else Color.White
                    )

                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = if (isSelected) Color.Black.copy(alpha = 0.15f) else Color(0xFF334155)
                    ) {
                        Text(
                            text = "${cat.count}",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isSelected) Color(0xFF0F172A) else Color(0xFF94A3B8),
                            modifier = Modifier.padding(horizontal = 5.dp, vertical = 1.dp)
                        )
                    }
                }
            }
        }
    }
}
