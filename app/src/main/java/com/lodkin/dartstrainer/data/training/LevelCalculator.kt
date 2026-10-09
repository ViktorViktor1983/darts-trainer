package com.lodkin.dartstrainer.data.training

import kotlin.math.ceil

/**
 * Расчёт уровня, потенциала и трендов игрока.
 *
 * Логика:
 *   • Уровень = среднее трёх параметров, округление вверх.
 *   • Потенциал = максимум из трёх параметров.
 *   • Слабый параметр = минимум из трёх.
 *   • Тренды считаем по 3 последним тренировкам: растёт / падает / стоит.
 *
 * Уровень пересчитывается ПОСЛЕ экзамена (раз в 10 тренировок).
 * Внутри 10-тренировочного цикла — только тренды и корректировка плана.
 */
object LevelCalculator {

    /**
     * Результат расчёта уровня.
     */
    data class LevelResult(
        val level: Int,                 // Итоговый уровень (1..16)
        val potential: Int,             // Потенциал — уровень по лучшему параметру
        val weakestType: WeaknessType?, // Что тянет вниз (может быть null, если данных мало)
        val levelByScore: Int,          // Уровень по набору очков (0 = нет данных)
        val levelByAccuracy: Int,       // Уровень по точности в сектор
        val levelByDoubles: Int         // Уровень по удвоениям
    )

    /**
     * Рассчитать уровень по средним значениям трёх параметров за 10 тренировок.
     *
     * @param avgScore     средний результат по набору очков (Сектор 20 или Набор очков). null — нет данных.
     * @param avgAccuracy  средний результат по Большому раунду. null — нет данных.
     * @param avgDoubles   средний процент попадания в удвоения. null — нет данных.
     */
    fun calculateLevel(
        avgScore: Int?,
        avgAccuracy: Int?,
        avgDoubles: Float?
    ): LevelResult {

        // Уровень по каждому параметру (если данных нет — 0)
        val levelByScore = avgScore?.let { DartsNorms.levelBySector20(it) } ?: 0
        val levelByAccuracy = avgAccuracy?.let { DartsNorms.levelByBigRound(it) } ?: 0
        val levelByDoubles = avgDoubles?.let { DartsNorms.levelByDoubles(it) } ?: 0

        // Собираем только те, что есть
        val validLevels = mutableListOf<Int>()
        if (levelByScore > 0) validLevels.add(levelByScore)
        if (levelByAccuracy > 0) validLevels.add(levelByAccuracy)
        if (levelByDoubles > 0) validLevels.add(levelByDoubles)

        if (validLevels.isEmpty()) {
            return LevelResult(
                level = 1, potential = 1, weakestType = null,
                levelByScore = 0, levelByAccuracy = 0, levelByDoubles = 0
            )
        }

        // Среднее, округление вверх
        val sum = validLevels.sum()
        val avg = sum.toDouble() / validLevels.size
        val finalLevel = ceil(avg).toInt().coerceIn(1, DartsNorms.totalLevels())

        // Потенциал = максимум
        val potential = validLevels.maxOrNull() ?: finalLevel

        // Слабый параметр = тот, у которого уровень минимальный
        val minLevel = validLevels.minOrNull() ?: finalLevel
        val weakestType = when {
            levelByScore > 0 && levelByScore == minLevel -> WeaknessType.SCORE
            levelByAccuracy > 0 && levelByAccuracy == minLevel -> WeaknessType.ACCURACY
            levelByDoubles > 0 && levelByDoubles == minLevel -> WeaknessType.DOUBLES
            else -> null
        }

        return LevelResult(
            level = finalLevel,
            potential = potential,
            weakestType = weakestType,
            levelByScore = levelByScore,
            levelByAccuracy = levelByAccuracy,
            levelByDoubles = levelByDoubles
        )
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
        }
    }

    /**
     * Мотивация при росте параметра.
     * Показываем игроку, что он растёт и близок к повышению.
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
        }
        return "$playerName, отличный результат! Три тренировки подряд растут $paramName. " +
                "Если продолжишь в том же темпе — через $sessionsToNextLevel тренировок выйдешь на новый уровень."
    }

    /**
     * Простая похвала за хорошую игру.
     * Без обещания уровня — просто мотивация.
     */
    fun buildPraise(playerName: String, value: Float): String {
        return "$playerName, сегодня отличный результат — $value. Так держать!"
    }

    /**
     * Сообщение после экзамена — когда уровень подтверждён или изменён.
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
                // Понижение — мягко
                "$playerName, экзамен сдан. Уровень пока $newLevel ($newName).\n\n" +
                        "Не переживай — если что-то просело, мы это подтянем. " +
                        "Потенциал у тебя $potential — значит, есть куда расти."
            }
        }
    }
}
