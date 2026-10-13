package com.lodkin.dartstrainer.ui.aroundclock

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import com.lodkin.dartstrainer.data.aroundclock.AroundClockOrder
import com.lodkin.dartstrainer.data.aroundclock.AroundClockRepository
import com.lodkin.dartstrainer.data.aroundclock.AroundClockTarget
import com.lodkin.dartstrainer.data.training.TrainingRepository
import com.lodkin.dartstrainer.theme.Accent
import com.lodkin.dartstrainer.theme.DarkBg
import com.lodkin.dartstrainer.theme.ErrorColor
import com.lodkin.dartstrainer.theme.GoldAccent
import com.lodkin.dartstrainer.theme.TileBg
import com.lodkin.dartstrainer.theme.TileBgDark
import com.lodkin.dartstrainer.ui.common.CelebrationOverlay
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import kotlin.math.abs
import kotlin.random.Random

private val PaleYellow = Color(0xFFFFE082)
private val PaleYellowText = Color(0xFF3E2723)

private val HitGreen = Color(0xFF4CAF50)
private val MissRed = Color(0xFF8B2A2A)

private data class ThrowInApproach(
    val sector: Int,
    val hit: Boolean
)

/**
 * @param sectorStart          С какого сектора начинать. 1 — полная версия,
 *                             10 — укороченная (используется для утроений).
 * @param trainingRepository   для чтения/обновления тренировочного рекорда.
 * @param trainingRecordKey    ключ рекорда в training_records.
 */
@Composable
fun AroundClockGameScreen(
    target: AroundClockTarget,
    order: AroundClockOrder,
    hitsRequired: Int,
    repository: AroundClockRepository,
    sectorStart: Int = 1,
    trainingRepository: TrainingRepository? = null,
    trainingRecordKey: String? = null,
    onFinish: (totalDarts: Int, completed: Boolean, sectorResults: Map<Int, Int>) -> Unit,
    onBack: () -> Unit
) {
    val fullVersion = sectorStart <= 1
    val isTrainingMode = trainingRepository != null && !trainingRecordKey.isNullOrBlank()

    val sectorSequence = remember(target, order, sectorStart) {
        val sectors = (sectorStart..20).toList() + 25
        if (order == AroundClockOrder.ORDERED) sectors
        else sectors.shuffled(Random(System.currentTimeMillis()))
    }

    val allThrows = remember { mutableStateListOf<ThrowInApproach>() }

    var showBackConfirm by remember { mutableStateOf(false) }
    var showFinishDialog by remember { mutableStateOf(false) }
    var bestRecord by remember { mutableStateOf<Int?>(null) }
    var recordsCount by remember { mutableStateOf(0) }
    var flashMiss by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        if (!fullVersion) return@LaunchedEffect
        if (isTrainingMode) {
            val rec = withContext(Dispatchers.IO) {
                trainingRepository!!.getTrainingRecord(trainingRecordKey!!)
            }
            bestRecord = rec?.bestDarts?.takeIf { it > 0 }
            recordsCount = if (rec == null) 0 else 1
        } else {
            val games = withContext(Dispatchers.IO) {
                repository.getAllGames()
            }
            val matching = games.filter {
                it.completed &&
                it.targetName == target.name &&
                it.orderName == order.name &&
                it.hitsRequired == hitsRequired
            }
            recordsCount = matching.size
            bestRecord = matching.minOfOrNull { it.totalDarts }
        }
    }

    val state = remember(allThrows.size) {
        recalcState(allThrows.toList(), sectorSequence, hitsRequired)
    }

    val currentSectorOrNull: Int? = state.currentSector
    val isFinished: Boolean = state.finished

    LaunchedEffect(isFinished) {
        if (isFinished) showFinishDialog = true
    }

    val throwsInCurrentApproach = allThrows.size % 3

    LaunchedEffect(flashMiss) {
        if (flashMiss) {
            delay(700L)
            if (!isFinished) {
                val remaining = 3 - throwsInCurrentApproach
                repeat(remaining) {
                    if (!isFinished) {
                        val cs = currentSectorOrNull ?: return@repeat
                        allThrows.add(ThrowInApproach(sector = cs, hit = false))
                    }
                }
            }
            flashMiss = false
        }
    }

    fun addThrow(hit: Boolean) {
        if (isFinished || flashMiss) return
        val cs = currentSectorOrNull ?: return
        allThrows.add(ThrowInApproach(sector = cs, hit = hit))
    }

    fun missAllRemaining() {
        if (isFinished || flashMiss) return
        flashMiss = true
    }

    fun undo() {
        if (allThrows.isEmpty() || flashMiss) return
        allThrows.removeAt(allThrows.lastIndex)
        showFinishDialog = false
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBg)
    ) {
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
                    .padding(horizontal = 14.dp, vertical = 10.dp)
            ) {
                Text("← Назад", color = Accent, fontSize = 14.sp, fontWeight = FontWeight.Bold)
            }
            Spacer(Modifier.width(8.dp))
            Column(
                modifier = Modifier.weight(1f),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    "Кругосветка",
                    color = Color.White,
                    fontSize = 27.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )
                Text(
                    "${targetLabel(target)} · ${
                        if (order == AroundClockOrder.ORDERED) "по порядку" else "случайно"
                    } · попаданий: $hitsRequired",
                    color = Accent,
                    fontSize = 12.sp,
                    textAlign = TextAlign.Center
                )
            }
            Spacer(Modifier.width(8.dp))
            Column(horizontalAlignment = Alignment.End) {
                Text("ДРОТИКОВ", color = Color(0xFF99AABB), fontSize = 10.sp, letterSpacing = 1.sp)
                Text("${allThrows.size}", color = GoldAccent, fontSize = 26.sp, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(4.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    for (i in 0..2) {
                        val filled = i < throwsInCurrentApproach || flashMiss
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(if (filled) Accent else Color.White.copy(alpha = 0.2f))
                        )
                    }
                }
            }
        }

        SectorProgressMap(
            sequence = sectorSequence,
            currentSector = currentSectorOrNull,
            isFinished = isFinished
        )

        if (!isFinished && currentSectorOrNull != null) {
            val cs = currentSectorOrNull
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFF16202C))
                    .padding(vertical = 6.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    "ЦЕЛЬ",
                    color = Color(0xFF99AABB),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 3.sp
                )
                Text(
                    sectorLabel(cs, target),
                    color = GoldAccent,
                    fontSize = 116.sp,
                    fontWeight = FontWeight.Bold,
                    lineHeight = 122.sp
                )
                Text(
                    "попаданий: ${state.hitsInCurrent} / $hitsRequired",
                    color = Color.White.copy(alpha = 0.6f),
                    fontSize = 14.sp
                )
                Spacer(Modifier.height(4.dp))
                if (fullVersion) {
                    if (bestRecord != null) {
                        Text(
                            "🏆 лучший результат: $bestRecord дротиков",
                            color = GoldAccent.copy(alpha = 0.75f),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium
                        )
                    } else {
                        Text(
                            "рекорда ещё нет — установи первый!",
                            color = Color.White.copy(alpha = 0.4f),
                            fontSize = 11.sp
                        )
                    }
                } else {
                    Text(
                        "укороченная версия",
                        color = Color.White.copy(alpha = 0.4f),
                        fontSize = 11.sp
                    )
                }
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFF16202C))
                    .padding(vertical = 18.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    "КРУГ ПРОЙДЕН!",
                    color = GoldAccent,
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 2.sp
                )
                Spacer(Modifier.height(6.dp))
                Text(
                    "всего дротиков: ${allThrows.size}",
                    color = Color.White.copy(alpha = 0.7f),
                    fontSize = 15.sp
                )
            }
        }

        Spacer(Modifier.height(6.dp))

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            for (i in 0..2) {
                val globalIndex = (allThrows.size - throwsInCurrentApproach) + i
                val existing: ThrowInApproach? =
                    if (globalIndex < allThrows.size) allThrows[globalIndex] else null

                val rowSector: Int? = existing?.sector ?: currentSectorOrNull
                val previewMiss = flashMiss && existing == null && i >= throwsInCurrentApproach

                ApproachRow(
                    sector = rowSector,
                    target = target,
                    result = existing?.let { if (it.hit) RowResult.HIT else RowResult.MISS },
                    enabled = !isFinished && !flashMiss && existing == null && i == throwsInCurrentApproach,
                    previewMiss = previewMiss,
                    onHit = { addThrow(hit = true) },
                    onMiss = { addThrow(hit = false) }
                )
            }
        }

        Spacer(Modifier.height(8.dp))

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp)
                .weight(1f),
            verticalArrangement = Arrangement.Bottom
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                val canMissAll = !isFinished && !flashMiss
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(78.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(if (canMissAll) TileBgDark else TileBgDark.copy(alpha = 0.4f))
                        .clickable(enabled = canMissAll) { missAllRemaining() },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        "Все мимо",
                        color = if (canMissAll) Color.White else Color.White.copy(alpha = 0.4f),
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                val canUndo = allThrows.isNotEmpty() && !showFinishDialog && !flashMiss
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(78.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(if (canUndo) PaleYellow else PaleYellow.copy(alpha = 0.35f))
                        .clickable(enabled = canUndo) { undo() },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        "↶ Ход назад",
                        color = PaleYellowText,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
            Spacer(Modifier.height(12.dp))
        }
    }

    if (showBackConfirm) {
        AlertDialog(
            onDismissRequest = { showBackConfirm = false },
            containerColor = TileBgDark,
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

    // ── Итоги + анимация ──
    val prevRecord = bestRecord
    val isFirstEver = fullVersion && recordsCount == 0
    val currentDarts = allThrows.size

    val isNewRecord = fullVersion && !isFirstEver && prevRecord != null && currentDarts < prevRecord
    val isAlmostRecord = fullVersion && !isFirstEver && !isNewRecord && prevRecord != null && prevRecord > 0 &&
            abs(prevRecord - currentDarts).toFloat() / prevRecord <= 0.10f
    val missingDarts = if (prevRecord != null) currentDarts - prevRecord else 0

    val recordScale by animateFloatAsState(
        targetValue = if (isNewRecord) 1f else 0.85f,
        animationSpec = tween(durationMillis = 500),
        label = "record_scale"
    )

    if (showFinishDialog) {
        CelebrationOverlay(
            visible = true,
            isNewRecord = isNewRecord,
            onConfirm = {
                showFinishDialog = false
                if (!isTrainingMode) {
                    // Игровая БД сохраняется вызывающей стороной (MainActivity) —
                    // здесь не дублируем.
                }
                onFinish(allThrows.size, true, state.sectorResults)
            },
            onUndo = {
                showFinishDialog = false
                undo()
            }
        ) {
            Text(
                "КРУГ ПРОЙДЕН",
                color = Accent,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp
            )
            Spacer(Modifier.height(12.dp))

            if (isNewRecord && prevRecord != null) {
                CelebrationRecordBlock(
                    title = "🏆 НОВЫЙ РЕКОРД!",
                    value = "$currentDarts дротиков",
                    prevValue = "$prevRecord дротиков",
                    scale = recordScale
                )
                Spacer(Modifier.height(14.dp))
            } else if (isAlmostRecord && prevRecord != null) {
                AlmostRecordBlock(
                    text = "До нового рекорда не хватило $missingDarts дротиков",
                    bestValue = "$prevRecord дротиков"
                )
                Spacer(Modifier.height(14.dp))
            } else if (isFirstEver) {
                FirstResultBlock()
                Spacer(Modifier.height(14.dp))
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Всего дротиков:", color = Color.White, fontSize = 15.sp)
                Text(
                    "$currentDarts",
                    color = GoldAccent,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold
                )
            }
            if (fullVersion && prevRecord != null) {
                Spacer(Modifier.height(4.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Твой рекорд:", color = Color.White.copy(alpha = 0.6f), fontSize = 13.sp)
                    Text(
                        "$prevRecord",
                        color = Color.White.copy(alpha = 0.6f),
                        fontSize = 13.sp
                    )
                }
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
                    Text(sectorLabel(sec, target), color = Color.White, fontSize = 13.sp)
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
}

@Composable
private fun SectorProgressMap(
    sequence: List<Int>,
    currentSector: Int?,
    isFinished: Boolean
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFF0D1117))
            .padding(horizontal = 8.dp, vertical = 6.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(3.dp)
        ) {
            sequence.forEach { sec ->
                val isPast = when {
                    isFinished -> true
                    currentSector == null -> false
                    else -> sequence.indexOf(sec) < sequence.indexOf(currentSector)
                }
                val isCurrent = !isFinished && sec == currentSector

                val bg = when {
                    isCurrent -> Accent
                    isPast -> HitGreen
                    else -> TileBgDark
                }
                val fg = when {
                    isCurrent || isPast -> Color(0xFF121212)
                    else -> Color.White.copy(alpha = 0.5f)
                }
                val label = if (sec == 25) "B" else "$sec"

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(26.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(bg),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        label,
                        color = fg,
                        fontSize = 10.sp,
                        fontWeight = if (isCurrent || isPast) FontWeight.Bold else FontWeight.Normal
                    )
                }
            }
        }
    }
}

private enum class RowResult { HIT, MISS, EMPTY }

@Composable
private fun ApproachRow(
    sector: Int?,
    target: AroundClockTarget,
    result: RowResult?,
    enabled: Boolean,
    previewMiss: Boolean = false,
    onHit: () -> Unit,
    onMiss: () -> Unit
) {
    val currentResult = result ?: RowResult.EMPTY

    val missBg = when {
        previewMiss -> MissRed
        currentResult == RowResult.MISS -> MissRed
        currentResult == RowResult.HIT -> TileBg.copy(alpha = 0.5f)
        else -> TileBg
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
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Box(
            modifier = Modifier
                .weight(1f)
                .height(66.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(if (missEnabled || previewMiss) missBg else missBg.copy(alpha = 0.6f))
                .clickable(enabled = missEnabled) { onMiss() },
            contentAlignment = Alignment.Center
        ) {
            Text(
                "ПРОМАХ",
                color = when {
                    previewMiss -> Color.White
                    currentResult == RowResult.MISS -> Color.White
                    else -> Color.White.copy(alpha = if (missEnabled) 0.9f else 0.4f)
                },
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )
        }

        val label = sector?.let { sectorLabel(it, target) } ?: "—"
        Box(
            modifier = Modifier
                .weight(1f)
                .height(66.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(if (hitEnabled) hitBg else hitBg.copy(alpha = 0.6f))
                .clickable(enabled = hitEnabled) { onHit() },
            contentAlignment = Alignment.Center
        ) {
            Text(
                label,
                color = if (currentResult == RowResult.HIT) Color.White
                else Color.White.copy(alpha = if (hitEnabled) 0.9f else 0.4f),
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

private data class GameState(
    val currentSector: Int?,
    val hitsInCurrent: Int,
    val sectorsCompleted: Int,
    val sectorResults: Map<Int, Int>,
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
        val cur = sequence[targetIndex]

        sectorResults[cur] = (sectorResults[cur] ?: 0) + 1

        if (t.hit) {
            hitsInCurrent++
            if (hitsInCurrent >= hitsRequired) {
                sectorsCompleted++
                targetIndex++
                hitsInCurrent = 0
            }
        }
    }

    val finished = targetIndex >= sequence.size
    val currentSector: Int? = if (finished) null else sequence[targetIndex]

    return GameState(
        currentSector = currentSector,
        hitsInCurrent = hitsInCurrent,
        sectorsCompleted = sectorsCompleted,
        sectorResults = sectorResults,
        finished = finished
    )
}

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

@Composable
private fun CelebrationRecordBlock(
    title: String,
    value: String,
    prevValue: String,
    scale: Float
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(GoldAccent.copy(alpha = 0.15f))
            .padding(14.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("🎉  ✨  🎊", fontSize = 22.sp, textAlign = TextAlign.Center)
        Spacer(Modifier.height(6.dp))
        Text(
            title,
            color = GoldAccent,
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.sp,
            textAlign = TextAlign.Center,
            modifier = Modifier.scale(scale)
        )
        Spacer(Modifier.height(8.dp))
        Text(
            value,
            color = GoldAccent,
            fontSize = 32.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )
        Spacer(Modifier.height(4.dp))
        Text(
            "прошлый: $prevValue",
            color = Color.White.copy(alpha = 0.55f),
            fontSize = 12.sp,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun AlmostRecordBlock(text: String, bestValue: String) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(Accent.copy(alpha = 0.10f))
            .padding(14.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            "💪 Почти рекорд",
            color = Accent,
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )
        Spacer(Modifier.height(4.dp))
        Text(
            text,
            color = Color.White.copy(alpha = 0.85f),
            fontSize = 13.sp,
            textAlign = TextAlign.Center
        )
        Spacer(Modifier.height(4.dp))
        Text(
            "рекорд: $bestValue",
            color = Color.White.copy(alpha = 0.5f),
            fontSize = 11.sp,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun FirstResultBlock() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(TileBg.copy(alpha = 0.7f))
            .padding(12.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            "Твой результат сохранён",
            color = Color.White.copy(alpha = 0.85f),
            fontSize = 13.sp,
            textAlign = TextAlign.Center
        )
        Spacer(Modifier.height(2.dp))
        Text(
            "Рекорд появится со следующей игры",
            color = Color.White.copy(alpha = 0.5f),
            fontSize = 11.sp,
            textAlign = TextAlign.Center
        )
    }
}
