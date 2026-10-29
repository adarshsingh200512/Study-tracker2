package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme = darkColorScheme(
    primary = BrandTeal400,
    onPrimary = Navy950,
    primaryContainer = BrandTeal900,
    onPrimaryContainer = BrandTeal100,
    secondary = BrandTeal300,
    onSecondary = Navy950,
    secondaryContainer = Navy800,
    onSecondaryContainer = BrandTeal200,
    tertiary = AccentAmber,
    onTertiary = Navy950,
    background = Navy950,
    onBackground = Color(0xFFF1F5F9),
    surface = DarkSurface,
    onSurface = Color(0xFFF1F5F9),
    surfaceVariant = DarkCardBg,
    onSurfaceVariant = Color(0xFF94A3B8),
    outline = DarkCardBorder,
    error = AccentRed,
    onError = Color.White
)

private val LightColorScheme = lightColorScheme(
    primary = BrandTeal600,
    onPrimary = Color.White,
    primaryContainer = BrandTeal100,
    onPrimaryContainer = BrandTeal900,
    secondary = BrandTeal700,
    onSecondary = Color.White,
    secondaryContainer = BrandTeal50,
    onSecondaryContainer = BrandTeal800,
    tertiary = AccentAmber,
    onTertiary = Color.White,
    background = LightBackground,
    onBackground = Navy900,
    surface = LightSurface,
    onSurface = Navy900,
    surfaceVariant = Color(0xFFF1F5F9),
    onSurfaceVariant = Navy700,
    outline = LightCardBorder,
    error = AccentRed,
    onError = Color.White
)

@Composable
fun StudyTrackerTheme(
    darkTheme: Boolean = true, // Academic dark mode by default for premium focus vibe
    dynamicColor: Boolean = false, // Preserve our distinctive brand colors
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
