package com.lodkin.dartstrainer.ui.biground

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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.lodkin.dartstrainer.data.biground.BigRoundCategory
import com.lodkin.dartstrainer.data.biground.BigRoundGameEntity
import com.lodkin.dartstrainer.data.biground.BigRoundRepository
import com.lodkin.dartstrainer.theme.Accent
import com.lodkin.dartstrainer.theme.DarkBg
import com.lodkin.dartstrainer.theme.GoldAccent
import com.lodkin.dartstrainer.theme.TileBg
import com.lodkin.dartstrainer.theme.TileBgDark
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.Locale

@Composable
fun BigRoundSetupScreen(
    playerName: String,
    repository: BigRoundRepository,
    onStartGame: (BigRoundCategory) -> Unit,
    onOpenStats: () -> Unit,
    onBack: () -> Unit
) {
    var category by remember { mutableStateOf(BigRoundCategory.MALE) }
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
                "Большой раунд",
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

            Text(
                "КАТЕГОРИЯ",
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
                CategoryButton(
                    label = "Мужской",
                    selected = category == BigRoundCategory.MALE,
                    onClick = { category = BigRoundCategory.MALE },
                    modifier = Modifier.weight(1f)
                )
                CategoryButton(
                    label = "Юношеский",
                    selected = category == BigRoundCategory.YOUTH,
                    onClick = { category = BigRoundCategory.YOUTH },
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(Modifier.height(24.dp))

            // ── Описание ──
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
                        "21 сектор по 3 дротика: 1 → 2 → 3 → … → 20 → Bull. " +
                        "Вводишь число попаданий за подход (S=1, D=2, T=3), " +
                        "приложение считает очки. После партии — норматив.",
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
                .clickable { onStartGame(category) }
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
        BigRoundChartDialog(
            repository = repository,
            onDismiss = { showChartDialog = false }
        )
    }
}

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
private fun CategoryButton(
    label: String,
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
        Text(
            label,
            color = if (selected) Color(0xFF121212) else Color.White,
            fontSize = 15.sp,
            fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium
        )
    }
}

// ─────────────────────────────────────────────
// Диалог с графиком
// ─────────────────────────────────────────────
@Composable
private fun BigRoundChartDialog(
    repository: BigRoundRepository,
    onDismiss: () -> Unit
) {
    var chartCategory by remember { mutableStateOf(BigRoundCategory.MALE) }
    var games by remember { mutableStateOf<List<BigRoundGameEntity>>(emptyList()) }
    var loading by remember { mutableStateOf(true) }

    LaunchedEffect(chartCategory) {
        loading = true
        games = withContext(Dispatchers.IO) {
            repository.getAllGames().filter { it.categoryName == chartCategory.name }
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
            Text("График · Большой раунд", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
        },
        text = {
            Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                // Переключатель категории
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    listOf(BigRoundCategory.MALE, BigRoundCategory.YOUTH).forEach { c ->
                        val isSelected = c == chartCategory
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (isSelected) Accent else TileBg)
                                .clickable { chartCategory = c }
                                .padding(vertical = 10.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                if (c == BigRoundCategory.MALE) "Мужской" else "Юношеский",
                                color = if (isSelected) Color(0xFF121212) else Color.White,
                                fontSize = 13.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
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
                            "Недостаточно матчей\n(нужно минимум 2)",
                            color = Color.White.copy(alpha = 0.5f),
                            fontSize = 12.sp,
                            textAlign = TextAlign.Center,
                            lineHeight = 18.sp
                        )
                    }
                } else {
                    val maxY = (scores.maxOrNull() ?: 100.0) * 1.1
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
