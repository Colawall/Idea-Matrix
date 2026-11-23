package com.example.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

data class IdeaMatrixCustomColors(
    val background: Color,
    val cardBackground: Color,
    val cardBorder: Color,
    val searchBarBg: Color,
    val textPrimary: Color,
    val textSecondary: Color,
    val textTertiary: Color,
    val chipBg: Color,
    val chipText: Color,
    val navBarBg: Color,
    val navBarPillBg: Color,
    val navBarPillText: Color,
    val navBarUnselected: Color,
    val divider: Color,
    val isDark: Boolean
)

val LightCustomColors = IdeaMatrixCustomColors(
    background = AppBackground,
    cardBackground = CardWhite,
    cardBorder = Color(0xFFE5E7EB),
    searchBarBg = CardWhite,
    textPrimary = Color(0xFF111827),
    textSecondary = Color(0xFF6B7280),
    textTertiary = Color(0xFF9CA3AF),
    chipBg = Color(0xFFEBECEE),
    chipText = Color(0xFF4B5563),
    navBarBg = CardWhite,
    navBarPillBg = SkyBluePill,
    navBarPillText = DarkCardBlack,
    navBarUnselected = Color(0xFF6B7280),
    divider = Color(0xFFE5E7EB),
    isDark = false
)

val DarkCustomColors = IdeaMatrixCustomColors(
    background = DarkSurfaceDeep,
    cardBackground = DarkCardBlack,
    cardBorder = DarkBorder,
    searchBarBg = DarkSurfaceElevated,
    textPrimary = Color(0xFFF9FAFB),
    textSecondary = Color(0xFF9EABB8),
    textTertiary = Color(0xFF6B7280),
    chipBg = DarkSurfaceElevated,
    chipText = Color(0xFFE5E7EB),
    navBarBg = DarkCardBlack,
    navBarPillBg = Color(0x3349CBEB),
    navBarPillText = SkyBluePrimary,
    navBarUnselected = Color(0xFF9EABB8),
    divider = DarkBorder,
    isDark = true
)

val LocalIdeaMatrixColors = staticCompositionLocalOf { LightCustomColors }

object AppTheme {
    val colors: IdeaMatrixCustomColors
        @Composable
        @ReadOnlyComposable
        get() = LocalIdeaMatrixColors.current
}

private val LightColorScheme = lightColorScheme(
    primary = SkyBluePrimary,
    onPrimary = TextPrimary,
    primaryContainer = SkyBlueLight,
    onPrimaryContainer = SkyBlueDark,
    secondary = MintGreenAccent,
    onSecondary = TextPrimary,
    secondaryContainer = MintGreenLight,
    onSecondaryContainer = MintGreenDark,
    background = AppBackground,
    onBackground = TextPrimary,
    surface = CardWhite,
    onSurface = TextPrimary,
    surfaceVariant = ChipGray,
    onSurfaceVariant = ChipGrayText,
    outline = BorderLight
)

private val DarkColorScheme = darkColorScheme(
    primary = SkyBluePrimary,
    onPrimary = DarkCardBlack,
    primaryContainer = DarkSurfaceElevated,
    onPrimaryContainer = SkyBluePrimary,
    secondary = MintGreenAccent,
    onSecondary = DarkCardBlack,
    secondaryContainer = DarkSurfaceElevated,
    onSecondaryContainer = MintGreenAccent,
    background = DarkSurfaceDeep,
    onBackground = TextWhite,
    surface = DarkCardBlack,
    onSurface = TextWhite,
    surfaceVariant = DarkSurfaceElevated,
    onSurfaceVariant = TextWhiteMuted,
    outline = DarkBorder
)

@Composable
fun IdeaMatrixTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme
    val customColors = if (darkTheme) DarkCustomColors else LightCustomColors

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as? Activity)?.window
            if (window != null) {
                window.statusBarColor = (if (darkTheme) DarkSurfaceDeep else AppBackground).toArgb()
                window.navigationBarColor = (if (darkTheme) DarkCardBlack else CardWhite).toArgb()
                val insetsController = WindowCompat.getInsetsController(window, view)
                insetsController.isAppearanceLightStatusBars = !darkTheme
                insetsController.isAppearanceLightNavigationBars = !darkTheme
            }
        }
    }

    CompositionLocalProvider(LocalIdeaMatrixColors provides customColors) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = Typography,
            content = content
        )
    }
}
