package com.lodkin.dartstrainer.ui.cricket

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.lodkin.dartstrainer.data.cricket.CricketGame
import com.lodkin.dartstrainer.data.cricket.CricketSector
import com.lodkin.dartstrainer.theme.Accent
import com.lodkin.dartstrainer.theme.GoldAccent
import com.lodkin.dartstrainer.theme.TileBg
import com.lodkin.dartstrainer.theme.TileBgDark

@Composable
fun CricketStatsScreen(
    game: CricketGame,
    onPlayAgain: () -> Unit,
    onBackToMenu: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF121212))
            .padding(16.dp)
    ) {
        Text(
            "ОТЧЁТ О МАТЧЕ",
            color = Accent,
            fontSize = 13.sp,
            fontWeight = FontWeight.Medium,
            letterSpacing = 3.sp,
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Center
        )

        Spacer(Modifier.height(16.dp))

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .verticalScroll(rememberScrollState())
        ) {
            // Победитель
            if (game.winnerIndex != null) {
                val winner = game.players[game.winnerIndex]
                WinnerCard(
                    name = winner.name,
                    matchScore = winner.matchTotalScore,
                    setsWon = winner.setsWon,
                    setsTotal = game.setsPerMatch
                )
                Spacer(Modifier.height(20.dp))
            }

            // Счёт по игрокам (за весь матч)
            SectionTitle("ИГРОКИ")
            Spacer(Modifier.height(8.dp))
            game.players.forEach { player ->
                PlayerStatsRow(
                    name = player.name,
                    matchScore = player.matchTotalScore,
                    matchDarts = player.matchDartsThrown,
                    setsWon = player.setsWon,
                    setsTotal = game.setsPerMatch
                )
                Spacer(Modifier.height(6.dp))
            }

            Spacer(Modifier.height(20.dp))

            // Детализация по секторам (за весь матч)
            SectionTitle("ПО СЕКТОРАМ (ЗА МАТЧ)")
            Spacer(Modifier.height(8.dp))

            SectorStatsHeader(game)

            CricketSector.ALL.forEach { sector ->
                SectorStatsRow(sector, game)
                Spacer(Modifier.height(4.dp))
            }
        }

        Spacer(Modifier.height(12.dp))

        // Кнопки
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(14.dp))
                    .background(TileBgDark)
                    .clickable { onBackToMenu() }
                    .padding(vertical = 14.dp),
                contentAlignment = Alignment.Center
            ) {
                Text("В меню", color = Accent, fontSize = 14.sp, fontWeight = FontWeight.Medium)
            }

            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(14.dp))
                    .background(Accent)
                    .clickable { onPlayAgain() }
                    .padding(vertical = 14.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    "Ещё раз",
                    color = Color(0xFF121212),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
private fun SectionTitle(text: String) {
    Text(
        text = text,
        color = Accent,
        fontSize = 13.sp,
        fontWeight = FontWeight.Medium,
        letterSpacing = 3.sp
    )
}

@Composable
private fun WinnerCard(
    name: String,
    matchScore: Int,
    setsWon: Int,
    setsTotal: Int
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(Color(0xFF1A2A33))
            .padding(20.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                "🏆",
                fontSize = 40.sp
            )
            Spacer(Modifier.height(8.dp))
            Text(
                "ПОБЕДА В МАТЧЕ",
                color = Accent,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                letterSpacing = 3.sp
            )
            Spacer(Modifier.height(4.dp))
            Text(
                name,
                color = GoldAccent,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
            Spacer(Modifier.height(8.dp))
            Text(
                "Сеты: $setsWon из $setsTotal",
                color = Color.White,
                fontSize = 15.sp,
                fontWeight = FontWeight.Medium
            )
            Text(
                "Очки за матч: $matchScore",
                color = Color.White,
                fontSize = 14.sp
            )
        }
    }
}

@Composable
private fun PlayerStatsRow(
    name: String,
    matchScore: Int,
    matchDarts: Int,
    setsWon: Int,
    setsTotal: Int
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(TileBgDark)
            .padding(horizontal = 14.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                name,
                color = Color.White,
                fontSize = 15.sp,
                fontWeight = FontWeight.Medium
            )
            Text(
                "Сеты: $setsWon из $setsTotal",
                color = Accent,
                fontSize = 11.sp
            )
        }
        Column(horizontalAlignment = Alignment.End) {
            Text(
                "Очки: $matchScore",
                color = GoldAccent,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                "Бросков: $matchDarts",
                color = Color.White.copy(alpha = 0.7f),
                fontSize = 11.sp
            )
        }
    }
}

@Composable
private fun SectorStatsHeader(game: CricketGame) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(TileBg)
            .padding(horizontal = 8.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            "Сектор",
            color = Color.White,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.width(60.dp)
        )
        game.players.forEach { player ->
            Text(
                player.name.take(8),
                color = Accent,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.weight(1f),
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun SectorStatsRow(sector: CricketSector, game: CricketGame) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(TileBgDark)
            .padding(horizontal = 8.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            sector.label,
            color = Color.White,
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.width(60.dp)
        )

        game.players.forEach { player ->
            val hits = player.matchHits[sector] ?: 0
            val score = player.matchScores[sector] ?: 0
            Column(
                modifier = Modifier.weight(1f),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    "$hits",
                    color = if (hits >= 3) Accent else Color.White,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )
                if (score > 0) {
                    Text(
                        "+$score",
                        color = GoldAccent,
                        fontSize = 10.sp
                    )
                } else {
                    Spacer(Modifier.height(12.dp))
                }
            }
        }
    }
}
