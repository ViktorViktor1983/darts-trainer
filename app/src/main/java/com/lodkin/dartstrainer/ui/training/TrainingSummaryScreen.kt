package com.lodkin.dartstrainer.ui.training

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.lodkin.dartstrainer.theme.Accent
import com.lodkin.dartstrainer.theme.ErrorColor
import com.lodkin.dartstrainer.theme.GoldAccent
import com.lodkin.dartstrainer.theme.TileBg
import com.lodkin.dartstrainer.theme.TileBgDark

enum class SummaryStatus { DONE, SKIPPED, NOT_DONE }

enum class SummaryBlockType { WARMUP, MAIN, PSYCHOLOGY, STRENGTH }

data class ExerciseSummary(
    val title: String,
    val minutes: Int,
    val status: SummaryStatus
)

data class BlockExerciseItem(
    val id: String,
    val title: String,
    val isRecommended: Boolean = false
)

/** 8 игр основной части (с рекомендациями наверху). */
fun defaultMainBlockItems(recommendedIds: Set<String>): List<BlockExerciseItem> {
    val all = listOf(
        BlockExerciseItem("aroundclock_doubles", "Кругосветка: Удвоения", recommendedIds.contains("aroundclock_doubles")),
        BlockExerciseItem("aroundclock_trebles", "Кругосветка: Утроения", recommendedIds.contains("aroundclock_trebles")),
        BlockExerciseItem("aroundclock_sectors", "Кругосветка: Сектора", recommendedIds.contains("aroundclock_sectors")),
        BlockExerciseItem("sector20", "Сектор 20", recommendedIds.contains("sector20")),
        BlockExerciseItem("scoreset", "Набор очков", recommendedIds.contains("scoreset")),
        BlockExerciseItem("biground", "Большой раунд", recommendedIds.contains("biground")),
        BlockExerciseItem("game501", "501", recommendedIds.contains("game501")),
        BlockExerciseItem("cricket", "Крикет", recommendedIds.contains("cricket"))
    )
    return all.sortedByDescending { it.isRecommended }
}

/** 4 упражнения разминки. */
fun warmupBlockItems(): List<BlockExerciseItem> = listOf(
    BlockExerciseItem("warmup_muscles", "Мышцы"),
    BlockExerciseItem("warmup_trajectory", "Траектория"),
    BlockExerciseItem("warmup_accuracy", "Кучность"),
    BlockExerciseItem("warmup_aim", "Прицеливание")
)

@Composable
fun TrainingSummaryScreen(
    playerName: String,
    planMinutes: Int,
    actualSeconds: Int,
    exercises: List<ExerciseSummary>,
    blockType: SummaryBlockType,
    blockExercises: List<BlockExerciseItem>,
    isLastBlock: Boolean,
    recommendedIds: Set<String>,
    onContinue: () -> Unit,
    onPlayExercise: (String) -> Unit,
    onPlayGame: (String) -> Unit,
    onFinishTraining: () -> Unit
) {
    val actualMinutes = (actualSeconds / 60).coerceAtLeast(1)
    val doneCount = exercises.count { it.status == SummaryStatus.DONE }
    val skippedCount = exercises.count { it.status == SummaryStatus.SKIPPED }
    val totalCount = exercises.size

    var showCoachWarning by remember { mutableStateOf(false) }
    var showBlockDialog by remember { mutableStateOf(false) }
    var showFinishDialog by remember { mutableStateOf(false) }
    var showGamesDialog by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0D1117))
            .padding(16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .verticalScroll(rememberScrollState())
        ) {
            Spacer(Modifier.height(16.dp))

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(TileBg)
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("🏆", fontSize = 40.sp)
                Spacer(Modifier.height(10.dp))
                Text(
                    "Блок завершён!",
                    color = GoldAccent,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(Modifier.height(6.dp))
                Text(
                    "$playerName, ты хорошо потрудился.",
                    color = Color.White.copy(alpha = 0.8f),
                    fontSize = 14.sp
                )
            }

            Spacer(Modifier.height(16.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(TileBgDark)
                    .padding(16.dp)
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text("Планировал", color = Color.White.copy(alpha = 0.6f), fontSize = 12.sp)
                    Spacer(Modifier.height(4.dp))
                    Text("$planMinutes мин", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                }
                Column(modifier = Modifier.weight(1f), horizontalAlignment = Alignment.End) {
                    Text("В тренировке", color = Color.White.copy(alpha = 0.6f), fontSize = 12.sp)
                    Spacer(Modifier.height(4.dp))
                    Text("$actualMinutes мин", color = Accent, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                }
            }

            Spacer(Modifier.height(20.dp))

            Text(
                "ЧТО СДЕЛАНО",
                color = Accent,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 2.sp
            )
            Spacer(Modifier.height(10.dp))

            exercises.forEach { ex ->
                SummaryRow(ex)
                Spacer(Modifier.height(8.dp))
            }

            Spacer(Modifier.height(16.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(TileBgDark.copy(alpha = 0.6f))
                    .padding(14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "Пройдено: $doneCount из $totalCount",
                    color = Color.White.copy(alpha = 0.85f),
                    fontSize = 13.sp
                )
                if (skippedCount > 0) {
                    Text(
                        "  ·  Пропущено: $skippedCount",
                        color = Color.White.copy(alpha = 0.5f),
                        fontSize = 13.sp
                    )
                }
            }

            Spacer(Modifier.height(24.dp))
        }

        // ── Кнопки ──
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Левая: «Остаться здесь»
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(14.dp))
                    .background(TileBgDark)
                    .clickable {
                        if (isLastBlock) {
                            showBlockDialog = true
                        } else {
                            showCoachWarning = true
                        }
                    }
                    .padding(vertical = 14.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    "Остаться здесь",
                    color = Color.White.copy(alpha = 0.9f),
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Medium
                )
            }

            // Правая: «Продолжить» / «Завершить тренировку»
            val rightLabel = if (isLastBlock) "Завершить тренировку" else "Продолжить тренировку"
            Box(
                modifier = Modifier
                    .weight(1.3f)
                    .clip(RoundedCornerShape(14.dp))
                    .background(Accent)
                    .clickable {
                        if (isLastBlock) {
                            showFinishDialog = true
                        } else {
                            onContinue()
                        }
                    }
                    .padding(vertical = 14.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    rightLabel,
                    color = Color(0xFF121212),
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }

    // ── Совет тренера ──
    if (showCoachWarning) {
        AlertDialog(
            onDismissRequest = { showCoachWarning = false },
            containerColor = TileBgDark,
            title = {
                Text("Совет тренера", color = GoldAccent, fontSize = 20.sp, fontWeight = FontWeight.Bold)
            },
            text = {
                Column {
                    Text(
                        "$playerName, рекомендую сначала завершить программу полностью — " +
                                "так будет больше пользы от тренировки.",
                        color = Color.White.copy(alpha = 0.9f),
                        fontSize = 14.sp,
                        lineHeight = 20.sp
                    )
                    Spacer(Modifier.height(12.dp))
                    Text(
                        "А в самом конце сможешь поиграть в любую игру или выполнить " +
                                "любое упражнение, какое захочешь.",
                        color = Color.White.copy(alpha = 0.75f),
                        fontSize = 13.sp,
                        lineHeight = 19.sp
                    )
                }
            },
            confirmButton = {
                Text(
                    "Продолжить тренировку",
                    color = Color(0xFF121212),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier
                        .clip(RoundedCornerShape(10.dp))
                        .background(Accent)
                        .clickable {
                            showCoachWarning = false
                            onContinue()
                        }
                        .padding(horizontal = 16.dp, vertical = 10.dp)
                )
            },
            dismissButton = {
                Text(
                    "Всё равно остаться",
                    color = Color.White.copy(alpha = 0.75f),
                    fontSize = 14.sp,
                    modifier = Modifier
                        .clip(RoundedCornerShape(10.dp))
                        .clickable {
                            showCoachWarning = false
                            showBlockDialog = true
                        }
                        .padding(horizontal = 14.dp, vertical = 10.dp)
                )
            }
        )
    }

    // ── Остаться в блоке (упражнения текущего блока) ──
    if (showBlockDialog) {
        AlertDialog(
            onDismissRequest = { showBlockDialog = false },
            containerColor = TileBgDark,
            title = {
                Text("Остаться в блоке", color = GoldAccent, fontSize = 20.sp, fontWeight = FontWeight.Bold)
            },
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 400.dp)
                        .verticalScroll(rememberScrollState())
                ) {
                    Text(
                        "Выбери упражнение. Результат пойдёт в тренировочную статистику.",
                        color = Color.White.copy(alpha = 0.7f),
                        fontSize = 13.sp
                    )
                    Spacer(Modifier.height(12.dp))
                    blockExercises.forEach { ex ->
                        BlockItemRow(ex) {
                            showBlockDialog = false
                            onPlayExercise(ex.id)
                        }
                        Spacer(Modifier.height(8.dp))
                    }
                }
            },
            confirmButton = {
                Text(
                    "Закрыть",
                    color = Accent,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier
                        .clip(RoundedCornerShape(10.dp))
                        .background(Accent.copy(alpha = 0.15f))
                        .clickable { showBlockDialog = false }
                        .padding(horizontal = 18.dp, vertical = 10.dp)
                )
            }
        )
    }

    // ── Завершить тренировку (последний блок) ──
    if (showFinishDialog) {
        AlertDialog(
            onDismissRequest = { showFinishDialog = false },
            containerColor = TileBgDark,
            title = {
                Text("Тренировка завершена", color = GoldAccent, fontSize = 20.sp, fontWeight = FontWeight.Bold)
            },
            text = {
                Column {
                    Text(
                        "Что делаем дальше?",
                        color = Color.White.copy(alpha = 0.9f),
                        fontSize = 14.sp
                    )
                    Spacer(Modifier.height(8.dp))
                    Text(
                        "Можно ещё поиграть в любую игру или полностью завершить тренировку.",
                        color = Color.White.copy(alpha = 0.7f),
                        fontSize = 13.sp
                    )
                }
            },
            confirmButton = {
                Text(
                    "Ещё поиграть",
                    color = Color(0xFF121212),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier
                        .clip(RoundedCornerShape(10.dp))
                        .background(Accent)
                        .clickable {
                            showFinishDialog = false
                            showGamesDialog = true
                        }
                        .padding(horizontal = 16.dp, vertical = 10.dp)
                )
            },
            dismissButton = {
                Text(
                    "Полностью закончить",
                    color = Color.White.copy(alpha = 0.75f),
                    fontSize = 14.sp,
                    modifier = Modifier
                        .clip(RoundedCornerShape(10.dp))
                        .clickable {
                            showFinishDialog = false
                            onFinishTraining()
                        }
                        .padding(horizontal = 14.dp, vertical = 10.dp)
                )
            }
        )
    }

    // ── Список игр для «Ещё поиграть» ──
    if (showGamesDialog) {
        val games = defaultMainBlockItems(recommendedIds)
        AlertDialog(
            onDismissRequest = { showGamesDialog = false },
            containerColor = TileBgDark,
            title = {
                Text("Во что сыграем?", color = GoldAccent, fontSize = 20.sp, fontWeight = FontWeight.Bold)
            },
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 400.dp)
                        .verticalScroll(rememberScrollState())
                ) {
                    games.forEach { g ->
                        BlockItemRow(g) {
                            showGamesDialog = false
                            onPlayGame(g.id)
                        }
                        Spacer(Modifier.height(8.dp))
                    }
                }
            },
            confirmButton = {
                Text(
                    "Закрыть",
                    color = Accent,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier
                        .clip(RoundedCornerShape(10.dp))
                        .background(Accent.copy(alpha = 0.15f))
                        .clickable { showGamesDialog = false }
                        .padding(horizontal = 18.dp, vertical = 10.dp)
                )
            }
        )
    }
}

@Composable
private fun BlockItemRow(item: BlockExerciseItem, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(if (item.isRecommended) Accent.copy(alpha = 0.12f) else TileBg)
            .clickable { onClick() }
            .padding(horizontal = 14.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            item.title,
            color = Color.White,
            fontSize = 14.sp,
            fontWeight = if (item.isRecommended) FontWeight.Bold else FontWeight.Medium,
            modifier = Modifier.weight(1f)
        )
        if (item.isRecommended) {
            Column(horizontalAlignment = Alignment.End) {
                Text("★", color = GoldAccent, fontSize = 16.sp)
                Text("рекомендуем", color = GoldAccent.copy(alpha = 0.85f), fontSize = 10.sp)
            }
        } else {
            Text("→", color = Accent, fontSize = 16.sp, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun SummaryRow(exercise: ExerciseSummary) {
    val (icon, iconColor, bg) = when (exercise.status) {
        SummaryStatus.DONE -> Triple("✓", Accent, TileBgDark)
        SummaryStatus.SKIPPED -> Triple("–", Color.White.copy(alpha = 0.4f), TileBgDark.copy(alpha = 0.6f))
        SummaryStatus.NOT_DONE -> Triple("·", ErrorColor.copy(alpha = 0.6f), TileBgDark.copy(alpha = 0.6f))
    }
    val titleColor = when (exercise.status) {
        SummaryStatus.DONE -> Color.White
        SummaryStatus.SKIPPED -> Color.White.copy(alpha = 0.5f)
        SummaryStatus.NOT_DONE -> Color.White.copy(alpha = 0.5f)
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(bg)
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(28.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(if (exercise.status == SummaryStatus.DONE) Accent.copy(alpha = 0.2f) else TileBg),
            contentAlignment = Alignment.Center
        ) {
            Text(icon, color = iconColor, fontSize = 16.sp, fontWeight = FontWeight.Bold)
        }
        Spacer(Modifier.width(12.dp))
        Text(
            exercise.title,
            color = titleColor,
            fontSize = 14.sp,
            fontWeight = if (exercise.status == SummaryStatus.DONE) FontWeight.Medium else FontWeight.Normal,
            modifier = Modifier.weight(1f)
        )
        Text(
            "~${exercise.minutes} мин",
            color = if (exercise.status == SummaryStatus.DONE) Accent else Color.White.copy(alpha = 0.4f),
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold
        )
    }
}
