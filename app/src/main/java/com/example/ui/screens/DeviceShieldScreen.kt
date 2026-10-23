package com.example.ui.screens
import androidx.compose.ui.geometry.*
import androidx.compose.ui.draw.*
import androidx.compose.ui.graphics.*
import com.example.ui.theme.*

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Android
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.AutoFixHigh
import androidx.compose.material.icons.filled.BatteryChargingFull
import androidx.compose.material.icons.filled.BookmarkAdd
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.GpsFixed
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.SystemUpdate
import androidx.compose.material.icons.filled.Vibration
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.example.data.local.SystemEvolutionLog
import com.example.hardware.ApkShareHelper
import com.example.hardware.AutonomousEvolutionEngine
import com.example.hardware.DeviceShieldManager
import androidx.compose.ui.geometry.Offset
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun DeviceShieldScreen(
  deviceShieldManager: DeviceShieldManager,
  autonomousEngine: AutonomousEvolutionEngine,
  evolutionLogs: List<SystemEvolutionLog>,
  selectedLanguage: String,
  isAnalyzingImage: Boolean,
  imageAnalysisResult: String?,
  onAnalyzeImage: (Bitmap, String, Boolean) -> Unit,
  onRunAutoHeal: () -> Unit,
  onRunAutoImprove: () -> Unit,
  onRunAutoUpdateVersion: () -> Unit,
  onRunFullAutonomousCycle: () -> Unit,
  onClearEvolutionLogs: () -> Unit,
  onSaveCheckpoint: (String, String) -> Unit,
  onSpeak: (String) -> Unit,
  onOpenThemeStudio: () -> Unit = {}
) {
  val context = LocalContext.current
  val scope = rememberCoroutineScope()
  val permissionsList by deviceShieldManager.permissionsState.collectAsState()
  val telemetry by deviceShieldManager.telemetry.collectAsState()
  val engineState by autonomousEngine.state.collectAsState()
  val apkMetadata = remember(engineState.currentVersionName) { ApkShareHelper.getApkMetadata(context) }

  var capturedBitmap by remember { mutableStateOf<Bitmap?>(null) }
  var visionPrompt by remember { mutableStateOf("") }
  var isSharingApk by remember { mutableStateOf(false) }

  val saveApkLauncher = rememberLauncherForActivityResult(
    ActivityResultContracts.CreateDocument("application/vnd.android.package-archive")
  ) { uri: Uri? ->
    if (uri != null) {
      scope.launch {
        isSharingApk = true
        ApkShareHelper.exportApkToUri(context, uri)
        isSharingApk = false
      }
    }
  }

  LaunchedEffect(Unit) {
    deviceShieldManager.refreshPermissionsStatus()
    deviceShieldManager.refreshSystemAndNetworkTelemetry()
    autonomousEngine.refreshMemoryMetrics()
  }

  // Multi-permission launcher
  val multiPermissionLauncher = rememberLauncherForActivityResult(
    ActivityResultContracts.RequestMultiplePermissions()
  ) {
    deviceShieldManager.refreshPermissionsStatus()
    deviceShieldManager.refreshSystemAndNetworkTelemetry()
  }

  // Single permission launcher
  val singlePermissionLauncher = rememberLauncherForActivityResult(
    ActivityResultContracts.RequestPermission()
  ) {
    deviceShieldManager.refreshPermissionsStatus()
    deviceShieldManager.refreshSystemAndNetworkTelemetry()
  }

  // Camera preview capture launcher
  val takePictureLauncher = rememberLauncherForActivityResult(
    ActivityResultContracts.TakePicturePreview()
  ) { bitmap ->
    if (bitmap != null) {
      capturedBitmap = bitmap
      onAnalyzeImage(bitmap, visionPrompt, false)
    }
  }

  // Camera permission specifically for launching camera
  val cameraPermissionForCaptureLauncher = rememberLauncherForActivityResult(
    ActivityResultContracts.RequestPermission()
  ) { granted ->
    deviceShieldManager.refreshPermissionsStatus()
    if (granted) {
      takePictureLauncher.launch(null)
    }
  }

  // Zero-permission Android Photo Picker launcher
  val pickMediaLauncher = rememberLauncherForActivityResult(
    ActivityResultContracts.PickVisualMedia()
  ) { uri: Uri? ->
    if (uri != null) {
      try {
        val bmp = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
          val source = ImageDecoder.createSource(context.contentResolver, uri)
          ImageDecoder.decodeBitmap(source) { decoder, _, _ ->
            decoder.isMutableRequired = true
          }
        } else {
          @Suppress("DEPRECATION")
          MediaStore.Images.Media.getBitmap(context.contentResolver, uri)
        }
        capturedBitmap = bmp
        onAnalyzeImage(bmp, visionPrompt, false)
      } catch (_: Exception) {
      }
    }
  }

  val grantedCount = permissionsList.count { it.isGranted }
  val totalPermissions = permissionsList.size

  LazyColumn(
    modifier = Modifier
      .fillMaxSize()
      .damaskBackground()
      .testTag("device_shield_list"),
    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 10.dp),
    verticalArrangement = Arrangement.spacedBy(10.dp)
  ) {
    // 1. Compact Header Card
    item {
      Card(
        modifier = Modifier
          .fillMaxWidth()
          .skeuomorphicCard(borderColor = MetalSecondary),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent)
      ) {
        Column(modifier = Modifier.padding(12.dp)) {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
              Icon(
                imageVector = Icons.Default.Shield,
                contentDescription = null,
                tint = MetalSecondary,
                modifier = Modifier.size(20.dp)
              )
              Spacer(modifier = Modifier.width(8.dp))
              Column {
                Text(
                  text = "Escudo Hyper Lux v${engineState.currentVersionName}",
                  color = TextMain,
                  fontSize = 14.sp,
                  fontWeight = FontWeight.Bold
                )
                Text(
                  text = "Motor Autónomo • APK • Permisos • Sensores",
                  color = MetalSecondary,
                  fontSize = 10.sp,
                  fontFamily = FontFamily.Monospace
                )
              }
            }

            IconButton(
              onClick = {
                deviceShieldManager.refreshPermissionsStatus()
                deviceShieldManager.refreshSystemAndNetworkTelemetry()
                autonomousEngine.refreshMemoryMetrics()
                deviceShieldManager.triggerDiscreetVibration()
              },
              modifier = Modifier
                .size(32.dp)
                .testTag("refresh_telemetry_button")
            ) {
              Icon(
                imageVector = Icons.Default.Refresh,
                contentDescription = "Actualizar Telemetría",
                tint = MetalPrimary,
                modifier = Modifier.size(18.dp)
              )
            }
          }
        }
      }
    }

    // 1B. INTERFACE & THEME OPTIONS QUICK STUDIO CARD
    item {
      Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = SurfacePearl),
        border = androidx.compose.foundation.BorderStroke(1.dp, MetalSecondary.copy(alpha = 0.65f))
      ) {
        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
              Icon(
                imageVector = Icons.Default.Palette,
                contentDescription = null,
                tint = MetalSecondary,
                modifier = Modifier.size(19.dp)
              )
              Spacer(modifier = Modifier.width(6.dp))
              Column {
                Text(
                  text = "Opciones de Interfaz y Temas",
                  color = TextMain,
                  fontSize = 13.sp,
                  fontWeight = FontWeight.Bold
                )
                Text(
                  text = "6 Temas de Lujo • Transparencia Cristal • Escudo A&Ω",
                  color = MetalPrimary,
                  fontSize = 9.sp,
                  fontFamily = FontFamily.Monospace
                )
              }
            }

            Button(
              onClick = onOpenThemeStudio,
              shape = RoundedCornerShape(8.dp),
              contentPadding = PaddingValues(horizontal = 10.dp, vertical = 5.dp),
              colors = ButtonDefaults.buttonColors(containerColor = MetalSecondary, contentColor = Color.Black),
              modifier = Modifier
                .heightIn(min = 30.dp, max = 34.dp)
                .testTag("open_theme_studio_shield_button")
            ) {
              Icon(Icons.Default.Palette, contentDescription = null, modifier = Modifier.size(13.dp))
              Spacer(modifier = Modifier.width(4.dp))
              Text("Personalizar", fontSize = 10.sp, fontWeight = FontWeight.Bold)
            }
          }

          // Quick 6-Theme Horizontal / Grid Selector
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
          ) {
            HYPER_LUX_THEME_PRESETS.take(3).forEach { preset ->
              val isSelected = UiThemeController.selectedPresetId == preset.id
              OutlinedButton(
                onClick = { UiThemeController.selectPreset(context, preset.id) },
                shape = RoundedCornerShape(8.dp),
                contentPadding = PaddingValues(horizontal = 6.dp, vertical = 4.dp),
                border = androidx.compose.foundation.BorderStroke(
                  if (isSelected) 1.5.dp else 1.dp,
                  if (isSelected) preset.primaryAccent else MetalPrimary
                ),
                modifier = Modifier
                  .weight(1f)
                  .heightIn(min = 32.dp, max = 36.dp)
              ) {
                Box(
                  modifier = Modifier
                    .size(10.dp)
                    .background(preset.primaryAccent, CircleShape)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                  text = preset.name.substringBefore(" &"),
                  color = if (isSelected) preset.primaryAccent else TextMain,
                  fontSize = 9.sp,
                  fontWeight = FontWeight.Bold,
                  maxLines = 1
                )
              }
            }
          }

          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
          ) {
            HYPER_LUX_THEME_PRESETS.drop(3).forEach { preset ->
              val isSelected = UiThemeController.selectedPresetId == preset.id
              OutlinedButton(
                onClick = { UiThemeController.selectPreset(context, preset.id) },
                shape = RoundedCornerShape(8.dp),
                contentPadding = PaddingValues(horizontal = 6.dp, vertical = 4.dp),
                border = androidx.compose.foundation.BorderStroke(
                  if (isSelected) 1.5.dp else 1.dp,
                  if (isSelected) preset.primaryAccent else MetalPrimary
                ),
                modifier = Modifier
                  .weight(1f)
                  .heightIn(min = 32.dp, max = 36.dp)
              ) {
                Box(
                  modifier = Modifier
                    .size(10.dp)
                    .background(preset.primaryAccent, CircleShape)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                  text = preset.name.substringBefore(" &"),
                  color = if (isSelected) preset.primaryAccent else TextMain,
                  fontSize = 9.sp,
                  fontWeight = FontWeight.Bold,
                  maxLines = 1
                )
              }
            }
          }
        }
      }
    }

    // 2. AUTONOMOUS EVOLUTION ENGINE CARD
    item {
      Card(
        modifier = Modifier
          .fillMaxWidth()
          .skeuomorphicCard(borderColor = MetalPrimary),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent)
      ) {
        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
              Icon(
                imageVector = Icons.Default.AutoFixHigh,
                contentDescription = null,
                tint = MetalSecondary,
                modifier = Modifier.size(22.dp)
              )
              Spacer(modifier = Modifier.width(8.dp))
              Column {
                Text(
                  text = "MOTOR AUTÓNOMO SUPREMO",
                  color = TextMain,
                  fontSize = 13.sp,
                  fontWeight = FontWeight.ExtraBold,
                  letterSpacing = 0.5.sp
                )
                Text(
                  text = "Evolución • Parches • Optimización",
                  color = TextSub,
                  fontSize = 10.sp,
                  fontFamily = FontFamily.Monospace
                )
              }
            }

            Box(
              modifier = Modifier
                .background(MetalSecondary.copy(alpha = 0.1f), RoundedCornerShape(6.dp))
                .border(0.6.dp, MetalSecondary, RoundedCornerShape(6.dp))
                .padding(horizontal = 8.dp, vertical = 3.dp)
            ) {
              Text(
                text = "v${engineState.currentVersionName}",
                color = MetalSecondary,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold
              )
            }
          }

          // Master 1-Tap Full Cycle Button
          Button(
            onClick = onRunFullAutonomousCycle,
            enabled = !engineState.isRunningEngine,
            modifier = Modifier
              .fillMaxWidth()
              .jewelButton()
              .testTag("run_full_autonomous_button"),
            colors = ButtonDefaults.buttonColors(
              containerColor = Color.Transparent,
              contentColor = TextMain
            )
          ) {
            if (engineState.isRunningEngine) {
              CircularProgressIndicator(color = MetalSecondary, modifier = Modifier.size(16.dp), strokeWidth = 2.dp)
              Spacer(modifier = Modifier.width(8.dp))
              Text(
                text = engineState.currentOperationLabel ?: "EVOLUCIONANDO...",
                fontSize = 11.sp,
                fontWeight = FontWeight.ExtraBold,
                color = MetalSecondary
              )
            } else {
              Icon(Icons.Default.AutoFixHigh, contentDescription = null, modifier = Modifier.size(18.dp), tint = MetalSecondary)
              Spacer(modifier = Modifier.width(8.dp))
              Text("EJECUTAR CICLO DE EVOLUCIÓN", fontSize = 12.sp, fontWeight = FontWeight.ExtraBold)
            }
          }

          // Direct Self-Installer APK Launch Button
          OutlinedButton(
            onClick = {
              scope.launch {
                autonomousEngine.launchSelfUpdateApkInstaller()
              }
            },
            enabled = !engineState.isRunningEngine,
            shape = RoundedCornerShape(8.dp),
            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 5.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, MetalPrimary.copy(alpha = 0.7f)),
            modifier = Modifier
              .fillMaxWidth()
              .heightIn(min = 32.dp, max = 35.dp)
              .testTag("install_updated_apk_button")
          ) {
            Icon(Icons.Default.SystemUpdate, contentDescription = null, tint = MetalPrimary, modifier = Modifier.size(14.dp))
            Spacer(modifier = Modifier.width(6.dp))
            Text(
              text = "Instalar Paquete APK Auto-Actualizado (v${engineState.currentVersionName})",
              color = MetalPrimary,
              fontSize = 10.sp,
              fontWeight = FontWeight.Bold
            )
          }

          // Active Optimization Directive
          Text(
            text = "Directiva activa: ${engineState.activeOptimizationDirective}",
            color = TextSub,
            fontSize = 10.sp,
            fontFamily = FontFamily.Monospace
          )

          // Latest Engine Report
          if (engineState.latestReport != null) {
            Card(
              modifier = Modifier.fillMaxWidth(),
              colors = CardDefaults.cardColors(containerColor = SurfacePearl),
              shape = RoundedCornerShape(8.dp),
              border = androidx.compose.foundation.BorderStroke(1.dp, MetalSecondary.copy(alpha = 0.4f))
            ) {
              Column(modifier = Modifier.padding(10.dp)) {
                Row(
                  modifier = Modifier.fillMaxWidth(),
                  horizontalArrangement = Arrangement.SpaceBetween,
                  verticalAlignment = Alignment.CenterVertically
                ) {
                  Text(
                    text = "REPORTE DEL MOTOR AUTÓNOMO (v${engineState.currentVersionName}):",
                    color = MetalSecondary,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace
                  )
                  IconButton(
                    onClick = { onSpeak(engineState.latestReport!!) },
                    modifier = Modifier.size(24.dp)
                  ) {
                    Icon(Icons.Default.VolumeUp, contentDescription = "Escuchar", tint = MetalPrimary, modifier = Modifier.size(14.dp))
                  }
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                  text = engineState.latestReport!!,
                  color = TextMain,
                  fontSize = 11.sp,
                  lineHeight = 15.sp
                )
              }
            }
          }

          // Evolution History Log
          if (evolutionLogs.isNotEmpty()) {
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically
            ) {
              Text(
                text = "Historial de Auto-Evolución (${evolutionLogs.size}):",
                color = MetalPrimary,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace
              )
              IconButton(
                onClick = onClearEvolutionLogs,
                modifier = Modifier.size(24.dp)
              ) {
                Icon(Icons.Default.DeleteSweep, contentDescription = "Limpiar Historial", tint = TextSub, modifier = Modifier.size(14.dp))
              }
            }

            evolutionLogs.take(4).forEach { log ->
              Row(
                modifier = Modifier
                  .fillMaxWidth()
                  .background(SurfacePearl, RoundedCornerShape(6.dp))
                  .padding(horizontal = 8.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
              ) {
                Column(modifier = Modifier.weight(1f)) {
                  Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(log.title, color = TextMain, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                      text = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date(log.timestamp)),
                      color = TextSub,
                      fontSize = 9.sp
                    )
                  }
                  Text(
                    text = log.details.lines().firstOrNull()?.take(80) ?: "",
                    color = TextSub,
                    fontSize = 9.sp,
                    maxLines = 1
                  )
                }
                Box(
                  modifier = Modifier
                    .background(MetalSecondary.copy(alpha = 0.15f), RoundedCornerShape(4.dp))
                    .padding(horizontal = 5.dp, vertical = 2.dp)
                ) {
                  Text(
                    text = "${log.status} • ${log.versionTag}",
                    color = MetalSecondary,
                    fontSize = 8.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace
                  )
                }
              }
            }
          }
        }
      }
    }

    // 3. Share & Export Application in APK Format Card
    item {
      Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = SurfacePearl),
        border = androidx.compose.foundation.BorderStroke(1.dp, MetalSecondary.copy(alpha = 0.5f))
      ) {
        Column(modifier = Modifier.padding(12.dp)) {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
              Icon(
                imageVector = Icons.Default.Android,
                contentDescription = null,
                tint = MetalSecondary,
                modifier = Modifier.size(19.dp)
              )
              Spacer(modifier = Modifier.width(6.dp))
              Column {
                Text(
                  text = "Compartir Aplicación en Formato APK",
                  color = TextMain,
                  fontSize = 13.sp,
                  fontWeight = FontWeight.Bold
                )
                Text(
                  text = "Hyper_Lux_v${engineState.currentVersionName}.apk • ${apkMetadata.sizeMegabytes} • Listo para compartir",
                  color = MetalPrimary,
                  fontSize = 9.sp,
                  fontFamily = FontFamily.Monospace
                )
              }
            }
          }

          Spacer(modifier = Modifier.height(6.dp))
          Text(
            text = "Envía el instalador .APK oficial de Hyper Lux por Bluetooth, Quick Share, WhatsApp, Telegram o guárdalo en tus archivos.",
            color = TextSub,
            fontSize = 11.sp,
            lineHeight = 14.sp
          )

          Spacer(modifier = Modifier.height(8.dp))
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
          ) {
            Button(
              onClick = {
                scope.launch {
                  isSharingApk = true
                  ApkShareHelper.shareApkFile(context)
                  isSharingApk = false
                }
              },
              enabled = !isSharingApk,
              shape = RoundedCornerShape(8.dp),
              contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
              colors = ButtonDefaults.buttonColors(containerColor = MetalSecondary, contentColor = Color.Black),
              modifier = Modifier
                .weight(1f)
                .heightIn(min = 34.dp, max = 38.dp)
                .testTag("shield_share_apk_button")
            ) {
              if (isSharingApk) {
                CircularProgressIndicator(color = Color.Black, modifier = Modifier.size(14.dp), strokeWidth = 2.dp)
              } else {
                Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(14.dp))
                Spacer(modifier = Modifier.width(5.dp))
                Text("Compartir .APK", fontSize = 11.sp, fontWeight = FontWeight.Bold)
              }
            }

            OutlinedButton(
              onClick = {
                saveApkLauncher.launch("Hyper_Lux_v${engineState.currentVersionName}.apk")
              },
              enabled = !isSharingApk,
              shape = RoundedCornerShape(8.dp),
              contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
              border = androidx.compose.foundation.BorderStroke(1.dp, MetalPrimary),
              modifier = Modifier
                .weight(1f)
                .heightIn(min = 34.dp, max = 38.dp)
                .testTag("shield_save_apk_button")
            ) {
              Icon(Icons.Default.Download, contentDescription = null, tint = MetalPrimary, modifier = Modifier.size(14.dp))
              Spacer(modifier = Modifier.width(5.dp))
              Text("Guardar .APK", color = MetalPrimary, fontSize = 11.sp, fontWeight = FontWeight.Bold)
            }
          }
        }
      }
    }

    // 4. Android Permissions Hub Section
    item {
      Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = SurfacePearl),
        border = androidx.compose.foundation.BorderStroke(1.dp, MetalPrimary)
      ) {
        Column(modifier = Modifier.padding(12.dp)) {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Column(modifier = Modifier.weight(1f)) {
              Text(
                text = "Permisos del Sistema Android ($grantedCount/$totalPermissions)",
                color = TextMain,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold
              )
              Text(
                text = "Activa todos los permisos para voz, cámara, ubicación y alertas.",
                color = TextSub,
                fontSize = 11.sp
              )
            }
            Spacer(modifier = Modifier.width(8.dp))
            Button(
              onClick = {
                multiPermissionLauncher.launch(
                  deviceShieldManager.getRequiredRuntimePermissions().toTypedArray()
                )
              },
              shape = RoundedCornerShape(8.dp),
              contentPadding = PaddingValues(horizontal = 10.dp, vertical = 5.dp),
              colors = ButtonDefaults.buttonColors(
                containerColor = MetalSecondary,
                contentColor = Color.Black
              ),
              modifier = Modifier
                .heightIn(min = 32.dp, max = 36.dp)
                .testTag("grant_all_permissions_button")
            ) {
              Text("Activar Todos", fontSize = 11.sp, fontWeight = FontWeight.Bold)
            }
          }

          Spacer(modifier = Modifier.height(8.dp))

          permissionsList.forEach { perm ->
            Row(
              modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp)
                .background(Color.White, RoundedCornerShape(8.dp))
                .padding(horizontal = 10.dp, vertical = 8.dp),
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.SpaceBetween
            ) {
              Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                  Box(
                    modifier = Modifier
                      .size(7.dp)
                      .background(if (perm.isGranted) MetalSecondary else MetalPrimary, CircleShape)
                  )
                  Spacer(modifier = Modifier.width(6.dp))
                  Text(
                    text = perm.title,
                    color = TextMain,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold
                  )
                  Spacer(modifier = Modifier.width(6.dp))
                  Text(
                    text = "[${perm.ruleReference}]",
                    color = MetalSecondary,
                    fontSize = 9.sp,
                    fontFamily = FontFamily.Monospace
                  )
                }
                Text(
                  text = perm.description,
                  color = TextSub,
                  fontSize = 10.sp,
                  lineHeight = 13.sp
                )
              }

              Spacer(modifier = Modifier.width(8.dp))

              if (perm.isGranted) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                  Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = "Concedido",
                    tint = MetalSecondary,
                    modifier = Modifier.size(15.dp)
                  )
                  Spacer(modifier = Modifier.width(3.dp))
                  Text(
                    text = "ACTIVO",
                    color = MetalSecondary,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace
                  )
                }
              } else {
                OutlinedButton(
                  onClick = { singlePermissionLauncher.launch(perm.permission) },
                  shape = RoundedCornerShape(6.dp),
                  contentPadding = PaddingValues(horizontal = 8.dp, vertical = 3.dp),
                  border = androidx.compose.foundation.BorderStroke(1.dp, MetalPrimary),
                  modifier = Modifier.heightIn(min = 28.dp, max = 32.dp)
                ) {
                  Text("Permitir", color = MetalPrimary, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                }
              }
            }
          }

          Spacer(modifier = Modifier.height(6.dp))
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End
          ) {
            OutlinedButton(
              onClick = {
                try {
                  val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                    data = Uri.fromParts("package", context.packageName, null)
                  }
                  context.startActivity(intent)
                } catch (_: Exception) {
                }
              },
              shape = RoundedCornerShape(6.dp),
              contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
              border = androidx.compose.foundation.BorderStroke(1.dp, MetalPrimary),
              modifier = Modifier.heightIn(min = 28.dp, max = 32.dp)
            ) {
              Icon(Icons.Default.Settings, contentDescription = null, tint = TextSub, modifier = Modifier.size(13.dp))
              Spacer(modifier = Modifier.width(4.dp))
              Text("Ajustes del Sistema Android", color = TextSub, fontSize = 10.sp)
            }
          }
        }
      }
    }

    // 5. Multimodal Visual Scanner (Camera + Photo Picker)
    item {
      Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = SurfacePearl),
        border = androidx.compose.foundation.BorderStroke(1.dp, MetalSecondary.copy(alpha = 0.4f))
      ) {
        Column(modifier = Modifier.padding(12.dp)) {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
              Icon(Icons.Default.CameraAlt, contentDescription = null, tint = MetalSecondary, modifier = Modifier.size(18.dp))
              Spacer(modifier = Modifier.width(6.dp))
              Text(
                text = "Escáner Visual IA (Traducción y Auditoría)",
                color = TextMain,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold
              )
            }
            Text(
              text = selectedLanguage.uppercase(),
              color = MetalSecondary,
              fontSize = 10.sp,
              fontFamily = FontFamily.Monospace,
              fontWeight = FontWeight.Bold
            )
          }

          Spacer(modifier = Modifier.height(4.dp))
          Text(
            text = "Toma una foto o elige una imagen para traducir menús/documentos a $selectedLanguage o detectar cláusulas engañosas (Reglas 1 y 6).",
            color = TextSub,
            fontSize = 11.sp,
            lineHeight = 14.sp
          )

          Spacer(modifier = Modifier.height(8.dp))
          OutlinedTextField(
            value = visionPrompt,
            onValueChange = { visionPrompt = it },
            placeholder = {
              Text(
                "Instrucción opcional (ej. Traduce este contrato y busca riesgos)...",
                fontSize = 11.sp,
                color = TextSub
              )
            },
            modifier = Modifier
              .fillMaxWidth()
              .testTag("vision_prompt_input"),
            shape = RoundedCornerShape(8.dp),
            singleLine = true,
            colors = OutlinedTextFieldDefaults.colors(
              focusedBorderColor = MetalSecondary,
              unfocusedBorderColor = MetalPrimary,
              focusedContainerColor = SurfacePearl,
              unfocusedContainerColor = SurfacePearl,
              focusedTextColor = TextMain,
              unfocusedTextColor = TextMain
            )
          )

          Spacer(modifier = Modifier.height(8.dp))
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
          ) {
            Button(
              onClick = {
                val hasCam = ContextCompat.checkSelfPermission(
                  context,
                  Manifest.permission.CAMERA
                ) == PackageManager.PERMISSION_GRANTED
                if (hasCam) {
                  takePictureLauncher.launch(null)
                } else {
                  cameraPermissionForCaptureLauncher.launch(Manifest.permission.CAMERA)
                }
              },
              enabled = !isAnalyzingImage,
              shape = RoundedCornerShape(8.dp),
              contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
              colors = ButtonDefaults.buttonColors(containerColor = MetalSecondary, contentColor = Color.Black),
              modifier = Modifier
                .weight(1f)
                .heightIn(min = 34.dp, max = 38.dp)
                .testTag("capture_camera_button")
            ) {
              Icon(Icons.Default.CameraAlt, contentDescription = null, modifier = Modifier.size(14.dp))
              Spacer(modifier = Modifier.width(4.dp))
              Text("Usar Cámara", fontSize = 11.sp, fontWeight = FontWeight.Bold)
            }

            OutlinedButton(
              onClick = {
                pickMediaLauncher.launch(
                  PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                )
              },
              enabled = !isAnalyzingImage,
              shape = RoundedCornerShape(8.dp),
              contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
              border = androidx.compose.foundation.BorderStroke(1.dp, MetalPrimary),
              modifier = Modifier
                .weight(1f)
                .heightIn(min = 34.dp, max = 38.dp)
                .testTag("pick_gallery_button")
            ) {
              Icon(Icons.Default.PhotoLibrary, contentDescription = null, tint = MetalPrimary, modifier = Modifier.size(14.dp))
              Spacer(modifier = Modifier.width(4.dp))
              Text("Galería", color = MetalPrimary, fontSize = 11.sp, fontWeight = FontWeight.Bold)
            }
          }

          if (capturedBitmap != null) {
            Spacer(modifier = Modifier.height(8.dp))
            Row(
              modifier = Modifier.fillMaxWidth(),
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.SpaceBetween
            ) {
              Image(
                bitmap = capturedBitmap!!.asImageBitmap(),
                contentDescription = "Imagen capturada",
                contentScale = ContentScale.Crop,
                modifier = Modifier
                  .size(56.dp)
                  .clip(RoundedCornerShape(8.dp))
                  .border(1.dp, MetalSecondary, RoundedCornerShape(8.dp))
              )
              Spacer(modifier = Modifier.width(8.dp))
              Column(modifier = Modifier.weight(1f)) {
                Text("Imagen lista para análisis multimodal", color = TextMain, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                Text("Resolución: ${capturedBitmap!!.width}x${capturedBitmap!!.height} px", color = TextSub, fontSize = 10.sp)
              }
              OutlinedButton(
                onClick = { onAnalyzeImage(capturedBitmap!!, visionPrompt, true) },
                enabled = !isAnalyzingImage,
                shape = RoundedCornerShape(6.dp),
                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                modifier = Modifier.heightIn(min = 28.dp, max = 32.dp)
              ) {
                Text("Enviar al Chat", color = MetalPrimary, fontSize = 10.sp)
              }
            }
          }

          if (isAnalyzingImage) {
            Spacer(modifier = Modifier.height(8.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
              CircularProgressIndicator(color = MetalSecondary, modifier = Modifier.size(16.dp), strokeWidth = 2.dp)
              Spacer(modifier = Modifier.width(8.dp))
              Text("Analizando imagen con visión multimodal...", color = MetalSecondary, fontSize = 11.sp)
            }
          }

          if (imageAnalysisResult != null) {
            Spacer(modifier = Modifier.height(8.dp))
            Card(
              modifier = Modifier.fillMaxWidth(),
              colors = CardDefaults.cardColors(containerColor = SurfacePearl),
              shape = RoundedCornerShape(8.dp),
              border = androidx.compose.foundation.BorderStroke(1.dp, MetalSecondary.copy(alpha = 0.4f))
            ) {
              Column(modifier = Modifier.padding(10.dp)) {
                Row(
                  modifier = Modifier.fillMaxWidth(),
                  horizontalArrangement = Arrangement.SpaceBetween,
                  verticalAlignment = Alignment.CenterVertically
                ) {
                  Text(
                    text = "RESULTADO DE VISIÓN IA (ALPHA):",
                    color = MetalSecondary,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace
                  )
                  Row {
                    IconButton(
                      onClick = { onSpeak(imageAnalysisResult) },
                      modifier = Modifier.size(26.dp)
                    ) {
                      Icon(Icons.Default.VolumeUp, contentDescription = "Escuchar", tint = MetalPrimary, modifier = Modifier.size(15.dp))
                    }
                    IconButton(
                      onClick = {
                        scope.launch {
                          ApkShareHelper.shareTextAsFile(
                            context = context,
                            fileNamePrefix = "Vision_IA_${selectedLanguage}",
                            textContent = imageAnalysisResult
                          )
                        }
                      },
                      modifier = Modifier.size(26.dp)
                    ) {
                      Icon(Icons.Default.Share, contentDescription = "Enviar como Archivo", tint = MetalSecondary, modifier = Modifier.size(15.dp))
                    }
                  }
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                  text = imageAnalysisResult,
                  color = TextMain,
                  fontSize = 12.sp,
                  lineHeight = 16.sp
                )
              }
            }
          }
        }
      }
    }

    // 7. Real-Time Anti-Hacking & Physical Sensors Telemetry Grid (Regla 1 y 2)
    item {
      Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = SurfacePearl),
        border = androidx.compose.foundation.BorderStroke(1.dp, MetalPrimary)
      ) {
        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
          Text(
            text = "Telemetría Anti-Rastreo y Sensores Físicos (Regla 1)",
            color = MetalSecondary,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.Monospace
          )

          // Network & VPN Row
          Row(
            modifier = Modifier
              .fillMaxWidth()
              .background(Color.White, RoundedCornerShape(8.dp))
              .padding(10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
          ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
              Icon(Icons.Default.Wifi, contentDescription = null, tint = MetalPrimary, modifier = Modifier.size(16.dp))
              Spacer(modifier = Modifier.width(8.dp))
              Column {
                Text("Red: ${telemetry.networkType}", color = TextMain, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                Text(
                  text = if (telemetry.isVpnActive) "Túnel VPN Activo • Tráfico Cifrado" else "Conexión Directa Estándar",
                  color = if (telemetry.isVpnActive) MetalSecondary else TextSub,
                  fontSize = 10.sp
                )
              }
            }
            Text(
              text = if (telemetry.isConnected) "ONLINE" else "OFFLINE",
              color = if (telemetry.isConnected) MetalSecondary else DangerRed,
              fontSize = 10.sp,
              fontWeight = FontWeight.Bold,
              fontFamily = FontFamily.Monospace
            )
          }

          // Magnetometer / EMF Detector Row
          Row(
            modifier = Modifier
              .fillMaxWidth()
              .background(Color.White, RoundedCornerShape(8.dp))
              .padding(10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
          ) {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
              Icon(
                imageVector = Icons.Default.Explore,
                contentDescription = null,
                tint = if (telemetry.magneticAnomalyDetected) DangerRed else MetalSecondary,
                modifier = Modifier.size(16.dp)
              )
              Spacer(modifier = Modifier.width(8.dp))
              Column {
                Text(
                  text = "Campo Magnético (Detector EMF): ${String.format(Locale.US, "%.1f", telemetry.magneticFieldMicroTesla)} µT",
                  color = TextMain,
                  fontSize = 12.sp,
                  fontWeight = FontWeight.SemiBold
                )
                Text(
                  text = if (telemetry.magneticAnomalyDetected)
                    "¡Alerta! Campo magnético elevado cerca del dispositivo."
                  else
                    "Nivel normal (Sin anomalías magnéticas/espía cercanas).",
                  color = if (telemetry.magneticAnomalyDetected) DangerRed else TextSub,
                  fontSize = 10.sp
                )
              }
            }
          }

          // GPS Location Row
          Row(
            modifier = Modifier
              .fillMaxWidth()
              .background(Color.White, RoundedCornerShape(8.dp))
              .padding(10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
          ) {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
              Icon(Icons.Default.GpsFixed, contentDescription = null, tint = MetalSecondary, modifier = Modifier.size(16.dp))
              Spacer(modifier = Modifier.width(8.dp))
              Column {
                Text(
                  text = "Ubicación de Resguardo (Regla 2)",
                  color = TextMain,
                  fontSize = 12.sp,
                  fontWeight = FontWeight.SemiBold
                )
                Text(
                  text = telemetry.gpsCoordinates + (telemetry.gpsAccuracyMeters?.let { " (±${it.toInt()}m)" } ?: ""),
                  color = TextSub,
                  fontSize = 10.sp,
                  fontFamily = FontFamily.Monospace
                )
              }
            }
            OutlinedButton(
              onClick = {
                deviceShieldManager.refreshSystemAndNetworkTelemetry()
                onSaveCheckpoint(
                  "Punto de Resguardo GPS",
                  "Coordenadas registradas: ${telemetry.gpsCoordinates} | Red: ${telemetry.networkType}"
                )
              },
              shape = RoundedCornerShape(6.dp),
              contentPadding = PaddingValues(horizontal = 8.dp, vertical = 3.dp),
              border = androidx.compose.foundation.BorderStroke(1.dp, MetalSecondary),
              modifier = Modifier.heightIn(min = 28.dp, max = 32.dp)
            ) {
              Text("Guardar GPS", color = MetalSecondary, fontSize = 10.sp)
            }
          }

          // System Integrity & Battery Row
          Row(
            modifier = Modifier
              .fillMaxWidth()
              .background(Color.White, RoundedCornerShape(8.dp))
              .padding(10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
          ) {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
              Icon(
                imageVector = if (telemetry.isAdbEnabled) Icons.Default.Warning else Icons.Default.BatteryChargingFull,
                contentDescription = null,
                tint = if (telemetry.isAdbEnabled) MetalSecondary else MetalPrimary,
                modifier = Modifier.size(16.dp)
              )
              Spacer(modifier = Modifier.width(8.dp))
              Column {
                Text(
                  text = "Integridad del Sistema & Energía (${telemetry.batteryLevel}% • ${telemetry.batteryTempCelsius}°C)",
                  color = TextMain,
                  fontSize = 12.sp,
                  fontWeight = FontWeight.SemiBold
                )
                Text(
                  text = if (telemetry.isAdbEnabled)
                    "Aviso Regla 1: Depuración USB (ADB) activa. Desactívala si conectas a puertos públicos."
                  else
                    "Puerto ADB protegido • Luz ambiental: ${telemetry.ambientLightLux.toInt()} lx",
                  color = if (telemetry.isAdbEnabled) MetalSecondary else TextSub,
                  fontSize = 10.sp
                )
              }
            }
          }
        }
      }
    }
  }
}
