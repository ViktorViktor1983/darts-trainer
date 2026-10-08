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
import com.lodkin.dartstrainer.data.cricket.CricketDatabase
import com.lodkin.dartstrainer.data.cricket.CricketPlayer
import com.lodkin.dartstrainer.data.cricket.CricketRepository
import com.lodkin.dartstrainer.data.cricket.CricketSettingsStorage
import com.lodkin.dartstrainer.data.cricket.CricketType
import com.lodkin.dartstrainer.data.cricket.PlayerNamesStorage
import com.lodkin.dartstrainer.theme.Accent
import com.lodkin.dartstrainer.theme.DarkBg
import com.lodkin.dartstrainer.theme.GoldAccent
import com.lodkin.dartstrainer.theme.TileBg
import com.lodkin.dartstrainer.theme.TileBgDark
import kotlin.random.Random
import java.util.Locale

private val PaleRed = Color(0xFFE57373)
private val PaleRedText = Color(0xFF3E1010)

data class PlayerSlot(
    val isBot: Boolean,
    val name: String,
    val bot: CricketBot
)

@Composable
fun CricketSetupScreen(
    playerName: String,
    onStartGame: (CricketType, List<CricketPlayer>, Int, Int, Boolean, Int, Int) -> Unit,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val savedNames = remember { PlayerNamesStorage.getSavedNames(context).toMutableList() }

    var isPairGame by remember { mutableStateOf(CricketSettingsStorage.isPairGame(context)) }
    var cricketType by remember { mutableStateOf(CricketSettingsStorage.getCricketType(context)) }
    var autoOkSeconds by remember { mutableStateOf(CricketSettingsStorage.getAutoOk(context)) }
    var legsPerSet by remember { mutableStateOf(CricketSettingsStorage.getLegsPerSet(context)) }
    var setsPerMatch by remember { mutableStateOf(CricketSettingsStorage.getSetsPerMatch(context)) }

    var showBullInputDialog by remember { mutableStateOf(false) }
    var showBullResultDialog by remember { mutableStateOf(false) }
    var humanBullResult by remember { mutableStateOf(-1) }
    var botBullResult by remember { mutableStateOf(-1) }

    var slots by remember {
        mutableStateOf(loadSlots(context, playerName))
    }

    // ── Мой средний MPR из статистики ──
    val cricketRepo = remember { CricketRepository(CricketDatabase.get(context).cricketDao()) }
    var myAvgMpr by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(playerName) {
        val games = cricketRepo.getAllGames()
        var sum = 0.0
        var count = 0
        for (g in games) {
            val bots = parseStringListC(g.playerIsBot)
            val names = parseStringListC(g.playerNames)
            val mprList = parseDoubleListC(g.mpr)
            for (i in bots.indices) {
                val name = names.getOrNull(i) ?: ""
                if (isOwnerC(bots[i], name, playerName)) {
                    mprList.getOrNull(i)?.let { sum += it; count++ }
                    break
                }
            }
        }
        myAvgMpr = if (count > 0) "%.2f".format(Locale.US, sum / count) else null
    }

    val activeSlots = if (isPairGame) slots else slots.take(2)
    val allBots = activeSlots.all { it.isBot }
    val allHumans = activeSlots.all { !it.isBot }
    val hasHuman = activeSlots.any { !it.isBot }

    val showMyMprInSlot1 = !slots[0].isBot && slots[0].name.equals(playerName, ignoreCase = true)

    LaunchedEffect(isPairGame, cricketType, autoOkSeconds, legsPerSet, setsPerMatch) {
        CricketSettingsStorage.setPairGame(context, isPairGame)
        CricketSettingsStorage.setCricketType(context, cricketType)
        CricketSettingsStorage.setAutoOk(context, autoOkSeconds)
        CricketSettingsStorage.setLegsPerSet(context, legsPerSet)
        CricketSettingsStorage.setSetsPerMatch(context, setsPerMatch)
    }

    LaunchedEffect(slots) {
        CricketSettingsStorage.setSlotIsBot(context, slots.map { it.isBot })
        CricketSettingsStorage.setSlotNames(context, slots.map { it.name })
        CricketSettingsStorage.setSlotBotIds(context, slots.map { it.bot.id })
    }

    fun startGame(startingTeam: Int) {
        val players: List<CricketPlayer> = if (isPairGame) {
            listOf(
                CricketPlayer(name = if (slots[0].isBot) slots[0].bot.name else slots[0].name,
                    isBot = slots[0].isBot, botLevel = if (slots[0].isBot) slots[0].bot.id else 0, teamIndex = 0),
                CricketPlayer(name = if (slots[1].isBot) slots[1].bot.name else slots[1].name,
                    isBot = slots[1].isBot, botLevel = if (slots[1].isBot) slots[1].bot.id else 0, teamIndex = 1),
                CricketPlayer(name = if (slots[2].isBot) slots[2].bot.name else slots[2].name,
                    isBot = slots[2].isBot, botLevel = if (slots[2].isBot) slots[2].bot.id else 0, teamIndex = 0),
                CricketPlayer(name = if (slots[3].isBot) slots[3].bot.name else slots[3].name,
                    isBot = slots[3].isBot, botLevel = if (slots[3].isBot) slots[3].bot.id else 0, teamIndex = 1)
            ).also {
                slots.forEach { s -> if (!s.isBot) PlayerNamesStorage.saveName(context, s.name) }
            }
        } else {
            val a = slots[0]; val b = slots[1]
            listOf(
                CricketPlayer(name = if (a.isBot) a.bot.name else a.name,
                    isBot = a.isBot, botLevel = if (a.isBot) a.bot.id else 0, teamIndex = 0),
                CricketPlayer(name = if (b.isBot) b.bot.name else b.name,
                    isBot = b.isBot, botLevel = if (b.isBot) b.bot.id else 0, teamIndex = 1)
            ).also {
                if (!a.isBot) PlayerNamesStorage.saveName(context, a.name)
                if (!b.isBot) PlayerNamesStorage.saveName(context, b.name)
                if (!a.isBot) PlayerNamesStorage.setLastPlayer1(context, a.name)
                if (!b.isBot) PlayerNamesStorage.setLastPlayer2(context, b.name)
            }
        }
        onStartGame(cricketType, players, legsPerSet, setsPerMatch, isPairGame, startingTeam, autoOkSeconds)
    }

    LaunchedEffect(isPairGame) {
        humanBullResult = -1
        botBullResult = -1
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBg)
            .padding(16.dp)
    ) {
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
            Text("Крикет", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = Color.White)
        }

        Spacer(Modifier.height(20.dp))

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .verticalScroll(rememberScrollState())
        ) {
            Text(
                if (isPairGame) "КОМАНДЫ (2 НА 2)" else "ИГРОКИ",
                color = Accent, fontSize = 13.sp, fontWeight = FontWeight.Medium,
                letterSpacing = 3.sp, modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.Center
            )
            Spacer(Modifier.height(10.dp))

            if (isPairGame) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    TeamColumn(
                        teamLabel = "КОМАНДА A",
                        slot1 = slots[0],
                        slot2 = slots[2],
                        slot1Label = "1",
                        slot2Label = "3",
                        savedNames = savedNames,
                        context = context,
                        onSlot1Change = { newSlot -> slots = slots.toMutableList().also { it[0] = newSlot } },
                        onSlot2Change = { newSlot -> slots = slots.toMutableList().also { it[2] = newSlot } },
                        myAvgMprForSlot1 = if (showMyMprInSlot1) myAvgMpr else null,
                        modifier = Modifier.weight(1f)
                    )
                    TeamColumn(
                        teamLabel = "КОМАНДА B",
                        slot1 = slots[1],
                        slot2 = slots[3],
                        slot1Label = "2",
                        slot2Label = "4",
                        savedNames = savedNames,
                        context = context,
                        onSlot1Change = { newSlot -> slots = slots.toMutableList().also { it[1] = newSlot } },
                        onSlot2Change = { newSlot -> slots = slots.toMutableList().also { it[3] = newSlot } },
                        myAvgMprForSlot1 = null,
                        modifier = Modifier.weight(1f)
                    )
                }
            } else {
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
                        onSlotChange = { newSlot -> slots = slots.toMutableList().also { it[0] = newSlot } },
                        myAvgMpr = if (showMyMprInSlot1) myAvgMpr else null,
                        modifier = Modifier.weight(1f)
                    )
                    PlayerCell(
                        number = 2,
                        slot = slots[1],
                        savedNames = savedNames,
                        context = context,
                        onSlotChange = { newSlot -> slots = slots.toMutableList().also { it[1] = newSlot } },
                        myAvgMpr = null,
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            Spacer(Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(TileBgDark)
                    .clickable { isPairGame = !isPairGame }
                    .padding(horizontal = 14.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Парная игра (2 на 2)", color = Color.White, fontSize = 14.sp,
                    fontWeight = FontWeight.Medium, modifier = Modifier.weight(1f))
                Switch(checked = isPairGame, onCheckedChange = { isPairGame = it },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = Accent, checkedTrackColor = Accent.copy(alpha = 0.5f),
                        uncheckedThumbColor = Color.White, uncheckedTrackColor = TileBg))
            }

            Spacer(Modifier.height(24.dp))

            Text("ТИП КРИКЕТА", color = Accent, fontSize = 13.sp, fontWeight = FontWeight.Medium,
                letterSpacing = 3.sp, modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.Center)
            Spacer(Modifier.height(10.dp))

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                CricketTypeHalf("Американский", cricketType == CricketType.AMERICAN,
                    { cricketType = CricketType.AMERICAN }, Modifier.weight(1f))
                CricketTypeHalf("Без набора", cricketType == CricketType.NO_SCORE,
                    { cricketType = CricketType.NO_SCORE }, Modifier.weight(1f))
            }

            Spacer(Modifier.height(24.dp))

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                CompactNumberPicker("СЕТЫ", setsPerMatch, { setsPerMatch = it }, Modifier.weight(1f))
                CompactNumberPicker("ЛЕГИ", legsPerSet, { legsPerSet = it }, Modifier.weight(1f))
            }

            Spacer(Modifier.height(24.dp))

            Text("АВТООК", color = Accent, fontSize = 13.sp, fontWeight = FontWeight.Medium,
                letterSpacing = 3.sp, modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.Center)
            Spacer(Modifier.height(10.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf(3, 5, 10, 0).forEach { sec ->
                    val label = if (sec == 0) "Выкл" else "$sec с"
                    val selected = autoOkSeconds == sec
                    Box(
                        modifier = Modifier.weight(1f)
                            .clip(RoundedCornerShape(12.dp))
                            .background(if (selected) Accent else TileBgDark)
                            .clickable { autoOkSeconds = sec }
                            .padding(vertical = 12.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(label, color = if (selected) Color(0xFF121212) else Color.White,
                            fontSize = 14.sp, fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium)
                    }
                }
            }

            if (hasHuman && !allHumans) {
                Spacer(Modifier.height(24.dp))
                Text("КТО НАЧИНАЕТ", color = Accent, fontSize = 13.sp, fontWeight = FontWeight.Medium,
                    letterSpacing = 3.sp, modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.Center)
                Spacer(Modifier.height(10.dp))

                Box(
                    modifier = Modifier.fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(PaleRed)
                        .clickable { showBullInputDialog = true }
                        .padding(vertical = 16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text("Разыграть Bull", color = PaleRedText, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                }
            } else if (allHumans) {
                Spacer(Modifier.height(24.dp))
                Text("КТО НАЧИНАЕТ", color = Accent, fontSize = 13.sp, fontWeight = FontWeight.Medium,
                    letterSpacing = 3.sp, modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.Center)
                Spacer(Modifier.height(10.dp))

                val labelA = if (isPairGame) "КОМАНДА A" else "Игрок 1"
                val labelB = if (isPairGame) "КОМАНДА B" else "Игрок 2"
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Box(
                        modifier = Modifier.weight(1f)
                            .clip(RoundedCornerShape(12.dp))
                            .background(PaleRed)
                            .clickable { startGame(0) }
                            .padding(vertical = 16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(labelA, color = PaleRedText, fontSize = 15.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
                    }
                    Box(
                        modifier = Modifier.weight(1f)
                            .clip(RoundedCornerShape(12.dp))
                            .background(PaleRed)
                            .clickable { startGame(1) }
                            .padding(vertical = 16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(labelB, color = PaleRedText, fontSize = 15.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
                    }
                }
            }

            Spacer(Modifier.height(24.dp))
        }

        if (allBots) {
            Box(
                modifier = Modifier.fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(Accent)
                    .clickable { startGame(0) }
                    .padding(vertical = 18.dp),
                contentAlignment = Alignment.Center
            ) {
                Text("НАЧАТЬ ИГРУ", color = Color(0xFF121212), fontSize = 16.sp, fontWeight = FontWeight.Bold)
            }
        }
    }

    if (showBullInputDialog) {
        AlertDialog(
            onDismissRequest = { showBullInputDialog = false },
            confirmButton = {},
            dismissButton = {},
            title = { Text("Укажите Ваш результат броска в Bull", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    BullResultButton("КРАСНЫЙ (50)", Color(0xFFD32F2F)) {
                        humanBullResult = 2
                        botBullResult = generateBotBullResult(activeSlots.first { it.isBot }.bot)
                        showBullInputDialog = false
                        showBullResultDialog = true
                    }
                    Spacer(Modifier.height(10.dp))
                    BullResultButton("ЗЕЛЁНЫЙ (25)", Color(0xFF4CAF50)) {
                        humanBullResult = 1
                        botBullResult = generateBotBullResult(activeSlots.first { it.isBot }.bot)
                        showBullInputDialog = false
                        showBullResultDialog = true
                    }
                    Spacer(Modifier.height(10.dp))
                    BullResultButton("МИМО (0)", Color(0xFF78909C)) {
                        humanBullResult = 0
                        botBullResult = generateBotBullResult(activeSlots.first { it.isBot }.bot)
                        showBullInputDialog = false
                        showBullResultDialog = true
                    }
                    Spacer(Modifier.height(14.dp))
                    TextButton(onClick = { showBullInputDialog = false }, modifier = Modifier.fillMaxWidth()) {
                        Text("ОТМЕНИТЬ РОЗЫГРЫШ", color = Color.White.copy(alpha = 0.6f), fontSize = 13.sp)
                    }
                }
            }
        )
    }

    if (showBullResultDialog) {
        val humanSlotIndex = activeSlots.indexOfFirst { !it.isBot }
        val humanTeam = if (humanSlotIndex < 0) 0 else {
            if (isPairGame) {
                if (humanSlotIndex == 0 || humanSlotIndex == 2) 0 else 1
            } else {
                humanSlotIndex
            }
        }
        val humanName = activeSlots.first { !it.isBot }.let { it.name }
        val botName = activeSlots.first { it.isBot }.bot.name

        val humanLabel = when (humanBullResult) { 2 -> "Красный (50)"; 1 -> "Зелёный (25)"; else -> "Мимо (0)" }
        val botLabel = when (botBullResult) { 2 -> "Красный (50)"; 1 -> "Зелёный (25)"; else -> "Мимо (0)" }
        val humanColor = when (humanBullResult) { 2 -> Color(0xFFD32F2F); 1 -> Color(0xFF4CAF50); else -> Color(0xFF78909C) }
        val botColor = when (botBullResult) { 2 -> Color(0xFFD32F2F); 1 -> Color(0xFF4CAF50); else -> Color(0xFF78909C) }

        val draw = humanBullResult == botBullResult
        val humanWins = humanBullResult > botBullResult

        AlertDialog(
            onDismissRequest = { },
            confirmButton = {
                if (draw) {
                    TextButton(onClick = {
                        humanBullResult = -1
                        botBullResult = -1
                        showBullResultDialog = false
                        showBullInputDialog = true
                    }) { Text("Перебросить", color = Accent) }
                } else {
                    TextButton(onClick = {
                        val winnerTeam = if (humanWins) humanTeam else 1 - humanTeam
                        showBullResultDialog = false
                        startGame(winnerTeam)
                    }) { Text("Начать игру", color = Accent) }
                }
            },
            title = { Text(if (draw) "Ничья — переброс" else "Результат розыгрыша", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    Text("Вы попали: ", color = Color.White, fontSize = 14.sp)
                    Text(humanLabel, color = humanColor, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.height(8.dp))
                    Text("Бот попал: ", color = Color.White, fontSize = 14.sp)
                    Text(botLabel, color = botColor, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                    if (!draw) {
                        Spacer(Modifier.height(14.dp))
                        Text(
                            "Игру начинает ${if (humanWins) humanName else botName}",
                            color = GoldAccent, fontSize = 18.sp, fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        )
    }
}

// ─────────────────────────────────────────────
// Загрузка слотов
// ─────────────────────────────────────────────
private fun loadSlots(context: android.content.Context, playerName: String): List<PlayerSlot> {
    val bots = CricketSettingsStorage.getSlotIsBot(context)
    val names = CricketSettingsStorage.getSlotNames(context)
    val botIds = CricketSettingsStorage.getSlotBotIds(context)

    val ownerName = when {
        playerName.isNotBlank() -> playerName
        PlayerNamesStorage.getLastPlayer1(context).isNotBlank() -> PlayerNamesStorage.getLastPlayer1(context)
        else -> "Игрок 1"
    }

    val defaults = listOf(
        PlayerSlot(false, ownerName, CRICKET_BOTS[2]),
        PlayerSlot(true, PlayerNamesStorage.getLastPlayer2(context).ifBlank { "Игрок 2" }, CRICKET_BOTS[2]),
        PlayerSlot(true, "Игрок 3", CRICKET_BOTS[2]),
        PlayerSlot(true, "Игрок 4", CRICKET_BOTS[2])
    )

    if (bots.size < 4 || names.size < 4 || botIds.size < 4) return defaults

    return List(4) { i ->
        val botIdx = (botIds[i] - 1).coerceIn(0, CRICKET_BOTS.lastIndex)
        val bot = CRICKET_BOTS[botIdx]
        val isBot = bots[i]
        val name = when {
            isBot -> bot.name
            i == 0 && playerName.isNotBlank() -> playerName
            else -> names[i].ifBlank { if (i == 0) "Игрок 1" else "Игрок ${i + 1}" }
        }
        PlayerSlot(isBot, name, bot)
    }
}

private fun generateBotBullResult(bot: CricketBot): Int {
    val lvl = bot.id.coerceIn(1, 16)
    val pRed = 0.05 + (lvl - 1) * 0.027
    val pGreen = 0.25 + (lvl - 1) * 0.013
    val r = Random.nextDouble()
    return when {
        r < pRed -> 2
        r < pRed + pGreen -> 1
        else -> 0
    }
}

@Composable
private fun BullResultButton(label: String, color: Color, onClick: () -> Unit) {
    Box(
        modifier = Modifier.fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(color)
            .clickable { onClick() }
            .padding(vertical = 14.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(label, color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun CompactNumberPicker(label: String, value: Int, onValueChange: (Int) -> Unit, modifier: Modifier = Modifier) {
    var expanded by remember { mutableStateOf(false) }
    Box(modifier = modifier) {
        Row(
            modifier = Modifier.fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(TileBgDark)
                .clickable { expanded = true }
                .padding(horizontal = 14.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(label, color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Medium, modifier = Modifier.weight(1f))
            Text("$value", color = Accent, fontSize = 18.sp, fontWeight = FontWeight.Bold)
            Spacer(Modifier.width(6.dp))
            Text("▼", color = Accent, fontSize = 10.sp)
        }
        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }, modifier = Modifier.background(DarkBg)) {
            (1..10).forEach { n ->
                DropdownMenuItem(
                    text = { Text("$n", color = if (n == value) Accent else Color.White, fontWeight = if (n == value) FontWeight.Bold else FontWeight.Normal) },
                    onClick = { onValueChange(n); expanded = false }
                )
            }
        }
    }
}

@Composable
private fun TeamColumn(
    teamLabel: String, slot1: PlayerSlot, slot2: PlayerSlot, slot1Label: String, slot2Label: String,
    savedNames: List<String>, context: android.content.Context,
    onSlot1Change: (PlayerSlot) -> Unit, onSlot2Change: (PlayerSlot) -> Unit,
    myAvgMprForSlot1: String? = null,
    modifier: Modifier
) {
    Column(modifier = modifier.clip(RoundedCornerShape(12.dp)).background(TileBgDark).padding(8.dp)) {
        Text(teamLabel, color = Accent, fontSize = 11.sp, fontWeight = FontWeight.Bold,
            letterSpacing = 2.sp, modifier = Modifier.fillMaxWidth().padding(bottom = 6.dp), textAlign = TextAlign.Center)
        PlayerInnerSlot(slot1Label, slot1, savedNames, context, onSlot1Change, myAvgMprForSlot1)
        Spacer(Modifier.height(6.dp))
        PlayerInnerSlot(slot2Label, slot2, savedNames, context, onSlot2Change, null)
    }
}

@Composable
private fun PlayerInnerSlot(
    numberLabel: String, slot: PlayerSlot, savedNames: List<String>,
    context: android.content.Context, onSlotChange: (PlayerSlot) -> Unit,
    myAvgMpr: String? = null
) {
    var expanded by remember { mutableStateOf(false) }
    var showAddDialog by remember { mutableStateOf(false) }
    var newNameInput by remember { mutableStateOf("") }
    val menuScrollState = rememberScrollState()

    Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        // Плашка с номером
        Box(modifier = Modifier.size(22.dp).clip(RoundedCornerShape(11.dp)).background(TileBg),
            contentAlignment = Alignment.Center) {
            Text(numberLabel, color = Accent, fontSize = 11.sp, fontWeight = FontWeight.Bold)
        }
        // Отдельная плашка со средним MPR (если есть)
        if (myAvgMpr != null) {
            Spacer(Modifier.width(4.dp))
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(11.dp))
                    .background(TileBg)
                    .padding(horizontal = 6.dp, vertical = 3.dp)
            ) {
                Text("ср. $myAvgMpr", color = GoldAccent, fontSize = 10.sp, fontWeight = FontWeight.Bold)
            }
        }
        Spacer(Modifier.width(6.dp))
        Box(modifier = Modifier.weight(1f)) {
            Row(
                modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(8.dp)).background(TileBg)
                    .clickable { expanded = true }.padding(horizontal = 10.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(if (slot.isBot) slot.bot.name else slot.name, color = Color.White, fontSize = 13.sp,
                    fontWeight = FontWeight.Medium, maxLines = 1, modifier = Modifier.weight(1f))
                Text("▼", color = Accent, fontSize = 10.sp)
            }
            PlayerDropdown(expanded, { expanded = false }, slot, savedNames, menuScrollState,
                onSlotChange, { expanded = false; newNameInput = ""; showAddDialog = true }, { expanded = false })
        }
    }

    if (showAddDialog) {
        AddNameDialog(newNameInput, { newNameInput = it },
            onSave = {
                val trimmed = newNameInput.trim()
                if (trimmed.isNotBlank()) {
                    onSlotChange(slot.copy(isBot = false, name = trimmed))
                    PlayerNamesStorage.saveName(context, trimmed)
                }
                showAddDialog = false
            },
            onCancel = { showAddDialog = false })
    }
}

@Composable
private fun PlayerDropdown(
    expanded: Boolean, onDismiss: () -> Unit, slot: PlayerSlot, savedNames: List<String>,
    menuScrollState: androidx.compose.foundation.ScrollState,
    onSlotChange: (PlayerSlot) -> Unit, onNewName: () -> Unit, onClose: () -> Unit
) {
    DropdownMenu(
        expanded = expanded, onDismissRequest = onDismiss, scrollState = menuScrollState,
        modifier = Modifier.heightIn(max = 400.dp).background(DarkBg).drawWithContent {
            drawContent()
            val gh = 40.dp.toPx()
            drawRect(brush = Brush.verticalGradient(colors = listOf(Color.Transparent, DarkBg.copy(alpha = 0.9f), DarkBg),
                startY = size.height - gh, endY = size.height))
            if (menuScrollState.maxValue > 0) {
                val vp = menuScrollState.viewportSize.toFloat()
                val mx = menuScrollState.maxValue.toFloat()
                val ct = vp + mx
                if (ct > 0f) {
                    val tf = (vp / ct).coerceIn(0.05f, 1f)
                    val th = (size.height * tf).coerceAtLeast(40f)
                    val sf = if (mx > 0f) menuScrollState.value.toFloat() / mx else 0f
                    val tt = (size.height - th) * sf
                    val tw = 4.dp.toPx()
                    val tr = 3.dp.toPx()
                    drawRoundRect(color = Accent.copy(alpha = 0.8f),
                        topLeft = Offset(size.width - tw - tr, tt),
                        size = Size(tw, th),
                        cornerRadius = CornerRadius(tw / 2f))
                }
            }
        }
    ) {
        Text("  ИГРОК", color = Accent, fontSize = 11.sp, fontWeight = FontWeight.Bold, letterSpacing = 2.sp,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp))
        savedNames.forEach { saved ->
            DropdownMenuItem(text = { Text(saved, color = Color.White) },
                onClick = { onSlotChange(slot.copy(isBot = false, name = saved)); onClose() })
        }
        DropdownMenuItem(text = { Text("+ Новое имя", color = Accent) }, onClick = onNewName)
        HorizontalDivider(color = TileBg)
        Text("  БОТЫ", color = Accent, fontSize = 11.sp, fontWeight = FontWeight.Bold, letterSpacing = 2.sp,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp))
        CRICKET_BOTS.forEach { b ->
            DropdownMenuItem(
                text = {
                    Column {
                        Text("${b.id}. ${b.name}", color = Color.White, fontWeight = FontWeight.Medium)
                        Text("ср. ${b.averageMin}–${b.averageMax}", color = Accent, fontSize = 11.sp)
                    }
                },
                onClick = { onSlotChange(slot.copy(isBot = true, bot = b)); onClose() })
        }
    }
}

@Composable
private fun PlayerCell(
    number: Int, slot: PlayerSlot, savedNames: List<String>,
    context: android.content.Context, onSlotChange: (PlayerSlot) -> Unit,
    myAvgMpr: String? = null,
    modifier: Modifier
) {
    var expanded by remember { mutableStateOf(false) }
    var showAddDialog by remember { mutableStateOf(false) }
    var newNameInput by remember { mutableStateOf("") }
    val menuScrollState = rememberScrollState()

    Column(modifier = modifier.clip(RoundedCornerShape(12.dp)).background(TileBgDark).padding(8.dp)) {
        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            // Плашка с номером
            Box(modifier = Modifier.size(24.dp).clip(RoundedCornerShape(12.dp)).background(TileBg),
                contentAlignment = Alignment.Center) {
                Text("$number", color = Accent, fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }
            // Отдельная плашка со средним MPR (если есть)
            if (myAvgMpr != null) {
                Spacer(Modifier.width(4.dp))
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(TileBg)
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text("ср. $myAvgMpr", color = GoldAccent, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
        Spacer(Modifier.height(6.dp))
        Box(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(8.dp)).background(TileBg)
                    .clickable { expanded = true }.padding(horizontal = 10.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(if (slot.isBot) slot.bot.name else slot.name, color = Color.White, fontSize = 14.sp,
                    fontWeight = FontWeight.Medium, maxLines = 1, modifier = Modifier.weight(1f))
                Text("▼", color = Accent, fontSize = 10.sp)
            }
            PlayerDropdown(expanded, { expanded = false }, slot, savedNames, menuScrollState,
                onSlotChange, { expanded = false; newNameInput = ""; showAddDialog = true }, { expanded = false })
        }
    }

    if (showAddDialog) {
        AddNameDialog(newNameInput, { newNameInput = it },
            onSave = {
                val trimmed = newNameInput.trim()
                if (trimmed.isNotBlank()) {
                    onSlotChange(slot.copy(isBot = false, name = trimmed))
                    PlayerNamesStorage.saveName(context, trimmed)
                }
                showAddDialog = false
            },
            onCancel = { showAddDialog = false })
    }
}

@Composable
private fun AddNameDialog(value: String, onValueChange: (String) -> Unit,
                          onSave: () -> Unit, onCancel: () -> Unit) {
    AlertDialog(
        onDismissRequest = onCancel,
        confirmButton = { TextButton(onClick = onSave) { Text("Сохранить", color = Accent) } },
        dismissButton = { TextButton(onClick = onCancel) { Text("Отмена", color = Color.White.copy(alpha = 0.6f)) } },
        title = { Text("Новое имя игрока", color = Color.White) },
        text = {
            OutlinedTextField(value = value, onValueChange = onValueChange, singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = Color.White, unfocusedTextColor = Color.White,
                    focusedBorderColor = Accent, unfocusedBorderColor = TileBg, cursorColor = Accent))
        }
    )
}

@Composable
private fun CricketTypeHalf(label: String, selected: Boolean, onClick: () -> Unit, modifier: Modifier) {
    val bg = if (selected) Accent else TileBgDark
    val fg = if (selected) Color(0xFF121212) else Color.White
    Box(modifier = modifier.clip(RoundedCornerShape(12.dp)).background(bg).clickable { onClick() }
        .padding(vertical = 16.dp), contentAlignment = Alignment.Center) {
        Text(label, color = fg, fontSize = 14.sp,
            fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium, textAlign = TextAlign.Center)
    }
}

// ─────────────────────────────────────────────
// Хелперы для чтения статистики
// ─────────────────────────────────────────────
private fun parseStringListC(s: String): List<String> =
    if (s.isBlank()) emptyList() else s.split("|")

private fun parseDoubleListC(s: String): List<Double> =
    if (s.isBlank()) emptyList() else s.split("|").map { it.toDoubleOrNull() ?: 0.0 }

private fun isOwnerC(isBotFlag: String, name: String, ownerName: String): Boolean {
    if (isBotFlag != "0") return false
    if (ownerName.isBlank()) return true
    return name.equals(ownerName, ignoreCase = true)
}
