package com.example.ui.screens
import androidx.compose.ui.geometry.*
import androidx.compose.ui.draw.*
import androidx.compose.ui.graphics.*
import com.example.ui.theme.*

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.CloudSync
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Dns
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.PowerSettingsNew
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.hardware.InternalAiManager
import com.example.hardware.InternalModelInfo

@Composable
fun InternalAiDialog(
  isOpen: Boolean,
  onDismiss: () -> Unit
) {
  if (!isOpen) return

  val context = LocalContext.current
  val models by InternalAiManager.models.collectAsStateWithLifecycle()
  val serverState by InternalAiManager.serverState.collectAsStateWithLifecycle()
  val logs by InternalAiManager.learningLogs.collectAsStateWithLifecycle()

  var selectedTab by remember { mutableIntStateOf(0) }
  var testPrompt by remember { mutableStateOf("") }
  var testOutput by remember { mutableStateOf<String?>(null) }

  Dialog(onDismissRequest = onDismiss) {
    Card(
      modifier = Modifier
        .fillMaxWidth()
        .heightIn(max = 680.dp)
        .border(1.2.dp, MetalSecondary.copy(alpha = 0.85f), RoundedCornerShape(16.dp)),
      shape = RoundedCornerShape(16.dp),
      colors = CardDefaults.cardColors(containerColor = Color(0xF50B0D14))
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
                .size(34.dp)
                .background(MetalSecondary.copy(alpha = 0.18f), CircleShape),
              contentAlignment = Alignment.Center
            ) {
              Icon(Icons.Default.Dns, contentDescription = null, tint = MetalSecondary, modifier = Modifier.size(20.dp))
            }
            Spacer(modifier = Modifier.width(10.dp))
            Column {
              Text("IA INTERNA & SERVIDOR", color = TextMain, fontWeight = FontWeight.ExtraBold, fontSize = 13.sp)
              Text("Gemma • Antigravity • Modo Offline & Híbrido", color = MetalPrimary, fontSize = 9.sp, fontFamily = FontFamily.Monospace)
            }
          }

          IconButton(onClick = onDismiss, modifier = Modifier.size(28.dp)) {
            Icon(Icons.Default.Close, contentDescription = "Cerrar", tint = TextSub, modifier = Modifier.size(18.dp))
          }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Tabs: 0: Modelos & Servidor, 1: Autoaprendizaje & Memoria, 2: Test Local
        TabRow(
          selectedTabIndex = selectedTab,
          containerColor = Color.White,
          contentColor = MetalSecondary,
          indicator = { tabPositions ->
            TabRowDefaults.SecondaryIndicator(
              Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
              color = MetalSecondary,
              height = 2.dp
            )
          }
        ) {
          Tab(
            selected = selectedTab == 0,
            onClick = { selectedTab = 0 },
            text = { Text("Modelos & Servidor", fontSize = 10.sp, fontWeight = FontWeight.Bold) }
          )
          Tab(
            selected = selectedTab == 1,
            onClick = { selectedTab = 1 },
            text = { Text("Autoaprendizaje", fontSize = 10.sp, fontWeight = FontWeight.Bold) }
          )
          Tab(
            selected = selectedTab == 2,
            onClick = { selectedTab = 2 },
            text = { Text("Consola Offline", fontSize = 10.sp, fontWeight = FontWeight.Bold) }
          )
        }

        Spacer(modifier = Modifier.height(10.dp))

        Column(
          modifier = Modifier
            .weight(1f)
            .verticalScroll(rememberScrollState()),
          verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
          when (selectedTab) {
            0 -> {
              // Server status card
              Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(10.dp),
                colors = CardDefaults.cardColors(containerColor = SurfacePearl)
              ) {
                Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                  Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                  ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                      Box(
                        modifier = Modifier
                          .size(8.dp)
                          .background(if (serverState.isServerRunning) ShieldGreen else DangerRed, CircleShape)
                      )
                      Spacer(modifier = Modifier.width(6.dp))
                      Text(
                        text = if (serverState.isServerRunning) "Servidor Activo: ${serverState.serverUrl}" else "Servidor Local Detenido",
                        color = if (serverState.isServerRunning) ShieldGreen else DangerRed,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                      )
                    }

                    Switch(
                      checked = serverState.isServerRunning,
                      onCheckedChange = { InternalAiManager.toggleServer(context) },
                      colors = SwitchDefaults.colors(checkedThumbColor = MetalSecondary, checkedTrackColor = Color.White)
                    )
                  }

                  // Storage quota progress
                  val usedPercent = (serverState.totalCacheUsedMb.toFloat() / serverState.maxStorageCapMb).coerceIn(0f, 1f)
                  Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                  ) {
                    Text("Caché en Memoria (Hasta 10 GB):", color = TextSub, fontSize = 10.sp)
                    Text("${serverState.totalCacheUsedMb} MB / ${serverState.maxStorageCapMb} MB", color = MetalSecondary, fontSize = 10.sp, fontFamily = FontFamily.Monospace)
                  }
                  LinearProgressIndicator(
                    progress = { usedPercent },
                    modifier = Modifier
                      .fillMaxWidth()
                      .height(4.dp)
                      .clip(RoundedCornerShape(2.dp)),
                    color = MetalSecondary,
                    trackColor = MetalPrimary
                  )

                  // Switches for Autostart on boot and Hybrid mode
                  Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                  ) {
                    Text("Arranque automático al encender:", color = TextSub, fontSize = 10.sp)
                    Switch(
                      checked = serverState.isAutoStartOnBootEnabled,
                      onCheckedChange = { InternalAiManager.setAutoBoot(context, it) },
                      colors = SwitchDefaults.colors(checkedThumbColor = MetalPrimary)
                    )
                  }

                  Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                  ) {
                    Text("Modo Híbrido (Local + Nube):", color = TextSub, fontSize = 10.sp)
                    Switch(
                      checked = serverState.isHybridModeEnabled,
                      onCheckedChange = { InternalAiManager.setHybridMode(context, it) },
                      colors = SwitchDefaults.colors(checkedThumbColor = ShieldGreen)
                    )
                  }
                }
              }

              Text("MODELOS DE IA EN DISPOSITIVO:", color = MetalSecondary, fontSize = 11.sp, fontWeight = FontWeight.Bold)

              models.forEach { model ->
                ModelDownloadItem(
                  model = model,
                  isActive = serverState.activeModelId == model.id,
                  onSelect = { InternalAiManager.selectActiveModel(context, model.id) },
                  onDownload = { InternalAiManager.downloadModel(context, model.id) }
                )
              }
            }

            1 -> {
              // Auto-learning & multi-storage memory
              Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(10.dp),
                colors = CardDefaults.cardColors(containerColor = SurfacePearl)
              ) {
                Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                  Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                  ) {
                    Column {
                      Text("Patrones de Autoaprendizaje", color = TextMain, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                      Text("${serverState.autoLearningPatternsCount} patrones aprendidos en memoria", color = MetalPrimary, fontSize = 10.sp, fontFamily = FontFamily.Monospace)
                    }
                    Button(
                      onClick = { InternalAiManager.triggerCloudAndLocalMemorySync() },
                      enabled = !serverState.isSyncingCloudMemory,
                      shape = RoundedCornerShape(8.dp),
                      colors = ButtonDefaults.buttonColors(containerColor = MetalSecondary, contentColor = Color.Black)
                    ) {
                      if (serverState.isSyncingCloudMemory) {
                        CircularProgressIndicator(color = Color.Black, modifier = Modifier.size(12.dp), strokeWidth = 2.dp)
                      } else {
                        Icon(Icons.Default.CloudSync, contentDescription = null, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Sincronizar", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                      }
                    }
                  }

                  HorizontalDivider(color = MetalPrimary, thickness = 0.6.dp)

                  Text("DESTINOS DE MEMORIA ACTIVOS:", color = MetalSecondary, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                  serverState.connectedCloudDestinations.forEach { dest ->
                    Row(verticalAlignment = Alignment.CenterVertically) {
                      Icon(Icons.Default.CheckCircle, contentDescription = null, tint = ShieldGreen, modifier = Modifier.size(14.dp))
                      Spacer(modifier = Modifier.width(6.dp))
                      Text(dest, color = TextSub, fontSize = 11.sp)
                    }
                  }
                }
              }

              Text("REGISTROS DE AUTOAPRENDIZAJE:", color = MetalPrimary, fontSize = 11.sp, fontWeight = FontWeight.Bold)

              Column(
                modifier = Modifier
                  .fillMaxWidth()
                  .background(Color.White, RoundedCornerShape(8.dp))
                  .padding(8.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
              ) {
                logs.take(8).forEach { log ->
                  Text(log, color = TextSub, fontSize = 9.sp, fontFamily = FontFamily.Monospace)
                }
              }
            }

            2 -> {
              // Local offline test console
              Text("PROBAR INFERENCIA 100% OFFLINE:", color = MetalSecondary, fontSize = 11.sp, fontWeight = FontWeight.Bold)
              Text("Escribe cualquier consulta para procesarla directamente con el modelo local en memoria sin internet.", color = TextSub, fontSize = 10.sp)

              OutlinedTextField(
                value = testPrompt,
                onValueChange = { testPrompt = it },
                placeholder = { Text("Ej: Explica cómo funciona el autoaprendizaje...", fontSize = 11.sp, color = TextSub) },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(10.dp),
                colors = OutlinedTextFieldDefaults.colors(
                  focusedBorderColor = MetalPrimary,
                  unfocusedBorderColor = MetalPrimary,
                  focusedContainerColor = Color.White,
                  unfocusedContainerColor = Color.White,
                  focusedTextColor = TextMain,
                  unfocusedTextColor = TextMain
                ),
                maxLines = 3
              )

              Button(
                onClick = {
                  if (testPrompt.isNotBlank()) {
                    testOutput = InternalAiManager.generateOfflineResponse(testPrompt, "Español")
                  }
                },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MetalPrimary, contentColor = Color.Black)
              ) {
                Icon(Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Ejecutar en Servidor Local Offline", fontWeight = FontWeight.Bold, fontSize = 11.sp)
              }

              if (testOutput != null) {
                Card(
                  modifier = Modifier.fillMaxWidth(),
                  shape = RoundedCornerShape(10.dp),
                  colors = CardDefaults.cardColors(containerColor = SurfacePearl)
                ) {
                  Column(modifier = Modifier.padding(10.dp)) {
                    Text("SALIDA DEL SERVIDOR:", color = ShieldGreen, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(testOutput!!, color = TextMain, fontSize = 11.sp, fontFamily = FontFamily.Monospace)
                  }
                }
              }
            }
          }
        }
      }
    }
  }
}

@Composable
private fun ModelDownloadItem(
  model: InternalModelInfo,
  isActive: Boolean,
  onSelect: () -> Unit,
  onDownload: () -> Unit
) {
  Card(
    modifier = Modifier
      .fillMaxWidth()
      .border(
        width = if (isActive) 1.2.dp else 0.6.dp,
        color = if (isActive) MetalSecondary else MetalPrimary,
        shape = RoundedCornerShape(10.dp)
      ),
    shape = RoundedCornerShape(10.dp),
    colors = CardDefaults.cardColors(
      containerColor = if (isActive) MetalSecondary.copy(alpha = 0.08f) else Color.White
    )
  ) {
    Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Column(modifier = Modifier.weight(1f)) {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Text(model.name, color = TextMain, fontWeight = FontWeight.Bold, fontSize = 12.sp)
            if (isActive) {
              Spacer(modifier = Modifier.width(6.dp))
              Box(
                modifier = Modifier
                  .background(MetalSecondary.copy(alpha = 0.2f), RoundedCornerShape(4.dp))
                  .padding(horizontal = 4.dp, vertical = 1.dp)
              ) {
                Text("ACTIVO", color = MetalSecondary, fontSize = 8.sp, fontWeight = FontWeight.Bold)
              }
            }
          }
          Text("${model.provider} • ${model.sizeMb} MB", color = MetalPrimary, fontSize = 10.sp, fontFamily = FontFamily.Monospace)
        }

        if (model.isDownloaded) {
          if (!isActive) {
            OutlinedButton(
              onClick = onSelect,
              shape = RoundedCornerShape(8.dp),
              contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 8.dp, vertical = 2.dp)
            ) {
              Text("Activar", color = MetalSecondary, fontSize = 10.sp)
            }
          }
        } else if (model.isDownloading) {
          CircularProgressIndicator(
            progress = { model.downloadProgress },
            modifier = Modifier.size(24.dp),
            color = MetalSecondary,
            strokeWidth = 2.5.dp
          )
        } else {
          Button(
            onClick = onDownload,
            shape = RoundedCornerShape(8.dp),
            colors = ButtonDefaults.buttonColors(containerColor = MetalSecondary, contentColor = Color.Black),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 8.dp, vertical = 2.dp)
          ) {
            Icon(Icons.Default.Download, contentDescription = null, modifier = Modifier.size(12.dp))
            Spacer(modifier = Modifier.width(4.dp))
            Text("Descargar", fontSize = 10.sp, fontWeight = FontWeight.Bold)
          }
        }
      }

      Text(model.description, color = TextSub, fontSize = 10.sp)

      if (model.isDownloading) {
        LinearProgressIndicator(
          progress = { model.downloadProgress },
          modifier = Modifier
            .fillMaxWidth()
            .height(3.dp),
          color = MetalSecondary,
          trackColor = MetalPrimary
        )
      }
    }
  }
}
