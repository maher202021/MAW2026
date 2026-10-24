package com.serviseyem.ui.screens.dashboard.components

import androidx.compose.foundation.layout.*
import com.serviseyem.ui.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.serviseyem.utils.VisualThemePalette
import com.serviseyem.data.SpecialOfferEntity
import java.util.UUID

@Composable
fun AdvancedOffersManagementDialog(
    ownerId: String = "",
    viewModel: com.serviseyem.ui.screens.dashboard.viewmodels.DashboardExtensionsViewModel = androidx.lifecycle.viewmodel.compose.viewModel(factory = object : androidx.lifecycle.ViewModelProvider.Factory { override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T { return com.serviseyem.ui.screens.dashboard.viewmodels.DashboardExtensionsViewModel(ownerId) as T } }),
    themeColors: VisualThemePalette,
    onDismiss: () -> Unit
) {
    val offers by viewModel.specialOffers.collectAsState()
    var showAddDialog by remember { mutableStateOf(false) }
    var newTitle by remember { mutableStateOf("") }
    var newDiscount by remember { mutableStateOf("10") }
    var newDuration by remember { mutableStateOf("7") }

    Surface(
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surface,
        modifier = Modifier.fillMaxWidth().padding(16.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp).fillMaxWidth()
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("إدارة العروض والتخفيضات 🏷️", fontSize = 18.sp, color = themeColors.primary)
                IconButton(onClick = { showAddDialog = true }) {
                    Icon(Icons.Default.Add, contentDescription = "إضافة عرض", tint = themeColors.primary)
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            if (showAddDialog) {
                OutlinedTextField(
                    value = newTitle,
                    onValueChange = { newTitle = it },
                    label = { Text("عنوان العرض") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = newDiscount,
                        onValueChange = { newDiscount = it },
                        label = { Text("نسبة الخصم %") },
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = newDuration,
                        onValueChange = { newDuration = it },
                        label = { Text("المدة (أيام)") },
                        modifier = Modifier.weight(1f)
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    TextButton(onClick = { showAddDialog = false }) { Text("إلغاء") }
                    Button(
                        onClick = {
                            if (newTitle.isNotBlank()) {
                                val offerId = UUID.randomUUID().toString()
                                val newOffer = SpecialOfferEntity(
                                    id = offerId,
                                    providerId = ownerId,
                                    title = newTitle.trim(),
                                    discountPercent = newDiscount.toIntOrNull() ?: 10,
                                    expiryDate = "بعد ${newDuration.toIntOrNull() ?: 7} أيام",
                                    isEnabled = true
                                )
                                // ✨ م2-ج2: استخدام ViewModel بدلاً من Firestore المباشر
                                viewModel.addSpecialOffer(newOffer)

                                newTitle = ""
                                showAddDialog = false
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = themeColors.primary)
                    ) {
                        Text("حفظ وإضافة")
                    }
                }
                Spacer(modifier = Modifier.height(12.dp))
            }

            LazyColumn(modifier = Modifier.fillMaxWidth().heightIn(max = 400.dp)) {
                items(offers, key = { it.id }) { offer ->
                    Card(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp).fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(offer.title, fontSize = 14.sp)
                                Text("خصم: ${offer.discountPercent}% - المدة: ${offer.expiryDate}", fontSize = 12.sp, color = MaterialTheme.colorScheme.secondary)
                            }
                            Row {
                                Switch(
                                    checked = offer.isEnabled,
                                    onCheckedChange = { active ->
                                        // ✨ م2-ج2: استخدام ViewModel
                                        viewModel.updateSpecialOfferStatus(offer.id, active)
                                    }
                                )
                                IconButton(onClick = {
                                    // ✨ م2-ج2: استخدام ViewModel
                                    viewModel.deleteSpecialOffer(offer.id)
                                }) {
                                    Icon(Icons.Default.Delete, contentDescription = "حذف", tint = MaterialTheme.colorScheme.error)
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
            Button(
                onClick = onDismiss,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = themeColors.primary)
            ) {
                Text("إغلاق")
            }
        }
    }
}
