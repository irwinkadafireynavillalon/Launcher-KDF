package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
  entities = [
    ProtectedPerson::class,
    ThreatRecord::class,
    CustomRule::class,
    VocabularyItem::class,
    ConversationMessage::class,
    PracticeCheckpoint::class,
    SystemEvolutionLog::class,
    PracticeSessionEntity::class
  ],
  version = 3,
  exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
  abstract fun protectedPersonDao(): ProtectedPersonDao
  abstract fun threatRecordDao(): ThreatRecordDao
  abstract fun customRuleDao(): CustomRuleDao
  abstract fun vocabularyDao(): VocabularyDao
  abstract fun conversationDao(): ConversationDao
  abstract fun checkpointDao(): CheckpointDao
  abstract fun systemEvolutionDao(): SystemEvolutionDao
  abstract fun practiceSessionDao(): PracticeSessionDao


  companion object {
    @Volatile
    private var INSTANCE: AppDatabase? = null

    fun getDatabase(context: Context): AppDatabase {
      return INSTANCE ?: synchronized(this) {
        val instance = Room.databaseBuilder(
          context.applicationContext,
          AppDatabase::class.java,
          "linguashield_vault.db"
        ).fallbackToDestructiveMigration(true).build()
        INSTANCE = instance
        instance
      }
    }
  }
}
