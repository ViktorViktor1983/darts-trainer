package com.lodkin.dartstrainer.data.game501

import kotlin.random.Random

// ИИ бота для игры x01
object Game501BotAI {

    private const val ENABLE_VARIABILITY = false

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
            game.sessionForm * updatedPlayer.botStreak * fatigueFactor(game.sessionStartTime)
        } else 1.0

        val startLeg = currentGame.currentLegNumber
        val startSet = currentGame.currentSetNumber

        var dartsThrown = 0
        var previousMissed = false
        var turnScoreForCategories = 0

        while (dartsThrown < 3 && !currentGame.isFinished) {
            if (currentGame.currentLegNumber != startLeg ||
                currentGame.currentSetNumber != startSet
            ) break

            val current = currentGame.players.getOrNull(playerIndex) ?: break
            val target = chooseTarget(current, previousMissed) ?: break

            val result = simulateDart(target, multiplier, current.botLevel)
            previousMissed = !result.isHit

            val wasDoubleTarget = target.multiplier == 2
            val hitDouble = result.actualMultiplier == 2 && result.actualSector != 0
            if (wasDoubleTarget && !hitDouble) {
                currentGame = Game501Logic.recordDoublesAttempts(currentGame, 1)
            }

            if (result.actualSector == 0) {
                currentGame = Game501Logic.applyThrow(currentGame, 20, ThrowMultiplier.MISS)
            } else {
                val mult = when (result.actualMultiplier) {
                    2 -> ThrowMultiplier.DOUBLE
                    3 -> ThrowMultiplier.TRIPLE
                    else -> ThrowMultiplier.SINGLE
                }
                currentGame = Game501Logic.applyThrow(currentGame, result.actualSector, mult)
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
        botLevel: Int
    ): SimulatedThrow {
        val baseAcc = when (target.multiplier) {
            3 -> getTripleAccuracy(botLevel)
            2 -> getDoubleAccuracy(botLevel)
            else -> getSingleAccuracy(botLevel)
        }
        val finalAcc = (baseAcc * formMultiplier).coerceIn(0.01, 0.99)

        if (Random.nextDouble() < finalAcc) {
            return SimulatedThrow(target.sector, target.multiplier, isHit = true)
        }
        return when (target.multiplier) {
            3 -> missFromTriple(target.sector)
            2 -> missFromDouble(target.sector)
            else -> missFromSingle(target.sector)
        }
    }

    // ─────────────────────────────────────────────
    // ТОЧНОСТИ (калибровка: Разрядник T≈12%, D≈22%)
    // ─────────────────────────────────────────────
    private fun getTripleAccuracy(botLevel: Int): Double {
        val lvl = botLevel.coerceIn(1, 16)
        // Новичок 5%, Разрядник (7) 12%, Легенда (16) 23%
        return 0.05 + (lvl - 1) * 0.012
    }

    private fun getDoubleAccuracy(botLevel: Int): Double {
        val lvl = botLevel.coerceIn(1, 16)
        // Новичок 8%, Разрядник (7) 21%, Легенда (16) 41%
        return 0.08 + (lvl - 1) * 0.022
    }

    private fun getSingleAccuracy(botLevel: Int): Double {
        val t = getTripleAccuracy(botLevel)
        return (t * 1.4 + 0.25).coerceAtMost(0.95)
    }

    private fun missFromTriple(sector: Int): SimulatedThrow {
        val r = Random.nextDouble()
        if (r < 0.57) return SimulatedThrow(sector, 1, isHit = false)
        if (r < 0.81) {
            val (l, rt) = NEIGHBORS[sector] ?: (sector to sector)
            val n = if (Random.nextBoolean()) l else rt
            return SimulatedThrow(n, 1, isHit = false)
        }
        if (r < 0.97) {
            val (l, rt) = NEIGHBORS[sector] ?: (sector to sector)
            val n = if (Random.nextBoolean()) l else rt
            return SimulatedThrow(n, 3, isHit = false)
        }
        if (r < 0.995) {
            val other = listOf(12, 18).random()
            return SimulatedThrow(other, 1, isHit = false)
        }
        return SimulatedThrow(0, 0, isHit = false)
    }

    private fun missFromDouble(sector: Int): SimulatedThrow {
        val r = Random.nextDouble()
        if (r < 0.50) return SimulatedThrow(0, 0, isHit = false)
        if (r < 0.75) return SimulatedThrow(sector, 1, isHit = false)
        if (r < 0.90) {
            val (l, rt) = NEIGHBORS[sector] ?: (sector to sector)
            val n = if (Random.nextBoolean()) l else rt
            return SimulatedThrow(n, 2, isHit = false)
        }
        val (l, rt) = NEIGHBORS[sector] ?: (sector to sector)
        val n = if (Random.nextBoolean()) l else rt
        return SimulatedThrow(n, 1, isHit = false)
    }

    private fun missFromSingle(sector: Int): SimulatedThrow {
        val r = Random.nextDouble()
        if (r < 0.35) {
            val (l, rt) = NEIGHBORS[sector] ?: (sector to sector)
            val n = if (Random.nextBoolean()) l else rt
            return SimulatedThrow(n, 1, isHit = false)
        }
        if (r < 0.70) {
            val (l, rt) = NEIGHBORS[sector] ?: (sector to sector)
            val n = if (Random.nextBoolean()) l else rt
            return SimulatedThrow(n, 3, isHit = false)
        }
        if (r < 0.90) return SimulatedThrow(sector, 2, isHit = false)
        return SimulatedThrow(0, 0, isHit = false)
    }

    private fun chooseTarget(player: Player501, previousMissed: Boolean): CheckoutThrow? {
        val score = player.score
        if (score > 170 || !CheckoutTable.isCheckoutPossible(score)) {
            return if (score % 2 == 0) CheckoutThrow(20, 3) else CheckoutThrow(19, 3)
        }
        val paths = CheckoutTable.pathsFor(score) ?: return CheckoutThrow(20, 3)
        val path = if (previousMissed) {
            paths.firstOrNull { it.label.startsWith("Промах") } ?: paths.first()
        } else paths.first()
        val dartsUsed = player.turnDarts
        if (dartsUsed >= path.throws.size) return path.throws.last()
        return path.throws[dartsUsed]
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
