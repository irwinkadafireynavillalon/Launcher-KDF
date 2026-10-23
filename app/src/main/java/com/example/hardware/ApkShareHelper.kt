package com.example.hardware

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.OpenableColumns
import android.widget.Toast
import androidx.core.content.FileProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.util.Locale

data class ApkPackageMetadata(
  val appName: String,
  val packageName: String,
  val versionName: String,
  val sourceApkPath: String,
  val sizeMegabytes: String,
  val isAvailable: Boolean
)

data class AttachedFilePayload(
  val fileName: String,
  val mimeType: String,
  val bytes: ByteArray,
  val sizeFormatted: String
)

object ApkShareHelper {

  fun getApkMetadata(context: Context): ApkPackageMetadata {
    return try {
      val appInfo = context.applicationInfo
      val sourceFile = File(appInfo.sourceDir)
      val exists = sourceFile.exists() && sourceFile.canRead()
      val sizeMb = if (exists) {
        String.format(Locale.US, "%.2f MB", sourceFile.length().toDouble() / (1024.0 * 1024.0))
      } else {
        "N/D"
      }

      val pkgInfo = context.packageManager.getPackageInfo(context.packageName, 0)
      val version = pkgInfo.versionName ?: "1.0"

      ApkPackageMetadata(
        appName = "Hyper Lux",
        packageName = context.packageName,
        versionName = version,
        sourceApkPath = sourceFile.absolutePath,
        sizeMegabytes = sizeMb,
        isAvailable = exists
      )
    } catch (e: Exception) {
      ApkPackageMetadata(
        appName = "Hyper Lux",
        packageName = context.packageName,
        versionName = "1.0",
        sourceApkPath = context.applicationInfo.sourceDir ?: "",
        sizeMegabytes = "N/D",
        isAvailable = false
      )
    }
  }

  /**
   * Reads any user-selected Uri file (PDF, TXT, CSV, Audio, Image, Code, Document, etc.)
   */
  fun readAttachedFileFromUri(context: Context, uri: Uri): AttachedFilePayload? {
    return try {
      var displayName = "archivo_adjunto"
      context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
        val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
        if (nameIndex >= 0 && cursor.moveToFirst()) {
          displayName = cursor.getString(nameIndex) ?: displayName
        }
      }
      val mimeType = context.contentResolver.getType(uri) ?: "application/octet-stream"
      val bytes = context.contentResolver.openInputStream(uri)?.use { it.readBytes() } ?: return null
      val sizeKb = bytes.size / 1024.0
      val sizeFormatted = if (sizeKb >= 1024.0) {
        String.format(Locale.US, "%.2f MB", sizeKb / 1024.0)
      } else {
        String.format(Locale.US, "%.1f KB", sizeKb.coerceAtLeast(0.1))
      }
      AttachedFilePayload(
        fileName = displayName,
        mimeType = mimeType,
        bytes = bytes,
        sizeFormatted = sizeFormatted
      )
    } catch (e: Exception) {
      null
    }
  }

  /**
   * Shares any text content or AI analysis as a downloadable/sendable .txt document file via Android's Share Sheet.
   */
  suspend fun shareTextAsFile(
    context: Context,
    fileNamePrefix: String,
    textContent: String
  ): Result<File> = withContext(Dispatchers.IO) {
    try {
      val shareDir = File(context.cacheDir, "shared_apk")
      if (!shareDir.exists()) shareDir.mkdirs()

      val cleanPrefix = fileNamePrefix.replace(Regex("[^a-zA-Z0-9_-]"), "_").take(32)
      val outFile = File(shareDir, "${cleanPrefix}_HyperLux.txt")
      outFile.writeText(textContent, Charsets.UTF_8)

      val authority = "${context.packageName}.fileprovider"
      val contentUri: Uri = FileProvider.getUriForFile(context, authority, outFile)

      withContext(Dispatchers.Main) {
        val shareIntent = Intent(Intent.ACTION_SEND).apply {
          type = "text/plain"
          putExtra(Intent.EXTRA_STREAM, contentUri)
          putExtra(Intent.EXTRA_SUBJECT, "Documento Hyper Lux: $fileNamePrefix")
          putExtra(Intent.EXTRA_TEXT, textContent)
          addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        val chooser = Intent.createChooser(shareIntent, "Enviar Archivo Hyper Lux").apply {
          addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(chooser)
      }
      Result.success(outFile)
    } catch (e: Exception) {
      withContext(Dispatchers.Main) {
        Toast.makeText(context, "Error al enviar archivo: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
      }
      Result.failure(e)
    }
  }

  /**
   * Shares any AttachedFilePayload (PDF, image, audio, zip, script, document, etc.) to external apps via FileProvider.
   */
  suspend fun shareAttachedPayloadFile(
    context: Context,
    payload: AttachedFilePayload
  ): Result<File> = withContext(Dispatchers.IO) {
    try {
      val shareDir = File(context.cacheDir, "shared_apk")
      if (!shareDir.exists()) shareDir.mkdirs()

      val safeName = payload.fileName.replace(Regex("[^a-zA-Z0-9._-]"), "_").ifBlank { "archivo_hyperlux.bin" }
      val outFile = File(shareDir, safeName)
      FileOutputStream(outFile).use { it.write(payload.bytes) }

      val authority = "${context.packageName}.fileprovider"
      val contentUri: Uri = FileProvider.getUriForFile(context, authority, outFile)

      withContext(Dispatchers.Main) {
        val shareIntent = Intent(Intent.ACTION_SEND).apply {
          type = payload.mimeType.ifBlank { "*/*" }
          putExtra(Intent.EXTRA_STREAM, contentUri)
          putExtra(Intent.EXTRA_SUBJECT, "Archivo Hyper Lux: ${payload.fileName}")
          putExtra(
            Intent.EXTRA_TEXT,
            "Archivo enviado desde Hyper Lux (Antigravity CLI): ${payload.fileName} (${payload.sizeFormatted})"
          )
          addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        val chooser = Intent.createChooser(shareIntent, "Enviar Archivo (${payload.fileName})").apply {
          addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(chooser)
      }
      Result.success(outFile)
    } catch (e: Exception) {
      withContext(Dispatchers.Main) {
        Toast.makeText(context, "No se pudo compartir el archivo: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
      }
      Result.failure(e)
    }
  }

  /**
   * Copies the installed base APK to cache/shared_apk/Hyper_Lux_v1.0.apk
   * and launches the system Chooser to share the .apk file across apps.
   */
  suspend fun shareApkFile(context: Context): Result<File> = withContext(Dispatchers.IO) {
    try {
      val sourceApk = File(context.applicationInfo.sourceDir)
      if (!sourceApk.exists()) {
        return@withContext Result.failure(IllegalStateException("No se encontró el archivo APK base en el dispositivo."))
      }

      val shareDir = File(context.cacheDir, "shared_apk")
      if (!shareDir.exists()) {
        shareDir.mkdirs()
      }

      val destApk = File(shareDir, "Hyper_Lux_v1.0.apk")
      FileInputStream(sourceApk).use { input ->
        FileOutputStream(destApk).use { output ->
          input.copyTo(output)
        }
      }

      val authority = "${context.packageName}.fileprovider"
      val contentUri: Uri = FileProvider.getUriForFile(context, authority, destApk)

      withContext(Dispatchers.Main) {
        val shareIntent = Intent(Intent.ACTION_SEND).apply {
          type = "application/vnd.android.package-archive"
          putExtra(Intent.EXTRA_STREAM, contentUri)
          putExtra(Intent.EXTRA_SUBJECT, "Hyper Lux APK (v1.0)")
          putExtra(
            Intent.EXTRA_TEXT,
            "Instalador APK oficial de Hyper Lux (Edición Oro y Plata)."
          )
          addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }

        val chooser = Intent.createChooser(shareIntent, "Compartir Hyper Lux (.APK)").apply {
          addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(chooser)
      }

      Result.success(destApk)
    } catch (e: Exception) {
      withContext(Dispatchers.Main) {
        Toast.makeText(
          context,
          "Error al preparar APK para compartir: ${e.localizedMessage}",
          Toast.LENGTH_LONG
        ).show()
      }
      Result.failure(e)
    }
  }

  /**
   * Exports the installed base APK into a user-selected Document URI (e.g., Downloads folder).
   */
  suspend fun exportApkToUri(context: Context, targetUri: Uri): Result<Unit> = withContext(Dispatchers.IO) {
    try {
      val sourceApk = File(context.applicationInfo.sourceDir)
      if (!sourceApk.exists()) {
        return@withContext Result.failure(IllegalStateException("Archivo APK base no encontrado."))
      }

      context.contentResolver.openOutputStream(targetUri)?.use { outputStream ->
        FileInputStream(sourceApk).use { inputStream ->
          inputStream.copyTo(outputStream)
        }
      } ?: return@withContext Result.failure(IllegalStateException("No se pudo abrir el destino de escritura."))

      withContext(Dispatchers.Main) {
        Toast.makeText(
          context,
          "Archivo Hyper_Lux.apk guardado exitosamente en tu dispositivo.",
          Toast.LENGTH_LONG
        ).show()
      }
      Result.success(Unit)
    } catch (e: Exception) {
      withContext(Dispatchers.Main) {
        Toast.makeText(
          context,
          "No se pudo guardar el APK: ${e.localizedMessage}",
          Toast.LENGTH_LONG
        ).show()
      }
      Result.failure(e)
    }
  }
}
