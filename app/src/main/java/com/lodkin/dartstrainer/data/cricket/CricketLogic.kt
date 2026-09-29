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

    // Все ли сектора закрыты у одного игрока
    fun hasClosedAll(player: CricketPlayer): Boolean {
        return CricketSector.ALL.all { isClosed(player, it) }
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

        var newScores = player.scores.toMutableMap()
        var scoreGained = 0

        // Начисляем очки только в American, и только если есть overflow
        if (game.type == CricketType.AMERICAN && overflow > 0) {
            // Проверяем: сектор закрыт у соперника?
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

        // Проверка победы
        val winner = checkWinner(updatedPlayers, game.type)
        if (winner != null) {
            return updatedGame.copy(
                isFinished = true,
                winnerIndex = winner
            )
        }

        return updatedGame
    }

    // Определение победителя
    fun checkWinner(players: List<CricketPlayer>, type: CricketType): Int? {
        if (players.isEmpty()) return null

        val closedAllIndices = players.indices.filter { i ->
            hasClosedAll(players[i])
        }

        if (closedAllIndices.isEmpty()) return null

        if (type == CricketType.NO_SCORE) {
            // Без очков: побеждает первый, кто закрыл всё
            return closedAllIndices.first()
        }

        // American: побеждает тот, кто закрыл всё И имеет больше всех очков
        val maxScore = players.maxOf { it.totalScore }

        // Ищем игрока, который закрыл всё И имеет максимальный счёт
        val winner = closedAllIndices.firstOrNull { i ->
            players[i].totalScore == maxScore
        }

        return winner
    }

    fun nextPlayer(game: CricketGame): CricketGame {
        val next = (game.currentPlayerIndex + 1) % game.players.size
        return game.copy(
            currentPlayerIndex = next,
            currentTurnDarts = 0
        )
    }

    fun newGame(type: CricketType, players: List<CricketPlayer>): CricketGame {
        return CricketGame(
            type = type,
            players = players,
            currentPlayerIndex = 0,
            isFinished = false,
            winnerIndex = null,
            currentTurnDarts = 0
        )
    }
}
