package com.serviseyem.ui.screens.map.components

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
import androidx.compose.ui.window.Dialog
import com.serviseyem.utils.VisualThemePalette

@Composable
fun AlephVisualAssistantDialog(
    targetEntity: Any?,
    userLat: Double,
    userLng: Double,
    onDismiss: () -> Unit,
    onOpenBooking: (Any?) -> Unit,
    themeColors: VisualThemePalette
) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "👁️ المساعد البصري الذكي",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = themeColors.accent
                )
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "يقوم المساعد البصري بتحليل موقعك الحالي واستكشاف الخدمات المجاورة لك بدقة عالية.",
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(20.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("إغلاق")
                    }
                    if (targetEntity != null) {
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(
                            onClick = { onOpenBooking(targetEntity) },
                            colors = ButtonDefaults.buttonColors(containerColor = themeColors.accent)
                        ) {
                            Text("حجز الخدمة", color = Color.White)
                        }
                    }
                }
            }
        }
    }
}
