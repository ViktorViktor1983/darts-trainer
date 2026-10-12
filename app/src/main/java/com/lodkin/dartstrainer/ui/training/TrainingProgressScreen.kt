package com.lodkin.dartstrainer.ui.training

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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.lodkin.dartstrainer.data.training.DartsNorms
import com.lodkin.dartstrainer.data.training.TrainingRepository
import com.lodkin.dartstrainer.data.training.TrainingStatsAnalyzer
import com.lodkin.dartstrainer.data.training.WeaknessDetector
import com.lodkin.dartstrainer.data.training.WeaknessType
import com.lodkin.dartstrainer.theme.Accent
import com.lodkin.dartstrainer.theme.GoldAccent
import com.lodkin.dartstrainer.theme.TileBg
import com.lodkin.dartstrainer.theme.TileBgDark
import java.util.Locale

/**
 * Экран «Мой прогресс».
 *
 * Показывает:
 *   • Уровень и потенциал (по накопленным данным).
 *   • 4 параметра: набор очков, точность, удвоения, утроения.
 *   • Слабое место.
 *   • Средний темп по каждому упражнению.
 *
 * Если данных мало — предупреждение вместо цифр.
 */
@Composable
fun TrainingProgressScreen(
    repository: TrainingRepository,
    onBack: () -> Unit
) {
    var analysis by remember { mutableStateOf<TrainingStatsAnalyzer.TrainingAnalysis?>(null) }
    var loading by remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        analysis = TrainingStatsAnalyzer.analyze(repository)
        loading = false
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0D1117))
            .padding(16.dp)
    ) {
        // ── Верхняя панель ──
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
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
                "Мой прогресс",
                color = GoldAccent,
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(Modifier.height(20.dp))

        if (loading) {
            Box(
                modifier = Modifier.fillMaxWidth().padding(40.dp),
                contentAlignment = Alignment.Center
            ) {
                Text("Считаем...", color = Accent, fontSize = 16.sp)
            }
            return@Column
        }

        val a = analysis
        if (a == null) {
            Box(
                modifier = Modifier.fillMaxWidth().padding(40.dp),
                contentAlignment = Alignment.Center
            ) {
                Text("Не удалось проанализировать данные", color = Color.White.copy(alpha = 0.6f), fontSize = 14.sp)
            }
            return@Column
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .verticalScroll(rememberScrollState())
        ) {
            if (!a.hasEnoughData) {
                InfoCard(
                    title = "Мало данных",
                    text = "Пока накоплено ${a.dataPointsCount} записей. Нужно хотя бы 6 " +
                            "(по одному разу каждое упражнение), чтобы программа точно подбирала " +
                            "нагрузку. Пройди ещё несколько тренировок — и данные появятся."
                )
                Spacer(Modifier.height(16.dp))
            }

            // ── Уровень ──
            LevelBlock(a)
            Spacer(Modifier.height(16.dp))

            // ── 4 параметра ──
            SectionTitle("ПАРАМЕТРЫ")
            Spacer(Modifier.height(10.dp))

            ParamRow(
                label = "Набор очков",
                value = a.avgScore?.let { "$it очков" } ?: "нет данных",
                level = a.levelByScore,
                isWeak = a.weakestType == WeaknessType.SCORE
            )
            Spacer(Modifier.height(8.dp))

            ParamRow(
                label = "Точность в сектор",
                value = a.avgSectorsPercent?.let { formatPercent(it) + " попаданий" } ?: "нет данных",
                level = if (a.weakestType == WeaknessType.ACCURACY) 0 else 0,
                isWeak = a.weakestType == WeaknessType.ACCURACY
            )
            Spacer(Modifier.height(8.dp))

            ParamRow(
                label = "Удвоения",
                value = a.avgDoublesPercent?.let { formatPercent(it) + " попаданий" } ?: "нет данных",
                level = 0,
                isWeak = a.weakestType == WeaknessType.DOUBLES
            )
            Spacer(Modifier.height(8.dp))

            ParamRow(
                label = "Утроения",
                value = a.avgTreblesPercent?.let { formatPercent(it) + " попаданий" } ?: "нет данных",
                level = 0,
                isWeak = a.weakestType == WeaknessType.TREBLES
            )

            Spacer(Modifier.height(20.dp))

            // ── Слабое место ──
            if (a.weakestType != null) {
                SectionTitle("СЛАБОЕ МЕСТО")
                Spacer(Modifier.height(10.dp))
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(Accent.copy(alpha = 0.12f))
                        .padding(16.dp)
                ) {
                    Text(
                        WeaknessDetector.describe(a.weakestType),
                        color = GoldAccent,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(Modifier.height(6.dp))
                    Text(
                        "Программа на день делает акцент на этом параметре. " +
                                "Не более 50% времени, чтобы не перегружать.",
                        color = Color.White.copy(alpha = 0.75f),
                        fontSize = 13.sp
                    )
                }
                Spacer(Modifier.height(20.dp))
            }

            // ── Средний темп ──
            if (a.avgMinutesByExercise.isNotEmpty()) {
                SectionTitle("ТВОЙ ТЕМП")
                Spacer(Modifier.height(10.dp))
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(TileBgDark)
                        .padding(14.dp)
                ) {
                    Text(
                        "Среднее время по последним тренировкам. Программа подстраивается под него.",
                        color = Color.White.copy(alpha = 0.6f),
                        fontSize = 12.sp
                    )
                    Spacer(Modifier.height(12.dp))

                    a.avgMinutesByExercise.forEach { (id, minutes) ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                exerciseTitle(id),
                                color = Color.White,
                                fontSize = 14.sp,
                                modifier = Modifier.weight(1f)
                            )
                            Text(
                                formatMinutes(minutes),
                                color = Accent,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
                Spacer(Modifier.height(20.dp))
            }

            Spacer(Modifier.height(16.dp))
        }
    }
}

// ─────────────────────────────────────────────
// Блок «Уровень»
// ─────────────────────────────────────────────

@Composable
private fun LevelBlock(a: TrainingStatsAnalyzer.TrainingAnalysis) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(TileBg)
            .padding(16.dp)
    ) {
        Text(
            "УРОВЕНЬ",
            color = Accent,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 2.sp
        )
        Spacer(Modifier.height(10.dp))

        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                "${a.level}",
                color = GoldAccent,
                fontSize = 36.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(Modifier.width(10.dp))
            Text(
                DartsNorms.getLevelName(a.level),
                color = Color.White,
                fontSize = 18.sp,
                fontWeight = FontWeight.Medium
            )
        }

        Spacer(Modifier.height(10.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                "Потенциал",
                color = Color.White.copy(alpha = 0.6f),
                fontSize = 13.sp
            )
            Text(
                "${a.potential}",
                color = Accent,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

// ─────────────────────────────────────────────
// Строка параметра
// ─────────────────────────────────────────────

@Composable
private fun ParamRow(
    label: String,
    value: String,
    level: Int,
    isWeak: Boolean
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(if (isWeak) Accent.copy(alpha = 0.12f) else TileBgDark)
            .padding(14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                label,
                color = Color.White,
                fontSize = 14.sp,
                fontWeight = if (isWeak) FontWeight.Bold else FontWeight.Medium
            )
            Spacer(Modifier.height(2.dp))
            Text(
                value,
                color = Color.White.copy(alpha = 0.65f),
                fontSize = 12.sp
            )
        }
        if (isWeak) {
            Text(
                "★",
                color = GoldAccent,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
        } else if (level > 0) {
            Text(
                "ур. $level",
                color = Accent,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

// ─────────────────────────────────────────────
// Инфо-карточка
// ─────────────────────────────────────────────

@Composable
private fun InfoCard(title: String, text: String) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(TileBgDark)
            .padding(16.dp)
    ) {
        Text(
            title,
            color = GoldAccent,
            fontSize = 15.sp,
            fontWeight = FontWeight.Bold
        )
        Spacer(Modifier.height(6.dp))
        Text(
            text,
            color = Color.White.copy(alpha = 0.8f),
            fontSize = 13.sp,
            lineHeight = 19.sp
        )
    }
}

@Composable
private fun SectionTitle(text: String) {
    Text(
        text,
        color = Accent,
        fontSize = 12.sp,
        fontWeight = FontWeight.Bold,
        letterSpacing = 2.sp
    )
}

// ─────────────────────────────────────────────
// Утилиты
// ─────────────────────────────────────────────

private fun formatPercent(value: Float): String =
    String.format(Locale.US, "%.0f%%", value)

private fun formatMinutes(minutes: Float): String {
    val rounded = kotlin.math.round(minutes * 10f) / 10f
    return String.format(Locale.US, "%.1f мин", rounded)
}

private fun exerciseTitle(id: String): String = when (id) {
    "aroundclock_doubles" -> "Кругосветка: Удвоения"
    "aroundclock_sectors" -> "Кругосветка: Сектора"
    "aroundclock_trebles" -> "Кругосветка: Утроения"
    "sector20" -> "Сектор 20"
    "scoreset" -> "Набор очков"
    "biground" -> "Большой раунд"
    else -> id
}
