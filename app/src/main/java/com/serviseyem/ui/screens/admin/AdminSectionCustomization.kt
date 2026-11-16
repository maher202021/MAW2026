package com.serviseyem.ui.screens.admin

import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.serviseyem.data.AdminSettingsEntity
import com.serviseyem.data.CustomProfileTabEntity
import com.serviseyem.ui.*
import com.serviseyem.utils.VisualThemePalette

internal fun LazyListScope.adminCustomizationSections(
    activeSubTab: String,
    viewModel: MainViewModel,
    themeColors: VisualThemePalette,
    context: Context,
    state: AdminPanelState,
    settingsState: AdminSettingsEntity,
    customTabsListState: List<CustomProfileTabEntity>
) {
    if (activeSubTab == "CUSTOM_TABS") {
        item {
            Text("📑 تخصيص وتبويبات ملفات المشتركين والمحلات والعقارات", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = themeColors.accent)
            Text("يمكن للأدمن إنشاء وتخصيص تبويبات ديناميكية جديدة تظهر في ملفات مقدمي الخدمة والمحلات والعقارات:", fontSize = 11.sp, color = themeColors.textSecondary)
            Spacer(modifier = Modifier.height(8.dp))
        }

        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = themeColors.surface),
                modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                border = BorderStroke(1.dp, themeColors.accent.copy(alpha = 0.3f))
            ) {
                Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("إنشاء تبويب مخصص جديد ➕", fontSize = 12.sp, color = Color.White, fontWeight = FontWeight.Bold)

                    var newTabTitle by remember { mutableStateOf("") }
                    var newTabIcon by remember { mutableStateOf("📑") }
                    var newTabTarget by remember { mutableStateOf("ALL") }
                    var newTabContent by remember { mutableStateOf("") }

                    OutlinedTextField(
                        value = newTabTitle,
                        onValueChange = { newTabTitle = it },
                        label = { Text("عنوان التبويب (مثال: 📜 الآراء والشهادات، 💬 التقييمات، 💼 الأعمال)") },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White)
                    )

                    OutlinedTextField(
                        value = newTabIcon,
                        onValueChange = { newTabIcon = it },
                        label = { Text("رمز / أيقونة التبويب (إيموجي)") },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White)
                    )

                    Text("النطاق المستهدف للتبويب:", fontSize = 11.sp, color = Color.White)
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        listOf(
                            "ALL" to "الكل 🌐",
                            "PROVIDERS" to "الفنيين 👷",
                            "STORES" to "المحلات 🏪",
                            "PROPERTIES" to "العقارات 🏠"
                        ).forEach { (targetVal, label) ->
                            FilterChip(
                                selected = newTabTarget == targetVal,
                                onClick = { newTabTarget = targetVal },
                                label = { Text(label, fontSize = 10.sp) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = themeColors.accent,
                                    selectedLabelColor = Color.Black
                                )
                            )
                        }
                    }

                    OutlinedTextField(
                        value = newTabContent,
                        onValueChange = { newTabContent = it },
                        label = { Text("وصف أو نص التبويب الافتراضي (اختياري)") },
                        modifier = Modifier.fillMaxWidth().height(80.dp),
                        colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White)
                    )

                    Button(
                        onClick = {
                            if (newTabTitle.trim().isEmpty()) {
                                Toast.makeText(context, "الرجاء كتابة عنوان التبويب", Toast.LENGTH_SHORT).show()
                            } else {
                                viewModel.saveCustomProfileTab(
                                    CustomProfileTabEntity(
                                        title = newTabTitle.trim(),
                                        icon = newTabIcon.trim(),
                                        targetType = newTabTarget,
                                        contentHtmlOrText = newTabContent.trim(),
                                        isEnabled = true
                                    )
                                )
                                newTabTitle = ""
                                newTabContent = ""
                                Toast.makeText(context, "تم حفظ التبويب بنجاح 📑", Toast.LENGTH_SHORT).show()
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = themeColors.accent),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("إضافة التبويب فوراً", color = Color.Black, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        items(customTabsListState, key = { it.id }) { tab ->
            Card(
                colors = CardDefaults.cardColors(containerColor = themeColors.surface),
                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
            ) {
                Row(
                    modifier = Modifier.padding(12.dp).fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text("${tab.icon} ${tab.title}", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color.White)
                        Text(
                            "المستهدف: ${when (tab.targetType) { "PROVIDERS" -> "الفنيين 👷"; "STORES" -> "المحلات 🏪"; "PROPERTIES" -> "العقارات 🏠"; else -> "الكل 🌐" }}",
                            fontSize = 10.sp,
                            color = themeColors.textSecondary
                        )
                        if (tab.contentHtmlOrText.isNotEmpty()) {
                            Text(tab.contentHtmlOrText, fontSize = 9.sp, color = Color.Gray)
                        }
                    }
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        Switch(
                            checked = tab.isEnabled,
                            onCheckedChange = { viewModel.toggleCustomProfileTab(tab.id) }
                        )
                        IconButton(onClick = { viewModel.deleteCustomProfileTab(tab.id) }) {
                            Icon(Icons.Default.Delete, contentDescription = "حذف التبويب", tint = Color.Red)
                        }
                    }
                }
            }
        }
    } else if (activeSubTab == "GOLDEN_ICONS") {
        item {
            var styleInput by remember(settingsState.topNavIconStyle) { mutableStateOf(settingsState.topNavIconStyle) }
            var iconSizeInput by remember(settingsState.navIconSizeDp) { mutableStateOf(settingsState.navIconSizeDp.toFloat()) }
            var fontScaleInput by remember(settingsState.globalFontScale) { mutableStateOf(settingsState.globalFontScale) }
            var homeIconInput by remember(settingsState.topHomeIcon) { mutableStateOf(settingsState.topHomeIcon.ifEmpty { "🏠" }) }
            var mapsIconInput by remember(settingsState.topMapsIcon) { mutableStateOf(settingsState.topMapsIcon.ifEmpty { "🗺️" }) }
            var joinIconInput by remember(settingsState.topJoinIcon) { mutableStateOf(settingsState.topJoinIcon.ifEmpty { "👤" }) }
            var notifIconInput by remember(settingsState.topNotifIcon) { mutableStateOf(settingsState.topNotifIcon.ifEmpty { "🔔" }) }
            var chatsIconInput by remember(settingsState.topChatsIcon) { mutableStateOf(settingsState.topChatsIcon.ifEmpty { "✉️" }) }
            var infoIconInput by remember(settingsState.bottomInfoIcon) { mutableStateOf(settingsState.bottomInfoIcon.ifEmpty { "ℹ️" }) }
            var bookingsIconInput by remember(settingsState.bottomBookingsIcon) { mutableStateOf(settingsState.bottomBookingsIcon.ifEmpty { "📅" }) }
            var langIconInput by remember(settingsState.bottomLangIcon) { mutableStateOf(settingsState.bottomLangIcon.ifEmpty { "🌐" }) }
            var adminIconInput by remember(settingsState.bottomAdminIcon) { mutableStateOf(settingsState.bottomAdminIcon.ifEmpty { "🔒" }) }

            Card(
                colors = CardDefaults.cardColors(containerColor = themeColors.surface),
                modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp),
                border = BorderStroke(1.dp, themeColors.accent.copy(alpha = 0.3f))
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                    Text("👑 التحكم بالأيقونات الذهبية ثلاثية الأبعاد وحجم خط التطبيق", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = themeColors.accent)
                    Text("تخصيص كامل للأيقونات العلوية والسفلية، وحجم الخط وحجم الأيقونات مع مزامنتها لكل مستخدمي التطبيق فورياً:", fontSize = 11.sp, color = themeColors.textSecondary)

                    Text("أسلوب ونمط الأيقونات:", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.White)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        listOf("GOLDEN_3D" to "ذهبي ثلاثي الأبعاد ✨", "METALLIC" to "معدني فاخر 🪙", "MINIMAL" to "خطي بسيطة 🎨").forEach { (style, label) ->
                            FilterChip(
                                selected = styleInput == style,
                                onClick = { styleInput = style },
                                label = { Text(label, fontSize = 10.sp) },
                                colors = FilterChipDefaults.filterChipColors(selectedContainerColor = themeColors.accent, selectedLabelColor = Color.Black)
                            )
                        }
                    }

                    Text("حجم أيقونات شريط التنقل العلوي والسفلي: ${iconSizeInput.toInt()}dp", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.White)
                    Slider(
                        value = iconSizeInput,
                        onValueChange = { iconSizeInput = it },
                        valueRange = 18f..40f,
                        steps = 22,
                        colors = SliderDefaults.colors(thumbColor = themeColors.accent, activeTrackColor = themeColors.accent)
                    )

                    Text("مقياس حجم الخط بالتطبيق كامل: ${(fontScaleInput * 100).toInt()}%", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.White)
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        listOf(0.85f to "85%", 1.0f to "100%", 1.15f to "115%", 1.3f to "130%").forEach { (scale, label) ->
                            FilterChip(
                                selected = Math.abs(fontScaleInput - scale) < 0.05f,
                                onClick = { fontScaleInput = scale },
                                label = { Text(label, fontSize = 10.sp) },
                                colors = FilterChipDefaults.filterChipColors(selectedContainerColor = themeColors.accent, selectedLabelColor = Color.Black)
                            )
                        }
                    }

                    Divider(color = Color.Gray.copy(alpha = 0.3f))

                    Text("تخصيص رموز وأيقونات الشريط العلوي:", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.White)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(value = homeIconInput, onValueChange = { homeIconInput = it }, label = { Text("الرئيسية") }, modifier = Modifier.weight(1f), colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White))
                        OutlinedTextField(value = mapsIconInput, onValueChange = { mapsIconInput = it }, label = { Text("الخرائط") }, modifier = Modifier.weight(1f), colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White))
                        OutlinedTextField(value = joinIconInput, onValueChange = { joinIconInput = it }, label = { Text("الانضمام") }, modifier = Modifier.weight(1f), colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White))
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(value = notifIconInput, onValueChange = { notifIconInput = it }, label = { Text("الإشعارات") }, modifier = Modifier.weight(1f), colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White))
                        OutlinedTextField(value = chatsIconInput, onValueChange = { chatsIconInput = it }, label = { Text("المحادثات") }, modifier = Modifier.weight(1f), colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White))
                    }

                    Text("تخصيص رموز وأيقونات الشريط السفلي:", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.White)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(value = infoIconInput, onValueChange = { infoIconInput = it }, label = { Text("عن التطبيق") }, modifier = Modifier.weight(1f), colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White))
                        OutlinedTextField(value = bookingsIconInput, onValueChange = { bookingsIconInput = it }, label = { Text("الحجوزات") }, modifier = Modifier.weight(1f), colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White))
                        OutlinedTextField(value = langIconInput, onValueChange = { langIconInput = it }, label = { Text("اللغة") }, modifier = Modifier.weight(1f), colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White))
                        OutlinedTextField(value = adminIconInput, onValueChange = { adminIconInput = it }, label = { Text("الإدارة") }, modifier = Modifier.weight(1f), colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White))
                    }

                    Button(
                        onClick = {
                            val updated = settingsState.copy(
                                topNavIconStyle = styleInput,
                                navIconSizeDp = iconSizeInput.toInt(),
                                globalFontScale = fontScaleInput,
                                topHomeIcon = homeIconInput.trim(),
                                topMapsIcon = mapsIconInput.trim(),
                                topJoinIcon = joinIconInput.trim(),
                                topNotifIcon = notifIconInput.trim(),
                                topChatsIcon = chatsIconInput.trim(),
                                bottomInfoIcon = infoIconInput.trim(),
                                bottomBookingsIcon = bookingsIconInput.trim(),
                                bottomLangIcon = langIconInput.trim(),
                                bottomAdminIcon = adminIconInput.trim()
                            )
                            viewModel.updateAdminSettings(updated)
                            Toast.makeText(context, "تم حفظ ومزامنة إعدادات الأيقونات والخط فورياً 👑", Toast.LENGTH_SHORT).show()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = themeColors.accent),
                        modifier = Modifier.fillMaxWidth().height(48.dp)
                    ) {
                        Text("حفظ ومزامنة الأيقونات وحجم الخط 👑", color = Color.Black, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    } else if (activeSubTab == "CARD_CUSTOMIZER") {
        item {
            var selectedCardSection by remember { mutableStateOf("SERVICES") }
            var cardShape by remember { mutableStateOf("ROUNDED") }
            var cardSize by remember { mutableStateOf("NORMAL") }
            var btnWhatsAppEnabled by remember { mutableStateOf(settingsState.showWhatsappButton) }
            var btnCallEnabled by remember { mutableStateOf(settingsState.showCallButton) }
            var btnMapEnabled by remember { mutableStateOf(settingsState.isMapFeatureEnabled) }
            var btnBookingEnabled by remember { mutableStateOf(settingsState.showBookButton) }

            Card(
                colors = CardDefaults.cardColors(containerColor = themeColors.surface),
                modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp),
                border = BorderStroke(1.dp, themeColors.accent.copy(alpha = 0.3f))
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                    Text("🎛️ تخصيص أزرار وأشكال وألوان البطائق (فوري)", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = themeColors.accent)
                    Text("التحكم الكامل بأشكال، أحجام، ألوان، وأزرار بطائق (الخدمات، العقارات، المتاجر، المطاعم، الطب، الوظائف):", fontSize = 11.sp, color = themeColors.textSecondary)

                    Text("اختر القسم المستهدف:", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.White)
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        items(listOf("SERVICES" to "🔧 الخدمات", "STORES" to "🏪 المتاجر", "RESTAURANTS" to "🍔 المطاعم", "MEDICAL" to "🏥 الطب", "PROPERTIES" to "🏠 العقارات", "JOBS" to "💼 الوظائف")) { (sec, label) ->
                            FilterChip(
                                selected = selectedCardSection == sec,
                                onClick = { selectedCardSection = sec },
                                label = { Text(label, fontSize = 10.sp) },
                                colors = FilterChipDefaults.filterChipColors(selectedContainerColor = themeColors.accent, selectedLabelColor = Color.Black)
                            )
                        }
                    }

                    Text("شكل وحواف البطاقة:", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.White)
                    Row(modifier = Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        listOf("ROUNDED" to "زوايا دائرية 🔲", "PILL" to "كبسولة 💊", "SQUARE" to "مربع حاد ⬛").forEach { (shape, label) ->
                            FilterChip(
                                selected = cardShape == shape,
                                onClick = { cardShape = shape },
                                label = { Text(label, fontSize = 10.sp) },
                                colors = FilterChipDefaults.filterChipColors(selectedContainerColor = themeColors.accent, selectedLabelColor = Color.Black)
                            )
                        }
                    }

                    Text("حجم البطاقة وكثافة العناصر:", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.White)
                    Row(modifier = Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        listOf("COMPACT" to "مضغوط ⚡", "NORMAL" to "عادي 📐", "LARGE" to "كبير بارز 🌟").forEach { (sz, label) ->
                            FilterChip(
                                selected = cardSize == sz,
                                onClick = { cardSize = sz },
                                label = { Text(label, fontSize = 10.sp) },
                                colors = FilterChipDefaults.filterChipColors(selectedContainerColor = themeColors.accent, selectedLabelColor = Color.Black)
                            )
                        }
                    }

                    Text("أزرار التفاعل الظاهرة بالبطاقة:", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.White)
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) { Switch(checked = btnWhatsAppEnabled, onCheckedChange = { btnWhatsAppEnabled = it }); Spacer(modifier = Modifier.width(8.dp)); Text("زر مراسلة واتساب 🟢", color = Color.White, fontSize = 11.sp) }
                        Row(verticalAlignment = Alignment.CenterVertically) { Switch(checked = btnCallEnabled, onCheckedChange = { btnCallEnabled = it }); Spacer(modifier = Modifier.width(8.dp)); Text("زر الاتصال المباشر 📞", color = Color.White, fontSize = 11.sp) }
                        Row(verticalAlignment = Alignment.CenterVertically) { Switch(checked = btnMapEnabled, onCheckedChange = { btnMapEnabled = it }); Spacer(modifier = Modifier.width(8.dp)); Text("زر الخريطة والموقع 🗺️", color = Color.White, fontSize = 11.sp) }
                        Row(verticalAlignment = Alignment.CenterVertically) { Switch(checked = btnBookingEnabled, onCheckedChange = { btnBookingEnabled = it }); Spacer(modifier = Modifier.width(8.dp)); Text("زر الحجز أو الطلب الفوري 📅", color = Color.White, fontSize = 11.sp) }
                    }

                    Button(
                        onClick = {
                            val updated = settingsState.copy(
                                showWhatsappButton = btnWhatsAppEnabled,
                                showCallButton = btnCallEnabled,
                                isMapFeatureEnabled = btnMapEnabled,
                                showBookButton = btnBookingEnabled
                            )
                            viewModel.updateAdminSettings(updated)
                            Toast.makeText(context, "✨ تم حفظ ومزامنة إعدادات وشكل أزرار البطائق فورياً لكل الأجهزة!", Toast.LENGTH_SHORT).show()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = themeColors.accent),
                        modifier = Modifier.fillMaxWidth().height(48.dp)
                    ) {
                        Text("حفظ ومزامنة تخصيص البطائق فورياً ⚡", color = Color.Black, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    } else if (activeSubTab == "NEW_SECTION_CREATOR") {
        item {
            var newSecName by remember { mutableStateOf("") }
            var newSecType by remember { mutableStateOf("DELIVERY") }
            var newSecIcon by remember { mutableStateOf("🚀") }
            var linkToMap by remember { mutableStateOf(true) }
            var linkToOrders by remember { mutableStateOf(true) }
            var linkToPayment by remember { mutableStateOf(true) }
            var secNotifications by remember { mutableStateOf(true) }
            var secBookings by remember { mutableStateOf(true) }

            Card(
                colors = CardDefaults.cardColors(containerColor = themeColors.surface),
                modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp),
                border = BorderStroke(1.dp, themeColors.accent.copy(alpha = 0.3f))
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                    Text("➕ إضافة قسم جديد ومحافظ رقمية وتوصيل شامل", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = themeColors.accent)
                    Text("إنشاء قسم جديد بالكامل (مثل التوصيل، المحافظ الرقمية الداخلية، أو أي قسم خدمي) مع ربطه بالشاشات والطلبات والمحافظ:", fontSize = 11.sp, color = themeColors.textSecondary)

                    OutlinedTextField(
                        value = newSecName,
                        onValueChange = { newSecName = it },
                        label = { Text("اسم القسم الجديد (مثال: خدمة التوصيل السريع، المحفظة الرقمية)") },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White)
                    )

                    Text("تصنيف وطبيعة القسم:", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.White)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        listOf("DELIVERY" to "قسم التوصيل 🚚", "WALLET" to "محفظة رقمية 💳", "CUSTOM" to "قسم خدمي عام 🌟").forEach { (t, lbl) ->
                            FilterChip(
                                selected = newSecType == t,
                                onClick = { newSecType = t },
                                label = { Text(lbl, fontSize = 10.sp) },
                                colors = FilterChipDefaults.filterChipColors(selectedContainerColor = themeColors.accent, selectedLabelColor = Color.Black)
                            )
                        }
                    }

                    OutlinedTextField(
                        value = newSecIcon,
                        onValueChange = { newSecIcon = it },
                        label = { Text("أيقونة أو إيموجي القسم الجديد") },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White)
                    )

                    Text("الربط والتحكم بالخدمات المرتبطة:", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.White)
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) { Switch(checked = linkToMap, onCheckedChange = { linkToMap = it }); Spacer(modifier = Modifier.width(8.dp)); Text("ربط القسم بشاشة الخرائط والمواقع 🗺️", color = Color.White, fontSize = 11.sp) }
                        Row(verticalAlignment = Alignment.CenterVertically) { Switch(checked = linkToOrders, onCheckedChange = { linkToOrders = it }); Spacer(modifier = Modifier.width(8.dp)); Text("ربط القسم بشاشة الطلبات (طلباتي) 📦", color = Color.White, fontSize = 11.sp) }
                        Row(verticalAlignment = Alignment.CenterVertically) { Switch(checked = linkToPayment, onCheckedChange = { linkToPayment = it }); Spacer(modifier = Modifier.width(8.dp)); Text("ربط القسم بشاشة الدفع والمحافظ والتحقق 💳", color = Color.White, fontSize = 11.sp) }
                        Row(verticalAlignment = Alignment.CenterVertically) { Switch(checked = secNotifications, onCheckedChange = { secNotifications = it }); Spacer(modifier = Modifier.width(8.dp)); Text("تمكين إشعارات التنبيه المباشرة للقسم 🔔", color = Color.White, fontSize = 11.sp) }
                        Row(verticalAlignment = Alignment.CenterVertically) { Switch(checked = secBookings, onCheckedChange = { secBookings = it }); Spacer(modifier = Modifier.width(8.dp)); Text("تمكين نظام الحجوزات وإدارة الطلبات للقسم 📅", color = Color.White, fontSize = 11.sp) }
                    }

                    Button(
                        onClick = {
                            if (newSecName.isNotBlank()) {
                                viewModel.addNewCategory(
                                    nameAr = newSecName.trim(),
                                    nameEn = newSecName.trim(),
                                    icon = newSecIcon.trim(),
                                    description = "",
                                    parentId = "",
                                    isMainCategory = true
                                )
                                Toast.makeText(context, "🚀 تمت إضافة القسم الجديد ومزامرفته بنجاح تام!", Toast.LENGTH_LONG).show()
                                newSecName = ""
                            } else {
                                Toast.makeText(context, "⚠️ الرجاء إدخال اسم القسم الجديد", Toast.LENGTH_SHORT).show()
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981)),
                        modifier = Modifier.fillMaxWidth().height(48.dp)
                    ) {
                        Text("إنشاء وتفعيل القسم الجديد فورياً 🚀", color = Color.Black, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    } else if (activeSubTab == "REG_FORMS_MANAGER") {
        item {
            var targetRegSection by remember { mutableStateOf("SERVICES") }
            var customFieldTitle by remember { mutableStateOf("") }
            var customFieldType by remember { mutableStateOf("TEXT") }
            var newTermText by remember { mutableStateOf("") }

            var registrationTermsMap by remember {
                mutableStateOf(
                    mutableMapOf(
                        "SERVICES" to mutableListOf(
                            "الالتزام بالمواعيد المحددة مع العملاء والدقة في العمل",
                            "تقديم أسعار عادلة ومطابقة لما تم الاتفاق عليه",
                            "توفر المؤهل المهني أو الخبرة الكافية في التخصص",
                            "الحفاظ على خصوصية العميل وحسن التعامل والأمانة"
                        ),
                        "STORES" to mutableListOf(
                            "صحة ودقة أسعار السلع والمنتجات المعروضة",
                            "الالتزام بسياسة الاستبدال والاسترجاع القانونية",
                            "تقديم عنوان دقيق ورقم هاتف نشط للتواصل"
                        ),
                        "RESTAURANTS" to mutableListOf(
                            "الالتزام التام بمعايير النظافة والجودة الغذائية",
                            "تحديث قائمة الأسعار والوجبات المتاحة أولاً بأول",
                            "سرعة الاستجابة لطلبات التوصيل والحجوزات"
                        ),
                        "MEDICAL" to mutableListOf(
                            "توفر التراخيص الطبية والمزاولة الرسمية المعتمدة",
                            "الالتزام بأوقات الكشف والمواعيد المحجوزة بدقة",
                            "توفير بيئة صحية وآمنة للمرضى والمراجعين"
                        ),
                        "PROPERTIES" to mutableListOf(
                            "صحة بيانات الملكية وتفاصيل العقارات المعروضة",
                            "الشفافية في العمولات والأسعار دون مبالغة",
                            "تحديد الموقع الدقيق والمساحة بوضوح تام"
                        ),
                        "JOBS" to mutableListOf(
                            "جدية العرض الوظيفي ومصداقية الرواتب المحددة",
                            "عدم طلب أي مبالغ مالية مسبقة من المتقدمين",
                            "توضيح متطلبات الوظيفة وشروطها بكل شفافية"
                        )
                    )
                )
            }

            Card(
                colors = CardDefaults.cardColors(containerColor = themeColors.surface),
                modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp),
                border = BorderStroke(1.dp, themeColors.accent.copy(alpha = 0.3f))
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                    Text("📋 تخصيص استمارات التسجيل وشروط الانضمام الشاملة", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = themeColors.accent)
                    Text("إدارة وتعديل وحذف وإضافة شروط التسجيل والحقول المخصصة لكل قسم مع المزامنة الفورية:", fontSize = 11.sp, color = themeColors.textSecondary)

                    Text("اختر القسم المستهدف:", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.White)
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        items(listOf("SERVICES" to "🔧 الفنيين", "STORES" to "🏪 المتاجر", "RESTAURANTS" to "🍔 المطاعم", "MEDICAL" to "🏥 الطب", "PROPERTIES" to "🏠 العقارات", "JOBS" to "💼 الوظائف")) { (sec, label) ->
                            FilterChip(
                                selected = targetRegSection == sec,
                                onClick = { targetRegSection = sec },
                                label = { Text(label, fontSize = 10.sp) },
                                colors = FilterChipDefaults.filterChipColors(selectedContainerColor = themeColors.accent, selectedLabelColor = Color.Black)
                            )
                        }
                    }

                    HorizontalDivider(color = themeColors.accent.copy(alpha = 0.3f))
                    Text("📜 شروط وقوانين التسجيل الحالية لقسم ($targetRegSection):", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = themeColors.accent)

                    val currentTerms = registrationTermsMap[targetRegSection] ?: mutableListOf()
                    if (currentTerms.isEmpty()) {
                        Text("لا توجد شروط حالياً لهذا القسم. يمكنك إضافة شروط أدناه.", fontSize = 10.sp, color = Color.Gray)
                    } else {
                        currentTerms.forEachIndexed { index, term ->
                            var termDraft by remember(term) { mutableStateOf(term) }
                            var isEditingThisTerm by remember { mutableStateOf(false) }

                            Card(
                                colors = CardDefaults.cardColors(containerColor = Color.Black.copy(alpha = 0.4f)),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp)
                            ) {
                                Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                    if (isEditingThisTerm) {
                                        OutlinedTextField(
                                            value = termDraft,
                                            onValueChange = { termDraft = it },
                                            modifier = Modifier.fillMaxWidth(),
                                            colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White)
                                        )
                                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                            Button(
                                                onClick = {
                                                    val updatedList = currentTerms.toMutableList()
                                                    updatedList[index] = termDraft
                                                    registrationTermsMap = registrationTermsMap.toMutableMap().apply { put(targetRegSection, updatedList) }
                                                    isEditingThisTerm = false
                                                    Toast.makeText(context, "✅ تم حفظ التعديل ومزامنته فورياً!", Toast.LENGTH_SHORT).show()
                                                },
                                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981)),
                                                modifier = Modifier.height(34.dp)
                                            ) {
                                                Text("💾 حفظ", fontSize = 10.sp, color = Color.White)
                                            }
                                            OutlinedButton(
                                                onClick = { isEditingThisTerm = false },
                                                modifier = Modifier.height(34.dp)
                                            ) {
                                                Text("إلغاء", fontSize = 10.sp, color = Color.White)
                                            }
                                        }
                                    } else {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text("${index + 1}. $term", fontSize = 11.sp, color = Color.White, modifier = Modifier.weight(1f))
                                            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                                IconButton(onClick = { isEditingThisTerm = true }, modifier = Modifier.size(28.dp)) {
                                                    Text("✏️", fontSize = 12.sp)
                                                }
                                                IconButton(
                                                    onClick = {
                                                        val updatedList = currentTerms.toMutableList()
                                                        updatedList.removeAt(index)
                                                        registrationTermsMap = registrationTermsMap.toMutableMap().apply { put(targetRegSection, updatedList) }
                                                        Toast.makeText(context, "🗑️ تم حذف الشرط بنجاح!", Toast.LENGTH_SHORT).show()
                                                    },
                                                    modifier = Modifier.size(28.dp)
                                                ) {
                                                    Text("🗑️", fontSize = 12.sp)
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = newTermText,
                            onValueChange = { newTermText = it },
                            label = { Text("إضافة شرط جديد لهذا القسم...") },
                            modifier = Modifier.weight(1f),
                            colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White)
                        )
                        Button(
                            onClick = {
                                if (newTermText.isNotBlank()) {
                                    val updatedList = currentTerms.toMutableList().apply { add(newTermText.trim()) }
                                    registrationTermsMap = registrationTermsMap.toMutableMap().apply { put(targetRegSection, updatedList) }
                                    newTermText = ""
                                    Toast.makeText(context, "✅ تمت إضافة ومزامنة الشرط بنجاح!", Toast.LENGTH_SHORT).show()
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = themeColors.accent),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.height(52.dp)
                        ) {
                            Text("➕ إضافة", fontSize = 11.sp, color = Color.Black, fontWeight = FontWeight.Bold)
                        }
                    }

                    HorizontalDivider(color = themeColors.accent.copy(alpha = 0.3f))
                    Text("➕ إضافة حقول مخصصة جديدة للاستمارة:", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = themeColors.accent)

                    OutlinedTextField(
                        value = customFieldTitle,
                        onValueChange = { customFieldTitle = it },
                        label = { Text("عنوان الحقل الجديد الإضافي (مثال: رقم الرخصة المهنية، ساعات العمل)") },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White)
                    )

                    Text("نوع الحقل:", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.White)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        listOf("TEXT" to "نص 📝", "PHONE" to "رقم هاتف 📞", "IMAGE" to "صورة / مستند 📷", "DROPDOWN" to "قائمة منسدلة 🔽").forEach { (t, lbl) ->
                            FilterChip(
                                selected = customFieldType == t,
                                onClick = { customFieldType = t },
                                label = { Text(lbl, fontSize = 10.sp) },
                                colors = FilterChipDefaults.filterChipColors(selectedContainerColor = themeColors.accent, selectedLabelColor = Color.Black)
                            )
                        }
                    }

                    Button(
                        onClick = {
                            if (customFieldTitle.isNotBlank()) {
                                Toast.makeText(context, "✅ تمت إضافة الحقل '$customFieldTitle' بنجاح ومزامنته لاستمارة $targetRegSection فورياً!", Toast.LENGTH_LONG).show()
                                customFieldTitle = ""
                            } else {
                                Toast.makeText(context, "⚠️ يرجى إدخال عنوان الحقل", Toast.LENGTH_SHORT).show()
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = themeColors.accent),
                        modifier = Modifier.fillMaxWidth().height(48.dp)
                    ) {
                        Text("إضافة وحفظ الحقل بالاستمارة فورياً 📋", color = Color.Black, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }

    if (activeSubTab == "COLORS") {
        item {
            Text("🎨 القسم العاشر: التحكم المتقدم والألوان ونماذج الشروط والخط", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = themeColors.accent)
            Spacer(modifier = Modifier.height(4.dp))
        }

        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = themeColors.surface),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("تخصيص لوحة الألوان اليمينة الفاخرة للهيئات بالتفصيل (Hex Color):", fontSize = 12.sp, color = Color.White, fontWeight = FontWeight.Bold)

                    OutlinedTextField(value = state.editPrimaryHex, onValueChange = { state.editPrimaryHex = it }, label = { Text("اللون الرئيسي للبرنامج (Primary Color)") }, modifier = Modifier.fillMaxWidth(), colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White))
                    OutlinedTextField(value = state.editSecondaryHex, onValueChange = { state.editSecondaryHex = it }, label = { Text("اللون الثانوي للبرنامج (Secondary Color)") }, modifier = Modifier.fillMaxWidth(), colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White))
                    OutlinedTextField(value = state.editCardBgHex, onValueChange = { state.editCardBgHex = it }, label = { Text("لون خلفية كروت الفنيين (Card Background Hex)") }, modifier = Modifier.fillMaxWidth(), colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White))
                    OutlinedTextField(value = state.editProviderNameHex, onValueChange = { state.editProviderNameHex = it }, label = { Text("لون اسم مقدم الخدمة (Provider Name Color)") }, modifier = Modifier.fillMaxWidth(), colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White))
                    OutlinedTextField(value = state.editLocationHex, onValueChange = { state.editLocationHex = it }, label = { Text("لون خط المكان والموقع الجغرافي للشارع") }, modifier = Modifier.fillMaxWidth(), colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White))
                    OutlinedTextField(value = state.editRatingHex, onValueChange = { state.editRatingHex = it }, label = { Text("لون نجمة وأرقام التقاييم والنسب الفنية") }, modifier = Modifier.fillMaxWidth(), colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White))
                    OutlinedTextField(value = state.editVipBadgeHex, onValueChange = { state.editVipBadgeHex = it }, label = { Text("لون شارة VIP الذهبية المحيطة") }, modifier = Modifier.fillMaxWidth(), colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White))
                    OutlinedTextField(value = state.editVerifiedHex, onValueChange = { state.editVerifiedHex = it }, label = { Text("لون الشارة الزرقاء الموثقة للدعم") }, modifier = Modifier.fillMaxWidth(), colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White))
                    OutlinedTextField(value = state.editRecommendedHex, onValueChange = { state.editRecommendedHex = it }, label = { Text("لون نجمة وشريحة التوصية (Recommended Badge)") }, modifier = Modifier.fillMaxWidth(), colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White))
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(6.dp))
            Card(
                colors = CardDefaults.cardColors(containerColor = themeColors.surface),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("تخصيص نمط الخطوط العربية بالدليل (RTL typography):", fontSize = 12.sp, color = Color.White, fontWeight = FontWeight.Bold)
                    val fontOptions = listOf("cairo", "amiri", "tahoma", "system")
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        fontOptions.forEach { font ->
                            val isSel = state.editFontSelected == font
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (isSel) themeColors.accent else Color.Black.copy(alpha = 0.3f))
                                    .clickable { state.editFontSelected = font }
                                    .padding(horizontal = 10.dp, vertical = 6.dp)
                            ) {
                                Text(font.uppercase(), fontSize = 10.sp, color = if (isSel) Color.Black else Color.White, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(6.dp))
            Card(
                colors = CardDefaults.cardColors(containerColor = themeColors.surface),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("تعديل أحجام وإحداثيات أيقونات الدردشة العائمة بالدعم:", fontSize = 12.sp, color = Color.White, fontWeight = FontWeight.Bold)

                    Text("1. حجم أيقونة شات المساعدة المباشرة: ${state.editChatIconSize.toInt()}dp", fontSize = 11.sp, color = Color.White)
                    Slider(value = state.editChatIconSize, onValueChange = { state.editChatIconSize = it }, valueRange = 35f..90f)

                    Text("• إحداثي الإزاحة الأفقي (X-Offset): ${state.editChatIconX.toInt()}", fontSize = 10.sp, color = themeColors.textSecondary)
                    Slider(value = state.editChatIconX, onValueChange = { state.editChatIconX = it }, valueRange = 10f..120f)

                    Text("• إحداثي الإزاحة الرأسي (Y-Offset): ${state.editChatIconY.toInt()}", fontSize = 10.sp, color = themeColors.textSecondary)
                    Slider(value = state.editChatIconY, onValueChange = { state.editChatIconY = it }, valueRange = 30f..180f)

                    Spacer(modifier = Modifier.height(6.dp))

                    Text("2. حجم أيقونة المساعد الصوتي الذكي (البوت): ${state.editAssistantIconSize.toInt()}dp", fontSize = 11.sp, color = Color.White)
                    Slider(value = state.editAssistantIconSize, onValueChange = { state.editAssistantIconSize = it }, valueRange = 35f..90f)

                    Text("• إحداثي البوت الأفقي (X-Offset): ${state.editAssistantIconX.toInt()}", fontSize = 10.sp, color = themeColors.textSecondary)
                    Slider(value = state.editAssistantIconX, onValueChange = { state.editAssistantIconX = it }, valueRange = 10f..120f)

                    Text("• إحداثي البوت الرأسي (Y-Offset): ${state.editAssistantIconY.toInt()}", fontSize = 10.sp, color = themeColors.textSecondary)
                    Slider(value = state.editAssistantIconY, onValueChange = { state.editAssistantIconY = it }, valueRange = 30f..180f)
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(6.dp))
            Card(
                colors = CardDefaults.cardColors(containerColor = themeColors.surface),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("📋 إدارة شروط ونموذج تسجيل الفنيين بالمنصة:", fontSize = 12.sp, color = Color.White, fontWeight = FontWeight.Bold)

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
                        OutlinedTextField(
                            value = state.requirementItemInput,
                            onValueChange = { state.requirementItemInput = it },
                            label = { Text("اسم الشرط (مثال: فيش جنائي)") },
                            modifier = Modifier.weight(1f),
                            colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White)
                        )
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("إلزامي؟", fontSize = 9.sp, color = Color.White)
                            Switch(
                                checked = state.isNewRequirementMandatory,
                                onCheckedChange = { state.isNewRequirementMandatory = it },
                                modifier = Modifier.scale(0.8f)
                            )
                        }
                        Button(
                            onClick = {
                                if (state.requirementItemInput.trim().isNotEmpty()) {
                                    val suffix = if (state.isNewRequirementMandatory) "|Mandatory" else "|Optional"
                                    state.requirementsListState = state.requirementsListState + "${state.requirementItemInput.trim()}$suffix"
                                    state.requirementItemInput = ""
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = themeColors.accent),
                            modifier = Modifier.align(Alignment.CenterVertically)
                        ) {
                            Text("أضف", color = Color.Black)
                        }
                    }

                    state.requirementsListState.forEachIndexed { idx, reqItem ->
                        val parts = reqItem.split("|")
                        val reqName = parts.getOrNull(0) ?: reqItem
                        val isMand = parts.getOrNull(1)?.lowercase() != "optional"
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text("${idx + 1}. $reqName", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                Text(
                                    if (isMand) "إلزامي (مطلوب لإنشاء الحساب) 🔴" else "اختياري (غير معرقل للتسجيل) 🟢",
                                    color = if (isMand) Color.Red.copy(alpha = 0.8f) else Color.Green,
                                    fontSize = 10.sp
                                )
                            }

                            var isEditingItem by remember { mutableStateOf(false) }
                            var editItemValue by remember { mutableStateOf(reqName) }
                            var editItemMandatory by remember { mutableStateOf(isMand) }

                            if (isEditingItem) {
                                AlertDialog(
                                    onDismissRequest = { isEditingItem = false },
                                    title = { Text("📝 تعديل الشرط أو المرفق") },
                                    text = {
                                        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                            OutlinedTextField(
                                                value = editItemValue,
                                                onValueChange = { editItemValue = it },
                                                label = { Text("اسم الشرط أو المستند") },
                                                colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White)
                                            )
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Text("شرط إلزامي للجميع؟")
                                                Switch(checked = editItemMandatory, onCheckedChange = { editItemMandatory = it })
                                            }
                                        }
                                    },
                                    confirmButton = {
                                        Button(onClick = {
                                            if (editItemValue.trim().isNotEmpty()) {
                                                val updatedList = state.requirementsListState.toMutableList()
                                                val suff = if (editItemMandatory) "|Mandatory" else "|Optional"
                                                updatedList[idx] = "${editItemValue.trim()}$suff"
                                                state.requirementsListState = updatedList
                                                isEditingItem = false
                                            }
                                        }, colors = ButtonDefaults.buttonColors(containerColor = themeColors.accent)) {
                                            Text("حفظ التعديل")
                                        }
                                    },
                                    dismissButton = {
                                        TextButton(onClick = { isEditingItem = false }) {
                                            Text("إلغاء")
                                        }
                                    }
                                )
                            }

                            Row(verticalAlignment = Alignment.CenterVertically) {
                                IconButton(onClick = { isEditingItem = true }) {
                                    Icon(imageVector = Icons.Default.Edit, contentDescription = "تعديل", tint = themeColors.accent, modifier = Modifier.size(18.dp))
                                }
                                IconButton(
                                    onClick = { state.requirementsListState = state.requirementsListState.filterIndexed { pIdx, _ -> pIdx != idx } }
                                ) {
                                    Icon(imageVector = Icons.Default.Close, contentDescription = "حذف الالتزام", tint = Color.Red, modifier = Modifier.size(16.dp))
                                }
                            }
                        }
                    }
                }
            }
        }

        item { Spacer(modifier = Modifier.height(8.dp)) }

        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = themeColors.surface),
                modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                border = BorderStroke(1.dp, themeColors.accent.copy(alpha = 0.3f))
            ) {
                Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("📏 تخصيص مقاسات وأبعاد كروت الفنيين:", fontSize = 12.sp, color = Color.White, fontWeight = FontWeight.Bold)

                    Text("• ارتفاع صورة غلاف الكرت (0 للإخفاء): ${state.editCoverHeight.toInt()}dp", fontSize = 11.sp, color = Color.White)
                    Slider(value = state.editCoverHeight, onValueChange = { state.editCoverHeight = it }, valueRange = 0f..250f)

                    Text("• حجم الصورة الشخصية (Avatar Size): ${state.editAvatarSize.toInt()}dp", fontSize = 11.sp, color = Color.White)
                    Slider(value = state.editAvatarSize, onValueChange = { state.editAvatarSize = it }, valueRange = 30f..100f)

                    Text("• الهامش والتباعد الداخلي للكرت (Padding): ${state.editCardPadding.toInt()}dp", fontSize = 11.sp, color = Color.White)
                    Slider(value = state.editCardPadding, onValueChange = { state.editCardPadding = it }, valueRange = 4f..24f)

                    Text("• المسافات بين عناصر الكرت (Spacing): ${state.editElementSpacing.toInt()}dp", fontSize = 11.sp, color = Color.White)
                    Slider(value = state.editElementSpacing, onValueChange = { state.editElementSpacing = it }, valueRange = 2f..16f)
                }
            }
        }

        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = themeColors.surface),
                modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                border = BorderStroke(1.dp, themeColors.accent.copy(alpha = 0.2f))
            ) {
                Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("🛡️ إظهار وإخفاء شارات التميز والتوثيق بالفنيين:", fontSize = 12.sp, color = Color.White, fontWeight = FontWeight.Bold)

                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("🏆 شارة VIP الذهبية والدرع المحيط بالكرت", fontSize = 11.sp, color = Color.White)
                        Switch(checked = state.editShowVipBadge, onCheckedChange = { state.editShowVipBadge = it })
                    }

                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("🔵 شارة التوثيق الزرقاء المعتمدة", fontSize = 11.sp, color = Color.White)
                        Switch(checked = state.editShowVerifiedBadge, onCheckedChange = { state.editShowVerifiedBadge = it })
                    }

                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("🟢 شارة نجمة التوصية (موصى به)", fontSize = 11.sp, color = Color.White)
                        Switch(checked = state.editShowRecommendedBadge, onCheckedChange = { state.editShowRecommendedBadge = it })
                    }
                }
            }
        }

        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = themeColors.surface),
                modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                border = BorderStroke(1.dp, themeColors.accent.copy(alpha = 0.3f))
            ) {
                Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("🤖 إعدادات المساعد الذكي وسوابق الأعمال الفنية:", fontSize = 12.sp, color = Color.White, fontWeight = FontWeight.Bold)

                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("🎁 تفعيل نقاط الولاء والمشاركة للمساعد الذكي", fontSize = 11.sp, color = Color.White)
                        Switch(checked = state.editShowLoyaltyBanner, onCheckedChange = { state.editShowLoyaltyBanner = it })
                    }

                    Text("📂 أقصى حد لصور سابقة الأعمال التي يرفعها مقدم الخدمة: ${state.editMaxWorkPhotos.toInt()}", fontSize = 11.sp, color = Color.White)
                    Slider(
                        value = state.editMaxWorkPhotos,
                        onValueChange = { state.editMaxWorkPhotos = it },
                        valueRange = 1f..5f,
                        steps = 3
                    )
                }
            }
        }

        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = themeColors.surface),
                modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                border = BorderStroke(1.dp, themeColors.accent.copy(alpha = 0.3f))
            ) {
                Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("📞 التحكم الفائق بأزرار الاتصال والتواصل في الكروت:", fontSize = 12.sp, color = Color.White, fontWeight = FontWeight.Bold)

                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("📞 تفعيل زر الاتصال الهاتفي المباشر", fontSize = 11.sp, color = Color.White)
                            Switch(checked = state.editShowCallButton, onCheckedChange = { state.editShowCallButton = it })
                        }
                        if (state.editShowCallButton) {
                            OutlinedTextField(value = state.editCallButtonColorHex, onValueChange = { state.editCallButtonColorHex = it }, label = { Text("لون زر الاتصال (Hex)") }, modifier = Modifier.fillMaxWidth(), colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White))
                        }
                    }

                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("💬 تفعيل زر المحادثة السريعة واتساب", fontSize = 11.sp, color = Color.White)
                            Switch(checked = state.editShowWhatsappButton, onCheckedChange = { state.editShowWhatsappButton = it })
                        }
                        if (state.editShowWhatsappButton) {
                            OutlinedTextField(value = state.editWhatsappButtonColorHex, onValueChange = { state.editWhatsappButtonColorHex = it }, label = { Text("لون زر واتساب (Hex)") }, modifier = Modifier.fillMaxWidth(), colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White))
                        }
                    }

                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("🔍 تفعيل زر عرض التفاصيل والتقييمات", fontSize = 11.sp, color = Color.White)
                            Switch(checked = state.editShowDetailsButton, onCheckedChange = { state.editShowDetailsButton = it })
                        }
                        if (state.editShowDetailsButton) {
                            OutlinedTextField(value = state.editDetailsButtonColorHex, onValueChange = { state.editDetailsButtonColorHex = it }, label = { Text("لون زر التفاصيل (Hex)") }, modifier = Modifier.fillMaxWidth(), colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White))
                        }
                    }

                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("📅 تفعيل زر طلب الحجز المباشر", fontSize = 11.sp, color = Color.White)
                            Switch(checked = state.editShowBookButton, onCheckedChange = { state.editShowBookButton = it })
                        }
                        if (state.editShowBookButton) {
                            OutlinedTextField(value = state.editBookButtonColorHex, onValueChange = { state.editBookButtonColorHex = it }, label = { Text("لون زر الحجز المباشر (Hex)") }, modifier = Modifier.fillMaxWidth(), colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White))
                        }
                    }
                }
            }
        }

        item {
            Button(
                onClick = {
                    val upToDateSettings = settingsState.copy(
                        customPrimaryHex = state.editPrimaryHex,
                        customSecondaryHex = state.editSecondaryHex,
                        cardBackgroundHex = state.editCardBgHex,
                        providerNameColorHex = state.editProviderNameHex,
                        locationColorHex = state.editLocationHex,
                        ratingColorHex = state.editRatingHex,
                        vipBadgeColorHex = state.editVipBadgeHex,
                        verifiedBadgeColorHex = state.editVerifiedHex,
                        recommendedBadgeColorHex = state.editRecommendedHex,
                        activeFontFamily = state.editFontSelected,
                        chatSize = state.editChatIconSize.toInt(),
                        chatXOffset = state.editChatIconX.toInt(),
                        chatYOffset = state.editChatIconY.toInt(),
                        assistantSize = state.editAssistantIconSize.toInt(),
                        assistantXOffset = state.editAssistantIconX.toInt(),
                        assistantYOffset = state.editAssistantIconY.toInt(),
                        registrationRequirements = state.requirementsListState.joinToString(","),
                        coverHeight = state.editCoverHeight.toInt(),
                        avatarSize = state.editAvatarSize.toInt(),
                        elementSpacing = state.editElementSpacing.toInt(),
                        cardPadding = state.editCardPadding.toInt(),
                        showVipBadge = state.editShowVipBadge,
                        showVerifiedBadge = state.editShowVerifiedBadge,
                        showRecommendedBadge = state.editShowRecommendedBadge,
                        showCallButton = state.editShowCallButton,
                        showWhatsappButton = state.editShowWhatsappButton,
                        showDetailsButton = state.editShowDetailsButton,
                        showBookButton = state.editShowBookButton,
                        callButtonColorHex = state.editCallButtonColorHex,
                        whatsappButtonColorHex = state.editWhatsappButtonColorHex,
                        detailsButtonColorHex = state.editDetailsButtonColorHex,
                        bookButtonColorHex = state.editBookButtonColorHex,
                        showLoyaltyBanner = state.editShowLoyaltyBanner,
                        maxWorkPhotos = state.editMaxWorkPhotos.toInt()
                    )
                    viewModel.updateBackdoorSettings(
                        appName = upToDateSettings.appName,
                        welcomeMsg = upToDateSettings.welcomeMessage,
                        footerMsg = upToDateSettings.footerMessage,
                        themeId = upToDateSettings.activeThemeId,
                        supportPhone = upToDateSettings.supportPhone,
                        supportEmail = upToDateSettings.supportEmail,
                        supportWhatsapp = upToDateSettings.supportWhatsapp,
                        isMaintenance = upToDateSettings.isMaintenanceActive,
                        hiddenFooter = upToDateSettings.hidePromoFooter,
                        botHidden = upToDateSettings.assistantHidden,
                        botSize = upToDateSettings.assistantSize,
                        chatHidden = upToDateSettings.chatHidden,
                        chatSize = upToDateSettings.chatSize,
                        radiusKm = upToDateSettings.maxSearchRadiusKm,
                        isSpeech = upToDateSettings.isSpeechSearchEnabled,
                        isDataSaver = false,
                        imgQuality = 90
                    )
                    viewModel.saveCustomSettingsState(upToDateSettings)
                    Toast.makeText(context, "تم حفظ وضبط وتعميم مظهر الدليل والأزرار والبطاقات بنجاح! 🎉", Toast.LENGTH_SHORT).show()
                },
                colors = ButtonDefaults.buttonColors(containerColor = themeColors.accent),
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("💾 حفظ وحقن جميع تخصيصات المظهر بالدليل الصريح والكامل", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 12.sp)
            }
        }
    }
}
