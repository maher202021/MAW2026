package com.serviseyem

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.serviseyem.data.*
import com.serviseyem.ui.*
import com.serviseyem.ui.MainViewModel
import com.serviseyem.utils.VisualThemePalette

// --------------------------------------------------------
// 3. PROPERTIES TAB CONTENT & PROPERTY CARD
// --------------------------------------------------------
@Composable
fun PropertiesTabContent(
    viewModel: MainViewModel,
    themeColors: VisualThemePalette,
    onPropertyClick: (PropertyEntity) -> Unit,
    onAddPropertyClick: () -> Unit,
    sectionId: String = "properties"
) {
    val properties by viewModel.properties.collectAsState()
    val cities by viewModel.cities.collectAsState()
    val adminRole by viewModel.adminRole.collectAsState()
    val currentPhone by viewModel.currentUserPhone.collectAsState()
    val currentLang by viewModel.currentLanguage.collectAsState()
    val isEn = currentLang == "en"

    var searchQuery by remember { mutableStateOf("") }
    var selectedCityId by remember { mutableStateOf("") }
    var filterType by remember { mutableStateOf("") } // rent, sale, empty
    var filterPropType by remember { mutableStateOf("") } // apartment, house, land, shop, empty
    var itemsToShowLimit by remember { mutableStateOf(10) }

    val activeProps = remember(properties, searchQuery, selectedCityId, filterType, filterPropType, sectionId) {
        properties.filter {
            !it.isDeleted &&
            (it.sectionId == sectionId || (sectionId == "properties" && (it.sectionId == "properties" || it.sectionId == "realestate" || it.sectionId.isEmpty()))) &&
            (it.isActive || adminRole != "GUEST" || it.ownerId == currentPhone) &&
            (searchQuery.isEmpty() || it.title.contains(searchQuery, true) || it.description.contains(searchQuery, true)) &&
            (selectedCityId.isEmpty() || it.cityId == selectedCityId) &&
            (filterType.isEmpty() || it.type == filterType) &&
            (filterPropType.isEmpty() || it.propertyType == filterPropType)
        }.sortedByDescending { it.isPinned }
    }

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        // Real-time join request status banner for property advertisers (Transitions immediately to full profile view after approval)
        val myProp = properties.find { !it.isDeleted && it.ownerId == currentPhone }
        myProp?.let { prop ->
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = if (prop.isActive) Color(0xFF0F291E) else Color(0xFF2C2414)
                ),
                border = BorderStroke(
                    width = 1.dp,
                    color = if (prop.isActive) Color(0xFF22C55E).copy(alpha = 0.5f) else Color(0xFFEAB308).copy(alpha = 0.5f)
                ),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (prop.isActive) {
                                if (isEn) "✅ Property advertisement successfully activated!" else "✅ تم تفعيل إعلان العقار بنجاح!"
                            } else {
                                if (isEn) "⏳ Property listing request under review" else "⏳ طلب إدراج العقار قيد المراجعة"
                            },
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (prop.isActive) Color(0xFF4ADE80) else Color(0xFFFACC15)
                        )
                        Box(
                            modifier = Modifier
                                .background(
                                    if (prop.isActive) Color(0xFF22C55E).copy(alpha = 0.2f) else Color(0xFFEAB308).copy(alpha = 0.2f),
                                    RoundedCornerShape(4.dp)
                                )
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = if (prop.isActive) {
                                    if (isEn) "Published" else "منشور وعامل"
                                } else {
                                    if (isEn) "Pending" else "قيد التدقيق الإداري"
                                },
                                fontSize = 8.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (prop.isActive) Color(0xFF4ADE80) else Color(0xFFFACC15)
                            )
                        }
                    }
                    Text(
                        text = if (prop.isActive) {
                            if (isEn) "Welcome! Your property advertisement (${prop.title}) was approved and published. You can now receive calls and inquiries from buyers." else "أهلاً بك يا غالي! تم الموافقة على إعلان عقارك (${prop.title}) ونشره رسمياً للجمهور. يمكنك الآن استقبال الاتصالات والاستفسارات من المشترين المهتمين."
                        } else {
                            if (isEn) "Your property listing request (${prop.title}) was submitted. Data and documents are under administrative review, and you will be notified once published." else "تم تقديم طلب إدراج عقارك (${prop.title}) بنجاح. بياناتك والمستندات المرفقة قيد التدقيق والفحص الإداري حالياً من قبل الإدارة لضمان المصداقية وسننبهك فور نشره."
                        },
                        fontSize = 10.sp,
                        color = Color.White.copy(alpha = 0.9f)
                    )
                    Button(
                        onClick = { onPropertyClick(prop) },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (prop.isActive) Color(0xFF22C55E) else Color(0xFFEAB308)
                        ),
                        modifier = Modifier.fillMaxWidth().heightIn(min = 44.dp),
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = if (prop.isActive) {
                                if (isEn) "📂 View & Manage Published Property" else "📂 عرض وإدارة ملف العقار المنشور"
                            } else {
                                if (isEn) "👁️ Preview Listing Details" else "👁️ عرض ومعاينة تفاصيل طلبك وعقد الملكية"
                            },
                            color = Color.Black,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }

        // Search & Filters Header
        Card(
            colors = CardDefaults.cardColors(containerColor = themeColors.surface),
            shape = RoundedCornerShape(12.dp)
        ) {
            Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text(if (isEn) "Search for apartment, building, land..." else "ابحث عن شقة، عمارة، أرض، دكان...", fontSize = 12.sp, color = themeColors.textSecondary) },
                    modifier = Modifier.fillMaxWidth().testTag("property_search_input"),
                    leadingIcon = { Icon(Icons.Default.Search, null, tint = themeColors.textSecondary) },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    )
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    // Type selector (Rent/Sale)
                    val types = listOf(
                        Pair("", if (isEn) "All" else "الكل"),
                        Pair("rent", if (isEn) "For Rent" else "للإيجار"),
                        Pair("sale", if (isEn) "For Sale" else "للبيع")
                    )
                    types.forEach { t ->
                        val isSel = filterType == t.first
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .background(if (isSel) themeColors.accent else Color.Black.copy(alpha = 0.3f), RoundedCornerShape(8.dp))
                                .clickable { filterType = t.first }
                                .padding(vertical = 8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(t.second, fontSize = 10.sp, color = if (isSel) Color.Black else Color.White, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // City selector
                    Box(modifier = Modifier.weight(1f)) {
                        var expanded by remember { mutableStateOf(false) }
                        val cityLabel = if (selectedCityId.isEmpty()) {
                            if (isEn) "All Cities" else "كل المحافظات"
                        } else {
                            val city = cities.find { it.id == selectedCityId }
                            if (isEn) (city?.nameEn ?: city?.nameAr ?: "") else (city?.nameAr ?: "")
                        }
                        Button(
                            onClick = { expanded = true },
                            colors = ButtonDefaults.buttonColors(containerColor = Color.Black.copy(alpha = 0.3f)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(cityLabel, fontSize = 10.sp, color = Color.White)
                        }
                        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                            DropdownMenuItem(
                                text = { Text(if (isEn) "All Cities" else "كل المحافظات") },
                                onClick = { selectedCityId = ""; expanded = false }
                            )
                            cities.forEach { city ->
                                DropdownMenuItem(
                                    text = { Text(if (isEn) city.nameEn else city.nameAr) },
                                    onClick = { selectedCityId = city.id; expanded = false }
                                )
                            }
                        }
                    }

                    // Property Type Selector
                    Box(modifier = Modifier.weight(1f)) {
                        var expanded by remember { mutableStateOf(false) }
                        val pTypes = mapOf(
                            "" to (if (isEn) "All Properties" else "جميع أنواع العقارات"),
                            "apartment" to (if (isEn) "Apartment" else "شقة سكينة"),
                            "house" to (if (isEn) "House / Villa" else "بيت مستقل/فيلا"),
                            "land" to (if (isEn) "Land" else "أرض عقارية"),
                            "shop" to (if (isEn) "Shop / Store" else "محل تجاري/دكان")
                        )
                        val buttonLabel = pTypes[filterPropType] ?: (if (isEn) "Property Type" else "نوع العقار")
                        Button(
                            onClick = { expanded = true },
                            colors = ButtonDefaults.buttonColors(containerColor = Color.Black.copy(alpha = 0.3f)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(buttonLabel, fontSize = 10.sp, color = Color.White)
                        }
                        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                            pTypes.forEach { (key, value) ->
                                DropdownMenuItem(
                                    text = { Text(value) },
                                    onClick = { filterPropType = key; expanded = false }
                                )
                            }
                        }
                    }
                }
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = if (isEn) "🏠 Available Properties (${activeProps.size}):" else "🏠 العقارات والمنشآت المتاحة (${activeProps.size}):",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                modifier = Modifier.weight(1f)
            )
            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                Button(
                    onClick = { viewModel.triggerRestoreAccountDialog.value = true },
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Gray),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                    modifier = Modifier.heightIn(min = 44.dp)
                ) {
                    Text(if (isEn) "Restore 🔓" else "استرجاع 🔓", color = Color.White, fontSize = 9.5.sp, fontWeight = FontWeight.Bold)
                }
                Button(
                    onClick = onAddPropertyClick,
                    colors = ButtonDefaults.buttonColors(containerColor = themeColors.accent),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                    modifier = Modifier.heightIn(min = 44.dp)
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, tint = Color.Black, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(3.dp))
                    Text(if (isEn) "Add Property" else "أضف عقار", color = Color.Black, fontSize = 9.5.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        if (activeProps.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxWidth().padding(40.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(if (isEn) "No properties match the current search filters." else "لم يتم العثور على أي عقار يطابق فلاتر البحث الحالية.", color = themeColors.textSecondary, fontSize = 12.sp)
            }
        } else {
            activeProps.take(itemsToShowLimit).forEach { prop ->
                PropertyListItemCard(prop = prop, themeColors = themeColors, onClick = { onPropertyClick(prop) }, viewModel = viewModel)
            }
            if (activeProps.size > itemsToShowLimit) {
                Button(
                    onClick = { itemsToShowLimit += 10 },
                    colors = ButtonDefaults.buttonColors(containerColor = themeColors.accent),
                    modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)
                ) {
                    Text(if (isEn) "Load More Properties ⏬" else "عرض المزيد من العقارات والمنشآت ⏬", color = Color.Black, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
fun PropertyListItemCard(
    prop: PropertyEntity,
    themeColors: VisualThemePalette,
    onClick: () -> Unit,
    viewModel: MainViewModel
) {
    val context = LocalContext.current
    val settingsState by viewModel.settings.collectAsState()

    Card(
        colors = CardDefaults.cardColors(containerColor = themeColors.surface),
        shape = RoundedCornerShape(8.dp),
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .border(
                1.dp,
                if (prop.isPinned) themeColors.accent else themeColors.accent.copy(alpha = 0.08f),
                RoundedCornerShape(8.dp)
            )
    ) {
        Column {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(78.dp)
                    .background(Color.DarkGray)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(themeColors.primary.copy(alpha = 0.1f))
                ) {
                    Text("📸 صور العقار", modifier = Modifier.align(Alignment.Center), color = Color.Gray, fontSize = 12.sp)
                }

                // Type Badge (Rent/Sale)
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .background(if (prop.type == "rent") Color(0xFF0284C7) else Color(0xFF16A34A), RoundedCornerShape(bottomStart = 8.dp))
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = if (prop.type == "rent") "للإيجار" else "للبيع",
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }

                if (prop.isPinned) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopStart)
                            .background(themeColors.accent, RoundedCornerShape(bottomEnd = 8.dp))
                            .padding(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Text("📌 متميز", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = Color.Black)
                    }
                }

                if (!prop.isActive) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .background(Color.Red)
                            .padding(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Text("قيد التحقق", fontSize = 8.sp, fontWeight = FontWeight.Bold, color = Color.White)
                    }
                }
            }

            Column(modifier = Modifier.padding(7.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(prop.title, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.White)
                    Spacer(modifier = Modifier.width(6.dp))
                    if (prop.isVip) {
                        Box(
                            modifier = Modifier
                                .background(Color(0xFFD97706), RoundedCornerShape(4.dp))
                                .padding(horizontal = 4.dp, vertical = 1.dp)
                        ) {
                            Text("🏆 VIP", fontSize = 8.sp, color = Color.White, fontWeight = FontWeight.Bold)
                        }
                        Spacer(modifier = Modifier.width(3.dp))
                    }
                    if (prop.isVerified) {
                        Box(
                            modifier = Modifier
                                .background(Color(0xFF3B82F6), RoundedCornerShape(4.dp))
                                .padding(horizontal = 4.dp, vertical = 1.dp)
                        ) {
                            Text("✅ موثق", fontSize = 8.sp, color = Color.White, fontWeight = FontWeight.Bold)
                        }
                        Spacer(modifier = Modifier.width(3.dp))
                    }
                    if (prop.isRecommended) {
                        Box(
                            modifier = Modifier
                                .background(Color(0xFFEC4899), RoundedCornerShape(4.dp))
                                .padding(horizontal = 4.dp, vertical = 1.dp)
                        ) {
                            Text("💖 موصى به", fontSize = 8.sp, color = Color.White, fontWeight = FontWeight.Bold)
                        }
                        Spacer(modifier = Modifier.width(3.dp))
                    }
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(prop.description, fontSize = 10.sp, color = themeColors.textSecondary, maxLines = 2, overflow = TextOverflow.Ellipsis)
                
                Spacer(modifier = Modifier.height(10.dp))
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("سعر الطلب:", fontSize = 8.sp, color = themeColors.textSecondary)
                        Text("🔽 ${prop.price} ${prop.currency}", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color(0xFF10B981))
                    }

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .background(Color.Black.copy(alpha = 0.4f), RoundedCornerShape(6.dp))
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = "📍 الحي: ${prop.localNeighborhood}",
                                fontSize = 9.sp,
                                color = Color.White,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }

                HorizontalDivider(color = Color.Gray.copy(alpha = 0.2f), modifier = Modifier.padding(top = 8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth().padding(top = 6.dp),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    if (settingsState.showCallButton) {
                        Button(
                            onClick = {
                                val u = android.content.Intent(android.content.Intent.ACTION_DIAL, android.net.Uri.parse("tel:${prop.phone}"))
                                context.startActivity(u)
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981)),
                            modifier = Modifier.weight(1f).heightIn(min = 44.dp),
                            contentPadding = PaddingValues(horizontal = 6.dp, vertical = 6.dp)
                        ) {
                            Text("📞 اتصال مباشر", fontSize = 10.sp, lineHeight = 14.sp, maxLines = 2, softWrap = true, color = Color.White, fontWeight = FontWeight.Bold)
                        }
                    }
                    if (settingsState.showVoiceCallButton && !settingsState.disableVoiceCalls) {
                        Button(
                            onClick = {
                                if (viewModel != null) {
                                    viewModel.startVoiceCall(prop.title, "صاحب العقار HD")
                                } else {
                                    val u = android.content.Intent(android.content.Intent.ACTION_DIAL, android.net.Uri.parse("tel:${prop.phone}"))
                                    context.startActivity(u)
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2563EB)),
                            modifier = Modifier.weight(1f).heightIn(min = 44.dp),
                            contentPadding = PaddingValues(horizontal = 6.dp, vertical = 6.dp)
                        ) {
                            Text("🎙️ مكالمة صوتية", fontSize = 10.sp, lineHeight = 14.sp, maxLines = 2, softWrap = true, color = Color.White, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}
