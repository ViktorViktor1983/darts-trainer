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
import java.util.Locale
import kotlin.random.Random

@Composable
fun Game501Screen(
    initialGame: Game501,
    onGameFinish: (Game501) -> Unit,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    var game by remember { mutableStateOf(initialGame) }
    var inputText by remember { mutableStateOf("") }
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

    val history = remember { mutableStateListOf<Game501>() }

    fun saveHistory() { history.add(game); if (history.size > 300) history.removeAt(0) }
    fun undo() { if (history.isNotEmpty()) game = history.removeAt(history.lastIndex) }

    LaunchedEffect(Unit) { quickSums = Game501SettingsStorage.getQuickSums(context) }

    LaunchedEffect(recordSum) {
        val s = recordSum
        if (s != null && s > 0) Game501SettingsStorage.recordHumanSum(context, s)
        recordSum = null
    }

    // ─────────────────────────────────────────────
    // Обработка суммы за подход
    // ─────────────────────────────────────────────
    fun applySum(value: Int) {
        if (value < 0 || value > 180) return
        val currentPlayer = game.players.getOrNull(game.currentPlayerIndex) ?: return
        if (currentPlayer.isBot || game.isFinished) return

        saveHistory()

        val scoreBefore = currentPlayer.score

        // Закрытие лега (сумма равна остатку)
        if (value == scoreBefore && value > 0) {
            chosenDarts = 0
            showLegQuestionDialog = true
            return
        }

        // Перебор (сумма больше остатка)
        if (value > scoreBefore) {
            val updated = Game501Logic.applyTurnScore(game, value)
            showBustMessage = true
            game = updated
            if (scoreBefore <= 50) {
                showDoublesOnlyDialog = true
            } else {
                game = Game501Logic.finishTurn(game)
            }
            return
        }

        // Обычный ввод (в т.ч. 0)
        val updated = Game501Logic.applyTurnScore(game, value)
        if (!updated.isFinished) recordSum = value
        game = updated

        if (scoreBefore <= 50) {
            showDoublesOnlyDialog = true
        } else {
            game = Game501Logic.finishTurn(game)
        }
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

    // ─────────────────────────────────────────────
    // Ввод остатка
    // ─────────────────────────────────────────────
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
            if (scoreBefore <= 50) {
                showDoublesOnlyDialog = true
            } else {
                game = Game501Logic.finishTurn(game)
            }
            return
        }

        val updated = Game501Logic.applyTurnRemaining(game, value)
        recordSum = gained
        game = updated

        if (scoreBefore <= 50) {
            showDoublesOnlyDialog = true
        } else {
            game = Game501Logic.finishTurn(game)
        }
    }

    fun pressLeg() {
        val currentPlayer = game.players.getOrNull(game.currentPlayerIndex) ?: return
        if (currentPlayer.isBot || game.isFinished) return
        chosenDarts = 0
        showLegQuestionDialog = true
    }

    // Автоход бота
    LaunchedEffect(game.currentPlayerIndex, game.isFinished, showLegWonDialog, showSetWonDialog, showWinDialog, showLegQuestionDialog, showDoublesQuestionDialog, showDoublesOnlyDialog) {
        val currentPlayer = game.players.getOrNull(game.currentPlayerIndex) ?: return@LaunchedEffect
        if (game.isFinished || showLegWonDialog || showSetWonDialog || showWinDialog || showLegQuestionDialog || showDoublesQuestionDialog || showDoublesOnlyDialog) return@LaunchedEffect
        if (!currentPlayer.isBot) return@LaunchedEffect
        delay(900L)
        if (game.isFinished || showLegWonDialog || showSetWonDialog || showWinDialog || showLegQuestionDialog || showDoublesQuestionDialog || showDoublesOnlyDialog) return@LaunchedEffect
        if (game.players.getOrNull(game.currentPlayerIndex)?.isBot != true) return@LaunchedEffect

        saveHistory()
        game = botPerformTurn(game)
    }

    LaunchedEffect(showBustMessage) {
        if (showBustMessage) { delay(1500L); showBustMessage = false }
    }

    LaunchedEffect(game.lastLegWinnerIndex) {
        if (game.lastLegWinnerIndex != null) showLegWonDialog = true
    }
    LaunchedEffect(game.lastSetWinnerIndex, showLegWonDialog) {
        if (game.lastSetWinnerIndex != null && !showLegWonDialog && !showWinDialog) showSetWonDialog = true
    }
    LaunchedEffect(game.isFinished, showLegWonDialog, showSetWonDialog) {
        if (game.isFinished && !showLegWonDialog && !showSetWonDialog && !showWinDialog) showWinDialog = true
    }

    Column(modifier = Modifier.fillMaxSize().background(Color(0xFF0D1117))) {
        TopBar501(game = game, onBack = { showBackConfirm = true })
        PlayersHeader501(game = game)

        Row(
            modifier = Modifier.fillMaxWidth().background(Color(0xFF16202C))
                .padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                if (inputText.isEmpty()) "—" else inputText,
                color = if (inputText.isEmpty()) Color.White.copy(alpha = 0.3f) else GoldAccent,
                fontSize = 32.sp, fontWeight = FontWeight.Bold,
                modifier = Modifier.weight(1f), textAlign = TextAlign.Center
            )
        }

        QuickButtons(
            sums = quickSums,
            enabled = !game.isFinished && game.currentPlayer?.isBot != true,
            onPress = { pressQuickButton(it) }
        )

        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            BigActionButton(
                label = "Остаток",
                enabled = !game.isFinished && game.currentPlayer?.isBot != true,
                color = TileBg, textColor = Accent, modifier = Modifier.weight(1f)
            ) { submitRemaining() }
            BigActionButton(
                label = "ЛЕГ",
                enabled = !game.isFinished && game.currentPlayer?.isBot != true,
                color = ErrorColor, textColor = Color.White, modifier = Modifier.weight(1f)
            ) { pressLeg() }
        }

        Keyboard501(
            enabled = !game.isFinished && game.currentPlayer?.isBot != true,
            onDigit = { inputText = if (inputText.length < 3) inputText + it else inputText },
            onBackspace = { inputText = if (inputText.isNotEmpty()) inputText.dropLast(1) else "" },
            onClear = { inputText = "" },
            onOk = { submitSum() }
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

    // ДИАЛОГ 1: Сколько дротиков?
    if (showLegQuestionDialog) {
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

    // ДИАЛОГ 2: Сколько попыток в удвоение? (при закрытии)
    if (showDoublesQuestionDialog) {
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
                                        game = Game501Logic.closeLegManually(game, chosenDarts, n)
                                        showDoublesQuestionDialog = false
                                        chosenDarts = 0
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

    // ДИАЛОГ попыток в удвоение (без закрытия — при остатке ≤ 50)
    if (showDoublesOnlyDialog) {
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

    if (showLegWonDialog) {
        AlertDialog(
            onDismissRequest = { },
            confirmButton = {
                TextButton(onClick = {
                    showLegWonDialog = false
                    game = game.copy(lastLegWinnerIndex = null)
                }) { Text("Продолжить", color = Accent) }
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

    if (showSetWonDialog) {
        AlertDialog(
            onDismissRequest = { },
            confirmButton = {
                TextButton(onClick = {
                    showSetWonDialog = false
                    game = game.copy(lastSetWinnerIndex = null)
                }) { Text("Продолжить", color = Accent) }
            },
            title = { Text("СЕТ ЗАВЕРШЁН", color = GoldAccent, fontSize = 22.sp, fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    Text("Победитель:", color = Color.White, fontSize = 14.sp)
                    val winnerTeam = game.lastSetWinnerIndex ?: 0
                    Text(game.playersOfTeam(winnerTeam).joinToString("/") { it.name },
                        color = GoldAccent, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                }
            }
        )
    }

    if (showWinDialog) {
        WinDialog501(
            game = game,
            onUndo = { undo(); showWinDialog = false },
            onContinue = {
                showWinDialog = false
                Game501SettingsStorage.updateQuickSumsIfNeeded(context)
                onGameFinish(game)
            }
        )
    }

    if (showBackConfirm) {
        AlertDialog(
            onDismissRequest = { showBackConfirm = false },
            confirmButton = {
                TextButton(onClick = { showBackConfirm = false; onBack() }) { Text("ДА", color = ErrorColor) }
            },
            dismissButton = {
                TextButton(onClick = { showBackConfirm = false }) { Text("НЕТ", color = Accent) }
            },
            title = { Text("Выйти из игры?", color = Color.White) },
            text = { Text("Прогресс не сохранится.", color = Color.White) }
        )
    }
}

// ─────────────────────────────────────────────
// Ход бота
// ─────────────────────────────────────────────
private fun botPerformTurn(game: Game501): Game501 {
    val player = game.currentPlayer ?: return game
    val botLevel = player.botLevel
    val requiresDouble = game.outMode == OutMode.DOUBLE_OUT || game.outMode == OutMode.DOUBLE_IN_OUT

    // Если остаток ≤ 50 — бот пытается закрыть
    if (requiresDouble && player.score <= 50) {
        val closeChance = 0.15 + (botLevel - 1) * 0.025
        if (Random.nextDouble() < closeChance) {
            val darts = Random.nextInt(1, 4)
            val attempts = Random.nextInt(1, darts + 1)
            return Game501Logic.closeLegManually(game, darts, attempts)
        }
    }

    val scoreBefore = player.score
    val gained = generateBotTurnScore(botLevel)
    val capped = gained.coerceAtMost(player.score + 1)

    val updated = Game501Logic.applyTurnScore(game, capped)
    var result = updated

    if (scoreBefore <= 50 && !result.isFinished) {
        val attempts = Random.nextInt(0, 4)
        result = Game501Logic.recordDoublesAttempts(result, attempts)
    }

    if (!result.isFinished) result = Game501Logic.finishTurn(result)
    return result
}

private fun generateBotTurnScore(botLevel: Int): Int {
    val lvl = botLevel.coerceIn(1, 16)
    val basePpr = when (lvl) {
        1 -> 24; 2 -> 31; 3 -> 38; 4 -> 45
        5 -> 51; 6 -> 57; 7 -> 62; 8 -> 67
        9 -> 72; 10 -> 77; 11 -> 82; 12 -> 87
        13 -> 92; 14 -> 97; 15 -> 102; else -> 110
    }
    val min = (basePpr * 0.5).toInt()
    val max = (basePpr * 1.4).toInt()
    return Random.nextInt(min, max + 1).coerceIn(0, 180)
}

// ─────────────────────────────────────────────
// Шапка игроков
// ─────────────────────────────────────────────
@Composable
private fun PlayersHeader501(game: Game501) {
    val activeTeam = game.currentPlayer?.teamIndex ?: -1
    val teamA = game.playersOfTeam(0)
    val teamB = game.playersOfTeam(1)

    Column(modifier = Modifier.fillMaxWidth().background(Color(0xFF16202C)).padding(vertical = 10.dp)) {
        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            PlayerScoreCol(
                name = teamA.joinToString("/") { it.name },
                score = if (game.isPairGame) teamA.minOfOrNull { it.score } ?: 0 else teamA.firstOrNull()?.score ?: 0,
                ppr = teamA.firstOrNull()?.let { Game501Logic.matchPpr(it) } ?: 0.0,
                isActive = activeTeam == 0,
                modifier = Modifier.weight(1f)
            )
            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(horizontal = 8.dp)) {
                Text("ЛЕГ", color = Color(0xFF99AABB), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                Text("${teamA.firstOrNull()?.legsInCurrentSet ?: 0} : ${teamB.firstOrNull()?.legsInCurrentSet ?: 0}",
                    color = Color.White, fontSize = 26.sp, fontWeight = FontWeight.Bold)
            }
            PlayerScoreCol(
                name = teamB.joinToString("/") { it.name },
                score = if (game.isPairGame) teamB.minOfOrNull { it.score } ?: 0 else teamB.firstOrNull()?.score ?: 0,
                ppr = teamB.firstOrNull()?.let { Game501Logic.matchPpr(it) } ?: 0.0,
                isActive = activeTeam == 1,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun PlayerScoreCol(
    name: String, score: Int, ppr: Double, isActive: Boolean, modifier: Modifier
) {
    Column(modifier = modifier, horizontalAlignment = Alignment.CenterHorizontally) {
        Text(name, color = if (isActive) Accent else Color.White, fontSize = 16.sp,
            fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
        Text("$score", color = if (isActive) GoldAccent else Color.White,
            fontSize = 44.sp, fontWeight = FontWeight.Bold)
        Text("ср. ${String.format(Locale.US, "%.1f", ppr)}",
            color = Color.White.copy(alpha = 0.6f), fontSize = 12.sp)
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
private fun QuickButtons(sums: List<Int>, enabled: Boolean, onPress: (Int) -> Unit) {
    val rows = sums.chunked(6)
    Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 6.dp, vertical = 4.dp)) {
        rows.forEach { row ->
            Row(modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                row.forEach { sum ->
                    Box(
                        modifier = Modifier.weight(1f).height(48.dp).clip(RoundedCornerShape(8.dp))
                            .background(TileBg).clickable(enabled = enabled) { onPress(sum) },
                        contentAlignment = Alignment.Center
                    ) { Text("$sum", color = Color.White, fontSize = 15.sp, fontWeight = FontWeight.Medium) }
                }
                if (row.size < 6) repeat(6 - row.size) { Spacer(Modifier.weight(1f)) }
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
        modifier = modifier.height(52.dp).clip(RoundedCornerShape(10.dp))
            .background(if (enabled) color else color.copy(alpha = 0.4f))
            .clickable(enabled = enabled) { onClick() },
        contentAlignment = Alignment.Center
    ) { Text(label, color = textColor, fontSize = 17.sp, fontWeight = FontWeight.Bold) }
}

@Composable
private fun Keyboard501(
    enabled: Boolean, onDigit: (String) -> Unit, onBackspace: () -> Unit,
    onClear: () -> Unit, onOk: () -> Unit
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
                modifier = Modifier.weight(1f).height(56.dp).clip(RoundedCornerShape(10.dp))
                    .background(TileBgDark).clickable(enabled = enabled) { onClear() },
                contentAlignment = Alignment.Center
            ) { Text("C", color = ErrorColor, fontSize = 20.sp, fontWeight = FontWeight.Bold) }
            DigitKey("0", enabled, onDigit, Modifier.weight(1f))
            Box(
                modifier = Modifier.weight(1f).height(56.dp).clip(RoundedCornerShape(10.dp))
                    .background(TileBgDark).clickable(enabled = enabled) { onBackspace() },
                contentAlignment = Alignment.Center
            ) { Text("⌫", color = Color.White, fontSize = 22.sp, fontWeight = FontWeight.Bold) }
        }
        Spacer(Modifier.height(8.dp))
        Box(
            modifier = Modifier.fillMaxWidth().height(60.dp).clip(RoundedCornerShape(12.dp))
                .background(if (enabled) Accent else TileBgDark)
                .clickable(enabled = enabled) { onOk() },
            contentAlignment = Alignment.Center
        ) {
            Text("OK", color = if (enabled) Color(0xFF121212) else Accent.copy(alpha = 0.5f),
                fontSize = 22.sp, fontWeight = FontWeight.Bold)
        }
        Spacer(Modifier.height(8.dp))
    }
}

@Composable
private fun DigitKey(digit: String, enabled: Boolean, onDigit: (String) -> Unit, modifier: Modifier) {
    Box(
        modifier = modifier.height(56.dp).clip(RoundedCornerShape(10.dp))
            .background(TileBg).clickable(enabled = enabled) { onDigit(digit) },
        contentAlignment = Alignment.Center
    ) { Text(digit, color = Color.White, fontSize = 22.sp, fontWeight = FontWeight.Bold) }
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
