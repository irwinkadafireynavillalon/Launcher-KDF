package com.example.ui.screens

import android.app.WallpaperManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.Shader
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.hardware.ComputeAccelerationManager
import com.example.hardware.ComputeMode
import com.example.hardware.SuperResolutionPreset
import com.example.ui.theme.*
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream

@Composable
fun HardwareAcceleratorDialog(
  isOpen: Boolean,
  onDismiss: () -> Unit
) {
  if (!isOpen) return

  val context = LocalContext.current
  val telemetry by ComputeAccelerationManager.telemetry.collectAsState()
  val scope = rememberCoroutineScope()

  var activeTab by remember { mutableIntStateOf(0) }
  var processedPreviewBitmap by remember { mutableStateOf<Bitmap?>(null) }
  var isProcessingImage by remember { mutableStateOf(false) }

  // Eliza & Heuristic interactive simulator state
  var elizaInput by remember { mutableStateOf("") }
  var elizaResponse by remember {
    mutableStateOf(
      "Escribe o toca una prueba rápida para ver cómo las Heurísticas, Automatización Contextual y el Efecto Eliza responden en <1 ms usando sensores del sistema."
    )
  }
  var elizaRuleUsed by remember {
    mutableStateOf("Motor Heurístico Determinista (0% AI Washing • Latencia <1 ms)")
  }

  LaunchedEffect(Unit) {
    ComputeAccelerationManager.refreshContextualSensors(context)
  }

  val photoPickerLauncher = rememberLauncherForActivityResult(
    ActivityResultContracts.PickVisualMedia()
  ) { uri: Uri? ->
    if (uri != null) {
      scope.launch {
        isProcessingImage = true
        try {
          val inputBitmap = context.contentResolver.openInputStream(uri)?.use { stream ->
            BitmapFactory.decodeStream(stream)
          }
          if (inputBitmap != null) {
            val enhanced = ComputeAccelerationManager.processImageHardwareAccelerated(
              sourceBitmap = inputBitmap,
              applyClarityBoost = true
            )
            processedPreviewBitmap = enhanced
            Toast.makeText(
              context,
              "Imagen escalada con ${telemetry.superResolutionPreset.title}",
              Toast.LENGTH_SHORT
            ).show()
          }
        } catch (e: Exception) {
          Toast.makeText(context, "Error al procesar: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
        } finally {
          isProcessingImage = false
        }
      }
    }
  }

  Dialog(onDismissRequest = onDismiss) {
    Card(
      modifier = Modifier
        .fillMaxWidth()
        .heightIn(max = 670.dp)
        .skeuomorphicCard(borderColor = MetalGold)
        .testTag("hardware_accelerator_dialog"),
      colors = CardDefaults.cardColors(containerColor = Color.Transparent)
    ) {
      Column(
        modifier = Modifier
          .fillMaxWidth()
          .padding(14.dp)
      ) {
        // Compact Dialog Header
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
              modifier = Modifier
                .size(34.dp)
                .clip(CircleShape)
                .background(MetalGold.copy(alpha = 0.18f))
                .border(1.2.dp, MetalGold, CircleShape),
              contentAlignment = Alignment.Center
            ) {
              Icon(
                imageVector = Icons.Default.Memory,
                contentDescription = null,
                tint = MetalGold,
                modifier = Modifier.size(18.dp)
              )
            }
            Spacer(modifier = Modifier.width(8.dp))
            Column {
              Text(
                text = "NPU / GPU SUPERRESOLUCIÓN & HEURÍSTICAS",
                color = TextMain,
                fontSize = 12.sp,
                fontWeight = FontWeight.ExtraBold
              )
              Text(
                text = "AI Upscaling • Fotografía Computacional ISP • Contexto & Anti-AI Washing",
                color = MetalSilver,
                fontSize = 9.sp
              )
            }
          }

          IconButton(
            onClick = onDismiss,
            modifier = Modifier.size(28.dp)
          ) {
            Icon(
              imageVector = Icons.Default.Close,
              contentDescription = "Cerrar",
              tint = TextSub,
              modifier = Modifier.size(18.dp)
            )
          }
        }

        Spacer(modifier = Modifier.height(8.dp))

        TabRow(
          selectedTabIndex = activeTab,
          containerColor = DarkSurface,
          contentColor = MetalGold,
          indicator = { tabPositions ->
            TabRowDefaults.SecondaryIndicator(
              Modifier.tabIndicatorOffset(tabPositions[activeTab]),
              color = MetalGold,
              height = 2.dp
            )
          }
        ) {
          Tab(selected = activeTab == 0, onClick = { activeTab = 0 }) {
            Text("IMAGEN & NPU", modifier = Modifier.padding(vertical = 8.dp), fontSize = 10.sp, fontWeight = FontWeight.Bold)
          }
          Tab(selected = activeTab == 1, onClick = { activeTab = 1 }) {
            Text("HEURÍSTICAS", modifier = Modifier.padding(vertical = 8.dp), fontSize = 10.sp, fontWeight = FontWeight.Bold)
          }
          Tab(selected = activeTab == 2, onClick = { activeTab = 2 }) {
            Text("AUDITORÍA IA", modifier = Modifier.padding(vertical = 8.dp), fontSize = 10.sp, fontWeight = FontWeight.Bold)
          }
        }

        Spacer(modifier = Modifier.height(10.dp))

        Column(
          modifier = Modifier
            .weight(1f)
            .verticalScroll(rememberScrollState()),
          verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
          when (activeTab) {
            0 -> {
              // Hardware Status Summary Card (GPU / NPU / ISP / DSP)
              Column(
                modifier = Modifier
                  .fillMaxWidth()
                  .clip(RoundedCornerShape(10.dp))
                  .background(DarkSurfaceElevated)
                  .border(1.dp, MetalGold.copy(alpha = 0.55f), RoundedCornerShape(10.dp))
                  .padding(10.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
              ) {
                Row(
                  modifier = Modifier.fillMaxWidth(),
                  horizontalArrangement = Arrangement.SpaceBetween
                ) {
                  Text(
                    "GPU: ${telemetry.gpuPipelineDescription}",
                    color = MetalGold,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace
                  )
                  Text(
                    "${telemetry.cpuCores} Núcleos",
                    color = MetalSilver,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold
                  )
                }
                Text(
                  "NPU / ML Nativo: ${telemetry.npuEngineDescription}",
                  color = TextMain,
                  fontSize = 10.sp,
                  fontFamily = FontFamily.Monospace
                )
                Row(
                  modifier = Modifier.fillMaxWidth(),
                  horizontalArrangement = Arrangement.SpaceBetween
                ) {
                  Text("GPU Ops: ${telemetry.operationsDispatchedToGpu}", color = MetalSecondary, fontSize = 9.sp)
                  Text("NPU/ISP Ops: ${telemetry.operationsDispatchedToNpu}", color = MetalGold, fontSize = 9.sp)
                  Text("CPU Ops: ${telemetry.operationsDispatchedToCpu}", color = MetalSilver, fontSize = 9.sp)
                }
              }

              // 1. SUPERRESOLUCIÓN / AI UPSCALING (FSR / Snapdragon GSR / MetalFX style)
              Text(
                "1. SUPERRESOLUCIÓN / AI UPSCALING (GPU & NPU)",
                color = MetalGold,
                fontWeight = FontWeight.ExtraBold,
                fontSize = 10.sp
              )
              Column(verticalArrangement = Arrangement.spacedBy(5.dp)) {
                SuperResolutionPreset.entries.forEach { preset ->
                  val selected = telemetry.superResolutionPreset == preset
                  Row(
                    modifier = Modifier
                      .fillMaxWidth()
                      .clip(RoundedCornerShape(9.dp))
                      .background(if (selected) MetalSecondary.copy(alpha = 0.18f) else DarkSurfaceElevated)
                      .border(
                        width = if (selected) 1.4.dp else 0.7.dp,
                        brush = Brush.horizontalGradient(
                          if (selected) listOf(MetalGold, MetalSecondary, MetalSilver)
                          else listOf(MetalGold.copy(alpha = 0.3f), MetalSilver.copy(alpha = 0.25f))
                        ),
                        shape = RoundedCornerShape(9.dp)
                      )
                      .clickable {
                        ComputeAccelerationManager.setSuperResolutionPreset(preset)
                      }
                      .padding(horizontal = 10.dp, vertical = 7.dp)
                      .testTag("sr_preset_${preset.id}"),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                  ) {
                    Column(modifier = Modifier.weight(1f)) {
                      Text(preset.title, color = TextMain, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                      Text(preset.subtitle, color = TextSub, fontSize = 9.sp)
                    }
                    if (selected) {
                      Icon(Icons.Default.CheckCircle, contentDescription = null, tint = MetalGold, modifier = Modifier.size(16.dp))
                    }
                  }
                }
              }

              // 2. FOTOGRAFÍA COMPUTACIONAL (ISP / NPU)
              Text(
                "2. FOTOGRAFÍA COMPUTACIONAL (PROCESAMIENTO ISP / NPU)",
                color = MetalGold,
                fontWeight = FontWeight.ExtraBold,
                fontSize = 10.sp
              )
              Column(
                modifier = Modifier
                  .fillMaxWidth()
                  .clip(RoundedCornerShape(10.dp))
                  .background(DarkSurfaceElevated)
                  .border(0.8.dp, MetalSilver.copy(alpha = 0.4f), RoundedCornerShape(10.dp))
                  .padding(horizontal = 10.dp, vertical = 6.dp),
                verticalArrangement = Arrangement.spacedBy(2.dp)
              ) {
                Row(
                  modifier = Modifier.fillMaxWidth(),
                  horizontalArrangement = Arrangement.SpaceBetween,
                  verticalAlignment = Alignment.CenterVertically
                ) {
                  Column(modifier = Modifier.weight(1f)) {
                    Text("HDR Computacional (Tone Mapping ISP)", color = TextMain, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    Text("Expande rango dinámico, luces de oro y sombras profundas", color = TextSub, fontSize = 9.sp)
                  }
                  Switch(
                    checked = telemetry.computationalHdrEnabled,
                    onCheckedChange = { ComputeAccelerationManager.setComputationalHdr(it) },
                    colors = SwitchDefaults.colors(checkedThumbColor = MetalGold)
                  )
                }

                Row(
                  modifier = Modifier.fillMaxWidth(),
                  horizontalArrangement = Arrangement.SpaceBetween,
                  verticalAlignment = Alignment.CenterVertically
                ) {
                  Column(modifier = Modifier.weight(1f)) {
                    Text("Reducción de Ruido Neuronal (ISP Denoise)", color = TextMain, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    Text("Filtra grano cromático y suaviza artefactos de compresión", color = TextSub, fontSize = 9.sp)
                  }
                  Switch(
                    checked = telemetry.ispDenoiseEnabled,
                    onCheckedChange = { ComputeAccelerationManager.setIspDenoise(it) },
                    colors = SwitchDefaults.colors(checkedThumbColor = MetalGold)
                  )
                }

                Row(
                  modifier = Modifier.fillMaxWidth(),
                  horizontalArrangement = Arrangement.SpaceBetween,
                  verticalAlignment = Alignment.CenterVertically
                ) {
                  Column(modifier = Modifier.weight(1f)) {
                    Text("Modo Retrato & Profundidad Bokeh (ISP)", color = TextMain, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    Text("Enfoque central nítido con viñeta óptica cinematográfica", color = TextSub, fontSize = 9.sp)
                  }
                  Switch(
                    checked = telemetry.portraitBokehDepthEnabled,
                    onCheckedChange = { ComputeAccelerationManager.setPortraitBokehDepth(it) },
                    colors = SwitchDefaults.colors(checkedThumbColor = MetalGold)
                  )
                }
              }

              // 3. DELEGACIÓN DE HARDWARE (NNAPI / AICore / GPU / CPU)
              Text(
                "3. DELEGACIÓN DE HARDWARE (NNAPI / AICORE / VULKAN)",
                color = MetalGold,
                fontWeight = FontWeight.ExtraBold,
                fontSize = 10.sp
              )
              ComputeMode.entries.forEach { mode ->
                val isSelected = telemetry.mode == mode
                Row(
                  modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(if (isSelected) MetalSecondary.copy(alpha = 0.16f) else DarkSurfaceElevated)
                    .border(
                      width = if (isSelected) 1.2.dp else 0.6.dp,
                      color = if (isSelected) MetalGold else MetalSilver.copy(alpha = 0.3f),
                      shape = RoundedCornerShape(8.dp)
                    )
                    .clickable { ComputeAccelerationManager.setComputeMode(mode) }
                    .padding(horizontal = 10.dp, vertical = 7.dp),
                  verticalAlignment = Alignment.CenterVertically,
                  horizontalArrangement = Arrangement.SpaceBetween
                ) {
                  Column(modifier = Modifier.weight(1f)) {
                    Text(mode.displayName, color = TextMain, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    Text(mode.shortDesc, color = TextSub, fontSize = 9.sp)
                  }
                  if (isSelected) {
                    Icon(Icons.Default.Check, contentDescription = null, tint = MetalGold, modifier = Modifier.size(15.dp))
                  }
                }
              }

              // 4. LABORATORIO DE SUPERRESOLUCIÓN EN VIVO (GALERÍA O CUADRO PROCEDURAL)
              Text(
                "4. PROCESADOR EN VIVO DE FOTOS Y FONDOS",
                color = MetalGold,
                fontWeight = FontWeight.ExtraBold,
                fontSize = 10.sp
              )
              Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
              ) {
                OutlinedButton(
                  onClick = {
                    photoPickerLauncher.launch(
                      PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                    )
                  },
                  modifier = Modifier.weight(1f),
                  contentPadding = PaddingValues(vertical = 8.dp, horizontal = 8.dp)
                ) {
                  Icon(Icons.Default.PhotoCamera, contentDescription = null, tint = MetalGold, modifier = Modifier.size(15.dp))
                  Spacer(modifier = Modifier.width(5.dp))
                  Text("Escalar Foto Galería", color = TextMain, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                }

                OutlinedButton(
                  onClick = {
                    scope.launch {
                      isProcessingImage = true
                      val sample = generateSampleThemeFrameBitmap()
                      val upscaled = ComputeAccelerationManager.processImageHardwareAccelerated(sample, true)
                      processedPreviewBitmap = upscaled
                      isProcessingImage = false
                    }
                  },
                  modifier = Modifier.weight(1f),
                  contentPadding = PaddingValues(vertical = 8.dp, horizontal = 8.dp)
                ) {
                  Icon(Icons.Default.AutoFixHigh, contentDescription = null, tint = MetalSilver, modifier = Modifier.size(15.dp))
                  Spacer(modifier = Modifier.width(5.dp))
                  Text("Generar Frame 4K", color = TextMain, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                }
              }

              if (isProcessingImage) {
                LinearProgressIndicator(
                  modifier = Modifier.fillMaxWidth(),
                  color = MetalGold
                )
              }

              processedPreviewBitmap?.let { bmp ->
                Column(
                  modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(DarkSurfaceElevated)
                    .border(1.dp, MetalGold, RoundedCornerShape(10.dp))
                    .padding(8.dp),
                  verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                  Image(
                    bitmap = bmp.asImageBitmap(),
                    contentDescription = "Resultado Superresolución",
                    modifier = Modifier
                      .fillMaxWidth()
                      .height(120.dp)
                      .clip(RoundedCornerShape(8.dp)),
                    contentScale = ContentScale.Crop
                  )
                  Text(
                    "Resultado: ${telemetry.lastUpscaledDimensions} • Latencia: ${telemetry.lastUpscaleLatencyMs} ms",
                    color = MetalGold,
                    fontSize = 9.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold
                  )
                  Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                  ) {
                    Button(
                      onClick = {
                        try {
                          val bgFile = File(context.filesDir, "upscaled_kdf_wallpaper.jpg")
                          FileOutputStream(bgFile).use { out ->
                            bmp.compress(Bitmap.CompressFormat.JPEG, 96, out)
                          }
                          UiThemeController.updateCustomBackgroundUri(context, bgFile.absolutePath)
                          WallpaperManager.getInstance(context).setBitmap(bmp)
                          Toast.makeText(
                            context,
                            "Imagen con Superresolución + HDR fijada en el sistema",
                            Toast.LENGTH_SHORT
                          ).show()
                        } catch (_: Exception) {
                          Toast.makeText(context, "Aplicada en LAUNCH KDF", Toast.LENGTH_SHORT).show()
                        }
                      },
                      modifier = Modifier.fillMaxWidth(),
                      colors = ButtonDefaults.buttonColors(containerColor = MetalGold, contentColor = Color.Black),
                      contentPadding = PaddingValues(vertical = 6.dp)
                    ) {
                      Icon(Icons.Default.Wallpaper, contentDescription = null, modifier = Modifier.size(14.dp))
                      Spacer(modifier = Modifier.width(6.dp))
                      Text("APLICAR IMAGEN ESCALADA COMO FONDO", fontSize = 10.sp, fontWeight = FontWeight.ExtraBold)
                    }
                  }
                }
              }
            }

            1 -> {
              // TAB 2: AUTOMATIZACIÓN CONTEXTUAL, HEURÍSTICAS & EFECTO ELIZA
              val snap = telemetry.contextualSnapshot
              Text(
                "1. AUTOMATIZACIÓN CONTEXTUAL (SMART FEATURES POR SENSORES)",
                color = MetalGold,
                fontWeight = FontWeight.ExtraBold,
                fontSize = 10.sp
              )
              Column(
                modifier = Modifier
                  .fillMaxWidth()
                  .clip(RoundedCornerShape(10.dp))
                  .background(DarkSurfaceElevated)
                  .border(1.dp, MetalGold.copy(alpha = 0.6f), RoundedCornerShape(10.dp))
                  .padding(10.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
              ) {
                Row(
                  modifier = Modifier.fillMaxWidth(),
                  horizontalArrangement = Arrangement.SpaceBetween,
                  verticalAlignment = Alignment.CenterVertically
                ) {
                  Text(
                    "Auto-Adaptación Inteligente por Sensores",
                    color = TextMain,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                  )
                  Switch(
                    checked = telemetry.contextualAutoAdaptEnabled,
                    onCheckedChange = { ComputeAccelerationManager.setContextualAutoAdapt(it, context) },
                    colors = SwitchDefaults.colors(checkedThumbColor = MetalGold)
                  )
                }

                Row(
                  modifier = Modifier.fillMaxWidth(),
                  horizontalArrangement = Arrangement.SpaceBetween
                ) {
                  Text("Horario: ${snap.timePeriodLabel}", color = MetalSilver, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                  Text("Batería: ${snap.batteryPercent}%", color = MetalGold, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                }
                Row(
                  modifier = Modifier.fillMaxWidth(),
                  horizontalArrangement = Arrangement.SpaceBetween
                ) {
                  Text("Red: ${snap.networkTypeLabel}", color = TextMain, fontSize = 10.sp)
                  Text("RAM Libre: ${snap.freeRamMb} MB", color = MetalSecondary, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                }

                HorizontalDivider(color = MetalSilver.copy(alpha = 0.2f))

                Text(
                  "Regla Activa: ${snap.activeSmartRuleDescription}",
                  color = MetalGold,
                  fontSize = 10.sp,
                  fontWeight = FontWeight.Bold
                )
                Text(
                  snap.recommendedThemeReason,
                  color = TextSub,
                  fontSize = 9.sp
                )

                Row(
                  modifier = Modifier.fillMaxWidth(),
                  horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                  Button(
                    onClick = {
                      val updated = ComputeAccelerationManager.refreshContextualSensors(context)
                      UiThemeController.selectPreset(context, updated.recommendedThemeId)
                      Toast.makeText(
                        context,
                        "Tema '${updated.recommendedThemeId}' aplicado por Regla Contextual",
                        Toast.LENGTH_SHORT
                      ).show()
                    },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(containerColor = MetalGold, contentColor = Color.Black),
                    contentPadding = PaddingValues(vertical = 6.dp, horizontal = 8.dp)
                  ) {
                    Icon(Icons.Default.AutoAwesome, contentDescription = null, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("APLICAR TEMA SUGERIDO (${snap.recommendedThemeId})", fontSize = 9.sp, fontWeight = FontWeight.ExtraBold)
                  }
                }
              }

              // 2. LABORATORIO DE HEURÍSTICAS & EFECTO ELIZA
              Text(
                "2. MOTOR DE HEURÍSTICAS Y EFECTO ELIZA (<1 MS SIN RED NEURONAL)",
                color = MetalGold,
                fontWeight = FontWeight.ExtraBold,
                fontSize = 10.sp
              )
              Text(
                "Prueba cómo las reglas condicionales y patrones reflexivos (Efecto Eliza) generan respuestas instantáneas adaptadas al estado del teléfono, mostrando la regla exacta ejecutada:",
                color = TextSub,
                fontSize = 9.sp
              )

              // Quick preset chips for Eliza / Heuristics
              Row(
                modifier = Modifier
                  .fillMaxWidth()
                  .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
              ) {
                listOf(
                  "¿Cómo está mi batería?",
                  "Quiero mejorar la nitidez",
                  "Me siento con energía hoy",
                  "¿Qué tema recomiendas?"
                ).forEach { sampleQuery ->
                  Box(
                    modifier = Modifier
                      .clip(RoundedCornerShape(50))
                      .background(DarkSurfaceElevated)
                      .border(0.8.dp, MetalGold.copy(alpha = 0.6f), RoundedCornerShape(50))
                      .clickable {
                        elizaInput = sampleQuery
                        val (resp, rule) = ComputeAccelerationManager.evaluateElizaAndHeuristicQuery(sampleQuery, context)
                        elizaResponse = resp
                        elizaRuleUsed = rule
                      }
                      .padding(horizontal = 9.dp, vertical = 4.dp)
                  ) {
                    Text(sampleQuery, color = MetalGold, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                  }
                }
              }

              Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
              ) {
                OutlinedTextField(
                  value = elizaInput,
                  onValueChange = { elizaInput = it },
                  placeholder = { Text("Escribeej. 'Necesito más velocidad'...", fontSize = 10.sp) },
                  singleLine = true,
                  modifier = Modifier.weight(1f),
                  textStyle = androidx.compose.ui.text.TextStyle(color = TextMain, fontSize = 11.sp)
                )
                IconButton(
                  onClick = {
                    if (elizaInput.isNotBlank()) {
                      val (resp, rule) = ComputeAccelerationManager.evaluateElizaAndHeuristicQuery(elizaInput, context)
                      elizaResponse = resp
                      elizaRuleUsed = rule
                    }
                  },
                  modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(MetalGold)
                ) {
                  Icon(Icons.AutoMirrored.Filled.Send, contentDescription = "Evaluar Regla", tint = Color.Black)
                }
              }

              Column(
                modifier = Modifier
                  .fillMaxWidth()
                  .clip(RoundedCornerShape(10.dp))
                  .background(DarkSurfaceElevated)
                  .border(1.dp, MetalSilver.copy(alpha = 0.5f), RoundedCornerShape(10.dp))
                  .padding(10.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
              ) {
                Text(
                  elizaResponse,
                  color = TextMain,
                  fontSize = 11.sp,
                  fontWeight = FontWeight.Medium
                )
                Text(
                  "🔍 Transparencia: $elizaRuleUsed",
                  color = MetalGold,
                  fontSize = 9.sp,
                  fontFamily = FontFamily.Monospace,
                  fontWeight = FontWeight.Bold
                )
              }
            }

            2 -> {
              // TAB 3: AUDITORÍA ANTI-AI WASHING (TRANSPARENCIA TÉCNICA)
              Text(
                "AUDITORÍA DE TRANSPARENCIA: HARDWARE vs. IA REAL vs. HEURÍSTICAS",
                color = MetalGold,
                fontWeight = FontWeight.ExtraBold,
                fontSize = 10.sp
              )
              Text(
                "A diferencia de apps con 'AI Washing' (que etiquetan simples reglas if/else como IA avanzada), LAUNCH KDF declara con total transparencia qué motor ejecuta cada función:",
                color = TextSub,
                fontSize = 9.sp
              )

              val auditItems = remember { ComputeAccelerationManager.getTransparencyAuditList() }
              auditItems.forEach { item ->
                Column(
                  modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(DarkSurfaceElevated)
                    .border(
                      width = 1.dp,
                      brush = Brush.horizontalGradient(listOf(MetalGold.copy(alpha = 0.55f), MetalSilver.copy(alpha = 0.45f))),
                      shape = RoundedCornerShape(10.dp)
                    )
                    .padding(10.dp),
                  verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                  Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                  ) {
                    Text(
                      item.featureName,
                      color = TextMain,
                      fontSize = 11.sp,
                      fontWeight = FontWeight.ExtraBold,
                      modifier = Modifier.weight(1f)
                    )
                    Box(
                      modifier = Modifier
                        .clip(RoundedCornerShape(50))
                        .background(MetalGold.copy(alpha = 0.18f))
                        .border(0.8.dp, MetalGold, RoundedCornerShape(50))
                        .padding(horizontal = 7.dp, vertical = 2.dp)
                    ) {
                      Text(
                        item.categoryBadge,
                        color = MetalGold,
                        fontSize = 8.sp,
                        fontWeight = FontWeight.ExtraBold
                      )
                    }
                  }
                  Text(
                    "Motor / API: ${item.hardwareOrApiUsed}",
                    color = MetalSilver,
                    fontSize = 9.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold
                  )
                  Text(
                    item.technicalExplanation,
                    color = TextSub,
                    fontSize = 9.sp
                  )
                }
              }
            }
          }
        }

        Spacer(modifier = Modifier.height(10.dp))

        Button(
          onClick = onDismiss,
          modifier = Modifier
            .fillMaxWidth()
            .jewelButton()
            .height(40.dp),
          colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent, contentColor = TextMain),
          contentPadding = PaddingValues(vertical = 4.dp)
        ) {
          Text("APLICAR Y VOLVER AL LAUNCHER", fontWeight = FontWeight.ExtraBold, fontSize = 12.sp)
        }
      }
    }
  }
}

private fun generateSampleThemeFrameBitmap(): Bitmap {
  val w = 720
  val h = 420
  val bmp = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)
  val canvas = Canvas(bmp)
  val bgPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
    shader = LinearGradient(
      0f, 0f, w.toFloat(), h.toFloat(),
      intArrayOf(
        SurfacePearl.toArgb(),
        DarkSurface.toArgb(),
        MetalSecondary.toArgb(),
        MetalGold.toArgb()
      ),
      floatArrayOf(0f, 0.45f, 0.8f, 1f),
      Shader.TileMode.CLAMP
    )
  }
  canvas.drawRect(0f, 0f, w.toFloat(), h.toFloat(), bgPaint)

  val ringPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
    style = Paint.Style.STROKE
    strokeWidth = 4f
    color = MetalGold.toArgb()
  }
  val silverPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
    style = Paint.Style.STROKE
    strokeWidth = 2.5f
    color = MetalSilver.toArgb()
  }
  for (r in 1..6) {
    canvas.drawCircle(w * 0.5f, h * 0.5f, r * 32f, if (r % 2 == 0) ringPaint else silverPaint)
  }
  return bmp
}
