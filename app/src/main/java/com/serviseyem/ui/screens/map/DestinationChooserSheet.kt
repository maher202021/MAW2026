package com.serviseyem.ui.screens.map

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.serviseyem.data.ProviderEntity
import com.serviseyem.data.StoreEntity
import com.serviseyem.ui.screens.map.utils.OfflineMapManager
import com.serviseyem.ui.screens.map.visualaddress.LandmarkAddressPicker
import com.serviseyem.utils.*
import kotlinx.coroutines.delay
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DestinationChooserSheet(
    userLat: Double,
    userLng: Double,
    hasRealLocation: Boolean,
    providers: List<ProviderEntity>,
    stores: List<StoreEntity>,
    savedAddresses: List<LandmarkAddress>,
    onPick: (name: String, lat: Double, lng: Double) -> Unit,
    onPickFromMap: () -> Unit,
    onDownloadPlaces: () -> Unit,
    onDismiss: () -> Unit,
    selectedCity: String = "صنعاء",
    onAddressSaved: ((LandmarkAddress) -> Unit)? = null
) {
    val context = LocalContext.current
    val recents = remember { RecentDestinations(context) }
    val addressStore = remember { LandmarkAddressStore(context) }
    var localSavedAddresses by remember(savedAddresses) {
        mutableStateOf((savedAddresses + addressStore.all()).distinctBy { it.label })
    }
    var showAddAddressPicker by remember { mutableStateOf(false) }

    var tab by remember { mutableIntStateOf(0) }
    var query by remember { mutableStateOf("") }
    var outcome by remember { mutableStateOf<PlaceSearchOutcome?>(null) }
    var busy by remember { mutableStateOf(false) }
    var coordText by remember { mutableStateOf("") }
    val tabs = listOf("بحث", "الخريطة", "المدن", "عناويني", "الأخيرة", "إحداثيات")

    LaunchedEffect(query, tab) {
        if (tab != 0) return@LaunchedEffect
        if (query.trim().length < 2) {
            outcome = null
            busy = false
            return@LaunchedEffect
        }
        delay(500)
        busy = true
        outcome = PlaceSearch.search(
            context = context,
            query = query,
            providers = providers,
            stores = stores,
            userLat = if (hasRealLocation) userLat else 0.0,
            userLng = if (hasRealLocation) userLng else 0.0
        )
        busy = false
    }

    fun pick(name: String, lat: Double, lng: Double) {
        recents.add(name, lat, lng)
        onPick(name, lat, lng)
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = Color(0xFF0F172A)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(horizontal = 16.dp)
                .heightIn(min = 320.dp, max = 560.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "اختر وجهتك",
                    color = Color.White,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold
                )
                if (!OfflinePlacesDb.isInstalled(context)) {
                    TextButton(
                        onClick = onDownloadPlaces,
                        modifier = Modifier.testTag("chooser_download_places_chip")
                    ) {
                        Text("📥 قاعدة الأماكن", color = Color(0xFF38BDF8), fontSize = 12.sp)
                    }
                }
            }

            ScrollableTabRow(
                selectedTabIndex = tab,
                containerColor = Color.Transparent,
                contentColor = Color(0xFF00E5FF),
                edgePadding = 0.dp
            ) {
                tabs.forEachIndexed { i, t ->
                    Tab(
                        selected = tab == i,
                        onClick = { tab = i },
                        modifier = Modifier.testTag("chooser_tab_$i"),
                        text = { Text(t, fontSize = 12.sp, color = if (tab == i) Color(0xFF00E5FF) else Color(0xFFCBD5E1)) }
                    )
                }
            }

            Spacer(Modifier.height(10.dp))

            when (tab) {
                0 -> {
                    OutlinedTextField(
                        value = query,
                        onValueChange = { query = it },
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("chooser_search_input"),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedBorderColor = Color(0xFF00E5FF),
                            unfocusedBorderColor = Color(0xFF334155),
                            cursorColor = Color(0xFF00E5FF)
                        ),
                        placeholder = { Text("شارع، حي، مدينة، معلم، فني…", fontSize = 12.sp, color = Color(0xFF94A3B8)) }
                    )
                    if (busy) LinearProgressIndicator(modifier = Modifier.fillMaxWidth().padding(top = 6.dp), color = Color(0xFF00E5FF))
                    Spacer(Modifier.height(8.dp))
                    when (val o = outcome) {
                        is PlaceSearchOutcome.Results -> LazyColumn {
                            items(o.items) { r ->
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .defaultMinSize(minHeight = 48.dp)
                                        .clickable { pick(r.shortName, r.latitude, r.longitude) }
                                        .padding(12.dp)
                                ) {
                                    Text(
                                        text = "${r.icon} ${r.shortName}",
                                        color = Color.White,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis,
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Medium
                                    )
                                    if (r.displayName != r.shortName) {
                                        Text(
                                            text = r.displayName,
                                            color = Color(0xFF94A3B8),
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis,
                                            fontSize = 11.sp
                                        )
                                    }
                                }
                                HorizontalDivider(color = Color.White.copy(alpha = 0.06f))
                            }
                        }
                        is PlaceSearchOutcome.Message -> Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            Text(o.text, color = Color(0xFFF59E0B), fontSize = 13.sp)
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                OutlinedButton(
                                    onClick = { tab = 1 },
                                    modifier = Modifier.defaultMinSize(minHeight = 48.dp).testTag("chooser_msg_map_btn")
                                ) {
                                    Text("من الخريطة", color = Color.White)
                                }
                                OutlinedButton(
                                    onClick = { tab = 2 },
                                    modifier = Modifier.defaultMinSize(minHeight = 48.dp).testTag("chooser_msg_cities_btn")
                                ) {
                                    Text("المدن", color = Color.White)
                                }
                                if (o.canDownloadPlaces) {
                                    Button(
                                        onClick = onDownloadPlaces,
                                        modifier = Modifier.defaultMinSize(minHeight = 48.dp).testTag("chooser_msg_download_places_btn")
                                    ) {
                                        Text("تحميل الأماكن")
                                    }
                                }
                            }
                        }
                        null -> Text(
                            text = "اكتب حرفين على الأقل. يعمل البحث بلا إنترنت بعد تحميل قاعدة الأماكن.",
                            color = Color(0xFF94A3B8),
                            fontSize = 12.sp
                        )
                    }
                }

                1 -> Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = "اضغط على الخريطة في موقع وجهتك. إن كانت الخريطة بلا تفاصيل بلا إنترنت فاستعمل «المدن» أو «الإحداثيات».",
                        color = Color(0xFFCBD5E1),
                        fontSize = 13.sp
                    )
                    Button(
                        onClick = onPickFromMap,
                        modifier = Modifier
                            .fillMaxWidth()
                            .defaultMinSize(minHeight = 48.dp)
                            .testTag("chooser_pick_from_map_button")
                    ) {
                        Text("اختيار من الخريطة")
                    }
                }

                2 -> LazyColumn {
                    val builtin = OfflineMapManager.allCities().map { Triple(it.nameAr, it.latitude, it.longitude) }
                    val fromDb = if (OfflinePlacesDb.isInstalled(context)) OfflinePlacesDb.listMajor(context, 150) else emptyList()
                    items(builtin + fromDb.map { Triple(it.shortName, it.latitude, it.longitude) }) { (n, la, lo) ->
                        Text(
                            text = "🏙️ $n",
                            color = Color.White,
                            fontSize = 14.sp,
                            modifier = Modifier
                                .fillMaxWidth()
                                .defaultMinSize(minHeight = 48.dp)
                                .clickable { pick(n, la, lo) }
                                .padding(12.dp)
                        )
                        HorizontalDivider(color = Color.White.copy(alpha = 0.06f))
                    }
                }

                3 -> Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedButton(
                        onClick = { showAddAddressPicker = true },
                        modifier = Modifier
                            .fillMaxWidth()
                            .defaultMinSize(minHeight = 48.dp)
                            .testTag("chooser_add_landmark_address_button")
                    ) {
                        Text("🏠 إضافة عنوان بالمعلم القريب", color = Color(0xFF10B981))
                    }
                    if (localSavedAddresses.isEmpty()) {
                        Text("لا عناوين محفوظة بعد (زر 🏠).", color = Color(0xFF94A3B8), fontSize = 13.sp)
                    } else {
                        LazyColumn {
                            items(localSavedAddresses) { a ->
                                Text(
                                    text = "🏠 ${a.label}: ${a.describe()}",
                                    color = Color.White,
                                    fontSize = 13.5.sp,
                                    maxLines = 2,
                                    overflow = TextOverflow.Ellipsis,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .defaultMinSize(minHeight = 48.dp)
                                        .clickable { pick(a.label, a.pointLat, a.pointLng) }
                                        .padding(12.dp)
                                )
                                HorizontalDivider(color = Color.White.copy(alpha = 0.06f))
                            }
                        }
                    }
                }

                4 -> {
                    val list = remember { recents.all() }
                    if (list.isEmpty()) {
                        Text("لا وجهات سابقة.", color = Color(0xFF94A3B8), fontSize = 13.sp)
                    } else {
                        LazyColumn {
                            items(list) { d ->
                                Text(
                                    text = "🕘 ${d.name}",
                                    color = Color.White,
                                    fontSize = 14.sp,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .defaultMinSize(minHeight = 48.dp)
                                        .clickable { pick(d.name, d.lat, d.lng) }
                                        .padding(12.dp)
                                )
                                HorizontalDivider(color = Color.White.copy(alpha = 0.06f))
                            }
                        }
                    }
                }

                else -> Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = coordText,
                        onValueChange = { coordText = it },
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("chooser_coordinates_input"),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedBorderColor = Color(0xFF00E5FF),
                            unfocusedBorderColor = Color(0xFF334155),
                            cursorColor = Color(0xFF00E5FF)
                        ),
                        placeholder = { Text("15.3694, 44.1910", fontSize = 12.sp, color = Color(0xFF94A3B8)) }
                    )
                    val parsed = parseCoordinates(coordText)
                    Button(
                        onClick = {
                            parsed?.let {
                                pick(
                                    "إحداثيات ${"%.4f".format(Locale.US, it.first)}, ${"%.4f".format(Locale.US, it.second)}",
                                    it.first,
                                    it.second
                                )
                            }
                        },
                        enabled = parsed != null,
                        modifier = Modifier
                            .fillMaxWidth()
                            .defaultMinSize(minHeight = 48.dp)
                            .testTag("chooser_coordinates_go_button")
                    ) {
                        Text("اذهب")
                    }
                }
            }
        }
    }

    if (showAddAddressPicker) {
        LandmarkAddressPicker(
            city = if (selectedCity == "الكل" || selectedCity.isBlank()) "صنعاء" else selectedCity,
            onAddressSelected = { visualAddr ->
                val newAddr = LandmarkAddress(
                    label = visualAddr.landmarkName,
                    landmarkName = visualAddr.landmarkName,
                    distanceMeters = visualAddr.approximateDistanceMeters,
                    directionLabel = visualAddr.direction.labelArabic,
                    notes = visualAddr.visualNotes,
                    pointLat = visualAddr.calculatedLatitude,
                    pointLng = visualAddr.calculatedLongitude
                )
                addressStore.add(newAddr)
                localSavedAddresses = (listOf(newAddr) + localSavedAddresses.filterNot { it.label == newAddr.label })
                onAddressSaved?.invoke(newAddr)
                showAddAddressPicker = false
            },
            onDismiss = { showAddAddressPicker = false }
        )
    }
}

/** يقبل «lat, lng» أو «lat lng» (فاصلة عشرية بنقطة)، وأرقاماً عربية-هندية. */
fun parseCoordinates(text: String): Pair<Double, Double>? {
    val t = text.trim()
        .map { c -> if (c in '٠'..'٩') ('0' + (c - '٠')) else c }
        .joinToString("")
        .replace('،', ',')
    val m = Regex("""^\s*(-?\d{1,2}(?:\.\d+)?)\s*[, ]\s*(-?\d{1,3}(?:\.\d+)?)\s*$""").find(t) ?: return null
    val la = m.groupValues[1].toDoubleOrNull() ?: return null
    val lo = m.groupValues[2].toDoubleOrNull() ?: return null
    return if (LandmarkAddress.isValid(la, lo)) la to lo else null
}
