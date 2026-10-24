package com.serviseyem.utils

import com.serviseyem.utils.*

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.min

/**
 * Levenshtein distance fuzzy matching engine for search queries across categories.
 */
object LevenshteinMatcher {

    private val searchCache = java.util.concurrent.ConcurrentHashMap<String, Pair<Long, Boolean>>()
    private const val CACHE_TTL = 30_000L // 30 seconds

    fun calculateDistance(s1: String, s2: String): Int {
        val str1 = s1.lowercase().trim()
        val str2 = s2.lowercase().trim()
        val dp = Array(str1.length + 1) { IntArray(str2.length + 1) }

        for (i in 0..str1.length) dp[i][0] = i
        for (j in 0..str2.length) dp[0][j] = j

        for (i in 1..str1.length) {
            for (j in 1..str2.length) {
                val cost = if (str1[i - 1] == str2[j - 1]) 0 else 1
                dp[i][j] = minOf(
                    dp[i - 1][j] + 1,      // deletion
                    dp[i][j - 1] + 1,      // insertion
                    dp[i - 1][j - 1] + cost // substitution
                )
            }
        }
        return dp[str1.length][str2.length]
    }

    fun isFuzzyMatch(query: String, target: String, maxDistance: Int = 3): Boolean {
        if (query.isBlank()) return true
        val q = query.lowercase().trim()
        val t = target.lowercase().trim()

        if (searchCache.size > 500) {
            val now = System.currentTimeMillis()
            searchCache.entries.removeIf { now - it.value.first > CACHE_TTL }
        }

        val cacheKey = "$q::$t::$maxDistance"
        val cached = searchCache[cacheKey]
        if (cached != null && System.currentTimeMillis() - cached.first < CACHE_TTL) {
            return cached.second
        }

        if (t.contains(q)) {
            searchCache[cacheKey] = Pair(System.currentTimeMillis(), true)
            return true
        }

        if (t.length > 100) {
            val result = t.contains(q)
            searchCache[cacheKey] = Pair(System.currentTimeMillis(), result)
            return result
        }

        val queryWords = q.split("\\s+".toRegex())
        val targetWords = t.split("\\s+".toRegex())

        for (qw in queryWords) {
            for (tw in targetWords) {
                val allowedDist = when {
                    qw.length <= 3 -> 1
                    qw.length <= 5 -> 2
                    else -> maxDistance
                }
                if (calculateDistance(qw, tw) <= allowedDist) {
                    searchCache[cacheKey] = Pair(System.currentTimeMillis(), true)
                    return true
                }
            }
        }
        searchCache[cacheKey] = Pair(System.currentTimeMillis(), false)
        return false
    }
}

data class SearchTechnicianItem(
    val id: String,
    val name: String,
    val profession: String,
    val rating: Double,
    val phone: String
)

data class SearchStoreItem(
    val id: String,
    val name: String,
    val category: String,
    val address: String,
    val isVerified: Boolean = true
)

data class SearchArticleItem(
    val id: String,
    val title: String,
    val summary: String,
    val category: String
)

data class SearchResultData(
    val technicians: List<SearchTechnicianItem> = emptyList(),
    val stores: List<SearchStoreItem> = emptyList(),
    val articles: List<SearchArticleItem> = emptyList()
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CrossFuzzySearchScreen(
    query: String,
    onQueryChange: (String) -> Unit,
    allTechnicians: List<SearchTechnicianItem>,
    allStores: List<SearchStoreItem>,
    allArticles: List<SearchArticleItem>,
    onSelectTechnician: (SearchTechnicianItem) -> Unit,
    onSelectStore: (SearchStoreItem) -> Unit,
    onSelectArticle: (SearchArticleItem) -> Unit,
    onClose: () -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedTabIndex by remember { mutableIntStateOf(0) }
    val tabTitles = listOf("👷 فنيون", "🏬 محلات وقوائم", "💡 مقالات وحلول")

    val filteredTechnicians = remember(query, allTechnicians) {
        if (query.isBlank()) allTechnicians else {
            allTechnicians.filter {
                LevenshteinMatcher.isFuzzyMatch(query, it.name) ||
                        LevenshteinMatcher.isFuzzyMatch(query, it.profession)
            }
        }
    }

    val filteredStores = remember(query, allStores) {
        if (query.isBlank()) allStores else {
            allStores.filter {
                LevenshteinMatcher.isFuzzyMatch(query, it.name) ||
                        LevenshteinMatcher.isFuzzyMatch(query, it.category) ||
                        LevenshteinMatcher.isFuzzyMatch(query, it.address)
            }
        }
    }

    val filteredArticles = remember(query, allArticles) {
        if (query.isBlank()) allArticles else {
            allArticles.filter {
                LevenshteinMatcher.isFuzzyMatch(query, it.title) ||
                        LevenshteinMatcher.isFuzzyMatch(query, it.summary) ||
                        LevenshteinMatcher.isFuzzyMatch(query, it.category)
            }
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp)
    ) {
        // Search Input Bar
        OutlinedTextField(
            value = query,
            onValueChange = onQueryChange,
            modifier = Modifier.fillMaxWidth(),
            placeholder = { Text("بحث متسامح بالأخطاء الإملائية (فنيون، محلات، مقالات)...") },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = "بحث") },
            trailingIcon = {
                if (query.isNotEmpty()) {
                    IconButton(onClick = { onQueryChange("") }) {
                        Icon(Icons.Default.Clear, contentDescription = "مسح")
                    }
                }
            },
            shape = RoundedCornerShape(16.dp),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Tabs
        TabRow(
            selectedTabIndex = selectedTabIndex,
            containerColor = MaterialTheme.colorScheme.surfaceVariant,
            contentColor = MaterialTheme.colorScheme.primary,
            modifier = Modifier.clip(RoundedCornerShape(12.dp))
        ) {
            tabTitles.forEachIndexed { index, title ->
                Tab(
                    selected = selectedTabIndex == index,
                    onClick = { selectedTabIndex = index },
                    text = {
                        Text(
                            text = title,
                            fontWeight = if (selectedTabIndex == index) FontWeight.Bold else FontWeight.Normal,
                            fontSize = 13.sp
                        )
                    }
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Tab Content
        when (selectedTabIndex) {
            0 -> { // Technicians
                if (filteredTechnicians.isEmpty()) {
                    EmptyResultPlaceholder("لا توجد نتائج فنيين مطابقة للبحث")
                } else {
                    LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(filteredTechnicians) { tech ->
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { onSelectTechnician(tech) },
                                shape = RoundedCornerShape(12.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                            ) {
                                Row(
                                    modifier = Modifier.padding(16.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        Icons.Default.Person,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(32.dp)
                                    )
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(tech.name, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                                        Text(tech.profession, color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 13.sp)
                                    }
                                    Badge(containerColor = MaterialTheme.colorScheme.primaryContainer) {
                                        Text("★ ${tech.rating}", fontSize = 12.sp)
                                    }
                                }
                            }
                        }
                    }
                }
            }
            1 -> { // Stores
                if (filteredStores.isEmpty()) {
                    EmptyResultPlaceholder("لا توجد محلات أو مراكز مطابقة للبحث")
                } else {
                    LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(filteredStores) { store ->
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { onSelectStore(store) },
                                shape = RoundedCornerShape(12.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                            ) {
                                Row(
                                    modifier = Modifier.padding(16.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        Icons.Default.ShoppingCart,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.secondary,
                                        modifier = Modifier.size(32.dp)
                                    )
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(store.name, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                                        Text("${store.category} - ${store.address}", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 13.sp)
                                    }
                                }
                            }
                        }
                    }
                }
            }
            2 -> { // Articles
                if (filteredArticles.isEmpty()) {
                    EmptyResultPlaceholder("لا توجد مقالات أو حلول مطابقة")
                } else {
                    LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(filteredArticles) { article ->
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { onSelectArticle(article) },
                                shape = RoundedCornerShape(12.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                            ) {
                                Row(
                                    modifier = Modifier.padding(16.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        Icons.Default.Info,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.tertiary,
                                        modifier = Modifier.size(32.dp)
                                    )
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(article.title, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                                        Text(article.summary, color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 12.sp, maxLines = 2)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun EmptyResultPlaceholder(text: String) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(32.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(text = text, color = Color.Gray, fontSize = 14.sp)
    }
}
