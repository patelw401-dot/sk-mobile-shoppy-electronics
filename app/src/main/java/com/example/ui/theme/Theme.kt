package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

// SK Mobile Shoppy and Electronic - Premium Black + Gold Color Scheme
private val BlackGoldColorScheme = darkColorScheme(
    primary = GoldPrimary,                // #D4AF37
    onPrimary = Color(0xFF0B0B0B),        // Dark contrast on gold
    primaryContainer = BlackSurfaceSecondary, // #222222
    onPrimaryContainer = GoldHighlight,   // #F5D76E
    secondary = GoldHighlight,            // #F5D76E
    onSecondary = Color(0xFF0B0B0B),
    secondaryContainer = BlackSurfaceSecondary,
    onSecondaryContainer = GoldHighlight,
    tertiary = GoldPrimary,
    onTertiary = Color(0xFF0B0B0B),
    background = BlackBackground,         // #0B0B0B
    onBackground = TextMain,              // #FFFFFF
    surface = BlackSurface,               // #171717
    onSurface = TextMain,                 // #FFFFFF
    surfaceVariant = BlackSurfaceSecondary, // #222222
    onSurfaceVariant = TextSecondary,     // #C5C5C5
    outline = BlackBorder,                // #383838
    outlineVariant = Color(0xFF282828),
    error = ColorError,                   // #CF6679
    onError = Color.White
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = true, // Enforce Premium Black + Gold Theme
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = BlackGoldColorScheme,
        typography = Typography,
        content = content
    )
}
