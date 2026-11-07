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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.serviseyem.data.PropertyEntity
import com.serviseyem.domain.isAdmin
import com.serviseyem.ui.MainViewModel
import com.serviseyem.ui.components.BespokePremiumSystem
import com.serviseyem.ui.setActiveBrowserTab
import com.serviseyem.utils.VisualThemePalette
import com.serviseyem.utils.formatPriceWithCurrency

/**
 * 🏢 PropertiesSectionView - عرض العقارات والاستثمارات بتصميم عصري فخم وخفيف
 */
@Composable
fun PropertiesSectionView(
    viewModel: MainViewModel,
    themeColors: VisualThemePalette,
    onPropertyClick: (PropertyEntity) -> Unit,
    onCreatePropertyClick: () -> Unit
) {
    val allProperties by viewModel.properties.collectAsState()
    val currentUserId by viewModel.currentUserId.collectAsState()
    val adminRole by viewModel.adminRole.collectAsState()
    val isAdminUser = adminRole.isAdmin()

    val propertiesList = remember(allProperties, currentUserId, adminRole) {
        allProperties.filter { 
            !it.isDeleted && (it.isApproved || it.ownerId == currentUserId || isAdminUser)
        }
    }
    var selectedSubCategory by remember { mutableStateOf("الكل") }

    val subCategories = listOf("الكل", "إيجار", "بيع", "شقق", "فلل", "أراضي", "محلات")

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(com.serviseyem.ui.theme.AppBackground)
            .padding(bottom = 20.dp)
    ) {
        // 0. Top Back Header
        BespokePremiumSystem.SectionBackHeader(
            title = "العقارات والاستثمارات",
            onBackClick = { viewModel.setActiveBrowserTab("الرئيسية") }
        )

        // 1. Luxury Estate Section Header
        BespokePremiumSystem.SectionHeader(
            title = "العقارات والاستثمارات",
            subtitle = "اكتشف أفخم العقارات والأراضي في اليمن",
            icon = Icons.Default.Apartment,
            accentColor = Color(0xFF4F46E5),
            actionLabel = "أضف عقارك",
            onActionClick = onCreatePropertyClick
        )

        // 2. Elegant Smart Filters (Horizontal Pills)
        Row(
            modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()).padding(horizontal = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            subCategories.forEach { cat ->
                val isSelected = selectedSubCategory == cat
                Surface(
                    onClick = { selectedSubCategory = cat },
                    shape = RoundedCornerShape(20.dp),
                    color = if (isSelected) Color(0xFF4F46E5) else Color(0xFF1E293B),
                    border = BorderStroke(1.dp, if (isSelected) Color.Transparent else Color(0xFF334155)),
                    shadowElevation = if (isSelected) 3.dp else 1.dp
                ) {
                    Text(
                        cat,
                        color = if (isSelected) Color.White else Color(0xFFCBD5E1),
                        fontSize = 11.5.sp,
                        fontWeight = if (isSelected) FontWeight.ExtraBold else FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 18.dp, vertical = 8.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // 3. Luxury Property Cards
        propertiesList.forEach { prop ->
            BespokePremiumSystem.EstateLuxuryCard(
                title = prop.title,
                price = formatPriceWithCurrency(prop.price, prop.currency),
                location = "${prop.cityId} - ${prop.localNeighborhood}",
                rooms = prop.rooms,
                area = "${prop.areaSqm.toInt()} م²",
                imageUrl = prop.images.firstOrNull() ?: "https://images.unsplash.com/photo-1582407947304-fd86f028f716?auto=format&fit=crop&w=800&q=80",
                onBookmark = { /* Bookmark Logic */ },
                onShare = { /* Share Logic */ },
                onClick = { onPropertyClick(prop) }
            )
        }

        if (propertiesList.isEmpty()) {
            Box(modifier = Modifier.fillMaxWidth().padding(40.dp), contentAlignment = Alignment.Center) {
                Text("لا توجد عقارات معروضة حالياً", color = Color.Gray, fontSize = 14.sp)
            }
        }
    }
}

