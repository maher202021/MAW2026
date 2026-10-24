package com.serviseyem.ui.screens.admin.permissions

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.serviseyem.data.models.AdminPermissionsRegistry
import com.serviseyem.data.models.PermissionCategory
import com.serviseyem.data.models.PermissionLevel
import com.serviseyem.utils.VisualThemePalette

@Composable
fun AdminPermissionsMatrixListSection(
    permsViewModel: AdminPermissionsViewModel,
    themeColors: VisualThemePalette,
    activePermissions: Set<String>,
    expandedCategories: Set<PermissionCategory>,
    searchQuery: String,
    selectedLevelFilter: String?,
    selectedCategoryFilter: String?,
    modifier: Modifier = Modifier
) {
    val filteredCategories = PermissionCategory.values().filter { cat ->
        selectedCategoryFilter == null || selectedCategoryFilter == cat.name
    }

    var totalDisplayedPermsCount = 0

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        filteredCategories.forEach { category ->
            var categoryPerms = AdminPermissionsRegistry.getByCategory(category)
            if (selectedLevelFilter != null) {
                categoryPerms = categoryPerms.filter { it.level.name == selectedLevelFilter }
            }
            if (searchQuery.isNotBlank()) {
                val q = searchQuery.trim().lowercase()
                categoryPerms = categoryPerms.filter {
                    it.name.lowercase().contains(q) ||
                    it.key.lowercase().contains(q) ||
                    it.description.lowercase().contains(q) ||
                    it.targetGroup.lowercase().contains(q)
                }
            }

            if (categoryPerms.isNotEmpty()) {
                totalDisplayedPermsCount += categoryPerms.size
                val isExpanded = expandedCategories.contains(category) || searchQuery.isNotBlank()
                val catKeys = categoryPerms.map { it.key }
                val activeInCat = catKeys.count { activePermissions.contains(it) }
                val isAllSelectedInCat = activeInCat == catKeys.size && catKeys.isNotEmpty()

                Card(
                    colors = CardDefaults.cardColors(containerColor = themeColors.surface),
                    shape = RoundedCornerShape(10.dp),
                    border = BorderStroke(
                        1.dp,
                        if (activeInCat > 0) themeColors.accent.copy(alpha = 0.5f) else Color.DarkGray.copy(alpha = 0.4f)
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(10.dp)) {
                        // Category Header Bar
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    permsViewModel.toggleCategoryExpansion(category)
                                }
                                .padding(vertical = 4.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.weight(1f, fill = false)
                            ) {
                                Text(category.iconEmoji, fontSize = 16.sp)
                                Spacer(modifier = Modifier.width(8.dp))
                                Column {
                                    Text(
                                        text = "${category.arabicTitle} ($activeInCat / ${category.expectedCount})",
                                        color = Color.White,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Text(
                                        text = "الرمز الفني: ${category.name}",
                                        color = themeColors.textSecondary,
                                        fontSize = 8.sp
                                    )
                                }
                            }

                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                // Toggle all in this category
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(if (isAllSelectedInCat) themeColors.accent else Color(0xFF0F172A))
                                        .border(0.5.dp, if (isAllSelectedInCat) Color.White else Color.Gray, RoundedCornerShape(6.dp))
                                        .clickable {
                                            permsViewModel.toggleCategoryPermissions(category, !isAllSelectedInCat)
                                        }
                                        .padding(horizontal = 8.dp, vertical = 4.dp)
                                ) {
                                    Text(
                                        text = if (isAllSelectedInCat) "إلغاء القسم" else "تحديد القسم (${categoryPerms.size})",
                                        color = if (isAllSelectedInCat) Color.Black else Color.White,
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }

                                Icon(
                                    imageVector = if (isExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                                    contentDescription = null,
                                    tint = themeColors.accent,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }

                        // Expanded Permission Items List
                        AnimatedVisibility(visible = isExpanded) {
                            Column(
                                modifier = Modifier.padding(top = 8.dp),
                                verticalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                categoryPerms.forEach { perm ->
                                    val isPermActive = activePermissions.contains(perm.key)
                                    val levelBadgeColor = when (perm.level) {
                                        PermissionLevel.BASIC -> Color(0xFF10B981)
                                        PermissionLevel.MEDIUM -> Color(0xFF3B82F6)
                                        PermissionLevel.ADVANCED -> Color(0xFFF59E0B)
                                        PermissionLevel.SENSITIVE -> Color(0xFFEF4444)
                                    }

                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(
                                                if (isPermActive) themeColors.accent.copy(alpha = 0.08f)
                                                else Color(0xFF0B1320).copy(alpha = 0.5f)
                                            )
                                            .border(
                                                0.5.dp,
                                                if (isPermActive) themeColors.accent.copy(alpha = 0.3f)
                                                else Color.DarkGray.copy(alpha = 0.2f),
                                                RoundedCornerShape(8.dp)
                                            )
                                            .clickable {
                                                permsViewModel.togglePermission(perm.key, !isPermActive)
                                            }
                                            .padding(horizontal = 10.dp, vertical = 8.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column(modifier = Modifier.weight(1f)) {
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                                            ) {
                                                Box(
                                                    modifier = Modifier
                                                        .clip(RoundedCornerShape(4.dp))
                                                        .background(levelBadgeColor.copy(alpha = 0.15f))
                                                        .border(0.5.dp, levelBadgeColor, RoundedCornerShape(4.dp))
                                                        .padding(horizontal = 4.dp, vertical = 1.dp)
                                                ) {
                                                    Text(
                                                        text = perm.level.name,
                                                        color = levelBadgeColor,
                                                        fontSize = 7.sp,
                                                        fontWeight = FontWeight.Bold
                                                    )
                                                }
                                                Text(
                                                    text = perm.name,
                                                    fontSize = 11.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = if (isPermActive) Color.White else Color.Gray
                                                )
                                            }

                                            if (perm.description.isNotBlank()) {
                                                Spacer(modifier = Modifier.height(2.dp))
                                                Text(
                                                    text = perm.description,
                                                    fontSize = 9.sp,
                                                    color = themeColors.textSecondary,
                                                    lineHeight = 13.sp
                                                )
                                            }

                                            Spacer(modifier = Modifier.height(2.dp))
                                            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                                Text(
                                                    text = "🔑 ${perm.key}",
                                                    color = themeColors.accent.copy(alpha = 0.8f),
                                                    fontSize = 8.sp,
                                                    fontWeight = FontWeight.SemiBold
                                                )
                                                Text(
                                                    text = "• النطاق: ${perm.targetGroup}",
                                                    color = Color.Gray,
                                                    fontSize = 8.sp
                                                )
                                            }
                                        }

                                        Switch(
                                            checked = isPermActive,
                                            onCheckedChange = { checked ->
                                                permsViewModel.togglePermission(perm.key, checked)
                                            },
                                            colors = SwitchDefaults.colors(
                                                checkedThumbColor = Color.Black,
                                                checkedTrackColor = themeColors.accent,
                                                uncheckedThumbColor = Color.LightGray,
                                                uncheckedTrackColor = Color.DarkGray
                                            ),
                                            modifier = Modifier.padding(start = 6.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        if (totalDisplayedPermsCount == 0) {
            Card(
                colors = CardDefaults.cardColors(containerColor = themeColors.surface),
                modifier = Modifier.fillMaxWidth()
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "🔍 لم يتم العثور على صلاحيات مطابقة لمعايير البحث.",
                        color = Color.LightGray,
                        fontSize = 11.sp
                    )
                }
            }
        }
    }
}
