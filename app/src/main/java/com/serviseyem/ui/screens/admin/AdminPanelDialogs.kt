@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
package com.serviseyem.ui.screens.admin

import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.serviseyem.data.CategoryEntity
import com.serviseyem.data.PaymentWalletEntity
import com.serviseyem.ui.*
import com.serviseyem.utils.SecureHasher
import com.serviseyem.utils.VisualThemePalette
import com.serviseyem.utils.getAreaCoords
import com.serviseyem.utils.getDistance
import com.serviseyem.utils.getProviderCoords

@Composable
internal fun AdminPanelDialogs(
    viewModel: MainViewModel,
    themeColors: VisualThemePalette,
    context: Context,
    state: AdminPanelState,
    categories: List<CategoryEntity>
) {
    // 0. Rejection dialog for pending provider request
    state.rejectingProviderRequest?.let { req ->
        AlertDialog(
            onDismissRequest = { state.rejectingProviderRequest = null },
            title = { Text("📝 توضيح سبب رفض الطلب", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color.White) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("يرجى كتابة سبب رفض طلب انضمام الفني ${req.name}:", fontSize = 11.sp, color = Color.LightGray)
                    OutlinedTextField(
                        value = state.providerRejectionReasonText,
                        onValueChange = { state.providerRejectionReasonText = it },
                        placeholder = { Text("مثال: المستندات المرفقة غير واضحة") },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White)
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.rejectTechnician(req.id, state.providerRejectionReasonText.ifBlank { "لم يستوفِ الشروط المطلوبة" })
                        state.rejectingProviderRequest = null
                        state.providerRejectionReasonText = ""
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Red)
                ) {
                    Text("تأكيد الرفض", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { state.rejectingProviderRequest = null }) {
                    Text("إلغاء", color = Color.LightGray)
                }
            },
            containerColor = Color(0xFF1E293B)
        )
    }

    // 1. Delete category confirmation
    state.showDeleteCategoryConfirmId?.let { catId ->
        val catName = categories.find { it.id == catId }?.name ?: ""
        AlertDialog(
            onDismissRequest = { state.showDeleteCategoryConfirmId = null },
            containerColor = Color(0xFF1E293B),
            title = { Text("⚠️ هل ترغب في حذف القسم؟", color = Color.White, fontWeight = FontWeight.Bold) },
            text = { Text("أنت على وشك حذف قسم الصيانة ($catName) نهائياً. سيتم إزالتها من شريط الانتقالات ومقدمي الخدمات.", color = themeColors.textSecondary) },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.deleteCategory(catId)
                        state.showDeleteCategoryConfirmId = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Red)
                ) {
                    Text("نعم، احذف القسم")
                }
            },
            dismissButton = {
                Button(onClick = { state.showDeleteCategoryConfirmId = null }, colors = ButtonDefaults.buttonColors(containerColor = Color.Gray)) {
                    Text("إلغاء الإجراء")
                }
            }
        )
    }

    // 2. Edit category Dialog
    state.showEditCategoryObj?.let { cat ->
        var editIsMain by remember(cat.id) { mutableStateOf(cat.isMainCategory || cat.parentId.isEmpty()) }
        var editParentId by remember(cat.id) { mutableStateOf(cat.parentId) }

        Dialog(onDismissRequest = { state.showEditCategoryObj = null }) {
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
                modifier = Modifier.padding(16.dp).fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("✏️ تعديل وتخصيص هيكلية القسم", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color.White)

                    OutlinedTextField(
                        value = state.editCatName,
                        onValueChange = { state.editCatName = it },
                        label = { Text("اسم القسم") },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White)
                    )

                    OutlinedTextField(
                        value = state.editCatIcon,
                        onValueChange = { state.editCatIcon = it },
                        label = { Text("أيقونة إيموجي مميزة (مثال: 🚰, ⚡)") },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White)
                    )

                    Text("نوع وهيكلية القسم:", fontSize = 11.sp, color = themeColors.accent, fontWeight = FontWeight.Bold)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Button(
                            onClick = { editIsMain = true; editParentId = "" },
                            colors = ButtonDefaults.buttonColors(containerColor = if (editIsMain) themeColors.accent else Color.DarkGray),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("قسم رئيسي 🟢", fontSize = 10.sp, color = if (editIsMain) Color.Black else Color.White)
                        }
                        Button(
                            onClick = { editIsMain = false },
                            colors = ButtonDefaults.buttonColors(containerColor = if (!editIsMain) themeColors.accent else Color.DarkGray),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("قسم فرعي 🟡", fontSize = 10.sp, color = if (!editIsMain) Color.Black else Color.White)
                        }
                    }

                    if (!editIsMain) {
                        val mainCategories = categories.filter { (it.isMainCategory || it.parentId.isEmpty()) && it.id != cat.id }
                        Text("حدد القسم الرئيسي التابع له:", fontSize = 10.sp, color = Color.White)
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            mainCategories.forEach { parent ->
                                val isSelected = editParentId == parent.id
                                FilterChip(
                                    selected = isSelected,
                                    onClick = { editParentId = parent.id },
                                    label = { Text("${parent.icon} ${parent.name}", fontSize = 10.sp) },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = themeColors.accent,
                                        selectedLabelColor = Color.Black
                                    )
                                )
                            }
                        }
                    }

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Button(
                            onClick = {
                                if (state.editCatName.trim().isNotEmpty()) {
                                    viewModel.editCategory(
                                        categoryId = cat.id,
                                        newName = state.editCatName.trim(),
                                        newIcon = state.editCatIcon.trim(),
                                        parentId = if (!editIsMain) editParentId else "",
                                        isMainCategory = editIsMain
                                    )
                                    state.showEditCategoryObj = null
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = themeColors.accent),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("حفظ الهيكلية", color = Color.Black, fontWeight = FontWeight.Bold)
                        }
                        Button(
                            onClick = { state.showEditCategoryObj = null },
                            colors = ButtonDefaults.buttonColors(containerColor = Color.Gray),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("إلغاء", color = Color.White)
                        }
                    }
                }
            }
        }
    }

    // 2a. Edit City Dialog
    state.showEditCityObj?.let { city ->
        var editArName by remember(city.id) { mutableStateOf(city.nameAr) }
        var editEnName by remember(city.id) { mutableStateOf(city.nameEn) }
        var editIcon by remember(city.id) { mutableStateOf(city.icon.ifEmpty { "📍" }) }
        var editPhotoUrl by remember(city.id) { mutableStateOf(city.photoUrl) }
        var editOrder by remember(city.id) { mutableStateOf(city.sortOrder.toString()) }

        Dialog(onDismissRequest = { state.showEditCityObj = null }) {
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
                modifier = Modifier.padding(16.dp).fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("✏️ تعديل وتخصيص بيانات المحافظة/المدينة", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color.White)

                    OutlinedTextField(
                        value = editArName,
                        onValueChange = { editArName = it },
                        label = { Text("الاسم باللغة العربية") },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White)
                    )

                    OutlinedTextField(
                        value = editEnName,
                        onValueChange = { editEnName = it },
                        label = { Text("الاسم باللغة الإنجليزية") },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White)
                    )

                    OutlinedTextField(
                        value = editIcon,
                        onValueChange = { editIcon = it },
                        label = { Text("أيقونة/إيموجي رمزية (مثال: 🏰, 🏖️, 📍)") },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White)
                    )

                    OutlinedTextField(
                        value = editPhotoUrl,
                        onValueChange = { editPhotoUrl = it },
                        label = { Text("رابط صورة رمزية للمحافظة (URL)") },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White)
                    )

                    OutlinedTextField(
                        value = editOrder,
                        onValueChange = { editOrder = it },
                        label = { Text("رقم الترتيب (الترتيب في القائمة)") },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White)
                    )

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Button(
                            onClick = {
                                if (editArName.trim().isNotEmpty()) {
                                    viewModel.updateCity(
                                        city.copy(
                                            nameAr = editArName.trim(),
                                            nameEn = editEnName.trim(),
                                            icon = editIcon.trim().ifEmpty { "📍" },
                                            photoUrl = editPhotoUrl.trim(),
                                            sortOrder = editOrder.toIntOrNull() ?: 0
                                        )
                                    )
                                    state.showEditCityObj = null
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = themeColors.accent),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("حفظ التغييرات", color = Color.Black, fontWeight = FontWeight.Bold)
                        }
                        Button(
                            onClick = { state.showEditCityObj = null },
                            colors = ButtonDefaults.buttonColors(containerColor = Color.Gray),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("إلغاء", color = Color.White)
                        }
                    }
                }
            }
        }
    }

    // 2b. Merge category Dialog
    state.showMergeCategoryObj?.let { sourceCat ->
        Dialog(onDismissRequest = { state.showMergeCategoryObj = null }) {
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
                modifier = Modifier.padding(16.dp).fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("🔄 دمج وتحويل فنيين ومتاجر قسم الصيانة", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color.White)
                    Text(
                        "أنت على وشك دمج قسم (${sourceCat.icon} ${sourceCat.name}). سيتم نقل جميع الفنيين والمتاجر المسجلين في هذا القسم إلى القسم الذي تحدده بالأسفل، ثم سيتم حذف هذا القسم نهائياً.",
                        fontSize = 11.sp,
                        color = Color.LightGray
                    )

                    val otherCategories = categories.filter { it.id != sourceCat.id }

                    if (otherCategories.isEmpty()) {
                        Text("⚠️ لا توجد أقسام أخرى متاحة للدمج معها!", color = Color.Red, fontSize = 12.sp)
                    } else {
                        var expanded by remember { mutableStateOf(false) }
                        val selectedTarget = categories.find { it.id == state.selectedTargetCategoryIdForMerge }

                        Text("اختر القسم المستهدف للدمج والتحويل إليه:", fontSize = 12.sp, color = themeColors.textSecondary)

                        Box(modifier = Modifier.fillMaxWidth()) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(Color.White.copy(alpha = 0.05f), RoundedCornerShape(8.dp))
                                    .clickable { expanded = true }
                                    .padding(12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = selectedTarget?.let { "${it.icon} ${it.name}" } ?: "اختر قسم آخر...",
                                    color = Color.White,
                                    fontSize = 13.sp
                                )
                                Icon(
                                    imageVector = Icons.Default.ArrowDropDown,
                                    contentDescription = "عرض",
                                    tint = Color.White
                                )
                            }

                            DropdownMenu(
                                expanded = expanded,
                                onDismissRequest = { expanded = false },
                                modifier = Modifier.background(Color(0xFF1E293B))
                            ) {
                                otherCategories.forEach { targetCat ->
                                    DropdownMenuItem(
                                        text = { Text("${targetCat.icon} ${targetCat.name}", color = Color.White) },
                                        onClick = {
                                            state.selectedTargetCategoryIdForMerge = targetCat.id
                                            expanded = false
                                        }
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(4.dp))

                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Button(
                                onClick = {
                                    if (state.selectedTargetCategoryIdForMerge.isNotEmpty()) {
                                        viewModel.mergeCategories(sourceCat.id, state.selectedTargetCategoryIdForMerge)
                                        state.showMergeCategoryObj = null
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = Color.Red),
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("تأكيد دمج القسم", color = Color.White, fontWeight = FontWeight.Bold)
                            }
                            Button(
                                onClick = { state.showMergeCategoryObj = null },
                                colors = ButtonDefaults.buttonColors(containerColor = Color.Gray),
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("إلغاء", color = Color.White)
                            }
                        }
                    }
                }
            }
        }
    }

    // 3. Delete reservation confirmation Dialog
    state.showDeleteBookingConfirmId?.let { bId ->
        AlertDialog(
            onDismissRequest = { state.showDeleteBookingConfirmId = null },
            containerColor = Color(0xFF1E293B),
            title = { Text("⚠️ هل تقصد حذف الحجز نهائياً؟", color = Color.White) },
            text = { Text("حذف هذا الحجز سيزيله من جدول حجوزات الدعم والمتابعة مباشرة.", color = themeColors.textSecondary) },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.deleteBooking(bId)
                        state.showDeleteBookingConfirmId = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Red)
                ) {
                    Text("نعم، امسح تماماً", color = Color.White)
                }
            },
            dismissButton = {
                Button(onClick = { state.showDeleteBookingConfirmId = null }, colors = ButtonDefaults.buttonColors(containerColor = Color.Gray)) {
                    Text("تراجع")
                }
            }
        )
    }

    // 3b. Booking Rejection Reason Dialog
    state.showRejectionReasonDialogId?.let { bId ->
        AlertDialog(
            onDismissRequest = { state.showRejectionReasonDialogId = null },
            containerColor = Color(0xFF1E293B),
            title = { Text("❌ رفض طلب الحجز", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("الرجاء توضيح سبب رفض طلب الحجز ليتم إرساله للعميل مباشرة في التنبيهات:", color = Color.White, fontSize = 11.sp)
                    OutlinedTextField(
                        value = state.bookingRejectionReasonInput,
                        onValueChange = { state.bookingRejectionReasonInput = it },
                        label = { Text("سبب الرفض (مثال: جدول الأعمال ممتلئ، الموقع خارج التغطية)", fontSize = 11.sp) },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White)
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (state.bookingRejectionReasonInput.trim().isEmpty()) {
                            Toast.makeText(context, "الرجاء كتابة سبب الرفض لتنبيه العميل به", Toast.LENGTH_SHORT).show()
                        } else {
                            viewModel.updateBookingStatus(bId, "REJECTED", state.bookingRejectionReasonInput)
                            state.showRejectionReasonDialogId = null
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Red)
                ) {
                    Text("تأكيد الرفض وإرسال السبب", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                Button(
                    onClick = { state.showRejectionReasonDialogId = null },
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Gray)
                ) {
                    Text("إلغاء", color = Color.White, fontSize = 11.sp)
                }
            }
        )
    }

    // 4. Delete targeted notifications confirmation Dialog
    state.showDeleteNotifConfirmId?.let { nId ->
        AlertDialog(
            onDismissRequest = { state.showDeleteNotifConfirmId = null },
            containerColor = Color(0xFF1E293B),
            title = { Text("⚠️ تأكيد حذف الإشعار الموجه", color = Color.White) },
            text = { Text("هذا الإشعار سيوضع كمنشور تالف وسيتم محوه من قاعدة بيانات الهواتف المحلية والمخزن.", color = themeColors.textSecondary) },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.deleteNotification(nId)
                        state.showDeleteNotifConfirmId = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Red)
                ) {
                    Text("نعم، احذف الذكرى")
                }
            },
            dismissButton = {
                Button(onClick = { state.showDeleteNotifConfirmId = null }, colors = ButtonDefaults.buttonColors(containerColor = Color.Gray)) {
                    Text("تراجع")
                }
            }
        )
    }

    // 5. Active Chat Channel logs visualizer dialog and direct replies
    state.showActiveChatChannelObj?.let { ch ->
        Dialog(onDismissRequest = { state.showActiveChatChannelObj = null }) {
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A)),
                modifier = Modifier.padding(12.dp).fillMaxWidth(),
                border = BorderStroke(1.dp, themeColors.accent)
            ) {
                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    val partnerName = if (ch.isProvider) "مقدم الخدمة: ${ch.userName}" else "مستخدم الدليل: ${ch.userName}"
                    Text("💬 مراقبة الشات: $partnerName", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = themeColors.accent)

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp)
                            .background(Color.Black.copy(alpha = 0.4f), RoundedCornerShape(8.dp))
                            .padding(8.dp)
                    ) {
                        LazyColumn(modifier = Modifier.fillMaxSize(), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            items(ch.messages, key = { it.id }) { msg ->
                                val alignment = if (msg.senderId == "admin") Alignment.End else Alignment.Start
                                val bubbleBg = if (msg.senderId == "admin") themeColors.primary else Color.Gray.copy(alpha = 0.3f)
                                Column(horizontalAlignment = alignment, modifier = Modifier.fillMaxWidth()) {
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(bubbleBg)
                                            .padding(horizontal = 10.dp, vertical = 6.dp)
                                    ) {
                                        Text(msg.message, fontSize = 11.sp, color = Color.White)
                                    }
                                    Text(msg.senderName, fontSize = 9.sp, color = themeColors.textSecondary)
                                }
                            }
                        }
                    }

                    OutlinedTextField(
                        value = state.adminChatReplyInput,
                        onValueChange = { state.adminChatReplyInput = it },
                        label = { Text("اكتب رد المشرف الصريح والكامل هنا...") },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White)
                    )

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Button(
                            onClick = {
                                if (state.adminChatReplyInput.trim().isNotEmpty()) {
                                    viewModel.replyToChatChannel(ch.id, "admin", state.adminChatReplyInput.trim(), "مشرف الدعم")
                                    val updatedCh = viewModel.chatChannels.value.find { it.id == ch.id }
                                    if (updatedCh != null) {
                                        state.showActiveChatChannelObj = updatedCh
                                    }
                                    state.adminChatReplyInput = ""
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color.Green),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("إرسال الرد الموثق", color = Color.Black, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                        Button(
                            onClick = {
                                viewModel.toggleBlockChatChannel(ch.id)
                                val updatedCh = viewModel.chatChannels.value.find { it.id == ch.id }
                                if (updatedCh != null) {
                                    state.showActiveChatChannelObj = updatedCh
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEA580C)),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(if (ch.isBlocked) "إلغاء الحظر" else "حظر الطرفين", fontSize = 11.sp)
                        }
                    }
                }
            }
        }
    }

    // 6. Delete communication log dialog
    state.showDeleteChatConfirmId?.let { chId ->
        AlertDialog(
            onDismissRequest = { state.showDeleteChatConfirmId = null },
            containerColor = Color(0xFF1E293B),
            title = { Text("⚠️ هل أنت متأكد من مسح ملفات السجل صراحة؟", color = Color.White) },
            text = { Text("سيتم اقتطاع وحذف قنوات الشات من قاعدة الداتا فوراً دون أي إمكانية استرجاع.", color = themeColors.textSecondary) },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.deleteChatChannel(chId)
                        state.showDeleteChatConfirmId = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Red)
                ) {
                    Text("حذف ذيل المحادثة كاملة")
                }
            },
            dismissButton = {
                Button(onClick = { state.showDeleteChatConfirmId = null }, colors = ButtonDefaults.buttonColors(containerColor = Color.Gray)) {
                    Text("إلغاء")
                }
            }
        )
    }

    // 7. Wipe confirming AlertDialog
    if (state.showWipeConfirmDialog) {
        AlertDialog(
            onDismissRequest = { state.showWipeConfirmDialog = false },
            containerColor = Color(0xFF0F172A),
            title = { Text("🚨 تأكيد الهوية الأمنية للمصفي", color = Color.Red, fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "هذه العملية فائقة الخطورة وذات تصفية كلية فورية للمركز الفني بالمنصة والدليل اليمني. الرجاء كتابة الرمز السري للأدمن لإكمال المسح المخصص:",
                        color = themeColors.textSecondary,
                        fontSize = 12.sp
                    )
                    OutlinedTextField(
                        value = state.wipeInputPassword,
                        onValueChange = { state.wipeInputPassword = it },
                        label = { Text("كلمة مرور الأدمن") },
                        visualTransformation = PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White)
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (viewModel.verifyAdminOrOwnerPassword(state.wipeInputPassword)) {
                            val selectedCols = buildSelectedCollectionsList(state)
                            val success = viewModel.wipeSelectedDatabaseData(state.wipeInputPassword, selectedCols)
                            if (success) {
                                state.showWipeConfirmDialog = false
                                state.wipeInputPassword = ""
                                Toast.makeText(context, "💥 تم تصفية الفئات المحددة وإعادتها للصفر بنجاح!", Toast.LENGTH_LONG).show()
                            }
                        } else {
                            Toast.makeText(context, "❌ كلمة مرور الأدمن غير صحيحة! تم منع التطهير.", Toast.LENGTH_SHORT).show()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Red)
                ) {
                    Text("تأكيد مسح وتطهير النظام العظيم", color = Color.White)
                }
            },
            dismissButton = {
                Button(
                    onClick = {
                        state.showWipeConfirmDialog = false
                        state.wipeInputPassword = ""
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Gray)
                ) {
                    Text("إلغاء عملية التطهير")
                }
            }
        )
    }

    // 8. Editing Booking Dialog Control
    state.editingBookingObj?.let { booking ->
        var editCustName by rememberSaveable(booking.id) { mutableStateOf(booking.customerName) }
        var editCustPhone by rememberSaveable(booking.id) { mutableStateOf(booking.customerPhone) }
        var editCustArea by rememberSaveable(booking.id) { mutableStateOf(booking.customerArea) }
        var editCustService by rememberSaveable(booking.id) { mutableStateOf(booking.serviceType) }
        var editCustDate by rememberSaveable(booking.id) { mutableStateOf(booking.dateString) }
        var editCustTime by rememberSaveable(booking.id) { mutableStateOf(booking.timeString) }
        var editCustStatus by rememberSaveable(booking.id) { mutableStateOf(booking.status) }
        var editCustPassword by rememberSaveable(booking.id) {
            mutableStateOf(if (booking.pinCode.isNotBlank()) "••••" else "1234")
        }

        Dialog(onDismissRequest = { state.editingBookingObj = null }) {
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
                border = BorderStroke(1.dp, themeColors.accent),
                modifier = Modifier.fillMaxWidth().padding(16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp).verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text("✏️ تعديل بيانات استمارة الحجز", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.White)

                    OutlinedTextField(
                        value = editCustName,
                        onValueChange = { editCustName = it },
                        label = { Text("الاسم") },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White)
                    )

                    OutlinedTextField(
                        value = editCustPhone,
                        onValueChange = { editCustPhone = it },
                        label = { Text("الهاتف") },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White)
                    )

                    OutlinedTextField(
                        value = editCustArea,
                        onValueChange = { editCustArea = it },
                        label = { Text("مكان الإقامة والحي") },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White)
                    )

                    OutlinedTextField(
                        value = editCustService,
                        onValueChange = { editCustService = it },
                        label = { Text("نوع الخدمة المطلوبة") },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White)
                    )

                    OutlinedTextField(
                        value = editCustDate,
                        onValueChange = { editCustDate = it },
                        label = { Text("التاريخ") },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White)
                    )

                    OutlinedTextField(
                        value = editCustTime,
                        onValueChange = { editCustTime = it },
                        label = { Text("الوقت أو الساعة") },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White)
                    )

                    OutlinedTextField(
                        value = editCustPassword,
                        onValueChange = { editCustPassword = it },
                        label = { Text("🔑 رمز المرور السري (تغيير/إعادة تعيين)") },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White)
                    )

                    Text("حدد حالة الحجز:", color = themeColors.textSecondary, fontSize = 11.sp)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        val statuses = listOf("PENDING", "APPROVED", "IN_PROGRESS", "COMPLETED", "REJECTED")
                        statuses.forEach { st ->
                            val isSel = editCustStatus == st
                            val color = when (st) {
                                "PENDING" -> Color(0xFFF59E0B)
                                "APPROVED" -> Color.Green
                                "IN_PROGRESS" -> Color(0xFF3B82F6)
                                "COMPLETED" -> Color(0xFF10B981)
                                else -> Color.Red
                            }
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(if (isSel) color else Color.Black.copy(alpha = 0.3f))
                                    .border(1.dp, if (isSel) Color.White else color.copy(alpha = 0.4f), RoundedCornerShape(6.dp))
                                    .clickable { editCustStatus = st }
                                    .padding(vertical = 6.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = when (st) {
                                        "PENDING" -> "تعليق"
                                        "APPROVED" -> "قبول"
                                        "IN_PROGRESS" -> "عمل"
                                        "COMPLETED" -> "تم"
                                        else -> "رفض"
                                    },
                                    color = if (isSel) Color.White else color,
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = {
                                if (editCustName.trim().isNotEmpty() && editCustPhone.trim().isNotEmpty()) {
                                    val updatedB = booking.copy(
                                        customerName = editCustName.trim(),
                                        customerPhone = editCustPhone.trim(),
                                        customerArea = editCustArea.trim(),
                                        serviceType = editCustService.trim(),
                                        dateString = editCustDate.trim(),
                                        timeString = editCustTime.trim(),
                                        status = editCustStatus,
                                        pinCode = if (editCustPassword.trim() == "••••" || editCustPassword.trim().contains(":")) booking.pinCode else if (editCustPassword.trim().isNotBlank()) SecureHasher.hashPin(editCustPassword.trim()) else booking.pinCode
                                    )
                                    viewModel.updateBooking(updatedB)
                                    state.editingBookingObj = null
                                } else {
                                    viewModel.triggerNotification("⚠️ يجب ملء الاسم والهاتف بالحد الأدنى!")
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = themeColors.accent),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("💾 حفظ ونشر", color = Color.Black, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }

                        Button(
                            onClick = { state.editingBookingObj = null },
                            colors = ButtonDefaults.buttonColors(containerColor = Color.Gray),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("إلغاء الأمر", color = Color.White, fontSize = 11.sp)
                        }
                    }
                }
            }
        }
    }

    // ------------------ PAYMENT SYSTEM DIALOGS ------------------

    // 1. Add/Edit Wallet Dialog
    if (state.showAddWalletDialog) {
        AlertDialog(
            onDismissRequest = { state.showAddWalletDialog = false },
            title = {
                Text(
                    text = if (state.editingWalletObj == null) "➕ إضافة محفظة استقبال أموال جديدة" else "✏️ تعديل بيانات المحفظة",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            },
            text = {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    item {
                        Text("مزود المحفظة المصرفية / الجوالة:", fontSize = 11.sp, color = Color.LightGray)
                        val providersList = listOf(
                            Pair("jeeb", "محفظة جيب 📱"),
                            Pair("alKarimi", "الكريمي ام فلوس / حساب 🏦"),
                            Pair("jawaly", "محفظة جوالي 📲"),
                            Pair("floosi", "ون كاش / فلوسي 💳"),
                            Pair("cashExchange", "حوالة صرافة نقدية (النجم/العمقي/المميز) 💸"),
                            Pair("foreignCurrency", "محفظة عملات أجنبية (USD/SAR) 🌐"),
                            Pair("yemenMobile", "يمن موبايل كاش 🇾🇪"),
                            Pair("mtc", "محفظة MTC ⚡"),
                            Pair("sabafon", "سبأ كاش 📞"),
                            Pair("other", "تحويلات مصرفية أخرى 🌐")
                        )
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            LazyRow(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                items(providersList) { prov ->
                                    val isSelected = state.walletProviderInput == prov.first
                                    Box(
                                        modifier = Modifier
                                            .background(
                                                if (isSelected) themeColors.accent else Color.DarkGray,
                                                RoundedCornerShape(6.dp)
                                            )
                                            .clickable { state.walletProviderInput = prov.first }
                                            .padding(horizontal = 8.dp, vertical = 4.dp)
                                    ) {
                                        Text(prov.second, color = if (isSelected) Color.Black else Color.White, fontSize = 9.sp)
                                    }
                                }
                            }
                        }
                    }

                    item {
                        Text("عملة المحفظة الحالية:", fontSize = 11.sp, color = Color.LightGray)
                        val currenciesList = listOf(
                            Pair("YER", "🇾🇪 ريال يمني YER"),
                            Pair("USD", "💵 دولار أمريكي USD"),
                            Pair("SAR", "🇸🇦 ريال سعودي SAR")
                        )
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            currenciesList.forEach { cur ->
                                val isSelected = state.walletCurrencyInput == cur.first
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .background(if (isSelected) themeColors.accent else Color.DarkGray, RoundedCornerShape(6.dp))
                                        .clickable { state.walletCurrencyInput = cur.first }
                                        .padding(vertical = 6.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(cur.second, color = if (isSelected) Color.Black else Color.White, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }

                    item {
                        Text("غرض/استخدام المحفظة:", fontSize = 11.sp, color = Color.LightGray)
                        val typesList = listOf(
                            Pair("BOTH", "🔄 إيداع وسحب معاً"),
                            Pair("DEPOSIT", "📥 إيداع فقط (استقبال)"),
                            Pair("WITHDRAWAL", "📤 سحب فقط (تحويل للعميل)")
                        )
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            typesList.forEach { typeItem ->
                                val isSelected = state.walletTypeInput == typeItem.first
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .background(if (isSelected) themeColors.accent else Color.DarkGray, RoundedCornerShape(6.dp))
                                        .clickable { state.walletTypeInput = typeItem.first }
                                        .padding(vertical = 6.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(typeItem.second, color = if (isSelected) Color.Black else Color.White, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }

                    item {
                        OutlinedTextField(
                            value = state.walletNumberInput,
                            onValueChange = { state.walletNumberInput = it },
                            label = { Text("رقم المحفظة / رقم الحساب", fontSize = 11.sp) },
                            modifier = Modifier.fillMaxWidth(),
                            colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White)
                        )
                    }

                    item {
                        OutlinedTextField(
                            value = state.walletAccountNameArInput,
                            onValueChange = { state.walletAccountNameArInput = it },
                            label = { Text("اسم صاحب الحساب (بالعربية)", fontSize = 11.sp) },
                            modifier = Modifier.fillMaxWidth(),
                            colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White)
                        )
                    }

                    item {
                        OutlinedTextField(
                            value = state.walletDescriptionInput,
                            onValueChange = { state.walletDescriptionInput = it },
                            label = { Text("وصف قصير / تعليمات سريعة للعميل", fontSize = 11.sp) },
                            modifier = Modifier.fillMaxWidth(),
                            colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White)
                        )
                    }

                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("إظهار المحفظة في خيارات الدفع للعميل", fontSize = 12.sp, color = Color.White)
                            Switch(
                                checked = state.walletIsVisibleInput,
                                onCheckedChange = { state.walletIsVisibleInput = it },
                                colors = SwitchDefaults.colors(checkedThumbColor = themeColors.accent)
                            )
                        }
                    }

                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("تعيين كافتراضية بالدليل", fontSize = 12.sp, color = Color.White)
                            Switch(
                                checked = state.walletIsDefaultInput,
                                onCheckedChange = { state.walletIsDefaultInput = it },
                                colors = SwitchDefaults.colors(checkedThumbColor = themeColors.accent)
                            )
                        }
                    }

                    item {
                        OutlinedTextField(
                            value = state.walletDisplayOrderInput,
                            onValueChange = { state.walletDisplayOrderInput = it },
                            label = { Text("الترتيب في العرض للعميل", fontSize = 11.sp) },
                            modifier = Modifier.fillMaxWidth(),
                            colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White)
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val wallet = PaymentWalletEntity(
                            id = state.editingWalletObj?.id ?: "",
                            provider = state.walletProviderInput,
                            walletNumber = state.walletNumberInput,
                            accountName = state.walletAccountNameInput.ifBlank { state.walletAccountNameArInput },
                            accountNameAr = state.walletAccountNameArInput,
                            description = state.walletDescriptionInput,
                            walletType = state.walletTypeInput,
                            currency = state.walletCurrencyInput,
                            isVisibleToUsers = state.walletIsVisibleInput,
                            isDefault = state.walletIsDefaultInput,
                            displayOrder = state.walletDisplayOrderInput.toIntOrNull() ?: 0,
                            status = state.walletStatusInput
                        )
                        if (state.editingWalletObj == null) {
                            viewModel.addPaymentWallet(wallet)
                        } else {
                            viewModel.updatePaymentWallet(wallet)
                        }
                        state.showAddWalletDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = themeColors.accent)
                ) {
                    Text("حفظ المحفظة 💾", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                }
            },
            dismissButton = {
                TextButton(onClick = { state.showAddWalletDialog = false }) {
                    Text("إلغاء", color = Color.White)
                }
            }
        )
    }

    // 1b. Internal Wallet Transaction Dialog
    val activeWalletForTx = state.selectedWalletForTx
    if (state.showWalletTxDialog && activeWalletForTx != null) {
        val wallet = activeWalletForTx
        AlertDialog(
            onDismissRequest = { state.showWalletTxDialog = false },
            title = {
                Text("💸 معاملة محفظة داخلية (${wallet.ownerName})", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = themeColors.accent)
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("نوع الحساب: ${wallet.ownerType} | الرقم: ${wallet.ownerPhone}", fontSize = 11.sp, color = Color.LightGray)
                    Text("الرصيد الحالي: ${wallet.balance} ريال يمني 🇾🇪", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.White)

                    Divider(color = Color.White.copy(alpha = 0.1f))

                    Text("اختر نوع العملية المالية:", fontSize = 11.sp, color = Color.White)
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        Button(
                            onClick = { state.txTypeInput = "DEPOSIT" },
                            colors = ButtonDefaults.buttonColors(containerColor = if (state.txTypeInput == "DEPOSIT") Color(0xFF10B981) else Color.DarkGray)
                        ) {
                            Text("إيداع ➕", fontSize = 10.sp)
                        }
                        Button(
                            onClick = { state.txTypeInput = "WITHDRAWAL" },
                            colors = ButtonDefaults.buttonColors(containerColor = if (state.txTypeInput == "WITHDRAWAL") Color.Red else Color.DarkGray)
                        ) {
                            Text("سحب ➖", fontSize = 10.sp)
                        }
                        Button(
                            onClick = { state.txTypeInput = "TRANSFER" },
                            colors = ButtonDefaults.buttonColors(containerColor = if (state.txTypeInput == "TRANSFER") themeColors.accent else Color.DarkGray)
                        ) {
                            Text("تحويل 🔄", fontSize = 10.sp, color = if (state.txTypeInput == "TRANSFER") Color.Black else Color.White)
                        }
                    }

                    OutlinedTextField(
                        value = state.txAmountInput,
                        onValueChange = { state.txAmountInput = it },
                        label = { Text("المبلغ بالريال اليمني", fontSize = 11.sp) },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White)
                    )

                    OutlinedTextField(
                        value = state.txNoteInput,
                        onValueChange = { state.txNoteInput = it },
                        label = { Text("ملاحظات/سبب العملية", fontSize = 11.sp) },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White)
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val amt = state.txAmountInput.toDoubleOrNull() ?: 0.0
                        if (amt > 0) {
                            viewModel.performWalletTransaction(
                                walletId = wallet.id,
                                ownerName = wallet.ownerName,
                                ownerPhone = wallet.ownerPhone,
                                ownerType = wallet.ownerType,
                                type = state.txTypeInput,
                                amount = amt,
                                note = state.txNoteInput
                            )
                            state.showWalletTxDialog = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = themeColors.accent)
                ) {
                    Text("تنفيذ العملية ⚡", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                }
            },
            dismissButton = {
                TextButton(onClick = { state.showWalletTxDialog = false }) {
                    Text("إلغاء", color = Color.Gray)
                }
            }
        )
    }

    // 2. Verify Payment Dialog
    state.verifyingPaymentObj?.let { payment ->
        AlertDialog(
            onDismissRequest = { state.verifyingPaymentObj = null },
            title = { Text("✅ تأكيد واعتماد الدفع الوارد", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color.White) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("أنت على وشك اعتماد وتأكيد دفعة بقيمة ${payment.amount} ريال يمني والمحولة عبر ${payment.walletProvider}.", fontSize = 11.sp, color = Color.LightGray)
                    Text("سيعمل هذا التفعيل على تفعيل/اعتماد طلب الحجز المرتبط وتنشيط حساب المعاملة تلقائياً.", fontSize = 11.sp, color = themeColors.accent)

                    OutlinedTextField(
                        value = state.adminVerifyPaymentNote,
                        onValueChange = { state.adminVerifyPaymentNote = it },
                        label = { Text("ملاحظة وتوضيح (اختياري)", fontSize = 11.sp) },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White)
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.verifyPayment(payment.id, true, state.adminVerifyPaymentNote, "مدير الدليل 🛡️")
                        state.verifyingPaymentObj = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981))
                ) {
                    Text("اعتماد وتأكيد ✅", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                }
            },
            dismissButton = {
                TextButton(onClick = { state.verifyingPaymentObj = null }) {
                    Text("إلغاء", color = Color.White)
                }
            }
        )
    }

    // 3. Reject Payment Dialog
    state.rejectingPaymentObj?.let { payment ->
        AlertDialog(
            onDismissRequest = { state.rejectingPaymentObj = null },
            title = { Text("❌ رفض المعاملة وإثبات الدفع", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color.White) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("يرجى توضيح سبب رفض إثبات التحويل للعميل:", fontSize = 11.sp, color = Color.LightGray)

                    OutlinedTextField(
                        value = state.adminRejectPaymentNote,
                        onValueChange = { state.adminRejectPaymentNote = it },
                        label = { Text("سبب الرفض المالي", fontSize = 11.sp) },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White)
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.verifyPayment(payment.id, false, state.adminRejectPaymentNote, "مدير الدليل 🛡️")
                        state.rejectingPaymentObj = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Red)
                ) {
                    Text("تأكيد الرفض ❌", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                }
            },
            dismissButton = {
                TextButton(onClick = { state.rejectingPaymentObj = null }) {
                    Text("إلغاء", color = Color.White)
                }
            }
        )
    }

    // 4. Refund Payment Dialog
    state.refundingPaymentObj?.let { payment ->
        AlertDialog(
            onDismissRequest = { state.refundingPaymentObj = null },
            title = { Text("🔄 إرجاع واسترداد المبلغ", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color.White) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("تنبيه: سيتم تسجيل هذه العملية كـ 'تم الاسترداد' وإلغاء فاعليتها المالية في إحصائيات الدليل.", fontSize = 11.sp, color = Color.LightGray)
                    Text("يرجى كتابة سبب الاسترداد للتسجيل والمتابعة:", fontSize = 11.sp, color = Color.LightGray)

                    OutlinedTextField(
                        value = state.refundReasonInput,
                        onValueChange = { state.refundReasonInput = it },
                        label = { Text("سبب إرجاع المبلغ", fontSize = 11.sp) },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White)
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.refundPayment(payment.id, state.refundReasonInput.ifBlank { "استرداد بناء على طلب الأطراف" })
                        state.refundingPaymentObj = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD97706))
                ) {
                    Text("تأكيد الاسترداد 🔄", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                }
            },
            dismissButton = {
                TextButton(onClick = { state.refundingPaymentObj = null }) {
                    Text("إلغاء", color = Color.White)
                }
            }
        )
    }

    // 8.5 Redirect Booking Dialog Control
    state.redirectingBookingObj?.let { booking ->
        val providersList by viewModel.providers.collectAsState()

        val nearestProvider = remember(booking, providersList) {
            val userCoords = getAreaCoords(booking.customerArea)
            providersList.minByOrNull { tech ->
                val techCoords = getProviderCoords(tech)
                getDistance(userCoords.first, userCoords.second, techCoords.first, techCoords.second)
            }
        }

        var selectedTargetType by remember { mutableStateOf("SPECIFIC") }
        var selectedOtherProviderId by remember { mutableStateOf("") }
        var dropdownExpanded by remember { mutableStateOf(false) }

        Dialog(onDismissRequest = { state.redirectingBookingObj = null }) {
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
                border = BorderStroke(1.5.dp, themeColors.accent),
                modifier = Modifier.fillMaxWidth().padding(16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp).verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text("🔄 توجيه الحجز إلى مسؤول جديد", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Color.White)

                    Text("العميل: ${booking.customerName} - الحي: ${booking.customerArea}", fontSize = 11.sp, color = themeColors.textSecondary)
                    Text("الفني الحالي: ${booking.providerName}", fontSize = 11.sp, color = themeColors.accent)

                    Divider(color = Color.Gray.copy(alpha = 0.3f))

                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth().clickable { selectedTargetType = "ADMIN" }
                        ) {
                            RadioButton(selected = (selectedTargetType == "ADMIN"), onClick = { selectedTargetType = "ADMIN" })
                            Text("المدير (الأدمن) 👑", fontSize = 12.sp, color = Color.White)
                        }

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth().clickable { selectedTargetType = "SPECIFIC" }
                        ) {
                            RadioButton(selected = (selectedTargetType == "SPECIFIC"), onClick = { selectedTargetType = "SPECIFIC" })
                            Text("الفني المذكور: ${booking.providerName} 👷", fontSize = 12.sp, color = Color.White)
                        }

                        nearestProvider?.let { np ->
                            val userCoords = getAreaCoords(booking.customerArea)
                            val techCoords = getProviderCoords(np)
                            val distance = getDistance(userCoords.first, userCoords.second, techCoords.first, techCoords.second)
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.fillMaxWidth().clickable { selectedTargetType = "NEAREST" }
                            ) {
                                RadioButton(selected = (selectedTargetType == "NEAREST"), onClick = { selectedTargetType = "NEAREST" })
                                Text("الفني الأقرب للمستخدم: ${np.name} (يبعد ${String.format("%.1f", distance)} كم) 📍", fontSize = 12.sp, color = Color.White)
                            }
                        }

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth().clickable { selectedTargetType = "OTHER" }
                        ) {
                            RadioButton(selected = (selectedTargetType == "OTHER"), onClick = { selectedTargetType = "OTHER" })
                            Text("توجيه لفني آخر من الدليل 🔎", fontSize = 12.sp, color = Color.White)
                        }
                    }

                    if (selectedTargetType == "OTHER") {
                        Box(modifier = Modifier.fillMaxWidth()) {
                            val selectedProvName = providersList.find { it.id == selectedOtherProviderId }?.name ?: "اختر الفني الآخر..."
                            Button(
                                onClick = { dropdownExpanded = true },
                                colors = ButtonDefaults.buttonColors(containerColor = themeColors.surface),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(selectedProvName, color = Color.White, fontSize = 11.sp)
                            }

                            DropdownMenu(
                                expanded = dropdownExpanded,
                                onDismissRequest = { dropdownExpanded = false },
                                modifier = Modifier.background(Color(0xFF1E293B)).fillMaxWidth(0.8f)
                            ) {
                                providersList.forEach { prov ->
                                    DropdownMenuItem(
                                        text = { Text(prov.name + " (${prov.customCategoryName.ifBlank { "فني" }})", color = Color.White, fontSize = 11.sp) },
                                        onClick = {
                                            selectedOtherProviderId = prov.id
                                            dropdownExpanded = false
                                        }
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = {
                                val targetProvider = when (selectedTargetType) {
                                    "ADMIN" -> Pair("admin", "إدارة الدليل")
                                    "SPECIFIC" -> Pair(booking.providerId, booking.providerName)
                                    "NEAREST" -> Pair(nearestProvider?.id ?: "admin", nearestProvider?.name ?: "إدارة الدليل")
                                    "OTHER" -> {
                                        val op = providersList.find { it.id == selectedOtherProviderId }
                                        Pair(op?.id ?: "admin", op?.name ?: "إدارة الدليل")
                                    }
                                    else -> Pair(booking.providerId, booking.providerName)
                                }

                                val updatedB = booking.copy(
                                    providerId = targetProvider.first,
                                    providerName = targetProvider.second
                                )
                                viewModel.updateBooking(updatedB)

                                viewModel.addNotification(
                                    title = "🔄 تم تعديل وتوجيه حجزك",
                                    message = "عزيزي العميل، تم توجيه حجزك رقم ${booking.id} بنجاح ومسؤولية تنفيذ الخدمة انتقلت إلى: ${targetProvider.second}.",
                                    targetType = "USER",
                                    targetValue = booking.customerPhone
                                )

                                if (targetProvider.first != "admin") {
                                    val targetPhone = providersList.find { it.id == targetProvider.first }?.phone ?: ""
                                    viewModel.addNotification(
                                        title = "📅 تم توجيه حجز جديد لك",
                                        message = "تم توجيه حجز جديد لك من الإدارة.\nالعميل: ${booking.customerName}\nالهاتف: ${booking.customerPhone}\nالمنطقة: ${booking.customerArea}\nالخدمة: ${booking.serviceType}\nيرجى التواصل معه مباشرة للتنسيق.",
                                        targetType = "PROVIDER",
                                        targetValue = targetPhone,
                                        customerPhone = booking.customerPhone,
                                        customerName = booking.customerName
                                    )
                                }

                                state.redirectingBookingObj = null
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = themeColors.accent),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("توجيه 🔄", color = Color.Black, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }

                        Button(
                            onClick = { state.redirectingBookingObj = null },
                            colors = ButtonDefaults.buttonColors(containerColor = Color.Gray),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("إلغاء", color = Color.White, fontSize = 11.sp)
                        }
                    }
                }
            }
        }
    }

    // 9. Editing Supervisor Dialog Control
    state.editingSupervisorObj?.let { supervisor ->
        var editSupName by rememberSaveable(supervisor.id) { mutableStateOf(supervisor.name) }
        var editSupRole by rememberSaveable(supervisor.id) { mutableStateOf(supervisor.role) }
        var editSupPasscode by rememberSaveable(supervisor.id) { mutableStateOf(supervisor.passcodeHash) }
        var editSupPermissions by remember(supervisor.id) { mutableStateOf(supervisor.permissions) }

        Dialog(onDismissRequest = { state.editingSupervisorObj = null }) {
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
                border = BorderStroke(1.dp, themeColors.accent),
                modifier = Modifier.fillMaxWidth().fillMaxHeight(0.9f).padding(8.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp).verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text("✏️ تعديل صلاحيات وبيانات المشرف", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.White)

                    OutlinedTextField(
                        value = editSupName,
                        onValueChange = { editSupName = it },
                        label = { Text("اسم المشرف الثلاثي") },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White)
                    )

                    OutlinedTextField(
                        value = editSupPasscode,
                        onValueChange = { editSupPasscode = it },
                        label = { Text("رمز المرور والدخول (Passcode)") },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White)
                    )

                    Text("اختر الأدوار والصلاحيات الأمنية للمشرف (يمكنك اختيار صلاحية واحدة أو أكثر):", color = themeColors.textSecondary, fontSize = 11.sp)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        val roles = listOf("ADMIN", "AUDITOR", "SUPPORT", "OPERATIONS")
                        roles.forEach { rl ->
                            val isSel = editSupRole.split(",").contains(rl)
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(if (isSel) themeColors.accent else Color.Black.copy(alpha = 0.3f))
                                    .clickable {
                                        val currentSelected = editSupRole.split(",").filter { it.isNotEmpty() }.toMutableList()
                                        if (currentSelected.contains(rl)) {
                                            currentSelected.remove(rl)
                                        } else {
                                            currentSelected.add(rl)
                                        }
                                        if (currentSelected.isEmpty()) {
                                            currentSelected.add("SUPPORT")
                                        }
                                        editSupRole = currentSelected.joinToString(",")
                                    }
                                    .padding(vertical = 8.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = when (rl) {
                                        "ADMIN" -> "مدير 👑"
                                        "AUDITOR" -> "مدقق 🔍"
                                        "OPERATIONS" -> "ميداني 🚗"
                                        else -> "دعم 📞"
                                    },
                                    color = if (isSel) Color.Black else Color.White,
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }

                    AdminPermissionsSelectorView(
                        themeColors = themeColors,
                        selectedPermissions = editSupPermissions,
                        onPermissionsChanged = { editSupPermissions = it }
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = {
                                if (editSupName.trim().isNotEmpty() && editSupPasscode.trim().isNotEmpty()) {
                                    viewModel.editSupervisor(
                                        id = supervisor.id,
                                        name = editSupName.trim(),
                                        role = editSupRole,
                                        passcode = editSupPasscode.trim(),
                                        permissions = editSupPermissions
                                    )
                                    state.editingSupervisorObj = null
                                } else {
                                    viewModel.triggerNotification("⚠️ يرجى كتابة الاسم والرمز بالكامل")
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = themeColors.accent),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("💾 حفظ التعديل والصلاحيات", color = Color.Black, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }

                        Button(
                            onClick = { state.editingSupervisorObj = null },
                            colors = ButtonDefaults.buttonColors(containerColor = Color.Gray),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("إلغاء", color = Color.White, fontSize = 11.sp)
                        }
                    }
                }
            }
        }
    }

    // 10. Password Recovery Notification Action Dialog
    state.selectedRecoveryNotif?.let { notif ->
        var phoneExtract by remember(notif.id) {
            mutableStateOf(
                try {
                    val phoneRegex = Regex("(?:هاتف:|الرقم:|الهاتف:)?\\s*([0-9]{9,10})")
                    phoneRegex.find(notif.message)?.groupValues?.getOrNull(1) ?: ""
                } catch (e: Exception) {
                    ""
                }
            )
        }
        val nameExtract = remember(notif.id) {
            try {
                when {
                    notif.message.contains("المتجر") -> notif.message.split("المتجر").getOrNull(1)?.substringBefore("(")?.trim() ?: "المستخدم"
                    notif.message.contains("الحساب:") -> notif.message.split("الحساب:").getOrNull(1)?.substringBefore("(")?.trim() ?: "المستخدم"
                    else -> "المستخدم"
                }
            } catch (e: Exception) {
                "المستخدم"
            }
        }

        var newPasswordInput by remember(notif.id) { mutableStateOf("123456") }
        var notifyActionChoice by remember(notif.id) { mutableStateOf("DIRECT_PASSWORD") }

        AlertDialog(
            onDismissRequest = { state.selectedRecoveryNotif = null },
            containerColor = Color(0xFF0F172A),
            title = { Text("🔓 إدارة استعادة كلمة المرور للحساب", color = themeColors.accent, fontWeight = FontWeight.Bold, fontSize = 14.sp) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("تفاصيل الطلب: ${notif.message}", fontSize = 11.sp, color = Color.White)

                    OutlinedTextField(
                        value = phoneExtract,
                        onValueChange = { phoneExtract = it },
                        label = { Text("رقم الهاتف المستهدف") },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White)
                    )

                    OutlinedTextField(
                        value = newPasswordInput,
                        onValueChange = { newPasswordInput = it },
                        label = { Text("كلمة المرور الجديدة (أو الحالية)") },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White)
                    )

                    Text("اختر الإجراء الإداري للرد والاشعار:", fontSize = 11.sp, color = themeColors.accent, fontWeight = FontWeight.Bold)

                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth().clickable { notifyActionChoice = "DIRECT_PASSWORD" }) {
                        RadioButton(selected = notifyActionChoice == "DIRECT_PASSWORD", onClick = { notifyActionChoice = "DIRECT_PASSWORD" })
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("إعادة تعيين كلمة المرور وإرسالها بإشعار مباشر للمستخدم", fontSize = 10.sp, color = Color.White)
                    }

                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth().clickable { notifyActionChoice = "VERIFICATION_WHATSAPP" }) {
                        RadioButton(selected = notifyActionChoice == "VERIFICATION_WHATSAPP", onClick = { notifyActionChoice = "VERIFICATION_WHATSAPP" })
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("طلب التحقق عبر الواتس / التليجرام أو المحادثة الفورية", fontSize = 10.sp, color = Color.White)
                    }

                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth().clickable { notifyActionChoice = "INSTANT_CHAT" }) {
                        RadioButton(selected = notifyActionChoice == "INSTANT_CHAT", onClick = { notifyActionChoice = "INSTANT_CHAT" })
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("فتح محادثة فورية مباشرة مع المستخدم لإثبات الهوية", fontSize = 10.sp, color = Color.White)
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (phoneExtract.isNotEmpty() && newPasswordInput.isNotEmpty()) {
                            viewModel.adminResetAccountPassword(
                                phone = phoneExtract,
                                newPassword = newPasswordInput.trim(),
                                notifyAction = notifyActionChoice,
                                customerName = nameExtract
                            )
                            if (notifyActionChoice == "INSTANT_CHAT") {
                                viewModel.getOrCreateChatChannel(
                                    providerId = "admin",
                                    providerName = "الإدارة والدعم",
                                    customerId = phoneExtract,
                                    customerName = nameExtract
                                )
                                Toast.makeText(context, "💬 تم فتح محادثة الدعم وإرسال الإجراء بنجاح!", Toast.LENGTH_LONG).show()
                            } else {
                                Toast.makeText(context, "✅ تم إعادة تعيين كلمة المرور وإرسال الإشعار بنجاح!", Toast.LENGTH_LONG).show()
                            }
                            state.selectedRecoveryNotif = null
                        } else {
                            Toast.makeText(context, "الرجاء التأكد من رقم الهاتف وكلمة المرور", Toast.LENGTH_SHORT).show()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = themeColors.accent)
                ) {
                    Text("تنفيذ الإجراء وإرسال 🚀", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                }
            },
            dismissButton = {
                Button(
                    onClick = { state.selectedRecoveryNotif = null },
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Gray)
                ) {
                    Text("إلغاء", color = Color.White, fontSize = 11.sp)
                }
            }
        )
    }
}
