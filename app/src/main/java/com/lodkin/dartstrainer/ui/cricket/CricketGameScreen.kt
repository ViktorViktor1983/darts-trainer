package com.lodkin.dartstrainer.ui.cricket

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.lodkin.dartstrainer.data.cricket.*
import com.lodkin.dartstrainer.theme.Accent
import com.lodkin.dartstrainer.theme.ErrorColor
import com.lodkin.dartstrainer.theme.GoldAccent
import com.lodkin.dartstrainer.theme.TileBg
import com.lodkin.dartstrainer.theme.TileBgDark
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.Locale

// ─────────────────────────────────────────────
// Нормативы американского крикета
// ─────────────────────────────────────────────
private data class CricketNorm(val name: String, val maxDarts: Int)

private val AMERICAN_CRICKET_NORMS: List<CricketNorm> = listOf(
    CricketNorm("КМС", 24),
    CricketNorm("I разряд", 36),
    CricketNorm("II разряд", 48),
    CricketNorm("III разряд", 57),
    CricketNorm("I юношеский", 62),
    CricketNorm("II юношеский", 67),
    CricketNorm("III юношеский", 72)
)

@Composable
fun CricketGameScreen(
    repository: CricketRepository,
    initialGame: CricketGame,
    existingId: Long = 0L,
    onGameFinish: (CricketGame, Long) -> Unit,
    onBack: () -> Unit
) {
    val scope = rememberCoroutineScope()
    var game by remember { mutableStateOf(initialGame) }
    var currentId by remember { mutableStateOf(existingId) }

    var showWinDialog by remember { mutableStateOf(false) }
    var showBackConfirm by remember { mutableStateOf(false) }
    var showLegWonDialog by remember { mutableStateOf(false) }
    var showSetWonDialog by remember { mutableStateOf(false) }
    var legWinnerLabel by remember { mutableStateOf("") }
    var setWinnerLabel by remember { mutableStateOf("") }
    var dartsSelected by remember { mutableStateOf(0) }

    // Флаг блокировки ОК после автоперехода хода
    var okBlocked by remember { mutableStateOf(false) }

    val history = remember { mutableStateListOf<CricketGame>() }

    fun saveHistory() { history.add(game); if (history.size > 300) history.removeAt(0) }
    fun undo() { if (history.isNotEmpty()) game = history.removeAt(history.lastIndex) }

    LaunchedEffect(game) {
        if (!game.isFinished) {
            currentId = repository.saveUnfinishedGame(game, currentId)
        }
    }

    fun applyDartsCorrection(g: CricketGame, realDarts: Int): CricketGame {
        val winnerIdx = g.lastLegWinnerPlayerIndex ?: return g
        if (winnerIdx !in g.players.indices) return g
        val diff = realDarts - g.lastLegDartsClicked
        if (diff == 0) return g
        val updated = g.players.toMutableList()
        val player = updated[winnerIdx]
        updated[winnerIdx] = player.copy(
            dartsThrown = (player.dartsThrown + diff).coerceAtLeast(0),
            matchDartsThrown = (player.matchDartsThrown + diff).coerceAtLeast(0),
            legMisses = if (diff > 0) player.legMisses + diff else (player.legMisses + diff).coerceAtLeast(0),
            matchMissesThrown = if (diff > 0) player.matchMissesThrown + diff else (player.matchMissesThrown + diff).coerceAtLeast(0)
        )
        val newHistory = if (g.legHistory.isNotEmpty()) {
            val last = g.legHistory.last()
            val newPlayers = last.players.mapIndexed { i, snap ->
                if (i == winnerIdx) snap.copy(
                    darts = (snap.darts + diff).coerceAtLeast(0),
                    misses = if (diff > 0) snap.misses + diff else (snap.misses + diff).coerceAtLeast(0)
                ) else snap
            }
            g.legHistory.dropLast(1) + last.copy(players = newPlayers)
        } else g.legHistory
        return g.copy(players = updated, legHistory = newHistory)
    }

    LaunchedEffect(game.currentPlayerIndex, game.isFinished, showLegWonDialog, showSetWonDialog, showWinDialog) {
        val currentPlayer = game.players.getOrNull(game.currentPlayerIndex) ?: return@LaunchedEffect
        if (game.isFinished || showLegWonDialog || showSetWonDialog || showWinDialog) return@LaunchedEffect
        if (!currentPlayer.isBot) return@LaunchedEffect
        delay(700L)
        if (game.isFinished || showLegWonDialog || showSetWonDialog || showWinDialog) return@LaunchedEffect
        if (game.players.getOrNull(game.currentPlayerIndex)?.isBot != true) return@LaunchedEffect
        saveHistory()
        game = CricketBotAI.performTurn(game, game.currentPlayerIndex)
    }

    LaunchedEffect(game.currentTurnDarts, game.currentPlayerIndex, game.isFinished, showLegWonDialog, showSetWonDialog, showWinDialog) {
        if (game.autoOkSeconds <= 0 || game.isFinished) return@LaunchedEffect
        if (showLegWonDialog || showSetWonDialog || showWinDialog) return@LaunchedEffect
        val currentPlayer = game.players.getOrNull(game.currentPlayerIndex) ?: return@LaunchedEffect
        if (currentPlayer.isBot) return@LaunchedEffect
        if (game.currentTurnDarts <= 0 || game.currentTurnDarts >= 3) return@LaunchedEffect
        delay(game.autoOkSeconds * 1000L)
        if (game.isFinished || showLegWonDialog || showSetWonDialog || showWinDialog) return@LaunchedEffect
        if (game.players.getOrNull(game.currentPlayerIndex)?.isBot != false) return@LaunchedEffect
        if (game.currentTurnDarts <= 0 || game.currentTurnDarts >= 3) return@LaunchedEffect
        saveHistory()
        game = CricketLogic.finishTurn(game)
    }

    // ── Блокировка ОК после автоперехода хода ──
    // Следим за сменой игрока: если ход перешёл (currentPlayerIndex изменился),
    // и это произошло «само» (после 3-го дротика), блокируем ОК на 3 секунды.
    var lastCurrentPlayerIndex by remember { mutableStateOf(game.currentPlayerIndex) }
    LaunchedEffect(game.currentPlayerIndex, game.currentTurnDarts) {
        if (game.currentPlayerIndex != lastCurrentPlayerIndex) {
            // Ход перешёл — блокируем ОК на 3 секунды
            okBlocked = true
            lastCurrentPlayerIndex = game.currentPlayerIndex
            delay(3000L)
            okBlocked = false
        }
    }

    LaunchedEffect(game.lastLegWinnerIndex) {
        val teamIdx = game.lastLegWinnerIndex
        if (teamIdx != null) {
            legWinnerLabel = teamName(game, teamIdx)
            dartsSelected = 0
            showLegWonDialog = true
        }
    }

    LaunchedEffect(game.lastSetWinnerIndex, showLegWonDialog) {
        val teamIdx = game.lastSetWinnerIndex
        if (teamIdx != null && !showLegWonDialog && !showWinDialog) {
            setWinnerLabel = teamName(game, teamIdx)
            showSetWonDialog = true
        }
    }

    LaunchedEffect(game.isFinished, showLegWonDialog, showSetWonDialog) {
        if (game.isFinished && !showLegWonDialog && !showSetWonDialog && !showWinDialog) {
            showWinDialog = true
        }
    }

    Column(modifier = Modifier.fillMaxSize().background(Color(0xFF0D1117))) {
        TopBar(game = game, onBack = { showBackConfirm = true })
        ParamsRow(game = game)
        TeamsHeaderRow(game = game)
        ScoreControlRow(
            game = game,
            canUndo = history.isNotEmpty(),
            okBlocked = okBlocked,
            onUndo = { undo() },
            onOk = {
                val currentPlayer = game.players.getOrNull(game.currentPlayerIndex)
                if (!game.isFinished && currentPlayer?.isBot != true && !okBlocked) {
                    saveHistory()
                    game = CricketLogic.finishTurn(game)
                }
            }
        )
        Column(modifier = Modifier.fillMaxWidth().weight(1f)) {
            CricketSector.ALL.forEach { sector ->
                Box(modifier = Modifier.fillMaxWidth().weight(1f)) {
                    SectorRow(
                        sector = sector, game = game,
                        onThrow = { result ->
                            val currentPlayer = game.players.getOrNull(game.currentPlayerIndex)
                            if (currentPlayer?.isBot == true) return@SectorRow
                            if (game.currentTurnDarts >= 3) return@SectorRow
                            saveHistory()
                            var updated = CricketLogic.applyThrow(
                                game = game, sector = sector, result = result,
                                playerIndex = game.currentPlayerIndex
                            )
                            if (updated.currentTurnDarts >= 3 && !updated.isFinished) {
                                updated = CricketLogic.nextPlayer(updated)
                            }
                            game = updated
                        }
                    )
                }
            }
        }
    }

    if (showLegWonDialog) {
        val winnerTeam = game.lastLegWinnerIndex ?: 0
        val humanWon = game.playersOfTeam(winnerTeam).any { !it.isBot }
        val needsAnswer = humanWon && dartsSelected == 0

        AlertDialog(
            onDismissRequest = { },
            confirmButton = {
                if (!needsAnswer) {
                    TextButton(onClick = {
                        showLegWonDialog = false
                        game = game.copy(lastLegWinnerIndex = null)
                    }) { Text("Продолжить", color = Accent) }
                }
            },
            title = {
                Column {
                    Text("ЛЕГ ЗАВЕРШЁН", color = Accent, fontSize = 22.sp, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.height(4.dp))
                    Text(
                        "Сет ${game.currentSetNumber}/${game.setsPerMatch}  •  Лег ${game.currentLegNumber}/${game.legsPerSet}",
                        color = Color.White.copy(alpha = 0.7f), fontSize = 12.sp
                    )
                }
            },
            text = {
                Column {
                    Text("Победитель:", color = Color.White, fontSize = 14.sp)
                    Text(legWinnerLabel, color = GoldAccent, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.height(12.dp))

                    if (needsAnswer) {
                        Text(
                            "Сколько дротиков ушло на закрытие лега?",
                            color = Color.White, fontSize = 15.sp, fontWeight = FontWeight.Medium
                        )
                        Spacer(Modifier.height(12.dp))
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            listOf(1, 2, 3).forEach { n ->
                                Box(
                                    modifier = Modifier.weight(1f)
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(Accent)
                                        .clickable {
                                            dartsSelected = n
                                            game = applyDartsCorrection(game, n)
                                        }
                                        .padding(vertical = 14.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text("$n", color = Color(0xFF121212), fontSize = 20.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    } else {
                        val snapshot = game.legHistory.lastOrNull()
                        if (snapshot != null) LegStatsDisplay(snapshot = snapshot)
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
                    Text(setWinnerLabel, color = GoldAccent, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.height(12.dp))
                    Text("Счёт по сетам:", color = Color.White, fontSize = 14.sp)
                    TeamScoresList(game, showLegs = false)
                }
            }
        )
    }

    if (showWinDialog) {
        WinDialog(
            game = game,
            onUndo = { undo(); showWinDialog = false },
            onContinue = {
                showWinDialog = false
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
private fun LegStatsDisplay(snapshot: LegSnapshot) {
    Column {
        snapshot.players.forEachIndexed { idx, p ->
            if (idx > 0) Spacer(Modifier.height(8.dp))
            val mpr = if (p.darts < 3) 0.0 else p.legMarks.toDouble() / (p.darts / 3.0)
            val missPct = if (p.darts <= 0) 0.0 else p.misses.toDouble() / p.darts * 100.0
            val triplesPct = if (p.darts <= 0) 0.0 else p.triples.toDouble() / p.darts * 100.0
            Column(
                modifier = Modifier.fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(TileBg).padding(10.dp)
            ) {
                Text(p.name, color = Accent, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(4.dp))
                StatLine("Средний набор", String.format(Locale.US, "%.2f", mpr))
                StatLine("Промахи", String.format(Locale.US, "%.1f%%", missPct))
                StatLine("Утроения", String.format(Locale.US, "%.1f%%", triplesPct))
                StatLine("Очки", p.score.toString())
                StatLine("Дротиков", p.darts.toString())
            }
        }
    }
}

@Composable
private fun StatLine(label: String, value: String) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(label, color = Color.White.copy(alpha = 0.8f), fontSize = 12.sp)
        Text(value, color = GoldAccent, fontSize = 13.sp, fontWeight = FontWeight.Bold)
    }
}

private fun teamName(game: CricketGame, teamIdx: Int): String =
    game.playersOfTeam(teamIdx).joinToString("/") { it.name }

@Composable
private fun TeamScoresList(game: CricketGame, showLegs: Boolean) {
    game.players.forEach { p ->
        val value = if (showLegs) p.legsInCurrentSet else p.setsWon
        Text("${p.name}: $value", color = Color.White, fontSize = 13.sp)
    }
}

@Composable
private fun TopBar(game: CricketGame, onBack: () -> Unit) {
    val modeLabel = if (game.type == CricketType.AMERICAN) "Американский" else "Без набора"
    Row(
        modifier = Modifier.fillMaxWidth().background(Color(0xFF1A2332))
            .padding(horizontal = 12.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text("Крикет ($modeLabel)", color = Color.White, fontSize = 20.sp,
            fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
        Spacer(Modifier.width(12.dp))
        Box(
            modifier = Modifier.clip(RoundedCornerShape(10.dp)).background(TileBg)
                .clickable { onBack() }.padding(horizontal = 24.dp, vertical = 10.dp),
            contentAlignment = Alignment.Center
        ) { Text("← Назад", color = Accent, fontSize = 16.sp, fontWeight = FontWeight.Bold) }
    }
}

@Composable
private fun ParamsRow(game: CricketGame) {
    Row(
        modifier = Modifier.fillMaxWidth().background(Color(0xFF1A2332))
            .padding(horizontal = 12.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            "Параметры игры (Сеты до ${game.setsPerMatch}, Леги до ${game.legsPerSet})",
            color = Color(0xFFCCDDEE), fontSize = 14.sp
        )
    }
}

@Composable
private fun TeamsHeaderRow(game: CricketGame) {
    val currentIdx = game.currentPlayerIndex
    val currentPlayer = game.players.getOrNull(currentIdx)
    val activeTeam = currentPlayer?.teamIndex ?: -1
    val teamAName = if (activeTeam == 0 && currentPlayer != null) currentPlayer.name
        else game.playersOfTeam(0).joinToString("/") { it.name }
    val teamBName = if (activeTeam == 1 && currentPlayer != null) currentPlayer.name
        else game.playersOfTeam(1).joinToString("/") { it.name }
    val legsA = game.playersOfTeam(0).firstOrNull()?.legsInCurrentSet ?: 0
    val legsB = game.playersOfTeam(1).firstOrNull()?.legsInCurrentSet ?: 0
    val setsA = game.playersOfTeam(0).firstOrNull()?.setsWon ?: 0
    val setsB = game.playersOfTeam(1).firstOrNull()?.setsWon ?: 0
    val avgA = avgPerLeg(game, 0)
    val avgB = avgPerLeg(game, 1)

    Column(modifier = Modifier.fillMaxWidth().background(Color(0xFF16202C))
        .padding(horizontal = 10.dp, vertical = 16.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            SetBadge(setsA)
            Spacer(Modifier.width(8.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(teamAName, color = if (activeTeam == 0) Accent else Color.White,
                    fontSize = 22.sp, fontWeight = FontWeight.Bold, maxLines = 1,
                    overflow = TextOverflow.Ellipsis)
                Text("ср. %.2f".format(Locale.US, avgA), color = Color(0xFF99AABB), fontSize = 18.sp)
            }
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("ЛЕГ", color = Color(0xFF99AABB), fontSize = 14.sp, fontWeight = FontWeight.Bold)
                Text("$legsA : $legsB", color = Color.White, fontSize = 34.sp, fontWeight = FontWeight.Bold)
            }
            Column(modifier = Modifier.weight(1f), horizontalAlignment = Alignment.End) {
                Text(teamBName, color = if (activeTeam == 1) Accent else Color.White,
                    fontSize = 22.sp, fontWeight = FontWeight.Bold, maxLines = 1,
                    overflow = TextOverflow.Ellipsis, textAlign = TextAlign.End)
                Text("ср. %.2f".format(Locale.US, avgB), color = Color(0xFF99AABB), fontSize = 18.sp)
            }
            Spacer(Modifier.width(8.dp))
            SetBadge(setsB)
        }
    }
}

@Composable
private fun SetBadge(sets: Int) {
    Column(
        modifier = Modifier.clip(RoundedCornerShape(8.dp)).background(TileBgDark)
            .padding(horizontal = 12.dp, vertical = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("СЕТ", color = Accent, fontSize = 14.sp, fontWeight = FontWeight.Bold)
        Text("$sets", color = Color.White, fontSize = 23.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun ScoreControlRow(
    game: CricketGame,
    canUndo: Boolean,
    okBlocked: Boolean,
    onUndo: () -> Unit,
    onOk: () -> Unit
) {
    val currentPlayer = game.players.getOrNull(game.currentPlayerIndex)
    val isBotTurn = currentPlayer?.isBot == true
    val dartsA = game.playersOfTeam(0).sumOf { it.dartsThrown }
    val dartsB = game.playersOfTeam(1).sumOf { it.dartsThrown }
    val scoreA = CricketLogic.teamTotalScore(game, 0)
    val scoreB = CricketLogic.teamTotalScore(game, 1)
    val diffA = scoreA - scoreB
    val diffB = -diffA

    val okEnabled = !game.isFinished && !isBotTurn && !okBlocked

    Row(modifier = Modifier.fillMaxWidth().background(Color(0xFF16202C))
        .padding(horizontal = 8.dp, vertical = 6.dp), verticalAlignment = Alignment.CenterVertically) {
        Text("$dartsA", color = Color.White, fontSize = 18.sp,
            fontWeight = FontWeight.Bold, modifier = Modifier.width(28.dp))
        Spacer(Modifier.width(4.dp))
        UndoButton(enabled = canUndo, onClick = onUndo)
        Spacer(Modifier.width(4.dp))
        ScoreDiffBadge(text = if (diffA >= 0) "+$diffA" else "$diffA", positive = diffA >= 0)
        Spacer(Modifier.weight(1f))
        Box(
            modifier = Modifier.clip(RoundedCornerShape(8.dp))
                .background(if (okEnabled) Accent else TileBgDark)
                .clickable(enabled = okEnabled) { onOk() }
                .padding(horizontal = 32.dp, vertical = 10.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(if (isBotTurn) "..." else "OK",
                color = if (okEnabled) Color(0xFF121212) else Accent.copy(alpha = 0.5f),
                fontSize = 18.sp, fontWeight = FontWeight.Bold)
        }
        Spacer(Modifier.weight(1f))
        ScoreDiffBadge(text = if (diffB >= 0) "+$diffB" else "$diffB", positive = diffB >= 0)
        Spacer(Modifier.width(4.dp))
        UndoButton(enabled = canUndo, onClick = onUndo)
        Spacer(Modifier.width(4.dp))
        Text("$dartsB", color = Color.White, fontSize = 18.sp,
            fontWeight = FontWeight.Bold, modifier = Modifier.width(28.dp), textAlign = TextAlign.End)
    }
}

@Composable
private fun UndoButton(enabled: Boolean, onClick: () -> Unit) {
    Box(
        modifier = Modifier.size(width = 48.dp, height = 38.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(if (enabled) TileBg else TileBgDark)
            .clickable(enabled = enabled) { onClick() },
        contentAlignment = Alignment.Center
    ) { Text("↶", color = if (enabled) Accent else Accent.copy(alpha = 0.3f), fontSize = 22.sp, fontWeight = FontWeight.Bold) }
}

// ── Увеличенная плашка разницы очков ──
@Composable
private fun ScoreDiffBadge(text: String, positive: Boolean) {
    Box(modifier = Modifier.clip(RoundedCornerShape(8.dp)).background(TileBgDark)
        .padding(horizontal = 14.dp, vertical = 10.dp)) {
        Text(text, color = if (positive) GoldAccent else Color(0xFFE57373),
            fontSize = 26.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun SectorRow(sector: CricketSector, game: CricketGame, onThrow: (ThrowResult) -> Unit) {
    val currentPlayer = game.players.getOrNull(game.currentPlayerIndex)
    val activeTeam = currentPlayer?.teamIndex ?: -1
    val teamAClosed = CricketLogic.isClosedByTeam(game, 0, sector)
    val teamBClosed = CricketLogic.isClosedByTeam(game, 1, sector)
    val allClosed = teamAClosed && teamBClosed

    Row(
        modifier = Modifier.fillMaxSize()
            .background(if (allClosed) Color(0xFF1A1A1A) else Color(0xFF16202C))
            .padding(horizontal = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        TeamSectorCell(sector, game, 0, activeTeam == 0, onThrow, mirror = false, Modifier.weight(1f))
        CenterCell(sector)
        TeamSectorCell(sector, game, 1, activeTeam == 1, onThrow, mirror = true, Modifier.weight(1f))
    }
}

@Composable
private fun CenterCell(sector: CricketSector) {
    if (sector == CricketSector.BULL) {
        Box(modifier = Modifier.size(48.dp).clip(CircleShape).background(Color(0xFF2E7D32)),
            contentAlignment = Alignment.Center) {
            Box(modifier = Modifier.size(22.dp).clip(CircleShape).background(Color(0xFFD32F2F)))
        }
    } else {
        Text(sector.label, color = Color.White, fontSize = 26.sp,
            fontWeight = FontWeight.Bold, modifier = Modifier.width(56.dp), textAlign = TextAlign.Center)
    }
}

@Composable
private fun TeamSectorCell(
    sector: CricketSector, game: CricketGame, team: Int,
    isActive: Boolean, onThrow: (ThrowResult) -> Unit,
    mirror: Boolean, modifier: Modifier
) {
    val rawHits = game.playersOfTeam(team).sumOf { it.hits[sector] ?: 0 }
    val displayHits = rawHits.coerceAtMost(3)
    val myScore = teamScore(game, team, sector)
    val canThrow = isActive && !game.isFinished && game.currentTurnDarts < 3

    Row(modifier = modifier, verticalAlignment = Alignment.CenterVertically) {
        if (!mirror) {
            if (sector.hasTriple) {
                ThrowCircleButton("T", canThrow) { onThrow(ThrowResult.TRIPLE) }
                Spacer(Modifier.weight(1f))
            }
            ThrowCircleButton("D", canThrow) { onThrow(ThrowResult.DOUBLE) }
            Spacer(Modifier.weight(1f))
            HitsSquare(displayHits, myScore > 0, canThrow) { onThrow(ThrowResult.SINGLE) }
            Spacer(Modifier.weight(2f))
        } else {
            Spacer(Modifier.weight(2f))
            HitsSquare(displayHits, myScore > 0, canThrow) { onThrow(ThrowResult.SINGLE) }
            Spacer(Modifier.weight(1f))
            ThrowCircleButton("D", canThrow) { onThrow(ThrowResult.DOUBLE) }
            if (sector.hasTriple) {
                Spacer(Modifier.weight(1f))
                ThrowCircleButton("T", canThrow) { onThrow(ThrowResult.TRIPLE) }
            }
        }
    }
}

@Composable
private fun ThrowCircleButton(label: String, enabled: Boolean, onClick: () -> Unit) {
    Box(modifier = Modifier.size(42.dp).clip(CircleShape)
        .background(if (enabled) Accent.copy(alpha = 0.9f) else TileBgDark)
        .clickable(enabled = enabled) { onClick() }, contentAlignment = Alignment.Center) {
        Text(label, color = if (enabled) Color(0xFF121212) else Accent.copy(alpha = 0.4f),
            fontSize = 17.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun HitsSquare(hits: Int, hasScore: Boolean, enabled: Boolean, onClick: () -> Unit) {
    val color = when {
        hits >= 3 && hasScore -> Color(0xFF9C27B0)
        hits >= 3 -> Color(0xFFE53935)
        hits == 2 -> Color(0xFFFFC107)
        hits == 1 -> Color(0xFF4CAF50)
        else -> TileBgDark
    }
    val label = when {
        hits >= 3 && hasScore -> "3+"
        hits >= 3 -> "3"
        hits == 2 -> "2"
        hits == 1 -> "1"
        else -> "S"
    }
    val labelColor = when {
        hits == 0 -> Color(0xFF78909C)
        hits >= 3 -> Color.White
        else -> Color(0xFF121212)
    }
    Box(modifier = Modifier.size(42.dp).clip(RoundedCornerShape(8.dp)).background(color)
        .clickable(enabled = enabled) { onClick() }, contentAlignment = Alignment.Center) {
        Text(label, color = labelColor, fontSize = if (hits == 0) 18.sp else 16.sp, fontWeight = FontWeight.Bold)
    }
}

private fun teamScore(game: CricketGame, team: Int, sector: CricketSector): Int =
    game.playersOfTeam(team).sumOf { it.scores[sector] ?: 0 }

private fun avgPerLeg(game: CricketGame, team: Int): Double {
    val players = game.playersOfTeam(team)
    val darts = players.sumOf { it.dartsThrown }
    if (darts < 3) return 0.0
    val marks = players.sumOf { it.legMarks }
    return marks.toDouble() / (darts / 3.0)
}

@Composable
private fun WinDialog(game: CricketGame, onUndo: () -> Unit, onContinue: () -> Unit) {
    val winnerTeam = game.winnerIndex
    val winnerName = if (winnerTeam != null) teamName(game, winnerTeam) else "—"
    val teamMatchScore = if (winnerTeam != null) CricketLogic.teamMatchScore(game, winnerTeam) else 0
    val teamDarts = if (winnerTeam != null) game.playersOfTeam(winnerTeam).sumOf { it.matchDartsThrown } else 0
    val teamSets = if (winnerTeam != null) game.playersOfTeam(winnerTeam).firstOrNull()?.setsWon ?: 0 else 0

    AlertDialog(
        onDismissRequest = { },
        confirmButton = { TextButton(onClick = onContinue) { Text("Продолжить", color = Accent) } },
        dismissButton = { TextButton(onClick = onUndo) { Text("↶ Отменить", color = ErrorColor) } },
        title = { Text("ПОБЕДА В МАТЧЕ!", color = Accent, fontSize = 22.sp, fontWeight = FontWeight.Bold) },
        text = {
            Column {
                Text(winnerName, color = GoldAccent, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(8.dp))
                Text("Сеты: $teamSets из ${game.setsPerMatch}", color = Color.White, fontSize = 14.sp)
                Text("Очки за матч: $teamMatchScore", color = Color.White, fontSize = 14.sp)
                Text("Бросков: $teamDarts", color = Color.White, fontSize = 14.sp)

                if (game.type == CricketType.AMERICAN) {
                    Spacer(Modifier.height(14.dp))
                    Text(
                        "НОРМАТИВ",
                        color = Accent,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 2.sp
                    )
                    Spacer(Modifier.height(6.dp))

                    val darts = teamDarts
                    val achievedIdx = AMERICAN_CRICKET_NORMS.indexOfFirst { darts <= it.maxDarts }
                    val achieved = if (achievedIdx >= 0) AMERICAN_CRICKET_NORMS[achievedIdx] else null
                    val next = if (achievedIdx > 0) AMERICAN_CRICKET_NORMS[achievedIdx - 1] else null

                    if (achieved != null) {
                        Row(
                            modifier = Modifier.fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                                .background(TileBg)
                                .padding(10.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                achieved.name,
                                color = GoldAccent,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                "≤ ${achieved.maxDarts}",
                                color = GoldAccent,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        if (next != null) {
                            Spacer(Modifier.height(6.dp))
                            Text(
                                "До норматива «${next.name}» нужно было сыграть на ${darts - next.maxDarts} бросков меньше",
                                color = Color.White.copy(alpha = 0.6f),
                                fontSize = 12.sp,
                                lineHeight = 16.sp
                            )
                        }
                    } else {
                        val lowest = AMERICAN_CRICKET_NORMS.last()
                        Text(
                            "Пока без разряда. До «${lowest.name}» нужно было сыграть на ${darts - lowest.maxDarts} бросков меньше.",
                            color = Color.White.copy(alpha = 0.6f),
                            fontSize = 12.sp,
                            lineHeight = 16.sp
                        )
                    }
                }
            }
        }
    )
}
