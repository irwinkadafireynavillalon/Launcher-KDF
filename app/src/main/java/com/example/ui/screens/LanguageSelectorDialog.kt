package com.example.ui.screens
import androidx.compose.ui.geometry.*
import androidx.compose.ui.draw.*
import androidx.compose.ui.graphics.*
import com.example.ui.theme.*

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Language
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog

@Composable
fun LanguageSelectorDialog(
  isOpen: Boolean,
  currentLanguage: String,
  onDismiss: () -> Unit,
  onSelectLanguage: (String) -> Unit
) {
  if (!isOpen) return

  val availableLanguages = listOf(
    "Inglés" to "English (US / UK)",
    "Español" to "Español (Latinoamérica / España)",
    "Francés" to "Français (France / Canada)",
    "Alemán" to "Deutsch (Deutschland)",
    "Italiano" to "Italiano (Italia)",
    "Portugués" to "Português (Brasil / Portugal)",
    "Japonés" to "日本語 (Nihongo)",
    "Chino Mandarín" to "中文 (Mandarin)",
    "Ruso" to "Русский (Ruso)",
    "Árabe" to "العربية (Árabe)",
    "Coreano" to "한국어 (Coreano)"
  )

  Dialog(onDismissRequest = onDismiss) {
    Card(
      modifier = Modifier
        .fillMaxWidth()
        .heightIn(max = 560.dp)
        .border(1.2.dp, MetalSecondary.copy(alpha = 0.85f), RoundedCornerShape(16.dp)),
      shape = RoundedCornerShape(16.dp),
      colors = CardDefaults.cardColors(containerColor = Color(0xF50B0D14))
    ) {
      Column(
        modifier = Modifier
          .fillMaxWidth()
          .padding(14.dp)
      ) {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
              modifier = Modifier
                .size(34.dp)
                .background(MetalSecondary.copy(alpha = 0.18f), CircleShape),
              contentAlignment = Alignment.Center
            ) {
              Icon(Icons.Default.Language, contentDescription = null, tint = MetalSecondary, modifier = Modifier.size(20.dp))
            }
            Spacer(modifier = Modifier.width(10.dp))
            Column {
              Text("SELECCIÓN DE IDIOMA", color = TextMain, fontWeight = FontWeight.Bold, fontSize = 13.sp)
              Text("Voz, traducción y síntesis", color = MetalPrimary, fontSize = 10.sp)
            }
          }

          IconButton(onClick = onDismiss, modifier = Modifier.size(28.dp)) {
            Icon(Icons.Default.Close, contentDescription = "Cerrar", tint = TextSub, modifier = Modifier.size(18.dp))
          }
        }

        Spacer(modifier = Modifier.height(14.dp))

        Column(
          modifier = Modifier
            .weight(1f)
            .verticalScroll(rememberScrollState()),
          verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
          availableLanguages.forEach { (lang, nativeName) ->
            val isSelected = currentLanguage.equals(lang, ignoreCase = true)
            Row(
              modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(10.dp))
                .background(if (isSelected) MetalSecondary.copy(alpha = 0.22f) else Color.White)
                .border(
                  width = if (isSelected) 1.2.dp else 0.6.dp,
                  color = if (isSelected) MetalSecondary else MetalPrimary,
                  shape = RoundedCornerShape(10.dp)
                )
                .clickable {
                  onSelectLanguage(lang)
                  onDismiss()
                }
                .padding(horizontal = 12.dp, vertical = 10.dp),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically
            ) {
              Column {
                Text(text = lang, color = if (isSelected) MetalSecondary else TextMain, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                Text(text = nativeName, color = TextSub, fontSize = 10.sp)
              }
              if (isSelected) {
                Icon(Icons.Default.Check, contentDescription = null, tint = MetalSecondary, modifier = Modifier.size(18.dp))
              }
            }
          }
        }
      }
    }
  }
}
