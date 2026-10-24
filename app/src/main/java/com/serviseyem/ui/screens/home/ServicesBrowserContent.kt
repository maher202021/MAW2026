package com.serviseyem.ui.screens.home

import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.serviseyem.data.*
import com.serviseyem.ui.*
import com.serviseyem.ui.screens.home.sections.ProvidersSectionView
import com.serviseyem.utils.VisualThemePalette

/**
 * 📱 ServicesBrowserMainContent - عرض بطائق مقدمي الخدمات فقط دون أي إضافات أسفلها
 */
@Composable
fun ServicesBrowserMainContent(
    viewModel: MainViewModel,
    themeColors: VisualThemePalette,
    displayProviders: List<ProviderEntity>,
    displayStores: List<StoreEntity> = emptyList(),
    displayProperties: List<PropertyEntity> = emptyList(),
    isProvidersLoading: Boolean,
    categories: List<CategoryEntity>,
    selectedCategoryId: String?,
    onCategorySelected: (String?) -> Unit,
    providersLimit: Int,
    onLoadMore: () -> Unit,
    onStoreClick: (StoreEntity) -> Unit,
    onPropertyClick: (PropertyEntity) -> Unit,
    onChatOpen: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // عرض بطائق مقدمي الخدمات والفنيين فقط دون أي أقسام أخرى أسفلها
        ProvidersSectionView(
            viewModel = viewModel,
            themeColors = themeColors,
            displayProviders = displayProviders,
            isProvidersLoading = isProvidersLoading,
            categories = categories,
            selectedCategoryId = selectedCategoryId,
            onCategorySelected = onCategorySelected,
            providersLimit = providersLimit,
            onLoadMore = onLoadMore,
            onChatOpen = onChatOpen,
            modifier = Modifier.fillMaxWidth()
        )
    }
}
