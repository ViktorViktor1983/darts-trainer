package com.lodkin.dartstrainer.ui.training

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import com.lodkin.dartstrainer.data.training.DartsNorms
import com.lodkin.dartstrainer.data.training.ProblemSectorsStorage
import com.lodkin.dartstrainer.data.training.TrainingStatsAnalyzer
import com.lodkin.dartstrainer.theme.Accent
import com.lodkin.dartstrainer.theme.DarkBg
import com.lodkin.dartstrainer.theme.ErrorColor
import com.lodkin.dartstrainer.theme.GoldAccent
import com.lodkin.dartstrainer.theme.TileBg
import com.lodkin.dartstrainer.theme.TileBgDark
import kotlinx.coroutines.delay
import java.util.Locale

private data class TargetItem(
    val name: String,
    val displayName: String,
    val currentPercent: Float,
    val normPercent: Float,
    val isCompleted: Boolean
)

@Composable
fun ProblemSectorsScreen(
    analysis: TrainingStatsAnalyzer.TrainingAnalysis,
    playerLevel: Int,
    onFinish: () -> Unit,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    var isPlaying by remember { mutableStateOf(false) }
    var selectedTargets by remember { mutableStateOf<List<TargetItem>>(emptyList()) }
    var dartsCount by remember { mutableStateOf(15) }

    // Собираем все цели из анализа
    val allTargets = remember(analysis, playerLevel) {
        val list = mutableListOf<TargetItem>()
        val norm = DartsNorms.getNorm(playerLevel)
        val completed = ProblemSectorsStorage.getCompleted(context)

        analysis.worstSectors.take(3).forEach { w ->
            val name = if (w.sector == 25) "Bull" else "S${w.sector}"
            list.add(TargetItem(name, name, w.percent, norm.sectorsPercent, completed.contains(name)))
        }
        analysis.worstDoubles.take(3).forEach { w ->
            val name = if (w.sector == 25) "D-Bull" else "D${w.sector}"
            list.add(TargetItem(name, name, w.percent, norm.doublesPercent, completed.contains(name)))
        }
        analysis.worstTrebles.take(3).forEach { w ->
            val name = if (w.sector == 25) "T-Bull" else "T${w.sector}"
            list.add(TargetItem(name, name, w.percent, norm.treblePercent, completed.contains(name)))
        }
        list
    }

    if (isPlaying) {
        ProblemSectorsGame(
            targets = selectedTargets,
            dartsPerTarget = dartsCount,
            onFinishGame = {
                isPlaying = false
                onFinish()
            },
            onBack = { isPlaying = false }
        )
    } else {
        ProblemSectorsSetup(
            targets = allTargets,
            selectedTargets = selectedTargets,
            onToggleTarget = { target ->
                selectedTargets = if (selectedTargets.any { it.name == target.name }) {
                    selectedTargets.filter { it.name != target.name }
                } else {
                    selectedTargets + target
                }
            },
            dartsCount = dartsCount,
            onDartsCountChange = { dartsCount = it },
            onStart = { isPlaying = true },
            onBack = onBack
        )
    }
}

@Composable
private fun ProblemSectorsSetup(
    targets: List<TargetItem>,
    selectedTargets: List<TargetItem>,
    onToggleTarget: (TargetItem) -> Unit,
    dartsCount: Int,
    onDartsCountChange: (Int) -> Unit,
    onStart: () -> Unit,
    onBack: () -> Unit
) {
    val dartsOptions = listOf(15, 30, 45, 60, 75, 90, 105, 120)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBg)
            .padding(16.dp)
    ) {
        // Шапка
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier.clip(RoundedCornerShape(12.dp)).background(TileBgDark)
                    .clickable { onBack() }.padding(horizontal = 14.dp, vertical = 10.dp)
            ) { Text("← Назад", color = Accent, fontSize = 14.sp, fontWeight = FontWeight.Medium) }
            Spacer(Modifier.width(12.dp))
            Text("Проблемные сектора", color = GoldAccent, fontSize = 22.sp, fontWeight = FontWeight.Bold)
        }

        Spacer(Modifier.height(16.dp))
        
        Text(
            "Это отработка ваших слабых мест. Данные не идут в общую статистику, вы можете расслабиться и просто бросать дротики.",
            color = Color.White.copy(alpha = 0.7f),
            fontSize = 13.sp,
            lineHeight = 18.sp
        )

        Spacer(Modifier.height(20.dp))

        if (targets.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("Проблемных секторов пока нет.\nСыграйте 5 Кругосветок для сбора статистики.", color = Color.White.copy(alpha = 0.5f), textAlign = TextAlign.Center)
            }
            return@Column
        }

        Column(modifier = Modifier.weight(1f).verticalScroll(rememberScrollState())) {
            targets.forEach { target ->
                val isSelected = selectedTargets.any { it.name == target.name }
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(if (isSelected) Accent.copy(alpha = 0.15f) else TileBg)
                        .clickable { onToggleTarget(target) }
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(target.displayName, color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                            if (target.isCompleted) {
                                Spacer(Modifier.width(8.dp))
                                Text("✅", fontSize = 16.sp)
                            }
                        }
                        Spacer(Modifier.height(4.dp))
                        Text(
                            "Точность: ${String.format(Locale.US, "%.1f", target.currentPercent)}% (норма: ${String.format(Locale.US, "%.0f", target.normPercent)}%)",
                            color = Color.White.copy(alpha = 0.7f), fontSize = 13.sp
                        )
                    }
                    Box(
                        modifier = Modifier.size(24.dp).clip(RoundedCornerShape(12.dp))
                            .background(if (isSelected) Accent else TileBgDark),
                        contentAlignment = Alignment.Center
                    ) {
                        if (isSelected) Text("✓", color = Color(0xFF121212), fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        Spacer(Modifier.height(16.dp))

        Text("Количество дротиков на каждый сектор:", color = Accent, fontSize = 13.sp, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(8.dp))
        
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            dartsOptions.chunked(4).forEach { row ->
                Column(modifier = Modifier.weight(1f)) {
                    row.forEach { count ->
                        val isSel = dartsCount == count
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (isSel) Accent else TileBgDark)
                                .clickable { onDartsCountChange(count) }
                                .padding(vertical = 10.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("$count", color = if (isSel) Color(0xFF121212) else Color.White, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        Spacer(Modifier.height(16.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(14.dp))
                .background(if (selectedTargets.isEmpty()) Accent.copy(alpha = 0.3f) else Accent)
                .clickable(enabled = selectedTargets.isNotEmpty()) {
                    onStart()
                }
                .padding(vertical = 16.dp),
            contentAlignment = Alignment.Center
        ) {
            Text("НАЧАТЬ ОТРАБОТКУ", color = Color(0xFF121212), fontSize = 16.sp, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun ProblemSectorsGame(
    targets: List<TargetItem>,
    dartsPerTarget: Int,
    onFinishGame: () -> Unit,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    var showFinishDialog by remember { mutableStateOf(false) }
    var flashMiss by remember { mutableStateOf(false) }
    
    // Храним результаты бросков: индекс цели и попал/не попал
    val allThrows = remember { mutableStateListOf<Pair<Int, Boolean>>() }
    
    val currentTargetIndex = allThrows.size / dartsPerTarget
    val throwsInCurrent = allThrows.size % dartsPerTarget
    val isFinished = currentTargetIndex >= targets.size
    
    val currentTarget = if (isFinished) null else targets[currentTargetIndex]

    LaunchedEffect(flashMiss) {
        if (flashMiss) {
            delay(700L)
            if (!isFinished) {
                val remaining = 3 - (throwsInCurrent % 3)
                repeat(remaining) {
                    if (!isFinished) {
                        allThrows.add(currentTargetIndex to false)
                    }
                }
            }
            flashMiss = false
        }
    }

    fun addThrow(hit: Boolean) {
        if (isFinished || flashMiss) return
        allThrows.add(currentTargetIndex to hit)
    }

    fun missAllRemaining() {
        if (isFinished || flashMiss) return
        flashMiss = true
    }

    fun undo() {
        if (allThrows.isEmpty() || flashMiss) return
        allThrows.removeAt(allThrows.lastIndex)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBg)
    ) {
        // Шапка
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFF1A2332))
                .padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(10.dp))
                    .background(TileBg)
                    .clickable { onBack() }
                    .padding(horizontal = 14.dp, vertical = 10.dp)
            ) {
                Text("← Назад", color = Accent, fontSize = 14.sp, fontWeight = FontWeight.Bold)
            }
            Spacer(Modifier.width(8.dp))
            Column(
                modifier = Modifier.weight(1f),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    "Проблемные сектора",
                    color = Color.White,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )
                Text(
                    "Отработка (не идет в статистику)",
                    color = Accent,
                    fontSize = 12.sp,
                    textAlign = TextAlign.Center
                )
            }
            Spacer(Modifier.width(8.dp))
            Column(horizontalAlignment = Alignment.End) {
                Text("ДРОТИКОВ", color = Color(0xFF99AABB), fontSize = 10.sp, letterSpacing = 1.sp)
                Text("${allThrows.size}", color = GoldAccent, fontSize = 26.sp, fontWeight = FontWeight.Bold)
            }
        }

        // Прогресс-бар по целям
        Row(
            modifier = Modifier.fillMaxWidth().background(Color(0xFF0D1117)).padding(horizontal = 8.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            targets.forEachIndexed { idx, t ->
                val isPast = idx < currentTargetIndex
                val isCurrent = idx == currentTargetIndex
                val bg = when {
                    isFinished -> Color(0xFF4CAF50)
                    isCurrent -> Accent
                    isPast -> Color(0xFF4CAF50)
                    else -> TileBgDark
                }
                Box(
                    modifier = Modifier.weight(1f).height(26.dp).clip(RoundedCornerShape(4.dp)).background(bg),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        t.displayName,
                        color = if (isCurrent || isPast || isFinished) Color(0xFF121212) else Color.White.copy(alpha = 0.5f),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        // Центральный блок
        if (!isFinished && currentTarget != null) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFF16202C))
                    .padding(vertical = 6.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    "ЦЕЛЬ",
                    color = Color(0xFF99AABB),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 3.sp
                )
                Text(
                    currentTarget.displayName,
                    color = GoldAccent,
                    fontSize = 100.sp,
                    fontWeight = FontWeight.Bold,
                    lineHeight = 106.sp
                )
                Text(
                    "дротиков: $throwsInCurrent / $dartsPerTarget",
                    color = Color.White.copy(alpha = 0.6f),
                    fontSize = 14.sp
                )
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFF16202C))
                    .padding(vertical = 18.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    "ВСЕ СЕКТОРА ОТРАБОТАНЫ!",
                    color = GoldAccent,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
                Spacer(Modifier.height(6.dp))
                Text(
                    "Всего дротиков: ${allThrows.size}",
                    color = Color.White.copy(alpha = 0.7f),
                    fontSize = 15.sp
                )
            }
        }

        Spacer(Modifier.height(6.dp))

        // Ряды ввода (по 3 дротика, как в Кругосветке)
        if (!isFinished && currentTarget != null) {
            Column(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                for (i in 0..2) {
                    val globalIndex = (throwsInCurrent / 3) * 3 + i
                    val existing = allThrows.getOrNull(globalIndex)
                    val previewMiss = flashMiss && existing == null && i >= (throwsInCurrent % 3)
                    
                    ApproachRow(
                        targetLabel = currentTarget.displayName,
                        result = existing?.let { if (it.second) RowResult.HIT else RowResult.MISS },
                        enabled = !isFinished && !flashMiss && existing == null && i == (throwsInCurrent % 3),
                        previewMiss = previewMiss,
                        onHit = { addThrow(true) },
                        onMiss = { addThrow(false) }
                    )
                }
            }
        }

        Spacer(Modifier.height(8.dp))

        // Кнопки управления
        Column(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp).weight(1f),
            verticalArrangement = Arrangement.Bottom
        ) {
            if (!isFinished) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    val canMissAll = !isFinished && !flashMiss
                    Box(
                        modifier = Modifier.weight(1f).height(78.dp).clip(RoundedCornerShape(12.dp))
                            .background(if (canMissAll) TileBgDark else TileBgDark.copy(alpha = 0.4f))
                            .clickable(enabled = canMissAll) { missAllRemaining() },
                        contentAlignment = Alignment.Center
                    ) {
                        Text("Все мимо", color = if (canMissAll) Color.White else Color.White.copy(alpha = 0.4f), fontSize = 20.sp, fontWeight = FontWeight.Bold)
                    }

                    val canUndo = allThrows.isNotEmpty() && !flashMiss
                    Box(
                        modifier = Modifier.weight(1f).height(78.dp).clip(RoundedCornerShape(12.dp))
                            .background(if (canUndo) Color(0xFFFFE082) else Color(0xFFFFE082).copy(alpha = 0.35f))
                            .clickable(enabled = canUndo) { undo() },
                        contentAlignment = Alignment.Center
                    ) {
                        Text("↶ Ход назад", color = Color(0xFF3E2723), fontSize = 20.sp, fontWeight = FontWeight.Bold)
                    }
                }
            } else {
                Box(
                    modifier = Modifier.fillMaxWidth().height(60.dp).clip(RoundedCornerShape(14.dp))
                        .background(Accent).clickable { showFinishDialog = true },
                    contentAlignment = Alignment.Center
                ) {
                    Text("ЗАВЕРШИТЬ", color = Color(0xFF121212), fontSize = 16.sp, fontWeight = FontWeight.Bold)
                }
            }
            Spacer(Modifier.height(12.dp))
        }
    }

    if (showFinishDialog) {
        AlertDialog(
            onDismissRequest = { },
            containerColor = TileBgDark,
            confirmButton = {
                TextButton(onClick = {
                    targets.forEach { ProblemSectorsStorage.markCompleted(context, it.name) }
                    showFinishDialog = false
                    onFinishGame()
                }) { Text("ЗАВЕРШИТЬ", color = Accent) }
            },
            title = { Text("Отработка завершена!", color = GoldAccent, fontSize = 20.sp, fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    Text("Отличная работа. Эти сектора теперь помечены как пройденные.", color = Color.White, fontSize = 14.sp)
                    Spacer(Modifier.height(12.dp))
                    Text("Помни: данные не идут в статистику. Следующие проблемные сектора появятся после сдачи экзамена и набора новой статистики.", color = Color.White.copy(alpha = 0.7f), fontSize = 13.sp, lineHeight = 18.sp)
                }
            }
        )
    }
}

private enum class RowResult { HIT, MISS, EMPTY }

@Composable
private fun ApproachRow(
    targetLabel: String,
    result: RowResult?,
    enabled: Boolean,
    previewMiss: Boolean = false,
    onHit: () -> Unit,
    onMiss: () -> Unit
) {
    val currentResult = result ?: RowResult.EMPTY

    val missBg = when {
        previewMiss -> Color(0xFF8B2A2A)
        currentResult == RowResult.MISS -> Color(0xFF8B2A2A)
        currentResult == RowResult.HIT -> TileBg.copy(alpha = 0.5f)
        else -> TileBg
    }
    val hitBg = when (currentResult) {
        RowResult.HIT -> Color(0xFF4CAF50)
        RowResult.MISS -> TileBg.copy(alpha = 0.5f)
        RowResult.EMPTY -> TileBg
    }
    val missEnabled = enabled && currentResult == RowResult.EMPTY
    val hitEnabled = enabled && currentResult == RowResult.EMPTY

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Box(
            modifier = Modifier
                .weight(1f)
                .height(66.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(if (missEnabled || previewMiss) missBg else missBg.copy(alpha = 0.6f))
                .clickable(enabled = missEnabled) { onMiss() },
            contentAlignment = Alignment.Center
        ) {
            Text(
                "ПРОМАХ",
                color = when {
                    previewMiss -> Color.White
                    currentResult == RowResult.MISS -> Color.White
                    else -> Color.White.copy(alpha = if (missEnabled) 0.9f else 0.4f)
                },
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )
        }

        Box(
            modifier = Modifier
                .weight(1f)
                .height(66.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(if (hitEnabled) hitBg else hitBg.copy(alpha = 0.6f))
                .clickable(enabled = hitEnabled) { onHit() },
            contentAlignment = Alignment.Center
        ) {
            Text(
                targetLabel,
                color = if (currentResult == RowResult.HIT) Color.White
                else Color.White.copy(alpha = if (hitEnabled) 0.9f else 0.4f),
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}
