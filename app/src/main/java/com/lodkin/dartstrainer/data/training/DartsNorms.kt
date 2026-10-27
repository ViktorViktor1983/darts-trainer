package com.lodkin.dartstrainer.data.training

/**
 * Нормативы и шкала 16 уровней.
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
        LevelNorm(1, "Новичок", 300, 390, 250, 20f, 6f, 5f),
        LevelNorm(2, "Ученик", 350, 435, 320, 25f, 9f, 8f),
        LevelNorm(3, "Любитель", 400, 480, 390, 30f, 10f, 9f),
        LevelNorm(4, "Уверенный", 450, 525, 460, 35f, 12f, 11f),
        LevelNorm(5, "Опытный", 500, 570, 525, 40f, 15f, 14f),
        LevelNorm(6, "Практик", 550, 615, 590, 45f, 17f, 16f),
        LevelNorm(7, "Разрядник", 600, 660, 660, 50f, 18f, 17f),
        LevelNorm(8, "Турнирный", 630, 685, 680, 54f, 19f, 18f),
        LevelNorm(9, "Сильный", 660, 710, 705, 58f, 22f, 21f),
        LevelNorm(10, "Крепкий", 690, 735, 730, 62f, 25f, 24f),
        LevelNorm(11, "КМС", 720, 760, 750, 65f, 27f, 26f),
        LevelNorm(12, "Почти мастер", 750, 790, 780, 68f, 31f, 29f),
        LevelNorm(13, "Мастер", 780, 820, 810, 72f, 33f, 32f),
        LevelNorm(14, "Чемпион", 820, 860, 850, 76f, 38f, 36f),
        LevelNorm(15, "Профи", 860, 900, 890, 80f, 42f, 40f),
        LevelNorm(16, "Легенда", 900, 940, 930, 85f, 46f, 44f)
    )

    private val pprThresholds: List<Pair<Int, Float>> = listOf(
        1 to 22f, 2 to 28f, 3 to 33f, 4 to 38f,
        5 to 43f, 6 to 49f, 7 to 52f, 8 to 55f,
        9 to 61f, 10 to 66f, 11 to 71f, 12 to 80f,
        13 to 87f, 14 to 93f, 15 to 99f, 16 to 104f
    )

    private val mprThresholds: List<Pair<Int, Float>> = listOf(
        1 to 0.5f, 2 to 0.8f, 3 to 1.1f, 4 to 1.4f,
        5 to 1.7f, 6 to 2.0f, 7 to 2.3f, 8 to 2.6f,
        9 to 2.9f, 10 to 3.2f, 11 to 3.5f, 12 to 3.8f,
        13 to 4.1f, 14 to 4.4f, 15 to 4.7f, 16 to 5.0f
    )

    fun totalLevels(): Int = levels.size

    fun getNorm(level: Int): LevelNorm {
        val clamped = level.coerceIn(1, levels.size)
        return levels[clamped - 1]
    }

    fun getLevelName(level: Int): String = getNorm(level).name

    fun levelBySector20(value: Int): Int = levels.lastOrNull { value >= it.sector20 }?.level ?: 1
    fun levelByScoreSet(value: Int): Int = levels.lastOrNull { value >= it.scoreSet }?.level ?: 1
    fun levelByBigRound(value: Int): Int = levels.lastOrNull { value >= it.bigRound }?.level ?: 1
    fun levelBySectorsPercent(percent: Float): Int = levels.lastOrNull { percent >= it.sectorsPercent }?.level ?: 1
    fun levelByDoubles(percent: Float): Int = levels.lastOrNull { percent >= it.doublesPercent }?.level ?: 1
    fun levelByTrebles(percent: Float): Int = levels.lastOrNull { percent >= it.treblePercent }?.level ?: 1
    fun levelByPpr(ppr: Float): Int = pprThresholds.lastOrNull { ppr >= it.second }?.first ?: 1
    fun levelByMpr(mpr: Float): Int = mprThresholds.lastOrNull { mpr >= it.second }?.first ?: 1

    fun weaknessName(type: WeaknessType): String = when (type) {
        WeaknessType.SCORE -> "Набор очков"
        WeaknessType.DOUBLES -> "Удвоения"
        WeaknessType.ACCURACY -> "Точность в сектор"
        WeaknessType.TREBLES -> "Утроения"
    }

    /**
     * Определяет цветовой статус результата относительно текущего уровня.
     * 0 = Красный (ниже нормы), 1 = Желтый (в норме), 2 = Зеленый (выше нормы).
     */
    fun getStatus(value: Float, currentLevel: Int, type: String): Int {
        val currentNorm = getNorm(currentLevel)
        val nextNorm = if (currentLevel < 16) getNorm(currentLevel + 1) else currentNorm

        return when (type) {
            "game501" -> {
                val normCurrent = pprThresholds.firstOrNull { it.first == currentLevel }?.second ?: 0f
                val normNext = pprThresholds.firstOrNull { it.first == currentLevel + 1 }?.second ?: 999f
                if (value < normCurrent) 0 else if (value >= normNext) 2 else 1
            }
            "cricket" -> {
                val normCurrent = mprThresholds.firstOrNull { it.first == currentLevel }?.second ?: 0f
                val normNext = mprThresholds.firstOrNull { it.first == currentLevel + 1 }?.second ?: 99f
                if (value < normCurrent) 0 else if (value >= normNext) 2 else 1
            }
            "sector20" -> {
                if (value < currentNorm.sector20) 0 else if (value >= nextNorm.sector20) 2 else 1
            }
            "biground" -> {
                if (value < currentNorm.bigRound) 0 else if (value >= nextNorm.bigRound) 2 else 1
            }
            "scoreset" -> {
                if (value < currentNorm.scoreSet) 0 else if (value >= nextNorm.scoreSet) 2 else 1
            }
            "aroundclock_sectors" -> {
                if (value < currentNorm.sectorsPercent) 0 else if (value >= nextNorm.sectorsPercent) 2 else 1
            }
            "aroundclock_doubles" -> {
                if (value < currentNorm.doublesPercent) 0 else if (value >= nextNorm.doublesPercent) 2 else 1
            }
            "aroundclock_trebles" -> {
                if (value < currentNorm.treblePercent) 0 else if (value >= nextNorm.treblePercent) 2 else 1
            }
            else -> 1
        }
    }
}
