package com.lodkin.dartstrainer.ui.game501

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
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
import com.lodkin.dartstrainer.data.game501.*
import com.lodkin.dartstrainer.theme.Accent
import com.lodkin.dartstrainer.theme.ErrorColor
import com.lodkin.dartstrainer.theme.GoldAccent
import com.lodkin.dartstrainer.theme.TileBg
import com.lodkin.dartstrainer.theme.TileBgDark
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.Locale
import kotlin.random.Random

private val PaleYellow = Color(0xFFFFE082)
private val PaleYellowText = Color(0xFF3E2723)

@Composable
fun Game501Screen(
    repository: Game501Repository,
    initialGame: Game501,
    existingId: Long = 0L,
    onGameFinish: (Game501, Long) -> Unit,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var game by remember { mutableStateOf(initialGame) }
    var inputText by remember { mutableStateOf("") }

    // id сохранённой незавершённой партии.
    // 0 = партия ещё не сохранена (только началась или уже была завершена).
    var currentId by remember { mutableStateOf(existingId) }

    var lastScoreA by remember { mutableStateOf<Int?>(null) }
    var lastScoreB by remember { mutableStateOf<Int?>(null) }

    var showWinDialog by remember { mutableStateOf(false) }
    var showBackConfirm by remember { mutableStateOf(false) }
    var showLegWonDialog by remember { mutableStateOf(false) }
    var showSetWonDialog by remember { mutableStateOf(false) }
    var showLegQuestionDialog by remember { mutableStateOf(false) }
    var showDoublesQuestionDialog by remember { mutableStateOf(false) }
    var showDoublesOnlyDialog by remember { mutableStateOf(false) }
    var chosenDarts by remember { mutableStateOf(0) }
    var showBustMessage by remember { mutableStateOf(false) }

    var quickSums by remember { mutableStateOf(Game501SettingsStorage.getQuickSums(context)) }
    var recordSum by remember { mutableStateOf<Int?>(null) }

    var lastSeenLegHistorySize by remember { mutableStateOf(game.legHistory.size) }
    var lastSeenSetNumber by remember { mutableStateOf(game.currentSetNumber) }

    val allBots = game.players.all { it.isBot }

    val history = remember { mutableStateListOf<Game501>() }

    fun saveHistory() { history.add(game); if (history.size > 300) history.removeAt(0) }
    fun undo() { if (history.isNotEmpty()) game = history.removeAt(history.lastIndex) }

    val dialogOpen = showLegWonDialog || showSetWonDialog || showWinDialog ||
        showLegQuestionDialog || showDoublesQuestionDialog || showDoublesOnlyDialog
    val canUndo = history.isNotEmpty() && !dialogOpen

    LaunchedEffect(Unit) {
        if (game.sessionStartTime == 0L) {
            val form = 0.85 + Random.nextDouble() * 0.30
            game = game.copy(
                sessionForm = form,
                sessionStartTime = System.currentTimeMillis()
            )
        }
        quickSums = Game501SettingsStorage.getQuickSums(context)
    }

    LaunchedEffect(recordSum) {
        val s = recordSum
        if (s != null && s > 0) Game501SettingsStorage.recordHumanSum(context, s)
        recordSum = null
    }

    // ─────────────────────────────────────────
    // АВТОСОХРАНЕНИЕ.
    // Срабатывает при каждом изменении game, если партия ещё не завершена.
    // currentId обновляется — последующие сохранения будут обновлять ту же запись.
    // ─────────────────────────────────────────
    LaunchedEffect(game) {
        if (!game.isFinished) {
            currentId = repository.saveUnfinishedGame(game, currentId)
        }
    }

    fun setLastScore(value: Int) {
        val team = game.currentPlayer?.teamIndex ?: 0
        if (team == 0) lastScoreA = value else lastScoreB = value
    }

    fun applySum(value: Int) {
        if (value < 0 || value > 180) return
        val currentPlayer = game.players.getOrNull(game.currentPlayerIndex) ?: return
        if (currentPlayer.isBot || game.isFinished) return

        saveHistory()
        val scoreBefore = currentPlayer.score

        if (value == scoreBefore && value > 0) {
            chosenDarts = 0
            showLegQuestionDialog = true
            return
        }

        if (value > scoreBefore) {
            val updated = Game501Logic.applyTurnScore(game, value)
            showBustMessage = true
            game = updated
            if (scoreBefore <= 50) showDoublesOnlyDialog = true
            else game = Game501Logic.finishTurn(game)
            return
        }

        val updated = Game501Logic.applyTurnScore(game, value)
        if (!updated.isFinished) {
            recordSum = value
            setLastScore(value)
        }
        game = updated

        if (scoreBefore <= 50) showDoublesOnlyDialog = true
        else game = Game501Logic.finishTurn(game)
    }

    fun submitSum() {
        val value = inputText.toIntOrNull() ?: return
        inputText = ""
        applySum(value)
    }

    fun pressQuickButton(sum: Int) {
        inputText = ""
        applySum(sum)
    }

    fun submitRemaining() {
        val value = inputText.toIntOrNull() ?: return
        inputText = ""
        val currentPlayer = game.players.getOrNull(game.currentPlayerIndex) ?: return
        if (currentPlayer.isBot || game.isFinished) return
        if (value < 0 || value >= currentPlayer.score) return
        val gained = currentPlayer.score - value
        if (gained < 0) return

        saveHistory()
        val scoreBefore = currentPlayer.score

        if (value == 0) {
            chosenDarts = 0
            showLegQuestionDialog = true
            return
        }

        val requiresDouble = game.outMode == OutMode.DOUBLE_OUT || game.outMode == OutMode.DOUBLE_IN_OUT
        if (value == 1 && requiresDouble) {
            val updated = Game501Logic.applyTurnRemaining(game, value)
            showBustMessage = true
            game = updated
            if (scoreBefore <= 50) showDoublesOnlyDialog = true
            else game = Game501Logic.finishTurn(game)
            return
        }

        val updated = Game501Logic.applyTurnRemaining(game, value)
        recordSum = gained
        setLastScore(gained)
        game = updated

        if (scoreBefore <= 50) showDoublesOnlyDialog = true
        else game = Game501Logic.finishTurn(game)
    }

    fun pressLeg() {
        val currentPlayer = game.players.getOrNull(game.currentPlayerIndex) ?: return
        if (currentPlayer.isBot || game.isFinished) return
        chosenDarts = 0
        showLegQuestionDialog = true
    }

    LaunchedEffect(
        game.currentPlayerIndex,
        game.currentLegNumber,
        game.currentSetNumber,
        game.isFinished,
        showLegQuestionDialog, showDoublesQuestionDialog, showDoublesOnlyDialog,
        showWinDialog,
        showLegWonDialog, showSetWonDialog
    ) {
        if (game.isFinished) return@LaunchedEffect
        if (showLegQuestionDialog || showDoublesQuestionDialog || showDoublesOnlyDialog) return@LaunchedEffect
        if (showWinDialog) return@LaunchedEffect

        val currentPlayer = game.players.getOrNull(game.currentPlayerIndex) ?: return@LaunchedEffect
        if (!currentPlayer.isBot) return@LaunchedEffect

        if (!allBots && (showLegWonDialog || showSetWonDialog)) return@LaunchedEffect

        delay(if (allBots) 400L else 900L)

        if (game.isFinished) return@LaunchedEffect
        if (showLegQuestionDialog || showDoublesQuestionDialog || showDoublesOnlyDialog) return@LaunchedEffect
        if (game.players.getOrNull(game.currentPlayerIndex)?.isBot != true) return@LaunchedEffect
        if (!allBots && (showLegWonDialog || showSetWonDialog)) return@LaunchedEffect

        val idx = game.currentPlayerIndex
        val teamIdx = game.players.getOrNull(idx)?.teamIndex ?: 0
        val scoreBefore = game.players.getOrNull(idx)?.matchScoreGained ?: 0

        saveHistory()
        game = Game501BotAI.performTurn(game, idx)

        val scoreAfter = game.players.getOrNull(idx)?.matchScoreGained ?: 0
        val gained = scoreAfter - scoreBefore
        if (teamIdx == 0) lastScoreA = gained else lastScoreB = gained
    }

    LaunchedEffect(showBustMessage) {
        if (showBustMessage) { delay(1500L); showBustMessage = false }
    }

    LaunchedEffect(game.legHistory.size, allBots) {
        if (allBots) {
            lastSeenLegHistorySize = game.legHistory.size
            return@LaunchedEffect
        }
        if (game.legHistory.size > lastSeenLegHistorySize) {
            lastSeenLegHistorySize = game.legHistory.size
            showLegWonDialog = true
        }
    }

    LaunchedEffect(game.currentSetNumber, allBots, showLegWonDialog) {
        if (allBots) {
            lastSeenSetNumber = game.currentSetNumber
            return@LaunchedEffect
        }
        if (game.currentSetNumber > lastSeenSetNumber && !showLegWonDialog && !showWinDialog) {
            lastSeenSetNumber = game.currentSetNumber
            showSetWonDialog = true
        }
    }

    LaunchedEffect(game.isFinished, allBots, showLegWonDialog, showSetWonDialog) {
        if (!game.isFinished) return@LaunchedEffect
        if (allBots) {
            Game501SettingsStorage.updateQuickSumsIfNeeded(context)
            onGameFinish(game, currentId)
        } else if (!showLegWonDialog && !showSetWonDialog && !showWinDialog) {
            showWinDialog = true
        }
    }

    val activeTeam = game.currentPlayer?.teamIndex ?: 0

    Column(modifier = Modifier.fillMaxSize().background(Color(0xFF0D1117))) {
        TopBar501(game = game, onBack = { showBackConfirm = true })
        PlayersHeader501(game = game)

        Row(
            modifier = Modifier.fillMaxWidth().background(Color(0xFF16202C))
                .padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            PrevScoreBlock("Пред.", lastScoreA, activeTeam == 0, Alignment.Start)
            Text(
                if (inputText.isEmpty()) "—" else inputText,
                color = if (inputText.isEmpty()) Color.White.copy(alpha = 0.3f) else GoldAccent,
                fontSize = 32.sp, fontWeight = FontWeight.Bold,
                modifier = Modifier.weight(1f), textAlign = TextAlign.Center
            )
            PrevScoreBlock("Пред.", lastScoreB, activeTeam == 1, Alignment.End)
        }

        QuickButtonsWithLeg(
            sums = quickSums,
            enabled = !game.isFinished && game.currentPlayer?.isBot != true,
            onQuick = { pressQuickButton(it) },
            onLeg = { pressLeg() }
        )

        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 6.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            BigActionButton(
                "Остаток",
                !game.isFinished && game.currentPlayer?.isBot != true,
                TileBg, Accent, Modifier.weight(2f)
            ) { submitRemaining() }
            BigActionButton(
                "OK",
                !game.isFinished && game.currentPlayer?.isBot != true,
                Accent, Color(0xFF121212), Modifier.weight(4f)
            ) { submitSum() }
        }

        Keyboard501(
            enabled = !game.isFinished && game.currentPlayer?.isBot != true,
            canUndo = canUndo,
            onDigit = { inputText = if (inputText.length < 3) inputText + it else inputText },
            onBackspace = { inputText = if (inputText.isNotEmpty()) inputText.dropLast(1) else "" },
            onClear = { inputText = "" },
            onUndo = { undo() }
        )
    }

    if (showBustMessage) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Box(modifier = Modifier.clip(RoundedCornerShape(12.dp)).background(ErrorColor)
                .padding(horizontal = 32.dp, vertical = 20.dp)) {
                Text("ПЕРЕБОР", color = Color.White, fontSize = 28.sp, fontWeight = FontWeight.Bold)
            }
        }
    }

    if (showLegQuestionDialog && !allBots) {
        AlertDialog(
            onDismissRequest = { },
            confirmButton = {},
            title = { Text("Сколько дротиков ушло на закрытие лега?", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold) },
            text = {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf(1, 2, 3).forEach { n ->
                        Box(
                            modifier = Modifier.weight(1f).clip(RoundedCornerShape(10.dp)).background(Accent)
                                .clickable {
                                    chosenDarts = n
                                    showLegQuestionDialog = false
                                    showDoublesQuestionDialog = true
                                }.padding(vertical = 14.dp),
                            contentAlignment = Alignment.Center
                        ) { Text("$n", color = Color(0xFF121212), fontSize = 20.sp, fontWeight = FontWeight.Bold) }
                    }
                }
            }
        )
    }

    if (showDoublesQuestionDialog && !allBots) {
        val allowZero = game.outMode == OutMode.STRAIGHT_OUT
        AlertDialog(
            onDismissRequest = { },
            confirmButton = {},
            title = { Text("Сколько попыток в удвоение было?", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        listOf(0, 1, 2, 3).forEach { n ->
                            val btnEnabled = !(n == 0 && !allowZero)
                            Box(
                                modifier = Modifier.weight(1f)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(
                                        if (btnEnabled) Accent
                                        else Accent.copy(alpha = 0.25f)
                                    )
                                    .clickable(enabled = btnEnabled) {
                                        saveHistory()
                                        game = Game501Logic.closeLegManually(game, chosenDarts, n)
                                        showDoublesQuestionDialog = false
                                        chosenDarts = 0
                                    }
                                    .padding(vertical = 14.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    "$n",
                                    color = if (btnEnabled) Color(0xFF121212)
                                    else Color(0xFF121212).copy(alpha = 0.4f),
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                    Spacer(Modifier.height(10.dp))
                    Text(
                        if (allowZero)
                            "0 — если в этом подходе ты вообще не целился в удвоение"
                        else
                            "В этом режиме закрыть лег без попытки в дабл невозможно",
                        color = Color.White.copy(alpha = 0.6f), fontSize = 12.sp
                    )
                }
            }
        )
    }

    if (showDoublesOnlyDialog && !allBots) {
        AlertDialog(
            onDismissRequest = { },
            confirmButton = {},
            title = { Text("Сколько попыток в удвоение было?", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        listOf(0, 1, 2, 3).forEach { n ->
                            Box(
                                modifier = Modifier.weight(1f).clip(RoundedCornerShape(10.dp)).background(Accent)
                                    .clickable {
                                        saveHistory()
                                        game = Game501Logic.recordDoublesAttempts(game, n)
                                        game = Game501Logic.finishTurn(game)
                                        showDoublesOnlyDialog = false
                                    }.padding(vertical = 14.dp),
                                contentAlignment = Alignment.Center
                            ) { Text("$n", color = Color(0xFF121212), fontSize = 20.sp, fontWeight = FontWeight.Bold) }
                        }
                    }
                    Spacer(Modifier.height(10.dp))
                    Text("0 — если в этом подходе ты вообще не целился в удвоение",
                        color = Color.White.copy(alpha = 0.6f), fontSize = 12.sp)
                }
            }
        )
    }

    if (showLegWonDialog && !allBots) {
        AlertDialog(
            onDismissRequest = { },
            confirmButton = {
                TextButton(onClick = { showLegWonDialog = false }) { Text("Продолжить", color = Accent) }
            },
            title = {
                Column {
                    Text("ЛЕГ ЗАВЕРШЁН", color = Accent, fontSize = 22.sp, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.height(4.dp))
                    Text("Сет ${game.currentSetNumber}/${game.setsPerMatch}  •  Лег ${game.currentLegNumber}/${game.legsPerSet}",
                        color = Color.White.copy(alpha = 0.7f), fontSize = 12.sp)
                }
            },
            text = {
                Column {
                    val snapshot = game.legHistory.lastOrNull()
                    if (snapshot != null) {
                        snapshot.players.forEachIndexed { idx, p ->
                            if (idx > 0) Spacer(Modifier.height(8.dp))
                            val ppr = if (p.darts < 3) 0.0 else p.scoreGained.toDouble() / (p.darts / 3.0)
                            val dblPct = if (p.doublesAttempted > 0) p.doublesHit.toDouble() / p.doublesAttempted * 100.0 else 0.0
                            Column(modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(10.dp)).background(TileBg).padding(10.dp)) {
                                Text(p.name, color = Accent, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                                Spacer(Modifier.height(4.dp))
                                StatLine501("Средний набор", String.format(Locale.US, "%.2f", ppr))
                                StatLine501("Дротиков", p.darts.toString())
                                StatLine501("Удвоения", "${p.doublesHit} из ${p.doublesAttempted} (${String.format(Locale.US, "%.1f%%", dblPct)})")
                            }
                        }
                    }
                }
            }
        )
    }

    if (showSetWonDialog && !allBots) {
        AlertDialog(
            onDismissRequest = { },
            confirmButton = {
                TextButton(onClick = { showSetWonDialog = false }) { Text("Продолжить", color = Accent) }
            },
            title = { Text("СЕТ ЗАВЕРШЁН", color = GoldAccent, fontSize = 22.sp, fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    Text("Победитель:", color = Color.White, fontSize = 14.sp)
                    val winnerTeam = game.legHistory.lastOrNull()?.winningTeam ?: 0
                    Text(game.playersOfTeam(winnerTeam).joinToString("/") { it.name },
                        color = GoldAccent, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                }
            }
        )
    }

    if (showWinDialog && !allBots) {
        WinDialog501(
            game = game,
            onUndo = { undo(); showWinDialog = false },
            onContinue = {
                showWinDialog = false
                Game501SettingsStorage.updateQuickSumsIfNeeded(context)
                onGameFinish(game, currentId)
            }
        )
    }

    if (showBackConfirm) {
        AlertDialog(
            onDismissRequest = { showBackConfirm = false },
            confirmButton = {
                TextButton(onClick = {
                    showBackConfirm = false
                    onBack()
                }) { Text("Продолжить позже", color = Accent) }
            },
            dismissButton = {
                TextButton(onClick = {
                    showBackConfirm = false
                    // Удаляем сохранённую незавершённую партию и выходим
                    scope.launch {
                        if (currentId > 0L) repository.deleteGame(currentId)
                        onBack()
                    }
                }) { Text("Удалить партию", color = ErrorColor) }
            },
            title = { Text("Прервать игру?", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold) },
            text = {
                Text(
                    "Партия автоматически сохраняется. Ты можешь вернуться к ней позже " +
                    "из статистики — она будет отмечена красным.\n\n" +
                    "Либо удалить её навсегда.",
                    color = Color.White,
                    fontSize = 14.sp
                )
            }
        )
    }
}

@Composable
private fun PrevScoreBlock(
    label: String, value: Int?, isActive: Boolean, alignment: Alignment.Horizontal
) {
    val nameColor = if (isActive) Accent else Color.White.copy(alpha = 0.45f)
    val valueColor = if (isActive) Color.White else Color.White.copy(alpha = 0.55f)
    Column(modifier = Modifier.width(60.dp), horizontalAlignment = alignment) {
        Text(label, color = nameColor, fontSize = 10.sp)
        Text(if (value != null) "$value" else "—", color = valueColor, fontSize = 18.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun PlayersHeader501(game: Game501) {
    val activeTeam = game.currentPlayer?.teamIndex ?: -1
    val teamA = game.playersOfTeam(0)
    val teamB = game.playersOfTeam(1)

    val legsA = teamA.firstOrNull()?.legsInCurrentSet ?: 0
    val legsB = teamB.firstOrNull()?.legsInCurrentSet ?: 0
    val setsA = teamA.firstOrNull()?.setsWon ?: 0
    val setsB = teamB.firstOrNull()?.setsWon ?: 0

    Column(modifier = Modifier.fillMaxWidth().background(Color(0xFF16202C)).padding(vertical = 14.dp)) {
        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            PlayerScoreCol(
                name = teamA.joinToString("/") { it.name },
                score = if (game.isPairGame) teamA.minOfOrNull { it.score } ?: 0 else teamA.firstOrNull()?.score ?: 0,
                ppr = teamA.firstOrNull()?.let { Game501Logic.matchPpr(it) } ?: 0.0,
                legDarts = if (game.isPairGame) teamA.sumOf { it.legDarts } else teamA.firstOrNull()?.legDarts ?: 0,
                isActive = activeTeam == 0,
                modifier = Modifier.weight(1f)
            )
            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(horizontal = 8.dp)) {
                Text("СЕТ", color = Color(0xFF99AABB), fontSize = 10.sp, fontWeight = FontWeight.Bold)
                Text("$setsA : $setsB", color = Color.White.copy(alpha = 0.85f),
                    fontSize = 16.sp, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(4.dp))
                Text("ЛЕГ", color = Color(0xFF99AABB), fontSize = 14.sp, fontWeight = FontWeight.Bold)
                Text("$legsA : $legsB", color = Color.White, fontSize = 34.sp, fontWeight = FontWeight.Bold)
            }
            PlayerScoreCol(
                name = teamB.joinToString("/") { it.name },
                score = if (game.isPairGame) teamB.minOfOrNull { it.score } ?: 0 else teamB.firstOrNull()?.score ?: 0,
                ppr = teamB.firstOrNull()?.let { Game501Logic.matchPpr(it) } ?: 0.0,
                legDarts = if (game.isPairGame) teamB.sumOf { it.legDarts } else teamB.firstOrNull()?.legDarts ?: 0,
                isActive = activeTeam == 1,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun PlayerScoreCol(
    name: String, score: Int, ppr: Double, legDarts: Int,
    isActive: Boolean, modifier: Modifier
) {
    Column(modifier = modifier, horizontalAlignment = Alignment.CenterHorizontally) {
        Text(name, color = if (isActive) Accent else Color.White, fontSize = 20.sp,
            fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
        Text("$score", color = if (isActive) GoldAccent else Color.White,
            fontSize = 55.sp, fontWeight = FontWeight.Bold)
        Text("ср. ${String.format(Locale.US, "%.1f", ppr)}",
            color = Color.White.copy(alpha = 0.6f), fontSize = 15.sp)
        Spacer(Modifier.height(2.dp))
        Text("🎯 $legDarts", color = Color.White.copy(alpha = 0.75f), fontSize = 14.sp, fontWeight = FontWeight.Medium)
    }
}

@Composable
private fun TopBar501(game: Game501, onBack: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().background(Color(0xFF1A2332)).padding(horizontal = 12.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(game.modeLabel, color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold,
            modifier = Modifier.weight(1f))
        Spacer(Modifier.width(12.dp))
        Box(
            modifier = Modifier.clip(RoundedCornerShape(10.dp)).background(TileBg).clickable { onBack() }
                .padding(horizontal = 24.dp, vertical = 10.dp),
            contentAlignment = Alignment.Center
        ) { Text("← Назад", color = Accent, fontSize = 16.sp, fontWeight = FontWeight.Bold) }
    }
}

@Composable
private fun QuickButtonsWithLeg(
    sums: List<Int>, enabled: Boolean, onQuick: (Int) -> Unit, onLeg: () -> Unit
) {
    val rows = sums.chunked(6)
    Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 6.dp, vertical = 4.dp)) {
        rows.forEachIndexed { rowIdx, row ->
            Row(modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                row.forEach { sum ->
                    Box(
                        modifier = Modifier.weight(1f).height(48.dp).clip(RoundedCornerShape(8.dp))
                            .background(TileBg).clickable(enabled = enabled) { onQuick(sum) },
                        contentAlignment = Alignment.Center
                    ) { Text("$sum", color = Color.White, fontSize = 15.sp, fontWeight = FontWeight.Medium) }
                }
                if (rowIdx == rows.lastIndex) {
                    val freeSlots = 6 - row.size
                    if (freeSlots > 1) repeat(freeSlots - 1) { Spacer(Modifier.weight(1f)) }
                    Box(
                        modifier = Modifier.weight(1f).height(48.dp).clip(RoundedCornerShape(8.dp))
                            .background(if (enabled) ErrorColor else ErrorColor.copy(alpha = 0.4f))
                            .clickable(enabled = enabled) { onLeg() },
                        contentAlignment = Alignment.Center
                    ) { Text("ЛЕГ", color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold) }
                }
            }
        }
    }
}

@Composable
private fun BigActionButton(
    label: String, enabled: Boolean, color: Color, textColor: Color,
    modifier: Modifier, onClick: () -> Unit
) {
    Box(
        modifier = modifier.height(68.dp).clip(RoundedCornerShape(10.dp))
            .background(if (enabled) color else color.copy(alpha = 0.4f))
            .clickable(enabled = enabled) { onClick() },
        contentAlignment = Alignment.Center
    ) { Text(label, color = textColor, fontSize = 20.sp, fontWeight = FontWeight.Bold) }
}

@Composable
private fun Keyboard501(
    enabled: Boolean, canUndo: Boolean,
    onDigit: (String) -> Unit, onBackspace: () -> Unit,
    onClear: () -> Unit, onUndo: () -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 6.dp, vertical = 4.dp)) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            DigitKey("1", enabled, onDigit, Modifier.weight(1f))
            DigitKey("2", enabled, onDigit, Modifier.weight(1f))
            DigitKey("3", enabled, onDigit, Modifier.weight(1f))
        }
        Spacer(Modifier.height(6.dp))
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            DigitKey("4", enabled, onDigit, Modifier.weight(1f))
            DigitKey("5", enabled, onDigit, Modifier.weight(1f))
            DigitKey("6", enabled, onDigit, Modifier.weight(1f))
        }
        Spacer(Modifier.height(6.dp))
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            DigitKey("7", enabled, onDigit, Modifier.weight(1f))
            DigitKey("8", enabled, onDigit, Modifier.weight(1f))
            DigitKey("9", enabled, onDigit, Modifier.weight(1f))
        }
        Spacer(Modifier.height(6.dp))
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            Box(
                modifier = Modifier.weight(1f).height(60.dp).clip(RoundedCornerShape(10.dp))
                    .background(TileBgDark).clickable(enabled = enabled) { onClear() },
                contentAlignment = Alignment.Center
            ) { Text("C", color = ErrorColor, fontSize = 22.sp, fontWeight = FontWeight.Bold) }
            DigitKey("0", enabled, onDigit, Modifier.weight(1f))
            Box(
                modifier = Modifier.weight(1f).height(60.dp).clip(RoundedCornerShape(10.dp))
                    .background(TileBgDark).clickable(enabled = enabled) { onBackspace() },
                contentAlignment = Alignment.Center
            ) { Text("⌫", color = Color.White, fontSize = 24.sp, fontWeight = FontWeight.Bold) }
        }
        Spacer(Modifier.height(6.dp))
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            Spacer(Modifier.weight(1f))
            Box(
                modifier = Modifier.weight(1f).height(60.dp).clip(RoundedCornerShape(10.dp))
                    .background(if (canUndo) PaleYellow else PaleYellow.copy(alpha = 0.35f))
                    .clickable(enabled = canUndo) { onUndo() },
                contentAlignment = Alignment.Center
            ) {
                Text("↶", color = PaleYellowText, fontSize = 36.sp, fontWeight = FontWeight.Black)
            }
            Spacer(Modifier.weight(1f))
        }
        Spacer(Modifier.height(8.dp))
    }
}

@Composable
private fun DigitKey(digit: String, enabled: Boolean, onDigit: (String) -> Unit, modifier: Modifier) {
    Box(
        modifier = modifier.height(60.dp).clip(RoundedCornerShape(10.dp))
            .background(TileBg).clickable(enabled = enabled) { onDigit(digit) },
        contentAlignment = Alignment.Center
    ) { Text(digit, color = Color.White, fontSize = 24.sp, fontWeight = FontWeight.Bold) }
}

@Composable
private fun StatLine501(label: String, value: String) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(label, color = Color.White.copy(alpha = 0.8f), fontSize = 12.sp)
        Text(value, color = GoldAccent, fontSize = 13.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun WinDialog501(game: Game501, onUndo: () -> Unit, onContinue: () -> Unit) {
    val winnerTeam = game.winnerIndex
    val winnerName = if (winnerTeam != null) game.playersOfTeam(winnerTeam).joinToString("/") { it.name } else "—"
    val winner = if (winnerTeam != null) game.playersOfTeam(winnerTeam).firstOrNull() else null
    val ppr = winner?.let { Game501Logic.matchPpr(it) } ?: 0.0
    val dbl = winner?.let { Game501Logic.doublesAccuracy(it) } ?: 0.0

    AlertDialog(
        onDismissRequest = { },
        confirmButton = { TextButton(onClick = onContinue) { Text("Продолжить", color = Accent) } },
        dismissButton = { TextButton(onClick = onUndo) { Text("↶ Отменить", color = ErrorColor) } },
        title = { Text("ПОБЕДА В МАТЧЕ!", color = Accent, fontSize = 22.sp, fontWeight = FontWeight.Bold) },
        text = {
            Column {
                Text(winnerName, color = GoldAccent, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(8.dp))
                Text("Сеты: ${winner?.setsWon ?: 0} из ${game.setsPerMatch}", color = Color.White, fontSize = 14.sp)
                Text("Средний набор: ${String.format(Locale.US, "%.2f", ppr)}", color = Color.White, fontSize = 14.sp)
                Text("Точность удвоений: ${String.format(Locale.US, "%.1f%%", dbl)}", color = Color.White, fontSize = 14.sp)
            }
        }
    )
}
