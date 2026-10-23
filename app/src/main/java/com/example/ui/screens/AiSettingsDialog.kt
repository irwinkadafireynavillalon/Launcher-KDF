package com.example.ui.screens
import androidx.compose.ui.geometry.*
import androidx.compose.ui.draw.*
import androidx.compose.ui.graphics.*
import com.example.ui.theme.*

import android.content.Context
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
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.CloudSync
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.RecordVoiceOver
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.audio.TtsManager
import com.example.hardware.InternalAiManager

@Composable
fun AiSettingsDialog(
  isOpen: Boolean,
  ttsManager: TtsManager,
  selectedLanguage: String,
  onDismiss: () -> Unit
) {
  if (!isOpen) return

  val context = LocalContext.current
  val speechRate by ttsManager.speechRate.collectAsState()
  val autoSpeak by ttsManager.autoSpeakReplies.collectAsState()

  var temperature by remember { mutableFloatStateOf(0.7f) }
  var topP by remember { mutableFloatStateOf(0.95f) }
  var topK by remember { mutableFloatStateOf(40f) }
  var autoLearningEnabled by remember { mutableStateOf(true) }
  var driveSyncEnabled by remember { mutableStateOf(true) }
  var megaTeraboxSyncEnabled by remember { mutableStateOf(true) }
  var customSystemDirective by remember {
    mutableStateOf("Asistente soberano Hyper Lux con respuesta analítica, síntesis empática y resguardo absoluto de privacidad.")
  }

  Dialog(onDismissRequest = onDismiss) {
    Card(
      modifier = Modifier
        .fillMaxWidth()
        .heightIn(max = 660.dp)
        .border(1.2.dp, MetalSecondary.copy(alpha = 0.85f), RoundedCornerShape(16.dp)),
      shape = RoundedCornerShape(16.dp),
      colors = CardDefaults.cardColors(containerColor = SurfacePearl)
    ) {
      Column(
        modifier = Modifier
          .fillMaxWidth()
          .padding(14.dp)
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
                .size(32.dp)
                .background(MetalSecondary.copy(alpha = 0.2f), CircleShape),
              contentAlignment = Alignment.Center
            ) {
              Icon(Icons.Default.Tune, contentDescription = null, tint = MetalSecondary, modifier = Modifier.size(18.dp))
            }
            Spacer(modifier = Modifier.width(8.dp))
            Column {
              Text("AJUSTES DEL NÚCLEO IA", color = TextMain, fontSize = 13.sp, fontWeight = FontWeight.ExtraBold)
              Text("Voz • Parámetros • Autoaprendizaje & Servidores", color = MetalPrimary, fontSize = 9.sp, fontFamily = FontFamily.Monospace)
            }
          }

          IconButton(onClick = onDismiss, modifier = Modifier.size(28.dp)) {
            Icon(Icons.Default.Close, contentDescription = "Cerrar", tint = TextSub, modifier = Modifier.size(18.dp))
          }
        }

        Spacer(modifier = Modifier.height(10.dp))

        Column(
          modifier = Modifier
            .weight(1f)
            .verticalScroll(rememberScrollState()),
          verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
          // 1. SELECCIÓN DE VOZ & PARÁMETROS TTS
          Text("1. CONFIGURACIÓN DE VOZ (TTS)", color = MetalSecondary, fontSize = 11.sp, fontWeight = FontWeight.Bold)

          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Text("Auto-reproducir respuestas por voz:", color = TextSub, fontSize = 11.sp)
            Switch(
              checked = autoSpeak,
              onCheckedChange = { ttsManager.setAutoSpeak(it) },
              colors = SwitchDefaults.colors(checkedThumbColor = MetalSecondary)
            )
          }

          Column {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
              Text("Velocidad de Habla / Voz:", color = TextSub, fontSize = 10.sp)
              Text("${String.format("%.2f", speechRate)}x", color = MetalSecondary, fontSize = 10.sp, fontFamily = FontFamily.Monospace)
            }
            Slider(
              value = speechRate,
              onValueChange = { ttsManager.setSpeechRate(it) },
              valueRange = 0.7f..1.5f,
              colors = SliderDefaults.colors(thumbColor = MetalSecondary, activeTrackColor = MetalSecondary)
            )
          }

          Button(
            onClick = { ttsManager.speak("Prueba de sintetizador de voz Hyper Lux en $selectedLanguage", selectedLanguage) },
            shape = RoundedCornerShape(8.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color.White, contentColor = MetalSecondary),
            modifier = Modifier.fillMaxWidth()
          ) {
            Icon(Icons.Default.RecordVoiceOver, contentDescription = null, modifier = Modifier.size(14.dp))
            Spacer(modifier = Modifier.width(6.dp))
            Text("Probar Voz Actual", fontSize = 11.sp, fontWeight = FontWeight.Bold)
          }

          HorizontalDivider(color = MetalPrimary, thickness = 0.6.dp)

          // 2. PARÁMETROS IA INTERNA
          Text("2. PARÁMETROS DE INFERENCIA IA", color = MetalPrimary, fontSize = 11.sp, fontWeight = FontWeight.Bold)

          Column {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
              Text("Temperature (Creatividad vs Precisión):", color = TextSub, fontSize = 10.sp)
              Text("${String.format("%.2f", temperature)}", color = MetalPrimary, fontSize = 10.sp, fontFamily = FontFamily.Monospace)
            }
            Slider(
              value = temperature,
              onValueChange = { temperature = it },
              valueRange = 0.1f..1.5f,
              colors = SliderDefaults.colors(thumbColor = MetalPrimary, activeTrackColor = MetalPrimary)
            )
          }

          Column {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
              Text("Top-P (Nucleus Sampling):", color = TextSub, fontSize = 10.sp)
              Text("${String.format("%.2f", topP)}", color = MetalPrimary, fontSize = 10.sp, fontFamily = FontFamily.Monospace)
            }
            Slider(
              value = topP,
              onValueChange = { topP = it },
              valueRange = 0.5f..1.0f,
              colors = SliderDefaults.colors(thumbColor = MetalPrimary, activeTrackColor = MetalPrimary)
            )
          }

          Text("Directriz / Instrucción del Sistema Personalizada:", color = TextSub, fontSize = 10.sp)
          OutlinedTextField(
            value = customSystemDirective,
            onValueChange = { customSystemDirective = it },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(8.dp),
            colors = OutlinedTextFieldDefaults.colors(
              focusedBorderColor = MetalPrimary,
              unfocusedBorderColor = MetalPrimary,
              focusedContainerColor = SurfacePearl,
              unfocusedContainerColor = SurfacePearl,
              focusedTextColor = TextMain,
              unfocusedTextColor = TextMain
            ),
            maxLines = 3
          )

          HorizontalDivider(color = MetalPrimary, thickness = 0.6.dp)

          // 3. AUTOAPRENDIZAJE Y MEMORIA EXTERNA
          Text("3. AUTOAPRENDIZAJE & MEMORIA SERVIDORES", color = ShieldGreen, fontSize = 11.sp, fontWeight = FontWeight.Bold)

          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Text("Autoaprendizaje Continuo Activo:", color = TextMain, fontSize = 11.sp, fontWeight = FontWeight.Bold)
            Switch(
              checked = autoLearningEnabled,
              onCheckedChange = { autoLearningEnabled = it },
              colors = SwitchDefaults.colors(checkedThumbColor = ShieldGreen)
            )
          }

          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Text("Sincronizar con Google Drive / Cloud:", color = TextSub, fontSize = 10.sp)
            Switch(
              checked = driveSyncEnabled,
              onCheckedChange = { driveSyncEnabled = it },
              colors = SwitchDefaults.colors(checkedThumbColor = MetalSecondary)
            )
          }

          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Text("Sincronizar con Mega / TeraBox / Nube:", color = TextSub, fontSize = 10.sp)
            Switch(
              checked = megaTeraboxSyncEnabled,
              onCheckedChange = { megaTeraboxSyncEnabled = it },
              colors = SwitchDefaults.colors(checkedThumbColor = MetalPrimary)
            )
          }

          Button(
            onClick = { InternalAiManager.triggerCloudAndLocalMemorySync() },
            shape = RoundedCornerShape(8.dp),
            colors = ButtonDefaults.buttonColors(containerColor = MetalSecondary, contentColor = Color.Black),
            modifier = Modifier.fillMaxWidth()
          ) {
            Icon(Icons.Default.CloudSync, contentDescription = null, modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.width(6.dp))
            Text("Forzar Sincronización de Memoria Ahora", fontSize = 11.sp, fontWeight = FontWeight.Bold)
          }
        }

        Spacer(modifier = Modifier.height(8.dp))

        Button(
          onClick = onDismiss,
          modifier = Modifier.fillMaxWidth().height(42.dp),
          shape = RoundedCornerShape(10.dp),
          colors = ButtonDefaults.buttonColors(containerColor = MetalSecondary, contentColor = Color.Black)
        ) {
          Text("Cerrar y Guardar Parámetros", fontWeight = FontWeight.Bold, fontSize = 12.sp)
        }
      }
    }
  }
}
