package com.example.ui.screens.auth

import android.app.Activity
import android.content.Context
import android.provider.Settings
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.EaseInOutSine
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.QrCode2
import androidx.compose.material.icons.filled.RocketLaunch
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import kotlinx.coroutines.launch
import kotlin.math.absoluteValue

// Strict color palette: sky blue #49CBEB, mint #86F5D0, white, and near-black
private val ColorSkyBlue = Color(0xFF49CBEB)
private val ColorMint = Color(0xFF86F5D0)
private val ColorPureWhite = Color(0xFFFFFFFF)
private val ColorNearBlack = Color(0xFF0B0F19)
private val ColorDarkCard = Color(0xFF151B28)
private val ColorDarkBorder = Color(0xFF232D3F)
private val ColorGoogleDarkText = Color(0xFF1F1F1F)

data class OnboardingSlide(
    val title: String,
    val description: String,
    val icon: ImageVector,
    val iconTint: Color
)

fun isReducedMotionEnabled(context: Context): Boolean {
    return try {
        val durationScale = Settings.Global.getFloat(
            context.contentResolver,
            Settings.Global.ANIMATOR_DURATION_SCALE,
            1f
        )
        val transitionScale = Settings.Global.getFloat(
            context.contentResolver,
            Settings.Global.TRANSITION_ANIMATION_SCALE,
            1f
        )
        durationScale == 0f || transitionScale == 0f
    } catch (_: Exception) {
        false
    }
}

@Composable
fun SignInScreen(
    isLoading: Boolean,
    errorMessage: String?,
    onSignInClick: (Activity) -> Unit,
    onContinueWithoutAccount: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val activity = context as? Activity
    val coroutineScope = rememberCoroutineScope()
    val hapticFeedback = LocalHapticFeedback.current
    val density = LocalDensity.current.density

    // Detect system reduced motion setting
    val isReducedMotion = remember(context) { isReducedMotionEnabled(context) }

    var showTermsDialog by remember { mutableStateOf(false) }
    var showPrivacyDialog by remember { mutableStateOf(false) }

    val slides = remember {
        listOf(
            OnboardingSlide(
                title = "Know when you’re ready.",
                description = "Score each idea on what you have and what’s missing.",
                icon = Icons.Default.Speed,
                iconTint = ColorSkyBlue
            ),
            OnboardingSlide(
                title = "Build ideas together.",
                description = "Invite teammates and decide who contributes and who launches.",
                icon = Icons.Default.Group,
                iconTint = ColorMint
            ),
            OnboardingSlide(
                title = "Connect in seconds.",
                description = "Share your personal code or QR to link up with collaborators.",
                icon = Icons.Default.QrCode2,
                iconTint = ColorSkyBlue
            ),
            OnboardingSlide(
                title = "Track every stage.",
                description = "Follow ideas from planning to started, done, and live.",
                icon = Icons.Default.RocketLaunch,
                iconTint = ColorMint
            )
        )
    }

    val pagerState = rememberPagerState(pageCount = { slides.size })

    // Ambient floating rocket animation (slow: 2800ms)
    val infiniteTransition = rememberInfiniteTransition(label = "rocket_ambient")

    val floatOffsetY by if (!isReducedMotion) {
        infiniteTransition.animateFloat(
            initialValue = -7f,
            targetValue = 7f,
            animationSpec = infiniteRepeatable(
                animation = tween(durationMillis = 2800, easing = EaseInOutSine),
                repeatMode = RepeatMode.Reverse
            ),
            label = "rocket_float"
        )
    } else {
        remember { mutableFloatStateOf(0f) }
    }

    // Ambient pulsing glow animation (slow: 2400ms)
    val glowAlpha by if (!isReducedMotion) {
        infiniteTransition.animateFloat(
            initialValue = 0.16f,
            targetValue = 0.36f,
            animationSpec = infiniteRepeatable(
                animation = tween(durationMillis = 2400, easing = EaseInOutSine),
                repeatMode = RepeatMode.Reverse
            ),
            label = "glow_alpha"
        )
    } else {
        remember { mutableFloatStateOf(0.24f) }
    }

    val glowScale by if (!isReducedMotion) {
        infiniteTransition.animateFloat(
            initialValue = 0.94f,
            targetValue = 1.06f,
            animationSpec = infiniteRepeatable(
                animation = tween(durationMillis = 2400, easing = EaseInOutSine),
                repeatMode = RepeatMode.Reverse
            ),
            label = "glow_scale"
        )
    } else {
        remember { mutableFloatStateOf(1f) }
    }

    // Friendly error message transformation
    val friendlyError = remember(errorMessage) {
        when {
            errorMessage == null -> null
            errorMessage.contains("cancel", ignoreCase = true) ->
                "Sign-in was cancelled. Tap 'Sign in with Google' when you're ready."
            errorMessage.contains("network", ignoreCase = true) ||
                    errorMessage.contains("offline", ignoreCase = true) ||
                    errorMessage.contains("connection", ignoreCase = true) ||
                    errorMessage.contains("host", ignoreCase = true) ->
                "No internet connection. Please check your network and try again."
            else -> errorMessage
        }
    }

    // Sign-in button press interaction and scale to 0.96 with haptic feedback
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    LaunchedEffect(isPressed) {
        if (isPressed) {
            hapticFeedback.performHapticFeedback(HapticFeedbackType.TextHandleMove)
        }
    }

    val buttonScale by animateFloatAsState(
        targetValue = if (isPressed && !isLoading && !isReducedMotion) 0.96f else 1f,
        animationSpec = tween(
            durationMillis = if (isReducedMotion) 0 else 180,
            easing = FastOutSlowInEasing
        ),
        label = "btn_press_scale"
    )

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(ColorNearBlack)
            .statusBarsPadding()
            .navigationBarsPadding()
            .testTag("sign_in_screen"),
        contentAlignment = Alignment.Center
    ) {
        // Ambient background glow (sky blue & mint on near-black) with slow pulse
        Box(
            modifier = Modifier
                .size(360.dp)
                .graphicsLayer {
                    scaleX = glowScale
                    scaleY = glowScale
                }
                .background(
                    Brush.radialGradient(
                        colors = listOf(
                            ColorSkyBlue.copy(alpha = glowAlpha),
                            ColorMint.copy(alpha = glowAlpha * 0.6f),
                            Color.Transparent
                        )
                    )
                )
        )

        // Scrollable content ensuring support for largest system font sizes
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp, vertical = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            Spacer(modifier = Modifier.height(8.dp))

            // Rocket Logo with Float Animation & Glow
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Box(
                    modifier = Modifier
                        .graphicsLayer {
                            translationY = floatOffsetY * density
                        }
                        .size(80.dp)
                        .clip(CircleShape)
                        .background(
                            Brush.linearGradient(
                                colors = listOf(ColorSkyBlue, ColorMint)
                            )
                        )
                        .padding(3.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(CircleShape)
                            .background(ColorDarkCard),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.RocketLaunch,
                            contentDescription = "Idea Matrix Rocket Logo",
                            tint = ColorSkyBlue,
                            modifier = Modifier.size(42.dp)
                        )
                    }
                }

                Text(
                    text = "Idea Matrix",
                    style = MaterialTheme.typography.headlineLarge.copy(
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 32.sp,
                        letterSpacing = (-0.5).sp
                    ),
                    color = ColorPureWhite
                )
            }

            // Segmented Progress Indicator at the top of the Pager with animated filling
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                slides.indices.forEach { index ->
                    val isSelected = pagerState.currentPage == index
                    val isPassed = pagerState.currentPage > index

                    // Segment fill progress animation (duration 220ms, between 150-300ms)
                    val targetFill = when {
                        isSelected -> 1f
                        isPassed -> 1f
                        else -> 0f
                    }
                    val fillFraction by animateFloatAsState(
                        targetValue = targetFill,
                        animationSpec = tween(
                            durationMillis = if (isReducedMotion) 0 else 220,
                            easing = FastOutSlowInEasing
                        ),
                        label = "segment_fill_$index"
                    )

                    val barColor = if (index % 2 == 0) ColorSkyBlue else ColorMint

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(4.dp)
                            .clip(RoundedCornerShape(2.dp))
                            .background(ColorDarkBorder)
                            .clickable {
                                coroutineScope.launch {
                                    pagerState.animateScrollToPage(index)
                                }
                            }
                    ) {
                        if (fillFraction > 0f) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth(fillFraction)
                                    .height(4.dp)
                                    .clip(RoundedCornerShape(2.dp))
                                    .background(barColor)
                            )
                        }
                    }
                }
            }

            // Swipeable Pager of 4 Onboarding Slides with fade & slide on swipe
            HorizontalPager(
                state = pagerState,
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 210.dp)
            ) { page ->
                val slide = slides[page]

                // Real-time fade and slide effect during swipe gesture
                val pageOffset = ((pagerState.currentPage - page) + pagerState.currentPageOffsetFraction)
                val absOffset = pageOffset.absoluteValue.coerceIn(0f, 1f)

                val slideAlpha = if (isReducedMotion) 1f else (1f - (absOffset * 0.65f))
                val slideTranslationX = if (isReducedMotion) 0f else (-pageOffset * 80f)

                SlideCard(
                    slide = slide,
                    modifier = Modifier.graphicsLayer {
                        alpha = slideAlpha
                        translationX = slideTranslationX
                    }
                )
            }

            // Friendly Error Message (if any)
            if (friendlyError != null) {
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = ColorDarkCard,
                    border = BorderStroke(1.dp, ColorSkyBlue.copy(alpha = 0.6f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = null,
                            tint = ColorSkyBlue,
                            modifier = Modifier.size(20.dp)
                        )
                        Text(
                            text = friendlyError,
                            style = MaterialTheme.typography.bodySmall.copy(
                                fontWeight = FontWeight.Medium,
                                fontSize = 13.sp,
                                lineHeight = 18.sp
                            ),
                            color = ColorPureWhite,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }

            // Official Google Sign-In Button with Press Scale & Morph into Spinner
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                BoxWithConstraints(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    val fullWidth = maxWidth
                    // Morph button width from full width to circle (52.dp) when loading (250ms duration)
                    val targetButtonWidth = if (isLoading) 52.dp else fullWidth
                    val animatedWidth by animateDpAsState(
                        targetValue = targetButtonWidth,
                        animationSpec = tween(
                            durationMillis = if (isReducedMotion) 0 else 250,
                            easing = FastOutSlowInEasing
                        ),
                        label = "button_morph_width"
                    )

                    Button(
                        onClick = {
                            activity?.let { onSignInClick(it) }
                        },
                        enabled = !isLoading && activity != null,
                        interactionSource = interactionSource,
                        shape = RoundedCornerShape(50),
                        contentPadding = PaddingValues(0.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = ColorPureWhite,
                            contentColor = ColorGoogleDarkText,
                            disabledContainerColor = ColorPureWhite.copy(alpha = if (isLoading) 1f else 0.8f),
                            disabledContentColor = ColorGoogleDarkText
                        ),
                        modifier = Modifier
                            .width(animatedWidth)
                            .height(52.dp)
                            .graphicsLayer {
                                scaleX = buttonScale
                                scaleY = buttonScale
                            }
                            .testTag("btn_sign_in_google")
                            .shadow(elevation = 8.dp, shape = RoundedCornerShape(50))
                    ) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            // Loading Spinner when signing in
                            if (isLoading) {
                                CircularProgressIndicator(
                                    color = ColorGoogleDarkText,
                                    strokeWidth = 2.5.dp,
                                    modifier = Modifier.size(22.dp)
                                )
                            } else {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.Center,
                                    modifier = Modifier.padding(horizontal = 16.dp)
                                ) {
                                    // Official Google G Logo vector drawable
                                    Icon(
                                        painter = painterResource(id = R.drawable.ic_google_logo),
                                        contentDescription = "Google",
                                        tint = Color.Unspecified,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Text(
                                        text = "Sign in with Google",
                                        style = MaterialTheme.typography.labelLarge.copy(
                                            fontWeight = FontWeight.SemiBold,
                                            fontSize = 15.sp,
                                            letterSpacing = 0.25.sp
                                        ),
                                        color = ColorGoogleDarkText,
                                        maxLines = 1
                                    )
                                }
                            }
                        }
                    }
                }

                // "Continue without account" text link (opens in local-only mode)
                TextButton(
                    onClick = onContinueWithoutAccount,
                    modifier = Modifier
                        .padding(top = 2.dp)
                        .testTag("btn_continue_guest")
                ) {
                    Text(
                        text = "Continue without account",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 14.sp
                        ),
                        color = ColorMint
                    )
                }
            }

            Spacer(modifier = Modifier.weight(1f, fill = false))

            // Terms & Privacy Links
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(bottom = 8.dp)
            ) {
                Text(
                    text = "Terms",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium
                    ),
                    color = ColorPureWhite.copy(alpha = 0.65f),
                    modifier = Modifier.clickable { showTermsDialog = true }
                )

                Text(
                    text = "•",
                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 12.sp),
                    color = ColorPureWhite.copy(alpha = 0.4f)
                )

                Text(
                    text = "Privacy",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium
                    ),
                    color = ColorPureWhite.copy(alpha = 0.65f),
                    modifier = Modifier.clickable { showPrivacyDialog = true }
                )
            }
        }
    }

    // Terms of Service Dialog
    if (showTermsDialog) {
        AlertDialog(
            onDismissRequest = { showTermsDialog = false },
            title = {
                Text(
                    text = "Terms of Service",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = ColorPureWhite
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "Welcome to Idea Matrix.",
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                        color = ColorSkyBlue
                    )
                    Text(
                        text = "Idea Matrix helps product builders and founders assess, prioritize, and collaborate on startup concepts. By signing in, you agree to create an account to store ideas and connect with collaborators.",
                        style = MaterialTheme.typography.bodySmall.copy(fontSize = 13.sp, lineHeight = 18.sp),
                        color = ColorPureWhite.copy(alpha = 0.85f)
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = { showTermsDialog = false }) {
                    Text("Close", color = ColorMint, fontWeight = FontWeight.Bold)
                }
            },
            containerColor = ColorDarkCard,
            shape = RoundedCornerShape(20.dp)
        )
    }

    // Privacy Policy Dialog
    if (showPrivacyDialog) {
        AlertDialog(
            onDismissRequest = { showPrivacyDialog = false },
            title = {
                Text(
                    text = "Privacy Policy",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = ColorPureWhite
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "Your Privacy Matters.",
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                        color = ColorMint
                    )
                    Text(
                        text = "We use your Google account exclusively to authenticate your identity, generate your unique share code (IM-XXXXXX), and synchronize your ideas securely. Your data is protected by zero-trust Firestore security rules.",
                        style = MaterialTheme.typography.bodySmall.copy(fontSize = 13.sp, lineHeight = 18.sp),
                        color = ColorPureWhite.copy(alpha = 0.85f)
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = { showPrivacyDialog = false }) {
                    Text("Close", color = ColorMint, fontWeight = FontWeight.Bold)
                }
            },
            containerColor = ColorDarkCard,
            shape = RoundedCornerShape(20.dp)
        )
    }
}

@Composable
private fun SlideCard(
    slide: OnboardingSlide,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 4.dp),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = ColorDarkCard),
        border = BorderStroke(1.dp, ColorDarkBorder)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(22.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Large rounded icon card with sky blue or mint tint
            Box(
                modifier = Modifier
                    .size(68.dp)
                    .clip(RoundedCornerShape(20.dp))
                    .background(slide.iconTint.copy(alpha = 0.16f))
                    .border(1.dp, slide.iconTint.copy(alpha = 0.35f), RoundedCornerShape(20.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = slide.icon,
                    contentDescription = null,
                    tint = slide.iconTint,
                    modifier = Modifier.size(34.dp)
                )
            }

            // Bold Title
            Text(
                text = slide.title,
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 19.sp,
                    letterSpacing = (-0.2).sp
                ),
                color = ColorPureWhite,
                textAlign = TextAlign.Center
            )

            // One-line description at least 14sp with high contrast
            Text(
                text = slide.description,
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontSize = 14.sp,
                    lineHeight = 20.sp,
                    fontWeight = FontWeight.Normal
                ),
                color = ColorPureWhite.copy(alpha = 0.92f),
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 4.dp)
            )
        }
    }
}
