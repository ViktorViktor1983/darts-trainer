package com.lodkin.dartstrainer.ui.common

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupProperties
import kotlin.random.Random

/**
 * Праздничный оверлей: конфетти + салют.
 *
 * Показывается поверх диалога рекорда. Клики НЕ блокирует —
 * пользователь может нажимать кнопки диалога, пока идёт анимация.
 *
 * @param visible          показать / скрыть оверлей
 * @param durationMillis   длительность анимации в миллисекундах
 */
@Composable
fun CelebrationOverlay(
    visible: Boolean,
    durationMillis: Long = 3000L
) {
    if (!visible) return

    var elapsedMs by remember { mutableStateOf(0L) }
    var running by remember { mutableStateOf(true) }

    val confetti = remember { List(50) { ConfettiParticle.newRandom() } }
    val fireworks = remember { List(7) { FireworkParticle.newRandom() } }

    LaunchedEffect(Unit) {
        val start = withFrameNanos { it }
        while (running) {
            withFrameNanos { now ->
                val ms = (now - start) / 1_000_000L
                elapsedMs = ms
                if (ms >= durationMillis) running = false
            }
        }
    }

    Popup(
        alignment = Alignment.TopStart,
        properties = PopupProperties(
            focusable = false,
            dismissOnBackPress = false,
            dismissOnClickOutside = false
        )
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val t = elapsedMs / 1000f
            val totalSec = durationMillis / 1000f

            // ── Конфетти ──
            confetti.forEach { c ->
                val x = c.startX * size.width + c.vx * t * 100f
                val y = c.startY * size.height + c.vy * t * 100f + 0.5f * 350f * t * t
                val fade = (1f - (t / totalSec)).coerceIn(0f, 1f)
                if (fade > 0.05f && y < size.height + 50f) {
                    drawRect(
                        color = c.color.copy(alpha = fade),
                        topLeft = Offset(x, y),
                        size = Size(c.size, c.size)
                    )
                }
            }

            // ── Салют (расширяющиеся кольца) ──
            fireworks.forEach { f ->
                val local = (t - f.delaySec).coerceAtLeast(0f)
                if (local in 0.01f..1.5f) {
                    val progress = local / 1.5f
                    val radius = 30f + progress * 220f
                    val fade = (1f - progress).coerceIn(0f, 1f)
                    val center = Offset(f.x * size.width, f.y * size.height)

                    drawCircle(
                        color = f.color.copy(alpha = fade),
                        radius = radius,
                        center = center,
                        style = Stroke(width = 5f)
                    )
                    drawCircle(
                        color = f.color.copy(alpha = fade * 0.5f),
                        radius = radius * 0.6f,
                        center = center,
                        style = Stroke(width = 2f)
                    )
                }
            }
        }
    }
}

// ─────────────────────────────────────────────
// Модели частиц
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
            Color(0xFFFFD54F), // золотой
            Color(0xFF4FC3F7), // голубой
            Color(0xFFE57373), // розовый
            Color(0xFF81C784), // зелёный
            Color(0xFFFFB74D), // оранжевый
            Color(0xFFBA68C8)  // фиолетовый
        )

        fun newRandom() = ConfettiParticle(
            startX = Random.nextFloat(),
            startY = -0.1f - Random.nextFloat() * 0.2f,
            vx = (Random.nextFloat() - 0.5f) * 0.3f,
            vy = 0.3f + Random.nextFloat() * 0.4f,
            size = 6f + Random.nextFloat() * 8f,
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
            Color(0xFF81C784)
        )

        fun newRandom() = FireworkParticle(
            x = 0.15f + Random.nextFloat() * 0.7f,
            y = 0.15f + Random.nextFloat() * 0.5f,
            delaySec = Random.nextFloat() * 1.5f,
            color = COLORS.random()
        )
    }
}
