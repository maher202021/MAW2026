package com.serviseyem.ui.screens.home.sections

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.serviseyem.data.StoreEntity
import com.serviseyem.domain.isAdmin
import com.serviseyem.ui.MainViewModel
import com.serviseyem.ui.components.BespokePremiumSystem
import com.serviseyem.ui.setActiveBrowserTab
import com.serviseyem.ui.screens.home.extensions.isCommercialStore
import com.serviseyem.utils.VisualThemePalette

/**
 * 🛍️ StoresSectionView - عرض المحلات والمتاجر التجارية بتصميم عصري فخم وخفيف
 */
@Composable
fun StoresSectionView(
    viewModel: MainViewModel,
    themeColors: VisualThemePalette,
    onStoreClick: (StoreEntity) -> Unit,
    onCreateStoreClick: () -> Unit
) {
    val allStores by viewModel.stores.collectAsState()
    val currentUserId by viewModel.currentUserId.collectAsState()
    val adminRole by viewModel.adminRole.collectAsState()
    val isAdminUser = adminRole.isAdmin()

    val commercialStores = remember(allStores, currentUserId, adminRole) {
        allStores.filter { 
            it.isCommercialStore() && !it.isDeleted && (it.isApproved || it.ownerId == currentUserId || isAdminUser)
        }
    }
    var selectedSubCat by remember { mutableStateOf<String?>("الكل") }

    val subCategories = listOf(
        Pair("الكل", "🏷️"),
        Pair("سوبرماركت", "🛒"),
        Pair("ملابس", "👗"),
        Pair("إلكترونيات", "📱"),
        Pair("منزلية", "🏠"),
        Pair("عطور", "✨"),
        Pair("ألعاب", "🎮")
    )

    val filteredStores = remember(commercialStores, selectedSubCat) {
        if (selectedSubCat.isNullOrBlank() || selectedSubCat == "الكل") {
            commercialStores
        } else {
            val query = selectedSubCat!!
            commercialStores.filter {
                it.categoryId.contains(query, ignoreCase = true) ||
                it.name.contains(query, ignoreCase = true) ||
                it.description.contains(query, ignoreCase = true)
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(BespokePremiumSystem.DeepDark)
            .padding(bottom = 20.dp)
    ) {
        // 0. Top Back Header
        BespokePremiumSystem.SectionBackHeader(
            title = "المحلات والمتاجر",
            onBackClick = { viewModel.setActiveBrowserTab("الرئيسية") }
        )

        // 1. TechHub Section Header
        BespokePremiumSystem.SectionHeader(
            title = "المتاجر والمعارض",
            subtitle = "تسوق من أفضل العلامات التجارية في اليمن",
            icon = Icons.Default.Storefront,
            accentColor = BespokePremiumSystem.EmeraldAccent,
            actionLabel = "إضافة متجر +",
            onActionClick = onCreateStoreClick
        )

        // 2. Promotional Carousel (Simulated with a prominent Box)
        Surface(
            modifier = Modifier.fillMaxWidth().height(160.dp).padding(horizontal = 8.dp),
            shape = RoundedCornerShape(24.dp),
            color = Color(0xFF1E293B),
            border = BorderStroke(1.dp, BespokePremiumSystem.GlassBorder)
        ) {
            Box(modifier = Modifier.fillMaxSize()) {
                AsyncImage(
                    model = "https://images.unsplash.com/photo-1607082348824-0a96f2a4b9da?auto=format&fit=crop&w=800&q=80",
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize().alpha(0.6f)
                )
                Column(modifier = Modifier.align(Alignment.BottomStart).padding(20.dp)) {
                    Surface(shape = RoundedCornerShape(8.dp), color = BespokePremiumSystem.EmeraldAccent) {
                        Text("خصم 30%", color = BespokePremiumSystem.DeepDark, fontSize = 10.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp))
                    }
                    Text("عروض الشتاء الكبرى", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.ExtraBold)
                    Text("على كافة الأجهزة الإلكترونية", color = Color.LightGray, fontSize = 12.sp)
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // 3. Category Micro-Pills (Working Filter)
        Row(
            modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()).padding(horizontal = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            subCategories.forEach { (label, emoji) ->
                val isSelected = (selectedSubCat == label) || (selectedSubCat == null && label == "الكل")
                Surface(
                    onClick = {
                        selectedSubCat = if (label == "الكل") "الكل" else label
                    },
                    shape = RoundedCornerShape(12.dp),
                    color = if (isSelected) BespokePremiumSystem.EmeraldAccent else Color.White.copy(alpha = 0.05f),
                    border = BorderStroke(1.dp, if (isSelected) Color.Transparent else BespokePremiumSystem.GlassBorder)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(emoji, fontSize = 14.sp)
                        Text(
                            label,
                            color = if (isSelected) BespokePremiumSystem.DeepDark else Color.White,
                            fontSize = 12.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // 4. Stores Grid (Simulated with Rows for Nested Scroll safety)
        val chunkedStores = filteredStores.chunked(2)
        chunkedStores.forEach { pair ->
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                pair.forEach { store ->
                    Box(modifier = Modifier.weight(1f)) {
                        BespokePremiumSystem.TechHubStoreCard(
                            title = store.name,
                            imageUrl = store.logoImage.ifBlank { store.coverImage },
                            rating = store.rating,
                            priceRange = "$$$",
                            onCartClick = { onStoreClick(store) },
                            onClick = { onStoreClick(store) }
                        )
                    }
                }
                if (pair.size == 1) Spacer(modifier = Modifier.weight(1f))
            }
        }

        if (filteredStores.isEmpty()) {
            Box(modifier = Modifier.fillMaxWidth().padding(40.dp), contentAlignment = Alignment.Center) {
                Text(
                    if (selectedSubCat == "الكل" || selectedSubCat == null) "لا توجد متاجر متاحة حالياً" else "لا توجد متاجر في قسم $selectedSubCat",
                    color = Color.Gray,
                    fontSize = 14.sp
                )
            }
        }

        if (commercialStores.isEmpty()) {
            Box(modifier = Modifier.fillMaxWidth().padding(40.dp), contentAlignment = Alignment.Center) {
                Text("لا توجد متاجر متاحة حالياً", color = Color.Gray, fontSize = 14.sp)
            }
        }
    }
}

