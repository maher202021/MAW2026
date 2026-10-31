package com.serviseyem.ui.screens.home

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.serviseyem.data.*
import com.serviseyem.domain.entities.ProductItemEntity
import com.serviseyem.ui.*
import com.serviseyem.utils.AnalyticsEventsHelper
import com.serviseyem.utils.VisualThemePalette
import kotlinx.coroutines.delay

@Composable
fun UnifiedGlobalSearchScreen(
    viewModel: MainViewModel,
    themeColors: VisualThemePalette,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var searchQuery by remember { mutableStateOf("") }
    var debouncedQuery by remember { mutableStateOf("") }

    // Read real data sources from MainViewModel
    val providers by viewModel.providers.collectAsState()
    val stores by viewModel.stores.collectAsState()
    val properties by viewModel.properties.collectAsState()
    val jobs by viewModel.jobs.collectAsState()
    val products by viewModel.products.collectAsState()

    // Server-side search results
    var serverProviders by remember { mutableStateOf<List<ProviderEntity>>(emptyList()) }
    var serverStores by remember { mutableStateOf<List<StoreEntity>>(emptyList()) }
    var serverProperties by remember { mutableStateOf<List<PropertyEntity>>(emptyList()) }
    var serverJobs by remember { mutableStateOf<List<JobEntity>>(emptyList()) }
    var isSearchingServer by remember { mutableStateOf(false) }

    // Simple debounce logic (400ms)
    LaunchedEffect(searchQuery) {
        delay(400)
        debouncedQuery = searchQuery.trim()
        if (debouncedQuery.length >= 2) {
            AnalyticsEventsHelper.logSearchPerformed(context, debouncedQuery)
            
            // Trigger Server Search
            isSearchingServer = true
            val searchResults = viewModel.searchAll(debouncedQuery)
            serverProviders = searchResults["providers"]?.filterIsInstance<ProviderEntity>() ?: emptyList()
            serverStores = searchResults["stores"]?.filterIsInstance<StoreEntity>() ?: emptyList()
            serverProperties = searchResults["properties"]?.filterIsInstance<PropertyEntity>() ?: emptyList()
            serverJobs = searchResults["jobs"]?.filterIsInstance<JobEntity>() ?: emptyList()
            isSearchingServer = false
        } else {
            serverProviders = emptyList()
            serverStores = emptyList()
            serverProperties = emptyList()
            serverJobs = emptyList()
        }
    }

    // Filtered results based on debounced query (local filtering + server results)
    val filteredProviders = remember(debouncedQuery, providers, serverProviders) {
        if (debouncedQuery.isBlank()) emptyList() else {
            val local = providers.filter {
                it.name.contains(debouncedQuery, ignoreCase = true) ||
                it.profession.contains(debouncedQuery, ignoreCase = true) ||
                it.specialization.contains(debouncedQuery, ignoreCase = true) ||
                it.phone.contains(debouncedQuery)
            }
            (local + serverProviders).distinctBy { it.id }.take(30)
        }
    }

    val filteredStores = remember(debouncedQuery, stores, serverStores) {
        if (debouncedQuery.isBlank()) emptyList() else {
            val local = stores.filter {
                it.name.contains(debouncedQuery, ignoreCase = true) ||
                it.description.contains(debouncedQuery, ignoreCase = true) ||
                it.localNeighborhood.contains(debouncedQuery, ignoreCase = true) ||
                it.phone.contains(debouncedQuery)
            }
            (local + serverStores).distinctBy { it.id }.take(30)
        }
    }

    val filteredProperties = remember(debouncedQuery, properties, serverProperties) {
        if (debouncedQuery.isBlank()) emptyList() else {
            val local = properties.filter {
                it.title.contains(debouncedQuery, ignoreCase = true) ||
                it.description.contains(debouncedQuery, ignoreCase = true) ||
                it.localNeighborhood.contains(debouncedQuery, ignoreCase = true) ||
                it.propertyType.contains(debouncedQuery, ignoreCase = true)
            }
            (local + serverProperties).distinctBy { it.id }.take(30)
        }
    }

    val filteredJobs = remember(debouncedQuery, jobs, serverJobs) {
        if (debouncedQuery.isBlank()) emptyList() else {
            val local = jobs.filter {
                it.title.contains(debouncedQuery, ignoreCase = true) ||
                it.description.contains(debouncedQuery, ignoreCase = true) ||
                it.companyName.contains(debouncedQuery, ignoreCase = true) ||
                it.requirements.contains(debouncedQuery, ignoreCase = true)
            }
            (local + serverJobs).distinctBy { it.id }.take(30)
        }
    }

    val filteredProducts = remember(debouncedQuery, products) {
        if (debouncedQuery.isBlank()) emptyList() else {
            products.filter {
                it.name.contains(debouncedQuery, ignoreCase = true) ||
                it.description.contains(debouncedQuery, ignoreCase = true) ||
                it.category.contains(debouncedQuery, ignoreCase = true)
            }.take(30)
        }
    }

    val hasResults = filteredProviders.isNotEmpty() || filteredStores.isNotEmpty() ||
            filteredProperties.isNotEmpty() || filteredJobs.isNotEmpty() || filteredProducts.isNotEmpty()

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(themeColors.background)
            .statusBarsPadding()
    ) {
        // Search Header Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            IconButton(
                onClick = onBackClick,
                modifier = Modifier
                    .background(themeColors.surface, CircleShape)
                    .size(40.dp)
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "رجوع",
                    tint = themeColors.textPrimary
                )
            }

            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text("بحث موحد في جميع الأقسام والخدمات...", fontSize = 13.sp) },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = themeColors.accent) },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { searchQuery = "" }) {
                            Icon(Icons.Default.Close, contentDescription = "مسح", tint = themeColors.textSecondary)
                        }
                    }
                },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = themeColors.accent,
                    unfocusedBorderColor = themeColors.surface,
                    focusedContainerColor = themeColors.surface,
                    unfocusedContainerColor = themeColors.surface,
                    focusedTextColor = themeColors.textPrimary,
                    unfocusedTextColor = themeColors.textPrimary
                ),
                shape = RoundedCornerShape(24.dp),
                singleLine = true,
                modifier = Modifier.weight(1f)
            )
        }

        if (isSearchingServer) {
            LinearProgressIndicator(
                modifier = Modifier.fillMaxWidth().height(2.dp),
                color = themeColors.accent,
                trackColor = Color.Transparent
            )
        }

        if (debouncedQuery.isBlank()) {
            // Default Empty State Helper Suggestions
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "🔎 ابحث عن أي شيء في اليمن",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = themeColors.accent
                    )
                    Text(
                        text = "أدخل اسم فني، مهنة، متجر، عقار، أو إعلان وظيفة للبدء بالبحث المباشر الفوري والمستقر.",
                        fontSize = 13.sp,
                        color = themeColors.textSecondary,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                }
            }
        } else if (!hasResults) {
            // No results state
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "⚠️ لا توجد نتائج مطابقة",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = themeColors.textSecondary
                    )
                    Text(
                        text = "لم نجد أي نتائج لـ \"$debouncedQuery\". حاول استخدام كلمات بحث مختلفة.",
                        fontSize = 13.sp,
                        color = themeColors.textSecondary,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                }
            }
        } else {
            // Search results list
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(bottom = 24.dp)
            ) {
                // 1. Technicians / Providers Section
                if (filteredProviders.isNotEmpty()) {
                    item {
                        SearchSectionHeader(title = "الفنيون ومزودو الخدمات 👷", themeColors = themeColors)
                    }
                    items(filteredProviders, key = { "prov_${it.id}" }) { provider ->
                        SearchItemCard(
                            title = provider.name.ifBlank { "فني دليل اليمن" },
                            subtitle = provider.profession.ifBlank { "فني صيانة معتمد" },
                            extraInfo = "📱 ${provider.phone} | ⭐ ${if (provider.rating > 0) provider.rating else 5.0}",
                            imageUrl = provider.profileImage,
                            fallbackEmoji = "👷",
                            themeColors = themeColors,
                            onClick = {
                                viewModel.selectedProvider = provider
                                viewModel.selectedStore = null
                                viewModel.selectedProperty = null
                                viewModel.selectedJob = null
                                viewModel.navigateToScreen(AppScreens.PROVIDER_DETAILS)
                            }
                        )
                    }
                }

                // 2. Stores / Businesses Section
                if (filteredStores.isNotEmpty()) {
                    item {
                        SearchSectionHeader(title = "المحلات والمتاجر والمطاعم 🏪", themeColors = themeColors)
                    }
                    items(filteredStores, key = { "store_${it.id}" }) { store ->
                        val isMedical = store.sectionId.contains("medical") || store.categoryId.contains("medical") || store.categoryId.contains("pharmacy") || store.name.contains("طبي") || store.name.contains("صيدلية")
                        val isRestaurant = !isMedical && (store.sectionId.contains("restaurant") || store.categoryId.contains("restaurant") || store.name.contains("مطعم"))
                        val emoji = when {
                            isMedical -> "🏥"
                            isRestaurant -> "🍔"
                            else -> "🏪"
                        }
                        SearchItemCard(
                            title = store.name.ifBlank { "متجر معتمد" },
                            subtitle = store.description.ifBlank { "متجر معتمد في دليل اليمن" },
                            extraInfo = "📍 ${store.localNeighborhood.ifBlank { "اليمن" }} | 📱 ${store.phone}",
                            imageUrl = store.logoImage.ifBlank { store.coverImage },
                            fallbackEmoji = emoji,
                            themeColors = themeColors,
                            onClick = {
                                viewModel.selectedStore = store
                                viewModel.selectedProvider = null
                                viewModel.selectedProperty = null
                                viewModel.selectedJob = null
                                viewModel.navigateToScreen(AppScreens.STORE_DETAILS)
                            }
                        )
                    }
                }

                // 3. Properties Section
                if (filteredProperties.isNotEmpty()) {
                    item {
                        SearchSectionHeader(title = "العقارات والمكاتب المعروضة 🏠", themeColors = themeColors)
                    }
                    items(filteredProperties, key = { "prop_${it.id}" }) { property ->
                        val priceStr = if (property.price > 0) "${property.price} ${property.currency}" else "حسب الاتفاق"
                        SearchItemCard(
                            title = property.title.ifBlank { "عقار معروض" },
                            subtitle = property.description.ifBlank { "عقار متاح بجميع التفاصيل" },
                            extraInfo = "📍 ${property.localNeighborhood.ifBlank { property.cityId }} | 💰 $priceStr",
                            imageUrl = property.images.firstOrNull() ?: "",
                            fallbackEmoji = "🏠",
                            themeColors = themeColors,
                            onClick = {
                                viewModel.selectedProperty = property
                                viewModel.selectedProvider = null
                                viewModel.selectedStore = null
                                viewModel.selectedJob = null
                                viewModel.navigateToScreen(AppScreens.PROPERTY_DETAILS)
                            }
                        )
                    }
                }

                // 4. Jobs Section
                if (filteredJobs.isNotEmpty()) {
                    item {
                        SearchSectionHeader(title = "إعلانات الوظائف والفرص 💼", themeColors = themeColors)
                    }
                    items(filteredJobs, key = { "job_${it.id}" }) { job ->
                        SearchItemCard(
                            title = job.title.ifBlank { "إعلان وظيفة" },
                            subtitle = job.companyName.ifBlank { "جهة غير محددة" },
                            extraInfo = "💼 ${job.jobType} | 💰 ${job.salary.ifBlank { "غير محدد" }}",
                            imageUrl = "",
                            fallbackEmoji = "💼",
                            themeColors = themeColors,
                            onClick = {
                                viewModel.selectedJob = job
                                viewModel.selectedProvider = null
                                viewModel.selectedStore = null
                                viewModel.selectedProperty = null
                                viewModel.navigateToScreen(AppScreens.DYNAMIC_PROFILE)
                            }
                        )
                    }
                }

                // 5. Products Section
                if (filteredProducts.isNotEmpty()) {
                    item {
                        SearchSectionHeader(title = "المنتجات والخدمات المعروضة 🛍️", themeColors = themeColors)
                    }
                    items(filteredProducts, key = { "prod_${it.id}" }) { product ->
                        SearchItemCard(
                            title = product.name,
                            subtitle = product.description,
                            extraInfo = "💰 ${product.price.toInt()} ريال يمني",
                            imageUrl = product.imageUrl,
                            fallbackEmoji = "🛍️",
                            themeColors = themeColors,
                            onClick = {
                                // Find store of this product and open its details
                                val matchedStore = stores.firstOrNull { it.id == product.storeId }
                                if (matchedStore != null) {
                                    viewModel.selectedStore = matchedStore
                                    viewModel.selectedProvider = null
                                    viewModel.selectedProperty = null
                                    viewModel.selectedJob = null
                                    viewModel.navigateToScreen(AppScreens.STORE_DETAILS)
                                }
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun SearchSectionHeader(
    title: String,
    themeColors: VisualThemePalette
) {
    Text(
        text = title,
        fontSize = 14.sp,
        fontWeight = FontWeight.Bold,
        color = themeColors.accent,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
    )
}

@Composable
fun SearchItemCard(
    title: String,
    subtitle: String,
    extraInfo: String,
    imageUrl: String,
    fallbackEmoji: String,
    themeColors: VisualThemePalette,
    onClick: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = themeColors.surface),
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .border(0.5.dp, Color.White.copy(alpha = 0.08f), RoundedCornerShape(12.dp))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Coil AsyncImage / Fallback
            if (imageUrl.isNotBlank()) {
                AsyncImage(
                    model = imageUrl,
                    contentDescription = title,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .size(52.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color.White.copy(alpha = 0.05f))
                )
            } else {
                Box(
                    modifier = Modifier
                        .size(52.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color.White.copy(alpha = 0.05f)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(fallbackEmoji, fontSize = 24.sp)
                }
            }

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = title,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = themeColors.textPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = subtitle,
                    fontSize = 12.sp,
                    color = themeColors.textSecondary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = extraInfo,
                    fontSize = 11.sp,
                    color = themeColors.accent,
                    fontWeight = FontWeight.Medium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}
