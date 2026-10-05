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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.lodkin.dartstrainer.data.SettingsStorage
import com.lodkin.dartstrainer.data.cricket.CricketGameEntity
import com.lodkin.dartstrainer.data.cricket.CricketRepository
import com.lodkin.dartstrainer.data.game501.CheckoutTable
import com.lodkin.dartstrainer.data.game501.Game501Database
import com.lodkin.dartstrainer.data.game501.Game501Entity
import com.lodkin.dartstrainer.data.game501.Game501Repository
import com.lodkin.dartstrainer.theme.Accent
import com.lodkin.dartstrainer.theme.ErrorColor
import com.lodkin.dartstrainer.theme.GoldAccent
import com.lodkin.dartstrainer.theme.TileBg
import com.lodkin.dartstrainer.theme.TileBgDark
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

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
// Список валидных чекаутов (2..170 минус bogey numbers)
// ─────────────────────────────────────────────
private val VALID_CHECKOUTS: List<Int> by lazy {
    (2..170).filter { CheckoutTable.isCheckoutPossible(it) }
}

// ─────────────────────────────────────────────
// Цвета чек-аутов
// ─────────────────────────────────────────────
private val CheckoutGray = Color(0xFF455A64)
private val CheckoutGreen = Color(0xFF4CAF50)
private val CheckoutYellow = Color(0xFFFFD54F)
private val CheckoutRed = Color(0xFFE53935)
private val CheckoutPurple = Color(0xFF9C27B0)
private val CheckoutIndigo = Color(0xFF3F51B5)
private val CheckoutTeal = Color(0xFF00BCD4)
private val CheckoutGold = Color(0xFFFFC107)
private val CheckoutDiamond = Color(0xFFB3E5FC)

private fun checkoutColor(count: Int): Pair<Color, Color> {
    return when {
        count >= 100 -> CheckoutDiamond to Color(0xFF0D1117)
        count >= 50  -> CheckoutGold to Color(0xFF121212)
        count >= 30  -> CheckoutTeal to Color(0xFF121212)
        count >= 20  -> CheckoutIndigo to Color.White
        count >= 10  -> CheckoutPurple to Color.White
        count >= 5   -> CheckoutRed to Color.White
        count >= 3   -> CheckoutYellow to Color(0xFF121212)
        count >= 1   -> CheckoutGreen to Color.White
        else         -> CheckoutGray to Color.White.copy(alpha = 0.45f)
    }
}

// ─────────────────────────────────────────────
// Агрегаты
// ─────────────────────────────────────────────
private data class CricketAggregate(
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

private data class Game501Aggregate(
    val legs: Int = 0,
    val darts: Int = 0,
    val doublesHit: Int = 0,
    val doublesAttempted: Int = 0,
    val wins: Int = 0,
    val totalMatches: Int = 0,
    val avgPpr: Double = 0.0,
    val bestMatchPpr: Double = 0.0,
    val bestLegPpr: Double = 0.0,
    val avgDartsPerLeg: Double = 0.0,
    val avgCount180: Double = 0.0,
    val avgCount170plus: Double = 0.0,
    val avgCount130plus: Double = 0.0,
    val avgCount90plus: Double = 0.0,
    val avgCount57plus: Double = 0.0,
    val avgCount57minus: Double = 0.0,
    val first9: Double = 0.0,
    val nonClose: Double = 0.0,
    val currentStreak: Int = 0,
    val maxStreak: Int = 0,
    val trend: Double = 0.0,
    val hasTrend: Boolean = false,
    val byBot: List<BotStat> = emptyList(),
    val topMatches: List<MatchInfo> = emptyList(),
    val closedCheckouts: Map<Int, Int> = emptyMap()
)

private data class BotStat(
    val botName: String,
    val matches: Int,
    val ppr: Double,
    val dblPct: Double
)

private data class MatchInfo(
    val dateMillis: Long,
    val ppr: Double,
    val opponent: String,
    val dartsPerLeg: Double
)

// ─────────────────────────────────────────────
// Экран статистики
// ─────────────────────────────────────────────
@Composable
fun StatsScreen(
    repository: CricketRepository,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val game501Repo = remember {
        Game501Repository(Game501Database.get(context).game501Dao())
    }
    // Имя владельца телефона (из онбординга).
    // Если пустое — считаем статистику по всем людям (старое поведение).
    val ownerName = remember { SettingsStorage.getPlayerName(context).trim() }

    var selectedTab by remember { mutableStateOf(0) }
    var period by remember { mutableStateOf(StatPeriod.ALL) }
    var games by remember { mutableStateOf<List<CricketGameEntity>>(emptyList()) }
    var games501 by remember { mutableStateOf<List<Game501Entity>>(emptyList()) }
    var showResetDialog by remember { mutableStateOf(false) }
    var reloadKey by remember { mutableStateOf(0) }

    LaunchedEffect(period, reloadKey) {
        val cutoff = period.daysBack?.let {
            System.currentTimeMillis() - it * 24L * 60L * 60L * 1000L
        }
        val allCricket = repository.getAllGames()
        games = if (cutoff == null) allCricket else allCricket.filter { it.dateMillis >= cutoff }

        val all501 = game501Repo.getAllGames()
        games501 = if (cutoff == null) all501 else all501.filter { it.dateMillis >= cutoff }
    }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
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
            Column {
                Text("Статистика", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = Color.White)
                if (ownerName.isNotBlank()) {
                    Text(
                        "игрок: $ownerName",
                        color = Accent,
                        fontSize = 12.sp
                    )
                }
            }
        }

        Spacer(Modifier.height(16.dp))

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
                ownerName = ownerName,
                period = period,
                onPeriodChange = { period = it },
                onResetClick = { showResetDialog = true }
            )
        } else {
            Game501TabContent(
                games = games501,
                ownerName = ownerName,
                period = period,
                onPeriodChange = { period = it },
                onResetClick = { showResetDialog = true }
            )
        }
    }

    if (showResetDialog) {
        val isCricket = selectedTab == 0
        AlertDialog(
            onDismissRequest = { showResetDialog = false },
            confirmButton = {
                TextButton(onClick = {
                    scope.launch {
                        if (isCricket) repository.clearAll()
                        else game501Repo.clearAll()
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
            title = {
                Text(
                    if (isCricket) "Сбросить статистику крикета?"
                    else "Сбросить статистику 501?",
                    color = Color.White
                )
            },
            text = {
                Text(
                    if (isCricket) "Все матчи по крикету будут удалены. Отменить это действие нельзя."
                    else "Все матчи x01 будут удалены. Отменить это действие нельзя.",
                    color = Color.White
                )
            }
        )
    }
}

// ─────────────────────────────────────────────
// Контент вкладки Крикет
// ─────────────────────────────────────────────
@Composable
private fun CricketTabContent(
    games: List<CricketGameEntity>,
    ownerName: String,
    period: StatPeriod,
    onPeriodChange: (StatPeriod) -> Unit,
    onResetClick: () -> Unit
) {
    val aggregate = remember(games, ownerName) { computeAggregate(games, ownerName) }

    Column(modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
        PeriodSelector(period = period, onPeriodChange = onPeriodChange)
        Spacer(Modifier.height(16.dp))

        if (aggregate.legs == 0) {
            EmptyStats()
        } else {
            SectionTitle("ОСНОВНЫЕ")
            Spacer(Modifier.height(8.dp))
            StatRowCard("Легов сыграно", aggregate.legs.toString())
            Spacer(Modifier.height(6.dp))
            StatRowCard("Средний набор (MPR)", "%.2f".format(Locale.US, aggregate.avgMpr))
            Spacer(Modifier.height(6.dp))
            StatRowCard("Лучший MPR за матч", "%.2f".format(Locale.US, aggregate.bestMpr))

            Spacer(Modifier.height(20.dp))
            SectionTitle("ТОЧНОСТЬ")
            Spacer(Modifier.height(8.dp))
            StatRowCard("Промахи", percentLabel(aggregate.misses, aggregate.darts),
                "${aggregate.misses} из ${aggregate.darts} дротиков")
            Spacer(Modifier.height(6.dp))
            StatRowCard("Утроения", percentLabel(aggregate.triples, aggregate.darts),
                "${aggregate.triples} утроений")
            Spacer(Modifier.height(6.dp))
            StatRowCard(
                "Точность Bull",
                if (aggregate.bullAttempts > 0) percentLabel(aggregate.bullHits, aggregate.bullAttempts) else "—",
                if (aggregate.bullAttempts > 0) "${aggregate.bullHits} из ${aggregate.bullAttempts} прицельных"
                else "Нет прицельных бросков"
            )

            Spacer(Modifier.height(20.dp))
            SectionTitle("ДОСТИЖЕНИЯ")
            Spacer(Modifier.height(8.dp))
            StatRowCard("Идеальные подходы (8–9)", "${aggregate.perfectRounds} раз", "в ${aggregate.legs} легах")
            Spacer(Modifier.height(6.dp))
            StatRowCard("Сильные подходы (6–7)", "${aggregate.strongRounds} раз", "в ${aggregate.legs} легах")

            Spacer(Modifier.height(24.dp))
            ResetButton(onResetClick)
            Spacer(Modifier.height(24.dp))
        }
    }
}

// ─────────────────────────────────────────────
// Контент вкладки 501
// ─────────────────────────────────────────────
@Composable
private fun Game501TabContent(
    games: List<Game501Entity>,
    ownerName: String,
    period: StatPeriod,
    onPeriodChange: (StatPeriod) -> Unit,
    onResetClick: () -> Unit
) {
    val agg = remember(games, ownerName) { computeAggregate501(games, ownerName) }
    var showCheckoutsDialog by remember { mutableStateOf(false) }

    Column(modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
        PeriodSelector(period = period, onPeriodChange = onPeriodChange)
        Spacer(Modifier.height(16.dp))

        if (agg.totalMatches == 0) {
            EmptyStats()
            return@Column
        }

        SectionTitle("ОСНОВНЫЕ")
        Spacer(Modifier.height(8.dp))
        StatRowCard("Средний набор (PPR)", "%.2f".format(Locale.US, agg.avgPpr))
        Spacer(Modifier.height(6.dp))
        StatRowCard(
            "Точность удвоений",
            if (agg.doublesAttempted > 0) percentLabel(agg.doublesHit, agg.doublesAttempted) else "—",
            "${agg.doublesHit} из ${agg.doublesAttempted} попыток"
        )
        Spacer(Modifier.height(6.dp))
        StatRowCard(
            "Средний дротиков на лег",
            if (agg.avgDartsPerLeg > 0) "%.1f".format(Locale.US, agg.avgDartsPerLeg) else "—",
            "всего ${agg.darts} дротиков"
        )

        Spacer(Modifier.height(20.dp))

        SectionTitle("КАТЕГОРИИ СУММ (в среднем за лег)")
        Spacer(Modifier.height(8.dp))
        StatRowCard("180", "%.2f".format(Locale.US, agg.avgCount180))
        Spacer(Modifier.height(6.dp))
        StatRowCard("170+", "%.2f".format(Locale.US, agg.avgCount170plus))
        Spacer(Modifier.height(6.dp))
        StatRowCard("130+", "%.2f".format(Locale.US, agg.avgCount130plus))
        Spacer(Modifier.height(6.dp))
        StatRowCard("90+", "%.2f".format(Locale.US, agg.avgCount90plus))
        Spacer(Modifier.height(6.dp))
        StatRowCard("57+", "%.2f".format(Locale.US, agg.avgCount57plus))
        Spacer(Modifier.height(6.dp))
        StatRowCard("57−", "%.2f".format(Locale.US, agg.avgCount57minus))

        Spacer(Modifier.height(20.dp))

        SectionTitle("НАБОРЫ")
        Spacer(Modifier.height(8.dp))
        StatRowCard(
            "Первые 9 дротиков",
            if (agg.first9 > 0) "%.2f".format(Locale.US, agg.first9) else "—",
            "средний набор за первые 3 подхода"
        )
        Spacer(Modifier.height(6.dp))
        StatRowCard(
            "Набор без закрытия (>170)",
            if (agg.nonClose > 0) "%.2f".format(Locale.US, agg.nonClose) else "—",
            "средний набор на подходе, когда остаток >170"
        )

        Spacer(Modifier.height(20.dp))

        if (agg.byBot.isNotEmpty()) {
            SectionTitle("ПО УРОВНЯМ БОТОВ")
            Spacer(Modifier.height(8.dp))
            agg.byBot.forEach { b ->
                StatRowCard(
                    label = b.botName,
                    value = "%.2f PPR".format(Locale.US, b.ppr),
                    sub = "${b.matches} ${pluralMatches(b.matches)} · удв. %.1f%%".format(Locale.US, b.dblPct)
                )
                Spacer(Modifier.height(6.dp))
            }
            Spacer(Modifier.height(20.dp))
        }

        SectionTitle("ДОСТИЖЕНИЯ")
        Spacer(Modifier.height(8.dp))
        StatRowCard("Лучший PPR за матч", "%.2f".format(Locale.US, agg.bestMatchPpr))
        Spacer(Modifier.height(6.dp))
        StatRowCard("Лучший PPR за лег", "%.2f".format(Locale.US, agg.bestLegPpr))
        Spacer(Modifier.height(6.dp))
        StatRowCard(
            "Побед в матчах",
            "${agg.wins} из ${agg.totalMatches}",
            if (agg.totalMatches > 0)
                "%.0f%% побед".format(Locale.US, agg.wins.toDouble() / agg.totalMatches * 100.0)
            else null
        )
        Spacer(Modifier.height(6.dp))
        StatRowCard("Текущая серия побед", "${agg.currentStreak}")
        Spacer(Modifier.height(6.dp))
        StatRowCard("Максимальная серия побед", "${agg.maxStreak}")

        if (agg.topMatches.isNotEmpty()) {
            Spacer(Modifier.height(12.dp))
            SectionTitle("ЛУЧШИЕ 5 МАТЧЕЙ")
            Spacer(Modifier.height(8.dp))
            agg.topMatches.forEach { m ->
                StatRowCard(
                    label = "${formatDate(m.dateMillis)} · против ${m.opponent}",
                    value = "%.2f PPR".format(Locale.US, m.ppr),
                    sub = "дротиков на лег: %.1f".format(Locale.US, m.dartsPerLeg)
                )
                Spacer(Modifier.height(6.dp))
            }
        }

        if (agg.hasTrend) {
            Spacer(Modifier.height(20.dp))
            SectionTitle("ТРЕНД")
            Spacer(Modifier.height(8.dp))
            val sign = if (agg.trend >= 0) "+" else ""
            val label = when {
                agg.trend >= 1.0 -> "растёшь"
                agg.trend <= -1.0 -> "падаешь"
                else -> "стабильно"
            }
            StatRowCard(
                label = "PPR последние vs предыдущие",
                value = "$sign%.2f".format(Locale.US, agg.trend),
                sub = label
            )
        }

        Spacer(Modifier.height(20.dp))
        SectionTitle("ЧЕКАУТЫ")
        Spacer(Modifier.height(8.dp))

        val validTotal = VALID_CHECKOUTS.size
        val closedCount = VALID_CHECKOUTS.count { (agg.closedCheckouts[it] ?: 0) > 0 }

        StatRowCard(
            label = "Закрыто чек-аутов",
            value = "$closedCount из $validTotal",
            sub = "нажми, чтобы открыть список всех чек-аутов"
        )
        Spacer(Modifier.height(6.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(Accent)
                .clickable { showCheckoutsDialog = true }
                .padding(vertical = 14.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                "📋 Открыть таблицу чек-аутов",
                color = Color(0xFF121212),
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(Modifier.height(24.dp))
        ResetButton(onResetClick)
        Spacer(Modifier.height(24.dp))
    }

    if (showCheckoutsDialog) {
        CheckoutsDialog(
            closedCheckouts = agg.closedCheckouts,
            onDismiss = { showCheckoutsDialog = false }
        )
    }
}

// ─────────────────────────────────────────────
// Диалог со списком чек-аутов
// ─────────────────────────────────────────────
@Composable
private fun CheckoutsDialog(
    closedCheckouts: Map<Int, Int>,
    onDismiss: () -> Unit
) {
    val validTotal = VALID_CHECKOUTS.size
    val closedCount = VALID_CHECKOUTS.count { (closedCheckouts[it] ?: 0) > 0 }

    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(onClick = onDismiss) { Text("OK", color = Accent) }
        },
        title = {
            Column {
                Text("Закрытые чек-ауты", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(4.dp))
                Text(
                    "Закрыто $closedCount из $validTotal (${(closedCount * 100 / validTotal)}%)",
                    color = GoldAccent, fontSize = 13.sp, fontWeight = FontWeight.Medium
                )
            }
        },
        text = {
            Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                VALID_CHECKOUTS.chunked(5).forEach { row ->
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        row.forEach { value ->
                            val count = closedCheckouts[value] ?: 0
                            CheckoutCell(value = value, count = count, modifier = Modifier.weight(1f))
                        }
                        if (row.size < 5) {
                            repeat(5 - row.size) { Spacer(Modifier.weight(1f)) }
                        }
                    }
                }

                Spacer(Modifier.height(12.dp))
                Text("Легенда:", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(4.dp))
                LegendRow(CheckoutGray, "не закрыт")
                LegendRow(CheckoutGreen, "1 раз")
                LegendRow(CheckoutYellow, "3 раза")
                LegendRow(CheckoutRed, "5 раз")
                LegendRow(CheckoutPurple, "10 раз")
                LegendRow(CheckoutIndigo, "20 раз")
                LegendRow(CheckoutTeal, "30 раз")
                LegendRow(CheckoutGold, "50 раз")
                LegendRow(CheckoutDiamond, "100 раз")
            }
        }
    )
}

@Composable
private fun CheckoutCell(value: Int, count: Int, modifier: Modifier) {
    val (bg, fg) = checkoutColor(count)
    Box(
        modifier = modifier
            .height(42.dp)
            .clip(RoundedCornerShape(6.dp))
            .background(bg),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                "$value",
                color = fg,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold
            )
            if (count > 0) {
                Text(
                    "$count",
                    color = fg.copy(alpha = 0.75f),
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

@Composable
private fun LegendRow(color: Color, label: String) {
    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(vertical = 1.dp)) {
        Box(
            modifier = Modifier
                .size(12.dp)
                .clip(RoundedCornerShape(3.dp))
                .background(color)
        )
        Spacer(Modifier.width(6.dp))
        Text(label, color = Color.White.copy(alpha = 0.75f), fontSize = 10.sp)
    }
}

@Composable
private fun ResetButton(onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(TileBgDark)
            .clickable { onClick() }
            .padding(vertical = 14.dp),
        contentAlignment = Alignment.Center
    ) {
        Text("Сбросить статистику", color = ErrorColor, fontSize = 14.sp, fontWeight = FontWeight.Medium)
    }
}

private fun pluralMatches(n: Int): String {
    val mod10 = n % 10
    val mod100 = n % 100
    return when {
        mod100 in 11..19 -> "матчей"
        mod10 == 1 -> "матч"
        mod10 in 2..4 -> "матча"
        else -> "матчей"
    }
}

private fun formatDate(millis: Long): String {
    val fmt = SimpleDateFormat("dd.MM", Locale.US)
    return fmt.format(Date(millis))
}

// ─────────────────────────────────────────────
// Фильтр по периоду
// ─────────────────────────────────────────────
@Composable
private fun PeriodSelector(period: StatPeriod, onPeriodChange: (StatPeriod) -> Unit) {
    Column {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            listOf(StatPeriod.WEEK, StatPeriod.MONTH, StatPeriod.THREE_MONTHS).forEach { p ->
                PeriodChip(p.label, p == period, { onPeriodChange(p) }, Modifier.weight(1f))
            }
        }
        Spacer(Modifier.height(6.dp))
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            listOf(StatPeriod.HALF_YEAR, StatPeriod.YEAR, StatPeriod.ALL).forEach { p ->
                PeriodChip(p.label, p == period, { onPeriodChange(p) }, Modifier.weight(1f))
            }
        }
    }
}

@Composable
private fun PeriodChip(label: String, selected: Boolean, onClick: () -> Unit, modifier: Modifier) {
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
private fun TabButton(label: String, selected: Boolean, onClick: () -> Unit, modifier: Modifier) {
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
        Text(value, color = GoldAccent, fontSize = 18.sp, fontWeight = FontWeight.Bold)
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

// ─────────────────────────────────────────────
// Хелперы
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
    return String.format(Locale.US, "%.1f%%", p)
}

// Возвращает true, если игрок с этим именем — это владелец.
// Если ownerName пустой — считаем любого человека владельцем (старое поведение).
private fun isOwner(isBotFlag: String, name: String, ownerName: String): Boolean {
    if (isBotFlag != "0") return false
    if (ownerName.isBlank()) return true
    return name.equals(ownerName, ignoreCase = true)
}

// ─────────────────────────────────────────────
// Логика — крикет
// ─────────────────────────────────────────────
private fun computeAggregate(games: List<CricketGameEntity>, ownerName: String): CricketAggregate {
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
        val names = parseStringList(g.playerNames)
        val dartsList = parseIntList(g.totalDarts)
        val missesList = parseIntList(g.misses)
        val triplesList = parseIntList(g.triples)
        val bullAttList = parseIntList(g.bullAttempts)
        val bullHitList = parseIntList(g.bullHits)
        val perfList = parseIntList(g.perfectRounds)
        val strongList = parseIntList(g.strongRounds)
        val mprList = parseDoubleList(g.mpr)

        var hasOwner = false
        for (i in bots.indices) {
            val name = names.getOrNull(i) ?: ""
            if (isOwner(bots[i], name, ownerName)) {
                hasOwner = true
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
        if (hasOwner) legs += g.legsPlayed
    }

    return CricketAggregate(
        legs = legs,
        darts = darts,
        misses = misses,
        triples = triples,
        bullAttempts = bullAttempts,
        bullHits = bullHits,
        perfectRounds = perfect,
        strongRounds = strong,
        avgMpr = if (mprValues.isEmpty()) 0.0 else mprValues.average(),
        bestMpr = mprValues.maxOrNull() ?: 0.0
    )
}

// ─────────────────────────────────────────────
// Логика — x01
// ─────────────────────────────────────────────
private fun computeAggregate501(games: List<Game501Entity>, ownerName: String): Game501Aggregate {
    var legs = 0
    var darts = 0
    var doublesHit = 0
    var doublesAttempted = 0
    var wins = 0
    var matches = 0

    var sumCount180 = 0
    var sumCount170plus = 0
    var sumCount130plus = 0
    var sumCount90plus = 0
    var sumCount57plus = 0
    var sumCount57minus = 0

    var sumFirst9Score = 0
    var sumFirst9Darts = 0
    var sumNonCloseScore = 0
    var sumNonCloseDarts = 0

    val pprValues = mutableListOf<Double>()
    val bestLegPprValues = mutableListOf<Double>()
    val allMatchesInOrder = mutableListOf<Boolean>()

    val botMatches = mutableMapOf<String, MutableList<Game501Entity>>()
    val matchCandidates = mutableListOf<MatchInfo>()

    val closedCheckouts = mutableMapOf<Int, Int>()

    for (g in games) {
        val bots = parseStringList(g.playerIsBot)
        val names = parseStringList(g.playerNames)
        val dartsList = parseIntList(g.matchDarts)
        val hitList = parseIntList(g.doublesHit)
        val attList = parseIntList(g.doublesAttempted)
        val pprList = parseDoubleList(g.ppr)
        val bestLegPprList = parseDoubleList(g.bestLegPpr)

        val c180 = parseIntList(g.matchCount180)
        val c170 = parseIntList(g.matchCount170plus)
        val c130 = parseIntList(g.matchCount130plus)
        val c90 = parseIntList(g.matchCount90plus)
        val c57p = parseIntList(g.matchCount57plus)
        val c57m = parseIntList(g.matchCount57minus)

        val f9s = parseIntList(g.first9Score)
        val f9d = parseIntList(g.first9Darts)
        val ncs = parseIntList(g.nonCloseScore)
        val ncd = parseIntList(g.nonCloseDarts)

        val closeValuesByPlayer = g.closeValues.split("|")

        var hasOwner = false
        var ownerWon = false
        var ownerIndex = -1

        for (i in bots.indices) {
            val name = names.getOrNull(i) ?: ""
            if (isOwner(bots[i], name, ownerName)) {
                hasOwner = true
                ownerIndex = i
                darts += dartsList.getOrElse(i) { 0 }
                doublesHit += hitList.getOrElse(i) { 0 }
                doublesAttempted += attList.getOrElse(i) { 0 }
                if (i < pprList.size) pprValues.add(pprList[i])
                if (i < bestLegPprList.size && bestLegPprList[i] > 0) {
                    bestLegPprValues.add(bestLegPprList[i])
                }
                if (g.winnerIndex == i) {
                    wins++
                    ownerWon = true
                }

                sumCount180 += c180.getOrElse(i) { 0 }
                sumCount170plus += c170.getOrElse(i) { 0 }
                sumCount130plus += c130.getOrElse(i) { 0 }
                sumCount90plus += c90.getOrElse(i) { 0 }
                sumCount57plus += c57p.getOrElse(i) { 0 }
                sumCount57minus += c57m.getOrElse(i) { 0 }

                sumFirst9Score += f9s.getOrElse(i) { 0 }
                sumFirst9Darts += f9d.getOrElse(i) { 0 }
                sumNonCloseScore += ncs.getOrElse(i) { 0 }
                sumNonCloseDarts += ncd.getOrElse(i) { 0 }

                val myCloseValues = closeValuesByPlayer.getOrNull(i) ?: ""
                if (myCloseValues.isNotBlank()) {
                    myCloseValues.split(",").forEach { raw ->
                        val v = raw.trim().toIntOrNull()
                        if (v != null && v in 2..170) {
                            closedCheckouts[v] = (closedCheckouts[v] ?: 0) + 1
                        }
                    }
                }
            }
        }

        if (hasOwner) {
            matches++
            legs += g.legsPlayed

            val botNames = bots.indices.filter { bots[it] == "1" }
                .mapNotNull { idx -> names.getOrNull(idx) }
            val opponentLabel = if (botNames.isEmpty()) "человек" else botNames.joinToString(" + ")

            if (botNames.isNotEmpty()) {
                botMatches.getOrPut(opponentLabel) { mutableListOf() }.add(g)
            }

            val humanPpr = pprList.getOrElse(ownerIndex) { 0.0 }
            val humanDarts = dartsList.getOrElse(ownerIndex) { 0 }
            val dpl = if (g.legsPlayed > 0) humanDarts.toDouble() / g.legsPlayed else 0.0
            matchCandidates.add(
                MatchInfo(
                    dateMillis = g.dateMillis,
                    ppr = humanPpr,
                    opponent = opponentLabel,
                    dartsPerLeg = dpl
                )
            )

            allMatchesInOrder.add(ownerWon)
        }
    }

    val avgPpr = if (pprValues.isEmpty()) 0.0 else pprValues.average()
    val bestMatchPpr = pprValues.maxOrNull() ?: 0.0
    val bestLegPpr = bestLegPprValues.maxOrNull() ?: 0.0
    val avgDartsPerLeg = if (legs > 0) darts.toDouble() / legs else 0.0

    val safeLegs = if (legs > 0) legs.toDouble() else 1.0
    val avg180 = sumCount180 / safeLegs
    val avg170 = sumCount170plus / safeLegs
    val avg130 = sumCount130plus / safeLegs
    val avg90 = sumCount90plus / safeLegs
    val avg57p = sumCount57plus / safeLegs
    val avg57m = sumCount57minus / safeLegs

    val first9 = if (sumFirst9Darts > 0) sumFirst9Score.toDouble() / (sumFirst9Darts / 3.0) else 0.0
    val nonClose = if (sumNonCloseDarts > 0) sumNonCloseScore.toDouble() / (sumNonCloseDarts / 3.0) else 0.0

    var currentStreak = 0
    for (won in allMatchesInOrder) {
        if (won) currentStreak++ else break
    }

    var maxStreak = 0
    var run = 0
    for (i in allMatchesInOrder.indices.reversed()) {
        if (allMatchesInOrder[i]) {
            run++
            if (run > maxStreak) maxStreak = run
        } else {
            run = 0
        }
    }

    var trend = 0.0
    var hasTrend = false
    val matchPprs = games.mapNotNull { g ->
        val bots = parseStringList(g.playerIsBot)
        val names = parseStringList(g.playerNames)
        val pprList = parseDoubleList(g.ppr)
        val idx = bots.indices.firstOrNull { i ->
            isOwner(bots[i], names.getOrNull(i) ?: "", ownerName)
        } ?: -1
        if (idx >= 0) pprList.getOrNull(idx) else null
    }
    if (matchPprs.size >= 4) {
        val half = matchPprs.size / 2
        val fresh = matchPprs.take(half)
        val old = matchPprs.drop(half).take(half)
        if (fresh.isNotEmpty() && old.isNotEmpty()) {
            trend = fresh.average() - old.average()
            hasTrend = true
        }
    }

    val byBotList = botMatches.map { (name, list) ->
        val pprs = mutableListOf<Double>()
        var hitSum = 0
        var attSum = 0
        for (g in list) {
            val bots = parseStringList(g.playerIsBot)
            val names = parseStringList(g.playerNames)
            val pprList = parseDoubleList(g.ppr)
            val hitList = parseIntList(g.doublesHit)
            val attList = parseIntList(g.doublesAttempted)
            val idx = bots.indices.firstOrNull { i ->
                isOwner(bots[i], names.getOrNull(i) ?: "", ownerName)
            } ?: -1
            if (idx >= 0) {
                pprList.getOrNull(idx)?.let { pprs.add(it) }
                hitSum += hitList.getOrElse(idx) { 0 }
                attSum += attList.getOrElse(idx) { 0 }
            }
        }
        BotStat(
            botName = name,
            matches = list.size,
            ppr = if (pprs.isEmpty()) 0.0 else pprs.average(),
            dblPct = if (attSum > 0) hitSum.toDouble() / attSum * 100.0 else 0.0
        )
    }.sortedBy { it.ppr }

    val top5 = matchCandidates.sortedByDescending { it.ppr }.take(5)

    return Game501Aggregate(
        legs = legs,
        darts = darts,
        doublesHit = doublesHit,
        doublesAttempted = doublesAttempted,
        wins = wins,
        totalMatches = matches,
        avgPpr = avgPpr,
        bestMatchPpr = bestMatchPpr,
        bestLegPpr = bestLegPpr,
        avgDartsPerLeg = avgDartsPerLeg,
        avgCount180 = avg180,
        avgCount170plus = avg170,
        avgCount130plus = avg130,
        avgCount90plus = avg90,
        avgCount57plus = avg57p,
        avgCount57minus = avg57m,
        first9 = first9,
        nonClose = nonClose,
        currentStreak = currentStreak,
        maxStreak = maxStreak,
        trend = trend,
        hasTrend = hasTrend,
        byBot = byBotList,
        topMatches = top5,
        closedCheckouts = closedCheckouts
    )
}
