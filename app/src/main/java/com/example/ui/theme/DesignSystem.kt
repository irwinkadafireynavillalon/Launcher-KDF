package com.example.ui.theme

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * LAUNCH KDF Adaptive Bimetallic & Cyber-Glass Design System Helpers
 */

@Composable
fun Modifier.skeuomorphicCard(
  elevation: Dp = 8.dp,
  cornerRadius: Dp = 16.dp,
  borderColor: Color = MetalPrimary,
  borderWidth: Dp = 1.2.dp
): Modifier = this
  .shadow(
    elevation = elevation,
    shape = RoundedCornerShape(cornerRadius),
    ambientColor = ShadowSoft,
    spotColor = ShadowSoft
  )
  .background(
    brush = Brush.verticalGradient(
      colors = listOf(
        DarkSurfaceElevated.copy(alpha = UiThemeController.glassOpacity),
        DarkSurface.copy(alpha = UiThemeController.glassOpacity)
      )
    ),
    shape = RoundedCornerShape(cornerRadius)
  )
  .border(
    width = borderWidth,
    brush = Brush.linearGradient(
      colors = listOf(borderColor.copy(alpha = 0.7f), MetalSecondary.copy(alpha = 0.85f), borderColor.copy(alpha = 0.7f))
    ),
    shape = RoundedCornerShape(cornerRadius)
  )
  .padding(borderWidth)
  .drawBehind {
    drawIntoCanvas { canvas ->
      val paint = Paint().asFrameworkPaint().apply {
        color = MetalSecondary.copy(alpha = 0.25f).toArgb()
        maskFilter = android.graphics.BlurMaskFilter(4f, android.graphics.BlurMaskFilter.Blur.NORMAL)
      }
      canvas.nativeCanvas.drawLine(0f, 0f, size.width, 0f, paint)
    }
  }

@Composable
fun Modifier.jewelButton(
  cornerRadius: Dp = 10.dp,
  borderColor: Color = MetalSecondary
): Modifier = this
  .shadow(
    elevation = 4.dp,
    shape = RoundedCornerShape(cornerRadius),
    clip = false
  )
  .background(
    brush = Brush.verticalGradient(
      colors = listOf(
        DarkSurfaceElevated,
        DarkSurface
      )
    ),
    shape = RoundedCornerShape(cornerRadius)
  )
  .border(
    width = 1.2.dp,
    brush = Brush.linearGradient(
      colors = listOf(borderColor, MetalPrimary.copy(alpha = 0.75f), borderColor)
    ),
    shape = RoundedCornerShape(cornerRadius)
  )
  .clip(RoundedCornerShape(cornerRadius))

@Composable
fun Modifier.skeuomorphicIconFrame(
  size: Dp = 48.dp,
  primaryMetal: Color = MetalSecondary,
  secondaryMetal: Color = MetalPrimary
): Modifier = this
  .size(size)
  .shadow(4.dp, CircleShape)
  .background(
    brush = Brush.sweepGradient(
      colors = listOf(primaryMetal, secondaryMetal, primaryMetal)
    ),
    shape = CircleShape
  )
  .padding(2.dp)
  .background(DarkSurfaceElevated, CircleShape)
  .border(1.dp, secondaryMetal.copy(alpha = 0.5f), CircleShape)

fun Modifier.damaskBackground(): Modifier = this.drawBehind {
  drawRect(
    brush = Brush.radialGradient(
      colors = listOf(DarkSurface, SurfacePearl),
      center = center,
      radius = (size.maxDimension * 0.75f).coerceAtLeast(100f)
    )
  )
}
