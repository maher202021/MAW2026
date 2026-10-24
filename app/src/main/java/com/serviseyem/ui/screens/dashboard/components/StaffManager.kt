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
import com.serviseyem.data.repositories.StaffMember
import com.serviseyem.ui.screens.dashboard.viewmodels.DashboardExtensionsViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModel
import java.util.UUID

/**
 * 👥 StaffManager (إدارة الموظفين وفريق العمل والصلاحيات)
 * إضافة أعضاء الفريق، تعيين الأدوار والصلاحيات (تعديل الأسعار، التواصل مع العملاء، إدارة الحجوزات).
 */
@Composable
fun StaffManager(
    ownerId: String = "",
    viewModel: DashboardExtensionsViewModel = viewModel(factory = object : ViewModelProvider.Factory { override fun <T : ViewModel> create(modelClass: Class<T>): T { return DashboardExtensionsViewModel(ownerId) as T } }),
    themeColors: VisualThemePalette,
    modifier: Modifier = Modifier
) {
    val staffList by viewModel.staff.collectAsState()

    var showAddDialog by remember { mutableStateOf(false) }
    var newName by remember { mutableStateOf("") }
    var newPhone by remember { mutableStateOf("") }
    var newRole by remember { mutableStateOf("فني ميداني") }

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
                    Icon(Icons.Default.Person, contentDescription = null, tint = Color(0xFF00E5FF))
                    Text(
                        text = "إدارة فريق العمل والصلاحيات",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }

                Button(
                    onClick = { showAddDialog = true },
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00E5FF), contentColor = Color(0xFF0F172A)),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("إضافة موظف", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }

            staffList.forEach { member ->
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
                                    text = member.name,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = Color(0xFF00E5FF).copy(alpha = 0.2f)
                                ) {
                                    Text(
                                        text = member.role,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF00E5FF),
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                            }
                            Text(
                                text = "الهاتف: ${member.phone} • صلاحيات: ${if (member.canEditPrices) "تعديل الأسعار + " else ""}المحادثات",
                                fontSize = 11.sp,
                                color = Color(0xFF94A3B8)
                            )
                        }

                        IconButton(onClick = {
                            // ✨ م2-ج2: استخدام ViewModel بدلاً من Firestore المباشر
                            viewModel.deleteStaff(member.id)
                        }) {
                            Icon(Icons.Default.Delete, contentDescription = "Delete", tint = Color(0xFFEF4444))
                        }
                    }
                }
            }
        }
    }

    if (showAddDialog) {
        AlertDialog(
            onDismissRequest = { showAddDialog = false },
            title = { Text("إضافة موظف / فني جديد") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = newName,
                        onValueChange = { newName = it },
                        label = { Text("الاسم الكامل للموظف") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = newPhone,
                        onValueChange = { newPhone = it },
                        label = { Text("رقم الهاتف") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = newRole,
                        onValueChange = { newRole = it },
                        label = { Text("الدور الوظيفي (مثال: فني ميداني، كاشير)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (newName.isNotBlank() && newPhone.isNotBlank()) {
                            val memberId = UUID.randomUUID().toString()
                            val newMember = StaffMember(
                                id = memberId,
                                ownerId = ownerId,
                                name = newName.trim(),
                                role = newRole.trim(),
                                phone = newPhone.trim(),
                                canEditPrices = false,
                                canChat = true
                            )
                            // ✨ م2-ج2: استخدام ViewModel
                            viewModel.addStaff(newMember)

                            showAddDialog = false
                            newName = ""
                            newPhone = ""
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00E5FF), contentColor = Color(0xFF0F172A))
                ) {
                    Text("إضافة الفريق")
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
