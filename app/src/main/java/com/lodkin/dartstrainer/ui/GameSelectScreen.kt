package com.lodkin.dartstrainer.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
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
        // Верхняя панель
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

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            GameCard(
                title = "501",
                subtitle = "Классика. Дойди от 501 до нуля",
                modifier = Modifier.fillMaxWidth().height(140.dp),
                onClick = on501
            )
            GameCard(
                title = "Крикет",
                subtitle = "Закрой сектора 15–20 и Bull",
                modifier = Modifier.fillMaxWidth().height(140.dp),
                onClick = onCricket
            )
            GameCard(
                title = "Сектор",
                subtitle = "30 дротиков в один сектор. Нормативы для S20",
                modifier = Modifier.fillMaxWidth().height(140.dp),
                onClick = onSector
            )
            GameCard(
                title = "Кругосветка",
                subtitle = "Пройди сектора 1–20 и Bull по кругу",
                modifier = Modifier.fillMaxWidth().height(140.dp),
                onClick = onAroundClock
            )
            GameCard(
                title = "Большой раунд",
                subtitle = "21 сектор по 3 дротика. Нормативы разрядов",
                modifier = Modifier.fillMaxWidth().height(140.dp),
                onClick = onBigRound
            )
            GameCard(
                title = "Набор очков",
                subtitle = "10 подходов. Набери максимум очков. Нормативы",
                modifier = Modifier.fillMaxWidth().height(140.dp),
                onClick = onScoreSet
            )
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
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text(
                title,
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = GoldAccent,
                textAlign = TextAlign.Center
            )
            Text(
                subtitle,
                fontSize = 13.sp,
                color = Accent,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 24.dp)
            )
        }
    }
}
