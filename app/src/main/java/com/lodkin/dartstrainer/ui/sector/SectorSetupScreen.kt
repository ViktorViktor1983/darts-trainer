package com.lodkin.dartstrainer.ui.sector

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
import com.lodkin.dartstrainer.data.sector.SectorGameEntity
import com.lodkin.dartstrainer.data.sector.SectorRepository
import com.lodkin.dartstrainer.theme.Accent
import com.lodkin.dartstrainer.theme.DarkBg
import com.lodkin.dartstrainer.theme.GoldAccent
import com.lodkin.dartstrainer.theme.TileBg
import com.lodkin.dartstrainer.theme.TileBgDark
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.Locale

// Список секторов для dropdown: Bull первый, потом 20 вниз до 1
private val SECTOR_LIST: List<Int> = listOf(25) + (20 downTo 1)

private fun sectorLabel(sector: Int): String =
    if (sector == 25) "BULL" else "$sector"

@Composable
fun SectorSetupScreen(
    sectorRepository: SectorRepository,
    onStartGame: (Int) -> Unit,
    onOpenStats: () -> Unit,
    onBack: () -> Unit
) {
    var selectedSector by remember { mutableStateOf(20) }
    var dropdownExpanded by remember { mutableStateOf(false) }
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
                "Сектор",
                fontSize = 22.sp,
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
            Text(
                "ВЫБЕРИ СЕКТОР",
                color = Accent,
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium,
                letterSpacing = 3.sp,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center
            )
            Spacer(Modifier.height(12.dp))

            // ── Dropdown ──
            Box(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(64.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(TileBg)
                        .clickable { dropdownExpanded = true }
                        .padding(horizontal = 20.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        sectorLabel(selectedSector),
                        color = if (selectedSector == 25) GoldAccent else Color.White,
                        fontSize = 26.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.weight(1f)
                    )
                    Text("▼", color = Accent, fontSize = 14.sp)
                }
                DropdownMenu(
                    expanded = dropdownExpanded,
                    onDismissRequest = { dropdownExpanded = false },
                    modifier = Modifier
                        .background(DarkBg)
                        .heightIn(max = 400.dp)
                ) {
                    SECTOR_LIST.forEach { sec ->
                        val isSelected = sec == selectedSector
                        DropdownMenuItem(
                            text = {
                                Text(
                                    sectorLabel(sec),
                                    color = if (isSelected) Accent else Color.White,
                                    fontSize = 16.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                )
                            },
                            onClick = {
                                selectedSector = sec
                                dropdownExpanded = false
                            }
                        )
                    }
                }
            }

            Spacer(Modifier.height(24.dp))

            // ── Подсказка ──
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(TileBgDark)
                    .padding(16.dp)
            ) {
                Column {
                    Text(
                        "Сектор ${sectorLabel(selectedSector)}",
                        color = GoldAccent,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(Modifier.height(6.dp))
                    Text(
                        "10 подходов по 3 дротика. Вводишь число попаданий " +
                        "за подход (S=1, D=2, T=3). Приложение считает очки. " +
                        "После партии увидишь норматив.",
                        color = Color.White.copy(alpha = 0.75f),
                        fontSize = 13.sp,
                        lineHeight = 18.sp
                    )
                    if (selectedSector == 20) {
                        Spacer(Modifier.height(10.dp))
                        Text(
                            "Для Сектора 20 действуют официальные нормативы разрядов.",
                            color = Accent.copy(alpha = 0.8f),
                            fontSize = 11.sp,
                            lineHeight = 16.sp
                        )
                    }
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
                .clickable { onStartGame(selectedSector) }
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

    // ── Диалог с графиком ──
    if (showChartDialog) {
        SectorChartDialog(
            repository = sectorRepository,
            onDismiss = { showChartDialog = false }
        )
    }
}

// ─────────────────────────────────────────────
// Кнопка-иконка в шапке
// ─────────────────────────────────────────────
@Composable
private fun HeaderIconButton(
    icon: String,
    onClick: () -> Unit
) {
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

// ─────────────────────────────────────────────
// Диалог с графиком по сектору
// ─────────────────────────────────────────────
@Composable
private fun SectorChartDialog(
    repository: SectorRepository,
    onDismiss: () -> Unit
) {
    var chartSector by remember { mutableStateOf(20) }
    var sectorDropdownExpanded by remember { mutableStateOf(false) }
    var games by remember { mutableStateOf<List<SectorGameEntity>>(emptyList()) }
    var loading by remember { mutableStateOf(true) }

    LaunchedEffect(chartSector) {
        loading = true
        games = withContext(Dispatchers.IO) {
            repository.getGamesBySector(chartSector)
        }.reversed()
        loading = false
    }

    val scores = games.map { it.totalScore.toDouble() }

    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(onClick = onDismiss) { Text("OK", color = Accent) }
        },
        title = {
            Text("График по секторам", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
        },
        text = {
            Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                // Выбор сектора
                Box(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(TileBg)
                            .clickable { sectorDropdownExpanded = true }
                            .padding(horizontal = 14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            "Сектор: ${sectorLabel(chartSector)}",
                            color = Color.White,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Medium,
                            modifier = Modifier.weight(1f)
                        )
                        Text("▼", color = Accent, fontSize = 12.sp)
                    }
                    DropdownMenu(
                        expanded = sectorDropdownExpanded,
                        onDismissRequest = { sectorDropdownExpanded = false },
                        modifier = Modifier
                            .background(DarkBg)
                            .heightIn(max = 400.dp)
                    ) {
                        SECTOR_LIST.forEach { sec ->
                            val isSelected = sec == chartSector
                            DropdownMenuItem(
                                text = {
                                    Text(
                                        sectorLabel(sec),
                                        color = if (isSelected) Accent else Color.White,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                    )
                                },
                                onClick = {
                                    chartSector = sec
                                    sectorDropdownExpanded = false
                                }
                            )
                        }
                    }
                }

                Spacer(Modifier.height(16.dp))

                // График
                if (loading) {
                    Box(
                        modifier = Modifier.fillMaxWidth().height(180.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("Загрузка…", color = Color.White.copy(alpha = 0.5f), fontSize = 12.sp)
                    }
                } else if (scores.size < 2) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(180.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(TileBgDark),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            "Недостаточно матчей\n(нужно минимум 2 по этому сектору)",
                            color = Color.White.copy(alpha = 0.5f),
                            fontSize = 12.sp,
                            textAlign = TextAlign.Center,
                            lineHeight = 18.sp
                        )
                    }
                } else {
                    val maxPossible = if (chartSector == 25) 750.0 else chartSector * 30.0
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(TileBgDark)
                            .padding(8.dp)
                    ) {
                        LineChart(
                            points = scores,
                            yMin = 0.0,
                            yMax = maxPossible,
                            lineColor = Accent
                        )
                    }
                    Spacer(Modifier.height(6.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            "мин: ${scores.minOrNull()?.toInt() ?: 0}",
                            color = Color.White.copy(alpha = 0.5f), fontSize = 11.sp
                        )
                        Text(
                            "сред: ${"%.0f".format(Locale.US, scores.average())}",
                            color = GoldAccent, fontSize = 11.sp
                        )
                        Text(
                            "макс: ${scores.maxOrNull()?.toInt() ?: 0}",
                            color = Color.White.copy(alpha = 0.5f), fontSize = 11.sp
                        )
                    }
                    Spacer(Modifier.height(4.dp))
                    Text(
                        "матчей: ${scores.size}",
                        color = Color.White.copy(alpha = 0.4f),
                        fontSize = 10.sp
                    )
                }
            }
        }
    )
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
