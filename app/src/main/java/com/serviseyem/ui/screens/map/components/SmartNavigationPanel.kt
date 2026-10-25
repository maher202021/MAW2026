package com.serviseyem.ui.screens.map.components

import android.widget.Toast
import androidx.compose.animation.*
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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.serviseyem.VoiceManager
import com.serviseyem.ui.screens.map.utils.NavigationRouteInfo
import com.serviseyem.ui.screens.map.utils.StreetOrLandmark
import com.serviseyem.ui.screens.map.utils.YemeniCityLocation
import com.serviseyem.ui.screens.map.utils.YemeniNavigationHub
import com.serviseyem.utils.ValhallaDownloadManager
import com.serviseyem.utils.VisualThemePalette

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SmartNavigationPanel(
    userLat: Double,
    userLng: Double,
    themeColors: VisualThemePalette,
    onDrawRouteOnMap: (lat1: Double, lng1: Double, lat2: Double, lng2: Double, destName: String) -> Unit,
    onClearRoute: () -> Unit,
    onCloseNavigation: () -> Unit,
    onPromptDownloadOffline: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val isOfflineDownloaded = remember { ValhallaDownloadManager.isDownloaded(context) }

    // Start collapsed by default so the map & route path are 100% visible to the user!
    var isCollapsed by remember { mutableStateOf(true) }

    // 0: ملاحة بين المدن والمحافظات (Inter-City)
    // 1: ملاحة الشوارع والأحياء والمحلات (Street & Urban)
    var selectedNavTab by remember { mutableIntStateOf(if (isOfflineDownloaded) 1 else 0) }

    // Voice announcer toggle
    var isVoiceEnabled by remember { mutableStateOf(true) }

    // Inter-City states
    var originCity by remember { mutableStateOf("صنعاء") }
    var destCity by remember { mutableStateOf("عدن") }

    // Street navigation states
    var streetSearchQuery by remember { mutableStateOf("") }
    var selectedStreetOrLandmark by remember { mutableStateOf<StreetOrLandmark?>(null) }

    // Active Route Calculation
    val activeRoute = remember(selectedNavTab, originCity, destCity, selectedStreetOrLandmark, userLat, userLng) {
        if (selectedNavTab == 0) {
            YemeniNavigationHub.calculateInterCityRoute(originCity, destCity)
        } else {
            val target = selectedStreetOrLandmark ?: YemeniNavigationHub.POPULAR_STREETS_LANDMARKS.first()
            YemeniNavigationHub.calculateStreetRoute(userLat, userLng, target.lat, target.lng, target.name)
        }
    }

    // Auto-draw route whenever destination changes
    LaunchedEffect(activeRoute) {
        onDrawRouteOnMap(
            activeRoute.originCoords.first,
            activeRoute.originCoords.second,
            activeRoute.destCoords.first,
            activeRoute.destCoords.second,
            activeRoute.destinationName
        )
    }

    // Filtered streets list for search
    val filteredStreets = remember(streetSearchQuery) {
        if (streetSearchQuery.isBlank()) {
            YemeniNavigationHub.POPULAR_STREETS_LANDMARKS
        } else {
            YemeniNavigationHub.POPULAR_STREETS_LANDMARKS.filter {
                it.name.contains(streetSearchQuery, ignoreCase = true) ||
                it.area.contains(streetSearchQuery, ignoreCase = true) ||
                it.city.contains(streetSearchQuery, ignoreCase = true)
            }
        }
    }

    if (isCollapsed) {
        // ==================== COMPACT HUD BAR (Waze/Google Maps style - Leaves 90% map visible) ====================
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xF90B132B)),
            border = BorderStroke(1.5.dp, Color(0xFF00E5FF)),
            elevation = CardDefaults.cardElevation(defaultElevation = 10.dp),
            modifier = modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Surface(
                        shape = CircleShape,
                        color = Color(0xFF00E5FF).copy(alpha = 0.2f),
                        modifier = Modifier.size(38.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(Icons.Default.Navigation, contentDescription = null, tint = Color(0xFF00E5FF), modifier = Modifier.size(20.dp))
                        }
                    }
                    Column {
                        Text(
                            text = activeRoute.destinationName,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text(text = "📏 ${activeRoute.distanceKm} كم", fontSize = 11.sp, color = Color(0xFF00E5FF))
                            Text(text = "⏱️ ${activeRoute.durationText}", fontSize = 11.sp, color = Color(0xFF10B981))
                        }
                    }
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    // Expand Button
                    Button(
                        onClick = { isCollapsed = false },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981)),
                        shape = RoundedCornerShape(10.dp),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                        modifier = Modifier.height(36.dp)
                    ) {
                        Icon(Icons.Default.ExpandLess, contentDescription = null, tint = Color.Black, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(2.dp))
                        Text("توسيع", color = Color.Black, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }

                    // Close Navigation
                    IconButton(
                        onClick = {
                            onClearRoute()
                            VoiceManager.stop()
                            onCloseNavigation()
                        },
                        modifier = Modifier
                            .size(36.dp)
                            .background(Color(0xFF1E293B), CircleShape)
                    ) {
                        Icon(Icons.Default.Close, contentDescription = "إغلاق", tint = Color(0xFFEF4444), modifier = Modifier.size(18.dp))
                    }
                }
            }
        }
    } else {
        // ==================== EXPANDED PANEL ====================
        Card(
            shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xF90B132B)),
            border = BorderStroke(1.5.dp, Color(0xFF00E5FF)),
            elevation = CardDefaults.cardElevation(defaultElevation = 14.dp),
            modifier = modifier
                .fillMaxWidth()
                .heightIn(max = 500.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Collapse / Minimize Bar Button
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Navigation,
                            contentDescription = null,
                            tint = Color(0xFF00E5FF),
                            modifier = Modifier.size(20.dp)
                        )
                        Text(
                            text = "نظام الملاحة الذكي المتطور 🧭",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        // Minimize Button to let user see map fully
                        Button(
                            onClick = { isCollapsed = true },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E293B)),
                            border = BorderStroke(1.dp, Color(0xFF00E5FF)),
                            shape = RoundedCornerShape(10.dp),
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                            modifier = Modifier.height(32.dp)
                        ) {
                            Icon(Icons.Default.ExpandMore, contentDescription = null, tint = Color(0xFF00E5FF), modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(2.dp))
                            Text("تصغير لرؤية الخريطة", color = Color(0xFF00E5FF), fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        }

                        // Close Navigation
                        IconButton(
                            onClick = {
                                onClearRoute()
                                VoiceManager.stop()
                                onCloseNavigation()
                            },
                            modifier = Modifier
                                .size(32.dp)
                                .background(Color(0xFF1E293B), CircleShape)
                        ) {
                            Icon(Icons.Default.Close, contentDescription = "إغلاق", tint = Color(0xFFEF4444), modifier = Modifier.size(16.dp))
                        }
                    }
                }

                // Mode Tabs: Inter-City vs Urban Street
                TabRow(
                    selectedTabIndex = selectedNavTab,
                    containerColor = Color(0xFF111C30),
                    contentColor = Color(0xFF00E5FF),
                    divider = {}
                ) {
                    Tab(
                        selected = selectedNavTab == 0,
                        onClick = { selectedNavTab = 0 },
                        text = {
                            Text(
                                text = "🛣️ بين المدن والمحافظات",
                                fontSize = 11.sp,
                                fontWeight = if (selectedNavTab == 0) FontWeight.Bold else FontWeight.Normal,
                                color = if (selectedNavTab == 0) Color(0xFF00E5FF) else Color(0xFF94A3B8)
                            )
                        }
                    )
                    Tab(
                        selected = selectedNavTab == 1,
                        onClick = {
                            if (!isOfflineDownloaded) {
                                onPromptDownloadOffline()
                            }
                            selectedNavTab = 1
                        },
                        text = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "🏙️ الشوارع والأحياء",
                                    fontSize = 11.sp,
                                    fontWeight = if (selectedNavTab == 1) FontWeight.Bold else FontWeight.Normal,
                                    color = if (selectedNavTab == 1) Color(0xFF10B981) else Color(0xFF94A3B8)
                                )
                                if (isOfflineDownloaded) {
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("✓", color = Color(0xFF10B981), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    )
                }

                // Main Tab Body
                if (selectedNavTab == 0) {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            var showOriginMenu by remember { mutableStateOf(false) }
                            OutlinedButton(
                                onClick = { showOriginMenu = true },
                                shape = RoundedCornerShape(10.dp),
                                border = BorderStroke(1.dp, Color(0xFF334155)),
                                colors = ButtonDefaults.outlinedButtonColors(containerColor = Color(0xFF162032)),
                                modifier = Modifier.weight(1f)
                            ) {
                                Column(horizontalAlignment = Alignment.Start) {
                                    Text("نقطة الانطلاق 📍", fontSize = 9.sp, color = Color(0xFF94A3B8))
                                    Text(originCity, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                }
                            }
                            DropdownMenu(
                                expanded = showOriginMenu,
                                onDismissRequest = { showOriginMenu = false }
                            ) {
                                YemeniNavigationHub.CITIES.forEach { city ->
                                    DropdownMenuItem(
                                        text = { Text(city.name) },
                                        onClick = {
                                            originCity = city.name
                                            showOriginMenu = false
                                        }
                                    )
                                }
                            }

                            Box(modifier = Modifier.align(Alignment.CenterVertically)) {
                                Icon(Icons.Default.SwapHoriz, contentDescription = null, tint = Color(0xFF00E5FF))
                            }

                            var showDestMenu by remember { mutableStateOf(false) }
                            OutlinedButton(
                                onClick = { showDestMenu = true },
                                shape = RoundedCornerShape(10.dp),
                                border = BorderStroke(1.dp, Color(0xFF10B981)),
                                colors = ButtonDefaults.outlinedButtonColors(containerColor = Color(0xFF162032)),
                                modifier = Modifier.weight(1f)
                            ) {
                                Column(horizontalAlignment = Alignment.Start) {
                                    Text("مدينة الوصول 🏁", fontSize = 9.sp, color = Color(0xFF10B981))
                                    Text(destCity, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                }
                            }
                            DropdownMenu(
                                expanded = showDestMenu,
                                onDismissRequest = { showDestMenu = false }
                            ) {
                                YemeniNavigationHub.CITIES.forEach { city ->
                                    DropdownMenuItem(
                                        text = { Text(city.name) },
                                        onClick = {
                                            destCity = city.name
                                            showDestMenu = false
                                        }
                                    )
                                }
                            }
                        }

                        LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            items(listOf("عدن", "تعز", "الحديدة", "إب", "المكلا", "مأرب", "ذمار")) { city ->
                                FilterChip(
                                    selected = destCity == city,
                                    onClick = { destCity = city },
                                    label = { Text(city, fontSize = 11.sp) },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = Color(0xFF10B981),
                                        selectedLabelColor = Color.Black,
                                        containerColor = Color(0xFF1E293B),
                                        labelColor = Color.White
                                    )
                                )
                            }
                        }
                    }
                } else {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = streetSearchQuery,
                            onValueChange = { streetSearchQuery = it },
                            placeholder = { Text("ابحث عن الشارع، الحي، المستشفى، أو المعلم...", fontSize = 11.sp, color = Color.Gray) },
                            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = Color(0xFF10B981)) },
                            trailingIcon = {
                                if (streetSearchQuery.isNotEmpty()) {
                                    IconButton(onClick = { streetSearchQuery = "" }) {
                                        Icon(Icons.Default.Clear, contentDescription = "مسح", tint = Color.Gray)
                                    }
                                }
                            },
                            shape = RoundedCornerShape(10.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedContainerColor = Color(0xFF162032),
                                unfocusedContainerColor = Color(0xFF162032),
                                focusedBorderColor = Color(0xFF10B981),
                                unfocusedBorderColor = Color(0xFF334155),
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White
                            ),
                            singleLine = true,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(46.dp)
                        )

                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            items(filteredStreets.take(8)) { street ->
                                Surface(
                                    shape = RoundedCornerShape(10.dp),
                                    color = if (selectedStreetOrLandmark?.name == street.name) Color(0xFF10B981) else Color(0xFF1E293B),
                                    border = BorderStroke(1.dp, Color(0xFF334155)),
                                    modifier = Modifier.clickable {
                                        selectedStreetOrLandmark = street
                                        if (isVoiceEnabled) {
                                            VoiceManager.speak("تم اختيار الوجهة: ${street.name}، ${street.area}")
                                        }
                                    }
                                ) {
                                    Text(
                                        text = street.name,
                                        fontSize = 11.sp,
                                        color = if (selectedStreetOrLandmark?.name == street.name) Color.Black else Color.White,
                                        fontWeight = FontWeight.SemiBold,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp)
                                    )
                                }
                            }
                        }
                    }
                }

                // Route Metrics Summary
                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF111E38)),
                    border = BorderStroke(1.dp, Color(0xFF1E3A5F))
                ) {
                    Column(
                        modifier = Modifier.padding(10.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                Icon(Icons.Default.Route, contentDescription = null, tint = Color(0xFF00E5FF), modifier = Modifier.size(16.dp))
                                Text(text = "المسافة: ${activeRoute.distanceKm} كم", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFF00E5FF))
                            }
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                Icon(Icons.Default.Timer, contentDescription = null, tint = Color(0xFF10B981), modifier = Modifier.size(16.dp))
                                Text(text = "الوقت: ${activeRoute.durationText}", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFF10B981))
                            }
                        }
                        Text(
                            text = "🛣️ ${activeRoute.highwayOrStreetName}",
                            fontSize = 11.sp,
                            color = Color(0xFFCBD5E1),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                // Action Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = {
                            val speechText = "مسار الملاحة إلى ${activeRoute.destinationName}. المسافة ${activeRoute.distanceKm} كم، والزمن المقدر ${activeRoute.durationText}. رافقتكم السلامة."
                            VoiceManager.speak(speechText)
                            Toast.makeText(context, "🗣️ جاري نطق التوجيهات...", Toast.LENGTH_SHORT).show()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E293B)),
                        border = BorderStroke(1.dp, Color(0xFF00E5FF)),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .weight(1f)
                            .height(40.dp)
                    ) {
                        Icon(Icons.Default.VolumeUp, contentDescription = null, tint = Color(0xFF00E5FF), modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("نطق الإرشادات", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }

                    Button(
                        onClick = {
                            val arrivalSpeech = "لقد وصلت إلى وجهتك: ${activeRoute.destinationName} بنجاح!"
                            VoiceManager.speak(arrivalSpeech)
                            Toast.makeText(context, "🏁 تم إعلان الوصول بنجاح!", Toast.LENGTH_LONG).show()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981)),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .weight(1f)
                            .height(40.dp)
                    ) {
                        Icon(Icons.Default.Celebration, contentDescription = null, tint = Color.Black, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("إعلان الوصول", color = Color.Black, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
