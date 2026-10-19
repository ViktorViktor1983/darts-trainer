package com.lodkin.dartstrainer.data.training

import kotlin.math.abs
import kotlin.math.ceil
import kotlin.math.roundToInt

/**
 * Анализатор накопленных данных тренировок.
 *
 * Считает два комплекса:
 *   • Комплекс 1 — упражнения (набор, точность, удвоения, утроения).
 *   • Комплекс 2 — игры (501 PPR и Крикет MPR).
 *
 * ВАЖНО: для кругосветок score = общее количество дротиков.
 * Для Сектор 20 / Набор / Большой раунд score = очки.
 *
 * Дополнительно (с версии «Статистика по секторам»):
 *   • парсит detailsJson Кругосветок (Сектора, Удвоения, Утроения)
 *     формата "1:2,2:1,3:4" (сектор : дротиков_потрачено);
 *   • считает средний процент попаданий по каждому сектору;
 *   • сравнивает его с общим средним и возвращает топ-3 худших;
 *   • формирует список целей для точечной работы (targetPracticeGoals);
 *   • проверяет, было ли уже упражнение "targeted_practice" в этом периоде;
 *   • считает прогресс накопления статистики (от 0 до 100%) по каждому
 *     из 8 упражнений и общий прогресс. Экзамен доступен, когда все 8 по 100%.
 */
object TrainingStatsAnalyzer {

    private const val RECENT_LIMIT = 5
    private const val AROUND_CLOCK_TARGETS = 21

    /** Минимум записей (завершённых Кругосветок), чтобы сектор попал в отчёт. */
    private const val MIN_SECTOR_ENTRIES = 5

    /** Сколько худших секторов максимум показываем. */
    private const val WORST_SECTORS_TOP = 3

    /**
     * Минимальное отклонение от среднего (в процентах), при котором
     * сектор считается проблемным и попадает в отчёт.
     */
    private const val MIN_DEVIATION_PERCENT = 25f

    /** Цель по количеству записей для каждого упражнения для открытия экзамена. */
    private const val EXERCISE_GOAL = 5

    /**
     * Один проблемный сектор.
     */
    data class SectorWeakness(
        val sector: Int,
        val percent: Float,
        val deviation: Float,
        val count: Int
    )

    /**
     * Прогресс накопления данных по одному упражнению.
     */
    data class ExerciseProgress(
        val id: String,
        val title: String,
        val count: Int,
        val goal: Int
    ) {
        val percent: Float
            get() = (count.toFloat() / goal).coerceAtMost(1f)
    }

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
        val levelBySectors: Int,
        val levelByDoubles: Int,
        val levelByTrebles: Int,

        // Уровни по играм (комплекс 2)
        val level501: Int,
        val levelCricket: Int,

        // Два комплекса
        val complex1Level: Int,
        val complex2Level: Int,

        // Итоговый уровень
        val level: Int,
        val potential: Int,

        // Флаги расхождения
        val gamesLagBehind: Boolean,
        val exercisesLagBehind: Boolean,

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

        // Топ худших секторов (только проблемные, отклонение > 25%)
        val worstSectors: List<SectorWeakness>,
        val sectorsDataAvailable: Boolean,

        // Топ худших удвоений
        val worstDoubles: List<SectorWeakness>,
        val doublesDataAvailable: Boolean,

        // Топ худших утроений
        val worstTrebles: List<SectorWeakness>,
        val treblesDataAvailable: Boolean,

        // Список целей для точечной работы (например, ["S13", "D7", "T11"])
        val targetPracticeGoals: List<String>,

        // Было ли уже выполнено упражнение "targeted_practice" в этом периоде
        val hasTargetedPracticeThisPeriod: Boolean,

        // Прогресс накопления данных по упражнениям (для экзамена)
        val exercisesProgress: List<ExerciseProgress>,
        val overallProgress: Float,
        val isReadyForNormatives: Boolean,

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

        // ── Все записи Кругосветок (для детальной статистики) ──
        val allSectorsResults = repository.getAllExerciseResultsByExerciseId("aroundclock_sectors")
        val allDoublesResults = repository.getAllExerciseResultsByExerciseId("aroundclock_doubles")
        val allTreblesResults = repository.getAllExerciseResultsByExerciseId("aroundclock_trebles")

        // ── Проверяем, было ли упражнение "targeted_practice" в этом периоде ──
        val allTargetedResults = repository.getAllExerciseResultsByExerciseId("targeted_practice")
        val hasTargetedPractice = allTargetedResults.isNotEmpty()

        // ── Считаем прогресс накопления данных по 8 упражнениям ──
        val progressList = listOf(
            ExerciseProgress("game501", "501", all501Results.size, EXERCISE_GOAL),
            ExerciseProgress("cricket", "Крикет", allCricketResults.size, EXERCISE_GOAL),
            ExerciseProgress("aroundclock_sectors", "Кругосветка: Сектора", allSectorsResults.size, EXERCISE_GOAL),
            ExerciseProgress("aroundclock_doubles", "Кругосветка: Удвоения", allDoublesResults.size, EXERCISE_GOAL),
            ExerciseProgress("aroundclock_trebles", "Кругосветка: Утроения", allTreblesResults.size, EXERCISE_GOAL),
            ExerciseProgress("sector20", "Сектор 20", sector20Results.size, EXERCISE_GOAL),
            ExerciseProgress("biground", "Большой раунд", bigroundResults.size, EXERCISE_GOAL),
            ExerciseProgress("scoreset", "Набор очков", scoresetResults.size, EXERCISE_GOAL)
        )
        val overallProgress = progressList.map { it.percent }.average().toFloat()
        val isReadyForNormatives = progressList.all { it.count >= EXERCISE_GOAL }

        // ── Средний набор очков (Сектор 20 + Набор) ──
        val scoreValues = mutableListOf<Int>()
        sector20Results.forEach { scoreValues.add(it.score) }
        scoresetResults.forEach { scoreValues.add(it.score) }
        val avgScore = if (scoreValues.isEmpty()) null else scoreValues.average().roundToInt()

        // ── Большой раунд (оставляем для справки, но в уровень не идёт) ──
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

        // ── Точность в сектор (Кругосветка-Сектора) ──
        val avgSectorsPercent = if (aroundSectorsResults.isEmpty()) null else {
            val avgDarts = aroundSectorsResults.map { it.score }.average()
            if (avgDarts <= 0.0) null
            else ((AROUND_CLOCK_TARGETS / avgDarts) * 100.0).toFloat()
        }

        // ── Уровни по параметрам (комплекс 1) ──
        val levelByScore = avgScore?.let { DartsNorms.levelBySector20(it) } ?: 0
        val levelByBigRound = avgBigRound?.let { DartsNorms.levelByBigRound(it) } ?: 0
        val levelBySectors = avgSectorsPercent?.let { DartsNorms.levelBySectorsPercent(it) } ?: 0
        val levelByDoubles = avgDoublesPercent?.let { DartsNorms.levelByDoubles(it) } ?: 0
        val levelByTrebles = avgTreblesPercent?.let { DartsNorms.levelByTrebles(it) } ?: 0

        val exerciseLevels = mutableListOf<Int>()
        if (levelByScore > 0) exerciseLevels.add(levelByScore)
        if (levelBySectors > 0) exerciseLevels.add(levelBySectors)
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

        // ── Итоговый уровень ──
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
            if (levelBySectors > 0) candidates.add(WeaknessType.ACCURACY to levelBySectors)
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

        // ── Проблемные сектора ──
        val sectorsReport = computeWorstSectors(allSectorsResults)
        val doublesReport = computeWorstSectors(allDoublesResults)
        val treblesReport = computeWorstSectors(allTreblesResults)

        // ── Сборка списка целей для точечной работы ──
        val goals = mutableListOf<String>()
        
        sectorsReport.problemSectors.take(3).forEach { 
            goals.add(if (it.sector == 25) "Bull" else "S${it.sector}")
        }
        doublesReport.problemSectors.take(3).forEach { 
            goals.add(if (it.sector == 25) "D-Bull" else "D${it.sector}")
        }
        treblesReport.problemSectors.take(3).forEach { 
            goals.add(if (it.sector == 25) "T-Bull" else "T${it.sector}")
        }

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
            levelBySectors = levelBySectors,
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
            worstSectors = sectorsReport.problemSectors,
            sectorsDataAvailable = sectorsReport.dataAvailable,
            worstDoubles = doublesReport.problemSectors,
            doublesDataAvailable = doublesReport.dataAvailable,
            worstTrebles = treblesReport.problemSectors,
            treblesDataAvailable = treblesReport.dataAvailable,
            targetPracticeGoals = goals,
            hasTargetedPracticeThisPeriod = hasTargetedPractice,
            exercisesProgress = progressList,
            overallProgress = overallProgress,
            isReadyForNormatives = isReadyForNormatives,
            dataPointsCount = count,
            hasEnoughData = hasEnough
        )
    }

    private data class SectorsReport(
        val problemSectors: List<SectorWeakness>,
        val dataAvailable: Boolean
    )

    private fun computeWorstSectors(
        allSectors: List<ExerciseResult>
    ): SectorsReport {
        if (allSectors.isEmpty()) return SectorsReport(emptyList(), false)

        val sumDarts = mutableMapOf<Int, Int>()
        val countSector = mutableMapOf<Int, Int>()

        allSectors.forEach { r ->
            val parsed = parseSectorDetails(r.detailsJson)
            if (parsed.isEmpty()) return@forEach
            parsed.forEach { (sector, darts) ->
                if (darts > 0) {
                    sumDarts[sector] = (sumDarts[sector] ?: 0) + darts
                    countSector[sector] = (countSector[sector] ?: 0) + 1
                }
            }
        }

        val filtered = sumDarts.keys.filter { (countSector[it] ?: 0) >= MIN_SECTOR_ENTRIES }
        if (filtered.isEmpty()) return SectorsReport(emptyList(), false)

        val totalCount = filtered.sumOf { countSector[it] ?: 0 }
        val totalDarts = filtered.sumOf { sumDarts[it] ?: 0 }
        if (totalDarts <= 0 || totalCount <= 0) return SectorsReport(emptyList(), false)
        val generalPercent = totalCount.toDouble() / totalDarts.toDouble() * 100.0

        val problems = filtered
            .map { sector ->
                val c = countSector[sector] ?: 0
                val d = sumDarts[sector] ?: 0
                val sectorPercent = if (d > 0) c.toDouble() / d.toDouble() * 100.0 else 0.0
                val deviation = if (generalPercent > 0.0) {
                    (generalPercent - sectorPercent) / generalPercent * 100.0
                } else 0.0
                SectorWeakness(
                    sector = sector,
                    percent = sectorPercent.toFloat(),
                    deviation = deviation.toFloat(),
                    count = c
                )
            }
            .filter { it.deviation > MIN_DEVIATION_PERCENT }
            .sortedByDescending { it.deviation }
            .take(WORST_SECTORS_TOP)

        return SectorsReport(problems, true)
    }

    private fun parseSectorDetails(s: String): Map<Int, Int> {
        if (s.isBlank()) return emptyMap()
        val result = mutableMapOf<Int, Int>()
        s.split(",").forEach { entry ->
            val parts = entry.trim().split(":", limit = 2)
            if (parts.size == 2) {
                val sector = parts[0].trim().toIntOrNull()
                val darts = parts[1].trim().toIntOrNull()
                if (sector != null && darts != null) {
                    result[sector] = (result[sector] ?: 0) + darts
                }
            }
        }
        return result
    }

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

    fun tempoFor(analysis: TrainingAnalysis, exerciseId: String, fallbackMinutes: Int): Int {
        val tempo = analysis.avgMinutesByExercise[exerciseId] ?: return fallbackMinutes
        return tempo.roundToInt().coerceAtLeast(1)
    }
}
