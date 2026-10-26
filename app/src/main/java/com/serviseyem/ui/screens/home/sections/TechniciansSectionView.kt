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

        // 2. Sub-Category Quick Links
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .padding(horizontal = 6.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            subCategories.forEach { (catName, emoji) ->
                val isSelected = selectedSubCat == catName
                Surface(
                    onClick = { selectedSubCat = catName },
                    shape = RoundedCornerShape(12.dp),
                    color = if (isSelected) BespokePremiumSystem.EmeraldAccent else BespokePremiumSystem.SurfaceDark,
                    border = BorderStroke(1.dp, if (isSelected) Color.Transparent else BespokePremiumSystem.GlassBorder)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(emoji, fontSize = 13.sp)
                        Text(
                            text = catName,
                            fontSize = 11.5.sp,
                            fontWeight = if (isSelected) FontWeight.Black else FontWeight.SemiBold,
                            color = if (isSelected) BespokePremiumSystem.DeepDark else Color.White
                        )
                    }
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
