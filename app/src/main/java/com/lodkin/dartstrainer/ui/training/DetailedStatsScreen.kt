package com.lodkin.dartstrainer.ui.training

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
import com.lodkin.dartstrainer.data.game501.Game501Entity
import com.lodkin.dartstrainer.data.game501.Game501Repository
import com.lodkin.dartstrainer.data.training.MonthlyDartsStat
import com.lodkin.dartstrainer.data.training.TrainingRepository
import com.lodkin.dartstrainer.data.training.TrainingStatsAnalyzer
import com.lodkin.dartstrainer.data.training.TrainingUnfinishedStorage
import com.lodkin.dartstrainer.theme.Accent
import com.lodkin.dartstrainer.theme.DarkBg
import com.lodkin.dartstrainer.theme.ErrorColor
import com.lodkin.dartstrainer.theme.GoldAccent
import com.lodkin.dartstrainer.theme.TileBg
import com.lodkin.dartstrainer.theme.TileBgDark
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Экран «Подробная статистика».
 *
 * Хаб для просмотра детальной аналитики по всем дисциплинам.
 */
@Composable
fun DetailedStatsScreen(
    repository: TrainingRepository,
    game501Repository: Game501Repository,
    cricketRepository: CricketRepository,
    onBack: () -> Unit,
    onOpenDiscipline: (String) -> Unit,
    onResumeGame501: (Game501Entity) -> Unit = {},
    onResumeCricket: (CricketGameEntity) -> Unit = {}
) {
    val scope = rememberCoroutineScope()
    val context = androidx.compose.ui.platform.LocalContext.current
    
    var analysis by remember { mutableStateOf<TrainingStatsAnalyzer.TrainingAnalysis?>(null) }
    var loading by remember { mutableStateOf(true) }

    var totalDarts by remember { mutableStateOf(0) }
    var dartsThisWeek by remember { mutableStateOf(0) }
    var dartsByMonth by remember { mutableStateOf<List<MonthlyDartsStat>>(emptyList()) }

    var showProgressDialog by remember { mutableStateOf(false) }
    var showDartsDialog by remember { mutableStateOf(false) }

    // ── Незавершённые партии ──
    var unfinished501 by remember { mutableStateOf<List<Game501Entity>>(emptyList()) }
    var unfinishedCricket by remember { mutableStateOf<List<CricketGameEntity>>(emptyList()) }
    var showUnfinishedDialog501 by remember { mutableStateOf(false) }
    var showUnfinishedDialogCricket by remember { mutableStateOf(false) }
    var reloadKey by remember { mutableStateOf(0) }

    LaunchedEffect(reloadKey) {
        val a = withContext(Dispatchers.IO) {
            TrainingStatsAnalyzer.analyze(repository)
        }
        val td = withContext(Dispatchers.IO) { repository.getTotalDartsThrown() }
        val dw = withContext(Dispatchers.IO) { repository.getDartsThrownThisWeek() }
        val dm = withContext(Dispatchers.IO) { repository.getDartsByMonth() }

        analysis = a
        totalDarts = td
        dartsThisWeek = dw
        dartsByMonth = dm

        // Загружаем незавершённые партии
        val ids501 = TrainingUnfinishedStorage.getUnfinished501(context)
        val allUnfinished501 = withContext(Dispatchers.IO) { game501Repository.getUnfinishedGames() }
        unfinished501 = allUnfinished501.filter { ids501.contains(it.id) }

        val idsCricket = TrainingUnfinishedStorage.getUnfinishedCricket(context)
        val allUnfinishedCricket = withContext(Dispatchers.IO) { cricketRepository.getUnfinishedGames() }
        unfinishedCricket = allUnfinishedCricket.filter { idsCricket.contains(it.id) }

        loading = false
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBg)
            .padding(16.dp)
    ) {
        // ── ШАПКА ──
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
                "Подробная статистика",
                color = GoldAccent,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.weight(1f)
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

        val a = analysis ?: return@Column

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .verticalScroll(rememberScrollState())
        ) {
            // ── БЛОК НЕЗАВЕРШЁННЫХ ПАРТИЙ ──
            if (unfinished501.isNotEmpty() || unfinishedCricket.isNotEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xFF3A1A1A))
                        .clickable {
                            if (unfinished501.isNotEmpty()) showUnfinishedDialog501 = true
                            else showUnfinishedDialogCricket = true
                        }
                        .padding(14.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            "⏸ Незавершённые партии (${unfinished501.size + unfinishedCricket.size})",
                            color = ErrorColor,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text("▶", color = ErrorColor, fontSize = 14.sp)
                    }
                }
                Spacer(Modifier.height(12.dp))
            }

            // ── БЛОК ЭКЗАМЕНА ──
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(TileBg)
                    .clickable { showProgressDialog = true }
                    .padding(14.dp)
            ) {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (a.isReadyForNormatives) "Пора сдавать экзамен!" else "Прогресс до экзамена",
                            color = if (a.isReadyForNormatives) GoldAccent else Color.White,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "${(a.overallProgress * 100).toInt()}%",
                            color = GoldAccent,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Spacer(Modifier.height(8.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(10.dp)
                            .clip(RoundedCornerShape(5.dp))
                            .background(Color.Black.copy(alpha = 0.3f))
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth(a.overallProgress)
                                .fillMaxHeight()
                                .clip(RoundedCornerShape(5.dp))
                                .background(if (a.isReadyForNormatives) GoldAccent else Accent)
                        )
                    }
                }
            }

            Spacer(Modifier.height(12.dp))

            // ── БЛОК ИНТЕНСИВНОСТИ ──
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(TileBg)
                    .clickable { showDartsDialog = true }
                    .padding(14.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "🎯 На этой неделе: $dartsThisWeek дротиков",
                        color = Color.White,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium
                    )
                    Text("▶", color = Accent, fontSize = 12.sp)
                }
            }

            Spacer(Modifier.height(24.dp))

            Text(
                "ВЫБЕРИ ДИСЦИПЛИНУ",
                color = Accent,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 2.sp
            )
            Spacer(Modifier.height(10.dp))

            // ── СПИСОК ДИСЦИПЛИН ──
            val disciplines = listOf(
                "game501" to "501",
                "cricket" to "Крикет",
                "sector20" to "Сектор 20",
                "biground" to "Большой раунд",
                "scoreset" to "Набор очков",
                "aroundclock_sectors" to "Кругосветка: Сектора",
                "aroundclock_doubles" to "Кругосветка: Удвоения",
                "aroundclock_trebles" to "Кругосветка: Утроения"
            )

            disciplines.forEach { (id, title) ->
                // Получаем краткую сводку для карточки
                val summary = when (id) {
                    "game501" -> a.avg501Ppr?.let { "Ср. PPR: ${String.format(Locale.US, "%.1f", it)} · ${a.count501Matches} партий" }
                    "cricket" -> a.avgCricketMpr?.let { "Ср. MPR: ${String.format(Locale.US, "%.1f", it)} · ${a.countCricketMatches} партий" }
                    else -> "Нажми для подробностей"
                }

                // Считаем количество проблем для Кругосветок
                val problemsCount = when (id) {
                    "aroundclock_sectors" -> if (a.sectorsDataAvailable) a.worstSectors.size else 0
                    "aroundclock_doubles" -> if (a.doublesDataAvailable) a.worstDoubles.size else 0
                    "aroundclock_trebles" -> if (a.treblesDataAvailable) a.worstTrebles.size else 0
                    else -> 0
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(TileBgDark)
                        .clickable { onOpenDiscipline(id) }
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(title, color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                            
                            // Красный счетчик проблем
                            if (problemsCount > 0) {
                                Spacer(Modifier.width(8.dp))
                                val word = when (problemsCount) {
                                    1 -> "проблема"
                                    2, 3, 4 -> "проблемы"
                                    else -> "проблем"
                                }
                                Text(
                                    text = "$problemsCount $word",
                                    color = Color(0xFFE57373), // Мягкий красный
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                        if (summary != null) {
                            Spacer(Modifier.height(4.dp))
                            Text(summary, color = Color.White.copy(alpha = 0.5f), fontSize = 12.sp)
                        }
                    }
                    Text("▶", color = Accent, fontSize = 14.sp)
                }
            }
            Spacer(Modifier.height(20.dp))
        }
    }

    if (showProgressDialog) {
        ExamProgressDialog(
            overallProgress = analysis?.overallProgress ?: 0f,
            exercises = analysis?.exercisesProgress ?: emptyList(),
            onPlayExercise = { /* В этом экране мы просто смотрим, запуск пока убран */ },
            onDismiss = { showProgressDialog = false }
        )
    }

    if (showDartsDialog) {
        DartsIntensityDialog(
            totalDarts = totalDarts,
            dartsThisWeek = dartsThisWeek,
            monthlyStats = dartsByMonth,
            onDismiss = { showDartsDialog = false }
        )
    }

    // ── ДИАЛОГИ НЕЗАВЕРШЁННЫХ ПАРТИЙ ──
    if (showUnfinishedDialog501) {
        UnfinishedDialog501(
            games = unfinished501,
            onResume = { entity ->
                showUnfinishedDialog501 = false
                onResumeGame501(entity)
            },
            onDelete = { entity ->
                scope.launch {
                    game501Repository.deleteGame(entity.id)
                    TrainingUnfinishedStorage.removeUnfinished501(context, entity.id)
                    reloadKey++
                }
            },
            onDismiss = { showUnfinishedDialog501 = false }
        )
    }

    if (showUnfinishedDialogCricket) {
        UnfinishedDialogCricket(
            games = unfinishedCricket,
            onResume = { entity ->
                showUnfinishedDialogCricket = false
                onResumeCricket(entity)
            },
            onDelete = { entity ->
                scope.launch {
                    cricketRepository.deleteGame(entity.id)
                    TrainingUnfinishedStorage.removeUnfinishedCricket(context, entity.id)
                    reloadKey++
                }
            },
            onDismiss = { showUnfinishedDialogCricket = false }
        )
    }
}

// ─────────────────────────────────────────────
// ДИАЛОГИ (скопированы из MainMenuScreen)
// ─────────────────────────────────────────────

@Composable
private fun ExamProgressDialog(
    overallProgress: Float,
    exercises: List<TrainingStatsAnalyzer.ExerciseProgress>,
    onPlayExercise: (String) -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = TileBgDark,
        title = {
            Text(
                "Прогресс до экзамена",
                color = GoldAccent,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Общий прогресс", color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                    Text("${(overallProgress * 100).toInt()}%", color = GoldAccent, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                }
                Spacer(Modifier.height(6.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(10.dp)
                        .clip(RoundedCornerShape(5.dp))
                        .background(Color.Black.copy(alpha = 0.3f))
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(overallProgress)
                            .fillMaxHeight()
                            .clip(RoundedCornerShape(5.dp))
                            .background(if (overallProgress >= 1f) GoldAccent else Accent)
                    )
                }

                Spacer(Modifier.height(20.dp))
                Text("ДЕТАЛИ ПО УПРАЖНЕНИЯМ", color = Accent, fontSize = 11.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
                Spacer(Modifier.height(10.dp))

                exercises.forEach { ex ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(ex.title, color = Color.White.copy(alpha = 0.9f), fontSize = 13.sp, modifier = Modifier.weight(1f))
                        Text(
                            "${(ex.percent * 100).toInt()}%",
                            color = if (ex.percent >= 1f) GoldAccent else Accent,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.width(40.dp),
                            textAlign = TextAlign.End
                        )
                    }
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(6.dp)
                            .clip(RoundedCornerShape(3.dp))
                            .background(Color.Black.copy(alpha = 0.3f))
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth(ex.percent)
                                .fillMaxHeight()
                                .clip(RoundedCornerShape(3.dp))
                                .background(if (ex.percent >= 1f) GoldAccent else Accent)
                        )
                    }
                    Spacer(Modifier.height(8.dp))
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("ЗАКРЫТЬ", color = Accent, fontWeight = FontWeight.Bold)
            }
        }
    )
}

@Composable
private fun DartsIntensityDialog(
    totalDarts: Int,
    dartsThisWeek: Int,
    monthlyStats: List<MonthlyDartsStat>,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = TileBgDark,
        title = {
            Text(
                "Интенсивность тренировок",
                color = GoldAccent,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Всего брошено:", color = Color.White, fontSize = 14.sp)
                    Text("$totalDarts", color = GoldAccent, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                }
                Spacer(Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("За эту неделю:", color = Color.White, fontSize = 14.sp)
                    Text("$dartsThisWeek", color = GoldAccent, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                }

                Spacer(Modifier.height(20.dp))
                Text("РАЗБИВКА ПО МЕСЯЦАМ", color = Accent, fontSize = 11.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
                Spacer(Modifier.height(10.dp))

                if (monthlyStats.isEmpty()) {
                    Text("Пока нет данных. Сыграй первую тренировку!", color = Color.White.copy(alpha = 0.6f), fontSize = 13.sp)
                } else {
                    monthlyStats.forEach { stat ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(formatMonth(stat.month), color = Color.White.copy(alpha = 0.9f), fontSize = 14.sp)
                            Text("${stat.darts}", color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("ЗАКРЫТЬ", color = Accent, fontWeight = FontWeight.Bold)
            }
        }
    )
}

private fun formatMonth(monthStr: String): String {
    return try {
        val parts = monthStr.split("-")
        if (parts.size == 2) {
            val year = parts[0]
            val month = parts[1].toInt()
            val months = arrayOf("Январь", "Февраль", "Март", "Апрель", "Май", "Июнь", "Июль", "Август", "Сентябрь", "Октябрь", "Ноябрь", "Декабрь")
            "${months[month - 1]} $year"
        } else {
            monthStr
        }
    } catch (e: Exception) {
        monthStr
    }
}

// ─────────────────────────────────────────────
// ДИАЛОГИ НЕЗАВЕРШЁННЫХ ПАРТИЙ (скопированы из StatsMenuScreen)
// ─────────────────────────────────────────────

@Composable
private fun UnfinishedDialog501(
    games: List<Game501Entity>,
    onResume: (Game501Entity) -> Unit,
    onDelete: (Game501Entity) -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(onClick = onDismiss) { Text("Отложить", color = Accent) }
        },
        title = {
            Column {
                Text(
                    "Незавершённые партии 501",
                    color = Color.White,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    if (games.size == 1) "Можно продолжить или удалить."
                    else "Найдено ${games.size} партий.",
                    color = Color.White.copy(alpha = 0.6f),
                    fontSize = 12.sp
                )
            }
        },
        text = {
            if (games.isEmpty()) {
                Text(
                    "Пока нет незавершённых партий.",
                    color = Color.White.copy(alpha = 0.7f),
                    fontSize = 14.sp
                )
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 420.dp)
                        .verticalScroll(rememberScrollState())
                ) {
                    games.forEach { entity ->
                        UnfinishedCard501(
                            entity = entity,
                            onResume = { onResume(entity) },
                            onDelete = { onDelete(entity) }
                        )
                        Spacer(Modifier.height(8.dp))
                    }
                }
            }
        }
    )
}

@Composable
private fun UnfinishedCard501(
    entity: Game501Entity,
    onResume: () -> Unit,
    onDelete: () -> Unit
) {
    val names = entity.playerNames.split("|")
    val scores = entity.matchScore.split("|")
    val dateStr = formatDate(entity.lastUpdateMillis.takeIf { it > 0 } ?: entity.dateMillis)

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(Color(0xFF3A1A1A))
            .padding(12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                "⏸ НЕЗАВЕРШЕНА",
                color = ErrorColor,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 2.sp,
                modifier = Modifier.weight(1f)
            )
            Text(
                dateStr,
                color = Color.White.copy(alpha = 0.6f),
                fontSize = 11.sp
            )
        }
        Spacer(Modifier.height(6.dp))
        Text(
            names.joinToString("  ·  "),
            color = Color.White,
            fontSize = 13.sp,
            fontWeight = FontWeight.Medium
        )
        Spacer(Modifier.height(2.dp))
        Text(
            buildString {
                append("Легов: ${entity.legsPlayed}")
                if (scores.size == names.size && scores.isNotEmpty()) {
                    append("   ·   очков: ")
                    append(scores.joinToString(" / "))
                }
            },
            color = Color.White.copy(alpha = 0.7f),
            fontSize = 11.sp
        )
        Spacer(Modifier.height(10.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(8.dp))
                    .background(Accent)
                    .clickable { onResume() }
                    .padding(vertical = 10.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    "Продолжить",
                    color = Color(0xFF121212),
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold
                )
            }
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(8.dp))
                    .background(TileBgDark)
                    .clickable { onDelete() }
                    .padding(vertical = 10.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    "Удалить",
                    color = ErrorColor,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
private fun UnfinishedDialogCricket(
    games: List<CricketGameEntity>,
    onResume: (CricketGameEntity) -> Unit,
    onDelete: (CricketGameEntity) -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(onClick = onDismiss) { Text("Отложить", color = Accent) }
        },
        title = {
            Column {
                Text(
                    "Незавершённые партии в крикет",
                    color = Color.White,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    if (games.size == 1) "Можно продолжить или удалить."
                    else "Найдено ${games.size} партий.",
                    color = Color.White.copy(alpha = 0.6f),
                    fontSize = 12.sp
                )
            }
        },
        text = {
            if (games.isEmpty()) {
                Text(
                    "Пока нет незавершённых партий.",
                    color = Color.White.copy(alpha = 0.7f),
                    fontSize = 14.sp
                )
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 420.dp)
                        .verticalScroll(rememberScrollState())
                ) {
                    games.forEach { entity ->
                        UnfinishedCardCricket(
                            entity = entity,
                            onResume = { onResume(entity) },
                            onDelete = { onDelete(entity) }
                        )
                        Spacer(Modifier.height(8.dp))
                    }
                }
            }
        }
    )
}

@Composable
private fun UnfinishedCardCricket(
    entity: CricketGameEntity,
    onResume: () -> Unit,
    onDelete: () -> Unit
) {
    val names = entity.playerNames.split("|")
    val mprs = entity.mpr.split("|")
    val dateStr = formatDate(entity.lastUpdateMillis.takeIf { it > 0 } ?: entity.dateMillis)
    val mode = if (entity.cricketType == "AMERICAN") "Американский" else "Без набора"

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(Color(0xFF3A1A1A))
            .padding(12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                "⏸ НЕЗАВЕРШЕНА",
                color = ErrorColor,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 2.sp,
                modifier = Modifier.weight(1f)
            )
            Text(
                dateStr,
                color = Color.White.copy(alpha = 0.6f),
                fontSize = 11.sp
            )
        }
        Spacer(Modifier.height(6.dp))
        Text(
            names.joinToString("  ·  "),
            color = Color.White,
            fontSize = 13.sp,
            fontWeight = FontWeight.Medium
        )
        Spacer(Modifier.height(2.dp))
        Text(
            buildString {
                append(mode)
                append("   ·   MPR: ")
                append(mprs.joinToString(" / ") { "%.2f".format(Locale.US, it.toDoubleOrNull() ?: 0.0) })
            },
            color = Color.White.copy(alpha = 0.7f),
            fontSize = 11.sp
        )
        Spacer(Modifier.height(10.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(8.dp))
                    .background(Accent)
                    .clickable { onResume() }
                    .padding(vertical = 10.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    "Продолжить",
                    color = Color(0xFF121212),
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold
                )
            }
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(8.dp))
                    .background(TileBgDark)
                    .clickable { onDelete() }
                    .padding(vertical = 10.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    "Удалить",
                    color = ErrorColor,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

private fun formatDate(millis: Long): String {
    val fmt = SimpleDateFormat("dd.MM HH:mm", Locale.US)
    return fmt.format(Date(millis))
}
