package com.example.ui.screens

import android.Manifest
import android.content.Context
import android.os.Build
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.audio.KdfAudioToneManager
import com.example.audio.KdfTone
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.DarkSurfaceElevated
import com.example.ui.theme.MetalPrimary
import com.example.ui.theme.MetalSecondary
import com.example.ui.theme.TextMain
import com.example.ui.theme.TextSub
import com.example.ui.theme.UiThemeController

@Composable
fun NotificationTonesDialog(
  isOpen: Boolean,
  onDismiss: () -> Unit
) {
  if (!isOpen) return

  val context = LocalContext.current
  val scope = rememberCoroutineScope()
  val isPlaying by KdfAudioToneManager.isPlaying.collectAsStateWithLifecycle()
  val playingToneId by KdfAudioToneManager.playingToneId.collectAsStateWithLifecycle()
  val waveformSamples by KdfAudioToneManager.waveformSamples.collectAsStateWithLifecycle()

  // Permission launcher for real Android system notifications
  val notificationPermissionLauncher = rememberLauncherForActivityResult(
    contract = ActivityResultContracts.RequestPermission()
  ) { isGranted ->
    if (isGranted) {
      val activeTone = KdfAudioToneManager.TONES.find { it.id == UiThemeController.active4kNotificationToneId }
        ?: KdfAudioToneManager.TONES.first()
      KdfAudioToneManager.sendTestNotification(context, activeTone, scope)
      Toast.makeText(context, "🔔 Alerta 4K enviada al sistema Android", Toast.LENGTH_SHORT).show()
    } else {
      Toast.makeText(context, "Permiso de notificaciones necesario para emitir alertas", Toast.LENGTH_LONG).show()
    }
  }

  DisposableEffect(Unit) {
    onDispose {
      KdfAudioToneManager.stopPlayback()
    }
  }

  Dialog(
    onDismissRequest = {
      KdfAudioToneManager.stopPlayback()
      onDismiss()
    },
    properties = DialogProperties(usePlatformDefaultWidth = false)
  ) {
    Box(
      modifier = Modifier
        .padding(horizontal = 14.dp, vertical = 24.dp)
        .fillMaxWidth()
        .clip(RoundedCornerShape(18.dp))
        .background(DarkSurfaceElevated.copy(alpha = 0.98f))
        .border(1.2.dp, Brush.linearGradient(listOf(MetalSecondary, MetalPrimary, MetalSecondary)), RoundedCornerShape(18.dp))
        .padding(14.dp)
        .testTag("notification_tones_dialog")
    ) {
      Column(modifier = Modifier.fillMaxWidth()) {
        // Compact Header
        Row(
          modifier = Modifier.fillMaxWidth(),
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.SpaceBetween
        ) {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
              modifier = Modifier
                .size(34.dp)
                .clip(CircleShape)
                .background(MetalSecondary.copy(alpha = 0.18f))
                .border(1.dp, MetalSecondary, CircleShape),
              contentAlignment = Alignment.Center
            ) {
              Icon(
                imageVector = Icons.Default.GraphicEq,
                contentDescription = null,
                tint = MetalSecondary,
                modifier = Modifier.size(18.dp)
              )
            }
            Spacer(modifier = Modifier.width(8.dp))
            Column {
              Text(
                text = "TONOS NOTIFICACIONES 4K",
                color = MetalSecondary,
                fontSize = 13.sp,
                fontWeight = FontWeight.ExtraBold,
                letterSpacing = 0.5.sp
              )
              Text(
                text = "Audio Studio 48,000 Hz Hi-Res • 10 Tonos Nativos",
                color = TextSub,
                fontSize = 9.sp
              )
            }
          }

          // Close button
          Box(
            modifier = Modifier
              .clip(RoundedCornerShape(6.dp))
              .background(DarkSurface)
              .border(0.8.dp, MetalPrimary.copy(alpha = 0.4f), RoundedCornerShape(6.dp))
              .clickable {
                KdfAudioToneManager.stopPlayback()
                onDismiss()
              }
              .padding(horizontal = 8.dp, vertical = 4.dp)
          ) {
            Text("Cerrar", color = TextMain, fontSize = 10.sp, fontWeight = FontWeight.Bold)
          }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Real-Time Waveform Visualizer Banner
        AnimatedWaveformCanvas(
          isPlaying = isPlaying,
          waveformSamples = waveformSamples
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Quick System Notification Test Bar
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(DarkSurface)
            .border(0.8.dp, MetalSecondary.copy(alpha = 0.35f), RoundedCornerShape(8.dp))
            .padding(horizontal = 10.dp, vertical = 6.dp),
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.SpaceBetween
        ) {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
              imageVector = Icons.Default.NotificationsActive,
              contentDescription = null,
              tint = MetalSecondary,
              modifier = Modifier.size(15.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            val currentActiveTone = KdfAudioToneManager.TONES.find { it.id == UiThemeController.active4kNotificationToneId }
            Text(
              text = "Activo: ${currentActiveTone?.name ?: "KDF Royal Gold 4K"}",
              color = TextMain,
              fontSize = 10.sp,
              fontWeight = FontWeight.SemiBold
            )
          }

          Box(
            modifier = Modifier
              .clip(RoundedCornerShape(6.dp))
              .background(MetalSecondary)
              .clickable {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                  notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                } else {
                  val activeTone = KdfAudioToneManager.TONES.find { it.id == UiThemeController.active4kNotificationToneId }
                    ?: KdfAudioToneManager.TONES.first()
                  KdfAudioToneManager.sendTestNotification(context, activeTone, scope)
                  Toast.makeText(context, "🔔 Alerta 4K enviada al sistema Android", Toast.LENGTH_SHORT).show()
                }
              }
              .padding(horizontal = 8.dp, vertical = 4.dp)
              .testTag("test_system_notification_button")
          ) {
            Text("Probar Alerta Real", color = Color.Black, fontSize = 9.sp, fontWeight = FontWeight.ExtraBold)
          }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // List of 4K Tones
        LazyColumn(
          modifier = Modifier
            .fillMaxWidth()
            .weight(1f, fill = false),
          verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
          items(KdfAudioToneManager.TONES, key = { it.id }) { tone ->
            ToneItemRow(
              tone = tone,
              isActive = tone.id == UiThemeController.active4kNotificationToneId,
              isPlaying = isPlaying && playingToneId == tone.id,
              onPlayToggle = {
                if (isPlaying && playingToneId == tone.id) {
                  KdfAudioToneManager.stopPlayback()
                } else {
                  KdfAudioToneManager.previewTone(context, tone, scope)
                }
              },
              onSelectActive = {
                UiThemeController.updateActive4kTone(context, tone.id)
                KdfAudioToneManager.setActiveTone(tone.id)
                Toast.makeText(context, "Tono '${tone.name}' asignado como alerta activa", Toast.LENGTH_SHORT).show()
              }
            )
          }
        }
      }
    }
  }
}

@Composable
fun AnimatedWaveformCanvas(
  isPlaying: Boolean,
  waveformSamples: List<Float>
) {
  val infiniteTransition = rememberInfiniteTransition(label = "wave_anim")
  val phase by infiniteTransition.animateFloat(
    initialValue = 0f,
    targetValue = 6.28f,
    animationSpec = infiniteRepeatable(
      animation = tween(1200, easing = LinearEasing),
      repeatMode = RepeatMode.Restart
    ),
    label = "phase"
  )

  Box(
    modifier = Modifier
      .fillMaxWidth()
      .height(52.dp)
      .clip(RoundedCornerShape(8.dp))
      .background(Color(0xFF0D0618))
      .border(0.8.dp, MetalPrimary.copy(alpha = 0.35f), RoundedCornerShape(8.dp))
  ) {
    Canvas(modifier = Modifier.matchParentSize()) {
      val barCount = 36
      val barWidth = size.width / (barCount * 1.4f)
      val barSpacing = barWidth * 0.4f

      for (i in 0 until barCount) {
        val sampleVal = if (waveformSamples.isNotEmpty() && i < waveformSamples.size) {
          waveformSamples[i]
        } else {
          0.12f
        }

        val dynamicHeight = if (isPlaying) {
          val waveMod = (kotlin.math.sin(phase + (i * 0.3f)) * 0.35f + 0.65f).toFloat()
          (sampleVal.coerceAtLeast(0.15f) * waveMod * (size.height * 0.85f)).coerceIn(4f, size.height * 0.88f)
        } else {
          (kotlin.math.sin(i * 0.4f) * 4f + 8f).coerceAtLeast(4f)
        }

        val x = i * (barWidth + barSpacing) + (barSpacing / 2f)
        val y = (size.height - dynamicHeight) / 2f

        val barColor = if (isPlaying) {
          if (i % 3 == 0) MetalSecondary else MetalPrimary
        } else {
          MetalPrimary.copy(alpha = 0.45f)
        }

        drawRoundRect(
          color = barColor,
          topLeft = Offset(x, y),
          size = Size(barWidth, dynamicHeight),
          cornerRadius = CornerRadius(2.dp.toPx(), 2.dp.toPx())
        )
      }
    }

    // Overlay Badge
    Row(
      modifier = Modifier
        .align(Alignment.TopEnd)
        .padding(4.dp)
        .background(Color.Black.copy(alpha = 0.65f), RoundedCornerShape(4.dp))
        .padding(horizontal = 5.dp, vertical = 2.dp),
      verticalAlignment = Alignment.CenterVertically
    ) {
      Text(
        text = if (isPlaying) "REPRODUCIENDO 4K • 48 kHz" else "AUDIO 4K STUDIO READY",
        color = if (isPlaying) MetalSecondary else TextSub,
        fontSize = 8.sp,
        fontWeight = FontWeight.Bold
      )
    }
  }
}

@Composable
fun ToneItemRow(
  tone: KdfTone,
  isActive: Boolean,
  isPlaying: Boolean,
  onPlayToggle: () -> Unit,
  onSelectActive: () -> Unit
) {
  Card(
    modifier = Modifier
      .fillMaxWidth()
      .testTag("tone_item_${tone.id}"),
    shape = RoundedCornerShape(10.dp),
    colors = CardDefaults.cardColors(
      containerColor = if (isActive) DarkSurface.copy(alpha = 0.95f) else Color(0xFF140B22)
    ),
    border = androidx.compose.foundation.BorderStroke(
      width = if (isActive) 1.2.dp else 0.6.dp,
      color = if (isActive) MetalSecondary else MetalPrimary.copy(alpha = 0.25f)
    )
  ) {
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 8.dp, vertical = 6.dp),
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.SpaceBetween
    ) {
      // Play/Stop Action Button
      IconButton(
        onClick = onPlayToggle,
        modifier = Modifier
          .size(32.dp)
          .clip(CircleShape)
          .background(if (isPlaying) MetalSecondary else DarkSurface)
          .border(1.dp, MetalSecondary, CircleShape)
      ) {
        Icon(
          imageVector = if (isPlaying) Icons.Default.Stop else Icons.Default.PlayArrow,
          contentDescription = if (isPlaying) "Detener" else "Reproducir",
          tint = if (isPlaying) Color.Black else MetalSecondary,
          modifier = Modifier.size(16.dp)
        )
      }

      Spacer(modifier = Modifier.width(8.dp))

      // Tone Metadata
      Column(modifier = Modifier.weight(1f)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
          Text(
            text = "${tone.iconEmoji} ${tone.name}",
            color = TextMain,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold
          )
          Spacer(modifier = Modifier.width(4.dp))
          Box(
            modifier = Modifier
              .background(MetalPrimary.copy(alpha = 0.15f), RoundedCornerShape(3.dp))
              .padding(horizontal = 4.dp, vertical = 1.dp)
          ) {
            Text(
              text = tone.category,
              color = MetalPrimary,
              fontSize = 7.5.sp,
              fontWeight = FontWeight.Bold
            )
          }
        }
        Text(
          text = tone.description,
          color = TextSub,
          fontSize = 8.5.sp,
          maxLines = 1
        )
        Text(
          text = "${tone.sampleRate} Hz • ${tone.bitDepth} • ${tone.durationMs}ms",
          color = MetalSecondary.copy(alpha = 0.85f),
          fontSize = 7.5.sp,
          fontWeight = FontWeight.SemiBold
        )
      }

      Spacer(modifier = Modifier.width(6.dp))

      // Set as Active Tone Action
      Box(
        modifier = Modifier
          .clip(RoundedCornerShape(6.dp))
          .background(if (isActive) MetalSecondary.copy(alpha = 0.2f) else DarkSurface)
          .border(
            width = 0.8.dp,
            color = if (isActive) MetalSecondary else MetalPrimary.copy(alpha = 0.35f),
            shape = RoundedCornerShape(6.dp)
          )
          .clickable { onSelectActive() }
          .padding(horizontal = 6.dp, vertical = 4.dp)
      ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
          if (isActive) {
            Icon(
              imageVector = Icons.Default.Check,
              contentDescription = null,
              tint = MetalSecondary,
              modifier = Modifier.size(10.dp)
            )
            Spacer(modifier = Modifier.width(2.dp))
          }
          Text(
            text = if (isActive) "Activo" else "Usar",
            color = if (isActive) MetalSecondary else TextSub,
            fontSize = 9.sp,
            fontWeight = FontWeight.Bold
          )
        }
      }
    }
  }
}
