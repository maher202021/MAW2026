package com.serviseyem.ui.screens.dashboard.components

import android.content.Context
import com.serviseyem.ui.*
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
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
import androidx.compose.ui.platform.LocalContext
import com.serviseyem.data.RatingEntity
import com.serviseyem.ui.MainViewModel
import com.serviseyem.utils.VisualThemePalette

/**
 * ⭐ StoreReviewsCard - بطاقة عرض تقييمات العملاء والرد عليها
 */
@Composable
fun StoreReviewsCard(
    storeRatings: List<RatingEntity>,
    viewModel: MainViewModel,
    themeColors: VisualThemePalette,
    context: Context = LocalContext.current
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = themeColors.surface),
        border = BorderStroke(1.dp, themeColors.accent.copy(alpha = 0.3f)),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text("⭐ تقييمات العملاء والرد المباشر عليها:", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = themeColors.accent)
            
            if (storeRatings.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text("📭 لا توجد تقييمات أو تعليقات من العملاء حالياً.", fontSize = 10.sp, color = Color.Gray)
                }
            } else {
                storeRatings.forEach { r ->
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color.White.copy(alpha = 0.03f), RoundedCornerShape(8.dp))
                            .padding(10.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(r.userName.ifEmpty { "عميل مجهول" }, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.White)
                            Text("⭐".repeat(r.rating.toInt().coerceIn(1, 5)), fontSize = 10.sp, color = Color.Yellow)
                        }

                        Text(r.comment, fontSize = 11.sp, color = Color.LightGray)

                        if (r.reply.isNotEmpty()) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 4.dp)
                                    .background(themeColors.primary.copy(alpha = 0.15f), RoundedCornerShape(4.dp))
                                    .padding(6.dp)
                            ) {
                                Text("💬 ردّك الحالي: ${r.reply}", fontSize = 10.sp, color = themeColors.accent, fontWeight = FontWeight.Bold)
                            }
                        } else {
                            var replyText by remember { mutableStateOf("") }
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                OutlinedTextField(
                                    value = replyText,
                                    onValueChange = { replyText = it },
                                    placeholder = { Text("اكتب ردك للعميل هنا...", fontSize = 9.sp) },
                                    modifier = Modifier.weight(1f).height(44.dp),
                                    textStyle = androidx.compose.ui.text.TextStyle(fontSize = 11.sp, color = Color.White),
                                    colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White)
                                )
                                Button(
                                    onClick = {
                                        if (replyText.trim().isNotEmpty()) {
                                            viewModel.addRatingReply(r.id, replyText.trim())
                                            replyText = ""
                                            Toast.makeText(context, "✅ تم إرسال ردّك بنجاح للعميل والنشره فوراً", Toast.LENGTH_SHORT).show()
                                        }
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = themeColors.accent),
                                    contentPadding = PaddingValues(horizontal = 8.dp),
                                    modifier = Modifier.height(34.dp)
                                ) {
                                    Text("رد ⚡", color = Color.Black, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
