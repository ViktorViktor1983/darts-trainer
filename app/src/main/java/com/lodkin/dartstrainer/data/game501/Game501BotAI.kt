package com.lodkin.dartstrainer.data.game501

import kotlin.random.Random

// ИИ бота для игры x01
object Game501BotAI {

    // ─────────────────────────────────────────────
    // Полный ход бота (до 3 дротиков)
    // ─────────────────────────────────────────────
    fun performTurn(game: Game501, playerIndex: Int): Game501 {
        val player = game.players.getOrNull(playerIndex) ?: return game
        if (!player.isBot) return game

        // Обновляем серию
        val updatedPlayer = updateStreak(player)
        val playersWithStreak = game.players.toMutableList()
        playersWithStreak[playerIndex] = updatedPlayer
        var currentGame = game.copy(players = playersWithStreak)

        // Итоговый множитель точности: форма дня × серия × усталость
        val multiplier = game.sessionForm *
            updatedPlayer.botStreak *
            fatigueFactor(game.sessionStartTime)

        val startLeg = currentGame.currentLegNumber
        val startSet = currentGame.currentSetNumber

        var dartsThrown = 0
        var previousMissed = false  // был ли промах в предыдущем дротике

        while (dartsThrown < 3 && !currentGame.isFinished) {
            if (currentGame.currentLegNumber != startLeg ||
                currentGame.currentSetNumber != startSet
            ) break

            val current = currentGame.players[playerIndex]
            val target = chooseTarget(current, game.outMode, previousMissed)
                ?: break  // нет цели — выходим

            val result = simulateDart(target, multiplier, current.botLevel)
            previousMissed = !result.isHit

            // Применяем бросок
            currentGame = applySimulatedThrow(currentGame, playerIndex, result, target)
            dartsThrown++
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

    // ─────────────────────────────────────────────
    // Симуляция одного дротика
    // ─────────────────────────────────────────────
    private data class SimulatedThrow(
        val actualSector: Int,      // 0 если мимо
        val actualMultiplier: Int,  // 1=S, 2=D, 3=T, 0=miss
        val isHit: Boolean          // попал в цель (не в S из-за промаха)
    )

    private fun simulateDart(
        target: CheckoutThrow,
        formMultiplier: Double,
        botLevel: Int
    ): SimulatedThrow {
        // Базовая сложность попадания в целевой множитель
        val baseAcc = when (target.multiplier) {
            1 -> 0.85
            2 -> 0.35
            3 -> 0.30
            else -> 0.5
        }

        val levelAcc = (botLevel / 16.0).coerceIn(0.05, 1.0)
        val finalAcc = (baseAcc * levelAcc * formMultiplier).coerceIn(0.01, 0.99)

        // Попал в цель?
        if (Random.nextDouble() < finalAcc) {
            return SimulatedThrow(target.sector, target.multiplier, isHit = true)
        }

        // Не попал в цель. Промах рядом (S того же сектора) или мимо?
        if (Random.nextDouble() < 0.4) {
            return SimulatedThrow(target.sector, 1, isHit = false)
        }

        // Мимо
        return SimulatedThrow(0, 0, isHit = false)
    }

    // ─────────────────────────────────────────────
    // Применение симулированного броска
    // ─────────────────────────────────────────────
    private fun applySimulatedThrow(
        game: Game501,
        playerIndex: Int,
        result: SimulatedThrow,
        target: CheckoutThrow
    ): Game501 {
        val player = game.players[playerIndex]

        // Мимо
        if (result.actualSector == 0) {
            val updated = game.players.toMutableList()
            updated[playerIndex] = player.copy(
                turnDarts = player.turnDarts + 1,
                legDarts = player.legDarts + 1,
                matchDarts = player.matchDarts + 1
            )
            return game.copy(players = updated)
        }

        val points = result.actualSector * result.actualMultiplier
        val newScore = player.score - points
        val isDouble = result.actualMultiplier == 2

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

        // Перебор — сбрасываем подход (без учёта в статистике)
        if (isBust) {
            val updated = game.players.toMutableList()
            val missing = (3 - player.turnDarts).coerceAtLeast(0)
            updated[playerIndex] = player.copy(
                turnScore = 0,
                turnDarts = 0,
                legDarts = player.legDarts + missing,
                matchDarts = player.matchDarts + missing
            )
            return Game501Logic.nextPlayer(game.copy(players = updated))
        }

        val updated = game.players.toMutableList()
        var updatedPlayer = player.copy(
            score = newScore,
            turnScore = player.turnScore + points,
            turnDarts = player.turnDarts + 1,
            legDarts = player.legDarts + 1,
            legScoreGained = player.legScoreGained + points,
            matchDarts = player.matchDarts + 1,
            matchScoreGained = player.matchScoreGained + points,
            legDoublesAttempted = if (isDouble) player.legDoublesAttempted + 1 else player.legDoublesAttempted,
            legDoublesHit = if (isDouble && newScore == 0) player.legDoublesHit + 1 else player.legDoublesHit,
            matchDoublesAttempted = if (isDouble) player.matchDoublesAttempted + 1 else player.matchDoublesAttempted,
            matchDoublesHit = if (isDouble && newScore == 0) player.matchDoublesHit + 1 else player.matchDoublesHit
        )

        // Первые 9 дротиков
        if (updatedPlayer.first9Darts < 9) {
            updatedPlayer = updatedPlayer.copy(
                first9Score = updatedPlayer.first9Score + points,
                first9Darts = updatedPlayer.first9Darts + 1
            )
        }
        // Набор без закрытия
        if (player.score > 170) {
            updatedPlayer = updatedPlayer.copy(
                nonCloseScore = updatedPlayer.nonCloseScore + points,
                nonCloseDarts = updatedPlayer.nonCloseDarts + 1
            )
        }

        updated[playerIndex] = updatedPlayer
        var updatedGame = game.copy(players = updated)

        // Проверка победы в леге
        if (newScore == 0 && canFinish) {
            // Записываем значение закрытия
            val closingValue = player.score
            val pWithClose = updatedGame.players[playerIndex].copy(
                listOfCloseValues = updatedGame.players[playerIndex].listOfCloseValues.toMutableList().also {
                    it.add(closingValue)
                }
            )
            val finalPlayers = updatedGame.players.toMutableList()
            finalPlayers[playerIndex] = pWithClose
            updatedGame = updatedGame.copy(players = finalPlayers)

            updatedGame = awardLegWinForBot(updatedGame, updatedPlayer.teamIndex)
        }

        return updatedGame
    }

    // ─────────────────────────────────────────────
    // Завершение лега (упрощённая версия для бота)
    // ─────────────────────────────────────────────
    private fun awardLegWinForBot(game: Game501, winningTeam: Int): Game501 {
        // Используем стандартный finishLeg через Game501Logic
        // Но он приватный. Поэтому просто вызываем ещё один applyTurnScore(0)
        // и надеемся, что finishLeg сработает.
        // Проще: вызываем внутренний closeLeg с 0 дротиков. Хм.

        // Вместо этого — используем публичный метод через Game501Logic
        // Нам нужно передать "закрытие" — но applyThrow уже обработает победу.

        // Проблема: applyThrow не закрывает лег автоматически.
        // Придётся вызывать finishLeg вручную.

        // Пока делаем через хак: вызываем closeLegManually с 0 дротиков,
        // который внутри вызывает finishLeg.

        return Game501Logic.closeLegManuallyForBot(game, winningTeam)
    }

    // ─────────────────────────────────────────────
    // Выбор цели
    // ─────────────────────────────────────────────
    private fun chooseTarget(
        player: Player501,
        outMode: OutMode,
        previousMissed: Boolean
    ): CheckoutThrow? {
        val score = player.score

        // 1. Bogey numbers и набор (>170)
        if (score > 170 || !CheckoutTable.isCheckoutPossible(score)) {
            // Стратегия: чётный → T20, нечётный → T19
            return if (score % 2 == 0) {
                CheckoutThrow(20, 3)  // T20
            } else {
                CheckoutThrow(19, 3)  // T19
            }
        }

        // 2. Закрытие — есть путь в таблице
        val paths = CheckoutTable.pathsFor(score) ?: return CheckoutThrow(20, 3)

        // Если предыдущий дротик был промахом — берём путь "Промах ..."
        val path = if (previousMissed) {
            paths.firstOrNull { it.label.startsWith("Промах") } ?: paths.first()
        } else {
            paths.first()
        }

        // Определяем, какой по счёту дротик сейчас в подходе
        val dartsUsed = player.turnDarts
        if (dartsUsed >= path.throws.size) {
            // Все дротики в пути использованы — берём последний
            return path.throws.last()
        }

        return path.throws[dartsUsed]
    }

    // ─────────────────────────────────────────────
    // Серия бота (как в крикете)
    // ─────────────────────────────────────────────
    private fun updateStreak(player: Player501): Player501 {
        if (player.botStreakLeft > 0) {
            return player.copy(botStreakLeft = player.botStreakLeft - 1)
        }
        if (Random.nextDouble() < 0.20) {
            val positive = Random.nextBoolean()
            val newStreak = if (positive) {
                1.10 + Random.nextDouble() * 0.10
            } else {
                0.80 + Random.nextDouble() * 0.10
            }
            val duration = 1 + Random.nextInt(2)
            return player.copy(botStreak = newStreak, botStreakLeft = duration - 1)
        }
        return player.copy(botStreak = 1.0, botStreakLeft = 0)
    }

    // ─────────────────────────────────────────────
    // Усталость (от времени сессии)
    // ─────────────────────────────────────────────
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

    private fun requiresDoubleOut(outMode: OutMode): Boolean =
        outMode == OutMode.DOUBLE_OUT || outMode == OutMode.DOUBLE_IN_OUT
}
