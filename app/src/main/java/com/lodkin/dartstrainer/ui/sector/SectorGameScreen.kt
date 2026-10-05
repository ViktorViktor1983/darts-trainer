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
import androidx.compose.ui.text.style.TextAlign
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

// Нормативы для S20 (от низшего к высшему)
private val SECTOR_20_NORMS: List<Pair<String, Int>> = listOf(
    "II юношеский" to 360,
    "I юношеский" to 400,
    "III разряд" to 460,
    "II разряд" to 540,
    "I разряд" to 600,
    "КМС" to 760,
    "МС" to 960
)

private const val TOTAL_DARTS = 30

@Composable
fun SectorGameScreen(
    sector: Int,
    onFinish: (Int) -> Unit,
    onBack: () -> Unit
) {
    // Счёт: 0..90 (если все 30 попадёшь в утроение = 90)
    var score by remember { mutableStateOf(0) }
    var dartsThrown by remember { mutableStateOf(0) }
    var hits by remember { mutableStateOf(0) }

    // История попаданий: список очков за каждый дротик
    val history = remember { mutableStateListOf<Int>() }

    var showBackConfirm by remember { mutableStateOf(false) }
    var showFinishDialog by remember { mutableStateOf(false) }

    val isBull = sector == 25
    val sectorLabel = if (isBull) "BULL" else "S$sector"

    fun addThrow(points: Int, isHit: Boolean) {
        if (dartsThrown >= TOTAL_DARTS) return
        score += points
        if (isHit) hits++
        history.add(points)
        dartsThrown++

        if (dartsThrown >= TOTAL_DARTS) {
            showFinishDialog = true
        }
    }

    fun undo() {
        if (history.isEmpty()) return
        val last = history.removeAt(history.lastIndex)
        score -= last
        if (last > 0) hits--
        dartsThrown--
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
                "$dartsThrown / $TOTAL_DARTS",
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
                .padding(vertical = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                "ОЧКИ",
                color = Color(0xFF99AABB),
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 2.sp
            )
            Text(
                "$score",
                color = GoldAccent,
                fontSize = 56.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(Modifier.height(4.dp))
            Text(
                "попаданий: $hits / $dartsThrown",
                color = Color.White.copy(alpha = 0.6f),
                fontSize = 13.sp
            )
            if (dartsThrown > 0) {
                Text(
                    "точность: ${"%.1f".format(hits.toDouble() / dartsThrown * 100)}%",
                    color = Color.White.copy(alpha = 0.5f),
                    fontSize = 12.sp
                )
            }
        }

        // ── Кнопки ввода ──
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            val enabled = dartsThrown < TOTAL_DARTS

            if (!isBull) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    ThrowButton(
                        label = "S$sector",
                        sub = "+1",
                        color = TileBg,
                        enabled = enabled,
                        modifier = Modifier.weight(1f)
                    ) { addThrow(1, true) }

                    ThrowButton(
                        label = "D$sector",
                        sub = "+2",
                        color = TileBg,
                        enabled = enabled,
                        modifier = Modifier.weight(1f)
                    ) { addThrow(2, true) }

                    ThrowButton(
                        label = "T$sector",
                        sub = "+3",
                        color = TileBg,
                        enabled = enabled,
                        modifier = Modifier.weight(1f)
                    ) { addThrow(3, true) }
                }
            } else {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    ThrowButton(
                        label = "BULL 25",
                        sub = "+25",
                        color = TileBg,
                        enabled = enabled,
                        modifier = Modifier.weight(1f)
                    ) { addThrow(25, true) }

                    ThrowButton(
                        label = "BULL 50",
                        sub = "+50",
                        color = TileBg,
                        enabled = enabled,
                        modifier = Modifier.weight(1f)
                    ) { addThrow(50, true) }
                }
            }

            ThrowButton(
                label = "МИМО",
                sub = "0",
                color = Color(0xFF2A2A2A),
                enabled = enabled,
                modifier = Modifier.fillMaxWidth()
            ) { addThrow(0, false) }
        }

        // ── История и откат ──
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // История
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(56.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(TileBgDark)
                    .padding(horizontal = 10.dp),
                contentAlignment = Alignment.CenterStart
            ) {
                if (history.isEmpty()) {
                    Text(
                        "Пока пусто",
                        color = Color.White.copy(alpha = 0.4f),
                        fontSize = 12.sp
                    )
                } else {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScrollSafe(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Последние 15 бросков
                        history.takeLast(15).forEach { v ->
                            val bg = when (v) {
                                0 -> Color(0xFF3A3A3A)
                                1 -> Color(0xFF4CAF50)
                                2 -> Color(0xFF2196F3)
                                3 -> Color(0xFF9C27B0)
                                25 -> Color(0xFFFF9800)
                                50 -> Color(0xFFE91E63)
                                else -> TileBg
                            }
                            Box(
                                modifier = Modifier
                                    .size(32.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(bg),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    if (v == 0) "0" else "$v",
                                    color = Color.White,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }

            // Откат
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(if (history.isNotEmpty()) PaleYellow else PaleYellow.copy(alpha = 0.35f))
                    .clickable(enabled = history.isNotEmpty()) { undo() },
                contentAlignment = Alignment.Center
            ) {
                Text("↶", color = PaleYellowText, fontSize = 28.sp, fontWeight = FontWeight.Black)
            }
        }

        Spacer(Modifier.height(8.dp))

        // ── Кнопка «Завершить» ──
        if (dartsThrown > 0 && dartsThrown < TOTAL_DARTS) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 4.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(TileBgDark)
                    .clickable { showFinishDialog = true }
                    .padding(vertical = 12.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    "ЗАВЕРШИТЬ ДОСРОЧНО",
                    color = ErrorColor,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }

        Spacer(Modifier.height(12.dp))
    }

    // ── Диалог подтверждения выхода ──
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

    // ── Диалог результата ──
    if (showFinishDialog) {
        val resultTitle = if (dartsThrown >= TOTAL_DARTS) "ИГРА ЗАВЕРШЕНА" else "РЕЗУЛЬТАТ"
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
                    undo()
                }) { Text("↶ Отменить бросок", color = ErrorColor) }
            },
            title = {
                Text(resultTitle, color = Accent, fontSize = 22.sp, fontWeight = FontWeight.Bold)
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
                        Text("Очки:", color = Color.White, fontSize = 15.sp)
                        Text(
                            "$score",
                            color = GoldAccent,
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Spacer(Modifier.height(4.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Попаданий:", color = Color.White, fontSize = 14.sp)
                        Text(
                            "$hits / $dartsThrown",
                            color = Color.White,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Spacer(Modifier.height(4.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Точность:", color = Color.White, fontSize = 14.sp)
                        Text(
                            if (dartsThrown > 0) "%.1f%%".format(hits.toDouble() / dartsThrown * 100) else "—",
                            color = Color.White,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    // ── Разряд (только S20) ──
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

@Composable
private fun ThrowButton(
    label: String,
    sub: String,
    color: Color,
    enabled: Boolean,
    modifier: Modifier,
    onClick: () -> Unit
) {
    Box(
        modifier = modifier
            .height(72.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(if (enabled) color else color.copy(alpha = 0.4f))
            .clickable(enabled = enabled) { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                label,
                color = Color.White,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                sub,
                color = Accent,
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

// Хелпер: обычный horizontalScroll
@Composable
private fun Modifier.horizontalScrollSafe(state: androidx.compose.foundation.ScrollState): Modifier {
    return this.then(androidx.compose.foundation.horizontalScroll(state))
}
