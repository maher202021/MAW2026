package com.serviseyem.ui.screens.home.sections

import androidx.compose.foundation.BorderStroke
import com.serviseyem.ui.*
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.serviseyem.data.CategoryEntity
import com.serviseyem.data.ProviderEntity
import com.serviseyem.data.helpers.DefaultDemoData
import com.serviseyem.ui.MainViewModel
import com.serviseyem.ui.ProviderCard
import com.serviseyem.utils.VisualThemePalette

/**
 * 🔧 ProvidersSectionView - عرض بطائق مقدمي الخدمات والفنيين المعتمدين فوراً بدون أي مربعات فارغة أو سكيلتون
 */
@Composable
fun ProvidersSectionView(
    viewModel: MainViewModel,
    themeColors: VisualThemePalette,
    displayProviders: List<ProviderEntity>,
    isProvidersLoading: Boolean,
    categories: List<CategoryEntity>,
    selectedCategoryId: String?,
    onCategorySelected: (String?) -> Unit,
    providersLimit: Int,
    onLoadMore: () -> Unit,
    onChatOpen: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    // ضمان عدم ظهور أي سكيلتون أو مربعات فارغة أبداً - استخدام البيانات الافتراضية المباشرة عند الفراغ
    val effectiveProviders = if (displayProviders.isEmpty()) {
        DefaultDemoData.getDemoProviders()
    } else {
        displayProviders
    }

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Categories Horizontal Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .padding(vertical = 2.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            val isAll = selectedCategoryId.isNullOrEmpty()
            Surface(
                onClick = { onCategorySelected(null) },
                shape = RoundedCornerShape(16.dp),
                color = if (isAll) Color.White else Color(0xFF131E33),
                shadowElevation = if (isAll) 3.dp else 1.dp,
                border = BorderStroke(1.dp, if (isAll) Color.White else Color(0xFF1F2F4E))
            ) {
                Text(
                    "⭐ كل الأقسام",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (isAll) Color(0xFF0F172A) else Color.White,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp)
                )
            }

            categories.forEach { cat ->
                val isSelected = selectedCategoryId == cat.id
                Surface(
                    onClick = { onCategorySelected(cat.id) },
                    shape = RoundedCornerShape(16.dp),
                    color = if (isSelected) Color.White else Color(0xFF131E33),
                    shadowElevation = if (isSelected) 3.dp else 1.dp,
                    border = BorderStroke(1.dp, if (isSelected) Color.White else Color(0xFF1F2F4E))
                ) {
                    Text(
                        text = (if (cat.icon.isBlank()) {
                            when {
                                cat.name.contains("سباك") -> "🚰"
                                cat.name.contains("كهربا") -> "⚡"
                                cat.name.contains("تكييف") -> "❄️"
                                cat.name.contains("نجار") -> "🪚"
                                cat.name.contains("جهاز") || cat.name.contains("غسالة") -> "🧺"
                                cat.name.contains("طبي") || cat.name.contains("دكتور") -> "🩺"
                                cat.name.contains("صيدلية") -> "💊"
                                cat.name.contains("محام") -> "⚖️"
                                cat.name.contains("هندس") -> "🏗️"
                                cat.name.contains("نظاف") -> "🧹"
                                cat.name.contains("دروس") || cat.name.contains("تعليم") -> "📚"
                                cat.name.contains("نقل") || cat.name.contains("سيارة") -> "🚗"
                                cat.name.contains("عقار") -> "🏠"
                                cat.name.contains("محل") -> "🏪"
                                cat.name.contains("مطعم") -> "🍲"
                                cat.name.contains("تجميل") -> "✂️"
                                cat.name.contains("تقني") -> "💻"
                                else -> "🔧"
                            }
                        } else cat.icon) + " " + cat.name,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isSelected) Color(0xFF0F172A) else Color.White,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp)
                    )
                }
            }
        }

        // Providers List Section - Direct rendered cards (No skeleton placeholders)
        val limitedProviders = effectiveProviders.take(providersLimit)
        limitedProviders.forEach { provider ->
            ProviderCard(
                provider = provider,
                themeColors = themeColors,
                viewModel = viewModel,
                onChatOpen = onChatOpen
            )
        }

        if (effectiveProviders.size > providersLimit) {
            Button(
                onClick = onLoadMore,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E293B)),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.fillMaxWidth().height(42.dp)
            ) {
                Text("عرض المزيد من الفنيين (${effectiveProviders.size - providersLimit} متبقي) ⬇️", color = themeColors.accent, fontSize = 11.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}
