package com.lodkin.dartstrainer.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Language
import androidx.compose.material3.Icon
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
import com.lodkin.dartstrainer.data.training.TrainingDatabase
import com.lodkin.dartstrainer.data.training.TrainingRepository
import com.lodkin.dartstrainer.data.training.TrainingStatsAnalyzer
import com.lodkin.dartstrainer.theme.Accent
import com.lodkin.dartstrainer.theme.DarkBg
import com.lodkin.dartstrainer.theme.GoldAccent
import com.lodkin.dartstrainer.theme.TileBg
import com.lodkin.dartstrainer.theme.TileBgDark
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.Locale

/**
 * Главное меню.
 */
@Composable
fun MainMenuScreen(
    onTraining: () -> Unit,
    onFreePlay: () -> Unit,
    onStatsClick: () -> Unit,
    onSettingsClick: () -> Unit,
    onOpenDetailedStats: () -> Unit,
    onOnlineClick: () -> Unit
) {
    val context = LocalContext.current
    val repository = remember {
        TrainingRepository(TrainingDatabase.getInstance(context), context)
    }

    var avgPpr by remember { mutableStateOf<Float?>(null) }
    var avgDbl by remember { mutableStateOf<Float?>(null) }
    var count501 by remember { mutableStateOf(0) }

    var overallProgress by remember { mutableStateOf(0f) }
    var isReadyForNormatives by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        val analysis = withContext(Dispatchers.IO) {
            TrainingStatsAnalyzer.analyze(repository)
        }
        avgPpr = analysis.avg501Ppr
        avgDbl = analysis.avg501DoublesPercent
        count501 = analysis.count501Matches
        overallProgress = analysis.overallProgress
        isReadyForNormatives = analysis.isReadyForNormatives
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Заголовок с шестерёнкой справа
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Spacer(Modifier.size(48.dp))

            Text(
                text = "Darts Trainer",
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = GoldAccent,
                letterSpacing = 2.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier.weight(1f)
            )

            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(TileBgDark)
                    .clickable { onSettingsClick() },
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "⚙",
                    fontSize = 24.sp,
                    color = Accent
                )
            }
        }

        Spacer(Modifier.height(20.dp))

        // Тренировка
        TrainingCard(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            avg501Ppr = avgPpr,
            avg501DoublesPercent = avgDbl,
            count501Matches = count501,
            overallProgress = overallProgress,
            isReadyForNormatives = isReadyForNormatives,
            onOpenDetailedStats = onOpenDetailedStats,
            onClick = onTraining
        )

        Spacer(Modifier.height(12.dp))

        // КНОПКА ОНЛАЙН С ВЕКТОРНОЙ ИКОНКОЙ
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(60.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(Accent) // Яркий голубой цвет
                .clickable { onOnlineClick() },
            contentAlignment = Alignment.Center
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Language,
                    contentDescription = "Онлайн",
                    tint = DarkBg, // Черный (темный) цвет иконки
                    modifier = Modifier.size(24.dp)
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    text = "Играть онлайн",
                    color = DarkBg, // Черный (темный) цвет текста
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
            }
        }

        Spacer(Modifier.height(12.dp))

        // Просто поиграть с кнопкой статистики в правом нижнем углу
        FreePlayCard(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            onFreePlay = onFreePlay,
            onStatsClick = onStatsClick
        )
    }
}

/**
 * Карточка «Тренировка».
 */
@Composable
private fun TrainingCard(
    modifier: Modifier,
    avg501Ppr: Float?,
    avg501DoublesPercent: Float?,
    count501Matches: Int,
    overallProgress: Float,
    isReadyForNormatives: Boolean,
    onOpenDetailedStats: () -> Unit,
    onClick: () -> Unit
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(20.dp))
            .background(TileBg)
            .clickable { onClick() }
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "Тренировка",
                fontSize = 26.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                textAlign = TextAlign.Center
            )
            Text(
                text = "По программе тренера",
                fontSize = 14.sp,
                color = Accent,
                textAlign = TextAlign.Center
            )

            if (avg501Ppr != null && count501Matches > 0) {
                Spacer(Modifier.height(14.dp))
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(TileBgDark)
                        .padding(horizontal = 18.dp, vertical = 8.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                "PPR",
                                color = Color.White.copy(alpha = 0.55f),
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.sp
                            )
                            Spacer(Modifier.height(2.dp))
                            Text(
                                String.format(Locale.US, "%.1f", avg501Ppr),
                                color = GoldAccent,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Spacer(Modifier.width(20.dp))

                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                "УДВОЕНИЯ",
                                color = Color.White.copy(alpha = 0.55f),
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.sp
                            )
                            Spacer(Modifier.height(2.dp))
                            Text(
                                if (avg501DoublesPercent != null)
                                    "${avg501DoublesPercent.toInt()}%"
                                else "—",
                                color = GoldAccent,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
                Spacer(Modifier.height(4.dp))
                Text(
                    "$count501Matches ${plural501Matches(count501Matches)}",
                    color = Color.White.copy(alpha = 0.4f),
                    fontSize = 10.sp
                )
            }

            Spacer(Modifier.height(20.dp))

            // ── БОЛЬШАЯ КНОПКА ПОДРОБНОЙ СТАТИСТИКИ ──
            Box(
                modifier = Modifier
                    .fillMaxWidth(0.85f)
                    .clip(RoundedCornerShape(12.dp))
                    .background(if (isReadyForNormatives) GoldAccent.copy(alpha = 0.2f) else Accent.copy(alpha = 0.15f))
                    .clickable { onOpenDetailedStats() }
                    .padding(vertical = 12.dp, horizontal = 16.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "📊 Подробная статистика",
                        color = if (isReadyForNormatives) GoldAccent else Accent,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = if (isReadyForNormatives) "Пора сдавать экзамен!" else "До экзамена: ${(overallProgress * 100).toInt()}%",
                        color = Color.White.copy(alpha = 0.8f),
                        fontSize = 12.sp
                    )
                }
            }
        }
    }
}

private fun plural501Matches(n: Int): String {
    val mod10 = n % 10
    val mod100 = n % 100
    return when {
        mod100 in 11..19 -> "партий"
        mod10 == 1 -> "партия"
        mod10 in 2..4 -> "партии"
        else -> "партий"
    }
}

@Composable
private fun FreePlayCard(
    modifier: Modifier,
    onFreePlay: () -> Unit,
    onStatsClick: () -> Unit
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(20.dp))
            .background(TileBg)
            .clickable { onFreePlay() }
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "Просто поиграть",
                fontSize = 26.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                textAlign = TextAlign.Center
            )
            Text(
                text = "Свободная игра",
                fontSize = 14.sp,
                color = Accent,
                textAlign = TextAlign.Center
            )
        }

        Box(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(10.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(TileBgDark)
                .clickable { onStatsClick() }
                .padding(horizontal = 12.dp, vertical = 14.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("📊", fontSize = 22.sp)
                Spacer(Modifier.height(2.dp))
                Text(
                    "Статистика",
                    color = Accent,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(Modifier.height(2.dp))
                Text(
                    "матчи · прогресс · достижения",
                    color = Color.White.copy(alpha = 0.55f),
                    fontSize = 9.sp,
                    textAlign = TextAlign.Center,
                    lineHeight = 11.sp
                )
            }
        }
    }
}
