package com.lodkin.dartstrainer.ui.training

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
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
import com.lodkin.dartstrainer.theme.DarkBg
import com.lodkin.dartstrainer.theme.GoldAccent
import com.lodkin.dartstrainer.theme.TileBg
import com.lodkin.dartstrainer.theme.TileBgDark

@Composable
fun TrainingBlockScreen(
    exercises: List<PlannedExercise>,
    currentIndex: Int,
    onStartCurrent: () -> Unit,
    onSkipCurrent: () -> Unit,
    onBack: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBg)
            .padding(16.dp)
    ) {
        // Шапка
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .background(TileBgDark)
                    .clickable { onBack() }
                    .padding(horizontal = 14.dp, vertical = 10.dp)
            ) {
                Text("← Назад", color = Accent, fontSize = 14.sp, fontWeight = FontWeight.Medium)
            }
            Spacer(Modifier.width(12.dp))
            Text(
                "Упражнения",
                color = GoldAccent,
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(Modifier.height(20.dp))

        if (exercises.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text("Список упражнений пуст", color = Color.White.copy(alpha = 0.5f))
            }
            return@Column
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            itemsIndexed(exercises) { index, exercise ->
                val isCurrent = index == currentIndex
                val isPast = index < currentIndex
                val isFuture = index > currentIndex

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(if (isCurrent) Accent.copy(alpha = 0.15f) else TileBg)
                        .padding(16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Номер упражнения
                        Box(
                            modifier = Modifier
                                .size(28.dp)
                                .clip(RoundedCornerShape(14.dp))
                                .background(if (isCurrent) Accent else TileBgDark),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                "${index + 1}",
                                color = if (isCurrent) Color(0xFF121212) else Color.White,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Spacer(Modifier.width(12.dp))
                        
                        // Название и время
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                exercise.title,
                                color = if (isPast) Color.White.copy(alpha = 0.5f) else Color.White,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                "~${exercise.minutes} мин",
                                color = Color.White.copy(alpha = 0.5f),
                                fontSize = 12.sp
                            )
                        }
                        
                        // Статус
                        if (isPast) {
                            Text("✅", fontSize = 20.sp)
                        } else if (isCurrent) {
                            Text("▶", color = Accent, fontSize = 20.sp)
                        }
                    }

                    if (isCurrent) {
                        Spacer(Modifier.height(12.dp))
                        Text(
                            exercise.whyImportant,
                            color = Color.White.copy(alpha = 0.7f),
                            fontSize = 13.sp,
                            lineHeight = 18.sp
                        )
                        Spacer(Modifier.height(16.dp))
                        
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .height(48.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(Accent)
                                    .clickable { onStartCurrent() },
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    "НАЧАТЬ",
                                    color = Color(0xFF121212),
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .height(48.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(TileBgDark)
                                    .clickable { onSkipCurrent() },
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    "ПРОПУСТИТЬ",
                                    color = Color.White.copy(alpha = 0.7f),
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    } else if (isFuture) {
                        Spacer(Modifier.height(8.dp))
                        Text(
                            exercise.whyImportant,
                            color = Color.White.copy(alpha = 0.4f),
                            fontSize = 12.sp,
                            lineHeight = 16.sp
                        )
                    }
                }
            }
        }
    }
}
