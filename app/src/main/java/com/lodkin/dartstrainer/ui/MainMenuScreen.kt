package com.lodkin.dartstrainer.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.lodkin.dartstrainer.theme.Accent
import com.lodkin.dartstrainer.theme.GoldAccent
import com.lodkin.dartstrainer.theme.TileBg
import com.lodkin.dartstrainer.theme.TileBgDark
import java.util.Locale

@Composable
fun MainMenuScreen(
    onTraining: () -> Unit,
    onFreePlay: () -> Unit,
    onStatsClick: () -> Unit,
    onSettingsClick: () -> Unit,
    avg501Ppr: Float? = null,
    avg501DoublesPercent: Float? = null,
    count501Matches: Int = 0
) {
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
                .weight(50f),
            avg501Ppr = avg501Ppr,
            avg501DoublesPercent = avg501DoublesPercent,
            count501Matches = count501Matches,
            onClick = onTraining
        )

        Spacer(Modifier.height(12.dp))

        // Просто поиграть с кнопкой статистики в правом нижнем углу
        FreePlayCard(
            modifier = Modifier
                .fillMaxWidth()
                .weight(50f),
            onFreePlay = onFreePlay,
            onStatsClick = onStatsClick
        )
    }
}

/**
 * Карточка «Тренировка».
 *
 * Сверху — заголовок и подпись. Снизу, если есть данные по 501,
 * показываем средний PPR и % удвоений по всем тренировочным партиям.
 */
@Composable
private fun TrainingCard(
    modifier: Modifier,
    avg501Ppr: Float?,
    avg501DoublesPercent: Float?,
    count501Matches: Int,
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
        }
    }
}

/**
 * Плюрализация для слова «партия».
 */
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
        // Центральный текст
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

        // Кнопка статистики в правом нижнем углу
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
