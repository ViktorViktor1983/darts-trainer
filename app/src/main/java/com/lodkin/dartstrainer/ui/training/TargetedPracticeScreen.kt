package com.lodkin.dartstrainer.ui.training

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
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

private val HitGreen = Color(0xFF4CAF50)
private val MissRed = Color(0xFF8B2A2A)

/**
 * Экран точечной работы по слабым местам («Снайпер»).
 */
@Composable
fun TargetedPracticeScreen(
    targets: List<String>,
    dartsPerTarget: Int = 15,
    onFinish: (hits: Int, totalDarts: Int) -> Unit,
    onBack: () -> Unit
) {
    if (targets.isEmpty()) {
        onFinish(0, 0)
        return
    }

    var currentTargetIndex by remember { mutableStateOf(0) }
    var dartsThrownOnCurrent by remember { mutableStateOf(0) }
    var totalHits by remember { mutableStateOf(0) }
    var totalDartsThrown by remember { mutableStateOf(0) }
    var showBackConfirm by remember { mutableStateOf(false) }
    var showFinishDialog by remember { mutableStateOf(false) }

    val currentTarget = targets[currentTargetIndex]
    val isLastTarget = currentTargetIndex == targets.lastIndex
    val dartsLeft = dartsPerTarget - dartsThrownOnCurrent

    fun registerThrow(isHit: Boolean) {
        if (showFinishDialog) return

        if (isHit) totalHits++
        totalDartsThrown++
        dartsThrownOnCurrent++

        if (dartsThrownOnCurrent >= dartsPerTarget) {
            if (isLastTarget) {
                showFinishDialog = true
            } else {
                currentTargetIndex++
                dartsThrownOnCurrent = 0
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBg)
    ) {
        // ── ШАПКА ──
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
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    "Точечная работа",
                    color = Color.White,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    "Цель ${currentTargetIndex + 1} из ${targets.size}",
                    color = Accent,
                    fontSize = 12.sp
                )
            }
        }

        // ── ОСНОВНАЯ ЧАСТЬ ──
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                "ТЕКУЩАЯ ЦЕЛЬ",
                color = Color.White.copy(alpha = 0.5f),
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 2.sp
            )
            Spacer(Modifier.height(8.dp))
            Text(
                currentTarget,
                color = GoldAccent,
                fontSize = 80.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
            Spacer(Modifier.height(16.dp))
            Text(
                "Осталось бросков: $dartsLeft / $dartsPerTarget",
                color = Color.White,
                fontSize = 18.sp,
                fontWeight = FontWeight.Medium
            )
            
            Spacer(Modifier.height(32.dp))

            // Прогресс-точки
            Row(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                modifier = Modifier.padding(horizontal = 16.dp)
            ) {
                repeat(dartsPerTarget) { i ->
                    val filled = i < dartsThrownOnCurrent
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(8.dp)
                            .clip(RoundedCornerShape(4.dp))
                            .background(if (filled) Accent else TileBgDark)
                    )
                }
            }
        }

        // ── КНОПКИ ──
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .padding(bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Кнопка ПРОМАХ
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(80.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(MissRed)
                        .clickable { registerThrow(false) },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        "МИМО",
                        color = Color.White,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                // Кнопка ПОПАЛ
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(80.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(HitGreen)
                        .clickable { registerThrow(true) },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        "ПОПАЛ",
                        color = Color.White,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }

    // ── ДИАЛОГ ВЫХОДА ──
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
            title = { Text("Выйти из упражнения?", color = Color.White) },
            text = { Text("Прогресс не сохранится.", color = Color.White) }
        )
    }

    // ── ДИАЛОГ ЗАВЕРШЕНИЯ ──
    if (showFinishDialog) {
        AlertDialog(
            onDismissRequest = { },
            containerColor = TileBgDark,
            title = {
                Text(
                    "Упражнение завершено",
                    color = GoldAccent,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column {
                    Text(
                        "Ты выполнил все цели.",
                        color = Color.White,
                        fontSize = 15.sp
                    )
                    Spacer(Modifier.height(12.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Попаданий:", color = Color.White.copy(alpha = 0.7f), fontSize = 14.sp)
                        Text("$totalHits", color = GoldAccent, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                    }
                    Spacer(Modifier.height(4.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Всего бросков:", color = Color.White.copy(alpha = 0.7f), fontSize = 14.sp)
                        Text("$totalDartsThrown", color = Color.White, fontSize = 16.sp)
                    }
                    Spacer(Modifier.height(4.dp))
                    val percent = if (totalDartsThrown > 0) (totalHits * 100 / totalDartsThrown) else 0
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Точность:", color = Color.White.copy(alpha = 0.7f), fontSize = 14.sp)
                        Text("$percent%", color = Accent, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    showFinishDialog = false
                    onFinish(totalHits, totalDartsThrown)
                }) {
                    Text("ЗАВЕРШИТЬ", color = Accent, fontWeight = FontWeight.Bold)
                }
            }
        )
    }
}
