package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ElectricBolt
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.RocketLaunch
import androidx.compose.material.icons.filled.Spa
import androidx.compose.material.icons.filled.BatteryChargingFull
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipRect
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.ui.theme.AppTheme
import com.example.ui.theme.DarkCardBlack
import com.example.ui.theme.DarkSurfaceElevated
import com.example.ui.theme.MintGreenAccent
import com.example.ui.theme.MintGreenDark
import com.example.ui.theme.SkyBlueDark
import com.example.ui.theme.SkyBlueLight
import com.example.ui.theme.SkyBluePrimary
import com.example.ui.viewmodel.DailyFeelingOption
import com.example.ui.viewmodel.FeelingEffectType
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

/**
 * Fullscreen celebration overlay that renders the custom interactive animation
 * followed by the themed pop-up dialog.
 */
@Composable
fun DailyFeelingEffectOverlay(
    option: DailyFeelingOption,
    onDismiss: () -> Unit
) {
    var showPopup by remember { mutableStateOf(false) }

    // Start with the specific effect animation, then reveal popup smoothly
    LaunchedEffect(option) {
        delay(700)
        showPopup = true
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .testTag("feeling_effect_overlay"),
        contentAlignment = Alignment.Center
    ) {
        // Semi-transparent backdrop
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.65f))
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null
                ) { /* Consume taps */ }
        )

        // Custom Visual Animation Effect based on option
        when (option.effectType) {
            FeelingEffectType.THUNDER_LIGHTNING -> {
                ThunderLightningEffect()
            }
            FeelingEffectType.FLAMES -> {
                FlamesEffect()
            }
            FeelingEffectType.ROCKET_TAKEOFF -> {
                RocketTakeOffEffect()
            }
            FeelingEffectType.PLANT_SPURT -> {
                PlantSpurtEffect()
            }
            FeelingEffectType.BATTERY_CHARGING -> {
                BatteryChargingEffect()
            }
        }

        // Animated Pop-up Dialog
        AnimatedVisibility(
            visible = showPopup,
            enter = fadeIn(tween(350)) + scaleIn(spring(dampingRatio = 0.65f, stiffness = 400f)),
            exit = fadeOut(tween(250)) + scaleOut(tween(200)),
            modifier = Modifier.padding(horizontal = 24.dp)
        ) {
            DailyFeelingPopupCard(
                option = option,
                onConfirm = onDismiss
            )
        }
    }
}

/**
 * 95%: Thunder-Lightning effect with electric flash, jagged branching lightning bolts,
 * and high-voltage spark particles.
 */
@Composable
fun ThunderLightningEffect() {
    val strikeProgress = remember { Animatable(0f) }
    val flashAlpha = remember { Animatable(0f) }

    LaunchedEffect(Unit) {
        // Multiple rapid lightning flashes & strikes
        launch {
            flashAlpha.animateTo(0.7f, tween(60))
            flashAlpha.animateTo(0.1f, tween(80))
            flashAlpha.animateTo(0.9f, tween(70))
            flashAlpha.animateTo(0f, tween(250))
        }
        strikeProgress.animateTo(1f, tween(650, easing = FastOutSlowInEasing))
    }

    // Spark particles
    val particles = remember {
        List(40) {
            val angle = Random.nextFloat() * 2 * Math.PI.toFloat()
            val speed = Random.nextFloat() * 320f + 140f
            Triple(angle, speed, Random.nextFloat() * 6f + 3f)
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        // Background Electric Flash
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFFE0F7FD).copy(alpha = flashAlpha.value * 0.4f))
        )

        Canvas(modifier = Modifier.fillMaxSize()) {
            val width = size.width
            val height = size.height
            val p = strikeProgress.value

            if (p > 0.05f) {
                // Primary jagged lightning bolt
                val mainPath = Path()
                mainPath.moveTo(width * 0.48f, 0f)
                val segments = 8
                val startY = 0f
                val endY = height * 0.62f * p

                var currentX = width * 0.48f
                var currentY = startY

                for (i in 1..segments) {
                    val targetY = (endY / segments) * i
                    val jitterX = if (i % 2 == 0) (width * 0.08f) else -(width * 0.07f)
                    val nextX = currentX + jitterX
                    mainPath.lineTo(nextX, targetY)
                    currentX = nextX
                    currentY = targetY
                }

                // Electric glow blur
                drawPath(
                    path = mainPath,
                    color = Color(0xFF49CBEB).copy(alpha = (1f - p * 0.4f)),
                    style = Stroke(width = 18f, cap = StrokeCap.Round, join = StrokeJoin.Round)
                )
                // Hot core white-yellow bolt
                drawPath(
                    path = mainPath,
                    color = Color(0xFFFFFBEB),
                    style = Stroke(width = 6f, cap = StrokeCap.Round, join = StrokeJoin.Round)
                )

                // Secondary branching fork
                val branchPath = Path()
                branchPath.moveTo(width * 0.52f, height * 0.22f)
                branchPath.lineTo(width * 0.68f, height * 0.32f * p)
                branchPath.lineTo(width * 0.74f, height * 0.46f * p)

                drawPath(
                    path = branchPath,
                    color = Color(0xFF38BDF8).copy(alpha = 0.8f * (1f - p * 0.5f)),
                    style = Stroke(width = 6f, cap = StrokeCap.Round)
                )
                drawPath(
                    path = branchPath,
                    color = Color.White,
                    style = Stroke(width = 2.5f, cap = StrokeCap.Round)
                )

                // Spark burst radiating from strike ground
                val sparkOrigin = Offset(currentX, currentY)
                val sparkProgress = ((p - 0.2f) / 0.8f).coerceIn(0f, 1f)
                particles.forEach { (angle, speed, particleSize) ->
                    val dist = speed * sparkProgress
                    val px = sparkOrigin.x + cos(angle) * dist
                    val py = sparkOrigin.y + sin(angle) * dist
                    val alpha = (1f - sparkProgress).coerceIn(0f, 1f)

                    drawCircle(
                        color = if (Random.nextBoolean()) Color(0xFFFDE047) else Color(0xFF38BDF8),
                        radius = (particleSize * (1f - sparkProgress * 0.5f)).coerceAtLeast(0.5f),
                        center = Offset(px, py),
                        alpha = alpha
                    )
                }
            }
        }
    }
}

/**
 * 85%: Flames effect with animated rising flame tongues, heat wave glow,
 * and dancing burning embers.
 */
@Composable
fun FlamesEffect() {
    val flameAnim = remember { Animatable(0f) }

    LaunchedEffect(Unit) {
        flameAnim.animateTo(1f, tween(1000, easing = LinearEasing))
    }

    val infiniteTransition = rememberInfiniteTransition(label = "flames")
    val waveOffset by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 2 * Math.PI.toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "flame_wave"
    )

    data class Ember(
        val xRatio: Float,
        val speed: Float,
        val radius: Float,
        val color: Color
    )

    val embers = remember {
        List(45) {
            Ember(
                xRatio = Random.nextFloat() * 0.7f + 0.15f,
                speed = Random.nextFloat() * 450f + 250f,
                radius = Random.nextFloat() * 7f + 3f,
                color = when (Random.nextInt(4)) {
                    0 -> Color(0xFFFDE047) // Yellow
                    1 -> Color(0xFFFB923C) // Orange
                    2 -> Color(0xFFEF4444) // Crimson Red
                    else -> Color(0xFFF97316) // Flame Amber
                }
            )
        }
    }

    Canvas(modifier = Modifier.fillMaxSize()) {
        val width = size.width
        val height = size.height
        val progress = flameAnim.value

        // Ambient Bottom Warm Fire Glow
        val glowRadius = width * 0.7f * progress
        if (progress > 0.01f && glowRadius > 1f) {
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        Color(0xFFFF5722).copy(alpha = 0.65f),
                        Color(0xFFE65100).copy(alpha = 0.35f),
                        Color.Transparent
                    ),
                    center = Offset(width * 0.5f, height * 0.85f),
                    radius = glowRadius
                )
            )
        }

        // Rising embers with sinusoidal horizontal drift
        embers.forEachIndexed { i, ember ->
            val emberProgress = ((progress * 1.3f) - (i * 0.015f)).coerceIn(0f, 1f)
            if (emberProgress > 0f) {
                val baseY = height * 0.88f
                val currentY = baseY - (ember.speed * emberProgress)
                val driftX = sin(waveOffset + i) * 32f
                val currentX = (width * ember.xRatio) + driftX
                val alpha = (1f - emberProgress).coerceIn(0f, 1f)

                drawCircle(
                    color = ember.color,
                    radius = (ember.radius * (1f - emberProgress * 0.4f)).coerceAtLeast(0.5f),
                    center = Offset(currentX, currentY),
                    alpha = alpha
                )
            }
        }

        // Stylized Flame Petals licking upward
        val flameCenters = listOf(0.38f, 0.46f, 0.54f, 0.62f)
        if (progress > 0.01f) {
            flameCenters.forEachIndexed { idx, centerRatio ->
                val flameHeight = (180f + sin(waveOffset + idx * 1.5f) * 45f) * progress
                if (flameHeight > 1f) {
                    val flamePath = Path().apply {
                        val baseX = width * centerRatio
                        val baseY = height * 0.82f
                        moveTo(baseX - 35f, baseY)
                        quadraticTo(
                            baseX - 45f, baseY - flameHeight * 0.5f,
                            baseX + sin(waveOffset + idx) * 15f, baseY - flameHeight
                        )
                        quadraticTo(
                            baseX + 45f, baseY - flameHeight * 0.5f,
                            baseX + 35f, baseY
                        )
                        close()
                    }

                    drawPath(
                        path = flamePath,
                        brush = Brush.verticalGradient(
                            colors = listOf(
                                Color(0xFFFFFBEB),
                                Color(0xFFF59E0B),
                                Color(0xFFDC2626)
                            ),
                            startY = height * 0.82f - flameHeight,
                            endY = height * 0.82f
                        ),
                        alpha = 0.85f
                    )
                }
            }
        }
    }
}

/**
 * 70%: Rocket take-off effect with rocket icon blasting upward,
 * expanding billowing smoke puffs, exhaust fire trail, and stars.
 */
@Composable
fun RocketTakeOffEffect() {
    val rocketAnim = remember { Animatable(0f) }

    LaunchedEffect(Unit) {
        rocketAnim.animateTo(1f, tween(900, easing = FastOutSlowInEasing))
    }

    data class SmokePuff(
        val xOffset: Float,
        val yOffset: Float,
        val maxRadius: Float,
        val delay: Float
    )

    val smokePuffs = remember {
        List(24) {
            SmokePuff(
                xOffset = (Random.nextFloat() - 0.5f) * 160f,
                yOffset = Random.nextFloat() * 90f,
                maxRadius = Random.nextFloat() * 45f + 25f,
                delay = Random.nextFloat() * 0.3f
            )
        }
    }

    Canvas(modifier = Modifier.fillMaxSize()) {
        val width = size.width
        val height = size.height
        val p = rocketAnim.value

        val startY = height * 0.72f
        val currentRocketY = startY - (height * 0.58f * p)
        val rocketX = width * 0.5f

        // Expanding launch smoke rings at the base
        smokePuffs.forEach { puff ->
            val puffProgress = ((p - puff.delay) / (1f - puff.delay)).coerceIn(0f, 1f)
            if (puffProgress > 0.02f) {
                val puffX = rocketX + puff.xOffset * puffProgress
                val puffY = startY + puff.yOffset * puffProgress
                val alpha = (1f - puffProgress).coerceIn(0f, 0.7f)
                val puffRadius = (puff.maxRadius * puffProgress).coerceAtLeast(0.5f)

                drawCircle(
                    color = Color(0xFF94A3B8),
                    radius = puffRadius,
                    center = Offset(puffX, puffY),
                    alpha = alpha
                )
            }
        }

        // Burning Rocket Exhaust Fire Plume
        if (p < 0.95f) {
            val plumeLength = 110f + (p * 60f)
            val plumePath = Path().apply {
                moveTo(rocketX - 18f, currentRocketY + 30f)
                lineTo(rocketX + 18f, currentRocketY + 30f)
                lineTo(rocketX, currentRocketY + 30f + plumeLength)
                close()
            }

            drawPath(
                path = plumePath,
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFFFFFBEB),
                        Color(0xFFF97316),
                        Color(0xFFEF4444),
                        Color.Transparent
                    ),
                    startY = currentRocketY + 30f,
                    endY = currentRocketY + 30f + plumeLength
                )
            )
        }
    }

    // Rocket Composable Icon shooting upwards
    val progress = rocketAnim.value
    Box(
        modifier = Modifier
            .fillMaxSize(),
        contentAlignment = Alignment.TopCenter
    ) {
        val rocketOffset = (480.dp * (1f - progress))
        Box(
            modifier = Modifier
                .offset(y = rocketOffset + 120.dp)
                .scale(1f + progress * 0.25f)
                .size(72.dp)
                .background(SkyBluePrimary, CircleShape)
                .shadow(12.dp, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.RocketLaunch,
                contentDescription = "Rocket Launch",
                tint = DarkCardBlack,
                modifier = Modifier.size(42.dp)
            )
        }
    }
}

/**
 * 55%: Plant spurt effect with rapid stem spurt, spring-blooming leaves,
 * and swirling nature pollen particles.
 */
@Composable
fun PlantSpurtEffect() {
    val growth = remember { Animatable(0f) }

    LaunchedEffect(Unit) {
        growth.animateTo(1f, spring(dampingRatio = 0.55f, stiffness = 280f))
    }

    data class Pollen(
        val angle: Float,
        val radius: Float,
        val speed: Float,
        val color: Color
    )

    val pollens = remember {
        List(30) {
            Pollen(
                angle = Random.nextFloat() * 2 * Math.PI.toFloat(),
                radius = Random.nextFloat() * 5f + 2.5f,
                speed = Random.nextFloat() * 200f + 80f,
                color = if (Random.nextBoolean()) Color(0xFF86F5D0) else Color(0xFF34D399)
            )
        }
    }

    Canvas(modifier = Modifier.fillMaxSize()) {
        val width = size.width
        val height = size.height
        val g = growth.value

        val startX = width * 0.5f
        val startY = height * 0.72f
        val stemHeight = 220f * g

        // Sprouting Green Vine Stem
        val stemPath = Path().apply {
            moveTo(startX, startY)
            quadraticTo(
                startX - 24f * g, startY - stemHeight * 0.5f,
                startX, startY - stemHeight
            )
        }

        drawPath(
            path = stemPath,
            brush = Brush.verticalGradient(
                colors = listOf(Color(0xFF34D399), Color(0xFF059669)),
                startY = startY - stemHeight,
                endY = startY
            ),
            style = Stroke(width = 12f * g, cap = StrokeCap.Round)
        )

        // Branching leaves unfurling
        if (g > 0.35f) {
            val leafScale = ((g - 0.35f) / 0.65f).coerceIn(0f, 1f)
            if (leafScale > 0.02f) {
                val leafRadius = (35f * leafScale).coerceAtLeast(1f)

                // Left leaf
                val leftLeafY = startY - (stemHeight * 0.45f)
                drawOval(
                    brush = Brush.radialGradient(
                        colors = listOf(Color(0xFF86F5D0), Color(0xFF10B981)),
                        center = Offset(startX - 28f * leafScale, leftLeafY),
                        radius = leafRadius
                    ),
                    topLeft = Offset(startX - 55f * leafScale, leftLeafY - 14f * leafScale),
                    size = Size(50f * leafScale, 26f * leafScale)
                )

                // Right leaf
                val rightLeafY = startY - (stemHeight * 0.7f)
                drawOval(
                    brush = Brush.radialGradient(
                        colors = listOf(Color(0xFF86F5D0), Color(0xFF10B981)),
                        center = Offset(startX + 28f * leafScale, rightLeafY),
                        radius = leafRadius
                    ),
                    topLeft = Offset(startX + 5f * leafScale, rightLeafY - 14f * leafScale),
                    size = Size(50f * leafScale, 26f * leafScale)
                )

                // Blooming Flower Bud / Sprout at Tip
                drawCircle(
                    color = Color(0xFF86F5D0),
                    radius = (16f * leafScale).coerceAtLeast(0.5f),
                    center = Offset(startX, startY - stemHeight)
                )
                drawCircle(
                    color = Color(0xFFFDE047),
                    radius = (8f * leafScale).coerceAtLeast(0.5f),
                    center = Offset(startX, startY - stemHeight)
                )
            }
        }

        // Swirling Pollen Particles
        val tipPos = Offset(startX, startY - stemHeight)
        pollens.forEach { p ->
            val dist = p.speed * g
            val px = tipPos.x + cos(p.angle) * dist
            val py = tipPos.y + sin(p.angle) * dist * 0.65f
            val alpha = (1f - g * 0.7f).coerceIn(0f, 1f)

            drawCircle(
                color = p.color,
                radius = p.radius.coerceAtLeast(0.5f),
                center = Offset(px, py),
                alpha = alpha
            )
        }
    }
}

/**
 * 40%: Battery charging effect with glowing neon battery outline,
 * animated liquid/charge level filling up, and pulsing energy bolt.
 */
@Composable
fun BatteryChargingEffect() {
    val fillProgress = remember { Animatable(0f) }
    val boltPulse = remember { Animatable(1f) }

    LaunchedEffect(Unit) {
        launch {
            fillProgress.animateTo(1f, tween(850, easing = FastOutSlowInEasing))
        }
        launch {
            while (true) {
                boltPulse.animateTo(1.25f, tween(300))
                boltPulse.animateTo(0.95f, tween(300))
            }
        }
    }

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Canvas(modifier = Modifier.size(160.dp, 220.dp)) {
                val w = size.width
                val h = size.height
                val charge = fillProgress.value

                // Battery Terminal Cap at the top
                drawRoundRect(
                    color = Color(0xFF86F5D0),
                    topLeft = Offset(w * 0.35f, 0f),
                    size = Size(w * 0.3f, 16f),
                    cornerRadius = CornerRadius(6f, 6f)
                )

                // Outer Battery Frame
                drawRoundRect(
                    color = Color(0xFF334155),
                    topLeft = Offset(10f, 20f),
                    size = Size(w - 20f, h - 30f),
                    cornerRadius = CornerRadius(24f, 24f),
                    style = Stroke(width = 8f)
                )

                // Neon Green Charge Filling Up
                val innerX = 20f
                val innerMaxH = h - 50f
                val currentChargeH = innerMaxH * charge
                val innerY = 30f + (innerMaxH - currentChargeH)

                if (charge > 0.05f && currentChargeH > 1f) {
                    drawRoundRect(
                        brush = Brush.verticalGradient(
                            colors = listOf(
                                Color(0xFF86F5D0),
                                Color(0xFF10B981),
                                Color(0xFF047857)
                            ),
                            startY = innerY,
                            endY = innerY + currentChargeH
                        ),
                        topLeft = Offset(innerX, innerY),
                        size = Size(w - 40f, currentChargeH),
                        cornerRadius = CornerRadius(16f, 16f)
                    )
                }

                // Concentric Energy Pulse Rings
                val pulseRadius = ((w * 0.5f) + (charge * 40f)).coerceAtLeast(1f)
                drawCircle(
                    color = Color(0xFF86F5D0).copy(alpha = 0.25f * (1f - (charge % 0.5f) * 2)),
                    radius = pulseRadius,
                    center = Offset(w * 0.5f, h * 0.55f),
                    style = Stroke(width = 4f)
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Pulsing Charging Lightning Bolt
            Box(
                modifier = Modifier
                    .scale(boltPulse.value)
                    .background(Color(0xFF86F5D0).copy(alpha = 0.2f), CircleShape)
                    .padding(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Bolt,
                    contentDescription = "Charging Bolt",
                    tint = Color(0xFF86F5D0),
                    modifier = Modifier.size(36.dp)
                )
            }
        }
    }
}

/**
 * Themed Pop-up Card celebrating the selected readiness option
 */
@Composable
fun DailyFeelingPopupCard(
    option: DailyFeelingOption,
    onConfirm: () -> Unit
) {
    val colors = AppTheme.colors

    val (accentColor, darkAccentColor, iconVector, headlineTitle, motivationText) = when (option.effectType) {
        FeelingEffectType.THUNDER_LIGHTNING -> {
            Tuple5(
                SkyBluePrimary,
                Color(0xFF0369A1),
                Icons.Default.ElectricBolt,
                "Fired Up",
                "High-voltage clarity! You have the momentum and drive to tackle your biggest bets today. Strike while the energy is electric!"
            )
        }
        FeelingEffectType.FLAMES -> {
            Tuple5(
                Color(0xFFF97316),
                Color(0xFFC2410C),
                Icons.Default.LocalFireDepartment,
                "Super Ready",
                "Full blaze ahead! You're burning through obstacles and primed for high-velocity execution. Ready to build and ship!"
            )
        }
        FeelingEffectType.ROCKET_TAKEOFF -> {
            Tuple5(
                Color(0xFF38BDF8),
                Color(0xFF0284C7),
                Icons.Default.RocketLaunch,
                "Getting There",
                "Trajectory locked in! You're breaking through inertia and picking up serious launch speed. Every small action builds orbital momentum."
            )
        }
        FeelingEffectType.PLANT_SPURT -> {
            Tuple5(
                MintGreenAccent,
                MintGreenDark,
                Icons.Default.Spa,
                "Explorer",
                "Curiosity in full bloom! You're gathering insights, testing fresh angles, and planting seeds that will grow into high-conviction ideas."
            )
        }
        FeelingEffectType.BATTERY_CHARGING -> {
            Tuple5(
                Color(0xFF34D399),
                Color(0xFF059669),
                Icons.Default.BatteryChargingFull,
                "Recharging",
                "Strategic recovery mode! Rest and reflection are essential parts of the founder cycle. Powering up to hit the ground running soon."
            )
        }
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(24.dp, RoundedCornerShape(32.dp))
            .border(
                BorderStroke(2.dp, Brush.linearGradient(listOf(accentColor, accentColor.copy(alpha = 0.3f)))),
                RoundedCornerShape(32.dp)
            )
            .testTag("daily_feeling_popup"),
        shape = RoundedCornerShape(32.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (colors.isDark) DarkCardBlack else Color.White
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(26.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Dismiss button at top right
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                IconButton(
                    onClick = onConfirm,
                    modifier = Modifier
                        .size(32.dp)
                        .background(
                            if (colors.isDark) DarkSurfaceElevated else Color(0xFFF1F5F9),
                            CircleShape
                        )
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Close",
                        tint = colors.textSecondary,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            // Big Animated Badge
            Box(
                modifier = Modifier
                    .size(88.dp)
                    .background(
                        Brush.radialGradient(
                            colors = listOf(accentColor.copy(alpha = 0.35f), Color.Transparent),
                            radius = 120f
                        ),
                        CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(68.dp)
                        .background(accentColor, CircleShape)
                        .shadow(8.dp, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = iconVector,
                        contentDescription = null,
                        tint = DarkCardBlack,
                        modifier = Modifier.size(36.dp)
                    )
                }
            }

            // Headline Title & Percentage Pill
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = iconVector,
                        contentDescription = null,
                        tint = accentColor,
                        modifier = Modifier.size(24.dp)
                    )
                    Text(
                        text = headlineTitle,
                        style = MaterialTheme.typography.headlineSmall.copy(
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 24.sp
                        ),
                        color = colors.textPrimary,
                        textAlign = TextAlign.Center
                    )
                }

                Surface(
                    shape = RoundedCornerShape(50),
                    color = accentColor.copy(alpha = 0.18f),
                    border = BorderStroke(1.dp, accentColor.copy(alpha = 0.5f))
                ) {
                    Text(
                        text = "${option.readiness}% Readiness Recorded",
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        ),
                        color = if (colors.isDark) accentColor else darkAccentColor,
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp)
                    )
                }
            }

            // Motivational Context Text
            Text(
                text = motivationText,
                style = MaterialTheme.typography.bodyMedium.copy(
                    lineHeight = 22.sp
                ),
                color = colors.textSecondary,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 4.dp)
            )

            Spacer(modifier = Modifier.height(4.dp))

            // Confirm Action Button
            Button(
                onClick = onConfirm,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .testTag("feeling_popup_confirm_button"),
                shape = RoundedCornerShape(50),
                colors = ButtonDefaults.buttonColors(
                    containerColor = accentColor,
                    contentColor = DarkCardBlack
                )
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = iconVector,
                        contentDescription = null,
                        tint = DarkCardBlack,
                        modifier = Modifier.size(18.dp)
                    )
                    Text(
                        text = when (option.effectType) {
                            FeelingEffectType.THUNDER_LIGHTNING -> "Let's Strike"
                            FeelingEffectType.FLAMES -> "Keep The Fire Burning"
                            FeelingEffectType.ROCKET_TAKEOFF -> "Engage Thrusters"
                            FeelingEffectType.PLANT_SPURT -> "Keep Exploring"
                            FeelingEffectType.BATTERY_CHARGING -> "Power Up"
                        },
                        style = MaterialTheme.typography.labelLarge.copy(
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 15.sp
                        )
                    )
                }
            }
        }
    }
}

private data class Tuple5<A, B, C, D, E>(
    val a: A, val b: B, val c: C, val d: D, val e: E
)

