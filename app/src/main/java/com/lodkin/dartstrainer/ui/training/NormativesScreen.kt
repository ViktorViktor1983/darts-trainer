package com.lodkin.dartstrainer.ui.training

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.lodkin.dartstrainer.data.training.DartsNorms
import com.lodkin.dartstrainer.theme.Accent
import com.lodkin.dartstrainer.theme.ErrorColor
import com.lodkin.dartstrainer.theme.GoldAccent
import com.lodkin.dartstrainer.theme.TileBg
import com.lodkin.dartstrainer.theme.TileBgDark

/**
 * Шаг нормативов.
 */
enum class NormativeStep(val id: String, val title: String, val subtitle: String) {
    GAME_501("game501", "501 против бота", "до 3 побед · макс 5 легов"),
    CRICKET("cricket", "Крикет против бота", "до 3 побед · макс 5 легов"),
    SECTOR_20("sector20", "Сектор 20", "30 дротиков в 20-й сектор"),
    BIG_ROUND("biground", "Большой раунд", "21 сектор × 3 дротика"),
    SCORE_SET("scoreset", "Набор очков", "10 подходов × 3 дротика")
}

/**
 * Экран «Сдача нормативов».
 *
 * Показывает 5 шагов. Пройденные отмечаются галочкой.
 * Кнопка «Начать / Продолжить» запускает текущий шаг (через колбэк).
 *
 * @param completedSteps набор пройденных шагов (id).
 * @param currentStep шаг, который сейчас нужно выполнить, или null если всё пройдено.
 * @param result результат оценки — если null, нормативы ещё идут; иначе показан итог.
 * @param playerName имя игрока.
 * @param currentLevel текущий уровень игрока.
 * @param newLevel новый уровень, достигнутый по итогам нормативов (или null, если не сдал).
 * @param onStartStep колбэк: «начать этот шаг».
 * @param onFinish колбэк: «закрыть экран нормативов».
 * @param onCancel колбэк: «отменить нормативы и вернуться».
 */
@Composable
fun NormativesScreen(
    playerName: String,
    currentLevel: Int,
    completedSteps: Set<String>,
    currentStep: NormativeStep?,
    newLevel: Int?,
    onStartStep: (NormativeStep) -> Unit,
    onFinish: () -> Unit,
    onCancel: () -> Unit
) {
    var showCancelDialog by remember { mutableStateOf(false) }

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
                    .clickable { showCancelDialog = true }
                    .padding(horizontal = 14.dp, vertical = 10.dp)
            ) {
                Text("← Закрыть", color = Accent, fontSize = 14.sp, fontWeight = FontWeight.Medium)
            }
            Spacer(Modifier.width(12.dp))
            Text(
                "Сдача нормативов",
                color = GoldAccent,
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(Modifier.height(20.dp))

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .verticalScroll(rememberScrollState())
        ) {
            // ── Шапка ──
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(TileBg)
                    .padding(16.dp)
            ) {
                Text(
                    "ПРИВЕТ, ${playerName.uppercase()}",
                    color = Accent,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 2.sp
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    "Сейчас мы проверим твой уровень по всем параметрам. " +
                            "Пять упражнений — как на настоящем экзамене. " +
                            "По итогам дадим новый уровень, если есть рост.",
                    color = Color.White.copy(alpha = 0.85f),
                    fontSize = 14.sp,
                    lineHeight = 20.sp
                )
                Spacer(Modifier.height(12.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        "Текущий уровень:",
                        color = Color.White.copy(alpha = 0.6f),
                        fontSize = 13.sp
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        "$currentLevel · ${DartsNorms.getLevelName(currentLevel)}",
                        color = GoldAccent,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(Modifier.height(20.dp))

            SectionTitle("ПРОГРАММА НОРМАТИВОВ")
            Spacer(Modifier.height(10.dp))

            // ── Список шагов ──
            NormativeStep.entries.forEach { step ->
                StepRow(
                    step = step,
                    isDone = step.id in completedSteps,
                    isCurrent = step == currentStep,
                    isLast = newLevel != null
                )
                Spacer(Modifier.height(8.dp))
            }

            Spacer(Modifier.height(20.dp))

            // ── Итог ──
            if (newLevel != null) {
                ResultCard(
                    currentLevel = currentLevel,
                    newLevel = newLevel,
                    isSuccess = newLevel > currentLevel
                )
                Spacer(Modifier.height(20.dp))
            }
        }

        // ── Нижняя кнопка ──
        if (newLevel != null) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(Accent)
                    .clickable { onFinish() }
                    .padding(vertical = 16.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    "Продолжить",
                    color = Color(0xFF121212),
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        } else {
            val label = when {
                currentStep == null -> "Все упражнения пройдены"
                currentStep == NormativeStep.GAME_501 && completedSteps.isEmpty() -> "▶  Начать нормативы"
                else -> "▶  Продолжить · ${currentStep.title}"
            }
            val enabled = currentStep != null
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(if (enabled) Accent else Accent.copy(alpha = 0.4f))
                    .clickable(enabled = enabled) { currentStep?.let { onStartStep(it) } }
                    .padding(vertical = 16.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    label,
                    color = Color(0xFF121212),
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }

    // ── Диалог отмены ──
    if (showCancelDialog) {
        AlertDialog(
            onDismissRequest = { showCancelDialog = false },
            containerColor = TileBgDark,
            title = {
                Text(
                    "Прервать сдачу нормативов?",
                    color = GoldAccent,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Text(
                    "Прогресс сдачи не сохранится. Ты сможешь начать заново в любой момент.",
                    color = Color.White.copy(alpha = 0.9f),
                    fontSize = 14.sp
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    showCancelDialog = false
                    onCancel()
                }) { Text("Прервать", color = ErrorColor, fontWeight = FontWeight.Bold) }
            },
            dismissButton = {
                TextButton(onClick = { showCancelDialog = false }) {
                    Text("Продолжить", color = Accent, fontWeight = FontWeight.Bold)
                }
            }
        )
    }
}

// ─────────────────────────────────────────────
// Строка шага
// ─────────────────────────────────────────────

@Composable
private fun StepRow(
    step: NormativeStep,
    isDone: Boolean,
    isCurrent: Boolean,
    isLast: Boolean
) {
    val bg = when {
        isDone -> TileBgDark.copy(alpha = 0.5f)
        isCurrent -> Accent.copy(alpha = 0.15f)
        else -> TileBgDark
    }
    val numberBg = when {
        isDone -> TileBg
        isCurrent -> Accent
        else -> TileBg
    }
    val numberColor = when {
        isCurrent -> Color(0xFF121212)
        else -> Accent
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(bg)
            .padding(14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(32.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(numberBg),
            contentAlignment = Alignment.Center
        ) {
            if (isDone) {
                Text("✓", color = Accent, fontSize = 16.sp, fontWeight = FontWeight.Bold)
            } else {
                Text(
                    "${NormativeStep.entries.indexOf(step) + 1}",
                    color = numberColor,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
        Spacer(Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                step.title,
                color = if (isDone) Color.White.copy(alpha = 0.6f) else Color.White,
                fontSize = 14.sp,
                fontWeight = if (isCurrent) FontWeight.Bold else FontWeight.Medium
            )
            Spacer(Modifier.height(2.dp))
            Text(
                step.subtitle,
                color = Color.White.copy(alpha = 0.5f),
                fontSize = 11.sp
            )
        }
        if (isCurrent && !isLast) {
            Text(
                "сейчас",
                color = Accent,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

// ─────────────────────────────────────────────
// Карточка результата
// ─────────────────────────────────────────────

@Composable
private fun ResultCard(
    currentLevel: Int,
    newLevel: Int,
    isSuccess: Boolean
) {
    val bg = if (isSuccess) GoldAccent.copy(alpha = 0.15f) else TileBg
    val titleColor = if (isSuccess) GoldAccent else Color.White
    val title = if (isSuccess) "🏆 НОВЫЙ УРОВЕНЬ!" else "Уровень подтверждён"

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(bg)
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            title,
            color = titleColor,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )
        Spacer(Modifier.height(10.dp))

        if (isSuccess) {
            Text(
                "Было: $currentLevel · ${DartsNorms.getLevelName(currentLevel)}",
                color = Color.White.copy(alpha = 0.6f),
                fontSize = 13.sp
            )
            Spacer(Modifier.height(6.dp))
            Text(
                "Стало: $newLevel · ${DartsNorms.getLevelName(newLevel)}",
                color = GoldAccent,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(Modifier.height(12.dp))
            Text(
                "Статистика этого периода сброшена — начинаешь новый. " +
                        "Рекорды и закрытые чекауты сохранены.",
                color = Color.White.copy(alpha = 0.75f),
                fontSize = 12.sp,
                lineHeight = 17.sp,
                textAlign = TextAlign.Center
            )
        } else {
            Text(
                "Уровень $currentLevel · ${DartsNorms.getLevelName(currentLevel)}",
                color = GoldAccent,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(Modifier.height(12.dp))
            Text(
                "Статистика этого периода сохранена. Следующие нормативы — " +
                        "после новых тренировок. Продолжай работать и расти!",
                color = Color.White.copy(alpha = 0.75f),
                fontSize = 12.sp,
                lineHeight = 17.sp,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun SectionTitle(text: String) {
    Text(
        text,
        color = Accent,
        fontSize = 12.sp,
        fontWeight = FontWeight.Bold,
        letterSpacing = 2.sp
    )
}
