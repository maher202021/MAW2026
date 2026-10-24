package com.serviseyem.ui.screens.admin
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.lazy.LazyColumn

import androidx.compose.foundation.BorderStroke
import com.serviseyem.ui.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.serviseyem.data.models.AdminPermissionsRegistry
import com.serviseyem.data.models.PermissionCategory
import com.serviseyem.utils.VisualThemePalette

@Composable
fun AdminPermissionsSelectorView(
    themeColors: VisualThemePalette,
    selectedPermissions: List<String>,
    onPermissionsChanged: (List<String>) -> Unit,
    modifier: Modifier = Modifier,
    onSaveRequested: (() -> Unit)? = null
) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedCategoryKey by remember { mutableStateOf("ALL") }

    val allPerms = remember { AdminPermissionsRegistry.allPermissions }
    val categories = remember {
        listOf("ALL" to "الكل") + PermissionCategory.values().map { it.tabKey to it.arabicTitle }
    }

    val filteredPerms = remember(searchQuery, selectedCategoryKey, allPerms) {
        allPerms.filter { item ->
            val matchCat = selectedCategoryKey == "ALL" || item.category.tabKey == selectedCategoryKey
            val matchQuery = searchQuery.isBlank() ||
                item.name.contains(searchQuery, ignoreCase = true) ||
                item.key.contains(searchQuery, ignoreCase = true) ||
                item.description.contains(searchQuery, ignoreCase = true)
            matchCat && matchQuery
        }
    }

    Card(
        colors = CardDefaults.cardColors(containerColor = themeColors.surface),
        border = BorderStroke(1.dp, Color.Gray.copy(alpha = 0.3f)),
        modifier = modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "🔐 مصفوفة الصلاحيات التفصيلية (${selectedPermissions.size}/${allPerms.size})",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    TextButton(onClick = { onPermissionsChanged(allPerms.map { it.key }) }) {
                        Text("تحديد الكل", fontSize = 10.sp, color = themeColors.accent)
                    }
                    TextButton(onClick = { onPermissionsChanged(emptyList()) }) {
                        Text("إلغاء الكل", fontSize = 10.sp, color = Color.Red.copy(alpha = 0.8f))
                    }
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                label = { Text("بحث في الصلاحيات...", fontSize = 11.sp) },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = themeColors.accent) },
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White
                )
            )

            Spacer(modifier = Modifier.height(8.dp))

            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                items(categories) { (catKey, catTitle) ->
                    val isSel = selectedCategoryKey == catKey
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (isSel) themeColors.accent else themeColors.surface)
                            .clickable { selectedCategoryKey = catKey }
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                            .border(1.dp, if (isSel) Color.White else Color.Gray.copy(alpha = 0.3f), RoundedCornerShape(8.dp))
                    ) {
                        Text(
                            text = catTitle,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isSel) Color.Black else Color.White
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 280.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                items(filteredPerms) { perm ->
                    val isChecked = selectedPermissions.contains(perm.key)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(6.dp))
                            .background(if (isChecked) themeColors.accent.copy(alpha = 0.15f) else Color.Transparent)
                            .clickable {
                                if (isChecked) {
                                    onPermissionsChanged(selectedPermissions.filter { it != perm.key })
                                } else {
                                    onPermissionsChanged(selectedPermissions + perm.key)
                                }
                            }
                            .padding(horizontal = 8.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Checkbox(
                            checked = isChecked,
                            onCheckedChange = { checked ->
                                if (checked) {
                                    onPermissionsChanged(selectedPermissions + perm.key)
                                } else {
                                    onPermissionsChanged(selectedPermissions.filter { it != perm.key })
                                }
                            },
                            colors = CheckboxDefaults.colors(
                                checkedColor = themeColors.accent,
                                uncheckedColor = Color.Gray
                            )
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = perm.name.ifBlank { perm.key },
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            if (perm.description.isNotBlank()) {
                                Text(
                                    text = perm.description,
                                    fontSize = 9.sp,
                                    color = themeColors.textSecondary,
                                    maxLines = 1
                                )
                            }
                        }
                    }
                }
            }

            if (onSaveRequested != null) {
                Spacer(modifier = Modifier.height(10.dp))
                Button(
                    onClick = onSaveRequested,
                    colors = ButtonDefaults.buttonColors(containerColor = themeColors.accent),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("حفظ مصفوفة الصلاحيات 💾", color = Color.Black, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
