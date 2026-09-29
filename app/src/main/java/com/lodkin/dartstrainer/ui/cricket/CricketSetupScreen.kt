package com.lodkin.dartstrainer.ui.cricket

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.lodkin.dartstrainer.data.cricket.CricketPlayer
import com.lodkin.dartstrainer.data.cricket.CricketType
import com.lodkin.dartstrainer.theme.Accent
import com.lodkin.dartstrainer.theme.DarkBg
import com.lodkin.dartstrainer.theme.GoldAccent
import com.lodkin.dartstrainer.theme.TileBg
import com.lodkin.dartstrainer.theme.TileBgDark

@Composable
fun CricketSetupScreen(
    playerName: String,
    onStartGame: (CricketType, List<CricketPlayer>) -> Unit,
    onBack: () -> Unit
) {
    var isPairGame by remember { mutableStateOf(false) }
    var cricketType by remember { mutableStateOf(CricketType.AMERICAN) }
    var playWithBot by remember { mutableStateOf(true) }
    var autoOkSeconds by remember { mutableStateOf(3) }

    // Игроки
    var player1Name by remember { mutableStateOf(playerName.ifBlank { "Игрок 1" }) }
    var player2Name by remember { mutableStateOf(if (playWithBot) "Бот" else "Игрок 2") }
    var player3Name by remember { mutableStateOf(if (playWithBot) "Бот" else "Игрок 3") }
    var player4Name by remember { mutableStateOf(if (playWithBot) "Бот" else "Игрок 4") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBg)
            .padding(16.dp)
    ) {
        // Верхняя панель
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .background(TileBgDark)
                    .clickable { onBack() }
                    .padding(horizontal = 16.dp, vertical = 10.dp)
            ) {
                Text("← Назад", color = Accent, fontSize = 15.sp)
            }
            Spacer(Modifier.width(12.dp))
            Text(
                "Крикет",
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
        }

        Spacer(Modifier.height(20.dp))

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .verticalScroll(rememberScrollState())
        ) {

            // ── Игроки ──
            SectionTitle("ИГРОКИ")
            Spacer(Modifier.height(8.dp))

            PlayerInput(player1Name, { player1Name = it }, "Вы")
            Spacer(Modifier.height(8.dp))

            if (isPairGame) {
                PlayerInput(player2Name, { player2Name = it }, "Ваш партнёр")
                Spacer(Modifier.height(8.dp))
                PlayerInput(player3Name, { player3Name = it }, "Соперник 1")
                Spacer(Modifier.height(8.dp))
                PlayerInput(player4Name, { player4Name = it }, "Соперник 2")
            } else {
                PlayerInput(player2Name, { player2Name = it }, "Соперник")
            }

            Spacer(Modifier.height(24.dp))

            // ── Игра ──
            SectionTitle("ИГРА")
            Spacer(Modifier.height(8.dp))

            // Парная игра
            SettingRow(
                label = "Парная игра (2 на 2)",
                checked = isPairGame,
                onCheckedChange = { isPairGame = it }
            )

            // Игра с ботом
            SettingRow(
                label = "Игра с ботом",
                checked = playWithBot,
                onCheckedChange = {
                    playWithBot = it
                    // Обновляем имена ботов
                    player2Name = if (it) "Бот" else "Игрок 2"
                    player3Name = if (it) "Бот" else "Игрок 3"
                    player4Name = if (it) "Бот" else "Игрок 4"
                }
            )

            Spacer(Modifier.height(16.dp))

            // Тип крикета
            Text(
                "Тип крикета",
                color = Accent,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            CricketTypeOption(
                label = "Американский (с очками)",
                description = "Классика. Закрывай сектора и набирай очки.",
                selected = cricketType == CricketType.AMERICAN,
                onClick = { cricketType = CricketType.AMERICAN }
            )
            Spacer(Modifier.height(8.dp))
            CricketTypeOption(
                label = "Без набора очков",
                description = "Кто быстрее закроет все сектора.",
                selected = cricketType == CricketType.NO_SCORE,
                onClick = { cricketType = CricketType.NO_SCORE }
            )

            Spacer(Modifier.height(24.dp))

            // ── Ввод ──
            SectionTitle("ВВОД")
            Spacer(Modifier.height(8.dp))

            Text(
                "АвтоОК (секунд):",
                color = Color.White,
                fontSize = 14.sp,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf(3, 5, 10, 0).forEach { sec ->
                    val label = if (sec == 0) "Выкл" else "$sec с"
                    val selected = autoOkSeconds == sec
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(12.dp))
                            .background(if (selected) Accent else TileBgDark)
                            .clickable { autoOkSeconds = sec }
                            .padding(vertical = 12.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            label,
                            color = if (selected) Color(0xFF121212) else Color.White,
                            fontSize = 14.sp,
                            fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium
                        )
                    }
                }
            }

            Spacer(Modifier.height(24.dp))
        }

        // Кнопка «Начать игру»
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(Accent)
                .clickable {
                    val players = buildList {
                        add(CricketPlayer(name = player1Name, isBot = false))
                        add(CricketPlayer(name = player2Name, isBot = playWithBot && !isPairGame || (isPairGame && playWithBot)))
                        if (isPairGame) {
                            add(CricketPlayer(name = player3Name, isBot = playWithBot))
                            add(CricketPlayer(name = player4Name, isBot = playWithBot))
                        }
                    }
                    onStartGame(cricketType, players)
                }
                .padding(vertical = 18.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                "НАЧАТЬ ИГРУ",
                color = Color(0xFF121212),
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
private fun SectionTitle(text: String) {
    Text(
        text = text,
        color = Accent,
        fontSize = 13.sp,
        fontWeight = FontWeight.Medium,
        letterSpacing = 3.sp
    )
}

@Composable
private fun PlayerInput(value: String, onChange: (String) -> Unit, hint: String) {
    OutlinedTextField(
        value = value,
        onValueChange = onChange,
        modifier = Modifier.fillMaxWidth(),
        singleLine = true,
        placeholder = { Text(hint, color = TileBg) },
        keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Words),
        colors = OutlinedTextFieldDefaults.colors(
            focusedTextColor = Color.White,
            unfocusedTextColor = Color.White,
            focusedBorderColor = Accent,
            unfocusedBorderColor = TileBg,
            cursorColor = Accent
        )
    )
}

@Composable
private fun SettingRow(
    label: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(TileBgDark)
            .clickable { onCheckedChange(!checked) }
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            label,
            color = Color.White,
            fontSize = 15.sp,
            modifier = Modifier.weight(1f)
        )
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = Accent,
                checkedTrackColor = Accent.copy(alpha = 0.5f),
                uncheckedThumbColor = Color.White,
                uncheckedTrackColor = TileBg
            )
        )
    }
}

@Composable
private fun CricketTypeOption(
    label: String,
    description: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    val bg = if (selected) Color(0xFF1A2A33) else TileBgDark
    val borderColor = if (selected) Accent else TileBg

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(bg)
            .clickable { onClick() }
            .padding(14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(20.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(borderColor),
            contentAlignment = Alignment.Center
        ) {
            if (selected) {
                Text("✓", color = Color(0xFF121212), fontSize = 14.sp, fontWeight = FontWeight.Bold)
            }
        }
        Spacer(Modifier.width(12.dp))
        Column {
            Text(
                label,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = if (selected) Accent else Color.White
            )
            Spacer(Modifier.height(2.dp))
            Text(
                description,
                fontSize = 12.sp,
                color = Color.White.copy(alpha = 0.8f)
            )
        }
    }
}
