package com.serviseyem.ui.screens.home.sections

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Handyman
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.serviseyem.data.ProviderEntity
import com.serviseyem.data.helpers.DefaultDemoData
import com.serviseyem.ui.AppScreens
import com.serviseyem.ui.MainViewModel
import com.serviseyem.ui.ProviderCard
import com.serviseyem.ui.components.BespokePremiumSystem
import com.serviseyem.ui.navigateTo
import com.serviseyem.ui.setActiveBrowserTab
import com.serviseyem.utils.VisualThemePalette

data class TechSubCategoryItem(
    val title: String,
    val emoji: String,
    val gradient: Brush
)

/**
 * 🛠️ TechniciansSectionView - قسم الفنيين والحرفيين المعتمدين مع تصفية الأقسام الفرعية وزر العودة المباشر
 */
@Composable
fun TechniciansSectionView(
    viewModel: MainViewModel,
    themeColors: VisualThemePalette,
    onChatOpen: (String) -> Unit
) {
    val rawProviders by viewModel.providers.collectAsState()
    val allProviders = remember(rawProviders) {
        if (rawProviders.isEmpty()) DefaultDemoData.getDemoProviders() else rawProviders
    }

    var selectedSubCat by remember { mutableStateOf("الكل") }

    val subCategories = listOf(
        Pair("الكل", "🛠️"),
        Pair("كهرباء", "⚡"),
        Pair("سباكة", "🔧"),
        Pair("تكييف", "❄️"),
        Pair("صيانة أجهزة", "📱"),
        Pair("نجارة وأثاث", "🪚"),
        Pair("دهانات وديكور", "🎨"),
        Pair("ميكانيك", "🚗"),
        Pair("بناء وترميم", "🧱"),
        Pair("ألمنيوم وحدادة", "🔩")
    )

    val filteredProviders = remember(allProviders, selectedSubCat) {
        if (selectedSubCat == "الكل") {
            allProviders
        } else {
            val query = when (selectedSubCat) {
                "كهرباء" -> listOf("كهرباء", "توليد", "طاقة", "تمديد")
                "سباكة" -> listOf("سباك", "صحي", "مياه", "مضخات")
                "تكييف" -> listOf("تكييف", "تبريد", "مكيف")
                "صيانة أجهزة" -> listOf("أجهز", "جوال", "شبكات", "كمبيوتر", "إلكترون")
                "نجارة وأثاث" -> listOf("نجار", "أثاث", "خشب")
                "دهانات وديكور" -> listOf("دهان", "ديكور", "بوية")
                "ميكانيك" -> listOf("ميكانيك", "سيار", "محرك")
                "بناء وترميم" -> listOf("بناء", "ترميم", "مقاول")
                "ألمنيوم وحدادة" -> listOf("ألمنيوم", "حدادة", "لحام")
                else -> listOf(selectedSubCat)
            }
            allProviders.filter { provider ->
                query.any { q ->
                    provider.profession.contains(q, ignoreCase = true) ||
                    provider.specialization.contains(q, ignoreCase = true) ||
                    provider.categoryId.contains(q, ignoreCase = true) ||
                    provider.customCategoryName.contains(q, ignoreCase = true) ||
                    provider.name.contains(q, ignoreCase = true)
                }
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(BespokePremiumSystem.DeepDark)
            .padding(bottom = 24.dp)
    ) {
        // 0. Top Back Header
        BespokePremiumSystem.SectionBackHeader(
            title = "الفنيون والحرفيون",
            onBackClick = { viewModel.setActiveBrowserTab("الرئيسية") }
        )

        // 1. Bespoke Section Header
        BespokePremiumSystem.SectionHeader(
            title = "الفنيون والخدمات الحرفية",
            subtitle = "نخبة المهنيين المعتمدين والموثقين بضمان الخدمة في اليمن",
            icon = Icons.Default.Build,
            accentColor = BespokePremiumSystem.EmeraldAccent,
            actionLabel = "انضم كفني +",
            onActionClick = {
                viewModel.setTargetRegistrationType("technician")
                viewModel.navigateTo(AppScreens.REGISTER_FORM)
            }
        )

        Spacer(modifier = Modifier.height(10.dp))

        // 2. Sub-Category 2-Column Grid (شبكة عمودين للأقسام الفرعية ببطاقات أيقونية ملونة)
        val subCategoryCards = listOf(
            TechSubCategoryItem("كهرباء", "⚡", Brush.horizontalGradient(listOf(Color(0xFFF59E0B), Color(0xFFD97706)))),
            TechSubCategoryItem("سباكة", "🔧", Brush.horizontalGradient(listOf(Color(0xFF0284C7), Color(0xFF0369A1)))),
            TechSubCategoryItem("تكييف", "❄️", Brush.horizontalGradient(listOf(Color(0xFF06B6D4), Color(0xFF0891B2)))),
            TechSubCategoryItem("نجارة", "🪚", Brush.horizontalGradient(listOf(Color(0xFFD97706), Color(0xFFB45309)))),
            TechSubCategoryItem("صيانة", "📱", Brush.horizontalGradient(listOf(Color(0xFF8B5CF6), Color(0xFF6D28D9)))),
            TechSubCategoryItem("حدادة ونجارة", "🔩", Brush.horizontalGradient(listOf(Color(0xFF475569), Color(0xFF334155)))),
            TechSubCategoryItem("بستنة", "🌱", Brush.horizontalGradient(listOf(Color(0xFF10B981), Color(0xFF059669))))
        )

        // All Filter Pill
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 2.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("اختر التخصص المطلوب:", color = com.serviseyem.ui.theme.TextLightMint, fontSize = 12.sp, fontWeight = FontWeight.Bold)
            Surface(
                onClick = { selectedSubCat = "الكل" },
                shape = RoundedCornerShape(10.dp),
                color = if (selectedSubCat == "الكل") BespokePremiumSystem.EmeraldAccent else BespokePremiumSystem.SurfaceDark,
                border = BorderStroke(1.dp, if (selectedSubCat == "الكل") Color.Transparent else BespokePremiumSystem.GlassBorder)
            ) {
                Text(
                    text = "عرض الكل (${allProviders.size})",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (selectedSubCat == "الكل") com.serviseyem.ui.theme.CardSurface else com.serviseyem.ui.theme.TextLightMint,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(4.dp))

        // 2-Column Grid of subcategories using for loops for clean Composable context
        val chunkedSubCats = subCategoryCards.chunked(2)
        for (pair in chunkedSubCats) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 3.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                for (item in pair) {
                    val isSelected = selectedSubCat == item.title
                    Surface(
                        onClick = { selectedSubCat = if (isSelected) "الكل" else item.title },
                        shape = RoundedCornerShape(16.dp),
                        color = com.serviseyem.ui.theme.CardSurface,
                        border = BorderStroke(if (isSelected) 2.dp else 1.dp, if (isSelected) com.serviseyem.ui.theme.DarkCyanPrimary else com.serviseyem.ui.theme.CardBorder),
                        shadowElevation = if (isSelected) 4.dp else 2.dp,
                        modifier = Modifier
                            .weight(1f)
                            .height(58.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(horizontal = 10.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(38.dp)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(item.gradient),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(item.emoji, fontSize = 18.sp)
                            }
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = item.title,
                                    fontSize = 12.5.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = com.serviseyem.ui.theme.TextLightMint,
                                    maxLines = 1
                                )
                                Text(
                                    text = if (isSelected) "محدد ✓" else "تصفح الفنيين",
                                    fontSize = 9.5.sp,
                                    color = if (isSelected) com.serviseyem.ui.theme.DarkCyanPrimary else com.serviseyem.ui.theme.TextMutedMint
                                )
                            }
                        }
                    }
                }
                if (pair.size == 1) {
                    Spacer(modifier = Modifier.weight(1f))
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // 3. Providers List
        if (filteredProviders.isEmpty()) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 20.dp),
                shape = RoundedCornerShape(16.dp),
                color = BespokePremiumSystem.SurfaceDark,
                border = BorderStroke(1.dp, BespokePremiumSystem.GlassBorder)
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("🛠️", fontSize = 36.sp)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "لا يوجد فنيين مسجلين حالياً في قسم \"$selectedSubCat\"",
                        color = Color.White,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Button(
                        onClick = { selectedSubCat = "الكل" },
                        colors = ButtonDefaults.buttonColors(containerColor = BespokePremiumSystem.EmeraldAccent),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text("عرض جميع الفنيين", color = BespokePremiumSystem.DeepDark, fontWeight = FontWeight.Bold)
                    }
                }
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 6.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                filteredProviders.forEach { provider ->
                    ProviderCard(
                        provider = provider,
                        themeColors = themeColors,
                        viewModel = viewModel,
                        onChatOpen = onChatOpen
                    )
                }
            }
        }
    }
}
