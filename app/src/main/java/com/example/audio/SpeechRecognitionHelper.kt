package com.example.audio

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class SpeechRecognitionHelper(private val context: Context) {
  private var speechRecognizer: SpeechRecognizer? = null

  private val _isListening = MutableStateFlow(false)
  val isListening: StateFlow<Boolean> = _isListening.asStateFlow()

  private val _spokenText = MutableStateFlow("")
  val spokenText: StateFlow<String> = _spokenText.asStateFlow()

  private val _rmsDbLevel = MutableStateFlow(0f)
  val rmsDbLevel: StateFlow<Float> = _rmsDbLevel.asStateFlow()

  fun startListening(languageCode: String = "en", onResult: (String) -> Unit) {
    if (!SpeechRecognizer.isRecognitionAvailable(context)) {
      return
    }

    stopListening()

    val localeStr = when (languageCode.lowercase()) {
      "en", "english", "inglés" -> "en-US"
      "fr", "french", "francés" -> "fr-FR"
      "de", "german", "alemán" -> "de-DE"
      "ja", "japanese", "japonés" -> "ja-JP"
      "it", "italian", "italiano" -> "it-IT"
      "pt", "portuguese", "portugués" -> "pt-BR"
      "zh", "chinese", "mandarín", "mandarin" -> "zh-CN"
      "ko", "korean", "coreano" -> "ko-KR"
      "ru", "russian", "ruso" -> "ru-RU"
      "ar", "arabic", "árabe" -> "ar-SA"
      else -> "es-ES"
    }

    val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
      putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
      putExtra(RecognizerIntent.EXTRA_LANGUAGE, localeStr)
      putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
    }

    speechRecognizer = SpeechRecognizer.createSpeechRecognizer(context).apply {
      setRecognitionListener(object : RecognitionListener {
        override fun onReadyForSpeech(params: Bundle?) {
          _isListening.value = true
        }

        override fun onBeginningOfSpeech() {}
        override fun onRmsChanged(rmsdB: Float) {
          _rmsDbLevel.value = rmsdB.coerceAtLeast(0f)
        }

        override fun onBufferReceived(buffer: ByteArray?) {}

        override fun onEndOfSpeech() {
          _isListening.value = false
          _rmsDbLevel.value = 0f
        }

        override fun onError(error: Int) {
          _isListening.value = false
          _rmsDbLevel.value = 0f
        }

        override fun onResults(results: Bundle?) {
          _isListening.value = false
          _rmsDbLevel.value = 0f
          val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
          val result = matches?.firstOrNull() ?: ""
          if (result.isNotBlank()) {
            _spokenText.value = result
            onResult(result)
          }
        }

        override fun onPartialResults(partialResults: Bundle?) {
          val matches = partialResults?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
          val partial = matches?.firstOrNull() ?: ""
          if (partial.isNotBlank()) {
            _spokenText.value = partial
          }
        }

        override fun onEvent(eventType: Int, params: Bundle?) {}
      })
      startListening(intent)
    }
  }

  fun stopListening() {
    try {
      speechRecognizer?.stopListening()
      speechRecognizer?.destroy()
      speechRecognizer = null
    } catch (_: Exception) {}
    _isListening.value = false
    _rmsDbLevel.value = 0f
  }
}
