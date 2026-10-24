package com.serviseyem.ui.screens.admin

import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.serviseyem.data.StoreEntity
import com.serviseyem.ui.MainViewModel
import com.serviseyem.ui.screens.admin.components.AdminEntityCard
import com.serviseyem.utils.VisualThemePalette

/**
 * 🏥 Admin Panel: Medical & Pharmacies Management (إدارة القطاع الطبي والصيدليات)
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminMedicalPanel(
    onBack: () -> Unit = {},
    viewModel: MainViewModel = viewModel(),
    themeColors: VisualThemePalette,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val allStores by viewModel.stores.collectAsState()

    val medicalStores = remember(allStores) {
        allStores.filter {
            it.sectionId == "medical" || it.categoryId == "medical" ||
            it.providerType == "medical" || it.name.contains("صيدلية") ||
            it.name.contains("عيادة") || it.name.contains("مستشفى") ||
            it.name.contains("طبي") || it.name.contains("دكتور")
        }
    }

    var selectedFilter by remember { mutableStateOf("الكل") }
    var searchQuery by remember { mutableStateOf("") }
    var showAddDialog by remember { mutableStateOf(false) }

    val filteredStores = remember(medicalStores, selectedFilter, searchQuery) {
        medicalStores.filter { store ->
            val matchFilter = when (selectedFilter) {
                "صيدليات" -> store.name.contains("صيدلية") || store.categoryId.contains("pharmacy")
                "عيادات ومراكز" -> store.name.contains("عيادة") || store.name.contains("مركز")
                "مستشفيات" -> store.name.contains("مستشفى")
                "محظور" -> store.isBlocked
                else -> true
            }
            val matchSearch = searchQuery.isBlank() ||
                    store.name.contains(searchQuery, ignoreCase = true) ||
                    store.phone.contains(searchQuery) ||
                    store.cityId.contains(searchQuery, ignoreCase = true)
            matchFilter && matchSearch
        }
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Control Header Card
        Card(
            colors = CardDefaults.cardColors(containerColor = themeColors.surface),
            shape = RoundedCornerShape(14.dp),
            border = BorderStroke(1.dp, themeColors.accent.copy(alpha = 0.3f)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "🏥 المراكز الطبية والصيدليات (${medicalStores.size})",
                        fontSize = 13.5.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )

                    Button(
                        onClick = { showAddDialog = true },
                        colors = ButtonDefaults.buttonColors(containerColor = themeColors.accent),
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp)
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null, tint = Color.Black, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("إضافة منشأة طبية ➕", fontSize = 11.sp, color = Color.Black, fontWeight = FontWeight.Bold)
                    }
                }

                // Search
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("بحث بالاسم، المدينة، الهاتف، التخصص...", fontSize = 11.5.sp, color = Color.Gray) },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = themeColors.accent, modifier = Modifier.size(18.dp)) },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { searchQuery = "" }) {
                                Icon(Icons.Default.Clear, contentDescription = "مسح", tint = Color.Gray, modifier = Modifier.size(16.dp))
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedBorderColor = themeColors.accent,
                        unfocusedBorderColor = Color.White.copy(alpha = 0.15f)
                    )
                )

                // Filters
                val filters = listOf("الكل", "صيدليات", "عيادات ومراكز", "مستشفيات", "محظور")
                LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    items(filters) { f ->
                        val isSelected = selectedFilter == f
                        FilterChip(
                            selected = isSelected,
                            onClick = { selectedFilter = f },
                            label = { Text(f, fontSize = 11.sp, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = themeColors.accent,
                                selectedLabelColor = Color.Black,
                                containerColor = Color.White.copy(alpha = 0.05f),
                                labelColor = Color.White
                            )
                        )
                    }
                }
            }
        }

        // Facilities List
        if (filteredStores.isEmpty()) {
            Card(
                colors = CardDefaults.cardColors(containerColor = themeColors.surface),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Box(modifier = Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                    Text("لا توجد منشآت طبية مسجلة مطابقة 🏥", color = Color.Gray, fontSize = 13.sp)
                }
            }
        } else {
            filteredStores.forEach { store ->
                AdminEntityCard(
                    title = store.name.ifBlank { "منشأة طبية" },
                    subtitle = "📍 ${store.cityId} - ${store.localNeighborhood.ifBlank { "المركز الرئيسي" }} • 📱 ${store.phone}",
                    details = "🏥 ترخيص طبي: ${store.medicalLicenseNo.ifBlank { "معتمد وموثق ✅" }}",
                    statusText = if (store.isBlocked) "🚫 محظور" else if (store.isVip) "⭐ VIP" else "نشط ✅",
                    statusColor = if (store.isBlocked) Color(0xFFEF4444) else if (store.isVip) Color(0xFFF59E0B) else Color(0xFF10B981),
                    isVip = store.isVip,
                    isBlocked = store.isBlocked,
                    themeColors = themeColors,
                    actions = {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            OutlinedButton(
                                onClick = {
                                    viewModel.adminViewModel.toggleStoreBlocked(store.id, !store.isBlocked)
                                    Toast.makeText(context, if (store.isBlocked) "تم إلغاء حظر المنشأة ✅" else "تم حظر المنشأة 🚫", Toast.LENGTH_SHORT).show()
                                },
                                shape = RoundedCornerShape(8.dp),
                                border = BorderStroke(1.dp, if (store.isBlocked) Color(0xFF10B981) else Color(0xFFEF5350)),
                                modifier = Modifier.weight(1f)
                            ) {
                                Text(
                                    if (store.isBlocked) "فك الحظر ✅" else "حظر المنشأة 🚫",
                                    fontSize = 10.5.sp,
                                    color = if (store.isBlocked) Color(0xFF10B981) else Color(0xFFEF5350)
                                )
                            }

                            if (store.phone.isNotBlank()) {
                                IconButton(
                                    onClick = {
                                        try {
                                            val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:${store.phone}"))
                                            context.startActivity(intent)
                                        } catch (e: Exception) {
                                            Toast.makeText(context, "تعذر فتح تطبيق الاتصال", Toast.LENGTH_SHORT).show()
                                        }
                                    },
                                    modifier = Modifier.background(Color(0xFF10B981).copy(alpha = 0.15f), RoundedCornerShape(8.dp))
                                ) {
                                    Icon(Icons.Default.Call, contentDescription = "اتصال", tint = Color(0xFF10B981), modifier = Modifier.size(18.dp))
                                }
                            }

                            IconButton(
                                onClick = {
                                    viewModel.adminViewModel.deleteStore(store.id)
                                    Toast.makeText(context, "🗑️ تم حذف المنشأة الطبية", Toast.LENGTH_SHORT).show()
                                },
                                modifier = Modifier.background(Color(0xFFEF5350).copy(alpha = 0.15f), RoundedCornerShape(8.dp))
                            ) {
                                Icon(Icons.Default.Delete, contentDescription = "حذف", tint = Color(0xFFEF5350), modifier = Modifier.size(18.dp))
                            }
                        }
                    }
                )
                Spacer(modifier = Modifier.height(8.dp))
            }
        }
    }

    // Add Medical Dialog
    if (showAddDialog) {
        var medName by remember { mutableStateOf("") }
        var medPhone by remember { mutableStateOf("") }
        var medCity by remember { mutableStateOf("صنعاء") }
        var medLicense by remember { mutableStateOf("") }
        var medAddress by remember { mutableStateOf("") }

        AlertDialog(
            onDismissRequest = { showAddDialog = false },
            title = { Text("➕ إضافة مركز طبي أو صيدلية", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Color.White) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = medName,
                        onValueChange = { medName = it },
                        label = { Text("اسم المركز / الصيدلية / العيادة") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = medPhone,
                        onValueChange = { medPhone = it },
                        label = { Text("رقم الهاتف والتواصل") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = medCity,
                        onValueChange = { medCity = it },
                        label = { Text("المحافظة / المدينة") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = medAddress,
                        onValueChange = { medAddress = it },
                        label = { Text("الحي والشارع") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = medLicense,
                        onValueChange = { medLicense = it },
                        label = { Text("رقم الترخيص الطبي (اختياري)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (medName.isBlank()) {
                            Toast.makeText(context, "يرجى كتابة اسم المنشأة", Toast.LENGTH_SHORT).show()
                            return@Button
                        }
                        val newStore = StoreEntity(
                            id = "med_${System.currentTimeMillis()}",
                            name = medName.trim(),
                            phone = medPhone.trim(),
                            cityId = medCity.trim(),
                            localNeighborhood = medAddress.trim(),
                            sectionId = "medical",
                            categoryId = "medical",
                            medicalLicenseNo = medLicense.trim(),
                            isApproved = true,
                            isActive = true
                        )
                        viewModel.adminViewModel.saveStore(newStore)
                        showAddDialog = false
                        Toast.makeText(context, "تمت إضافة المنشأة الطبية بنجاح 🎉", Toast.LENGTH_SHORT).show()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = themeColors.accent)
                ) {
                    Text("إضافة وحفظ ➕", color = Color.Black, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddDialog = false }) {
                    Text("إلغاء", color = Color.Gray)
                }
            },
            containerColor = Color(0xFF1E293B)
        )
    }
}
