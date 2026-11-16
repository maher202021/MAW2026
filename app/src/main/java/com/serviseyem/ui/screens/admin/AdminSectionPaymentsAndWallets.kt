package com.serviseyem.ui.screens.admin

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.serviseyem.data.AdminSettingsEntity
import com.serviseyem.data.InternalWalletEntity
import com.serviseyem.data.PaymentEntity
import com.serviseyem.data.PaymentWalletEntity
import com.serviseyem.ui.*
import com.serviseyem.ui.components.OptionCheckboxCard
import com.serviseyem.utils.VisualThemePalette

internal fun LazyListScope.adminPaymentsAndWalletsSection(
    activeSubTab: String,
    viewModel: MainViewModel,
    themeColors: VisualThemePalette,
    state: AdminPanelState,
    settingsState: AdminSettingsEntity,
    internalWallets: List<InternalWalletEntity>,
    paymentWallets: List<PaymentWalletEntity>,
    paymentsList: List<PaymentEntity>
) {
    if (activeSubTab != "PAYMENTS") return

    item {
        Spacer(modifier = Modifier.height(10.dp))
        Text("💳 نظام الدفع والتحقق والمحافظ الرقمية والمكالمات", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = themeColors.accent)
        Text("إدارة خيارات الدفع، المحافظ الجوالية والمستندات، تخصيص شروط الحجز والعربون للمزودين والأقسام، والمحافظ الداخلية في الوقت الفعلي:", fontSize = 11.sp, color = themeColors.textSecondary)
        Spacer(modifier = Modifier.height(12.dp))
    }

    item {
        Card(
            colors = CardDefaults.cardColors(containerColor = themeColors.surface),
            border = BorderStroke(1.dp, themeColors.accent.copy(alpha = 0.3f)),
            modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp)
        ) {
            Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text("⚙️ إعدادات الدفع العامة ونظام العمولات (نمط المربعات ☑)", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = themeColors.accent)

                OptionCheckboxCard(
                    title = "تفعيل نظام الدفع كاملاً بالمنصة",
                    subtitle = "تمكين أو إيقاف كافة خيارات ووظائف الدفع المالي في التطبيق",
                    isChecked = state.isPaymentEnabledInput,
                    onCheckedChange = { state.isPaymentEnabledInput = it },
                    themeColors = themeColors
                )

                OptionCheckboxCard(
                    title = "ربط الدفع باستمارة الحجز",
                    subtitle = "إلزام المستخدمين بالمرور بصفحة اختيار وسيلة الدفع عند الحجز",
                    isChecked = state.isBookingPaymentRequiredInput,
                    onCheckedChange = { state.isBookingPaymentRequiredInput = it },
                    themeColors = themeColors
                )

                OptionCheckboxCard(
                    title = "طلب دفع مقدم (عربون) عند الحجز",
                    subtitle = "اشتراط دفع جزء من مبلغ الخدمة مقدماً لتأكيد جدية الطلب",
                    isChecked = state.requireAdvancePaymentInput,
                    onCheckedChange = { state.requireAdvancePaymentInput = it },
                    themeColors = themeColors
                )

                if (state.requireAdvancePaymentInput) {
                    OutlinedTextField(
                        value = state.advancePaymentPercentInput,
                        onValueChange = { state.advancePaymentPercentInput = it },
                        label = { Text("نسبة الدفع المقدم (مثال: 0.30 لـ 30%)", fontSize = 11.sp) },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White)
                    )

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = state.minAdvanceAmountInput,
                            onValueChange = { state.minAdvanceAmountInput = it },
                            label = { Text("الحد الأدنى للمقدم (ريال)", fontSize = 11.sp) },
                            modifier = Modifier.weight(1f),
                            colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White)
                        )
                        OutlinedTextField(
                            value = state.maxAdvanceAmountInput,
                            onValueChange = { state.maxAdvanceAmountInput = it },
                            label = { Text("الحد الأقصى للمقدم (ريال)", fontSize = 11.sp) },
                            modifier = Modifier.weight(1f),
                            colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White)
                        )
                    }
                }

                OptionCheckboxCard(
                    title = "تفعيل اقتطاع العمولة من مقدم الخدمة",
                    subtitle = "احتساب نسبة عمولة المنصة تلقائياً على كل حجز مكتمل",
                    isChecked = state.isCommissionEnabledInput,
                    onCheckedChange = { state.isCommissionEnabledInput = it },
                    themeColors = themeColors
                )

                if (state.isCommissionEnabledInput) {
                    OutlinedTextField(
                        value = state.paymentCommissionRateInput,
                        onValueChange = { state.paymentCommissionRateInput = it },
                        label = { Text("نسبة عمولة المنصة (مثال: 0.10 لـ 10%)", fontSize = 11.sp) },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White)
                    )
                }

                OptionCheckboxCard(
                    title = "إظهار تبويب المحفظة والدفع بداخل الملفات الشخصية",
                    subtitle = "تمكين قسم المحفظة الرقمية بصفحات المحلات والفنيين والمطاعم والمستخدمين",
                    isChecked = state.showWalletInProfileInput,
                    onCheckedChange = { state.showWalletInProfileInput = it },
                    themeColors = themeColors
                )

                Divider(color = Color.White.copy(alpha = 0.1f), modifier = Modifier.padding(vertical = 4.dp))

                Text("🔗 ربط الحجز الفوري والدفع المتقدم وحالات العربون الخاص", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = themeColors.accent)

                OutlinedTextField(
                    value = state.linkedCategoriesForInstantBookingInput,
                    onValueChange = { state.linkedCategoriesForInstantBookingInput = it },
                    label = { Text("معرفات الأقسام المربوطة بالحجز الفوري (مفصولة بفارزة)", fontSize = 11.sp) },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White)
                )

                OutlinedTextField(
                    value = state.linkedProvidersForDepositInput,
                    onValueChange = { state.linkedProvidersForDepositInput = it },
                    label = { Text("معرفات/أرقام الفنيين والمحلات المشترط عليهم دفع عربون فقط", fontSize = 11.sp) },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White)
                )

                OutlinedTextField(
                    value = state.exemptUsersFromDepositInput,
                    onValueChange = { state.exemptUsersFromDepositInput = it },
                    label = { Text("أرقام هواتف المستخدمين المعفيين من العربون (الحجز المباشر بدون عربون)", fontSize = 11.sp) },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White)
                )

                Divider(color = Color.White.copy(alpha = 0.1f), modifier = Modifier.padding(vertical = 4.dp))

                Text("📞 نظام المكالمات الصوتية المباشرة داخل التطبيق", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = themeColors.accent)

                OptionCheckboxCard(
                    title = "تفعيل نظام المكالمات الصوتية داخل التطبيق",
                    subtitle = "السماح بإجراء مكالمات صوتية مباشرة وفورية بجودة عالية بين الأطراف",
                    isChecked = state.voiceCallsEnabledInput,
                    onCheckedChange = { state.voiceCallsEnabledInput = it },
                    themeColors = themeColors
                )

                if (state.voiceCallsEnabledInput) {
                    OutlinedTextField(
                        value = state.voiceCallsAllowedCategoriesInput,
                        onValueChange = { state.voiceCallsAllowedCategoriesInput = it },
                        label = { Text("الأقسام المصرح لها بالمكالمات (فارغ = للجميع)", fontSize = 11.sp) },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White)
                    )

                    OutlinedTextField(
                        value = state.voiceCallsAllowedProvidersInput,
                        onValueChange = { state.voiceCallsAllowedProvidersInput = it },
                        label = { Text("أرقام هواتف الفنيين والمحلات المصرح لهم بالمكالمات", fontSize = 11.sp) },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedLabelColor = themeColors.accent,
                            unfocusedLabelColor = Color.LightGray
                        )
                    )

                    Text(
                        text = "☑️ حدد الصح أمام اسم الفني أو المحل لمنحه صلاحية إجراء واستقبال المكالمات:",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = themeColors.accent
                    )

                    val allowedList = remember(state.voiceCallsAllowedProvidersInput) {
                        state.voiceCallsAllowedProvidersInput.split(",").map { it.trim() }.filter { it.isNotEmpty() }
                    }

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 160.dp)
                            .verticalScroll(rememberScrollState())
                            .background(Color.Black.copy(alpha = 0.3f), RoundedCornerShape(8.dp))
                            .padding(8.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        val providersList by viewModel.providers.collectAsState()
                        val storesList by viewModel.stores.collectAsState()

                        if (providersList.isEmpty() && storesList.isEmpty()) {
                            Text("لا يوجد مقدمي خدمات أو محلات مسجلة حالياً", fontSize = 10.sp, color = Color.Gray)
                        } else {
                            providersList.filter { !it.isDeleted }.forEach { p ->
                                val identifier = if (p.phone.isNotEmpty()) p.phone else p.id
                                val isChecked = allowedList.contains(identifier) || allowedList.contains(p.id) || allowedList.contains(p.phone)
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable {
                                            val current = allowedList.toMutableList()
                                            if (isChecked) {
                                                current.remove(p.phone)
                                                current.remove(p.id)
                                            } else {
                                                if (p.phone.isNotEmpty()) current.add(p.phone) else current.add(p.id)
                                            }
                                            state.voiceCallsAllowedProvidersInput = current.distinct().joinToString(",")
                                        }
                                        .padding(vertical = 2.dp)
                                ) {
                                    Checkbox(
                                        checked = isChecked,
                                        onCheckedChange = { checked ->
                                            val current = allowedList.toMutableList()
                                            if (!checked) {
                                                current.remove(p.phone)
                                                current.remove(p.id)
                                            } else {
                                                if (p.phone.isNotEmpty()) current.add(p.phone) else current.add(p.id)
                                            }
                                            state.voiceCallsAllowedProvidersInput = current.distinct().joinToString(",")
                                        },
                                        colors = CheckboxDefaults.colors(checkedColor = themeColors.accent)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("🛠️ ${p.name} (${p.phone.ifEmpty { "بدون هاتف" }}) - ${p.area}", fontSize = 10.sp, color = Color.White)
                                }
                            }

                            storesList.filter { !it.isDeleted }.forEach { s ->
                                val identifier = if (s.phone.isNotEmpty()) s.phone else s.id
                                val isChecked = allowedList.contains(identifier) || allowedList.contains(s.id) || allowedList.contains(s.phone)
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable {
                                            val current = allowedList.toMutableList()
                                            if (isChecked) {
                                                current.remove(s.phone)
                                                current.remove(s.id)
                                            } else {
                                                if (s.phone.isNotEmpty()) current.add(s.phone) else current.add(s.id)
                                            }
                                            state.voiceCallsAllowedProvidersInput = current.distinct().joinToString(",")
                                        }
                                        .padding(vertical = 2.dp)
                                ) {
                                    Checkbox(
                                        checked = isChecked,
                                        onCheckedChange = { checked ->
                                            val current = allowedList.toMutableList()
                                            if (!checked) {
                                                current.remove(s.phone)
                                                current.remove(s.id)
                                            } else {
                                                if (s.phone.isNotEmpty()) current.add(s.phone) else current.add(s.id)
                                            }
                                            state.voiceCallsAllowedProvidersInput = current.distinct().joinToString(",")
                                        },
                                        colors = CheckboxDefaults.colors(checkedColor = themeColors.accent)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("🏪 ${s.name} (${s.phone.ifEmpty { "بدون هاتف" }}) - ${s.localNeighborhood}", fontSize = 10.sp, color = Color.White)
                                }
                            }
                        }
                    }

                    OutlinedTextField(
                        value = state.voiceCallsAllowedUsersInput,
                        onValueChange = { state.voiceCallsAllowedUsersInput = it },
                        label = { Text("أرقام هواتف المستخدمين المصرح لهم بالمكالمات الصوتية", fontSize = 11.sp) },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White)
                    )

                    Spacer(modifier = Modifier.height(6.dp))
                    HorizontalDivider(color = Color.White.copy(alpha = 0.1f))
                    Spacer(modifier = Modifier.height(6.dp))
                    Text("🎙️ إعدادات المكالمات الصوتية والمظهر المتقدمة", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = themeColors.accent)

                    OptionCheckboxCard(
                        title = "إخفاء المكالمات الصوتية داخل التطبيق بالكامل",
                        subtitle = "عند التفعيل، سيتم إخفاء زر المكالمة الصوتية من بطاقة المزود ومن شاشات المحادثات",
                        isChecked = state.disableVoiceCallsInput,
                        onCheckedChange = { state.disableVoiceCallsInput = it },
                        themeColors = themeColors
                    )

                    OptionCheckboxCard(
                        title = "إخفاء الشريط العلوي بالكامل (الهيدر)",
                        subtitle = "عند التفعيل، سيتم إخفاء شريط العنوان العلوي الذي يحتوي على اسم التطبيق وأيقونة اللغة بالكامل",
                        isChecked = state.hideTopHeaderBarInput,
                        onCheckedChange = { state.hideTopHeaderBarInput = it },
                        themeColors = themeColors
                    )

                    OutlinedTextField(
                        value = state.customAppNameInput,
                        onValueChange = { state.customAppNameInput = it },
                        label = { Text("تخصيص اسم التطبيق (اتركه فارغاً للاسم الافتراضي)", fontSize = 11.sp) },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White)
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                Button(
                    onClick = {
                        val updatedSettings = settingsState.copy(
                            isPaymentEnabled = state.isPaymentEnabledInput,
                            isBookingPaymentRequired = state.isBookingPaymentRequiredInput,
                            requireAdvancePayment = state.requireAdvancePaymentInput,
                            advancePaymentPercent = state.advancePaymentPercentInput.toFloatOrNull() ?: 0.30f,
                            minAdvanceAmount = state.minAdvanceAmountInput.toDoubleOrNull() ?: 500.0,
                            maxAdvanceAmount = state.maxAdvanceAmountInput.toDoubleOrNull() ?: 50000.0,
                            isCommissionEnabled = state.isCommissionEnabledInput,
                            paymentCommissionRate = state.paymentCommissionRateInput.toFloatOrNull() ?: 0.10f,
                            showWalletInProfile = state.showWalletInProfileInput,
                            linkedCategoriesForInstantBooking = state.linkedCategoriesForInstantBookingInput,
                            linkedProvidersForDeposit = state.linkedProvidersForDepositInput,
                            exemptUsersFromDeposit = state.exemptUsersFromDepositInput,
                            voiceCallsEnabled = state.voiceCallsEnabledInput,
                            voiceCallsAllowedCategories = state.voiceCallsAllowedCategoriesInput,
                            voiceCallsAllowedProviders = state.voiceCallsAllowedProvidersInput,
                            voiceCallsAllowedUsers = state.voiceCallsAllowedUsersInput,
                            disableVoiceCalls = state.disableVoiceCallsInput,
                            hideTopHeaderBar = state.hideTopHeaderBarInput,
                            customAppName = state.customAppNameInput
                        )
                        viewModel.saveCustomSettingsState(updatedSettings)
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = themeColors.accent)
                ) {
                    Text("💾 حفظ ومزامنة كافة الإعدادات فورياً في Firestore", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                }
            }
        }
    }

    item {
        Spacer(modifier = Modifier.height(8.dp))
        Card(
            colors = CardDefaults.cardColors(containerColor = themeColors.surface),
            border = BorderStroke(1.dp, themeColors.accent.copy(alpha = 0.4f)),
            modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp)
        ) {
            Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("🏦 نظام المحافظ الرقمية الداخلية للمحلات والمزودين", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = themeColors.accent)
                    Button(
                        onClick = {
                            val newW = InternalWalletEntity(
                                id = "W_${System.currentTimeMillis().toString().takeLast(6)}",
                                ownerName = "محل/فني جديد",
                                ownerPhone = "770000000",
                                ownerType = "STORE",
                                balance = 0.0
                            )
                            viewModel.saveInternalWallet(newW)
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = themeColors.accent),
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                    ) {
                        Text("➕ إنشاء محفظة جديدة", color = Color.Black, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    }
                }

                Text("إدارة الأرصدة، شحن وتفريغ رصيد الفنيين والمطاعم والمحلات وسجل المعاملات المباشر:", fontSize = 11.sp, color = Color.LightGray)

                if (internalWallets.isEmpty()) {
                    Text("⚠️ لا توجد محافظ داخلية حتى الآن. انقر 'إنشاء محفظة جديدة' للبدء.", fontSize = 11.sp, color = Color.Gray, modifier = Modifier.padding(vertical = 8.dp))
                } else {
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        internalWallets.forEach { w ->
                            Card(
                                colors = CardDefaults.cardColors(containerColor = themeColors.surface.copy(alpha = 0.7f)),
                                border = BorderStroke(1.dp, Color.White.copy(alpha = 0.1f)),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier.padding(10.dp).fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text("${w.ownerName} (${w.ownerType})", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                        Text("📱 رقم التواصل: ${w.ownerPhone}", fontSize = 10.sp, color = Color.LightGray)
                                        Text("💰 الرصيد الحالي: ${w.balance} ريال يمني 🇾🇪", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = themeColors.accent)
                                    }
                                    Button(
                                        onClick = {
                                            state.selectedWalletForTx = w
                                            state.txTypeInput = "DEPOSIT"
                                            state.txAmountInput = "1000"
                                            state.txNoteInput = "شحن رصيد بواسطة الإدارة"
                                            state.showWalletTxDialog = true
                                        },
                                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981)),
                                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                                    ) {
                                        Text("إيداع / سحب 💸", color = Color.Black, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    item {
        Spacer(modifier = Modifier.height(8.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("📱 المحافظ الجوالية المتاحة للعملاء", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = themeColors.accent)
            Button(
                onClick = {
                    state.editingWalletObj = null
                    state.walletNumberInput = ""
                    state.walletAccountNameInput = ""
                    state.walletAccountNameArInput = ""
                    state.walletDescriptionInput = ""
                    state.walletInstructionsInput = ""
                    state.walletIsDefaultInput = false
                    state.walletDisplayOrderInput = "0"
                    state.walletMinTransferInput = "0"
                    state.walletMaxTransferInput = "100000"
                    state.walletStatusInput = "active"
                    state.showAddWalletDialog = true
                },
                colors = ButtonDefaults.buttonColors(containerColor = themeColors.accent),
                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
            ) {
                Text("➕ إضافة محفظة", color = Color.Black, fontSize = 10.sp, fontWeight = FontWeight.Bold)
            }
        }
        Spacer(modifier = Modifier.height(6.dp))
    }

    if (paymentWallets.isEmpty()) {
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = themeColors.surface.copy(alpha = 0.5f)),
                modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp)
            ) {
                Box(modifier = Modifier.fillMaxWidth().padding(16.dp), contentAlignment = Alignment.Center) {
                    Text("⚠️ لا توجد أي محافظ جوالية مضافة حالياً. يرجى إضافة محفظة دفع لاستقبال أموال العملاء.", fontSize = 11.sp, color = Color.Gray)
                }
            }
        }
    } else {
        item {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                paymentWallets.forEach { wallet ->
                    Card(
                        colors = CardDefaults.cardColors(containerColor = themeColors.surface),
                        border = BorderStroke(1.dp, if (wallet.isDefault) themeColors.accent else themeColors.accent.copy(alpha = 0.15f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                val providerName = when (wallet.provider) {
                                    "jeeb" -> "محفظة جيب 📱"
                                    "alKarimi" -> "الكريمي ام فلوس 🏦"
                                    "jawaly" -> "محفظة جوالي 📲"
                                    "yemenMobile" -> "يمن موبايل كاش 🇾🇪"
                                    "mtc" -> "محفظة MTC ⚡"
                                    "sabafon" -> "سبأ كاش 📞"
                                    "youssef" -> "محفظة يوسف 👤"
                                    else -> "محفظة تحويل أخرى 🌐"
                                }
                                Text(providerName, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = themeColors.accent)

                                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                    if (wallet.isDefault) {
                                        Box(
                                            modifier = Modifier
                                                .background(themeColors.accent.copy(alpha = 0.2f), RoundedCornerShape(4.dp))
                                                .padding(horizontal = 6.dp, vertical = 2.dp)
                                        ) {
                                            Text("الافتراضية ⭐", color = themeColors.accent, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                                        }
                                    }
                                    Box(
                                        modifier = Modifier
                                            .background(
                                                if (wallet.currency == "USD") Color(0xFF3B82F6).copy(alpha = 0.2f)
                                                else if (wallet.currency == "SAR") Color(0xFFF59E0B).copy(alpha = 0.2f)
                                                else Color(0xFF10B981).copy(alpha = 0.2f),
                                                RoundedCornerShape(4.dp)
                                            )
                                            .padding(horizontal = 6.dp, vertical = 2.dp)
                                    ) {
                                        Text(
                                            when (wallet.currency) {
                                                "USD" -> "💵 دولار USD"
                                                "SAR" -> "🇸🇦 ريال سعودي"
                                                else -> "🇾🇪 ريال يمني"
                                            },
                                            color = Color.White,
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                    Box(
                                        modifier = Modifier
                                            .background(
                                                if (wallet.status == "active") Color(0xFF10B981).copy(alpha = 0.2f) else Color.Red.copy(alpha = 0.2f),
                                                RoundedCornerShape(4.dp)
                                            )
                                            .padding(horizontal = 6.dp, vertical = 2.dp)
                                    ) {
                                        Text(
                                            if (wallet.status == "active") "نشطة 🟢" else "موقفة 🚫",
                                            color = if (wallet.status == "active") Color(0xFF10B981) else Color.Red,
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            }

                            Text("رقم المحفظة / الحساب: ${wallet.walletNumber}", fontSize = 12.sp, color = Color.White, fontWeight = FontWeight.Bold)
                            Text("اسم صاحب الحساب: ${wallet.accountNameAr.ifBlank { wallet.accountName }}", fontSize = 11.sp, color = Color.LightGray)
                            Text(
                                "النوع: ${when (wallet.walletType) { "DEPOSIT" -> "📥 إيداع فقط"; "WITHDRAWAL" -> "📤 سحب فقط"; else -> "🔄 إيداع وسحب" }} | الرؤية للمستخدم: ${if (wallet.isVisibleToUsers) "👁️ ظاهرة" else "🙈 مخفية"}",
                                fontSize = 10.sp,
                                color = themeColors.accent
                            )
                            if (wallet.description.isNotEmpty()) {
                                Text("الوصف: ${wallet.description}", fontSize = 10.sp, color = Color.Gray)
                            }

                            HorizontalDivider(color = Color.White.copy(alpha = 0.05f), modifier = Modifier.padding(vertical = 4.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                    Button(
                                        onClick = {
                                            state.editingWalletObj = wallet
                                            state.walletProviderInput = wallet.provider
                                            state.walletNumberInput = wallet.walletNumber
                                            state.walletAccountNameInput = wallet.accountName
                                            state.walletAccountNameArInput = wallet.accountNameAr
                                            state.walletDescriptionInput = wallet.description
                                            state.walletInstructionsInput = wallet.instructions
                                            state.walletTypeInput = wallet.walletType
                                            state.walletCurrencyInput = wallet.currency
                                            state.walletIsVisibleInput = wallet.isVisibleToUsers
                                            state.walletIsDefaultInput = wallet.isDefault
                                            state.walletDisplayOrderInput = wallet.displayOrder.toString()
                                            state.walletMinTransferInput = wallet.minTransferAmount.toString()
                                            state.walletMaxTransferInput = wallet.maxTransferAmount.toString()
                                            state.walletStatusInput = wallet.status
                                            state.showAddWalletDialog = true
                                        },
                                        colors = ButtonDefaults.buttonColors(containerColor = Color.Blue.copy(alpha = 0.6f)),
                                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 1.dp)
                                    ) {
                                        Text("تعديل ✏️", color = Color.White, fontSize = 9.sp)
                                    }

                                    Button(
                                        onClick = { viewModel.togglePaymentWalletVisibility(wallet.id, wallet.isVisibleToUsers) },
                                        colors = ButtonDefaults.buttonColors(containerColor = if (wallet.isVisibleToUsers) Color.DarkGray else Color(0xFF059669)),
                                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 1.dp)
                                    ) {
                                        Text(if (wallet.isVisibleToUsers) "إخفاء 🙈" else "إظهار 👁️", color = Color.White, fontSize = 9.sp)
                                    }

                                    Button(
                                        onClick = { viewModel.updatePaymentWallet(wallet.copy(isDefault = true)) },
                                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD97706)),
                                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 1.dp),
                                        enabled = !wallet.isDefault
                                    ) {
                                        Text("افتراضية ⭐", color = Color.White, fontSize = 9.sp)
                                    }

                                    Button(
                                        onClick = {
                                            viewModel.updatePaymentWallet(wallet.copy(status = if (wallet.status == "active") "suspended" else "active"))
                                        },
                                        colors = ButtonDefaults.buttonColors(containerColor = Color.DarkGray),
                                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 1.dp)
                                    ) {
                                        Text(if (wallet.status == "active") "تعطيل ⛔" else "تنشيط 🟢", color = Color.White, fontSize = 9.sp)
                                    }
                                }

                                IconButton(
                                    onClick = { viewModel.deletePaymentWallet(wallet.id) },
                                    modifier = Modifier.size(24.dp)
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

    item {
        Spacer(modifier = Modifier.height(16.dp))
        Text("💸 طلبات المعاملات وإثباتات دفع العملاء الواردة", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = themeColors.accent)
        Text("مراقبة وتأكيد الدفع اليدوي بعد فحص الحساب ومطابقة رقم التحويل المرفق بالرسائل المصرفية لديهم:", fontSize = 11.sp, color = themeColors.textSecondary)
        Spacer(modifier = Modifier.height(6.dp))
    }

    if (paymentsList.isEmpty()) {
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = themeColors.surface.copy(alpha = 0.5f)),
                modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp)
            ) {
                Box(modifier = Modifier.fillMaxWidth().padding(16.dp), contentAlignment = Alignment.Center) {
                    Text("⚠️ لا توجد أي معاملات دفع مسجلة بالمنصة حتى الآن.", fontSize = 11.sp, color = Color.Gray)
                }
            }
        }
    } else {
        item {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                paymentsList.forEach { payment ->
                    Card(
                        colors = CardDefaults.cardColors(containerColor = themeColors.surface),
                        border = BorderStroke(
                            1.dp,
                            when (payment.status) {
                                "COMPLETED" -> Color(0xFF10B981).copy(alpha = 0.5f)
                                "PROCESSING" -> Color(0xFF3B82F6).copy(alpha = 0.5f)
                                "FAILED" -> Color.Red.copy(alpha = 0.5f)
                                else -> Color.Gray.copy(alpha = 0.3f)
                            }
                        ),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("المبلغ: ${payment.amount} ريال يمني 🇾🇪", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color.White)

                                Box(
                                    modifier = Modifier
                                        .background(
                                            when (payment.status) {
                                                "COMPLETED" -> Color(0xFF10B981).copy(alpha = 0.2f)
                                                "PROCESSING" -> Color(0xFF3B82F6).copy(alpha = 0.2f)
                                                "FAILED" -> Color.Red.copy(alpha = 0.2f)
                                                "REFUNDED" -> Color.Magenta.copy(alpha = 0.2f)
                                                else -> Color.Gray.copy(alpha = 0.2f)
                                            },
                                            RoundedCornerShape(4.dp)
                                        )
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    val statusText = when (payment.status) {
                                        "COMPLETED" -> "مكتمل ومؤكد ✅"
                                        "PROCESSING" -> "بانتظار التحقق ⏳"
                                        "FAILED" -> "مرفوض ❌"
                                        "REFUNDED" -> "تم الاسترداد 🔄"
                                        else -> "طلب معلق ⏳"
                                    }
                                    Text(
                                        statusText,
                                        color = when (payment.status) {
                                            "COMPLETED" -> Color(0xFF10B981)
                                            "PROCESSING" -> Color(0xFF3B82F6)
                                            "FAILED" -> Color.Red
                                            "REFUNDED" -> Color.Magenta
                                            else -> Color.Gray
                                        },
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }

                            Text("👤 العميل (معرف): ${payment.userId}", fontSize = 11.sp, color = Color.LightGray)
                            Text("🛠️ فني الخدمة (معرف): ${payment.providerId}", fontSize = 11.sp, color = Color.LightGray)
                            if (payment.bookingId.isNotEmpty()) {
                                Text("📅 رقم طلب الحجز: ${payment.bookingId}", fontSize = 11.sp, color = themeColors.accent)
                            }

                            Divider(color = Color.White.copy(alpha = 0.05f))

                            Text("طريقة الدفع: ${if (payment.method == "mobileWallet") "محفظة جوال" else payment.method}", fontSize = 11.sp, color = Color.White)
                            if (payment.walletProvider.isNotEmpty()) {
                                Text("مزود المحفظة: ${payment.walletProvider} | رقم: ${payment.walletNumber}", fontSize = 11.sp, color = Color.LightGray)
                                Text("اسم المحول: ${payment.walletAccountName}", fontSize = 11.sp, color = Color.LightGray)
                            }
                            if (payment.transferId.isNotEmpty()) {
                                Text("رقم الحوالة/الإحالة: ${payment.transferId}", fontSize = 12.sp, color = themeColors.accent, fontWeight = FontWeight.Bold)
                            }

                            if (payment.transferPhoto.isNotEmpty()) {
                                Spacer(modifier = Modifier.height(4.dp))
                                Text("📸 صورة إثبات التحويل المرفقة:", fontSize = 10.sp, color = Color.LightGray)
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(100.dp)
                                        .background(Color.Black.copy(alpha = 0.3f), RoundedCornerShape(6.dp))
                                        .border(1.dp, Color.White.copy(alpha = 0.1f), RoundedCornerShape(6.dp)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    if (payment.transferPhoto.startsWith("http")) {
                                        Text("🔗 رابط الصورة: ${payment.transferPhoto.take(35)}...", fontSize = 10.sp, color = themeColors.accent)
                                    } else {
                                        Text("🖼️ صورة مشفرة Base64 جاهزة للتحقق", fontSize = 11.sp, color = Color.White)
                                    }
                                }
                            }

                            if (payment.verificationNote.isNotEmpty()) {
                                Text("📝 ملاحظة التحقق: ${payment.verificationNote}", fontSize = 10.sp, color = Color.LightGray)
                            }

                            if (payment.status == "PROCESSING" || payment.status == "PENDING") {
                                Divider(color = Color.White.copy(alpha = 0.05f), modifier = Modifier.padding(vertical = 4.dp))

                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Button(
                                        onClick = {
                                            state.verifyingPaymentObj = payment
                                            state.adminVerifyPaymentNote = "تم التحقق ومطابقة الحوالة بنجاح"
                                        },
                                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981)),
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Text("قبول وتأكيد ✅", color = Color.Black, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                    }

                                    Button(
                                        onClick = {
                                            state.rejectingPaymentObj = payment
                                            state.adminRejectPaymentNote = "رقم التحويل غير صحيح أو الحوالة غير واردة بالحساب"
                                        },
                                        colors = ButtonDefaults.buttonColors(containerColor = Color.Red),
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Text("رفض المعاملة ❌", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                            } else if (payment.status == "COMPLETED") {
                                Divider(color = Color.White.copy(alpha = 0.05f), modifier = Modifier.padding(vertical = 4.dp))
                                Button(
                                    onClick = {
                                        state.refundingPaymentObj = payment
                                        state.refundReasonInput = "تم إلغاء الخدمة أو بطلب من الفني والعميل"
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD97706)),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text("🔄 بدء إجراءات استرداد المبلغ (Refund)", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
