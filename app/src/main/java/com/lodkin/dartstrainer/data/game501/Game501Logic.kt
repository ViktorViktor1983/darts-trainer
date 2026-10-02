package com.lodkin.dartstrainer.data.game501

// Логика игры x01 — чистые функции без UI
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
                turnScore = 0,
                turnDarts = 0,
                legDarts = 0,
                legScoreGained = 0,
                legDoublesHit = 0,
                legDoublesAttempted = 0,
                legCount180 = 0,
                legCount170plus = 0,
                legCount130plus = 0,
                legCount90plus = 0,
                legCount57plus = 0,
                legCount57minus = 0
            )
        }

        return Game501(
            gameType = gameType,
            outMode = outMode,
            players = prepared,
            currentPlayerIndex = startingPlayerIndex,
            legsPerSet = legsPerSet,
            setsPerMatch = setsPerMatch,
            currentLegNumber = 1,
            currentSetNumber = 1,
            isPairGame = isPairGame,
            teamCount = 2,
            playersPerTeam = if (isPairGame) 2 else 1,
            lastLegStartingTeam = startingTeamIndex,
            autoOkSeconds = autoOkSeconds,
            sessionStartTime = sessionStartTime,
            sessionForm = sessionForm
        )
    }

    private fun applyCategory(player: Player501, gained: Int): Player501 {
        return when {
            gained == 180 -> player.copy(
                legCount180 = player.legCount180 + 1,
                matchCount180 = player.matchCount180 + 1
            )
            gained >= 170 -> player.copy(
                legCount170plus = player.legCount170plus + 1,
                matchCount170plus = player.matchCount170plus + 1
            )
            gained >= 130 -> player.copy(
                legCount130plus = player.legCount130plus + 1,
                matchCount130plus = player.matchCount130plus + 1
            )
            gained >= 90 -> player.copy(
                legCount90plus = player.legCount90plus + 1,
                matchCount90plus = player.matchCount90plus + 1
            )
            gained >= 57 -> player.copy(
                legCount57plus = player.legCount57plus + 1,
                matchCount57plus = player.matchCount57plus + 1
            )
            else -> player.copy(
                legCount57minus = player.legCount57minus + 1,
                matchCount57minus = player.matchCount57minus + 1
            )
        }
    }

    private fun registerApproach(
        player: Player501,
        gained: Int,
        dartsUsed: Int,
        scoreBefore: Int
    ): Player501 {
        var p = player
        if (p.first9Darts < 9) {
            val remainingSlots = (9 - p.first9Darts).coerceAtLeast(0)
            val dartsToAdd = dartsUsed.coerceAtMost(remainingSlots)
            p = p.copy(
                first9Score = p.first9Score + gained,
                first9Darts = p.first9Darts + dartsToAdd
            )
        }
        if (scoreBefore > 170) {
            p = p.copy(
                nonCloseScore = p.nonCloseScore + gained,
                nonCloseDarts = p.nonCloseDarts + dartsUsed
            )
        }
        p = applyCategory(p, gained)
        return p
    }

    fun applyThrow(game: Game501, sector: Int, multiplier: ThrowMultiplier): Game501 {
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
        var updatedPlayer = player.copy(
            score = newScore,
            turnScore = player.turnScore + points,
            turnDarts = player.turnDarts + 1,
            legDarts = player.legDarts + 1,
            legScoreGained = player.legScoreGained + points,
            matchDarts = player.matchDarts + 1,
            matchScoreGained = player.matchScoreGained + points,
            legDoublesAttempted = if (isDouble) player.legDoublesAttempted + 1 else player.legDoublesAttempted,
            legDoublesHit = if (isDouble) player.legDoublesHit + 1 else player.legDoublesHit,
            matchDoublesAttempted = if (isDouble) player.matchDoublesAttempted + 1 else player.matchDoublesAttempted,
            matchDoublesHit = if (isDouble) player.matchDoublesHit + 1 else player.matchDoublesHit
        )
        if (updatedPlayer.first9Darts < 9) {
            updatedPlayer = updatedPlayer.copy(
                first9Score = updatedPlayer.first9Score + points,
                first9Darts = updatedPlayer.first9Darts + 1
            )
        }
        if (player.score > 170) {
            updatedPlayer = updatedPlayer.copy(
                nonCloseScore = updatedPlayer.nonCloseScore + points,
                nonCloseDarts = updatedPlayer.nonCloseDarts + 1
            )
        }
        updatedPlayers[playerIndex] = updatedPlayer
        var updatedGame = game.copy(players = updatedPlayers)
        if (newScore == 0 && canFinish) {
            updatedGame = finishLeg(updatedGame, updatedPlayer.teamIndex)
        }
        return updatedGame
    }

    // ─────────────────────────────────────────────
    // Ввод суммы за подход. НЕ переключает игрока.
    // Переключение делает finishTurn() отдельно.
    // ─────────────────────────────────────────────
    fun applyTurnScore(game: Game501, gained: Int): Game501 {
        if (game.isFinished) return game
        if (gained < 0) return game
        val playerIndex = game.currentPlayerIndex
        val player = game.players[playerIndex]

        if (gained == 0) {
            // Все мимо — просто сбрасываем turnScore/turnDarts.
            // Остальные дротики добавит finishTurn().
            val updatedPlayers = game.players.toMutableList()
            updatedPlayers[playerIndex] = player.copy(
                turnScore = 0,
                turnDarts = 0
            )
            return game.copy(players = updatedPlayers)
        }

        val newScore = player.score - gained

        if (newScore < 0 || (newScore == 1 && requiresDoubleOut(game.outMode))) {
            return registerBust(game, playerIndex)
        }

        val updatedPlayers = game.players.toMutableList()
        var updatedPlayer = player.copy(
            score = newScore,
            turnScore = player.turnScore + gained,
            turnDarts = 3,
            legDarts = player.legDarts + 3,
            legScoreGained = player.legScoreGained + gained,
            matchDarts = player.matchDarts + 3,
            matchScoreGained = player.matchScoreGained + gained
        )
        updatedPlayer = registerApproach(updatedPlayer, gained, 3, player.score)
        updatedPlayers[playerIndex] = updatedPlayer

        if (newScore == 0) {
            val closingValue = player.score
            val pWithClose = updatedPlayers[playerIndex].copy(
                listOfCloseValues = updatedPlayers[playerIndex].listOfCloseValues.toMutableList().also {
                    it.add(closingValue)
                }
            )
            updatedPlayers[playerIndex] = pWithClose
            return finishLeg(game.copy(players = updatedPlayers), player.teamIndex)
        }
        return game.copy(players = updatedPlayers)
    }

    fun applyTurnRemaining(game: Game501, remaining: Int): Game501 {
        val player = game.currentPlayer ?: return game
        val gained = player.score - remaining
        return applyTurnScore(game, gained)
    }

    // ─────────────────────────────────────────────
    // Ручное закрытие лега (кнопка «Лег» или ответ в диалоге)
    // ─────────────────────────────────────────────
    fun closeLegManually(
        game: Game501,
        dartsUsed: Int,
        doublesAttempted: Int
    ): Game501 {
        if (game.isFinished) return game
        val playerIndex = game.currentPlayerIndex
        val player = game.players[playerIndex]

        val gained = player.score
        val scoreBefore = player.score
        val attemptsFixed = if (doublesAttempted < 1) 1 else doublesAttempted

        val updatedPlayers = game.players.toMutableList()
        var updatedPlayer = player.copy(
            score = 0,
            turnScore = player.turnScore + gained,
            turnDarts = player.turnDarts + dartsUsed,
            legDarts = player.legDarts + dartsUsed,
            legScoreGained = player.legScoreGained + gained,
            legDoublesHit = player.legDoublesHit + 1,
            legDoublesAttempted = player.legDoublesAttempted + attemptsFixed,
            matchDarts = player.matchDarts + dartsUsed,
            matchScoreGained = player.matchScoreGained + gained,
            matchDoublesHit = player.matchDoublesHit + 1,
            matchDoublesAttempted = player.matchDoublesAttempted + attemptsFixed
        )
        updatedPlayer = registerApproach(updatedPlayer, gained, dartsUsed, scoreBefore)
        updatedPlayer = updatedPlayer.copy(
            listOfCloseValues = updatedPlayer.listOfCloseValues.toMutableList().also {
                it.add(scoreBefore)
            }
        )
        updatedPlayers[playerIndex] = updatedPlayer
        return finishLeg(game.copy(players = updatedPlayers), player.teamIndex)
    }

    // ─────────────────────────────────────────────
    // Записать попытки в удвоение БЕЗ попадания
    // (используется когда игрок целился в дабл, но не закрыл лег)
    // ─────────────────────────────────────────────
    fun recordDoublesAttempts(game: Game501, attempts: Int): Game501 {
        if (attempts <= 0) return game
        val playerIndex = game.currentPlayerIndex
        if (playerIndex !in game.players.indices) return game
        val player = game.players[playerIndex]
        val updatedPlayers = game.players.toMutableList()
        updatedPlayers[playerIndex] = player.copy(
            legDoublesAttempted = player.legDoublesAttempted + attempts,
            matchDoublesAttempted = player.matchDoublesAttempted + attempts
        )
        return game.copy(players = updatedPlayers)
    }

    private fun registerMiss(game: Game501, playerIndex: Int): Game501 {
        val updatedPlayers = game.players.toMutableList()
        val player = updatedPlayers[playerIndex]
        var p = player.copy(
            turnDarts = player.turnDarts + 1,
            legDarts = player.legDarts + 1,
            matchDarts = player.matchDarts + 1
        )
        if (p.first9Darts < 9) p = p.copy(first9Darts = p.first9Darts + 1)
        if (player.score > 170) p = p.copy(nonCloseDarts = p.nonCloseDarts + 1)
        updatedPlayers[playerIndex] = p
        return game.copy(players = updatedPlayers)
    }

    // ─────────────────────────────────────────────
    // Перебор (bust) — НЕ переключает игрока, только сбрасывает состояние.
    // Переключение сделает finishTurn().
    // ─────────────────────────────────────────────
    private fun registerBust(game: Game501, playerIndex: Int): Game501 {
        val updatedPlayers = game.players.toMutableList()
        val player = updatedPlayers[playerIndex]
        updatedPlayers[playerIndex] = player.copy(
            turnScore = 0,
            turnDarts = 0
        )
        return game.copy(players = updatedPlayers)
    }

    // ─────────────────────────────────────────────
    // Завершение подхода: добавляет недостающие промахи и переключает игрока
    // ─────────────────────────────────────────────
    fun finishTurn(game: Game501): Game501 {
        if (game.isFinished) return game
        val playerIndex = game.currentPlayerIndex
        val player = game.players[playerIndex]
        val missing = (3 - player.turnDarts).coerceAtLeast(0)
        val updatedPlayers = game.players.toMutableList()
        var p = player.copy(
            turnDarts = 0,
            turnScore = 0,
            legDarts = player.legDarts + missing,
            matchDarts = player.matchDarts + missing
        )
        if (p.first9Darts < 9 && missing > 0) {
            val dartsToAdd = missing.coerceAtMost(9 - p.first9Darts)
            p = p.copy(first9Darts = p.first9Darts + dartsToAdd)
        }
        if (player.score > 170 && missing > 0) {
            p = p.copy(nonCloseDarts = p.nonCloseDarts + missing)
        }
        updatedPlayers[playerIndex] = p
        return nextPlayer(game.copy(players = updatedPlayers))
    }

    fun nextPlayer(game: Game501): Game501 {
        val next = (game.currentPlayerIndex + 1) % game.players.size
        return game.copy(currentPlayerIndex = next)
    }

    private fun finishLeg(game: Game501, winningTeam: Int): Game501 {
        val updatedPlayers = game.players.toMutableList()
        for (i in updatedPlayers.indices) {
            val p = updatedPlayers[i]
            val ppr = if (p.legDarts > 0) p.legScoreGained.toDouble() / (p.legDarts / 3.0) else 0.0
            val newListDarts = p.listOfLegDarts.toMutableList().also { it.add(p.legDarts) }
            val newListPpr = p.listOfLegPpr.toMutableList().also { it.add(ppr) }
            updatedPlayers[i] = p.copy(
                listOfLegDarts = newListDarts,
                listOfLegPpr = newListPpr
            )
        }

        val snapshot = LegSnapshot501(
            setNumber = game.currentSetNumber,
            legNumber = game.currentLegNumber,
            winningTeam = winningTeam,
            players = updatedPlayers.map { p ->
                LegPlayerSnapshot501(
                    name = p.name,
                    teamIndex = p.teamIndex,
                    isBot = p.isBot,
                    darts = p.legDarts,
                    scoreGained = p.legScoreGained,
                    doublesHit = p.legDoublesHit,
                    doublesAttempted = p.legDoublesAttempted,
                    count180 = p.legCount180,
                    count170plus = p.legCount170plus,
                    count130plus = p.legCount130plus,
                    count90plus = p.legCount90plus,
                    count57plus = p.legCount57plus,
                    count57minus = p.legCount57minus
                )
            }
        )

        val currentLegs = updatedPlayers.first { it.teamIndex == winningTeam }.legsInCurrentSet
        val newLegsInCurrentSet = currentLegs + 1
        val setWon = newLegsInCurrentSet >= game.legsPerSet

        val nextStartingTeam = 1 - game.lastLegStartingTeam
        val nextStartingPlayerIndex = game.players.indexOfFirst { it.teamIndex == nextStartingTeam }
            .let { if (it >= 0) it else 0 }

        val newHistory = game.legHistory + snapshot

        if (setWon) {
            val currentSets = updatedPlayers.first { it.teamIndex == winningTeam }.setsWon
            val newSetsWon = currentSets + 1
            for (i in updatedPlayers.indices) {
                if (updatedPlayers[i].teamIndex == winningTeam) {
                    updatedPlayers[i] = updatedPlayers[i].copy(
                        legsInCurrentSet = newLegsInCurrentSet,
                        setsWon = newSetsWon
                    )
                }
            }
            if (newSetsWon >= game.setsPerMatch) {
                return game.copy(
                    players = updatedPlayers,
                    isFinished = true,
                    winnerIndex = winningTeam,
                    lastLegWinnerIndex = winningTeam,
                    lastSetWinnerIndex = winningTeam,
                    legHistory = newHistory
                )
            }
            val resetPlayers = updatedPlayers.map { p ->
                resetLegStats(p, game.gameType.startScore).copy(legsInCurrentSet = 0)
            }
            return game.copy(
                players = resetPlayers,
                currentPlayerIndex = nextStartingPlayerIndex,
                currentLegNumber = 1,
                currentSetNumber = game.currentSetNumber + 1,
                lastLegWinnerIndex = winningTeam,
                lastSetWinnerIndex = winningTeam,
                lastLegStartingTeam = nextStartingTeam,
                legHistory = newHistory
            )
        }

        for (i in updatedPlayers.indices) {
            if (updatedPlayers[i].teamIndex == winningTeam) {
                updatedPlayers[i] = updatedPlayers[i].copy(legsInCurrentSet = newLegsInCurrentSet)
            }
        }
        val resetPlayers = updatedPlayers.map { p -> resetLegStats(p, game.gameType.startScore) }
        return game.copy(
            players = resetPlayers,
            currentPlayerIndex = nextStartingPlayerIndex,
            currentLegNumber = game.currentLegNumber + 1,
            lastLegWinnerIndex = winningTeam,
            lastSetWinnerIndex = null,
            lastLegStartingTeam = nextStartingTeam,
            legHistory = newHistory
        )
    }

    private fun resetLegStats(p: Player501, startScore: Int): Player501 {
        return p.copy(
            score = startScore,
            turnScore = 0,
            turnDarts = 0,
            legDarts = 0,
            legScoreGained = 0,
            legDoublesHit = 0,
            legDoublesAttempted = 0,
            legCount180 = 0,
            legCount170plus = 0,
            legCount130plus = 0,
            legCount90plus = 0,
            legCount57plus = 0,
            legCount57minus = 0
        )
    }

    private fun requiresDoubleOut(outMode: OutMode): Boolean =
        outMode == OutMode.DOUBLE_OUT || outMode == OutMode.DOUBLE_IN_OUT

    fun legPpr(player: Player501): Double {
        if (player.legDarts < 3) return 0.0
        return player.legScoreGained.toDouble() / (player.legDarts / 3.0)
    }

    fun matchPpr(player: Player501): Double {
        if (player.matchDarts < 3) return 0.0
        return player.matchScoreGained.toDouble() / (player.matchDarts / 3.0)
    }

    fun doublesAccuracy(player: Player501): Double {
        if (player.matchDoublesAttempted <= 0) return 0.0
        return player.matchDoublesHit.toDouble() / player.matchDoublesAttempted * 100.0
    }

    fun first9Ppr(player: Player501): Double {
        if (player.first9Darts < 3) return 0.0
        return player.first9Score.toDouble() / (player.first9Darts / 3.0)
    }

    fun nonClosePpr(player: Player501): Double {
        if (player.nonCloseDarts < 3) return 0.0
        return player.nonCloseScore.toDouble() / (player.nonCloseDarts / 3.0)
    }
}
