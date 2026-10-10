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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.lodkin.dartstrainer.data.training.PotentialSnapshot
import com.lodkin.dartstrainer.data.training.TrainingPlanner
import com.lodkin.dartstrainer.data.training.TrainingPlanner.TrainingPlan
import com.lodkin.dartstrainer.data.training.TrainingRepository
import com.lodkin.dartstrainer.data.training.WeaknessDetector
import com.lodkin.dartstrainer.data.training.WeaknessType
import com.lodkin.dartstrainer.theme.Accent
import com.lodkin.dartstrainer.theme.GoldAccent
import com.lodkin.dartstrainer.theme.TileBg
import com.lodkin.dartstrainer.theme.TileBgDark

/**
 * Экран «План дня».
 *
 * Показывает, из каких блоков состоит тренировка, с минутами.
 * Пока нет реальной статистики — использует стартовую программу (фокус на удвоения).
 */
@Composable
fun TrainingPlanScreen(
    playerName: String,
    minutes: Int,
    repository: TrainingRepository,
    onStartTraining: () -> Unit,
    onBack: () -> Unit
) {
    var snapshot by remember { mutableStateOf<PotentialSnapshot?>(null) }
    var loading by remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        snapshot = repository.getLastPotential()
        loading = false
    }

    val plan: TrainingPlan = remember(snapshot, minutes) {
        buildPlanFromSnapshot(snapshot, minutes)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0D1117))
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
                "План на сегодня",
                color = GoldAccent,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(Modifier.height(16.dp))

        if (loading) {
            Box(
                modifier = Modifier.fillMaxWidth().padding(40.dp),
                contentAlignment = Alignment.Center
            ) {
                Text("Загрузка...", color = Accent, fontSize = 16.sp)
            }
            return@Column
        }

        // ── Шапка: фокус и общее время ──
        PlanHeaderCard(playerName, plan)

        Spacer(Modifier.height(16.dp))

        // ── Список блоков ──
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .verticalScroll(rememberScrollState())
        ) {
            plan.blocks.forEachIndexed { index, block ->
                BlockRow(
                    index = index + 1,
                    title = block.title,
                    description = block.description,
                    minutes = block.minutes,
                    isMainFocus = block.isMainFocus
                )
                Spacer(Modifier.height(8.dp))
            }
        }

        Spacer(Modifier.height(12.dp))

        // ── Кнопка «Начать» ──
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(14.dp))
                .background(Accent)
                .clickable { onStartTraining() }
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
    }
}

// ─────────────────────────────────────────────
// Построение плана из snapshot
// ─────────────────────────────────────────────

private fun buildPlanFromSnapshot(
    snapshot: PotentialSnapshot?,
    minutes: Int
): TrainingPlan {
    // Пока у нас нет реальной статистики по всем 4 параметрам в snapshot,
    // используем только weakestType как главный фокус.
    val focusType = snapshot?.weakestType ?: WeaknessType.DOUBLES

    val main = WeaknessDetector.Weakness(
        type = focusType,
        currentValue = 0f,
        normValue = 0f,
        gapPercent = 0f,
        isFalling = false,
        priority = 1
    )
    val weaknesses = WeaknessDetector.WeaknessResult(
        weaknesses = listOf(main),
        mainWeakness = main
    )

    return TrainingPlanner.buildPlan(
        weaknesses = weaknesses,
        minutes = minutes,
        isControl = false,
        postponedDoubles = emptyList()
    )
}

// ─────────────────────────────────────────────
// Шапка плана
// ─────────────────────────────────────────────

@Composable
private fun PlanHeaderCard(playerName: String, plan: TrainingPlan) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(TileBg)
            .padding(16.dp)
    ) {
        Text(
            "$playerName, план на сегодня:",
            color = Color.White,
            fontSize = 15.sp,
            fontWeight = FontWeight.Medium
        )
        Spacer(Modifier.height(10.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text("Всего", color = Color.White.copy(alpha = 0.6f), fontSize = 12.sp)
                Text(
                    "${plan.totalMinutes} мин",
                    color = GoldAccent,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold
                )
            }
            Column(horizontalAlignment = Alignment.End) {
                Text("Фокус", color = Color.White.copy(alpha = 0.6f), fontSize = 12.sp)
                Text(
                    plan.focus?.let { WeaknessDetector.describe(it) } ?: "Общая",
                    color = Accent,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

// ─────────────────────────────────────────────
// Строка одного блока
// ─────────────────────────────────────────────

@Composable
private fun BlockRow(
    index: Int,
    title: String,
    description: String,
    minutes: Int,
    isMainFocus: Boolean
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(if (isMainFocus) Accent.copy(alpha = 0.12f) else TileBgDark)
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Номер блока
        Box(
            modifier = Modifier
                .size(32.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(if (isMainFocus) Accent else TileBg),
            contentAlignment = Alignment.Center
        ) {
            Text(
                "$index",
                color = if (isMainFocus) Color(0xFF121212) else Color.White,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    title,
                    color = Color.White,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )
                if (isMainFocus) {
                    Spacer(Modifier.width(6.dp))
                    Text(
                        "★",
                        color = GoldAccent,
                        fontSize = 14.sp
                    )
                }
            }
            Spacer(Modifier.height(2.dp))
            Text(
                description,
                color = Color.White.copy(alpha = 0.65f),
                fontSize = 12.sp
            )
        }

        Spacer(Modifier.width(8.dp))

        Text(
            "$minutes мин",
            color = if (isMainFocus) Accent else Color.White.copy(alpha = 0.8f),
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold
        )
    }
}
