package com.example.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.DarkCardBlack
import com.example.ui.theme.MintGreenAccent
import com.example.ui.theme.MintGreenDark
import com.example.ui.theme.SkyBluePrimary
import com.example.ui.theme.SkyBlueLight

@Composable
fun CircularReadinessMeter(
    percentage: Int,
    modifier: Modifier = Modifier,
    size: Dp = 58.dp,
    strokeWidth: Dp = 6.dp,
    textColor: Color = Color.White,
    trackColor: Color = Color(0x33FFFFFF)
) {
    val animatedProgress by animateFloatAsState(
        targetValue = percentage / 100f,
        animationSpec = tween(durationMillis = 800),
        label = "readiness_arc"
    )

    Box(
        modifier = modifier.size(size),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.size(size)) {
            // Background track
            drawCircle(
                color = trackColor,
                radius = (size.toPx() - strokeWidth.toPx()) / 2f,
                style = Stroke(width = strokeWidth.toPx())
            )

            // Progress arc
            drawArc(
                brush = Brush.sweepGradient(
                    0.0f to SkyBluePrimary,
                    0.7f to MintGreenAccent,
                    1.0f to SkyBluePrimary
                ),
                startAngle = -90f,
                sweepAngle = 360f * animatedProgress,
                useCenter = false,
                style = Stroke(
                    width = strokeWidth.toPx(),
                    cap = StrokeCap.Round
                )
            )
        }

        Text(
            text = "$percentage%",
            style = MaterialTheme.typography.labelLarge.copy(
                fontWeight = FontWeight.Bold,
                fontSize = if (size > 50.dp) 13.sp else 10.sp
            ),
            color = textColor
        )
    }
}

@Composable
fun ReadinessPillBadge(
    percentage: Int,
    modifier: Modifier = Modifier,
    isDarkCard: Boolean = false
) {
    val bg = if (isDarkCard) {
        Color(0x3349CBEB)
    } else {
        SkyBlueLight
    }
    val contentColor = if (isDarkCard) {
        MintGreenAccent
    } else {
        Color(0xFF0284A5)
    }

    Box(
        modifier = modifier
            .background(
                color = bg,
                shape = RoundedCornerShape(50)
            )
            .padding(horizontal = 10.dp, vertical = 4.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = "$percentage% Ready",
                style = MaterialTheme.typography.labelMedium.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp
                ),
                color = contentColor
            )
        }
    }
}
