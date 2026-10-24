package com.serviseyem.ui.screens.dashboard.components

import androidx.compose.foundation.background
import com.serviseyem.ui.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.serviseyem.utils.VisualThemePalette

@Composable
fun UnifiedReviewsSection(
    userName: String,
    rating: Double,
    comment: String,
    dateString: String = "",
    ownerReply: String = "",
    themeColors: VisualThemePalette,
    onReplySubmit: (String) -> Unit
) {
    var showReplyDialog by remember { mutableStateOf(false) }
    var replyInput by remember { mutableStateOf(ownerReply) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = themeColors.surface),
        shape = RoundedCornerShape(12.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.08f))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
                    Text(text = "👤 ${userName.ifBlank { "عميل" }}", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = themeColors.textPrimary)
                    Text(text = "⭐ %.1f".format(rating), fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFFFFA000))
                }
                if (dateString.isNotBlank()) {
                    Text(text = dateString, fontSize = 10.sp, color = Color.Gray)
                }
            }

            if (comment.isNotBlank()) {
                Text(text = comment, fontSize = 12.sp, color = themeColors.textSecondary)
            }

            if (ownerReply.isNotBlank()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(themeColors.accent.copy(alpha = 0.1f), RoundedCornerShape(8.dp))
                        .padding(10.dp)
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                        Text(text = "💬 رد الإدارة / صاحب المنشأة:", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = themeColors.accent)
                        Text(text = ownerReply, fontSize = 11.sp, color = themeColors.textPrimary)
                    }
                }
            } else {
                TextButton(
                    onClick = { showReplyDialog = true },
                    contentPadding = PaddingValues(0.dp)
                ) {
                    Text(text = "الرد على التقييم 💬", fontSize = 11.sp, color = themeColors.accent, fontWeight = FontWeight.Bold)
                }
            }
        }
    }

    if (showReplyDialog) {
        AlertDialog(
            onDismissRequest = { showReplyDialog = false },
            title = { Text("الرد على تقييم العميل", fontSize = 14.sp, fontWeight = FontWeight.Bold) },
            text = {
                OutlinedTextField(
                    value = replyInput,
                    onValueChange = { replyInput = it },
                    label = { Text("اكتب ردك للعميل...") },
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (replyInput.isNotBlank()) {
                            onReplySubmit(replyInput)
                            showReplyDialog = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = themeColors.accent)
                ) {
                    Text("إرسال الرد", color = Color.Black, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showReplyDialog = false }) {
                    Text("إلغاء")
                }
            }
        )
    }
}
