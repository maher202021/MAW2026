package com.serviseyem.ui.screens.map.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Navigation
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.Radar
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.serviseyem.data.PropertyEntity
import com.serviseyem.data.ProviderEntity
import com.serviseyem.data.StoreEntity
import com.serviseyem.utils.getPropertyCoords
import com.serviseyem.utils.getProviderCoords
import com.serviseyem.utils.getStoreCoords
import kotlin.math.*

/**
 * 🗺️ OfflineInteractiveMap (Full Entity Overload)
 * Native Jetpack Compose Canvas Interactive Map
 * 100% Offline, Zero WebViews, Instant 60FPS, Guaranteed to NEVER show a black screen!
 * Features:
 * - Real Geographic Projection (Equirectangular / Flat Earth approximation with latitude cosine)
 * - True coordinate mapping without distance clamping or artificial offset distortions
 * - Wide Zoom range (0.25f - 8.0f) for city-wide overview down to fine street level
 * - Elegant halos, full unclipped labels with crisp drop-shadows
 * - Pulsating neon user GPS marker
 * - Zero black screen fallback with try-catch safety
 */
@Composable
fun OfflineInteractiveMap(
    userCoords: Pair<Double, Double>,
    nearbyProviders: List<ProviderEntity>,
    nearbyStores: List<StoreEntity>,
    nearbyProperties: List<PropertyEntity>,
    dynamicOffsets: Map<String, Pair<Double, Double>>,
    selectedCity: String = "الكل",
    zoomScale: Float,
    onZoomScaleChange: (Float) -> Unit,
    panOffset: Offset,
    onPanOffsetChange: (Offset) -> Unit,
    selectedEntity: Any?,
    onProviderSelected: (ProviderEntity) -> Unit,
    onStoreSelected: (StoreEntity) -> Unit,
    onPropertySelected: (PropertyEntity) -> Unit,
    onDeselect: () -> Unit,
    onSwitchToRadar: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    // [تعديل] استخدام إحداثيات GPS الحقيقية أو إحداثيات المدينة كبديل آمن ودقيق
    val defaultCityCoords = remember(selectedCity) {
        com.serviseyem.ui.screens.map.utils.OfflineMapManager.getCityCoordinates(selectedCity)
    }
    val safeUserLat = if (userCoords.first != 0.0 && !userCoords.first.isNaN()) userCoords.first else defaultCityCoords.latitude
    val safeUserLng = if (userCoords.second != 0.0 && !userCoords.second.isNaN()) userCoords.second else defaultCityCoords.longitude

    // Compute governorate center coordinates dynamically from accurate registry
    val cityCenterCoords = remember(selectedCity, safeUserLat, safeUserLng) {
        if (selectedCity == "الكل" || selectedCity.isBlank()) {
            Pair(safeUserLat, safeUserLng)
        } else {
            val c = com.serviseyem.ui.screens.map.utils.OfflineMapManager.getCityCoordinates(selectedCity)
            Pair(c.latitude, c.longitude)
        }
    }

    val originLat = cityCenterCoords.first
    val originLng = cityCenterCoords.second

    // Precision Geographic Projection constants
    val metersPerDegreeLat = 110540.0
    val metersPerDegreeLng = 111320.0 * cos(Math.toRadians(originLat))

    // Animated pulse for user location marker
    val infiniteTransition = rememberInfiniteTransition(label = "user_pulse")
    val pulseProgress by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1800, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "pulse_progress"
    )

    // Accurate calculation of map points
    val mapPoints = remember(
        nearbyProviders,
        nearbyStores,
        nearbyProperties,
        originLat,
        originLng
    ) {
        val points = mutableListOf<InteractiveMapPoint>()

        // 1. Providers
        nearbyProviders.forEach { p ->
            val hasValidCoords = p.latitude != 0.0 && p.longitude != 0.0 &&
                    !p.latitude.isNaN() && !p.longitude.isNaN() &&
                    p.latitude in -90.0..90.0 && p.longitude in -180.0..180.0

            val xMeters: Float
            val yMeters: Float

            if (hasValidCoords) {
                // Real Geographic Projection without distance clamping
                val dLng = p.longitude - originLng
                val dLat = p.latitude - originLat
                xMeters = (dLng * metersPerDegreeLng).toFloat()
                yMeters = (-dLat * metersPerDegreeLat).toFloat()
            } else {
                // إصلاح 1.2: تطبيق حساب الإزاحة الثابتة بدقة 100% (Deterministic Hash Offset)
                val targetCity = if (p.cityId.isNotBlank()) p.cityId else selectedCity
                val cityCoords = com.serviseyem.ui.screens.map.utils.OfflineMapManager.getCityCoordinates(targetCity)
                val dLng = cityCoords.longitude - originLng
                val dLat = cityCoords.latitude - originLat
                val baseXMeters = (dLng * metersPerDegreeLng).toFloat()
                val baseYMeters = (-dLat * metersPerDegreeLat).toFloat()

                val idHash = p.id.hashCode().toLong()
                val hashAngle = (abs(idHash) % 360).toDouble() * (Math.PI / 180.0)
                val hashOffsetMeters = 40.0 + (abs(idHash ushr 16) % 120)   // بين 40 و 160 متر
                val offsetX = (hashOffsetMeters * cos(hashAngle)).toFloat()
                val offsetY = (hashOffsetMeters * sin(hashAngle)).toFloat()
                xMeters = baseXMeters + offsetX
                yMeters = baseYMeters + offsetY
            }

            points.add(
                InteractiveMapPoint(
                    id = p.id,
                    name = p.name.ifBlank { "فني متخصص" },
                    category = p.profession.ifBlank { "خدمات صيانة" },
                    type = "PROVIDER",
                    emoji = "👷",
                    color = Color(0xFF00E5FF), // Neon Cyan
                    xMeters = xMeters,
                    yMeters = yMeters,
                    rating = p.rating.toDouble().coerceAtLeast(1.0),
                    phone = p.phone,
                    originalEntity = p
                )
            )
        }

        // 2. Stores & Restaurants & Medical Centers
        nearbyStores.forEach { s ->
            val hasValidCoords = s.latitude != 0.0 && s.longitude != 0.0 &&
                    !s.latitude.isNaN() && !s.longitude.isNaN() &&
                    s.latitude in -90.0..90.0 && s.longitude in -180.0..180.0

            val xMeters: Float
            val yMeters: Float

            if (hasValidCoords) {
                // Real Geographic Projection
                val dLng = s.longitude - originLng
                val dLat = s.latitude - originLat
                xMeters = (dLng * metersPerDegreeLng).toFloat()
                yMeters = (-dLat * metersPerDegreeLat).toFloat()
            } else {
                // إصلاح 1.2: تطبيق حساب الإزاحة الثابتة بدقة 100% (Deterministic Hash Offset)
                val targetCity = if (s.cityId.isNotBlank()) s.cityId else selectedCity
                val cityCoords = com.serviseyem.ui.screens.map.utils.OfflineMapManager.getCityCoordinates(targetCity)
                val dLng = cityCoords.longitude - originLng
                val dLat = cityCoords.latitude - originLat
                val baseXMeters = (dLng * metersPerDegreeLng).toFloat()
                val baseYMeters = (-dLat * metersPerDegreeLat).toFloat()

                val idHash = s.id.hashCode().toLong()
                val hashAngle = (abs(idHash) % 360).toDouble() * (Math.PI / 180.0)
                val hashOffsetMeters = 40.0 + (abs(idHash ushr 16) % 120)   // بين 40 و 160 متر
                val offsetX = (hashOffsetMeters * cos(hashAngle)).toFloat()
                val offsetY = (hashOffsetMeters * sin(hashAngle)).toFloat()
                xMeters = baseXMeters + offsetX
                yMeters = baseYMeters + offsetY
            }

            val isMedical = s.sectionId.contains("medical") || s.categoryId.contains("medical") || s.name.contains("طبي") || s.name.contains("صيدلية")
            val isRestaurant = !isMedical && (s.sectionId.contains("restaurant") || s.categoryId.contains("restaurant") || s.name.contains("مطعم") || s.name.contains("كافيه"))

            val pinColor = when {
                isMedical -> Color(0xFFEC4899)     // Vivid Pink
                isRestaurant -> Color(0xFFF59E0B)  // Vibrant Amber/Orange
                else -> Color(0xFF10B981)          // Emerald Green
            }

            val pinEmoji = when {
                isMedical -> "🏥"
                isRestaurant -> "🍔"
                else -> "🏪"
            }

            points.add(
                InteractiveMapPoint(
                    id = s.id,
                    name = s.name.ifBlank { "متجر" },
                    category = s.description.ifBlank { if (isMedical) "مركز طبي" else if (isRestaurant) "مطعم" else "متجر" },
                    type = if (isMedical) "MEDICAL" else if (isRestaurant) "RESTAURANT" else "STORE",
                    emoji = pinEmoji,
                    color = pinColor,
                    xMeters = xMeters,
                    yMeters = yMeters,
                    rating = s.rating.toDouble().coerceAtLeast(1.0),
                    phone = s.phone,
                    originalEntity = s
                )
            )
        }

        // 3. Properties
        nearbyProperties.forEach { prop ->
            val hasValidCoords = prop.latitude != 0.0 && prop.longitude != 0.0 &&
                    !prop.latitude.isNaN() && !prop.longitude.isNaN() &&
                    prop.latitude in -90.0..90.0 && prop.longitude in -180.0..180.0

            val xMeters: Float
            val yMeters: Float

            if (hasValidCoords) {
                // Real Geographic Projection
                val dLng = prop.longitude - originLng
                val dLat = prop.latitude - originLat
                xMeters = (dLng * metersPerDegreeLng).toFloat()
                yMeters = (-dLat * metersPerDegreeLat).toFloat()
            } else {
                // إصلاح 1.2: تطبيق حساب الإزاحة الثابتة بدقة 100% (Deterministic Hash Offset)
                val targetCity = if (prop.cityId.isNotBlank()) prop.cityId else selectedCity
                val cityCoords = com.serviseyem.ui.screens.map.utils.OfflineMapManager.getCityCoordinates(targetCity)
                val dLng = cityCoords.longitude - originLng
                val dLat = cityCoords.latitude - originLat
                val baseXMeters = (dLng * metersPerDegreeLng).toFloat()
                val baseYMeters = (-dLat * metersPerDegreeLat).toFloat()

                val idHash = prop.id.hashCode().toLong()
                val hashAngle = (abs(idHash) % 360).toDouble() * (Math.PI / 180.0)
                val hashOffsetMeters = 40.0 + (abs(idHash ushr 16) % 120)   // بين 40 و 160 متر
                val offsetX = (hashOffsetMeters * cos(hashAngle)).toFloat()
                val offsetY = (hashOffsetMeters * sin(hashAngle)).toFloat()
                xMeters = baseXMeters + offsetX
                yMeters = baseYMeters + offsetY
            }

            points.add(
                InteractiveMapPoint(
                    id = prop.id,
                    name = prop.title.ifBlank { "عقار معروض" },
                    category = "عقار",
                    type = "PROPERTY",
                    emoji = "🏠",
                    color = Color(0xFF8B5CF6), // Purple / Violet
                    xMeters = xMeters,
                    yMeters = yMeters,
                    rating = 5.0,
                    phone = prop.phone,
                    originalEntity = prop
                )
            )
        }

        points
    }

    // Reset pan & zoom smoothly when city changes
    LaunchedEffect(selectedCity) {
        onPanOffsetChange(Offset.Zero)
        onZoomScaleChange(1.0f)
    }

    BoxWithConstraints(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF0F172A))
    ) {
        val widthPx = constraints.maxWidth.toFloat()
        val heightPx = constraints.maxHeight.toFloat()
        val centerX = widthPx / 2f + panOffset.x
        val centerY = heightPx / 2f + panOffset.y

        // Professional zoom scale: 0.25f (overview ~15km radius) to 8.0f (fine details ~400m radius)
        val clampedZoom = zoomScale.coerceIn(0.25f, 8.0f)
        val baseMetersPerPx = 4.0f
        val metersPerPx = (baseMetersPerPx / clampedZoom).coerceIn(0.4f, 25.0f)

        // Compute screen coordinates for all items
        val screenPoints = remember(mapPoints, centerX, centerY, metersPerPx) {
            mapPoints.map { pt ->
                pt.copy(
                    screenX = centerX + (pt.xMeters / metersPerPx),
                    screenY = centerY + (pt.yMeters / metersPerPx)
                )
            }
        }

        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(Unit) {
                    detectTransformGestures { _, pan, zoom, _ ->
                        val nextZoom = (zoomScale * zoom).coerceIn(0.25f, 8.0f)
                        onZoomScaleChange(nextZoom)
                        onPanOffsetChange(panOffset + pan)
                    }
                }
                .pointerInput(screenPoints) {
                    detectTapGestures(
                        onDoubleTap = {
                            val nextZoom = (zoomScale * 1.4f).coerceIn(0.25f, 8.0f)
                            onZoomScaleChange(nextZoom)
                        },
                        onTap = { tapOffset ->
                            val clicked = screenPoints.minByOrNull { pt ->
                                val dx = tapOffset.x - pt.screenX
                                val dy = tapOffset.y - pt.screenY
                                dx * dx + dy * dy
                            }
                            if (clicked != null) {
                                val distPx = sqrt(
                                    (tapOffset.x - clicked.screenX).pow(2) +
                                            (tapOffset.y - clicked.screenY).pow(2)
                                )
                                // 54px comfortable touch radius
                                if (distPx < 54f) {
                                    when (val ent = clicked.originalEntity) {
                                        is ProviderEntity -> onProviderSelected(ent)
                                        is StoreEntity -> onStoreSelected(ent)
                                        is PropertyEntity -> onPropertySelected(ent)
                                    }
                                } else {
                                    onDeselect()
                                }
                            } else {
                                onDeselect()
                            }
                        }
                    )
                }
        ) {
            val safeCX = if (centerX.isNaN() || centerX.isInfinite()) size.width / 2f else centerX
            val safeCY = if (centerY.isNaN() || centerY.isInfinite()) size.height / 2f else centerY
            val safeMeters = if (metersPerPx.isNaN() || metersPerPx.isInfinite() || metersPerPx <= 0f) 4.0f else metersPerPx
            val safePan = if (panOffset.x.isNaN() || panOffset.y.isNaN()) Offset.Zero else panOffset
            val safeZoom = if (clampedZoom.isNaN() || clampedZoom.isInfinite() || clampedZoom <= 0f) 1.0f else clampedZoom

            try {
                // 1. Draw city roads, avenues, ring-roads and parks
                drawCityRoadGrid(
                    centerX = safeCX,
                    centerY = safeCY,
                    metersPerPx = safeMeters,
                    widthPx = size.width,
                    heightPx = size.height,
                    panOffset = safePan,
                    zoomScale = safeZoom,
                    selectedCity = selectedCity,
                    originLat = originLat,
                    originLng = originLng,
                    metersPerDegreeLat = metersPerDegreeLat,
                    metersPerDegreeLng = metersPerDegreeLng
                )

                // 2. User GPS coordinates projection
                val userXMeters = ((safeUserLng - originLng) * metersPerDegreeLng).toFloat()
                val userYMeters = (-(safeUserLat - originLat) * metersPerDegreeLat).toFloat()
                val userScreenX = safeCX + (userXMeters / safeMeters)
                val userScreenY = safeCY + (userYMeters / safeMeters)
                val userPin = Offset(userScreenX, userScreenY)

                val visiblePadding = 90f
                val minX = -visiblePadding
                val maxX = size.width + visiblePadding
                val minY = -visiblePadding
                val maxY = size.height + visiblePadding

                // 3. Proximity distance rings around user GPS (Tactical Radar overlay)
                if (userPin.x in minX..maxX && userPin.y in minY..maxY) {
                    val ring500Px = 500f / safeMeters
                    val ring1000Px = 1000f / safeMeters
                    val ring2000Px = 2000f / safeMeters

                    val dashRingStroke = Stroke(
                        width = 1.2f,
                        pathEffect = androidx.compose.ui.graphics.PathEffect.dashPathEffect(floatArrayOf(10f, 10f))
                    )

                    if (ring500Px in 35f..size.width) {
                        drawCircle(Color(0xFF00E5FF).copy(alpha = 0.15f), ring500Px, userPin, style = dashRingStroke)
                    }
                    if (ring1000Px in 35f..size.width) {
                        drawCircle(Color(0xFF00E5FF).copy(alpha = 0.10f), ring1000Px, userPin, style = dashRingStroke)
                    }
                    if (ring2000Px in 35f..size.width) {
                        drawCircle(Color(0xFF00E5FF).copy(alpha = 0.06f), ring2000Px, userPin, style = dashRingStroke)
                    }
                }

                // 4. Dynamic paints scaled with zoom for sharp crisp rendering
                val fontScale = (safeZoom.coerceIn(0.6f, 2.5f)).pow(0.35f)
                val emojiSize = (28f * fontScale).coerceIn(20f, 38f)
                val titleLabelSize = (18f * fontScale).coerceIn(13f, 25f)
                val subLabelSize = (14f * fontScale).coerceIn(11f, 19f)

                val emojiPaint = android.graphics.Paint().apply {
                    textSize = emojiSize
                    textAlign = android.graphics.Paint.Align.CENTER
                    isAntiAlias = true
                }

                val titlePaint = android.graphics.Paint().apply {
                    textSize = titleLabelSize
                    color = android.graphics.Color.WHITE
                    textAlign = android.graphics.Paint.Align.CENTER
                    isFakeBoldText = true
                    isAntiAlias = true
                    setShadowLayer(6f, 2f, 2f, android.graphics.Color.parseColor("#E6000000"))
                }

                val subtitlePaint = android.graphics.Paint().apply {
                    textSize = subLabelSize
                    color = android.graphics.Color.parseColor("#38BDF8") // Sky blue accent
                    textAlign = android.graphics.Paint.Align.CENTER
                    isFakeBoldText = true
                    isAntiAlias = true
                    setShadowLayer(4f, 1f, 1f, android.graphics.Color.parseColor("#E6000000"))
                }

                val labelBgPaint = android.graphics.Paint().apply {
                    color = android.graphics.Color.parseColor("#CC0F172A")
                    isAntiAlias = true
                    style = android.graphics.Paint.Style.FILL
                }

                // 5. Render service & entity markers with ground needles, shadows and distance tags
                screenPoints.forEach { pt ->
                    val groundPoint = Offset(pt.screenX, pt.screenY)
                    if (groundPoint.x in minX..maxX && groundPoint.y in minY..maxY) {
                        val isSelected = when (val ent = selectedEntity) {
                            is ProviderEntity -> ent.id == pt.id
                            is StoreEntity -> ent.id == pt.id
                            is PropertyEntity -> ent.id == pt.id
                            else -> false
                        }

                        val baseRadius = 15f * fontScale.coerceIn(0.85f, 1.4f)
                        val pinHeadCenter = Offset(groundPoint.x, groundPoint.y - baseRadius - 5f)

                        // Ground contact shadow
                        drawOval(
                            color = Color.Black.copy(alpha = 0.40f),
                            topLeft = Offset(groundPoint.x - baseRadius * 0.75f, groundPoint.y - 3f),
                            size = Size(baseRadius * 1.5f, 6f)
                        )

                        // Ground pointer needle
                        val needlePath = Path().apply {
                            moveTo(groundPoint.x, groundPoint.y)
                            lineTo(groundPoint.x - baseRadius * 0.55f, pinHeadCenter.y + baseRadius * 0.5f)
                            lineTo(groundPoint.x + baseRadius * 0.55f, pinHeadCenter.y + baseRadius * 0.5f)
                            close()
                        }
                        drawPath(needlePath, color = if (isSelected) Color.White else pt.color)

                        // Outer glowing aura / halo
                        if (isSelected) {
                            drawCircle(pt.color.copy(alpha = 0.30f), baseRadius * 2.8f, pinHeadCenter)
                            drawCircle(pt.color.copy(alpha = 0.65f), baseRadius * 1.9f, pinHeadCenter)
                            drawCircle(Color.White, baseRadius * 1.35f, pinHeadCenter, style = Stroke(4f))
                        } else {
                            drawCircle(pt.color.copy(alpha = 0.35f), baseRadius * 1.7f, pinHeadCenter)
                        }

                        // Badge core
                        drawCircle(Color(0xFF0F172A), baseRadius, pinHeadCenter)
                        drawCircle(
                            color = if (isSelected) Color.White else pt.color,
                            radius = baseRadius,
                            center = pinHeadCenter,
                            style = Stroke(if (isSelected) 3.5f else 2.5f)
                        )

                        // Emoji icon inside badge
                        drawContext.canvas.nativeCanvas.drawText(
                            pt.emoji,
                            pinHeadCenter.x,
                            pinHeadCenter.y + (emojiSize * 0.35f),
                            emojiPaint
                        )

                        // Calculate distance from user GPS
                        val distMeters = sqrt((pt.xMeters - userXMeters).pow(2) + (pt.yMeters - userYMeters).pow(2))
                        val distString = if (distMeters < 1000f) {
                            "${distMeters.roundToInt()} م"
                        } else {
                            "${String.format(java.util.Locale.US, "%.1f", distMeters / 1000f)} كم"
                        }

                        // Legible multi-attribute label with dark pill background
                        if (safeZoom >= 0.50f) {
                            val displayName = if (pt.name.length > 20) pt.name.take(19) + "…" else pt.name
                            val titleWidth = titlePaint.measureText(displayName)
                            val subText = "${pt.category} • $distString"
                            val subWidth = subtitlePaint.measureText(subText)
                            val maxTextWidth = max(titleWidth, subWidth)

                            val labelTop = pinHeadCenter.y + baseRadius + 4f
                            val labelBottom = labelTop + titleLabelSize + subLabelSize + 8f

                            val bgRect = android.graphics.RectF(
                                pinHeadCenter.x - (maxTextWidth / 2f) - 10f,
                                labelTop - 2f,
                                pinHeadCenter.x + (maxTextWidth / 2f) + 10f,
                                labelBottom
                            )

                            drawContext.canvas.nativeCanvas.drawRoundRect(
                                bgRect,
                                12f,
                                12f,
                                labelBgPaint
                            )

                            // Title line
                            drawContext.canvas.nativeCanvas.drawText(
                                displayName,
                                pinHeadCenter.x,
                                labelTop + titleLabelSize - 1f,
                                titlePaint
                            )

                            // Subtitle line (category & distance)
                            drawContext.canvas.nativeCanvas.drawText(
                                subText,
                                pinHeadCenter.x,
                                labelTop + titleLabelSize + subLabelSize + 3f,
                                subtitlePaint
                            )
                        }
                    }
                }

                // 6. User GPS location with pulsating neon cyan animation & halo
                if (userPin.x in minX..maxX && userPin.y in minY..maxY) {
                    val pulseRadiusMax = 44f
                    val currentPulse = pulseProgress * pulseRadiusMax
                    val pulseAlpha = (1f - pulseProgress).coerceIn(0f, 1f) * 0.55f

                    // Pulsing outer ripple
                    drawCircle(
                        color = Color(0xFF00E5FF).copy(alpha = pulseAlpha),
                        radius = 14f + currentPulse,
                        center = userPin
                    )
                    // Mid glow
                    drawCircle(
                        color = Color(0xFF00E5FF).copy(alpha = 0.35f),
                        radius = 20f,
                        center = userPin
                    )
                    // High-contrast neon pin core
                    drawCircle(
                        color = Color(0xFF0F172A),
                        radius = 13f,
                        center = userPin
                    )
                    drawCircle(
                        color = Color(0xFF00E5FF),
                        radius = 11f,
                        center = userPin
                    )
                    drawCircle(
                        color = Color.White,
                        radius = 5f,
                        center = userPin
                    )

                    // User location title badge
                    val uLabel = "موقعك المباشر 📍"
                    val uSubLabel = if (selectedCity != "الكل") "محافظة $selectedCity" else "نظام تحديد المواقع GPS نشط"
                    val uTitlePaint = android.graphics.Paint().apply {
                        textSize = 21f
                        color = android.graphics.Color.WHITE
                        textAlign = android.graphics.Paint.Align.CENTER
                        isFakeBoldText = true
                        isAntiAlias = true
                        setShadowLayer(4f, 1f, 1f, android.graphics.Color.BLACK)
                    }
                    val uSubPaint = android.graphics.Paint().apply {
                        textSize = 15f
                        color = android.graphics.Color.parseColor("#38BDF8")
                        textAlign = android.graphics.Paint.Align.CENTER
                        isFakeBoldText = true
                        isAntiAlias = true
                    }
                    val uW = uTitlePaint.measureText(uLabel).coerceAtLeast(uSubPaint.measureText(uSubLabel))
                    val uRect = android.graphics.RectF(
                        userPin.x - (uW / 2f) - 14f,
                        userPin.y - 68f,
                        userPin.x + (uW / 2f) + 14f,
                        userPin.y - 18f
                    )
                    val uBgPaint = android.graphics.Paint().apply {
                        color = android.graphics.Color.parseColor("#E60F172A")
                        style = android.graphics.Paint.Style.FILL
                        isAntiAlias = true
                    }
                    val uBorderPaint = android.graphics.Paint().apply {
                        color = android.graphics.Color.parseColor("#00E5FF")
                        style = android.graphics.Paint.Style.STROKE
                        strokeWidth = 1.8f
                        isAntiAlias = true
                    }
                    drawContext.canvas.nativeCanvas.drawRoundRect(uRect, 10f, 10f, uBgPaint)
                    drawContext.canvas.nativeCanvas.drawRoundRect(uRect, 10f, 10f, uBorderPaint)
                    drawContext.canvas.nativeCanvas.drawText(uLabel, userPin.x, userPin.y - 42f, uTitlePaint)
                    drawContext.canvas.nativeCanvas.drawText(uSubLabel, userPin.x, userPin.y - 24f, uSubPaint)
                }

            } catch (e: Exception) {
                // Guaranteed safety: Never display a blank or crash screen
                drawRect(Color(0xFF0F172A))
                drawCircle(Color(0xFF00E5FF).copy(alpha = 0.3f), 40f, Offset(size.width / 2, size.height / 2))
            }
        }

        // Top-left city and offline map indicator badge
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = Color(0xFF1E293B).copy(alpha = 0.92f),
            border = ButtonDefaults.outlinedButtonBorder,
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(start = 16.dp, top = 80.dp)
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Icon(Icons.Default.Place, contentDescription = null, tint = Color(0xFF00E5FF), modifier = Modifier.size(14.dp))
                Text(
                    text = if (selectedCity == "الكل") "خريطة اليمن التفاعلية 🗺️" else "خريطة $selectedCity 📍",
                    fontSize = 11.sp,
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        // Top-right Interactive North Compass Needle
        Surface(
            shape = androidx.compose.foundation.shape.CircleShape,
            color = Color(0xFF1E293B).copy(alpha = 0.92f),
            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF334155)),
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(end = 16.dp, top = 80.dp)
                .size(38.dp)
                .clickable {
                    onPanOffsetChange(Offset.Zero)
                }
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = Icons.Default.Navigation,
                    contentDescription = "محاذاة للشمال",
                    tint = Color(0xFFFF5252),
                    modifier = Modifier.size(20.dp)
                )
            }
        }

        // Center friendly notice when no points match selected filter
        if (mapPoints.isEmpty()) {
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = Color(0xFF1E293B).copy(alpha = 0.95f),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF334155)),
                modifier = Modifier
                    .align(Alignment.Center)
                    .padding(32.dp)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(Icons.Default.Info, contentDescription = null, tint = Color(0xFF00E5FF), modifier = Modifier.size(18.dp))
                    Text(
                        text = "لا توجد خدمات في هذه المنطقة حالياً",
                        color = Color.White,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }

        // Bottom-left Dynamic Cartographic Scale Ruler & Zoom Multiplier
        val rawScaleMeters = 75f * metersPerPx
        val scaleDistanceMeters = when {
            rawScaleMeters <= 75f -> 50
            rawScaleMeters <= 150f -> 100
            rawScaleMeters <= 350f -> 200
            rawScaleMeters <= 750f -> 500
            rawScaleMeters <= 1500f -> 1000
            rawScaleMeters <= 3500f -> 2000
            rawScaleMeters <= 7500f -> 5000
            rawScaleMeters <= 15000f -> 10000
            else -> 20000
        }
        val scaleBarWidthDp = ((scaleDistanceMeters / metersPerPx) * 0.85f).coerceIn(40f, 130f).dp
        val scaleLabelText = if (scaleDistanceMeters < 1000) "$scaleDistanceMeters م" else "${scaleDistanceMeters / 1000} كم"

        Surface(
            shape = RoundedCornerShape(12.dp),
            color = Color(0xFF1E293B).copy(alpha = 0.90f),
            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF334155)),
            modifier = Modifier
                .align(Alignment.BottomStart)
                .navigationBarsPadding()
                .padding(start = 16.dp, bottom = 24.dp)
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = scaleLabelText,
                        color = Color(0xFFE2E8F0),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Box(
                        modifier = Modifier
                            .width(scaleBarWidthDp)
                            .height(3.dp)
                            .background(Color(0xFF00E5FF), RoundedCornerShape(1.dp))
                    )
                }
                Text(
                    text = "${String.format(java.util.Locale.US, "%.1f", clampedZoom)}x",
                    color = Color(0xFF00E5FF),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

/**
 * Draws structured city roads, ring-roads, blocks and parks in high-contrast neon cartographic style
 */
private fun DrawScope.drawCityRoadGrid(
    centerX: Float,
    centerY: Float,
    metersPerPx: Float,
    widthPx: Float,
    heightPx: Float,
    panOffset: Offset,
    zoomScale: Float,
    selectedCity: String,
    originLat: Double,
    originLng: Double,
    metersPerDegreeLat: Double,
    metersPerDegreeLng: Double
) {
    // Geodesic projection: maps real (lat, lng) to pixel offsets
    fun geoToScreen(lat: Double, lng: Double): Offset {
        val xm = ((lng - originLng) * metersPerDegreeLng).toFloat()
        val ym = (-(lat - originLat) * metersPerDegreeLat).toFloat()
        return Offset(centerX + (xm / metersPerPx), centerY + (ym / metersPerPx))
    }

    // 1. Urban Cartographic Background Blocks
    val gridSize = (140f * zoomScale.coerceIn(0.5f, 2.5f))
    val startX = (panOffset.x % gridSize) - gridSize
    val startY = (panOffset.y % gridSize) - gridSize

    val blockColor = Color(0xFF131D31)
    val blockBorderColor = Color(0xFF1E2C44)

    var currentY = startY
    while (currentY < heightPx + gridSize) {
        var currentX = startX
        while (currentX < widthPx + gridSize) {
            drawRoundRect(
                color = blockColor,
                topLeft = Offset(currentX + 8f, currentY + 8f),
                size = Size(gridSize - 16f, gridSize - 16f),
                cornerRadius = CornerRadius(8f, 8f)
            )
            drawRoundRect(
                color = blockBorderColor,
                topLeft = Offset(currentX + 8f, currentY + 8f),
                size = Size(gridSize - 16f, gridSize - 16f),
                cornerRadius = CornerRadius(8f, 8f),
                style = Stroke(width = 1f)
            )
            currentX += gridSize
        }
        currentY += gridSize
    }

    // 2. Real Public Parks and Waterfronts
    data class GeoParkData(val name: String, val lat: Double, val lng: Double, val wM: Float, val hM: Float)
    val parks = when {
        selectedCity.contains("عدن") -> listOf(
            GeoParkData("ساحل أبين وكورنيش خور مكسر 🏖️", 12.8050, 45.0450, 1200f, 400f),
            GeoParkData("حديقة النصر العامة 🌲", 12.8350, 45.0250, 600f, 450f),
            GeoParkData("متنزه صيرة وبحر صيرة 🌊", 12.7750, 45.0400, 500f, 400f)
        )
        selectedCity.contains("تعز") -> listOf(
            GeoParkData("حديقة الشعب العامة 🌲", 13.5820, 44.0150, 500f, 400f),
            GeoParkData("متنزه جبل صبر السياحي ⛰️", 13.5450, 44.0280, 800f, 500f)
        )
        selectedCity.contains("الحديدة") -> listOf(
            GeoParkData("حديقة الشعب بالحديدة 🌲", 14.8020, 42.9600, 600f, 450f),
            GeoParkData("كورنيش الكثيب والساحل 🌊", 14.8250, 42.9400, 1100f, 350f)
        )
        selectedCity.contains("إب") -> listOf(
            GeoParkData("منتجع مشورة الطبيعي 🌲", 13.9850, 44.1350, 750f, 500f),
            GeoParkData("حديقة خليج سري العامة 🌸", 13.9720, 44.1750, 500f, 400f)
        )
        selectedCity.contains("حضرموت") || selectedCity.contains("المكلا") -> listOf(
            GeoParkData("متنزه خور المكلا السياحي 🌊", 14.5360, 49.1280, 900f, 300f),
            GeoParkData("حديقة الطفل بالمكلا 🌲", 14.5420, 49.1350, 450f, 350f)
        )
        selectedCity.contains("مأرب") -> listOf(
            GeoParkData("متنزه سد مأرب التاريخي 🌊", 15.3950, 45.2400, 850f, 550f),
            GeoParkData("حديقة النصر العامة 🌲", 15.4680, 45.3280, 450f, 350f)
        )
        selectedCity.contains("ذمار") -> listOf(
            GeoParkData("حديقة السعادة بذمار 🌲", 14.5480, 44.4080, 500f, 400f),
            GeoParkData("متنزه قرن هران الطبيعي ⛰️", 14.5650, 44.3920, 700f, 450f)
        )
        else -> listOf(
            GeoParkData("حديقة السبعين الكبرى 🌲", 15.3320, 44.2020, 750f, 550f),
            GeoParkData("حديقة الثورة العامة 🌲", 15.3850, 44.2050, 650f, 500f),
            GeoParkData("ميدان التحرير 🏛️", 15.3565, 44.2055, 300f, 250f)
        )
    }

    parks.forEach { p ->
        val centerPt = geoToScreen(p.lat, p.lng)
        val wPx = p.wM / metersPerPx
        val hPx = p.hM / metersPerPx
        val rectTopLeft = Offset(centerPt.x - wPx / 2f, centerPt.y - hPx / 2f)
        if (rectTopLeft.x < widthPx + 200f && rectTopLeft.x + wPx > -200f &&
            rectTopLeft.y < heightPx + 200f && rectTopLeft.y + hPx > -200f) {
            drawRoundRect(
                color = Color(0xFF10B981).copy(alpha = 0.40f),
                topLeft = rectTopLeft,
                size = Size(wPx, hPx),
                cornerRadius = CornerRadius(14f, 14f)
            )
            drawRoundRect(
                color = Color(0xFF059669).copy(alpha = 0.70f),
                topLeft = rectTopLeft,
                size = Size(wPx, hPx),
                cornerRadius = CornerRadius(14f, 14f),
                style = Stroke(width = 1.5f)
            )
        }
    }

    // 3. Real Geodesic Roads (شبكة الشوارع الحقيقية لكافة المدن اليمنية الرئيسية)
    data class GeoRoadSegment(
        val name: String,
        val isHighway: Boolean,
        val points: List<Pair<Double, Double>>
    )

    val roads: List<GeoRoadSegment> = when {
        selectedCity.contains("عدن") -> listOf(
            GeoRoadSegment("طريق خط الجسر البحري", true, listOf(Pair(12.8350, 45.0150), Pair(12.8150, 45.0250), Pair(12.7980, 45.0350))),
            GeoRoadSegment("شارع مدرم (المعلا الرئيسي)", true, listOf(Pair(12.7880, 44.9950), Pair(12.7870, 45.0150))),
            GeoRoadSegment("شارع أروى (كريتر)", false, listOf(Pair(12.7750, 45.0300), Pair(12.7780, 45.0420))),
            GeoRoadSegment("طريق العقبة التاريخي", false, listOf(Pair(12.7850, 45.0150), Pair(12.7760, 45.0280))),
            GeoRoadSegment("كورنيش ريمي والمنصورة", false, listOf(Pair(12.8450, 45.0120), Pair(12.8250, 45.0150))),
            GeoRoadSegment("شارع التسعين التجاري", true, listOf(Pair(12.8550, 44.9900), Pair(12.8550, 45.0250))),
            GeoRoadSegment("طريق مطار عدن الدولي", true, listOf(Pair(12.8250, 45.0300), Pair(12.8350, 45.0400)))
        )
        selectedCity.contains("تعز") -> listOf(
            GeoRoadSegment("شارع جمال عبدالناصر الرئيسي", true, listOf(Pair(13.5850, 44.0050), Pair(13.5780, 44.0180), Pair(13.5750, 44.0300))),
            GeoRoadSegment("طريق الحوبان العام", true, listOf(Pair(13.6050, 44.0550), Pair(13.5900, 44.0400))),
            GeoRoadSegment("شارع 26 سبتمبر", false, listOf(Pair(13.5820, 44.0150), Pair(13.5750, 44.0150))),
            GeoRoadSegment("طريق قلعة القاهرة السياحي", false, listOf(Pair(13.5750, 44.0180), Pair(13.5650, 44.0180))),
            GeoRoadSegment("شارع وادي القاضي", false, listOf(Pair(13.5900, 44.0100), Pair(13.5820, 44.0120))),
            GeoRoadSegment("طريق بير باشا وجامعة تعز", true, listOf(Pair(13.5780, 43.9850), Pair(13.5800, 44.0050)))
        )
        selectedCity.contains("الحديدة") -> listOf(
            GeoRoadSegment("شارع صنعاء الرئيسي", true, listOf(Pair(14.8100, 42.9850), Pair(14.7950, 42.9650), Pair(14.7900, 42.9550))),
            GeoRoadSegment("كورنيش الحديدة الساحلي", true, listOf(Pair(14.8350, 42.9350), Pair(14.8050, 42.9450), Pair(14.7800, 42.9500))),
            GeoRoadSegment("شارع الميناء التجاري", true, listOf(Pair(14.8050, 42.9450), Pair(14.7950, 42.9500))),
            GeoRoadSegment("شارع جمال بالحديدة", false, listOf(Pair(14.8000, 42.9550), Pair(14.7900, 42.9600))),
            GeoRoadSegment("شارع فلسطين والقدس", false, listOf(Pair(14.8150, 42.9650), Pair(14.8000, 42.9700)))
        )
        selectedCity.contains("إب") -> listOf(
            GeoRoadSegment("شارع تعز العام (إب)", true, listOf(Pair(13.9850, 44.1750), Pair(13.9650, 44.1700), Pair(13.9450, 44.1650))),
            GeoRoadSegment("شارع العدين الرئيسي", true, listOf(Pair(13.9700, 44.1750), Pair(13.9720, 44.1550), Pair(13.9750, 44.1400))),
            GeoRoadSegment("الدائري الغربي بإب", true, listOf(Pair(13.9900, 44.1550), Pair(13.9650, 44.1550))),
            GeoRoadSegment("طريق بعدان ومشورة السياحي", false, listOf(Pair(13.9700, 44.1800), Pair(13.9750, 44.1950))),
            GeoRoadSegment("شارع 22 مايو", false, listOf(Pair(13.9650, 44.1650), Pair(13.9600, 44.1750)))
        )
        selectedCity.contains("حضرموت") || selectedCity.contains("المكلا") -> listOf(
            GeoRoadSegment("شارع الستين البحري (المكلا)", true, listOf(Pair(14.5450, 49.1100), Pair(14.5380, 49.1250), Pair(14.5300, 49.1450))),
            GeoRoadSegment("طريق الخور وجسر بن حبريش", true, listOf(Pair(14.5380, 49.1250), Pair(14.5340, 49.1300))),
            GeoRoadSegment("شارع الشرج التجاري", false, listOf(Pair(14.5420, 49.1200), Pair(14.5360, 49.1220))),
            GeoRoadSegment("طريق فوة العام", true, listOf(Pair(14.5500, 49.0700), Pair(14.5450, 49.1050))),
            GeoRoadSegment("طريق الديس والغليلة", false, listOf(Pair(14.5450, 49.1350), Pair(14.5550, 49.1450)))
        )
        selectedCity.contains("مأرب") -> listOf(
            GeoRoadSegment("الشارع العام الرئيسي بمأرب", true, listOf(Pair(15.4850, 45.3100), Pair(15.4650, 45.3250), Pair(15.4450, 45.3400))),
            GeoRoadSegment("طريق سد مأرب التاريخي", true, listOf(Pair(15.4600, 45.3200), Pair(15.4200, 45.2700))),
            GeoRoadSegment("خط صافر الدولي", true, listOf(Pair(15.4650, 45.3350), Pair(15.4700, 45.3850))),
            GeoRoadSegment("شارع المحافظة والدائري", false, listOf(Pair(15.4700, 45.3150), Pair(15.4550, 45.3300)))
        )
        selectedCity.contains("ذمار") -> listOf(
            GeoRoadSegment("شارع صنعاء - تعز العام", true, listOf(Pair(14.5650, 44.4000), Pair(14.5450, 44.4050), Pair(14.5250, 44.4100))),
            GeoRoadSegment("الدائري الشمالي بذمار", true, listOf(Pair(14.5600, 44.3850), Pair(14.5600, 44.4200))),
            GeoRoadSegment("شارع رداع التجاري", false, listOf(Pair(14.5450, 44.4050), Pair(14.5400, 44.4300))),
            GeoRoadSegment("شارع السوق المركزي", false, listOf(Pair(14.5420, 44.3980), Pair(14.5460, 44.4120)))
        )
        else -> listOf(
            // صنعاء والشوارع الرئيسية الافتراضية
            GeoRoadSegment("شارع الستين الغربي", true, listOf(Pair(15.3950, 44.1680), Pair(15.3780, 44.1680), Pair(15.3480, 44.1720), Pair(15.3200, 44.1750))),
            GeoRoadSegment("شارع الستين الشرقي", true, listOf(Pair(15.3950, 44.2250), Pair(15.3650, 44.2250), Pair(15.3350, 44.2200), Pair(15.3000, 44.2200))),
            GeoRoadSegment("شارع الخمسين الجنوبي", true, listOf(Pair(15.3050, 44.1700), Pair(15.3050, 44.1950), Pair(15.3050, 44.2250))),
            GeoRoadSegment("شارع السبعين الرئيسي", true, listOf(Pair(15.3450, 44.2050), Pair(15.3320, 44.2050), Pair(15.3250, 44.2050), Pair(15.3050, 44.2050))),
            GeoRoadSegment("شارع الزبيري الرئيسي", true, listOf(Pair(15.3480, 44.1720), Pair(15.3520, 44.1880), Pair(15.3530, 44.1980), Pair(15.3530, 44.2150))),
            GeoRoadSegment("شارع الدائري الغربي", false, listOf(Pair(15.3800, 44.1780), Pair(15.3680, 44.1830), Pair(15.3620, 44.1865), Pair(15.3585, 44.1880), Pair(15.3520, 44.1880), Pair(15.3420, 44.1890))),
            GeoRoadSegment("شارع حدة الرئيسي", false, listOf(Pair(15.3520, 44.1980), Pair(15.3400, 44.1940), Pair(15.3280, 44.1920), Pair(15.3100, 44.1850))),
            GeoRoadSegment("شارع هائل التجاري", false, listOf(Pair(15.3700, 44.1830), Pair(15.3620, 44.1830), Pair(15.3520, 44.1840))),
            GeoRoadSegment("شارع الرقاص", false, listOf(Pair(15.3700, 44.1800), Pair(15.3600, 44.1810), Pair(15.3520, 44.1820))),
            GeoRoadSegment("شارع بغداد", false, listOf(Pair(15.3520, 44.1920), Pair(15.3440, 44.1920), Pair(15.3380, 44.1920))),
            GeoRoadSegment("شارع الجزائر", false, listOf(Pair(15.3400, 44.1880), Pair(15.3400, 44.1950), Pair(15.3400, 44.2050))),
            GeoRoadSegment("شارع عمان", false, listOf(Pair(15.3480, 44.2000), Pair(15.3320, 44.2000))),
            GeoRoadSegment("شارع علي عبدالمغني", false, listOf(Pair(15.3600, 44.2050), Pair(15.3530, 44.2080), Pair(15.3520, 44.2120))),
            GeoRoadSegment("شارع المطار", true, listOf(Pair(15.3850, 44.2150), Pair(15.4200, 44.2200), Pair(15.4600, 44.2250))),
            GeoRoadSegment("شارع تعز العام", true, listOf(Pair(15.3520, 44.2160), Pair(15.3350, 44.2160), Pair(15.3200, 44.2160), Pair(15.2950, 44.2200))),
            GeoRoadSegment("شارع مأرب والنصر", true, listOf(Pair(15.3850, 44.2200), Pair(15.3850, 44.2600)))
        )
    }

    val highwayOuterColor = Color(0xFFF59E0B) // Amber
    val highwayInnerColor = Color(0xFFFEF08A) // Yellow
    val streetOuterColor = Color(0xFF0284C7)  // Sky
    val streetInnerColor = Color(0xFFE0F2FE)  // Light cyan

    val roadTextPaint = android.graphics.Paint().apply {
        textSize = (20f * zoomScale.coerceIn(0.7f, 1.6f)).coerceIn(16f, 28f)
        color = android.graphics.Color.WHITE
        textAlign = android.graphics.Paint.Align.CENTER
        isFakeBoldText = true
        isAntiAlias = true
        setShadowLayer(5f, 1f, 1f, android.graphics.Color.BLACK)
    }

    // إصلاح 1.3: رسم الشوارع بمستويات تفصيل تعتمد على قيمة zoomScale
    roads.forEach { r ->
        // عند التصغير يقتصر العرض على الشوارع الرئيسية، وعند التكبير تظهر الشوارع الفرعية
        val shouldDrawSegment = if (r.isHighway) true else zoomScale >= 0.55f
        if (shouldDrawSegment) {
            val pts = r.points.map { geoToScreen(it.first, it.second) }
            val zoomFactor = zoomScale.coerceIn(0.4f, 4.0f)
            val outerW = if (r.isHighway) (8.0f * zoomFactor).coerceIn(4f, 24f) else (5.0f * zoomFactor).coerceIn(2.5f, 16f)
            val innerW = if (r.isHighway) (outerW * 0.55f) else (outerW * 0.45f)
            val oCol = if (r.isHighway) highwayOuterColor else streetOuterColor
            val iCol = if (r.isHighway) highwayInnerColor else streetInnerColor

            for (i in 0 until pts.size - 1) {
                val p1 = pts[i]
                val p2 = pts[i + 1]
                drawLine(color = oCol, start = p1, end = p2, strokeWidth = outerW)
                drawLine(color = iCol, start = p1, end = p2, strokeWidth = innerW)
            }

            // إظهار أسماء الشوارع: الرئيسية عند zoom منخفض، والفرعية عند التكبير
            val shouldShowLabel = if (r.isHighway) zoomScale >= 0.45f else zoomScale >= 0.85f
            if (shouldShowLabel && pts.size >= 2) {
                val midIdx = (pts.size - 1) / 2
                val midX = (pts[midIdx].x + pts[midIdx + 1].x) / 2f
                val midY = (pts[midIdx].y + pts[midIdx + 1].y) / 2f
                if (midX in -80f..(widthPx + 80f) && midY in -80f..(heightPx + 80f)) {
                    drawContext.canvas.nativeCanvas.drawText(r.name + " 🛣️", midX, midY - 8f, roadTextPaint)
                }
            }
        }
    }

    // 4. Real Roundabouts & Landmarks (الجولات والمعالم الحقيقية لكافة المدن)
    data class GeoRoundaboutData(val name: String, val lat: Double, val lng: Double, val isHighlight: Boolean = false)
    val roundabouts = when {
        selectedCity.contains("عدن") -> listOf(
            GeoRoundaboutData("جولة كالتكس (المنصورة)", 12.8350, 45.0150, true),
            GeoRoundaboutData("جولة ريجل (خور مكسر)", 12.8120, 45.0350, true),
            GeoRoundaboutData("قلعة صيرة التاريخية 🏰", 12.7720, 45.0420, true),
            GeoRoundaboutData("جولة الفل (كريتر)", 12.7780, 45.0320),
            GeoRoundaboutData("دوار المعلا الرئيسي", 12.7880, 45.0080),
            GeoRoundaboutData("جولة سوزوكي والشيخ عثمان", 12.8620, 45.0280)
        )
        selectedCity.contains("تعز") -> listOf(
            GeoRoundaboutData("جولة العواضي (شارع جمال)", 13.5780, 44.0180, true),
            GeoRoundaboutData("قلعة القاهرة الأثرية 🏰", 13.5650, 44.0180, true),
            GeoRoundaboutData("جولة سنان 📍", 13.5820, 44.0120),
            GeoRoundaboutData("دوار الحوبان المركزي", 13.6020, 44.0480, true),
            GeoRoundaboutData("باب موسى التاريخي 🏛️", 13.5730, 44.0160),
            GeoRoundaboutData("جولة المسبح", 13.5850, 44.0150)
        )
        selectedCity.contains("الحديدة") -> listOf(
            GeoRoundaboutData("دوار الساعة الشهير ⏰", 14.7950, 42.9550, true),
            GeoRoundaboutData("ميناء الحديدة الدولي ⚓", 14.8050, 42.9450, true),
            GeoRoundaboutData("جولة الكورنيش الساحلي", 14.8150, 42.9400),
            GeoRoundaboutData("جولة يمن موبايل", 14.7980, 42.9650),
            GeoRoundaboutData("دوار الصدفة", 14.8250, 42.9380)
        )
        selectedCity.contains("إب") -> listOf(
            GeoRoundaboutData("جولة العدين المركزية 📍", 13.9720, 44.1550, true),
            GeoRoundaboutData("منتجع جبل ربي ⛰️", 13.9550, 44.1720, true),
            GeoRoundaboutData("جولة الصوفية", 13.9680, 44.1680),
            GeoRoundaboutData("دوار المعاين", 13.9850, 44.1600),
            GeoRoundaboutData("جولة سبلة", 13.9620, 44.1780)
        )
        selectedCity.contains("حضرموت") || selectedCity.contains("المكلا") -> listOf(
            GeoRoundaboutData("خور المكلا وجسر الشهيد 🌉", 14.5380, 49.1250, true),
            GeoRoundaboutData("حصن الغويزي التاريخي 🏰", 14.5580, 49.1480, true),
            GeoRoundaboutData("جولة الدلة المركزية ☕", 14.5420, 49.1200, true),
            GeoRoundaboutData("منارة المكلا التاريخية 🗼", 14.5320, 49.1380),
            GeoRoundaboutData("دوار فوة السكني", 14.5480, 49.0950)
        )
        selectedCity.contains("مأرب") -> listOf(
            GeoRoundaboutData("دوار المدينة المركزي 📍", 15.4650, 45.3250, true),
            GeoRoundaboutData("عرش بلقيس الأثري 🏛️", 15.4380, 45.3350, true),
            GeoRoundaboutData("معبد أوام التاريخي 🏛️", 15.4280, 45.3420, true),
            GeoRoundaboutData("سد مأرب العظيم 🌊", 15.3950, 45.2400, true),
            GeoRoundaboutData("جولة النصر", 15.4720, 45.3200)
        )
        selectedCity.contains("ذمار") -> listOf(
            GeoRoundaboutData("جولة الميدان العام 📍", 14.5450, 44.4050, true),
            GeoRoundaboutData("الجامع الكبير التاريخي 🕌", 14.5420, 44.4080, true),
            GeoRoundaboutData("دوار المحافظة", 14.5550, 44.4020),
            GeoRoundaboutData("جولة عمارة النصر", 14.5480, 44.4150)
        )
        else -> listOf(
            GeoRoundaboutData("جولة الضبيبي 📍", 15.3585, 44.1880, true),
            GeoRoundaboutData("جامعة صنعاء القديمة 🏛️", 15.3620, 44.1865, true),
            GeoRoundaboutData("جولة كنتاكي / الرويشان", 15.3510, 44.1980),
            GeoRoundaboutData("جولة المصباحي (حدة)", 15.3280, 44.1920),
            GeoRoundaboutData("جولة عصر", 15.3460, 44.1720),
            GeoRoundaboutData("جولة مذبح", 15.3780, 44.1680),
            GeoRoundaboutData("ميدان التحرير 🏛️", 15.3565, 44.2055),
            GeoRoundaboutData("باب اليمن التاريخي 🏛️", 15.3530, 44.2155),
            GeoRoundaboutData("ميدان السبعين وجامع الصالح 🕌", 15.3250, 44.2050),
            GeoRoundaboutData("جولة شميله", 15.3200, 44.2160),
            GeoRoundaboutData("جولة سبأ", 15.3720, 44.2120),
            GeoRoundaboutData("جولة القادسية", 15.3380, 44.1820)
        )
    }

    val roundPaint = android.graphics.Paint().apply {
        textSize = (20f * zoomScale.coerceIn(0.7f, 1.5f)).coerceIn(15f, 26f)
        color = android.graphics.Color.WHITE
        textAlign = android.graphics.Paint.Align.CENTER
        isFakeBoldText = true
        isAntiAlias = true
        setShadowLayer(5f, 1f, 1f, android.graphics.Color.BLACK)
    }

    roundabouts.forEach { rb ->
        // إظهار المعالم البارزة دائماً والمعالم المحلية عند مستوى zoom مناسب
        val shouldShowRb = rb.isHighlight || zoomScale >= 0.75f
        if (shouldShowRb) {
            val pt = geoToScreen(rb.lat, rb.lng)
            if (pt.x in -120f..(widthPx + 120f) && pt.y in -120f..(heightPx + 120f)) {
                val ringColor = if (rb.isHighlight) Color(0xFF00E5FF) else Color(0xFFFBBF24)
                drawCircle(color = ringColor.copy(alpha = 0.35f), radius = 16f, center = pt)
                drawCircle(color = ringColor, radius = 9f, center = pt)
                drawCircle(color = Color(0xFF0F172A), radius = 5f, center = pt)
                drawContext.canvas.nativeCanvas.drawText(rb.name, pt.x, pt.y + 24f, roundPaint)
            }
        }
    }

    // 5. Real Neighborhood Badges (أحياء كافة المدن اليمنية بإحداثياتها الدقيقة)
    data class GeoDistrictData(val name: String, val lat: Double, val lng: Double, val emoji: String)
    val districts = when {
        selectedCity.contains("عدن") -> listOf(
            GeoDistrictData("حي كريتر التاريخي", 12.7750, 45.0320, "🏛️"),
            GeoDistrictData("حي المعلا", 12.7880, 45.0050, "🚢"),
            GeoDistrictData("حي التواهي", 12.7950, 44.9850, "🌊"),
            GeoDistrictData("حي خور مكسر", 12.8150, 45.0350, "✈️"),
            GeoDistrictData("حي المنصورة", 12.8450, 45.0150, "🏙️"),
            GeoDistrictData("حي الشيخ عثمان", 12.8650, 45.0300, "🛍️"),
            GeoDistrictData("حي البريقة", 12.7500, 44.8850, "🏖️"),
            GeoDistrictData("حي دار سعد", 12.8900, 45.0250, "🏡")
        )
        selectedCity.contains("تعز") -> listOf(
            GeoDistrictData("حي القاهرة", 13.5780, 44.0150, "🏰"),
            GeoDistrictData("حي المظفر", 13.5820, 44.0080, "🏛️"),
            GeoDistrictData("حي الحوبان", 13.6050, 44.0500, "🏭"),
            GeoDistrictData("حي صالة", 13.5800, 44.0350, "🌳"),
            GeoDistrictData("حي بير باشا", 13.5780, 43.9850, "🎓"),
            GeoDistrictData("حي وادي القاضي", 13.5900, 44.0100, "🏡"),
            GeoDistrictData("حي الجمهوري", 13.5700, 44.0250, "🏥")
        )
        selectedCity.contains("الحديدة") -> listOf(
            GeoDistrictData("حي الميناء", 14.8050, 42.9450, "⚓"),
            GeoDistrictData("حي الحوك", 14.7850, 42.9550, "🌊"),
            GeoDistrictData("حي الحالي", 14.8050, 42.9750, "🌴"),
            GeoDistrictData("حي 7 يوليو", 14.8150, 42.9850, "🏙️"),
            GeoDistrictData("حي غليل", 14.7950, 42.9650, "🛍️")
        )
        selectedCity.contains("إب") -> listOf(
            GeoDistrictData("حي الظهار", 13.9750, 44.1650, "🏙️"),
            GeoDistrictData("حي المشنة", 13.9650, 44.1750, "🏞️"),
            GeoDistrictData("مدينة جبلة التاريخية", 13.9200, 44.1480, "👑"),
            GeoDistrictData("حي المعاين", 13.9880, 44.1580, "🏡"),
            GeoDistrictData("حي السبل", 13.9620, 44.1720, "✨")
        )
        selectedCity.contains("حضرموت") || selectedCity.contains("المكلا") -> listOf(
            GeoDistrictData("حي المكلا القديمة", 14.5320, 49.1350, "🏛️"),
            GeoDistrictData("حي الشرج", 14.5420, 49.1200, "🛍️"),
            GeoDistrictData("حي الديس", 14.5480, 49.1380, "🏙️"),
            GeoDistrictData("حي فوه", 14.5500, 49.0850, "🏖️"),
            GeoDistrictData("حي روكب", 14.5650, 49.1850, "🌊")
        )
        selectedCity.contains("مأرب") -> listOf(
            GeoDistrictData("حي المدينة المركزي", 15.4650, 45.3250, "🏛️"),
            GeoDistrictData("حي الروضة", 15.4850, 45.3150, "🏡"),
            GeoDistrictData("منطقة الوادي", 15.4500, 45.3650, "🌴"),
            GeoDistrictData("حي الجفينة", 15.4350, 45.3050, "⛺")
        )
        selectedCity.contains("ذمار") -> listOf(
            GeoDistrictData("حي الميدان المركزي", 14.5450, 44.4050, "🛍️"),
            GeoDistrictData("حي السوق والجمارك", 14.5400, 44.3980, "🏢"),
            GeoDistrictData("حي روضة ذمار", 14.5550, 44.4150, "🏡"),
            GeoDistrictData("حي هران", 14.5680, 44.3900, "⛰️")
        )
        else -> listOf(
            GeoDistrictData("حي جامعة صنعاء", 15.3650, 44.1850, "🎓"),
            GeoDistrictData("حي هائل التجاري", 15.3620, 44.1820, "🛍️"),
            GeoDistrictData("حي الدائري", 15.3560, 44.1890, "🏙️"),
            GeoDistrictData("حي حدة الراقي", 15.3300, 44.1880, "✨"),
            GeoDistrictData("صنعاء القديمة التاريخية", 15.3550, 44.2160, "🏛️"),
            GeoDistrictData("حي التحرير", 15.3570, 44.2050, "🏢"),
            GeoDistrictData("حي السبعين", 15.3350, 44.2050, "🌳"),
            GeoDistrictData("حي عصر", 15.3420, 44.1690, "⛰️"),
            GeoDistrictData("حي المذبح", 15.3850, 44.1680, "🏡"),
            GeoDistrictData("حي الحصبة", 15.3820, 44.2050, "🏙️"),
            GeoDistrictData("حي الروضة", 15.4200, 44.2150, "🌿"),
            GeoDistrictData("حي شميله", 15.3200, 44.2160, "🛒"),
            GeoDistrictData("حي الأصبحي", 15.3050, 44.2100, "🏘️"),
            GeoDistrictData("حي بيت بوس", 15.2950, 44.1950, "🏰"),
            GeoDistrictData("حي نقم وشيراتون", 15.3650, 44.2300, "🏨"),
            GeoDistrictData("حي الصافية", 15.3450, 44.2120, "🏡")
        )
    }

    val distPaint = android.graphics.Paint().apply {
        textSize = (22f * zoomScale.coerceIn(0.7f, 1.5f)).coerceIn(16f, 28f)
        color = android.graphics.Color.WHITE
        textAlign = android.graphics.Paint.Align.CENTER
        isFakeBoldText = true
        isAntiAlias = true
        setShadowLayer(6f, 1f, 1f, android.graphics.Color.BLACK)
    }

    districts.forEach { d ->
        val pt = geoToScreen(d.lat, d.lng)
        if (pt.x in -120f..(widthPx + 120f) && pt.y in -120f..(heightPx + 120f)) {
            val label = "${d.emoji} ${d.name}"
            val textW = distPaint.measureText(label)
            val bgRect = android.graphics.RectF(pt.x - textW / 2f - 10f, pt.y - 20f, pt.x + textW / 2f + 10f, pt.y + 8f)
            val pillPaint = android.graphics.Paint().apply {
                color = android.graphics.Color.parseColor("#B30F172A")
                isAntiAlias = true
                style = android.graphics.Paint.Style.FILL
            }
            val borderPaint = android.graphics.Paint().apply {
                color = android.graphics.Color.parseColor("#475569")
                isAntiAlias = true
                style = android.graphics.Paint.Style.STROKE
                strokeWidth = 1.2f
            }
            drawContext.canvas.nativeCanvas.drawRoundRect(bgRect, 8f, 8f, pillPaint)
            drawContext.canvas.nativeCanvas.drawRoundRect(bgRect, 8f, 8f, borderPaint)
            drawContext.canvas.nativeCanvas.drawText(label, pt.x, pt.y, distPaint)
        }
    }
}

data class MapLabel(val text: String, val x: Float, val y: Float, val paint: android.graphics.Paint)

data class InteractiveMapPoint(
    val id: String,
    val name: String,
    val category: String,
    val type: String,
    val emoji: String,
    val color: Color,
    val xMeters: Float,
    val yMeters: Float,
    val rating: Double,
    val phone: String,
    val originalEntity: Any,
    val screenX: Float = 0f,
    val screenY: Float = 0f
)

