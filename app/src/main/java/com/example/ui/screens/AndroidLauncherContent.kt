package com.example.ui.screens

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
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
import androidx.compose.material.icons.filled.Apps
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Sms
import androidx.compose.material.icons.filled.WbSunny
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.DarkSurfaceElevated
import com.example.ui.theme.MetalPrimary
import com.example.ui.theme.MetalSecondary
import com.example.ui.theme.TextMain
import com.example.ui.theme.TextSub
import com.example.ui.theme.UiThemeController

@Composable
fun AndroidLauncherContent(
  dateStr: String,
  timeStr: String,
  batteryPct: Int,
  installedApps: List<LauncherAppEntry>,
  searchQuery: String,
  onSearchQueryChange: (String) -> Unit,
  onAppClick: (LauncherAppEntry) -> Unit,
  onAppLongClick: (LauncherAppEntry) -> Unit,
  onOpenVoiceSearch: () -> Unit
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
      .testTag("android_launcher_mode_view")
  ) {
    // 1. Google Pixel Style "At a Glance" Widget
    PixelAtAGlanceWidget(
      dateStr = dateStr,
      batteryPct = batteryPct,
      onOpenCalendar = {
        openSystemIntent(context, Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_APP_CALENDAR))
      }
    )

    Spacer(modifier = Modifier.height(10.dp))

    // 2. Google Search Pill (Rounded capsule with G, Mic, and Lens)
    PixelSearchCapsule(
      searchQuery = searchQuery,
      onSearchQueryChange = onSearchQueryChange,
      onVoiceClick = onOpenVoiceSearch
    )

    Spacer(modifier = Modifier.height(10.dp))

    // 3. Android Material You Apps Grid
    LazyVerticalGrid(
      columns = GridCells.Fixed(4),
      modifier = Modifier
        .weight(1f)
        .fillMaxWidth()
        .testTag("android_apps_grid"),
      contentPadding = PaddingValues(horizontal = 6.dp, vertical = 6.dp),
      verticalArrangement = Arrangement.spacedBy(14.dp),
      horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
      items(filteredApps, key = { it.id }) { app ->
        AndroidAppTile(
          app = app,
          onClick = { onAppClick(app) },
          onLongClick = { onAppLongClick(app) }
        )
      }
    }

    // 4. Android Bottom Dock
    AndroidMaterialDock(
      onOpenPhone = { openSystemIntent(context, Intent(Intent.ACTION_DIAL)) },
      onOpenMessages = { openSystemIntent(context, Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_APP_MESSAGING)) },
      onOpenBrowser = { openSystemIntent(context, Intent(Intent.ACTION_VIEW, Uri.parse("https://www.google.com"))) },
      onOpenSettings = { openSystemIntent(context, Intent(Settings.ACTION_SETTINGS)) },
      onOpenAppDrawer = { onSearchQueryChange("") }
    )
  }
}

@Composable
fun PixelAtAGlanceWidget(
  dateStr: String,
  batteryPct: Int,
  onOpenCalendar: () -> Unit
) {
  Row(
    modifier = Modifier
      .fillMaxWidth()
      .clip(RoundedCornerShape(16.dp))
      .background(Color(0xFF1E2124).copy(alpha = 0.85f))
      .border(0.8.dp, Color(0xFF3C4043), RoundedCornerShape(16.dp))
      .clickable { onOpenCalendar() }
      .padding(horizontal = 14.dp, vertical = 10.dp),
    verticalAlignment = Alignment.CenterVertically,
    horizontalArrangement = Arrangement.SpaceBetween
  ) {
    Column {
      Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(
          imageVector = Icons.Default.CalendarToday,
          contentDescription = null,
          tint = Color(0xFF8AB4F8),
          modifier = Modifier.size(13.dp)
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(
          text = dateStr,
          color = Color.White,
          fontSize = 12.sp,
          fontWeight = FontWeight.SemiBold
        )
      }
      Spacer(modifier = Modifier.height(2.dp))
      Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(
          imageVector = Icons.Default.WbSunny,
          contentDescription = null,
          tint = Color(0xFFFDD663),
          modifier = Modifier.size(13.dp)
        )
        Spacer(modifier = Modifier.width(5.dp))
        Text(
          text = "24°C Despejado • Batería $batteryPct%",
          color = Color(0xFFBDC1C6),
          fontSize = 10.sp
        )
      }
    }

    // Pixel Accent Badge
    Box(
      modifier = Modifier
        .clip(RoundedCornerShape(50))
        .background(Color(0xFF8AB4F8).copy(alpha = 0.18f))
        .border(0.8.dp, Color(0xFF8AB4F8), RoundedCornerShape(50))
        .padding(horizontal = 8.dp, vertical = 3.dp)
    ) {
      Text(
        text = "Material You",
        color = Color(0xFF8AB4F8),
        fontSize = 8.5.sp,
        fontWeight = FontWeight.Bold
      )
    }
  }
}

@Composable
fun PixelSearchCapsule(
  searchQuery: String,
  onSearchQueryChange: (String) -> Unit,
  onVoiceClick: () -> Unit
) {
  Row(
    modifier = Modifier
      .fillMaxWidth()
      .height(44.dp)
      .shadow(4.dp, RoundedCornerShape(22.dp))
      .clip(RoundedCornerShape(22.dp))
      .background(Color(0xFF2D3135))
      .border(0.8.dp, Color(0xFF42474E), RoundedCornerShape(22.dp))
      .padding(horizontal = 12.dp),
    verticalAlignment = Alignment.CenterVertically
  ) {
    // Google "G" Badge
    Box(
      modifier = Modifier
        .size(24.dp)
        .clip(CircleShape)
        .background(Color.White),
      contentAlignment = Alignment.Center
    ) {
      Text(
        text = "G",
        color = Color(0xFF4285F4),
        fontSize = 14.sp,
        fontWeight = FontWeight.Black
      )
    }

    Spacer(modifier = Modifier.width(10.dp))

    BasicTextField(
      value = searchQuery,
      onValueChange = onSearchQueryChange,
      modifier = Modifier.weight(1f),
      textStyle = TextStyle(
        color = Color.White,
        fontSize = 12.sp,
        fontWeight = FontWeight.Normal
      ),
      cursorBrush = SolidColor(Color(0xFF8AB4F8)),
      singleLine = true,
      decorationBox = { innerTextField ->
        if (searchQuery.isEmpty()) {
          Text(
            text = "Buscar apps, contactos, web...",
            color = Color(0xFF9AA0A6),
            fontSize = 12.sp
          )
        }
        innerTextField()
      }
    )

    if (searchQuery.isNotEmpty()) {
      Icon(
        imageVector = Icons.Default.Clear,
        contentDescription = "Limpiar",
        tint = Color(0xFF9AA0A6),
        modifier = Modifier
          .size(16.dp)
          .clickable { onSearchQueryChange("") }
      )
      Spacer(modifier = Modifier.width(6.dp))
    }

    Icon(
      imageVector = Icons.Default.Mic,
      contentDescription = "Voz",
      tint = Color(0xFF34A853),
      modifier = Modifier
        .size(18.dp)
        .clickable { onVoiceClick() }
    )
  }
}

@Composable
fun AndroidAppTile(
  app: LauncherAppEntry,
  onClick: () -> Unit,
  onLongClick: () -> Unit
) {
  Column(
    modifier = Modifier
      .fillMaxWidth()
      .clip(RoundedCornerShape(12.dp))
      .clickable { onClick() }
      .padding(4.dp),
    horizontalAlignment = Alignment.CenterHorizontally
  ) {
    Box(
      modifier = Modifier
        .size(50.dp)
        .shadow(2.dp, CircleShape)
        .clip(CircleShape)
        .background(
          if (app.isInternalModule) Color(0xFF8AB4F8)
          else Color(0xFF303134)
        )
        .border(1.dp, Color(0xFF5F6368), CircleShape),
      contentAlignment = Alignment.Center
    ) {
      if (app.appIconBitmap != null) {
        Image(
          bitmap = app.appIconBitmap,
          contentDescription = app.label,
          modifier = Modifier.size(36.dp)
        )
      } else {
        Icon(
          imageVector = app.internalIcon,
          contentDescription = app.label,
          tint = if (app.isInternalModule) Color(0xFF1E2124) else Color.White,
          modifier = Modifier.size(26.dp)
        )
      }
    }

    Spacer(modifier = Modifier.height(4.dp))

    Text(
      text = app.label,
      color = Color(0xFFE8EAED),
      fontSize = 10.sp,
      fontWeight = FontWeight.Medium,
      maxLines = 1,
      overflow = TextOverflow.Ellipsis,
      textAlign = TextAlign.Center
    )
  }
}

@Composable
fun AndroidMaterialDock(
  onOpenPhone: () -> Unit,
  onOpenMessages: () -> Unit,
  onOpenBrowser: () -> Unit,
  onOpenSettings: () -> Unit,
  onOpenAppDrawer: () -> Unit
) {
  Row(
    modifier = Modifier
      .fillMaxWidth()
      .padding(vertical = 6.dp)
      .clip(RoundedCornerShape(24.dp))
      .background(Color(0xFF1E2124).copy(alpha = 0.92f))
      .border(0.8.dp, Color(0xFF3C4043), RoundedCornerShape(24.dp))
      .padding(horizontal = 14.dp, vertical = 6.dp),
    horizontalArrangement = Arrangement.SpaceAround,
    verticalAlignment = Alignment.CenterVertically
  ) {
    DockCircleButton(
      icon = Icons.Default.Call,
      tint = Color(0xFF34A853),
      onClick = onOpenPhone
    )
    DockCircleButton(
      icon = Icons.Default.Sms,
      tint = Color(0xFF4285F4),
      onClick = onOpenMessages
    )
    DockCircleButton(
      icon = Icons.Default.Language,
      tint = Color(0xFFEA4335),
      onClick = onOpenBrowser
    )
    DockCircleButton(
      icon = Icons.Default.Apps,
      tint = Color(0xFFFBBC04),
      onClick = onOpenAppDrawer
    )
  }
}

@Composable
private fun DockCircleButton(
  icon: androidx.compose.ui.graphics.vector.ImageVector,
  tint: Color,
  onClick: () -> Unit
) {
  Box(
    modifier = Modifier
      .size(42.dp)
      .clip(CircleShape)
      .background(Color(0xFF2D3135))
      .border(0.8.dp, tint.copy(alpha = 0.5f), CircleShape)
      .clickable { onClick() },
    contentAlignment = Alignment.Center
  ) {
    Icon(
      imageVector = icon,
      contentDescription = null,
      tint = tint,
      modifier = Modifier.size(20.dp)
    )
  }
}
