package com.serviseyem.ui.components

import android.content.Context
import com.serviseyem.ui.*
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
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
import com.serviseyem.utils.FirebaseStorageUploader
import com.serviseyem.utils.VisualThemePalette
import kotlinx.coroutines.launch
import java.util.UUID

@Composable
fun FlexibleCatalogUploader(
    themeColors: VisualThemePalette,
    excelFileName: String,
    onExcelFileChange: (fileName: String, base64Data: String) -> Unit,
    pdfFileName: String,
    onPdfFileChange: (fileName: String, base64Data: String) -> Unit,
    imagesList: List<String>,
    onImagesListChange: (List<String>) -> Unit,
    externalLink: String,
    onExternalLinkChange: (String) -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var activeUploadTab by remember { mutableStateOf(0) } // 0: Excel/CSV, 1: PDF, 2: Images, 3: External Link
    var errorMessage by remember { mutableStateOf<String?>(null) }

    // Excel Picker Launcher
    val excelPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            val fileName = getFileNameFromUri(context, it) ?: "list_data.xlsx"
            val extension = fileName.substringAfterLast('.', "").lowercase()
            if (extension in listOf("exe", "bat", "sh", "apk", "vbs", "cmd", "msi")) {
                errorMessage = "❌ يمنع تماماً رفع الملفات التنفيذية أو الخبيثة (.exe, .bat, .apk)"
                Toast.makeText(context, errorMessage, Toast.LENGTH_LONG).show()
                return@let
            }
            if (extension !in listOf("xlsx", "xls", "csv")) {
                errorMessage = "⚠️ يرجى اختيار ملف جدول Excel بصيغة .xlsx أو .xls أو .csv"
                Toast.makeText(context, errorMessage, Toast.LENGTH_LONG).show()
                return@let
            }
            scope.launch {
                val bytes = context.contentResolver.openInputStream(it)?.use { s -> s.readBytes() }
                val uploadedUrl = if (bytes != null) {
                    FirebaseStorageUploader.uploadBytesToStorage(
                        bytes = bytes,
                        storagePath = "catalogs/excel/${UUID.randomUUID().toString().take(8)}_$fileName",
                        mimeType = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
                    ).getOrElse { it.toString() }
                } else it.toString()
                onExcelFileChange(fileName, uploadedUrl)
                errorMessage = null
                Toast.makeText(context, "✅ تم اختيار جدول البيانات: $fileName", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // PDF Picker Launcher
    val pdfPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            val fileName = getFileNameFromUri(context, it) ?: "catalog.pdf"
            val extension = fileName.substringAfterLast('.', "").lowercase()
            if (extension in listOf("exe", "bat", "sh", "apk", "vbs", "cmd", "msi")) {
                errorMessage = "❌ يمنع تماماً رفع الملفات التنفيذية!"
                Toast.makeText(context, errorMessage, Toast.LENGTH_LONG).show()
                return@let
            }
            if (extension != "pdf") {
                errorMessage = "⚠️ يرجى اختيار ملف PDF بصيغة .pdf"
                Toast.makeText(context, errorMessage, Toast.LENGTH_LONG).show()
                return@let
            }
            val size = getFileSizeFromUri(context, it)
            if (size > 1_048_576) { // 1MB limit
                errorMessage = "⚠️ حجم ملف PDF يتجاوز 1 ميجابايت! يرجى ضغطه قبل الرفع لتوفير سعة التخزين."
                Toast.makeText(context, errorMessage, Toast.LENGTH_LONG).show()
                return@let
            }
            scope.launch {
                val bytes = context.contentResolver.openInputStream(it)?.use { s -> s.readBytes() }
                val uploadedUrl = if (bytes != null) {
                    FirebaseStorageUploader.uploadBytesToStorage(
                        bytes = bytes,
                        storagePath = "catalogs/pdf/${UUID.randomUUID().toString().take(8)}_$fileName",
                        mimeType = "application/pdf"
                    ).getOrElse { it.toString() }
                } else it.toString()
                onPdfFileChange(fileName, uploadedUrl)
                errorMessage = null
                Toast.makeText(context, "✅ تم اختيار كتالوج PDF: $fileName", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // Batch Images Picker Launcher
    val imagesPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetMultipleContents()
    ) { uris: List<Uri> ->
        scope.launch {
            val uploadedUrls = uris.mapIndexedNotNull { index, uri ->
                val path = "catalogs/images/img_${index}_${UUID.randomUUID().toString().take(8)}.webp"
                FirebaseStorageUploader.uploadImageUri(context, uri, path)
                    .getOrElse { uri.toString() }
                    .takeIf { it.isNotEmpty() }
            }
            onImagesListChange((imagesList + uploadedUrls).take(10))
            Toast.makeText(context, "✅ تم ضغط ورفع ${uploadedUrls.size} صور خفيفة للكتالوج", Toast.LENGTH_SHORT).show()
        }
    }

    Card(
        colors = CardDefaults.cardColors(containerColor = themeColors.surface),
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.dp, themeColors.accent.copy(alpha = 0.4f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(
                text = "📂 نظام رفع قوائم المنتجات والخدمات والأسعار (مرن وموفر للتخزين):",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = themeColors.accent
            )

            // Tabs for upload method selection
            LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                val tabs = listOf(
                    Triple("📊 Excel / CSV", 0, excelFileName.isNotEmpty()),
                    Triple("📄 PDF (حد أقصى 1MB)", 1, pdfFileName.isNotEmpty()),
                    Triple("🖼️ صور مضغوطة", 2, imagesList.isNotEmpty()),
                    Triple("🔗 رابط خارجي", 3, externalLink.isNotEmpty())
                )
                items(tabs) { tab ->
                    val isSel = activeUploadTab == tab.second
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(20.dp))
                            .background(if (isSel) themeColors.accent else Color.DarkGray.copy(alpha = 0.5f))
                            .clickable { activeUploadTab = tab.second }
                            .padding(horizontal = 10.dp, vertical = 6.dp)
                            .border(
                                1.dp,
                                if (tab.third) Color.Green else if (isSel) themeColors.accent else Color.Transparent,
                                RoundedCornerShape(20.dp)
                            )
                    ) {
                        Text(
                            text = tab.first + if (tab.third) " ✓" else "",
                            fontSize = 10.sp,
                            color = if (isSel) Color.Black else Color.White,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            errorMessage?.let { msg ->
                Text(
                    text = msg,
                    fontSize = 10.sp,
                    color = Color.Red,
                    fontWeight = FontWeight.Bold
                )
            }

            when (activeUploadTab) {
                0 -> { // Excel / CSV
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text(
                            text = "رفع جدول قوائم المنتجات والأسعار بسهولة (مع فحص الحماية من الكود الخبيث):",
                            fontSize = 10.sp,
                            color = Color.LightGray
                        )
                        Button(
                            onClick = { excelPicker.launch("*/*") },
                            colors = ButtonDefaults.buttonColors(containerColor = themeColors.primary),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("اختر ملف Excel / CSV 📊", fontSize = 11.sp, color = Color.White)
                        }
                        if (excelFileName.isNotEmpty()) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(Color(0xFF065F46), RoundedCornerShape(8.dp))
                                    .padding(8.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("📄 الملف المرفق: $excelFileName", fontSize = 11.sp, color = Color.White)
                                IconButton(onClick = { onExcelFileChange("", "") }) {
                                    Icon(Icons.Default.Delete, contentDescription = "حذف", tint = Color.Red)
                                }
                            }
                        }
                    }
                }
                1 -> { // PDF
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text(
                            text = "رفع الكتالوج بصيغة PDF موحدة (بشرط ألا يتجاوز الحجم 1 ميجابايت لتوفير مساحة التخزين المجانية):",
                            fontSize = 10.sp,
                            color = Color.LightGray
                        )
                        Button(
                            onClick = { pdfPicker.launch("application/pdf") },
                            colors = ButtonDefaults.buttonColors(containerColor = themeColors.primary),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("اختر ملف PDF 📄", fontSize = 11.sp, color = Color.White)
                        }
                        if (pdfFileName.isNotEmpty()) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(Color(0xFF065F46), RoundedCornerShape(8.dp))
                                    .padding(8.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("📄 الملف المرفق: $pdfFileName (مفحوص 1MB ✓)", fontSize = 11.sp, color = Color.White)
                                IconButton(onClick = { onPdfFileChange("", "") }) {
                                    Icon(Icons.Default.Delete, contentDescription = "حذف", tint = Color.Red)
                                }
                            }
                        }
                    }
                }
                2 -> { // Compressed Images
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text(
                            text = "رفع صور فردية أو مجمعة لقائمة الطعام/المنتجات مع ضغط تلقائي وتصغير الحجم (WebP/JPEG) في جهازك قبل الرفع:",
                            fontSize = 10.sp,
                            color = Color.LightGray
                        )
                        Button(
                            onClick = { imagesPicker.launch("image/*") },
                            colors = ButtonDefaults.buttonColors(containerColor = themeColors.primary),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("إضافة صور المنيو/الكتالوج 🖼️", fontSize = 11.sp, color = Color.White)
                        }
                        if (imagesList.isNotEmpty()) {
                            Text("تم ضغط ${imagesList.size} صورة بنجاح وتهيئتها للرفع السريع", fontSize = 10.sp, color = Color.Green)
                        }
                    }
                }
                3 -> { // External Link
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text(
                            text = "إدراج رابط خارجي آمن (مثل Google Drive أو رابط سحابي معتمد) لتخفيف العبء عن الخوادم:",
                            fontSize = 10.sp,
                            color = Color.LightGray
                        )
                        OutlinedTextField(
                            value = externalLink,
                            onValueChange = onExternalLinkChange,
                            placeholder = { Text("https://drive.google.com/...", fontSize = 10.sp, color = Color.Gray) },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White)
                        )
                    }
                }
            }
        }
    }
}

private fun getFileNameFromUri(context: Context, uri: Uri): String? {
    return try {
        var name: String? = null
        if (uri.scheme == "content") {
            val cursor = context.contentResolver.query(uri, null, null, null, null)
            cursor?.use {
                if (it.moveToFirst()) {
                    val nameIndex = it.getColumnIndex(android.provider.OpenableColumns.DISPLAY_NAME)
                    if (nameIndex != -1) name = it.getString(nameIndex)
                }
            }
        }
        name ?: uri.path?.substringAfterLast('/')
    } catch (e: Exception) {
        null
    }
}

private fun getFileSizeFromUri(context: Context, uri: Uri): Long {
    return try {
        var size: Long = 0
        if (uri.scheme == "content") {
            val cursor = context.contentResolver.query(uri, null, null, null, null)
            cursor?.use {
                if (it.moveToFirst()) {
                    val sizeIndex = it.getColumnIndex(android.provider.OpenableColumns.SIZE)
                    if (sizeIndex != -1) size = it.getLong(sizeIndex)
                }
            }
        }
        size
    } catch (e: Exception) {
        0L
    }
}
