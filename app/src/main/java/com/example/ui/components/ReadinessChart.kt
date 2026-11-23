package com.example.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.asAndroidPath
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.graphics.toArgb
import com.example.data.local.DailyCheckInEntity
import com.example.ui.theme.AppTheme
import com.example.ui.theme.MintGreenAccent
import com.example.ui.theme.SkyBluePrimary
import com.example.ui.theme.TextSecondary

@Composable
fun ReadinessLineChart(
    checkIns: List<DailyCheckInEntity>,
    modifier: Modifier = Modifier
) {
    val colors = AppTheme.colors
    val density = LocalDensity.current
    val progress = remember { Animatable(0f) }

    LaunchedEffect(checkIns) {
        progress.snapTo(0f)
        progress.animateTo(1f, animationSpec = tween(durationMillis = 900))
    }

    var selectedIndex by remember { mutableStateOf<Int?>(null) }

    // Fallback if empty
    val displayData = if (checkIns.isEmpty()) {
        listOf(
            DailyCheckInEntity(dayName = "Mon", dateLabel = "1", readinessLevel = 60, feelingTag = "Explorer"),
            DailyCheckInEntity(dayName = "Tue", dateLabel = "2", readinessLevel = 68, feelingTag = "Getting There"),
            DailyCheckInEntity(dayName = "Wed", dateLabel = "3", readinessLevel = 74, feelingTag = "Getting There"),
            DailyCheckInEntity(dayName = "Thu", dateLabel = "4", readinessLevel = 82, feelingTag = "Super Ready"),
            DailyCheckInEntity(dayName = "Fri", dateLabel = "5", readinessLevel = 85, feelingTag = "Super Ready"),
            DailyCheckInEntity(dayName = "Sat", dateLabel = "6", readinessLevel = 90, feelingTag = "Fired Up"),
            DailyCheckInEntity(dayName = "Sun", dateLabel = "7", readinessLevel = 88, feelingTag = "Fired Up")
        )
    } else {
        checkIns.takeLast(7)
    }

    Box(modifier = modifier.fillMaxWidth()) {
        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp)
                .padding(horizontal = 8.dp, vertical = 4.dp)
                .pointerInput(displayData) {
                    detectTapGestures { offset ->
                        val count = displayData.size
                        if (count > 1) {
                            val stepX = size.width / (count - 1)
                            val tappedIndex = (offset.x / stepX).toInt().coerceIn(0, count - 1)
                            selectedIndex = tappedIndex
                        }
                    }
                }
        ) {
            val width = size.width
            val height = size.height - 40f // Leave space for day markers
            val count = displayData.size

            if (count < 2) return@Canvas

            val stepX = width / (count - 1)
            val minScore = 40f
            val maxScore = 100f
            val scoreRange = maxScore - minScore

            // Guidelines
            val guideLevels = listOf(50, 75, 100)
            val dashEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f)

            guideLevels.forEach { lvl ->
                val y = height - ((lvl - minScore) / scoreRange) * height
                drawLine(
                    color = if (colors.isDark) Color(0x2EFFFFFF) else Color(0x1F000000),
                    start = Offset(0f, y),
                    end = Offset(width, y),
                    strokeWidth = 1.dp.toPx(),
                    pathEffect = dashEffect
                )
            }

            // Calculate points
            val points = displayData.mapIndexed { index, item ->
                val x = index * stepX
                val normalizedScore = ((item.readinessLevel - minScore) / scoreRange).coerceIn(0f, 1f)
                val targetY = height - (normalizedScore * height)
                val animatedY = height - (normalizedScore * height * progress.value)
                Offset(x, animatedY)
            }

            // Build smooth Bezier path
            val linePath = Path()
            val fillPath = Path()

            linePath.moveTo(points.first().x, points.first().y)
            fillPath.moveTo(points.first().x, height)
            fillPath.lineTo(points.first().x, points.first().y)

            for (i in 0 until points.size - 1) {
                val p0 = points[i]
                val p1 = points[i + 1]
                val midX = (p0.x + p1.x) / 2f

                linePath.cubicTo(
                    midX, p0.y,
                    midX, p1.y,
                    p1.x, p1.y
                )
                fillPath.cubicTo(
                    midX, p0.y,
                    midX, p1.y,
                    p1.x, p1.y
                )
            }

            fillPath.lineTo(points.last().x, height)
            fillPath.close()

            // Draw gradient fill below line
            drawPath(
                path = fillPath,
                brush = Brush.verticalGradient(
                    colors = listOf(
                        SkyBluePrimary.copy(alpha = 0.45f),
                        MintGreenAccent.copy(alpha = 0.20f),
                        Color.Transparent
                    ),
                    startY = 0f,
                    endY = height
                )
            )

            // Draw line
            drawPath(
                path = linePath,
                color = SkyBluePrimary,
                style = Stroke(
                    width = 3.5.dp.toPx(),
                    cap = StrokeCap.Round
                )
            )

            // Draw day markers & data points
            val labelColorInt = if (colors.isDark) {
                android.graphics.Color.parseColor("#9EABB8")
            } else {
                android.graphics.Color.parseColor("#6B7280")
            }
            val textPaint = android.graphics.Paint().apply {
                color = labelColorInt
                textSize = density.run { 12.sp.toPx() }
                textAlign = android.graphics.Paint.Align.CENTER
                isAntiAlias = true
            }

            val badgeColorInt = if (colors.isDark) {
                android.graphics.Color.parseColor("#F9FAFB")
            } else {
                android.graphics.Color.parseColor("#121417")
            }
            val badgeTextPaint = android.graphics.Paint().apply {
                color = badgeColorInt
                textSize = density.run { 11.sp.toPx() }
                textAlign = android.graphics.Paint.Align.CENTER
                isFakeBoldText = true
                isAntiAlias = true
            }

            points.forEachIndexed { i, pt ->
                // Outer glow circle
                drawCircle(
                    color = SkyBluePrimary.copy(alpha = 0.25f),
                    radius = 7.dp.toPx(),
                    center = pt
                )
                // Main circle
                val innerDotColor = if (selectedIndex == i) {
                    MintGreenAccent
                } else if (colors.isDark) {
                    Color(0xFF1A1E24)
                } else {
                    Color.White
                }
                drawCircle(
                    color = innerDotColor,
                    radius = 4.5.dp.toPx(),
                    center = pt
                )
                drawCircle(
                    color = SkyBluePrimary,
                    radius = 4.5.dp.toPx(),
                    center = pt,
                    style = Stroke(width = 2.dp.toPx())
                )

                // Day marker label at bottom
                val label = displayData[i].dayName
                drawContext.canvas.nativeCanvas.drawText(
                    label,
                    pt.x,
                    height + 26f,
                    textPaint
                )

                // If selected or final point, show score badge above
                if (selectedIndex == i || (selectedIndex == null && i == points.size - 1)) {
                    val scoreText = "${displayData[i].readinessLevel}%"
                    drawContext.canvas.nativeCanvas.drawText(
                        scoreText,
                        pt.x,
                        pt.y - 14f,
                        badgeTextPaint
                    )
                }
            }
        }
    }
}
