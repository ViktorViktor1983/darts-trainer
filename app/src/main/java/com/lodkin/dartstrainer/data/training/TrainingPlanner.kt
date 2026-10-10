package com.lodkin.dartstrainer.data.training

/**
 * Генератор программы тренировки на день.
 *
 * Логика:
 *   1. Берёт слабости из WeaknessDetector.
 *   2. Строит структуру: разминка → блок 1 → блок 2 → блок 3 → силовые.
 *   3. Распределяет время по минутам в зависимости от выбранной длительности.
 *   4. Учитывает отложенные даблы (возвращаются в начале тренировки).
 *   5. Учитывает контрольную тренировку (каждая 10-я).
 *
 * Программа НЕ зависит от желаний игрока. Игрок идёт по плану тренера.
 * Хочет «просто поиграть» — идёт в раздел «Просто поиграть».
 */
object TrainingPlanner {

    // ─────────────────────────────────────────────
    // МОДЕЛИ ПЛАНА
    // ─────────────────────────────────────────────

    /**
     * Что за упражнение в блоке.
     */
    enum class ExerciseKind {
        WARMUP_MUSCLES,       // Разминка: мышцы
        WARMUP_TRAJECTORY,    // Разминка: траектория
        WARMUP_ACCURACY,      // Разминка: кучность
        WARMUP_AIM,           // Разминка: прицеливание

        DOUBLES_ROUNDS,       // Раунды на удвоения (D1..D20 + Bull)
        DOUBLES_TARGETED,     // Точечная тренировка 2-3 слабых даблов
        DOUBLES_POSTPONED,    // Возврат отложенных даблов

        SECTOR_20,            // Сектор 20 (30 дротиков)
        SECTOR_19,            // Сектор 19 (30 дротиков)
        SCORE_SET,            // Набор очков (10 подходов × 3)

        AROUND_CLOCK_SECTORS, // Кругосветка: Сектора
        AROUND_CLOCK_DOUBLES, // Кругосветка: Удвоения
        AROUND_CLOCK_TREBLES, // Кругосветка: Утроения

        GAME_501,             // Партия 501
        GAME_CRICKET,         // Партия Крикет
        GAME_ANY,             // Игрок выбирает из 6 игр

        STRENGTH              // Силовые упражнения
    }

    /**
     * Один блок в тренировке.
     */
    data class TrainingBlock(
        val kind: ExerciseKind,
        val title: String,
        val description: String,
        val minutes: Int,
        val isMainFocus: Boolean = false
    )

    /**
     * Готовый план тренировки.
     */
    data class TrainingPlan(
        val blocks: List<TrainingBlock>,
        val totalMinutes: Int,
        val focus: WeaknessType?,       // Главное слабое место
        val isControl: Boolean,         // Это контрольная тренировка?
        val postponedDoubles: List<Int> // Какие даблы вернуть
    )

    // ─────────────────────────────────────────────
    // ГЛАВНАЯ ФУНКЦИЯ
    // ─────────────────────────────────────────────

    /**
     * Построить план тренировки на день.
     *
     * @param weaknesses     результат WeaknessDetector
     * @param minutes        общая длительность (30 / 45 / 60 / 80 / 100 / 120)
     * @param isControl      это контрольная тренировка?
     * @param postponedDoubles список номеров отложенных даблов (пусто — нет)
     */
    fun buildPlan(
        weaknesses: WeaknessDetector.WeaknessResult,
        minutes: Int,
        isControl: Boolean,
        postponedDoubles: List<Int> = emptyList()
    ): TrainingPlan {

        // Контрольная — отдельная программа
        if (isControl) {
            return buildControlPlan(minutes)
        }

        val blocks = mutableListOf<TrainingBlock>()
        var remaining = minutes

        // ── 1. Разминка (4 фазы) ──
        val warmup = buildWarmup(minutes)
        blocks.addAll(warmup)
        remaining -= warmup.sumOf { it.minutes }

        // ── 2. Отложенные даблы (если есть) ──
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

        // ── 3. Блок 1 — главная слабость ──
        val main = weaknesses.mainWeakness
        if (main != null && remaining > 0) {
            val timeMain = (remaining * 0.5).toInt().coerceAtLeast(10)
            blocks.add(buildMainBlock(main.type, timeMain))
            remaining -= timeMain
        }

        // ── 4. Блок 2 — вторая слабость (если время) ──
        val second = weaknesses.weaknesses.getOrNull(1)
        if (second != null && remaining >= 15) {
            val timeSecond = (remaining * 0.5).toInt().coerceAtLeast(10)
            blocks.add(buildSecondBlock(second.type, timeSecond))
            remaining -= timeSecond
        }

        // ── 5. Блок 3 — игра на разнообразие ──
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

        // ── 6. Силовые (всегда) ──
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
    // РАЗМИНКА
    // ─────────────────────────────────────────────

    /**
     * Разминка. Разное время для коротких и длинных тренировок.
     */
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
    // БЛОК 1 — ГЛАВНАЯ СЛАБОСТЬ
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

    // ─────────────────────────────────────────────
    // БЛОК 2 — ВТОРАЯ СЛАБОСТЬ
    // ─────────────────────────────────────────────

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

    // ─────────────────────────────────────────────
    // ОПИСАНИЕ ИГРОВОГО БЛОКА
    // ─────────────────────────────────────────────

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

    /**
     * Контрольная тренировка — отдельная программа.
     * 5 упражнений: Большой раунд, Сектор 20, Набор очков, 501, Крикет.
     */
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

    /**
     * Каждые 10 тренировок — контрольная.
     */
    fun isControlSession(completedCount: Int): Boolean {
        if (completedCount <= 0) return false
        return completedCount % 10 == 0
    }

    /**
     * Определить, сколько тренировок осталось до контрольной.
     */
    fun sessionsUntilControl(completedCount: Int): Int {
        val remainder = completedCount % 10
        return if (remainder == 0) 10 else 10 - remainder
    }
}
