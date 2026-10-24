package com.serviseyem.ui.screens.home.sections

import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.serviseyem.data.StoreEntity
import com.serviseyem.domain.isAdmin
import com.serviseyem.ui.MainViewModel
import com.serviseyem.ui.screens.home.extensions.isMedicalCenter
import com.serviseyem.utils.VisualThemePalette
import com.serviseyem.utils.formatArabicWorkingHours

/**
 * 🏥 MedicalCentersSectionView - عرض المراكز والعيادات والمستشفيات الطبية بتصميم عصري فخم وخفيف
 */
@Composable
fun MedicalCentersSectionView(
    viewModel: MainViewModel,
    themeColors: VisualThemePalette,
    onStoreClick: (StoreEntity) -> Unit,
    onCreateMedicalClick: () -> Unit
) {
    val context = LocalContext.current
    val allStores by viewModel.stores.collectAsState()
    val currentUserId by viewModel.currentUserId.collectAsState()
    val adminRole by viewModel.adminRole.collectAsState()
    val isAdminUser = adminRole.isAdmin()

    val medicalList = remember(allStores, currentUserId, adminRole) {
        allStores.filter { 
            it.isMedicalCenter() && !it.isDeleted && (it.isApproved || it.ownerId == currentUserId || isAdminUser)
        }
    }
    var selectedSubCategory by remember { mutableStateOf("الكل") }

    val subCategories = listOf(
        "الكل",
        "مستشفيات عامة",
        "عيادات تخصصية",
        "طب وجراحة الأسنان",
        "صيدليات ومستلزمات",
        "مختبرات تحاليل",
        "مراكز بصريات"
    )

    val filteredList = remember(medicalList, selectedSubCategory) {
        if (selectedSubCategory == "الكل") medicalList
        else {
            val key = selectedSubCategory.replace("مستشفيات", "مستشفى")
                .replace("عيادات", "عيادة")
                .replace("صيدليات", "صيدلية")
                .replace("مختبرات", "مختبر")
            medicalList.filter { store ->
                store.name.contains(key, ignoreCase = true) || 
                store.description.contains(key, ignoreCase = true) || 
                store.categoryId.contains(key, ignoreCase = true)
            }
        }
    }

    Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        // Section Header
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f).padding(end = 8.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Icon(Icons.Default.LocalHospital, contentDescription = null, tint = Color(0xFF0D5C46), modifier = Modifier.size(18.dp))
                    Text("المراكز والمجمعات الطبية", fontSize = 14.5.sp, fontWeight = FontWeight.Bold, color = Color(0xFF0F172A))
                }
                Text("تصفح المستشفيات والعيادات والمختبرات المعتمدة باليمن", fontSize = 10.5.sp, color = Color(0xFF64748B), maxLines = 1)
            }
            Button(
                onClick = onCreateMedicalClick,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0D5C46)),
                shape = RoundedCornerShape(10.dp),
                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                modifier = Modifier.defaultMinSize(minHeight = 34.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = null, tint = Color.White, modifier = Modifier.size(15.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("إضافة مركز", fontSize = 11.sp, color = Color.White, fontWeight = FontWeight.Bold, maxLines = 1)
            }
        }

        // Subcategories Chips
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .padding(vertical = 2.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            subCategories.forEach { subCat ->
                val isSelected = selectedSubCategory == subCat
                Surface(
                    onClick = { selectedSubCategory = subCat },
                    shape = RoundedCornerShape(16.dp),
                    color = if (isSelected) Color(0xFF0D5C46) else Color.White,
                    shadowElevation = if (isSelected) 3.dp else 1.dp,
                    border = BorderStroke(1.dp, if (isSelected) Color(0xFFD4AF37) else Color(0xFFE2E8F0))
                ) {
                    Text(
                        subCat,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isSelected) Color.White else Color(0xFF1E293B),
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                    )
                }
            }
        }

        if (filteredList.isEmpty()) {
            Card(
                colors = CardDefaults.cardColors(containerColor = Color.White),
                border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier.fillMaxWidth().padding(12.dp)
            ) {
                Text(
                    "لا توجد مراكز طبية أو عيادات مسجلة في هذا القسم حالياً.",
                    fontSize = 11.5.sp,
                    color = Color(0xFF64748B),
                    modifier = Modifier.padding(16.dp),
                    textAlign = TextAlign.Center
                )
            }
        } else {
            filteredList.forEach { store ->
                Card(
                    onClick = { onStoreClick(store) },
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    shape = RoundedCornerShape(16.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.5.dp),
                    border = BorderStroke(1.dp, if (store.isVip) Color(0xFFD4AF37).copy(alpha = 0.5f) else Color(0xFFE2E8F0)),
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 2.dp, vertical = 4.dp)
                ) {
                    Column(modifier = Modifier.fillMaxWidth().padding(12.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = Color(0xFFF1F5F9),
                                border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
                                modifier = Modifier.size(56.dp)
                            ) {
                                val logoUrl = store.logoImage.ifBlank { store.coverImage }
                                if (logoUrl.isNotBlank()) {
                                    coil.compose.AsyncImage(
                                        model = logoUrl,
                                        contentDescription = store.name,
                                        contentScale = androidx.compose.ui.layout.ContentScale.Crop,
                                        modifier = Modifier.fillMaxSize()
                                    )
                                } else {
                                    Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                                        Icon(Icons.Default.LocalHospital, contentDescription = null, tint = Color(0xFF0D5C46), modifier = Modifier.size(28.dp))
                                    }
                                }
                            }

                            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                                        modifier = Modifier.weight(1f, fill = false)
                                    ) {
                                        Text(
                                            text = store.name,
                                            fontSize = 14.5.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color(0xFF0F172A),
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                        if (store.isVerified) {
                                            Icon(Icons.Default.Verified, contentDescription = "معتمد", tint = Color(0xFF0D5C46), modifier = Modifier.size(14.dp))
                                        }
                                    }

                                    Surface(
                                        shape = RoundedCornerShape(6.dp),
                                        color = Color(0xFFFEF3C7),
                                        border = BorderStroke(0.5.dp, Color(0xFFF59E0B))
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(2.dp)
                                        ) {
                                            Icon(Icons.Default.Star, contentDescription = null, tint = Color(0xFFD97706), modifier = Modifier.size(11.dp))
                                            Text(String.format("%.1f", store.rating), fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color(0xFF92400E))
                                        }
                                    }
                                }

                                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                    Icon(Icons.Default.LocationOn, contentDescription = null, tint = Color(0xFF64748B), modifier = Modifier.size(12.dp))
                                    Text(
                                        "${store.cityId} - ${store.localNeighborhood.ifBlank { "اليمن" }}",
                                        fontSize = 11.sp,
                                        color = Color(0xFF475569),
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }

                                if (store.workingHours.isNotEmpty()) {
                                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                        Icon(Icons.Default.Schedule, contentDescription = null, tint = Color(0xFF10B981), modifier = Modifier.size(12.dp))
                                        Text(
                                            formatArabicWorkingHours(store.workingHours),
                                            fontSize = 10.sp,
                                            color = Color(0xFF059669),
                                            fontWeight = FontWeight.Medium
                                        )
                                    }
                                }
                            }
                        }

                        // Instant Action Buttons: [اتصال بالمركز] [حجز / استشارة]
                        Spacer(modifier = Modifier.height(10.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            FilledTonalButton(
                                onClick = {
                                    try {
                                        val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:${store.phone}"))
                                        context.startActivity(intent)
                                    } catch (e: Exception) {
                                        Toast.makeText(context, "هاتف المركز: ${store.phone}", Toast.LENGTH_SHORT).show()
                                    }
                                },
                                colors = ButtonDefaults.filledTonalButtonColors(
                                    containerColor = Color(0xFFDCFCE7),
                                    contentColor = Color(0xFF065F46)
                                ),
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.weight(1f).height(34.dp),
                                contentPadding = PaddingValues(horizontal = 4.dp, vertical = 0.dp)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(3.dp)
                                ) {
                                    Icon(Icons.Default.Phone, contentDescription = "اتصال", tint = Color(0xFF065F46), modifier = Modifier.size(13.dp))
                                    Text("اتصال", fontSize = 11.sp, fontWeight = FontWeight.Bold, maxLines = 1, softWrap = false)
                                }
                            }

                            Button(
                                onClick = { onStoreClick(store) },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0D5C46)),
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.weight(1.3f).height(34.dp),
                                contentPadding = PaddingValues(horizontal = 4.dp, vertical = 0.dp)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(3.dp)
                                ) {
                                    Icon(Icons.Default.LocalHospital, contentDescription = "العيادات", tint = Color.White, modifier = Modifier.size(13.dp))
                                    Text("العيادات والخدمات", fontSize = 11.sp, color = Color.White, fontWeight = FontWeight.Bold, maxLines = 1, softWrap = false)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
