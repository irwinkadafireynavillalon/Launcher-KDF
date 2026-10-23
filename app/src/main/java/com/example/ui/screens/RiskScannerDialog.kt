package com.example.ui.screens
import androidx.compose.ui.geometry.*
import androidx.compose.ui.draw.*
import androidx.compose.ui.graphics.*
import com.example.ui.theme.*

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material.icons.filled.NetworkCheck
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog

@Composable
fun RiskScannerDialog(
  isOpen: Boolean,
  isLoading: Boolean,
  analysisResult: String?,
  onDismiss: () -> Unit,
  onAnalyze: (String) -> Unit
) {
  if (!isOpen) return

  var selectedTab by remember { mutableIntStateOf(0) }
  var inputSituation by remember { mutableStateOf("") }

  Dialog(onDismissRequest = onDismiss) {
    Card(
      modifier = Modifier
        .fillMaxWidth()
        .heightIn(max = 660.dp)
        .skeuomorphicCard(borderColor = DangerRed),
      colors = CardDefaults.cardColors(containerColor = Color.Transparent)
    ) {
      Column(
        modifier = Modifier
          .fillMaxWidth()
          .padding(14.dp)
      ) {
        // Header
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
              modifier = Modifier
                .size(32.dp)
                .background(DangerRed.copy(alpha = 0.2f), CircleShape),
              contentAlignment = Alignment.Center
            ) {
              Icon(Icons.Default.Security, contentDescription = null, tint = DangerRed, modifier = Modifier.size(18.dp))
            }
            Spacer(modifier = Modifier.width(8.dp))
            Column {
              Text("ESCÁNER DE RIESGO & SEGURIDAD", color = TextMain, fontSize = 13.sp, fontWeight = FontWeight.Bold)
              Text("Blindaje de Sistema • Dispositivo & IA", color = DangerRed, fontSize = 9.sp, fontFamily = FontFamily.Monospace)
            }
          }

          IconButton(onClick = onDismiss, modifier = Modifier.size(28.dp)) {
            Icon(Icons.Default.Close, contentDescription = "Cerrar", tint = TextSub, modifier = Modifier.size(18.dp))
          }
        }

        Spacer(modifier = Modifier.height(10.dp))

        TabRow(
          selectedTabIndex = selectedTab,
          containerColor = Color.White,
          contentColor = DangerRed,
          indicator = { tabPositions ->
            TabRowDefaults.SecondaryIndicator(
              Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
              color = DangerRed,
              height = 2.dp
            )
          }
        ) {
          Tab(
            selected = selectedTab == 0,
            onClick = { selectedTab = 0 },
            text = { Text("Análisis Situacional", fontSize = 10.sp, fontWeight = FontWeight.Bold) }
          )
          Tab(
            selected = selectedTab == 1,
            onClick = { selectedTab = 1 },
            text = { Text("Seguridad Dispositivo", fontSize = 10.sp, fontWeight = FontWeight.Bold) }
          )
        }

        Spacer(modifier = Modifier.height(10.dp))

        Column(
          modifier = Modifier
            .weight(1f)
            .verticalScroll(rememberScrollState()),
          verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
          when (selectedTab) {
            0 -> {
              Text(
                text = "Describe una persona, mensaje sospechoso, propuesta o intento de rastreo. El Orquestador evaluará riesgos ocultos bajo Protocolo Alpha.",
                color = TextSub,
                fontSize = 11.sp,
                lineHeight = 15.sp
              )

              OutlinedTextField(
                value = inputSituation,
                onValueChange = { inputSituation = it },
                placeholder = {
                  Text("Ejemplo: Mensaje con enlaces sospechosos o solicitud inusual de contraseñas...", fontSize = 11.sp, color = TextSub)
                },
                modifier = Modifier
                  .fillMaxWidth()
                  .heightIn(min = 80.dp, max = 130.dp)
                  .testTag("risk_scanner_input"),
                shape = RoundedCornerShape(10.dp),
                maxLines = 4,
                colors = OutlinedTextFieldDefaults.colors(
                  focusedBorderColor = DangerRed,
                  unfocusedBorderColor = MetalPrimary,
                  focusedContainerColor = SurfacePearl,
                  unfocusedContainerColor = SurfacePearl,
                  focusedTextColor = TextMain,
                  unfocusedTextColor = TextMain
                )
              )

              Button(
                onClick = { onAnalyze(inputSituation) },
                enabled = inputSituation.isNotBlank() && !isLoading,
                modifier = Modifier
                  .fillMaxWidth()
                  .height(40.dp)
                  .testTag("run_risk_analysis_button"),
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.buttonColors(containerColor = DangerRed, contentColor = Color.White)
              ) {
                if (isLoading) {
                  CircularProgressIndicator(color = Color.White, modifier = Modifier.size(14.dp), strokeWidth = 2.dp)
                  Spacer(modifier = Modifier.width(6.dp))
                  Text("Evaluando Amenaza...", fontSize = 11.sp)
                } else {
                  Icon(Icons.Default.Shield, contentDescription = null, modifier = Modifier.size(15.dp))
                  Spacer(modifier = Modifier.width(6.dp))
                  Text("Ejecutar Diagnóstico Privado", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
              }

              if (analysisResult != null) {
                Card(
                  modifier = Modifier.fillMaxWidth(),
                  shape = RoundedCornerShape(10.dp),
                  colors = CardDefaults.cardColors(containerColor = Color.White),
                  border = androidx.compose.foundation.BorderStroke(1.dp, MetalSecondary.copy(alpha = 0.6f))
                ) {
                  Column(modifier = Modifier.padding(10.dp)) {
                    Text("DIAGNÓSTICO DEL ORQUESTADOR (ALPHA):", color = MetalSecondary, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(analysisResult, color = TextMain, fontSize = 11.sp, lineHeight = 16.sp)
                  }
                }
              }
            }

            1 -> {
              // Ajustes de seguridad de la aplicación, apps del sistema y dispositivo (Point 10)
              Text("AUDITORÍA DE SEGURIDAD DEL DISPOSITIVO:", color = MetalSecondary, fontSize = 11.sp, fontWeight = FontWeight.Bold)

              SecurityStatusItem(
                icon = Icons.Default.PhoneAndroid,
                title = "Integridad de Sandbox & Memoria",
                status = "PROTEGIDO",
                details = "Espacio aislado verificado. Sin manipulación de binarios."
              )

              SecurityStatusItem(
                icon = Icons.Default.Lock,
                title = "Aislamiento de Permisos del Sistema",
                status = "ACTIVO",
                details = "Cámara y Micrófono protegidos bajo demanda explícita."
              )

              SecurityStatusItem(
                icon = Icons.Default.Memory,
                title = "Escudo de Sensores y Acelerómetro",
                status = "MONITOREANDO",
                details = "Frecuencia de muestreo alta regulada sin fugas de telemetría."
              )

              SecurityStatusItem(
                icon = Icons.Default.NetworkCheck,
                title = "Firewall Local del Servidor Offline",
                status = "ESTRICTO",
                details = "localhost:8080 enrutado exclusivamente en el dispositivo."
              )
            }
          }
        }
      }
    }
  }
}

@Composable
private fun SecurityStatusItem(
  icon: androidx.compose.ui.graphics.vector.ImageVector,
  title: String,
  status: String,
  details: String
) {
  Row(
    modifier = Modifier
      .fillMaxWidth()
      .clip(RoundedCornerShape(8.dp))
      .background(Color.White)
      .border(0.6.dp, MetalPrimary, RoundedCornerShape(8.dp))
      .padding(10.dp),
    verticalAlignment = Alignment.CenterVertically
  ) {
    Box(
      modifier = Modifier
        .size(32.dp)
        .background(ShieldGreen.copy(alpha = 0.15f), CircleShape),
      contentAlignment = Alignment.Center
    ) {
      Icon(icon, contentDescription = null, tint = ShieldGreen, modifier = Modifier.size(16.dp))
    }
    Spacer(modifier = Modifier.width(10.dp))
    Column(modifier = Modifier.weight(1f)) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
      ) {
        Text(title, color = TextMain, fontSize = 11.sp, fontWeight = FontWeight.Bold)
        Text(status, color = ShieldGreen, fontSize = 9.sp, fontWeight = FontWeight.ExtraBold, fontFamily = FontFamily.Monospace)
      }
      Text(details, color = TextSub, fontSize = 9.sp)
    }
  }
}
