package com.lodkin.dartstrainer.data.game501

object Game501Logic {

    fun newGame(
        gameType: GameType,
        outMode: OutMode,
        players: List<Player501>,
        legsPerSet: Int = 1,
        setsPerMatch: Int = 1,
        isPairGame: Boolean = false,
        startingTeamIndex: Int = 0,
        autoOkSeconds: Int = 0,
        sessionStartTime: Long = 0L,
        sessionForm: Double = 1.0
    ): Game501 {
        val startingPlayerIndex = players.indexOfFirst { it.teamIndex == startingTeamIndex }
            .let { if (it >= 0) it else 0 }
        val prepared = players.map { p ->
            p.copy(
                score = gameType.startScore,
                turnScore = 0, turnDarts = 0,
                legDarts = 0, legScoreGained = 0,
                legDoublesHit = 0, legDoublesAttempted = 0,
                legCount180 = 0, legCount170plus = 0, legCount130plus = 0,
                legCount90plus = 0, legCount57plus = 0, legCount57minus = 0,
                first9Score = 0, first9Darts = 0
            )
        }
        return Game501(
            gameType = gameType, outMode = outMode, players = prepared,
            currentPlayerIndex = startingPlayerIndex,
            legsPerSet = legsPerSet, setsPerMatch = setsPerMatch,
            isPairGame = isPairGame, teamCount = 2,
            playersPerTeam = if (isPairGame) 2 else 1,
            lastLegStartingTeam = startingTeamIndex,
            autoOkSeconds = autoOkSeconds,
            sessionStartTime = sessionStartTime, sessionForm = sessionForm
        )
    }

    private fun applyCategory(player: Player501, gained: Int): Player501 {
        return when {
            gained == 180 -> player.copy(
                legCount180 = player.legCount180 + 1,
                matchCount180 = player.matchCount180 + 1)
            gained >= 170 -> player.copy(
                legCount170plus = player.legCount170plus + 1,
                matchCount170plus = player.matchCount170plus + 1)
            gained >= 130 -> player.copy(
                legCount130plus = player.legCount130plus + 1,
                matchCount130plus = player.matchCount130plus + 1)
            gained >= 90 -> player.copy(
                legCount90plus = player.legCount90plus + 1,
                matchCount90plus = player.matchCount90plus + 1)
            gained >= 57 -> player.copy(
                legCount57plus = player.legCount57plus + 1,
                matchCount57plus = player.matchCount57plus + 1)
            else -> player.copy(
                legCount57minus = player.legCount57minus + 1,
                matchCount57minus = player.matchCount57minus + 1)
        }
    }

    fun recordCategoryForBot(game: Game501, playerIndex: Int, turnScore: Int): Game501 {
        if (playerIndex !in game.players.indices) return game
        if (turnScore <= 0) return game
        val updated = game.players.toMutableList()
        val p = updated[playerIndex]
        val pWithCat = applyCategory(p, turnScore)
        val scoreBeforeApprox = p.score + turnScore
        val pFinal = if (scoreBeforeApprox > 170) {
            pWithCat.copy(
                nonCloseScore = pWithCat.nonCloseScore + turnScore,
                nonCloseDarts = pWithCat.nonCloseDarts + 3
            )
        } else pWithCat
        updated[playerIndex] = pFinal
        return game.copy(players = updated)
    }

    private fun registerApproach(player: Player501, gained: Int, dartsUsed: Int, scoreBefore: Int): Player501 {
        var p = player
        if (p.first9Darts < 9) {
            val remaining = (9 - p.first9Darts).coerceAtLeast(0)
            val toAdd = dartsUsed.coerceAtMost(remaining)
            p = p.copy(
                first9Score = p.first9Score + gained,
                first9Darts = p.first9Darts + toAdd
            )
        }
        if (scoreBefore > 170) {
            p = p.copy(
                nonCloseScore = p.nonCloseScore + gained,
                nonCloseDarts = p.nonCloseDarts + dartsUsed
            )
        }
        return applyCategory(p, gained)
    }

    // countDoubleAttempt = true (default) — считать попытку в дабл при попадании
    // в дабл-сектор. Для ручной игры.
    // countDoubleAttempt = false — для ботов: попытки считаются ТОЛЬКО когда
    // бот целился в дабл (делает Game501BotAI через recordDoublesAttempts).
    //
    // countDoubleHit = true (default) — считать "попадание в дабл" при
    // обнулении остатка. Для ручной игры.
    // countDoubleHit = false — для ботов: попадание считает Game501BotAI
    // через recordDoublesHit (когда дротик попал в ЦЕЛЕВОЙ дабл-сектор,
    // независимо от закрытия). Так D% = % попаданий в дабл-сектор.
    fun applyThrow(
        game: Game501,
        sector: Int,
        multiplier: ThrowMultiplier,
        countDoubleAttempt: Boolean = true,
        countDoubleHit: Boolean = true
    ): Game501 {
        if (game.isFinished) return game
        val playerIndex = game.currentPlayerIndex
        val player = game.players[playerIndex]
        if (multiplier == ThrowMultiplier.MISS) return registerMiss(game, playerIndex)

        val points = sector * multiplier.value
        val newScore = player.score - points
        val isDouble = multiplier == ThrowMultiplier.DOUBLE

        val canFinish = when (game.outMode) {
            OutMode.DOUBLE_OUT -> isDouble && newScore == 0
            OutMode.DOUBLE_IN_OUT -> isDouble && newScore == 0 && player.turnScore > 0
            OutMode.STRAIGHT_OUT -> newScore == 0
        }
        val isBust = when {
            newScore < 0 -> true
            newScore == 1 && requiresDoubleOut(game.outMode) -> true
            newScore == 0 && !canFinish && requiresDoubleOut(game.outMode) -> true
            else -> false
        }
        if (isBust) return registerBust(game, playerIndex)

        val updatedPlayers = game.players.toMutableList()
        val countAttempt = isDouble && countDoubleAttempt
        val countHit = isDouble && newScore == 0 && countDoubleHit
        var up = player.copy(
            score = newScore,
            turnScore = player.turnScore + points,
            turnDarts = player.turnDarts + 1,
            legDarts = player.legDarts + 1,
            legScoreGained = player.legScoreGained + points,
            matchDarts = player.matchDarts + 1,
            matchScoreGained = player.matchScoreGained + points,
            legDoublesAttempted = if (countAttempt) player.legDoublesAttempted + 1 else player.legDoublesAttempted,
            legDoublesHit = if (countHit) player.legDoublesHit + 1 else player.legDoublesHit,
            matchDoublesAttempted = if (countAttempt) player.matchDoublesAttempted + 1 else player.matchDoublesAttempted,
            matchDoublesHit = if (countHit) player.matchDoublesHit + 1 else player.matchDoublesHit
        )
        if (up.first9Darts < 9) {
            up = up.copy(
                first9Score = up.first9Score + points,
                first9Darts = up.first9Darts + 1
            )
        }
        if (player.score > 170) {
            up = up.copy(
                nonCloseScore = up.nonCloseScore + points,
                nonCloseDarts = up.nonCloseDarts + 1
            )
        }
        updatedPlayers[playerIndex] = up

        var updatedGame = game.copy(players = updatedPlayers)

        if (newScore == 0 && canFinish) {
            val closingValue = player.score
            val pWithClose = updatedGame.players[playerIndex].copy(
                listOfCloseValues = updatedGame.players[playerIndex].listOfCloseValues.toMutableList().also {
                    it.add(closingValue)
                }
            )
            val finalPlayers = updatedGame.players.toMutableList()
            finalPlayers[playerIndex] = pWithClose
            updatedGame = updatedGame.copy(players = finalPlayers)

            updatedGame = finishLeg(updatedGame, up.teamIndex)
        }
        return updatedGame
    }

    fun applyTurnScore(game: Game501, gained: Int): Game501 {
        if (game.isFinished) return game
        if (gained < 0) return game
        val playerIndex = game.currentPlayerIndex
        val player = game.players[playerIndex]
        if (gained == 0) {
            val updated = game.players.toMutableList()
            updated[playerIndex] = player.copy(turnScore = 0, turnDarts = 0)
            return game.copy(players = updated)
        }
        val newScore = player.score - gained
        if (newScore < 0 || (newScore == 1 && requiresDoubleOut(game.outMode))) {
            return registerBust(game, playerIndex)
        }
        val updated = game.players.toMutableList()
        var up = player.copy(
            score = newScore,
            turnScore = player.turnScore + gained,
            turnDarts = 3,
            legDarts = player.legDarts + 3,
            legScoreGained = player.legScoreGained + gained,
            matchDarts = player.matchDarts + 3,
            matchScoreGained = player.matchScoreGained + gained
        )
        up = registerApproach(up, gained, 3, player.score)
        updated[playerIndex] = up
        if (newScore == 0) {
            val closingValue = player.score
            val pwc = updated[playerIndex].copy(
                listOfCloseValues = updated[playerIndex].listOfCloseValues.toMutableList().also { it.add(closingValue) }
            )
            updated[playerIndex] = pwc
            return finishLeg(game.copy(players = updated), player.teamIndex)
        }
        return game.copy(players = updated)
    }

    fun applyTurnRemaining(game: Game501, remaining: Int): Game501 {
        val player = game.currentPlayer ?: return game
        return applyTurnScore(game, player.score - remaining)
    }

    fun closeLegManually(game: Game501, dartsUsed: Int, doublesAttempted: Int): Game501 {
        if (game.isFinished) return game
        val playerIndex = game.currentPlayerIndex
        val player = game.players[playerIndex]
        val gained = player.score
        val scoreBefore = player.score
        val attempts = if (doublesAttempted < 1) 1 else doublesAttempted
        val updated = game.players.toMutableList()
        var up = player.copy(
            score = 0, turnScore = player.turnScore + gained,
            turnDarts = player.turnDarts + dartsUsed,
            legDarts = player.legDarts + dartsUsed,
            legScoreGained = player.legScoreGained + gained,
            legDoublesHit = player.legDoublesHit + 1,
            legDoublesAttempted = player.legDoublesAttempted + attempts,
            matchDarts = player.matchDarts + dartsUsed,
            matchScoreGained = player.matchScoreGained + gained,
            matchDoublesHit = player.matchDoublesHit + 1,
            matchDoublesAttempted = player.matchDoublesAttempted + attempts
        )
        up = registerApproach(up, gained, dartsUsed, scoreBefore)
        up = up.copy(listOfCloseValues = up.listOfCloseValues.toMutableList().also { it.add(scoreBefore) })
        updated[playerIndex] = up
        return finishLeg(game.copy(players = updated), player.teamIndex)
    }

    fun recordDoublesAttempts(game: Game501, attempts: Int): Game501 {
        if (attempts <= 0) return game
        val idx = game.currentPlayerIndex
        if (idx !in game.players.indices) return game
        val p = game.players[idx]
        val updated = game.players.toMutableList()
        updated[idx] = p.copy(
            legDoublesAttempted = p.legDoublesAttempted + attempts,
            matchDoublesAttempted = p.matchDoublesAttempted + attempts
        )
        return game.copy(players = updated)
    }

    // Регистрирует попадание в дабл-сектор (для ботов: попадание в ЦЕЛЕВОЙ
    // дабл-сектор, независимо от того, обнулился ли остаток).
    fun recordDoublesHit(game: Game501, hits: Int): Game501 {
        if (hits <= 0) return game
        val idx = game.currentPlayerIndex
        if (idx !in game.players.indices) return game
        val p = game.players[idx]
        val updated = game.players.toMutableList()
        updated[idx] = p.copy(
            legDoublesHit = p.legDoublesHit + hits,
            matchDoublesHit = p.matchDoublesHit + hits
        )
        return game.copy(players = updated)
    }

    private fun registerMiss(game: Game501, playerIndex: Int): Game501 {
        val updated = game.players.toMutableList()
        val p = updated[playerIndex]
        var up = p.copy(turnDarts = p.turnDarts + 1, legDarts = p.legDarts + 1, matchDarts = p.matchDarts + 1)
        if (up.first9Darts < 9) up = up.copy(first9Darts = up.first9Darts + 1)
        if (p.score > 170) up = up.copy(nonCloseDarts = up.nonCloseDarts + 1)
        updated[playerIndex] = up
        return game.copy(players = updated)
    }

    private fun registerBust(game: Game501, playerIndex: Int): Game501 {
        val updated = game.players.toMutableList()
        val p = updated[playerIndex]
        val missing = (3 - p.turnDarts).coerceAtLeast(0)
        var up = p.copy(
            turnScore = 0, turnDarts = 0,
            legDarts = p.legDarts + missing,
            matchDarts = p.matchDarts + missing
        )
        if (up.first9Darts < 9 && missing > 0) {
            val toAdd = missing.coerceAtMost(9 - up.first9Darts)
            up = up.copy(first9Darts = up.first9Darts + toAdd)
        }
        if (p.score > 170 && missing > 0) {
            up = up.copy(nonCloseDarts = up.nonCloseDarts + missing)
        }
        updated[playerIndex] = up
        return game.copy(players = updated)
    }

    fun finishTurn(game: Game501): Game501 {
        if (game.isFinished) return game
        val idx = game.currentPlayerIndex
        val p = game.players[idx]
        val missing = (3 - p.turnDarts).coerceAtLeast(0)
        val updated = game.players.toMutableList()
        var up = p.copy(
            turnDarts = 0, turnScore = 0,
            legDarts = p.legDarts + missing,
            matchDarts = p.matchDarts + missing
        )
        if (up.first9Darts < 9 && missing > 0) {
            val toAdd = missing.coerceAtMost(9 - up.first9Darts)
            up = up.copy(first9Darts = up.first9Darts + toAdd)
        }
        if (p.score > 170 && missing > 0) {
            up = up.copy(nonCloseDarts = up.nonCloseDarts + missing)
        }
        updated[idx] = up
        return nextPlayer(game.copy(players = updated))
    }

    fun nextPlayer(game: Game501): Game501 {
        val next = (game.currentPlayerIndex + 1) % game.players.size
        return game.copy(currentPlayerIndex = next)
    }

    private fun finishLeg(game: Game501, winningTeam: Int): Game501 {
        val updated = game.players.toMutableList()
        for (i in updated.indices) {
            val p = updated[i]
            val ppr = if (p.legDarts > 0) p.legScoreGained.toDouble() / (p.legDarts / 3.0) else 0.0
            val f9 = if (p.first9Darts > 0) p.first9Score.toDouble() / (p.first9Darts / 3.0) else 0.0
            updated[i] = p.copy(
                listOfLegDarts = p.listOfLegDarts.toMutableList().also { it.add(p.legDarts) },
                listOfLegPpr = p.listOfLegPpr.toMutableList().also { it.add(ppr) },
                listOfFirst9Ppr = p.listOfFirst9Ppr.toMutableList().also { it.add(f9) }
            )
        }
        val snapshot = LegSnapshot501(
            setNumber = game.currentSetNumber,
            legNumber = game.currentLegNumber,
            winningTeam = winningTeam,
            players = updated.map { p ->
                LegPlayerSnapshot501(
                    name = p.name, teamIndex = p.teamIndex, isBot = p.isBot,
                    darts = p.legDarts, scoreGained = p.legScoreGained,
                    doublesHit = p.legDoublesHit, doublesAttempted = p.legDoublesAttempted,
                    count180 = p.legCount180, count170plus = p.legCount170plus,
                    count130plus = p.legCount130plus, count90plus = p.legCount90plus,
                    count57plus = p.legCount57plus, count57minus = p.legCount57minus
                )
            }
        )
        val currentLegs = updated.first { it.teamIndex == winningTeam }.legsInCurrentSet
        val newLegs = currentLegs + 1
        val setWon = newLegs >= game.legsPerSet
        val nextTeam = 1 - game.lastLegStartingTeam
        val nextIdx = game.players.indexOfFirst { it.teamIndex == nextTeam }.let { if (it >= 0) it else 0 }
        val newHistory = game.legHistory + snapshot

        if (setWon) {
            val curSets = updated.first { it.teamIndex == winningTeam }.setsWon
            val newSets = curSets + 1
            for (i in updated.indices) {
                if (updated[i].teamIndex == winningTeam) {
                    updated[i] = updated[i].copy(legsInCurrentSet = newLegs, setsWon = newSets)
                }
            }
            if (newSets >= game.setsPerMatch) {
                return game.copy(
                    players = updated, isFinished = true, winnerIndex = winningTeam,
                    lastLegWinnerIndex = winningTeam, lastSetWinnerIndex = winningTeam,
                    legHistory = newHistory
                )
            }
            val reset = updated.map { p -> resetLeg(p, game.gameType.startScore).copy(legsInCurrentSet = 0) }
            return game.copy(
                players = reset, currentPlayerIndex = nextIdx,
                currentLegNumber = 1, currentSetNumber = game.currentSetNumber + 1,
                lastLegWinnerIndex = winningTeam, lastSetWinnerIndex = winningTeam,
                lastLegStartingTeam = nextTeam, legHistory = newHistory
            )
        }
        for (i in updated.indices) {
            if (updated[i].teamIndex == winningTeam) {
                updated[i] = updated[i].copy(legsInCurrentSet = newLegs)
            }
        }
        val reset = updated.map { p -> resetLeg(p, game.gameType.startScore) }
        return game.copy(
            players = reset, currentPlayerIndex = nextIdx,
            currentLegNumber = game.currentLegNumber + 1,
            lastLegWinnerIndex = winningTeam,
            lastLegStartingTeam = nextTeam, legHistory = newHistory
        )
    }

    private fun resetLeg(p: Player501, startScore: Int): Player501 = p.copy(
        score = startScore, turnScore = 0, turnDarts = 0,
        legDarts = 0, legScoreGained = 0, legDoublesHit = 0, legDoublesAttempted = 0,
        legCount180 = 0, legCount170plus = 0, legCount130plus = 0,
        legCount90plus = 0, legCount57plus = 0, legCount57minus = 0,
        first9Score = 0, first9Darts = 0
    )

    private fun requiresDoubleOut(outMode: OutMode): Boolean =
        outMode == OutMode.DOUBLE_OUT || outMode == OutMode.DOUBLE_IN_OUT

    fun matchPpr(player: Player501): Double {
        if (player.matchDarts < 3) return 0.0
        return player.matchScoreGained.toDouble() / (player.matchDarts / 3.0)
    }
    fun doublesAccuracy(player: Player501): Double {
        if (player.matchDoublesAttempted <= 0) return 0.0
        return player.matchDoublesHit.toDouble() / player.matchDoublesAttempted * 100.0
    }
    fun first9Ppr(player: Player501): Double {
        if (player.listOfFirst9Ppr.isEmpty()) return 0.0
        return player.listOfFirst9Ppr.average()
    }
    fun nonClosePpr(player: Player501): Double {
        if (player.nonCloseDarts < 3) return 0.0
        return player.nonCloseScore.toDouble() / (player.nonCloseDarts / 3.0)
    }
}
