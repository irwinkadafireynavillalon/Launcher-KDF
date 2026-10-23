package com.example.ui.screens
import androidx.compose.ui.geometry.*
import androidx.compose.ui.draw.*
import androidx.compose.ui.graphics.*
import com.example.ui.theme.*

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.SmartToy
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.audio.TtsManager
import com.example.hardware.InternalAiManager
import kotlinx.coroutines.delay
import kotlin.math.roundToInt

data class MiniChatMessage(
  val sender: String,
  val text: String
)

@Composable
fun FloatingAssistantButtonOverlay(
  ttsManager: TtsManager,
  selectedLanguage: String,
  onSendMessage: (String) -> Unit
) {
  if (!UiThemeController.isFloatingButtonEnabled) return

  var isDockedToEdge by remember { mutableStateOf(false) }
  var isMiniChatOpen by remember { mutableStateOf(false) }
  var lastInteractionTime by remember { mutableStateOf(System.currentTimeMillis()) }

  // 3-second auto-dock timer
  LaunchedEffect(lastInteractionTime, isDockedToEdge, isMiniChatOpen) {
    if (!isDockedToEdge && !isMiniChatOpen) {
      delay(3000)
      isDockedToEdge = true
    }
  }

  BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
    val density = LocalDensity.current
    val maxWidthPx = with(density) { maxWidth.toPx() }
    val maxHeightPx = with(density) { maxHeight.toPx() }

    var offsetX by remember { mutableFloatStateOf(maxWidthPx - with(density) { 60.dp.toPx() }) }
    var offsetY by remember { mutableFloatStateOf(maxHeightPx * 0.45f) }

    val iconVector = when (UiThemeController.floatingButtonIconType) {
      "ROBOT" -> Icons.Default.SmartToy
      "SPARK" -> Icons.Default.AutoAwesome
      "BRAIN" -> Icons.Default.Psychology
      else -> Icons.Default.SmartToy
    }

    if (isDockedToEdge) {
      // Golden line strip attached to the edge
      val isRightEdge = offsetX >= maxWidthPx / 2
      Box(
        modifier = Modifier
          .offset {
            IntOffset(
              x = if (isRightEdge) (maxWidthPx - with(density) { 10.dp.toPx() }).roundToInt() else 0,
              y = offsetY.coerceIn(80f, maxHeightPx - 200f).roundToInt()
            )
          }
          .width(10.dp)
          .height(54.dp)
          .clip(
            if (isRightEdge) RoundedCornerShape(topStart = 8.dp, bottomStart = 8.dp)
            else RoundedCornerShape(topEnd = 8.dp, bottomEnd = 8.dp)
          )
          .background(
            Brush.verticalGradient(
              colors = listOf(MetalSecondary, Color(0xFFFFDF4F), MetalSecondary)
            )
          )
          .border(
            width = 1.dp,
            color = Color.White.copy(alpha = 0.8f),
            shape = if (isRightEdge) RoundedCornerShape(topStart = 8.dp, bottomStart = 8.dp)
            else RoundedCornerShape(topEnd = 8.dp, bottomEnd = 8.dp)
          )
          .clickable {
            isDockedToEdge = false
            lastInteractionTime = System.currentTimeMillis()
          }
          .testTag("floating_docked_golden_line")
      )
    } else {
      // Expanded Floating Action Button
      Box(
        modifier = Modifier
          .offset {
            IntOffset(
              x = offsetX.coerceIn(10f, maxWidthPx - with(density) { 56.dp.toPx() }).roundToInt(),
              y = offsetY.coerceIn(80f, maxHeightPx - 160f).roundToInt()
            )
          }
          .pointerInput(Unit) {
            detectDragGestures(
              onDragStart = {
                lastInteractionTime = System.currentTimeMillis()
              },
              onDragEnd = {
                // Snap towards closest lateral edge
                val isCloserToRight = offsetX > maxWidthPx / 2
                offsetX = if (isCloserToRight) (maxWidthPx - with(density) { 62.dp.toPx() }) else with(density) { 10.dp.toPx() }
                lastInteractionTime = System.currentTimeMillis()
              }
            ) { change, dragAmount ->
              change.consume()
              offsetX += dragAmount.x
              offsetY += dragAmount.y
              lastInteractionTime = System.currentTimeMillis()
            }
          }
          .skeuomorphicIconFrame(
            size = 52.dp,
            primaryMetal = MetalSecondary,
            secondaryMetal = MetalPrimary
          )
          .clickable {
            isMiniChatOpen = true
            lastInteractionTime = System.currentTimeMillis()
          }
          .testTag("floating_assistant_button"),
        contentAlignment = Alignment.Center
      ) {
        Icon(
          imageVector = iconVector,
          contentDescription = "Asistente Flotante",
          tint = Color.Black,
          modifier = Modifier.size(26.dp)
        )
      }
    }
  }

  // Mini Chatbot Dialog Overlay
  if (isMiniChatOpen) {
    MiniChatbotSheet(
      ttsManager = ttsManager,
      selectedLanguage = selectedLanguage,
      onDismiss = {
        isMiniChatOpen = false
        lastInteractionTime = System.currentTimeMillis()
      },
      onForwardToMain = { text ->
        isMiniChatOpen = false
        onSendMessage(text)
      }
    )
  }
}

@Composable
private fun MiniChatbotSheet(
  ttsManager: TtsManager,
  selectedLanguage: String,
  onDismiss: () -> Unit,
  onForwardToMain: (String) -> Unit
) {
  var miniInput by remember { mutableStateOf("") }
  val messages = remember {
    mutableStateListOf(
      MiniChatMessage("bot", "¡Hola! Soy tu asistente flotante Hyper Lux. ¿En qué te puedo ayudar rápidamente?")
    )
  }
  val listState = rememberLazyListState()

  Dialog(onDismissRequest = onDismiss) {
    Card(
      modifier = Modifier
        .fillMaxWidth()
        .heightIn(max = 500.dp)
        .skeuomorphicCard(borderColor = MetalSecondary),
      colors = CardDefaults.cardColors(containerColor = Color.Transparent)
    ) {
      Column(
        modifier = Modifier
          .fillMaxWidth()
          .padding(12.dp)
      ) {
        // Header
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
              modifier = Modifier
                .size(28.dp)
                .background(MetalSecondary, CircleShape),
              contentAlignment = Alignment.Center
            ) {
              Icon(Icons.Default.SmartToy, contentDescription = null, tint = Color.Black, modifier = Modifier.size(16.dp))
            }
            Spacer(modifier = Modifier.width(8.dp))
            Column {
              Text("ASISTENTE FLOTANTE", color = MetalSecondary, fontWeight = FontWeight.Bold, fontSize = 12.sp)
              Text("Mini Chatbot • Modo Rápido", color = MetalPrimary, fontSize = 9.sp, fontFamily = FontFamily.Monospace)
            }
          }

          IconButton(onClick = onDismiss, modifier = Modifier.size(26.dp)) {
            Icon(Icons.Default.Close, contentDescription = "Cerrar", tint = TextSub, modifier = Modifier.size(18.dp))
          }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Message stream
        LazyColumn(
          state = listState,
          modifier = Modifier
            .weight(1f)
            .fillMaxWidth(),
          verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
          items(messages) { msg ->
            val isUser = msg.sender == "user"
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start
            ) {
              Card(
                modifier = Modifier.fillMaxWidth(0.85f),
                shape = RoundedCornerShape(10.dp),
                colors = CardDefaults.cardColors(
                  containerColor = if (isUser) MetalSecondary.copy(alpha = 0.22f) else Color.White
                )
              ) {
                Column(modifier = Modifier.padding(8.dp)) {
                  Text(
                    text = if (isUser) "TÚ" else "HYPER LUX BOT",
                    color = if (isUser) MetalSecondary else MetalPrimary,
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Bold
                  )
                  Spacer(modifier = Modifier.height(2.dp))
                  Text(text = msg.text, color = TextMain, fontSize = 11.sp)
                }
              }
            }
          }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Input row
        Row(
          modifier = Modifier.fillMaxWidth(),
          verticalAlignment = Alignment.CenterVertically
        ) {
          OutlinedTextField(
            value = miniInput,
            onValueChange = { miniInput = it },
            placeholder = { Text("Consulta rápida...", fontSize = 11.sp, color = TextSub) },
            modifier = Modifier.weight(1f),
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
              focusedBorderColor = MetalSecondary,
              unfocusedBorderColor = MetalPrimary,
              focusedContainerColor = SurfacePearl,
              unfocusedContainerColor = SurfacePearl,
              focusedTextColor = TextMain,
              unfocusedTextColor = TextMain
            ),
            singleLine = true
          )

          Spacer(modifier = Modifier.width(6.dp))

          IconButton(
            onClick = {
              if (miniInput.isNotBlank()) {
                val userText = miniInput.trim()
                miniInput = ""
                messages.add(MiniChatMessage("user", userText))
                val reply = InternalAiManager.generateOfflineResponse(userText, selectedLanguage)
                messages.add(MiniChatMessage("bot", reply))
                ttsManager.speak(reply, selectedLanguage)
              }
            },
            modifier = Modifier
              .size(38.dp)
              .background(MetalSecondary, CircleShape)
          ) {
            Icon(Icons.Default.Send, contentDescription = "Enviar", tint = Color.Black, modifier = Modifier.size(16.dp))
          }
        }
      }
    }
  }
}
