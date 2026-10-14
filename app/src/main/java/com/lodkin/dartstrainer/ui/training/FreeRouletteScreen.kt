package com.lodkin.dartstrainer.ui.training

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.lodkin.dartstrainer.theme.Accent
import com.lodkin.dartstrainer.theme.ErrorColor
import com.lodkin.dartstrainer.theme.GoldAccent
import com.lodkin.dartstrainer.theme.TileBg
import com.lodkin.dartstrainer.theme.TileBgDark
import kotlinx.coroutines.delay

/**
 * Экран «Рулетка» в свободной тренировке.
 *
 * При появлении — короткая анимация (1,5 сек): название быстро мелькает,
 * потом останавливается на выпавшем упражнении.
 *
 * Три кнопки: Играть / Пропустить / Закончить тренировку.
 *
 * @param finalExercise   упражнение, на котором остановится рулетка
 * @param allExercises    все доступные упражнения (для анимации)
 * @param completedCount  сколько упражнений уже пройдено в этой сессии
 * @param noRepeat        галочка «Без повторов»
 * @param onToggleNoRepeat колбэк переключения галочки
 */
@Composable
internal fun FreeRouletteScreen(
    finalExercise: PlannedExercise,
    allExercises: List<PlannedExercise>,
    completedCount: Int,
    noRepeat: Boolean,
    onToggleNoRepeat: (Boolean) -> Unit,
    onPlay: () -> Unit,
    onSkip: () -> Unit,
    onFinish: () -> Unit
) {
    var spinning by remember { mutableStateOf(true) }
    var shownTitle by remember { mutableStateOf(finalExercise.title) }

    LaunchedEffect(finalExercise.id) {
        spinning = true
        shownTitle = finalExercise.title
        val start = System.currentTimeMillis()
        while (System.currentTimeMillis() - start < 1500L) {
            shownTitle = allExercises.random().title
            delay(80L)
        }
        shownTitle = finalExercise.title
        spinning = false
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0D1117))
            .padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(Modifier.height(8.dp))

        Text(
            "СВОБОДНАЯ ТРЕНИРОВКА",
            color = Accent,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 3.sp
        )

        Spacer(Modifier.height(16.dp))

        // ── Галочка «Без повторов» ──
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(TileBg)
                .clickable { onToggleNoRepeat(!noRepeat) }
                .padding(horizontal = 14.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                "Без повторов",
                color = Color.White,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.weight(1f)
            )
            Switch(
                checked = noRepeat,
                onCheckedChange = { onToggleNoRepeat(it) },
                colors = SwitchDefaults.colors(
                    checkedThumbColor = Accent,
                    checkedTrackColor = Accent.copy(alpha = 0.5f),
                    uncheckedThumbColor = Color.White,
                    uncheckedTrackColor = TileBgDark
                )
            )
        }

        Spacer(Modifier.height(20.dp))

        Text(
            "ВЫПАЛО:",
            color = Color.White.copy(alpha = 0.55f),
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 2.sp
        )

        Spacer(Modifier.height(10.dp))

        // ── Плашка с названием ──
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 110.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(
                    if (spinning) TileBg
                    else Accent.copy(alpha = 0.15f)
                )
                .padding(16.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    shownTitle,
                    color = if (spinning) Color.White.copy(alpha = 0.7f) else GoldAccent,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                    lineHeight = 30.sp
                )
                if (!spinning) {
                    Spacer(Modifier.height(6.dp))
                    Text(
                        "~${finalExercise.minutes} мин",
                        color = Color.White.copy(alpha = 0.55f),
                        fontSize = 13.sp
                    )
                }
            }
        }

        Spacer(Modifier.height(14.dp))

        Text(
            "Пройдено: $completedCount",
            color = Color.White.copy(alpha = 0.5f),
            fontSize = 12.sp
        )

        Spacer(Modifier.height(22.dp))

        // ── «Играть» ──
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(14.dp))
                .background(if (spinning) Accent.copy(alpha = 0.35f) else Accent)
                .clickable(enabled = !spinning) { onPlay() }
                .padding(vertical = 16.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                "▶  Играть",
                color = Color(0xFF121212),
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(Modifier.height(10.dp))

        // ── «Пропустить» ──
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(14.dp))
                .background(Color(0xFF1A2332))
                .clickable(enabled = !spinning) { onSkip() }
                .padding(vertical = 14.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                "Пропустить",
                color = Color.White.copy(alpha = if (spinning) 0.4f else 0.85f),
                fontSize = 15.sp,
                fontWeight = FontWeight.Medium
            )
        }

        Spacer(Modifier.height(14.dp))

        Text(
            "─── или ───",
            color = Color.White.copy(alpha = 0.25f),
            fontSize = 12.sp
        )

        Spacer(Modifier.height(14.dp))

        // ── «Закончить тренировку» ──
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(14.dp))
                .background(ErrorColor.copy(alpha = 0.15f))
                .clickable(enabled = !spinning) { onFinish() }
                .padding(vertical = 14.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                "⬛  Закончить тренировку",
                color = ErrorColor.copy(alpha = if (spinning) 0.5f else 1f),
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}
