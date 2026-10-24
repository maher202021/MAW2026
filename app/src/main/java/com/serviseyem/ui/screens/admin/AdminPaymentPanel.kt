@file:OptIn(androidx.compose.foundation.layout.ExperimentalLayoutApi::class, androidx.compose.material3.ExperimentalMaterial3Api::class)

package com.serviseyem.ui.screens.admin
import com.serviseyem.ui.*

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import com.serviseyem.data.PaymentAdminSettingsEntity
import com.serviseyem.ui.MainViewModel
import com.serviseyem.utils.VisualThemePalette

/**
 * 💳 لوحة تحكم المدفوعات والاشتراكات وصلاحيات الأدمن (Admin Payment & Billing Panel)
 * تتيح التحكم بالنظام المالي، ربط أو إلغاء ربط القطاعات، وتعديل أرصدة المحافظ والمدفوعات
 */
@Composable
fun AdminPaymentPanel(
    viewModel: MainViewModel,
    themeColors: VisualThemePalette,
    onToggleSystem: ((Boolean) -> Unit)? = null,
    onUnlinkAll: (() -> Unit)? = null,
    onAdminOverride: ((String, Double) -> Unit)? = null
) {
    val context = LocalContext.current
    val primaryColor = themeColors.primary
    val cardBg = themeColors.surface
    val textColor = themeColors.textPrimary

    var systemEnabled by remember { mutableStateOf(true) }
    var linkBookings by remember { mutableStateOf(true) }
    var linkStores by remember { mutableStateOf(true) }
    var linkRestaurants by remember { mutableStateOf(true) }
    var linkMedical by remember { mutableStateOf(true) }
    var linkProperties by remember { mutableStateOf(true) }
    var linkJobs by remember { mutableStateOf(true) }

    var overrideTargetId by remember { mutableStateOf("") }
    var overrideAmount by remember { mutableStateOf("") }
    var showOverrideDialog by remember { mutableStateOf(false) }

    val gatewayIntegration = remember { com.serviseyem.utils.PaymentGatewayIntegration(context) }
    var gatewaysList by remember { mutableStateOf(gatewayIntegration.getGateways(context)) }
    var editingGateway by remember { mutableStateOf<com.serviseyem.utils.GatewayConfig?>(null) }
    var showEditGatewayDialog by remember { mutableStateOf(false) }

    var editApiKey by remember { mutableStateOf("") }
    var editMerchantId by remember { mutableStateOf("") }
    var editSecret by remember { mutableStateOf("") }
    var editEndpoint by remember { mutableStateOf("") }

    LaunchedEffect(Unit) {
        viewModel?.db?.collection("settings")?.document("payment_config")?.get()
            ?.addOnSuccessListener { doc ->
                if (doc != null && doc.exists()) {
                    systemEnabled = doc.getBoolean("systemEnabled") ?: true
                    linkBookings = doc.getBoolean("linkBookings") ?: true
                    linkStores = doc.getBoolean("linkStores") ?: true
                    linkRestaurants = doc.getBoolean("linkRestaurants") ?: true
                    linkMedical = doc.getBoolean("linkMedical") ?: true
                    linkProperties = doc.getBoolean("linkProperties") ?: true
                    linkJobs = doc.getBoolean("linkJobs") ?: true
                }
            }
    }

    fun savePaymentConfig(
        newSystemEnabled: Boolean = systemEnabled,
        newLinkBookings: Boolean = linkBookings,
        newLinkStores: Boolean = linkStores,
        newLinkRestaurants: Boolean = linkRestaurants,
        newLinkMedical: Boolean = linkMedical,
        newLinkProperties: Boolean = linkProperties,
        newLinkJobs: Boolean = linkJobs
    ) {
        val data = mapOf<String, Any>(
            "systemEnabled" to newSystemEnabled,
            "linkBookings" to newLinkBookings,
            "linkStores" to newLinkStores,
            "linkRestaurants" to newLinkRestaurants,
            "linkMedical" to newLinkMedical,
            "linkProperties" to newLinkProperties,
            "linkJobs" to newLinkJobs,
            "updatedAt" to System.currentTimeMillis()
        )
        viewModel?.db?.collection("settings")?.document("payment_config")?.set(data)
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = cardBg),
                elevation = CardDefaults.cardElevation(4.dp)
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(CircleShape)
                                .background(primaryColor.copy(alpha = 0.18f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Settings,
                                contentDescription = null,
                                tint = primaryColor,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                        Column {
                            Text(
                                text = "لوحة تحكم المدفوعات والاشتراكات",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = textColor
                            )
                            Text(
                                text = "إدارة بوابات الدفع، ربط القطاعات، وتجاوزات الإدارة المالية",
                                style = MaterialTheme.typography.bodySmall,
                                color = textColor.copy(alpha = 0.7f)
                            )
                        }
                    }

                    HorizontalDivider(
                        modifier = Modifier.padding(vertical = 12.dp),
                        color = textColor.copy(alpha = 0.1f)
                    )

                    // Master Toggle
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(if (systemEnabled) primaryColor.copy(alpha = 0.1f) else Color.Red.copy(alpha = 0.1f))
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                "تفعيل نظام الدفع الكلي",
                                fontWeight = FontWeight.Bold,
                                color = textColor,
                                fontSize = 15.sp
                            )
                            Text(
                                if (systemEnabled) "النظام المالي نشط ويعمل بجميع الخدمات" else "النظام المالي معطل مؤقتاً (وضع الدفع المباشر)",
                                fontSize = 12.sp,
                                color = textColor.copy(alpha = 0.7f)
                            )
                        }
                        Switch(
                            checked = systemEnabled,
                            onCheckedChange = {
                                systemEnabled = it
                                savePaymentConfig(newSystemEnabled = it)
                                onToggleSystem?.invoke(it)
                                Toast.makeText(
                                    context,
                                    if (it) "تم تفعيل نظام الدفع الكلي وحفظ الإعداد" else "تم تعطيل نظام الدفع الكلي وحفظ الإعداد",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        )
                    }
                }
            }

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = cardBg)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        "🔗 ربط منظومة الدفع بالقطاعات",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = textColor
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    PaymentSectorToggleRow(
                        title = "خدمات الحجوزات والفنيين",
                        subtitle = "حساب العربون والعمولات تلقائياً",
                        icon = Icons.Default.DateRange,
                        checked = linkBookings,
                        onCheckedChange = {
                            linkBookings = it
                            savePaymentConfig(newLinkBookings = it)
                        }
                    )

                    PaymentSectorToggleRow(
                        title = "المتاجر والمنتجات",
                        subtitle = "الدفع عبر المحافظ الإلكترونية",
                        icon = Icons.Default.ShoppingCart,
                        checked = linkStores,
                        onCheckedChange = {
                            linkStores = it
                            savePaymentConfig(newLinkStores = it)
                        }
                    )

                    PaymentSectorToggleRow(
                        title = "المطاعم والطلبات",
                        subtitle = "تسوية الفواتير والتوصيل",
                        icon = Icons.Default.ShoppingCart,
                        checked = linkRestaurants,
                        onCheckedChange = {
                            linkRestaurants = it
                            savePaymentConfig(newLinkRestaurants = it)
                        }
                    )

                    PaymentSectorToggleRow(
                        title = "المراكز الطبية والعيادات",
                        subtitle = "رسوم المعاينة والاستشارات",
                        icon = Icons.Default.Info,
                        checked = linkMedical,
                        onCheckedChange = {
                            linkMedical = it
                            savePaymentConfig(newLinkMedical = it)
                        }
                    )

                    PaymentSectorToggleRow(
                        title = "العقارات والإيجارات",
                        subtitle = "عربون حجز المعاينة أو الإيجار",
                        icon = Icons.Default.Home,
                        checked = linkProperties,
                        onCheckedChange = {
                            linkProperties = it
                            savePaymentConfig(newLinkProperties = it)
                        }
                    )

                    PaymentSectorToggleRow(
                        title = "بوابة الوظائف",
                        subtitle = "رسوم التقديم أو توثيق الحسابات",
                        icon = Icons.Default.Person,
                        checked = linkJobs,
                        onCheckedChange = {
                            linkJobs = it
                            savePaymentConfig(newLinkJobs = it)
                        }
                    )
                }
            }

            // Dynamic Gateways Management Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = cardBg)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        "🔌 إدارة بوابات الدفع الإلكترونية (Live Gateways)",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = textColor
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    gatewaysList.forEach { gateway ->
                        val isGatewayActive = gateway.isEnabled && gateway.apiKey.isNotBlank()
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 6.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(textColor.copy(alpha = 0.03f))
                                .border(0.5.dp, textColor.copy(alpha = 0.08f), RoundedCornerShape(10.dp))
                                .padding(10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(gateway.name, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = textColor)
                                Text(gateway.description, fontSize = 10.5.sp, color = textColor.copy(alpha = 0.6f))
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                                    modifier = Modifier.padding(top = 4.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(5.dp)
                                            .clip(CircleShape)
                                            .background(if (isGatewayActive) Color(0xFF10B981) else Color(0xFF94A3B8))
                                    )
                                    Text(
                                        text = if (isGatewayActive) "نشط (ربط بوابة حقيقية) ✓" else "غير نشط (وضع محلي تجريبي)",
                                        fontSize = 9.sp,
                                        color = if (isGatewayActive) Color(0xFF10B981) else textColor.copy(alpha = 0.5f),
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }

                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                IconButton(
                                    onClick = {
                                        editingGateway = gateway
                                        editApiKey = if (gateway.apiKey.startsWith("gcm:")) com.serviseyem.utils.SecurityCryptoUtils.decryptCrossDevice(gateway.apiKey) else gateway.apiKey
                                        editMerchantId = if (gateway.merchantId.startsWith("gcm:")) com.serviseyem.utils.SecurityCryptoUtils.decryptCrossDevice(gateway.merchantId) else gateway.merchantId
                                        editSecret = if (gateway.secret.startsWith("gcm:")) com.serviseyem.utils.SecurityCryptoUtils.decryptCrossDevice(gateway.secret) else gateway.secret
                                        editEndpoint = gateway.endpoint
                                        showEditGatewayDialog = true
                                    },
                                    modifier = Modifier.size(32.dp)
                                ) {
                                    Icon(Icons.Default.Edit, contentDescription = "تعديل", tint = primaryColor, modifier = Modifier.size(16.dp))
                                }

                                Switch(
                                    checked = gateway.isEnabled,
                                    onCheckedChange = { checked ->
                                        val updated = gateway.copy(isEnabled = checked)
                                        gatewayIntegration.saveGateway(context, updated)
                                        gatewaysList = gatewayIntegration.getGateways(context)
                                        Toast.makeText(context, "تم حفظ حالة ${gateway.name}", Toast.LENGTH_SHORT).show()
                                    }
                                )
                            }
                        }
                    }
                }
            }

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = cardBg)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        "⚙️ إجراءات وتجاوزات الأدمن (Admin Overrides)",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = textColor
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    Button(
                        onClick = { showOverrideDialog = true },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = primaryColor)
                    ) {
                        Icon(Icons.Default.Build, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("تعديل رصيد محفظة / اعتماد دفع يدوي")
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedButton(
                        onClick = {
                            linkBookings = false
                            linkStores = false
                            linkRestaurants = false
                            linkMedical = false
                            linkProperties = false
                            linkJobs = false
                            savePaymentConfig(
                                newLinkBookings = false,
                                newLinkStores = false,
                                newLinkRestaurants = false,
                                newLinkMedical = false,
                                newLinkProperties = false,
                                newLinkJobs = false
                            )
                            onUnlinkAll?.invoke()
                            Toast.makeText(context, "تم إلغاء الربط الشامل لكل القطاعات وحفظ الحالة", Toast.LENGTH_LONG).show()
                        },
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = MaterialTheme.colorScheme.error
                        ),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.error),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.Clear, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("إلغاء الربط الشامل لكل القطاعات")
                    }
                }
            }

    if (showOverrideDialog) {
        AlertDialog(
            onDismissRequest = { showOverrideDialog = false },
            title = { Text("تعديل رصيد أو تجاوز دفع إداري") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = overrideTargetId,
                        onValueChange = { overrideTargetId = it },
                        label = { Text("معرف المستخدم / المحفظة") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = overrideAmount,
                        onValueChange = { overrideAmount = it },
                        label = { Text("المبلغ (ريال يمني)") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val amt = overrideAmount.toDoubleOrNull() ?: 0.0
                        onAdminOverride?.invoke(overrideTargetId, amt)
                        Toast.makeText(context, "تم تسجيل التعديل الإداري بنجاح: $amt ر.ي", Toast.LENGTH_SHORT).show()
                        showOverrideDialog = false
                    }
                ) {
                    Text("تنفيذ")
                }
            },
            dismissButton = {
                TextButton(onClick = { showOverrideDialog = false }) {
                    Text("إلغاء")
                }
            }
        )
    }

    if (showEditGatewayDialog && editingGateway != null) {
        val gateway = editingGateway!!
        AlertDialog(
            onDismissRequest = { showEditGatewayDialog = false },
            title = { Text("تعديل إعدادات بوابة ${gateway.name}") },
            text = {
                Column(
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    OutlinedTextField(
                        value = editApiKey,
                        onValueChange = { editApiKey = it },
                        label = { Text("مفتاح واجهة التطبيق (API Key)") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = editMerchantId,
                        onValueChange = { editMerchantId = it },
                        label = { Text("معرف التاجر (Merchant ID)") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = editSecret,
                        onValueChange = { editSecret = it },
                        label = { Text("السر الخاص (Secret Key)") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = editEndpoint,
                        onValueChange = { editEndpoint = it },
                        label = { Text("عنوان بوابة الدفع (Endpoint URL)") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                    Text(
                        "🔒 ملاحظة أمنية: يتم تشفير وحفظ المفاتيح السرية محلياً وسحابياً بأعلى معايير الحماية (AES-GCM KeyStore).",
                        fontSize = 9.5.sp,
                        color = MaterialTheme.colorScheme.primary,
                        lineHeight = 13.sp
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val updated = gateway.copy(
                            apiKey = editApiKey,
                            merchantId = editMerchantId,
                            secret = editSecret,
                            endpoint = editEndpoint
                        )
                        gatewayIntegration.saveGateway(context, updated)
                        gatewaysList = gatewayIntegration.getGateways(context)
                        Toast.makeText(context, "تم حفظ الإعدادات المشفرة لـ ${gateway.name} بنجاح ✓", Toast.LENGTH_SHORT).show()
                        showEditGatewayDialog = false
                    }
                ) {
                    Text("حفظ مشفر")
                }
            },
            dismissButton = {
                TextButton(onClick = { showEditGatewayDialog = false }) {
                    Text("إلغاء")
                }
            }
        )
    }
}
}

@Composable
fun PaymentSectorToggleRow(
    title: String,
    subtitle: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            modifier = Modifier.weight(1f),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(20.dp)
            )
            Column {
                Text(title, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                Text(subtitle, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
            }
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange
        )
    }
}
