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
fun StatsScreen(onBack: () -> Unit) {
    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
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
                "Статистика",
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
        }

        Spacer(Modifier.height(20.dp))

        Column(
            modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState())
        ) {
            // Уровень
            Text(
                "УРОВЕНЬ",
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium,
                color = Accent,
                letterSpacing = 3.sp
            )
            Spacer(Modifier.height(8.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(TileBgDark)
                    .padding(20.dp)
            ) {
                Column {
                    Text(
                        "Уровень пока не определён",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Spacer(Modifier.height(8.dp))
                    Text(
                        "Пройди первую тренировку — и здесь появятся твои результаты.",
                        fontSize = 13.sp,
                        color = Accent,
                        lineHeight = 18.sp
                    )
                }
            }

            Spacer(Modifier.height(24.dp))

            // Ключевые параметры
            Text(
                "КЛЮЧЕВЫЕ ПАРАМЕТРЫ",
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium,
                color = Accent,
                letterSpacing = 3.sp
            )
            Spacer(Modifier.height(8.dp))
            ParamRow("Сектор 20", "—")
            Spacer(Modifier.height(6.dp))
            ParamRow("Набор очков", "—")
            Spacer(Modifier.height(6.dp))
            ParamRow("Большой раунд", "—")
            Spacer(Modifier.height(6.dp))
            ParamRow("Удвоения", "—")

            Spacer(Modifier.height(24.dp))

            // Игровая статистика
            Text(
                "ИГРЫ 501",
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium,
                color = Accent,
                letterSpacing = 3.sp
            )
            Spacer(Modifier.height(8.dp))
            ParamRow("Средний набор", "—")
            Spacer(Modifier.height(6.dp))
            ParamRow("Побед", "—")
            Spacer(Modifier.height(6.dp))
            ParamRow("Лучший лег", "—")

            Spacer(Modifier.height(24.dp))

            // Чекауты
            Text(
                "ЧЕКАУТЫ",
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium,
                color = Accent,
                letterSpacing = 3.sp
            )
            Spacer(Modifier.height(8.dp))
            ParamRow("Уровень 1 (2–60)", "—")
            Spacer(Modifier.height(6.dp))
            ParamRow("Уровень 2 (60–100)", "—")
            Spacer(Modifier.height(6.dp))
            ParamRow("Уровень 3 (100–170)", "—")

            Spacer(Modifier.height(24.dp))

            // Достижения
            Text(
                "ДОСТИЖЕНИЯ",
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium,
                color = Accent,
                letterSpacing = 3.sp
            )
            Spacer(Modifier.height(8.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(TileBgDark)
                    .padding(20.dp)
            ) {
                Text(
                    "Пока нет достижений.\nТренируйся — и они появятся!",
                    fontSize = 13.sp,
                    color = Color.White,
                    lineHeight = 18.sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            Spacer(Modifier.height(24.dp))
        }
    }
}

@Composable
fun ParamRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(TileBg)
            .padding(horizontal = 16.dp, vertical = 14.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(label, color = Color.White, fontSize = 15.sp)
        Text(value, color = GoldAccent, fontSize = 18.sp, fontWeight = FontWeight.Bold)
    }
}
