package com.lodkin.dartstrainer.data.training

import kotlin.math.ceil

/**
 * Расчёт уровня, потенциала и трендов игрока.
 *
 * ЛОГИКА УРОВНЯ (два независимых комплекса):
 *
 *   Комплекс 1 — 501:
 *     Даёт PPR. Уровень определяется по таблице ботов 501.
 *
 *   Комплекс 2 — упражнения:
 *     Даёт 4 чистых параметра: набор очков, точность в сектор,
 *     удвоения, утроения. Уровень = среднее 4 уровней, округление ВВЕРХ.
 *
 *   ИТОГОВЫЙ УРОВЕНЬ = МАКСИМУМ из двух комплексов.
 *
 *   Расхождение между комплексами — сигнал психологии:
 *     • 501 ниже упражнений → «в игре давит давление».
 *     • Упражнения ниже 501 → «в упражнениях не выкладываешься».
 *
 * Уровень пересчитывается ПОСЛЕ экзамена (раз в 10 тренировок).
 * Внутри цикла — только тренды и корректировка плана.
 */
object LevelCalculator {

    /**
     * Результат расчёта уровня.
     */
    data class LevelResult(
        val level: Int,                 // Итоговый уровень (1..16)
        val potential: Int,             // Потенциал — уровень по лучшему параметру
        val weakestType: WeaknessType?, // Что тянет вниз
        val levelByScore: Int,          // Уровень по набору очков (0 = нет данных)
        val levelByAccuracy: Int,       // Уровень по точности в сектор
        val levelByDoubles: Int,        // Уровень по удвоениям
        val levelByTrebles: Int,        // Уровень по утроениям
        val levelByPpr: Int             // Уровень по 501 (PPR), 0 = нет данных
    )

    /**
     * Рассчитать уровень по комплексу «упражнения» (4 параметра).
     *
     * @param avgScore     средний результат по набору очков (Сектор 20 / Набор очков)
     * @param avgAccuracy  средний результат по Большому раунду (точность в сектор)
     * @param avgDoubles   средний процент попадания в удвоения
     * @param avgTrebles   средний процент попадания в утроения
     */
    fun calculateExerciseLevel(
        avgScore: Int?,
        avgAccuracy: Int?,
        avgDoubles: Float?,
        avgTrebles: Float?
    ): LevelResult {

        val levelByScore = avgScore?.let { DartsNorms.levelBySector20(it) } ?: 0
        val levelByAccuracy = avgAccuracy?.let { DartsNorms.levelByBigRound(it) } ?: 0
        val levelByDoubles = avgDoubles?.let { DartsNorms.levelByDoubles(it) } ?: 0
        val levelByTrebles = avgTrebles?.let { DartsNorms.levelByTrebles(it) } ?: 0

        val validLevels = mutableListOf<Int>()
        if (levelByScore > 0) validLevels.add(levelByScore)
        if (levelByAccuracy > 0) validLevels.add(levelByAccuracy)
        if (levelByDoubles > 0) validLevels.add(levelByDoubles)
        if (levelByTrebles > 0) validLevels.add(levelByTrebles)

        if (validLevels.isEmpty()) {
            return LevelResult(
                level = 1, potential = 1, weakestType = null,
                levelByScore = 0, levelByAccuracy = 0,
                levelByDoubles = 0, levelByTrebles = 0, levelByPpr = 0
            )
        }

        // Среднее, округление ВВЕРХ
        val sum = validLevels.sum()
        val avg = sum.toDouble() / validLevels.size
        val finalLevel = ceil(avg).toInt().coerceIn(1, DartsNorms.totalLevels())

        // Потенциал = максимум
        val potential = validLevels.maxOrNull() ?: finalLevel

        // Слабый параметр = минимальный
        val minLevel = validLevels.minOrNull() ?: finalLevel
        val weakestType = when {
            levelByScore > 0 && levelByScore == minLevel -> WeaknessType.SCORE
            levelByAccuracy > 0 && levelByAccuracy == minLevel -> WeaknessType.ACCURACY
            levelByDoubles > 0 && levelByDoubles == minLevel -> WeaknessType.DOUBLES
            levelByTrebles > 0 && levelByTrebles == minLevel -> WeaknessType.TREBLES
            else -> null
        }

        return LevelResult(
            level = finalLevel,
            potential = potential,
            weakestType = weakestType,
            levelByScore = levelByScore,
            levelByAccuracy = levelByAccuracy,
            levelByDoubles = levelByDoubles,
            levelByTrebles = levelByTrebles,
            levelByPpr = 0
        )
    }

    /**
     * Рассчитать ИТОГОВЫЙ уровень: максимум из двух комплексов.
     *
     * @param exerciseResult результат по 4 параметрам (комплекс «упражнения»)
     * @param pprLevel       уровень по 501 (PPR) из таблицы ботов. 0 = нет данных.
     */
    fun calculateFinalLevel(
        exerciseResult: LevelResult,
        pprLevel: Int
    ): LevelResult {
        val finalLevel = maxOf(exerciseResult.level, pprLevel)
            .coerceIn(1, DartsNorms.totalLevels())

        return exerciseResult.copy(
            level = finalLevel,
            levelByPpr = pprLevel
        )
    }

    /**
     * Определить, расходятся ли комплексы. Если да — вернуть тип расхождения.
     */
    fun detectDiscrepancy(exerciseResult: LevelResult, pprLevel: Int): Discrepancy {
        if (pprLevel == 0) return Discrepancy.NONE
        val diff = pprLevel - exerciseResult.level
        return when {
            diff <= -2 -> Discrepancy.PPR_LOWER      // 501 хуже упражнений — давление
            diff >= 2 -> Discrepancy.PPR_HIGHER       // 501 лучше упражнений — расслаблен
            else -> Discrepancy.NONE
        }
    }

    enum class Discrepancy {
        NONE,         // Расхождения нет
        PPR_LOWER,    // 501 хуже упражнений → в игре давит
        PPR_HIGHER    // 501 лучше упражнений → в упражнениях не выкладывается
    }

    // ─────────────────────────────────────────────
    // ТРЕНДЫ
    // ─────────────────────────────────────────────

    enum class Trend {
        RISING,     // Растёт
        FALLING,    // Падает
        STABLE      // Стоит на месте
    }

    /**
     * Определить тренд по значениям (берём 3 последних).
     * Если значений меньше 3 — считаем STABLE.
     */
    fun detectTrend(values: List<Float>): Trend {
        if (values.size < 3) return Trend.STABLE
        val last3 = values.takeLast(3)
        val rising = last3[0] < last3[1] && last3[1] < last3[2]
        val falling = last3[0] > last3[1] && last3[1] > last3[2]
        return when {
            rising -> Trend.RISING
            falling -> Trend.FALLING
            else -> Trend.STABLE
        }
    }

    // ─────────────────────────────────────────────
    // СООБЩЕНИЯ
    // ─────────────────────────────────────────────

    /**
     * Мягкий намёк, что параметр просел.
     * Игроку не показываем цифры — только мягкое сообщение.
     */
    fun buildDeclineHint(weak: WeaknessType): String {
        return when (weak) {
            WeaknessType.DOUBLES ->
                "Сегодня у нас даблы. В последнее время они у тебя немного подсели — но ничего, мы их быстренько подтянем."
            WeaknessType.SCORE ->
                "Сегодня фокус на набор очков. В последнее время он немного просел — подтянем, и ты снова будешь набирать легко."
            WeaknessType.ACCURACY ->
                "Сегодня работаем над точностью в сектор. Она немного подсела — но это дело наживное, быстро вернём форму."
            WeaknessType.TREBLES ->
                "Сегодня тренируем утроения. В последнее время они немного подсели — но ничего, подтянем."
        }
    }

    /**
     * Мотивация при росте параметра.
     */
    fun buildGrowthHint(
        playerName: String,
        weak: WeaknessType,
        sessionsToNextLevel: Int
    ): String {
        val paramName = when (weak) {
            WeaknessType.DOUBLES -> "удвоения"
            WeaknessType.SCORE -> "набор очков"
            WeaknessType.ACCURACY -> "точность в сектор"
            WeaknessType.TREBLES -> "утроения"
        }
        return "$playerName, отличный результат! Три тренировки подряд растут $paramName. " +
                "Если продолжишь в том же темпе — через $sessionsToNextLevel тренировок выйдешь на новый уровень."
    }

    /**
     * Простая похвала за хорошую игру.
     */
    fun buildPraise(playerName: String, value: Float): String {
        return "$playerName, сегодня отличный результат — $value. Так держать!"
    }

    /**
     * Сообщение после экзамена.
     */
    fun buildExamMessage(
        playerName: String,
        oldLevel: Int,
        newLevel: Int,
        potential: Int,
        weakestType: WeaknessType?
    ): String {
        val oldName = DartsNorms.getLevelName(oldLevel)
        val newName = DartsNorms.getLevelName(newLevel)

        return when {
            newLevel > oldLevel -> {
                "🏆 $playerName, экзамен сдан!\n\n" +
                        "Ты поднялся с уровня $oldLevel ($oldName) на уровень $newLevel ($newName)!\n\n" +
                        "Потенциал: $potential."
            }
            newLevel == oldLevel -> {
                "✅ $playerName, экзамен сдан!\n\n" +
                        "Твой уровень подтверждён: $newLevel ($newName).\n\n" +
                        (weakestType?.let {
                            "Потенциал: $potential. Тянут вниз: ${DartsNorms.weaknessName(it).lowercase()}. " +
                                    "Подтяни их — и выйдешь на уровень $potential."
                        } ?: "Продолжай тренироваться в том же духе!")
            }
            else -> {
                "$playerName, экзамен сдан. Уровень пока $newLevel ($newName).\n\n" +
                        "Не переживай — если что-то просело, мы это подтянем. " +
                        "Потенциал у тебя $potential — значит, есть куда расти."
            }
        }
    }

    /**
     * Сообщение о расхождении комплексов (психология).
     */
    fun buildDiscrepancyMessage(playerName: String, discrepancy: Discrepancy): String {
        return when (discrepancy) {
            Discrepancy.PPR_LOWER ->
                "$playerName, в тренировке ты показываешь высокий уровень, а в игре 501 — ниже. " +
                        "Это не про технику — это про давление. Поработаем над психологией."
            Discrepancy.PPR_HIGHER ->
                "$playerName, в игре 501 ты раскрываешься лучше, чем в упражнениях. " +
                        "Похоже, упражнения тебе даются скучнее — попробуем сделать их интереснее."
            Discrepancy.NONE -> ""
        }
    }
}
