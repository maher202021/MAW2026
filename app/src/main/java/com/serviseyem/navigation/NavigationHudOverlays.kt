package com.serviseyem.navigation

import android.content.Context
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.serviseyem.utils.LocationStatus
import com.serviseyem.utils.OfflinePlacesDb

@Composable
internal fun NavigationTopHudColumn(
    hasValhallaFiles: Boolean,
    isVoiceMuted: Boolean,
    statusBannerText: String?,
    isGpsWeak: Boolean,
    searchQuery: String,
    isSearching: Boolean,
    searchResults: List<NavigationSearchResult>,
    searchMessage: String?,
    canDownloadPlacesFromSearch: Boolean,
    nextTurnManeuver: ManeuverType,
    currentInstruction: String,
    targetName: String,
    onBackClick: () -> Unit,
    onOpenValhallaDialog: () -> Unit,
    onOpenDestChooser: () -> Unit,
    onToggleMute: () -> Unit,
    onDismissBanner: () -> Unit,
    onSearchQueryChange: (String) -> Unit,
    onClearSearch: () -> Unit,
    onSelectSearchResult: (NavigationSearchResult) -> Unit,
    onOpenPlacesDownload: () -> Unit,
    locationStatus: LocationStatus? = null,
    onLocationStatusAction: () -> Unit = {}
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .padding(horizontal = 14.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Action bar: Back, Mode Badge, Change Dest & Mute
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            IconButton(
                onClick = onBackClick,
                modifier = Modifier
                    .size(48.dp)
                    .background(Color(0xFF0B1220).copy(alpha = 0.92f), RoundedCornerShape(14.dp))
            ) {
                Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "رجوع", tint = Color.White)
            }

            Surface(
                shape = RoundedCornerShape(20.dp),
                color = if (hasValhallaFiles) Color(0xFF064E3B).copy(alpha = 0.85f) else Color(0xFF1E3A8A).copy(alpha = 0.85f),
                border = BorderStroke(1.dp, if (hasValhallaFiles) Color(0xFF10B981) else Color(0xFF5B86FF)),
                modifier = Modifier.defaultMinSize(minHeight = 40.dp).clickable(onClick = onOpenValhallaDialog)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(if (hasValhallaFiles) Color(0xFF10B981) else Color(0xFF00E5FF))
                    )
                    Text(
                        text = if (hasValhallaFiles) "ملاحة أوفلاين (فالدالا)" else "تقريبي • أضف أوفلاين 📥",
                        color = Color.White,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                IconButton(
                    onClick = onOpenDestChooser,
                    modifier = Modifier
                        .size(48.dp)
                        .background(Color(0xFF0B1220).copy(alpha = 0.92f), RoundedCornerShape(14.dp))
                ) {
                    Icon(
                        imageVector = Icons.Default.AltRoute,
                        contentDescription = "تغيير الوجهة",
                        tint = Color(0xFF5B86FF)
                    )
                }

                IconButton(
                    onClick = onToggleMute,
                    modifier = Modifier
                        .size(48.dp)
                        .background(Color(0xFF0B1220).copy(alpha = 0.92f), RoundedCornerShape(14.dp))
                ) {
                    Icon(
                        imageVector = if (isVoiceMuted) Icons.Default.VolumeOff else Icons.Default.VolumeUp,
                        contentDescription = "الصوت",
                        tint = if (isVoiceMuted) Color.Gray else Color(0xFF00E5FF)
                    )
                }
            }
        }

        statusBannerText?.let { banner ->
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = Color(0xFF065F46).copy(alpha = 0.92f),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(banner, color = Color.White, fontSize = 11.5.sp, modifier = Modifier.weight(1f))
                    TextButton(onClick = onDismissBanner) {
                        Text("حسناً", color = Color(0xFFA7F3D0), fontSize = 11.sp)
                    }
                }
            }
        }

        // GPS / LocationHealth Status Warning Banner
        if (locationStatus != null && locationStatus !is LocationStatus.Good) {
            val (msg, bg, clickable) = when (locationStatus) {
                is LocationStatus.NoPermission -> Triple("فعّل إذن الموقع للانطلاق من موقعك الفعلي", Color(0xFFB45309), true)
                is LocationStatus.ProviderOff -> Triple("خدمة GPS متوقفة — اضغط لتشغيلها", Color(0xFFB91C1C), true)
                is LocationStatus.Searching -> Triple("جاري البحث عن إشارة GPS لتحديد نقطة البداية...", Color(0xFF1E3A8A), false)
                is LocationStatus.Weak -> Triple("إشارة GPS ضعيفة (±${locationStatus.accuracyM.toInt()}م)", Color(0xFFB45309), false)
                is LocationStatus.Good -> Triple("", Color.Transparent, false)
            }
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = bg.copy(alpha = 0.92f),
                modifier = Modifier
                    .fillMaxWidth()
                    .let { if (clickable) it.clickable(onClick = onLocationStatusAction) else it }
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(Icons.Default.GpsNotFixed, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                    Text(msg, color = Color.White, fontSize = 11.5.sp, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
                    if (clickable) {
                        Text("تفعيل", color = Color(0xFF00E5FF), fontSize = 11.5.sp, fontWeight = FontWeight.Black)
                    }
                }
            }
        } else if (isGpsWeak) {
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = Color(0xFFEF4444).copy(alpha = 0.9f),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(Icons.Default.Warning, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                    Text("إشارة GPS ضعيفة - جاري المتابعة بالتقدير الملاحي", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        // Search Bar
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = Color(0xFF0B1220).copy(alpha = 0.95f),
            border = BorderStroke(1.dp, Color(0xFF3A6BFF).copy(alpha = 0.45f)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = onSearchQueryChange,
                    placeholder = { Text("بحث عن مكان، شارع، أو خدمة لتغيير الوجهة...", color = Color.White.copy(alpha = 0.6f), fontSize = 12.5.sp) },
                    textStyle = androidx.compose.ui.text.TextStyle(
                        color = Color.White,
                        fontSize = 13.5.sp,
                        fontWeight = FontWeight.Medium
                    ),
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = Color(0xFF00E5FF)) },
                    trailingIcon = {
                        if (isSearching) {
                            CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp, color = Color(0xFF00E5FF))
                        } else if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = onClearSearch) {
                                Icon(Icons.Default.Close, contentDescription = "مسح", tint = Color.Gray)
                            }
                        }
                    },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent,
                        focusedBorderColor = Color.Transparent,
                        unfocusedBorderColor = Color.Transparent,
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        cursorColor = Color(0xFF00E5FF)
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .defaultMinSize(minHeight = 50.dp)
                )

                if (searchResults.isNotEmpty()) {
                    HorizontalDivider(color = Color.White.copy(alpha = 0.1f))
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 200.dp)
                    ) {
                        items(searchResults) { result ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { onSelectSearchResult(result) }
                                    .padding(horizontal = 14.dp, vertical = 10.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Icon(
                                    imageVector = if (result.isLocal) Icons.Default.Place else Icons.Default.LocationSearching,
                                    contentDescription = null,
                                    tint = if (result.isLocal) Color(0xFF10B981) else Color(0xFF38BDF8),
                                    modifier = Modifier.size(20.dp)
                                )
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(text = result.title, color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                    Text(text = result.subtitle, color = Color.Gray, fontSize = 11.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                }
                            }
                            HorizontalDivider(color = Color.White.copy(alpha = 0.05f))
                        }
                    }
                } else if (searchMessage != null) {
                    HorizontalDivider(color = Color.White.copy(alpha = 0.1f))
                    Column(
                        modifier = Modifier.padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = searchMessage,
                            color = Color(0xFFF59E0B),
                            fontSize = 12.sp
                        )
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedButton(
                                onClick = onOpenDestChooser,
                                modifier = Modifier.defaultMinSize(minHeight = 48.dp)
                            ) {
                                Text("اختر وجهة بديلة 🎯", color = Color.White, fontSize = 11.5.sp)
                            }
                            if (canDownloadPlacesFromSearch) {
                                Button(
                                    onClick = onOpenPlacesDownload,
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981)),
                                    modifier = Modifier.defaultMinSize(minHeight = 48.dp)
                                ) {
                                    Text("تحميل الأماكن", color = Color.Black, fontSize = 11.5.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            }
        }

        // Dynamic Turn-by-Turn Card
        Surface(
            shape = RoundedCornerShape(18.dp),
            color = Color(0xFF0B1220).copy(alpha = 0.96f),
            border = BorderStroke(1.dp, Color(0xFF3A6BFF).copy(alpha = 0.55f)),
            shadowElevation = 8.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier.padding(14.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(52.dp)
                        .background(
                            Brush.verticalGradient(listOf(Color(0xFF5B86FF), Color(0xFF3A6BFF), Color(0xFF2851D8))),
                            RoundedCornerShape(14.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = when (nextTurnManeuver) {
                            ManeuverType.TURN_RIGHT, ManeuverType.SLIGHT_RIGHT, ManeuverType.SHARP_RIGHT -> Icons.Default.TurnRight
                            ManeuverType.TURN_LEFT, ManeuverType.SLIGHT_LEFT, ManeuverType.SHARP_LEFT -> Icons.Default.TurnLeft
                            ManeuverType.U_TURN -> Icons.Default.Refresh
                            ManeuverType.ROUNDABOUT -> Icons.Default.ChangeCircle
                            ManeuverType.DESTINATION -> Icons.Default.Flag
                            else -> Icons.Default.ArrowUpward
                        },
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(32.dp)
                    )
                }

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = currentInstruction,
                        color = Color.White,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.ExtraBold,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "الوجهة: $targetName",
                        color = Color(0xFF38BDF8),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    }
}

@Composable
internal fun NavigationFloatingWidgets(
    speedKmh: Int,
    is3DMode: Boolean = true,
    onToggle3D: () -> Unit = {},
    onReportHazardClick: () -> Unit = {},
    onRecenterClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(10.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // 3D / 2D Perspective Toggle Button
        Surface(
            shape = CircleShape,
            color = Color(0xFF0F172A).copy(alpha = 0.94f),
            border = BorderStroke(1.5.dp, if (is3DMode) Color(0xFF00E5FF) else Color(0xFF64748B)),
            modifier = Modifier
                .size(46.dp)
                .clickable { onToggle3D() }
        ) {
            Box(contentAlignment = Alignment.Center) {
                Text(
                    text = if (is3DMode) "3D" else "2D",
                    color = if (is3DMode) Color(0xFF00E5FF) else Color.White,
                    fontWeight = FontWeight.Black,
                    fontSize = 13.sp
                )
            }
        }

        // Road Hazard Report Button (⚠️ تنبيه طريق)
        IconButton(
            onClick = onReportHazardClick,
            modifier = Modifier
                .size(46.dp)
                .background(Color(0xFF7F1D1D).copy(alpha = 0.94f), CircleShape)
                .clip(CircleShape)
        ) {
            Icon(
                imageVector = Icons.Default.Warning,
                contentDescription = "إبلاغ عن تنبيه أو عائق في الطريق",
                tint = Color(0xFFFCD34D),
                modifier = Modifier.size(22.dp)
            )
        }

        Surface(
            shape = CircleShape,
            color = Color(0xFF0F172A).copy(alpha = 0.9f),
            border = BorderStroke(1.5.dp, Color(0xFF00E5FF)),
            modifier = Modifier.size(52.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "$speedKmh",
                    color = Color.White,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Black
                )
                Text("كم/س", color = Color(0xFF00E5FF), fontSize = 8.sp, fontWeight = FontWeight.Bold)
            }
        }

        IconButton(
            onClick = onRecenterClick,
            modifier = Modifier
                .size(48.dp)
                .background(Color(0xFF0B1220).copy(alpha = 0.92f), CircleShape)
                .clip(CircleShape)
        ) {
            Icon(imageVector = Icons.Default.MyLocation, contentDescription = "موقعي", tint = Color(0xFF00E5FF))
        }
    }
}

@Composable
internal fun NavigationPickOnMapBar(
    context: Context,
    pickedNavPoint: Pair<Double, Double>?,
    onCancel: () -> Unit,
    onConfirmStart: (label: String, lat: Double, lng: Double) -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = Color(0xF50F172A),
        border = BorderStroke(1.5.dp, Color(0xFF00E5FF)),
        modifier = modifier
    ) {
        if (pickedNavPoint == null) {
            Row(
                modifier = Modifier.padding(14.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    "اضغط على الخريطة لتحديد الوجهة الجديدة 🎯",
                    color = Color.White,
                    fontSize = 12.5.sp,
                    modifier = Modifier.weight(1f)
                )
                TextButton(onClick = onCancel) {
                    Text("إلغاء", color = Color(0xFFEF4444))
                }
            }
        } else {
            val (la, lo) = pickedNavPoint
            val nearest by produceState<String?>(initialValue = null, key1 = la, key2 = lo) {
                value = kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
                    OfflinePlacesDb.nearest(context, la, lo)
                }
            }
            val label = nearest?.let { "قرب $it" } ?: "نقطة على الخريطة"
            Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("الوجهة: $label", color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(
                        onClick = { onConfirmStart(label, la, lo) },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981)),
                        modifier = Modifier.weight(1f).defaultMinSize(minHeight = 48.dp)
                    ) {
                        Text("ابدأ الملاحة", color = Color.Black, fontWeight = FontWeight.Bold)
                    }
                    OutlinedButton(
                        onClick = onCancel,
                        modifier = Modifier.defaultMinSize(minHeight = 48.dp)
                    ) {
                        Text("إلغاء", color = Color.White)
                    }
                }
            }
        }
    }
}
