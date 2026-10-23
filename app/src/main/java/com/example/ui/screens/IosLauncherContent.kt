package com.example.ui.screens

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Message
import androidx.compose.material.icons.filled.BatteryChargingFull
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Cloud
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.Headphones
import androidx.compose.material.icons.filled.Message
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Smartphone
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.WbSunny
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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.DarkSurfaceElevated
import com.example.ui.theme.MetalPrimary
import com.example.ui.theme.MetalSecondary
import com.example.ui.theme.TextMain
import com.example.ui.theme.TextSub

@Composable
fun IosLauncherContent(
  dateStr: String,
  timeStr: String,
  batteryPct: Int,
  installedApps: List<LauncherAppEntry>,
  searchQuery: String,
  onSearchQueryChange: (String) -> Unit,
  onAppClick: (LauncherAppEntry) -> Unit,
  onAppLongClick: (LauncherAppEntry) -> Unit,
  onOpenKdfStudio: () -> Unit
) {
  val context = LocalContext.current
  val filteredApps = remember(installedApps, searchQuery) {
    if (searchQuery.isBlank()) installedApps
    else installedApps.filter {
      it.label.contains(searchQuery, ignoreCase = true) ||
          it.packageName.contains(searchQuery, ignoreCase = true)
    }
  }

  Column(
    modifier = Modifier
      .fillMaxSize()
      .testTag("ios_launcher_mode_view")
  ) {
    // 1. iOS Top Widgets Row (Weather Widget + Battery Ring Widget)
    IosWidgetsRow(batteryPct = batteryPct)

    Spacer(modifier = Modifier.height(10.dp))

    // 2. iOS Spotlight Search Bar
    IosSpotlightSearchBar(
      searchQuery = searchQuery,
      onSearchQueryChange = onSearchQueryChange
    )

    Spacer(modifier = Modifier.height(8.dp))

    // 3. iOS Springboard Apps Grid (Squircles with Red Badges)
    LazyVerticalGrid(
      columns = GridCells.Fixed(4),
      modifier = Modifier
        .weight(1f)
        .fillMaxWidth()
        .testTag("ios_apps_grid"),
      contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
      verticalArrangement = Arrangement.spacedBy(14.dp),
      horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
      items(filteredApps, key = { it.id }) { app ->
        IosAppTile(
          app = app,
          onClick = { onAppClick(app) },
          onLongClick = { onAppLongClick(app) }
        )
      }
    }

    // 4. Iconic iOS Frosted Glass Floating Dock
    IosFrostedGlassDock(
      onOpenPhone = { openSystemIntent(context, Intent(Intent.ACTION_DIAL)) },
      onOpenSafari = { openSystemIntent(context, Intent(Intent.ACTION_VIEW, Uri.parse("https://www.apple.com"))) },
      onOpenMessages = { openSystemIntent(context, Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_APP_MESSAGING)) },
      onOpenKdf = onOpenKdfStudio
    )
  }
}

@Composable
fun IosWidgetsRow(batteryPct: Int) {
  Row(
    modifier = Modifier
      .fillMaxWidth()
      .padding(horizontal = 4.dp),
    horizontalArrangement = Arrangement.spacedBy(10.dp)
  ) {
    // Left: iOS Weather Widget
    Card(
      modifier = Modifier
        .weight(1f)
        .height(108.dp)
        .shadow(4.dp, RoundedCornerShape(20.dp)),
      shape = RoundedCornerShape(20.dp),
      colors = CardDefaults.cardColors(
        containerColor = Color.Transparent
      )
    ) {
      Box(
        modifier = Modifier
          .fillMaxSize()
          .background(
            Brush.verticalGradient(
              listOf(Color(0xFF1B4F8C), Color(0xFF2A75D3))
            )
          )
          .padding(10.dp)
      ) {
        Column(modifier = Modifier.fillMaxSize()) {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Top
          ) {
            Column {
              Text("Madrid", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
              Text("22°", color = Color.White, fontSize = 24.sp, fontWeight = FontWeight.ExtraBold)
            }
            Icon(
              imageVector = Icons.Default.WbSunny,
              contentDescription = null,
              tint = Color(0xFFFFD700),
              modifier = Modifier.size(24.dp)
            )
          }
          Spacer(modifier = Modifier.weight(1f))
          Text("Despejado", color = Color.White.copy(alpha = 0.9f), fontSize = 9.sp, fontWeight = FontWeight.Medium)
          Text("Máx: 25° • Mín: 14°", color = Color.White.copy(alpha = 0.75f), fontSize = 8.sp)
        }
      }
    }

    // Right: iOS Battery Ring Widget
    Card(
      modifier = Modifier
        .weight(1f)
        .height(108.dp)
        .shadow(4.dp, RoundedCornerShape(20.dp)),
      shape = RoundedCornerShape(20.dp),
      colors = CardDefaults.cardColors(
        containerColor = Color(0xFF1C1C1E).copy(alpha = 0.92f)
      ),
      border = androidx.compose.foundation.BorderStroke(0.8.dp, Color(0xFF38383A))
    ) {
      Column(
        modifier = Modifier
          .fillMaxSize()
          .padding(10.dp),
        verticalArrangement = Arrangement.SpaceBetween
      ) {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Text("Baterías", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
          Text("$batteryPct%", color = Color(0xFF34C759), fontSize = 12.sp, fontWeight = FontWeight.ExtraBold)
        }

        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceAround,
          verticalAlignment = Alignment.CenterVertically
        ) {
          // iPhone Ring
          BatteryMiniRing(
            percentage = batteryPct,
            color = Color(0xFF34C759),
            icon = Icons.Default.Smartphone
          )
          // AirPods Ring
          BatteryMiniRing(
            percentage = 85,
            color = Color(0xFF34C759),
            icon = Icons.Default.Headphones
          )
          // Watch Ring
          BatteryMiniRing(
            percentage = 92,
            color = Color(0xFF34C759),
            icon = Icons.Default.BatteryChargingFull
          )
        }

        Text(
          text = "iPhone • AirPods • Watch",
          color = Color(0xFF8E8E93),
          fontSize = 7.5.sp,
          textAlign = TextAlign.Center,
          modifier = Modifier.fillMaxWidth()
        )
      }
    }
  }
}

@Composable
private fun BatteryMiniRing(
  percentage: Int,
  color: Color,
  icon: androidx.compose.ui.graphics.vector.ImageVector
) {
  Box(
    modifier = Modifier.size(36.dp),
    contentAlignment = Alignment.Center
  ) {
    Canvas(modifier = Modifier.fillMaxSize()) {
      val strokeW = 3.dp.toPx()
      // Background ring
      drawArc(
        color = Color(0xFF38383A),
        startAngle = 0f,
        sweepAngle = 360f,
        useCenter = false,
        style = Stroke(width = strokeW)
      )
      // Progress ring
      drawArc(
        color = color,
        startAngle = -90f,
        sweepAngle = (percentage / 100f) * 360f,
        useCenter = false,
        style = Stroke(width = strokeW)
      )
    }
    Icon(
      imageVector = icon,
      contentDescription = null,
      tint = Color.White,
      modifier = Modifier.size(14.dp)
    )
  }
}

@Composable
fun IosSpotlightSearchBar(
  searchQuery: String,
  onSearchQueryChange: (String) -> Unit
) {
  Row(
    modifier = Modifier
      .fillMaxWidth()
      .height(36.dp)
      .clip(RoundedCornerShape(10.dp))
      .background(Color(0xFF2C2C2E).copy(alpha = 0.85f))
      .padding(horizontal = 10.dp),
    verticalAlignment = Alignment.CenterVertically
  ) {
    Icon(
      imageVector = Icons.Default.Search,
      contentDescription = null,
      tint = Color(0xFF8E8E93),
      modifier = Modifier.size(15.dp)
    )
    Spacer(modifier = Modifier.width(6.dp))
    BasicTextField(
      value = searchQuery,
      onValueChange = onSearchQueryChange,
      modifier = Modifier.weight(1f),
      textStyle = TextStyle(
        color = Color.White,
        fontSize = 12.sp,
        fontWeight = FontWeight.Normal
      ),
      cursorBrush = SolidColor(Color(0xFF007AFF)),
      singleLine = true,
      decorationBox = { innerTextField ->
        if (searchQuery.isEmpty()) {
          Text(
            text = "Buscar en Spotlight",
            color = Color(0xFF8E8E93),
            fontSize = 11.5.sp
          )
        }
        innerTextField()
      }
    )
  }
}

@Composable
fun IosAppTile(
  app: LauncherAppEntry,
  onClick: () -> Unit,
  onLongClick: () -> Unit
) {
  // Deterministic notification badge counter for iOS realism
  val badgeCount = remember(app.id) {
    when {
      app.packageName.contains("dialer") || app.packageName.contains("phone") -> 1
      app.packageName.contains("mms") || app.packageName.contains("message") -> 3
      app.packageName.contains("mail") -> 5
      app.isInternalModule -> 2
      else -> 0
    }
  }

  Column(
    modifier = Modifier
      .fillMaxWidth()
      .clip(RoundedCornerShape(14.dp))
      .clickable { onClick() }
      .padding(2.dp),
    horizontalAlignment = Alignment.CenterHorizontally
  ) {
    Box(
      modifier = Modifier.size(52.dp),
      contentAlignment = Alignment.Center
    ) {
      // iOS Squircle Icon Shape (22% radius with subtle gradient gloss)
      Box(
        modifier = Modifier
          .fillMaxSize()
          .shadow(3.dp, RoundedCornerShape(13.dp))
          .clip(RoundedCornerShape(13.dp))
          .background(
            if (app.isInternalModule) {
              Brush.linearGradient(listOf(Color(0xFF007AFF), Color(0xFF5856D6)))
            } else {
              Brush.linearGradient(listOf(Color(0xFF2C2C2E), Color(0xFF1C1C1E)))
            }
          )
          .border(0.6.dp, Color.White.copy(alpha = 0.2f), RoundedCornerShape(13.dp)),
        contentAlignment = Alignment.Center
      ) {
        if (app.appIconBitmap != null) {
          Image(
            bitmap = app.appIconBitmap,
            contentDescription = app.label,
            modifier = Modifier
              .size(38.dp)
              .clip(RoundedCornerShape(8.dp))
          )
        } else {
          Icon(
            imageVector = app.internalIcon,
            contentDescription = app.label,
            tint = Color.White,
            modifier = Modifier.size(26.dp)
          )
        }
      }

      // iOS Red Notification Badge
      if (badgeCount > 0) {
        Box(
          modifier = Modifier
            .align(Alignment.TopEnd)
            .size(16.dp)
            .clip(CircleShape)
            .background(Color(0xFFFF3B30))
            .border(1.2.dp, Color.Black, CircleShape),
          contentAlignment = Alignment.Center
        ) {
          Text(
            text = "$badgeCount",
            color = Color.White,
            fontSize = 9.sp,
            fontWeight = FontWeight.ExtraBold
          )
        }
      }
    }

    Spacer(modifier = Modifier.height(4.dp))

    Text(
      text = app.label,
      color = Color.White,
      fontSize = 9.5.sp,
      fontWeight = FontWeight.Normal,
      maxLines = 1,
      overflow = TextOverflow.Ellipsis,
      textAlign = TextAlign.Center
    )
  }
}

@Composable
fun IosFrostedGlassDock(
  onOpenPhone: () -> Unit,
  onOpenSafari: () -> Unit,
  onOpenMessages: () -> Unit,
  onOpenKdf: () -> Unit
) {
  Box(
    modifier = Modifier
      .fillMaxWidth()
      .padding(horizontal = 14.dp, vertical = 6.dp)
      .height(64.dp)
      .clip(RoundedCornerShape(26.dp))
      .background(Color(0xFF2C2C2E).copy(alpha = 0.65f))
      .border(0.8.dp, Color.White.copy(alpha = 0.22f), RoundedCornerShape(26.dp))
      .padding(horizontal = 16.dp),
    contentAlignment = Alignment.Center
  ) {
    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.CenterVertically
    ) {
      // 1. Phone (iOS Green)
      IosDockIcon(
        bgBrush = Brush.linearGradient(listOf(Color(0xFF34C759), Color(0xFF30D158))),
        icon = Icons.Default.Call,
        onClick = onOpenPhone
      )
      // 2. Safari (iOS Blue)
      IosDockIcon(
        bgBrush = Brush.linearGradient(listOf(Color(0xFF007AFF), Color(0xFF5AC8FA))),
        icon = Icons.Default.Explore,
        onClick = onOpenSafari
      )
      // 3. Messages (iOS Green Bubble)
      IosDockIcon(
        bgBrush = Brush.linearGradient(listOf(Color(0xFF34C759), Color(0xFF30D158))),
        icon = Icons.AutoMirrored.Filled.Message,
        onClick = onOpenMessages
      )
      // 4. KDF Royal Shield
      Box(
        modifier = Modifier
          .size(46.dp)
          .shadow(4.dp, RoundedCornerShape(12.dp))
          .clip(RoundedCornerShape(12.dp))
          .background(Color(0xFF0D0618))
          .border(1.2.dp, Color(0xFFFFD700), RoundedCornerShape(12.dp))
          .clickable { onOpenKdf() },
        contentAlignment = Alignment.Center
      ) {
        Image(
          painter = painterResource(id = R.drawable.ic_kdf_logo),
          contentDescription = "KDF",
          modifier = Modifier.size(38.dp)
        )
      }
    }
  }
}

@Composable
private fun IosDockIcon(
  bgBrush: Brush,
  icon: androidx.compose.ui.graphics.vector.ImageVector,
  onClick: () -> Unit
) {
  Box(
    modifier = Modifier
      .size(46.dp)
      .shadow(4.dp, RoundedCornerShape(12.dp))
      .clip(RoundedCornerShape(12.dp))
      .background(bgBrush)
      .border(0.8.dp, Color.White.copy(alpha = 0.3f), RoundedCornerShape(12.dp))
      .clickable { onClick() },
    contentAlignment = Alignment.Center
  ) {
    Icon(
      imageVector = icon,
      contentDescription = null,
      tint = Color.White,
      modifier = Modifier.size(22.dp)
    )
  }
}
