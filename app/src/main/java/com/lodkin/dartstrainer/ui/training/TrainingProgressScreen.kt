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
import com.lodkin.dartstrainer.theme.ErrorColor
import com.lodkin.dartstrainer.theme.GoldAccent
import com.lodkin.dartstrainer.theme.TileBg
import com.lodkin.dartstrainer.theme.TileBgDark
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.Locale

/**
 * Экран «Мой прогресс».
 */
@Composable
fun TrainingProgressScreen(
    repository: TrainingRepository,
    onBack: () -> Unit
) {
    var analysis by remember { mutableStateOf<TrainingStatsAnalyzer.TrainingAnalysis?>(null) }
    var loading by remember { mutableStateOf(true) }

    var checkoutsMap by remember { mutableStateOf<Map<Int, Int>>(emptyMap()) }
    var showCheckouts by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        analysis = TrainingStatsAnalyzer.analyze(repository)
        checkoutsMap = withContext(Dispatchers.IO) {
            repository.getAllCheckouts()
        }
        loading = false
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0D1117))
            .padding(16.dp)
    ) {
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
                fontWeight = FontWeight.Bold,
                modifier = Modifier.weight(1f)
            )
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Accent.copy(alpha = 0.18f))
                    .clickable { showCheckouts = true },
                contentAlignment = Alignment.Center
            ) {
                Text("🏆", fontSize = 20.sp)
            }
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

            FinalLevelBlock(a)
            Spacer(Modifier.height(16.dp))

            if (a.gamesLagBehind || a.exercisesLagBehind) {
                DivergenceCard(a)
                Spacer(Modifier.height(16.dp))
            }

            SectionTitle("КОМПЛЕКС 1 — УПРАЖНЕНИЯ")
            Spacer(Modifier.height(10.dp))
            ComplexHeader(
                level = a.complex1Level,
                subtitle = "среднее по 4 параметрам"
            )
            Spacer(Modifier.height(8.dp))

            ParamRow(
                label = "Набор очков",
                value = a.avgScore?.let { "$it очков" } ?: "нет данных",
                level = a.levelByScore,
                isWeak = a.weakestType == WeaknessType.SCORE
            )
            Spacer(Modifier.height(6.dp))
            ParamRow(
                label = "Точность в сектор",
                value = a.avgSectorsPercent?.let { formatPercent(it) + " попаданий" } ?: "нет данных",
                level = a.levelBySectors,
                isWeak = a.weakestType == WeaknessType.ACCURACY
            )
            Spacer(Modifier.height(6.dp))
            ParamRow(
                label = "Удвоения",
                value = a.avgDoublesPercent?.let { formatPercent(it) + " попаданий" } ?: "нет данных",
                level = a.levelByDoubles,
                isWeak = a.weakestType == WeaknessType.DOUBLES
            )
            Spacer(Modifier.height(6.dp))
            ParamRow(
                label = "Утроения",
                value = a.avgTreblesPercent?.let { formatPercent(it) + " попаданий" } ?: "нет данных",
                level = a.levelByTrebles,
                isWeak = a.weakestType == WeaknessType.TREBLES
            )

            Spacer(Modifier.height(20.dp))

            SectionTitle("КОМПЛЕКС 2 — ИГРЫ")
            Spacer(Modifier.height(10.dp))
            ComplexHeader(
                level = a.complex2Level,
                subtitle = "среднее по 501 и Крикету"
            )
            Spacer(Modifier.height(8.dp))

            ParamRow(
                label = "501 — PPR",
                value = a.avg501Ppr?.let {
                    String.format(Locale.US, "%.1f · %d партий", it, a.count501Matches)
                } ?: "нет данных",
                level = a.level501,
                isWeak = false
            )
            Spacer(Modifier.height(6.dp))
            ParamRow(
                label = "Крикет — MPR",
                value = a.avgCricketMpr?.let {
                    String.format(Locale.US, "%.1f · %d партий", it, a.countCricketMatches)
                } ?: "нет данных",
                level = a.levelCricket,
                isWeak = false
            )

            Spacer(Modifier.height(20.dp))

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

            SectionTitle("ХУДШИЕ СЕКТОРА")
            Spacer(Modifier.height(10.dp))
            WorstSectorsBlock(
                sectors = a.worstSectors,
                dataAvailable = a.sectorsDataAvailable
            )
            Spacer(Modifier.height(20.dp))

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

    if (showCheckouts) {
        TrainingCheckoutsDialog(
            closedCheckouts = checkoutsMap,
            onDismiss = { showCheckouts = false }
        )
    }
}

/**
 * Блок «Худшие сектора».
 *
 * Показывает ТОЛЬКО проблемные сектора (отставание от среднего > 25%).
 * Сектора, которые идут нормально или лучше среднего, не показываются —
 * они не тянут статистику вниз.
 *
 * Три состояния:
 *   1. Данных мало (Кругосветку-Сектора прошёл < 5 раз) → подсказка.
 *   2. Данных достаточно, проблемных секторов нет → «всё в норме».
 *   3. Есть проблемные сектора → список топ-3 худших с индикацией:
 *      🔴 отставание > 50%
 *      🟡 отставание > 25%
 */
@Composable
private fun WorstSectorsBlock(
    sectors: List<TrainingStatsAnalyzer.SectorWeakness>,
    dataAvailable: Boolean
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(TileBgDark)
            .padding(14.dp)
    ) {
        when {
            !dataAvailable -> {
                Text(
                    "Пока не хватает данных. Чтобы увидеть слабые сектора, нужно пройти " +
                            "Кругосветку-Сектора минимум 5 раз.",
                    color = Color.White.copy(alpha = 0.75f),
                    fontSize = 13.sp,
                    lineHeight = 19.sp
                )
            }

            sectors.isEmpty() -> {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("✅", fontSize = 18.sp)
                    Spacer(Modifier.width(8.dp))
                    Text(
                        "Все сектора в норме",
                        color = GoldAccent,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
                Spacer(Modifier.height(6.dp))
                Text(
                    "Ни один сектор не отстаёт от твоего среднего больше чем на 25%. " +
                            "Продолжай в том же духе.",
                    color = Color.White.copy(alpha = 0.75f),
                    fontSize = 13.sp,
                    lineHeight = 19.sp
                )
            }

            else -> {
                Text(
                    "Сектора, которые тянут статистику вниз.",
                    color = Color.White.copy(alpha = 0.6f),
                    fontSize = 12.sp
                )
                Spacer(Modifier.height(10.dp))

                sectors.forEachIndexed { index, s ->
                    if (index > 0) {
                        Spacer(Modifier.height(10.dp))
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            sectorIndicator(s.deviation),
                            fontSize = 18.sp
                        )
                        Spacer(Modifier.width(10.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                sectorName(s.sector),
                                color = Color.White,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(Modifier.height(2.dp))
                            Text(
                                "ниже среднего на ${formatPercent(s.deviation)}",
                                color = Color.White.copy(alpha = 0.65f),
                                fontSize = 12.sp
                            )
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                formatPercent(s.percent),
                                color = GoldAccent,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                "${s.count}×",
                                color = Color.White.copy(alpha = 0.5f),
                                fontSize = 11.sp
                            )
                        }
                    }
                }

                Spacer(Modifier.height(10.dp))
                Text(
                    "🔴 >50% · 🟡 >25%",
                    color = Color.White.copy(alpha = 0.45f),
                    fontSize = 11.sp
                )
            }
        }
    }
}

/** Цветовой индикатор отклонения от среднего. */
private fun sectorIndicator(deviation: Float): String = when {
    deviation > 50f -> "🔴"
    else -> "🟡"
}

/** Название сектора: 1..20 — «Сектор N», 25 — «Bull (25)». */
private fun sectorName(sector: Int): String =
    if (sector == 25) "Bull (25)" else "Сектор $sector"

@Composable
private fun FinalLevelBlock(a: TrainingStatsAnalyzer.TrainingAnalysis) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(TileBg)
            .padding(16.dp)
    ) {
        Text(
            "ТВОЙ УРОВЕНЬ",
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
                fontSize = 42.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(Modifier.width(12.dp))
            Text(
                DartsNorms.getLevelName(a.level),
                color = Color.White,
                fontSize = 20.sp,
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

@Composable
private fun DivergenceCard(a: TrainingStatsAnalyzer.TrainingAnalysis) {
    val message = if (a.gamesLagBehind) {
        "Упражнения дают ${a.complex1Level}-й уровень, а игры — только ${a.complex2Level}-й. " +
                "Скорее всего, дело не в технике, а в психологии: в партии сложнее, чем " +
                "в упражнении. Стоит подтянуть игры."
    } else {
        "Игры дают ${a.complex2Level}-й уровень, а упражнения — только ${a.complex1Level}-й. " +
                "Стоит усилить тренировку базовых параметров — набора, точности, " +
                "удвоений или утроений."
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(ErrorColor.copy(alpha = 0.10f))
            .padding(16.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("⚠", color = ErrorColor, fontSize = 20.sp)
            Spacer(Modifier.width(8.dp))
            Text(
                if (a.gamesLagBehind) "Игры отстают" else "Упражнения отстают",
                color = ErrorColor,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold
            )
        }
        Spacer(Modifier.height(8.dp))
        Text(
            message,
            color = Color.White.copy(alpha = 0.85f),
            fontSize = 13.sp,
            lineHeight = 19.sp
        )
    }
}

@Composable
private fun ComplexHeader(level: Int, subtitle: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(TileBg)
            .padding(14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                subtitle,
                color = Color.White.copy(alpha = 0.6f),
                fontSize = 12.sp
            )
            Spacer(Modifier.height(4.dp))
            if (level > 0) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        "Уровень $level",
                        color = GoldAccent,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        DartsNorms.getLevelName(level),
                        color = Color.White,
                        fontSize = 14.sp
                    )
                }
            } else {
                Text(
                    "нет данных",
                    color = Color.White.copy(alpha = 0.45f),
                    fontSize = 16.sp
                )
            }
        }
    }
}

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
