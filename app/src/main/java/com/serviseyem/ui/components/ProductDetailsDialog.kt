package com.serviseyem.ui

import androidx.compose.foundation.Image
import com.serviseyem.ui.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
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
import com.serviseyem.data.*
import com.serviseyem.utils.VisualThemePalette
import androidx.compose.ui.graphics.ImageBitmap
import com.serviseyem.rememberBase64Bitmap

/**
 * ℹ️ Product Details Dialog Component
 */
@Composable
fun ProductDetailsDialog(
    product: ProductEntity,
    themeColors: VisualThemePalette,
    onDismiss: () -> Unit,
    onOrderClick: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            colors = CardDefaults.cardColors(containerColor = themeColors.background),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "تفاصيل المنتج 📦",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = themeColors.accent
                )

                // Product Image
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color.DarkGray),
                    contentAlignment = Alignment.Center
                ) {
                    com.serviseyem.ui.components.SmartAsyncImage(
                        model = product.imageUrl,
                        contentDescription = product.name,
                        modifier = Modifier.fillMaxSize(),
                        fallbackEmoji = "📦"
                    )
                }

                Text(
                    text = product.name,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )

                if (product.description.isNotEmpty()) {
                    Text(
                        text = product.description,
                        fontSize = 11.sp,
                        color = themeColors.textSecondary,
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "السعر:",
                        fontSize = 12.sp,
                        color = Color.LightGray
                    )
                    Text(
                        text = "${product.price.toInt()} YER",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = themeColors.accent
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    TextButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("إغلاق", color = Color.Gray)
                    }
                    if (product.isAvailable) {
                        Button(
                            onClick = {
                                onDismiss()
                                onOrderClick()
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = themeColors.accent),
                            modifier = Modifier.weight(1.5f)
                        ) {
                            Text("طلب شراء 🛒", color = Color.Black, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}
