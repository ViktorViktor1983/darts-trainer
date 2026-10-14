package com.lodkin.dartstrainer.data.training

import kotlin.math.roundToInt

/**
 * Анализатор накопленных данных тренировок.
 *
 * Читает результаты упражнений из БД, вычисляет:
 *   • Средние значения по 4 параметрам (набор, точность, удвоения, утроения).
 *   • Уровень игрока по этим параметрам.
 *   • Слабое место (через WeaknessDetector).
 *   • Средний темп по каждому упражнению (по последним 5 записям).
 *   • Средние показатели по 501 (PPR, % удвоений) — для главного экрана.
 *
 * ВАЖНО: для кругосветок score = общее количество дротиков (чем меньше — тем лучше).
 * Для Сектор 20 / Набор / Большой раунд score = очки (чем больше — тем лучше).
 */
object TrainingStatsAnalyzer {

    /** Сколько последних записей берём для расчёта. */
    private const val RECENT_LIMIT = 5

    /** Сколько целей в кругосветке (для перевода дротиков в процент попаданий). */
    private const val AROUND_CLOCK_TARGETS = 21

    /**
     * Сводный результат анализа.
     */
    data class TrainingAnalysis(
        // Средние значения по параметрам
        val avgScore: Int?,
        val avgBigRound: Int?,
        val avgDoublesPercent: Float?,
        val avgTreblesPercent: Float?,
        val avgSectorsPercent: Float?,

        // Уровни
        val levelByScore: Int,
        val levelByBigRound: Int,
        val level: Int,
        val potential: Int,
        val weakestType: WeaknessType?,

        // Средний темп по упражнениям (в минутах)
        val avgMinutesByExercise: Map<String, Float>,

        // Средние показатели 501 (для главного экрана)
        val avg501Ppr: Float?,
        val avg501DoublesPercent: Float?,
        val count501Matches: Int,

        // Сколько записей было обработано
        val dataPointsCount: Int,

        // Данных достаточно для построения программы?
        val hasEnoughData: Boolean
    )

    /**
     * Провести анализ по накопленным данным.
     */
    suspend fun analyze(repository: TrainingRepository): TrainingAnalysis {
        // ── Считываем последние 5 записей по каждому упражнению ──
        val sector20Results = repository.getLatestExerciseResults("sector20", RECENT_LIMIT)
        val scoresetResults = repository.getLatestExerciseResults("scoreset", RECENT_LIMIT)
        val bigroundResults = repository.getLatestExerciseResults("biground", RECENT_LIMIT)
        val aroundDoublesResults = repository.getLatestExerciseResults("aroundclock_doubles", RECENT_LIMIT)
        val aroundTreblesResults = repository.getLatestExerciseResults("aroundclock_trebles", RECENT_LIMIT)
        val aroundSectorsResults = repository.getLatestExerciseResults("aroundclock_sectors", RECENT_LIMIT)

        // ── Все партии 501 — для средних на главном экране ──
        val all501Results = repository.getAllExerciseResultsByExerciseId("game501")

        // ── Средний набор очков (объединяем sector20 и scoreset) ──
        val scoreValues = mutableListOf<Int>()
        sector20Results.forEach { scoreValues.add(it.score) }
        scoresetResults.forEach { scoreValues.add(it.score) }
        val avgScore = if (scoreValues.isEmpty()) null else scoreValues.average().roundToInt()

        // ── Средний Большой раунд ──
        val avgBigRound = if (bigroundResults.isEmpty()) null
            else bigroundResults.map { it.score }.average().roundToInt()

        // ── Удвоения (из Кругосветки-Удвоения) ──
        val avgDoublesPercent = if (aroundDoublesResults.isEmpty()) null else {
            val avgDarts = aroundDoublesResults.map { it.score }.average()
            if (avgDarts <= 0.0) null
            else ((AROUND_CLOCK_TARGETS / avgDarts) * 100.0).toFloat()
        }

        // ── Утроения (из Кругосветки-Утроения) ──
        val avgTreblesPercent = if (aroundTreblesResults.isEmpty()) null else {
            val avgDarts = aroundTreblesResults.map { it.score }.average()
            if (avgDarts <= 0.0) null
            else ((AROUND_CLOCK_TARGETS / avgDarts) * 100.0).toFloat()
        }

        // ── Точность в сектор (из Кругосветки-Сектора) ──
        val avgSectorsPercent = if (aroundSectorsResults.isEmpty()) null else {
            val avgDarts = aroundSectorsResults.map { it.score }.average()
            if (avgDarts <= 0.0) null
            else ((AROUND_CLOCK_TARGETS / avgDarts) * 100.0).toFloat()
        }

        // ── Уровни ──
        val levelByScore = avgScore?.let { DartsNorms.levelBySector20(it) } ?: 0
        val levelByBigRound = avgBigRound?.let { DartsNorms.levelByBigRound(it) } ?: 0

        val validLevels = mutableListOf<Int>()
        if (levelByScore > 0) validLevels.add(levelByScore)
        if (levelByBigRound > 0) validLevels.add(levelByBigRound)

        val level = if (validLevels.isEmpty()) 1 else {
            val avg = validLevels.sum().toDouble() / validLevels.size
            kotlin.math.ceil(avg).toInt().coerceIn(1, DartsNorms.totalLevels())
        }
        val potential = validLevels.maxOrNull() ?: level

        // ── Слабое место ──
        val weakestType: WeaknessType? = run {
            val candidates = mutableListOf<Pair<WeaknessType, Int>>()
            if (levelByScore > 0) candidates.add(WeaknessType.SCORE to levelByScore)
            if (levelByBigRound > 0) candidates.add(WeaknessType.ACCURACY to levelByBigRound)
            candidates.minByOrNull { it.second }?.first
        }

        // ── Средний темп по каждому упражнению (в минутах) ──
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

        // ── Средние показатели 501 (PPR, % удвоений) ──
        val (avg501Ppr, avg501Doubles, count501) = compute501Averages(all501Results)

        // ── Общее количество данных ──
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
            level = level,
            potential = potential,
            weakestType = weakestType,
            avgMinutesByExercise = avgMinutesByExercise,
            avg501Ppr = avg501Ppr,
            avg501DoublesPercent = avg501Doubles,
            count501Matches = count501,
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

    private fun parseDetails(s: String): Map<String, String> {
        if (s.isBlank()) return emptyMap()
        return s.split(";").mapNotNull { entry ->
            val parts = entry.split("=")
            if (parts.size == 2) parts[0].trim() to parts[1].trim() else null
        }.toMap()
    }

    /**
     * Получить средний темп упражнения в минутах.
     * Если данных нет — вернуть fallback (стандартное время).
     */
    fun tempoFor(analysis: TrainingAnalysis, exerciseId: String, fallbackMinutes: Int): Int {
        val tempo = analysis.avgMinutesByExercise[exerciseId] ?: return fallbackMinutes
        return tempo.roundToInt().coerceAtLeast(1)
    }
}
