package com.serviseyem.ui.screens.dashboard.components

import androidx.compose.foundation.background
import com.serviseyem.ui.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.serviseyem.utils.VisualThemePalette
import com.serviseyem.data.SpecialOfferEntity
import com.serviseyem.ui.screens.dashboard.viewmodels.DashboardExtensionsViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModel
import java.util.UUID

/**
 * 🎟️ CouponManager (إدارة كوبونات وقسائم الخصم)
 * توليد رموز ترويجية، تحديد الحد الأقصى للاستخدام، ومراقبة عدد مرات الاستخدام المتبقية.
 */
@Composable
fun CouponManager(
    ownerId: String = "",
    viewModel: DashboardExtensionsViewModel = viewModel(factory = object : ViewModelProvider.Factory { override fun <T : ViewModel> create(modelClass: Class<T>): T { return DashboardExtensionsViewModel(ownerId) as T } }),
    themeColors: VisualThemePalette,
    modifier: Modifier = Modifier
) {
    val coupons by viewModel.coupons.collectAsState()

    var showAddDialog by remember { mutableStateOf(false) }
    var code by remember { mutableStateOf("") }
    var discountPercent by remember { mutableStateOf("10") }
    var maxUses by remember { mutableStateOf("50") }
    var minAmount by remember { mutableStateOf("3000") }

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
        modifier = modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Icon(Icons.Default.Favorite, contentDescription = null, tint = Color(0xFFA855F7))
                    Text(
                        text = "كوبونات وقسائم الخصم",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }

                Button(
                    onClick = { showAddDialog = true },
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFA855F7), contentColor = Color.White),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("كوبون جديد", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }

            coupons.forEach { coupon ->
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = Color(0xFF334155),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                Text(
                                    text = coupon.couponCode,
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Black,
                                    color = Color(0xFFA855F7)
                                )
                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = Color(0xFFA855F7).copy(alpha = 0.2f)
                                ) {
                                    Text(
                                        text = "${coupon.discountPercent}% خصم",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFFA855F7),
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                            }
                            Text(
                                text = "وصف العرض: ${coupon.description}",
                                fontSize = 11.sp,
                                color = Color(0xFF94A3B8)
                            )
                        }

                        Row {
                            IconButton(onClick = {
                                // ✨ م2-ج2: استخدام ViewModel بدلاً من Firestore المباشر
                                viewModel.updateCouponStatus(coupon.id, !coupon.isEnabled)
                            }) {
                                Icon(
                                    if (coupon.isEnabled) Icons.Default.CheckCircle else Icons.Default.Close,
                                    contentDescription = null,
                                    tint = if (coupon.isEnabled) Color(0xFF10B981) else Color(0xFF94A3B8)
                                )
                            }
                            IconButton(onClick = {
                                // ✨ م2-ج2: استخدام ViewModel
                                viewModel.deleteCoupon(coupon.id)
                            }) {
                                Icon(Icons.Default.Delete, contentDescription = null, tint = Color(0xFFEF4444))
                            }
                        }
                    }
                }
            }
        }
    }

    if (showAddDialog) {
        AlertDialog(
            onDismissRequest = { showAddDialog = false },
            title = { Text("إنشاء كود خصم جديد") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = code,
                        onValueChange = { code = it.uppercase() },
                        label = { Text("رمز الكوبون (مثال: PROMO20)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = discountPercent,
                        onValueChange = { discountPercent = it },
                        label = { Text("نسبة الخصم %") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = maxUses,
                        onValueChange = { maxUses = it },
                        label = { Text("أقصى عدد للاستخدام") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = minAmount,
                        onValueChange = { minAmount = it },
                        label = { Text("الحد الأدنى للطلب (ريال)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (code.isNotBlank()) {
                            val couponId = UUID.randomUUID().toString()
                            val newCoupon = SpecialOfferEntity(
                                id = couponId,
                                providerId = ownerId,
                                title = "كوبون خصم $code",
                                description = "كوبون بقيمة ${discountPercent.toIntOrNull() ?: 10}% لـ ${maxUses.toIntOrNull() ?: 50} استخدام بحد أدنى للطلب ${minAmount.toDoubleOrNull() ?: 0.0}",
                                couponCode = code,
                                discountPercent = discountPercent.toIntOrNull() ?: 10,
                                isEnabled = true
                            )
                            // ✨ م2-ج2: استخدام ViewModel
                            viewModel.addCoupon(newCoupon)

                            showAddDialog = false
                            code = ""
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFA855F7), contentColor = Color.White)
                ) {
                    Text("إنشاء الكوبون")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddDialog = false }) {
                    Text("إلغاء")
                }
            }
        )
    }
}
