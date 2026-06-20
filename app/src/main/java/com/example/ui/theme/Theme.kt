package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = CorporateBlue,
    onPrimary = Color.White,
    primaryContainer = NavyDark,
    onPrimaryContainer = AccentLightBlue,
    secondary = AccentLightBlue,
    onSecondary = Color.Black,
    background = DarkNavy,
    onBackground = DarkText,
    surface = DarkSurface,
    onSurface = DarkText,
    surfaceVariant = Color(0xFF1E293B),
    onSurfaceVariant = DarkTextSecondary
)

private val LightColorScheme = lightColorScheme(
    primary = CorporateBlue,
    onPrimary = Color.White,
    primaryContainer = AccentLightBlue,
    onPrimaryContainer = NavyDark,
    secondary = NeutralGreyText,
    onSecondary = Color.White,
    background = BrightBackground,
    onBackground = CoalText,
    surface = Color.White,
    onSurface = CoalText,
    surfaceVariant = Color(0xFFF3F4F9),
    onSurfaceVariant = NeutralGreyText
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) {
        DarkColorScheme
    } else {
        LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
