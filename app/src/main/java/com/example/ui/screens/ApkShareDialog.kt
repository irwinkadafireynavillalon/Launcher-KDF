package com.example.ui.screens
import androidx.compose.ui.geometry.*
import androidx.compose.ui.draw.*
import androidx.compose.ui.graphics.*
import com.example.ui.theme.*

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Android
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.hardware.ApkShareHelper
import kotlinx.coroutines.launch

@Composable
fun ApkShareDialog(
  isOpen: Boolean,
  onDismiss: () -> Unit
) {
  if (!isOpen) return

  val context = LocalContext.current
  val scope = rememberCoroutineScope()
  val metadata = remember { ApkShareHelper.getApkMetadata(context) }
  var isProcessing by remember { mutableStateOf(false) }
  var statusMessage by remember { mutableStateOf<String?>(null) }

  val saveApkDocumentLauncher = rememberLauncherForActivityResult(
    ActivityResultContracts.CreateDocument("application/vnd.android.package-archive")
  ) { uri: Uri? ->
    if (uri != null) {
      scope.launch {
        isProcessing = true
        val res = ApkShareHelper.exportApkToUri(context, uri)
        isProcessing = false
        statusMessage = if (res.isSuccess) {
          "APK de Hyper Lux guardado correctamente en tu dispositivo."
        } else {
          "No se pudo guardar el APK: ${res.exceptionOrNull()?.localizedMessage}"
        }
      }
    }
  }

  Dialog(onDismissRequest = onDismiss) {
    Surface(
      shape = RoundedCornerShape(16.dp),
      color = SurfacePearl,
      border = androidx.compose.foundation.BorderStroke(1.dp, MetalSecondary.copy(alpha = 0.6f)),
      modifier = Modifier
        .fillMaxWidth()
        .padding(vertical = 12.dp)
    ) {
      Column(
        modifier = Modifier
          .padding(16.dp)
          .verticalScroll(rememberScrollState())
      ) {
        // Header
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
              imageVector = Icons.Default.Android,
              contentDescription = "APK",
              tint = MetalSecondary,
              modifier = Modifier.size(22.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Column {
              Text(
                text = "Compartir Hyper Lux (.APK)",
                color = TextMain,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold
              )
              Text(
                text = "Edición Oro y Plata • Instalador Directo",
                color = MetalSecondary,
                fontSize = 10.sp,
                fontFamily = FontFamily.Monospace
              )
            }
          }
          IconButton(onClick = onDismiss, modifier = Modifier.size(28.dp)) {
            Icon(Icons.Default.Close, contentDescription = "Cerrar", tint = TextSub, modifier = Modifier.size(18.dp))
          }
        }

        Spacer(modifier = Modifier.height(10.dp))

        Text(
          text = "Envía el paquete instalable (.apk) completo de Hyper Lux por Bluetooth, Quick Share, WhatsApp, Telegram, Drive o guárdalo en tus archivos locales.",
          color = TextSub,
          fontSize = 11.sp,
          lineHeight = 15.sp
        )

        Spacer(modifier = Modifier.height(10.dp))

        // APK Metadata Card
        Card(
          modifier = Modifier.fillMaxWidth(),
          colors = CardDefaults.cardColors(containerColor = SurfacePearl),
          shape = RoundedCornerShape(10.dp),
          border = androidx.compose.foundation.BorderStroke(1.dp, MetalPrimary)
        ) {
          Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically
            ) {
              Text(
                text = "Hyper_Lux_v${metadata.versionName}.apk",
                color = MetalSecondary,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace
              )
              Box(
                modifier = Modifier
                  .background(MetalSecondary.copy(alpha = 0.2f), RoundedCornerShape(4.dp))
                  .padding(horizontal = 6.dp, vertical = 2.dp)
              ) {
                Text(
                  text = metadata.sizeMegabytes,
                  color = MetalSecondary,
                  fontSize = 10.sp,
                  fontWeight = FontWeight.Bold,
                  fontFamily = FontFamily.Monospace
                )
              }
            }
            Text(
              text = "Paquete: ${metadata.packageName}",
              color = TextSub,
              fontSize = 10.sp,
              fontFamily = FontFamily.Monospace
            )
            Row(verticalAlignment = Alignment.CenterVertically) {
              Icon(Icons.Default.VerifiedUser, contentDescription = null, tint = MetalPrimary, modifier = Modifier.size(12.dp))
              Spacer(modifier = Modifier.width(4.dp))
              Text(
                text = "Paquete APK Oficial Listo para Compartir",
                color = MetalPrimary,
                fontSize = 10.sp,
                fontFamily = FontFamily.Monospace
              )
            }
          }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Share via Android Chooser Button
        Button(
          onClick = {
            scope.launch {
              isProcessing = true
              statusMessage = null
              val result = ApkShareHelper.shareApkFile(context)
              isProcessing = false
              if (result.isSuccess) {
                statusMessage = "Selector de envío APK abierto correctamente."
              }
            }
          },
          enabled = !isProcessing,
          contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
          modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 36.dp, max = 40.dp)
            .testTag("confirm_share_apk_button"),
          shape = RoundedCornerShape(8.dp),
          colors = ButtonDefaults.buttonColors(
            containerColor = MetalSecondary,
            contentColor = Color.Black
          )
        ) {
          if (isProcessing) {
            CircularProgressIndicator(color = Color.Black, modifier = Modifier.size(14.dp), strokeWidth = 2.dp)
            Spacer(modifier = Modifier.width(6.dp))
            Text("Preparando paquete APK...", fontSize = 11.sp, fontWeight = FontWeight.Bold)
          } else {
            Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(15.dp))
            Spacer(modifier = Modifier.width(6.dp))
            Text("Compartir Archivo .APK Ahora", fontSize = 11.sp, fontWeight = FontWeight.Bold)
          }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Save APK to local storage / Downloads
        OutlinedButton(
          onClick = {
            saveApkDocumentLauncher.launch("Hyper_Lux_v${metadata.versionName}.apk")
          },
          enabled = !isProcessing,
          contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
          modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 34.dp, max = 38.dp)
            .testTag("save_apk_storage_button"),
          shape = RoundedCornerShape(8.dp),
          border = androidx.compose.foundation.BorderStroke(1.dp, MetalPrimary)
        ) {
          Icon(Icons.Default.Download, contentDescription = null, tint = MetalPrimary, modifier = Modifier.size(15.dp))
          Spacer(modifier = Modifier.width(6.dp))
          Text("Guardar .APK en Descargas / Archivos", color = MetalPrimary, fontSize = 11.sp, fontWeight = FontWeight.Bold)
        }

        if (statusMessage != null) {
          Spacer(modifier = Modifier.height(8.dp))
          Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            shape = RoundedCornerShape(8.dp)
          ) {
            Text(
              text = statusMessage!!,
              color = MetalSecondary,
              fontSize = 11.sp,
              modifier = Modifier.padding(8.dp)
            )
          }
        }
      }
    }
  }
}
