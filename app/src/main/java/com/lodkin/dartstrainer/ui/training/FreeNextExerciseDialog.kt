package com.lodkin.dartstrainer.ui.training

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.lodkin.dartstrainer.theme.Accent
import com.lodkin.dartstrainer.theme.ErrorColor
import com.lodkin.dartstrainer.theme.GoldAccent
import com.lodkin.dartstrainer.theme.TileBgDark

/**
 * Экран «Следующее упражнение» в свободной тренировке.
 *
 * В свободном режиме список упражнений идёт по кругу бесконечно —
 * поэтому здесь нет счётчика «N / total», только порядковый номер
 * пройденных упражнений в этой сессии.
 */
@Composable
internal fun FreeNextExerciseDialog(
    exerciseTitle: String,
    exerciseMinutes: Int,
    completedSoFar: Int,
    onPlay: () -> Unit,
    onSkip: () -> Unit,
    onFinish: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0D1117)),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp)
                .clip(RoundedCornerShape(20.dp))
                .background(TileBgDark)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                if (completedSoFar > 0) "ПРОЙДЕНО: $completedSoFar" else "СВОБОДНАЯ ТРЕНИРОВКА",
                color = Accent,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 2.sp
            )
            Spacer(Modifier.height(12.dp))
            Text(
                "Сейчас будет:",
                color = Color.White.copy(alpha = 0.7f),
                fontSize = 13.sp
            )
            Spacer(Modifier.height(6.dp))
            Text(
                exerciseTitle,
                color = GoldAccent,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(Modifier.height(4.dp))
            Text(
                "~$exerciseMinutes мин",
                color = Color.White.copy(alpha = 0.55f),
                fontSize = 13.sp
            )

            Spacer(Modifier.height(22.dp))

            // «Играть»
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(Accent)
                    .clickable { onPlay() }
                    .padding(vertical = 16.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    "▶  Играть",
                    color = Color(0xFF121212),
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(Modifier.height(10.dp))

            // «Пропустить»
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(Color(0xFF1A2332))
                    .clickable { onSkip() }
                    .padding(vertical = 14.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    "Пропустить",
                    color = Color.White.copy(alpha = 0.85f),
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Medium
                )
            }

            Spacer(Modifier.height(16.dp))

            Text(
                "─── или ───",
                color = Color.White.copy(alpha = 0.3f),
                fontSize = 12.sp
            )

            Spacer(Modifier.height(16.dp))

            // «Закончить тренировку»
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(ErrorColor.copy(alpha = 0.15f))
                    .clickable { onFinish() }
                    .padding(vertical = 14.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    "⬛  Закончить тренировку",
                    color = ErrorColor,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}
