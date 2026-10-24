package com.serviseyem.ui.screens.dashboard.components

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.result.PickVisualMediaRequest
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
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
import com.serviseyem.ui.components.SmartAsyncImage
import com.serviseyem.utils.VisualThemePalette
import com.serviseyem.utils.FirebaseStorageUploader
import android.widget.Toast
import kotlinx.coroutines.launch

@Composable
fun UnifiedImagePicker(
    currentImageUrl: String,
    storagePath: String = "uploads/${System.currentTimeMillis()}_item.webp",
    label: String = "اختيار صورة",
    themeColors: VisualThemePalette,
    onImageSelected: (String) -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    var isUploading by remember { mutableStateOf(false) }

    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        if (uri != null) {
            coroutineScope.launch {
                isUploading = true
                val path = storagePath.ifEmpty { "products/${System.currentTimeMillis()}_item.webp" }
                val result = FirebaseStorageUploader.uploadImageToStorage(context, uri, path)
                isUploading = false
                result.onSuccess { url ->
                    onImageSelected(url)
                    Toast.makeText(context, "تم رفع صورة العنصر بنجاح ✅", Toast.LENGTH_SHORT).show()
                }.onFailure { err ->
                    Toast.makeText(context, "فشل رفع الصورة: ${err.message ?: "خطأ في الاتصال"}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

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
            Text(
                text = label,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = themeColors.textPrimary
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(70.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color.Black.copy(alpha = 0.2f))
                        .border(1.dp, themeColors.accent.copy(alpha = 0.3f), RoundedCornerShape(8.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    if (isUploading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(28.dp),
                            color = themeColors.accent,
                            strokeWidth = 2.dp
                        )
                    } else if (currentImageUrl.isNotBlank()) {
                        SmartAsyncImage(
                            model = currentImageUrl,
                            contentDescription = label,
                            modifier = Modifier.fillMaxSize()
                        )
                    } else {
                        Text(text = "🖼️", fontSize = 24.sp)
                    }
                }

                Button(
                    onClick = { imagePickerLauncher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)) },
                    colors = ButtonDefaults.buttonColors(containerColor = themeColors.accent),
                    shape = RoundedCornerShape(8.dp),
                    enabled = !isUploading
                ) {
                    if (isUploading) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            CircularProgressIndicator(modifier = Modifier.size(14.dp), color = Color.Black, strokeWidth = 2.dp)
                            Text("جاري الرفع...", fontSize = 11.sp, color = Color.Black, fontWeight = FontWeight.Bold)
                        }
                    } else {
                        Text(
                            text = if (currentImageUrl.isNotBlank()) "تغيير الصورة 🖼️" else "رفع / اختيار صورة 🖼️",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.Black
                        )
                    }
                }
            }
        }
    }
}
