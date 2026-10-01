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

    private fun finalizeTurn(player: CricketPlayer): CricketPlayer {
        if (player.turnMarks <= 0 && player.turnMisses <= 0) {
            return player.copy(turnMarks = 0, turnMisses = 0, turnTriples = 0)
        }
        val perfect = if (player.turnMarks >= 8) 1 else 0
        val strong = if (player.turnMarks in 6..7) 1 else 0
        return player.copy(
            turnMarks = 0,
            turnMisses = 0,
            turnTriples = 0,
            matchPerfectRounds = player.matchPerfectRounds + perfect,
            matchStrongRounds = player.matchStrongRounds + strong
        )
    }

    private fun resetPlayerForNewLeg(player: CricketPlayer): CricketPlayer {
        val finalized = finalizeTurn(player)
        return finalized.copy(
            hits = CricketSector.ALL.associateWith { 0 }.toMutableMap(),
            scores = CricketSector.ALL.associateWith { 0 }.toMutableMap(),
            totalScore = 0,
            dartsThrown = 0,
            legMarks = 0,
            legMisses = 0,
            legTriples = 0,
            turnMarks = 0,
            turnMisses = 0,
            turnTriples = 0
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
                matchMissesThrown = player.matchMissesThrown + 1,
                legMisses = player.legMisses + 1,
                turnMisses = player.turnMisses + 1
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
            legTriples = if (isTriple) player.legTriples + 1 else player.legTriples,
            turnMarks = player.turnMarks + multiplier,
            turnTriples = if (isTriple) player.turnTriples + 1 else player.turnTriples,
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
                matchMissesThrown = player.matchMissesThrown + missing,
                legMisses = player.legMisses + missing,
                turnMisses = player.turnMisses + missing
            )
        }

        return nextPlayer(game.copy(players = updatedPlayers))
    }

    // ─────────────────────────────────────────────
    // Корректировка после диалога «Сколько дротиков ушло на закрытие?»
    // realDarts = сколько реально потратил игрок (1, 2 или 3)
    // Убирает лишние промахи из статистики игрока И из снимка лега
    // ─────────────────────────────────────────────
    fun adjustLastLegDarts(game: CricketGame, realDarts: Int): CricketGame {
        val winningPlayerIndex = game.lastLegWinnerPlayerIndex ?: return game
        if (winningPlayerIndex !in game.players.indices) return game

        val updated = game.players.toMutableList()
        val player = updated[winningPlayerIndex]

        val programDarts = game.lastLegDartsClicked
        val extra = programDarts - realDarts
        if (extra <= 0) return game

        val newDarts = (player.dartsThrown - extra).coerceAtLeast(0)
        val newMatchDarts = (player.matchDartsThrown - extra).coerceAtLeast(0)
        val newMisses = (player.legMisses - extra).coerceAtLeast(0)
        val newMatchMisses = (player.matchMissesThrown - extra).coerceAtLeast(0)

        updated[winningPlayerIndex] = player.copy(
            dartsThrown = newDarts,
            matchDartsThrown = newMatchDarts,
            legMisses = newMisses,
            matchMissesThrown = newMatchMisses
        )

        // Корректируем последний снимок в истории (только что добавленный)
        val newHistory = if (game.legHistory.isNotEmpty()) {
            val last = game.legHistory.last()
            val newPlayers = last.players.mapIndexed { i, snap ->
                if (i == winningPlayerIndex) {
                    snap.copy(
                        darts = (snap.darts - extra).coerceAtLeast(0),
                        misses = (snap.misses - extra).coerceAtLeast(0)
                    )
                } else snap
            }
            game.legHistory.dropLast(1) + last.copy(players = newPlayers)
        } else game.legHistory

        return game.copy(players = updated, legHistory = newHistory)
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

        val lastPlayerIdx = game.currentPlayerIndex
        if (lastPlayerIdx in updatedPlayers.indices) {
            updatedPlayers[lastPlayerIdx] = finalizeTurn(updatedPlayers[lastPlayerIdx])
        }

        val snapshot = LegSnapshot(
            setNumber = game.currentSetNumber,
            legNumber = game.currentLegNumber,
            winningTeam = winningTeam,
            players = updatedPlayers.map { p ->
                LegPlayerSnapshot(
                    name = p.name,
                    teamIndex = p.teamIndex,
                    isBot = p.isBot,
                    legMarks = p.legMarks,
                    darts = p.dartsThrown,
                    misses = p.legMisses,
                    triples = p.legTriples,
                    score = p.totalScore
                )
            }
        )

        val currentLegs = updatedPlayers.first { it.teamIndex == winningTeam }.legsInCurrentSet
        val newLegsInCurrentSet = currentLegs + 1
        val setWon = newLegsInCurrentSet >= game.legsPerSet

        val nextStartingTeam = 1 - game.lastLegStartingTeam
        val nextStartingPlayerIndex = game.players
            .indexOfFirst { it.teamIndex == nextStartingTeam }
            .let { if (it >= 0) it else 0 }

        val newHistory = game.legHistory + snapshot
        val winnerPlayerIndex = game.currentPlayerIndex

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
                    lastSetWinnerIndex = winningTeam,
                    lastLegWinnerPlayerIndex = winnerPlayerIndex,
                    lastLegDartsClicked = game.currentTurnDarts,
                    legHistory = newHistory
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
                lastLegStartingTeam = nextStartingTeam,
                lastLegWinnerPlayerIndex = winnerPlayerIndex,
                lastLegDartsClicked = game.currentTurnDarts,
                legHistory = newHistory
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
            lastLegStartingTeam = nextStartingTeam,
            lastLegWinnerPlayerIndex = winnerPlayerIndex,
            lastLegDartsClicked = game.currentTurnDarts,
            legHistory = newHistory
        )
    }

    fun nextPlayer(game: CricketGame): CricketGame {
        val updatedPlayers = game.players.toMutableList()
        val playerIndex = game.currentPlayerIndex
        if (playerIndex in updatedPlayers.indices) {
            updatedPlayers[playerIndex] = finalizeTurn(updatedPlayers[playerIndex])
        }
        val next = (game.currentPlayerIndex + 1) % game.players.size
        return game.copy(
            players = updatedPlayers,
            currentPlayerIndex = next,
            currentTurnDarts = 0
        )
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
            sessionForm = sessionForm,
            legHistory = emptyList()
        )
    }
}
