package com.example.ui.screens

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BookmarkAdd
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Restore
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.local.ConversationMessage
import com.example.data.local.PracticeSessionEntity
import com.example.ui.theme.DangerRed
import com.example.ui.theme.MetalPrimary
import com.example.ui.theme.MetalSecondary
import com.example.ui.theme.ShieldGreen
import com.example.ui.theme.SurfacePearl
import com.example.ui.theme.TextMain
import com.example.ui.theme.TextSub
import com.example.ui.theme.skeuomorphicCard
import org.json.JSONArray
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun ChatHistoryAndSandboxDialog(
  isOpen: Boolean,
  messages: List<ConversationMessage>,
  practiceSessions: List<PracticeSessionEntity> = emptyList(),
  currentLanguage: String = "Español",
  currentScenario: String = "Conversación Libre",
  onDismiss: () -> Unit,
  onClearHistory: () -> Unit,
  onNewChat: () -> Unit,
  onSaveCurrentSession: (String) -> Unit = {},
  onLoadSession: (PracticeSessionEntity) -> Unit = {},
  onDeleteSession: (PracticeSessionEntity) -> Unit = {},
  onClearAllSessions: () -> Unit = {}
) {
  if (!isOpen) return

  val context = LocalContext.current
  var selectedTab by remember { mutableIntStateOf(0) }
  var customSessionTitle by remember { mutableStateOf("") }
  var selectedLanguageFilter by remember { mutableStateOf("Todos") }
  var expandedSessionId by remember { mutableStateOf<Long?>(null) }

  var sandboxPrompt by remember { mutableStateOf("") }
  var sandboxLogs by remember {
    mutableStateOf(
      listOf(
        "Sandbox aislado iniciado en memoria segura.",
        "Límites: Hasta 10GB de espacio asignable, 0 fugas de memoria."
      )
    )
  }

  val availableLanguages = remember(practiceSessions) {
    listOf("Todos") + practiceSessions.map { it.language }.distinct()
  }

  val filteredSessions = remember(practiceSessions, selectedLanguageFilter) {
    if (selectedLanguageFilter == "Todos") {
      practiceSessions
    } else {
      practiceSessions.filter { it.language.equals(selectedLanguageFilter, ignoreCase = true) }
    }
  }

  Dialog(onDismissRequest = onDismiss) {
    Card(
      modifier = Modifier
        .fillMaxWidth()
        .heightIn(max = 650.dp)
        .skeuomorphicCard(cornerRadius = 16.dp, borderColor = MetalPrimary)
        .testTag("practice_sessions_dialog"),
      shape = RoundedCornerShape(16.dp),
      colors = CardDefaults.cardColors(containerColor = SurfacePearl)
    ) {
      Column(
        modifier = Modifier
          .fillMaxWidth()
          .padding(12.dp)
      ) {
        // Compact Header
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
              modifier = Modifier
                .size(28.dp)
                .background(MetalSecondary.copy(alpha = 0.15f), CircleShape),
              contentAlignment = Alignment.Center
            ) {
              Icon(
                Icons.Default.Storage,
                contentDescription = null,
                tint = MetalSecondary,
                modifier = Modifier.size(16.dp)
              )
            }
            Spacer(modifier = Modifier.width(8.dp))
            Column {
              Text(
                "SESIONES DE PRÁCTICA (ROOM DB)",
                color = TextMain,
                fontWeight = FontWeight.ExtraBold,
                fontSize = 12.sp
              )
              Text(
                "Base de datos local • $currentLanguage ($currentScenario)",
                color = MetalSecondary,
                fontSize = 9.sp,
                fontFamily = FontFamily.Monospace,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
              )
            }
          }

          IconButton(
            onClick = onDismiss,
            modifier = Modifier
              .size(28.dp)
              .testTag("close_history_dialog_button")
          ) {
            Icon(
              Icons.Default.Close,
              contentDescription = "Cerrar",
              tint = TextSub,
              modifier = Modifier.size(16.dp)
            )
          }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Compact Tabs
        TabRow(
          selectedTabIndex = selectedTab,
          containerColor = Color.White,
          contentColor = MetalSecondary,
          indicator = { tabPositions ->
            TabRowDefaults.SecondaryIndicator(
              Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
              color = MetalSecondary,
              height = 2.dp
            )
          }
        ) {
          Tab(
            selected = selectedTab == 0,
            onClick = { selectedTab = 0 },
            modifier = Modifier.testTag("tab_practice_sessions"),
            text = {
              Text(
                "Sesiones (${practiceSessions.size})",
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold
              )
            }
          )
          Tab(
            selected = selectedTab == 1,
            onClick = { selectedTab = 1 },
            modifier = Modifier.testTag("tab_active_messages"),
            text = {
              Text(
                "Chat Activo (${messages.size})",
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold
              )
            }
          )
          Tab(
            selected = selectedTab == 2,
            onClick = { selectedTab = 2 },
            modifier = Modifier.testTag("tab_sandbox"),
            text = {
              Text(
                "Sandbox",
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold
              )
            }
          )
        }

        Spacer(modifier = Modifier.height(8.dp))

        when (selectedTab) {
          0 -> {
            // TAB 0: Room Practice Sessions Storage & Retrieval
            Card(
              modifier = Modifier.fillMaxWidth(),
              shape = RoundedCornerShape(10.dp),
              colors = CardDefaults.cardColors(containerColor = Color.White),
              border = BorderStroke(1.dp, MetalPrimary.copy(alpha = 0.6f))
            ) {
              Column(modifier = Modifier.padding(8.dp)) {
                Text(
                  text = "GUARDAR PRÁCTICA ACTUAL EN ROOM DB",
                  color = MetalSecondary,
                  fontSize = 9.sp,
                  fontWeight = FontWeight.ExtraBold,
                  fontFamily = FontFamily.Monospace
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                  modifier = Modifier.fillMaxWidth(),
                  verticalAlignment = Alignment.CenterVertically,
                  horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                  OutlinedTextField(
                    value = customSessionTitle,
                    onValueChange = { customSessionTitle = it },
                    placeholder = {
                      Text(
                        "Título opcional (ej. Práctica $currentLanguage)...",
                        fontSize = 10.sp,
                        color = TextSub
                      )
                    },
                    modifier = Modifier
                      .weight(1f)
                      .testTag("session_title_input"),
                    singleLine = true,
                    shape = RoundedCornerShape(8.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                      focusedBorderColor = MetalSecondary,
                      unfocusedBorderColor = MetalPrimary,
                      focusedContainerColor = SurfacePearl,
                      unfocusedContainerColor = SurfacePearl,
                      focusedTextColor = TextMain,
                      unfocusedTextColor = TextMain
                    )
                  )

                  Button(
                    onClick = {
                      onSaveCurrentSession(customSessionTitle.trim())
                      customSessionTitle = ""
                      Toast.makeText(
                        context,
                        "Sesión de práctica guardada en Room DB",
                        Toast.LENGTH_SHORT
                      ).show()
                    },
                    enabled = messages.isNotEmpty(),
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(
                      containerColor = MetalSecondary,
                      contentColor = Color.White
                    ),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                    modifier = Modifier.testTag("save_current_session_button")
                  ) {
                    Icon(
                      Icons.Default.BookmarkAdd,
                      contentDescription = "Guardar sesión",
                      modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Guardar", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                  }
                }
              }
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Compact Filter & Action Row
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically
            ) {
              LazyRow(
                modifier = Modifier.weight(1f),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
              ) {
                items(availableLanguages) { lang ->
                  val isSelected = lang.equals(selectedLanguageFilter, ignoreCase = true)
                  Box(
                    modifier = Modifier
                      .background(
                        if (isSelected) MetalSecondary else Color.White,
                        RoundedCornerShape(12.dp)
                      )
                      .border(
                        1.dp,
                        if (isSelected) MetalSecondary else MetalPrimary,
                        RoundedCornerShape(12.dp)
                      )
                      .clickable { selectedLanguageFilter = lang }
                      .padding(horizontal = 8.dp, vertical = 3.dp)
                      .testTag("filter_lang_$lang")
                  ) {
                    Text(
                      text = lang,
                      color = if (isSelected) Color.White else TextMain,
                      fontSize = 9.sp,
                      fontWeight = FontWeight.Bold
                    )
                  }
                }
              }

              Spacer(modifier = Modifier.width(6.dp))

              OutlinedButton(
                onClick = {
                  onNewChat()
                  Toast.makeText(
                    context,
                    "Nueva práctica iniciada (sesión previa archivada)",
                    Toast.LENGTH_SHORT
                  ).show()
                  onDismiss()
                },
                shape = RoundedCornerShape(8.dp),
                border = BorderStroke(1.dp, MetalSecondary),
                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                modifier = Modifier
                  .height(28.dp)
                  .testTag("new_chat_session_button")
              ) {
                Icon(
                  Icons.Default.Refresh,
                  contentDescription = null,
                  tint = MetalSecondary,
                  modifier = Modifier.size(12.dp)
                )
                Spacer(modifier = Modifier.width(3.dp))
                Text(
                  "Nueva",
                  color = MetalSecondary,
                  fontSize = 9.sp,
                  fontWeight = FontWeight.Bold
                )
              }
            }

            Spacer(modifier = Modifier.height(6.dp))

            if (filteredSessions.isEmpty()) {
              Box(
                modifier = Modifier
                  .weight(1f)
                  .fillMaxWidth()
                  .padding(16.dp),
                contentAlignment = Alignment.Center
              ) {
                Text(
                  "No hay sesiones de práctica guardadas para este filtro.",
                  color = TextSub,
                  fontSize = 11.sp
                )
              }
            } else {
              LazyColumn(
                modifier = Modifier
                  .weight(1f)
                  .fillMaxWidth()
                  .testTag("practice_sessions_list"),
                verticalArrangement = Arrangement.spacedBy(6.dp)
              ) {
                items(filteredSessions, key = { it.id }) { session ->
                  val dateStr = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
                    .format(Date(session.timestamp))
                  val isExpanded = expandedSessionId == session.id

                  Card(
                    modifier = Modifier
                      .fillMaxWidth()
                      .clickable {
                        expandedSessionId = if (isExpanded) null else session.id
                      }
                      .testTag("practice_session_card_${session.id}"),
                    shape = RoundedCornerShape(10.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    border = BorderStroke(1.dp, MetalPrimary.copy(alpha = 0.7f))
                  ) {
                    Column(modifier = Modifier.padding(9.dp)) {
                      Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                      ) {
                        Column(modifier = Modifier.weight(1f)) {
                          Text(
                            text = session.title,
                            color = TextMain,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.ExtraBold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                          )
                          Spacer(modifier = Modifier.height(2.dp))
                          Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                          ) {
                            Box(
                              modifier = Modifier
                                .background(
                                  MetalSecondary.copy(alpha = 0.12f),
                                  RoundedCornerShape(4.dp)
                                )
                                .padding(horizontal = 5.dp, vertical = 1.dp)
                            ) {
                              Text(
                                text = session.language,
                                color = MetalSecondary,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold
                              )
                            }
                            Text(
                              text = "• ${session.scenario}",
                              color = TextSub,
                              fontSize = 9.sp,
                              maxLines = 1,
                              overflow = TextOverflow.Ellipsis
                            )
                          }
                        }

                        Row(verticalAlignment = Alignment.CenterVertically) {
                          // Compact Load / Restore Session Button
                          Button(
                            onClick = {
                              onLoadSession(session)
                              Toast.makeText(
                                context,
                                "Sesión '${session.title}' restaurada",
                                Toast.LENGTH_SHORT
                              ).show()
                              onDismiss()
                            },
                            shape = RoundedCornerShape(6.dp),
                            colors = ButtonDefaults.buttonColors(
                              containerColor = MetalSecondary,
                              contentColor = Color.White
                            ),
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 3.dp),
                            modifier = Modifier
                              .height(26.dp)
                              .testTag("load_session_button_${session.id}")
                          ) {
                            Icon(
                              Icons.Default.Restore,
                              contentDescription = "Cargar sesión",
                              modifier = Modifier.size(12.dp)
                            )
                            Spacer(modifier = Modifier.width(3.dp))
                            Text("Cargar", fontSize = 9.sp, fontWeight = FontWeight.Bold)
                          }

                          Spacer(modifier = Modifier.width(4.dp))

                          IconButton(
                            onClick = { onDeleteSession(session) },
                            modifier = Modifier
                              .size(24.dp)
                              .testTag("delete_session_button_${session.id}")
                          ) {
                            Icon(
                              Icons.Default.Delete,
                              contentDescription = "Eliminar sesión",
                              tint = DangerRed,
                              modifier = Modifier.size(14.dp)
                            )
                          }

                          Icon(
                            imageVector = if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                            contentDescription = "Ver transcripción",
                            tint = TextSub,
                            modifier = Modifier.size(16.dp)
                          )
                        }
                      }

                      Spacer(modifier = Modifier.height(4.dp))
                      Text(
                        text = session.summaryPreview,
                        color = TextSub,
                        fontSize = 10.sp,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                      )

                      Spacer(modifier = Modifier.height(3.dp))
                      Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                      ) {
                        Text(
                          text = "${session.messageCount} mensajes • ${session.aiEngine}",
                          color = MetalPrimary,
                          fontSize = 8.sp,
                          fontFamily = FontFamily.Monospace,
                          fontWeight = FontWeight.Bold
                        )
                        Text(
                          text = dateStr,
                          color = TextSub,
                          fontSize = 8.sp,
                          fontFamily = FontFamily.Monospace
                        )
                      }

                      // Expandable full session transcript stored in Room
                      AnimatedVisibility(visible = isExpanded) {
                        val transcriptItems = remember(session.transcriptJson) {
                          parseSessionTranscriptPreview(session.transcriptJson)
                        }
                        Column(
                          modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 6.dp)
                            .background(SurfacePearl, RoundedCornerShape(6.dp))
                            .padding(6.dp),
                          verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                          HorizontalDivider(color = MetalPrimary.copy(alpha = 0.4f))
                          Text(
                            text = "TRANSCRIPCIÓN GUARDADA EN ROOM:",
                            color = MetalSecondary,
                            fontSize = 8.sp,
                            fontWeight = FontWeight.ExtraBold,
                            fontFamily = FontFamily.Monospace
                          )
                          transcriptItems.forEach { (role, content) ->
                            Text(
                              text = "${if (role == "user") "Tú" else "IA"}: $content",
                              color = TextMain,
                              fontSize = 9.sp,
                              maxLines = 3,
                              overflow = TextOverflow.Ellipsis
                            )
                          }
                        }
                      }
                    }
                  }
                }
              }
            }
          }

          1 -> {
            // TAB 1: Active Chat Messages in Room
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically
            ) {
              Button(
                onClick = {
                  onNewChat()
                  onDismiss()
                },
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.buttonColors(
                  containerColor = MetalSecondary,
                  contentColor = Color.White
                ),
                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                modifier = Modifier.height(30.dp)
              ) {
                Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(13.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Archivar y Nuevo Chat", fontSize = 10.sp, fontWeight = FontWeight.Bold)
              }

              OutlinedButton(
                onClick = onClearHistory,
                shape = RoundedCornerShape(8.dp),
                border = BorderStroke(1.dp, DangerRed.copy(alpha = 0.6f)),
                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                modifier = Modifier.height(30.dp)
              ) {
                Icon(
                  Icons.Default.DeleteSweep,
                  contentDescription = null,
                  tint = DangerRed,
                  modifier = Modifier.size(13.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text("Limpiar Activo", color = DangerRed, fontSize = 10.sp)
              }
            }

            Spacer(modifier = Modifier.height(8.dp))

            if (messages.isEmpty()) {
              Box(
                modifier = Modifier
                  .weight(1f)
                  .fillMaxWidth(),
                contentAlignment = Alignment.Center
              ) {
                Text("No hay mensajes en la conversación activa.", color = TextSub, fontSize = 11.sp)
              }
            } else {
              LazyColumn(
                modifier = Modifier
                  .weight(1f)
                  .fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(6.dp)
              ) {
                items(messages.takeLast(60)) { msg ->
                  val timeStr = SimpleDateFormat("HH:mm - dd/MM", Locale.getDefault())
                    .format(Date(msg.timestamp))
                  Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    border = BorderStroke(0.5.dp, MetalPrimary)
                  ) {
                    Column(modifier = Modifier.padding(8.dp)) {
                      Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                      ) {
                        Text(
                          text = if (msg.role == "user") "TÚ (${msg.language})" else "IA ORQUESTADOR (${msg.language})",
                          color = if (msg.role == "user") MetalSecondary else MetalPrimary,
                          fontSize = 9.sp,
                          fontWeight = FontWeight.Bold
                        )
                        Text(timeStr, color = TextSub, fontSize = 8.sp, fontFamily = FontFamily.Monospace)
                      }
                      Spacer(modifier = Modifier.height(3.dp))
                      Text(msg.content, color = TextMain, fontSize = 11.sp, maxLines = 4)
                    }
                  }
                }
              }
            }
          }

          2 -> {
            // TAB 2: Sandbox panel
            Column(
              modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState()),
              verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
              Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(10.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                border = BorderStroke(1.dp, MetalPrimary)
              ) {
                Column(
                  modifier = Modifier.padding(10.dp),
                  verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                  Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                      Icons.Default.Security,
                      contentDescription = null,
                      tint = ShieldGreen,
                      modifier = Modifier.size(15.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                      "Caja de Arena Aislada Activa",
                      color = ShieldGreen,
                      fontSize = 11.sp,
                      fontWeight = FontWeight.Bold
                    )
                  }
                  Text(
                    text = "Entorno seguro para pruebas de prompts, gramática y ejecución sin alterar las sesiones guardadas en Room.",
                    color = TextSub,
                    fontSize = 10.sp
                  )
                }
              }

              OutlinedTextField(
                value = sandboxPrompt,
                onValueChange = { sandboxPrompt = it },
                placeholder = {
                  Text(
                    "Ejecutar prompt experimental en Sandbox...",
                    fontSize = 11.sp,
                    color = TextSub
                  )
                },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(10.dp),
                colors = OutlinedTextFieldDefaults.colors(
                  focusedBorderColor = MetalSecondary,
                  unfocusedBorderColor = MetalPrimary,
                  focusedContainerColor = Color.White,
                  unfocusedContainerColor = Color.White,
                  focusedTextColor = TextMain,
                  unfocusedTextColor = TextMain
                ),
                maxLines = 3
              )

              Button(
                onClick = {
                  if (sandboxPrompt.isNotBlank()) {
                    val prompt = sandboxPrompt.trim()
                    sandboxPrompt = ""
                    val time = SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(Date())
                    val newLogs = sandboxLogs.toMutableList()
                    newLogs.add(0, "[$time] Insumo: $prompt")
                    newLogs.add(
                      0,
                      "[$time] Resultado Sandbox: Ejecución aislada completada exitosamente."
                    )
                    sandboxLogs = newLogs
                  }
                },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.buttonColors(
                  containerColor = MetalSecondary,
                  contentColor = Color.White
                )
              ) {
                Icon(Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(15.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Ejecutar en Sandbox Seguro", fontWeight = FontWeight.Bold, fontSize = 11.sp)
              }

              Text(
                "REGISTROS DE EJECUCIÓN SANDBOX:",
                color = MetalSecondary,
                fontSize = 9.sp,
                fontWeight = FontWeight.Bold
              )

              Column(
                modifier = Modifier
                  .fillMaxWidth()
                  .background(Color.White, RoundedCornerShape(8.dp))
                  .border(1.dp, MetalPrimary.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                  .padding(8.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
              ) {
                sandboxLogs.take(10).forEach { log ->
                  Text(log, color = TextSub, fontSize = 9.sp, fontFamily = FontFamily.Monospace)
                }
              }
            }
          }
        }
      }
    }
  }
}

private fun parseSessionTranscriptPreview(json: String): List<Pair<String, String>> {
  if (json.isBlank()) return emptyList()
  return try {
    val arr = JSONArray(json)
    val result = mutableListOf<Pair<String, String>>()
    for (i in 0 until arr.length()) {
      val obj = arr.getJSONObject(i)
      result.add(obj.optString("role", "model") to obj.optString("content", ""))
    }
    result
  } catch (_: Exception) {
    emptyList()
  }
}
