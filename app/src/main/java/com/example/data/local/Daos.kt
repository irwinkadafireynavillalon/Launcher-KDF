package com.example.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface ProtectedPersonDao {
  @Query("SELECT * FROM protected_persons ORDER BY id DESC")
  fun getAll(): Flow<List<ProtectedPerson>>

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insert(person: ProtectedPerson): Long

  @Delete
  suspend fun delete(person: ProtectedPerson)
}

@Dao
interface ThreatRecordDao {
  @Query("SELECT * FROM threat_records ORDER BY timestamp DESC")
  fun getAll(): Flow<List<ThreatRecord>>

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insert(threat: ThreatRecord): Long

  @Delete
  suspend fun delete(threat: ThreatRecord)
}

@Dao
interface CustomRuleDao {
  @Query("SELECT * FROM custom_rules ORDER BY ruleNumber ASC")
  fun getAll(): Flow<List<CustomRule>>

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insert(rule: CustomRule): Long

  @Query("SELECT COUNT(*) FROM custom_rules")
  suspend fun getCount(): Int

  @Delete
  suspend fun delete(rule: CustomRule)
}

@Dao
interface VocabularyDao {
  @Query("SELECT * FROM vocabulary_items WHERE language = :language ORDER BY id DESC")
  fun getByLanguage(language: String): Flow<List<VocabularyItem>>

  @Query("SELECT * FROM vocabulary_items ORDER BY id DESC")
  fun getAll(): Flow<List<VocabularyItem>>

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insert(item: VocabularyItem): Long

  @Update
  suspend fun update(item: VocabularyItem)

  @Delete
  suspend fun delete(item: VocabularyItem)
}

@Dao
interface ConversationDao {
  @Query("SELECT * FROM conversation_messages WHERE language = :language ORDER BY timestamp ASC")
  fun getByLanguage(language: String): Flow<List<ConversationMessage>>

  @Query("SELECT * FROM conversation_messages ORDER BY timestamp ASC")
  fun getAll(): Flow<List<ConversationMessage>>

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insert(message: ConversationMessage): Long

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertAll(messages: List<ConversationMessage>)

  @Query("DELETE FROM conversation_messages WHERE language = :language")
  suspend fun clearByLanguage(language: String)

  @Query("DELETE FROM conversation_messages WHERE content = ''")
  suspend fun deleteEmptyMessages(): Int

  @Query("DELETE FROM conversation_messages")
  suspend fun clearAll()
}

@Dao
interface PracticeSessionDao {
  @Query("SELECT * FROM practice_sessions ORDER BY timestamp DESC")
  fun getAllSessions(): Flow<List<PracticeSessionEntity>>

  @Query("SELECT * FROM practice_sessions WHERE language = :language ORDER BY timestamp DESC")
  fun getSessionsByLanguage(language: String): Flow<List<PracticeSessionEntity>>

  @Query("SELECT * FROM practice_sessions WHERE id = :sessionId LIMIT 1")
  suspend fun getSessionById(sessionId: Long): PracticeSessionEntity?

  @Query("SELECT COUNT(*) FROM practice_sessions")
  suspend fun getSessionCount(): Int

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertSession(session: PracticeSessionEntity): Long

  @Update
  suspend fun updateSession(session: PracticeSessionEntity)

  @Delete
  suspend fun deleteSession(session: PracticeSessionEntity)

  @Query("DELETE FROM practice_sessions WHERE id = :sessionId")
  suspend fun deleteSessionById(sessionId: Long)

  @Query("DELETE FROM practice_sessions")
  suspend fun clearAllSessions()
}

@Dao
interface CheckpointDao {
  @Query("SELECT * FROM practice_checkpoints ORDER BY timestamp DESC")
  fun getAll(): Flow<List<PracticeCheckpoint>>

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insert(checkpoint: PracticeCheckpoint): Long

  @Delete
  suspend fun delete(checkpoint: PracticeCheckpoint)
}

@Dao
interface SystemEvolutionDao {
  @Query("SELECT * FROM system_evolution_logs ORDER BY timestamp DESC")
  fun getAll(): Flow<List<SystemEvolutionLog>>

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insert(log: SystemEvolutionLog): Long

  @Query("DELETE FROM system_evolution_logs")
  suspend fun clearAll()
}

