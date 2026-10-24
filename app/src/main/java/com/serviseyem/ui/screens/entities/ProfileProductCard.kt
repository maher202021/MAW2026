package com.serviseyem.ui.screens.entities

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.serviseyem.data.ProductEntity
import com.serviseyem.ui.components.SmartAsyncImage
import com.serviseyem.utils.VisualThemePalette

/**
 * 🛍️ ProfileProductCard - بطاقة منتج مربعة 1:1 لكالوج المتجر (عمودين)
 * تحتوي على: صورة مربعة 1:1، شارة تخفيض، الاسم، السعر بالريال اليمني، وزر الإضافة السريع (+)
 */
@Composable
fun ProfileProductCard(
    product: ProductEntity,
    themeColors: VisualThemePalette,
    modifier: Modifier = Modifier,
    onAddToCartClick: () -> Unit = {},
    onClick: () -> Unit = {}
) {
    val discountVal = if (product.isOffer && product.discountPercent > 0) {
        product.discountPercent
    } else if (product.oldPrice > product.price && product.oldPrice > 0) {
        (((product.oldPrice - product.price) / product.oldPrice) * 100).toInt()
    } else {
        0
    }

    Card(
        colors = CardDefaults.cardColors(containerColor = themeColors.surface),
        shape = RoundedCornerShape(14.dp),
        border = BorderStroke(1.dp, Color(0xFF334155)),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = modifier
            .fillMaxWidth()
            .clickable { onClick() }
    ) {
        Column {
            // 1:1 Square Image Container with Discount Badge & Fallback
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1.0f)
                    .background(Color(0xFF0F172A))
            ) {
                if (product.imageUrl.isNotEmpty()) {
                    SmartAsyncImage(
                        model = product.imageUrl,
                        contentDescription = product.name,
                        modifier = Modifier.fillMaxSize(),
                        fallbackEmoji = "📦"
                    )
                } else {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.ShoppingCart,
                            contentDescription = null,
                            tint = Color.LightGray.copy(alpha = 0.5f),
                            modifier = Modifier.size(36.dp)
                        )
                    }
                }

                // Discount Badge (عرض خاص / تخفيض)
                if (discountVal > 0) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(6.dp)
                            .background(Color(0xFFDC2626), RoundedCornerShape(6.dp))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = "-$discountVal%",
                            color = Color.White,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.ExtraBold
                        )
                    }
                }
            }

            // Info & Price Body
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = product.name,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    fontSize = 12.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                if (product.description.isNotBlank()) {
                    Text(
                        text = product.description,
                        color = Color.LightGray,
                        fontSize = 9.5.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        val displayPrice = product.price.toInt()
                        Text(
                            text = "$displayPrice ر.ي",
                            fontWeight = FontWeight.ExtraBold,
                            color = themeColors.accent,
                            fontSize = 12.sp
                        )
                        if (discountVal > 0) {
                            val originalPrice = if (product.oldPrice > 0) product.oldPrice.toInt() else (displayPrice * 100) / (100 - discountVal).coerceAtLeast(1)
                            Text(
                                text = "$originalPrice ر.ي",
                                color = Color.Gray,
                                fontSize = 8.5.sp,
                                textDecoration = androidx.compose.ui.text.style.TextDecoration.LineThrough
                            )
                        }
                    }

                    // Quick Add Button (+)
                    Surface(
                        shape = CircleShape,
                        color = themeColors.accent,
                        modifier = Modifier.size(28.dp)
                    ) {
                        IconButton(
                            onClick = onAddToCartClick,
                            modifier = Modifier.fillMaxSize()
                        ) {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = "إضافة سريعة",
                                tint = Color.Black,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}
