package com.lodkin.dartstrainer.ui.training

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.lodkin.dartstrainer.data.training.TrainingBlocksStorage
import com.lodkin.dartstrainer.data.training.TrainingBotLevelStorage
import com.lodkin.dartstrainer.data.training.TrainingRepository
import com.lodkin.dartstrainer.data.training.TrainingStateStorage
import com.lodkin.dartstrainer.theme.Accent
import com.lodkin.dartstrainer.theme.ErrorColor
import com.lodkin.dartstrainer.theme.GoldAccent
import com.lodkin.dartstrainer.theme.TileBg
import com.lodkin.dartstrainer.theme.TileBgDark
import kotlinx.coroutines.launch

/**
 * Настройки тренировочного раздела.
 *
 * Здесь только то, что касается тренировки.
 * Общие настройки приложения — в SettingsScreen.
 */
@Composable
fun TrainingSettingsScreen(
    repository: TrainingRepository,
    onBack: () -> Unit,
    onAfterReset: () -> Unit = {}
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var warmupOn by remember { mutableStateOf(TrainingBlocksStorage.isWarmupEnabled(context)) }
    var psychologyOn by remember { mutableStateOf(TrainingBlocksStorage.isPsychologyEnabled(context)) }
    var strengthOn by remember { mutableStateOf(TrainingBlocksStorage.isStrengthEnabled(context)) }

    var showResetDialog by remember { mutableStateOf(false) }
    var resetDone by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0D1117))
            .padding(16.dp)
    ) {
        // ── Верхняя панель ──
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .background(TileBgDark)
                    .clickable { onBack() }
                    .padding(horizontal = 16.dp, vertical = 10.dp)
            ) {
                Text("← Назад", color = Accent, fontSize = 15.sp)
            }
            Spacer(Modifier.width(12.dp))
            Text(
                "Настройки тренировки",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
        }

        Spacer(Modifier.height(20.dp))

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .verticalScroll(rememberScrollState())
        ) {
            // ── Раздел «Блоки тренировки» ──
            Text(
                "БЛОКИ ТРЕНИРОВКИ",
                color = Accent,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 2.sp
            )
            Spacer(Modifier.height(6.dp))
            Text(
                "Отключи блоки, которые тебе не нужны. Программа будет их пропускать.",
                color = Color.White.copy(alpha = 0.6f),
                fontSize = 12.sp
            )
            Spacer(Modifier.height(12.dp))

            BlockToggleRow(
                icon = "🔥",
                title = "Разминка",
                subtitle = "Разогрев мышц и настройка техники",
                checked = warmupOn,
                enabled = true,
                onToggle = {
                    warmupOn = it
                    TrainingBlocksStorage.setWarmupEnabled(context, it)
                }
            )
            Spacer(Modifier.height(10.dp))

            BlockToggleRow(
                icon = "🎯",
                title = "Основная часть",
                subtitle = "Нельзя отключить — это суть тренировки",
                checked = true,
                enabled = false,
                onToggle = { }
            )
            Spacer(Modifier.height(10.dp))

            BlockToggleRow(
                icon = "🧠",
                title = "Психология",
                subtitle = "Дыхание, визуализация, ритуал",
                checked = psychologyOn,
                enabled = true,
                onToggle = {
                    psychologyOn = it
                    TrainingBlocksStorage.setPsychologyEnabled(context, it)
                }
            )
            Spacer(Modifier.height(10.dp))

            BlockToggleRow(
                icon = "💪",
                title = "Силовая",
                subtitle = "Упражнения с гантелями и эспандером",
                checked = strengthOn,
                enabled = true,
                onToggle = {
                    strengthOn = it
                    TrainingBlocksStorage.setStrengthEnabled(context, it)
                }
            )

            Spacer(Modifier.height(28.dp))

            // ── Раздел «Сброс данных тренировки» ──
            Text(
                "СБРОС ДАННЫХ ТРЕНИРОВКИ",
                color = Accent,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 2.sp
            )
            Spacer(Modifier.height(6.dp))
            Text(
                "Начать тренировочный прогресс с чистого листа. " +
                        "Игры и статистика «Просто поиграть» не тронутся.",
                color = Color.White.copy(alpha = 0.6f),
                fontSize = 12.sp
            )
            Spacer(Modifier.height(12.dp))

            if (resetDone) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(Accent.copy(alpha = 0.12f))
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        "✅ Данные тренировок сброшены",
                        color = Accent,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        "Уровень, слабые места, темп — всё начнётся заново.",
                        color = Color.White.copy(alpha = 0.7f),
                        fontSize = 12.sp
                    )
                }
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(TileBgDark)
                        .clickable { showResetDialog = true }
                        .padding(vertical = 16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            "Сбросить тренировочные данные",
                            color = ErrorColor,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(Modifier.height(4.dp))
                        Text(
                            "Уровень, слабое место, темп — начнётся заново",
                            color = Color.White.copy(alpha = 0.5f),
                            fontSize = 11.sp
                        )
                    }
                }
            }

            Spacer(Modifier.height(24.dp))

            Text(
                "Скоро появятся: настройка сложности ботов, выбор приоритетных упражнений, " +
                        "режим «только основная часть».",
                color = Color.White.copy(alpha = 0.4f),
                fontSize = 11.sp
            )

            Spacer(Modifier.height(24.dp))
        }
    }

    // ── Диалог подтверждения сброса ──
    if (showResetDialog) {
        AlertDialog(
            onDismissRequest = { showResetDialog = false },
            containerColor = TileBgDark,
            title = {
                Text(
                    "Сбросить данные тренировки?",
                    color = GoldAccent,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Text(
                    "Будут удалены:\n" +
                            "• все тренировочные сессии\n" +
                            "• результаты всех упражнений\n" +
                            "• снимки уровня и потенциала\n" +
                            "• отложенные даблы\n" +
                            "• уровень ботов в тренировке\n\n" +
                            "Игровая статистика («Просто поиграть») останется. " +
                            "Настройки блоков — тоже.",
                    color = Color.White,
                    fontSize = 13.sp,
                    lineHeight = 19.sp
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    showResetDialog = false
                    scope.launch {
                        repository.clearAllTrainingData()
                        TrainingStateStorage.clear(context)
                        TrainingBotLevelStorage.clear(context)
                        resetDone = true
                        onAfterReset()
                    }
                }) {
                    Text("СБРОСИТЬ", color = ErrorColor, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showResetDialog = false }) {
                    Text("Отмена", color = Accent)
                }
            }
        )
    }
}

// ─────────────────────────────────────────────
// Строка с переключателем блока
// ─────────────────────────────────────────────

@Composable
private fun BlockToggleRow(
    icon: String,
    title: String,
    subtitle: String,
    checked: Boolean,
    enabled: Boolean,
    onToggle: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(TileBgDark)
            .clickable(enabled = enabled) { onToggle(!checked) }
            .padding(horizontal = 14.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(icon, fontSize = 20.sp)
        Spacer(Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                title,
                color = if (enabled) Color.White else Color.White.copy(alpha = 0.5f),
                fontSize = 15.sp,
                fontWeight = FontWeight.Medium
            )
            Spacer(Modifier.height(2.dp))
            Text(
                subtitle,
                color = if (enabled) Color.White.copy(alpha = 0.55f)
                else Color.White.copy(alpha = 0.35f),
                fontSize = 11.sp
            )
        }
        Spacer(Modifier.width(8.dp))

        if (enabled) {
            Switch(
                checked = checked,
                onCheckedChange = { onToggle(it) },
                colors = SwitchDefaults.colors(
                    checkedThumbColor = Color(0xFF121212),
                    checkedTrackColor = Accent,
                    uncheckedThumbColor = Color.White.copy(alpha = 0.7f),
                    uncheckedTrackColor = TileBg
                )
            )
        } else {
            Box(
                modifier = Modifier
                    .width(52.dp)
                    .height(32.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(GoldAccent.copy(alpha = 0.25f))
            ) {
                Box(
                    modifier = Modifier
                        .padding(top = 2.dp, end = 2.dp)
                        .size(28.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(GoldAccent.copy(alpha = 0.8f))
                        .align(Alignment.CenterEnd)
                )
            }
        }
    }
}
