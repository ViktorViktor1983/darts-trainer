package com.lodkin.dartstrainer.ui

import androidx.compose.foundation.Canvas
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.lodkin.dartstrainer.data.SettingsStorage
import com.lodkin.dartstrainer.data.cricket.CricketEntityConverter
import com.lodkin.dartstrainer.data.cricket.CricketGame
import com.lodkin.dartstrainer.data.cricket.CricketGameEntity
import com.lodkin.dartstrainer.data.cricket.CricketRepository
import com.lodkin.dartstrainer.data.game501.CheckoutTable
import com.lodkin.dartstrainer.data.game501.Game501
import com.lodkin.dartstrainer.data.game501.Game501Database
import com.lodkin.dartstrainer.data.game501.Game501Entity
import com.lodkin.dartstrainer.data.game501.Game501EntityConverter
import com.lodkin.dartstrainer.data.game501.Game501Repository
import com.lodkin.dartstrainer.theme.Accent
import com.lodkin.dartstrainer.theme.ErrorColor
import com.lodkin.dartstrainer.theme.GoldAccent
import com.lodkin.dartstrainer.theme.TileBg
import com.lodkin.dartstrainer.theme.TileBgDark
import com.lodkin.dartstrainer.ui.cricket.CricketStatsScreen
import com.lodkin.dartstrainer.ui.cricket.MatchesListDialogCricket
import com.lodkin.dartstrainer.ui.game501.Game501StatsScreen
import com.lodkin.dartstrainer.ui.game501.MatchesListDialog501
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

// ─────────────────────────────────────────────
// Валидные чекауты (только для диалога чекаутов)
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
// Экран статистики
// ─────────────────────────────────────────────
@Composable
fun StatsScreen(
    repository: CricketRepository,
    initialTab: Int = 0,
    showTabs: Boolean = true,
    onResumeGame501: (Game501Entity) -> Unit = {},
    onResumeCricket: (CricketGameEntity) -> Unit = {},
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val game501Repo = remember {
        Game501Repository(Game501Database.get(context).game501Dao())
    }
    val ownerName = remember { SettingsStorage.getPlayerName(context).trim() }

    var selectedTab by remember { mutableStateOf(initialTab) }
    var period by remember { mutableStateOf(StatPeriod.ALL) }
    var games by remember { mutableStateOf<List<CricketGameEntity>>(emptyList()) }
    var allCricketGames by remember { mutableStateOf<List<CricketGameEntity>>(emptyList()) }
    var games501 by remember { mutableStateOf<List<Game501Entity>>(emptyList()) }
    var showResetDialog by remember { mutableStateOf(false) }
    var reloadKey by remember { mutableStateOf(0) }

    var selectedVariant by remember { mutableStateOf(GameVariant.ALL) }
    var showVariantDialog by remember { mutableStateOf(false) }

    var showCricketChart by remember { mutableStateOf(false) }
    var show501Chart by remember { mutableStateOf(false) }
    var showCheckoutsDialog by remember { mutableStateOf(false) }

    var showMatchesList501 by remember { mutableStateOf(false) }
    var showMatchesListCricket by remember { mutableStateOf(false) }

    var viewingGame501 by remember { mutableStateOf<Game501?>(null) }
    var viewingCricket by remember { mutableStateOf<CricketGame?>(null) }

    LaunchedEffect(period, reloadKey) {
        val cutoff = period.daysBack?.let {
            System.currentTimeMillis() - it * 24L * 60L * 60L * 1000L
        }

        val allCricket = repository.getAllGamesIncludingUnfinished()
        allCricketGames = if (cutoff == null) allCricket else allCricket.filter { it.dateMillis >= cutoff }
        games = allCricketGames.filter { it.isFinished }

        val all501 = game501Repo.getAllGames()
        games501 = if (cutoff == null) all501 else all501.filter { it.dateMillis >= cutoff }
    }

    val cricketChartData = remember(games, ownerName) {
        StatsCompute.cricketChartData(games, ownerName)
    }
    val game501ChartData = remember(games501, ownerName, selectedVariant) {
        StatsCompute.game501ChartData(games501, ownerName, selectedVariant)
    }

    val allCheckouts = remember(games501, ownerName) {
        StatsCompute.closedCheckouts(
            games501.filter { it.isFinished && it.outModeName != "STRAIGHT_OUT" },
            ownerName
        )
    }

    // ─────────────────────────────────────────
    // Просмотр подробной статистики старого матча
    // ─────────────────────────────────────────
    val viewing501 = viewingGame501
    if (viewing501 != null) {
        Game501StatsScreen(
            game = viewing501,
            onPlayAgain = { viewingGame501 = null },
            onBackToMenu = { viewingGame501 = null }
        )
        return
    }

    val viewingCrick = viewingCricket
    if (viewingCrick != null) {
        CricketStatsScreen(
            game = viewingCrick,
            onPlayAgain = { viewingCricket = null },
            onBackToMenu = { viewingCricket = null }
        )
        return
    }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {

        // ── ШАПКА ──
        // Слева — только «Назад» (длиннее, стрелка крупнее).
        // По центру — «Статистика» и «игрок: …» ×2.
        // Справа — только активные кнопки:
        //   Крикет: 📈 (график MPR)
        //   501:    📈 (график PPR/D%) + 🏆 (чекауты)
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .background(TileBgDark)
                    .clickable { onBack() }
                    .padding(horizontal = 24.dp, vertical = 14.dp)
            ) {
                Text("←", color = Accent, fontSize = 32.sp, fontWeight = FontWeight.Bold)
            }

            Column(
                modifier = Modifier.weight(1f),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    "Статистика",
                    fontSize = 40.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                if (ownerName.isNotBlank()) {
                    Text("игрок: $ownerName", color = Accent, fontSize = 22.sp)
                }
            }

            if (selectedTab == 0) {
                // Крикет: одна кнопка 📈
                HeaderIconButton(
                    icon = "📈",
                    active = true,
                    onClick = { showCricketChart = true }
                )
            } else {
                // 501: 📈 + 🏆
                HeaderIconButton(
                    icon = "📈",
                    active = true,
                    onClick = { show501Chart = true }
                )
                Spacer(Modifier.width(6.dp))
                HeaderIconButton(
                    icon = "🏆",
                    active = true,
                    onClick = { showCheckoutsDialog = true }
                )
            }
        }

        Spacer(Modifier.height(16.dp))

        // ── ТАБЫ ──
        if (showTabs) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                TabButton("КРИКЕТ", selectedTab == 0, { selectedTab = 0 }, Modifier.weight(1f))
                Row(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(12.dp))
                        .background(if (selectedTab == 1) Accent else TileBgDark)
                        .clickable { selectedTab = 1 }
                        .padding(vertical = 12.dp, horizontal = 12.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "501",
                        color = if (selectedTab == 1) Color(0xFF121212) else Color.White,
                        fontSize = 14.sp,
                        fontWeight = if (selectedTab == 1) FontWeight.Bold else FontWeight.Medium,
                        letterSpacing = 2.sp
                    )
                    if (selectedTab == 1) {
                        Spacer(Modifier.width(6.dp))
                        Box(
                            modifier = Modifier
                                .size(24.dp)
                                .clip(RoundedCornerShape(6.dp))
                                .background(Color(0xFF121212).copy(alpha = 0.15f))
                                .clickable { showVariantDialog = true },
                            contentAlignment = Alignment.Center
                        ) {
                            Text("▼", color = Color(0xFF121212), fontSize = 12.sp)
                        }
                    }
                }
            }
        } else {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Text(
                    if (selectedTab == 0) "КРИКЕТ" else "501",
                    color = Accent,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 3.sp
                )
            }
        }

        // ── Строка активного фильтра ──
        if (selectedTab == 1 && selectedVariant != GameVariant.ALL) {
            Spacer(Modifier.height(8.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(TileBgDark)
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "Показано: ${selectedVariant.label}",
                    color = GoldAccent,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.weight(1f)
                )
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(TileBg)
                        .clickable { selectedVariant = GameVariant.ALL }
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text("✕ Сбросить", color = Color.White, fontSize = 11.sp)
                }
            }
        }

        Spacer(Modifier.height(16.dp))

        if (selectedTab == 0) {
            val unfinishedCount = remember(allCricketGames) {
                allCricketGames.count { !it.isFinished }
            }
            MatchesButton(
                unfinishedCount = unfinishedCount,
                onClick = { showMatchesListCricket = true }
            )
            Spacer(Modifier.height(12.dp))

            CricketTabContent(
                games = games,
                ownerName = ownerName,
                period = period,
                onPeriodChange = { period = it },
                onResetClick = { showResetDialog = true }
            )
        } else {
            val unfinishedCount = remember(games501) { games501.count { !it.isFinished } }
            MatchesButton(
                unfinishedCount = unfinishedCount,
                onClick = { showMatchesList501 = true }
            )
            Spacer(Modifier.height(12.dp))

            Game501TabContent(
                games = games501,
                ownerName = ownerName,
                variant = selectedVariant,
                period = period,
                onPeriodChange = { period = it },
                onResetClick = { showResetDialog = true }
            )
        }
    }

    if (showVariantDialog) {
        VariantDialog(
            selected = selectedVariant,
            onSelect = { selectedVariant = it; showVariantDialog = false },
            onDismiss = { showVariantDialog = false }
        )
    }

    if (showCricketChart) {
        ChartDialog(
            title = "График MPR (крикет)",
            onDismiss = { showCricketChart = false },
            charts = listOf(
                ChartSpec(
                    title = "Средний набор (MPR)",
                    points = cricketChartData,
                    yMin = 0.0,
                    yMax = 6.0,
                    lineColor = Accent
                )
            )
        )
    }

    if (show501Chart) {
        ChartDialog(
            title = "Графики x01",
            onDismiss = { show501Chart = false },
            charts = game501ChartData
        )
    }

    if (showCheckoutsDialog) {
        CheckoutsDialog(
            closedCheckouts = allCheckouts,
            onDismiss = { showCheckoutsDialog = false }
        )
    }

    if (showMatchesList501) {
        MatchesListDialog501(
            games = games501,
            onResume = { entity ->
                showMatchesList501 = false
                onResumeGame501(entity)
            },
            onViewMatch = { entity ->
                showMatchesList501 = false
                val restored = Game501EntityConverter.toGame501(entity)
                if (restored != null) viewingGame501 = restored
            },
            onDelete = { entity ->
                scope.launch {
                    game501Repo.deleteGame(entity.id)
                    reloadKey++
                }
            },
            onDismiss = { showMatchesList501 = false }
        )
    }

    if (showMatchesListCricket) {
        MatchesListDialogCricket(
            games = allCricketGames,
            onResume = { entity ->
                showMatchesListCricket = false
                onResumeCricket(entity)
            },
            onViewMatch = { entity ->
                showMatchesListCricket = false
                val restored = CricketEntityConverter.toCricketGame(entity)
                if (restored != null) viewingCricket = restored
            },
            onDelete = { entity ->
                scope.launch {
                    repository.deleteGame(entity.id)
                    reloadKey++
                }
            },
            onDismiss = { showMatchesListCricket = false }
        )
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
// Кнопка «Список матчей» (компактная, сверху)
// ─────────────────────────────────────────────
@Composable
private fun MatchesButton(
    unfinishedCount: Int,
    onClick: () -> Unit
) {
    val bg = if (unfinishedCount > 0) ErrorColor else TileBgDark
    val fg = Color.White

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(bg)
            .clickable { onClick() }
            .padding(horizontal = 14.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            "📋  Список матчей",
            color = fg,
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.weight(1f)
        )
        if (unfinishedCount > 0) {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(10.dp))
                    .background(Color.White.copy(alpha = 0.25f))
                    .padding(horizontal = 8.dp, vertical = 2.dp)
            ) {
                Text(
                    "⏸ $unfinishedCount",
                    color = fg,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        } else {
            Text("▶", color = fg, fontSize = 14.sp)
        }
    }
}

// ─────────────────────────────────────────────
// Кнопка-иконка в шапке
// ─────────────────────────────────────────────
@Composable
private fun HeaderIconButton(
    icon: String,
    active: Boolean,
    onClick: () -> Unit
) {
    val alpha = if (active) 1.0f else 0.4f
    Box(
        modifier = Modifier
            .size(48.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(TileBgDark)
            .clickable(enabled = active) { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Text(icon, fontSize = 22.sp, color = Color.White.copy(alpha = alpha))
    }
}

// ─────────────────────────────────────────────
// График
// ─────────────────────────────────────────────
@Composable
private fun ChartDialog(
    title: String,
    charts: List<ChartSpec>,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(onClick = onDismiss) { Text("OK", color = Accent) }
        },
        title = {
            Text(title, color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
        },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState())
            ) {
                charts.forEachIndexed { idx, chart ->
                    if (idx > 0) Spacer(Modifier.height(20.dp))
                    ChartBlock(chart)
                }
            }
        }
    )
}

@Composable
private fun ChartBlock(spec: ChartSpec) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            spec.title,
            color = Accent,
            fontSize = 13.sp,
            fontWeight = FontWeight.Medium,
            letterSpacing = 2.sp
        )
        Spacer(Modifier.height(8.dp))

        if (spec.points.size < 2) {
            Text(
                "Недостаточно матчей для графика",
                color = Color.White.copy(alpha = 0.5f),
                fontSize = 11.sp
            )
        } else {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(TileBgDark)
                    .padding(8.dp)
            ) {
                LineChart(
                    points = spec.points,
                    yMin = spec.yMin,
                    yMax = spec.yMax,
                    lineColor = spec.lineColor
                )
            }
            Spacer(Modifier.height(4.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    "мин: %.2f".format(Locale.US, spec.points.minOrNull() ?: 0.0),
                    color = Color.White.copy(alpha = 0.5f), fontSize = 10.sp
                )
                Text(
                    "сред: %.2f".format(Locale.US, spec.points.average()),
                    color = GoldAccent, fontSize = 10.sp
                )
                Text(
                    "макс: %.2f".format(Locale.US, spec.points.maxOrNull() ?: 0.0),
                    color = Color.White.copy(alpha = 0.5f), fontSize = 10.sp
                )
            }
            Spacer(Modifier.height(2.dp))
            Text(
                "матчей: ${spec.points.size}",
                color = Color.White.copy(alpha = 0.4f),
                fontSize = 10.sp
            )
        }
    }
}

@Composable
private fun LineChart(
    points: List<Double>,
    yMin: Double,
    yMax: Double,
    lineColor: Color
) {
    val gridColor = Color.White.copy(alpha = 0.12f)

    Canvas(modifier = Modifier.fillMaxSize()) {
        val w = size.width
        val h = size.height
        val leftPad = 36.dp.toPx()
        val rightPad = 8.dp.toPx()
        val topPad = 10.dp.toPx()
        val bottomPad = 18.dp.toPx()

        val plotW = w - leftPad - rightPad
        val plotH = h - topPad - bottomPad
        if (plotW <= 0f || plotH <= 0f) return@Canvas

        val range = (yMax - yMin).coerceAtLeast(0.0001)

        val gridSteps = 5
        for (i in 0..gridSteps) {
            val y = topPad + plotH * i / gridSteps
            drawLine(
                color = gridColor,
                start = Offset(leftPad, y),
                end = Offset(leftPad + plotW, y),
                strokeWidth = 1f
            )
        }

        val n = points.size
        val xStep = if (n > 1) plotW / (n - 1).toFloat() else 0f
        val coords = points.mapIndexed { i, v ->
            val x = leftPad + xStep * i
            val rel = ((v - yMin) / range).coerceIn(0.0, 1.0).toFloat()
            val y = topPad + plotH * (1f - rel)
            Offset(x, y)
        }

        if (coords.size >= 2) {
            val path = Path().apply {
                moveTo(coords[0].x, coords[0].y)
                for (i in 1 until coords.size) lineTo(coords[i].x, coords[i].y)
            }
            drawPath(path, color = lineColor, style = Stroke(width = 2.dp.toPx()))
        }

        val dotRadius = 3.dp.toPx()
        coords.forEach { c ->
            drawCircle(color = lineColor, radius = dotRadius, center = c)
            drawCircle(color = Color.White, radius = dotRadius / 2f, center = c)
        }
    }
}

// ─────────────────────────────────────────────
// Диалог выбора модификации игры
// ─────────────────────────────────────────────
@Composable
private fun VariantDialog(
    selected: GameVariant,
    onSelect: (GameVariant) -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(onClick = onDismiss) { Text("OK", color = Accent) }
        },
        title = {
            Text("Какая игра?", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold)
        },
        text = {
            Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                VariantRow(GameVariant.ALL, selected, onSelect)
                Spacer(Modifier.height(8.dp))
                VariantGroupTitle("501")
                VariantRow(GameVariant.X501_DO, selected, onSelect)
                VariantRow(GameVariant.X501_DIDO, selected, onSelect)
                Spacer(Modifier.height(8.dp))
                VariantGroupTitle("301")
                VariantRow(GameVariant.X301_DO, selected, onSelect)
                VariantRow(GameVariant.X301_DIDO, selected, onSelect)
                Spacer(Modifier.height(8.dp))
                VariantGroupTitle("701")
                VariantRow(GameVariant.X701_DO, selected, onSelect)
                VariantRow(GameVariant.X701_DIDO, selected, onSelect)
                Spacer(Modifier.height(8.dp))
                VariantGroupTitle("1001")
                VariantRow(GameVariant.X1001_DO, selected, onSelect)
                VariantRow(GameVariant.X1001_DIDO, selected, onSelect)
                Spacer(Modifier.height(12.dp))
                Text(
                    "Игры в режиме «Без даблов» в статистику не попадают.",
                    color = Color.White.copy(alpha = 0.5f), fontSize = 10.sp
                )
            }
        }
    )
}

@Composable
private fun VariantGroupTitle(text: String) {
    Text(
        text,
        color = Accent,
        fontSize = 11.sp,
        fontWeight = FontWeight.Bold,
        letterSpacing = 2.sp,
        modifier = Modifier.padding(vertical = 4.dp)
    )
}

@Composable
private fun VariantRow(
    variant: GameVariant,
    selected: GameVariant,
    onSelect: (GameVariant) -> Unit
) {
    val isSelected = variant == selected
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(if (isSelected) Accent else TileBgDark)
            .clickable { onSelect(variant) }
            .padding(horizontal = 12.dp, vertical = 10.dp)
    ) {
        Text(
            variant.label,
            color = if (isSelected) Color(0xFF121212) else Color.White,
            fontSize = 14.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
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
    val aggregate = remember(games, ownerName) {
        StatsCompute.cricketAggregate(games, ownerName)
    }

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
            StatRowCard("Промахи", StatsParse.percentLabel(aggregate.misses, aggregate.darts),
                "${aggregate.misses} из ${aggregate.darts} дротиков")
            Spacer(Modifier.height(6.dp))
            StatRowCard("Утроения", StatsParse.percentLabel(aggregate.triples, aggregate.darts),
                "${aggregate.triples} утроений")
            Spacer(Modifier.height(6.dp))
            StatRowCard(
                "Точность Bull",
                if (aggregate.bullAttempts > 0) StatsParse.percentLabel(aggregate.bullHits, aggregate.bullAttempts) else "—",
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
    variant: GameVariant,
    period: StatPeriod,
    onPeriodChange: (StatPeriod) -> Unit,
    onResetClick: () -> Unit
) {
    val filteredGames = remember(games, variant) {
        games.filter { g ->
            if (!g.isFinished) return@filter false
            if (g.outModeName == "STRAIGHT_OUT") return@filter false
            if (variant == GameVariant.ALL) return@filter true
            g.gameTypeName == variant.gameTypeName && g.outModeName == variant.outModeName
        }
    }

    val agg = remember(filteredGames, ownerName) {
        StatsCompute.game501Aggregate(filteredGames, ownerName)
    }

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
            if (agg.doublesAttempted > 0) StatsParse.percentLabel(agg.doublesHit, agg.doublesAttempted) else "—",
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
                    sub = "${b.matches} ${StatsParse.pluralMatches(b.matches)} · удв. %.1f%%".format(Locale.US, b.dblPct)
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

        Spacer(Modifier.height(24.dp))
        ResetButton(onResetClick)
        Spacer(Modifier.height(24.dp))
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
                Text("Мои закрытые чекауты", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold)
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
            Text("$value", color = fg, fontSize = 13.sp, fontWeight = FontWeight.Bold)
            if (count > 0) {
                Text("$count", color = fg.copy(alpha = 0.75f), fontSize = 9.sp, fontWeight = FontWeight.Medium)
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
