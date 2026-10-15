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
            BoxWithConstraints(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                // Ограничиваем высоту карточки, чтобы она не вылезала за экран.
                val maxCardHeight = maxHeight - 40.dp

                Column(
                    modifier = Modifier
                        .padding(20.dp)
                        .fillMaxWidth()
                        .heightIn(max = maxCardHeight)
                        .clip(RoundedCornerShape(20.dp))
                        .background(TileBgDark)
                        .padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // ── Прокручиваемый контент (занимает всё свободное место) ──
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f, fill = false)
                            .verticalScroll(rememberScrollState()),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        content()
                    }

                    Spacer(Modifier.height(18.dp))

                    // ── Фиксированные кнопки внизу — всегда видны ──
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
            }

            if (isNewRecord) {
                CelebrationCanvas()
            }
        }
    }
}

@Composable
private fun CelebrationCanvas() {
    var elapsedMs by remember { mutableStateOf(0L) }
    var running by remember { mutableStateOf(true) }

    val confetti = remember { List(90) { ConfettiParticle.newRandom() } }
    val rockets = remember { List(12) { RocketParticle.newRandom() } }

    LaunchedEffect(Unit) {
        val start = withFrameNanos { it }
        while (running) {
            withFrameNanos { now ->
                val ms = (now - start) / 1_000_000L
                elapsedMs = ms
                if (ms >= 4500L) running = false
            }
        }
    }

    Canvas(modifier = Modifier.fillMaxSize()) {
        val t = elapsedMs / 1000f
        val totalSec = 4.5f

        // ── Конфетти ──
        confetti.forEach { c ->
            val x = c.startX * size.width + c.vx * t * 180f
            val y = c.startY * size.height + c.vy * t * 180f + 0.5f * 500f * t * t
            val fade = (1f - (t / totalSec)).coerceIn(0f, 1f)
            if (fade > 0.1f && y < size.height + 100f) {
                val side = c.size * 2.4f
                drawRect(
                    color = c.color.copy(alpha = fade),
                    topLeft = Offset(x, y),
                    size = Size(side, side)
                )
            }
        }

        // ── Салют ──
        rockets.forEach { r ->
            val local = t - r.delaySec
            if (local <= 0f) return@forEach

            val startX = r.startX * size.width
            val startY = size.height + 40f
            val targetY = r.targetY * size.height

            if (local < r.riseDuration) {
                val riseProgress = local / r.riseDuration
                val curY = startY + (targetY - startY) * riseProgress

                drawCircle(
                    color = Color.White.copy(alpha = 1f),
                    radius = 8f,
                    center = Offset(startX, curY)
                )
                drawCircle(
                    color = r.color.copy(alpha = 0.5f),
                    radius = 16f,
                    center = Offset(startX, curY)
                )
                drawCircle(
                    color = r.color.copy(alpha = 0.75f),
                    radius = 6f,
                    center = Offset(startX, curY + 22f)
                )
                drawCircle(
                    color = r.color.copy(alpha = 0.5f),
                    radius = 5f,
                    center = Offset(startX, curY + 44f)
                )
                drawCircle(
                    color = r.color.copy(alpha = 0.3f),
                    radius = 4f,
                    center = Offset(startX, curY + 66f)
                )
            } else {
                val explosionLocal = local - r.riseDuration
                val explosionDuration = 2.0f
                if (explosionLocal <= explosionDuration) {
                    val progress = explosionLocal / explosionDuration
                    val fade = (1f - progress).coerceIn(0f, 1f)
                    val maxRadius = 320f + r.size * 100f
                    val radius = maxRadius * progress
                    val center = Offset(startX, targetY)

                    if (progress < 0.6f) {
                        val cf = (1f - progress / 0.6f).coerceIn(0f, 1f)
                        drawCircle(
                            color = Color.White.copy(alpha = cf),
                            radius = 40f + progress * 100f,
                            center = center
                        )
                        drawCircle(
                            color = r.color.copy(alpha = cf * 0.7f),
                            radius = 70f + progress * 160f,
                            center = center
                        )
                    }

                    val rayCount = 24
                    for (i in 0 until rayCount) {
                        val angle = (i.toFloat() / rayCount) * 2f * Math.PI.toFloat()
                        val dx = cos(angle) * radius
                        val dy = sin(angle) * radius
                        val dotX = center.x + dx
                        val dotY = center.y + dy

                        drawCircle(
                            color = r.color.copy(alpha = fade),
                            radius = 10f + (1f - progress) * 8f,
                            center = Offset(dotX, dotY)
                        )
                        drawCircle(
                            color = Color.White.copy(alpha = fade * 0.9f),
                            radius = 4f + (1f - progress) * 3f,
                            center = Offset(dotX, dotY)
                        )
                    }

                    if (progress > 0.15f && progress < 0.85f) {
                        val innerRadius = radius * 0.55f
                        for (i in 0 until rayCount) {
                            val angle = (i.toFloat() / rayCount + 0.5f / rayCount) * 2f * Math.PI.toFloat()
                            val dx = cos(angle) * innerRadius
                            val dy = sin(angle) * innerRadius
                            drawCircle(
                                color = r.color.copy(alpha = fade * 0.6f),
                                radius = 5f,
                                center = Offset(center.x + dx, center.y + dy)
                            )
                        }
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
            Color(0xFFFFFFFF),
            Color(0xFFFFEB3B)
        )

        fun newRandom() = ConfettiParticle(
            startX = Random.nextFloat(),
            startY = -0.15f - Random.nextFloat() * 0.3f,
            vx = (Random.nextFloat() - 0.5f) * 0.5f,
            vy = 0.35f + Random.nextFloat() * 0.5f,
            size = 8f + Random.nextFloat() * 12f,
            color = COLORS.random()
        )
    }
}

private data class RocketParticle(
    val startX: Float,
    val targetY: Float,
    val delaySec: Float,
    val riseDuration: Float,
    val size: Float,
    val color: Color
) {
    companion object {
        private val COLORS = listOf(
            Color(0xFFFFD54F),
            Color(0xFF4FC3F7),
            Color(0xFFFFB74D),
            Color(0xFFE57373),
            Color(0xFF81C784),
            Color(0xFFBA68C8),
            Color(0xFFFFEB3B),
            Color(0xFF64B5F6)
        )

        fun newRandom() = RocketParticle(
            startX = 0.05f + Random.nextFloat() * 0.9f,
            targetY = 0.10f + Random.nextFloat() * 0.40f,
            delaySec = Random.nextFloat() * 2.2f,
            riseDuration = 0.6f + Random.nextFloat() * 0.5f,
            size = 0.9f + Random.nextFloat() * 1.2f,
            color = COLORS.random()
        )
    }
}
