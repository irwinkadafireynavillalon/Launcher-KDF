package com.example.audio

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import android.net.Uri
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.content.FileProvider
import com.example.MainActivity
import com.example.R
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.nio.ByteBuffer
import java.nio.ByteOrder
import kotlin.math.PI
import kotlin.math.exp
import kotlin.math.sin

data class KdfTone(
  val id: String,
  val name: String,
  val category: String,
  val iconEmoji: String,
  val sampleRate: Int = 48000,
  val bitDepth: String = "24-bit Studio Hi-Res",
  val durationMs: Int,
  val description: String,
  val synthesisFormula: (timeSec: Float, durationSec: Float) -> Float
)

object KdfAudioToneManager {
  private const val TAG = "KdfAudioToneManager"
  const val CHANNEL_ID = "kdf_4k_notifications_channel"
  const val CHANNEL_NAME = "Tonos Notificaciones 4K KDF"

  private val _isPlaying = MutableStateFlow(false)
  val isPlaying = _isPlaying.asStateFlow()

  private val _activeToneId = MutableStateFlow<String?>("gold_chime_4k")
  val activeToneId = _activeToneId.asStateFlow()

  private val _playingToneId = MutableStateFlow<String?>(null)
  val playingToneId = _playingToneId.asStateFlow()

  private val _waveformSamples = MutableStateFlow<List<Float>>(emptyList())
  val waveformSamples = _waveformSamples.asStateFlow()

  private var activePlaybackJob: Job? = null
  private var currentAudioTrack: AudioTrack? = null

  val TONES: List<KdfTone> = listOf(
    KdfTone(
      id = "gold_chime_4k",
      name = "KDF Royal Gold 4K",
      category = "👑 KDF OS",
      iconEmoji = "✨",
      durationMs = 1200,
      description = "Campana de oro 24K multi-armónica con decaimiento cristalino y brillo estéreo.",
      synthesisFormula = { t, d ->
        val env = exp(-3.2f * (t / d))
        val f0 = 880f // A5
        val h1 = sin(2f * PI.toFloat() * f0 * t) * 0.5f
        val h2 = sin(2f * PI.toFloat() * (f0 * 2f) * t) * 0.25f
        val h3 = sin(2f * PI.toFloat() * (f0 * 3f) * t) * 0.15f
        val h4 = sin(2f * PI.toFloat() * (f0 * 4.2f) * t) * 0.10f
        val shimmer = sin(2f * PI.toFloat() * 12f * t) * 0.05f
        (h1 + h2 + h3 + h4 + shimmer) * env
      }
    ),
    KdfTone(
      id = "vortex_pulse_4k",
      name = "Psico Vortex 4K",
      category = "👑 KDF OS",
      iconEmoji = "🌀",
      durationMs = 950,
      description = "Pulso sub-armónico cibernético con barrido morado y resonancia profunda.",
      synthesisFormula = { t, d ->
        val env = exp(-2.8f * (t / d))
        val sweepFreq = 380f - (220f * (t / d))
        val sub = sin(2f * PI.toFloat() * sweepFreq * t) * 0.6f
        val res = sin(2f * PI.toFloat() * (sweepFreq * 2.5f) * t) * 0.25f
        val subBass = sin(2f * PI.toFloat() * 75f * t) * 0.35f
        (sub + res + subBass) * env
      }
    ),
    KdfTone(
      id = "metrix_arp_4k",
      name = "Metrix Relativ 4K",
      category = "👑 KDF OS",
      iconEmoji = "🟩",
      durationMs = 850,
      description = "Arpegio de datos de 8 pulsos digitales ascendentes con resonancia plata y oro.",
      synthesisFormula = { t, d ->
        val noteIndex = (t / d * 8f).toInt().coerceIn(0, 7)
        val noteFreqs = floatArrayOf(523.25f, 659.25f, 783.99f, 987.77f, 1046.50f, 1318.51f, 1567.98f, 2093.00f)
        val noteTime = (t - (noteIndex * d / 8f)).coerceAtLeast(0f)
        val noteEnv = exp(-14f * (noteTime / (d / 8f)))
        val currentFreq = noteFreqs[noteIndex]
        val tone = sin(2f * PI.toFloat() * currentFreq * t) * 0.7f
        val sparkle = sin(2f * PI.toFloat() * (currentFreq * 2f) * t) * 0.25f
        (tone + sparkle) * noteEnv
      }
    ),
    KdfTone(
      id = "adn_pink_echo_4k",
      name = "ADN-Pink Pulse 4K",
      category = "👑 KDF OS",
      iconEmoji = "🧬",
      durationMs = 1100,
      description = "Ping sintético electro-rosado con decaimiento estéreo y saturación suave.",
      synthesisFormula = { t, d ->
        val env = exp(-3.0f * (t / d))
        val toneA = sin(2f * PI.toFloat() * 659.25f * t) * 0.5f // E5
        val toneB = sin(2f * PI.toFloat() * 987.77f * t) * 0.35f // B5
        val toneC = sin(2f * PI.toFloat() * 1318.51f * t) * 0.2f // E6
        (toneA + toneB + toneC) * env
      }
    ),
    KdfTone(
      id = "sky_eter_harp_4k",
      name = "Sky-Eter Celestial 4K",
      category = "👑 KDF OS",
      iconEmoji = "🌌",
      durationMs = 1400,
      description = "Acorde sagrado 528 Hz Solfeggio con armónicos celestiales y brillo plateado.",
      synthesisFormula = { t, d ->
        val attack = (t / 0.08f).coerceAtMost(1.0f)
        val decay = exp(-2.2f * (t / d))
        val env = attack * decay
        val base = sin(2f * PI.toFloat() * 528f * t) * 0.45f
        val harm1 = sin(2f * PI.toFloat() * 1056f * t) * 0.30f
        val harm2 = sin(2f * PI.toFloat() * 1584f * t) * 0.18f
        val angel = sin(2f * PI.toFloat() * 2112f * t) * 0.10f
        (base + harm1 + harm2 + angel) * env
      }
    ),
    KdfTone(
      id = "dragon_amber_4k",
      name = "Dragon-Fire Alert 4K",
      category = "👑 KDF OS",
      iconEmoji = "🔥",
      durationMs = 1000,
      description = "Impacto cálido con curva de frecuencia baja, chisporroteo y resonancia volcánica.",
      synthesisFormula = { t, d ->
        val env = exp(-3.5f * (t / d))
        val punchFreq = 180f - (120f * (t / d))
        val punch = sin(2f * PI.toFloat() * punchFreq * t) * 0.65f
        val sub = sin(2f * PI.toFloat() * 55f * t) * 0.4f
        val crackle = ((Math.random().toFloat() * 2f - 1f) * 0.08f) * exp(-8f * (t / d))
        (punch + sub + crackle) * env
      }
    ),
    KdfTone(
      id = "android_pop_4k",
      name = "Android Material Pop 4K",
      category = "🤖 Android",
      iconEmoji = "🫧",
      durationMs = 380,
      description = "Pop táctil ultra-nítido estilo Android Pixel con respuesta háptica instantánea.",
      synthesisFormula = { t, d ->
        val env = exp(-9.5f * (t / d))
        val popFreq = 1400f - (800f * (t / d))
        val tone = sin(2f * PI.toFloat() * popFreq * t) * 0.8f
        val sub = sin(2f * PI.toFloat() * 450f * t) * 0.25f
        (tone + sub) * env
      }
    ),
    KdfTone(
      id = "ios_tritone_4k",
      name = "iOS Tri-Tone 4K Master",
      category = "🍏 iOS",
      iconEmoji = "🔔",
      durationMs = 900,
      description = "El icónico Tri-Tone de Apple remasterizado a 48kHz en alta fidelidad de estudio.",
      synthesisFormula = { t, d ->
        val segment = (t / d * 3f).toInt().coerceIn(0, 2)
        val freqs = floatArrayOf(783.99f, 1046.50f, 1318.51f) // G5, C6, E6
        val segmentTime = (t - (segment * d / 3f)).coerceAtLeast(0f)
        val segEnv = exp(-4.2f * (segmentTime / (d / 3f)))
        val currentFreq = freqs[segment]
        val mainTone = sin(2f * PI.toFloat() * currentFreq * t) * 0.65f
        val overtone = sin(2f * PI.toFloat() * (currentFreq * 2f) * t) * 0.22f
        (mainTone + overtone) * segEnv
      }
    ),
    KdfTone(
      id = "quantum_tachyon_4k",
      name = "Quantum Tachyon 4K",
      category = "👑 KDF OS",
      iconEmoji = "⚡",
      durationMs = 600,
      description = "Micro-sweep taquiónico cuántico de 1.2 kHz a 4.8 kHz con bloqueo gravitacional.",
      synthesisFormula = { t, d ->
        val env = exp(-4.0f * (t / d))
        val chirpFreq = 1200f + (3600f * (t / d))
        val chirp = sin(2f * PI.toFloat() * chirpFreq * t) * 0.6f
        val lock = sin(2f * PI.toFloat() * 150f * t) * 0.35f
        (chirp + lock) * env
      }
    ),
    KdfTone(
      id = "apex_champagne_4k",
      name = "Apex VIP Champagne 4K",
      category = "👑 KDF OS",
      iconEmoji = "🥂",
      durationMs = 1350,
      description = "Resonancia acústica de copa de cristal VIP C7 con sustain largo y brillo dorado.",
      synthesisFormula = { t, d ->
        val env = exp(-2.4f * (t / d))
        val f0 = 2093f // C7
        val glass1 = sin(2f * PI.toFloat() * f0 * t) * 0.55f
        val glass2 = sin(2f * PI.toFloat() * (f0 * 1.51f) * t) * 0.28f
        val glass3 = sin(2f * PI.toFloat() * (f0 * 2.38f) * t) * 0.12f
        (glass1 + glass2 + glass3) * env
      }
    )
  )

  fun initNotificationChannel(context: Context) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
      val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager
        ?: return
      val channel = NotificationChannel(
        CHANNEL_ID,
        CHANNEL_NAME,
        NotificationManager.IMPORTANCE_HIGH
      ).apply {
        description = "Notificaciones del sistema y alertas KDF en Ultra Alta Definición 4K"
        enableLights(true)
        enableVibration(true)
        vibrationPattern = longArrayOf(0, 80, 40, 120)
        setShowBadge(true)
      }
      notificationManager.createNotificationChannel(channel)
    }
  }

  fun previewTone(
    context: Context,
    tone: KdfTone,
    scope: CoroutineScope,
    onComplete: (() -> Unit)? = null
  ) {
    stopPlayback()

    activePlaybackJob = scope.launch(Dispatchers.Default) {
      _isPlaying.value = true
      _playingToneId.value = tone.id

      // Haptic pulse
      triggerHaptic(context)

      try {
        val sampleRate = tone.sampleRate
        val totalSamples = (sampleRate * (tone.durationMs / 1000f)).toInt()
        val pcmData = ShortArray(totalSamples)
        val vizSamples = mutableListOf<Float>()
        val vizStep = (totalSamples / 36).coerceAtLeast(1)

        val durationSec = tone.durationMs / 1000f
        for (i in 0 until totalSamples) {
          val t = i.toFloat() / sampleRate
          val sampleFloat = tone.synthesisFormula(t, durationSec).coerceIn(-1.0f, 1.0f)
          pcmData[i] = (sampleFloat * Short.MAX_VALUE).toInt().toShort()

          if (i % vizStep == 0 && vizSamples.size < 36) {
            vizSamples.add(kotlin.math.abs(sampleFloat))
          }
        }

        _waveformSamples.value = vizSamples

        val minBufferSize = AudioTrack.getMinBufferSize(
          sampleRate,
          AudioFormat.CHANNEL_OUT_MONO,
          AudioFormat.ENCODING_PCM_16BIT
        )
        val bufferSize = minBufferSize.coerceAtLeast(totalSamples * 2)

        val track = AudioTrack.Builder()
          .setAudioAttributes(
            AudioAttributes.Builder()
              .setUsage(AudioAttributes.USAGE_NOTIFICATION)
              .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
              .build()
          )
          .setAudioFormat(
            AudioFormat.Builder()
              .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
              .setSampleRate(sampleRate)
              .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
              .build()
          )
          .setBufferSizeInBytes(bufferSize)
          .setTransferMode(AudioTrack.MODE_STATIC)
          .build()

        currentAudioTrack = track
        track.write(pcmData, 0, totalSamples)
        track.play()

        // Wait for tone duration
        kotlinx.coroutines.delay(tone.durationMs.toLong() + 50L)

        track.stop()
        track.release()
      } catch (e: Exception) {
        Log.e(TAG, "Error playing 4K tone: ${e.message}", e)
      } finally {
        currentAudioTrack = null
        _isPlaying.value = false
        _playingToneId.value = null
        withContext(Dispatchers.Main) {
          onComplete?.invoke()
        }
      }
    }
  }

  fun stopPlayback() {
    activePlaybackJob?.cancel()
    activePlaybackJob = null
    try {
      currentAudioTrack?.apply {
        if (playState == AudioTrack.PLAYSTATE_PLAYING) {
          stop()
        }
        release()
      }
    } catch (_: Exception) {}
    currentAudioTrack = null
    _isPlaying.value = false
    _playingToneId.value = null
  }

  fun setActiveTone(toneId: String) {
    _activeToneId.value = toneId
  }

  fun generateWavFile(context: Context, tone: KdfTone): File {
    val dir = File(context.filesDir, "kdf_4k_tones").apply { mkdirs() }
    val wavFile = File(dir, "${tone.id}.wav")
    if (wavFile.exists() && wavFile.length() > 100) {
      return wavFile
    }

    val sampleRate = tone.sampleRate
    val totalSamples = (sampleRate * (tone.durationMs / 1000f)).toInt()
    val pcmShorts = ShortArray(totalSamples)
    val durationSec = tone.durationMs / 1000f

    for (i in 0 until totalSamples) {
      val t = i.toFloat() / sampleRate
      val s = tone.synthesisFormula(t, durationSec).coerceIn(-1.0f, 1.0f)
      pcmShorts[i] = (s * Short.MAX_VALUE).toInt().toShort()
    }

    val byteCount = totalSamples * 2
    val headerSize = 44
    val totalSize = headerSize + byteCount - 8

    FileOutputStream(wavFile).use { fos ->
      val header = ByteBuffer.allocate(headerSize).order(ByteOrder.LITTLE_ENDIAN)
      header.put("RIFF".toByteArray())
      header.putInt(totalSize)
      header.put("WAVE".toByteArray())
      header.put("fmt ".toByteArray())
      header.putInt(16) // Subchunk1Size for PCM
      header.putShort(1.toShort()) // AudioFormat 1 = PCM
      header.putShort(1.toShort()) // NumChannels 1 = Mono
      header.putInt(sampleRate)
      header.putInt(sampleRate * 2) // ByteRate = SampleRate * NumChannels * BitsPerSample/8
      header.putShort(2.toShort()) // BlockAlign = NumChannels * BitsPerSample/8
      header.putShort(16.toShort()) // BitsPerSample
      header.put("data".toByteArray())
      header.putInt(byteCount)

      fos.write(header.array())

      val dataBuffer = ByteBuffer.allocate(byteCount).order(ByteOrder.LITTLE_ENDIAN)
      for (sample in pcmShorts) {
        dataBuffer.putShort(sample)
      }
      fos.write(dataBuffer.array())
      fos.flush()
    }

    return wavFile
  }

  fun sendTestNotification(
    context: Context,
    tone: KdfTone,
    scope: CoroutineScope
  ) {
    initNotificationChannel(context)

    // Trigger local audio synthesis preview synchronously for maximum audio punch
    previewTone(context, tone, scope)

    val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager
      ?: return

    val intent = Intent(context, MainActivity::class.java).apply {
      flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
    }
    val pendingIntent = PendingIntent.getActivity(
      context,
      1001,
      intent,
      PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
    )

    val notification = NotificationCompat.Builder(context, CHANNEL_ID)
      .setSmallIcon(R.mipmap.ic_launcher)
      .setContentTitle("👑 LAUNCH KDF 4K • ${tone.name}")
      .setContentText("Alerta con audio 4K Studio a 48,000 Hz: ${tone.description}")
      .setStyle(
        NotificationCompat.BigTextStyle()
          .bigText("${tone.description}\n\nFrecuencia: ${tone.sampleRate} Hz | Calidad: ${tone.bitDepth} | Categoría: ${tone.category}")
      )
      .setPriority(NotificationCompat.PRIORITY_HIGH)
      .setContentIntent(pendingIntent)
      .setAutoCancel(true)
      .setVibrate(longArrayOf(0, 100, 50, 100))
      .build()

    try {
      notificationManager.notify(777, notification)
    } catch (e: SecurityException) {
      Log.e(TAG, "Notification permission missing: ${e.message}")
    }
  }

  private fun triggerHaptic(context: Context) {
    try {
      if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
        val vibrator = vibratorManager?.defaultVibrator
        vibrator?.vibrate(VibrationEffect.createPredefined(VibrationEffect.EFFECT_CLICK))
      } else {
        @Suppress("DEPRECATION")
        val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
          vibrator?.vibrate(VibrationEffect.createOneShot(35, VibrationEffect.DEFAULT_AMPLITUDE))
        } else {
          @Suppress("DEPRECATION")
          vibrator?.vibrate(35)
        }
      }
    } catch (_: Exception) {}
  }
}
