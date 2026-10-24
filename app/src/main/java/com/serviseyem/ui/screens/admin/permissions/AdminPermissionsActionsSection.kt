package com.serviseyem.ui.screens.admin.permissions

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.serviseyem.data.models.PermissionCategory
import com.serviseyem.ui.MainViewModel
import com.serviseyem.utils.VisualThemePalette

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun AdminPermissionsActionsSection(
    viewModel: MainViewModel,
    permsViewModel: AdminPermissionsViewModel,
    themeColors: VisualThemePalette,
    selectedRole: String,
    activePermissionsCount: Int,
    searchQuery: String,
    selectedLevelFilter: String?,
    selectedCategoryFilter: String?,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        // Top Title Card
        Card(
            colors = CardDefaults.cardColors(containerColor = themeColors.surface),
            shape = RoundedCornerShape(12.dp),
            border = BorderStroke(1.dp, themeColors.accent.copy(alpha = 0.4f)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Lock,
                            contentDescription = null,
                            tint = themeColors.accent,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(
                                text = "🛡️ مصفوفة الصلاحيات الشاملة (538 صلاحية)",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            Text(
                                text = "38 مجالاً وقطاعاً إدارياً مفصلاً بدون أي اختصار",
                                fontSize = 10.sp,
                                color = themeColors.accent
                            )
                        }
                    }
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color(0xFF059669).copy(alpha = 0.2f))
                            .border(1.dp, Color(0xFF059669), RoundedCornerShape(8.dp))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = "$activePermissionsCount / 538 نشطة",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF34D399)
                        )
                    }
                }
                Text(
                    text = "تحكم دقيق ومفصل في كل إجراء إداري بالنظام: الإشعارات، البنرات، استمارات التسجيل، الحجوزات، المحادثات، الثيمات، الأقسام، الخرائط، المنشآت، الرقابة، والإدارة المالية.",
                    fontSize = 10.sp,
                    color = themeColors.textSecondary,
                    lineHeight = 15.sp
                )
            }
        }

        // Quick Presets and Bulk Actions
        Card(
            colors = CardDefaults.cardColors(containerColor = themeColors.surface),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "⚡ إجراءات جماعية سريعة:",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    ActionChipButton("✅ تفعيل الكل (538)", isPrimary = true) {
                        permsViewModel.enableAllPermissions()
                        Toast.makeText(context, "تم تفعيل جميع الـ 538 صلاحية بنجاح!", Toast.LENGTH_SHORT).show()
                    }
                    ActionChipButton("🚫 تعطيل الكل", isPrimary = false) {
                        permsViewModel.disableAllPermissions()
                        Toast.makeText(context, "تم تعطيل جميع الصلاحيات", Toast.LENGTH_SHORT).show()
                    }
                    ActionChipButton("🟢 الصلاحيات الأساسية فقط", isPrimary = false) {
                        permsViewModel.enableBasicPermissionsOnly()
                        Toast.makeText(context, "تم تفعيل الصلاحيات الأساسية", Toast.LENGTH_SHORT).show()
                    }
                    ActionChipButton("🔵 الأساسية والمتوسطة", isPrimary = false) {
                        permsViewModel.enableBasicAndMediumPermissions()
                        Toast.makeText(context, "تم تفعيل الأساسية والمتوسطة", Toast.LENGTH_SHORT).show()
                    }
                    ActionChipButton("📂 فتح جميع الأقسام", isPrimary = false) {
                        permsViewModel.expandAllCategories()
                    }
                    ActionChipButton("📁 طي جميع الأقسام", isPrimary = false) {
                        permsViewModel.collapseAllCategories()
                    }
                }
            }
        }

        // Search and Filters Bar
        Card(
            colors = CardDefaults.cardColors(containerColor = themeColors.surface),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { permsViewModel.setSearchQuery(it) },
                    label = { Text("بحث في مصفوفة الصلاحيات (الاسم، الوصف، الرمز، المجال)...", fontSize = 10.sp) },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = themeColors.accent, modifier = Modifier.size(18.dp)) },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { permsViewModel.setSearchQuery("") }) {
                                Icon(Icons.Default.Close, contentDescription = "مسح", tint = Color.Gray, modifier = Modifier.size(16.dp))
                            }
                        }
                    },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedBorderColor = themeColors.accent,
                        unfocusedBorderColor = Color.Gray.copy(alpha = 0.4f)
                    )
                )

                // Level Filter
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    val levels = listOf(
                        null to "الكل (538)",
                        "BASIC" to "أساسي 🟢",
                        "MEDIUM" to "متوسط 🔵",
                        "ADVANCED" to "متقدم 🟡",
                        "SENSITIVE" to "حساس 🔴"
                    )
                    levels.forEach { (lvlKey, lvlTitle) ->
                        val isSel = selectedLevelFilter == lvlKey
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(6.dp))
                                .background(if (isSel) themeColors.accent else Color(0xFF0F172A))
                                .clickable { permsViewModel.setLevelFilter(if (isSel) null else lvlKey) }
                                .padding(vertical = 6.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = lvlTitle,
                                fontSize = 9.sp,
                                fontWeight = if (isSel) FontWeight.Bold else FontWeight.Normal,
                                color = if (isSel) Color.Black else Color.White,
                                maxLines = 1,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }

                // Category Quick Filter Scrollable Row
                Text("تصفية سريعة حسب المجال (38 مجالاً):", fontSize = 10.sp, color = themeColors.textSecondary)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    val isAllCatSel = selectedCategoryFilter == null
                    FilterChip(
                        selected = isAllCatSel,
                        onClick = { permsViewModel.setCategoryFilter(null) },
                        label = { Text("🌟 الكل (38)", fontSize = 9.sp) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = themeColors.accent,
                            selectedLabelColor = Color.Black,
                            containerColor = Color(0xFF0F172A),
                            labelColor = Color.White
                        )
                    )
                    PermissionCategory.values().forEach { cat ->
                        val isCatSel = selectedCategoryFilter == cat.name
                        FilterChip(
                            selected = isCatSel,
                            onClick = { permsViewModel.setCategoryFilter(if (isCatSel) null else cat.name) },
                            label = { Text("${cat.iconEmoji} ${cat.arabicTitle} (${cat.expectedCount})", fontSize = 9.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = themeColors.accent,
                                selectedLabelColor = Color.Black,
                                containerColor = Color(0xFF0F172A),
                                labelColor = Color.White
                            )
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun AdminPermissionsSaveBar(
    viewModel: MainViewModel,
    themeColors: VisualThemePalette,
    selectedRole: String,
    activePermissions: Set<String>,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    Card(
        colors = CardDefaults.cardColors(containerColor = themeColors.surface),
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.dp, themeColors.accent),
        modifier = modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "💾 مزامنة وحفظ مصفوفة الصلاحيات",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Text(
                        text = "الدور المحدد: $selectedRole | الصلاحيات الممنوحة: ${activePermissions.size} / 538",
                        fontSize = 10.sp,
                        color = themeColors.accent
                    )
                }
            }
            Button(
                onClick = {
                    viewModel.settingsViewModel.saveCustomPermissionsMatrixToFirestore(activePermissions.toList())
                    viewModel.triggerNotification("✅ تم حفظ ومزامنة مصفوفة الـ 538 صلاحية للدور $selectedRole بنجاح!")
                    Toast.makeText(
                        context,
                        "🚀 تم حفظ وتطبيق ${activePermissions.size} صلاحية للدور $selectedRole بنجاح!",
                        Toast.LENGTH_LONG
                    ).show()
                },
                colors = ButtonDefaults.buttonColors(containerColor = themeColors.accent),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = null,
                    tint = Color.Black,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "حفظ ومزامنة الصلاحيات في Firestore فوراً 💾",
                    color = Color.Black,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
fun ActionChipButton(
    title: String,
    isPrimary: Boolean = false,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(if (isPrimary) Color(0xFF059669) else Color(0xFF0F172A))
            .border(0.5.dp, if (isPrimary) Color(0xFF34D399) else Color.Gray.copy(alpha = 0.4f), RoundedCornerShape(8.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 8.dp, vertical = 6.dp)
    ) {
        Text(
            text = title,
            color = Color.White,
            fontSize = 9.sp,
            fontWeight = if (isPrimary) FontWeight.Bold else FontWeight.Normal,
            maxLines = 1
        )
    }
}
