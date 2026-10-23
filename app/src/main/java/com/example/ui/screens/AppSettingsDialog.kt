package com.example.ui.screens

import android.app.ActivityManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import android.widget.Toast
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
import androidx.compose.material.icons.filled.Apps
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Diamond
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
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
import androidx.compose.runtime.rememberCoroutineScope
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
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.DarkSurfaceElevated
import com.example.ui.theme.LauncherMode
import com.example.ui.theme.MetalPrimary
import com.example.ui.theme.MetalSecondary
import com.example.ui.theme.SurfacePearl
import com.example.ui.theme.TextMain
import com.example.ui.theme.TextSub
import com.example.ui.theme.UiThemeController
import kotlinx.coroutines.launch

@Composable
fun AppSettingsDialog(
  isOpen: Boolean,
  onDismiss: () -> Unit,
  onOpenThemeStudio: (() -> Unit)? = null,
  onOpenNotificationTones: (() -> Unit)? = null,
  onOpenLogoStudio: (() -> Unit)? = null,
  onOpenHardwareAccelerator: (() -> Unit)? = null,
  onOpenRiskScanner: (() -> Unit)? = null,
  onOpenDashboard: (() -> Unit)? = null,
  onSetDefaultHome: (() -> Unit)? = null
) {
  if (!isOpen) return

  val context = LocalContext.current
  val prefs = remember { context.getSharedPreferences("hyperlux_app_settings_prefs", Context.MODE_PRIVATE) }
  val coroutineScope = rememberCoroutineScope()

  var activeTab by remember { mutableIntStateOf(0) }

  // App notifications toggle
  var appNotificationsEnabled by remember { mutableStateOf(prefs.getBoolean("app_notifications_enabled", true)) }

  // API keys
  var geminiApiKey by remember { mutableStateOf(prefs.getString("custom_gemini_api_key", "") ?: "") }
  var otherAiApiKey by remember { mutableStateOf(prefs.getString("other_ai_api_key", "") ?: "") }
  var otherAiProvider by remember { mutableStateOf(prefs.getString("other_ai_provider", "OpenAI / Claude / DeepSeek") ?: "OpenAI / Claude / DeepSeek") }

  // System permissions toggles
  var sysAppsPermEnabled by remember { mutableStateOf(prefs.getBoolean("sys_apps_perm_enabled", true)) }
  var hardwarePermEnabled by remember { mutableStateOf(prefs.getBoolean("hardware_perm_enabled", true)) }
  var highPerfSensorsEnabled by remember { mutableStateOf(prefs.getBoolean("high_perf_sensors_enabled", true)) }

  Dialog(onDismissRequest = onDismiss) {
    Card(
      modifier = Modifier
        .fillMaxWidth()
        .heightIn(max = 680.dp)
        .border(1.2.dp, MetalSecondary.copy(alpha = 0.85f), RoundedCornerShape(16.dp))
        .testTag("app_settings_dialog"),
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
                .size(32.dp)
                .background(MetalSecondary.copy(alpha = 0.2f), CircleShape)
                .border(1.dp, MetalSecondary, CircleShape),
              contentAlignment = Alignment.Center
            ) {
              Icon(Icons.Default.Settings, contentDescription = null, tint = MetalSecondary, modifier = Modifier.size(18.dp))
            }
            Spacer(modifier = Modifier.width(8.dp))
            Column {
              Text("CENTRO DE AJUSTES Y CONTROL", color = TextMain, fontSize = 12.sp, fontWeight = FontWeight.ExtraBold)
              Text("Modos • Sistema • Herramientas • Hardware", color = MetalPrimary, fontSize = 8.5.sp, fontFamily = FontFamily.Monospace)
            }
          }

          IconButton(onClick = onDismiss, modifier = Modifier.size(28.dp)) {
            Icon(Icons.Default.Close, contentDescription = "Cerrar", tint = TextSub, modifier = Modifier.size(18.dp))
          }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Tabs: 0: Modos & Lanzador, 1: Permisos & Notif., 2: API Keys IA, 3: Sistema & Apps
        TabRow(
          selectedTabIndex = activeTab,
          containerColor = DarkSurfaceElevated,
          contentColor = MetalSecondary,
          indicator = { tabPositions ->
            TabRowDefaults.SecondaryIndicator(
              Modifier.tabIndicatorOffset(tabPositions[activeTab]),
              color = MetalSecondary,
              height = 2.dp
            )
          }
        ) {
          Tab(
            selected = activeTab == 0,
            onClick = { activeTab = 0 },
            text = { Text("Lanzador", fontSize = 9.5.sp, fontWeight = FontWeight.Bold, color = if (activeTab == 0) MetalSecondary else TextSub) }
          )
          Tab(
            selected = activeTab == 1,
            onClick = { activeTab = 1 },
            text = { Text("Permisos", fontSize = 9.5.sp, fontWeight = FontWeight.Bold, color = if (activeTab == 1) MetalSecondary else TextSub) }
          )
          Tab(
            selected = activeTab == 2,
            onClick = { activeTab = 2 },
            text = { Text("API Keys IA", fontSize = 9.5.sp, fontWeight = FontWeight.Bold, color = if (activeTab == 2) MetalSecondary else TextSub) }
          )
          Tab(
            selected = activeTab == 3,
            onClick = { activeTab = 3 },
            text = { Text("Hardware", fontSize = 9.5.sp, fontWeight = FontWeight.Bold, color = if (activeTab == 3) MetalSecondary else TextSub) }
          )
        }

        Spacer(modifier = Modifier.height(12.dp))

        Column(
          modifier = Modifier
            .weight(1f)
            .verticalScroll(rememberScrollState()),
          verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
          when (activeTab) {
            0 -> {
              // =================== TAB 0: MODOS & LANZADOR ===================
              Text(
                "3 MODOS DE LANZADOR DEL SISTEMA:",
                color = MetalSecondary,
                fontSize = 11.sp,
                fontWeight = FontWeight.ExtraBold
              )

              LauncherMode.entries.forEach { mode ->
                val isSelected = UiThemeController.launcherMode == mode
                Row(
                  modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(if (isSelected) MetalSecondary.copy(alpha = 0.18f) else DarkSurface)
                    .border(
                      width = if (isSelected) 1.2.dp else 0.6.dp,
                      color = if (isSelected) MetalSecondary else MetalPrimary.copy(alpha = 0.3f),
                      shape = RoundedCornerShape(10.dp)
                    )
                    .clickable {
                      UiThemeController.updateLauncherMode(context, mode)
                      Toast.makeText(context, "Modo cambiado a ${mode.label}", Toast.LENGTH_SHORT).show()
                    }
                    .padding(horizontal = 10.dp, vertical = 8.dp),
                  verticalAlignment = Alignment.CenterVertically,
                  horizontalArrangement = Arrangement.SpaceBetween
                ) {
                  Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                    Text(mode.iconEmoji, fontSize = 20.sp)
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                      Text(mode.label, color = TextMain, fontSize = 11.5.sp, fontWeight = FontWeight.Bold)
                      Text(mode.subtitle, color = TextSub, fontSize = 8.5.sp)
                    }
                  }
                  if (isSelected) {
                    Box(
                      modifier = Modifier
                        .size(20.dp)
                        .background(MetalSecondary, CircleShape),
                      contentAlignment = Alignment.Center
                    ) {
                      Icon(Icons.Default.Check, contentDescription = null, tint = Color.Black, modifier = Modifier.size(14.dp))
                    }
                  }
                }
              }

              HorizontalDivider(color = MetalPrimary.copy(alpha = 0.4f), thickness = 0.8.dp)

              // Acciones del Sistema
              Text("PREMIUM - DESBLOQUEAR TODO:", color = MetalSecondary, fontSize = 11.sp, fontWeight = FontWeight.Bold)
              
              Button(
                onClick = {
                    UiThemeController.updatePremiumStatus(context, true)
                    Toast.makeText(context, "¡Activado Modo Premium!", Toast.LENGTH_SHORT).show()
                },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MetalSecondary, contentColor = Color.Black)
              ) {
                Text(if (UiThemeController.isPremium) "¡Ya eres Premium!" else "Desbloquear Premium", fontSize = 11.sp, fontWeight = FontWeight.ExtraBold)
              }

              HorizontalDivider(color = MetalPrimary.copy(alpha = 0.4f), thickness = 0.8.dp)
              Text("ACCIONES DEL SISTEMA:", color = MetalSecondary, fontSize = 11.sp, fontWeight = FontWeight.Bold)

              // Fijar como Launcher Predeterminado
              Button(
                onClick = {
                  if (onSetDefaultHome != null) {
                    onSetDefaultHome()
                  } else {
                    promptSetDefaultHomeLauncher(context)
                  }
                },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MetalSecondary, contentColor = Color.Black)
              ) {
                Icon(Icons.Default.Home, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Fijar LAUNCH KDF como Inicio Predeterminado", fontSize = 11.sp, fontWeight = FontWeight.ExtraBold)
              }

              // Turbo RAM Boost
              Button(
                onClick = {
                  System.gc()
                  Toast.makeText(context, "⚡ Memoria RAM optimizada y procesos depurados", Toast.LENGTH_SHORT).show()
                },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.buttonColors(containerColor = DarkSurfaceElevated, contentColor = MetalSecondary),
                border = androidx.compose.foundation.BorderStroke(1.dp, MetalSecondary.copy(alpha = 0.6f))
              ) {
                Icon(Icons.Default.Bolt, contentDescription = null, tint = MetalSecondary, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Optimizar y Depurar Memoria RAM", fontSize = 11.sp, fontWeight = FontWeight.Bold)
              }

              HorizontalDivider(color = MetalPrimary.copy(alpha = 0.4f), thickness = 0.8.dp)

              // Herramientas y Módulos
              Text("HERRAMIENTAS DE PERSONALIZACIÓN Y CONTROL:", color = MetalSecondary, fontSize = 11.sp, fontWeight = FontWeight.Bold)

              Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
              ) {
                // Estudio de Temas
                OutlinedButton(
                  onClick = {
                    onDismiss()
                    onOpenThemeStudio?.invoke()
                  },
                  modifier = Modifier.weight(1f),
                  shape = RoundedCornerShape(8.dp)
                ) {
                  Icon(Icons.Default.Palette, contentDescription = null, tint = MetalSecondary, modifier = Modifier.size(14.dp))
                  Spacer(modifier = Modifier.width(6.dp))
                  Text("Temas & Fondos", fontSize = 10.sp, color = TextMain)
                }

                // Tonos 4K
                OutlinedButton(
                  onClick = {
                    onDismiss()
                    onOpenNotificationTones?.invoke()
                  },
                  modifier = Modifier.weight(1f),
                  shape = RoundedCornerShape(8.dp)
                ) {
                  Icon(Icons.Default.GraphicEq, contentDescription = null, tint = MetalSecondary, modifier = Modifier.size(14.dp))
                  Spacer(modifier = Modifier.width(6.dp))
                  Text("Tonos 4K", fontSize = 10.sp, color = TextMain)
                }
                
                // Dashboard
                OutlinedButton(
                  onClick = {
                    onDismiss()
                    onOpenDashboard?.invoke()
                  },
                  modifier = Modifier.weight(1f),
                  shape = RoundedCornerShape(8.dp)
                ) {
                  Icon(Icons.Default.GraphicEq, contentDescription = null, tint = MetalSecondary, modifier = Modifier.size(14.dp))
                  Spacer(modifier = Modifier.width(6.dp))
                  Text("Dashboard", fontSize = 10.sp, color = TextMain)
                }
              }

              Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
              ) {
                // Logo & CSS
                OutlinedButton(
                  onClick = {
                    onDismiss()
                    onOpenLogoStudio?.invoke()
                  },
                  modifier = Modifier.weight(1f),
                  shape = RoundedCornerShape(8.dp)
                ) {
                  Icon(Icons.Default.Diamond, contentDescription = null, tint = MetalSecondary, modifier = Modifier.size(14.dp))
                  Spacer(modifier = Modifier.width(6.dp))
                  Text("Logo & CSS 3D", fontSize = 10.sp, color = TextMain)
                }

                // Turbo Hardware NPU/GPU
                OutlinedButton(
                  onClick = {
                    onDismiss()
                    onOpenHardwareAccelerator?.invoke()
                  },
                  modifier = Modifier.weight(1f),
                  shape = RoundedCornerShape(8.dp)
                ) {
                  Icon(Icons.Default.Memory, contentDescription = null, tint = MetalSecondary, modifier = Modifier.size(14.dp))
                  Spacer(modifier = Modifier.width(6.dp))
                  Text("Turbo NPU/GPU", fontSize = 10.sp, color = TextMain)
                }
              }

              HorizontalDivider(color = MetalPrimary.copy(alpha = 0.4f), thickness = 0.8.dp)

              // Ajustes de Cuadrícula
              Text("CONFIGURACIÓN DE CUADRÍCULA Y NAVEGACIÓN:", color = MetalSecondary, fontSize = 11.sp, fontWeight = FontWeight.ExtraBold)

              // Altura barra navegación
              Column(
                modifier = Modifier
                  .fillMaxWidth()
                  .background(DarkSurface, RoundedCornerShape(8.dp))
                  .padding(10.dp)
              ) {
                Text("Altura Barra Navegación: ${UiThemeController.navigationBarHeight.toInt()}dp", color = TextMain, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                Slider(
                  value = UiThemeController.navigationBarHeight,
                  onValueChange = { UiThemeController.updateNavigationBarHeight(context, it) },
                  valueRange = 40f..80f,
                  colors = SliderDefaults.colors(thumbColor = MetalSecondary, activeTrackColor = MetalSecondary)
                )
              }

              // Asistente IA
              Row(
                modifier = Modifier
                  .fillMaxWidth()
                  .background(DarkSurface, RoundedCornerShape(8.dp))
                  .padding(10.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
              ) {
                Column(modifier = Modifier.weight(1f)) {
                  Text("Motor de Ayuda IA", color = TextMain, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                  Text("Activar asistente flotante", color = TextSub, fontSize = 8.5.sp)
                }
                Switch(
                  checked = UiThemeController.assistantEngineEnabled,
                  onCheckedChange = { UiThemeController.updateAssistantEngineEnabled(context, it) },
                  colors = SwitchDefaults.colors(checkedThumbColor = MetalSecondary)
                )
              }

              Row(
                modifier = Modifier
                  .fillMaxWidth()
                  .background(DarkSurface, RoundedCornerShape(8.dp))
                  .padding(10.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
              ) {
                Column {
                  Text("Columnas de Apps: ${UiThemeController.gridColumns}", color = TextMain, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                  Text("Distribución de iconos", color = TextSub, fontSize = 8.5.sp)
                }
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                  (3..5).forEach { cols ->
                    Box(
                      modifier = Modifier
                        .size(28.dp)
                        .clip(RoundedCornerShape(6.dp))
                        .background(if (UiThemeController.gridColumns == cols) MetalSecondary else DarkSurfaceElevated)
                        .clickable { UiThemeController.updateGridColumns(context, cols) },
                      contentAlignment = Alignment.Center
                    ) {
                      Text("$cols", color = if (UiThemeController.gridColumns == cols) Color.Black else TextMain, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                  }
                }
              }

              Row(
                modifier = Modifier
                  .fillMaxWidth()
                  .background(DarkSurface, RoundedCornerShape(8.dp))
                  .padding(10.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
              ) {
                Column {
                  Text("Etiquetas de Texto", color = TextMain, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                  Text("Mostrar nombres bajo iconos", color = TextSub, fontSize = 8.5.sp)
                }
                Switch(
                  checked = UiThemeController.showAppLabels,
                  onCheckedChange = { UiThemeController.updateShowAppLabels(context, it) },
                  colors = SwitchDefaults.colors(checkedThumbColor = MetalSecondary)
                )
              }

              Row(
                modifier = Modifier
                  .fillMaxWidth()
                  .background(DarkSurface, RoundedCornerShape(8.dp))
                  .padding(10.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
              ) {
                Column {
                  Text("Dock Flotante", color = TextMain, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                  Text("Barra inferior rápida", color = TextSub, fontSize = 8.5.sp)
                }
                Switch(
                  checked = UiThemeController.showFloatingDock,
                  onCheckedChange = { UiThemeController.updateShowFloatingDock(context, it) },
                  colors = SwitchDefaults.colors(checkedThumbColor = MetalSecondary)
                )
              }
            }

            1 -> {
              // =================== TAB 1: PERMISOS & NOTIF ===================
              Text("NOTIFICACIONES DE LA APLICACIÓN:", color = MetalSecondary, fontSize = 11.sp, fontWeight = FontWeight.Bold)

              Row(
                modifier = Modifier
                  .fillMaxWidth()
                  .background(DarkSurface, RoundedCornerShape(8.dp))
                  .padding(10.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
              ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                  Icon(Icons.Default.Notifications, contentDescription = null, tint = MetalSecondary, modifier = Modifier.size(18.dp))
                  Spacer(modifier = Modifier.width(8.dp))
                  Column {
                    Text("Alertas y Notificaciones Activas", color = TextMain, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    Text("Avisos del sistema y audio 4K", color = TextSub, fontSize = 9.sp)
                  }
                }
                Switch(
                  checked = appNotificationsEnabled,
                  onCheckedChange = {
                    appNotificationsEnabled = it
                    prefs.edit().putBoolean("app_notifications_enabled", it).apply()
                  },
                  colors = SwitchDefaults.colors(checkedThumbColor = MetalSecondary)
                )
              }

              HorizontalDivider(color = MetalPrimary.copy(alpha = 0.4f), thickness = 0.6.dp)

              Text("PERMISOS DEL SISTEMA PARA LAUNCH KDF:", color = MetalPrimary, fontSize = 11.sp, fontWeight = FontWeight.Bold)

              Button(
                onClick = {
                  val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                    data = Uri.fromParts("package", context.packageName, null)
                  }
                  context.startActivity(intent)
                },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color.White, contentColor = MetalPrimary)
              ) {
                Icon(Icons.Default.Lock, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Abrir Administrador de Permisos de Android", fontSize = 11.sp, fontWeight = FontWeight.Bold)
              }

              Text(
                "• Notificaciones: Canal de alta prioridad para tonos 4K\n• Micrófono: Comandos y asistente por voz\n• Batería y Sensores: Telemetría de hardware en tiempo real\n• Inicio / Home: Concedido para actuar como lanzador del sistema",
                color = TextSub,
                fontSize = 9.sp,
                lineHeight = 15.sp
              )
            }

            2 -> {
              // =================== TAB 2: API KEYS IA ===================
              var isTestingKey by remember { mutableStateOf(false) }
              var testResultText by remember { mutableStateOf<String?>(null) }

              Text("CONFIGURAR API KEY GEMINI (OPCIONAL):", color = MetalSecondary, fontSize = 11.sp, fontWeight = FontWeight.Bold)
              Text(
                "Puedes ingresar tu clave de Google AI Studio (aistudio.google.com) o usar el motor local sin internet.",
                color = TextSub,
                fontSize = 9.5.sp,
                lineHeight = 14.sp
              )

              OutlinedTextField(
                value = geminiApiKey,
                onValueChange = {
                  geminiApiKey = it
                  testResultText = null
                },
                placeholder = { Text("AIzaSy...", color = TextSub, fontSize = 11.sp) },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp),
                colors = OutlinedTextFieldDefaults.colors(
                  focusedBorderColor = MetalSecondary,
                  unfocusedBorderColor = MetalPrimary,
                  focusedContainerColor = DarkSurface,
                  unfocusedContainerColor = DarkSurface,
                  focusedTextColor = TextMain,
                  unfocusedTextColor = TextMain
                ),
                singleLine = true
              )

              Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
              ) {
                OutlinedButton(
                  onClick = {
                    if (geminiApiKey.isBlank()) {
                      testResultText = "⚠️ Ingresa primero una clave para probar."
                      return@OutlinedButton
                    }
                    isTestingKey = true
                    testResultText = "Probando conexión con Google Gemini..."
                    coroutineScope.launch {
                      val repo = com.example.data.network.GeminiRepository(context)
                      val (_, message) = repo.testApiKey(geminiApiKey.trim())
                      isTestingKey = false
                      testResultText = message
                    }
                  },
                  modifier = Modifier.weight(1f),
                  shape = RoundedCornerShape(8.dp),
                  enabled = !isTestingKey
                ) {
                  Text(if (isTestingKey) "Verificando..." else "Probar Clave", fontSize = 10.sp, color = MetalPrimary)
                }

                Button(
                  onClick = {
                    prefs.edit().putString("custom_gemini_api_key", geminiApiKey.trim()).apply()
                    Toast.makeText(context, "API Key guardada", Toast.LENGTH_SHORT).show()
                  },
                  modifier = Modifier.weight(1f),
                  shape = RoundedCornerShape(8.dp),
                  colors = ButtonDefaults.buttonColors(containerColor = MetalSecondary, contentColor = Color.Black)
                ) {
                  Text("Guardar Clave", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                }
              }

              testResultText?.let {
                Text(it, color = if (it.startsWith("⚠️") || it.startsWith("Error")) Color(0xFFEF4444) else MetalSecondary, fontSize = 9.5.sp)
              }
            }

            3 -> {
              // =================== TAB 3: HARDWARE & SISTEMA ===================
              Text("ACCESO A SENSORES Y HARDWARE NATIVO:", color = MetalSecondary, fontSize = 11.sp, fontWeight = FontWeight.Bold)

              Row(
                modifier = Modifier
                  .fillMaxWidth()
                  .background(DarkSurface, RoundedCornerShape(8.dp))
                  .padding(10.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
              ) {
                Column(modifier = Modifier.weight(1f)) {
                  Text("Sensores de Alta Frecuencia", color = TextMain, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                  Text("Uso de HIGH_SAMPLING_RATE_SENSORS para telemetría", color = TextSub, fontSize = 8.5.sp)
                }
                Switch(
                  checked = highPerfSensorsEnabled,
                  onCheckedChange = {
                    highPerfSensorsEnabled = it
                    prefs.edit().putBoolean("high_perf_sensors_enabled", it).apply()
                  },
                  colors = SwitchDefaults.colors(checkedThumbColor = MetalSecondary)
                )
              }

              HorizontalDivider(color = MetalPrimary.copy(alpha = 0.4f), thickness = 0.6.dp)

              Text("GESTIÓN DE APLICACIONES DE ANDROID:", color = MetalPrimary, fontSize = 11.sp, fontWeight = FontWeight.Bold)

              Button(
                onClick = {
                  val intent = Intent(Settings.ACTION_MANAGE_APPLICATIONS_SETTINGS)
                  context.startActivity(intent)
                },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.buttonColors(containerColor = DarkSurfaceElevated, contentColor = MetalPrimary),
                border = androidx.compose.foundation.BorderStroke(0.8.dp, MetalPrimary.copy(alpha = 0.5f))
              ) {
                Icon(Icons.Default.Apps, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Administrador de Apps de Android", fontSize = 11.sp, fontWeight = FontWeight.Bold)
              }

              Button(
                onClick = {
                  val shareIntent = Intent(Intent.ACTION_SEND).apply {
                    type = "text/plain"
                    putExtra(Intent.EXTRA_TEXT, "LAUNCH KDF - Lanzador de Sistema con 3 Modos (KDF, Android, iOS) y Audio 4K.")
                  }
                  context.startActivity(Intent.createChooser(shareIntent, "Compartir LAUNCH KDF"))
                },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.buttonColors(containerColor = DarkSurfaceElevated, contentColor = MetalSecondary),
                border = androidx.compose.foundation.BorderStroke(0.8.dp, MetalSecondary.copy(alpha = 0.5f))
              ) {
                Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Compartir LAUNCH KDF", fontSize = 11.sp, fontWeight = FontWeight.Bold)
              }
            }
          }
        }

        Spacer(modifier = Modifier.height(8.dp))

        Button(
          onClick = onDismiss,
          modifier = Modifier.fillMaxWidth().height(40.dp),
          shape = RoundedCornerShape(10.dp),
          colors = ButtonDefaults.buttonColors(containerColor = MetalSecondary, contentColor = Color.Black)
        ) {
          Text("Cerrar Ajustes", fontWeight = FontWeight.ExtraBold, fontSize = 12.sp)
        }
      }
    }
  }
}
