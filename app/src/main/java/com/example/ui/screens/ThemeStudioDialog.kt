package com.example.ui.screens

import android.app.WallpaperManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.Shader
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.ui.theme.*
import java.io.File
import java.io.FileOutputStream
import java.util.Locale

data class WallpaperEffectOption(
  val id: String,
  val title: String,
  val subtitle: String
)

val WALLPAPER_EFFECT_OPTIONS = listOf(
  WallpaperEffectOption("PSICO_VORTEX", "Vórtice Nob-Psico", "Espiral psicodélica negro y morado con destellos de oro y plata"),
  WallpaperEffectOption("METRIX_CODE_RAIN", "Lluvia Metrix-Relativ", "Cascada digital Matrix con nodos de oro líquido y plata"),
  WallpaperEffectOption("ADN_PINK_HELIX", "Hélice ADN-Pink", "Cadenas genéticas rosas y negras con puentes de plata y oro"),
  WallpaperEffectOption("SKY_ETER_CELESTIAL", "Cielo Sky-Eter", "Esferas celestes azul y blanco con halos de oro y plata"),
  WallpaperEffectOption("DRAGON_FIRE_EMBERS", "Llamas Dragon-Fire", "Brasas ascendentes rojo-naranja con chispas de oro y plata"),
  WallpaperEffectOption("QUANTUM_ATOM", "Átomo Quantum-Venom", "Órbitas bio-ácidas en rotación con núcleos de oro y plata"),
  WallpaperEffectOption("ARCADE_SYNTH_GRID", "Horizonte Electro-Arcade", "Sol retro-futurista dorado con rejilla neón y cromo"),
  WallpaperEffectOption("SOLAR_ORBIT", "Órbita Solar-Pharaoh", "Anillos gravitacionales imperiales de oro y plata"),
  WallpaperEffectOption("AURORA_WAVES", "Ondas Cosmic-Nebula", "Flujo orgánico multicapa de luz estelar, oro y plata"),
  WallpaperEffectOption("CYBER_GRID", "Malla Cyber-KDF 3D", "Horizonte vectorial interactivo y nodos de energía"),
  WallpaperEffectOption("PARTICLE_MATRIX", "Constelación Cuántica", "Red dinámica de partículas bimetálicas conectadas"),
  WallpaperEffectOption("LUXURY_MESH", "Geometría Bimetálica", "Hexágonos imperiales con destellos de oro y cromo"),
  WallpaperEffectOption("RADAR_HUD", "Radar Táctico KDF", "Escaneo orbital en tiempo real con telemetría"),
  WallpaperEffectOption("STATIC_CLEAN", "Minimalismo Puro", "Superficie limpia de alto contraste sin animación")
)

val ICON_PACK_STYLE_OPTIONS = listOf(
  "NOB_PSICO" to "Pack Nob-Psico (Negro/Morado + Oro & Plata)",
  "METRIX_RELATIV" to "Pack Metrix-Relativ (Matrix + Oro & Plata)",
  "ADN_PINK" to "Pack ADN-Pink (Rosa/Negro + Plata & Oro)",
  "SKY_ETER" to "Pack Sky-Eter (Azul/Blanco + Oro & Plata)",
  "DRAGON_FIRE" to "Pack Dragon-Fire (Rojo/Naranja + Oro & Plata)",
  "QUANTUM_VENOM" to "Pack Quantum-Venom (Bio-Lima + Oro & Plata)",
  "ELECTRO_ARCADE" to "Pack Electro-Arcade (Synthwave + Oro & Plata)",
  "SOLAR_PHARAOH" to "Pack Solar-Pharaoh (Imperial + Oro & Plata)",
  "COSMIC_NEBULA" to "Pack Cosmic-Nebula (Galaxia + Oro & Plata)",
  "ROYAL_GOLD_SILVER" to "Pack Bimetálico Puro (Oro 24K & Plata)"
)

val CLOCK_STYLE_OPTIONS = listOf(
  "CYBER_HUD" to "HUD Táctico KDF",
  "LUXURY_DIGITAL" to "Cronógrafo Oro & Plata",
  "MINIMAL_GLASS" to "Cristal Minimalista",
  "CHRONO_RING" to "Anillo Orbital",
  "COMPACT_PILL" to "Cápsula Ultra-Compacta"
)

val ICON_SHAPE_OPTIONS = listOf(
  "SQUIRCLE" to "Squircle KDF",
  "CIRCLE" to "Círculo Puro",
  "HEXAGON" to "Hexágono Cyber",
  "DIAMOND" to "Diamante Táctico",
  "TEARDROP" to "Gota Dinámica",
  "BIMETALLIC_RING" to "Anillo Oro & Plata"
)

@Composable
fun ThemeStudioDialog(
  isOpen: Boolean,
  onDismiss: () -> Unit
) {
  if (!isOpen) return

  val context = LocalContext.current
  var activeTab by remember { mutableIntStateOf(0) }

  val backgroundPickerLauncher = rememberLauncherForActivityResult(
    ActivityResultContracts.PickVisualMedia()
  ) { uri: Uri? ->
    if (uri != null) {
      try {
        val bgFile = File(context.filesDir, "custom_hyperlux_wallpaper.jpg")
        context.contentResolver.openInputStream(uri)?.use { input ->
          FileOutputStream(bgFile).use { output ->
            input.copyTo(output)
          }
        }
        UiThemeController.updateCustomBackgroundUri(context, bgFile.absolutePath)
        Toast.makeText(context, "Fondo personalizado aplicado en LAUNCH KDF", Toast.LENGTH_SHORT).show()
      } catch (e: Exception) {
        Toast.makeText(context, "Error: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
      }
    }
  }

  Dialog(onDismissRequest = onDismiss) {
    Card(
      modifier = Modifier
        .fillMaxWidth()
        .heightIn(max = 660.dp)
        .skeuomorphicCard(borderColor = MetalSecondary),
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
            Icon(
              Icons.Default.Palette,
              contentDescription = null,
              tint = MetalGold,
              modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Column {
              Text(
                "ESTUDIO LAUNCH KDF • ORO & PLATA",
                color = TextMain,
                fontSize = 13.sp,
                fontWeight = FontWeight.ExtraBold
              )
              Text(
                "Nob-Psico • Metrix-Relativ • ADN-Pink • Sky-Eter • Dragon-Fire",
                color = MetalSilver,
                fontSize = 9.sp
              )
            }
          }
          Row(verticalAlignment = Alignment.CenterVertically) {
            TextButton(
              onClick = {
                UiThemeController.resetLauncherDefaults(context)
                Toast.makeText(context, "Tema Nob-Psico y ajustes restaurados", Toast.LENGTH_SHORT).show()
              },
              contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
            ) {
              Text("RESET", color = MetalGold, fontSize = 10.sp, fontWeight = FontWeight.Bold)
            }
            IconButton(
              onClick = onDismiss,
              modifier = Modifier.size(30.dp)
            ) {
              Icon(Icons.Default.Close, contentDescription = "Cerrar", tint = TextSub, modifier = Modifier.size(18.dp))
            }
          }
        }

        Spacer(modifier = Modifier.height(8.dp))

        TabRow(
          selectedTabIndex = activeTab,
          containerColor = DarkSurface,
          contentColor = MetalSecondary,
          indicator = { tabPositions ->
            TabRowDefaults.SecondaryIndicator(
              Modifier.tabIndicatorOffset(tabPositions[activeTab]),
              color = MetalGold,
              height = 2.dp
            )
          }
        ) {
          Tab(selected = activeTab == 0, onClick = { activeTab = 0 }) {
            Text("TEMAS", modifier = Modifier.padding(vertical = 8.dp), fontSize = 10.sp, fontWeight = FontWeight.Bold)
          }
          Tab(selected = activeTab == 1, onClick = { activeTab = 1 }) {
            Text("FONDOS", modifier = Modifier.padding(vertical = 8.dp), fontSize = 10.sp, fontWeight = FontWeight.Bold)
          }
          Tab(selected = activeTab == 2, onClick = { activeTab = 2 }) {
            Text("ICONOS", modifier = Modifier.padding(vertical = 8.dp), fontSize = 10.sp, fontWeight = FontWeight.Bold)
          }
          Tab(selected = activeTab == 3, onClick = { activeTab = 3 }) {
            Text("INTERFAZ", modifier = Modifier.padding(vertical = 8.dp), fontSize = 10.sp, fontWeight = FontWeight.Bold)
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
              Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
              ) {
                Text(
                  "COLECCIÓN DE TEMAS CON ORO Y PLATA (${HYPER_LUX_THEME_PRESETS.size})",
                  color = MetalGold,
                  fontWeight = FontWeight.ExtraBold,
                  fontSize = 10.sp
                )
              }

              Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier
                  .fillMaxWidth()
                  .clip(RoundedCornerShape(8.dp))
                  .background(DarkSurfaceElevated.copy(alpha = 0.75f))
                  .padding(horizontal = 10.dp, vertical = 4.dp)
              ) {
                Text(
                  "Sincronizar Fondo e Iconos al elegir Tema",
                  color = TextMain,
                  fontSize = 11.sp,
                  fontWeight = FontWeight.SemiBold
                )
                Switch(
                  checked = UiThemeController.autoSyncWallpaperWithTheme,
                  onCheckedChange = { UiThemeController.updateAutoSyncWallpaperWithTheme(context, it) },
                  colors = SwitchDefaults.colors(checkedThumbColor = MetalGold)
                )
              }

              Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                HYPER_LUX_THEME_PRESETS.forEach { preset ->
                  val isSelected = UiThemeController.selectedPresetId == preset.id
                  Row(
                    modifier = Modifier
                      .fillMaxWidth()
                      .clip(RoundedCornerShape(10.dp))
                      .background(
                        if (isSelected) preset.secondaryAccent.copy(alpha = 0.18f)
                        else DarkSurfaceElevated.copy(alpha = 0.75f)
                      )
                      .border(
                        width = if (isSelected) 1.6.dp else 0.8.dp,
                        brush = Brush.horizontalGradient(
                          if (isSelected) {
                            listOf(preset.goldAccent, preset.secondaryAccent, preset.silverAccent)
                          } else {
                            listOf(preset.goldAccent.copy(alpha = 0.35f), preset.silverAccent.copy(alpha = 0.35f))
                          }
                        ),
                        shape = RoundedCornerShape(10.dp)
                      )
                      .clickable {
                        if (preset.isPremium && !UiThemeController.isPremium) {
                          Toast.makeText(context, "Este tema es Premium", Toast.LENGTH_SHORT).show()
                        } else {
                          UiThemeController.selectPreset(context, preset.id)
                        }
                      }
                      .padding(horizontal = 10.dp, vertical = 8.dp)
                      .testTag("theme_preset_${preset.id}"),
                    verticalAlignment = Alignment.CenterVertically
                  ) {
                    // Quad-color preview badge (Base + Theme Color + Gold + Silver)
                    Box(
                      modifier = Modifier
                        .size(34.dp)
                        .clip(CircleShape)
                        .background(preset.baseSurfaceTint)
                        .border(
                          width = 2.dp,
                          brush = Brush.sweepGradient(
                            listOf(preset.goldAccent, preset.silverAccent, preset.secondaryAccent, preset.goldAccent)
                          ),
                          shape = CircleShape
                        ),
                      contentAlignment = Alignment.Center
                    ) {
                      Box(
                        modifier = Modifier
                          .size(16.dp)
                          .background(preset.secondaryAccent, CircleShape)
                          .border(1.dp, preset.primaryAccent, CircleShape)
                      )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column(modifier = Modifier.weight(1f)) {
                      Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(preset.name, color = TextMain, fontWeight = FontWeight.ExtraBold, fontSize = 12.sp)
                        Spacer(modifier = Modifier.width(6.dp))
                        if (preset.isPremium && !UiThemeController.isPremium) {
                          Icon(Icons.Default.Lock, contentDescription = "Premium", tint = MetalGold, modifier = Modifier.size(12.dp))
                        }
                        // Gold & Silver mini pills
                        Box(
                          modifier = Modifier
                            .size(8.dp)
                            .background(preset.goldAccent, CircleShape)
                        )
                        Spacer(modifier = Modifier.width(3.dp))
                        Box(
                          modifier = Modifier
                            .size(8.dp)
                            .background(preset.silverAccent, CircleShape)
                        )
                      }
                      Text(preset.subtitle, color = TextSub, fontSize = 10.sp)
                    }
                    if (isSelected) {
                      Icon(
                        Icons.Default.CheckCircle,
                        contentDescription = "Seleccionado",
                        tint = preset.goldAccent,
                        modifier = Modifier.size(18.dp)
                      )
                    }
                  }
                }
              }
            }

            1 -> {
              Text(
                "FONDOS ANIMADOS DIVERTIDOS (60 FPS ORO & PLATA)",
                color = MetalGold,
                fontWeight = FontWeight.ExtraBold,
                fontSize = 10.sp
              )
              WALLPAPER_EFFECT_OPTIONS.forEach { effect ->
                val selected = UiThemeController.wallpaperEffectId == effect.id
                Row(
                  modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(if (selected) MetalSecondary.copy(alpha = 0.16f) else DarkSurfaceElevated)
                    .border(
                      width = if (selected) 1.5.dp else 0.7.dp,
                      brush = Brush.horizontalGradient(
                        if (selected) listOf(MetalGold, MetalSecondary, MetalSilver)
                        else listOf(MetalGold.copy(alpha = 0.25f), MetalSilver.copy(alpha = 0.25f))
                      ),
                      shape = RoundedCornerShape(10.dp)
                    )
                    .clickable { UiThemeController.updateWallpaperEffect(context, effect.id) }
                    .padding(10.dp)
                    .testTag("wallpaper_effect_${effect.id}"),
                  verticalAlignment = Alignment.CenterVertically
                ) {
                  Icon(
                    imageVector = Icons.Default.AutoAwesome,
                    contentDescription = null,
                    tint = if (selected) MetalGold else MetalSilver,
                    modifier = Modifier.size(18.dp)
                  )
                  Spacer(modifier = Modifier.width(10.dp))
                  Column(modifier = Modifier.weight(1f)) {
                    Text(effect.title, color = TextMain, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    Text(effect.subtitle, color = TextSub, fontSize = 10.sp)
                  }
                  if (selected) {
                    Icon(Icons.Default.Check, contentDescription = null, tint = MetalGold, modifier = Modifier.size(16.dp))
                  }
                }
              }

              Spacer(modifier = Modifier.height(4.dp))
              Text(
                "VELOCIDAD DE ANIMACIÓN: ${String.format(Locale.US, "%.1fx", UiThemeController.animationSpeed)}",
                color = TextMain,
                fontWeight = FontWeight.Bold,
                fontSize = 11.sp
              )
              Slider(
                value = UiThemeController.animationSpeed,
                onValueChange = { UiThemeController.updateAnimationSpeed(context, it) },
                valueRange = 0.2f..2.5f,
                colors = SliderDefaults.colors(thumbColor = MetalGold, activeTrackColor = MetalSecondary)
              )

              Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
              ) {
                OutlinedButton(
                  onClick = {
                    backgroundPickerLauncher.launch(
                      PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                    )
                  },
                  modifier = Modifier.weight(1f),
                  contentPadding = PaddingValues(vertical = 8.dp, horizontal = 8.dp)
                ) {
                  Icon(Icons.Default.PhotoLibrary, contentDescription = null, tint = MetalGold, modifier = Modifier.size(16.dp))
                  Spacer(modifier = Modifier.width(6.dp))
                  Text("Imagen Galería", color = TextMain, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }

                OutlinedButton(
                  onClick = {
                    applyCurrentThemeToSystemWallpaper(context)
                  },
                  modifier = Modifier.weight(1f),
                  contentPadding = PaddingValues(vertical = 8.dp, horizontal = 8.dp)
                ) {
                  Icon(Icons.Default.Wallpaper, contentDescription = null, tint = MetalSilver, modifier = Modifier.size(16.dp))
                  Spacer(modifier = Modifier.width(6.dp))
                  Text("Fijar en Android", color = TextMain, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
              }

              if (UiThemeController.customBackgroundUri != null) {
                TextButton(
                  onClick = { UiThemeController.updateCustomBackgroundUri(context, null) },
                  modifier = Modifier.fillMaxWidth()
                ) {
                  Text("Quitar imagen personalizada de galería", color = DangerRed, fontSize = 11.sp)
                }
              }
            }

            2 -> {
              Text(
                "PACK DE ICONOS TEMÁTICOS (CON ORO Y PLATA)",
                color = MetalGold,
                fontWeight = FontWeight.ExtraBold,
                fontSize = 10.sp
              )
              Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                ICON_PACK_STYLE_OPTIONS.forEach { (id, label) ->
                  val selected = UiThemeController.iconPackStyleId == id
                  Row(
                    modifier = Modifier
                      .fillMaxWidth()
                      .clip(RoundedCornerShape(10.dp))
                      .background(if (selected) MetalSecondary.copy(alpha = 0.16f) else DarkSurfaceElevated)
                      .border(
                        width = if (selected) 1.5.dp else 0.7.dp,
                        brush = Brush.horizontalGradient(
                          if (selected) listOf(MetalGold, MetalSecondary, MetalSilver)
                          else listOf(MetalGold.copy(alpha = 0.25f), MetalSilver.copy(alpha = 0.25f))
                        ),
                        shape = RoundedCornerShape(10.dp)
                      )
                      .clickable { UiThemeController.updateIconPackStyle(context, id) }
                      .padding(horizontal = 12.dp, vertical = 8.dp)
                      .testTag("icon_pack_style_$id"),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                  ) {
                    Text(label, color = TextMain, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                    if (selected) {
                      Icon(Icons.Default.CheckCircle, contentDescription = null, tint = MetalGold, modifier = Modifier.size(16.dp))
                    }
                  }
                }
              }

              Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
              ) {
                Text("Aura Oro/Plata y Emblemas en Iconos", color = TextMain, fontSize = 12.sp)
                Switch(
                  checked = UiThemeController.applyThemeTintToAppIcons,
                  onCheckedChange = { UiThemeController.updateApplyThemeTintToAppIcons(context, it) },
                  colors = SwitchDefaults.colors(checkedThumbColor = MetalGold)
                )
              }

              Spacer(modifier = Modifier.height(4.dp))
              Text(
                "GEOMETRÍA DE ICONOS",
                color = MetalSilver,
                fontWeight = FontWeight.Bold,
                fontSize = 10.sp
              )
              Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                ICON_SHAPE_OPTIONS.forEach { (id, label) ->
                  val selected = UiThemeController.iconShapeId == id
                  Row(
                    modifier = Modifier
                      .fillMaxWidth()
                      .clip(RoundedCornerShape(10.dp))
                      .background(if (selected) MetalSecondary.copy(alpha = 0.15f) else DarkSurfaceElevated)
                      .border(
                        width = if (selected) 1.4.dp else 0.7.dp,
                        color = if (selected) MetalGold else MetalPrimary.copy(alpha = 0.25f),
                        shape = RoundedCornerShape(10.dp)
                      )
                      .clickable { UiThemeController.updateIconShape(context, id) }
                      .padding(horizontal = 12.dp, vertical = 8.dp)
                      .testTag("icon_shape_$id"),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                  ) {
                    Text(label, color = TextMain, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    if (selected) {
                      Icon(Icons.Default.CheckCircle, contentDescription = null, tint = MetalGold, modifier = Modifier.size(16.dp))
                    }
                  }
                }
              }

              Spacer(modifier = Modifier.height(6.dp))
              Text(
                "COLUMNAS DE LA GRILLA: ${UiThemeController.gridColumns} COLUMNAS",
                color = MetalGold,
                fontWeight = FontWeight.Bold,
                fontSize = 10.sp
              )
              Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
              ) {
                listOf(3, 4, 5, 6).forEach { cols ->
                  val selected = UiThemeController.gridColumns == cols
                  Box(
                    modifier = Modifier
                      .weight(1f)
                      .clip(RoundedCornerShape(8.dp))
                      .background(if (selected) MetalGold else DarkSurfaceElevated)
                      .border(1.dp, MetalGold, RoundedCornerShape(8.dp))
                      .clickable { UiThemeController.updateGridColumns(context, cols) }
                      .padding(vertical = 8.dp),
                    contentAlignment = Alignment.Center
                  ) {
                    Text(
                      text = "${cols}x",
                      color = if (selected) Color.Black else TextMain,
                      fontWeight = FontWeight.ExtraBold,
                      fontSize = 12.sp
                    )
                  }
                }
              }

              Spacer(modifier = Modifier.height(6.dp))
              Text(
                "TAMAÑO DE ICONOS: ${UiThemeController.iconSizeDp.toInt()} DP",
                color = TextMain,
                fontWeight = FontWeight.Bold,
                fontSize = 11.sp
              )
              Slider(
                value = UiThemeController.iconSizeDp,
                onValueChange = { UiThemeController.updateIconSizeDp(context, it) },
                valueRange = 38f..66f,
                colors = SliderDefaults.colors(thumbColor = MetalGold, activeTrackColor = MetalSecondary)
              )

              Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
              ) {
                Text("Mostrar nombres de aplicaciones", color = TextMain, fontSize = 12.sp)
                Switch(
                  checked = UiThemeController.showAppLabels,
                  onCheckedChange = { UiThemeController.updateShowAppLabels(context, it) },
                  colors = SwitchDefaults.colors(checkedThumbColor = MetalGold)
                )
              }
            }

            3 -> {
              Text(
                "ESTILO DEL RELOJ CENTRAL & WIDGET HUD",
                color = MetalGold,
                fontWeight = FontWeight.Bold,
                fontSize = 10.sp
              )
              Row(
                modifier = Modifier
                  .fillMaxWidth()
                  .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
              ) {
                CLOCK_STYLE_OPTIONS.forEach { (id, label) ->
                  val selected = UiThemeController.clockStyleId == id
                  FilterChip(
                    selected = selected,
                    onClick = { UiThemeController.updateClockStyle(context, id) },
                    label = { Text(label, fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                    colors = FilterChipDefaults.filterChipColors(
                      selectedContainerColor = MetalGold.copy(alpha = 0.22f),
                      selectedLabelColor = MetalGold
                    )
                  )
                }
              }

              Text(
                "OPACIDAD DE CRISTAL (GLASSMORPHISM): ${(UiThemeController.glassOpacity * 100).toInt()}%",
                color = TextMain,
                fontWeight = FontWeight.Bold,
                fontSize = 11.sp
              )
              Slider(
                value = UiThemeController.glassOpacity,
                onValueChange = { UiThemeController.updateGlassOpacity(context, it) },
                valueRange = 0.40f..1.0f,
                colors = SliderDefaults.colors(thumbColor = MetalGold, activeTrackColor = MetalSecondary)
              )

              Text(
                "ESCALA DE TIPOGRAFÍA GLOBAL: ${(UiThemeController.uiScaleMultiplier * 100).toInt()}%",
                color = TextMain,
                fontWeight = FontWeight.Bold,
                fontSize = 11.sp
              )
              Slider(
                value = UiThemeController.uiScaleMultiplier,
                onValueChange = { UiThemeController.updateUiScale(context, it) },
                valueRange = 0.85f..1.20f,
                colors = SliderDefaults.colors(thumbColor = MetalGold, activeTrackColor = MetalSecondary)
              )

              Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
              ) {
                Text("Dock Inferior Compacto", color = TextMain, fontSize = 12.sp)
                Switch(
                  checked = UiThemeController.showFloatingDock,
                  onCheckedChange = { UiThemeController.updateShowFloatingDock(context, it) },
                  colors = SwitchDefaults.colors(checkedThumbColor = MetalGold)
                )
              }

              Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
              ) {
                Text("Barra de Telemetría (CPU/RAM/Batería)", color = TextMain, fontSize = 12.sp)
                Switch(
                  checked = UiThemeController.showTelemetryBar,
                  onCheckedChange = { UiThemeController.updateShowTelemetryBar(context, it) },
                  colors = SwitchDefaults.colors(checkedThumbColor = MetalGold)
                )
              }

              Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
              ) {
                Text("Botón Flotante de Voz IA", color = TextMain, fontSize = 12.sp)
                Switch(
                  checked = UiThemeController.isFloatingButtonEnabled,
                  onCheckedChange = { UiThemeController.updateFloatingButtonEnabled(context, it) },
                  colors = SwitchDefaults.colors(checkedThumbColor = MetalGold)
                )
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
            .height(40.dp)
            .testTag("save_theme_studio_button"),
          colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent, contentColor = TextMain),
          contentPadding = PaddingValues(vertical = 4.dp)
        ) {
          Text("APLICAR Y VOLVER AL LAUNCHER", fontWeight = FontWeight.ExtraBold, fontSize = 12.sp)
        }
      }
    }
  }
}

private fun applyCurrentThemeToSystemWallpaper(context: android.content.Context) {
  try {
    val wm = WallpaperManager.getInstance(context)
    val bmp = Bitmap.createBitmap(1080, 1920, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(bmp)
    val paint = Paint(Paint.ANTI_ALIAS_FLAG)
    paint.shader = LinearGradient(
      0f, 0f, 1080f, 1920f,
      intArrayOf(
        SurfacePearl.toArgb(),
        DarkSurface.toArgb(),
        MetalSecondary.copy(alpha = 0.45f).toArgb(),
        MetalGold.copy(alpha = 0.35f).toArgb()
      ),
      floatArrayOf(0f, 0.55f, 0.85f, 1f),
      Shader.TileMode.CLAMP
    )
    canvas.drawRect(0f, 0f, 1080f, 1920f, paint)

    // Draw gold and silver accent geometry onto the Android system wallpaper
    val goldPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
      color = MetalGold.copy(alpha = 0.32f).toArgb()
      strokeWidth = 2.5f
      style = Paint.Style.STROKE
    }
    val silverPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
      color = MetalSilver.copy(alpha = 0.25f).toArgb()
      strokeWidth = 1.8f
      style = Paint.Style.STROKE
    }
    for (x in 0..1080 step 90) {
      canvas.drawLine(x.toFloat(), 0f, x.toFloat(), 1920f, if (x % 180 == 0) goldPaint else silverPaint)
    }
    for (y in 0..1920 step 90) {
      canvas.drawLine(0f, y.toFloat(), 1080f, y.toFloat(), if (y % 180 == 0) goldPaint else silverPaint)
    }
    for (r in 1..5) {
      canvas.drawCircle(540f, 860f, r * 140f, if (r % 2 == 0) goldPaint else silverPaint)
    }

    wm.setBitmap(bmp)
    Toast.makeText(context, "Fondo de pantalla del sistema Android actualizado con Oro y Plata", Toast.LENGTH_SHORT).show()
  } catch (e: Exception) {
    Toast.makeText(context, "Fondo aplicado en LAUNCH KDF", Toast.LENGTH_SHORT).show()
  }
}
