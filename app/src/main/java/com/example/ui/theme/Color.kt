package com.example.ui.theme

import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color

data class LuxuryThemePreset(
  val id: String,
  val name: String,
  val subtitle: String,
  val primaryAccent: Color,      // Silver / Chrome / Theme Secondary
  val secondaryAccent: Color,    // Main Theme Accent / Gold
  val tertiaryAccent: Color,     // Card & Icon Surface
  val baseSurfaceTint: Color,    // Deep Background Base
  val borderBaseColor: Color,    // Primary Border
  val goldAccent: Color = Color(0xFFFFD700),
  val silverAccent: Color = Color(0xFFE2E8F0),
  val defaultWallpaperEffectId: String = "PSICO_VORTEX",
  val defaultIconPackStyleId: String = "NOB_PSICO",
  val isPremium: Boolean = false
)

val HYPER_LUX_THEME_PRESETS = listOf(
  LuxuryThemePreset(
    id = "nob_psico",
    name = "nob-psico",
    subtitle = "Negro & Morado Psicodélico con Oro 24K y Plata",
    primaryAccent = Color(0xFFE2E8F0),    // Plata Cromo
    secondaryAccent = Color(0xFFA855F7),  // Morado Psicodélico Intenso
    tertiaryAccent = Color(0xFF160B28),   // Superficie Violeta Abisal
    baseSurfaceTint = Color(0xFF06030C),  // Negro Profundo
    borderBaseColor = Color(0xFFFFD700),  // Oro 24K
    goldAccent = Color(0xFFFFD700),
    silverAccent = Color(0xFFE2E8F0),
    defaultWallpaperEffectId = "PSICO_VORTEX",
    defaultIconPackStyleId = "NOB_PSICO"
  ),
  LuxuryThemePreset(
    id = "metrix_relativ",
    name = "metrix-relativ",
    subtitle = "Código Matrix Esmeralda con Oro y Plata Relativista",
    primaryAccent = Color(0xFFD9E2EC),    // Plata Relativista
    secondaryAccent = Color(0xFF00FF66),  // Verde Matrix Puro
    tertiaryAccent = Color(0xFF071A10),   // Superficie Terminal Matrix
    baseSurfaceTint = Color(0xFF020804),  // Negro Vacío Matrix
    borderBaseColor = Color(0xFFD4AF37),  // Oro Imperial
    goldAccent = Color(0xFFFFD700),
    silverAccent = Color(0xFFCBD5E1),
    defaultWallpaperEffectId = "METRIX_CODE_RAIN",
    defaultIconPackStyleId = "METRIX_RELATIV"
  ),
  LuxuryThemePreset(
    id = "adn_pink",
    name = "ADN-Pink",
    subtitle = "Rosa Neón & Negro Obsidiana con Plata y Oro",
    primaryAccent = Color(0xFFE2E8F0),    // Plata Brillante
    secondaryAccent = Color(0xFFFF2A85),  // Rosa ADN Biotecnológico
    tertiaryAccent = Color(0xFF1F0918),   // Negro-Rosa Profundo
    baseSurfaceTint = Color(0xFF090307),  // Negro Obsidiana
    borderBaseColor = Color(0xFFFFD700),  // Oro 24K
    goldAccent = Color(0xFFFFD700),
    silverAccent = Color(0xFFF1F5F9),
    defaultWallpaperEffectId = "ADN_PINK_HELIX",
    defaultIconPackStyleId = "ADN_PINK"
  ),
  LuxuryThemePreset(
    id = "sky_eter",
    name = "Sky-Eter",
    subtitle = "Azul Cielo Éter & Blanco Nube con Oro y Plata",
    primaryAccent = Color(0xFF94A3B8),    // Plata Celestial
    secondaryAccent = Color(0xFF0EA5E9),  // Azul Cielo Éter
    tertiaryAccent = Color(0xFFE0F2FE),   // Blanco-Azul Cristalino
    baseSurfaceTint = Color(0xFFF0F9FF),  // Blanco Éter Luminoso
    borderBaseColor = Color(0xFFD4AF37),  // Oro Solar
    goldAccent = Color(0xFFD4AF37),
    silverAccent = Color(0xFF94A3B8),
    defaultWallpaperEffectId = "SKY_ETER_CELESTIAL",
    defaultIconPackStyleId = "SKY_ETER"
  ),
  LuxuryThemePreset(
    id = "dragon_fire",
    name = "Dragon-Fire",
    subtitle = "Rojo Dragón & Naranja Volcánico con Oro y Plata",
    primaryAccent = Color(0xFFFF6B00),    // Naranja Fuego
    secondaryAccent = Color(0xFFFF1E1E),  // Rojo Dragón
    tertiaryAccent = Color(0xFF240B07),   // Carbón Volcánico
    baseSurfaceTint = Color(0xFF0D0402),  // Negro Ceniza
    borderBaseColor = Color(0xFFFFD700),  // Oro Escama de Dragón
    goldAccent = Color(0xFFFFD700),
    silverAccent = Color(0xFFE2E8F0),
    defaultWallpaperEffectId = "DRAGON_FIRE_EMBERS",
    defaultIconPackStyleId = "DRAGON_FIRE"
  ),
  LuxuryThemePreset(
    id = "quantum_venom",
    name = "Quantum-Venom",
    subtitle = "Negro Simbionte & Lima Radioactivo con Oro y Plata",
    primaryAccent = Color(0xFFE2E8F0),
    secondaryAccent = Color(0xFFA3E635),
    tertiaryAccent = Color(0xFF111A08),
    baseSurfaceTint = Color(0xFF060903),
    borderBaseColor = Color(0xFFFFD700),
    goldAccent = Color(0xFFFFD700),
    silverAccent = Color(0xFFE2E8F0),
    defaultWallpaperEffectId = "QUANTUM_ATOM",
    defaultIconPackStyleId = "QUANTUM_VENOM"
  ),
  LuxuryThemePreset(
    id = "electro_arcade",
    name = "Electro-Arcade 80s",
    subtitle = "Magenta Synthwave & Cian Eléctrico con Oro y Plata",
    primaryAccent = Color(0xFF00F5D4),
    secondaryAccent = Color(0xFFF72585),
    tertiaryAccent = Color(0xFF140A29),
    baseSurfaceTint = Color(0xFF070312),
    borderBaseColor = Color(0xFFFFD700),
    goldAccent = Color(0xFFFFD700),
    silverAccent = Color(0xFFE2E8F0),
    defaultWallpaperEffectId = "ARCADE_SYNTH_GRID",
    defaultIconPackStyleId = "ELECTRO_ARCADE"
  ),
  LuxuryThemePreset(
    id = "solar_pharaoh",
    name = "Solar-Pharaoh",
    subtitle = "Oro Egipcio, Turquesa Imperial & Plata sobre Obsidiana",
    primaryAccent = Color(0xFF2DD4BF),
    secondaryAccent = Color(0xFFFFD700),
    tertiaryAccent = Color(0xFF1A1508),
    baseSurfaceTint = Color(0xFF0A0803),
    borderBaseColor = Color(0xFFFFD700),
    goldAccent = Color(0xFFFFD700),
    silverAccent = Color(0xFFE2E8F0),
    defaultWallpaperEffectId = "SOLAR_ORBIT",
    defaultIconPackStyleId = "SOLAR_PHARAOH"
  ),
  LuxuryThemePreset(
    id = "cosmic_nebula",
    name = "Cosmic-Nebula",
    subtitle = "Violeta Galáctico & Azul Cuántico con Oro y Plata",
    primaryAccent = Color(0xFF38BDF8),
    secondaryAccent = Color(0xFFC084FC),
    tertiaryAccent = Color(0xFF111328),
    baseSurfaceTint = Color(0xFF060714),
    borderBaseColor = Color(0xFFD4AF37),
    goldAccent = Color(0xFFFFD700),
    silverAccent = Color(0xFFE2E8F0),
    defaultWallpaperEffectId = "PARTICLE_MATRIX",
    defaultIconPackStyleId = "COSMIC_NEBULA"
  ),
  LuxuryThemePreset(
    id = "midnight_luxury",
    name = "Medianoche Bimetálica KDF",
    subtitle = "Oro 24K & Cromo sobre Negro OLED",
    primaryAccent = Color(0xFFBCCCDC),
    secondaryAccent = Color(0xFFD4AF37),
    tertiaryAccent = Color(0xFF16181D),
    baseSurfaceTint = Color(0xFF07080C),
    borderBaseColor = Color(0xFF2D3340),
    goldAccent = Color(0xFFD4AF37),
    silverAccent = Color(0xFFBCCCDC),
    defaultWallpaperEffectId = "CYBER_GRID",
    defaultIconPackStyleId = "BIMETALLIC_CLASSIC"
  ),
  LuxuryThemePreset(
    id = "bimetallic_royal",
    name = "Bimetálico Real Perla",
    subtitle = "Cromo Pulido & Oro 24K sobre Blanco Perla",
    primaryAccent = Color(0xFFBCCCDC),
    secondaryAccent = Color(0xFFD4AF37),
    tertiaryAccent = Color(0xFFE8ECEF),
    baseSurfaceTint = Color(0xFFF8F9FA),
    borderBaseColor = Color(0xFFD9E2EC),
    goldAccent = Color(0xFFD4AF37),
    silverAccent = Color(0xFF94A3B8),
    defaultWallpaperEffectId = "LUXURY_MESH",
    defaultIconPackStyleId = "BIMETALLIC_CLASSIC",
    isPremium = true
  )
)

// Base static palette references
val WhitePearl = Color(0xFFF8F9FA)
val ChromePolished = Color(0xFFD9E2EC)
val ChromeDark = Color(0xFFBCCCDC)
val GoldPolished = Color(0xFFD4AF37)
val GoldBright = Color(0xFFFFDF73)
val GraphiteCharcoal = Color(0xFF1C1C1E)
val PlumbousGray = Color(0xFF5A6065)

val DangerRed = Color(0xFFFF4D6D)

// Reactive Compose-Tracked Colors (Defaulting to nob-psico!)
var MetalPrimary by mutableStateOf(Color(0xFFE2E8F0))
var MetalSecondary by mutableStateOf(Color(0xFFA855F7))
var MetalGold by mutableStateOf(Color(0xFFFFD700))
var MetalSilver by mutableStateOf(Color(0xFFE2E8F0))
var SurfacePearl by mutableStateOf(Color(0xFF06030C))
var TextMain by mutableStateOf(Color.White)
var TextSub by mutableStateOf(Color(0xFFCBD5E1))

var ShieldGreen by mutableStateOf(Color(0xFF10B981))

// Skeuomorphic Shadow & Glow colors
val ShadowSoft = Color(0x40000000)
val GlowWhite = Color(0xFFFFFFFF)

// Legacy & Surface mappings
var AlphaGold by mutableStateOf(GoldPolished)
var CyberCyan by mutableStateOf(ChromePolished)
var DeepBackground by mutableStateOf(Color(0xFF06030C))
var DarkSurface by mutableStateOf(Color(0xFF160B28))
var DarkSurfaceElevated by mutableStateOf(Color(0xFF21123A))
enum class LauncherMode(
  val id: String,
  val label: String,
  val iconEmoji: String,
  val subtitle: String
) {
  KDF_OS("kdf_os", "Estilo KDF", "👑", "Cyber-Luxury Orbital HUD & Telemetría 60FPS"),
  ANDROID("android", "Estilo android", "🤖", "Stock Material You, At-A-Glance & Quick Pill"),
  IOS("ios", "Estilo iOS", "🍏", "Apple Springboard, Squircles & Frosted Glass Dock")
}

var DarkSurfaceBorder by mutableStateOf(Color(0xFFFFD700))
var GlassBarSurface by mutableStateOf(Color(0xFF120822))
var TextPrimary by mutableStateOf(Color.White)
var TextSecondary by mutableStateOf(Color(0xFFCBD5E1))

object UiThemeController {
  var launcherMode by mutableStateOf(LauncherMode.KDF_OS)
    private set

  var active4kNotificationToneId by mutableStateOf("gold_chime_4k")
    private set

  var selectedPresetId by mutableStateOf("nob_psico")
    private set

  var backgroundClarity by mutableFloatStateOf(1.0f)
    private set

  var uiScaleMultiplier by mutableFloatStateOf(1.00f)
    private set

  var isFloatingButtonEnabled by mutableStateOf(false)
    private set

  var customBackgroundUri by mutableStateOf<String?>(null)
    private set

  var floatingButtonIconType by mutableStateOf("ROBOT")
    private set

  // LAUNCH KDF Home Launcher Personalization Settings
  var wallpaperEffectId by mutableStateOf("PSICO_VORTEX")
    private set

  var iconPackStyleId by mutableStateOf("NOB_PSICO")
    private set

  var autoSyncWallpaperWithTheme by mutableStateOf(true)
    private set

  var applyThemeTintToAppIcons by mutableStateOf(true)
    private set

  var clockStyleId by mutableStateOf("CYBER_HUD")
    private set

  var iconShapeId by mutableStateOf("SQUIRCLE")
    private set

  var gridColumns by mutableIntStateOf(4)
    private set

  var iconSizeDp by mutableFloatStateOf(52f)
    private set

  var navigationBarHeight by mutableFloatStateOf(56f)
    private set

  var showAppLabels by mutableStateOf(true)
    private set

  var showFloatingDock by mutableStateOf(true)
    private set

  var showTelemetryBar by mutableStateOf(true)
    private set

  var assistantEngineEnabled by mutableStateOf(true)
    private set

  var glassOpacity by mutableFloatStateOf(0.86f)
    private set

  var animationSpeed by mutableFloatStateOf(1.1f)
    private set

  var isPremium by mutableStateOf(false)
    private set

  var pinnedPackages by mutableStateOf<Set<String>>(emptySet())
    private set

  fun isDarkThemeActive(): Boolean {
    return SurfacePearl.red + SurfacePearl.green + SurfacePearl.blue < 1.3f
  }

  fun currentPreset(): LuxuryThemePreset {
    return HYPER_LUX_THEME_PRESETS.find { it.id == selectedPresetId }
      ?: HYPER_LUX_THEME_PRESETS.first()
  }

  fun loadFromPreferences(context: Context) {
    val prefs = context.getSharedPreferences("hyperlux_ui_theme_prefs", Context.MODE_PRIVATE)
    val modeId = prefs.getString("launcher_mode_id", LauncherMode.KDF_OS.id) ?: LauncherMode.KDF_OS.id
    launcherMode = LauncherMode.entries.find { it.id == modeId } ?: LauncherMode.KDF_OS
    active4kNotificationToneId = prefs.getString("active_4k_tone_id", "gold_chime_4k") ?: "gold_chime_4k"

    selectedPresetId = prefs.getString("preset_id", "nob_psico") ?: "nob_psico"
    backgroundClarity = prefs.getFloat("background_clarity", 1.0f)
    uiScaleMultiplier = prefs.getFloat("ui_scale_multiplier", 1.00f)
    isFloatingButtonEnabled = prefs.getBoolean("floating_btn_enabled", false)
    floatingButtonIconType = prefs.getString("floating_btn_icon", "ROBOT") ?: "ROBOT"
    customBackgroundUri = prefs.getString("custom_bg_uri", null)

    val preset = currentPreset()
    wallpaperEffectId = prefs.getString("wallpaper_effect_id", preset.defaultWallpaperEffectId)
      ?: preset.defaultWallpaperEffectId
    iconPackStyleId = prefs.getString("icon_pack_style_id", preset.defaultIconPackStyleId)
      ?: preset.defaultIconPackStyleId
    autoSyncWallpaperWithTheme = prefs.getBoolean("auto_sync_wallpaper", true)
    applyThemeTintToAppIcons = prefs.getBoolean("apply_theme_tint_icons", true)

    clockStyleId = prefs.getString("clock_style_id", "CYBER_HUD") ?: "CYBER_HUD"
    iconShapeId = prefs.getString("icon_shape_id", "SQUIRCLE") ?: "SQUIRCLE"
    gridColumns = prefs.getInt("grid_columns", 4).coerceIn(3, 6)
    iconSizeDp = prefs.getFloat("icon_size_dp", 52f).coerceIn(38f, 68f)
    navigationBarHeight = prefs.getFloat("nav_bar_height", 56f).coerceIn(40f, 80f)
    showAppLabels = prefs.getBoolean("show_app_labels", true)
    showFloatingDock = prefs.getBoolean("show_floating_dock", true)
    showTelemetryBar = prefs.getBoolean("show_telemetry_bar", true)
    assistantEngineEnabled = prefs.getBoolean("assistant_engine_enabled", true)
    isPremium = prefs.getBoolean("is_premium", false)
    glassOpacity = prefs.getFloat("glass_opacity", 0.86f).coerceIn(0.35f, 1.0f)
    animationSpeed = prefs.getFloat("animation_speed", 1.1f).coerceIn(0.2f, 2.5f)
    pinnedPackages = prefs.getStringSet("pinned_packages", emptySet()) ?: emptySet()

    recomputePalette()
  }

  private fun saveToPreferences(context: Context) {
    context.getSharedPreferences("hyperlux_ui_theme_prefs", Context.MODE_PRIVATE)
      .edit()
      .putString("launcher_mode_id", launcherMode.id)
      .putString("active_4k_tone_id", active4kNotificationToneId)
      .putString("preset_id", selectedPresetId)
      .putFloat("background_clarity", backgroundClarity)
      .putFloat("ui_scale_multiplier", uiScaleMultiplier)
      .putBoolean("floating_btn_enabled", isFloatingButtonEnabled)
      .putString("floating_btn_icon", floatingButtonIconType)
      .putString("custom_bg_uri", customBackgroundUri)
      .putString("wallpaper_effect_id", wallpaperEffectId)
      .putString("icon_pack_style_id", iconPackStyleId)
      .putBoolean("auto_sync_wallpaper", autoSyncWallpaperWithTheme)
      .putBoolean("apply_theme_tint_icons", applyThemeTintToAppIcons)
      .putString("clock_style_id", clockStyleId)
      .putString("icon_shape_id", iconShapeId)
      .putInt("grid_columns", gridColumns)
      .putFloat("icon_size_dp", iconSizeDp)
      .putFloat("nav_bar_height", navigationBarHeight)
      .putBoolean("show_app_labels", showAppLabels)
      .putBoolean("show_floating_dock", showFloatingDock)
      .putBoolean("show_telemetry_bar", showTelemetryBar)
       .putBoolean("assistant_engine_enabled", assistantEngineEnabled)
       .putBoolean("is_premium", isPremium)
       .putFloat("glass_opacity", glassOpacity)
      .putFloat("animation_speed", animationSpeed)
      .putStringSet("pinned_packages", pinnedPackages)
      .apply()
  }

  fun updateNavigationBarHeight(context: Context, height: Float) {
    navigationBarHeight = height.coerceIn(40f, 80f)
    saveToPreferences(context)
  }

  fun updateAssistantEngineEnabled(context: Context, enabled: Boolean) {
    assistantEngineEnabled = enabled
    saveToPreferences(context)
  }

  fun updateLauncherMode(context: Context, mode: LauncherMode) {
    launcherMode = mode
    saveToPreferences(context)
  }

  fun updateActive4kTone(context: Context, toneId: String) {
    active4kNotificationToneId = toneId
    saveToPreferences(context)
  }

  fun updatePremiumStatus(context: Context, premium: Boolean) {
    isPremium = premium
    saveToPreferences(context)
  }

  fun selectPreset(context: Context, presetId: String) {
    selectedPresetId = presetId
    val preset = currentPreset()
    if (autoSyncWallpaperWithTheme) {
      wallpaperEffectId = preset.defaultWallpaperEffectId
      iconPackStyleId = preset.defaultIconPackStyleId
    }
    recomputePalette()
    saveToPreferences(context)
  }

  fun updateIconPackStyle(context: Context, styleId: String) {
    iconPackStyleId = styleId
    saveToPreferences(context)
  }

  fun updateAutoSyncWallpaperWithTheme(context: Context, enabled: Boolean) {
    autoSyncWallpaperWithTheme = enabled
    saveToPreferences(context)
  }

  fun updateApplyThemeTintToAppIcons(context: Context, enabled: Boolean) {
    applyThemeTintToAppIcons = enabled
    saveToPreferences(context)
  }

  fun updateCustomBackgroundUri(context: Context, uri: String?) {
    customBackgroundUri = uri
    saveToPreferences(context)
  }

  fun updateUiScale(context: Context, scale: Float) {
    uiScaleMultiplier = scale.coerceIn(0.80f, 1.25f)
    saveToPreferences(context)
  }

  fun updateFloatingButtonEnabled(context: Context, enabled: Boolean) {
    isFloatingButtonEnabled = enabled
    saveToPreferences(context)
  }

  fun updateWallpaperEffect(context: Context, effectId: String) {
    wallpaperEffectId = effectId
    saveToPreferences(context)
  }

  fun updateClockStyle(context: Context, styleId: String) {
    clockStyleId = styleId
    saveToPreferences(context)
  }

  fun updateIconShape(context: Context, shapeId: String) {
    iconShapeId = shapeId
    saveToPreferences(context)
  }

  fun updateGridColumns(context: Context, columns: Int) {
    gridColumns = columns.coerceIn(3, 6)
    saveToPreferences(context)
  }

  fun updateIconSizeDp(context: Context, sizeDp: Float) {
    iconSizeDp = sizeDp.coerceIn(38f, 68f)
    saveToPreferences(context)
  }

  fun updateShowAppLabels(context: Context, show: Boolean) {
    showAppLabels = show
    saveToPreferences(context)
  }

  fun updateShowFloatingDock(context: Context, show: Boolean) {
    showFloatingDock = show
    saveToPreferences(context)
  }

  fun updateShowTelemetryBar(context: Context, show: Boolean) {
    showTelemetryBar = show
    saveToPreferences(context)
  }

  fun updateGlassOpacity(context: Context, opacity: Float) {
    glassOpacity = opacity.coerceIn(0.35f, 1.0f)
    saveToPreferences(context)
  }

  fun updateAnimationSpeed(context: Context, speed: Float) {
    animationSpeed = speed.coerceIn(0.2f, 2.5f)
    saveToPreferences(context)
  }

  fun togglePinnedPackage(context: Context, packageName: String) {
    val mutable = pinnedPackages.toMutableSet()
    if (mutable.contains(packageName)) {
      mutable.remove(packageName)
    } else {
      mutable.add(packageName)
    }
    pinnedPackages = mutable
    saveToPreferences(context)
  }

  fun resetLauncherDefaults(context: Context) {
    selectedPresetId = "nob_psico"
    wallpaperEffectId = "PSICO_VORTEX"
    iconPackStyleId = "NOB_PSICO"
    autoSyncWallpaperWithTheme = true
    applyThemeTintToAppIcons = true
    clockStyleId = "CYBER_HUD"
    iconShapeId = "SQUIRCLE"
    gridColumns = 4
    iconSizeDp = 52f
    showAppLabels = true
    showFloatingDock = true
    showTelemetryBar = true
    glassOpacity = 0.86f
    animationSpeed = 1.1f
    uiScaleMultiplier = 1.0f
    customBackgroundUri = null
    recomputePalette()
    saveToPreferences(context)
  }

  private fun recomputePalette() {
    val preset = currentPreset()

    MetalPrimary = preset.primaryAccent
    MetalSecondary = preset.secondaryAccent
    MetalGold = preset.goldAccent
    MetalSilver = preset.silverAccent
    SurfacePearl = preset.baseSurfaceTint

    val isDark = preset.baseSurfaceTint.red + preset.baseSurfaceTint.green + preset.baseSurfaceTint.blue < 1.3f
    if (isDark) {
      TextMain = Color.White
      TextSub = Color(0xFFCBD5E1)
      DarkSurface = preset.tertiaryAccent
      DarkSurfaceElevated = Color(
        red = (preset.tertiaryAccent.red + 0.05f).coerceAtMost(1f),
        green = (preset.tertiaryAccent.green + 0.05f).coerceAtMost(1f),
        blue = (preset.tertiaryAccent.blue + 0.06f).coerceAtMost(1f),
        alpha = 1f
      )
      GlassBarSurface = preset.tertiaryAccent.copy(alpha = 0.90f)
    } else {
      TextMain = Color(0xFF0F172A)
      TextSub = Color(0xFF334155)
      DarkSurface = preset.tertiaryAccent
      DarkSurfaceElevated = Color.White
      GlassBarSurface = Color.White.copy(alpha = 0.92f)
    }

    AlphaGold = preset.goldAccent
    CyberCyan = preset.primaryAccent
    ShieldGreen = Color(0xFF10B981)
    DeepBackground = preset.baseSurfaceTint
    DarkSurfaceBorder = preset.borderBaseColor
    TextPrimary = TextMain
    TextSecondary = TextSub
  }
}
