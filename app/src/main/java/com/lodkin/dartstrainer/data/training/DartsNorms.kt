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
 *
 * ЧЕТЫРЕ ПАРАМЕТРА (комплекс 1 — упражнения):
 *   • sector20 / scoreSet / bigRound — основные нормативные упражнения
 *   • sectorsPercent — процент попадания в обычные сектора (Кругосветка-Сектора)
 *   • doublesPercent — процент попадания в удвоения (Кругосветка-Удвоения)
 *   • treblePercent  — процент попадания в утроения (Кругосветка-Утроения)
 *
 * ДВЕ ШКАЛЫ (комплекс 2 — игры):
 *   • PPR — средний набор за подход в 501
 *   • MPR — средний набор за подход в Крикете (максимум 9.0)
 */
data class LevelNorm(
    val level: Int,
    val name: String,
    val sector20: Int,          // очки за 30 дротиков в 20-й сектор
    val scoreSet: Int,          // очки за 30 дротиков (упражнение «Набор очков»)
    val bigRound: Int,          // очки за 63 дротика (21 сектор × 3)
    val sectorsPercent: Float,  // процент попадания в обычные сектора (Кругосветка)
    val doublesPercent: Float,  // процент попадания в удвоения
    val treblePercent: Float    // процент попадания в утроения
)

object DartsNorms {

    val levels: List<LevelNorm> = listOf(
        LevelNorm(
            level = 1, name = "Новичок",
            sector20 = 300, scoreSet = 390, bigRound = 250,
            sectorsPercent = 20f, doublesPercent = 6f, treblePercent = 5f
        ),
        LevelNorm(
            level = 2, name = "Ученик",
            sector20 = 350, scoreSet = 435, bigRound = 320,
            sectorsPercent = 25f, doublesPercent = 9f, treblePercent = 8f
        ),
        LevelNorm(
            level = 3, name = "Любитель",
            sector20 = 400, scoreSet = 480, bigRound = 390,
            sectorsPercent = 30f, doublesPercent = 10f, treblePercent = 9f
        ),
        LevelNorm(
            level = 4, name = "Уверенный",
            sector20 = 450, scoreSet = 525, bigRound = 460,
            sectorsPercent = 35f, doublesPercent = 12f, treblePercent = 11f
        ),
        LevelNorm(
            level = 5, name = "Опытный",
            sector20 = 500, scoreSet = 570, bigRound = 525,
            sectorsPercent = 40f, doublesPercent = 15f, treblePercent = 14f
        ),
        LevelNorm(
            level = 6, name = "Практик",
            sector20 = 550, scoreSet = 615, bigRound = 590,
            sectorsPercent = 45f, doublesPercent = 17f, treblePercent = 16f
        ),
        // ── Известная точка: I взрослый разряд ──
        LevelNorm(
            level = 7, name = "Разрядник",
            sector20 = 600, scoreSet = 660, bigRound = 660,
            sectorsPercent = 50f, doublesPercent = 18f, treblePercent = 17f
        ),
        LevelNorm(
            level = 8, name = "Турнирный",
            sector20 = 630, scoreSet = 685, bigRound = 680,
            sectorsPercent = 54f, doublesPercent = 19f, treblePercent = 18f
        ),
        LevelNorm(
            level = 9, name = "Сильный",
            sector20 = 660, scoreSet = 710, bigRound = 705,
            sectorsPercent = 58f, doublesPercent = 22f, treblePercent = 21f
        ),
        LevelNorm(
            level = 10, name = "Крепкий",
            sector20 = 690, scoreSet = 735, bigRound = 730,
            sectorsPercent = 62f, doublesPercent = 25f, treblePercent = 24f
        ),
        // ── Известная точка: КМС ──
        LevelNorm(
            level = 11, name = "КМС",
            sector20 = 720, scoreSet = 760, bigRound = 750,
            sectorsPercent = 65f, doublesPercent = 27f, treblePercent = 26f
        ),
        LevelNorm(
            level = 12, name = "Почти мастер",
            sector20 = 750, scoreSet = 790, bigRound = 780,
            sectorsPercent = 68f, doublesPercent = 31f, treblePercent = 29f
        ),
        // ── Известная точка: МС ──
        LevelNorm(
            level = 13, name = "Мастер",
            sector20 = 780, scoreSet = 820, bigRound = 810,
            sectorsPercent = 72f, doublesPercent = 33f, treblePercent = 32f
        ),
        LevelNorm(
            level = 14, name = "Чемпион",
            sector20 = 820, scoreSet = 860, bigRound = 850,
            sectorsPercent = 76f, doublesPercent = 38f, treblePercent = 36f
        ),
        LevelNorm(
            level = 15, name = "Профи",
            sector20 = 860, scoreSet = 900, bigRound = 890,
            sectorsPercent = 80f, doublesPercent = 42f, treblePercent = 40f
        ),
        // ── Известная точка: уровень Люка Литтлера ──
        LevelNorm(
            level = 16, name = "Легенда",
            sector20 = 900, scoreSet = 940, bigRound = 930,
            sectorsPercent = 85f, doublesPercent = 46f, treblePercent = 44f
        )
    )

    // ─────────────────────────────────────────────
    // Шкала PPR (501) и MPR (Крикет)
    // ─────────────────────────────────────────────

    /** Минимальный PPR для каждого уровня (1..16). */
    private val pprThresholds: List<Pair<Int, Float>> = listOf(
        1 to 22f, 2 to 28f, 3 to 33f, 4 to 38f,
        5 to 43f, 6 to 49f, 7 to 52f, 8 to 55f,
        9 to 61f, 10 to 66f, 11 to 71f, 12 to 80f,
        13 to 87f, 14 to 93f, 15 to 99f, 16 to 104f
    )

    /** 
     * Минимальный MPR для каждого уровня (1..16). 
     * Максимум в дартсе — 9.0. Уровень 16 — 5.0 (мировой класс).
     */
    private val mprThresholds: List<Pair<Int, Float>> = listOf(
        1 to 0.5f, 2 to 0.8f, 3 to 1.1f, 4 to 1.4f,
        5 to 1.7f, 6 to 2.0f, 7 to 2.3f, 8 to 2.6f,
        9 to 2.9f, 10 to 3.2f, 11 to 3.5f, 12 to 3.8f,
        13 to 4.1f, 14 to 4.4f, 15 to 4.7f, 16 to 5.0f
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
     * Определить уровень по проценту попадания в обычные сектора (Кругосветка).
     */
    fun levelBySectorsPercent(percent: Float): Int =
        levels.lastOrNull { percent >= it.sectorsPercent }?.level ?: 1

    /**
     * Определить уровень по проценту попадания в удвоения.
     */
    fun levelByDoubles(percent: Float): Int =
        levels.lastOrNull { percent >= it.doublesPercent }?.level ?: 1

    /**
     * Определить уровень по проценту попадания в утроения.
     */
    fun levelByTrebles(percent: Float): Int =
        levels.lastOrNull { percent >= it.treblePercent }?.level ?: 1

    /**
     * Определить уровень по среднему PPR (501).
     */
    fun levelByPpr(ppr: Float): Int =
        pprThresholds.lastOrNull { ppr >= it.second }?.first ?: 1

    /**
     * Определить уровень по среднему MPR (Крикет).
     */
    fun levelByMpr(mpr: Float): Int =
        mprThresholds.lastOrNull { mpr >= it.second }?.first ?: 1

    /**
     * Русское название параметра — для UI.
     */
    fun weaknessName(type: WeaknessType): String = when (type) {
        WeaknessType.SCORE    -> "Набор очков"
        WeaknessType.DOUBLES  -> "Удвоения"
        WeaknessType.ACCURACY -> "Точность в сектор"
        WeaknessType.TREBLES  -> "Утроения"
    }
}
