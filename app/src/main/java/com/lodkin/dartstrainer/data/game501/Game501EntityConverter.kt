package com.lodkin.dartstrainer.data.game501

/**
 * Конвертер сохранённой в базе партии (Game501Entity) обратно в объект Game501.
 * Нужен для показа подробной статистики уже сыгранного матча из списка.
 *
 * ВАЖНО: у завершённых партий не сохраняется legHistory — блок «ПО ЛЕГАМ»
 * для старых матчей будет пустой. Всё остальное восстанавливается.
 */
object Game501EntityConverter {

    fun toGame501(entity: Game501Entity): Game501? {
        return try {
            val gameType = GameType.valueOf(entity.gameTypeName)
            val outMode = OutMode.valueOf(entity.outModeName)

            val names = parseStringList(entity.playerNames)
            val bots = parseStringList(entity.playerIsBot)
            val darts = parseIntList(entity.matchDarts)
            val scores = parseIntList(entity.matchScore)
            val hits = parseIntList(entity.doublesHit)
            val attempts = parseIntList(entity.doublesAttempted)
            val c180 = parseIntList(entity.matchCount180)
            val c170 = parseIntList(entity.matchCount170plus)
            val c130 = parseIntList(entity.matchCount130plus)
            val c90 = parseIntList(entity.matchCount90plus)
            val c57p = parseIntList(entity.matchCount57plus)
            val c57m = parseIntList(entity.matchCount57minus)
            val f9s = parseIntList(entity.first9Score)
            val f9d = parseIntList(entity.first9Darts)
            val ncs = parseIntList(entity.nonCloseScore)
            val ncd = parseIntList(entity.nonCloseDarts)
            val bestLegPpr = parseDoubleList(entity.bestLegPpr)

            val closeValuesByPlayer = entity.closeValues.split("|")

            val players = mutableListOf<Player501>()
            for (i in names.indices) {
                val name = names[i]
                val isBot = bots.getOrNull(i) == "1"
                val teamIndex = if (entity.isPairGame) (i % 2) else i

                val player = Player501(
                    name = name,
                    isBot = isBot,
                    botLevel = 0,
                    teamIndex = teamIndex
                )
                player.matchDarts = darts.getOrElse(i) { 0 }
                player.matchScoreGained = scores.getOrElse(i) { 0 }
                player.matchDoublesHit = hits.getOrElse(i) { 0 }
                player.matchDoublesAttempted = attempts.getOrElse(i) { 0 }
                player.matchCount180 = c180.getOrElse(i) { 0 }
                player.matchCount170plus = c170.getOrElse(i) { 0 }
                player.matchCount130plus = c130.getOrElse(i) { 0 }
                player.matchCount90plus = c90.getOrElse(i) { 0 }
                player.matchCount57plus = c57p.getOrElse(i) { 0 }
                player.matchCount57minus = c57m.getOrElse(i) { 0 }
                player.first9Score = f9s.getOrElse(i) { 0 }
                player.first9Darts = f9d.getOrElse(i) { 0 }
                player.nonCloseScore = ncs.getOrElse(i) { 0 }
                player.nonCloseDarts = ncd.getOrElse(i) { 0 }

                // Средний дротиков на лег — заполняем listOfLegDarts одним значением,
                // чтобы average() в отчёте выдал правильное число.
                val legsPlayed = if (entity.legsPlayed > 0) entity.legsPlayed else 1
                val avgDartsPerLeg = player.matchDarts.toDouble() / legsPlayed
                player.listOfLegDarts.clear()
                player.listOfLegDarts.add(avgDartsPerLeg.toInt().coerceAtLeast(0))

                // Лучший PPR за лег — заполняем listOfLegPpr, чтобы bestLegPpr работал.
                bestLegPpr.getOrNull(i)?.let { if (it > 0.0) player.listOfLegPpr.add(it) }

                // Закрытые чекауты
                val myClose = closeValuesByPlayer.getOrNull(i) ?: ""
                player.listOfCloseValues.clear()
                if (myClose.isNotBlank()) {
                    myClose.split(",").forEach { raw ->
                        val v = raw.trim().toIntOrNull()
                        if (v != null) player.listOfCloseValues.add(v)
                    }
                }

                players.add(player)
            }

            Game501(
                gameType = gameType,
                outMode = outMode,
                players = players,
                currentPlayerIndex = 0,
                isFinished = true,
                winnerIndex = if (entity.winnerIndex >= 0) entity.winnerIndex else null,
                legsPerSet = 1,
                setsPerMatch = 1,
                currentLegNumber = 1,
                currentSetNumber = 1,
                lastLegWinnerIndex = null,
                lastSetWinnerIndex = null,
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
