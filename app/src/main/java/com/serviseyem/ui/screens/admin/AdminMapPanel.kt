@file:OptIn(androidx.compose.foundation.layout.ExperimentalLayoutApi::class, androidx.compose.material3.ExperimentalMaterial3Api::class)

package com.serviseyem.ui.screens.admin
import com.serviseyem.ui.*

import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.serviseyem.data.PropertyEntity
import com.serviseyem.data.ProviderEntity
import com.serviseyem.data.StoreEntity
import com.serviseyem.ui.MainViewModel
import com.serviseyem.utils.VisualThemePalette

/**
 * 🗺️ لوحة إدارة الخرائط والمواقع الجغرافية (Admin Map Panel)
 * إشراف كامل على توزيع العلامات (Markers)، التنسيق الجغرافي، دقة الـ GPS، والمدن اليمنية
 */
@Composable
fun AdminMapPanel(
    viewModel: MainViewModel,
    themeColors: VisualThemePalette,
    onOpenMap: () -> Unit = {}
) {
    val context = LocalContext.current
    val providers by viewModel.providers.collectAsState(initial = emptyList())
    val stores by viewModel.stores.collectAsState(initial = emptyList())
    val properties by viewModel.properties.collectAsState(initial = emptyList())

    var selectedCityFilter by remember { mutableStateOf("الكل") }
    var selectedTypeFilter by remember { mutableStateOf("ALL") } // ALL, PROVIDER, STORE, RESTAURANT, MEDICAL, PROPERTY
    var searchQuery by remember { mutableStateOf("") }

    val yemeniCities = com.serviseyem.domain.YemenCities.listWithAll

    val totalProvidersWithGps = providers.count { it.latitude != 0.0 && it.longitude != 0.0 }
    val totalStoresWithGps = stores.count { it.latitude != 0.0 && it.longitude != 0.0 }
    val totalPropertiesWithGps = properties.count { it.latitude != 0.0 && it.longitude != 0.0 }
    val grandTotalPins = totalProvidersWithGps + totalStoresWithGps + totalPropertiesWithGps

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Header Card
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = themeColors.surface),
            border = BorderStroke(1.dp, Color(0xFF00E5FF).copy(alpha = 0.3f)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF00E5FF).copy(alpha = 0.15f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.Place, contentDescription = null, tint = Color(0xFF00E5FF), modifier = Modifier.size(22.dp))
                        }
                        Column {
                            Text("إدارة الخرائط والمواقع الجغرافية", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = Color.White)
                            Text("نظام الرادار والتوزيع الجغرافي للخدمات", fontSize = 11.sp, color = Color.LightGray)
                        }
                    }

                    Button(
                        onClick = onOpenMap,
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00E5FF)),
                        shape = RoundedCornerShape(10.dp),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Icon(Icons.Default.LocationOn, contentDescription = null, tint = Color.Black, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("فتح الخريطة", color = Color.Black, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }

                // Stats Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    StatBadge(title = "إجمالي العلامات", count = "$grandTotalPins", color = Color(0xFF00E5FF), modifier = Modifier.weight(1f))
                    StatBadge(title = "فنيون معتمدون", count = "$totalProvidersWithGps", color = Color(0xFF38BDF8), modifier = Modifier.weight(1f))
                    StatBadge(title = "متاجر ومطاعم", count = "$totalStoresWithGps", color = Color(0xFF10B981), modifier = Modifier.weight(1f))
                    StatBadge(title = "عقارات معروضة", count = "$totalPropertiesWithGps", color = Color(0xFF8B5CF6), modifier = Modifier.weight(1f))
                }
            }
        }

        // City Filters
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            items(yemeniCities) { city ->
                FilterChip(
                    selected = selectedCityFilter == city,
                    onClick = { selectedCityFilter = city },
                    label = { Text(city, fontSize = 11.sp) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = Color(0xFF00E5FF).copy(alpha = 0.2f),
                        selectedLabelColor = Color(0xFF00E5FF)
                    )
                )
            }
        }

        // Search Bar
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            placeholder = { Text("بحث عن فني، متجر، أو عقار...", fontSize = 12.sp) },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = Color.Gray) },
            trailingIcon = {
                if (searchQuery.isNotEmpty()) {
                    IconButton(onClick = { searchQuery = "" }) {
                        Icon(Icons.Default.Clear, contentDescription = "مسح", tint = Color.Gray)
                    }
                }
            },
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        // List of Pinned Entities
        LazyColumn(
            modifier = Modifier.fillMaxWidth().weight(1f),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Providers
            val matchedProviders = providers.filter { p ->
                (selectedCityFilter == "الكل" || p.cityId.contains(selectedCityFilter) || p.area.contains(selectedCityFilter)) &&
                (searchQuery.isEmpty() || p.name.contains(searchQuery, ignoreCase = true) || p.profession.contains(searchQuery, ignoreCase = true))
            }

            items(matchedProviders) { p ->
                AdminMapEntityItem(
                    title = p.name,
                    subtitle = "${p.profession} - ${p.area.ifEmpty { p.cityId.ifEmpty { "صنعاء" } }}",
                    lat = p.latitude,
                    lng = p.longitude,
                    typeLabel = "فني 👷",
                    typeColor = Color(0xFF00E5FF),
                    phone = p.phone,
                    themeColors = themeColors
                )
            }

            // Stores
            val matchedStores = stores.filter { s ->
                (selectedCityFilter == "الكل" || s.cityId.contains(selectedCityFilter) || s.localNeighborhood.contains(selectedCityFilter)) &&
                (searchQuery.isEmpty() || s.name.contains(searchQuery, ignoreCase = true) || s.categoryId.contains(searchQuery, ignoreCase = true))
            }

            items(matchedStores) { s ->
                val isMedical = s.sectionId.contains("medical") || s.name.contains("طبي") || s.name.contains("صيدلية")
                val isRestaurant = s.sectionId.contains("restaurant") || s.name.contains("مطعم") || s.name.contains("كافيه")
                val (label, color) = when {
                    isMedical -> Pair("مركز طبي 🏥", Color(0xFFEC4899))
                    isRestaurant -> Pair("مطعم 🍔", Color(0xFFF59E0B))
                    else -> Pair("متجر 🏪", Color(0xFF10B981))
                }
                AdminMapEntityItem(
                    title = s.name,
                    subtitle = "${s.categoryId.ifEmpty { "قسم تجاري" }} - ${s.cityId.ifEmpty { "صنعاء" }}",
                    lat = s.latitude,
                    lng = s.longitude,
                    typeLabel = label,
                    typeColor = color,
                    phone = s.phone,
                    themeColors = themeColors
                )
            }

            // Properties
            val matchedProperties = properties.filter { pr ->
                (selectedCityFilter == "الكل" || pr.cityId.contains(selectedCityFilter) || pr.localNeighborhood.contains(selectedCityFilter)) &&
                (searchQuery.isEmpty() || pr.title.contains(searchQuery, ignoreCase = true))
            }

            items(matchedProperties) { pr ->
                AdminMapEntityItem(
                    title = pr.title,
                    subtitle = "عقار - ${pr.cityId.ifEmpty { "صنعاء" }} (${pr.price} ${pr.currency})",
                    lat = pr.latitude,
                    lng = pr.longitude,
                    typeLabel = "عقار 🏠",
                    typeColor = Color(0xFF8B5CF6),
                    phone = pr.phone,
                    themeColors = themeColors
                )
            }
        }
    }
}

@Composable
private fun StatBadge(title: String, count: String, color: Color, modifier: Modifier = Modifier) {
    Card(
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(containerColor = color.copy(alpha = 0.12f)),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.padding(vertical = 8.dp, horizontal = 6.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(count, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = color)
            Text(title, fontSize = 9.sp, color = Color.LightGray, maxLines = 1, textAlign = TextAlign.Center)
        }
    }
}

@Composable
private fun AdminMapEntityItem(
    title: String,
    subtitle: String,
    lat: Double,
    lng: Double,
    typeLabel: String,
    typeColor: Color,
    phone: String,
    themeColors: VisualThemePalette
) {
    val context = LocalContext.current
    val hasGps = lat != 0.0 && lng != 0.0

    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = themeColors.surface),
        border = BorderStroke(1.dp, if (hasGps) typeColor.copy(alpha = 0.25f) else Color.DarkGray),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(10.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        text = typeLabel,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = typeColor,
                        modifier = Modifier
                            .background(typeColor.copy(alpha = 0.12f), RoundedCornerShape(6.dp))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                    Text(
                        text = title,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Text(subtitle, fontSize = 11.sp, color = Color.Gray, maxLines = 1, overflow = TextOverflow.Ellipsis)

                if (hasGps) {
                    Text("📍 الإحداثيات: ${String.format("%.4f", lat)}, ${String.format("%.4f", lng)}", fontSize = 10.sp, color = Color(0xFF38BDF8))
                } else {
                    Text("⚠️ الإحداثيات غير محددة (موقع المدينة الافتراضي)", fontSize = 10.sp, color = Color(0xFFF59E0B))
                }
            }

            // Navigation button
            if (hasGps) {
                IconButton(
                    onClick = {
                        val provider = "GOOGLE_MAPS"
                        try {
                            when (provider) {
                                "MAPBOX" -> {
                                    val uri = android.net.Uri.parse("mapbox://directions/profile/mapbox/driving/$lng,$lat")
                                    context.startActivity(android.content.Intent(android.content.Intent.ACTION_VIEW, uri))
                                    android.widget.Toast.makeText(context, "تم فتح Mapbox", android.widget.Toast.LENGTH_SHORT).show()
                                }
                                "MAPLIBRE" -> {
                                    val uri = android.net.Uri.parse("geo:$lat,$lng?q=$lat,$lng")
                                    context.startActivity(android.content.Intent(android.content.Intent.ACTION_VIEW, uri))
                                    android.widget.Toast.makeText(context, "تم فتح MapLibre / الخريطة الافتراضية", android.widget.Toast.LENGTH_SHORT).show()
                                }
                                else -> {
                                    val uri = android.net.Uri.parse("google.navigation:q=$lat,$lng")
                                    val intent = android.content.Intent(android.content.Intent.ACTION_VIEW, uri).apply {
                                        setPackage("com.google.android.apps.maps")
                                    }
                                    context.startActivity(intent)
                                    android.widget.Toast.makeText(context, "تم فتح خرائط جوجل", android.widget.Toast.LENGTH_SHORT).show()
                                }
                            }
                        } catch (e: Exception) {
                            val fallbackUri = android.net.Uri.parse("https://www.google.com/maps/search/?api=1&query=$lat,$lng")
                            context.startActivity(android.content.Intent(android.content.Intent.ACTION_VIEW, fallbackUri))
                        }
                    },
                    modifier = androidx.compose.ui.Modifier.size(32.dp)
                ) {
                    Icon(Icons.Default.LocationOn, contentDescription = "توجيه", tint = Color(0xFF00E5FF))
                }
            }
        }
    }
}
