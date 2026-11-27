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

private val DarkColorScheme =
  darkColorScheme(
    primary = GoldPrimary,
    onPrimary = Color(0xFF0B0B0B),
    secondary = ShopPrimaryBlue,
    onSecondary = Color.White,
    tertiary = GoldHighlight,
    onTertiary = Color(0xFF0B0B0B),
    background = BlackBackground,
    onBackground = TextMain,
    surface = BlackSurface,
    onSurface = TextMain,
    surfaceVariant = BlackSurfaceSecondary,
    onSurfaceVariant = TextSecondary,
    outline = BlackBorder,
    error = ColorError,
    onError = Color.White,
  )

private val LightColorScheme =
  lightColorScheme(
    primary = ShopPrimaryBlue,
    onPrimary = Color.White,
    secondary = GoldDark,
    onSecondary = Color.White,
    tertiary = ShopAccentAmber,
    onTertiary = Color.White,
    background = LightScreenBg,
    onBackground = LightTextPrimary,
    surface = LightCardBg,
    onSurface = LightTextPrimary,
    surfaceVariant = LightAdminBg,
    onSurfaceVariant = LightTextSecondary,
    outline = LightInputBorder,
    error = ColorError,
    onError = Color.White,
  )

@Composable
fun MyApplicationTheme(
  darkTheme: Boolean = isSystemInDarkTheme(),
  dynamicColor: Boolean = false,
  content: @Composable () -> Unit,
) {
  val colorScheme =
    when {
      dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
        val context = LocalContext.current
        if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
      }

      darkTheme -> DarkColorScheme
      else -> LightColorScheme
    }

  MaterialTheme(colorScheme = colorScheme, typography = Typography, content = content)
}

