package com.example

import android.graphics.BitmapFactory
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Diamond
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Hub
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.MainViewModel
import com.example.ui.screens.AiSettingsDialog
import com.example.ui.screens.ApkShareDialog
import com.example.ui.screens.AppSettingsDialog
import com.example.ui.screens.ChatHistoryAndSandboxDialog
import com.example.ui.screens.DashboardScreen
import com.example.ui.screens.DeviceShieldScreen
import com.example.ui.screens.FloatingAssistantButtonOverlay
import com.example.ui.screens.HardwareAcceleratorDialog
import com.example.ui.screens.ImprovementFeedbackDialog
import com.example.ui.screens.InternalAiDialog
import com.example.ui.screens.KdfLauncherScreen
import com.example.ui.screens.LanguageSelectorDialog
import com.example.ui.screens.LiveConversationScreen
import com.example.ui.screens.LiveSessionDialog
import com.example.ui.screens.LogoAndCssStudioDialog
import com.example.ui.screens.NotificationTonesDialog
import com.example.ui.screens.OrchestratorScreen
import com.example.ui.screens.RiskScannerDialog
import com.example.ui.screens.ThemeStudioDialog
import com.example.ui.screens.promptSetDefaultHomeLauncher
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.DarkSurfaceElevated
import com.example.ui.theme.MetalPrimary
import com.example.ui.theme.MetalSecondary
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.theme.TextMain
import com.example.ui.theme.TextSub
import com.example.ui.theme.UiThemeController
import com.example.ui.theme.damaskBackground
import com.example.ui.theme.jewelButton
import java.io.File

class MainActivity : ComponentActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    UiThemeController.loadFromPreferences(this)
    enableEdgeToEdge()
    setContent {
      MyApplicationTheme {
        LinguaGuardianApp()
      }
    }
  }
}

enum class AppDestination(
  val route: String,
  val title: String,
  val icon: ImageVector
) {
  HOME_LAUNCHER("home_launcher", "Inicio KDF", Icons.Default.Home),
  LIVE_VOICE("live_voice", "Voz IA", Icons.Default.GraphicEq),
  DEVICE_SHIELD("device_shield", "Escudo", Icons.Default.Shield),
  ORCHESTRATOR("orchestrator", "Orquestador", Icons.Default.Hub)
}

@Composable
fun LinguaGuardianApp(viewModel: MainViewModel = viewModel()) {
  val context = LocalContext.current

  var currentDestination by rememberSaveable { mutableStateOf(AppDestination.HOME_LAUNCHER) }
  var showThemeStudioDialog by rememberSaveable { mutableStateOf(false) }
  var showApkShareDialog by rememberSaveable { mutableStateOf(false) }
  var showRiskScannerDialog by rememberSaveable { mutableStateOf(false) }
  var showHardwareAcceleratorDialog by rememberSaveable { mutableStateOf(false) }
  var showAppSettingsDialog by rememberSaveable { mutableStateOf(false) }
  var showInternalAiDialog by rememberSaveable { mutableStateOf(false) }
  var showAiSettingsDialog by rememberSaveable { mutableStateOf(false) }
  var showLanguageSelectorDialog by rememberSaveable { mutableStateOf(false) }
  var showChatHistoryDialog by rememberSaveable { mutableStateOf(false) }
  var showLiveSessionDialog by rememberSaveable { mutableStateOf(false) }
  var showImprovementDialog by rememberSaveable { mutableStateOf(false) }
  var showNotificationTonesDialog by rememberSaveable { mutableStateOf(false) }
  var showLogoStudioDialog by rememberSaveable { mutableStateOf(false) }
  var showDashboardDialog by rememberSaveable { mutableStateOf(false) }

  val selectedLanguage by viewModel.selectedLanguage.collectAsStateWithLifecycle()
  val selectedScenario by viewModel.selectedScenario.collectAsStateWithLifecycle()
  val messages by viewModel.messages.collectAsStateWithLifecycle()
  val practiceSessions by viewModel.practiceSessions.collectAsStateWithLifecycle()
  val evolutionLogs by viewModel.evolutionLogs.collectAsStateWithLifecycle()
  val isLoading by viewModel.isLoading.collectAsStateWithLifecycle()

  val fastLookupResult by viewModel.fastLookupResult.collectAsStateWithLifecycle()
  val isAnalyzingImage by viewModel.isAnalyzingImage.collectAsStateWithLifecycle()
  val imageAnalysisResult by viewModel.imageAnalysisResult.collectAsStateWithLifecycle()
  val isAnalyzingThreat by viewModel.isAnalyzingThreat.collectAsStateWithLifecycle()
  val threatAnalysisResult by viewModel.threatAnalysisResult.collectAsStateWithLifecycle()

  val generatedImage by viewModel.generatedImageBitmap.collectAsStateWithLifecycle()
  val isGeneratingImage by viewModel.isGeneratingImage.collectAsStateWithLifecycle()
  val imageError by viewModel.imageGenerationError.collectAsStateWithLifecycle()
  val orchestratorResult by viewModel.orchestratorResult.collectAsStateWithLifecycle()
  val isOrchestrating by viewModel.isOrchestrating.collectAsStateWithLifecycle()
  val orchestratorLogs by viewModel.orchestratorLogs.collectAsStateWithLifecycle()

  val autonomousState by viewModel.autonomousEngine.state.collectAsStateWithLifecycle()

  if (currentDestination != AppDestination.HOME_LAUNCHER) {
    BackHandler {
      currentDestination = AppDestination.HOME_LAUNCHER
    }
  }

  // Submenu Dialogs
  AppSettingsDialog(
    isOpen = showAppSettingsDialog,
    onDismiss = { showAppSettingsDialog = false },
    onOpenThemeStudio = { showThemeStudioDialog = true },
    onOpenNotificationTones = { showNotificationTonesDialog = true },
    onOpenLogoStudio = { showLogoStudioDialog = true },
    onOpenHardwareAccelerator = { showHardwareAcceleratorDialog = true },
    onOpenRiskScanner = { showRiskScannerDialog = true },
    onOpenDashboard = { showDashboardDialog = true },
    onSetDefaultHome = { promptSetDefaultHomeLauncher(context) }
  )

  InternalAiDialog(
    isOpen = showInternalAiDialog,
    onDismiss = { showInternalAiDialog = false }
  )

  AiSettingsDialog(
    isOpen = showAiSettingsDialog,
    ttsManager = viewModel.ttsManager,
    selectedLanguage = selectedLanguage,
    onDismiss = { showAiSettingsDialog = false }
  )

  LanguageSelectorDialog(
    isOpen = showLanguageSelectorDialog,
    currentLanguage = selectedLanguage,
    onDismiss = { showLanguageSelectorDialog = false },
    onSelectLanguage = { viewModel.selectLanguage(it) }
  )

  ChatHistoryAndSandboxDialog(
    isOpen = showChatHistoryDialog,
    messages = messages,
    practiceSessions = practiceSessions,
    currentLanguage = selectedLanguage,
    currentScenario = selectedScenario,
    onDismiss = { showChatHistoryDialog = false },
    onClearHistory = { viewModel.clearChat() },
    onNewChat = { viewModel.startNewPracticeSession(archiveCurrent = true) },
    onSaveCurrentSession = { customTitle ->
      viewModel.saveCurrentPracticeSession(customTitle)
    },
    onLoadSession = { session ->
      viewModel.loadPracticeSession(session)
    },
    onDeleteSession = { session ->
      viewModel.deletePracticeSession(session)
    },
    onClearAllSessions = {
      viewModel.clearAllPracticeSessions()
    }
  )

  LiveSessionDialog(
    isOpen = showLiveSessionDialog,
    selectedLanguage = selectedLanguage,
    ttsManager = viewModel.ttsManager,
    speechHelper = viewModel.speechHelper,
    onDismiss = { showLiveSessionDialog = false }
  )

  ThemeStudioDialog(
    isOpen = showThemeStudioDialog,
    onDismiss = { showThemeStudioDialog = false }
  )

  ImprovementFeedbackDialog(
    isOpen = showImprovementDialog,
    onDismiss = { showImprovementDialog = false },
    onSendFeedback = { feedback -> viewModel.addCustomRule(feedback) }
  )

  ApkShareDialog(
    isOpen = showApkShareDialog,
    onDismiss = { showApkShareDialog = false }
  )

  RiskScannerDialog(
    isOpen = showRiskScannerDialog,
    isLoading = isAnalyzingThreat,
    analysisResult = threatAnalysisResult,
    onDismiss = { showRiskScannerDialog = false },
    onAnalyze = { desc -> viewModel.analyzeThreat(desc) }
  )

  HardwareAcceleratorDialog(
    isOpen = showHardwareAcceleratorDialog,
    onDismiss = { showHardwareAcceleratorDialog = false }
  )

  NotificationTonesDialog(
    isOpen = showNotificationTonesDialog,
    onDismiss = { showNotificationTonesDialog = false }
  )

  LogoAndCssStudioDialog(
    isOpen = showLogoStudioDialog,
    onDismiss = { showLogoStudioDialog = false }
  )

  if (showDashboardDialog) {
    Dialog(onDismissRequest = { showDashboardDialog = false }) {
      DashboardScreen(onDismiss = { showDashboardDialog = false })
    }
  }

  val customBgPath = UiThemeController.customBackgroundUri
  val customBitmap = remember(customBgPath) {
    customBgPath?.let { path ->
      val file = File(path)
      if (file.exists()) BitmapFactory.decodeFile(file.absolutePath)?.asImageBitmap() else null
    }
  }

  Box(
    modifier = Modifier
      .fillMaxSize()
      .damaskBackground()
  ) {
    if (customBitmap != null) {
      Image(
        bitmap = customBitmap,
        contentDescription = null,
        contentScale = ContentScale.Crop,
        modifier = Modifier
          .fillMaxSize()
          .alpha(0.32f)
      )
    }

    Scaffold(
      modifier = Modifier.fillMaxSize(),
      containerColor = Color.Transparent,
      contentWindowInsets = WindowInsets.safeDrawing
    ) { innerPadding ->
      Column(
        modifier = Modifier
          .fillMaxSize()
          .padding(innerPadding)
          .consumeWindowInsets(innerPadding)
      ) {
        TopHeaderAndNavigationBar(
          versionName = autonomousState.currentVersionName,
          currentDestination = currentDestination,
          onSelectDestination = { currentDestination = it },
          onOpenThemeStudio = { showThemeStudioDialog = true },
          onOpenApkShare = { showApkShareDialog = true },
          onOpenRiskScanner = { showRiskScannerDialog = true },
          onOpenHardwareAccelerator = { showHardwareAcceleratorDialog = true },
          onOpenAppSettings = { showAppSettingsDialog = true },
          onOpenInternalAi = { showInternalAiDialog = true },
          onOpenAiSettings = { showAiSettingsDialog = true },
          onOpenLanguageSelector = { showLanguageSelectorDialog = true },
          onOpenHistory = { showChatHistoryDialog = true },
          onOpenImprovement = { showImprovementDialog = true },
          onOpenNotificationTones = { showNotificationTonesDialog = true },
          onOpenLogoStudio = { showLogoStudioDialog = true },
          onSetDefaultHome = { promptSetDefaultHomeLauncher(context) },
          currentAiEngine = viewModel.selectedAiEngine.collectAsStateWithLifecycle().value,
          onSetAiEngine = { viewModel.setSelectedAiEngine(it) }
        )

        Box(
          modifier = Modifier
            .weight(1f)
            .fillMaxWidth()
        ) {
          when (currentDestination) {
            AppDestination.HOME_LAUNCHER -> {
              KdfLauncherScreen(
                versionName = autonomousState.currentVersionName,
                practiceSessionCount = practiceSessions.size,
                onOpenVoiceChat = { currentDestination = AppDestination.LIVE_VOICE },
                onOpenDeviceShield = { currentDestination = AppDestination.DEVICE_SHIELD },
                onOpenOrchestrator = { currentDestination = AppDestination.ORCHESTRATOR },
                onOpenThemeStudio = { showThemeStudioDialog = true },
                onOpenHardwareAccelerator = { showHardwareAcceleratorDialog = true },
                onOpenRiskScanner = { showRiskScannerDialog = true },
                onOpenHistory = { showChatHistoryDialog = true },
                onOpenAppSettings = { showAppSettingsDialog = true },
                onOpenApkShare = { showApkShareDialog = true },
                onQuickAiCommand = { prompt ->
                  viewModel.sendMessage(prompt)
                  currentDestination = AppDestination.LIVE_VOICE
                },
                onOpenNotificationTones = { showNotificationTonesDialog = true },
                onOpenLogoStudio = { showLogoStudioDialog = true }
              )
            }

            AppDestination.LIVE_VOICE -> {
              LiveConversationScreen(
                messages = messages,
                selectedLanguage = selectedLanguage,
                selectedScenario = selectedScenario,
                isLoading = isLoading,
                ttsManager = viewModel.ttsManager,
                speechHelper = viewModel.speechHelper,
                fastLookupResult = fastLookupResult,
                onSendMessage = { viewModel.sendMessage(it) },
                onLanguageChange = { viewModel.selectLanguage(it) },
                onScenarioChange = { viewModel.selectScenario(it) },
                onFastLookup = { viewModel.runFastLookup(it) },
                onClearChat = { viewModel.clearChat() },
                onCaptureImageToChat = { bmp, prompt ->
                  viewModel.analyzeCapturedImage(bmp, prompt, sendToChat = true)
                },
                onSendAttachedFile = { payload, prompt ->
                  viewModel.sendAttachedFile(payload, prompt)
                },
                onGenerateImagePrompt = { prompt ->
                  viewModel.generateImage(prompt)
                  currentDestination = AppDestination.ORCHESTRATOR
                },
                onOpenLiveSession = { showLiveSessionDialog = true },
                onOpenInternalAi = { showInternalAiDialog = true },
                onOpenHistory = { showChatHistoryDialog = true },
                onNewChat = { viewModel.startNewPracticeSession(archiveCurrent = true) },
                onSaveSession = { viewModel.saveCurrentPracticeSession() },
                practiceSessionCount = practiceSessions.size
              )
            }

            AppDestination.DEVICE_SHIELD -> {
              DeviceShieldScreen(
                deviceShieldManager = viewModel.deviceShieldManager,
                autonomousEngine = viewModel.autonomousEngine,
                evolutionLogs = evolutionLogs,
                selectedLanguage = selectedLanguage,
                isAnalyzingImage = isAnalyzingImage,
                imageAnalysisResult = imageAnalysisResult,
                onAnalyzeImage = { bmp, prompt, sendToChat ->
                  viewModel.analyzeCapturedImage(bmp, prompt, sendToChat)
                },
                onRunAutoHeal = { viewModel.runAutoHeal() },
                onRunAutoImprove = { viewModel.runAutoImprove() },
                onRunAutoUpdateVersion = { viewModel.runAutoUpdateVersion() },
                onRunFullAutonomousCycle = { viewModel.runFullAutonomousEvolution() },
                onClearEvolutionLogs = { viewModel.clearEvolutionLogs() },
                onSaveCheckpoint = { title, summary ->
                  viewModel.saveCheckpoint(title, summary)
                },
                onSpeak = { text ->
                  viewModel.ttsManager.speak(text, selectedLanguage)
                },
                onOpenThemeStudio = { showThemeStudioDialog = true }
              )
            }

            AppDestination.ORCHESTRATOR -> {
              OrchestratorScreen(
                selectedLanguage = selectedLanguage,
                generatedImage = generatedImage,
                isGeneratingImage = isGeneratingImage,
                imageError = imageError,
                orchestratorResult = orchestratorResult,
                isOrchestrating = isOrchestrating,
                orchestratorLogs = orchestratorLogs,
                ttsManager = viewModel.ttsManager,
                onGenerateImage = { prompt, ratio -> viewModel.generateImage(prompt, ratio) },
                onClearGeneratedImage = { viewModel.clearGeneratedImage() },
                onRunOrchestration = { goal -> viewModel.runOrchestration(goal) },
                onClearOrchestratorLogs = { viewModel.clearOrchestratorLogs() },
                onSendImageToChat = { bmp, prompt ->
                  viewModel.analyzeCapturedImage(bmp, prompt, sendToChat = true)
                  currentDestination = AppDestination.LIVE_VOICE
                }
              )
            }
          }

          if (UiThemeController.assistantEngineEnabled) {
            FloatingAssistantButtonOverlay(
              ttsManager = viewModel.ttsManager,
              selectedLanguage = selectedLanguage,
              onSendMessage = { viewModel.sendMessage(it) }
            )
          }
        }
      }
    }
  }
}

@Composable
fun TopHeaderAndNavigationBar(
  versionName: String,
  currentDestination: AppDestination,
  onSelectDestination: (AppDestination) -> Unit,
  onOpenThemeStudio: () -> Unit,
  onOpenApkShare: () -> Unit,
  onOpenRiskScanner: () -> Unit,
  onOpenHardwareAccelerator: () -> Unit,
  onOpenAppSettings: () -> Unit,
  onOpenInternalAi: () -> Unit,
  onOpenAiSettings: () -> Unit,
  onOpenLanguageSelector: () -> Unit,
  onOpenHistory: () -> Unit,
  onOpenImprovement: () -> Unit,
  onOpenNotificationTones: () -> Unit = {},
  onOpenLogoStudio: () -> Unit = {},
  onSetDefaultHome: () -> Unit,
  currentAiEngine: String,
  onSetAiEngine: (String) -> Unit
) {
  var menuExpanded by remember { mutableStateOf(false) }

  // Ultra-sleek, low-profile header bar so it never takes visual space away from the launcher
  Row(
    modifier = Modifier
      .fillMaxWidth()
      .background(DarkSurfaceElevated.copy(alpha = 0.92f))
      .drawBehind {
        drawLine(
          brush = Brush.horizontalGradient(listOf(MetalPrimary, MetalSecondary, MetalPrimary)),
          start = Offset(0f, size.height),
          end = Offset(size.width, size.height),
          strokeWidth = 1.2.dp.toPx()
        )
      }
      .padding(horizontal = 8.dp, vertical = 5.dp),
    verticalAlignment = Alignment.CenterVertically,
    horizontalArrangement = Arrangement.SpaceBetween
  ) {
    // Left: Brand Icon + LAUNCH KDF Title (Tap to return to Home Launcher)
    Row(
      verticalAlignment = Alignment.CenterVertically,
      modifier = Modifier
        .clickable {
          onSelectDestination(AppDestination.HOME_LAUNCHER)
          onOpenLogoStudio()
        }
        .testTag("header_home_brand_button")
    ) {
      Image(
        painter = painterResource(id = R.drawable.ic_kdf_logo),
        contentDescription = "Icono LAUNCH KDF",
        contentScale = ContentScale.Crop,
        modifier = Modifier
          .size(24.dp)
          .clip(CircleShape)
          .border(1.dp, MetalSecondary, CircleShape)
          .testTag("app_header_icon")
      )
      Spacer(modifier = Modifier.width(6.dp))
      Text(
        text = "LAUNCH KDF",
        color = TextMain,
        fontWeight = FontWeight.ExtraBold,
        fontSize = 12.sp,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis
      )
      Spacer(modifier = Modifier.width(4.dp))
      Box(
        modifier = Modifier
          .background(MetalSecondary.copy(alpha = 0.16f), RoundedCornerShape(4.dp))
          .border(0.5.dp, MetalSecondary, RoundedCornerShape(4.dp))
          .padding(horizontal = 4.dp, vertical = 1.dp)
      ) {
        Text(
          text = "OS v$versionName",
          color = MetalSecondary,
          fontSize = 7.sp,
          fontWeight = FontWeight.Bold
        )
      }
    }

    // Center-Right: Ultra-compact mode switcher pills + Theme Studio + Menu
    Row(
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
      AppDestination.entries.forEach { dest ->
        val selected = currentDestination == dest
        Box(
          modifier = Modifier
            .clip(RoundedCornerShape(6.dp))
            .background(if (selected) MetalSecondary else DarkSurface)
            .border(
              width = 0.7.dp,
              color = if (selected) MetalSecondary else MetalPrimary.copy(alpha = 0.35f),
              shape = RoundedCornerShape(6.dp)
            )
            .clickable { onSelectDestination(dest) }
            .padding(horizontal = 6.dp, vertical = 4.dp)
            .testTag("nav_pill_${dest.route}"),
          contentAlignment = Alignment.Center
        ) {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
              imageVector = dest.icon,
              contentDescription = dest.title,
              tint = if (selected) Color.Black else MetalSecondary,
              modifier = Modifier.size(13.dp)
            )
            if (selected) {
              Spacer(modifier = Modifier.width(3.dp))
              Text(
                text = dest.title,
                color = Color.Black,
                fontSize = 9.sp,
                fontWeight = FontWeight.ExtraBold
              )
            }
          }
        }
      }

      // Theme Studio quick icon
      IconButton(
        onClick = onOpenThemeStudio,
        modifier = Modifier
          .size(26.dp)
          .jewelButton(cornerRadius = 7.dp)
          .testTag("header_theme_studio_button")
      ) {
        Icon(
          imageVector = Icons.Default.Palette,
          contentDescription = "Estudio de Temas",
          tint = MetalSecondary,
          modifier = Modifier.size(14.dp)
        )
      }

      // Overflow Menu Button
      Box {
        IconButton(
          onClick = { menuExpanded = true },
          modifier = Modifier
            .size(26.dp)
            .jewelButton(cornerRadius = 7.dp)
            .testTag("header_overflow_menu_button")
        ) {
          Icon(
            imageVector = Icons.Default.MoreVert,
            contentDescription = "Menú Principal",
            tint = MetalSecondary,
            modifier = Modifier.size(14.dp)
          )
        }

        DropdownMenu(
          expanded = menuExpanded,
          onDismissRequest = { menuExpanded = false },
          modifier = Modifier
            .background(DarkSurfaceElevated)
            .border(1.2.dp, MetalSecondary, RoundedCornerShape(12.dp))
            .widthIn(min = 240.dp)
            .testTag("top_dropdown_menu")
        ) {
          Box(
            modifier = Modifier
              .fillMaxWidth()
              .background(DarkSurface)
              .padding(horizontal = 12.dp, vertical = 8.dp)
          ) {
            Text(
              text = "SISTEMA CENTRAL LAUNCH KDF",
              color = MetalSecondary,
              fontSize = 10.sp,
              fontWeight = FontWeight.ExtraBold,
              letterSpacing = 0.8.sp
            )
          }
          HorizontalDivider(color = MetalPrimary.copy(alpha = 0.3f), thickness = 1.dp)

          DropdownMenuItem(
            text = {
              Text("FIJAR COMO LAUNCHER ANDROID", color = MetalSecondary, fontWeight = FontWeight.ExtraBold, fontSize = 12.sp)
            },
            leadingIcon = {
              Icon(Icons.Default.Home, contentDescription = null, tint = MetalSecondary, modifier = Modifier.size(17.dp))
            },
            onClick = {
              menuExpanded = false
              onSetDefaultHome()
            }
          )

          DropdownMenuItem(
            text = {
              Text("ESTUDIO DE TEMAS & FONDOS", color = TextMain, fontWeight = FontWeight.Bold, fontSize = 12.sp)
            },
            leadingIcon = {
              Icon(Icons.Default.Palette, contentDescription = null, tint = MetalSecondary, modifier = Modifier.size(17.dp))
            },
            onClick = {
              menuExpanded = false
              onOpenThemeStudio()
            }
          )

          DropdownMenuItem(
            text = {
              Text("TONOS NOTIFICACIONES 4K", color = MetalSecondary, fontWeight = FontWeight.ExtraBold, fontSize = 12.sp)
            },
            leadingIcon = {
              Icon(Icons.Default.GraphicEq, contentDescription = null, tint = MetalSecondary, modifier = Modifier.size(17.dp))
            },
            onClick = {
              menuExpanded = false
              onOpenNotificationTones()
            }
          )

          DropdownMenuItem(
            text = {
              Text("LOGO KDF & CÓDIGO CSS", color = TextMain, fontWeight = FontWeight.Bold, fontSize = 12.sp)
            },
            leadingIcon = {
              Icon(Icons.Default.Diamond, contentDescription = null, tint = MetalSecondary, modifier = Modifier.size(17.dp))
            },
            onClick = {
              menuExpanded = false
              onOpenLogoStudio()
            }
          )

          DropdownMenuItem(
            text = {
              Column {
                Text("INTELIGENCIA ACTIVA", color = MetalSecondary, fontWeight = FontWeight.ExtraBold, fontSize = 9.sp)
                Text(currentAiEngine, color = TextMain, fontWeight = FontWeight.Bold, fontSize = 12.sp)
              }
            },
            onClick = {
              onSetAiEngine(if (currentAiEngine == "Gemini 1.5 Pro") "IA Local" else "Gemini 1.5 Pro")
            }
          )

          DropdownMenuItem(
            text = {
              Text("HISTORIAL & SESIONES ROOM", color = TextMain, fontWeight = FontWeight.Bold, fontSize = 12.sp)
            },
            leadingIcon = {
              Icon(Icons.Default.History, contentDescription = null, tint = MetalSecondary, modifier = Modifier.size(17.dp))
            },
            onClick = {
              menuExpanded = false
              onOpenHistory()
            }
          )

          DropdownMenuItem(
            text = {
              Text("ACELERACIÓN CPU / GPU", color = TextMain, fontWeight = FontWeight.Bold, fontSize = 12.sp)
            },
            leadingIcon = {
              Icon(Icons.Default.Memory, contentDescription = null, tint = MetalSecondary, modifier = Modifier.size(17.dp))
            },
            onClick = {
              menuExpanded = false
              onOpenHardwareAccelerator()
            }
          )

          DropdownMenuItem(
            text = {
              Text("ESCUDO & AUTO-IA", color = TextMain, fontWeight = FontWeight.Bold, fontSize = 12.sp)
            },
            leadingIcon = {
              Icon(Icons.Default.Shield, contentDescription = null, tint = MetalSecondary, modifier = Modifier.size(17.dp))
            },
            onClick = {
              menuExpanded = false
              onSelectDestination(AppDestination.DEVICE_SHIELD)
            }
          )

          DropdownMenuItem(
            text = {
              Text("IA ORQUESTADOR & IMAGEN", color = TextMain, fontWeight = FontWeight.Bold, fontSize = 12.sp)
            },
            leadingIcon = {
              Icon(Icons.Default.Hub, contentDescription = null, tint = MetalSecondary, modifier = Modifier.size(17.dp))
            },
            onClick = {
              menuExpanded = false
              onSelectDestination(AppDestination.ORCHESTRATOR)
            }
          )

          DropdownMenuItem(
            text = {
              Text("OPTIMIZACIÓN CONTINUA", color = TextMain, fontWeight = FontWeight.Bold, fontSize = 12.sp)
            },
            leadingIcon = {
              Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = MetalSecondary, modifier = Modifier.size(17.dp))
            },
            onClick = {
              menuExpanded = false
              onOpenImprovement()
            }
          )

          DropdownMenuItem(
            text = {
              Text("CONFIGURACIÓN SISTEMA", color = TextMain, fontWeight = FontWeight.Bold, fontSize = 12.sp)
            },
            leadingIcon = {
              Icon(Icons.Default.Settings, contentDescription = null, tint = MetalSecondary, modifier = Modifier.size(17.dp))
            },
            onClick = {
              menuExpanded = false
              onOpenAppSettings()
            }
          )

          DropdownMenuItem(
            text = {
              Text("DISTRIBUCIÓN APK", color = TextMain, fontWeight = FontWeight.Bold, fontSize = 12.sp)
            },
            leadingIcon = {
              Icon(Icons.Default.Share, contentDescription = null, tint = MetalSecondary, modifier = Modifier.size(17.dp))
            },
            onClick = {
              menuExpanded = false
              onOpenApkShare()
            }
          )
        }
      }
    }
  }
}
