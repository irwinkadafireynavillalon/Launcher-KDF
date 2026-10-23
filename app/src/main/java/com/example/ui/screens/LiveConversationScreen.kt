package com.example.ui.screens
import androidx.compose.ui.geometry.*
import androidx.compose.ui.draw.*
import androidx.compose.ui.graphics.*
import com.example.ui.theme.*

import android.Manifest
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.isImeVisible
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AttachFile
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Dns
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MicOff
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.RadioButtonChecked
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material.icons.filled.VolumeOff
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.*
import androidx.compose.ui.draw.drawBehind
import androidx.compose.material3.HorizontalDivider
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.example.audio.SpeechRecognitionHelper
import com.example.audio.TtsManager
import com.example.data.local.ConversationMessage
import com.example.hardware.ApkShareHelper
import com.example.hardware.AttachedFilePayload
import com.example.hardware.InternalAiManager
import androidx.compose.ui.draw.drawBehind
import androidx.compose.material3.HorizontalDivider
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(androidx.compose.foundation.layout.ExperimentalLayoutApi::class)
@Composable
fun LiveConversationScreen(
  messages: List<ConversationMessage>,
  selectedLanguage: String,
  selectedScenario: String,
  isLoading: Boolean,
  ttsManager: TtsManager,
  speechHelper: SpeechRecognitionHelper,
  fastLookupResult: String?,
  onSendMessage: (String) -> Unit,
  onLanguageChange: (String) -> Unit,
  onScenarioChange: (String) -> Unit,
  onFastLookup: (String) -> Unit,
  onClearChat: () -> Unit,
  onCaptureImageToChat: (Bitmap, String) -> Unit = { _, _ -> },
  onSendAttachedFile: (AttachedFilePayload, String) -> Unit = { _, _ -> },
  onGenerateImagePrompt: (String) -> Unit = {},
  onOpenLiveSession: () -> Unit = {},
  onOpenInternalAi: () -> Unit = {},
  onOpenHistory: () -> Unit = {},
  onNewChat: () -> Unit = {},
  onSaveSession: () -> Unit = {},
  practiceSessionCount: Int = 0
) {
  val context = LocalContext.current
  val scope = rememberCoroutineScope()
  var textInput by remember { mutableStateOf("") }
  var quickLookupInput by remember { mutableStateOf("") }
  var showFastLookupBar by remember { mutableStateOf(false) }
  var showActionMenu by remember { mutableStateOf(false) }
  var showContextMenu by remember { mutableStateOf(false) }
  var attachedFile by remember { mutableStateOf<AttachedFilePayload?>(null) }

  val listState = rememberLazyListState()

  val isSpeaking by ttsManager.isSpeaking.collectAsState()
  val speechRate by ttsManager.speechRate.collectAsState()
  val autoSpeak by ttsManager.autoSpeakReplies.collectAsState()
  val isListening by speechHelper.isListening.collectAsState()
  val spokenText by speechHelper.spokenText.collectAsState()

  // Ensure soft keyboard does not obstruct text field and recent messages
  val isImeVisible = WindowInsets.isImeVisible
  LaunchedEffect(isImeVisible) {
    if (isImeVisible && messages.isNotEmpty()) {
      listState.animateScrollToItem(messages.size - 1)
    }
  }

  // Auto update input if user speaks
  LaunchedEffect(spokenText) {
    if (spokenText.isNotBlank()) {
      textInput = spokenText
    }
  }

  // Auto scroll to bottom when new message arrives
  LaunchedEffect(messages.size, isLoading) {
    if (messages.isNotEmpty()) {
      listState.animateScrollToItem(messages.size - 1)
    }
  }

  // Audio permission launcher
  val permissionLauncher = rememberLauncherForActivityResult(
    ActivityResultContracts.RequestPermission()
  ) { isGranted ->
    if (isGranted) {
      speechHelper.startListening(selectedLanguage) { result ->
        textInput = result
      }
    }
  }

  // Camera capture launcher for instant visual translation in chat
  val cameraCaptureLauncher = rememberLauncherForActivityResult(
    ActivityResultContracts.TakePicturePreview()
  ) { bitmap ->
    if (bitmap != null) {
      onCaptureImageToChat(bitmap, textInput.trim())
      textInput = ""
    }
  }

  val cameraPermissionLauncher = rememberLauncherForActivityResult(
    ActivityResultContracts.RequestPermission()
  ) { granted ->
    if (granted) {
      cameraCaptureLauncher.launch(null)
    }
  }

  // Universal File Picker Launcher (PDF, TXT, CSV, JSON, Audio, Video, Image, Code, Binary)
  val filePickerLauncher = rememberLauncherForActivityResult(
    ActivityResultContracts.GetContent()
  ) { uri ->
    if (uri != null) {
      val payload = ApkShareHelper.readAttachedFileFromUri(context, uri)
      if (payload != null) {
        attachedFile = payload
        Toast.makeText(
          context,
          "Archivo listo: ${payload.fileName} (${payload.sizeFormatted})",
          Toast.LENGTH_SHORT
        ).show()
      } else {
        Toast.makeText(context, "No se pudo leer el archivo seleccionado", Toast.LENGTH_SHORT).show()
      }
    }
  }

  // Waveform animation
  val infiniteTransition = rememberInfiniteTransition(label = "pulse")
  val pulseScale by infiniteTransition.animateFloat(
    initialValue = 1.0f,
    targetValue = 1.22f,
    animationSpec = infiniteRepeatable(
      animation = tween(600, easing = FastOutSlowInEasing),
      repeatMode = RepeatMode.Reverse
    ),
    label = "scale"
  )

  val languages = listOf(
    "English", "French", "German", "Japanese", "Spanish",
    "Italian", "Mandarin", "Portuguese", "Korean", "Russian", "Arabic"
  )
  val scenarios = listOf(
    "Conversación Casual & Amistosa",
    "Ordenando en Restaurante / Café",
    "Entrevista de Trabajo",
    "Aeropuerto y Viajes",
    "Soporte Técnico & Negocios",
    "Reunión de Amigos / Charla Cotidiana",
    "Negociación Ejecutiva & Contratos",
    "Emergencia Médica & Seguridad",
    "Debate Cultural & Académico"
  )

  Column(
    modifier = Modifier
      .fillMaxSize()
      .damaskBackground()
  ) {
    // Ultra-Compact Status & Controls Toolbar
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .background(Color.White.copy(alpha = 0.9f))
        .padding(horizontal = 10.dp, vertical = 6.dp),
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.CenterVertically
    ) {
      Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.weight(1f)
      ) {
        if (isSpeaking) {
          Icon(
            imageVector = Icons.Default.GraphicEq,
            contentDescription = "Hablando",
            tint = MetalPrimary,
            modifier = Modifier
              .size(15.dp)
              .scale(pulseScale)
          )
          Spacer(modifier = Modifier.width(5.dp))
          Text(
            text = "Hablando ($selectedLanguage)...",
            color = MetalPrimary,
            fontSize = 10.sp,
            fontFamily = FontFamily.Monospace,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
          )
        } else if (isListening) {
          Icon(
            imageVector = Icons.Default.Mic,
            contentDescription = "Escuchando",
            tint = DangerRed,
            modifier = Modifier
              .size(15.dp)
              .scale(pulseScale)
          )
          Spacer(modifier = Modifier.width(5.dp))
          Text(
            text = "Escuchando voz...",
            color = DangerRed,
            fontSize = 10.sp,
            fontFamily = FontFamily.Monospace,
            maxLines = 1
          )
        } else {
          Box {
            Row(
              verticalAlignment = Alignment.CenterVertically,
              modifier = Modifier
                .clickable { showContextMenu = true }
                .testTag("language_scenario_selector_chip")
            ) {
              Box(
                modifier = Modifier
                  .size(7.dp)
                  .background(ShieldGreen, CircleShape)
              )
              Spacer(modifier = Modifier.width(5.dp))
              Text(
                text = "$selectedLanguage • ${selectedScenario.take(18)}...",
                color = TextSub,
                fontSize = 10.sp,
                fontFamily = FontFamily.Monospace,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
              )
              Icon(
                imageVector = Icons.Default.ExpandMore,
                contentDescription = "Cambiar idioma o escenario",
                tint = MetalSecondary,
                modifier = Modifier.size(13.dp)
              )
            }

            DropdownMenu(
              expanded = showContextMenu,
              onDismissRequest = { showContextMenu = false },
              modifier = Modifier
                .background(Color.White)
                .border(1.dp, MetalPrimary, RoundedCornerShape(10.dp))
            ) {
              Text(
                text = "IDIOMA DE PRÁCTICA",
                color = MetalSecondary,
                fontSize = 9.sp,
                fontWeight = FontWeight.ExtraBold,
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
              )
              languages.forEach { lang ->
                DropdownMenuItem(
                  text = { Text(lang, color = TextMain, fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                  onClick = {
                    onLanguageChange(lang)
                    showContextMenu = false
                  }
                )
              }
              HorizontalDivider(color = MetalPrimary.copy(alpha = 0.4f))
              Text(
                text = "ESCENARIO DE PRÁCTICA",
                color = MetalSecondary,
                fontSize = 9.sp,
                fontWeight = FontWeight.ExtraBold,
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
              )
              scenarios.forEach { scen ->
                DropdownMenuItem(
                  text = { Text(scen, color = TextMain, fontSize = 11.sp) },
                  onClick = {
                    onScenarioChange(scen)
                    showContextMenu = false
                  }
                )
              }
            }
          }
        }
      }

      Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
        // Compact Room Sessions count & open button
        Box(
          modifier = Modifier
            .background(SurfacePearl, RoundedCornerShape(6.dp))
            .border(1.dp, MetalSecondary, RoundedCornerShape(6.dp))
            .clickable { onOpenHistory() }
            .padding(horizontal = 6.dp, vertical = 3.dp)
            .testTag("open_sessions_toolbar_button")
        ) {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
              imageVector = Icons.Default.History,
              contentDescription = "Sesiones guardadas",
              tint = MetalSecondary,
              modifier = Modifier.size(11.dp)
            )
            Spacer(modifier = Modifier.width(3.dp))
            Text(
              text = "$practiceSessionCount",
              color = MetalSecondary,
              fontSize = 9.sp,
              fontWeight = FontWeight.Bold,
              fontFamily = FontFamily.Monospace
            )
          }
        }

        // Compact Save Practice Session to Room button
        IconButton(
          onClick = {
            onSaveSession()
            Toast.makeText(context, "Sesión de práctica guardada en Room DB", Toast.LENGTH_SHORT).show()
          },
          modifier = Modifier
            .size(28.dp)
            .testTag("save_session_toolbar_button")
        ) {
          Icon(
            imageVector = Icons.Default.Add,
            contentDescription = "Guardar Sesión en Room",
            tint = MetalSecondary,
            modifier = Modifier.size(15.dp)
          )
        }

        // TTS Speed cycle button
        Box(
          modifier = Modifier
            .background(SurfacePearl, RoundedCornerShape(6.dp))
            .border(1.dp, MetalPrimary, RoundedCornerShape(6.dp))
            .clickable {
              val nextRate = when {
                speechRate < 0.9f -> 1.0f
                speechRate < 1.15f -> 1.25f
                else -> 0.75f
              }
              ttsManager.setSpeechRate(nextRate)
            }
            .padding(horizontal = 6.dp, vertical = 3.dp)
        ) {
          Text(
            text = "${speechRate}x",
            color = MetalSecondary,
            fontSize = 9.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.Monospace
          )
        }

        // Auto-speak toggle button
        IconButton(
          onClick = { ttsManager.setAutoSpeak(!autoSpeak) },
          modifier = Modifier.size(28.dp)
        ) {
          Icon(
            imageVector = if (autoSpeak) Icons.Default.VolumeUp else Icons.Default.VolumeOff,
            contentDescription = "Voz Automática",
            tint = if (autoSpeak) ShieldGreen else TextSub,
            modifier = Modifier.size(15.dp)
          )
        }

        // Fast Lookup toggle
        IconButton(
          onClick = { showFastLookupBar = !showFastLookupBar },
          modifier = Modifier
            .size(28.dp)
            .testTag("toggle_fast_lookup_button")
        ) {
          Icon(
            imageVector = Icons.Default.Search,
            contentDescription = "Traducción Rápida",
            tint = if (showFastLookupBar) MetalSecondary else MetalPrimary,
            modifier = Modifier.size(15.dp)
          )
        }

        // Clear chat
        IconButton(
          onClick = onClearChat,
          modifier = Modifier
            .size(28.dp)
            .testTag("clear_chat_button")
        ) {
          Icon(
            imageVector = Icons.Default.DeleteSweep,
            contentDescription = "Limpiar Chat",
            tint = TextSub,
            modifier = Modifier.size(15.dp)
          )
        }
      }
    }



    // Fast Lookup Bar (gemini-1.5-flash)
    AnimatedVisibility(visible = showFastLookupBar) {
      Card(
        modifier = Modifier
          .fillMaxWidth()
          .padding(horizontal = 10.dp, vertical = 4.dp)
          .skeuomorphicCard(borderColor = MetalPrimary),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent)
      ) {
        Column(modifier = Modifier.padding(8.dp)) {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Text(
              text = "Traducción Rápida (gemini-1.5-flash)",
              color = MetalPrimary,
              fontSize = 10.sp,
              fontWeight = FontWeight.Bold
            )
            if (fastLookupResult != null && quickLookupInput.isNotBlank()) {
              Text(
                text = "Enviar como Archivo",
                color = MetalSecondary,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.clickable {
                  scope.launch {
                    ApkShareHelper.shareTextAsFile(
                      context = context,
                      fileNamePrefix = "Traduccion_${quickLookupInput.trim()}",
                      textContent = fastLookupResult
                    )
                  }
                }
              )
            }
          }
          Spacer(modifier = Modifier.height(4.dp))
          Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
          ) {
            OutlinedTextField(
              value = quickLookupInput,
              onValueChange = { quickLookupInput = it },
              placeholder = { Text("Palabra o modismo...", fontSize = 11.sp, color = TextSub) },
              modifier = Modifier.weight(1f),
              singleLine = true,
              shape = RoundedCornerShape(8.dp),
              colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = TextMain,
                unfocusedTextColor = TextMain,
                focusedContainerColor = SurfacePearl,
                unfocusedContainerColor = SurfacePearl
              )
            )
            Spacer(modifier = Modifier.width(6.dp))
            IconButton(
              onClick = {
                if (quickLookupInput.isNotBlank()) {
                  onFastLookup(quickLookupInput.trim())
                }
              },
              modifier = Modifier
                .size(36.dp)
                .background(MetalPrimary, RoundedCornerShape(8.dp))
            ) {
              Icon(Icons.Default.Search, contentDescription = "Buscar", tint = Color.Black, modifier = Modifier.size(18.dp))
            }
          }

          if (fastLookupResult != null) {
            Spacer(modifier = Modifier.height(6.dp))
            Card(
              modifier = Modifier.fillMaxWidth(),
              colors = CardDefaults.cardColors(containerColor = SurfacePearl)
            ) {
              Text(
                text = fastLookupResult,
                color = TextMain,
                fontSize = 11.sp,
                modifier = Modifier.padding(8.dp),
                lineHeight = 15.sp
              )
            }
          }
        }
      }
    }

    // Scrollable Messages Thread (Main Area)
    LazyColumn(
      state = listState,
      modifier = Modifier
        .weight(1f)
        .fillMaxWidth()
        .testTag("chat_messages_list"),
      contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
      verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
      items(messages, key = { it.id }) { msg ->
        val isUser = msg.role == "user"
        Column(
          modifier = Modifier.fillMaxWidth(),
          horizontalAlignment = if (isUser) Alignment.End else Alignment.Start
        ) {
          // Compact Message Header
          Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
          ) {
            Text(
              text = if (isUser) "Tú" else "KDF-LuX",
              color = TextSub,
              fontSize = 9.sp,
              fontFamily = FontFamily.Monospace
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
              text = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date(msg.timestamp)),
              color = TextSub,
              fontSize = 9.sp
            )
            if (!isUser && msg.alphaTriggered) {
              Spacer(modifier = Modifier.width(6.dp))
              Box(
                modifier = Modifier
                  .background(MetalSecondary.copy(alpha = 0.2f), RoundedCornerShape(4.dp))
                  .padding(horizontal = 4.dp, vertical = 1.dp)
              ) {
                Text(
                  text = "ALPHA",
                  color = MetalSecondary,
                  fontSize = 8.sp,
                  fontWeight = FontWeight.Bold,
                  fontFamily = FontFamily.Monospace
                )
              }
            }
          }

          // Message Bubble
          Card(
            modifier = Modifier
              .fillMaxWidth(0.90f)
              .skeuomorphicCard(
                elevation = 4.dp,
                cornerRadius = 14.dp,
                borderColor = if (isUser) MetalSecondary else MetalPrimary
              ),
            colors = CardDefaults.cardColors(containerColor = Color.Transparent),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
          ) {
            Column(modifier = Modifier.padding(horizontal = 11.dp, vertical = 9.dp)) {
              Text(
                text = msg.content,
                color = TextMain,
                fontSize = 13.sp,
                lineHeight = 18.sp
              )

              // Compact Action Bar for AI messages
              if (!isUser) {
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                  modifier = Modifier.fillMaxWidth(),
                  horizontalArrangement = Arrangement.End,
                  verticalAlignment = Alignment.CenterVertically
                ) {
                  // Copy
                  IconButton(
                    onClick = {
                      val cm = context.getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager
                      cm?.setPrimaryClip(ClipData.newPlainText("KDF-LuX", msg.content))
                      Toast.makeText(context, "Copiado al portapapeles", Toast.LENGTH_SHORT).show()
                    },
                    modifier = Modifier.size(24.dp)
                  ) {
                    Icon(
                      imageVector = Icons.Default.ContentCopy,
                      contentDescription = "Copiar texto",
                      tint = TextSub,
                      modifier = Modifier.size(13.dp)
                    )
                  }

                  Spacer(modifier = Modifier.width(6.dp))

                  // Auto-Mejora Feedback button
                  IconButton(
                    onClick = {
                      val prefs = context.getSharedPreferences("kdf_lux_improvement_prefs", Context.MODE_PRIVATE)
                      val existing = prefs.getString("user_improvement_rules", "") ?: ""
                      val newRule = "Ajuste por feedback de usuario (${SimpleDateFormat("dd/MM HH:mm", Locale.getDefault()).format(Date())}): ${msg.content.take(60)}..."
                      val updated = if (existing.isBlank()) newRule else "$existing\n$newRule"
                      prefs.edit().putString("user_improvement_rules", updated).apply()
                      Toast.makeText(context, "✨ Orden de Auto-Mejora guardada en ajustes y aplicada al Orquestador", Toast.LENGTH_LONG).show()
                    },
                    modifier = Modifier.size(24.dp)
                  ) {
                    Icon(
                      imageVector = Icons.Default.AutoAwesome,
                      contentDescription = "Enviar Feedback para Auto-Mejora",
                      tint = MetalSecondary,
                      modifier = Modifier.size(13.dp)
                    )
                  }

                  Spacer(modifier = Modifier.width(6.dp))

                  // Send / Share message as a downloadable .txt File
                  IconButton(
                    onClick = {
                      scope.launch {
                        ApkShareHelper.shareTextAsFile(
                          context = context,
                          fileNamePrefix = "Mensaje_${selectedLanguage}",
                          textContent = msg.content
                        )
                      }
                    },
                    modifier = Modifier.size(24.dp)
                  ) {
                    Icon(
                      imageVector = Icons.Default.Share,
                      contentDescription = "Enviar como Archivo",
                      tint = MetalSecondary,
                      modifier = Modifier.size(14.dp)
                    )
                  }

                  Spacer(modifier = Modifier.width(6.dp))

                  // Speak / Stop TTS
                  IconButton(
                    onClick = {
                      if (isSpeaking) ttsManager.stop() else ttsManager.speak(msg.content, selectedLanguage)
                    },
                    modifier = Modifier.size(24.dp)
                  ) {
                    Icon(
                      imageVector = if (isSpeaking) Icons.Default.Stop else Icons.Default.VolumeUp,
                      contentDescription = "Escuchar pronunciación",
                      tint = MetalPrimary,
                      modifier = Modifier.size(15.dp)
                    )
                  }
                }
              }
            }
          }
        }
      }

      if (isLoading) {
        item {
          Row(
            modifier = Modifier
              .fillMaxWidth()
              .padding(6.dp),
            verticalAlignment = Alignment.CenterVertically
          ) {
            CircularProgressIndicator(
              color = MetalPrimary,
              modifier = Modifier.size(14.dp),
              strokeWidth = 2.dp
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
              text = "Orquestador procesando...",
              color = TextSub,
              fontSize = 10.sp,
              fontFamily = FontFamily.Monospace
            )
          }
        }
      }
    }

    // Compact Input Bar
    Card(
      modifier = Modifier
        .fillMaxWidth()
        .imePadding(),
      shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp),
      colors = CardDefaults.cardColors(containerColor = Color.White),
      elevation = CardDefaults.cardElevation(defaultElevation = 10.dp)
    ) {
      Column(
        modifier = Modifier
          .fillMaxWidth()
          .drawBehind {
             // Molding line
             drawLine(
                brush = Brush.horizontalGradient(listOf(MetalPrimary, MetalSecondary, MetalPrimary)),
                start = androidx.compose.ui.geometry.Offset(0f, 0f),
                end = androidx.compose.ui.geometry.Offset(size.width, 0f),
                strokeWidth = 2.dp.toPx()
             )
          }
      ) {
        // Attached File Preview Banner
        if (attachedFile != null) {
          val file = attachedFile!!
          Row(
            modifier = Modifier
              .fillMaxWidth()
              .background(SurfacePearl)
              .padding(horizontal = 12.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Row(
              verticalAlignment = Alignment.CenterVertically,
              modifier = Modifier.weight(1f)
            ) {
              Icon(
                imageVector = Icons.Default.Description,
                contentDescription = null,
                tint = MetalSecondary,
                modifier = Modifier.size(20.dp)
              )
              Spacer(modifier = Modifier.width(8.dp))
              Column {
                Text(
                  text = file.fileName,
                  color = TextMain,
                  fontSize = 12.sp,
                  fontWeight = FontWeight.Bold,
                  maxLines = 1,
                  overflow = TextOverflow.Ellipsis
                )
                Text(
                  text = "${file.sizeFormatted} • ${file.mimeType}",
                  color = TextSub,
                  fontSize = 10.sp
                )
              }
            }

            IconButton(
              onClick = { attachedFile = null },
              modifier = Modifier.size(24.dp)
            ) {
              Icon(Icons.Default.Close, contentDescription = null, tint = PlumbousGray, modifier = Modifier.size(16.dp))
            }
          }
          HorizontalDivider(color = MetalPrimary.copy(alpha = 0.2f))
        }

        Row(
          modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 10.dp, vertical = 8.dp),
          verticalAlignment = Alignment.CenterVertically
        ) {
          // Action Dropdown Button (+)
          Box {
            IconButton(
              onClick = { showActionMenu = true },
              modifier = Modifier
                .size(40.dp)
                .jewelButton()
                .testTag("chat_actions_dropdown_button")
            ) {
              Icon(
                imageVector = Icons.Default.Add,
                contentDescription = "Opciones",
                tint = MetalSecondary,
                modifier = Modifier.size(22.dp)
              )
            }

            DropdownMenu(
              expanded = showActionMenu,
              onDismissRequest = { showActionMenu = false },
              modifier = Modifier
                .background(Color.White)
                .border(1.5.dp, MetalPrimary, RoundedCornerShape(12.dp))
            ) {
              DropdownMenuItem(
                text = { Text("Cámara (Visión IA)", color = TextMain, fontWeight = FontWeight.Bold) },
                leadingIcon = { Icon(Icons.Default.CameraAlt, contentDescription = null, tint = MetalPrimary) },
                onClick = {
                  showActionMenu = false
                  val hasCam = ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
                  if (hasCam) cameraCaptureLauncher.launch(null) else cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
                }
              )
              DropdownMenuItem(
                text = { Text("Adjuntar Archivo", color = TextMain, fontWeight = FontWeight.Bold) },
                leadingIcon = { Icon(Icons.Default.AttachFile, contentDescription = null, tint = MetalSecondary) },
                onClick = {
                  showActionMenu = false
                  filePickerLauncher.launch("*/*")
                }
              )
              DropdownMenuItem(
                text = { Text("Guardar Sesión de Práctica", color = TextMain, fontWeight = FontWeight.Bold) },
                leadingIcon = { Icon(Icons.Default.History, contentDescription = null, tint = MetalSecondary) },
                onClick = {
                  showActionMenu = false
                  onSaveSession()
                  Toast.makeText(context, "Sesión guardada en base de datos Room", Toast.LENGTH_SHORT).show()
                }
              )
              DropdownMenuItem(
                text = { Text("Sesiones Guardadas ($practiceSessionCount)", color = TextMain, fontWeight = FontWeight.Bold) },
                leadingIcon = { Icon(Icons.Default.Refresh, contentDescription = null, tint = MetalPrimary) },
                onClick = {
                  showActionMenu = false
                  onOpenHistory()
                }
              )
              DropdownMenuItem(
                text = { Text("Crear Imagen IA", color = TextMain, fontWeight = FontWeight.Bold) },
                leadingIcon = { Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = MetalSecondary) },
                onClick = {
                  showActionMenu = false
                  if (textInput.isNotBlank()) {
                    onGenerateImagePrompt(textInput.trim())
                    textInput = ""
                  } else {
                    Toast.makeText(context, "Escribe una descripción primero", Toast.LENGTH_SHORT).show()
                  }
                }
              )
            }
          }

          Spacer(modifier = Modifier.width(8.dp))

          OutlinedTextField(
            value = textInput,
            onValueChange = { textInput = it },
            placeholder = {
              Text(
                "Escribe un mensaje...",
                fontSize = 13.sp,
                color = PlumbousGray
              )
            },
            modifier = Modifier
              .weight(1f)
              .heightIn(min = 44.dp)
              .testTag("chat_input_text_field"),
            shape = RoundedCornerShape(22.dp),
            colors = OutlinedTextFieldDefaults.colors(
              focusedBorderColor = MetalSecondary,
              unfocusedBorderColor = MetalPrimary,
              focusedContainerColor = SurfacePearl,
              unfocusedContainerColor = SurfacePearl,
              focusedTextColor = TextMain,
              unfocusedTextColor = TextMain
            ),
            maxLines = 3
          )

          Spacer(modifier = Modifier.width(8.dp))

          // Mic Button
          IconButton(
            onClick = {
              if (isListening) {
                speechHelper.stopListening()
              } else {
                val hasPermission = ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED
                if (hasPermission) speechHelper.startListening(selectedLanguage) { textInput = it }
                else permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
              }
            },
            modifier = Modifier
              .size(40.dp)
              .background(if (isListening) Color(0xFFFF4D6D) else Color.White, CircleShape)
              .border(2.dp, if (isListening) Color.White else MetalPrimary, CircleShape)
              .testTag("microphone_button")
          ) {
            Icon(
              imageVector = if (isListening) Icons.Default.MicOff else Icons.Default.Mic,
              contentDescription = "Micrófono",
              tint = if (isListening) Color.White else MetalPrimary,
              modifier = Modifier.size(20.dp)
            )
          }

          Spacer(modifier = Modifier.width(6.dp))

          // Send Button
          val canSend = (textInput.isNotBlank() || attachedFile != null) && !isLoading
          IconButton(
            onClick = {
              val currentFile = attachedFile
              val toSend = textInput.trim()
              if (currentFile != null) {
                textInput = ""
                attachedFile = null
                onSendAttachedFile(currentFile, toSend)
              } else if (toSend.isNotBlank()) {
                textInput = ""
                onSendMessage(toSend)
              }
            },
            enabled = canSend,
            modifier = Modifier
              .size(40.dp)
              .jewelButton()
              .testTag("send_message_button")
          ) {
            Icon(
              imageVector = Icons.Default.Send,
              contentDescription = "Enviar",
              tint = if (canSend) MetalSecondary else PlumbousGray,
              modifier = Modifier.size(20.dp)
            )
          }
        }
      }
    }
  }
}
