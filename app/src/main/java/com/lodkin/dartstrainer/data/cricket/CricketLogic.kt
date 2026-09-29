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

    // Закрыт ли сектор у ОДНОГО игрока (личная статистика)
    fun isClosed(player: CricketPlayer, sector: CricketSector): Boolean {
        return (player.hits[sector] ?: 0) >= 3
    }

    // Закрыт ли сектор у КОМАНДЫ (сумма меток всех игроков команды >= 3)
    fun isClosedByTeam(game: CricketGame, team: Int, sector: CricketSector): Boolean {
        val totalHits = game.playersOfTeam(team).sumOf { it.hits[sector] ?: 0 }
        return totalHits >= 3
    }

    fun isClosedByAllTeams(game: CricketGame, sector: CricketSector): Boolean {
        return (0 until game.teamCount).all { isClosedByTeam(game, it, sector) }
    }

    // Все ли сектора закрыты у команды
    fun hasClosedAllTeam(game: CricketGame, team: Int): Boolean {
        return CricketSector.ALL.all { isClosedByTeam(game, team, it) }
    }

    // Сумма очков команды в ТЕКУЩЕМ леге
    fun teamTotalScore(game: CricketGame, team: Int): Int {
        return game.playersOfTeam(team).sumOf { it.totalScore }
    }

    // Сумма очков команды за ВЕСЬ МАТЧ
    fun teamMatchScore(game: CricketGame, team: Int): Int {
        return game.playersOfTeam(team).sumOf { it.matchTotalScore }
    }

    // Сброс состояния игрока для нового лега.
    // Накопительные match* поля НЕ сбрасываются.
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
                dartsThrown = player.dartsThrown + 1,
                matchDartsThrown = player.matchDartsThrown + 1
            )
            return game.copy(
                players = updatedPlayers,
                currentTurnDarts = game.currentTurnDarts + 1
            )
        }

        val multiplier = multiplierOf(result)
        val updatedPlayers = game.players.toMutableList()
        val player = updatedPlayers[playerIndex]

        // Сколько меток в этом секторе у КОМАНДЫ игрока ДО броска
        val teamHitsBefore = game.playersOfTeam(player.teamIndex)
            .sumOf { it.hits[sector] ?: 0 }
        val teamAlreadyClosed = teamHitsBefore >= 3

        // Личные метки игрока в этом секторе
        val currentHits = player.hits[sector] ?: 0
        val hitsToAdd = minOf(multiplier, 3 - currentHits)
        val newHits = currentHits + hitsToAdd

        // Сколько попаданий уйдёт в очки (закрытие команды уже случилось или случится сейчас)
        val overflow: Int = if (teamAlreadyClosed) {
            multiplier
        } else {
            val afterTeamHits = teamHitsBefore + hitsToAdd
            if (afterTeamHits > 3) afterTeamHits - 3 else 0
        }

        val newHitsMap = player.hits.toMutableMap()
        newHitsMap[sector] = newHits

        val newScores = player.scores.toMutableMap()
        var scoreGained = 0

        if (game.type == CricketType.AMERICAN && overflow > 0) {
            // Проверяем: сектор закрыт у ДРУГИХ команд?
            val myTeam = player.teamIndex
            val otherTeams = (0 until game.teamCount).filter { it != myTeam }
            val anyOtherTeamClosed = otherTeams.any { isClosedByTeam(game, it, sector) }

            if (!anyOtherTeamClosed) {
                val sectorValue = if (sector == CricketSector.BULL) 25 else sector.number
                scoreGained = overflow * sectorValue
                newScores[sector] = (newScores[sector] ?: 0) + scoreGained
            }
        }

        // Накопительные данные за матч
        val newMatchHits = player.matchHits.toMutableMap()
        newMatchHits[sector] = (newMatchHits[sector] ?: 0) + hitsToAdd

        val newMatchScores = player.matchScores.toMutableMap()
        if (scoreGained > 0) {
            newMatchScores[sector] = (newMatchScores[sector] ?: 0) + scoreGained
        }

        updatedPlayers[playerIndex] = player.copy(
            hits = newHitsMap,
            scores = newScores,
            totalScore = player.totalScore + scoreGained,
            dartsThrown = player.dartsThrown + 1,
            matchHits = newMatchHits,
            matchScores = newMatchScores,
            matchTotalScore = player.matchTotalScore + scoreGained,
            matchDartsThrown = player.matchDartsThrown + 1
        )

        val updatedGame = game.copy(
            players = updatedPlayers,
            currentTurnDarts = game.currentTurnDarts + 1
        )

        // Проверка победы в ЛЕГЕ (по командам)
        val legWinnerTeam = checkLegWinner(updatedGame)
        if (legWinnerTeam != null) {
            return awardLegWin(updatedGame, legWinnerTeam)
        }

        return updatedGame
    }

    // Определение команды-победителя ЛЕГА (возвращает teamIndex или null)
    fun checkLegWinner(game: CricketGame): Int? {
        if (game.players.isEmpty()) return null

        val closedAllTeams = (0 until game.teamCount).filter { team ->
            hasClosedAllTeam(game, team)
        }

        if (closedAllTeams.isEmpty()) return null

        if (game.type == CricketType.NO_SCORE) {
            return closedAllTeams.first()
        }

        // American: побеждает команда с максимальным счётом
        val maxScore = closedAllTeams.maxOf { teamTotalScore(game, it) }
        return closedAllTeams.firstOrNull { teamTotalScore(game, it) == maxScore }
    }

    // Присуждение победы в леге и продвижение по сетам/матчу
    fun awardLegWin(game: CricketGame, winningTeam: Int): CricketGame {
        val updatedPlayers = game.players.toMutableList()

        // Сколько легов у команды-победителя уже было (у всех игроков одинаково)
        val currentLegs = updatedPlayers
            .first { it.teamIndex == winningTeam }
            .legsInCurrentSet
        val newLegsInCurrentSet = currentLegs + 1
        val setWon = newLegsInCurrentSet >= game.legsPerSet

        if (setWon) {
            val currentSets = updatedPlayers
                .first { it.teamIndex == winningTeam }
                .setsWon
            val newSetsWon = currentSets + 1

            // Обновляем счёт у всех игроков команды
            for (i in updatedPlayers.indices) {
                val p = updatedPlayers[i]
                if (p.teamIndex == winningTeam) {
                    updatedPlayers[i] = p.copy(
                        legsInCurrentSet = newLegsInCurrentSet,
                        setsWon = newSetsWon
                    )
                }
            }

            if (newSetsWon >= game.setsPerMatch) {
                // Победа в МАТЧЕ
                return game.copy(
                    players = updatedPlayers,
                    isFinished = true,
                    winnerIndex = winningTeam,
                    lastLegWinnerIndex = null,
                    lastSetWinnerIndex = winningTeam
                )
            }

            // Новый сет: сброс легов и личной статистики у ВСЕХ
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
                lastSetWinnerIndex = winningTeam
            )
        }

        // Новый лег
        for (i in updatedPlayers.indices) {
            val p = updatedPlayers[i]
            if (p.teamIndex == winningTeam) {
                updatedPlayers[i] = p.copy(legsInCurrentSet = newLegsInCurrentSet)
            }
        }

        val resetPlayers = updatedPlayers.map { resetPlayerForNewLeg(it) }
        return game.copy(
            players = resetPlayers,
            currentPlayerIndex = 0,
            currentTurnDarts = 0,
            currentLegNumber = game.currentLegNumber + 1,
            lastLegWinnerIndex = winningTeam,
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
        setsPerMatch: Int = 1,
        isPairGame: Boolean = false
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
            lastSetWinnerIndex = null,
            isPairGame = isPairGame,
            teamCount = 2,
            playersPerTeam = if (isPairGame) 2 else 1
        )
    }
}
