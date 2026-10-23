package com.example.ui.screens
import androidx.compose.ui.geometry.*
import androidx.compose.ui.draw.*
import androidx.compose.ui.graphics.*
import com.example.ui.theme.*

import android.Manifest
import android.content.Context
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
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
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import kotlinx.coroutines.delay

enum class OnboardingStep {
  STARTUP_ANIMATION_5S,
  TERMS_AND_PRIVACY,
  PERMISSIONS,
  GOOGLE_SYNC_OPTIONAL,
  AI_NAME_SELECTION,
  COMPLETED
}

object OnboardingPrefsManager {
  private const val PREF_NAME = "hyperlux_onboarding_prefs"

  fun isCompleted(context: Context): Boolean {
    val sp = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
    return sp.getBoolean("onboarding_completed", false)
  }

  fun setCompleted(context: Context, completed: Boolean) {
    val sp = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
    sp.edit().putBoolean("onboarding_completed", completed).apply()
  }

  fun getAiName(context: Context): String {
    val sp = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
    return sp.getString("ai_custom_name", "Hyper Lux") ?: "Hyper Lux"
  }

  fun setAiName(context: Context, name: String) {
    val sp = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
    sp.edit().putString("ai_custom_name", name.trim().ifEmpty { "Hyper Lux" }).apply()
  }
}

@Composable
fun OnboardingFlowContainer(
  onFinishOnboarding: () -> Unit
) {
  val context = LocalContext.current
  var currentStep by remember { mutableStateOf(OnboardingStep.STARTUP_ANIMATION_5S) }

  Box(
    modifier = Modifier
      .fillMaxSize()
      .damaskBackground()
  ) {
    when (currentStep) {
      OnboardingStep.STARTUP_ANIMATION_5S -> {
        StartupAnimatedSplashScreen(
          onFinished = {
            currentStep = OnboardingStep.TERMS_AND_PRIVACY
          }
        )
      }
      OnboardingStep.TERMS_AND_PRIVACY -> {
        TermsAndPrivacyScreen(
          onAccept = {
            currentStep = OnboardingStep.PERMISSIONS
          }
        )
      }
      OnboardingStep.PERMISSIONS -> {
        PermissionsSetupScreen(
          onContinue = {
            currentStep = OnboardingStep.GOOGLE_SYNC_OPTIONAL
          }
        )
      }
      OnboardingStep.GOOGLE_SYNC_OPTIONAL -> {
        GoogleSyncOptionalScreen(
          onContinue = {
            currentStep = OnboardingStep.AI_NAME_SELECTION
          }
        )
      }
      OnboardingStep.AI_NAME_SELECTION -> {
        AiNameSelectionScreen(
          onComplete = { chosenName ->
            OnboardingPrefsManager.setAiName(context, chosenName)
            OnboardingPrefsManager.setCompleted(context, true)
            onFinishOnboarding()
          }
        )
      }
      OnboardingStep.COMPLETED -> {
        onFinishOnboarding()
      }
    }
  }
}

/**
 * 1. Animated startup of exactly 5 seconds duration
 */
@Composable
fun StartupAnimatedSplashScreen(
  onFinished: () -> Unit
) {
  var secondsElapsed by remember { mutableIntStateOf(0) }
  var progress by remember { mutableFloatStateOf(0f) }

  val infiniteTransition = rememberInfiniteTransition(label = "pulse")
  val pulseScale by infiniteTransition.animateFloat(
    initialValue = 0.94f,
    targetValue = 1.06f,
    animationSpec = infiniteRepeatable(
      animation = tween(1200, easing = FastOutSlowInEasing),
      repeatMode = RepeatMode.Reverse
    ),
    label = "scale"
  )

  val auraAlpha by infiniteTransition.animateFloat(
    initialValue = 0.35f,
    targetValue = 0.90f,
    animationSpec = infiniteRepeatable(
      animation = tween(1400, easing = LinearEasing),
      repeatMode = RepeatMode.Reverse
    ),
    label = "aura"
  )

  val currentStatusText = when (secondsElapsed) {
    0 -> "Inicializando Núcleo IA Híbrido & Memoria Cuántica..."
    1 -> "Calibrando Aceleradores de Hardware CPU / GPU..."
    2 -> "Configurando Servidor Local Autónomo en localhost:8080..."
    3 -> "Asignando Caché de Memoria de 10GB & Sandbox de Seguridad..."
    4 -> "Verificando Protocolos Alpha, Escudo y Autoaprendizaje..."
    else -> "¡Hyper Lux Sistema Operativo Iniciado con Éxito!"
  }

  // 5 seconds timer: 50 ticks of 100ms
  LaunchedEffect(Unit) {
    for (i in 1..50) {
      delay(100)
      progress = i / 50f
      secondsElapsed = (i / 10).coerceAtMost(5)
    }
    delay(200)
    onFinished()
  }

  Box(
    modifier = Modifier
      .fillMaxSize()
      .background(
        Brush.radialGradient(
          colors = listOf(
            MetalSecondary.copy(alpha = 0.1f),
            SurfacePearl,
            Color.White
          )
        )
      ),
    contentAlignment = Alignment.Center
  ) {
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .padding(32.dp),
      horizontalAlignment = Alignment.CenterHorizontally,
      verticalArrangement = Arrangement.Center
    ) {
      // Glowing shield launcher icon
      Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier.size(140.dp)
      ) {
        // Outer aura circle
        Box(
          modifier = Modifier
            .size(136.dp)
            .scale(pulseScale)
            .clip(CircleShape)
            .background(MetalSecondary.copy(alpha = auraAlpha * 0.35f))
        )

        Image(
          painter = painterResource(id = R.drawable.img_alpha_omega_bg_1791765871635),
          contentDescription = "Hyper Lux Logo",
          modifier = Modifier
            .size(110.dp)
            .scale(pulseScale)
            .clip(CircleShape)
            .border(2.5.dp, MetalSecondary, CircleShape)
        )
      }

      Spacer(modifier = Modifier.height(28.dp))

      Text(
        text = "HYPER-LuX",
        color = TextMain,
        fontSize = 28.sp,
        fontWeight = FontWeight.ExtraBold,
        letterSpacing = 2.sp
      )

      Text(
        text = "SISTEMA OPERATIVO DE IA HÍBRIDA & ESCUDO",
        color = MetalPrimary,
        fontSize = 10.sp,
        fontWeight = FontWeight.Bold,
        letterSpacing = 1.sp
      )

      Spacer(modifier = Modifier.height(36.dp))

      // 5-second progress indicator
      LinearProgressIndicator(
        progress = { progress },
        modifier = Modifier
          .fillMaxWidth(0.85f)
          .height(6.dp)
          .clip(RoundedCornerShape(3.dp)),
        color = MetalSecondary,
        trackColor = MetalPrimary
      )

      Spacer(modifier = Modifier.height(14.dp))

      Text(
        text = currentStatusText,
        color = TextSub,
        fontSize = 11.sp,
        textAlign = TextAlign.Center,
        fontFamily = FontFamily.Monospace,
        maxLines = 2
      )

      Spacer(modifier = Modifier.height(8.dp))

      Text(
        text = "Arranque inicial: ${5 - secondsElapsed}s restantes",
        color = TextSub,
        fontSize = 10.sp
      )
    }
  }
}

/**
 * 2. Terms and conditions of the application and AI + Privacy policies
 */
@Composable
fun TermsAndPrivacyScreen(
  onAccept: () -> Unit
) {
  var acceptTerms by remember { mutableStateOf(false) }
  var acceptPrivacy by remember { mutableStateOf(false) }

  Column(
    modifier = Modifier
      .fillMaxSize()
      .padding(20.dp)
      .verticalScroll(rememberScrollState()),
    horizontalAlignment = Alignment.CenterHorizontally,
    verticalArrangement = Arrangement.spacedBy(16.dp)
  ) {
    Spacer(modifier = Modifier.height(20.dp))

    Icon(
      imageVector = Icons.Default.Security,
      contentDescription = null,
      tint = MetalSecondary,
      modifier = Modifier.size(48.dp)
    )

    Text(
      text = "Términos, Condiciones & Privacidad",
      color = TextMain,
      fontSize = 18.sp,
      fontWeight = FontWeight.Bold,
      textAlign = TextAlign.Center
    )

    Text(
      text = "Por favor lee y acepta las directrices de Hyper Lux y del núcleo de Inteligencia Artificial para continuar.",
      color = TextSub,
      fontSize = 12.sp,
      textAlign = TextAlign.Center
    )

    Card(
      modifier = Modifier
        .fillMaxWidth()
        .skeuomorphicCard(borderColor = MetalSecondary),
      colors = CardDefaults.cardColors(containerColor = Color.Transparent)
    ) {
      Column(
        modifier = Modifier.padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
      ) {
        Text(
          text = "1. TÉRMINOS DE LA APLICACIÓN & IA",
          color = MetalSecondary,
          fontSize = 12.sp,
          fontWeight = FontWeight.Bold
        )
        Text(
          text = "• Hyper Lux provee orquestación de inteligencia artificial híbrida.\n• El usuario mantiene total soberanía sobre sus datos.\n• La IA está equipada con autorregulación local.",
          color = TextSub,
          fontSize = 11.sp,
          lineHeight = 16.sp
        )

        Text(
          text = "2. POLÍTICA DE PRIVACIDAD",
          color = MetalPrimary,
          fontSize = 12.sp,
          fontWeight = FontWeight.Bold
        )
        Text(
          text = "• Los modelos se ejecutan localmente sin transmitir información personal.\n• La memoria de autoaprendizaje se almacena localmente de forma segura.",
          color = TextSub,
          fontSize = 11.sp,
          lineHeight = 16.sp
        )
      }
    }

    Spacer(modifier = Modifier.height(10.dp))

    Button(
      onClick = onAccept,
      enabled = acceptTerms && acceptPrivacy,
      modifier = Modifier
        .fillMaxWidth()
        .jewelButton()
        .testTag("accept_terms_continue_button"),
      colors = ButtonDefaults.buttonColors(
        containerColor = Color.Transparent,
        contentColor = TextMain,
        disabledContentColor = PlumbousGray
      )
    ) {
      Text("ACEPTAR Y CONTINUAR", fontWeight = FontWeight.ExtraBold, fontSize = 14.sp)
    }
  }
}

/**
 * 3. App permissions interface
 */
@Composable
fun PermissionsSetupScreen(
  onContinue: () -> Unit
) {
  Column(
    modifier = Modifier
      .fillMaxSize()
      .padding(20.dp)
      .verticalScroll(rememberScrollState()),
    horizontalAlignment = Alignment.CenterHorizontally,
    verticalArrangement = Arrangement.spacedBy(14.dp)
  ) {
    Spacer(modifier = Modifier.height(20.dp))

    Icon(
      imageVector = Icons.Default.Lock,
      contentDescription = null,
      tint = MetalPrimary,
      modifier = Modifier.size(44.dp)
    )

    Text(
      text = "Permisos de la Aplicación",
      color = TextMain,
      fontSize = 18.sp,
      fontWeight = FontWeight.Bold
    )

    Text(
      text = "Para activar todas las capacidades (voz en vivo, cámara visión, servidor en segundo plano y memoria de hasta 10GB), Hyper Lux requiere los siguientes permisos:",
      color = TextSub,
      fontSize = 12.sp,
      textAlign = TextAlign.Center
    )

    PermissionItemCard(
      icon = Icons.Default.Mic,
      title = "Micrófono & Reconocimiento de Voz",
      desc = "Permite comandos de voz en vivo, transcripción y conversación fluida."
    )

    PermissionItemCard(
      icon = Icons.Default.CameraAlt,
      title = "Cámara & Visión Computacional",
      desc = "Permite análisis visual de texto, objetos y escaneo en tiempo real."
    )

    PermissionItemCard(
      icon = Icons.Default.Folder,
      title = "Memoria de Aplicación & Caché (Hasta 10GB)",
      desc = "Aloja los modelos descargados (Gemma/Antigravity) y la memoria de autoaprendizaje."
    )

    PermissionItemCard(
      icon = Icons.Default.NotificationsActive,
      title = "Segundo Plano & Notificaciones Permanentes",
      desc = "Mantiene activo el servidor de IA interno incluso al encender el dispositivo."
    )

    Spacer(modifier = Modifier.height(10.dp))

    Button(
      onClick = onContinue,
      modifier = Modifier
        .fillMaxWidth()
        .height(48.dp)
        .testTag("grant_permissions_continue_button"),
      shape = RoundedCornerShape(12.dp),
      colors = ButtonDefaults.buttonColors(containerColor = MetalPrimary, contentColor = Color.Black)
    ) {
      Text("Configurar y Continuar", fontWeight = FontWeight.Bold, fontSize = 14.sp)
    }
  }
}

@Composable
private fun PermissionItemCard(
  icon: androidx.compose.ui.graphics.vector.ImageVector,
  title: String,
  desc: String
) {
  Row(
    modifier = Modifier
      .fillMaxWidth()
      .clip(RoundedCornerShape(10.dp))
      .background(Color.White)
      .border(0.8.dp, MetalPrimary, RoundedCornerShape(10.dp))
      .padding(12.dp),
    verticalAlignment = Alignment.CenterVertically
  ) {
    Box(
      modifier = Modifier
        .size(36.dp)
        .background(MetalSecondary.copy(alpha = 0.15f), CircleShape),
      contentAlignment = Alignment.Center
    ) {
      Icon(imageVector = icon, contentDescription = null, tint = MetalSecondary, modifier = Modifier.size(20.dp))
    }
    Spacer(modifier = Modifier.width(12.dp))
    Column(modifier = Modifier.weight(1f)) {
      Text(text = title, color = TextMain, fontSize = 12.sp, fontWeight = FontWeight.Bold)
      Text(text = desc, color = TextSub, fontSize = 10.sp)
    }
  }
}

/**
 * 4. Google Account Sync Optional
 */
@Composable
fun GoogleSyncOptionalScreen(
  onContinue: () -> Unit
) {
  var isSynced by remember { mutableStateOf(false) }

  Column(
    modifier = Modifier
      .fillMaxSize()
      .padding(24.dp),
    horizontalAlignment = Alignment.CenterHorizontally,
    verticalArrangement = Arrangement.Center
  ) {
    Icon(
      imageVector = Icons.Default.CloudDone,
      contentDescription = null,
      tint = MetalSecondary,
      modifier = Modifier.size(52.dp)
    )

    Spacer(modifier = Modifier.height(16.dp))

    Text(
      text = "Sincronización con Cuenta Google",
      color = TextMain,
      fontSize = 18.sp,
      fontWeight = FontWeight.Bold,
      textAlign = TextAlign.Center
    )

    Text(
      text = "(Opcional)",
      color = MetalPrimary,
      fontSize = 12.sp,
      fontWeight = FontWeight.Bold
    )

    Spacer(modifier = Modifier.height(16.dp))

    Text(
      text = "Puedes vincular tu cuenta de Google para respaldar tus memorias de autoaprendizaje, configuraciones y checkpoints en Google Drive de forma encriptada.",
      color = TextSub,
      fontSize = 12.sp,
      textAlign = TextAlign.Center,
      lineHeight = 18.sp
    )

    Spacer(modifier = Modifier.height(24.dp))

    Card(
      modifier = Modifier.fillMaxWidth(),
      shape = RoundedCornerShape(12.dp),
      colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
      Column(
        modifier = Modifier.padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
      ) {
        Text("• Respaldo automático de autoaprendizaje en Google Drive", color = TextSub, fontSize = 11.sp)
        Text("• Sincronización entre dispositivos", color = TextSub, fontSize = 11.sp)
        Text("• 100% Opcional: Si omites, Hyper Lux funcionará completamente en modo local.", color = ShieldGreen, fontSize = 11.sp, fontWeight = FontWeight.Bold)
      }
    }

    Spacer(modifier = Modifier.height(30.dp))

    Button(
      onClick = {
        isSynced = true
        onContinue()
      },
      modifier = Modifier
        .fillMaxWidth()
        .height(46.dp),
      shape = RoundedCornerShape(12.dp),
      colors = ButtonDefaults.buttonColors(containerColor = MetalSecondary, contentColor = Color.Black)
    ) {
      Text("Sincronizar con Cuenta Google", fontWeight = FontWeight.Bold, fontSize = 13.sp)
    }

    Spacer(modifier = Modifier.height(10.dp))

    OutlinedButton(
      onClick = onContinue,
      modifier = Modifier
        .fillMaxWidth()
        .height(46.dp),
      shape = RoundedCornerShape(12.dp),
      border = androidx.compose.foundation.BorderStroke(1.dp, MetalPrimary)
    ) {
      Text("Continuar en Modo Local / Omitir por ahora", color = TextSub, fontSize = 12.sp)
    }
  }
}

/**
 * 5. Selection of AI Name (once on install, then kept active in background)
 */
@Composable
fun AiNameSelectionScreen(
  onComplete: (String) -> Unit
) {
  var aiName by remember { mutableStateOf("Hyper Lux") }
  val suggestions = listOf("Hyper Lux", "Aura", "Nova", "Atlas", "Omega", "Nexus")

  Column(
    modifier = Modifier
      .fillMaxSize()
      .padding(24.dp)
      .verticalScroll(rememberScrollState()),
    horizontalAlignment = Alignment.CenterHorizontally,
    verticalArrangement = Arrangement.Center
  ) {
    Icon(
      imageVector = Icons.Default.Psychology,
      contentDescription = null,
      tint = MetalSecondary,
      modifier = Modifier.size(50.dp)
    )

    Spacer(modifier = Modifier.height(16.dp))

    Text(
      text = "Nombra a tu IA Personal",
      color = TextMain,
      fontSize = 20.sp,
      fontWeight = FontWeight.Bold,
      textAlign = TextAlign.Center
    )

    Text(
      text = "Configuración única de instalación. Tu IA se mantendrá activa permanentemente en segundo plano para responder y autoaprender.",
      color = TextSub,
      fontSize = 12.sp,
      textAlign = TextAlign.Center
    )

    Spacer(modifier = Modifier.height(24.dp))

    OutlinedTextField(
      value = aiName,
      onValueChange = { aiName = it },
      label = { Text("Nombre de la IA", color = MetalSecondary) },
      modifier = Modifier
        .fillMaxWidth()
        .testTag("ai_name_input_field"),
      shape = RoundedCornerShape(12.dp),
      colors = OutlinedTextFieldDefaults.colors(
        focusedBorderColor = MetalSecondary,
        unfocusedBorderColor = MetalPrimary,
        focusedContainerColor = Color.White,
        unfocusedContainerColor = Color.White,
        focusedTextColor = TextMain,
        unfocusedTextColor = TextMain
      ),
      singleLine = true
    )

    Spacer(modifier = Modifier.height(14.dp))

    Text(
      text = "Sugerencias rápidas:",
      color = TextSub,
      fontSize = 11.sp,
      modifier = Modifier.align(Alignment.Start)
    )

    Spacer(modifier = Modifier.height(8.dp))

    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
      suggestions.take(3).forEach { suggestion ->
        Box(
          modifier = Modifier
            .weight(1f)
            .clip(RoundedCornerShape(8.dp))
            .background(if (aiName == suggestion) MetalSecondary.copy(alpha = 0.25f) else SurfacePearl)
            .border(0.8.dp, if (aiName == suggestion) MetalSecondary else MetalPrimary, RoundedCornerShape(8.dp))
            .clickable { aiName = suggestion }
            .padding(vertical = 8.dp),
          contentAlignment = Alignment.Center
        ) {
          Text(suggestion, color = if (aiName == suggestion) MetalSecondary else TextSub, fontSize = 11.sp, fontWeight = FontWeight.Bold)
        }
      }
    }

    Spacer(modifier = Modifier.height(6.dp))

    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
      suggestions.drop(3).forEach { suggestion ->
        Box(
          modifier = Modifier
            .weight(1f)
            .clip(RoundedCornerShape(8.dp))
            .background(if (aiName == suggestion) MetalSecondary.copy(alpha = 0.25f) else SurfacePearl)
            .border(0.8.dp, if (aiName == suggestion) MetalSecondary else MetalPrimary, RoundedCornerShape(8.dp))
            .clickable { aiName = suggestion }
            .padding(vertical = 8.dp),
          contentAlignment = Alignment.Center
        ) {
          Text(suggestion, color = if (aiName == suggestion) MetalSecondary else TextSub, fontSize = 11.sp, fontWeight = FontWeight.Bold)
        }
      }
    }

    Spacer(modifier = Modifier.height(24.dp))

    Card(
      modifier = Modifier.fillMaxWidth(),
      shape = RoundedCornerShape(10.dp),
      colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
      Row(
        modifier = Modifier.padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
      ) {
        Icon(Icons.Default.Speed, contentDescription = null, tint = ShieldGreen, modifier = Modifier.size(20.dp))
        Spacer(modifier = Modifier.width(10.dp))
        Text(
          text = "Servidor interno con encendido automático y segundo plano forzoso configurado.",
          color = ShieldGreen,
          fontSize = 11.sp
        )
      }
    }

    Spacer(modifier = Modifier.height(26.dp))

    Button(
      onClick = {
        onComplete(aiName)
      },
      modifier = Modifier
        .fillMaxWidth()
        .height(48.dp)
        .testTag("finish_onboarding_button"),
      shape = RoundedCornerShape(12.dp),
      colors = ButtonDefaults.buttonColors(containerColor = MetalSecondary, contentColor = Color.Black)
    ) {
      Text("Iniciar Experiencia Hyper Lux", fontWeight = FontWeight.Bold, fontSize = 14.sp)
    }
  }
}
