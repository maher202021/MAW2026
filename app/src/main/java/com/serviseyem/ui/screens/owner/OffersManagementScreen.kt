package com.serviseyem.ui.screens.owner

import android.widget.Toast
import com.serviseyem.ui.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.serviseyem.data.CouponEntity
import com.serviseyem.data.UnifiedBusinessAccount
import com.serviseyem.ui.MainViewModel
import com.serviseyem.utils.VisualThemePalette
import java.util.UUID

@Composable
fun OffersManagementScreen(
    account: UnifiedBusinessAccount,
    viewModel: MainViewModel,
    themeColors: VisualThemePalette
) {
    val context = LocalContext.current
    val couponsList by viewModel.coupons.collectAsState()

    var showAddOfferDialog by remember { mutableStateOf(false) }
    var offerToEdit by remember { mutableStateOf<CouponEntity?>(null) }

    Scaffold(
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = {
                    offerToEdit = null
                    showAddOfferDialog = true
                },
                icon = { Icon(Icons.Default.Add, contentDescription = "إضافة") },
                text = { Text("إضافة كوبون/عرض خصم جديد", fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                containerColor = Color(0xFFF59E0B),
                contentColor = Color.Black
            )
        },
        containerColor = themeColors.background
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("🎁 قائمة كوبونات العروض والتخفيضات:", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = themeColors.accent)
                Surface(
                    color = Color(0xFFF59E0B).copy(alpha = 0.2f),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        "${couponsList.size} كوبونات",
                        fontSize = 11.sp,
                        color = Color(0xFFF59E0B),
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                    )
                }
            }

            if (couponsList.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("🎁", fontSize = 48.sp)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("لا توجد عروض أو كوبونات تخفيض حالياً", color = Color.Gray, fontSize = 12.sp)
                    }
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(couponsList, key = { it.id }) { offer ->
                        OfferItemCard(
                            offer = offer,
                            themeColors = themeColors,
                            onEdit = {
                                offerToEdit = offer
                                showAddOfferDialog = true
                            },
                            onDelete = {
                                viewModel.deleteCoupon(offer.id)
                                Toast.makeText(context, "تم حذف الكوبون بنجاح", Toast.LENGTH_SHORT).show()
                            }
                        )
                    }
                }
            }
        }
    }

    if (showAddOfferDialog) {
        AddEditOfferDialog(
            account = account,
            offer = offerToEdit,
            onDismiss = { showAddOfferDialog = false },
            onSave = { newCoupon ->
                viewModel.saveCoupon(newCoupon)
                showAddOfferDialog = false
                Toast.makeText(context, "تم نشر كوبون الخصم بنجاح!", Toast.LENGTH_SHORT).show()
            }
        )
    }
}

@Composable
fun OfferItemCard(
    offer: CouponEntity,
    themeColors: VisualThemePalette,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = themeColors.surface),
        border = BorderStroke(1.dp, Color(0xFFF59E0B).copy(alpha = 0.4f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Surface(
                color = Color(0xFFF59E0B).copy(alpha = 0.2f),
                shape = RoundedCornerShape(10.dp)
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                ) {
                    Text("${offer.discountPercentage}%", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color(0xFFF59E0B))
                    Text("خصم", fontSize = 9.sp, color = Color.White)
                }
            }

            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(
                    text = "كود الخصم: ${offer.code}",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = "النقاط المكتسبة: ${offer.pointsValue} نقطة",
                    fontSize = 10.sp,
                    color = Color.LightGray,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = "حالة الكوبون: ${offer.status}",
                    fontSize = 9.sp,
                    color = Color(0xFF10B981)
                )
            }

            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                IconButton(onClick = onEdit, modifier = Modifier.size(28.dp)) {
                    Icon(Icons.Default.Edit, contentDescription = "تعديل", tint = Color(0xFF3B82F6), modifier = Modifier.size(16.dp))
                }
                IconButton(onClick = onDelete, modifier = Modifier.size(28.dp)) {
                    Icon(Icons.Default.Delete, contentDescription = "حذف", tint = Color(0xFFEF4444), modifier = Modifier.size(16.dp))
                }
            }
        }
    }
}

@Composable
fun AddEditOfferDialog(
    account: UnifiedBusinessAccount,
    offer: CouponEntity?,
    onDismiss: () -> Unit,
    onSave: (CouponEntity) -> Unit
) {
    var codeInput by remember { mutableStateOf(offer?.code ?: "YEMEN2026") }
    var percentInput by remember { mutableStateOf(offer?.discountPercentage?.toString() ?: "20") }
    var pointsInput by remember { mutableStateOf(offer?.pointsValue?.toString() ?: "50") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (offer == null) "إضافة كوبون جديد" else "تعديل الكوبون", fontSize = 14.sp, fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = codeInput,
                    onValueChange = { codeInput = it },
                    label = { Text("رمز كود الخصم (Code)") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = percentInput,
                    onValueChange = { percentInput = it },
                    label = { Text("نسبة الخصم (%)") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = pointsInput,
                    onValueChange = { pointsInput = it },
                    label = { Text("قيمة النقاط (Points)") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (codeInput.isNotBlank()) {
                        val newOffer = (offer ?: CouponEntity(
                            id = UUID.randomUUID().toString()
                        )).copy(
                            code = codeInput,
                            discountPercentage = percentInput.toIntOrNull() ?: 20,
                            pointsValue = pointsInput.toIntOrNull() ?: 50
                        )
                        onSave(newOffer)
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF59E0B))
            ) {
                Text("نشر الكوبون", color = Color.Black, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("إلغاء")
            }
        }
    )
}
