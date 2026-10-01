package com.lodkin.dartstrainer.data.game501

// Логика игры 501 — чистые функции без UI
object Game501Logic {

    // ─────────────────────────────────────────────
    // Начать новый матч
    // ─────────────────────────────────────────────
    fun newGame(
        mode: Game501Mode,
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
                score = mode.startScore,
                turnScore = 0,
                turnDarts = 0,
                legDarts = 0,
                legScoreGained = 0,
                legDoublesHit = 0,
                legDoublesAttempted = 0
            )
        }

        return Game501(
            mode = mode,
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

    // ─────────────────────────────────────────────
    // Один бросок (S/D/T/Miss)
    // ─────────────────────────────────────────────
    fun applyThrow(game: Game501, sector: Int, multiplier: ThrowMultiplier): Game501 {
        if (game.isFinished) return game
        val playerIndex = game.currentPlayerIndex
        val player = game.players[playerIndex]

        // Если бросок мимо — просто тратим дротик
        if (multiplier == ThrowMultiplier.MISS) {
            return registerMiss(game, playerIndex)
        }

        val points = sector * multiplier.value
        val newScore = player.score - points
        val isDouble = multiplier == ThrowMultiplier.DOUBLE

        // Проверяем условия закрытия
        val canFinishOnThisThrow = when (game.mode) {
            Game501Mode.X501_DOUBLE_OUT -> isDouble && newScore == 0
            Game501Mode.X501_DOUBLE_IN_OUT -> isDouble && newScore == 0 && player.turnScore > 0
            Game501Mode.X301 -> isDouble && newScore == 0
            Game501Mode.STRAIGHT_OUT -> newScore == 0
        }

        // Перебор (bust) в Double Out / 301 / Double In
        val isBust = when {
            newScore < 0 -> true
            newScore == 1 && requiresDoubleOut(game.mode) -> true
            newScore == 0 && !canFinishOnThisThrow && requiresDoubleOut(game.mode) -> true
            else -> false
        }

        if (isBust) {
            return registerBust(game, playerIndex)
        }

        // Обновляем игрока
        val updatedPlayers = game.players.toMutableList()
        val updatedPlayer = player.copy(
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
        updatedPlayers[playerIndex] = updatedPlayer

        var updatedGame = game.copy(players = updatedPlayers)

        // Победа в леге?
        if (newScore == 0 && canFinishOnThisThrow) {
            updatedGame = finishLeg(updatedGame, updatedPlayer.teamIndex)
            return updatedGame
        }

        return updatedGame
    }

    // ─────────────────────────────────────────────
    // Ввод суммы за подход (для человека)
    // ─────────────────────────────────────────────
    fun applyTurnScore(game: Game501, gained: Int): Game501 {
        if (game.isFinished) return game
        if (gained <= 0) return game
        val playerIndex = game.currentPlayerIndex
        val player = game.players[playerIndex]
        val newScore = player.score - gained

        // Bust: ушли в минус или оставили 1 (в Double Out)
        if (newScore < 0 || (newScore == 1 && requiresDoubleOut(game.mode))) {
            return registerBust(game, playerIndex)
        }

        // Нельзя закрыть лег через ввод суммы — только через бросок в удвоение
        // Поэтому при newScore == 0 — тоже bust
        if (newScore == 0 && requiresDoubleOut(game.mode)) {
            return registerBust(game, playerIndex)
        }

        val updatedPlayers = game.players.toMutableList()
        updatedPlayers[playerIndex] = player.copy(
            score = newScore,
            turnScore = player.turnScore + gained,
            turnDarts = 3,
            legDarts = player.legDarts + 3,
            legScoreGained = player.legScoreGained + gained,
            matchDarts = player.matchDarts + 3,
            matchScoreGained = player.matchScoreGained + gained
        )
        return game.copy(players = updatedPlayers)
    }

    // ─────────────────────────────────────────────
    // Ввод через «Остаток» (программа сама считает сумму)
    // ─────────────────────────────────────────────
    fun applyTurnRemaining(game: Game501, remaining: Int): Game501 {
        val player = game.currentPlayer ?: return game
        val gained = player.score - remaining
        return applyTurnScore(game, gained)
    }

    // ─────────────────────────────────────────────
    // Закрытие лега вручную (кнопка «Лег»)
    // ─────────────────────────────────────────────
    fun closeLegManually(game: Game501, dartsUsed: Int): Game501 {
        if (game.isFinished) return game
        val playerIndex = game.currentPlayerIndex
        val player = game.players[playerIndex]

        val updatedPlayers = game.players.toMutableList()
        updatedPlayers[playerIndex] = player.copy(
            score = 0,
            turnScore = player.turnScore + player.score,
            turnDarts = player.turnDarts + dartsUsed,
            legDarts = player.legDarts + dartsUsed,
            legScoreGained = player.legScoreGained + player.score,
            legDoublesHit = player.legDoublesHit + 1,
            legDoublesAttempted = player.legDoublesAttempted + 1,
            matchDarts = player.matchDarts + dartsUsed,
            matchScoreGained = player.matchScoreGained + player.score,
            matchDoublesHit = player.matchDoublesHit + 1,
            matchDoublesAttempted = player.matchDoublesAttempted + 1
        )
        return finishLeg(game.copy(players = updatedPlayers), player.teamIndex)
    }

    // ─────────────────────────────────────────────
    // Промах (тратим дротик)
    // ─────────────────────────────────────────────
    private fun registerMiss(game: Game501, playerIndex: Int): Game501 {
        val updatedPlayers = game.players.toMutableList()
        val player = updatedPlayers[playerIndex]
        updatedPlayers[playerIndex] = player.copy(
            turnDarts = player.turnDarts + 1,
            legDarts = player.legDarts + 1,
            matchDarts = player.matchDarts + 1
        )
        return game.copy(players = updatedPlayers)
    }

    // ─────────────────────────────────────────────
    // Перебор (bust) — сбрасываем подход
    // ─────────────────────────────────────────────
    private fun registerBust(game: Game501, playerIndex: Int): Game501 {
        // Уже накопили turnDarts за этот подход? Считаем их все в статистику
        val updatedPlayers = game.players.toMutableList()
        val player = updatedPlayers[playerIndex]
        val dartsUsedInTurn = (3 - (3 - player.turnDarts)).coerceIn(0, 3)

        updatedPlayers[playerIndex] = player.copy(
            // Счёт возвращается к тому, что был в начале подхода
            score = player.score + player.turnScore,  // turnScore не увеличивали, но на всякий
            turnScore = 0,
            turnDarts = 3,  // считаем, что подход закончен
            legDarts = player.legDarts + (3 - player.turnDarts).coerceAtLeast(0),
            matchDarts = player.matchDarts + (3 - player.turnDarts).coerceAtLeast(0)
        )
        return game.copy(players = updatedPlayers)
    }

    // ─────────────────────────────────────────────
    // Завершение подхода (кнопка OK / АвтоОК)
    // ─────────────────────────────────────────────
    fun finishTurn(game: Game501): Game501 {
        if (game.isFinished) return game
        val playerIndex = game.currentPlayerIndex
        val player = game.players[playerIndex]

        // Если вручную ввели сумму — turnDarts уже = 3. Иначе добавляем недостающие промахи.
        val missing = (3 - player.turnDarts).coerceAtLeast(0)
        val updatedPlayers = game.players.toMutableList()
        updatedPlayers[playerIndex] = player.copy(
            turnDarts = 0,
            turnScore = 0,
            legDarts = player.legDarts + missing,
            matchDarts = player.matchDarts + missing
        )

        return nextPlayer(game.copy(players = updatedPlayers))
    }

    // ─────────────────────────────────────────────
    // Переход к следующему игроку
    // ─────────────────────────────────────────────
    fun nextPlayer(game: Game501): Game501 {
        val next = (game.currentPlayerIndex + 1) % game.players.size
        return game.copy(currentPlayerIndex = next)
    }

    // ─────────────────────────────────────────────
    // Завершение лега: победа + сброс
    // ─────────────────────────────────────────────
    private fun finishLeg(game: Game501, winningTeam: Int): Game501 {
        // Снимок лега до сброса
        val snapshot = LegSnapshot501(
            setNumber = game.currentSetNumber,
            legNumber = game.currentLegNumber,
            winningTeam = winningTeam,
            players = game.players.map { p ->
                LegPlayerSnapshot501(
                    name = p.name,
                    teamIndex = p.teamIndex,
                    isBot = p.isBot,
                    darts = p.legDarts,
                    scoreGained = p.legScoreGained,
                    doublesHit = p.legDoublesHit,
                    doublesAttempted = p.legDoublesAttempted
                )
            }
        )

        val updatedPlayers = game.players.toMutableList()
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
            // Новый сет: сброс легов, счёт 501
            val resetPlayers = updatedPlayers.map { p ->
                p.copy(
                    score = game.mode.startScore,
                    turnScore = 0,
                    turnDarts = 0,
                    legDarts = 0,
                    legScoreGained = 0,
                    legDoublesHit = 0,
                    legDoublesAttempted = 0,
                    legsInCurrentSet = 0
                )
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

        // Новый лег: сброс счёта
        for (i in updatedPlayers.indices) {
            if (updatedPlayers[i].teamIndex == winningTeam) {
                updatedPlayers[i] = updatedPlayers[i].copy(legsInCurrentSet = newLegsInCurrentSet)
            }
        }
        val resetPlayers = updatedPlayers.map { p ->
            p.copy(
                score = game.mode.startScore,
                turnScore = 0,
                turnDarts = 0,
                legDarts = 0,
                legScoreGained = 0,
                legDoublesHit = 0,
                legDoublesAttempted = 0
            )
        }
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

    // ─────────────────────────────────────────────
    // Утилиты
    // ─────────────────────────────────────────────
    private fun requiresDoubleOut(mode: Game501Mode): Boolean =
        mode == Game501Mode.X501_DOUBLE_OUT ||
        mode == Game501Mode.X501_DOUBLE_IN_OUT ||
        mode == Game501Mode.X301

    // Средний набор за лег (PPR) по игроку
    fun legPpr(player: Player501): Double {
        if (player.legDarts < 3) return 0.0
        return player.legScoreGained.toDouble() / (player.legDarts / 3.0)
    }

    // Средний набор за матч
    fun matchPpr(player: Player501): Double {
        if (player.matchDarts < 3) return 0.0
        return player.matchScoreGained.toDouble() / (player.matchDarts / 3.0)
    }

    // Точность удвоений за матч
    fun doublesAccuracy(player: Player501): Double {
        if (player.matchDoublesAttempted <= 0) return 0.0
        return player.matchDoublesHit.toDouble() / player.matchDoublesAttempted * 100.0
    }
}
