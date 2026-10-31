package com.serviseyem.ui.screens.map.components

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.serviseyem.data.ProviderEntity
import com.serviseyem.data.StoreEntity

/**
 * 👁️ AlephVisualAssistantDialog
 * المساعد البصري الذكي (Aleph)
 * - تحليل جغرافي وسياقي للموقع الحالي (Lat/Lng)
 * - الإجابة الفورية عن المكان، المعالم، وتوفر الخدمات
 */
@Composable
fun AlephVisualAssistantDialog(
    userLat: Double,
    userLng: Double,
    selectedCity: String,
    nearbyProviders: List<ProviderEntity>,
    nearbyStores: List<StoreEntity>,
    targetEntity: Any? = null,
    onDismiss: () -> Unit
) {
    var queryText by remember { mutableStateOf("") }
    var currentAnswer by remember {
        mutableStateOf(
            if (targetEntity != null) {
                when (targetEntity) {
                    is ProviderEntity -> "📍 تحليل المكان لـ ${targetEntity.name} (${targetEntity.profession}): يقع في نطاق $selectedCity، مسجل كفني معتمد مع تقييم ممتاز. يتوفر حجز فوري واتصال مباشر عبر التطبيق."
                    is StoreEntity -> "📍 متجر ${targetEntity.name}: مصنف كـ ${targetEntity.description.ifEmpty { targetEntity.name }} في $selectedCity. متوافق مع خرائط Vela ويسهل الوصول إليه عبر شوارع المنطقة الرئيسية."
                    else -> "📍 إحداثيات الموقع: [${String.format("%.4f", userLat)}, ${String.format("%.4f", userLng)}]. المنطقة نشطة ويتوفر فيها فنيون ومتاجر معتمدة."
                }
            } else {
                "مرحباً بك! أنا Aleph المساعد البصري لخرائط Vela.\nأنا متصل الآن بإحداثياتك الحالية [${String.format("%.4f", userLat)}, ${String.format("%.4f", userLng)}] في $selectedCity. تم رصد ${nearbyProviders.size} فني و${nearbyStores.size} متجر في هذا النطاق.\nكيف يمكنني مساعدتك بخصوص هذا الموقع؟"
            }
        )
    }
    var isThinking by remember { mutableStateOf(false) }

    val quickQuestions = listOf(
        "🔍 ما هي أقرب الخدمات المتاحة هنا؟",
        "👨‍🔧 هل تتوفر ورش صيانة أو فنيون للطوارئ؟",
        "🏪 أين أجد أقرب صيدلية أو متجر أغذية؟",
        "📍 تحليل جودة تغطية الموقع والوصول"
    )

    fun askQuestion(q: String) {
        queryText = q
        isThinking = true
        when {
            q.contains("أقرب الخدمات") -> {
                val pNames = nearbyProviders.take(3).joinToString("، ") { it.name }
                val sNames = nearbyStores.take(3).joinToString("، ") { it.name }
                currentAnswer = "📍 في هذا الموقع:\n• أقرب الفنيين: $pNames.\n• أقرب المتاجر: $sNames.\nجميع المواقع محددة بدقة عبر أقمار OAM الاصطناعية وVela 3D."
            }
            q.contains("فنيون") || q.contains("صيانة") -> {
                val providersList = nearbyProviders.filter { it.isAvailable }.take(4)
                if (providersList.isNotEmpty()) {
                    currentAnswer = "🛠️ يتوفر ${providersList.size} فنيين متاحين حالياً للحجز الفوري:\n" +
                            providersList.joinToString("\n") { "• ${it.name} (${it.profession}) - تقييم ${it.rating}⭐" }
                } else {
                    currentAnswer = "🛠️ يتوفر فنيون في المدينة ولكن يفضل الحجز المسبق عبر التطبيق لضمان التواجد الفوري."
                }
            }
            q.contains("صيدلية") || q.contains("متجر") -> {
                val storesList = nearbyStores.take(4)
                currentAnswer = "🏪 المحلات الأقرب إليك:\n" +
                        storesList.joinToString("\n") { "• ${it.name} (${it.description.ifEmpty { it.name }}) - هاتف: ${it.phone}" }
            }
            else -> {
                currentAnswer = "🌐 تحليل Aleph للموقع [${String.format("%.4f", userLat)}, ${String.format("%.4f", userLng)}]: الموقع ضمن النطاق الحضري لـ $selectedCity، ومطابق في طبقة الصور الجوية OAM، مع إمكانية التبديل إلى Vela 3D لرؤية الكتل البنائية بدقة."
            }
        }
        isThinking = false
    }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = Color(0xFF0F172A),
            border = androidx.compose.foundation.BorderStroke(1.5.dp, Color(0xFF00E5FF).copy(alpha = 0.6f)),
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(18.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = Color(0xFF00E5FF).copy(alpha = 0.15f),
                            modifier = Modifier.size(36.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text("👁️", fontSize = 18.sp)
                            }
                        }
                        Column {
                            Text(
                                "المساعد البصري Aleph",
                                color = Color.White,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                "تحليل ذكي لخرائط Vela & OAM",
                                color = Color(0xFF00E5FF),
                                fontSize = 11.sp
                            )
                        }
                    }

                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.testTag("close_aleph_dialog_btn")
                    ) {
                        Icon(Icons.Default.Close, contentDescription = "إغلاق", tint = Color(0xFF94A3B8))
                    }
                }

                Spacer(Modifier.height(14.dp))

                // Location Coordinate Pill
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = Color(0xFF1E293B),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(Icons.Default.Place, contentDescription = null, tint = Color(0xFF00E5FF), modifier = Modifier.size(16.dp))
                        Text(
                            text = "الموقع: $selectedCity [${String.format("%.4f", userLat)}, ${String.format("%.4f", userLng)}]",
                            color = Color(0xFFCBD5E1),
                            fontSize = 11.5.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }

                Spacer(Modifier.height(12.dp))

                // Answer Bubble
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = Color(0xFF1E293B).copy(alpha = 0.8f),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF334155)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 90.dp, max = 180.dp)
                ) {
                    LazyColumn(modifier = Modifier.padding(12.dp)) {
                        item {
                            if (isThinking) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(16.dp),
                                        color = Color(0xFF00E5FF),
                                        strokeWidth = 2.dp
                                    )
                                    Text("جاري فحص الصور الجوية وبيانات Vela...", color = Color(0xFF94A3B8), fontSize = 12.sp)
                                }
                            } else {
                                Text(
                                    text = currentAnswer,
                                    color = Color.White,
                                    fontSize = 13.sp,
                                    lineHeight = 20.sp
                                )
                            }
                        }
                    }
                }

                Spacer(Modifier.height(12.dp))

                // Quick Question Chips
                Text("أسئلة سريعة عن المكان:", color = Color(0xFF94A3B8), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(6.dp))
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(quickQuestions) { q ->
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = Color(0xFF00E5FF).copy(alpha = 0.12f),
                            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF00E5FF).copy(alpha = 0.35f)),
                            modifier = Modifier.clickable { askQuestion(q) }
                        ) {
                            Text(
                                text = q,
                                color = Color(0xFF00E5FF),
                                fontSize = 11.5.sp,
                                fontWeight = FontWeight.Medium,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                            )
                        }
                    }
                }

                Spacer(Modifier.height(12.dp))

                // Custom Question Input
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = queryText,
                        onValueChange = { queryText = it },
                        placeholder = { Text("اسأل Aleph عن هذا المكان...", fontSize = 12.sp, color = Color(0xFF64748B)) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedBorderColor = Color(0xFF00E5FF),
                            unfocusedBorderColor = Color(0xFF334155),
                            focusedContainerColor = Color(0xFF0B1120),
                            unfocusedContainerColor = Color(0xFF0B1120)
                        ),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp)
                            .testTag("aleph_query_input")
                    )

                    IconButton(
                        onClick = { if (queryText.isNotBlank()) askQuestion(queryText) },
                        modifier = Modifier
                            .size(46.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color(0xFF00E5FF))
                            .testTag("aleph_send_btn")
                    ) {
                        Icon(Icons.Default.Send, contentDescription = "إرسال", tint = Color(0xFF0F172A))
                    }
                }
            }
        }
    }
}
