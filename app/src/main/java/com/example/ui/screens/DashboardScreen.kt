package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.MetalSecondary
import com.example.ui.theme.TextMain
import com.example.ui.theme.TextSub

@Composable
fun DashboardScreen(onDismiss: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0B0D14))
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Text("Dashboard de Progreso", color = TextMain, fontSize = 20.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(16.dp))

        // Vocab Chart
        Text("Progreso de Vocabulario", color = MetalSecondary, fontSize = 14.sp)
        Card(
            modifier = Modifier.fillMaxWidth().height(200.dp),
            colors = CardDefaults.cardColors(containerColor = DarkSurface)
        ) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("Gráfico de Vocabulario (Vico)", color = TextSub)
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Conv Chart
        Text("Frecuencia de Conversación", color = MetalSecondary, fontSize = 14.sp)
        Card(
            modifier = Modifier.fillMaxWidth().height(200.dp),
            colors = CardDefaults.cardColors(containerColor = DarkSurface)
        ) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("Gráfico de Conversación (Vico)", color = TextSub)
            }
        }
    }
}
