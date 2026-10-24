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
import com.serviseyem.data.StoreEntity
import com.serviseyem.ui.MainViewModel
import com.serviseyem.ui.screens.admin.components.AdminEntityCard
import com.serviseyem.utils.PermissionGuard
import com.serviseyem.utils.RoleManager
import com.serviseyem.utils.VisualThemePalette

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminRestaurantsPanel(
    viewModel: MainViewModel,
    themeColors: VisualThemePalette,
    modifier: Modifier = Modifier
) {
    val adminRoleStr by viewModel.adminRole.collectAsState()
    val supervisorPermissions by viewModel.authViewModel.currentSupervisorPermissions.collectAsState()
    if (!PermissionGuard.hasPermission(
            role = RoleManager.fromRoleString(adminRoleStr),
            permission = PermissionGuard.PERMISSION_RESTAURANTS,
            supervisorGrantedPermissions = supervisorPermissions
        )
    ) {
        PermissionGuard.UnauthorizedView()
        return
    }

    val context = LocalContext.current
    val stores by viewModel.stores.collectAsState()
    val restaurants = remember(stores) {
        stores.filter {
            it.sectionId == "restaurants" || it.categoryId.contains("rest", true) ||
            it.categoryId.contains("مطعم", true) || it.name.contains("مطعم", true) ||
            it.name.contains("كافيه", true) || it.name.contains("وجب", true) ||
            it.name.contains("شاورما", true) || it.name.contains("مشاوي", true) ||
            it.name.contains("عصير", true) || it.name.contains("حلويات", true)
        }
    }

    var selectedCategory by remember { mutableStateOf("الكل") }
    var searchQuery by remember { mutableStateOf("") }
    var showAddDialog by remember { mutableStateOf(false) }

    val filteredList = remember(restaurants, selectedCategory, searchQuery) {
        restaurants.filter { item ->
            val matchCategory = when (selectedCategory) {
                "مطاعم" -> item.name.contains("مطعم") || item.name.contains("مشاوي") || item.name.contains("وجب")
                "كافيهات" -> item.name.contains("كافيه") || item.name.contains("قهوة") || item.name.contains("عصير")
                "حلويات" -> item.name.contains("حلو") || item.name.contains("مخبز") || item.name.contains("كيك")
                "VIP" -> item.isVip
                "محظور" -> item.isBlocked
                else -> true
            }
            val matchSearch = searchQuery.isBlank() ||
                    item.name.contains(searchQuery, ignoreCase = true) ||
                    item.phone.contains(searchQuery) ||
                    item.cityId.contains(searchQuery, ignoreCase = true)
            matchCategory && matchSearch
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
            shape = RoundedCornerShape(16.dp),
            border = BorderStroke(1.dp, themeColors.accent.copy(alpha = 0.35f)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.ShoppingCart,
                            contentDescription = null,
                            tint = themeColors.accent,
                            modifier = Modifier.size(22.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "🍔 إدارة المطاعم والكافيهات (${restaurants.size})",
                            fontSize = 13.5.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }

                    Button(
                        onClick = { showAddDialog = true },
                        colors = ButtonDefaults.buttonColors(containerColor = themeColors.accent),
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp)
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null, tint = Color.Black, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("إضافة مطعم ➕", fontSize = 11.sp, color = Color.Black, fontWeight = FontWeight.Bold)
                    }
                }

                // Search
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("بحث باسم المطعم، الكافيه، المدينة، الهاتف...", fontSize = 11.5.sp, color = Color.Gray) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = themeColors.accent, modifier = Modifier.size(18.dp)) },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { searchQuery = "" }) {
                                Icon(Icons.Default.Clear, contentDescription = "مسح", tint = Color.Gray, modifier = Modifier.size(16.dp))
                            }
                        }
                    },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedBorderColor = themeColors.accent,
                        unfocusedBorderColor = Color.White.copy(alpha = 0.15f)
                    )
                )

                // Category Chips
                val categories = listOf("الكل", "مطاعم", "كافيهات", "حلويات", "VIP", "محظور")
                LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    items(categories) { cat ->
                        val isSelected = selectedCategory == cat
                        FilterChip(
                            selected = isSelected,
                            onClick = { selectedCategory = cat },
                            label = { Text(cat, fontSize = 11.sp, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal) },
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

        // List
        if (filteredList.isEmpty()) {
            Card(
                colors = CardDefaults.cardColors(containerColor = themeColors.surface),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Box(modifier = Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                    Text("لا توجد مطاعم أو كافيهات مطابقة 🍔", color = Color.Gray, fontSize = 13.sp)
                }
            }
        } else {
            filteredList.forEach { item ->
                AdminEntityCard(
                    title = item.name.ifBlank { "مطعم / كافيه" },
                    subtitle = "📍 ${item.cityId} - ${item.localNeighborhood.ifBlank { "الرئيسي" }} • 📱 ${item.phone}",
                    details = "🍽️ النشاط: ${item.description.ifBlank { "مأكولات ومشروبات" }}",
                    statusText = if (item.isBlocked) "🚫 محظور" else if (item.isVip) "⭐ VIP" else if (item.isActive) "نشط ✅" else "غير نشط ⏸️",
                    statusColor = if (item.isBlocked) Color(0xFFEF4444) else if (item.isVip) Color(0xFFF59E0B) else if (item.isActive) Color(0xFF10B981) else Color.Gray,
                    isVip = item.isVip,
                    isBlocked = item.isBlocked,
                    themeColors = themeColors,
                    actions = {
                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                                    Checkbox(
                                        checked = item.isVip,
                                        onCheckedChange = {
                                            viewModel.adminViewModel.setStoreVip(item.id, it)
                                            Toast.makeText(context, if (it) "تم تفعيل شارة VIP ⭐" else "تم إلغاء VIP", Toast.LENGTH_SHORT).show()
                                        },
                                        colors = CheckboxDefaults.colors(checkedColor = Color(0xFFF59E0B)),
                                        modifier = Modifier.size(24.dp)
                                    )
                                    Spacer(modifier = Modifier.width(2.dp))
                                    Text("VIP ⭐", fontSize = 10.sp, color = Color.White)
                                }

                                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                                    Checkbox(
                                        checked = item.isVerified,
                                        onCheckedChange = {
                                            viewModel.adminViewModel.setStoreVerified(item.id, it)
                                            Toast.makeText(context, if (it) "تم توثيق المطعم ✅" else "تم إلغاء التوثيق", Toast.LENGTH_SHORT).show()
                                        },
                                        colors = CheckboxDefaults.colors(checkedColor = Color(0xFF3B82F6)),
                                        modifier = Modifier.size(24.dp)
                                    )
                                    Spacer(modifier = Modifier.width(2.dp))
                                    Text("موثق 🛡️", fontSize = 10.sp, color = Color.White)
                                }

                                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                                    Checkbox(
                                        checked = item.isRecommended,
                                        onCheckedChange = {
                                            viewModel.adminViewModel.setStoreRecommended(item.id, it)
                                        },
                                        colors = CheckboxDefaults.colors(checkedColor = Color(0xFFEC4899)),
                                        modifier = Modifier.size(24.dp)
                                    )
                                    Spacer(modifier = Modifier.width(2.dp))
                                    Text("موصى به 🔥", fontSize = 10.sp, color = Color.White)
                                }
                            }

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                OutlinedButton(
                                    onClick = {
                                        viewModel.adminViewModel.setStoreBlocked(item.id, !item.isBlocked, "حظر إداري")
                                        Toast.makeText(context, if (item.isBlocked) "تم فك الحظر عن المطعم ✅" else "تم حظر المطعم 🚫", Toast.LENGTH_SHORT).show()
                                    },
                                    shape = RoundedCornerShape(8.dp),
                                    border = BorderStroke(1.dp, if (item.isBlocked) Color(0xFF10B981) else Color(0xFFEF4444)),
                                    modifier = Modifier.weight(1f),
                                    contentPadding = PaddingValues(vertical = 4.dp)
                                ) {
                                    Text(
                                        if (item.isBlocked) "فك الحظر ✅" else "حظر 🚫",
                                        fontSize = 10.5.sp,
                                        color = if (item.isBlocked) Color(0xFF10B981) else Color(0xFFEF4444)
                                    )
                                }

                                if (item.phone.isNotBlank()) {
                                    IconButton(
                                        onClick = {
                                            try {
                                                val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:${item.phone}"))
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
                                        viewModel.adminViewModel.deleteStore(item.id)
                                        Toast.makeText(context, "🗑️ تم حذف المطعم", Toast.LENGTH_SHORT).show()
                                    },
                                    modifier = Modifier.background(Color(0xFFEF5350).copy(alpha = 0.15f), RoundedCornerShape(8.dp))
                                ) {
                                    Icon(Icons.Default.Delete, contentDescription = "Delete", tint = Color(0xFFEF5350), modifier = Modifier.size(18.dp))
                                }
                            }
                        }
                    }
                )
                Spacer(modifier = Modifier.height(8.dp))
            }
        }
    }

    // Add Restaurant Dialog
    if (showAddDialog) {
        var restName by remember { mutableStateOf("") }
        var restPhone by remember { mutableStateOf("") }
        var restCity by remember { mutableStateOf("صنعاء") }
        var restCategory by remember { mutableStateOf("مطعم ومأكولات") }
        var restNeighborhood by remember { mutableStateOf("") }

        AlertDialog(
            onDismissRequest = { showAddDialog = false },
            title = { Text("➕ إضافة مطعم أو كافيه جديد", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Color.White) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = restName,
                        onValueChange = { restName = it },
                        label = { Text("اسم المطعم أو الكافيه") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = restPhone,
                        onValueChange = { restPhone = it },
                        label = { Text("رقم الهاتف والطلبات") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = restCity,
                        onValueChange = { restCity = it },
                        label = { Text("المدينة / المحافظة") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = restNeighborhood,
                        onValueChange = { restNeighborhood = it },
                        label = { Text("الحي والشارع") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = restCategory,
                        onValueChange = { restCategory = it },
                        label = { Text("التصنيف (مطعم، كافيه، وجبات، حلويات)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (restName.isBlank()) {
                            Toast.makeText(context, "يرجى كتابة اسم المطعم", Toast.LENGTH_SHORT).show()
                            return@Button
                        }
                        val newStore = StoreEntity(
                            id = "rest_${System.currentTimeMillis()}",
                            name = restName.trim(),
                            phone = restPhone.trim(),
                            cityId = restCity.trim(),
                            localNeighborhood = restNeighborhood.trim(),
                            sectionId = "restaurants",
                            categoryId = "restaurants",
                            description = restCategory.trim(),
                            isApproved = true,
                            isActive = true
                        )
                        viewModel.adminViewModel.saveStore(newStore)
                        showAddDialog = false
                        Toast.makeText(context, "تمت إضافة المطعم بنجاح 🍔", Toast.LENGTH_SHORT).show()
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
