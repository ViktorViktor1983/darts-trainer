package com.lodkin.dartstrainer.data.cricket

/**
 * Конвертер сохранённой в базе партии (CricketGameEntity) обратно в объект CricketGame.
 * Нужен для показа подробной статистики уже сыгранного матча из списка.
 *
 * Логика:
 *  - Если партия НЕЗАВЕРШЁННАЯ — восстанавливаем её из stateBlob (полное состояние).
 *  - Если партия ЗАВЕРШЁННАЯ — собираем CricketGame только из сохранённых агрегатов
 *    (MPR, промахи, утроения, Bull, очки, дротики). legHistory и hits/scores по секторам
 *    у завершённых партий не сохраняются, поэтому блок «ПО ЛЕГАМ» для них будет пустой.
 *
 * ВАЖНО: MPR в CricketStatsScreen считается через matchHits.values.sum(),
 * поэтому мы «складываем» все метки в один сектор S20 — сумма получается правильной.
 */
object CricketEntityConverter {

    fun toCricketGame(entity: CricketGameEntity): CricketGame? {
        // Незавершённую восстанавливаем из полного состояния.
        if (!entity.isFinished && entity.stateBlob.isNotBlank()) {
            val fromBlob = CricketSerializer.fromJson(entity.stateBlob)
            if (fromBlob != null) return fromBlob
        }

        // Завершённую — собираем из агрегатов.
        return try {
            val names = parseStringList(entity.playerNames)
            val bots = parseStringList(entity.playerIsBot)
            val dartsList = parseIntList(entity.totalDarts)
            val missesList = parseIntList(entity.misses)
            val triplesList = parseIntList(entity.triples)
            val bullAttList = parseIntList(entity.bullAttempts)
            val bullHitList = parseIntList(entity.bullHits)
            val perfectList = parseIntList(entity.perfectRounds)
            val strongList = parseIntList(entity.strongRounds)
            val mprList = parseDoubleList(entity.mpr)

            val players = mutableListOf<CricketPlayer>()
            for (i in names.indices) {
                val name = names[i]
                val isBot = bots.getOrNull(i) == "1"
                val teamIndex = if (entity.isPairGame) (i % 2) else i

                val p = CricketPlayer(
                    name = name,
                    isBot = isBot,
                    botLevel = 0,
                    teamIndex = teamIndex
                )

                val darts = dartsList.getOrElse(i) { 0 }
                val mpr = mprList.getOrElse(i) { 0.0 }
                // Восстанавливаем сумму меток из MPR: marks = MPR * (darts / 3).
                val marks = if (darts >= 3) (mpr * darts / 3.0).toInt() else 0

                p.matchDartsThrown = darts
                p.matchMissesThrown = missesList.getOrElse(i) { 0 }
                p.matchTriplesHit = triplesList.getOrElse(i) { 0 }
                p.matchBullAttempts = bullAttList.getOrElse(i) { 0 }
                p.matchBullHits = bullHitList.getOrElse(i) { 0 }
                p.matchPerfectRounds = perfectList.getOrElse(i) { 0 }
                p.matchStrongRounds = strongList.getOrElse(i) { 0 }

                // Кладём всю сумму меток в S20, чтобы MatchPlayerCard показал правильный MPR.
                p.matchHits.clear()
                for (s in CricketSector.ALL) p.matchHits[s] = 0
                p.matchHits[CricketSector.S20] = marks

                // Очки за матч
                val scoreFromDb = 0 // в CricketGameEntity нет matchTotalScore отдельно
                p.matchTotalScore = scoreFromDb

                players.add(p)
            }

            CricketGame(
                type = CricketType.valueOf(entity.cricketType),
                players = players,
                currentPlayerIndex = 0,
                isFinished = true,
                winnerIndex = if (entity.winnerIndex >= 0) entity.winnerIndex else null,
                currentTurnDarts = 0,
                legsPerSet = 1,
                setsPerMatch = 1,
                currentLegNumber = 1,
                currentSetNumber = 1,
                lastLegWinnerIndex = null,
                lastSetWinnerIndex = null,
                lastLegWinnerPlayerIndex = null,
                lastLegDartsClicked = 0,
                lastLegStartingTeam = 0,
                autoOkSeconds = 0,
                sessionStartTime = entity.dateMillis,
                sessionForm = 1.0,
                legHistory = emptyList(),
                isPairGame = entity.isPairGame,
                teamCount = 2,
                playersPerTeam = if (entity.isPairGame) 2 else 1
            )
        } catch (e: Exception) {
            null
        }
    }

    private fun parseStringList(s: String): List<String> =
        if (s.isBlank()) emptyList() else s.split("|")

    private fun parseIntList(s: String): List<Int> =
        if (s.isBlank()) emptyList() else s.split("|").map { it.toIntOrNull() ?: 0 }

    private fun parseDoubleList(s: String): List<Double> =
        if (s.isBlank()) emptyList() else s.split("|").map { it.toDoubleOrNull() ?: 0.0 }
}
