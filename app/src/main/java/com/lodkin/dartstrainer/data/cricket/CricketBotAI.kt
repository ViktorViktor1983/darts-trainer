package com.lodkin.dartstrainer.data.cricket

import kotlin.random.Random

// ИИ бота для крикета
object CricketBotAI {

    // Выбор целевого сектора для подхода
    fun chooseTargetSector(game: CricketGame, playerIndex: Int): CricketSector {
        val player = game.players[playerIndex]
        val others = game.players.filterIndexed { i, _ -> i != playerIndex }

        // Приоритет 1: срочно закрыть то, что соперники уже закрыли, а бот — нет
        val threatened = CricketSector.ALL.filter { sector ->
            !CricketLogic.isClosed(player, sector) &&
                others.any { CricketLogic.isClosed(it, sector) }
        }
        if (threatened.isNotEmpty()) {
            for (pref in CricketSector.ALL) {
                if (pref in threatened) return pref
            }
            return threatened.first()
        }

        // Приоритет 2 (American): набрать очки там, где соперник ещё открыт
        if (game.type == CricketType.AMERICAN) {
            val scoringSectors = CricketSector.ALL.filter { sector ->
                CricketLogic.isClosed(player, sector) &&
                    others.any { !CricketLogic.isClosed(it, sector) }
            }
            if (scoringSectors.isNotEmpty()) {
                // Приоритет: 20 → 19 → 18 → 17 → 16 → 15 → Bull
                // (T19 эффективнее Bull по площади и очкам за бросок)
                for (pref in CricketSector.ALL) {
                    if (pref in scoringSectors) return pref
                }
            }
        }

        // Приоритет 3: закрывать свои незакрытые сектора по убыванию
        for (sector in CricketSector.ALL) {
            if (!CricketLogic.isClosed(player, sector)) return sector
        }

        // Всё закрыто — по инерции в 20
        return CricketSector.S20
    }

    // Полный ход бота (3 дротика). Возвращает обновлённое состояние игры.
    fun performTurn(game: CricketGame, playerIndex: Int): CricketGame {
        val player = game.players[playerIndex]
        val bot = CRICKET_BOTS.firstOrNull { it.id == player.botLevel } ?: CRICKET_BOTS[0]

        val target = chooseTargetSector(game, playerIndex)

        val startLeg = game.currentLegNumber
        val startSet = game.currentSetNumber

        var currentGame = game
        var dartsThrown = 0

        while (dartsThrown < 3 && !currentGame.isFinished) {
            // Если лег или сет сменились по ходу хода — прекращаем
            if (currentGame.currentLegNumber != startLeg ||
                currentGame.currentSetNumber != startSet
            ) break

            val marks = marksForDart(bot)
            val result = when (marks) {
                0 -> ThrowResult.MISS
                1 -> ThrowResult.SINGLE
                2 -> ThrowResult.DOUBLE
                else -> {
                    // Bull без утроения: 3 метки невозможны, понижаем до DOUBLE
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

        // Передать ход следующему, если лег не сменился и игра не завершилась
        val legChanged = currentGame.currentLegNumber != startLeg ||
            currentGame.currentSetNumber != startSet
        if (!currentGame.isFinished && !legChanged &&
            currentGame.currentPlayerIndex == playerIndex
        ) {
            currentGame = CricketLogic.nextPlayer(currentGame)
        }

        return currentGame
    }

    // Сколько меток принесёт один дротик (0..3) — модель "как у человека".
    // Матожидание подобрано под средний MPR бота, но с естественным разбросом.
    private fun marksForDart(bot: CricketBot): Int {
        // Целевой MPR бота (метки за подход)
        val targetMPR = (bot.averageMin + bot.averageMax) / 20.0
        // Целевые метки за дротик
        val targetMean = targetMPR / 3.0

        // Коэффициент "мастерства" (0..1).
        // Формула матожидания: E = 3*pT + 2*pD + 1*pS,
        // где pT = 0.45s, pD = 0.18s, pS = 0.25 + 0.05s
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
