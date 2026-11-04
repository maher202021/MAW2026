package com.serviseyem.ui.screens.home.sections

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.serviseyem.data.StoreEntity
import com.serviseyem.domain.isAdmin
import com.serviseyem.ui.MainViewModel
import com.serviseyem.ui.components.BespokePremiumSystem
import com.serviseyem.ui.setActiveBrowserTab
import com.serviseyem.ui.screens.home.extensions.isRestaurantOrCafe
import com.serviseyem.utils.VisualThemePalette

/**
 * 🍔 RestaurantsSectionView - عرض المطاعم والكافيهات بتصميم عصري فخم وخفيف
 */
@Composable
fun RestaurantsSectionView(
    viewModel: MainViewModel,
    themeColors: VisualThemePalette,
    onStoreClick: (StoreEntity) -> Unit,
    onCreateRestaurantClick: () -> Unit
) {
    val allStores by viewModel.stores.collectAsState()
    val currentUserId by viewModel.currentUserId.collectAsState()
    val adminRole by viewModel.adminRole.collectAsState()
    val isAdminUser = adminRole.isAdmin()

    val restaurantsList = remember(allStores, currentUserId, adminRole) {
        allStores.filter { 
            it.isRestaurantOrCafe() && !it.isDeleted && (it.isApproved || it.ownerId == currentUserId || isAdminUser)
        }
    }
    var selectedSubCat by remember { mutableStateOf<String?>("الكل") }

    val subCategories = listOf(
        Triple("الكل", "🍽️", Color(0xFF10B981)),
        Triple("شعبية", "🍲", Color(0xFFF97316)),
        Triple("سريعة", "🍔", Color(0xFFEF4444)),
        Triple("مشاوي", "🍢", Color(0xFFB91C1C)),
        Triple("كافيه", "☕", Color(0xFF8B4513)),
        Triple("حلويات", "🍰", Color(0xFFEC4899)),
        Triple("بحرية", "🐟", Color(0xFF3B82F6))
    )

    val filteredRestaurants = remember(restaurantsList, selectedSubCat) {
        if (selectedSubCat.isNullOrBlank() || selectedSubCat == "الكل") {
            restaurantsList
        } else {
            val query = selectedSubCat!!
            restaurantsList.filter {
                it.categoryId.contains(query, ignoreCase = true) ||
                it.name.contains(query, ignoreCase = true) ||
                it.description.contains(query, ignoreCase = true)
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(com.serviseyem.ui.theme.AppBackground)
            .padding(bottom = 20.dp)
    ) {
        // 0. Top Back Header
        BespokePremiumSystem.SectionBackHeader(
            title = "المطاعم والكافيهات",
            onBackClick = { viewModel.setActiveBrowserTab("الرئيسية") }
        )

        // 1. Foodie Section Header
        BespokePremiumSystem.SectionHeader(
            title = "المطاعم والكافيهات",
            subtitle = "اكتشف أشهى المأكولات والوجبات في مدينتك",
            icon = Icons.Default.Restaurant,
            accentColor = Color(0xFFE11D48),
            actionLabel = "تسجيل مطعمك",
            onActionClick = onCreateRestaurantClick
        )

        // 2. Top Hero Promo Banner (Arby's Style - Screenshot 1)
        Surface(
            modifier = Modifier.fillMaxWidth().height(160.dp).padding(horizontal = 8.dp),
            shape = RoundedCornerShape(20.dp),
            color = Color(0xFF121A2C),
            border = BorderStroke(1.dp, Color(0xFFE11D48).copy(alpha = 0.5f)),
            shadowElevation = 4.dp
        ) {
            Box(modifier = Modifier.fillMaxSize()) {
                com.serviseyem.ui.components.SmartAsyncImage(
                    model = "https://images.unsplash.com/photo-1568901346375-23c9450c58cd?auto=format&fit=crop&w=800&q=80",
                    contentDescription = "عرض المطاعم",
                    contentScale = androidx.compose.ui.layout.ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            androidx.compose.ui.graphics.Brush.horizontalGradient(
                                listOf(Color(0xE6E11D48), Color(0xCC881337), Color(0x66070A13))
                            )
                        )
                )
                Column(modifier = Modifier.align(Alignment.CenterStart).padding(16.dp)) {
                    Surface(shape = RoundedCornerShape(8.dp), color = Color(0xFFE8B23A)) {
                        Text("عروض خاصة 🔥", color = Color(0xFF070A13), fontSize = 10.sp, fontWeight = FontWeight.Black, modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp))
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Text("أشهى المأكولات والوجبات السريعة", color = Color.White, fontSize = 16.5.sp, fontWeight = FontWeight.Black)
                    Text("خصومات حصرية تصل حتى 25% مع توصيل سريع", color = Color(0xFFCBD5E1), fontSize = 11.sp)
                    Spacer(modifier = Modifier.height(10.dp))
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = Color.White,
                        shadowElevation = 2.dp
                    ) {
                        Text("اطلب الآن ➔", color = Color(0xFFE11D48), fontSize = 11.sp, fontWeight = FontWeight.Black, modifier = Modifier.padding(horizontal = 12.dp, vertical = 5.dp))
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // 3. Illustrated Category Selector (Working Filter)
        Row(
            modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()).padding(horizontal = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            subCategories.forEach { (label, emoji, glowColor) ->
                val isSelected = (selectedSubCat == label) || (selectedSubCat == null && label == "الكل")
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.clickable {
                        selectedSubCat = if (label == "الكل") "الكل" else label
                    }
                ) {
                    Surface(
                        shape = CircleShape,
                        color = if (isSelected) Color(0xFFE11D48).copy(alpha = 0.25f) else Color(0xFF121A2C),
                        border = BorderStroke(if (isSelected) 2.dp else 1.dp, if (isSelected) Color(0xFFE11D48) else Color(0xFF1E293B)),
                        shadowElevation = if (isSelected) 4.dp else 2.dp,
                        modifier = Modifier.size(58.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(emoji, fontSize = 24.sp)
                        }
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        label,
                        color = if (isSelected) glowColor else Color.White,
                        fontSize = 11.5.sp,
                        fontWeight = if (isSelected) FontWeight.Black else FontWeight.Bold
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // 3. Popular Picks Label
        Text(
            if (selectedSubCat == "الكل" || selectedSubCat == null) "الأكثر طلباً حالياً 🔥" else "نتائج قسم $selectedSubCat",
            color = com.serviseyem.ui.components.BespokePremiumSystem.CardDarkTitle,
            fontSize = 15.sp,
            fontWeight = FontWeight.ExtraBold,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
        )

        // 4. Restaurant Cards
        filteredRestaurants.forEach { rest ->
            BespokePremiumSystem.FoodieRestaurantCard(
                title = rest.name,
                imageUrl = rest.logoImage.ifBlank { rest.coverImage },
                deliveryTime = "30-45 دقيقة",
                price = "تبدأ من 1500 ريال",
                onClick = { onStoreClick(rest) },
                onAddClick = { onStoreClick(rest) }
            )
        }

        if (filteredRestaurants.isEmpty()) {
            Box(modifier = Modifier.fillMaxWidth().padding(40.dp), contentAlignment = Alignment.Center) {
                Text(
                    if (selectedSubCat == "الكل" || selectedSubCat == null) "لا توجد مطاعم مسجلة حالياً" else "لا توجد مطاعم في قسم $selectedSubCat",
                    color = Color.Gray,
                    fontSize = 14.sp
                )
            }
        }
    }
}

