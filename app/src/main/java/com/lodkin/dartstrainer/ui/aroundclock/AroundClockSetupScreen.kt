package com.lodkin.dartstrainer.ui.aroundclock

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.lodkin.dartstrainer.data.aroundclock.AroundClockGameEntity
import com.lodkin.dartstrainer.data.aroundclock.AroundClockOrder
import com.lodkin.dartstrainer.data.aroundclock.AroundClockRepository
import com.lodkin.dartstrainer.data.aroundclock.AroundClockTarget
import com.lodkin.dartstrainer.theme.Accent
import com.lodkin.dartstrainer.theme.DarkBg
import com.lodkin.dartstrainer.theme.GoldAccent
import com.lodkin.dartstrainer.theme.TileBg
import com.lodkin.dartstrainer.theme.TileBgDark
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.Locale

@Composable
fun AroundClockSetupScreen(
    playerName: String,
    repository: AroundClockRepository,
    onStartGame: (AroundClockTarget, AroundClockOrder, Int) -> Unit,
    onOpenStats: () -> Unit,
    onBack: () -> Unit
) {
    var target by remember { mutableStateOf(AroundClockTarget.SINGLE) }
    var order by remember { mutableStateOf(AroundClockOrder.ORDERED) }
    var hitsRequired by remember { mutableStateOf(1) }
    var showChartDialog by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBg)
            .padding(16.dp)
    ) {
        // ── Шапка ──
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(10.dp))
                    .background(TileBgDark)
                    .clickable { onBack() }
                    .padding(horizontal = 12.dp, vertical = 8.dp)
            ) {
                Text("← Назад", color = Accent, fontSize = 14.sp, fontWeight = FontWeight.Bold)
            }
            Spacer(Modifier.width(10.dp))
            Text(
                "Кругосветка",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                modifier = Modifier.weight(1f)
            )
            HeaderIconButton("📊") { onOpenStats() }
            Spacer(Modifier.width(6.dp))
            HeaderIconButton("📈") { showChartDialog = true }
        }

        Spacer(Modifier.height(20.dp))

        // ── Содержимое ──
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .verticalScroll(rememberScrollState())
        ) {
            // Игрок
            Text(
                "ИГРОК",
                color = Accent,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 3.sp
            )
            Spacer(Modifier.height(6.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(TileBg)
                    .padding(horizontal = 16.dp, vertical = 14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    if (playerName.isNotBlank()) playerName else "Игрок",
                    color = Color.White,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Medium
                )
            }

            Spacer(Modifier.height(20.dp))

            // Цель
            Text(
                "ЦЕЛЬ",
                color = Accent,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 3.sp
            )
            Spacer(Modifier.height(6.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                TargetButton(
                    label = "Сектора",
                    selected = target == AroundClockTarget.SINGLE,
                    onClick = { target = AroundClockTarget.SINGLE },
                    modifier = Modifier.weight(1f)
                )
                TargetButton(
                    label = "Удвоения",
                    selected = target == AroundClockTarget.DOUBLE,
                    onClick = { target = AroundClockTarget.DOUBLE },
                    modifier = Modifier.weight(1f)
                )
                TargetButton(
                    label = "Утроения",
                    selected = target == AroundClockTarget.TRIPLE,
                    onClick = { target = AroundClockTarget.TRIPLE },
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(Modifier.height(20.dp))

            // Порядок
            Text(
                "ПОРЯДОК",
                color = Accent,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 3.sp
            )
            Spacer(Modifier.height(6.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OrderButton(
                    label = "По порядку",
                    sub = "1 → 2 → … → Bull",
                    selected = order == AroundClockOrder.ORDERED,
                    onClick = { order = AroundClockOrder.ORDERED },
                    modifier = Modifier.weight(1f)
                )
                OrderButton(
                    label = "Случайно",
                    sub = "перемешанный список",
                    selected = order == AroundClockOrder.RANDOM,
                    onClick = { order = AroundClockOrder.RANDOM },
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(Modifier.height(20.dp))

            // Попаданий на сектор
            Text(
                "ПОПАДАНИЙ НА СЕКТОР",
                color = Accent,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 3.sp
            )
            Spacer(Modifier.height(6.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                (1..3).forEach { n ->
                    HitsButton(
                        label = "$n",
                        sub = if (n == 1) "раз" else "раза",
                        selected = hitsRequired == n,
                        onClick = { hitsRequired = n },
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            Spacer(Modifier.height(24.dp))

            // Описание
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(TileBgDark)
                    .padding(16.dp)
            ) {
                Column {
                    Text(
                        "Как играть",
                        color = GoldAccent,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(Modifier.height(8.dp))
                    Text(
                        "Попадай в целевые сектора по кругу. " +
                        "На каждый сектор нужно попасть $hitsRequired " +
                        (if (hitsRequired == 1) "раз" else "раза") +
                        " — тогда перейдёшь к следующему. " +
                        "Bull в конце: " +
                        when (target) {
                            AroundClockTarget.SINGLE -> "зелёное 25"
                            AroundClockTarget.DOUBLE -> "красное 50"
                            AroundClockTarget.TRIPLE -> "красное 50"
                        } + ". " +
                        "В конце увидишь, сколько дротиков потратил на весь круг.",
                        color = Color.White.copy(alpha = 0.75f),
                        fontSize = 12.sp,
                        lineHeight = 18.sp
                    )
                }
            }

            Spacer(Modifier.height(24.dp))
        }

        // ── Кнопка «Начать игру» ──
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(Accent)
                .clickable { onStartGame(target, order, hitsRequired) }
                .padding(vertical = 18.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                "НАЧАТЬ ИГРУ",
                color = Color(0xFF121212),
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }

    if (showChartDialog) {
        AroundClockChartDialog(
            repository = repository,
            onDismiss = { showChartDialog = false }
        )
    }
}

// ─────────────────────────────────────────────
// Кнопки
// ─────────────────────────────────────────────
@Composable
private fun HeaderIconButton(icon: String, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .size(40.dp)
            .clip(RoundedCornerShape(10.dp))
            .background(TileBgDark)
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Text(icon, fontSize = 18.sp, color = Color.White)
    }
}

@Composable
private fun TargetButton(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier
) {
    Box(
        modifier = modifier
            .height(56.dp)
            .clip(RoundedCornerShape(10.dp))
            .background(if (selected) Accent else TileBg)
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Text(
            label,
            color = if (selected) Color(0xFF121212) else Color.White,
            fontSize = 13.sp,
            fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium
        )
    }
}

@Composable
private fun OrderButton(
    label: String,
    sub: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier
) {
    Box(
        modifier = modifier
            .height(72.dp)
            .clip(RoundedCornerShape(10.dp))
            .background(if (selected) Accent else TileBg)
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                label,
                color = if (selected) Color(0xFF121212) else Color.White,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(Modifier.height(2.dp))
            Text(
                sub,
                color = if (selected) Color(0xFF121212).copy(alpha = 0.7f) else Color.White.copy(alpha = 0.5f),
                fontSize = 10.sp
            )
        }
    }
}

@Composable
private fun HitsButton(
    label: String,
    sub: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier
) {
    Box(
        modifier = modifier
            .height(64.dp)
            .clip(RoundedCornerShape(10.dp))
            .background(if (selected) Accent else TileBg)
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                label,
                color = if (selected) Color(0xFF121212) else Color.White,
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                sub,
                color = if (selected) Color(0xFF121212).copy(alpha = 0.7f) else Color.White.copy(alpha = 0.5f),
                fontSize = 10.sp
            )
        }
    }
}

// ─────────────────────────────────────────────
// Диалог с графиком
// ─────────────────────────────────────────────
@Composable
private fun AroundClockChartDialog(
    repository: AroundClockRepository,
    onDismiss: () -> Unit
) {
    var chartTarget by remember { mutableStateOf(AroundClockTarget.SINGLE) }
    var targetDropdownExpanded by remember { mutableStateOf(false) }
    var games by remember { mutableStateOf<List<AroundClockGameEntity>>(emptyList()) }
    var loading by remember { mutableStateOf(true) }

    LaunchedEffect(chartTarget) {
        loading = true
        games = withContext(Dispatchers.IO) {
            repository.getAllGames().filter { it.targetName == chartTarget.name }
        }.reversed()
        loading = false
    }

    val darts = games.filter { it.completed }.map { it.totalDarts.toDouble() }

    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(onClick = onDismiss) { Text("OK", color = Accent) }
        },
        title = {
            Text("График · Кругосветка", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
        },
        text = {
            Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                // Dropdown по цели
                Box(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(TileBg)
                            .clickable { targetDropdownExpanded = true }
                            .padding(horizontal = 14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            "Цель: ${targetLabel(chartTarget)}",
                            color = Color.White,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Medium,
                            modifier = Modifier.weight(1f)
                        )
                        Text("▼", color = Accent, fontSize = 12.sp)
                    }
                    DropdownMenu(
                        expanded = targetDropdownExpanded,
                        onDismissRequest = { targetDropdownExpanded = false },
                        modifier = Modifier.background(DarkBg)
                    ) {
                        AroundClockTarget.values().forEach { t ->
                            val isSelected = t == chartTarget
                            DropdownMenuItem(
                                text = {
                                    Text(
                                        targetLabel(t),
                                        color = if (isSelected) Accent else Color.White,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                    )
                                },
                                onClick = {
                                    chartTarget = t
                                    targetDropdownExpanded = false
                                }
                            )
                        }
                    }
                }

                Spacer(Modifier.height(16.dp))

                if (loading) {
                    Box(
                        modifier = Modifier.fillMaxWidth().height(180.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("Загрузка…", color = Color.White.copy(alpha = 0.5f), fontSize = 12.sp)
                    }
                } else if (darts.size < 2) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(180.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(TileBgDark),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            "Недостаточно матчей\n(нужно минимум 2)",
                            color = Color.White.copy(alpha = 0.5f),
                            fontSize = 12.sp,
                            textAlign = TextAlign.Center,
                            lineHeight = 18.sp
                        )
                    }
                } else {
                    val maxY = (darts.maxOrNull() ?: 50.0) * 1.1
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(TileBgDark)
                            .padding(8.dp)
                    ) {
                        LineChart(
                            points = darts,
                            yMin = 0.0,
                            yMax = maxY,
                            lineColor = Accent
                        )
                    }
                    Spacer(Modifier.height(6.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            "мин: ${darts.minOrNull()?.toInt() ?: 0}",
                            color = Color.White.copy(alpha = 0.5f), fontSize = 11.sp
                        )
                        Text(
                            "сред: ${"%.0f".format(Locale.US, darts.average())}",
                            color = GoldAccent, fontSize = 11.sp
                        )
                        Text(
                            "макс: ${darts.maxOrNull()?.toInt() ?: 0}",
                            color = Color.White.copy(alpha = 0.5f), fontSize = 11.sp
                        )
                    }
                    Spacer(Modifier.height(4.dp))
                    Text(
                        "матчей: ${darts.size}",
                        color = Color.White.copy(alpha = 0.4f),
                        fontSize = 10.sp
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        "по оси Y — потрачено дротиков на круг (меньше — лучше)",
                        color = Color.White.copy(alpha = 0.5f),
                        fontSize = 10.sp
                    )
                }
            }
        }
    )
}

private fun targetLabel(t: AroundClockTarget): String = when (t) {
    AroundClockTarget.SINGLE -> "Сектора"
    AroundClockTarget.DOUBLE -> "Удвоения"
    AroundClockTarget.TRIPLE -> "Утроения"
}

// ─────────────────────────────────────────────
// LineChart
// ─────────────────────────────────────────────
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
