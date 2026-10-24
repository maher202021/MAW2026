package com.serviseyem.ui.screens.dashboard.components

import androidx.compose.foundation.background
import com.serviseyem.ui.*
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddAPhoto
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.serviseyem.ui.components.SmartAsyncImage
import com.serviseyem.utils.VisualThemePalette
import kotlinx.coroutines.launch

@Composable
fun UnifiedProfileSection(
    title: String,
    subtitle: String = "",
    phone: String = "",
    cityArea: String = "",
    photoUrl: String = "",
    coverUrl: String = "",
    rating: Double = 5.0,
    reviewCount: Int = 0,
    isAvailable: Boolean = true,
    themeColors: VisualThemePalette,
    onChangePhoto: ((String) -> Unit)? = null,
    onChangeCover: ((String) -> Unit)? = null
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val coroutineScope = androidx.compose.runtime.rememberCoroutineScope()

    var isUploadingPhoto by androidx.compose.runtime.remember { androidx.compose.runtime.mutableStateOf(false) }
    var isUploadingCover by androidx.compose.runtime.remember { androidx.compose.runtime.mutableStateOf(false) }

    val profilePhotoPicker = androidx.activity.compose.rememberLauncherForActivityResult(
        contract = androidx.activity.result.contract.ActivityResultContracts.GetContent()
    ) { uri: android.net.Uri? ->
        if (uri != null && onChangePhoto != null) {
            coroutineScope.launch {
                isUploadingPhoto = true
                val path = "profiles/${System.currentTimeMillis()}_avatar.webp"
                val result = com.serviseyem.utils.FirebaseStorageUploader.uploadImageToStorage(context, uri, path)
                isUploadingPhoto = false
                result.onSuccess { downloadUrl ->
                    onChangePhoto(downloadUrl)
                    android.widget.Toast.makeText(context, "تم رفع وتحديث الصورة الشخصية بنجاح ✅", android.widget.Toast.LENGTH_SHORT).show()
                }.onFailure { err ->
                    android.widget.Toast.makeText(context, "فشل رفع الصورة: ${err.message ?: "خطأ في الاتصال"}", android.widget.Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    val coverPhotoPicker = androidx.activity.compose.rememberLauncherForActivityResult(
        contract = androidx.activity.result.contract.ActivityResultContracts.GetContent()
    ) { uri: android.net.Uri? ->
        if (uri != null && onChangeCover != null) {
            coroutineScope.launch {
                isUploadingCover = true
                val path = "covers/${System.currentTimeMillis()}_cover.webp"
                val result = com.serviseyem.utils.FirebaseStorageUploader.uploadImageToStorage(context, uri, path)
                isUploadingCover = false
                result.onSuccess { downloadUrl ->
                    onChangeCover(downloadUrl)
                    android.widget.Toast.makeText(context, "تم رفع وتحديث صورة الغلاف بنجاح ✅", android.widget.Toast.LENGTH_SHORT).show()
                }.onFailure { err ->
                    android.widget.Toast.makeText(context, "فشل رفع الغلاف: ${err.message ?: "خطأ في الاتصال"}", android.widget.Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = themeColors.surface),
        shape = RoundedCornerShape(16.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.08f))
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(110.dp)
                    .background(Color.Black.copy(alpha = 0.3f))
            ) {
                if (coverUrl.isNotBlank()) {
                    SmartAsyncImage(
                        model = coverUrl,
                        contentDescription = "Cover",
                        modifier = Modifier.fillMaxSize()
                    )
                }

                if (onChangeCover != null) {
                    Surface(
                        color = Color.Black.copy(alpha = 0.6f),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(8.dp)
                    ) {
                        TextButton(
                            onClick = { if (!isUploadingCover) coverPhotoPicker.launch("image/*") },
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                            enabled = !isUploadingCover
                        ) {
                            if (isUploadingCover) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(12.dp),
                                        color = Color.White,
                                        strokeWidth = 1.5.dp
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("جاري الرفع... ⏳", fontSize = 10.sp, color = Color.White)
                                }
                            } else {
                                Text("تغيير الغلاف 🖼️", fontSize = 10.sp, color = Color.White)
                            }
                        }
                    }
                }

                Box(
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(start = 16.dp, bottom = 8.dp)
                        .size(64.dp)
                        .clip(CircleShape)
                        .border(2.dp, themeColors.accent, CircleShape)
                        .background(Color.Black),
                    contentAlignment = Alignment.Center
                ) {
                    if (photoUrl.isNotBlank()) {
                        SmartAsyncImage(
                            model = photoUrl,
                            contentDescription = "Profile Photo",
                            modifier = Modifier.fillMaxSize()
                        )
                    } else {
                        Text(text = "👤", fontSize = 28.sp)
                    }
                    if (onChangePhoto != null) {
                        IconButton(
                            onClick = { if (!isUploadingPhoto) profilePhotoPicker.launch("image/*") },
                            enabled = !isUploadingPhoto,
                            modifier = Modifier
                                .fillMaxSize()
                                .background(Color.Black.copy(alpha = 0.3f))
                        ) {
                            if (isUploadingPhoto) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(22.dp),
                                    color = themeColors.accent,
                                    strokeWidth = 2.dp
                                )
                            } else {
                                Icon(
                                    imageVector = Icons.Default.AddAPhoto,
                                    contentDescription = "تغيير الصورة",
                                    tint = Color.White,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    }
                }
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = title,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = themeColors.textPrimary
                    )

                    Surface(
                        color = if (isAvailable) Color(0xFF10B981) else Color(0xFFEF4444),
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Text(
                            text = if (isAvailable) "متاح الآن 🟢" else "مشغول / مغلق 🔴",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                        )
                    }
                }

                if (subtitle.isNotBlank()) {
                    Text(text = subtitle, fontSize = 12.sp, color = themeColors.textSecondary)
                }

                val formattedRating = String.format(java.util.Locale.US, "%.1f", rating)
                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (phone.isNotBlank()) {
                        Text(text = "📞 $phone", fontSize = 11.sp, color = themeColors.accent)
                    }
                    if (cityArea.isNotBlank()) {
                        Text(text = "📍 $cityArea", fontSize = 11.sp, color = themeColors.textSecondary)
                    }
                    Text(text = "⭐ $formattedRating ($reviewCount تقييم)", fontSize = 11.sp, color = Color(0xFFFFA000))
                }
            }
        }
    }
}
