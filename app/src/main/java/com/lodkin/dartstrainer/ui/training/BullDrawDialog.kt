package com.lodkin.dartstrainer.ui.training

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.lodkin.dartstrainer.theme.Accent
import com.lodkin.dartstrainer.theme.GoldAccent
import com.lodkin.dartstrainer.theme.TileBgDark
import kotlin.random.Random

private val BullRed = Color(0xFFD32F2F)
private val BullGreen = Color(0xFF4CAF50)
private val BullMiss = Color(0xFF78909C)

/**
 * Диалог розыгрыша Bull перед тренировочной партией 501 или Крикета.
 *
 * Правила:
 *   • Игрок выбирает результат своего броска: Красный (50), Зелёный (25), Мимо (0).
 *   • Бот «бросает» автоматически по формуле от своего уровня.
 *   • Если результат разный — победитель начинает игру.
 *   • Если равенство — переброс.
 *
 * @param playerName имя игрока (для показа)
 * @param botName    имя бота (для показа)
 * @param botLevel   уровень бота (1..16) — используется для расчёта его броска
 * @param onWinnerChosen  колбэк с победителем: 0 — начинает игрок, 1 — бот
 * @param onCancel   колбэк отмены розыгрыша (закрыть без старта)
 */
@Composable
internal fun BullDrawDialog(
    playerName: String,
    botName: String,
    botLevel: Int,
    onWinnerChosen: (Int) -> Unit,
    onCancel: () -> Unit
) {
    var humanResult by remember { mutableStateOf(-1) }   // -1 = ещё не бросал, 0=мимо, 1=зелёный, 2=красный
    var botResult by remember { mutableStateOf(-1) }
    var showResult by remember { mutableStateOf(false) }

    // ── Фаза 1: игрок выбирает результат своего броска ──
    if (!showResult) {
        AlertDialog(
            onDismissRequest = onCancel,
            containerColor = TileBgDark,
            confirmButton = {},
            dismissButton = {},
            title = {
                Text(
                    "Разыграем Bull?",
                    color = GoldAccent,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column {
                    Text(
                        "Кто начнёт партию — решит бросок в Bull. " +
                                "Укажи, куда попал твой дротик.",
                        color = Color.White.copy(alpha = 0.9f),
                        fontSize = 14.sp,
                        lineHeight = 20.sp
                    )
                    Spacer(Modifier.height(14.dp))

                    BullChoiceButton("КРАСНЫЙ (50)", BullRed) {
                        humanResult = 2
                        botResult = generateBotBullResult(botLevel)
                        showResult = true
                    }
                    Spacer(Modifier.height(10.dp))
                    BullChoiceButton("ЗЕЛЁНЫЙ (25)", BullGreen) {
                        humanResult = 1
                        botResult = generateBotBullResult(botLevel)
                        showResult = true
                    }
                    Spacer(Modifier.height(10.dp))
                    BullChoiceButton("МИМО (0)", BullMiss) {
                        humanResult = 0
                        botResult = generateBotBullResult(botLevel)
                        showResult = true
                    }

                    Spacer(Modifier.height(14.dp))
                    TextButton(
                        onClick = onCancel,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            "ОТМЕНИТЬ",
                            color = Color.White.copy(alpha = 0.6f),
                            fontSize = 13.sp
                        )
                    }
                }
            }
        )
    }

    // ── Фаза 2: показываем результат ──
    if (showResult) {
        val draw = humanResult == botResult
        val humanWins = humanResult > botResult

        val humanLabel = when (humanResult) {
            2 -> "Красный (50)"
            1 -> "Зелёный (25)"
            else -> "Мимо (0)"
        }
        val botLabel = when (botResult) {
            2 -> "Красный (50)"
            1 -> "Зелёный (25)"
            else -> "Мимо (0)"
        }
        val humanColor = when (humanResult) {
            2 -> BullRed
            1 -> BullGreen
            else -> BullMiss
        }
        val botColor = when (botResult) {
            2 -> BullRed
            1 -> BullGreen
            else -> BullMiss
        }

        AlertDialog(
            onDismissRequest = { },
            containerColor = TileBgDark,
            confirmButton = {
                if (draw) {
                    TextButton(onClick = {
                        humanResult = -1
                        botResult = -1
                        showResult = false
                    }) {
                        Text("Перебросить", color = Accent, fontWeight = FontWeight.Bold)
                    }
                } else {
                    TextButton(onClick = {
                        val winner = if (humanWins) 0 else 1
                        onWinnerChosen(winner)
                    }) {
                        Text("Начать игру", color = Accent, fontWeight = FontWeight.Bold)
                    }
                }
            },
            title = {
                Text(
                    if (draw) "Ничья — переброс" else "Результат розыгрыша",
                    color = GoldAccent,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column {
                    Text(
                        "$playerName попал:",
                        color = Color.White.copy(alpha = 0.8f),
                        fontSize = 13.sp
                    )
                    Text(
                        humanLabel,
                        color = humanColor,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(Modifier.height(10.dp))
                    Text(
                        "$botName попал:",
                        color = Color.White.copy(alpha = 0.8f),
                        fontSize = 13.sp
                    )
                    Text(
                        botLabel,
                        color = botColor,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                    if (!draw) {
                        Spacer(Modifier.height(14.dp))
                        Text(
                            "Игру начинает ${if (humanWins) playerName else botName}",
                            color = GoldAccent,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        )
    }
}

@Composable
private fun BullChoiceButton(label: String, color: Color, onClick: () -> Unit) {
    androidx.compose.foundation.layout.Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(color)
            .clickable { onClick() }
            .padding(vertical = 14.dp),
        contentAlignment = androidx.compose.ui.Alignment.Center
    ) {
        Text(label, color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
    }
}

/**
 * Имитация броска в Bull для бота.
 * Множитель 0.8 — как в свободной игре.
 *
 * @param botLevel уровень бота 1..16
 * @return 2 — красный, 1 — зелёный, 0 — мимо
 */
private fun generateBotBullResult(botLevel: Int): Int {
    val lvl = botLevel.coerceIn(1, 16)
    val pRed = (0.05 + (lvl - 1) * 0.027) * 0.8
    val pGreen = (0.25 + (lvl - 1) * 0.013) * 0.8
    val r = Random.nextDouble()
    return when {
        r < pRed -> 2
        r < pRed + pGreen -> 1
        else -> 0
    }
}
