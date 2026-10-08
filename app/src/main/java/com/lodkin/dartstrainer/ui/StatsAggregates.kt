package com.lodkin.dartstrainer.ui

import com.lodkin.dartstrainer.data.cricket.CricketGameEntity
import com.lodkin.dartstrainer.data.game501.Game501Entity
import java.util.Locale

// ─────────────────────────────────────────────
// Период фильтра
// ─────────────────────────────────────────────
enum class StatPeriod(val label: String, val daysBack: Long?) {
    WEEK("Неделя", 7L),
    MONTH("Месяц", 30L),
    THREE_MONTHS("3 мес", 90L),
    HALF_YEAR("Полгода", 180L),
    YEAR("Год", 365L),
    ALL("Всё время", null)
}

// ─────────────────────────────────────────────
// Вариант игры x01 для фильтра
// ─────────────────────────────────────────────
enum class GameVariant(
    val label: String,
    val shortLabel: String,
    val gameTypeName: String?,
    val outModeName: String?
) {
    ALL("Все x01", "Все x01", null, null),
    X501_DO("501 · Double Out", "501 DO", "X501", "DOUBLE_OUT"),
    X501_DIDO("501 · Double In/Out", "501 DI/DO", "X501", "DOUBLE_IN_OUT"),
    X301_DO("301 · Double Out", "301 DO", "X301", "DOUBLE_OUT"),
    X301_DIDO("301 · Double In/Out", "301 DI/DO", "X301", "DOUBLE_IN_OUT"),
    X701_DO("701 · Double Out", "701 DO", "X701", "DOUBLE_OUT"),
    X701_DIDO("701 · Double In/Out", "701 DI/DO", "X701", "DOUBLE_IN_OUT"),
    X1001_DO("1001 · Double Out", "1001 DO", "X1001", "DOUBLE_OUT"),
    X1001_DIDO("1001 · Double In/Out", "1001 DI/DO", "X1001", "DOUBLE_IN_OUT")
}

// ─────────────────────────────────────────────
// Агрегаты
// ─────────────────────────────────────────────
data class CricketAggregate(
    val legs: Int = 0,
    val darts: Int = 0,
    val misses: Int = 0,
    val triples: Int = 0,
    val bullAttempts: Int = 0,
    val bullHits: Int = 0,
    val perfectRounds: Int = 0,
    val strongRounds: Int = 0,
    val avgMpr: Double = 0.0,
    val bestMpr: Double = 0.0
)

data class Game501Aggregate(
    val legs: Int = 0,
    val darts: Int = 0,
    val doublesHit: Int = 0,
    val doublesAttempted: Int = 0,
    val wins: Int = 0,
    val totalMatches: Int = 0,
    val avgPpr: Double = 0.0,
    val bestMatchPpr: Double = 0.0,
    val bestLegPpr: Double = 0.0,
    val avgDartsPerLeg: Double = 0.0,
    val avgCount180: Double = 0.0,
    val avgCount170plus: Double = 0.0,
    val avgCount130plus: Double = 0.0,
    val avgCount90plus: Double = 0.0,
    val avgCount57plus: Double = 0.0,
    val avgCount57minus: Double = 0.0,
    val first9: Double = 0.0,
    val nonClose: Double = 0.0,
    val currentStreak: Int = 0,
    val maxStreak: Int = 0,
    val trend: Double = 0.0,
    val hasTrend: Boolean = false,
    val byBot: List<BotStat> = emptyList(),
    val topMatches: List<MatchInfo> = emptyList()
)

data class BotStat(
    val botName: String,
    val matches: Int,
    val ppr: Double,
    val dblPct: Double
)

data class MatchInfo(
    val dateMillis: Long,
    val ppr: Double,
    val opponent: String,
    val dartsPerLeg: Double
)

data class ChartSpec(
    val title: String,
    val points: List<Double>,
    val yMin: Double,
    val yMax: Double,
    val lineColor: androidx.compose.ui.graphics.Color
)

// ─────────────────────────────────────────────
// Хелперы парсинга
// ─────────────────────────────────────────────
object StatsParse {

    fun stringList(s: String): List<String> =
        if (s.isBlank()) emptyList() else s.split("|")

    fun intList(s: String): List<Int> =
        if (s.isBlank()) emptyList() else s.split("|").map { it.toIntOrNull() ?: 0 }

    fun doubleList(s: String): List<Double> =
        if (s.isBlank()) emptyList() else s.split("|").map { it.toDoubleOrNull() ?: 0.0 }

    fun percentLabel(part: Int, total: Int): String {
        if (total <= 0) return "—"
        val p = part.toDouble() / total * 100.0
        return String.format(Locale.US, "%.1f%%", p)
    }

    fun isOwner(isBotFlag: String, name: String, ownerName: String): Boolean {
        if (isBotFlag != "0") return false
        if (ownerName.isBlank()) return true
        return name.equals(ownerName, ignoreCase = true)
    }

    fun pluralMatches(n: Int): String {
        val mod10 = n % 10
        val mod100 = n % 100
        return when {
            mod100 in 11..19 -> "матчей"
            mod10 == 1 -> "матч"
            mod10 in 2..4 -> "матча"
            else -> "матчей"
        }
    }
}

// ─────────────────────────────────────────────
// Функции подсчёта
// ─────────────────────────────────────────────
object StatsCompute {

    // ── Крикет ──
    fun cricketAggregate(games: List<CricketGameEntity>, ownerName: String): CricketAggregate {
        var legs = 0
        var darts = 0
        var misses = 0
        var triples = 0
        var bullAttempts = 0
        var bullHits = 0
        var perfect = 0
        var strong = 0
        val mprValues = mutableListOf<Double>()

        for (g in games) {
            val bots = StatsParse.stringList(g.playerIsBot)
            val names = StatsParse.stringList(g.playerNames)
            val dartsList = StatsParse.intList(g.totalDarts)
            val missesList = StatsParse.intList(g.misses)
            val triplesList = StatsParse.intList(g.triples)
            val bullAttList = StatsParse.intList(g.bullAttempts)
            val bullHitList = StatsParse.intList(g.bullHits)
            val perfList = StatsParse.intList(g.perfectRounds)
            val strongList = StatsParse.intList(g.strongRounds)
            val mprList = StatsParse.doubleList(g.mpr)

            var hasOwner = false
            for (i in bots.indices) {
                val name = names.getOrNull(i) ?: ""
                if (StatsParse.isOwner(bots[i], name, ownerName)) {
                    hasOwner = true
                    darts += dartsList.getOrElse(i) { 0 }
                    misses += missesList.getOrElse(i) { 0 }
                    triples += triplesList.getOrElse(i) { 0 }
                    bullAttempts += bullAttList.getOrElse(i) { 0 }
                    bullHits += bullHitList.getOrElse(i) { 0 }
                    perfect += perfList.getOrElse(i) { 0 }
                    strong += strongList.getOrElse(i) { 0 }
                    if (i < mprList.size) mprValues.add(mprList[i])
                }
            }
            if (hasOwner) legs += g.legsPlayed
        }

        return CricketAggregate(
            legs = legs,
            darts = darts,
            misses = misses,
            triples = triples,
            bullAttempts = bullAttempts,
            bullHits = bullHits,
            perfectRounds = perfect,
            strongRounds = strong,
            avgMpr = if (mprValues.isEmpty()) 0.0 else mprValues.average(),
            bestMpr = mprValues.maxOrNull() ?: 0.0
        )
    }

    fun cricketChartData(games: List<CricketGameEntity>, ownerName: String): List<Double> {
        val sorted = games.sortedBy { it.dateMillis }
        val out = mutableListOf<Double>()
        for (g in sorted) {
            val bots = StatsParse.stringList(g.playerIsBot)
            val names = StatsParse.stringList(g.playerNames)
            val mprList = StatsParse.doubleList(g.mpr)
            for (i in bots.indices) {
                val name = names.getOrNull(i) ?: ""
                if (StatsParse.isOwner(bots[i], name, ownerName)) {
                    mprList.getOrNull(i)?.let { out.add(it) }
                    break
                }
            }
        }
        return out
    }

    // ── Чекауты ──
    fun closedCheckouts(games: List<Game501Entity>, ownerName: String): Map<Int, Int> {
        val result = mutableMapOf<Int, Int>()
        for (g in games) {
            val bots = StatsParse.stringList(g.playerIsBot)
            val names = StatsParse.stringList(g.playerNames)
            val closeValuesByPlayer = g.closeValues.split("|")

            for (i in bots.indices) {
                val name = names.getOrNull(i) ?: ""
                if (StatsParse.isOwner(bots[i], name, ownerName)) {
                    val myClose = closeValuesByPlayer.getOrNull(i) ?: ""
                    if (myClose.isNotBlank()) {
                        myClose.split(",").forEach { raw ->
                            val v = raw.trim().toIntOrNull()
                            if (v != null && v in 2..170) {
                                result[v] = (result[v] ?: 0) + 1
                            }
                        }
                    }
                }
            }
        }
        return result
    }

    // ── 501 графики ──
    fun game501ChartData(
        games: List<Game501Entity>,
        ownerName: String,
        variant: GameVariant
    ): List<ChartSpec> {
        val filtered = games.filter { g ->
            if (!g.isFinished) return@filter false
            if (g.outModeName == "STRAIGHT_OUT") return@filter false
            if (variant == GameVariant.ALL) return@filter true
            g.gameTypeName == variant.gameTypeName && g.outModeName == variant.outModeName
        }.sortedBy { it.dateMillis }

        val pprList = mutableListOf<Double>()
        val dblList = mutableListOf<Double>()

        for (g in filtered) {
            val bots = StatsParse.stringList(g.playerIsBot)
            val names = StatsParse.stringList(g.playerNames)
            val pprStr = StatsParse.doubleList(g.ppr)
            val hitList = StatsParse.intList(g.doublesHit)
            val attList = StatsParse.intList(g.doublesAttempted)

            for (i in bots.indices) {
                val name = names.getOrNull(i) ?: ""
                if (StatsParse.isOwner(bots[i], name, ownerName)) {
                    pprStr.getOrNull(i)?.let { pprList.add(it) }
                    val hit = hitList.getOrElse(i) { 0 }
                    val att = attList.getOrElse(i) { 0 }
                    if (att > 0) dblList.add(hit.toDouble() / att * 100.0)
                    break
                }
            }
        }

        return listOf(
            ChartSpec(
                title = "Средний набор (PPR)",
                points = pprList,
                yMin = 30.0,
                yMax = 110.0,
                lineColor = com.lodkin.dartstrainer.theme.Accent
            ),
            ChartSpec(
                title = "Точность удвоений (D%)",
                points = dblList,
                yMin = 0.0,
                yMax = 60.0,
                lineColor = com.lodkin.dartstrainer.theme.GoldAccent
            )
        )
    }

    // ── 501 агрегат ──
    fun game501Aggregate(games: List<Game501Entity>, ownerName: String): Game501Aggregate {
        var legs = 0
        var darts = 0
        var doublesHit = 0
        var doublesAttempted = 0
        var wins = 0
        var matches = 0

        var sumCount180 = 0
        var sumCount170plus = 0
        var sumCount130plus = 0
        var sumCount90plus = 0
        var sumCount57plus = 0
        var sumCount57minus = 0

        var sumFirst9Score = 0
        var sumFirst9Darts = 0
        var sumNonCloseScore = 0
        var sumNonCloseDarts = 0

        val pprValues = mutableListOf<Double>()
        val bestLegPprValues = mutableListOf<Double>()
        val allMatchesInOrder = mutableListOf<Boolean>()

        val botMatches = mutableMapOf<String, MutableList<Game501Entity>>()
        val matchCandidates = mutableListOf<MatchInfo>()

        for (g in games) {
            val bots = StatsParse.stringList(g.playerIsBot)
            val names = StatsParse.stringList(g.playerNames)
            val dartsList = StatsParse.intList(g.matchDarts)
            val hitList = StatsParse.intList(g.doublesHit)
            val attList = StatsParse.intList(g.doublesAttempted)
            val pprList = StatsParse.doubleList(g.ppr)
            val bestLegPprList = StatsParse.doubleList(g.bestLegPpr)

            val c180 = StatsParse.intList(g.matchCount180)
            val c170 = StatsParse.intList(g.matchCount170plus)
            val c130 = StatsParse.intList(g.matchCount130plus)
            val c90 = StatsParse.intList(g.matchCount90plus)
            val c57p = StatsParse.intList(g.matchCount57plus)
            val c57m = StatsParse.intList(g.matchCount57minus)

            val f9s = StatsParse.intList(g.first9Score)
            val f9d = StatsParse.intList(g.first9Darts)
            val ncs = StatsParse.intList(g.nonCloseScore)
            val ncd = StatsParse.intList(g.nonCloseDarts)

            var hasOwner = false
            var ownerWon = false
            var ownerIndex = -1

            for (i in bots.indices) {
                val name = names.getOrNull(i) ?: ""
                if (StatsParse.isOwner(bots[i], name, ownerName)) {
                    hasOwner = true
                    ownerIndex = i
                    darts += dartsList.getOrElse(i) { 0 }
                    doublesHit += hitList.getOrElse(i) { 0 }
                    doublesAttempted += attList.getOrElse(i) { 0 }
                    if (i < pprList.size) pprValues.add(pprList[i])
                    if (i < bestLegPprList.size && bestLegPprList[i] > 0) {
                        bestLegPprValues.add(bestLegPprList[i])
                    }
                    if (g.winnerIndex == i) {
                        wins++
                        ownerWon = true
                    }

                    sumCount180 += c180.getOrElse(i) { 0 }
                    sumCount170plus += c170.getOrElse(i) { 0 }
                    sumCount130plus += c130.getOrElse(i) { 0 }
                    sumCount90plus += c90.getOrElse(i) { 0 }
                    sumCount57plus += c57p.getOrElse(i) { 0 }
                    sumCount57minus += c57m.getOrElse(i) { 0 }

                    sumFirst9Score += f9s.getOrElse(i) { 0 }
                    sumFirst9Darts += f9d.getOrElse(i) { 0 }
                    sumNonCloseScore += ncs.getOrElse(i) { 0 }
                    sumNonCloseDarts += ncd.getOrElse(i) { 0 }
                }
            }

            if (hasOwner) {
                matches++
                legs += g.legsPlayed

                val botNames = bots.indices.filter { bots[it] == "1" }
                    .mapNotNull { idx -> names.getOrNull(idx) }
                val opponentLabel = if (botNames.isEmpty()) "человек" else botNames.joinToString(" + ")

                if (botNames.isNotEmpty()) {
                    botMatches.getOrPut(opponentLabel) { mutableListOf() }.add(g)
                }

                val humanPpr = pprList.getOrElse(ownerIndex) { 0.0 }
                val humanDarts = dartsList.getOrElse(ownerIndex) { 0 }
                val dpl = if (g.legsPlayed > 0) humanDarts.toDouble() / g.legsPlayed else 0.0
                matchCandidates.add(
                    MatchInfo(
                        dateMillis = g.dateMillis,
                        ppr = humanPpr,
                        opponent = opponentLabel,
                        dartsPerLeg = dpl
                    )
                )

                allMatchesInOrder.add(ownerWon)
            }
        }

        val avgPpr = if (pprValues.isEmpty()) 0.0 else pprValues.average()
        val bestMatchPpr = pprValues.maxOrNull() ?: 0.0
        val bestLegPpr = bestLegPprValues.maxOrNull() ?: 0.0
        val avgDartsPerLeg = if (legs > 0) darts.toDouble() / legs else 0.0

        val safeLegs = if (legs > 0) legs.toDouble() else 1.0
        val avg180 = sumCount180 / safeLegs
        val avg170 = sumCount170plus / safeLegs
        val avg130 = sumCount130plus / safeLegs
        val avg90 = sumCount90plus / safeLegs
        val avg57p = sumCount57plus / safeLegs
        val avg57m = sumCount57minus / safeLegs

        val first9 = if (sumFirst9Darts > 0) sumFirst9Score.toDouble() / (sumFirst9Darts / 3.0) else 0.0
        val nonClose = if (sumNonCloseDarts > 0) sumNonCloseScore.toDouble() / (sumNonCloseDarts / 3.0) else 0.0

        var currentStreak = 0
        for (won in allMatchesInOrder) {
            if (won) currentStreak++ else break
        }

        var maxStreak = 0
        var run = 0
        for (i in allMatchesInOrder.indices.reversed()) {
            if (allMatchesInOrder[i]) {
                run++
                if (run > maxStreak) maxStreak = run
            } else {
                run = 0
            }
        }

        var trend = 0.0
        var hasTrend = false
        val matchPprs = games.mapNotNull { g ->
            val bots = StatsParse.stringList(g.playerIsBot)
            val names = StatsParse.stringList(g.playerNames)
            val pprList = StatsParse.doubleList(g.ppr)
            val idx = bots.indices.firstOrNull { i ->
                StatsParse.isOwner(bots[i], names.getOrNull(i) ?: "", ownerName)
            } ?: -1
            if (idx >= 0) pprList.getOrNull(idx) else null
        }
        if (matchPprs.size >= 4) {
            val half = matchPprs.size / 2
            val fresh = matchPprs.take(half)
            val old = matchPprs.drop(half).take(half)
            if (fresh.isNotEmpty() && old.isNotEmpty()) {
                trend = fresh.average() - old.average()
                hasTrend = true
            }
        }

        val byBotList = botMatches.map { (name, list) ->
            val pprs = mutableListOf<Double>()
            var hitSum = 0
            var attSum = 0
            for (g in list) {
                val bots = StatsParse.stringList(g.playerIsBot)
                val names = StatsParse.stringList(g.playerNames)
                val pprList = StatsParse.doubleList(g.ppr)
                val hitList = StatsParse.intList(g.doublesHit)
                val attList = StatsParse.intList(g.doublesAttempted)
                val idx = bots.indices.firstOrNull { i ->
                    StatsParse.isOwner(bots[i], names.getOrNull(i) ?: "", ownerName)
                } ?: -1
                if (idx >= 0) {
                    pprList.getOrNull(idx)?.let { pprs.add(it) }
                    hitSum += hitList.getOrElse(idx) { 0 }
                    attSum += attList.getOrElse(idx) { 0 }
                }
            }
            BotStat(
                botName = name,
                matches = list.size,
                ppr = if (pprs.isEmpty()) 0.0 else pprs.average(),
                dblPct = if (attSum > 0) hitSum.toDouble() / attSum * 100.0 else 0.0
            )
        }.sortedBy { it.ppr }

        val top5 = matchCandidates.sortedByDescending { it.ppr }.take(5)

        return Game501Aggregate(
            legs = legs,
            darts = darts,
            doublesHit = doublesHit,
            doublesAttempted = doublesAttempted,
            wins = wins,
            totalMatches = matches,
            avgPpr = avgPpr,
            bestMatchPpr = bestMatchPpr,
            bestLegPpr = bestLegPpr,
            avgDartsPerLeg = avgDartsPerLeg,
            avgCount180 = avg180,
            avgCount170plus = avg170,
            avgCount130plus = avg130,
            avgCount90plus = avg90,
            avgCount57plus = avg57p,
            avgCount57minus = avg57m,
            first9 = first9,
            nonClose = nonClose,
            currentStreak = currentStreak,
            maxStreak = maxStreak,
            trend = trend,
            hasTrend = hasTrend,
            byBot = byBotList,
            topMatches = top5
        )
    }
}
