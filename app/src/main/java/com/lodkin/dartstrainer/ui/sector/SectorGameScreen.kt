package com.lodkin.dartstrainer.ui.sector

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
import com.lodkin.dartstrainer.theme.Accent
import com.lodkin.dartstrainer.theme.DarkBg
import com.lodkin.dartstrainer.theme.ErrorColor
import com.lodkin.dartstrainer.theme.GoldAccent
import com.lodkin.dartstrainer.theme.TileBg
import com.lodkin.dartstrainer.theme.TileBgDark

private val PaleYellow = Color(0xFFFFE082)
private val PaleYellowText = Color(0xFF3E2723)

private val SECTOR_20_NORMS: List<Pair<String, Int>> = listOf(
    "II юношеский" to 360,
    "I юношеский" to 400,
    "III разряд" to 460,
    "II разряд" to 540,
    "I разряд" to 600,
    "КМС" to 760,
    "МС" to 960
)

private const val TOTAL_APPROACHES = 10
private const val MAX_HITS_PER_APPROACH = 9 // 3 утроения = 9 попаданий

@Composable
fun SectorGameScreen(
    sector: Int,
    onFinish: (Int) -> Unit,
    onBack: () -> Unit
) {
    // Список попаданий за каждый завершённый подход
    val approaches = remember { mutableStateListOf<Int>() }
    var inputText by remember { mutableStateOf("") }

    var showBackConfirm by remember { mutableStateOf(false) }
    var showFinishDialog by remember { mutableStateOf(false) }

    val isBull = sector == 25
    val sectorLabel = if (isBull) "BULL" else "S$sector"
    val pointsPerHit = if (isBull) 25 else sector

    val currentApproachNumber = approaches.size + 1
    val totalHits = approaches.sum()
    val totalScore = totalHits * pointsPerHit
    val isFinished = approaches.size >= TOTAL_APPROACHES

    // Валидность: число 0..9
    fun isInputValid(text: String): Boolean {
        if (text.isBlank()) return false
        val v = text.toIntOrNull() ?: return false
        return v in 0..MAX_HITS_PER_APPROACH
    }

    fun submitApproach() {
        if (!isInputValid(inputText)) return
        val v = inputText.toInt()
        approaches.add(v)
        inputText = ""

        if (approaches.size >= TOTAL_APPROACHES) {
            showFinishDialog = true
        }
    }

    fun undoApproach() {
        if (approaches.isEmpty()) return
        approaches.removeAt(approaches.lastIndex)
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

        // ── Счёт ──
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFF16202C))
                .padding(vertical = 14.dp),
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
                fontSize = 56.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                "попаданий: $totalHits",
                color = Color.White.copy(alpha = 0.6f),
                fontSize = 13.sp
            )
        }

        // ── Поле ввода ──
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 10.dp)
                .height(80.dp)
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
                        "S=1, D=2, T=3",
                        color = Color.White.copy(alpha = 0.4f),
                        fontSize = 10.sp
                    )
                }
                Text(
                    if (inputText.isEmpty()) "—" else inputText,
                    color = if (inputText.isEmpty()) Color.White.copy(alpha = 0.3f) else GoldAccent,
                    fontSize = 42.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        // ── Клавиатура ──
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
                        .height(64.dp)
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
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                DigitKey("0", keyboardEnabled) { inputText = appendDigit(inputText, "0") }

                val okEnabled = keyboardEnabled && isInputValid(inputText)
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(64.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(if (okEnabled) Accent else Accent.copy(alpha = 0.35f))
                        .clickable(enabled = okEnabled) { submitApproach() },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        "OK",
                        color = if (okEnabled) Color(0xFF121212) else Color(0xFF121212).copy(alpha = 0.5f),
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(Modifier.height(8.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(if (approaches.isNotEmpty()) PaleYellow else PaleYellow.copy(alpha = 0.35f))
                    .clickable(enabled = approaches.isNotEmpty() && !showFinishDialog) {
                        undoApproach()
                    },
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

            // История подходов (в попаданиях)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(44.dp)
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

        Spacer(Modifier.height(12.dp))
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
        val bestRank = if (sector == 20) {
            SECTOR_20_NORMS.reversed().firstOrNull { score >= it.second }
        } else null
        val nextRank = if (sector == 20 && bestRank != SECTOR_20_NORMS.lastOrNull()) {
            SECTOR_20_NORMS.reversed().firstOrNull { score < it.second }
        } else null

        AlertDialog(
            onDismissRequest = { },
            confirmButton = {
                TextButton(onClick = {
                    showFinishDialog = false
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
                Text("ИГРА ЗАВЕРШЕНА", color = Accent, fontSize = 22.sp, fontWeight = FontWeight.Bold)
            },
            text = {
                Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
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
                            fontSize = 20.sp,
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
                            "РАЗРЯД",
                            color = Accent,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 2.sp
                        )
                        Spacer(Modifier.height(6.dp))
                        if (bestRank != null) {
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
                                    bestRank.first,
                                    color = GoldAccent,
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    "${bestRank.second}+",
                                    color = GoldAccent,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            if (nextRank != null) {
                                Spacer(Modifier.height(6.dp))
                                Text(
                                    "До разряда «${nextRank.first}» не хватило ${nextRank.second - score} очков",
                                    color = Color.White.copy(alpha = 0.6f),
                                    fontSize = 12.sp
                                )
                            }
                        } else {
                            Text(
                                "Пока без разряда. До II юношеского не хватило ${360 - score} очков.",
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
            .height(64.dp)
            .clip(RoundedCornerShape(10.dp))
            .background(if (enabled) TileBg else TileBg.copy(alpha = 0.4f))
            .clickable(enabled = enabled) { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Text(
            digit,
            color = Color.White,
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold
        )
    }
}
