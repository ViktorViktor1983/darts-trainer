package com.lodkin.dartstrainer.ui.online

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.lodkin.dartstrainer.theme.Accent

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
    val keyboardController = LocalSoftwareKeyboardController.current
    val focusManager = LocalFocusManager.current

    if (isExpanded) {
        Column(
            modifier = modifier
                .fillMaxSize()
                .clip(RoundedCornerShape(16.dp))
                .background(Color(0xFFF5F5F5))
                .padding(6.dp)
        ) {
            // Шапка с крестиком
            Row(
                modifier = Modifier.fillMaxWidth().padding(bottom = 2.dp),
                horizontalArrangement = Arrangement.End
            ) {
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

            // Лента сообщений (перевернутая, чтобы прижиматься к низу)
            LazyColumn(
                state = listState,
                modifier = Modifier.weight(1f).fillMaxWidth(),
                reverseLayout = true, // ВАЖНО: новые сообщения снизу
                verticalArrangement = Arrangement.spacedBy(4.dp, Alignment.Bottom)
            ) {
                // Переворачиваем список для отображения
                items(messages.reversed()) { msg ->
                    if (msg.isSystem) {
                        Text(
                            text = msg.text,
                            color = Color.DarkGray,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                    } else {
                        val bgColor = if (msg.isMyMessage) Accent.copy(alpha = 0.3f) else Color.White
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = if (msg.isMyMessage) Arrangement.End else Arrangement.Start
                        ) {
                            Column(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(bgColor)
                                    .padding(horizontal = 8.dp, vertical = 6.dp)
                            ) {
                                if (!msg.isMyMessage) {
                                    Text(msg.senderName, color = Color(0xFFE65100), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }
                                Text(text = msg.text, color = Color.Black, fontSize = 14.sp)
                            }
                        }
                    }
                }
            }

            // Поле ввода (без рамок и лишних отступов)
            Row(
                modifier = Modifier.fillMaxWidth().padding(top = 6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(44.dp)
                        .clip(RoundedCornerShape(22.dp))
                        .background(Color.White)
                        .padding(horizontal = 12.dp),
                    contentAlignment = Alignment.CenterStart
                ) {
                    BasicTextField(
                        value = inputText,
                        onValueChange = { if (it.length <= 200) inputText = it },
                        textStyle = TextStyle(fontSize = 15.sp, color = Color.Black),
                        cursorBrush = SolidColor(Accent),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                        keyboardActions = KeyboardActions(onSend = {
                            if (inputText.isNotBlank()) {
                                onSendMessage(inputText.trim())
                                inputText = ""
                                keyboardController?.hide()
                                focusManager.clearFocus()
                            }
                        }),
                        decorationBox = { innerTextField ->
                            if (inputText.isEmpty()) {
                                Text("Сообщение...", fontSize = 15.sp, color = Color.Gray)
                            }
                            innerTextField()
                        }
                    )
                }
                
                Spacer(Modifier.width(6.dp))
                
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(if (inputText.isNotBlank()) Accent else Color.LightGray)
                        .clickable(enabled = inputText.isNotBlank()) {
                            onSendMessage(inputText.trim())
                            inputText = ""
                            keyboardController?.hide()
                            focusManager.clearFocus()
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Text("➤", color = if (inputText.isNotBlank()) Color.White else Color.Gray, fontSize = 18.sp)
                }
            }
        }
    }
}
