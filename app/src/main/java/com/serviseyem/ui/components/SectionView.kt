package com.serviseyem.ui.components

import androidx.compose.foundation.clickable
import com.serviseyem.ui.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowLeft
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

/**
 * 📑 SectionView (مكون الأقسام الموحد للشاشة الرئيسية)
 * يوفر ترويسة قياسية مع عنوان القسم، أيقونة معبرة، زر "عرض الكل"، ومساحة مخصصة للمحتوى (Slot).
 */
@Composable
fun SectionView(
    title: String,
    icon: ImageVector? = null,
    iconColor: Color = Color(0xFF00E5FF),
    onSeeAllClick: (() -> Unit)? = null,
    seeAllText: String = "عرض الكل",
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 10.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (icon != null) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = iconColor.copy(alpha = 0.15f),
                        modifier = Modifier.size(32.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = icon,
                                contentDescription = null,
                                tint = iconColor,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }

                Text(
                    text = title,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }

            if (onSeeAllClick != null) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.clickable { onSeeAllClick() }
                ) {
                    Text(
                        text = seeAllText,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = iconColor
                    )
                    Icon(
                        imageVector = Icons.Default.KeyboardArrowLeft,
                        contentDescription = "See All",
                        tint = iconColor,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }

        // Section Body Content
        content()
    }
}
