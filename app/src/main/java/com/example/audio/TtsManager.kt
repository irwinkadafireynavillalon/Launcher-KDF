package com.example.audio

import android.content.Context
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import android.util.Log
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.Locale

class TtsManager(context: Context) : TextToSpeech.OnInitListener {
  private var tts: TextToSpeech? = TextToSpeech(context.applicationContext, this)
  private var isInitialized = false

  private val _isSpeaking = MutableStateFlow(false)
  val isSpeaking: StateFlow<Boolean> = _isSpeaking.asStateFlow()

  private val _speechRate = MutableStateFlow(1.0f)
  val speechRate: StateFlow<Float> = _speechRate.asStateFlow()

  private val _autoSpeakReplies = MutableStateFlow(true)
  val autoSpeakReplies: StateFlow<Boolean> = _autoSpeakReplies.asStateFlow()

  override fun onInit(status: Int) {
    if (status == TextToSpeech.SUCCESS) {
      isInitialized = true
      tts?.language = Locale.US
      tts?.setSpeechRate(_speechRate.value)
      tts?.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
        override fun onStart(utteranceId: String?) {
          _isSpeaking.value = true
        }

        override fun onDone(utteranceId: String?) {
          _isSpeaking.value = false
        }

        @Deprecated("Deprecated in Java")
        override fun onError(utteranceId: String?) {
          _isSpeaking.value = false
        }
      })
    } else {
      Log.e("TtsManager", "TextToSpeech initialization failed with status: $status")
    }
  }

  fun setSpeechRate(rate: Float) {
    val clamped = rate.coerceIn(0.5f, 1.75f)
    _speechRate.value = clamped
    tts?.setSpeechRate(clamped)
  }

  fun setAutoSpeak(enabled: Boolean) {
    _autoSpeakReplies.value = enabled
    if (!enabled) stop()
  }

  fun speak(text: String, languageCode: String = "en") {
    if (!isInitialized || tts == null) return

    val cleanText = text.replace(Regex("[\\[\\]*#_`]"), "")
    val locale = when (languageCode.lowercase()) {
      "en", "english", "inglés" -> Locale.US
      "fr", "french", "francés" -> Locale.FRANCE
      "de", "german", "alemán" -> Locale.GERMANY
      "ja", "japanese", "japonés" -> Locale.JAPAN
      "it", "italian", "italiano" -> Locale.ITALY
      "pt", "portuguese", "portugués" -> Locale("pt", "BR")
      "zh", "chinese", "mandarín", "mandarin" -> Locale.CHINA
      "ko", "korean", "coreano" -> Locale.KOREA
      "ru", "russian", "ruso" -> Locale("ru", "RU")
      "ar", "arabic", "árabe" -> Locale("ar", "SA")
      else -> Locale("es", "ES")
    }

    try {
      tts?.language = locale
      tts?.setSpeechRate(_speechRate.value)
    } catch (e: Exception) {
      Log.w("TtsManager", "Could not set TTS locale: $locale")
    }

    tts?.speak(cleanText, TextToSpeech.QUEUE_FLUSH, null, "utterance_${System.currentTimeMillis()}")
  }

  fun stop() {
    tts?.stop()
    _isSpeaking.value = false
  }

  fun shutdown() {
    tts?.stop()
    tts?.shutdown()
    tts = null
  }
}
