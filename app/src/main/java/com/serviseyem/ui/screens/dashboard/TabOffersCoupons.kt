package com.serviseyem.ui.screens.dashboard

import androidx.compose.foundation.layout.*
import com.serviseyem.ui.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.serviseyem.ui.screens.dashboard.components.UnifiedEmptyState
import androidx.hilt.navigation.compose.hiltViewModel
import com.serviseyem.ui.screens.dashboard.viewmodels.OffersViewModel
import com.serviseyem.utils.VisualThemePalette
import com.serviseyem.data.SpecialOfferEntity

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.ui.platform.LocalContext
import android.widget.Toast

@Composable
fun TabOffersCoupons(
    storeId: String = "",
    themeColors: VisualThemePalette,
    viewModel: OffersViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val offersList by viewModel.offers.collectAsState()
    var showAddDialog by remember { mutableStateOf(false) }
    var itemToDelete by remember { mutableStateOf<SpecialOfferEntity?>(null) }

    var titleInput by remember { mutableStateOf("") }
    var descInput by remember { mutableStateOf("") }
    var discountInput by remember { mutableStateOf("") }
    var codeInput by remember { mutableStateOf("") }

    val displayedOffers = remember(offersList, storeId) {
        if (storeId.isNotBlank()) {
            offersList.filter { it.providerId == storeId || it.providerId.isBlank() }
        } else {
            offersList
        }
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = "🏷️ العروض والتخفيضات والكوبونات (${displayedOffers.size})", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = themeColors.textPrimary)
            Button(
                onClick = { showAddDialog = true },
                colors = ButtonDefaults.buttonColors(containerColor = themeColors.accent),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text("إضافة عرض ➕", fontSize = 12.sp, color = Color.Black, fontWeight = FontWeight.Bold)
            }
        }

        if (displayedOffers.isEmpty()) {
            UnifiedEmptyState(
                title = "لا توجد عروض ترويجية نشطة حالياً",
                description = "يمكنك إضافة عروض خصم أو كود خصم لجذب المزيد من العملاء.",
                iconText = "🏷️",
                actionLabel = "إضافة عرض جديد ➕",
                onActionClick = { showAddDialog = true },
                themeColors = themeColors
            )
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(displayedOffers, key = { it.id }) { offer ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = themeColors.surface),
                        shape = RoundedCornerShape(12.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.08f))
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                Text(text = offer.title, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = themeColors.textPrimary)
                                if (offer.description.isNotBlank()) {
                                    Text(text = offer.description, fontSize = 11.sp, color = themeColors.textSecondary)
                                }
                                if (offer.couponCode.isNotBlank()) {
                                    Text(text = "كود الخصم: ${offer.couponCode}", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = themeColors.accent)
                                }
                            }
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                Surface(
                                    color = Color(0xFFEF4444),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Text(
                                        text = "خصم %${offer.discountPercent}",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White,
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                                    )
                                }
                                IconButton(
                                    onClick = { itemToDelete = offer },
                                    modifier = Modifier.size(32.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Delete,
                                        contentDescription = "حذف العرض",
                                        tint = Color(0xFFEF4444),
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    val target = itemToDelete
    if (target != null) {
        AlertDialog(
            onDismissRequest = { itemToDelete = null },
            title = { Text("تأكيد حذف العرض 🗑️", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color.White) },
            text = {
                Text(
                    "هل أنت متأكد من رغبتك في حذف العرض '${target.title}'؟ لن يتمكن العملاء من استخدامه بعد الحذف.",
                    fontSize = 12.sp,
                    color = Color.LightGray
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.deleteOffer(target.id)
                        itemToDelete = null
                        Toast.makeText(context, "تم حذف العرض بنجاح 🗑️", Toast.LENGTH_SHORT).show()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF4444))
                ) {
                    Text("حذف نهائي", color = Color.White, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { itemToDelete = null }) {
                    Text("إلغاء", color = Color.Gray)
                }
            },
            containerColor = Color(0xFF1E293B)
        )
    }

    if (showAddDialog) {
        AlertDialog(
            onDismissRequest = { showAddDialog = false },
            title = { Text("إضافة عرض أو كوبون خصم جديد", fontSize = 14.sp, fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = titleInput,
                        onValueChange = { titleInput = it },
                        label = { Text("عنوان العرض (مثال: خصم الصيف)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = descInput,
                        onValueChange = { descInput = it },
                        label = { Text("وصف العرض / الشروط") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = discountInput,
                        onValueChange = { discountInput = it },
                        label = { Text("نسبة الخصم %") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = codeInput,
                        onValueChange = { codeInput = it },
                        label = { Text("كود الكوبون (اختياري)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val disc = discountInput.toIntOrNull() ?: 10
                        if (titleInput.isNotBlank()) {
                            val newOffer = SpecialOfferEntity(
                                id = java.util.UUID.randomUUID().toString(),
                                providerId = storeId,
                                title = titleInput.trim(),
                                description = descInput.trim(),
                                discountPercent = disc,
                                couponCode = codeInput.trim(),
                                isEnabled = true
                            )
                            viewModel.addOffer(newOffer)
                            Toast.makeText(context, "تمت إضافة العرض الترويجي بنجاح 🏷️", Toast.LENGTH_SHORT).show()
                            titleInput = ""
                            descInput = ""
                            discountInput = ""
                            codeInput = ""
                            showAddDialog = false
                        } else {
                            Toast.makeText(context, "يرجى إدخال عنوان العرض أولاً", Toast.LENGTH_SHORT).show()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = themeColors.accent)
                ) {
                    Text("إضافة العرض", color = Color.Black, fontWeight = FontWeight.Bold)
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
