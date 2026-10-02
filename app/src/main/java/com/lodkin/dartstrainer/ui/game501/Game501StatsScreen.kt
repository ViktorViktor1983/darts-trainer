package com.lodkin.dartstrainer.ui.game501

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.*
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
import com.lodkin.dartstrainer.data.game501.Player501
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
    var detailsExpanded by remember { mutableStateOf(false) }

    val winnerTeam = game.winnerIndex
    val winnerName = if (winnerTeam != null) game.playersOfTeam(winnerTeam).joinToString("/") { it.name } else "—"
    val loserTeam = if (winnerTeam != null) 1 - winnerTeam else 0
    val loserName = game.playersOfTeam(loserTeam).joinToString("/") { it.name }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0D1117))
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
            // ─────────────────────────────────────────────
            // ШАПКА: ПОБЕДИТЕЛЬ и СОПЕРНИК
            // ─────────────────────────────────────────────
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                PlayersTopBlock(
                    name = winnerName,
                    isWinner = true,
                    setsWon = game.playersOfTeam(winnerTeam ?: 0).firstOrNull()?.setsWon ?: 0,
                    setsTotal = game.setsPerMatch,
                    modifier = Modifier.weight(1f)
                )
                PlayersTopBlock(
                    name = loserName,
                    isWinner = false,
                    setsWon = game.playersOfTeam(loserTeam).firstOrNull()?.setsWon ?: 0,
                    setsTotal = game.setsPerMatch,
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(Modifier.height(20.dp))

            // ─────────────────────────────────────────────
            // 3 ГЛАВНЫХ ПАРАМЕТРА (по каждому игроку)
            // ─────────────────────────────────────────────
            SectionTitle("ГЛАВНЫЕ ПАРАМЕТРЫ")
            Spacer(Modifier.height(8.dp))

            game.players.forEach { p ->
                MainMetricsCard(p)
                Spacer(Modifier.height(8.dp))
            }

            Spacer(Modifier.height(16.dp))

            // ─────────────────────────────────────────────
            // РАСКРЫВАЮЩАЯСЯ ПОДРОБНАЯ СТАТИСТИКА
            // ─────────────────────────────────────────────
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(TileBgDark)
                    .clickable { detailsExpanded = !detailsExpanded }
                    .padding(vertical = 14.dp),
                contentAlignment = Alignment.Center
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("📊", fontSize = 18.sp)
                    Spacer(Modifier.width(8.dp))
                    Text(
                        if (detailsExpanded) "Свернуть подробности" else "Подробная статистика",
                        color = Accent,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        if (detailsExpanded) "▲" else "▼",
                        color = Accent,
                        fontSize = 12.sp
                    )
                }
            }

            AnimatedVisibility(
                visible = detailsExpanded,
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut()
            ) {
                Column {
                    Spacer(Modifier.height(16.dp))
                    DetailedStats(game)
                }
            }

            Spacer(Modifier.height(24.dp))
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

// ─────────────────────────────────────────────
// Шапка игрока (победитель / соперник)
// ─────────────────────────────────────────────
@Composable
private fun PlayersTopBlock(
    name: String,
    isWinner: Boolean,
    setsWon: Int,
    setsTotal: Int,
    modifier: Modifier
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(if (isWinner) Color(0xFF1A2A33) else TileBgDark)
            .padding(12.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        if (isWinner) {
            Text("🏆", fontSize = 22.sp)
            Spacer(Modifier.height(2.dp))
        }
        Text(
            name,
            color = if (isWinner) GoldAccent else Color.White,
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
            maxLines = 2
        )
        Spacer(Modifier.height(4.dp))
        Text(
            "Сеты: $setsWon из $setsTotal",
            color = Color.White.copy(alpha = 0.7f),
            fontSize = 12.sp
        )
    }
}

// ─────────────────────────────────────────────
// Карточка с 3 главными параметрами
// ─────────────────────────────────────────────
@Composable
private fun MainMetricsCard(player: Player501) {
    val ppr = Game501Logic.matchPpr(player)
    val dblPct = Game501Logic.doublesAccuracy(player)
    val avgDartsPerLeg = if (player.listOfLegDarts.isEmpty()) 0.0
        else player.listOfLegDarts.average()

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(TileBg)
            .padding(12.dp)
    ) {
        Text(player.name, color = Accent, fontSize = 14.sp, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(8.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            MetricCell("Средний набор", String.format(Locale.US, "%.2f", ppr), Modifier.weight(1f))
            MetricCell("Удвоения", String.format(Locale.US, "%.1f%%", dblPct), Modifier.weight(1f))
            MetricCell("Дротиков на лег", String.format(Locale.US, "%.1f", avgDartsPerLeg), Modifier.weight(1f))
        }
    }
}

@Composable
private fun MetricCell(label: String, value: String, modifier: Modifier) {
    Column(modifier = modifier, horizontalAlignment = Alignment.CenterHorizontally) {
        Text(label, color = Color.White.copy(alpha = 0.6f), fontSize = 11.sp, textAlign = TextAlign.Center)
        Spacer(Modifier.height(4.dp))
        Text(value, color = GoldAccent, fontSize = 18.sp, fontWeight = FontWeight.Bold)
    }
}

// ─────────────────────────────────────────────
// Подробная статистика
// ─────────────────────────────────────────────
@Composable
private fun DetailedStats(game: Game501) {
    // Категории по суммам
    SectionTitle("КАТЕГОРИИ СУММ")
    Spacer(Modifier.height(8.dp))
    CategoriesTable(game.players)

    Spacer(Modifier.height(16.dp))

    // Набор без закрытия + первые 9 дротиков
    SectionTitle("НАБОРЫ")
    Spacer(Modifier.height(8.dp))
    game.players.forEach { p ->
        NonCloseAndFirst9Card(p)
        Spacer(Modifier.height(6.dp))
    }

    Spacer(Modifier.height(16.dp))

    // Все окончания
    SectionTitle("ВСЕ ОКОНЧАНИЯ")
    Spacer(Modifier.height(8.dp))
    game.players.forEach { p ->
        CloseValuesCard(p)
        Spacer(Modifier.height(6.dp))
    }

    Spacer(Modifier.height(16.dp))

    // Разбивка по легам
    if (game.legHistory.isNotEmpty()) {
        SectionTitle("ПО ЛЕГАМ")
        Spacer(Modifier.height(8.dp))
        game.legHistory.reversed().forEach { snapshot ->
            LegDetailedRow(snapshot)
            Spacer(Modifier.height(6.dp))
        }
    }
}

// ─────────────────────────────────────────────
// Категории сумм (таблица)
// ─────────────────────────────────────────────
@Composable
private fun CategoriesTable(players: List<Player501>) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(TileBg)
            .padding(10.dp)
    ) {
        // Шапка
        Row(modifier = Modifier.fillMaxWidth()) {
            Text("Категория", color = Color.White.copy(alpha = 0.6f), fontSize = 12.sp,
                fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
            players.forEach { p ->
                Text(p.name.take(8), color = Accent, fontSize = 11.sp,
                    fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f),
                    textAlign = TextAlign.Center)
            }
        }
        Spacer(Modifier.height(6.dp))

        CategoryRow("180", players.map { it.matchCount180 })
        CategoryRow("170+", players.map { it.matchCount170plus })
        CategoryRow("130+", players.map { it.matchCount130plus })
        CategoryRow("90+", players.map { it.matchCount90plus })
        CategoryRow("57+", players.map { it.matchCount57plus })
        CategoryRow("57−", players.map { it.matchCount57minus })
    }
}

@Composable
private fun CategoryRow(label: String, counts: List<Int>) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 3.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(label, color = Color.White, fontSize = 13.sp, modifier = Modifier.weight(1f))
        counts.forEach { c ->
            Text("$c", color = GoldAccent, fontSize = 14.sp,
                fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f),
                textAlign = TextAlign.Center)
        }
    }
}

// ─────────────────────────────────────────────
// Набор без закрытия + первые 9 дротиков
// ─────────────────────────────────────────────
@Composable
private fun NonCloseAndFirst9Card(player: Player501) {
    val first9 = Game501Logic.first9Ppr(player)
    val nonClose = Game501Logic.nonClosePpr(player)

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(TileBg)
            .padding(10.dp)
    ) {
        Text(player.name, color = Accent, fontSize = 13.sp, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(6.dp))
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text("Первые 9 дротиков", color = Color.White.copy(alpha = 0.8f), fontSize = 12.sp)
            Text(String.format(Locale.US, "%.2f", first9),
                color = GoldAccent, fontSize = 13.sp, fontWeight = FontWeight.Bold)
        }
        Spacer(Modifier.height(3.dp))
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text("Набор без закрытия (>170)", color = Color.White.copy(alpha = 0.8f), fontSize = 12.sp)
            Text(String.format(Locale.US, "%.2f", nonClose),
                color = GoldAccent, fontSize = 13.sp, fontWeight = FontWeight.Bold)
        }
    }
}

// ─────────────────────────────────────────────
// Значения, с которых игрок закрывал лег
// ─────────────────────────────────────────────
@Composable
private fun CloseValuesCard(player: Player501) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(TileBg)
            .padding(10.dp)
    ) {
        Text(player.name, color = Accent, fontSize = 13.sp, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(6.dp))
        Text(
            if (player.listOfCloseValues.isEmpty()) "—"
            else player.listOfCloseValues.joinToString(", "),
            color = Color.White,
            fontSize = 13.sp
        )
    }
}

// ─────────────────────────────────────────────
// Разбивка по легу (со соперником)
// ─────────────────────────────────────────────
@Composable
private fun LegDetailedRow(snapshot: LegSnapshot501) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(TileBg)
            .padding(10.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text("Сет ${snapshot.setNumber} • Лег ${snapshot.legNumber}",
                color = Accent, fontSize = 13.sp, fontWeight = FontWeight.Bold)
            Text("Победа: команда ${if (snapshot.winningTeam == 0) "A" else "B"}",
                color = GoldAccent, fontSize = 11.sp)
        }
        Spacer(Modifier.height(6.dp))

        // Строки по игрокам
        snapshot.players.forEachIndexed { idx, p ->
            if (idx > 0) Spacer(Modifier.height(4.dp))
            val ppr = if (p.darts < 3) 0.0 else p.scoreGained.toDouble() / (p.darts / 3.0)
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(p.name, color = Color.White, fontSize = 12.sp,
                    modifier = Modifier.weight(1f))
                Text(String.format(Locale.US, "%.2f", ppr),
                    color = GoldAccent, fontSize = 12.sp,
                    fontWeight = FontWeight.Bold, modifier = Modifier.width(60.dp),
                    textAlign = TextAlign.End)
                Text("(${p.darts})", color = Color.White.copy(alpha = 0.6f),
                    fontSize = 11.sp, modifier = Modifier.width(45.dp),
                    textAlign = TextAlign.End)
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
