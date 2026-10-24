package com.serviseyem.ui.screens.entities

import androidx.compose.foundation.BorderStroke
import com.serviseyem.ui.*
import androidx.compose.foundation.clickable
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarBorder
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import coil.compose.AsyncImage
import com.serviseyem.data.RatingEntity
import com.serviseyem.utils.VisualThemePalette
import com.serviseyem.utils.ImageOptimizationUtils
import kotlinx.coroutines.launch

@Composable
fun ProfileReviewCard(
    review: RatingEntity,
    themeColors: VisualThemePalette
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = themeColors.surface),
        shape = RoundedCornerShape(10.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(10.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = review.userName.ifEmpty { "عميل معتمد" },
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    fontSize = 12.sp
                )
                Row {
                    val ratingCount = review.rating.toInt().coerceIn(1, 5)
                    repeat(ratingCount) {
                        Icon(
                            imageVector = Icons.Default.Star,
                            contentDescription = null,
                            tint = Color(0xFFFFB300),
                            modifier = Modifier.size(14.dp)
                        )
                    }
                }
            }
            Text(text = review.comment, color = Color.LightGray, fontSize = 11.sp)
            if (review.reply.isNotBlank()) {
                Surface(
                    color = Color.Black.copy(alpha = 0.3f),
                    shape = RoundedCornerShape(6.dp),
                    border = BorderStroke(1.dp, themeColors.accent.copy(alpha = 0.3f)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 4.dp)
                ) {
                    Column(modifier = Modifier.padding(6.dp)) {
                        Text(
                            text = "رد المنشأة / المزود 💬:",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = themeColors.accent
                        )
                        Text(text = review.reply, fontSize = 10.5.sp, color = Color.White)
                    }
                }
            }
        }
    }
}

/**
 * ⚠️ ملاحظة معمارية:
 * هذا المكون (EnhancedReviewInput) معرّف لكنه غير مستخدم حالياً في أي شاشة.
 * يمكن استخدامه مستقبلاً في:
 * - تقييم الطلبات العاجلة مع صور
 * - تقييم مع دعم وسائط
 * 
 * لا تحذفه — قد يحتاج المشروع إليه في مرحلة لاحقة.
 */
@Composable
fun EnhancedReviewInput(
    currentUserId: String = "",
    onSubmit: (rating: Int, comment: String, images: List<String>) -> Unit,
    themeColors: VisualThemePalette
) {
    var rating by remember { mutableStateOf(0) }
    var comment by remember { mutableStateOf("") }
    var selectedImages by remember { mutableStateOf<List<android.net.Uri>>(emptyList()) }
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    
    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetMultipleContents()
    ) { uris ->
        selectedImages = uris
    }
    
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text("قم بتقييم تجربتك 🌟", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color.White)
        
        Row(
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            (1..5).forEach { star ->
                IconButton(
                    onClick = { rating = star },
                    modifier = Modifier.size(40.dp)
                ) {
                    Icon(
                        imageVector = if (star <= rating) Icons.Default.Star else Icons.Default.StarBorder,
                        contentDescription = null,
                        tint = if (star <= rating) Color(0xFFFFD700) else Color.Gray,
                        modifier = Modifier.size(28.dp)
                    )
                }
            }
        }
        
        OutlinedTextField(
            value = comment,
            onValueChange = { comment = it },
            placeholder = { Text("شارك تجربتك مع الآخرين...", fontSize = 12.sp) },
            modifier = Modifier.fillMaxWidth().height(90.dp),
            maxLines = 3,
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White,
                focusedBorderColor = themeColors.accent,
                unfocusedBorderColor = Color.Gray
            )
        )
        
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "📷 أضف صوراً (اختياري)",
                fontSize = 11.sp,
                color = Color.LightGray
            )
            
            Button(
                onClick = { imagePickerLauncher.launch("image/*") },
                colors = ButtonDefaults.buttonColors(containerColor = Color.White.copy(alpha = 0.1f)),
                shape = RoundedCornerShape(8.dp),
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp)
            ) {
                Text("اختر صور 📸", fontSize = 11.sp, color = Color.White)
            }
        }
        
        if (selectedImages.isNotEmpty()) {
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                modifier = Modifier.fillMaxWidth().height(70.dp)
            ) {
                items(selectedImages) { uri ->
                    AsyncImage(
                        model = uri,
                        contentDescription = null,
                        modifier = Modifier
                            .width(60.dp)
                            .fillMaxHeight()
                            .clip(RoundedCornerShape(8.dp))
                    )
                }
            }
        }
        
        Button(
            onClick = {
                if (currentUserId.isBlank() || currentUserId == "guest" || currentUserId.startsWith("guest_")) {
                    android.widget.Toast.makeText(context, "عذراً! التقييم متاح فقط للمستخدمين والفنيين وأصحاب المحلات المسجلين بالنظام.", android.widget.Toast.LENGTH_LONG).show()
                } else if (rating > 0 && comment.isNotBlank()) {
                    coroutineScope.launch {
                        val imageBase64List = selectedImages.map { uri ->
                            ImageOptimizationUtils.compressAndOptimizeImage(
                                context = context,
                                uri = uri,
                                maxWidth = 400,
                                maxHeight = 400,
                                quality = 60,
                                maxSizeKB = 100
                            )
                        }.filter { it.isNotEmpty() }
                        
                        onSubmit(rating, comment, imageBase64List)
                    }
                }
            },
            enabled = rating > 0 && comment.isNotBlank(),
            colors = ButtonDefaults.buttonColors(
                containerColor = if (rating > 0 && comment.isNotBlank()) themeColors.accent else Color.Gray
            ),
            shape = RoundedCornerShape(10.dp),
            modifier = Modifier.fillMaxWidth().height(44.dp)
        ) {
            Text(
                text = "إرسال التقييم ⭐",
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = if (rating > 0 && comment.isNotBlank()) Color.Black else Color.White
            )
        }
    }
}

