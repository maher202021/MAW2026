package com.serviseyem.ui.screens.map.components

import androidx.compose.foundation.background
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
import com.serviseyem.ui.screens.map.MapMode
import com.serviseyem.utils.VisualThemePalette

@Composable
fun MapControlCenter(
    currentMode: MapMode,
    onModeChange: (MapMode) -> Unit,
    tiltAngle: Float,
    onToggleTilt: () -> Unit,
    onOpenAleph: () -> Unit,
    themeColors: VisualThemePalette,
    modifier: Modifier = Modifier
) {
    val cardPadding = 4.dp
    val buttonSize = 36.dp
    val iconSize = 16.sp
    val textSize = 6.sp

    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xAA0F172A)),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0x22FFFFFF)),
        modifier = modifier
            .padding(horizontal = 4.dp, vertical = 4.dp)
            .width(IntrinsicSize.Min)
    ) {
        Column(
            modifier = Modifier
                .padding(vertical = 4.dp, horizontal = 2.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "VELA",
                fontSize = 8.sp,
                fontWeight = FontWeight.Black,
                color = themeColors.accent.copy(alpha = 0.8f),
                letterSpacing = 0.5.sp
            )
            
            Divider(color = Color(0x11FFFFFF), modifier = Modifier.width(20.dp))

            MapMode.values().forEach { mode ->
                val isSelected = currentMode == mode
                Surface(
                    onClick = {
                        if (mode == MapMode.ALEPH_AI) {
                            onOpenAleph()
                        } else {
                            onModeChange(mode)
                        }
                    },
                    shape = RoundedCornerShape(12.dp),
                    color = if (isSelected) themeColors.accent else Color(0x08FFFFFF),
                    modifier = Modifier
                        .padding(1.dp)
                        .size(buttonSize)
                ) {
                    Column(
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.fillMaxSize()
                    ) {
                        Text(text = mode.icon, fontSize = iconSize)
                        Text(
                            text = mode.label.split(" ").first(),
                            fontSize = textSize,
                            fontWeight = if (isSelected) FontWeight.ExtraBold else FontWeight.Normal,
                            color = if (isSelected) Color.Black else Color(0xFF94A3B8),
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                            maxLines = 1
                        )
                    }
                }
            }
        }
    }
}
