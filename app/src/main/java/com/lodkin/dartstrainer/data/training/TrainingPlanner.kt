package com.lodkin.dartstrainer.data.training

import com.lodkin.dartstrainer.ui.training.getWarmupBlockItems
import kotlin.math.roundToInt

/**
 * Генератор программы тренировки на день.
 */
object TrainingPlanner {

    // ─────────────────────────────────────────────
    // МОДЕЛИ ПЛАНА
    // ─────────────────────────────────────────────

    enum class ExerciseKind {
        WARMUP,
        DOUBLES_ROUNDS,
        DOUBLES_TARGETED,
        DOUBLES_POSTPONED,
        SECTOR_20,
        SECTOR_19,
        SCORE_SET,
        AROUND_CLOCK_SECTORS,
        AROUND_CLOCK_DOUBLES,
        AROUND_CLOCK_TREBLES,
        TARGETED_PRACTICE,
        GAME_501,
        GAME_CRICKET,
        GAME_ANY,
        STRENGTH
    }

    data class TrainingBlock(
        val kind: ExerciseKind,
        val title: String,
        val description: String,
        val minutes: Int,
        val isMainFocus: Boolean = false
    )

    data class TrainingPlan(
        val blocks: List<TrainingBlock>,
        val totalMinutes: Int,
        val focus: WeaknessType?,
        val isControl: Boolean,
        val postponedDoubles: List<Int>
    )

    data class PlannedDynamicExercise(
        val id: String,
        val minutes: Int,
        val focusHint: String? = null
    )

    private const val INTRO_TRAININGS_COUNT = 5
    private const val INTRO_NO_HARD_COUNT = 3
    private const val HARD_DIFFICULTY = 7

    /**
     * Шкала тяжести: чем больше число — тем тяжелее упражнение.
     */
    private fun difficultyOf(id: String): Int = when (id) {
        "cricket" -> 1
        "game501" -> 2
        "aroundclock_sectors" -> 3
        "scoreset" -> 4
        "targeted_practice" -> 5
        "problem_sectors" -> 5 // Добавили
        "sector20" -> 6
        "biground" -> 7
        "aroundclock_doubles" -> 8
        "aroundclock_trebles" -> 9
        else -> 5
    }

    fun restSecondsAfter(id: String): Int {
        val d = difficultyOf(id)
        return when {
            d <= 3 -> 3 * 60
            d <= 6 -> 4 * 60
            else -> 5 * 60
        }
    }

    // ─────────────────────────────────────────────
    // ГЛАВНАЯ ФУНКЦИЯ (тренировка по времени)
    // ─────────────────────────────────────────────

    fun buildPlan(
        weaknesses: WeaknessDetector.WeaknessResult,
        minutes: Int,
        isControl: Boolean,
        postponedDoubles: List<Int> = emptyList()
    ): TrainingPlan {

        if (isControl) {
            return buildControlPlan(minutes)
        }

        val blocks = mutableListOf<TrainingBlock>()
        var remaining = minutes

        val warmupExercises = getWarmupBlockItems()
        warmupExercises.forEach { ex ->
            blocks.add(
                TrainingBlock(
                    kind = ExerciseKind.WARMUP,
                    title = ex.title,
                    description = ex.whyImportant,
                    minutes = ex.minutes
                )
            )
            remaining -= ex.minutes
        }

        if (postponedDoubles.isNotEmpty() && remaining > 0) {
            val minPostponed = minOf(remaining, postponedDoubles.size * 5)
            blocks.add(
                TrainingBlock(
                    kind = ExerciseKind.DOUBLES_POSTPONED,
                    title = "Возврат отложенных даблов",
                    description = "Свежая рука — отличный шанс закрыть то, что не получилось в прошлый раз.",
                    minutes = minPostponed,
                    isMainFocus = true
                )
            )
            remaining -= minPostponed
        }

        val main = weaknesses.mainWeakness
        if (main != null && remaining > 0) {
            val timeMain = (remaining * 0.5).toInt().coerceAtLeast(10)
            blocks.add(buildMainBlock(main.type, timeMain))
            remaining -= timeMain
        }

        val second = weaknesses.weaknesses.getOrNull(1)
        if (second != null && remaining >= 15) {
            val timeSecond = (remaining * 0.5).toInt().coerceAtLeast(10)
            blocks.add(buildSecondBlock(second.type, timeSecond))
            remaining -= timeSecond
        }

        if (remaining >= 10) {
            val gameTime = minOf(remaining - 5, 30)
            if (gameTime >= 10) {
                blocks.add(
                    TrainingBlock(
                        kind = ExerciseKind.GAME_ANY,
                        title = "Игровой блок",
                        description = buildGameDescription(main?.type),
                        minutes = gameTime
                    )
                )
                remaining -= gameTime
            }
        }

        blocks.add(
            TrainingBlock(
                kind = ExerciseKind.STRENGTH,
                title = "Силовые упражнения",
                description = "Выносливость мышц — чтобы рука не садилась в конце турнира.",
                minutes = 5
            )
        )

        return TrainingPlan(
            blocks = blocks,
            totalMinutes = blocks.sumOf { it.minutes },
            focus = main?.type,
            isControl = false,
            postponedDoubles = postponedDoubles
        )
    }

    // ─────────────────────────────────────────────
    // ДИНАМИЧЕСКИЙ ВЫБОР УПРАЖНЕНИЙ ОСНОВНОЙ ЧАСТИ
    // ─────────────────────────────────────────────

    fun buildDynamicMainExerciseIds(
        analysis: TrainingStatsAnalyzer.TrainingAnalysis,
        budgetMinutes: Int,
        completedTrainingCount: Int = Int.MAX_VALUE,
        ignoreHardLimit: Boolean = false
    ): List<PlannedDynamicExercise> {
        if (budgetMinutes <= 0) return emptyList()

        val isIntroductory = !ignoreHardLimit && completedTrainingCount < INTRO_TRAININGS_COUNT
        val noHardAllowed = !ignoreHardLimit && completedTrainingCount < INTRO_NO_HARD_COUNT

        // Порядок предпочтения по слабому месту (ДОБАВИЛИ problem_sectors)
        val preferenceOrder: List<String> = when (analysis.weakestType) {
            WeaknessType.DOUBLES -> listOf(
                "aroundclock_doubles", "game501", "biground", "sector20", "scoreset",
                "aroundclock_sectors", "aroundclock_trebles", "cricket", "problem_sectors"
            )
            WeaknessType.SCORE -> listOf(
                "sector20", "game501", "scoreset", "biground", "aroundclock_doubles",
                "aroundclock_sectors", "aroundclock_trebles", "cricket", "problem_sectors"
            )
            WeaknessType.ACCURACY -> listOf(
                "aroundclock_sectors", "cricket", "biground", "sector20", "aroundclock_trebles",
                "scoreset", "aroundclock_doubles", "game501", "problem_sectors"
            )
            WeaknessType.TREBLES -> listOf(
                "aroundclock_trebles", "cricket", "biground", "aroundclock_sectors", "sector20",
                "scoreset", "aroundclock_doubles", "game501", "problem_sectors"
            )
            null -> listOf(
                "aroundclock_doubles", "sector20", "aroundclock_sectors", "scoreset", "biground",
                "aroundclock_trebles", "game501", "cricket", "problem_sectors"
            )
        }

        val orderedIds: List<String> = if (isIntroductory) {
            val games = listOf("game501", "cricket")
            val others = preferenceOrder.filter { it !in games }
                .sortedBy { difficultyOf(it) }
            (games + others).distinct()
        } else {
            preferenceOrder
        }

        val maxAllowedDifficulty: Int = when {
            ignoreHardLimit -> 9
            completedTrainingCount < 2 -> 3
            completedTrainingCount == 2 -> 6
            else -> 9
        }

        fun fallbackMinutes(id: String): Int = when (id) {
            "aroundclock_doubles" -> 14
            "aroundclock_sectors" -> 12
            "aroundclock_trebles" -> 12
            "sector20" -> 8
            "scoreset" -> 6
            "biground" -> 12
            "game501" -> 18
            "cricket" -> 12
            "targeted_practice" -> 15
            "problem_sectors" -> 15 // Добавили
            else -> 10
        }

        fun minutesFor(id: String): Int {
            val tempo = analysis.avgMinutesByExercise[id]
            val value = if (tempo != null && tempo > 0f) tempo.roundToInt() else fallbackMinutes(id)
            return value.coerceAtLeast(3)
        }

        var remaining = budgetMinutes
        val result = mutableListOf<PlannedDynamicExercise>()
        var hardCount = 0

        // 1. Точечная работа
        if (analysis.targetPracticeGoals.isNotEmpty() && !analysis.hasTargetedPracticeThisPeriod) {
            val tpMinutes = (analysis.targetPracticeGoals.size * 2).coerceIn(5, 15)
            if (tpMinutes <= remaining) {
                result.add(PlannedDynamicExercise("targeted_practice", tpMinutes))
                remaining -= tpMinutes
            }
        }

        // 2. Остальные упражнения
        for (id in orderedIds) {
            val diff = difficultyOf(id)
            if (diff > maxAllowedDifficulty) continue

            val isHard = diff >= HARD_DIFFICULTY
            if (isHard && noHardAllowed) continue
            if (isHard && hardCount >= 1 && !ignoreHardLimit) continue

            val m = minutesFor(id)
            if (m <= remaining) {
                val hint = buildFocusHint(id, analysis)
                result.add(PlannedDynamicExercise(id, m, hint))
                remaining -= m
                if (isHard) hardCount++
            }
        }

        return result
    }

    private fun buildFocusHint(id: String, analysis: TrainingStatsAnalyzer.TrainingAnalysis): String? {
        return when (id) {
            "aroundclock_doubles" -> {
                val worst = analysis.worstDoubles.take(3).map { "D${it.sector}" }
                if (worst.isNotEmpty()) "Твой фокус: ${worst.joinToString(", ")}. Обрати на них особое внимание!" else null
            }
            "aroundclock_trebles" -> {
                val worst = analysis.worstTrebles.take(3).map { "T${it.sector}" }
                if (worst.isNotEmpty()) "Твой фокус: ${worst.joinToString(", ")}. Обрати на них особое внимание!" else null
            }
            "aroundclock_sectors" -> {
                val worst = analysis.worstSectors.take(3).map { if (it.sector == 25) "Bull" else "S${it.sector}" }
                if (worst.isNotEmpty()) "Твой фокус: ${worst.joinToString(", ")}. Обрати на них особое внимание!" else null
            }
            else -> null
        }
    }

    // ─────────────────────────────────────────────
    // БЛОКИ
    // ─────────────────────────────────────────────

    private fun buildMainBlock(type: WeaknessType, minutes: Int): TrainingBlock {
        return when (type) {
            WeaknessType.DOUBLES -> TrainingBlock(
                kind = ExerciseKind.DOUBLES_ROUNDS,
                title = "Раунды на удвоения",
                description = "D1, D2, ..., D20, Bull. Один раз попал — переход к следующей цели.",
                minutes = minutes,
                isMainFocus = true
            )
            WeaknessType.SCORE -> TrainingBlock(
                kind = ExerciseKind.SCORE_SET,
                title = "Набор очков",
                description = "10 подходов по 3 дротика. Цель — максимум очков.",
                minutes = minutes,
                isMainFocus = true
            )
            WeaknessType.ACCURACY -> TrainingBlock(
                kind = ExerciseKind.AROUND_CLOCK_SECTORS,
                title = "Кругосветка: Сектора",
                description = "По порядку S1–S20. Нужно попасть 1 раз — переход к следующему.",
                minutes = minutes,
                isMainFocus = true
            )
            WeaknessType.TREBLES -> TrainingBlock(
                kind = ExerciseKind.AROUND_CLOCK_TREBLES,
                title = "Кругосветка: Утроения",
                description = "По порядку T1–T20. Точность в утроения — база набора очков.",
                minutes = minutes,
                isMainFocus = true
            )
        }
    }

    private fun buildSecondBlock(type: WeaknessType, minutes: Int): TrainingBlock {
        return when (type) {
            WeaknessType.DOUBLES -> TrainingBlock(
                ExerciseKind.DOUBLES_TARGETED,
                "Точечные удвоения",
                "2–3 слабых дабла — точечная тренировка.",
                minutes
            )
            WeaknessType.SCORE -> TrainingBlock(
                ExerciseKind.SECTOR_20,
                "Сектор 20",
                "30 дротиков в 20-й сектор. Считаем сумму очков.",
                minutes
            )
            WeaknessType.ACCURACY -> TrainingBlock(
                ExerciseKind.AROUND_CLOCK_SECTORS,
                "Кругосветка: Сектора",
                "По порядку S1–S20, попасть 1 раз.",
                minutes
            )
            WeaknessType.TREBLES -> TrainingBlock(
                ExerciseKind.AROUND_CLOCK_TREBLES,
                "Кругосветка: Утроения",
                "По порядку T1–T20.",
                minutes
            )
        }
    }

    private fun buildGameDescription(focus: WeaknessType?): String {
        return when (focus) {
            WeaknessType.DOUBLES -> "Рекомендуем 501 — партия упирается в дабл, это закрепит тренировку."
            WeaknessType.SCORE -> "Рекомендуем Крикет — утроения T15–T20 и Bull подтянут набор."
            WeaknessType.ACCURACY -> "Рекомендуем 501 — точность выхода на дабл решает исход партии."
            WeaknessType.TREBLES -> "Рекомендуем Крикет — работа на утроения."
            null -> "Выбирай любую из 6 игр — просто играй в удовольствие."
        }
    }

    // ─────────────────────────────────────────────
    // КОНТРОЛЬНАЯ ТРЕНИРОВКА
    // ─────────────────────────────────────────────

    private fun buildControlPlan(minutes: Int): TrainingPlan {
        val blocks = mutableListOf<TrainingBlock>()

        val warmupExercises = getWarmupBlockItems()
        warmupExercises.forEach { ex ->
            blocks.add(
                TrainingBlock(
                    kind = ExerciseKind.WARMUP,
                    title = ex.title,
                    description = ex.whyImportant,
                    minutes = ex.minutes
                )
            )
        }

        blocks.add(
            TrainingBlock(
                ExerciseKind.SCORE_SET,
                "Большой раунд",
                "21 сектор × 3 дротика. Экзаменационный норматив.",
                15
            )
        )
        blocks.add(
            TrainingBlock(
                ExerciseKind.SECTOR_20,
                "Сектор 20",
                "30 дротиков в 20-й сектор. Экзаменационный норматив.",
                8
            )
        )
        blocks.add(
            TrainingBlock(
                ExerciseKind.SCORE_SET,
                "Набор очков",
                "10 подходов × 3 дротика. Экзаменационный норматив.",
                8
            )
        )
        blocks.add(
            TrainingBlock(
                ExerciseKind.GAME_501,
                "501 против бота",
                "Игра под давлением — результат идёт в отдельный раздел.",
                15
            )
        )
        blocks.add(
            TrainingBlock(
                ExerciseKind.GAME_CRICKET,
                "Крикет против бота",
                "Игра под давлением.",
                15
            )
        )
        blocks.add(
            TrainingBlock(
                ExerciseKind.STRENGTH,
                "Силовые упражнения",
                "Обязательны даже в контрольный день.",
                5
            )
        )

        return TrainingPlan(
            blocks = blocks,
            totalMinutes = blocks.sumOf { it.minutes },
            focus = null,
            isControl = true,
            postponedDoubles = emptyList()
        )
    }

    // ─────────────────────────────────────────────
    // ХЕЛПЕРЫ
    // ─────────────────────────────────────────────

    fun isControlSession(completedCount: Int): Boolean {
        if (completedCount <= 0) return false
        return completedCount % 10 == 0
    }

    fun sessionsUntilControl(completedCount: Int): Int {
        val remainder = completedCount % 10
        return if (remainder == 0) 10 else 10 - remainder
    }
}
