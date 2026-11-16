package com.serviseyem

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.serviseyem.data.*
import com.serviseyem.ui.*
import com.serviseyem.ui.MainViewModel
import com.serviseyem.utils.VisualThemePalette

// --------------------------------------------------------
// 5. PROPERTY DETAILS DIALOG
// --------------------------------------------------------
@Composable
fun PropertyDetailsDialog(
    property: PropertyEntity,
    viewModel: MainViewModel,
    themeColors: VisualThemePalette,
    onDismiss: () -> Unit
) {
    val ratings by viewModel.ratings.collectAsState()
    val currentUserId by viewModel.currentUserId.collectAsState()
    val currentUserName by viewModel.currentUserName.collectAsState()
    val context = LocalContext.current

    val propReviews = remember(ratings) { ratings.filter { it.targetId == property.id && it.targetType == "PROPERTY" && it.isApproved } }

    var userRatingInput by remember { mutableStateOf(5f) }
    var userCommentInput by remember { mutableStateOf("") }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            color = themeColors.background,
            modifier = Modifier.fillMaxSize()
        ) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.TopCenter
            ) {
                Column(
                    modifier = Modifier
                        .widthIn(max = 840.dp)
                        .fillMaxSize()
                        .navigationBarsPadding()
                        .verticalScroll(rememberScrollState())
                        .padding(bottom = 32.dp)
                ) {
                    // Photo Gallery/Cover
                    val hasImages = property.images.isNotEmpty()
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(if (hasImages) 160.dp else 110.dp)
                            .background(themeColors.primary.copy(alpha = 0.15f))
                    ) {
                        if (!hasImages) {
                            Text("🏠 ${property.title}", modifier = Modifier.align(Alignment.CenterStart).statusBarsPadding().padding(start = 16.dp, end = 64.dp), color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                        } else {
                            Text("📸 معرض صور العقار التفصيلية", modifier = Modifier.align(Alignment.Center), color = Color.Gray, fontSize = 12.sp)
                        }
                        IconButton(
                            onClick = onDismiss,
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .statusBarsPadding()
                                .padding(8.dp)
                                .size(44.dp)
                                .background(Color.Black.copy(alpha = 0.5f), CircleShape)
                        ) {
                            Icon(Icons.Default.Close, contentDescription = "إغلاق", tint = Color.Red)
                        }
                        Box(
                            modifier = Modifier
                                .align(Alignment.TopStart)
                                .statusBarsPadding()
                                .background(if (property.type == "rent") Color(0xFF0284C7) else Color(0xFF16A34A), RoundedCornerShape(bottomEnd = 8.dp))
                                .padding(horizontal = 10.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = if (property.type == "rent") "للإيجار" else "للبيع",
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                    }

                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    val adminRole by viewModel.adminRole.collectAsState()
                    var showEditDialog by remember { mutableStateOf(false) }

                    if (adminRole != "GUEST") {
                        Card(
                            colors = CardDefaults.cardColors(containerColor = Color(0xFF7F1D1D).copy(alpha = 0.2f)),
                            border = BorderStroke(1.dp, Color.Red),
                            modifier = Modifier.fillMaxWidth().padding(bottom = 10.dp)
                        ) {
                            Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                Text("🛠️ لوحة تحكم الإدارة الفورية لهذا العقار:", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Button(
                                        onClick = { showEditDialog = true },
                                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981)),
                                        modifier = Modifier.weight(1f).heightIn(min = 44.dp),
                                        shape = RoundedCornerShape(8.dp),
                                        contentPadding = PaddingValues(horizontal = 6.dp, vertical = 6.dp)
                                    ) {
                                        Text("📝 تعديل البيانات", color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                    }
                                    Button(
                                        onClick = {
                                            viewModel.deleteProperty(property.id)
                                            onDismiss()
                                            android.widget.Toast.makeText(context, "🗑️ تم حذف وحظر العقار من النظام بنجاح!", android.widget.Toast.LENGTH_LONG).show()
                                        },
                                        colors = ButtonDefaults.buttonColors(containerColor = Color.Red),
                                        modifier = Modifier.weight(1f).heightIn(min = 44.dp),
                                        shape = RoundedCornerShape(8.dp),
                                        contentPadding = PaddingValues(horizontal = 6.dp, vertical = 6.dp)
                                    ) {
                                        Text("🗑️ حذف وحظر", color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }
                    }

                    if (showEditDialog) {
                        PropertyCreateEditDialog(
                            property = property,
                            viewModel = viewModel,
                            themeColors = themeColors,
                            onDismiss = { showEditDialog = false }
                        )
                    }

                    Text(property.title, fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Color.White)
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("السعر المطلوب المالي:", fontSize = 9.sp, color = themeColors.textSecondary)
                            Text("${property.price} ${property.currency}", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = themeColors.accent)
                        }
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("⭐", fontSize = 12.sp)
                            Spacer(modifier = Modifier.width(3.dp))
                            Text(String.format("%.1f", property.rating), fontSize = 13.sp, fontWeight = FontWeight.Bold, color = themeColors.accent)
                        }
                    }

                    Divider(color = themeColors.accent.copy(alpha = 0.15f))

                    Text("📝 تفاصيل و مواصفات العقار:", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = themeColors.accent)
                    Text(property.description, fontSize = 11.sp, color = Color.White)

                    // Show image gallery if available
                    if (property.images.isNotEmpty()) {
                        Text("📸 معرض صور العقار التفصيلية (${property.images.size}):", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = themeColors.accent)
                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth().height(120.dp)
                        ) {
                            items(property.images.size) { index ->
                                val bMap = rememberBase64Bitmap(property.images[index])
                                if (bMap != null) {
                                    Image(
                                        bitmap = bMap,
                                        contentDescription = null,
                                        modifier = Modifier.width(160.dp).fillMaxHeight().clip(RoundedCornerShape(8.dp)),
                                        contentScale = ContentScale.Crop
                                    )
                                }
                            }
                        }
                    }

                    // Show PDF link if available and approved
                    if (property.pdfFileBase64.isNotEmpty() && property.pdfStatus == "APPROVED") {
                        Card(
                            colors = CardDefaults.cardColors(containerColor = themeColors.surface),
                            modifier = Modifier.fillMaxWidth().clickable {
                                android.widget.Toast.makeText(context, "📄 تم تحميل مستندات وخرائط وثيقة الملكية المعتمدة للعقار بنجاح!", android.widget.Toast.LENGTH_LONG).show()
                            }
                        ) {
                            Row(modifier = Modifier.padding(10.dp), verticalAlignment = Alignment.CenterVertically) {
                                Text("📄", fontSize = 24.sp)
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text("عرض وثائق وتفاصيل المخططات الهندسية والملكية (PDF)", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = themeColors.accent)
                                    Text("مستند رسمي معتمد وموثق ومفحوص من الإدارة", fontSize = 9.sp, color = Color.LightGray)
                                }
                            }
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Card(
                            colors = CardDefaults.cardColors(containerColor = themeColors.surface),
                            modifier = Modifier.weight(1f)
                        ) {
                            Column(modifier = Modifier.padding(8.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("نوع العقار", fontSize = 8.sp, color = themeColors.textSecondary)
                                Text(property.propertyType, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.White)
                            }
                        }
                        Card(
                            colors = CardDefaults.cardColors(containerColor = themeColors.surface),
                            modifier = Modifier.weight(1f)
                        ) {
                            Column(modifier = Modifier.padding(8.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("📍 الحي والمربع السكني", fontSize = 8.sp, color = themeColors.textSecondary)
                                Text(property.localNeighborhood, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.White)
                            }
                        }
                    }

                    // Contacts panel
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = {
                                val u = Intent(Intent.ACTION_DIAL, Uri.parse("tel:${property.phone}"))
                                context.startActivity(u)
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF16A34A)),
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Default.Call, null, tint = Color.White, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("اتصال بالمالك", color = Color.White, fontSize = 11.sp)
                        }

                        Button(
                            onClick = {
                                val u = Intent(Intent.ACTION_VIEW, Uri.parse("https://wa.me/${property.phone.replace("+", "")}"))
                                context.startActivity(u)
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0D9488)),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("💬 واتساب مالي", color = Color.White, fontSize = 11.sp)
                        }
                    }

                    Divider(color = themeColors.accent.copy(alpha = 0.15f))

                    // Reviews/Comments list
                    Text("💬 الاستفسارات والتعليقات (${propReviews.size}):", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = themeColors.accent)
                    propReviews.forEach { rev ->
                        Card(
                            colors = CardDefaults.cardColors(containerColor = themeColors.surface),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(8.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(rev.userName, fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                    Row {
                                        repeat(rev.rating.toInt()) { Text("★", fontSize = 8.sp, color = themeColors.accent) }
                                    }
                                }
                                Text(rev.comment, fontSize = 10.sp, color = themeColors.textSecondary)
                            }
                        }
                    }

                    // Add comment form
                    Card(
                        colors = CardDefaults.cardColors(containerColor = themeColors.surface),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text("➕ أضف استفسارك أو تقييمك للعقار:", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.White)
                            OutlinedTextField(
                                value = userCommentInput,
                                onValueChange = { userCommentInput = it },
                                placeholder = { Text("اكتب سؤالك أو تعليقك...", fontSize = 11.sp, color = themeColors.textSecondary) },
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true,
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedTextColor = Color.White,
                                    unfocusedTextColor = Color.White
                                )
                            )
                            Button(
                                onClick = {
                                    if (userCommentInput.isNotEmpty()) {
                                        viewModel.addRating(
                                            RatingEntity(
                                                targetId = property.id,
                                                targetType = "PROPERTY",
                                                userId = currentUserId,
                                                userName = currentUserName.ifEmpty { "عميل عقارات" },
                                                rating = userRatingInput,
                                                comment = userCommentInput
                                            )
                                        )
                                        userCommentInput = ""
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = themeColors.accent),
                                modifier = Modifier.align(Alignment.End)
                            ) {
                                Text("إرسال", color = Color.Black, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
            }
        }
    }
}
