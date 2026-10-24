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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.serviseyem.data.ProductEntity
import com.serviseyem.data.UnifiedBusinessAccount
import com.serviseyem.rememberBase64Bitmap
import com.serviseyem.ui.MainViewModel
import com.serviseyem.utils.VisualThemePalette
import java.util.UUID

@Composable
fun ProductServiceManagementScreen(
    account: UnifiedBusinessAccount,
    viewModel: MainViewModel,
    themeColors: VisualThemePalette
) {
    val context = LocalContext.current
    val productsList by viewModel.products.collectAsState()

    var showAddDialog by remember { mutableStateOf(false) }
    var productToEdit by remember { mutableStateOf<ProductEntity?>(null) }

    val myProducts = remember(productsList, account.id) {
        productsList.filter { it.storeId == account.id }
    }

    Scaffold(
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = {
                    productToEdit = null
                    showAddDialog = true
                },
                icon = { Icon(Icons.Default.Add, contentDescription = "إضافة") },
                text = { Text("إضافة منتج/خدمة جديد", fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                containerColor = Color(0xFF10B981),
                contentColor = Color.White
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
                Text("🛒 قائمة المنتجات والخدمات المتاحة:", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = themeColors.accent)
                Surface(
                    color = themeColors.accent.copy(alpha = 0.2f),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        "${myProducts.size} عنصر",
                        fontSize = 11.sp,
                        color = themeColors.accent,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                    )
                }
            }

            if (myProducts.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("🛒", fontSize = 48.sp)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("لم تقم بإضافة أي منتجات حتى الآن", color = Color.Gray, fontSize = 12.sp)
                    }
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(myProducts, key = { it.id }) { product ->
                        ProductItemCard(
                            product = product,
                            themeColors = themeColors,
                            onEdit = {
                                productToEdit = product
                                showAddDialog = true
                            },
                            onDelete = {
                                viewModel.deleteProduct(product.id)
                                Toast.makeText(context, "تم حذف المنتج بنجاح", Toast.LENGTH_SHORT).show()
                            },
                            onToggleAvailable = { isAvail ->
                                val updated = product.copy(isAvailable = isAvail)
                                viewModel.saveProduct(updated)
                            }
                        )
                    }
                }
            }
        }
    }

    if (showAddDialog) {
        AddEditProductDialog(
            account = account,
            product = productToEdit,
            onDismiss = { showAddDialog = false },
            onSave = { newProd ->
                viewModel.saveProduct(newProd)
                showAddDialog = false
                Toast.makeText(context, "تم حفظ المنتج بنجاح!", Toast.LENGTH_SHORT).show()
            }
        )
    }
}

@Composable
fun ProductItemCard(
    product: ProductEntity,
    themeColors: VisualThemePalette,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onToggleAvailable: (Boolean) -> Unit
) {
    val bitmap = rememberBase64Bitmap(product.imageUrl)

    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = themeColors.surface),
        border = BorderStroke(1.dp, Color.Gray.copy(alpha = 0.3f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color.DarkGray)
            ) {
                if (bitmap != null) {
                    androidx.compose.foundation.Image(
                        bitmap = bitmap,
                        contentDescription = product.name,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else if (product.imageUrl.startsWith("http")) {
                    AsyncImage(
                        model = product.imageUrl,
                        contentDescription = product.name,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("🛒", fontSize = 24.sp)
                    }
                }
            }

            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(
                    text = product.name,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = product.description.ifBlank { "منتج ممتاز متوفر حالياً" },
                    fontSize = 10.sp,
                    color = Color.LightGray,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = "${product.price} ${product.currency}",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF10B981)
                )
            }

            Column(horizontalAlignment = Alignment.End, verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("متوفر:", fontSize = 9.sp, color = Color.Gray)
                    Switch(
                        checked = product.isAvailable,
                        onCheckedChange = onToggleAvailable
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
}

@Composable
fun AddEditProductDialog(
    account: UnifiedBusinessAccount,
    product: ProductEntity?,
    onDismiss: () -> Unit,
    onSave: (ProductEntity) -> Unit
) {
    var titleInput by remember { mutableStateOf(product?.name ?: "") }
    var descInput by remember { mutableStateOf(product?.description ?: "") }
    var priceInput by remember { mutableStateOf(product?.price?.toString() ?: "1000") }
    var imageInput by remember { mutableStateOf(product?.imageUrl ?: "") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (product == null) "إضافة منتج/خدمة جديدة" else "تعديل المنتج", fontSize = 14.sp, fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = titleInput,
                    onValueChange = { titleInput = it },
                    label = { Text("اسم المنتج أو الخدمة") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = descInput,
                    onValueChange = { descInput = it },
                    label = { Text("الوصف / التفاصيل") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = priceInput,
                    onValueChange = { priceInput = it },
                    label = { Text("السعر (YER)") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = imageInput,
                    onValueChange = { imageInput = it },
                    label = { Text("رابط أو صورة المنتج (Base64)") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val parsedPrice = priceInput.toDoubleOrNull() ?: 0.0
                    if (titleInput.isNotBlank()) {
                        val newProd = (product ?: ProductEntity(
                            id = UUID.randomUUID().toString(),
                            storeId = account.id
                        )).copy(
                            name = titleInput,
                            description = descInput,
                            price = parsedPrice,
                            imageUrl = imageInput
                        )
                        onSave(newProd)
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981))
            ) {
                Text("حفظ المنتج", color = Color.White)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("إلغاء")
            }
        }
    )
}
