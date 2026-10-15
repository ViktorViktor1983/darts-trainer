package com.lodkin.dartstrainer.data.training

import kotlin.math.abs
import kotlin.math.ceil
import kotlin.math.roundToInt

/**
 * Анализатор накопленных данных тренировок.
 *
 * Читает результаты упражнений из БД, вычисляет:
 *   • Средние значения по 4 параметрам (набор, точность, удвоения, утроения).
 *   • Средние показатели 501 (PPR) и Крикета (MPR).
 *   • Два комплекса уровня:
 *       - Комплекс 1 — упражнения (среднее 4 параметров).
 *       - Комплекс 2 — игры (среднее 501 PPR и Крикета MPR).
 *   • Итоговый уровень — максимум из двух.
 *   • Флаги расхождения: недотягивают игры / недотягивают упражнения.
 *   • Слабое место.
 *   • Средний темп по каждому упражнению (по последним 5 записям).
 *
 * ВАЖНО: для кругосветок score = общее количество дротиков (чем меньше — тем лучше).
 * Для Сектор 20 / Набор / Большой раунд score = очки (чем больше — тем лучше).
 */
object TrainingStatsAnalyzer {

    private const val RECENT_LIMIT = 5
    private const val AROUND_CLOCK_TARGETS = 21

    /**
     * Сводный результат анализа.
     */
    data class TrainingAnalysis(
        // Средние значения по 4 параметрам
        val avgScore: Int?,
        val avgBigRound: Int?,
        val avgDoublesPercent: Float?,
        val avgTreblesPercent: Float?,
        val avgSectorsPercent: Float?,

        // Уровни по параметрам (комплекс 1)
        val levelByScore: Int,
        val levelByBigRound: Int,
        val levelByDoubles: Int,
        val levelByTrebles: Int,

        // Уровни по играм (комплекс 2)
        val level501: Int,
        val levelCricket: Int,

        // Два комплекса
        val complex1Level: Int,        // уровень по упражнениям
        val complex2Level: Int,        // уровень по играм

        // Итоговый уровень (максимум из двух комплексов)
        val level: Int,
        val potential: Int,

        // Флаги расхождения — какой комплекс отстаёт
        val gamesLagBehind: Boolean,      // игры отстают от упражнений
        val exercisesLagBehind: Boolean,  // упражнения отстают от игр

        val weakestType: WeaknessType?,

        // Темп по упражнениям
        val avgMinutesByExercise: Map<String, Float>,

        // Средние по 501
        val avg501Ppr: Float?,
        val avg501DoublesPercent: Float?,
        val count501Matches: Int,

        // Средние по Крикету
        val avgCricketMpr: Float?,
        val countCricketMatches: Int,

        val dataPointsCount: Int,
        val hasEnoughData: Boolean
    )

    suspend fun analyze(repository: TrainingRepository): TrainingAnalysis {
        // ── Последние 5 записей по каждому упражнению ──
        val sector20Results = repository.getLatestExerciseResults("sector20", RECENT_LIMIT)
        val scoresetResults = repository.getLatestExerciseResults("scoreset", RECENT_LIMIT)
        val bigroundResults = repository.getLatestExerciseResults("biground", RECENT_LIMIT)
        val aroundDoublesResults = repository.getLatestExerciseResults("aroundclock_doubles", RECENT_LIMIT)
        val aroundTreblesResults = repository.getLatestExerciseResults("aroundclock_trebles", RECENT_LIMIT)
        val aroundSectorsResults = repository.getLatestExerciseResults("aroundclock_sectors", RECENT_LIMIT)

        // ── Все партии 501 и Крикета ──
        val all501Results = repository.getAllExerciseResultsByExerciseId("game501")
        val allCricketResults = repository.getAllExerciseResultsByExerciseId("cricket")

        // ── Средний набор очков (Сектор 20 + Набор) ──
        val scoreValues = mutableListOf<Int>()
        sector20Results.forEach { scoreValues.add(it.score) }
        scoresetResults.forEach { scoreValues.add(it.score) }
        val avgScore = if (scoreValues.isEmpty()) null else scoreValues.average().roundToInt()

        // ── Большой раунд ──
        val avgBigRound = if (bigroundResults.isEmpty()) null
            else bigroundResults.map { it.score }.average().roundToInt()

        // ── Удвоения ──
        val avgDoublesPercent = if (aroundDoublesResults.isEmpty()) null else {
            val avgDarts = aroundDoublesResults.map { it.score }.average()
            if (avgDarts <= 0.0) null
            else ((AROUND_CLOCK_TARGETS / avgDarts) * 100.0).toFloat()
        }

        // ── Утроения ──
        val avgTreblesPercent = if (aroundTreblesResults.isEmpty()) null else {
            val avgDarts = aroundTreblesResults.map { it.score }.average()
            if (avgDarts <= 0.0) null
            else ((AROUND_CLOCK_TARGETS / avgDarts) * 100.0).toFloat()
        }

        // ── Точность в сектор ──
        val avgSectorsPercent = if (aroundSectorsResults.isEmpty()) null else {
            val avgDarts = aroundSectorsResults.map { it.score }.average()
            if (avgDarts <= 0.0) null
            else ((AROUND_CLOCK_TARGETS / avgDarts) * 100.0).toFloat()
        }

        // ── Уровни по параметрам (комплекс 1) ──
        val levelByScore = avgScore?.let { DartsNorms.levelBySector20(it) } ?: 0
        val levelByBigRound = avgBigRound?.let { DartsNorms.levelByBigRound(it) } ?: 0
        val levelByDoubles = avgDoublesPercent?.let { DartsNorms.levelByDoubles(it) } ?: 0
        val levelByTrebles = avgTreblesPercent?.let { DartsNorms.levelByTrebles(it) } ?: 0

        val exerciseLevels = mutableListOf<Int>()
        if (levelByScore > 0) exerciseLevels.add(levelByScore)
        if (levelByBigRound > 0) exerciseLevels.add(levelByBigRound)
        if (levelByDoubles > 0) exerciseLevels.add(levelByDoubles)
        if (levelByTrebles > 0) exerciseLevels.add(levelByTrebles)

        val complex1Level = if (exerciseLevels.isEmpty()) 0 else {
            ceil(exerciseLevels.sum().toDouble() / exerciseLevels.size).toInt()
                .coerceIn(1, DartsNorms.totalLevels())
        }

        // ── Средние по 501 ──
        val (avg501Ppr, avg501Doubles, count501) = compute501Averages(all501Results)

        // ── Средние по Крикету ──
        val (avgCricketMpr, countCricket) = computeCricketAverages(allCricketResults)

        // ── Уровни по играм (комплекс 2) ──
        val level501 = avg501Ppr?.let { DartsNorms.levelByPpr(it) } ?: 0
        val levelCricket = avgCricketMpr?.let { DartsNorms.levelByMpr(it) } ?: 0

        val gameLevels = mutableListOf<Int>()
        if (level501 > 0) gameLevels.add(level501)
        if (levelCricket > 0) gameLevels.add(levelCricket)

        val complex2Level = if (gameLevels.isEmpty()) 0 else {
            ceil(gameLevels.sum().toDouble() / gameLevels.size).toInt()
                .coerceIn(1, DartsNorms.totalLevels())
        }

        // ── Итоговый уровень = максимум из двух ──
        val level = when {
            complex1Level == 0 && complex2Level == 0 -> 1
            complex1Level == 0 -> complex2Level
            complex2Level == 0 -> complex1Level
            else -> maxOf(complex1Level, complex2Level)
        }
        val potential = when {
            complex1Level == 0 && complex2Level == 0 -> 1
            else -> maxOf(complex1Level, complex2Level)
        }

        // ── Флаги расхождения ──
        val gamesLagBehind = complex1Level > 0 && complex2Level > 0 &&
                abs(complex1Level - complex2Level) >= 1 && complex2Level < complex1Level
        val exercisesLagBehind = complex1Level > 0 && complex2Level > 0 &&
                abs(complex1Level - complex2Level) >= 1 && complex1Level < complex2Level

        // ── Слабое место ──
        val weakestType: WeaknessType? = run {
            val candidates = mutableListOf<Pair<WeaknessType, Int>>()
            if (levelByScore > 0) candidates.add(WeaknessType.SCORE to levelByScore)
            if (levelByBigRound > 0) candidates.add(WeaknessType.ACCURACY to levelByBigRound)
            if (levelByDoubles > 0) candidates.add(WeaknessType.DOUBLES to levelByDoubles)
            if (levelByTrebles > 0) candidates.add(WeaknessType.TREBLES to levelByTrebles)
            candidates.minByOrNull { it.second }?.first
        }

        // ── Средний темп ──
        val avgMinutesByExercise = mutableMapOf<String, Float>()
        fun addTempo(exerciseId: String, list: List<ExerciseResult>) {
            val withDuration = list.filter { it.durationSeconds > 0 }
            if (withDuration.isNotEmpty()) {
                val avgSec = withDuration.map { it.durationSeconds }.average()
                avgMinutesByExercise[exerciseId] = (avgSec / 60.0).toFloat()
            }
        }
        addTempo("sector20", sector20Results)
        addTempo("scoreset", scoresetResults)
        addTempo("biground", bigroundResults)
        addTempo("aroundclock_doubles", aroundDoublesResults)
        addTempo("aroundclock_trebles", aroundTreblesResults)
        addTempo("aroundclock_sectors", aroundSectorsResults)

        // ── Общее количество данных (для блока «мало данных») ──
        val count = sector20Results.size + scoresetResults.size + bigroundResults.size +
                aroundDoublesResults.size + aroundTreblesResults.size + aroundSectorsResults.size
        val hasEnough = count >= 6

        return TrainingAnalysis(
            avgScore = avgScore,
            avgBigRound = avgBigRound,
            avgDoublesPercent = avgDoublesPercent,
            avgTreblesPercent = avgTreblesPercent,
            avgSectorsPercent = avgSectorsPercent,
            levelByScore = levelByScore,
            levelByBigRound = levelByBigRound,
            levelByDoubles = levelByDoubles,
            levelByTrebles = levelByTrebles,
            level501 = level501,
            levelCricket = levelCricket,
            complex1Level = complex1Level,
            complex2Level = complex2Level,
            level = level,
            potential = potential,
            gamesLagBehind = gamesLagBehind,
            exercisesLagBehind = exercisesLagBehind,
            weakestType = weakestType,
            avgMinutesByExercise = avgMinutesByExercise,
            avg501Ppr = avg501Ppr,
            avg501DoublesPercent = avg501Doubles,
            count501Matches = count501,
            avgCricketMpr = avgCricketMpr,
            countCricketMatches = countCricket,
            dataPointsCount = count,
            hasEnoughData = hasEnough
        )
    }

    /**
     * Средний PPR и % удвоений по партиям 501.
     * Парсит detailsJson: "won=X;ppr=Y;dbl=Z".
     */
    private fun compute501Averages(
        all501: List<ExerciseResult>
    ): Triple<Float?, Float?, Int> {
        val pprValues = mutableListOf<Float>()
        val dblValues = mutableListOf<Float>()

        all501.forEach { r ->
            val details = parseDetails(r.detailsJson)
            val ppr = details["ppr"]?.toFloatOrNull()
            val dbl = details["dbl"]?.toFloatOrNull()
            if (ppr != null && ppr > 0f) pprValues.add(ppr)
            if (dbl != null && dbl >= 0f) dblValues.add(dbl)
        }

        val avgPpr = if (pprValues.isEmpty()) null else pprValues.average().toFloat()
        val avgDbl = if (dblValues.isEmpty()) null else dblValues.average().toFloat()
        return Triple(avgPpr, avgDbl, pprValues.size)
    }

    /**
     * Средний MPR по партиям Крикета.
     * Парсит detailsJson: "won=X;mpr=Y".
     */
    private fun computeCricketAverages(
        allCricket: List<ExerciseResult>
    ): Pair<Float?, Int> {
        val mprValues = mutableListOf<Float>()

        allCricket.forEach { r ->
            val details = parseDetails(r.detailsJson)
            val mpr = details["mpr"]?.toFloatOrNull()
            if (mpr != null && mpr > 0f) mprValues.add(mpr)
        }

        val avgMpr = if (mprValues.isEmpty()) null else mprValues.average().toFloat()
        return avgMpr to mprValues.size
    }

    private fun parseDetails(s: String): Map<String, String> {
        if (s.isBlank()) return emptyMap()
        return s.split(";").mapNotNull { entry ->
            val parts = entry.split("=", limit = 2)
            if (parts.size == 2) parts[0].trim() to parts[1].trim() else null
        }.toMap()
    }

    /**
     * Получить средний темп упражнения в минутах.
     */
    fun tempoFor(analysis: TrainingAnalysis, exerciseId: String, fallbackMinutes: Int): Int {
        val tempo = analysis.avgMinutesByExercise[exerciseId] ?: return fallbackMinutes
        return tempo.roundToInt().coerceAtLeast(1)
    }
}
