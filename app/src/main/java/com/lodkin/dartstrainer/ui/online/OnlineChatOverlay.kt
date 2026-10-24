package com.lodkin.dartstrainer.ui.online

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.lodkin.dartstrainer.theme.Accent
import com.lodkin.dartstrainer.theme.GoldAccent
import com.lodkin.dartstrainer.theme.TileBg
import com.lodkin.dartstrainer.theme.TileBgDark

data class ChatMessage(
    val id: String,
    val senderName: String,
    val text: String,
    val isMyMessage: Boolean = false,
    val isSystem: Boolean = false
)

@Composable
fun OnlineChatOverlay(
    modifier: Modifier = Modifier,
    isExpanded: Boolean,
    messages: List<ChatMessage>,
    unreadCount: Int,
    onSendMessage: (String) -> Unit,
    onClose: () -> Unit,
    onExpand: () -> Unit
) {
    if (!isExpanded) {
        // Компактный вид (когда чат свернут)
        Box(
            modifier = modifier
                .clip(RoundedCornerShape(12.dp))
                .background(TileBgDark)
                .clickable { onExpand() }
                .padding(10.dp),
            contentAlignment = Alignment.CenterStart
        ) {
            val lastMsg = messages.lastOrNull()
            if (lastMsg != null) {
                Column {
                    if (!lastMsg.isSystem) {
                        Text(
                            lastMsg.senderName,
                            color = GoldAccent,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(Modifier.height(2.dp))
                    }
                    Text(
                        lastMsg.text,
                        color = Color.White.copy(alpha = 0.85f),
                        fontSize = 13.sp,
                        maxLines = 2 // Ограничиваем двумя строками, чтобы не ломать высоту
                    )
                }
            } else {
                Text("Нет сообщений", color = Color.White.copy(alpha = 0.5f), fontSize = 13.sp)
            }
            
            if (unreadCount > 0) {
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .size(20.dp)
                        .clip(CircleShape)
                        .background(Color.Red),
                    contentAlignment = Alignment.Center
                ) {
                    Text("$unreadCount", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    } else {
        // Развернутый вид (на весь экран)
        val listState = rememberLazyListState()
        var inputText by remember { mutableStateOf("") }

        LaunchedEffect(messages.size) {
            if (messages.isNotEmpty()) {
                listState.animateScrollToItem(messages.size - 1)
            }
        }

        // Поднимаем чат выше, чтобы поле ввода не наезжало на системные кнопки
        Box(
            modifier = modifier
                .fillMaxSize()
                .padding(bottom = 48.dp) // Отступ снизу
                .clip(RoundedCornerShape(16.dp))
                .background(Color(0xFFF5F5F5))
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                // Шапка чата
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFFE0E0E0))
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Чат", color = Color.Black, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                    
                    // Большой и заметный крестик
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFCCCCCC))
                            .clickable { onClose() },
                        contentAlignment = Alignment.Center
                    ) {
                        Text("✕", color = Color.Black, fontSize = 24.sp, fontWeight = FontWeight.Bold)
                    }
                }

                // Лента сообщений
                LazyColumn(
                    modifier = Modifier.weight(1f).fillMaxWidth(),
                    state = listState,
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(messages) { msg ->
                        if (msg.isSystem) {
                            Text(
                                msg.text,
                                color = Color.Gray,
                                fontSize = 12.sp,
                                modifier = Modifier.fillMaxWidth(),
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                            )
                        } else {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = if (msg.isMyMessage) Arrangement.End else Arrangement.Start
                            ) {
                                Column(
                                    modifier = Modifier
                                        .widthIn(max = 280.dp)
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(if (msg.isMyMessage) Accent else Color.White)
                                        .padding(12.dp)
                                ) {
                                    if (!msg.isMyMessage) {
                                        Text(
                                            msg.senderName,
                                            color = Color(0xFFE65100),
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Spacer(Modifier.height(4.dp))
                                    }
                                    Text(
                                        msg.text,
                                        color = if (msg.isMyMessage) Color.White else Color.Black,
                                        fontSize = 15.sp
                                    )
                                }
                            }
                        }
                    }
                }

                // Поле ввода
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color.White)
                        .padding(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    BasicTextField(
                        value = inputText,
                        onValueChange = { inputText = it },
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(24.dp))
                            .background(Color(0xFFF0F0F0))
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                        textStyle = TextStyle(color = Color.Black, fontSize = 15.sp),
                        decorationBox = { innerTextField ->
                            if (inputText.isEmpty()) {
                                Text("Сообщение...", color = Color.Gray, fontSize = 15.sp)
                            }
                            innerTextField()
                        }
                    )
                    Spacer(Modifier.width(8.dp))
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape)
                            .background(if (inputText.isNotBlank()) Accent else Color(0xFFCCCCCC))
                            .clickable(enabled = inputText.isNotBlank()) {
                                onSendMessage(inputText)
                                inputText = ""
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Text("➤", color = if (inputText.isNotBlank()) Color.White else Color.Gray, fontSize = 20.sp)
                    }
                }
            }
        }
    }
}
