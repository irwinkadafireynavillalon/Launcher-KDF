package com.example.ui.screens
import androidx.compose.ui.geometry.*
import androidx.compose.ui.draw.*
import androidx.compose.ui.graphics.*
import com.example.ui.theme.*

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.graphics.Bitmap
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Hub
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material.icons.filled.Wallpaper
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.audio.TtsManager
import java.io.File
import java.io.FileOutputStream

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun OrchestratorScreen(
  selectedLanguage: String,
  generatedImage: Bitmap?,
  isGeneratingImage: Boolean,
  imageError: String?,
  orchestratorResult: String?,
  isOrchestrating: Boolean,
  orchestratorLogs: List<String>,
  ttsManager: TtsManager,
  onGenerateImage: (prompt: String, aspectRatio: String) -> Unit,
  onClearGeneratedImage: () -> Unit,
  onRunOrchestration: (masterGoal: String) -> Unit,
  onClearOrchestratorLogs: () -> Unit,
  onSendImageToChat: (Bitmap, String) -> Unit
) {
  val context = LocalContext.current
  var imagePrompt by remember { mutableStateOf("") }
  var selectedAspectRatio by remember { mutableStateOf("1:1") }
  var masterGoalInput by remember { mutableStateOf("") }

  val aspectRatios = listOf("1:1", "16:9", "9:16", "4:3")

  val sampleImagePrompts = listOf(
    "Ciudad cyberpunk dorada con rascacielos holográficos Hyper Lux",
    "Retrato fotorrealista de un gato astronauta en el espacio",
    "Paisaje místico japonés con cerezos en flor al atardecer",
    "Emblema heráldico futurista 3D con escudo dorado y neón cian"
  )

  val sampleWorkflows = listOf(
    "Plan de aprendizaje acelerado de $selectedLanguage con recursos visuales y reglas de seguridad",
    "Auditoría integral del sistema: escaneo de riesgos, limpieza de datos y optimización",
    "Generar historia interactiva bilingüe con vocabulario clave y traducción fonética"
  )

  LazyColumn(
    modifier = Modifier
      .fillMaxSize()
      .damaskBackground()
      .padding(horizontal = 12.dp)
      .imePadding(),
    contentPadding = PaddingValues(vertical = 12.dp),
    verticalArrangement = Arrangement.spacedBy(14.dp)
  ) {
    // 1. Header Banner del Orquestador
    item {
      Card(
        modifier = Modifier
          .fillMaxWidth()
          .skeuomorphicCard(borderColor = MetalSecondary),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent)
      ) {
        Column(modifier = Modifier.padding(14.dp)) {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
              Box(
                modifier = Modifier
                  .size(36.dp)
                  .clip(CircleShape)
                  .background(MetalSecondary.copy(alpha = 0.2f))
                  .border(1.2.dp, MetalSecondary, CircleShape),
                contentAlignment = Alignment.Center
              ) {
                Icon(
                  imageVector = Icons.Default.Hub,
                  contentDescription = "IA Orquestador",
                  tint = MetalSecondary,
                  modifier = Modifier.size(20.dp)
                )
              }
              Spacer(modifier = Modifier.width(10.dp))
              Column {
                Text(
                  text = "IA ORQUESTADOR SUPREMO",
                  color = TextMain,
                  fontSize = 14.sp,
                  fontWeight = FontWeight.ExtraBold,
                  letterSpacing = 0.5.sp
                )
                Text(
                  text = "Coordinación Multi-Agente & Servicios Gemini",
                  color = MetalPrimary,
                  fontSize = 10.sp,
                  fontFamily = FontFamily.Monospace
                )
              }
            }

            Box(
              modifier = Modifier
                .background(ShieldGreen.copy(alpha = 0.18f), RoundedCornerShape(6.dp))
                .border(0.8.dp, ShieldGreen, RoundedCornerShape(6.dp))
                .padding(horizontal = 6.dp, vertical = 3.dp)
            ) {
              Text(
                text = "EN LÍNEA",
                color = ShieldGreen,
                fontSize = 9.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace
              )
            }
          }

          Spacer(modifier = Modifier.height(10.dp))

          // 5 Agentes en red
          FlowRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
          ) {
            AgentStatusPill("🧠 Gemini 1.5 Pro", "Razonador")
            AgentStatusPill("🎨 Gemini 1.5 Flash", "Creador Visual")
            AgentStatusPill("🌐 Gemini 1.5 Flash", "Políglota")
            AgentStatusPill("🛡️ Protocolo Alpha", "Escudo")
            AgentStatusPill("⚡ CPU / GPU Dinámico", "Hardware")
            AgentStatusPill("⚙️ Auto-Evolución", "Reparación")
          }
        }
      }
    }

    // 2. SECCIÓN: GENERADOR DE FOTOS E IMÁGENES GEMINI
    item {
      Card(
        modifier = Modifier.fillMaxWidth().testTag("gemini_image_studio_card"),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = SurfacePearl),
        border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(MetalPrimary.copy(alpha = 0.7f)))
      ) {
        Column(modifier = Modifier.padding(14.dp)) {
          Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.fillMaxWidth()
          ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
              Icon(
                imageVector = Icons.Default.Palette,
                contentDescription = null,
                tint = MetalPrimary,
                modifier = Modifier.size(18.dp)
              )
              Spacer(modifier = Modifier.width(8.dp))
              Text(
                text = "CREACIÓN DE FOTOS & IMÁGENES",
                color = TextMain,
                fontWeight = FontWeight.Bold,
                fontSize = 12.sp,
                letterSpacing = 0.5.sp
              )
            }

            Box(
              modifier = Modifier
                .background(MetalPrimary.copy(alpha = 0.15f), RoundedCornerShape(6.dp))
                .border(0.6.dp, MetalPrimary, RoundedCornerShape(6.dp))
                .padding(horizontal = 6.dp, vertical = 2.dp)
            ) {
              Text(
                text = "gemini-1.5-flash",
                color = MetalPrimary,
                fontSize = 8.sp,
                fontFamily = FontFamily.Monospace
              )
            }
          }

          Spacer(modifier = Modifier.height(8.dp))
          Text(
            text = "Genera arte, fotos realistas e ilustraciones conceptuales describiendo lo que imaginas con Gemini:",
            color = TextSub,
            fontSize = 11.sp
          )

          Spacer(modifier = Modifier.height(10.dp))

          // Prompt TextField
          OutlinedTextField(
            value = imagePrompt,
            onValueChange = { imagePrompt = it },
            placeholder = { Text("Ej: Retrato cyberpunk con neón dorado y cian...", fontSize = 12.sp, color = TextSub) },
            modifier = Modifier
              .fillMaxWidth()
              .testTag("image_prompt_input"),
            shape = RoundedCornerShape(10.dp),
            colors = OutlinedTextFieldDefaults.colors(
              focusedBorderColor = MetalSecondary,
              unfocusedBorderColor = MetalPrimary,
              focusedContainerColor = Color.White,
              unfocusedContainerColor = Color.White,
              focusedTextColor = TextMain,
              unfocusedTextColor = TextMain
            ),
            maxLines = 3
          )

          Spacer(modifier = Modifier.height(8.dp))

          // Sugerencias rápidas
          Text(text = "Ideas rápidas:", color = TextSub, fontSize = 10.sp)
          Spacer(modifier = Modifier.height(4.dp))
          FlowRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
          ) {
            sampleImagePrompts.forEach { prompt ->
              Box(
                modifier = Modifier
                  .clip(RoundedCornerShape(6.dp))
                  .background(Color.White)
                  .border(0.6.dp, MetalSecondary.copy(alpha = 0.5f), RoundedCornerShape(6.dp))
                  .clickable { imagePrompt = prompt }
                  .padding(horizontal = 8.dp, vertical = 4.dp)
              ) {
                Text(
                  text = prompt.take(28) + "...",
                  color = MetalSecondary,
                  fontSize = 9.sp,
                  maxLines = 1
                )
              }
            }
          }

          Spacer(modifier = Modifier.height(10.dp))

          // Aspect Ratio Selector
          Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.fillMaxWidth()
          ) {
            Text(text = "Formato:", color = TextSub, fontSize = 11.sp, fontWeight = FontWeight.Bold)
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
              aspectRatios.forEach { ratio ->
                val selected = selectedAspectRatio == ratio
                Box(
                  modifier = Modifier
                    .clip(RoundedCornerShape(6.dp))
                    .background(if (selected) MetalPrimary.copy(alpha = 0.25f) else Color.White)
                    .border(
                      width = if (selected) 1.dp else 0.6.dp,
                      color = if (selected) MetalPrimary else MetalPrimary,
                      shape = RoundedCornerShape(6.dp)
                    )
                    .clickable { selectedAspectRatio = ratio }
                    .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                  Text(
                    text = ratio,
                    color = if (selected) MetalPrimary else TextSub,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold
                  )
                }
              }
            }
          }

          Spacer(modifier = Modifier.height(12.dp))

          // Botón de generación
          Button(
            onClick = {
              if (imagePrompt.isNotBlank() && !isGeneratingImage) {
                onGenerateImage(imagePrompt.trim(), selectedAspectRatio)
              }
            },
            enabled = imagePrompt.isNotBlank() && !isGeneratingImage,
            modifier = Modifier
              .fillMaxWidth()
              .jewelButton()
              .testTag("generate_image_button"),
            colors = ButtonDefaults.buttonColors(
              containerColor = Color.Transparent,
              contentColor = TextMain
            )
          ) {
            if (isGeneratingImage) {
              CircularProgressIndicator(
                modifier = Modifier.size(18.dp),
                color = MetalSecondary,
                strokeWidth = 2.dp
              )
              Spacer(modifier = Modifier.width(8.dp))
              Text("CREANDO ARTE...", fontWeight = FontWeight.ExtraBold, color = MetalSecondary)
            } else {
              Icon(Icons.Default.AutoAwesome, contentDescription = null, modifier = Modifier.size(18.dp), tint = MetalSecondary)
              Spacer(modifier = Modifier.width(8.dp))
              Text("GENERAR IMAGEN GEMINI", fontWeight = FontWeight.ExtraBold, fontSize = 13.sp, color = TextMain)
            }
          }

          if (imageError != null) {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
              text = "Aviso: $imageError",
              color = DangerRed,
              fontSize = 11.sp
            )
          }

          // Visualizador de la Imagen Generada
          if (generatedImage != null) {
            Spacer(modifier = Modifier.height(14.dp))
            HorizontalDivider(color = MetalPrimary, thickness = 0.8.dp)
            Spacer(modifier = Modifier.height(10.dp))

            Text(
              text = "Resultado de Imagen Generada:",
              color = MetalSecondary,
              fontWeight = FontWeight.Bold,
              fontSize = 12.sp
            )
            Spacer(modifier = Modifier.height(8.dp))

            Box(
              modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .border(1.5.dp, MetalSecondary, RoundedCornerShape(12.dp))
                .background(Color.Black)
            ) {
              Image(
                bitmap = generatedImage.asImageBitmap(),
                contentDescription = "Imagen generada por Gemini",
                contentScale = ContentScale.Fit,
                modifier = Modifier
                  .fillMaxWidth()
                  .heightIn(max = 320.dp)
              )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Botones de acción con la imagen
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
              // Botón: Usar como fondo
              OutlinedButton(
                onClick = {
                  try {
                    val file = File(context.filesDir, "custom_bg_gemini_${System.currentTimeMillis()}.png")
                    val out = FileOutputStream(file)
                    generatedImage.compress(Bitmap.CompressFormat.PNG, 95, out)
                    out.flush()
                    out.close()
                    UiThemeController.updateCustomBackgroundUri(context, file.absolutePath)
                    Toast.makeText(context, "¡Imagen establecida como fondo de la app!", Toast.LENGTH_SHORT).show()
                  } catch (e: Exception) {
                    Toast.makeText(context, "Error al guardar fondo: ${e.message}", Toast.LENGTH_SHORT).show()
                  }
                },
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(8.dp)
              ) {
                Icon(Icons.Default.Wallpaper, contentDescription = null, modifier = Modifier.size(14.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Usar Fondo", fontSize = 11.sp)
              }

              // Botón: Enviar al Chat
              Button(
                onClick = {
                  onSendImageToChat(generatedImage, imagePrompt)
                  Toast.makeText(context, "Imagen enviada al chat de IA", Toast.LENGTH_SHORT).show()
                },
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.buttonColors(containerColor = MetalPrimary, contentColor = Color.Black),
                shape = RoundedCornerShape(8.dp)
              ) {
                Icon(Icons.Default.Send, contentDescription = null, modifier = Modifier.size(14.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Enviar a Chat", fontSize = 11.sp)
              }

              // Botón: Quitar imagen
              IconButton(
                onClick = { onClearGeneratedImage() },
                modifier = Modifier
                  .size(38.dp)
                  .background(Color.White, RoundedCornerShape(8.dp))
              ) {
                Icon(Icons.Default.Close, contentDescription = "Cerrar", tint = TextSub, modifier = Modifier.size(16.dp))
              }
            }
          }
        }
      }
    }

    // 3. SECCIÓN: ORQUESTACIÓN DE MISIÓN MULTI-AGENTE
    item {
      Card(
        modifier = Modifier
          .fillMaxWidth()
          .skeuomorphicCard(borderColor = MetalPrimary),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent)
      ) {
        Column(modifier = Modifier.padding(14.dp)) {
          Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.fillMaxWidth()
          ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
              Icon(
                imageVector = Icons.Default.Psychology,
                contentDescription = null,
                tint = MetalSecondary,
                modifier = Modifier.size(20.dp)
              )
              Spacer(modifier = Modifier.width(10.dp))
              Text(
                text = "MISIÓN ORQUESTADA",
                color = TextMain,
                fontWeight = FontWeight.ExtraBold,
                fontSize = 13.sp,
                letterSpacing = 0.5.sp
              )
            }

            Box(
              modifier = Modifier
                .background(MetalSecondary.copy(alpha = 0.1f), RoundedCornerShape(6.dp))
                .border(0.6.dp, MetalSecondary, RoundedCornerShape(6.dp))
                .padding(horizontal = 8.dp, vertical = 3.dp)
            ) {
              Text(
                text = "gemini-1.5-pro",
                color = MetalSecondary,
                fontSize = 8.sp,
                fontWeight = FontWeight.Bold
              )
            }
          }

          Spacer(modifier = Modifier.height(12.dp))
          Text(
            text = "Define un objetivo maestro para que el Orquestador lo ejecute con sus agentes especializados:",
            color = TextSub,
            fontSize = 12.sp
          )

          Spacer(modifier = Modifier.height(10.dp))

          OutlinedTextField(
            value = masterGoalInput,
            onValueChange = { masterGoalInput = it },
            placeholder = { Text("Ej: Plan de inmersión en $selectedLanguage...", fontSize = 12.sp, color = PlumbousGray) },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
              focusedBorderColor = MetalSecondary,
              unfocusedBorderColor = MetalPrimary,
              focusedContainerColor = SurfacePearl,
              unfocusedContainerColor = SurfacePearl,
              focusedTextColor = TextMain,
              unfocusedTextColor = TextMain
            ),
            maxLines = 4
          )

          Spacer(modifier = Modifier.height(14.dp))

          Button(
            onClick = {
              if (masterGoalInput.isNotBlank() && !isOrchestrating) {
                onRunOrchestration(masterGoalInput.trim())
              }
            },
            enabled = masterGoalInput.isNotBlank() && !isOrchestrating,
            modifier = Modifier
              .fillMaxWidth()
              .jewelButton(),
            colors = ButtonDefaults.buttonColors(
              containerColor = Color.Transparent,
              contentColor = TextMain
            )
          ) {
            if (isOrchestrating) {
              CircularProgressIndicator(
                modifier = Modifier.size(18.dp),
                color = MetalSecondary,
                strokeWidth = 2.dp
              )
              Spacer(modifier = Modifier.width(10.dp))
              Text("ORQUESTANDO...", fontWeight = FontWeight.ExtraBold, color = MetalSecondary)
            } else {
              Icon(Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(20.dp), tint = MetalSecondary)
              Spacer(modifier = Modifier.width(10.dp))
              Text("EJECUTAR ORQUESTACIÓN", fontWeight = FontWeight.ExtraBold, fontSize = 13.sp)
            }
          }
        }
      }
    }

    // 4. CONSOLA DE EJECUCIÓN
    item {
      Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = androidx.compose.foundation.BorderStroke(1.dp, MetalPrimary.copy(alpha = 0.5f))
      ) {
        Column(modifier = Modifier.padding(12.dp)) {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
              Box(modifier = Modifier.size(8.dp).background(MetalSecondary, CircleShape))
              Spacer(modifier = Modifier.width(8.dp))
              Text(
                text = "CONSOLA DE EVENTOS",
                color = TextMain,
                fontSize = 11.sp,
                fontWeight = FontWeight.ExtraBold,
                letterSpacing = 0.5.sp
              )
            }

            IconButton(
              onClick = onClearOrchestratorLogs,
              modifier = Modifier.size(24.dp)
            ) {
              Icon(Icons.Default.Refresh, contentDescription = "Limpiar", tint = PlumbousGray, modifier = Modifier.size(16.dp))
            }
          }

          Spacer(modifier = Modifier.height(8.dp))

          Column(
            modifier = Modifier
              .fillMaxWidth()
              .heightIn(max = 150.dp)
              .background(SurfacePearl, RoundedCornerShape(10.dp))
              .padding(10.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
          ) {
            orchestratorLogs.takeLast(6).forEach { log ->
              Text(
                text = "> $log",
                color = if (log.contains("✅", true)) MetalSecondary else TextSub,
                fontSize = 10.sp,
                fontWeight = FontWeight.Medium,
                fontFamily = FontFamily.Monospace
              )
            }
          }
        }
      }
    }

    // 5. RESULTADO DE LA ORQUESTACIÓN
    if (orchestratorResult != null) {
      item {
        Card(
          modifier = Modifier.fillMaxWidth(),
          shape = RoundedCornerShape(14.dp),
          colors = CardDefaults.cardColors(containerColor = Color.White),
          border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(MetalSecondary))
        ) {
          Column(modifier = Modifier.padding(14.dp)) {
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically
            ) {
              Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.CheckCircle, contentDescription = null, tint = MetalSecondary, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                  text = "INFORME FINAL DEL ORQUESTADOR",
                  color = MetalSecondary,
                  fontWeight = FontWeight.Bold,
                  fontSize = 12.sp
                )
              }

              Row {
                IconButton(
                  onClick = {
                    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                    clipboard.setPrimaryClip(ClipData.newPlainText("Informe Orquestador", orchestratorResult))
                    Toast.makeText(context, "Informe copiado", Toast.LENGTH_SHORT).show()
                  },
                  modifier = Modifier.size(28.dp)
                ) {
                  Icon(Icons.Default.ContentCopy, contentDescription = "Copiar", tint = TextSub, modifier = Modifier.size(15.dp))
                }

                IconButton(
                  onClick = { ttsManager.speak(orchestratorResult, selectedLanguage) },
                  modifier = Modifier.size(28.dp)
                ) {
                  Icon(Icons.Default.VolumeUp, contentDescription = "Escuchar", tint = MetalPrimary, modifier = Modifier.size(15.dp))
                }
              }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
              text = orchestratorResult,
              color = TextMain,
              fontSize = 12.sp,
              lineHeight = 18.sp
            )
          }
        }
      }
    }

    // 6. DIRECTORIO DE SERVICIOS GEMINI
    item {
      Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = SurfacePearl),
        border = CardDefaults.outlinedCardBorder()
      ) {
        Column(modifier = Modifier.padding(12.dp)) {
          Text(
            text = "SERVICIOS ACTIVOS DE GEMINI",
            color = MetalSecondary,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.Monospace
          )
          Spacer(modifier = Modifier.height(8.dp))

          GeminiServiceItem("🖼️ Gemini Image Gen", "gemini-1.5-flash", "Creación de imágenes de alta fidelidad desde texto")
          GeminiServiceItem("🧠 Gemini Deep Reasoning", "gemini-1.5-pro", "Pensamiento estructurado para tareas analíticas y código")
          GeminiServiceItem("💬 Gemini Conversational", "gemini-1.5-flash", "Diálogo políglota de baja latencia y alta fluidez")
          GeminiServiceItem("📷 Gemini Vision OCR", "Multimodal ImagePart", "Traducción en tiempo real de fotos y cámara")
          GeminiServiceItem("📎 Gemini Doc Analysis", "Multimodal InlineData", "Análisis de documentos PDF, código y archivos binarios")
          GeminiServiceItem("🛡️ Protocolo Alpha", "Safety Guardrails", "Detección proactiva de riesgos cibernéticos y manipulación")
        }
      }
    }
  }
}

@Composable
fun AgentStatusPill(name: String, role: String) {
  Box(
    modifier = Modifier
      .clip(RoundedCornerShape(8.dp))
      .background(SurfacePearl)
      .border(0.6.dp, MetalPrimary, RoundedCornerShape(8.dp))
      .padding(horizontal = 8.dp, vertical = 4.dp)
  ) {
    Column {
      Text(text = name, color = TextMain, fontSize = 9.sp, fontWeight = FontWeight.Bold)
      Text(text = role, color = MetalPrimary, fontSize = 8.sp, fontFamily = FontFamily.Monospace)
    }
  }
}

@Composable
fun GeminiServiceItem(name: String, model: String, description: String) {
  Row(
    modifier = Modifier
      .fillMaxWidth()
      .padding(vertical = 4.dp),
    verticalAlignment = Alignment.CenterVertically
  ) {
    Box(
      modifier = Modifier
        .size(6.dp)
        .background(MetalPrimary, CircleShape)
    )
    Spacer(modifier = Modifier.width(8.dp))
    Column(modifier = Modifier.weight(1f)) {
      Row(verticalAlignment = Alignment.CenterVertically) {
        Text(text = name, color = TextMain, fontSize = 11.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.width(6.dp))
        Text(text = "• $model", color = MetalSecondary, fontSize = 8.sp, fontFamily = FontFamily.Monospace)
      }
      Text(text = description, color = TextSub, fontSize = 9.sp)
    }
  }
}
