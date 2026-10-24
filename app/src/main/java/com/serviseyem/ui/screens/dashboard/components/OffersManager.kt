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
import java.util.UUID

/**
 * 🏷️ OffersManager (إدارة العروض والخصومات الخاصة)
 * تمكّن التاجر أو مقدم الخدمة من إضافة، تعديل، وحذف العروض الترويجية وتحديد نسبة الخصم وتاريخ الانتهاء.
 */
@Composable
fun OffersManager(
    ownerId: String = "",
    viewModel: com.serviseyem.ui.screens.dashboard.viewmodels.DashboardExtensionsViewModel = androidx.lifecycle.viewmodel.compose.viewModel(factory = object : androidx.lifecycle.ViewModelProvider.Factory { override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T { return com.serviseyem.ui.screens.dashboard.viewmodels.DashboardExtensionsViewModel(ownerId) as T } }),
    themeColors: VisualThemePalette,
    modifier: Modifier = Modifier
) {
    val offers by viewModel.specialOffers.collectAsState()

    var showAddDialog by remember { mutableStateOf(false) }
    var newTitle by remember { mutableStateOf("") }
    var newDesc by remember { mutableStateOf("") }
    var newDiscount by remember { mutableStateOf("15") }
    var newExpiry by remember { mutableStateOf("2026-10-01") }

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
                    Icon(Icons.Default.Favorite, contentDescription = null, tint = Color(0xFFF59E0B))
                    Text(
                        text = "إدارة العروض والخصومات",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }

                Button(
                    onClick = { showAddDialog = true },
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF59E0B), contentColor = Color(0xFF0F172A)),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("عرض جديد", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }

            if (offers.isEmpty()) {
                Text(
                    text = "لا توجد عروض ترويجية نشطة حالياً",
                    color = Color(0xFF94A3B8),
                    fontSize = 13.sp,
                    modifier = Modifier.padding(vertical = 16.dp)
                )
            } else {
                offers.forEach { offer ->
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
                                        text = offer.title,
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White
                                    )
                                    Surface(
                                        shape = RoundedCornerShape(6.dp),
                                        color = Color(0xFFF59E0B).copy(alpha = 0.2f)
                                    ) {
                                        Text(
                                            text = "${offer.discountPercent}% خصم",
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color(0xFFF59E0B),
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                        )
                                    }
                                }
                                Text(
                                    text = offer.description,
                                    fontSize = 12.sp,
                                    color = Color(0xFFCBD5E1)
                                )
                                Text(
                                    text = "صالح حتى: ${offer.expiryDate}",
                                    fontSize = 10.sp,
                                    color = Color(0xFF94A3B8)
                                )
                            }

                            Row {
                                IconButton(onClick = {
                                    // ✨ م2-ج2: استخدام ViewModel بدلاً من Firestore المباشر
                                    viewModel.updateSpecialOfferStatus(offer.id, !offer.isEnabled)
                                }) {
                                    Icon(
                                        if (offer.isEnabled) Icons.Default.CheckCircle else Icons.Default.Close,
                                        contentDescription = "Toggle",
                                        tint = if (offer.isEnabled) Color(0xFF10B981) else Color(0xFF94A3B8)
                                    )
                                }
                                IconButton(onClick = {
                                    // ✨ م2-ج2: استخدام ViewModel
                                    viewModel.deleteSpecialOffer(offer.id)
                                }) {
                                    Icon(Icons.Default.Delete, contentDescription = "Delete", tint = Color(0xFFEF4444))
                                }
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
            title = { Text("إضافة عرض ترويجي جديد") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = newTitle,
                        onValueChange = { newTitle = it },
                        label = { Text("عنوان العرض") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = newDesc,
                        onValueChange = { newDesc = it },
                        label = { Text("تفاصيل العرض") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = newDiscount,
                        onValueChange = { newDiscount = it },
                        label = { Text("نسبة الخصم %") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = newExpiry,
                        onValueChange = { newExpiry = it },
                        label = { Text("تاريخ الانتهاء (YYYY-MM-DD)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (newTitle.isNotBlank()) {
                            val offerId = UUID.randomUUID().toString()
                            val newOffer = SpecialOfferEntity(
                                id = offerId,
                                providerId = ownerId,
                                title = newTitle.trim(),
                                description = newDesc.trim(),
                                discountPercent = newDiscount.toIntOrNull() ?: 10,
                                expiryDate = newExpiry.trim(),
                                isEnabled = true
                            )
                            // ✨ م2-ج2: استخدام ViewModel
                            viewModel.addSpecialOffer(newOffer)

                            showAddDialog = false
                            newTitle = ""
                            newDesc = ""
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF59E0B), contentColor = Color(0xFF0F172A))
                ) {
                    Text("حفظ العرض")
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
