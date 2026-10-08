package com.lodkin.dartstrainer.ui.scoreset

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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.lodkin.dartstrainer.data.scoreset.ScoreSetRepository
import com.lodkin.dartstrainer.theme.Accent
import com.lodkin.dartstrainer.theme.DarkBg
import com.lodkin.dartstrainer.theme.ErrorColor
import com.lodkin.dartstrainer.theme.GoldAccent
import com.lodkin.dartstrainer.theme.TileBg
import com.lodkin.dartstrainer.theme.TileBgDark
import kotlinx.coroutines.delay

private val PaleYellow = Color(0xFFFFE082)
private val PaleYellowText = Color(0xFF3E2723)

private const val TOTAL_APPROACHES = 10
private const val AUTO_OK_SECONDS = 3
private const val MAX_APPROACH_SCORE = 180

private val SCORESET_NORMS: List<Pair<String, Int>> = listOf(
    "III юношеский" to 390,
    "II юношеский" to 430,
    "I юношеский" to 470,
    "III разряд" to 510,
    "II разряд" to 560,
    "I разряд" to 660,
    "КМС" to 760
)

@Composable
fun ScoreSetGameScreen(
    repository: ScoreSetRepository,
    onFinish: (totalScore: Int, approaches: List<Int>) -> Unit,
    onBack: () -> Unit
) {
    val approaches = remember { mutableStateListOf<Int>() }
    var inputText by remember { mutableStateOf("") }
    var autoOkActive by remember { mutableStateOf(false) }

    var showBackConfirm by remember { mutableStateOf(false) }
    var showFinishDialog by remember { mutableStateOf(false) }

    val currentApproachNumber = approaches.size + 1
    val totalScore = approaches.sum()
    val isFinished = approaches.size >= TOTAL_APPROACHES

    fun isInputValid(text: String): Boolean {
        if (text.isBlank()) return false
        val v = text.toIntOrNull() ?: return false
        return v in 0..MAX_APPROACH_SCORE
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
                "Набор очков",
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

        // ── Счёт (увеличен ×2) ──
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
                "сред. набор: ${if (approaches.isEmpty()) "—" else "%.1f".format(totalScore.toDouble() / approaches.size)}",
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
                        "СУММА ЗА ПОДХОД",
                        color = Accent,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 2.sp
                    )
                    Spacer(Modifier.height(2.dp))
                    Text(
                        "от 0 до 180",
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
                DigitKey("7", keyboardEnabled) { inputText = appendDigit(inputText, "7") }
                DigitKey("8", keyboardEnabled) { inputText = appendDigit(inputText, "8") }
                DigitKey("9", keyboardEnabled) { inputText = appendDigit(inputText, "9") }
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
                        approaches.forEach { v ->
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(if (v > 0) TileBg else Color(0xFF2A2A2A)),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    "$v",
                                    color = Color.White,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
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
        val achievedIdx = SCORESET_NORMS.indices.reversed()
            .firstOrNull { score >= SCORESET_NORMS[it].second }
        val achieved = if (achievedIdx != null) SCORESET_NORMS[achievedIdx] else null
        val next = if (achievedIdx != null && achievedIdx < SCORESET_NORMS.size - 1)
            SCORESET_NORMS[achievedIdx + 1] else null

        AlertDialog(
            onDismissRequest = { },
            confirmButton = {
                TextButton(onClick = {
                    showFinishDialog = false
                    onFinish(totalScore, approaches.toList())
                }) { Text("Продолжить", color = Accent) }
            },
            dismissButton = {
                TextButton(onClick = {
                    showFinishDialog = false
                    undoApproach()
                }) { Text("↶ Отменить подход", color = ErrorColor) }
            },
            title = {
                Text("ИГРА ЗАВЕРШЕНА", color = Accent, fontSize = 22.sp, fontWeight = FontWeight.Bold)
            },
            text = {
                Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
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
                    Spacer(Modifier.height(4.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Средний набор:", color = Color.White, fontSize = 14.sp)
                        Text(
                            "%.1f".format(score.toDouble() / TOTAL_APPROACHES),
                            color = Color.White,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

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
                        val lowest = SCORESET_NORMS.first()
                        Text(
                            "Пока без разряда. До «${lowest.first}» не хватило ${lowest.second - score} очков.",
                            color = Color.White.copy(alpha = 0.6f),
                            fontSize = 12.sp
                        )
                    }
                }
            }
        )
    }
}

private fun appendDigit(current: String, digit: String): String {
    if (current.length >= 3) return current
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
