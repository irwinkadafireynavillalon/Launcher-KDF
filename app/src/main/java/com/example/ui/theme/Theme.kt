package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Density

@Composable
fun MyApplicationTheme(
  darkTheme: Boolean = false, // We prioritize the white/pearl theme as requested
  dynamicColor: Boolean = false,
  content: @Composable () -> Unit
) {
  val currentDensity = LocalDensity.current
  val customDensity = Density(
    density = currentDensity.density,
    fontScale = currentDensity.fontScale * UiThemeController.uiScaleMultiplier
  )

  val isDarkPreset = SurfacePearl.red + SurfacePearl.green + SurfacePearl.blue < 1.0f
  
  val luxuryColorScheme = if (isDarkPreset) {
    darkColorScheme(
      primary = MetalSecondary, // Gold
      onPrimary = Color.Black,
      primaryContainer = Color(0xFF1C1C1E),
      onPrimaryContainer = MetalSecondary,
      secondary = MetalPrimary, // Chrome
      onSecondary = Color.White,
      secondaryContainer = SurfacePearl,
      onSecondaryContainer = MetalPrimary,
      tertiary = MetalSecondary,
      onTertiary = Color.Black,
      tertiaryContainer = SurfacePearl,
      onTertiaryContainer = MetalSecondary,
      error = DangerRed,
      onError = Color.White,
      background = SurfacePearl,
      onBackground = Color.White,
      surface = Color(0xFF121212),
      onSurface = Color.White,
      surfaceVariant = Color(0xFF1C1C1E),
      onSurfaceVariant = Color(0xFFBCCCDC),
      outline = MetalPrimary
    )
  } else {
    lightColorScheme(
      primary = MetalSecondary, // Gold
      onPrimary = Color.White,
      primaryContainer = Color.White,
      onPrimaryContainer = MetalSecondary,
      secondary = MetalPrimary, // Chrome
      onSecondary = GraphiteCharcoal,
      secondaryContainer = SurfacePearl,
      onSecondaryContainer = MetalPrimary,
      tertiary = MetalSecondary,
      onTertiary = Color.White,
      tertiaryContainer = SurfacePearl,
      onTertiaryContainer = MetalSecondary,
      error = DangerRed,
      onError = Color.White,
      background = SurfacePearl,
      onBackground = GraphiteCharcoal,
      surface = Color.White,
      onSurface = GraphiteCharcoal,
      surfaceVariant = SurfacePearl,
      onSurfaceVariant = PlumbousGray,
      outline = MetalPrimary
    )
  }

  CompositionLocalProvider(LocalDensity provides customDensity) {
    MaterialTheme(
      colorScheme = luxuryColorScheme,
      typography = Typography,
      content = content
    )
  }
}
