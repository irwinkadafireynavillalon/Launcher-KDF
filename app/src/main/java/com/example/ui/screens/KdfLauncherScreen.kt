package com.example.ui.screens

import android.app.ActivityManager
import android.app.role.RoleManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.BatteryManager
import android.os.Build
import android.provider.Settings
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas as ComposeCanvas
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.GenericShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.R
import com.example.hardware.ComputeAccelerationManager
import com.example.ui.theme.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

data class LauncherAppEntry(
  val id: String,
  val label: String,
  val packageName: String,
  val category: String,
  val isInternalModule: Boolean = false,
  val internalIcon: ImageVector = Icons.Default.Apps,
  val accentColor: Color = MetalSecondary,
  val appIconBitmap: ImageBitmap? = null,
  val onInternalClick: (() -> Unit)? = null
)

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun KdfLauncherScreen(
  versionName: String,
  practiceSessionCount: Int,
  onOpenVoiceChat: () -> Unit,
  onOpenDeviceShield: () -> Unit,
  onOpenOrchestrator: () -> Unit,
  onOpenThemeStudio: () -> Unit,
  onOpenHardwareAccelerator: () -> Unit,
  onOpenRiskScanner: () -> Unit,
  onOpenHistory: () -> Unit,
  onOpenAppSettings: () -> Unit,
  onOpenApkShare: () -> Unit,
  onQuickAiCommand: (String) -> Unit,
  onOpenNotificationTones: () -> Unit = {},
  onOpenLogoStudio: () -> Unit = {}
) {
  val context = LocalContext.current
  val hardwareTelemetry by ComputeAccelerationManager.telemetry.collectAsStateWithLifecycle()

  var searchQuery by remember { mutableStateOf("") }
  var selectedCategory by remember { mutableStateOf("Todas") }
  var selectedThemeChipIndex by remember { mutableStateOf(0) }
  var installedSystemApps by remember { mutableStateOf<List<LauncherAppEntry>>(emptyList()) }
  var selectedAppForMenu by remember { mutableStateOf<LauncherAppEntry?>(null) }
  var showQuickControlSheet by remember { mutableStateOf(false) }

  // Real-time clock & system telemetry state
  var currentTimeStr by remember { mutableStateOf("") }
  var currentSecondsStr by remember { mutableStateOf("") }
  var currentDateStr by remember { mutableStateOf("") }
  var batteryPct by remember { mutableIntStateOf(100) }
  var ramUsedPct by remember { mutableIntStateOf(42) }
  var ramFreeMb by remember { mutableLongStateOf(2048L) }
  var isDefaultHome by remember { mutableStateOf(isKdfDefaultHomeLauncher(context)) }

  LaunchedEffect(Unit) {
    while (true) {
      val now = Date()
      currentTimeStr = SimpleDateFormat("HH:mm", Locale.getDefault()).format(now)
      currentSecondsStr = SimpleDateFormat(":ss", Locale.getDefault()).format(now)
      currentDateStr = SimpleDateFormat("EEEE, d MMM yyyy", Locale.forLanguageTag("es-ES"))
        .format(now)
        .replaceFirstChar { it.uppercase() }

      val bm = context.getSystemService(Context.BATTERY_SERVICE) as? BatteryManager
      batteryPct = bm?.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY)?.coerceIn(1, 100) ?: 95

      val am = context.getSystemService(Context.ACTIVITY_SERVICE) as? ActivityManager
      if (am != null) {
        val memInfo = ActivityManager.MemoryInfo()
        am.getMemoryInfo(memInfo)
        val total = memInfo.totalMem.coerceAtLeast(1L)
        val avail = memInfo.availMem
        ramFreeMb = avail / (1024L * 1024L)
        ramUsedPct = (((total - avail) * 100L) / total).toInt().coerceIn(10, 99)
      }
      isDefaultHome = isKdfDefaultHomeLauncher(context)
      delay(1000L)
    }
  }

  // Load real installed apps from Android PackageManager
  LaunchedEffect(Unit) {
    installedSystemApps = withContext(Dispatchers.IO) {
      loadInstalledAndroidApps(context)
    }
  }

  val kdfInternalModules = remember(practiceSessionCount) {
    listOf(
      LauncherAppEntry(
        id = "kdf_theme_studio",
        label = "Temas KDF",
        packageName = "kdf.internal.themes",
        category = "Sistema KDF",
        isInternalModule = true,
        internalIcon = Icons.Default.Palette,
        accentColor = Color(0xFFD4AF37),
        onInternalClick = onOpenThemeStudio
      ),
      LauncherAppEntry(
        id = "kdf_voice_ai",
        label = "Voz & Chat IA",
        packageName = "kdf.internal.voice",
        category = "Sistema KDF",
        isInternalModule = true,
        internalIcon = Icons.Default.GraphicEq,
        accentColor = Color(0xFF00F5D4),
        onInternalClick = onOpenVoiceChat
      ),
      LauncherAppEntry(
        id = "kdf_orchestrator",
        label = "IA Orquestador",
        packageName = "kdf.internal.orchestrator",
        category = "Sistema KDF",
        isInternalModule = true,
        internalIcon = Icons.Default.Hub,
        accentColor = Color(0xFFA855F7),
        onInternalClick = onOpenOrchestrator
      ),
      LauncherAppEntry(
        id = "kdf_shield",
        label = "Escudo KDF",
        packageName = "kdf.internal.shield",
        category = "Sistema KDF",
        isInternalModule = true,
        internalIcon = Icons.Default.Shield,
        accentColor = Color(0xFF10B981),
        onInternalClick = onOpenDeviceShield
      ),
      LauncherAppEntry(
        id = "kdf_hardware",
        label = "Turbo CPU/GPU",
        packageName = "kdf.internal.hardware",
        category = "Sistema KDF",
        isInternalModule = true,
        internalIcon = Icons.Default.Memory,
        accentColor = Color(0xFF38BDF8),
        onInternalClick = onOpenHardwareAccelerator
      ),
      LauncherAppEntry(
        id = "kdf_scanner",
        label = "Auditor Riesgo",
        packageName = "kdf.internal.scanner",
        category = "Sistema KDF",
        isInternalModule = true,
        internalIcon = Icons.Default.Security,
        accentColor = Color(0xFFEF4444),
        onInternalClick = onOpenRiskScanner
      ),
      LauncherAppEntry(
        id = "kdf_room_sessions",
        label = "Sesiones ($practiceSessionCount)",
        packageName = "kdf.internal.sessions",
        category = "Sistema KDF",
        isInternalModule = true,
        internalIcon = Icons.Default.Storage,
        accentColor = Color(0xFFF59E0B),
        onInternalClick = onOpenHistory
      ),
      LauncherAppEntry(
        id = "kdf_settings",
        label = "Ajustes KDF",
        packageName = "kdf.internal.settings",
        category = "Sistema KDF",
        isInternalModule = true,
        internalIcon = Icons.Default.Tune,
        accentColor = Color(0xFF94A3B8),
        onInternalClick = onOpenAppSettings
      ),
      LauncherAppEntry(
        id = "kdf_share_apk",
        label = "Extraer APK",
        packageName = "kdf.internal.apk",
        category = "Sistema KDF",
        isInternalModule = true,
        internalIcon = Icons.Default.Share,
        accentColor = Color(0xFFEC4899),
        onInternalClick = onOpenApkShare
      ),
      LauncherAppEntry(
        id = "kdf_4k_tones",
        label = "Tonos 4K",
        packageName = "kdf.internal.tones",
        category = "Sistema KDF",
        isInternalModule = true,
        internalIcon = Icons.Default.GraphicEq,
        accentColor = Color(0xFFFFD700),
        onInternalClick = onOpenNotificationTones
      ),
      LauncherAppEntry(
        id = "kdf_logo_studio",
        label = "Logo & CSS",
        packageName = "kdf.internal.logocss",
        category = "Sistema KDF",
        isInternalModule = true,
        internalIcon = Icons.Default.Diamond,
        accentColor = Color(0xFFE2E8F0),
        onInternalClick = onOpenLogoStudio
      )
    )
  }

  val allLauncherApps = remember(kdfInternalModules, installedSystemApps) {
    kdfInternalModules + installedSystemApps
  }

  val pinnedSet = UiThemeController.pinnedPackages
  val filteredApps = remember(allLauncherApps, searchQuery, selectedCategory, pinnedSet) {
    allLauncherApps.filter { app ->
      val matchesSearch = searchQuery.isBlank() ||
        app.label.contains(searchQuery, ignoreCase = true) ||
        app.packageName.contains(searchQuery, ignoreCase = true)
      val matchesCategory = when (selectedCategory) {
        "Todas" -> true
        "Fijadas" -> pinnedSet.contains(app.packageName) || app.isInternalModule
        "Sistema KDF" -> app.isInternalModule
        "Android Apps" -> !app.isInternalModule
        else -> app.category == selectedCategory
      }
      matchesSearch && matchesCategory
    }
  }

  val currentIconShape = remember(UiThemeController.iconShapeId) {
    resolveLauncherIconShape(UiThemeController.iconShapeId)
  }

  Box(
    modifier = Modifier
      .fillMaxSize()
      .testTag("kdf_launcher_root")
  ) {
    // 1. Live Procedural 60 FPS Wallpaper Layer
    LiveLauncherWallpaperCanvas(
      effectId = UiThemeController.wallpaperEffectId,
      animationSpeed = UiThemeController.animationSpeed,
      modifier = Modifier.fillMaxSize()
    )

    // 2. Main Launcher Foreground Content
    Column(
      modifier = Modifier
        .fillMaxSize()
        .padding(horizontal = 8.dp, vertical = 2.dp),
      verticalArrangement = Arrangement.SpaceBetween
    ) {
      // 3 Tipos de Launcher Switcher Bar (KDF OS | Android | iOS)
      LauncherModeSelectorPill(
        currentMode = UiThemeController.launcherMode,
        onSelectMode = { UiThemeController.updateLauncherMode(context, it) }
      )

      Spacer(modifier = Modifier.height(2.dp))

      when (UiThemeController.launcherMode) {
        LauncherMode.ANDROID -> {
          AndroidLauncherContent(
            dateStr = currentDateStr,
            timeStr = currentTimeStr,
            batteryPct = batteryPct,
            installedApps = allLauncherApps,
            searchQuery = searchQuery,
            onSearchQueryChange = { searchQuery = it },
            onAppClick = { app -> launchAppEntry(context, app) },
            onAppLongClick = { app -> selectedAppForMenu = app },
            onOpenVoiceSearch = onOpenVoiceChat
          )
        }
        LauncherMode.IOS -> {
          IosLauncherContent(
            dateStr = currentDateStr,
            timeStr = currentTimeStr,
            batteryPct = batteryPct,
            installedApps = allLauncherApps,
            searchQuery = searchQuery,
            onSearchQueryChange = { searchQuery = it },
            onAppClick = { app -> launchAppEntry(context, app) },
            onAppLongClick = { app -> selectedAppForMenu = app },
            onOpenKdfStudio = onOpenThemeStudio
          )
        }
        LauncherMode.KDF_OS -> {
          // TOP SECTION: Clock / HUD + Quick Theme Strip + Search & Category Filter
          Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(6.dp)
          ) {
            // Customizable Clock & System Telemetry HUD
            KdfClockAndHudWidget(
              clockStyleId = UiThemeController.clockStyleId,
              timeStr = currentTimeStr,
              secondsStr = currentSecondsStr,
              dateStr = currentDateStr,
              batteryPct = batteryPct,
              ramUsedPct = ramUsedPct,
              ramFreeMb = ramFreeMb,
              cpuLoad = hardwareTelemetry.cpuLoadEstimatePercent,
              gpuLoad = if (hardwareTelemetry.gpuActive) 68 else 15,
              showTelemetry = UiThemeController.showTelemetryBar,
              isDefaultHome = isDefaultHome,
              onCycleClockStyle = {
                val ids = CLOCK_STYLE_OPTIONS.map { it.first }
                val nextIdx = (ids.indexOf(UiThemeController.clockStyleId) + 1) % ids.size
                UiThemeController.updateClockStyle(context, ids[nextIdx])
              },
              onBoostRam = {
                System.gc()
                try {
                  val am = context.getSystemService(Context.ACTIVITY_SERVICE) as? ActivityManager
                  installedSystemApps.take(10).forEach { app ->
                    am?.killBackgroundProcesses(app.packageName)
                  }
                } catch (_: Exception) {}
                Toast.makeText(
                  context,
                  "⚡ Turbo KDF completado: Caché depurada y memoria RAM optimizada (${ramFreeMb} MB libres)",
                  Toast.LENGTH_SHORT
                ).show()
              },
              onSetDefaultHome = {
                promptSetDefaultHomeLauncher(context)
              },
              onOpenQuickControls = {
                showQuickControlSheet = !showQuickControlSheet
              },
              onOpenLogoStudio = onOpenLogoStudio
            )

            // Expandable Compact System Control Deck (Wi-Fi, Bluetooth, Sound, Display, Wallpaper, Grid)
            AnimatedVisibility(visible = showQuickControlSheet) {
              KdfCompactSystemControlDeck(
                context = context,
                onOpenThemeStudio = onOpenThemeStudio,
                onSetDefaultHome = { promptSetDefaultHomeLauncher(context) },
                onOpenNotificationTones = onOpenNotificationTones,
                onOpenLogoStudio = onOpenLogoStudio
              )
            }

        // Instant Theme & Wallpaper Switcher Bar (Ultra-compact 1-tap theme ribbon)
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState()),
          horizontalArrangement = Arrangement.spacedBy(6.dp),
          verticalAlignment = Alignment.CenterVertically
        ) {
          // Quick Effect cycle chip
          val currentEffectName = WALLPAPER_EFFECT_OPTIONS
            .find { it.id == UiThemeController.wallpaperEffectId }?.title ?: "Fondo Vivo"
          Row(
            modifier = Modifier
              .clip(RoundedCornerShape(50))
              .background(DarkSurfaceElevated.copy(alpha = 0.85f))
              .border(0.8.dp, MetalPrimary.copy(alpha = 0.45f), RoundedCornerShape(50))
              .clickable {
                val ids = WALLPAPER_EFFECT_OPTIONS.map { it.id }
                val next = (ids.indexOf(UiThemeController.wallpaperEffectId) + 1) % ids.size
                UiThemeController.updateWallpaperEffect(context, ids[next])
              }
              .padding(horizontal = 9.dp, vertical = 4.dp)
              .testTag("launcher_cycle_wallpaper_chip"),
            verticalAlignment = Alignment.CenterVertically
          ) {
            Icon(
              Icons.Default.AutoAwesome,
              contentDescription = null,
              tint = MetalSecondary,
              modifier = Modifier.size(12.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
              currentEffectName,
              color = TextMain,
              fontSize = 10.sp,
              fontWeight = FontWeight.Bold
            )
          }

          // Instant 1-tap theme presets
          HYPER_LUX_THEME_PRESETS.forEachIndexed { idx, preset ->
            val selected = UiThemeController.selectedPresetId == preset.id
            Row(
              modifier = Modifier
                .clip(RoundedCornerShape(50))
                .background(
                  if (selected) preset.secondaryAccent.copy(alpha = 0.25f)
                  else DarkSurfaceElevated.copy(alpha = 0.75f)
                )
                .border(
                  width = if (selected) 1.2.dp else 0.6.dp,
                  color = if (selected) preset.secondaryAccent else MetalPrimary.copy(alpha = 0.3f),
                  shape = RoundedCornerShape(50)
                )
                .clickable {
                  selectedThemeChipIndex = idx
                  UiThemeController.selectPreset(context, preset.id)
                }
                .padding(horizontal = 8.dp, vertical = 4.dp),
              verticalAlignment = Alignment.CenterVertically
            ) {
              Box(
                modifier = Modifier
                  .size(10.dp)
                  .background(preset.secondaryAccent, CircleShape)
                  .border(0.5.dp, Color.White, CircleShape)
              )
              Spacer(modifier = Modifier.width(5.dp))
              Text(
                preset.name.take(14),
                color = TextMain,
                fontSize = 10.sp,
                fontWeight = if (selected) FontWeight.ExtraBold else FontWeight.Medium
              )
            }
          }
        }

        // Universal Compact Search & Category Filter Row
        Row(
          modifier = Modifier.fillMaxWidth(),
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
          // Search Bar
          Row(
            modifier = Modifier
              .weight(1f)
              .height(34.dp)
              .clip(RoundedCornerShape(10.dp))
              .background(DarkSurfaceElevated.copy(alpha = UiThemeController.glassOpacity))
              .border(0.8.dp, MetalPrimary.copy(alpha = 0.4f), RoundedCornerShape(10.dp))
              .padding(horizontal = 10.dp),
            verticalAlignment = Alignment.CenterVertically
          ) {
            Icon(
              Icons.Default.Search,
              contentDescription = "Buscar apps o comando",
              tint = MetalSecondary,
              modifier = Modifier.size(15.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.CenterStart) {
              if (searchQuery.isEmpty()) {
                Text(
                  "Buscar apps, web o comando IA...",
                  color = TextSub,
                  fontSize = 11.sp,
                  maxLines = 1,
                  overflow = TextOverflow.Ellipsis
                )
              }
              BasicTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                singleLine = true,
                textStyle = TextStyle(
                  color = TextMain,
                  fontSize = 12.sp,
                  fontWeight = FontWeight.Medium
                ),
                cursorBrush = SolidColor(MetalSecondary),
                modifier = Modifier
                  .fillMaxWidth()
                  .testTag("launcher_search_input")
              )
            }
            if (searchQuery.isNotEmpty()) {
              Icon(
                Icons.AutoMirrored.Filled.Send,
                contentDescription = "Consultar a IA",
                tint = MetalSecondary,
                modifier = Modifier
                  .size(16.dp)
                  .clickable {
                    val query = searchQuery
                    searchQuery = ""
                    onQuickAiCommand(query)
                  }
              )
              Spacer(modifier = Modifier.width(6.dp))
              Icon(
                Icons.Default.Close,
                contentDescription = "Limpiar búsqueda",
                tint = TextSub,
                modifier = Modifier
                  .size(15.dp)
                  .clickable { searchQuery = "" }
              )
            }
          }

          // Quick Icon Shape cycler button
          Box(
            modifier = Modifier
              .size(34.dp)
              .clip(RoundedCornerShape(10.dp))
              .background(DarkSurfaceElevated.copy(alpha = UiThemeController.glassOpacity))
              .border(0.8.dp, MetalSecondary.copy(alpha = 0.5f), RoundedCornerShape(10.dp))
              .clickable {
                val shapes = ICON_SHAPE_OPTIONS.map { it.first }
                val next = (shapes.indexOf(UiThemeController.iconShapeId) + 1) % shapes.size
                UiThemeController.updateIconShape(context, shapes[next])
              }
              .testTag("launcher_cycle_icon_shape_btn"),
            contentAlignment = Alignment.Center
          ) {
            Icon(
              Icons.Default.Category,
              contentDescription = "Cambiar forma de iconos",
              tint = MetalSecondary,
              modifier = Modifier.size(16.dp)
            )
          }

          // Quick Grid Columns cycler button
          Box(
            modifier = Modifier
              .height(34.dp)
              .clip(RoundedCornerShape(10.dp))
              .background(DarkSurfaceElevated.copy(alpha = UiThemeController.glassOpacity))
              .border(0.8.dp, MetalPrimary.copy(alpha = 0.5f), RoundedCornerShape(10.dp))
              .clickable {
                val nextCols = if (UiThemeController.gridColumns >= 6) 3 else UiThemeController.gridColumns + 1
                UiThemeController.updateGridColumns(context, nextCols)
              }
              .padding(horizontal = 8.dp)
              .testTag("launcher_cycle_grid_columns_btn"),
            contentAlignment = Alignment.Center
          ) {
            Text(
              text = "${UiThemeController.gridColumns}x",
              color = MetalSecondary,
              fontSize = 11.sp,
              fontWeight = FontWeight.ExtraBold
            )
          }
        }

        // Compact Category Pills
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState()),
          horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
          listOf("Todas", "Fijadas", "Sistema KDF", "Android Apps").forEach { category ->
            val active = selectedCategory == category
            Box(
              modifier = Modifier
                .clip(RoundedCornerShape(8.dp))
                .background(
                  if (active) MetalSecondary.copy(alpha = 0.22f)
                  else DarkSurfaceElevated.copy(alpha = 0.60f)
                )
                .border(
                  width = if (active) 1.dp else 0.5.dp,
                  color = if (active) MetalSecondary else MetalPrimary.copy(alpha = 0.25f),
                  shape = RoundedCornerShape(8.dp)
                )
                .clickable { selectedCategory = category }
                .padding(horizontal = 10.dp, vertical = 3.dp)
            ) {
              Text(
                text = category,
                color = if (active) MetalSecondary else TextSub,
                fontSize = 10.sp,
                fontWeight = if (active) FontWeight.ExtraBold else FontWeight.Medium
              )
            }
          }
        }
      }

      // MIDDLE SECTION: Customizable App Grid (Installed Android Apps + KDF System Modules)
      LazyVerticalGrid(
        columns = GridCells.Fixed(UiThemeController.gridColumns),
        modifier = Modifier
          .weight(1f)
          .fillMaxWidth()
          .padding(vertical = 4.dp)
          .testTag("launcher_apps_grid"),
        contentPadding = PaddingValues(vertical = 4.dp, horizontal = 2.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
      ) {
        items(filteredApps, key = { it.id }) { app ->
          val isPinned = pinnedSet.contains(app.packageName)
          LauncherAppGridTile(
            app = app,
            isPinned = isPinned,
            iconShape = currentIconShape,
            iconSizeDp = UiThemeController.iconSizeDp,
            showLabel = UiThemeController.showAppLabels,
            onClick = {
              launchAppEntry(context, app)
            },
            onLongClick = {
              selectedAppForMenu = app
            }
          )
        }
      }

      // BOTTOM SECTION: Sleek, Ultra-Compact Floating Dock
      if (UiThemeController.showFloatingDock) {
        KdfCompactFloatingDock(
          onOpenPhone = { openSystemIntent(context, Intent(Intent.ACTION_DIAL)) },
          onOpenBrowser = {
            openSystemIntent(
              context,
              Intent(Intent.ACTION_VIEW, Uri.parse("https://www.google.com"))
            )
          },
          onOpenVoiceChat = onOpenVoiceChat,
          onOpenOrchestrator = onOpenOrchestrator,
          onOpenDeviceShield = onOpenDeviceShield,
          onOpenThemeStudio = onOpenThemeStudio
        )
      }
    }
  }
}

    // Long-Press App Options Modal
    selectedAppForMenu?.let { app ->
      LauncherAppActionDialog(
        app = app,
        isPinned = pinnedSet.contains(app.packageName),
        onDismiss = { selectedAppForMenu = null },
        onLaunch = {
          selectedAppForMenu = null
          launchAppEntry(context, app)
        },
        onTogglePin = {
          UiThemeController.togglePinnedPackage(context, app.packageName)
          selectedAppForMenu = null
        },
        onAppInfo = {
          selectedAppForMenu = null
          if (!app.isInternalModule) {
            try {
              val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                data = Uri.fromParts("package", app.packageName, null)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
              }
              context.startActivity(intent)
            } catch (_: Exception) {}
          } else {
            onOpenAppSettings()
          }
        },
        onUninstall = {
          selectedAppForMenu = null
          if (!app.isInternalModule) {
            try {
              val intent = Intent(Intent.ACTION_DELETE).apply {
                data = Uri.fromParts("package", app.packageName, null)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
              }
              context.startActivity(intent)
            } catch (_: Exception) {}
          }
        }
      )
    }
  }
}

@Composable
private fun KdfClockAndHudWidget(
  clockStyleId: String,
  timeStr: String,
  secondsStr: String,
  dateStr: String,
  batteryPct: Int,
  ramUsedPct: Int,
  ramFreeMb: Long,
  cpuLoad: Int,
  gpuLoad: Int,
  showTelemetry: Boolean,
  isDefaultHome: Boolean,
  onCycleClockStyle: () -> Unit,
  onBoostRam: () -> Unit,
  onSetDefaultHome: () -> Unit,
  onOpenQuickControls: () -> Unit,
  onOpenLogoStudio: () -> Unit = {}
) {
  Column(
    modifier = Modifier
      .fillMaxWidth()
      .clip(RoundedCornerShape(14.dp))
      .background(DarkSurfaceElevated.copy(alpha = UiThemeController.glassOpacity * 0.9f))
      .border(1.dp, MetalSecondary.copy(alpha = 0.45f), RoundedCornerShape(14.dp))
      .padding(horizontal = 12.dp, vertical = 8.dp)
  ) {
    Row(
      modifier = Modifier.fillMaxWidth(),
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.SpaceBetween
    ) {
      Row(verticalAlignment = Alignment.CenterVertically) {
        // Royal Crown & KDF Emblem
        Image(
          painter = painterResource(id = R.drawable.ic_kdf_logo),
          contentDescription = "Emblema Real KDF",
          contentScale = ContentScale.Crop,
          modifier = Modifier
            .size(36.dp)
            .clip(RoundedCornerShape(8.dp))
            .border(1.dp, MetalSecondary, RoundedCornerShape(8.dp))
            .clickable { onOpenLogoStudio() }
            .testTag("hud_royal_kdf_logo")
        )
        Spacer(modifier = Modifier.width(8.dp))

        // Clock Display (Tap to cycle clock style!)
        Row(
          verticalAlignment = Alignment.Bottom,
          modifier = Modifier
            .clickable { onCycleClockStyle() }
            .testTag("launcher_clock_widget")
        ) {
        when (clockStyleId) {
          "COMPACT_PILL" -> {
            Row(
              modifier = Modifier
                .clip(RoundedCornerShape(50))
                .background(MetalSecondary.copy(alpha = 0.18f))
                .border(1.dp, MetalSecondary, RoundedCornerShape(50))
                .padding(horizontal = 10.dp, vertical = 3.dp),
              verticalAlignment = Alignment.CenterVertically
            ) {
              Text(
                text = timeStr.ifEmpty { "12:00" },
                color = TextMain,
                fontSize = 18.sp,
                fontWeight = FontWeight.ExtraBold,
                fontFamily = FontFamily.Monospace
              )
              Spacer(modifier = Modifier.width(6.dp))
              Text(
                text = dateStr,
                color = MetalSecondary,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold
              )
            }
          }

          "MINIMAL_GLASS" -> {
            Column {
              Text(
                text = timeStr.ifEmpty { "12:00" },
                color = TextMain,
                fontSize = 26.sp,
                fontWeight = FontWeight.Light,
                letterSpacing = 2.sp
              )
              Text(
                text = dateStr.uppercase(),
                color = TextSub,
                fontSize = 9.sp,
                fontWeight = FontWeight.SemiBold,
                letterSpacing = 1.2.sp
              )
            }
          }

          "LUXURY_DIGITAL" -> {
            Column {
              Row(verticalAlignment = Alignment.Bottom) {
                Text(
                  text = timeStr.ifEmpty { "12:00" },
                  color = MetalSecondary,
                  fontSize = 26.sp,
                  fontWeight = FontWeight.ExtraBold
                )
                Text(
                  text = secondsStr,
                  color = MetalPrimary,
                  fontSize = 13.sp,
                  fontWeight = FontWeight.Bold,
                  modifier = Modifier.padding(bottom = 3.dp, start = 2.dp)
                )
              }
              Text(
                text = "CRONÓGRAFO IMPERIAL • $dateStr",
                color = TextSub,
                fontSize = 9.sp,
                fontWeight = FontWeight.Bold
              )
            }
          }

          "CHRONO_RING" -> {
            Row(verticalAlignment = Alignment.CenterVertically) {
              Box(
                modifier = Modifier
                  .size(40.dp)
                  .border(2.dp, MetalSecondary, CircleShape),
                contentAlignment = Alignment.Center
              ) {
                Text(
                  text = timeStr.ifEmpty { "12:00" },
                  color = TextMain,
                  fontSize = 11.sp,
                  fontWeight = FontWeight.ExtraBold
                )
              }
              Spacer(modifier = Modifier.width(8.dp))
              Column {
                Text("LAUNCH KDF ORBIT", color = MetalSecondary, fontSize = 10.sp, fontWeight = FontWeight.ExtraBold)
                Text(dateStr, color = TextSub, fontSize = 10.sp)
              }
            }
          }

          else -> {
            // CYBER_HUD default
            Column {
              Row(verticalAlignment = Alignment.Bottom) {
                Text(
                  text = timeStr.ifEmpty { "12:00" },
                  color = TextMain,
                  fontSize = 24.sp,
                  fontWeight = FontWeight.Black,
                  fontFamily = FontFamily.Monospace
                )
                Text(
                  text = secondsStr,
                  color = MetalSecondary,
                  fontSize = 12.sp,
                  fontWeight = FontWeight.Bold,
                  fontFamily = FontFamily.Monospace,
                  modifier = Modifier.padding(bottom = 3.dp, start = 2.dp)
                )
              }
              Text(
                text = dateStr,
                color = MetalPrimary,
                fontSize = 10.sp,
                fontWeight = FontWeight.SemiBold
              )
            }
          }
        }
      }
    }

      // Right Actions: Turbo RAM Booster + Control Center Toggle + Default Home Launcher Button
      Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
      ) {
        if (!isDefaultHome) {
          Box(
            modifier = Modifier
              .clip(RoundedCornerShape(8.dp))
              .background(MetalSecondary.copy(alpha = 0.2f))
              .border(1.dp, MetalSecondary, RoundedCornerShape(8.dp))
              .clickable { onSetDefaultHome() }
              .padding(horizontal = 7.dp, vertical = 4.dp)
              .testTag("set_default_home_button")
          ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
              Icon(
                Icons.Default.Home,
                contentDescription = "Fijar como Launcher Predeterminado",
                tint = MetalSecondary,
                modifier = Modifier.size(13.dp)
              )
              Spacer(modifier = Modifier.width(3.dp))
              Text(
                "FIJAR INICIO",
                color = TextMain,
                fontSize = 9.sp,
                fontWeight = FontWeight.ExtraBold
              )
            }
          }
        }

        // Turbo RAM Boost Pill
        Box(
          modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(DarkSurface)
            .border(0.8.dp, ShieldGreen.copy(alpha = 0.7f), RoundedCornerShape(8.dp))
            .clickable { onBoostRam() }
            .padding(horizontal = 7.dp, vertical = 4.dp)
            .testTag("turbo_ram_boost_button")
        ) {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
              Icons.Default.Bolt,
              contentDescription = "Optimizar RAM",
              tint = ShieldGreen,
              modifier = Modifier.size(13.dp)
            )
            Spacer(modifier = Modifier.width(2.dp))
            Text(
              "TURBO",
              color = TextMain,
              fontSize = 9.sp,
              fontWeight = FontWeight.ExtraBold
            )
          }
        }

        // Quick System Control Center Toggle
        Box(
          modifier = Modifier
            .size(28.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(DarkSurface)
            .border(0.8.dp, MetalPrimary.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
            .clickable { onOpenQuickControls() }
            .testTag("toggle_control_center_button"),
          contentAlignment = Alignment.Center
        ) {
          Icon(
            Icons.Default.Tune,
            contentDescription = "Centro de Control del Sistema",
            tint = MetalSecondary,
            modifier = Modifier.size(15.dp)
          )
        }
      }
    }

    if (showTelemetry) {
      Spacer(modifier = Modifier.height(6.dp))
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        TelemetryMiniBadge(label = "BAT", value = "$batteryPct%", accent = ShieldGreen)
        TelemetryMiniBadge(label = "RAM", value = "$ramUsedPct% (${ramFreeMb}M)", accent = MetalSecondary)
        TelemetryMiniBadge(label = "CPU", value = "$cpuLoad%", accent = MetalPrimary)
        TelemetryMiniBadge(label = "GPU", value = "$gpuLoad%", accent = Color(0xFF38BDF8))
      }
    }
  }
}

@Composable
private fun TelemetryMiniBadge(label: String, value: String, accent: Color) {
  Row(verticalAlignment = Alignment.CenterVertically) {
    Box(
      modifier = Modifier
        .size(6.dp)
        .background(accent, CircleShape)
    )
    Spacer(modifier = Modifier.width(4.dp))
    Text(
      text = "$label: ",
      color = TextSub,
      fontSize = 9.sp,
      fontWeight = FontWeight.Bold
    )
    Text(
      text = value,
      color = TextMain,
      fontSize = 9.sp,
      fontWeight = FontWeight.ExtraBold,
      fontFamily = FontFamily.Monospace
    )
  }
}

@Composable
private fun KdfCompactSystemControlDeck(
  context: Context,
  onOpenThemeStudio: () -> Unit,
  onSetDefaultHome: () -> Unit,
  onOpenNotificationTones: () -> Unit = {},
  onOpenLogoStudio: () -> Unit = {}
) {
  Row(
    modifier = Modifier
      .fillMaxWidth()
      .clip(RoundedCornerShape(12.dp))
      .background(DarkSurface.copy(alpha = 0.94f))
      .border(1.dp, MetalSecondary.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
      .padding(horizontal = 8.dp, vertical = 6.dp)
      .horizontalScroll(rememberScrollState()),
    horizontalArrangement = Arrangement.spacedBy(8.dp),
    verticalAlignment = Alignment.CenterVertically
  ) {
    SystemControlChip(
      icon = Icons.Default.GraphicEq,
      label = "Tonos 4K",
      onClick = onOpenNotificationTones
    )
    SystemControlChip(
      icon = Icons.Default.Diamond,
      label = "Logo & CSS",
      onClick = onOpenLogoStudio
    )
    SystemControlChip(
      icon = Icons.Default.Wifi,
      label = "Wi-Fi",
      onClick = { openSystemIntent(context, Intent(Settings.ACTION_WIFI_SETTINGS)) }
    )
    SystemControlChip(
      icon = Icons.Default.Bluetooth,
      label = "Bluetooth",
      onClick = { openSystemIntent(context, Intent(Settings.ACTION_BLUETOOTH_SETTINGS)) }
    )
    SystemControlChip(
      icon = Icons.Default.Brightness6,
      label = "Pantalla",
      onClick = { openSystemIntent(context, Intent(Settings.ACTION_DISPLAY_SETTINGS)) }
    )
    SystemControlChip(
      icon = Icons.AutoMirrored.Filled.VolumeUp,
      label = "Sonido",
      onClick = { openSystemIntent(context, Intent(Settings.ACTION_SOUND_SETTINGS)) }
    )
    SystemControlChip(
      icon = Icons.Default.Home,
      label = "App Inicio",
      onClick = onSetDefaultHome
    )
    SystemControlChip(
      icon = Icons.Default.Palette,
      label = "Estudio KDF",
      onClick = onOpenThemeStudio
    )
    SystemControlChip(
      icon = Icons.Default.Settings,
      label = "Android",
      onClick = { openSystemIntent(context, Intent(Settings.ACTION_SETTINGS)) }
    )
  }
}

@Composable
private fun SystemControlChip(
  icon: ImageVector,
  label: String,
  onClick: () -> Unit
) {
  Row(
    modifier = Modifier
      .clip(RoundedCornerShape(8.dp))
      .background(DarkSurfaceElevated)
      .border(0.7.dp, MetalPrimary.copy(alpha = 0.35f), RoundedCornerShape(8.dp))
      .clickable { onClick() }
      .padding(horizontal = 8.dp, vertical = 5.dp),
    verticalAlignment = Alignment.CenterVertically
  ) {
    Icon(icon, contentDescription = label, tint = MetalSecondary, modifier = Modifier.size(14.dp))
    Spacer(modifier = Modifier.width(4.dp))
    Text(label, color = TextMain, fontSize = 10.sp, fontWeight = FontWeight.Bold)
  }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun LauncherAppGridTile(
  app: LauncherAppEntry,
  isPinned: Boolean,
  iconShape: Shape,
  iconSizeDp: Float,
  showLabel: Boolean,
  onClick: () -> Unit,
  onLongClick: () -> Unit
) {
  val gold = MetalGold
  val silver = MetalSilver
  val themeSecondary = MetalSecondary
  val themePrimary = MetalPrimary
  val iconPackStyleId = UiThemeController.iconPackStyleId
  val applyThemeTint = UiThemeController.applyThemeTintToAppIcons

  val containerGradient = when (iconPackStyleId) {
    "NOB_PSICO" -> listOf(Color(0xFF06020C), Color(0xFF240747), Color(0xFF090314))
    "METRIX_RELATIV" -> listOf(Color(0xFF010A03), Color(0xFF062E12), Color(0xFF020F05))
    "ADN_PINK" -> listOf(Color(0xFF0A0207), Color(0xFF3B0624), Color(0xFF0F030B))
    "SKY_ETER" -> listOf(Color(0xFF07223B), Color(0xFF0F4C81), Color(0xFF1E3A5F))
    "DRAGON_FIRE" -> listOf(Color(0xFF140302), Color(0xFF4C0D06), Color(0xFF1F0603))
    "QUANTUM_VENOM" -> listOf(Color(0xFF040D02), Color(0xFF1B3B08), Color(0xFF071404))
    "ELECTRO_ARCADE" -> listOf(Color(0xFF090318), Color(0xFF2E0B52), Color(0xFF0B1F3A))
    "SOLAR_PHARAOH" -> listOf(Color(0xFF0D0902), Color(0xFF382606), Color(0xFF171003))
    "COSMIC_NEBULA" -> listOf(Color(0xFF050414), Color(0xFF1E1552), Color(0xFF0A0824))
    else -> listOf(DarkSurfaceElevated, DarkSurface)
  }

  Column(
    modifier = Modifier
      .fillMaxWidth()
      .clip(RoundedCornerShape(12.dp))
      .combinedClickable(
        onClick = onClick,
        onLongClick = onLongClick
      )
      .padding(vertical = 4.dp, horizontal = 2.dp)
      .testTag("launcher_app_${app.id}"),
    horizontalAlignment = Alignment.CenterHorizontally
  ) {
    Box(contentAlignment = Alignment.TopEnd) {
      Box(
        modifier = Modifier
          .size(iconSizeDp.dp)
          .clip(iconShape)
          .background(brush = Brush.linearGradient(colors = containerGradient))
          .border(
            width = if (app.isInternalModule || applyThemeTint) 1.6.dp else 1.1.dp,
            brush = Brush.sweepGradient(listOf(gold, themeSecondary, silver, themePrimary, gold)),
            shape = iconShape
          ),
        contentAlignment = Alignment.Center
      ) {
        if (applyThemeTint || app.isInternalModule) {
          ComposeCanvas(modifier = Modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height
            val center = Offset(w * 0.5f, h * 0.5f)
            drawCircle(
              color = gold.copy(alpha = 0.45f),
              radius = w * 0.37f,
              center = center,
              style = Stroke(width = 1.6f)
            )
            drawCircle(
              color = silver.copy(alpha = 0.38f),
              radius = w * 0.27f,
              center = center,
              style = Stroke(width = 1.2f)
            )
          }
        }

        if (app.appIconBitmap != null) {
          Box(
            modifier = Modifier
              .fillMaxSize()
              .padding(if (applyThemeTint) 7.dp else 5.dp),
            contentAlignment = Alignment.Center
          ) {
            Image(
              bitmap = app.appIconBitmap,
              contentDescription = app.label,
              modifier = Modifier
                .fillMaxSize()
                .clip(iconShape)
            )
            if (applyThemeTint) {
              Box(
                modifier = Modifier
                  .fillMaxSize()
                  .clip(iconShape)
                  .background(
                    Brush.linearGradient(
                      colors = listOf(
                        themeSecondary.copy(alpha = 0.14f),
                        Color.Transparent,
                        gold.copy(alpha = 0.16f)
                      )
                    )
                  )
              )
            }
          }
        } else {
          Icon(
            imageVector = app.internalIcon,
            contentDescription = app.label,
            tint = app.accentColor,
            modifier = Modifier.size((iconSizeDp * 0.50f).dp)
          )
        }
      }

      if (isPinned) {
        Box(
          modifier = Modifier
            .size(13.dp)
            .background(gold, CircleShape)
            .border(1.dp, Color.Black, CircleShape),
          contentAlignment = Alignment.Center
        ) {
          Icon(
            Icons.Default.Star,
            contentDescription = "Fijada",
            tint = Color.Black,
            modifier = Modifier.size(8.dp)
          )
        }
      } else if (applyThemeTint) {
        Box(
          modifier = Modifier
            .size(10.dp)
            .background(Brush.linearGradient(listOf(gold, silver)), CircleShape)
            .border(0.8.dp, Color.Black, CircleShape)
        )
      }
    }

    if (showLabel) {
      Spacer(modifier = Modifier.height(4.dp))
      Text(
        text = app.label,
        color = TextMain,
        fontSize = 10.sp,
        fontWeight = FontWeight.SemiBold,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis,
        textAlign = TextAlign.Center
      )
    }
  }
}

@Composable
private fun KdfCompactFloatingDock(
  onOpenPhone: () -> Unit,
  onOpenBrowser: () -> Unit,
  onOpenVoiceChat: () -> Unit,
  onOpenOrchestrator: () -> Unit,
  onOpenDeviceShield: () -> Unit,
  onOpenThemeStudio: () -> Unit
) {
  Row(
    modifier = Modifier
      .fillMaxWidth()
      .height(UiThemeController.navigationBarHeight.dp)
      .padding(horizontal = 8.dp, vertical = 2.dp)
      .clip(RoundedCornerShape(20.dp))
      .background(DarkSurfaceElevated.copy(alpha = (UiThemeController.glassOpacity + 0.08f).coerceAtMost(1f)))
      .border(
        width = 1.2.dp,
        brush = Brush.horizontalGradient(listOf(MetalPrimary, MetalSecondary, MetalPrimary)),
        shape = RoundedCornerShape(20.dp)
      )
      .padding(horizontal = 12.dp, vertical = 6.dp)
      .testTag("launcher_floating_dock"),
    horizontalArrangement = Arrangement.SpaceEvenly,
    verticalAlignment = Alignment.CenterVertically
  ) {
    DockCompactIcon(
      icon = Icons.Default.Phone,
      label = "Teléfono",
      tint = Color(0xFF10B981),
      onClick = onOpenPhone
    )
    DockCompactIcon(
      icon = Icons.Default.Language,
      label = "Web",
      tint = Color(0xFF38BDF8),
      onClick = onOpenBrowser
    )
    DockCompactIcon(
      icon = Icons.Default.GraphicEq,
      label = "Voz IA",
      tint = MetalSecondary,
      onClick = onOpenVoiceChat
    )
    DockCompactIcon(
      icon = Icons.Default.Hub,
      label = "Orquestador",
      tint = Color(0xFFA855F7),
      onClick = onOpenOrchestrator
    )
    DockCompactIcon(
      icon = Icons.Default.Shield,
      label = "Escudo",
      tint = Color(0xFF00F5D4),
      onClick = onOpenDeviceShield
    )
    DockCompactIcon(
      icon = Icons.Default.Palette,
      label = "Temas",
      tint = Color(0xFFF43F5E),
      onClick = onOpenThemeStudio
    )
  }
}

@Composable
private fun DockCompactIcon(
  icon: ImageVector,
  label: String,
  tint: Color,
  onClick: () -> Unit
) {
  Column(
    modifier = Modifier
      .clip(RoundedCornerShape(10.dp))
      .clickable { onClick() }
      .padding(horizontal = 6.dp, vertical = 2.dp),
    horizontalAlignment = Alignment.CenterHorizontally
  ) {
    Box(
      modifier = Modifier
        .size(34.dp)
        .clip(RoundedCornerShape(10.dp))
        .background(DarkSurface)
        .border(0.8.dp, tint.copy(alpha = 0.65f), RoundedCornerShape(10.dp)),
      contentAlignment = Alignment.Center
    ) {
      Icon(
        imageVector = icon,
        contentDescription = label,
        tint = tint,
        modifier = Modifier.size(18.dp)
      )
    }
    Text(
      text = label,
      color = TextSub,
      fontSize = 8.sp,
      fontWeight = FontWeight.Bold
    )
  }
}

@Composable
private fun LauncherAppActionDialog(
  app: LauncherAppEntry,
  isPinned: Boolean,
  onDismiss: () -> Unit,
  onLaunch: () -> Unit,
  onTogglePin: () -> Unit,
  onAppInfo: () -> Unit,
  onUninstall: () -> Unit
) {
  AlertDialog(
    onDismissRequest = onDismiss,
    containerColor = DarkSurfaceElevated,
    titleContentColor = TextMain,
    textContentColor = TextSub,
    title = {
      Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(app.internalIcon, contentDescription = null, tint = MetalSecondary, modifier = Modifier.size(20.dp))
        Spacer(modifier = Modifier.width(8.dp))
        Column {
          Text(app.label, fontWeight = FontWeight.ExtraBold, fontSize = 14.sp, color = TextMain)
          Text(app.packageName, fontSize = 10.sp, color = TextSub)
        }
      }
    },
    text = {
      Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        OutlinedButton(onClick = onLaunch, modifier = Modifier.fillMaxWidth()) {
          Icon(Icons.Default.PlayArrow, contentDescription = null, tint = MetalSecondary)
          Spacer(modifier = Modifier.width(6.dp))
          Text("Abrir aplicación", color = TextMain)
        }
        OutlinedButton(onClick = onTogglePin, modifier = Modifier.fillMaxWidth()) {
          Icon(Icons.Default.Star, contentDescription = null, tint = MetalSecondary)
          Spacer(modifier = Modifier.width(6.dp))
          Text(if (isPinned) "Quitar de Fijadas" else "Fijar en Inicio KDF", color = TextMain)
        }
        OutlinedButton(onClick = onAppInfo, modifier = Modifier.fillMaxWidth()) {
          Icon(Icons.Default.Info, contentDescription = null, tint = MetalPrimary)
          Spacer(modifier = Modifier.width(6.dp))
          Text("Información del Sistema", color = TextMain)
        }
        if (!app.isInternalModule) {
          OutlinedButton(onClick = onUninstall, modifier = Modifier.fillMaxWidth()) {
            Icon(Icons.Default.Delete, contentDescription = null, tint = DangerRed)
            Spacer(modifier = Modifier.width(6.dp))
            Text("Desinstalar paquete", color = DangerRed)
          }
        }
      }
    },
    confirmButton = {
      TextButton(onClick = onDismiss) {
        Text("Cerrar", color = MetalSecondary, fontWeight = FontWeight.Bold)
      }
    }
  )
}

@Composable
fun LiveLauncherWallpaperCanvas(
  effectId: String,
  animationSpeed: Float,
  modifier: Modifier = Modifier
) {
  val infiniteTransition = rememberInfiniteTransition(label = "kdf_wallpaper_anim")
  val durationMs = (9000f / animationSpeed.coerceAtLeast(0.2f)).toInt().coerceIn(2000, 30000)
  val phase by infiniteTransition.animateFloat(
    initialValue = 0f,
    targetValue = (2f * PI).toFloat(),
    animationSpec = infiniteRepeatable(
      animation = tween(durationMillis = durationMs, easing = LinearEasing),
      repeatMode = RepeatMode.Restart
    ),
    label = "wallpaper_phase"
  )

  val primaryColor = MetalPrimary
  val secondaryColor = MetalSecondary
  val goldColor = MetalGold
  val silverColor = MetalSilver
  val surfaceColor = SurfacePearl
  val deepSurface = DarkSurface

  ComposeCanvas(modifier = modifier) {
    val w = size.width
    val h = size.height

    // Base rich gradient
    drawRect(
      brush = Brush.verticalGradient(
        colors = listOf(surfaceColor, deepSurface, surfaceColor)
      )
    )

    when (effectId) {
      "PSICO_VORTEX" -> {
        val center = Offset(w * 0.5f, h * 0.46f)
        val maxR = w * 0.62f
        val rings = 14
        for (i in 1..rings) {
          val frac = i.toFloat() / rings
          val r = maxR * frac
          val wobbleX = cos(phase * 1.5f + i * 0.6f) * 18f * (1f - frac * 0.5f)
          val wobbleY = sin(phase * 1.3f + i * 0.6f) * 18f * (1f - frac * 0.5f)
          val ringColor = when (i % 4) {
            0 -> goldColor.copy(alpha = 0.36f)
            1 -> secondaryColor.copy(alpha = 0.40f)
            2 -> silverColor.copy(alpha = 0.32f)
            else -> primaryColor.copy(alpha = 0.28f)
          }
          drawCircle(
            color = ringColor,
            radius = r,
            center = Offset(center.x + wobbleX, center.y + wobbleY),
            style = Stroke(width = 2.4f + (i % 3))
          )
        }
        for (orb in 0 until 16) {
          val angle = phase * (if (orb % 2 == 0) 1f else -1f) + (orb * PI.toFloat() / 8f)
          val dist = (w * 0.10f) + (orb % 6) * (w * 0.075f)
          val pos = Offset(center.x + cos(angle) * dist, center.y + sin(angle) * dist)
          drawCircle(
            color = if (orb % 2 == 0) goldColor.copy(alpha = 0.78f) else silverColor.copy(alpha = 0.72f),
            radius = if (orb % 3 == 0) 5.5f else 3.5f,
            center = pos
          )
        }
      }

      "METRIX_CODE_RAIN" -> {
        val columns = 16
        val colWidth = w / columns
        val normPhase = phase / (2f * PI.toFloat())
        for (c in 0 until columns) {
          val x = colWidth * (c + 0.5f)
          val speedFactor = 1f + (c % 4) * 0.35f
          val headProgress = ((normPhase * speedFactor) + (c * 0.17f)) % 1f
          val headY = headProgress * h
          val tailLen = h * (0.22f + (c % 3) * 0.08f)
          val streamColor = when (c % 4) {
            0 -> goldColor
            2 -> silverColor
            else -> secondaryColor
          }
          drawLine(
            brush = Brush.verticalGradient(
              colors = listOf(Color.Transparent, streamColor.copy(alpha = 0.52f), goldColor.copy(alpha = 0.88f)),
              startY = (headY - tailLen).coerceAtLeast(0f),
              endY = headY
            ),
            start = Offset(x, (headY - tailLen).coerceAtLeast(0f)),
            end = Offset(x, headY),
            strokeWidth = 3.2f
          )
          for (seg in 0 until 6) {
            val segY = (headY - seg * 26f)
            if (segY in 0f..h) {
              drawCircle(
                color = if (seg == 0) silverColor.copy(alpha = 0.92f) else streamColor.copy(alpha = 0.48f),
                radius = if (seg == 0) 4.2f else 2.5f,
                center = Offset(x, segY)
              )
            }
          }
        }
      }

      "ADN_PINK_HELIX" -> {
        val nodes = 22
        val stepY = h / nodes
        for (i in 0..nodes) {
          val y = i * stepY
          val waveAngle = phase * 1.4f + i * 0.42f
          val x1 = w * 0.5f + sin(waveAngle) * (w * 0.28f)
          val x2 = w * 0.5f + sin(waveAngle + PI.toFloat()) * (w * 0.28f)
          val bridgeColor = if (i % 2 == 0) silverColor.copy(alpha = 0.34f) else goldColor.copy(alpha = 0.36f)
          drawLine(
            color = bridgeColor,
            start = Offset(x1, y),
            end = Offset(x2, y),
            strokeWidth = 2.0f
          )
          drawCircle(
            color = secondaryColor.copy(alpha = 0.80f),
            radius = 6.5f,
            center = Offset(x1, y)
          )
          drawCircle(
            color = goldColor.copy(alpha = 0.92f),
            radius = 3.0f,
            center = Offset(x1, y)
          )
          drawCircle(
            color = silverColor.copy(alpha = 0.82f),
            radius = 5.5f,
            center = Offset(x2, y)
          )
        }
      }

      "SKY_ETER_CELESTIAL" -> {
        val sunCenter = Offset(w * 0.5f, h * 0.32f)
        drawCircle(
          brush = Brush.radialGradient(
            colors = listOf(
              Color.White.copy(alpha = 0.45f),
              goldColor.copy(alpha = 0.30f),
              secondaryColor.copy(alpha = 0.20f),
              Color.Transparent
            ),
            center = sunCenter,
            radius = w * 0.52f
          ),
          center = sunCenter,
          radius = w * 0.52f
        )
        for (r in 1..4) {
          val radius = w * (0.11f * r)
          drawCircle(
            color = if (r % 2 == 0) goldColor.copy(alpha = 0.40f) else silverColor.copy(alpha = 0.44f),
            radius = radius,
            center = sunCenter,
            style = Stroke(width = 1.6f)
          )
          val orbAngle = phase * (if (r % 2 == 0) 1f else -0.8f) + r
          val orbPos = Offset(sunCenter.x + cos(orbAngle) * radius, sunCenter.y + sin(orbAngle) * radius)
          drawCircle(
            color = if (r % 2 == 0) goldColor else Color.White,
            radius = 4.8f,
            center = orbPos
          )
        }
        for (wave in 0..3) {
          val path = Path()
          val baseY = h * (0.52f + wave * 0.11f)
          path.moveTo(0f, baseY)
          var x = 0f
          val step = (w / 16f).coerceAtLeast(16f)
          while (x <= w + step) {
            val y = baseY + sin(phase + (x / w) * 3.5f + wave) * 28f
            path.lineTo(x, y)
            x += step
          }
          val waveCol = when (wave) {
            0 -> Color.White.copy(alpha = 0.38f)
            1 -> goldColor.copy(alpha = 0.32f)
            2 -> silverColor.copy(alpha = 0.36f)
            else -> secondaryColor.copy(alpha = 0.34f)
          }
          drawPath(path = path, color = waveCol, style = Stroke(width = 3.2f))
        }
      }

      "DRAGON_FIRE_EMBERS" -> {
        val normPhase = phase / (2f * PI.toFloat())
        drawCircle(
          brush = Brush.radialGradient(
            colors = listOf(
              goldColor.copy(alpha = 0.38f),
              secondaryColor.copy(alpha = 0.32f),
              primaryColor.copy(alpha = 0.24f),
              Color.Transparent
            ),
            center = Offset(w * 0.5f, h * 0.85f),
            radius = w * 0.65f
          ),
          center = Offset(w * 0.5f, h * 0.85f),
          radius = w * 0.65f
        )
        for (flame in 0..4) {
          val path = Path()
          val baseX = w * (0.15f + flame * 0.175f)
          path.moveTo(baseX, h)
          var y = h
          val step = (h / 18f).coerceAtLeast(18f)
          while (y >= h * 0.15f) {
            val progress = 1f - (y / h)
            val sway = sin(phase * 1.6f + progress * 5f + flame) * (36f * progress)
            path.lineTo(baseX + sway, y)
            y -= step
          }
          val flameColor = when (flame % 4) {
            0 -> secondaryColor.copy(alpha = 0.36f)
            1 -> goldColor.copy(alpha = 0.40f)
            2 -> primaryColor.copy(alpha = 0.36f)
            else -> silverColor.copy(alpha = 0.32f)
          }
          drawPath(path = path, color = flameColor, style = Stroke(width = 3.5f))
        }
        for (spark in 0 until 24) {
          val p = ((normPhase * (1f + (spark % 3) * 0.4f)) + spark * 0.09f) % 1f
          val sy = h * (1f - p)
          val sx = (w * ((spark * 0.043f) % 1f)) + sin(phase + spark) * 24f
          val sparkCol = when (spark % 3) {
            0 -> goldColor.copy(alpha = (1f - p) * 0.88f)
            1 -> silverColor.copy(alpha = (1f - p) * 0.80f)
            else -> secondaryColor.copy(alpha = (1f - p) * 0.82f)
          }
          drawCircle(
            color = sparkCol,
            radius = 3.2f + (spark % 3) * 1.4f,
            center = Offset(sx, sy)
          )
        }
      }

      "QUANTUM_ATOM" -> {
        val center = Offset(w * 0.5f, h * 0.46f)
        drawCircle(
          brush = Brush.radialGradient(
            colors = listOf(goldColor.copy(alpha = 0.45f), secondaryColor.copy(alpha = 0.20f), Color.Transparent),
            center = center,
            radius = w * 0.25f
          ),
          center = center,
          radius = w * 0.25f
        )
        val numOrbits = 4
        for (o in 0 until numOrbits) {
          val orbitAngle = (o * PI / numOrbits).toFloat()
          val path = Path()
          val steps = 48
          for (s in 0..steps) {
            val t = (s.toFloat() / steps) * (2f * PI.toFloat())
            val rx = cos(t) * (w * 0.40f)
            val ry = sin(t) * (w * 0.14f)
            val rotX = rx * cos(orbitAngle) - ry * sin(orbitAngle)
            val rotY = rx * sin(orbitAngle) + ry * cos(orbitAngle)
            val pt = Offset(center.x + rotX, center.y + rotY)
            if (s == 0) path.moveTo(pt.x, pt.y) else path.lineTo(pt.x, pt.y)
          }
          val col = when (o % 3) {
            0 -> secondaryColor.copy(alpha = 0.36f)
            1 -> goldColor.copy(alpha = 0.38f)
            else -> silverColor.copy(alpha = 0.36f)
          }
          drawPath(path = path, color = col, style = Stroke(width = 2.0f))
          val eAngle = phase * 1.5f + o * 1.2f
          val erx = cos(eAngle) * (w * 0.40f)
          val ery = sin(eAngle) * (w * 0.14f)
          val ePos = Offset(
            center.x + erx * cos(orbitAngle) - ery * sin(orbitAngle),
            center.y + erx * sin(orbitAngle) + ery * cos(orbitAngle)
          )
          drawCircle(
            color = if (o % 2 == 0) goldColor else silverColor,
            radius = 5.5f,
            center = ePos
          )
        }
      }

      "ARCADE_SYNTH_GRID" -> {
        val horizonY = h * 0.38f
        drawCircle(
          brush = Brush.verticalGradient(
            colors = listOf(goldColor.copy(alpha = 0.55f), secondaryColor.copy(alpha = 0.40f), Color.Transparent),
            startY = horizonY - w * 0.28f,
            endY = horizonY + w * 0.08f
          ),
          radius = w * 0.26f,
          center = Offset(w * 0.5f, horizonY - w * 0.05f)
        )
        val numLines = 12
        for (i in -numLines..numLines) {
          val startX = w * 0.5f + (i * (w / numLines) * 0.22f)
          val endX = w * 0.5f + (i * (w / numLines) * 1.65f)
          drawLine(
            color = if (i % 2 == 0) goldColor.copy(alpha = 0.25f) else silverColor.copy(alpha = 0.22f),
            start = Offset(startX, horizonY),
            end = Offset(endX, h),
            strokeWidth = 1.6f
          )
        }
        val sweepCount = 10
        val normalizedPhase = (phase / (2f * PI.toFloat()))
        for (j in 0 until sweepCount) {
          val progress = ((j.toFloat() / sweepCount) + normalizedPhase) % 1f
          val y = horizonY + (h - horizonY) * (progress * progress)
          drawLine(
            color = secondaryColor.copy(alpha = (0.10f + progress * 0.26f).coerceAtMost(0.40f)),
            start = Offset(0f, y),
            end = Offset(w, y),
            strokeWidth = 2.0f
          )
        }
      }

      "CYBER_GRID" -> {
        val horizonY = h * 0.35f
        val numLines = 12
        for (i in -numLines..numLines) {
          val startX = w * 0.5f + (i * (w / numLines) * 0.25f)
          val endX = w * 0.5f + (i * (w / numLines) * 1.6f)
          drawLine(
            color = primaryColor.copy(alpha = 0.16f),
            start = Offset(startX, horizonY),
            end = Offset(endX, h),
            strokeWidth = 1.5f
          )
        }
        val sweepCount = 10
        val normalizedPhase = (phase / (2f * PI.toFloat()))
        for (j in 0 until sweepCount) {
          val progress = ((j.toFloat() / sweepCount) + normalizedPhase) % 1f
          val y = horizonY + (h - horizonY) * (progress * progress)
          drawLine(
            color = secondaryColor.copy(alpha = (0.08f + progress * 0.22f).coerceAtMost(0.35f)),
            start = Offset(0f, y),
            end = Offset(w, y),
            strokeWidth = 1.8f
          )
        }
        // Glowing sun/core at horizon
        drawCircle(
          brush = Brush.radialGradient(
            colors = listOf(secondaryColor.copy(alpha = 0.28f), Color.Transparent),
            center = Offset(w * 0.5f, horizonY),
            radius = w * 0.38f
          ),
          center = Offset(w * 0.5f, horizonY),
          radius = w * 0.38f
        )
      }

      "PARTICLE_MATRIX" -> {
        val count = 18
        val points = ArrayList<Offset>(count)
        for (i in 0 until count) {
          val angle = phase + i * 0.7f
          val px = (w * (0.12f + (i * 0.048f) % 0.78f)) + cos(angle) * 28f
          val py = (h * (0.10f + ((i * 7) % 17) / 20f)) + sin(angle * 1.2f) * 34f
          points.add(Offset(px, py))
        }
        for (i in 0 until count) {
          for (j in i + 1 until count) {
            val p1 = points[i]
            val p2 = points[j]
            val dist = kotlin.math.hypot((p1.x - p2.x).toDouble(), (p1.y - p2.y).toDouble()).toFloat()
            if (dist < w * 0.35f) {
              val alpha = ((1f - dist / (w * 0.35f)) * 0.22f).coerceIn(0f, 0.25f)
              drawLine(
                color = primaryColor.copy(alpha = alpha),
                start = p1,
                end = p2,
                strokeWidth = 1.2f
              )
            }
          }
          drawCircle(
            color = secondaryColor.copy(alpha = 0.45f),
            radius = 3.5f,
            center = points[i]
          )
        }
      }

      "AURORA_WAVES" -> {
        for (wave in 0..3) {
          val path = Path()
          val baseY = h * (0.25f + wave * 0.16f)
          path.moveTo(0f, baseY)
          var x = 0f
          val step = (w / 16f).coerceAtLeast(16f)
          while (x <= w + step) {
            val y = baseY + sin(phase + (x / w) * 4f + wave) * 45f
            path.lineTo(x, y)
            x += step
          }
          val waveColor = if (wave % 2 == 0) secondaryColor else primaryColor
          drawPath(
            path = path,
            color = waveColor.copy(alpha = 0.22f),
            style = Stroke(width = 3.5f)
          )
        }
      }

      "LUXURY_MESH" -> {
        val spacing = (w / 5f).coerceAtLeast(60f)
        var y = 0f
        var row = 0
        while (y < h + spacing) {
          var x = if (row % 2 == 0) 0f else spacing * 0.5f
          while (x < w + spacing) {
            val pulse = (sin(phase + (x + y) * 0.005f) + 1f) * 0.5f
            drawCircle(
              color = secondaryColor.copy(alpha = 0.08f + pulse * 0.14f),
              radius = spacing * 0.36f,
              center = Offset(x, y),
              style = Stroke(width = 1.2f)
            )
            x += spacing
          }
          y += spacing * 0.86f
          row++
        }
      }

      "RADAR_HUD" -> {
        val center = Offset(w * 0.5f, h * 0.46f)
        val maxR = w * 0.44f
        for (r in 1..4) {
          drawCircle(
            color = primaryColor.copy(alpha = 0.16f),
            radius = maxR * (r / 4f),
            center = center,
            style = Stroke(width = 1.2f)
          )
        }
        val sweepEnd = Offset(
          center.x + cos(phase) * maxR,
          center.y + sin(phase) * maxR
        )
        drawLine(
          color = secondaryColor.copy(alpha = 0.55f),
          start = center,
          end = sweepEnd,
          strokeWidth = 2.5f
        )
      }

      "SOLAR_ORBIT" -> {
        val center = Offset(w * 0.5f, h * 0.45f)
        for (i in 1..5) {
          val radius = (w * 0.09f) * i
          drawCircle(
            color = primaryColor.copy(alpha = 0.14f),
            radius = radius,
            center = center,
            style = Stroke(width = 1.2f)
          )
          val angle = phase * (if (i % 2 == 0) 1f else -0.7f) + i
          val satCenter = Offset(
            center.x + cos(angle) * radius,
            center.y + sin(angle) * radius
          )
          drawCircle(
            color = secondaryColor.copy(alpha = 0.7f),
            radius = 5f,
            center = satCenter
          )
        }
      }

      else -> {
        // STATIC_CLEAN
      }
    }
  }
}

fun resolveLauncherIconShape(shapeId: String): Shape {
  return when (shapeId) {
    "CIRCLE" -> CircleShape
    "HEXAGON" -> GenericShape { size, _ ->
      val w = size.width
      val h = size.height
      moveTo(w * 0.5f, 0f)
      lineTo(w, h * 0.25f)
      lineTo(w, h * 0.75f)
      lineTo(w * 0.5f, h)
      lineTo(0f, h * 0.75f)
      lineTo(0f, h * 0.25f)
      close()
    }
    "DIAMOND" -> GenericShape { size, _ ->
      val w = size.width
      val h = size.height
      moveTo(w * 0.5f, 0f)
      lineTo(w, h * 0.5f)
      lineTo(w * 0.5f, h)
      lineTo(0f, h * 0.5f)
      close()
    }
    "TEARDROP" -> RoundedCornerShape(
      topStartPercent = 50,
      topEndPercent = 50,
      bottomStartPercent = 50,
      bottomEndPercent = 12
    )
    "BIMETALLIC_RING" -> CircleShape
    else -> RoundedCornerShape(26) // SQUIRCLE default
  }
}

private fun loadInstalledAndroidApps(context: Context): List<LauncherAppEntry> {
  return try {
    val pm = context.packageManager
    val mainIntent = Intent(Intent.ACTION_MAIN, null).apply {
      addCategory(Intent.CATEGORY_LAUNCHER)
    }
    val resolveInfos = pm.queryIntentActivities(mainIntent, 0)
    resolveInfos
      .mapNotNull { ri ->
        val pkg = ri.activityInfo?.packageName ?: return@mapNotNull null
        if (pkg == context.packageName) return@mapNotNull null
        val label = ri.loadLabel(pm)?.toString()?.takeIf { it.isNotBlank() } ?: pkg
        val iconDrawable = try {
          ri.loadIcon(pm)
        } catch (_: Exception) {
          null
        }
        val iconBitmap = iconDrawable?.let { drawableToImageBitmap(it) }
        LauncherAppEntry(
          id = "android_$pkg",
          label = label,
          packageName = pkg,
          category = "Android Apps",
          isInternalModule = false,
          appIconBitmap = iconBitmap
        )
      }
      .distinctBy { it.packageName }
      .sortedBy { it.label.lowercase() }
  } catch (_: Exception) {
    emptyList()
  }
}

private fun drawableToImageBitmap(drawable: Drawable): ImageBitmap? {
  return try {
    if (drawable is BitmapDrawable && drawable.bitmap != null) {
      return drawable.bitmap.asImageBitmap()
    }
    val width = drawable.intrinsicWidth.coerceIn(48, 192)
    val height = drawable.intrinsicHeight.coerceIn(48, 192)
    val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(bitmap)
    drawable.setBounds(0, 0, canvas.width, canvas.height)
    drawable.draw(canvas)
    bitmap.asImageBitmap()
  } catch (_: Exception) {
    null
  }
}

fun launchAppEntry(context: Context, app: LauncherAppEntry) {
  if (app.isInternalModule) {
    app.onInternalClick?.invoke()
    return
  }
  try {
    val launchIntent = context.packageManager.getLaunchIntentForPackage(app.packageName)
    if (launchIntent != null) {
      launchIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
      context.startActivity(launchIntent)
    } else {
      Toast.makeText(context, "Abriendo ${app.label}...", Toast.LENGTH_SHORT).show()
    }
  } catch (e: Exception) {
    Toast.makeText(context, "No se pudo iniciar ${app.label}", Toast.LENGTH_SHORT).show()
  }
}

fun openSystemIntent(context: Context, intent: Intent) {
  try {
    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    context.startActivity(intent)
  } catch (_: Exception) {
    try {
      val fallback = Intent(Settings.ACTION_SETTINGS).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
      context.startActivity(fallback)
    } catch (_: Exception) {}
  }
}

fun isKdfDefaultHomeLauncher(context: Context): Boolean {
  return try {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
      val roleManager = context.getSystemService(RoleManager::class.java)
      if (roleManager != null && roleManager.isRoleAvailable(RoleManager.ROLE_HOME)) {
        return roleManager.isRoleHeld(RoleManager.ROLE_HOME)
      }
    }
    val intent = Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_HOME)
    val res = context.packageManager.resolveActivity(intent, PackageManager.MATCH_DEFAULT_ONLY)
    res?.activityInfo?.packageName == context.packageName
  } catch (_: Exception) {
    false
  }
}

fun promptSetDefaultHomeLauncher(context: Context) {
  try {
    val intent = Intent(Settings.ACTION_HOME_SETTINGS).apply {
      addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    }
    context.startActivity(intent)
    Toast.makeText(
      context,
      "Selecciona 'LAUNCH KDF' como tu Aplicación de Inicio predeterminada",
      Toast.LENGTH_LONG
    ).show()
  } catch (_: Exception) {
    try {
      val intent = Intent(Settings.ACTION_SETTINGS).apply {
        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
      }
      context.startActivity(intent)
    } catch (_: Exception) {}
  }
}

@Composable
fun LauncherModeSelectorPill(
  currentMode: LauncherMode,
  onSelectMode: (LauncherMode) -> Unit
) {
  Row(
    modifier = Modifier
      .fillMaxWidth()
      .clip(RoundedCornerShape(8.dp))
      .background(DarkSurfaceElevated.copy(alpha = 0.92f))
      .border(0.8.dp, MetalSecondary.copy(alpha = 0.45f), RoundedCornerShape(8.dp))
      .padding(2.dp)
      .testTag("launcher_mode_selector_pill"),
    horizontalArrangement = Arrangement.spacedBy(3.dp)
  ) {
    LauncherMode.entries.forEach { mode ->
      val selected = currentMode == mode
      Box(
        modifier = Modifier
          .weight(1f)
          .clip(RoundedCornerShape(6.dp))
          .background(if (selected) MetalSecondary else Color.Transparent)
          .clickable { onSelectMode(mode) }
          .padding(vertical = 4.dp)
          .testTag("launcher_mode_tab_${mode.id}"),
        contentAlignment = Alignment.Center
      ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
          Text(mode.iconEmoji, fontSize = 11.sp)
          Spacer(modifier = Modifier.width(4.dp))
          Text(
            text = mode.label,
            color = if (selected) Color.Black else TextSub,
            fontSize = 9.5.sp,
            fontWeight = if (selected) FontWeight.ExtraBold else FontWeight.Bold
          )
        }
      }
    }
  }
}

