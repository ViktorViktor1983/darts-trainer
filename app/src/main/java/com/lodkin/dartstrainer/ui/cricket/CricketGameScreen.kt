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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.lodkin.dartstrainer.data.cricket.*
import com.lodkin.dartstrainer.theme.Accent
import com.lodkin.dartstrainer.theme.ErrorColor
import com.lodkin.dartstrainer.theme.GoldAccent
import com.lodkin.dartstrainer.theme.TileBg
import com.lodkin.dartstrainer.theme.TileBgDark

@Composable
fun CricketGameScreen(
    initialGame: CricketGame,
    onGameFinish: (CricketGame) -> Unit,
    onBack: () -> Unit
) {
    var game by remember { mutableStateOf(initialGame) }
    var showWinDialog by remember { mutableStateOf(false) }
    var showBackConfirm by remember { mutableStateOf(false) }

    LaunchedEffect(game.isFinished) {
        if (game.isFinished) {
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
            onNextPlayer = {
                if (!game.isFinished) {
                    game = CricketLogic.nextPlayer(game)
                }
            }
        )
    }

    if (showWinDialog) {
        WinDialog(
            game = game,
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
                "Крикет",
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

        Spacer(Modifier.height(6.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            game.players.forEachIndexed { index, player ->
                val isActive = index == game.currentPlayerIndex
                PlayerHeader(
                    player = player,
                    isActive = isActive,
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
    modifier: Modifier
) {
    val bg = if (isActive) Accent else TileBg
    val fg = if (isActive) Color(0xFF121212) else Color.White

    Column(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(bg)
            .padding(horizontal = 6.dp, vertical = 6.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            player.name,
            color = fg,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            maxLines = 1,
            textAlign = TextAlign.Center
        )
        Text(
            player.totalScore.toString(),
            color = if (isActive) Color(0xFF121212) else GoldAccent,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold
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
                Spacer(Modifier.width(4.dp))
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
    val isClosed = hits >= 3

    // Может ли игрок набирать очки в этом секторе?
    val canScore = if (game.type == CricketType.AMERICAN) {
        val others = game.players.filterIndexed { i, _ -> i != playerIndex }
        val anyOtherNotClosed = others.any { (it.hits[sector] ?: 0) < 3 }
        isClosed && anyOtherNotClosed
    } else {
        false
    }

    // Показывать кнопки, если ход игрока и сектор не закрыт (или можно набирать очки)
    val showButtons = isActive && (!isClosed || canScore)

    // Закрыт ли сектор у всех — тогда он "мёртвый"
    val closedByAll = game.players.all { (it.hits[sector] ?: 0) >= 3 }

    Column(
        modifier = modifier
            .clip(RoundedCornerShape(10.dp))
            .background(
                when {
                    closedByAll -> Color(0xFF2A2A2A)
                    isClosed -> Color(0xFF2A3F2A)
                    else -> TileBg
                }
            )
            .padding(6.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            sector.label,
            color = if (isClosed) Accent else Color.White,
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold
        )

        Spacer(Modifier.height(4.dp))

        HitsVisualization(hits = hits)

        Spacer(Modifier.height(4.dp))

        if (score > 0) {
            Text(
                "+$score",
                color = GoldAccent,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold
            )
        } else {
            Spacer(Modifier.height(14.dp))
        }

        if (showButtons) {
            Spacer(Modifier.height(4.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                // У Bull нет утроения — кнопка T не показывается
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
                    .size(10.dp)
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
    onNextPlayer: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(TileBgDark)
            .padding(8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(10.dp))
                    .background(TileBg)
                    .clickable { }
                    .padding(vertical = 12.dp),
                contentAlignment = Alignment.Center
            ) {
                Text("↶ Ход назад", color = Accent, fontSize = 12.sp, fontWeight = FontWeight.Medium)
            }

            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(10.dp))
                    .background(Accent)
                    .clickable(enabled = !game.isFinished) { onNextPlayer() }
                    .padding(vertical = 12.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    "Завершить ход",
                    color = Color(0xFF121212),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
private fun WinDialog(game: CricketGame, onContinue: () -> Unit) {
    val winner = game.winnerIndex?.let { game.players.getOrNull(it) }

    AlertDialog(
        onDismissRequest = { },
        confirmButton = {
            TextButton(onClick = onContinue) {
                Text("Продолжить", color = Accent)
            }
        },
        title = {
            Text(
                "ПОБЕДА!",
                color = Accent,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column {
                Text(
                    winner?.name ?: "—",
                    color = GoldAccent,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    "Очки: ${winner?.totalScore ?: 0}",
                    color = Color.White,
                    fontSize = 14.sp
                )
                Text(
                    "Бросков: ${winner?.dartsThrown ?: 0}",
                    color = Color.White,
                    fontSize = 14.sp
                )
            }
        }
    )
}
