package com.lodkin.dartstrainer.ui.aroundclock

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
import com.lodkin.dartstrainer.data.aroundclock.AroundClockOrder
import com.lodkin.dartstrainer.data.aroundclock.AroundClockTarget
import com.lodkin.dartstrainer.theme.Accent
import com.lodkin.dartstrainer.theme.DarkBg
import com.lodkin.dartstrainer.theme.ErrorColor
import com.lodkin.dartstrainer.theme.GoldAccent
import com.lodkin.dartstrainer.theme.TileBg
import com.lodkin.dartstrainer.theme.TileBgDark
import kotlin.random.Random

private val PaleYellow = Color(0xFFFFE082)
private val PaleYellowText = Color(0xFF3E2723)

private val HitGreen = Color(0xFF4CAF50)
private val MissRed = Color(0xFF8B2A2A)

// Секторы 1..20 + Bull (25)
private val ALL_SECTORS = (1..20).toList() + 25

private data class ThrowInApproach(
    val sector: Int,        // целевой сектор на момент броска
    val hit: Boolean
)

@Composable
fun AroundClockGameScreen(
    target: AroundClockTarget,
    order: AroundClockOrder,
    hitsRequired: Int,
    onFinish: (totalDarts: Int, completed: Boolean, sectorResults: Map<Int, Int>) -> Unit,
    onBack: () -> Unit
) {
    // Последовательность секторов для игры — фиксируется один раз при старте
    val sectorSequence = remember {
        if (order == AroundClockOrder.ORDERED) ALL_SECTORS
        else ALL_SECTORS.shuffled(Random(System.currentTimeMillis()))
    }

    // Все сделанные броски (история для откатов)
    val allThrows = remember { mutableStateListOf<ThrowInApproach>() }

    var showBackConfirm by remember { mutableStateOf(false) }
    var showFinishDialog by remember { mutableStateOf(false) }

    // ── Пересчёт состояния из истории бросков ──
    val state = remember(allThrows.size) {
        recalcState(allThrows.toList(), sectorSequence, hitsRequired)
    }

    val currentSector = state.currentSector
    val isFinished = state.finished

    // Автозапуск финального диалога
    LaunchedEffect(isFinished) {
        if (isFinished) showFinishDialog = true
    }

    // Текущий подход: последние броски после последнего кратного 3
    val throwsInCurrentApproach = allThrows.size % 3

    fun addThrow(hit: Boolean) {
        if (isFinished) return
        // Сектор на момент броска — тот, что сейчас активен
        allThrows.add(ThrowInApproach(sector = currentSector, hit = hit))
    }

    fun missAllRemaining() {
        if (isFinished) return
        val remaining = 3 - throwsInCurrentApproach
        repeat(remaining) {
            if (!isFinished) addThrow(hit = false)
        }
    }

    fun undo() {
        if (allThrows.isEmpty()) return
        allThrows.removeAt(allThrows.lastIndex)
        showFinishDialog = false
    }

    fun clearApproach() {
        if (throwsInCurrentApproach == 0) return
        repeat(throwsInCurrentApproach) {
            if (allThrows.isNotEmpty()) allThrows.removeAt(allThrows.lastIndex)
        }
        showFinishDialog = false
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBg)
    ) {
        // ── Шапка ──
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFF1A2332))
                .padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(10.dp))
                    .background(TileBg)
                    .clickable { showBackConfirm = true }
                    .padding(horizontal = 12.dp, vertical = 8.dp)
            ) {
                Text("← Назад", color = Accent, fontSize = 13.sp, fontWeight = FontWeight.Bold)
            }
            Spacer(Modifier.width(10.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    "Кругосветка",
                    color = Color.White,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    "${targetLabel(target)} · ${
                        if (order == AroundClockOrder.ORDERED) "по порядку" else "случайно"
                    } · попаданий: $hitsRequired",
                    color = Accent,
                    fontSize = 11.sp
                )
            }
            Column(horizontalAlignment = Alignment.End) {
                Text("ДРОТИКОВ", color = Color(0xFF99AABB), fontSize = 9.sp, letterSpacing = 1.sp)
                Text("${allThrows.size}", color = GoldAccent, fontSize = 22.sp, fontWeight = FontWeight.Bold)
            }
        }

        // ── Текущая цель ──
        if (!isFinished && currentSector != null) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFF16202C))
                    .padding(vertical = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    "ЦЕЛЬ",
                    color = Color(0xFF99AABB),
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 2.sp
                )
                Text(
                    sectorLabel(currentSector, target),
                    color = GoldAccent,
                    fontSize = 54.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    "попаданий: ${state.hitsInCurrent} / $hitsRequired",
                    color = Color.White.copy(alpha = 0.6f),
                    fontSize = 13.sp
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    "пройдено секторов: ${state.sectorsCompleted} / ${sectorSequence.size}",
                    color = Color.White.copy(alpha = 0.45f),
                    fontSize = 11.sp
                )
            }
        } else {
            // Финальный баннер
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFF16202C))
                    .padding(vertical = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    "КРУГ ПРОЙДЕН!",
                    color = GoldAccent,
                    fontSize = 26.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 2.sp
                )
                Spacer(Modifier.height(6.dp))
                Text(
                    "всего дротиков: ${allThrows.size}",
                    color = Color.White.copy(alpha = 0.7f),
                    fontSize = 14.sp
                )
            }
        }

        Spacer(Modifier.height(10.dp))

        // ── Три строки подхода ──
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            for (i in 0..2) {
                val globalIndex = (allThrows.size - throwsInCurrentApproach) + i
                val existing = if (globalIndex < allThrows.size) allThrows[globalIndex] else null

                ApproachRow(
                    sector = existing?.sector ?: currentSector,
                    target = target,
                    result = existing?.let { if (it.hit) RowResult.HIT else RowResult.MISS },
                    enabled = !isFinished && existing == null && i == throwsInCurrentApproach,
                    onHit = { addThrow(hit = true) },
                    onMiss = { addThrow(hit = false) }
                )
            }
        }

        Spacer(Modifier.height(10.dp))

        // ── Кнопки управления ──
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp)
                .weight(1f),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // aOK / Все мимо
            val canMissAll = !isFinished && throwsInCurrentApproach in 1..2
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(58.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(if (canMissAll) TileBg else TileBg.copy(alpha = 0.4f))
                    .clickable(enabled = canMissAll) { missAllRemaining() },
                contentAlignment = Alignment.Center
            ) {
                Text(
                    "aOK / Все мимо",
                    color = if (canMissAll) Color.White else Color.White.copy(alpha = 0.4f),
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Стереть
                val canClear = !isFinished && throwsInCurrentApproach > 0
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(52.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(if (canClear) TileBgDark else TileBgDark.copy(alpha = 0.4f))
                        .clickable(enabled = canClear) { clearApproach() },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        "⌫ Стереть",
                        color = if (canClear) Color.White else Color.White.copy(alpha = 0.4f),
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium
                    )
                }

                // Ход назад
                val canUndo = allThrows.isNotEmpty() && !showFinishDialog
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(52.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(if (canUndo) PaleYellow else PaleYellow.copy(alpha = 0.35f))
                        .clickable(enabled = canUndo) { undo() },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        "↶ Ход назад",
                        color = PaleYellowText,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        Spacer(Modifier.height(12.dp))
    }

    // ── Подтверждение выхода ──
    if (showBackConfirm) {
        AlertDialog(
            onDismissRequest = { showBackConfirm = false },
            confirmButton = {
                TextButton(onClick = {
                    showBackConfirm = false
                    onBack()
                }) { Text("ДА", color = ErrorColor) }
            },
            dismissButton = {
                TextButton(onClick = { showBackConfirm = false }) {
                    Text("НЕТ", color = Accent)
                }
            },
            title = { Text("Выйти из игры?", color = Color.White) },
            text = { Text("Прогресс не сохранится.", color = Color.White) }
        )
    }

    // ── Финал ──
    if (showFinishDialog) {
        AlertDialog(
            onDismissRequest = { },
            confirmButton = {
                TextButton(onClick = {
                    showFinishDialog = false
                    onFinish(
                        allThrows.size,
                        true,
                        state.sectorResults
                    )
                }) { Text("Продолжить", color = Accent) }
            },
            dismissButton = {
                TextButton(onClick = {
                    showFinishDialog = false
                    undo()
                }) { Text("↶ Отменить бросок", color = ErrorColor) }
            },
            title = {
                Text("КРУГ ПРОЙДЕН", color = Accent, fontSize = 22.sp, fontWeight = FontWeight.Bold)
            },
            text = {
                Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Всего дротиков:", color = Color.White, fontSize = 15.sp)
                        Text(
                            "${allThrows.size}",
                            color = GoldAccent,
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Spacer(Modifier.height(4.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Секторов пройдено:", color = Color.White, fontSize = 14.sp)
                        Text(
                            "${state.sectorsCompleted}",
                            color = Color.White,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Spacer(Modifier.height(14.dp))
                    Text(
                        "РАЗБИВКА ПО СЕКТОРАМ",
                        color = Accent,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 2.sp
                    )
                    Spacer(Modifier.height(6.dp))
                    state.sectorResults.entries.forEach { (sec, darts) ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 2.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                sectorLabel(sec, target),
                                color = Color.White,
                                fontSize = 13.sp
                            )
                            Text(
                                "$darts",
                                color = GoldAccent,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        )
    }
}

// ─────────────────────────────────────────────
// Строка подхода
// ─────────────────────────────────────────────
private enum class RowResult { HIT, MISS, EMPTY }

@Composable
private fun ApproachRow(
    sector: Int?,
    target: AroundClockTarget,
    result: RowResult?,
    enabled: Boolean,
    onHit: () -> Unit,
    onMiss: () -> Unit
) {
    val currentResult = result ?: RowResult.EMPTY
    val missBg = when (currentResult) {
        RowResult.MISS -> MissRed
        RowResult.HIT -> TileBg.copy(alpha = 0.5f)
        RowResult.EMPTY -> TileBg
    }
    val hitBg = when (currentResult) {
        RowResult.HIT -> HitGreen
        RowResult.MISS -> TileBg.copy(alpha = 0.5f)
        RowResult.EMPTY -> TileBg
    }
    val missEnabled = enabled && currentResult == RowResult.EMPTY
    val hitEnabled = enabled && currentResult == RowResult.EMPTY

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        // МИМО
        Box(
            modifier = Modifier
                .weight(1f)
                .height(64.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(if (missEnabled) missBg else missBg.copy(alpha = 0.6f))
                .clickable(enabled = missEnabled) { onMiss() },
            contentAlignment = Alignment.Center
        ) {
            Text(
                "МИМО",
                color = if (currentResult == RowResult.MISS) Color.White
                else Color.White.copy(alpha = if (missEnabled) 0.9f else 0.4f),
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )
        }

        // ЦЕЛЕВОЙ СЕКТОР
        val label = sector?.let { sectorLabel(it, target) } ?: "—"
        Box(
            modifier = Modifier
                .weight(1f)
                .height(64.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(if (hitEnabled) hitBg else hitBg.copy(alpha = 0.6f))
                .clickable(enabled = hitEnabled) { onHit() },
            contentAlignment = Alignment.Center
        ) {
            Text(
                label,
                color = if (currentResult == RowResult.HIT) Color.White
                else Color.White.copy(alpha = if (hitEnabled) 0.9f else 0.4f),
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

// ─────────────────────────────────────────────
// Пересчёт состояния из истории бросков
// ─────────────────────────────────────────────
private data class GameState(
    val currentSector: Int?,
    val hitsInCurrent: Int,
    val sectorsCompleted: Int,
    val sectorResults: Map<Int, Int>,  // потрачено дротиков на каждый сектор
    val finished: Boolean
)

private fun recalcState(
    history: List<ThrowInApproach>,
    sequence: List<Int>,
    hitsRequired: Int
): GameState {
    var targetIndex = 0
    var hitsInCurrent = 0
    var sectorsCompleted = 0
    val sectorResults = mutableMapOf<Int, Int>()

    for (t in history) {
        if (targetIndex >= sequence.size) break
        val currentSector = sequence[targetIndex]

        // Увеличиваем счётчик дротиков на текущий сектор
        sectorResults[currentSector] = (sectorResults[currentSector] ?: 0) + 1

        if (t.hit) {
            hitsInCurrent++
            if (hitsInCurrent >= hitsRequired) {
                // Сектор закрыт
                sectorsCompleted++
                targetIndex++
                hitsInCurrent = 0
            }
        }
    }

    val finished = targetIndex >= sequence.size
    val currentSector = if (finished) null else sequence[targetIndex]

    return GameState(
        currentSector = currentSector,
        hitsInCurrent = hitsInCurrent,
        sectorsCompleted = sectorsCompleted,
        sectorResults = sectorResults,
        finished = finished
    )
}

// ─────────────────────────────────────────────
// Метки секторов
// ─────────────────────────────────────────────
private fun sectorLabel(sector: Int, target: AroundClockTarget): String {
    if (sector == 25) {
        return when (target) {
            AroundClockTarget.SINGLE -> "Bull 25"
            AroundClockTarget.DOUBLE -> "Bull 50"
            AroundClockTarget.TRIPLE -> "Bull 50"
        }
    }
    return when (target) {
        AroundClockTarget.SINGLE -> "S$sector"
        AroundClockTarget.DOUBLE -> "D$sector"
        AroundClockTarget.TRIPLE -> "T$sector"
    }
}

private fun targetLabel(target: AroundClockTarget): String = when (target) {
    AroundClockTarget.SINGLE -> "Сектора"
    AroundClockTarget.DOUBLE -> "Удвоения"
    AroundClockTarget.TRIPLE -> "Утроения"
}
