package com.serviseyem.ui.screens.dashboard.components

import androidx.compose.foundation.background
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
import com.serviseyem.data.repositories.LoyaltyProgram
import java.util.UUID

/**
 * 🎁 LoyaltyManager (إدارة برامج الولاء ونقاط المكافآت)
 * إنشاء برامج الولاء، تحديد النقاط المطلوبة، ومكافأة العملاء المتكررين.
 */
@Composable
fun LoyaltyManager(
    ownerId: String = "",
    viewModel: com.serviseyem.ui.screens.dashboard.viewmodels.DashboardExtensionsViewModel = androidx.lifecycle.viewmodel.compose.viewModel(factory = object : androidx.lifecycle.ViewModelProvider.Factory { override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T { return com.serviseyem.ui.screens.dashboard.viewmodels.DashboardExtensionsViewModel(ownerId) as T } }),
    themeColors: VisualThemePalette,
    modifier: Modifier = Modifier
) {
    val programs by viewModel.loyaltyPrograms.collectAsState()

    var showAddDialog by remember { mutableStateOf(false) }
    var programName by remember { mutableStateOf("") }
    var pointsReq by remember { mutableStateOf("100") }
    var rewardDesc by remember { mutableStateOf("") }
    var discountVal by remember { mutableStateOf("50") }

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
                    Icon(Icons.Default.Star, contentDescription = null, tint = Color(0xFFEAB308))
                    Text(
                        text = "برامج الولاء ونقاط المكافآت",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }

                Button(
                    onClick = { showAddDialog = true },
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEAB308), contentColor = Color(0xFF0F172A)),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("برنامج جديد", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }

            programs.forEach { program ->
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
                                    text = program.name,
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = Color(0xFFEAB308).copy(alpha = 0.2f)
                                ) {
                                    Text(
                                        text = "${program.pointsRequired} نقطة",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFFEAB308),
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                            }
                            Text(
                                text = "المكافأة: ${program.rewardDescription} • خصم ${program.discountValue.toInt()} ريال",
                                fontSize = 11.sp,
                                color = Color(0xFF94A3B8)
                            )
                        }

                        Row {
                            IconButton(onClick = {
                                // ✨ م2-ج2: استخدام ViewModel بدلاً من Firestore المباشر
                                viewModel.updateLoyaltyProgramStatus(program.id, !program.isEnabled)
                            }) {
                                Icon(
                                    if (program.isEnabled) Icons.Default.CheckCircle else Icons.Default.Close,
                                    contentDescription = null,
                                    tint = if (program.isEnabled) Color(0xFF10B981) else Color(0xFF94A3B8)
                                )
                            }
                            IconButton(onClick = {
                                // ✨ م2-ج2: استخدام ViewModel
                                viewModel.deleteLoyaltyProgram(program.id)
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
            title = { Text("إنشاء برنامج ولاء جديد") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = programName,
                        onValueChange = { programName = it },
                        label = { Text("اسم البرنامج (مثال: عملاء VIP)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = pointsReq,
                        onValueChange = { pointsReq = it },
                        label = { Text("النقاط المطلوبة للاستبدال") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = rewardDesc,
                        onValueChange = { rewardDesc = it },
                        label = { Text("وصف المكافأة أو الميزة") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = discountVal,
                        onValueChange = { discountVal = it },
                        label = { Text("قيمة الخصم التقديرية (ريال)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (programName.isNotBlank()) {
                            val progId = UUID.randomUUID().toString()
                            val newProg = LoyaltyProgram(
                                id = progId,
                                ownerId = ownerId,
                                name = programName.trim(),
                                pointsRequired = pointsReq.toIntOrNull() ?: 100,
                                rewardDescription = rewardDesc.trim(),
                                discountValue = discountVal.toDoubleOrNull() ?: 50.0,
                                isEnabled = true
                            )
                            // ✨ م2-ج2: استخدام ViewModel
                            viewModel.addLoyaltyProgram(newProg)

                            showAddDialog = false
                            programName = ""
                            rewardDesc = ""
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEAB308), contentColor = Color(0xFF0F172A))
                ) {
                    Text("إنشاء البرنامج")
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
