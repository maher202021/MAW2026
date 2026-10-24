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

@Composable
fun AdminAutoRoutingScreenContent(
    viewModel: MainViewModel,
    themeColors: VisualThemePalette,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var isSaving by remember { mutableStateOf(false) }

    var isGeneralAutoRoutingEnabled by remember { mutableStateOf(true) }

    // القطاعات المخصصة: المحلات، المراكز التجارية، المطاعم، المراكز الطبية، وخدمات التوصيل
    var isStoresRoutingEnabled by remember { mutableStateOf(true) }
    var storesCriteria by remember { mutableStateOf("الأقرب جغرافياً (GPS)") }
    var storesRadiusKm by remember { mutableStateOf("15") }

    var isMallsRoutingEnabled by remember { mutableStateOf(true) }
    var mallsCriteria by remember { mutableStateOf("الأقرب داخل نفس المدينة") }

    var isRestaurantsRoutingEnabled by remember { mutableStateOf(true) }
    var restaurantsCriteria by remember { mutableStateOf("أقرب مطعم مع أسرع تحضير") }
    var restaurantsRadiusKm by remember { mutableStateOf("10") }

    var isMedicalRoutingEnabled by remember { mutableStateOf(true) }
    var medicalCriteria by remember { mutableStateOf("أقرب طوارئ وتوفر التخصص") }

    var isDeliveryRoutingEnabled by remember { mutableStateOf(true) }
    var deliveryCriteria by remember { mutableStateOf("إسناد تلقائي لأقرب مندوب شاغر") }
    var deliveryMaxRadiusKm by remember { mutableStateOf("25") }

    LaunchedEffect(Unit) {
        viewModel.db.collection("settings").document("auto_routing").get()
            .addOnSuccessListener { doc ->
                if (doc != null && doc.exists()) {
                    isGeneralAutoRoutingEnabled = doc.getBoolean("isGeneralAutoRoutingEnabled") ?: true
                    isStoresRoutingEnabled = doc.getBoolean("isStoresRoutingEnabled") ?: true
                    isMallsRoutingEnabled = doc.getBoolean("isMallsRoutingEnabled") ?: true
                    isRestaurantsRoutingEnabled = doc.getBoolean("isRestaurantsRoutingEnabled") ?: true
                    isMedicalRoutingEnabled = doc.getBoolean("isMedicalRoutingEnabled") ?: true
                    isDeliveryRoutingEnabled = doc.getBoolean("isDeliveryRoutingEnabled") ?: true
                    storesCriteria = doc.getString("storesCriteria") ?: "الأقرب جغرافياً (GPS)"
                    restaurantsCriteria = doc.getString("restaurantsCriteria") ?: "أقرب مطعم مع أسرع تحضير"
                    medicalCriteria = doc.getString("medicalCriteria") ?: "أقرب طوارئ وتوفر التخصص"
                    deliveryCriteria = doc.getString("deliveryCriteria") ?: "إسناد تلقائي لأقرب مندوب شاغر"
                }
            }
    }

    Column(modifier = modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(14.dp)) {
        // بطاقة التحكم الرئيسية مع Checkbox لتفعيل أو إغلاق الخاصية بسهولة
        Card(
            colors = CardDefaults.cardColors(containerColor = themeColors.surface),
            shape = RoundedCornerShape(16.dp),
            border = BorderStroke(2.dp, if (isGeneralAutoRoutingEnabled) themeColors.accent else Color.Gray),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Icon(Icons.Default.LocationOn, contentDescription = null, tint = themeColors.accent, modifier = Modifier.size(24.dp))
                        Text("🧭 خوارزميات التوجيه التلقائي والذكاء", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Color.White)
                    }
                    AdminStatusBadge(
                        text = if (isGeneralAutoRoutingEnabled) "مفعل 🟢" else "معطل ⚪",
                        containerColor = if (isGeneralAutoRoutingEnabled) Color(0xFF10B981) else Color.Gray
                    )
                }

                // مربع اختيار (Checkbox) لتفعيل أو إغلاق الخاصية بسهولة وبشكل مباشر
                Surface(
                    color = if (isGeneralAutoRoutingEnabled) themeColors.accent.copy(alpha = 0.15f) else Color.White.copy(alpha = 0.05f),
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, if (isGeneralAutoRoutingEnabled) themeColors.accent.copy(alpha = 0.4f) else Color.White.copy(alpha = 0.1f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { isGeneralAutoRoutingEnabled = !isGeneralAutoRoutingEnabled }
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Checkbox(
                            checked = isGeneralAutoRoutingEnabled,
                            onCheckedChange = { isGeneralAutoRoutingEnabled = it },
                            colors = CheckboxDefaults.colors(checkedColor = themeColors.accent)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = if (isGeneralAutoRoutingEnabled) "خاصية التوجيه التلقائي مفعلة حالياً" else "خاصية التوجيه التلقائي معطلة",
                                fontSize = 12.5.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isGeneralAutoRoutingEnabled) themeColors.accent else Color.LightGray
                            )
                            Text(
                                text = "مربع الاختيار يمكن الأدمن من تفعيل أو إيقاف التوجيه التلقائي فوراً لكافة القطاعات",
                                fontSize = 10.sp,
                                color = Color.Gray
                            )
                        }
                    }
                }
            }
        }

        Text("🎯 التوجيه الذكي المخصص حسب القطاعات:", fontSize = 13.5.sp, fontWeight = FontWeight.Bold, color = themeColors.accent)

        // 1. قطاع المحلات والمتاجر
        Card(
            colors = CardDefaults.cardColors(containerColor = themeColors.surface),
            shape = RoundedCornerShape(14.dp),
            border = BorderStroke(1.dp, Color.White.copy(alpha = 0.08f)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Checkbox(checked = isStoresRoutingEnabled, onCheckedChange = { isStoresRoutingEnabled = it })
                        Text("🏪 قطاع المحلات والمتاجر", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color.White)
                    }
                    AdminStatusBadge(text = if (isStoresRoutingEnabled) "نشط" else "متوقف", containerColor = if (isStoresRoutingEnabled) Color(0xFF10B981) else Color.Gray)
                }
                if (isStoresRoutingEnabled) {
                    Text("معيار التوجيه: أقرب متجر جغرافي (GPS) مع فحص توفر البضائع", fontSize = 11.sp, color = Color.LightGray)
                    OutlinedTextField(
                        value = storesRadiusKm,
                        onValueChange = { storesRadiusKm = it },
                        label = { Text("نطاق البحث الجغرافي للمحلات (كيلومتر)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp)
                    )
                }
            }
        }

        // 2. قطاع المراكز التجارية والأسواق
        Card(
            colors = CardDefaults.cardColors(containerColor = themeColors.surface),
            shape = RoundedCornerShape(14.dp),
            border = BorderStroke(1.dp, Color.White.copy(alpha = 0.08f)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Checkbox(checked = isMallsRoutingEnabled, onCheckedChange = { isMallsRoutingEnabled = it })
                        Text("🏢 قطاع المراكز التجارية والأسواق", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color.White)
                    }
                    AdminStatusBadge(text = if (isMallsRoutingEnabled) "نشط" else "متوقف", containerColor = if (isMallsRoutingEnabled) Color(0xFF10B981) else Color.Gray)
                }
                if (isMallsRoutingEnabled) {
                    Text("معيار التوجيه: التوجيه للمركز التجاري الأقرب داخل نفس المدينة والأكثر شمولاً", fontSize = 11.sp, color = Color.LightGray)
                }
            }
        }

        // 3. قطاع المطاعم والكافيهات
        Card(
            colors = CardDefaults.cardColors(containerColor = themeColors.surface),
            shape = RoundedCornerShape(14.dp),
            border = BorderStroke(1.dp, Color.White.copy(alpha = 0.08f)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Checkbox(checked = isRestaurantsRoutingEnabled, onCheckedChange = { isRestaurantsRoutingEnabled = it })
                        Text("🍔 قطاع المطاعم والكافيهات", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color.White)
                    }
                    AdminStatusBadge(text = if (isRestaurantsRoutingEnabled) "نشط" else "متوقف", containerColor = if (isRestaurantsRoutingEnabled) Color(0xFF10B981) else Color.Gray)
                }
                if (isRestaurantsRoutingEnabled) {
                    Text("معيار التوجيه: أقرب مطعم للحي السكني مع سرعة التجهيز التقديرية والتوصيل المباشر", fontSize = 11.sp, color = Color.LightGray)
                    OutlinedTextField(
                        value = restaurantsRadiusKm,
                        onValueChange = { restaurantsRadiusKm = it },
                        label = { Text("أقصى مسافة لتوجيه طلبات الأطعمة (كم)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp)
                    )
                }
            }
        }

        // 4. قطاع المراكز الطبية والعيادات
        Card(
            colors = CardDefaults.cardColors(containerColor = themeColors.surface),
            shape = RoundedCornerShape(14.dp),
            border = BorderStroke(1.dp, Color.White.copy(alpha = 0.08f)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Checkbox(checked = isMedicalRoutingEnabled, onCheckedChange = { isMedicalRoutingEnabled = it })
                        Text("🏥 قطاع المراكز الطبية والعيادات", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color.White)
                    }
                    AdminStatusBadge(text = if (isMedicalRoutingEnabled) "نشط" else "متوقف", containerColor = if (isMedicalRoutingEnabled) Color(0xFF10B981) else Color.Gray)
                }
                if (isMedicalRoutingEnabled) {
                    Text("معيار التوجيه: التوجيه الفوري لأقرب مجمع طبي طارئ، مع مراعاة التخصص المطلوب والمناوبة", fontSize = 11.sp, color = Color.LightGray)
                }
            }
        }

        // 5. خدمات التوصيل والمندوبين
        Card(
            colors = CardDefaults.cardColors(containerColor = themeColors.surface),
            shape = RoundedCornerShape(14.dp),
            border = BorderStroke(1.dp, Color.White.copy(alpha = 0.08f)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Checkbox(checked = isDeliveryRoutingEnabled, onCheckedChange = { isDeliveryRoutingEnabled = it })
                        Text("🛵 خدمات التوصيل والمندوبين", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color.White)
                    }
                    AdminStatusBadge(text = if (isDeliveryRoutingEnabled) "نشط" else "متوقف", containerColor = if (isDeliveryRoutingEnabled) Color(0xFF10B981) else Color.Gray)
                }
                if (isDeliveryRoutingEnabled) {
                    Text("معيار التوجيه: إسناد الطلب تلقائياً لأقرب كابتن توصيل متوفر مع تتبع مسار GPS وزمن الوصول", fontSize = 11.sp, color = Color.LightGray)
                    OutlinedTextField(
                        value = deliveryMaxRadiusKm,
                        onValueChange = { deliveryMaxRadiusKm = it },
                        label = { Text("أقصى نطاق تغطية لأسطول التوصيل (كم)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp)
                    )
                }
            }
        }

        Button(
            onClick = {
                if (isSaving) return@Button
                isSaving = true
                val config = mapOf(
                    "isGeneralAutoRoutingEnabled" to isGeneralAutoRoutingEnabled,
                    "isStoresRoutingEnabled" to isStoresRoutingEnabled,
                    "storesRadiusKm" to storesRadiusKm,
                    "isMallsRoutingEnabled" to isMallsRoutingEnabled,
                    "isRestaurantsRoutingEnabled" to isRestaurantsRoutingEnabled,
                    "restaurantsRadiusKm" to restaurantsRadiusKm,
                    "isMedicalRoutingEnabled" to isMedicalRoutingEnabled,
                    "isDeliveryRoutingEnabled" to isDeliveryRoutingEnabled,
                    "deliveryMaxRadiusKm" to deliveryMaxRadiusKm,
                    "updatedAt" to System.currentTimeMillis()
                )
                viewModel.db.collection("settings").document("auto_routing")
                    .set(config)
                    .addOnSuccessListener {
                        isSaving = false
                        Toast.makeText(context, "✅ تم حفظ وتطبيق خوارزميات التوجيه الذكي سحابياً!", Toast.LENGTH_SHORT).show()
                    }
                    .addOnFailureListener { err ->
                        isSaving = false
                        Toast.makeText(context, "❌ فشل حفظ خوارزميات التوجيه: ${err.localizedMessage}", Toast.LENGTH_LONG).show()
                    }
            },
            enabled = !isSaving,
            colors = ButtonDefaults.buttonColors(containerColor = themeColors.accent),
            shape = RoundedCornerShape(10.dp),
            modifier = Modifier.fillMaxWidth().height(48.dp)
        ) {
            if (isSaving) {
                CircularProgressIndicator(modifier = Modifier.size(18.dp), color = Color.Black, strokeWidth = 2.dp)
                Spacer(modifier = Modifier.width(6.dp))
                Text("جاري الحفظ...", color = Color.Black, fontSize = 12.5.sp, fontWeight = FontWeight.Bold)
            } else {
                Icon(Icons.Default.Check, contentDescription = null, tint = Color.Black)
                Spacer(modifier = Modifier.width(8.dp))
                Text("💾 حفظ وتطبيق خوارزميات التوجيه سحابياً", color = Color.Black, fontSize = 12.5.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}


@Composable
fun AdminAutoRoutingScreen(
    viewModel: MainViewModel,
    themeColors: VisualThemePalette,
    modifier: Modifier = Modifier
) = AdminAutoRoutingScreenContent(viewModel, themeColors, modifier)
