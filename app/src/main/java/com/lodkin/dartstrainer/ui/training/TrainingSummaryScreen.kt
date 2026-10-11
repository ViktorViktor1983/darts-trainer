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

/**
 * Статус упражнения в итогах тренировки.
 */
enum class SummaryStatus {
    DONE,
    SKIPPED,
    NOT_DONE
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
 * Игра, доступная в разделе «Остаться здесь».
 */
data class ExtraGame(
    val id: String,
    val title: String,
    val isRecommended: Boolean
)

/**
 * Список игр основной части.
 * Рекомендуемые — наверх.
 */
fun defaultMainGames(recommendedIds: Set<String>): List<ExtraGame> {
    val all = listOf(
        ExtraGame("aroundclock_doubles", "Кругосветка: Удвоения", recommendedIds.contains("aroundclock_doubles")),
        ExtraGame("aroundclock_trebles", "Кругосветка: Утроения", recommendedIds.contains("aroundclock_trebles")),
        ExtraGame("aroundclock_sectors", "Кругосветка: Сектора", recommendedIds.contains("aroundclock_sectors")),
        ExtraGame("sector20", "Сектор 20", recommendedIds.contains("sector20")),
        ExtraGame("scoreset", "Набор очков", recommendedIds.contains("scoreset")),
        ExtraGame("biground", "Большой раунд", recommendedIds.contains("biground")),
        ExtraGame("game501", "501", recommendedIds.contains("game501")),
        ExtraGame("cricket", "Крикет", recommendedIds.contains("cricket"))
    )
    return all.sortedByDescending { it.isRecommended }
}

/**
 * Экран итогов тренировки.
 *
 * @param nextBlockTitle  название следующего блока. null — тренировка завершена.
 * @param recommendedIds  id рекомендованных игр (★).
 */
@Composable
fun TrainingSummaryScreen(
    playerName: String,
    planMinutes: Int,
    actualSeconds: Int,
    exercises: List<ExerciseSummary>,
    nextBlockTitle: String?,
    recommendedIds: Set<String>,
    onContinue: () -> Unit,
    onPlayGame: (String) -> Unit
) {
    val actualMinutes = (actualSeconds / 60).coerceAtLeast(1)
    val doneCount = exercises.count { it.status == SummaryStatus.DONE }
    val skippedCount = exercises.count { it.status == SummaryStatus.SKIPPED }
    val totalCount = exercises.size

    var showCoachWarning by remember { mutableStateOf(false) }
    var showStayDialog by remember { mutableStateOf(false) }

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

            // ── Время ──
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

        // ── Две кнопки внизу ──
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(14.dp))
                    .background(TileBgDark)
                    .clickable { showCoachWarning = true }
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

            val continueLabel = if (nextBlockTitle == null) "Закончить тренировку" else "Продолжить тренировку"
            Box(
                modifier = Modifier
                    .weight(1.3f)
                    .clip(RoundedCornerShape(14.dp))
                    .background(Accent)
                    .clickable { onContinue() }
                    .padding(vertical = 14.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    continueLabel,
                    color = Color(0xFF121212),
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }

    // ── Диалог-предупреждение от тренера ──
    if (showCoachWarning) {
        AlertDialog(
            onDismissRequest = { showCoachWarning = false },
            containerColor = TileBgDark,
            title = {
                Text(
                    "Совет тренера",
                    color = GoldAccent,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column {
                    Text(
                        playerName + ", рекомендую сначала завершить программу полностью — " +
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
                    Spacer(Modifier.height(14.dp))
                    Text(
                        "Что делаем?",
                        color = Color.White.copy(alpha = 0.6f),
                        fontSize = 13.sp
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
                            showStayDialog = true
                        }
                        .padding(horizontal = 14.dp, vertical = 10.dp)
                )
            }
        )
    }

    // ── Диалог «Остаться здесь» — список игр ──
    if (showStayDialog) {
        val games = defaultMainGames(recommendedIds)
        StayHereDialog(
            games = games,
            onSelect = { id ->
                showStayDialog = false
                onPlayGame(id)
            },
            onClose = { showStayDialog = false }
        )
    }
}

@Composable
private fun StayHereDialog(
    games: List<ExtraGame>,
    onSelect: (String) -> Unit,
    onClose: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onClose,
        containerColor = TileBgDark,
        title = {
            Text(
                "Продолжить здесь",
                color = GoldAccent,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 400.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                Text(
                    "Выбери игру. Результат пойдёт в тренировочную статистику.",
                    color = Color.White.copy(alpha = 0.7f),
                    fontSize = 13.sp
                )
                Spacer(Modifier.height(12.dp))

                games.forEach { game ->
                    GameRow(game) { onSelect(game.id) }
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
                    .clickable { onClose() }
                    .padding(horizontal = 18.dp, vertical = 10.dp)
            )
        }
    )
}

@Composable
private fun GameRow(game: ExtraGame, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(if (game.isRecommended) Accent.copy(alpha = 0.12f) else TileBg)
            .clickable { onClick() }
            .padding(horizontal = 14.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            game.title,
            color = Color.White,
            fontSize = 14.sp,
            fontWeight = if (game.isRecommended) FontWeight.Bold else FontWeight.Medium,
            modifier = Modifier.weight(1f)
        )
        if (game.isRecommended) {
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
