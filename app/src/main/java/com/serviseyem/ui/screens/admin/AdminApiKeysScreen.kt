package com.serviseyem.ui.screens.admin

// ⚠️ Sensitive keys (banking, payment) must be stored in Cloud Functions Secrets, not Firestore

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
import com.serviseyem.data.repositories.ApiKeyRepositoryImpl
import com.serviseyem.data.repositories.ApiKeysEntity
import com.serviseyem.data.repositories.IApiKeyRepository
import com.serviseyem.ui.MainViewModel
import com.serviseyem.utils.VisualThemePalette
import com.serviseyem.ui.screens.admin.components.*
import kotlinx.coroutines.launch

@Composable
fun AdminApiKeysScreenContent(
    viewModel: MainViewModel,
    themeColors: VisualThemePalette,
    modifier: Modifier = Modifier,
    apiKeyRepository: IApiKeyRepository = remember { ApiKeyRepositoryImpl() }
) {
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current
    val scope = rememberCoroutineScope()

    var geminiApiKey by remember { mutableStateOf("") }
    var openaiApiKey by remember { mutableStateOf("") }
    var selectedAiModel by remember { mutableStateOf("gemini-1.5-flash") }
    var showGeminiKey by remember { mutableStateOf(false) }

    var googleMapsKey by remember { mutableStateOf("") }
    var mapboxKey by remember { mutableStateOf("") }
    var selectedMapEngine by remember { mutableStateOf("OPEN_STREET_MAP") }
    var showMapsKey by remember { mutableStateOf(false) }

    var kuraimiToken by remember { mutableStateOf("") }
    var jawwalPayKey by remember { mutableStateOf("") }
    var floosakKey by remember { mutableStateOf("") }
    var oneCashKey by remember { mutableStateOf("") }

    var webhookUrl by remember { mutableStateOf("") }
    var whatsappToken by remember { mutableStateOf("") }
    var smsGatewayKey by remember { mutableStateOf("") }

    val customKeys = remember { mutableStateListOf<Triple<String, String, String>>() }
    var showAddCustomKeyDialog by remember { mutableStateOf(false) }
    var newKeyName by remember { mutableStateOf("") }
    var newKeyValue by remember { mutableStateOf("") }
    var newKeyEndpoint by remember { mutableStateOf("") }

    LaunchedEffect(Unit) {
        geminiApiKey = apiKeyRepository.getApiKey("geminiApiKey").getOrDefault("")
        openaiApiKey = apiKeyRepository.getApiKey("openaiApiKey").getOrDefault("")
        selectedAiModel = apiKeyRepository.getApiKey("selectedAiModel").getOrDefault("gemini-1.5-flash")
        googleMapsKey = apiKeyRepository.getApiKey("googleMapsKey").getOrDefault("")
        mapboxKey = apiKeyRepository.getApiKey("mapboxKey").getOrDefault("")
        selectedMapEngine = apiKeyRepository.getApiKey("selectedMapEngine").getOrDefault("OPEN_STREET_MAP")
        kuraimiToken = apiKeyRepository.getApiKey("kuraimiToken").getOrDefault("")
        jawwalPayKey = apiKeyRepository.getApiKey("jawwalPayKey").getOrDefault("")
        floosakKey = apiKeyRepository.getApiKey("floosakKey").getOrDefault("")
        oneCashKey = apiKeyRepository.getApiKey("oneCashKey").getOrDefault("")
        webhookUrl = apiKeyRepository.getApiKey("webhookUrl").getOrDefault("")
        whatsappToken = apiKeyRepository.getApiKey("whatsappToken").getOrDefault("")
        smsGatewayKey = apiKeyRepository.getApiKey("smsGatewayKey").getOrDefault("")
    }

    Column(modifier = modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(14.dp)) {
        Text("🔌 إدارة مفاتيح الربط والخدمات السحابية (API Keys)", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = themeColors.accent)
        Text("صلاحية كاملة لإضافة، تعديل، أو حذف مفاتيح API للمساعد الذكي، الخرائط، ومحافظ الدفع والارتباط السحابي:", fontSize = 11.sp, color = themeColors.textSecondary)

        // 1. مفاتيح المساعد الذكي
        Card(
            colors = CardDefaults.cardColors(containerColor = themeColors.surface),
            shape = RoundedCornerShape(14.dp),
            border = BorderStroke(1.dp, Color.White.copy(alpha = 0.08f)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Icon(Icons.Default.AccountBox, contentDescription = null, tint = Color(0xFF60A5FA))
                    Text("🤖 مفاتيح المساعد الذكي (AI Assistant)", fontSize = 13.5.sp, fontWeight = FontWeight.Bold, color = Color.White)
                }

                OutlinedTextField(
                    value = geminiApiKey,
                    onValueChange = { geminiApiKey = it },
                    label = { Text("Google Gemini API Key") },
                    visualTransformation = if (showGeminiKey) VisualTransformation.None else PasswordVisualTransformation(),
                    trailingIcon = {
                        IconButton(onClick = { showGeminiKey = !showGeminiKey }) {
                            Icon(if (showGeminiKey) Icons.Default.Close else Icons.Default.Done, contentDescription = null, tint = Color.Gray)
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White)
                )

                OutlinedTextField(
                    value = openaiApiKey,
                    onValueChange = { openaiApiKey = it },
                    label = { Text("OpenAI / DeepSeek API Key (اختياري)") },
                    visualTransformation = PasswordVisualTransformation(),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White)
                )
            }
        }

        // 2. مفاتيح الخرائط
        Card(
            colors = CardDefaults.cardColors(containerColor = themeColors.surface),
            shape = RoundedCornerShape(14.dp),
            border = BorderStroke(1.dp, Color.White.copy(alpha = 0.08f)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Icon(Icons.Default.LocationOn, contentDescription = null, tint = Color(0xFF34D399))
                    Text("🗺️ مفاتيح الخرائط وتحديد المواقع", fontSize = 13.5.sp, fontWeight = FontWeight.Bold, color = Color.White)
                }

                OutlinedTextField(
                    value = googleMapsKey,
                    onValueChange = { googleMapsKey = it },
                    label = { Text("Google Maps SDK API Key") },
                    visualTransformation = if (showMapsKey) VisualTransformation.None else PasswordVisualTransformation(),
                    trailingIcon = {
                        IconButton(onClick = { showMapsKey = !showMapsKey }) {
                            Icon(if (showMapsKey) Icons.Default.Close else Icons.Default.Done, contentDescription = null, tint = Color.Gray)
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White)
                )

                OutlinedTextField(
                    value = mapboxKey,
                    onValueChange = { mapboxKey = it },
                    label = { Text("Mapbox Access Token (اختياري)") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White)
                )
            }
        }

        // 3. محافظ وبوابات الدفع الإلكتروني اليمنية (مؤمنة سحابياً)
        Card(
            colors = CardDefaults.cardColors(containerColor = themeColors.surface),
            shape = RoundedCornerShape(14.dp),
            border = BorderStroke(1.dp, Color.White.copy(alpha = 0.08f)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Icon(Icons.Default.ShoppingCart, contentDescription = null, tint = Color(0xFFFBBF24))
                    Text("💳 محافظ وبوابات الدفع الإلكتروني", fontSize = 13.5.sp, fontWeight = FontWeight.Bold, color = Color.White)
                }

                Surface(
                    color = Color(0xFF0F172A),
                    shape = RoundedCornerShape(10.dp),
                    border = BorderStroke(1.dp, Color(0xFF10B981).copy(alpha = 0.3f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalAlignment = Alignment.Top
                    ) {
                        Icon(
                            Icons.Default.Lock,
                            contentDescription = null,
                            tint = Color(0xFF10B981),
                            modifier = Modifier.size(24.dp)
                        )
                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text(
                                "🔒 إدارة مشفرة وسحابية (Cloud Functions)",
                                fontSize = 12.5.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF10B981)
                            )
                            Text(
                                "هذه المفاتيح الحساسة (بنك الكريمي، جوال بي، فلوسك، ون كاش) تُدار من Cloud Functions، لا حاجة لإدخالها هنا لضمان أقصى درجات الأمان وحماية البيانات المالية.",
                                fontSize = 11.sp,
                                color = Color.LightGray,
                                lineHeight = 16.sp
                            )
                        }
                    }
                }
            }
        }

        // 4. الارتباط السحابي والويب هوك
        Card(
            colors = CardDefaults.cardColors(containerColor = themeColors.surface),
            shape = RoundedCornerShape(14.dp),
            border = BorderStroke(1.dp, Color.White.copy(alpha = 0.08f)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Icon(Icons.Default.Share, contentDescription = null, tint = Color(0xFFA78BFA))
                    Text("☁️ الارتباط السحابي والويب هوك (Webhooks & SMS)", fontSize = 13.5.sp, fontWeight = FontWeight.Bold, color = Color.White)
                }

                OutlinedTextField(
                    value = webhookUrl,
                    onValueChange = { webhookUrl = it },
                    label = { Text("رابط الخادم السحابي (Cloud Webhook URL)") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White)
                )

                OutlinedTextField(
                    value = whatsappToken,
                    onValueChange = { whatsappToken = it },
                    label = { Text("مفتاح واتساب للأعمال (WhatsApp Cloud API Token)") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White)
                )

                OutlinedTextField(
                    value = smsGatewayKey,
                    onValueChange = { smsGatewayKey = it },
                    label = { Text("مفتاح بوابة الرسائل القصيرة (SMS Gateway Key)") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White)
                )
            }
        }

        // 5. المفاتيح المخصصة
        Card(
            colors = CardDefaults.cardColors(containerColor = themeColors.surface),
            shape = RoundedCornerShape(14.dp),
            border = BorderStroke(1.dp, Color.White.copy(alpha = 0.08f)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("➕ المفاتيح السحابية المخصصة", fontSize = 13.5.sp, fontWeight = FontWeight.Bold, color = Color.White)
                    Button(
                        onClick = { showAddCustomKeyDialog = true },
                        colors = ButtonDefaults.buttonColors(containerColor = themeColors.accent),
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Text("+ إضافة مفتاح جديد", color = Color.Black, fontSize = 10.5.sp, fontWeight = FontWeight.Bold)
                    }
                }

                if (customKeys.isEmpty()) {
                    Text("لا توجد مفاتيح مخصصة مضافة حالياً.", fontSize = 11.sp, color = Color.Gray)
                } else {
                    customKeys.forEachIndexed { index, (name, key, endpoint) ->
                        Surface(
                            color = Color(0xFF1E293B),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(10.dp).fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(name, fontWeight = FontWeight.Bold, fontSize = 12.sp, color = Color.White)
                                    Text("Endpoint: $endpoint", fontSize = 10.sp, color = Color.LightGray)
                                }
                                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                    IconButton(
                                        onClick = {
                                            clipboardManager.setText(AnnotatedString(key))
                                            Toast.makeText(context, "تم نسخ المفتاح للحافظة", Toast.LENGTH_SHORT).show()
                                        },
                                        modifier = Modifier.size(28.dp)
                                    ) {
                                        Icon(Icons.Default.Share, contentDescription = "نسخ", tint = Color.LightGray, modifier = Modifier.size(16.dp))
                                    }
                                    IconButton(
                                        onClick = { customKeys.removeAt(index) },
                                        modifier = Modifier.size(28.dp)
                                    ) {
                                        Icon(Icons.Default.Delete, contentDescription = "حذف", tint = Color.Red, modifier = Modifier.size(16.dp))
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // زر الحفظ والمزامنة الفورية السحابية
        Button(
            onClick = {
                scope.launch {
                    val results = listOf(
                        apiKeyRepository.setApiKey("geminiApiKey", geminiApiKey),
                        apiKeyRepository.setApiKey("openaiApiKey", openaiApiKey),
                        apiKeyRepository.setApiKey("selectedAiModel", selectedAiModel),
                        apiKeyRepository.setApiKey("googleMapsKey", googleMapsKey),
                        apiKeyRepository.setApiKey("mapboxKey", mapboxKey),
                        apiKeyRepository.setApiKey("selectedMapEngine", selectedMapEngine),
                        apiKeyRepository.setApiKey("kuraimiToken", kuraimiToken),
                        apiKeyRepository.setApiKey("jawwalPayKey", jawwalPayKey),
                        apiKeyRepository.setApiKey("floosakKey", floosakKey),
                        apiKeyRepository.setApiKey("oneCashKey", oneCashKey),
                        apiKeyRepository.setApiKey("webhookUrl", webhookUrl),
                        apiKeyRepository.setApiKey("whatsappToken", whatsappToken),
                        apiKeyRepository.setApiKey("smsGatewayKey", smsGatewayKey)
                    )
                    if (results.all { it.isSuccess }) {
                        Toast.makeText(context, "✅ تم حفظ ومزامنة كافة المفاتيح سحابياً فوراً وأمان تام!", Toast.LENGTH_LONG).show()
                    } else {
                        Toast.makeText(context, "❌ حدث خطأ أثناء حفظ بعض المفاتيح، يرجى المحاولة لاحقاً", Toast.LENGTH_SHORT).show()
                    }
                }
            },
            colors = ButtonDefaults.buttonColors(containerColor = themeColors.accent),
            shape = RoundedCornerShape(10.dp),
            modifier = Modifier.fillMaxWidth().height(48.dp)
        ) {
            Icon(Icons.Default.Check, contentDescription = null, tint = Color.Black)
            Spacer(modifier = Modifier.width(8.dp))
            Text("💾 حفظ ومزامنة كافة المفاتيح سحابياً الآن", color = Color.Black, fontSize = 12.5.sp, fontWeight = FontWeight.Bold)
        }
    }

    if (showAddCustomKeyDialog) {
        AlertDialog(
            onDismissRequest = { showAddCustomKeyDialog = false },
            title = { Text("➕ إضافة مفتاح ربط سحابي مخصص", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color.White) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = newKeyName,
                        onValueChange = { newKeyName = it },
                        label = { Text("اسم الخدمة أو البوابة") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = newKeyValue,
                        onValueChange = { newKeyValue = it },
                        label = { Text("مفتاح API السري") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = newKeyEndpoint,
                        onValueChange = { newKeyEndpoint = it },
                        label = { Text("رابط الربط / Endpoint (اختياري)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (newKeyName.isNotBlank() && newKeyValue.isNotBlank()) {
                            customKeys.add(Triple(newKeyName, newKeyValue, newKeyEndpoint))
                            newKeyName = ""
                            newKeyValue = ""
                            newKeyEndpoint = ""
                            showAddCustomKeyDialog = false
                            Toast.makeText(context, "تمت إضافة المفتاح بنجاح", Toast.LENGTH_SHORT).show()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = themeColors.accent)
                ) {
                    Text("إضافة", color = Color.Black, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddCustomKeyDialog = false }) {
                    Text("إلغاء", color = Color.White)
                }
            }
        )
    }
}


@Composable
fun AdminApiKeysScreen(
    viewModel: MainViewModel,
    themeColors: VisualThemePalette,
    modifier: Modifier = Modifier
) = AdminApiKeysScreenContent(viewModel, themeColors, modifier)
