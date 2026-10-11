package com.lodkin.dartstrainer.ui.training

import android.content.Context
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.lodkin.dartstrainer.theme.Accent
import com.lodkin.dartstrainer.theme.ErrorColor
import com.lodkin.dartstrainer.theme.GoldAccent
import com.lodkin.dartstrainer.theme.TileBg
import com.lodkin.dartstrainer.theme.TileBgDark
import kotlin.math.roundToInt

private const val PREFS = "training_prefs"
private const val KEY_VISIBLE = "timer_visible"
private const val KEY_X = "timer_offset_x"
private const val KEY_Y = "timer_offset_y"

/**
 * Оверлей таймера тренировки.
 *
 * Возможности:
 *   • Перетаскивание пальцем (сохраняется позиция).
 *   • Короткий тап — пауза / продолжить.
 *   • Долгое нажатие — меню: «Убрать таймер» / «Сбросить позицию».
 *   • Если убран — в правом верхнем углу иконка ⏱, нажатие возвращает таймер.
 *
 * Позиция и видимость сохраняются в SharedPreferences (training_prefs).
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun TrainingTimerOverlay(
    secondsLeft: Int,
    isPaused: Boolean,
    onTogglePause: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val prefs = remember { context.getSharedPreferences(PREFS, Context.MODE_PRIVATE) }

    var isVisible by remember { mutableStateOf(prefs.getBoolean(KEY_VISIBLE, true)) }
    var savedX by remember { mutableStateOf(prefs.getFloat(KEY_X, -1f)) }
    var savedY by remember { mutableStateOf(prefs.getFloat(KEY_Y, -1f)) }

    var showMenu by remember { mutableStateOf(false) }

    // Размеры контейнера и самой плашки
    var containerWidth by remember { mutableStateOf(0f) }
    var containerHeight by remember { mutableStateOf(0f) }
    var barWidth by remember { mutableStateOf(0f) }
    var barHeight by remember { mutableStateOf(0f) }

    // Текущая позиция (левый верхний угол плашки), px
    var posX by remember { mutableStateOf(0f) }
    var posY by remember { mutableStateOf(0f) }

    // Инициализация позиции
    LaunchedEffect(containerWidth, containerHeight, barWidth, barHeight, isVisible) {
        if (containerWidth > 0 && barWidth > 0) {
            val maxX = (containerWidth - barWidth).coerceAtLeast(0f)
            val maxY = (containerHeight - barHeight).coerceAtLeast(0f)

            posX = if (savedX < 0f) (maxX / 2f) else savedX.coerceIn(0f, maxX)
            posY = if (savedY < 0f) 24f else savedY.coerceIn(0f, maxY)
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .onGloballyPositioned { coords ->
                containerWidth = coords.size.width.toFloat()
                containerHeight = coords.size.height.toFloat()
            }
    ) {
        if (!isVisible) {
            // ── Иконка ⏱ в правом верхнем углу ──
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(top = 8.dp, end = 12.dp)
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(TileBg)
                    .clickable {
                        isVisible = true
                        prefs.edit().putBoolean(KEY_VISIBLE, true).apply()
                    },
                contentAlignment = Alignment.Center
            ) {
                Text("⏱", fontSize = 18.sp)
            }
        } else {
            // ── Плашка таймера ──
            Box(
                modifier = Modifier
                    .offset { IntOffset(posX.roundToInt(), posY.roundToInt()) }
                    .onGloballyPositioned { coords ->
                        barWidth = coords.size.width.toFloat()
                        barHeight = coords.size.height.toFloat()
                    }
                    .pointerInput(Unit) {
                        detectDragGestures { change, dragAmount ->
                            change.consume()
                            val maxX = (containerWidth - barWidth).coerceAtLeast(0f)
                            val maxY = (containerHeight - barHeight).coerceAtLeast(0f)

                            posX = (posX + dragAmount.x).coerceIn(0f, maxX)
                            posY = (posY + dragAmount.y).coerceIn(0f, maxY)

                            prefs.edit()
                                .putFloat(KEY_X, posX)
                                .putFloat(KEY_Y, posY)
                                .apply()
                        }
                    }
                    .combinedClickable(
                        onClick = { onTogglePause() },
                        onLongClick = { showMenu = true }
                    )
            ) {
                TimerBarContent(
                    secondsLeft = secondsLeft,
                    isPaused = isPaused
                )
            }
        }
    }

    // ── Меню при долгом нажатии ──
    if (showMenu) {
        AlertDialog(
            onDismissRequest = { showMenu = false },
            containerColor = TileBgDark,
            title = {
                Text(
                    "Таймер",
                    color = GoldAccent,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Text(
                    "Что сделать с таймером?",
                    color = Color.White.copy(alpha = 0.85f),
                    fontSize = 14.sp
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    prefs.edit()
                        .remove(KEY_X)
                        .remove(KEY_Y)
                        .apply()
                    savedX = -1f
                    savedY = -1f
                    if (containerWidth > 0 && barWidth > 0) {
                        posX = (containerWidth - barWidth) / 2f
                        posY = 24f
                    }
                    showMenu = false
                }) {
                    Text("Сбросить позицию", color = Accent, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    isVisible = false
                    prefs.edit().putBoolean(KEY_VISIBLE, false).apply()
                    showMenu = false
                }) {
                    Text("Убрать таймер", color = ErrorColor, fontWeight = FontWeight.Bold)
                }
            }
        )
    }
}

// ─────────────────────────────────────────────
// Внешний вид плашки
// ─────────────────────────────────────────────

@Composable
private fun TimerBarContent(
    secondsLeft: Int,
    isPaused: Boolean
) {
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(20.dp))
            .background(Color(0xE61A2332))
            .padding(horizontal = 12.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text("⏱", fontSize = 14.sp)
        Spacer(Modifier.width(6.dp))
        Text(
            formatTrainingTime(secondsLeft),
            color = if (secondsLeft < 60) ErrorColor else GoldAccent,
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold
        )
        Spacer(Modifier.width(10.dp))
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(10.dp))
                .background(TileBg)
                .padding(horizontal = 10.dp, vertical = 3.dp)
        ) {
            Text(
                if (isPaused) "▶" else "⏸",
                color = Accent,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

private fun formatTrainingTime(total: Int): String {
    val safe = total.coerceAtLeast(0)
    val h = safe / 3600
    val m = (safe % 3600) / 60
    val s = safe % 60
    return if (h > 0) "%d:%02d:%02d".format(h, m, s)
    else "%02d:%02d".format(m, s)
}
