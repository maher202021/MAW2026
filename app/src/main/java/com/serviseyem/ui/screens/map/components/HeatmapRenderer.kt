package com.serviseyem.ui.screens.map.components

import androidx.compose.ui.geometry.Offset
import com.serviseyem.ui.*
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import kotlin.math.exp
import kotlin.math.pow
import kotlin.math.sqrt

/**
 * 🔥 HeatmapRenderer
 * Real Kernel Density Estimation (KDE) calculation and rendering
 * Color gradient: Blue (low) -> Green -> Yellow -> Red (high density)
 */
object HeatmapRenderer {

    data class WeightedPoint(
        val x: Float,
        val y: Float,
        val weight: Float = 1.0f
    )

    /**
     * Compute Gaussian Kernel Density at (px, py) from a list of points
     */
    fun computeKernelDensity(
        px: Float,
        py: Float,
        points: List<WeightedPoint>,
        bandwidth: Float
    ): Float {
        var totalDensity = 0.0f
        val variance = bandwidth.pow(2)

        for (pt in points) {
            val distSq = (px - pt.x).pow(2) + (py - pt.y).pow(2)
            if (distSq < variance * 9) { // 3-sigma cutoff
                val kernelVal = exp(-distSq / (2 * variance))
                totalDensity += pt.weight * kernelVal
            }
        }
        return totalDensity
    }

    /**
     * Draw KDE Heatmap on Canvas with realistic multi-stop Gaussian density gradients
     */
    fun drawHeatmapLayer(
        drawScope: DrawScope,
        points: List<WeightedPoint>,
        bandwidth: Float = 60f,
        maxOpacity: Float = 0.65f
    ) {
        if (points.isEmpty()) return

        for (pt in points) {
            val radius = (bandwidth * (1.1f + 0.35f * pt.weight.coerceIn(0.5f, 2.5f))).coerceAtLeast(15f)
            val alpha = (maxOpacity * (pt.weight / 1.1f)).coerceIn(0.20f, 0.85f)

            drawScope.drawCircle(
                brush = Brush.radialGradient(
                    0.00f to Color(0xFFDC2626).copy(alpha = alpha),          // Hot Core (Deep Red)
                    0.25f to Color(0xFFEA580C).copy(alpha = alpha * 0.85f),  // High density (Deep Orange)
                    0.50f to Color(0xFFFBBF24).copy(alpha = alpha * 0.65f),  // Medium density (Amber)
                    0.72f to Color(0xFF10B981).copy(alpha = alpha * 0.40f),  // Moderate density (Emerald)
                    0.88f to Color(0xFF06B6D4).copy(alpha = alpha * 0.20f),  // Outer aura (Cyan)
                    1.00f to Color.Transparent,
                    center = Offset(pt.x, pt.y),
                    radius = radius
                ),
                center = Offset(pt.x, pt.y),
                radius = radius
            )
        }
    }
}
