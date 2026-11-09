package com.serviseyem.ui.screens.map.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.serviseyem.utils.VisualThemePalette
import java.text.SimpleDateFormat
import java.util.*

/**
 * 🌋 EarthquakeBottomSheet - نافذة منبثقة للتحكم بطبقة الزلازل الحية
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EarthquakeBottomSheet(
    isActive: Boolean,
    onToggleActive: (Boolean) -> Unit,
    lastUpdateTime: Long?,
    earthquakeCount: Int,
    isLoading: Boolean,
    onRefreshNow: () -> Unit,
    themeColors: VisualThemePalette,
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = Color(0xFF0F172A),
        scrimColor = Color.Black.copy(alpha = 0.65f),
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
        dragHandle = {
            BottomSheetDefaults.DragHandle(
                color = Color.White.copy(alpha = 0.3f),
                width = 44.dp,
                height = 4.dp
            )
        },
        modifier = Modifier.testTag("earthquake_bottom_sheet")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .padding(bottom = 32.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .background(Color(0xFFEF4444).copy(alpha = 0.2f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Warning,
                            contentDescription = "زلزال",
                            tint = Color(0xFFEF4444),
                            modifier = Modifier.size(24.dp)
                        )
                    }
                    Column {
                        Text(
                            text = "مرصد الزلازل والأنشطة الزلزالية 🌋",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp
                        )
                        Text(
                            text = "رصد حي ومباشر عالمياً من هيئة USGS الأمريكية",
                            color = Color(0xFF94A3B8),
                            fontSize = 11.sp
                        )
                    }
                }

                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "إغلاق",
                        tint = Color.LightGray,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            // 1. تفعيل/إيقاف الطبقة
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = Color(0xFF1E293B),
                border = BorderStroke(1.dp, if (isActive) Color(0xFFEF4444).copy(alpha = 0.5f) else Color.White.copy(alpha = 0.1f))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "عرض طبقة الزلازل على الخريطة",
                            color = Color.White,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 13.sp
                        )
                        Text(
                            text = if (isActive) "الطبقة مفعلة الآن وتظهر دوائر الهزات الأرضية" else "انقر لتفعيل الطبقة وعرض الهزات على الخريطة",
                            color = if (isActive) Color(0xFFFCA5A5) else Color(0xFF94A3B8),
                            fontSize = 10.sp
                        )
                    }

                    Switch(
                        checked = isActive,
                        onCheckedChange = { onToggleActive(it) },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.White,
                            checkedTrackColor = Color(0xFFEF4444),
                            uncheckedThumbColor = Color.Gray,
                            uncheckedTrackColor = Color(0xFF334155)
                        ),
                        modifier = Modifier.testTag("earthquake_toggle_switch")
                    )
                }
            }

            // 2. حالة آخر تحديث وإحصائية
            Surface(
                shape = RoundedCornerShape(14.dp),
                color = Color(0xFF1E293B).copy(alpha = 0.6f),
                border = BorderStroke(1.dp, Color.White.copy(alpha = 0.08f))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceAround,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "وقت آخر تحديث",
                            fontSize = 10.sp,
                            color = Color(0xFF94A3B8)
                        )
                        Text(
                            text = if (lastUpdateTime != null) {
                                val sdf = SimpleDateFormat("hh:mm a", Locale.getDefault())
                                sdf.format(Date(lastUpdateTime))
                            } else {
                                "لم يتم التحديث بعد"
                            },
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF00E5FF)
                        )
                    }

                    Box(
                        modifier = Modifier
                            .width(1.dp)
                            .height(28.dp)
                            .background(Color.White.copy(alpha = 0.15f))
                    )

                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "الهزات المرصودة (24س)",
                            fontSize = 10.sp,
                            color = Color(0xFF94A3B8)
                        )
                        Text(
                            text = if (earthquakeCount > 0) "$earthquakeCount هزة" else if (isActive) "جاري الفحص..." else "-",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (earthquakeCount > 0) Color(0xFFF59E0B) else Color.White
                        )
                    }
                }
            }

            // 3. زر "تحديث الآن"
            val infiniteTransition = rememberInfiniteTransition(label = "refresh_spin")
            val rotation by infiniteTransition.animateFloat(
                initialValue = 0f,
                targetValue = 360f,
                animationSpec = infiniteRepeatable(
                    animation = tween(800, easing = LinearEasing),
                    repeatMode = RepeatMode.Restart
                ),
                label = "spin_anim"
            )

            Button(
                onClick = onRefreshNow,
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFFDC2626),
                    contentColor = Color.White
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .testTag("earthquake_refresh_btn")
            ) {
                Icon(
                    imageVector = Icons.Default.Refresh,
                    contentDescription = "تحديث",
                    modifier = Modifier
                        .size(18.dp)
                        .rotate(if (isLoading) rotation else 0f)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = if (isLoading) "جاري جلب أحدث البيانات..." else "تحديث البيانات الآن 🔄",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            // 4. دليل مقياس ريختر اللوني
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 4.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text(
                    text = "دليل ألوان الهزات على الخريطة:",
                    fontSize = 10.sp,
                    color = Color(0xFF94A3B8),
                    fontWeight = FontWeight.SemiBold
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    LegendItem(color = Color(0xFFDC2626), label = "> 5.5 ريختر (شديد)")
                    LegendItem(color = Color(0xFFEA580C), label = "4.0 - 5.4 (متوسط)")
                    LegendItem(color = Color(0xFFF59E0B), label = "2.5 - 3.9 (خفيف)")
                    LegendItem(color = Color(0xFF10B981), label = "< 2.5 (طفيف)")
                }
            }
        }
    }
}

@Composable
private fun LegendItem(color: Color, label: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Box(
            modifier = Modifier
                .size(8.dp)
                .background(color, CircleShape)
        )
        Text(
            text = label,
            fontSize = 9.sp,
            color = Color(0xFFCBD5E1)
        )
    }
}
