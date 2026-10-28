package com.lodkin.dartstrainer.data.training

import kotlin.math.ceil
import kotlin.math.roundToInt

/**
 * Расчёт уровня, потенциала и трендов игрока.
 */
object LevelCalculator {

    // ─────────────────────────────────────────────
    // ЛОГИКА ЭКЗАМЕНА (5 ДИСЦИПЛИН)
    // ─────────────────────────────────────────────

    /**
     * Рассчитать уровень ИСКЛЮЧИТЕЛЬНО по результатам экзамена (5 дисциплин).
     * 
     * @param ppr Средний PPR в 501 за экзамен
     * @param mpr Средний MPR в Крикете за экзамен
     * @param score20 Очки в Секторе 20
     * @param scoreBR Очки в Большом раунде
     * @param scoreSS Очки в Наборе очков
     * @return Итоговый уровень (1..16)
     */
    fun calculateExamLevel(
        ppr: Float,
        mpr: Float,
        score20: Int,
        scoreBR: Int,
        scoreSS: Int
    ): Int {
        // 1. Уровни по играм
        val lvl501 = if (ppr > 0f) DartsNorms.levelByPpr(ppr) else 1
        val lvlCricket = if (mpr > 0f) DartsNorms.levelByMpr(mpr) else 1
        
        // 2. Уровни по нормативам
        val lvlS20 = if (score20 > 0) DartsNorms.levelBySector20(score20) else 1
        val lvlBR = if (scoreBR > 0) DartsNorms.levelByBigRound(scoreBR) else 1
        val lvlSS = if (scoreSS > 0) DartsNorms.levelByScoreSet(scoreSS) else 1

        // 3. Средний уровень по играм (округляем по правилам математики)
        val avgGames = ((lvl501 + lvlCricket) / 2.0).roundToInt()

        // 4. Средний уровень по нормативам (округляем по правилам математики)
        val avgNorms = ((lvlS20 + lvlBR + lvlSS) / 3.0).roundToInt()

        // 5. Итоговый уровень (округляем по правилам математики)
        val finalLevel = ((avgGames + avgNorms) / 2.0).roundToInt()

        return finalLevel.coerceIn(1, DartsNorms.totalLevels())
    }

    // ─────────────────────────────────────────────
    // СТАРАЯ ЛОГИКА (ДЛЯ ТРЕНИРОВОК)
    // ─────────────────────────────────────────────

    data class LevelResult(
        val level: Int,
        val potential: Int,
        val weakestType: WeaknessType?,
        val levelByScore: Int,
        val levelByAccuracy: Int,
        val levelByDoubles: Int,
        val levelByTrebles: Int,
        val levelByPpr: Int
    )

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

        val sum = validLevels.sum()
        val avg = sum.toDouble() / validLevels.size
        val finalLevel = ceil(avg).toInt().coerceIn(1, DartsNorms.totalLevels())

        val potential = validLevels.maxOrNull() ?: finalLevel

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

    fun detectDiscrepancy(exerciseResult: LevelResult, pprLevel: Int): Discrepancy {
        if (pprLevel == 0) return Discrepancy.NONE
        val diff = pprLevel - exerciseResult.level
        return when {
            diff <= -2 -> Discrepancy.PPR_LOWER
            diff >= 2 -> Discrepancy.PPR_HIGHER
            else -> Discrepancy.NONE
        }
    }

    enum class Discrepancy {
        NONE, PPR_LOWER, PPR_HIGHER
    }

    enum class Trend {
        RISING, FALLING, STABLE
    }

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

    fun buildDeclineHint(weak: WeaknessType): String {
        return when (weak) {
            WeaknessType.DOUBLES -> "Сегодня у нас даблы. В последнее время они у тебя немного подсели — но ничего, мы их быстренько подтянем."
            WeaknessType.SCORE -> "Сегодня фокус на набор очков. В последнее время он немного просел — подтянем, и ты снова будешь набирать легко."
            WeaknessType.ACCURACY -> "Сегодня работаем над точностью в сектор. Она немного подсела — но это дело наживное, быстро вернём форму."
            WeaknessType.TREBLES -> "Сегодня тренируем утроения. В последнее время они немного подсели — но ничего, подтянем."
        }
    }

    fun buildGrowthHint(playerName: String, weak: WeaknessType, sessionsToNextLevel: Int): String {
        val paramName = when (weak) {
            WeaknessType.DOUBLES -> "удвоения"
            WeaknessType.SCORE -> "набор очков"
            WeaknessType.ACCURACY -> "точность в сектор"
            WeaknessType.TREBLES -> "утроения"
        }
        return "$playerName, отличный результат! Три тренировки подряд растут $paramName. " +
                "Если продолжишь в том же темпе — через $sessionsToNextLevel тренировок выйдешь на новый уровень."
    }

    fun buildPraise(playerName: String, value: Float): String {
        return "$playerName, сегодня отличный результат — $value. Так держать!"
    }

    fun buildExamMessage(playerName: String, oldLevel: Int, newLevel: Int, potential: Int, weakestType: WeaknessType?): String {
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

    fun buildDiscrepancyMessage(playerName: String, discrepancy: Discrepancy): String {
        return when (discrepancy) {
            Discrepancy.PPR_LOWER -> "$playerName, в тренировке ты показываешь высокий уровень, а в игре 501 — ниже. " +
                    "Это не про технику — это про давление. Поработаем над психологией."
            Discrepancy.PPR_HIGHER -> "$playerName, в игре 501 ты раскрываешься лучше, чем в упражнениях. " +
                    "Похоже, упражнения тебе даются скучнее — попробуем сделать их интереснее."
            Discrepancy.NONE -> ""
        }
    }
}
