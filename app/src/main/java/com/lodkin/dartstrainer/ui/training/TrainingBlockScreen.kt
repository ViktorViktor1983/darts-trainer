package com.lodkin.dartstrainer.ui.training

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.lodkin.dartstrainer.theme.Accent
import com.lodkin.dartstrainer.theme.DarkBg
import com.lodkin.dartstrainer.theme.GoldAccent
import com.lodkin.dartstrainer.theme.TileBg
import com.lodkin.dartstrainer.theme.TileBgDark
import kotlinx.coroutines.delay

@Composable
fun TrainingBlockScreen(
    exercises: List<PlannedExercise>,
    currentIndex: Int,
    showTimer: Boolean,
    onStartCurrent: () -> Unit,
    onSkipCurrent: () -> Unit,
    onBack: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBg)
            .padding(16.dp)
    ) {
        // Шапка
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .background(TileBgDark)
                    .clickable { onBack() }
                    .padding(horizontal = 14.dp, vertical = 10.dp)
            ) {
                Text("← Назад", color = Accent, fontSize = 14.sp, fontWeight = FontWeight.Medium)
            }
            Spacer(Modifier.width(12.dp))
            Text(
                "Упражнения",
                color = GoldAccent,
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(Modifier.height(20.dp))

        if (exercises.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text("Список упражнений пуст", color = Color.White.copy(alpha = 0.5f))
            }
            return@Column
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            itemsIndexed(exercises) { index, exercise ->
                val isCurrent = index == currentIndex
                val isPast = index < currentIndex
                val isFuture = index > currentIndex

                if (isCurrent) {
                    CurrentExerciseCard(
                        exercise = exercise,
                        showTimer = showTimer,
                        onFinish = onSkipCurrent,
                        onBack = onBack
                    )
                } else {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp))
                            .background(TileBg)
                            .padding(16.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(28.dp)
                                    .clip(RoundedCornerShape(14.dp))
                                    .background(TileBgDark),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    "${index + 1}",
                                    color = Color.White,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            Spacer(Modifier.width(12.dp))
                            
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    exercise.title,
                                    color = if (isPast) Color.White.copy(alpha = 0.5f) else Color.White,
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    "~${exercise.minutes} мин",
                                    color = Color.White.copy(alpha = 0.5f),
                                    fontSize = 12.sp
                                )
                            }
                            
                            if (isPast) {
                                Text("✅", fontSize = 20.sp)
                            } else if (isFuture) {
                                Text("▶", color = Accent, fontSize = 20.sp)
                            }
                        }
                        if (isFuture) {
                            Spacer(Modifier.height(8.dp))
                            Text(
                                exercise.whyImportant,
                                color = Color.White.copy(alpha = 0.4f),
                                fontSize = 12.sp,
                                lineHeight = 16.sp
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun CurrentExerciseCard(
    exercise: PlannedExercise,
    showTimer: Boolean,
    onFinish: () -> Unit,
    onBack: () -> Unit
) {
    var showPreview by remember { mutableStateOf(showTimer) }
    var previewTimeLeft by remember { mutableStateOf(5) }
    var timerStarted by remember { mutableStateOf(false) }

    // Определяем время для таймера
    val totalSeconds = when {
        exercise.id.startsWith("warm_darts") -> 2 * 60
        exercise.id.startsWith("warm_") -> 40
        else -> exercise.minutes * 60
    }

    // Логика превью (5 секунд)
    LaunchedEffect(Unit) {
        if (!showTimer) return@LaunchedEffect
        previewTimeLeft = 5
        while (previewTimeLeft > 0) {
            delay(1000L)
            previewTimeLeft--
        }
        showPreview = false
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(Accent.copy(alpha = 0.15f))
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(28.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(Accent),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    "1",
                    color = Color(0xFF121212),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )
            }
            Spacer(Modifier.width(12.dp))
            Text(
                exercise.title,
                color = Color.White,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(Modifier.height(12.dp))

        Text(
            exercise.whyImportant,
            color = Color.White.copy(alpha = 0.7f),
            fontSize = 13.sp,
            lineHeight = 18.sp
        )

        Spacer(Modifier.height(16.dp))

        if (showTimer) {
            if (showPreview) {
                Box(
                    modifier = Modifier.fillMaxWidth().height(120.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            "ПРИГОТОВЬСЯ",
                            color = Accent,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 2.sp
                        )
                        Spacer(Modifier.height(8.dp))
                        Text(
                            "$previewTimeLeft",
                            color = Color.White,
                            fontSize = 48.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            } else {
                TrainingTimer(
                    totalSeconds = totalSeconds,
                    isStarted = timerStarted,
                    onStart = { timerStarted = true },
                    onFinish = onFinish
                )
            }
        }

        Spacer(Modifier.height(16.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(48.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Accent)
                    .clickable { onStartCurrent() }, // Здесь вызов запуска игры, но для показателя "Начать" используем onStartCurrent
                contentAlignment = Alignment.Center
            ) {
                Text(
                    "НАЧАТЬ",
                    color = Color(0xFF121212),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )
            }
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(48.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(TileBgDark)
                    .clickable { onFinish() },
                contentAlignment = Alignment.Center
            ) {
                Text(
                    "ПРОПУСТИТЬ",
                    color = Color.White.copy(alpha = 0.7f),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

@Composable
private fun TrainingTimer(
    totalSeconds: Int,
    isStarted: Boolean,
    onStart: () -> Unit,
    onFinish: () -> Unit
) {
    var timeLeft by remember { mutableStateOf(totalSeconds) }
    var isRunning by remember { mutableStateOf(isStarted) }

    LaunchedEffect(isRunning, totalSeconds) {
        if (isRunning) {
            while (timeLeft > 0) {
                delay(1000L)
                timeLeft--
            }
            onFinish()
        }
    }

    val progress = if (totalSeconds > 0) timeLeft.toFloat() / totalSeconds else 0f
    val animatedProgress by animateFloatAsState(
        targetValue = progress,
        animationSpec = tween(durationMillis = 300)
    )

    val minutes = timeLeft / 60
    val seconds = timeLeft % 60
    val timeString = String.format("%02d:%02d", minutes, seconds)

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(120.dp),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.size(120.dp)) {
            val strokeWidth = 8.dp.toPx()
            val diameter = size.minDimension - strokeWidth
            
            drawArc(
                color = Color.White.copy(alpha = 0.1f),
                startAngle = -90f,
                sweepAngle = 360f,
                useCenter = false,
                topLeft = androidx.compose.ui.geometry.Offset(strokeWidth / 2, strokeWidth / 2),
                size = Size(diameter, diameter),
                style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
            )
            
            drawArc(
                color = Accent,
                startAngle = -90f,
                sweepAngle = 360f * animatedProgress,
                useCenter = false,
                topLeft = androidx.compose.ui.geometry.Offset(strokeWidth / 2, strokeWidth / 2),
                size = Size(diameter, diameter),
                style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
            )
        }
        
        Text(
            text = timeString,
            color = Color.White,
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold
        )
    }
    
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(48.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(if (isRunning) TileBgDark else Accent)
            .clickable { 
                if (!isStarted) {
                    onStart()
                    isRunning = true
                } else {
                    isRunning = !isRunning 
                }
            },
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = if (isRunning) "ПАУЗА" else if (!isStarted) "ЗАПУСТИТЬ ТАЙМЕР" else "ПРОДОЛЖИТЬ",
            color = if (isRunning) Color.White else Color(0xFF121212),
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold
        )
    }
}
