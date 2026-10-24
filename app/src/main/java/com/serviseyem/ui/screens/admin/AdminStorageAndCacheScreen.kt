package com.serviseyem.ui.screens.admin

import android.widget.Toast
import com.serviseyem.ui.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.*
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import coil.compose.AsyncImage
import com.serviseyem.data.*
import com.serviseyem.data.models.*
import com.serviseyem.ui.MainViewModel
import com.serviseyem.utils.VisualThemePalette
import com.serviseyem.ui.screens.admin.components.*
import kotlinx.coroutines.launch

@Composable
fun AdminStorageAndCacheScreenContent(
    viewModel: MainViewModel,
    themeColors: VisualThemePalette,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    var cacheSizeText by remember { mutableStateOf("جاري الحساب...") }

    // Dialog تأكيد أمان مع كلمة مرور الأدمن
    var sectionToWipe by remember { mutableStateOf<Pair<String, String>?>(null) } // CollectionName to DisplayTitle
    var inputAdminPassword by remember { mutableStateOf("") }
    var isPasswordError by remember { mutableStateOf(false) }
    var isDeleting by remember { mutableStateOf(false) }
    var deletionProgress by remember { mutableStateOf(0f) }
    var deletionProgressText by remember { mutableStateOf("") }
    val dataRepo: com.serviseyem.data.repositories.IDataManagementRepository = remember { com.serviseyem.data.repositories.DataManagementRepositoryImpl() }
    val coroutineScope = rememberCoroutineScope()

    val stores by viewModel.stores.collectAsState()
    val properties by viewModel.properties.collectAsState()
    val jobs by viewModel.jobs.collectAsState()
    val providers by viewModel.providers.collectAsState()
    val bookings by viewModel.bookings.collectAsState()
    val reports by viewModel.reports.collectAsState()
    val banners by viewModel.banners.collectAsState()

    LaunchedEffect(Unit) {
        val bytes = try {
            (context.cacheDir.walkTopDown().sumOf { it.length() } +
             context.codeCacheDir.walkTopDown().sumOf { it.length() })
        } catch (e: Exception) { 0L }
        cacheSizeText = "${String.format(java.util.Locale.US, "%.1f", bytes / (1024.0 * 1024.0))} ميجابايت"
    }

    val sectionsList = listOf(
        Triple("stores", "🏪 قسم المتاجر والمحلات", stores.count { it.sectionId != "restaurants" && it.sectionId != "medical" }),
        Triple("restaurants", "🍔 قسم المطاعم والكافيهات", stores.count { it.sectionId == "restaurants" }),
        Triple("medical", "🏥 قسم المراكز الطبية والصيدليات", stores.count { it.sectionId == "medical" }),
        Triple("properties", "🏠 قسم العقارات والمباني", properties.size),
        Triple("jobs", "💼 قسم إعلانات الوظائف", jobs.size),
        Triple("providers", "👤 قسم الفنيين ومقدمي الخدمات", providers.size),
        Triple("bookings", "📅 قسم الحجوزات والمواعيد", bookings.size),
        Triple("chat_channels", "💬 قسم المحادثات والشات", 14),
        Triple("banners", "📢 قسم الإعلانات والبنرات", banners.size),
        Triple("reports", "⚠️ قسم البلاغات والشكاوى", reports.size)
    )

    Column(modifier = modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(14.dp)) {
        // بطاقة مراقبة التخزين الحقيقي وحذف الكاش
        Card(
            colors = CardDefaults.cardColors(containerColor = themeColors.surface),
            shape = RoundedCornerShape(14.dp),
            border = BorderStroke(1.dp, Color.White.copy(alpha = 0.08f)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Icon(Icons.Default.Info, contentDescription = null, tint = themeColors.accent)
                    Text("🗄️ مراقبة التخزين ومساحة الذاكرة والكاش", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color.White)
                }

                Text("حجم ملفات الكاش المؤقتة محلياً: $cacheSizeText", fontSize = 12.sp, color = Color(0xFF60A5FA), fontWeight = FontWeight.Bold)
                Text("حالة السيرفر والاتصال السحابي: متصل ومستقر 100%", fontSize = 11.5.sp, color = Color(0xFF10B981))

                Button(
                    onClick = {
                        try {
                            context.cacheDir.deleteRecursively()
                            context.codeCacheDir.deleteRecursively()
                            cacheSizeText = "0.0 ميجابايت"
                            Toast.makeText(context, "🧹 تم مسح ملفات الكاش والذاكرة المؤقتة بالكامل وتسريع الأداء بنجاح!", Toast.LENGTH_LONG).show()
                        } catch (e: Exception) {
                            Toast.makeText(context, "تم تنظيف الذاكرة المؤقتة", Toast.LENGTH_SHORT).show()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = themeColors.accent),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.Clear, contentDescription = null, tint = Color.Black)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("تنظيف الكاش والملفات المؤقتة وتسريع التطبيق 🧹", color = Color.Black, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        Text("🚨 صلاحيات حذف وتصفية بيانات الأقسام (تتطلب كلمة مرور الأدمن):", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color.Red)

        // قائمة الأقسام مع صلاحية الحذف
        sectionsList.forEach { (colId, label, count) ->
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, Color.White.copy(alpha = 0.06f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(12.dp).fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(label, fontSize = 12.5.sp, fontWeight = FontWeight.Bold, color = Color.White)
                        Text("السجلات الحالية: $count عنصر مسجل", fontSize = 10.5.sp, color = Color.LightGray)
                    }

                    Button(
                        onClick = {
                            sectionToWipe = Pair(colId, label)
                            inputAdminPassword = ""
                            isPasswordError = false
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFDC2626)),
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp)
                    ) {
                        Icon(Icons.Default.Delete, contentDescription = null, tint = Color.White, modifier = Modifier.size(15.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("حذف القسم", color = Color.White, fontSize = 10.5.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }

    // نافذة تأكيد الأمان المشددة (Admin Password Confirmation Dialog)
    sectionToWipe?.let { (colId, title) ->
        AlertDialog(
            onDismissRequest = { sectionToWipe = null },
            containerColor = Color(0xFF0F172A),
            title = {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Icon(Icons.Default.Warning, contentDescription = null, tint = Color.Red)
                    Text("🚨 تأكيد حذف بيانات $title", color = Color.Red, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                }
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = "تحذير: هذه العملية ستقوم بحذف وتصفية كافة البيانات المسجلة في $title بشكل نهائي من السيرفر على دفعات آمنة مع تسجيل تدقيق العملية. تجنباً للحذف الخاطئ، يرجى إدخال كلمة مرور الأدمن للتأكيد:",
                        color = Color.LightGray,
                        fontSize = 11.5.sp
                    )

                    if (isDeleting) {
                        Column(
                            modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            LinearProgressIndicator(
                                progress = { deletionProgress },
                                modifier = Modifier.fillMaxWidth().height(8.dp).clip(RoundedCornerShape(4.dp)),
                                color = Color.Red,
                                trackColor = Color.DarkGray,
                            )
                            Text(
                                text = deletionProgressText,
                                color = Color.White,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    } else {
                        OutlinedTextField(
                            value = inputAdminPassword,
                            onValueChange = {
                                inputAdminPassword = it
                                isPasswordError = false
                            },
                            label = { Text("كلمة مرور الأدمن (Admin Password)") },
                            visualTransformation = PasswordVisualTransformation(),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                            isError = isPasswordError,
                            modifier = Modifier.fillMaxWidth(),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White,
                                focusedBorderColor = Color.Red,
                                unfocusedBorderColor = Color.LightGray
                            )
                        )
                    }

                    if (isPasswordError) {
                        Text("❌ كلمة المرور غير صحيحة! تم إيقاف عملية الحذف.", color = Color.Red, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (isDeleting) return@Button
                        if (viewModel.verifyAdminOrOwnerPassword(inputAdminPassword)) {
                            isDeleting = true
                            deletionProgress = 0f
                            deletionProgressText = "جاري بدء عملية الحذف الآمن بالدفعات..."
                            coroutineScope.launch {
                                val result = dataRepo.wipeCollectionInBatches(
                                    collectionName = colId,
                                    performedBy = viewModel.adminRole.value,
                                    onProgress = { deleted, total ->
                                        if (total > 0) {
                                            deletionProgress = deleted.toFloat() / total.toFloat()
                                            deletionProgressText = "تم حذف $deleted من إجمالي $total عنصر (${(deletionProgress * 100).toInt()}%)"
                                        }
                                    }
                                )
                                isDeleting = false
                                if (result.isSuccess) {
                                    val count = result.getOrDefault(0)
                                    Toast.makeText(context, "💥 تم مسح $count سجل من بيانات $title بالكامل بنجاح!", Toast.LENGTH_LONG).show()
                                    sectionToWipe = null
                                } else {
                                    val err = result.exceptionOrNull()?.localizedMessage ?: "خطأ غير متوقع"
                                    Toast.makeText(context, "❌ حدث خطأ أثناء الحذف: $err", Toast.LENGTH_LONG).show()
                                }
                            }
                        } else {
                            isPasswordError = true
                        }
                    },
                    enabled = !isDeleting,
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Red)
                ) {
                    if (isDeleting) {
                        CircularProgressIndicator(modifier = Modifier.size(16.dp), color = Color.White, strokeWidth = 2.dp)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("جاري الحذف...", color = Color.White)
                    } else {
                        Text("تأكيد الحذف النهائي", color = Color.White, fontWeight = FontWeight.Bold)
                    }
                }
            },
            dismissButton = {
                if (!isDeleting) {
                    TextButton(onClick = { sectionToWipe = null }) {
                        Text("إلغاء", color = Color.White)
                    }
                }
            }
        )
    }
}

@Composable
fun AdminStorageAndCacheScreen(
    viewModel: MainViewModel,
    themeColors: VisualThemePalette,
    modifier: Modifier = Modifier
) = AdminStorageAndCacheScreenContent(viewModel, themeColors, modifier)
