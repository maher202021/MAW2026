package com.serviseyem.ui

import com.serviseyem.utils.*
import com.serviseyem.ui.*

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.serviseyem.utils.VisualThemePalette
import com.serviseyem.data.*
import java.util.Locale

/**
 * 🗑️ Admin Soft Delete & Recovery Management Panel
 * Solves Problem 5: Standardized soft delete management across all entities with single-click restoration & controlled permanent removal.
 */
@Composable
fun AdminDeletedEntitiesPanel(
    viewModel: MainViewModel,
    themeColors: VisualThemePalette
) {
    val adminRole by viewModel.adminRole.collectAsState()
    if (adminRole != "OWNER" && adminRole != "ADMIN") {
        // UI-only guard, does NOT affect admin privileges or ViewModel logic
        return
    }

    val context = LocalContext.current
    var selectedCategory by remember { mutableStateOf("STORES") } // STORES, PROVIDERS, PROPERTIES, JOBS
    var showPermanentDeleteConfirmDialog by remember { mutableStateOf<Triple<String, String, String>?>(null) } // id, type, name
    var deleteReasonInput by remember { mutableStateOf("") }

    val storesList by viewModel.stores.collectAsState()
    val providersList by viewModel.providers.collectAsState()
    val deletedProvidersList by viewModel.deletedProviders.collectAsState()
    val propertiesList by viewModel.properties.collectAsState()
    val jobsList by viewModel.jobs.collectAsState()

    val deletedStores = remember(storesList) { storesList.filter { it.isDeleted } }
    val allDeletedProviders = remember(providersList, deletedProvidersList) {
        (providersList.filter { it.isDeleted } + deletedProvidersList).distinctBy { it.id }
    }
    val deletedProperties = remember(propertiesList) { propertiesList.filter { it.isDeleted } }
    val deletedJobs = remember(jobsList) { jobsList.filter { it.isDeleted } }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(themeColors.background)
            .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        // Category Selector Tabs
        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            val tabs = listOf(
                Pair("STORES", "المحلات والأنشطة (${deletedStores.size})"),
                Pair("PROVIDERS", "الفنيون والخدمات (${allDeletedProviders.size})"),
                Pair("PROPERTIES", "العقارات والأراضي (${deletedProperties.size})"),
                Pair("JOBS", "إعلانات الوظائف (${deletedJobs.size})")
            )
            items(tabs) { tab ->
                val isSelected = selectedCategory == tab.first
                Button(
                    onClick = { selectedCategory = tab.first },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isSelected) themeColors.accent else themeColors.surface
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        tab.second,
                        fontSize = 11.sp,
                        color = if (isSelected) Color.Black else Color.White,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                    )
                }
            }
        }

        HorizontalDivider(color = themeColors.accent.copy(alpha = 0.3f))

        Text(
            "📋 السجلات المحذوفة ناعماً (يمكن استعادتها بضغطة زر أو حذفها نهائياً):",
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White
        )

        LazyColumn(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            when (selectedCategory) {
                "STORES" -> {
                    if (deletedStores.isEmpty()) {
                        item { EmptyDeletedPlaceholder("لا توجد محلات أو أنشطة محذوفة ناعماً") }
                    } else {
                        items(deletedStores) { store ->
                            DeletedItemRow(
                                title = store.name,
                                subtitle = "الهاتف: ${store.phone} • الحي: ${store.localNeighborhood}",
                                deletedAt = store.deletedAt ?: store.createdAt,
                                onRestore = {
                                    if (adminRole != "OWNER" && adminRole != "ADMIN") return@DeletedItemRow
                                    viewModel.restoreStore(store.id)
                                    Toast.makeText(context, "🔄 تم استعادة المحل (${store.name}) بنجاح!", Toast.LENGTH_SHORT).show()
                                },
                                onPermanentDelete = {
                                    if (adminRole != "OWNER" && adminRole != "ADMIN") return@DeletedItemRow
                                    deleteReasonInput = ""
                                    showPermanentDeleteConfirmDialog = Triple(store.id, "STORE", store.name)
                                }
                            )
                        }
                    }
                }
                "PROVIDERS" -> {
                    if (allDeletedProviders.isEmpty()) {
                        item { EmptyDeletedPlaceholder("لا يوجد فنيون أو مهنيون محذوفون ناعماً") }
                    } else {
                        items(allDeletedProviders) { provider ->
                            DeletedItemRow(
                                title = provider.name,
                                subtitle = "الهاتف: ${provider.phone} • المهنة: ${provider.profession}",
                                deletedAt = provider.deletedAt ?: provider.createdAt,
                                onRestore = {
                                    if (adminRole != "OWNER" && adminRole != "ADMIN") return@DeletedItemRow
                                    viewModel.restoreProvider(provider.id)
                                    Toast.makeText(context, "🔄 تم استعادة حساب الفني (${provider.name}) بنجاح!", Toast.LENGTH_SHORT).show()
                                },
                                onPermanentDelete = {
                                    if (adminRole != "OWNER" && adminRole != "ADMIN") return@DeletedItemRow
                                    deleteReasonInput = ""
                                    showPermanentDeleteConfirmDialog = Triple(provider.id, "PROVIDER", provider.name)
                                }
                            )
                        }
                    }
                }
                "PROPERTIES" -> {
                    if (deletedProperties.isEmpty()) {
                        item { EmptyDeletedPlaceholder("لا توجد عقارات محذوفة ناعماً") }
                    } else {
                        items(deletedProperties) { prop ->
                            DeletedItemRow(
                                title = prop.title,
                                subtitle = "السعر: ${prop.price} YER • المنطقة: ${prop.localNeighborhood}",
                                deletedAt = prop.deletedAt ?: prop.createdAt,
                                onRestore = {
                                    if (adminRole != "OWNER" && adminRole != "ADMIN") return@DeletedItemRow
                                    viewModel.restoreProperty(prop.id)
                                    Toast.makeText(context, "🔄 تم استعادة العقار (${prop.title}) بنجاح!", Toast.LENGTH_SHORT).show()
                                },
                                onPermanentDelete = {
                                    if (adminRole != "OWNER" && adminRole != "ADMIN") return@DeletedItemRow
                                    deleteReasonInput = ""
                                    showPermanentDeleteConfirmDialog = Triple(prop.id, "PROPERTY", prop.title)
                                }
                            )
                        }
                    }
                }
                "JOBS" -> {
                    if (deletedJobs.isEmpty()) {
                        item { EmptyDeletedPlaceholder("لا توجد إعلانات وظائف محذوفة ناعماً") }
                    } else {
                        items(deletedJobs) { job ->
                            DeletedItemRow(
                                title = job.title,
                                subtitle = "الجهة: ${job.companyName} • الراتب: ${job.salary}",
                                deletedAt = job.createdAt,
                                onRestore = {
                                    if (adminRole != "OWNER" && adminRole != "ADMIN") return@DeletedItemRow
                                    viewModel.restoreJob(job.id)
                                    Toast.makeText(context, "🔄 تم استعادة الإعلان الوظيفي (${job.title}) بنجاح!", Toast.LENGTH_SHORT).show()
                                },
                                onPermanentDelete = {
                                    if (adminRole != "OWNER" && adminRole != "ADMIN") return@DeletedItemRow
                                    deleteReasonInput = ""
                                    showPermanentDeleteConfirmDialog = Triple(job.id, "JOB", job.title)
                                }
                            )
                        }
                    }
                }
            }
        }
    }

    // Permanent Delete Modal Dialog
    showPermanentDeleteConfirmDialog?.let { (entityId, entityType, entityName) ->
        AlertDialog(
            onDismissRequest = {
                showPermanentDeleteConfirmDialog = null
                deleteReasonInput = ""
            },
            title = {
                Text("⚠️ تأكيد الحذف النهائي الشامل", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color(0xFFEF4444))
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        "تحذير: سيتم إزالة ($entityName) نهائياً من قاعدة البيانات السحابية والمرفقات بالكامل بدون إمكانية الاستعادة مستقبلاً.",
                        fontSize = 11.sp,
                        color = Color.White
                    )
                    OutlinedTextField(
                        value = deleteReasonInput,
                        onValueChange = { deleteReasonInput = it },
                        label = { Text("سبب الحذف النهائي (مطلوب للتوثيق)", fontSize = 10.sp) },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (adminRole != "OWNER" && adminRole != "ADMIN") return@Button
                        if (deleteReasonInput.isNotBlank()) {
                            val cleanReason = deleteReasonInput.trim()
                            when (entityType) {
                                "STORE" -> viewModel.deleteStorePermanently(entityId)
                                "PROVIDER" -> viewModel.removeProviderPermanently(entityId)
                                "PROPERTY" -> viewModel.deletePropertyPermanently(entityId)
                                "JOB" -> viewModel.deleteJobPermanently(entityId)
                            }
                            viewModel.logAdminActivity("حذف نهائي للسجل ($entityName - $entityType - $entityId) | السبب: $cleanReason")
                            Toast.makeText(context, "🗑️ تم الحذف النهائي للسجل ($entityName)", Toast.LENGTH_SHORT).show()
                            showPermanentDeleteConfirmDialog = null
                            deleteReasonInput = ""
                        } else {
                            Toast.makeText(context, "⚠️ يرجى كتابة سبب الحذف النهائي", Toast.LENGTH_SHORT).show()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF4444))
                ) {
                    Text("تأكيد الحذف النهائي 🛑", fontSize = 11.sp, color = Color.White, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    showPermanentDeleteConfirmDialog = null
                    deleteReasonInput = ""
                }) {
                    Text("إلغاء", fontSize = 11.sp, color = Color.LightGray)
                }
            }
        )
    }
}

@Composable
private fun DeletedItemRow(
    title: String,
    subtitle: String,
    deletedAt: Long,
    onRestore: () -> Unit,
    onPermanentDelete: () -> Unit
) {
    val dateStr = remember(deletedAt) {
        // ✨ م2: استخدام DateFormatter
        com.serviseyem.utils.DateFormatter.formatDisplay(deletedAt)
    }

    Card(
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
        shape = RoundedCornerShape(10.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .padding(12.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(title, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color.White)
                Text(subtitle, fontSize = 10.sp, color = Color.LightGray)
                Text("📅 تاريخ الحذف: $dateStr", fontSize = 9.sp, color = Color(0xFFF59E0B))
            }

            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                Button(
                    onClick = onRestore,
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981)),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Text("استعادة 🔄", fontSize = 10.sp, color = Color.Black, fontWeight = FontWeight.Bold)
                }

                Button(
                    onClick = onPermanentDelete,
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF4444)),
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text("نهائي 🗑️", fontSize = 10.sp, color = Color.White)
                }
            }
        }
    }
}

@Composable
private fun EmptyDeletedPlaceholder(message: String) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(message, fontSize = 12.sp, color = Color.Gray)
    }
}
