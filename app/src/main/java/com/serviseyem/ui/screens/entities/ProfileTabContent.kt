package com.serviseyem.ui.screens.entities

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.border
import androidx.compose.ui.draw.clip
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.mutableStateOf
import com.serviseyem.ui.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.text.style.TextAlign
import com.serviseyem.data.*
import com.serviseyem.ui.components.SmartAsyncImage
import com.serviseyem.utils.VisualThemePalette

@Composable
fun EmptyStateBox(msg: String, themeColors: VisualThemePalette) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 16.dp)
            .background(Color.Black.copy(alpha = 0.15f), RoundedCornerShape(12.dp))
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(text = msg, color = Color.Gray, fontSize = 12.sp, textAlign = TextAlign.Center)
    }
}

@Composable
fun ProfileTabContent(
    selectedTab: Int,
    entityType: ProfileEntityType,
    provider: ProviderEntity?,
    store: StoreEntity?,
    property: PropertyEntity?,
    job: JobEntity?,
    products: List<ProductEntity>,
    entityReviews: List<RatingEntity>,
    entityDescription: String,
    themeColors: VisualThemePalette,
    isOwner: Boolean = false,
    onAddReviewClick: () -> Unit
) {
    when (selectedTab) {
        0 -> {
            // First Tab (Photos / Products / Menu / Listings)
            when (entityType) {
                ProfileEntityType.TECHNICIAN -> {
                    val photos = provider?.workPhotosBase64 ?: emptyList()
                    if (photos.isEmpty()) {
                        EmptyStateBox("لا توجد صور سابقة أعمال مرفوعة حالياً.", themeColors)
                    } else {
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            items(photos) { photo ->
                                Card(
                                    modifier = Modifier.size(120.dp),
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    SmartAsyncImage(model = photo, contentDescription = "عمل سابق", modifier = Modifier.fillMaxSize())
                                }
                            }
                        }
                    }
                }
                ProfileEntityType.STORE, ProfileEntityType.RESTAURANT, ProfileEntityType.MEDICAL -> {
                    val storeProducts = products.filter { it.storeId == (store?.id ?: "") }
                    var selectedSubCategory by remember { mutableStateOf("الكل") }

                    val subCats = remember(storeProducts) {
                        val list = mutableListOf("الكل")
                        storeProducts.forEach { p ->
                            if (p.category.isNotBlank() && !list.contains(p.category)) {
                                list.add(p.category)
                            }
                        }
                        if (list.size == 1 && (store?.id == "store_demo_alyusri" || store?.name?.contains("بهارات") == true)) {
                            listOf("الكل", "توابل مشكلة", "بهارات اللحوم", "بهارات شاي", "خلطات خاصة")
                        } else {
                            list
                        }
                    }

                    val filteredProducts = remember(storeProducts, selectedSubCategory) {
                        if (selectedSubCategory == "الكل") storeProducts
                        else storeProducts.filter { it.category == selectedSubCategory }
                    }

                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        // Header: الأقسام والتصنيفات
                        Text(
                            text = "الأقسام والتصنيفات",
                            fontSize = 13.5.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            modifier = Modifier.padding(horizontal = 4.dp)
                        )

                        // Circular Categories Row (Screenshot 2 Architecture)
                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(14.dp),
                            contentPadding = PaddingValues(horizontal = 4.dp)
                        ) {
                            items(subCats) { catName ->
                                val isSelected = selectedSubCategory == catName
                                val catEmoji = when {
                                    catName == "الكل" -> "🏪"
                                    catName.contains("مشكل") -> "🌶️"
                                    catName.contains("لحوم") -> "🥩"
                                    catName.contains("شاي") -> "☕"
                                    catName.contains("خاص") -> "✨"
                                    catName.contains("دجاج") -> "🍗"
                                    catName.contains("عصير") -> "🍹"
                                    catName.contains("حلى") -> "🍰"
                                    catName.contains("أسنان") -> "🦷"
                                    catName.contains("أطفال") -> "👶"
                                    else -> "📦"
                                }

                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    modifier = Modifier
                                        .clickable { selectedSubCategory = catName }
                                        .padding(vertical = 2.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(54.dp)
                                            .clip(androidx.compose.foundation.shape.CircleShape)
                                            .background(if (isSelected) Color(0xFF2563EB) else Color(0xFF1E2D4A))
                                            .border(
                                                width = if (isSelected) 2.dp else 1.dp,
                                                color = if (isSelected) Color(0xFF60A5FA) else Color(0xFF334155),
                                                shape = androidx.compose.foundation.shape.CircleShape
                                            ),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(text = catEmoji, fontSize = 22.sp)
                                    }
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text(
                                        text = catName,
                                        fontSize = 10.5.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                        color = if (isSelected) Color(0xFF60A5FA) else Color(0xFF94A3B8)
                                    )
                                }
                            }
                        }

                        // 2-Column Product Grid
                        if (filteredProducts.isEmpty()) {
                            EmptyStateBox("لا توجد أصناف في هذا القسم حالياً.", themeColors)
                        } else {
                            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                filteredProducts.chunked(2).forEach { rowPair ->
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                                    ) {
                                        rowPair.forEach { prod ->
                                            Box(modifier = Modifier.weight(1f)) {
                                                ProfileProductCard(product = prod, themeColors = themeColors)
                                            }
                                        }
                                        if (rowPair.size == 1) {
                                            Spacer(modifier = Modifier.weight(1f))
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
                ProfileEntityType.REAL_ESTATE -> {
                    val images = property?.images ?: emptyList()
                    if (images.isEmpty()) {
                        EmptyStateBox("لا توجد صور إضافية للعقار.", themeColors)
                    } else {
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            items(images) { img ->
                                Card(
                                    modifier = Modifier.size(140.dp),
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    SmartAsyncImage(model = img, contentDescription = "صورة العقار", modifier = Modifier.fillMaxSize())
                                }
                            }
                        }
                    }
                }
                else -> {
                    Text(
                        text = entityDescription,
                        color = Color.White,
                        fontSize = 13.sp,
                        modifier = Modifier.padding(8.dp)
                    )
                }
            }
        }
        1 -> {
            // Reviews / Secondary info
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("التقييمات وآراء العملاء ⭐", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = themeColors.accent)
                    if (!isOwner) {
                        Button(
                            onClick = onAddReviewClick,
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFA000)),
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                        ) {
                            Text("أضف تقييمك ⭐", fontSize = 11.sp, color = Color.Black, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                if (entityReviews.isEmpty()) {
                    EmptyStateBox("لا توجد تقييمات مسجلة بعد. كن أول من يكتب تقييماً!", themeColors)
                } else {
                    entityReviews.forEach { review ->
                        ProfileReviewCard(review = review, themeColors = themeColors)
                    }
                }
            }
        }
        2 -> {
            // Warranty / Working Hours / Policy
            Card(
                colors = CardDefaults.cardColors(containerColor = themeColors.surface),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("📜 الشروط والضمان المعتمد بالمنصة", fontWeight = FontWeight.Bold, color = themeColors.accent, fontSize = 13.sp)
                    Text("• جميع التعاملات تخضع لميثاق الجودة والحماية في دليل خدمات اليمن.", color = Color.White, fontSize = 11.sp)
                    Text("• إمكانية استرجاع الرسوم أو رفع شكوى مباشرة لإدارة المنصة في حال الإخلال بالمواصفات.", color = Color.LightGray, fontSize = 11.sp)
                    Text("• الدفع المباشر عبر المحافظ الإلكترونية المعتمدة (الكريمي، جيب، جوالي، ون كاش).", color = Color.LightGray, fontSize = 11.sp)
                }
            }
        }
    }
}
