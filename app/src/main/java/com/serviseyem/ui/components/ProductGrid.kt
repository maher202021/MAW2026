package com.serviseyem.ui

import androidx.compose.foundation.layout.*
import com.serviseyem.ui.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.serviseyem.data.*
import com.serviseyem.utils.VisualThemePalette

/**
 * 📊 Product Grid Component
 */
@Composable
fun ProductGrid(
    products: List<ProductEntity>,
    isOwnerOrAdmin: Boolean,
    themeColors: VisualThemePalette,
    viewModel: MainViewModel,
    onProductOrderClick: (ProductEntity) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        products.forEach { product ->
            ProductListItemCard(
                product = product,
                isOwnerOrAdmin = isOwnerOrAdmin,
                themeColors = themeColors,
                viewModel = viewModel,
                onOrderClick = { onProductOrderClick(product) }
            )
        }
    }
}
