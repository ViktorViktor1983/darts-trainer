package com.lodkin.dartstrainer.ui.training

import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
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
import com.lodkin.dartstrainer.data.aroundclock.AroundClockOrder
import com.lodkin.dartstrainer.data.aroundclock.AroundClockRepository
import com.lodkin.dartstrainer.data.aroundclock.AroundClockTarget
import com.lodkin.dartstrainer.data.biground.BigRoundCategory
import com.lodkin.dartstrainer.data.biground.BigRoundRepository
import com.lodkin.dartstrainer.data.scoreset.ScoreSetRepository
import com.lodkin.dartstrainer.data.sector.SectorRepository
import com.lodkin.dartstrainer.data.training.BlockType
import com.lodkin.dartstrainer.data.training.InventoryStorage
import com.lodkin.dartstrainer.data.training.TrainingRepository
import com.lodkin.dartstrainer.data.training.WeaknessType
import com.lodkin.dartstrainer.theme.Accent
import com.lodkin.dartstrainer.theme.DarkBg
import com.lodkin.dartstrainer.theme.GoldAccent
import com.lodkin.dartstrainer.theme.TileBgDark
import com.lodkin.dartstrainer.ui.aroundclock.AroundClockGameScreen
import com.lodkin.dartstrainer.ui.biground.BigRoundGameScreen
import com.lodkin.dartstrainer.ui.scoreset.ScoreSetGameScreen
import com.lodkin.dartstrainer.ui.sector.SectorGameScreen

/**
 * Запуск упражнения основной части. Используется и в обычной,
 * и в свободной тренировке.
 */
@Composable
internal fun MainExerciseRunner(
    exerciseId: String,
    playerLevel: Int,
    aroundClockRepository: AroundClockRepository,
    sectorRepository: SectorRepository,
    scoreSetRepository: ScoreSetRepository,
    bigRoundRepository: BigRoundRepository,
    trainingRepository: TrainingRepository?,
    onFinish: (BlockType, Int, Int, String) -> Unit,
    onBack: () -> Unit
) {
    val isTraining = trainingRepository != null

    when (exerciseId) {
        "aroundclock_doubles" -> AroundClockGameScreen(
            target = AroundClockTarget.DOUBLE,
            order = AroundClockOrder.ORDERED,
            hitsRequired = 1,
            repository = aroundClockRepository,
            sectorStart = 1,
            trainingRepository = trainingRepository,
            trainingRecordKey = if (isTraining) "aroundclock_doubles" else null,
            onFinish = { totalDarts, _, sr ->
                val details = sr.entries.joinToString(",") { "${it.key}:${it.value}" }
                onFinish(BlockType.DOUBLES, totalDarts, totalDarts, details)
            },
            onBack = onBack
        )
        "aroundclock_sectors" -> AroundClockGameScreen(
            target = AroundClockTarget.SINGLE,
            order = AroundClockOrder.ORDERED,
            hitsRequired = 1,
            repository = aroundClockRepository,
            sectorStart = 1,
            trainingRepository = trainingRepository,
            trainingRecordKey = if (isTraining) "aroundclock_sectors" else null,
            onFinish = { totalDarts, _, sr ->
                val details = sr.entries.joinToString(",") { "${it.key}:${it.value}" }
                onFinish(BlockType.SECTOR, totalDarts, totalDarts, details)
            },
            onBack = onBack
        )
        "aroundclock_trebles" -> {
            val sectorStart = if (playerLevel >= 9) 1 else 10
            AroundClockGameScreen(
                target = AroundClockTarget.TRIPLE,
                order = AroundClockOrder.ORDERED,
                hitsRequired = 1,
                repository = aroundClockRepository,
                sectorStart = sectorStart,
                trainingRepository = trainingRepository,
                trainingRecordKey = if (isTraining) "aroundclock_trebles" else null,
                onFinish = { totalDarts, _, sr ->
                    val details = sr.entries.joinToString(",") { "${it.key}:${it.value}" }
                    onFinish(BlockType.TREBLES, totalDarts, totalDarts, details)
                },
                onBack = onBack
            )
        }
        "sector20" -> SectorGameScreen(
            sector = 20,
            repository = sectorRepository,
            isTrainingMode = isTraining,
            trainingRepository = trainingRepository,
            trainingRecordKey = if (isTraining) "sector20" else null,
            onFinish = { score -> onFinish(BlockType.SCORE, score, 30, "") },
            onBack = onBack
        )
        "scoreset" -> ScoreSetGameScreen(
            repository = scoreSetRepository,
            isTrainingMode = isTraining,
            trainingRepository = trainingRepository,
            trainingRecordKey = if (isTraining) "scoreset" else null,
            onFinish = { totalScore, approaches -> onFinish(BlockType.SCORE, totalScore, approaches.size * 3, "") },
            onBack = onBack
        )
        "biground" -> BigRoundGameScreen(
            category = BigRoundCategory.MALE,
            repository = bigRoundRepository,
            isTrainingMode = isTraining,
            trainingRepository = trainingRepository,
            trainingRecordKey = if (isTraining) "biground" else null,
            onFinish = { score -> onFinish(BlockType.SECTOR, score, 63, "") },
            onBack = onBack
        )
        else -> TrainingExercisePlaceholder(
            title = "Упражнение",
            description = "Это упражнение пока в разработке.",
            onDone = { onFinish(BlockType.SCORE, 0, 0, "") },
            onBack = onBack
        )
    }
}

internal fun recommendedGameIds(weakness: WeaknessType?): Set<String> = when (weakness) {
    WeaknessType.DOUBLES -> setOf("aroundclock_doubles", "game501")
    WeaknessType.SCORE -> setOf("sector20", "scoreset")
    WeaknessType.ACCURACY -> setOf("aroundclock_sectors", "biground")
    WeaknessType.TREBLES -> setOf("aroundclock_trebles", "biground", "cricket")
    null -> emptySet()
}

@Composable
internal fun TrainingExercisePlaceholder(
    title: String,
    description: String,
    onDone: () -> Unit,
    onBack: () -> Unit
) {
    Box(modifier = Modifier.fillMaxSize().background(DarkBg).padding(16.dp)) {
        Column(modifier = Modifier.fillMaxSize()) {
            Box(
                modifier = Modifier.clip(RoundedCornerShape(12.dp)).background(TileBgDark)
                    .clickable { onBack() }.padding(horizontal = 14.dp, vertical = 10.dp)
            ) { Text("← Назад", color = Accent, fontSize = 14.sp, fontWeight = FontWeight.Medium) }

            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(24.dp)
                ) {
                    Text(title, color = GoldAccent, fontSize = 24.sp, fontWeight = FontWeight.Bold)
                    if (description.isNotBlank()) {
                        Spacer(Modifier.height(12.dp))
                        Text(description, color = Color.White.copy(alpha = 0.85f), fontSize = 14.sp)
                    }
                    Spacer(Modifier.height(16.dp))
                    Text(
                        "Упражнение в разработке",
                        color = Color.White.copy(alpha = 0.5f),
                        fontSize = 12.sp
                    )
                    Spacer(Modifier.height(24.dp))
                    Box(
                        modifier = Modifier.clip(RoundedCornerShape(14.dp))
                            .background(Accent).clickable { onDone() }
                            .padding(horizontal = 32.dp, vertical = 14.dp)
                    ) { Text("Готово", color = Color(0xFF121212), fontSize = 15.sp, fontWeight = FontWeight.Bold) }
                }
            }
        }
    }
}

// ─────────────────────────────────────────────
// МОДЕЛИ И СПИСКИ УПРАЖНЕНИЙ
// ─────────────────────────────────────────────

data class PlannedExercise(
    val id: String,
    val title: String,
    val minutes: Int,
    val whyImportant: String = ""
)

data class BlockExerciseItem(
    val id: String,
    val title: String
)

// ПЕРЕИМЕНОВАНО В getDefaultMainExercises
internal fun getDefaultMainExercises(): List<PlannedExercise> = listOf(
    PlannedExercise("aroundclock_doubles", "Кругосветка: Удвоения", 14, "Тренирует точность на удвоениях, чтобы закрывать леги с первого дротика."),
    PlannedExercise("aroundclock_sectors", "Кругосветка: Сектора", 12, "Улучшает общую точность и контроль над сектором."),
    PlannedExercise("aroundclock_trebles", "Кругосветка: Утроения", 12, "Помогает набирать больше очков за подход (особенно в Крикете)."),
    PlannedExercise("sector20", "Сектор 20", 8, "База для набора очков в 501."),
    PlannedExercise("scoreset", "Набор очков", 6, "Учит стабильно набирать максимум за 3 дротика."),
    PlannedExercise("biground", "Большой раунд", 12, "Тренирует переход по всей мишени."),
    PlannedExercise("game501", "501", 18, "Главная игра — учит считать и закрывать."),
    PlannedExercise("cricket", "Крикет", 12, "Тренирует утроения и стратегию.")
)

// ─────────────────────────────────────────────
// ПСИХОЛОГИЯ (выбор по 3 в цикле)
// ─────────────────────────────────────────────

private val ALL_PSYCHOLOGY_EXERCISES = listOf(
    PlannedExercise("psy_breath", "Дыхание 4-4-7", 3, "Снижает пульс и убирает дрожь в руках перед важным броском."),
    PlannedExercise("psy_tunnel", "Туннельное зрение", 3, "Учит мозг не отвлекаться на фон и концентрироваться только на цели."),
    PlannedExercise("psy_ritual", "Ритуал трёх шагов", 3, "Стабильность — залог победы. Одинаковые действия настраивают на бросок."),
    PlannedExercise("psy_visual", "Визуализация полёта", 3, "Прогревает нейронные связи, и рука бросает точнее."),
    PlannedExercise("psy_single", "Один дротик", 3, "Убирает расслабленность. Каждый бросок — решающий."),
    PlannedExercise("psy_trigger_word", "Триггер-слово", 3, "Создаёт условный рефлекс для тела начать правильное движение."),
    PlannedExercise("psy_mirror", "Зеркало", 3, "Контроль техники и осознанность движений."),
    PlannedExercise("psy_ideomotor", "Идеомоторная тренировка", 3, "Улучшение техники без физических бросков.")
)

internal fun getNextPsychologyExercises(context: Context): List<PlannedExercise> {
    val size = ALL_PSYCHOLOGY_EXERCISES.size
    val startIndex = InventoryStorage.getPsychologyIndex(context) % size
    
    val result = mutableListOf<PlannedExercise>()
    for (i in 0 until 3) {
        val index = (startIndex + i) % size
        result.add(ALL_PSYCHOLOGY_EXERCISES[index])
    }
    
    InventoryStorage.incrementPsychologyIndex(context)
    return result
}

// ─────────────────────────────────────────────
// СИЛОВЫЕ (выбор по инвентарю и по 3 в цикле)
// ─────────────────────────────────────────────

private data class StrengthTemplate(
    val id: String,
    val title: String,
    val minutes: Int,
    val whyImportant: String,
    val requiresDumbbells: Boolean = false,
    val requiresBand: Boolean = false
)

private val ALL_STRENGTH_EXERCISES = listOf(
    StrengthTemplate("str_plank", "Планка", 2, "Укрепляет мышцы кора. Твое тело не будет гулять при броске."),
    StrengthTemplate("str_wall_pushups", "Отжимания от стены", 2, "Разогревает плечевой пояс. Рука меньше устает."),
    StrengthTemplate("str_triceps_stretch", "Растяжка трицепса", 1, "Убирает зажатость в локте. Бросок становится свободным."),
    StrengthTemplate("str_boat", "Лодочка", 2, "Укрепляет мышцы спины. Помогает держать осанку."),
    StrengthTemplate("str_side_plank", "Боковая планка", 2, "Тренирует косые мышцы. Нужно для стабильности корпуса."),
    StrengthTemplate("str_chest_stretch", "Растяжка грудных", 1, "Раскрывает плечи. Правильная стойка для броска."),
    StrengthTemplate("str_dumbbell_imitation", "Имитация броска с гантелью", 3, "Медленно повторяй движение броска с гантелью. Укрепляет нужные мышцы.", requiresDumbbells = true),
    StrengthTemplate("str_dumbbell_wrist", "Вращения кистью с гантелью", 2, "Разрабатывает предплечье и кисть. Улучшает выпуск дротика.", requiresDumbbells = true),
    StrengthTemplate("str_band_pull", "Разведение рук с резинкой", 2, "Укрепляет плечи и улучшает подвижность сустава.", requiresBand = true)
)

internal fun getNextStrengthExercises(context: Context): List<PlannedExercise> {
    val hasDumbbells = InventoryStorage.hasDumbbells(context)
    val hasBand = InventoryStorage.hasResistanceBand(context)

    val available = ALL_STRENGTH_EXERCISES.filter { ex ->
        (!ex.requiresDumbbells || hasDumbbells) && (!ex.requiresBand || hasBand)
    }

    if (available.isEmpty()) return emptyList()

    val size = available.size
    val startIndex = InventoryStorage.getStrengthIndex(context) % size
    
    val result = mutableListOf<PlannedExercise>()
    for (i in 0 until 3) {
        val index = (startIndex + i) % size
        val template = available[index]
        result.add(
            PlannedExercise(
                id = template.id,
                title = template.title,
                minutes = template.minutes,
                whyImportant = template.whyImportant
            )
        )
    }
    
    InventoryStorage.incrementStrengthIndex(context)
    return result
}

// ─────────────────────────────────────────────
// РАЗМИНКА (НОВАЯ ЛОГИКА)
// ─────────────────────────────────────────────

// Группа 1: Шея
private val WARMUP_NECK = listOf(
    PlannedExercise("warm_neck_rot", "Вращения шеей", 1, "Медленные наклоны головы вперед-назад и влево-вправо. Снимает напряжение и улучшает прицеливание."),
    PlannedExercise("warm_neck_tilt", "Наклоны головы к плечам", 1, "Плавные наклоны к левому и правому плечу. По 10 секунд на каждую сторону."),
    PlannedExercise("warm_neck_turn", "Повороты головы", 1, "Медленно повернуть голову влево, задержаться, затем вправо. По 5 раз в каждую сторону.")
)

// Группа 2: Плечевой пояс
private val WARMUP_SHOULDERS = listOf(
    PlannedExercise("warm_shoulder_rot", "Вращения плечами", 1, "Руки свободно опущены. Круговые движения плечами вперед и назад. По 10 раз."),
    PlannedExercise("warm_mill", "Мельница", 1, "Вращения прямыми руками вперед и назад. По 10 раз."),
    PlannedExercise("warm_chest_stretch", "Растяжка грудных мышц", 1, "Руки в замок за спиной, отводим назад и раскрываем грудь. 20 секунд.")
)

// Группа 3: Кисти и предплечья
private val WARMUP_WRISTS = listOf(
    PlannedExercise("warm_wrist_rot", "Вращения кистями", 1, "Руки в стороны, вращаем кулаками в запястьях. По 10 раз в каждую сторону."),
    PlannedExercise("warm_grip", "Сжимание эспандера (или кулака)", 1, "Максимально сжать и разжать. 15 раз."),
    PlannedExercise("warm_triceps_stretch", "Растяжка трицепса", 1, "Рука за головой, другая давит на локоть. По 20 секунд на каждую руку.")
)

// Группа 4: Тазобедренный пояс и ноги
private val WARMUP_LEGS = listOf(
    PlannedExercise("warm_hips", "Вращения тазом", 1, "Руки на поясе, круговые движения бедрами. По 10 раз в каждую сторону."),
    PlannedExercise("warm_body_bends", "Наклоны корпуса", 1, "Вперед (к полу) и в стороны. По 10 раз."),
    PlannedExercise("warm_knees", "Вращения в коленях и голеностопе", 1, "Ноги на ширине плеч, вращаем коленями, затем по очереди стопами. По 10 раз.")
)

// Группа 5: С дротиками
private val WARMUP_DARTS = listOf(
    PlannedExercise("warm_darts_free", "Бросание без цели", 2, "10 подходов по 3 дротика. Просто в мишень, следя за свободой движения."),
    PlannedExercise("warm_darts_imitation", "Имитация броска", 2, "30 раз. Полноценное движение рукой без дротика, представляя его."),
    PlannedExercise("warm_darts_bull", "Бросок в Bull", 2, "10 подходов по 3 дротика. 30 дротиков в центр.")
)

internal fun getWarmupBlockItems(): List<PlannedExercise> {
    val result = mutableListOf<PlannedExercise>()
    
    // Берем случайное по одному из каждой группы
    result.add(WARMUP_NECK.random())
    result.add(WARMUP_SHOULDERS.random())
    result.add(WARMUP_WRISTS.random())
    result.add(WARMUP_LEGS.random())
    result.add(WARMUP_DARTS.random())
    
    return result
}

internal fun getDefaultMainBlockItems(recommendedIds: Set<String>): List<BlockExerciseItem> {
    return getDefaultMainExercises().map { BlockExerciseItem(it.id, it.title) }
}
