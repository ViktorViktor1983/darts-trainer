package com.lodkin.dartstrainer.data.cricket

import kotlin.random.Random

// ИИ бота для крикета
object CricketBotAI {

    // Полный ход бота (3 дротика)
    fun performTurn(game: CricketGame, playerIndex: Int): CricketGame {
        val player = game.players[playerIndex]
        val bot = CRICKET_BOTS.firstOrNull { it.id == player.botLevel } ?: CRICKET_BOTS[0]

        val preferScore = decidePreferScore(game, playerIndex)
        val target = chooseTargetSector(game, playerIndex, preferScore)

        val startLeg = game.currentLegNumber
        val startSet = game.currentSetNumber

        var currentGame = game
        var dartsThrown = 0

        while (dartsThrown < 3 && !currentGame.isFinished) {
            if (currentGame.currentLegNumber != startLeg ||
                currentGame.currentSetNumber != startSet
            ) break

            val marks = marksForDart(bot)
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
    // Решение: набирать очки или закрывать?
    // true = набирать очки, false = закрывать сектора
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

        // Если всё закрыто — только набирать очки
        if (notClosedByMe.isEmpty()) return true

        // Если проигрываем — сначала набрать очки (если есть где)
        if (diff < 0) {
            val scoringAvailable = CricketSector.ALL.any { sector ->
                CricketLogic.isClosedByTeam(game, myTeam, sector) &&
                    !CricketLogic.isClosedByTeam(game, otherTeam, sector)
            }
            return scoringAvailable
        }

        // Остался 1 сектор до победы и счёт не меньше соперника — закрывать!
        if (notClosedByMe.size == 1 && diff >= 0) return false

        // Проверка: есть ли где набирать очки
        val scoringAvailable = CricketSector.ALL.any { sector ->
            CricketLogic.isClosedByTeam(game, myTeam, sector) &&
                !CricketLogic.isClosedByTeam(game, otherTeam, sector)
        }
        if (!scoringAvailable) return false  // нечего набирать — закрываем

        // Вероятностный выбор в зависимости от преимущества
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
    // preferScore = true → приоритет на сектора для очков
    // ─────────────────────────────────────────────
    private fun chooseTargetSector(
        game: CricketGame,
        playerIndex: Int,
        preferScore: Boolean
    ): CricketSector {
        val player = game.players[playerIndex]
        val myTeam = player.teamIndex
        val otherTeam = 1 - myTeam

        // Сектора, где я закрыл, а соперник — нет (можно набирать очки)
        val scoringSectors = CricketSector.ALL.filter { sector ->
            CricketLogic.isClosedByTeam(game, myTeam, sector) &&
                !CricketLogic.isClosedByTeam(game, otherTeam, sector)
        }

        // Сектора, где соперник закрыл, а я — нет (срочно закрыть!)
        val threatened = CricketSector.ALL.filter { sector ->
            !CricketLogic.isClosedByTeam(game, myTeam, sector) &&
                CricketLogic.isClosedByTeam(game, otherTeam, sector)
        }

        // Мои незакрытые сектора
        val notClosedByMe = CricketSector.ALL.filter { sector ->
            !CricketLogic.isClosedByTeam(game, myTeam, sector)
        }

        // Порядок приоритетов в зависимости от стратегии
        val groups = if (preferScore) {
            listOf(scoringSectors, threatened, notClosedByMe)
        } else {
            listOf(threatened, notClosedByMe, scoringSectors)
        }

        for (group in groups) {
            if (group.isNotEmpty()) {
                // Приоритет секторов: 20, 19, 18, 17, 16, 15, Bull
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
    // ─────────────────────────────────────────────
    private fun marksForDart(bot: CricketBot): Int {
        val targetMPR = (bot.averageMin + bot.averageMax) / 20.0
        val targetMean = targetMPR / 3.0

        // E = 0.25 + 1.76s  =>  s = (targetMean - 0.25) / 1.76
        val s = ((targetMean - 0.25) / 1.76).coerceIn(0.02, 1.0)

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
