package com.lodkin.dartstrainer.ui.training

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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.lodkin.dartstrainer.data.SettingsStorage
import com.lodkin.dartstrainer.data.training.DartsNorms
import com.lodkin.dartstrainer.data.training.PotentialSnapshot
import com.lodkin.dartstrainer.data.training.TrainingPlanner
import com.lodkin.dartstrainer.data.training.TrainingRepository
import com.lodkin.dartstrainer.data.training.WeaknessDetector
import com.lodkin.dartstrainer.data.training.WeaknessType
import com.lodkin.dartstrainer.theme.Accent
import com.lodkin.dartstrainer.theme.GoldAccent
import com.lodkin.dartstrainer.theme.TileBg
import com.lodkin.dartstrainer.theme.TileBgDark

/**
 * Главный экран тренировочного раздела.
 *
 * При заходе показывает диалог выбора времени тренировки.
 * По умолчанию подсвечено время, выбранное в анкете.
 *
 * Если это контрольная тренировка (каждая 10-я), а время меньше 60 минут —
 * предлагает увеличить время или отложить экзамен.
 */
@Composable
fun TrainingMenuScreen(
    playerName: String,
    repository: TrainingRepository,
    onStartTraining: () -> Unit,
    onPlan: () -> Unit,
    onTips: () -> Unit,
    onBack: () -> Unit
) {
    val context = LocalContext.current

    var snapshot by remember { mutableStateOf<PotentialSnapshot?>(null) }
    var loading by remember { mutableStateOf(true) }
    var completedCount by remember { mutableStateOf(0) }

    // Время из анкеты (по умолчанию)
    val defaultMinutes = remember { SettingsStorage.getTrainingMinutes(context) }

    // Состояние диалогов
    var showTimeDialog by remember { mutableStateOf(true) }
    var showControlDialog by remember { mutableStateOf(false) }
    var selectedMinutes by remember { mutableStateOf(defaultMinutes) }

    LaunchedEffect(Unit) {
        snapshot = repository.getLastPotential()
        completedCount = repository.getCompletedSessionCount()
        loading = false
    }

    val isControl = TrainingPlanner.isControlSession(completedCount)

    // ── Диалог 1: выбор времени ──
    if (showTimeDialog) {
        TimeSelectionDialog(
            defaultMinutes = defaultMinutes,
            selectedMinutes = selectedMinutes,
            onSelect = { selectedMinutes = it },
            onConfirm = {
                if (isControl && selectedMinutes < 60) {
                    // Показываем предупреждение про контрольную
                    showTimeDialog = false
                    showControlDialog = true
                } else {
                    saveSessionPrefs(context, selectedMinutes, skipControl = false)
                    showTimeDialog = false
                    onStartTraining()
                }
            },
            onCancel = { showTimeDialog = false }
        )
    }

    // ── Диалог 2: контрольная и мало времени ──
    if (showControlDialog) {
        ControlTimeDialog(
            selectedMinutes = selectedMinutes,
            onIncreaseTo60 = {
                saveSessionPrefs(context, 60, skipControl = false)
                showControlDialog = false
                onStartTraining()
            },
            onPostpone = {
                saveSessionPrefs(context, selectedMinutes, skipControl = true)
                showControlDialog = false
                onStartTraining()
            },
            onCancel = { showControlDialog = false }
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
                fontWeight = FontWeight.Bold
            )
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
            Spacer(Modifier.height(16.dp))
            FocusCard(snap)
        }

        Spacer(Modifier.height(16.dp))

        // Строка с выбранным временем
        SessionInfoRow(
            selectedMinutes = selectedMinutes,
            completedCount = completedCount,
            isControl = isControl
        )

        Spacer(Modifier.height(16.dp))

        BigButton(
            title = "🏃 Начать тренировку",
            subtitle = "Программа на $selectedMinutes минут",
            isPrimary = true,
            onClick = {
                if (isControl && selectedMinutes < 60) {
                    showControlDialog = true
                } else {
                    saveSessionPrefs(context, selectedMinutes, skipControl = false)
                    onStartTraining()
                }
            }
        )
        Spacer(Modifier.height(10.dp))
        BigButton(
            title = "⏱ Изменить время",
            subtitle = "Сейчас выбрано: $selectedMinutes мин",
            isPrimary = false,
            onClick = { showTimeDialog = true }
        )
        Spacer(Modifier.height(10.dp))
        BigButton(
            title = "📋 План дня",
            subtitle = "Что тебя ждёт сегодня",
            isPrimary = false,
            onClick = onPlan
        )
        Spacer(Modifier.height(10.dp))
        BigButton(
            title = "💡 Советы",
            subtitle = "Психология и техника",
            isPrimary = false,
            onClick = onTips
        )

        Spacer(Modifier.height(24.dp))
    }
}

// ─────────────────────────────────────────────
// ДИАЛОГ 1: выбор времени тренировки
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

                // Варианты в 2 строки по 3
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
                "Начать",
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

// ─────────────────────────────────────────────
// ДИАЛОГ 2: контрольная и мало времени
// ─────────────────────────────────────────────

@Composable
private fun ControlTimeDialog(
    selectedMinutes: Int,
    onIncreaseTo60: () -> Unit,
    onPostpone: () -> Unit,
    onCancel: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onCancel,
        containerColor = TileBgDark,
        title = {
            Text(
                "Сегодня контрольная тренировка",
                color = GoldAccent,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column {
                Text(
                    "Контрольная тренировка — это экзамен, где ты сдаёшь нормативы " +
                            "и определяешь свой уровень. Она занимает около часа.",
                    color = Color.White.copy(alpha = 0.85f),
                    fontSize = 14.sp
                )
                Spacer(Modifier.height(12.dp))
                Text(
                    "Ты выбрал $selectedMinutes мин — этого мало для полной программы.",
                    color = Accent,
                    fontSize = 13.sp
                )
                Spacer(Modifier.height(12.dp))
                Text(
                    "Что делаем?",
                    color = Color.White,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        },
        confirmButton = {
            Text(
                "Увеличить до 60",
                color = Color(0xFF121212),
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier
                    .clip(RoundedCornerShape(10.dp))
                    .background(Accent)
                    .clickable { onIncreaseTo60() }
                    .padding(horizontal = 14.dp, vertical = 10.dp)
            )
        },
        dismissButton = {
            Text(
                "Отложить экзамен",
                color = Color.White.copy(alpha = 0.85f),
                fontSize = 14.sp,
                modifier = Modifier
                    .clip(RoundedCornerShape(10.dp))
                    .background(TileBg)
                    .clickable { onPostpone() }
                    .padding(horizontal = 14.dp, vertical = 10.dp)
            )
        }
    )
}

// ─────────────────────────────────────────────
// Сохранение выбора в SharedPreferences
// ─────────────────────────────────────────────

private fun saveSessionPrefs(
    context: android.content.Context,
    minutes: Int,
    skipControl: Boolean
) {
    context.getSharedPreferences("training_prefs", android.content.Context.MODE_PRIVATE)
        .edit()
        .putInt("session_minutes", minutes)
        .putBoolean("skip_control_exam", skipControl)
        .apply()
}

// ─────────────────────────────────────────────
// КАРТОЧКИ
// ─────────────────────────────────────────────

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
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(Modifier.width(12.dp))
            Text(
                DartsNorms.getLevelName(snap.level),
                color = Color.White,
                fontSize = 18.sp,
                fontWeight = FontWeight.Medium
            )
        }

        Spacer(Modifier.height(8.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(10.dp)
                .clip(RoundedCornerShape(5.dp))
                .background(TileBgDark)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(0.5f)
                    .height(10.dp)
                    .clip(RoundedCornerShape(5.dp))
                    .background(Accent)
            )
        }

        Spacer(Modifier.height(12.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text("Потенциал", color = Color.White.copy(alpha = 0.6f), fontSize = 12.sp)
                Text(
                    "${snap.potential}",
                    color = Accent,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )
            }
            Column(horizontalAlignment = Alignment.End) {
                Text("Слабое место", color = Color.White.copy(alpha = 0.6f), fontSize = 12.sp)
                Text(
                    WeaknessDetector.describe(snap.weakestType),
                    color = GoldAccent,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
private fun FocusCard(snap: PotentialSnapshot) {
    val description = when (snap.weakestType) {
        WeaknessType.DOUBLES ->
            "Сегодня потренируем удвоения — они решают исход партии."
        WeaknessType.SCORE ->
            "Сегодня фокус на набор очков — важно быстро прийти к чекауту."
        WeaknessType.ACCURACY ->
            "Сегодня тренируем точность в сектор — умение попасть туда, куда нужно."
        WeaknessType.TREBLES ->
            "Сегодня работаем над утроениями — база набора очков."
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(TileBgDark)
            .padding(16.dp)
    ) {
        Text(
            "ФОКУС ДНЯ",
            color = Accent,
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium,
            letterSpacing = 3.sp
        )
        Spacer(Modifier.height(8.dp))
        Text(
            WeaknessDetector.describe(snap.weakestType),
            color = GoldAccent,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold
        )
        Spacer(Modifier.height(6.dp))
        Text(
            description,
            color = Color.White.copy(alpha = 0.85f),
            fontSize = 14.sp
        )
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

@Composable
private fun SessionInfoRow(
    selectedMinutes: Int,
    completedCount: Int,
    isControl: Boolean
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(TileBgDark)
            .padding(12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                "Сегодняшняя тренировка",
                color = Color.White.copy(alpha = 0.7f),
                fontSize = 12.sp
            )
            Text(
                "$selectedMinutes мин",
                color = Accent,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold
            )
        }
        if (isControl) {
            Spacer(Modifier.height(4.dp))
            Text(
                "🎯 Сегодня контрольная — экзамен на уровень",
                color = GoldAccent,
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium
            )
        } else {
            Spacer(Modifier.height(4.dp))
            Text(
                "Тренировок всего: $completedCount. До экзамена: " +
                        "${TrainingPlanner.sessionsUntilControl(completedCount)}",
                color = Color.White.copy(alpha = 0.6f),
                fontSize = 11.sp
            )
        }
    }
}

@Composable
private fun BigButton(
    title: String,
    subtitle: String,
    isPrimary: Boolean,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(if (isPrimary) Accent else TileBgDark)
            .clickable { onClick() }
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                title,
                color = if (isPrimary) Color(0xFF121212) else Color.White,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(Modifier.height(2.dp))
            Text(
                subtitle,
                color = if (isPrimary) Color(0xFF121212).copy(alpha = 0.7f)
                else Color.White.copy(alpha = 0.6f),
                fontSize = 12.sp
            )
        }
        Text(
            "→",
            color = if (isPrimary) Color(0xFF121212) else Accent,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold
        )
    }
}
