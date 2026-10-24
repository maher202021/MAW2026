@file:OptIn(androidx.compose.foundation.layout.ExperimentalLayoutApi::class, androidx.compose.material3.ExperimentalMaterial3Api::class)

package com.serviseyem.ui.screens.admin
import com.serviseyem.ui.*

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.serviseyem.ui.MainViewModel
import com.serviseyem.utils.AiAssistantEngine
import com.serviseyem.utils.AiResponse
import com.serviseyem.utils.VisualThemePalette
import kotlinx.coroutines.launch

/**
 * 🤖 لوحة إدارة المساعد الذكي وقاموس الصيانة اليمني (Admin Assistant Panel)
 * إشراف وتجربة للمساعد الذكي، اختبار المصطلحات اليمنية (سباكة، كهرباء، طاقة شمسية، تكييف)، وتحديث الردود
 */
@Composable
fun AdminAssistantPanel(
    viewModel: MainViewModel,
    themeColors: VisualThemePalette
) {
    val context = LocalContext.current
    val aiEngine = remember { AiAssistantEngine(context) }
    val coroutineScope = rememberCoroutineScope()

    var testQuery by remember { mutableStateOf("") }
    var testResult by remember { mutableStateOf<AiResponse?>(null) }
    var selectedCategoryFilter by remember { mutableStateOf("الكل") }

    val categories = listOf("الكل", "سباكة", "كهرباء", "طاقة شمسية", "تكييف", "سيارات", "عقارات", "مراكز طبية")

    val sampleYemeniPrompts = listOf(
        "الدينمو ما يرفعش ماء ويطلع صوت هواء",
        "البزبوز يقطر والماصورة مكسورة",
        "الانفرتر يصيح وطلع كود Fault 04",
        "البطارية الجل تفضي بسرعة",
        "المكيف يثلج والماصورة مجمدة",
        "القاطع نزل وشورت في فيش المكيف",
        "المكيف الصحراوي ماطوره يزن"
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Header Card
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = themeColors.surface),
            border = BorderStroke(1.dp, Color(0xFF10B981).copy(alpha = 0.3f)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF10B981).copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.Info, contentDescription = null, tint = Color(0xFF10B981), modifier = Modifier.size(22.dp))
                    }
                    Column {
                        Text("إدارة المساعد الذكي وقاموس الصيانة 🇾🇪", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = Color.White)
                        Text("100+ دليل صيانة مجرب متوافق مع اللهجة والمصطلحات اليمنية", fontSize = 11.sp, color = Color.LightGray)
                    }
                }

                // Stats Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    StatBox(title = "أدلة السباكة", count = "25", color = Color(0xFF00E5FF), modifier = Modifier.weight(1f))
                    StatBox(title = "أدلة الكهرباء", count = "25", color = Color(0xFFF59E0B), modifier = Modifier.weight(1f))
                    StatBox(title = "الطاقة الشمسية", count = "25", color = Color(0xFF10B981), modifier = Modifier.weight(1f))
                    StatBox(title = "التكييف والتبريد", count = "25", color = Color(0xFFEC4899), modifier = Modifier.weight(1f))
                }
            }
        }

        // Test Interactive Search
        Card(
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = themeColors.surface),
            border = BorderStroke(1.dp, Color.DarkGray),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("🧪 مختبر فحص استجابة المساعد الذكي للمصطلحات المحلية:", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.White)

                OutlinedTextField(
                    value = testQuery,
                    onValueChange = { testQuery = it },
                    placeholder = { Text("اكتب مشكلة باللهجة اليمنية (مثال: بزبوز يقطر، انفرتر 04...)", fontSize = 11.sp) },
                    trailingIcon = {
                        IconButton(
                            onClick = {
                                if (testQuery.isNotBlank()) {
                                    coroutineScope.launch {
                                        aiEngine.queryAssistant(testQuery) { resp ->
                                            testResult = resp
                                        }
                                    }
                                }
                            }
                        ) {
                            Icon(Icons.Default.PlayArrow, contentDescription = "فحص", tint = Color(0xFF10B981))
                        }
                    },
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                // Quick Prompt Chips
                LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    items(sampleYemeniPrompts) { prompt ->
                        AssistChip(
                            onClick = {
                                testQuery = prompt
                                coroutineScope.launch {
                                    aiEngine.queryAssistant(prompt) { resp ->
                                        testResult = resp
                                    }
                                }
                            },
                            label = { Text(prompt, fontSize = 10.sp, color = Color.White) },
                            colors = AssistChipDefaults.assistChipColors(containerColor = Color(0xFF1E293B)),
                            border = BorderStroke(0.5.dp, Color.Gray)
                        )
                    }
                }
            }
        }

        // Result Card if any
        testResult?.let { result ->
            Card(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
                border = BorderStroke(1.dp, Color(0xFF10B981)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(result.title, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Color(0xFF10B981))
                        result.estimatedCost?.let { cost ->
                            Text("💰 $cost", fontSize = 11.sp, color = Color(0xFFF59E0B), fontWeight = FontWeight.Bold)
                        }
                    }

                    Text(result.message, fontSize = 12.sp, color = Color.White)

                    if (result.diySteps.isNotEmpty()) {
                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text("📋 خطوات الفحص والإصلاح (DIY):", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF00E5FF))
                            result.diySteps.forEachIndexed { idx, step ->
                                Text("${idx + 1}. $step", fontSize = 11.sp, color = Color(0xFFCBD5E1))
                            }
                        }
                    }

                    result.preventiveTips?.let { tip ->
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color(0xFFF59E0B).copy(alpha = 0.12f))
                                .padding(8.dp)
                        ) {
                            Text("💡 نصيحة وقائية: $tip", fontSize = 11.sp, color = Color(0xFFFCD34D), fontWeight = FontWeight.Medium)
                        }
                    }

                    result.chipLabel?.let { chip ->
                        Button(
                            onClick = { Toast.makeText(context, "إجراء متصل: $chip", Toast.LENGTH_SHORT).show() },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981)),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(chip, color = Color.Black, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun StatBox(title: String, count: String, color: Color, modifier: Modifier = Modifier) {
    Card(
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(containerColor = color.copy(alpha = 0.12f)),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.padding(vertical = 8.dp, horizontal = 4.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(count, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = color)
            Text(title, fontSize = 9.sp, color = Color.LightGray, maxLines = 1, textAlign = TextAlign.Center)
        }
    }
}
