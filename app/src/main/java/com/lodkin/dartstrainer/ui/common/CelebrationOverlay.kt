package com.lodkin.dartstrainer.ui.common

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.lodkin.dartstrainer.theme.Accent
import com.lodkin.dartstrainer.theme.ErrorColor
import com.lodkin.dartstrainer.theme.TileBg
import com.lodkin.dartstrainer.theme.TileBgDark
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

/**
 * Диалог с праздничной анимацией поверх содержимого.
 *
 * @param visible      показывать ли диалог
 * @param isNewRecord  побит ли рекорд → включает анимацию
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
                        Text(
                            "↶ Отменить",
                            color = ErrorColor,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
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
                        Text(
                            "Продолжить",
                            color = Color(0xFF121212),
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
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
// Canvas: конфетти + салют (ракеты + взрывы)
// ─────────────────────────────────────────────

@Composable
private fun CelebrationCanvas() {
    var elapsedMs by remember { mutableStateOf(0L) }
    var running by remember { mutableStateOf(true) }

    val confetti = remember { List(60) { ConfettiParticle.newRandom() } }
    val rockets = remember { List(6) { RocketParticle.newRandom() } }

    LaunchedEffect(Unit) {
        val start = withFrameNanos { it }
        while (running) {
            withFrameNanos { now ->
                val ms = (now - start) / 1_000_000L
                elapsedMs = ms
                if (ms >= 4000L) running = false
            }
        }
    }

    Canvas(modifier = Modifier.fillMaxSize()) {
        val t = elapsedMs / 1000f
        val totalSec = 4.0f

        // ── Конфетти ──
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

        // ── Салют: ракеты + взрывы ──
        rockets.forEach { r ->
            val local = t - r.delaySec
            if (local <= 0f) return@forEach

            val startX = r.startX * size.width
            val startY = size.height + 40f
            val targetY = r.targetY * size.height

            // Фаза 1: взлёт (riseDuration секунд)
            if (local < r.riseDuration) {
                val riseProgress = local / r.riseDuration
                val curY = startY + (targetY - startY) * riseProgress
                // Голова ракеты
                drawCircle(
                    color = Color.White.copy(alpha = 0.95f),
                    radius = 5f,
                    center = Offset(startX, curY)
                )
                // Хвост
                drawCircle(
                    color = r.color.copy(alpha = 0.6f),
                    radius = 3f,
                    center = Offset(startX, curY + 20f)
                )
                drawCircle(
                    color = r.color.copy(alpha = 0.3f),
                    radius = 2f,
                    center = Offset(startX, curY + 40f)
                )
            } else {
                // Фаза 2: взрыв
                val explosionLocal = local - r.riseDuration
                val explosionDuration = 1.6f
                if (explosionLocal <= explosionDuration) {
                    val progress = explosionLocal / explosionDuration
                    val fade = (1f - progress).coerceIn(0f, 1f)
                    val maxRadius = 180f + r.size * 60f
                    val radius = maxRadius * progress

                    val center = Offset(startX, targetY)

                    // Лучи в разные стороны
                    val rayCount = 16
                    for (i in 0 until rayCount) {
                        val angle = (i.toFloat() / rayCount) * 2f * Math.PI.toFloat()
                        val dx = cos(angle) * radius
                        val dy = sin(angle) * radius
                        val dotX = center.x + dx
                        val dotY = center.y + dy

                        drawCircle(
                            color = r.color.copy(alpha = fade),
                            radius = 4f + (1f - progress) * 3f,
                            center = Offset(dotX, dotY)
                        )
                    }

                    // Яркая центральная вспышка
                    if (progress < 0.4f) {
                        val centerFade = (1f - progress / 0.4f).coerceIn(0f, 1f)
                        drawCircle(
                            color = Color.White.copy(alpha = centerFade * 0.9f),
                            radius = 25f + progress * 60f,
                            center = center
                        )
                    }
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

private data class RocketParticle(
    val startX: Float,        // 0..1 — где стартует ракета по X
    val targetY: Float,       // 0..1 — где взрывается (по Y, от верха)
    val delaySec: Float,      // задержка перед стартом
    val riseDuration: Float,  // сколько летит вверх
    val size: Float,          // базовый радиус взрыва
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

        fun newRandom() = RocketParticle(
            startX = 0.1f + Random.nextFloat() * 0.8f,
            targetY = 0.12f + Random.nextFloat() * 0.35f,
            delaySec = Random.nextFloat() * 2.0f,
            riseDuration = 0.6f + Random.nextFloat() * 0.5f,
            size = 0.8f + Random.nextFloat() * 1.0f,
            color = COLORS.random()
        )
    }
}
