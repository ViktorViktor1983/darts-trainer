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

    fun isClosedByAll(players: List<CricketPlayer>, sector: CricketSector): Boolean {
        return players.all { isClosed(it, sector) }
    }

    fun hasClosedAll(player: CricketPlayer): Boolean {
        return CricketSector.ALL.all { isClosed(player, it) }
    }

    // Сброс состояния игрока для нового лега
    private fun resetPlayerForNewLeg(player: CricketPlayer): CricketPlayer {
        return player.copy(
            hits = CricketSector.ALL.associateWith { 0 }.toMutableMap(),
            scores = CricketSector.ALL.associateWith { 0 }.toMutableMap(),
            totalScore = 0
        )
    }

    // Обработка одного броска
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
                dartsThrown = player.dartsThrown + 1
            )
            return game.copy(
                players = updatedPlayers,
                currentTurnDarts = game.currentTurnDarts + 1
            )
        }

        val multiplier = multiplierOf(result)
        val updatedPlayers = game.players.toMutableList()
        val player = updatedPlayers[playerIndex]

        val currentHits = player.hits[sector] ?: 0
        val hitsToAdd = minOf(multiplier, 3 - currentHits)
        val newHits = currentHits + hitsToAdd
        val overflow = multiplier - hitsToAdd

        val newHitsMap = player.hits.toMutableMap()
        newHitsMap[sector] = newHits

        val newScores = player.scores.toMutableMap()
        var scoreGained = 0

        if (game.type == CricketType.AMERICAN && overflow > 0) {
            val otherPlayers = updatedPlayers.filterIndexed { i, _ -> i != playerIndex }
            val anyOtherClosed = otherPlayers.any { isClosed(it, sector) }

            if (!anyOtherClosed) {
                val sectorValue = if (sector == CricketSector.BULL) 25 else sector.number
                scoreGained = overflow * sectorValue
                newScores[sector] = (newScores[sector] ?: 0) + scoreGained
            }
        }

        updatedPlayers[playerIndex] = player.copy(
            hits = newHitsMap,
            scores = newScores,
            totalScore = player.totalScore + scoreGained,
            dartsThrown = player.dartsThrown + 1
        )

        val updatedGame = game.copy(
            players = updatedPlayers,
            currentTurnDarts = game.currentTurnDarts + 1
        )

        // Проверка победы в ЛЕГЕ
        val legWinner = checkLegWinner(updatedPlayers, game.type)
        if (legWinner != null) {
            return awardLegWin(updatedGame, legWinner)
        }

        return updatedGame
    }

    // Определение победителя ЛЕГА
    fun checkLegWinner(players: List<CricketPlayer>, type: CricketType): Int? {
        if (players.isEmpty()) return null

        val closedAllIndices = players.indices.filter { i ->
            hasClosedAll(players[i])
        }

        if (closedAllIndices.isEmpty()) return null

        if (type == CricketType.NO_SCORE) {
            return closedAllIndices.first()
        }

        val maxScore = players.maxOf { it.totalScore }
        val winner = closedAllIndices.firstOrNull { i ->
            players[i].totalScore == maxScore
        }

        return winner
    }

    // Присуждение победы в леге и продвижение по сетам/матчу
    fun awardLegWin(game: CricketGame, legWinnerIndex: Int): CricketGame {
        val updatedPlayers = game.players.toMutableList()
        val winner = updatedPlayers[legWinnerIndex]

        val newLegsInCurrentSet = winner.legsInCurrentSet + 1
        val setWon = newLegsInCurrentSet >= game.legsPerSet

        updatedPlayers[legWinnerIndex] = winner.copy(
            legsInCurrentSet = newLegsInCurrentSet
        )

        if (setWon) {
            val newSetsWon = updatedPlayers[legWinnerIndex].setsWon + 1
            updatedPlayers[legWinnerIndex] = updatedPlayers[legWinnerIndex].copy(
                setsWon = newSetsWon
            )

            if (newSetsWon >= game.setsPerMatch) {
                // Победа в МАТЧЕ
                return game.copy(
                    players = updatedPlayers,
                    isFinished = true,
                    winnerIndex = legWinnerIndex,
                    lastLegWinnerIndex = null,
                    lastSetWinnerIndex = legWinnerIndex
                )
            }

            // Новый сет
            val resetPlayers = updatedPlayers.map { p ->
                resetPlayerForNewLeg(p).copy(legsInCurrentSet = 0)
            }
            return game.copy(
                players = resetPlayers,
                currentPlayerIndex = 0,
                currentTurnDarts = 0,
                currentLegNumber = 1,
                currentSetNumber = game.currentSetNumber + 1,
                lastLegWinnerIndex = null,
                lastSetWinnerIndex = legWinnerIndex
            )
        }

        // Новый лег
        val resetPlayers = updatedPlayers.map { p -> resetPlayerForNewLeg(p) }
        return game.copy(
            players = resetPlayers,
            currentPlayerIndex = 0,
            currentTurnDarts = 0,
            currentLegNumber = game.currentLegNumber + 1,
            lastLegWinnerIndex = legWinnerIndex,
            lastSetWinnerIndex = null
        )
    }

    fun nextPlayer(game: CricketGame): CricketGame {
        val next = (game.currentPlayerIndex + 1) % game.players.size
        return game.copy(
            currentPlayerIndex = next,
            currentTurnDarts = 0
        )
    }

    fun newGame(
        type: CricketType,
        players: List<CricketPlayer>,
        legsPerSet: Int = 1,
        setsPerMatch: Int = 1
    ): CricketGame {
        return CricketGame(
            type = type,
            players = players,
            currentPlayerIndex = 0,
            isFinished = false,
            winnerIndex = null,
            currentTurnDarts = 0,
            legsPerSet = legsPerSet,
            setsPerMatch = setsPerMatch,
            currentLegNumber = 1,
            currentSetNumber = 1,
            lastLegWinnerIndex = null,
            lastSetWinnerIndex = null
        )
    }
}
