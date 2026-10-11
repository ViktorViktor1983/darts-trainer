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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.lodkin.dartstrainer.data.SettingsStorage
import com.lodkin.dartstrainer.data.training.DartsNorms
import com.lodkin.dartstrainer.data.training.PotentialSnapshot
import com.lodkin.dartstrainer.data.training.TrainingRepository
import com.lodkin.dartstrainer.theme.Accent
import com.lodkin.dartstrainer.theme.GoldAccent
import com.lodkin.dartstrainer.theme.TileBg
import com.lodkin.dartstrainer.theme.TileBgDark

enum class TrainingSection {
    WARMUP,
    MAIN,
    PSYCHOLOGY,
    STRENGTH
}

/**
 * Главный экран тренировки.
 *
 * @param autoOpenTimeDialog  true — при заходе открыть диалог выбора времени
 *                            (обычно при входе из главного меню).
 *                            false — не открывать (например, после завершения тренировки).
 */
@Composable
fun TrainingMenuScreen(
    playerName: String,
    repository: TrainingRepository,
    autoOpenTimeDialog: Boolean,
    onStartTraining: () -> Unit,
    onBack: () -> Unit
) {
    val context = LocalContext.current

    var snapshot by remember { mutableStateOf<PotentialSnapshot?>(null) }
    var loading by remember { mutableStateOf(true) }

    val defaultMinutes = remember { SettingsStorage.getTrainingMinutes(context) }

    var showTimeDialog by remember { mutableStateOf(autoOpenTimeDialog) }
    var selectedMinutes by remember { mutableStateOf(defaultMinutes) }
    var openedSection by remember { mutableStateOf<TrainingSection?>(null) }

    LaunchedEffect(Unit) {
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

    openedSection?.let { section ->
        SectionPreviewDialog(
            section = section,
            minutes = selectedMinutes,
            onClose = { openedSection = null }
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0D1117))
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
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
        }

        Spacer(Modifier.height(16.dp))

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

        Spacer(Modifier.height(20.dp))

        SectionRow("🔥", "Разминка", estimateWarmup(selectedMinutes)) { openedSection = TrainingSection.WARMUP }
        Spacer(Modifier.height(10.dp))
        SectionRow("🎯", "Основная часть", estimateMain(selectedMinutes)) { openedSection = TrainingSection.MAIN }
        Spacer(Modifier.height(10.dp))
        SectionRow("🧠", "Психология", estimatePsychology(selectedMinutes)) { openedSection = TrainingSection.PSYCHOLOGY }
        Spacer(Modifier.height(10.dp))
        SectionRow("💪", "Силовая", estimateStrength()) { openedSection = TrainingSection.STRENGTH }

        Spacer(Modifier.height(24.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(14.dp))
                .background(Accent)
                .clickable {
                    saveSessionPrefs(context, selectedMinutes)
                    onStartTraining()
                }
                .padding(vertical = 16.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                "▶  Начать тренировку",
                color = Color(0xFF121212),
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(Modifier.height(24.dp))
    }
}

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

@Composable
private fun SectionPreviewDialog(
    section: TrainingSection,
    minutes: Int,
    onClose: () -> Unit
) {
    val preview = when (section) {
        TrainingSection.WARMUP -> SectionPreview(
            icon = "🔥",
            title = "Разминка",
            subtitle = "Разогреваем мышцы и настраиваем технику. Внутри ${estimateWarmup(minutes)} минут.",
            items = listOf(
                "Мышцы — вращения плечами, локтями, кистями, растяжка",
                "Траектория — броски без цели, фокус на движении руки",
                "Кучность — повторяем одинаковое движение",
                "Прицеливание — переходим к конкретным секторам"
            )
        )
        TrainingSection.MAIN -> SectionPreview(
            icon = "🎯",
            title = "Основная часть",
            subtitle = "Главные упражнения тренировки. Состав подбирается программой под тебя.",
            items = listOf(
                "Первое упражнение — из программы дня",
                "Второе упражнение — закрепление",
                "Игровой блок — партия на выбор (501 или Крикет)",
                "Между упражнениями — короткие паузы"
            )
        )
        TrainingSection.PSYCHOLOGY -> SectionPreview(
            icon = "🧠",
            title = "Психология",
            subtitle = "Настраиваем голову: дыхание, визуализация, ритуал.",
            items = listOf(
                "Дыхание 4-2-6 — снимаем напряжение",
                "Визуализация броска — настраиваемся перед важным броском",
                "Ритуал — последовательность действий, которая помогает"
            )
        )
        TrainingSection.STRENGTH -> SectionPreview(
            icon = "💪",
            title = "Силовая",
            subtitle = "Укрепляем мышцы, чтобы рука не садилась в конце турнира.",
            items = listOf(
                "Имитация броска с гантелей",
                "Удержание руки с гантелей",
                "Изометрическое удержание плеча",
                "Разгибания с эспандером",
                "Расслабление антагониста"
            )
        )
    }

    AlertDialog(
        onDismissRequest = onClose,
        containerColor = TileBgDark,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(preview.icon, fontSize = 22.sp)
                Spacer(Modifier.width(10.dp))
                Text(
                    preview.title,
                    color = GoldAccent,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        },
        text = {
            Column {
                Text(
                    preview.subtitle,
                    color = Color.White.copy(alpha = 0.75f),
                    fontSize = 13.sp
                )
                Spacer(Modifier.height(14.dp))
                Text(
                    "Что будет внутри:",
                    color = Accent,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
                Spacer(Modifier.height(8.dp))
                preview.items.forEach { item ->
                    Row(
                        modifier = Modifier.padding(vertical = 3.dp),
                        verticalAlignment = Alignment.Top
                    ) {
                        Text("•", color = Accent, fontSize = 14.sp)
                        Spacer(Modifier.width(8.dp))
                        Text(item, color = Color.White, fontSize = 13.sp)
                    }
                }
                Spacer(Modifier.height(14.dp))
                Text(
                    "Начать сразу с этого раздела нельзя — тренировка идёт по программе.",
                    color = Color.White.copy(alpha = 0.5f),
                    fontSize = 11.sp
                )
            }
        },
        confirmButton = {
            Text(
                "Понятно",
                color = Accent,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier
                    .clip(RoundedCornerShape(10.dp))
                    .background(Accent.copy(alpha = 0.15f))
                    .clickable { onClose() }
                    .padding(horizontal = 18.dp, vertical = 10.dp)
            )
        }
    )
}

private data class SectionPreview(
    val icon: String,
    val title: String,
    val subtitle: String,
    val items: List<String>
)

private fun estimateWarmup(totalMinutes: Int): Int =
    if (totalMinutes >= 100) 7 else 5

private fun estimatePsychology(totalMinutes: Int): Int =
    if (totalMinutes >= 80) 10 else 5

private fun estimateStrength(): Int = 5

private fun estimateMain(totalMinutes: Int): Int =
    (totalMinutes - estimateWarmup(totalMinutes) - estimatePsychology(totalMinutes) - estimateStrength())
        .coerceAtLeast(10)

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

@Composable
private fun SectionRow(
    icon: String,
    title: String,
    minutes: Int,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(TileBgDark)
            .clickable { onClick() }
            .padding(horizontal = 16.dp, vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(icon, fontSize = 22.sp)
        Spacer(Modifier.width(14.dp))
        Text(
            title,
            color = Color.White,
            fontSize = 16.sp,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.weight(1f)
        )
        Text(
            "~$minutes мин",
            color = Accent,
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold
        )
        Spacer(Modifier.width(10.dp))
        Text(
            "ℹ",
            color = Accent,
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold
        )
    }
}
