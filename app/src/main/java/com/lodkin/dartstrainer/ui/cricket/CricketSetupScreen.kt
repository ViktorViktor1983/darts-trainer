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
import androidx.compose.material3.HorizontalDivider
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
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
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

// Один слот игрока
data class PlayerSlot(
    val isBot: Boolean,
    val name: String,
    val bot: CricketBot
)

@Composable
fun CricketSetupScreen(
    playerName: String,
    onStartGame: (CricketType, List<CricketPlayer>, Int, Int) -> Unit,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val savedNames = remember { PlayerNamesStorage.getSavedNames(context).toMutableList() }

    var isPairGame by remember { mutableStateOf(false) }
    var cricketType by remember { mutableStateOf(CricketType.AMERICAN) }
    var autoOkSeconds by remember { mutableStateOf(3) }

    // Настройки матча
    var legsPerSet by remember { mutableStateOf(1) }
    var setsPerMatch by remember { mutableStateOf(1) }

    // 4 слота игрока
    var slots by remember {
        mutableStateOf(
            listOf(
                PlayerSlot(
                    isBot = false,
                    name = PlayerNamesStorage.getLastPlayer1(context).ifBlank {
                        playerName.ifBlank { "Игрок 1" }
                    },
                    bot = CRICKET_BOTS[2]
                ),
                PlayerSlot(
                    isBot = true,
                    name = PlayerNamesStorage.getLastPlayer2(context).ifBlank { "Игрок 2" },
                    bot = CRICKET_BOTS[2]
                ),
                PlayerSlot(
                    isBot = true,
                    name = "Игрок 3",
                    bot = CRICKET_BOTS[2]
                ),
                PlayerSlot(
                    isBot = true,
                    name = "Игрок 4",
                    bot = CRICKET_BOTS[2]
                )
            )
        )
    }

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

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.Top
            ) {
                PlayerCell(
                    number = 1,
                    slot = slots[0],
                    savedNames = savedNames,
                    context = context,
                    onSlotChange = { newSlot ->
                        slots = slots.toMutableList().also { it[0] = newSlot }
                    },
                    modifier = Modifier.weight(1f)
                )
                PlayerCell(
                    number = 2,
                    slot = slots[1],
                    savedNames = savedNames,
                    context = context,
                    onSlotChange = { newSlot ->
                        slots = slots.toMutableList().also { it[1] = newSlot }
                    },
                    modifier = Modifier.weight(1f)
                )
            }

            if (isPairGame) {
                Spacer(Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    PlayerCell(
                        number = 3,
                        slot = slots[2],
                        savedNames = savedNames,
                        context = context,
                        onSlotChange = { newSlot ->
                            slots = slots.toMutableList().also { it[2] = newSlot }
                        },
                        modifier = Modifier.weight(1f)
                    )
                    PlayerCell(
                        number = 4,
                        slot = slots[3],
                        savedNames = savedNames,
                        context = context,
                        onSlotChange = { newSlot ->
                            slots = slots.toMutableList().also { it[3] = newSlot }
                        },
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            Spacer(Modifier.height(16.dp))

            // ── Парная игра ──
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(TileBgDark)
                    .clickable { isPairGame = !isPairGame }
                    .padding(horizontal = 14.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "Парная игра (2 на 2)",
                    color = Color.White,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.weight(1f)
                )
                Switch(
                    checked = isPairGame,
                    onCheckedChange = { isPairGame = it },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = Accent,
                        checkedTrackColor = Accent.copy(alpha = 0.5f),
                        uncheckedThumbColor = Color.White,
                        uncheckedTrackColor = TileBg
                    )
                )
            }

            Spacer(Modifier.height(24.dp))

            // ── Тип крикета — в одну строку ──
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

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                CricketTypeHalf(
                    label = "С очками",
                    selected = cricketType == CricketType.AMERICAN,
                    onClick = { cricketType = CricketType.AMERICAN },
                    modifier = Modifier.weight(1f)
                )
                CricketTypeHalf(
                    label = "Без очков",
                    selected = cricketType == CricketType.NO_SCORE,
                    onClick = { cricketType = CricketType.NO_SCORE },
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(Modifier.height(24.dp))

            // ── Сеты / Леги в одну строку ──
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                CompactNumberPicker(
                    label = "СЕТЫ",
                    value = setsPerMatch,
                    onValueChange = { setsPerMatch = it },
                    modifier = Modifier.weight(1f)
                )
                CompactNumberPicker(
                    label = "ЛЕГИ",
                    value = legsPerSet,
                    onValueChange = { legsPerSet = it },
                    modifier = Modifier.weight(1f)
                )
            }

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
                    val activeSlots = if (isPairGame) slots else slots.take(2)
                    val players = activeSlots.map { slot ->
                        CricketPlayer(
                            name = if (slot.isBot) slot.bot.name else slot.name,
                            isBot = slot.isBot,
                            botLevel = if (slot.isBot) slot.bot.id else 0
                        )
                    }
                    activeSlots.forEach { slot ->
                        if (!slot.isBot) {
                            PlayerNamesStorage.saveName(context, slot.name)
                        }
                    }
                    if (!activeSlots[0].isBot) {
                        PlayerNamesStorage.setLastPlayer1(context, activeSlots[0].name)
                    }
                    if (activeSlots.size > 1 && !activeSlots[1].isBot) {
                        PlayerNamesStorage.setLastPlayer2(context, activeSlots[1].name)
                    }

                    onStartGame(cricketType, players, legsPerSet, setsPerMatch)
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
// Компактный выбор числа (Сеты / Леги) с выпадающим списком 1..10
// ─────────────────────────────────────────────
@Composable
private fun CompactNumberPicker(
    label: String,
    value: Int,
    onValueChange: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }

    Box(modifier = modifier) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(TileBgDark)
                .clickable { expanded = true }
                .padding(horizontal = 14.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                label,
                color = Color.White,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.weight(1f)
            )
            Text(
                "$value",
                color = Accent,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(Modifier.width(6.dp))
            Text("▼", color = Accent, fontSize = 10.sp)
        }

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier.background(DarkBg)
        ) {
            (1..10).forEach { n ->
                DropdownMenuItem(
                    text = {
                        Text(
                            "$n",
                            color = if (n == value) Accent else Color.White,
                            fontWeight = if (n == value) FontWeight.Bold else FontWeight.Normal
                        )
                    },
                    onClick = {
                        onValueChange(n)
                        expanded = false
                    }
                )
            }
        }
    }
}

// ─────────────────────────────────────────────
// Ячейка игрока — с единым списком (Игрок / Бот)
// ─────────────────────────────────────────────
@Composable
private fun PlayerCell(
    number: Int,
    slot: PlayerSlot,
    savedNames: List<String>,
    context: android.content.Context,
    onSlotChange: (PlayerSlot) -> Unit,
    modifier: Modifier
) {
    var expanded by remember { mutableStateOf(false) }
    var showAddDialog by remember { mutableStateOf(false) }
    var newNameInput by remember { mutableStateOf("") }
    val menuScrollState = rememberScrollState()

    Column(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(TileBgDark)
            .padding(8.dp)
    ) {
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

        Box(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(TileBg)
                    .clickable { expanded = true }
                    .padding(horizontal = 10.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    if (slot.isBot) slot.bot.name else slot.name,
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
                onDismissRequest = { expanded = false },
                scrollState = menuScrollState,
                modifier = Modifier
                    .heightIn(max = 480.dp)
                    .background(DarkBg)
                    .drawWithContent {
                        drawContent()
                        val gradientHeight = 56.dp.toPx()
                        drawRect(
                            brush = Brush.verticalGradient(
                                colors = listOf(
                                    Color.Transparent,
                                    DarkBg.copy(alpha = 0.9f),
                                    DarkBg
                                ),
                                startY = size.height - gradientHeight,
                                endY = size.height
                            )
                        )
                        if (menuScrollState.maxValue > 0) {
                            val viewportPx = menuScrollState.viewportSize.toFloat()
                            val maxScrollPx = menuScrollState.maxValue.toFloat()
                            val contentPx = viewportPx + maxScrollPx
                            if (contentPx > 0f) {
                                val thumbFraction = (viewportPx / contentPx).coerceIn(0.05f, 1f)
                                val thumbHeightPx = (size.height * thumbFraction).coerceAtLeast(56f)
                                val scrollFraction = if (maxScrollPx > 0f) {
                                    menuScrollState.value.toFloat() / maxScrollPx
                                } else 0f
                                val thumbTopPx = (size.height - thumbHeightPx) * scrollFraction
                                val thumbWidthPx = 4.dp.toPx()
                                val thumbRightPx = 3.dp.toPx()
                                drawRoundRect(
                                    color = Accent.copy(alpha = 0.8f),
                                    topLeft = Offset(
                                        x = size.width - thumbWidthPx - thumbRightPx,
                                        y = thumbTopPx
                                    ),
                                    size = Size(thumbWidthPx, thumbHeightPx),
                                    cornerRadius = CornerRadius(thumbWidthPx / 2f)
                                )
                            }
                        }
                    }
            ) {
                Text(
                    "  ИГРОК",
                    color = Accent,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 2.sp,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                )

                savedNames.forEach { saved ->
                    DropdownMenuItem(
                        text = { Text(saved, color = Color.White) },
                        onClick = {
                            onSlotChange(slot.copy(isBot = false, name = saved))
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

                HorizontalDivider(color = TileBg)

                Text(
                    "  БОТЫ",
                    color = Accent,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 2.sp,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                )

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
                                    "ср. ${b.averageMin}–${b.averageMax}",
                                    color = Accent,
                                    fontSize = 11.sp
                                )
                            }
                        },
                        onClick = {
                            onSlotChange(slot.copy(isBot = true, bot = b))
                            expanded = false
                        }
                    )
                }
            }
        }
    }

    if (showAddDialog) {
        AlertDialog(
            onDismissRequest = { showAddDialog = false },
            confirmButton = {
                TextButton(onClick = {
                    val trimmed = newNameInput.trim()
                    if (trimmed.isNotBlank()) {
                        onSlotChange(slot.copy(isBot = false, name = trimmed))
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

// ─────────────────────────────────────────────
// Половина строки для типа крикета
// ─────────────────────────────────────────────
@Composable
private fun CricketTypeHalf(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier
) {
    val bg = if (selected) Accent else TileBgDark
    val fg = if (selected) Color(0xFF121212) else Color.White

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(bg)
            .clickable { onClick() }
            .padding(vertical = 16.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            label,
            color = fg,
            fontSize = 14.sp,
            fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
            textAlign = TextAlign.Center
        )
    }
}
