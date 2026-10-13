package com.lodkin.dartstrainer.ui.sector

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
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
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.lodkin.dartstrainer.data.sector.SectorRepository
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
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.math.abs

private val PaleYellow = Color(0xFFFFE082)
private val PaleYellowText = Color(0xFF3E2723)

private val SECTOR_20_NORMS: List<Pair<String, Int>> = listOf(
    "II юношеский" to 360,
    "I юношеский" to 400,
    "III разряд" to 460,
    "II разряд" to 540,
    "I разряд" to 600,
    "КМС" to 720
)

private const val TOTAL_APPROACHES = 10
private const val AUTO_OK_SECONDS = 3
private const val MAX_HITS_SECTOR = 9
private const val MAX_HITS_BULL = 6

@Composable
fun SectorGameScreen(
    sector: Int,
    repository: SectorRepository,
    isTrainingMode: Boolean = false,
    trainingRepository: TrainingRepository? = null,
    trainingRecordKey: String? = null,
    onFinish: (Int) -> Unit,
    onBack: () -> Unit
) {
    val scope = rememberCoroutineScope()

    val approaches = remember { mutableStateListOf<Int>() }
    var inputText by remember { mutableStateOf("") }
    var autoOkActive by remember { mutableStateOf(false) }

    var showBackConfirm by remember { mutableStateOf(false) }
    var showFinishDialog by remember { mutableStateOf(false) }

    var bestRecord by remember { mutableStateOf<Int?>(null) }
    var recordsCount by remember { mutableStateOf(0) }

    val isBull = sector == 25
    val sectorLabel = if (isBull) "BULL" else "S$sector"
    val pointsPerHit = if (isBull) 25 else sector
    val maxHits = if (isBull) MAX_HITS_BULL else MAX_HITS_SECTOR

    val currentApproachNumber = approaches.size + 1
    val totalHits = approaches.sum()
    val totalScore = totalHits * pointsPerHit
    val isFinished = approaches.size >= TOTAL_APPROACHES

    LaunchedEffect(sector, isTrainingMode) {
        if (isTrainingMode && trainingRepository != null && !trainingRecordKey.isNullOrBlank()) {
            val rec = withContext(Dispatchers.IO) {
                trainingRepository.getTrainingRecord(trainingRecordKey)
            }
            bestRecord = rec?.bestScore
            recordsCount = if (rec == null) 0 else 1
        } else {
            val games = withContext(Dispatchers.IO) {
                repository.getGamesBySector(sector)
            }
            recordsCount = games.size
            bestRecord = games.maxOfOrNull { it.totalScore }
        }
    }

    fun isInputValid(text: String): Boolean {
        if (text.isBlank()) return false
        val v = text.toIntOrNull() ?: return false
        return v in 0..maxHits
    }

    fun submitApproach() {
        if (!isInputValid(inputText)) return
        val v = inputText.toInt()
        approaches.add(v)
        inputText = ""
        autoOkActive = false

        if (approaches.size >= TOTAL_APPROACHES) {
            showFinishDialog = true
        }
    }

    fun undoApproach() {
        if (approaches.isEmpty()) return
        approaches.removeAt(approaches.lastIndex)
        showFinishDialog = false
        autoOkActive = false
    }

    LaunchedEffect(inputText, approaches.size) {
        if (isInputValid(inputText) && !isFinished && !showFinishDialog) {
            autoOkActive = true
            delay(AUTO_OK_SECONDS * 1000L)
            if (isInputValid(inputText)) submitApproach()
        } else {
            autoOkActive = false
        }
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
                .padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(10.dp))
                    .background(TileBg)
                    .clickable { showBackConfirm = true }
                    .padding(horizontal = 14.dp, vertical = 8.dp)
            ) {
                Text("← Назад", color = Accent, fontSize = 14.sp, fontWeight = FontWeight.Bold)
            }
            Spacer(Modifier.width(12.dp))
            Text(
                "Сектор · $sectorLabel",
                color = Color.White,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.weight(1f)
            )
            Text(
                "$currentApproachNumber / $TOTAL_APPROACHES",
                color = Accent,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFF16202C))
                .padding(vertical = 4.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                "РЕЗУЛЬТАТ",
                color = Color(0xFF99AABB),
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 2.sp
            )
            Text(
                "$totalScore",
                color = GoldAccent,
                fontSize = 112.sp,
                fontWeight = FontWeight.Bold,
                lineHeight = 118.sp
            )
            Text(
                "попаданий: $totalHits",
                color = Color.White.copy(alpha = 0.6f),
                fontSize = 12.sp
            )
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 6.dp)
                .height(70.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(TileBgDark)
                .padding(horizontal = 16.dp),
            contentAlignment = Alignment.Center
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        "ПОПАДАНИЙ В $sectorLabel",
                        color = Accent,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 2.sp
                    )
                    Spacer(Modifier.height(2.dp))
                    Text(
                        if (isBull) "25 = 1 · 50 = 2" else "S=1 · D=2 · T=3",
                        color = Color.White.copy(alpha = 0.4f),
                        fontSize = 10.sp
                    )
                    if (autoOkActive && isInputValid(inputText)) {
                        Spacer(Modifier.height(2.dp))
                        Text(
                            "авто-OK через $AUTO_OK_SECONDS сек",
                            color = Accent.copy(alpha = 0.7f),
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
                Text(
                    if (inputText.isEmpty()) "—" else inputText,
                    color = if (inputText.isEmpty()) Color.White.copy(alpha = 0.3f) else GoldAccent,
                    fontSize = 40.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp)
                .weight(1f)
        ) {
            val keyboardEnabled = !isFinished
            val highDigitsEnabled = keyboardEnabled && !isBull

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                DigitKey("1", keyboardEnabled) { inputText = appendDigit(inputText, "1") }
                DigitKey("2", keyboardEnabled) { inputText = appendDigit(inputText, "2") }
                DigitKey("3", keyboardEnabled) { inputText = appendDigit(inputText, "3") }
            }
            Spacer(Modifier.height(6.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                DigitKey("4", keyboardEnabled) { inputText = appendDigit(inputText, "4") }
                DigitKey("5", keyboardEnabled) { inputText = appendDigit(inputText, "5") }
                DigitKey("6", keyboardEnabled) { inputText = appendDigit(inputText, "6") }
            }
            Spacer(Modifier.height(6.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                DigitKey("7", highDigitsEnabled) { inputText = appendDigit(inputText, "7") }
                DigitKey("8", highDigitsEnabled) { inputText = appendDigit(inputText, "8") }
                DigitKey("9", highDigitsEnabled) { inputText = appendDigit(inputText, "9") }
            }
            Spacer(Modifier.height(6.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(58.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(TileBgDark)
                        .clickable(enabled = keyboardEnabled && inputText.isNotEmpty()) {
                            if (inputText.isNotEmpty()) inputText = inputText.dropLast(1)
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        "⌫",
                        color = if (inputText.isNotEmpty()) Color.White else Color.White.copy(alpha = 0.3f),
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                DigitKey("0", keyboardEnabled) { inputText = appendDigit(inputText, "0") }

                val okEnabled = keyboardEnabled && isInputValid(inputText)
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(58.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(if (okEnabled) Accent else Accent.copy(alpha = 0.35f))
                        .clickable(enabled = okEnabled) { submitApproach() },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        "OK",
                        color = if (okEnabled) Color(0xFF121212) else Color(0xFF121212).copy(alpha = 0.5f),
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(Modifier.height(8.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(46.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(if (approaches.isNotEmpty()) PaleYellow else PaleYellow.copy(alpha = 0.35f))
                    .clickable(enabled = approaches.isNotEmpty() && !showFinishDialog) { undoApproach() },
                contentAlignment = Alignment.Center
            ) {
                Text(
                    "↶ ХОД НАЗАД",
                    color = PaleYellowText,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(Modifier.height(8.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(42.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(TileBgDark)
                    .padding(horizontal = 10.dp),
                contentAlignment = Alignment.CenterStart
            ) {
                if (approaches.isEmpty()) {
                    Text(
                        "Пока пусто",
                        color = Color.White.copy(alpha = 0.4f),
                        fontSize = 11.sp
                    )
                } else {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        approaches.forEach { hits ->
                            Box(
                                modifier = Modifier
                                    .size(32.dp)
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(if (hits > 0) TileBg else Color(0xFF2A2A2A)),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text(
                                        "$hits",
                                        color = Color.White,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        "${hits * pointsPerHit}",
                                        color = Accent,
                                        fontSize = 8.sp,
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        Spacer(Modifier.height(8.dp))
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

    if (showFinishDialog) {
        val score = totalScore
        val prevRecord = bestRecord
        val isFirstEver = recordsCount == 0

        val isNewRecord = !isFirstEver && prevRecord != null && score > prevRecord
        val isAlmostRecord = !isFirstEver && !isNewRecord && prevRecord != null && prevRecord > 0 &&
                abs(prevRecord - score).toFloat() / prevRecord <= 0.10f
        val missingPoints = if (prevRecord != null) prevRecord - score else 0

        val recordScale by animateFloatAsState(
            targetValue = if (isNewRecord) 1f else 0.85f,
            animationSpec = tween(durationMillis = 500),
            label = "record_scale"
        )

        // Праздничный оверлей: конфетти + салют поверх диалога.
        CelebrationOverlay(visible = isNewRecord, durationMillis = 3000L)

        val achievedIdx = SECTOR_20_NORMS.indices.reversed()
            .firstOrNull { score >= SECTOR_20_NORMS[it].second }
        val achieved = if (achievedIdx != null) SECTOR_20_NORMS[achievedIdx] else null
        val next = if (achievedIdx != null && achievedIdx < SECTOR_20_NORMS.size - 1)
            SECTOR_20_NORMS[achievedIdx + 1] else null

        AlertDialog(
            onDismissRequest = { },
            containerColor = TileBgDark,
            confirmButton = {
                TextButton(onClick = {
                    showFinishDialog = false
                    if (!isTrainingMode) {
                        scope.launch {
                            repository.saveGame(
                                sector = sector,
                                totalScore = totalScore,
                                totalHits = totalHits,
                                approaches = approaches.toList()
                            )
                        }
                    }
                    onFinish(score)
                }) { Text("Продолжить", color = Accent) }
            },
            dismissButton = {
                TextButton(onClick = {
                    showFinishDialog = false
                    undoApproach()
                }) { Text("↶ Отменить подход", color = ErrorColor) }
            },
            title = {
                Text("ИГРА ЗАВЕРШЕНА", color = Accent, fontSize = 20.sp, fontWeight = FontWeight.Bold)
            },
            text = {
                Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                    if (isNewRecord && prevRecord != null) {
                        CelebrationRecordBlock(
                            title = "🏆 НОВЫЙ РЕКОРД!",
                            value = "$score очков",
                            prevValue = "$prevRecord очков",
                            scale = recordScale
                        )
                        Spacer(Modifier.height(14.dp))
                    } else if (isAlmostRecord && prevRecord != null) {
                        AlmostRecordBlock(
                            text = "До нового рекорда не хватило $missingPoints очков",
                            bestValue = "$prevRecord очков"
                        )
                        Spacer(Modifier.height(14.dp))
                    } else if (isFirstEver) {
                        FirstResultBlock()
                        Spacer(Modifier.height(14.dp))
                    }

                    Text(
                        "Сектор: $sectorLabel",
                        color = Color.White.copy(alpha = 0.7f),
                        fontSize = 13.sp
                    )
                    Spacer(Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Попаданий:", color = Color.White, fontSize = 15.sp)
                        Text(
                            "$totalHits",
                            color = Color.White,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Spacer(Modifier.height(4.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Всего очков:", color = Color.White, fontSize = 15.sp)
                        Text(
                            "$score",
                            color = GoldAccent,
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    if (sector == 20) {
                        Spacer(Modifier.height(14.dp))
                        Text(
                            "НОРМАТИВ",
                            color = Accent,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 2.sp
                        )
                        Spacer(Modifier.height(6.dp))

                        if (achieved != null) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(TileBg)
                                    .padding(10.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    achieved.first,
                                    color = GoldAccent,
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    "${achieved.second}+",
                                    color = GoldAccent,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            if (next != null) {
                                Spacer(Modifier.height(6.dp))
                                Text(
                                    "До норматива «${next.first}» не хватило ${next.second - score} очков",
                                    color = Color.White.copy(alpha = 0.6f),
                                    fontSize = 12.sp
                                )
                            } else {
                                Spacer(Modifier.height(6.dp))
                                Text(
                                    "Максимальный норматив достигнут!",
                                    color = GoldAccent.copy(alpha = 0.85f),
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        } else {
                            val lowest = SECTOR_20_NORMS.first()
                            Text(
                                "Пока без разряда. До «${lowest.first}» не хватило ${lowest.second - score} очков.",
                                color = Color.White.copy(alpha = 0.6f),
                                fontSize = 12.sp
                            )
                        }
                    }
                }
            }
        )
    }
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
        Text(
            "🎉  ✨  🎊",
            fontSize = 22.sp,
            textAlign = TextAlign.Center
        )
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

private fun appendDigit(current: String, digit: String): String {
    if (current.length >= 2) return current
    if (current == "0") return digit
    return current + digit
}

@Composable
private fun RowScope.DigitKey(
    digit: String,
    enabled: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .weight(1f)
            .height(58.dp)
            .clip(RoundedCornerShape(10.dp))
            .background(if (enabled) TileBg else TileBg.copy(alpha = 0.4f))
            .clickable(enabled = enabled) { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Text(
            digit,
            color = if (enabled) Color.White else Color.White.copy(alpha = 0.3f),
            fontSize = 26.sp,
            fontWeight = FontWeight.Bold
        )
    }
}
