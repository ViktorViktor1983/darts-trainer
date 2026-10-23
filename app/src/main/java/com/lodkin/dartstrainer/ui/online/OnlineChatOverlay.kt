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
import com.lodkin.dartstrainer.theme.TileBg
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
    isExpanded: Boolean,
    messages: List<ChatMessage>,
    unreadCount: Int,
    onSendMessage: (String) -> Unit,
    onClose: () -> Unit
) {
    var inputText by remember { mutableStateOf("") }
    val listState = rememberLazyListState()
    val scope = rememberCoroutineScope()

    // Автоскролл вниз при новом сообщении
    LaunchedEffect(messages.size) {
        if (isExpanded && messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.size - 1)
        }
    }

    if (isExpanded) {
        // РАЗВЕРНУТЫЙ ЧАТ
        Column(
            modifier = modifier
                .fillMaxSize()
                .clip(RoundedCornerShape(16.dp))
                .background(Color(0xFFF5F5F5)) // Светлый фон
                .padding(8.dp)
        ) {
            // Шапка чата
            Row(
                modifier = Modifier.fillMaxWidth().padding(bottom = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Чат",
                    color = Color.Black,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    modifier = Modifier.weight(1f)
                )
                // Кнопка закрытия (свернуть)
                Box(
                    modifier = Modifier
                        .size(24.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFE0E0E0))
                        .clickable { onClose() },
                    contentAlignment = Alignment.Center
                ) {
                    Text("✕", color = Color.DarkGray, fontSize = 12.sp, fontWeight = FontWeight.Bold)
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
                            color = Color.Gray,
                            fontSize = 11.sp,
                            modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp),
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                    } else {
                        val bgColor = if (msg.isMyMessage) Accent.copy(alpha = 0.2f) else Color.White
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
                                        color = Color(0xFFE65100), // Темно-оранжевый для контраста
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                                Text(text = msg.text, color = Color.Black, fontSize = 13.sp)
                            }
                        }
                    }
                }
            }

            // Поле ввода
            Row(
                modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = inputText,
                    onValueChange = { if (it.length <= 200) inputText = it },
                    modifier = Modifier.weight(1f).height(48.dp),
                    placeholder = { Text("Сообщение...", fontSize = 12.sp, color = Color.Gray) },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.Black,
                        unfocusedTextColor = Color.Black,
                        focusedBorderColor = Accent,
                        unfocusedBorderColor = Color.LightGray,
                        cursorColor = Accent,
                        focusedContainerColor = Color.White,
                        unfocusedContainerColor = Color.White
                    ),
                    singleLine = true,
                    shape = RoundedCornerShape(24.dp)
                )
                Spacer(Modifier.width(8.dp))
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(if (inputText.isNotBlank()) Accent else Color.LightGray)
                        .clickable(enabled = inputText.isNotBlank()) {
                            onSendMessage(inputText.trim())
                            inputText = ""
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Text("➤", color = if (inputText.isNotBlank()) Color.White else Color.Gray, fontSize = 18.sp)
                }
            }
        }
    }
}
