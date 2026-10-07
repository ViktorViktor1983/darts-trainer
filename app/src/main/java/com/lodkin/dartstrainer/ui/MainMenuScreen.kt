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

@Composable
fun MainMenuScreen(
    onTraining: () -> Unit,
    onFreePlay: () -> Unit,
    onStatsClick: () -> Unit,
    onSettingsClick: () -> Unit
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
        BigButton(
            title = "Тренировка",
            subtitle = "По программе тренера",
            modifier = Modifier
                .fillMaxWidth()
                .weight(50f),
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

@Composable
fun BigButton(
    title: String,
    subtitle: String,
    modifier: Modifier,
    onClick: () -> Unit
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(20.dp))
            .background(TileBg)
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = title,
                fontSize = 26.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                textAlign = TextAlign.Center
            )
            Text(
                text = subtitle,
                fontSize = 14.sp,
                color = Accent,
                textAlign = TextAlign.Center
            )
        }
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

        // Кнопка статистики в правом нижнем углу — выше в 3 раза
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
