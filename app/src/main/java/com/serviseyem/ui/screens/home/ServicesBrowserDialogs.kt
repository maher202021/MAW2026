package com.serviseyem.ui.screens.home

import android.content.Context
import com.serviseyem.ui.*
import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import coil.compose.AsyncImage
import com.serviseyem.data.CityEntity
import com.serviseyem.data.JobEntity
import com.serviseyem.data.PropertyEntity
import com.serviseyem.data.StoreEntity
import com.serviseyem.ui.screens.home.extensions.isMedicalCenter
import com.serviseyem.ui.screens.home.extensions.isRestaurantOrCafe
import com.serviseyem.ui.MainViewModel
import com.serviseyem.utils.VisualThemePalette

/**
 * 🛠️ ServicesBrowserDialogs - النوافذ الحوارية لتفاصيل المتاجر والعقارات والتصفية المتقدمة
 */

@Composable
fun FilterPanelDialog(
    cities: List<CityEntity>,
    selectedCityId: String,
    onSelectCity: (String) -> Unit,
    isVipOnly: Boolean,
    onToggleVip: (Boolean) -> Unit,
    isAvailableOnly: Boolean,
    onToggleAvailable: (Boolean) -> Unit,
    radiusKm: Int,
    onRadiusChange: (Int) -> Unit,
    neighborhood: String,
    onNeighborhoodChange: (String) -> Unit,
    themeColors: VisualThemePalette,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A)),
            shape = RoundedCornerShape(16.dp),
            border = BorderStroke(1.5.dp, themeColors.accent),
            modifier = Modifier.fillMaxWidth().padding(8.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("⚙️ تصفية النتائج المتقدمة", fontSize = 13.5.sp, fontWeight = FontWeight.Bold, color = themeColors.accent)
                    IconButton(onClick = onDismiss, modifier = Modifier.size(24.dp)) {
                        Icon(Icons.Default.Close, contentDescription = "إغلاق", tint = Color.Red)
                    }
                }

                // Neighborhood Input
                OutlinedTextField(
                    value = neighborhood,
                    onValueChange = onNeighborhoodChange,
                    label = { Text("الحي / الشارع السكني", fontSize = 11.sp) },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedBorderColor = themeColors.accent,
                        unfocusedBorderColor = Color.Gray
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                // Switches
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("⭐ الفنيون المميزون فقط (VIP)", fontSize = 11.sp, color = Color.White)
                    Switch(
                        checked = isVipOnly,
                        onCheckedChange = onToggleVip,
                        colors = SwitchDefaults.colors(checkedThumbColor = themeColors.accent)
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("🟢 المتاحون للعمل الآن فقط", fontSize = 11.sp, color = Color.White)
                    Switch(
                        checked = isAvailableOnly,
                        onCheckedChange = onToggleAvailable,
                        colors = SwitchDefaults.colors(checkedThumbColor = themeColors.accent)
                    )
                }

                Button(
                    onClick = onDismiss,
                    colors = ButtonDefaults.buttonColors(containerColor = themeColors.accent),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth().height(44.dp)
                ) {
                    Text("تطبيق الفلاتر ✅", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                }
            }
        }
    }
}

@Composable
fun StoreQuickDetailsDialog(
    store: StoreEntity,
    context: Context,
    themeColors: VisualThemePalette,
    onDismiss: () -> Unit,
    onOpenDetails: () -> Unit = {}
) {
    val isRest = store.isRestaurantOrCafe()
    val isMed = store.isMedicalCenter()
    val typeIcon = when {
        isMed -> "🏥"
        isRest -> "🍽️"
        else -> "🏪"
    }
    val typeTitle = when {
        isMed -> "مركز طبي / عيادة"
        isRest -> "مطعم / كافيه"
        else -> "متجر تجاري"
    }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A)),
            shape = RoundedCornerShape(16.dp),
            border = BorderStroke(1.5.dp, themeColors.accent),
            modifier = Modifier.fillMaxWidth().padding(8.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(14.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("$typeIcon ${store.name}", fontSize = 13.5.sp, fontWeight = FontWeight.Bold, color = themeColors.accent)
                    IconButton(onClick = onDismiss, modifier = Modifier.size(24.dp)) {
                        Icon(Icons.Default.Close, contentDescription = "إغلاق", tint = Color.Red)
                    }
                }

                if (store.coverImage.isNotEmpty()) {
                    AsyncImage(
                        model = store.coverImage,
                        contentDescription = store.name,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(130.dp)
                            .clip(RoundedCornerShape(10.dp)),
                        contentScale = ContentScale.Crop
                    )
                }

                Text("نوع المنشأة: $typeTitle", fontSize = 11.5.sp, color = themeColors.accent, fontWeight = FontWeight.SemiBold)
                if (store.ownerName.isNotEmpty()) {
                    Text("المالك / المدير: ${store.ownerName}", fontSize = 11.5.sp, color = Color.White)
                }
                Text("الموقع: ${store.cityId} - ${store.localNeighborhood}", fontSize = 11.sp, color = Color.LightGray)
                if (store.workingHours.isNotEmpty()) {
                    Text("أوقات العمل: ${store.workingHours}", fontSize = 10.5.sp, color = Color.LightGray)
                }
                if (store.description.isNotEmpty()) {
                    Text(store.description, fontSize = 10.5.sp, color = Color.Gray)
                }

                Button(
                    onClick = onOpenDetails,
                    colors = ButtonDefaults.buttonColors(containerColor = themeColors.accent),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth().height(42.dp)
                ) {
                    Icon(Icons.Default.Info, contentDescription = null, tint = Color.Black)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("🛒 عرض صفحة المتجر والمنتجات بالكامل", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                }

                Button(
                    onClick = {
                        val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:${store.phone}"))
                        context.startActivity(intent)
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981)),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth().height(42.dp)
                ) {
                    Icon(Icons.Default.Phone, contentDescription = null, tint = Color.White)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("اتصال مباشر (${store.phone})", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                }
            }
        }
    }
}

@Composable
fun PropertyQuickDetailsDialog(
    property: PropertyEntity,
    context: Context,
    themeColors: VisualThemePalette,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A)),
            shape = RoundedCornerShape(16.dp),
            border = BorderStroke(1.5.dp, themeColors.accent),
            modifier = Modifier.fillMaxWidth().padding(8.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(14.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("🏡 ${property.title}", fontSize = 13.5.sp, fontWeight = FontWeight.Bold, color = themeColors.accent)
                    IconButton(onClick = onDismiss, modifier = Modifier.size(24.dp)) {
                        Icon(Icons.Default.Close, contentDescription = "إغلاق", tint = Color.Red)
                    }
                }

                if (property.images.isNotEmpty()) {
                    AsyncImage(
                        model = property.images.first(),
                        contentDescription = property.title,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(130.dp)
                            .clip(RoundedCornerShape(10.dp)),
                        contentScale = ContentScale.Crop
                    )
                }

                Text("السعر: ${property.price.toInt()} ${property.currency}", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = themeColors.accent)
                Text("النوع: ${property.propertyType} (${property.type})", fontSize = 11.sp, color = Color.White)
                Text("الموقع: ${property.cityId} - ${property.localNeighborhood}", fontSize = 11.sp, color = Color.LightGray)

                Button(
                    onClick = {
                        val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:${property.phone}"))
                        context.startActivity(intent)
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981)),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth().height(44.dp)
                ) {
                    Icon(Icons.Default.Phone, contentDescription = null, tint = Color.White)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("اتصال بصاحب العقار (${property.phone})", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 11.5.sp)
                }
            }
        }
    }
}

@Composable
fun JobQuickDetailsDialog(
    job: JobEntity,
    context: Context,
    themeColors: VisualThemePalette,
    onDismiss: () -> Unit,
    onApply: () -> Unit = {}
) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A)),
            shape = RoundedCornerShape(16.dp),
            border = BorderStroke(1.5.dp, themeColors.accent),
            modifier = Modifier.fillMaxWidth().padding(8.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(14.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("💼 ${job.title}", fontSize = 13.5.sp, fontWeight = FontWeight.Bold, color = themeColors.accent)
                    IconButton(onClick = onDismiss, modifier = Modifier.size(24.dp)) {
                        Icon(Icons.Default.Close, contentDescription = "إغلاق", tint = Color.Red)
                    }
                }

                Text("جهة العمل: ${job.companyName}", fontSize = 11.5.sp, color = Color.White, fontWeight = FontWeight.Bold)
                Text("الراتب المتوقع: ${job.salary}", fontSize = 11.sp, color = themeColors.accent)
                Text("الموقع والنوع: ${job.cityId} - ${job.jobType}", fontSize = 11.sp, color = Color.LightGray)
                if (job.description.isNotEmpty()) {
                    Text("تفاصيل الوظيفة:\n${job.description}", fontSize = 10.5.sp, color = Color.Gray)
                }

                Button(
                    onClick = {
                        onApply()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = themeColors.accent),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth().height(44.dp)
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, tint = Color.Black)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("التقديم على هذه الوظيفة 📩", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 11.5.sp)
                }

                Button(
                    onClick = {
                        val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:${job.phone}"))
                        context.startActivity(intent)
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981)),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth().height(44.dp)
                ) {
                    Icon(Icons.Default.Phone, contentDescription = null, tint = Color.White)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("اتصال مباشر (${job.phone})", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 11.5.sp)
                }
            }
        }
    }
}
