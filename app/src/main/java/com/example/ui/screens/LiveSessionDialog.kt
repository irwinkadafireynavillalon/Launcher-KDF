package com.example.ui.screens
import androidx.compose.ui.geometry.*
import androidx.compose.ui.draw.*
import androidx.compose.ui.graphics.*
import com.example.ui.theme.*

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CallEnd
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MicOff
import androidx.compose.material.icons.filled.RadioButtonChecked
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.audio.SpeechRecognitionHelper
import com.example.audio.TtsManager
import kotlinx.coroutines.delay

@Composable
fun LiveSessionDialog(
  isOpen: Boolean,
  selectedLanguage: String,
  ttsManager: TtsManager,
  speechHelper: SpeechRecognitionHelper,
  onDismiss: () -> Unit
) {
  if (!isOpen) return

  var isMuted by remember { mutableStateOf(false) }
  var isVideoActive by remember { mutableStateOf(true) }
  var liveTranscript by remember { mutableStateOf("Escuchando tu voz en tiempo real...") }
  var liveAiResponse by remember { mutableStateOf("Hyper Lux Live activo. Habla naturalmente en cualquier momento.") }

  val infiniteTransition = rememberInfiniteTransition(label = "pulse")
  val pulseScale by infiniteTransition.animateFloat(
    initialValue = 0.90f,
    targetValue = 1.15f,
    animationSpec = infiniteRepeatable(
      animation = tween(800, easing = FastOutSlowInEasing),
      repeatMode = RepeatMode.Reverse
    ),
    label = "live_pulse"
  )

  LaunchedEffect(Unit) {
    ttsManager.speak("Sesión en vivo iniciada. Estoy escuchando.", selectedLanguage)
  }

  Dialog(
    onDismissRequest = onDismiss,
    properties = DialogProperties(usePlatformDefaultWidth = false)
  ) {
    Box(
      modifier = Modifier
        .fillMaxSize()
        .background(Color(0xF007090F))
        .padding(20.dp)
    ) {
      Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
      ) {
        // Top row
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          // LIVE indicator badge
          Row(
            modifier = Modifier
              .clip(RoundedCornerShape(20.dp))
              .background(DangerRed.copy(alpha = 0.2f))
              .border(1.dp, DangerRed, RoundedCornerShape(20.dp))
              .padding(horizontal = 12.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
          ) {
            Box(
              modifier = Modifier
                .size(10.dp)
                .scale(pulseScale)
                .background(DangerRed, CircleShape)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text("LIVE EN VIVO", color = DangerRed, fontSize = 11.sp, fontWeight = FontWeight.ExtraBold)
          }

          Text(
            text = "$selectedLanguage • Latencia Ultra-Baja",
            color = MetalPrimary,
            fontSize = 11.sp,
            fontFamily = FontFamily.Monospace
          )
        }

        // Central live interactive audio wave & camera simulation
        Column(
          horizontalAlignment = Alignment.CenterHorizontally,
          verticalArrangement = Arrangement.Center,
          modifier = Modifier.weight(1f)
        ) {
          Box(
            modifier = Modifier
              .size(180.dp)
              .clip(CircleShape)
              .background(
                Brush.radialGradient(
                  colors = listOf(
                    MetalPrimary.copy(alpha = 0.25f),
                    MetalSecondary.copy(alpha = 0.15f),
                    Color.Transparent
                  )
                )
              )
              .border(2.dp, MetalSecondary, CircleShape),
            contentAlignment = Alignment.Center
          ) {
            Icon(
              imageVector = Icons.Default.GraphicEq,
              contentDescription = null,
              tint = MetalSecondary,
              modifier = Modifier.size(72.dp).scale(pulseScale)
            )
          }

          Spacer(modifier = Modifier.height(24.dp))

          // Live transcript bubble
          Card(
            modifier = Modifier.fillMaxWidth(0.9f),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White)
          ) {
            Column(modifier = Modifier.padding(14.dp)) {
              Text("TRANSCRIPCIÓN EN VIVO:", color = MetalPrimary, fontSize = 9.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
              Spacer(modifier = Modifier.height(4.dp))
              Text(liveTranscript, color = TextMain, fontSize = 12.sp)
              Spacer(modifier = Modifier.height(8.dp))
              Text("RESPUESTA IA CONTINUA:", color = MetalSecondary, fontSize = 9.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
              Spacer(modifier = Modifier.height(4.dp))
              Text(liveAiResponse, color = TextSub, fontSize = 12.sp)
            }
          }
        }

        // Bottom Controls: Mic Mute, Camera Toggle, End Call
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 20.dp),
          horizontalArrangement = Arrangement.Center,
          verticalAlignment = Alignment.CenterVertically
        ) {
          // Mic toggle
          IconButton(
            onClick = { isMuted = !isMuted },
            modifier = Modifier
              .size(52.dp)
              .background(if (isMuted) DangerRed else Color.White, CircleShape)
              .border(1.dp, MetalSecondary, CircleShape)
          ) {
            Icon(
              imageVector = if (isMuted) Icons.Default.MicOff else Icons.Default.Mic,
              contentDescription = "Mute",
              tint = Color.White,
              modifier = Modifier.size(24.dp)
            )
          }

          Spacer(modifier = Modifier.width(20.dp))

          // End Live Session Button
          Button(
            onClick = onDismiss,
            shape = CircleShape,
            colors = ButtonDefaults.buttonColors(containerColor = DangerRed, contentColor = Color.White),
            modifier = Modifier.size(64.dp)
          ) {
            Icon(Icons.Default.CallEnd, contentDescription = "Finalizar Live", modifier = Modifier.size(28.dp))
          }

          Spacer(modifier = Modifier.width(20.dp))

          // Video toggle
          IconButton(
            onClick = { isVideoActive = !isVideoActive },
            modifier = Modifier
              .size(52.dp)
              .background(if (isVideoActive) MetalPrimary.copy(alpha = 0.2f) else Color.White, CircleShape)
              .border(1.dp, MetalPrimary, CircleShape)
          ) {
            Icon(
              imageVector = Icons.Default.Videocam,
              contentDescription = "Cámara",
              tint = MetalPrimary,
              modifier = Modifier.size(24.dp)
            )
          }
        }
      }
    }
  }
}
