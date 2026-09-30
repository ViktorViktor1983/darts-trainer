package com.lodkin.dartstrainer.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
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
import com.lodkin.dartstrainer.data.cricket.CricketGameEntity
import com.lodkin.dartstrainer.data.cricket.CricketRepository
import com.lodkin.dartstrainer.theme.Accent
import com.lodkin.dartstrainer.theme.ErrorColor
import com.lodkin.dartstrainer.theme.GoldAccent
import com.lodkin.dartstrainer.theme.TileBg
import com.lodkin.dartstrainer.theme.TileBgDark
import kotlinx.coroutines.launch

// ─────────────────────────────────────────────
// Период фильтра
// ─────────────────────────────────────────────
private enum class StatPeriod(val label: String, val daysBack: Long?) {
    WEEK("Неделя", 7L),
    MONTH("Месяц", 30L),
    THREE_MONTHS("3 мес", 90L),
    HALF_YEAR("Полгода", 180L),
    YEAR("Год", 365L),
    ALL("Всё время", null)
}

// ─────────────────────────────────────────────
// Агрегат статистики крикета (только по игрокам-людям)
// ─────────────────────────────────────────────
private data class CricketAggregate(
    val matches: Int = 0,
    val legs: Int = 0,
    val darts: Int = 0,
    val misses: Int = 0,
    val triples: Int = 0,
    val bullAttempts: Int = 0,
    val bullHits: Int = 0,
    val perfectRounds: Int = 0,
    val strongRounds: Int = 0,
    val avgMpr: Double = 0.0,
    val bestMpr: Double = 0.0
)

// ─────────────────────────────────────────────
// Экран статистики
// ─────────────────────────────────────────────
@Composable
fun StatsScreen(
    repository: CricketRepository,
    onBack: () -> Unit
) {
    val scope = rememberCoroutineScope()
    var selectedTab by remember { mutableStateOf(0) }   // 0 = Крикет, 1 = 501
    var period by remember { mutableStateOf(StatPeriod.ALL) }
    var games by remember { mutableStateOf<List<CricketGameEntity>>(emptyList()) }
    var showResetDialog by remember { mutableStateOf(false) }
    var reloadKey by remember { mutableStateOf(0) }

    LaunchedEffect(period, reloadKey) {
        val all = repository.getAllGames()
        val cutoff = period.daysBack?.let {
            System.currentTimeMillis() - it * 24L * 60L * 60L * 1000L
        }
        games = if (cutoff == null) all else all.filter { it.dateMillis >= cutoff }
    }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        // Верхняя панель
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .background(TileBgDark)
                    .clickable { onBack() }
                    .padding(horizontal = 16.dp, vertical = 10.dp)
            ) {
                Text("← Назад", color = Accent, fontSize = 15.sp)
            }
            Spacer(Modifier.width(12.dp))
            Text(
                "Статистика",
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
        }

        Spacer(Modifier.height(16.dp))

        // Табы
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            TabButton("КРИКЕТ", selectedTab == 0, { selectedTab = 0 }, Modifier.weight(1f))
            TabButton("501", selectedTab == 1, { selectedTab = 1 }, Modifier.weight(1f))
        }

        Spacer(Modifier.height(16.dp))

        if (selectedTab == 0) {
            CricketTabContent(
                games = games,
                period = period,
                onPeriodChange = { period = it },
                onResetClick = { showResetDialog = true }
            )
        } else {
            PlaceholderTab("Статистика 501", "Раздел в разработке")
        }
    }

    if (showResetDialog) {
        AlertDialog(
            onDismissRequest = { showResetDialog = false },
            confirmButton = {
                TextButton(onClick = {
                    scope.launch {
                        repository.clearAll()
                        reloadKey++
                    }
                    showResetDialog = false
                }) { Text("УДАЛИТЬ", color = ErrorColor) }
            },
            dismissButton = {
                TextButton(onClick = { showResetDialog = false }) {
                    Text("Отмена", color = Accent)
                }
            },
            title = { Text("Сбросить статистику?", color = Color.White) },
            text = { Text("Все матчи по крикету будут удалены. Отменить это действие нельзя.", color = Color.White) }
        )
    }
}

// ─────────────────────────────────────────────
// Контент вкладки Крикет
// ─────────────────────────────────────────────
@Composable
private fun CricketTabContent(
    games: List<CricketGameEntity>,
    period: StatPeriod,
    onPeriodChange: (StatPeriod) -> Unit,
    onResetClick: () -> Unit
) {
    val aggregate = remember(games) { computeAggregate(games) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
    ) {
        // Фильтр по периоду
        PeriodSelector(period = period, onPeriodChange = onPeriodChange)

        Spacer(Modifier.height(16.dp))

        if (aggregate.matches == 0) {
            EmptyStats()
        } else {
            // Блок 1 — Основные
            SectionTitle("ОСНОВНЫЕ")
            Spacer(Modifier.height(8.dp))
            StatRowCard("Матчей сыграно", aggregate.matches.toString())
            Spacer(Modifier.height(6.dp))
            StatRowCard("Легов сыграно", aggregate.legs.toString())
            Spacer(Modifier.height(6.dp))
            StatRowCard("Средний набор (MPR)", "%.2f".format(aggregate.avgMpr))
            Spacer(Modifier.height(6.dp))
            StatRowCard("Лучший MPR за матч", "%.2f".format(aggregate.bestMpr))

            Spacer(Modifier.height(20.dp))

            // Блок 2 — Точность
            SectionTitle("ТОЧНОСТЬ")
            Spacer(Modifier.height(8.dp))
            StatRowCard(
                label = "Промахи",
                value = percentLabel(aggregate.misses, aggregate.darts),
                sub = "${aggregate.misses} из ${aggregate.darts} дротиков"
            )
            Spacer(Modifier.height(6.dp))
            StatRowCard(
                label = "Утроения",
                value = percentLabel(aggregate.triples, aggregate.darts),
                sub = "${aggregate.triples} утроений"
            )
            Spacer(Modifier.height(6.dp))
            StatRowCard(
                label = "Точность Bull",
                value = if (aggregate.bullAttempts > 0)
                    percentLabel(aggregate.bullHits, aggregate.bullAttempts)
                else "—",
                sub = if (aggregate.bullAttempts > 0)
                    "${aggregate.bullHits} из ${aggregate.bullAttempts} прицельных"
                else "Нет прицельных бросков"
            )

            Spacer(Modifier.height(20.dp))

            // Блок 3 — Достижения
            SectionTitle("ДОСТИЖЕНИЯ")
            Spacer(Modifier.height(8.dp))
            StatRowCard(
                label = "Идеальные подходы (8–9)",
                value = "${aggregate.perfectRounds} раз",
                sub = "в ${aggregate.legs} легах"
            )
            Spacer(Modifier.height(6.dp))
            StatRowCard(
                label = "Сильные подходы (6–7)",
                value = "${aggregate.strongRounds} раз",
                sub = "в ${aggregate.legs} легах"
            )

            Spacer(Modifier.height(24.dp))

            // Кнопка сброса
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(TileBgDark)
                    .clickable { onResetClick() }
                    .padding(vertical = 14.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    "Сбросить статистику",
                    color = ErrorColor,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium
                )
            }

            Spacer(Modifier.height(24.dp))
        }
    }
}

// ─────────────────────────────────────────────
// Фильтр по периоду
// ─────────────────────────────────────────────
@Composable
private fun PeriodSelector(
    period: StatPeriod,
    onPeriodChange: (StatPeriod) -> Unit
) {
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            listOf(StatPeriod.WEEK, StatPeriod.MONTH, StatPeriod.THREE_MONTHS).forEach { p ->
                PeriodChip(p.label, p == period, { onPeriodChange(p) }, Modifier.weight(1f))
            }
        }
        Spacer(Modifier.height(6.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            listOf(StatPeriod.HALF_YEAR, StatPeriod.YEAR, StatPeriod.ALL).forEach { p ->
                PeriodChip(p.label, p == period, { onPeriodChange(p) }, Modifier.weight(1f))
            }
        }
    }
}

@Composable
private fun PeriodChip(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(10.dp))
            .background(if (selected) Accent else TileBgDark)
            .clickable { onClick() }
            .padding(vertical = 10.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            label,
            color = if (selected) Color(0xFF121212) else Color.White,
            fontSize = 12.sp,
            fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
            textAlign = TextAlign.Center
        )
    }
}

// ─────────────────────────────────────────────
// UI-компоненты
// ─────────────────────────────────────────────
@Composable
private fun TabButton(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(if (selected) Accent else TileBgDark)
            .clickable { onClick() }
            .padding(vertical = 12.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            label,
            color = if (selected) Color(0xFF121212) else Color.White,
            fontSize = 14.sp,
            fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
            letterSpacing = 2.sp
        )
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
private fun StatRowCard(label: String, value: String, sub: String? = null) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(TileBg)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(label, color = Color.White, fontSize = 14.sp)
            if (sub != null) {
                Text(sub, color = Color.White.copy(alpha = 0.5f), fontSize = 11.sp)
            }
        }
        Text(
            value,
            color = GoldAccent,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
private fun EmptyStats() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(TileBgDark)
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            "Пока нет сыгранных матчей\nза выбранный период.",
            color = Color.White.copy(alpha = 0.7f),
            fontSize = 14.sp,
            textAlign = TextAlign.Center,
            lineHeight = 20.sp
        )
    }
}

@Composable
private fun PlaceholderTab(title: String, subtitle: String) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(TileBgDark)
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(title, color = Accent, fontSize = 16.sp, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(6.dp))
            Text(subtitle, color = Color.White.copy(alpha = 0.7f), fontSize = 13.sp)
        }
    }
}

// ─────────────────────────────────────────────
// Логика подсчёта
// ─────────────────────────────────────────────
private fun parseStringList(s: String): List<String> =
    if (s.isBlank()) emptyList() else s.split("|")

private fun parseIntList(s: String): List<Int> =
    if (s.isBlank()) emptyList() else s.split("|").map { it.toIntOrNull() ?: 0 }

private fun parseDoubleList(s: String): List<Double> =
    if (s.isBlank()) emptyList() else s.split("|").map { it.toDoubleOrNull() ?: 0.0 }

private fun percentLabel(part: Int, total: Int): String {
    if (total <= 0) return "—"
    val p = part.toDouble() / total * 100.0
    return "%.1f%%".format(p)
}

private fun computeAggregate(games: List<CricketGameEntity>): CricketAggregate {
    var matches = 0
    var legs = 0
    var darts = 0
    var misses = 0
    var triples = 0
    var bullAttempts = 0
    var bullHits = 0
    var perfect = 0
    var strong = 0
    val mprValues = mutableListOf<Double>()

    for (g in games) {
        val bots = parseStringList(g.playerIsBot)
        val dartsList = parseIntList(g.totalDarts)
        val missesList = parseIntList(g.misses)
        val triplesList = parseIntList(g.triples)
        val bullAttList = parseIntList(g.bullAttempts)
        val bullHitList = parseIntList(g.bullHits)
        val perfList = parseIntList(g.perfectRounds)
        val strongList = parseIntList(g.strongRounds)
        val mprList = parseDoubleList(g.mpr)

        var hasHuman = false
        for (i in bots.indices) {
            if (bots[i] == "0") {
                hasHuman = true
                darts += dartsList.getOrElse(i) { 0 }
                misses += missesList.getOrElse(i) { 0 }
                triples += triplesList.getOrElse(i) { 0 }
                bullAttempts += bullAttList.getOrElse(i) { 0 }
                bullHits += bullHitList.getOrElse(i) { 0 }
                perfect += perfList.getOrElse(i) { 0 }
                strong += strongList.getOrElse(i) { 0 }
                if (i < mprList.size) mprValues.add(mprList[i])
            }
        }
        if (hasHuman) {
            matches++
            legs += g.legsPlayed
        }
    }

    val avgMpr = if (mprValues.isEmpty()) 0.0 else mprValues.average()
    val bestMpr = mprValues.maxOrNull() ?: 0.0

    return CricketAggregate(
        matches = matches,
        legs = legs,
        darts = darts,
        misses = misses,
        triples = triples,
        bullAttempts = bullAttempts,
        bullHits = bullHits,
        perfectRounds = perfect,
        strongRounds = strong,
        avgMpr = avgMpr,
        bestMpr = bestMpr
    )
}
