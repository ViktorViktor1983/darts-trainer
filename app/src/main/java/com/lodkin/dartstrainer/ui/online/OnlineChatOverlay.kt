package com.lodkin.dartstrainer.ui.online

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.lodkin.dartstrainer.theme.Accent
import com.lodkin.dartstrainer.theme.DarkBg
import com.lodkin.dartstrainer.theme.GoldAccent
import com.lodkin.dartstrainer.theme.TileBg
import com.lodkin.dartstrainer.theme.TileBgDark
import kotlinx.coroutines.launch

// Модель сообщения в чате
data class ChatMessage(
    val id: String,
    val senderName: String,
    val text: String,
    val isSystem: Boolean = false,
    val isMyMessage: Boolean = false
)

@Composable
fun OnlineChatOverlay(
    modifier: Modifier = Modifier,
    isExpandedInitially: Boolean = true,
    messages: List<ChatMessage>,
    unreadCount: Int,
    onSendMessage: (String) -> Unit
) {
    var isExpanded by remember { mutableStateOf(isExpandedInitially) }
    var inputText by remember { mutableStateOf("") }
    val listState = rememberLazyListState()
    val scope = rememberCoroutineScope()

    // Автоскролл вниз при новом сообщении
    LaunchedEffect(messages.size) {
        if (isExpanded && messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.size - 1)
        }
    }

    // Сброс непрочитанных при открытии
    LaunchedEffect(isExpanded) {
        if (isExpanded && unreadCount > 0) {
            // В реальном приложении здесь будет вызов колбэка для сброса счетчика
        }
    }

    if (isExpanded) {
        // РАЗВЕРНУТЫЙ ЧАТ
        Column(
            modifier = modifier
                .fillMaxWidth()
                .height(280.dp) // Примерно 1/3 экрана
                .clip(RoundedCornerShape(16.dp))
                .background(TileBgDark)
                .padding(8.dp)
        ) {
            // Шапка чата
            Row(
                modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Чат",
                    color = Accent,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    modifier = Modifier.weight(1f)
                )
                // Кнопка закрытия (свернуть)
                Box(
                    modifier = Modifier
                        .size(24.dp)
                        .clip(CircleShape)
                        .background(TileBg)
                        .clickable { isExpanded = false },
                    contentAlignment = Alignment.Center
                ) {
                    Text("✕", color = Color.White, fontSize = 12.sp)
                }
            }

            // Лента сообщений
            LazyColumn(
                state = listState,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                items(messages) { msg ->
                    if (msg.isSystem) {
                        Text(
                            text = msg.text,
                            color = Color.White.copy(alpha = 0.5f),
                            fontSize = 11.sp,
                            modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp),
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                    } else {
                        val bgColor = if (msg.isMyMessage) Accent.copy(alpha = 0.2f) else TileBg
                        val textColor = if (msg.isMyMessage) Accent else Color.White
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = if (msg.isMyMessage) Arrangement.End else Arrangement.Start
                        ) {
                            Column(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(bgColor)
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                if (!msg.isMyMessage) {
                                    Text(
                                        text = msg.senderName,
                                        color = GoldAccent,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                                Text(text = msg.text, color = textColor, fontSize = 13.sp)
                            }
                        }
                    }
                }
            }

            // Поле ввода
            Row(
                modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = inputText,
                    onValueChange = { if (it.length <= 200) inputText = it },
                    modifier = Modifier.weight(1f).height(48.dp),
                    placeholder = { Text("Сообщение...", fontSize = 12.sp, color = Color.White.copy(alpha = 0.4f)) },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedBorderColor = Accent,
                        unfocusedBorderColor = TileBg,
                        cursorColor = Accent
                    ),
                    singleLine = true,
                    shape = RoundedCornerShape(24.dp)
                )
                Spacer(Modifier.width(8.dp))
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(if (inputText.isNotBlank()) Accent else TileBg)
                        .clickable(enabled = inputText.isNotBlank()) {
                            onSendMessage(inputText.trim())
                            inputText = ""
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Text("➤", color = if (inputText.isNotBlank()) DarkBg else Color.White, fontSize = 18.sp)
                }
            }
        }
    } else {
        // СВЕРНУТЫЙ ЧАТ (Иконка)
        Box(
            modifier = modifier
                .size(48.dp)
                .clip(CircleShape)
                .background(TileBgDark)
                .clickable { isExpanded = true },
            contentAlignment = Alignment.Center
        ) {
            Text("💬", fontSize = 24.sp)
            
            // Красный бейджик с количеством непрочитанных
            if (unreadCount > 0) {
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(2.dp)
                        .size(18.dp)
                        .clip(CircleShape)
                        .background(Color.Red),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = if (unreadCount > 9) "9+" else unreadCount.toString(),
                        color = Color.White,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}
