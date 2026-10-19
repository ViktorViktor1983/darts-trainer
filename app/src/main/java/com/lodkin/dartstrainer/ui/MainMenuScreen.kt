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
import com.lodkin.dartstrainer.data.training.TrainingDatabase
import com.lodkin.dartstrainer.data.training.TrainingRepository
import com.lodkin.dartstrainer.data.training.TrainingStatsAnalyzer
import com.lodkin.dartstrainer.theme.Accent
import com.lodkin.dartstrainer.theme.GoldAccent
import com.lodkin.dartstrainer.theme.TileBg
import com.lodkin.dartstrainer.theme.TileBgDark
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.Locale

/**
 * Главное меню.
 *
 * ВАЖНО: этот экран сам читает статистику 501 из тренировочной БД
 * и показывает средний PPR + % удвоений на карточке «Тренировка».
 * MainActivity его трогать не нужно.
 *
 * Также здесь показывается общая шкала прогресса до экзамена (0–100%).
 * При нажатии на неё открывается окно с детализацией по всем 8 упражнениям.
 */
@Composable
fun MainMenuScreen(
    onTraining: () -> Unit,
    onFreePlay: () -> Unit,
    onStatsClick: () -> Unit,
    onSettingsClick: () -> Unit
) {
    val context = LocalContext.current
    val repository = remember {
        TrainingRepository(TrainingDatabase.getInstance(context))
    }

    var avgPpr by remember { mutableStateOf<Float?>(null) }
    var avgDbl by remember { mutableStateOf<Float?>(null) }
    var count501 by remember { mutableStateOf(0) }

    var overallProgress by remember { mutableStateOf(0f) }
    var exercisesProgress by remember { mutableStateOf<List<TrainingStatsAnalyzer.ExerciseProgress>>(emptyList()) }
    var isReadyForNormatives by remember { mutableStateOf(false) }

    var showProgressDialog by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        val analysis = withContext(Dispatchers.IO) {
            TrainingStatsAnalyzer.analyze(repository)
        }
        avgPpr = analysis.avg501Ppr
        avgDbl = analysis.avg501DoublesPercent
        count501 = analysis.count501Matches
        overallProgress = analysis.overallProgress
        exercisesProgress = analysis.exercisesProgress
        isReadyForNormatives = analysis.isReadyForNormatives
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Заголовок с шестерёнкой справа
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Spacer(Modifier.size(48.dp))

            Text(
                text = "Darts Trainer",
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = GoldAccent,
                letterSpacing = 2.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier.weight(1f)
            )

            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(TileBgDark)
                    .clickable { onSettingsClick() },
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "⚙",
                    fontSize = 24.sp,
                    color = Accent
                )
            }
        }

        Spacer(Modifier.height(20.dp))

        // Тренировка
        TrainingCard(
            modifier = Modifier
                .fillMaxWidth()
                .weight(50f),
            avg501Ppr = avgPpr,
            avg501DoublesPercent = avgDbl,
            count501Matches = count501,
            overallProgress = overallProgress,
            isReadyForNormatives = isReadyForNormatives,
            onProgressClick = { showProgressDialog = true },
            onClick = onTraining
        )

        Spacer(Modifier.height(12.dp))

        // Просто поиграть с кнопкой статистики в правом нижнем углу
        FreePlayCard(
            modifier = Modifier
                .fillMaxWidth()
                .weight(50f),
            onFreePlay = onFreePlay,
            onStatsClick = onStatsClick
        )
    }

    if (showProgressDialog) {
        ExamProgressDialog(
            overallProgress = overallProgress,
            exercises = exercisesProgress,
            onDismiss = { showProgressDialog = false }
        )
    }
}

/**
 * Карточка «Тренировка».
 * Сверху — заголовок. Снизу — плашка с PPR/удвоениями и шкала прогресса.
 */
@Composable
private fun TrainingCard(
    modifier: Modifier,
    avg501Ppr: Float?,
    avg501DoublesPercent: Float?,
    count501Matches: Int,
    overallProgress: Float,
    isReadyForNormatives: Boolean,
    onProgressClick: () -> Unit,
    onClick: () -> Unit
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(20.dp))
            .background(TileBg)
            .clickable { onClick() }
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "Тренировка",
                fontSize = 26.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                textAlign = TextAlign.Center
            )
            Text(
                text = "По программе тренера",
                fontSize = 14.sp,
                color = Accent,
                textAlign = TextAlign.Center
            )

            if (avg501Ppr != null && count501Matches > 0) {
                Spacer(Modifier.height(14.dp))
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(TileBgDark)
                        .padding(horizontal = 18.dp, vertical = 8.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                "PPR",
                                color = Color.White.copy(alpha = 0.55f),
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.sp
                            )
                            Spacer(Modifier.height(2.dp))
                            Text(
                                String.format(Locale.US, "%.1f", avg501Ppr),
                                color = GoldAccent,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Spacer(Modifier.width(20.dp))

                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                "УДВОЕНИЯ",
                                color = Color.White.copy(alpha = 0.55f),
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.sp
                            )
                            Spacer(Modifier.height(2.dp))
                            Text(
                                if (avg501DoublesPercent != null)
                                    "${avg501DoublesPercent.toInt()}%"
                                else "—",
                                color = GoldAccent,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
                Spacer(Modifier.height(4.dp))
                Text(
                    "$count501Matches ${plural501Matches(count501Matches)}",
                    color = Color.White.copy(alpha = 0.4f),
                    fontSize = 10.sp
                )
            }

            // ── ШКАЛА ПРОГРЕССА ДО ЭКЗАМЕНА ──
            Spacer(Modifier.height(20.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth(0.8f)
                    .clip(RoundedCornerShape(8.dp))
                    .background(TileBgDark)
                    .clickable { onProgressClick() }
                    .padding(12.dp)
            ) {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (isReadyForNormatives) "Пора сдавать экзамен!" else "До экзамена",
                            color = if (isReadyForNormatives) GoldAccent else Color.White,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "${(overallProgress * 100).toInt()}%",
                            color = GoldAccent,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Spacer(Modifier.height(8.dp))
                    // Полоска прогресса
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp)
                            .clip(RoundedCornerShape(4.dp))
                            .background(Color.Black.copy(alpha = 0.3f))
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth(overallProgress)
                                .fillMaxHeight()
                                .clip(RoundedCornerShape(4.dp))
                                .background(if (isReadyForNormatives) GoldAccent else Accent)
                        )
                    }
                }
            }
        }
    }
}

/**
 * Всплывающее окно с детализацией прогресса по всем 8 упражнениям.
 */
@Composable
private fun ExamProgressDialog(
    overallProgress: Float,
    exercises: List<TrainingStatsAnalyzer.ExerciseProgress>,
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
                // Общая шкала
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "Общий прогресс",
                        color = Color.White,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        "${(overallProgress * 100).toInt()}%",
                        color = GoldAccent,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
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
                Text(
                    "ДЕТАЛИ ПО УПРАЖНЕНИЯМ",
                    color = Accent,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
                Spacer(Modifier.height(10.dp))

                // Список всех 8 упражнений
                exercises.forEach { ex ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            ex.title,
                            color = Color.White.copy(alpha = 0.9f),
                            fontSize = 13.sp,
                            modifier = Modifier.weight(1f)
                        )
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

private fun plural501Matches(n: Int): String {
    val mod10 = n % 10
    val mod100 = n % 100
    return when {
        mod100 in 11..19 -> "партий"
        mod10 == 1 -> "партия"
        mod10 in 2..4 -> "партии"
        else -> "партий"
    }
}

@Composable
private fun FreePlayCard(
    modifier: Modifier,
    onFreePlay: () -> Unit,
    onStatsClick: () -> Unit
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(20.dp))
            .background(TileBg)
            .clickable { onFreePlay() }
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "Просто поиграть",
                fontSize = 26.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                textAlign = TextAlign.Center
            )
            Text(
                text = "Свободная игра",
                fontSize = 14.sp,
                color = Accent,
                textAlign = TextAlign.Center
            )
        }

        Box(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(10.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(TileBgDark)
                .clickable { onStatsClick() }
                .padding(horizontal = 12.dp, vertical = 14.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("📊", fontSize = 22.sp)
                Spacer(Modifier.height(2.dp))
                Text(
                    "Статистика",
                    color = Accent,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(Modifier.height(2.dp))
                Text(
                    "матчи · прогресс · достижения",
                    color = Color.White.copy(alpha = 0.55f),
                    fontSize = 9.sp,
                    textAlign = TextAlign.Center,
                    lineHeight = 11.sp
                )
            }
        }
    }
}
