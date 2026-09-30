package com.lodkin.dartstrainer.ui.cricket

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
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

@Composable
fun CricketGameScreen(
    initialGame: CricketGame,
    onGameFinish: (CricketGame) -> Unit,
    onBack: () -> Unit
) {
    var game by remember { mutableStateOf(initialGame) }
    var showWinDialog by remember { mutableStateOf(false) }
    var showBackConfirm by remember { mutableStateOf(false) }
    var showLegWonDialog by remember { mutableStateOf(false) }
    var showSetWonDialog by remember { mutableStateOf(false) }
    var legWinnerLabel by remember { mutableStateOf("") }
    var setWinnerLabel by remember { mutableStateOf("") }

    val history = remember { mutableStateListOf<CricketGame>() }

    fun saveHistory() {
        history.add(game)
        if (history.size > 300) history.removeAt(0)
    }

    fun undo() {
        if (history.isNotEmpty()) {
            game = history.removeAt(history.lastIndex)
        }
    }

    // ── Автоход бота ──
    LaunchedEffect(
        game.currentPlayerIndex,
        game.isFinished,
        showLegWonDialog,
        showSetWonDialog,
        showWinDialog
    ) {
        val currentPlayer = game.players.getOrNull(game.currentPlayerIndex) ?: return@LaunchedEffect
        if (game.isFinished) return@LaunchedEffect
        if (showLegWonDialog || showSetWonDialog || showWinDialog) return@LaunchedEffect
        if (!currentPlayer.isBot) return@LaunchedEffect

        delay(700L)

        if (game.isFinished) return@LaunchedEffect
        if (showLegWonDialog || showSetWonDialog || showWinDialog) return@LaunchedEffect
        if (game.players.getOrNull(game.currentPlayerIndex)?.isBot != true) return@LaunchedEffect

        saveHistory()
        game = CricketBotAI.performTurn(game, game.currentPlayerIndex)
    }

    LaunchedEffect(game.lastLegWinnerIndex) {
        val teamIdx = game.lastLegWinnerIndex
        if (teamIdx != null && !game.isFinished) {
            legWinnerLabel = teamName(game, teamIdx)
            showLegWonDialog = true
        }
    }

    LaunchedEffect(game.lastSetWinnerIndex) {
        val teamIdx = game.lastSetWinnerIndex
        if (teamIdx != null && !game.isFinished) {
            setWinnerLabel = teamName(game, teamIdx)
            showSetWonDialog = true
        }
    }

    LaunchedEffect(game.isFinished) {
        if (game.isFinished && !showWinDialog) showWinDialog = true
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0D1117))
    ) {
        TopBar(onBack = { showBackConfirm = true })

        ParamsRow(game = game)

        TeamsRow(game = game)

        OKRow(
            game = game,
            onOk = {
                val currentPlayer = game.players.getOrNull(game.currentPlayerIndex)
                if (!game.isFinished && currentPlayer?.isBot != true) {
                    saveHistory()
                    game = CricketLogic.finishTurn(game)
                }
            }
        )

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .verticalScroll(rememberScrollState())
        ) {
            CricketSector.ALL.forEach { sector ->
                SectorRowNew(
                    sector = sector,
                    game = game,
                    onThrow = { result ->
                        val currentPlayer = game.players.getOrNull(game.currentPlayerIndex)
                        if (currentPlayer?.isBot == true) return@SectorRowNew

                        saveHistory()
                        val currentPlayerIndex = game.currentPlayerIndex
                        val updated = CricketLogic.applyThrow(
                            game = game,
                            sector = sector,
                            result = result,
                            playerIndex = currentPlayerIndex
                        )
                        game = updated
                    }
                )
            }
            Spacer(Modifier.height(8.dp))
        }

        BottomBar(
            game = game,
            canUndo = history.isNotEmpty(),
            onUndo = { undo() }
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
                Text("ЛЕГ ЗАВЕРШЁН", color = Accent, fontSize = 22.sp, fontWeight = FontWeight.Bold)
            },
            text = {
                Column {
                    Text("Победитель:", color = Color.White, fontSize = 14.sp)
                    Text(legWinnerLabel, color = GoldAccent, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.height(12.dp))
                    Text("Счёт по легам в сете:", color = Color.White, fontSize = 14.sp)
                    TeamScoresList(game, showLegs = true)
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
            title = {
                Text("СЕТ ЗАВЕРШЁН", color = GoldAccent, fontSize = 22.sp, fontWeight = FontWeight.Bold)
            },
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
            onContinue = { showWinDialog = false; onGameFinish(game) }
        )
    }

    if (showBackConfirm) {
        AlertDialog(
            onDismissRequest = { showBackConfirm = false },
            confirmButton = {
                TextButton(onClick = { showBackConfirm = false; onBack() }) {
                    Text("ДА", color = ErrorColor)
                }
            },
            dismissButton = {
                TextButton(onClick = { showBackConfirm = false }) {
                    Text("НЕТ", color = Accent)
                }
            },
            title = { Text("Выйти из игры?", color = Color.White) },
            text = { Text("Прогресс не сохранится.", color = Color.White) }
        )
    }
}

private fun teamName(game: CricketGame, teamIdx: Int): String {
    return game.playersOfTeam(teamIdx).joinToString("/") { it.name }
}

@Composable
private fun TeamScoresList(game: CricketGame, showLegs: Boolean) {
    game.players.forEach { p ->
        val value = if (showLegs) p.legsInCurrentSet else p.setsWon
        Text("${p.name}: $value", color = Color.White, fontSize = 13.sp)
    }
}

// ─────────────────────────────────────────────
// ВЕРХНЯЯ ПАНЕЛЬ
// ─────────────────────────────────────────────
@Composable
private fun TopBar(onBack: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFF1A2332))
            .padding(horizontal = 12.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(32.dp)
                .clip(CircleShape)
                .background(TileBg)
                .clickable { onBack() },
            contentAlignment = Alignment.Center
        ) {
            Text("←", color = Accent, fontSize = 18.sp)
        }
        Spacer(Modifier.width(12.dp))
        Text("Крикет", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
    }
}

// ─────────────────────────────────────────────
// ПАРАМЕТРЫ ИГРЫ
// ─────────────────────────────────────────────
@Composable
private fun ParamsRow(game: CricketGame) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFF1A2332))
            .padding(horizontal = 12.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            "Параметры игры (Сеты до ${game.setsPerMatch}, Леги до ${game.legsPerSet})",
            color = Color(0xFFCCDDEE),
            fontSize = 13.sp
        )
    }
}

// ─────────────────────────────────────────────
// ИМЕНА КОМАНД + СР + СЧЁТ ПО ЛЕГАМ
// ─────────────────────────────────────────────
@Composable
private fun TeamsRow(game: CricketGame) {
    val currentIdx = game.currentPlayerIndex
    val currentPlayer = game.players.getOrNull(currentIdx)
    val activeTeam = currentPlayer?.teamIndex ?: -1

    val teamAName = if (activeTeam == 0 && currentPlayer != null) {
        currentPlayer.name
    } else {
        game.playersOfTeam(0).joinToString("/") { it.name }
    }
    val teamBName = if (activeTeam == 1 && currentPlayer != null) {
        currentPlayer.name
    } else {
        game.playersOfTeam(1).joinToString("/") { it.name }
    }

    val legsA = game.playersOfTeam(0).firstOrNull()?.legsInCurrentSet ?: 0
    val legsB = game.playersOfTeam(1).firstOrNull()?.legsInCurrentSet ?: 0

    val setsA = game.playersOfTeam(0).firstOrNull()?.setsWon ?: 0
    val setsB = game.playersOfTeam(1).firstOrNull()?.setsWon ?: 0

    val avgA = avgPerLeg(game, 0)
    val avgB = avgPerLeg(game, 1)

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFF16202C))
            .padding(horizontal = 8.dp, vertical = 8.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            SetBadge(setsA)
            Spacer(Modifier.width(6.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    teamAName,
                    color = if (activeTeam == 0) Accent else Color.White,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text("ср. %.2f".format(avgA), color = Color(0xFF99AABB), fontSize = 11.sp)
            }
            Text(
                "$legsA : $legsB",
                color = Color.White,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(horizontal = 12.dp)
            )
            Column(modifier = Modifier.weight(1f), horizontalAlignment = Alignment.End) {
                Text(
                    teamBName,
                    color = if (activeTeam == 1) Accent else Color.White,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    textAlign = TextAlign.End
                )
                Text("ср. %.2f".format(avgB), color = Color(0xFF99AABB), fontSize = 11.sp)
            }
            Spacer(Modifier.width(6.dp))
            SetBadge(setsB)
        }
    }
}

@Composable
private fun SetBadge(sets: Int) {
    Column(
        modifier = Modifier
            .clip(RoundedCornerShape(6.dp))
            .background(TileBgDark)
            .padding(horizontal = 6.dp, vertical = 4.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("СЕТ", color = Accent, fontSize = 9.sp, fontWeight = FontWeight.Bold)
        Text("$sets", color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold)
    }
}

// ─────────────────────────────────────────────
// СЧЁТЧИК ДРОТИКОВ + КНОПКА OK
// ─────────────────────────────────────────────
@Composable
private fun OKRow(game: CricketGame, onOk: () -> Unit) {
    val currentPlayer = game.players.getOrNull(game.currentPlayerIndex)
    val isBotTurn = currentPlayer?.isBot == true
    val dartsA = game.playersOfTeam(0).sumOf { it.dartsThrown }
    val dartsB = game.playersOfTeam(1).sumOf { it.dartsThrown }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFF16202C))
            .padding(horizontal = 12.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text("$dartsA", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))

        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(8.dp))
                .background(if (isBotTurn) TileBgDark else Accent)
                .clickable(enabled = !game.isFinished && !isBotTurn) { onOk() }
                .padding(horizontal = 40.dp, vertical = 8.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                if (isBotTurn) "..." else "OK",
                color = if (isBotTurn) Accent.copy(alpha = 0.5f) else Color(0xFF121212),
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )
        }

        Text(
            "$dartsB",
            color = Color.White,
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.weight(1f),
            textAlign = TextAlign.End
        )
    }
}

// ─────────────────────────────────────────────
// СТРОКА СЕКТОРА
// ─────────────────────────────────────────────
@Composable
private fun SectorRowNew(
    sector: CricketSector,
    game: CricketGame,
    onThrow: (ThrowResult) -> Unit
) {
    val currentPlayer = game.players.getOrNull(game.currentPlayerIndex)
    val activeTeam = currentPlayer?.teamIndex ?: -1

    val teamAClosed = CricketLogic.isClosedByTeam(game, 0, sector)
    val teamBClosed = CricketLogic.isClosedByTeam(game, 1, sector)
    val allClosed = teamAClosed && teamBClosed

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(if (allClosed) Color(0xFF1A1A1A) else Color(0xFF16202C))
            .padding(horizontal = 4.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        LeftCell(
            sector = sector,
            game = game,
            isActive = activeTeam == 0,
            isClosed = teamAClosed,
            onThrow = onThrow,
            modifier = Modifier.weight(1f)
        )

        CenterCell(sector = sector)

        RightCell(
            sector = sector,
            game = game,
            isActive = activeTeam == 1,
            isClosed = teamBClosed,
            onThrow = onThrow,
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun CenterCell(sector: CricketSector) {
    if (sector == CricketSector.BULL) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(CircleShape)
                .background(Color(0xFF2E7D32)),
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .size(16.dp)
                    .clip(CircleShape)
                    .background(Color(0xFFD32F2F))
            )
        }
    } else {
        Text(
            sector.label,
            color = Color.White,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.width(40.dp),
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun LeftCell(
    sector: CricketSector,
    game: CricketGame,
    isActive: Boolean,
    isClosed: Boolean,
    onThrow: (ThrowResult) -> Unit,
    modifier: Modifier
) {
    val hits = teamHits(game, 0, sector)
    val score = teamScore(game, 0, sector)
    val buttonLabel = if (sector == CricketSector.BULL) "D" else "T"
    val canThrow = isActive && !game.isFinished

    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(CircleShape)
                .background(if (canThrow) Accent.copy(alpha = 0.9f) else TileBgDark)
                .clickable(enabled = canThrow) { onThrow(ThrowResult.TRIPLE) },
            contentAlignment = Alignment.Center
        ) {
            Text(
                buttonLabel,
                color = if (canThrow) Color(0xFF121212) else Accent.copy(alpha = 0.4f),
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(Modifier.width(6.dp))

        MarksRow(
            hits = hits,
            enabled = canThrow,
            onClick = { onThrow(ThrowResult.SINGLE) }
        )

        Spacer(Modifier.width(4.dp))

        if (score > 0) {
            Text("+$score", color = GoldAccent, fontSize = 11.sp, fontWeight = FontWeight.Bold)
        } else {
            Spacer(Modifier.width(20.dp))
        }

        Spacer(Modifier.weight(1f))
    }
}

@Composable
private fun RightCell(
    sector: CricketSector,
    game: CricketGame,
    isActive: Boolean,
    isClosed: Boolean,
    onThrow: (ThrowResult) -> Unit,
    modifier: Modifier
) {
    val hits = teamHits(game, 1, sector)
    val score = teamScore(game, 1, sector)
    val buttonLabel = if (sector == CricketSector.BULL) "D" else "T"
    val canThrow = isActive && !game.isFinished

    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.End
    ) {
        Spacer(Modifier.weight(1f))

        if (score > 0) {
            Text("+$score", color = GoldAccent, fontSize = 11.sp, fontWeight = FontWeight.Bold)
        } else {
            Spacer(Modifier.width(20.dp))
        }

        Spacer(Modifier.width(4.dp))

        MarksRow(
            hits = hits,
            enabled = canThrow,
            onClick = { onThrow(ThrowResult.SINGLE) }
        )

        Spacer(Modifier.width(6.dp))

        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(CircleShape)
                .background(if (canThrow) Accent.copy(alpha = 0.9f) else TileBgDark)
                .clickable(enabled = canThrow) { onThrow(ThrowResult.TRIPLE) },
            contentAlignment = Alignment.Center
        ) {
            Text(
                buttonLabel,
                color = if (canThrow) Color(0xFF121212) else Accent.copy(alpha = 0.4f),
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
private fun MarksRow(
    hits: Int,
    enabled: Boolean,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier.clickable(enabled = enabled) { onClick() },
        horizontalArrangement = Arrangement.spacedBy(3.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        repeat(3) { i ->
            val filled = i < hits
            Box(
                modifier = Modifier
                    .size(18.dp)
                    .clip(CircleShape)
                    .background(if (filled) Accent else TileBgDark)
            )
        }
    }
}

// ─────────────────────────────────────────────
// НИЖНЯЯ ПАНЕЛЬ
// ─────────────────────────────────────────────
@Composable
private fun BottomBar(
    game: CricketGame,
    canUndo: Boolean,
    onUndo: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFF1A2332))
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(10.dp))
                .background(if (canUndo) TileBg else TileBgDark)
                .clickable(enabled = canUndo) { onUndo() }
                .padding(vertical = 12.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                "↶ Вернуть ход соперника",
                color = if (canUndo) Accent else Accent.copy(alpha = 0.3f),
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

// ─────────────────────────────────────────────
// УТИЛИТЫ
// ─────────────────────────────────────────────
private fun teamHits(game: CricketGame, team: Int, sector: CricketSector): Int {
    return game.playersOfTeam(team).sumOf { it.hits[sector] ?: 0 }.coerceAtMost(3)
}

private fun teamScore(game: CricketGame, team: Int, sector: CricketSector): Int {
    return game.playersOfTeam(team).sumOf { it.scores[sector] ?: 0 }
}

// Средний набор = все метки команды за лег / (все дротики / 3)
private fun avgPerLeg(game: CricketGame, team: Int): Double {
    val players = game.playersOfTeam(team)
    val darts = players.sumOf { it.dartsThrown }
    if (darts < 3) return 0.0
    val marks = players.sumOf { it.legMarks }
    return marks.toDouble() / (darts / 3.0)
}

// ─────────────────────────────────────────────
// ДИАЛОГ ПОБЕДЫ
// ─────────────────────────────────────────────
@Composable
private fun WinDialog(
    game: CricketGame,
    onUndo: () -> Unit,
    onContinue: () -> Unit
) {
    val winnerTeam = game.winnerIndex
    val winnerName = if (winnerTeam != null) teamName(game, winnerTeam) else "—"
    val teamMatchScore = if (winnerTeam != null) CricketLogic.teamMatchScore(game, winnerTeam) else 0
    val teamDarts = if (winnerTeam != null) game.playersOfTeam(winnerTeam).sumOf { it.matchDartsThrown } else 0
    val teamSets = if (winnerTeam != null) game.playersOfTeam(winnerTeam).firstOrNull()?.setsWon ?: 0 else 0

    AlertDialog(
        onDismissRequest = { },
        confirmButton = {
            TextButton(onClick = onContinue) { Text("Продолжить", color = Accent) }
        },
        dismissButton = {
            TextButton(onClick = onUndo) { Text("↶ Отменить", color = ErrorColor) }
        },
        title = {
            Text("ПОБЕДА В МАТЧЕ!", color = Accent, fontSize = 22.sp, fontWeight = FontWeight.Bold)
        },
        text = {
            Column {
                Text(winnerName, color = GoldAccent, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(8.dp))
                Text("Сеты: $teamSets из ${game.setsPerMatch}", color = Color.White, fontSize = 14.sp)
                Text("Очки за матч: $teamMatchScore", color = Color.White, fontSize = 14.sp)
                Text("Бросков: $teamDarts", color = Color.White, fontSize = 14.sp)
            }
        }
    )
}
