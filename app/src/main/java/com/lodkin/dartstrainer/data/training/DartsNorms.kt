package com.lodkin.dartstrainer.data.training

/**
 * Нормативы и шкала 16 уровней.
 *
 * Известные точки (официальные разряды):
 *   • Уровень 7  (Разрядник) = I взрослый разряд
 *   • Уровень 11 (КМС)       = КМС
 *   • Уровень 13 (Мастер)    = МС
 *   • Уровень 16 (Легенда)   = уровень Люка Литтлера
 *
 * Остальные уровни — промежуточные ступени для мотивации игрока.
 * Числа между известными точками распределены пропорционально.
 * При необходимости корректируются в процессе.
 *
 * Пол и возраст не спрашиваем — используем мужские нормативы.
 */

data class LevelNorm(
    val level: Int,
    val name: String,
    val sector20: Int,          // очки за 30 дротиков в 20-й сектор
    val scoreSet: Int,          // очки за 30 дротиков (упражнение «Набор очков»)
    val bigRound: Int,          // очки за 63 дротика (21 сектор × 3)
    val doublesPercent: Float   // процент попадания в удвоения
)

object DartsNorms {

    val levels: List<LevelNorm> = listOf(
        LevelNorm(
            level = 1, name = "Новичок",
            sector20 = 300, scoreSet = 390, bigRound = 250,
            doublesPercent = 6f
        ),
        LevelNorm(
            level = 2, name = "Ученик",
            sector20 = 350, scoreSet = 435, bigRound = 320,
            doublesPercent = 9f
        ),
        LevelNorm(
            level = 3, name = "Любитель",
            sector20 = 400, scoreSet = 480, bigRound = 390,
            doublesPercent = 10f
        ),
        LevelNorm(
            level = 4, name = "Уверенный",
            sector20 = 450, scoreSet = 525, bigRound = 460,
            doublesPercent = 12f
        ),
        LevelNorm(
            level = 5, name = "Опытный",
            sector20 = 500, scoreSet = 570, bigRound = 525,
            doublesPercent = 15f
        ),
        LevelNorm(
            level = 6, name = "Практик",
            sector20 = 550, scoreSet = 615, bigRound = 590,
            doublesPercent = 17f
        ),
        // ── Известная точка: I взрослый разряд ──
        LevelNorm(
            level = 7, name = "Разрядник",
            sector20 = 600, scoreSet = 660, bigRound = 660,
            doublesPercent = 18f
        ),
        LevelNorm(
            level = 8, name = "Турнирный",
            sector20 = 630, scoreSet = 685, bigRound = 680,
            doublesPercent = 19f
        ),
        LevelNorm(
            level = 9, name = "Сильный",
            sector20 = 660, scoreSet = 710, bigRound = 705,
            doublesPercent = 22f
        ),
        LevelNorm(
            level = 10, name = "Крепкий",
            sector20 = 690, scoreSet = 735, bigRound = 730,
            doublesPercent = 25f
        ),
        // ── Известная точка: КМС ──
        LevelNorm(
            level = 11, name = "КМС",
            sector20 = 720, scoreSet = 760, bigRound = 750,
            doublesPercent = 27f
        ),
        LevelNorm(
            level = 12, name = "Почти мастер",
            sector20 = 750, scoreSet = 790, bigRound = 780,
            doublesPercent = 31f
        ),
        // ── Известная точка: МС ──
        LevelNorm(
            level = 13, name = "Мастер",
            sector20 = 780, scoreSet = 820, bigRound = 810,
            doublesPercent = 33f
        ),
        LevelNorm(
            level = 14, name = "Чемпион",
            sector20 = 820, scoreSet = 860, bigRound = 850,
            doublesPercent = 38f
        ),
        LevelNorm(
            level = 15, name = "Профи",
            sector20 = 860, scoreSet = 900, bigRound = 890,
            doublesPercent = 42f
        ),
        // ── Известная точка: уровень Люка Литтлера ──
        LevelNorm(
            level = 16, name = "Легенда",
            sector20 = 900, scoreSet = 940, bigRound = 930,
            doublesPercent = 46f
        )
    )

    // ─────────────────────────────────────────────
    // Хелперы
    // ─────────────────────────────────────────────

    fun totalLevels(): Int = levels.size

    /**
     * Вернуть норматив по номеру уровня (1..16).
     * Если номер вне диапазона — возвращаем крайний.
     */
    fun getNorm(level: Int): LevelNorm {
        val clamped = level.coerceIn(1, levels.size)
        return levels[clamped - 1]
    }

    fun getLevelName(level: Int): String = getNorm(level).name

    /**
     * Определить уровень по результату упражнения «Сектор 20».
     */
    fun levelBySector20(value: Int): Int =
        levels.lastOrNull { value >= it.sector20 }?.level ?: 1

    /**
     * Определить уровень по результату упражнения «Набор очков».
     */
    fun levelByScoreSet(value: Int): Int =
        levels.lastOrNull { value >= it.scoreSet }?.level ?: 1

    /**
     * Определить уровень по результату «Большого раунда».
     */
    fun levelByBigRound(value: Int): Int =
        levels.lastOrNull { value >= it.bigRound }?.level ?: 1

    /**
     * Определить уровень по проценту попадания в удвоения.
     */
    fun levelByDoubles(percent: Float): Int =
        levels.lastOrNull { percent >= it.doublesPercent }?.level ?: 1

    /**
     * Русское название параметра — для UI.
     */
    fun weaknessName(type: WeaknessType): String = when (type) {
        WeaknessType.SCORE    -> "Набор очков"
        WeaknessType.DOUBLES  -> "Удвоения"
        WeaknessType.ACCURACY -> "Точность в сектор"
    }
}
