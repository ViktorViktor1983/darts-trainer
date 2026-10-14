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
import com.lodkin.dartstrainer.data.training.ExerciseResult
import com.lodkin.dartstrainer.data.training.TrainingRepository
import com.lodkin.dartstrainer.theme.Accent
import com.lodkin.dartstrainer.theme.ErrorColor
import com.lodkin.dartstrainer.theme.GoldAccent
import com.lodkin.dartstrainer.theme.TileBg
import com.lodkin.dartstrainer.theme.TileBgDark
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.Locale

/**
 * Итоговый экран свободной тренировки.
 *
 * Показывает, что игрок успел пройти за сессию:
 *   • сколько упражнений,
 *   • сколько времени,
 *   • краткую статистику по каждому упражнению.
 *
 * Кнопка «Завершить тренировку» — выход в главное меню.
 */
@Composable
fun FreeTrainingSummaryScreen(
    playerName: String,
    sessionId: Long,
    actualSeconds: Int,
    repository: TrainingRepository,
    onFinishTraining: () -> Unit
) {
    val actualMinutes = (actualSeconds / 60).coerceAtLeast(1)
    var results by remember { mutableStateOf<List<ExerciseResult>>(emptyList()) }
    var loading by remember { mutableStateOf(true) }

    LaunchedEffect(sessionId) {
        results = withContext(Dispatchers.IO) {
            if (sessionId > 0L) repository.getExerciseResults(sessionId) else emptyList()
        }
        loading = false
    }

    // Отфильтруем «пустышки» (без очков и дротиков) — например, психология.
    val visibleResults = results.filter { it.score > 0 || it.dartsThrown > 0 }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0D1117))
            .padding(16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .verticalScroll(rememberScrollState())
        ) {
            Spacer(Modifier.height(16.dp))

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(TileBg)
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("🎯", fontSize = 40.sp)
                Spacer(Modifier.height(10.dp))
                Text(
                    "Свободная тренировка",
                    color = GoldAccent,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )
                Spacer(Modifier.height(6.dp))
                Text(
                    "$playerName, вот что ты успел сделать.",
                    color = Color.White.copy(alpha = 0.8f),
                    fontSize = 14.sp,
                    textAlign = TextAlign.Center
                )
            }

            Spacer(Modifier.height(16.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(TileBgDark)
                    .padding(16.dp)
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text("Время", color = Color.White.copy(alpha = 0.6f), fontSize = 12.sp)
                    Spacer(Modifier.height(4.dp))
                    Text(
                        "$actualMinutes мин",
                        color = Accent,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
                Column(modifier = Modifier.weight(1f), horizontalAlignment = Alignment.End) {
                    Text("Упражнений", color = Color.White.copy(alpha = 0.6f), fontSize = 12.sp)
                    Spacer(Modifier.height(4.dp))
                    Text(
                        "${visibleResults.size}",
                        color = GoldAccent,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(Modifier.height(20.dp))

            if (loading) {
                Box(
                    modifier = Modifier.fillMaxWidth().padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text("Загрузка...", color = Accent, fontSize = 14.sp)
                }
            } else if (visibleResults.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(TileBgDark)
                        .padding(20.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        "Ты пока не прошёл ни одного упражнения.\n" +
                                "Заходи снова — попробуем!",
                        color = Color.White.copy(alpha = 0.75f),
                        fontSize = 14.sp,
                        textAlign = TextAlign.Center,
                        lineHeight = 20.sp
                    )
                }
            } else {
                Text(
                    "ЧТО СДЕЛАНО",
                    color = Accent,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 2.sp
                )
                Spacer(Modifier.height(10.dp))

                visibleResults.forEach { r ->
                    ResultRow(r)
                    Spacer(Modifier.height(8.dp))
                }
            }

            Spacer(Modifier.height(20.dp))
        }

        // ── Кнопка завершения ──
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(14.dp))
                .background(Accent)
                .clickable { onFinishTraining() }
                .padding(vertical = 16.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                "Завершить тренировку",
                color = Color(0xFF121212),
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(Modifier.height(8.dp))
    }
}

// ─────────────────────────────────────────────
// Строка результата
// ─────────────────────────────────────────────

@Composable
private fun ResultRow(result: ExerciseResult) {
    val title = titleFor(result.exerciseId)
    val details = parseDetails(result.detailsJson)
    val won = details["won"]

    // Правая колонка — ключевая цифра
    val primaryValue: String = when (result.exerciseId) {
        "game501" -> {
            val ppr = details["ppr"]?.toFloatOrNull()
            if (ppr != null) "PPR %.1f".format(Locale.US, ppr)
            else "PPR ${result.score}"
        }
        "cricket" -> {
            val mpr = details["mpr"]?.toFloatOrNull()
            if (mpr != null) "MPR %.1f".format(Locale.US, mpr)
            else "MPR ${result.score}"
        }
        "sector20", "scoreset", "biground" -> "${result.score} очков"
        "aroundclock_sectors", "aroundclock_doubles", "aroundclock_trebles" ->
            "${result.score} ${pluralDarts(result.score)}"
        else -> "${result.score}"
    }

    // Дополнительная подпись
    val subLine: String? = when (result.exerciseId) {
        "game501" -> {
            val dbl = details["dbl"]
            val base = if (dbl != null) "удвоения $dbl%" else null
            when {
                won == "true" && base != null -> "победа · $base"
                won == "false" && base != null -> "поражение · $base"
                won == "true" -> "победа"
                won == "false" -> "поражение"
                else -> base
            }
        }
        "cricket" -> when (won) {
            "true" -> "победа"
            "false" -> "поражение"
            else -> null
        }
        "aroundclock_sectors", "aroundclock_doubles", "aroundclock_trebles" ->
            "${result.dartsThrown} ${pluralDarts(result.dartsThrown)}"
        else -> null
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(TileBgDark)
            .padding(14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                title,
                color = Color.White,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium
            )
            if (!subLine.isNullOrBlank()) {
                Spacer(Modifier.height(2.dp))
                Text(
                    subLine,
                    color = Color.White.copy(alpha = 0.55f),
                    fontSize = 11.sp
                )
            }
        }
        Text(
            primaryValue,
            color = GoldAccent,
            fontSize = 15.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

// ─────────────────────────────────────────────
// Хелперы
// ─────────────────────────────────────────────

private fun parseDetails(s: String): Map<String, String> {
    if (s.isBlank()) return emptyMap()
    return s.split(";").mapNotNull { entry ->
        val parts = entry.split("=")
        if (parts.size == 2) parts[0].trim() to parts[1].trim() else null
    }.toMap()
}

private fun titleFor(exerciseId: String): String {
    // У "extra_" упражнений ключ тот же, что у обычного — просто убираем префикс.
    val cleanId = exerciseId.removePrefix("extra_")
    return when (cleanId) {
        "aroundclock_doubles" -> "Кругосветка: Удвоения"
        "aroundclock_trebles" -> "Кругосветка: Утроения"
        "aroundclock_sectors" -> "Кругосветка: Сектора"
        "sector20" -> "Сектор 20"
        "scoreset" -> "Набор очков"
        "biground" -> "Большой раунд"
        "game501" -> "501 против бота"
        "cricket" -> "Крикет против бота"
        else -> exerciseId
    }
}

private fun pluralDarts(n: Int): String {
    val mod10 = n % 10
    val mod100 = n % 100
    return when {
        mod100 in 11..19 -> "дротиков"
        mod10 == 1 -> "дротик"
        mod10 in 2..4 -> "дротика"
        else -> "дротиков"
    }
}
