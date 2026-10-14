package com.lodkin.dartstrainer.ui.training

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.lodkin.dartstrainer.theme.Accent
import com.lodkin.dartstrainer.theme.ErrorColor
import com.lodkin.dartstrainer.theme.GoldAccent
import com.lodkin.dartstrainer.theme.TileBgDark

/**
 * Диалог «Прервать тренировку?» (обычный режим).
 * Игрок выбирает: Закончить сейчас или Отложить до 3 часов.
 */
@Composable
internal fun ExitTrainingDialog(
    onFinish: () -> Unit,
    onPostpone: () -> Unit
) {
    AlertDialog(
        onDismissRequest = { },
        containerColor = TileBgDark,
        title = {
            Text(
                "Прервать тренировку?",
                color = GoldAccent,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column {
                Text(
                    "Закончить сейчас или отложить?",
                    color = Color.White.copy(alpha = 0.9f),
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Medium
                )
                Spacer(Modifier.height(10.dp))
                Text(
                    "• «Закончить» — завершает тренировку. " +
                            "Всё, что успел пройти, пойдёт в статистику.",
                    color = Color.White.copy(alpha = 0.75f),
                    fontSize = 13.sp,
                    lineHeight = 18.sp
                )
                Spacer(Modifier.height(6.dp))
                Text(
                    "• «Отложить» — сохранит тренировку до 3 часов. " +
                            "Вернёшься с того же места. Через 3 часа — сбросится, " +
                            "прогресс уйдёт в статистику.",
                    color = Color.White.copy(alpha = 0.75f),
                    fontSize = 13.sp,
                    lineHeight = 18.sp
                )
            }
        },
        confirmButton = {
            Text(
                "Закончить",
                color = Color(0xFF121212),
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier
                    .clip(RoundedCornerShape(10.dp))
                    .background(ErrorColor)
                    .clickable { onFinish() }
                    .padding(horizontal = 16.dp, vertical = 10.dp)
            )
        },
        dismissButton = {
            Text(
                "Отложить (до 3ч)",
                color = Color.White,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier
                    .clip(RoundedCornerShape(10.dp))
                    .background(TileBgDark)
                    .clickable { onPostpone() }
                    .padding(horizontal = 16.dp, vertical = 10.dp)
            )
        }
    )
}

/**
 * Диалог «Незавершённая тренировка» — Продолжить или Начать заново.
 */
@Composable
internal fun ResumeTrainingDialog(
    minutesAgo: Int,
    onContinue: () -> Unit,
    onRestart: () -> Unit
) {
    AlertDialog(
        onDismissRequest = { },
        containerColor = TileBgDark,
        title = {
            Text(
                "Незавершённая тренировка",
                color = GoldAccent,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column {
                Text(
                    "У тебя есть незавершённая тренировка. " +
                            if (minutesAgo > 0) "Прошло $minutesAgo мин." else "Только что прервана.",
                    color = Color.White.copy(alpha = 0.85f),
                    fontSize = 14.sp,
                    lineHeight = 20.sp
                )
                Spacer(Modifier.height(10.dp))
                Text(
                    "Продолжить с того места, где остановился, или начать заново?",
                    color = Color.White.copy(alpha = 0.7f),
                    fontSize = 13.sp
                )
            }
        },
        confirmButton = {
            Text(
                "Продолжить",
                color = Color(0xFF121212),
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier
                    .clip(RoundedCornerShape(10.dp))
                    .background(Accent)
                    .clickable { onContinue() }
                    .padding(horizontal = 16.dp, vertical = 10.dp)
            )
        },
        dismissButton = {
            Text(
                "Начать заново",
                color = Color.White.copy(alpha = 0.75f),
                fontSize = 14.sp,
                modifier = Modifier
                    .clip(RoundedCornerShape(10.dp))
                    .clickable { onRestart() }
                    .padding(horizontal = 14.dp, vertical = 10.dp)
            )
        }
    )
}

/**
 * Диалог «Время тренировки вышло» — Закончить или Продолжить +20 мин.
 */
@Composable
internal fun TimeUpDialog(
    trainingMinutes: Int,
    onFinish: () -> Unit,
    onContinuePlus20: () -> Unit
) {
    AlertDialog(
        onDismissRequest = { },
        containerColor = TileBgDark,
        title = {
            Text(
                "⏱ Время тренировки вышло",
                color = GoldAccent,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column {
                Text(
                    "Ты потренировался $trainingMinutes минут — это твоя цель на сегодня. " +
                            "Что делаем дальше?",
                    color = Color.White.copy(alpha = 0.85f),
                    fontSize = 14.sp
                )
            }
        },
        confirmButton = {
            TextButton(onClick = onFinish) {
                Text("Закончить", color = ErrorColor, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onContinuePlus20) {
                Text("Продолжить +20 мин", color = GoldAccent, fontWeight = FontWeight.Bold)
            }
        }
    )
}
