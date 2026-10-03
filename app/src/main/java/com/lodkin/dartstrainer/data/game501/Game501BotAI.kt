package com.lodkin.dartstrainer.data.game501

import kotlin.random.Random

// ИИ бота для игры x01
object Game501BotAI {

    // ─────────────────────────────────────────────
    // Полный ход бота (до 3 дротиков)
    // Использует Game501Logic.applyThrow — вся логика там.
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
        var previousMissed = false

        while (dartsThrown < 3 && !currentGame.isFinished) {
            if (currentGame.currentLegNumber != startLeg ||
                currentGame.currentSetNumber != startSet
            ) break

            val current = currentGame.players.getOrNull(playerIndex) ?: break
            val target = chooseTarget(current, previousMissed)
                ?: break

            val result = simulateDart(target, multiplier, current.botLevel)
            previousMissed = !result.isHit

            if (result.actualSector == 0) {
                // Мимо
                currentGame = Game501Logic.applyThrow(
                    currentGame,
                    20,
                    ThrowMultiplier.MISS,
                    playerIndex
                )
            } else {
                val mult = when (result.actualMultiplier) {
                    2 -> ThrowMultiplier.DOUBLE
                    3 -> ThrowMultiplier.TRIPLE
                    else -> ThrowMultiplier.SINGLE
                }
                currentGame = Game501Logic.applyThrow(
                    currentGame,
                    result.actualSector,
                    mult,
                    playerIndex
                )
            }
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
        val isHit: Boolean          // попал в цель или нет
    )

    private fun simulateDart(
        target: CheckoutThrow,
        formMultiplier: Double,
        botLevel: Int
    ): SimulatedThrow {
        // Базовая сложность попадания в целевой множитель
        val baseAcc = when (target.multiplier) {
            1 -> 0.85    // S
            2 -> 0.35    // D
            3 -> 0.30    // T
            else -> 0.5
        }

        val levelAcc = (botLevel / 16.0).coerceIn(0.05, 1.0)
        val finalAcc = (baseAcc * levelAcc * formMultiplier).coerceIn(0.01, 0.99)

        // Попал в цель?
        if (Random.nextDouble() < finalAcc) {
            return SimulatedThrow(target.sector, target.multiplier, isHit = true)
        }

        // Не попал. С шансом 40% — попал в S того же сектора (промах рядом)
        if (Random.nextDouble() < 0.4 && target.sector in 1..20) {
            return SimulatedThrow(target.sector, 1, isHit = false)
        }

        // Иначе — мимо (сектор 0)
        return SimulatedThrow(0, 0, isHit = false)
    }

    // ─────────────────────────────────────────────
    // Выбор цели
    // ─────────────────────────────────────────────
    private fun chooseTarget(
        player: Player501,
        previousMissed: Boolean
    ): CheckoutThrow? {
        val score = player.score

        // 1. Bogey numbers и большой остаток — T20 или T19 по чётности
        if (score > 170 || !CheckoutTable.isCheckoutPossible(score)) {
            return if (score % 2 == 0) {
                CheckoutThrow(20, 3)  // T20
            } else {
                CheckoutThrow(19, 3)  // T19
            }
        }

        // 2. Закрытие — есть путь в таблице
        val paths = CheckoutTable.pathsFor(score) ?: return CheckoutThrow(20, 3)

        // Если предыдущий дротик был промахом — берём «Промах ...»
        val path = if (previousMissed) {
            paths.firstOrNull { it.label.startsWith("Промах") } ?: paths.first()
        } else {
            paths.first()
        }

        val dartsUsed = player.turnDarts
        if (dartsUsed >= path.throws.size) {
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
}
