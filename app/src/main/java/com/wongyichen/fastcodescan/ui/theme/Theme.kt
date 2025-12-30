package com.wongyichen.fastcodescan.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val LightColorScheme = lightColorScheme(
    primary = AppColors.Primary,
    onPrimary = AppColors.PrimaryForeground,
    primaryContainer = AppColors.Primary,
    onPrimaryContainer = AppColors.PrimaryForeground,
    secondary = AppColors.Secondary,
    onSecondary = AppColors.SecondaryForeground,
    secondaryContainer = AppColors.Secondary,
    onSecondaryContainer = AppColors.SecondaryForeground,
    tertiary = AppColors.Accent,
    onTertiary = AppColors.AccentForeground,
    tertiaryContainer = AppColors.Accent,
    onTertiaryContainer = AppColors.AccentForeground,
    error = AppColors.Destructive,
    onError = AppColors.DestructiveForeground,
    errorContainer = AppColors.Destructive,
    onErrorContainer = AppColors.DestructiveForeground,
    background = AppColors.Background,
    onBackground = AppColors.Foreground,
    surface = AppColors.Card,
    onSurface = AppColors.CardForeground,
    surfaceVariant = AppColors.Muted,
    onSurfaceVariant = AppColors.MutedForeground,
    outline = AppColors.Border,
    outlineVariant = AppColors.Input,
    inverseSurface = AppColors.Foreground,
    inverseOnSurface = AppColors.Background,
    inversePrimary = AppColors.PrimaryForeground,
    surfaceTint = AppColors.Primary,
    // Surface containers for dropdown menus, dialogs, etc.
    surfaceContainerLowest = AppColors.Background,
    surfaceContainerLow = AppColors.Background,
    surfaceContainer = AppColors.Card,
    surfaceContainerHigh = AppColors.Muted,
    surfaceContainerHighest = AppColors.Muted
)

private val DarkColorScheme = darkColorScheme(
    primary = AppColors.PrimaryDark,
    onPrimary = AppColors.PrimaryForegroundDark,
    primaryContainer = AppColors.PrimaryDark,
    onPrimaryContainer = AppColors.PrimaryForegroundDark,
    secondary = AppColors.SecondaryDark,
    onSecondary = AppColors.SecondaryForegroundDark,
    secondaryContainer = AppColors.SecondaryDark,
    onSecondaryContainer = AppColors.SecondaryForegroundDark,
    tertiary = AppColors.AccentDark,
    onTertiary = AppColors.AccentForegroundDark,
    tertiaryContainer = AppColors.AccentDark,
    onTertiaryContainer = AppColors.AccentForegroundDark,
    error = AppColors.DestructiveDark,
    onError = AppColors.DestructiveForegroundDark,
    errorContainer = AppColors.DestructiveDark,
    onErrorContainer = AppColors.DestructiveForegroundDark,
    background = AppColors.BackgroundDark,
    onBackground = AppColors.ForegroundDark,
    surface = AppColors.CardDark,
    onSurface = AppColors.CardForegroundDark,
    surfaceVariant = AppColors.MutedDark,
    onSurfaceVariant = AppColors.MutedForegroundDark,
    outline = AppColors.BorderDark,
    outlineVariant = AppColors.InputDark,
    inverseSurface = AppColors.ForegroundDark,
    inverseOnSurface = AppColors.BackgroundDark,
    inversePrimary = AppColors.PrimaryForegroundDark,
    surfaceTint = AppColors.PrimaryDark,
    // Surface containers for dropdown menus, dialogs, etc.
    surfaceContainerLowest = AppColors.BackgroundDark,
    surfaceContainerLow = AppColors.BackgroundDark,
    surfaceContainer = AppColors.CardDark,
    surfaceContainerHigh = AppColors.MutedDark,
    surfaceContainerHighest = AppColors.MutedDark
)

@Composable
fun FastCodeScanTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            WindowCompat.setDecorFitsSystemWindows(window, false)
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.VANILLA_ICE_CREAM) {
                @Suppress("DEPRECATION")
                window.statusBarColor = colorScheme.background.toArgb()
            }
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        shapes = Shapes,
        content = content
    )
}
