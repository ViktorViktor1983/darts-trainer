package com.lodkin.dartstrainer.ui.cricket

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.lodkin.dartstrainer.data.cricket.CRICKET_BOTS
import com.lodkin.dartstrainer.data.cricket.CricketBot
import com.lodkin.dartstrainer.data.cricket.CricketPlayer
import com.lodkin.dartstrainer.data.cricket.CricketType
import com.lodkin.dartstrainer.data.cricket.PlayerNamesStorage
import com.lodkin.dartstrainer.theme.Accent
import com.lodkin.dartstrainer.theme.DarkBg
import com.lodkin.dartstrainer.theme.TileBg
import com.lodkin.dartstrainer.theme.TileBgDark

@Composable
fun CricketSetupScreen(
    playerName: String,
    onStartGame: (CricketType, List<CricketPlayer>) -> Unit,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val savedNames = remember { PlayerNamesStorage.getSavedNames(context).toMutableList() }

    var isPairGame by remember { mutableStateOf(false) }
    var cricketType by remember { mutableStateOf(CricketType.AMERICAN) }
    var playWithBot by remember { mutableStateOf(true) }
    var autoOkSeconds by remember { mutableStateOf(3) }

    var player1Name by remember {
        mutableStateOf(
            PlayerNamesStorage.getLastPlayer1(context).ifBlank {
                playerName.ifBlank { "Игрок 1" }
            }
        )
    }
    var player2Name by remember {
        mutableStateOf(PlayerNamesStorage.getLastPlayer2(context).ifBlank { "Игрок 2" })
    }
    var player3Name by remember { mutableStateOf("Игрок 3") }
    var player4Name by remember { mutableStateOf("Игрок 4") }

    var bot2 by remember { mutableStateOf(CRICKET_BOTS[2]) }
    var bot3 by remember { mutableStateOf(CRICKET_BOTS[2]) }
    var bot4 by remember { mutableStateOf(CRICKET_BOTS[2]) }

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

            // ── Игроки (заголовок по центру) ──
            Text(
                "ИГРОКИ",
                color = Accent,
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium,
                letterSpacing = 3.sp,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center
            )
            Spacer(Modifier.height(10.dp))

            // Первый ряд
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.Top
            ) {
                PlayerCell(
                    number = 1,
                    name = player1Name,
                    onNameChange = { player1Name = it },
                    savedNames = savedNames,
                    isBot = false,
                    bot = CRICKET_BOTS[2],
                    onBotChange = {},
                    context = context,
                    modifier = Modifier.weight(1f)
                )

                PlayerCell(
                    number = 2,
                    name = if (playWithBot) bot2.name else player2Name,
                    onNameChange = { player2Name = it },
                    savedNames = savedNames,
                    isBot = playWithBot,
                    bot = bot2,
                    onBotChange = { bot2 = it },
                    context = context,
                    modifier = Modifier.weight(1f)
                )
            }

            // Парная игра — второй ряд
            if (isPairGame) {
                Spacer(Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    PlayerCell(
                        number = 3,
                        name = if (playWithBot) bot3.name else player3Name,
                        onNameChange = { player3Name = it },
                        savedNames = savedNames,
                        isBot = playWithBot,
                        bot = bot3,
                        onBotChange = { bot3 = it },
                        context = context,
                        modifier = Modifier.weight(1f)
                    )
                    PlayerCell(
                        number = 4,
                        name = if (playWithBot) bot4.name else player4Name,
                        onNameChange = { player4Name = it },
                        savedNames = savedNames,
                        isBot = playWithBot,
                        bot = bot4,
                        onBotChange = { bot4 = it },
                        context = context,
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            Spacer(Modifier.height(16.dp))

            // ── Парная игра + Бот в одной строке ──
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                HalfSetting(
                    label = "Парная игра",
                    checked = isPairGame,
                    onCheckedChange = { isPairGame = it },
                    modifier = Modifier.weight(1f)
                )
                HalfSetting(
                    label = "Бот",
                    checked = playWithBot,
                    onCheckedChange = { playWithBot = it },
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(Modifier.height(24.dp))

            // ── Тип крикета ──
            Text(
                "ТИП КРИКЕТА",
                color = Accent,
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium,
                letterSpacing = 3.sp,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center
            )
            Spacer(Modifier.height(10.dp))

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

            // ── АвтоОК ──
            Text(
                "АВТООК",
                color = Accent,
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium,
                letterSpacing = 3.sp,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center
            )
            Spacer(Modifier.height(10.dp))
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
                        add(
                            CricketPlayer(
                                name = if (playWithBot) bot2.name else player2Name,
                                isBot = playWithBot,
                                botLevel = if (playWithBot) bot2.id else 0
                            )
                        )
                        if (isPairGame) {
                            add(
                                CricketPlayer(
                                    name = if (playWithBot) bot3.name else player3Name,
                                    isBot = playWithBot,
                                    botLevel = if (playWithBot) bot3.id else 0
                                )
                            )
                            add(
                                CricketPlayer(
                                    name = if (playWithBot) bot4.name else player4Name,
                                    isBot = playWithBot,
                                    botLevel = if (playWithBot) bot4.id else 0
                                )
                            )
                        }
                    }
                    PlayerNamesStorage.saveName(context, player1Name)
                    if (!playWithBot) {
                        PlayerNamesStorage.saveName(context, player2Name)
                        if (isPairGame) {
                            PlayerNamesStorage.saveName(context, player3Name)
                            PlayerNamesStorage.saveName(context, player4Name)
                        }
                    }
                    PlayerNamesStorage.setLastPlayer1(context, player1Name)
                    if (!playWithBot) PlayerNamesStorage.setLastPlayer2(context, player2Name)

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

// ─────────────────────────────────────────────
// Ячейка игрока (одна колонка) — БЕЗ тумблера бота
// ─────────────────────────────────────────────
@Composable
private fun PlayerCell(
    number: Int,
    name: String,
    onNameChange: (String) -> Unit,
    savedNames: List<String>,
    isBot: Boolean,
    bot: CricketBot,
    onBotChange: (CricketBot) -> Unit,
    context: android.content.Context,
    modifier: Modifier
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(TileBgDark)
            .padding(8.dp)
    ) {
        // Верхняя строка: номер
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(24.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(TileBg),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    "$number",
                    color = Accent,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        Spacer(Modifier.height(6.dp))

        // Поле имени или выбор бота
        if (isBot) {
            BotSelector(
                bot = bot,
                onBotChange = onBotChange,
                modifier = Modifier.fillMaxWidth()
            )
        } else {
            NameSelector(
                name = name,
                onNameChange = onNameChange,
                savedNames = savedNames,
                context = context,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
private fun NameSelector(
    name: String,
    onNameChange: (String) -> Unit,
    savedNames: List<String>,
    context: android.content.Context,
    modifier: Modifier
) {
    var expanded by remember { mutableStateOf(false) }
    var showAddDialog by remember { mutableStateOf(false) }
    var newNameInput by remember { mutableStateOf("") }

    Box(modifier = modifier) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(8.dp))
                .background(TileBg)
                .clickable { expanded = true }
                .padding(horizontal = 10.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                name,
                color = Color.White,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                maxLines = 1,
                modifier = Modifier.weight(1f)
            )
            Text("▼", color = Accent, fontSize = 10.sp)
        }

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            savedNames.forEach { saved ->
                DropdownMenuItem(
                    text = { Text(saved, color = Color.White) },
                    onClick = {
                        onNameChange(saved)
                        expanded = false
                    }
                )
            }
            DropdownMenuItem(
                text = { Text("+ Новое имя", color = Accent) },
                onClick = {
                    expanded = false
                    newNameInput = ""
                    showAddDialog = true
                }
            )
        }
    }

    if (showAddDialog) {
        AlertDialog(
            onDismissRequest = { showAddDialog = false },
            confirmButton = {
                TextButton(onClick = {
                    val trimmed = newNameInput.trim()
                    if (trimmed.isNotBlank()) {
                        onNameChange(trimmed)
                        PlayerNamesStorage.saveName(context, trimmed)
                    }
                    showAddDialog = false
                }) {
                    Text("Сохранить", color = Accent)
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddDialog = false }) {
                    Text("Отмена", color = Color.White.copy(alpha = 0.6f))
                }
            },
            title = { Text("Новое имя игрока", color = Color.White) },
            text = {
                OutlinedTextField(
                    value = newNameInput,
                    onValueChange = { newNameInput = it },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedBorderColor = Accent,
                        unfocusedBorderColor = TileBg,
                        cursorColor = Accent
                    )
                )
            }
        )
    }
}

@Composable
private fun BotSelector(
    bot: CricketBot,
    onBotChange: (CricketBot) -> Unit,
    modifier: Modifier
) {
    var expanded by remember { mutableStateOf(false) }

    Box(modifier = modifier) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(8.dp))
                .background(TileBg)
                .clickable { expanded = true }
                .padding(horizontal = 10.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    bot.name,
                    color = Color.White,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium,
                    maxLines = 1
                )
                Text(
                    "ср. ${bot.averageMin}–${bot.averageMax}",
                    color = Accent,
                    fontSize = 10.sp
                )
            }
            Text("▼", color = Accent, fontSize = 10.sp)
        }

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            CRICKET_BOTS.forEach { b ->
                DropdownMenuItem(
                    text = {
                        Column {
                            Text(
                                "${b.id}. ${b.name}",
                                color = Color.White,
                                fontWeight = FontWeight.Medium
                            )
                            Text(
                                "ср. ${b.averageMin}–${b.averageMax} · ${b.description}",
                                color = Accent,
                                fontSize = 11.sp
                            )
                        }
                    },
                    onClick = {
                        onBotChange(b)
                        expanded = false
                    }
                )
            }
        }
    }
}

// Половина строки: тумблер + подпись
@Composable
private fun HalfSetting(
    label: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier
) {
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(TileBgDark)
            .clickable { onCheckedChange(!checked) }
            .padding(horizontal = 12.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            label,
            color = Color.White,
            fontSize = 13.sp,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.weight(1f),
            maxLines = 2
        )
        Spacer(Modifier.width(6.dp))
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
