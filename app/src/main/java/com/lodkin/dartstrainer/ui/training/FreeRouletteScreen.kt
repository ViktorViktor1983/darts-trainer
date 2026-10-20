package com.lodkin.dartstrainer.ui.training

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
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
 * @param withFriend включен ли режим игры с другом.
 * @param onToggleWithFriend колбэк переключения режима.
 */
@Composable
internal fun FreeRouletteScreen(
    finalExercise: PlannedExercise?,
    allExercises: List<PlannedExercise>,
    completedCount: Int,
    noRepeat: Boolean,
    onToggleNoRepeat: (Boolean) -> Unit,
    chooseMyself: Boolean,
    onToggleChooseMyself: (Boolean) -> Unit,
    withFriend: Boolean,
    onToggleWithFriend: (Boolean) -> Unit,
    onPlay: () -> Unit,
    onPlayDirect: (String) -> Unit,
    onSkip: () -> Unit,
    onFinish: () -> Unit
) {
    var spinning by remember { mutableStateOf(!chooseMyself && finalExercise != null) }
    var shownTitle by remember { mutableStateOf(finalExercise?.title ?: "") }

    LaunchedEffect(finalExercise?.id, chooseMyself) {
        if (chooseMyself || finalExercise == null) {
            spinning = false
            return@LaunchedEffect
        }
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

        // ── Переключатели ──
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // 1. Без повторов
            Row(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(12.dp))
                    .background(TileBg)
                    .clickable { onToggleNoRepeat(!noRepeat) }
                    .padding(horizontal = 10.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "Без повторов",
                    color = Color.White,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.weight(1f)
                )
                Switch(
                    checked = noRepeat,
                    onCheckedChange = { onToggleNoRepeat(it) },
                    modifier = Modifier.scale(0.7f),
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = Accent,
                        checkedTrackColor = Accent.copy(alpha = 0.5f),
                        uncheckedThumbColor = Color.White,
                        uncheckedTrackColor = TileBgDark
                    )
                )
            }

            // 2. Выбор вручную
            Row(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(12.dp))
                    .background(TileBg)
                    .clickable { onToggleChooseMyself(!chooseMyself) }
                    .padding(horizontal = 10.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "Выбрать самому",
                    color = Color.White,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.weight(1f)
                )
                Switch(
                    checked = chooseMyself,
                    onCheckedChange = { onToggleChooseMyself(it) },
                    modifier = Modifier.scale(0.7f),
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = GoldAccent,
                        checkedTrackColor = GoldAccent.copy(alpha = 0.5f),
                        uncheckedThumbColor = Color.White,
                        uncheckedTrackColor = TileBgDark
                    )
                )
            }
        }

        Spacer(Modifier.height(20.dp))

        if (chooseMyself) {
            // ── РЕЖИМ ВЫБОРА ВРУЧНУЮ ──
            Text(
                "ВЫБЕРИ УПРАЖНЕНИЕ:",
                color = Color.White.copy(alpha = 0.55f),
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 2.sp
            )
            Spacer(Modifier.height(10.dp))

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                allExercises.forEach { ex ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(TileBg)
                            .clickable { onPlayDirect(ex.id) }
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                ex.title,
                                color = Color.White,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(Modifier.height(4.dp))
                            Text(
                                "~${ex.minutes} мин",
                                color = Color.White.copy(alpha = 0.5f),
                                fontSize = 12.sp
                            )
                        }
                        Text("▶", color = Accent, fontSize = 18.sp)
                    }
                }
                
                // ── Переключатель Игры с другом ──
                Spacer(Modifier.height(8.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(TileBg)
                        .clickable { onToggleWithFriend(!withFriend) }
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            "Играть с другом",
                            color = Color.White,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            "Для 501 и Крикета",
                            color = Color.White.copy(alpha = 0.5f),
                            fontSize = 11.sp
                        )
                    }
                    Switch(
                        checked = withFriend,
                        onCheckedChange = { onToggleWithFriend(it) },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.White,
                            checkedTrackColor = Accent,
                            uncheckedThumbColor = Color.White,
                            uncheckedTrackColor = TileBgDark
                        )
                    )
                }
                Spacer(Modifier.height(10.dp))
            }

            Spacer(Modifier.height(10.dp))

            // ── «Закончить тренировку» ──
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(ErrorColor.copy(alpha = 0.15f))
                    .clickable { onFinish() }
                    .padding(vertical = 14.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    "⬛  Закончить тренировку",
                    color = ErrorColor,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )
            }

        } else {
            // ── РЕЖИМ РУЛЕТКИ ──
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
                    if (!spinning && finalExercise != null) {
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
}
