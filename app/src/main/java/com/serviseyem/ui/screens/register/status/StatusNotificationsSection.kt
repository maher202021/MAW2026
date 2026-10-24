package com.serviseyem.ui.screens.register.status

import android.content.Context
import com.serviseyem.ui.*
import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.serviseyem.data.NotificationEntity
import com.serviseyem.utils.VisualThemePalette

/**
 * 🔔 StatusNotificationsSection - قسم الإشعارات والتعميمات مع زري اتصال ومراسلة
 */
@Composable
fun StatusNotificationsSection(
    notifications: List<NotificationEntity>,
    onOpenChatWithCustomer: (String, String) -> Unit,
    themeColors: VisualThemePalette,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = "🔔 الإشعارات والتعميمات الموجهة إليك:",
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            color = themeColors.accent
        )

        if (notifications.isEmpty()) {
            Card(
                colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.02f)),
                border = BorderStroke(1.dp, Color.White.copy(alpha = 0.06f)),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 24.dp, horizontal = 16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "🔔",
                        fontSize = 32.sp
                    )
                    Text(
                        text = "لا توجد إشعارات إدارية جديدة حالياً",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White.copy(alpha = 0.7f)
                    )
                    Text(
                        text = "سيتم نشر أي تعاميم، عروض عمل، أو تحديثات إدارية هامة هنا فور صدورها.",
                        fontSize = 10.sp,
                        color = Color.Gray,
                        lineHeight = 15.sp,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                }
            }
        } else {
            notifications.take(4).forEach { notif ->
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.05f)),
                    border = BorderStroke(1.dp, Color.White.copy(alpha = 0.1f)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(notif.title, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = themeColors.accent)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(notif.message, fontSize = 11.sp, color = Color.LightGray, lineHeight = 16.sp)

                        if (notif.customerPhone.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(8.dp))
                            HorizontalDivider(color = Color.White.copy(alpha = 0.1f), thickness = 0.5.dp)
                            Spacer(modifier = Modifier.height(8.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Button(
                                    onClick = {
                                        val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:${notif.customerPhone}"))
                                        context.startActivity(intent)
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981)),
                                    shape = RoundedCornerShape(6.dp),
                                    modifier = Modifier.weight(1f),
                                    contentPadding = PaddingValues(vertical = 4.dp, horizontal = 8.dp)
                                ) {
                                    Text("📞 اتصال مباشر", fontSize = 10.sp, color = Color.White, fontWeight = FontWeight.Bold)
                                }

                                Button(
                                    onClick = {
                                        onOpenChatWithCustomer(notif.customerPhone, notif.customerName)
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = themeColors.accent),
                                    shape = RoundedCornerShape(6.dp),
                                    modifier = Modifier.weight(1f),
                                    contentPadding = PaddingValues(vertical = 4.dp, horizontal = 8.dp)
                                ) {
                                    Text("💬 مراسلة فورية", fontSize = 10.sp, color = Color.Black, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
