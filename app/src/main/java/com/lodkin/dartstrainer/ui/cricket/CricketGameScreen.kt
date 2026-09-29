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

    // Диалоги лега и сета (храним имя выигравшей команды)
    var showLegWonDialog by remember { mutableStateOf(false) }
    var showSetWonDialog by remember { mutableStateOf(false) }
    var legWinnerLabel by remember { mutableStateOf("") }
    var setWinnerLabel by remember { mutableStateOf("") }

    // История для отката
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

    // ── Автоматический ход бота ──
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

    // Реакция на завершение лега
    LaunchedEffect(game.lastLegWinnerIndex) {
        val teamIdx = game.lastLegWinnerIndex
        if (teamIdx != null && !game.isFinished) {
            legWinnerLabel = teamName(game, teamIdx)
            showLegWonDialog = true
        }
    }

    // Реакция на завершение сета
    LaunchedEffect(game.lastSetWinnerIndex) {
        val teamIdx = game.lastSetWinnerIndex
        if (teamIdx != null && !game.isFinished) {
            setWinnerLabel = teamName(game, teamIdx)
            showSetWonDialog = true
        }
    }

    LaunchedEffect(game.isFinished) {
        if (game.isFinished && !showWinDialog) {
            showWinDialog = true
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF121212))
    ) {
        CricketTopBar(
            game = game,
            onBack = { showBackConfirm = true }
        )

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .verticalScroll(rememberScrollState())
        ) {
            CricketSector.ALL.forEach { sector ->
                SectorRow(
                    sector = sector,
                    game = game,
                    onThrow = { result ->
                        val currentPlayer = game.players.getOrNull(game.currentPlayerIndex)
                        if (currentPlayer?.isBot == true) return@SectorRow

                        saveHistory()
                        val currentPlayerIndex = game.currentPlayerIndex
                        var updatedGame = CricketLogic.applyThrow(
                            game = game,
                            sector = sector,
                            result = result,
                            playerIndex = currentPlayerIndex
                        )

                        if (updatedGame.currentTurnDarts >= 3 && !updatedGame.isFinished) {
                            updatedGame = CricketLogic.nextPlayer(updatedGame)
                        }

                        game = updatedGame
                    }
                )
            }
        }

        CricketBottomBar(
            game = game,
            canUndo = history.isNotEmpty(),
            onUndo = { undo() },
            onNextPlayer = {
                val currentPlayer = game.players.getOrNull(game.currentPlayerIndex)
                if (!game.isFinished && currentPlayer?.isBot != true) {
                    saveHistory()
                    game = CricketLogic.nextPlayer(game)
                }
            }
        )
    }

    // Диалог завершения ЛЕГА
    if (showLegWonDialog) {
        AlertDialog(
            onDismissRequest = { },
            confirmButton = {
                TextButton(onClick = {
                    showLegWonDialog = false
                    game = game.copy(lastLegWinnerIndex = null)
                }) {
                    Text("Продолжить", color = Accent)
                }
            },
            title = {
                Text(
                    "ЛЕГ ЗАВЕРШЁН",
                    color = Accent,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column {
                    Text("Победитель лега:", color = Color.White, fontSize = 14.sp)
                    Text(
                        legWinnerLabel,
                        color = GoldAccent,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(Modifier.height(12.dp))
                    Text("Счёт по легам в сете:", color = Color.White, fontSize = 14.sp)
                    Spacer(Modifier.height(4.dp))
                    TeamScoresList(game, showLegs = true)
                }
            }
        )
    }

    // Диалог завершения СЕТА
    if (showSetWonDialog) {
        AlertDialog(
            onDismissRequest = { },
            confirmButton = {
                TextButton(onClick = {
                    showSetWonDialog = false
                    game = game.copy(lastSetWinnerIndex = null)
                }) {
                    Text("Продолжить", color = Accent)
                }
            },
            title = {
                Text(
                    "СЕТ ЗАВЕРШЁН",
                    color = GoldAccent,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column {
                    Text("Победитель сета:", color = Color.White, fontSize = 14.sp)
                    Text(
                        setWinnerLabel,
                        color = GoldAccent,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(Modifier.height(12.dp))
                    Text("Счёт по сетам:", color = Color.White, fontSize = 14.sp)
                    Spacer(Modifier.height(4.dp))
                    TeamScoresList(game, showLegs = false)
                }
            }
        )
    }

    // Финальный диалог победы в МАТЧЕ
    if (showWinDialog) {
        WinDialog(
            game = game,
            onUndo = {
                undo()
                showWinDialog = false
            },
            onContinue = {
                showWinDialog = false
                onGameFinish(game)
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
                }) { Text("ДА", color = ErrorColor) }
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

// Собираем имя команды (join через " + ")
private fun teamName(game: CricketGame, teamIdx: Int): String {
    return game.playersOfTeam(teamIdx).joinToString(" + ") { it.name }
}

@Composable
private fun TeamScoresList(game: CricketGame, showLegs: Boolean) {
    game.players.forEach { p ->
        val value = if (showLegs) p.legsInCurrentSet else p.setsWon
        Text(
            "${p.name}: $value",
            color = Color.White,
            fontSize = 13.sp
        )
    }
}

@Composable
private fun CricketTopBar(game: CricketGame, onBack: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(TileBgDark)
            .padding(horizontal = 8.dp, vertical = 6.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
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

            Spacer(Modifier.width(8.dp))

            Text(
                if (game.isPairGame) "Крикет (2×2)" else "Крикет",
                color = Color.White,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.weight(1f)
            )

            val typeLabel = if (game.type == CricketType.AMERICAN) "Амер." else "Без очков"
            Text(
                typeLabel,
                color = Accent,
                fontSize = 12.sp
            )
        }

        Spacer(Modifier.height(4.dp))

        Text(
            "Сет ${game.currentSetNumber}/${game.setsPerMatch}  •  Лег ${game.currentLegNumber}/${game.legsPerSet}",
            color = Color.White.copy(alpha = 0.7f),
            fontSize = 11.sp,
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Center
        )

        Spacer(Modifier.height(6.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            game.players.forEachIndexed { index, player ->
                val isActive = index == game.currentPlayerIndex
                PlayerHeader(
                    player = player,
                    isActive = isActive,
                    isPairGame = game.isPairGame,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun PlayerHeader(
    player: CricketPlayer,
    isActive: Boolean,
    isPairGame: Boolean,
    modifier: Modifier
) {
    val bg = if (isActive) Accent else TileBg
    val fg = if (isActive) Color(0xFF121212) else Color.White

    // MPR (marks per round) — среднее меток за подход
    val mpr: Double = if (player.matchDartsThrown > 0) {
        val totalMarks = player.matchHits.values.sum()
        val rounds = player.matchDartsThrown / 3.0
        totalMarks / rounds
    } else 0.0

    Column(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(bg)
            .padding(horizontal = 4.dp, vertical = 6.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            player.name,
            color = fg,
            fontSize = if (isPairGame) 9.sp else 11.sp,
            fontWeight = FontWeight.Bold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            textAlign = TextAlign.Center
        )
        Text(
            player.totalScore.toString(),
            color = if (isActive) Color(0xFF121212) else GoldAccent,
            fontSize = if (isPairGame) 14.sp else 18.sp,
            fontWeight = FontWeight.Bold
        )
        Text(
            "S${player.setsWon} L${player.legsInCurrentSet}",
            color = if (isActive) Color(0xFF121212).copy(alpha = 0.7f) else Accent,
            fontSize = 8.sp,
            fontWeight = FontWeight.Medium
        )
        // Отладочный MPR (потом отключим)
        Text(
            "MPR %.2f".format(mpr),
            color = if (isActive) Color(0xFF121212).copy(alpha = 0.6f) else Color.White.copy(alpha = 0.5f),
            fontSize = 8.sp
        )
    }
}

@Composable
private fun SectorRow(
    sector: CricketSector,
    game: CricketGame,
    onThrow: (ThrowResult) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 4.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        game.players.forEachIndexed { index, player ->
            val isActive = index == game.currentPlayerIndex && !game.isFinished
            PlayerSectorCell(
                player = player,
                sector = sector,
                game = game,
                isActive = isActive,
                playerIndex = index,
                onThrow = onThrow,
                modifier = Modifier.weight(1f)
            )
            if (index < game.players.size - 1) {
                Spacer(Modifier.width(3.dp))
            }
        }
    }
}

@Composable
private fun PlayerSectorCell(
    player: CricketPlayer,
    sector: CricketSector,
    game: CricketGame,
    isActive: Boolean,
    playerIndex: Int,
    onThrow: (ThrowResult) -> Unit,
    modifier: Modifier
) {
    val hits = player.hits[sector] ?: 0
    val score = player.scores[sector] ?: 0

    val teamClosed = CricketLogic.isClosedByTeam(game, player.teamIndex, sector)
    val allTeamsClosed = CricketLogic.isClosedByAllTeams(game, sector)

    val canScore = if (game.type == CricketType.AMERICAN) {
        val myTeam = player.teamIndex
        val otherTeams = (0 until game.teamCount).filter { it != myTeam }
        val anyOtherNotClosed = otherTeams.any { !CricketLogic.isClosedByTeam(game, it, sector) }
        teamClosed && anyOtherNotClosed
    } else false

    val showButtons = isActive && !player.isBot && (!teamClosed || canScore)

    Column(
        modifier = modifier
            .clip(RoundedCornerShape(10.dp))
            .background(
                when {
                    allTeamsClosed -> Color(0xFF2A2A2A)
                    teamClosed -> Color(0xFF2A3F2A)
                    else -> TileBg
                }
            )
            .padding(4.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            sector.label,
            color = if (teamClosed) Accent else Color.White,
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold
        )

        Spacer(Modifier.height(3.dp))

        HitsVisualization(hits = hits)

        Spacer(Modifier.height(3.dp))

        if (score > 0) {
            Text(
                "+$score",
                color = GoldAccent,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold
            )
        } else {
            Spacer(Modifier.height(12.dp))
        }

        if (showButtons) {
            Spacer(Modifier.height(4.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                if (sector.hasTriple) {
                    ThrowButton("T", Modifier.weight(1f)) { onThrow(ThrowResult.TRIPLE) }
                }
                ThrowButton("S", Modifier.weight(1f)) { onThrow(ThrowResult.SINGLE) }
                ThrowButton("D", Modifier.weight(1f)) { onThrow(ThrowResult.DOUBLE) }
            }
        }
    }
}

@Composable
private fun HitsVisualization(hits: Int) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(2.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        repeat(3) { index ->
            val filled = index < hits
            Box(
                modifier = Modifier
                    .size(9.dp)
                    .clip(CircleShape)
                    .background(if (filled) Accent else TileBgDark)
            )
        }
    }
}

@Composable
private fun ThrowButton(label: String, modifier: Modifier, onClick: () -> Unit) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(6.dp))
            .background(Accent.copy(alpha = 0.8f))
            .clickable { onClick() }
            .padding(vertical = 6.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            label,
            color = Color(0xFF121212),
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
private fun CricketBottomBar(
    game: CricketGame,
    canUndo: Boolean,
    onUndo: () -> Unit,
    onNextPlayer: () -> Unit
) {
    val currentPlayer = game.players.getOrNull(game.currentPlayerIndex)
    val isBotTurn = currentPlayer?.isBot == true

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(TileBgDark)
            .padding(8.dp)
    ) {
        if (isBotTurn && !game.isFinished) {
            Text(
                "Ход бота: ${currentPlayer?.name ?: ""}...",
                color = Accent,
                fontSize = 12.sp,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 6.dp),
                textAlign = TextAlign.Center
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            val undoBg = if (canUndo) TileBg else TileBgDark
            val undoFg = if (canUndo) Accent else Accent.copy(alpha = 0.3f)

            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(10.dp))
                    .background(undoBg)
                    .clickable(enabled = canUndo) { onUndo() }
                    .padding(vertical = 12.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    "↶ Ход назад",
                    color = undoFg,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium
                )
            }

            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(10.dp))
                    .background(if (isBotTurn) TileBgDark else Accent)
                    .clickable(enabled = !game.isFinished && !isBotTurn) { onNextPlayer() }
                    .padding(vertical = 12.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    if (isBotTurn) "Ждём бота..." else "Завершить ход",
                    color = if (isBotTurn) Accent.copy(alpha = 0.5f) else Color(0xFF121212),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
private fun WinDialog(
    game: CricketGame,
    onUndo: () -> Unit,
    onContinue: () -> Unit
) {
    val winnerTeam = game.winnerIndex
    val winnerName = if (winnerTeam != null) teamName(game, winnerTeam) else "—"

    val teamMatchScore = if (winnerTeam != null) {
        CricketLogic.teamMatchScore(game, winnerTeam)
    } else 0

    val teamDarts = if (winnerTeam != null) {
        game.playersOfTeam(winnerTeam).sumOf { it.matchDartsThrown }
    } else 0

    val teamSets = if (winnerTeam != null) {
        game.playersOfTeam(winnerTeam).firstOrNull()?.setsWon ?: 0
    } else 0

    AlertDialog(
        onDismissRequest = { },
        confirmButton = {
            TextButton(onClick = onContinue) {
                Text("Продолжить", color = Accent)
            }
        },
        dismissButton = {
            TextButton(onClick = onUndo) {
                Text("↶ Отменить", color = ErrorColor)
            }
        },
        title = {
            Text(
                "ПОБЕДА В МАТЧЕ!",
                color = Accent,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column {
                Text(
                    winnerName,
                    color = GoldAccent,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    "Сеты: $teamSets из ${game.setsPerMatch}",
                    color = Color.White,
                    fontSize = 14.sp
                )
                Text(
                    "Очки за матч: $teamMatchScore",
                    color = Color.White,
                    fontSize = 14.sp
                )
                Text(
                    "Бросков: $teamDarts",
                    color = Color.White,
                    fontSize = 14.sp
                )
            }
        }
    )
}
