package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = NeonViolet,
    onPrimary = Color.White,
    primaryContainer = CosmicSurfaceVariant,
    onPrimaryContainer = Color.White,
    secondary = CyberMagenta,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFF38153D),
    onSecondaryContainer = GlowPink,
    tertiary = CyberCyan,
    onTertiary = Color.Black,
    background = CosmicBackground,
    onBackground = TextPrimary,
    surface = CosmicSurface,
    onSurface = TextPrimary,
    surfaceVariant = CosmicSurfaceVariant,
    onSurfaceVariant = TextSecondary,
    outline = GlassBorder,
    outlineVariant = Color(0x22FFFFFF)
)

@Composable
fun MyApplicationTheme(
    accentColor: Color = NeonViolet,
    content: @Composable () -> Unit
) {
    val dynamicScheme = DarkColorScheme.copy(
        primary = accentColor,
        secondary = if (accentColor == NeonViolet) CyberMagenta else CyberCyan
    )

    MaterialTheme(
        colorScheme = dynamicScheme,
        typography = Typography,
        content = content
    )
}
