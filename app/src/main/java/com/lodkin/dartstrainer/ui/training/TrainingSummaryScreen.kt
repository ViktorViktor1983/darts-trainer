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
import com.lodkin.dartstrainer.theme.DarkBg
import com.lodkin.dartstrainer.theme.GoldAccent
import com.lodkin.dartstrainer.theme.TileBg
import com.lodkin.dartstrainer.theme.TileBgDark
import java.util.Locale

// Модели, которые используются на этом экране
data class ExerciseSummary(
    val title: String,
    val minutes: Int,
    val status: SummaryStatus
)

enum class SummaryStatus {
    DONE, SKIPPED, NOT_DONE
}

enum class SummaryBlockType {
    WARMUP, MAIN, PSYCHOLOGY, STRENGTH
}

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
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBg)
            .padding(16.dp)
    ) {
        // Шапка
        Text(
            text = "Итоги: ${blockTitle(blockType)}",
            color = GoldAccent,
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold
        )
        Spacer(Modifier.height(16.dp))

        // Статистика по времени
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(TileBg)
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text("План", color = Color.White.copy(alpha = 0.6f), fontSize = 12.sp)
                Text("$planMinutes мин", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
            }
            Column(horizontalAlignment = Alignment.End) {
                Text("Факт", color = Color.White.copy(alpha = 0.6f), fontSize = 12.sp)
                Text("${actualSeconds / 60} мин", color = GoldAccent, fontSize = 18.sp, fontWeight = FontWeight.Bold)
            }
        }

        Spacer(Modifier.height(20.dp))

        // Список упражнений
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
        ) {
            Text("УПРАЖНЕНИЯ", color = Accent, fontSize = 12.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
            Spacer(Modifier.height(8.dp))

            exercises.forEach { ex ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(TileBgDark)
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        ex.title,
                        color = if (ex.status == SummaryStatus.DONE) Color.White else Color.White.copy(alpha = 0.5f),
                        fontSize = 14.sp,
                        modifier = Modifier.weight(1f)
                    )
                    Text(
                        when (ex.status) {
                            SummaryStatus.DONE -> "✅"
                            SummaryStatus.SKIPPED -> "⏭"
                            SummaryStatus.NOT_DONE -> "—"
                        },
                        fontSize = 16.sp
                    )
                }
            }

            // Рекомендуемые игры (если есть)
            if (blockExercises.isNotEmpty()) {
                Spacer(Modifier.height(20.dp))
                Text("РЕКОМЕНДОВАНО СЫГРАТЬ", color = Accent, fontSize = 12.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
                Spacer(Modifier.height(8.dp))

                blockExercises.forEach { game ->
                    val isRecommended = game.id in recommendedIds
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(if (isRecommended) Accent.copy(alpha = 0.15f) else TileBg)
                            .clickable { onPlayGame(game.id) }
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            game.title,
                            color = Color.White,
                            fontSize = 14.sp,
                            fontWeight = if (isRecommended) FontWeight.Bold else FontWeight.Normal,
                            modifier = Modifier.weight(1f)
                        )
                        if (isRecommended) {
                            Text("▶ Играть", color = Accent, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        } else {
                            Text("▶", color = Accent, fontSize = 12.sp)
                        }
                    }
                }
            }
        }

        Spacer(Modifier.height(16.dp))

        // Кнопки
        if (isLastBlock) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(GoldAccent)
                    .clickable { onFinishTraining() },
                contentAlignment = Alignment.Center
            ) {
                Text("ЗАКОНЧИТЬ ТРЕНИРОВКУ", color = Color(0xFF121212), fontSize = 16.sp, fontWeight = FontWeight.Bold)
            }
        } else {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(Accent)
                    .clickable { onContinue() },
                contentAlignment = Alignment.Center
            ) {
                Text("ПРОДОЛЖИТЬ", color = Color(0xFF121212), fontSize = 16.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}

private fun blockTitle(type: SummaryBlockType): String = when (type) {
    SummaryBlockType.WARMUP -> "Разминка"
    SummaryBlockType.MAIN -> "Основная часть"
    SummaryBlockType.PSYCHOLOGY -> "Психология"
    SummaryBlockType.STRENGTH -> "Силовая"
}
