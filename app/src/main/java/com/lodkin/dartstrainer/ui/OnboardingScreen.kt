package com.lodkin.dartstrainer.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.lodkin.dartstrainer.theme.Accent
import com.lodkin.dartstrainer.theme.GoldAccent
import com.lodkin.dartstrainer.theme.TileBg
import com.lodkin.dartstrainer.theme.TileBgDark

data class OnboardingResult(
    val name: String,
    val trainingMinutes: Int,
    val trainingsPerWeek: Int,
    val trainingMode: String,
    val startMode: String
)

@Composable
fun OnboardingScreen(onFinish: (OnboardingResult) -> Unit) {
    var step by remember { mutableStateOf(1) }
    var name by remember { mutableStateOf("") }
    var trainingMinutes by remember { mutableStateOf(60) }
    var trainingsPerWeek by remember { mutableStateOf(3) }
    var trainingMode by remember { mutableStateOf("flexible") }
    var startMode by remember { mutableStateOf("start") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = "Darts Trainer",
            fontSize = 26.sp,
            fontWeight = FontWeight.Bold,
            color = GoldAccent,
            letterSpacing = 2.sp,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)
        )
        Text(
            text = "Шаг $step из 5",
            fontSize = 13.sp,
            color = Accent,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp)
        )

        // Область с прокруткой + подсказка
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
        ) {
            val scrollState = rememberScrollState()
            val atBottom by remember {
                derivedStateOf {
                    scrollState.maxValue == 0 ||
                    scrollState.value >= scrollState.maxValue - 10
                }
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(scrollState)
            ) {
                when (step) {
                    1 -> StepName(name) { name = it }
                    2 -> StepTime(trainingMinutes) { trainingMinutes = it }
                    3 -> StepWeek(trainingsPerWeek) { trainingsPerWeek = it }
                    4 -> StepMode(trainingMode) { trainingMode = it }
                    5 -> StepStart(startMode) { startMode = it }
                }
                Spacer(Modifier.height(20.dp))
            }

            // Подсказка «прокрутите вниз» — исчезает, когда доскроллили
            if (!atBottom) {
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Accent.copy(alpha = 0.9f))
                        .padding(vertical = 10.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        "↓  Листайте, чтобы увидеть все варианты",
                        color = Color(0xFF121212),
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        Spacer(Modifier.height(16.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            if (step > 1) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(14.dp))
                        .background(TileBgDark)
                        .clickable { step -= 1 }
                        .padding(vertical = 16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text("Назад", color = Accent, fontSize = 16.sp, fontWeight = FontWeight.Medium)
                }
            }

            val canProceed = when (step) {
                1 -> name.isNotBlank()
                else -> true
            }

            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(14.dp))
                    .background(if (canProceed) TileBg else TileBgDark)
                    .clickable(enabled = canProceed) {
                        if (step < 5) {
                            step += 1
                        } else {
                            onFinish(
                                OnboardingResult(
                                    name = name.trim(),
                                    trainingMinutes = trainingMinutes,
                                    trainingsPerWeek = trainingsPerWeek,
                                    trainingMode = trainingMode,
                                    startMode = startMode
                                )
                            )
                        }
                    }
                    .padding(vertical = 16.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    if (step < 5) "Далее" else "Готово",
                    color = Color.White,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
private fun StepName(name: String, onChange: (String) -> Unit) {
    Text(
        "Как вас зовут?",
        fontSize = 22.sp,
        fontWeight = FontWeight.Bold,
        color = Color.White,
        modifier = Modifier.padding(bottom = 8.dp)
    )
    Text(
        "Приложение будет обращаться к вам по имени.",
        fontSize = 14.sp,
        color = Accent,
        modifier = Modifier.padding(bottom = 20.dp)
    )
    OutlinedTextField(
        value = name,
        onValueChange = onChange,
        modifier = Modifier.fillMaxWidth(),
        singleLine = true,
        placeholder = { Text("Например: Виктор", color = TileBg) },
        keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Words),
        colors = OutlinedTextFieldDefaults.colors(
            focusedTextColor = Color.White,
            unfocusedTextColor = Color.White,
            focusedBorderColor = Accent,
            unfocusedBorderColor = TileBg,
            cursorColor = Accent
        )
    )
}

@Composable
private fun StepTime(current: Int, onChange: (Int) -> Unit) {
    Text(
        "Сколько времени на тренировку?",
        fontSize = 22.sp,
        fontWeight = FontWeight.Bold,
        color = Color.White,
        modifier = Modifier.padding(bottom = 8.dp)
    )
    Text(
        "Программа будет строиться под это время. Можно выбрать «всегда разное» — тогда при старте каждой тренировки приложение спросит, сколько у вас сегодня.",
        fontSize = 14.sp,
        color = Accent,
        lineHeight = 18.sp,
        modifier = Modifier.padding(bottom = 16.dp)
    )

    TimeOption("30 минут", 30, "Быстро, легко вписать в день", "Только 1 блок", current, onChange)
    Spacer(Modifier.height(8.dp))
    TimeOption("45 минут", 45, "Оптимально для занятого дня", "Мало на полный цикл", current, onChange)
    Spacer(Modifier.height(8.dp))
    TimeOption("1 час", 60, "Разминка + 2 блока + силовые", "Хватает не на всё", current, onChange)
    Spacer(Modifier.height(8.dp))
    TimeOption("1 час 20 минут", 80, "Полноценная тренировка", "Мало на игру в конце", current, onChange)
    Spacer(Modifier.height(8.dp))
    TimeOption("1 час 40 минут", 100, "3 блока + игра", "Нужно время", current, onChange)
    Spacer(Modifier.height(8.dp))
    TimeOption("2 часа", 120, "Полный цикл, глубоко", "Требует отдачи", current, onChange)
    Spacer(Modifier.height(8.dp))
    TimeOption("Более 2 часов", 150, "Для подготовки к турнирам", "Для серьёзных спортсменов", current, onChange)
    Spacer(Modifier.height(8.dp))
    TimeOption("Всегда разное время", 0, "Свобода, под реальную жизнь", "Нужно каждый раз выбирать", current, onChange)
}

@Composable
private fun TimeOption(
    label: String,
    value: Int,
    pros: String,
    cons: String,
    current: Int,
    onChange: (Int) -> Unit
) {
    val selected = current == value
    val bg = if (selected) Color(0xFF1A2A33) else TileBgDark
    val borderColor = if (selected) Accent else TileBg

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(bg)
            .clickable { onChange(value) }
            .padding(horizontal = 12.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(20.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(borderColor),
            contentAlignment = Alignment.Center
        ) {
            if (selected) {
                Text("✓", color = Color(0xFF121212), fontSize = 14.sp, fontWeight = FontWeight.Bold)
            }
        }
        Spacer(Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                label,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = if (selected) Accent else Color.White
            )
            Spacer(Modifier.height(3.dp))
            Text("+ $pros", fontSize = 11.sp, color = Accent, lineHeight = 14.sp)
            Text("− $cons", fontSize = 11.sp, color = Color(0xFFB39DDB), lineHeight = 14.sp)
        }
    }
}

@Composable
private fun StepWeek(current: Int, onChange: (Int) -> Unit) {
    Text(
        "Сколько раз в неделю?",
        fontSize = 22.sp,
        fontWeight = FontWeight.Bold,
        color = Color.White,
        modifier = Modifier.padding(bottom = 8.dp)
    )
    Text(
        "Цель на неделю. Когда тренироваться — решаете сами.",
        fontSize = 14.sp,
        color = Accent,
        modifier = Modifier.padding(bottom = 20.dp)
    )
    val options = listOf(
        "2–3 раза" to 3,
        "4–5 раз" to 5,
        "6–7 раз" to 7,
        "Каждый день" to 7
    )
    options.forEach { (label, value) ->
        SelectRow(
            label = label,
            selected = current == value,
            onClick = { onChange(value) }
        )
        Spacer(Modifier.height(10.dp))
    }
}

@Composable
private fun StepMode(current: String, onChange: (String) -> Unit) {
    Text(
        "Режим тренировок",
        fontSize = 22.sp,
        fontWeight = FontWeight.Bold,
        color = Color.White,
        modifier = Modifier.padding(bottom = 8.dp)
    )
    Text(
        "Как удобнее тренироваться.",
        fontSize = 14.sp,
        color = Accent,
        modifier = Modifier.padding(bottom = 16.dp)
    )

    ModeCard(
        title = "По расписанию",
        description = "Выбираете конкретные дни недели. Приложение напоминает.",
        pros = "Дисциплина, привычка",
        cons = "Сложно при гибком графике",
        selected = current == "schedule",
        onClick = { onChange("schedule") }
    )
    Spacer(Modifier.height(12.dp))

    ModeCard(
        title = "Гибкий (рекомендуется)",
        description = "Говорите «хочу 3 тренировки в неделю». Когда — решаете сами.",
        pros = "Свобода, подходит большинству",
        cons = "Можно откладывать",
        selected = current == "flexible",
        onClick = { onChange("flexible") }
    )
    Spacer(Modifier.height(12.dp))

    ModeCard(
        title = "Свободный",
        description = "Без расписания. Тренируетесь когда хотите.",
        pros = "Максимальная свобода",
        cons = "Легко забросить",
        selected = current == "free",
        onClick = { onChange("free") }
    )
}

@Composable
private fun StepStart(current: String, onChange: (String) -> Unit) {
    Text(
        "Как определить уровень?",
        fontSize = 22.sp,
        fontWeight = FontWeight.Bold,
        color = Color.White,
        modifier = Modifier.padding(bottom = 8.dp)
    )
    Text(
        "Можно не проходить тест — уровень уточнится сам за 3–5 тренировок.",
        fontSize = 14.sp,
        color = Accent,
        modifier = Modifier.padding(bottom = 16.dp)
    )

    ModeCard(
        title = "Быстрая проверка",
        description = "15–20 минут упражнений — первое приближение к уровню.",
        pros = "Программа сразу ближе к вам",
        cons = "Результат одной тренировки неточен",
        selected = current == "test",
        onClick = { onChange("test") }
    )
    Spacer(Modifier.height(12.dp))

    ModeCard(
        title = "Стартовая программа (рекомендуется)",
        description = "Начинаете со стартовой. Уровень определится сам за 3–5 тренировок.",
        pros = "Мягкое начало, без стресса",
        cons = "Первые тренировки легковатые",
        selected = current == "start",
        onClick = { onChange("start") }
    )
    Spacer(Modifier.height(12.dp))

    ModeCard(
        title = "Оценить себя",
        description = "Скажете свой уровень сами: новичок, любитель, разрядник, профи.",
        pros = "Мгновенно",
        cons = "Самооценка может быть неточной",
        selected = current == "self",
        onClick = { onChange("self") }
    )
}

@Composable
private fun SelectRow(label: String, selected: Boolean, onClick: () -> Unit) {
    val bg = if (selected) Accent else TileBgDark
    val fg = if (selected) Color(0xFF121212) else Color.White
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(bg)
            .clickable { onClick() }
            .padding(vertical = 16.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            label,
            color = fg,
            fontSize = 16.sp,
            fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium
        )
    }
}

@Composable
private fun ModeCard(
    title: String,
    description: String,
    pros: String,
    cons: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    val borderColor = if (selected) Accent else TileBg
    val bg = if (selected) Color(0xFF1A2A33) else TileBgDark
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(bg)
            .clickable { onClick() }
            .padding(16.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(20.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(borderColor),
                contentAlignment = Alignment.Center
            ) {
                if (selected) {
                    Text("✓", color = Color(0xFF121212), fontSize = 14.sp, fontWeight = FontWeight.Bold)
                }
            }
            Spacer(Modifier.width(10.dp))
            Text(
                title,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = if (selected) Accent else Color.White
            )
        }
        Spacer(Modifier.height(8.dp))
        Text(
            description,
            fontSize = 13.sp,
            color = Color.White,
            lineHeight = 18.sp
        )
        Spacer(Modifier.height(8.dp))
        Text("Плюсы: $pros", fontSize = 12.sp, color = Accent)
        Text("Минусы: $cons", fontSize = 12.sp, color = Color(0xFFB39DDB))
    }
}
