package com.lodkin.dartstrainer.data.cricket

import kotlin.random.Random

// ИИ бота для крикета
object CricketBotAI {

    // Полный ход бота (3 дротика)
    fun performTurn(game: CricketGame, playerIndex: Int): CricketGame {
        val player = game.players[playerIndex]
        if (!player.isBot) return game

        // Обновляем состояние серии для этого подхода
        val updatedPlayer = updateStreak(player)

        // Сохраняем обновлённого игрока в игре
        val playersWithStreak = game.players.toMutableList()
        playersWithStreak[playerIndex] = updatedPlayer
        var currentGame = game.copy(players = playersWithStreak)

        val bot = CRICKET_BOTS.firstOrNull { it.id == updatedPlayer.botLevel } ?: CRICKET_BOTS[0]

        // Итоговый множитель точности: форма дня × серия × усталость
        val multiplier = game.sessionForm *
            updatedPlayer.botStreak *
            fatigueFactor(game.sessionStartTime)

        val preferScore = decidePreferScore(currentGame, playerIndex)
        val target = chooseTargetSector(currentGame, playerIndex, preferScore)

        val startLeg = currentGame.currentLegNumber
        val startSet = currentGame.currentSetNumber

        var dartsThrown = 0

        while (dartsThrown < 3 && !currentGame.isFinished) {
            if (currentGame.currentLegNumber != startLeg ||
                currentGame.currentSetNumber != startSet
            ) break

            val marks = marksForDart(bot, multiplier)
            val result = when (marks) {
                0 -> ThrowResult.MISS
                1 -> ThrowResult.SINGLE
                2 -> ThrowResult.DOUBLE
                else -> {
                    if (target == CricketSector.BULL) ThrowResult.DOUBLE
                    else ThrowResult.TRIPLE
                }
            }

            currentGame = CricketLogic.applyThrow(
                game = currentGame,
                sector = target,
                result = result,
                playerIndex = playerIndex
            )
            dartsThrown++
        }

        val legChanged = currentGame.currentLegNumber != startLeg ||
            currentGame.currentSetNumber != startSet
        if (!currentGame.isFinished && !legChanged &&
            currentGame.currentPlayerIndex == playerIndex
        ) {
            currentGame = CricketLogic.nextPlayer(currentGame)
        }

        return currentGame
    }

    // ─────────────────────────────────────────────
    // Серия (стрик) бота
    // ─────────────────────────────────────────────
    private fun updateStreak(player: CricketPlayer): CricketPlayer {
        // Если серия ещё идёт — уменьшаем счётчик, streak не меняется
        if (player.botStreakLeft > 0) {
            return player.copy(botStreakLeft = player.botStreakLeft - 1)
        }

        // Серия закончилась (или не начиналась). 25% шанс новой серии.
        if (Random.nextDouble() < 0.25) {
            val positive = Random.nextBoolean()
            val newStreak = if (positive) {
                1.10 + Random.nextDouble() * 0.10   // 1.10..1.20 «летит»
            } else {
                0.80 + Random.nextDouble() * 0.10   // 0.80..0.90 «не летит»
            }
            val duration = 2 + Random.nextInt(2)     // 2 или 3 подхода
            return player.copy(botStreak = newStreak, botStreakLeft = duration - 1)
        }

        // Обычное состояние
        return player.copy(botStreak = 1.0, botStreakLeft = 0)
    }

    // ─────────────────────────────────────────────
    // Усталость (глобальная, зависит от времени сессии)
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

    // ─────────────────────────────────────────────
    // Решение: набирать очки или закрывать?
    // ─────────────────────────────────────────────
    private fun decidePreferScore(game: CricketGame, playerIndex: Int): Boolean {
        val player = game.players[playerIndex]
        val myTeam = player.teamIndex
        val otherTeam = 1 - myTeam

        val myScore = CricketLogic.teamTotalScore(game, myTeam)
        val otherScore = CricketLogic.teamTotalScore(game, otherTeam)
        val diff = myScore - otherScore

        val notClosedByMe = CricketSector.ALL.filter { sector ->
            !CricketLogic.isClosedByTeam(game, myTeam, sector)
        }

        if (notClosedByMe.isEmpty()) return true

        if (diff < 0) {
            val scoringAvailable = CricketSector.ALL.any { sector ->
                CricketLogic.isClosedByTeam(game, myTeam, sector) &&
                    !CricketLogic.isClosedByTeam(game, otherTeam, sector)
            }
            return scoringAvailable
        }

        if (notClosedByMe.size == 1 && diff >= 0) return false

        val scoringAvailable = CricketSector.ALL.any { sector ->
            CricketLogic.isClosedByTeam(game, myTeam, sector) &&
                !CricketLogic.isClosedByTeam(game, otherTeam, sector)
        }
        if (!scoringAvailable) return false

        val pScore = when {
            diff < 50 -> 0.5
            diff < 100 -> 0.35
            diff < 150 -> 0.2
            diff < 200 -> 0.05
            else -> 0.0
        }

        return Random.nextDouble() < pScore
    }

    // ─────────────────────────────────────────────
    // Выбор целевого сектора
    // ─────────────────────────────────────────────
    private fun chooseTargetSector(
        game: CricketGame,
        playerIndex: Int,
        preferScore: Boolean
    ): CricketSector {
        val player = game.players[playerIndex]
        val myTeam = player.teamIndex
        val otherTeam = 1 - myTeam

        val scoringSectors = CricketSector.ALL.filter { sector ->
            CricketLogic.isClosedByTeam(game, myTeam, sector) &&
                !CricketLogic.isClosedByTeam(game, otherTeam, sector)
        }

        val threatened = CricketSector.ALL.filter { sector ->
            !CricketLogic.isClosedByTeam(game, myTeam, sector) &&
                CricketLogic.isClosedByTeam(game, otherTeam, sector)
        }

        val notClosedByMe = CricketSector.ALL.filter { sector ->
            !CricketLogic.isClosedByTeam(game, myTeam, sector)
        }

        val groups = if (preferScore) {
            listOf(scoringSectors, threatened, notClosedByMe)
        } else {
            listOf(threatened, notClosedByMe, scoringSectors)
        }

        for (group in groups) {
            if (group.isNotEmpty()) {
                for (pref in CricketSector.ALL) {
                    if (pref in group) return pref
                }
                return group.first()
            }
        }

        return CricketSector.S20
    }

    // ─────────────────────────────────────────────
    // Сколько меток принесёт один дротик (0..3)
    // multiplier = форма × серия × усталость
    // ─────────────────────────────────────────────
    private fun marksForDart(bot: CricketBot, multiplier: Double): Int {
        val targetMPR = (bot.averageMin + bot.averageMax) / 20.0
        val targetMean = targetMPR / 3.0

        // Базовый коэффициент мастерства
        val baseS = ((targetMean - 0.25) / 1.76).coerceIn(0.02, 1.0)

        // Применяем множитель (форма × серия × усталость)
        val s = (baseS * multiplier).coerceIn(0.02, 1.0)

        val pTriple = s * 0.45
        val pDouble = s * 0.18
        val pSingle = 0.25 + s * 0.05

        val r = Random.nextDouble()
        return when {
            r < pTriple -> 3
            r < pTriple + pDouble -> 2
            r < pTriple + pDouble + pSingle -> 1
            else -> 0
        }
    }
}
