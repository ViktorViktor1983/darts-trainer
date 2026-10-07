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
fun GameSelectScreen(
    onCricket: () -> Unit,
    on501: () -> Unit,
    onSector: () -> Unit,
    onAroundClock: () -> Unit,
    onBigRound: () -> Unit,
    onScoreSet: () -> Unit,
    onBack: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
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
                "Выбор игры",
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
        }

        Spacer(Modifier.height(20.dp))

        Text(
            "Во что сыграем?",
            fontSize = 18.sp,
            color = Color.White,
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Center
        )

        Spacer(Modifier.height(20.dp))

        // ── Сетка 2×3 ──
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Ряд 1: 501 | Крикет
            Row(
                modifier = Modifier.fillMaxWidth().weight(1f),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                GameCard(
                    title = "501",
                    subtitle = "Дойди от 501 до нуля",
                    modifier = Modifier.fillMaxHeight().weight(1f),
                    onClick = on501
                )
                GameCard(
                    title = "Крикет",
                    subtitle = "Сектора 15–20 и Bull",
                    modifier = Modifier.fillMaxHeight().weight(1f),
                    onClick = onCricket
                )
            }

            // Ряд 2: Сектор | Большой раунд
            Row(
                modifier = Modifier.fillMaxWidth().weight(1f),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                GameCard(
                    title = "Сектор",
                    subtitle = "30 дротиков в один сектор",
                    modifier = Modifier.fillMaxHeight().weight(1f),
                    onClick = onSector
                )
                GameCard(
                    title = "Большой раунд",
                    subtitle = "21 сектор × 3 дротика",
                    modifier = Modifier.fillMaxHeight().weight(1f),
                    onClick = onBigRound
                )
            }

            // Ряд 3: Набор очков | Кругосветка
            Row(
                modifier = Modifier.fillMaxWidth().weight(1f),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                GameCard(
                    title = "Набор очков",
                    subtitle = "10 подходов на максимум",
                    modifier = Modifier.fillMaxHeight().weight(1f),
                    onClick = onScoreSet
                )
                GameCard(
                    title = "Кругосветка",
                    subtitle = "Сектора 1–20 и Bull по кругу",
                    modifier = Modifier.fillMaxHeight().weight(1f),
                    onClick = onAroundClock
                )
            }
        }
    }
}

@Composable
private fun GameCard(
    title: String,
    subtitle: String,
    modifier: Modifier,
    onClick: () -> Unit
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(20.dp))
            .background(TileBg)
            .clickable { onClick() }
            .padding(8.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text(
                title,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = GoldAccent,
                textAlign = TextAlign.Center,
                lineHeight = 24.sp
            )
            Text(
                subtitle,
                fontSize = 11.sp,
                color = Accent,
                textAlign = TextAlign.Center,
                lineHeight = 14.sp,
                modifier = Modifier.padding(horizontal = 4.dp)
            )
        }
    }
}
