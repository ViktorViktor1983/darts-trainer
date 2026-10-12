package com.lodkin.dartstrainer.data.training

import kotlin.math.roundToInt

/**
 * Генератор программы тренировки на день.
 *
 * Основные задачи:
 *   1. Динамический выбор упражнений основной части на основе анализа данных
 *      (слабое место + реальный темп игрока).
 *   2. Разминка и вспомогательные блоки — по фиксированной логике.
 *   3. Правило: не более 50% времени основной части на слабое место.
 *
 * Программа НЕ зависит от желаний игрока. Игрок идёт по плану тренера.
 * Хочет «просто поиграть» — идёт в раздел «Просто поиграть».
 */
object TrainingPlanner {

    // ─────────────────────────────────────────────
    // МОДЕЛИ ПЛАНА
    // ─────────────────────────────────────────────

    enum class ExerciseKind {
        WARMUP_MUSCLES,
        WARMUP_TRAJECTORY,
        WARMUP_ACCURACY,
        WARMUP_AIM,

        DOUBLES_ROUNDS,
        DOUBLES_TARGETED,
        DOUBLES_POSTPONED,

        SECTOR_20,
        SECTOR_19,
        SCORE_SET,

        AROUND_CLOCK_SECTORS,
        AROUND_CLOCK_DOUBLES,
        AROUND_CLOCK_TREBLES,

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

    /**
     * Динамически выбранное упражнение основной части.
     */
    data class PlannedDynamicExercise(
        val id: String,
        val minutes: Int
    )

    // ─────────────────────────────────────────────
    // ГЛАВНАЯ ФУНКЦИЯ (пока не используется в UI)
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

        val warmup = buildWarmup(minutes)
        blocks.addAll(warmup)
        remaining -= warmup.sumOf { it.minutes }

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

    /**
     * Подобрать упражнения основной части на день.
     *
     * Логика:
     *   1. Первым идёт упражнение на слабое место (из анализа).
     *   2. Дальше — остальные 5 упражнений в порядке полезности.
     *   3. Учитываем бюджет времени и реальный темп игрока (avgMinutesByExercise).
     *   4. Правило «не более 50% на слабое место»: одно упражнение на слабое —
     *      это максимум 1 из 3-4, то есть ≤50%.
     *
     * @param analysis  результат TrainingStatsAnalyzer.analyze()
     * @param budgetMinutes  сколько минут доступно на основную часть
     * @return список id + минуты, в порядке прохождения
     */
    fun buildDynamicMainExerciseIds(
        analysis: TrainingStatsAnalyzer.TrainingAnalysis,
        budgetMinutes: Int
    ): List<PlannedDynamicExercise> {
        if (budgetMinutes <= 0) return emptyList()

        // Все 6 упражнений основной части (в порядке приоритета показа)
        val allIds = listOf(
            "aroundclock_doubles",
            "sector20",
            "aroundclock_sectors",
            "scoreset",
            "biground",
            "aroundclock_trebles"
        )

        // Стандартные минуты — если темпа ещё нет
        fun fallbackMinutes(id: String): Int = when (id) {
            "aroundclock_doubles" -> 14
            "aroundclock_sectors" -> 12
            "aroundclock_trebles" -> 12
            "sector20" -> 8
            "scoreset" -> 6
            "biground" -> 12
            else -> 10
        }

        // Реальные минуты с учётом темпа
        fun minutesFor(id: String): Int {
            val tempo = analysis.avgMinutesByExercise[id]
            val value = if (tempo != null && tempo > 0f) tempo.roundToInt() else fallbackMinutes(id)
            return value.coerceAtLeast(3)
        }

        // Главное упражнение — на слабое место
        val mainFocusId = when (analysis.weakestType) {
            WeaknessType.DOUBLES -> "aroundclock_doubles"
            WeaknessType.SCORE -> "sector20"
            WeaknessType.ACCURACY -> "aroundclock_sectors"
            WeaknessType.TREBLES -> "aroundclock_trebles"
            null -> "aroundclock_doubles"
        }

        // Упорядоченный список: сначала главное, потом остальные
        val orderedIds = mutableListOf<String>()
        orderedIds.add(mainFocusId)
        allIds.forEach { if (it != mainFocusId) orderedIds.add(it) }

        // Набираем по бюджету
        var remaining = budgetMinutes
        val result = mutableListOf<PlannedDynamicExercise>()
        for (id in orderedIds) {
            val m = minutesFor(id)
            if (m <= remaining) {
                result.add(PlannedDynamicExercise(id, m))
                remaining -= m
            }
        }

        return result
    }

    // ─────────────────────────────────────────────
    // РАЗМИНКА
    // ─────────────────────────────────────────────

    private fun buildWarmup(totalMinutes: Int): List<TrainingBlock> {
        val long = totalMinutes >= 100
        val traj = if (long) 2 else 1
        val acc = if (long) 2 else 1

        return listOf(
            TrainingBlock(
                ExerciseKind.WARMUP_MUSCLES,
                "Разминка: мышцы",
                "Вращения плечами, локтями, кистями. Растяжка.",
                3
            ),
            TrainingBlock(
                ExerciseKind.WARMUP_TRAJECTORY,
                "Разминка: траектория",
                "Бросай без цели, следи только за движением руки.",
                traj
            ),
            TrainingBlock(
                ExerciseKind.WARMUP_ACCURACY,
                "Разминка: кучность",
                "Второй и третий — тем же движением. Следи за одинаковостью.",
                acc
            ),
            TrainingBlock(
                ExerciseKind.WARMUP_AIM,
                "Прицеливание",
                "Теперь целься в конкретный сектор.",
                1
            )
        )
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
            WeaknessType.DOUBLES ->
                "Рекомендуем 501 — партия упирается в дабл, это закрепит тренировку."
            WeaknessType.SCORE ->
                "Рекомендуем Крикет — утроения T15–T20 и Bull подтянут набор."
            WeaknessType.ACCURACY ->
                "Рекомендуем 501 — точность выхода на дабл решает исход партии."
            WeaknessType.TREBLES ->
                "Рекомендуем Крикет — работа на утроения."
            null ->
                "Выбирай любую из 6 игр — просто играй в удовольствие."
        }
    }

    // ─────────────────────────────────────────────
    // КОНТРОЛЬНАЯ ТРЕНИРОВКА
    // ─────────────────────────────────────────────

    private fun buildControlPlan(minutes: Int): TrainingPlan {
        val blocks = mutableListOf<TrainingBlock>()

        blocks.add(
            TrainingBlock(
                ExerciseKind.WARMUP_MUSCLES,
                "Разминка: мышцы",
                "Лёгкая разминка перед экзаменом.",
                3
            )
        )
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
