package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Diamond
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.R
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.DarkSurfaceElevated
import com.example.ui.theme.MetalPrimary
import com.example.ui.theme.MetalSecondary
import com.example.ui.theme.TextMain
import com.example.ui.theme.TextSub

@Composable
fun LogoAndCssStudioDialog(
  isOpen: Boolean,
  onDismiss: () -> Unit
) {
  if (!isOpen) return

  val context = LocalContext.current
  var selectedTab by remember { mutableStateOf(0) } // 0 = Visual Logo & Specs, 1 = CSS Code, 2 = Android XML

  val cssCode = remember {
    """
/* ========================================================
   LAUNCH KDF LUXURY EMBLEM - CSS & HTML STYLING SPEC
   Crown, 24K Gold & Silver 3D Lettering, Cyber Shield
   ======================================================== */

.kdf-badge-container {
  width: 280px;
  height: 280px;
  position: relative;
  border-radius: 48px;
  background: radial-gradient(circle at 50% 30%, #15102a 0%, #06030c 85%);
  border: 4px solid;
  border-image: linear-gradient(135deg, #ffd700, #c0c0c0, #ffb300, #e0e0e0) 1;
  box-shadow: 0 0 35px rgba(255, 215, 0, 0.45),
              inset 0 0 25px rgba(192, 192, 192, 0.25);
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  font-family: 'Montserrat', 'Inter', sans-serif;
  overflow: hidden;
}

/* Carbon Fiber Texture Overlay */
.kdf-badge-container::before {
  content: '';
  position: absolute;
  inset: 0;
  background: repeating-linear-gradient(
    45deg,
    rgba(255,255,255,0.03) 0px,
    rgba(255,255,255,0.03) 2px,
    transparent 2px,
    transparent 4px
  );
  pointer-events: none;
}

/* Royal 3D Crown */
.kdf-royal-crown {
  font-size: 38px;
  filter: drop-shadow(0 2px 8px rgba(255, 215, 0, 0.8));
  margin-bottom: -6px;
  z-index: 2;
}

/* 3D Beveled Gold & Silver Letters */
.kdf-letters {
  font-size: 54px;
  font-weight: 900;
  letter-spacing: 2px;
  background: linear-gradient(135deg, #fff7c2 0%, #ffd700 35%, #e0e0e0 65%, #ffffff 100%);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  filter: drop-shadow(2px 4px 6px rgba(0, 0, 0, 0.95))
          drop-shadow(0 0 12px rgba(255, 215, 0, 0.6));
  z-index: 2;
}

/* Sub-Ribbon Badge */
.kdf-ribbon {
  background: linear-gradient(90deg, #1f1435, #2d1e4c, #1f1435);
  border: 1.5px solid #ffd700;
  border-radius: 12px;
  padding: 4px 16px;
  font-size: 11px;
  font-weight: 800;
  color: #ffd700;
  letter-spacing: 1.5px;
  text-transform: uppercase;
  box-shadow: 0 4px 10px rgba(0,0,0,0.5);
  margin-top: 4px;
  z-index: 2;
}
    """.trimIndent()
  }

  Dialog(
    onDismissRequest = onDismiss,
    properties = DialogProperties(usePlatformDefaultWidth = false)
  ) {
    Box(
      modifier = Modifier
        .padding(horizontal = 14.dp, vertical = 24.dp)
        .fillMaxWidth()
        .clip(RoundedCornerShape(18.dp))
        .background(DarkSurfaceElevated.copy(alpha = 0.98f))
        .border(1.2.dp, Brush.linearGradient(listOf(MetalSecondary, MetalPrimary, MetalSecondary)), RoundedCornerShape(18.dp))
        .padding(14.dp)
        .testTag("logo_css_studio_dialog")
    ) {
      Column(
        modifier = Modifier
          .fillMaxWidth()
          .verticalScroll(rememberScrollState())
      ) {
        // Compact Header
        Row(
          modifier = Modifier.fillMaxWidth(),
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.SpaceBetween
        ) {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
              modifier = Modifier
                .size(32.dp)
                .clip(CircleShape)
                .background(MetalSecondary.copy(alpha = 0.2f))
                .border(1.dp, MetalSecondary, CircleShape),
              contentAlignment = Alignment.Center
            ) {
              Icon(
                imageVector = Icons.Default.Diamond,
                contentDescription = null,
                tint = MetalSecondary,
                modifier = Modifier.size(16.dp)
              )
            }
            Spacer(modifier = Modifier.width(8.dp))
            Column {
              Text(
                text = "STUDIO DE LOGO & CSS",
                color = MetalSecondary,
                fontSize = 12.sp,
                fontWeight = FontWeight.ExtraBold,
                letterSpacing = 0.5.sp
              )
              Text(
                text = "Emblema KDF • Corona Real • PNG & CSS",
                color = TextSub,
                fontSize = 8.5.sp
              )
            }
          }

          Box(
            modifier = Modifier
              .clip(RoundedCornerShape(6.dp))
              .background(DarkSurface)
              .border(0.8.dp, MetalPrimary.copy(alpha = 0.4f), RoundedCornerShape(6.dp))
              .clickable { onDismiss() }
              .padding(horizontal = 8.dp, vertical = 4.dp)
          ) {
            Text("Cerrar", color = TextMain, fontSize = 9.5.sp, fontWeight = FontWeight.Bold)
          }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Tab Selector Pills
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(DarkSurface)
            .padding(3.dp),
          horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
          listOf("Emblema KDF", "Código CSS 3D", "Android Icon").forEachIndexed { index, tabTitle ->
            val selected = selectedTab == index
            Box(
              modifier = Modifier
                .weight(1f)
                .clip(RoundedCornerShape(6.dp))
                .background(if (selected) MetalSecondary else Color.Transparent)
                .clickable { selectedTab = index }
                .padding(vertical = 5.dp),
              contentAlignment = Alignment.Center
            ) {
              Text(
                text = tabTitle,
                color = if (selected) Color.Black else TextSub,
                fontSize = 9.sp,
                fontWeight = if (selected) FontWeight.ExtraBold else FontWeight.Medium
              )
            }
          }
        }

        Spacer(modifier = Modifier.height(12.dp))

        when (selectedTab) {
          0 -> {
            // Visual Logo Preview & Specs
            Column(
              modifier = Modifier.fillMaxWidth(),
              horizontalAlignment = Alignment.CenterHorizontally
            ) {
              Box(
                modifier = Modifier
                  .size(150.dp)
                  .clip(RoundedCornerShape(26.dp))
                  .border(2.dp, Brush.linearGradient(listOf(MetalSecondary, MetalPrimary, MetalSecondary)), RoundedCornerShape(26.dp))
                  .background(Color(0xFF07040E)),
                contentAlignment = Alignment.Center
              ) {
                Image(
                  painter = painterResource(id = R.drawable.ic_kdf_logo),
                  contentDescription = "Logo KDF Launcher",
                  contentScale = ContentScale.Fit,
                  modifier = Modifier
                    .size(136.dp)
                    .clip(RoundedCornerShape(20.dp))
                )
              }

              Spacer(modifier = Modifier.height(10.dp))

              Text(
                text = "👑 LAUNCH KDF ROYAL SHIELD",
                color = MetalSecondary,
                fontSize = 11.sp,
                fontWeight = FontWeight.ExtraBold
              )
              Text(
                text = "Formato: PNG 32-bit RGBA • Resolución: 512x512 Master • Vector Safe-Zone 66dp",
                color = TextSub,
                fontSize = 8.5.sp
              )

              Spacer(modifier = Modifier.height(8.dp))

              Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(10.dp),
                colors = CardDefaults.cardColors(containerColor = DarkSurface),
                border = androidx.compose.foundation.BorderStroke(0.6.dp, MetalPrimary.copy(alpha = 0.25f))
              ) {
                Column(modifier = Modifier.padding(10.dp)) {
                  Text("Detalles de Composición:", color = TextMain, fontSize = 9.5.sp, fontWeight = FontWeight.Bold)
                  Spacer(modifier = Modifier.height(3.dp))
                  Text("• Corona Imperial de Oro y Diamantes en cúspide", color = TextSub, fontSize = 8.5.sp)
                  Text("• Tipografía 'KDF' con bisel 3D en Oro 24K y Plata 925", color = TextSub, fontSize = 8.5.sp)
                  Text("• Escudo hexagonal de fibra de carbono con trazas de circuitos dorados", color = TextSub, fontSize = 8.5.sp)
                  Text("• Borde exterior enjoyado con destellos holográficos", color = TextSub, fontSize = 8.5.sp)
                  Text("• Convertido y adaptado a mipmap en 5 densidades (mdpi a xxxhdpi)", color = TextSub, fontSize = 8.5.sp)
                }
              }
            }
          }

          1 -> {
            // CSS Code Viewer & Copy
            Column(modifier = Modifier.fillMaxWidth()) {
              Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
              ) {
                Text(
                  text = "Código CSS para Web & UI",
                  color = MetalSecondary,
                  fontSize = 10.sp,
                  fontWeight = FontWeight.Bold
                )

                Box(
                  modifier = Modifier
                    .clip(RoundedCornerShape(6.dp))
                    .background(MetalSecondary)
                    .clickable {
                      val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                      val clip = ClipData.newPlainText("KDF Logo CSS", cssCode)
                      clipboard.setPrimaryClip(clip)
                      Toast.makeText(context, "Código CSS copiado al portapapeles", Toast.LENGTH_SHORT).show()
                    }
                    .padding(horizontal = 8.dp, vertical = 3.dp)
                ) {
                  Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                      imageVector = Icons.Default.ContentCopy,
                      contentDescription = null,
                      tint = Color.Black,
                      modifier = Modifier.size(11.dp)
                    )
                    Spacer(modifier = Modifier.width(3.dp))
                    Text("Copiar CSS", color = Color.Black, fontSize = 8.5.sp, fontWeight = FontWeight.ExtraBold)
                  }
                }
              }

              Spacer(modifier = Modifier.height(6.dp))

              Box(
                modifier = Modifier
                  .fillMaxWidth()
                  .clip(RoundedCornerShape(8.dp))
                  .background(Color(0xFF090514))
                  .border(0.7.dp, MetalPrimary.copy(alpha = 0.3f), RoundedCornerShape(8.dp))
                  .padding(8.dp)
              ) {
                Text(
                  text = cssCode,
                  color = Color(0xFFC7D2FE),
                  fontSize = 8.sp,
                  fontFamily = FontFamily.Monospace,
                  lineHeight = 11.sp
                )
              }
            }
          }

          2 -> {
            // Android Launcher XML Spec
            Column(modifier = Modifier.fillMaxWidth()) {
              Text(
                text = "Integración Adaptive Icon Android",
                color = MetalSecondary,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold
              )
              Spacer(modifier = Modifier.height(4.dp))
              Text(
                text = "Ruta: res/drawable/ic_kdf_logo.png\nForeground: res/drawable/ic_launcher_foreground.xml\nMipmaps: mipmap-mdpi, hdpi, xhdpi, xxhdpi, xxxhdpi (ic_launcher.png y ic_launcher_round.png)",
                color = TextSub,
                fontSize = 8.5.sp
              )
              Spacer(modifier = Modifier.height(6.dp))
              Box(
                modifier = Modifier
                  .fillMaxWidth()
                  .clip(RoundedCornerShape(8.dp))
                  .background(Color(0xFF090514))
                  .border(0.7.dp, MetalPrimary.copy(alpha = 0.3f), RoundedCornerShape(8.dp))
                  .padding(8.dp)
              ) {
                Text(
                  text = """<layer-list xmlns:android="http://schemas.android.com/apk/res/android">
    <item
        android:width="76dp"
        android:height="76dp"
        android:drawable="@drawable/ic_kdf_logo"
        android:gravity="center" />
</layer-list>""",
                  color = Color(0xFFC7D2FE),
                  fontSize = 8.5.sp,
                  fontFamily = FontFamily.Monospace
                )
              }
            }
          }
        }
      }
    }
  }
}
