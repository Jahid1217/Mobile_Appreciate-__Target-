package com.example.shops.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = SkyBlue,
    onPrimary = DarkBackground,
    primaryContainer = Color(0xFF13335F),
    onPrimaryContainer = DarkText,
    secondary = MintGreen,
    onSecondary = DarkBackground,
    secondaryContainer = Color(0xFF14352E),
    onSecondaryContainer = DarkText,
    tertiary = SoftGold,
    onTertiary = DarkBackground,
    tertiaryContainer = Color(0xFF49320A),
    onTertiaryContainer = DarkText,
    error = CoralRed,
    background = DarkBackground,
    onBackground = DarkText,
    surface = DarkSurface,
    onSurface = DarkText,
    surfaceVariant = DarkSurfaceAlt,
    onSurfaceVariant = DarkSubtleText,
    outline = Color(0xFF41556F),
    outlineVariant = Color(0xFF243246)
)

private val LightColorScheme = lightColorScheme(
    primary = OceanBlue,
    onPrimary = LightSurface,
    primaryContainer = Color(0xFFDCEAFE),
    onPrimaryContainer = LightText,
    secondary = MintGreen,
    onSecondary = LightSurface,
    secondaryContainer = Color(0xFFDDF7ED),
    onSecondaryContainer = LightText,
    tertiary = SoftGold,
    onTertiary = LightText,
    tertiaryContainer = Color(0xFFFFE8BF),
    onTertiaryContainer = LightText,
    error = CoralRed,
    background = LightBackground,
    onBackground = LightText,
    surface = LightSurface,
    onSurface = LightText,
    surfaceVariant = LightSurfaceAlt,
    onSurfaceVariant = LightSubtleText,
    outline = Color(0xFF8695A7),
    outlineVariant = Color(0xFFD0DAE7)
)

@Composable
fun ShopsTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Dynamic color is available on Android 12+
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }

        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
