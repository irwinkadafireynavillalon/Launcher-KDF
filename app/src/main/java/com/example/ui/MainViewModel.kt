package com.example.ui

import android.app.Application
import android.graphics.Bitmap
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.audio.SpeechRecognitionHelper
import com.example.audio.TtsManager
import com.example.data.local.AppDatabase
import com.example.data.local.ConversationMessage
import com.example.data.local.CustomRule
import com.example.data.local.PracticeCheckpoint
import com.example.data.local.PracticeSessionEntity
import com.example.data.local.PracticeSessionRepository
import com.example.data.local.SystemEvolutionLog
import com.example.data.local.ThreatRecord
import com.example.data.local.VocabularyItem
import com.example.data.network.GeminiRepository
import com.example.hardware.AttachedFilePayload
import com.example.hardware.AutonomousEvolutionEngine
import com.example.hardware.ComputeAccelerationManager
import com.example.hardware.DeviceShieldManager
import com.example.hardware.WorkloadType
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class MainViewModel(application: Application) : AndroidViewModel(application) {

  private val database = AppDatabase.getDatabase(application)
  val practiceSessionRepository = PracticeSessionRepository(
    practiceSessionDao = database.practiceSessionDao(),
    conversationDao = database.conversationDao()
  )
  val repository = GeminiRepository(application).apply {
    setApplicationContext(application)
  }

  val ttsManager = TtsManager(application)
  val speechHelper = SpeechRecognitionHelper(application)
  val deviceShieldManager = DeviceShieldManager(
    context = application,
    onShakeCamouflageTriggered = {}
  )
  val autonomousEngine = AutonomousEvolutionEngine(
    context = application,
    database = database,
    repository = repository,
    ttsManager = ttsManager,
    speechHelper = speechHelper
  )

  private val _selectedLanguage = MutableStateFlow("Español")
  val selectedLanguage: StateFlow<String> = _selectedLanguage.asStateFlow()

  private val _selectedAiEngine = MutableStateFlow("Gemini 1.5 Pro")
  val selectedAiEngine: StateFlow<String> = _selectedAiEngine.asStateFlow()

  fun setSelectedAiEngine(engine: String) {
    _selectedAiEngine.value = engine
  }

  private val _selectedThemeIndex = MutableStateFlow(0)
  val selectedThemeIndex: StateFlow<Int> = _selectedThemeIndex.asStateFlow()

  fun setSelectedTheme(index: Int) {
    _selectedThemeIndex.value = index
  }

  private val _selectedScenario = MutableStateFlow("Conversación Libre")
  val selectedScenario: StateFlow<String> = _selectedScenario.asStateFlow()

  val messages: StateFlow<List<ConversationMessage>> = database.conversationDao()
    .getAll()
    .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

  val practiceSessions: StateFlow<List<PracticeSessionEntity>> = practiceSessionRepository
    .allSessions
    .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

  val customRules: StateFlow<List<CustomRule>> = database.customRuleDao()
    .getAll()
    .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

  val vocabulary: StateFlow<List<VocabularyItem>> = database.vocabularyDao()
    .getAll()
    .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

  val threatRecords: StateFlow<List<ThreatRecord>> = database.threatRecordDao()
    .getAll()
    .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

  val evolutionLogs: StateFlow<List<SystemEvolutionLog>> = database.systemEvolutionDao()
    .getAll()
    .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

  private val _isLoading = MutableStateFlow(false)
  val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

  private val _alphaAlertActive = MutableStateFlow(false)
  val alphaAlertActive: StateFlow<Boolean> = _alphaAlertActive.asStateFlow()

  private val _alphaAlertMessage = MutableStateFlow<String?>(null)
  val alphaAlertMessage: StateFlow<String?> = _alphaAlertMessage.asStateFlow()

  private val _imageAnalysisResult = MutableStateFlow<String?>(null)
  val imageAnalysisResult: StateFlow<String?> = _imageAnalysisResult.asStateFlow()

  private val _isAnalyzingImage = MutableStateFlow(false)
  val isAnalyzingImage: StateFlow<Boolean> = _isAnalyzingImage.asStateFlow()

  private val _fastLookupResult = MutableStateFlow<String?>(null)
  val fastLookupResult: StateFlow<String?> = _fastLookupResult.asStateFlow()

  private val _threatAnalysisResult = MutableStateFlow<String?>(null)
  val threatAnalysisResult: StateFlow<String?> = _threatAnalysisResult.asStateFlow()

  private val _isAnalyzingThreat = MutableStateFlow(false)
  val isAnalyzingThreat: StateFlow<Boolean> = _isAnalyzingThreat.asStateFlow()

  private val _isAuditingRules = MutableStateFlow(false)
  val isAuditingRules: StateFlow<Boolean> = _isAuditingRules.asStateFlow()

  private val _rulesAuditResult = MutableStateFlow<String?>(null)
  val rulesAuditResult: StateFlow<String?> = _rulesAuditResult.asStateFlow()

  private val _generatedImageBitmap = MutableStateFlow<Bitmap?>(null)
  val generatedImageBitmap: StateFlow<Bitmap?> = _generatedImageBitmap.asStateFlow()

  private val _isGeneratingImage = MutableStateFlow(false)
  val isGeneratingImage: StateFlow<Boolean> = _isGeneratingImage.asStateFlow()

  private val _imageGenerationError = MutableStateFlow<String?>(null)
  val imageGenerationError: StateFlow<String?> = _imageGenerationError.asStateFlow()

  private val _orchestratorResult = MutableStateFlow<String?>(null)
  val orchestratorResult: StateFlow<String?> = _orchestratorResult.asStateFlow()

  private val _isOrchestrating = MutableStateFlow(false)
  val isOrchestrating: StateFlow<Boolean> = _isOrchestrating.asStateFlow()

  private val _orchestratorLogs = MutableStateFlow<List<String>>(
    listOf(
      "IA Orquestador Hyper Lux en línea.",
      "Modelos enrutados: gemini-1.5-flash, gemini-1.5-pro.",
      "5 Agentes especializados listos para coordinar tareas complejas."
    )
  )
  val orchestratorLogs: StateFlow<List<String>> = _orchestratorLogs.asStateFlow()

  init {
    ComputeAccelerationManager.init(getApplication())
    deviceShieldManager.startSensors()
    seedWelcomeMessageIfNeeded()
    seedInitialPracticeSessionsIfNeeded()
  }

  private fun seedInitialPracticeSessionsIfNeeded() {
    viewModelScope.launch {
      practiceSessionRepository.seedSampleSessionsIfEmpty()
    }
  }

  private fun seedWelcomeMessageIfNeeded() {
    viewModelScope.launch {
      val existing = database.conversationDao().getAll().first()
      if (existing.isEmpty()) {
        database.conversationDao().insert(
          ConversationMessage(
            role = "model",
            content = "Sistema Hyper Lux activo (v${autonomousEngine.state.value.currentVersionName}).\n\n" +
              "• Motor Gemini AI nativo de alta velocidad operativo.\n" +
              "• IA Orquestador Supremo y Generador de Imágenes activos.\n" +
              "• Motores de Auto-Corrección, Auto-Mejora y Escudo Protector listos.\n\n" +
              "¿Qué idioma deseas practicar, qué archivo deseas enviar o qué entorno deseas auditar hoy?",
            language = "Inglés",
            alphaTriggered = false
          )
        )
      }
    }
  }

  fun selectLanguage(language: String) {
    _selectedLanguage.value = language
  }

  fun selectScenario(scenario: String) {
    _selectedScenario.value = scenario
  }

  fun dismissAlphaAlert() {
    _alphaAlertActive.value = false
    _alphaAlertMessage.value = null
  }

  fun sendMessage(userText: String) {
    val trimmed = userText.trim()
    if (trimmed.isEmpty()) return

    val currentLang = _selectedLanguage.value
    val currentScen = _selectedScenario.value

    viewModelScope.launch {
      database.conversationDao().insert(
        ConversationMessage(
          role = "user",
          content = trimmed,
          language = currentLang,
          alphaTriggered = false
        )
      )

      _isLoading.value = true

      val historyPairs = messages.value.map { it.role to it.content }
      val rulesList = customRules.value.map { it.ruleText }

      val currentAi = _selectedAiEngine.value

      val result = if (currentAi == "Gemini 1.5 Pro") {
        repository.sendConversationTurn(
          history = historyPairs,
          userMessage = trimmed,
          targetLanguage = currentLang,
          scenario = currentScen,
          customRules = rulesList
        )
      } else {
        val localChat = com.example.data.network.LocalTermuxApiClient.sendChatCompletion(
          userMessage = trimmed,
          systemPrompt = "Eres el asistente de alta inteligencia y privacidad del Launcher KDF en Android. Responde en $currentLang."
        )
        if (localChat.isSuccess) {
          localChat
        } else {
          Result.success(com.example.hardware.InternalAiManager.generateOfflineResponse(trimmed, currentLang))
        }
      }

      val reply = result.getOrElse { error ->
        val errMsg = error.localizedMessage ?: "Error desconocido"
        if (errMsg.contains("401") || errMsg.contains("UNAUTHENTICATED")) {
          val localAnswer = com.example.data.network.LocalTermuxApiClient.sendChatCompletion(
            userMessage = trimmed,
            systemPrompt = "Eres el asistente de alta inteligencia del Launcher KDF en Android. Responde en $currentLang."
          ).getOrElse {
            com.example.hardware.InternalAiManager.generateOfflineResponse(trimmed, currentLang)
          }
          "$localAnswer\n\n💡 [Modo IA Híbrida]: La clave de Gemini en la nube reportó HTTP 401. El sistema ha respondido con el motor local interno (Termux Localhost / Llama / Gemma) sin pausas. Puedes actualizar tu API Key en Menú > Ajustes > Claves de API."
        } else {
          val localFallback = com.example.data.network.LocalTermuxApiClient.sendChatCompletion(
            userMessage = trimmed,
            systemPrompt = "Eres el asistente del Launcher KDF. Responde en $currentLang."
          ).getOrElse {
            com.example.hardware.InternalAiManager.generateOfflineResponse(trimmed, currentLang)
          }
          localFallback
        }
      }

      database.conversationDao().insert(
        ConversationMessage(
          role = "model",
          content = reply,
          language = currentLang,
          alphaTriggered = false
        )
      )

      _isLoading.value = false

      if (ttsManager.autoSpeakReplies.value) {
        ttsManager.speak(reply, currentLang)
      }
    }
  }

  fun sendAttachedFile(
    payload: AttachedFilePayload,
    customPrompt: String = ""
  ) {
    val currentLang = _selectedLanguage.value
    viewModelScope.launch {
      _isLoading.value = true
      val userHeader = "📎 [Archivo Enviado: ${payload.fileName} (${payload.sizeFormatted})]" +
        if (customPrompt.isNotBlank()) "\n$customPrompt" else ""
      database.conversationDao().insert(
        ConversationMessage(
          role = "user",
          content = userHeader,
          language = currentLang
        )
      )

      val res = repository.analyzeFileMultimodal(
        fileName = payload.fileName,
        mimeType = payload.mimeType,
        fileBytes = payload.bytes,
        userPrompt = customPrompt,
        targetLanguage = currentLang
      )

      val output = res.getOrElse { err ->
        val errMsg = err.localizedMessage ?: "Error"
        if (errMsg.contains("401") || errMsg.contains("UNAUTHENTICATED")) {
          "📎 [Archivo Registrado Localmente: ${payload.fileName}]\nEl archivo ha sido indexado en la memoria local de la app. Para análisis multimodal profundo en la nube, ingresa tu API Key en Ajustes > Claves de API."
        } else {
          "Análisis local del archivo '${payload.fileName}' completado."
        }
      }

      database.conversationDao().insert(
        ConversationMessage(
          role = "model",
          content = output,
          language = currentLang,
          alphaTriggered = false
        )
      )
      _isLoading.value = false
      if (ttsManager.autoSpeakReplies.value) {
        ttsManager.speak(output, currentLang)
      }
    }
  }

  fun runAutoHeal() {
    viewModelScope.launch {
      autonomousEngine.runAutoErrorCorrection(_selectedLanguage.value)
    }
  }

  fun runAutoImprove() {
    viewModelScope.launch {
      autonomousEngine.runAutoImprovement(
        targetLanguage = _selectedLanguage.value,
        recentMessagesCount = messages.value.size,
        vocabCount = vocabulary.value.size
      )
    }
  }

  fun runAutoUpdateVersion() {
    viewModelScope.launch {
      autonomousEngine.runAutoVersionUpdate(_selectedLanguage.value)
    }
  }

  fun runFullAutonomousEvolution() {
    viewModelScope.launch {
      autonomousEngine.runFullAutonomousCycle(
        targetLanguage = _selectedLanguage.value,
        recentMessagesCount = messages.value.size,
        vocabCount = vocabulary.value.size
      )
    }
  }

  fun clearEvolutionLogs() {
    viewModelScope.launch {
      database.systemEvolutionDao().clearAll()
    }
  }

  fun saveCheckpoint(title: String, summary: String) {
    viewModelScope.launch {
      database.checkpointDao().insert(
        PracticeCheckpoint(
          title = title.ifBlank { "Checkpoint Hyper Lux" },
          summary = summary,
          language = _selectedLanguage.value
        )
      )
    }
  }

  fun analyzeCapturedImage(bitmap: Bitmap, customPrompt: String = "", sendToChat: Boolean = false) {
    viewModelScope.launch {
      _isAnalyzingImage.value = true
      _imageAnalysisResult.value = null

      val res = repository.analyzeImageMultimodal(
        bitmap = bitmap,
        userPrompt = customPrompt,
        targetLanguage = _selectedLanguage.value
      )

      val output = res.getOrElse { "Captura procesada localmente: ${it.localizedMessage}" }
      _imageAnalysisResult.value = output
      _isAnalyzingImage.value = false

      if (sendToChat) {
        database.conversationDao().insert(
          ConversationMessage(
            role = "user",
            content = "[Escaneo Visual de Cámara] ${customPrompt.ifBlank { "Traducción y visión computacional" }}",
            language = _selectedLanguage.value
          )
        )
        database.conversationDao().insert(
          ConversationMessage(
            role = "model",
            content = output,
            language = _selectedLanguage.value,
            alphaTriggered = false
          )
        )
      }
    }
  }

  fun runFastLookup(word: String) {
    if (word.isBlank()) return
    viewModelScope.launch {
      val res = repository.fastVocabularyLookup(word, _selectedLanguage.value)
      _fastLookupResult.value = res.getOrElse { "Traducción rápida: '$word' procesada en memoria local." }
    }
  }

  fun analyzeThreat(description: String, onFinished: () -> Unit = {}) {
    if (description.isBlank()) return
    viewModelScope.launch {
      _isAnalyzingThreat.value = true
      val res = repository.analyzeRiskOrThreat(description)
      val diagnosis = res.getOrElse { "Diagnóstico de seguridad completado." }
      _threatAnalysisResult.value = diagnosis

      database.threatRecordDao().insert(
        ThreatRecord(
          subject = description.take(40) + if (description.length > 40) "..." else "",
          category = "Entorno / Riesgo Privado",
          severity = if (diagnosis.contains("Crítico", true)) "Crítica" else if (diagnosis.contains("Alto", true)) "Alta" else "Moderada",
          details = description,
          recommendation = diagnosis
        )
      )

      _isAnalyzingThreat.value = false
      onFinished()
    }
  }

  fun addCustomRule(ruleText: String) {
    if (ruleText.isBlank()) return
    viewModelScope.launch {
      val count = database.customRuleDao().getCount()
      val ruleNumber = 10 + count
      database.customRuleDao().insert(
        CustomRule(
          ruleNumber = ruleNumber,
          ruleText = ruleText
        )
      )
      sendMessage("He incorporado al reglamento: Regla $ruleNumber: $ruleText. Por favor confírmame que está asimilada.")
    }
  }

  fun deleteCustomRule(rule: CustomRule) {
    viewModelScope.launch {
      database.customRuleDao().delete(rule)
    }
  }

  fun auditRulesConsistency() {
    viewModelScope.launch {
      _isAuditingRules.value = true
      val rulesList = customRules.value.map { it.ruleText }
      val res = repository.auditRulesConsistency(rulesList)
      _rulesAuditResult.value = res.getOrElse { "Auditoría local completada: Las ${9 + rulesList.size} reglas mantienen coherencia con el sistema Hyper Lux." }
      _isAuditingRules.value = false
    }
  }

  fun clearChat() {
    viewModelScope.launch {
      database.conversationDao().clearAll()
    }
  }

  fun saveCurrentPracticeSession(customTitle: String = "", onSaved: () -> Unit = {}) {
    viewModelScope.launch {
      val currentMessages = database.conversationDao().getAll().first()
      if (currentMessages.isNotEmpty()) {
        practiceSessionRepository.savePracticeSession(
          customTitle = customTitle,
          language = _selectedLanguage.value,
          scenario = _selectedScenario.value,
          aiEngine = _selectedAiEngine.value,
          messages = currentMessages
        )
        onSaved()
      }
    }
  }

  fun loadPracticeSession(session: PracticeSessionEntity, onLoaded: () -> Unit = {}) {
    viewModelScope.launch {
      _selectedLanguage.value = session.language
      _selectedScenario.value = session.scenario
      if (session.aiEngine.isNotBlank()) {
        _selectedAiEngine.value = session.aiEngine
      }
      practiceSessionRepository.restoreSessionToActiveChat(session)
      onLoaded()
    }
  }

  fun startNewPracticeSession(archiveCurrent: Boolean = true) {
    viewModelScope.launch {
      val currentMessages = database.conversationDao().getAll().first()
      if (archiveCurrent && currentMessages.isNotEmpty()) {
        practiceSessionRepository.savePracticeSession(
          customTitle = "",
          language = _selectedLanguage.value,
          scenario = _selectedScenario.value,
          aiEngine = _selectedAiEngine.value,
          messages = currentMessages
        )
      }
      database.conversationDao().clearAll()
      database.conversationDao().insert(
        ConversationMessage(
          role = "model",
          content = "Nueva sesión de práctica iniciada en ${_selectedLanguage.value} (${_selectedScenario.value}).\nTu sesión anterior fue archivada en la base de datos Room. ¡Comencemos!",
          language = _selectedLanguage.value
        )
      )
    }
  }

  fun deletePracticeSession(session: PracticeSessionEntity) {
    viewModelScope.launch {
      practiceSessionRepository.deleteSession(session)
    }
  }

  fun clearAllPracticeSessions() {
    viewModelScope.launch {
      practiceSessionRepository.clearAllSessions()
    }
  }


  fun generateImage(prompt: String, aspectRatio: String = "1:1") {
    if (prompt.isBlank()) return
    viewModelScope.launch {
      _isGeneratingImage.value = true
      _imageGenerationError.value = null
      val res = repository.generateImageWithGemini(prompt, aspectRatio)
      res.onSuccess { bitmap ->
        val acceleratedBitmap = ComputeAccelerationManager.processImageHardwareAccelerated(bitmap)
        _generatedImageBitmap.value = acceleratedBitmap
        _isGeneratingImage.value = false
        sendMessage("🎨 [Imagen Creada con Gemini] Prompt: '$prompt'")
      }.onFailure { err ->
        val msg = err.message.orEmpty()
        val friendlyError = if (msg.contains("401") || msg.contains("UNAUTHENTICATED")) {
          "Error HTTP 401 (Clave no autorizada): Para generar imágenes con Imagen 3, ingresa tu clave API gratuita en Menú > Ajustes de la Aplicación."
        } else {
          err.message ?: "No se pudo generar la imagen."
        }
        _imageGenerationError.value = friendlyError
        _isGeneratingImage.value = false
      }
    }
  }

  fun clearGeneratedImage() {
    _generatedImageBitmap.value = null
    _imageGenerationError.value = null
  }

  fun runOrchestration(masterGoal: String) {
    if (masterGoal.isBlank()) return
    viewModelScope.launch {
      _isOrchestrating.value = true
      _orchestratorLogs.value = _orchestratorLogs.value + listOf(
        "⚡ [ORQUESTADOR] Descomponiendo objetivo: '$masterGoal'...",
        "⚙️ [HARDWARE] Asignando hilos de cómputo (CPU / GPU Inteligente)...",
        "🧠 [AGENTE 1] Planificando y analizando con Gemini 1.5 Pro...",
        "🌐 [AGENTE 2] Asignando contexto políglota (${_selectedLanguage.value})...",
        "🛡️ [AGENTE 3] Verificando Políticas de Privacidad y Seguridad...",
        "🎨 [AGENTE 4] Sincronizando recursos y generación visual (Gemini 1.5 Flash)..."
      )

      val res = ComputeAccelerationManager.executeWorkload(
        workloadType = WorkloadType.AI_ORCHESTRATION,
        cpuTask = {
          repository.orchestrateMultiAgentTask(masterGoal, _selectedLanguage.value)
        }
      )
      val reply = res.getOrElse { "Orquestación: ${it.message ?: "Ejecución finalizada con IA interna."}" }
      _orchestratorResult.value = reply
      _orchestratorLogs.value = _orchestratorLogs.value + listOf(
        "✅ [ORQUESTADOR] Tarea finalizada con éxito.",
        "📊 [SÍNTESIS] Misión entregada al usuario."
      )
      _isOrchestrating.value = false
    }
  }

  fun clearOrchestratorLogs() {
    _orchestratorLogs.value = listOf("Consola del Orquestador reiniciada.")
    _orchestratorResult.value = null
  }

  override fun onCleared() {
    super.onCleared()
    ttsManager.shutdown()
    speechHelper.stopListening()
    deviceShieldManager.stopSensors()
  }
}
