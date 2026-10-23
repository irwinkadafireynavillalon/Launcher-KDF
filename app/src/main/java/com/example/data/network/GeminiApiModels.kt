package com.example.data.network

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class GeminiRequest(
  val contents: List<GeminiContent>,
  val systemInstruction: GeminiContent? = null,
  val generationConfig: GeminiGenerationConfig? = null
)

@JsonClass(generateAdapter = true)
data class GeminiContent(
  val role: String? = null,
  val parts: List<GeminiPart>
)

@JsonClass(generateAdapter = true)
data class GeminiPart(
  val text: String? = null,
  val inlineData: GeminiInlineData? = null
)

@JsonClass(generateAdapter = true)
data class GeminiInlineData(
  val mimeType: String,
  val data: String
)

@JsonClass(generateAdapter = true)
data class GeminiGenerationConfig(
  val temperature: Float? = null,
  val topP: Float? = null,
  val topK: Int? = null,
  val responseMimeType: String? = null,
  val thinkingConfig: GeminiThinkingConfig? = null,
  val responseModalities: List<String>? = null,
  val speechConfig: GeminiSpeechConfig? = null,
  val imageConfig: GeminiImageConfig? = null
)

@JsonClass(generateAdapter = true)
data class GeminiImageConfig(
  val aspectRatio: String? = "1:1",
  val imageSize: String? = "1K"
)

@JsonClass(generateAdapter = true)
data class GeminiThinkingConfig(
  val thinkingLevel: String // "HIGH" or "LOW"
)

@JsonClass(generateAdapter = true)
data class GeminiSpeechConfig(
  val voiceConfig: GeminiVoiceConfig? = null
)

@JsonClass(generateAdapter = true)
data class GeminiVoiceConfig(
  val prebuiltVoiceConfig: GeminiPrebuiltVoiceConfig? = null
)

@JsonClass(generateAdapter = true)
data class GeminiPrebuiltVoiceConfig(
  val voiceName: String
)

@JsonClass(generateAdapter = true)
data class GeminiResponse(
  val candidates: List<GeminiCandidate>? = null,
  val error: GeminiErrorDetails? = null
)

@JsonClass(generateAdapter = true)
data class GeminiCandidate(
  val content: GeminiContent? = null,
  val finishReason: String? = null
)

@JsonClass(generateAdapter = true)
data class GeminiErrorDetails(
  val code: Int? = null,
  val message: String? = null,
  val status: String? = null
)
