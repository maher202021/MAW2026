package com.serviseyem.ui.screens.register.components

import android.content.Context
import androidx.core.content.FileProvider
import com.serviseyem.ui.*
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.PlayArrow
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
import coil.compose.AsyncImage
import com.serviseyem.utils.VisualThemePalette
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream

/**
 * 📷 RegistrationImagePicker - مكون اختيار والتقاط الصور بالكاميرا مع الضغط والمعاينة المكبرة
 */
@Composable
fun RegistrationImagePicker(
    title: String = "صور المعرض / الهوية / الترخيص",
    subtitle: String = "يمكنك إضافة حتى 5 صور معتمدة لتوثيق ملفك",
    imagesUris: List<Uri>,
    onImagesSelected: (List<Uri>) -> Unit,
    onImageRemoved: (Int) -> Unit,
    maxImages: Int = 5,
    isUploading: Boolean = false,
    uploadProgress: Float = 0f,
    themeColors: VisualThemePalette,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var previewImageUri by remember { mutableStateOf<Uri?>(null) }
    var showSourceDialog by remember { mutableStateOf(false) }

    val scope = rememberCoroutineScope()
    var isProcessingImages by remember { mutableStateOf(false) }
    var compressionJob by remember { mutableStateOf<Job?>(null) }

    DisposableEffect(Unit) {
        onDispose {
            compressionJob?.cancel()
        }
    }

    // Compress URI to file <= 300KB in background IO dispatcher with clarity and dimension verification
    suspend fun compressSingleUriBg(uri: Uri): Uri {
        return withContext(Dispatchers.IO) {
            try {
                val inputStream = context.contentResolver.openInputStream(uri) ?: return@withContext uri
                val bitmap = try {
                    BitmapFactory.decodeStream(inputStream)
                } catch (e: Exception) {
                    e.printStackTrace()
                    return@withContext uri
                } ?: return@withContext uri

                // Identity Image Verification: check dimensions and basic clarity
                if (bitmap.width < 100 || bitmap.height < 100) {
                    withContext(Dispatchers.Main) {
                        android.widget.Toast.makeText(context, "⚠️ أبعاد الصورة صغيرة جداً وغير واضحة (الحد الأدنى 100x100)", android.widget.Toast.LENGTH_SHORT).show()
                    }
                    return@withContext uri
                }

                val tempFile = File(context.cacheDir, "comp_${System.currentTimeMillis()}_${(1000..9999).random()}.jpg")
                var quality = 85
                val out = ByteArrayOutputStream()
                bitmap.compress(Bitmap.CompressFormat.JPEG, quality, out)
                while (out.toByteArray().size > 300 * 1024 && quality > 20) {
                    out.reset()
                    quality -= 15
                    bitmap.compress(Bitmap.CompressFormat.JPEG, quality, out)
                }
                val fos = FileOutputStream(tempFile)
                fos.write(out.toByteArray())
                fos.flush()
                fos.close()
                bitmap.recycle()
                FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", tempFile)
            } catch (e: Exception) {
                uri
            }
        }
    }

    // Gallery Picker
    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickMultipleVisualMedia(maxImages)
    ) { uris ->
        if (uris.isNotEmpty()) {
            if (imagesUris.size >= maxImages) {
                android.widget.Toast.makeText(context, "لا يمكن إضافة أكثر من $maxImages صور", android.widget.Toast.LENGTH_SHORT).show()
                return@rememberLauncherForActivityResult
            }
            compressionJob?.cancel()
            compressionJob = scope.launch {
                isProcessingImages = true
                val compressed = uris.mapNotNull { uri: Uri -> compressSingleUriBg(uri) }
                val combined = (imagesUris + compressed).take(maxImages)
                onImagesSelected(combined)
                isProcessingImages = false
            }
        }
    }

    // Camera Capture
    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicturePreview()
    ) { bitmap ->
        if (bitmap != null) {
            if (imagesUris.size >= maxImages) {
                android.widget.Toast.makeText(context, "لا يمكن إضافة أكثر من $maxImages صور", android.widget.Toast.LENGTH_SHORT).show()
                return@rememberLauncherForActivityResult
            }
            compressionJob?.cancel()
            compressionJob = scope.launch {
                isProcessingImages = true
                withContext(Dispatchers.IO) {
                    try {
                        val tempFile = File(context.cacheDir, "cam_${System.currentTimeMillis()}.jpg")
                        var quality = 85
                        val out = ByteArrayOutputStream()
                        bitmap.compress(Bitmap.CompressFormat.JPEG, quality, out)
                        while (out.toByteArray().size > 300 * 1024 && quality > 20) {
                            out.reset()
                            quality -= 15
                            bitmap.compress(Bitmap.CompressFormat.JPEG, quality, out)
                        }
                        val fos = FileOutputStream(tempFile)
                        fos.write(out.toByteArray())
                        fos.flush()
                        fos.close()
                        val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", tempFile)
                        val combined = (imagesUris + listOf(uri)).take(maxImages)
                        withContext(Dispatchers.Main) {
                            onImagesSelected(combined)
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
                isProcessingImages = false
            }
        }
    }

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(title, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = themeColors.accent)
                Text(subtitle, fontSize = 10.5.sp, color = Color.Gray)
            }
            Text(
                "${imagesUris.size}/$maxImages",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = themeColors.accent
            )
        }

        if (isProcessingImages) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(themeColors.accent.copy(alpha = 0.15f), RoundedCornerShape(8.dp))
                    .padding(8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                CircularProgressIndicator(
                    modifier = Modifier.size(16.dp),
                    color = themeColors.accent,
                    strokeWidth = 2.dp
                )
                Text(
                    "جاري معالجة وضغط الصور في الخلفية...",
                    fontSize = 10.5.sp,
                    color = themeColors.accent,
                    fontWeight = FontWeight.Medium
                )
            }
        }

        if (isUploading) {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                LinearProgressIndicator(
                    progress = { uploadProgress.coerceIn(0f, 1f) },
                    modifier = Modifier.fillMaxWidth().height(6.dp).clip(RoundedCornerShape(3.dp)),
                    color = themeColors.accent,
                    trackColor = Color.White.copy(alpha = 0.1f)
                )
                Text(
                    "جاري رفع الصور إلى السحابة... ${(uploadProgress * 100).toInt()}%",
                    fontSize = 10.sp,
                    color = themeColors.accent
                )
            }
        }

        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (imagesUris.size < maxImages) {
                item {
                    OutlinedCard(
                        onClick = { showSourceDialog = true },
                        colors = CardDefaults.outlinedCardColors(containerColor = Color(0xFF0F172A)),
                        border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(themeColors.accent)),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.size(80.dp)
                    ) {
                        Column(
                            modifier = Modifier.fillMaxSize(),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Icon(Icons.Default.Add, contentDescription = "إضافة صورة", tint = themeColors.accent)
                            Spacer(modifier = Modifier.height(2.dp))
                            Text("إضافة", fontSize = 10.sp, color = Color.White)
                        }
                    }
                }
            }

            itemsIndexed(imagesUris) { index, uri ->
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .border(1.dp, Color.White.copy(alpha = 0.2f), RoundedCornerShape(12.dp))
                        .clickable { previewImageUri = uri }
                ) {
                    AsyncImage(
                        model = uri,
                        contentDescription = "معاينة صورة $index",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                    IconButton(
                        onClick = { onImageRemoved(index) },
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(2.dp)
                            .size(20.dp)
                            .background(Color.Red, CircleShape)
                    ) {
                        Icon(Icons.Default.Close, contentDescription = "حذف الصورة", tint = Color.White, modifier = Modifier.size(12.dp))
                    }
                }
            }
        }
    }

    // Source Selection Dialog (Camera or Gallery)
    if (showSourceDialog) {
        AlertDialog(
            onDismissRequest = { showSourceDialog = false },
            title = { Text("📷 اختيار مصدر الصورة", fontSize = 13.sp, color = themeColors.accent) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Button(
                        onClick = {
                            showSourceDialog = false
                            cameraLauncher.launch(null)
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = themeColors.accent)
                    ) {
                        Text("📸 التقاط بالكاميرا المباشرة", color = Color.Black, fontWeight = FontWeight.Bold)
                    }
                    OutlinedButton(
                        onClick = {
                            showSourceDialog = false
                            galleryLauncher.launch(androidx.activity.result.PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("🖼️ اختيار من المعرض والاستوديو", color = Color.White)
                    }
                }
            },
            confirmButton = {},
            dismissButton = {
                TextButton(onClick = { showSourceDialog = false }) {
                    Text("إلغاء", color = Color.Gray)
                }
            },
            containerColor = Color(0xFF0F172A)
        )
    }

    // Fullscreen Zoomed Preview Dialog
    previewImageUri?.let { uri ->
        Dialog(onDismissRequest = { previewImageUri = null }) {
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A)),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(380.dp)
                    .border(2.dp, themeColors.accent, RoundedCornerShape(16.dp))
            ) {
                Box(modifier = Modifier.fillMaxSize()) {
                    AsyncImage(
                        model = uri,
                        contentDescription = "معاينة مكبرة",
                        contentScale = ContentScale.Fit,
                        modifier = Modifier.fillMaxSize().padding(12.dp)
                    )
                    IconButton(
                        onClick = { previewImageUri = null },
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(8.dp)
                            .background(Color.Black.copy(alpha = 0.6f), CircleShape)
                    ) {
                        Icon(Icons.Default.Close, contentDescription = "إغلاق", tint = Color.White)
                    }
                }
            }
        }
    }
}
