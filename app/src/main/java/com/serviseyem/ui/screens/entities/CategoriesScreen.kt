package com.serviseyem.ui.screens.entities

import androidx.compose.foundation.BorderStroke
import com.serviseyem.ui.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import kotlinx.coroutines.flow.first
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.serviseyem.data.CategoryEntity
import com.serviseyem.data.repositories.CategoryRepository
import com.serviseyem.ui.MainViewModel
import com.serviseyem.ui.components.SkeletonCard
import com.serviseyem.utils.VisualThemePalette

@Composable
fun CategoriesScreen(
    viewModel: MainViewModel,
    themeColors: VisualThemePalette,
    categoryRepository: CategoryRepository = remember { CategoryRepository(viewModel.db) },
    onCategoryClick: (String) -> Unit
) {
    val vmCategories by viewModel.categories.collectAsState()
    var categories by remember { mutableStateOf<List<CategoryEntity>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var retryCount by remember { mutableStateOf(0) }
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(retryCount, vmCategories) {
        if (vmCategories.isNotEmpty()) {
            categories = vmCategories
            isLoading = false
            errorMessage = null
        } else {
            isLoading = true
            errorMessage = null
            try {
                val result = categoryRepository.observeCategories().first()
                isLoading = false
                result.onSuccess { fetched ->
                    categories = fetched.ifEmpty { vmCategories }
                    errorMessage = null
                }.onFailure { error ->
                    if (vmCategories.isNotEmpty()) {
                        categories = vmCategories
                        errorMessage = null
                    } else {
                        errorMessage = "فشل تحميل الفئات: ${error.localizedMessage ?: "خطأ غير معروف"}"
                    }
                }
            } catch (e: Exception) {
                isLoading = false
                categories = vmCategories
            }
        }
    }

    if (errorMessage != null) {
        LaunchedEffect(errorMessage) {
            val result = snackbarHostState.showSnackbar(
                message = errorMessage ?: "حدث خطأ غير متوقع",
                actionLabel = "إعادة المحاولة",
                duration = SnackbarDuration.Indefinite
            )
            if (result == SnackbarResult.ActionPerformed) {
                retryCount++
            }
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = themeColors.background,
        modifier = Modifier.fillMaxSize()
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "🗂️ فئات دليل الخدمات اليمني المتاحة:",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = themeColors.accent
                )
            }

            if (isLoading) {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                ) {
                    items(6) {
                        SkeletonCard(height = 90.dp)
                    }
                }
            } else if (errorMessage != null && categories.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Text(
                            text = errorMessage ?: "حدث خطأ أثناء تحميل الفئات",
                            color = Color(0xFFD32F2F),
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(horizontal = 20.dp)
                        )
                        Button(
                            onClick = { retryCount++ },
                            colors = ButtonDefaults.buttonColors(containerColor = themeColors.accent)
                        ) {
                            Text("إعادة المحاولة 🔄", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }
                    }
                }
            } else if (categories.isEmpty() && errorMessage == null) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "لا توجد فئات متاحة حالياً.",
                        color = themeColors.textSecondary,
                        fontSize = 14.sp,
                        textAlign = TextAlign.Center
                    )
                }
            } else {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .testTag("categories_grid")
                ) {
                    items(categories, key = { it.id }) { cat ->
                        DynamicCategoryCard(
                            category = cat,
                            themeColors = themeColors,
                            onClick = { onCategoryClick(cat.id) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun DynamicCategoryCard(
    category: CategoryEntity,
    themeColors: VisualThemePalette,
    onClick: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = themeColors.surface),
        border = BorderStroke(1.dp, themeColors.accent.copy(alpha = 0.3f)),
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .testTag("category_card_${category.id}")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Surface(
                color = themeColors.accent.copy(alpha = 0.15f),
                shape = RoundedCornerShape(20.dp)
            ) {
                Text(
                    text = category.icon.ifBlank { "✨" },
                    fontSize = 28.sp,
                    modifier = Modifier.padding(10.dp)
                )
            }

            Text(
                text = category.name,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = themeColors.textPrimary,
                textAlign = TextAlign.Center,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}
