package com.serviseyem.ui

import com.serviseyem.utils.*
import com.serviseyem.ui.*
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
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
import com.serviseyem.utils.VisualThemePalette
import com.serviseyem.data.ProductAttachment
import java.util.UUID

@Composable
fun ProductAttachmentsSection(
    attachments: List<ProductAttachment>,
    onAttachmentsChanged: (List<ProductAttachment>) -> Unit,
    mode: String, // "REGISTRATION", "MANAGEMENT", "VISITOR_VIEW"
    themeColors: VisualThemePalette,
    departmentType: String = "GENERAL"
) {
    val context = LocalContext.current
    var showAddDialog by remember { mutableStateOf(false) }
    var selectedType by remember { mutableStateOf("PDF") }
    var customFileName by remember { mutableStateOf("") }

    val filePicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            val name = customFileName.ifBlank { "document_${System.currentTimeMillis()}.${selectedType.lowercase()}" }
            val newAtt = ProductAttachment(
                id = UUID.randomUUID().toString(),
                type = selectedType,
                url = uri.toString(),
                fileName = name,
                size = 1024 * 750L,
                mimeType = when (selectedType) {
                    "PDF" -> "application/pdf"
                    "IMAGE" -> "image/jpeg"
                    else -> "*/*"
                }
            )
            onAttachmentsChanged(attachments + newAtt)
            showAddDialog = false
            Toast.makeText(context, "تم رفع الملف بنجاح 📁", Toast.LENGTH_SHORT).show()
        }
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        colors = CardDefaults.cardColors(containerColor = themeColors.surface),
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.dp, themeColors.accent.copy(alpha = 0.4f))
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            when (mode) {
                "REGISTRATION" -> {
                    val titleText = when (departmentType.uppercase()) {
                        "MEDICAL" -> "🏥 المستندات والشهادات الطبية (اختياري)"
                        "RESTAURANT" -> "🍽️ قائمة الوجبات أو المنيو (اختياري)"
                        "PROPERTY" -> "🏢 عقود أو صكوك العقارات (اختياري)"
                        "STORE" -> "🛍️ كاتالوج المنتجات (اختياري)"
                        else -> "📁 المستندات والملفات التعريفية (اختياري)"
                    }
                    Text(
                        titleText,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = themeColors.accent
                    )
                    Text(
                        "يمكنك إرفاق ملف PDF أو صور توضيحية لتعزيز ملفك لدى العملاء.",
                        fontSize = 10.sp,
                        color = Color.LightGray
                    )

                    Button(
                        onClick = { showAddDialog = true },
                        colors = ButtonDefaults.buttonColors(containerColor = themeColors.accent),
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("👉 [+ رفع ملف]", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }

                    Text(
                        "📌 الملفات المرفوعة (${attachments.size}):",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )

                    if (attachments.isEmpty()) {
                        Text("(لا يوجد ملفات بعد)", fontSize = 9.sp, color = Color.Gray)
                    } else {
                        attachments.forEach { att ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(Color(0xFF0F172A))
                                    .padding(8.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Text(
                                        when (att.type) {
                                            "EXCEL", "CSV" -> "📊"
                                            "PDF" -> "📄"
                                            "IMAGE" -> "🖼️"
                                            else -> "📋"
                                        },
                                        fontSize = 14.sp
                                    )
                                    Column {
                                        Text(att.fileName, fontSize = 10.sp, color = Color.White, fontWeight = FontWeight.Bold)
                                        Text("${att.type} - ${(att.size / 1024)}KB", fontSize = 8.sp, color = Color.Gray)
                                    }
                                }
                                IconButton(
                                    onClick = { onAttachmentsChanged(attachments.filter { it.id != att.id }) },
                                    modifier = Modifier.size(24.dp)
                                ) {
                                    Icon(Icons.Default.Delete, contentDescription = "حذف", tint = Color.Red, modifier = Modifier.size(16.dp))
                                }
                            }
                        }
                    }
                }

                "MANAGEMENT" -> {
                    Text(
                        "📦 إدارة مرفقات المنتجات",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = themeColors.accent
                    )
                    Text(
                        "قم بإضافة/تعديل/حذف ملفات منتجاتك. هذه الملفات ستظهر للزوار في صفحة ملفك الشخصي.",
                        fontSize = 10.sp,
                        color = Color.LightGray
                    )

                    Button(
                        onClick = { showAddDialog = true },
                        colors = ButtonDefaults.buttonColors(containerColor = themeColors.accent),
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("📁 [+ رفع ملف جديد]", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }

                    Text(
                        "📂 ملفاتي الحالية (${attachments.size}):",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )

                    if (attachments.isEmpty()) {
                        Text("لا توجد ملفات مرفوعة حالياً. اضغط على الزر أعلاه لإضافة المنيو أو الكتالوج.", fontSize = 10.sp, color = Color.Gray)
                    } else {
                        attachments.forEach { att ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(Color(0xFF0F172A))
                                    .padding(8.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Text(
                                        when (att.type) {
                                            "EXCEL", "CSV" -> "📊"
                                            "PDF" -> "📄"
                                            "IMAGE" -> "🖼️"
                                            else -> "📋"
                                        },
                                        fontSize = 16.sp
                                    )
                                    Column {
                                        Text(att.fileName, fontSize = 11.sp, color = Color.White, fontWeight = FontWeight.Bold)
                                        Text("${att.type} • ${att.size / 1024}KB", fontSize = 9.sp, color = Color.Gray)
                                    }
                                }

                                Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                                    TextButton(
                                        onClick = {
                                            Toast.makeText(context, "👁️ جاري فتح الملف: ${att.fileName}", Toast.LENGTH_SHORT).show()
                                        },
                                        contentPadding = PaddingValues(horizontal = 4.dp, vertical = 2.dp)
                                    ) {
                                        Text("[👁️ عرض]", fontSize = 9.sp, color = themeColors.accent)
                                    }
                                    TextButton(
                                        onClick = {
                                            Toast.makeText(context, "📥 جاري تحميل الملف: ${att.fileName}", Toast.LENGTH_SHORT).show()
                                        },
                                        contentPadding = PaddingValues(horizontal = 4.dp, vertical = 2.dp)
                                    ) {
                                        Text("[📥 تحميل]", fontSize = 9.sp, color = Color(0xFF38BDF8))
                                    }
                                    TextButton(
                                        onClick = { onAttachmentsChanged(attachments.filter { it.id != att.id }) },
                                        contentPadding = PaddingValues(horizontal = 4.dp, vertical = 2.dp)
                                    ) {
                                        Text("[🗑️ حذف]", fontSize = 9.sp, color = Color.Red)
                                    }
                                }
                            }
                        }
                    }

                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(8.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text("⚠️ ملاحظة:", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = themeColors.accent)
                            Text("• ملفات Excel/JSON ستظهر كجدول قابل للتصفح في ملفك الشخصي.", fontSize = 9.sp, color = Color.LightGray)
                            Text("• ملفات PDF ستظهر كملف قابل للتحميل.", fontSize = 9.sp, color = Color.LightGray)
                            Text("• الصور ستظهر في معرض الصور الخاص بمنتجاتك.", fontSize = 9.sp, color = Color.LightGray)
                        }
                    }
                }

                "VISITOR_VIEW" -> {
                    Text(
                        "🛒 منتجاتنا/خدماتنا",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = themeColors.accent
                    )
                    Text("تعرف على قائمة منتجاتنا/خدماتنا:", fontSize = 10.sp, color = Color.LightGray)

                    val pdfs = attachments.filter { it.type == "PDF" }
                    val excels = attachments.filter { it.type == "EXCEL" || it.type == "CSV" }
                    val images = attachments.filter { it.type == "IMAGE" }

                    if (pdfs.isNotEmpty() || excels.isNotEmpty()) {
                        Text("📄 الكتالوجات والملفات:", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.White)
                        pdfs.forEach { p ->
                            Button(
                                onClick = {
                                    Toast.makeText(context, "📥 جاري تحميل المنيو (PDF)...", Toast.LENGTH_SHORT).show()
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E293B)),
                                modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text("👉 [تحميل المنيو (PDF)] (${p.fileName} - ${(p.size / 1024)}KB)", fontSize = 10.sp, color = themeColors.accent, fontWeight = FontWeight.Bold)
                            }
                        }
                        excels.forEach { ex ->
                            Button(
                                onClick = {
                                    Toast.makeText(context, "📥 جاري تحميل قائمة الأسعار (Excel)...", Toast.LENGTH_SHORT).show()
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E293B)),
                                modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text("👉 [تحميل قائمة الأسعار (Excel)] (${ex.fileName} - ${(ex.size / 1024)}KB)", fontSize = 10.sp, color = Color(0xFF10B981), fontWeight = FontWeight.Bold)
                            }
                        }
                    }

                    if (attachments.isEmpty()) {
                        Text("لا توجد ملفات أو مرفقات مضافة حالياً من قبل صاحب المنشأة.", fontSize = 10.sp, color = Color.Gray)
                    }

                    if (images.isNotEmpty()) {
                        Text("🖼️ معرض صور المنتجات:", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.White)
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            items(images) { img ->
                                Box(
                                    modifier = Modifier
                                        .size(65.dp)
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(Color(0xFF1E293B)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Text("🖼️", fontSize = 18.sp)
                                        Text(img.fileName, fontSize = 7.sp, color = Color.White, maxLines = 1)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (showAddDialog) {
        AlertDialog(
            onDismissRequest = { showAddDialog = false },
            title = { Text("📁 رفع مرفق جديد", fontSize = 14.sp, fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("اختر صيغة وصنف الملف المطلوب رفعـه:", fontSize = 11.sp, color = Color.Gray)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        listOf("EXCEL" to "📊 Excel", "PDF" to "📄 PDF", "IMAGE" to "🖼️ صورة", "JSON" to "📋 JSON").forEach { (type, lbl) ->
                            FilterChip(
                                selected = selectedType == type,
                                onClick = { selectedType = type },
                                label = { Text(lbl, fontSize = 9.sp) }
                            )
                        }
                    }
                    OutlinedTextField(
                        value = customFileName,
                        onValueChange = { customFileName = it },
                        label = { Text("اسم الملف (مثال: menu.pdf أو prices.xlsx)") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val mime = when (selectedType) {
                            "PDF" -> "application/pdf"
                            "EXCEL", "CSV" -> "*/*"
                            "IMAGE" -> "image/*"
                            else -> "*/*"
                        }
                        filePicker.launch(mime)
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = themeColors.accent)
                ) {
                    Text("اختر من هاتفـك 📱", color = Color.Black, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddDialog = false }) {
                    Text("إلغاء")
                }
            }
        )
    }
}

@Composable
fun SpecialOffersSection(
    offersJson: String,
    onOffersChanged: (String) -> Unit,
    isEditable: Boolean,
    themeColors: VisualThemePalette
) {
    val context = LocalContext.current
    var offersList by remember(offersJson) { mutableStateOf(com.serviseyem.data.SpecialOfferEntity.parseList(offersJson)) }
    var showAddDialog by remember { mutableStateOf(false) }

    var titleInput by remember { mutableStateOf("") }
    var descInput by remember { mutableStateOf("") }
    var percentInput by remember { mutableStateOf("15") }
    var originalPriceInput by remember { mutableStateOf("10000") }
    var offerPriceInput by remember { mutableStateOf("8500") }
    var expiryInput by remember { mutableStateOf("2026-12-31") }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        colors = CardDefaults.cardColors(containerColor = themeColors.surface),
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.dp, themeColors.accent.copy(alpha = 0.4f))
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "🔥 العروض والتخفيضات الحصرية",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = themeColors.accent,
                    modifier = Modifier.weight(1f, fill = false),
                    maxLines = 1
                )
                if (isEditable) {
                    Button(
                        onClick = { showAddDialog = true },
                        colors = ButtonDefaults.buttonColors(containerColor = themeColors.accent),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Icon(Icons.Default.Add, null, tint = Color.Black, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            "+ إضافة عرض جديد",
                            color = Color.Black,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            softWrap = false
                        )
                    }
                }
            }

            if (offersList.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text("لا توجد عروض أو تخفيضات مضافة حالياً 🏷️", fontSize = 11.sp, color = Color.Gray)
                }
            } else {
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(offersList) { offer ->
                        Card(
                            colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
                            border = BorderStroke(1.dp, themeColors.accent.copy(alpha = 0.3f)),
                            modifier = Modifier.width(220.dp)
                        ) {
                            Column(
                                modifier = Modifier.padding(10.dp),
                                verticalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Surface(
                                        color = Color.Red,
                                        shape = RoundedCornerShape(6.dp)
                                    ) {
                                        Text(
                                            "خصم ${offer.discountPercent}%",
                                            color = Color.White,
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold,
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                        )
                                    }
                                    if (isEditable) {
                                        IconButton(
                                            onClick = {
                                                val updated = offersList.filter { it.id != offer.id }
                                                offersList = updated
                                                onOffersChanged(com.serviseyem.data.SpecialOfferEntity.serializeList(updated))
                                                Toast.makeText(context, "تم حذف العرض 🗑️", Toast.LENGTH_SHORT).show()
                                            },
                                            modifier = Modifier.size(24.dp)
                                        ) {
                                            Icon(Icons.Default.Delete, null, tint = Color.Red, modifier = Modifier.size(16.dp))
                                        }
                                    }
                                }

                                Text(
                                    offer.title.ifEmpty { "عرض خاص محدود" },
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )

                                if (offer.description.isNotEmpty()) {
                                    Text(
                                        offer.description,
                                        fontSize = 10.sp,
                                        color = Color.LightGray,
                                        maxLines = 2
                                    )
                                }

                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    if (offer.originalPrice > 0) {
                                        Text(
                                            "${offer.originalPrice.toInt()} ر.ي",
                                            fontSize = 10.sp,
                                            color = Color.Gray,
                                            style = androidx.compose.ui.text.TextStyle(textDecoration = androidx.compose.ui.text.style.TextDecoration.LineThrough)
                                        )
                                    }
                                    Text(
                                        "${offer.offerPrice.toInt()} ر.ي",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = themeColors.accent
                                    )
                                }

                                if (offer.expiryDate.isNotEmpty()) {
                                    Text(
                                        "⏳ ينتهي في: ${offer.expiryDate}",
                                        fontSize = 9.sp,
                                        color = Color.Yellow
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (showAddDialog) {
        androidx.compose.ui.window.Dialog(onDismissRequest = { showAddDialog = false }) {
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFF151C24)),
                border = BorderStroke(1.dp, themeColors.accent),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp)
            ) {
                Column(
                    modifier = Modifier
                        .padding(14.dp)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            "🏷️ إضافة عرض أو تخفيض جديد",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = themeColors.accent
                        )
                        IconButton(
                            onClick = { showAddDialog = false },
                            modifier = Modifier.size(24.dp)
                        ) {
                            Icon(Icons.Default.Clear, contentDescription = "إغلاق", tint = Color.Gray, modifier = Modifier.size(16.dp))
                        }
                    }

                    // Title with Preset Chips
                    Text("اسم العرض أو عنوان التخفيض:", fontSize = 10.sp, color = Color.LightGray)
                    OutlinedTextField(
                        value = titleInput,
                        onValueChange = { titleInput = it },
                        placeholder = { Text("مثال: خصم الصيف 20%", fontSize = 11.sp, color = Color.Gray) },
                        modifier = Modifier.fillMaxWidth(),
                        textStyle = androidx.compose.ui.text.TextStyle(color = Color.White, fontSize = 11.sp),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White, focusedBorderColor = themeColors.accent)
                    )

                    // Quick Preset Title Chips
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        listOf("خصم خاص 🔥", "تخفيض الموسم 🏷️", "عرض محدود ⏳", "اشتري 1 واحصل على 1 🎁").forEach { preset ->
                            Surface(
                                onClick = { titleInput = preset },
                                shape = RoundedCornerShape(12.dp),
                                color = if (titleInput == preset) themeColors.accent else Color.White.copy(alpha = 0.08f),
                                border = BorderStroke(0.5.dp, themeColors.accent.copy(alpha = 0.4f))
                            ) {
                                Text(
                                    preset,
                                    fontSize = 9.5.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (titleInput == preset) Color.Black else Color.White,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        }
                    }

                    // Discount Percentage Selector Chips
                    Text("حدد نسبة الخصم %:", fontSize = 10.sp, color = Color.LightGray)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        listOf("10", "20", "30", "50").forEach { pct ->
                            val isSel = percentInput == pct
                            Surface(
                                onClick = {
                                    percentInput = pct
                                    val orig = originalPriceInput.toDoubleOrNull() ?: 10000.0
                                    val dis = pct.toDoubleOrNull() ?: 10.0
                                    offerPriceInput = (orig * (1 - dis / 100)).toInt().toString()
                                },
                                shape = RoundedCornerShape(10.dp),
                                color = if (isSel) Color.Red else Color(0xFF222C36),
                                modifier = Modifier.weight(1f)
                            ) {
                                Box(contentAlignment = Alignment.Center, modifier = Modifier.padding(vertical = 6.dp)) {
                                    Text("%$pct", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                }
                            }
                        }
                    }

                    // Prices & Calculation Row
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = originalPriceInput,
                            onValueChange = {
                                originalPriceInput = it
                                val orig = it.toDoubleOrNull() ?: 0.0
                                val dis = percentInput.toDoubleOrNull() ?: 0.0
                                if (orig > 0) {
                                    offerPriceInput = (orig * (1 - dis / 100)).toInt().toString()
                                }
                            },
                            label = { Text("السعر الأصلي (ر.ي)", fontSize = 10.sp) },
                            modifier = Modifier.weight(1f),
                            textStyle = androidx.compose.ui.text.TextStyle(color = Color.White, fontSize = 11.sp),
                            colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White)
                        )

                        OutlinedTextField(
                            value = offerPriceInput,
                            onValueChange = { offerPriceInput = it },
                            label = { Text("السعر بعد الخصم (ر.ي)", fontSize = 10.sp) },
                            modifier = Modifier.weight(1f),
                            textStyle = androidx.compose.ui.text.TextStyle(color = themeColors.accent, fontSize = 11.sp, fontWeight = FontWeight.Bold),
                            colors = OutlinedTextFieldDefaults.colors(focusedTextColor = themeColors.accent, unfocusedTextColor = themeColors.accent)
                        )
                    }

                    // Details
                    OutlinedTextField(
                        value = descInput,
                        onValueChange = { descInput = it },
                        label = { Text("ملاحظات وشروط العرض (اختياري)", fontSize = 10.sp) },
                        modifier = Modifier.fillMaxWidth(),
                        textStyle = androidx.compose.ui.text.TextStyle(color = Color.White, fontSize = 11.sp),
                        colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White)
                    )

                    // Expiry Presets
                    Text("مدة العرض / تاريخ الانتهاء:", fontSize = 10.sp, color = Color.LightGray)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        listOf("اليوم فقط ⏳", "3 أيام 📅", "أسبوع كامل 🗓️", "حتى نفاد الكمية 📦").forEach { expPreset ->
                            Surface(
                                onClick = { expiryInput = expPreset },
                                shape = RoundedCornerShape(10.dp),
                                color = if (expiryInput == expPreset) Color(0xFFFF9800) else Color.White.copy(alpha = 0.08f)
                            ) {
                                Text(
                                    expPreset,
                                    fontSize = 9.5.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (expiryInput == expPreset) Color.Black else Color.White,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        }
                    }

                    // Action buttons
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 4.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = { showAddDialog = false },
                            colors = ButtonDefaults.buttonColors(containerColor = Color.DarkGray),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("إلغاء", color = Color.White, fontSize = 11.sp)
                        }

                        Button(
                            onClick = {
                                val newOffer = com.serviseyem.data.SpecialOfferEntity(
                                    id = java.util.UUID.randomUUID().toString(),
                                    title = titleInput.ifBlank { "عرض خاص" },
                                    description = descInput,
                                    discountPercent = percentInput.toIntOrNull() ?: 10,
                                    originalPrice = originalPriceInput.toDoubleOrNull() ?: 0.0,
                                    offerPrice = offerPriceInput.toDoubleOrNull() ?: 0.0,
                                    expiryDate = expiryInput.ifBlank { "حتى نفاد الكمية" }
                                )
                                val updated = offersList + newOffer
                                offersList = updated
                                onOffersChanged(com.serviseyem.data.SpecialOfferEntity.serializeList(updated))
                                showAddDialog = false
                                Toast.makeText(context, "✅ تم نشر العرض بنجاح في التطبيق!", Toast.LENGTH_SHORT).show()
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = themeColors.accent),
                            modifier = Modifier.weight(1.5f)
                        ) {
                            Text("حفظ ونشر العرض 🚀", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                        }
                    }
                }
            }
        }
    }
}
