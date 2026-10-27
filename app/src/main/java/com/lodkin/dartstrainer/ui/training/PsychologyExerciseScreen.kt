package com.lodkin.dartstrainer.ui.training

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
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
import com.lodkin.dartstrainer.theme.Accent
import com.lodkin.dartstrainer.theme.DarkBg
import com.lodkin.dartstrainer.theme.GoldAccent
import com.lodkin.dartstrainer.theme.TileBg
import com.lodkin.dartstrainer.theme.TileBgDark
import kotlinx.coroutines.delay

@Composable
fun PsychologyExerciseScreen(
    exerciseId: String,
    onFinish: (exerciseId: String) -> Unit,
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
                "Психология",
                color = GoldAccent,
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(Modifier.height(24.dp))

        when (exerciseId) {
            "psy_ritual" -> TextExerciseContent(
                modifier = Modifier.weight(1f),
                title = "Ритуал трёх шагов",
                description = "Стабильность — залог победы. Одинаковые действия перед броском успокаивают нервную систему и настраивают мозг на выполнение задачи.",
                action = "Твоя задача: перед каждым броском выполнять одинаковую последовательность:\n\n1. Встань на одну и ту же точку на полу.\n2. Сделай глубокий вдох.\n3. Посмотри на цель 2 секунды.\n4. Брось дротик.",
                onFinish = { onFinish(exerciseId) }
            )

            "psy_visual" -> TextExerciseContent(
                modifier = Modifier.weight(1f),
                title = "Визуализация полёта",
                description = "Мозг не различает реальное действие и яркое представление. Визуализация «прогревает» нейронные связи, и рука бросает точнее.",
                action = "Твоя задача:\n\nПеред каждым броском закрой глаза на 3 секунды и представь, как дротик летит по идеальной дуге и втыкается ровно в центр цели. Открой глаза и бросай.",
                onFinish = { onFinish(exerciseId) }
            )

            "psy_ideomotor" -> TextExerciseContent(
                modifier = Modifier.weight(1f),
                title = "Идеомоторная тренировка",
                description = "Реальное улучшение техники без физических бросков. Мозг запоминает правильные движения, и потом рука делает их автоматически.",
                action = "Твоя задача:\n\nСядь на стул, закрой глаза и мысленно «прокрути» 3 идеальных броска: как ты встаешь, как замахиваешься, как разжимаешь пальцы, как дротик втыкается в центр.",
                onFinish = { onFinish(exerciseId) }
            )

            "psy_trigger_word" -> TextExerciseContent(
                modifier = Modifier.weight(1f),
                title = "Триггер-слово",
                description = "Создаёт условный рефлекс. Короткое слово — это сигнал для тела начать правильное движение, отключая лишние мысли.",
                action = "Твоя задача:\n\n1. Придумай короткое слово (например, «Цель», «Бросок», «Точно»).\n2. Сделай 5 подходов по 3 дротика.\n3. Перед каждым броском говори это слово про себя за секунду до замаха.",
                onFinish = { onFinish(exerciseId) }
            )

            "psy_mirror" -> TextExerciseContent(
                modifier = Modifier.weight(1f),
                title = "Зеркало",
                description = "Контроль техники и осознанность движений. Помогает увидеть и исправить ошибки в замахе, которые ты не замечаешь на автомате.",
                action = "Твоя задача:\n\n1. Встань перед зеркалом так, чтобы видеть руку с дротиком.\n2. Сделай 10 медленных, осознанных замахов без броска.\n3. Следи за тем, чтобы локоть не гулял, а кисть двигалась по прямой.",
                onFinish = { onFinish(exerciseId) }
            )

            "psy_breath" -> BreathExerciseContent(
                modifier = Modifier.weight(1f),
                onFinish = { onFinish(exerciseId) }
            )

            "psy_single" -> SingleDartExerciseContent(
                modifier = Modifier.weight(1f),
                onFinish = { onFinish(exerciseId) }
            )

            "psy_tunnel" -> TunnelVisionExerciseContent(
                modifier = Modifier.weight(1f),
                onFinish = { onFinish(exerciseId) }
            )

            else -> TextExerciseContent(
                modifier = Modifier.weight(1f),
                title = "Упражнение",
                description = "Описание отсутствует.",
                action = "",
                onFinish = { onFinish(exerciseId) }
            )
        }
    }
}

// ─────────────────────────────────────────────
// Текстовые упражнения
// ─────────────────────────────────────────────
@Composable
private fun TextExerciseContent(
    modifier: Modifier = Modifier,
    title: String,
    description: String,
    action: String,
    onFinish: () -> Unit
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                title,
                color = GoldAccent,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
            Spacer(Modifier.height(16.dp))
            Text(
                description,
                color = Color.White.copy(alpha = 0.7f),
                fontSize = 15.sp,
                lineHeight = 22.sp,
                textAlign = TextAlign.Center
            )
            Spacer(Modifier.height(24.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(TileBg)
                    .padding(16.dp)
            ) {
                Text(
                    action,
                    color = Color.White,
                    fontSize = 16.sp,
                    lineHeight = 24.sp
                )
            }
        }
        FinishButton(onFinish)
    }
}

// ─────────────────────────────────────────────
// 4: Дыхание 4-4-7
// ─────────────────────────────────────────────
@Composable
private fun BreathExerciseContent(
    modifier: Modifier = Modifier,
    onFinish: () -> Unit
) {
    var currentPhase by remember { mutableStateOf(0) }
    var timeLeft by remember { mutableStateOf(4) }
    var cyclesDone by remember { mutableStateOf(0) }

    LaunchedEffect(currentPhase, cyclesDone) {
        if (cyclesDone >= 3) return@LaunchedEffect
        
        val maxTime = if (currentPhase == 2) 7 else 4
        timeLeft = maxTime
        
        while (timeLeft > 0) {
            delay(1000L)
            timeLeft--
        }
        
        if (currentPhase < 2) {
            currentPhase++
        } else {
            currentPhase = 0
            cyclesDone++
        }
    }

    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text("Дыхание 4-4-7", color = GoldAccent, fontSize = 24.sp, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(8.dp))
            Text("Удлинённый выдох снижает пульс и убирает дрожь в руках.", color = Color.White.copy(alpha = 0.7f), fontSize = 14.sp, textAlign = TextAlign.Center)

            Spacer(Modifier.height(40.dp))

            if (cyclesDone >= 3) {
                Text("Упражнение выполнено!\nТы готов бросать.", color = Accent, fontSize = 20.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
            } else {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                    PhaseBox("ВДОХ", currentPhase == 0, timeLeft)
                    PhaseBox("ЗАДЕРЖКА", currentPhase == 1, timeLeft)
                    PhaseBox("ВЫДОХ", currentPhase == 2, timeLeft)
                }
                Spacer(Modifier.height(20.dp))
                Text("Цикл ${cyclesDone + 1} из 3", color = Color.White.copy(alpha = 0.5f), fontSize = 14.sp)
            }
        }
        FinishButton(onFinish)
    }
}

@Composable
private fun PhaseBox(label: String, isActive: Boolean, time: Int) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.width(90.dp).clip(RoundedCornerShape(12.dp)).background(if (isActive) Accent else TileBgDark).padding(vertical = 16.dp)
    ) {
        Text(label, color = if (isActive) Color(0xFF121212) else Color.White.copy(alpha = 0.5f), fontSize = 12.sp, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(8.dp))
        Text(if (isActive) "$time" else "-", color = if (isActive) Color(0xFF121212) else Color.White.copy(alpha = 0.5f), fontSize = 32.sp, fontWeight = FontWeight.Bold)
    }
}

// ─────────────────────────────────────────────
// 7: Один дротик
// ─────────────────────────────────────────────
@Composable
private fun SingleDartExerciseContent(
    modifier: Modifier = Modifier,
    onFinish: () -> Unit
) {
    var dartsThrown by remember { mutableStateOf(0) }
    val totalDarts = 15

    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text("Один дротик", color = GoldAccent, fontSize = 24.sp, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(8.dp))
            Text("Убирает расслабленность. Каждый бросок — решающий.", color = Color.White.copy(alpha = 0.7f), fontSize = 14.sp, textAlign = TextAlign.Center)

            Spacer(Modifier.height(40.dp))
            Text("Дротик $dartsThrown из $totalDarts", color = Color.White, fontSize = 24.sp, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(32.dp))

            if (dartsThrown < totalDarts) {
                Box(
                    modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(14.dp)).background(Accent).clickable { dartsThrown++ }.padding(vertical = 20.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text("СДЕЛАЛ БРОСОК", color = Color(0xFF121212), fontSize = 18.sp, fontWeight = FontWeight.Bold)
                }
            } else {
                Text("Все 15 бросков сделаны!\nОтличная концентрация.", color = Accent, fontSize = 20.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
            }
        }
        FinishButton(onFinish)
    }
}

// ─────────────────────────────────────────────
// 8: Туннельное зрение
// ─────────────────────────────────────────────
@Composable
private fun TunnelVisionExerciseContent(
    modifier: Modifier = Modifier,
    onFinish: () -> Unit
) {
    var timeLeft by remember { mutableStateOf(15) }
    var isFocusing by remember { mutableStateOf(true) }
    var dartsThrown by remember { mutableStateOf(0) }
    val totalDarts = 3

    LaunchedEffect(isFocusing, dartsThrown) {
        if (!isFocusing) return@LaunchedEffect
        timeLeft = 15
        while (timeLeft > 0) {
            delay(1000L)
            timeLeft--
        }
        if (timeLeft == 0) {
            isFocusing = false
        }
    }

    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text("Туннельное зрение", color = GoldAccent, fontSize = 24.sp, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(8.dp))
            Text("Учит мозг не отвлекаться на фон и концентрироваться на цели.", color = Color.White.copy(alpha = 0.7f), fontSize = 14.sp, textAlign = TextAlign.Center)

            Spacer(Modifier.height(40.dp))

            if (dartsThrown >= totalDarts) {
                Text("Упражнение выполнено!\nТы сконцентрировался на 3 бросках.", color = Accent, fontSize = 20.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
            } else {
                if (isFocusing) {
                    Text("Смотри на точку в центре мишени", color = Color.White, fontSize = 18.sp, textAlign = TextAlign.Center)
                    Spacer(Modifier.height(24.dp))
                    Text("$timeLeft", color = Accent, fontSize = 64.sp, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.height(24.dp))
                    Text("Не отводи взгляд", color = Color.White.copy(alpha = 0.5f), fontSize = 14.sp)
                } else {
                    Text("ВРЕМЯ ВЫШЛО! ДЕЛАЙ БРОСОК!", color = GoldAccent, fontSize = 24.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
                    Spacer(Modifier.height(32.dp))
                    Box(
                        modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(14.dp)).background(Accent).clickable { dartsThrown += 1; isFocusing = true }.padding(vertical = 20.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("БРОСОК СДЕЛАН", color = Color(0xFF121212), fontSize = 18.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
        FinishButton(onFinish)
    }
}

// ─────────────────────────────────────────────
// Общие элементы
// ─────────────────────────────────────────────
@Composable
private fun FinishButton(onFinish: () -> Unit) {
    Box(
        modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(14.dp)).background(GoldAccent).clickable { onFinish() }.padding(vertical = 16.dp),
        contentAlignment = Alignment.Center
    ) {
        Text("ЗАВЕРШИТЬ", color = Color(0xFF121212), fontSize = 16.sp, fontWeight = FontWeight.Bold)
    }
}
