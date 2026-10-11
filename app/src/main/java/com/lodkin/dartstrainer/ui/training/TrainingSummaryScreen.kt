package com.lodkin.dartstrainer.ui.training

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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

/**
 * Статус упражнения в итогах тренировки.
 */
enum class SummaryStatus {
    DONE,       // Пройдено
    SKIPPED,    // Пропущено
    NOT_DONE    // Не дошли (например, тренировка прервана)
}

/**
 * Одно упражнение в итогах.
 */
data class ExerciseSummary(
    val title: String,
    val minutes: Int,
    val status: SummaryStatus
)

/**
 * Экран итогов тренировки.
 *
 * Показывает:
 *   • Поздравление.
 *   • Время: план / факт.
 *   • Список всех упражнений со статусами.
 *   • Кнопки: «Ещё тренировку» / «В меню».
 */
@Composable
fun TrainingSummaryScreen(
    playerName: String,
    planMinutes: Int,
    actualSeconds: Int,
    exercises: List<ExerciseSummary>,
    onRestart: () -> Unit,
    onFinish: () -> Unit
) {
    val actualMinutes = (actualSeconds / 60).coerceAtLeast(1)
    val doneCount = exercises.count { it.status == SummaryStatus.DONE }
    val skippedCount = exercises.count { it.status == SummaryStatus.SKIPPED }
    val totalCount = exercises.size

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0D1117))
            .padding(16.dp)
    ) {
        // ── Прокручиваемая часть ──
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .verticalScroll(rememberScrollState())
        ) {
            Spacer(Modifier.height(16.dp))

            // ── Поздравление ──
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
                    "Тренировка завершена!",
                    color = GoldAccent,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(Modifier.height(6.dp))
                Text(
                    "$playerName, ты хорошо потрудился сегодня.",
                    color = Color.White.copy(alpha = 0.8f),
                    fontSize = 14.sp
                )
            }

            Spacer(Modifier.height(16.dp))

            // ── Время ──
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(TileBgDark)
                    .padding(16.dp)
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        "Планировал",
                        color = Color.White.copy(alpha = 0.6f),
                        fontSize = 12.sp
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        "$planMinutes мин",
                        color = Color.White,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
                Column(
                    modifier = Modifier.weight(1f),
                    horizontalAlignment = Alignment.End
                ) {
                    Text(
                        "Оттренировал",
                        color = Color.White.copy(alpha = 0.6f),
                        fontSize = 12.sp
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        "$actualMinutes мин",
                        color = Accent,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(Modifier.height(20.dp))

            // ── Сводка по упражнениям ──
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

            // ── Итоговая строка ──
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(TileBgDark.copy(alpha = 0.6f))
                    .padding(14.dp),
                horizontalAlignment = Alignment.CenterHorizontally
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

        // ── Кнопки внизу ──
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(14.dp))
                    .background(TileBgDark)
                    .clickable { onFinish() }
                    .padding(vertical = 14.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    "В меню",
                    color = Color.White.copy(alpha = 0.85f),
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Medium
                )
            }
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(14.dp))
                    .background(Accent)
                    .clickable { onRestart() }
                    .padding(vertical = 14.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    "Ещё тренировку",
                    color = Color(0xFF121212),
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

// ─────────────────────────────────────────────
// Строка упражнения в итогах
// ─────────────────────────────────────────────

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
            Text(
                icon,
                color = iconColor,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )
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
