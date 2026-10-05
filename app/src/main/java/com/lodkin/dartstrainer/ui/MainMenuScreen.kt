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
            // Пустая ячейка слева (для симметрии)
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

            // Кнопка настроек — шестерёнка
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

        BigButton(
            title = "Тренировка",
            subtitle = "По программе тренера",
            modifier = Modifier
                .fillMaxWidth()
                .weight(40f),
            onClick = onTraining
        )

        Spacer(Modifier.height(12.dp))

        StatsBlock(
            modifier = Modifier
                .fillMaxWidth()
                .weight(20f),
            onClick = onStatsClick
        )

        Spacer(Modifier.height(12.dp))

        BigButton(
            title = "Просто поиграть",
            subtitle = "Свободная игра",
            modifier = Modifier
                .fillMaxWidth()
                .weight(40f),
            onClick = onFreePlay
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
fun StatsBlock(
    modifier: Modifier,
    onClick: () -> Unit
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(TileBgDark)
            .clickable { onClick() }
            .padding(horizontal = 16.dp, vertical = 10.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "ТВОЙ УРОВЕНЬ",
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium,
                color = Accent,
                letterSpacing = 3.sp
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = "Не определён",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                textAlign = TextAlign.Center
            )
            Spacer(Modifier.height(2.dp))
            Text(
                text = "Нажми, чтобы посмотреть статистику",
                fontSize = 10.sp,
                color = Accent,
                textAlign = TextAlign.Center
            )
        }
    }
}
