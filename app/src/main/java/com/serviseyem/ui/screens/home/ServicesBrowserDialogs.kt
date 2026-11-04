@file:OptIn(androidx.compose.foundation.layout.ExperimentalLayoutApi::class, androidx.compose.material3.ExperimentalMaterial3Api::class)
package com.serviseyem.ui.screens.home

import android.content.Context
import com.serviseyem.ui.*
import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import coil.compose.AsyncImage
import com.serviseyem.data.CityEntity
import com.serviseyem.data.JobEntity
import com.serviseyem.data.PropertyEntity
import com.serviseyem.data.StoreEntity
import com.serviseyem.data.CategoryEntity
import com.serviseyem.ui.screens.home.extensions.isMedicalCenter
import com.serviseyem.ui.screens.home.extensions.isRestaurantOrCafe
import com.serviseyem.ui.MainViewModel
import com.serviseyem.utils.VisualThemePalette

/**
 * 🛠️ ServicesBrowserDialogs - النوافذ الحوارية لتفاصيل المتاجر والعقارات والتصفية المتقدمة
 */

@Composable
fun FilterPanelDialog(
    cities: List<CityEntity>,
    selectedCityId: String,
    onSelectCity: (String) -> Unit,
    categories: List<CategoryEntity> = emptyList(),
    selectedCategoryId: String? = null,
    onSelectCategory: (String?) -> Unit = {},
    isVipOnly: Boolean,
    onToggleVip: (Boolean) -> Unit,
    isAvailableOnly: Boolean,
    onToggleAvailable: (Boolean) -> Unit,
    radiusKm: Int,
    onRadiusChange: (Int) -> Unit,
    neighborhood: String,
    onNeighborhoodChange: (String) -> Unit,
    themeColors: VisualThemePalette,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A)),
            shape = RoundedCornerShape(20.dp),
            border = BorderStroke(1.5.dp, Color.White.copy(alpha = 0.15f)),
            modifier = Modifier.fillMaxWidth().padding(8.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(18.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("⚙️ تصفية وبحث ذكي 🔍", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.White)
                    IconButton(onClick = onDismiss, modifier = Modifier.size(24.dp)) {
                        Icon(Icons.Default.Close, contentDescription = "إغلاق", tint = Color.White.copy(alpha = 0.6f))
                    }
                }

                // 1. Categories
                if (categories.isNotEmpty()) {
                    Text("الأقسام المتاحة", fontSize = 12.sp, color = Color.White.copy(alpha = 0.5f))
                    androidx.compose.foundation.layout.FlowRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        val isAll = selectedCategoryId.isNullOrEmpty()
                        FilterChip(
                            selected = isAll,
                            onClick = { onSelectCategory(null) },
                            label = { Text("⭐ كل الأقسام", fontSize = 10.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = themeColors.accent,
                                selectedLabelColor = Color.Black
                            )
                        )
                        categories.forEach { cat ->
                            val isSelected = selectedCategoryId == cat.id
                            FilterChip(
                                selected = isSelected,
                                onClick = { onSelectCategory(cat.id) },
                                label = { Text(cat.name, fontSize = 10.sp) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = themeColors.accent,
                                    selectedLabelColor = Color.Black
                                )
                            )
                        }
                    }
                    Divider(color = Color.White.copy(alpha = 0.1f))
                }

                // 2. Cities
                Text("الموقع (المحافظة)", fontSize = 12.sp, color = Color.White.copy(alpha = 0.5f))
                androidx.compose.foundation.layout.FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    val cityNames = listOf("صنعاء", "عدن", "تعز", "إب", "الحديدة", "حضرموت", "ذمار", "مأرب", "صعدة", "حجة")
                    cityNames.forEach { cityName ->
                        val isSelected = selectedCityId == cityName
                        FilterChip(
                            selected = isSelected,
                            onClick = { onSelectCity(cityName) },
                            label = { Text(cityName, fontSize = 10.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = themeColors.accent,
                                selectedLabelColor = Color.Black
                            )
                        )
                    }
                }
                
                Divider(color = Color.White.copy(alpha = 0.1f))

                // Neighborhood Input
                OutlinedTextField(
                    value = neighborhood,
                    onValueChange = onNeighborhoodChange,
                    label = { Text("البحث في حي محدد (مثال: حي هايل)", fontSize = 11.sp) },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedBorderColor = themeColors.accent,
                        unfocusedBorderColor = Color.White.copy(alpha = 0.2f)
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                // Switches
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("الفنيون المميزون (VIP)", fontSize = 12.sp, color = Color.White, fontWeight = FontWeight.Bold)
                        Text("عرض النخبة المعتمدين فقط", fontSize = 10.sp, color = Color.Gray)
                    }
                    Switch(
                        checked = isVipOnly,
                        onCheckedChange = onToggleVip,
                        colors = SwitchDefaults.colors(checkedThumbColor = themeColors.accent)
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("المتاحون حالياً 🟢", fontSize = 12.sp, color = Color.White, fontWeight = FontWeight.Bold)
                        Text("عرض من هم في وضع الخدمة الآن", fontSize = 10.sp, color = Color.Gray)
                    }
                    Switch(
                        checked = isAvailableOnly,
                        onCheckedChange = onToggleAvailable,
                        colors = SwitchDefaults.colors(checkedThumbColor = themeColors.accent)
                    )
                }

                Button(
                    onClick = onDismiss,
                    colors = ButtonDefaults.buttonColors(containerColor = themeColors.accent),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth().height(48.dp)
                ) {
                    Text("تطبيق الفلاتر والبحث ✅", color = Color.Black, fontWeight = FontWeight.ExtraBold, fontSize = 14.sp)
                }
            }
        }
    }
}

@Composable
fun StoreQuickDetailsDialog(
    store: StoreEntity,
    context: Context,
    themeColors: VisualThemePalette,
    onDismiss: () -> Unit,
    onOpenDetails: () -> Unit = {}
) {
    val isRest = store.isRestaurantOrCafe()
    val isMed = store.isMedicalCenter()
    val typeIcon = when {
        isMed -> "🏥"
        isRest -> "🍽️"
        else -> "🏪"
    }
    val typeTitle = when {
        isMed -> "مركز طبي / عيادة"
        isRest -> "مطعم / كافيه"
        else -> "متجر تجاري"
    }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A)),
            shape = RoundedCornerShape(16.dp),
            border = BorderStroke(1.5.dp, themeColors.accent),
            modifier = Modifier.fillMaxWidth().padding(8.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(14.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("$typeIcon ${store.name}", fontSize = 13.5.sp, fontWeight = FontWeight.Bold, color = themeColors.accent)
                    IconButton(onClick = onDismiss, modifier = Modifier.size(24.dp)) {
                        Icon(Icons.Default.Close, contentDescription = "إغلاق", tint = Color.Red)
                    }
                }

                if (store.coverImage.isNotEmpty()) {
                    AsyncImage(
                        model = store.coverImage,
                        contentDescription = store.name,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(130.dp)
                            .clip(RoundedCornerShape(10.dp)),
                        contentScale = ContentScale.Crop
                    )
                }

                Text("نوع المنشأة: $typeTitle", fontSize = 11.5.sp, color = themeColors.accent, fontWeight = FontWeight.SemiBold)
                if (store.ownerName.isNotEmpty()) {
                    Text("المالك / المدير: ${store.ownerName}", fontSize = 11.5.sp, color = Color.White)
                }
                Text("الموقع: ${store.cityId} - ${store.localNeighborhood}", fontSize = 11.sp, color = Color.LightGray)
                if (store.workingHours.isNotEmpty()) {
                    Text("أوقات العمل: ${store.workingHours}", fontSize = 10.5.sp, color = Color.LightGray)
                }
                if (store.description.isNotEmpty()) {
                    Text(store.description, fontSize = 10.5.sp, color = Color.Gray)
                }

                Button(
                    onClick = onOpenDetails,
                    colors = ButtonDefaults.buttonColors(containerColor = themeColors.accent),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth().height(42.dp)
                ) {
                    Icon(Icons.Default.Info, contentDescription = null, tint = Color.Black)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("🛒 عرض صفحة المتجر والمنتجات بالكامل", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                }

                Button(
                    onClick = {
                        val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:${store.phone}"))
                        context.startActivity(intent)
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981)),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth().height(42.dp)
                ) {
                    Icon(Icons.Default.Phone, contentDescription = null, tint = Color.White)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("اتصال مباشر (${store.phone})", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                }
            }
        }
    }
}

@Composable
fun PropertyQuickDetailsDialog(
    property: PropertyEntity,
    context: Context,
    themeColors: VisualThemePalette,
    onDismiss: () -> Unit
) {
    val propertyTypeArabic = remember(property.propertyType) {
        when (property.propertyType.lowercase()) {
            "apartment", "شقة" -> "شقة فاخرة"
            "house", "villa", "فلة", "منزل" -> "فلة مستقلة"
            "land", "أرض" -> "أرض سكنية / تجارية"
            "shop", "محل" -> "محل تجاري"
            else -> property.propertyType.ifBlank { "عقار سكني" }
        }
    }
    val contractTypeArabic = if (property.type.lowercase() == "sale" || property.type == "بيع") "للبيع" else "للإيجار"

    Dialog(
        onDismissRequest = onDismiss,
        properties = androidx.compose.ui.window.DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Card(
            colors = CardDefaults.cardColors(containerColor = Color(0xFF070A13)),
            shape = RoundedCornerShape(24.dp),
            border = BorderStroke(1.5.dp, Color(0xFFE8B23A)),
            modifier = Modifier
                .fillMaxWidth(0.93f)
                .padding(vertical = 16.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // 1. Header Bar
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("🏡", fontSize = 20.sp)
                        Text(
                            "تفاصيل العقار الشاملة",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color.White
                        )
                    }
                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier
                            .size(32.dp)
                            .background(Color(0xFF1E293B), CircleShape)
                    ) {
                        Icon(Icons.Default.Close, contentDescription = "إغلاق", tint = Color.White, modifier = Modifier.size(18.dp))
                    }
                }

                // 2. Cover Photo with Badges
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                        .clip(RoundedCornerShape(16.dp))
                ) {
                    val imageUrl = property.images.firstOrNull() ?: "https://images.unsplash.com/photo-1582407947304-fd86f028f716?auto=format&fit=crop&w=800&q=80"
                    AsyncImage(
                        model = imageUrl,
                        contentDescription = property.title,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                androidx.compose.ui.graphics.Brush.verticalGradient(
                                    listOf(Color.Transparent, Color(0xDD070A13))
                                )
                            )
                    )
                    Row(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Surface(shape = RoundedCornerShape(8.dp), color = Color(0xFFE8B23A)) {
                            Text(
                                "VIP موثق ⭐",
                                color = Color(0xFF070A13),
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Black,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                        Surface(shape = RoundedCornerShape(8.dp), color = Color(0xFF2563EB)) {
                            Text(
                                contractTypeArabic,
                                color = Color.White,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }
                }

                // 3. Title & Price Tag
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        property.title.ifBlank { "عقار فاخر في موقع ممتاز" },
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Black,
                        color = Color.White
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = com.serviseyem.utils.formatPriceWithCurrency(property.price, property.currency),
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Black,
                            color = Color(0xFFE8B23A)
                        )
                        Text(
                            text = "📍 ${property.cityId} - ${property.localNeighborhood}",
                            fontSize = 12.sp,
                            color = Color(0xFF94A3B8)
                        )
                    }
                }

                Divider(color = Color(0xFF1E293B))

                // 4. Specification Grid (4 Boxes)
                Text("المواصفات الرئيسية للعقار", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color.White)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF121A2C)),
                        border = BorderStroke(1.dp, Color(0xFF1E293B)),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Column(modifier = Modifier.padding(10.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("🛏️", fontSize = 18.sp)
                            Spacer(modifier = Modifier.height(2.dp))
                            Text("عدد الغرف", fontSize = 10.sp, color = Color(0xFF94A3B8))
                            Text("${property.rooms} غرفة", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.White)
                        }
                    }
                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF121A2C)),
                        border = BorderStroke(1.dp, Color(0xFF1E293B)),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Column(modifier = Modifier.padding(10.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("📐", fontSize = 18.sp)
                            Spacer(modifier = Modifier.height(2.dp))
                            Text("المساحة الإجمالية", fontSize = 10.sp, color = Color(0xFF94A3B8))
                            Text("${property.areaSqm.toInt()} م²", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.White)
                        }
                    }
                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF121A2C)),
                        border = BorderStroke(1.dp, Color(0xFF1E293B)),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Column(modifier = Modifier.padding(10.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("🏠", fontSize = 18.sp)
                            Spacer(modifier = Modifier.height(2.dp))
                            Text("نوع العقار", fontSize = 10.sp, color = Color(0xFF94A3B8))
                            Text(propertyTypeArabic, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.White, maxLines = 1)
                        }
                    }
                }

                // 5. Full Description
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF121A2C)),
                    border = BorderStroke(1.dp, Color(0xFF1E293B)),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text("📋 وصف العقار والتفاصيل الكاملة:", fontSize = 12.5.sp, fontWeight = FontWeight.Bold, color = Color(0xFFE8B23A))
                        Text(
                            text = property.description.ifBlank {
                                "عقار فاخر ومميز بتشطيب ديلوكس في موقع استراتيجي هادئ وقريب من الخط العام، يتوفر فيه كافة الخدمات والكهرباء والماء والمدارس."
                            },
                            fontSize = 11.5.sp,
                            color = Color(0xFFCBD5E1),
                            lineHeight = 18.sp
                        )
                    }
                }

                // 6. Key Amenities Checklist
                Text("✨ المميزات والخدمات المتاحة", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color.White)
                androidx.compose.foundation.layout.FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    val amenities = listOf("⚡ كهرباء مستقلة", "💧 خزان مياه خاص", "🚗 موقف سيارات", "🛗 مصعد كهربائي", "🛡️ حراسة 24/7", "🛋️ تشطيب سوبر ديلوكس")
                    amenities.forEach { item ->
                        Surface(
                            shape = RoundedCornerShape(20.dp),
                            color = Color(0xFF1E293B),
                            border = BorderStroke(1.dp, Color(0xFF334155))
                        ) {
                            Text(item, color = Color.White, fontSize = 10.5.sp, fontWeight = FontWeight.SemiBold, modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp))
                        }
                    }
                }

                Divider(color = Color(0xFF1E293B))

                // 7. Owner Info
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFF121A2C), RoundedCornerShape(12.dp))
                        .padding(10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text("صاحب العقار / المعلن:", fontSize = 10.5.sp, color = Color(0xFF94A3B8))
                        Text(property.ownerName.ifBlank { "مالك العقار المعتمد" }, fontSize = 12.5.sp, fontWeight = FontWeight.Bold, color = Color.White)
                    }
                    Surface(shape = RoundedCornerShape(8.dp), color = Color(0xFF10B981).copy(alpha = 0.2f)) {
                        Text("📱 ${property.phone}", color = Color(0xFF10B981), fontSize = 11.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(8.dp))
                    }
                }

                // 8. Action Buttons
                Button(
                    onClick = {
                        val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:${property.phone}"))
                        context.startActivity(intent)
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981)),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth().height(46.dp)
                ) {
                    Icon(Icons.Default.Phone, contentDescription = null, tint = Color.White)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("اتصال مباشر بمالك العقار (${property.phone})", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = {
                            val cleanPhone = property.phone.replace("+", "").replace(" ", "").trim()
                            val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://api.whatsapp.com/send?phone=$cleanPhone"))
                            try { context.startActivity(intent) } catch (e: Exception) {}
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0D9488)),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.weight(1f).height(44.dp)
                    ) {
                        Text("💬 مراسلة واتساب", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 11.5.sp)
                    }

                    Button(
                        onClick = {
                            val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:${property.phone}"))
                            context.startActivity(intent)
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE8B23A)),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.weight(1f).height(44.dp)
                    ) {
                        Text("📅 طلب معاينة", color = Color(0xFF070A13), fontWeight = FontWeight.Black, fontSize = 11.5.sp)
                    }
                }
            }
        }
    }
}

@Composable
fun JobQuickDetailsDialog(
    job: JobEntity,
    context: Context,
    themeColors: VisualThemePalette,
    onDismiss: () -> Unit,
    onApply: () -> Unit = {}
) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A)),
            shape = RoundedCornerShape(16.dp),
            border = BorderStroke(1.5.dp, themeColors.accent),
            modifier = Modifier.fillMaxWidth().padding(8.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(14.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("💼 ${job.title}", fontSize = 13.5.sp, fontWeight = FontWeight.Bold, color = themeColors.accent)
                    IconButton(onClick = onDismiss, modifier = Modifier.size(24.dp)) {
                        Icon(Icons.Default.Close, contentDescription = "إغلاق", tint = Color.Red)
                    }
                }

                Text("جهة العمل: ${job.companyName}", fontSize = 11.5.sp, color = Color.White, fontWeight = FontWeight.Bold)
                Text("الراتب المتوقع: ${job.salary}", fontSize = 11.sp, color = themeColors.accent)
                Text("الموقع والنوع: ${job.cityId} - ${job.jobType}", fontSize = 11.sp, color = Color.LightGray)
                if (job.description.isNotEmpty()) {
                    Text("تفاصيل الوظيفة:\n${job.description}", fontSize = 10.5.sp, color = Color.Gray)
                }

                Button(
                    onClick = {
                        onApply()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = themeColors.accent),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth().height(44.dp)
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, tint = Color.Black)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("التقديم على هذه الوظيفة 📩", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 11.5.sp)
                }

                Button(
                    onClick = {
                        val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:${job.phone}"))
                        context.startActivity(intent)
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981)),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth().height(44.dp)
                ) {
                    Icon(Icons.Default.Phone, contentDescription = null, tint = Color.White)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("اتصال مباشر (${job.phone})", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 11.5.sp)
                }
            }
        }
    }
}
