package com.lodkin.dartstrainer.ui.training

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
 *
 * onFinish передаёт: (blockType, score, darts, detailsJson).
 * detailsJson — строка с подробностями (для кругосветок: "1:2,2:1,3:4").
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

/**
 * Рекомендуемые игры по слабому месту.
 */
internal fun recommendedGameIds(weakness: WeaknessType?): Set<String> = when (weakness) {
    WeaknessType.DOUBLES -> setOf("aroundclock_doubles", "game501")
    WeaknessType.SCORE -> setOf("sector20", "scoreset")
    WeaknessType.ACCURACY -> setOf("aroundclock_sectors", "biground")
    WeaknessType.TREBLES -> setOf("aroundclock_trebles", "biground", "cricket")
    null -> emptySet()
}

/**
 * Заглушка для упражнения в разработке.
 */
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
// СПИСКИ УПРАЖНЕНИЙ ПО УМОЛЧАНИЮ
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

internal fun defaultMainExercises(): List<PlannedExercise> = listOf(
    PlannedExercise("aroundclock_doubles", "Кругосветка: Удвоения", 14, "Тренирует точность на удвоениях, чтобы закрывать леги с первого дротика."),
    PlannedExercise("aroundclock_sectors", "Кругосветка: Сектора", 12, "Улучшает общую точность и контроль над сектором."),
    PlannedExercise("aroundclock_trebles", "Кругосветка: Утроения", 12, "Помогает набирать больше очков за подход (особенно в Крикете)."),
    PlannedExercise("sector20", "Сектор 20", 8, "База для набора очков в 501."),
    PlannedExercise("scoreset", "Набор очков", 6, "Учит стабильно набирать максимум за 3 дротика."),
    PlannedExercise("biground", "Большой раунд", 12, "Тренирует переход по всей мишени."),
    PlannedExercise("game501", "501", 18, "Главная игра — учит считать и закрывать."),
    PlannedExercise("cricket", "Крикет", 12, "Тренирует утроения и стратегию.")
)

internal fun defaultPsychologyExercises(): List<PlannedExercise> = listOf(
    PlannedExercise("psy_breath", "Дыхание 4-4-7", 3, "Снижает пульс и убирает дрожь в руках перед важным броском."),
    PlannedExercise("psy_tunnel", "Туннельное зрение", 3, "Учит мозг не отвлекаться на фон и концентрироваться только на цели."),
    PlannedExercise("psy_ritual", "Ритуал трёх шагов", 3, "Стабильность — залог победы. Одинаковые действия настраивают на бросок."),
    PlannedExercise("psy_visual", "Визуализация полёта", 3, "Прогревает нейронные связи, и рука бросает точнее."),
    PlannedExercise("psy_single", "Один дротик", 3, "Убирает расслабленность. Каждый бросок — решающий."),
    PlannedExercise("psy_trigger_word", "Триггер-слово", 3, "Создаёт условный рефлекс для тела начать правильное движение."),
    PlannedExercise("psy_mirror", "Зеркало", 3, "Контроль техники и осознанность движений."),
    PlannedExercise("psy_ideomotor", "Идеомоторная тренировка", 3, "Улучшение техники без физических бросков.")
)

internal fun defaultStrengthExercises(): List<PlannedExercise> = listOf(
    PlannedExercise("str_1", "Силовая: Плечи", 2, "Укрепляет плечевой пояс."),
    PlannedExercise("str_2", "Силовая: Кисти", 2, "Разрабатывает кисть для контроля дротика."),
    PlannedExercise("str_3", "Силовая: Спина", 2, "Стабильная стойка."),
    PlannedExercise("str_4", "Силовая: Растяжка", 1, "Снимает напряжение после тренировки.")
)

internal fun warmupBlockItems(): List<BlockExerciseItem> = listOf(
    BlockExerciseItem("warm_muscles", "Разминка: мышцы"),
    BlockExerciseItem("warm_trajectory", "Разминка: траектория"),
    BlockExerciseItem("warm_accuracy", "Разминка: кучность"),
    BlockExerciseItem("warm_aim", "Прицеливание")
)

internal fun defaultMainBlockItems(recommendedIds: Set<String>): List<BlockExerciseItem> {
    return defaultMainExercises().map { BlockExerciseItem(it.id, it.title) }
}
