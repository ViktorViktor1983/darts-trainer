package com.lodkin.dartstrainer.data.training

import com.lodkin.dartstrainer.data.training.LevelCalculator.Trend

/**
 * Определение слабых мест игрока.
 *
 * Логика:
 *   1. Сравниваем каждый из 4 параметров с порогом текущего уровня.
 *   2. Чем больше «отставание» — тем выше приоритет.
 *   3. Если параметр падает по тренду — приоритет повышается.
 *   4. Возвращаем список слабостей, отсортированный по приоритету.
 *
 * Программа использует это, чтобы построить план тренировки:
 *   • Слабость №1 — 50% тренировки.
 *   • Слабость №2 — поддержка.
 *   • Остальное — комплексные игры, силовые.
 */
object WeaknessDetector {

    /**
     * Одна найденная слабость.
     *
     * @param type          какой параметр
     * @param currentValue  текущее значение игрока
     * @param normValue     порог текущего уровня (по этому параметру)
     * @param gapPercent    насколько отстаёт (в % от нормы). 0 = на уровне.
     * @param isFalling     тренд: падает ли
     * @param priority      1 = главная, 2 = вторая, 3 = третья
     */
    data class Weakness(
        val type: WeaknessType,
        val currentValue: Float,
        val normValue: Float,
        val gapPercent: Float,
        val isFalling: Boolean,
        val priority: Int
    )

    /**
     * Результат анализа слабых мест.
     */
    data class WeaknessResult(
        val weaknesses: List<Weakness>,     // Отсортированы по приоритету
        val mainWeakness: Weakness?         // Главная слабость (или null, если всё в норме)
    )

    /**
     * Определить слабые места.
     *
     * @param level        текущий уровень игрока (1..16)
     * @param scoreValue   текущее значение «Набор очков» (или null, если нет данных)
     * @param accuracyValue текущее значение «Точность в сектор»
     * @param doublesValue  текущее значение «Удвоения» (%)
     * @param treblesValue  текущее значение «Утроения» (%)
     * @param scoreTrend    тренд по набору (RISING / FALLING / STABLE)
     * @param accuracyTrend тренд по точности
     * @param doublesTrend  тренд по удвоениям
     * @param treblesTrend  тренд по утроениям
     */
    fun detect(
        level: Int,
        scoreValue: Float?,
        accuracyValue: Float?,
        doublesValue: Float?,
        treblesValue: Float?,
        scoreTrend: Trend = Trend.STABLE,
        accuracyTrend: Trend = Trend.STABLE,
        doublesTrend: Trend = Trend.STABLE,
        treblesTrend: Trend = Trend.STABLE
    ): WeaknessResult {

        val norm = DartsNorms.getNorm(level)
        val candidates = mutableListOf<Weakness>()

        // ── Набор очков ──
        scoreValue?.let { value ->
            val normValue = norm.scoreSet.toFloat()
            val gap = calculateGap(value, normValue)
            if (gap > 0f || scoreTrend == Trend.FALLING) {
                candidates.add(
                    Weakness(
                        type = WeaknessType.SCORE,
                        currentValue = value,
                        normValue = normValue,
                        gapPercent = gap,
                        isFalling = scoreTrend == Trend.FALLING,
                        priority = 0 // назначим после сортировки
                    )
                )
            }
        }

        // ── Точность в сектор ──
        accuracyValue?.let { value ->
            val normValue = norm.bigRound.toFloat()
            val gap = calculateGap(value, normValue)
            if (gap > 0f || accuracyTrend == Trend.FALLING) {
                candidates.add(
                    Weakness(
                        type = WeaknessType.ACCURACY,
                        currentValue = value,
                        normValue = normValue,
                        gapPercent = gap,
                        isFalling = accuracyTrend == Trend.FALLING,
                        priority = 0
                    )
                )
            }
        }

        // ── Удвоения ──
        doublesValue?.let { value ->
            val normValue = norm.doublesPercent
            val gap = calculateGap(value, normValue)
            if (gap > 0f || doublesTrend == Trend.FALLING) {
                candidates.add(
                    Weakness(
                        type = WeaknessType.DOUBLES,
                        currentValue = value,
                        normValue = normValue,
                        gapPercent = gap,
                        isFalling = doublesTrend == Trend.FALLING,
                        priority = 0
                    )
                )
            }
        }

        // ── Утроения ──
        treblesValue?.let { value ->
            val normValue = norm.treblePercent
            val gap = calculateGap(value, normValue)
            if (gap > 0f || treblesTrend == Trend.FALLING) {
                candidates.add(
                    Weakness(
                        type = WeaknessType.TREBLES,
                        currentValue = value,
                        normValue = normValue,
                        gapPercent = gap,
                        isFalling = treblesTrend == Trend.FALLING,
                        priority = 0
                    )
                )
            }
        }

        // ── Сортировка по приоритету ──
        // Сначала те, что падают (тренд вниз), потом по величине отставания.
        val sorted = candidates
            .sortedWith(
                compareByDescending<Weakness> { it.isFalling }
                    .thenByDescending { it.gapPercent }
            )
            .mapIndexed { index, w -> w.copy(priority = index + 1) }

        return WeaknessResult(
            weaknesses = sorted,
            mainWeakness = sorted.firstOrNull()
        )
    }

    /**
     * Насколько значение отстаёт от нормы (в % от нормы).
     * 0 или меньше — значит, норма выполнена или превышена.
     */
    private fun calculateGap(current: Float, norm: Float): Float {
        if (norm <= 0f) return 0f
        if (current >= norm) return 0f
        return (norm - current) / norm * 100f
    }

    /**
     * Человеческое название слабости — для UI.
     */
    fun describe(type: WeaknessType): String = when (type) {
        WeaknessType.SCORE -> "Набор очков"
        WeaknessType.ACCURACY -> "Точность в сектор"
        WeaknessType.DOUBLES -> "Удвоения"
        WeaknessType.TREBLES -> "Утроения"
    }
}
