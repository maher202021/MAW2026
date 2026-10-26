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
        // Categories Horizontal Bar REMOVED for Minimalism
        // Surface(...) { ... }

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
