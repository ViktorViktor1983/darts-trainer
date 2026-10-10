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
import com.lodkin.dartstrainer.theme.Accent
import com.lodkin.dartstrainer.theme.GoldAccent
import com.lodkin.dartstrainer.theme.TileBg
import com.lodkin.dartstrainer.theme.TileBgDark
import kotlinx.coroutines.delay

/**
 * Экран разминки.
 *
 * 4 фазы подряд:
 *   1. Мышцы       — разогрев и растяжка
 *   2. Траектория  — броски без цели, фокус на движении руки
 *   3. Кучность    — одинаковая траектория, без цели
 *   4. Прицеливание — переход к основной части
 *
 * На каждой фазе таймер. Можно пропустить фазу кнопкой «Далее».
 * Можно пропустить всю разминку кнопкой сверху (если игрок уже разогрет).
 *
 * @param longWarmup  true для тренировок 100+ мин (траектория и кучность по 2 мин)
 */
@Composable
fun TrainingWarmupScreen(
    longWarmup: Boolean = false,
    onFinish: () -> Unit,
    onBack: () -> Unit
) {
    // Сколько минут на каждой фазе
    val trajMinutes = if (longWarmup) 2 else 1
    val accMinutes = if (longWarmup) 2 else 1

    // Список фаз
    val phases = remember(longWarmup) {
        listOf(
            WarmupPhase(
                number = 1,
                title = "Мышцы",
                description = "Вращения плечами, локтями, кистями. Наклоны корпуса. " +
                        "Растяжка плеча и предплечья по 20–30 сек на каждую руку.",
                seconds = 3 * 60
            ),
            WarmupPhase(
                number = 2,
                title = "Траектория",
                description = "Бросай без прицеливания, в любую часть доски. " +
                        "Смотри не на мишень, а на руку: как выпрямляется локоть, " +
                        "с какой силой идёт выброс.",
                seconds = trajMinutes * 60
            ),
            WarmupPhase(
                number = 3,
                title = "Кучность",
                description = "По-прежнему без цели. Первый бросок — куда угодно. " +
                        "Второй и третий — тем же движением. Следи за одинаковостью.",
                seconds = accMinutes * 60
            ),
            WarmupPhase(
                number = 4,
                title = "Прицеливание",
                description = "Теперь целься в конкретный сектор. Разминка закончена — " +
                        "переходим к основной части тренировки.",
                seconds = 60
            )
        )
    }

    var phaseIndex by remember { mutableStateOf(0) }
    var secondsLeft by remember { mutableStateOf(phases[0].seconds) }
    var isPaused by remember { mutableStateOf(false) }

    // Таймер текущей фазы
    LaunchedEffect(phaseIndex, isPaused) {
        if (isPaused) return@LaunchedEffect
        while (secondsLeft > 0 && !isPaused) {
            delay(1000L)
            secondsLeft -= 1
        }
    }

    // Когда время фазы истекло — автопереход
    LaunchedEffect(secondsLeft) {
        if (secondsLeft <= 0) {
            if (phaseIndex < phases.lastIndex) {
                phaseIndex += 1
                secondsLeft = phases[phaseIndex].seconds
            } else {
                onFinish()
            }
        }
    }

    val currentPhase = phases[phaseIndex]

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
                "Разминка",
                color = GoldAccent,
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(Modifier.height(20.dp))

        // ── Прогресс-точки 4 фазы ──
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            phases.forEachIndexed { i, _ ->
                val isActive = i == phaseIndex
                val isDone = i < phaseIndex
                Box(
                    modifier = Modifier
                        .padding(horizontal = 4.dp)
                        .size(if (isActive) 14.dp else 10.dp)
                        .clip(RoundedCornerShape(50))
                        .background(
                            when {
                                isActive -> Accent
                                isDone -> Accent.copy(alpha = 0.6f)
                                else -> TileBg
                            }
                        )
                )
            }
        }

        Spacer(Modifier.height(24.dp))

        // ── Карточка фазы ──
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .clip(RoundedCornerShape(16.dp))
                .background(TileBg)
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                "ШАГ ${currentPhase.number} ИЗ ${phases.size}",
                color = Accent,
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                letterSpacing = 3.sp
            )

            Spacer(Modifier.height(12.dp))

            Text(
                currentPhase.title,
                color = GoldAccent,
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )

            Spacer(Modifier.height(20.dp))

            // Таймер
            Text(
                formatSeconds(secondsLeft),
                color = Color.White,
                fontSize = 64.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )

            Spacer(Modifier.height(24.dp))

            Text(
                currentPhase.description,
                color = Color.White.copy(alpha = 0.85f),
                fontSize = 14.sp,
                textAlign = TextAlign.Center,
                lineHeight = 20.sp
            )
        }

        Spacer(Modifier.height(16.dp))

        // ── Кнопки управления ──
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Пауза / Продолжить
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(14.dp))
                    .background(TileBgDark)
                    .clickable { isPaused = !isPaused }
                    .padding(vertical = 14.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    if (isPaused) "▶  Продолжить" else "⏸  Пауза",
                    color = Accent,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium
                )
            }

            // Далее / Пропустить фазу
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(14.dp))
                    .background(Accent)
                    .clickable {
                        if (phaseIndex < phases.lastIndex) {
                            phaseIndex += 1
                            secondsLeft = phases[phaseIndex].seconds
                            isPaused = false
                        } else {
                            onFinish()
                        }
                    }
                    .padding(vertical = 14.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    if (phaseIndex < phases.lastIndex) "Далее →" else "К тренировке →",
                    color = Color(0xFF121212),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        Spacer(Modifier.height(10.dp))

        // ── Пропустить всю разминку ──
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(TileBgDark.copy(alpha = 0.6f))
                .clickable { onFinish() }
                .padding(vertical = 12.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                "Пропустить разминку",
                color = Color.White.copy(alpha = 0.7f),
                fontSize = 13.sp
            )
        }
    }
}

// ─────────────────────────────────────────────
// Модель фазы
// ─────────────────────────────────────────────

private data class WarmupPhase(
    val number: Int,
    val title: String,
    val description: String,
    val seconds: Int
)

private fun formatSeconds(total: Int): String {
    val safe = total.coerceAtLeast(0)
    val m = safe / 60
    val s = safe % 60
    return "%d:%02d".format(m, s)
}
