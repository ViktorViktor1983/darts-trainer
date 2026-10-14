package com.lodkin.dartstrainer.data.training

import kotlin.math.roundToInt

/**
 * Генератор программы тренировки на день.
 *
 * Правила (согласованы с автором):
 *   1. Максимум 1 тяжёлое упражнение за тренировку (кроме вводного режима).
 *   2. Первые 3 тренировки — вводный режим без тяжёлых упражнений,
 *      больше игровой формы:
 *        • Тренировки 1–2: только игры + лёгкие (difficulty 1–3).
 *        • Тренировка 3:   игры + лёгкие + средние (difficulty 1–6).
 *        • Тренировка 4+:  обычный режим.
 *   3. Первые 5 тренировок — сортировка по возрастанию тяжести
 *      (лёгкие идут первыми), чтобы игрок плавно вкатился.
 *   4. Учитывается слабое место (первое упражнение — на слабое).
 *   5. Учитывается реальный темп игрока (avgMinutesByExercise).
 *   6. Не более 50% времени на слабое место.
 *
 * Шкала тяжести (от лёгкого к тяжёлому, 1 = самое лёгкое):
 *   1 — Крикет
 *   2 — 501
 *   3 — Кругосветка: Сектора
 *   4 — Набор очков
 *   5 — Сектор 20
 *   6 — Большой раунд
 *   7 — Кругосветка: Удвоения
 *   8 — Кругосветка: Утроения
 *
 * Порог «тяжёлое»: difficulty >= 7.
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

    data class PlannedDynamicExercise(
        val id: String,
        val minutes: Int
    )

    /** Пороговое количество тренировок для «вводного режима». */
    private const val INTRO_TRAININGS_COUNT = 5

    /** Пороговое количество тренировок, до которого запрещены тяжёлые. */
    private const val INTRO_NO_HARD_COUNT = 3

    /** Порог, с которого упражнение считается тяжёлым. */
    private const val HARD_DIFFICULTY = 7

    /**
     * Шкала тяжести: чем больше число — тем тяжелее упражнение.
     */
    private fun difficultyOf(id: String): Int = when (id) {
        "cricket" -> 1
        "game501" -> 2
        "aroundclock_sectors" -> 3
        "scoreset" -> 4
        "sector20" -> 5
        "biground" -> 6
        "aroundclock_doubles" -> 7
        "aroundclock_trebles" -> 8
        else -> 4
    }

    /**
     * Сколько секунд отдыха после упражнения.
     *   • лёгкие (1–3)  — 3 минуты
     *   • средние (4–6) — 4 минуты
     *   • тяжёлые (7–8) — 5 минут
     *
     * Для разминочных / психологических / силовых id — 3 минуты.
     */
    fun restSecondsAfter(id: String): Int {
        val d = difficultyOf(id)
        return when {
            d <= 3 -> 3 * 60
            d <= 6 -> 4 * 60
            else -> 5 * 60
        }
    }

    // ─────────────────────────────────────────────
    // ГЛАВНАЯ ФУНКЦИЯ
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
     * @param analysis               результат анализа данных
     * @param budgetMinutes          сколько минут доступно на основную часть
     * @param completedTrainingCount сколько тренировок уже завершено (для вводного режима)
     */
    fun buildDynamicMainExerciseIds(
        analysis: TrainingStatsAnalyzer.TrainingAnalysis,
        budgetMinutes: Int,
        completedTrainingCount: Int = Int.MAX_VALUE
    ): List<PlannedDynamicExercise> {
        if (budgetMinutes <= 0) return emptyList()

        val isIntroductory = completedTrainingCount < INTRO_TRAININGS_COUNT
        val noHardAllowed = completedTrainingCount < INTRO_NO_HARD_COUNT

        // Порядок предпочтения по слабому месту
        val preferenceOrder: List<String> = when (analysis.weakestType) {
            WeaknessType.DOUBLES -> listOf(
                "aroundclock_doubles",
                "game501",
                "biground",
                "sector20",
                "scoreset",
                "aroundclock_sectors",
                "aroundclock_trebles",
                "cricket"
            )
            WeaknessType.SCORE -> listOf(
                "sector20",
                "game501",
                "scoreset",
                "biground",
                "aroundclock_doubles",
                "aroundclock_sectors",
                "aroundclock_trebles",
                "cricket"
            )
            WeaknessType.ACCURACY -> listOf(
                "aroundclock_sectors",
                "cricket",
                "biground",
                "sector20",
                "aroundclock_trebles",
                "scoreset",
                "aroundclock_doubles",
                "game501"
            )
            WeaknessType.TREBLES -> listOf(
                "aroundclock_trebles",
                "cricket",
                "biground",
                "aroundclock_sectors",
                "sector20",
                "scoreset",
                "aroundclock_doubles",
                "game501"
            )
            null -> listOf(
                "aroundclock_doubles",
                "sector20",
                "aroundclock_sectors",
                "scoreset",
                "biground",
                "aroundclock_trebles",
                "game501",
                "cricket"
            )
        }

        // ── Порядок упражнений ──
        // Вводный режим: сначала игры, потом лёгкие/средние по возрастанию тяжести.
        // Обычный режим: как в preferenceOrder (учитывает слабое место).
        val orderedIds: List<String> = if (isIntroductory) {
            val games = listOf("game501", "cricket")
            val others = preferenceOrder.filter { it !in games }
                .sortedBy { difficultyOf(it) }
            (games + others).distinct()
        } else {
            preferenceOrder
        }

        // ── Максимально допустимая тяжесть ──
        val maxAllowedDifficulty: Int = when {
            completedTrainingCount < 2 -> 3   // Тренировки 1–2: игры + лёгкие
            completedTrainingCount == 2 -> 6  // Тренировка 3: игры + лёгкие + средние
            else -> 8                          // Тренировка 4+: всё
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
            else -> 10
        }

        fun minutesFor(id: String): Int {
            val tempo = analysis.avgMinutesByExercise[id]
            val value = if (tempo != null && tempo > 0f) tempo.roundToInt() else fallbackMinutes(id)
            return value.coerceAtLeast(3)
        }

        // ── Набор с правилами ──
        var remaining = budgetMinutes
        val result = mutableListOf<PlannedDynamicExercise>()
        var hardCount = 0

        for (id in orderedIds) {
            val diff = difficultyOf(id)

            // Фильтр по максимально допустимой тяжести (вводный режим)
            if (diff > maxAllowedDifficulty) continue

            // Правило: максимум 1 тяжёлое за тренировку (кроме вводного режима)
            val isHard = diff >= HARD_DIFFICULTY
            if (isHard && noHardAllowed) continue
            if (isHard && hardCount >= 1) continue

            val m = minutesFor(id)
            if (m <= remaining) {
                result.add(PlannedDynamicExercise(id, m))
                remaining -= m
                if (isHard) hardCount++
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
