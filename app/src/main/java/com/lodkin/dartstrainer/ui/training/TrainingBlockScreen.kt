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
import com.lodkin.dartstrainer.theme.GoldAccent
import com.lodkin.dartstrainer.theme.TileBg
import com.lodkin.dartstrainer.theme.TileBgDark

/**
 * Одно упражнение в основной части тренировки.
 */
data class PlannedExercise(
    val id: String,
    val title: String,
    val minutes: Int,
    val whyImportant: String   // зачем это упражнение — показываем при попытке пропуска
)

/**
 * Список упражнений основной части по умолчанию.
 *
 * Пока хардкод. Позже будем брать из TrainingPlanner по слабым местам игрока.
 */
fun defaultMainExercises(): List<PlannedExercise> = listOf(
    PlannedExercise(
        id = "aroundclock_doubles",
        title = "Раунды на удвоения",
        minutes = 25,
        whyImportant = "Это упражнение тренирует закрытие лега. В игре именно удвоения " +
                "решают, кто победит — даже если ты хорошо набираешь, без закрытия победы не будет."
    ),
    PlannedExercise(
        id = "sector20",
        title = "Сектор 20",
        minutes = 15,
        whyImportant = "Это упражнение тренирует стабильный набор очков. В 501 важно " +
                "быстро и уверенно идти от старта к удобному чекауту."
    ),
    PlannedExercise(
        id = "scoreset",
        title = "Набор очков",
        minutes = 10,
        whyImportant = "Это упражнение показывает твой реальный средний набор. " +
                "По нему мы видим прогресс и подбираем программу."
    )
)

/**
 * Экран «Основная часть».
 *
 * Показывает список упражнений с прогрессом. Игрок проходит их по порядку.
 * Пропустить упражнение можно — но с подтверждением и объяснением, зачем оно.
 */
@Composable
fun TrainingBlockScreen(
    exercises: List<PlannedExercise>,
    currentIndex: Int,
    onStartCurrent: () -> Unit,
    onSkipCurrent: () -> Unit,
    onBack: () -> Unit
) {
    var showSkipDialog by remember { mutableStateOf(false) }

    val current = exercises.getOrNull(currentIndex)
    val total = exercises.size

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
                "Основная часть",
                color = GoldAccent,
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(Modifier.height(8.dp))

        Text(
            "Упражнение ${currentIndex + 1} из $total",
            color = Color.White.copy(alpha = 0.6f),
            fontSize = 13.sp
        )

        Spacer(Modifier.height(16.dp))

        // ── Карточка текущего упражнения ──
        if (current != null) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(TileBg)
                    .padding(20.dp)
            ) {
                Text(
                    "СЕЙЧАС",
                    color = Accent,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 2.sp
                )
                Spacer(Modifier.height(10.dp))
                Text(
                    current.title,
                    color = GoldAccent,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(Modifier.height(6.dp))
                Text(
                    "~${current.minutes} минут",
                    color = Color.White.copy(alpha = 0.7f),
                    fontSize = 14.sp
                )
            }

            Spacer(Modifier.height(16.dp))

            // ── Кнопка «Начать упражнение» ──
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(Accent)
                    .clickable { onStartCurrent() }
                    .padding(vertical = 16.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    "▶  Начать упражнение",
                    color = Color(0xFF121212),
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(Modifier.height(10.dp))

            // ── Кнопка «Пропустить» ──
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(TileBgDark)
                    .clickable { showSkipDialog = true }
                    .padding(vertical = 14.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    "Пропустить упражнение",
                    color = Color.White.copy(alpha = 0.75f),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }

        Spacer(Modifier.height(20.dp))

        // ── Список всех упражнений ──
        Text(
            "ЧТО ВПЕРЕДИ",
            color = Accent,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 2.sp
        )
        Spacer(Modifier.height(10.dp))

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .verticalScroll(rememberScrollState())
        ) {
            exercises.forEachIndexed { index, ex ->
                val status = when {
                    index < currentIndex -> ExerciseStatus.DONE
                    index == currentIndex -> ExerciseStatus.CURRENT
                    else -> ExerciseStatus.PENDING
                }
                ExerciseRow(index + 1, ex, status)
                Spacer(Modifier.height(8.dp))
            }
        }
    }

    // ── Диалог «Пропустить?» ──
    if (showSkipDialog && current != null) {
        AlertDialog(
            onDismissRequest = { showSkipDialog = false },
            containerColor = TileBgDark,
            title = {
                Text(
                    "Пропустить «${current.title}»?",
                    color = GoldAccent,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column {
                    Text(
                        current.whyImportant,
                        color = Color.White.copy(alpha = 0.85f),
                        fontSize = 14.sp,
                        lineHeight = 20.sp
                    )
                    Spacer(Modifier.height(14.dp))
                    Text(
                        "Всё равно пропустить?",
                        color = Color.White.copy(alpha = 0.6f),
                        fontSize = 13.sp
                    )
                }
            },
            confirmButton = {
                Text(
                    "Пропустить",
                    color = Color(0xFFFF8A8A),
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier
                        .clip(RoundedCornerShape(10.dp))
                        .background(Color(0x22FF8A8A))
                        .clickable {
                            showSkipDialog = false
                            onSkipCurrent()
                        }
                        .padding(horizontal = 18.dp, vertical = 10.dp)
                )
            },
            dismissButton = {
                Text(
                    "Не пропускать",
                    color = Accent,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier
                        .clip(RoundedCornerShape(10.dp))
                        .clickable { showSkipDialog = false }
                        .padding(horizontal = 18.dp, vertical = 10.dp)
                )
            }
        )
    }
}

// ─────────────────────────────────────────────
// Статус упражнения
// ─────────────────────────────────────────────

private enum class ExerciseStatus { DONE, CURRENT, PENDING }

// ─────────────────────────────────────────────
// Строка упражнения в списке
// ─────────────────────────────────────────────

@Composable
private fun ExerciseRow(
    number: Int,
    exercise: PlannedExercise,
    status: ExerciseStatus
) {
    val bg = when (status) {
        ExerciseStatus.CURRENT -> Accent.copy(alpha = 0.15f)
        ExerciseStatus.DONE -> TileBgDark.copy(alpha = 0.5f)
        ExerciseStatus.PENDING -> TileBgDark
    }

    val numberBg = when (status) {
        ExerciseStatus.CURRENT -> Accent
        ExerciseStatus.DONE -> TileBg
        ExerciseStatus.PENDING -> TileBg
    }

    val numberColor = when (status) {
        ExerciseStatus.CURRENT -> Color(0xFF121212)
        ExerciseStatus.DONE -> Accent
        ExerciseStatus.PENDING -> Color.White.copy(alpha = 0.5f)
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
                .size(32.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(numberBg),
            contentAlignment = Alignment.Center
        ) {
            if (status == ExerciseStatus.DONE) {
                Text("✓", color = Accent, fontSize = 16.sp, fontWeight = FontWeight.Bold)
            } else {
                Text("$number", color = numberColor, fontSize = 14.sp, fontWeight = FontWeight.Bold)
            }
        }

        Spacer(Modifier.width(12.dp))

        Text(
            exercise.title,
            color = if (status == ExerciseStatus.DONE) Color.White.copy(alpha = 0.5f) else Color.White,
            fontSize = 14.sp,
            fontWeight = if (status == ExerciseStatus.CURRENT) FontWeight.Bold else FontWeight.Medium,
            modifier = Modifier.weight(1f)
        )

        Text(
            "~${exercise.minutes} мин",
            color = if (status == ExerciseStatus.DONE) Color.White.copy(alpha = 0.4f) else Accent,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold
        )
    }
}
