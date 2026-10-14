package com.lodkin.dartstrainer.ui.training

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.lodkin.dartstrainer.theme.Accent
import com.lodkin.dartstrainer.theme.GoldAccent
import com.lodkin.dartstrainer.theme.TileBg
import com.lodkin.dartstrainer.theme.TileBgDark
import kotlinx.coroutines.delay

/**
 * Оверлей таймера отдыха между упражнениями.
 *
 * Показывается после завершения упражнения, блокирует переход
 * к следующему, пока не истечёт время или игрок не нажмёт «Пропустить».
 *
 * Длительность зависит от тяжести упражнения:
 *   • лёгкие (1–3)  — 3 минуты
 *   • средние (4–6) — 4 минуты
 *   • тяжёлые (7–8) — 5 минут
 *
 * @param totalSeconds сколько секунд длится отдых
 * @param nextExerciseTitle название следующего упражнения (для подсказки)
 * @param onSkip вызывается, когда игрок нажал «Пропустить» или время вышло
 */
@Composable
fun RestTimerOverlay(
    totalSeconds: Int,
    nextExerciseTitle: String?,
    onSkip: () -> Unit
) {
    if (totalSeconds <= 0) return

    var secondsLeft by remember(totalSeconds) { mutableStateOf(totalSeconds) }
    var finished by remember { mutableStateOf(false) }

    LaunchedEffect(totalSeconds) {
        secondsLeft = totalSeconds
        finished = false
        while (secondsLeft > 0) {
            delay(1000L)
            secondsLeft -= 1
        }
        finished = true
    }

    LaunchedEffect(finished) {
        if (finished) {
            // Автоматически закрываем через 300 мс, чтобы не мигало.
            delay(300L)
            onSkip()
        }
    }

    Dialog(
        onDismissRequest = { },
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            dismissOnBackPress = false,
            dismissOnClickOutside = false
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.72f)),
            contentAlignment = Alignment.Center
        ) {
            Column(
                modifier = Modifier
                    .padding(24.dp)
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(22.dp))
                    .background(TileBgDark)
                    .padding(horizontal = 24.dp, vertical = 28.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    "ОТДЫХ",
                    color = Accent,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 4.sp
                )

                Spacer(Modifier.height(16.dp))

                Text(
                    formatRestTime(secondsLeft),
                    color = GoldAccent,
                    fontSize = 64.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )

                Spacer(Modifier.height(8.dp))

                Text(
                    "Расслабь руку, подыши спокойно",
                    color = Color.White.copy(alpha = 0.75f),
                    fontSize = 14.sp,
                    textAlign = TextAlign.Center
                )

                if (!nextExerciseTitle.isNullOrBlank()) {
                    Spacer(Modifier.height(16.dp))
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(TileBg)
                            .padding(12.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            "ДАЛЬШЕ",
                            color = Accent,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 2.sp
                        )
                        Spacer(Modifier.height(4.dp))
                        Text(
                            nextExerciseTitle,
                            color = Color.White,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium,
                            textAlign = TextAlign.Center
                        )
                    }
                }

                Spacer(Modifier.height(22.dp))

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(Accent)
                        .clickable { onSkip() }
                        .padding(vertical = 16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        "Пропустить отдых",
                        color = Color(0xFF121212),
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

private fun formatRestTime(total: Int): String {
    val safe = total.coerceAtLeast(0)
    val m = safe / 60
    val s = safe % 60
    return "%d:%02d".format(m, s)
}
