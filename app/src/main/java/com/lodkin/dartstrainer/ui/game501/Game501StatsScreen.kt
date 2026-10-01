package com.lodkin.dartstrainer.ui.game501

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
import com.lodkin.dartstrainer.data.game501.Game501
import com.lodkin.dartstrainer.data.game501.Game501Logic
import com.lodkin.dartstrainer.data.game501.LegSnapshot501
import com.lodkin.dartstrainer.theme.Accent
import com.lodkin.dartstrainer.theme.GoldAccent
import com.lodkin.dartstrainer.theme.TileBg
import com.lodkin.dartstrainer.theme.TileBgDark
import java.util.Locale

@Composable
fun Game501StatsScreen(
    game: Game501,
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
                val winner = game.playersOfTeam(game.winnerIndex).firstOrNull()
                WinnerCard501(
                    name = winnerName,
                    setsWon = winner?.setsWon ?: 0,
                    setsTotal = game.setsPerMatch
                )
                Spacer(Modifier.height(20.dp))
            }

            // Итоги матча
            SectionTitle501("ИТОГИ МАТЧА")
            Spacer(Modifier.height(8.dp))
            game.players.forEach { p ->
                MatchPlayerCard501(
                    name = p.name,
                    darts = p.matchDarts,
                    scoreGained = p.matchScoreGained,
                    doublesHit = p.matchDoublesHit,
                    doublesAttempted = p.matchDoublesAttempted
                )
                Spacer(Modifier.height(6.dp))
            }

            Spacer(Modifier.height(24.dp))

            // Разбивка по легам
            if (game.legHistory.isNotEmpty()) {
                SectionTitle501("ПО ЛЕГАМ")
                Spacer(Modifier.height(8.dp))
                game.legHistory.reversed().forEach { snapshot ->
                    LegCard501(snapshot = snapshot)
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
private fun SectionTitle501(text: String) {
    Text(
        text = text,
        color = Accent,
        fontSize = 13.sp,
        fontWeight = FontWeight.Medium,
        letterSpacing = 3.sp
    )
}

@Composable
private fun WinnerCard501(name: String, setsWon: Int, setsTotal: Int) {
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
            Text("ПОБЕДА В МАТЧЕ", color = Accent, fontSize = 14.sp,
                fontWeight = FontWeight.Medium, letterSpacing = 3.sp)
            Spacer(Modifier.height(4.dp))
            Text(name, color = GoldAccent, fontSize = 22.sp,
                fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
            Spacer(Modifier.height(6.dp))
            Text("Сеты: $setsWon из $setsTotal", color = Color.White, fontSize = 14.sp)
        }
    }
}

@Composable
private fun MatchPlayerCard501(
    name: String,
    darts: Int,
    scoreGained: Int,
    doublesHit: Int,
    doublesAttempted: Int
) {
    val ppr = if (darts < 3) 0.0 else scoreGained.toDouble() / (darts / 3.0)
    val dblPct = if (doublesAttempted > 0) doublesHit.toDouble() / doublesAttempted * 100.0 else 0.0

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(TileBgDark)
            .padding(12.dp)
    ) {
        Text(name, color = Accent, fontSize = 16.sp, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(6.dp))
        StatLine501("Средний набор (PPR)", String.format(Locale.US, "%.2f", ppr))
        StatLine501("Очков набрано", scoreGained.toString())
        StatLine501("Дротиков", darts.toString())
        StatLine501("Удвоения", "${doublesHit} из ${doublesAttempted} (${String.format(Locale.US, "%.1f%%", dblPct)})")
    }
}

@Composable
private fun LegCard501(snapshot: LegSnapshot501) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(TileBg)
            .padding(12.dp)
    ) {
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

        snapshot.players.forEachIndexed { idx, p ->
            if (idx > 0) Spacer(Modifier.height(6.dp))
            val ppr = if (p.darts < 3) 0.0 else p.scoreGained.toDouble() / (p.darts / 3.0)
            val dblPct = if (p.doublesAttempted > 0) p.doublesHit.toDouble() / p.doublesAttempted * 100.0 else 0.0

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(TileBgDark)
                    .padding(10.dp)
            ) {
                Text(p.name, color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(4.dp))
                StatLine501("Средний набор", String.format(Locale.US, "%.2f", ppr))
                StatLine501("Очков за лег", p.scoreGained.toString())
                StatLine501("Дротиков", p.darts.toString())
                StatLine501("Удвоения", "${p.doublesHit} из ${p.doublesAttempted}")
            }
        }
    }
}

@Composable
private fun StatLine501(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, color = Color.White.copy(alpha = 0.8f), fontSize = 13.sp)
        Text(value, color = GoldAccent, fontSize = 13.sp, fontWeight = FontWeight.Bold)
    }
}
