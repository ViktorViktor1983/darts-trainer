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

    // Сколько раз засчитывается попадание
    fun multiplierOf(result: ThrowResult): Int = when (result) {
        ThrowResult.SINGLE -> 1
        ThrowResult.DOUBLE -> 2
        ThrowResult.TRIPLE -> 3
        ThrowResult.MISS -> 0
    }

    // Проверка: закрыт ли сектор у игрока
    fun isClosed(player: CricketPlayer, sector: CricketSector): Boolean {
        return (player.hits[sector] ?: 0) >= 3
    }

    // Проверка: закрыт ли сектор у всех игроков
    fun isClosedByAll(players: List<CricketPlayer>, sector: CricketSector): Boolean {
        return players.all { isClosed(it, sector) }
    }

    // Проверка: закрыты ли все сектора у всех игроков
    fun isGameFinished(players: List<CricketPlayer>): Boolean {
        return CricketSector.ALL.all { sector ->
            players.all { isClosed(it, sector) }
        }
    }

    // Обработка одного броска
    // Возвращает новое состояние игрока после броска
    fun applyThrow(
        game: CricketGame,
        sector: CricketSector,
        result: ThrowResult,
        playerIndex: Int
    ): CricketGame {
        if (result == ThrowResult.MISS) {
            // Просто увеличиваем счётчик бросков
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

        // Текущее количество попаданий в сектор
        val currentHits = player.hits[sector] ?: 0
        // Сколько попаданий добавляем (не больше 3 всего)
        val hitsToAdd = minOf(multiplier, 3 - currentHits)
        val newHits = currentHits + hitsToAdd

        // Сколько попаданий "перешло" в очки (сверх 3)
        val overflow = multiplier - hitsToAdd

        // Обновляем попадания
        val newHitsMap = player.hits.toMutableMap()
        newHitsMap[sector] = newHits

        // Считаем очки
        var newScores = player.scores.toMutableMap()
        var scoreGained = 0

        if (game.type == CricketType.AMERICAN && overflow > 0) {
            // Проверяем: закрыт ли сектор у соперника
            val otherPlayers = updatedPlayers.filterIndexed { i, _ -> i != playerIndex }
            val anyOtherClosed = otherPlayers.any { isClosed(it, sector) }

            if (!anyOtherClosed) {
                // Начисляем очки
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

        // Проверка: игра закончена?
        if (isGameFinished(updatedPlayers)) {
            // Побеждает тот, у кого больше очков
            val winner = updatedPlayers.indices.maxByOrNull { updatedPlayers[it].totalScore }
            return updatedGame.copy(
                isFinished = true,
                winnerIndex = winner
            )
        }

        return updatedGame
    }

    // Передать ход следующему игроку
    fun nextPlayer(game: CricketGame): CricketGame {
        val next = (game.currentPlayerIndex + 1) % game.players.size
        return game.copy(
            currentPlayerIndex = next,
            currentTurnDarts = 0
        )
    }

    // Начать новую игру
    fun newGame(
        type: CricketType,
        players: List<CricketPlayer>
    ): CricketGame {
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
