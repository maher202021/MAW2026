package com.serviseyem.ui.screens.register.status

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.serviseyem.utils.VisualThemePalette

/**
 * PendingApprovalView - عرض شاشة طلب قيد المراجعة والتدقيق الإداري مع تأكيد قبل الإلغاء
 */
@Composable
fun PendingApprovalView(
    title: String = "طلب انضمامك قيد المراجعة ⏳",
    message: String = "تم استلام طلبك بنجاح وهو قيد التدقيق الإداري ومراجعة البيانات من قِبل الإدارة. فور الموافقة سيتم تفعيل حسابك فوراً.",
    detailsTitle: String = "📋 تفاصيل الطلب المقدم:",
    detailsList: List<Pair<String, String>>,
    onCancelRequest: () -> Unit,
    themeColors: VisualThemePalette,
    modifier: Modifier = Modifier
) {
    var showCancelConfirmDialog by remember { mutableStateOf(false) }

    if (showCancelConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showCancelConfirmDialog = false },
            containerColor = Color(0xFF1E293B),
            title = {
                Text("تأكيد إلغاء الطلب", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 15.sp)
            },
            text = {
                Text(
                    "هل أنت متأكد من إلغاء طلب التسجيل الحالي والعودة لشاشة التسجيل؟ سيتم حذف الطلب المعلق.",
                    color = Color.LightGray,
                    fontSize = 12.sp
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        showCancelConfirmDialog = false
                        onCancelRequest()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF4444)),
                    modifier = Modifier.defaultMinSize(minHeight = 48.dp)
                ) {
                    Text("نعم، إلغاء الطلب", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                }
            },
            dismissButton = {
                OutlinedButton(
                    onClick = { showCancelConfirmDialog = false },
                    modifier = Modifier.defaultMinSize(minHeight = 48.dp)
                ) {
                    Text("تراجع", color = Color.White, fontSize = 12.sp)
                }
            }
        )
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(12.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = themeColors.surface),
        border = BorderStroke(1.5.dp, Color(0xFFF59E0B))
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .background(Color(0xFFF59E0B).copy(alpha = 0.2f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(text = "⏳", fontSize = 28.sp)
            }

            Text(
                text = title,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFFF59E0B),
                textAlign = TextAlign.Center
            )

            Text(
                text = message,
                fontSize = 11.sp,
                color = Color.White.copy(alpha = 0.85f),
                textAlign = TextAlign.Center,
                lineHeight = 16.sp
            )

            // خطوات التتبع الواضحة
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.Black.copy(alpha = 0.22f), RoundedCornerShape(10.dp))
                    .padding(horizontal = 10.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("✅ 1. تم الإرسال", fontSize = 10.sp, color = Color(0xFF10B981), fontWeight = FontWeight.Bold)
                Text("⏳ 2. مراجعة الأدمن", fontSize = 10.sp, color = Color(0xFFF59E0B), fontWeight = FontWeight.Bold)
                Text("🔒 3. تفعيل الحساب", fontSize = 10.sp, color = Color.Gray)
            }

            HorizontalDivider(color = Color.White.copy(alpha = 0.1f), thickness = 1.dp)

            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFF2D2214)),
                border = BorderStroke(1.dp, Color(0xFFF59E0B).copy(alpha = 0.3f)),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(detailsTitle, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFFF59E0B))
                    detailsList.forEach { (key, value) ->
                        Text("• $key: $value", fontSize = 11.sp, color = Color.White)
                    }
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            Button(
                onClick = { showCancelConfirmDialog = true },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF475569)),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .testTag("pending_cancel_button")
            ) {
                Text("❌ إلغاء والعودة لشاشة التسجيل", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}
