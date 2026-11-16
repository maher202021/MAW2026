package com.serviseyem.ui.screens.admin

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.serviseyem.data.AdminSettingsEntity
import com.serviseyem.data.BookingEntity
import com.serviseyem.data.NotificationEntity
import com.serviseyem.data.ProviderEntity
import com.serviseyem.data.SupervisorEntity
import com.serviseyem.ui.*
import com.serviseyem.utils.VisualThemePalette

@OptIn(ExperimentalLayoutApi::class)
internal fun LazyListScope.adminOperationsSections(
    activeSubTab: String,
    viewModel: MainViewModel,
    themeColors: VisualThemePalette,
    context: Context,
    state: AdminPanelState,
    settingsState: AdminSettingsEntity,
    bookings: List<BookingEntity>,
    notifications: List<NotificationEntity>,
    activatedProviders: List<ProviderEntity>,
    supervisorsList: List<SupervisorEntity>
) {
    if (activeSubTab == "BOOKINGS") {
        item {
            Text("📅 إدارة حجوزات الصيانة والطلبات والتحكم بالاستمارات", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = themeColors.accent)
            Spacer(modifier = Modifier.height(4.dp))

            val bookingSubTabs = listOf(
                Triple("SERVICES", "🔧 الخدمات والمهن", bookings.count { it.providerId.contains("SERVICES") || it.providerId == "ALL" || !it.providerId.contains("PROPERTIES") && !it.providerId.contains("STORES") && !it.providerId.contains("MEDICAL") && !it.providerId.contains("RESTAURANTS") && !it.providerId.contains("JOBS") }),
                Triple("PROPERTIES", "🏠 العقارات", bookings.count { it.providerId.contains("PROPERTIES") }),
                Triple("STORES", "🏪 المراكز والمحلات", bookings.count { it.providerId.contains("STORES") }),
                Triple("MEDICAL", "🏥 المراكز الطبية", bookings.count { it.providerId.contains("MEDICAL") }),
                Triple("RESTAURANTS", "🍔 المطاعم والكافيهات", bookings.count { it.providerId.contains("RESTAURANTS") }),
                Triple("JOBS", "💼 الوظائف", bookings.count { it.providerId.contains("JOBS") })
            )

            LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp), modifier = Modifier.padding(vertical = 4.dp)) {
                items(bookingSubTabs.size) { index ->
                    val st = bookingSubTabs[index]
                    val isSel = state.adminBookingSubTab == st.first
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(if (isSel) themeColors.accent else themeColors.surface)
                            .clickable { state.adminBookingSubTab = st.first }
                            .padding(horizontal = 10.dp, vertical = 6.dp)
                            .border(1.dp, if (isSel) Color.White else Color.Gray.copy(alpha = 0.3f), RoundedCornerShape(12.dp))
                    ) {
                        Text("${st.second} (${st.third})", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = if (isSel) Color.Black else Color.White)
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
                Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("⚙️ لوحة التحكم بمسار وحقول استمارة الحجز الشاملة:", fontSize = 12.sp, color = Color.White, fontWeight = FontWeight.Bold)

                    val entityTitle = when (state.adminBookingSubTab) {
                        "STORES" -> "المراكز والمحلات"
                        "MEDICAL" -> "المراكز الطبية والعيادات"
                        "RESTAURANTS" -> "المطاعم والكافيهات"
                        "PROPERTIES" -> "العقارات والأملاك"
                        "JOBS" -> "الوظائف والشركات"
                        else -> "الخدمات والصيانة"
                    }
                    val entityBothLabel = when (state.adminBookingSubTab) {
                        "STORES" -> "الأدمن والمتجر 👥"
                        "MEDICAL" -> "الأدمن والمركز الطبي 👥"
                        "RESTAURANTS" -> "الأدمن والمطعم 👥"
                        "PROPERTIES" -> "الأدمن وصاحب العقار 👥"
                        "JOBS" -> "الأدمن وصاحب العمل 👥"
                        else -> "الأدمن والفني 👥"
                    }
                    val entityProviderLabel = when (state.adminBookingSubTab) {
                        "STORES" -> "المتجر مباشرة 🏪"
                        "MEDICAL" -> "المركز الطبي مباشرة 🏥"
                        "RESTAURANTS" -> "المطعم مباشرة 🍔"
                        "PROPERTIES" -> "المعلن مباشرة 🏠"
                        "JOBS" -> "جهة العمل مباشرة 💼"
                        else -> "الفني مباشرة 🛠️"
                    }

                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text("📍 توجيه حجوزات ($entityTitle) الواردة من العملاء:", fontSize = 11.sp, color = themeColors.accent, fontWeight = FontWeight.Bold)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            val rModes = listOf(
                                Pair("BOTH", entityBothLabel),
                                Pair("ADMIN", "الأدمن فقط 👮"),
                                Pair("PROVIDER", entityProviderLabel)
                            )
                            rModes.forEach { mode ->
                                val isSel = settingsState.bookingRouting == mode.first
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(if (isSel) themeColors.accent else Color.Gray.copy(alpha = 0.2f))
                                        .clickable {
                                            viewModel.saveCustomSettingsState(settingsState.copy(bookingRouting = mode.first))
                                        }
                                        .padding(vertical = 6.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = mode.second,
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isSel) Color.Black else Color.White
                                    )
                                }
                            }
                        }
                    }

                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("عرض أيقونة الحجوزات للجمهور:", fontSize = 11.sp, color = themeColors.textSecondary)
                            Switch(
                                checked = settingsState.isBookingsIconVisible,
                                onCheckedChange = { viewModel.saveCustomSettingsState(settingsState.copy(isBookingsIconVisible = it)) },
                                colors = SwitchDefaults.colors(checkedThumbColor = themeColors.accent)
                            )
                        }

                        Text("🔒 صلاحية الدخول وتصفح الحجوزات والمواعيد:", fontSize = 11.sp, color = themeColors.textSecondary)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            val accessOptions = listOf(
                                Pair("ALL", "الجميع 🌍"),
                                Pair("REGISTERED_ONLY", "المسجلين فقط 🔒"),
                                Pair("DISABLED", "معطلة 🚫")
                            )
                            accessOptions.forEach { opt ->
                                val isSel = settingsState.bookingsAccessControl == opt.first
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(if (isSel) themeColors.accent else Color.Gray.copy(alpha = 0.2f))
                                        .clickable {
                                            viewModel.saveCustomSettingsState(settingsState.copy(bookingsAccessControl = opt.first))
                                        }
                                        .padding(vertical = 6.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = opt.second,
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isSel) Color.Black else Color.White
                                    )
                                }
                            }
                        }

                        OutlinedTextField(
                            value = settingsState.blockedUsersForBookings,
                            onValueChange = { viewModel.saveCustomSettingsState(settingsState.copy(blockedUsersForBookings = it)) },
                            label = { Text("أرقام الهواتف المحظورة من نظام الحجوزات (مثال: 777644, 73...)") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White)
                        )
                    }

                    OutlinedTextField(
                        value = settingsState.bookingTerms,
                        onValueChange = { viewModel.saveCustomSettingsState(settingsState.copy(bookingTerms = it)) },
                        label = { Text("شروط الحجز المعروضة للعميل (شروط وأحكام)") },
                        modifier = Modifier.fillMaxWidth(),
                        maxLines = 3,
                        colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White)
                    )

                    Text("✏️ تخصيص وتعديل حقول استمارة طلب الحجز (اسم الحقل):", fontSize = 11.sp, color = Color.Yellow, fontWeight = FontWeight.Bold)

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        OutlinedTextField(
                            value = settingsState.bookingLabelName,
                            onValueChange = { viewModel.saveCustomSettingsState(settingsState.copy(bookingLabelName = it)) },
                            label = { Text("حقل اسم العميل") },
                            modifier = Modifier.weight(1f),
                            colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White)
                        )
                        OutlinedTextField(
                            value = settingsState.bookingLabelPhone,
                            onValueChange = { viewModel.saveCustomSettingsState(settingsState.copy(bookingLabelPhone = it)) },
                            label = { Text("حقل رقم الهاتف") },
                            modifier = Modifier.weight(1f),
                            colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White)
                        )
                    }

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        OutlinedTextField(
                            value = settingsState.bookingLabelArea,
                            onValueChange = { viewModel.saveCustomSettingsState(settingsState.copy(bookingLabelArea = it)) },
                            label = { Text("حقل العنوان والحي") },
                            modifier = Modifier.weight(1f),
                            colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White)
                        )
                        OutlinedTextField(
                            value = settingsState.bookingLabelService,
                            onValueChange = { viewModel.saveCustomSettingsState(settingsState.copy(bookingLabelService = it)) },
                            label = { Text("حقل نوع الخدمة") },
                            modifier = Modifier.weight(1f),
                            colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White)
                        )
                    }
                }
            }
        }

        item {
            val pendingCount = bookings.count { it.status == "PENDING" }
            val inProgressCount = bookings.count { it.status == "IN_PROGRESS" }
            val completedCount = bookings.count { it.status == "COMPLETED" || it.status == "APPROVED" }

            Row(
                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Card(
                    colors = CardDefaults.cardColors(containerColor = themeColors.surface),
                    border = BorderStroke(1.dp, Color(0xFFF59E0B)),
                    modifier = Modifier.weight(1f)
                ) {
                    Column(modifier = Modifier.padding(10.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("⏳ قيد الانتظار", fontSize = 10.sp, color = Color(0xFFF59E0B), fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(pendingCount.toString(), fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.White)
                    }
                }

                Card(
                    colors = CardDefaults.cardColors(containerColor = themeColors.surface),
                    border = BorderStroke(1.dp, Color(0xFF3B82F6)),
                    modifier = Modifier.weight(1f)
                ) {
                    Column(modifier = Modifier.padding(10.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("⚡ جاري العمل", fontSize = 10.sp, color = Color(0xFF3B82F6), fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(inProgressCount.toString(), fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.White)
                    }
                }

                Card(
                    colors = CardDefaults.cardColors(containerColor = themeColors.surface),
                    border = BorderStroke(1.dp, Color(0xFF10B981)),
                    modifier = Modifier.weight(1f)
                ) {
                    Column(modifier = Modifier.padding(10.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("✅ مكتملة", fontSize = 10.sp, color = Color(0xFF10B981), fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(completedCount.toString(), fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.White)
                    }
                }
            }
        }

        val filteredBookings = bookings.filter { b ->
            when (state.adminBookingSubTab) {
                "SERVICES" -> b.providerId.contains("SERVICES") || b.providerId == "ALL" || (!b.providerId.contains("PROPERTIES") && !b.providerId.contains("STORES") && !b.providerId.contains("MEDICAL") && !b.providerId.contains("RESTAURANTS") && !b.providerId.contains("JOBS"))
                "PROPERTIES" -> b.providerId.contains("PROPERTIES")
                "STORES" -> b.providerId.contains("STORES")
                "MEDICAL" -> b.providerId.contains("MEDICAL")
                "RESTAURANTS" -> b.providerId.contains("RESTAURANTS")
                "JOBS" -> b.providerId.contains("JOBS")
                else -> true
            }
        }

        if (filteredBookings.isEmpty()) {
            item {
                Card(colors = CardDefaults.cardColors(containerColor = themeColors.surface), modifier = Modifier.fillMaxWidth()) {
                    Text("لا توجد طلبات حجز مطابقة في هذا القسم حالياً.", fontSize = 11.sp, color = themeColors.textSecondary, modifier = Modifier.padding(16.dp))
                }
            }
        } else {
            items(filteredBookings, key = { it.id }) { b ->
                Card(
                    colors = CardDefaults.cardColors(containerColor = themeColors.surface),
                    border = BorderStroke(1.dp, if (b.status == "PENDING") themeColors.accent else Color.Transparent),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("اسم العميل: ${b.customerName}", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color.White)
                            val bColor = when (b.status) {
                                "APPROVED" -> Color.Green
                                "COMPLETED" -> Color(0xFF10B981)
                                "IN_PROGRESS" -> Color(0xFF3B82F6)
                                "REJECTED" -> Color.Red
                                else -> themeColors.accent
                            }
                            Text(
                                text = when (b.status) {
                                    "APPROVED" -> "معتمد"
                                    "COMPLETED" -> "مكتمل"
                                    "IN_PROGRESS" -> "جاري التنفيذ"
                                    "REJECTED" -> "مرفوض"
                                    else -> "بانتظار الموافقة"
                                },
                                fontSize = 11.sp,
                                color = bColor,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Text("هاتف العميل: ${b.customerPhone}", fontSize = 11.sp, color = themeColors.textSecondary)
                        Text("منطقة السكن والحي: ${b.customerArea}", fontSize = 11.sp, color = themeColors.textSecondary)
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            Text("📅 تاريخ الحجز: ${b.dateString}", fontSize = 11.sp, color = Color.White)
                            Text("🕒 وقت الحجز: ${b.timeString}", fontSize = 11.sp, color = Color.White)
                        }
                        if (b.serviceType.isNotEmpty()) {
                            Text("نوع الخدمة المطلوبة: ${b.serviceType}", fontSize = 11.sp, color = Color.Yellow)
                        }
                        Text("اسم الفني المستهدف: ${b.providerName}", fontSize = 11.sp, color = themeColors.accent)
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                if (b.status == "PENDING" || b.status == "UNDER_REVIEW") {
                                    Button(
                                        onClick = { viewModel.updateBookingStatus(b.id, "IN_PROGRESS") },
                                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF3B82F6)),
                                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                                    ) {
                                        Text("قبول وبدء الحجز", color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                    }
                                    Button(
                                        onClick = {
                                            state.bookingRejectionReasonInput = ""
                                            state.showRejectionReasonDialogId = b.id
                                        },
                                        colors = ButtonDefaults.buttonColors(containerColor = Color.Red),
                                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                                    ) {
                                        Text("رفض الحجز", color = Color.White, fontSize = 10.sp)
                                    }
                                } else if (b.status == "APPROVED") {
                                    Button(
                                        onClick = { viewModel.updateBookingStatus(b.id, "IN_PROGRESS") },
                                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF3B82F6)),
                                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                                    ) {
                                        Text("بدء التنفيذ", color = Color.White, fontSize = 10.sp)
                                    }
                                } else if (b.status == "IN_PROGRESS") {
                                    Button(
                                        onClick = { viewModel.updateBookingStatus(b.id, "COMPLETED") },
                                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981)),
                                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                                    ) {
                                        Text("إكمال الخدمة (مكتمل)", color = Color.White, fontSize = 10.sp)
                                    }
                                }
                            }
                            Row(horizontalArrangement = Arrangement.spacedBy(4.dp), verticalAlignment = Alignment.CenterVertically) {
                                IconButton(
                                    onClick = {
                                        viewModel.openOrCreateChatChannel(
                                            targetId = b.clientId.takeIf { it.isNotBlank() } ?: "user_${b.customerPhone}",
                                            targetType = "CLIENT",
                                            targetName = b.customerName,
                                            targetPhone = b.customerPhone,
                                            relatedEntityId = b.id,
                                            relatedEntityType = "BOOKING"
                                        ) { created ->
                                            created?.let { state.showActiveChatChannelObj = it }
                                        }
                                    }
                                ) {
                                    Icon(imageVector = Icons.Default.Send, contentDescription = "دردشة فورية للتنسيق", tint = Color.Green, modifier = Modifier.size(20.dp))
                                }
                                IconButton(onClick = { state.redirectingBookingObj = b }) {
                                    Icon(imageVector = Icons.Default.Refresh, contentDescription = "توجيه الحجز", tint = Color.Cyan, modifier = Modifier.size(20.dp))
                                }
                                IconButton(onClick = { state.editingBookingObj = b }) {
                                    Icon(imageVector = Icons.Default.Edit, contentDescription = "تعديل الحجز", tint = Color.Green, modifier = Modifier.size(20.dp))
                                }
                                IconButton(onClick = { state.showDeleteBookingConfirmId = b.id }) {
                                    Icon(imageVector = Icons.Default.Delete, contentDescription = "حذف", tint = Color.Red, modifier = Modifier.size(20.dp))
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (activeSubTab == "NOTIFICATIONS") {
        item {
            Spacer(modifier = Modifier.height(14.dp))
            Text("🔔 نظام الإشعارات الذكية الموجهة (Targeted)", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = themeColors.accent)
            Spacer(modifier = Modifier.height(4.dp))
        }

        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = themeColors.surface),
                modifier = Modifier.fillMaxWidth(),
                border = BorderStroke(1.dp, themeColors.accent.copy(alpha = 0.3f))
            ) {
                Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("إرسال إشعار فوري موجه:", fontSize = 12.sp, color = Color.White, fontWeight = FontWeight.Bold)

                    OutlinedTextField(
                        value = state.notifTitleInput,
                        onValueChange = { state.notifTitleInput = it },
                        label = { Text("عنوان الإشعار (مثال: خصم هائل اليوم!)") },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White)
                    )

                    OutlinedTextField(
                        value = state.notifMsgInput,
                        onValueChange = { state.notifMsgInput = it },
                        label = { Text("مضمون الرسالة بالتفصيل") },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White)
                    )

                    Text("نطاق ومجموعة المستهدفين بالبث الفوري:", fontSize = 11.sp, color = themeColors.textSecondary)
                    FlowRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        maxItemsInEachRow = 3
                    ) {
                        listOf(
                            Pair("ALL", "الجميع 🌍"),
                            Pair("USER", "العملاء 👤"),
                            Pair("PROVIDER", "الفنيون 🔧"),
                            Pair("STORE", "المتاجر 🏪"),
                            Pair("RESTAURANT", "المطاعم 🍔"),
                            Pair("MEDICAL", "المراكز الطبية 🏥"),
                            Pair("PROPERTY", "العقارات 🏢"),
                            Pair("JOB", "الوظائف 💼"),
                            Pair("SUPERVISOR", "المشرفون 👮"),
                            Pair("AREA", "محافظة 📍")
                        ).forEach { (type, label) ->
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.clickable { state.notifTargetType = type }.padding(vertical = 4.dp)
                            ) {
                                RadioButton(selected = state.notifTargetType == type, onClick = { state.notifTargetType = type })
                                Text(label, fontSize = 11.sp, color = Color.White)
                            }
                        }
                    }

                    if (state.notifTargetType != "ALL") {
                        OutlinedTextField(
                            value = state.notifTargetValue,
                            onValueChange = { state.notifTargetValue = it },
                            label = {
                                val lbl = when (state.notifTargetType) {
                                    "USER" -> "رقم هاتف العميل للتوصيل"
                                    "PROVIDER" -> "رقم هاتف أو معرّف الفني"
                                    "SUPERVISOR" -> "معرّف/اسم المشرف المالي"
                                    else -> "اسم المحافظة/المدينة المستهدفة"
                                }
                                Text(lbl)
                            },
                            modifier = Modifier.fillMaxWidth(),
                            colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White)
                        )

                        if (state.notifTargetType == "PROVIDER" && activatedProviders.isNotEmpty()) {
                            Text("اختيار سريع من الفنيين المعتمدين بالمنصة:", fontSize = 10.sp, color = themeColors.accent)
                            LazyRow(
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
                            ) {
                                items(activatedProviders) { prov ->
                                    AssistChip(
                                        onClick = { state.notifTargetValue = prov.phone },
                                        label = { Text(prov.name, fontSize = 10.sp, color = Color.White) },
                                        colors = AssistChipDefaults.assistChipColors(containerColor = themeColors.primary.copy(alpha = 0.2f))
                                    )
                                }
                            }
                        }

                        if (state.notifTargetType == "SUPERVISOR" && supervisorsList.isNotEmpty()) {
                            Text("اختيار سريع من المشرفين المتواجدين:", fontSize = 10.sp, color = themeColors.accent)
                            LazyRow(
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
                            ) {
                                items(supervisorsList) { sup ->
                                    AssistChip(
                                        onClick = { state.notifTargetValue = sup.name },
                                        label = { Text(sup.name, fontSize = 10.sp, color = Color.White) },
                                        colors = AssistChipDefaults.assistChipColors(containerColor = themeColors.primary.copy(alpha = 0.2f))
                                    )
                                }
                            }
                        }
                    }

                    Text("⏱️ جدولة البث وتحديد مدة صلاحية الإشعار (اختياري):", fontSize = 11.sp, color = themeColors.textSecondary)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = state.notifDelayHours,
                            onValueChange = { state.notifDelayHours = it },
                            label = { Text("تأخير الإرسال (ساعات)") },
                            placeholder = { Text("فوري = فارغ") },
                            modifier = Modifier.weight(1f),
                            colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White)
                        )

                        OutlinedTextField(
                            value = state.notifValidityHours,
                            onValueChange = { state.notifValidityHours = it },
                            label = { Text("مدة الصلاحية (ساعات)") },
                            placeholder = { Text("دائم = فارغ") },
                            modifier = Modifier.weight(1f),
                            colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White)
                        )
                    }

                    Button(
                        onClick = {
                            if (state.notifTitleInput.trim().isEmpty() || state.notifMsgInput.trim().isEmpty()) {
                                Toast.makeText(context, "يرجى تعبئة العنوان ونص الرسالة", Toast.LENGTH_SHORT).show()
                            } else {
                                val delayH = state.notifDelayHours.toDoubleOrNull() ?: 0.0
                                val validH = state.notifValidityHours.toDoubleOrNull() ?: 0.0

                                val scheduledTime = if (delayH > 0) {
                                    System.currentTimeMillis() + (delayH * 3600 * 1000).toLong()
                                } else 0L

                                val expiryTimestamp = if (validH > 0) {
                                    (if (scheduledTime > 0) scheduledTime else System.currentTimeMillis()) + (validH * 3600 * 1000).toLong()
                                } else 0L

                                viewModel.addNotification(
                                    title = state.notifTitleInput,
                                    message = state.notifMsgInput,
                                    targetType = state.notifTargetType,
                                    targetValue = if (state.notifTargetType == "ALL") "" else state.notifTargetValue,
                                    expiryTimestamp = expiryTimestamp,
                                    scheduledTime = scheduledTime
                                )
                                state.notifTitleInput = ""
                                state.notifMsgInput = ""
                                state.notifTargetValue = ""
                                state.notifDelayHours = ""
                                state.notifValidityHours = ""
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = themeColors.primary),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("بث وإرسال الإشعار للمستهدفين المحددين 🚀", color = Color.White, fontWeight = FontWeight.Bold)
                    }

                    Spacer(modifier = Modifier.height(6.dp))
                    Text("⚡ نماذج سريعة للإرسال والبث الفوري لجميع المستخدمين:", fontSize = 11.sp, color = themeColors.textSecondary)
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        Button(
                            onClick = {
                                viewModel.addNotification(
                                    title = "🔥 عرض حصري محدود من إدارة المنصة",
                                    message = "خصم يصل إلى 35% على خدمات التكييف، التمديدات وصيانة الأجهزة المنزلية اليوم فقط! احجز فنيك الآن عبر التطبيق.",
                                    targetType = "ALL",
                                    targetValue = ""
                                )
                                Toast.makeText(context, "تم بث عرض ترويجي للجميع بنجاح!", Toast.LENGTH_SHORT).show()
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD97706)),
                            modifier = Modifier.weight(1f),
                            contentPadding = PaddingValues(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text("بث عرض ترويجي 📢", color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        }

                        Button(
                            onClick = {
                                viewModel.addNotification(
                                    title = "🛠️ تحديث فني هام للنظام وتطوير الأداء",
                                    message = "عملائنا الأعزاء، نود إعلامكم بإطلاق تحديث جديد للبحث الجغرافي وحساب المسافات بأعلى دقة. نوصي بتحديث التطبيق الآن.",
                                    targetType = "ALL",
                                    targetValue = ""
                                )
                                Toast.makeText(context, "تم بث إعلان تحديث هام للجميع بنجاح!", Toast.LENGTH_SHORT).show()
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2563EB)),
                            modifier = Modifier.weight(1f),
                            contentPadding = PaddingValues(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text("بث تحديث فني ⚙️", color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        item {
            Text("📋 تاريخ الرسائل البوش السابقة (${notifications.size}):", fontSize = 12.sp, color = themeColors.textSecondary)
        }

        items(notifications, key = { it.id }) { n ->
            Card(
                colors = CardDefaults.cardColors(containerColor = themeColors.surface),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text(n.title, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color.White)
                        IconButton(onClick = { state.showDeleteNotifConfirmId = n.id }, modifier = Modifier.size(24.dp)) {
                            Icon(imageVector = Icons.Default.Delete, contentDescription = "حذف", tint = Color.Red, modifier = Modifier.size(16.dp))
                        }
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(n.message, fontSize = 12.sp, color = Color.White)
                    Spacer(modifier = Modifier.height(4.dp))
                    val filterStr = when (n.targetType) {
                        "USER" -> "مستهدف صريح بالهاتف: ${n.targetValue}"
                        "AREA" -> "محافظة يمنية محددة: ${n.targetValue}"
                        else -> "جميع المشتركين بالمنصة"
                    }
                    Text("نطاق الاستهداف: $filterStr", fontSize = 10.sp, color = themeColors.accent, fontWeight = FontWeight.Bold)

                    val isRecovery = n.title.contains("استعادة") || n.title.contains("طلب") || n.message.contains("يطلب") || n.message.contains("هاتف")
                    if (isRecovery) {
                        Spacer(modifier = Modifier.height(6.dp))
                        Button(
                            onClick = { state.selectedRecoveryNotif = n },
                            colors = ButtonDefaults.buttonColors(containerColor = themeColors.secondary),
                            modifier = Modifier.fillMaxWidth().height(32.dp),
                            contentPadding = PaddingValues(0.dp)
                        ) {
                            Text("🔓 إدارة واستعادة كلمة المرور ومراسلة المستخدم", fontSize = 10.sp, color = Color.White, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }

    if (activeSubTab == "VIP") {
        item {
            Text("💳 لوحة التحكم باشتراكات الفنيين والتجديد", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = themeColors.accent)
            Text("إدارة فترات الصلاحية وشارات الإعلانات، وبث إشعارات التحذير قبل الانتهاء بـ 48 ساعة:", fontSize = 11.sp, color = themeColors.textSecondary)
            Spacer(modifier = Modifier.height(6.dp))
        }

        item {
            Button(
                onClick = {
                    var sentCount = 0
                    val fortyEightHoursMs = 48L * 60 * 60 * 1000
                    activatedProviders.forEach { p ->
                        val timeLeft = p.subscriptionExpiry - System.currentTimeMillis()
                        if (timeLeft > 0 && timeLeft <= fortyEightHoursMs) {
                            viewModel.addNotification(
                                title = "تنبيه هام بفترة تجديد الاشتراك",
                                message = "عزيزنا الفني المعتمد ${p.name}، يرجى التنويه بأن اشتراكك الفني ينتهي خلال أقل من 48 ساعة. يرجى تجديد الاشتراك فوراً لتفادي تجميد حسابك.",
                                targetType = "USER",
                                targetValue = p.phone
                            )
                            sentCount++
                        }
                    }
                    if (sentCount > 0) {
                        Toast.makeText(context, "تم بث تنبيهات بوش تلقائية لعدد ($sentCount) فنيين اشتراكهم ينتهي خلال 48 ساعة!", Toast.LENGTH_LONG).show()
                    } else {
                        Toast.makeText(context, "لم يتم العثور على أي فنيين اقترب انتهاء اشتراكهم (تحت 48 ساعة) في السجلات حالياً.", Toast.LENGTH_LONG).show()
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = themeColors.primary),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("🚨 بث تلقائي لتنبيهات 48 ساعة لجميع الفنيين المستهدفين", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
            }
            Spacer(modifier = Modifier.height(10.dp))
        }

        items(activatedProviders, key = { it.id }) { p ->
            val timeLeft = p.subscriptionExpiry - System.currentTimeMillis()
            val daysLeft = (timeLeft / (24L * 60 * 60 * 1000)).toInt()
            val hoursLeft = ((timeLeft % (24L * 60 * 60 * 1000)) / (60L * 60 * 1000)).toInt()

            val timeString = if (timeLeft < 0) {
                "منتهي الصلاحية ❌"
            } else if (daysLeft > 0) {
                "متبقي $daysLeft يوم و$hoursLeft ساعة"
            } else {
                "متبقي $hoursLeft ساعة فقط ⚠️"
            }

            val isNearExpiry = timeLeft > 0 && timeLeft <= (48L * 60 * 60 * 1000)

            Card(
                colors = CardDefaults.cardColors(containerColor = themeColors.surface),
                border = BorderStroke(1.dp, if (isNearExpiry) Color.Red else themeColors.accent.copy(alpha = 0.2f)),
                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
            ) {
                Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Text(p.name, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color.White)
                        Text(
                            text = timeString,
                            fontSize = 11.sp,
                            color = if (timeLeft < 0) Color.Red else if (isNearExpiry) Color.Yellow else Color.Green,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Text("رقم الهاتف: ${p.phone}", fontSize = 11.sp, color = themeColors.textSecondary)
                    Text("حالة الاشتراك الفني: ${p.subscriptionStatus}", fontSize = 11.sp, color = themeColors.accent)

                    if (!p.password.isNullOrBlank()) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text("🔑 كلمة المرور: ${p.password}", fontSize = 11.sp, color = themeColors.accent, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            Button(
                                onClick = {
                                    val whatsappText = "مرحباً يا غالي، كلمة المرور الخاصة بحسابك الفني في دليل خدمات اليمن هي: ${p.password}"
                                    val whatsappUrl = "https://wa.me/967${p.phone.trim().removePrefix("0").removePrefix("+967")}?text=${Uri.encode(whatsappText)}"
                                    try {
                                        context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(whatsappUrl)))
                                    } catch (e: Exception) {
                                        Toast.makeText(context, "فشل فتح واتساب", Toast.LENGTH_SHORT).show()
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF25D366)),
                                modifier = Modifier.height(28.dp),
                                contentPadding = PaddingValues(horizontal = 4.dp, vertical = 2.dp)
                            ) {
                                Text("🟢 واتساب", color = Color.White, fontSize = 8.sp, fontWeight = FontWeight.Bold)
                            }

                            Button(
                                onClick = {
                                    val smsText = "مرحباً يا غالي، كلمة المرور الخاصة بحسابك الفني في دليل خدمات اليمن هي: ${p.password}"
                                    try {
                                        val intent = Intent(Intent.ACTION_SENDTO, Uri.parse("smsto:${p.phone}")).apply {
                                            putExtra("sms_body", smsText)
                                        }
                                        context.startActivity(intent)
                                    } catch (e: Exception) {
                                        Toast.makeText(context, "فشل فتح SMS", Toast.LENGTH_SHORT).show()
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF3B82F6)),
                                modifier = Modifier.height(28.dp),
                                contentPadding = PaddingValues(horizontal = 4.dp, vertical = 2.dp)
                            ) {
                                Text("💬 SMS", color = Color.White, fontSize = 8.sp, fontWeight = FontWeight.Bold)
                            }

                            Button(
                                onClick = {
                                    viewModel.addNotification(
                                        title = "🔑 تذكير بكلمة المرور الخاصة بك",
                                        message = "مرحباً يا غالي، كلمة المرور الخاصة بحسابك الفني هي: ${p.password}",
                                        targetType = "USER",
                                        targetValue = p.phone
                                    )
                                    Toast.makeText(context, "تم إرسال تذكير بكلمة المرور للفني بنجاح كإشعار داخلي", Toast.LENGTH_SHORT).show()
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF8B5CF6)),
                                modifier = Modifier.height(28.dp),
                                contentPadding = PaddingValues(horizontal = 4.dp, vertical = 2.dp)
                            ) {
                                Text("📱 إشعار", color = Color.White, fontSize = 8.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        Button(
                            onClick = {
                                viewModel.extendProviderSubscription(p.id, 30L * 24 * 60 * 60 * 1000)
                                Toast.makeText(context, "تم تجديد اشتراك ${p.name} لمدة 30 يوماً بنجاح", Toast.LENGTH_SHORT).show()
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981)),
                            modifier = Modifier.weight(1f),
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                        ) {
                            Text("تجديد 30 يوم 🟢", color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        }

                        Button(
                            onClick = {
                                viewModel.addNotification(
                                    title = "تنبيه هام بانتهاء صلاحية الاشتراك",
                                    message = "عزيزنا الفني ${p.name}، نود تذكيرك بأن اشتراكك ينتهي خلال 48 ساعة فقط. الرجاء المسارعة بالتجديد للاستمرار بظهور اسمك للزبائن في التطبيق.",
                                    targetType = "USER",
                                    targetValue = p.phone
                                )
                                Toast.makeText(context, "تم إرسال إشعار بوش يدوي ينبه الفني بالفترة المحددة بـ 48 ساعة", Toast.LENGTH_SHORT).show()
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF4444)),
                            modifier = Modifier.weight(1f),
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                        ) {
                            Text("تنبيه بـ 48 ساعة 🔔", color = Color.White, fontSize = 10.sp)
                        }
                    }
                }
            }
        }
    }
}
