package com.serviseyem.ui

import androidx.compose.foundation.BorderStroke
import com.serviseyem.ui.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.serviseyem.data.*
import com.serviseyem.utils.VisualThemePalette
import androidx.compose.ui.graphics.ImageBitmap
import com.serviseyem.rememberBase64Bitmap

/**
 * 📦 Product Item Card Component with Inline Price Editing for Owner/Admin
 */
@Composable
fun ProductListItemCard(
    product: ProductEntity,
    isOwnerOrAdmin: Boolean,
    themeColors: VisualThemePalette,
    viewModel: MainViewModel,
    isMedical: Boolean = false,
    isRestaurant: Boolean = false,
    onOrderClick: () -> Unit
) {
    val context = LocalContext.current
    var editingPrice by remember(product.price) { mutableStateOf(if (product.price % 1.0 == 0.0) product.price.toLong().toString() else product.price.toString()) }
    var editingDiscount by remember(product.discountPercent) { mutableStateOf(product.discountPercent.toString()) }
    var isPriceEditing by remember { mutableStateOf(false) }

    Card(
        colors = CardDefaults.cardColors(containerColor = Color.Black.copy(alpha = 0.35f)),
        shape = RoundedCornerShape(10.dp),
        border = BorderStroke(1.dp, themeColors.accent.copy(alpha = 0.2f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Product Image Thumbnail
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color.DarkGray),
                contentAlignment = Alignment.Center
            ) {
                val fallbackEmoji = when {
                    isMedical -> "🏥"
                    isRestaurant -> "🍔"
                    else -> "📦"
                }
                com.serviseyem.ui.components.SmartAsyncImage(
                    model = product.imageUrl,
                    contentDescription = product.name,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop,
                    fallbackEmoji = fallbackEmoji
                )
            }

            Spacer(modifier = Modifier.width(10.dp))

            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        text = product.name,
                        fontSize = 13.5.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    if (product.isOffer || product.discountPercent > 0) {
                        Box(
                            modifier = Modifier
                                .background(Color(0xFFEF4444), RoundedCornerShape(4.dp))
                                .padding(horizontal = 4.dp, vertical = 1.dp)
                        ) {
                            Text(
                                text = if (product.discountPercent > 0) "🔥 خصم %${product.discountPercent}" else "🔥 عرض خاص",
                                fontSize = 8.sp,
                                color = Color.White,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                    if (product.isOffer || (product.price > 0 && product.price <= 2000)) {
                        Box(
                            modifier = Modifier
                                .background(Color(0xFFFFD700), RoundedCornerShape(4.dp))
                                .padding(horizontal = 4.dp, vertical = 1.dp)
                        ) {
                            Text("🏷️ أرخص سعر", fontSize = 8.sp, color = Color.Black, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                if (product.description.isNotEmpty()) {
                    Text(
                        text = product.description,
                        fontSize = 10.sp,
                        color = themeColors.textSecondary,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                // Price display or Inline Price Editor
                if (isOwnerOrAdmin && isPriceEditing) {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(4.dp),
                        modifier = Modifier.padding(top = 2.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            OutlinedTextField(
                                value = editingPrice,
                                onValueChange = { editingPrice = it },
                                label = { Text("السعر", fontSize = 8.sp) },
                                modifier = Modifier.width(75.dp).height(42.dp),
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedTextColor = Color.White,
                                    unfocusedTextColor = Color.White
                                )
                            )
                            OutlinedTextField(
                                value = editingDiscount,
                                onValueChange = { editingDiscount = it },
                                label = { Text("خصم %", fontSize = 8.sp) },
                                modifier = Modifier.width(65.dp).height(42.dp),
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedTextColor = Color.White,
                                    unfocusedTextColor = Color.White
                                )
                            )
                            Button(
                                onClick = {
                                    val pVal = editingPrice.toDoubleOrNull() ?: product.price
                                    val dVal = editingDiscount.toIntOrNull() ?: 0
                                    val oldP = if (dVal > 0 && product.oldPrice <= 0.0) pVal else product.oldPrice
                                    viewModel.saveProduct(
                                        product.copy(
                                            price = pVal,
                                            discountPercent = dVal,
                                            isOffer = dVal > 0,
                                            oldPrice = oldP
                                        )
                                    )
                                    isPriceEditing = false
                                    android.widget.Toast.makeText(context, "✅ تم حفظ التعديل السريع للمنتج!", android.widget.Toast.LENGTH_SHORT).show()
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981)),
                                contentPadding = PaddingValues(horizontal = 6.dp, vertical = 2.dp),
                                modifier = Modifier.height(36.dp)
                            ) {
                                Text("💾 حفظ", fontSize = 9.sp, color = Color.White)
                            }
                        }
                    }
                } else {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = com.serviseyem.utils.formatPriceWithCurrency(product.price, product.currency),
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF10B981)
                        )
                        if (product.oldPrice > product.price && product.oldPrice > 0.0) {
                            Text(
                                text = com.serviseyem.utils.formatPriceWithCurrency(product.oldPrice, product.currency),
                                fontSize = 11.sp,
                                color = Color.Gray,
                                style = androidx.compose.ui.text.TextStyle(textDecoration = androidx.compose.ui.text.style.TextDecoration.LineThrough)
                            )
                        }
                        if (isOwnerOrAdmin) {
                            Text(
                                text = "✏️ تعديل سريع",
                                fontSize = 9.sp,
                                color = Color.Cyan,
                                modifier = Modifier
                                    .clickable { isPriceEditing = true }
                                    .padding(horizontal = 2.dp)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.width(6.dp))

            // Order Action Button
            Button(
                onClick = onOrderClick,
                colors = ButtonDefaults.buttonColors(containerColor = if (isMedical) Color(0xFF0284C7) else themeColors.accent),
                shape = RoundedCornerShape(8.dp),
                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
            ) {
                val btnText = when {
                    isMedical -> "📅 حجز موعد"
                    isRestaurant -> "🍽️ طلب الوجبة"
                    else -> "🛒 شراء"
                }
                Text(btnText, color = if (isMedical) Color.White else Color.Black, fontSize = 10.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}
