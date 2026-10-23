package com.example.data.network

import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit

data class LocalAiStatus(
  val isOnline: Boolean,
  val serviceName: String = "Termux Localhost",
  val activeModel: String = "Gemma / Llama 3.2",
  val serverUrl: String = "http://127.0.0.1:8080",
  val errorDetail: String? = null
)

data class LocalFileOperationResult(
  val success: Boolean,
  val fileName: String,
  val content: String,
  val filePath: String? = null,
  val message: String
)

object LocalTermuxApiClient {
  private const val TAG = "LocalTermuxApiClient"
  private val CANDIDATE_HOSTS = listOf(
    "http://127.0.0.1:8080",
    "http://127.0.0.1:4250",
    "http://127.0.0.1:5000",
    "http://localhost:8080",
    "http://10.0.2.2:8080"
  )

  private val httpClient = OkHttpClient.Builder()
    .connectTimeout(4, TimeUnit.SECONDS)
    .readTimeout(60, TimeUnit.SECONDS)
    .writeTimeout(60, TimeUnit.SECONDS)
    .build()

  private var activeBaseUrl = "http://127.0.0.1:8080"

  suspend fun checkServerStatus(): LocalAiStatus = withContext(Dispatchers.IO) {
    for (host in CANDIDATE_HOSTS) {
      try {
        val healthUrl = if (host.contains("5000")) "$host/api/status" else "$host/health"
        val request = Request.Builder()
          .url(healthUrl)
          .get()
          .build()

        httpClient.newCall(request).execute().use { response ->
          if (response.isSuccessful) {
            val body = response.body?.string().orEmpty()
            val json = try { JSONObject(body) } catch (e: Exception) { JSONObject() }
            activeBaseUrl = host
            return@withContext LocalAiStatus(
              isOnline = true,
              serviceName = json.optString("service", "Termux Local AI"),
              activeModel = json.optString("model", "Llama / Gemma"),
              serverUrl = host
            )
          }
        }
      } catch (e: Exception) {
        Log.d(TAG, "Host $host no respondió: ${e.message}")
      }
    }

    LocalAiStatus(
      isOnline = false,
      serviceName = "Termux Localhost (Inactivo)",
      activeModel = "Modo Offline",
      serverUrl = activeBaseUrl,
      errorDetail = "Servidor Termux detenido. Inícialo con: bash ~/setup_kdf_studio.sh o ~/KDF-LuX/run.sh"
    )
  }

  suspend fun sendChatCompletion(
    userMessage: String,
    systemPrompt: String = "Eres el Asistente Autónomo Hyper Lux de KDF ejecutándote localmente en Android.",
    model: String = "gemma-3-270m"
  ): Result<String> = withContext(Dispatchers.IO) {
    try {
      val messagesArray = JSONArray().apply {
        put(JSONObject().apply {
          put("role", "system")
          put("content", systemPrompt)
        })
        put(JSONObject().apply {
          put("role", "user")
          put("content", userMessage)
        })
      }

      val payload = JSONObject().apply {
        put("model", model)
        put("messages", messagesArray)
        put("prompt", userMessage)
        put("temperature", 0.6)
        put("max_tokens", 800)
      }

      val body = payload.toString().toRequestBody("application/json; charset=utf-8".toMediaType())
      val endpoint = if (activeBaseUrl.contains("4250")) "$activeBaseUrl/v1/chat/completions" else "$activeBaseUrl/v1/chat/completions"
      val request = Request.Builder()
        .url(endpoint)
        .post(body)
        .build()

      httpClient.newCall(request).execute().use { response ->
        if (!response.isSuccessful) {
          return@withContext Result.failure(IllegalStateException("HTTP ${response.code}: ${response.message}"))
        }
        val resBody = response.body?.string().orEmpty()
        val json = JSONObject(resBody)
        val choices = json.optJSONArray("choices")
        if (choices != null && choices.length() > 0) {
          val firstChoice = choices.getJSONObject(0)
          val msgObj = firstChoice.optJSONObject("message")
          val content = msgObj?.optString("content").orEmpty().trim()
          if (content.isNotBlank()) {
            return@withContext Result.success(content)
          }
        }
        val directResponse = json.optString("response", "").trim()
        if (directResponse.isNotBlank()) {
          return@withContext Result.success(directResponse)
        }
        Result.failure(IllegalStateException("Respuesta de IA local vacía"))
      }
    } catch (e: Exception) {
      Log.w(TAG, "Error en sendChatCompletion: ${e.message}")
      Result.failure(e)
    }
  }

  suspend fun createFile(
    filename: String,
    description: String,
    content: String = ""
  ): Result<LocalFileOperationResult> = withContext(Dispatchers.IO) {
    try {
      val payload = JSONObject().apply {
        put("filename", filename)
        put("description", description)
        put("content", content)
      }
      val body = payload.toString().toRequestBody("application/json".toMediaType())
      val request = Request.Builder()
        .url("$activeBaseUrl/api/file/create")
        .post(body)
        .build()

      httpClient.newCall(request).execute().use { response ->
        if (!response.isSuccessful) {
          return@withContext Result.failure(IllegalStateException("Error ${response.code} al crear archivo"))
        }
        val resJson = JSONObject(response.body?.string().orEmpty())
        Result.success(
          LocalFileOperationResult(
            success = resJson.optBoolean("success", true),
            fileName = filename,
            content = content.ifBlank { resJson.optString("content_preview", "") },
            filePath = resJson.optString("path", null),
            message = resJson.optString("message", "Archivo creado en Termux")
          )
        )
      }
    } catch (e: Exception) {
      Result.failure(e)
    }
  }
}
