package com.lodkin.dartstrainer.ui.game501

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.lodkin.dartstrainer.data.game501.Game501Entity
import com.lodkin.dartstrainer.theme.Accent
import com.lodkin.dartstrainer.theme.ErrorColor
import com.lodkin.dartstrainer.theme.GoldAccent
import com.lodkin.dartstrainer.theme.TileBg
import com.lodkin.dartstrainer.theme.TileBgDark
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Диалог со списком всех матчей 501 (незавершённые сверху, красным;
 * завершённые ниже). Тап по карточке:
 *  - незавершённая → onResume (продолжить партию)
 *  - завершённая → onViewMatch (показать подробную статистику матча)
 */
@Composable
fun MatchesListDialog501(
    games: List<Game501Entity>,
    onResume: (Game501Entity) -> Unit,
    onViewMatch: (Game501Entity) -> Unit,
    onDelete: (Game501Entity) -> Unit,
    onDismiss: () -> Unit
) {
    val unfinished = remember(games) { games.filter { !it.isFinished } }
    val finished = remember(games) { games.filter { it.isFinished } }

    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(onClick = onDismiss) { Text("Закрыть", color = Accent) }
        },
        title = {
            Column {
                Text(
                    "Список матчей",
                    color = Color.White,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    "Незавершённых: ${unfinished.size}   ·   Завершённых: ${finished.size}",
                    color = Color.White.copy(alpha = 0.6f),
                    fontSize = 12.sp
                )
            }
        },
        text = {
            if (games.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        "Пока нет ни одного матча.",
                        color = Color.White.copy(alpha = 0.7f),
                        fontSize = 14.sp
                    )
                }
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 480.dp)
                        .verticalScroll(rememberScrollState())
                ) {
                    // ── Незавершённые ──
                    if (unfinished.isNotEmpty()) {
                        SectionHeader("НЕЗАВЕРШЁННЫЕ", ErrorColor)
                        Spacer(Modifier.height(6.dp))
                        unfinished.forEach { e ->
                            UnfinishedMatchRow(
                                entity = e,
                                onResume = { onResume(e) },
                                onDelete = { onDelete(e) }
                            )
                            Spacer(Modifier.height(6.dp))
                        }
                        Spacer(Modifier.height(14.dp))
                    }

                    // ── Завершённые ──
                    if (finished.isNotEmpty()) {
                        SectionHeader("ЗАВЕРШЁННЫЕ", Accent)
                        Spacer(Modifier.height(6.dp))
                        finished.forEach { e ->
                            FinishedMatchRow(
                                entity = e,
                                onClick = { onViewMatch(e) }
                            )
                            Spacer(Modifier.height(6.dp))
                        }
                    }
                }
            }
        }
    )
}

@Composable
private fun SectionHeader(text: String, color: Color) {
    Text(
        text,
        color = color,
        fontSize = 11.sp,
        fontWeight = FontWeight.Bold,
        letterSpacing = 2.sp,
        modifier = Modifier.padding(vertical = 4.dp)
    )
}

// ─────────────────────────────────────────────
// Незавершённая партия — красная, с кнопками
// ─────────────────────────────────────────────
@Composable
private fun UnfinishedMatchRow(
    entity: Game501Entity,
    onResume: () -> Unit,
    onDelete: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(Color(0xFF3A1A1A))
            .padding(horizontal = 12.dp, vertical = 10.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                "⏸ НЕЗАВЕРШЕНА",
                color = ErrorColor,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 2.sp,
                modifier = Modifier.weight(1f)
            )
            Text(
                formatDate(entity.lastUpdateMillis.takeIf { it > 0 } ?: entity.dateMillis),
                color = Color.White.copy(alpha = 0.6f),
                fontSize = 11.sp
            )
        }
        Spacer(Modifier.height(4.dp))
        Text(
            entity.playerNames.split("|").joinToString("  ·  "),
            color = Color.White,
            fontSize = 13.sp,
            fontWeight = FontWeight.Medium
        )
        Spacer(Modifier.height(8.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(8.dp))
                    .background(Accent)
                    .clickable { onResume() }
                    .padding(vertical = 10.dp),
                contentAlignment = Alignment.Center
            ) {
                Text("Продолжить", color = Color(0xFF121212), fontSize = 13.sp, fontWeight = FontWeight.Bold)
            }
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(8.dp))
                    .background(TileBgDark)
                    .clickable { onDelete() }
                    .padding(vertical = 10.dp),
                contentAlignment = Alignment.Center
            ) {
                Text("Удалить", color = ErrorColor, fontSize = 13.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}

// ─────────────────────────────────────────────
// Завершённая партия — обычная, тап → подробная статистика
// ─────────────────────────────────────────────
@Composable
private fun FinishedMatchRow(
    entity: Game501Entity,
    onClick: () -> Unit
) {
    val names = entity.playerNames.split("|")
    val scores = entity.matchScore.split("|")
    val pprs = entity.ppr.split("|")
    val winnerIdx = entity.winnerIndex
    val winnerName = if (winnerIdx in names.indices) names[winnerIdx] else "—"
    val scoreLine = buildString {
        for (i in names.indices) {
            if (i > 0) append("   ·   ")
            append(names.getOrElse(i) { "?" })
            append(" ")
            append(scores.getOrElse(i) { "0" })
        }
    }
    val pprLine = buildString {
        for (i in pprs.indices) {
            if (i > 0) append("   ·   ")
            append("%.1f".format(Locale.US, pprs.getOrNull(i)?.toDoubleOrNull() ?: 0.0))
        }
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(TileBg)
            .clickable { onClick() }
            .padding(horizontal = 12.dp, vertical = 10.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                formatDate(entity.dateMillis),
                color = Color.White.copy(alpha = 0.6f),
                fontSize = 11.sp,
                modifier = Modifier.weight(1f)
            )
            Text(
                "🏆 $winnerName",
                color = GoldAccent,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold
            )
        }
        Spacer(Modifier.height(4.dp))
        Text(
            scoreLine,
            color = Color.White,
            fontSize = 13.sp,
            fontWeight = FontWeight.Medium
        )
        Spacer(Modifier.height(2.dp))
        Text(
            "PPR: $pprLine",
            color = Accent,
            fontSize = 11.sp
        )
        Spacer(Modifier.height(4.dp))
        Text(
            "Нажми, чтобы посмотреть подробную статистику",
            color = Color.White.copy(alpha = 0.45f),
            fontSize = 10.sp
        )
    }
}

private fun formatDate(millis: Long): String {
    val fmt = SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.US)
    return fmt.format(Date(millis))
}
