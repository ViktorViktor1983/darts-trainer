package com.lodkin.dartstrainer.ui.training

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.lodkin.dartstrainer.theme.Accent
import com.lodkin.dartstrainer.theme.GoldAccent
import com.lodkin.dartstrainer.theme.TileBgDark

/**
 * Дополнительные всплывающие окошки тренировочного раздела.
 *
 * Здесь живут только «тонкие» диалоги — текст и кнопки.
 * Вся бизнес-логика (сброс стейта, переходы, запуск шагов) остаётся
 * в MainActivity и передаётся сюда через колбэки.
 *
 * Раньше эти диалоги были прямо в MainActivity.kt — вынесли, чтобы
 * разгрузить главный файл.
 */

/**
 * Диалог «Незавершённые нормативы».
 *
 * Показывается, когда в TrainingMenuScreen игрок нажимает «Тренировка»
 * и в хранилище нашлась незавершённая сдача нормативов (моложе 24 часов).
 *
 * Кнопки:
 *   • «Продолжить» — MainActivity восстановит прогресс из снапшота.
 *   • «Начать заново» — MainActivity очистит снапшот, начнётся новая сдача.
 *
 * @param minutesAgo сколько минут назад было последнее сохранение (0 = «только что»).
 * @param onContinue игрок выбрал «Продолжить».
 * @param onRestart игрок выбрал «Начать заново».
 */
@Composable
fun NormativesResumeDialog(
    minutesAgo: Int,
    onContinue: () -> Unit,
    onRestart: () -> Unit
) {
    AlertDialog(
        onDismissRequest = { },
        containerColor = TileBgDark,
        title = {
            Text(
                "Незавершённые нормативы",
                color = GoldAccent,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Text(
                "У тебя есть незавершённая сдача нормативов. " +
                        if (minutesAgo > 0) "Прошло $minutesAgo мин." else "Только что прервана.",
                color = Color.White.copy(alpha = 0.85f),
                fontSize = 14.sp
            )
        },
        confirmButton = {
            TextButton(onClick = onContinue) {
                Text("Продолжить", color = Accent, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onRestart) {
                Text("Начать заново", color = Color.White.copy(alpha = 0.75f))
            }
        }
    )
}

/**
 * Диалог «Все упражнения пройдены» (свободная тренировка).
 *
 * Показывается, когда в свободном режиме включена галочка «Без повторов»
 * и игрок прошёл (или пропустил) все 8 упражнений. Спрашиваем, что делать:
 * начать новый круг или закончить тренировку.
 *
 * @param onNewRound игрок выбрал «Новый круг» — MainActivity сбросит списки
 *                   пройденных/пропущенных и выберет следующее упражнение.
 * @param onFinish игрок выбрал «Закончить тренировку» — MainActivity уведёт
 *                 на экран итогов свободной тренировки.
 */
@Composable
fun FreeNewRoundDialog(
    onNewRound: () -> Unit,
    onFinish: () -> Unit
) {
    AlertDialog(
        onDismissRequest = { },
        containerColor = TileBgDark,
        title = {
            Text(
                "Все упражнения пройдены",
                color = GoldAccent,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Text(
                "Ты прошёл все 8 упражнений. Что делаем дальше?",
                color = Color.White.copy(alpha = 0.9f),
                fontSize = 14.sp
            )
        },
        confirmButton = {
            TextButton(onClick = onNewRound) {
                Text("Новый круг", color = Accent, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onFinish) {
                Text("Закончить тренировку", color = Color.White.copy(alpha = 0.75f))
            }
        }
    )
}
