package com.lodkin.dartstrainer.ui.about

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.lodkin.dartstrainer.theme.Accent
import com.lodkin.dartstrainer.theme.GoldAccent
import com.lodkin.dartstrainer.theme.TileBg
import com.lodkin.dartstrainer.theme.TileBgDark

/**
 * Экран «О программе».
 *
 * Один длинный скроллируемый экран с раскрывающимися карточками.
 * Каждый раздел — отдельная карточка, при тапе раскрывается текст.
 */
@Composable
fun AboutScreen(onBack: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0D1117))
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        // ── Верхняя панель ──
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
                Text(
                    "← Назад",
                    color = Accent,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium
                )
            }
            Spacer(Modifier.width(12.dp))
            Text(
                "О программе",
                color = GoldAccent,
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(Modifier.height(8.dp))

        Text(
            "Здесь простым языком рассказано, как устроено приложение, " +
                    "что и зачем мы делаем. Если что-то непонятно — загляни сюда.",
            color = Color.White.copy(alpha = 0.7f),
            fontSize = 13.sp,
            lineHeight = 19.sp
        )

        Spacer(Modifier.height(20.dp))

        // ── Разделы ──
        ExpandableCard(
            icon = "📊",
            title = "Статистика",
            content = { StatisticsContent() }
        )
        Spacer(Modifier.height(10.dp))
        ExpandableCard(
            icon = "🎯",
            title = "Уровень и рекорды",
            content = { LevelAndRecordsContent() }
        )
        ExpandableCard(
            icon = "📋",
            title = "Сдача нормативов",
            content = { NormativesContent() }
        )
        Spacer(Modifier.height(10.dp))
        ExpandableCard(
            icon = "🤖",
            title = "Боты",
            content = { BotsContent() }
        )
        Spacer(Modifier.height(10.dp))
        ExpandableCard(
            icon = "🏋️",
            title = "Тренировка",
            content = { TrainingContent() }
        )
        Spacer(Modifier.height(10.dp))
        ExpandableCard(
            icon = "💡",
            title = "Полезные фишки",
            content = { FeaturesContent() }
        )
        Spacer(Modifier.height(10.dp))
        ExpandableCard(
            icon = "ℹ️",
            title = "О программе",
            content = { AboutContent() }
        )

        Spacer(Modifier.height(24.dp))
    }
}

// ============================================================
// Раскрывающаяся карточка
// ============================================================

@Composable
private fun ExpandableCard(
    icon: String,
    title: String,
    content: @Composable () -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(TileBg)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { expanded = !expanded }
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(icon, fontSize = 22.sp)
            Spacer(Modifier.width(12.dp))
            Text(
                title,
                color = Color.White,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.weight(1f)
            )
            Text(
                if (expanded) "▲" else "▼",
                color = Accent,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold
            )
        }

        AnimatedVisibility(
            visible = expanded,
            enter = fadeIn(tween(200)) + expandVertically(tween(200)),
            exit = fadeOut(tween(150)) + shrinkVertically(tween(150))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(TileBgDark)
                    .padding(16.dp)
            ) {
                content()
            }
        }
    }
}

// ============================================================
// Разделы
// ============================================================

@Composable
private fun StatisticsContent() {
    Text(
        "Две отдельные статистики",
        color = GoldAccent,
        fontSize = 14.sp,
        fontWeight = FontWeight.Bold
    )
    Spacer(Modifier.height(6.dp))
    Paragraph(
        "В приложении две статистики, которые не смешиваются между собой. " +
                "Одна — для тренировок (это влияет на уровень и программу). " +
                "Вторая — для свободной игры «Просто поиграть» (это просто для информации)."
    )

    Spacer(Modifier.height(14.dp))

    Text(
        "Что считается в тренировке",
        color = GoldAccent,
        fontSize = 14.sp,
        fontWeight = FontWeight.Bold
    )
    Spacer(Modifier.height(6.dp))
    Bullet("Набор очков — из Сектора 20 и упражнения «Набор».")
    Bullet("Точность в сектор — из Большого раунда и Кругосветки-Сектора.")
    Bullet("Удвоения — из Кругосветки-Удвоения и партий 501.")
    Bullet("Утроения — из Кругосветки-Утроения.")
    Bullet("Игры 501 и Крикет — отдельно (PPR и MPR).")

    Spacer(Modifier.height(14.dp))

    Text(
        "Честность цифр",
        color = GoldAccent,
        fontSize = 14.sp,
        fontWeight = FontWeight.Bold
    )
    Spacer(Modifier.height(6.dp))
    Paragraph(
        "Мы не показываем цифру, если данных мало. Пока не набралось хотя бы " +
                "несколько записей — увидишь пометку «Мало данных», а не случайное число."
    )

    Spacer(Modifier.height(14.dp))

    Text(
        "Личный прогресс",
        color = GoldAccent,
        fontSize = 14.sp,
        fontWeight = FontWeight.Bold
    )
    Spacer(Modifier.height(6.dp))
    Paragraph(
        "Мы сравниваем тебя только с тобой самим. Никаких рейтингов и сравнений " +
                "с другими игроками — только твой собственный путь."
    )

    Spacer(Modifier.height(14.dp))

    Text(
        "Проблемные сектора",
        color = GoldAccent,
        fontSize = 14.sp,
        fontWeight = FontWeight.Bold
    )
    Spacer(Modifier.height(6.dp))
    Paragraph(
        "После 5 пройденных Кругосветок программа начинает анализировать, " +
                "в какие конкретные сектора ты попадаешь хуже всего. Если процент " +
                "попаданий в сектор на 25% ниже твоего среднего — он подсвечивается " +
                "жёлтым, а если на 50% ниже — красным. Теперь ты знаешь, какой сектор " +
                "нужно подтянуть."
    )
}

@Composable
private fun LevelAndRecordsContent() {
    Text(
        "16 уровней",
        color = GoldAccent,
        fontSize = 14.sp,
        fontWeight = FontWeight.Bold
    )
    Spacer(Modifier.height(6.dp))
    Paragraph(
        "Уровень идёт от Новичка (1) до Легенды (16). Каждый уровень — это " +
                "примерная планка по среднему набору очков и точности. " +
                "Уровни ориентируются на спортивные разряды: I разряд, КМС, МС и так далее — " +
                "но это ориентир для тренировки, а не официальное присвоение."
    )

    Spacer(Modifier.height(14.dp))

    Text(
        "Как считается уровень",
        color = GoldAccent,
        fontSize = 14.sp,
        fontWeight = FontWeight.Bold
    )
    Spacer(Modifier.height(6.dp))
    Bullet("Комплекс 1 — упражнения. Считается среднее по 4 параметрам.")
    Bullet("Комплекс 2 — игры. Считается среднее по 501 (PPR) и Крикету (MPR).")
    Bullet("Итоговый уровень — это максимум из двух комплексов.")

    Spacer(Modifier.height(10.dp))
    Paragraph(
        "Если комплексы расходятся (например, упражнения дают 7-й уровень, " +
                "а игры — только 5-й), программа подскажет: игры нужно подтянуть. " +
                "Часто это не про технику, а про психологию — в игре сложнее, чем в упражнениях."
    )

    Spacer(Modifier.height(14.dp))

    Text(
        "Рекорды",
        color = GoldAccent,
        fontSize = 14.sp,
        fontWeight = FontWeight.Bold
    )
    Spacer(Modifier.height(6.dp))
    Paragraph(
        "В каждом упражнении и в каждой игре у тебя есть личный рекорд. " +
                "Если побил — покажем салют. Если был близок (не хватило до 10%) — " +
                "скажем «почти рекорд». Рекорды не влияют на уровень — это отдельная " +
                "радость и мотивация."
    )
}

@Composable
private fun NormativesContent() {
    Text(
        "Что это такое",
        color = GoldAccent,
        fontSize = 14.sp,
        fontWeight = FontWeight.Bold
    )
    Spacer(Modifier.height(6.dp))
    Paragraph(
        "Раз в 10 тренировок программа предлагает сдать нормативы. " +
                "Это пять упражнений, которые проверяют твой реальный уровень " +
                "по всем параметрам. Если рост есть — уровень обновится."
    )

    Spacer(Modifier.height(14.dp))

    Text(
        "Из чего состоят нормативы",
        color = GoldAccent,
        fontSize = 14.sp,
        fontWeight = FontWeight.Bold
    )
    Spacer(Modifier.height(6.dp))
    Bullet("501 против бота — до 3 побед.")
    Bullet("Крикет против бота — до 3 побед.")
    Bullet("Сектор 20 — 30 дротиков.")
    Bullet("Большой раунд — 21 сектор.")
    Bullet("Набор очков — 10 подходов.")

    Spacer(Modifier.height(14.dp))

    Text(
        "Лестница ботов",
        color = GoldAccent,
        fontSize = 14.sp,
        fontWeight = FontWeight.Bold
    )
    Spacer(Modifier.height(6.dp))
    Paragraph(
        "В партиях 501 и Крикета бот меняется по лестнице: чуть слабее, " +
                "равный, чуть сильнее, снова слабее, равный. Это проверяет не только " +
                "технику, но и умение держать уровень против разной силы соперника. " +
                "Всего можно сыграть максимум 5 легов — партия идёт до 3 побед."
    )

    Spacer(Modifier.height(14.dp))

    Text(
        "Что происходит после",
        color = GoldAccent,
        fontSize = 14.sp,
        fontWeight = FontWeight.Bold
    )
    Spacer(Modifier.height(6.dp))
    Bullet("Если хотя бы один комплекс подрос — уровень обновляется.")
    Bullet("Если уровень не изменился — он подтверждён.")
    Bullet("При росте статистика этого периода обнуляется, начинается новый.")
    Bullet("Рекорды и закрытые чекауты сохраняются всегда.")

    Spacer(Modifier.height(14.dp))

    Text(
        "Можно не спешить",
        color = GoldAccent,
        fontSize = 14.sp,
        fontWeight = FontWeight.Bold
    )
    Spacer(Modifier.height(6.dp))
    Paragraph(
        "Сдавать нормативы не обязательно сразу после напоминания. " +
                "Можно продолжать тренироваться — плашка «Пора сдать нормативы» " +
                "будет висеть и напомнит, когда будешь готов. " +
                "Если прервёшь сдачу — прогресс сохранится на 24 часа, " +
                "и можно будет продолжить с того же места."
    )
}

@Composable
private fun BotsContent() {
    Text(
        "16 уровней сложности",
        color = GoldAccent,
        fontSize = 14.sp,
        fontWeight = FontWeight.Bold
    )
    Spacer(Modifier.height(6.dp))
    Paragraph(
        "Каждый бот — от Новичка до Легенды — со своим характером, " +
                "силой и стилем. В списке видно примерный набор и точность удвоений."
    )

    Spacer(Modifier.height(14.dp))

    Text(
        "Бот играет как живой",
        color = GoldAccent,
        fontSize = 14.sp,
        fontWeight = FontWeight.Bold
    )
    Spacer(Modifier.height(6.dp))
    Bullet("Форма дня — сегодня может лететь, завтра не идёт.")
    Bullet("Серии — внутри партии может поймать волну или посыпаться.")
    Bullet("Усталость — играй долго, точность падает.")
    Bullet("Нервничает в концовке — когда у тебя остаток маленький, мажет в даблы.")
    Bullet("Думает перед броском — если дротиков не хватит, не тратит зря, оставляет удобный остаток.")
    Bullet("Любимые удвоения — у каждого уровня свой вкус.")
    Bullet("Честно разыгрывает Bull перед партией.")
}

@Composable
private fun TrainingContent() {
    Text(
        "Два режима тренировки",
        color = GoldAccent,
        fontSize = 14.sp,
        fontWeight = FontWeight.Bold
    )
    Spacer(Modifier.height(6.dp))
    Bullet("По времени — программа на день, под твой уровень и слабое место.")
    Bullet("Свободная — без времени, рулетка случайно выбирает упражнение. " +
            "Заканчиваешь когда хочешь, без обязательств.")

    Spacer(Modifier.height(14.dp))

    Text(
        "Структура тренировки",
        color = GoldAccent,
        fontSize = 14.sp,
        fontWeight = FontWeight.Bold
    )
    Spacer(Modifier.height(6.dp))
    Paragraph(
        "Каждая тренировка строится по порядку: разминка → психология → основная часть → " +
                "силовые. Любой из блоков можно отключить в настройках."
    )

    Spacer(Modifier.height(14.dp))

    Text(
        "Мягкий старт",
        color = GoldAccent,
        fontSize = 14.sp,
        fontWeight = FontWeight.Bold
    )
    Spacer(Modifier.height(6.dp))
    Paragraph(
        "Первые тренировки — вводные. Тяжёлые упражнения (удвоения, утроения) " +
                "появляются только с 4-й тренировки. Ты плавно вкатываешься, а не " +
                "сразу бьёшь по самому сложному."
    )

    Spacer(Modifier.height(14.dp))

    Text(
        "Отдых между упражнениями",
        color = GoldAccent,
        fontSize = 14.sp,
        fontWeight = FontWeight.Bold
    )
    Spacer(Modifier.height(6.dp))
    Paragraph(
        "После каждого упражнения запускается таймер отдыха: 3 минуты после лёгкого, " +
                "4 — после среднего, 5 — после тяжёлого. Можно пропустить, если не нужен."
    )

    Spacer(Modifier.height(14.dp))

    Text(
        "Авто-OK в играх",
        color = GoldAccent,
        fontSize = 14.sp,
        fontWeight = FontWeight.Bold
    )
    Spacer(Modifier.height(6.dp))
    Paragraph(
        "В тренировочных партиях 501 и Крикет авто-OK всегда включён на 3 секунды. " +
                "Не нужно жать ОК после каждого подхода — партия идёт бодрее."
    )

    Spacer(Modifier.height(14.dp))

    Text(
        "Закрытые чекауты",
        color = GoldAccent,
        fontSize = 14.sp,
        fontWeight = FontWeight.Bold
    )
    Spacer(Modifier.height(6.dp))
    Paragraph(
        "В разделе «Мой прогресс» есть кнопка 🏆 — там отмечено, какие чекауты " +
                "(остатки 2–170) ты закрывал в тренировочных партиях 501. " +
                "Чем больше раз закрыт остаток — тем ярче клетка на сетке. " +
                "Это твой личный дневник побед: видишь, что уже делал это раньше — " +
                "значит, сможешь и сейчас. Уверенность работает на результат."
    )

    Spacer(Modifier.height(14.dp))

    Text(
        "Умные упражнения на слабые сектора",
        color = GoldAccent,
        fontSize = 14.sp,
        fontWeight = FontWeight.Bold
    )
    Spacer(Modifier.height(6.dp))
    Paragraph(
        "Если программа видит, что у тебя проваливается конкретный сектор " +
                "(например, D7 или T19), в ближайшую тренировку она автоматически " +
                "добавит точечное упражнение для его отработки."
    )
}

@Composable
private fun FeaturesContent() {
    Text(
        "Свободная тренировка — когда нет настроения",
        color = GoldAccent,
        fontSize = 14.sp,
        fontWeight = FontWeight.Bold
    )
    Spacer(Modifier.height(6.dp))
    Paragraph(
        "Бывает, что зашёл потренироваться, но состояние не то. " +
                "Не хочется отрабатывать запланированное, и появляется чувство вины. " +
                "Свободная тренировка — специально для таких моментов. " +
                "Рулетка выбирает упражнение случайно, нет времени и обязательств. " +
                "Можно выйти в любой момент, и это нормально."
    )

    Spacer(Modifier.height(14.dp))

    Text(
        "Быстрые кнопки учатся под тебя",
        color = GoldAccent,
        fontSize = 14.sp,
        fontWeight = FontWeight.Bold
    )
    Spacer(Modifier.height(6.dp))
    Paragraph(
        "На экране 501 есть кнопки с частыми суммами. Приложение запоминает, " +
                "какие суммы ты вводишь чаще всего. После каждых 3 партий кнопки " +
                "перестраиваются под тебя. Чем больше играешь, тем удобнее становится."
    )

    Spacer(Modifier.height(14.dp))

    Text(
        "Возобновление тренировки",
        color = GoldAccent,
        fontSize = 14.sp,
        fontWeight = FontWeight.Bold
    )
    Spacer(Modifier.height(6.dp))
    Paragraph(
        "Если во время тренировки пришлось отвлечься — вернуться можно в течение " +
                "3 часов. Программа предложит продолжить с того же места. " +
                "Через 3 часа — тренировка закроется, а результаты, что успел сделать, " +
                "уйдут в статистику."
    )

    Spacer(Modifier.height(14.dp))

    Text(
        "Разыгрыш Bull",
        color = GoldAccent,
        fontSize = 14.sp,
        fontWeight = FontWeight.Bold
    )
    Spacer(Modifier.height(6.dp))
    Paragraph(
        "Перед партией 501 или Крикета — честный розыгрыш Bull. " +
                "Ты бросаешь, бот бросает, кто ближе — тот и начинает. При равенстве — переброс. " +
                "Работает и в свободной игре, и в тренировке."
    )

    Spacer(Modifier.height(14.dp))

    Text(
        "Отложить тренировку",
        color = GoldAccent,
        fontSize = 14.sp,
        fontWeight = FontWeight.Bold
    )
    Spacer(Modifier.height(6.dp))
    Paragraph(
        "Если начал тренировку, но срочно нужно уйти — можно нажать «Отложить». " +
                "Программа запомнит, где ты остановился, и предложит продолжить при " +
                "следующем входе. Действует 3 часа."
    )
}

@Composable
private fun AboutContent() {
    Text(
        "Darts Trainer",
        color = GoldAccent,
        fontSize = 16.sp,
        fontWeight = FontWeight.Bold
    )
    Spacer(Modifier.height(4.dp))
    Paragraph("Персональный тренер по дартсу. Версия 1.0.")

    Spacer(Modifier.height(14.dp))

    Text(
        "Автор",
        color = GoldAccent,
        fontSize = 14.sp,
        fontWeight = FontWeight.Bold
    )
    Spacer(Modifier.height(4.dp))
    Paragraph("Виктор Лодкин")

    Spacer(Modifier.height(14.dp))

    Text(
        "Хранение данных",
        color = GoldAccent,
        fontSize = 14.sp,
        fontWeight = FontWeight.Bold
    )
    Spacer(Modifier.height(6.dp))
    Paragraph(
        "Всё хранится локально на твоём телефоне. Никаких серверов, никаких " +
                "аккаунтов, никакой передачи данных. Удалил приложение — данные удалились."
    )

    Spacer(Modifier.height(14.dp))

    Text(
        "Второе приложение",
        color = GoldAccent,
        fontSize = 14.sp,
        fontWeight = FontWeight.Bold
    )
    Spacer(Modifier.height(6.dp))
    Paragraph(
        "У автора есть ещё одно приложение — Darts Checkout. " +
                "Это справочник чекаутов (все возможные закрытия от 2 до 170). " +
                "Darts Trainer — самостоятельное приложение, но использует ту же логику чекаутов " +
                "внутри себя."
    )

    Spacer(Modifier.height(14.dp))

    Text(
        "Спасибо!",
        color = GoldAccent,
        fontSize = 14.sp,
        fontWeight = FontWeight.Bold
    )
    Spacer(Modifier.height(6.dp))
    Paragraph(
        "Спасибо, что пользуешься приложением. Если найдёшь баг или захочешь " +
                "что-то добавить — напиши автору. Каждый отзыв помогает сделать " +
                "Darts Trainer лучше. Приятной игры! 🎯"
    )
}

// ============================================================
// Хелперы
// ============================================================

@Composable
private fun Paragraph(text: String) {
    Text(
        text,
        color = Color.White.copy(alpha = 0.85f),
        fontSize = 13.sp,
        lineHeight = 19.sp
    )
}

@Composable
private fun Bullet(text: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 3.dp),
        verticalAlignment = Alignment.Top
    ) {
        Text("•", color = Accent, fontSize = 14.sp)
        Spacer(Modifier.width(8.dp))
        Text(
            text,
            color = Color.White.copy(alpha = 0.85f),
            fontSize = 13.sp,
            lineHeight = 19.sp,
            modifier = Modifier.weight(1f)
        )
    }
}
