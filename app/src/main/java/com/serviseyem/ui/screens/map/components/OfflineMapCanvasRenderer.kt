package com.serviseyem.ui.screens.map.components

import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import com.serviseyem.data.PropertyEntity
import com.serviseyem.data.ProviderEntity
import com.serviseyem.data.StoreEntity
import kotlin.math.max
import kotlin.math.pow
import kotlin.math.roundToInt
import kotlin.math.sqrt

internal fun DrawScope.drawTacticalProximityRings(
    userPin: Offset,
    metersPerPx: Float,
    minX: Float,
    maxX: Float,
    minY: Float,
    maxY: Float
) {
    if (userPin.x !in minX..maxX || userPin.y !in minY..maxY) return

    val ring500Px = 500f / metersPerPx
    val ring1000Px = 1000f / metersPerPx
    val ring2000Px = 2000f / metersPerPx

    val dashRingStroke = Stroke(
        width = 1.2f,
        pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f))
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

internal fun DrawScope.drawInteractiveMapMarkers(
    screenPoints: List<InteractiveMapPoint>,
    selectedEntity: Any?,
    zoomScale: Float,
    userXMeters: Float,
    userYMeters: Float,
    minX: Float,
    maxX: Float,
    minY: Float,
    maxY: Float
) {
    val fontScale = (zoomScale.coerceIn(0.6f, 2.5f)).pow(0.35f)
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
        color = android.graphics.Color.parseColor("#38BDF8")
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

            drawOval(
                color = Color.Black.copy(alpha = 0.40f),
                topLeft = Offset(groundPoint.x - baseRadius * 0.75f, groundPoint.y - 3f),
                size = Size(baseRadius * 1.5f, 6f)
            )

            val needlePath = Path().apply {
                moveTo(groundPoint.x, groundPoint.y)
                lineTo(groundPoint.x - baseRadius * 0.55f, pinHeadCenter.y + baseRadius * 0.5f)
                lineTo(groundPoint.x + baseRadius * 0.55f, pinHeadCenter.y + baseRadius * 0.5f)
                close()
            }
            drawPath(needlePath, color = if (isSelected) Color.White else pt.color)

            if (isSelected) {
                drawCircle(pt.color.copy(alpha = 0.30f), baseRadius * 2.8f, pinHeadCenter)
                drawCircle(pt.color.copy(alpha = 0.65f), baseRadius * 1.9f, pinHeadCenter)
                drawCircle(Color.White, baseRadius * 1.35f, pinHeadCenter, style = Stroke(4f))
            } else {
                drawCircle(pt.color.copy(alpha = 0.35f), baseRadius * 1.7f, pinHeadCenter)
            }

            drawCircle(Color(0xFF0F172A), baseRadius, pinHeadCenter)
            drawCircle(
                color = if (isSelected) Color.White else pt.color,
                radius = baseRadius,
                center = pinHeadCenter,
                style = Stroke(if (isSelected) 3.5f else 2.5f)
            )

            drawContext.canvas.nativeCanvas.drawText(
                pt.emoji,
                pinHeadCenter.x,
                pinHeadCenter.y + (emojiSize * 0.35f),
                emojiPaint
            )

            val distMeters = sqrt((pt.xMeters - userXMeters).pow(2) + (pt.yMeters - userYMeters).pow(2))
            val distString = if (distMeters < 1000f) {
                "${distMeters.roundToInt()} م"
            } else {
                "${String.format(java.util.Locale.US, "%.1f", distMeters / 1000f)} كم"
            }

            if (zoomScale >= 0.50f) {
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

                drawContext.canvas.nativeCanvas.drawRoundRect(bgRect, 12f, 12f, labelBgPaint)
                drawContext.canvas.nativeCanvas.drawText(
                    displayName,
                    pinHeadCenter.x,
                    labelTop + titleLabelSize - 1f,
                    titlePaint
                )
                drawContext.canvas.nativeCanvas.drawText(
                    subText,
                    pinHeadCenter.x,
                    labelTop + titleLabelSize + subLabelSize + 3f,
                    subtitlePaint
                )
            }
        }
    }
}

internal fun DrawScope.drawUserLocationMarker(
    userPin: Offset,
    pulseProgress: Float,
    selectedCity: String,
    minX: Float,
    maxX: Float,
    minY: Float,
    maxY: Float
) {
    if (userPin.x !in minX..maxX || userPin.y !in minY..maxY) return

    val pulseRadiusMax = 44f
    val currentPulse = pulseProgress * pulseRadiusMax
    val pulseAlpha = (1f - pulseProgress).coerceIn(0f, 1f) * 0.55f

    drawCircle(
        color = Color(0xFF00E5FF).copy(alpha = pulseAlpha),
        radius = 14f + currentPulse,
        center = userPin
    )
    drawCircle(
        color = Color(0xFF00E5FF).copy(alpha = 0.35f),
        radius = 20f,
        center = userPin
    )
    drawCircle(color = Color(0xFF0F172A), radius = 13f, center = userPin)
    drawCircle(color = Color(0xFF00E5FF), radius = 11f, center = userPin)
    drawCircle(color = Color.White, radius = 5f, center = userPin)

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

internal fun DrawScope.drawCityRoadGrid(
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
    getCityParks(selectedCity).forEach { p ->
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

    // 3. Real Geodesic Roads
    val highwayOuterColor = Color(0xFFF59E0B)
    val highwayInnerColor = Color(0xFFFEF08A)
    val streetOuterColor = Color(0xFF0284C7)
    val streetInnerColor = Color(0xFFE0F2FE)

    val roadTextPaint = android.graphics.Paint().apply {
        textSize = (20f * zoomScale.coerceIn(0.7f, 1.6f)).coerceIn(16f, 28f)
        color = android.graphics.Color.WHITE
        textAlign = android.graphics.Paint.Align.CENTER
        isFakeBoldText = true
        isAntiAlias = true
        setShadowLayer(5f, 1f, 1f, android.graphics.Color.BLACK)
    }

    getCityRoads(selectedCity).forEach { r ->
        val shouldDrawSegment = if (r.isHighway) true else zoomScale >= 0.55f
        if (shouldDrawSegment) {
            val pts = r.points.map { geoToScreen(it.first, it.second) }
            val zoomFactor = zoomScale.coerceIn(0.4f, 4.0f)
            val outerW = if (r.isHighway) (8.0f * zoomFactor).coerceIn(4f, 24f) else (5.0f * zoomFactor).coerceIn(2.5f, 16f)
            val innerW = if (r.isHighway) (outerW * 0.55f) else (outerW * 0.45f)
            val oCol = if (r.isHighway) highwayOuterColor else streetOuterColor
            val iCol = if (r.isHighway) highwayInnerColor else streetInnerColor

            for (i in 0 until pts.size - 1) {
                drawLine(color = oCol, start = pts[i], end = pts[i + 1], strokeWidth = outerW)
                drawLine(color = iCol, start = pts[i], end = pts[i + 1], strokeWidth = innerW)
            }

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

    // 4. Real Roundabouts & Landmarks
    val roundPaint = android.graphics.Paint().apply {
        textSize = (20f * zoomScale.coerceIn(0.7f, 1.5f)).coerceIn(15f, 26f)
        color = android.graphics.Color.WHITE
        textAlign = android.graphics.Paint.Align.CENTER
        isFakeBoldText = true
        isAntiAlias = true
        setShadowLayer(5f, 1f, 1f, android.graphics.Color.BLACK)
    }

    getCityRoundabouts(selectedCity).forEach { rb ->
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

    // 5. Real Neighborhood Badges
    val distPaint = android.graphics.Paint().apply {
        textSize = (22f * zoomScale.coerceIn(0.7f, 1.5f)).coerceIn(16f, 28f)
        color = android.graphics.Color.WHITE
        textAlign = android.graphics.Paint.Align.CENTER
        isFakeBoldText = true
        isAntiAlias = true
        setShadowLayer(6f, 1f, 1f, android.graphics.Color.BLACK)
    }

    getCityDistricts(selectedCity).forEach { d ->
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
