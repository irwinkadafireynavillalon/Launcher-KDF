package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "protected_persons")
data class ProtectedPerson(
  @PrimaryKey(autoGenerate = true) val id: Long = 0,
  val name: String,
  val relationship: String,
  val priority: String, // "Vital", "Alta", "Media"
  val notes: String,
  val addedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "threat_records")
data class ThreatRecord(
  @PrimaryKey(autoGenerate = true) val id: Long = 0,
  val subject: String,
  val category: String, // "Manipulación", "Rastreo", "Hackeo", "Entorno"
  val severity: String, // "Baja", "Moderada", "Alta", "Crítica"
  val details: String,
  val recommendation: String,
  val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "custom_rules")
data class CustomRule(
  @PrimaryKey(autoGenerate = true) val id: Long = 0,
  val ruleNumber: Int,
  val ruleText: String,
  val addedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "vocabulary_items")
data class VocabularyItem(
  @PrimaryKey(autoGenerate = true) val id: Long = 0,
  val word: String,
  val phonetic: String,
  val translation: String,
  val language: String,
  val exampleSentence: String,
  val mastered: Boolean = false,
  val dateAdded: Long = System.currentTimeMillis()
)

@Entity(tableName = "conversation_messages")
data class ConversationMessage(
  @PrimaryKey(autoGenerate = true) val id: Long = 0,
  val role: String, // "user", "model"
  val content: String,
  val language: String,
  val timestamp: Long = System.currentTimeMillis(),
  val alphaTriggered: Boolean = false,
  val grammarNote: String? = null,
  val thinkingProcess: String? = null
)

@Entity(tableName = "practice_checkpoints")
data class PracticeCheckpoint(
  @PrimaryKey(autoGenerate = true) val id: Long = 0,
  val title: String,
  val summary: String,
  val language: String,
  val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "system_evolution_logs")
data class SystemEvolutionLog(
  @PrimaryKey(autoGenerate = true) val id: Long = 0,
  val engineType: String, // "AUTO_HEAL", "AUTO_IMPROVE", "AUTO_UPDATE"
  val versionTag: String,
  val title: String,
  val details: String,
  val status: String, // "REPARADO", "MEJORADO", "ACTUALIZADO"
  val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "practice_sessions")
data class PracticeSessionEntity(
  @PrimaryKey(autoGenerate = true) val id: Long = 0,
  val title: String,
  val language: String,
  val scenario: String,
  val aiEngine: String,
  val messageCount: Int,
  val summaryPreview: String,
  val transcriptJson: String,
  val timestamp: Long = System.currentTimeMillis()
)

