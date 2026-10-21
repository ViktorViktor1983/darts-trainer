package com.lodkin.dartstrainer.ui.training

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
import com.lodkin.dartstrainer.data.training.TrainingRepository
import com.lodkin.dartstrainer.theme.Accent
import com.lodkin.dartstrainer.theme.GoldAccent
import com.lodkin.dartstrainer.theme.TileBg
import com.lodkin.dartstrainer.theme.TileBgDark
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.Locale

/**
 * Экран детальной статистики по конкретной дисциплине.
 */
@Composable
fun DisciplineStatsScreen(
    repository: TrainingRepository,
    disciplineId: String,
    onBack: () -> Unit
) {
    var period by remember { mutableStateOf(StatPeriod.ALL) }
    var loading by remember { mutableStateOf(true) }
    
    var showChartsDialog by remember { mutableStateOf(false) }

    // ── Стейты для 501 ──
    var avgPpr by remember { mutableStateOf(0f) }
    var avgDbl by remember { mutableStateOf(0f) }
    var avgFirst9 by remember { mutableStateOf(0f) }
    
    var count180 by remember { mutableStateOf(0) }
    var count170 by remember { mutableStateOf(0) }
    var count130 by remember { mutableStateOf(0) }
    var count90 by remember { mutableStateOf(0) }
    var count57p by remember { mutableStateOf(0) }
    var count57m by remember { mutableStateOf(0) }
    
    // ── Стейты для Крикета ──
    var avgMpr by remember { mutableStateOf(0f) }
    var avgPerfectRounds by remember { mutableStateOf(0f) }
    var avgStrongRounds by remember { mutableStateOf(0f) }
    var avgMisses by remember { mutableStateOf(0f) }
    var avgTriples by remember { mutableStateOf(0f) }
    var bullAccuracy by remember { mutableStateOf(0f) }
    
    // Данные для графиков
    var pprChartData by remember { mutableStateOf<List<Double>>(emptyList()) }
    var dblChartData by remember { mutableStateOf<List<Double>>(emptyList()) }
    var first9ChartData by remember { mutableStateOf<List<Double>>(emptyList()) }
    var c57mChartData by remember { mutableStateOf<List<Double>>(emptyList()) }
    
    var mprChartData by remember { mutableStateOf<List<Double>>(emptyList()) }
    var perfectRoundsChartData by remember { mutableStateOf<List<Double>>(emptyList()) }

    LaunchedEffect(disciplineId, period) {
        loading = true
        val cutoff = period.daysBack?.let {
            System.currentTimeMillis() - it * 24L * 60L * 60L * 1000L
        }

        val allResults = withContext(Dispatchers.IO) {
            repository.getAllExerciseResultsByExerciseId(disciplineId)
        }
        
        val filtered = if (cutoff == null) allResults else allResults.filter { it.timestamp >= cutoff }

        if (disciplineId == "game501" && filtered.isNotEmpty()) {
            var sumPpr = 0f
            var sumDbl = 0f
            var sumFirst9 = 0f
            var totalMatches = 0

            var c180 = 0; var c170 = 0; var c130 = 0; var c90 = 0; var c57p = 0; var c57m = 0
            
            val pprs = mutableListOf<Double>()
            val dbls = mutableListOf<Double>()
            val f9s = mutableListOf<Double>()
            val c57ms = mutableListOf<Double>()

            filtered.forEach { r ->
                val details = parseDetails(r.detailsJson)
                val ppr = details["ppr"]?.toFloatOrNull() ?: 0f
                val dbl = details["dbl"]?.toFloatOrNull() ?: 0f
                val f9 = details["first9"]?.toFloatOrNull() ?: 0f
                val c57 = details["c57m"]?.toIntOrNull() ?: 0
                
                if (ppr > 0f) {
                    sumPpr += ppr; sumDbl += dbl; sumFirst9 += f9; totalMatches++
                    pprs.add(ppr.toDouble()); dbls.add(dbl.toDouble()); f9s.add(f9.toDouble()); c57ms.add(c57.toDouble())
                }

                c180 += details["c180"]?.toIntOrNull() ?: 0
                c170 += details["c170"]?.toIntOrNull() ?: 0
                c130 += details["c130"]?.toIntOrNull() ?: 0
                c90 += details["c90"]?.toIntOrNull() ?: 0
                c57p += details["c57p"]?.toIntOrNull() ?: 0
                c57m += details["c57m"]?.toIntOrNull() ?: 0
            }

            if (totalMatches > 0) {
                avgPpr = sumPpr / totalMatches
                avgDbl = sumDbl / totalMatches
                avgFirst9 = sumFirst9 / totalMatches
            }
            
            val divisor = if (totalMatches > 0) totalMatches.toFloat() else 1f
            count180 = (c180 / divisor).toInt()
            count170 = (c170 / divisor).toInt()
            count130 = (c130 / divisor).toInt()
            count90 = (c90 / divisor).toInt()
            count57p = (c57p / divisor).toInt()
            count57m = (c57m / divisor).toInt()

            pprChartData = pprs
            dblChartData = dbls
            first9ChartData = f9s
            c57mChartData = c57ms
            
        } else if (disciplineId == "cricket" && filtered.isNotEmpty()) {
            var sumMpr = 0f
            var sumPerfect = 0f
            var sumStrong = 0f
            var sumMisses = 0f
            var sumTriples = 0f
            var totalBullAttempts = 0
            var totalBullHits = 0
            var totalMatches = 0
            
            val mprs = mutableListOf<Double>()
            val perfects = mutableListOf<Double>()

            filtered.forEach { r ->
                val details = parseDetails(r.detailsJson)
                val mpr = details["mpr"]?.toFloatOrNull() ?: 0f
                
                if (mpr > 0f) {
                    sumMpr += mpr
                    sumPerfect += (details["c9r"]?.toIntOrNull() ?: 0).toFloat()
                    sumStrong += (details["c6r"]?.toIntOrNull() ?: 0).toFloat()
                    sumMisses += (details["miss"]?.toIntOrNull() ?: 0).toFloat()
                    sumTriples += (details["tripl"]?.toIntOrNull() ?: 0).toFloat()
                    totalBullAttempts += (details["bulla"]?.toIntOrNull() ?: 0)
                    totalBullHits += (details["bullh"]?.toIntOrNull() ?: 0)
                    totalMatches++
                    
                    mprs.add(mpr.toDouble())
                    perfects.add((details["c9r"]?.toIntOrNull() ?: 0).toDouble())
                }
            }

            if (totalMatches > 0) {
                avgMpr = sumMpr / totalMatches
                avgPerfectRounds = sumPerfect / totalMatches
                avgStrongRounds = sumStrong / totalMatches
                avgMisses = sumMisses / totalMatches
                avgTriples = sumTriples / totalMatches
                if (totalBullAttempts > 0) {
                    bullAccuracy = totalBullHits.toFloat() / totalBullAttempts * 100f
                }
            }
            
            mprChartData = mprs
            perfectRoundsChartData = perfects
        }
        loading = false
    }

    val disciplineTitle = when(disciplineId) {
        "game501" -> "501"
        "cricket" -> "Крикет"
        "sector20" -> "Сектор 20"
        else -> "Статистика"
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0D1117))
            .padding(16.dp)
    ) {
        // Шапка
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .background(TileBgDark)
                    .clickable { onBack() }
                    .padding(horizontal = 14.dp, vertical = 10.dp)
            ) {
                Text("← Назад", color = Accent, fontSize = 14.sp, fontWeight = FontWeight.Medium)
            }
            Spacer(Modifier.width(12.dp))
            Text(
                disciplineTitle,
                color = GoldAccent,
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.weight(1f)
            )
            // Кнопка графиков
            val hasCharts = if (disciplineId == "game501") pprChartData.isNotEmpty() else mprChartData.isNotEmpty()
            if (hasCharts) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(TileBgDark)
                        .clickable { showChartsDialog = true },
                    contentAlignment = Alignment.Center
                ) {
                    Text("📈", fontSize = 22.sp)
                }
            }
        }

        Spacer(Modifier.height(16.dp))

        // Фильтры по времени
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            listOf(StatPeriod.WEEK, StatPeriod.MONTH, StatPeriod.THREE_MONTHS).forEach { p ->
                PeriodChip(p.label, p == period, { period = p }, Modifier.weight(1f))
            }
        }
        Spacer(Modifier.height(6.dp))
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            listOf(StatPeriod.HALF_YEAR, StatPeriod.YEAR, StatPeriod.ALL).forEach { p ->
                PeriodChip(p.label, p == period, { period = p }, Modifier.weight(1f))
            }
        }

        Spacer(Modifier.height(20.dp))

        if (loading) {
            Box(modifier = Modifier.fillMaxWidth().weight(1f), contentAlignment = Alignment.Center) {
                Text("Загружаем данные...", color = Accent)
            }
        } else if (disciplineId == "game501" && avgPpr == 0f) {
            EmptyStats()
        } else if (disciplineId == "cricket" && avgMpr == 0f) {
            EmptyStats()
        } else if (disciplineId == "game501") {
            // ── 501 ──
            Column(
                modifier = Modifier.fillMaxWidth().weight(1f).verticalScroll(rememberScrollState())
            ) {
                Text("ОСНОВНЫЕ", color = Accent, fontSize = 12.sp, fontWeight = FontWeight.Bold, letterSpacing = 2.sp)
                Spacer(Modifier.height(8.dp))
                StatRowCard("Средний PPR", String.format(Locale.US, "%.2f", avgPpr))
                Spacer(Modifier.height(6.dp))
                StatRowCard("Средняя точность удвоений", String.format(Locale.US, "%.1f%%", avgDbl))
                Spacer(Modifier.height(6.dp))
                StatRowCard("Средний набор за 9 дротиков", String.format(Locale.US, "%.2f", avgFirst9))

                Spacer(Modifier.height(20.dp))
                Text("КАТЕГОРИИ (в среднем за матч)", color = Accent, fontSize = 12.sp, fontWeight = FontWeight.Bold, letterSpacing = 2.sp)
                Spacer(Modifier.height(8.dp))
                StatRowCard("180", "$count180")
                Spacer(Modifier.height(6.dp))
                StatRowCard("170+", "$count170")
                Spacer(Modifier.height(6.dp))
                StatRowCard("130+", "$count130")
                Spacer(Modifier.height(6.dp))
                StatRowCard("90+", "$count90")
                Spacer(Modifier.height(6.dp))
                StatRowCard("57+", "$count57p")
                Spacer(Modifier.height(6.dp))
                StatRowCard("57-", "$count57m")
                Spacer(Modifier.height(24.dp))
            }
        } else if (disciplineId == "cricket") {
            // ── КРИКЕТ ──
            Column(
                modifier = Modifier.fillMaxWidth().weight(1f).verticalScroll(rememberScrollState())
            ) {
                Text("ОСНОВНЫЕ", color = Accent, fontSize = 12.sp, fontWeight = FontWeight.Bold, letterSpacing = 2.sp)
                Spacer(Modifier.height(8.dp))
                StatRowCard("Средний MPR", String.format(Locale.US, "%.2f", avgMpr))
                Spacer(Modifier.height(6.dp))
                StatRowCard("Точность Bull", if (bullAccuracy > 0) String.format(Locale.US, "%.1f%%", bullAccuracy) else "—")
                Spacer(Modifier.height(6.dp))
                StatRowCard("Промахи (в среднем за матч)", String.format(Locale.US, "%.1f", avgMisses))
                Spacer(Modifier.height(6.dp))
                StatRowCard("Утроения (в среднем за матч)", String.format(Locale.US, "%.1f", avgTriples))

                Spacer(Modifier.height(20.dp))
                Text("КАЧЕСТВЕННЫЕ ПОДХОДЫ (в среднем за матч)", color = Accent, fontSize = 12.sp, fontWeight = FontWeight.Bold, letterSpacing = 2.sp)
                Spacer(Modifier.height(8.dp))
                StatRowCard("Идеальные (9 марок)", String.format(Locale.US, "%.1f", avgPerfectRounds))
                Spacer(Modifier.height(6.dp))
                StatRowCard("Сильные (6-7 марок)", String.format(Locale.US, "%.1f", avgStrongRounds))
                
                Spacer(Modifier.height(24.dp))
            }
        } else {
            Box(modifier = Modifier.fillMaxWidth().weight(1f), contentAlignment = Alignment.Center) {
                Text("Детальная статистика для этой дисциплины\nв разработке", color = Color.White.copy(alpha = 0.5f), textAlign = TextAlign.Center)
            }
        }
    }

    if (showChartsDialog) {
        if (disciplineId == "game501") {
            AllCharts501Dialog(pprChartData, dblChartData, first9ChartData, c57mChartData) { showChartsDialog = false }
        } else if (disciplineId == "cricket") {
            AllChartsCricketDialog(mprChartData, perfectRoundsChartData) { showChartsDialog = false }
        }
    }
}

@Composable
private fun EmptyStats() {
    Box(modifier = Modifier.fillMaxWidth().weight(1f), contentAlignment = Alignment.Center) {
        Text("Нет данных за выбранный период", color = Color.White.copy(alpha = 0.5f))
    }
}

// ─────────────────────────────────────────────
// ДИАЛОГИ С ГРАФИКАМИ
// ─────────────────────────────────────────────

@Composable
private fun AllCharts501Dialog(
    pprData: List<Double>, dblData: List<Double>, first9Data: List<Double>, c57mData: List<Double>, onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = TileBgDark,
        confirmButton = { TextButton(onClick = onDismiss) { Text("ЗАКРЫТЬ", color = Accent) } },
        title = { Text("Графики 501", color = GoldAccent, fontSize = 20.sp, fontWeight = FontWeight.Bold) },
        text = {
            Column(modifier = Modifier.fillMaxWidth().verticalScroll(rememberScrollState())) {
                ChartBlock("Средний PPR", pprData, 0.0, 120.0, Accent)
                Spacer(Modifier.height(24.dp))
                ChartBlock("Точность удвоений (%)", dblData, 0.0, 100.0, GoldAccent)
                Spacer(Modifier.height(24.dp))
                ChartBlock("Набор за первые 9 дротиков", first9Data, 0.0, 180.0, Color(0xFF4CAF50))
                Spacer(Modifier.height(24.dp))
                ChartBlock("Подходы < 57 (шт.)", c57mData, 0.0, 20.0, Color(0xFFE57373))
                Spacer(Modifier.height(4.dp))
                Text("Чем ниже линия, тем меньше провальных подходов.", color = Color.White.copy(alpha = 0.5f), fontSize = 11.sp)
            }
        }
    )
}

@Composable
private fun AllChartsCricketDialog(
    mprData: List<Double>, perfectRoundsData: List<Double>, onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = TileBgDark,
        confirmButton = { TextButton(onClick = onDismiss) { Text("ЗАКРЫТЬ", color = Accent) } },
        title = { Text("Графики Крикета", color = GoldAccent, fontSize = 20.sp, fontWeight = FontWeight.Bold) },
        text = {
            Column(modifier = Modifier.fillMaxWidth().verticalScroll(rememberScrollState())) {
                ChartBlock("Средний MPR", mprData, 0.0, 9.0, Accent)
                Spacer(Modifier.height(24.dp))
                ChartBlock("Идеальные раунды (9 марок)", perfectRoundsData, 0.0, 10.0, GoldAccent)
                Spacer(Modifier.height(4.dp))
                Text("Чем выше линия, тем больше ты делаешь идеальных подходов.", color = Color.White.copy(alpha = 0.5f), fontSize = 11.sp)
            }
        }
    )
}

@Composable
private fun ChartBlock(title: String, points: List<Double>, yMin: Double, yMax: Double, lineColor: Color) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(title, color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Medium)
        Spacer(Modifier.height(6.dp))

        if (points.size < 2) {
            Text("Недостаточно матчей для графика", color = Color.White.copy(alpha = 0.5f), fontSize = 11.sp)
        } else {
            Box(modifier = Modifier.fillMaxWidth().height(150.dp).clip(RoundedCornerShape(10.dp)).background(Color.Black.copy(alpha = 0.2f)).padding(8.dp)) {
                LineChart(points, yMin, yMax, lineColor)
            }
            Spacer(Modifier.height(4.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("мин: %.1f".format(Locale.US, points.minOrNull() ?: 0.0), color = Color.White.copy(alpha = 0.5f), fontSize = 10.sp)
                Text("сред: %.1f".format(Locale.US, points.average()), color = GoldAccent, fontSize = 10.sp)
                Text("макс: %.1f".format(Locale.US, points.maxOrNull() ?: 0.0), color = Color.White.copy(alpha = 0.5f), fontSize = 10.sp)
            }
        }
    }
}

@Composable
private fun LineChart(points: List<Double>, yMin: Double, yMax: Double, lineColor: Color) {
    val gridColor = Color.White.copy(alpha = 0.12f)

    Canvas(modifier = Modifier.fillMaxSize()) {
        val w = size.width; val h = size.height
        val leftPad = 36.dp.toPx(); val rightPad = 8.dp.toPx()
        val topPad = 10.dp.toPx(); val bottomPad = 18.dp.toPx()

        val plotW = w - leftPad - rightPad
        val plotH = h - topPad - bottomPad
        if (plotW <= 0f || plotH <= 0f) return@Canvas

        val range = (yMax - yMin).coerceAtLeast(0.0001)

        val gridSteps = 5
        for (i in 0..gridSteps) {
            val y = topPad + plotH * i / gridSteps
            drawLine(color = gridColor, start = Offset(leftPad, y), end = Offset(leftPad + plotW, y), strokeWidth = 1f)
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
        coords.forEach { c -> drawCircle(color = lineColor, radius = dotRadius, center = c) }
    }
}

// ─────────────────────────────────────────────
// ВСПОМОГАТЕЛЬНЫЕ КОМПОНЕНТЫ
// ─────────────────────────────────────────────

@Composable
private fun StatRowCard(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(10.dp)).background(TileBg).padding(horizontal = 16.dp, vertical = 14.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(label, color = Color.White, fontSize = 14.sp)
        Text(value, color = GoldAccent, fontSize = 18.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun PeriodChip(label: String, selected: Boolean, onClick: () -> Unit, modifier: Modifier) {
    Box(
        modifier = modifier.clip(RoundedCornerShape(10.dp)).background(if (selected) Accent else TileBgDark).clickable { onClick() }.padding(vertical = 10.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(label, color = if (selected) Color(0xFF121212) else Color.White, fontSize = 12.sp, fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium)
    }
}

private fun parseDetails(s: String): Map<String, String> {
    if (s.isBlank()) return emptyMap()
    return s.split(";").mapNotNull { entry ->
        val parts = entry.split("=", limit = 2)
        if (parts.size == 2) parts[0].trim() to parts[1].trim() else null
    }.toMap()
}

enum class StatPeriod(val label: String, val daysBack: Long?) {
    WEEK("Неделя", 7),
    MONTH("Месяц", 30),
    THREE_MONTHS("3 мес", 90),
    HALF_YEAR("Полгода", 180),
    YEAR("Год", 365),
    ALL("Всё время", null)
}
