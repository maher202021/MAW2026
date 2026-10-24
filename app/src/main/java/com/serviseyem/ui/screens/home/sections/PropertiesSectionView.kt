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
import com.serviseyem.data.PropertyEntity
import com.serviseyem.domain.isAdmin
import com.serviseyem.ui.MainViewModel
import com.serviseyem.utils.VisualThemePalette
import com.serviseyem.utils.formatPriceWithCurrency

/**
 * 🏢 PropertiesSectionView - عرض العقارات والاستثمارات بتصميم عصري فخم وخفيف
 */
@Composable
fun PropertiesSectionView(
    viewModel: MainViewModel,
    themeColors: VisualThemePalette,
    onPropertyClick: (PropertyEntity) -> Unit,
    onCreatePropertyClick: () -> Unit
) {
    val context = LocalContext.current
    val allProperties by viewModel.properties.collectAsState()
    val currentUserId by viewModel.currentUserId.collectAsState()
    val adminRole by viewModel.adminRole.collectAsState()
    val isAdminUser = adminRole.isAdmin()

    val propertiesList = remember(allProperties, currentUserId, adminRole) {
        allProperties.filter { 
            !it.isDeleted && (it.isApproved || it.ownerId == currentUserId || isAdminUser)
        }
    }
    var selectedSubCategory by remember { mutableStateOf("الكل") }

    val subCategories = listOf(
        "الكل",
        "شقق للإيجار",
        "شقق مفروشة",
        "فلل وقصور",
        "أراضي ومخططات",
        "محلات تجارية",
        "عمارات واستثمارات"
    )

    val filteredList = remember(propertiesList, selectedSubCategory) {
        if (selectedSubCategory == "الكل") propertiesList
        else {
            val key = selectedSubCategory.replace("شقق للإيجار", "إيجار")
                .replace("شقق مفروشة", "مفروشة")
                .replace("فلل وقصور", "فيلا")
                .replace("أراضي ومخططات", "أرض")
            propertiesList.filter { prop ->
                prop.title.contains(key, ignoreCase = true) || 
                prop.description.contains(key, ignoreCase = true) || 
                prop.propertyType.contains(key, ignoreCase = true)
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
                    Icon(Icons.Default.Apartment, contentDescription = null, tint = Color(0xFF0D5C46), modifier = Modifier.size(18.dp))
                    Text("العقارات والاستثمارات", fontSize = 14.5.sp, fontWeight = FontWeight.Bold, color = Color(0xFF0F172A))
                }
                Text("تصفح الشقق المفروشة والفلل والأراضي المعروضة باليمن", fontSize = 10.5.sp, color = Color(0xFF64748B), maxLines = 1)
            }
            Button(
                onClick = onCreatePropertyClick,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0D5C46)),
                shape = RoundedCornerShape(10.dp),
                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                modifier = Modifier.defaultMinSize(minHeight = 34.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = null, tint = Color.White, modifier = Modifier.size(15.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("إضافة عقار", fontSize = 11.sp, color = Color.White, fontWeight = FontWeight.Bold, maxLines = 1)
            }
        }

        // Subcategories row
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
                    "لا توجد عقارات معروضة في هذا القسم حالياً.",
                    fontSize = 11.5.sp,
                    color = Color(0xFF64748B),
                    modifier = Modifier.padding(16.dp),
                    textAlign = TextAlign.Center
                )
            }
        } else {
            filteredList.forEach { property ->
                Card(
                    onClick = { onPropertyClick(property) },
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    shape = RoundedCornerShape(16.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.5.dp),
                    border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
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
                                modifier = Modifier.size(60.dp)
                            ) {
                                val imgUrl = property.images.firstOrNull() ?: ""
                                if (imgUrl.isNotBlank()) {
                                    coil.compose.AsyncImage(
                                        model = imgUrl,
                                        contentDescription = property.title,
                                        contentScale = androidx.compose.ui.layout.ContentScale.Crop,
                                        modifier = Modifier.fillMaxSize()
                                    )
                                } else {
                                    Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                                        Icon(Icons.Default.Apartment, contentDescription = null, tint = Color(0xFF0D5C46), modifier = Modifier.size(30.dp))
                                    }
                                }
                            }

                            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                                Text(
                                    text = property.title,
                                    fontSize = 14.5.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF0F172A),
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )

                                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                    Icon(Icons.Default.LocationOn, contentDescription = null, tint = Color(0xFF64748B), modifier = Modifier.size(12.dp))
                                    Text(
                                        "${property.cityId} - ${property.localNeighborhood.ifBlank { "اليمن" }}",
                                        fontSize = 11.sp,
                                        color = Color(0xFF475569),
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }

                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = Color(0xFFDCFCE7),
                                    border = BorderStroke(0.5.dp, Color(0xFF10B981))
                                ) {
                                    Text(
                                        text = formatPriceWithCurrency(property.price, property.currency),
                                        fontSize = 11.sp,
                                        color = Color(0xFF065F46),
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                            }
                        }

                        // Instant Action Buttons
                        Spacer(modifier = Modifier.height(10.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            FilledTonalButton(
                                onClick = {
                                    try {
                                        val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:${property.phone}"))
                                        context.startActivity(intent)
                                    } catch (e: Exception) {
                                        Toast.makeText(context, "هاتف المالك: ${property.phone}", Toast.LENGTH_SHORT).show()
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
                                    Text("اتصال بالمالك", fontSize = 11.sp, fontWeight = FontWeight.Bold, maxLines = 1, softWrap = false)
                                }
                            }

                            Button(
                                onClick = { onPropertyClick(property) },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0D5C46)),
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.weight(1f).height(34.dp),
                                contentPadding = PaddingValues(horizontal = 4.dp, vertical = 0.dp)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(3.dp)
                                ) {
                                    Icon(Icons.Default.Visibility, contentDescription = "التفاصيل", tint = Color.White, modifier = Modifier.size(13.dp))
                                    Text("التفاصيل والصور", fontSize = 11.sp, color = Color.White, fontWeight = FontWeight.Bold, maxLines = 1, softWrap = false)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
