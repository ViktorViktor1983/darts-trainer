package com.lodkin.dartstrainer.ui.game501

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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.lodkin.dartstrainer.data.cricket.CRICKET_BOTS
import com.lodkin.dartstrainer.data.cricket.CricketBot
import com.lodkin.dartstrainer.data.cricket.PlayerNamesStorage
import com.lodkin.dartstrainer.data.game501.Game501Database
import com.lodkin.dartstrainer.data.game501.Game501Repository
import com.lodkin.dartstrainer.data.game501.Game501SettingsStorage
import com.lodkin.dartstrainer.data.game501.Game501Simulator
import com.lodkin.dartstrainer.data.game501.GameType
import com.lodkin.dartstrainer.data.game501.OutMode
import com.lodkin.dartstrainer.data.game501.Player501
import com.lodkin.dartstrainer.theme.Accent
import com.lodkin.dartstrainer.theme.DarkBg
import com.lodkin.dartstrainer.theme.GoldAccent
import com.lodkin.dartstrainer.theme.TileBg
import com.lodkin.dartstrainer.theme.TileBgDark
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.random.Random
import java.util.Locale

private val PaleRed = Color(0xFFE57373)
private val PaleRedText = Color(0xFF3E1010)

data class Slot501(
    val isBot: Boolean,
    val name: String,
    val bot: CricketBot
)

private fun OutMode.displayName(): String = when (this) {
    OutMode.DOUBLE_IN_OUT -> "Double In / Double Out"
    OutMode.DOUBLE_OUT -> "Double Out"
    OutMode.STRAIGHT_OUT -> "Без даблов"
}

@Composable
fun Game501SetupScreen(
    playerName: String,
    onStartGame: (GameType, OutMode, List<Player501>, Int, Int, Boolean, Int, Int) -> Unit,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val savedNames = remember { PlayerNamesStorage.getSavedNames(context).toMutableList() }

    var isPairGame by remember { mutableStateOf(Game501SettingsStorage.isPairGame(context)) }
    var gameType by remember { mutableStateOf(Game501SettingsStorage.getGameType(context)) }
    var outMode by remember {
        mutableStateOf(Game501SettingsStorage.getOutMode(context, Game501SettingsStorage.getGameType(context)))
    }
    var autoOkSeconds by remember { mutableStateOf(Game501SettingsStorage.getAutoOk(context)) }
    var legsPerSet by remember { mutableStateOf(Game501SettingsStorage.getLegsPerSet(context)) }
    var setsPerMatch by remember { mutableStateOf(Game501SettingsStorage.getSetsPerMatch(context)) }

    var showBullInputDialog by remember { mutableStateOf(false) }
    var showBullResultDialog by remember { mutableStateOf(false) }
    var humanBullResult by remember { mutableStateOf(-1) }
    var botBullResult by remember { mutableStateOf(-1) }

    var slots by remember { mutableStateOf(loadSlots(context, playerName)) }

    var showTestDialog by remember { mutableStateOf(false) }
    var testBotLevels by remember { mutableStateOf(setOf(7)) }
    var testLegs by remember { mutableStateOf(200) }
    var testRunning by remember { mutableStateOf(false) }
    var testResults by remember { mutableStateOf<List<Game501Simulator.SimResult>>(emptyList()) }
    var showTestResult by remember { mutableStateOf(false) }

    val game501Repo = remember { Game501Repository(Game501Database.get(context).game501Dao()) }
    var myAvgPpr by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(playerName) {
        val games = game501Repo.getAllGames()
        var sum = 0.0
        var count = 0
        for (g in games) {
            if (g.outModeName == "STRAIGHT_OUT") continue
            val bots = parseStringList501(g.playerIsBot)
            val names = parseStringList501(g.playerNames)
            val pprList = parseDoubleList501(g.ppr)
            for (i in bots.indices) {
                val name = names.getOrNull(i) ?: ""
                if (isOwner501(bots[i], name, playerName)) {
                    pprList.getOrNull(i)?.let { sum += it; count++ }
                    break
                }
            }
        }
        myAvgPpr = if (count > 0) "%.1f".format(Locale.US, sum / count) else null
    }

    fun plateFor(slot: Slot501): String? {
        return when {
            slot.isBot -> {
                val range = slot.bot.pprLabel.substringBefore("·").trim()
                if (range.isBlank()) null else range
            }
            slot.name.equals(playerName, ignoreCase = true) && myAvgPpr != null -> "ср. $myAvgPpr"
            else -> null
        }
    }

    val activeSlots = if (isPairGame) slots else slots.take(2)
    val allBots = activeSlots.all { it.isBot }
    val allHumans = activeSlots.all { !it.isBot }
    val hasHuman = activeSlots.any { !it.isBot }

    fun changeGameType(newType: GameType) {
        Game501SettingsStorage.setOutMode(context, gameType, outMode)
        gameType = newType
        outMode = Game501SettingsStorage.getOutMode(context, newType)
        Game501SettingsStorage.setGameType(context, newType)
    }

    fun changeOutMode(newMode: OutMode) {
        outMode = newMode
        Game501SettingsStorage.setOutMode(context, gameType, newMode)
    }

    LaunchedEffect(isPairGame, autoOkSeconds, legsPerSet, setsPerMatch) {
        Game501SettingsStorage.setPairGame(context, isPairGame)
        Game501SettingsStorage.setAutoOk(context, autoOkSeconds)
        Game501SettingsStorage.setLegsPerSet(context, legsPerSet)
        Game501SettingsStorage.setSetsPerMatch(context, setsPerMatch)
    }

    LaunchedEffect(slots) {
        Game501SettingsStorage.setSlotIsBot(context, slots.map { it.isBot })
        Game501SettingsStorage.setSlotNames(context, slots.map { it.name })
        Game501SettingsStorage.setSlotBotIds(context, slots.map { it.bot.id })
    }

    fun startGame(startingTeam: Int) {
        val players: List<Player501> = if (isPairGame) {
            listOf(
                Player501(name = if (slots[0].isBot) slots[0].bot.name else slots[0].name,
                    isBot = slots[0].isBot, botLevel = if (slots[0].isBot) slots[0].bot.id else 0,
                    teamIndex = 0, score = gameType.startScore),
                Player501(name = if (slots[1].isBot) slots[1].bot.name else slots[1].name,
                    isBot = slots[1].isBot, botLevel = if (slots[1].isBot) slots[1].bot.id else 0,
                    teamIndex = 1, score = gameType.startScore),
                Player501(name = if (slots[2].isBot) slots[2].bot.name else slots[2].name,
                    isBot = slots[2].isBot, botLevel = if (slots[2].isBot) slots[2].bot.id else 0,
                    teamIndex = 0, score = gameType.startScore),
                Player501(name = if (slots[3].isBot) slots[3].bot.name else slots[3].name,
                    isBot = slots[3].isBot, botLevel = if (slots[3].isBot) slots[3].bot.id else 0,
                    teamIndex = 1, score = gameType.startScore)
            ).also { slots.forEach { s -> if (!s.isBot) PlayerNamesStorage.saveName(context, s.name) } }
        } else {
            val a = slots[0]; val b = slots[1]
            listOf(
                Player501(name = if (a.isBot) a.bot.name else a.name,
                    isBot = a.isBot, botLevel = if (a.isBot) a.bot.id else 0,
                    teamIndex = 0, score = gameType.startScore),
                Player501(name = if (b.isBot) b.bot.name else b.name,
                    isBot = b.isBot, botLevel = if (b.isBot) b.bot.id else 0,
                    teamIndex = 1, score = gameType.startScore)
            ).also {
                if (!a.isBot) { PlayerNamesStorage.saveName(context, a.name); PlayerNamesStorage.setLastPlayer1(context, a.name) }
                if (!b.isBot) { PlayerNamesStorage.saveName(context, b.name); PlayerNamesStorage.setLastPlayer2(context, b.name) }
            }
        }
        onStartGame(gameType, outMode, players, legsPerSet, setsPerMatch, isPairGame, startingTeam, autoOkSeconds)
    }

    fun runBotTest() {
        if (testBotLevels.isEmpty()) return
        testRunning = true
        testResults = emptyList()
        scope.launch {
            val levels = testBotLevels.sorted()
            val result = withContext(Dispatchers.Default) {
                Game501Simulator.simulateMany(
                    botLevels = levels,
                    legsToPlay = testLegs
                )
            }
            testResults = result
            testRunning = false
            showTestResult = true
        }
    }

    LaunchedEffect(isPairGame) { humanBullResult = -1; botBullResult = -1 }

    Column(modifier = Modifier.fillMaxSize().background(DarkBg).padding(16.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier.clip(RoundedCornerShape(12.dp)).background(TileBgDark)
                    .clickable { onBack() }.padding(horizontal = 16.dp, vertical = 10.dp)
            ) { Text("← Назад", color = Accent, fontSize = 15.sp) }
            Spacer(Modifier.width(12.dp))
            Text("x01", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = Color.White, modifier = Modifier.weight(1f))
            Box(
                modifier = Modifier.clip(RoundedCornerShape(10.dp)).background(TileBgDark)
                    .clickable { showTestDialog = true }
                    .padding(horizontal = 12.dp, vertical = 8.dp)
            ) {
                Text("🧪 Тест бота", color = Accent, fontSize = 13.sp, fontWeight = FontWeight.Bold)
            }
        }

        Spacer(Modifier.height(20.dp))

        Column(modifier = Modifier.fillMaxWidth().weight(1f).verticalScroll(rememberScrollState())) {
            Text(
                if (isPairGame) "КОМАНДЫ (2 НА 2)" else "ИГРОКИ",
                color = Accent, fontSize = 13.sp, fontWeight = FontWeight.Medium,
                letterSpacing = 3.sp, modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.Center
            )
            Spacer(Modifier.height(10.dp))

            if (isPairGame) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.Top) {
                    TeamColumn501("КОМАНДА A", slots[0], slots[2], "1", "3", savedNames, context,
                        { newSlot -> slots = slots.toMutableList().also { it[0] = newSlot } },
                        { newSlot -> slots = slots.toMutableList().also { it[2] = newSlot } },
                        plate1 = plateFor(slots[0]),
                        plate2 = plateFor(slots[2]),
                        modifier = Modifier.weight(1f))
                    TeamColumn501("КОМАНДА B", slots[1], slots[3], "2", "4", savedNames, context,
                        { newSlot -> slots = slots.toMutableList().also { it[1] = newSlot } },
                        { newSlot -> slots = slots.toMutableList().also { it[3] = newSlot } },
                        plate1 = plateFor(slots[1]),
                        plate2 = plateFor(slots[3]),
                        modifier = Modifier.weight(1f))
                }
            } else {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.Top) {
                    PlayerCell501(1, slots[0], savedNames, context,
                        { newSlot -> slots = slots.toMutableList().also { it[0] = newSlot } },
                        plateText = plateFor(slots[0]),
                        modifier = Modifier.weight(1f))
                    PlayerCell501(2, slots[1], savedNames, context,
                        { newSlot -> slots = slots.toMutableList().also { it[1] = newSlot } },
                        plateText = plateFor(slots[1]),
                        modifier = Modifier.weight(1f))
                }
            }

            Spacer(Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp)).background(TileBgDark)
                    .clickable { isPairGame = !isPairGame }.padding(horizontal = 14.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Парная игра (2 на 2)", color = Color.White, fontSize = 14.sp,
                    fontWeight = FontWeight.Medium, modifier = Modifier.weight(1f))
                Switch(checked = isPairGame, onCheckedChange = { isPairGame = it },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = Accent, checkedTrackColor = Accent.copy(alpha = 0.5f),
                        uncheckedThumbColor = Color.White, uncheckedTrackColor = TileBg))
            }

            Spacer(Modifier.height(20.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text("ИГРА", color = Accent, fontSize = 13.sp, fontWeight = FontWeight.Medium,
                        letterSpacing = 3.sp, modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.Center)
                    Spacer(Modifier.height(8.dp))
                    GameTypeDropdown(selected = gameType, onSelect = { changeGameType(it) })
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text("ЗАКРЫТИЕ", color = Accent, fontSize = 13.sp, fontWeight = FontWeight.Medium,
                        letterSpacing = 3.sp, modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.Center)
                    Spacer(Modifier.height(8.dp))
                    OutModeDropdown(selected = outMode, onSelect = { changeOutMode(it) })
                }
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
                        modifier = Modifier.weight(1f).clip(RoundedCornerShape(12.dp))
                            .background(if (selected) Accent else TileBgDark)
                            .clickable { autoOkSeconds = sec }.padding(vertical = 12.dp),
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
                    modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp)).background(PaleRed)
                        .clickable { showBullInputDialog = true }.padding(vertical = 16.dp),
                    contentAlignment = Alignment.Center
                ) { Text("Разыграть Bull", color = PaleRedText, fontSize = 16.sp, fontWeight = FontWeight.Bold) }
            } else if (allHumans) {
                Spacer(Modifier.height(24.dp))
                Text("КТО НАЧИНАЕТ", color = Accent, fontSize = 13.sp, fontWeight = FontWeight.Medium,
                    letterSpacing = 3.sp, modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.Center)
                Spacer(Modifier.height(10.dp))
                val labelA = if (isPairGame) "КОМАНДА A" else "Игрок 1"
                val labelB = if (isPairGame) "КОМАНДА B" else "Игрок 2"
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Box(modifier = Modifier.weight(1f).clip(RoundedCornerShape(12.dp)).background(PaleRed)
                        .clickable { startGame(0) }.padding(vertical = 16.dp),
                        contentAlignment = Alignment.Center
                    ) { Text(labelA, color = PaleRedText, fontSize = 15.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center) }
                    Box(modifier = Modifier.weight(1f).clip(RoundedCornerShape(12.dp)).background(PaleRed)
                        .clickable { startGame(1) }.padding(vertical = 16.dp),
                        contentAlignment = Alignment.Center
                    ) { Text(labelB, color = PaleRedText, fontSize = 15.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center) }
                }
            }

            Spacer(Modifier.height(24.dp))
        }

        if (allBots) {
            Box(
                modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(16.dp)).background(Accent)
                    .clickable { startGame(0) }.padding(vertical = 18.dp),
                contentAlignment = Alignment.Center
            ) { Text("НАЧАТЬ ИГРУ", color = Color(0xFF121212), fontSize = 16.sp, fontWeight = FontWeight.Bold) }
        }
    }

    if (showBullInputDialog) {
        AlertDialog(
            onDismissRequest = { showBullInputDialog = false },
            confirmButton = {}, dismissButton = {},
            title = { Text("Укажите Ваш результат броска в Bull", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    BullBtn("КРАСНЫЙ (50)", Color(0xFFD32F2F)) {
                        humanBullResult = 2
                        botBullResult = generateBotBullResult(activeSlots.first { it.isBot }.bot)
                        showBullInputDialog = false; showBullResultDialog = true
                    }
                    Spacer(Modifier.height(10.dp))
                    BullBtn("ЗЕЛЁНЫЙ (25)", Color(0xFF4CAF50)) {
                        humanBullResult = 1
                        botBullResult = generateBotBullResult(activeSlots.first { it.isBot }.bot)
                        showBullInputDialog = false; showBullResultDialog = true
                    }
                    Spacer(Modifier.height(10.dp))
                    BullBtn("МИМО (0)", Color(0xFF78909C)) {
                        humanBullResult = 0
                        botBullResult = generateBotBullResult(activeSlots.first { it.isBot }.bot)
                        showBullInputDialog = false; showBullResultDialog = true
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
            if (isPairGame) { if (humanSlotIndex == 0 || humanSlotIndex == 2) 0 else 1 } else humanSlotIndex
        }
        val humanName = activeSlots.first { !it.isBot }.name
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
                        humanBullResult = -1; botBullResult = -1
                        showBullResultDialog = false; showBullInputDialog = true
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
                        Text("Игру начинает ${if (humanWins) humanName else botName}",
                            color = GoldAccent, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        )
    }

    if (showTestDialog) {
        AlertDialog(
            onDismissRequest = { if (!testRunning) showTestDialog = false },
            confirmButton = {
                TextButton(onClick = { runBotTest() }, enabled = !testRunning && testBotLevels.isNotEmpty()) {
                    Text(
                        if (testRunning) "Идёт тест..." else "Запустить (${testBotLevels.size})",
                        color = if (testBotLevels.isEmpty()) Color.White.copy(alpha = 0.4f) else Accent
                    )
                }
            },
            dismissButton = {
                TextButton(onClick = { showTestDialog = false }, enabled = !testRunning) {
                    Text("Отмена", color = Color.White.copy(alpha = 0.6f))
                }
            },
            title = { Text("Тест ботов", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold) },
            text = {
                Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                    Text("Отметь ботов (можно несколько):", color = Color.White, fontSize = 14.sp)
                    Spacer(Modifier.height(6.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Box(
                            modifier = Modifier.clip(RoundedCornerShape(8.dp)).background(TileBg)
                                .clickable(enabled = !testRunning) {
                                    testBotLevels = CRICKET_BOTS.map { it.id }.toSet()
                                }
                                .padding(horizontal = 10.dp, vertical = 6.dp)
                        ) { Text("Выбрать все", color = Accent, fontSize = 12.sp, fontWeight = FontWeight.Medium) }
                        Box(
                            modifier = Modifier.clip(RoundedCornerShape(8.dp)).background(TileBg)
                                .clickable(enabled = !testRunning) { testBotLevels = emptySet() }
                                .padding(horizontal = 10.dp, vertical = 6.dp)
                        ) { Text("Снять все", color = Color.White.copy(alpha = 0.7f), fontSize = 12.sp) }
                    }
                    Spacer(Modifier.height(10.dp))

                    val botRows = CRICKET_BOTS.chunked(4)
                    botRows.forEach { row ->
                        Row(modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp),
                            horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            row.forEach { b ->
                                val selected = b.id in testBotLevels
                                Box(
                                    modifier = Modifier.weight(1f).clip(RoundedCornerShape(8.dp))
                                        .background(if (selected) Accent else TileBgDark)
                                        .clickable(enabled = !testRunning) {
                                            testBotLevels = if (selected) testBotLevels - b.id
                                            else testBotLevels + b.id
                                        }
                                        .padding(vertical = 8.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text("${b.id}",
                                        color = if (selected) Color(0xFF121212) else Color.White,
                                        fontSize = 14.sp,
                                        fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium)
                                }
                            }
                            if (row.size < 4) repeat(4 - row.size) { Spacer(Modifier.weight(1f)) }
                        }
                    }
                    Spacer(Modifier.height(8.dp))
                    val names = testBotLevels.sorted()
                        .mapNotNull { id -> CRICKET_BOTS.firstOrNull { it.id == id }?.name }
                        .joinToString(", ")
                    Text(
                        if (names.isEmpty()) "Никого не выбрано" else "Выбрано: $names",
                        color = GoldAccent, fontSize = 12.sp, fontWeight = FontWeight.Bold
                    )

                    Spacer(Modifier.height(12.dp))
                    Text("Сколько легов прогнать (на каждого):", color = Color.White, fontSize = 14.sp)
                    Spacer(Modifier.height(6.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        listOf(50, 100, 200, 500).forEach { n ->
                            val selected = testLegs == n
                            Box(
                                modifier = Modifier.weight(1f).clip(RoundedCornerShape(8.dp))
                                    .background(if (selected) Accent else TileBgDark)
                                    .clickable(enabled = !testRunning) { testLegs = n }
                                    .padding(vertical = 8.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text("$n",
                                    color = if (selected) Color(0xFF121212) else Color.White,
                                    fontSize = 13.sp,
                                    fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium)
                            }
                        }
                    }
                }
            }
        )
    }

    if (showTestResult && testResults.isNotEmpty()) {
        AlertDialog(
            onDismissRequest = { showTestResult = false },
            confirmButton = {
                TextButton(onClick = { showTestResult = false }) { Text("OK", color = Accent) }
            },
            title = {
                Text("Результаты теста", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold)
            },
            text = {
                Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                    Row(modifier = Modifier.fillMaxWidth().padding(bottom = 4.dp)) {
                        Text("Бот", color = Accent, fontSize = 11.sp, fontWeight = FontWeight.Bold,
                            modifier = Modifier.weight(1.6f))
                        Text("PPR", color = Accent, fontSize = 11.sp, fontWeight = FontWeight.Bold,
                            modifier = Modifier.weight(1f), textAlign = TextAlign.End)
                        Text("D%", color = Accent, fontSize = 11.sp, fontWeight = FontWeight.Bold,
                            modifier = Modifier.weight(1f), textAlign = TextAlign.End)
                        Text("Д/Л", color = Accent, fontSize = 11.sp, fontWeight = FontWeight.Bold,
                            modifier = Modifier.weight(1f), textAlign = TextAlign.End)
                    }
                    HorizontalDivider(color = TileBg)
                    Spacer(Modifier.height(4.dp))

                    testResults.forEach { r ->
                        val botName = CRICKET_BOTS.firstOrNull { it.id == r.botLevel }?.name ?: "?"
                        val attemptsPerLeg = if (r.totalLegs > 0)
                            r.doublesAttempted.toDouble() / r.totalLegs else 0.0
                        Row(modifier = Modifier.fillMaxWidth().padding(vertical = 3.dp)) {
                            Column(modifier = Modifier.weight(1.6f)) {
                                Text("${r.botLevel}. $botName", color = Color.White, fontSize = 12.sp,
                                    fontWeight = FontWeight.Medium)
                                Text("лег: ${r.totalLegs}", color = Color.White.copy(alpha = 0.5f), fontSize = 10.sp)
                                Text("hit/att: ${r.doublesHit}/${r.doublesAttempted} (${"%.1f".format(Locale.US, attemptsPerLeg)}/лег)",
                                    color = Color.White.copy(alpha = 0.5f), fontSize = 10.sp)
                            }
                            Text("%.1f".format(Locale.US, r.ppr), color = GoldAccent, fontSize = 12.sp,
                                fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f), textAlign = TextAlign.End)
                            Text("%.1f".format(Locale.US, r.doublesAccuracy), color = GoldAccent, fontSize = 12.sp,
                                fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f), textAlign = TextAlign.End)
                            Text("%.1f".format(Locale.US, r.dartsPerLeg), color = GoldAccent, fontSize = 12.sp,
                                fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f), textAlign = TextAlign.End)
                        }
                    }

                    Spacer(Modifier.height(10.dp))
                    HorizontalDivider(color = TileBg)
                    Spacer(Modifier.height(6.dp))
                    Text("PPR — средний набор, D% — точность удвоений,",
                        color = Color.White.copy(alpha = 0.5f), fontSize = 10.sp)
                    Text("Д/Л — дротиков на лег, hit/att — попаданий/попыток в дабл.",
                        color = Color.White.copy(alpha = 0.5f), fontSize = 10.sp)
                    Text("Легов на каждого: $testLegs",
                        color = Color.White.copy(alpha = 0.5f), fontSize = 10.sp)
                }
            }
        )
    }
}

@Composable
private fun GameTypeDropdown(selected: GameType, onSelect: (GameType) -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    Box(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth().height(48.dp).clip(RoundedCornerShape(12.dp))
                .background(TileBg)
                .clickable { expanded = true }.padding(horizontal = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(selected.label, color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold,
                modifier = Modifier.weight(1f),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis)
            Text("▼", color = Accent, fontSize = 10.sp)
        }
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier.background(DarkBg)
        ) {
            GameType.values().forEach { type ->
                DropdownMenuItem(
                    text = {
                        Text(type.label,
                            color = if (type == selected) Accent else Color.White,
                            fontSize = 15.sp,
                            fontWeight = if (type == selected) FontWeight.Bold else FontWeight.Normal)
                    },
                    onClick = { onSelect(type); expanded = false }
                )
            }
        }
    }
}

@Composable
private fun OutModeDropdown(selected: OutMode, onSelect: (OutMode) -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    Box(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth().height(48.dp).clip(RoundedCornerShape(12.dp))
                .background(TileBg)
                .clickable { expanded = true }.padding(horizontal = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(selected.displayName(), color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Medium,
                modifier = Modifier.weight(1f),
                maxLines = 2,
                overflow = TextOverflow.Ellipsis)
            Text("▼", color = Accent, fontSize = 10.sp)
        }
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier.background(DarkBg)
        ) {
            OutMode.values().forEach { mode ->
                DropdownMenuItem(
                    text = {
                        Text(mode.displayName(),
                            color = if (mode == selected) Accent else Color.White,
                            fontSize = 14.sp,
                            fontWeight = if (mode == selected) FontWeight.Bold else FontWeight.Normal)
                    },
                    onClick = { onSelect(mode); expanded = false }
                )
            }
        }
    }
}

@Composable
private fun SimResultRow(label: String, value: String) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(label, color = Color.White, fontSize = 14.sp)
        Text(value, color = GoldAccent, fontSize = 14.sp, fontWeight = FontWeight.Bold)
    }
}

private fun loadSlots(context: android.content.Context, playerName: String): List<Slot501> {
    val bots = Game501SettingsStorage.getSlotIsBot(context)
    val names = Game501SettingsStorage.getSlotNames(context)
    val botIds = Game501SettingsStorage.getSlotBotIds(context)

    val ownerName = when {
        playerName.isNotBlank() -> playerName
        PlayerNamesStorage.getLastPlayer1(context).isNotBlank() -> PlayerNamesStorage.getLastPlayer1(context)
        else -> "Игрок 1"
    }

    val defaults = listOf(
        Slot501(false, ownerName, CRICKET_BOTS[2]),
        Slot501(true, PlayerNamesStorage.getLastPlayer2(context).ifBlank { "Игрок 2" }, CRICKET_BOTS[2]),
        Slot501(true, "Игрок 3", CRICKET_BOTS[2]),
        Slot501(true, "Игрок 4", CRICKET_BOTS[2])
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
        Slot501(isBot, name, bot)
    }
}

// Бросок в Bull для розыгрыша первого хода.
// Множитель 0.8 — понижено по просьбе автора (раньше было 0.9).
private fun generateBotBullResult(bot: CricketBot): Int {
    val lvl = bot.id.coerceIn(1, 16)
    val pRed = (0.05 + (lvl - 1) * 0.027) * 0.8
    val pGreen = (0.25 + (lvl - 1) * 0.013) * 0.8
    val r = Random.nextDouble()
    return when {
        r < pRed -> 2
        r < pRed + pGreen -> 1
        else -> 0
    }
}

@Composable
private fun BullBtn(label: String, color: Color, onClick: () -> Unit) {
    Box(
        modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(10.dp)).background(color)
            .clickable { onClick() }.padding(vertical = 14.dp),
        contentAlignment = Alignment.Center
    ) { Text(label, color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold) }
}

@Composable
private fun CompactNumberPicker(label: String, value: Int, onValueChange: (Int) -> Unit, modifier: Modifier = Modifier) {
    var expanded by remember { mutableStateOf(false) }
    Box(modifier = modifier) {
        Row(
            modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp)).background(TileBgDark)
                .clickable { expanded = true }.padding(horizontal = 14.dp, vertical = 14.dp),
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
private fun TeamColumn501(
    teamLabel: String, slot1: Slot501, slot2: Slot501, slot1Label: String, slot2Label: String,
    savedNames: List<String>, context: android.content.Context,
    onSlot1Change: (Slot501) -> Unit, onSlot2Change: (Slot501) -> Unit,
    plate1: String? = null, plate2: String? = null,
    modifier: Modifier
) {
    Column(modifier = modifier.clip(RoundedCornerShape(12.dp)).background(TileBgDark).padding(8.dp)) {
        Text(teamLabel, color = Accent, fontSize = 11.sp, fontWeight = FontWeight.Bold,
            letterSpacing = 2.sp, modifier = Modifier.fillMaxWidth().padding(bottom = 6.dp), textAlign = TextAlign.Center)
        PlayerInnerSlot501(slot1Label, slot1, savedNames, context, onSlot1Change, plate1)
        Spacer(Modifier.height(6.dp))
        PlayerInnerSlot501(slot2Label, slot2, savedNames, context, onSlot2Change, plate2)
    }
}

@Composable
private fun PlayerInnerSlot501(
    numberLabel: String, slot: Slot501, savedNames: List<String>,
    context: android.content.Context, onSlotChange: (Slot501) -> Unit,
    plateText: String? = null
) {
    var expanded by remember { mutableStateOf(false) }
    var showAddDialog by remember { mutableStateOf(false) }
    var newNameInput by remember { mutableStateOf("") }
    Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(22.dp)
                .clip(RoundedCornerShape(11.dp))
                .background(TileBg),
            contentAlignment = Alignment.Center
        ) {
            Text(numberLabel, color = Accent, fontSize = 11.sp, fontWeight = FontWeight.Bold)
        }
        if (plateText != null) {
            Spacer(Modifier.width(4.dp))
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(11.dp))
                    .background(TileBg)
                    .padding(horizontal = 6.dp, vertical = 3.dp)
            ) {
                Text(plateText, color = GoldAccent, fontSize = 10.sp, fontWeight = FontWeight.Bold)
            }
        }
        Spacer(Modifier.width(6.dp))
        Box(modifier = Modifier.weight(1f)) {
            Row(modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(8.dp)).background(TileBg)
                .clickable { expanded = true }.padding(horizontal = 10.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically) {
                Text(if (slot.isBot) slot.bot.name else slot.name, color = Color.White, fontSize = 13.sp,
                    fontWeight = FontWeight.Medium, maxLines = 1, modifier = Modifier.weight(1f))
                Text("▼", color = Accent, fontSize = 10.sp)
            }
            SlotDropdown(expanded, { expanded = false }, slot, savedNames, onSlotChange,
                { expanded = false; newNameInput = ""; showAddDialog = true })
        }
    }
    if (showAddDialog) {
        AddNameDialog501(newNameInput, { newNameInput = it },
            onSave = {
                val trimmed = newNameInput.trim()
                if (trimmed.isNotBlank()) { onSlotChange(slot.copy(isBot = false, name = trimmed)); PlayerNamesStorage.saveName(context, trimmed) }
                showAddDialog = false
            }, onCancel = { showAddDialog = false })
    }
}

@Composable
private fun SlotDropdown(
    expanded: Boolean, onDismiss: () -> Unit, slot: Slot501, savedNames: List<String>,
    onSlotChange: (Slot501) -> Unit, onNewName: () -> Unit
) {
    DropdownMenu(expanded = expanded, onDismissRequest = onDismiss,
        modifier = Modifier.heightIn(max = 400.dp).background(DarkBg)) {
        Text("  ИГРОК", color = Accent, fontSize = 11.sp, fontWeight = FontWeight.Bold, letterSpacing = 2.sp,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp))
        savedNames.forEach { saved ->
            DropdownMenuItem(text = { Text(saved, color = Color.White) },
                onClick = { onSlotChange(slot.copy(isBot = false, name = saved)); onDismiss() })
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
                        Text(b.pprLabel, color = Accent, fontSize = 11.sp)
                    }
                },
                onClick = { onSlotChange(slot.copy(isBot = true, bot = b)); onDismiss() })
        }
    }
}

@Composable
private fun PlayerCell501(
    number: Int, slot: Slot501, savedNames: List<String>,
    context: android.content.Context, onSlotChange: (Slot501) -> Unit,
    plateText: String? = null,
    modifier: Modifier
) {
    var expanded by remember { mutableStateOf(false) }
    var showAddDialog by remember { mutableStateOf(false) }
    var newNameInput by remember { mutableStateOf("") }
    Column(modifier = modifier.clip(RoundedCornerShape(12.dp)).background(TileBgDark).padding(8.dp)) {
        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(24.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(TileBg),
                contentAlignment = Alignment.Center
            ) {
                Text("$number", color = Accent, fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }
            if (plateText != null) {
                Spacer(Modifier.width(4.dp))
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(TileBg)
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(plateText, color = GoldAccent, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
        Spacer(Modifier.height(6.dp))
        Box(modifier = Modifier.fillMaxWidth()) {
            Row(modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(8.dp)).background(TileBg)
                .clickable { expanded = true }.padding(horizontal = 10.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically) {
                Text(if (slot.isBot) slot.bot.name else slot.name, color = Color.White, fontSize = 14.sp,
                    fontWeight = FontWeight.Medium, maxLines = 1, modifier = Modifier.weight(1f))
                Text("▼", color = Accent, fontSize = 10.sp)
            }
            SlotDropdown(expanded, { expanded = false }, slot, savedNames, onSlotChange,
                { expanded = false; newNameInput = ""; showAddDialog = true })
        }
    }
    if (showAddDialog) {
        AddNameDialog501(newNameInput, { newNameInput = it },
            onSave = {
                val trimmed = newNameInput.trim()
                if (trimmed.isNotBlank()) { onSlotChange(slot.copy(isBot = false, name = trimmed)); PlayerNamesStorage.saveName(context, trimmed) }
                showAddDialog = false
            }, onCancel = { showAddDialog = false })
    }
}

@Composable
private fun AddNameDialog501(value: String, onValueChange: (String) -> Unit,
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

// ─────────────────────────────────────────────
// Хелперы для чтения статистики
// ─────────────────────────────────────────────
private fun parseStringList501(s: String): List<String> =
    if (s.isBlank()) emptyList() else s.split("|")

private fun parseDoubleList501(s: String): List<Double> =
    if (s.isBlank()) emptyList() else s.split("|").map { it.toDoubleOrNull() ?: 0.0 }

private fun isOwner501(isBotFlag: String, name: String, ownerName: String): Boolean {
    if (isBotFlag != "0") return false
    if (ownerName.isBlank()) return true
    return name.equals(ownerName, ignoreCase = true)
}
