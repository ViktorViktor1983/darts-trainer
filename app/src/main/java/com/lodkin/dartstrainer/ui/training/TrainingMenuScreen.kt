package com.lodkin.dartstrainer.ui.training

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.lodkin.dartstrainer.data.SettingsStorage
import com.lodkin.dartstrainer.data.training.DartsNorms
import com.lodkin.dartstrainer.data.training.PotentialSnapshot
import com.lodkin.dartstrainer.data.training.TrainingBlocksStorage
import com.lodkin.dartstrainer.data.training.TrainingRepository
import com.lodkin.dartstrainer.theme.Accent
import com.lodkin.dartstrainer.theme.GoldAccent
import com.lodkin.dartstrainer.theme.TileBg
import com.lodkin.dartstrainer.theme.TileBgDark

@Composable
fun TrainingMenuScreen(
    playerName: String,
    repository: TrainingRepository,
    @Suppress("UNUSED_PARAMETER") autoOpenTimeDialog: Boolean,
    onStartTraining: () -> Unit,
    onStartFreeTraining: () -> Unit,
    onOpenSettings: () -> Unit,
    onOpenProgress: () -> Unit,
    onBack: () -> Unit
) {
    val context = LocalContext.current

    var snapshot by remember { mutableStateOf<PotentialSnapshot?>(null) }
    var loading by remember { mutableStateOf(true) }

    val defaultMinutes = remember { SettingsStorage.getTrainingMinutes(context) }

    var showTimeDialog by remember { mutableStateOf(false) }
    var selectedMinutes by remember { mutableStateOf(defaultMinutes) }

    // Читаем флаги блоков — только чтобы понять, включены они или нет
    // (нужно для блока «Сегодня» и для того, чтобы настройки не потерялись).
    LaunchedEffect(Unit) {
        TrainingBlocksStorage.isWarmupEnabled(context)
        TrainingBlocksStorage.isPsychologyEnabled(context)
        TrainingBlocksStorage.isStrengthEnabled(context)
        snapshot = repository.getLastPotential()
        loading = false
    }

    if (showTimeDialog) {
        TimeSelectionDialog(
            defaultMinutes = defaultMinutes,
            selectedMinutes = selectedMinutes,
            onSelect = { selectedMinutes = it },
            onConfirm = {
                saveSessionPrefs(context, selectedMinutes)
                showTimeDialog = false
            },
            onCancel = { showTimeDialog = false }
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0D1117))
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        // ── Верхняя панель ──
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
                "Тренировка",
                color = GoldAccent,
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.weight(1f)
            )
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Accent.copy(alpha = 0.18f))
                    .clickable { onOpenProgress() },
                contentAlignment = Alignment.Center
            ) {
                Text("📊", fontSize = 20.sp)
            }
            Spacer(Modifier.width(8.dp))
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Accent.copy(alpha = 0.18f))
                    .clickable { onOpenSettings() },
                contentAlignment = Alignment.Center
            ) {
                Text("⚙", fontSize = 20.sp, color = Accent)
            }
        }

        Spacer(Modifier.height(20.dp))

        if (loading) {
            Box(
                modifier = Modifier.fillMaxWidth().padding(40.dp),
                contentAlignment = Alignment.Center
            ) {
                Text("Загрузка...", color = Accent, fontSize = 16.sp)
            }
            return@Column
        }

        val snap = snapshot
        if (snap == null) {
            WelcomeNoTrainingCard(playerName)
        } else {
            LevelCard(playerName, snap)
        }

        Spacer(Modifier.height(16.dp))

        // ── Кнопка «Свободная тренировка» с пульсацией ──
        FreeTrainingButton(onClick = onStartFreeTraining)

        Spacer(Modifier.height(10.dp))

        // ── Кнопка «По времени с выбором» ──
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(14.dp))
                .background(Accent),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clickable {
                        saveSessionPrefs(context, selectedMinutes)
                        onStartTraining()
                    }
                    .padding(vertical = 16.dp, horizontal = 14.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    "▶  По времени · $selectedMinutes мин",
                    color = Color(0xFF121212),
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
            }
            Box(
                modifier = Modifier
                    .padding(end = 6.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(Color(0xFF121212).copy(alpha = 0.18f))
                    .clickable { showTimeDialog = true }
                    .padding(horizontal = 14.dp, vertical = 12.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    "▼",
                    color = Color(0xFF121212),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        Spacer(Modifier.height(20.dp))

        // ── Плашка «Сегодня» ──
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(TileBgDark)
                .padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                "Сегодня",
                color = Color.White.copy(alpha = 0.7f),
                fontSize = 13.sp,
                modifier = Modifier.weight(1f)
            )
            Text(
                "$selectedMinutes мин",
                color = Accent,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(Modifier.width(12.dp))
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(TileBg)
                    .clickable { showTimeDialog = true }
                    .padding(horizontal = 10.dp, vertical = 6.dp)
            ) {
                Text("Изменить", color = Accent, fontSize = 12.sp, fontWeight = FontWeight.Medium)
            }
        }

        Spacer(Modifier.height(24.dp))
    }
}

/**
 * Кнопка «Свободная тренировка» с мягкой пульсацией текста и значка.
 * Период: 0,7 сек на затухание + 0,7 сек на возврат.
 */
@Composable
private fun FreeTrainingButton(onClick: () -> Unit) {
    val infiniteTransition = rememberInfiniteTransition(label = "free_pulse")
    val alpha by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 0.35f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 700, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "free_pulse_alpha"
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(TileBgDark)
            .clickable { onClick() }
            .padding(vertical = 16.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                "⏱  Свободная тренировка",
                color = Accent.copy(alpha = alpha),
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(Modifier.height(3.dp))
            Text(
                "без времени · в любой момент можно закончить",
                color = Color.White.copy(alpha = 0.5f),
                fontSize = 11.sp
            )
        }
    }
}

// ─────────────────────────────────────────────
// ДИАЛОГ ВЫБОРА ВРЕМЕНИ
// ─────────────────────────────────────────────

@Composable
private fun TimeSelectionDialog(
    defaultMinutes: Int,
    selectedMinutes: Int,
    onSelect: (Int) -> Unit,
    onConfirm: () -> Unit,
    onCancel: () -> Unit
) {
    val options = listOf(30, 45, 60, 80, 100, 120)

    AlertDialog(
        onDismissRequest = onCancel,
        containerColor = TileBgDark,
        title = {
            Text(
                "Сколько времени на тренировку?",
                color = GoldAccent,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column {
                Text(
                    "В анкете ты выбрал $defaultMinutes мин. " +
                            "Сегодня можешь выбрать другое время.",
                    color = Color.White.copy(alpha = 0.8f),
                    fontSize = 13.sp
                )
                Spacer(Modifier.height(16.dp))

                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        options.take(3).forEach { m ->
                            TimeOptionButton(m, m == selectedMinutes, Modifier.weight(1f), onSelect)
                        }
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        options.drop(3).forEach { m ->
                            TimeOptionButton(m, m == selectedMinutes, Modifier.weight(1f), onSelect)
                        }
                    }
                }
            }
        },
        confirmButton = {
            Text(
                "Готово",
                color = Accent,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier
                    .clip(RoundedCornerShape(10.dp))
                    .background(Accent.copy(alpha = 0.15f))
                    .clickable { onConfirm() }
                    .padding(horizontal = 18.dp, vertical = 10.dp)
            )
        },
        dismissButton = {
            Text(
                "Отмена",
                color = Color.White.copy(alpha = 0.7f),
                fontSize = 15.sp,
                modifier = Modifier
                    .clip(RoundedCornerShape(10.dp))
                    .clickable { onCancel() }
                    .padding(horizontal = 18.dp, vertical = 10.dp)
            )
        }
    )
}

@Composable
private fun TimeOptionButton(
    minutes: Int,
    selected: Boolean,
    modifier: Modifier,
    onSelect: (Int) -> Unit
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(10.dp))
            .background(if (selected) Accent else TileBg)
            .clickable { onSelect(minutes) }
            .padding(vertical = 14.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            "$minutes мин",
            color = if (selected) Color(0xFF121212) else Color.White,
            fontSize = 14.sp,
            fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium
        )
    }
}

private fun saveSessionPrefs(context: android.content.Context, minutes: Int) {
    context.getSharedPreferences("training_prefs", android.content.Context.MODE_PRIVATE)
        .edit()
        .putInt("session_minutes", minutes)
        .apply()
}

@Composable
private fun LevelCard(playerName: String, snap: PotentialSnapshot) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(TileBg)
            .padding(16.dp)
    ) {
        Text(
            "Привет, $playerName!",
            color = Color.White,
            fontSize = 16.sp,
            fontWeight = FontWeight.Medium
        )
        Spacer(Modifier.height(12.dp))

        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                "Уровень ${snap.level}",
                color = GoldAccent,
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(Modifier.width(10.dp))
            Text(
                DartsNorms.getLevelName(snap.level),
                color = Color.White,
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium
            )
        }

        Spacer(Modifier.height(10.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                "Потенциал",
                color = Color.White.copy(alpha = 0.6f),
                fontSize = 12.sp
            )
            Text(
                "${snap.potential}",
                color = Accent,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
private fun WelcomeNoTrainingCard(playerName: String) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(TileBg)
            .padding(20.dp)
    ) {
        Text(
            "Привет, $playerName!",
            color = Color.White,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold
        )
        Spacer(Modifier.height(10.dp))
        Text(
            "Уровень пока не определён. Пройди первую тренировку — и я начну подбирать программу под тебя.",
            color = Color.White.copy(alpha = 0.85f),
            fontSize = 14.sp
        )
        Spacer(Modifier.height(10.dp))
        Text(
            "Каждые 10 тренировок — экзамен, на котором подтвердишь свой уровень.",
            color = Accent,
            fontSize = 13.sp
        )
    }
}
