package com.serviseyem

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.serviseyem.data.*
import com.serviseyem.ui.*
import com.serviseyem.ui.MainViewModel
import com.serviseyem.utils.VisualThemePalette

/**
 * 📈 Price Adjuster Modal for Store Owner / Admin (Supports All Items OR Single Selected Item)
 */
@Composable
fun BulkPriceAdjusterDialog(
    storeId: String,
    products: List<ProductEntity>,
    viewModel: MainViewModel,
    themeColors: VisualThemePalette,
    onDismiss: () -> Unit
) {
    var targetScope by remember { mutableStateOf("SINGLE") } // "ALL" or "SINGLE"
    var selectedProductId by remember { mutableStateOf(products.firstOrNull()?.id ?: "") }
    var singleNewPriceInput by remember {
        mutableStateOf(products.find { it.id == (products.firstOrNull()?.id ?: "") }?.price?.toString() ?: "")
    }
    var percentInput by remember { mutableStateOf("10") }
    var actionType by remember { mutableStateOf("INCREASE") } // INCREASE or DECREASE
    var singleAdjustMode by remember { mutableStateOf("DIRECT") } // "DIRECT" (new price) or "PERCENT" (%)
    val context = LocalContext.current

    val currentSelectedProduct = remember(selectedProductId, products) {
        products.find { it.id == selectedProductId }
    }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            colors = CardDefaults.cardColors(containerColor = themeColors.surface),
            shape = RoundedCornerShape(16.dp),
            border = BorderStroke(2.dp, themeColors.accent),
            modifier = Modifier.fillMaxWidth().padding(12.dp)
        ) {
            Column(
                modifier = Modifier
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text("📈 رفع وتعديل الأسعار:", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Color.White)
                
                // Scope selector: Single item vs All items
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilterChip(
                        selected = targetScope == "SINGLE",
                        onClick = {
                            targetScope = "SINGLE"
                            if (selectedProductId.isEmpty() && products.isNotEmpty()) {
                                selectedProductId = products.first().id
                                singleNewPriceInput = products.first().price.toString()
                            }
                        },
                        label = { Text("🎯 سلعة/خدمة واحدة فقط", fontSize = 11.sp) },
                        modifier = Modifier.weight(1f)
                    )
                    FilterChip(
                        selected = targetScope == "ALL",
                        onClick = { targetScope = "ALL" },
                        label = { Text("🌐 جميع السلع (${products.size})", fontSize = 11.sp) },
                        modifier = Modifier.weight(1f)
                    )
                }

                if (targetScope == "SINGLE") {
                    if (products.isEmpty()) {
                        Text("⚠️ لا توجد منتجات أو خدمات مضافة حالياً لتعديل سعرها.", fontSize = 12.sp, color = Color(0xFFEF4444))
                    } else {
                        // Product Picker
                        var expandedPicker by remember { mutableStateOf(false) }
                        Text("اختر السلعة أو الخدمة المراد تعديل سعرها:", fontSize = 11.sp, color = themeColors.textSecondary)
                        
                        Box(modifier = Modifier.fillMaxWidth()) {
                            OutlinedCard(
                                onClick = { expandedPicker = true },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(10.dp),
                                border = BorderStroke(1.dp, themeColors.accent.copy(alpha = 0.5f)),
                                colors = CardDefaults.outlinedCardColors(containerColor = Color.Black.copy(alpha = 0.2f))
                            ) {
                                Row(
                                    modifier = Modifier.padding(12.dp).fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = currentSelectedProduct?.name ?: "اختر منتج...",
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color.White
                                        )
                                        Text(
                                            text = "السعر الحالي: ${currentSelectedProduct?.price ?: 0} ${currentSelectedProduct?.currency ?: "ريال"}",
                                            fontSize = 10.sp,
                                            color = themeColors.accent
                                        )
                                    }
                                    Text("▼", color = Color.White, fontSize = 12.sp)
                                }
                            }

                            DropdownMenu(
                                expanded = expandedPicker,
                                onDismissRequest = { expandedPicker = false },
                                modifier = Modifier.background(Color(0xFF1E293B)).fillMaxWidth(0.85f)
                            ) {
                                products.forEach { p ->
                                    DropdownMenuItem(
                                        text = {
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween
                                            ) {
                                                Text(p.name, color = Color.White, fontSize = 11.sp, maxLines = 1, overflow = TextOverflow.Ellipsis, modifier = Modifier.weight(1f))
                                                Text("${p.price} ${p.currency}", color = themeColors.accent, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                            }
                                        },
                                        onClick = {
                                            selectedProductId = p.id
                                            singleNewPriceInput = p.price.toString()
                                            expandedPicker = false
                                        }
                                    )
                                }
                            }
                        }

                        // Single adjustment method: Direct Price or Percentage
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            FilterChip(
                                selected = singleAdjustMode == "DIRECT",
                                onClick = { singleAdjustMode = "DIRECT" },
                                label = { Text("💰 إدخال السعر الجديد مباشرة", fontSize = 10.sp) },
                                modifier = Modifier.weight(1f)
                            )
                            FilterChip(
                                selected = singleAdjustMode == "PERCENT",
                                onClick = { singleAdjustMode = "PERCENT" },
                                label = { Text("📊 نسبة مئوية (%)", fontSize = 10.sp) },
                                modifier = Modifier.weight(1f)
                            )
                        }

                        if (singleAdjustMode == "DIRECT") {
                            OutlinedTextField(
                                value = singleNewPriceInput,
                                onValueChange = { singleNewPriceInput = it },
                                label = { Text("السعر الجديد (${currentSelectedProduct?.currency ?: "ريال"})", color = themeColors.textSecondary) },
                                modifier = Modifier.fillMaxWidth(),
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedTextColor = Color.White,
                                    unfocusedTextColor = Color.White
                                )
                            )
                        } else {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                FilterChip(
                                    selected = actionType == "INCREASE",
                                    onClick = { actionType = "INCREASE" },
                                    label = { Text("📈 رفع (+)") },
                                    modifier = Modifier.weight(1f)
                                )
                                FilterChip(
                                    selected = actionType == "DECREASE",
                                    onClick = { actionType = "DECREASE" },
                                    label = { Text("📉 تخفيض (-)") },
                                    modifier = Modifier.weight(1f)
                                )
                            }

                            OutlinedTextField(
                                value = percentInput,
                                onValueChange = { percentInput = it },
                                label = { Text("النسبة المئوية (%)", color = themeColors.textSecondary) },
                                modifier = Modifier.fillMaxWidth(),
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedTextColor = Color.White,
                                    unfocusedTextColor = Color.White
                                )
                            )
                        }
                    }
                } else {
                    // ALL ITEMS
                    Text("سيتم تطبيق نسبة التعديل على جميع السلع والخدمات (${products.size} عنصر):", fontSize = 11.sp, color = themeColors.textSecondary)

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        FilterChip(
                            selected = actionType == "INCREASE",
                            onClick = { actionType = "INCREASE" },
                            label = { Text("📈 رفع أسعار الجميع (+)") },
                            modifier = Modifier.weight(1f)
                        )
                        FilterChip(
                            selected = actionType == "DECREASE",
                            onClick = { actionType = "DECREASE" },
                            label = { Text("📉 تخفيض أسعار الجميع (-)") },
                            modifier = Modifier.weight(1f)
                        )
                    }

                    OutlinedTextField(
                        value = percentInput,
                        onValueChange = { percentInput = it },
                        label = { Text("النسبة المئوية لجميع المنتجات (%)", color = themeColors.textSecondary) },
                        modifier = Modifier.fillMaxWidth(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        )
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("إلغاء", color = Color.Gray)
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            if (targetScope == "SINGLE") {
                                val prod = currentSelectedProduct
                                if (prod != null) {
                                    val newPrice = if (singleAdjustMode == "DIRECT") {
                                        singleNewPriceInput.toDoubleOrNull() ?: prod.price
                                    } else {
                                        val pct = percentInput.toDoubleOrNull() ?: 0.0
                                        val factor = if (actionType == "INCREASE") (1.0 + pct / 100.0) else (1.0 - pct / 100.0)
                                        (prod.price * factor).let { Math.round(it * 10.0) / 10.0 }
                                    }
                                    viewModel.saveProduct(prod.copy(price = newPrice))
                                    android.widget.Toast.makeText(context, "✅ تم تحديث سعر '${prod.name}' إلى $newPrice ${prod.currency} بنجاح!", android.widget.Toast.LENGTH_LONG).show()
                                    onDismiss()
                                }
                            } else {
                                val pct = percentInput.toDoubleOrNull() ?: 0.0
                                if (pct > 0) {
                                    val factor = if (actionType == "INCREASE") (1.0 + pct / 100.0) else (1.0 - pct / 100.0)
                                    products.forEach { p ->
                                        val newP = (p.price * factor).let { Math.round(it * 10.0) / 10.0 }
                                        viewModel.saveProduct(p.copy(price = newP))
                                    }
                                    android.widget.Toast.makeText(context, "✅ تم تحديث أسعار ${products.size} منتج وخدمة بنجاح!", android.widget.Toast.LENGTH_LONG).show()
                                    onDismiss()
                                }
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = themeColors.accent)
                    ) {
                        Text(if (targetScope == "SINGLE") "حفظ السعر المحدد" else "تطبيق على جميع المنتجات", color = Color.Black, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

/**
 * ➕ Quick Add Product Modal for Store Owner / Admin
 */
@Composable
fun QuickAddProductDialog(
    storeId: String,
    viewModel: MainViewModel,
    themeColors: VisualThemePalette,
    onDismiss: () -> Unit
) {
    var name by remember { mutableStateOf("") }
    var desc by remember { mutableStateOf("") }
    var priceStr by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("خدمات") }
    var isOffer by remember { mutableStateOf(false) }
    var productImageBase64 by remember { mutableStateOf("") }

    val context = LocalContext.current

    val productImagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        if (uri != null) {
            try {
                val base64 = com.serviseyem.utils.compressAndResizeImageUri(context, uri, 800, 70)
                if (base64.isNotEmpty()) {
                    productImageBase64 = base64
                    android.widget.Toast.makeText(context, "📷 تم اختيار ضغط صورة المنتج بنجاح!", android.widget.Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            colors = CardDefaults.cardColors(containerColor = themeColors.surface),
            shape = RoundedCornerShape(16.dp),
            border = BorderStroke(2.dp, themeColors.accent),
            modifier = Modifier.fillMaxWidth().padding(16.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text("➕ إضافة منتج / سلعة جديدة للمتجر:", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color.White)

                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("اسم المنتج / السلعة", color = themeColors.textSecondary) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White)
                )

                OutlinedTextField(
                    value = desc,
                    onValueChange = { desc = it },
                    label = { Text("وصف السلعة / التفاصيل", color = themeColors.textSecondary) },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White)
                )

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = priceStr,
                        onValueChange = { priceStr = it },
                        label = { Text("السعر (YER)", color = themeColors.textSecondary) },
                        modifier = Modifier.weight(1f),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White)
                    )

                    OutlinedTextField(
                        value = category,
                        onValueChange = { category = it },
                        label = { Text("التبويب / القسم", color = themeColors.textSecondary) },
                        modifier = Modifier.weight(1f),
                        colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White)
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Button(
                        onClick = { productImagePickerLauncher.launch("image/*") },
                        colors = ButtonDefaults.buttonColors(containerColor = if (productImageBase64.isNotEmpty()) Color(0xFF10B981) else themeColors.accent)
                    ) {
                        Text(if (productImageBase64.isNotEmpty()) "تم اختيار صورة 📷" else "رفع صورة المنتج 📷", fontSize = 11.sp, color = Color.Black, fontWeight = FontWeight.Bold)
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Checkbox(checked = isOffer, onCheckedChange = { isOffer = it })
                        Text("تعيين كـ عرض خاص 🔥", fontSize = 11.sp, color = Color.White)
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) { Text("إلغاء", color = Color.Gray) }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            if (name.isNotBlank()) {
                                val p = ProductEntity(
                                    storeId = storeId,
                                    name = name,
                                    description = desc,
                                    price = priceStr.toDoubleOrNull() ?: 0.0,
                                    category = category.ifBlank { "خدمات" },
                                    isOffer = isOffer,
                                    imageUrl = if (productImageBase64.isNotEmpty()) "data:image/jpeg;base64,$productImageBase64" else ""
                                )
                                viewModel.saveProduct(p)
                                android.widget.Toast.makeText(context, "✅ تم إضافة المنتج بنجاح!", android.widget.Toast.LENGTH_SHORT).show()
                                onDismiss()
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = themeColors.accent)
                    ) {
                        Text("حفظ السلعة", color = Color.Black, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
