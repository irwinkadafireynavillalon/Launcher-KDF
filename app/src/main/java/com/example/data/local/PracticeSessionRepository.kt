package com.example.data.local

import kotlinx.coroutines.flow.Flow
import org.json.JSONArray
import org.json.JSONObject

class PracticeSessionRepository(
  private val practiceSessionDao: PracticeSessionDao,
  private val conversationDao: ConversationDao
) {

  val allSessions: Flow<List<PracticeSessionEntity>> = practiceSessionDao.getAllSessions()

  fun getSessionsByLanguage(language: String): Flow<List<PracticeSessionEntity>> {
    return practiceSessionDao.getSessionsByLanguage(language)
  }

  suspend fun getSessionById(sessionId: Long): PracticeSessionEntity? {
    return practiceSessionDao.getSessionById(sessionId)
  }

  suspend fun savePracticeSession(
    customTitle: String = "",
    language: String,
    scenario: String,
    aiEngine: String,
    messages: List<ConversationMessage>
  ): Long {
    if (messages.isEmpty()) return -1L

    val firstUserMsg = messages.firstOrNull { it.role == "user" }?.content
    val resolvedTitle = when {
      customTitle.isNotBlank() -> customTitle.trim()
      !firstUserMsg.isNullOrBlank() -> {
        val clean = firstUserMsg.replace("\n", " ").trim()
        if (clean.length > 42) "${clean.take(42)}..." else clean
      }
      else -> "Práctica de $language • $scenario"
    }

    val lastMsg = messages.lastOrNull()?.content?.replace("\n", " ")?.trim().orEmpty()
    val summaryPreview = if (lastMsg.length > 90) "${lastMsg.take(90)}..." else lastMsg

    val transcriptJson = serializeMessagesToJson(messages)

    val entity = PracticeSessionEntity(
      title = resolvedTitle,
      language = language,
      scenario = scenario,
      aiEngine = aiEngine,
      messageCount = messages.size,
      summaryPreview = summaryPreview.ifBlank { "Sesión de práctica en $language ($scenario)" },
      transcriptJson = transcriptJson,
      timestamp = System.currentTimeMillis()
    )

    return practiceSessionDao.insertSession(entity)
  }

  suspend fun restoreSessionToActiveChat(session: PracticeSessionEntity) {
    val restoredMessages = parseTranscript(session.transcriptJson, session.language)
    conversationDao.clearAll()
    if (restoredMessages.isNotEmpty()) {
      conversationDao.insertAll(restoredMessages)
    } else {
      conversationDao.insert(
        ConversationMessage(
          role = "model",
          content = "Sesión restaurada: ${session.title} (${session.language} • ${session.scenario}).\n${session.summaryPreview}",
          language = session.language
        )
      )
    }
  }

  suspend fun deleteSession(session: PracticeSessionEntity) {
    practiceSessionDao.deleteSession(session)
  }

  suspend fun deleteSessionById(sessionId: Long) {
    practiceSessionDao.deleteSessionById(sessionId)
  }

  suspend fun clearAllSessions() {
    practiceSessionDao.clearAllSessions()
  }

  suspend fun seedSampleSessionsIfEmpty() {
    if (practiceSessionDao.getSessionCount() > 0) return

    val now = System.currentTimeMillis()

    val englishMessages = listOf(
      ConversationMessage(
        role = "user",
        content = "Hello! I have an executive negotiation tomorrow in London. Can we practice key phrases?",
        language = "Inglés",
        timestamp = now - 7_200_000L
      ),
      ConversationMessage(
        role = "model",
        content = "Certainly! Let's practice high-impact executive negotiation phrases:\n1. \"We see strong alignment on the core objectives, provided we can structure a mutually beneficial timeline.\"\n2. \"Let's revisit the deliverables before finalizing the valuation.\"\nHow would you respond if they ask for an immediate 15% discount?",
        language = "Inglés",
        timestamp = now - 7_150_000L
      ),
      ConversationMessage(
        role = "user",
        content = "We can offer a 10% adjustment if we sign a two-year commitment today.",
        language = "Inglés",
        timestamp = now - 7_100_000L
      ),
      ConversationMessage(
        role = "model",
        content = "Excellent phrasing! Very natural and assertive. You can also say: \"We're prepared to extend a 10% preferential rate contingent upon a 24-month commitment.\"",
        language = "Inglés",
        timestamp = now - 7_050_000L
      )
    )

    val frenchMessages = listOf(
      ConversationMessage(
        role = "user",
        content = "Bonjour, je voudrais réserver une table pour deux personnes ce soir.",
        language = "Francés",
        timestamp = now - 3_600_000L
      ),
      ConversationMessage(
        role = "model",
        content = "Bonjour ! Avec plaisir. À quelle heure souhaitez-vous dîner, et préférez-vous la terrasse ou la salle principale ?",
        language = "Francés",
        timestamp = now - 3_550_000L
      ),
      ConversationMessage(
        role = "user",
        content = "À vingt heures, en terrasse s'il vous plaît.",
        language = "Francés",
        timestamp = now - 3_500_000L
      ),
      ConversationMessage(
        role = "model",
        content = "Parfait ! Votre prononciation et votre syntaxe sont impeccables. C'est noté pour 20h00 en terrasse.",
        language = "Francés",
        timestamp = now - 3_450_000L
      )
    )

    practiceSessionDao.insertSession(
      PracticeSessionEntity(
        title = "Negociación Ejecutiva en Londres",
        language = "Inglés",
        scenario = "Negociación Ejecutiva & Contratos",
        aiEngine = "Gemini 1.5 Pro",
        messageCount = englishMessages.size,
        summaryPreview = "Excellent phrasing! Very natural and assertive. You can also say...",
        transcriptJson = serializeMessagesToJson(englishMessages),
        timestamp = now - 7_050_000L
      )
    )

    practiceSessionDao.insertSession(
      PracticeSessionEntity(
        title = "Reserva en Restaurante de París",
        language = "Francés",
        scenario = "Ordenando en Restaurante / Café",
        aiEngine = "Gemini 1.5 Pro",
        messageCount = frenchMessages.size,
        summaryPreview = "Parfait ! Votre prononciation et votre syntaxe sont impeccables...",
        transcriptJson = serializeMessagesToJson(frenchMessages),
        timestamp = now - 3_450_000L
      )
    )
  }

  fun serializeMessagesToJson(messages: List<ConversationMessage>): String {
    val array = JSONArray()
    for (msg in messages) {
      val obj = JSONObject()
      obj.put("role", msg.role)
      obj.put("content", msg.content)
      obj.put("language", msg.language)
      obj.put("timestamp", msg.timestamp)
      obj.put("alphaTriggered", msg.alphaTriggered)
      if (msg.grammarNote != null) obj.put("grammarNote", msg.grammarNote)
      if (msg.thinkingProcess != null) obj.put("thinkingProcess", msg.thinkingProcess)
      array.put(obj)
    }
    return array.toString()
  }

  fun parseTranscript(transcriptJson: String, fallbackLanguage: String): List<ConversationMessage> {
    if (transcriptJson.isBlank()) return emptyList()
    return try {
      val array = JSONArray(transcriptJson)
      val list = mutableListOf<ConversationMessage>()
      for (i in 0 until array.length()) {
        val obj = array.getJSONObject(i)
        list.add(
          ConversationMessage(
            role = obj.optString("role", "model"),
            content = obj.optString("content", ""),
            language = obj.optString("language", fallbackLanguage),
            timestamp = obj.optLong("timestamp", System.currentTimeMillis()),
            alphaTriggered = obj.optBoolean("alphaTriggered", false),
            grammarNote = if (obj.has("grammarNote")) obj.optString("grammarNote") else null,
            thinkingProcess = if (obj.has("thinkingProcess")) obj.optString("thinkingProcess") else null
          )
        )
      }
      list
    } catch (_: Exception) {
      emptyList()
    }
  }
}
