package com.lodkin.dartstrainer.ui.common

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.lodkin.dartstrainer.theme.Accent
import com.lodkin.dartstrainer.theme.ErrorColor
import com.lodkin.dartstrainer.theme.TileBg
import com.lodkin.dartstrainer.theme.TileBgDark
import kotlin.random.Random

/**
 * Диалог с праздничной анимацией поверх содержимого.
 *
 * Всё в одном окне Dialog:
 *   1. Полупрозрачный фон.
 *   2. Карточка с контентом (по центру).
 *   3. Анимация (конфетти + салют) ПОВЕРХ карточки — если побит рекорд.
 *
 * @param visible      показывать ли диалог
 * @param isNewRecord  побит ли рекорд → включает анимацию
 * @param onConfirm    кнопка «Продолжить»
 * @param onUndo       кнопка «Отменить подход»
 * @param content      содержимое карточки (статистика, нормативы и т.п.)
 */
@Composable
fun CelebrationOverlay(
    visible: Boolean,
    isNewRecord: Boolean,
    onConfirm: () -> Unit,
    onUndo: () -> Unit,
    content: @Composable ColumnScope.() -> Unit
) {
    if (!visible) return

    Dialog(
        onDismissRequest = { },
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            dismissOnBackPress = false,
            dismissOnClickOutside = false
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.6f))
        ) {
            // ── Карточка диалога по центру ──
            Column(
                modifier = Modifier
                    .align(Alignment.Center)
                    .padding(20.dp)
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(20.dp))
                    .background(TileBgDark)
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                content()

                Spacer(Modifier.height(18.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(12.dp))
                            .background(TileBg)
                            .clickable { onUndo() }
                            .padding(vertical = 14.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text2(
                            text = "↶ Отменить",
                            color = ErrorColor
                        )
                    }
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(12.dp))
                            .background(Accent)
                            .clickable { onConfirm() }
                            .padding(vertical = 14.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text2(
                            text = "Продолжить",
                            color = Color(0xFF121212)
                        )
                    }
                }
            }

            // ── Анимация поверх карточки ──
            if (isNewRecord) {
                CelebrationCanvas()
            }
        }
    }
}

// ─────────────────────────────────────────────
// Canvas с анимацией: конфетти + салют
// ─────────────────────────────────────────────

@Composable
private fun CelebrationCanvas() {
    var elapsedMs by remember { mutableStateOf(0L) }
    var running by remember { mutableStateOf(true) }

    val confetti = remember { List(70) { ConfettiParticle.newRandom() } }
    val fireworks = remember { List(10) { FireworkParticle.newRandom() } }

    LaunchedEffect(Unit) {
        val start = withFrameNanos { it }
        while (running) {
            withFrameNanos { now ->
                val ms = (now - start) / 1_000_000L
                elapsedMs = ms
                if (ms >= 3500L) running = false
            }
        }
    }

    Canvas(modifier = Modifier.fillMaxSize()) {
        val t = elapsedMs / 1000f
        val totalSec = 3.5f

        // ── Конфетти (крупнее, ярче) ──
        confetti.forEach { c ->
            val x = c.startX * size.width + c.vx * t * 150f
            val y = c.startY * size.height + c.vy * t * 150f + 0.5f * 450f * t * t
            val fade = (1f - (t / totalSec)).coerceIn(0f, 1f)
            if (fade > 0.1f && y < size.height + 100f) {
                val side = c.size * 1.8f
                drawRect(
                    color = c.color.copy(alpha = fade),
                    topLeft = Offset(x, y),
                    size = Size(side, side)
                )
            }
        }

        // ── Салют (толще, ярче, поверх) ──
        fireworks.forEach { f ->
            val local = (t - f.delaySec).coerceAtLeast(0f)
            if (local in 0.01f..1.8f) {
                val progress = local / 1.8f
                val radius = 50f + progress * 320f
                val fade = (1f - progress).coerceIn(0f, 1f)
                val center = Offset(f.x * size.width, f.y * size.height)

                // Внешнее кольцо
                drawCircle(
                    color = f.color.copy(alpha = fade),
                    radius = radius,
                    center = center,
                    style = Stroke(width = 10f)
                )
                // Среднее яркое кольцо
                drawCircle(
                    color = Color.White.copy(alpha = fade * 0.8f),
                    radius = radius * 0.75f,
                    center = center,
                    style = Stroke(width = 5f)
                )
                // Внутреннее цветное
                drawCircle(
                    color = f.color.copy(alpha = fade * 0.5f),
                    radius = radius * 0.45f,
                    center = center,
                    style = Stroke(width = 3f)
                )
            }
        }
    }
}

// ─────────────────────────────────────────────
// Частицы
// ─────────────────────────────────────────────

private data class ConfettiParticle(
    val startX: Float,
    val startY: Float,
    val vx: Float,
    val vy: Float,
    val size: Float,
    val color: Color
) {
    companion object {
        private val COLORS = listOf(
            Color(0xFFFFD54F),
            Color(0xFF4FC3F7),
            Color(0xFFE57373),
            Color(0xFF81C784),
            Color(0xFFFFB74D),
            Color(0xFFBA68C8),
            Color(0xFFFFFFFF)
        )

        fun newRandom() = ConfettiParticle(
            startX = Random.nextFloat(),
            startY = -0.15f - Random.nextFloat() * 0.3f,
            vx = (Random.nextFloat() - 0.5f) * 0.4f,
            vy = 0.35f + Random.nextFloat() * 0.45f,
            size = 7f + Random.nextFloat() * 10f,
            color = COLORS.random()
        )
    }
}

private data class FireworkParticle(
    val x: Float,
    val y: Float,
    val delaySec: Float,
    val color: Color
) {
    companion object {
        private val COLORS = listOf(
            Color(0xFFFFD54F),
            Color(0xFF4FC3F7),
            Color(0xFFFFB74D),
            Color(0xFFE57373),
            Color(0xFF81C784),
            Color(0xFFBA68C8)
        )

        fun newRandom() = FireworkParticle(
            x = 0.15f + Random.nextFloat() * 0.7f,
            y = 0.15f + Random.nextFloat() * 0.5f,
            delaySec = Random.nextFloat() * 1.8f,
            color = COLORS.random()
        )
    }
}

// Небольшая обёртка над Text, чтобы не тащить лишние импорты наверх
@Composable
private fun Text2(text: String, color: Color) {
    androidx.compose.material3.Text(
        text = text,
        color = color,
        fontSize = androidx.compose.ui.unit.TextUnit(14f, androidx.compose.ui.unit.TextUnitType.Sp),
        fontWeight = androidx.compose.ui.text.font.FontWeight.Bold
    )
}
