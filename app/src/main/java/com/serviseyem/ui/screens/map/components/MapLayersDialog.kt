package com.serviseyem.ui.screens.map.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog

data class MapLayerItem(
    val id: String,
    val title: String,
    val subtitle: String,
    val iconEmoji: String
)

/**
 * 🗺️ MapLayersDialog (مطابقة تامة للصورة 2 لاختيار طبقة الخريطة)
 */
@Composable
fun MapLayersDialog(
    currentLayer: String,
    onSelectLayer: (String) -> Unit,
    onDismiss: () -> Unit
) {
    val layers = listOf(
        MapLayerItem(
            id = "satellite",
            title = "الأقمار الصناعية (عالي الدقة) 🛰️",
            subtitle = "صور جوية فضائية عالية الدقة للمدن والمعالم",
            iconEmoji = "🛰️"
        ),
        MapLayerItem(
            id = "vela_3d",
            title = "Vela 3D (مباني ثلاثية الأبعاد) 🏙️",
            subtitle = "عرض مجسم ثلاثي الأبعاد للمباني وإمالة الكاميرا التفاعلية",
            iconEmoji = "🏙️"
        ),
        MapLayerItem(
            id = "hot",
            title = "خريطة الشوارع الحية (HOT) 🗺️",
            subtitle = "خريطة تفاعلية للشوارع والأحياء المباشرة",
            iconEmoji = "🗺️"
        ),
        MapLayerItem(
            id = "topo",
            title = "التضاريس والارتفاعات (Topo) ⛰️",
            subtitle = "إظهار المعالم الجغرافية والجبال والمجاري",
            iconEmoji = "⛰️"
        ),
        MapLayerItem(
            id = "osm",
            title = "خريطة الشوارع القياسية (OSM) 🌐",
            subtitle = "الخريطة المفصلة المفتوحة",
            iconEmoji = "🌐"
        )
    )

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(22.dp),
            color = Color(0xFF0F172A).copy(alpha = 0.96f),
            border = BorderStroke(1.dp, Color(0xFF334155)),
            shadowElevation = 24.dp,
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .padding(vertical = 16.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(18.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Header
                Text(
                    text = "اختيار طبقة الخريطة 🗺️",
                    fontSize = 17.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color.White,
                    modifier = Modifier.padding(bottom = 6.dp)
                )

                // List of layer choices
                layers.forEach { layer ->
                    val isSelected = currentLayer == layer.id ||
                            (layer.id == "satellite" && (currentLayer == "google_satellite" || currentLayer == "hybrid")) ||
                            (layer.id == "osm" && currentLayer == "osm_ar")

                    val borderColor = if (isSelected) Color(0xFF10B981) else Color(0xFF1E293B)
                    val bgColor = if (isSelected) {
                        Brush.horizontalGradient(
                            listOf(Color(0xFF064E3B).copy(alpha = 0.6f), Color(0xFF0F172A).copy(alpha = 0.8f))
                        )
                    } else {
                        Brush.horizontalGradient(
                            listOf(Color(0xFF1E293B).copy(alpha = 0.7f), Color(0xFF0F172A).copy(alpha = 0.7f))
                        )
                    }

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(14.dp))
                            .background(bgColor)
                            .clickable {
                                onSelectLayer(layer.id)
                                onDismiss()
                            }
                            .padding(horizontal = 14.dp, vertical = 12.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(
                                modifier = Modifier.weight(1f),
                                verticalArrangement = Arrangement.spacedBy(3.dp)
                            ) {
                                Text(
                                    text = layer.title,
                                    fontSize = 13.5.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isSelected) Color(0xFF34D399) else Color.White
                                )
                                Text(
                                    text = layer.subtitle,
                                    fontSize = 10.5.sp,
                                    color = Color(0xFF94A3B8),
                                    lineHeight = 14.sp
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                // Close Button
                TextButton(
                    onClick = onDismiss,
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                ) {
                    Text(
                        text = "إغلاق",
                        color = Color(0xFF38BDF8),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}
