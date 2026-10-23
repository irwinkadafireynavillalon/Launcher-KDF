package com.example

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.data.local.AppDatabase
import com.example.data.local.ConversationMessage
import com.example.data.local.PracticeSessionRepository
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

  @Test
  fun `read string from context`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    assertEquals("LAUNCH KDF", appName)
  }

  @Test
  fun `store and retrieve past language practice sessions in Room`() = runBlocking {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val db = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
      .allowMainThreadQueries()
      .build()

    val repo = PracticeSessionRepository(
      practiceSessionDao = db.practiceSessionDao(),
      conversationDao = db.conversationDao()
    )

    val sampleMessages = listOf(
      ConversationMessage(role = "user", content = "Bonjour, comment allez-vous ?", language = "Francés"),
      ConversationMessage(role = "model", content = "Je vais très bien, merci !", language = "Francés")
    )

    val insertedId = repo.savePracticeSession(
      customTitle = "Sesión de Francés Básico",
      language = "Francés",
      scenario = "Conversación Casual & Amistosa",
      aiEngine = "Gemini 1.5 Pro",
      messages = sampleMessages
    )

    assertTrue(insertedId > 0)

    val allSessions = repo.allSessions.first()
    assertEquals(1, allSessions.size)
    assertEquals("Sesión de Francés Básico", allSessions.first().title)
    assertEquals("Francés", allSessions.first().language)
    assertEquals(2, allSessions.first().messageCount)

    repo.restoreSessionToActiveChat(allSessions.first())
    val restoredMessages = db.conversationDao().getAll().first()
    assertEquals(2, restoredMessages.size)
    assertEquals("Bonjour, comment allez-vous ?", restoredMessages[0].content)

    db.close()
  }
}

