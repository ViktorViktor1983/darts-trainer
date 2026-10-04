package com.lodkin.dartstrainer.data.game501

import kotlin.random.Random

object Game501BotAI {

    private const val ENABLE_VARIABILITY = true

    // Точность в утроение. Прямая таблица по уровням 1..16.
    private val TRIPLE_ACCURACY = doubleArrayOf(
        0.0,    // 0 — пустой
        0.002,  // 1  Новичок
        0.042,  // 2  Ученик
        0.080,  // 3  Любитель
        0.114,  // 4  Уверенный
        0.148,  // 5  Опытный
        0.185,  // 6  Практик
        0.198,  // 7  Разрядник
        0.230,  // 8  Турнирный
        0.270,  // 9  Сильный
        0.305,  // 10 Крепкий
        0.348,  // 11 КМС
        0.405,  // 12 Почти мастер
        0.455,  // 13 Мастер
        0.498,  // 14 Чемпион
        0.535,  // 15 Профи
        0.590   // 16 Легенда
    )

    // Точность в удвоение. Прямая таблица по уровням 1..16.
    private val DOUBLE_ACCURACY = doubleArrayOf(
        0.0,      // 0
        0.065,    // 1
        0.0855,   // 2
        0.105,    // 3
        0.127,    // 4
        0.147,    // 5
        0.168,    // 6
        0.190,    // 7
        0.205,    // 8
        0.229,    // 9
        0.250,    // 10
        0.270,    // 11
        0.293,    // 12
        0.325,    // 13
        0.383,    // 14
        0.408,    // 15
        0.438     // 16
    )

    private val NEIGHBORS: Map<Int, Pair<Int, Int>> = mapOf(
        20 to (5 to 1), 1 to (20 to 18), 18 to (1 to 4), 4 to (18 to 13),
        13 to (4 to 6), 6 to (13 to 10), 10 to (6 to 15), 15 to (10 to 2),
        2 to (15 to 17), 17 to (2 to 3), 3 to (17 to 19), 19 to (3 to 7),
        7 to (19 to 16), 16 to (7 to 8), 8 to (16 to 11), 11 to (8 to 14),
        14 to (11 to 9), 9 to (14 to 12), 12 to (9 to 5), 5 to (12 to 20)
    )

    fun performTurn(game: Game501, playerIndex: Int): Game501 {
        val player = game.players.getOrNull(playerIndex) ?: return game
        if (!player.isBot) return game

        val updatedPlayer = updateStreak(player)
        val playersWithStreak = game.players.toMutableList()
        playersWithStreak[playerIndex] = updatedPlayer
        var currentGame = game.copy(players = playersWithStreak)

        val multiplier = if (ENABLE_VARIABILITY) {
            val m = game.sessionForm *
                    updatedPlayer.botStreak *
                    fatigueFactor(game.sessionStartTime)
            m.coerceIn(0.55, 1.45)
        } else 1.0

        val pressure = pressureFactor(game, updatedPlayer.teamIndex)

        val startLeg = currentGame.currentLegNumber
        val startSet = currentGame.currentSetNumber

        var dartsThrown = 0
        var turnScoreForCategories = 0

        while (dartsThrown < 3 && !currentGame.isFinished) {
            if (currentGame.currentLegNumber != startLeg ||
                currentGame.currentSetNumber != startSet
            ) break

            val current = currentGame.players.getOrNull(playerIndex) ?: break
            val target = chooseTarget(current)

            val result = simulateDart(target, multiplier, pressure, current.botLevel)

            if (target.multiplier == 2) {
                currentGame = Game501Logic.recordDoublesAttempts(currentGame, 1)
                if (result.actualMultiplier == 2 && result.actualSector == target.sector) {
                    currentGame = Game501Logic.recordDoublesHit(currentGame, 1)
                }
            }

            if (result.actualSector == 0) {
                currentGame = Game501Logic.applyThrow(
                    currentGame, 20, ThrowMultiplier.MISS,
                    countDoubleAttempt = false, countDoubleHit = false
                )
            } else {
                val mult = when (result.actualMultiplier) {
                    2 -> ThrowMultiplier.DOUBLE
                    3 -> ThrowMultiplier.TRIPLE
                    else -> ThrowMultiplier.SINGLE
                }
                currentGame = Game501Logic.applyThrow(
                    currentGame, result.actualSector, mult,
                    countDoubleAttempt = false, countDoubleHit = false
                )
                turnScoreForCategories += result.actualSector * result.actualMultiplier
            }
            dartsThrown++
        }

        if (turnScoreForCategories > 0 && !currentGame.isFinished) {
            currentGame = Game501Logic.recordCategoryForBot(currentGame, playerIndex, turnScoreForCategories)
        }

        val legChanged = currentGame.currentLegNumber != startLeg ||
            currentGame.currentSetNumber != startSet
        if (!currentGame.isFinished && !legChanged &&
            currentGame.currentPlayerIndex == playerIndex
        ) {
            currentGame = Game501Logic.finishTurn(currentGame)
        }
        return currentGame
    }

    private data class SimulatedThrow(
        val actualSector: Int,
        val actualMultiplier: Int,
        val isHit: Boolean
    )

    private fun simulateDart(
        target: CheckoutThrow,
        formMultiplier: Double,
        pressure: Double,
        botLevel: Int
    ): SimulatedThrow {
        val baseAcc = when (target.multiplier) {
            3 -> getTripleAccuracy(botLevel)
            2 -> getDoubleAccuracy(botLevel)
            else -> getSingleAccuracy(botLevel)
        }
        val finalAcc = if (target.multiplier == 2) {
            (baseAcc * formMultiplier * pressure).coerceIn(0.01, 0.99)
        } else {
            (baseAcc * formMultiplier).coerceIn(0.01, 0.99)
        }

        if (Random.nextDouble() < finalAcc) {
            return SimulatedThrow(target.sector, target.multiplier, isHit = true)
        }
        return when (target.multiplier) {
            3 -> missFromTriple(target.sector, botLevel)
            2 -> missFromDouble(target.sector, botLevel)
            else -> missFromSingle(target.sector, botLevel)
        }
    }

    private fun pressureFactor(game: Game501, myTeamIndex: Int): Double {
        if (game.players.all { it.isBot }) return 1.0

        val opponent = game.players.firstOrNull { it.teamIndex != myTeamIndex }
            ?: return 1.0

        val dartsNeeded = dartsToClose(opponent.score)
        return when (dartsNeeded) {
            1 -> 0.78
            2 -> 0.85
            3 -> 0.92
            else -> 1.0
        }
    }

    private fun dartsToClose(score: Int): Int {
        if (score > 170) return 99
        if (score < 2) return 0
        val paths = CheckoutTable.pathsFor(score) ?: return 99
        val cleanPaths = paths.filter { !it.label.startsWith("Промах") }
        if (cleanPaths.isEmpty()) return 99
        return cleanPaths.minOf { it.throws.size }
    }

    private fun getTripleAccuracy(botLevel: Int): Double {
        val lvl = botLevel.coerceIn(1, 16)
        return TRIPLE_ACCURACY[lvl]
    }

    private fun getDoubleAccuracy(botLevel: Int): Double {
        val lvl = botLevel.coerceIn(1, 16)
        return DOUBLE_ACCURACY[lvl]
    }

    private fun getSingleAccuracy(botLevel: Int): Double {
        val t = getTripleAccuracy(botLevel)
        return (t * 1.4 + 0.25).coerceAtMost(0.95)
    }

    private fun interp(a: Double, b: Double, c: Double, lvl: Int): Double {
        return if (lvl <= 7) {
            a + (b - a) * (lvl - 1) / 6.0
        } else {
            b + (c - b) * (lvl - 7) / 9.0
        }
    }

    private fun missFromTriple(sector: Int, botLevel: Int): SimulatedThrow {
        val lvl = botLevel.coerceIn(1, 16)
        val pOut = interp(10.0, 0.63, 0.1, lvl)
        val pSTarget = interp(12.0, 28.0, 45.0, lvl)
        val pSNeighbor = interp(40.0, 40.9, 30.0, lvl)
        val pTNeighbor = interp(22.0, 20.3, 12.0, lvl)
        val pDNeighbor = interp(8.0, 5.1, 4.0, lvl)
        val pDTarget = interp(8.0, 5.1, 9.0, lvl)

        val total = pOut + pSTarget + pSNeighbor + pTNeighbor + pDNeighbor + pDTarget
        val r = Random.nextDouble() * total

        var acc = pOut
        if (r < acc) return SimulatedThrow(0, 0, isHit = false)
        acc += pSTarget
        if (r < acc) return SimulatedThrow(sector, 1, isHit = false)
        acc += pSNeighbor
        if (r < acc) {
            val (l, rt) = NEIGHBORS[sector] ?: (sector to sector)
            val n = if (Random.nextBoolean()) l else rt
            return SimulatedThrow(n, 1, isHit = false)
        }
        acc += pTNeighbor
        if (r < acc) {
            val (l, rt) = NEIGHBORS[sector] ?: (sector to sector)
            val n = if (Random.nextBoolean()) l else rt
            return SimulatedThrow(n, 3, isHit = false)
        }
        acc += pDNeighbor
        if (r < acc) {
            val (l, rt) = NEIGHBORS[sector] ?: (sector to sector)
            val n = if (Random.nextBoolean()) l else rt
            return SimulatedThrow(n, 2, isHit = false)
        }
        return SimulatedThrow(sector, 2, isHit = false)
    }

    private fun missFromSingle(sector: Int, botLevel: Int): SimulatedThrow {
        val lvl = botLevel.coerceIn(1, 16)
        val pOut = interp(8.0, 2.1, 0.5, lvl)
        val pTTarget = interp(5.0, 22.0, 40.0, lvl)
        val pSNeighbor = interp(60.0, 50.4, 35.0, lvl)
        val pDTarget = interp(13.0, 17.0, 15.0, lvl)
        val pTNeighbor = interp(7.0, 4.3, 5.0, lvl)
        val pDNeighbor = interp(7.0, 4.3, 4.5, lvl)

        val total = pOut + pTTarget + pSNeighbor + pDTarget + pTNeighbor + pDNeighbor
        val r = Random.nextDouble() * total

        var acc = pOut
        if (r < acc) return SimulatedThrow(0, 0, isHit = false)
        acc += pTTarget
        if (r < acc) return SimulatedThrow(sector, 3, isHit = false)
        acc += pSNeighbor
        if (r < acc) {
            val (l, rt) = NEIGHBORS[sector] ?: (sector to sector)
            val n = if (Random.nextBoolean()) l else rt
            return SimulatedThrow(n, 1, isHit = false)
        }
        acc += pDTarget
        if (r < acc) return SimulatedThrow(sector, 2, isHit = false)
        acc += pTNeighbor
        if (r < acc) {
            val (l, rt) = NEIGHBORS[sector] ?: (sector to sector)
            val n = if (Random.nextBoolean()) l else rt
            return SimulatedThrow(n, 3, isHit = false)
        }
        acc += pDNeighbor
        if (r < acc) {
            val (l, rt) = NEIGHBORS[sector] ?: (sector to sector)
            val n = if (Random.nextBoolean()) l else rt
            return SimulatedThrow(n, 2, isHit = false)
        }
        return SimulatedThrow(0, 0, isHit = false)
    }

    private fun missFromDouble(sector: Int, botLevel: Int): SimulatedThrow {
        val lvl = botLevel.coerceIn(1, 16)
        val pOut = interp(60.0, 38.0, 20.0, lvl)
        val pSTarget = interp(8.0, 36.2, 45.0, lvl)
        val pDNeighbor = interp(8.0, 12.3, 15.0, lvl)
        val pSNeighbor = interp(20.0, 12.3, 15.0, lvl)
        val pTNeighbor = interp(4.0, 1.2, 5.0, lvl)

        val total = pOut + pSTarget + pDNeighbor + pSNeighbor + pTNeighbor
        val r = Random.nextDouble() * total

        var acc = pOut
        if (r < acc) return SimulatedThrow(0, 0, isHit = false)
        acc += pSTarget
        if (r < acc) return SimulatedThrow(sector, 1, isHit = false)
        acc += pDNeighbor
        if (r < acc) {
            val (l, rt) = NEIGHBORS[sector] ?: (sector to sector)
            val n = if (Random.nextBoolean()) l else rt
            return SimulatedThrow(n, 2, isHit = false)
        }
        acc += pSNeighbor
        if (r < acc) {
            val (l, rt) = NEIGHBORS[sector] ?: (sector to sector)
            val n = if (Random.nextBoolean()) l else rt
            return SimulatedThrow(n, 1, isHit = false)
        }
        val (l, rt) = NEIGHBORS[sector] ?: (sector to sector)
        val n = if (Random.nextBoolean()) l else rt
        return SimulatedThrow(n, 3, isHit = false)
    }

    private fun chooseTarget(player: Player501): CheckoutThrow {
        val score = player.score
        if (score > 170 || !CheckoutTable.isCheckoutPossible(score)) {
            return if (score % 2 == 0) CheckoutThrow(20, 3) else CheckoutThrow(19, 3)
        }

        val dartsLeft = (3 - player.turnDarts).coerceAtLeast(1)
        val paths = CheckoutTable.pathsFor(score) ?: return CheckoutThrow(20, 3)

        val cleanPaths = paths.filter { !it.label.startsWith("Промах") }.ifEmpty { paths }
        val viable = cleanPaths.filter { it.throws.size <= dartsLeft }

        if (viable.isNotEmpty()) {
            val sorted = viable.sortedBy { path ->
                val last = path.throws.lastOrNull()
                if (last != null && last.multiplier == 2) {
                    BotPreferences.doublePriority(player.botLevel, last.sector)
                } else {
                    Int.MAX_VALUE
                }
            }
            return sorted.first().throws.first()
        }

        if (dartsLeft == 1) {
            return chooseSetupTarget(score, player.botLevel)
        }

        return CheckoutThrow(20, 1)
    }

    private fun chooseSetupTarget(score: Int, botLevel: Int): CheckoutThrow {
        val favDoubles = BotPreferences.favouriteDoubles(botLevel.coerceIn(1, 16))

        val candidates = listOf(
            CheckoutThrow(20, 1), CheckoutThrow(19, 1), CheckoutThrow(18, 1),
            CheckoutThrow(17, 1), CheckoutThrow(16, 1), CheckoutThrow(15, 1),
            CheckoutThrow(20, 3), CheckoutThrow(19, 3), CheckoutThrow(18, 3),
            CheckoutThrow(17, 3), CheckoutThrow(16, 3), CheckoutThrow(15, 3)
        )

        var bestThrow: CheckoutThrow = CheckoutThrow(20, 1)
        var bestQuality = Int.MIN_VALUE

        for (t in candidates) {
            val points = t.sector * t.multiplier
            val newScore = score - points
            if (newScore < 2 || newScore > 170) continue

            val quality = evaluateRemainder(newScore, favDoubles)
            if (quality > bestQuality) {
                bestQuality = quality
                bestThrow = t
            }
        }

        return bestThrow
    }

    private fun evaluateRemainder(score: Int, favDoubles: List<Int>): Int {
        if (score < 2) return -100
        if (score % 2 != 0) return -50
        if (score > 40) return -20
        val dblSector = score / 2
        if (dblSector < 1 || dblSector > 20) return -20

        val base = 80
        val favIdx = favDoubles.indexOf(dblSector)
        val bonus = when {
            favIdx == 0 -> 25
            favIdx == 1 -> 20
            favIdx == 2 -> 15
            favIdx in 3..5 -> 10
            else -> 0
        }
        return base + bonus
    }

    private fun updateStreak(player: Player501): Player501 {
        if (player.botStreakLeft > 0) {
            return player.copy(botStreakLeft = player.botStreakLeft - 1)
        }
        if (Random.nextDouble() < 0.20) {
            val positive = Random.nextBoolean()
            val newStreak = if (positive) 1.10 + Random.nextDouble() * 0.10
            else 0.80 + Random.nextDouble() * 0.10
            val duration = 1 + Random.nextInt(2)
            return player.copy(botStreak = newStreak, botStreakLeft = duration - 1)
        }
        return player.copy(botStreak = 1.0, botStreakLeft = 0)
    }

    private fun fatigueFactor(sessionStartTime: Long): Double {
        if (sessionStartTime <= 0L) return 1.0
        val minutes = (System.currentTimeMillis() - sessionStartTime) / 60000.0
        return when {
            minutes < 90.0 -> 1.0
            minutes < 120.0 -> 1.0 - 0.10 * (minutes - 90.0) / 30.0
            minutes < 180.0 -> 0.90 - 0.05 * (minutes - 120.0) / 60.0
            minutes < 240.0 -> 0.85 - 0.05 * (minutes - 180.0) / 60.0
            else -> 0.80
        }
    }
}
