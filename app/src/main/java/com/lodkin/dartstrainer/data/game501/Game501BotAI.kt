package com.lodkin.dartstrainer.data.game501

import kotlin.random.Random

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
        var turnScoreForCategories = 0

        while (dartsThrown < 3 && !currentGame.isFinished) {
            if (currentGame.currentLegNumber != startLeg ||
                currentGame.currentSetNumber != startSet
            ) break

            val current = currentGame.players.getOrNull(playerIndex) ?: break
            val target = chooseTarget(current)

            val result = simulateDart(target, multiplier, current.botLevel)

            // Попытка в дабл = каждый дротик, направленный в дабл-цель.
            // Попадание в дабл = дротик попал ТОЧНО в целевой дабл-сектор
            // (независимо от обнуления остатка). Так D% = реальный % попаданий.
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
            3 -> missFromTriple(target.sector, botLevel)
            2 -> missFromDouble(target.sector)
            else -> missFromSingle(target.sector)
        }
    }

    // ─────────────────────────────────────────────
    // КАЛИБРОВКА (v9)
    // D% теперь = % попаданий в ЦЕЛЕВОЙ дабл-сектор, поэтому
    // p_double ≈ целевой D%. Раньше (v8) p_double=0.97 давал
    // D%=27% потому что считался другой метрикой (закрытия).
    //
    // Целевые D%: ур.1=5-8%, ур.7=18-21%, ур.16=42-47%.
    // Формула: 0.065 + (lvl-1)*0.02533
    //   ур.1  → 0.065 (6.5%)
    //   ур.7  → 0.217 (21.7%)
    //   ур.16 → 0.445 (44.5%)
    //
    // p_triple НЕ менял (v8 показал PPR во всех коридорах).
    // После снижения p_double ожидаем лёгкое проседание PPR на
    // верхах (больше дротиков на добивание) — калибруем после теста.
    // ─────────────────────────────────────────────
    private fun getTripleAccuracy(botLevel: Int): Double {
        val lvl = botLevel.coerceIn(1, 16)
        return 0.02 + (lvl - 1) * 0.0253
    }

    private fun getDoubleAccuracy(botLevel: Int): Double {
        val lvl = botLevel.coerceIn(1, 16)
        return (0.065 + (lvl - 1) * 0.02533).coerceAtMost(0.60)
    }

    private fun getSingleAccuracy(botLevel: Int): Double {
        val t = getTripleAccuracy(botLevel)
        return (t * 1.4 + 0.25).coerceAtMost(0.95)
    }

    private fun missFromTriple(sector: Int, botLevel: Int): SimulatedThrow {
        val lvl = botLevel.coerceIn(1, 16)
        val missProb = 0.30 - (lvl - 1) * 0.018
        val s20Prob = 0.15 + (lvl - 1) * 0.025

        val r = Random.nextDouble()
        var acc = missProb
        if (r < acc) return SimulatedThrow(0, 0, isHit = false)
        acc += s20Prob
        if (r < acc) return SimulatedThrow(sector, 1, isHit = false)

        val remaining = (1.0 - acc).coerceAtLeast(0.001)
        val localR = (r - acc) / remaining
        val (l, rt) = NEIGHBORS[sector] ?: (sector to sector)
        val n = if (Random.nextBoolean()) l else rt
        return when {
            localR < 0.40 -> SimulatedThrow(n, 1, isHit = false)
            localR < 0.70 -> SimulatedThrow(n, 3, isHit = false)
            localR < 0.90 -> SimulatedThrow(n, 2, isHit = false)
            else -> SimulatedThrow(sector, 2, isHit = false)
        }
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

    // Выбор цели на ТЕКУЩИЙ дротик.
    // * Оставшиеся дротики в ходу = 3 - turnDarts.
    // * Пути для ТЕКУЩЕГО остатка, отфильтрованные по числу дротиков.
    // * Возвращаем ПЕРВЫЙ бросок первого подходящего пути.
    // * Если не влезает — подход в S20 (не попытка в дабл).
    private fun chooseTarget(player: Player501): CheckoutThrow {
        val score = player.score
        if (score > 170 || !CheckoutTable.isCheckoutPossible(score)) {
            return if (score % 2 == 0) CheckoutThrow(20, 3) else CheckoutThrow(19, 3)
        }

        val dartsLeft = (3 - player.turnDarts).coerceAtLeast(1)
        val paths = CheckoutTable.pathsFor(score) ?: return CheckoutThrow(20, 3)

        val cleanPaths = paths.filter { !it.label.startsWith("Промах") }.ifEmpty { paths }
        val viable = cleanPaths.filter { it.throws.size <= dartsLeft }

        if (viable.isEmpty()) {
            return CheckoutThrow(20, 1)
        }
        return viable.first().throws.first()
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
