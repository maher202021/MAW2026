package com.serviseyem.ui.screens.dashboard.components

import android.content.Context
import com.serviseyem.ui.*
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.platform.LocalContext
import com.serviseyem.data.ProductEntity
import com.serviseyem.ui.MainViewModel
import com.serviseyem.utils.VisualThemePalette

/**
 * 📦 StoreProductCatalogCard - بطاقة إظهار وإدارة كتالوج المنتجات للمتجر
 */
@Composable
fun StoreProductCatalogCard(
    storeProducts: List<ProductEntity>,
    viewModel: MainViewModel,
    themeColors: VisualThemePalette,
    context: Context = LocalContext.current,
    onAddProductClick: () -> Unit
) {
    var displayLimit by remember { mutableStateOf(20) }
    val displayedProducts = remember(storeProducts, displayLimit) {
        storeProducts.take(displayLimit)
    }

    Card(
        colors = CardDefaults.cardColors(containerColor = themeColors.surface),
        border = BorderStroke(1.dp, themeColors.accent.copy(alpha = 0.3f)),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("📦 كتالوج المنتجات المعروضة (${storeProducts.size}):", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = themeColors.accent)
                Button(
                    onClick = onAddProductClick,
                    colors = ButtonDefaults.buttonColors(containerColor = themeColors.primary),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 2.dp),
                    modifier = Modifier.height(28.dp)
                ) {
                    Text("+ إضافة منتج", color = Color.White, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                }
            }

            if (storeProducts.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text("📭 لا توجد منتجات مضافة لهذا المحل حالياً.", fontSize = 10.sp, color = Color.Gray)
                }
            } else {
                displayedProducts.forEach { prod ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color.Black.copy(alpha = 0.2f), RoundedCornerShape(6.dp))
                            .padding(8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                            val pBitmap = remember(prod.imageUrl) {
                                if (prod.imageUrl.isNotEmpty()) {
                                    try {
                                        val bytes = android.util.Base64.decode(prod.imageUrl, android.util.Base64.DEFAULT)
                                        android.graphics.BitmapFactory.decodeByteArray(bytes, 0, bytes.size)?.asImageBitmap()
                                    } catch(e: Exception) { null }
                                } else null
                            }
                            if (pBitmap != null) {
                                Image(
                                    bitmap = pBitmap,
                                    contentDescription = null,
                                    modifier = Modifier.size(36.dp).clip(RoundedCornerShape(4.dp)),
                                    contentScale = ContentScale.Crop
                                )
                            } else {
                                Box(modifier = Modifier.size(36.dp).background(Color.DarkGray, RoundedCornerShape(4.dp)), contentAlignment = Alignment.Center) {
                                    Text("📦", fontSize = 14.sp)
                                }
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(prod.name, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                Text("السعر: ${prod.price} ريال يمني", fontSize = 9.sp, color = themeColors.accent)
                            }
                        }

                        IconButton(
                            onClick = {
                                viewModel.deleteProduct(prod.id)
                                Toast.makeText(context, "🗑️ تم حذف المنتج بنجاح", Toast.LENGTH_SHORT).show()
                            },
                            modifier = Modifier.size(24.dp)
                        ) {
                            Text("🗑️", fontSize = 14.sp)
                        }
                    }
                }

                if (storeProducts.size > displayLimit || displayLimit > 20) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center
                    ) {
                        if (storeProducts.size > displayLimit) {
                            TextButton(onClick = { displayLimit += 20 }) {
                                Text("عرض المزيد (+20 منتج)", color = themeColors.accent, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                        if (displayLimit > 20) {
                            Spacer(Modifier.width(8.dp))
                            TextButton(onClick = { displayLimit = 20 }) {
                                Text("عرض أقل", color = Color.Gray, fontSize = 11.sp)
                            }
                        }
                    }
                }
            }
        }
    }
}
