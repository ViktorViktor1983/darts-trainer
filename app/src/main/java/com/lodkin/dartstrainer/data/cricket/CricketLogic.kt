package com.lodkin.dartstrainer.data.cricket

// Результат одного броска
enum class ThrowResult {
    SINGLE,     // S — попал в сектор (1 раз)
    DOUBLE,     // D — попал в удвоение (2 раза)
    TRIPLE,     // T — попал в утроение (3 раза)
    MISS        // мимо (0 раз)
}

// Логика крикета — чистые функции без UI
object CricketLogic {

    fun multiplierOf(result: ThrowResult): Int = when (result) {
        ThrowResult.SINGLE -> 1
        ThrowResult.DOUBLE -> 2
        ThrowResult.TRIPLE -> 3
        ThrowResult.MISS -> 0
    }

    fun isClosed(player: CricketPlayer, sector: CricketSector): Boolean {
        return (player.hits[sector] ?: 0) >= 3
    }

    fun isClosedByTeam(game: CricketGame, team: Int, sector: CricketSector): Boolean {
        val totalHits = game.playersOfTeam(team).sumOf { it.hits[sector] ?: 0 }
        return totalHits >= 3
    }

    fun isClosedByAllTeams(game: CricketGame, sector: CricketSector): Boolean {
        return (0 until game.teamCount).all { isClosedByTeam(game, it, sector) }
    }

    fun hasClosedAllTeam(game: CricketGame, team: Int): Boolean {
        return CricketSector.ALL.all { isClosedByTeam(game, team, it) }
    }

    fun teamTotalScore(game: CricketGame, team: Int): Int {
        return game.playersOfTeam(team).sumOf { it.totalScore }
    }

    fun teamMatchScore(game: CricketGame, team: Int): Int {
        return game.playersOfTeam(team).sumOf { it.matchTotalScore }
    }

    private fun resetPlayerForNewLeg(player: CricketPlayer): CricketPlayer {
        return player.copy(
            hits = CricketSector.ALL.associateWith { 0 }.toMutableMap(),
            scores = CricketSector.ALL.associateWith { 0 }.toMutableMap(),
            totalScore = 0,
            dartsThrown = 0,
            legMarks = 0
        )
    }

    fun applyThrow(
        game: CricketGame,
        sector: CricketSector,
        result: ThrowResult,
        playerIndex: Int
    ): CricketGame {
        if (result == ThrowResult.MISS) {
            val updatedPlayers = game.players.toMutableList()
            val player = updatedPlayers[playerIndex]
            updatedPlayers[playerIndex] = player.copy(
                dartsThrown = player.dartsThrown + 1,
                matchDartsThrown = player.matchDartsThrown + 1,
                matchMissesThrown = player.matchMissesThrown + 1
            )
            return game.copy(
                players = updatedPlayers,
                currentTurnDarts = game.currentTurnDarts + 1
            )
        }

        val multiplier = multiplierOf(result)
        val updatedPlayers = game.players.toMutableList()
        val player = updatedPlayers[playerIndex]

        val teamHitsBefore = game.playersOfTeam(player.teamIndex)
            .sumOf { it.hits[sector] ?: 0 }
        val teamAlreadyClosed = teamHitsBefore >= 3

        val overflow: Int = if (teamAlreadyClosed) {
            multiplier
        } else {
            maxOf(0, teamHitsBefore + multiplier - 3)
        }

        val currentHits = player.hits[sector] ?: 0
        val newHits = minOf(currentHits + multiplier, 3)

        val newHitsMap = player.hits.toMutableMap()
        newHitsMap[sector] = newHits

        val newScores = player.scores.toMutableMap()
        var scoreGained = 0

        if (game.type == CricketType.AMERICAN && overflow > 0) {
            val myTeam = player.teamIndex
            val otherTeams = (0 until game.teamCount).filter { it != myTeam }
            val anyOtherTeamClosed = otherTeams.any { isClosedByTeam(game, it, sector) }

            if (!anyOtherTeamClosed) {
                val sectorValue = if (sector == CricketSector.BULL) 25 else sector.number
                scoreGained = overflow * sectorValue
                newScores[sector] = (newScores[sector] ?: 0) + scoreGained
            }
        }

        val newMatchHits = player.matchHits.toMutableMap()
        newMatchHits[sector] = (newMatchHits[sector] ?: 0) + multiplier

        val newMatchScores = player.matchScores.toMutableMap()
        if (scoreGained > 0) {
            newMatchScores[sector] = (newMatchScores[sector] ?: 0) + scoreGained
        }

        val isTriple = result == ThrowResult.TRIPLE

        updatedPlayers[playerIndex] = player.copy(
            hits = newHitsMap,
            scores = newScores,
            totalScore = player.totalScore + scoreGained,
            dartsThrown = player.dartsThrown + 1,
            legMarks = player.legMarks + multiplier,
            matchHits = newMatchHits,
            matchScores = newMatchScores,
            matchTotalScore = player.matchTotalScore + scoreGained,
            matchDartsThrown = player.matchDartsThrown + 1,
            matchTriplesHit = if (isTriple) player.matchTriplesHit + 1 else player.matchTriplesHit
        )

        val updatedGame = game.copy(
            players = updatedPlayers,
            currentTurnDarts = game.currentTurnDarts + 1
        )

        val legWinnerTeam = checkLegWinner(updatedGame)
        if (legWinnerTeam != null) {
            return awardLegWin(updatedGame, legWinnerTeam)
        }

        return updatedGame
    }

    fun finishTurn(game: CricketGame): CricketGame {
        if (game.isFinished) return game
        val missing = (3 - game.currentTurnDarts).coerceAtLeast(0)
        val updatedPlayers = game.players.toMutableList()
        val playerIndex = game.currentPlayerIndex
        val player = updatedPlayers[playerIndex]

        if (missing > 0) {
            updatedPlayers[playerIndex] = player.copy(
                dartsThrown = player.dartsThrown + missing,
                matchDartsThrown = player.matchDartsThrown + missing,
                matchMissesThrown = player.matchMissesThrown + missing
            )
        }

        return nextPlayer(game.copy(players = updatedPlayers))
    }

    fun checkLegWinner(game: CricketGame): Int? {
        if (game.players.isEmpty()) return null
        val closedAllTeams = (0 until game.teamCount).filter { hasClosedAllTeam(game, it) }
        if (closedAllTeams.isEmpty()) return null
        if (game.type == CricketType.NO_SCORE) return closedAllTeams.first()
        val maxScore = closedAllTeams.maxOf { teamTotalScore(game, it) }
        return closedAllTeams.firstOrNull { teamTotalScore(game, it) == maxScore }
    }

    fun awardLegWin(game: CricketGame, winningTeam: Int): CricketGame {
        val updatedPlayers = game.players.toMutableList()
        val currentLegs = updatedPlayers.first { it.teamIndex == winningTeam }.legsInCurrentSet
        val newLegsInCurrentSet = currentLegs + 1
        val setWon = newLegsInCurrentSet >= game.legsPerSet

        val nextStartingTeam = 1 - game.lastLegStartingTeam
        val nextStartingPlayerIndex = game.players
            .indexOfFirst { it.teamIndex == nextStartingTeam }
            .let { if (it >= 0) it else 0 }

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
                    lastLegWinnerIndex = null,
                    lastSetWinnerIndex = winningTeam
                )
            }
            val resetPlayers = updatedPlayers.map { p ->
                resetPlayerForNewLeg(p).copy(legsInCurrentSet = 0)
            }
            return game.copy(
                players = resetPlayers,
                currentPlayerIndex = nextStartingPlayerIndex,
                currentTurnDarts = 0,
                currentLegNumber = 1,
                currentSetNumber = game.currentSetNumber + 1,
                lastLegWinnerIndex = null,
                lastSetWinnerIndex = winningTeam,
                lastLegStartingTeam = nextStartingTeam
            )
        }

        for (i in updatedPlayers.indices) {
            if (updatedPlayers[i].teamIndex == winningTeam) {
                updatedPlayers[i] = updatedPlayers[i].copy(legsInCurrentSet = newLegsInCurrentSet)
            }
        }
        val resetPlayers = updatedPlayers.map { resetPlayerForNewLeg(it) }
        return game.copy(
            players = resetPlayers,
            currentPlayerIndex = nextStartingPlayerIndex,
            currentTurnDarts = 0,
            currentLegNumber = game.currentLegNumber + 1,
            lastLegWinnerIndex = winningTeam,
            lastSetWinnerIndex = null,
            lastLegStartingTeam = nextStartingTeam
        )
    }

    fun nextPlayer(game: CricketGame): CricketGame {
        val next = (game.currentPlayerIndex + 1) % game.players.size
        return game.copy(currentPlayerIndex = next, currentTurnDarts = 0)
    }

    fun newGame(
        type: CricketType,
        players: List<CricketPlayer>,
        legsPerSet: Int = 1,
        setsPerMatch: Int = 1,
        isPairGame: Boolean = false,
        startingTeamIndex: Int = 0,
        autoOkSeconds: Int = 0,
        sessionStartTime: Long = 0L,
        sessionForm: Double = 1.0
    ): CricketGame {
        val startingPlayerIndex = players
            .indexOfFirst { it.teamIndex == startingTeamIndex }
            .let { if (it >= 0) it else 0 }

        return CricketGame(
            type = type,
            players = players,
            currentPlayerIndex = startingPlayerIndex,
            isFinished = false,
            winnerIndex = null,
            currentTurnDarts = 0,
            legsPerSet = legsPerSet,
            setsPerMatch = setsPerMatch,
            currentLegNumber = 1,
            currentSetNumber = 1,
            lastLegWinnerIndex = null,
            lastSetWinnerIndex = null,
            isPairGame = isPairGame,
            teamCount = 2,
            playersPerTeam = if (isPairGame) 2 else 1,
            lastLegStartingTeam = startingTeamIndex,
            autoOkSeconds = autoOkSeconds,
            sessionStartTime = sessionStartTime,
            sessionForm = sessionForm
        )
    }
}
