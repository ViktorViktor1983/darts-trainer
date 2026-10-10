package com.lodkin.dartstrainer.ui.training

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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.lodkin.dartstrainer.data.training.DartsNorms
import com.lodkin.dartstrainer.data.training.PotentialSnapshot
import com.lodkin.dartstrainer.data.training.TrainingRepository
import com.lodkin.dartstrainer.data.training.WeaknessType
import com.lodkin.dartstrainer.data.training.WeaknessDetector
import com.lodkin.dartstrainer.theme.Accent
import com.lodkin.dartstrainer.theme.GoldAccent
import com.lodkin.dartstrainer.theme.TileBg
import com.lodkin.dartstrainer.theme.TileBgDark

/**
 * Главный экран тренировочного раздела.
 *
 * Показывает:
 *   • Уровень игрока и название.
 *   • Прогресс внутри уровня (проценты).
 *   • Потенциал (лучший параметр).
 *   • Слабое место.
 *   • Кнопки: «Начать тренировку», «План дня», «Советы».
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
    var snapshot by remember { mutableStateOf<PotentialSnapshot?>(null) }
    var loading by remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        snapshot = repository.getLastPotential()
        loading = false
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
            // Ещё нет ни одной тренировки
            WelcomeNoTrainingCard(playerName)
        } else {
            // Есть данные — показываем уровень и потенциал
            LevelCard(playerName, snap)
            Spacer(Modifier.height(16.dp))
            FocusCard(snap)
        }

        Spacer(Modifier.height(24.dp))

        // ── Кнопки ──
        BigButton(
            title = "🏃 Начать тренировку",
            subtitle = "Программа на сегодня",
            isPrimary = true,
            onClick = onStartTraining
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
// Карточка «Уровень и потенциал»
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

        // Прогресс-бар (пока статичный — заполнение от 0 до 1, посчитаем позже)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(10.dp)
                .clip(RoundedCornerShape(5.dp))
                .background(TileBgDark)
        ) {
            // Позже заменим на реальный прогресс
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

// ─────────────────────────────────────────────
// Карточка «Фокус»
// ─────────────────────────────────────────────

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

// ─────────────────────────────────────────────
// Карточка «Пока нет тренировок»
// ─────────────────────────────────────────────

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

// ─────────────────────────────────────────────
// Большая кнопка
// ─────────────────────────────────────────────

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
