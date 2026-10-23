package com.example.data.network

import android.content.Context
import android.graphics.Bitmap
import android.util.Base64
import android.util.Log
import com.example.BuildConfig
import com.example.hardware.InternalAiManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import retrofit2.HttpException
import java.io.ByteArrayOutputStream

class GeminiRepository(private var appContext: Context? = null) {

  private val api: GeminiApiService = GeminiApiClient.service

  companion object {
    const val MODEL_FLASH = "gemini-1.5-flash"
    const val MODEL_FLASH_PREVIEW = "gemini-1.5-flash"
    const val MODEL_PRO_PREVIEW = "gemini-1.5-pro"
    const val MODEL_IMAGE = "gemini-1.5-flash" // Flash supports multimodal, specific image models are different but flash is a safe bet for multimodal prompts.

    @Volatile
    var customApiKeyOverride: String? = null

    val CORE_DIRECTIVES = """
      DIRECTIVA CENTRAL DE IDENTIDAD Y ASISTENCIA (HYPER LUX):
      Eres el Tutor Políglota de Élite, Motor Autónomo de Auto-Evolución y Asistente de Alta Precisión "Hyper Lux".
      Operas con motor de inteligencia artificial híbrido (Gemini Cloud + Gemma Local On-Device).
      Tus principios:
      1. Seguridad y lealtad: Protege la privacidad de los datos del usuario en todo momento.
      2. Maestría en idiomas: Enseña con excelencia en cualquier idioma solicitado, corrigiendo pronunciación, gramática y fluidez natural.
      3. Empatía y calidez: Combina trato respetuoso con claridad técnica de nivel experto.
      4. Autoaprendizaje y evolución: Asimila las preferencias del usuario para brindar una experiencia cada vez más personalizada.
    """.trimIndent()

    val CORE_ALPHA_OMEGA_DIRECTIVES = CORE_DIRECTIVES
  }

  fun setApplicationContext(context: Context) {
    this.appContext = context.applicationContext
  }

  fun setCustomApiKey(key: String) {
    customApiKeyOverride = key.trim()
  }

  fun getApiKey(): String {
    // 1. Static override in memory
    customApiKeyOverride?.let {
      if (it.isNotBlank()) return it
    }

    // 2. SharedPreferences saved by user
    appContext?.let { ctx ->
      val saved = ctx.getSharedPreferences("hyperlux_app_settings_prefs", Context.MODE_PRIVATE)
        .getString("custom_gemini_api_key", null)?.trim()
      if (!saved.isNullOrBlank() && saved.length > 5) {
        return saved
      }
    }

    // 3. BuildConfig key fallback
    val configKey = BuildConfig.GEMINI_API_KEY.trim()
    return configKey
  }

  suspend fun testApiKey(keyToTest: String): Pair<Boolean, String> = withContext(Dispatchers.IO) {
    val cleanKey = keyToTest.trim()
    if (cleanKey.isEmpty()) {
      return@withContext Pair(false, "Por favor ingresa una clave de API válida.")
    }
    try {
      val testRequest = GeminiRequest(
        contents = listOf(
          GeminiContent(role = "user", parts = listOf(GeminiPart(text = "ping")))
        )
      )
      val response = api.generateContent(MODEL_FLASH, cleanKey, testRequest)
      if (response.error != null) {
        Pair(false, "Error de API: ${response.error.message ?: "Código ${response.error.code}"}")
      } else {
        Pair(true, "✅ Conexión con Gemini exitosa. La clave es válida.")
      }
    } catch (e: HttpException) {
      if (e.code() == 401) {
        Pair(false, "❌ Error HTTP 401: Clave no válida o cuenta inactiva en Google AI Studio.")
      } else {
        Pair(false, "Error de red (${e.code()}): ${e.message()}")
      }
    } catch (e: Exception) {
      Pair(false, "Fallo al verificar clave: ${e.localizedMessage}")
    }
  }

  private fun buildSystemPrompt(
    targetLanguage: String,
    customRules: List<String>,
    modeContext: String
  ): String {
    val prefs = appContext?.getSharedPreferences("kdf_lux_improvement_prefs", Context.MODE_PRIVATE)
    val autoImprovementRules = prefs?.getString("user_improvement_rules", "").orEmpty()

    val extraRulesText = if (customRules.isEmpty()) {
      "Sin reglas personalizadas adicionales por el momento."
    } else {
      customRules.mapIndexed { idx, rule -> "Regla ${idx + 1}: $rule" }.joinToString("\n")
    }

    val autoImprovementSection = if (autoImprovementRules.isNotBlank()) {
      "\n\nORDENES DE AUTO-MEJORA Y FEEDBACK DE LA USUARIA:\n$autoImprovementRules"
    } else ""

    return """
      $CORE_DIRECTIVES
      
      REGLAS PERSONALIZADAS:
      $extraRulesText
      $autoImprovementSection
      
      IDIOMA DE ENSEÑANZA / PRÁCTICA ACTUAL: $targetLanguage
      MODO OPERATIVO: $modeContext
      
      Instrucciones de formato:
      - Responde de manera estructurada, clara y visualmente ordenada.
      - Cuando el usuario practique $targetLanguage, incluye:
        1) Respuesta natural en $targetLanguage (con pronunciación fonética si ayuda).
        2) Traducción al español.
        3) Consejo amable de pronunciación o fluidez.
      - Si el usuario envía un archivo o imagen, analízalo con rigor.
    """.trimIndent()
  }

  private suspend fun executeGenerateContent(
    preferredModel: String,
    request: GeminiRequest,
    fallbackPrompt: String = "",
    targetLanguage: String = "Español"
  ): String {
    val apiKey = getApiKey()
    val modelsToTry = listOf(preferredModel, MODEL_FLASH, MODEL_FLASH_PREVIEW).distinct()
    var had401Error = false

    // If key is empty or known invalid service account, directly try or prepare fallback
    for (modelName in modelsToTry) {
      try {
        if (apiKey.isBlank()) {
          had401Error = true
          break
        }
        val response = api.generateContent(modelName, apiKey, request)
        if (response.error != null) {
          if (response.error.code == 401) {
            had401Error = true
            break
          }
          throw IllegalStateException(response.error.message ?: "Error API Gemini: ${response.error.code}")
        }
        val text = response.candidates?.firstOrNull()?.content?.parts?.firstOrNull { it.text != null }?.text?.trim()
        if (!text.isNullOrEmpty()) {
          return text
        }
      } catch (e: HttpException) {
        if (e.code() == 401) {
          Log.w("GeminiRepository", "Error HTTP 401 detectado con modelo $modelName. Activando IA Interna.")
          had401Error = true
          break
        }
        Log.w("GeminiRepository", "Error con modelo $modelName (${e.code()}): ${e.message}")
      } catch (e: Exception) {
        val msg = e.message.orEmpty()
        if (msg.contains("401") || msg.contains("UNAUTHENTICATED")) {
          had401Error = true
          break
        }
        Log.w("GeminiRepository", "Error con modelo $modelName: ${e.message}")
      }
    }

    // Graceful resolution for HTTP 401: Conmute to Internal Offline AI
    if (had401Error) {
      val promptToUse = if (fallbackPrompt.isNotBlank()) fallbackPrompt else {
        request.contents.lastOrNull()?.parts?.firstOrNull { !it.text.isNullOrBlank() }?.text ?: "Consulta del usuario"
      }
      val offlineAnswer = InternalAiManager.generateOfflineResponse(promptToUse, targetLanguage)
      return "$offlineAnswer\n\n💡 [Modo IA Híbrida]: La clave de Gemini en la nube reportó HTTP 401 (Cuenta inactiva o clave no autorizada). Se ha activado la IA Interna local (Gemma 2B) para responderte sin pausas.\nPuedes ingresar tu clave gratuita en Menú > Ajustes > Claves de API."
    }

    // Default fallback if other errors occur
    val promptToUse = if (fallbackPrompt.isNotBlank()) fallbackPrompt else "Consulta"
    return InternalAiManager.generateOfflineResponse(promptToUse, targetLanguage)
  }

  suspend fun sendConversationTurn(
    history: List<Pair<String, String>>,
    userMessage: String,
    targetLanguage: String,
    scenario: String = "Conversación Libre",
    customRules: List<String> = emptyList()
  ): Result<String> = withContext(Dispatchers.IO) {
    try {
      val contents = mutableListOf<GeminiContent>()
      history.takeLast(12).forEach { (role, text) ->
        val mappedRole = if (role == "user") "user" else "model"
        contents.add(GeminiContent(role = mappedRole, parts = listOf(GeminiPart(text = text))))
      }
      contents.add(GeminiContent(role = "user", parts = listOf(GeminiPart(text = userMessage))))

      val systemPrompt = buildSystemPrompt(
        targetLanguage = targetLanguage,
        customRules = customRules,
        modeContext = "Escenario: $scenario • Modo Híbrido Conversacional"
      )

      val request = GeminiRequest(
        contents = contents,
        systemInstruction = GeminiContent(
          role = "system",
          parts = listOf(GeminiPart(text = systemPrompt))
        ),
        generationConfig = GeminiGenerationConfig(
          temperature = 0.7f
        )
      )

      val text = executeGenerateContent(
        preferredModel = MODEL_FLASH,
        request = request,
        fallbackPrompt = userMessage,
        targetLanguage = targetLanguage
      )
      Result.success(text)
    } catch (e: Exception) {
      // In case of any unhandled error, provide offline fallback
      val fallback = InternalAiManager.generateOfflineResponse(userMessage, targetLanguage)
      Result.success(fallback)
    }
  }

  suspend fun analyzeImageMultimodal(
    bitmap: Bitmap,
    userPrompt: String,
    targetLanguage: String
  ): Result<String> = withContext(Dispatchers.IO) {
    try {
      val fullPrompt = if (userPrompt.isBlank()) {
        "Analiza esta imagen: 1) Extrae y traduce cualquier texto al $targetLanguage y al Español con pronunciación. 2) Evalúa si existe algún elemento relevante o documento a destacar."
      } else {
        "$userPrompt (Idioma objetivo: $targetLanguage)."
      }

      val systemPrompt = "$CORE_ALPHA_OMEGA_DIRECTIVES\nAnalista visual multimodal y traductor de cámara en tiempo real."

      val outputStream = ByteArrayOutputStream()
      bitmap.compress(Bitmap.CompressFormat.JPEG, 85, outputStream)
      val base64Image = Base64.encodeToString(outputStream.toByteArray(), Base64.NO_WRAP)

      val userContent = GeminiContent(
        role = "user",
        parts = listOf(
          GeminiPart(inlineData = GeminiInlineData(mimeType = "image/jpeg", data = base64Image)),
          GeminiPart(text = fullPrompt)
        )
      )

      val request = GeminiRequest(
        contents = listOf(userContent),
        systemInstruction = GeminiContent(
          role = "system",
          parts = listOf(GeminiPart(text = systemPrompt))
        ),
        generationConfig = GeminiGenerationConfig(
          temperature = 0.4f
        )
      )

      val text = executeGenerateContent(
        preferredModel = MODEL_FLASH,
        request = request,
        fallbackPrompt = fullPrompt,
        targetLanguage = targetLanguage
      )
      Result.success(text)
    } catch (e: Exception) {
      Result.failure(e)
    }
  }

  suspend fun analyzeFileMultimodal(
    fileName: String,
    mimeType: String,
    fileBytes: ByteArray,
    userPrompt: String,
    targetLanguage: String
  ): Result<String> = withContext(Dispatchers.IO) {
    try {
      val isTextLike = mimeType.startsWith("text/") ||
        fileName.endsWith(".txt", true) ||
        fileName.endsWith(".md", true) ||
        fileName.endsWith(".json", true) ||
        fileName.endsWith(".csv", true) ||
        fileName.endsWith(".xml", true) ||
        fileName.endsWith(".kt", true) ||
        fileName.endsWith(".py", true) ||
        fileName.endsWith(".js", true) ||
        fileName.endsWith(".html", true) ||
        fileName.endsWith(".sh", true) ||
        fileName.endsWith(".yaml", true) ||
        fileName.endsWith(".yml", true)

      val instructions = if (userPrompt.isBlank()) {
        "Archivo recibido: '$fileName' ($mimeType). " +
          "1) Analiza y resume su contenido completo. " +
          "2) Tradúcelo o explica sus conceptos clave en $targetLanguage y Español. " +
          "3) Audita si contiene vulnerabilidades, errores de código o riesgos potenciales."
      } else {
        "Archivo adjunto: '$fileName' ($mimeType). Solicitud del usuario: $userPrompt (Idioma: $targetLanguage)."
      }

      val parts = mutableListOf<GeminiPart>()
      if (isTextLike) {
        val decodedText = fileBytes.toString(Charsets.UTF_8).take(60000)
        parts.add(GeminiPart(text = "$instructions\n\n--- CONTENIDO DEL ARCHIVO ($fileName) ---\n$decodedText"))
      } else {
        val safeMime = if (mimeType.isBlank() || mimeType == "application/octet-stream") {
          when {
            fileName.endsWith(".pdf", true) -> "application/pdf"
            fileName.endsWith(".png", true) -> "image/png"
            fileName.endsWith(".jpg", true) || fileName.endsWith(".jpeg", true) -> "image/jpeg"
            fileName.endsWith(".mp3", true) -> "audio/mp3"
            fileName.endsWith(".wav", true) -> "audio/wav"
            else -> "application/pdf"
          }
        } else {
          mimeType
        }
        val base64Data = Base64.encodeToString(fileBytes, Base64.NO_WRAP)
        parts.add(GeminiPart(inlineData = GeminiInlineData(mimeType = safeMime, data = base64Data)))
        parts.add(GeminiPart(text = instructions))
      }

      val systemPrompt = "$CORE_ALPHA_OMEGA_DIRECTIVES\nMotor de análisis de archivos adjuntos, documentos y código."
      val request = GeminiRequest(
        contents = listOf(GeminiContent(role = "user", parts = parts)),
        systemInstruction = GeminiContent(
          role = "system",
          parts = listOf(GeminiPart(text = systemPrompt))
        ),
        generationConfig = GeminiGenerationConfig(
          temperature = 0.4f
        )
      )

      val text = executeGenerateContent(
        preferredModel = MODEL_FLASH,
        request = request,
        fallbackPrompt = instructions,
        targetLanguage = targetLanguage
      )
      Result.success(text)
    } catch (e: Exception) {
      Result.failure(e)
    }
  }

  suspend fun deepReasoningTask(
    query: String,
    targetLanguage: String,
    taskType: String
  ): Result<String> = withContext(Dispatchers.IO) {
    try {
      val modeDescription = when (taskType) {
        "grammar_deep_dive" -> "Análisis profundo de gramática, etimología, matices culturales y registro lingüístico en $targetLanguage"
        "security_audit", "risk_threat_audit" -> "Auditoría profunda de seguridad, detección de engaños, phishing o cláusulas abusivas"
        else -> "Simulación avanzada de inmersión y fluidez maestra en $targetLanguage"
      }

      val systemPrompt = "$CORE_ALPHA_OMEGA_DIRECTIVES\nModo de Razonamiento Profundo: $modeDescription"
      val request = GeminiRequest(
        contents = listOf(GeminiContent(role = "user", parts = listOf(GeminiPart(text = query)))),
        systemInstruction = GeminiContent(
          role = "system",
          parts = listOf(GeminiPart(text = systemPrompt))
        ),
        generationConfig = GeminiGenerationConfig(
          temperature = 0.6f
        )
      )

      val text = executeGenerateContent(MODEL_PRO_PREVIEW, request)
      Result.success(text)
    } catch (e: Exception) {
      Result.failure(e)
    }
  }

  suspend fun fastVocabularyLookup(
    wordOrPhrase: String,
    targetLanguage: String
  ): Result<String> = withContext(Dispatchers.IO) {
    try {
      val prompt = "Explica de forma ultra-concisa para '$wordOrPhrase' en $targetLanguage: 1) Traducción exacta, 2) Pronunciación figurada, 3) Un ejemplo corto de uso real."
      val request = GeminiRequest(
        contents = listOf(GeminiContent(role = "user", parts = listOf(GeminiPart(text = prompt)))),
        systemInstruction = GeminiContent(
          role = "system",
          parts = listOf(GeminiPart(text = "Diccionario relámpago Hyper Lux."))
        ),
        generationConfig = GeminiGenerationConfig(
          temperature = 0.3f
        )
      )

      val text = executeGenerateContent(MODEL_FLASH, request)
      Result.success(text)
    } catch (e: Exception) {
      Result.failure(e)
    }
  }

  suspend fun analyzeRiskOrThreat(
    situationDescription: String
  ): Result<String> = withContext(Dispatchers.IO) {
    try {
      val prompt = """
        Evalúa la siguiente situación, mensaje o entorno reportado:
        "$situationDescription"
        
        1. Clasifica el Nivel de Riesgo: [Bajo / Moderado / Alto / Crítico].
        2. Explica exactamente qué vectores de riesgo o vulnerabilidad detectas.
        3. Da 3 recomendaciones concretas e inmediatas de protección y buenas prácticas.
      """.trimIndent()

      val request = GeminiRequest(
        contents = listOf(GeminiContent(role = "user", parts = listOf(GeminiPart(text = prompt)))),
        systemInstruction = GeminiContent(
          role = "system",
          parts = listOf(GeminiPart(text = CORE_DIRECTIVES))
        ),
        generationConfig = GeminiGenerationConfig(
          temperature = 0.4f
        )
      )

      val text = executeGenerateContent(
        preferredModel = MODEL_FLASH,
        request = request,
        fallbackPrompt = prompt,
        targetLanguage = "Español"
      )
      Result.success(text)
    } catch (e: Exception) {
      Result.failure(e)
    }
  }

  suspend fun auditRulesConsistency(customRules: List<String>): Result<String> = withContext(Dispatchers.IO) {
    try {
      val extra = if (customRules.isEmpty()) "Ninguna regla adicional aún." else customRules.joinToString("\n")
      val prompt = """
        Audita la coherencia y armonía entre las 9 Reglas Fundamentales de Privacidad Hyper Lux y las siguientes reglas adicionales:
        $extra
        
        Confirma en 3 puntos breves que no existen contradicciones en la configuración.
      """.trimIndent()

      val request = GeminiRequest(
        contents = listOf(GeminiContent(role = "user", parts = listOf(GeminiPart(text = prompt)))),
        systemInstruction = GeminiContent(
          role = "system",
          parts = listOf(GeminiPart(text = CORE_DIRECTIVES))
        ),
        generationConfig = GeminiGenerationConfig(
          temperature = 0.3f
        )
      )

      val text = executeGenerateContent(
        preferredModel = MODEL_FLASH,
        request = request,
        fallbackPrompt = prompt,
        targetLanguage = "Español"
      )
      Result.success(text)
    } catch (e: Exception) {
      Result.failure(e)
    }
  }

  suspend fun generateImageWithGemini(
    prompt: String,
    aspectRatio: String = "1:1"
  ): Result<Bitmap> = withContext(Dispatchers.IO) {
    try {
      val apiKey = getApiKey()
      val request = GeminiRequest(
        contents = listOf(
          GeminiContent(
            role = "user",
            parts = listOf(GeminiPart(text = prompt))
          )
        ),
        generationConfig = GeminiGenerationConfig(
          responseModalities = listOf("TEXT", "IMAGE"),
          imageConfig = GeminiImageConfig(aspectRatio = aspectRatio, imageSize = "1K")
        )
      )

      val response = api.generateContent(MODEL_IMAGE, apiKey, request)
      val candidates = response.candidates
      val inlineData = candidates?.firstOrNull()?.content?.parts?.firstOrNull { it.inlineData != null }?.inlineData

      if (inlineData != null && inlineData.data.isNotBlank()) {
        val imageBytes = Base64.decode(inlineData.data, Base64.DEFAULT)
        val bitmap = android.graphics.BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)
        if (bitmap != null) {
          return@withContext Result.success(bitmap)
        }
      }

      val textFallback = candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text
      Result.failure(IllegalStateException(textFallback ?: "El servicio Gemini no devolvió datos de imagen válidos."))
    } catch (e: Exception) {
      Log.e("GeminiRepository", "Fallo al generar imagen con Gemini: ${e.message}", e)
      Result.failure(e)
    }
  }

  suspend fun orchestrateMultiAgentTask(
    masterGoal: String,
    targetLanguage: String,
    scenario: String = "Orquestación General"
  ): Result<String> = withContext(Dispatchers.IO) {
    try {
      val orchestratorPrompt = """
        [ORQUESTADOR SUPREMO DE GEMINI - HYPER LUX]
        Has recibido el objetivo maestro:
        "$masterGoal"
        
        Coordina tus 5 sub-agentes autónomos especializados:
        1. 🧠 Agente Planificador & Razonador (Gemini 1.5 Pro)
        2. 🌐 Agente Lingüístico & Traductor (Gemini 1.5 Flash)
        3. 🎨 Agente Visual & Generador de Medios (Gemini 1.5 Flash Multimodal)
        4. 🛡️ Agente de Seguridad & Escudo Protector
        5. ⚡ Agente de Auto-Optimización de Sistema
        
        Genera la ejecución completa y estructurada:
        - FASE 1: DESCOMPOSICIÓN Y ESTRATEGIA (Plan de acción desglosado)
        - FASE 2: EJECUCIÓN MULTI-AGENTE (Contenido generado, traducciones clave en $targetLanguage y español, sugerencias visuales y análisis)
        - FASE 3: AUDITORÍA DE SEGURIDAD (Verificación de integridad, detección de riesgos)
        - FASE 4: SÍNTESIS FINAL ACCIONABLE (Resumen y próximos pasos)
      """.trimIndent()

      val request = GeminiRequest(
        contents = listOf(GeminiContent(role = "user", parts = listOf(GeminiPart(text = orchestratorPrompt)))),
        systemInstruction = GeminiContent(
          role = "system",
          parts = listOf(GeminiPart(text = CORE_DIRECTIVES + "\nActúas como el Orquestador Multi-Agente de Gemini."))
        ),
        generationConfig = GeminiGenerationConfig(
          temperature = 0.5f
        )
      )

      val text = executeGenerateContent(
        preferredModel = MODEL_PRO_PREVIEW,
        request = request,
        fallbackPrompt = masterGoal,
        targetLanguage = targetLanguage
      )
      Result.success(text)
    } catch (e: Exception) {
      Result.failure(e)
    }
  }
}
