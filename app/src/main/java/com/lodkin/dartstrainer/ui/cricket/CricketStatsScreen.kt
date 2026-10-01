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
import com.lodkin.dartstrainer.data.cricket.LegSnapshot
import com.lodkin.dartstrainer.theme.Accent
import com.lodkin.dartstrainer.theme.GoldAccent
import com.lodkin.dartstrainer.theme.TileBg
import com.lodkin.dartstrainer.theme.TileBgDark
import java.util.Locale

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
            fontSize = 14.sp,
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
                val winnerName = game.playersOfTeam(game.winnerIndex).joinToString("/") { it.name }
                WinnerCard(
                    name = winnerName,
                    setsWon = game.playersOfTeam(game.winnerIndex).firstOrNull()?.setsWon ?: 0,
                    setsTotal = game.setsPerMatch
                )
                Spacer(Modifier.height(20.dp))
            }

            // Итоги за матч по игрокам
            SectionTitle("ИТОГИ МАТЧА")
            Spacer(Modifier.height(8.dp))
            game.players.forEach { p ->
                MatchPlayerCard(
                    name = p.name,
                    darts = p.matchDartsThrown,
                    marks = p.matchHits.values.sum(),
                    misses = p.matchMissesThrown,
                    triples = p.matchTriplesHit,
                    score = p.matchTotalScore
                )
                Spacer(Modifier.height(6.dp))
            }

            Spacer(Modifier.height(24.dp))

            // Разбивка по легам (от последнего к первому)
            if (game.legHistory.isNotEmpty()) {
                SectionTitle("ПО ЛЕГАМ")
                Spacer(Modifier.height(8.dp))
                game.legHistory.reversed().forEach { snapshot ->
                    LegCard(snapshot = snapshot)
                    Spacer(Modifier.height(8.dp))
                }
            }
        }

        Spacer(Modifier.height(12.dp))

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
                Text("Ещё раз", color = Color(0xFF121212), fontSize = 14.sp, fontWeight = FontWeight.Bold)
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
private fun WinnerCard(name: String, setsWon: Int, setsTotal: Int) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(Color(0xFF1A2A33))
            .padding(20.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text("🏆", fontSize = 40.sp)
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
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
            Spacer(Modifier.height(6.dp))
            Text(
                "Сеты: $setsWon из $setsTotal",
                color = Color.White,
                fontSize = 14.sp
            )
        }
    }
}

// ─────────────────────────────────────────────
// Карточка игрока за весь матч
// ─────────────────────────────────────────────
@Composable
private fun MatchPlayerCard(
    name: String,
    darts: Int,
    marks: Int,
    misses: Int,
    triples: Int,
    score: Int
) {
    val mpr = if (darts < 3) 0.0 else marks.toDouble() / (darts / 3.0)
    val missPct = if (darts <= 0) 0.0 else misses.toDouble() / darts * 100.0
    val triplesPct = if (darts <= 0) 0.0 else triples.toDouble() / darts * 100.0

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(TileBgDark)
            .padding(12.dp)
    ) {
        Text(name, color = Accent, fontSize = 16.sp, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(6.dp))
        StatLine("Средний набор (MPR)", String.format(Locale.US, "%.2f", mpr))
        StatLine("Промахи", String.format(Locale.US, "%.1f%%", missPct))
        StatLine("Утроения", String.format(Locale.US, "%.1f%%", triplesPct))
        StatLine("Очки за матч", score.toString())
        StatLine("Дротиков за матч", darts.toString())
    }
}

// ─────────────────────────────────────────────
// Карточка одного лега
// ─────────────────────────────────────────────
@Composable
private fun LegCard(snapshot: LegSnapshot) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(TileBg)
            .padding(12.dp)
    ) {
        // Шапка лега
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                "Сет ${snapshot.setNumber} • Лег ${snapshot.legNumber}",
                color = Accent,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                "Победа: команда ${if (snapshot.winningTeam == 0) "A" else "B"}",
                color = GoldAccent,
                fontSize = 12.sp
            )
        }

        Spacer(Modifier.height(8.dp))

        // Метрики по каждому игроку
        snapshot.players.forEachIndexed { idx, p ->
            if (idx > 0) Spacer(Modifier.height(6.dp))

            val mpr = if (p.darts < 3) 0.0 else p.legMarks.toDouble() / (p.darts / 3.0)
            val missPct = if (p.darts <= 0) 0.0 else p.misses.toDouble() / p.darts * 100.0
            val triplesPct = if (p.darts <= 0) 0.0 else p.triples.toDouble() / p.darts * 100.0

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(TileBgDark)
                    .padding(10.dp)
            ) {
                Text(p.name, color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold)
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
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, color = Color.White.copy(alpha = 0.8f), fontSize = 13.sp)
        Text(value, color = GoldAccent, fontSize = 13.sp, fontWeight = FontWeight.Bold)
    }
}
