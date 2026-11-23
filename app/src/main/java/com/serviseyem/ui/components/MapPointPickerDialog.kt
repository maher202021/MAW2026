package com.serviseyem.ui.components

import android.Manifest
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.Place
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.serviseyem.ui.screens.map.components.OfflineInteractiveMap
import com.serviseyem.ui.screens.map.utils.OfflineMapManager
import com.serviseyem.utils.LandmarkAddress
import com.serviseyem.utils.LocationCapture
import com.serviseyem.utils.VisualThemePalette
import kotlinx.coroutines.launch
import java.util.Locale

/**
 * MapPointPickerDialog
 * منتقي نقطة جغرافية على الخريطة أو عبر GPS الحقيقي، مشترك بين لوحة الإدارة والتسجيل وتعديل الملف الشخصي.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MapPointPickerDialog(
    initialLat: Double = 0.0,
    initialLng: Double = 0.0,
    initialCity: String = "صنعاء",
    themeColors: VisualThemePalette,
    onDismiss: () -> Unit,
    onConfirm: (lat: Double, lng: Double) -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val cityCenter = remember(initialCity) { OfflineMapManager.getCityCoordinates(initialCity) }

    var selectedLat by remember {
        mutableStateOf(if (LandmarkAddress.isValid(initialLat, initialLng)) initialLat else cityCenter.latitude)
    }
    var selectedLng by remember {
        mutableStateOf(if (LandmarkAddress.isValid(initialLat, initialLng)) initialLng else cityCenter.longitude)
    }
    var hasPicked by remember {
        mutableStateOf(LandmarkAddress.isValid(initialLat, initialLng))
    }
    var activeCity by remember { mutableStateOf(initialCity.ifBlank { cityCenter.nameAr }) }
    var expandedCity by remember { mutableStateOf(false) }
    var zoomScale by remember { mutableStateOf(1.2f) }
    var panOffset by remember { mutableStateOf(Offset.Zero) }
    var isFetchingGps by remember { mutableStateOf(false) }
    var statusMsg by remember { mutableStateOf<String?>(null) }

    fun captureLiveGps() {
        scope.launch {
            isFetchingGps = true
            statusMsg = "جاري التقاط موقعك الحالي بدقة..."
            val loc = LocationCapture.fresh(context)
            isFetchingGps = false
            if (loc != null) {
                selectedLat = loc.latitude
                selectedLng = loc.longitude
                hasPicked = true
                statusMsg = "✅ تم التقاط موقعك بنجاح (±${loc.accuracy.toInt()} م)"
            } else {
                statusMsg = "⚠️ تعذر التقاط GPS. انقر على الخريطة أدناه لتحديد النقطة يدوياً."
            }
        }
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { grants ->
        val granted = grants[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
            grants[Manifest.permission.ACCESS_COARSE_LOCATION] == true
        if (granted) {
            captureLiveGps()
        } else {
            statusMsg = "⚠️ لم يتم منح إذن الموقع. حدد النقطة بنقرة على الخريطة."
        }
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Card(
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A)),
            border = BorderStroke(1.dp, themeColors.accent.copy(alpha = 0.4f)),
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .fillMaxHeight(0.82f)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        Icon(Icons.Default.Place, contentDescription = null, tint = themeColors.accent)
                        Text(
                            "تحديد الموقع الجغرافي",
                            color = Color.White,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "إغلاق", tint = Color.White)
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    ExposedDropdownMenuBox(
                        expanded = expandedCity,
                        onExpandedChange = { expandedCity = !expandedCity },
                        modifier = Modifier.weight(1f)
                    ) {
                        OutlinedTextField(
                            value = activeCity,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("الانتقال السريع إلى مدينة", fontSize = 11.sp) },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedCity) },
                            singleLine = true,
                            modifier = Modifier
                                .menuAnchor()
                                .fillMaxWidth()
                        )
                        ExposedDropdownMenu(
                            expanded = expandedCity,
                            onDismissRequest = { expandedCity = false }
                        ) {
                            OfflineMapManager.allCities().forEach { cityItem ->
                                DropdownMenuItem(
                                    text = { Text(cityItem.nameAr) },
                                    onClick = {
                                        activeCity = cityItem.nameAr
                                        selectedLat = cityItem.latitude
                                        selectedLng = cityItem.longitude
                                        expandedCity = false
                                    }
                                )
                            }
                        }
                    }

                    Button(
                        onClick = {
                            if (LocationCapture.hasLocationPermission(context)) {
                                captureLiveGps()
                            } else {
                                permissionLauncher.launch(
                                    arrayOf(
                                        Manifest.permission.ACCESS_FINE_LOCATION,
                                        Manifest.permission.ACCESS_COARSE_LOCATION
                                    )
                                )
                            }
                        },
                        enabled = !isFetchingGps,
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981)),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.height(52.dp)
                    ) {
                        if (isFetchingGps) {
                            CircularProgressIndicator(
                                color = Color.White,
                                strokeWidth = 2.dp,
                                modifier = Modifier.size(18.dp)
                            )
                        } else {
                            Icon(Icons.Default.MyLocation, contentDescription = "موقعي", tint = Color.White)
                        }
                    }
                }

                Text(
                    text = "انقر على أي نقطة في الخريطة لتثبيت الموقع، أو اسحب بإصبعين للتكبير والتنقل:",
                    color = Color.LightGray,
                    fontSize = 11.sp
                )

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .clip(RoundedCornerShape(14.dp))
                ) {
                    OfflineInteractiveMap(
                        userCoords = selectedLat to selectedLng,
                        nearbyProviders = emptyList(),
                        nearbyStores = emptyList(),
                        nearbyProperties = emptyList(),
                        dynamicOffsets = emptyMap(),
                        selectedCity = activeCity,
                        zoomScale = zoomScale,
                        onZoomScaleChange = { zoomScale = it },
                        panOffset = panOffset,
                        onPanOffsetChange = { panOffset = it },
                        selectedEntity = null,
                        onProviderSelected = {},
                        onStoreSelected = {},
                        onPropertySelected = {},
                        onDeselect = {},
                        pickMode = true,
                        onMapPicked = { lat, lng ->
                            selectedLat = lat
                            selectedLng = lng
                            hasPicked = true
                            statusMsg = "✅ تم تحديد النقطة على الخريطة"
                        },
                        modifier = Modifier.fillMaxSize()
                    )
                }

                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = Color(0xFF1E293B),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                        Text(
                            text = "الإحداثيات المحددة: ${String.format(Locale.US, "%.5f", selectedLat)}, ${String.format(Locale.US, "%.5f", selectedLng)}",
                            color = if (hasPicked) Color(0xFF34D399) else themeColors.accent,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                        statusMsg?.let { msg ->
                            Text(text = msg, color = Color.LightGray, fontSize = 10.5.sp)
                        }
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f).height(48.dp)
                    ) {
                        Text("إلغاء", color = Color.White)
                    }
                    Button(
                        onClick = { onConfirm(selectedLat, selectedLng) },
                        enabled = LandmarkAddress.isValid(selectedLat, selectedLng),
                        colors = ButtonDefaults.buttonColors(containerColor = themeColors.accent, contentColor = Color.Black),
                        modifier = Modifier.weight(1f).height(48.dp)
                    ) {
                        Text("اعتماد النقطة ✓", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
